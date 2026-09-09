#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../verification-check-host.ps1')

$script:Cases = 0
$script:Assertions = 0
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-verification-host-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-host', [Text.UTF8Encoding]::new($false))
$fixturePath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'fixtures/verification-child-fixture.ps1'))
$hostPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../verification-check-host.ps1'))
$pwshPath = (Get-Process -Id $PID).Path
$environment = [pscustomobject]@{ inherit = $true; variables = [pscustomobject]@{} }

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}
function Assert-CaseThrows {
    param([scriptblock]$Action, [string]$Pattern)
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_.Exception }
    Assert-Case ($null -ne $observed) "Expected exception matching $Pattern."
    Assert-Case ($observed.Message -match $Pattern) "Unexpected exception: $($observed.Message)"
}
function Invoke-Case {
    param([string]$Name, [scriptblock]$Action)
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}
function Invoke-FixtureHost {
    param(
        [string]$Name,
        [string]$Mode,
        [string]$Contract = 'SCIENTIFIC_SEMANTIC',
        [string]$Outcome = 'CONTRACT_SATISFIED',
        [int]$ExitCode = 0,
        [switch]$NoStructured,
        [object]$EnvironmentContract = $environment,
        [string[]]$AdditionalArguments = @(),
        [Collections.IDictionary]$EvidencePaths = @{}
    )
    $output = Join-Path $root $Name
    $structured = if ($NoStructured) { $null } else { Join-Path $output 'child-result.json' }
    $arguments = [Collections.Generic.List[string]]::new()
    foreach ($value in @('-NoProfile', '-File', $fixturePath, '-Mode', $Mode)) { $arguments.Add($value) }
    if (-not [string]::IsNullOrWhiteSpace($structured)) {
        $arguments.Add('-ResultPath')
        $arguments.Add($structured)
    }
    foreach ($value in @('-ContractClass', $Contract, '-Outcome', $Outcome, '-ExitCode', [string]$ExitCode)) {
        $arguments.Add($value)
    }
    foreach ($value in $AdditionalArguments) { $arguments.Add($value) }
    $nodeKind = if ($Contract.EndsWith('_DIAGNOSTIC', [StringComparison]::Ordinal)) {
        'DIAGNOSTIC_LEAF'
    } else {
        'ACCEPTANCE_LEAF'
    }
    return Invoke-VerificationChildProcess -CheckId $Name -NodeKind $nodeKind -Command $pwshPath -Arguments ([string[]]$arguments.ToArray()) -WorkingDirectory $root -EnvironmentContract $EnvironmentContract -OutputDirectory $output -StructuredOutputPath $structured -EvidencePaths $EvidencePaths
}

try {
    Invoke-Case 'successful semantic child preserves structured evidence' {
        $result = Invoke-FixtureHost success Structured
        Assert-Case ($result.completion_state -ceq 'COMPLETED') 'Semantic child did not complete.'
        Assert-Case ($result.exit_code -eq 0) 'Semantic child exit was not preserved.'
        Assert-Case ($result.structured_output.state -ceq 'PRESENT') 'Structured result was not loaded.'
        Assert-Case ($result.structured_output.value.outcome -ceq 'CONTRACT_SATISFIED') 'Structured semantic outcome changed.'
    }
    Invoke-Case 'nonzero semantic child remains raw evidence' {
        $result = Invoke-FixtureHost semantic-violation Structured SCIENTIFIC_SEMANTIC CONTRACT_VIOLATED 7
        Assert-Case ($result.completion_state -ceq 'COMPLETED') 'Nonzero child was not a normal completion.'
        Assert-Case ($result.exit_code -eq 7) 'Nonzero child exit changed.'
        Assert-Case ($result.structured_output.value.outcome -ceq 'CONTRACT_VIOLATED') 'Violation evidence changed.'
    }
    Invoke-Case 'diagnostic nonzero is captured without host verdict' {
        $result = Invoke-FixtureHost diagnostic-finding Structured STYLE_DIAGNOSTIC DIAGNOSTIC_FINDING 9
        Assert-Case ($result.exit_code -eq 9) 'Diagnostic exit was not preserved.'
        Assert-Case ($result.PSObject.Properties.Name -cnotcontains 'acceptance_verdict') 'Host assigned a verdict.'
        Assert-Case ($result.PSObject.Properties.Name -cnotcontains 'contract_class') 'Host assigned a contract class.'
    }
    Invoke-Case 'successful diagnostic child remains verdict-neutral' {
        $result = Invoke-FixtureHost diagnostic-clear Structured STYLE_DIAGNOSTIC DIAGNOSTIC_CLEAR 0
        Assert-Case ($result.node_kind -ceq 'DIAGNOSTIC_LEAF') 'Diagnostic node kind changed.'
        Assert-Case ($result.structured_output.value.outcome -ceq 'DIAGNOSTIC_CLEAR') 'Diagnostic clear evidence changed.'
        Assert-Case ($result.PSObject.Properties.Name -cnotcontains 'acceptance_verdict') 'Host assigned a diagnostic verdict.'
    }
    Invoke-Case 'PowerShell exception is preserved as child evidence' {
        $result = Invoke-FixtureHost exception Throw -NoStructured
        Assert-Case ($result.completion_state -ceq 'COMPLETED') 'Thrown child was misclassified as launch failure.'
        Assert-Case ($result.exit_code -ne 0) 'Thrown child exit was lost.'
        Assert-Case ($result.stderr_text -match 'VERIFICATION_CHILD_EXCEPTION_SENTINEL') 'Exception stderr was lost.'
    }
    Invoke-Case 'malformed and absent structured results remain distinct' {
        $malformed = Invoke-FixtureHost malformed Malformed
        $absent = Invoke-FixtureHost absent Absent
        Assert-Case ($malformed.structured_output.state -ceq 'MALFORMED') 'Malformed result was not identified.'
        Assert-Case ($absent.structured_output.state -ceq 'ABSENT') 'Absent result was not identified.'
    }
    Invoke-Case 'invalid UTF-8 preserves raw bytes' {
        $result = Invoke-FixtureHost invalid-utf8 InvalidUtf8 -NoStructured
        Assert-Case ($result.stdout_encoding -ceq 'INVALID_UTF8') 'Invalid UTF-8 was silently decoded.'
        Assert-Case ((Get-VerificationFileSha256 $result.stdout_log) -ceq $result.stdout_sha256) 'Raw stdout hash changed.'
        Assert-Case ([IO.File]::ReadAllBytes($result.stdout_log).Length -eq 4) 'Raw invalid output was not preserved.'
    }
    Invoke-Case 'simultaneous Unicode stdout and stderr are both preserved' {
        $previousEncoding = [Console]::OutputEncoding
        try {
            [Console]::OutputEncoding = [Text.Encoding]::GetEncoding(850)
            $result = Invoke-FixtureHost simultaneous Simultaneous
        } finally {
            [Console]::OutputEncoding = $previousEncoding
        }
        Assert-Case ($result.stdout_encoding -ceq 'UTF8') 'Unicode stdout did not decode as UTF-8.'
        Assert-Case ($result.stderr_encoding -ceq 'UTF8') 'Unicode stderr did not decode as UTF-8.'
        Assert-Case ($result.stdout_text -match 'stdout-café-64') 'Final stdout line was lost.'
        Assert-Case ($result.stderr_text -match 'stderr-café-64') 'Final stderr line was lost.'
    }
    Invoke-Case 'explicit environment reaches the child' {
        $controlled = [pscustomobject]@{
            inherit = $true
            variables = [pscustomobject]@{ GEOCEDG_FIXTURE_VALUE = 'café-controlled' }
        }
        $result = Invoke-FixtureHost environment Environment -EnvironmentContract $controlled
        Assert-Case ($result.structured_output.value.environment_value -ceq 'café-controlled') 'Controlled environment value changed.'
        Assert-Case ($result.environment.variables.GEOCEDG_FIXTURE_VALUE -ceq 'café-controlled') 'Environment contract was not recorded.'
    }
    Invoke-Case 'argument arrays preserve spaces Unicode and shell metacharacters' {
        $payload = 'one value café ; literal$not-a-command'
        $result = Invoke-FixtureHost arguments Arguments -AdditionalArguments @('-Payload', $payload)
        Assert-Case ($result.structured_output.value.payload -ceq $payload) 'Argument content changed.'
        Assert-Case ($result.working_directory -ceq [IO.Path]::GetFullPath($root)) 'Explicit working directory changed.'
    }
    Invoke-Case 'non-inherited environment excludes ambient values' {
        $ambientName = 'GEOCEDG_FIXTURE_VALUE'
        $previousValue = [Environment]::GetEnvironmentVariable($ambientName, 'Process')
        try {
            [Environment]::SetEnvironmentVariable($ambientName, 'ambient-value', 'Process')
            $controlled = [pscustomobject]@{
                inherit = $false
                variables = [pscustomobject]@{ GEOCEDG_FIXTURE_VALUE = 'isolated-value' }
            }
            $result = Invoke-FixtureHost isolated-environment Environment -EnvironmentContract $controlled
            Assert-Case ($result.structured_output.value.environment_value -ceq 'isolated-value') 'Isolated environment did not use its declared value.'
            Assert-Case (-not $result.environment.inherit) 'Environment inheritance flag changed.'
        } finally {
            [Environment]::SetEnvironmentVariable($ambientName, $previousValue, 'Process')
        }
    }
    Invoke-Case 'declared outputs cannot escape the unique child directory' {
        $outside = Join-Path $root 'outside.evidence'
        Assert-CaseThrows {
            Invoke-FixtureHost unsafe-output Structured -EvidencePaths @{ PAYLOAD = $outside }
        } 'escapes'
        Assert-Case (-not (Test-Path -LiteralPath $outside)) 'Unsafe evidence path was written.'
    }
    Invoke-Case 'generated evidence identity is preserved' {
        $output = Join-Path $root 'evidence'
        $evidencePath = Join-Path $output 'payload.evidence'
        $arguments = @('-EvidencePath', $evidencePath)
        $result = Invoke-FixtureHost evidence Structured -AdditionalArguments $arguments -EvidencePaths @{
            PAYLOAD = $evidencePath
        }
        Assert-Case ($result.generated_evidence.Count -eq 1) 'Generated evidence count changed.'
        Assert-Case ($result.generated_evidence[0].state -ceq 'PRESENT') 'Generated evidence was not found.'
        Assert-Case ($result.generated_evidence[0].sha256 -cmatch '^[0-9a-f]{64}$') 'Generated evidence hash is invalid.'
    }
    Invoke-Case 'standalone host exits zero after preserving nonzero child evidence' {
        $caseRoot = Join-Path $root 'standalone'
        [void][IO.Directory]::CreateDirectory($caseRoot)
        $childOutput = Join-Path $caseRoot 'child'
        $structured = Join-Path $childOutput 'child-result.json'
        $request = [ordered]@{
            check_id = 'standalone'
            node_kind = 'DIAGNOSTIC_LEAF'
            command = $pwshPath
            arguments = @('-NoProfile', '-File', $fixturePath, '-Mode', 'Structured', '-ResultPath', $structured, '-ContractClass', 'STYLE_DIAGNOSTIC', '-Outcome', 'DIAGNOSTIC_FINDING', '-ExitCode', '12')
            working_directory = $root
            environment = $environment
            output_directory = $childOutput
            structured_output_path = $structured
            evidence_paths = [ordered]@{}
        }
        $requestPath = Join-Path $caseRoot 'request.json'
        $resultPath = Join-Path $caseRoot 'result.json'
        [void](Write-VerificationAtomicJson $requestPath $request)
        & $pwshPath -NoProfile -File $hostPath -RequestPath $requestPath -ResultPath $resultPath
        $hostExit = $LASTEXITCODE
        $captured = Read-VerificationJson $resultPath
        Assert-Case ($hostExit -eq 0) 'Standalone host converted diagnostic child exit into host failure.'
        Assert-Case ($captured.exit_code -eq 12) 'Standalone host lost child exit.'
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker -PathType Leaf) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-host') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else {
        throw "Fixture cleanup refused unexpected path: $resolved"
    }
}

Write-Host "verification-check-host.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

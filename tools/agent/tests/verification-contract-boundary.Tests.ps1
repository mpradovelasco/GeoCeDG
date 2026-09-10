#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force
. (Join-Path $PSScriptRoot 'fixtures/verification-environment-fixture.ps1')

$script:Cases = 0
$script:Assertions = 0
$pwsh = (Get-Process -Id $PID).Path
$git = (Get-Command git -CommandType Application -ErrorAction Stop | Select-Object -First 1).Path

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}

function Invoke-Case {
    param([string]$Name, [scriptblock]$Body)
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}

function Invoke-Git {
    param([string]$Root, [string[]]$Arguments)
    $global:LASTEXITCODE = $null
    $output = & $git -C $Root @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Git failed: $($output -join [Environment]::NewLine)" }
    return $output
}

function Write-FixtureText {
    param([string]$Path, [string]$Text)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    [IO.File]::WriteAllText($Path, $Text.Replace("`r`n", "`n"),
        [Text.UTF8Encoding]::new($false))
}

function Invoke-PowerShellCapture {
    param([string]$ScriptPath, [string[]]$Arguments)
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $pwsh
    $start.WorkingDirectory = $repositoryRoot
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @('-NoLogo', '-NoProfile', '-File', $ScriptPath) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw "Unable to start $ScriptPath" }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        return [pscustomobject]@{
            exit_code = $process.ExitCode
            stdout = $stdoutTask.GetAwaiter().GetResult()
            stderr = $stderrTask.GetAwaiter().GetResult()
        }
    } finally { $process.Dispose() }
}

function New-PackagingArtifactFixture {
    param([string]$Root)
    $marker = 'INTERNAL EVALUATION — NOT FOR REDISTRIBUTION'
    $app = Join-Path $Root 'app-image/GeoCeDG'
    $packages = Join-Path $Root 'packages'
    [void][IO.Directory]::CreateDirectory((Join-Path $app 'app'))
    [void][IO.Directory]::CreateDirectory($packages)
    foreach ($path in @(
            (Join-Path $app 'GeoCeDG.exe'),
            (Join-Path $Root 'GeoCeDG-fixture-internal.zip'),
            (Join-Path $packages 'GeoCeDG-fixture-internal.msi'),
            (Join-Path $packages 'GeoCeDG-fixture-internal.exe'))) {
        Write-FixtureText $path 'fixture'
    }
    Write-FixtureText (Join-Path $app 'app/INTERNAL_EVALUATION_ONLY.txt') $marker
    Write-FixtureText (Join-Path $app 'app/GeoCeDG.cfg') `
        'app.mainclass=org.geocedg.desktop.GeoCeDG'
    Write-FixtureText (Join-Path $Root 'geocedg-windows.cdx.json') `
        '{"bomFormat":"CycloneDX","specVersion":"1.5","components":[{"name":"fixture"}]}'
    $manifest = [ordered]@{
        target = 'All'
        distribution_marker = $marker
        public_redistribution = 'BLOCKED PENDING LICENSE/ASSET APPROVAL'
        application = [ordered]@{ icon = [ordered]@{
                path = 'source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-application.ico'
                sha256 = 'e5dac1dd3a556f4ce9747f00d272281e9a571ecc5e757180ba1c6750b664cd73'
        } }
        file_association = [ordered]@{
            enabled_for_target = $true
            registration_scope = 'msi-exe-installers-only'
            extension = 'cedg'
            mime_type = 'application/x-geocedg-cedg'
            mime_basis = 'jdk25-jpackage-required-internal-unregistered'
            progid_strategy = 'jdk25-jpackage-generated-geocedg-owned'
            portable_outputs_association_free = $true
            compatibility_extension_claimed = $false
        }
        runtime = [ordered]@{ excluded_non_windows_native_jars = @('fixture.jar') }
    }
    Write-FixtureText (Join-Path $Root 'build-manifest.json') `
        ((ConvertTo-Json $manifest -Depth 10 -Compress) + "`n")
    Write-FixtureText (Join-Path $Root 'app-image.SHA256SUMS.txt') ""
    $zip = Join-Path $Root 'GeoCeDG-fixture-internal.zip'
    $hash = (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-FixtureText (Join-Path $Root 'SHA256SUMS.txt') `
        ($hash + '  GeoCeDG-fixture-internal.zip' + "`n")
}

function New-FakeWix {
    param([string]$Root, [bool]$ExtensionsAvailable)
    [void][IO.Directory]::CreateDirectory($Root)
    $xml = Join-Path $Root 'fixture.wxs'
    Write-FixtureText $xml @'
<Wix><Fragment><ProgId Id="GeoCeDG.CeDG" Description="GeoCeDG document (internal evaluation)"><Extension Id="cedg" ContentType="application/x-geocedg-cedg"><MIME ContentType="application/x-geocedg-cedg"/><Verb Id="open" Argument="%1" TargetFile="launcher"/></Extension></ProgId><File Id="launcher" Name="GeoCeDG.exe" Source="GeoCeDG.exe"/></Fragment></Wix>
'@
    $extensionLines = if ($ExtensionsAvailable) {
        "echo WixToolset.Util.wixext 5.0.2`necho WixToolset.UI.wixext 5.0.2"
    } else { 'rem extensions deliberately unavailable' }
    $batch = @'
@echo off
if /I "%~1"=="--version" (echo 5.0.2+fixture&amp; exit /b 0)
if /I "%~1"=="extension" (
{0}
exit /b 0
)
if /I "%~1"=="msi" (copy /y "%GEOCEDG_WIX_FIXTURE_XML%" "%~5" &gt;nul&amp; exit /b 0)
exit /b 1
'@
    $batch = ($batch -f $extensionLines).Replace('&amp;', '&').Replace('&gt;', '>')
    Write-FixtureText (Join-Path $Root 'wix.cmd') $batch
    return $xml
}

function New-NodeBase {
    param([string]$Id, [string]$Kind, [string]$Contract)
    $node = [ordered]@{
        check_id = $Id
        node_kind = $Kind
        tier = 'STATIC'
        command = [ordered]@{ kind = 'BUILTIN_PROJECTION'; identity = $Id; projection = 'STRUCTURED_CONTRACT' }
        argv = [string[]]@()
        working_directory = '.'
        dependencies = [string[]]@('repository.producer')
        platforms = [string[]]@('WINDOWS', 'LINUX', 'MACOS')
        impact_paths = [string[]]@('.')
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = [string[]]@()
        side_effects = [string[]]@('NONE')
        output_adapter = 'PROJECTION_STRUCTURED_CONTRACT_V1'
        required_for_profiles = [string[]]@('STATIC')
        producer_dependency = 'repository.producer'
        contract_class = $Contract
    }
    return $node
}

Invoke-Case 'real repository producer separates safety acceptance and style diagnostic' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-contract-boundary-' + [guid]::NewGuid().ToString('N'))
    $repo = Join-Path $root 'repo'
    $output = Join-Path $root 'evidence'
    [void][IO.Directory]::CreateDirectory($repo)
    $marker = Join-Path $root '.fixture-owner'
    [IO.File]::WriteAllText($marker, 'verification-contract-boundary', [Text.UTF8Encoding]::new($false))
    try {
        [void](Invoke-Git $repo @('init', '-q'))
        [void](Invoke-Git $repo @('config', 'user.name', 'Fixture'))
        [void](Invoke-Git $repo @('config', 'user.email', 'fixture@example.invalid'))
        [IO.File]::WriteAllText((Join-Path $repo 'tracked.txt'), "clean`n", [Text.UTF8Encoding]::new($false))
        [void](Invoke-Git $repo @('add', 'tracked.txt'))
        [void](Invoke-Git $repo @('commit', '-q', '-m', 'fixture'))
        [IO.File]::WriteAllText((Join-Path $repo 'tracked.txt'), "style finding   `n", [Text.UTF8Encoding]::new($false))

        $producer = [ordered]@{
            check_id = 'repository.producer'
            node_kind = 'PROCESS_PRODUCER'
            tier = 'STATIC'
            command = [ordered]@{ kind = 'PROCESS'; identity = 'repository-safety-test'; executable = $pwsh }
            argv = @('-NoLogo', '-NoProfile', '-File', (Join-Path $repositoryRoot 'tools/agent/checks/repository-safety.ps1'), '-RepositoryRoot', '{repository_root}', '-SafetyResultPath', '{evidence_REPOSITORY_SAFETY_RESULT}', '-DiagnosticResultPath', '{evidence_REPOSITORY_STYLE_RESULT}')
            working_directory = '.'
            dependencies = [string[]]@()
            platforms = [string[]]@('WINDOWS', 'LINUX', 'MACOS')
            impact_paths = [string[]]@('.')
            environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
            resource_locks = [string[]]@('git-index-read')
            side_effects = [string[]]@('REPOSITORY_READ', 'TASK_OWNED_OUTPUT')
            output_adapter = 'RAW_PROCESS_V1'
            required_for_profiles = [string[]]@('STATIC')
            produces_evidence = [string[]]@('PROCESS_RESULT', 'REPOSITORY_SAFETY_RESULT', 'REPOSITORY_STYLE_RESULT')
        }
        $safety = New-NodeBase repository.boundary-safety EVIDENCE_PROJECTION SAFETY
        $safety.command.evidence_name = 'REPOSITORY_SAFETY_RESULT'
        $style = New-NodeBase repository.style EVIDENCE_PROJECTION STYLE_DIAGNOSTIC
        $style.command.evidence_name = 'REPOSITORY_STYLE_RESULT'
        $registry = [ordered]@{
            schema_version = 1
            registry_id = 'fixture.boundary'
            profiles = @([ordered]@{ profile_id = 'STATIC'; coverage_state = 'COMPLETE'; missing_check_ids = [string[]]@() })
            compatibility_mappings = [object[]]@()
            phase_selections = [object[]]@()
            nodes = @($producer, $safety, $style)
        }
        $head = ([string](Invoke-Git $repo @('rev-parse', 'HEAD'))).Trim()
        $tree = ([string](Invoke-Git $repo @('rev-parse', 'HEAD^{tree}'))).Trim()
        $identity = New-VerificationTestEnvironmentIdentity -BaseCommit $null `
            -BaseTree $null -CandidateCommit $head -CandidateTree $tree `
            -CompatibilitySignature ('9' * 64)
        $run = Invoke-VerificationSupervisor -Registry $registry -Profile STATIC `
            -RepositoryRoot $repo -OutputDirectory $output -Identity $identity
        Assert-Case ($run.report.process_producers[0].exit_code -ne 0) 'Style producer did not exercise nonzero legacy exit.'
        Assert-Case ($run.report.process_producers[0].PSObject.Properties.Name -cnotcontains 'contract_class') 'Producer acquired a verdict contract.'
        Assert-Case ($run.report.acceptance_results[0].status -ceq 'CONTRACT_SATISFIED') 'Repository safety projection failed.'
        Assert-Case ($run.report.diagnostic_findings[0].outcome -ceq 'DIAGNOSTIC_FINDING') 'Style finding was not projected diagnostically.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED' -and $run.report.coverage_verdict -ceq 'COMPLETE') 'Diagnostic finding altered acceptance.'
        Assert-Case ($run.exit_code -eq 0) 'Diagnostic finding altered campaign exit.'
    } finally {
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-contract-boundary') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else {
            throw "Fixture cleanup refused unexpected path: $resolved"
        }
    }
}

Invoke-Case 'WORKSTATION is a single live installation leaf with no unrelated calls' {
    $registry = Read-VerificationJson (Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.json')
    $plan = Resolve-VerificationRegistryPlan $registry WORKSTATION
    Assert-Case (@($plan.nodes).Count -eq 1) 'WORKSTATION contains more than one leaf.'
    Assert-Case ($plan.nodes[0].check_id -ceq 'workstation.live-safety') 'WORKSTATION does not select the live safety checker.'
    $argv = [string[]]$plan.nodes[0].argv
    Assert-Case (@($argv | Where-Object { $_ -match 'fixture|bootstrap|operational|governance|compile|test' }).Count -eq 0) 'WORKSTATION invokes unrelated verification.'
    $scriptText = [IO.File]::ReadAllText((Join-Path $repositoryRoot 'tools/agent/checks/workstation-safety.ps1'))
    Assert-Case (-not $scriptText.Contains('om_env')) 'WORKSTATION mentions the external environment.'
    Assert-Case (-not $scriptText.Contains('--%')) 'WORKSTATION uses stop-parsing syntax.'
    Assert-Case ($scriptText.Contains('org.gradle.java.installations.auto-download=false')) 'WORKSTATION does not disable toolchain download.'
    Assert-Case (-not $scriptText.Contains("'javaToolchains'")) 'WORKSTATION configures the Gradle build to discover toolchains.'
    Assert-Case (-not $scriptText.Contains('Invoke-NativeCapture ''gradle.wrapper''')) 'WORKSTATION executes the wrapper and could download Gradle.'
    Assert-Case ($scriptText.Contains("@('--version')")) 'WORKSTATION Conda probe is not read-only.'
    foreach ($forbiddenCondaArgument in @("'env'", "'create'", "'update'", "'remove'", "'prune'", "'rename'")) {
        Assert-Case (-not $scriptText.Contains($forbiddenCondaArgument)) `
            "WORKSTATION contains forbidden Conda argument $forbiddenCondaArgument."
    }
}

Invoke-Case 'packaging product projection accepts valid artifacts and detects missing or altered evidence' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-packaging-product-' + [guid]::NewGuid().ToString('N'))
    $artifactRoot = Join-Path $root 'artifacts'
    $fakeBin = Join-Path $root 'bin'
    $marker = Join-Path $root '.fixture-owner'
    [void][IO.Directory]::CreateDirectory($root)
    Write-FixtureText $marker 'verification-contract-boundary'
    New-PackagingArtifactFixture $artifactRoot
    $xml = New-FakeWix $fakeBin $true
    $savedPath = $env:PATH
    $savedXml = $env:GEOCEDG_WIX_FIXTURE_XML
    try {
        $env:PATH = $fakeBin + [IO.Path]::PathSeparator + $savedPath
        $env:GEOCEDG_WIX_FIXTURE_XML = $xml
        $checker = Join-Path $repositoryRoot 'tools/agent/checks/packaging-product.ps1'
        $validResult = Join-Path $root 'valid.json'
        $valid = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $validResult, '-RequireArtifacts', '-ArtifactRoot', $artifactRoot)
        $validJson = Read-VerificationJson $validResult
        Assert-Case ($valid.exit_code -eq 0 -and
            $validJson.outcome -ceq 'CONTRACT_SATISFIED') `
            ("Valid packaging evidence was rejected: outcome={0}; cause={1}; stderr={2}; unsatisfied={3}" -f `
                $validJson.outcome, $validJson.cause, $valid.stderr,
                ((@($validJson.subcontracts | Where-Object status -CNE 'SATISFIED') |
                    ConvertTo-Json -Depth 10 -Compress)))
        Assert-Case (@($validJson.subcontracts | Where-Object status -CNE 'SATISFIED').Count -eq 0) `
            'Valid packaging evidence contains an unsatisfied product subcontract.'

        $appImage = Join-Path $artifactRoot 'app-image/GeoCeDG'
        $firstForbidden = Join-Path $appImage 'forbidden.pdf'
        Write-FixtureText $firstForbidden 'forbidden fixture'
        $singleResult = Join-Path $root 'single-forbidden.json'
        $single = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $singleResult, '-RequireArtifacts', '-ArtifactRoot', $artifactRoot)
        $singleJson = Read-VerificationJson $singleResult
        $singleBoundary = @($singleJson.subcontracts | Where-Object {
                $_.contract_id -ceq 'packaging.portable-boundary'
            })
        Assert-Case ($single.exit_code -eq 1 -and $singleBoundary.Count -eq 1 -and
            $singleBoundary[0].status -ceq 'VIOLATED' -and
            @($singleBoundary[0].observed).Count -eq 1 -and
            [IO.Path]::GetFullPath([string]$singleBoundary[0].observed[0]) -ceq
                [IO.Path]::GetFullPath($firstForbidden)) `
            'One forbidden packaging file was not reported once with its full path.'

        $secondForbidden = Join-Path $appImage 'second-forbidden.ggb'
        Write-FixtureText $secondForbidden 'forbidden fixture'
        $multipleResult = Join-Path $root 'multiple-forbidden.json'
        $multiple = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $multipleResult, '-RequireArtifacts', '-ArtifactRoot', $artifactRoot)
        $multipleJson = Read-VerificationJson $multipleResult
        $multipleBoundary = @($multipleJson.subcontracts | Where-Object {
                $_.contract_id -ceq 'packaging.portable-boundary'
            })
        $observedForbidden = @($multipleBoundary[0].observed | ForEach-Object {
                [IO.Path]::GetFullPath([string]$_)
            })
        Assert-Case ($multiple.exit_code -eq 1 -and $multipleBoundary.Count -eq 1 -and
            $multipleBoundary[0].status -ceq 'VIOLATED' -and
            $observedForbidden.Count -eq 2 -and
            @($observedForbidden | Sort-Object -Unique).Count -eq 2 -and
            $observedForbidden -ccontains [IO.Path]::GetFullPath($firstForbidden) -and
            $observedForbidden -ccontains [IO.Path]::GetFullPath($secondForbidden)) `
            'Multiple forbidden packaging files were not each reported exactly once.'
        Remove-Item -LiteralPath $firstForbidden, $secondForbidden -Force

        Write-FixtureText (Join-Path $artifactRoot 'SHA256SUMS.txt') `
            (('0' * 64) + '  GeoCeDG-fixture-internal.zip' + "`n")
        $alteredResult = Join-Path $root 'altered.json'
        $altered = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $alteredResult, '-RequireArtifacts', '-ArtifactRoot', $artifactRoot)
        $alteredJson = Read-VerificationJson $alteredResult
        Assert-Case ($altered.exit_code -eq 1 -and
            @($alteredJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.artifact-hashes' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 1) 'Altered artifact hash was not detected exactly once.'

        $missingRoot = Join-Path $root 'missing'
        [void][IO.Directory]::CreateDirectory($missingRoot)
        $missingResult = Join-Path $root 'missing.json'
        $missing = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $missingResult, '-RequireArtifacts', '-ArtifactRoot', $missingRoot)
        $missingJson = Read-VerificationJson $missingResult
        Assert-Case ($missing.exit_code -eq 1 -and
            @($missingJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.artifacts-present' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 1) 'Missing packaging evidence was not detected exactly once.'
    } finally {
        $env:PATH = $savedPath
        $env:GEOCEDG_WIX_FIXTURE_XML = $savedXml
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-contract-boundary') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else { throw "Fixture cleanup refused unexpected path: $resolved" }
    }
}

Invoke-Case 'packaging toolchain safety distinguishes available and missing requirements' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-packaging-toolchain-' + [guid]::NewGuid().ToString('N'))
    $fakeAvailable = Join-Path $root 'available'
    $fakeMissing = Join-Path $root 'missing'
    $marker = Join-Path $root '.fixture-owner'
    [void][IO.Directory]::CreateDirectory($root)
    Write-FixtureText $marker 'verification-contract-boundary'
    [void](New-FakeWix $fakeAvailable $true)
    [void](New-FakeWix $fakeMissing $false)
    $savedPath = $env:PATH
    $savedRequest = $env:GEOCEDG_PACKAGING_CHECK_TOOLCHAIN
    try {
        $env:GEOCEDG_PACKAGING_CHECK_TOOLCHAIN = 'true'
        $checker = Join-Path $repositoryRoot 'tools/agent/checks/workstation-safety.ps1'
        $env:PATH = $fakeAvailable + [IO.Path]::PathSeparator + $savedPath
        $availablePath = Join-Path $root 'available.json'
        $available = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $availablePath, '-ScratchDirectory', (Join-Path $root 'scratch-a'),
            '-ContractMode', 'PACKAGING')
        $availableJson = Read-VerificationJson $availablePath
        Assert-Case ($available.exit_code -eq 0 -and
            $availableJson.outcome -ceq 'CONTRACT_SATISFIED') `
            "Available packaging toolchain was rejected: $($available.stderr)"

        $env:PATH = $fakeMissing + [IO.Path]::PathSeparator + $savedPath
        $missingPath = Join-Path $root 'missing.json'
        $missing = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repositoryRoot,
            '-ResultPath', $missingPath, '-ScratchDirectory', (Join-Path $root 'scratch-m'),
            '-ContractMode', 'PACKAGING')
        $missingJson = Read-VerificationJson $missingPath
        Assert-Case ($missing.exit_code -eq 2 -and
            @($missingJson.subcontracts | Where-Object {
                    $_.contract_id -like 'packaging.wix-extension.*' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 2) 'Missing packaging toolchain extensions were not isolated as safety violations.'
    } finally {
        $env:PATH = $savedPath
        $env:GEOCEDG_PACKAGING_CHECK_TOOLCHAIN = $savedRequest
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-contract-boundary') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else { throw "Fixture cleanup refused unexpected path: $resolved" }
    }
}

Invoke-Case 'packaging repository safety detects worktree and generated-state changes' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-packaging-repository-' + [guid]::NewGuid().ToString('N'))
    $repo = Join-Path $root 'repo'
    $marker = Join-Path $root '.fixture-owner'
    [void][IO.Directory]::CreateDirectory($repo)
    Write-FixtureText $marker 'verification-contract-boundary'
    try {
        [void](Invoke-Git $repo @('init', '-q'))
        [void](Invoke-Git $repo @('config', 'user.name', 'Fixture'))
        [void](Invoke-Git $repo @('config', 'user.email', 'fixture@example.invalid'))
        Write-FixtureText (Join-Path $repo 'tracked.txt') "clean`n"
        Write-FixtureText (Join-Path $repo '.gitignore') "build/`n.gradle/`n.kotlin/`nforeign/`n"
        Write-FixtureText (Join-Path $repo 'build/tracked-output.txt') "tracked build output`n"
        [void](Invoke-Git $repo @('add', 'tracked.txt', '.gitignore'))
        [void](Invoke-Git $repo @('add', '-f', 'build/tracked-output.txt'))
        [void](Invoke-Git $repo @('commit', '-q', '-m', 'fixture'))
        $baseline = Join-Path $root 'baseline.json'
        $checker = Join-Path $repositoryRoot 'tools/agent/checks/repository-safety.ps1'
        $declaredWriteRoots = [string[]]@('.gradle', 'build', '.kotlin')
        $declaredWriteRootsArgument = $declaredWriteRoots -join ','
        $baselineRun = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $baseline, '-DeclaredWriteRoots', $declaredWriteRootsArgument,
            '-ContractMode', 'PACKAGING_BASELINE')
        $baselineJson = Read-VerificationJson $baseline
        Assert-Case ($baselineRun.exit_code -eq 0 -and
            $baselineJson.evidence_kind -ceq 'PACKAGING_REPOSITORY_BASELINE' -and
            $baselineJson.evidence_state -ceq 'PRESENT' -and
            (ConvertTo-Json @($baselineJson.declared_write_roots) -Compress) -ceq
                (ConvertTo-Json @('.gradle', '.kotlin', 'build') -Compress) -and
            $baselineJson.protected_file_count -ge 2 -and
            $baselineJson.protected_state_sha256 -match '^[0-9a-f]{64}$') `
            ("Packaging baseline producer did not preserve a complete initial state: exit={0}; roots={1}; expected={2}; protected_count={3}; protected_hash={4}; stderr={5}" -f `
                $baselineRun.exit_code,
                (ConvertTo-Json @($baselineJson.declared_write_roots) -Compress),
                (ConvertTo-Json @('.gradle', '.kotlin', 'build') -Compress),
                $baselineJson.protected_file_count, $baselineJson.protected_state_sha256,
                $baselineRun.stderr)

        $cleanPath = Join-Path $root 'clean.json'
        $clean = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $cleanPath, '-BaselinePath', $baseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument,
            '-ContractMode', 'PACKAGING')
        $cleanJson = Read-VerificationJson $cleanPath
        Assert-Case ($clean.exit_code -eq 0 -and
            $cleanJson.outcome -ceq 'CONTRACT_SATISFIED') `
            ("Unchanged packaging repository state was rejected: exit={0}; cause={1}; subcontracts={2}; stderr={3}" -f `
                $clean.exit_code, $cleanJson.cause,
                ((@($cleanJson.subcontracts) | ConvertTo-Json -Depth 10 -Compress)),
                $clean.stderr)

        $missingBaselinePath = Join-Path $root 'missing-baseline-result.json'
        $missingBaseline = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $missingBaselinePath,
            '-BaselinePath', (Join-Path $root 'absent.json'),
            '-DeclaredWriteRoots', $declaredWriteRootsArgument, '-ContractMode', 'PACKAGING')
        $missingBaselineJson = Read-VerificationJson $missingBaselinePath
        Assert-Case ($missingBaseline.exit_code -eq 3 -and
            $missingBaselineJson.outcome -ceq 'EVIDENCE_UNTRUSTED' -and
            @($missingBaselineJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.repository-baseline' -and
                    $_.status -ceq 'EVIDENCE_UNTRUSTED'
                }).Count -eq 1) 'Missing packaging baseline was not rejected as untrusted evidence.'

        Write-FixtureText (Join-Path $repo 'tracked.txt') "changed`n"
        [void][IO.Directory]::CreateDirectory((Join-Path $repo 'build'))
        Write-FixtureText (Join-Path $repo 'build/generated.txt') 'generated'
        $changedPath = Join-Path $root 'changed.json'
        $changed = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $changedPath, '-BaselinePath', $baseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument,
            '-ContractMode', 'PACKAGING')
        $changedJson = Read-VerificationJson $changedPath
        Assert-Case ($changed.exit_code -eq 2 -and
            @($changedJson.subcontracts | Where-Object status -CEQ 'VIOLATED').Count -eq 2) `
            'Packaging worktree and generated-state changes were not both detected.'

        Write-FixtureText (Join-Path $repo 'tracked.txt') "clean`n"
        $partialPath = Join-Path $root 'partial-restoration.json'
        $partial = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $partialPath, '-BaselinePath', $baseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument,
            '-ContractMode', 'PACKAGING')
        $partialJson = Read-VerificationJson $partialPath
        $partialViolations = @($partialJson.subcontracts | Where-Object status -CEQ 'VIOLATED')
        Assert-Case ($partial.exit_code -eq 0 -and $partialViolations.Count -eq 0 -and
            @($partialJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.declared-write-scope' -and
                    $_.status -ceq 'SATISFIED'
                }).Count -eq 1) `
            'Declared Gradle output was incorrectly treated as a protected mutation.'

        $trackedOutput = Join-Path $repo 'build/tracked-output.txt'
        Write-FixtureText $trackedOutput "tracked build output changed`n"
        $trackedProtectedPath = Join-Path $root 'tracked-protected.json'
        $trackedProtected = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $trackedProtectedPath, '-BaselinePath', $baseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument, '-ContractMode', 'PACKAGING')
        $trackedProtectedJson = Read-VerificationJson $trackedProtectedPath
        Assert-Case ($trackedProtected.exit_code -eq 2 -and
            @($trackedProtectedJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.generated-state-preserved' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 1 -and
            @($trackedProtectedJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.worktree-preserved' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 1) `
            'A tracked file inside a declared Gradle root was not protected.'
        Write-FixtureText $trackedOutput "tracked build output`n"

        $preexistingOutput = Join-Path $repo 'build/preexisting.txt'
        Write-FixtureText $preexistingOutput 'before'
        $preexistingBaseline = Join-Path $root 'preexisting-baseline.json'
        $preexistingBaselineRun = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $preexistingBaseline, '-DeclaredWriteRoots', $declaredWriteRootsArgument,
            '-ContractMode', 'PACKAGING_BASELINE')
        Write-FixtureText $preexistingOutput 'after'
        $preexistingResultPath = Join-Path $root 'preexisting-output.json'
        $preexistingResult = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $preexistingResultPath, '-BaselinePath', $preexistingBaseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument, '-ContractMode', 'PACKAGING')
        $preexistingJson = Read-VerificationJson $preexistingResultPath
        Assert-Case ($preexistingBaselineRun.exit_code -eq 0 -and
            $preexistingResult.exit_code -eq 0 -and
            $preexistingJson.outcome -ceq 'CONTRACT_SATISFIED') `
            'A pre-existing declared Gradle output was incorrectly attributed to packaging.'

        $foreignFile = Join-Path $repo 'foreign/not-a-gradle-output.txt'
        Write-FixtureText $foreignFile 'unauthorized ignored output'
        $foreignPath = Join-Path $root 'foreign-output.json'
        $foreign = Invoke-PowerShellCapture $checker @('-RepositoryRoot', $repo,
            '-SafetyResultPath', $foreignPath, '-BaselinePath', $preexistingBaseline,
            '-DeclaredWriteRoots', $declaredWriteRootsArgument, '-ContractMode', 'PACKAGING')
        $foreignJson = Read-VerificationJson $foreignPath
        Assert-Case ($foreign.exit_code -eq 2 -and
            @($foreignJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.generated-state-preserved' -and
                    $_.status -ceq 'VIOLATED'
                }).Count -eq 1 -and
            @($foreignJson.subcontracts | Where-Object {
                    $_.contract_id -ceq 'packaging.worktree-preserved' -and
                    $_.status -ceq 'SATISFIED'
                }).Count -eq 1) `
            'An ignored write outside declared Gradle roots was not rejected as a safety violation.'
    } finally {
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-contract-boundary') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else { throw "Fixture cleanup refused unexpected path: $resolved" }
    }
}

Write-Host "verification-contract-boundary.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

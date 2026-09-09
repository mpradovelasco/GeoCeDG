#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$registryPath = Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.json'
$schemaPath = Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.schema.json'
$registry = Read-VerificationJson $registryPath
[void](Assert-VerificationRegistry $registry $schemaPath)
$pwsh = (Get-Process -Id $PID).Path

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
    } finally {
        $process.Dispose()
    }
}

Invoke-Case 'all canonical profiles resolve explicitly' {
    foreach ($profile in @('STATIC', 'INFRA_UNIT', 'WORKSTATION', 'OPERATIONAL', 'DEV', 'INTEGRATION', 'FINAL')) {
        $plan = Resolve-VerificationRegistryPlan $registry $profile
        Assert-Case ($plan.profile -ceq $profile) "Profile $profile resolved incorrectly."
    }
    $phase = Resolve-VerificationRegistryPlan $registry PHASE G9A2
    Assert-Case ($phase.profile -ceq 'PHASE' -and $phase.selection -ceq 'G9A2') 'PHASE selection did not resolve exactly.'
}

Invoke-Case 'legacy aliases map only to canonical profiles' {
    $expected = [ordered]@{
        DEV = 'DEV'
        PHASE = 'PHASE'
        COMPOSED = 'INTEGRATION'
        FULL = 'FINAL'
        FullTests = 'FINAL'
    }
    foreach ($selector in $expected.Keys) {
        $selection = if ($selector -ceq 'PHASE') { 'G7A' } else { $null }
        $plan = Resolve-VerificationRegistryPlan $registry $selector $selection
        Assert-Case ($plan.profile -ceq $expected[$selector]) "$selector mapped incorrectly."
    }
}

Invoke-Case 'canonical and public WORKSTATION commands resolve byte-identical plans' {
    $canonical = Invoke-PowerShellCapture (Join-Path $repositoryRoot 'tools/agent/verify.ps1') `
        @('-Profile', 'WORKSTATION', '-PlanOnly', '-Quiet')
    $adapter = Invoke-PowerShellCapture (Join-Path $repositoryRoot 'tools/agent/verify-workstation.ps1') `
        @('-PlanOnly', '-Quiet')
    Assert-Case ($canonical.exit_code -eq 0 -and $adapter.exit_code -eq 0) 'WORKSTATION plan command failed.'
    Assert-Case ([string]::IsNullOrWhiteSpace($canonical.stderr) -and [string]::IsNullOrWhiteSpace($adapter.stderr)) 'WORKSTATION plan emitted stderr.'
    Assert-Case ($canonical.stdout -ceq $adapter.stdout) 'WORKSTATION adapter changed the resolved plan.'
    $plan = $canonical.stdout | ConvertFrom-Json -Depth 100
    Assert-Case ($plan.coverage_state -ceq 'COMPLETE') 'WORKSTATION is not complete.'
    Assert-Case (@($plan.nodes).Count -eq 1 -and $plan.nodes[0].check_id -ceq 'workstation.live-safety') 'WORKSTATION contains non-installation checks.'
}

Invoke-Case 'public compatibility adapters are thin canonical forwarders' {
    $adapters = [ordered]@{
        'verify-workstation.ps1' = 'WORKSTATION'
        'verify-operational.ps1' = 'OPERATIONAL'
        'verify-verification-infrastructure.ps1' = 'INFRA_UNIT'
    }
    foreach ($name in $adapters.Keys) {
        $text = [IO.File]::ReadAllText((Join-Path $repositoryRoot ('tools/agent/' + $name)))
        Assert-Case ($text.Contains("Profile = '$($adapters[$name])'")) "$name does not select its canonical profile."
        Assert-Case ($text.Contains("Join-Path `$PSScriptRoot 'verify.ps1'")) "$name does not forward to verify.ps1."
        Assert-Case (-not $text.Contains('Invoke-VerificationSupervisor')) "$name duplicates supervisor logic."
    }
}

Invoke-Case 'incomplete profiles fail safely without executing mixed legacy wrappers' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $owner = Join-Path $tempBase ('geocedg-incomplete-profile-' + [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($owner)
    $marker = Join-Path $owner '.fixture-owner'
    [IO.File]::WriteAllText($marker, 'verification-cli-compatibility', [Text.UTF8Encoding]::new($false))
    $output = Join-Path $owner 'evidence'
    $result = Invoke-PowerShellCapture (Join-Path $repositoryRoot 'tools/agent/verify-operational.ps1') `
        @('-Quiet', '-LogDirectory', $output)
    try {
        Assert-Case ($result.exit_code -eq 3) 'Incomplete OPERATIONAL profile did not fail safely.'
        $report = Read-VerificationJson (Join-Path $output 'verification-result.json')
        Assert-Case ($report.acceptance_verdict -ceq 'REJECTED_VERIFICATION_CORE') 'Incomplete profile claimed acceptance.'
        Assert-Case ($report.coverage_verdict -ceq 'INCOMPLETE') 'Incomplete profile hid its coverage gap.'
        Assert-Case (@($report.process_producers).Count -eq 0) 'Incomplete profile executed a legacy wrapper.'
    } finally {
        $resolved = [IO.Path]::GetFullPath($owner)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-cli-compatibility') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else {
            throw "Fixture cleanup refused unexpected path: $resolved"
        }
    }
}

Invoke-Case 'FINAL resolves complete without a mixed legacy leaf' {
    $plan = Resolve-VerificationRegistryPlan $registry FINAL
    Assert-Case ($plan.coverage_state -ceq 'COMPLETE') 'FINAL semantic coverage is incomplete.'
    Assert-Case (@($plan.missing_check_ids).Count -eq 0) 'FINAL reports missing required contracts.'
    Assert-Case (@($plan.nodes | Where-Object {
        $_.command.identity -match 'legacy|verify-operational|verify-verification-infrastructure'
    }).Count -eq 0) 'A legacy mixed wrapper was registered in FINAL.'
}

Write-Host "verification-cli-compatibility.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2
[CmdletBinding()]
param(
    [ValidateSet("STATIC", "INFRA_UNIT", "WORKSTATION", "OPERATIONAL", "DEV", "PHASE", "INTEGRATION", "FINAL")]
    [string]$Profile,
    [ValidateSet("DEV", "PHASE", "COMPOSED", "FULL")]
    [string]$Level = "COMPOSED",
    [ValidateSet("shared", "desktop")] [string]$Module,
    [string[]]$TestFilter = @(),
    [string]$Phase,
    [switch]$IndependentBuilds,
    [switch]$IncrementalBuild,
    [switch]$CleanBuild,
    [switch]$FullTests,
    [switch]$LaunchDesktop,
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$RunBenchmarks,
    [switch]$VerifyPackagingArtifacts,
    [switch]$PlanOnly,
    [switch]$Quiet,
    [string]$CloseoutReadinessPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
        "geocedg-verify-" + [guid]::NewGuid().ToString("N"))),
    [string]$BenchmarkOutputPath,
    [string]$PackagingArtifactRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

$repositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$registryPath = Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.json'
$registrySchemaPath = Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.schema.json'
$resultSchemaPath = Join-Path $repositoryRoot 'geocedg/specs/operations/verification-result.schema.json'
Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot 'verification-registry.psm1') -Force
Import-Module (Join-Path $PSScriptRoot 'verification-supervisor.psm1') -Force

function Get-CanonicalSelection {
    if (-not [string]::IsNullOrWhiteSpace($Profile)) {
        if ($PSBoundParameters.ContainsKey('Level') -or $FullTests) {
            throw 'Profile cannot be combined with legacy Level or FullTests selection.'
        }
        return $Profile
    }
    if ($FullTests) {
        if ($PSBoundParameters.ContainsKey('Level') -and $Level -cne 'FULL') {
            throw 'FullTests selects FINAL and cannot be combined with another Level.'
        }
        return 'FullTests'
    }
    return $Level
}

function Get-GitObject {
    param([string]$Expression)
    $git = Get-Command git -CommandType Application -ErrorAction Stop |
        Select-Object -First 1
    $global:LASTEXITCODE = $null
    $value = (& $git.Path -C $repositoryRoot rev-parse $Expression).Trim()
    if ($LASTEXITCODE -ne 0 -or $value -cnotmatch '^[0-9a-f]{40}$') {
        throw "Unable to resolve Git identity: $Expression"
    }
    return $value
}

try {
    $requestedProfile = Get-CanonicalSelection
    $selection = if ($requestedProfile -ceq 'PHASE') { $Phase } else { $null }
    if ($requestedProfile -ceq 'PHASE' -and [string]::IsNullOrWhiteSpace($selection)) {
        throw 'PHASE requires -Phase with an exact registered phase identifier.'
    }
    if ($requestedProfile -cne 'PHASE' -and $PSBoundParameters.ContainsKey('Phase')) {
        throw 'Phase is valid only for PHASE.'
    }

    $registry = Read-VerificationJson -Path $registryPath
    [void](Assert-VerificationRegistry -Registry $registry -SchemaPath $registrySchemaPath)
    $plan = Resolve-VerificationRegistryPlan -Registry $registry -Profile $requestedProfile -Selection $selection
    if ($PlanOnly) {
        ConvertTo-VerificationCanonicalJson -Value $plan | Write-Output
        exit 0
    }

    $identity = [pscustomobject]@{
        base_commit = $null
        base_tree = $null
        candidate_commit = Get-GitObject 'HEAD'
        candidate_tree = Get-GitObject 'HEAD^{tree}'
        environment_fingerprint = Get-VerificationDeterministicHash -Value ([ordered]@{
            platform = [Environment]::OSVersion.Platform.ToString()
            os_version = [Environment]::OSVersion.VersionString
            process_architecture = [Runtime.InteropServices.RuntimeInformation]::ProcessArchitecture.ToString()
            powershell = $PSVersionTable.PSVersion.ToString()
            conda_default_env = [Environment]::GetEnvironmentVariable('CONDA_DEFAULT_ENV')
            conda_prefix = [Environment]::GetEnvironmentVariable('CONDA_PREFIX')
            java_home = [Environment]::GetEnvironmentVariable('JAVA_HOME')
        })
    }
    $runParameters = @{
        Registry = $registry
        Profile = $requestedProfile
        Selection = $selection
        RepositoryRoot = $repositoryRoot
        OutputDirectory = $LogDirectory
        Identity = $identity
        RegistrySchemaPath = $registrySchemaPath
        ResultSchemaPath = $resultSchemaPath
    }
    $run = Invoke-VerificationSupervisor @runParameters
    if (-not $Quiet) {
        Write-Host "Resolved profile: $($run.plan.profile)"
        if ($null -ne $run.plan.selection) {
            Write-Host "Selection: $($run.plan.selection)"
        }
        Write-Host "Acceptance verdict: $($run.report.acceptance_verdict)"
        Write-Host "Coverage verdict: $($run.report.coverage_verdict)"
        Write-Host "Diagnostic findings: $($run.report.diagnostic_summary.findings)"
        Write-Host "Result: $($run.report_path)"
    }
    exit ([int]$run.exit_code)
} catch {
    Write-Error -Message $_.Exception.Message -ErrorAction Continue
    exit 3
}

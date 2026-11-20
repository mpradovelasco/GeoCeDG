#requires -Version 7.2
[CmdletBinding()]
param(
    [ValidateSet("STATIC", "INFRA_UNIT", "WORKSTATION", "PACKAGING", "OPERATIONAL", "DEV", "PHASE", "INTEGRATION", "FINAL")]
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
    [switch]$CheckToolchain,
    [switch]$PlanOnly,
    [switch]$Quiet,
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
Import-Module (Join-Path $PSScriptRoot 'verification-environment.psm1') -Force
Import-Module (Join-Path $PSScriptRoot 'verification-receipt.psm1') -Force
. (Join-Path $PSScriptRoot 'repository-input-identity.ps1')

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
    if ($requestedProfile -cne 'PACKAGING' -and
            ($CheckToolchain -or $VerifyPackagingArtifacts -or
             $PSBoundParameters.ContainsKey('PackagingArtifactRoot'))) {
        throw 'Packaging options are valid only for the PACKAGING profile.'
    }

    $registry = Read-VerificationJson -Path $registryPath
    [void](Assert-VerificationRegistry -Registry $registry -SchemaPath $registrySchemaPath)
    $plan = Resolve-VerificationRegistryPlan -Registry $registry -Profile $requestedProfile -Selection $selection
    if ($PlanOnly) {
        ConvertTo-VerificationCanonicalJson -Value $plan | Write-Output
        exit 0
    }

    $environmentContract = Get-VerificationEnvironmentContract
    $environmentObservation = Get-VerificationEnvironmentObservation `
        -RepositoryRoot $repositoryRoot
    $identity = [pscustomobject]@{
        base_commit = $null
        base_tree = $null
        candidate_commit = Get-GitObject 'HEAD'
        candidate_tree = Get-GitObject 'HEAD^{tree}'
        environment_contract = $environmentContract
        environment_compatibility_signature = `
            Get-VerificationEnvironmentCompatibilitySignature $environmentContract
        environment_observation = $environmentObservation
        environment_observation_hash = `
            Get-VerificationEnvironmentObservationHash $environmentObservation
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
    # The packaging repository baseline is produced once, by the registered
    # packaging.repository-baseline.producer node; the runner computes no
    # second, unconsumed snapshot.
    $packagingEnvironment = @{}
    if ($requestedProfile -ceq 'PACKAGING') {
        foreach ($name in @('GEOCEDG_PACKAGING_CHECK_TOOLCHAIN',
                'GEOCEDG_PACKAGING_REQUIRE_ARTIFACTS',
                'GEOCEDG_PACKAGING_ARTIFACT_ROOT')) {
            $packagingEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
        }
        [Environment]::SetEnvironmentVariable('GEOCEDG_PACKAGING_CHECK_TOOLCHAIN',
            $(if ($CheckToolchain -or $VerifyPackagingArtifacts) { 'true' } else { 'false' }))
        [Environment]::SetEnvironmentVariable('GEOCEDG_PACKAGING_REQUIRE_ARTIFACTS',
            $(if ($VerifyPackagingArtifacts) { 'true' } else { 'false' }))
        [Environment]::SetEnvironmentVariable('GEOCEDG_PACKAGING_ARTIFACT_ROOT',
            [string]$PackagingArtifactRoot)
    }
    try {
        $run = Invoke-VerificationSupervisor @runParameters
    } finally {
        if ($requestedProfile -ceq 'PACKAGING') {
            foreach ($name in $packagingEnvironment.Keys) {
                [Environment]::SetEnvironmentVariable($name, $packagingEnvironment[$name])
            }
        }
    }
    # PRE-G9B-R1: canonical acceptance receipt. Emission is gated on the
    # resolved FINAL profile; every other eligibility rule stays owned by the
    # existing receipt factory, which fails closed. No acceptance logic is
    # duplicated here and no receipt is written for any other path.
    $receiptPath = $null
    if ([string]$run.report.profile -ceq 'FINAL' -and
            [string]$run.report.run_state -ceq 'COMPLETED' -and
            [string]$run.report.acceptance_verdict -ceq 'ACCEPTED' -and
            [string]$run.report.coverage_verdict -ceq 'COMPLETE') {
        $candidateCommit = [string]$run.report.candidate_commit
        $checkerIdentity = Get-GeoCeDGVerificationAuthorityIdentity `
            -RepositoryRoot $repositoryRoot -Commit $candidateCommit
        $inputIdentity = Get-GeoCeDGRepositoryTrackedIdentity `
            $repositoryRoot $candidateCommit
        # Only the profile the accepted report actually established is claimed.
        $receipt = New-VerificationAcceptanceReceipt -Report $run.report `
            -CheckerIdentityHash ([string]$checkerIdentity.sha256) `
            -InputIdentityHash ([string]$inputIdentity.sha256) `
            -AcceptedProfiles @([string]$run.report.profile)
        $receiptPath = Write-VerificationAcceptanceReceipt `
            -Path (Join-Path $LogDirectory 'verification-receipt.json') `
            -Receipt $receipt
    }
    if (-not $Quiet) {
        Write-Host "Resolved profile: $($run.plan.profile)"
        if ($null -ne $run.plan.selection) {
            Write-Host "Selection: $($run.plan.selection)"
        }
        Write-Host "Acceptance verdict: $($run.report.acceptance_verdict)"
        Write-Host "Coverage verdict: $($run.report.coverage_verdict)"
        Write-Host "Diagnostic findings: $($run.report.diagnostic_summary.findings)"
        Write-Host "Result: $($run.report_path)"
        if ($null -ne $receiptPath) { Write-Host "Receipt: $receiptPath" }
    }
    exit ([int]$run.exit_code)
} catch {
    Write-Error -Message $_.Exception.Message -ErrorAction Continue
    exit 3
}

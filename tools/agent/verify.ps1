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

function Get-PackagingGeneratedState {
    $records = [Collections.Generic.List[object]]::new()
    foreach ($name in @('build', '.gradle', '.kotlin')) {
        $container = Join-Path $repositoryRoot $name
        if (-not (Test-Path -LiteralPath $container -PathType Container)) { continue }
        foreach ($file in Get-ChildItem -LiteralPath $container -File -Recurse -Force |
                Sort-Object FullName) {
            $records.Add([ordered]@{
                path = [IO.Path]::GetRelativePath($repositoryRoot, $file.FullName).Replace('\', '/')
                bytes = [long]$file.Length
                sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            })
        }
    }
    return [object[]]$records.ToArray()
}

function New-PackagingRepositoryBaseline {
    $git = Get-Command git -CommandType Application -ErrorAction Stop |
        Select-Object -First 1
    $status = (& $git.Path -C $repositoryRoot status --porcelain=v1 --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw 'Unable to capture packaging repository status.' }
    $generated = Get-PackagingGeneratedState
    $generatedJson = ConvertTo-VerificationCanonicalJson -Value $generated
    $generatedHash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes($generatedJson))).ToLowerInvariant()
    $path = Join-Path ([IO.Path]::GetTempPath()) (
        'geocedg-packaging-baseline-' + [guid]::NewGuid().ToString('N') + '.json')
    [void](Write-VerificationAtomicJson -Path $path -Value ([ordered]@{
        status = $status
        generated_state_sha256 = $generatedHash
    }))
    return $path
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
    $packagingBaseline = $null
    $packagingEnvironment = @{}
    if ($requestedProfile -ceq 'PACKAGING') {
        $packagingBaseline = New-PackagingRepositoryBaseline
        foreach ($name in @('GEOCEDG_PACKAGING_BASELINE_PATH',
                'GEOCEDG_PACKAGING_CHECK_TOOLCHAIN',
                'GEOCEDG_PACKAGING_REQUIRE_ARTIFACTS',
                'GEOCEDG_PACKAGING_ARTIFACT_ROOT')) {
            $packagingEnvironment[$name] = [Environment]::GetEnvironmentVariable($name)
        }
        [Environment]::SetEnvironmentVariable('GEOCEDG_PACKAGING_BASELINE_PATH',
            $packagingBaseline)
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
            if (-not [string]::IsNullOrWhiteSpace($packagingBaseline) -and
                    (Test-Path -LiteralPath $packagingBaseline -PathType Leaf)) {
                Remove-Item -LiteralPath $packagingBaseline -Force
            }
        }
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
    }
    exit ([int]$run.exit_code)
} catch {
    Write-Error -Message $_.Exception.Message -ErrorAction Continue
    exit 3
}

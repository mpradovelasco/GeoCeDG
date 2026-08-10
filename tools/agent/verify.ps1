[CmdletBinding()]
param(
    [switch]$FullTests,
    [switch]$LaunchDesktop,
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$RunBenchmarks,
    [switch]$VerifyPackagingArtifacts,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify"),
    [string]$BenchmarkOutputPath,
    [string]$PackagingArtifactRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$OperationalVerifier = Join-Path $PSScriptRoot "verify-operational.ps1"
$BaselineVerifier = Join-Path $PSScriptRoot "verify-baseline.ps1"
$FrontendVerifier = Join-Path $PSScriptRoot "verify-frontend.ps1"
$LegacyVerifier = Join-Path $PSScriptRoot "verify-legacy.ps1"
$PackagingVerifier = Join-Path $PSScriptRoot "verify-packaging.ps1"
$BenchmarkRunner = Join-Path $RepositoryRoot "tools\benchmark\run.ps1"
$InitialStatus = $null

function Assert-LastScriptSuccess {
    param([Parameter(Mandatory)] [string]$Description)

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

try {
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read initial repository status."
    }

    Write-Host "`n==> GeoCeDG operational contracts"
    & $OperationalVerifier
    Assert-LastScriptSuccess -Description "GeoCeDG operational contracts"

    Write-Host "`n==> Controlled legacy CeDG integration"
    & $LegacyVerifier
    Assert-LastScriptSuccess -Description "Controlled legacy CeDG integration"

    Write-Host "`n==> Standalone Windows packaging contracts"
    $packagingParameters = @{}
    if ($VerifyPackagingArtifacts) {
        $packagingParameters.CheckToolchain = $true
        $packagingParameters.RequireArtifacts = $true
        if (-not [string]::IsNullOrWhiteSpace($PackagingArtifactRoot)) {
            $packagingParameters.ArtifactRoot = [IO.Path]::GetFullPath(
                $PackagingArtifactRoot)
        }
    }
    & $PackagingVerifier @packagingParameters
    Assert-LastScriptSuccess -Description "Standalone Windows packaging"

    $baselineParameters = @{
        LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
    }
    if ($FullTests) {
        $baselineParameters.FullTests = $true
    }
    if ($LaunchDesktop) {
        $baselineParameters.LaunchDesktop = $true
    }
    if ($SkipBuild) {
        $baselineParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $baselineParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $baselineParameters.KeepBuildOutputs = $true
    }
    Write-Host "`n==> Pinned GeoGebra baseline"
    & $BaselineVerifier @baselineParameters
    Assert-LastScriptSuccess -Description "Pinned GeoGebra baseline"

    $frontendParameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "frontend"
    }
    if ($SkipBuild) {
        $frontendParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $frontendParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $frontendParameters.KeepBuildOutputs = $true
    }
    Write-Host "`n==> GeoCeDG frontend profile"
    & $FrontendVerifier @frontendParameters
    Assert-LastScriptSuccess -Description "GeoCeDG frontend profile"

    if ($RunBenchmarks) {
        if ([string]::IsNullOrWhiteSpace($BenchmarkOutputPath)) {
            $BenchmarkOutputPath = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "operational-benchmark.json"
        }
        $benchmarkParameters = @{
            OutputPath = [IO.Path]::GetFullPath($BenchmarkOutputPath)
        }
        Write-Host "`n==> Informational operational benchmark"
        & $BenchmarkRunner @benchmarkParameters
        Assert-LastScriptSuccess -Description "Informational operational benchmark"
    }

    & git -C $RepositoryRoot diff --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --check failed with exit code $LASTEXITCODE."
    }
    & git -C $RepositoryRoot diff --cached --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --cached --check failed with exit code $LASTEXITCODE."
    }

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read final repository status."
    }
    if ($finalStatus -ne $InitialStatus) {
        throw "Repository status changed during verification.`nBefore:`n$InitialStatus`nAfter:`n$finalStatus"
    }

    Write-Host "`nAll GeoCeDG verification gates passed."
    Write-Host "Logs: $([IO.Path]::GetFullPath($LogDirectory))"
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

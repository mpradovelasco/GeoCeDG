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
$WorkstationVerifier = Join-Path $PSScriptRoot "verify-workstation.ps1"
$BaselineVerifier = Join-Path $PSScriptRoot "verify-baseline.ps1"
$FrontendVerifier = Join-Path $PSScriptRoot "verify-frontend.ps1"
$LegacyVerifier = Join-Path $PSScriptRoot "verify-legacy.ps1"
$PackagingVerifier = Join-Path $PSScriptRoot "verify-packaging.ps1"
$DxfVerifier = Join-Path $PSScriptRoot "verify-dxf.ps1"
$LocusV2Verifier = Join-Path $PSScriptRoot "verify-locus-v2.ps1"
$G7AMetricVerifier = Join-Path $PSScriptRoot "verify-g7a-metrics.ps1"
$G7BMetricVerifier = Join-Path $PSScriptRoot "verify-g7b-metrics.ps1"
$G8AIntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8a-intersections.ps1"
$G8BIntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8b-intersections.ps1"
$G8CIntersectionDesignVerifier = Join-Path $PSScriptRoot `
    "verify-g8c-intersections-design.ps1"
$G8C1IntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8c1-intersections.ps1"
$BenchmarkRunner = Join-Path $RepositoryRoot "tools\benchmark\run.ps1"
$InitialStatus = $null

. (Join-Path $PSScriptRoot "repository-state.ps1")

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

    $repositoryState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $RepositoryRoot
    Write-Host "Repository state"
    Write-Host "  Branch: $($repositoryState.Branch)"
    Write-Host "  Commit: $($repositoryState.Commit)"
    Write-Host "  Latest included phase: $($repositoryState.LatestIncludedPhase)"

    Write-Host "`n==> GeoCeDG operational contracts"
    & $OperationalVerifier
    Assert-LastScriptSuccess -Description "GeoCeDG operational contracts"

    Write-Host "`n==> Windows workstation operational contracts"
    & $WorkstationVerifier
    Assert-LastScriptSuccess -Description "Windows workstation operational contracts"

    Write-Host "`n==> Controlled legacy CeDG integration"
    & $LegacyVerifier
    Assert-LastScriptSuccess -Description "Controlled legacy CeDG integration"

    Write-Host "`n==> Native 2D geometry and DXF export"
    $dxfParameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "dxf"
    }
    if ($SkipBuild) {
        $dxfParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $dxfParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $dxfParameters.KeepBuildOutputs = $true
    }
    & $DxfVerifier @dxfParameters
    Assert-LastScriptSuccess -Description "Native 2D geometry and DXF export"

    Write-Host "`n==> G6 Locus V2 characterization and experimental kernel"
    $locusV2Parameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "locus-v2"
    }
    if ($SkipBuild) {
        $locusV2Parameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $locusV2Parameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $locusV2Parameters.KeepBuildOutputs = $true
    }
    & $LocusV2Verifier @locusV2Parameters
    Assert-LastScriptSuccess -Description "G6 Locus V2"

    if ($repositoryState.LatestIncludedPhaseNumber -ge 7) {
        Write-Host "`n==> G7A Locus V2 metric characterization"
        $g7aMetricParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g7a-metrics"
        }
        if ($SkipBuild) {
            $g7aMetricParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g7aMetricParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g7aMetricParameters.KeepBuildOutputs = $true
        }
        & $G7AMetricVerifier @g7aMetricParameters
        Assert-LastScriptSuccess `
            -Description "G7A Locus V2 metric characterization"

        Write-Host "`n==> G7B native Locus V2 metric kernel"
        $g7bMetricParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g7b-metrics"
        }
        if ($SkipBuild) {
            $g7bMetricParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g7bMetricParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g7bMetricParameters.KeepBuildOutputs = $true
        }
        & $G7BMetricVerifier @g7bMetricParameters
        Assert-LastScriptSuccess -Description "G7B native Locus V2 metric kernel"
    }

    if ($repositoryState.LatestIncludedPhaseNumber -ge 8) {
        Write-Host "`n==> G8A Locus V2 intersection characterization and closeout"
        $g8aIntersectionParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8a-intersections"
            RequireFinalEvidence = $true
        }
        if ($SkipBuild) {
            $g8aIntersectionParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8aIntersectionParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8aIntersectionParameters.KeepBuildOutputs = $true
        }
        & $G8AIntersectionVerifier @g8aIntersectionParameters
        Assert-LastScriptSuccess `
            -Description "G8A Locus V2 intersection characterization and closeout"

        Write-Host "`n==> G8B native Locus V2 intersection kernel"
        $g8bIntersectionParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8b-intersections"
        }
        if ($SkipBuild) {
            $g8bIntersectionParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8bIntersectionParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8bIntersectionParameters.KeepBuildOutputs = $true
        }
        & $G8BIntersectionVerifier @g8bIntersectionParameters
        Assert-LastScriptSuccess `
            -Description "G8B native Locus V2 intersection kernel"

        Write-Host "`n==> G8C extended Locus V2 intersection design"
        $g8cDesignParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8c-intersections-design"
        }
        if ($SkipBuild) {
            $g8cDesignParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8cDesignParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8cDesignParameters.KeepBuildOutputs = $true
        }
        & $G8CIntersectionDesignVerifier @g8cDesignParameters
        Assert-LastScriptSuccess `
            -Description "G8C extended Locus V2 intersection design"

        $g8c1Evidence = Join-Path $RepositoryRoot `
            "geocedg\validation\locus-v2\g8c1\g8c1-intersection-kernel-evidence.json"
        if (Test-Path -LiteralPath $g8c1Evidence -PathType Leaf) {
            Write-Host "`n==> G8C1 extended one-parameter intersection kernel"
            $g8c1IntersectionParameters = @{
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g8c1-intersections"
            }
            if ($SkipBuild) {
                $g8c1IntersectionParameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g8c1IntersectionParameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g8c1IntersectionParameters.KeepBuildOutputs = $true
            }
            & $G8C1IntersectionVerifier @g8c1IntersectionParameters
            Assert-LastScriptSuccess `
                -Description "G8C1 extended one-parameter intersection kernel"
        }
    }

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

[CmdletBinding()]
param(
    [switch]$FullTests,
    [switch]$LaunchDesktop,
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$RunBenchmarks,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify"),
    [string]$BenchmarkOutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$OperationalVerifier = Join-Path $PSScriptRoot "verify-operational.ps1"
$BaselineVerifier = Join-Path $PSScriptRoot "verify-baseline.ps1"
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

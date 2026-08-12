[CmdletBinding()]
param(
    [switch]$ValidateOnly,
    [switch]$AllowToolchainDownload
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$Gradle = Join-Path $RepositoryRoot "gradlew.bat"
$Task = ":desktop:desktop:runLocusV2Laboratory"
$RequiredFiles = @(
    "source\desktop\desktop\build.gradle.kts",
    "source\desktop\desktop\src\main\java\org\geocedg\desktop\locus\LocusV2Laboratory.java",
    "source\desktop\desktop\src\main\java\org\geocedg\desktop\locus\LocusV2LaboratoryFrame.java",
    "source\desktop\desktop\src\main\java\org\geocedg\desktop\locus\LocusV2LaboratoryFixtures.java",
    "source\desktop\desktop\src\main\java\org\geocedg\desktop\locus\LocusV2LaboratoryController.java"
)

try {
    if (-not (Test-Path -LiteralPath $Gradle -PathType Leaf)) {
        throw "Repository Gradle wrapper not found: $Gradle"
    }
    foreach ($relativePath in $RequiredFiles) {
        $path = Join-Path $RepositoryRoot $relativePath
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Locus V2 laboratory component is missing: $relativePath"
        }
    }
    $buildSource = Get-Content -Raw -LiteralPath (
        Join-Path $RepositoryRoot "source\desktop\desktop\build.gradle.kts")
    if (-not $buildSource.Contains('"runLocusV2Laboratory"') -or
            -not $buildSource.Contains(
                'mainClass = "org.geocedg.desktop.locus.LocusV2Laboratory"')) {
        throw "The canonical Locus V2 laboratory Gradle task is not registered."
    }

    Write-Host "GeoCeDG Locus V2 Developer Laboratory"
    Write-Host "EXPERIMENTAL / INTERNAL / OPT-IN"
    Write-Host "No public command, Path contract, persistence, metric or intersection is enabled."
    Write-Host "The generated construction cannot be saved as a .ggb file."
    Write-Host "Gradle task: $Task"

    if ($ValidateOnly) {
        Write-Host "Locus V2 laboratory contract validation passed without launching GUI."
        exit 0
    }

    $arguments = @(
        $Task,
        "--no-daemon",
        "--no-problems-report",
        "--console=plain"
    )
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    Write-Host "Launching the laboratory. Close GeoCeDG and its diagnostics window to finish."
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $Gradle @arguments
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "Locus V2 laboratory launch failed with exit code $exitCode."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

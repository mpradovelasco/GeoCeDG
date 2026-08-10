[CmdletBinding()]
param(
    [string]$ResourceId = "cedg.legacy.template-v7",
    [switch]$Classic,
    [switch]$ValidateOnly,
    [switch]$AllowToolchainDownload,
    [string]$SettingsFile
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
$CatalogPath = Join-Path $RepositoryRoot "models\manifests\catalog.yml"
$IngestScript = Join-Path $PSScriptRoot "ingest.ps1"
$Gradle = Join-Path $RepositoryRoot "gradlew.bat"

function Resolve-RepositoryFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    if ([IO.Path]::IsPathRooted($RelativePath)) {
        throw "Registered resource paths must be repository-relative: $RelativePath"
    }
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot (
                $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar))))
    if (-not $absolute.StartsWith($RootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Registered path escapes the repository: $RelativePath"
    }
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "Registered file does not exist: $RelativePath"
    }
    return $absolute
}

try {
    $catalog = Get-Content -Raw -LiteralPath $CatalogPath |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    $selectedManifest = $null
    $selectedManifestPath = $null
    foreach ($manifestPath in @($catalog.models)) {
        $absoluteManifest = Resolve-RepositoryFile -RelativePath $manifestPath
        $manifest = Get-Content -Raw -LiteralPath $absoluteManifest |
            ConvertFrom-Json -Depth 100 -NoEnumerate
        if ($manifest.id -eq $ResourceId) {
            $selectedManifest = $manifest
            $selectedManifestPath = $absoluteManifest
            break
        }
    }
    if ($null -eq $selectedManifest) {
        throw "Resource is not registered in models/manifests/catalog.yml: $ResourceId"
    }
    if ($selectedManifest.template -or $selectedManifest.loaded_by_default) {
        throw "Laboratory resources must be non-template and opt-in: $ResourceId"
    }
    if ($selectedManifest.maturity -notin @(
            "legacy", "research", "experimental", "deprecated")) {
        throw "Stable resources do not use the Laboratory loader: $ResourceId"
    }
    if (-not $selectedManifest.laboratory.eligible) {
        throw "Resource is not eligible for Laboratory loading: $ResourceId"
    }

    $artifactPath = Resolve-RepositoryFile -RelativePath $selectedManifest.artifact.path
    $actualHash = (Get-FileHash -LiteralPath $artifactPath `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $selectedManifest.artifact.sha256) {
        throw "Resource hash mismatch for $ResourceId."
    }

    $resourceDirectory = Split-Path -Parent $selectedManifestPath
    & $IngestScript -ResourceId $ResourceId -Source $artifactPath `
        -TargetDirectory $resourceDirectory -Check
    if ($LASTEXITCODE -ne 0) {
        throw "Deterministic ingest check failed for $ResourceId."
    }

    Write-Host "CeDG Laboratory - EXPERIMENTAL / NON-STABLE RESOURCE"
    Write-Host "Resource: $ResourceId"
    Write-Host "Maturity: $($selectedManifest.maturity)"
    Write-Host "Artifact: $($selectedManifest.artifact.path)"
    Write-Host "SHA-256: $actualHash"
    Write-Host "The document toolbar is legacy context; it does not change the G2 profile."

    if ($ValidateOnly) {
        Write-Host "Laboratory resolution passed without launching a graphical application."
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($SettingsFile)) {
        $profileName = if ($Classic) { "classic" } else { "geocedg" }
        $SettingsFile = Join-Path ([IO.Path]::GetTempPath()) (
            "geocedg-laboratory\$profileName\preferences.properties")
    }
    $SettingsFile = [IO.Path]::GetFullPath($SettingsFile)
    New-Item -ItemType Directory -Path (Split-Path -Parent $SettingsFile) `
        -Force | Out-Null

    $task = if ($Classic) {
        ":desktop:desktop:run"
    } else {
        ":desktop:desktop:runGeoCeDG"
    }
    $applicationArguments = '"{0}" --settingsFile="{1}" --showSplash=false' -f `
        $artifactPath, $SettingsFile
    $gradleArguments = @(
        $task,
        "--args=$applicationArguments",
        "--no-daemon",
        "--no-problems-report",
        "--console=plain"
    )
    if (-not $AllowToolchainDownload) {
        $gradleArguments += "-Dorg.gradle.java.installations.auto-download=false"
    }

    $profileDescription = if ($Classic) {
        "Upstream Classic diagnostic"
    } else {
        "GeoCeDG"
    }
    Write-Host "Launching $profileDescription. Close the window to finish the task."
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $Gradle @gradleArguments
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "Laboratory launch failed with exit code $exitCode."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

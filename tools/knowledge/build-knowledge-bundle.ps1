[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path,
    [Parameter(Mandatory)] [string]$Profile,
    [string]$OutputDirectory = "artifacts/knowledge",
    [string]$ProfilesPath =
        "geocedg/specs/operations/knowledge-bundle-profiles.json",
    [string]$SchemaPath =
        "geocedg/specs/operations/knowledge-bundle.schema.json",
    [string]$BaselinePath = "docs/upstream/BASELINE_COMMIT.txt",
    [string]$ModifiedInventoryPath = "docs/upstream/modified-files.yml",
    [switch]$AllowDirty,
    [int]$MaximumFiles = 0,
    [long]$MaximumBytes = 0,
    [long]$MaximumTokens = 0,
    [long]$MaximumChunkTokens = 0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

try {
    Import-Module (Join-Path $PSScriptRoot "knowledge-bundle.psm1") -Force
    $parameters = @{
        RepositoryRoot = $RepositoryRoot
        ProfileId = $Profile
        OutputDirectory = $OutputDirectory
        ProfilesPath = $ProfilesPath
        SchemaPath = $SchemaPath
        BaselinePath = $BaselinePath
        ModifiedInventoryPath = $ModifiedInventoryPath
        MaximumFiles = $MaximumFiles
        MaximumBytes = $MaximumBytes
        MaximumTokens = $MaximumTokens
        MaximumChunkTokens = $MaximumChunkTokens
    }
    if ($AllowDirty) {
        $parameters.AllowDirty = $true
    }
    $result = New-GeoCeDGKnowledgeBundle @parameters
    Write-Host "Knowledge bundle generated."
    Write-Host "  ID: $($result.BundleId)"
    Write-Host "  Profile: $Profile"
    Write-Host "  Dirty: $($result.Dirty)"
    Write-Host "  Files: $($result.FileCount)"
    Write-Host "  Chunks: $($result.ChunkCount)"
    Write-Host "  Manifest: $($result.ManifestPath)"
    Write-Host "  Archive: $($result.ArchivePath)"
    Write-Host "  Archive SHA-256: $($result.ArchiveSha256)"
    $result
} catch {
    Write-Error "Knowledge-bundle generation failed: $($_.Exception.Message)"
    exit 1
}

exit 0

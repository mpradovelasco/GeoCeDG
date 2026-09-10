#requires -Version 7.2
[CmdletBinding()]
param(
    [switch]$CheckToolchain,
    [switch]$RequireArtifacts,
    [string]$ArtifactRoot,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
        'geocedg-packaging-' + [guid]::NewGuid().ToString('N'))),
    [switch]$PlanOnly,
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$canonical = Join-Path $PSScriptRoot 'verify.ps1'
if (-not $Quiet) {
    Write-Host 'Canonical replacement: .\tools\agent\verify.ps1 -Profile PACKAGING'
}
$parameters = @{
    Profile = 'PACKAGING'
    LogDirectory = $LogDirectory
}
if ($CheckToolchain -or $RequireArtifacts) { $parameters.CheckToolchain = $true }
if ($RequireArtifacts) { $parameters.VerifyPackagingArtifacts = $true }
if ($PSBoundParameters.ContainsKey('ArtifactRoot')) {
    $parameters.PackagingArtifactRoot = $ArtifactRoot
}
if ($PlanOnly) { $parameters.PlanOnly = $true }
if ($Quiet) { $parameters.Quiet = $true }
$global:LASTEXITCODE = $null
& $canonical @parameters
$code = $global:LASTEXITCODE
if ($null -eq $code) {
    Write-Error 'Canonical PACKAGING verifier returned without an exit code.'
    exit 3
}
exit $code

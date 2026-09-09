#requires -Version 7.2
[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$IncrementalBuild,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
        'geocedg-verification-infrastructure-' + [guid]::NewGuid().ToString('N'))),
    [switch]$Quiet,
    [switch]$PlanOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$canonical = Join-Path $PSScriptRoot 'verify.ps1'
if (-not $Quiet) {
    Write-Host 'Canonical replacement: .\tools\agent\verify.ps1 -Profile INFRA_UNIT'
}
$parameters = @{
    Profile = 'INFRA_UNIT'
    LogDirectory = $LogDirectory
}
if ($SkipBuild) { $parameters.SkipBuild = $true }
if ($AllowToolchainDownload) { $parameters.AllowToolchainDownload = $true }
if ($KeepBuildOutputs) { $parameters.KeepBuildOutputs = $true }
if ($IncrementalBuild) { $parameters.IncrementalBuild = $true }
if ($PlanOnly) { $parameters.PlanOnly = $true }
if ($Quiet) { $parameters.Quiet = $true }
$global:LASTEXITCODE = $null
& $canonical @parameters
$code = $global:LASTEXITCODE
if ($null -eq $code) {
    Write-Error 'Canonical INFRA_UNIT verifier returned without an exit code.'
    exit 3
}
exit $code

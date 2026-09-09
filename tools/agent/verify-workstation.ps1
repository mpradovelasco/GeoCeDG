#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
        'geocedg-workstation-' + [guid]::NewGuid().ToString('N'))),
    [switch]$PlanOnly,
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$canonical = Join-Path $PSScriptRoot 'verify.ps1'
if (-not $Quiet) {
    Write-Host 'Canonical command: .\tools\agent\verify.ps1 -Profile WORKSTATION'
}
$parameters = @{
    Profile = 'WORKSTATION'
    LogDirectory = $LogDirectory
}
if ($PlanOnly) { $parameters.PlanOnly = $true }
if ($Quiet) { $parameters.Quiet = $true }
$global:LASTEXITCODE = $null
& $canonical @parameters
$code = $global:LASTEXITCODE
if ($null -eq $code) {
    Write-Error 'Canonical WORKSTATION verifier returned without an exit code.'
    exit 3
}
exit $code

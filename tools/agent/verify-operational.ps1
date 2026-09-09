#requires -Version 7.2
[CmdletBinding()]
param(
    [switch]$Quiet,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
        'geocedg-operational-' + [guid]::NewGuid().ToString('N'))),
    [switch]$PlanOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$canonical = Join-Path $PSScriptRoot 'verify.ps1'
if (-not $Quiet) {
    Write-Host 'Canonical replacement: .\tools\agent\verify.ps1 -Profile OPERATIONAL'
}
$parameters = @{
    Profile = 'OPERATIONAL'
    LogDirectory = $LogDirectory
}
if ($PlanOnly) { $parameters.PlanOnly = $true }
if ($Quiet) { $parameters.Quiet = $true }
$global:LASTEXITCODE = $null
& $canonical @parameters
$code = $global:LASTEXITCODE
if ($null -eq $code) {
    Write-Error 'Canonical OPERATIONAL verifier returned without an exit code.'
    exit 3
}
exit $code

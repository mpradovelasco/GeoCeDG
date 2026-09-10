#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$TargetRepositoryRoot,
    [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
    [Parameter(Mandatory)] [string]$ReceiptPath,
    [string]$ApprovedCommit,
    [string]$ResultPath,
    [string]$LogDirectory,
    [string]$CloseoutCommit,
    [string]$PolicyPath,
    [string]$TechnicalEvidenceBundleDirectory,
    [string]$TechnicalEvidenceBundleSha256
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
foreach ($legacy in @($LogDirectory, $CloseoutCommit, $PolicyPath,
        $TechnicalEvidenceBundleDirectory, $TechnicalEvidenceBundleSha256)) {
    if (-not [string]::IsNullOrWhiteSpace($legacy)) {
        throw 'Legacy bundle, policy and closeout-commit arguments are retired; supply the acceptance receipt.'
    }
}
$parameters = @{
    Action = 'INSPECT'
    RepositoryRoot = $TargetRepositoryRoot
    CandidateCommit = $ReviewedTechnicalCommit
    ReceiptPath = $ReceiptPath
}
if (-not [string]::IsNullOrWhiteSpace($ApprovedCommit)) {
    $parameters.ApprovedCommit = $ApprovedCommit
}
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    $parameters.ResultPath = $ResultPath
}
& (Join-Path $PSScriptRoot 'phase-closeout.ps1') @parameters
exit $global:LASTEXITCODE

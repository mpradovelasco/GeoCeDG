#requires -Version 7.2
# Post-run deterministic catalog reconciliation (verification-levels.md section
# 11.3, ADR 0030). ANALYZE establishes eligibility before any repository change;
# VALIDATE checks a clean checkout of the reconciliation commit and writes the
# reconciliation receipt. Neither action runs FINAL, writes the source run or
# issues a FINAL receipt. Exit 0 = eligible/accepted, 3 = rejected.
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [ValidateSet('ANALYZE', 'VALIDATE')] [string]$Action,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../..'),
    [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
    [string]$MechanismCommit,
    [string]$ReconciliationCommit,
    [Parameter(Mandatory)] [string]$SourceResultPath,
    [Parameter(Mandatory)] [string]$SourceRunId,
    [Parameter(Mandatory)] [string]$ExpectedSourceResultSha256,
    # Several paths may be joined with the platform path separator, because
    # `pwsh -File` binds one string per parameter.
    [Parameter(Mandatory)] [string[]]$DiscoveryEvidencePath,
    [string]$ReceiptPath,
    [string]$ResultPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-catalog-reconciliation.psm1') -Force

$discovery = [string[]]@($DiscoveryEvidencePath | ForEach-Object {
    ([string]$_).Split([IO.Path]::PathSeparator, [StringSplitOptions]::RemoveEmptyEntries)
})
if ($Action -ceq 'VALIDATE' -and ([string]::IsNullOrWhiteSpace($ReconciliationCommit) -or
        [string]::IsNullOrWhiteSpace($ReceiptPath))) {
    throw 'VALIDATE requires -ReconciliationCommit and -ReceiptPath.'
}
if ($Action -ceq 'ANALYZE' -and -not [string]::IsNullOrWhiteSpace($ReceiptPath)) {
    throw 'ANALYZE never writes a reconciliation receipt.'
}
foreach ($output in @($ReceiptPath, $ResultPath)) {
    if (-not [string]::IsNullOrWhiteSpace($output)) {
        [void](Assert-VerificationCatalogReconciliationOutputPath -RepositoryRoot $RepositoryRoot `
            -SourceResultPath $SourceResultPath -Path $output)
    }
}

$parameters = @{
    Action = $Action
    RepositoryRoot = $RepositoryRoot
    ReviewedTechnicalCommit = $ReviewedTechnicalCommit
    SourceResultPath = $SourceResultPath
    SourceRunId = $SourceRunId
    ExpectedSourceResultSha256 = $ExpectedSourceResultSha256
    DiscoveryEvidencePath = $discovery
}
if (-not [string]::IsNullOrWhiteSpace($MechanismCommit)) { $parameters.MechanismCommit = $MechanismCommit }
if (-not [string]::IsNullOrWhiteSpace($ReconciliationCommit)) { $parameters.ReconciliationCommit = $ReconciliationCommit }
if ($Action -ceq 'VALIDATE') { $parameters.RequireReconciliationCheckout = $true }
$run = Invoke-VerificationCatalogReconciliation @parameters

$receiptFile = $null
if ([string]$run.status -ceq 'ACCEPTED') {
    $receiptFile = Write-VerificationCatalogReconciliationReceipt -RepositoryRoot $RepositoryRoot `
        -Path $ReceiptPath -Receipt $run.receipt
}
$summary = [ordered]@{
    schema_version = 1
    operation = 'POST_RUN_CATALOG_RECONCILIATION'
    action = $Action
    status = [string]$run.status
    rejection_code = $run.code
    rejection_message = $run.message
    receipt_path = $receiptFile
    receipt_id = $(if ($null -ne $run.receipt) { [string]$run.receipt.receipt_id } else { $null })
    analysis = $run.analysis
    source_run_written = $false
    final_receipt_issued = $false
    heavy_campaign_run = $false
}
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    [void](Write-VerificationAtomicJson -Path ([IO.Path]::GetFullPath($ResultPath)) -Value $summary)
}
$summary | ConvertTo-Json -Depth 20
if ([string]$run.status -cin @('ELIGIBLE', 'ACCEPTED')) { exit 0 }
Write-Error ("Catalog reconciliation rejected: " + [string]$run.message) -ErrorAction Continue
exit 3

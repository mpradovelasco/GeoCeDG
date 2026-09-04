#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$TargetRepositoryRoot,
    [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
    [Parameter(Mandatory)] [string]$CloseoutCommit,
    [Parameter(Mandatory)] [string]$PolicyPath,
    [Parameter(Mandatory)] [string]$TechnicalEvidenceBundleDirectory,
    [Parameter(Mandatory)] [string]$TechnicalEvidenceBundleSha256,
    [Parameter(Mandatory)] [string]$LogDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
. (Join-Path $PSScriptRoot 'phase-lifecycle.ps1')
$verificationRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$targetRoot = [IO.Path]::GetFullPath($TargetRepositoryRoot)
$logRoot = [IO.Path]::GetFullPath($LogDirectory)
$failure = $null
$context = $null
$sources = @('tools/agent/verify-phase-author-closeout.ps1',
    'tools/agent/phase-lifecycle.ps1', 'tools/agent/repository-input-identity.ps1',
    'tools/agent/evidence-integrity.ps1', 'tools/agent/repository-generated-state.ps1',
    'tools/agent/verification-runtime.psm1')
$implementation = @($sources | ForEach-Object {
    [ordered]@{path=$_;sha256=Get-GeoCeDGPhaseLifecycleHash ([IO.File]::ReadAllBytes((Join-Path $verificationRoot $_)))}
})
$verificationHead = Invoke-GeoCeDGPhaseLifecycleGitText $verificationRoot @('rev-parse','HEAD')
$verificationStatus = Invoke-GeoCeDGPhaseLifecycleGitText $verificationRoot @('status','--porcelain=v1','--untracked-files=all')
try {
    . (Join-Path $PSScriptRoot 'repository-generated-state.ps1')
    Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $targetRoot -LogDirectory $logRoot
    Assert-GeoCeDGPhaseLifecycle (-not (Test-Path -LiteralPath $logRoot)) 'Refusing to overwrite closeout evidence.'
    [void](New-Item -ItemType Directory -Path $logRoot)
    $effectivePolicy = if ([IO.Path]::IsPathRooted($PolicyPath)) { $PolicyPath } else { Join-Path $targetRoot $PolicyPath }
    $context = Get-GeoCeDGPhaseAuthorCloseoutTargetContext -RepositoryRoot $targetRoot `
        -ReviewedTechnicalCommit $ReviewedTechnicalCommit -CloseoutCommit $CloseoutCommit `
        -PolicyPath $effectivePolicy -BundleDirectory ([IO.Path]::GetFullPath($TechnicalEvidenceBundleDirectory)) `
        -BundleSha256 $TechnicalEvidenceBundleSha256
    foreach ($source in $implementation) {
        Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash ([IO.File]::ReadAllBytes((Join-Path $verificationRoot $source.path)))) -ceq $source.sha256) 'Verification implementation changed during closeout proof.'
    }
    Assert-GeoCeDGPhaseLifecycle ((Invoke-GeoCeDGPhaseLifecycleGitText $verificationRoot @('rev-parse','HEAD')) -ceq $verificationHead -and
        (Invoke-GeoCeDGPhaseLifecycleGitText $verificationRoot @('status','--porcelain=v1','--untracked-files=all')) -ceq $verificationStatus) 'Verification-code cohort changed during proof.'
} catch { $failure = $_.Exception.Message }
$result = [ordered]@{
    schemaVersion = 1
    state = $(if ($failure) { 'FAILED' } else { 'AUTHOR_CLOSEOUT_GIT_IDENTITY_PASSED_NOT_NEW_TECHNICAL_EXECUTION' })
    reviewedTechnicalCommit = $ReviewedTechnicalCommit
    closeoutCommit = $CloseoutCommit
    verificationCodeCommit = $verificationHead
    verificationCodeStatus = $verificationStatus
    verificationCodeSources = $implementation
    technicalEvidenceBundleSha256 = $TechnicalEvidenceBundleSha256
    targetContext = $context
    productRuntimeExecuted = $false
    currentRunReceiptProduced = $false
    authorDecisionCreatedByVerifier = $false
    selfApproved = $false
    failure = $failure
}
if (Test-Path -LiteralPath $logRoot -PathType Container) {
    $resultPath = Join-Path $logRoot 'author-closeout-result.json'
    if (-not (Test-Path -LiteralPath $resultPath)) {
        [IO.File]::WriteAllText($resultPath, (($result | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"), [Text.UTF8Encoding]::new($false))
    }
}
if ($failure) { Write-Error $failure -ErrorAction Continue; exit 1 }
Write-Host "AUTHOR_CLOSEOUT PASS: technical execution $ReviewedTechnicalCommit; closeout $CloseoutCommit."
Write-Host 'Tracked executable identity is Git-based; valid materialization is checked separately. No technical execution was repeated.'
exit 0

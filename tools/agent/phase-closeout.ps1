#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('READINESS', 'PREPARE', 'FINALIZE', 'AUDIT')]
    [string]$Action,
    [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
    [string]$CloseoutMode,
    [Parameter(Mandatory)] [string]$TechnicalCommit,
    [Parameter(Mandatory)] [string]$PolicyPath,
    [Parameter(Mandatory)] [string]$ReadinessReceiptPath,
    [string]$TechnicalCampaignPath,
    [string]$AuthorApprovalPath,
    [string]$PreparationResultPath,
    [string]$CloseoutCommit,
    [string]$ResultPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../..')
)

$Action = $Action.ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($CloseoutMode)) {
    $CloseoutMode = $CloseoutMode.ToUpperInvariant()
}

# READINESS is a cheap, mandatory fail-closed gate before an acceptance
# single-FULL campaign. It validates both closeout routes without selecting
# either. PREPARE is limited to the policy-derived status/decision delta.
# FINALIZE is an explicit VERIFIED-only commit/tag/fast-forward/atomic-push
# authority followed by AUDIT. AUTHOR_OPERATED never enters FINALIZE.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

. (Join-Path $PSScriptRoot 'closeout-workflow.ps1')

function Assert-GeoCeDGPhaseCloseoutCli {
    param([bool]$Condition, [Parameter(Mandatory)] [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Resolve-GeoCeDGPhaseCloseoutOutput {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )
    $full = [IO.Path]::GetFullPath($Path)
    Assert-GeoCeDGPhaseCloseoutCli (-not (Test-Path -LiteralPath $full)) `
        "Refusing to overwrite existing ${Description}: $full"
    $rootPath = [IO.Path]::GetFullPath($Root).TrimEnd('/', '\')
    $comparison = if ($IsWindows) {
        [StringComparison]::OrdinalIgnoreCase
    } else { [StringComparison]::Ordinal }
    $prefix = $rootPath + [IO.Path]::DirectorySeparatorChar
    if ($full.StartsWith($prefix, $comparison)) {
        $relative = [IO.Path]::GetRelativePath($rootPath, $full).Replace('\', '/')
        foreach ($candidate in @($relative, "$relative.geocedg-tmp-probe")) {
            $ignored = Invoke-GeoCeDGCloseoutGitText $rootPath `
                @('check-ignore', '--quiet', '--no-index', '--', $candidate) `
                -AllowFailure
            Assert-GeoCeDGPhaseCloseoutCli ($ignored.ExitCode -eq 0) `
                "In-repository $Description and its atomic temporary must be ignored generated evidence: $candidate"
        }
    }
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
    return $full
}

function Write-GeoCeDGPhaseCloseoutOutput {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Value
    )
    $bytes = ConvertTo-GeoCeDGCloseoutJsonBytes $Value
    $temporaryPath = "$Path.geocedg-tmp-$([guid]::NewGuid().ToString('N'))"
    $stream = $null
    $temporaryOwned = $false
    try {
        $stream = [IO.File]::Open($temporaryPath, [IO.FileMode]::CreateNew,
            [IO.FileAccess]::Write, [IO.FileShare]::None)
        $temporaryOwned = $true
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush($true)
        $stream.Dispose()
        $stream = $null
        [IO.File]::Move($temporaryPath, $Path, $false)
        $temporaryOwned = $false
    } finally {
        if ($null -ne $stream) { $stream.Dispose() }
        if ($temporaryOwned -and
                (Test-Path -LiteralPath $temporaryPath -PathType Leaf)) {
            [IO.File]::Delete($temporaryPath)
        }
    }
}

$root = [IO.Path]::GetFullPath($RepositoryRoot)
$readinessOutput = $null
$resultOutput = $null
$output = $null
$failure = $null
$preparationMutationRisk = $false

try {
    Assert-GeoCeDGPhaseCloseoutCli (Test-Path -LiteralPath $root -PathType Container) `
        "Repository root does not exist: $root"
    switch ($Action) {
        'READINESS' {
            Assert-GeoCeDGPhaseCloseoutCli ([string]::IsNullOrWhiteSpace($CloseoutMode)) `
                'READINESS is mode-neutral; omit -CloseoutMode and select it explicitly only for PREPARE/FINALIZE/AUDIT.'
            Assert-GeoCeDGPhaseCloseoutCli ([string]::IsNullOrWhiteSpace(
                    $TechnicalCampaignPath) -and
                [string]::IsNullOrWhiteSpace($AuthorApprovalPath) -and
                [string]::IsNullOrWhiteSpace($PreparationResultPath) -and
                [string]::IsNullOrWhiteSpace($CloseoutCommit) -and
                [string]::IsNullOrWhiteSpace($ResultPath)) `
                'READINESS accepts no technical evidence, author approval, C, or separate result path.'
            $readinessOutput = Resolve-GeoCeDGPhaseCloseoutOutput $root `
                $ReadinessReceiptPath 'readiness receipt'
            $output = Test-GeoCeDGCloseoutAcceptancePreflight `
                -RepositoryRoot $root -TechnicalCommit $TechnicalCommit `
                -PolicyPath $PolicyPath
            Write-GeoCeDGPhaseCloseoutOutput $readinessOutput $output
        }
        'PREPARE' {
            Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                    $CloseoutMode)) 'PREPARE requires explicit -CloseoutMode.'
            Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                    $TechnicalCampaignPath) -and
                -not [string]::IsNullOrWhiteSpace($AuthorApprovalPath) -and
                [string]::IsNullOrWhiteSpace($PreparationResultPath) -and
                [string]::IsNullOrWhiteSpace($CloseoutCommit)) `
                'PREPARE requires author approval and exactly one FULL technical campaign, but no C.'
            if ($CloseoutMode -ceq 'VERIFIED') {
                Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                        $ResultPath)) `
                    'VERIFIED PREPARE requires -ResultPath so FINALIZE can consume the exact preparation receipt.'
            }
            if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
                $resultOutput = Resolve-GeoCeDGPhaseCloseoutOutput $root $ResultPath `
                    'closeout-preparation result'
            }
            $output = Invoke-GeoCeDGCloseoutPreparation -RepositoryRoot $root `
                -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath `
                -CloseoutMode $CloseoutMode `
                -ReadinessReceiptPath $ReadinessReceiptPath `
                -TechnicalCampaignPath $TechnicalCampaignPath `
                -AuthorApprovalPath $AuthorApprovalPath
            $preparationMutationRisk = $true
            if ($null -ne $resultOutput) {
                try {
                    Write-GeoCeDGPhaseCloseoutOutput $resultOutput $output
                } catch {
                    $receiptFailure = $_
                    throw "PREPARE receipt persistence failed after the exact delta was staged; repository state was preserved for inspection: $($receiptFailure.Exception.Message)"
                }
            }
        }
        'FINALIZE' {
            Assert-GeoCeDGPhaseCloseoutCli ($CloseoutMode -ceq 'VERIFIED') `
                'FINALIZE is available only for explicit -CloseoutMode VERIFIED.'
            Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                    $TechnicalCampaignPath) -and
                -not [string]::IsNullOrWhiteSpace($AuthorApprovalPath) -and
                -not [string]::IsNullOrWhiteSpace($PreparationResultPath) -and
                [string]::IsNullOrWhiteSpace($CloseoutCommit) -and
                -not [string]::IsNullOrWhiteSpace($ResultPath)) `
                'FINALIZE requires one FULL campaign, author approval, PREPARE receipt, and a fresh result path; it creates C.'
            $resultOutput = Resolve-GeoCeDGPhaseCloseoutOutput $root $ResultPath `
                'verified finalization result'
            $output = Invoke-GeoCeDGVerifiedCloseoutFinalization `
                -RepositoryRoot $root -TechnicalCommit $TechnicalCommit `
                -PolicyPath $PolicyPath -CloseoutMode $CloseoutMode `
                -ReadinessReceiptPath $ReadinessReceiptPath `
                -TechnicalCampaignPath $TechnicalCampaignPath `
                -AuthorApprovalPath $AuthorApprovalPath `
                -PreparationResultPath $PreparationResultPath
            Write-GeoCeDGPhaseCloseoutOutput $resultOutput $output
        }
        'AUDIT' {
            Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                    $CloseoutMode)) 'AUDIT requires explicit -CloseoutMode.'
            Assert-GeoCeDGPhaseCloseoutCli (-not [string]::IsNullOrWhiteSpace(
                    $TechnicalCampaignPath) -and
                -not [string]::IsNullOrWhiteSpace($AuthorApprovalPath) -and
                [string]::IsNullOrWhiteSpace($PreparationResultPath) -and
                -not [string]::IsNullOrWhiteSpace($CloseoutCommit)) `
                'AUDIT requires exact C, author approval, and exactly one FULL technical campaign.'
            if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
                $resultOutput = Resolve-GeoCeDGPhaseCloseoutOutput $root $ResultPath `
                    'post-promotion audit result'
            }
            $output = Test-GeoCeDGCloseoutPostPromotionAudit `
                -RepositoryRoot $root -TechnicalCommit $TechnicalCommit `
                -CloseoutCommit $CloseoutCommit -PolicyPath $PolicyPath `
                -CloseoutMode $CloseoutMode `
                -ReadinessReceiptPath $ReadinessReceiptPath `
                -TechnicalCampaignPath $TechnicalCampaignPath `
                -AuthorApprovalPath $AuthorApprovalPath
            if ($null -ne $resultOutput) {
                Write-GeoCeDGPhaseCloseoutOutput $resultOutput $output
            }
        }
    }
} catch {
    $failure = $_.Exception.Message
    if ($Action -ceq 'PREPARE' -and
            $failure.StartsWith('PREPARE failed after its first bounded mutation;',
                [StringComparison]::Ordinal)) {
        $preparationMutationRisk = $true
    }
    $failed = [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = $(if ($Action -ceq 'READINESS') {
                'GEOCEDG_CLOSEOUT_READINESS'
            } elseif ($Action -ceq 'PREPARE') {
                'GEOCEDG_CLOSEOUT_PREPARATION'
            } elseif ($Action -ceq 'FINALIZE') {
                'GEOCEDG_VERIFIED_CLOSEOUT_FINALIZATION'
            } else { 'GEOCEDG_CLOSEOUT_POST_PROMOTION_AUDIT' })
        state = $(if ($Action -ceq 'READINESS') {
                'CLOSEOUT_READINESS_FAILED'
            } elseif ($Action -ceq 'PREPARE') {
                'CLOSEOUT_PREPARATION_FAILED'
            } elseif ($Action -ceq 'FINALIZE') {
                'VERIFIED_CLOSEOUT_FINALIZATION_FAILED'
            } else { 'POST_PROMOTION_AUDIT_FAILED' })
        closeoutMode = $CloseoutMode
        reviewedTechnicalCommit = $TechnicalCommit
        closeoutCommit = $(if ([string]::IsNullOrWhiteSpace($CloseoutCommit)) {
                $null
            } else { $CloseoutCommit })
        heavyAcceptanceCampaignAllowed = $false
        # FINALIZE is intentionally mutating.  A thrown error can occur after
        # any prefix of commit/tag/switch/merge/push, so reporting `false`
        # would fabricate negative evidence.  The failure receipt stays honest
        # and directs the operator to inspect refs and run the read-only audit.
        automaticCommitPerformed = $(if ($Action -ceq 'FINALIZE') {
                $null
            } else { $false })
        automaticTagPerformed = $(if ($Action -ceq 'FINALIZE') {
                $null
            } else { $false })
        automaticFastForwardPerformed = $(if ($Action -ceq 'FINALIZE') {
                $null
            } else { $false })
        automaticPushPerformed = $(if ($Action -ceq 'FINALIZE') {
                $null
            } else { $false })
        mutationState = $(if ($Action -ceq 'FINALIZE') {
                'UNKNOWN_REQUIRES_INSPECTION'
            } elseif ($Action -ceq 'PREPARE' -and $preparationMutationRisk) {
                'UNKNOWN_REQUIRES_INSPECTION'
            } else { 'NONE' })
        partialMutationPossible = ($Action -ceq 'FINALIZE' -or
            ($Action -ceq 'PREPARE' -and $preparationMutationRisk))
        requiredRecovery = $(if ($Action -ceq 'FINALIZE') {
                'INSPECT_EXACT_REPOSITORY_REFS_AND_RUN_POST_PROMOTION_AUDIT'
            } elseif ($Action -ceq 'PREPARE' -and $preparationMutationRisk) {
                'INSPECT_EXACT_T_STATUS_PATHS_AND_DECISION_RECORD'
            } else { $null })
        preparationRollback = $null
        selfApproved = $false
        failure = $failure
    }
    $failurePath = if ($Action -ceq 'READINESS') {
        $readinessOutput
    } else { $resultOutput }
    if ($null -ne $failurePath -and -not (Test-Path -LiteralPath $failurePath)) {
        try { Write-GeoCeDGPhaseCloseoutOutput $failurePath $failed } catch { }
    }
    Write-Error -Message $failure -ErrorAction Continue
    exit 1
}

if ($Action -ceq 'READINESS') {
    Write-Host "CLOSEOUT_READINESS PASS: exact clean T = $TechnicalCommit"
    Write-Host "Verification class: $($output.verificationClass)"
    Write-Host "Frozen acceptance level: $($output.plannedAcceptanceLevel)"
    Write-Host 'Closeout mode: not selected (mode-neutral acceptance plan)'
    Write-Host "Validated closeout modes: $(@($output.validatedCloseoutModes) -join ', ')"
    Write-Host "Acceptance plan SHA-256: $($output.acceptancePlanSha256)"
    Write-Host "Readiness receipt: $readinessOutput"
    Write-Host "Run only the frozen $($output.plannedAcceptanceLevel) acceptance plan; any higher level requires VERIFICATION_ESCALATION_REQUEST and author authorization."
} elseif ($Action -ceq 'PREPARE') {
    Write-Host "CLOSEOUT PREPARED: exact approved T = $TechnicalCommit"
    Write-Host "Closeout mode: $CloseoutMode"
    Write-Host "Mode state: $($output.modeState)"
    Write-Host "Promotion: T=$($output.promotion.technicalTarget); expected parent=$($output.promotion.expectedCloseoutParent); branch=$($output.promotion.branch); remote=$($output.promotion.remote)"
    Write-Host "Expected annotated tag: $($output.expectedTag.name); target placeholder=$($output.expectedTag.targetIdentity)"
    Write-Host 'Exact staged closeout delta:'
    foreach ($entry in @($output.expectedCloseoutDelta)) {
        Write-Host "  $($entry.path) mode=$($entry.mode) sha256=$($entry.expectedSha256)"
    }
    Write-Host "Closeout record SHA-256: $($output.closeoutRecordSha256)"
    Write-Host 'Only the exact status/decision delta is staged. No commit, tag, merge, fast-forward, or push was performed.'
    if ($CloseoutMode -ceq 'AUTHOR_OPERATED') {
        Write-Host 'Operations remaining under human authority (structured data, not a shell command):'
        foreach ($operation in @($output.humanOperations)) {
            $argumentsJson = ConvertTo-Json -InputObject ([object[]]@(
                    $operation.arguments)) -Compress
            Write-Host "  [$($operation.order)] $($operation.operation): program=$($operation.program) arguments=$argumentsJson"
        }
    } else {
        Write-Host 'VERIFIED AUTHOR_CLOSEOUT preparation passed for the projected delta; exact C and the complete gate remain pending FINALIZE/audit.'
        foreach ($operation in @($output.verifiedWorkflowOperations)) {
            $argumentsJson = ConvertTo-Json -InputObject ([object[]]@(
                    $operation.arguments)) -Compress
            Write-Host "  [$($operation.order)] $($operation.operation): program=$($operation.program) arguments=$argumentsJson"
        }
    }
    $auditParametersJson = ConvertTo-Json -InputObject $output.postPromotionAudit.parameters `
        -Depth 20 -Compress
    Write-Host "Required automated post-promotion audit: program=$($output.postPromotionAudit.program) parameters=$auditParametersJson"
} elseif ($Action -ceq 'FINALIZE') {
    Write-Host "VERIFIED CLOSEOUT FINALIZED: T = $TechnicalCommit; C = $($output.closeoutCommit)"
    Write-Host 'Status-only commit, annotated tag, fast-forward, and atomic branch/tag push completed.'
    Write-Host "Post-promotion audit: $($output.postPromotionAudit.state)"
    Write-Host 'Technical evidence was not repeated and remains attributed to T; selfApproved=false.'
} else {
    Write-Host "POST-PROMOTION AUDIT PASS: T = $TechnicalCommit; C = $CloseoutCommit"
    Write-Host "Closeout mode: $CloseoutMode"
    Write-Host "Physical input closure at PREPARE: state=$($output.physicalInputClosureAtPreparation.state); proofSha256=$($output.physicalInputClosureAtPreparation.proofSha256)"
    Write-Host 'Technical evidence remains attributed to T; selfApproved=false.'
}
if ($null -eq $resultOutput -and $Action -ne 'READINESS') {
    $output | ConvertTo-Json -Depth 100
}
exit 0

[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9x1-extended-dxf")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EvidenceRelativePath = "geocedg/validation/export/g9x1/g9x1-evidence.json"
$ScenariosRelativePath = "geocedg/validation/export/g9x1/g9x1-scenarios.json"
$IntegrityRelativePath = "geocedg/validation/export/g9x1/g9x1-evidence.sha256"
$EvidencePath = Join-Path $RepositoryRoot $EvidenceRelativePath
$ScenariosPath = Join-Path $RepositoryRoot $ScenariosRelativePath
$IntegrityPath = Join-Path $RepositoryRoot $IntegrityRelativePath
$EntrySha = "f528b2dcbe4a802d3dfdb334f842e39ec7f33015"
$ReconciliationBranch = "reconcile/g9x1-after-g9u0-r1"
$PassTagName = "geocedg-g9x1-pass"
$OriginalEntrySha = "22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0"
$OriginalBranch = "feature/g9x1-extended-dxf-curves"
$OriginalFrozenEvidenceSha = `
    "472de35266111f994cf9fa21c180cfb048b671ccefa8d48d10481c68f2cd8cd3"
$AbsorbedG9U0VerifierRelativePath = `
    "tools/agent/verify-g9u0-locus-v2-public-surface.ps1"
$PromptRelativePath = ".github/prompts/tasks/g9x1-extended-dxf-curves.prompt.md"
$PromptSha = "fab1dd78c85b337a4f5ac29b8b56ca5565761a2b7cc199312634f42cbe975b94"
$SpecificationRelativePath = `
    "geocedg/specs/export/dxf-curve-fidelity-and-approximation.md"
$SpecificationSha = `
    "e5de67f5cc2108cb1f4f85954b3538a8971805ec976ce23a51604574454e147d"
$AdrRelativePath = `
    "docs/adr/0014-export-only-dxf-approximation-and-sidecar.md"
$AdrSha = "2bc0a4f7eed551778f1e4b50b6704214ca3dab96a2c69d0514bdc24a9c715eb3"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$script:G9X1BoundaryMode = $null
$script:G9X1AuthorityCommit = $null

. (Join-Path $PSScriptRoot "repository-generated-state.ps1")
. (Join-Path $PSScriptRoot "dxf-authority-validation.ps1")

function Assert-Condition {
    param([bool]$Condition, [Parameter(Mandatory)] [string]$Message)

    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required G9X1 file is missing: $RelativePath"
    }
    return $path
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    try {
        return Get-Content -Raw -LiteralPath $path |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON: $($_.Exception.Message)"
    }
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    $text = [IO.File]::ReadAllText($path)
    $canonical = ($text -replace "`r`n", "`n") -replace "`r", "`n"
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
}

function Assert-CanonicalHash {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$Expected
    )

    $actual = Get-CanonicalLfSha256 -RelativePath $RelativePath
    Assert-Condition -Condition ($actual -ceq $Expected) `
        -Message "Canonical-LF SHA-256 mismatch for ${RelativePath}: $actual"
}

function Get-SortedUniqueStrings {
    param([object[]]$Values)

    [string[]]$result = @($Values | ForEach-Object { [string]$_ } |
        Sort-Object -CaseSensitive -Unique)
    return $result
}

function Get-WorktreeCandidatePaths {
    $tracked = @(& git -C $RepositoryRoot diff --name-only $EntrySha --)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate paths changed from the G9X1 entry commit."
    }
    $untracked = @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate untracked G9X1 paths."
    }
    return Get-SortedUniqueStrings -Values @($tracked + $untracked)
}

function Get-CommitCandidatePaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate committed G9X1 changes."
    return Get-SortedUniqueStrings -Values @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object { $_.Replace("\", "/") })
}

function Assert-G9X1CommitShape {
    param([Parameter(Mandatory)] [string]$Commit)

    $recordText = (& git -C $RepositoryRoot rev-list --parents -n 1 `
        $Commit).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            -not [string]::IsNullOrWhiteSpace($recordText)) `
        -Message "Unable to inspect the G9X1 authority commit."
    $record = @($recordText -split '\s+')
    Assert-Condition -Condition ($record.Count -eq 2 -and
            $record[0] -eq $Commit -and $record[1] -eq $EntrySha) `
        -Message "G9X1 must be one closeout commit whose sole parent is entry."
}

function Initialize-G9X1Boundary {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $head -cmatch '^[0-9a-f]{40}$') `
        -Message "Unable to resolve the current G9X1 HEAD."

    & git -C $RepositoryRoot show-ref --verify --quiet `
        "refs/tags/$PassTagName" 2>$null
    $tagLookupExit = $LASTEXITCODE
    Assert-Condition -Condition ($tagLookupExit -eq 0 -or $tagLookupExit -eq 1) `
        -Message "Unable to resolve the G9X1 PASS tag."

    if ($tagLookupExit -eq 0) {
        $tagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$PassTagName").Trim()
        $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagType -eq "tag") `
            -Message "$PassTagName must be an annotated tag."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$tagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $authorityCommit -cmatch '^[0-9a-f]{40}$') `
            -Message "Unable to peel the G9X1 PASS tag."
        $tagText = @(& git -C $RepositoryRoot cat-file tag $tagObject) -join "`n"
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagText.Contains("G9X1") -and
                $tagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "The G9X1 annotated tag lacks the approved PASS disposition."
        Assert-G9X1CommitShape -Commit $authorityCommit
        & git -C $RepositoryRoot merge-base --is-ancestor $authorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged G9X1 closeout."
        $script:G9X1BoundaryMode = "TAGGED_DESCENDANT"
        $script:G9X1AuthorityCommit = $authorityCommit
        return
    }

    $branch = ((@(& git -C $RepositoryRoot branch --show-current) -join "")).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve the current G9X1 branch."
    if ($head -eq $EntrySha) {
        Assert-Condition -Condition ($branch -eq $ReconciliationBranch) `
            -Message "Pre-commit G9X1 must remain on $ReconciliationBranch at entry."
        $staged = @(& git -C $RepositoryRoot diff --cached --name-only --)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $staged.Count -eq 0) `
            -Message "Pre-commit G9X1 requires an empty index."
        $script:G9X1BoundaryMode = "WORKTREE"
        return
    }

    Assert-Condition -Condition ($branch -eq $ReconciliationBranch -or
            $branch -eq "main") `
        -Message "Untagged committed G9X1 must be on its reconciliation branch or main."
    Assert-G9X1CommitShape -Commit $head
    $status = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            [string]::IsNullOrWhiteSpace($status)) `
        -Message "Untagged committed G9X1 requires a clean worktree and index."
    $script:G9X1BoundaryMode = "COMMITTED_PRETAG"
    $script:G9X1AuthorityCommit = $head
}

function Get-CandidatePaths {
    if ($G9X1BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeCandidatePaths)
    }
    Assert-Condition -Condition ($G9X1BoundaryMode -in @(
            "COMMITTED_PRETAG", "TAGGED_DESCENDANT") -and
            $null -ne $G9X1AuthorityCommit) `
        -Message "The G9X1 source boundary mode was not established."
    return @(Get-CommitCandidatePaths -Commit $G9X1AuthorityCommit)
}

function Assert-ScenarioAuthority {
    param([Parameter(Mandatory)] [object]$Scenarios)

    Assert-Condition -Condition ($Scenarios.schemaVersion -eq 1 -and
            $Scenarios.phase -eq "G9X1" -and
            $Scenarios.status -eq
                "IMPLEMENTATION_CANDIDATE_SCENARIOS_SOURCE_COMPLETE" -and
            [bool]$Scenarios.countsFrozen) `
        -Message "G9X1 scenario authority status is invalid."

    $expected = [ordered]@{
        C = @{Layer = "shared"; Count = 4}
        P = @{Layer = "shared"; Count = 10}
        A = @{Layer = "shared"; Count = 14}
        L = @{Layer = "shared"; Count = 12}
        M = @{Layer = "desktop"; Count = 10}
        D = @{Layer = "desktop"; Count = 8}
        S = @{Layer = "desktop"; Count = 4}
    }
    $groups = @($Scenarios.groups)
    Assert-Condition -Condition ($groups.Count -eq $expected.Count) `
        -Message "G9X1 scenario group count is not frozen at seven."

    $ids = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    $shared = 0
    $desktop = 0
    foreach ($group in $groups) {
        Assert-Condition -Condition ($expected.Contains([string]$group.id)) `
            -Message "Unexpected G9X1 scenario group: $($group.id)"
        $contract = $expected[[string]$group.id]
        $cases = @($group.cases)
        Assert-Condition -Condition (
                $group.layer -eq $contract.Layer -and
                $cases.Count -eq $contract.Count) `
            -Message "Scenario group $($group.id) has the wrong layer/count."
        foreach ($case in $cases) {
            $caseId = [string]$case.id
            Assert-Condition -Condition (
                    $caseId -match "^X1-$($group.id)[0-9]{2}$" -and
                    $ids.Add($caseId) -and
                    -not [string]::IsNullOrWhiteSpace([string]$case.title)) `
                -Message "Invalid or duplicate G9X1 scenario ID: $caseId"
        }
        if ($group.layer -eq "shared") {
            $shared += $cases.Count
        } else {
            $desktop += $cases.Count
        }
    }
    Assert-Condition -Condition ($shared -eq 40 -and $desktop -eq 22 -and
            $ids.Count -eq 62 -and
            $Scenarios.expectedCounts.shared -eq 40 -and
            $Scenarios.expectedCounts.desktop -eq 22 -and
            $Scenarios.expectedCounts.focusedTotal -eq 62) `
        -Message "G9X1 scenario totals are not 40 shared + 22 Desktop = 62."
}

function Assert-IntegrityRecord {
    $lines = @(Get-Content -LiteralPath (
            Resolve-RequiredFile -RelativePath $IntegrityRelativePath) |
        Where-Object { $_ -and -not $_.StartsWith("#") })
    Assert-Condition -Condition ($lines.Count -eq 2) `
        -Message "G9X1 integrity record must contain exactly two entries."

    $records = @{}
    foreach ($line in $lines) {
        Assert-Condition -Condition (
                $line -match '^([0-9a-f]{64})  (.+)$') `
            -Message "Malformed G9X1 integrity record line: $line"
        $records[$Matches[2]] = $Matches[1]
    }
    foreach ($relativePath in @($EvidenceRelativePath, $ScenariosRelativePath)) {
        Assert-Condition -Condition ($records.ContainsKey($relativePath)) `
            -Message "G9X1 integrity record omits $relativePath"
        Assert-CanonicalHash -RelativePath $relativePath `
            -Expected $records[$relativePath]
    }
}

function Assert-EvidenceContract {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [object]$Scenarios
    )

    Assert-Condition -Condition ($Evidence.schemaVersion -eq 1 -and
            $Evidence.phase -eq "G9X1" -and
            $Evidence.status -eq "PASS_AUTHOR_APPROVED") `
        -Message "G9X1 evidence status is invalid."
    Assert-Condition -Condition (-not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed -and
            -not [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PASS_AUTHOR_APPROVED") `
        -Message "G9X1 author-closeout approval tuple is invalid."
    Assert-Condition -Condition (
            $Evidence.provenance.entrySha -eq $EntrySha -and
            $Evidence.provenance.branch -eq $ReconciliationBranch -and
            $Evidence.provenance.canonicalPromptCanonicalLfSha256 -eq
                $PromptSha -and
            $Evidence.provenance.specificationCanonicalLfSha256 -eq
                $SpecificationSha -and
            $Evidence.provenance.adr0014CanonicalLfSha256 -eq $AdrSha -and
            $Evidence.provenance.preAuthorReviewEvidenceCanonicalLfSha256 -eq
                "1b3f017bd9d3dd53037a0edf6de40866de1f9143ba386101878f6d2dbec0cea9") `
        -Message "G9X1 authority provenance is inconsistent."
    $original = $Evidence.provenance.originalFrozenCandidate
    $reconciliation = $Evidence.provenance.reconciliation
    Assert-Condition -Condition (
            $original.entrySha -eq $OriginalEntrySha -and
            $original.branch -eq $OriginalBranch -and
            $original.pathCount -eq 46 -and
            $original.trackedModifiedCount -eq 15 -and
            $original.newPathCount -eq 31 -and
            $original.canonicalLfEvidenceSha256 -eq
                $OriginalFrozenEvidenceSha -and
            $reconciliation.baseSha -eq $EntrySha -and
            $reconciliation.g9x1OnlyPathCount -eq 42 -and
            $reconciliation.r1OnlyPathCount -eq 9 -and
            $reconciliation.overlapPathCount -eq 4 -and
            $reconciliation.resultPathCount -eq 45 -and
            @($reconciliation.absorbedByCorrectedBasePaths).Count -eq 1 -and
            $reconciliation.absorbedByCorrectedBasePaths[0] -eq
                $AbsorbedG9U0VerifierRelativePath) `
        -Message "G9X1 frozen-candidate reconciliation provenance is inconsistent."

    Assert-Condition -Condition (
            $Evidence.phaseDisposition.G9X1 -eq
                "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.G9U1 -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $Evidence.phaseDisposition.G9B -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $Evidence.phaseDisposition.G9C -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $Evidence.phaseDisposition.G9U2 -eq "BLOCKED" -and
            $Evidence.phaseDisposition.productiveG10 -eq
                "NOT_AUTHORIZED_NOT_STARTED") `
        -Message "G9X1 phase exclusions are inconsistent."

    Assert-Condition -Condition (
            $Evidence.closeout.authorDecision -eq
                "G9X1_PASS_AUTHOR_APPROVED" -and
            $Evidence.closeout.authorReviewDate -eq "2026-08-21" -and
            $Evidence.closeout.completionTag -eq $PassTagName -and
            $Evidence.closeout.reconciledCandidatePathCount -eq 45 -and
            @($Evidence.closeout.closeoutAdditionalPaths).Count -eq 1 -and
            $Evidence.closeout.closeoutAdditionalPaths[0] -eq
                "tools/agent/verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1" -and
            $Evidence.closeout.finalPathCount -eq 46 -and
            -not [bool]$Evidence.closeout.laterG9OrG10ImplementationExecuted) `
        -Message "G9X1 closeout metadata is inconsistent."

    Assert-Condition -Condition (
            $Evidence.architecture.placement -eq
                "READ_ONLY_EXTERNAL_EXPORT_SERVICE" -and
            -not [bool]$Evidence.architecture.kernelAuthorityChanged -and
            -not [bool]$Evidence.architecture.constructionDagMutated -and
            -not [bool]$Evidence.architecture.exportFeedbackIntoGeometry -and
            [bool]$Evidence.architecture.legacyG5ExactContractRetained) `
        -Message "G9X1 external-layer authority is not preserved."

    $outcomes = Get-SortedUniqueStrings -Values @(
        $Evidence.fidelityContract.outcomes)
    $guarantees = Get-SortedUniqueStrings -Values @(
        $Evidence.fidelityContract.guarantees)
    Assert-Condition -Condition (
            (($outcomes -join ',') -ceq
                'APPROXIMATE,EXACT,INVALID,UNSUPPORTED') -and
            (($guarantees -join ',') -ceq
                'CERTIFIED_ERROR_BOUND,ESTIMATED_ERROR,FLOATING_POINT_UNCERTIFIED') -and
            $Evidence.fidelityContract.sampledChordEvidenceMaximumClaim -eq
                "ESTIMATED_ERROR" -and
            -not [bool]$Evidence.fidelityContract.partialOutputDefault -and
            [bool]$Evidence.fidelityContract.sidecarMandatoryForFidelityReduction -and
            [bool]$Evidence.fidelityContract.allExactSidecarOptional -and
            -not [bool]$Evidence.fidelityContract.universalTwoFileAtomicityClaimed -and
            -not [bool]$Evidence.fidelityContract.runtimeGitInvocationAllowed -and
            $Evidence.fidelityContract.units -eq "UNITLESS") `
        -Message "G9X1 fidelity/preflight/sidecar evidence is inconsistent."

    Assert-Condition -Condition (
            $Evidence.scenarioAuthority.expectedShared -eq 40 -and
            $Evidence.scenarioAuthority.expectedDesktop -eq 22 -and
            $Evidence.scenarioAuthority.expectedFocusedTotal -eq 62 -and
            $Evidence.scenarioAuthority.path -eq $ScenariosRelativePath) `
        -Message "G9X1 evidence does not bind the 62-scenario authority."

    $scenarioExecution = [string]$Scenarios.testExecution.status
    $focusedExecution = [string]$Evidence.validation.focused.status
    Assert-Condition -Condition ($scenarioExecution -in @("PENDING", "PASSED") -and
            $focusedExecution -eq $scenarioExecution) `
        -Message "G9X1 scenario/evidence execution states disagree."
    if ($scenarioExecution -eq "PASSED") {
        Assert-Condition -Condition (
                $Scenarios.testExecution.shared -eq 40 -and
                $Scenarios.testExecution.desktop -eq 22 -and
                $Scenarios.testExecution.focusedTotal -eq 62 -and
                $Scenarios.testExecution.failures -eq 0 -and
                $Scenarios.testExecution.errors -eq 0 -and
                $Scenarios.testExecution.skipped -eq 0 -and
                $Evidence.validation.focused.shared -eq 40 -and
                $Evidence.validation.focused.desktop -eq 22 -and
                $Evidence.validation.focused.total -eq 62 -and
                $Evidence.validation.focused.failures -eq 0 -and
                $Evidence.validation.focused.errors -eq 0 -and
                $Evidence.validation.focused.skipped -eq 0) `
            -Message "Recorded G9X1 focused results are not clean 62/62."
    }
    Assert-Condition -Condition (
            $Evidence.validation.reconciliationPrerequisites.g9u0R1.status -eq
                "PASSED" -and
            $Evidence.validation.reconciliationPrerequisites.g9u0R1.shared -eq 4 -and
            $Evidence.validation.reconciliationPrerequisites.g9u0R1.desktop -eq 2 -and
            $Evidence.validation.reconciliationPrerequisites.g9u0R1.total -eq 6 -and
            $Evidence.validation.reconciliationPrerequisites.historicalG9U0.status -eq
                "PASSED" -and
            $Evidence.validation.reconciliationPrerequisites.historicalG9U0.shared -eq
                81 -and
            $Evidence.validation.reconciliationPrerequisites.historicalG9U0.desktop -eq
                12 -and
            $Evidence.validation.reconciliationPrerequisites.historicalG9U0.total -eq
                93) `
        -Message "G9X1 reconciliation prerequisite results are not clean."
    Assert-Condition -Condition (
            $Evidence.validation.focusedDeterministicRerun.status -eq
                "PASSED" -and
            $Evidence.validation.focusedDeterministicRerun.shared -eq 40 -and
            $Evidence.validation.focusedDeterministicRerun.desktop -eq 22 -and
            $Evidence.validation.focusedDeterministicRerun.total -eq 62 -and
            $Evidence.validation.focusedDeterministicRerun.failures -eq 0 -and
            $Evidence.validation.focusedDeterministicRerun.errors -eq 0 -and
            $Evidence.validation.focusedDeterministicRerun.skipped -eq 0 -and
            [bool]$Evidence.validation.focusedDeterministicRerun.matchesFocused -and
            $Evidence.validation.g5Regression.status -eq "PASSED" -and
            $Evidence.validation.g5Regression.shared -eq 5 -and
            $Evidence.validation.g5Regression.desktop -eq 5 -and
            $Evidence.validation.g5Regression.total -eq 10 -and
            $Evidence.validation.g5Regression.failures -eq 0 -and
            $Evidence.validation.g5Regression.errors -eq 0 -and
            $Evidence.validation.g5Regression.skipped -eq 0 -and
            $Evidence.validation.checkstyle -eq "PASSED" -and
            $Evidence.validation.staticVerifier -eq "PASSED" -and
            $Evidence.validation.gitDiffCheck -eq "PASSED" -and
            $Evidence.validation.gitDiffCachedCheck -eq "PASSED" -and
            $Evidence.validation.composedWithoutSkipBuild.status -eq "PASSED" -and
            $Evidence.validation.composedWithoutSkipBuild.exitCode -eq 0 -and
            $Evidence.validation.composedWithoutSkipBuild.terminalOutcome -eq
                "All GeoCeDG verification gates passed.") `
        -Message "G9X1 closeout validation tuple is not fully passed."
}

function Assert-FrozenInventory {
    param([Parameter(Mandatory)] [object]$Evidence)

    $inventoryStatus = [string]$Evidence.sourceBoundary.inventoryStatus
    if ($inventoryStatus -eq "OPEN_PENDING_IMPLEMENTATION_FREEZE") {
        Assert-Condition -Condition (
                @($Evidence.sourceBoundary.candidatePaths).Count -eq 0 -and
                $null -eq $Evidence.sourceBoundary.pathCount -and
                $Evidence.hardZeroCounters.status -eq
                    "PENDING_IMPLEMENTATION_FREEZE") `
            -Message "Open G9X1 inventory contains guessed candidate values."
        Assert-Condition -Condition $SkipBuild `
            -Message ("G9X1 inventory is open. Freeze exact paths and hard-zero " +
                "counters before running focused build validation.")
        Write-Host "G9X1 inventory is open; static scaffold validated."
        return $false
    }

    Assert-Condition -Condition ($inventoryStatus -eq "FROZEN" -and
            $Evidence.sourceBoundary.frozenAgainst -eq $EntrySha -and
            $Evidence.sourceBoundary.trackedModifiedCount -eq 15 -and
            $Evidence.sourceBoundary.newPathCount -eq 31 -and
            $Evidence.sourceBoundary.pathCount -eq 46) `
        -Message "Unknown or incorrectly based G9X1 inventory state."
    $expectedPaths = Get-SortedUniqueStrings -Values @(
        $Evidence.sourceBoundary.candidatePaths)
    Assert-Condition -Condition ($expectedPaths.Count -gt 0 -and
            $expectedPaths.Count -eq @($Evidence.sourceBoundary.candidatePaths).Count -and
            $expectedPaths.Count -eq $Evidence.sourceBoundary.pathCount) `
        -Message "G9X1 frozen inventory is empty, duplicated or miscounted."
    $actualPaths = Get-CandidatePaths
    $delta = @(Compare-Object -ReferenceObject $expectedPaths `
        -DifferenceObject $actualPaths -CaseSensitive)
    Assert-Condition -Condition ($delta.Count -eq 0) `
        -Message ("G9X1 candidate paths differ from frozen evidence:`n" +
            ($delta | Out-String))

    $categoryTotal = 0
    foreach ($property in $Evidence.sourceBoundary.categoryCounts.PSObject.Properties) {
        Assert-Condition -Condition ($property.Value -is [long] -or
                $property.Value -is [int]) `
            -Message "G9X1 inventory category $($property.Name) is not numeric."
        $categoryTotal += [int]$property.Value
    }
    Assert-Condition -Condition ($categoryTotal -eq $expectedPaths.Count) `
        -Message "G9X1 inventory category counts do not equal the path count."
    Assert-Condition -Condition (
            $Evidence.sourceBoundary.categoryCounts.productive -eq 28 -and
            $Evidence.sourceBoundary.categoryCounts.tests -eq 7 -and
            $Evidence.sourceBoundary.categoryCounts.validation -eq 4 -and
            $Evidence.sourceBoundary.categoryCounts.documentation -eq 4 -and
            $Evidence.sourceBoundary.categoryCounts.supporting -eq 3) `
        -Message "G9X1 closeout inventory categories are not frozen at 28/7/4/4/3."

    foreach ($path in $expectedPaths) {
        Assert-Condition -Condition (
                $path -notmatch '(^|/)(build|\.gradle|\.kotlin|artifacts)(/|$)' -and
                $path -notmatch '(^|/)(source/web|python)(/|$)' -and
                $path -notmatch '(?i)(g9u1|g9b|g9c|g9u2|g10)') `
            -Message "G9X1 inventory contains forbidden/generated scope: $path"
        Assert-Condition -Condition (
                $path -notmatch
                    '^source/shared/common/src/main/java/org/geocedg/common/kernel/' -and
                $path -notmatch
                    '^source/shared/common/src/main/java/org/geogebra/common/kernel/') `
            -Message "G9X1 changed geometric-kernel authority: $path"
    }

    Assert-Condition -Condition ($Evidence.hardZeroCounters.status -eq
            "FROZEN_ZERO") `
        -Message "G9X1 hard-zero counters are not frozen."
    foreach ($property in $Evidence.hardZeroCounters.values.PSObject.Properties) {
        Assert-Condition -Condition ($property.Value -eq 0) `
            -Message "G9X1 hard-zero counter is nonzero: $($property.Name)"
    }
    return $true
}

function Assert-TestScenarioMarkers {
    param([Parameter(Mandatory)] [object]$Scenarios)

    foreach ($group in @($Scenarios.groups)) {
        $className = [string]$group.testClass
        $relativeClass = $className.Replace('.', '/') + '.java'
        $prefix = if ($group.layer -eq "shared") {
            "source/shared/common-jre/src/test/java/"
        } else {
            "source/desktop/desktop/src/test/java/"
        }
        $path = Resolve-RequiredFile -RelativePath ($prefix + $relativeClass)
        $content = Get-Content -Raw -LiteralPath $path
        foreach ($case in @($group.cases)) {
            $matches = [regex]::Matches($content,
                [regex]::Escape([string]$case.id))
            Assert-Condition -Condition ($matches.Count -eq 1) `
                -Message ("Scenario $($case.id) must appear exactly once in " +
                    "$className; found $($matches.Count).")
        }
    }
}

function Assert-ProductStaticBoundaries {
    param([Parameter(Mandatory)] [object]$Evidence)

    $productiveJavaPaths = @(
        $Evidence.sourceBoundary.candidatePaths | Where-Object {
            $_ -match '^source/.+/src/main/java/.+\.java$'
        })
    foreach ($relativePath in $productiveJavaPaths) {
        $path = Resolve-RequiredFile -RelativePath $relativePath
        $content = Get-Content -Raw -LiteralPath $path
        Assert-GeoCeDGDxfSourceAuthority -RelativePath $relativePath -Content $content
    }
}

function Invoke-LoggedGradle {
    param([Parameter(Mandatory)] [string[]]$Arguments)

    $logPath = Join-Path $LogDirectory "g9x1-focused-gradle.log"
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $Arguments -LogPath $logPath `
            -Description "G9X1 focused tests, G5 regressions and Checkstyle" -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $Arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $Arguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Write-Host "`n==> G9X1 focused tests, G5 regressions and Checkstyle"
    Write-Host "    log: $logPath"
    $exitCode = -1
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "G9X1 Gradle validation failed with exit code $exitCode. See $logPath"
    }
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [string]$Layer,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $module = if ($Layer -eq "shared") {
        "source/shared/common-jre"
    } else {
        "source/desktop/desktop"
    }
    $path = Join-Path $RepositoryRoot (
        "$module/build/test-results/test/TEST-$ClassName.xml")
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "G9X1 test result is missing: $path"
    [xml]$result = Get-Content -Raw -LiteralPath $path
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("$ClassName is not clean: tests=$($suite.tests), " +
            "failures=$($suite.failures), errors=$($suite.errors), " +
            "skipped=$($suite.skipped).")
    Write-Host "$ClassName`: $($suite.tests) tests, 0 failures."
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null

try {
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    Initialize-G9X1Boundary
    if (-not $SkipBuild) {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedState = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9x1" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
    }

    foreach ($required in @(
            $EvidenceRelativePath,
            $ScenariosRelativePath,
            $IntegrityRelativePath,
            $PromptRelativePath,
            $SpecificationRelativePath,
            $AdrRelativePath,
            "docs/architecture/g9_extended_dxf_architecture.md",
            "docs/validation/g9x1_extended_dxf_implementation_candidate_report.md",
            "docs/roadmap/geocedg_roadmap.md",
            "docs/user/geocedg_user_guide.md",
            "models/regression/g5-dxf-foundation/construction.ggs",
            "models/regression/g5-dxf-foundation/expected-entities.yml",
            "models/regression/g5-dxf-foundation/manifest.yml")) {
        [void](Resolve-RequiredFile -RelativePath $required)
    }

    Assert-CanonicalHash -RelativePath $PromptRelativePath -Expected $PromptSha
    Assert-CanonicalHash -RelativePath $SpecificationRelativePath `
        -Expected $SpecificationSha
    Assert-CanonicalHash -RelativePath $AdrRelativePath -Expected $AdrSha

    $evidence = Read-JsonDocument -RelativePath $EvidenceRelativePath
    $scenarios = Read-JsonDocument -RelativePath $ScenariosRelativePath
    Assert-ScenarioAuthority -Scenarios $scenarios
    Assert-IntegrityRecord
    Assert-EvidenceContract -Evidence $evidence -Scenarios $scenarios
    $inventoryFrozen = Assert-FrozenInventory -Evidence $evidence

    $report = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath `
            "docs/validation/g9x1_extended_dxf_implementation_candidate_report.md")
    foreach ($fragment in @(
            "G9X1 = PASS — AUTHOR APPROVED",
            "Self-approved: **no**", "Author-approved: **yes**",
            "PASS claimed: **yes**", "selfApproved = false",
            "authorApproved = true", "passClaimed = true", $EntrySha,
            $OriginalEntrySha, $OriginalFrozenEvidenceSha, "46 paths",
            "15 tracked modifications", "40 shared + 22 Desktop", "62",
            "NO LATER G9 OR G10 IMPLEMENTATION WAS EXECUTED")) {
        Assert-Condition -Condition ($report.Contains($fragment)) `
            -Message "G9X1 author-closeout report is missing: $fragment"
    }
    $guide = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/user/geocedg_user_guide.md")
    foreach ($fragment in @(
            "G9X1 = PASS — AUTHOR APPROVED", "partialOutput=false",
            "ESTIMATED_ERROR", "`$INSUNITS=0", "experimental",
            "default-off")) {
        Assert-Condition -Condition ($guide.Contains($fragment)) `
            -Message "The living guide is missing the G9X1 boundary: $fragment"
    }
    $architecture = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/architecture/g9_extended_dxf_architecture.md")
    Assert-Condition -Condition (
            $architecture.Contains("G9X1 = PASS — AUTHOR APPROVED") -and
            $architecture.Contains("external") -and
            $architecture.Contains("default-off")) `
        -Message "The G9X1 architecture does not record the approved boundary."
    $roadmap = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/roadmap/geocedg_roadmap.md")
    Assert-Condition -Condition (
            $roadmap.Contains("G9X1 = PASS — AUTHOR APPROVED") -and
            $roadmap.Contains("G9U1") -and
            $roadmap.Contains("NOT AUTHORIZED")) `
        -Message "The roadmap does not preserve the G9X1 closeout boundary."

    if ($inventoryFrozen) {
        Assert-TestScenarioMarkers -Scenarios $scenarios
        Assert-ProductStaticBoundaries -Evidence $evidence
    }
    & (Join-Path $PSScriptRoot "tests/dxf-authority-validation.Tests.ps1") `
        -LogDirectory (Join-Path $LogDirectory "authority-infrastructure")
    Assert-Condition -Condition ($?) -Message "G9X1 DXF authority infrastructure fixtures failed."

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9X1."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9X1."

    if (-not $SkipBuild) {
        $sharedGroups = @($scenarios.groups | Where-Object {
                $_.layer -eq "shared" })
        $desktopGroups = @($scenarios.groups | Where-Object {
                $_.layer -eq "desktop" })
        $arguments = @(
            ":shared:common-jre:test",
            "--tests", "org.geocedg.common.export.GeometryExportFoundationTest"
        )
        foreach ($group in $sharedGroups) {
            $arguments += @("--tests", [string]$group.testClass)
        }
        $arguments += @(
            ":desktop:desktop:test",
            "--tests", "org.geocedg.desktop.GeoCeDGProfileTest"
        )
        foreach ($group in $desktopGroups) {
            $arguments += @("--tests", [string]$group.testClass)
        }
        $arguments += @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleMain",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        if (-not $AllowToolchainDownload) {
            $arguments += "-Dorg.gradle.java.installations.auto-download=false"
        }
        Invoke-LoggedGradle -Arguments $arguments
        foreach ($group in @($sharedGroups + $desktopGroups)) {
            Assert-TestResult -ClassName ([string]$group.testClass) `
                -Layer ([string]$group.layer) `
                -ExpectedTests @($group.cases).Count
        }
        Assert-TestResult `
            -ClassName "org.geocedg.common.export.GeometryExportFoundationTest" `
            -Layer "shared" -ExpectedTests 5
        Assert-TestResult -ClassName "org.geocedg.desktop.GeoCeDGProfileTest" `
            -Layer "desktop" -ExpectedTests 5
        Write-Host "G9X1 focused total: 62 tests, 0 failures."
        Write-Host "Inherited G5 regression total: 10 tests, 0 failures."
    } else {
        Write-Host "Skipping G9X1 Gradle validation because -SkipBuild was supplied."
    }

    Write-Host "G9X1 boundary mode: $G9X1BoundaryMode"
    Write-Host "G9X1 = PASS — AUTHOR APPROVED"
    Write-Host "G9X1 extended DXF verification passed."
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9X1 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
            if ($finalStatus -ne $InitialStatus) {
                throw "Repository status changed during G9X1 verification.`n" +
                    "Before:`n$InitialStatus`nAfter:`n$finalStatus"
            }
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup/status failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    Write-Error $Failure.Message
    exit 1
}

exit 0

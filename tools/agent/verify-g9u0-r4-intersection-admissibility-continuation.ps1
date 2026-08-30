[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r4-intersection-admissibility-continuation")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b"
$ExpectedBranch =
    "feature/g9u0-r4-intersection-admissibility-continuation"
$R3PassTagName = "geocedg-g9u0-r3-pass"
$R3PassTagObject = "1c1be8ebb58be9ad4c4e7242bc56105f9f310068"
$R3PassCommit = "ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b"
$CandidateStatus = "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_RE_REVIEW"
$FixturePath =
    "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg"
$FixtureLength = 13301
$FixtureSha256 =
    "47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c"
$FourSolutionFixturePath =
    "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg"
$FourSolutionFixtureLength = 14601
$FourSolutionFixtureSha256 =
    "51dcf7a002cb3984bb4cf5843d50e100f4bc8ef91217d4502fa7987c5b1ec21c"
$HistoricalLedgerV2Length = 1471
$HistoricalLedgerV2Sha256 =
    "cfdbe33112291bebef6e5825d7ad18a1cb986eba647b6deb81fa09188e5d5e21"
$PromptPath =
    ".github/prompts/tasks/g9u0-r4-intersection-admissibility-continuation.prompt.md"
$ScenarioPath =
    "geocedg/validation/g9u0-r4/g9u0-r4-intersection-admissibility-scenarios.json"
$ArchitecturePath =
    "docs/architecture/g9u0_r4_intersection_admissibility_continuation.md"
$ReportPath =
    "docs/validation/g9u0_r4_intersection_admissibility_continuation_candidate_report.md"
$AdrPath =
    "docs/adr/0017-deterministic-intersection-phase-rank-identity.md"
$IntersectionSpecPath =
    "geocedg/specs/locus/locus-v2-intersections.md"
$VerifierPath =
    "tools/agent/verify-g9u0-r4-intersection-admissibility-continuation.ps1"
$AlgoPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java"
$AddressProofPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionRootAddressProof2D.java"
$AllocationPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionRootAllocation2D.java"
$ExtendedCapabilityPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ExtendedTargetIntersectionCapability2D.java"
$RevisionEvidencePath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionRootRevisionEvidence2D.java"
$MetadataPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSemanticMetadata2D.java"
$TokenSourcePath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionRootTokenSource2D.java"
$SelectorPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionRootDeterministicSelector2D.java"
$ResolverPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicIntersectionRootIdentityResolver2D.java"
$TransitionPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicIntersectionRootTransition2D.java"
$PublicCapabilityPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicTargetIntersectionCapability2D.java"
$ContinuationPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionContinuation2D.java"
$ResultPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionResult2D.java"
$TargetsPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTargets2D.java"
$ConicTargetPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/NondegenerateConicIntersectionTarget2D.java"
$ImplicitTargetPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RegularPolynomialImplicitIntersectionTarget2D.java"
$SolverPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionSolver2D.java"
$PairSolverPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionSolver2D.java"
$LedgerPath =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java"
$AuthorTestPath =
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R4IntersectionAdmissibilityContinuationTest.java"
$LedgerTestPath =
    "source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/G9U0R4TokenLedgerAllocationTest.java"
$HistoricalTokenTestPath =
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0IntersectionTokenTest.java"
$PersistenceCompatibilityTestPath =
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0PersistenceCompatibilityTest.java"
$R3InspectorTestPath =
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R3InspectorWorkflowTest.java"
$NativeArchiveTestPath =
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R4NativeArchivePersistenceTest.java"
$MenuDefaultPath =
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties"
$MenuEnglishPath =
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_en.properties"
$MenuSpanishPath =
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_es.properties"
$HistoricalR3VerifierPath =
    "tools/agent/verify-g9u0-r3-public-locus-ui-hardening.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)

if ([string]::IsNullOrWhiteSpace($CanonicalSummaryPath)) {
    $CanonicalSummaryPath = Join-Path $LogDirectory "canonical-summary.json"
} else {
    $CanonicalSummaryPath = [IO.Path]::GetFullPath($CanonicalSummaryPath)
}
if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
    $CompareCanonicalSummaryPath =
        [IO.Path]::GetFullPath($CompareCanonicalSummaryPath)
}

. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

$PublicKernelTestClass =
    "org.geocedg.common.locus.G9U0R4IntersectionAdmissibilityContinuationTest"
$LedgerTestClass =
    "org.geocedg.common.kernel.locus.intersection.G9U0R4TokenLedgerAllocationTest"
$DesktopArchiveTestClass =
    "org.geocedg.desktop.G9U0R4NativeArchivePersistenceTest"
$ExpectedTestMethods = [ordered]@{
    $PublicKernelTestClass = @(
        "authorFourSolutionFixtureUsesFourIntrinsicSemanticSelectors",
        "authorFourSolutionSelectorsIgnoreEverySolverPermutation",
        "authorFourSolutionBindingsArePathIndependentAndMoveRegularly",
        "authorFourSolutionPointsRemainDefinedThroughoutRegularMotion",
        "authorMidpointCircleRootsAreInitiallyPointAdmissible",
        "publishedR3SingletonPositiveControlRemainsAdmissible",
        "preR4V1XmlTokenPointMigratesWithoutTokenChangeAndTracksMotion",
        "authorTokenPointsSurviveSaveReopenAndSameStateRecompute",
        "authorMidpointDirectRegularMotionRetainsExactTokenPoints",
        "authorMidpointBindingIsPathIndependentAcrossRegularHistories",
        "authorMidpointBroadRegularMotionIsContinuousAndDoesNotSwap",
        "materializedAuthorTokenPointUsesNormalUndoRedoLifecycle",
        "localAdmissibilityRemainsIndependentOfGlobalCompleteness",
        "disappearanceNewAppearanceAndTangencyRemainFailClosed",
        "periodicSeamIsCanonicalAndDoesNotDuplicateTheBoundaryRoot",
        "orientedRootCrossesPeriodicSeamWithoutDuplicateOrTokenSwap",
        "stableTransverseMotionUsesDeterministicCurrentSelectors",
        "spatialLeftRightOrderCanReverseWithoutTokenSwap",
        "uniqueDeterministicSelectorSurvivesUnobservedDirectUpdate",
        "repeatedDeterministicSelectorsUseIntrinsicSemanticRank",
        "rankedCollisionGroupAppearanceAndDisappearanceInvalidatesWithoutShift",
        "reversingProviderOrientationNeverTransfersRankedTokensWithoutMap",
        "rankedPeriodicSeamInvalidatesInsteadOfRotatingOpaqueTokens",
        "mergeCandidateParentKeysRemainExplicitAndNonAdmissible",
        "stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence"
    )
    $LedgerTestClass = @(
        "twoDistinctFirstAllocationsReceiveDistinctOpaqueIdentities",
        "duplicateCurrentSelectorReturnsTheSameAllocationAndFailsClosed",
        "changedRevisionAddressKeepsUniqueDeterministicIdentity",
        "differentDeterministicSelectorNeverReusesACommittedAddress",
        "selectorRejectsContextMismatchButNotParameterMotion",
        "unavailablePublicationBurnsPriorAllocation",
        "authorizedCopyRebasesContractAndRetainsIncarnation",
        "multiRootCurrentAllocationsCopyThroughOneToOneProvenance",
        "allocationSnapshotRoundTripRetainsExactOpaqueIdentity",
        "legacyMintRetainsItsExactReuseAndPersistenceContract",
        "r3SingletonTokenMigratesWithoutChangingItsExactOpaqueMaterial",
        "r3SingletonMigrationRequiresTheExactInitialAddress",
        "deterministicSelectorRejectsAGermFromAnotherComponent",
        "intrinsicPhaseSelectorRoundTripPreservesSemanticFrame",
        "phaseSelectorLedgerRoundTripRetainsExactOpaqueTokens",
        "changedVerifiedCollisionCardinalityBurnsAllRankedBindings",
        "orientationReversalCannotReuseRankedAllocationsWithoutDeclaredMap",
        "publishedContinuationKeyExposesItsExactVersionedSelector",
        "persistedAllocationRejectsNoncanonicalIncarnationSuffix",
        "persistedBindingRejectsForgedLegacyTokenMaterial",
        "persistedSnapshotRejectsDuplicateDeterministicBinding",
        "deterministicSelectorResolvesMovedAddressWithoutHistory",
        "multiRootCopyUsesOneSemanticMappingPerSharedLegacyIncarnation"
    )
    $DesktopArchiveTestClass = @(
        "newMidpointCircleTokensSurviveNativeCedgSaveAndReopen",
        "nativeCedgReopenPathMatchesDirectDeterministicBinding"
    )
}
$DesktopTestClasses = @(
    $DesktopArchiveTestClass
)
$ExpectedPublicKernelCount = $ExpectedTestMethods[$PublicKernelTestClass].Count
$ExpectedLedgerCount = $ExpectedTestMethods[$LedgerTestClass].Count
$ExpectedDesktopCount = $ExpectedTestMethods[$DesktopArchiveTestClass].Count
$ExpectedFocusedCount = $ExpectedPublicKernelCount + $ExpectedLedgerCount +
    $ExpectedDesktopCount
$ExpectedFocusedRunALogRoot =
    "artifacts/g9u0-r4/four-root-characterization-a"
$ExpectedFocusedRunBLogRoot =
    "artifacts/g9u0-r4/four-root-characterization-b"
$ExpectedComposedLogRoot =
    "artifacts/g9u0-r4/four-root-characterization-composed"

$RequiredProductiveSourcePaths = @(
    $AlgoPath,
    $AddressProofPath,
    $AllocationPath,
    $ExtendedCapabilityPath,
    $RevisionEvidencePath,
    $MetadataPath,
    $TokenSourcePath,
    $SelectorPath,
    $ResolverPath,
    $TransitionPath,
    $PublicCapabilityPath,
    $ContinuationPath,
    $ResultPath,
    $TargetsPath,
    $ConicTargetPath,
    $ImplicitTargetPath,
    $SolverPath,
    $PairSolverPath,
    $LedgerPath,
    $MenuDefaultPath,
    $MenuEnglishPath,
    $MenuSpanishPath
)
$AllowedCandidateSourcePaths = @(
    $RequiredProductiveSourcePaths +
    $AuthorTestPath +
    $LedgerTestPath +
    $HistoricalTokenTestPath +
    $PersistenceCompatibilityTestPath +
    $R3InspectorTestPath +
    $NativeArchiveTestPath +
    $FourSolutionFixturePath
)
$DeterministicSourcePaths = @(
    $AllowedCandidateSourcePaths +
    $FixturePath
)

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)))
    $prefix = $RepositoryRoot.TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes repository: $RelativePath"
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9U0-R4 path is missing: $RelativePath"
    return $absolute
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile $RelativePath) |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON: $($_.Exception.Message)"
    }
}

function Get-BinarySha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return (Get-FileHash -Algorithm SHA256 -LiteralPath (
        Resolve-RequiredFile $RelativePath)).Hash.ToLowerInvariant()
}

function Assert-ExactSet {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Actual,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualUnique = @($Actual | Sort-Object -Unique)
    $expectedUnique = @($Expected | Sort-Object -Unique)
    $missing = @($expectedUnique | Where-Object { $_ -notin $actualUnique })
    $unexpected = @($actualUnique | Where-Object { $_ -notin $expectedUnique })
    Assert-Condition -Condition ($Actual.Count -eq $actualUnique.Count -and
            $missing.Count -eq 0 -and $unexpected.Count -eq 0) `
        -Message ("{0} mismatch. missing={1}; unexpected={2}" -f $Description,
            ($missing -join ", "), ($unexpected -join ", "))
}

function Get-CandidatePaths {
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only `
            --no-renames $EntrySha HEAD --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $paths.Add($path.Replace("\", "/"))
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate committed R4 candidate paths."
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only `
            --no-renames --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate unstaged R4 candidate paths."
    foreach ($path in @(& git -C $RepositoryRoot diff --cached --name-only `
            --no-renames --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate staged R4 candidate paths."
    foreach ($path in @(& git -C $RepositoryRoot ls-files --others `
            --exclude-standard)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked R4 candidate paths."
    return @($paths | Sort-Object -Unique)
}

function Assert-EntryAuthority {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve current HEAD."
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $branch -eq $ExpectedBranch) `
        -Message "R4 candidate verification requires $ExpectedBranch."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $head
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R4 entry is not an ancestor of current HEAD."

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$R3PassTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagObject -eq $R3PassTagObject) `
        -Message "The R3 PASS annotated tag object changed."
    $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagType -eq "tag") `
        -Message "$R3PassTagName is not an annotated tag."
    $tagPeel = (& git -C $RepositoryRoot rev-parse "$tagObject^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagPeel -eq $R3PassCommit -and $R3PassCommit -eq $EntrySha) `
        -Message "The R3 PASS tag peel or R4 entry changed."

    $staged = @(& git -C $RepositoryRoot diff --cached --name-only --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $staged.Count -eq 0) `
        -Message "G9U0-R4 candidate verification requires an empty index."
}

function Assert-FixtureAuthority {
    $fixture = Resolve-RequiredFile $FixturePath
    Assert-Condition -Condition ((Get-Item -LiteralPath $fixture).Length -eq
            $FixtureLength) `
        -Message "The midpoint author fixture length changed."
    Assert-Condition -Condition ((Get-BinarySha256 $FixturePath) -ceq
            $FixtureSha256) `
        -Message "The midpoint author fixture SHA-256 changed."
    & git -C $RepositoryRoot ls-files --error-unmatch -- $FixturePath *> $null
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The midpoint fixture is not tracked regression authority."
    & git -C $RepositoryRoot diff --quiet $EntrySha -- $FixturePath
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R4 must not modify the byte-exact midpoint fixture."

    $fourSolutionFixture = Resolve-RequiredFile $FourSolutionFixturePath
    Assert-Condition -Condition ((Get-Item -LiteralPath $fourSolutionFixture).Length `
            -eq $FourSolutionFixtureLength) `
        -Message "The four-solution author fixture length changed."
    Assert-Condition -Condition ((Get-BinarySha256 $FourSolutionFixturePath) `
            -ceq $FourSolutionFixtureSha256) `
        -Message "The four-solution author fixture SHA-256 changed."
}

function Assert-ScenarioContract {
    param([Parameter(Mandatory)] [object]$Scenarios)

    $candidatePaths = @(Get-CandidatePaths)
    $candidateSource = @($candidatePaths | Where-Object {
            $_.StartsWith("source/", [StringComparison]::Ordinal)
        })
    $scenarioContractText = $Scenarios | ConvertTo-Json -Depth 100 -Compress
    Assert-Condition -Condition ($Scenarios.phase -eq "G9U0-R4" -and
            $Scenarios.status -eq $CandidateStatus -and
            [bool]$Scenarios.countsFrozen -and
            [bool]$Scenarios.implementation.started -and
            -not [bool]$Scenarios.implementation.selfApproved -and
            -not [bool]$Scenarios.implementation.authorApproved -and
            -not [bool]$Scenarios.implementation.passClaimed -and
            $Scenarios.authorReview.status -eq "PENDING_AUTHOR_RE_REVIEW" -and
            -not [bool]$Scenarios.authorReview.manualSmokePassed -and
            $Scenarios.historicalAuthorSmoke -eq
                "FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION" -and
            $Scenarios.historicalAuthorSmoke2 -eq
                "IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE" -and
            $Scenarios.deterministicPolicy -eq
                "AUTHOR_APPROVED_DIRECTION" -and
            $Scenarios.fourSolutionIdentityDisposition -eq
                "INTRINSIC_PHASE_RANK_AUTHOR_APPROVED" -and
            $Scenarios.materializationPolicyStatus -eq
                "EXISTING_EXACT_TOKEN_POLICY_RETAINED" -and
            $Scenarios.adaptiveCertificationStatus -eq
                "NOT_REQUIRED_FOR_SELECTOR_COLLISION") `
        -Message "R4 scenario status must remain an unapproved candidate."
    Assert-Condition -Condition (
            $Scenarios.authority.entrySha -eq $EntrySha -and
            $Scenarios.authority.r3PassTagObject -eq $R3PassTagObject -and
            $Scenarios.authority.r3PassPeel -eq $R3PassCommit -and
            $Scenarios.fixture.path -eq $FixturePath -and
            [int]$Scenarios.fixture.lengthBytes -eq $FixtureLength -and
            $Scenarios.fixture.sha256 -eq $FixtureSha256 -and
            -not [bool]$Scenarios.fixture.modifiedByR4 -and
            $Scenarios.fourSolutionFixture.path -eq
                $FourSolutionFixturePath -and
            $Scenarios.fourSolutionFixture.authorArtifact -eq
                "artifacts/smoke-test-g9u0-r2/fouSolutions.cedg" -and
            [int]$Scenarios.fourSolutionFixture.lengthBytes -eq
                $FourSolutionFixtureLength -and
            $Scenarios.fourSolutionFixture.sha256 -eq
                $FourSolutionFixtureSha256 -and
            $Scenarios.fourSolutionFixture.disposition -eq
                "BYTE_EXACT_AUTHOR_CHARACTERIZATION_FIXTURE") `
        -Message "R4 scenario entry/fixture authority changed."
    Assert-Condition -Condition (
            [int]$Scenarios.candidateInventory.totalPaths -eq
                $candidatePaths.Count -and
            [int]$Scenarios.candidateInventory.sourcePaths -eq
                $candidateSource.Count -and
            [int]$Scenarios.candidateInventory.focusedMethodCount -eq
                $ExpectedFocusedCount) `
        -Message "R4 scenario candidate inventory changed."

    $scenarioClasses = @($Scenarios.automatedGroups | ForEach-Object {
            $_.testClass
        })
    Assert-ExactSet -Actual $scenarioClasses `
        -Expected @($ExpectedTestMethods.Keys) `
        -Description "R4 focused test classes"
    foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
        $group = @($Scenarios.automatedGroups | Where-Object {
                $_.testClass -eq $entry.Key
            })
        Assert-Condition -Condition ($group.Count -eq 1) `
            -Message "R4 scenario class $($entry.Key) is not unique."
        $methods = @($group[0].cases | ForEach-Object { $_.method })
        Assert-ExactSet -Actual $methods -Expected @($entry.Value) `
            -Description "R4 methods for $($entry.Key)"
    }
    $automatedIds = @($Scenarios.automatedGroups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.id })
    $expectedAutomated = @(
        1..$ExpectedPublicKernelCount |
            ForEach-Object { "R4-A{0:D2}" -f $_ }
    ) + @(
        1..$ExpectedLedgerCount |
            ForEach-Object { "R4-L{0:D2}" -f $_ }
    ) + @(
        1..$ExpectedDesktopCount |
            ForEach-Object { "R4-E{0:D2}" -f $_ }
    )
    Assert-ExactSet -Actual $automatedIds -Expected $expectedAutomated `
        -Description "R4 automated scenario IDs"
    $requiredIds = @($Scenarios.requiredCoverage | ForEach-Object { $_.id })
    $requiredUnique = @($requiredIds | Sort-Object -Unique)
    Assert-Condition -Condition ($requiredIds.Count -gt 0 -and
            $requiredIds.Count -eq $requiredUnique.Count -and
            @($requiredIds | Where-Object {
                    $_ -cnotmatch '^R4-[CGPFD][0-9]{2}$'
                }).Count -eq 0) `
        -Message "R4 required coverage IDs must be unique canonical IDs."
    Assert-Condition -Condition (
            -not $scenarioContractText.Contains("PENDING_EXECUTION")) `
        -Message "R4 required coverage must contain no PENDING_EXECUTION state."
    Assert-Condition -Condition (
            [bool]$Scenarios.invariants.deterministicSemanticSelectionAuthoritative -and
            [bool]$Scenarios.invariants.continuityEvidenceIsSubordinate -and
            [bool]$Scenarios.invariants.pathIndependentCurrentSelection -and
            [bool]$Scenarios.invariants.localPointAdmissibilityIndependentOfGlobalCompleteness -and
            [string]$Scenarios.invariants.unresolvedCandidateScope -eq
                "SEMANTIC_COMPONENT" -and
            [bool]$Scenarios.invariants.sameComponentUnresolvedCandidateIsLocalVeto -and
            -not [bool]$Scenarios.invariants.unrelatedUnresolvedCandidatesAreGlobalVeto -and
            -not [bool]$Scenarios.invariants.identityByCoordinate -and
            -not [bool]$Scenarios.invariants.identityByOrderOrIndex -and
            -not [bool]$Scenarios.invariants.identityByExtrinsicOrderOrIndex -and
            -not [bool]$Scenarios.invariants.identityByProximityOrScreenState -and
            -not [bool]$Scenarios.invariants.contactSignAloneIsIdentity -and
            -not [bool]$Scenarios.invariants.revisionLocalRootGermAloneIsDurableIdentity -and
            -not [bool]$Scenarios.invariants.semanticRootTubeIsIdentity -and
            -not [bool]$Scenarios.invariants.statefulOneToOneRelationRequiredForIdentity -and
            -not [bool]$Scenarios.invariants.rootExistenceIsDeterministicIdentity -and
            -not [bool]$Scenarios.invariants.deterministicIdentityIsNumericalCertification -and
            -not [bool]$Scenarios.invariants.materializationPolicyMayOverrideIdentityAmbiguity -and
            -not [bool]$Scenarios.invariants.adaptiveCertificationMayResolveSelectorCollision -and
            -not [bool]$Scenarios.invariants.certificationPolicyRelaxedByR4 -and
            [bool]$Scenarios.invariants.repeatedSelectorUsesRootRank -and
            [bool]$Scenarios.invariants.repeatedBaseSelectorUsesIntrinsicOrientedPhaseRank -and
            [bool]$Scenarios.invariants.intrinsicPhaseUsesVerifiedCollisionGroup -and
            -not [bool]$Scenarios.invariants.intrinsicRankIsSolverOrResultOrder -and
            [bool]$Scenarios.invariants.baseSelectorRemainsUnextendedWhenUnique -and
            [bool]$Scenarios.invariants.phaseRankRequiresPairwiseDisjointIsolation -and
            [bool]$Scenarios.invariants.collisionCardinalityChangeInvalidatesTokens -and
            [bool]$Scenarios.invariants.collisionCardinalityChangeFailsClosed -and
            [bool]$Scenarios.invariants.orientationReversalRequiresExplicitMap -and
            [bool]$Scenarios.invariants.orientationChangeFailsClosedWithoutDeclaredMap -and
            [bool]$Scenarios.invariants.periodicFundamentalIntervalIsDeterministicFrame -and
            [bool]$Scenarios.invariants.periodicMonodromyIsIdentityDiscontinuity -and
            [bool]$Scenarios.invariants.periodicRankRotationInvalidatesTokens -and
            -not [bool]$Scenarios.invariants.rankedTokensRotateAcrossPeriodicSeam -and
            [bool]$Scenarios.invariants.equivalentTargetScalarRepresentationPreservesSelector -and
            [bool]$Scenarios.invariants.canonicalLineAndSegmentContactOrientation -and
            [bool]$Scenarios.invariants.canonicalCentralConicAndParabolaContactOrientation -and
            [bool]$Scenarios.invariants.canonicalPolynomialImplicitContactOrientation -and
            [bool]$Scenarios.invariants.rayDirectionRemainsSemantic -and
            [bool]$Scenarios.invariants.periodicModularMatchingRequiresExactCompleteCycle -and
            -not [bool]$Scenarios.invariants.globalCompletenessIsRootIdentity -and
            [string]$Scenarios.invariants.ledgerFormatVersion -eq "3" -and
            [string]$Scenarios.invariants.previousLedgerFormatVersionAccepted -eq
                "2" -and
            [string]$Scenarios.invariants.legacyLedgerFormatVersionAccepted -eq
                "1" -and
            [string]$Scenarios.invariants.externalTokenEnvelope -eq
                "locus-root/v3_UNCHANGED" -and
            [bool]$Scenarios.invariants.legacySingletonTokenMaterialPreserved -and
            -not [bool]$Scenarios.invariants.legacySingletonMayBindIntrinsicPhaseSelector -and
            [bool]$Scenarios.invariants.topologyParentEvidenceUsesCanonicalOrder -and
            [bool]$Scenarios.invariants.topologyParentEvidenceAdversarialPermutationTested -and
            -not [bool]$Scenarios.invariants.locusPairUsesSingleLocusPhaseRank -and
            [int]$Scenarios.invariants.automaticPersistentPoints -eq 0 -and
            [int]$Scenarios.invariants.candidateMarkersIntroduced -eq 0) `
        -Message "R4 deterministic-selection invariants drifted."
    Assert-ExactSet -Actual @($Scenarios.invariants.intrinsicSelectorFrame) `
        -Expected @(
            "STABLE_COMPONENT_LINEAGE",
            "TYPED_TRANSVERSE_GERM",
            "DECLARED_ORIENTATION",
            "PERIODIC_OR_NONPERIODIC_DOMAIN_KIND",
            "VERIFIED_COLLISION_CARDINALITY",
            "INTRINSIC_ORIENTED_PHASE_RANK"
        ) -Description "R4 intrinsic selector frame"
    Assert-ExactSet `
        -Actual @($Scenarios.invariants.legacyLedgerFormatVersionsAccepted) `
        -Expected @("1", "2") `
        -Description "R4 accepted legacy ledger formats"
    Assert-Condition -Condition (
            [int]$Scenarios.expectedCurrentFocusedCounts.publicKernel -eq
                $ExpectedPublicKernelCount -and
            [int]$Scenarios.expectedCurrentFocusedCounts.ledgerAllocation -eq
                $ExpectedLedgerCount -and
            [int]$Scenarios.expectedCurrentFocusedCounts.desktopArchive -eq
                $ExpectedDesktopCount -and
            [int]$Scenarios.expectedCurrentFocusedCounts.executedJUnit -eq
                $ExpectedFocusedCount -and
            $Scenarios.testExecution.status -eq "AUTOMATED_VALIDATION_PASS" -and
            [int]$Scenarios.testExecution.focusedR4 -eq $ExpectedFocusedCount -and
            [int]$Scenarios.testExecution.failures -eq 0 -and
            [int]$Scenarios.testExecution.errors -eq 0 -and
            [int]$Scenarios.testExecution.skipped -eq 0 -and
            [int]$Scenarios.testExecution.deterministicRerun.focusedR4 -eq
                $ExpectedFocusedCount -and
            $Scenarios.testExecution.deterministicRerun.status -eq "PASS" -and
            $Scenarios.testExecution.deterministicRerun.summaryMatch -eq "EXACT" -and
            $Scenarios.testExecution.canonicalSummarySha256 -cmatch
                '^[0-9a-f]{64}$' -and
            $Scenarios.testExecution.canonicalSummarySha256 -ceq
                $Scenarios.testExecution.deterministicRerun.canonicalSummarySha256 -and
            $Scenarios.testExecution.focusedRuns.A.status -eq "PASS" -and
            [int]$Scenarios.testExecution.focusedRuns.A.focusedR4 -eq
                $ExpectedFocusedCount -and
            $Scenarios.testExecution.focusedRuns.A.logRoot -eq
                $ExpectedFocusedRunALogRoot -and
            $Scenarios.testExecution.focusedRuns.B.status -eq "PASS" -and
            [int]$Scenarios.testExecution.focusedRuns.B.focusedR4 -eq
                $ExpectedFocusedCount -and
            $Scenarios.testExecution.focusedRuns.B.logRoot -eq
                $ExpectedFocusedRunBLogRoot -and
            $Scenarios.testExecution.composed.status -eq "PASS" -and
            [int]$Scenarios.testExecution.composed.exitCode -eq 0 -and
            $Scenarios.testExecution.composed.terminal -eq
                "All GeoCeDG verification gates passed." -and
            $Scenarios.testExecution.composed.logRoot -eq
                $ExpectedComposedLogRoot) `
        -Message "R4 frozen count or automated execution state drifted."
}

function Assert-ProductStaticContracts {
    $algo = [IO.File]::ReadAllText((Resolve-RequiredFile $AlgoPath))
    $address = [IO.File]::ReadAllText((Resolve-RequiredFile $AddressProofPath))
    $allocation = [IO.File]::ReadAllText((Resolve-RequiredFile $AllocationPath))
    $extendedCapability = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $ExtendedCapabilityPath))
    $revisionEvidence = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $RevisionEvidencePath))
    $metadata = [IO.File]::ReadAllText((Resolve-RequiredFile $MetadataPath))
    $tokenSource = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $TokenSourcePath))
    $selector = [IO.File]::ReadAllText((Resolve-RequiredFile $SelectorPath))
    $resolver = [IO.File]::ReadAllText((Resolve-RequiredFile $ResolverPath))
    $transition = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $TransitionPath))
    $publicCapability = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $PublicCapabilityPath))
    $continuation = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $ContinuationPath))
    $result = [IO.File]::ReadAllText((Resolve-RequiredFile $ResultPath))
    $targets = [IO.File]::ReadAllText((Resolve-RequiredFile $TargetsPath))
    $conicTarget = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $ConicTargetPath))
    $implicitTarget = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $ImplicitTargetPath))
    $solver = [IO.File]::ReadAllText((Resolve-RequiredFile $SolverPath))
    $pairSolver = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $PairSolverPath))
    $ledger = [IO.File]::ReadAllText((Resolve-RequiredFile $LedgerPath))
    $menuDefault = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $MenuDefaultPath))
    $menuEnglish = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $MenuEnglishPath))
    $menuSpanish = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $MenuSpanishPath))
    $authorTest = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $AuthorTestPath))
    $ledgerTest = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $LedgerTestPath))

    $localMatrixMethod = ""
    $localMatrixStart = $authorTest.IndexOf(
        "void localAdmissibilityRemainsIndependentOfGlobalCompleteness()",
        [StringComparison]::Ordinal)
    if ($localMatrixStart -ge 0) {
        $localMatrixEnd = $authorTest.IndexOf(
            "void disappearanceNewAppearanceAndTangencyRemainFailClosed()",
            $localMatrixStart + 1, [StringComparison]::Ordinal)
        if ($localMatrixEnd -gt $localMatrixStart) {
            $localMatrixMethod = $authorTest.Substring($localMatrixStart,
                $localMatrixEnd - $localMatrixStart)
        }
    }
    $stableTargetMethod = ""
    $stableTargetStart = $authorTest.IndexOf(
        "void stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence()",
        [StringComparison]::Ordinal)
    if ($stableTargetStart -ge 0) {
        $stableTargetEnd = $authorTest.IndexOf(
            "private static Map<String, LocusPoint2D> admissibleTokenSnapshot(",
            $stableTargetStart + 1, [StringComparison]::Ordinal)
        if ($stableTargetEnd -gt $stableTargetStart) {
            $stableTargetMethod = $authorTest.Substring($stableTargetStart,
                $stableTargetEnd - $stableTargetStart)
        }
    }
    $equivalentRepresentationHelper = ""
    $equivalentHelperStart = $authorTest.IndexOf(
        "private static void assertEquivalentTargetRepresentation(",
        [StringComparison]::Ordinal)
    if ($equivalentHelperStart -ge 0) {
        $equivalentHelperEnd = $authorTest.IndexOf(
            "private GeoPoint materialize(",
            $equivalentHelperStart + 1, [StringComparison]::Ordinal)
        if ($equivalentHelperEnd -gt $equivalentHelperStart) {
            $equivalentRepresentationHelper = $authorTest.Substring(
                $equivalentHelperStart,
                $equivalentHelperEnd - $equivalentHelperStart)
        }
    }
    $rankedPeriodicMethod = ""
    $rankedPeriodicStart = $authorTest.IndexOf(
        "void rankedPeriodicSeamInvalidatesInsteadOfRotatingOpaqueTokens()",
        [StringComparison]::Ordinal)
    if ($rankedPeriodicStart -ge 0) {
        $rankedPeriodicEnd = $authorTest.IndexOf(
            "void mergeCandidateParentKeysRemainExplicitAndNonAdmissible()",
            $rankedPeriodicStart + 1, [StringComparison]::Ordinal)
        if ($rankedPeriodicEnd -gt $rankedPeriodicStart) {
            $rankedPeriodicMethod = $authorTest.Substring(
                $rankedPeriodicStart,
                $rankedPeriodicEnd - $rankedPeriodicStart)
        }
    }
    $phaseLedgerMethod = ""
    $phaseLedgerStart = $ledgerTest.IndexOf(
        "void phaseSelectorLedgerRoundTripRetainsExactOpaqueTokens()",
        [StringComparison]::Ordinal)
    if ($phaseLedgerStart -ge 0) {
        $phaseLedgerEnd = $ledgerTest.IndexOf(
            "void changedVerifiedCollisionCardinalityBurnsAllRankedBindings()",
            $phaseLedgerStart + 1, [StringComparison]::Ordinal)
        if ($phaseLedgerEnd -gt $phaseLedgerStart) {
            $phaseLedgerMethod = $ledgerTest.Substring(
                $phaseLedgerStart,
                $phaseLedgerEnd - $phaseLedgerStart)
        }
    }
    $historicalLedgerV2 = ""
    $historicalLedgerV2Start = $ledgerTest.IndexOf(
        "private static final String HISTORICAL_LEDGER_V2 =",
        [StringComparison]::Ordinal)
    if ($historicalLedgerV2Start -ge 0) {
        $historicalLedgerV2End = $ledgerTest.IndexOf(
            "private static final String HISTORICAL_LEDGER_V2_TOKEN =",
            $historicalLedgerV2Start + 1, [StringComparison]::Ordinal)
        if ($historicalLedgerV2End -gt $historicalLedgerV2Start) {
            $historicalLedgerV2Block = $ledgerTest.Substring(
                $historicalLedgerV2Start,
                $historicalLedgerV2End - $historicalLedgerV2Start)
            $historicalLedgerV2 = ([regex]::Matches(
                    $historicalLedgerV2Block,
                    '"(?<value>[^"\\]*)"') | ForEach-Object {
                    $_.Groups["value"].Value
                }) -join ""
        }
    }
    $historicalLedgerV2Hash = ""
    if (-not [string]::IsNullOrEmpty($historicalLedgerV2)) {
        $historicalLedgerHasher = [Security.Cryptography.SHA256]::Create()
        try {
            $historicalLedgerHashBytes = $historicalLedgerHasher.ComputeHash(
                [Text.Encoding]::UTF8.GetBytes($historicalLedgerV2))
            $historicalLedgerV2Hash = [BitConverter]::ToString(
                $historicalLedgerHashBytes)
            $historicalLedgerV2Hash = $historicalLedgerV2Hash.Replace(
                "-", "").ToLowerInvariant()
        } finally {
            $historicalLedgerHasher.Dispose()
        }
    }
    $directFirstUpdate = $rankedPeriodicMethod.IndexOf(
        "directPhase.updateCascade();", [StringComparison]::Ordinal)
    $directSecondUpdate = -1
    if ($directFirstUpdate -ge 0) {
        $directSecondUpdate = $rankedPeriodicMethod.IndexOf(
            "directPhase.updateCascade();", $directFirstUpdate + 1,
            [StringComparison]::Ordinal)
    }
    $directRootCount = -1
    $directDiscontinuity = -1
    $directOldPointsUndefined = -1
    $directTokensDisjoint = -1
    if ($directFirstUpdate -ge 0) {
        $directRootCount = $rankedPeriodicMethod.IndexOf(
            "directRich.getIntersectionResult().getFiniteSolutions().size());",
            $directFirstUpdate + 1, [StringComparison]::Ordinal)
        $directDiscontinuity = $rankedPeriodicMethod.IndexOf(
            "== IdentityStatus.IDENTITY_DISCONTINUITY",
            $directFirstUpdate + 1, [StringComparison]::Ordinal)
        $directOldPointsUndefined = $rankedPeriodicMethod.IndexOf(
            ".noneMatch(GeoPoint::isDefined)", $directFirstUpdate + 1,
            [StringComparison]::Ordinal)
        $directTokensDisjoint = $rankedPeriodicMethod.IndexOf(
            "java.util.Collections.disjoint(initialTokens,",
            $directFirstUpdate + 1, [StringComparison]::Ordinal)
    }
    $directFreshTokensDisjoint = -1
    $directFreshAdmissible = -1
    if ($directSecondUpdate -ge 0) {
        $directFreshTokensDisjoint = $rankedPeriodicMethod.IndexOf(
            "java.util.Collections.disjoint(initialTokens,",
            $directSecondUpdate + 1, [StringComparison]::Ordinal)
        $directFreshAdmissible = $rankedPeriodicMethod.IndexOf(
            "allMatch(solution -> directRich.isPointAdmissible(",
            $directSecondUpdate + 1, [StringComparison]::Ordinal)
    }

    Assert-Condition -Condition (
            $ExpectedPublicKernelCount -eq 25 -and
            $ExpectedLedgerCount -eq 23 -and
            $ExpectedDesktopCount -eq 2 -and
            $ExpectedFocusedCount -eq 50) `
        -Message "R4 focused method inventory must remain 25 + 23 + 2 = 50."
    Assert-Condition -Condition (
            $authorTest.Contains(
                "void authorFourSolutionFixtureUsesFourIntrinsicSemanticSelectors()") -and
            $authorTest.Contains(
                '"g9u0-r4/deterministic-current-root/v2/"') -and
            $authorTest.Contains(
                "void authorFourSolutionSelectorsIgnoreEverySolverPermutation()") -and
            $authorTest.Contains("assertEquals(24, permutations.size());") -and
            $authorTest.Contains("resolveDetached(algorithm,") -and
            $authorTest.Contains(
                "void authorFourSolutionBindingsArePathIndependentAndMoveRegularly()") -and
            $authorTest.Contains("for (MotionPath path : MotionPath.values())") -and
            $authorTest.Contains(
                "void authorFourSolutionPointsRemainDefinedThroughoutRegularMotion()") -and
            $authorTest.Contains("assertTokenPoints(pointTokens, rich);") -and
            $authorTest.Contains(
                "void rankedCollisionGroupAppearanceAndDisappearanceInvalidatesWithoutShift()") -and
            $authorTest.Contains(
                "void reversingProviderOrientationNeverTransfersRankedTokensWithoutMap()") -and
            $authorTest.Contains("reverse.setValue(true);") -and
            $rankedPeriodicMethod.Contains(
                "final String byteIdenticalSeed = getApp().getXML();") -and
            $rankedPeriodicMethod.Contains(
                "getApp().setXML(byteIdenticalSeed, true);") -and
            $rankedPeriodicMethod.Contains("directPhase.setValue(-0.1);") -and
            $directRootCount -gt $directFirstUpdate -and
            $directDiscontinuity -gt $directRootCount -and
            $rankedPeriodicMethod.Contains(
                "assertEquals(4, directTransitionParents.size());") -and
            $rankedPeriodicMethod.Contains(
                "assertEquals(initialTokens, directTransitionParents,") -and
            $directOldPointsUndefined -gt $directDiscontinuity -and
            $directTokensDisjoint -gt $directOldPointsUndefined -and
            $rankedPeriodicMethod.Contains("tokens(directRich)") -and
            $directSecondUpdate -gt $directOldPointsUndefined -and
            $directFreshTokensDisjoint -gt $directSecondUpdate -and
            $directFreshAdmissible -gt $directSecondUpdate -and
            $rankedPeriodicMethod.Contains("-2 * Math.PI") -and
            $ledgerTest.Contains(
                "void intrinsicPhaseSelectorRoundTripPreservesSemanticFrame()") -and
            $phaseLedgerMethod.Contains(
                "migrated.importState(HISTORICAL_LEDGER_V2);") -and
            $historicalLedgerV2.Length -eq $HistoricalLedgerV2Length -and
            $historicalLedgerV2Hash -ceq $HistoricalLedgerV2Sha256 -and
            $phaseLedgerMethod.Contains("HISTORICAL_LEDGER_V2_TOKEN") -and
            $phaseLedgerMethod.Contains(
                'String falselyRelabeledPhaseState = "2|" + encoded.substring(2);') -and
            $phaseLedgerMethod.Contains(
                "assertThrows(IllegalArgumentException.class,") -and
            $phaseLedgerMethod.Contains(
                ".importState(falselyRelabeledPhaseState));") -and
            $phaseLedgerMethod.Contains(
                "String falselyBoundLegacyPhaseState = mutateOnlyCurrentEntry(") -and
            $phaseLedgerMethod.Contains(
                "legacyPhaseSelector = phaseSelector(") -and
            $phaseLedgerMethod.Contains(
                "NEGATIVE, Orientation.INCREASING, false, 2, 0);") -and
            $phaseLedgerMethod.Contains(
                "assertFalse(phaseAllocation.isReused());") -and
            $phaseLedgerMethod.Contains(
                "assertNotEquals(exactLegacyToken, phaseAllocation.getRootToken());") -and
            $phaseLedgerMethod.Contains(
                ".importState(falselyBoundLegacyPhaseState));") -and
            $authorTest.Contains(
                "sorted(Comparator.comparing(solution ->") -and
            $authorTest.Contains(
                "actual.getLineage().getCandidateParentTokens())") -and
            $authorTest.Contains("Collections.reverse(reversedParents);") -and
            $authorTest.Contains("r4-merge-parent-order-reversed") -and
            $authorTest.Contains(
                "canonicalOrderMerge.getLineage().getCandidateParentTokens()") -and
            $ledgerTest.Contains(
                "void changedVerifiedCollisionCardinalityBurnsAllRankedBindings()") -and
            $ledgerTest.Contains("assertFalse(ledger.validatesCurrentToken(") -and
            $ledgerTest.Contains(
                "void orientationReversalCannotReuseRankedAllocationsWithoutDeclaredMap()") -and
            $ledgerTest.Contains(
                "void publishedContinuationKeyExposesItsExactVersionedSelector()")) `
        -Message "R4 intrinsic phase/rank, permutation, lifecycle, orientation or periodic-seam regression coverage drifted."

    $allocationIndex = $resolver.IndexOf("evaluation.resolveCurrentRoot(",
        [StringComparison]::Ordinal)
    $continuityIndex = $resolver.IndexOf(
        "boolean continuityEstablished = transition",
        [StringComparison]::Ordinal)
    Assert-Condition -Condition (
            $algo.Contains("PublicIntersectionRootIdentityResolver2D") -and
            $algo.Contains("IntersectionRootTokenSource2D.semantic(") -and
            $algo.Contains("publicIdentityResolver.resolve(") -and
            $resolver.Contains(
                "Map<IntersectionRootDeterministicSelector2D, List<CurrentRoot>>") -and
            $resolver.Contains("failClosedSelectorCollision(") -and
            $resolver.Contains("intrinsicPhaseRanked(") -and
            $resolver.Contains("phaseInterval(") -and
            $resolver.Contains(
                "ordered.sort(Comparator.comparingDouble(root -> root.phase.lower))") -and
            $resolver.Contains("observedPhaseTransitionParents(") -and
            $resolver.Contains("failClosedPhaseTransition(") -and
            $resolver.Contains(
                "IntersectionRootDeterministicSelector2D.ofIntrinsicPhase(") -and
            $resolver.Contains(
                "uniqueRoots.sort(Comparator.comparing(root -> root.selector))") -and
            $resolver.Contains("evaluation.resolveCurrentRoot(") -and
            $resolver.Contains("currentRoot.selector, addressProof") -and
            $resolver.Contains("finiteRootsByComponent.getOrDefault(") -and
            $resolver.Contains("transitionAuthority.relate(") -and
            $resolver.Contains(".priorFor(currentRoot.solution)") -and
            $resolver.Contains("periodic && transition.isAmbiguous(") -and
            $resolver.Contains(
                "Rank is current-snapshot authority on an oriented nonperiodic component") -and
            $resolver.Contains(
                "complete periodic component, however, the same current ranks") -and
            $resolver.Contains("This relation only guards reuse of old tokens") -and
            $resolver.Contains("it never selects the current") -and
            $resolver.Contains("transition.isBudgetExhausted()") -and
            $resolver.Contains("DiagnosticCode.WORK_LIMIT_REACHED") -and
            $resolver.Contains(
                "continuity comparison budget; deterministic ") -and
            $resolver.Contains(
                "current selection remained authoritative") -and
            $resolver.Contains("IdentityStatus.NEW_TOPOLOGICAL_SOLUTION") -and
            $resolver.Contains(
                "IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED") -and
            $allocationIndex -ge 0 -and $continuityIndex -gt $allocationIndex) `
        -Message "R4 public resolver lost deterministic current-snapshot selection authority."
    Assert-Condition -Condition (
            $transition.Contains("sameConstructiveContext(") -and
            $transition.Contains("getSourcePairIdentity().equals(") -and
            $transition.Contains("getConstructiveIntersectionLineage().equals(") -and
            $transition.Contains("getTopologyContext().equals(") -and
            $transition.Contains("getTargetFamily() ==") -and
            $transition.Contains("getBranchSnapshotKey().equals(") -and
            $transition.Contains("getResolvedValidComponentKey().equals(") -and
            $transition.Contains("getCurrentRootGerm().equals(") -and
            $transition.Contains("boundedMappedSemanticStep(") -and
            $transition.Contains("span / subdivisions") -and
            $transition.Contains("priorsByCurrent") -and
            $transition.Contains("currentsByPrior") -and
            $transition.Contains("priors.size() > 1") -and
            $transition.Contains("descendants.size() == 1") -and
            $transition.Contains("getMaximumContinuationComparisons()") -and
            $transition.Contains("exhausted.put(root, Boolean.TRUE)") -and
            $transition.Contains("new Transition(Map.of(), exhausted, comparisons, true)") -and
            $transition.Contains("isBudgetExhausted()") -and
            $transition.Contains("component.equals(branch.getDeclaredDriverDomain())") -and
            $transition.Contains("component.equals(definition.getProvider().getDeclaredDomain())")) `
        -Message "R4 lost the bounded one-to-one continuity/topology diagnostic relation."
    Assert-Condition -Condition (
            -not $resolver.Contains("getEvaluatedPoint().getX") -and
            -not $resolver.Contains("getEvaluatedPoint().getY") -and
            -not $resolver.Contains("screenTo") -and
            -not $resolver.Contains("nearest") -and
            $resolver.Contains(
                "ordered.sort(Comparator.comparingDouble(root -> root.phase.lower))") -and
            -not $resolver.Contains("Comparator.comparing(root -> root.solution") -and
            -not $transition.Contains("getEvaluatedPoint().getX") -and
            -not $transition.Contains("getEvaluatedPoint().getY") -and
            -not $transition.Contains("screenTo") -and
            -not $transition.Contains("nearest") -and
            -not $transition.Contains("Comparator") -and
            -not $transition.Contains(".sorted(") -and
            -not $transition.Contains("indexOf(") -and
            $resolver.Contains(
                "List<LocusIntersectionSolution2D> canonicalParents") -and
            $resolver.Contains(
                ".sorted(Comparator.comparing(root -> root.getIdentity().getRootToken()))") -and
            -not $selector.Contains("getEvaluatedPoint(") -and
            -not $selector.Contains("getSemanticParameter(") -and
            -not $selector.Contains("solutionIndex") -and
            -not $selector.Contains("screenTo") -and
            -not $selector.Contains("nearest")) `
        -Message "R4 selection must use only intrinsic oriented phase order, never Cartesian, viewport, nearest-root or solver/list order."
    Assert-Condition -Condition (
            $ledger.Contains('private static final String FORMAT_VERSION = "3";') -and
            $ledger.Contains(
                'private static final String PREVIOUS_FORMAT_VERSION = "2";') -and
            $ledger.Contains(
                'private static final String LEGACY_FORMAT_VERSION = "1";') -and
            $ledger.Contains("g9u0-r4/ledger-current-root/v2/") -and
            $ledger.Contains("resolveCurrentRoot(") -and
            $ledger.Contains("IntersectionRootDeterministicSelector2D selector") -and
            $ledger.Contains("uniqueStartingAllocation(") -and
            $ledger.Contains("uniqueLegacySingletonAllocation(") -and
            $ledger.Contains("allowLegacySingletonBinding") -and
            $ledger.Contains("withCurrentRootBinding(") -and
            $ledger.Contains("encodeSnapshot(current, FORMAT_VERSION)") -and
            $ledger.Contains("decodeSnapshot(fields[2], importedVersion)") -and
            $ledger.Contains("hasDeterministicBindingFields(version) ? 10 : 8") -and
            $ledger.Contains("FORMAT_VERSION.equals(version)") -and
            $ledger.Contains("PREVIOUS_FORMAT_VERSION.equals(version)") -and
            $ledger.Contains("&& parsedSelector.hasIntrinsicPhase()") -and
            $ledger.Contains(
                '"Ledger v2 cannot contain intrinsic phase selectors"') -and
            $ledger.Contains("if (selector.hasIntrinsicPhase()) {") -and
            $ledger.Contains("entry.currentRootBinding.get().selector") -and
            $ledger.Contains("LEGACY_PUBLIC_SINGLETON_PREFIX") -and
            $allocation.Contains("private final String rootToken;") -and
            $allocation.Contains("private final String continuationKey;") -and
            $allocation.Contains("private final boolean reused;")) `
        -Message "R4 ledger lost canonical format-v3 authority or strict format-v2/v1 import compatibility."
    Assert-Condition -Condition (
            $selector.Contains(
                "g9u0-r4/deterministic-current-root/v1/") -and
            $selector.Contains(
                "g9u0-r4/deterministic-current-root/v2/") -and
            $selector.Contains("String componentLineage, String currentRootGerm") -and
            $selector.Contains("ofIntrinsicPhase(") -and
            $selector.Contains("collisionCardinality") -and
            $selector.Contains("intrinsicRank") -and
            $selector.Contains("Orientation orientation") -and
            $selector.Contains("PERIODIC_FUNDAMENTAL_INTERVAL") -and
            $selector.Contains(
                "isCurrentPublicRootGermForComponent(germ, component)") -and
            $selector.Contains("framed(component) + framed(germ)") -and
            $selector.Contains(
                "Frame component = frame(value, BASE_PREFIX.length())") -and
            $selector.Contains("Frame germ = frame(value, component.nextOffset)") -and
            $selector.Contains("getIntrinsicPhaseRank()") -and
            $selector.Contains("getCollisionCardinality()") -and
            $selector.Contains("getPhaseOrientation()") -and
            $selector.Contains("isPeriodicPhase()") -and
            $publicCapability.Contains(
                "g9u0-r4/current-transverse-root-germ/v1/") -and
            $publicCapability.Contains("withCurrentRootGerm(") -and
            $publicCapability.Contains("isCurrentPublicRootGerm(") -and
            $publicCapability.Contains("evaluateContact(") -and
            $revisionEvidence.Contains("getCurrentRootGerm()") -and
            $solver.Contains("establishesDurableCandidateIdentity(") -and
            $solver.Contains("nextRevisionLocalHandle(") -and
            $tokenSource.Contains("establishesDurableCandidateIdentity(") -and
            $tokenSource.Contains("return false;") -and
            $metadata.Contains("DETERMINISTIC_SELECTION_ESTABLISHED") -and
            $result.Contains(
                "IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED") -and
            -not $publicCapability.Contains("getEvaluatedPoint().getX") -and
            -not $publicCapability.Contains("getEvaluatedPoint().getY")) `
        -Message "R4 base-germ plus versioned intrinsic phase/rank selector boundary drifted."
    Assert-Condition -Condition (
            $solver.Contains(
                "java.util.LinkedHashSet<String> unresolvedComponents") -and
            $solver.Contains(
                "unresolvedComponents.add(candidate.getComponentKey())") -and
            $solver.Contains("List.copyOf(unresolvedComponents)") -and
            $result.Contains(
                "private final List<String> unresolvedCandidateComponentKeys;") -and
            $result.Contains("getUnresolvedCandidateComponentKeys()") -and
            $result.Contains("!unresolvedCandidateComponentKeys.contains(") -and
            $resolver.Contains(
                "current.getUnresolvedCandidateComponentKeys()") -and
            $resolver.Contains(
                "!result.getUnresolvedCandidateComponentKeys().contains(") -and
            $localMatrixMethod.Contains("List.of(unrelatedComponent)") -and
            $localMatrixMethod.Contains(
                "assertTrue(mixedResolved.findPointAdmissibleSolution(") -and
            $localMatrixMethod.Contains("List.of(establishedComponent)") -and
            $localMatrixMethod.Contains(
                "assertFalse(collidingResolved.findPointAdmissibleSolution(")) `
        -Message "R4 unresolved candidates must propagate and fail closed only on their semantic component."
    Assert-Condition -Condition (
            $targets.Contains(
                "contactOrientation = family == TargetFamily.RAY ? 1") -and
            $targets.Contains(": canonicalLineContactOrientation(a, b);") -and
            $targets.Contains(
                "static double canonicalLineContactOrientation(double a, double b)") -and
            $targets.Contains(
                "static double canonicalPolynomialContactOrientation(") -and
            $targets.Contains(
                "A ray does not use this helper because its line orientation encodes") -and
            @([regex]::Matches($targets,
                    'canonicalLineContactOrientation\(a, b\)')).Count -eq 1 -and
            -not $targets.Contains(
                "TargetFamily.RAY ? canonicalLineContactOrientation") -and
            $conicTarget.Contains(
                "this.contactOrientation = canonicalContactOrientation(conic);") -and
            $conicTarget.Contains(
                "double indicator = contactOrientation *") -and
            $conicTarget.Contains("family == TargetFamily.PARABOLA") -and
            $conicTarget.Contains("return centerLevel < 0 ? 1 : -1;") -and
            $implicitTarget.Contains(
                ".canonicalPolynomialContactOrientation(coefficients);") -and
            $implicitTarget.Contains(
                "double indicator = contactOrientation *")) `
        -Message "R4 target contact orientation lost canonical representation handling or altered ray direction authority."
    Assert-Condition -Condition (
            $stableTargetMethod.Contains(
                'targets.put("r4Ray", TargetFamily.RAY);') -and
            -not $stableTargetMethod.Contains(
                'requireLookup("r4Ray")') -and
            $stableTargetMethod.Contains(
                "line.setCoords(-line.getX(), -line.getY(), -line.getZ());") -and
            $stableTargetMethod.Contains(
                "assertEquivalentTargetRepresentation(lineRich") -and
            $stableTargetMethod.Contains(
                "conic.setCoeffs(-matrix[0], -2 * matrix[3], -matrix[1],") -and
            $stableTargetMethod.Contains(
                "assertEquivalentTargetRepresentation(conicRich") -and
            $stableTargetMethod.Contains(
                "implicit.setCoeff(negativeCoefficients);") -and
            $stableTargetMethod.Contains(
                "assertEquivalentTargetRepresentation(implicitRich") -and
            $equivalentRepresentationHelper.Contains(
                "findExactPointAdmissibleSolution(expected.getKey())") -and
            $equivalentRepresentationHelper.Contains(
                "assertTrue(materialized.isDefined());") -and
            $equivalentRepresentationHelper.Contains(
                "assertEquals(selectedToken, pointAlgorithm.getSelectedRootToken());") -and
            $equivalentRepresentationHelper.Contains(
                "assertEquals(selectedToken, pointAlgorithm.getEffectiveRootToken());")) `
        -Message "R4 supported-target regression lost end-to-end equivalent-representation and exact-token checks."
    Assert-Condition -Condition (
            $extendedCapability.Contains("completePeriodicSeam") -and
            $extendedCapability.Contains("hasCompletePeriodicSeam(") -and
            $extendedCapability.Contains("component.equals(branch.getDeclaredDriverDomain())") -and
            $pairSolver.Contains("candidate.getContinuationKey()")) `
        -Message "R4 periodic-seam or pair revision-evidence boundary drifted."
    Assert-Condition -Condition (
            $resolver.Contains("failClosedObservedTopologyTransition(") -and
            $resolver.Contains("LineageEventKind.SPLIT_CANDIDATE") -and
            $resolver.Contains("LineageEventKind.MERGE_CANDIDATE") -and
            $continuation.Contains("IdentityStatus.AMBIGUOUS_CONTINUATION") -and
            $result.Contains("event == LineageEventKind.APPEARED") -and
            $result.Contains("event == LineageEventKind.UNCHANGED") -and
            $result.Contains("lineageAdmissible")) `
        -Message "R4 merge/split final admissibility guard drifted."
    Assert-Condition -Condition (
            $address.Contains("targetContractSignature(") -and
            $address.Contains("not token identity or a cross-revision equality") -and
            $ledger.Contains("sameTargetContract(") -and
            $resolver.Contains("continuationContract(definition,") -and
            $solver.Contains("IntersectionRootAddressProof2D.targetContractSignature(")) `
        -Message "R4 revision-address evidence escaped its non-identity contract."
    Assert-Condition -Condition (
            $menuDefault.Contains(
                "LocusV2.Results.Value.DETERMINISTIC_SELECTION_ESTABLISHED=Deterministic selection established") -and
            $menuEnglish.Contains(
                "LocusV2.Results.Value.DETERMINISTIC_SELECTION_ESTABLISHED=Deterministic selection established") -and
            $menuSpanish.Contains(
                "LocusV2.Results.Value.DETERMINISTIC_SELECTION_ESTABLISHED=Selección determinista establecida")) `
        -Message "R4 deterministic-selection diagnostic localization drifted."

    $candidatePaths = @(Get-CandidatePaths)
    $candidateSource = @($candidatePaths | Where-Object {
            $_.StartsWith("source/", [StringComparison]::Ordinal)
        })
    Assert-Condition -Condition (
            $candidatePaths.Count -gt 0 -and
            $candidateSource.Count -eq $AllowedCandidateSourcePaths.Count) `
        -Message "R4 candidate inventory is empty or escaped its exact source boundary."
    Assert-ExactSet -Actual $candidateSource `
        -Expected $AllowedCandidateSourcePaths `
        -Description "R4 candidate source boundary"
    $productive = @($candidateSource | Where-Object {
            $_.Contains("/src/main/")
        })
    Assert-ExactSet -Actual $productive `
        -Expected $RequiredProductiveSourcePaths `
        -Description "R4 productive shared-kernel boundary"
}

function Assert-DocumentationContracts {
    $prompt = [IO.File]::ReadAllText((Resolve-RequiredFile $PromptPath))
    $architecture = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $ArchitecturePath))
    $report = [IO.File]::ReadAllText((Resolve-RequiredFile $ReportPath))
    $adr = [IO.File]::ReadAllText((Resolve-RequiredFile $AdrPath))
    $intersectionSpec = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $IntersectionSpecPath))
    [void](Resolve-RequiredFile $VerifierPath)

    $requiredHeadings = @(
        "# Objective", "# Authority and evidence hierarchy", "# Scope",
        "# Explicitly forbidden scope", "# Architectural placement",
        "# Required design/specification",
        "# Geometric invariants and degeneracies",
        "# Compatibility and serialization", "# Required tests and commands",
        "# Required artifacts", "# Stop conditions"
    )
    $actualHeadings = @($prompt -split "`r?`n" | Where-Object {
            $_.StartsWith("# ", [StringComparison]::Ordinal)
        })
    Assert-ExactSet -Actual $actualHeadings -Expected $requiredHeadings `
        -Description "canonical R4 prompt headings"
    foreach ($fragment in @(
            "G9U0-R4", "LOCAL POINT ADMISSIBILITY != GLOBAL COMPLETENESS",
            "NEW_TOPOLOGICAL_SOLUTION", "deterministic semantic selection",
            "current snapshot", "continuity heuristic", "path-independent",
            "current-root germ", "ADR 0017", "intrinsic phase", "rank",
            "canonical format v3", "format v1 and v2", "coordinate",
            "candidate order", "periodic", "orientation",
            "Continuity = OFF", "PENDING AUTHOR RE-REVIEW",
            "IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE",
            $FourSolutionFixtureSha256,
            "AUTO_MATERIALIZATION_FRONTEND_ONLY",
            "G9U0-R5 = DESIGN CANDIDATE", "G9U1 = DESIGNED — NOT AUTHORIZED")) {
        Assert-Condition -Condition ($prompt.Contains($fragment,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "Canonical R4 prompt lacks '$fragment'."
    }
    foreach ($text in @($architecture, $report)) {
        foreach ($fragment in @(
                "G9U0-R4", "selfApproved = false", "authorApproved = false",
                "passClaimed = false",
                "FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION",
                "IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE",
                $FourSolutionFixtureSha256,
                "AUTHOR_APPROVED_DIRECTION", "ADR 0017", "intrinsic phase",
                "canonical format v3", "PENDING AUTHOR RE-REVIEW")) {
            Assert-Condition -Condition ($text.Contains($fragment,
                    [StringComparison]::OrdinalIgnoreCase)) `
                -Message "R4 candidate documentation lacks '$fragment'."
            }
    }
    foreach ($fragment in @(
            "# ADR 0017: Deterministic intrinsic phase/rank",
            "Status: **Accepted**", "author-directed G9U0-R4",
            "verified collision-group cardinality", "format v3",
            "imports v1 and v2", "ADR 0008", "ADR 0009", "ADR 0013",
            "Solver enumeration permutations cannot change",
            "periodic", "orientation")) {
        Assert-Condition -Condition ($adr.Contains($fragment,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "Accepted ADR 0017 lacks '$fragment'."
    }
    foreach ($fragment in @(
            "ADR 0017", "complete enriched selector",
            "intrinsic", "phase", "rank", "verified collision",
            "format v3", "formats v1 and v2", "global completeness",
            "Solver discovery order", "periodic", "orientation")) {
        Assert-Condition -Condition ($intersectionSpec.Contains($fragment,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "Normative R4 intersection spec lacks '$fragment'."
    }
    foreach ($stale in @(
            "R4 does not introduce semantic rank",
            "this candidate does not authorize either",
            "REPEATED_SELECTOR_FAIL_CLOSED_PENDING_AUTHOR_DECISION",
            "PENDING_R4_AUTHOR_DECISION")) {
        Assert-Condition -Condition (-not $intersectionSpec.Contains($stale,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "Normative R4 intersection spec retains stale text '$stale'."
    }
    Assert-Condition -Condition (
            -not $prompt.Contains("G9U0-R4 = PASS — AUTHOR APPROVED") -and
            -not $report.Contains("G9U0-R4 = PASS — AUTHOR APPROVED") -and
            $report.Contains("PENDING AUTHOR RE-REVIEW") -and
            $report.Contains("implementationAuthorized = false")) `
        -Message "R4 documentation prematurely claims approval or validation."

    $candidatePaths = Get-CandidatePaths
    Assert-Condition -Condition (@($candidatePaths | Where-Object {
                $_.StartsWith("artifacts/", [StringComparison]::OrdinalIgnoreCase)
            }).Count -eq 0) `
        -Message "Generated artifacts must not be tracked as R4 candidate paths."
    Assert-Condition -Condition (@($candidatePaths | Where-Object {
                $_.StartsWith("source/web/", [StringComparison]::OrdinalIgnoreCase) -or
                $_.StartsWith("packaging/", [StringComparison]::OrdinalIgnoreCase) -or
                $_.StartsWith("geocedg/resources/", [StringComparison]::OrdinalIgnoreCase)
            }).Count -eq 0) `
        -Message "R4 escaped its shared-kernel/test/planning boundary."

    $historicalVerifier = [IO.File]::ReadAllText((Resolve-RequiredFile `
        "tools/agent/verify-g9u0-locus-v2-public-surface.ps1"))
    foreach ($fragment in @(
            '$G9U0TagName = "geocedg-g9u0-pass"',
            '$SealedSourceCommit', 'Get-AuthoritySourceText',
            'merge-base --is-ancestor', '$G9U0PromotionCommit HEAD',
            '& git -C $RepositoryRoot show $object')) {
        Assert-Condition -Condition ($historicalVerifier.Contains($fragment)) `
            -Message "Historical G9U0 sealed-descendant seam lacks '$fragment'."
    }

    $historicalR3Verifier = [IO.File]::ReadAllText((Resolve-RequiredFile `
        $HistoricalR3VerifierPath))
    foreach ($fragment in @(
            '$PassTagName = "geocedg-g9u0-r3-pass"',
            '$PassTagObject = "1c1be8ebb58be9ad4c4e7242bc56105f9f310068"',
            '$PassCommitSha = "ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b"',
            '$script:R3BoundaryMode = "TAGGED_DESCENDANT"',
            'Get-AuthorityBytes', 'Read-JsonDocument',
            'Get-CanonicalLfSha256', 'merge-base --is-ancestor',
            'Get-GitBlobBytes')) {
        Assert-Condition -Condition ($historicalR3Verifier.Contains($fragment)) `
            -Message "Historical G9U0-R3 sealed-descendant seam lacks '$fragment'."
    }

    $composedVerifier = [IO.File]::ReadAllText((Resolve-RequiredFile `
        "tools/agent/verify.ps1"))
    foreach ($fragment in @(
            '$g9u0R3PassCommit = "ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b"',
            '$g9u0R3AuthorityPaths', 'git -C $RepositoryRoot cat-file -e',
            'git -C $RepositoryRoot show')) {
        Assert-Condition -Condition ($composedVerifier.Contains($fragment)) `
            -Message "Composed G9U0-R3 sealed-authority seam lacks '$fragment'."
    }
    $r3Invocation = $composedVerifier.IndexOf(
        "& `$G9U0R3PublicLocusUiVerifier",
        [StringComparison]::Ordinal)
    $r4Invocation = $composedVerifier.IndexOf(
        "& `$G9U0R4IntersectionAdmissibilityVerifier",
        [StringComparison]::Ordinal)
    Assert-Condition -Condition (
            $composedVerifier.Contains(
                '"verify-g9u0-r4-intersection-admissibility-continuation.ps1"') -and
            $composedVerifier.Contains("`$g9u0R4IntegrationArtifacts") -and
            $composedVerifier.Contains(
                '"docs\adr\0017-deterministic-intersection-phase-rank-identity.md"') -and
            $composedVerifier.Contains(
                '"geocedg\specs\locus\locus-v2-intersections.md"') -and
            $composedVerifier.Contains(
                '"source\shared\common-jre\src\test\resources\org\geocedg\common\locus\g9u0-r4\fourSolutions.cedg"') -and
            $composedVerifier.Contains("Incomplete G9U0-R4 candidate integration") -and
            $r3Invocation -ge 0 -and $r4Invocation -gt $r3Invocation) `
        -Message "Composed verification must run the complete R4 gate after R3."
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [string]$LogFileName
    )

    if (-not $AllowToolchainDownload) {
        $Arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogFileName
    Write-Host "`n==> $Description"
    Write-Host "    log: $logPath"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "$Description failed with exit code $exitCode."
}

function Get-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [string[]]$ExpectedMethods
    )

    $resultRoot = if ($ClassName -in $DesktopTestClasses) {
        "source/desktop/desktop/build/test-results/test"
    } else {
        "source/shared/common-jre/build/test-results/test"
    }
    $relativePath = "$resultRoot/TEST-$ClassName.xml"
    [xml]$result = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile $relativePath)
    $suite = $result.testsuite
    $methods = @($suite.testcase | ForEach-Object {
            ([string]$_.name) -replace '\(.*\)$', ''
        })
    Assert-ExactSet -Actual $methods -Expected $ExpectedMethods `
        -Description "$ClassName test methods"
    Assert-Condition -Condition (
            [int]$suite.tests -eq $ExpectedMethods.Count -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "$ClassName test result is not clean."
    return [ordered]@{
        class = $ClassName
        tests = [int]$suite.tests
        failures = [int]$suite.failures
        errors = [int]$suite.errors
        skipped = [int]$suite.skipped
        methods = @($methods | Sort-Object -CaseSensitive)
    }
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $errors = @(Select-String -LiteralPath (
            Resolve-RequiredFile $RelativePath) -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle errors."
}

function Write-CanonicalSummary {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object[]]$TestResults
    )

    $candidatePaths = @(Get-CandidatePaths)
    $candidateSource = @($candidatePaths | Where-Object {
            $_.StartsWith("source/", [StringComparison]::Ordinal)
        })
    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9U0-R4"
        status = $CandidateStatus
        historicalAuthorSmoke = $Scenarios.historicalAuthorSmoke
        historicalAuthorSmoke2 = $Scenarios.historicalAuthorSmoke2
        deterministicPolicy = $Scenarios.deterministicPolicy
        fourSolutionIdentityDisposition =
            $Scenarios.fourSolutionIdentityDisposition
        materializationPolicyStatus = $Scenarios.materializationPolicyStatus
        adaptiveCertificationStatus = $Scenarios.adaptiveCertificationStatus
        implementation = [ordered]@{
            started = [bool]$Scenarios.implementation.started
            selfApproved = [bool]$Scenarios.implementation.selfApproved
            authorApproved = [bool]$Scenarios.implementation.authorApproved
            passClaimed = [bool]$Scenarios.implementation.passClaimed
        }
        authorReview = $Scenarios.authorReview.status
        entrySha = $EntrySha
        r3PassTagObject = $R3PassTagObject
        r3PassCommit = $R3PassCommit
        fixture = [ordered]@{
            path = $FixturePath
            lengthBytes = $FixtureLength
            sha256 = $FixtureSha256
        }
        fourSolutionFixture = [ordered]@{
            path = $FourSolutionFixturePath
            authorArtifact = $Scenarios.fourSolutionFixture.authorArtifact
            lengthBytes = $FourSolutionFixtureLength
            sha256 = $FourSolutionFixtureSha256
            disposition = $Scenarios.fourSolutionFixture.disposition
        }
        candidateInventory = [ordered]@{
            totalPaths = $candidatePaths.Count
            sourcePaths = $candidateSource.Count
            focusedMethodCount = $ExpectedFocusedCount
        }
        automatedScenarioIds = @($Scenarios.automatedGroups |
            ForEach-Object { $_.cases } | ForEach-Object { $_.id } |
            Sort-Object -CaseSensitive)
        requiredCoverageIds = @($Scenarios.requiredCoverage |
            ForEach-Object { $_.id } | Sort-Object -CaseSensitive)
        focusedR4Tests = $ExpectedFocusedCount
        testResults = @($TestResults | Sort-Object { $_.class })
        deterministicSourceHashes = @($DeterministicSourcePaths |
            Sort-Object -CaseSensitive | ForEach-Object {
                [ordered]@{ path = $_; sha256 = Get-BinarySha256 $_ }
            })
        contract = [ordered]@{
            localAdmissibilityRequiresGlobalCompleteness = $false
            initialIdentity =
                "FRESH_OPAQUE_ALLOCATION_OR_EXACT_LEGACY_SINGLETON_BINDING"
            crossRevisionIdentity =
                "UNIQUE_INTRINSIC_CURRENT_SNAPSHOT_SELECTOR_AND_EXACT_LEDGER_ALLOCATION"
            deterministicSemanticSelectionAuthoritative = $true
            continuityEvidenceIsSubordinate = $true
            pathIndependentCurrentSelection = $true
            identityByCoordinateOrderOrProximity = $false
            rootExistenceIsDeterministicIdentity = $false
            deterministicIdentityIsNumericalCertification = $false
            materializationPolicyMayOverrideIdentityAmbiguity = $false
            adaptiveCertificationMayResolveSelectorCollision = $false
            repeatedBaseSelectorUsesIntrinsicPhaseRank = $true
            intrinsicPhaseUsesOnlyCurrentVerifiedCollisionGroup = $true
            intrinsicRankUsesSolverOrListOrder = $false
            collisionCardinalityChangeInvalidatesTokens = $true
            orientationReversalRequiresExplicitMap = $true
            periodicRankRotationInvalidatesInsteadOfRetargeting = $true
            currentRootGermIsDurableIdentity = $false
            currentRootGermIsContinuationCertificate = $false
            boundedSemanticRootTubeIsIdentity = $false
            unboundedParameterProximityContinuation = $false
            statefulOneToOneTransitionRequiredForIdentity = $false
            statefulTransitionRetainedForTopologyDiagnostics = $true
            tokenLedgerFormatVersion = 3
            previousTokenLedgerFormatVersionAccepted = 2
            legacyTokenLedgerFormatVersionAccepted = 1
            legacySingletonTokenMaterialPreserved = $true
            automaticPersistentPoints = 0
            candidateMarkers = 0
            r5ProductiveImplementation = $false
            g9u1ContinuityPolicy =
                "CONTINUITY_OFF_PRODUCT_INVARIANT_PLANNING_ONLY"
            g9u1Executed = $false
            authorApproved = $false
            passClaimed = $false
        }
    }
    $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
    [void](New-Item -ItemType Directory `
        -Path (Split-Path -Parent $CanonicalSummaryPath) -Force)
    [IO.File]::WriteAllText($CanonicalSummaryPath, $json,
        [Text.UTF8Encoding]::new($false))
    $summaryHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
        $CanonicalSummaryPath).Hash.ToLowerInvariant()
    Assert-Condition -Condition ($summaryHash -ceq
            $Scenarios.testExecution.canonicalSummarySha256) `
        -Message "Canonical R4 summary does not match frozen scenario evidence."
    Write-Host "Canonical candidate summary: $CanonicalSummaryPath"
    Write-Host "Canonical candidate summary SHA-256: $summaryHash"

    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "Comparison summary is missing."
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message "Deterministic R4 candidate summary mismatch."
        Write-Host "Deterministic candidate-summary comparison: MATCH"
    }
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null
[string]$FailureContext = $null

try {
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    foreach ($path in @(
            $PromptPath, $ScenarioPath, $ArchitecturePath, $ReportPath,
            $AdrPath, $IntersectionSpecPath, $VerifierPath,
            $FixturePath, $FourSolutionFixturePath,
            $AlgoPath, $AddressProofPath,
            $AllocationPath, $ExtendedCapabilityPath, $RevisionEvidencePath,
            $MetadataPath, $TokenSourcePath, $SelectorPath, $ResolverPath,
            $TransitionPath, $SolverPath,
            $PairSolverPath, $LedgerPath,
            $PublicCapabilityPath, $ContinuationPath, $ResultPath,
            $TargetsPath, $ConicTargetPath, $ImplicitTargetPath,
            $AuthorTestPath, $LedgerTestPath, $HistoricalTokenTestPath,
            $PersistenceCompatibilityTestPath, $R3InspectorTestPath,
            $NativeArchiveTestPath, $MenuDefaultPath, $MenuEnglishPath,
            $MenuSpanishPath)) {
        [void](Resolve-RequiredFile $path)
    }

    Assert-EntryAuthority
    Assert-FixtureAuthority
    $scenarios = Read-JsonDocument $ScenarioPath
    Assert-ScenarioContract -Scenarios $scenarios
    Assert-ProductStaticContracts
    Assert-DocumentationContracts

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9U0-R4."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9U0-R4."

    if ($SkipBuild) {
        Write-Host "G9U0-R4 static candidate verification completed."
        Write-Host "G9U0-R4 remains PENDING AUTHOR RE-REVIEW; no PASS claimed."
    } else {
        $GeneratedState = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9u0-r4"
        $testArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($class in @($ExpectedTestMethods.Keys | Where-Object {
                    $_ -notin $DesktopTestClasses
                })) {
            $testArguments += @("--tests", $class)
        }
        $testArguments += @(
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $testArguments `
            -Description "G9U0-R4 focused common-jre tests" `
            -LogFileName "g9u0-r4-focused-common-jre-gradle.log"

        $desktopTestArguments = @(
            ":desktop:desktop:test"
        )
        foreach ($class in $DesktopTestClasses) {
            $desktopTestArguments += @("--tests", $class)
        }
        $desktopTestArguments += @(
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $desktopTestArguments `
            -Description "G9U0-R4 focused native Desktop archive test" `
            -LogFileName "g9u0-r4-focused-desktop-gradle.log"

        $checkstyleArguments = @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $checkstyleArguments `
            -Description "G9U0-R4 affected-module Checkstyle" `
            -LogFileName "g9u0-r4-checkstyle-gradle.log"

        $results = @()
        foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
            $results += Get-TestResult -ClassName $entry.Key `
                -ExpectedMethods @($entry.Value)
        }
        $total = (@($results | ForEach-Object { $_.tests }) |
            Measure-Object -Sum).Sum
        Assert-Condition -Condition ($total -eq $ExpectedFocusedCount) `
            -Message ("R4 focused JUnit count drifted from {0}." -f
                $ExpectedFocusedCount)
        foreach ($path in @(
                "source/shared/common/build/reports/checkstyle/main.xml",
                "source/shared/common-jre/build/reports/checkstyle/test.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $path
        }
        Write-CanonicalSummary -Scenarios $scenarios -TestResults $results
        Write-Host (("G9U0-R4 focused candidate result: {0}/{0} JUnit, " +
            "Checkstyle clean.") -f $ExpectedFocusedCount)
        Write-Host "G9U0-R4 remains PENDING AUTHOR RE-REVIEW; no PASS claimed."
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9U0-R4 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during R4 verification.`n" +
                    "Before:`n$InitialStatus`nAfter:`n$finalStatus")
            }
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
            $FailureContext = $_.ScriptStackTrace
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup/status failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    $failureMessage = $Failure.Message
    if (-not [string]::IsNullOrWhiteSpace($FailureContext)) {
        $failureMessage += "`n$FailureContext"
    }
    Write-Error $failureMessage
    exit 1
}

exit 0

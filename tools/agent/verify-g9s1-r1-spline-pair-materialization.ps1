#requires -Version 7.2
[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [ValidateSet("AUTO", "PRECOMMIT_CANDIDATE", "COMMITTED_CANDIDATE", "AUTHOR_CLOSEOUT", "PUBLISHED_REGRESSION")]
    [string]$LifecycleMode = "AUTO",
    [string]$ReviewedTechnicalCommit,
    [string]$CloseoutRecordPath,
    [string]$TechnicalEvidenceBundleDirectory,
    [string]$TechnicalEvidenceBundleSha256,
    [switch]$AuthorCloseoutOnly,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-g9s1-r1")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "../..")).Path
$EntrySha = "109f077fc5e2a40bcde45d3271eb928ee66fdfcc"
$ExpectedBranch = "codex/g9s1-r1-spline-pair-materialization"
$ImplementationCommit = "f761758bd664504057413539b9729ba444c904c1"
$LifecyclePolicyPath = "geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json"
$LifecycleHelperPath = "tools/agent/phase-lifecycle.ps1"
$DefaultCloseoutRecordPath = "geocedg/validation/g9s1-r1/g9s1-r1-author-closeout.json"
$PublishedRegressionAuthorityPath = "geocedg/validation/operations/g9s1-r1-published-regression-authority.json"
$PublishedReviewedTechnicalCommit = "a38d4fcde846fc97c51abc8d958de6998302c436"
$PublishedCloseoutCommit = "af459d856f1cdc384805f3035203acce8e6f6104"
$PromptPath = ".github/prompts/tasks/g9s1-r1-spline-pair-intersection-materialization.prompt.md"
$AdrPath = "docs/adr/0021-spline-pair-singleton-germ-materialization.md"
$SpecPath = "geocedg/specs/curves/spline-v2-pair-materialization.md"
$MatrixPath = "docs/validation/g9s1_r1_spline_pair_materialization_validation_matrix.md"
$ScenarioPath = "geocedg/validation/g9s1-r1/g9s1-r1-spline-pair-materialization-scenarios.json"
$EvidencePath = "geocedg/validation/g9s1-r1/g9s1-r1-spline-pair-materialization-evidence.json"
$ManifestPath = "geocedg/validation/g9s1-r1/g9s1-r1-evidence.sha256"
$D2EvidencePath = "geocedg/validation/g9s1-r1/g9s1-r1-d2-design-evidence.json"
$VerifierPath = "tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1"
$OpenRisk = "G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP"
$ProtectedPlanningCommit = "00982e7e148a634cd57ed928f322774df267d5e3"
$ProtectedPlanningBranch = "feature/g9u1-construction-workspace-planning-after-r6"
$RequiredHistoricalTags = [ordered]@{
    "geocedg-g9u0-r4-pass" = @("0f9b303057b00d23722ad1f9d3594b4609d668a7", "63c291464111a5bcdbca488d6639662e46c389c4")
    "geocedg-g9u0-r5-pass" = @("3712595fe2b168ba494379b6b3f0051e4122cfae", "5952dfdbd238e71e598f4d2ca92c3e03437df41c")
    "geocedg-g9s1-pass" = @("ece0ca6f00299d3347e57fac38b7a28cade28644", "de33f3a80102adb051aaa7547a72b7e97409c58c")
    "geocedg-g9u0-r6-pass" = @("2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e", "3942af594e4507e479f2c75019cef62e3d9fea6f")
}
$RequiredTestClasses = @(
    "org.geocedg.common.locus.G9S1R1SplinePairMaterializationTest",
    "org.geocedg.common.locus.G9S1R1NativeStructuralPairTest",
    "org.geocedg.common.locus.G9S1R1StructuralSplineContinuityTest",
    "org.geocedg.common.locus.G9S1R1StructuralSplineLifecycleTest",
    "org.geocedg.desktop.G9S1R1NativeArchivePersistenceTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1PairTokenLedgerTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1SplinePairIntervalCertificationTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1StructuralImplicitCertificationTest",
    "org.geocedg.common.kernel.spline.G9S1R1SplinePrecisionAdmissionTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1D2PairSheetContractTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1PairAtlasCharacterizationTest",
    "org.geocedg.common.kernel.locus.intersection.G9S1R1PairSelectorCharacterizationTest",
    "org.geocedg.common.locus.G9S1R1SplineAtlasHostCharacterizationTest",
    "org.geocedg.common.kernel.locus.intersection.PiecewisePolynomialPairIntersectionCapability2DTest"
)
$RequiredScenarioIds = @(
    1..8 | ForEach-Object { "R1-N{0:D2}" -f $_ }
) + @(1..12 | ForEach-Object { "R1-I{0:D2}" -f $_ }) +
    @(1..6 | ForEach-Object { "R1-M{0:D2}" -f $_ }) +
    @(1..10 | ForEach-Object { "R1-L{0:D2}" -f $_ }) +
    @(1..8 | ForEach-Object { "R1-P{0:D2}" -f $_ }) +
    @(1..4 | ForEach-Object { "R1-B{0:D2}" -f $_ }) +
    @(1..4 | ForEach-Object { "R1-V{0:D2}" -f $_ }) +
    @(1..20 | ForEach-Object { "R1-S{0:D2}" -f $_ }) +
    @(1..10 | ForEach-Object { "R1-C{0:D2}" -f $_ }) +
    @(1..16 | ForEach-Object { "R1-A{0:D2}" -f $_ }) +
    @(1..15 | ForEach-Object { "R1-U{0:D2}" -f $_ })
# Independent semantic anchors prevent joint deletion from source and editable
# scenario JSON from silently narrowing acceptance. Additional methods remain
# mandatory through exact source/scenario/live-JUnit equality below.
$MandatoryMethodAnchors = [ordered]@{
    "org.geocedg.common.kernel.spline.G9S1R1SplinePrecisionAdmissionTest" = @(
        "ordinaryCubicUsesBinary64WithoutPrecisionEscalation",
        "historicalQuinticPrecisionAndCanonicalExpansionRepeatExactly",
        "oneFallbackLevelFailsClosedWithoutSuccessiveStability",
        "arithmeticWorkCapRejectsBeforeReturningAModel",
        "precisionPolicyMaximumsCannotBeExpanded",
        "genuinelySingularLinearSystemIsRejectedAtEveryPrecision",
        "originalBackwardErrorRejectsAnIncorrectSolution",
        "precisionFallbackCannotBypassInconsistentOriginalEquation",
        "invalidSemanticInputsNeverEnterPrecisionFallback",
        "highSupportedDegreeAndPeriodicModelsReportArithmeticRoutes"
    )
    "org.geocedg.common.locus.G9S1R1StructuralSplineContinuityTest" = @(
        "historicalIndependentSpansHaveExactNonzeroNativeKnotDefect",
        "nativeBlockerHasStructuralJetsThroughCubicOrderMinusOne",
        "asymmetricCubicKnotsAreStructuralRatherThanToleranceGlued",
        "intermediateDegreesRetainAllStructuralJetsAndOriginalBoundaryRows",
        "nontrivialHighDegreeCurvesPreserveClassicFamilyGeometryAndBoundaryRows",
        "defaultClassicFloatChordKnotsAreNotTheNativeDoubleKnotAuthority",
        "admittedNonlinearDegreeSevenMatchesIndependentNativeEquationOracle",
        "previouslyRejectedOpenDegreeTwelveInputsRemainExplicitNumericalRejections",
        "fullRankDyadicDegreeTwelveInputStillRequiresNumericalAdmission",
        "historicallyAdmittedTwentyFivePointQuinticRetainsNumericalAdmission",
        "degreeTwelveNativeModelRetainsElevenExactKnotJets",
        "maximumPointCountUsesReducedBoundedSystem",
        "periodicCubicSeamAndInteriorJetsAreExact",
        "periodicIntermediateAndDegreeTwelveSeamsAreStructural",
        "roundedNormalizedKnotsKeepExactStructuralMeaning",
        "finiteSmallLargeAndMixedSignCoordinatesRetainStructuralJets",
        "differentCoordinateScalesDoNotBecomeContinuityTolerance",
        "exactStructuralNumeratorsAreDefensiveAndSignatureIsVersioned",
        "approximateClosedEndpointsAreRejectedWithoutAveraging",
        "signedZeroClosingCoordinatesDoNotBreakExactPeriodicity",
        "existingPointDegreeAndDenseAdmissionPolicyRemainBounded"
    )
    "org.geocedg.common.locus.G9S1R1StructuralSplineLifecycleTest" = @(
        "highPrecisionQuinticRebuildsExactDerivedAuthorityAfterXmlAndUndo",
        "highPrecisionQuinticCopyRemapsIdentityWithoutCopyingNumericalAuthority",
        "palindromicSplineNonminimumGuardsDoNotObstructCrossingResolution",
        "nativeRetracedSplineSingularTrueMinimumStillFailsClosed",
        "commandSchemaRebuildsVersionedModelWithoutPersistingDerivedState",
        "modelRevisionChangesWithoutChangingProviderSourceOrAddressIdentity",
        "invalidSourceRecoveryRetainsInteractionOwnedPointAndExactDirection",
        "compatibleRenamePreservesStructuralModelAndExistingBindings",
        "closureCopyRemapsSourceAndPointButPreservesSemanticDirection",
        "undoRedoRestoresInvalidAndValidSourceWithoutReplacingDurableIdentity",
        "oneSidedR4TokenReactivatesAfterStructuralSourceRecomputation",
        "transformedPartialMetricAndPointRecoverAcrossNegativeAndZeroDilation",
        "periodicInteractionDirectionBitsAndLiftSurviveStructuralModelReopen",
        "approximateHostEndpointEqualityCannotDeclareStructuralPeriodicClosure",
        "exactDistinctEndpointObjectsRetainClosedProviderAndCanonicalOwnership",
        "currentReopenAndDifferentRegularUpdatePathsYieldIdenticalModelSignatures",
        "sameSourceRichOnlyQueryRetainsItsSingleDependencyAcrossReopen",
        "sameSourceRichOnlyClosureCopyRemapsOneActualSourceWithoutPairAllocation",
        "explicitCompatibleNumericRedefinePreservesPairSourceTokenAndPoint",
        "incompatibleSplineReplacementRetiresOldPairAndPointWithoutRetargeting",
        "nativeSplineExplicitReplacementSucceedsWithoutPairConsumers",
        "nativeSplineExplicitReplacementRetiresRichOnlyPairWithoutPointChild",
        "nativeSplineExplicitReplacementRebuildsWithOrdinarySemanticPointOnly",
        "stagedSplineOutputUsesExactSerializationOverlayWithoutPublishingLiveIdentity"
    )
    "org.geocedg.common.locus.G9S1R1NativeStructuralPairTest" = @(
        "structuralSplineRetainsHistoricalImplicitTangencyBarrier",
        "nativePeriodicLoopAndStraightSplineCertifyTwoCurrentSingletonGerms",
        "existingPairChildrenConsumeOnePublishedSnapshotWithoutAnotherSolve",
        "nativePeriodicSeamHasOneOwnerAndRetainsPointsAcrossRegularCrossing",
        "nativeBothSourceInternalKnotRootMaterializesExactlyOnce",
        "nativeOneSourceKnotRootUsesStructuralChartWithoutDuplicatePoint",
        "nativeOnlySecondCallerSourceKnotHasTheSameCertifiedSemanticRoot",
        "nativeDistinctRootsOnOppositeSidesOfOneKnotAreNeverProximityMerged",
        "nativeNearTangencyDepthExhaustionKeepsTwoDistinctRichOnlyPreimages",
        "nativeNearKnotMotionCannotRetireCurrentSingletonToken",
        "nativePeriodicOperandsRemainSymmetricAndNegativeTransformHasNewContext",
        "nativePeriodicTangencyDoesNotAcquireTransverseMaterializationAuthority",
        "nativeOverlappingSplineImagesNeverProduceIsolatedPairTokens",
        "nativeRepeatedPeriodicTraversalHasFourRootsButNoSingletonGerm",
        "nativeCollapsedPeriodicImageInvalidatesAndRecoversOnlyExistingPoints"
    )
    "org.geocedg.common.locus.G9S1R1SplinePairMaterializationTest" = @(
        "simpleTransversePairMaterializesWithoutGlobalCompleteness",
        "projectedRankExchangePreservesBothExactTokensAndPoints",
        "identicalFinalDefinitionsResolveIndependentlyOfUpdatePathAndReopen",
        "undoRedoRestoresActiveDormantAndReactivatedGraph",
        "closureCopyRemapsBothSourcesAndRebasesPairToken",
        "dormantPointReactivatesSameSlotWithoutAllocatingGeoPoint",
        "sameGermAmbiguityDoesNotBlockIndependentlyUniqueOppositeGerm",
        "nativeDyadicKnotCrossingKeepsCurrentSingletonSlot",
        "nativeKnotStoredPolynomialJetDefectIsExactNotTolerance",
        "genericLocusPairStaysRichOnlyDespiteSharedInfrastructure"
    )
    "org.geocedg.common.kernel.locus.intersection.G9S1R1StructuralImplicitCertificationTest" = @(
        "simpleImplicitRootHasStructuralExistenceAndUniqueMaterialization",
        "separatedSimpleRootsPreserveBothCurrentTokens",
        "nonzeroTinyDerivativeUsesIntervalSignInsteadOfTangencyEpsilon",
        "exactTangencyRemainsOneEstimatedRichContactWithoutToken",
        "singularImplicitContactRemainsRichWithoutInventedMultiplicity",
        "stationaryDiscoveryProvenanceDoesNotCertifyMultiplicity",
        "exactBoundaryContactIsSeparateFromStationaryDiscoveryAndMultiplicity",
        "threeSimpleRootsRemainDistinctWithoutCoordinateZeroSnapping",
        "squaredTargetRetainsTwoRichContactsAndNoFalseTransverseRoot",
        "floatingArtificialSplitCannotAcquireSimpleRootCertificate",
        "veryCloseDistinctSimpleRootsAreNotMergedByParameterTolerance",
        "certifiedRootsBelowLegacyDeduplicationToleranceRemainDistinct",
        "derivativeIntervalContainingZeroCannotProveTransversality",
        "structuralValueIntervalExcludesAFalseCandidate",
        "boundedWorkExhaustionPublishesNoSimpleCertificate",
        "exactStructuralKnotHasOneCertifiedRootNotTwoSpanCandidates",
        "openSplineIncludedEndpointHasNoArtificialPeriodicChartBarrier",
        "periodicSeamHasOneCanonicalRootAndNoEndpointDuplicate",
        "liftedPeriodicProofUsesOutwardCanonicalEnclosureAndCertifiedDeduplication",
        "translatedSplineCertifiesOriginalCompositionNotExpandedSurrogate",
        "negativeDilationRetainsCertifiedOneSidedRoot",
        "arithmeticUncertaintyCannotTurnSquaredContactIntoSimpleRoot",
        "genericScalarLocusDoesNotAcquireTheStructuralSplineCapability"
    )
    "org.geocedg.common.kernel.locus.intersection.G9S1R1SplinePairIntervalCertificationTest" = @(
        "structuralRationalCoefficientBridgeProvesExactBounds",
        "outwardBasicOperationsContainExactDyadicResults",
        "subnormalUnderflowAndSignedZeroRemainEnclosed",
        "divisionEnclosesRationalValueAndRefusesZeroDivisor",
        "singletonTransverseRootHasIndependentExistenceAndClassProof",
        "oppositeGermsAreIndependentlyUniqueWithoutClaimingGlobalCompleteness",
        "productiveCandidateEnumerationReversalPreservesExactSelectorTokenBindings",
        "syntheticGlobalCompleteDoesNotMaterializeActualMultipleGermClasses",
        "syntheticGlobalIncompleteRetainsActuallyCertifiedLocalPairEligibility",
        "multipleSameGermRootsDoNotBlockCertifiedOppositeSingleton",
        "exactKnotGluePermitsRootProofAcrossBothSpanBoundaries",
        "nonzeroFloatingKnotDefectDoesNotBecomeExactGlue",
        "incompleteCoverageBudgetCannotClaimUniqueClass",
        "nestedSimilaritiesUseOriginalCapturedMapRatherThanFlattenedCoefficients",
        "largeTranslatedFlatteningCannotFabricateAnIntervalCertificate",
        "collapsedImageAndGenericEvaluatorAreNotPromoted",
        "canonicalSourceOrderProducesIdenticalCertificates",
        "exactPeriodicSeamChartHasOneCanonicalRootPerGerm",
        "repeatedPeriodicTraversalRetainsSameGermMultiplicity"
    )
    "org.geocedg.common.kernel.locus.intersection.G9S1R1PairTokenLedgerTest" = @(
        "sourceSwapNormalizesGermAndKeepsDescriptorsAssociated",
        "selectorRoundTripIsStrictAndContainsNoAddressOrChart",
        "pairProofRetainsBothExactAddressBitsAndCanonicalAxes",
        "oppositeGermsReceiveDistinctOpaqueTokensWithV5PairBindings",
        "ordinaryParameterAndProviderDriftNeverReplaceTheSelectorToken",
        "missingCurrentProofMakesClaimDormantAndUniqueRecurrenceReusesIt",
        "pairQuarantineRoundTripIsNotStickyAndNeverBecomesR4PeriodicState",
        "unavailableSnapshotPreservesOnlyClaimsAndDowngradesPairQuarantine",
        "duplicateStagingFailsClosedAndQuarantineCannotContradictStaging",
        "wrongSourceOrBranchContextCannotRetargetRetainedPairBinding",
        "pairSchemaRejectsDownversionedPayloadsMalformedVariantsAndProofs",
        "exactCopyReversesCanonicalAxesAndGermWithoutLosingTokenOwnership",
        "dormantClosureCopyRetainsExactAssociationAndCanReactivateLater",
        "copyWithoutExactTwoSourceAssociationFailsBeforeAllocation",
        "directIncrementalAndDormantHistoriesHaveIdenticalFinalSlotBindings"
    )
    "org.geocedg.desktop.G9S1R1NativeArchivePersistenceTest" = @(
        "nativeCedgActiveDormantAndReactivatedPairPreservesExactOwnership",
        "nativeCedgPairQuarantineRestoresCurrentMultiplicityAndReactivatesSamePoint",
        "nativeCedgPeriodicActiveSeamPreservesBothExactTokenBindings"
    )
}
$AuthorityPaths = @($PromptPath, $AdrPath, $SpecPath, $MatrixPath, $ScenarioPath,
    $EvidencePath, $ManifestPath, $VerifierPath, "tools/agent/verify.ps1",
    "tools/agent/verification-runtime.psm1",
    "docs/adr/0022-structural-spline-continuity.md",
    "docs/architecture/g9s1_r1_structural_spline_continuity.md",
    "docs/research/g9s1_r1_structural_spline_numerics.md")
$LifecycleContext = $null
$SelectedLifecycleMode = $LifecycleMode
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
if ([string]::IsNullOrWhiteSpace($CanonicalSummaryPath)) {
    $CanonicalSummaryPath = Join-Path $LogDirectory "canonical-summary.json"
} else { $CanonicalSummaryPath = [IO.Path]::GetFullPath($CanonicalSummaryPath) }

function Assert-R1 {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Resolve-R1File {
    param([Parameter(Mandatory)] [string]$RelativePath)
    Assert-R1 (-not [IO.Path]::IsPathRooted($RelativePath)) "Expected relative path: $RelativePath"
    $path = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $RelativePath))
    $prefix = $RepositoryRoot.TrimEnd('/', '\') + [IO.Path]::DirectorySeparatorChar
    Assert-R1 ($path.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase)) "Path escapes repository: $RelativePath"
    Assert-R1 (Test-Path -LiteralPath $path -PathType Leaf) "Required R1 file missing: $RelativePath"
    return $path
}

function Get-R1TextHash {
    param([Parameter(Mandatory)] [byte[]]$Bytes)
    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 239 -and $Bytes[1] -eq 187 -and $Bytes[2] -eq 191) { $offset = 3 }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString($Bytes, $offset, $Bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
        [Text.UTF8Encoding]::new($false).GetBytes($canonical))).ToLowerInvariant()
}

function Get-R1FileHash {
    param([Parameter(Mandatory)] [string]$RelativePath)
    return Get-R1TextHash ([IO.File]::ReadAllBytes((Resolve-R1File $RelativePath)))
}

function Get-R1SourceBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)
    $path = Resolve-R1File $RelativePath
    & git -C $RepositoryRoot diff --quiet HEAD -- $RelativePath
    $diffExit = $LASTEXITCODE
    Assert-R1 ($diffExit -in @(0, 1)) "Cannot classify source authority: $RelativePath"
    & git -C $RepositoryRoot ls-files --error-unmatch -- $RelativePath 2>$null | Out-Null
    $tracked = $LASTEXITCODE -eq 0
    if ($diffExit -ne 0 -or -not $tracked) { return ,([IO.File]::ReadAllBytes($path)) }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = "git"
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @("-C", $RepositoryRoot, "cat-file", "blob", "HEAD:$RelativePath")) { [void]$start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    $memory = [IO.MemoryStream]::new()
    try {
        [void]$process.Start()
        $copy = $process.StandardOutput.BaseStream.CopyToAsync($memory)
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [void]$copy.GetAwaiter().GetResult()
        $errorText = $errorTask.GetAwaiter().GetResult()
        Assert-R1 ($process.ExitCode -eq 0) "Git source blob failed: $errorText"
        return ,([byte[]]$memory.ToArray())
    } finally { $memory.Dispose(); $process.Dispose() }
}

function Assert-R1Set {
    param([AllowEmptyCollection()] [string[]]$Actual, [AllowEmptyCollection()] [string[]]$Expected, [string]$Description)
    $a = @($Actual | Sort-Object -Unique -CaseSensitive)
    $e = @($Expected | Sort-Object -Unique -CaseSensitive)
    $missing = @($e | Where-Object { $_ -cnotin $a })
    $extra = @($a | Where-Object { $_ -cnotin $e })
    Assert-R1 ($a.Count -eq $Actual.Count -and $missing.Count -eq 0 -and $extra.Count -eq 0) `
        "$Description mismatch; missing=$($missing -join ','); extra=$($extra -join ',')"
}

function Get-R1CandidatePaths {
    if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
        Assert-R1 ($null -ne $LifecycleContext) "R1 lifecycle context was not initialized."
        return @($LifecycleContext.CandidatePaths)
    }
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($arguments in @(
        @("diff", "--name-only", "--no-renames", $EntrySha, "HEAD", "--"),
        @("diff", "--name-only", "--no-renames", "--"),
        @("diff", "--cached", "--name-only", "--no-renames", "--"),
        @("ls-files", "--others", "--exclude-standard")
    )) {
        $values = @(& git -C $RepositoryRoot @arguments)
        Assert-R1 ($LASTEXITCODE -eq 0) "Cannot enumerate R1 source boundary."
        foreach ($value in $values) { if (-not [string]::IsNullOrWhiteSpace($value)) { $paths.Add($value.Replace('\', '/')) } }
    }
    return @($paths | Sort-Object -Unique -CaseSensitive)
}

function Assert-R1Entry {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-R1 ($LASTEXITCODE -eq 0) "Cannot resolve current R1 HEAD."
    if ($SelectedLifecycleMode -ceq "PRECOMMIT_CANDIDATE") {
        Assert-R1 ($head -ceq $EntrySha) "R1 candidate requires unchanged entry HEAD."
        $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
        Assert-R1 ($LASTEXITCODE -eq 0 -and $branch -ceq $ExpectedBranch) "Unexpected R1 implementation branch."
    } else {
        Assert-R1 ($null -ne $LifecycleContext -and $head -ceq $LifecycleContext.CurrentHead) `
            "Committed R1 lifecycle HEAD is not authenticated."
    }
    foreach ($entry in $RequiredHistoricalTags.GetEnumerator()) {
        $object = (& git -C $RepositoryRoot rev-parse "$($entry.Key)^{tag}").Trim()
        Assert-R1 ($LASTEXITCODE -eq 0 -and $object -ceq $entry.Value[0]) "Historical annotated tag changed: $($entry.Key)"
        $peel = (& git -C $RepositoryRoot rev-parse "$($entry.Key)^{}").Trim()
        Assert-R1 ($LASTEXITCODE -eq 0 -and $peel -ceq $entry.Value[1]) "Historical tag peel changed: $($entry.Key)"
        & git -C $RepositoryRoot merge-base --is-ancestor $peel $head
        Assert-R1 ($LASTEXITCODE -eq 0) "Historical product authority is not ancestral: $($entry.Key)"
    }
    $planning = (& git -C $RepositoryRoot rev-parse "refs/heads/$ProtectedPlanningBranch").Trim()
    Assert-R1 ($LASTEXITCODE -eq 0 -and $planning -ceq $ProtectedPlanningCommit) "Protected G9U1 planning branch changed."
}

function Assert-R1PreservedD2 {
    Assert-R1 ((Get-R1FileHash $D2EvidencePath) -ceq "21fe616f80bd511d4c0ca08a9e361c9800c4f518835113a72679b625de2f4f46") "Historical D2 evidence changed."
    Assert-R1 ((Get-R1FileHash 'docs/validation/g9s1_r1_implementation_blocker_report.md') -ceq
        'a85a65e1808f1760a7ce7d68029bbb783e535bfe362712f9a99e63b2b67e1ba5') "Historical scientific blocker report changed."
    Assert-R1 ((Get-R1FileHash 'geocedg/validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json') -ceq
        'd43d320b781c068dacd0091a55fdc6a73658b37f9b4da13d5941a8c8469ec90f') "Historical scientific blocker evidence changed."
    $d2 = Get-Content -Raw -LiteralPath (Resolve-R1File $D2EvidencePath) | ConvertFrom-Json -Depth 100
    foreach ($entry in $d2.canonicalLfSha256.PSObject.Properties) {
        if ($entry.Name -in @("docs/roadmap/geocedg_roadmap.md", "docs/validation/g9_documentation_bundle_traceability.md")) { continue }
        Assert-R1 ((Get-R1FileHash $entry.Name) -ceq [string]$entry.Value) "Preserved D2 authority changed: $($entry.Name)"
    }
}

function Assert-R1Contracts {
    param([object]$Scenarios, [object]$Evidence, [string[]]$CandidatePaths)
    Assert-R1 ($Scenarios.phase -ceq "G9S1-R1" -and $Evidence.phase -ceq "G9S1-R1") "Wrong R1 evidence phase."
    foreach ($field in @("designApproved", "implementationAuthorized", "selfApproved",
        "authorApprovedPhase", "passClaimed")) {
        Assert-R1 ($Evidence.approval.$field -is [bool]) "R1 approval field must be Boolean: $field"
    }
    $authorCloseout = $SelectedLifecycleMode -cin @("AUTHOR_CLOSEOUT", "PUBLISHED_REGRESSION")
    if ($authorCloseout) {
        Assert-R1 ($Evidence.status -ceq "PASS_AUTHOR_APPROVED" -and $Evidence.entrySha -ceq $EntrySha) `
            "R1 author-closeout state/entry drifted."
        Assert-R1 ($Evidence.approval.designApproved -and $Evidence.approval.implementationAuthorized -and
            -not $Evidence.approval.selfApproved -and $Evidence.approval.authorApprovedPhase -and
            $Evidence.approval.passClaimed) "R1 author-closeout approval flags are inconsistent."
    } else {
        Assert-R1 ($Evidence.status -cin @("IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW",
            "BLOCKED_AUTHOR_REVIEW_REQUIRED", "BLOCKED_AUTHORIZED_CORRECTIVE_CONTINUATION") -and
            $Evidence.entrySha -ceq $EntrySha) "R1 candidate state/entry drifted."
        if ($SelectedLifecycleMode -ceq "COMMITTED_CANDIDATE") {
            Assert-R1 ($Evidence.status -ceq "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW") `
                "Committed R1 must retain candidate status."
        }
        Assert-R1 ($Evidence.approval.designApproved -and $Evidence.approval.implementationAuthorized -and
            -not $Evidence.approval.selfApproved -and -not $Evidence.approval.authorApprovedPhase -and
            -not $Evidence.approval.passClaimed) "R1 design authorization is not phase PASS."
    }
    if ($Evidence.status -cin @("BLOCKED_AUTHOR_REVIEW_REQUIRED", "BLOCKED_AUTHORIZED_CORRECTIVE_CONTINUATION")) {
        Assert-R1 ($Evidence.approval.implementationComplete -is [bool] -and
            -not $Evidence.approval.implementationComplete) "Blocked R1 cannot claim implementation completion."
    }
    if ($Evidence.status -ceq "BLOCKED_AUTHORIZED_CORRECTIVE_CONTINUATION") {
        Assert-R1 ($Evidence.approval.correctionAAuthorized -is [bool] -and
            $Evidence.approval.correctionAAuthorized -and
            $Evidence.approval.correctionBAuthorized -is [bool] -and
            $Evidence.approval.correctionBAuthorized) "Corrective continuation requires explicit A/B author authority."
    }
    Assert-R1Set $CandidatePaths @($Evidence.sourceBoundary.candidatePaths) "R1 exact candidate inventory"
    Assert-R1 (@($CandidatePaths | Where-Object { $_ -match '^(artifacts/|source/desktop/.*/src/main/|source/web/|geocedg/features/)' }).Count -eq 0) "R1 source boundary includes generated/frontend/profile work."
    Assert-R1 (@($CandidatePaths | Where-Object { $_ -match '(^|/)g9u1[^/]*\.prompt\.md$' }).Count -eq 0) "Protected G9U1 prompt changes are outside R1."
    Assert-R1 ($Evidence.authority.canonicalPromptLfSha256 -ceq (Get-R1FileHash $PromptPath)) "R1 canonical prompt hash mismatch."
    Assert-R1 ($Evidence.infrastructureImpact -ceq "UPDATE_REQUIRED") "New R1 phase integration requires infrastructure review."
    Assert-R1Set @($Evidence.requiredLevels) @("PHASE", "COMPOSED", "FULL_CLEAN_BUILD") "R1 required levels"
    Assert-R1 ($Evidence.bootstrapImpact.outcome -ceq "NO_CHANGE_REQUIRED" -and
        -not [string]::IsNullOrWhiteSpace($Evidence.bootstrapImpact.rationale)) "Missing substantive bootstrap no-change review."
    Assert-R1 (@($Evidence.retainedRisks | Where-Object { $_.id -ceq $OpenRisk -and $_.status -ceq "OPEN_TRACKED" }).Count -eq 1) "R4 periodic-quarantine risk lost or closed."

    $matrix = Get-Content -Raw -LiteralPath (Resolve-R1File $MatrixPath)
    $matrixIds = @([regex]::Matches($matrix, '(?m)^\|\s*(R1-[A-Z][0-9]+)\s*\|') | ForEach-Object { $_.Groups[1].Value })
    Assert-R1Set $matrixIds $RequiredScenarioIds "R1 mandatory matrix"
    Assert-R1Set @($Scenarios.scenarioIds) $RequiredScenarioIds "R1 scenario inventory"
    $classes = @($Scenarios.focusedJUnit.classes)
    Assert-R1 (@($classes).Count -gt 0) "R1 focused classes missing."
    $classNames = @($classes | ForEach-Object { [string]$_.name })
    Assert-R1 ($classNames.Count -eq @($classNames | Sort-Object -Unique -CaseSensitive).Count) "Duplicate focused class."
    foreach ($className in $RequiredTestClasses) { Assert-R1 ($className -cin $classNames) "Required scientific class missing: $className" }
    Assert-R1 (@($classes | Where-Object { $_.name -match '^org\.geocedg\.desktop\..*R1.*' }).Count -ge 1) "Dedicated native Desktop R1 archive tests are mandatory."
    foreach ($class in $classes) {
        $text = Get-Content -Raw -LiteralPath (Resolve-R1File $class.source)
        $methods = @([regex]::Matches($text, '(?ms)^\s*@Test\s+(?:public\s+)?void\s+([A-Za-z0-9_]+)\s*\(') | ForEach-Object { $_.Groups[1].Value })
        Assert-R1 (@($class.methods).Count -gt 0) "No mandatory methods for $($class.name)"
        Assert-R1Set $methods @($class.methods) "R1 source method inventory $($class.name)"
        $expectedSource = if ($class.name -match '^org\.geocedg\.desktop\.') { "source/desktop/desktop/src/test/java/" } else { "source/shared/common-jre/src/test/java/" }
        Assert-R1 ($class.source -ceq ($expectedSource + $class.name.Replace('.', '/') + '.java')) "Class/source module mismatch: $($class.name)"
    }
    $coverage = @($Scenarios.coverage)
    Assert-R1Set @($coverage | ForEach-Object { [string]$_.id }) $RequiredScenarioIds "R1 scenario coverage mapping"
    $methodKeys = @($classes | ForEach-Object {
        $className = $_.name
        foreach ($method in $_.methods) { "$className#$method" }
    })
    foreach ($anchoredClass in $MandatoryMethodAnchors.Keys) {
        foreach ($anchoredMethod in $MandatoryMethodAnchors[$anchoredClass]) {
            $anchoredKey = "$anchoredClass#$anchoredMethod"
            Assert-R1 ($anchoredKey -cin $methodKeys) "Mandatory semantic regression removed: $anchoredKey"
        }
    }
    foreach ($row in $coverage) {
        $mappedMethods = @($row.methods)
        Assert-R1 ($mappedMethods.Count -gt 0 -or -not [string]::IsNullOrWhiteSpace($row.authority)) "Unexplained scenario coverage: $($row.id)"
        foreach ($method in $mappedMethods) { Assert-R1 ($method -cin $methodKeys) "Unknown scenario method $method for $($row.id)" }
    }
    $manifest = @{}
    foreach ($line in (Get-Content -LiteralPath (Resolve-R1File $ManifestPath))) {
        if ($line -match '^([0-9a-f]{64})\s{2}(.+)$') {
            Assert-R1 (-not $manifest.ContainsKey($Matches[2])) "Duplicate manifest path."
            $manifest[$Matches[2]] = $Matches[1]
        } elseif (-not [string]::IsNullOrWhiteSpace($line)) { throw "Malformed R1 hash manifest line." }
    }
    Assert-R1Set @($manifest.Keys) @($EvidencePath, $ScenarioPath) "R1 manifest inventory"
    foreach ($path in @($EvidencePath, $ScenarioPath)) { Assert-R1 ($manifest[$path] -ceq (Get-R1FileHash $path)) "R1 manifest mismatch: $path" }

    $phase = Get-GeoCeDGPhaseDefinition -Phase G9S1-R1
    Assert-R1 ($phase.Verifier -ceq (Split-Path -Leaf $VerifierPath)) "R1 phase registry mismatch."
    $composed = Get-Content -Raw -LiteralPath (Resolve-R1File "tools/agent/verify.ps1")
    Assert-R1 ($composed.IndexOf('& $G9S1R1SplinePairVerifier', [StringComparison]::Ordinal) -gt
        $composed.IndexOf('& $G9U0R6SemanticLocusPointInteractionVerifier', [StringComparison]::Ordinal)) "R1 must compose after R6."
    $utf8 = [Text.UTF8Encoding]::new($false)
    Assert-R1 ((Get-R1TextHash $utf8.GetBytes("pair`nsource`n")) -ceq (Get-R1TextHash $utf8.GetBytes("pair`r`nsource`r`n"))) "R1 hash is EOL-dependent."
    Assert-R1 ((Get-R1TextHash $utf8.GetBytes("pair`nsource`n")) -cne (Get-R1TextHash $utf8.GetBytes("pair`nSource`n"))) "R1 hash ignored real content mutation."
}

function Invoke-R1Gradle {
    param([string[]]$Arguments, [string]$Description, [string]$LogFileName)
    $effective = @($Arguments)
    if (-not $AllowToolchainDownload) { $effective += '-Dorg.gradle.java.installations.auto-download=false' }
    $logPath = Join-Path $LogDirectory $LogFileName
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath -RepositoryRoot $RepositoryRoot `
            -WorkingDirectory $RepositoryRoot -Arguments $effective -LogPath $logPath `
            -Description $Description -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) { $effective = @(ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $effective -KeepBuildOutputs:$KeepBuildOutputs) }
    Push-Location $RepositoryRoot
    try { & (Join-Path $RepositoryRoot 'gradlew.bat') @effective 2>&1 | Tee-Object -FilePath $logPath; $exit = $LASTEXITCODE }
    finally { Pop-Location }
    Assert-R1 ($exit -eq 0) "$Description failed with exit $exit; log $logPath"
}

function Get-R1TestResult {
    param([object]$Class)
    $root = if ($Class.name -match '^org\.geocedg\.desktop\.') { 'source/desktop/desktop/build/test-results/test' } else { 'source/shared/common-jre/build/test-results/test' }
    [xml]$xml = Get-Content -Raw -LiteralPath (Resolve-R1File "$root/TEST-$($Class.name).xml")
    $suite = $xml.testsuite
    $methods = @($suite.testcase | ForEach-Object { $_.name -replace '\(.*\)$', '' })
    Assert-R1Set $methods @($Class.methods) "Executed R1 methods $($Class.name)"
    Assert-R1 ([int]$suite.tests -eq @($Class.methods).Count -and [int]$suite.failures -eq 0 -and
        [int]$suite.errors -eq 0 -and [int]$suite.skipped -eq 0) "R1 JUnit failure/error/skip/count mismatch: $($Class.name)"
    return [ordered]@{ class = $Class.name; tests = [int]$suite.tests; failures = 0; errors = 0; skipped = 0; methods = @($methods | Sort-Object -CaseSensitive) }
}

function Initialize-R1Lifecycle {
    if ($SelectedLifecycleMode -ceq "AUTO") {
        $record = if ([string]::IsNullOrWhiteSpace($CloseoutRecordPath)) {
            Join-Path $RepositoryRoot $DefaultCloseoutRecordPath
        } elseif ([IO.Path]::IsPathRooted($CloseoutRecordPath)) {
            [IO.Path]::GetFullPath($CloseoutRecordPath)
        } else { [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $CloseoutRecordPath)) }
        $script:SelectedLifecycleMode = if (-not $AuthorCloseoutOnly -and
                (Test-Path -LiteralPath (Join-Path $RepositoryRoot $PublishedRegressionAuthorityPath) -PathType Leaf)) {
            "PUBLISHED_REGRESSION"
        } elseif (Test-Path -LiteralPath $record -PathType Leaf) {
            "AUTHOR_CLOSEOUT"
        } elseif (Test-Path -LiteralPath (Join-Path $RepositoryRoot $LifecyclePolicyPath) -PathType Leaf) {
            "COMMITTED_CANDIDATE"
        } else { "PRECOMMIT_CANDIDATE" }
    }
    if ($SelectedLifecycleMode -ceq "PRECOMMIT_CANDIDATE") {
        Assert-R1 (-not $AuthorCloseoutOnly -and [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "Precommit mode cannot accept closeout evidence."
        return
    }
    Assert-R1 (($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT") -eq [bool]$AuthorCloseoutOnly) `
        "Author-closeout mode is documentary-only; committed-candidate verification runs live gates."
    . (Join-Path $RepositoryRoot $LifecycleHelperPath)
    if ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION") {
        Assert-R1 ([string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "Published regression cannot accept documentary closeout evidence or overridden targets."
        $script:AuthorityPaths += @($PublishedRegressionAuthorityPath, $LifecyclePolicyPath,
            $LifecycleHelperPath, "tools/agent/repository-input-identity.ps1")
        $script:LifecycleContext = Get-GeoCeDGPhasePublishedRegressionContext `
            -RepositoryRoot $RepositoryRoot -PublishedAuthorityPath $PublishedRegressionAuthorityPath `
            -ExpectedReviewedTechnicalCommit $PublishedReviewedTechnicalCommit `
            -ExpectedCloseoutCommit $PublishedCloseoutCommit -ExpectedPolicyPath $LifecyclePolicyPath
        return
    }
    $script:AuthorityPaths += @(
        $LifecyclePolicyPath,
        $LifecycleHelperPath,
        "tools/agent/tests/phase-lifecycle.Tests.ps1",
        "tools/agent/verify-verification-infrastructure.ps1",
        "geocedg/specs/operations/verification-levels.md",
        "docs/adr/0023-phase-verifier-lifecycle-and-author-closeout.md",
        "docs/architecture/g9s1_r1_verifier_lifecycle.md",
        "docs/developer/geocedg_developer_guide.md",
        "docs/validation/g9s1_r1_verifier_lifecycle_report.md"
    )
    $parameters = @{
        RepositoryRoot = $RepositoryRoot
        PolicyPath = (Join-Path $RepositoryRoot $LifecyclePolicyPath)
        ExpectedImplementationCommit = $ImplementationCommit
        Mode = $SelectedLifecycleMode
    }
    if (-not [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit)) {
        $parameters.ReviewedTechnicalCommit = $ReviewedTechnicalCommit
    }
    $effectiveRecord = if ([string]::IsNullOrWhiteSpace($CloseoutRecordPath)) {
        Join-Path $RepositoryRoot $DefaultCloseoutRecordPath
    } elseif ([IO.Path]::IsPathRooted($CloseoutRecordPath)) {
        [IO.Path]::GetFullPath($CloseoutRecordPath)
    } else { [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $CloseoutRecordPath)) }
    if ($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT") {
        $script:AuthorityPaths += $DefaultCloseoutRecordPath
        $parameters.CloseoutRecordPath = $effectiveRecord
        $parameters.BundleDirectory = $TechnicalEvidenceBundleDirectory
        $parameters.BundleSha256 = $TechnicalEvidenceBundleSha256
    }
    $script:LifecycleContext = Get-GeoCeDGPhaseLifecycleContext @parameters
}

$InitialStatus = $null
$GeneratedState = $null
$Failure = $null
try {
    Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $RepositoryRoot -LogDirectory $LogDirectory
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 --untracked-files=all) -join "`n"
    Assert-R1 ($LASTEXITCODE -eq 0) "Cannot read repository status."
    Initialize-R1Lifecycle
    foreach ($path in $AuthorityPaths) { [void](Resolve-R1File $path) }
    Assert-R1Entry
    Assert-R1PreservedD2
    $scenarios = Get-Content -Raw -LiteralPath (Resolve-R1File $ScenarioPath) | ConvertFrom-Json -Depth 100
    $evidence = Get-Content -Raw -LiteralPath (Resolve-R1File $EvidencePath) | ConvertFrom-Json -Depth 100
    $candidatePaths = @(Get-R1CandidatePaths)
    Assert-R1Contracts $scenarios $evidence $candidatePaths
    if (-not $HistoricalRegressionsAlreadyComposed) {
        & (Join-Path $PSScriptRoot 'verify-g9u0-r6-semantic-locus-point-interaction-support.ps1') `
            -SkipBuild -LogDirectory (Join-Path $LogDirectory 'sealed-r6')
        Assert-R1 ($LASTEXITCODE -eq 0) "Historical descendant-safe R6 authority failed."
    }
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --check
    Assert-R1 ($LASTEXITCODE -eq 0) "git diff --check failed."
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --cached --check
    Assert-R1 ($LASTEXITCODE -eq 0) "git diff --cached --check failed."
    if ($AuthorCloseoutOnly) {
        Assert-R1 ($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT" -and -not $SkipBuild -and
            [string]::IsNullOrWhiteSpace($BuildEvidencePath) -and -not $IncrementalBuild) `
            "Author-closeout-only is a documentary consistency check, not build evidence."
        $closeout = [ordered]@{
            schemaVersion = 1
            phase = "G9S1-R1"
            state = "AUTHOR_CLOSEOUT_CONSISTENCY_LINKED_NOT_NEW_EXECUTION"
            lifecycle = $LifecycleContext
            productRuntimeExecuted = $false
            currentRunReceiptProduced = $false
            authorDecisionCreatedByVerifier = $false
        }
        [IO.File]::WriteAllText((Join-Path $LogDirectory "author-closeout-result.json"),
            (($closeout | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"),
            [Text.UTF8Encoding]::new($false))
        Write-Host "G9S1-R1 author-closeout consistency passed; linked technical evidence was not rerun."
    } elseif ($SkipBuild) {
        if ($evidence.status -ceq "BLOCKED_AUTHOR_REVIEW_REQUIRED") {
            Write-Host 'G9S1-R1 blocked-state static record is coherent; runtime acceptance is BLOCKED/INCOMPLETE, not PASS.'
        } elseif ($evidence.status -ceq "BLOCKED_AUTHORIZED_CORRECTIVE_CONTINUATION") {
            Write-Host 'G9S1-R1 corrective authorization is coherent; static checks do not establish correction or runtime acceptance.'
        } else {
            Write-Host 'G9S1-R1 static contracts passed; runtime acceptance remains INCOMPLETE.'
        }
    } else {
        Assert-R1 ($evidence.status -cne "BLOCKED_AUTHOR_REVIEW_REQUIRED") `
            "R1 is BLOCKED — AUTHOR REVIEW REQUIRED. Static coherence cannot confer PHASE acceptance; required scientific and FULL gates remain incomplete."
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedState = New-RepositoryGeneratedStateSnapshot -RepositoryRoot $RepositoryRoot `
                -DirectoryNames @('build', '.gradle', '.kotlin') -Label 'verify-g9s1-r1' -KeepCurrentOutputs:$KeepBuildOutputs
        }
        $classes = @($scenarios.focusedJUnit.classes)
        $flags = @('--rerun-tasks', '--no-build-cache', '--no-daemon', '--no-problems-report', '--console=plain')
        foreach ($module in @('shared', 'desktop')) {
            $selected = @($classes | Where-Object { ($_.name -match '^org\.geocedg\.desktop\.') -eq ($module -eq 'desktop') })
            $task = if ($module -eq 'desktop') { ':desktop:desktop:test' } else { ':shared:common-jre:test' }
            $arguments = @($task)
            foreach ($class in $selected) { $arguments += @('--tests', $class.name) }
            Invoke-R1Gradle ($arguments + $flags) "R1 focused $module scientific/lifecycle tests" "r1-$module-gradle.log"
        }
        $styles = @(':shared:common:checkstyleMain', ':shared:common-jre:checkstyleTest', ':desktop:desktop:checkstyleTest')
        Invoke-R1Gradle ($styles + $flags) 'R1 affected Checkstyle' 'r1-checkstyle-gradle.log'
        $results = @($classes | ForEach-Object { Get-R1TestResult $_ })
        foreach ($path in @('source/shared/common/build/reports/checkstyle/main.xml',
            'source/shared/common-jre/build/reports/checkstyle/test.xml', 'source/desktop/desktop/build/reports/checkstyle/test.xml')) {
            Assert-R1 (@(Select-String -LiteralPath (Resolve-R1File $path) -Pattern '<error ').Count -eq 0) "R1 Checkstyle errors: $path"
        }
        $sourceHashes = @($candidatePaths | Where-Object { $_ -match '^source/.*\.(java|properties)$' } | ForEach-Object {
            [ordered]@{ path = $_; sha256 = Get-R1TextHash (Get-R1SourceBytes $_) }
        })
        $summary = [ordered]@{
            schemaVersion = 1; phase = 'G9S1-R1'; state = 'TECHNICAL_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL'; entrySha = $EntrySha
            scenarioIds = @($RequiredScenarioIds | Sort-Object -CaseSensitive)
            testResults = @($results | Sort-Object { $_.class }); candidatePaths = $candidatePaths
            deterministicSourceHashes = $sourceHashes
            authorityHashes = @($AuthorityPaths | Sort-Object -CaseSensitive | ForEach-Object { [ordered]@{ path = $_; sha256 = Get-R1FileHash $_ } })
            openRisk = $OpenRisk; selfApproved = $false; authorApprovedPhase = $false; passClaimed = $false
        }
        if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
            $summary["lifecycle"] = [ordered]@{
                mode = $SelectedLifecycleMode
                implementationCommit = $LifecycleContext.ImplementationCommit
                currentHead = $LifecycleContext.CurrentHead
                infrastructurePaths = @($LifecycleContext.InfrastructurePaths)
                documentaryEvidenceLinked = [bool]$LifecycleContext.DocumentaryEvidenceLinked
            }
        }
        $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent $CanonicalSummaryPath))
        [IO.File]::WriteAllText($CanonicalSummaryPath, $json, [Text.UTF8Encoding]::new($false))
        $hash = (Get-FileHash -LiteralPath $CanonicalSummaryPath -Algorithm SHA256).Hash.ToLowerInvariant()
        if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
            $comparison = (Get-FileHash -LiteralPath $CompareCanonicalSummaryPath -Algorithm SHA256).Hash.ToLowerInvariant()
            Assert-R1 ($hash -ceq $comparison) "R1 deterministic canonical summary mismatch."
        }
        $total = ($results | ForEach-Object { [int]$_.tests } | Measure-Object -Sum).Sum
        Write-Host "G9S1-R1 focused scientific/lifecycle cases: $total; canonical SHA-256: $hash"
        Write-Host 'G9S1-R1 technical focused gates passed; author approval is separate.'
    }
} catch { $Failure = $_.Exception.Message + "`n" + $_.ScriptStackTrace }
finally {
    try {
        if ($null -ne $GeneratedState) { Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState -KeepCurrentOutputs:$KeepBuildOutputs -Description 'R1 generated output' }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 --untracked-files=all) -join "`n"
            Assert-R1 ($LASTEXITCODE -eq 0 -and $finalStatus -ceq $InitialStatus) "Repository status changed during R1 verification."
        }
    } catch { $Failure = (@($Failure, "Cleanup/status failure: $($_.Exception.Message)") | Where-Object { $_ }) -join "`n" }
}
if ($null -ne $Failure) { Write-Error -Message $Failure -ErrorAction Continue; exit 1 }
exit 0

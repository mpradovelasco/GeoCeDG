[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r6-semantic-locus-point-interaction-support")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "de33f3a80102adb051aaa7547a72b7e97409c58c"
$ExpectedBranch = "feature/g9u0-r6-semantic-locus-point-interaction-support"
$R6PassTagName = "geocedg-g9u0-r6-pass"
$S1PassTagName = "geocedg-g9s1-pass"
$S1PassTagObject = "ece0ca6f00299d3347e57fac38b7a28cade28644"
$ProtectedG9U1Branch = "feature/g9u1-construction-workspace-planning"
$ProtectedG9U1Checkpoint = "857de6628489bda0b65a5ba5145e62ca0795fc32"
$ProtectedG9U1PromptHash =
    "2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322"
$OpenPeriodicRisk = "G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP"

$PromptPath =
    ".github/prompts/tasks/g9u0-r6-semantic-locus-point-interaction-support.prompt.md"
$AdrPath = "docs/adr/0019-semantic-locus-point-interaction-support.md"
$SpecPath = "geocedg/specs/locus/locus-v2-point-interaction.md"
$ArchitecturePath =
    "docs/architecture/g9u0_r6_semantic_locus_point_interaction_support.md"
$MatrixPath =
    "docs/validation/g9u0_r6_semantic_locus_point_interaction_validation_matrix.md"
$ReportPath =
    "docs/validation/g9u0_r6_semantic_locus_point_interaction_candidate_report.md"
$ScenarioPath =
    "geocedg/validation/g9u0-r6/g9u0-r6-semantic-locus-point-interaction-scenarios.json"
$EvidencePath =
    "geocedg/validation/g9u0-r6/g9u0-r6-semantic-locus-point-interaction-evidence.json"
$ManifestPath = "geocedg/validation/g9u0-r6/g9u0-r6-evidence.sha256"
$VerifierRelativePath =
    "tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1"
$ComposedVerifierPath = "tools/agent/verify.ps1"
$S1VerifierPath = Join-Path $PSScriptRoot `
    "verify-g9s1-semantic-spline-2d-capability.ps1"

$InteractionSourceDirectory =
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/interaction"
$RequiredProductSourcePaths = @(
	"source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSemanticLocusPoint2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/CertifiedAffineLocus2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusEvaluationSession2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSemanticAddressState2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/PiecewisePolynomialLocus2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java",
	"$InteractionSourceDirectory/LocusPointInteractionCandidate2D.java",
	"$InteractionSourceDirectory/LocusPointInteractionInstrumentation2D.java",
	"$InteractionSourceDirectory/LocusPointInteractionInstrumentationSnapshot2D.java",
	"$InteractionSourceDirectory/LocusPointInteractionLocalEvidence2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionPolicy2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionQuery2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionResolver2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionResult2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionStatus2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionWorkBudget2D.java",
    "$InteractionSourceDirectory/package-info.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/ConstructionGeoRedefineProvider.java",
	"source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java",
	"source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java"
)
$TestSourcePaths = [ordered]@{
    "org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest" =
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R6SemanticLocusPointInteractionTest.java"
    "org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2DTest" =
        "source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2DTest.java"
    "org.geocedg.desktop.G9U0R6NativeArchivePersistenceTest" =
        "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R6NativeArchivePersistenceTest.java"
}
$ExpectedTestMethods = [ordered]@{
    "org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest" = @(
        "r601SplinePolynomialInverseIsUniqueAndCarriesExactAddressEvidence",
        "r602SplineKnotHasOneCanonicalOwnerIndependentOfSpanEnumeration",
        "r603SelfIntersectionReturnsAllExactPreimagesAndFailsClosed",
        "r604PolynomialOutsideWorldThresholdHasNoAdmissiblePreimage",
        "r605EvaluatorFallbackIsBoundedAndBudgetExhaustionIsTyped",
        "r606ExplicitSemanticPointIsNotInteractionOwnedAndCannotBeMoved",
        "r607InteractiveCreationAndMovePreservePointIdentityAndExactOwnership",
        "r608AmbiguousMoveLeavesInteractionOwnedDagInputsUntouched",
        "r609PeriodicSeamUsesCanonicalParameterAndNearestIntrinsicLift",
        "r610SimilarityImagesResolveSemanticallyAndCollapsedImageRecovers",
        "r611InvalidDynamicSourceFailsClosedAndValidRecoveryIsDeterministic",
        "r612RepeatedQueriesArePathIndependentAndReadNoPresentationAuthority",
        "r613InteractivePointSaveReopenRestoresOwnedStateAndMovement",
		"r614InteractivePointUndoRedoRetainsDurableIdentityAndOwnership",
		"r615InteractiveMoveUndoRedoRestoresTheExactSemanticAddress",
		"r616CopyRemapsTheOwnedSourceAndRemainsInteractivelyMovable",
		"r617DisconnectedComponentsRejectGapSelectionAndPreventPointJump",
		"r618TransformedGeneralLocusUsesEvaluatorFallbackWhenPolynomialIsUnsupported",
		"r619RotatedReflectedAndNegativeDilatedSplinesResolveCovariantly",
		"r620ExistingPointSurvivesTwoZeroMinusTwoCollapseCycleWithSameAddress",
		"r621RenameChangesNoDurableInteractionIdentityOrAddress",
		"r622StaleCandidateIsRejectedBeforeAnyConstructionPublication",
		"r623InstrumentationSeparatesGlobalAndLocalScopesWithinEveryDragBudget",
		"r624VersionedAddressStateRoundTripPreservesPeriodicSemanticEvidence",
		"r625MalformedAddressStateCannotMoveOrFallBackToGlobalResolution",
		"r626IncompatibleVersionedAddressCannotMoveOrReattachGlobally",
		"r627DeepSimilarityCompositionCountsNestedMissesAndStopsAtBudget",
		"r628ExcessivePolynomialDegreeFailsTypedBeforeUnboundedIsolationWork",
		"r629MultipleBranchesAreCanonicalUnderDefinitionOrderPerturbation",
		"r630CuspWithZeroSemanticSpeedCannotClaimUniqueAddress",
		"r631HighCurvatureEvaluatorRefinesAndForwardVerifiesSemantically",
		"r632NonperiodicSplineEndpointsHaveOneCanonicalOwnerEach",
		"r633PolynomialSearchRejectsSpansOutsideTheValidSemanticComponent",
		"r634ComponentLossRoundTripRecoversTheSameInteractivePoint",
		"r635ClosedSplineSeamDeduplicatesEquivalentPolynomialEndpoints",
		"r636NonfiniteInteractionInputsFailBeforeConstructionMutation",
		"r637InteractivePointFeedsPartialMetricThroughTheNormalDag",
		"r638RepeatedComponentLossPreservesLastAcceptedAddressUntilRecovery",
		"r639ExtremePeriodicLiftFailsClosedWithoutStaleCoordinates",
		"r640AddressStateCodecRejectsAliasesAndMalformedUtf8",
		"r641RemovalCleansOnlyExclusiveDedicatedAuxiliaries",
		"r642ActualSpanPermutationPreservesKnotAndCrossingCandidates",
		"r643EverySplineAndSimilarityCandidateIsForwardVerified",
		"r644EncodedOrdinaryPointInputsKeepTheirPresentationOwnership",
		"r645PersistedComponentLineageWinsAtSharedSemanticEndpoint",
		"r646NarrowGenericMinimumCannotUpgradeBoundedCoverage",
		"r647FailedMoveRollsBackTheWholeConstructionAndCanRecover",
		"r648ExcessivePolynomialCompositionDepthStopsPointAndPairTraversal",
		"r649ClosedSplineInteractivePointCrossesPeriodicSeamBidirectionally"
	)
	"org.geocedg.common.kernel.locus.intersection.PolynomialRootIsolation2DTest" = @(
		"derivativePartitionFindsSameClassRootsIndependentlyOfSignAtEnds",
		"repeatedRootAndZeroPolynomialRemainExplicit",
		"insufficientRefinementBudgetFailsClosed"
	)
	"org.geocedg.desktop.G9U0R6NativeArchivePersistenceTest" = @(
		"nativeCedgReopensOwnedTransformedPointAndMovesItAgain",
		"featureOffAndClassicPreserveOwnedPointWithoutCreationAuthority",
		"nativeCedgReopensDormantAddressAndRecoversTheSamePoint"
	)
}
$DesktopTestClasses = @(
    "org.geocedg.desktop.G9U0R6NativeArchivePersistenceTest"
)
$ExpectedSharedKernelCount = 52
$ExpectedDesktopCount = 3
$ExpectedFocusedCount = 55
$ExpectedMatrixRows = 72
$ExpectedCandidatePaths = @(
    ".github/prompts/tasks/g9u0-r6-semantic-locus-point-interaction-support.prompt.md",
    "docs/adr/0019-semantic-locus-point-interaction-support.md",
    "docs/architecture/g9u0_r6_semantic_locus_point_interaction_support.md",
    "docs/developer/geocedg_developer_guide.md",
    "docs/developer/locus_v2_api.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/upstream/modified-files.yml",
    "docs/validation/g9_documentation_bundle_traceability.md",
    "docs/validation/g9u0_r6_semantic_locus_point_interaction_candidate_report.md",
    "docs/validation/g9u0_r6_semantic_locus_point_interaction_validation_matrix.md",
    "geocedg/specs/locus/locus-v2-point-interaction.md",
    "geocedg/specs/README.md",
    "geocedg/validation/g9u0-r6/g9u0-r6-evidence.sha256",
    "geocedg/validation/g9u0-r6/g9u0-r6-semantic-locus-point-interaction-evidence.json",
    "geocedg/validation/g9u0-r6/g9u0-r6-semantic-locus-point-interaction-scenarios.json",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R6NativeArchivePersistenceTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2DTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R6SemanticLocusPointInteractionTest.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSemanticLocusPoint2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/CertifiedAffineLocus2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionCandidate2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionInstrumentation2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionInstrumentationSnapshot2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionLocalEvidence2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionPolicy2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionQuery2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionResolver2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionResult2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionStatus2D.java",
    "$InteractionSourceDirectory/LocusPointInteractionWorkBudget2D.java",
    "$InteractionSourceDirectory/package-info.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusEvaluationSession2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSemanticAddressState2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/PiecewisePolynomialLocus2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/ConstructionGeoRedefineProvider.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java",
    "tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1",
    "tools/agent/verify.ps1"
)
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$script:R6BoundaryMode = "UNSET"
$script:R6AuthorityCommit = $null

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

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RepositoryPath {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $fullPath = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $RelativePath))
    $rootPrefix = $RepositoryRoot.TrimEnd('\', '/') +
        [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($fullPath.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Path escapes repository root: $RelativePath"
    return $fullPath
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $fullPath = Resolve-RepositoryPath $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $fullPath -PathType Leaf) `
        -Message "Required G9U0-R6 file is missing: $RelativePath"
    return $fullPath
}

function Get-CanonicalLfSha256FromBytes {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        $Bytes, $offset, $Bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes(
                $canonical))).ToLowerInvariant()
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return Get-CanonicalLfSha256FromBytes -Bytes (
        Get-SourceAuthorityBytes $RelativePath)
}

function Get-GitBlobBytes {
    param([Parameter(Mandatory)] [string]$Object)

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    [void]$startInfo.ArgumentList.Add("-C")
    [void]$startInfo.ArgumentList.Add($RepositoryRoot)
    [void]$startInfo.ArgumentList.Add("cat-file")
    [void]$startInfo.ArgumentList.Add("blob")
    [void]$startInfo.ArgumentList.Add($Object)

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $memory = [IO.MemoryStream]::new()
    try {
        [void]$process.Start()
        $copyTask = $process.StandardOutput.BaseStream.CopyToAsync($memory)
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [void]$copyTask.GetAwaiter().GetResult()
        $errorText = $errorTask.GetAwaiter().GetResult()
        Assert-Condition -Condition ($process.ExitCode -eq 0) `
            -Message "Unable to read R6 Git blob ${Object}: $($errorText.Trim())"
        return ,([byte[]]$memory.ToArray())
    } finally {
        $memory.Dispose()
        $process.Dispose()
    }
}

function Get-SourceAuthorityBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [void](Resolve-RepositoryPath $RelativePath)
    if ($script:R6BoundaryMode -ne "TAGGED_DESCENDANT" -or
            $null -eq $script:R6AuthorityCommit) {
        return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
    }
    $normalized = $RelativePath.Replace("\", "/")
    return ,(Get-GitBlobBytes -Object "$($script:R6AuthorityCommit):$normalized")
}

function Convert-AuthorityBytesToText {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    return [Text.UTF8Encoding]::new($false, $true).GetString(
        $Bytes, $offset, $Bytes.Length - $offset)
}

function Get-SourceAuthorityText {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [byte[]]$bytes = Get-SourceAuthorityBytes $RelativePath
    return Convert-AuthorityBytesToText -Bytes $bytes
}

function Get-DeterministicSourceAuthorityBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)

    if ($script:R6BoundaryMode -eq "TAGGED_DESCENDANT") {
        return ,(Get-SourceAuthorityBytes $RelativePath)
    }
    $normalized = $RelativePath.Replace("\", "/")
    & git -C $RepositoryRoot diff --quiet HEAD -- $normalized
    $diffExit = $LASTEXITCODE
    if ($diffExit -eq 0) {
        & git -C $RepositoryRoot ls-files --error-unmatch -- $normalized `
            2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            return ,(Get-GitBlobBytes -Object "HEAD:$normalized")
        }
    } elseif ($diffExit -ne 1) {
        throw "Unable to classify R6 source authority for $RelativePath."
    }
    return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
}

function Get-DeterministicSourceSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return Get-CanonicalLfSha256FromBytes -Bytes (
        Get-DeterministicSourceAuthorityBytes $RelativePath)
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return (Get-SourceAuthorityText $RelativePath) |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON: $($_.Exception.Message)"
    }
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

function Get-WorktreeCandidatePaths {
    $paths = [Collections.Generic.List[string]]::new()
    $pathSets = [Collections.Generic.List[object]]::new()
    $pathSets.Add(@(& git -C $RepositoryRoot diff --name-only --no-renames `
            $EntrySha HEAD --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate committed R6 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --name-only --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate unstaged R6 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --cached --name-only `
            --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate staged R6 candidate paths."
    foreach ($pathSet in $pathSets) {
        foreach ($path in @($pathSet)) {
            if (-not [string]::IsNullOrWhiteSpace($path)) {
                $paths.Add($path.Replace("\", "/"))
            }
        }
    }
    foreach ($path in @(& git -C $RepositoryRoot ls-files --others `
            --exclude-standard)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $paths.Add($path.Replace("\", "/"))
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked R6 candidate paths."
    return @($paths | Sort-Object -Unique -CaseSensitive)
}

function Get-CommitCandidatePaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate sealed R6 candidate paths."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object { $_.Replace("\", "/") } |
        Sort-Object -Unique -CaseSensitive)
}

function Get-CandidatePaths {
    if ($script:R6BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeCandidatePaths)
    }
    Assert-Condition -Condition (
            $script:R6BoundaryMode -eq "TAGGED_DESCENDANT" -and
            $null -ne $script:R6AuthorityCommit) `
        -Message "The R6 source-boundary mode was not established."
    return @(Get-CommitCandidatePaths -Commit $script:R6AuthorityCommit)
}

function Get-TestMethodsFromSource {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $source = Get-SourceAuthorityText $RelativePath
    return @([regex]::Matches($source,
            '(?ms)^\s*@Test\s+(?:public\s+)?void\s+([A-Za-z0-9_]+)\s*\(') |
        ForEach-Object { $_.Groups[1].Value })
}

function Get-MatrixScenarioIds {
    $ids = [Collections.Generic.List[string]]::new()
    foreach ($line in (Get-SourceAuthorityText $MatrixPath) -split "`r?`n") {
        if ($line -match '^\|\s*(R6-[A-Z][0-9]+)\s*\|') {
            $ids.Add($Matches[1])
        }
    }
    return @($ids)
}

function Assert-EntryAuthority {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve R6 HEAD."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $head
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R6 entry commit is not an ancestor of HEAD."
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()

    $r6TagObject = ((@(& git -C $RepositoryRoot rev-parse `
        "refs/tags/$R6PassTagName" 2>$null) -join "")).Trim()
    $hasR6PassTag = $LASTEXITCODE -eq 0
    if ($hasR6PassTag) {
        $r6TagType = (& git -C $RepositoryRoot cat-file -t $r6TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $r6TagType -ceq "tag") `
            -Message "$R6PassTagName must remain annotated."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$r6TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to peel $R6PassTagName."
        $tagText = @(& git -C $RepositoryRoot cat-file tag $r6TagObject) `
            -join "`n"
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagText.Contains("G9U0-R6") -and
                $tagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "$R6PassTagName lacks the approved disposition."
        $closeoutRecord = @((& git -C $RepositoryRoot rev-list --parents `
            -n 1 $authorityCommit).Trim() -split '\s+')
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $closeoutRecord.Count -eq 2 -and
                $closeoutRecord[0] -ceq $authorityCommit -and
                $closeoutRecord[1] -ceq $EntrySha) `
            -Message "The sealed R6 closeout ancestry changed."
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $authorityCommit $head
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged R6 closeout."
        $script:R6BoundaryMode = "TAGGED_DESCENDANT"
        $script:R6AuthorityCommit = $authorityCommit
        Assert-ExactSet -Actual @(Get-CandidatePaths) `
            -Expected $ExpectedCandidatePaths `
            -Description "sealed R6 candidate inventory"
    } else {
        Assert-Condition -Condition ($head -ceq $EntrySha -and
                $branch -ceq $ExpectedBranch) `
            -Message ("Pre-commit R6 verification requires entry HEAD on " +
                "$ExpectedBranch; descendant verification requires its PASS tag.")
        $script:R6BoundaryMode = "WORKTREE"
    }

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "${S1PassTagName}^{tag}").Trim()
    $tagPeel = (& git -C $RepositoryRoot rev-parse `
        "${S1PassTagName}^{}").Trim()
    Assert-Condition -Condition ($tagObject -ceq $S1PassTagObject -and
            $tagPeel -ceq $EntrySha) `
        -Message "Sealed G9S1 PASS tag object/peel drifted."

    $protectedLocal = (& git -C $RepositoryRoot rev-parse `
        "refs/heads/$ProtectedG9U1Branch" 2>$null).Trim()
    if ($LASTEXITCODE -eq 0) {
        Assert-Condition -Condition ($protectedLocal -ceq $ProtectedG9U1Checkpoint) `
            -Message "Protected downstream G9U1 checkpoint drifted."
    }
}

function Assert-CandidateBoundary {
    $candidatePaths = @(Get-CandidatePaths)
    Assert-Condition -Condition ($candidatePaths.Count -gt 0) `
        -Message "R6 candidate inventory is empty."
    Assert-ExactSet -Actual $candidatePaths -Expected $ExpectedCandidatePaths `
        -Description "R6 frozen candidate path inventory"
    $allowedDesktopTest = $TestSourcePaths[
        "org.geocedg.desktop.G9U0R6NativeArchivePersistenceTest"]
    $forbidden = @($candidatePaths | Where-Object {
            ($_ -match '^source/(desktop|web)/' -and
                $_ -cne $allowedDesktopTest) -or
            $_ -match '^source/shared/common/src/main/java/org/geogebra/common/kernel/commands/' -or
            $_ -match '^\.github/prompts/tasks/g9u1-' -or
            $_ -match '^artifacts/'
        })
    Assert-Condition -Condition ($forbidden.Count -eq 0) `
        -Message ("R6 contains forbidden frontend/command/G9U1/artifact paths: " +
            ($forbidden -join ", "))
}

function Assert-CanonicalSourceHashingContract {
    $utf8 = [Text.UTF8Encoding]::new($false)
    [byte[]]$lf = $utf8.GetBytes("semantic-point`ninverse-authority`n")
    [byte[]]$crlf = $utf8.GetBytes("semantic-point`r`ninverse-authority`r`n")
    [byte[]]$mutated = $utf8.GetBytes("semantic-point`ninverse-Authority`n")
    $lfHash = Get-CanonicalLfSha256FromBytes -Bytes $lf
    $crlfHash = Get-CanonicalLfSha256FromBytes -Bytes $crlf
    $mutatedHash = Get-CanonicalLfSha256FromBytes -Bytes $mutated
    Assert-Condition -Condition ($lfHash -ceq $crlfHash) `
        -Message "R6 canonical source hashing is not LF/CRLF independent."
    Assert-Condition -Condition ($lfHash -cne $mutatedHash) `
        -Message "R6 canonical source hashing missed a semantic mutation."
}

function Assert-ScenarioAndEvidenceContracts {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object]$Evidence
    )

    Assert-Condition -Condition ($Scenarios.phase -ceq "G9U0-R6" -and
            $Scenarios.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            [int]$Scenarios.matrix.declaredRows -eq $ExpectedMatrixRows -and
            [bool]$Scenarios.approval.implementationStarted -and
            -not [bool]$Scenarios.approval.selfApproved -and
            [bool]$Scenarios.approval.authorApproved -and
            [bool]$Scenarios.approval.passClaimed -and
            $Scenarios.manualGuiSmoke.status -ceq
                "DEFERRED_TO_G9U1_BY_DESIGN" -and
            -not [bool]$Scenarios.manualGuiSmoke.requiredForR6 -and
            -not [bool]$Scenarios.manualGuiSmoke.passed -and
            $Scenarios.kernelDiagnosticAcceptance.status -ceq "PASS" -and
            $Scenarios.kernelDiagnosticAcceptance.acceptedValidationSurface -ceq
                "TEST_HOST_API" -and
            $Scenarios.kernelDiagnosticAcceptance.periodicSeamDrag -ceq
                "PASS_BIDIRECTIONAL_PATH_INDEPENDENT" -and
            $Scenarios.kernelDiagnosticAcceptance.unresolvedSeamControl -ceq
                "PASS_NO_MUTATION" -and
            $Scenarios.futureG9U1ManualSmoke.status -ceq
                "PLANNED_NOT_EXECUTED" -and
            @($Scenarios.futureG9U1ManualSmoke.steps).Count -eq 8 -and
            $Scenarios.protectedG9U1.branch -ceq $ProtectedG9U1Branch -and
            $Scenarios.protectedG9U1.checkpoint -ceq
                $ProtectedG9U1Checkpoint -and
            $Scenarios.protectedG9U1.promptCanonicalLfSha256 -ceq
                $ProtectedG9U1PromptHash -and
            -not [bool]$Scenarios.protectedG9U1.mergedIntoR6 -and
            -not [bool]$Scenarios.protectedG9U1.implementationAuthorized) `
        -Message "R6 approved scenario disposition drifted."
    Assert-ExactSet -Actual @($Scenarios.futureG9U1ManualSmoke.steps) `
        -Expected @(
            "POINT_TOOL_CLICK_LOCUS_V2_OR_SPLINE_V2",
            "CREATE",
            "DRAG",
            "PERIODIC_SEAM_CROSSING",
            "AMBIGUITY_CHOOSER",
            "TRANSFORMED_SOURCE",
            "K_ZERO",
            "SAVE_REOPEN"
        ) -Description "future G9U1 Point-tool manual-smoke steps"
    $matrixIds = @(Get-MatrixScenarioIds)
    $declaredIds = @($Scenarios.groups | ForEach-Object { $_.scenarioIds })
    Assert-Condition -Condition ($matrixIds.Count -eq $ExpectedMatrixRows) `
        -Message "R6 validation matrix row count drifted from $ExpectedMatrixRows."
    Assert-ExactSet -Actual $declaredIds -Expected $matrixIds `
        -Description "R6 scenario inventory"
    Assert-Condition -Condition (
            [int]$Scenarios.focusedJUnit.declared -eq $ExpectedFocusedCount -and
            [int]$Scenarios.focusedJUnit.sharedKernel -eq
                $ExpectedSharedKernelCount -and
            [int]$Scenarios.focusedJUnit.desktop -eq $ExpectedDesktopCount -and
            $Scenarios.focusedJUnit.focusedA.status -ceq "PASS" -and
            $Scenarios.focusedJUnit.focusedB.status -ceq
                "PASS_DETERMINISTIC_MATCH" -and
            [bool]$Scenarios.focusedJUnit.focusedB.mustMatchFocusedA) `
        -Message "R6 scenario focused/deterministic authority drifted."
    $scenarioClasses = [ordered]@{}
    foreach ($class in @($Scenarios.focusedJUnit.classes)) {
        $scenarioClasses[[string]$class.name] = [int]$class.methods
    }
    Assert-ExactSet -Actual @($scenarioClasses.Keys) `
        -Expected @($ExpectedTestMethods.Keys) `
        -Description "R6 scenario focused test classes"
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-Condition -Condition ($scenarioClasses[$className] -eq
                $ExpectedTestMethods[$className].Count) `
            -Message "R6 scenario method count drifted for $className."
    }
    $scenarioRisk = @($Scenarios.retainedRisks | Where-Object {
            $_.id -ceq $OpenPeriodicRisk -and
            $_.status -ceq "OPEN_TRACKED_NONBLOCKING"
        })
    Assert-Condition -Condition ($scenarioRisk.Count -eq 1) `
        -Message "R6 scenarios lost the retained periodic-quarantine risk."

    Assert-Condition -Condition ($Evidence.phase -ceq "G9U0-R6" -and
            $Evidence.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            $Evidence.provenance.entrySha -ceq $EntrySha -and
            $Evidence.provenance.g9s1PassTagObject -ceq $S1PassTagObject -and
            [bool]$Evidence.approval.implementationStarted -and
            -not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed -and
            -not [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -ceq "PASS_AUTHOR_APPROVED" -and
            $Evidence.manualGuiSmoke.status -ceq
                "DEFERRED_TO_G9U1_BY_DESIGN" -and
            -not [bool]$Evidence.manualGuiSmoke.requiredForR6 -and
            -not [bool]$Evidence.manualGuiSmoke.passed -and
            $Evidence.kernelDiagnosticAcceptance.status -ceq "PASS" -and
            $Evidence.kernelDiagnosticAcceptance.acceptedValidationSurface -ceq
                "TEST_HOST_API" -and
            $Evidence.kernelDiagnosticAcceptance.periodicSeamDrag -ceq
                "PASS_BIDIRECTIONAL_PATH_INDEPENDENT" -and
            $Evidence.kernelDiagnosticAcceptance.unresolvedSeamControl -ceq
                "PASS_NO_MUTATION" -and
            $Evidence.futureG9U1ManualSmoke.status -ceq
                "PLANNED_NOT_EXECUTED" -and
            $Evidence.futureG9U1ManualSmoke.route -ceq
                "POINT_TOOL_TO_CLICK_TO_CREATE_TO_DRAG_TO_SEAM_TO_CHOOSER_TO_TRANSFORM_TO_K_ZERO_TO_SAVE_REOPEN") `
        -Message "R6 approved evidence/entry disposition drifted."
    Assert-Condition -Condition (
            $Evidence.authority.canonicalPromptLfSha256 -ceq
                (Get-CanonicalLfSha256 $PromptPath) -and
            [int]$Evidence.authority.matrixRows -eq $ExpectedMatrixRows -and
            [int]$Evidence.authority.focusedJUnit -eq $ExpectedFocusedCount -and
            [int]$Evidence.authority.sharedKernelJUnit -eq
                $ExpectedSharedKernelCount -and
            [int]$Evidence.authority.desktopJUnit -eq $ExpectedDesktopCount) `
        -Message "R6 evidence prompt/matrix/focused authority drifted."

    $declaredClasses = [ordered]@{}
    foreach ($class in @($Evidence.authority.focusedTestClasses)) {
        $declaredClasses[[string]$class.name] = [int]$class.methods
    }
    Assert-ExactSet -Actual @($declaredClasses.Keys) `
        -Expected @($ExpectedTestMethods.Keys) `
        -Description "R6 evidence focused test classes"
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-Condition -Condition ($declaredClasses[$className] -eq
                $ExpectedTestMethods[$className].Count) `
            -Message "R6 evidence method count drifted for $className."
    }
    Assert-Condition -Condition (
            $Evidence.deterministicEvidence.sourceHashMethod -ceq
                "GIT_BLOB_OR_CURRENT_CANDIDATE_UTF8_NO_BOM_CANONICAL_LF_SHA256" -and
            -not [bool]$Evidence.deterministicEvidence.workingTreeLineEndingsAreAuthority -and
            [bool]$Evidence.deterministicEvidence.contentMutationMustChangeHash -and
            $Evidence.deterministicEvidence.focusedA.status -ceq "PASS" -and
            $Evidence.deterministicEvidence.focusedB.status -ceq
                "PASS_DETERMINISTIC_MATCH" -and
            [bool]$Evidence.deterministicEvidence.focusedB.mustMatchFocusedA) `
        -Message "R6 deterministic focused/hash evidence drifted."
    Assert-Condition -Condition (
            [bool]$Evidence.architecture.certifiedAffineWholeComponentCoverage -and
            [bool]$Evidence.architecture.durableSelectorSeparatedFromCurrentBindingCertificate -and
            [bool]$Evidence.architecture.evaluatorOnlyZeroOrOneFailsClosed -and
            [bool]$Evidence.architecture.boundedPolynomialCompositionDepth -and
            [bool]$Evidence.architecture.hostSnapshotMoveTransaction -and
            [bool]$Evidence.architecture.persistentRoleOwnedAuxiliaryPresentation -and
            [bool]$Evidence.architecture.exactPersistedComponentLineage -and
            [bool]$Evidence.architecture.exactEncodedPeriodicAddressAuthority -and
            [bool]$Evidence.architecture.bidirectionalPeriodicSeamDrag -and
            [bool]$Evidence.architecture.periodicSeamPathIndependence -and
            [bool]$Evidence.architecture.unresolvedPeriodicSeamLeavesPointUnchanged -and
            [bool]$Evidence.architecture.optionalAffineCaptureFailsClosedWithoutSemanticInvalidation) `
        -Message "R6 strengthened architecture evidence drifted."
    Assert-Condition -Condition (
            $Evidence.protectedG9U1.branch -ceq $ProtectedG9U1Branch -and
            $Evidence.protectedG9U1.checkpoint -ceq $ProtectedG9U1Checkpoint -and
            $Evidence.protectedG9U1.promptCanonicalLfSha256 -ceq
                $ProtectedG9U1PromptHash -and
            -not [bool]$Evidence.protectedG9U1.mergedIntoR6 -and
            -not [bool]$Evidence.protectedG9U1.implementationAuthorized) `
        -Message "Protected downstream G9U1 evidence drifted."
    $risk = @($Evidence.retainedRisks | Where-Object {
            $_.id -ceq $OpenPeriodicRisk -and
            $_.status -ceq "OPEN_TRACKED_NONBLOCKING"
        })
    Assert-Condition -Condition ($risk.Count -eq 1) `
        -Message "The retained R4 periodic-quarantine risk was lost or closed."
}

function Assert-ManifestContract {
    $actual = @{}
    foreach ($line in (Get-SourceAuthorityText $ManifestPath) -split "`r?`n") {
        if ($line -match '^([0-9a-f]{64})\s{2}(.+)$') {
            $actual[$Matches[2]] = $Matches[1]
        }
    }
    $expectedPaths = @($EvidencePath, $ScenarioPath)
    Assert-ExactSet -Actual @($actual.Keys) -Expected $expectedPaths `
        -Description "R6 evidence manifest paths"
    foreach ($path in $expectedPaths) {
        Assert-Condition -Condition ($actual[$path] -ceq
                (Get-CanonicalLfSha256 $path)) `
            -Message "R6 evidence manifest hash drifted for $path."
    }
}

function Assert-ProductStaticContracts {
    foreach ($path in @($RequiredProductSourcePaths + @($TestSourcePaths.Values))) {
        [void](Resolve-RequiredFile $path)
    }
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-ExactSet `
            -Actual @(Get-TestMethodsFromSource $TestSourcePaths[$className]) `
            -Expected @($ExpectedTestMethods[$className]) `
            -Description "R6 focused test methods for $className"
    }

    $status = Get-SourceAuthorityText `
        "$InteractionSourceDirectory/LocusPointInteractionStatus2D.java"
    foreach ($name in @("NO_ADMISSIBLE_PREIMAGE", "UNIQUE_ADMISSIBLE_PREIMAGE",
            "MULTIPLE_SEMANTIC_PREIMAGES", "UNRESOLVED_NUMERICAL_SEARCH",
            "INVALID_SOURCE", "DEGENERATE_SOURCE_IMAGE",
            "UNSUPPORTED_CAPABILITY")) {
        Assert-Condition -Condition ($status -match "\b$name\b") `
            -Message "R6 typed inverse status is missing: $name"
    }
    $candidate = Get-SourceAuthorityText `
        "$InteractionSourceDirectory/LocusPointInteractionCandidate2D.java"
    $resolver = Get-SourceAuthorityText `
        "$InteractionSourceDirectory/LocusPointInteractionResolver2D.java"
    $operations = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java"
    $isolation = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2D.java"
    $targetCapability = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialTargetIntersectionCapability2D.java"
    $pairCapability = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java"
    $pairContext = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionContext2D.java"
    $polynomial = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/PiecewisePolynomialLocus2D.java"
    $addressAlgorithm = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSemanticLocusPoint2D.java"
    $similarityEvaluator = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java"
    $redefineProvider = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/ConstructionGeoRedefineProvider.java"
    $identityRegistry = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java"
    $construction = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java"
    Assert-Condition -Condition ($candidate -match 'LocusSemanticAddress2D' -and
            $candidate -match 'sourceRevision' -and
            $candidate -match 'worldDistance' -and
            $resolver -match 'CertifiedAffineLocus2D' -and
            $resolver -match 'ALL_CERTIFIED_AFFINE_COMPONENTS' -and
            $resolver -match 'CERTIFIED_AFFINE_PROJECTION' -and
            $resolver -match 'establishesCompleteRequestedScope' -and
            $resolver -match 'PiecewisePolynomialLocus2D' -and
            $resolver -match 'evaluatorSearch' -and
            $operations -match 'createInteractiveSemanticPoint' -and
            $operations -match 'moveInteractiveSemanticPoint' -and
            $operations -match 'runAtomicConstructionMutation' -and
            $operations -match 'INTERACTION_POINT_OUTPUT_ROLE' -and
            $isolation -match 'class\s+PolynomialRootIsolation2D' -and
            $targetCapability -match 'PolynomialRootIsolation2D\.isolate' -and
            $polynomial -match 'getPolynomialCoordinateCoefficients' -and
            $polynomial -match 'getPolynomialCompositionDepth' -and
            $polynomial -match 'MAXIMUM_SAFE_COMPOSITION_DEPTH' -and
            $pairCapability -match 'getPolynomialCoordinateCoefficients' -and
            $pairContext -match 'MAXIMUM_SAFE_COMPOSITION_DEPTH' -and
            $construction -match 'runAtomicConstructionMutation' -and
            $redefineProvider -match 'supportsStableOutputRole' -and
            $redefineProvider -match 'hasDedicatedInteractionPointState' -and
            $redefineProvider -match 'LOCUS_INTERACTION_POINT' -and
            $identityRegistry -match 'supportsStableOutputRole' -and
            $identityRegistry -match 'restoreOwnedInputPresentation' -and
            $addressAlgorithm -match 'componentLineage' -and
            $addressAlgorithm -match 'lastAcceptedAddress' -and
            $addressAlgorithm -match 'getCurrentSemanticAddress' -and
            $addressAlgorithm -match 'persistedAddressMatchesInputs' -and
            $addressAlgorithm -match 'expectedRaw' -and
            $similarityEvaluator -match 'captureCertifiedAffine' -and
            $similarityEvaluator -match 'nonfiniteCertificate' -and
            $similarityEvaluator -match 'Collections\.emptyMap' -and
            $addressAlgorithm -match 'restoreOwnedInputPresentation') `
        -Message "R6 inverse/point/polynomial shared-kernel seam is incomplete."

    $interactionSources = ($RequiredProductSourcePaths | Where-Object {
            $_ -like "$InteractionSourceDirectory/*"
        } | ForEach-Object { Get-SourceAuthorityText $_ }) -join "`n"
    Assert-Condition -Condition ($interactionSources -notmatch
            'import\s+org\.geogebra\.common\.euclidian|LocusRenderCache|implements\s+Path') `
        -Message "R6 semantic authority leaked into Euclidian/render/Path state."
    $geoLocus = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java"
    Assert-Condition -Condition ($geoLocus -notmatch
            'class\s+GeoLocusV2[^\{]*implements[^\{]*\bPath\b') `
        -Message "R6 must not make GeoLocusV2 a generic Path."

    $commandChanges = @(Get-CandidatePaths | Where-Object {
            $_ -match '^source/shared/common/src/main/java/org/geogebra/common/kernel/commands/'
        })
    Assert-Condition -Condition ($commandChanges.Count -eq 0) `
        -Message "R6 added or modified a public command seam."
}

function Assert-DocumentationContracts {
    $combined = @($AdrPath, $SpecPath, $ArchitecturePath,
        $MatrixPath, $ReportPath) | ForEach-Object {
            Get-SourceAuthorityText $_
        } | Join-String -Separator "`n"
    Assert-Condition -Condition ($combined -match
            'PASS.+AUTHOR APPROVED' -and
            $combined -match 'selfApproved\s*[=:]\s*false' -and
            $combined -match 'authorApproved\s*[=:]\s*true' -and
            $combined -match 'passClaimed\s*[=:]\s*true' -and
            $combined -match 'manualGuiSmoke\s*[=:]\s*.*DEFERRED TO G9U1 BY DESIGN' -and
            $combined -match 'kernelDiagnosticAcceptance\s*[=:]\s*.*PASS' -and
            $combined -match 'GeoLocusV2' -and
            $combined -match 'SplineV2' -and
            $combined -match 'LocusPointInteractionResolver2D' -and
            $combined -match 'createInteractiveSemanticPoint' -and
            $combined -match 'moveInteractiveSemanticPoint' -and
            $combined -match [regex]::Escape($OpenPeriodicRisk)) `
        -Message "R6 approved/risk documentation contract drifted."
    $roadmap = Get-SourceAuthorityText "docs/roadmap/geocedg_roadmap.md"
    Assert-Condition -Condition ($roadmap -match 'G9S1.+G9U0-R6.+G9U1' -and
            $roadmap -match 'G9U0-R6' -and
            $roadmap -match 'G9U1.+NOT AUTHORIZED') `
        -Message "Living roadmap does not place approved R6 before unauthorized G9U1."
    $composed = Get-SourceAuthorityText $ComposedVerifierPath
    $s1Index = $composed.IndexOf('& $G9S1SemanticSplineVerifier',
        [StringComparison]::Ordinal)
    $r6Index = $composed.IndexOf(
        '& $G9U0R6SemanticLocusPointInteractionVerifier',
        [StringComparison]::Ordinal)
    Assert-Condition -Condition ($s1Index -ge 0 -and $r6Index -gt $s1Index) `
        -Message "Composed verification must execute R6 after G9S1."
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [string]$LogFileName
    )

    $effectiveArguments = @($Arguments)
    if (-not $AllowToolchainDownload) {
        $effectiveArguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogFileName
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $effectiveArguments -LogPath $logPath `
            -Description $Description -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $effectiveArguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $effectiveArguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Push-Location $RepositoryRoot
    try {
        & $RootGradle @effectiveArguments 2>&1 |
            Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "$Description failed with exit code $exitCode; log: $logPath"
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
            $_.name -replace '\(.*\)$', ''
        })
    Assert-ExactSet -Actual $methods -Expected $ExpectedMethods `
        -Description "Executed R6 methods for $ClassName"
    Assert-Condition -Condition (
            [int]$suite.tests -eq $ExpectedMethods.Count -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "R6 JUnit result is not clean for $ClassName."
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
            Resolve-RequiredFile $RelativePath) -Pattern '<error ')
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle errors."
}

function Write-CanonicalSummary {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object[]]$TestResults
    )

    $candidatePaths = @(Get-CandidatePaths)
    $sourcePaths = @($candidatePaths | Where-Object {
            $_ -match '^source/' -and $_ -match '\.(java|properties)$'
        } | Sort-Object -CaseSensitive)
    $sourceHashes = @($sourcePaths | ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-DeterministicSourceSha256 $_
            }
        })
    $authorityHashes = @($PromptPath, $AdrPath, $SpecPath,
        $ArchitecturePath, $MatrixPath, $ScenarioPath, $EvidencePath |
        Sort-Object -CaseSensitive | ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-CanonicalLfSha256 $_
            }
        })
    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9U0-R6"
        status = "PASS_AUTHOR_APPROVED"
        entrySha = $EntrySha
        g9s1PassTagObject = $S1PassTagObject
        scenarioIds = @($Scenarios.groups | ForEach-Object {
                $_.scenarioIds
            } | Sort-Object -CaseSensitive)
        focusedJUnit = $ExpectedFocusedCount
        focusedJUnitByModule = [ordered]@{
            sharedKernel = $ExpectedSharedKernelCount
            desktop = $ExpectedDesktopCount
        }
        testResults = @($TestResults | Sort-Object { $_.class })
        candidatePaths = $candidatePaths
        deterministicSourceHashes = $sourceHashes
        authorityHashes = $authorityHashes
        protectedG9U1Checkpoint = $ProtectedG9U1Checkpoint
        openRiskIds = @($OpenPeriodicRisk)
        selfApproved = $false
        authorApproved = $true
        passClaimed = $true
    }
    $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
    [void](New-Item -ItemType Directory -Path (
            Split-Path -Parent $CanonicalSummaryPath) -Force)
    [IO.File]::WriteAllText($CanonicalSummaryPath, $json,
        [Text.UTF8Encoding]::new($false))
    $summaryHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
        $CanonicalSummaryPath).Hash.ToLowerInvariant()
    Write-Host "Canonical R6 approved summary: $CanonicalSummaryPath"
    Write-Host "Canonical R6 approved summary SHA-256: $summaryHash"
    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "R6 comparison summary is missing."
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message "Deterministic R6 approved summary mismatch."
        Write-Host "Deterministic R6 approved-summary comparison: MATCH"
    }
}

$GeneratedState = $null
$InitialStatus = $null
$Failure = $null
$FailureContext = $null

try {
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."
    foreach ($path in @($PromptPath, $AdrPath, $SpecPath, $ArchitecturePath,
            $MatrixPath, $ReportPath, $ScenarioPath, $EvidencePath,
            $ManifestPath, $VerifierRelativePath, $ComposedVerifierPath)) {
        [void](Resolve-RequiredFile $path)
    }

    Assert-EntryAuthority
    Assert-CandidateBoundary
    Assert-CanonicalSourceHashingContract
    $scenarios = Read-JsonDocument $ScenarioPath
    $evidence = Read-JsonDocument $EvidencePath
    Assert-ScenarioAndEvidenceContracts -Scenarios $scenarios `
        -Evidence $evidence
    Assert-ManifestContract
    Assert-ProductStaticContracts
    Assert-DocumentationContracts

    if (-not $HistoricalRegressionsAlreadyComposed) {
        & $S1VerifierPath -SkipBuild `
            -LogDirectory (Join-Path $LogDirectory "sealed-g9s1")
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Sealed descendant-safe G9S1 authority failed."
    }
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9U0-R6."
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9U0-R6."

    if ($SkipBuild) {
        Write-Host "G9U0-R6 static approved-authority verification completed."
        Write-Host "G9U0-R6 = PASS — AUTHOR APPROVED"
        Write-Host "manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN"
        Write-Host "kernelDiagnosticAcceptance = PASS"
    } else {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedState = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9u0-r6" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
        $testArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($className in @($ExpectedTestMethods.Keys | Where-Object {
                    $_ -notin $DesktopTestClasses
                })) {
            $testArguments += @("--tests", $className)
        }
        $testArguments += @(
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $testArguments `
            -Description "G9U0-R6 focused common-jre tests" `
            -LogFileName "g9u0-r6-focused-common-jre-gradle.log"

        $desktopTestArguments = @(
            ":desktop:desktop:test", "--tests", $DesktopTestClasses[0],
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $desktopTestArguments `
            -Description "G9U0-R6 focused Desktop persistence tests" `
            -LogFileName "g9u0-r6-focused-desktop-gradle.log"

        $checkstyleArguments = @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $checkstyleArguments `
            -Description "G9U0-R6 affected-module Checkstyle" `
            -LogFileName "g9u0-r6-checkstyle-gradle.log"

        $results = @()
        foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
            $results += Get-TestResult -ClassName $entry.Key `
                -ExpectedMethods @($entry.Value)
        }
        $total = (@($results | ForEach-Object { $_.tests }) |
            Measure-Object -Sum).Sum
		Assert-Condition -Condition ($total -eq $ExpectedFocusedCount) `
			-Message "R6 focused JUnit count drifted from $ExpectedFocusedCount."
        foreach ($path in @(
                "source/shared/common/build/reports/checkstyle/main.xml",
                "source/shared/common-jre/build/reports/checkstyle/test.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $path
        }
        Write-CanonicalSummary -Scenarios $scenarios -TestResults $results
		Write-Host (("G9U0-R6 focused result: {0}/{0} JUnit, " +
			"Checkstyle clean.") -f $ExpectedFocusedCount)
        Write-Host "G9U0-R6 = PASS — AUTHOR APPROVED"
        Write-Host "manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN"
        Write-Host "kernelDiagnosticAcceptance = PASS"
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9U0-R6 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during R6 verification.`n" +
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
    $message = $Failure.Message
    if (-not [string]::IsNullOrWhiteSpace($FailureContext)) {
        $message += "`n$FailureContext"
    }
    Write-Error $message
    exit 1
}

exit 0

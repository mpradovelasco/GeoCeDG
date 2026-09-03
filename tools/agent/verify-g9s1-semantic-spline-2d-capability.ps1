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
        "geocedg-verify-g9s1-semantic-spline-2d-capability")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "5952dfdbd238e71e598f4d2ca92c3e03437df41c"
$ExpectedBranch = "feature/g9s1-semantic-spline-2d-capability"
$S1PassTagName = "geocedg-g9s1-pass"
$R5PassTagName = "geocedg-g9u0-r5-pass"
$R5PassTagObject = "3712595fe2b168ba494379b6b3f0051e4122cfae"
$R5PassCommit = "5952dfdbd238e71e598f4d2ca92c3e03437df41c"
$OpenPeriodicRisk = "G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP"

$PromptPath = ".github/prompts/tasks/g9s1-semantic-spline-2d-capability.prompt.md"
$FutureG9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md"
$AdrPath = "docs/adr/0018-semantic-spline-2d-capability.md"
$SpecPath = "geocedg/specs/curves/semantic-spline-2d.md"
$ArchitecturePath = "docs/architecture/g9s1_semantic_spline_2d_capability.md"
$ResearchPath = "docs/research/g9s1_semantic_spline_numerical_methods.md"
$MatrixPath =
    "docs/validation/g9s1_semantic_spline_2d_capability_validation_matrix.md"
$TraceabilityPath =
    "docs/validation/g9s1_semantic_spline_2d_scientific_traceability.md"
$ReportPath =
    "docs/validation/g9s1_semantic_spline_2d_capability_candidate_report.md"
$ScenarioPath =
    "geocedg/validation/g9s1/g9s1-semantic-spline-2d-scenarios.json"
$EvidencePath =
    "geocedg/validation/g9s1/g9s1-semantic-spline-2d-evidence.json"
$ManifestPath = "geocedg/validation/g9s1/g9s1-evidence.sha256"
$VerifierRelativePath =
    "tools/agent/verify-g9s1-semantic-spline-2d-capability.ps1"
$ComposedVerifierPath = "tools/agent/verify.ps1"
$R5VerifierPath = Join-Path $PSScriptRoot `
    "verify-g9u0-r5-locus-v2-similarity-transformations.ps1"

$CoreProductSourcePaths = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusBetweenMetricV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSplineV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusParameterPartition2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/PiecewisePolynomialLocus2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/EvaluatorOnlyLocusMetricCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplinePolynomialModel2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplineSemanticEvaluator2D.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLength.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdSplineV2.java"
)
$TestSourcePaths = [ordered]@{
    "org.geocedg.common.locus.G9S1SemanticSpline2DTest" =
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1SemanticSpline2DTest.java"
    "org.geocedg.common.locus.G9S1SplineIntersectionEfficiencyTest" =
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1SplineIntersectionEfficiencyTest.java"
    "org.geocedg.common.kernel.locus.intersection.PiecewisePolynomialPairIntersectionCapability2DTest" =
        "source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2DTest.java"
    "org.geocedg.desktop.G9S1NativeArchivePersistenceTest" =
        "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9S1NativeArchivePersistenceTest.java"
}
$ExpectedTestMethods = [ordered]@{
    "org.geocedg.common.locus.G9S1SemanticSpline2DTest" = @(
        "s01ToS03LineCircleAndConicProduceRichResults",
        "s04MultipleRootsAcrossSpansRemainDistinct",
        "s05RootAtKnotHasCanonicalSingleOwnership",
        "s06AndS07TangencyAndNearTangencyRemainTyped",
        "s08SelfIntersectionPreservesDistinctSemanticPreimages",
        "s09ToS11SplinePairImplicitAndLocusPairUseSemanticAuthority",
        "s12ConsumerEnumerationCannotReplaceExactTokens",
        "s13AndS14DynamicPathsPreserveMaterializedBindings",
        "s15AndS16TopologyTransitionDormancyAndReactivationFailClosed",
        "s17AndS18TotalAndPartialLengthUseSemanticAddresses",
        "partialLengthScalarRoutesThroughTheRichSemanticAuthority",
        "partialLengthScalarRecomputesAndFailsClosedWithoutMetricIntersectionCoupling",
        "partialLengthScalarIsSimilarityCovariantIncludingCollapsedImage",
        "partialLengthScalarUndoRedoCopyAndClassicRoutingRemainStable",
        "s19SelfIntersectionRequiresAddressedMetricEndpoints",
        "s20ToS22SimilarityClosureMetricAndIntersectionCovariance",
        "s23AndS24DefiningPointsAndTargetRemainDynamic",
        "s25SaveReopenReconstructsAuthoritativeInputsAndChildren",
        "s26UndoRedoRestoresSemanticIdentityGraph",
        "s27CopyRemapsSplineAndDependencySlice",
        "s28ClassicSplineCompatibilityRemainsUnchanged",
        "closedPeriodicSplineHasSeamAwareMetricsAndIntersection",
        "invalidDegreeWeightAndZeroLengthInputsFailClosed",
        "s29FunctionalWorkIsBoundedAndDeterministic",
        "s30SemanticConsumersNeverReadRenderAuthority"
    )
    "org.geocedg.common.locus.G9S1SplineIntersectionEfficiencyTest" = @(
        "explicitSpanCountersAreDeterministicAndBounded",
        "noRootSpansAreCountedWithoutFabricatingCandidates",
        "spanAndRawRootCountersEnforceExistingWorkCeilings"
    )
    "org.geocedg.common.kernel.locus.intersection.PiecewisePolynomialPairIntersectionCapability2DTest" = @(
        "multipleRootsInsideOneLegacyGridCellAreFoundSemantically",
        "callerOperandSwapKeepsCanonicalSemanticPairs",
        "transversePairRefinementIsInvariantUnderUniformScale",
        "canonicalKnotAndPeriodicSeamOwnershipPreventDuplicates",
        "tangencyRemainsRichOnlyAndFailClosed",
        "polynomialOverlapNeverManufacturesFiniteRoots",
        "pairBoxBudgetFailsCoherently"
    )
    "org.geocedg.desktop.G9S1NativeArchivePersistenceTest" = @(
        "nativeCedgReopensSplineConsumersAndExactTokenPoint",
        "featureOffAndClassicPreserveSplineWithoutCreation"
    )
}
$DesktopTestClasses = @(
    "org.geocedg.desktop.G9S1NativeArchivePersistenceTest"
)
$ExpectedSharedKernelCount = (@($ExpectedTestMethods.GetEnumerator() |
        Where-Object { $_.Key -notin $DesktopTestClasses } |
        ForEach-Object { $_.Value.Count }) | Measure-Object -Sum).Sum
$ExpectedDesktopCount = (@($ExpectedTestMethods.GetEnumerator() |
        Where-Object { $_.Key -in $DesktopTestClasses } |
        ForEach-Object { $_.Value.Count }) | Measure-Object -Sum).Sum
$ExpectedFocusedCount = $ExpectedSharedKernelCount + $ExpectedDesktopCount
$ExpectedMatrixRows = 59
$ExpectedSealedCandidatePaths = @(
    ".github/prompts/tasks/g9s1-semantic-spline-2d-capability.prompt.md",
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md",
    "docs/adr/0018-semantic-spline-2d-capability.md",
    "docs/architecture/g9s1_semantic_spline_2d_capability.md",
    "docs/developer/geocedg_developer_guide.md",
    "docs/developer/locus_v2_metric_api.md",
    "docs/developer/semantic_spline_2d_api.md",
    "docs/research/g9s1_semantic_spline_numerical_methods.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/upstream/modified-files.yml",
    "docs/user/geocedg_user_guide.md",
    "docs/validation/g9_documentation_bundle_traceability.md",
    "docs/validation/g9_public_workspace_validation_matrix.md",
    "docs/validation/g9s1_semantic_spline_2d_capability_candidate_report.md",
    "docs/validation/g9s1_semantic_spline_2d_capability_validation_matrix.md",
    "docs/validation/g9s1_semantic_spline_2d_scientific_traceability.md",
    "geocedg/specs/curves/semantic-spline-2d.md",
    "geocedg/specs/README.md",
    "geocedg/validation/g9s1/g9s1-evidence.sha256",
    "geocedg/validation/g9s1/g9s1-semantic-spline-2d-evidence.json",
    "geocedg/validation/g9s1/g9s1-semantic-spline-2d-scenarios.json",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9S1NativeArchivePersistenceTest.java",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/command_en.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/command_es.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/command.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_en.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_es.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties",
    "source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2DTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1SemanticSpline2DTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1SplineIntersectionEfficiencyTest.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusBetweenMetricV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSplineV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ExtendedTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionCapabilityContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionInstrumentation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionInstrumentationSnapshot2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTargets2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionSolver2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/NondegenerateConicIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RegularPolynomialImplicitIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDefinition2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusParameterPartition2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityTransform2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/DifferentialLocusMetricCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/EvaluatorOnlyLocusMetricCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusDifferentialEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricIntegrator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/PiecewisePolynomialLocus2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplinePolynomialModel2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplineSemanticEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/main/feature/RuntimeFeatureService.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/BasicCommandProcessorFactory.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLength.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdSplineV2.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CommandDispatcher.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/Commands.java",
    "tools/agent/verify-g9s1-semantic-spline-2d-capability.ps1",
    "tools/agent/verify-g9u0-r5-locus-v2-similarity-transformations.ps1",
    "tools/agent/verify.ps1"
)
$script:S1BoundaryMode = "UNSET"
$script:S1AuthorityCommit = $null
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
        -Message "Required G9S1 file is missing: $RelativePath"
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
            -Message "Unable to read G9S1 Git blob ${Object}: $($errorText.Trim())"
        return ,([byte[]]$memory.ToArray())
    } finally {
        $memory.Dispose()
        $process.Dispose()
    }
}

function Get-SourceAuthorityBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [void](Resolve-RepositoryPath $RelativePath)
    if ($script:S1BoundaryMode -ne "TAGGED_DESCENDANT" -or
            $null -eq $script:S1AuthorityCommit) {
        return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
    }
    $normalized = $RelativePath.Replace("\", "/")
    return ,(Get-GitBlobBytes -Object "$($script:S1AuthorityCommit):$normalized")
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

    if ($script:S1BoundaryMode -eq "TAGGED_DESCENDANT") {
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
        throw "Unable to classify G9S1 source authority for $RelativePath."
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
        -Message "Unable to enumerate committed G9S1 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --name-only --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate unstaged G9S1 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --cached --name-only `
            --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate staged G9S1 candidate paths."
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
        -Message "Unable to enumerate untracked G9S1 candidate paths."
    return @($paths | Sort-Object -Unique -CaseSensitive)
}

function Get-CommitCandidatePaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate sealed G9S1 candidate paths."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object { $_.Replace("\", "/") } |
        Sort-Object -Unique -CaseSensitive)
}

function Get-CandidatePaths {
    if ($script:S1BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeCandidatePaths)
    }
    Assert-Condition -Condition (
            $script:S1BoundaryMode -eq "TAGGED_DESCENDANT" -and
            $null -ne $script:S1AuthorityCommit) `
        -Message "The G9S1 source-boundary mode was not established."
    return @(Get-CommitCandidatePaths -Commit $script:S1AuthorityCommit)
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
        if ($line -match '^\|\s*(S1-[A-Z][0-9]+)\s*\|') {
            $ids.Add($Matches[1])
        }
    }
    return @($ids)
}

function Assert-EntryAuthority {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve G9S1 HEAD."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $head
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "G9S1 entry commit is not an ancestor of HEAD."
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()

    $s1TagObject = ((@(& git -C $RepositoryRoot rev-parse `
        "refs/tags/$S1PassTagName" 2>$null) -join "")).Trim()
    $hasS1PassTag = $LASTEXITCODE -eq 0
    if ($hasS1PassTag) {
        $s1TagType = (& git -C $RepositoryRoot cat-file -t $s1TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $s1TagType -ceq "tag") `
            -Message "$S1PassTagName must remain annotated."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$s1TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to peel $S1PassTagName."
        $tagText = @(& git -C $RepositoryRoot cat-file tag $s1TagObject) `
            -join "`n"
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagText.Contains("G9S1") -and
                $tagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "$S1PassTagName lacks the approved disposition."
        $closeoutRecord = @((& git -C $RepositoryRoot rev-list --parents `
            -n 1 $authorityCommit).Trim() -split '\s+')
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $closeoutRecord.Count -eq 2 -and
                $closeoutRecord[0] -ceq $authorityCommit -and
                $closeoutRecord[1] -ceq $EntrySha) `
            -Message "The sealed G9S1 closeout ancestry changed."
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $authorityCommit $head
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged G9S1 closeout."
        $script:S1BoundaryMode = "TAGGED_DESCENDANT"
        $script:S1AuthorityCommit = $authorityCommit
        Assert-ExactSet -Actual @(Get-CandidatePaths) `
            -Expected $ExpectedSealedCandidatePaths `
            -Description "sealed G9S1 candidate inventory"
    } else {
        Assert-Condition -Condition ($head -ceq $EntrySha -and
                $branch -ceq $ExpectedBranch) `
            -Message ("Pre-commit G9S1 verification requires entry HEAD on " +
                "$ExpectedBranch; descendant verification requires its PASS tag.")
        $script:S1BoundaryMode = "WORKTREE"
    }

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "${R5PassTagName}^{tag}").Trim()
    $tagPeel = (& git -C $RepositoryRoot rev-parse `
        "${R5PassTagName}^{}").Trim()
    Assert-Condition -Condition ($tagObject -ceq $R5PassTagObject -and
            $tagPeel -ceq $R5PassCommit) `
        -Message "Sealed R5 PASS tag object/peel drifted."
}

function Assert-CanonicalSourceHashingContract {
    $utf8 = [Text.UTF8Encoding]::new($false)
    [byte[]]$lf = $utf8.GetBytes("semantic-spline`nspan-authority`n")
    [byte[]]$crlf = $utf8.GetBytes("semantic-spline`r`nspan-authority`r`n")
    [byte[]]$mutated = $utf8.GetBytes("semantic-spline`nspan-Authority`n")
    $lfHash = Get-CanonicalLfSha256FromBytes -Bytes $lf
    $crlfHash = Get-CanonicalLfSha256FromBytes -Bytes $crlf
    $mutatedHash = Get-CanonicalLfSha256FromBytes -Bytes $mutated
    Assert-Condition -Condition ($lfHash -ceq $crlfHash) `
        -Message "G9S1 canonical source hashing is not LF/CRLF independent."
    Assert-Condition -Condition ($lfHash -cne $mutatedHash) `
        -Message "G9S1 canonical source hashing missed a content mutation."
}

function Assert-ScenarioAndEvidenceContracts {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object]$Evidence
    )

    Assert-Condition -Condition ($Scenarios.phase -ceq "G9S1" -and
            $Scenarios.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            [int]$Scenarios.matrix.declaredRows -eq $ExpectedMatrixRows -and
            [int]$Scenarios.focusedJUnit.declared -eq $ExpectedFocusedCount -and
            [int]$Scenarios.focusedJUnit.sharedKernel -eq
                $ExpectedSharedKernelCount -and
            [int]$Scenarios.focusedJUnit.desktop -eq $ExpectedDesktopCount -and
            $Scenarios.focusedJUnit.focusedA.status -ceq "PASS" -and
            $Scenarios.focusedJUnit.focusedB.status -ceq
                "PASS_DETERMINISTIC_MATCH" -and
            [bool]$Scenarios.focusedJUnit.focusedB.mustMatchFocusedA -and
            -not [bool]$Scenarios.approval.selfApproved -and
            [bool]$Scenarios.approval.authorApproved -and
            [bool]$Scenarios.approval.passClaimed -and
            $Scenarios.manualAuthorSmoke.status -ceq "PASS" -and
            [bool]$Scenarios.manualAuthorSmoke.passed -and
            $Scenarios.manualAuthorSmoke.partialLengthLocusControl -ceq
                "GENERATOR_MAIN_LM_2") `
        -Message "G9S1 approved scenario/smoke contract drifted."

    $declaredClasses = [ordered]@{}
    foreach ($class in @($Scenarios.focusedJUnit.classes)) {
        $declaredClasses[[string]$class.name] = [int]$class.methods
    }
    Assert-ExactSet -Actual @($declaredClasses.Keys) `
        -Expected @($ExpectedTestMethods.Keys) `
        -Description "G9S1 declared focused test classes"
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-Condition -Condition ($declaredClasses[$className] -eq
                $ExpectedTestMethods[$className].Count) `
            -Message "G9S1 declared method count drifted for $className."
    }

    $matrixIds = @(Get-MatrixScenarioIds)
    $declaredIds = @($Scenarios.groups | ForEach-Object { $_.scenarioIds })
    Assert-Condition -Condition ($matrixIds.Count -eq $ExpectedMatrixRows) `
        -Message "G9S1 validation matrix row count drifted from $ExpectedMatrixRows."
    Assert-ExactSet -Actual $declaredIds -Expected $matrixIds `
        -Description "G9S1 scenario inventory"

    Assert-Condition -Condition ($Evidence.phase -ceq "G9S1" -and
            $Evidence.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            $Evidence.provenance.entrySha -ceq $EntrySha -and
            $Evidence.provenance.r5PassTagObject -ceq $R5PassTagObject -and
            -not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed -and
            $Evidence.manualAuthorSmoke.status -ceq "PASS" -and
            [bool]$Evidence.manualAuthorSmoke.passed -and
            $Evidence.manualAuthorSmoke.partialLengthSplineControl -ceq
                "M_4_MP_2" -and
            $Evidence.manualAuthorSmoke.partialLengthLocusControl -ceq
                "GENERATOR_MAIN_LM_2") `
        -Message "G9S1 approved evidence/entry/smoke contract drifted."
    Assert-Condition -Condition (
            $Evidence.authority.canonicalPromptLfSha256 -ceq
                (Get-CanonicalLfSha256 $PromptPath) -and
            $Evidence.authority.futureG9U1PromptLfSha256 -ceq
                (Get-CanonicalLfSha256 $FutureG9U1PromptPath) -and
            [int]$Evidence.authority.matrixRows -eq $ExpectedMatrixRows -and
            [int]$Evidence.authority.focusedJUnit -eq $ExpectedFocusedCount -and
            [int]$Evidence.authority.sharedKernelJUnit -eq
                $ExpectedSharedKernelCount -and
            [int]$Evidence.authority.desktopJUnit -eq $ExpectedDesktopCount) `
        -Message "G9S1 prompt/matrix/focused evidence authority drifted."
    $evidenceClasses = [ordered]@{}
    foreach ($class in @($Evidence.authority.focusedTestClasses)) {
        $evidenceClasses[[string]$class.name] = [int]$class.methods
    }
    Assert-ExactSet -Actual @($evidenceClasses.Keys) `
        -Expected @($ExpectedTestMethods.Keys) `
        -Description "G9S1 evidence focused test classes"
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-Condition -Condition ($evidenceClasses[$className] -eq
                $ExpectedTestMethods[$className].Count) `
            -Message "G9S1 evidence method count drifted for $className."
    }
    Assert-Condition -Condition (
            $Evidence.deterministicEvidence.focusedA.status -ceq "PASS" -and
            $Evidence.deterministicEvidence.focusedB.status -ceq
                "PASS_DETERMINISTIC_MATCH" -and
            [bool]$Evidence.deterministicEvidence.focusedB.mustMatchFocusedA) `
        -Message "G9S1 frozen focused A/B evidence contract drifted."
    Assert-Condition -Condition (
            $Evidence.deterministicEvidence.sourceHashMethod -ceq
                "GIT_BLOB_OR_CURRENT_CANDIDATE_UTF8_NO_BOM_CANONICAL_LF_SHA256" -and
            -not [bool]$Evidence.deterministicEvidence.workingTreeLineEndingsAreAuthority -and
            [bool]$Evidence.deterministicEvidence.contentMutationMustChangeHash) `
        -Message "G9S1 deterministic source-hash contract drifted."
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
        -Description "G9S1 evidence manifest paths"
    foreach ($path in $expectedPaths) {
        Assert-Condition -Condition ($actual[$path] -ceq
                (Get-CanonicalLfSha256 $path)) `
            -Message "G9S1 evidence manifest hash drifted for $path."
    }
}

function Assert-ProductStaticContracts {
    foreach ($path in @($CoreProductSourcePaths + @($TestSourcePaths.Values))) {
        [void](Resolve-RequiredFile $path)
    }
    foreach ($className in $ExpectedTestMethods.Keys) {
        Assert-ExactSet `
            -Actual @(Get-TestMethodsFromSource $TestSourcePaths[$className]) `
            -Expected @($ExpectedTestMethods[$className]) `
            -Description "G9S1 focused test methods for $className"
    }

    $algo = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSplineV2.java"
    $evaluator = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplineSemanticEvaluator2D.java"
    $command = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdSplineV2.java"
    Assert-Condition -Condition ($algo -match
            'class\s+AlgoSplineV2\s+extends\s+AlgoLocusV2' -and
            $evaluator -match 'class\s+SplineSemanticEvaluator2D' -and
            $evaluator -match 'PiecewisePolynomialLocus2D' -and
            $command -match 'class\s+CmdSplineV2') `
        -Message "G9S1 semantic parent/evaluator/command seam is incomplete."
    $lengthCommand = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLength.java"
    $publicOperations = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java"
    $betweenMetric = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusBetweenMetricV2.java"
    $evaluatorMetric = Get-SourceAuthorityText `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/EvaluatorOnlyLocusMetricCapability2D.java"
    Assert-Condition -Condition ($lengthCommand -match
            'arg\[0\]\s+instanceof\s+GeoLocusV2' -and
            $lengthCommand -match 'scalarBetweenLength' -and
            $publicOperations -match
                'GeoNumeric\s+scalarBetweenLength\s*\(' -and
            $publicOperations -match 'new\s+AlgoLocusBetweenMetricV2' -and
            $publicOperations -match 'new\s+AlgoLocusMetricScalarAdapter' -and
            $betweenMetric -match
                '!start\.isDefined\(\)\s*\|\|\s*!target\.isDefined\(\)' -and
            $evaluatorMetric -match 'evaluateRouteDirectly' -and
            $evaluatorMetric -match 'route-local refinement defect' -and
            $evaluatorMetric -notmatch
                'org\.geocedg\.common\.kernel\.locus\.intersection|LocusRenderCache') `
        -Message "G9S1 rich-to-scalar partial metric seam is incomplete."
    $semanticSources = ($CoreProductSourcePaths | ForEach-Object {
            Get-SourceAuthorityText $_
        }) -join "`n"
    Assert-Condition -Condition ($semanticSources -notmatch
            'LocusRenderCache|org\.geogebra\.common\.euclidian|implements\s+Path') `
        -Message "G9S1 semantic authority leaked into render/viewport/Path state."

    foreach ($classicPath in @(
            "source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoSpline.java",
            "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdSpline.java")) {
        $boundaryCommit = if ($script:S1BoundaryMode -eq "TAGGED_DESCENDANT") {
            $script:S1AuthorityCommit
        } else {
            "HEAD"
        }
        & git -C $RepositoryRoot diff --quiet $EntrySha $boundaryCommit -- `
            $classicPath
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Classic Spline source changed during G9S1: $classicPath"
    }
}

function Assert-DocumentationContracts {
    $text = @($PromptPath, $AdrPath, $SpecPath, $ArchitecturePath,
        $ResearchPath, $MatrixPath, $TraceabilityPath, $ReportPath) |
        ForEach-Object {
            Get-SourceAuthorityText $_
        }
    $combined = $text -join "`n"
    Assert-Condition -Condition ($combined -match 'Option B' -and
            $combined -match 'SplineV2' -and
            $combined -match 'PASS — AUTHOR APPROVED' -and
            $combined -match 'Accepted' -and
            $combined -match 'selfApproved\s*=\s*false' -and
            $combined -match [regex]::Escape($OpenPeriodicRisk)) `
        -Message "G9S1 approved/risk documentation contract drifted."
    $futurePrompt = Get-SourceAuthorityText $FutureG9U1PromptPath
    Assert-Condition -Condition ($futurePrompt -match
            'POST-G9S1 SUCCESSOR — UNEXECUTED / NOT AUTHORIZED' -and
            $futurePrompt -match 'SplineV2' -and
            $futurePrompt -match 'Continuity = OFF' -and
            $futurePrompt -match 'geocedg\.brand\.topbar' -and
            $futurePrompt -match 'geocedg\.brand\.startup') `
        -Message "Prospective post-G9S1 G9U1 contract drifted."
    $composed = Get-SourceAuthorityText $ComposedVerifierPath
    $r5Index = $composed.IndexOf('& $G9U0R5SimilarityTransformationsVerifier',
        [StringComparison]::Ordinal)
    $s1Index = $composed.IndexOf('& $G9S1SemanticSplineVerifier',
        [StringComparison]::Ordinal)
    Assert-Condition -Condition ($r5Index -ge 0 -and $s1Index -gt $r5Index) `
        -Message "Composed verification must execute G9S1 after R5."
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
        -Description "Executed G9S1 methods for $ClassName"
    Assert-Condition -Condition (
            [int]$suite.tests -eq $ExpectedMethods.Count -and
            [int]$suite.failures -eq 0 -and
            [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "G9S1 focused JUnit result is not clean for $ClassName."
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
    $authorityHashes = @($PromptPath, $FutureG9U1PromptPath, $AdrPath,
        $SpecPath, $ArchitecturePath, $ResearchPath, $MatrixPath,
        $TraceabilityPath, $ScenarioPath, $EvidencePath |
        Sort-Object -CaseSensitive | ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-CanonicalLfSha256 $_
            }
        })
    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9S1"
        status = "PASS_AUTHOR_APPROVED"
        entrySha = $EntrySha
        r5PassTagObject = $R5PassTagObject
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
    Write-Host "Canonical G9S1 approved summary: $CanonicalSummaryPath"
    Write-Host "Canonical G9S1 approved summary SHA-256: $summaryHash"
    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "G9S1 comparison summary is missing."
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message "Deterministic G9S1 approved summary mismatch."
        Write-Host "Deterministic G9S1 approved-summary comparison: MATCH"
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
    foreach ($path in @($PromptPath, $FutureG9U1PromptPath, $AdrPath,
            $SpecPath, $ArchitecturePath, $ResearchPath, $MatrixPath,
            $TraceabilityPath, $ReportPath, $ScenarioPath, $EvidencePath,
            $ManifestPath, $VerifierRelativePath, $ComposedVerifierPath)) {
        [void](Resolve-RequiredFile $path)
    }

    Assert-EntryAuthority
    Assert-CanonicalSourceHashingContract
    $scenarios = Read-JsonDocument $ScenarioPath
    $evidence = Read-JsonDocument $EvidencePath
    Assert-ScenarioAndEvidenceContracts -Scenarios $scenarios `
        -Evidence $evidence
    Assert-ManifestContract
    Assert-ProductStaticContracts
    Assert-DocumentationContracts

    if (-not $HistoricalRegressionsAlreadyComposed) {
        & $R5VerifierPath -SkipBuild `
            -LogDirectory (Join-Path $LogDirectory "sealed-r5")
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Sealed descendant-safe R5 authority failed."
    }

    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9S1."
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9S1."

    if ($SkipBuild) {
        Write-Host "G9S1 static approved-authority verification completed."
        Write-Host "G9S1 = PASS — AUTHOR APPROVED"
    } else {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedState = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9s1" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
        $commonTestArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($className in @($ExpectedTestMethods.Keys | Where-Object {
                    $_ -notin $DesktopTestClasses
                })) {
            $commonTestArguments += @("--tests", $className)
        }
        $commonTestArguments += @(
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $commonTestArguments `
            -Description "G9S1 focused common-jre tests" `
            -LogFileName "g9s1-focused-common-jre-gradle.log"

        $desktopTestArguments = @(
            ":desktop:desktop:test", "--tests", $DesktopTestClasses[0],
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $desktopTestArguments `
            -Description "G9S1 focused Desktop native archive tests" `
            -LogFileName "g9s1-focused-desktop-gradle.log"

        $checkstyleArguments = @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $checkstyleArguments `
            -Description "G9S1 affected-module Checkstyle" `
            -LogFileName "g9s1-checkstyle-gradle.log"

        $results = @()
        foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
            $results += Get-TestResult -ClassName $entry.Key `
                -ExpectedMethods @($entry.Value)
        }
        $total = (@($results | ForEach-Object { $_.tests }) |
            Measure-Object -Sum).Sum
        Assert-Condition -Condition ($total -eq $ExpectedFocusedCount) `
            -Message "G9S1 focused JUnit count drifted from $ExpectedFocusedCount."
        foreach ($path in @(
                "source/shared/common/build/reports/checkstyle/main.xml",
                "source/shared/common-jre/build/reports/checkstyle/test.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $path
        }
        Write-CanonicalSummary -Scenarios $scenarios -TestResults $results
        Write-Host ("G9S1 focused result: {0}/{0} JUnit, Checkstyle clean." -f `
            $ExpectedFocusedCount)
        Write-Host "G9S1 = PASS — AUTHOR APPROVED"
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9S1 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during G9S1 verification.`n" +
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

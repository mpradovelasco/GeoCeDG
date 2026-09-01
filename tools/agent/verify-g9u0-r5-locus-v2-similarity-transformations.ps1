[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r5-locus-v2-similarity-transformations")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "ab465bfcbd08f168c730ba639ec5f99a4b08b9df"
$ExpectedBranch = "feature/g9u0-r5-locus-v2-similarity-transformations"
$R4PassTagName = "geocedg-g9u0-r4-pass"
$R5PassTagName = "geocedg-g9u0-r5-pass"
$R4PassTagObject = "0f9b303057b00d23722ad1f9d3594b4609d668a7"
$R4ProductCommit = "63c291464111a5bcdbca488d6639662e46c389c4"
$R5PassTagObject = "3712595fe2b168ba494379b6b3f0051e4122cfae"
$R5PassCommit = "5952dfdbd238e71e598f4d2ca92c3e03437df41c"
$PromptCanonicalLfSha256 =
    "87e500964201cd04756a9d8c55c814eaade450ebf8a73c559c835e034522b802"
$HistoricalG9U1PromptCanonicalLfSha256 =
    "502dabbac1f756e01d0f7935a337e389a3c5e26eaabf3452a6ffe953e83b6ddd"
$PostR3G9U1PromptCanonicalLfSha256 =
    "5c4918ffa6d7c4679439d2b45d359b96bc4ed35a4b99b517433df2d6eca23fa9"
$PostR5G9U1PromptCanonicalLfSha256 =
    "0b96f571932144f9a99f7681938edf756c8999cb847b61095f82430068e96389"
$OpenPeriodicRisk = "G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP"
$DynamicDilateFixturePath =
    "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r5/fourSolutionsDynamicDilate.cedg"
$DynamicDilateFixtureSha256 =
    "13cde59d54a463413140007e793a50e8cb933cab21d4be286c9d76f6b2f713fe"
$DynamicDilateFixtureSize = 25704

$PromptPath =
    ".github/prompts/tasks/g9u0-r5-locus-v2-similarity-transformations.prompt.md"
$HistoricalG9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace.prompt.md"
$PostR3G9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r3.prompt.md"
$PostR5G9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r5.prompt.md"
$SpecPath = "geocedg/specs/locus/locus-v2-similarity-transformations.md"
$ArchitecturePath =
    "docs/architecture/g9u0_r5_locus_v2_similarity_transformations.md"
$MatrixPath =
    "docs/validation/g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md"
$ScenarioPath =
    "geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-scenarios.json"
$EvidencePath =
    "geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-evidence.json"
$ManifestPath = "geocedg/validation/g9u0-r5/g9u0-r5-evidence.sha256"
$ReportPath =
    "docs/validation/g9u0_r5_locus_v2_similarity_transformations_candidate_report.md"
$VerifierRelativePath =
    "tools/agent/verify-g9u0-r5-locus-v2-similarity-transformations.ps1"
$ComposedVerifierPath = "tools/agent/verify.ps1"
$R4VerifierPath = Join-Path $PSScriptRoot `
    "verify-g9u0-r4-intersection-admissibility-continuation.ps1"

$ProductSourcePaths = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusSimilarityTransform2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityTransform2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/EvaluatorOnlyLocusMetricCapability2D.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdTranslate.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdRotate.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdMirror.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdDilate.java",
    "source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/commands/CmdMirror3D.java",
    "source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/commands/CmdRotate3D.java"
)
$TestSourcePaths = @(
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5SimilarityTransformationsTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5CommandRoutingPublicSurfaceTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5SemanticTransformEdgeCasesTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R5NativeArchivePersistenceTest.java"
)
$DeterministicSourcePaths = @($ProductSourcePaths + $TestSourcePaths)

$ExpectedTestMethods = [ordered]@{
    "org.geocedg.common.locus.G9U0R5SimilarityTransformationsTest" = @(
        "c01OrdinaryCommandsCreateSemanticLocusImages",
        "c02ParentInputsAreExactNormalDagDependencies",
        "c03LegacyMutableTransformationContractsRemainExcluded",
        "i02IdentityMapsAndCoincidentCompositionsAlwaysUseFreshIds",
        "e01TranslationPreservesSemanticAddressAndDomain",
        "e02RotationReflectionAndDilationEvaluateComposition",
        "e03TransformationClosureUsesNormalComposition",
        "d01DynamicInputsPublishOnlySemanticChangesAndRecover",
        "e04InvalidAddressesOverflowAndNonfiniteInputsFailWithoutStaleGeometry",
        "g01ZeroDilationIsValidCollapsedSemanticImage",
        "m01CollapsedOpenDomainHasTruthfulRichZeroLength",
        "m02SimilarityLengthCovarianceIsEvaluatorDerived",
        "m03PartialAndPeriodicMetricCovarianceUsesSemanticAddresses",
        "p02SemanticPointCovarianceKeepsAddressAndDistinctIdentity",
        "p03EverySimilarityFamilyFeedsNormalSemanticPointsAndDynamicInputs",
        "x01TransformedQueriesHaveCovariantGeometryButFreshTokens",
        "x02TransformedQueryBindingIsPathIndependentUnderRegularMotion",
        "x03RotationReflectionAndDilationIntersectionGeometryCovaries",
        "x04EverySupportedTargetFamilyConsumesTheTransformedEvaluator",
        "p01SaveReopenPreservesCommandIdentityAndDurableOutput",
        "s02CopyRemapsTheTransformDependencySliceWithoutIdReuse",
        "s03UndoRedoAndRenamePreserveTheTransformIdentity",
        "s04XmlKeepsOrdinaryCommandsAndContainsNoDerivedGeometryPayload",
        "s01TransformedImageStartsWithOrdinarySourceStyle",
        "n01UnsupportedInversionAnd3dCenterRemainOutsideR5",
        "i01CollapsedIntersectionNeverFabricatesIsolatedAdmissibleRoots"
    )
    "org.geocedg.common.locus.G9U0R5CommandRoutingPublicSurfaceTest" = @(
        "allSevenOrdinaryFormsRouteToOneSemanticParent",
        "reflectAndMirrorAliasesShareOrdinaryMirrorAuthority",
        "featureOffPreservesButCannotCreateTransformedLocus",
        "ordinaryTextAndVectorOverloadsRemainUpstreamOwned",
        "circleInversionAndThreeDimensionalFormsFailClosed",
        "locusDoesNotAcquireLegacyMutableTransformContractsOrPath",
        "nestedCommandsHaveOneSerializableAttachmentPerGeo",
        "rejectedCircularRedefineRollsBackSimilarityParticipation"
    )
    "org.geocedg.common.locus.G9U0R5SemanticTransformEdgeCasesTest" = @(
        "periodicNegativeDilationPreservesFundamentalDomainAndSeam",
        "disconnectedGapSurvivesCollapsedImageAndLaterTransformClosure",
        "decreasingOrientationAndNegativeScalePreserveSemanticAddress",
        "finiteRotationOverflowAndUndefinedInputsRecoverWithoutStalePoint",
        "multipleBranchesAndEmptyDefinitionRemainStructurallyExact",
        "emptyDefinitionTransformsToAnEmptyDefinedSemanticImage",
        "unboundedCollapsedImageHasRichExactZeroWithoutEndpointEvaluation",
        "finiteLargeReflectionCoefficientsNormalizeWithoutOverflow"
    )
    "org.geocedg.desktop.G9U0R5NativeArchivePersistenceTest" = @(
        "authorDynamicDilateFixtureUsesOneLiveParentAcrossFiniteFactorUpdates",
        "nativeCedgReopensEverySimilarityFamilyWithStableIdentityAndDependencies",
        "nativeCedgReopensDownstreamPointMetricAndTransformedIntersectionTokens",
        "featureOffAndClassicPreserveTransformedNativeDocumentWithoutCreation"
    )
}
$DesktopTestClasses = @(
    "org.geocedg.desktop.G9U0R5NativeArchivePersistenceTest"
)
$ExpectedFocusedCount = 46
$ExpectedSealedCandidatePaths = @(
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r5.prompt.md",
    "docs/architecture/g9u0_r5_locus_v2_similarity_transformations.md",
    "docs/developer/geocedg_developer_guide.md",
    "docs/developer/locus_v2_api.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/upstream/modified-files.yml",
    "docs/user/geocedg_user_guide.md",
    "docs/validation/g9_documentation_bundle_traceability.md",
    "docs/validation/g9_public_workspace_validation_matrix.md",
    "docs/validation/g9u0_r5_locus_v2_similarity_transformations_candidate_report.md",
    "docs/validation/g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md",
    "geocedg/specs/README.md",
    "geocedg/specs/locus/locus-v2-similarity-transformations.md",
    "geocedg/validation/g9u0-r5/g9u0-r5-evidence.sha256",
    "geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-evidence.json",
    "geocedg/validation/g9u0-r5/g9u0-r5-locus-v2-similarity-transformations-scenarios.json",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R5NativeArchivePersistenceTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5CommandRoutingPublicSurfaceTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5SemanticTransformEdgeCasesTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5SimilarityTransformationsTest.java",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r5/fourSolutionsDynamicDilate.cedg",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusSimilarityTransform2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityTransform2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/EvaluatorOnlyLocusMetricCapability2D.java",
    "source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/commands/CmdMirror3D.java",
    "source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/commands/CmdRotate3D.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdDilate.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdMirror.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdRotate.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdTranslate.java",
    "tools/agent/verify-g9u0-r5-locus-v2-similarity-transformations.ps1",
    "tools/agent/verify.ps1"
)
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
    $rootPrefix = $RepositoryRoot.TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($fullPath.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Path escapes repository root: $RelativePath"
    return $fullPath
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $fullPath = Resolve-RepositoryPath $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $fullPath -PathType Leaf) `
        -Message "Required R5 file is missing: $RelativePath"
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
            -Message "Unable to read R5 Git blob ${Object}: $($errorText.Trim())"
        return ,([byte[]]$memory.ToArray())
    } finally {
        $memory.Dispose()
        $process.Dispose()
    }
}

function Get-SourceAuthorityBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [void](Resolve-RepositoryPath $RelativePath)
    if ($script:R5BoundaryMode -ne "TAGGED_DESCENDANT" -or
            $null -eq $script:R5AuthorityCommit) {
        return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
    }
    $normalized = $RelativePath.Replace("\", "/")
    return ,(Get-GitBlobBytes -Object "$($script:R5AuthorityCommit):$normalized")
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

    if ($script:R5BoundaryMode -eq "TAGGED_DESCENDANT") {
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
        throw "Unable to classify R5 source authority for $RelativePath."
    }
    return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
}

function Get-DeterministicSourceSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return Get-CanonicalLfSha256FromBytes -Bytes (
        Get-DeterministicSourceAuthorityBytes $RelativePath)
}

function Assert-CanonicalSourceHashingContract {
    $utf8 = [Text.UTF8Encoding]::new($false)
    [byte[]]$lf = $utf8.GetBytes("tracked-source`nsemantic-line`n")
    [byte[]]$crlf = $utf8.GetBytes("tracked-source`r`nsemantic-line`r`n")
    [byte[]]$mutated = $utf8.GetBytes("tracked-source`nsemantic-Line`n")
    $lfHash = Get-CanonicalLfSha256FromBytes -Bytes $lf
    $crlfHash = Get-CanonicalLfSha256FromBytes -Bytes $crlf
    $mutatedHash = Get-CanonicalLfSha256FromBytes -Bytes $mutated
    Assert-Condition -Condition ($lfHash -ceq $crlfHash) `
        -Message "R5 canonical source hashing is not LF/CRLF independent."
    Assert-Condition -Condition ($lfHash -cne $mutatedHash) `
        -Message "R5 canonical source hashing missed a content mutation."
    $promptBlobHash = Get-CanonicalLfSha256FromBytes -Bytes (
        Get-SourceAuthorityBytes $PromptPath)
    Assert-Condition -Condition ($promptBlobHash -ceq
            (Get-CanonicalLfSha256 $PromptPath)) `
        -Message "Git blob and checked-out canonical-LF evidence differ."
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
        -Message "Unable to enumerate committed R5 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --name-only --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate unstaged R5 candidate paths."
    $pathSets.Add(@(& git -C $RepositoryRoot diff --cached --name-only `
            --no-renames --))
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate staged R5 candidate paths."
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
        -Message "Unable to enumerate untracked R5 candidate paths."
    return @($paths | Sort-Object -Unique -CaseSensitive)
}

function Get-CommitCandidatePaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate sealed R5 candidate paths."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object { $_.Replace("\", "/") } |
        Sort-Object -Unique -CaseSensitive)
}

function Get-CandidatePaths {
    if ($script:R5BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeCandidatePaths)
    }
    Assert-Condition -Condition (
            $script:R5BoundaryMode -eq "TAGGED_DESCENDANT" -and
            $null -ne $script:R5AuthorityCommit) `
        -Message "The R5 source-boundary mode was not established."
    return @(Get-CommitCandidatePaths -Commit $script:R5AuthorityCommit)
}

function Get-TestMethodsFromSource {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $source = Get-SourceAuthorityText $RelativePath
    return @([regex]::Matches($source,
            '(?ms)^\s*@Test\s+(?:public\s+)?void\s+([A-Za-z0-9_]+)\s*\(') |
        ForEach-Object { $_.Groups[1].Value })
}

function Assert-EntryAuthority {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve R5 HEAD."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $head
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R5 entry commit is not an ancestor of HEAD."
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()

    $r5TagObject = ((@(& git -C $RepositoryRoot rev-parse `
        "refs/tags/$R5PassTagName" 2>$null) -join "")).Trim()
    $hasR5PassTag = $LASTEXITCODE -eq 0
    if ($hasR5PassTag) {
        Assert-Condition -Condition ($r5TagObject -ceq $R5PassTagObject) `
            -Message "The G9U0-R5 PASS tag object changed."
        $r5TagType = (& git -C $RepositoryRoot cat-file -t $r5TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $r5TagType -ceq "tag") `
            -Message "$R5PassTagName must remain annotated."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$r5TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $authorityCommit -ceq $R5PassCommit) `
            -Message "The G9U0-R5 PASS tag peel changed."
        $r5TagText = @(& git -C $RepositoryRoot cat-file tag $r5TagObject) `
            -join "`n"
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $r5TagText.Contains("G9U0-R5") -and
                $r5TagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "$R5PassTagName lacks the approved disposition."
        $closeoutRecord = @((& git -C $RepositoryRoot rev-list --parents `
            -n 1 $authorityCommit).Trim() -split '\s+')
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $closeoutRecord.Count -eq 2 -and
                $closeoutRecord[0] -ceq $R5PassCommit -and
                $closeoutRecord[1] -ceq $EntrySha) `
            -Message "The sealed R5 closeout ancestry changed."
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $authorityCommit $head
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged R5 closeout."
        $script:R5BoundaryMode = "TAGGED_DESCENDANT"
        $script:R5AuthorityCommit = $authorityCommit
        Assert-ExactSet -Actual @(Get-CandidatePaths) `
            -Expected $ExpectedSealedCandidatePaths `
            -Description "sealed R5 candidate inventory"
    } else {
        Assert-Condition -Condition ($head -ceq $EntrySha -and
                $branch -ceq $ExpectedBranch) `
            -Message ("Pre-commit R5 verification requires entry HEAD on " +
                "$ExpectedBranch; descendant verification requires its PASS tag.")
        $script:R5BoundaryMode = "WORKTREE"
    }

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "${R4PassTagName}^{tag}").Trim()
    $tagPeel = (& git -C $RepositoryRoot rev-parse `
        "${R4PassTagName}^{}").Trim()
    Assert-Condition -Condition ($tagObject -ceq $R4PassTagObject -and
            $tagPeel -ceq $R4ProductCommit) `
        -Message "Sealed R4 PASS tag object/peel drifted."
    & git -C $RepositoryRoot merge-base --is-ancestor $R4ProductCommit $EntrySha
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R4 product PASS is not ancestral to the R5 entry."
    Assert-Condition -Condition (
            (Get-CanonicalLfSha256 $PromptPath) -ceq
                $PromptCanonicalLfSha256) `
        -Message "Canonical R5 execution prompt drifted from approved authority."
    Assert-Condition -Condition (
            (Get-CanonicalLfSha256 $HistoricalG9U1PromptPath) -ceq
                $HistoricalG9U1PromptCanonicalLfSha256 -and
            (Get-CanonicalLfSha256 $PostR3G9U1PromptPath) -ceq
                $PostR3G9U1PromptCanonicalLfSha256 -and
            (Get-CanonicalLfSha256 $PostR5G9U1PromptPath) -ceq
                $PostR5G9U1PromptCanonicalLfSha256) `
        -Message "Historical or definitive prospective G9U1 prompt drifted."
}

function Get-MatrixRows {
    $rows = [Collections.Generic.List[object]]::new()
    foreach ($line in (Get-SourceAuthorityText $MatrixPath) -split "`r?`n") {
        if ($line -match '^\|\s*(R5-[A-Z0-9-]+)\s*\|') {
            $rows.Add([pscustomobject]@{
                    id = $Matches[1]
                    inactive = $line -match 'INACTIVE'
                })
        }
    }
    return @($rows)
}

function Assert-ScenarioContract {
    param([Parameter(Mandatory)] [object]$Scenarios)

    Assert-Condition -Condition ($Scenarios.phase -ceq "G9U0-R5" -and
            $Scenarios.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            $Scenarios.zeroScalePolicy -ceq "OPTION_A_COLLAPSED_IMAGE") `
        -Message "R5 approved scenario status or Option-A policy drifted."
    Assert-Condition -Condition (
            [int]$Scenarios.matrix.allRows -eq 110 -and
            [int]$Scenarios.matrix.activeRows -eq 106) `
        -Message "R5 matrix frozen counts drifted."

    $rows = @(Get-MatrixRows)
    $active = @($rows | Where-Object { -not $_.inactive } |
        ForEach-Object { $_.id })
    $inactive = @($rows | Where-Object { $_.inactive } |
        ForEach-Object { $_.id })
    $declared = @($Scenarios.groups | ForEach-Object {
            $_.activeScenarioIds
        })
    Assert-Condition -Condition ($rows.Count -eq 110 -and
            $active.Count -eq 106 -and $inactive.Count -eq 4) `
        -Message "R5 matrix must contain 106 active and four inactive rows."
    Assert-ExactSet -Actual $declared -Expected $active `
        -Description "R5 active scenario inventory"
    Assert-ExactSet -Actual @($Scenarios.matrix.inactiveOptionBRows) `
        -Expected $inactive -Description "R5 inactive Option-B inventory"

    Assert-Condition -Condition (
            [int]$Scenarios.focusedJUnit.declared -eq $ExpectedFocusedCount -and
            [int]$Scenarios.focusedJUnit.sharedKernel -eq 42 -and
            [int]$Scenarios.focusedJUnit.desktopNativeArchive -eq 4 -and
            $Scenarios.focusedJUnit.focusedA.status -ceq
                "PASS" -and
            $Scenarios.focusedJUnit.focusedB.status -ceq
                "PASS" -and
            [bool]$Scenarios.focusedJUnit.focusedB.matchesFocusedA) `
        -Message "R5 candidate focused execution state drifted."
    Assert-Condition -Condition (
            [bool]$Scenarios.approval.implementationStarted -and
            -not [bool]$Scenarios.approval.selfApproved -and
            [bool]$Scenarios.approval.authorApproved -and
            [bool]$Scenarios.approval.passClaimed -and
            [bool]$Scenarios.manualAuthorSmoke.passed -and
            $Scenarios.manualAuthorSmoke.status -ceq
                "PASS_WITH_G9A_FREE_INPUT_LIMITATION_CHARACTERIZED") `
        -Message "R5 scenario must record author-approved closeout and smoke."
}

function Assert-EvidenceContract {
    param([Parameter(Mandatory)] [object]$Evidence)

    Assert-Condition -Condition ($Evidence.phase -ceq "G9U0-R5" -and
            $Evidence.status -ceq
                "PASS_AUTHOR_APPROVED" -and
            -not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed) `
        -Message "R5 evidence must record author-approved closeout."
    Assert-Condition -Condition (
            $Evidence.authority.canonicalPromptLfSha256 -ceq
                $PromptCanonicalLfSha256 -and
            $Evidence.authority.zeroScalePolicy -ceq
                "OPTION_A_COLLAPSED_IMAGE" -and
            [int]$Evidence.authority.activeMatrixRows -eq 106 -and
            $Evidence.authority.futureG9U1Prompt -ceq $PostR5G9U1PromptPath -and
            $Evidence.authority.futureG9U1PromptLfSha256 -ceq
                $PostR5G9U1PromptCanonicalLfSha256) `
        -Message "R5 evidence authority or Option-A matrix count drifted."
    Assert-Condition -Condition (
            $Evidence.deterministicEvidence.sourceHashMethod -ceq
                "GIT_BLOB_OR_CURRENT_CANDIDATE_UTF8_NO_BOM_CANONICAL_LF_SHA256" -and
            -not [bool]$Evidence.deterministicEvidence.workingTreeLineEndingsAreAuthority -and
            [bool]$Evidence.deterministicEvidence.contentMutationMustChangeHash -and
            $Evidence.deterministicEvidence.focusedA.status -ceq "PASS" -and
            $Evidence.deterministicEvidence.focusedB.status -ceq "PASS" -and
            [bool]$Evidence.deterministicEvidence.focusedB.matchesFocusedA) `
        -Message "R5 canonical source-hash contract drifted."
    $dynamic = $Evidence.dynamicDilationSmoke
    Assert-Condition -Condition ($dynamic.status -ceq
            "PASS_WITH_G9A_FREE_INPUT_LIMITATION_CHARACTERIZED" -and
            $dynamic.fixture.path -ceq $DynamicDilateFixturePath -and
            [int64]$dynamic.fixture.bytes -eq $DynamicDilateFixtureSize -and
            $dynamic.fixture.sha256 -ceq $DynamicDilateFixtureSha256 -and
            [bool]$dynamic.sliderUpdatePassed -and
            [bool]$dynamic.explicitExistingObjectEditPassed -and
            [bool]$dynamic.zeroCrossingRecoveryPassed -and
            [bool]$dynamic.saveReopenPassed -and
            $dynamic.freeInputDisposition -ceq
                "G9A_REDEFINE_CONTEXT_MISSING_DEFERRED_TO_G9U1_NOT_R5_BLOCKER") `
        -Message "R5 dynamic-dilation author-smoke evidence drifted."
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
        -Description "R5 evidence manifest paths"
    foreach ($path in $expectedPaths) {
        Assert-Condition -Condition ($actual[$path] -ceq
                (Get-CanonicalLfSha256 $path)) `
            -Message "R5 evidence manifest hash drifted for $path."
    }
}

function Assert-ProductStaticContracts {
    foreach ($path in @($ProductSourcePaths + $TestSourcePaths)) {
        [void](Resolve-RequiredFile $path)
    }
    [byte[]]$dynamicFixture = Get-SourceAuthorityBytes $DynamicDilateFixturePath
    $dynamicFixtureHash = [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData(
            $dynamicFixture)).ToLowerInvariant()
    Assert-Condition -Condition (
            $dynamicFixture.Length -eq $DynamicDilateFixtureSize -and
            $dynamicFixtureHash -ceq $DynamicDilateFixtureSha256) `
        -Message "R5 byte-exact dynamic-dilation author fixture drifted."
    foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
        $shortName = $entry.Key.Substring($entry.Key.LastIndexOf('.') + 1)
        $testPath = @($TestSourcePaths | Where-Object {
                [IO.Path]::GetFileNameWithoutExtension($_) -ceq $shortName
            })
        Assert-Condition -Condition ($testPath.Count -eq 1) `
            -Message "Unable to map R5 test class $($entry.Key)."
        Assert-ExactSet -Actual @(Get-TestMethodsFromSource $testPath[0]) `
            -Expected @($entry.Value) -Description "R5 methods for $($entry.Key)"
    }
    $declaredTotal = (@($ExpectedTestMethods.Values | ForEach-Object {
                $_.Count
            }) | Measure-Object -Sum).Sum
    Assert-Condition -Condition ($declaredTotal -eq $ExpectedFocusedCount) `
        -Message "R5 focused method total drifted from 46."

    $algo = Get-SourceAuthorityText $ProductSourcePaths[0]
    Assert-Condition -Condition ($algo -match
            'class\s+AlgoLocusSimilarityTransform2D\s+extends\s+AlgoLocusV2' -and
            $algo -match 'COLLAPSED_IMAGE') `
        -Message "R5 semantic parent or Option-A collapsed authority is missing."
    $newSemanticSources = ($ProductSourcePaths[0..2] | ForEach-Object {
            Get-SourceAuthorityText $_
        }) -join "`n"
    Assert-Condition -Condition ($newSemanticSources -notmatch
            'LocusRenderCache|Euclidian|viewport|screen position|implements\s+Path|implements\s+.*Transformable|implements\s+.*Translateable|implements\s+.*Rotatable|implements\s+.*Mirrorable|implements\s+.*Dilateable') `
        -Message "R5 semantic authority leaked into rendering or mutable Path transforms."
    foreach ($commandPath in $ProductSourcePaths[5..8]) {
        $command = Get-SourceAuthorityText $commandPath
        Assert-Condition -Condition ($command -match 'GeoLocusV2' -and
                $command -match 'LocusV2PublicOperations') `
            -Message "Ordinary R5 command routing is missing in $commandPath."
    }
}

function Assert-DocumentationContracts {
    $combined = @($SpecPath, $ArchitecturePath, $MatrixPath, $ReportPath) |
        ForEach-Object {
            Get-SourceAuthorityText $_
        }
    $text = $combined -join "`n"
    Assert-Condition -Condition ($text -match 'Option A' -and
            $text -match 'COLLAPSED_IMAGE' -and
            $text -match 'PASS — AUTHOR APPROVED' -and
            $text -match 'selfApproved\s*=\s*false' -and
            $text -match [regex]::Escape($OpenPeriodicRisk)) `
        -Message "R5 Option-A approved/risk documentation contract drifted."
    $futurePrompt = Get-SourceAuthorityText $PostR5G9U1PromptPath
    Assert-Condition -Condition ($futurePrompt -match
            'DEFINITIVE PROSPECTIVE POST-G9U0-R5 SUCCESSOR PROMPT' -and
            $futurePrompt -match 'G9U1 = DESIGNED — NOT AUTHORIZED' -and
            $futurePrompt -match 'geocedg\.brand\.topbar' -and
            $futurePrompt -match 'geocedg\.brand\.startup' -and
            $futurePrompt -match 'create-one, create-selected or create-all' -and
            $futurePrompt -match 'COLLAPSED_IMAGE' -and
            $futurePrompt -match 'TRANSFORMED_QUERY_TOKENS') `
        -Message "Definitive prospective post-R5 G9U1 contract drifted."
    $composed = Get-SourceAuthorityText $ComposedVerifierPath
    $r4Index = $composed.IndexOf('& $G9U0R4IntersectionAdmissibilityVerifier',
        [StringComparison]::Ordinal)
    $r5Index = $composed.IndexOf('& $G9U0R5SimilarityTransformationsVerifier',
        [StringComparison]::Ordinal)
    Assert-Condition -Condition ($r4Index -ge 0 -and $r5Index -gt $r4Index) `
        -Message "Composed verification must execute R5 after R4."
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

    $root = if ($ClassName -in $DesktopTestClasses) {
        "source/desktop/desktop/build/test-results/test"
    } else {
        "source/shared/common-jre/build/test-results/test"
    }
    $relativePath = "$root/TEST-$ClassName.xml"
    [xml]$result = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile $relativePath)
    $suite = $result.testsuite
    $methods = @($suite.testcase | ForEach-Object {
            $_.name -replace '\(.*\)$', ''
        })
    Assert-ExactSet -Actual $methods -Expected $ExpectedMethods `
        -Description "Executed R5 methods for $ClassName"
    Assert-Condition -Condition (
            [int]$suite.tests -eq $ExpectedMethods.Count -and
            [int]$suite.failures -eq 0 -and
            [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "R5 JUnit result is not clean for $ClassName."
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
    $activeIds = @($Scenarios.groups | ForEach-Object {
            $_.activeScenarioIds
        } | Sort-Object -CaseSensitive)
    $sourceHashes = @($DeterministicSourcePaths |
        Sort-Object -CaseSensitive | ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-DeterministicSourceSha256 $_
            }
        })
    $authorityHashes = @($PromptPath, $PostR5G9U1PromptPath, $SpecPath,
        $ArchitecturePath, $MatrixPath, $ScenarioPath, $EvidencePath |
        Sort-Object -CaseSensitive | ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-CanonicalLfSha256 $_
            }
        })
    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9U0-R5"
        status = "PASS_AUTHOR_APPROVED"
        zeroScalePolicy = "OPTION_A_COLLAPSED_IMAGE"
        entrySha = $EntrySha
        r4ProductCommit = $R4ProductCommit
        r4PassTagObject = $R4PassTagObject
        activeMatrixCount = 106
        inactiveOptionBCount = 4
        activeScenarioIds = $activeIds
        focusedJUnit = $ExpectedFocusedCount
        testResults = @($TestResults | Sort-Object { $_.class })
        candidatePaths = $candidatePaths
        deterministicSourceHashes = $sourceHashes
        authorityHashes = $authorityHashes
        commandFormCount = 7
        openRiskIds = @($OpenPeriodicRisk)
        dynamicDilationFixture = [ordered]@{
            path = $DynamicDilateFixturePath
            bytes = $DynamicDilateFixtureSize
            sha256 = $DynamicDilateFixtureSha256
        }
        manualAuthorSmoke =
            "PASS_WITH_G9A_FREE_INPUT_LIMITATION_CHARACTERIZED"
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
    Write-Host "Canonical R5 candidate summary: $CanonicalSummaryPath"
    Write-Host "Canonical R5 candidate summary SHA-256: $summaryHash"
    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "R5 comparison summary is missing."
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message "Deterministic R5 candidate summary mismatch."
        Write-Host "Deterministic R5 candidate-summary comparison: MATCH"
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
    foreach ($path in @($PromptPath, $HistoricalG9U1PromptPath,
            $PostR3G9U1PromptPath, $PostR5G9U1PromptPath,
            $SpecPath, $ArchitecturePath, $MatrixPath,
            $ScenarioPath, $EvidencePath, $ManifestPath, $ReportPath,
            $VerifierRelativePath, $ComposedVerifierPath)) {
        [void](Resolve-RequiredFile $path)
    }

    Assert-EntryAuthority
    Assert-CanonicalSourceHashingContract
    $scenarios = Read-JsonDocument $ScenarioPath
    $evidence = Read-JsonDocument $EvidencePath
    Assert-ScenarioContract -Scenarios $scenarios
    Assert-EvidenceContract -Evidence $evidence
    Assert-ManifestContract
    Assert-ProductStaticContracts
    Assert-DocumentationContracts

    if (-not $HistoricalRegressionsAlreadyComposed) {
        & $R4VerifierPath -SkipBuild `
            -LogDirectory (Join-Path $LogDirectory "sealed-r4")
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Sealed descendant-safe R4 authority failed."
    }
    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9U0-R5."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9U0-R5."

    if ($SkipBuild) {
        Write-Host "G9U0-R5 static approved-closeout verification completed."
        Write-Host "G9U0-R5 = PASS — AUTHOR APPROVED"
    } else {
        $GeneratedState = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9u0-r5"
        $commonArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($class in @($ExpectedTestMethods.Keys | Where-Object {
                    $_ -notin $DesktopTestClasses
                })) {
            $commonArguments += @("--tests", $class)
        }
        $commonArguments += @(
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $commonArguments `
            -Description "G9U0-R5 focused common-jre tests" `
            -LogFileName "g9u0-r5-focused-common-jre-gradle.log"

        $desktopArguments = @(
            ":desktop:desktop:test",
            "--tests", $DesktopTestClasses[0],
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $desktopArguments `
            -Description "G9U0-R5 focused Desktop native archive tests" `
            -LogFileName "g9u0-r5-focused-desktop-gradle.log"

        $checkstyleArguments = @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $checkstyleArguments `
            -Description "G9U0-R5 affected-module Checkstyle" `
            -LogFileName "g9u0-r5-checkstyle-gradle.log"

        $results = @()
        foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
            $results += Get-TestResult -ClassName $entry.Key `
                -ExpectedMethods @($entry.Value)
        }
        $total = (@($results | ForEach-Object { $_.tests }) |
            Measure-Object -Sum).Sum
        Assert-Condition -Condition ($total -eq $ExpectedFocusedCount) `
            -Message "R5 focused JUnit count drifted from 46."
        foreach ($path in @(
                "source/shared/common/build/reports/checkstyle/main.xml",
                "source/shared/common-jre/build/reports/checkstyle/test.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $path
        }
        Write-CanonicalSummary -Scenarios $scenarios -TestResults $results
        Write-Host "G9U0-R5 focused result: 46/46 JUnit, Checkstyle clean."
        Write-Host "G9U0-R5 = PASS — AUTHOR APPROVED"
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9U0-R5 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during R5 verification.`n" +
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

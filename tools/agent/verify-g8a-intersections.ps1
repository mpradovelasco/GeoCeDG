[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [switch]$RequireFinalEvidence,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g8a-intersections")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "315aec011cdc719a41a9bdc352a4a10ea502df6e"
$PromptSha = "0f962a36b2cae76d2208371bd39e68fff8bf5cd1d9a7f3f9e9ca14afefef424b"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$CheckstyleResult = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\reports\checkstyle\test.xml"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8a"
$EvidencePath = Join-Path $EvidenceRoot `
    "g8a-intersection-characterization-evidence.json"
$CloseoutEvidencePath = Join-Path $EvidenceRoot `
    "g8a-author-closeout-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8a-evidence.sha256"
$G8BEvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8b\g8b-intersection-kernel-evidence.json"
$G8BVerifierPath = Join-Path $PSScriptRoot "verify-g8b-intersections.ps1"
$G8CEvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c\g8c-design-characterization-evidence.json"
$G8CVerifierPath = Join-Path $PSScriptRoot "verify-g8c-intersections-design.ps1"
$G8C1EvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c1\g8c1-intersection-kernel-evidence.json"
$G8C1VerifierPath = Join-Path $PSScriptRoot "verify-g8c1-intersections.ps1"
$G8C2EvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c2\g8c2-intersection-kernel-evidence.json"
$G8C2VerifierPath = Join-Path $PSScriptRoot "verify-g8c2-intersections.ps1"
$ReferenceGenerator = Join-Path $EvidenceRoot `
    "generate_intersection_references.py"
$TestSourceRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\src\test\java\org\geocedg\common\locus"
$InitialStatus = $null
$GeneratedSnapshot = $null

$G8Documents = @(
    "docs\roadmap\geocedg_roadmap.md",
    "docs\roadmap\g8_locus_v2_intersections_plan.md",
    "geocedg\specs\locus\locus-v2-intersections.md",
    "docs\adr\0008-locus-v2-intersection-result-and-continuation.md",
    "docs\architecture\locus_v2_intersection_semantic_model.md",
    "docs\architecture\locus_v2_intersection_architecture.md",
    "docs\architecture\locus_v2_intersection_upstream_impact.md",
    "docs\developer\locus_v2_intersection_api.md",
    "docs\validation\g8_locus_v2_intersection_validation_matrix.md",
    "docs\validation\g8_locus_v2_intersection_benchmark_plan.md",
    "docs\validation\g8_locus_v2_intersection_scientific_traceability.md",
    "docs\validation\g8a_locus_v2_intersection_characterization_report.md",
    "docs\validation\g8a_locus_v2_intersection_traceability_matrix.md",
    "docs\user\geocedg_user_guide.md",
    ".github\prompts\tasks\g8b-locus-v2-intersection-kernel.prompt.md"
)

$TestClasses = [ordered]@{
    "org.geocedg.common.locus.G8AIntersectionSemanticCharacterizationTest" = 13
    "org.geocedg.common.locus.G8AIntersectionNumericalCharacterizationTest" = 15
    "org.geocedg.common.locus.G8AIntersectionIdentityCharacterizationTest" = 12
    "org.geocedg.common.locus.G8AIntersectionTopologyCharacterizationTest" = 7
    "org.geocedg.common.locus.G8AIntersectionKernelLifecycleCharacterizationTest" = 10
    "org.geocedg.common.locus.G8AIntersectionFunctionalBenchmarkTest" = 8
}

. $GeneratedStateHelper
. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$Path)

    $content = [IO.File]::ReadAllText($Path)
    $canonical = $content.Replace("`r`n", "`n").Replace("`r", "`n")
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
        return [Convert]::ToHexString(
            $sha256.ComputeHash($bytes)).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Required G8A artifact is missing: $RelativePath"
}

function Assert-EvidenceHashes {
    foreach ($line in Get-Content -LiteralPath $EvidenceHashes) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2) `
            -Message "Malformed G8A evidence hash line: $line"
        $relativePath = $parts[1].Trim().Replace("/", "\")
        $path = Join-Path $RepositoryRoot $relativePath
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed G8A evidence artifact is missing: $path"
        $actual = Get-CanonicalTextSha256 -Path $path
        Assert-Condition -Condition ($actual -eq $parts[0].ToLowerInvariant()) `
            -Message "G8A evidence hash mismatch: $path"
    }
}

function Assert-DocumentContains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$ExpectedText
    )

    $path = Join-Path $RepositoryRoot $RelativePath
    $content = Get-Content -LiteralPath $path -Raw
    foreach ($text in $ExpectedText) {
        Assert-Condition -Condition ($content.Contains($text)) `
            -Message "$RelativePath does not contain required closeout text: $text"
    }
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $G8Documents) {
        $document = Join-Path $RepositoryRoot $relativeDocument
        $content = Get-Content -LiteralPath $document -Raw
        foreach ($match in [regex]::Matches(
                $content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $match.Groups["target"].Value.Trim().Trim("<", ">")
            if ($target.StartsWith("#") -or
                    $target -match '^(https?|mailto):') {
                continue
            }
            $pathPart = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($pathPart)) {
                continue
            }
            $resolved = [IO.Path]::GetFullPath((Join-Path `
                (Split-Path -Parent $document) `
                ([Uri]::UnescapeDataString($pathPart))))
            Assert-Condition -Condition (Test-Path -LiteralPath $resolved) `
                -Message "Broken Markdown link in ${relativeDocument}: $target"
        }
    }
    Write-Host "G8 Markdown links resolved: $($G8Documents.Count) documents."
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8A test result: $path"
    [xml]$result = Get-Content -LiteralPath $path -Raw
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests) `
        -Message "$ClassName ran $($suite.tests) tests; expected $ExpectedTests."
    Assert-Condition -Condition ([int]$suite.failures -eq 0) `
        -Message "$ClassName has $($suite.failures) failures."
    Assert-Condition -Condition ([int]$suite.errors -eq 0) `
        -Message "$ClassName has $($suite.errors) errors."
    Assert-Condition -Condition ([int]$suite.skipped -eq 0) `
        -Message "$ClassName has $($suite.skipped) skipped tests."
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures."
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string]$LogName,
        [Parameter(Mandatory)] [string[]]$Arguments
    )

    if (-not $AllowToolchainDownload) {
        $Arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogName
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $Arguments -LogPath $logPath `
            -Description "G8A Gradle gate" -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $Arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $Arguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "G8A Gradle gate failed with exit code $exitCode. See $logPath"
    }
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "g8a-intersections" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    [void](Assert-GeoCeDGFrozenG8Anchor -RepositoryRoot $RepositoryRoot)
    $frozenChangedPaths = @(Get-GeoCeDGFrozenChangedPaths `
        -RepositoryRoot $RepositoryRoot -BaseCommit $EntrySha)

    $requiredFiles = @(
        ".github\prompts\tasks\g8a-locus-v2-intersection-characterization.prompt.md",
        "geocedg\validation\locus-v2\g8a\g8a-intersection-characterization-evidence.json",
        "geocedg\validation\locus-v2\g8a\g8a-author-closeout-evidence.json",
        "geocedg\validation\locus-v2\g8a\generate_intersection_references.py",
        "geocedg\validation\locus-v2\g8a\intersection-reference-values.json",
        "geocedg\validation\locus-v2\g8a\g8a-evidence.sha256",
        "tools\agent\verify-g8a-intersections.ps1"
    ) + $G8Documents
    foreach ($requiredFile in $requiredFiles) {
        Assert-RequiredFile -RelativePath $requiredFile
    }

    $testSources = @(Get-ChildItem -LiteralPath $TestSourceRoot `
        -Filter "G8AIntersection*.java" -File)
    Assert-Condition -Condition ($testSources.Count -eq 10) `
        -Message "Expected exactly ten G8AIntersection test-private Java files."
    Assert-RequiredFile -RelativePath `
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus\G8ATargetAdapters.java"

    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8A entry SHA."
    $promptPath = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g8a-locus-v2-intersection-characterization.prompt.md"
    $actualPromptSha = (Get-FileHash -LiteralPath $promptPath `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    Assert-Condition -Condition ($actualPromptSha -eq $PromptSha) `
        -Message "The executed G8A prompt hash changed."

    Assert-EvidenceHashes
    Assert-MarkdownLinks

    $evidence = Get-Content -LiteralPath $EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($evidence.status -eq `
            "CHARACTERIZATION_COMPLETE_AWAITING_AUTHOR_REVIEW") `
        -Message "G8A evidence has the wrong phase status."
    Assert-Condition -Condition ($evidence.provenance.initialHead -eq $EntrySha `
            -and $evidence.provenance.promptSha256 -eq $PromptSha) `
        -Message "G8A evidence provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.characterizationProbes.total.tests `
            -eq 65 -and [int]$evidence.characterizationProbes.total.failures -eq 0 `
            -and [int]$evidence.characterizationProbes.total.errors -eq 0 `
            -and [int]$evidence.characterizationProbes.total.skipped -eq 0) `
        -Message "G8A evidence must record 65 passing probes."
    Assert-Condition -Condition ($evidence.completenessAxis.values.Count -eq 3 `
            -and $evidence.completenessAxis.completeEmptyValidated `
            -and $evidence.completenessAxis.verifiedIncompleteFiniteValidated `
            -and $evidence.completenessAxis.verifiedNotEstablishedFiniteValidated) `
        -Message "G8A completeness evidence is incomplete."
    Assert-Condition -Condition (-not [bool]$evidence.identityDecision.`
            isolatingIntervalIsFundamentalIdentity `
            -and -not [bool]$evidence.identityDecision.coordinateNearestNeighbourUsed) `
        -Message "G8A identity evidence uses a forbidden authority."
    Assert-Condition -Condition (-not [bool]$evidence.mergeSplitHypothesis.`
            universalGenealogyValidated) `
        -Message "G8A must not claim universal merge/split genealogy."
    Assert-Condition -Condition ($evidence.stateRecommendation.startingPoint -eq `
            "QUERY_LOCAL" -and -not [bool]$evidence.stateRecommendation.`
            g7MetricIndexReuse -and -not [bool]$evidence.stateRecommendation.`
            sharedIntersectionOwnerRecommendedForG8b) `
        -Message "G8A state recommendation escaped query-local scope."
    Assert-Condition -Condition ($evidence.g8bCandidate.status -eq `
            "NOT_AUTHORIZED_BLOCKED_ON_AUTHOR_REVIEW" `
            -and -not [bool]$evidence.g8bCandidate.productiveMainSourceAddedByG8a `
            -and -not [bool]$evidence.g8bCandidate.publicCommandAdded `
            -and -not [bool]$evidence.g8bCandidate.pathAdded `
            -and -not [bool]$evidence.g8bCandidate.xmlPersistenceAdded `
            -and -not [bool]$evidence.g8bCandidate.threeDimensionalBehaviorAdded `
            -and -not [bool]$evidence.g8bCandidate.g9BehaviorAdded) `
        -Message "G8A evidence claims unauthorized productive/public behavior."

    $closeout = Get-Content -LiteralPath $CloseoutEvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($closeout.status -eq "PASS_AUTHOR_APPROVED" `
            -and $closeout.authorReviewDate -eq "2026-08-14") `
        -Message "G8A author-closeout status or date is inconsistent."
    Assert-Condition -Condition ($closeout.characterizationEvidence.sha256 -eq `
            "feb6f024c4ef4a96b6cfa1be15bd3a37009af0106a55818f72d1c614a9f04cf4" `
            -and $closeout.characterizationEvidence.immutableMeasuredEvidence `
            -and [int]$closeout.characterizationEvidence.tests -eq 65) `
        -Message "G8A closeout does not preserve the measured evidence."
    $expectedDecisions = [ordered]@{
        D1 = "APPROVE_G8A_PASS_AUTHOR_APPROVED"
        D2 = "APPROVE_IMMUTABLE_RICH_SET_PLUS_NORMAL_DAG_NONNUMERIC_RICH_GEO_AUTHORITY"
        D3 = "APPROVE_REQUIRE_INTERNAL_TOKEN_SELECTED_DYNAMIC_POINT_CONSUMER"
        D4 = "APPROVE_INDEPENDENT_COMPLETE_INCOMPLETE_NOT_ESTABLISHED_AXIS"
        D5 = "APPROVE_NON_SIGN_ONLY_TANGENCY_AND_SEPARATE_MULTIPLICITY_EVIDENCE"
        D6 = "APPROVE_TYPED_OVERLAP_AND_INFINITE_RESULT_SEMANTICS"
        D7 = "APPROVE_CAPABILITY_HIERARCHY_AND_INDEPENDENT_VERIFICATION"
        D8 = "APPROVE_MEASURED_VALUES_WITH_NORMALIZATION_CONTRACT"
        D9 = "APPROVE_PROVISIONAL_DETERMINISTIC_WORK_BUDGETS"
        D10 = "APPROVE_DURABLE_IDENTITY_SEPARATE_FROM_REVISION_EVIDENCE"
        D11 = "APPROVE_REJECTION_OF_UNIVERSAL_MERGE_SPLIT_GENEALOGY"
        D12 = "APPROVE_QUERY_LOCAL_STATE_NO_SHARED_OWNER_OR_G7_INDEX"
        D13 = "APPROVE_LINE_SEGMENT_RAY_CIRCLE_MINIMUM"
        D14 = "APPROVE_LEVEL_C_AND_FULL_CONIC_DEFERRAL"
        D15 = "APPROVE_INTERNAL_ONLY_PUBLIC_BOUNDARIES_CLOSED"
        D16 = "APPROVE_APPEND_ONLY_DEDICATED_GEOCLASS_IF_REQUIRED"
        D17 = "APPROVE_NORMATIVE_G8_SPEC_AND_ACCEPT_ADR_0008"
    }
    foreach ($decision in $expectedDecisions.GetEnumerator()) {
        Assert-Condition -Condition ($closeout.decisions.($decision.Key) -eq `
                $decision.Value) `
            -Message "G8A closeout decision $($decision.Key) is inconsistent."
    }
    Assert-Condition -Condition ($closeout.resultAuthority.immutableRichIntersectionSet `
            -and $closeout.resultAuthority.normalDagNonnumericRichGeo `
            -and $closeout.resultAuthority.requiredInternalTokenSelectedPointConsumer `
            -and -not $closeout.resultAuthority.ordinaryPointIsAuthority `
            -and -not $closeout.resultAuthority.pointConsumerRetargetByCoordinate `
            -and $closeout.resultAuthority.pointConsumerUndefinedWhenAbsentStaleOrAmbiguous `
            -and $closeout.resultAuthority.pointConsumerRecoversOnlyForSameCurrentToken) `
        -Message "G8A closeout result or dynamic-point authority is inconsistent."
    Assert-Condition -Condition (($closeout.completeness.values -join ",") -eq `
            "COMPLETE,INCOMPLETE,NOT_ESTABLISHED" `
            -and $closeout.completeness.orthogonalToRootVerificationAndOtherResultAxes `
            -and $closeout.completeness.completeEmptyIsSuccessfulGeometry `
            -and -not $closeout.completeness.pointConsumerHidesIncompleteSet) `
        -Message "G8A closeout completeness policy is inconsistent."
    Assert-Condition -Condition ($closeout.tolerancePolicy.policyId -eq `
            "g8b-initial-normalized/v1" `
            -and [double]$closeout.tolerancePolicy.rootParameterTolerance -eq 1e-12 `
            -and [double]$closeout.tolerancePolicy.absoluteResidualTolerance -eq 2e-12 `
            -and [double]$closeout.tolerancePolicy.relativeResidualTolerance -eq 2e-12 `
            -and [double]$closeout.tolerancePolicy.tangencyThreshold -eq 1e-10 `
            -and [double]$closeout.tolerancePolicy.deduplicationParameterTolerance -eq 4e-12 `
            -and [double]$closeout.tolerancePolicy.continuationParameterTolerance -eq 1e-8 `
            -and [double]$closeout.tolerancePolicy.coordinateVerificationTolerance -eq 4e-12 `
            -and $closeout.tolerancePolicy.equationScalingInvariant `
            -and $closeout.tolerancePolicy.parameterQuantitiesBoundToProviderSemantics `
            -and $closeout.tolerancePolicy.tangencyUsesNormalizedContactIndicator `
            -and -not $closeout.tolerancePolicy.coordinateClosenessEstablishesIdentity) `
        -Message "G8A closeout normalized tolerance policy is inconsistent."
    Assert-Condition -Condition ([int]$closeout.workBudget.maximumSemanticEvaluations -eq 32768 `
            -and [int]$closeout.workBudget.maximumSemanticDerivativeEvaluations -eq 16384 `
            -and [int]$closeout.workBudget.maximumTargetEvaluations -eq 32768 `
            -and [int]$closeout.workBudget.maximumCandidateIntervals -eq 8192 `
            -and [int]$closeout.workBudget.maximumIsolationSubdivisions -eq 8192 `
            -and [int]$closeout.workBudget.maximumIsolationDepth -eq 40 `
            -and [int]$closeout.workBudget.maximumRefinementIterationsPerCandidate -eq 80 `
            -and [int]$closeout.workBudget.maximumResidualVerifications -eq 1024 `
            -and [int]$closeout.workBudget.maximumCandidateCount -eq 512 `
            -and [int]$closeout.workBudget.maximumContinuationComparisons -eq 4096 `
            -and [int]$closeout.workBudget.maximumPublishedFiniteSolutions -eq 256 `
            -and [int]$closeout.workBudget.maximumRetainedIntersectionIndexEntries -eq 0 `
            -and [int]$closeout.workBudget.maximumRetainedTopologyEpochs -eq 2 `
            -and -not $closeout.workBudget.wallClockGate) `
        -Message "G8A closeout deterministic work budgets are inconsistent."
    Assert-Condition -Condition (-not $closeout.identity.reparameterizationChangesIdentityByItself `
            -and -not $closeout.identity.universalMergeSplitGenealogy `
            -and $closeout.identity.ambiguousContinuationIsExplicit `
            -and -not $closeout.identity.nearestCartesianMatching) `
        -Message "G8A closeout identity policy is inconsistent."
    Assert-Condition -Condition ($closeout.stateAndScope.queryLocal `
            -and -not $closeout.stateAndScope.g7MetricIndexReuse `
            -and -not $closeout.stateAndScope.sharedIntersectionOwner `
            -and -not $closeout.stateAndScope.globalIntersectionCache `
            -and ($closeout.stateAndScope.minimumFamilies -join ",") -eq `
                "line,segment,ray,circle" `
            -and ($closeout.stateAndScope.deferredFamilies -join ",") -eq `
                "full conics,functions,general implicit curves,locus-locus") `
        -Message "G8A closeout state or family scope is inconsistent."
    $openedPublicBoundary = @($closeout.publicBoundaries.PSObject.Properties |
        Where-Object { [bool]$_.Value })
    Assert-Condition -Condition ($openedPublicBoundary.Count -eq 0) `
        -Message "G8A closeout opened a forbidden public/product boundary."
    Assert-Condition -Condition ($closeout.normativePromotion.g8Specification -eq `
            "NORMATIVE_AUTHOR_APPROVED" `
            -and $closeout.normativePromotion.adr0008 -eq "ACCEPTED" `
            -and $closeout.executionBoundary.g8b -eq "AUTHORIZED_NOT_STARTED" `
            -and -not $closeout.executionBoundary.productiveG8ImplementationAddedByG8a `
            -and -not $closeout.executionBoundary.g8bPromptExecutedByCloseout) `
        -Message "G8A closeout promotion or execution boundary is inconsistent."

    Assert-DocumentContains -RelativePath `
        "geocedg\specs\locus\locus-v2-intersections.md" -ExpectedText @(
            "NORMATIVE / AUTHOR-APPROVED R1 REFINEMENT APPLIED",
            "g8b-initial-normalized/v1",
            "token-selected point consumer")
    Assert-DocumentContains -RelativePath `
        "docs\adr\0008-locus-v2-intersection-result-and-continuation.md" `
        -ExpectedText @("Status: **Accepted — R1 clarification applied**",
            "universal merge/split genealogy")
    Assert-DocumentContains -RelativePath `
        ".github\prompts\tasks\g8b-locus-v2-intersection-kernel.prompt.md" `
        -ExpectedText @("AUTHORIZED / NOT STARTED", "token-selected point consumer",
            "g8b-initial-normalized/v1")

    if ($RequireFinalEvidence) {
        foreach ($field in @("focusedG8a", "operationalRegression",
                "locusV2Regression", "g7aRegression", "g7bRegression",
                "composedAuthority", "markdownLinks", "gitDiffCheck")) {
            Assert-Condition -Condition ($evidence.finalValidation.$field -eq "PASS") `
                -Message "G8A final evidence is not closed: $field"
        }
    }

    foreach ($source in $testSources) {
        $sourceText = Get-Content -LiteralPath $source.FullName -Raw
        foreach ($forbidden in @("LocusRenderCache2D", "myPointList",
                "getPointLength(", "getPoints(")) {
            Assert-Condition -Condition (-not $sourceText.Contains($forbidden)) `
                -Message "Forbidden render/legacy authority in $($source.Name): $forbidden"
        }
    }

    $productiveChanges = @($frozenChangedPaths | Where-Object {
            $_ -match '^source/.+/src/main/'
        })
    $productiveStatus = @()
    $g8bFollowOnPresent = (Test-Path -LiteralPath $G8BEvidencePath `
            -PathType Leaf) -and (Test-Path -LiteralPath $G8BVerifierPath `
            -PathType Leaf)
    $g8bAllowedCompatibilityTests = @()
    if (-not $g8bFollowOnPresent) {
        Assert-Condition -Condition ($productiveChanges.Count -eq 0 `
                -and $productiveStatus.Count -eq 0) `
            -Message ("G8A changed productive source:`n" +
                (($productiveChanges + $productiveStatus) -join "`n"))
    } else {
        $g8bEvidence = Get-Content -LiteralPath $G8BEvidencePath -Raw |
            ConvertFrom-Json -Depth 100
        Assert-Condition -Condition ($g8bEvidence.status -eq `
                "PASS_AUTHOR_APPROVED") `
            -Message "The detected G8B follow-on evidence has an invalid status."
        $g8bAllowedProductive = @(
            "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java",
            "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java",
            "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java",
            "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java",
            "source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java"
        )
        $g8bAllowedCompatibilityTests = @(
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2KernelIntegrationTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusMetricProductiveLifecycleTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8AIntersectionKernelLifecycleCharacterizationTest.java",
            "source/shared/common-jre/src/test/java/org/geogebra/common/euclidian/DrawablesTest.java"
        )
        $compatibilityDifference = @(Compare-Object `
                -ReferenceObject @($g8bAllowedCompatibilityTests | Sort-Object) `
                -DifferenceObject @($g8bEvidence.productiveSource.compatibilityTestFilesModified |
                    Sort-Object))
        Assert-Condition -Condition ($compatibilityDifference.Count -eq 0) `
            -Message "G8B compatibility-test evidence does not match the exact delegated allowlist."
        $unexpectedProductive = @(($productiveChanges +
                ($productiveStatus | ForEach-Object {
                    ($_ -replace '^..\s+', '').Replace("\", "/")
                })) | Sort-Object -Unique | Where-Object {
                $_ -notmatch '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$' `
                    -and $_ -notin $g8bAllowedProductive
            })
        Assert-Condition -Condition ($unexpectedProductive.Count -eq 0) `
            -Message ("Post-G8A productive source escaped the G8B boundary:`n" +
                ($unexpectedProductive -join "`n"))
        Write-Host "G8A historical no-product boundary delegated to the versioned G8B verifier."
    }

    $sourceChanges = @($frozenChangedPaths | Where-Object {
            $_ -match '^source/'
        })
    $g8cDesignPresent = (Test-Path -LiteralPath $G8CEvidencePath `
            -PathType Leaf) -and (Test-Path -LiteralPath $G8CVerifierPath `
            -PathType Leaf)
    $g8cAllowedCharacterizationTests = @()
    if ($g8cDesignPresent) {
        $g8cEvidence = Get-Content -LiteralPath $G8CEvidencePath -Raw |
            ConvertFrom-Json -Depth 100
        Assert-Condition -Condition ($g8cEvidence.phase -eq "G8C_DESIGN" `
                -and $g8cEvidence.status -eq `
                    "PASS_AUTHOR_APPROVED") `
            -Message "The detected G8C design evidence has an invalid status."
        $g8cAllowedCharacterizationTests = @(
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CCharacterizationSupport.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CDesignFunctionalBenchmarkTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CExtendedTargetCharacterizationTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CLocusLocusCharacterizationTest.java"
        )
    }
    $g8c1Present = (Test-Path -LiteralPath $G8C1EvidencePath `
            -PathType Leaf) -and (Test-Path -LiteralPath $G8C1VerifierPath `
            -PathType Leaf)
    $g8c1AllowedTests = @()
    if ($g8c1Present) {
        $g8c1Evidence = Get-Content -LiteralPath $G8C1EvidencePath -Raw |
            ConvertFrom-Json -Depth 100
        Assert-Condition -Condition ($g8c1Evidence.phase -eq "G8C1" `
                -and $g8c1Evidence.status -eq "PASS_AUTHOR_APPROVED") `
            -Message "The detected G8C1 evidence has an invalid status."
        $g8c1AllowedTests = @(
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetFunctionalBenchmarkTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetKernelTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetLifecycleTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1IntersectionTestSupport.java"
        )
    }
    $g8c2Present = (Test-Path -LiteralPath $G8C2EvidencePath `
            -PathType Leaf) -and (Test-Path -LiteralPath $G8C2VerifierPath `
            -PathType Leaf)
    $g8c2AllowedTests = @()
    if ($g8c2Present) {
        $g8c2Evidence = Get-Content -LiteralPath $G8C2EvidencePath -Raw |
            ConvertFrom-Json -Depth 100
        Assert-Condition -Condition ($g8c2Evidence.phase -eq "G8C2" `
                -and $g8c2Evidence.status -eq `
                    "PASS_AUTHOR_APPROVED") `
            -Message "The detected G8C2 closeout evidence has an invalid status."
        $g8c2AllowedTests = @(
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2IntersectionTestSupport.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairFunctionalBenchmarkTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairKernelTest.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairLifecycleTest.java"
        )
    }
    $unexpectedSource = @($sourceChanges | Where-Object {
            $path = $_
            $isG8A = $path -match `
                '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8A(Intersection.*|TargetAdapters)\.java$'
            $isG8BTest = $g8bFollowOnPresent -and $path -match `
                '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8BIntersection.*\.java$'
            $isG8BCompatibility = $g8bFollowOnPresent -and
                $path -in $g8bAllowedCompatibilityTests
            $isG8BProductive = $g8bFollowOnPresent -and
                ($path -match `
                    '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$' `
                -or $path -in $g8bAllowedProductive)
            $isG8CDesignTest = $g8cDesignPresent -and
                $path -in $g8cAllowedCharacterizationTests
            $isG8C1Test = $g8c1Present -and $path -in $g8c1AllowedTests
            $isG8C2Test = $g8c2Present -and $path -in $g8c2AllowedTests
            -not ($isG8A -or $isG8BTest -or $isG8BCompatibility -or
                $isG8BProductive -or $isG8CDesignTest -or $isG8C1Test -or
                $isG8C2Test)
        })
    Assert-Condition -Condition ($unexpectedSource.Count -eq 0) `
        -Message ("G8A source changes escaped the test-private boundary:`n" +
            ($unexpectedSource -join "`n"))

    Write-Host "`n==> Independent G8A numerical references"
    $referenceLog = Join-Path $LogDirectory "g8a-independent-references.log"
    & conda run --no-capture-output -n om_env python `
        $ReferenceGenerator --check 2>&1 | Tee-Object -FilePath $referenceLog
    if ($LASTEXITCODE -ne 0) {
        throw "Independent G8A references failed. See $referenceLog"
    }

    if (-not $SkipBuild) {
        Write-Host "`n==> Test-private G8A characterization probes"
        $testArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($testClass in $TestClasses.Keys) {
            $testArguments += @("--tests", $testClass)
        }
        $testArguments += @("--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--console=plain")
        Invoke-LoggedGradle -LogName "g8a-intersection-characterization.log" `
            -Arguments $testArguments

        foreach ($entry in $TestClasses.GetEnumerator()) {
            Assert-TestResult -ClassName $entry.Key -ExpectedTests $entry.Value
        }

        Write-Host "`n==> G8A test-source checkstyle"
        Invoke-LoggedGradle -LogName "g8a-checkstyle-test.log" -Arguments @(
            ":shared:common-jre:checkstyleTest", "--rerun-tasks",
            "--no-build-cache", "--no-daemon", "--console=plain")
        Assert-Condition -Condition (Test-Path -LiteralPath $CheckstyleResult `
                -PathType Leaf) -Message "Missing G8A checkstyle XML result."
        $checkstyleErrors = @(Select-String -LiteralPath $CheckstyleResult `
            -Pattern "<error ")
        Assert-Condition -Condition ($checkstyleErrors.Count -eq 0) `
            -Message "G8A checkstyle reported $($checkstyleErrors.Count) violations."
    } else {
        Write-Host "Skipping G8A Gradle probes because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G8A author-closeout verification passed (65 probes)."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G8A build output"
    }
    if ($null -ne $InitialStatus) {
        $FinalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($FinalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G8A verification."
            exit 1
        }
    }
}

exit 0

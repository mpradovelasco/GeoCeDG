[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g8c2-intersections")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "a585591afd073ec390ffc34a532d390472277150"
$G8C1Commit = "3c72e889a436e4bbccde177e1f24423196575f04"
$G8C1Tag = "geocedg-g8c1-pass"
$PromptSha = "e7f0535332a9c5a2789f98476aef2f9f143e84f9bda0ad3b7d657f070d99e58b"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8c2"
$EvidencePath = Join-Path $EvidenceRoot "g8c2-intersection-kernel-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8c2-evidence.sha256"
$ReferenceGenerator = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c\generate_extended_intersection_references.py"
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$InitialStatus = $null
$GeneratedSnapshot = $null

$TestClasses = [ordered]@{
    "org.geocedg.common.locus.G8C2LocusPairKernelTest" = 16
    "org.geocedg.common.locus.G8C2LocusPairLifecycleTest" = 10
    "org.geocedg.common.locus.G8C2LocusPairFunctionalBenchmarkTest" = 8
}

$ProductiveFiles = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/EvaluatorPairIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionCandidateSet2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionOverlapEvidence2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSemanticMetadata2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSourceBinding2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocalPairIsolationEvidence2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionContinuation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionInstrumentationSnapshot2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionResult2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionSolution2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIdentity2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionCandidate2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionCandidateSet2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionEvidence2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionInstrumentation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionPolicy2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionQuery2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionSolver2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionWorkBudget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairResidualEvidence2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairRootTokenSource2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairSourceRevisionEvidence2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusSourceBinding2D.java"
)

$TestFiles = @(
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2IntersectionTestSupport.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairKernelTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairLifecycleTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairFunctionalBenchmarkTest.java"
)

$Documents = @(
    "docs/adr/0008-locus-v2-intersection-result-and-continuation.md",
    "docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md",
    "docs/architecture/locus_v2_extended_intersection_architecture.md",
    "docs/architecture/locus_v2_extended_intersection_capability_matrix.md",
    "docs/architecture/locus_v2_extended_intersection_semantic_model.md",
    "docs/architecture/locus_v2_extended_intersection_upstream_impact.md",
    "docs/architecture/locus_v2_intersection_architecture.md",
    "docs/architecture/locus_v2_intersection_semantic_model.md",
    "docs/architecture/locus_v2_intersection_upstream_impact.md",
    "docs/developer/locus_v2_extended_intersection_api.md",
    "docs/developer/locus_v2_intersection_api.md",
    "docs/roadmap/g8_locus_v2_intersections_plan.md",
    "docs/roadmap/g8c_locus_v2_extended_intersections_design.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/user/geocedg_user_guide.md",
    "docs/validation/g8c2_locus_v2_locus_intersection_kernel_report.md",
    "docs/validation/g8c2_locus_v2_locus_intersection_traceability_matrix.md",
    "docs/validation/g8c_locus_v2_extended_intersection_benchmark_plan.md",
    "docs/validation/g8c_locus_v2_extended_intersection_scientific_traceability.md",
    "docs/validation/g8c_locus_v2_extended_intersection_validation_matrix.md",
    "geocedg/specs/locus/locus-v2-extended-intersections.md",
    "geocedg/specs/locus/locus-v2-intersections.md"
)

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

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    Assert-Condition -Condition (Test-Path -LiteralPath `
            (Join-Path $RepositoryRoot $RelativePath) -PathType Leaf) `
        -Message "Required G8C2 artifact is missing: $RelativePath"
}

function Assert-Contains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Text
    )

    $content = Get-Content -LiteralPath `
        (Join-Path $RepositoryRoot $RelativePath) -Raw
    foreach ($requiredText in $Text) {
        Assert-Condition -Condition ($content.Contains($requiredText)) `
            -Message "$RelativePath does not contain: $requiredText"
    }
}

function Assert-EvidenceHashes {
    [void](Assert-GeoCeDGFrozenHashManifest -RepositoryRoot $RepositoryRoot `
        -ManifestPath $EvidenceHashes)
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $Documents) {
        if (-not $relativeDocument.EndsWith(".md")) {
            continue
        }
        $document = Join-Path $RepositoryRoot $relativeDocument
        $content = Get-Content -LiteralPath $document -Raw
        foreach ($match in [regex]::Matches(
                $content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $match.Groups["target"].Value.Trim().Trim("<", ">")
            if ($target.StartsWith("#") -or $target -match '^(https?|mailto):') {
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
    Write-Host "G8C2 Markdown links resolved: $($Documents.Count) documents."
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8C2 test result: $path"
    [xml]$result = Get-Content -LiteralPath $path -Raw
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "G8C2 test result is not clean: $ClassName"
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8C2 Checkstyle result: $RelativePath"
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G8C2 Checkstyle contains $($errors.Count) violations."
}

function Invoke-ReferenceCheck {
    $logPath = Join-Path $LogDirectory "g8c2-independent-references.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & conda run -n om_env python $ReferenceGenerator --check 2>&1 |
            Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C2 independent references failed. See $logPath"
}

function Invoke-G8C2Tests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test", "--tests",
        "org.geocedg.common.locus.G8C2*",
        ":shared:common:checkstyleMain",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain",
        "--no-problems-report"
    )
    $logPath = Join-Path $LogDirectory "g8c2-intersection-tests.log"
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $arguments -LogPath $logPath `
            -Description "G8C2 intersection tests" -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $arguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C2 Gradle gate failed. See $logPath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    [void](Assert-GeoCeDGFrozenG8Anchor -RepositoryRoot $RepositoryRoot)
    if (-not $SkipBuild) {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "g8c2" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($file in @(
            ".github/prompts/tasks/g8c2-locus-v2-locus-intersections.prompt.md",
            "geocedg/validation/locus-v2/g8c2/g8c2-contract-review-evidence.json",
            "geocedg/validation/locus-v2/g8c2/g8c2-intersection-kernel-evidence.json",
            "geocedg/validation/locus-v2/g8c2/g8c2-evidence.sha256",
            "geocedg/validation/locus-v2/g8c/extended-intersection-reference-values.json",
            "tools/agent/verify-g8c2-intersections.ps1") +
            $Documents + $ProductiveFiles + $TestFiles) {
        Assert-RequiredFile -RelativePath $file
    }

    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8C2 contract gate."
    $tagTarget = (& git -C $RepositoryRoot rev-parse "$G8C1Tag^{}" ).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and `
            $tagTarget -eq $G8C1Commit) `
        -Message "$G8C1Tag does not identify the G8C1 baseline."
    $promptPath = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g8c2-locus-v2-locus-intersections.prompt.md"
    Assert-Condition -Condition ((Get-GeoCeDGFrozenCanonicalTextSha256 `
            -RepositoryRoot $RepositoryRoot -Path $promptPath) -eq `
            $PromptSha) -Message "Canonical G8C2 prompt hash changed."

    Assert-Contains -RelativePath `
        "geocedg/specs/locus/locus-v2-extended-intersections.md" -Text @(
            "G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED",
            "canonical source pair", "Option B", "Local isolation",
            "finite component products and periodic branches")
    Assert-Contains -RelativePath `
        "docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md" -Text @(
            "Status: **Accepted**", "canonical unordered pair",
            "dedicated query-local dual-parameter solver")
    Assert-Contains -RelativePath "docs/roadmap/geocedg_roadmap.md" -Text @(
        "G8C1 = PASS — AUTHOR APPROVED",
        "G8C2 = PASS — AUTHOR APPROVED",
        "G8 = PASS — AUTHOR APPROVED",
        "G9 DESIGN = AUTHORIZED — NOT STARTED",
        "G9 IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED")
    Assert-Contains -RelativePath `
        "docs/validation/g8c2_locus_v2_locus_intersection_kernel_report.md" `
        -Text @("PASS — AUTHOR APPROVED",
            "34 tests", "NOT_ESTABLISHED", "120")

    $changedPaths = @(Get-GeoCeDGFrozenChangedPaths `
        -RepositoryRoot $RepositoryRoot -BaseCommit $EntrySha)
    foreach ($path in $changedPaths) {
        if ($path -match '^source/.+/src/main/') {
            Assert-Condition -Condition ($path -in $ProductiveFiles) `
                -Message "Unapproved productive G8C2 source edit: $path"
        }
        if ($path -match '^source/.+/src/test/.+\.java$') {
            Assert-Condition -Condition ($path -in $TestFiles) `
                -Message "Unapproved G8C2 test source edit: $path"
        }
        foreach ($forbiddenPath in @(
                "/commands/", "CmdIntersect", "AlgoDispatcher", "GeoLocus.java",
                "/Path.java", "GeoFactory", "/io/", "geogebra3D", "kernel3D",
                "/export/", "python/", "artifacts/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G8C2 scope edit: $path"
        }
    }

    $manifest = Get-GeoCeDGFrozenJson -RepositoryRoot $RepositoryRoot `
        -Path "docs/upstream/modified-files.yml"
    $registered = @($manifest.modifications | ForEach-Object { $_.path })
    Assert-Condition -Condition (@($registered | Group-Object | Where-Object `
            Count -gt 1).Count -eq 0) `
        -Message "The upstream modified-file inventory contains duplicate paths."
    foreach ($path in @($changedPaths | Where-Object { $_ -match '^source/' })) {
        Assert-Condition -Condition ($path -in $registered) `
            -Message "Changed source/test path is not registered: $path"
    }

    $sourceText = ($ProductiveFiles | ForEach-Object {
            Get-Content -LiteralPath (Join-Path $RepositoryRoot $_) -Raw
        }) -join "`n"
    foreach ($forbiddenAuthority in @(
            "LocusRenderCache2D", "myPointList", "EuclidianView",
            "LocusMetricIndex", "AlgoDispatcher")) {
        Assert-Condition -Condition (-not $sourceText.Contains(
                $forbiddenAuthority)) `
            -Message "Forbidden G8C2 authority dependency: $forbiddenAuthority"
    }
    foreach ($requiredContract in @(
            "LocusPairIdentity2D", "LocusPairIntersectionSolver2D",
            "EvaluatorPairIntersectionCapability2D",
            "LocalPairIsolationEvidence2D", "Completeness.NOT_ESTABLISHED",
            "OVERLAP_SUSPECTED_NOT_ESTABLISHED",
            "normalizedTangentDeterminant", "maximumParameterBoxes")) {
        Assert-Condition -Condition ($sourceText.Contains($requiredContract)) `
            -Message "Missing productive G8C2 contract: $requiredContract"
    }

    $evidence = Get-GeoCeDGFrozenJson -RepositoryRoot $RepositoryRoot `
        -Path $EvidencePath
    Assert-Condition -Condition ($evidence.status -eq `
            "PASS_AUTHOR_APPROVED" -and
            $evidence.provenance.entrySha -eq $EntrySha -and
            $evidence.provenance.canonicalPromptCanonicalTextSha256 -eq `
                $PromptSha) -Message "G8C2 evidence provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.tests.total.tests -eq 34 -and
            [int]$evidence.tests.total.failures -eq 0 -and
            [int]$evidence.tests.total.errors -eq 0 -and
            [int]$evidence.tests.total.skipped -eq 0) `
        -Message "G8C2 evidence must record 34 clean tests."
    Assert-Condition -Condition ([bool]$evidence.architecture.canonicalUnorderedSourcePair `
            -and [bool]$evidence.architecture.dedicatedDualParameterSolver `
            -and [bool]$evidence.architecture.queryLocal `
            -and [int]$evidence.architecture.retainedPairEntries -eq 0 `
            -and -not [bool]$evidence.architecture.g7MetricStateReuse) `
        -Message "G8C2 architecture evidence is inconsistent."
    Assert-Condition -Condition ([bool]$evidence.completenessAndAdmissibility.optionBPreserved `
            -and -not [bool]$evidence.completenessAndAdmissibility.parentCompleteRequiredForPoint `
            -and [bool]$evidence.completenessAndAdmissibility.localIsolationRequiredForPoint `
            -and $evidence.completenessAndAdmissibility.defaultEvaluatorCompleteness -eq `
                "NOT_ESTABLISHED") `
        -Message "G8C2 completeness, isolation, or Option B evidence is inconsistent."
    Assert-Condition -Condition ([int]$evidence.representativeCounterSnapshot.semanticEvaluations `
            -eq 120 -and
            [int]$evidence.representativeCounterSnapshot.parameterBoxesVisited `
            -eq 1024 -and
            [int]$evidence.representativeCounterSnapshot.pairRefinementIterations `
            -eq 8 -and
            [int]$evidence.representativeCounterSnapshot.retainedPairEntries `
            -eq 0) -Message "G8C2 counter baseline is inconsistent."
    foreach ($zeroScope in @(
            "publicCommands", "dispatcherOverloads", "publicPathAdditions",
            "xmlOrPersistenceRegistrations", "legacyGeoLocusChanges",
            "classicIntersectionChanges", "frontendChanges",
            "threeDimensionalChanges", "g9Changes",
            "sharedOrGlobalIntersectionCache",
            "renderSampleOrViewportAuthority", "g7MetricIndexAuthority",
            "generatedTrackedArtifacts")) {
        Assert-Condition -Condition ([int]$evidence.scopeAudit.$zeroScope -eq 0) `
            -Message "G8C2 forbidden scope gate is nonzero: $zeroScope"
    }
    Assert-Condition -Condition (-not [bool]$evidence.authorReview.required -and
            [bool]$evidence.authorReview.implementationPassClaimed -and
            $evidence.authorReview.disposition -eq "PASS_AUTHOR_APPROVED" -and
            @($evidence.authorReview.decisions.PSObject.Properties.Value |
                    Where-Object { $_ -notlike "APPROVED*" }).Count -eq 0 -and
            $evidence.phaseDisposition.g8c2 -eq "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g8 -eq "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g9Design -eq `
                "AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.g9Implementation -eq `
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $evidence.globalG8Closeout.disposition -eq `
                "PASS_AUTHOR_APPROVED" -and
            [bool]$evidence.globalG8Closeout.fundamentalDynamicIncidenceChainEstablished `
            -and [bool]$evidence.globalG8Closeout.locusLocusIncluded -and
            [bool]$evidence.globalG8Closeout.typedLimitationsAreNormativeBoundaries `
            -and -not [bool]$evidence.globalG8Closeout.universalCurveSupportClaimed) `
        -Message "G8C2 author-review disposition is inconsistent."

    Assert-EvidenceHashes
    Assert-MarkdownLinks
    Invoke-ReferenceCheck

    if (-not $SkipBuild) {
        Invoke-G8C2Tests
        foreach ($entry in $TestClasses.GetEnumerator()) {
            Assert-TestResult -ClassName $entry.Key -ExpectedTests $entry.Value
        }
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
    } else {
        Write-Host "Skipping G8C2 Gradle tests because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G8C2 verification passed (34 focused tests)."
    Write-Host "G8C2 and global G8 are PASS - AUTHOR APPROVED."
    Write-Host "G9 design is authorized/not started; implementation is not authorized."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G8C2 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G8C2 verification."
            exit 1
        }
    }
}

exit 0

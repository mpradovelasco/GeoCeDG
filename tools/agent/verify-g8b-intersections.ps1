[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g8b-intersections")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "6529a4ebfafa5dc9dca3cc1b4c3e7a89ebcba375"
$PromptSha = "7ea46214e3a8f88fe5b79e41417495386d01728d3c0a8f015d87c1df75ea51ac"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$SourceRoot = Join-Path $RepositoryRoot `
    "source\shared\common\src\main\java\org\geocedg\common\kernel\locus\intersection"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8b"
$EvidencePath = Join-Path $EvidenceRoot `
    "g8b-intersection-kernel-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8b-evidence.sha256"
$InitialStatus = $null
$GeneratedSnapshot = $null

$Documents = @(
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
    "docs\validation\g8b_locus_v2_intersection_kernel_report.md",
    "docs\validation\g8b_locus_v2_intersection_traceability_matrix.md",
    "docs\validation\g8b_r1_locus_v2_intersection_point_admissibility_report.md",
    "docs\user\geocedg_user_guide.md",
    ".github\prompts\tasks\g8b-locus-v2-intersection-kernel.prompt.md"
)

$TestClasses = [ordered]@{
    "org.geocedg.common.locus.G8BIntersectionKernelTest" = 25
    "org.geocedg.common.locus.G8BIntersectionLifecycleTest" = 15
    "org.geocedg.common.locus.G8BIntersectionTopologyAndScientificTest" = 9
}

. $GeneratedStateHelper

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
        -Message "Required G8B artifact is missing: $RelativePath"
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
    foreach ($line in Get-Content -LiteralPath $EvidenceHashes) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2) `
            -Message "Malformed G8B evidence hash line: $line"
        $path = Join-Path $RepositoryRoot $parts[1].Trim().Replace("/", "\")
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed G8B artifact is missing: $path"
        Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $path) -eq `
                $parts[0].ToLowerInvariant()) `
            -Message "G8B evidence hash mismatch: $path"
    }
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $Documents) {
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
    Write-Host "G8B Markdown links resolved: $($Documents.Count) documents."
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8B test result: $path"
    [xml]$result = Get-Content -LiteralPath $path -Raw
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("{0} is not clean: tests={1}, failures={2}, errors={3}, " +
            "skipped={4}." -f $ClassName, $suite.tests, $suite.failures,
            $suite.errors, $suite.skipped)
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing checkstyle result: $RelativePath"
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) violations."
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
    Write-Host "`n==> G8B productive shared-kernel tests and Checkstyle"
    Write-Host "    log: $logPath"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "G8B Gradle gate failed with exit code $exitCode. See $logPath"
    }
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g8b-intersections"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    $requiredFiles = @(
        ".github\prompts\tasks\g8b-locus-v2-intersection-kernel.prompt.md",
        "geocedg\validation\locus-v2\g8b\g8b-intersection-kernel-evidence.json",
        "geocedg\validation\locus-v2\g8b\g8b-evidence.sha256",
        "tools\agent\verify-g8b-intersections.ps1",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\algos\AlgoLocusIntersectionV2.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\algos\AlgoLocusIntersectionPointV2.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\geos\GeoLocusIntersectionResult.java",
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus\G8BIntersectionFixtures.java",
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus\G8BIntersectionKernelTest.java",
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus\G8BIntersectionLifecycleTest.java",
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus\G8BIntersectionTopologyAndScientificTest.java"
    ) + $Documents
    foreach ($file in $requiredFiles) {
        Assert-RequiredFile -RelativePath $file
    }

    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8B entry SHA."
    $promptPath = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g8b-locus-v2-intersection-kernel.prompt.md"
    Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $promptPath) `
            -eq $PromptSha) -Message "The canonical G8B prompt changed."

    Assert-Contains -RelativePath `
        "geocedg\specs\locus\locus-v2-intersections.md" -Text @(
            "NORMATIVE / AUTHOR-APPROVED R1 REFINEMENT APPLIED",
            'G8B = PASS — AUTHOR APPROVED',
            'G8C DESIGN = PASS — AUTHOR APPROVED'
        )
    Assert-Contains -RelativePath `
        "docs\adr\0008-locus-v2-intersection-result-and-continuation.md" `
        -Text @("Status: **Accepted — R1 clarification applied**")

    $sourceFiles = @(Get-ChildItem -LiteralPath $SourceRoot -Filter "*.java" -File)
    Assert-Condition -Condition ($sourceFiles.Count -ge 33) `
        -Message "Expected at least the 33 productive G8B intersection package files."
    foreach ($sourceFile in $sourceFiles) {
        $source = Get-Content -LiteralPath $sourceFile.FullName -Raw
        foreach ($forbidden in @(
                "import org.geocedg.common.kernel.locus.metric",
                "LocusRenderCache2D", "myPointList", "getPointLength()",
                "getPoints()", "import org.geogebra.common.euclidian",
                "ExecutorService", "java.util.concurrent")) {
            Assert-Condition -Condition (-not $source.Contains($forbidden)) `
                -Message "Forbidden G8B dependency in $($sourceFile.Name): $forbidden"
        }
    }

    Assert-Contains -RelativePath `
        "source\shared\common\src\main\java\org\geocedg\common\kernel\locus\intersection\EvaluatorOnlyIntersectionCapability2D.java" `
        -Text @("refineAbsoluteMinimum", "Completeness.NOT_ESTABLISHED")
    $fallback = Get-Content -LiteralPath (Join-Path $SourceRoot `
        "EvaluatorOnlyIntersectionCapability2D.java") -Raw
    Assert-Condition -Condition (-not $fallback.Contains("Completeness.COMPLETE")) `
        -Message "Evaluator-only fallback must never claim complete coverage."

    $richGeoPath = `
        "source\shared\common\src\main\java\org\geocedg\common\kernel\geos\GeoLocusIntersectionResult.java"
    $richGeo = Get-Content -LiteralPath (Join-Path $RepositoryRoot $richGeoPath) -Raw
    Assert-Condition -Condition (-not $richGeo.Contains("NumberValue") -and
            -not $richGeo.Contains("implements Path") -and
            $richGeo.Contains("ValueType.VOID") -and
            $richGeo.Contains("GeoClass.LOCUS_INTERSECTION_RESULT")) `
        -Message "The G8B rich Geo must stay nonnumeric, non-Path and dedicated."

    $pointPath = `
        "source\shared\common\src\main\java\org\geocedg\common\kernel\algos\AlgoLocusIntersectionPointV2.java"
    $pointSource = Get-Content -LiteralPath `
        (Join-Path $RepositoryRoot $pointPath) -Raw
    Assert-Condition -Condition ($pointSource.Contains(
            ".findPointAdmissibleSolution(selectedRootToken)") -and
            -not $pointSource.Contains("distance(")) `
        -Message "The G8B point consumer must select by token, never coordinates."
    $resultPath = Join-Path $SourceRoot "LocusIntersectionResult2D.java"
    $resultSource = Get-Content -LiteralPath $resultPath -Raw
    $pointAdmissibilityMethod = [regex]::Match($resultSource,
        '(?s)public Optional<LocusIntersectionSolution2D> findPointAdmissibleSolution\(.*?(?=\s+private boolean isLocallyPointAdmissible)').Value
    Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace(
            $pointAdmissibilityMethod) -and
            -not $pointAdmissibilityMethod.Contains("completenessEvidence")) `
        -Message "Point admissibility must be independent of global completeness."
    Assert-Condition -Condition ($resultSource.Contains(
            "LocalIsolationStatus.ESTABLISHED") -and
            $resultSource.Contains("IdentityStatus.CONTINUATION_ESTABLISHED") -and
            $resultSource.Contains("IdentityStatus.NEW_TOPOLOGICAL_SOLUTION")) `
        -Message "The G8B-R1 solution-local admissibility evidence is incomplete."

    $geoClass = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "source\shared\common\src\main\java\org\geogebra\common\plugin\GeoClass.java") -Raw
    Assert-Condition -Condition ($geoClass.Contains(
            'LOCUS_INTERSECTION_RESULT("LocusIntersectionResult",') -and
            $geoClass.Contains('"locusintersectionresult", 132, false);')) `
        -Message "The append-only G8B GeoClass value is missing or changed."

    $changedPaths = @(& git -C $RepositoryRoot diff --name-only $EntrySha --)
    $changedPaths += @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    $changedPaths = @($changedPaths | Sort-Object -Unique)
    $allowedProductive = @(
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java",
        "source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java"
    )
    foreach ($path in $changedPaths) {
        if ($path -match '^source/.+/src/main/') {
            $inPackage = $path -match `
                '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$'
            Assert-Condition -Condition ($inPackage -or $path -in $allowedProductive) `
                -Message "Unapproved productive G8B source edit: $path"
        }
        foreach ($forbiddenPath in @(
                "/commands/", "CmdIntersect", "AlgoDispatcher", "GeoLocus.java",
                "GeoLocusV2.java", "/Path.java", "GeoFactory", "/io/",
                "geogebra3D", "kernel3D", "/export/", "python/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G8B scope edit: $path"
        }
    }

    $manifest = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\upstream\modified-files.yml") -Raw | ConvertFrom-Json -Depth 100
    $registered = @($manifest.modifications | ForEach-Object { $_.path })
    foreach ($path in @($changedPaths | Where-Object { $_ -match '^source/' })) {
        Assert-Condition -Condition ($path -in $registered) `
            -Message "Changed source/test path is not registered: $path"
    }

    $evidence = Get-Content -LiteralPath $EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($evidence.status -eq `
            "PASS_AUTHOR_APPROVED") `
        -Message "G8B evidence has the wrong phase status."
    Assert-Condition -Condition ($evidence.provenance.entrySha -eq $EntrySha -and
            $evidence.provenance.canonicalPromptCanonicalTextSha256 -eq `
                $PromptSha) -Message "G8B evidence provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.tests.total.tests -eq 49 -and
            [int]$evidence.tests.total.failures -eq 0 -and
            [int]$evidence.tests.total.errors -eq 0 -and
            [int]$evidence.tests.total.skipped -eq 0) `
        -Message "G8B evidence must record 49 clean productive tests."
    Assert-Condition -Condition ($evidence.completeness.evaluatorFallbackCanClaimComplete `
            -eq $false -and [bool]$evidence.completeness.completeEmptyValidated `
            -and [bool]$evidence.resultAuthority.internalTokenSelectedPointConsumer) `
        -Message "G8B completeness or point-consumer evidence is inconsistent."
    Assert-Condition -Condition ($evidence.g8bR1.status -eq
            "PASS_AUTHOR_APPROVED" -and
            [bool]$evidence.resultAuthority.pointAdmissibilityIsSolutionLocal -and
            -not [bool]$evidence.resultAuthority.pointRequiresCompleteFiniteCurrentSuccess -and
            [bool]$evidence.resultAuthority.parentCompletenessRemainsVisible -and
            [bool]$evidence.resultAuthority.localIsolationRequired -and
            [int]$evidence.g8bR1.performance.consumerSemanticEvaluationIncrease -eq 0 -and
            [int]$evidence.g8bR1.performance.consumerRetainedIndexEntries -eq 0) `
        -Message "G8B-R1 point-admissibility evidence is inconsistent."
    Assert-Condition -Condition (-not [bool]$evidence.authorReview.required -and
            [bool]$evidence.authorReview.implementationPassClaimed -and
            $evidence.authorReview.disposition -eq
                "APPROVED_BOUNDED_INTERNAL_MINIMUM" -and
            $evidence.phaseDisposition.g8bR1 -eq "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g8b -eq "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g8cDesign -eq
                "AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.g8cImplementation -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.g8 -eq "IN_PROGRESS" -and
            $evidence.phaseDisposition.g9 -eq "NOT_STARTED") `
        -Message "G8B author-closeout phase disposition is inconsistent."
    Assert-Condition -Condition ([int]$evidence.workBudget.maximumSemanticEvaluations `
            -eq 32768 -and [int]$evidence.workBudget.maximumIsolationSubdivisions `
            -eq 8192 -and [int]$evidence.workBudget.maximumRetainedIntersectionIndexEntries `
            -eq 0 -and [int]$evidence.workBudget.maximumRetainedTopologyEpochs `
            -eq 2) -Message "G8B deterministic budgets are inconsistent."
    foreach ($zeroGate in @(
            "renderCacheReads", "renderVertexReads", "legacySampleReads",
            "viewportReads", "pixelToleranceReads", "metricIndexReads",
            "wholeLocusRegenerations", "retainedIntersectionIndexEntries")) {
        Assert-Condition -Condition ([int]$evidence.functionalGates.$zeroGate `
                -eq 0) -Message "G8B forbidden-authority gate is nonzero: $zeroGate"
    }
    foreach ($zeroScope in @(
            "publicCommands", "dispatcherOverloads", "publicPathAdditions",
            "xmlOrPersistenceRegistrations", "legacyGeoLocusChanges",
            "classicIntersectionChanges", "frontendChanges", "exportChanges",
            "threeDimensionalChanges", "pythonDslChanges", "g9Changes")) {
        Assert-Condition -Condition ([int]$evidence.scopeAudit.$zeroScope -eq 0) `
            -Message "G8B forbidden scope gate is nonzero: $zeroScope"
    }

    Assert-EvidenceHashes
    Assert-MarkdownLinks

    if (-not $SkipBuild) {
        Invoke-LoggedGradle -LogName "g8b-common.log" -Arguments @(
            ":shared:common-jre:test", "--tests",
            "org.geocedg.common.locus.G8BIntersection*",
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            "--rerun-tasks", "--no-daemon", "--console=plain"
        )
        foreach ($entry in $TestClasses.GetEnumerator()) {
            Assert-TestResult -ClassName $entry.Key -ExpectedTests $entry.Value
        }
        Assert-CheckstyleResult -RelativePath `
            "source\shared\common\build\reports\checkstyle\main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source\shared\common-jre\build\reports\checkstyle\test.xml"
    } else {
        Write-Host "Skipping G8B Gradle tests because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G8B-R1/G8B author-closeout verification passed (49 productive tests)."
    Write-Host "G8C design is author-approved; later approved extensions do not weaken the G8B baseline."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G8B build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G8B verification."
            exit 1
        }
    }
}

exit 0

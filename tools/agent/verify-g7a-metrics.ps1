[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$ReproduceCharacterization,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g7a-metrics")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$OriginMainSha = "726abd95be928e232f3a3f7c6b637605b46d0cb1"
$PlanningSha = "e918846a73829032ab1e1aff37e863fed40c1969"
$PromptSha = "4820bf0934b84f3ea84ec5f30930a0be56769c150940ce53483e1150232fab39"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$CheckstyleResult = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\reports\checkstyle\test.xml"
$ReferenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g7a"
$ReferenceGenerator = Join-Path $ReferenceRoot "generate_metric_references.py"
$ReferenceHashes = Join-Path $ReferenceRoot "metric-reference-values.sha256"
$EvidencePath = Join-Path $ReferenceRoot "g7a-characterization-evidence.json"
$R1EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g7a-r1"
$R1EvidencePath = Join-Path $R1EvidenceRoot `
    "g7a-r1-characterization-evidence.json"
$R1EvidenceHashes = Join-Path $R1EvidenceRoot "g7a-r1-evidence.sha256"
$G8BEvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8b\g8b-intersection-kernel-evidence.json"
$G8BVerifierPath = Join-Path $PSScriptRoot "verify-g8b-intersections.ps1"
$G7MarkdownDocuments = @(
    "docs\roadmap\g7_locus_v2_metrics_plan.md",
    "docs\roadmap\geocedg_roadmap.md",
    "docs\architecture\locus_v2_metric_semantic_model.md",
    "docs\architecture\locus_v2_metric_architecture.md",
    "geocedg\specs\locus\locus-v2-metrics.md",
    "docs\adr\0007-revision-scoped-locus-v2-metric-index.md",
    "docs\validation\g7_locus_v2_metric_validation_matrix.md",
    "docs\validation\g7_locus_v2_metric_benchmark_plan.md",
    "docs\validation\g7a_locus_v2_metric_characterization_report.md",
    "docs\validation\g7a_r1_locus_v2_metric_refinement_report.md",
    "docs\validation\g7a_locus_v2_metric_traceability_matrix.md",
    "docs\developer\locus_v2_metric_api.md",
    "docs\user\geocedg_user_guide.md"
)
$InitialStatus = $null
$GeneratedSnapshot = $null

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
    $canonicalContent = $content.Replace("`r`n", "`n").Replace("`r", "`n")
    $encoding = [Text.UTF8Encoding]::new($false)
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $hashBytes = $sha256.ComputeHash($encoding.GetBytes($canonicalContent))
        return [Convert]::ToHexString($hashBytes).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)
    $path = Join-Path $RepositoryRoot $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Required G7A artifact is missing: $RelativePath"
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )
    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G7A test result: $path"
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

function Assert-ReferenceHashes {
    foreach ($line in Get-Content -LiteralPath $ReferenceHashes) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2) `
            -Message "Malformed reference hash line: $line"
        $path = Join-Path $ReferenceRoot $parts[1].Trim()
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed reference artifact is missing: $path"
        $actual = Get-CanonicalTextSha256 -Path $path
        Assert-Condition -Condition ($actual -eq $parts[0].ToLowerInvariant()) `
            -Message "G7A reference hash mismatch: $path"
    }
}

function Assert-R1EvidenceHashes {
    foreach ($line in Get-Content -LiteralPath $R1EvidenceHashes) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2) `
            -Message "Malformed R1 evidence hash line: $line"
        $relativePath = $parts[1].Trim().Replace("/", "\")
        $path = Join-Path $RepositoryRoot $relativePath
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed R1 evidence artifact is missing: $path"
        $actual = Get-CanonicalTextSha256 -Path $path
        Assert-Condition -Condition ($actual -eq $parts[0].ToLowerInvariant()) `
            -Message "G7A-R1 evidence hash mismatch: $path"
    }
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $G7MarkdownDocuments) {
        $document = Join-Path $RepositoryRoot $relativeDocument
        Assert-Condition -Condition (Test-Path -LiteralPath $document -PathType Leaf) `
            -Message "Missing G7 Markdown document: $relativeDocument"
        $content = Get-Content -LiteralPath $document -Raw
        foreach ($match in [regex]::Matches($content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $match.Groups["target"].Value.Trim()
            if ($target.StartsWith("<") -and $target.EndsWith(">")) {
                $target = $target.Substring(1, $target.Length - 2)
            }
            if ($target.StartsWith("#") -or
                    $target -match '^(https?|mailto):') {
                continue
            }
            $pathPart = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($pathPart)) {
                continue
            }
            $decodedPath = [Uri]::UnescapeDataString($pathPart)
            $resolved = [IO.Path]::GetFullPath((Join-Path `
                (Split-Path -Parent $document) $decodedPath))
            Assert-Condition -Condition (Test-Path -LiteralPath $resolved) `
                -Message "Broken Markdown link in ${relativeDocument}: $target"
        }
    }
    Write-Host "G7 Markdown relative links resolved: $($G7MarkdownDocuments.Count) documents."
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g7a-metrics"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($relativePath in @(
            "docs\validation\g7a_locus_v2_metric_characterization_report.md",
            "docs\validation\g7a_locus_v2_metric_traceability_matrix.md",
            "docs\developer\locus_v2_metric_api.md",
            "geocedg\validation\locus-v2\g7a\g7a-characterization-evidence.json",
            "geocedg\validation\locus-v2\g7a\metric-reference-values.json",
            "geocedg\validation\locus-v2\g7a\metric-reference-values.sha256",
            "docs\validation\g7a_r1_locus_v2_metric_refinement_report.md",
            "geocedg\validation\locus-v2\g7a-r1\g7a-r1-characterization-evidence.json",
            "geocedg\validation\locus-v2\g7a-r1\g7a-r1-evidence.sha256",
            "tools\agent\verify-g7a-metrics.ps1")) {
        Assert-RequiredFile -RelativePath $relativePath
    }

    if ($ReproduceCharacterization) {
        $currentBranch = ((& git -C $RepositoryRoot branch --show-current) `
            -join "").Trim()
        Assert-Condition -Condition ($currentBranch -eq `
                "feature/g7a-locus-v2-metric-characterization") `
            -Message ("G7A characterization reproduction requires the " +
                "characterization branch; got $currentBranch.")
    }
    & git -C $RepositoryRoot merge-base --is-ancestor $OriginMainSha $PlanningSha
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Planning SHA does not descend from the pinned G7A origin/main SHA."
    & git -C $RepositoryRoot merge-base --is-ancestor $PlanningSha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current G7A/G7B branch does not descend from the planning SHA."

    $promptPath = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g7a-locus-v2-metric-characterization.prompt.md"
    $actualPromptSha = Get-CanonicalTextSha256 -Path $promptPath
    Assert-Condition -Condition ($actualPromptSha -eq $PromptSha) `
        -Message "The executed G7A prompt hash changed."
    Assert-ReferenceHashes
    Assert-R1EvidenceHashes
    Assert-MarkdownLinks

    $evidence = Get-Content -LiteralPath $EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($evidence.status -eq `
            "PASS_AUTHOR_APPROVED") `
        -Message "G7A evidence must record final author approval."
    Assert-Condition -Condition ($evidence.provenance.planningSha -eq `
            $PlanningSha) -Message "G7A evidence planning SHA mismatch."
    Assert-Condition -Condition ($evidence.provenance.originMainSha -eq `
            $OriginMainSha) -Message "G7A evidence origin/main SHA mismatch."
    Assert-Condition -Condition ($evidence.provenance.promptSha256 -eq `
            $PromptSha) -Message "G7A evidence prompt SHA mismatch."
    Assert-Condition -Condition ([int]$evidence.characterizationProbes.total.tests `
            -eq 37 -and [int]$evidence.characterizationProbes.total.failures `
            -eq 0) -Message "G7A evidence must record 37 passing probes."
    Assert-Condition -Condition (-not [bool]$evidence.sourceAudit.productiveMetricSourceAdded) `
        -Message "G7A evidence claims productive metric source."
    Assert-Condition -Condition ($evidence.phaseDisposition.g7a -eq `
            "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g7b -eq "AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.adr0007 -eq "ACCEPTED" -and
            $evidence.phaseDisposition.g7Spec -eq `
                "NORMATIVE_AUTHOR_APPROVED") `
        -Message "G7A evidence final phase disposition is inconsistent."

    $r1Evidence = Get-Content -LiteralPath $R1EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($r1Evidence.status -eq "PASS_AUTHOR_APPROVED") `
        -Message "G7A-R1 evidence must record final author approval."
    Assert-Condition -Condition ($r1Evidence.provenance.headAndPlanningSha -eq `
            $PlanningSha) -Message "G7A-R1 evidence planning SHA mismatch."
    Assert-Condition -Condition ($r1Evidence.provenance.executedG7APromptSha256 -eq `
            $PromptSha) -Message "G7A-R1 executed-prompt SHA mismatch."
    Assert-Condition -Condition ([int]$r1Evidence.probes.r1Total.tests -eq 14 `
            -and [int]$r1Evidence.probes.r1Total.failures -eq 0) `
        -Message "G7A-R1 evidence must record 14 passing focused probes."
    Assert-Condition -Condition ([int]$r1Evidence.probes.g7aRegression.tests -eq 37 `
            -and [int]$r1Evidence.probes.g7aRegression.failures -eq 0) `
        -Message "G7A-R1 evidence must preserve the 37-probe regression."
    Assert-Condition -Condition ([int]$r1Evidence.phaseDisposition.productiveG7SourceChanges `
            -eq 0) -Message "G7A-R1 evidence claims productive G7 source changes."
    Assert-Condition -Condition ($r1Evidence.phaseDisposition.g7aR1 -eq `
            "PASS_AUTHOR_APPROVED" -and
            $r1Evidence.phaseDisposition.g7a -eq "PASS_AUTHOR_APPROVED" -and
            $r1Evidence.phaseDisposition.g7b -eq "AUTHORIZED_NOT_STARTED" -and
            $r1Evidence.phaseDisposition.adr0007 -eq "ACCEPTED" -and
            $r1Evidence.phaseDisposition.g7Spec -eq `
                "NORMATIVE_AUTHOR_APPROVED") `
        -Message "G7A-R1 evidence final phase disposition is inconsistent."
    $componentStateBoundary = $r1Evidence.multiConsumer.componentStateBoundary
    Assert-Condition -Condition (-not [bool]$componentStateBoundary.keyIncludesEndpoints `
            -and -not [bool]$componentStateBoundary.sharedContributions) `
        -Message "R1 evidence violates the component-state boundary."
    Assert-Condition -Condition ($r1Evidence.multiConsumer.recommendation -eq `
            "DEDICATED_SHARED_OWNER") `
        -Message "G7A-R1 ownership recommendation is missing or changed."
    $r1N100 = $r1Evidence.multiConsumer.compatibleSameComponent.PSObject.Properties[
        "100"].Value
    $r1Shared100 = $r1N100.PSObject.Properties[
        "DEDICATED_SHARED_OWNER"].Value
    Assert-Condition -Condition ([int]$r1Shared100.builds -eq 1 -and
            [int]$r1Shared100.crossResultHits -eq 99) `
        -Message "G7A-R1 N=100 shared-owner evidence mismatch."

    $report = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g7a_locus_v2_metric_characterization_report.md") -Raw
    Assert-Condition -Condition ($report.Contains(
            "REEXECUTED FROM VERSIONED G6/G6R + RESTORED G7 PLANNING BASELINE")) `
        -Message "G7A recovery declaration is missing."
    Assert-Condition -Condition ($report.Contains("PRIOR UNVERSIONED G7A RESULTS =`r`nNOT USED") `
            -or $report.Contains("PRIOR UNVERSIONED G7A RESULTS =`nNOT USED")) `
        -Message "G7A prior-results declaration is missing."
    Assert-Condition -Condition ($report.Contains(
            "G7A = PASS — AUTHOR APPROVED")) `
        -Message "G7A author-approved PASS status is missing."
    Assert-Condition -Condition ($report.Contains(
            "G7B = AUTHORIZED / NOT STARTED")) `
        -Message "G7B must remain authorized and not started."
    Assert-Condition -Condition ($report.Contains("G8 = NOT STARTED")) `
        -Message "G8 must remain not started."

    $r1Report = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g7a_r1_locus_v2_metric_refinement_report.md") -Raw
    foreach ($requiredText in @(
            "METRIC_VALUE_REPRESENTATION = AUTHOR_APPROVED",
            "MULTI-METRIC OPTIMIZATION =",
            "DEDICATED_SHARED_OWNER",
            "ONE COMPONENT-STATE BUILD PER COMPLETE KEY UNTIL EVICTION OR INVALIDATION",
            "PRODUCTIVE G7 SOURCE CHANGES =`n0",
            "G7A-R1 = PASS — AUTHOR APPROVED",
            "G7A = PASS — AUTHOR APPROVED",
            "G7B = AUTHORIZED / NOT STARTED",
            "ADR 0007 = ACCEPTED",
            "G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED")) {
        $normalizedReport = $r1Report.Replace("`r`n", "`n")
        Assert-Condition -Condition ($normalizedReport.Contains($requiredText)) `
            -Message "G7A-R1 report is missing required text: $requiredText"
    }

    $decisionRows = @([regex]::Matches($report,
            '(?m)^\|\s*(?<id>(?:[1-9]|[1-3][0-9]|4[0-2]))\s*\|'))
    $decisionSequence = ($decisionRows | ForEach-Object {
            [int]$_.Groups["id"].Value
        }) -join ","
    Assert-Condition -Condition ($decisionRows.Count -eq 42 -and
            $decisionSequence -eq ((1..42) -join ",")) `
        -Message "The mandatory G7A decision table must contain rows 1 through 42 exactly once."

    $r1DecisionRows = @([regex]::Matches($r1Report,
            '(?m)^\|\s*R1-(?<id>(?:[1-9]|1[0-9]|2[0-2]))\s*\|'))
    $r1DecisionSequence = ($r1DecisionRows | ForEach-Object {
            [int]$_.Groups["id"].Value
        }) -join ","
    Assert-Condition -Condition ($r1DecisionRows.Count -eq 22 -and
            $r1DecisionSequence -eq ((1..22) -join ",")) `
        -Message "The mandatory G7A-R1 decision table must contain R1-1 through R1-22 exactly once."

    $traceability = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g7a_locus_v2_metric_traceability_matrix.md") -Raw
    $traceHeader = "| ID | requirement | source | decision | normative spec | ADR | " +
        "fixture | probe | evidence | G7B gate | user guide |"
    Assert-Condition -Condition ($traceability.Contains($traceHeader)) `
        -Message "The G7A traceability matrix is missing its required columns."
    $traceRows = @([regex]::Matches($traceability,
            '(?m)^\|\s*D(?<id>(?:0[1-9]|[1-3][0-9]|4[0-2]))\s*\|'))
    $traceSequence = ($traceRows | ForEach-Object {
            [int]$_.Groups["id"].Value
        }) -join ","
    Assert-Condition -Condition ($traceRows.Count -eq 42 -and
            $traceSequence -eq ((1..42) -join ",")) `
        -Message "The G7A traceability matrix must contain D01 through D42 exactly once."
    $r1TraceRows = @([regex]::Matches($traceability,
            '(?m)^\|\s*R1-(?<id>(?:[1-9]|1[0-9]|2[0-2]))\s*\|'))
    $r1TraceSequence = ($r1TraceRows | ForEach-Object {
            [int]$_.Groups["id"].Value
        }) -join ","
    Assert-Condition -Condition ($r1TraceRows.Count -eq 22 -and
            $r1TraceSequence -eq ((1..22) -join ",")) `
        -Message "The traceability matrix must contain R1-1 through R1-22 exactly once."
    foreach ($closeoutId in @("CLOSEOUT-1", "CLOSEOUT-2", "CLOSEOUT-3")) {
        Assert-Condition -Condition ($traceability.Contains("| $closeoutId |")) `
            -Message "The traceability matrix is missing $closeoutId."
    }
    foreach ($bindingText in @("normative G7 metric spec", "Accepted ADR 0007",
            "G7A raw evidence", "G7A-R1 raw evidence",
            "executed G7B implementation gate")) {
        Assert-Condition -Condition ($traceability.Contains($bindingText)) `
            -Message "The accepted traceability binding is missing: $bindingText"
    }

    $validationMatrix = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g7_locus_v2_metric_validation_matrix.md") -Raw
    foreach ($matrixId in @(
            "R1-VALUE-ABSENT", "R1-ERROR-NONE", "R1-GUARANTEE-G6",
            "R1-WORK-BUDGET", "R1-MULTI-1", "R1-MULTI-3",
            "R1-MULTI-10", "R1-MULTI-100", "R1-MULTI-TOTAL-FIRST",
            "R1-MULTI-LOCAL-FIRST", "R1-MULTI-POLICY",
            "R1-MULTI-REVISION", "R1-MULTI-TOPOLOGY",
            "R1-MULTI-CONSTRUCTION", "R1-MULTI-REMOVE",
            "R1-MULTI-NESTED", "CLOSEOUT-COMPONENT-STATE",
            "CLOSEOUT-TRAVERSAL-OPTIONAL", "CLOSEOUT-ERROR-CLOSED")) {
        Assert-Condition -Condition ($validationMatrix.Contains("| $matrixId |")) `
            -Message "The validation matrix is missing required R1 ID: $matrixId"
    }

    $benchmarkPlan = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g7_locus_v2_metric_benchmark_plan.md") -Raw
    foreach ($benchmarkId in @(
            "BM-G7-MULTI-CONSUMER", "BM-G7-MULTI-CONSUMER-MIXED",
            "BM-G7-MULTI-CONSUMER-TOTAL",
            "BM-G7-MULTI-CONSUMER-REVISION",
            "BM-G7-MULTI-CONSUMER-NESTED",
            "BM-G7-MULTI-CONSUMER-MEMORY")) {
        Assert-Condition -Condition ($benchmarkPlan.Contains($benchmarkId)) `
            -Message "The benchmark plan is missing required R1 family: $benchmarkId"
    }

    $spec = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "geocedg\specs\locus\locus-v2-metrics.md") -Raw
    Assert-Condition -Condition ($spec.Contains(
            "APPROVED AS NORMATIVE G7 METRIC CONTRACT")) `
        -Message "G7 metric spec is not normative/author-approved."
    foreach ($requiredSpecText in @("MetricValue2D", "MetricErrorEvidence2D",
            "LocusSemanticMetadata2D.NumericGuarantee", "metricWorkBudget",
            "DEDICATED_SHARED_OWNER", "LAZY_COMPONENT_REVISION",
            "LocusMetricComponentState2D", "Optional<TraversalOutcome>",
            "EstablishedMetricErrorAmount2D", "LIMIT_NOT_ESTABLISHED")) {
        Assert-Condition -Condition ($spec.Contains($requiredSpecText)) `
            -Message "G7 metric spec is missing R1 contract: $requiredSpecText"
    }
    $adr = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\adr\0007-revision-scoped-locus-v2-metric-index.md") -Raw
    Assert-Condition -Condition ($adr.Contains("Status: **Accepted**")) `
        -Message "ADR 0007 is not Accepted."
    Assert-Condition -Condition ($adr.Contains(
            "APPROVED AS G7A WORKING ARCHITECTURAL HYPOTHESIS")) `
        -Message "ADR 0007 working-hypothesis disposition is missing."
    Assert-Condition -Condition ($adr.Contains("DEDICATED_SHARED_OWNER") `
            -and $adr.Contains("LAZY_COMPONENT_REVISION") `
            -and $adr.Contains("LocusMetricComponentState2D") `
            -and $adr.Contains("metricWorkBudget")) `
        -Message "ADR 0007 is missing the R1 ownership/work-key recommendation."

    $g7bPrompt = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g7b-locus-v2-metric-kernel.prompt.md") -Raw
    foreach ($promptContract in @("G7A = PASS — AUTHOR APPROVED",
            "G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED",
            "ADR 0007 = ACCEPTED", "DEDICATED_SHARED_OWNER — AUTHOR APPROVED",
            "LocusMetricComponentState2D + LocusMetricRouteSegment2D",
            "Optional<TraversalOutcome>", "EstablishedMetricErrorAmount2D")) {
        Assert-Condition -Condition ($g7bPrompt.Contains($promptContract)) `
            -Message "The G7B prompt is missing approved contract: $promptContract"
    }

    $closeoutDocumentContracts = @{
        "docs\roadmap\g7_locus_v2_metrics_plan.md" =
            "G7B = PASS — AUTHOR APPROVED"
        "docs\roadmap\geocedg_roadmap.md" =
            "G7B = PASS — AUTHOR APPROVED"
        "docs\architecture\locus_v2_metric_semantic_model.md" =
            "Author-approved G7A/G7A-R1 semantic model"
        "docs\architecture\locus_v2_metric_architecture.md" =
            "Author-approved G7A/G7A-R1 architecture"
        "docs\developer\locus_v2_metric_api.md" =
            "G7A-R1, G7A AND G7B PASS — AUTHOR APPROVED"
        "docs\validation\g7_locus_v2_metric_validation_matrix.md" =
            "PASS — AUTHOR APPROVED"
        "docs\validation\g7_locus_v2_metric_benchmark_plan.md" =
            "PASS — AUTHOR APPROVED"
        "docs\user\geocedg_user_guide.md" =
            "internal productive metric available"
    }
    foreach ($contract in $closeoutDocumentContracts.GetEnumerator()) {
        $documentText = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
            $contract.Key) -Raw
        Assert-Condition -Condition ($documentText.Contains($contract.Value)) `
            -Message ("Closeout document {0} is missing: {1}" -f `
                $contract.Key, $contract.Value)
    }

    $testSourceRoot = Join-Path $RepositoryRoot `
        "source\shared\common-jre\src\test\java\org\geocedg\common\locus"
    $g7aSources = @(Get-ChildItem -LiteralPath $testSourceRoot `
        -Filter "G7A*.java" -File)
    Assert-Condition -Condition ($g7aSources.Count -eq 11) `
        -Message "Expected exactly eleven test-private G7A/R1 Java source files."
    foreach ($source in $g7aSources) {
        $sourceText = Get-Content -LiteralPath $source.FullName -Raw
        foreach ($forbidden in @("LocusRenderCache2D", "myPointList",
                "getPointLength()", "getPoints()")) {
            Assert-Condition -Condition (-not $sourceText.Contains($forbidden)) `
                -Message "Forbidden render/legacy metric access in $($source.Name): $forbidden"
        }
    }
    $semanticModelSource = Get-Content -LiteralPath (Join-Path $testSourceRoot `
        "G7AMetricSemanticModel.java") -Raw
    Assert-Condition -Condition (-not $semanticModelSource.Contains(
            "enum NumericGuarantee")) `
        -Message "G7A/R1 must not declare a duplicate metric NumericGuarantee enum."
    foreach ($semanticContract in @(
            "sealed interface MetricErrorAmount2D permits",
            "EstablishedMetricErrorAmount2D",
            "NotEstablishedMetricErrorAmount2D",
            "NotApplicableMetricErrorAmount2D",
            "Optional<TraversalOutcome> traversalOutcome")) {
        Assert-Condition -Condition ($semanticModelSource.Contains(
                $semanticContract)) `
            -Message "Test-private semantic model is missing: $semanticContract"
    }
    Assert-Condition -Condition (-not $semanticModelSource.Contains(
            "record MetricErrorAmount2D(")) `
        -Message "MetricErrorAmount2D must be a closed variant hierarchy."
    Assert-Condition -Condition (-not $semanticModelSource.Contains(
            "traversalOutcome == null")) `
        -Message "Rich result traversal must not use null."

    $multiOwnerSource = Get-Content -LiteralPath (Join-Path $testSourceRoot `
        "G7AR1MultiConsumerMetricIndexExperiment.java") -Raw
    foreach ($ownerContract in @(
            "record LocusMetricComponentState2D",
            "getOrBuildState",
            "LocusMetricComponentState2D state",
            "LocusMetricRouteSegment2D segment")) {
        Assert-Condition -Condition ($multiOwnerSource.Contains($ownerContract)) `
            -Message "Test-private shared owner is missing: $ownerContract"
    }
    Assert-Condition -Condition (-not $multiOwnerSource.Contains(
            "Map<FullKey, LocusMetricContribution2D>")) `
        -Message "Shared owner must not cache route-specific contributions."

    $productiveChanges = @(& git -C $RepositoryRoot diff --name-only `
        $PlanningSha -- ":(glob)source/**/src/main/**")
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to audit productive source changes from planning baseline."
    }
    if ($ReproduceCharacterization) {
        Assert-Condition -Condition ($productiveChanges.Count -eq 0) `
            -Message ("G7A changed productive source:`n" +
                ($productiveChanges -join "`n"))
    } else {
        $g8bFollowOnPresent = (Test-Path -LiteralPath $G8BEvidencePath `
                -PathType Leaf) -and (Test-Path -LiteralPath $G8BVerifierPath `
                -PathType Leaf)
        if ($g8bFollowOnPresent) {
            $g8bEvidence = Get-Content -LiteralPath $G8BEvidencePath -Raw |
                ConvertFrom-Json -Depth 100
            Assert-Condition -Condition ($g8bEvidence.status -eq `
                    "PASS_AUTHOR_APPROVED") `
                -Message "The detected G8B follow-on evidence has an invalid status."
        }
        $approvedG7BProductivePaths = @(
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/',
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetric(V2|ScalarAdapter)\.java$',
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocus(MetricResult|V2)\.java$',
            '^source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass\.java$',
            '^source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2LaboratoryController\.java$'
        )
        if ($g8bFollowOnPresent) {
            $approvedG7BProductivePaths += @(
                '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$',
                '^source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersection(Point)?V2\.java$',
                '^source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult\.java$'
            )
        }
        $unexpectedG7BProductive = @($productiveChanges | Where-Object {
                $candidate = $_
                -not @($approvedG7BProductivePaths | Where-Object {
                        $candidate -match $_
                    })
            })
        Assert-Condition -Condition ($unexpectedG7BProductive.Count -eq 0) `
            -Message ("Post-G7A productive source escaped its approved G7B/G8B boundary:`n" +
                ($unexpectedG7BProductive -join "`n"))
    }

    Write-Host "`n==> Independent G7A numerical references"
    & conda run --no-capture-output -n om_env python $ReferenceGenerator --check
    if ($LASTEXITCODE -ne 0) {
        throw "Independent G7A numerical references failed with exit code $LASTEXITCODE."
    }

    if (-not $SkipBuild) {
        Write-Host "`n==> Test-private G7A characterization probes"
        $arguments = @(
            ":shared:common-jre:test", "--tests",
            "org.geocedg.common.locus.G7AMetricSemanticCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.G7AMetricNumericalCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.G7AMetricIndexCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.G7ANestedMetricCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.G7AMetricGeoLifecycleCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.G7AR1MetricValueAndWorkBudgetTest",
            "--tests",
            "org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexTest",
            ":shared:common-jre:checkstyleTest", "--rerun-tasks",
            "--no-build-cache", "--no-daemon", "--console=plain"
        )
        if (-not $AllowToolchainDownload) {
            $arguments += "-Dorg.gradle.java.installations.auto-download=false"
        }
        $logPath = Join-Path $LogDirectory "g7a-metric-characterization-gradle.log"
        Push-Location -LiteralPath $RepositoryRoot
        try {
            & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
            $exitCode = $LASTEXITCODE
        } finally {
            Pop-Location
        }
        if ($exitCode -ne 0) {
            throw "G7A Gradle characterization failed with exit code $exitCode. See $logPath"
        }
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AMetricSemanticCharacterizationTest" `
            -ExpectedTests 10
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AMetricNumericalCharacterizationTest" `
            -ExpectedTests 10
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AMetricIndexCharacterizationTest" `
            -ExpectedTests 8
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7ANestedMetricCharacterizationTest" `
            -ExpectedTests 4
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AMetricGeoLifecycleCharacterizationTest" `
            -ExpectedTests 5
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AR1MetricValueAndWorkBudgetTest" `
            -ExpectedTests 6
        Assert-TestResult -ClassName `
            "org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexTest" `
            -ExpectedTests 8
        Assert-Condition -Condition (Test-Path -LiteralPath $CheckstyleResult `
                -PathType Leaf) -Message "Missing G7A checkstyle XML result."
        $checkstyleErrors = @(Select-String -LiteralPath $CheckstyleResult `
            -Pattern "<error ")
        Assert-Condition -Condition ($checkstyleErrors.Count -eq 0) `
            -Message "G7A test checkstyle reported $($checkstyleErrors.Count) violations."
    } else {
        Write-Host "Skipping G7A Gradle probes because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --check failed with exit code $LASTEXITCODE."
    }
    & git -C $RepositoryRoot diff --cached --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --cached --check failed with exit code $LASTEXITCODE."
    }

    Write-Host "G7A metric characterization verification passed (37 original + 14 R1)."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G7A build output"
    }
    if ($null -ne $InitialStatus) {
        $FinalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($FinalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G7A verification."
            exit 1
        }
    }
}

exit 0

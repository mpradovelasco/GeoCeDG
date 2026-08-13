[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g7b-metrics")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "bb3623dbd5945b558f42ff1a6f2d9ce4262cb983"
$PlanningSha = "e918846a73829032ab1e1aff37e863fed40c1969"
$VersionedPromptSha = "11e938be2788902298722d2e0442c9afb5700e1f7512b9b22732248d61af1c11"
$ExecutedPromptSha = "c215a36a8350e5dd44da9ae3e546899d8ab0cf1abc480aec22666fc363f19aed"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$CommonTestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$DesktopTestResultRoot = Join-Path $RepositoryRoot `
    "source\desktop\desktop\build\test-results\test"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g7b"
$EvidencePath = Join-Path $EvidenceRoot "g7b-metric-kernel-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g7b-evidence.sha256"
$MetricSourceRoot = Join-Path $RepositoryRoot `
    "source\shared\common\src\main\java\org\geocedg\common\kernel\locus\metric"
$InitialStatus = $null
$GeneratedSnapshot = $null

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

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$Path)

    $content = [IO.File]::ReadAllText($Path)
    $canonical = $content.Replace("`r`n", "`n").Replace("`r", "`n")
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $hex = [Convert]::ToHexString($sha256.ComputeHash(
            [Text.UTF8Encoding]::new($false).GetBytes($canonical)))
        return $hex.ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    Assert-Condition -Condition (Test-Path -LiteralPath `
        (Join-Path $RepositoryRoot $RelativePath) -PathType Leaf) `
        -Message "Required G7B artifact is missing: $RelativePath"
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $Root ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G7B test result: $path"
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
        -Message "$RelativePath contains $($errors.Count) checkstyle violations."
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$LogName,
        [Parameter(Mandatory)] [string]$Description
    )

    if (-not $AllowToolchainDownload) {
        $Arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogName
    Write-Host "`n==> $Description"
    Write-Host "    log: $logPath"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "$Description failed with exit code $exitCode. See $logPath"
    }
}

function Assert-EvidenceHashes {
    foreach ($line in Get-Content -LiteralPath $EvidenceHashes) {
        if ([string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2) `
            -Message "Malformed G7B evidence hash line: $line"
        $path = Join-Path $RepositoryRoot $parts[1].Trim().Replace("/", "\")
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed G7B evidence artifact is missing: $path"
        Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $path) -eq `
                $parts[0].ToLowerInvariant()) `
            -Message "G7B evidence hash mismatch: $path"
    }
}

function Assert-MarkdownLinks {
    param([Parameter(Mandatory)] [string[]]$Documents)

    foreach ($relativeDocument in $Documents) {
        $document = Join-Path $RepositoryRoot $relativeDocument
        $content = Get-Content -LiteralPath $document -Raw
        foreach ($match in [regex]::Matches($content,
                '\[[^\]]*\]\((?<target>[^)]+)\)')) {
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
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g7b-metrics"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    $requiredFiles = @(
        "source\shared\common\src\main\java\org\geocedg\common\kernel\geos\GeoLocusMetricResult.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\algos\AlgoLocusMetricV2.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\algos\AlgoLocusMetricScalarAdapter.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\locus\metric\LocusMetricComponentState2D.java",
        "source\shared\common\src\main\java\org\geocedg\common\kernel\locus\metric\LocusMetricSharedOwner2D.java",
        "docs\validation\g7b_locus_v2_metric_kernel_report.md",
        "docs\validation\g7b_locus_v2_metric_traceability_matrix.md",
        "geocedg\validation\locus-v2\g7b\g7b-metric-kernel-evidence.json",
        "geocedg\validation\locus-v2\g7b\g7b-evidence.sha256",
        "tools\agent\verify-g7b-metrics.ps1"
    )
    foreach ($file in $requiredFiles) {
        Assert-RequiredFile -RelativePath $file
    }

    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    Assert-Condition -Condition ($branch -eq `
            "feature/g7b-locus-v2-metric-kernel") `
        -Message "G7B verifier requires the G7B feature branch; got $branch."
    & git -C $RepositoryRoot merge-base --is-ancestor $PlanningSha $EntrySha
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "G7B entry SHA does not descend from the planning SHA."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current branch does not descend from the approved G7A closeout."
    $versionedPrompt = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g7b-locus-v2-metric-kernel.prompt.md"
    Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $versionedPrompt) `
            -eq $VersionedPromptSha) `
        -Message "The approved versioned G7B prompt changed during execution."

    $spec = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "geocedg\specs\locus\locus-v2-metrics.md") -Raw
    $adr = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\adr\0007-revision-scoped-locus-v2-metric-index.md") -Raw
    Assert-Condition -Condition ($spec.Contains(
            "APPROVED AS NORMATIVE G7 METRIC CONTRACT")) `
        -Message "The G7 metric spec is not normative."
    Assert-Condition -Condition ($adr.Contains("Status: **Accepted**")) `
        -Message "ADR 0007 is not Accepted."

    $metricFiles = @(Get-ChildItem -LiteralPath $MetricSourceRoot `
        -Filter "*.java" -File)
    Assert-Condition -Condition ($metricFiles.Count -ge 70) `
        -Message "The productive G7B metric package is incomplete."
    foreach ($metricFile in $metricFiles) {
        $source = Get-Content -LiteralPath $metricFile.FullName -Raw
        foreach ($forbidden in @("import org.geocedg.common.euclidian",
                "myPointList", "getPointLength()", "getPoints()",
                "java.util.concurrent", "ExecutorService")) {
            Assert-Condition -Condition (-not $source.Contains($forbidden)) `
                -Message "Forbidden metric dependency in $($metricFile.Name): $forbidden"
        }
    }
    $richGeo = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "source\shared\common\src\main\java\org\geocedg\common\kernel\geos\GeoLocusMetricResult.java") -Raw
    Assert-Condition -Condition (-not $richGeo.Contains("NumberValue") -and
            -not $richGeo.Contains("implements Path")) `
        -Message "The rich metric Geo must not be a NumberValue or Path."
    foreach ($requiredContract in @("Optional<TraversalOutcome>",
            "LocusMetricComponentState2D", "EstablishedMetricErrorAmount2D",
            "NotEstablishedMetricErrorAmount2D",
            "NotApplicableMetricErrorAmount2D")) {
        $found = @(Get-ChildItem -LiteralPath $MetricSourceRoot -Filter "*.java" |
            Select-String -SimpleMatch $requiredContract)
        Assert-Condition -Condition ($found.Count -gt 0) `
            -Message "Productive G7B contract is missing: $requiredContract"
    }

    $changedPaths = @(& git -C $RepositoryRoot diff --name-only $EntrySha --)
    $changedPaths += @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    foreach ($path in $changedPaths) {
        foreach ($forbiddenPath in @("CommandDispatcher", "/commands/",
                "MyXML", "/io/", "geogebra3D", "/kernel3D/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G7B public/persistence/3D edit: $path"
        }
    }

    $evidence = Get-Content -LiteralPath $EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($evidence.status -eq `
            "READY_FOR_AUTHOR_REVIEW") `
        -Message "G7B evidence disposition is not ready for author review."
    Assert-Condition -Condition ($evidence.provenance.entrySha -eq $EntrySha -and
            $evidence.provenance.executedPromptSha256 -eq $ExecutedPromptSha) `
        -Message "G7B evidence provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.tests.productive.total -eq 62 `
            -and [int]$evidence.tests.productive.failures -eq 0) `
        -Message "G7B evidence must record 62 passing productive probes."
    Assert-Condition -Condition ([int]$evidence.functionalGates.compatibleConsumers100.componentStateBuilds -eq 1 `
            -and [int]$evidence.functionalGates.compatibleConsumers100.crossResultHits -eq 99 `
            -and [int]$evidence.functionalGates.compatibleConsumers100.duplicateBuilds -eq 0) `
        -Message "G7B N=100 shared-owner gate is not recorded."
    foreach ($zeroGate in @("renderReads", "legacySampleReads",
            "wholeLocusRegenerations", "indexBuildsInsideDownstreamPoint",
            "failedEntriesPublished", "oldRevisionsRetained",
            "crossConstructionSharing", "publicCommands", "xmlRegistrations",
            "publicPathAdditions", "metric3dBehavior", "g8Behavior")) {
        Assert-Condition -Condition ([int]$evidence.functionalGates.$zeroGate `
                -eq 0) -Message "G7B functional gate is nonzero: $zeroGate"
    }
    Assert-EvidenceHashes

    $documents = @(
        "docs\roadmap\geocedg_roadmap.md",
        "docs\roadmap\g7_locus_v2_metrics_plan.md",
        "docs\architecture\locus_v2_metric_semantic_model.md",
        "docs\architecture\locus_v2_metric_architecture.md",
        "docs\developer\locus_v2_metric_api.md",
        "docs\validation\g7_locus_v2_metric_validation_matrix.md",
        "docs\validation\g7_locus_v2_metric_benchmark_plan.md",
        "docs\validation\g7b_locus_v2_metric_kernel_report.md",
        "docs\validation\g7b_locus_v2_metric_traceability_matrix.md",
        "docs\user\geocedg_user_guide.md"
    )
    Assert-MarkdownLinks -Documents $documents
    foreach ($document in $documents) {
        $text = Get-Content -LiteralPath (Join-Path $RepositoryRoot $document) -Raw
        Assert-Condition -Condition ($text.Contains("G7B")) `
            -Message "G7B status is missing from $document"
    }

    if (-not $SkipBuild) {
        Invoke-LoggedGradle -LogName "g7b-common.log" `
            -Description "G7B productive common-kernel tests and checkstyle" `
            -Arguments @(
                ":shared:common-jre:test", "--tests",
                "org.geocedg.common.locus.LocusMetricProductive*",
                ":shared:common:checkstyleMain",
                ":shared:common-jre:checkstyleTest",
                "--no-daemon", "--console=plain"
            )
        Invoke-LoggedGradle -LogName "g7b-laboratory.log" `
            -Description "G7B developer-laboratory contract and checkstyle" `
            -Arguments @(
                ":desktop:desktop:test", "--tests",
                "org.geocedg.desktop.locus.LocusV2LaboratoryContractTest",
                ":desktop:desktop:checkstyleMain",
                "--no-daemon", "--console=plain"
            )
        $testCounts = [ordered]@{
            "org.geocedg.common.locus.LocusMetricProductiveBenchmarkTest" = 7
            "org.geocedg.common.locus.LocusMetricProductiveImproperTest" = 6
            "org.geocedg.common.locus.LocusMetricProductiveLifecycleTest" = 11
            "org.geocedg.common.locus.LocusMetricProductiveNestedTest" = 2
            "org.geocedg.common.locus.LocusMetricProductiveNumericalTest" = 17
            "org.geocedg.common.locus.LocusMetricProductiveRouteTest" = 11
            "org.geocedg.common.locus.LocusMetricProductiveValueTest" = 8
        }
        foreach ($entry in $testCounts.GetEnumerator()) {
            Assert-TestResult -Root $CommonTestResultRoot `
                -ClassName $entry.Key -ExpectedTests $entry.Value
        }
        Assert-TestResult -Root $DesktopTestResultRoot `
            -ClassName "org.geocedg.desktop.locus.LocusV2LaboratoryContractTest" `
            -ExpectedTests 3
        Assert-CheckstyleResult -RelativePath `
            "source\shared\common\build\reports\checkstyle\main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source\shared\common-jre\build\reports\checkstyle\test.xml"
        Assert-CheckstyleResult -RelativePath `
            "source\desktop\desktop\build\reports\checkstyle\main.xml"
    } else {
        Write-Host "Skipping G7B Gradle probes because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G7B focused verification passed (62 productive + 3 laboratory tests)."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G7B build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G7B verification."
            exit 1
        }
    }
}

exit 0

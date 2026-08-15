[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g8c-intersections-design")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "3531db7838426305c505c291b1d614aa6df5175c"
$G8BTag = "geocedg-g8b-pass"
$G8C1PromptSha = "c096a069b10b85a27e8ac96223ca679d788441f2a684403991e66a1ffbcfabaa"
$G8C2PromptSha = "b79e5f37d9ed8393dfb81f6d05a09c456a9487a10852f85efc09e07ddc5951b0"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$CheckstyleResult = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\reports\checkstyle\test.xml"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8c"
$EvidencePath = Join-Path $EvidenceRoot `
    "g8c-design-characterization-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8c-design-evidence.sha256"
$ReferenceGenerator = Join-Path $EvidenceRoot `
    "generate_extended_intersection_references.py"
$G8C1EvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c1\g8c1-intersection-kernel-evidence.json"
$InitialStatus = $null
$GeneratedSnapshot = $null

$Documents = @(
    "docs\roadmap\geocedg_roadmap.md",
    "docs\roadmap\g8_locus_v2_intersections_plan.md",
    "docs\roadmap\g8c_locus_v2_extended_intersections_design.md",
    "geocedg\specs\locus\locus-v2-intersections.md",
    "geocedg\specs\locus\locus-v2-extended-intersections.md",
    "docs\adr\0008-locus-v2-intersection-result-and-continuation.md",
    "docs\adr\0009-locus-v2-locus-intersection-pair-semantics.md",
    "docs\architecture\locus_v2_extended_intersection_semantic_model.md",
    "docs\architecture\locus_v2_extended_intersection_architecture.md",
    "docs\architecture\locus_v2_extended_intersection_upstream_impact.md",
    "docs\architecture\locus_v2_extended_intersection_capability_matrix.md",
    "docs\developer\locus_v2_extended_intersection_api.md",
    "docs\validation\g8c_locus_v2_extended_intersection_characterization_report.md",
    "docs\validation\g8c_locus_v2_extended_intersection_validation_matrix.md",
    "docs\validation\g8c_locus_v2_extended_intersection_benchmark_plan.md",
    "docs\validation\g8c_locus_v2_extended_intersection_scientific_traceability.md",
    "docs\user\geocedg_user_guide.md",
    ".github\prompts\tasks\g8c1-locus-v2-extended-target-intersections.prompt.md",
    ".github\prompts\tasks\g8c2-locus-v2-locus-intersections.prompt.md"
)

$TestClasses = [ordered]@{
    "org.geocedg.common.locus.G8CExtendedTargetCharacterizationTest" = 13
    "org.geocedg.common.locus.G8CLocusLocusCharacterizationTest" = 13
    "org.geocedg.common.locus.G8CDesignFunctionalBenchmarkTest" = 6
}

$AllowedTestSources = @(
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CCharacterizationSupport.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CExtendedTargetCharacterizationTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CLocusLocusCharacterizationTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8CDesignFunctionalBenchmarkTest.java"
)

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
        -Message "Required G8C design artifact is missing: $RelativePath"
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
            -Message "Malformed G8C evidence hash line: $line"
        $path = Join-Path $RepositoryRoot $parts[1].Trim().Replace("/", "\")
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed G8C artifact is missing: $path"
        Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $path) -eq `
                $parts[0].ToLowerInvariant()) `
            -Message "G8C evidence hash mismatch: $path"
    }
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $Documents) {
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
    Write-Host "G8C Markdown links resolved: $($Documents.Count) documents."
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8C test result: $path"
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
    Assert-Condition -Condition (Test-Path -LiteralPath $CheckstyleResult `
            -PathType Leaf) -Message "Missing G8C test Checkstyle result."
    $errors = @(Select-String -LiteralPath $CheckstyleResult -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G8C test Checkstyle contains $($errors.Count) violations."
}

function Invoke-ReferenceCheck {
    $logPath = Join-Path $LogDirectory "g8c-independent-references.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & conda run -n om_env python $ReferenceGenerator --check 2>&1 |
            Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C independent references failed. See $logPath"
}

function Invoke-G8CTests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test",
        "--tests", "org.geocedg.common.locus.G8CExtendedTargetCharacterizationTest",
        "--tests", "org.geocedg.common.locus.G8CLocusLocusCharacterizationTest",
        "--tests", "org.geocedg.common.locus.G8CDesignFunctionalBenchmarkTest",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain"
    )
    $logPath = Join-Path $LogDirectory "g8c-design-tests.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C design Gradle gate failed. See $logPath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g8c-design"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    $requiredFiles = @(
        "geocedg\validation\locus-v2\g8c\g8c-design-characterization-evidence.json",
        "geocedg\validation\locus-v2\g8c\extended-intersection-reference-values.json",
        "geocedg\validation\locus-v2\g8c\generate_extended_intersection_references.py",
        "geocedg\validation\locus-v2\g8c\g8c-design-evidence.sha256",
        "tools\agent\verify-g8c-intersections-design.ps1"
    ) + $Documents + $AllowedTestSources
    foreach ($file in $requiredFiles) {
        Assert-RequiredFile -RelativePath $file
    }

    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8B completion SHA."
    $tagTarget = (& git -C $RepositoryRoot rev-parse "$G8BTag^{}" ).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagTarget -eq $EntrySha) `
        -Message "$G8BTag does not identify the G8C design baseline."

    $promptHashes = [ordered]@{
        ".github\prompts\tasks\g8c1-locus-v2-extended-target-intersections.prompt.md" = $G8C1PromptSha
        ".github\prompts\tasks\g8c2-locus-v2-locus-intersections.prompt.md" = $G8C2PromptSha
    }
    foreach ($prompt in $promptHashes.GetEnumerator()) {
        $actual = Get-CanonicalTextSha256 -Path `
            (Join-Path $RepositoryRoot $prompt.Key)
        Assert-Condition -Condition ($actual -eq $prompt.Value) `
            -Message "Future prompt hash changed: $($prompt.Key)"
        Assert-Contains -RelativePath $prompt.Key -Text @(
            "# Objective", "# Mandatory entry gate",
            "# Authority and evidence hierarchy", "# Explicitly forbidden scope",
            "# Architectural placement", "# Required design/specification",
            "# Geometric invariants and degeneracies",
            "# Compatibility and serialization",
            "# Required tests and commands", "# Required artifacts",
            "# Verification", "# Stop conditions", "do not execute")
    }

    Assert-Contains -RelativePath `
        "geocedg\specs\locus\locus-v2-extended-intersections.md" -Text @(
            "G8C1 NORMATIVE — AUTHOR APPROVED", "G8C2 PROPOSED — NOT NORMATIVE",
            "Option B", "Source symmetry",
            "Locus V2 × Locus V2")
    Assert-Contains -RelativePath `
        "docs\adr\0009-locus-v2-locus-intersection-pair-semantics.md" -Text @(
            "Status: **Proposed**", "canonical unordered pair")
    $g8c1CandidatePresent = Test-Path -LiteralPath $G8C1EvidencePath `
        -PathType Leaf
    $g8c1RoadmapState = if ($g8c1CandidatePresent) {
        "G8C1 = PASS — AUTHOR APPROVED"
    } else {
        "G8C1 = AUTHORIZED — NOT STARTED"
    }
    Assert-Contains -RelativePath `
        "docs\roadmap\geocedg_roadmap.md" -Text @(
            "G8C DESIGN = PASS — AUTHOR APPROVED",
            $g8c1RoadmapState,
            "G8C2 = NOT AUTHORIZED — NOT STARTED",
            "G9 = NOT STARTED")

    $changedPaths = @(& git -C $RepositoryRoot diff --name-only $EntrySha --)
    $changedPaths += @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    $changedPaths = @($changedPaths | Sort-Object -Unique)
    foreach ($path in $changedPaths) {
        if ($path -match '^source/.+/src/main/') {
            $allowedG8C1Productive = $g8c1CandidatePresent -and (
                $path -eq "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java" -or
                $path -match '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$')
            Assert-Condition -Condition $allowedG8C1Productive `
                -Message "Unapproved productive G8C source edit: $path"
        }
        if ($path -match '^source/.+/src/test/.+\.java$') {
            $allowedG8C1Test = $g8c1CandidatePresent -and (
                $path -eq "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8BIntersectionKernelTest.java" -or
                $path -match '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1[^/]+\.java$')
            Assert-Condition -Condition ($path -in $AllowedTestSources -or
                    $allowedG8C1Test) `
                -Message "Unapproved G8C characterization source: $path"
        }
        foreach ($forbiddenPath in @(
                "/commands/", "CmdIntersect", "AlgoDispatcher", "GeoLocus.java",
                "/Path.java", "GeoFactory", "/io/", "geogebra3D", "kernel3D",
                "/export/", "python/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G8C design scope edit: $path"
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
            "PASS_AUTHOR_APPROVED" -and
            $evidence.provenance.entrySha -eq $EntrySha) `
        -Message "G8C design evidence status/provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.tests.total.tests -eq 32 -and
            [int]$evidence.tests.total.failures -eq 0 -and
            [int]$evidence.tests.total.errors -eq 0 -and
            [int]$evidence.tests.total.skipped -eq 0) `
        -Message "G8C evidence must record 32 clean probes."
    Assert-Condition -Condition (-not [bool]$evidence.recommendation.singleUnifiedImplementation `
            -and $evidence.recommendation.phases.Count -eq 2 -and
            [bool]$evidence.recommendation.phases[0].authorized -and
            -not [bool]$evidence.recommendation.phases[1].authorized) `
        -Message "G8C phase recommendation or authorization is inconsistent."
    Assert-Condition -Condition (-not [bool]$evidence.authorReview.required -and
            [bool]$evidence.authorReview.designPassClaimed -and
            [bool]$evidence.authorReview.subdivisionApproved -and
            [bool]$evidence.authorReview.g8c1Authorized -and
            -not [bool]$evidence.authorReview.g8c2Authorized -and
            -not [bool]$evidence.authorReview.adr0009Accepted) `
        -Message "G8C author-closeout evidence is inconsistent."
    foreach ($zeroGate in @(
            "queryLocalRetainedEntries", "renderReads", "legacySampleReads",
            "viewportReads", "metricIndexReads", "wholeLocusRegenerations")) {
        Assert-Condition -Condition ([int]$evidence.functionalEvidence.$zeroGate `
                -eq 0) -Message "G8C forbidden/retained gate is nonzero: $zeroGate"
    }
    Assert-Condition -Condition ([int]$evidence.scopeAudit.productiveG8cSourceFiles `
            -eq 0 -and -not [bool]$evidence.scopeAudit.publicApiOrCommandChanges `
            -and -not [bool]$evidence.scopeAudit.pathChanges -and
            -not [bool]$evidence.scopeAudit.xmlPersistenceMigrationChanges -and
            -not [bool]$evidence.scopeAudit.threeDimensionalOrG9Changes -and
            -not [bool]$evidence.scopeAudit.sharedOrGlobalIntersectionState) `
        -Message "G8C scope evidence claims forbidden implementation."

    Assert-EvidenceHashes
    Assert-MarkdownLinks
    Invoke-ReferenceCheck

    if (-not $SkipBuild) {
        Invoke-G8CTests
        foreach ($entry in $TestClasses.GetEnumerator()) {
            Assert-TestResult -ClassName $entry.Key -ExpectedTests $entry.Value
        }
        Assert-CheckstyleResult
    } else {
        Write-Host "Skipping G8C Gradle probes because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G8C design author-closeout verification passed (32 characterization probes)."
    if ($g8c1CandidatePresent) {
        Write-Host "G8C1 is author-approved; G8C2 and G9 are not started."
    } else {
        Write-Host "G8C1 is authorized/not started; G8C2 and G9 are not started."
    }
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G8C design build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G8C verification."
            exit 1
        }
    }
}

exit 0

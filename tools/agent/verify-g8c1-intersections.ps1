[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g8c1-intersections")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "16c7c795c1e95881aeb497b8980480ef68ab5f7a"
$DesignTag = "geocedg-g8c-design-pass"
$PromptSha = "c096a069b10b85a27e8ac96223ca679d788441f2a684403991e66a1ffbcfabaa"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8c1"
$EvidencePath = Join-Path $EvidenceRoot "g8c1-intersection-kernel-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8c1-evidence.sha256"
$G8C2ContractEvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c2\g8c2-contract-review-evidence.json"
$G8C2ImplementationEvidencePath = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c2\g8c2-intersection-kernel-evidence.json"
$ReferenceGenerator = Join-Path $RepositoryRoot `
    "geocedg\validation\locus-v2\g8c\generate_extended_intersection_references.py"
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$InitialStatus = $null
$GeneratedSnapshot = $null

$TestClasses = [ordered]@{
    "org.geocedg.common.locus.G8C1ExtendedTargetKernelTest" = 22
    "org.geocedg.common.locus.G8C1ExtendedTargetLifecycleTest" = 10
    "org.geocedg.common.locus.G8C1ExtendedTargetFunctionalBenchmarkTest" = 6
}

$ProductiveFiles = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/BoundedFunctionGraphIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ExtendedTargetIntersectionCapability2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionCapabilityContext2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSemanticMetadata2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionTargetDomain2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionTargetSupport2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionInstrumentation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionInstrumentationSnapshot2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionPolicy2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionSolver2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTargets2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/NondegenerateConicIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RegularPolynomialImplicitIntersectionTarget2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/TargetCandidateEvaluation2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/TargetResidualEvaluation2D.java"
)

$TestFiles = @(
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8BIntersectionKernelTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1IntersectionTestSupport.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetKernelTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetLifecycleTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C1ExtendedTargetFunctionalBenchmarkTest.java"
)

$Documents = @(
    "docs/roadmap/geocedg_roadmap.md",
    "docs/roadmap/g8_locus_v2_intersections_plan.md",
    "docs/roadmap/g8c_locus_v2_extended_intersections_design.md",
    "geocedg/specs/locus/locus-v2-extended-intersections.md",
    "docs/architecture/locus_v2_extended_intersection_semantic_model.md",
    "docs/architecture/locus_v2_extended_intersection_architecture.md",
    "docs/architecture/locus_v2_extended_intersection_upstream_impact.md",
    "docs/architecture/locus_v2_extended_intersection_capability_matrix.md",
    "docs/developer/locus_v2_extended_intersection_api.md",
    "docs/validation/g8c_locus_v2_extended_intersection_validation_matrix.md",
    "docs/validation/g8c_locus_v2_extended_intersection_benchmark_plan.md",
    "docs/validation/g8c_locus_v2_extended_intersection_scientific_traceability.md",
    "docs/validation/g8c1_locus_v2_extended_target_intersection_kernel_report.md",
    "docs/validation/g8c1_locus_v2_extended_target_intersection_traceability_matrix.md",
    "docs/user/geocedg_user_guide.md"
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

    Assert-Condition -Condition (Test-Path -LiteralPath `
            (Join-Path $RepositoryRoot $RelativePath) -PathType Leaf) `
        -Message "Required G8C1 artifact is missing: $RelativePath"
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
            -Message "Malformed G8C1 evidence hash line: $line"
        $path = Join-Path $RepositoryRoot $parts[1].Trim().Replace("/", "\")
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Hashed G8C1 artifact is missing: $path"
        Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $path) -eq `
                $parts[0].ToLowerInvariant()) `
            -Message "G8C1 evidence hash mismatch: $path"
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
    Write-Host "G8C1 Markdown links resolved: $($Documents.Count) documents."
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8C1 test result: $path"
    [xml]$result = Get-Content -LiteralPath $path -Raw
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "G8C1 test result is not clean: $ClassName"
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G8C1 Checkstyle result: $RelativePath"
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G8C1 Checkstyle contains $($errors.Count) violations."
}

function Invoke-ReferenceCheck {
    $logPath = Join-Path $LogDirectory "g8c1-independent-references.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & conda run -n om_env python $ReferenceGenerator --check 2>&1 |
            Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C1 independent references failed. See $logPath"
}

function Invoke-G8C1Tests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test", "--tests",
        "org.geocedg.common.locus.G8C1*",
        ":shared:common:checkstyleMain",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain",
        "--no-problems-report"
    )
    $logPath = Join-Path $LogDirectory "g8c1-intersection-tests.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "G8C1 Gradle gate failed. See $logPath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g8c1"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($file in @(
            ".github/prompts/tasks/g8c1-locus-v2-extended-target-intersections.prompt.md",
            "docs/adr/0008-locus-v2-intersection-result-and-continuation.md",
            "docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md",
            "geocedg/validation/locus-v2/g8c1/g8c1-intersection-kernel-evidence.json",
            "geocedg/validation/locus-v2/g8c1/g8c1-evidence.sha256",
            "geocedg/validation/locus-v2/g8c/extended-intersection-reference-values.json",
            "tools/agent/verify-g8c1-intersections.ps1") +
            $Documents + $ProductiveFiles + $TestFiles) {
        Assert-RequiredFile -RelativePath $file
    }

    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8C design closeout."
    $tagTarget = (& git -C $RepositoryRoot rev-parse "$DesignTag^{}" ).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagTarget -eq $EntrySha) `
        -Message "$DesignTag does not identify the G8C1 entry baseline."
    $promptPath = Join-Path $RepositoryRoot `
        ".github\prompts\tasks\g8c1-locus-v2-extended-target-intersections.prompt.md"
    Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $promptPath) -eq `
            $PromptSha) -Message "Canonical G8C1 prompt hash changed."

    $g8c2ContractApproved = Test-Path -LiteralPath $G8C2ContractEvidencePath `
        -PathType Leaf
    $g8c2ImplementationPresent = Test-Path -LiteralPath `
        $G8C2ImplementationEvidencePath -PathType Leaf
    Assert-Contains -RelativePath `
        "geocedg/specs/locus/locus-v2-extended-intersections.md" -Text @(
            $(if ($g8c2ContractApproved) {
                    "G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED"
                } else {
                    "G8C1 NORMATIVE — AUTHOR APPROVED"
                }),
            "G8C1 is", "PASS — AUTHOR", "G8C2",
            "first-order normal geometric residual", "rho_vertical",
            "Option B")
    Assert-Contains -RelativePath "docs/roadmap/geocedg_roadmap.md" -Text @(
        "G8C DESIGN = PASS — AUTHOR APPROVED",
        "G8C1 = PASS — AUTHOR APPROVED",
        $(if ($g8c2ImplementationPresent) {
                "G8C2 = PASS — AUTHOR APPROVED"
            } elseif ($g8c2ContractApproved) {
                "G8C2 = AUTHORIZED — NOT STARTED"
            } else {
                "G8C2 = NOT AUTHORIZED — NOT STARTED"
            }),
        $(if ($g8c2ImplementationPresent) {
                "G9 DESIGN = AUTHORIZED — NOT STARTED"
            } else {
                "G9 = NOT STARTED"
            }))
    Assert-Contains -RelativePath `
        "docs/validation/g8c1_locus_v2_extended_target_intersection_kernel_report.md" `
        -Text @("PASS — AUTHOR APPROVED", "38 tests", "NOT_ESTABLISHED", "414")

    $changedPaths = @(& git -C $RepositoryRoot diff --name-only $EntrySha --)
    $changedPaths += @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    $changedPaths = @($changedPaths | Sort-Object -Unique)
    foreach ($path in $changedPaths) {
        if ($path -match '^source/.+/src/main/') {
            $allowedG8C2Productive = $g8c2ImplementationPresent -and (
                $path -eq "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java" -or
                $path -match '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$')
            Assert-Condition -Condition ($path -in $ProductiveFiles -or
                    $allowedG8C2Productive) `
                -Message "Unapproved productive G8C1 source edit: $path"
        }
        if ($path -match '^source/.+/src/test/.+\.java$') {
            Assert-Condition -Condition ($path -in $TestFiles -or $path -match `
                    '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C(CharacterizationSupport|ExtendedTargetCharacterizationTest|LocusLocusCharacterizationTest|DesignFunctionalBenchmarkTest)\.java$' -or
                    ($g8c2ImplementationPresent -and $path -match `
                    '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2[^/]+\.java$')) `
                -Message "Unapproved G8C1 test source edit: $path"
        }
        foreach ($forbiddenPath in @(
                "/commands/", "CmdIntersect", "AlgoDispatcher", "GeoLocus.java",
                "/Path.java", "GeoFactory", "/io/", "geogebra3D", "kernel3D",
                "/export/", "python/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G8C1 scope edit: $path"
        }
    }

    $manifest = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
        "docs\upstream\modified-files.yml") -Raw | ConvertFrom-Json -Depth 100
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
            -Message "Forbidden G8C1 authority dependency: $forbiddenAuthority"
    }
    foreach ($requiredContract in @(
            "FIRST_ORDER_NORMAL_LENGTH", "VERTICAL_MODEL_LENGTH",
            "DOMAIN_NOT_EXPLICIT", "REGULAR_POLYNOMIAL_IMPLICIT",
            "ExtendedTargetIntersectionCapability2D",
            "Completeness.NOT_ESTABLISHED", "LocalIsolationStatus.ESTABLISHED")) {
        Assert-Condition -Condition ($sourceText.Contains($requiredContract)) `
            -Message "Missing productive G8C1 contract: $requiredContract"
    }

    $evidence = Get-Content -LiteralPath $EvidencePath -Raw |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($evidence.status -eq `
            "PASS_AUTHOR_APPROVED" -and
            $evidence.provenance.entrySha -eq $EntrySha -and
            $evidence.provenance.canonicalPromptCanonicalTextSha256 -eq `
                $PromptSha) -Message "G8C1 evidence provenance is inconsistent."
    Assert-Condition -Condition ([int]$evidence.tests.total.tests -eq 38 -and
            [int]$evidence.tests.total.failures -eq 0 -and
            [int]$evidence.tests.total.errors -eq 0 -and
            [int]$evidence.tests.total.skipped -eq 0) `
        -Message "G8C1 evidence must record 38 clean tests."
    Assert-Condition -Condition ($evidence.capability.defaultCompleteness -eq `
            "NOT_ESTABLISHED" -and
            -not [bool]$evidence.capability.defaultCanClaimComplete -and
            [bool]$evidence.pointAdmissibility.optionBPreserved -and
            -not [bool]$evidence.pointAdmissibility.parentCompleteRequired) `
        -Message "G8C1 completeness or Option B evidence is inconsistent."
    Assert-Condition -Condition ([int]$evidence.representativeCounterSnapshot.semanticEvaluations `
            -eq 414 -and
            [int]$evidence.representativeCounterSnapshot.isolationSubdivisions `
            -eq 256 -and
            [int]$evidence.representativeCounterSnapshot.refinementIterations `
            -eq 95 -and
            [int]$evidence.representativeCounterSnapshot.retainedIntersectionIndexEntries `
            -eq 0) -Message "G8C1 counter baseline is inconsistent."
    foreach ($zeroScope in @(
            "publicCommands", "dispatcherOverloads", "publicPathAdditions",
            "xmlOrPersistenceRegistrations", "legacyGeoLocusChanges",
            "classicIntersectionChanges", "frontendChanges",
            "threeDimensionalChanges", "g8c2Changes", "g9Changes",
            "sharedOrGlobalIntersectionCache")) {
        Assert-Condition -Condition ([int]$evidence.scopeAudit.$zeroScope -eq 0) `
            -Message "G8C1 forbidden scope gate is nonzero: $zeroScope"
    }
    Assert-Condition -Condition (-not [bool]$evidence.authorReview.required -and
            [bool]$evidence.authorReview.implementationPassClaimed -and
            $evidence.authorReview.disposition -eq "PASS_AUTHOR_APPROVED" -and
            @($evidence.authorReview.decisions.PSObject.Properties.Value |
                    Where-Object { $_ -ne "APPROVED" }).Count -eq 0 -and
            $evidence.phaseDisposition.g8c1 -eq `
                "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g8c2 -eq "NOT_AUTHORIZED" -and
            $evidence.phaseDisposition.g9 -eq "NOT_STARTED") `
        -Message "G8C1 author-review disposition is inconsistent."

    Assert-EvidenceHashes
    Assert-MarkdownLinks
    Invoke-ReferenceCheck

    if (-not $SkipBuild) {
        Invoke-G8C1Tests
        foreach ($entry in $TestClasses.GetEnumerator()) {
            Assert-TestResult -ClassName $entry.Key -ExpectedTests $entry.Value
        }
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
    } else {
        Write-Host "Skipping G8C1 Gradle tests because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    Write-Host "G8C1 verification passed (38 focused tests)."
    Write-Host "G8C1 is PASS - AUTHOR APPROVED and remains internal."
    if ($g8c2ImplementationPresent) {
        Write-Host "G8C2/global G8 are author-approved; G9 design is authorized/not started."
    } elseif ($g8c2ContractApproved) {
        Write-Host "G8C2 is authorized/not started; G9 remains not started."
    } else {
        Write-Host "G8C2 and G9 remain not authorized/not started."
    }
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G8C1 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G8C1 verification."
            exit 1
        }
    }
}

exit 0

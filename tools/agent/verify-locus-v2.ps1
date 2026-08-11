[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-locus-v2")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$LegacyResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LegacyLocusCharacterizationTest.xml")
$SemanticResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2SemanticCharacterizationTest.xml")
$ScientificResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LegacyCeDGScientificModelCharacterizationTest.xml")
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

function Assert-Condition {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Join-Path $RepositoryRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required G6A file is missing: $RelativePath"
    }
    try {
        return Get-Content -Raw -LiteralPath $path |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON-compatible YAML: " +
            $_.Exception.Message
    }
}

function Invoke-LoggedGradle {
    param([Parameter(Mandatory)] [string[]]$Arguments)

    $logPath = Join-Path $LogDirectory "g6a-locus-v2-gradle.log"
    Write-Host "`n==> G6A characterization tests and checkstyle"
    Write-Host "    log: $logPath"
    $exitCode = -1
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "G6A Gradle validation failed with exit code $exitCode. See $logPath"
    }
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description did not produce $Path"
    }
    [xml]$result = Get-Content -Raw -LiteralPath $Path
    $suite = $result.testsuite
    if ([int]$suite.tests -ne $ExpectedTests -or [int]$suite.failures -ne 0 -or
            [int]$suite.errors -ne 0 -or [int]$suite.skipped -ne 0) {
        throw "$Description is not clean: tests=$($suite.tests), " +
            "failures=$($suite.failures), errors=$($suite.errors), " +
            "skipped=$($suite.skipped)."
    }
    Write-Host "${Description}: $($suite.tests) tests, 0 failures."
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null

try {
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedState = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot -DirectoryNames $GeneratedDirectoryNames `
            -Label "verify-locus-v2"
    }

    $requiredFiles = @(
        "docs/adr/0006-parallel-locus-v2-semantic-entity.md",
        "docs/architecture/locus_v2_semantic_model.md",
        "docs/architecture/locus_v2_upstream_impact.md",
        "docs/roadmap/g6_locus_v2_plan.md",
        "docs/validation/g6_locus_v2_validation_matrix.md",
        "docs/validation/g6_locus_v2_benchmark_plan.md",
        "docs/validation/g6a_locus_v2_characterization_report.md",
        "geocedg/specs/locus/locus-v2-semantics.md",
        "geocedg/validation/locus-v2/topology-fixture.yml",
        "geocedg/validation/locus-v2/tolerance-policy.yml",
        "geocedg/validation/locus-v2/scientific-pilots.yml",
        "geocedg/validation/locus-v2/g6a-characterization-baseline.yml",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyLocusCharacterizationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2SemanticCharacterizationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyCeDGScientificModelCharacterizationTest.java",
        "models/legacy/inter-cil-cono-oblique/manifest.yml",
        "models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb",
        "models/legacy/inter-cil-cono-oblique-two-levels/manifest.yml",
        "models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb"
    )
    foreach ($required in $requiredFiles) {
        Assert-Condition -Condition (Test-Path -LiteralPath (
                Join-Path $RepositoryRoot $required) -PathType Leaf) `
            -Message "Required G6A file is missing: $required"
    }

    $adr = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\adr\0006-parallel-locus-v2-semantic-entity.md")
    Assert-Condition -Condition $adr.Contains("- Status: **Accepted**") `
        -Message "ADR 0006 must be Accepted at G6A closeout."
    Assert-Condition -Condition $adr.Contains(
        "ACCEPTED AT G6A CLOSEOUT") `
        -Message "ADR 0006 is missing the second author-review disposition."

    $specification = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "geocedg\specs\locus\locus-v2-semantics.md")
    Assert-Condition -Condition $specification.Contains(
        "APPROVED AS NORMATIVE G6 SEMANTIC CONTRACT") `
        -Message "The G6 semantic contract is not marked normative."
    foreach ($requiredContract in @(
            "validDomainComponents[]",
            "POINTWISE_DETERMINISTIC",
            "CANONICAL_CONTINUATION_DETERMINISTIC",
            "UNSUPPORTED_NONDETERMINISM",
            "FLOATING_POINT_UNCERTIFIED",
            "recursive semantic evaluator composition",
            "scoped shared",
            "evaluation session as its minimum",
            'distinct appended V2 `GeoClass`/classification',
            'neither `isGeoLocus()` nor `isGeoLocusable()`',
            "Derived semantic services consumed by downstream constructions must preserve",
            "no public command")) {
        Assert-Condition -Condition $specification.Contains($requiredContract) `
            -Message "The G6A semantic contract is missing '$requiredContract'."
    }

    $topology = Read-JsonDocument `
        "geocedg/validation/locus-v2/topology-fixture.yml"
    Assert-Condition -Condition ($topology.schema_version -eq 1 -and
            $topology.status -eq "author-approved-g6a-reference" -and
            $topology.branch_family.a_zero[0].branch_key -eq "root" -and
            @($topology.component_path).Count -eq 4 -and
            @($topology.lineage_path).Count -eq 4) `
        -Message "The formal G6A topology fixture is invalid."

    $tolerances = Read-JsonDocument `
        "geocedg/validation/locus-v2/tolerance-policy.yml"
    Assert-Condition -Condition ($tolerances.schema_version -eq 1 -and
            $tolerances.status -eq "author-approved-g6b-validation-envelope" -and
            $tolerances.numeric_guarantee -eq "FLOATING_POINT_UNCERTIFIED" -and
            $tolerances.evaluation.formula -eq
                "max(1e-12 * max(1,S), 64 * ulp(max(1,S)))" -and
            @($tolerances.evaluation.scale_policy.forbidden) -contains
                "absolute distance from origin" -and
            $tolerances.render.forbidden_as.Count -eq 4) `
        -Message "The G6A tolerance policy is invalid."

    $scientificPilots = Read-JsonDocument `
        "geocedg/validation/locus-v2/scientific-pilots.yml"
    Assert-Condition -Condition (
            $scientificPilots.schema_version -eq 1 -and
            $scientificPilots.status -eq "g6a-evidence-author-approved" -and
            @($scientificPilots.local_executable_models).Count -eq 2 -and
            $scientificPilots.local_executable_models[0].role -eq
                "three-level pathological legacy reference" -and
            $scientificPilots.local_executable_models[1].role -eq
                "functional two-level legacy control" -and
            $scientificPilots.g6b_nested_fixture_policy.kind -eq
                "small internal typed three-level semantic reproduction" -and
            $scientificPilots.local_executable_models[0].sha256 -eq
                "b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27" -and
            $scientificPilots.local_executable_models[1].sha256 -eq
                "587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6") `
        -Message "The G6A scientific pilot pair is invalid."

    $baseline = Read-JsonDocument `
        "geocedg/validation/locus-v2/g6a-characterization-baseline.yml"
    Assert-Condition -Condition ($baseline.schema_version -eq 1 -and
            $baseline.status -eq "g6a-pass-author-approved" -and
            $baseline.baseline.upstream_sha -eq
                "9b93256b7df401ff056c37b502d82df4d72b1522" -and
            $baseline.tests.legacy -eq 6 -and
            $baseline.tests.semantic_fixture -eq 5 -and
            $baseline.tests.scientific_model -eq 4 -and
            $baseline.interpretation.legacy_sampling_is_view_dependent -and
            $baseline.interpretation.author_reported_severe_nested_degradation_reproduced -and
            $baseline.interpretation.causal_scope -like
                "observed and instrumented*") `
        -Message "The G6A characterization baseline is invalid."

    $report = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g6a_locus_v2_characterization_report.md")
    foreach ($requiredDisposition in @(
            "G6A = PASS — AUTHOR APPROVED",
            "ADR 0006 = ACCEPTED",
            "G6B = NOT STARTED")) {
        Assert-Condition -Condition $report.Contains($requiredDisposition) `
            -Message "The G6A report is missing '$requiredDisposition'."
    }

    $hashTargets = @{
        "LegacyLocusCharacterizationTest.java" = "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyLocusCharacterizationTest.java"
        "LocusV2SemanticCharacterizationTest.java" = "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2SemanticCharacterizationTest.java"
        "LegacyCeDGScientificModelCharacterizationTest.java" = "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyCeDGScientificModelCharacterizationTest.java"
        "Templatev7.ggb" = "models/legacy/template-v7/original/Templatev7.ggb"
        "InterCilConoOblique.ggb" = "models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb"
        "InterCilConoObliqueTwoLevels.ggb" = "models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb"
    }
    foreach ($hashName in $hashTargets.Keys) {
        $actualHash = (Get-FileHash -LiteralPath (Join-Path $RepositoryRoot `
                $hashTargets[$hashName]) -Algorithm SHA256).Hash.ToLowerInvariant()
        $expectedHash = [string]$baseline.source_hashes.$hashName
        Assert-Condition -Condition ($actualHash -eq $expectedHash) `
            -Message "G6A evidence hash mismatch for $hashName."
    }

    $productionRoot = Join-Path $RepositoryRoot `
        "source\shared\common\src\main\java"
    $productiveV2 = @(Get-ChildItem -LiteralPath $productionRoot -Recurse -File |
        Where-Object { $_.Name -match "(Geo|Algo|Draw)LocusV2" })
    Assert-Condition -Condition ($productiveV2.Count -eq 0) `
        -Message "G6A must not contain productive Locus V2 implementation classes."

    if (-not $SkipBuild) {
        $arguments = @(
            ":shared:common-jre:test", "--tests",
            "org.geocedg.common.locus.LegacyLocusCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2SemanticCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.LegacyCeDGScientificModelCharacterizationTest",
            ":shared:common-jre:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--max-workers=1", "--no-problems-report", "--console=plain"
        )
        if (-not $AllowToolchainDownload) {
            $arguments += "-Dorg.gradle.java.installations.auto-download=false"
        }
        Invoke-LoggedGradle -Arguments $arguments
        Assert-TestResult -Path $LegacyResult `
            -Description "G6A legacy characterization" -ExpectedTests 6
        Assert-TestResult -Path $SemanticResult `
            -Description "G6A semantic fixtures" -ExpectedTests 5
        Assert-TestResult -Path $ScientificResult `
            -Description "G6A scientific legacy models" -ExpectedTests 4
    } else {
        Write-Host "Skipping G6A Gradle validation because -SkipBuild was supplied."
    }

    Write-Host "G6A Locus V2 characterization verification passed."
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs -Description "G6A output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
            if ($finalStatus -ne $InitialStatus) {
                throw "Repository status changed during G6A verification.`n" +
                    "Before:`n$InitialStatus`nAfter:`n$finalStatus"
            }
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup/status failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    Write-Error $Failure.Message
    exit 1
}

exit 0

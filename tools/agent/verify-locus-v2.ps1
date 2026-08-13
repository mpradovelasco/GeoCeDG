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
$ValueContractsResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2ValueContractsTest.xml")
$KernelIntegrationResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2KernelIntegrationTest.xml")
$RenderResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2RenderSeparationTest.xml")
$FunctionalBenchmarkResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2FunctionalBenchmarkTest.xml")
$DrawablesResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geogebra.common.euclidian.DrawablesTest.xml")
$HardeningValueContractsResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2HardeningValueContractsTest.xml")
$SessionHardeningResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2SessionHardeningTest.xml")
$LifecycleHardeningResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2LifecycleHardeningTest.xml")
$RenderHardeningResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2RenderHardeningTest.xml")
$HardeningBenchmarkResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.locus.LocusV2HardeningBenchmarkTest.xml")
$LaboratoryResult = Join-Path $RepositoryRoot (
    "source\desktop\desktop\build\test-results\test\" +
    "TEST-org.geocedg.desktop.locus.LocusV2LaboratoryContractTest.xml")
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
        throw "Required G6 file is missing: $RelativePath"
    }
    try {
        return Get-Content -Raw -LiteralPath $path |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON-compatible YAML: " +
            $_.Exception.Message
    }
}

function Get-NormalizedTextSha256 {
    param([Parameter(Mandatory)] [string]$Path)

    $text = [IO.File]::ReadAllText($Path)
    $logicalText = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($logicalText)
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha256.ComputeHash($bytes)).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [string]$LogName = "g6-locus-v2-gradle.log",
        [string]$Description = "G6A characterization and productive G6B/G6R gates"
    )

    $logPath = Join-Path $LogDirectory $LogName
    Write-Host "`n==> $Description"
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
        throw "G6 Locus V2 Gradle validation failed with exit code $exitCode. See $logPath"
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
        "geocedg/validation/locus-v2/g6b-functional-evidence.yml",
        "geocedg/validation/locus-v2/g6r-hardening-evidence.yml",
        "geocedg/features/experimental.yml",
        "docs/architecture/locus_v2_implementation.md",
        "docs/developer/locus_v2_api.md",
        "docs/developer/repository_map.md",
        "docs/validation/g6r_locus_v2_hardening_report.md",
        "docs/validation/g6r_locus_v2_traceability_matrix.md",
        "docs/user/geocedg_user_guide.md",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyLocusCharacterizationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2SemanticCharacterizationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LegacyCeDGScientificModelCharacterizationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2ValueContractsTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2KernelIntegrationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2RenderSeparationTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2FunctionalBenchmarkTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2HardeningValueContractsTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2SessionHardeningTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2LifecycleHardeningTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2RenderHardeningTest.java",
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2HardeningBenchmarkTest.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDefinition2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusEvaluationSession2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSessionDiagnostic2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusInstrumentationSnapshot2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/package-info.java",
        "source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/DrawLocusV2.java",
        "source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderPolicy2D.java",
        "source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/package-info.java",
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2Laboratory.java",
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2LaboratoryController.java",
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2LaboratoryFixtures.java",
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2LaboratoryFrame.java",
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/package-info.java",
        "source/desktop/desktop/src/test/java/org/geocedg/desktop/locus/LocusV2LaboratoryContractTest.java",
        "tools/locus-v2/open-locus-v2-laboratory.ps1",
        "docs/validation/g6b_locus_v2_kernel_report.md",
        "models/legacy/inter-cil-cono-oblique/manifest.yml",
        "models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb",
        "models/legacy/inter-cil-cono-oblique-two-levels/manifest.yml",
        "models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb"
    )
    foreach ($required in $requiredFiles) {
        Assert-Condition -Condition (Test-Path -LiteralPath (
                Join-Path $RepositoryRoot $required) -PathType Leaf) `
            -Message "Required G6 file is missing: $required"
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
            $scientificPilots.g6b_execution.status -eq "g6b-pass" -and
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

    $g6bEvidence = Read-JsonDocument `
        "geocedg/validation/locus-v2/g6b-functional-evidence.yml"
    Assert-Condition -Condition ($g6bEvidence.schema_version -eq 1 -and
            $g6bEvidence.status -eq "g6b-pass" -and
            $g6bEvidence.entry.entry_sha -eq
                "b25153f4cfd563a47f00c3f98b5c67277037121d" -and
            $g6bEvidence.entry.implementation_commit -eq
                "0c4cc40a389477226b2a6cb507c4fa072790a586" -and
            $g6bEvidence.entry.baseline_upstream_sha -eq
                "9b93256b7df401ff056c37b502d82df4d72b1522" -and
            $g6bEvidence.entry.prompt_sha256 -eq
                "394b1fb1677205d6740a10da512a91b4e01b0f998f4eadcaf1c0e04a90b0fd53" -and
            @($g6bEvidence.semantic_contract.providers).Count -eq 2 -and
            -not $g6bEvidence.semantic_contract.public_command -and
            -not $g6bEvidence.semantic_contract.persistence -and
            -not $g6bEvidence.semantic_contract.public_path -and
            @($g6bEvidence.nested_functional_gate.depths).Count -eq 4 -and
            $g6bEvidence.nested_functional_gate.evaluator_calls[2] -eq 192 -and
            $g6bEvidence.nested_functional_gate.dependency_slice_builds -eq 0 -and
            $g6bEvidence.nested_functional_gate.whole_locus_regenerations -eq 0 -and
            $g6bEvidence.session_gate.configured_capacity -eq 256 -and
            $g6bEvidence.session_gate.retained_entries -le
                $g6bEvidence.session_gate.configured_capacity -and
            $g6bEvidence.render_gate.semantic_revision_changed_by_zoom -eq $false -and
            @($g6bEvidence.topology_gate.valid_component_counts).Count -eq 4) `
        -Message "The G6B functional evidence contract is invalid."

    $g6rEvidence = Read-JsonDocument `
        "geocedg/validation/locus-v2/g6r-hardening-evidence.yml"
    Assert-Condition -Condition ($g6rEvidence.schema_version -eq 1 -and
            $g6rEvidence.status -eq "g6r-pass" -and
            $g6rEvidence.entry.entry_sha -eq
                "e78b4e71ebf752de8c3552b466dbee52b400ab94" -and
            $g6rEvidence.entry.implementation_commit -eq
                "0c4cc40a389477226b2a6cb507c4fa072790a586" -and
            $g6rEvidence.entry.hardening_commit -eq "SELF" -and
            $g6rEvidence.entry.baseline_upstream_sha -eq
                "9b93256b7df401ff056c37b502d82df4d72b1522" -and
            -not $g6rEvidence.contract.semantic_contract_changed -and
            -not $g6rEvidence.contract.public_availability -and
            $g6rEvidence.contract.maturity -eq "experimental" -and
            -not $g6rEvidence.contract.enabled_by_default -and
            $g6rEvidence.hardening_tests.value_contracts -eq 5 -and
            $g6rEvidence.hardening_tests.session_and_cycle -eq 6 -and
            $g6rEvidence.hardening_tests.lifecycle -eq 7 -and
            $g6rEvidence.hardening_tests.render -eq 4 -and
            $g6rEvidence.hardening_tests.performance_distributions -eq 4 -and
            $g6rEvidence.hardening_tests.laboratory_contract -eq 3 -and
            $g6rEvidence.functional_gates.outer_queries -eq 128 -and
            @($g6rEvidence.functional_gates.nested_depths).Count -eq 4 -and
            $g6rEvidence.functional_gates.evaluator_calls[0] -eq 128 -and
            $g6rEvidence.functional_gates.evaluator_calls[1] -eq 256 -and
            $g6rEvidence.functional_gates.evaluator_calls[2] -eq 384 -and
            $g6rEvidence.functional_gates.evaluator_calls[3] -eq 640 -and
            $g6rEvidence.functional_gates.session_on_off_equal -and
            $g6rEvidence.functional_gates.dependency_slice_builds_per_point_query -eq 0 -and
            $g6rEvidence.functional_gates.whole_locus_regenerations -eq 0 -and
            $g6rEvidence.functional_gates.upstream_render_dependencies -eq 0 -and
            $g6rEvidence.session_measurement.bounded_retained_entries -le
                $g6rEvidence.session_measurement.bounded_capacity -and
            $g6rEvidence.render_measurement.adaptive_vertices -lt
                $g6rEvidence.render_measurement.uniform_vertices -and
            $g6rEvidence.render_measurement.decision -eq
                "adopt adaptive visual tessellation with uniform reference mode" -and
            -not $g6rEvidence.timing_policy.absolute_time_gate -and
            $g6rEvidence.developer_laboratory.opt_in -and
            $g6rEvidence.developer_laboratory.temporary_preferences -and
            -not $g6rEvidence.developer_laboratory.normal_geocedg_exposure -and
            -not $g6rEvidence.developer_laboratory.classic_exposure -and
            -not $g6rEvidence.developer_laboratory.persistence -and
            $g6rEvidence.developer_laboratory.nested_depths[4] -eq 5 -and
            $g6rEvidence.developer_laboratory.visual_smoke.distinct_window_title -and
            $g6rEvidence.developer_laboratory.visual_smoke.diagnostics_controls_reachable -and
            $g6rEvidence.developer_laboratory.visual_smoke.segment_provider_status -eq "VALID" -and
            $g6rEvidence.developer_laboratory.visual_smoke.nested_depth_five_status -eq "VALID" -and
            $g6rEvidence.developer_laboratory.visual_smoke.normal_geocedg_title -eq "GeoCeDG" -and
            $g6rEvidence.developer_laboratory.visual_smoke.classic_title -eq "GeoGebra Classic 5" -and
            $g6rEvidence.developer_laboratory.visual_smoke.laboratory_absent_from_normal_and_classic -and
            $g6rEvidence.developer_laboratory.visual_smoke.residual_processes_after_close -eq 0 -and
            $g6rEvidence.packaging_validation.technical_status -eq "PASS" -and
            $g6rEvidence.packaging_validation.zip_msi_exe_verified -and
            $g6rEvidence.packaging_validation.sbom_manifest_hashes_verified -and
            $g6rEvidence.packaging_validation.generated_outputs_removed_at_closeout -and
            $g6rEvidence.verification.composed_authority_with_builds -eq "PASS" -and
            $g6rEvidence.verification.composed_authority_static_recheck -eq "PASS" -and
            $g6rEvidence.verification.generated_repository_outputs_after_closeout -eq 0 -and
            $g6rEvidence.verification.residual_java_gradle_geocedg_processes -eq 0 -and
            $g6rEvidence.deferred.G7_metrics -eq "not started" -and
            $g6rEvidence.deferred.G8_intersections -eq "not started" -and
            $g6rEvidence.deferred.G9_spatial_semantics -eq "not started") `
        -Message "The G6R hardening evidence contract is invalid."

    $experimental = Read-JsonDocument "geocedg/features/experimental.yml"
    $locusFeature = @($experimental.features | Where-Object {
        $_.id -eq "cedg.locus.v2"
    })
    Assert-Condition -Condition ($locusFeature.Count -eq 1 -and
            $locusFeature[0].maturity -eq "experimental" -and
            -not $locusFeature[0].enabled_by_default -and
            $locusFeature[0].specification -eq
                "geocedg/specs/locus/locus-v2-semantics.md") `
        -Message "cedg.locus.v2 must remain experimental and disabled by default."

    $report = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g6a_locus_v2_characterization_report.md")
    foreach ($requiredDisposition in @(
            "G6A = PASS — AUTHOR APPROVED",
            "ADR 0006 = ACCEPTED",
            "G6B = NOT STARTED")) {
        Assert-Condition -Condition $report.Contains($requiredDisposition) `
            -Message "The G6A report is missing '$requiredDisposition'."
    }

    $g6bReport = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g6b_locus_v2_kernel_report.md")
    foreach ($requiredDisposition in @(
            "G6B = PASS",
            "G6 = PASS",
            "No G7, G8 or G9 implementation was started")) {
        Assert-Condition -Condition $g6bReport.Contains($requiredDisposition) `
            -Message "The G6B report is missing '$requiredDisposition'."
    }

    $g6rReport = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g6r_locus_v2_hardening_report.md")
    foreach ($requiredDisposition in @(
            "G6R = PASS",
            "G6 REMAINS PASS",
            "LOCUS V2 PUBLIC AVAILABILITY = NOT YET",
            "G7 = NOT STARTED")) {
        Assert-Condition -Condition $g6rReport.Contains($requiredDisposition) `
            -Message "The G6R report is missing '$requiredDisposition'."
    }

    $userGuide = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\user\geocedg_user_guide.md")
    foreach ($requiredGuideValue in @(
            "## Can I use Locus V2 now?",
            ".\tools\locus-v2\open-locus-v2-laboratory.ps1 -ValidateOnly",
            ":desktop:desktop:runLocusV2Laboratory",
            "GeoCeDG - Locus V2 Developer Laboratory",
            "experimental")) {
        Assert-Condition -Condition $userGuide.Contains($requiredGuideValue) `
            -Message "The G6R user guide is missing '$requiredGuideValue'."
    }
    $g7PlanningOnly = $userGuide.Contains('G7 (`PENDING / NOT STARTED`)')
    $g7aCharacterizedOnly = $userGuide.Contains("G7A characterization only") -and
        $userGuide.Contains("no productive metric available") -and
        $userGuide.Contains(
            "| G7B | Minimal native Locus V2 metric kernel | Authorized; not started |")
    $g7bInternalCandidate = $userGuide.Contains(
            "G7B internal productive metric available") -and
        $userGuide.Contains("no public metric available") -and
        $userGuide.Contains('G7B (`READY FOR AUTHOR REVIEW`)')
    Assert-Condition -Condition ($g7PlanningOnly -or $g7aCharacterizedOnly -or
            $g7bInternalCandidate) `
        -Message ("The user guide must report either the historical G7 planning " +
            "state, G7A characterization-only, or the internal G7B review candidate.")

    $traceability = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "docs\validation\g6r_locus_v2_traceability_matrix.md")
    foreach ($requiredTrace in @(
            "Provider-owned semantic parameter", "Branch identity",
            "Nested semantic composition", "Render/semantic separation",
            "No persistence", 'No public `Path`')) {
        Assert-Condition -Condition $traceability.Contains($requiredTrace) `
            -Message "The G6R traceability matrix is missing '$requiredTrace'."
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
        $hashPath = Join-Path $RepositoryRoot $hashTargets[$hashName]
        $actualHash = if ([IO.Path]::GetExtension($hashPath) -eq ".java") {
            Get-NormalizedTextSha256 -Path $hashPath
        } else {
            (Get-FileHash -LiteralPath $hashPath `
                -Algorithm SHA256).Hash.ToLowerInvariant()
        }
        $expectedHash = [string]$baseline.source_hashes.$hashName
        Assert-Condition -Condition ($actualHash -eq $expectedHash) `
            -Message "G6A evidence hash mismatch for $hashName."
    }

    $productionRoot = Join-Path $RepositoryRoot `
        "source\shared\common\src\main\java"
    $productiveV2 = @(Get-ChildItem -LiteralPath $productionRoot -Recurse -File |
        Where-Object { $_.Name -match "LocusV2" })
    Assert-Condition -Condition ($productiveV2.Count -ge 5) `
        -Message "The productive G6B Locus V2 implementation is incomplete."

    $geoClassSource = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geogebra\common\plugin\GeoClass.java")
    Assert-Condition -Condition ($geoClassSource -match
            "(?s)SHAPE_STADIUM\(.+?\),\s*/\*\* Experimental GeoCeDG.+?LOCUS_V2\(.+?\);") `
        -Message "LOCUS_V2 is not appended immediately after the former final GeoClass."

    $geoV2Source = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geocedg\common\kernel\geos\GeoLocusV2.java")
    Assert-Condition -Condition ($geoV2Source.Contains(
            "final class GeoLocusV2 extends GeoElement") -and
            $geoV2Source.Contains("return GeoClass.LOCUS_V2") -and
            $geoV2Source.Contains("return ValueType.VOID") -and
            $geoV2Source.Contains("throw new UnsupportedOperationException") -and
            $geoV2Source.Contains("restoreDefinedStateAfterEquivalentRecompute") -and
            -not $geoV2Source.Contains("myPointList") -and
            -not $geoV2Source.Contains("PathMoverLocus") -and
            -not $geoV2Source.Contains("LocusRenderCache2D")) `
        -Message "GeoLocusV2 violates its parallel semantic-only boundary."

    $nestedSource = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geocedg\common\kernel\algos\AlgoNestedLocusV2.java")
    Assert-Condition -Condition ($nestedSource.Contains(
            "upstreamDefinition.evaluate") -and
            -not $nestedSource.Contains("Render") -and
            -not $nestedSource.Contains("myPointList") -and
            -not $nestedSource.Contains("PathMoverLocus")) `
        -Message "Nested V2 must consume only upstream semantic evaluators."

    $sessionSource = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geocedg\common\kernel\locus\LocusEvaluationSession2D.java")
    Assert-Condition -Condition ($sessionSource.Contains("implements AutoCloseable") -and
            $sessionSource.Contains("new LocusSemanticKey2D") -and
            $sessionSource.Contains("activeStack") -and
            $sessionSource.Contains("finally") -and
            $sessionSource.Contains("Kind.CYCLE_REENTRY") -and
            $sessionSource.Contains("Kind.INCOHERENT_REVISION") -and
            $sessionSource.Contains("void close()") -and
            -not $sessionSource.Contains("static final Map") -and
            -not $sessionSource.Contains("Construction.addAlgorithm")) `
        -Message "The G6R evaluation session boundary is incomplete or global."

    $renderPolicySource = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geocedg\common\euclidian\draw\LocusRenderPolicy2D.java")
    $renderCacheSource = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geocedg\common\euclidian\draw\LocusRenderCache2D.java")
    Assert-Condition -Condition ($renderPolicySource.Contains("UNIFORM_REFERENCE") -and
            $renderPolicySource.Contains("ADAPTIVE_VISUAL") -and
            $renderPolicySource.Contains("DEFAULT_VISUAL_TOLERANCE_PIXELS") -and
            $renderCacheSource.Contains("screenChordError") -and
            $renderCacheSource.Contains("try (LocusEvaluationSession2D session") -and
            -not $renderCacheSource.Contains("myPointList") -and
            -not $renderCacheSource.Contains("LocusMetric") -and
            -not $renderCacheSource.Contains("PathMoverLocus")) `
        -Message "Adaptive rendering does not preserve the semantic/render boundary."

    $euclidianDraw = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
        "org\geogebra\common\euclidian\EuclidianDraw.java")
    Assert-Condition -Condition ($euclidianDraw.Contains("case LOCUS_V2:") -and
            $euclidianDraw.Contains("new DrawLocusV2")) `
        -Message "The dedicated 2D LOCUS_V2 drawable dispatch is missing."

    foreach ($forbiddenReference in @(
            "org\geogebra\common\kernel\commands\CmdLocus.java",
            "org\geogebra\common\kernel\algos\AlgoDispatcher.java",
            "org\geogebra\common\kernel\GeoFactory.java",
            "org\geogebra\common\kernel\commands\CmdLength.java",
            "org\geogebra\common\kernel\commands\CmdFirst.java",
            "org\geogebra\common\kernel\commands\CmdPerimeter.java",
            "org\geogebra\common\kernel\algos\AlgoIntegralODE.java",
            "org\geogebra\common\geogebra3D\euclidian3D\EuclidianView3D.java")) {
        $forbiddenText = Get-Content -Raw -LiteralPath (Join-Path $productionRoot `
            $forbiddenReference)
        Assert-Condition -Condition (-not $forbiddenText.Contains("LOCUS_V2") -and
                -not $forbiddenText.Contains("GeoLocusV2")) `
            -Message "G6B entered a forbidden legacy/public/3D contract: $forbiddenReference"
    }

    $laboratoryRoot = Join-Path $RepositoryRoot `
        "source\desktop\desktop\src\main\java\org\geocedg\desktop\locus"
    $laboratoryText = (Get-ChildItem -LiteralPath $laboratoryRoot -File |
        ForEach-Object { Get-Content -Raw -LiteralPath $_.FullName }) -join "`n"
    Assert-Condition -Condition ($laboratoryText.Contains(
            "LocusV2Factory") -and
            $laboratoryText.Contains("Locus V2 Developer Laboratory") -and
            $laboratoryText.Contains("cannot be saved") -and
            -not $laboratoryText.Contains("CmdLocus") -and
            -not $laboratoryText.Contains("AlgoDispatcher") -and
            -not $laboratoryText.Contains("PathParameter") -and
            -not $laboratoryText.Contains("getXML")) `
        -Message "The developer laboratory crossed a forbidden public/persistence seam."

    $normalLauncher = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "source\desktop\desktop\src\main\java\org\geocedg\desktop\GeoCeDG.java")
    $classicLauncher = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot `
        "source\desktop\desktop\src\main\java\org\geogebra\desktop\GeoGebra3D.java")
    Assert-Condition -Condition (-not $normalLauncher.Contains(
            "LocusV2Laboratory") -and
            -not $classicLauncher.Contains("LocusV2Laboratory")) `
        -Message "The opt-in laboratory leaked into normal GeoCeDG or Classic."

    & (Join-Path $PSHOME "pwsh.exe") -NoProfile -File (Join-Path $RepositoryRoot `
        "tools\locus-v2\open-locus-v2-laboratory.ps1") -ValidateOnly
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The developer laboratory static smoke failed."

    if (-not $SkipBuild) {
        $arguments = @(
            ":shared:common-jre:test", "--tests",
            "org.geocedg.common.locus.LegacyLocusCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2SemanticCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.LegacyCeDGScientificModelCharacterizationTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2ValueContractsTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2KernelIntegrationTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2RenderSeparationTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2FunctionalBenchmarkTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2HardeningValueContractsTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2SessionHardeningTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2LifecycleHardeningTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2RenderHardeningTest",
            "--tests",
            "org.geocedg.common.locus.LocusV2HardeningBenchmarkTest",
            "--tests",
            "org.geogebra.common.euclidian.DrawablesTest",
            ":shared:common:checkstyleMain",
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
        Assert-TestResult -Path $ValueContractsResult `
            -Description "G6B immutable value contracts" -ExpectedTests 5
        Assert-TestResult -Path $KernelIntegrationResult `
            -Description "G6B kernel and nested integration" -ExpectedTests 16
        Assert-TestResult -Path $RenderResult `
            -Description "G6B semantic/render separation" -ExpectedTests 5
        Assert-TestResult -Path $FunctionalBenchmarkResult `
            -Description "G6B functional benchmark" -ExpectedTests 2
        Assert-TestResult -Path $DrawablesResult `
            -Description "GeoClass drawable enumeration" -ExpectedTests 4
        Assert-TestResult -Path $HardeningValueContractsResult `
            -Description "G6R immutable value hardening" -ExpectedTests 5
        Assert-TestResult -Path $SessionHardeningResult `
            -Description "G6R session, eviction and cycle hardening" -ExpectedTests 6
        Assert-TestResult -Path $LifecycleHardeningResult `
            -Description "G6R lifecycle, recovery and removal hardening" -ExpectedTests 7
        Assert-TestResult -Path $RenderHardeningResult `
            -Description "G6R adaptive/uniform render hardening" -ExpectedTests 4
        Assert-TestResult -Path $HardeningBenchmarkResult `
            -Description "G6R measured functional distributions" -ExpectedTests 4

        $desktopArguments = @(
            ":desktop:desktop:test", "--tests",
            "org.geocedg.desktop.locus.LocusV2LaboratoryContractTest",
            ":desktop:desktop:checkstyleMain",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--max-workers=1", "--no-problems-report", "--console=plain"
        )
        if (-not $AllowToolchainDownload) {
            $desktopArguments += "-Dorg.gradle.java.installations.auto-download=false"
        }
        Invoke-LoggedGradle -Arguments $desktopArguments `
            -LogName "g6r-laboratory-gradle.log" `
            -Description "G6R developer laboratory Desktop gate"
        Assert-TestResult -Path $LaboratoryResult `
            -Description "G6R developer laboratory contract" -ExpectedTests 3
    } else {
        Write-Host "Skipping G6 Gradle validation because -SkipBuild was supplied."
    }

    Write-Host "G6A characterization and productive G6B/G6R verification passed."
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs -Description "G6 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
            if ($finalStatus -ne $InitialStatus) {
                throw "Repository status changed during G6 verification.`n" +
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

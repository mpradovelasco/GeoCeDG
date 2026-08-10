[CmdletBinding()]
param(
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
$ExpectedBaseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
$ExpectedVersion = "5.4.928.0"
$ExpectedTag = "geogebra-baseline-5.4.928.0"
. (Join-Path $PSScriptRoot "upstream-boundary.ps1")

function Write-Step {
    param([Parameter(Mandatory)] [string]$Message)

    if (-not $Quiet) {
        Write-Host "==> $Message"
    }
}

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Assert-Properties {
    param(
        [Parameter(Mandatory)] [object]$Value,
        [Parameter(Mandatory)] [string[]]$Names,
        [Parameter(Mandatory)] [string]$Description
    )

    foreach ($name in $Names) {
        if ($null -eq $Value.PSObject.Properties[$name]) {
            throw "$Description is missing required property '$name'."
        }
    }
}

function Resolve-RepositoryPath {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [switch]$RequireFile
    )

    Assert-Condition -Condition (-not [IO.Path]::IsPathRooted($RelativePath)) `
        -Message "Repository path must be relative: $RelativePath"
    $platformPath = $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $platformPath))
    Assert-Condition -Condition $absolute.StartsWith(
        $RootPrefix, [StringComparison]::OrdinalIgnoreCase) `
        -Message "Repository path escapes the root: $RelativePath"
    if ($RequireFile) {
        Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
            -Message "Required file does not exist: $RelativePath"
    }
    return $absolute
}

function Read-JsonCompatibleYaml {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = Resolve-RepositoryPath -RelativePath $RelativePath -RequireFile
    try {
        return Get-Content -Raw -LiteralPath $absolute |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON-compatible YAML: $($_.Exception.Message)"
    }
}

function Assert-NativeSuccess {
    param(
        [Parameter(Mandatory)] [scriptblock]$Command,
        [Parameter(Mandatory)] [string]$Description
    )

    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

function Assert-CleanTextFile {
    param([Parameter(Mandatory)] [string]$Path)

    $content = [IO.File]::ReadAllText($Path)
    Assert-Condition -Condition ($content.EndsWith("`n")) `
        -Message "Text file must end with a newline: $Path"
    Assert-Condition -Condition (-not [regex]::IsMatch($content, "\r?\n\r?\n$")) `
        -Message "Text file has a blank line at EOF: $Path"

    $lineNumber = 0
    foreach ($line in [regex]::Split($content, "\r?\n")) {
        $lineNumber++
        if ($line -match "[ `t]+$") {
            throw "Trailing whitespace in $Path at line $lineNumber."
        }
    }
}

try {
    Write-Step "Required operational structure"
    $requiredFiles = @(
        ".gitattributes",
        ".gitignore",
        ".github/prompts/canonical/governance.prompt.md",
        ".github/prompts/canonical/verification.prompt.md",
        ".github/prompts/tasks/task-template.prompt.md",
        ".github/prompts/tasks/g1-operational-layer.prompt.md",
        ".github/prompts/tasks/g2-frontend-foundation.prompt.md",
        ".github/prompts/reviews/change-review.prompt.md",
        ".github/workflows/verify.yml",
        "ai-shell/prompts/ask.md",
        "ai-shell/prompts/plan.md",
        "ai-shell/prompts/verify.md",
        "ai-shell/prompts/refactor.md",
        "ai-shell/prompts/architect.md",
        "docs/adr/0002-g1-operational-authority.md",
        "docs/adr/0001-geocedg-product-profile.md",
        "docs/upstream/GEOGEBRA_README.md",
        "docs/upstream/modified-files.yml",
        "docs/validation/g1r_repository_onboarding_report.md",
        "docs/validation/g1_operational_layer_report.md",
        "geocedg/specs/operations/manifest-contracts.md",
        "geocedg/specs/operations/feature-set.schema.json",
        "geocedg/specs/operations/model-manifest.schema.json",
        "geocedg/specs/operations/benchmark-suite.schema.json",
        "geocedg/specs/operations/regression-catalog.schema.json",
        "geocedg/specs/operations/upstream-modifications.schema.json",
        "geocedg/specs/ui/application-profile.schema.json",
        "geocedg/specs/ui/application-profile.md",
        "geocedg/features/stable.yml",
        "geocedg/features/experimental.yml",
        "apps/geocedg/application-profile.yml",
        "models/manifests/model-manifest.template.yml",
        "models/manifests/catalog.yml",
        "geocedg/validation/regression/catalog.yml",
        "benchmarks/suites/operational-smoke.yml",
        "benchmarks/models/stress-catalog.yml",
        "artifacts/README.md",
        "README.md",
        "tools/agent/verify-baseline.ps1",
        "tools/agent/verify-frontend.ps1",
        "tools/agent/verify-operational.ps1",
        "tools/agent/verify.ps1",
        "tools/agent/upstream-boundary.ps1",
        "tools/bootstrap/bootstrap-windows.ps1",
        "tools/benchmark/run.ps1"
    )
    foreach ($requiredFile in $requiredFiles) {
        [void](Resolve-RepositoryPath -RelativePath $requiredFile -RequireFile)
    }

    Write-Step "Repository onboarding contracts"
    $rootReadme = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot "README.md")
    foreach ($requiredReadmeValue in @(
            "# GeoCeDG",
            $ExpectedVersion,
            $ExpectedBaseline,
            $ExpectedTag,
            ".\tools\bootstrap\bootstrap-windows.ps1",
            ".\gradlew.bat :desktop:desktop:run",
            ".\gradlew.bat :desktop:desktop:runGeoCeDG",
            "docs/upstream/GEOGEBRA_README.md")) {
        Assert-Condition -Condition $rootReadme.Contains($requiredReadmeValue) `
            -Message "README.md is missing '$requiredReadmeValue'."
    }

    $attributes = Get-Content -Raw -LiteralPath (
        Join-Path $RepositoryRoot ".gitattributes")
    Assert-Condition -Condition $attributes.Contains(
        "docs/upstream/GEOGEBRA_README.md text eol=lf -whitespace") `
        -Message "Archived README preservation attributes are missing."

    $bootstrap = Get-Content -Raw -LiteralPath (
        Join-Path $RepositoryRoot "tools\bootstrap\bootstrap-windows.ps1")
    Assert-Condition -Condition $bootstrap.Contains("tools\agent\verify.ps1") `
        -Message "Windows bootstrap does not delegate to tools/agent/verify.ps1."
    foreach ($forbiddenBootstrapPattern in @(
            '(?im)\bgit\s+push\b',
            '(?im)\bgit\s+reset\b',
            '(?im)\bgit\s+merge\b',
            '(?im)\bgit\s+rebase\b',
            '(?im)\bgit\s+remote\s+set-url\b',
            '(?im)\bgit\s+config\s+--global\b')) {
        Assert-Condition -Condition (-not [regex]::IsMatch(
                $bootstrap, $forbiddenBootstrapPattern)) `
            -Message "Windows bootstrap contains a prohibited Git operation."
    }

    Write-Step "Prompt contracts and lightweight profiles"
    $requiredTaskHeadings = @(
        "# Objective",
        "# Authority and evidence hierarchy",
        "# Scope",
        "# Explicitly forbidden scope",
        "# Architectural placement",
        "# Required design/specification",
        "# Geometric invariants and degeneracies",
        "# Compatibility and serialization",
        "# Required tests and commands",
        "# Required artifacts",
        "# Stop conditions"
    )
    $taskPromptRoot = Join-Path $RepositoryRoot ".github\prompts\tasks"
    foreach ($taskPrompt in Get-ChildItem -LiteralPath $taskPromptRoot -Filter "*.prompt.md" -File) {
        $content = Get-Content -Raw -LiteralPath $taskPrompt.FullName
        foreach ($heading in $requiredTaskHeadings) {
            Assert-Condition -Condition ([regex]::IsMatch(
                    $content, "(?m)^$([regex]::Escape($heading))\r?$")) `
                -Message "$($taskPrompt.FullName) is missing heading '$heading'."
        }
    }

    $profileNames = @("ask", "plan", "verify", "refactor", "architect")
    foreach ($profileName in $profileNames) {
        $profilePath = Join-Path $RepositoryRoot "ai-shell\prompts\$profileName.md"
        $content = Get-Content -Raw -LiteralPath $profilePath
        Assert-Condition -Condition $content.Contains("../../AGENTS.md") `
            -Message "$profileName profile does not reference AGENTS.md."
        Assert-Condition -Condition $content.Contains("../../.github/prompts/") `
            -Message "$profileName profile does not reference canonical prompts."
    }

    Write-Step "JSON-compatible YAML and schema documents"
    $schemaPaths = @(
        "geocedg/specs/operations/feature-set.schema.json",
        "geocedg/specs/operations/model-manifest.schema.json",
        "geocedg/specs/operations/benchmark-suite.schema.json",
        "geocedg/specs/operations/regression-catalog.schema.json",
        "geocedg/specs/operations/upstream-modifications.schema.json",
        "geocedg/specs/ui/application-profile.schema.json"
    )
    foreach ($schemaPath in $schemaPaths) {
        $schema = Read-JsonCompatibleYaml -RelativePath $schemaPath
        Assert-Properties -Value $schema -Names @('$schema', '$id', 'type') `
            -Description $schemaPath
    }

    $allFeatureIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($setName in @("stable", "experimental")) {
        $path = "geocedg/features/$setName.yml"
        $manifest = Read-JsonCompatibleYaml -RelativePath $path
        Assert-Properties -Value $manifest -Names @(
            '$schema', 'schema_version', 'set', 'features') -Description $path
        Assert-Condition -Condition ($manifest.schema_version -eq 1) `
            -Message "$path has unsupported schema_version."
        Assert-Condition -Condition ($manifest.set -eq $setName) `
            -Message "$path declares set '$($manifest.set)'."
        foreach ($feature in @($manifest.features)) {
            Assert-Properties -Value $feature -Names @(
                'id', 'maturity', 'specification', 'enabled_by_default', 'depends_on') `
                -Description "$path feature"
            Assert-Condition -Condition $allFeatureIds.Add([string]$feature.id) `
                -Message "Duplicate feature id: $($feature.id)"
            [void](Resolve-RepositoryPath -RelativePath $feature.specification -RequireFile)
        }
    }

    $applicationProfilePath = "apps/geocedg/application-profile.yml"
    $applicationProfile = Read-JsonCompatibleYaml -RelativePath $applicationProfilePath
    Assert-Properties -Value $applicationProfile -Names @(
        '$schema', 'schema_version', 'profile_id', 'application', 'serialization',
        'perspective', 'toolbar', 'features') -Description $applicationProfilePath
    Assert-Condition -Condition (
        $applicationProfile.schema_version -eq 1 -and
        $applicationProfile.profile_id -eq "geocedg-desktop" -and
        $applicationProfile.application.name -eq "GeoCeDG" -and
        $applicationProfile.application.preferences_key -eq "geocedg" -and
        $applicationProfile.application.windows_app_id -eq "org.geocedg.desktop") `
        -Message "GeoCeDG application profile identity is invalid."
    Assert-Condition -Condition (
        $applicationProfile.serialization.app_code -eq "classic" -and
        $applicationProfile.serialization.policy -eq "preserve-upstream-g2") `
        -Message "G2 must preserve Classic serialization semantics."
    Assert-Condition -Condition (@($applicationProfile.toolbar.categories).Count -gt 0) `
        -Message "GeoCeDG toolbar has no active categories."
    foreach ($featureId in @($applicationProfile.features)) {
        Assert-Condition -Condition $allFeatureIds.Contains([string]$featureId) `
            -Message "Application profile references unknown feature: $featureId"
    }

    $upstreamModificationsPath = "docs/upstream/modified-files.yml"
    $upstreamModifications = Read-JsonCompatibleYaml `
        -RelativePath $upstreamModificationsPath
    Assert-Properties -Value $upstreamModifications -Names @(
        '$schema', 'schema_version', 'baseline_sha', 'modifications') `
        -Description $upstreamModificationsPath
    Assert-Condition -Condition (
        $upstreamModifications.schema_version -eq 1 -and
        $upstreamModifications.baseline_sha -eq $ExpectedBaseline) `
        -Message "Controlled upstream modification manifest is not pinned to the baseline."

    $modelTemplatePath = "models/manifests/model-manifest.template.yml"
    $modelTemplate = Read-JsonCompatibleYaml -RelativePath $modelTemplatePath
    Assert-Properties -Value $modelTemplate -Names @(
        '$schema', 'schema_version', 'template', 'id', 'maturity', 'source',
        'required_geogebra', 'loaded_by_default', 'inputs', 'outputs',
        'validity_domain', 'known_degeneracies', 'reference_models',
        'expected_metrics', 'license', 'replacement_candidate') `
        -Description $modelTemplatePath
    Assert-Condition -Condition (
        $modelTemplate.schema_version -eq 1 -and $modelTemplate.template) `
        -Message "Model manifest template identity is invalid."
    Assert-Condition -Condition (
        $modelTemplate.required_geogebra.version -eq $ExpectedVersion -and
        $modelTemplate.required_geogebra.baseline_sha -eq $ExpectedBaseline) `
        -Message "Model manifest template baseline is invalid."
    Assert-Condition -Condition (-not $modelTemplate.loaded_by_default) `
        -Message "Model manifest template must not load by default."

    $modelCatalog = Read-JsonCompatibleYaml -RelativePath "models/manifests/catalog.yml"
    Assert-Properties -Value $modelCatalog -Names @('schema_version', 'models') `
        -Description "models/manifests/catalog.yml"
    Assert-Condition -Condition ($modelCatalog.schema_version -eq 1) `
        -Message "Model catalog schema_version is unsupported."
    foreach ($modelManifestPath in @($modelCatalog.models)) {
        [void](Resolve-RepositoryPath -RelativePath $modelManifestPath -RequireFile)
    }

    $regressionCatalogPath = "geocedg/validation/regression/catalog.yml"
    $regressionCatalog = Read-JsonCompatibleYaml -RelativePath $regressionCatalogPath
    Assert-Properties -Value $regressionCatalog -Names @(
        '$schema', 'schema_version', 'baseline_sha', 'cases') `
        -Description $regressionCatalogPath
    Assert-Condition -Condition (
        $regressionCatalog.schema_version -eq 1 -and
        $regressionCatalog.baseline_sha -eq $ExpectedBaseline) `
        -Message "Regression catalog baseline or schema version is invalid."
    foreach ($case in @($regressionCatalog.cases)) {
        Assert-Properties -Value $case -Names @(
            'id', 'manifest', 'specification', 'expected') `
            -Description "Regression case"
        [void](Resolve-RepositoryPath -RelativePath $case.manifest -RequireFile)
        [void](Resolve-RepositoryPath -RelativePath $case.specification -RequireFile)
    }

    $benchmarkSuitePath = "benchmarks/suites/operational-smoke.yml"
    $benchmarkSuite = Read-JsonCompatibleYaml -RelativePath $benchmarkSuitePath
    Assert-Properties -Value $benchmarkSuite -Names @(
        '$schema', 'schema_version', 'id', 'maturity', 'budget_mode', 'cases') `
        -Description $benchmarkSuitePath
    Assert-Condition -Condition (
        $benchmarkSuite.schema_version -eq 1 -and
        $benchmarkSuite.budget_mode -eq "informational") `
        -Message "Benchmark suite version or budget mode is invalid."
    Assert-Condition -Condition (@($benchmarkSuite.cases).Count -gt 0) `
        -Message "Benchmark suite has no cases."
    $benchmarkCaseIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($case in @($benchmarkSuite.cases)) {
        Assert-Properties -Value $case -Names @(
            'id', 'script', 'arguments', 'warmup_iterations',
            'measurement_iterations', 'timeout_seconds', 'budget') `
            -Description "Benchmark case"
        Assert-Condition -Condition $benchmarkCaseIds.Add([string]$case.id) `
            -Message "Duplicate benchmark case id: $($case.id)"
        Assert-Condition -Condition ([string]$case.script).StartsWith("tools/agent/") `
            -Message "Benchmark script is outside tools/agent: $($case.script)"
        [void](Resolve-RepositoryPath -RelativePath $case.script -RequireFile)
        Assert-Condition -Condition ($case.warmup_iterations -ge 0) `
            -Message "Benchmark warm-up count is invalid."
        Assert-Condition -Condition ($case.measurement_iterations -gt 0) `
            -Message "Benchmark measurement count is invalid."
        Assert-Condition -Condition ($case.timeout_seconds -gt 0) `
            -Message "Benchmark timeout is invalid."
        Assert-Properties -Value $case.budget -Names @(
            'metric', 'warning_threshold_ms') -Description "Benchmark budget"
        Assert-Condition -Condition (
            $case.budget.metric -eq "median_elapsed_ms" -and
            $case.budget.warning_threshold_ms -gt 0) `
            -Message "Benchmark budget is invalid."
    }

    $stressCatalogPath = "benchmarks/models/stress-catalog.yml"
    $stressCatalog = Read-JsonCompatibleYaml -RelativePath $stressCatalogPath
    Assert-Properties -Value $stressCatalog -Names @(
        'schema_version', 'status', 'models') -Description $stressCatalogPath
    Assert-Condition -Condition ($stressCatalog.schema_version -eq 1) `
        -Message "Stress catalog schema_version is unsupported."
    foreach ($model in @($stressCatalog.models)) {
        Assert-Properties -Value $model -Names @(
            'id', 'enabled', 'asset', 'target_gate', 'purpose') `
            -Description "Stress model descriptor"
        if ($model.enabled) {
            Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace($model.asset)) `
                -Message "Enabled stress model $($model.id) has no asset."
            [void](Resolve-RepositoryPath -RelativePath $model.asset -RequireFile)
        }
    }

    Write-Step "No legacy model or tool import before G3"
    $forbiddenModelExtensions = @(".ggb", ".ggt", ".js")
    $importedModels = @(Get-ChildItem -LiteralPath (Join-Path $RepositoryRoot "models") `
        -Recurse -File | Where-Object {
            $forbiddenModelExtensions -contains $_.Extension.ToLowerInvariant()
        })
    $importedModelNames = @($importedModels | ForEach-Object { $_.FullName }) -join ", "
    Assert-Condition -Condition ($importedModels.Count -eq 0) `
        -Message "A legacy model/tool asset was imported before G3: $importedModelNames"

    Write-Step "CI delegates to executable authority"
    $workflow = Get-Content -Raw -LiteralPath (
        Join-Path $RepositoryRoot ".github\workflows\verify.yml")
    Assert-Condition -Condition $workflow.Contains(".\tools\agent\verify.ps1") `
        -Message "CI does not invoke tools/agent/verify.ps1."
    Assert-Condition -Condition $workflow.Contains("fetch-depth: 0") `
        -Message "CI must fetch baseline ancestry and tags."
    Assert-Condition -Condition $workflow.Contains('java-version: "22"') `
        -Message "CI Gradle launcher Java is not pinned."

    Write-Step "Operational text hygiene"
    $ownedTextRoots = @(
        ".github", "ai-shell", "geocedg", "models", "benchmarks",
        "artifacts", "apps", "tools/agent", "tools/benchmark", "tools/bootstrap"
    )
    $ownedTextFiles = [Collections.Generic.List[string]]::new()
    foreach ($root in $ownedTextRoots) {
        $absoluteRoot = Join-Path $RepositoryRoot $root
        foreach ($file in Get-ChildItem -LiteralPath $absoluteRoot -Recurse -File) {
            $ownedTextFiles.Add($file.FullName)
        }
    }
    foreach ($relative in @(
            ".gitignore",
            ".gitattributes",
            "README.md",
            "UPSTREAM.md",
            "docs/adr/0001-geocedg-product-profile.md",
            "docs/adr/0002-g1-operational-authority.md",
            "docs/upstream/modified-files.yml",
            "docs/validation/g2_frontend_foundation_report.md",
            "docs/validation/g1r_repository_onboarding_report.md",
            "docs/validation/g1_operational_layer_report.md",
            "tools/agent/verify-baseline.ps1",
            "tools/agent/verify-frontend.ps1",
            "tools/agent/verify-operational.ps1",
            "tools/agent/verify.ps1",
            "tools/agent/upstream-boundary.ps1")) {
        $ownedTextFiles.Add((Resolve-RepositoryPath -RelativePath $relative -RequireFile))
    }
    foreach ($textFile in $ownedTextFiles | Sort-Object -Unique) {
        Assert-CleanTextFile -Path $textFile
    }

    Write-Step "Git whitespace and upstream boundary"
    Assert-NativeSuccess -Description "Working-tree whitespace check" -Command {
        & git -C $RepositoryRoot diff --check
    }
    Assert-NativeSuccess -Description "Index whitespace check" -Command {
        & git -C $RepositoryRoot diff --cached --check
    }
    Assert-GeoCeDGUpstreamBoundary -RepositoryRoot $RepositoryRoot `
        -ExpectedBaseline $ExpectedBaseline

    if (-not $Quiet) {
        Write-Host "Operational verification passed."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

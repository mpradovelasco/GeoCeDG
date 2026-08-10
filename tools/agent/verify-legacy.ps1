[CmdletBinding()]
param(
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
$ExpectedResourceId = "cedg.legacy.template-v7"
$ExpectedArtifactHash = "f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113"
$ExpectedToolNames = @(
    "SplineLength", "sheetISOAnLand", "sheetISOAnVert", "directDimension",
    "SquarebyDiagonal", "CirclebyD", "EllipseAxis", "pointJump",
    "PoliLineVisibility", "Perimeter", "axisDimension", "relCoor",
    "DuctSymbol", "SymmSymbol", "listLength", "listLength12", "postLocus",
    "ellipseVisibility", "translationCoor", "circArcbyAngle", "dummyRotate",
    "conj2mainAxesEllipse", "ellipseLength12", "IFPositiveSelectPoint"
)
$ExpectedStableToolbar = [ordered]@{
    "selection-construction" = @(0)
    "primitives-incidence" = @(1, 2, 15, 18, 7, 16, 3, 4)
    "curves" = @(10, 11, 12)
    "intersections-locus" = @(5, 47)
    "transformations" = @(30, 31, 32)
    "measurement-validation" = @(36, 38)
}

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
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot (
                $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar))))
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

function Assert-SequenceEqual {
    param(
        [Parameter(Mandatory)] [object[]]$Actual,
        [Parameter(Mandatory)] [object[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    Assert-Condition -Condition ($Actual.Count -eq $Expected.Count) `
        -Message "$Description count differs: expected $($Expected.Count), got $($Actual.Count)."
    for ($index = 0; $index -lt $Expected.Count; $index++) {
        Assert-Condition -Condition ([string]$Actual[$index] -ceq [string]$Expected[$index]) `
            -Message "$Description differs at position $($index + 1): expected '$($Expected[$index])', got '$($Actual[$index])'."
    }
}

function Assert-Sha256 {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$Expected
    )

    $absolute = Resolve-RepositoryPath -RelativePath $RelativePath -RequireFile
    $actual = (Get-FileHash -LiteralPath $absolute -Algorithm SHA256).Hash.ToLowerInvariant()
    Assert-Condition -Condition ($actual -ceq $Expected) `
        -Message "SHA-256 mismatch for $RelativePath."
}

function Assert-JsonSchema {
    param(
        [Parameter(Mandatory)] [string]$SchemaPath,
        [Parameter(Mandatory)] [string]$DocumentPath
    )

    $absoluteSchema = Resolve-RepositoryPath -RelativePath $SchemaPath -RequireFile
    $absoluteDocument = Resolve-RepositoryPath -RelativePath $DocumentPath -RequireFile
    $valid = Get-Content -Raw -LiteralPath $absoluteDocument |
        Test-Json -SchemaFile $absoluteSchema -ErrorAction Stop
    Assert-Condition -Condition $valid `
        -Message "$DocumentPath does not satisfy $SchemaPath."
}

try {
    Write-Step "G3 controlled integration structure"
    $requiredFiles = @(
        ".github/prompts/tasks/g3-legacy-integration.prompt.md",
        "docs/adr/0003-controlled-legacy-integration.md",
        "docs/legacy/README.md",
        "docs/references/cedg/README.md",
        "docs/references/cedg/catalog.yml",
        "docs/references/cedg/public-model-corpus.yml",
        "geocedg/specs/legacy/controlled-integration.md",
        "geocedg/specs/operations/legacy-tool-curation.schema.json",
        "geocedg/specs/operations/legacy-tool-inventory.schema.json",
        "geocedg/specs/operations/scientific-reference-catalog.schema.json",
        "geocedg/specs/operations/external-model-corpus.schema.json",
        "models/legacy/template-v7/manifest.yml",
        "models/legacy/template-v7/curation.yml",
        "models/legacy/template-v7/derived/tool-inventory.yml",
        "models/legacy/template-v7/original/Templatev7.ggb",
        "tools/legacy/ingest.ps1",
        "tools/legacy/open-laboratory.ps1"
    )
    foreach ($requiredFile in $requiredFiles) {
        [void](Resolve-RepositoryPath -RelativePath $requiredFile -RequireFile)
    }
    $legacyIntakeFiles = @(Get-ChildItem -LiteralPath (
            Join-Path $RepositoryRoot "docs\legacy") -File)
    Assert-SequenceEqual -Actual @($legacyIntakeFiles.Name) -Expected @("README.md") `
        -Description "docs/legacy intake contents"

    Write-Step "G3 JSON-compatible schema and manifest contracts"
    foreach ($schemaPath in @(
            "geocedg/specs/operations/model-manifest.schema.json",
            "geocedg/specs/operations/legacy-tool-curation.schema.json",
            "geocedg/specs/operations/legacy-tool-inventory.schema.json",
            "geocedg/specs/operations/scientific-reference-catalog.schema.json",
            "geocedg/specs/operations/external-model-corpus.schema.json")) {
        $schema = Read-JsonCompatibleYaml -RelativePath $schemaPath
        Assert-Properties -Value $schema -Names @('$schema', '$id', 'type') `
            -Description $schemaPath
    }
    foreach ($pair in @(
            @("geocedg/specs/operations/model-manifest.schema.json",
                "models/legacy/template-v7/manifest.yml"),
            @("geocedg/specs/operations/legacy-tool-curation.schema.json",
                "models/legacy/template-v7/curation.yml"),
            @("geocedg/specs/operations/legacy-tool-inventory.schema.json",
                "models/legacy/template-v7/derived/tool-inventory.yml"),
            @("geocedg/specs/operations/scientific-reference-catalog.schema.json",
                "docs/references/cedg/catalog.yml"),
            @("geocedg/specs/operations/external-model-corpus.schema.json",
                "docs/references/cedg/public-model-corpus.yml"))) {
        Assert-JsonSchema -SchemaPath $pair[0] -DocumentPath $pair[1]
    }

    $modelCatalog = Read-JsonCompatibleYaml -RelativePath "models/manifests/catalog.yml"
    Assert-SequenceEqual -Actual @($modelCatalog.models) `
        -Expected @("models/legacy/template-v7/manifest.yml") `
        -Description "registered legacy manifests"
    $manifest = Read-JsonCompatibleYaml `
        -RelativePath "models/legacy/template-v7/manifest.yml"
    Assert-Properties -Value $manifest -Names @(
        '$schema', 'schema_version', 'template', 'id', 'maturity', 'source',
        'required_geogebra', 'loaded_by_default', 'artifact',
        'source_environment', 'derived_artifacts', 'implementation',
        'publications', 'laboratory') -Description "Templatev7 manifest"
    Assert-Condition -Condition (
        $manifest.schema_version -eq 1 -and -not $manifest.template -and
        $manifest.id -eq $ExpectedResourceId -and
        $manifest.maturity -eq "legacy" -and
        -not $manifest.loaded_by_default -and
        $manifest.artifact.immutable -and
        $manifest.artifact.sha256 -eq $ExpectedArtifactHash -and
        $manifest.laboratory.eligible -and
        -not $manifest.laboratory.stable_toolbar_replaced) `
        -Message "Templatev7 manifest identity, maturity, or load policy is invalid."
    Assert-Sha256 -RelativePath $manifest.artifact.path -Expected $ExpectedArtifactHash

    Write-Step "Templatev7 deterministic inventory and legacy toolbar"
    $curation = Read-JsonCompatibleYaml `
        -RelativePath "models/legacy/template-v7/curation.yml"
    $inventory = Read-JsonCompatibleYaml `
        -RelativePath "models/legacy/template-v7/derived/tool-inventory.yml"
    Assert-Condition -Condition (
        $curation.schema_version -eq 1 -and
        $curation.resource_id -eq $ExpectedResourceId -and
        $curation.source_artifact_sha256 -eq $ExpectedArtifactHash -and
        $curation.toolbar_interpretation.status -eq "authoritative-legacy-reference" -and
        -not $curation.toolbar_interpretation.future_constraint) `
        -Message "Templatev7 curation identity or toolbar policy is invalid."
    Assert-Condition -Condition (
        $inventory.schema_version -eq 1 -and
        $inventory.resource.id -eq $ExpectedResourceId -and
        $inventory.resource.sha256 -eq $ExpectedArtifactHash -and
        $inventory.container.format -eq "ggb" -and
        $inventory.container.geogebra_version -eq "5.2.879.0" -and
        $inventory.container.app -eq "classic") `
        -Message "Templatev7 structural inventory identity is invalid."
    Assert-Condition -Condition (
        $inventory.scripts.global_javascript.present -and
        @($inventory.scripts.global_javascript.functions).Count -eq 2 -and
        $inventory.scripts.document_geogebrascript_blocks -eq 2) `
        -Message "Templatev7 script inventory is incomplete."
    Assert-SequenceEqual -Actual @($inventory.tools.command_name) `
        -Expected $ExpectedToolNames -Description "Templatev7 macro order"
    Assert-SequenceEqual -Actual @($curation.tools.source_command_name) `
        -Expected $ExpectedToolNames -Description "Templatev7 curation order"
    Assert-SequenceEqual -Actual @($inventory.legacy_toolbar.custom_groups.group) `
        -Expected @(13, 14, 15, 16, 17, 18, 19) `
        -Description "Templatev7 custom toolbar groups"
    Assert-Condition -Condition (
        $inventory.legacy_toolbar.status -eq "authoritative-legacy-reference" -and
        -not $inventory.legacy_toolbar.future_constraint -and
        @($inventory.legacy_toolbar.custom_groups).Count -eq 7 -and
        @($inventory.tools).Count -eq 24) `
        -Message "Templatev7 legacy toolbar classification is invalid."
    foreach ($tool in @($inventory.tools)) {
        Assert-Condition -Condition ($null -ne $tool.original_toolbar) `
            -Message "Legacy toolbar position is missing for $($tool.command_name)."
        Assert-Condition -Condition (@($tool.dependencies.embedded_tools).Count -eq 0) `
            -Message "Unexpected embedded-tool dependency for $($tool.command_name)."
    }
    foreach ($locusToolName in @("postLocus", "listLength", "listLength12")) {
        $locusTool = @($inventory.tools | Where-Object command_name -eq $locusToolName)
        Assert-Condition -Condition (
            $locusTool.Count -eq 1 -and $locusTool[0].maturity -eq "research" -and
            $locusTool[0].family -eq "locus-research") `
            -Message "$locusToolName is not isolated as locus research evidence."
    }
    & (Join-Path $RepositoryRoot "tools\legacy\ingest.ps1") `
        -ResourceId $ExpectedResourceId `
        -Source $manifest.artifact.path `
        -TargetDirectory "models/legacy/template-v7" -Check
    if ($LASTEXITCODE -ne 0) {
        throw "Deterministic Templatev7 ingest verification failed."
    }

    Write-Step "Scientific references and external model corpus"
    $referenceCatalog = Read-JsonCompatibleYaml `
        -RelativePath "docs/references/cedg/catalog.yml"
    Assert-Condition -Condition (
        $referenceCatalog.schema_version -eq 1 -and
        $referenceCatalog.catalog_id -eq "cedg.scientific-references" -and
        @($referenceCatalog.references).Count -eq 12) `
        -Message "Scientific reference catalog identity or count is invalid."
    $referenceIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($reference in @($referenceCatalog.references)) {
        Assert-Properties -Value $reference -Names @(
            'id', 'kind', 'title', 'authors', 'year', 'citation', 'doi', 'isbn',
            'path', 'sha256', 'pages', 'topics', 'relation', 'rights') `
            -Description "scientific reference"
        Assert-Condition -Condition $referenceIds.Add([string]$reference.id) `
            -Message "Duplicate scientific reference ID: $($reference.id)"
        Assert-Sha256 -RelativePath $reference.path -Expected $reference.sha256
    }
    Assert-Condition -Condition $referenceIds.Contains("cedg.reference.book-2023") `
        -Message "The primary CeDG book is not registered."

    $corpus = Read-JsonCompatibleYaml `
        -RelativePath "docs/references/cedg/public-model-corpus.yml"
    Assert-Condition -Condition (
        $corpus.schema_version -eq 1 -and
        $corpus.id -eq "cedg.external.geogebra-book-models" -and
        $corpus.source_url -eq "https://www.geogebra.org/m/nmsgff5s" -and
        $corpus.related_reference -eq "cedg.reference.book-2023" -and
        -not $corpus.build_dependency -and
        $corpus.import_policy -eq "metadata-only-no-bulk-download" -and
        @($corpus.chapters).Count -eq 11) `
        -Message "Public CeDG model corpus policy or chapter count is invalid."
    $materialIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    $materialCount = 0
    foreach ($chapter in @($corpus.chapters)) {
        Assert-Properties -Value $chapter -Names @('chapter_id', 'title', 'models') `
            -Description "public corpus chapter"
        foreach ($model in @($chapter.models)) {
            Assert-Properties -Value $model -Names @(
                'material_id', 'title', 'url', 'book_problem',
                'regression_manifest') -Description "public model metadata"
            Assert-Condition -Condition $materialIds.Add([string]$model.material_id) `
                -Message "Duplicate public GeoGebra material: $($model.material_id)"
            Assert-Condition -Condition ($model.url -eq (
                    "https://www.geogebra.org/m/nmsgff5s#material/$($model.material_id)")) `
                -Message "Public model URL does not preserve provenance: $($model.material_id)"
            Assert-Condition -Condition ($null -eq $model.regression_manifest) `
                -Message "G3 must not promote a remote model to regression automatically."
            $materialCount++
        }
    }
    Assert-Condition -Condition ($materialCount -eq 71) `
        -Message "Expected 71 public model records, got $materialCount."
    foreach ($pilot in @($corpus.pilot_candidates)) {
        Assert-Condition -Condition (
            $materialIds.Contains([string]$pilot.material_id) -and
            $pilot.import_status -eq "not-imported" -and
            $null -eq $pilot.local_manifest) `
            -Message "Pilot candidate $($pilot.material_id) was imported or is not indexed."
    }

    Write-Step "CeDG Laboratory opt-in policy and stable G2 toolbar"
    $stableFeatures = Read-JsonCompatibleYaml `
        -RelativePath "geocedg/features/stable.yml"
    $experimentalFeatures = Read-JsonCompatibleYaml `
        -RelativePath "geocedg/features/experimental.yml"
    $ingestFeature = @($stableFeatures.features | Where-Object id -eq "cedg.legacy.ingest")
    $laboratoryFeature = @($experimentalFeatures.features |
        Where-Object id -eq "cedg.laboratory.legacy")
    Assert-Condition -Condition (
        $ingestFeature.Count -eq 1 -and $ingestFeature[0].maturity -eq "stable" -and
        $laboratoryFeature.Count -eq 1 -and
        $laboratoryFeature[0].maturity -eq "experimental" -and
        -not $laboratoryFeature[0].enabled_by_default) `
        -Message "G3 feature maturity or Laboratory default state is invalid."

    $profile = Read-JsonCompatibleYaml `
        -RelativePath "apps/geocedg/application-profile.yml"
    Assert-SequenceEqual -Actual @($profile.toolbar.categories.id) `
        -Expected @($ExpectedStableToolbar.Keys) -Description "G2 toolbar categories"
    foreach ($category in @($profile.toolbar.categories)) {
        Assert-SequenceEqual -Actual @($category.modes) `
            -Expected @($ExpectedStableToolbar[$category.id]) `
            -Description "G2 toolbar modes for $($category.id)"
    }
    Assert-SequenceEqual -Actual @($profile.features) -Expected @(
        "cedg.frontend.profile", "cedg.frontend.classic-diagnostic") `
        -Description "G2 profile feature selection"

    & (Join-Path $RepositoryRoot "tools\legacy\open-laboratory.ps1") `
        -ResourceId $ExpectedResourceId -ValidateOnly
    if ($LASTEXITCODE -ne 0) {
        throw "CeDG Laboratory resource resolution failed."
    }

    Write-Step "Legacy resource registration completeness"
    $registeredArtifactPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase)
    foreach ($manifestPath in @($modelCatalog.models)) {
        $registeredManifest = Read-JsonCompatibleYaml -RelativePath $manifestPath
        if ($null -ne $registeredManifest.PSObject.Properties['artifact']) {
            [void]$registeredArtifactPaths.Add(
                ([string]$registeredManifest.artifact.path).Replace("\", "/"))
        }
    }
    $legacyResources = @(Get-ChildItem -LiteralPath (
            Join-Path $RepositoryRoot "models\legacy") -Recurse -File |
        Where-Object { $_.Extension.ToLowerInvariant() -in @(
                ".ggb", ".ggt", ".js", ".ggs") })
    foreach ($resource in $legacyResources) {
        $relative = $resource.FullName.Substring($RootPrefix.Length).Replace("\", "/")
        Assert-Condition -Condition $registeredArtifactPaths.Contains($relative) `
            -Message "Legacy resource is not registered by a manifest: $relative"
        Assert-Condition -Condition $relative.Contains("/original/") `
            -Message "Original legacy resource is outside an original/ directory: $relative"
    }
    Assert-Condition -Condition ($legacyResources.Count -eq 1) `
        -Message "G3 intentionally imports only Templatev7; found $($legacyResources.Count) resources."

    if (-not $Quiet) {
        Write-Host "G3 legacy integration verification passed."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

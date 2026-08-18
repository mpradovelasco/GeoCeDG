[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9a2-spatial-point")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "5934d706fd9b30ea11b34d6ff0fe293e971cfc3f"
$ExpectedBranch = "feature/g9a2-spatial-semantic-point-pilot"
$G9A1TagName = "geocedg-g9a1-pass"
$G9A1TagObject = "9b125d9e4d23ff8ce68ce0ad9c16e30a8de338c7"
$G9A1Commit = "02e97ecc9a2e53aece913f7004c50c17fcc663e6"
$G9A2TagName = "geocedg-g9a2-pass"
$PromptPath = ".github/prompts/tasks/g9a2-spatial-semantic-point-pilot.prompt.md"
$PromptSha256 = "d02553668cfa8800fa28428e5ee8f293504bc5519ec53f8ed2d54571d802d23e"
$SpecificationPath = "geocedg/specs/spatial/g9-spatial-projection-semantics.md"
$SpecificationSha256 = "11e1327a6518a25178133a1bfc0720a6d73adabab7d127b5203b6da86b25ca56"
$Adr10Path = "docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md"
$Adr10Sha256 = "25b85c8f29488df3c313f3a1e67cea1cb25714253aa01d13625f8791ad20586d"
$Adr11Path = "docs/adr/0011-g9-spatial-persistence-and-phase-gates.md"
$Adr11Sha256 = "42fd3fdc0a7493f6bde28c1ba2c597e093e138b14fd73619204cb22d001ebf41"
$ValidationPlanPath = "docs/validation/g9_spatial_validation_and_benchmark_plan.md"
$ValidationPlanSha256 = "b50fb1b9d2582ee7478d29a67d1403799642fc2db3ee1dc4a9cda8cd436e1dda"
$ScientificTraceabilityPath = "docs/validation/g9_spatial_scientific_traceability.md"
$ScientificTraceabilitySha256 = "b32436aeccb2f25cc6b557c4f3d947c764db28378069ba1a108990184b35b3ff"
$DesignPath = "docs/architecture/g9a2_spatial_semantic_point_design.md"
$ReportPath = "docs/validation/g9a2_spatial_semantic_point_report.md"
$EvidencePath = "docs/validation/g9a2_spatial_point_evidence.json"
$EvidenceHashPath = "docs/validation/g9a2_spatial_point_evidence.sha256"
$NumericPolicyPath = "geocedg/validation/spatial/g9a2/numeric-policy.json"
$ReferencePath = "geocedg/validation/spatial/g9a2/point-reference-values.json"
$ReferenceHashPath = "geocedg/validation/spatial/g9a2/reference-evidence.sha256"
$ReferenceGeneratorPath = "geocedg/validation/spatial/g9a2/generate_point_references.py"
$ModelManifestPath = "models/regression/g9a2-spatial-point-pilot/manifest.yml"
$ModelArtifactPath = "models/regression/g9a2-spatial-point-pilot/g9a2-spatial-point-pilot.ggb"
$ModelGeneratorPath = "models/regression/g9a2-spatial-point-pilot/generate_model.py"
$ModelCatalogPath = "models/manifests/catalog.yml"
$RegressionCatalogPath = "geocedg/validation/regression/catalog.yml"
$FeatureManifestPath = "geocedg/features/experimental.yml"
$ModifiedFilesPath = "docs/upstream/modified-files.yml"
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$InitialStatus = $null
$GeneratedSnapshot = $null
$AuthorityCommit = $null

$AlwaysRequiredPaths = @(
    $PromptPath,
    $SpecificationPath,
    $Adr10Path,
    $Adr11Path,
    $ValidationPlanPath,
    $ScientificTraceabilityPath,
    $DesignPath,
    $ReportPath,
    $NumericPolicyPath,
    $ReferencePath,
    $ReferenceHashPath,
    $ReferenceGeneratorPath,
    $ModelManifestPath,
    $ModelArtifactPath,
    $ModelGeneratorPath,
    $ModelCatalogPath,
    $RegressionCatalogPath,
    $FeatureManifestPath,
    $ModifiedFilesPath,
    "docs/roadmap/geocedg_roadmap.md",
    "tools/agent/verify-g9a1-spatial-identity.ps1",
    "tools/agent/verify-g9a2-spatial-point.ps1",
    "tools/agent/verify.ps1"
)

$AllowedHostProductivePaths = @(
    "source/shared/common/src/main/java/org/geogebra/common/io/ConsElementXMLHandler.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoDispatcher.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java"
)

$RequiredScenarioIds = @(
    (1..11 | ForEach-Object { "A2-SYS-{0:D2}" -f $_ })
    (1..12 | ForEach-Object { "A2-POINT-{0:D2}" -f $_ })
    (1..5 | ForEach-Object { "A2-DYN-{0:D2}" -f $_ })
    (1..2 | ForEach-Object { "A2-AUTH-{0:D2}" -f $_ })
)

$RequiredHardZeroCounters = @(
    "labelFallbackLookups",
    "coordinateAssociationAttempts",
    "creationOrderAssociationAttempts",
    "xmlPositionAssociationAttempts",
    "outputIndexAssociationAttempts",
    "javaReferenceIdentityAssumptions",
    "visibleDiagramAssociationAttempts",
    "stalePayloadPublications",
    "mixedAuthorityRevisionPublications",
    "hiddenGraphRecomputations",
    "renderCacheReads",
    "rendererReads",
    "viewportReads",
    "screenCoordinateReads",
    "dpiReads",
    "cameraTransformReads",
    "layerOrVisibilityReads"
)

$RequiredZeroScopeCounters = @(
    "spatialDefinedEditing",
    "authorityTransitions",
    "generalPrimitiveSchemas",
    "lineSegmentRayVectorSchemas",
    "planeCircleConicSchemas",
    "spatialCurveSchemas",
    "composedSpatialObjects",
    "surfacesOrSolids",
    "automaticMigration",
    "publicCommands",
    "publicProcedures",
    "guiChanges",
    "locusChanges",
    "dxfChanges",
    "pythonProductChanges",
    "g9a3OrLaterImplementation",
    "productiveG10Implementation",
    "generatedTrackedArtifacts"
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

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)))
    $rootPrefix = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes repository: $RelativePath"
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9A2 artifact is missing: $RelativePath"
    return $absolute
}

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $bytes = [IO.File]::ReadAllBytes((Resolve-RequiredFile `
            -RelativePath $RelativePath))
    $offset = 0
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        $bytes, $offset, $bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $canonicalBytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha.ComputeHash($canonicalBytes)).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Get-BinarySha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return (Get-FileHash -Algorithm SHA256 -LiteralPath (
            Resolve-RequiredFile -RelativePath $RelativePath)).Hash.ToLowerInvariant()
}

function Read-JsonFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
                -RelativePath $RelativePath) | ConvertFrom-Json -Depth 100
    } catch {
        throw "Invalid JSON in ${RelativePath}: $($_.Exception.Message)"
    }
}

function Invoke-GeneratedEvidenceChecks {
    $logPath = Join-Path $LogDirectory "g9a2-generated-evidence.log"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & conda run --no-capture-output -n om_env python `
            $ReferenceGeneratorPath --check 2>&1 | Tee-Object -FilePath $logPath
        $referenceExit = $LASTEXITCODE
        & conda run --no-capture-output -n om_env python `
            $ModelGeneratorPath --check 2>&1 | Tee-Object -FilePath $logPath -Append
        $modelExit = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($referenceExit -eq 0 -and $modelExit -eq 0) `
        -Message "G9A2 generated evidence is stale. See $logPath"

    $manifest = Read-JsonFile -RelativePath $ModelManifestPath
    Assert-Condition -Condition ($manifest.id -eq
            "cedg.regression.g9a2-spatial-point-pilot" -and
            $manifest.artifact.path -eq $ModelArtifactPath -and
            [bool]$manifest.artifact.immutable -and
            $manifest.artifact.sha256 -match '^[0-9a-f]{64}$' -and
            $manifest.artifact.sha256 -eq (Get-BinarySha256 `
                -RelativePath $ModelArtifactPath) -and
            $ReferencePath -in @($manifest.reference_models)) `
        -Message "G9A2 canonical model manifest/hash is inconsistent."

    $modelCatalog = Read-JsonFile -RelativePath $ModelCatalogPath
    Assert-Condition -Condition (@($modelCatalog.models | Where-Object {
                $_ -eq $ModelManifestPath }).Count -eq 1) `
        -Message "G9A2 canonical model is not registered exactly once."
    $regressionCatalog = Read-JsonFile -RelativePath $RegressionCatalogPath
    $cases = @($regressionCatalog.cases | Where-Object {
            $_.id -eq "cedg.regression.g9a2-spatial-point-pilot" })
    Assert-Condition -Condition ($cases.Count -eq 1 -and
            $cases[0].manifest -eq $ModelManifestPath -and
            $cases[0].expected.evidence -eq $ReferencePath -and
            $cases[0].expected.validator -eq
                "tools/agent/verify-g9a2-spatial-point.ps1" -and
            $cases[0].expected.comparison -eq "semantic-point-certificate") `
        -Message "G9A2 regression-catalog entry is inconsistent."
}

function Get-CandidateChangedPaths {
    if ($null -ne $AuthorityCommit) {
        $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
            $EntrySha $AuthorityCommit --)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to enumerate frozen G9A2 changes."
        return @($paths | Where-Object {
                -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
                $_.Replace("\", "/") } | Sort-Object -Unique)
    }

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked G9A2 changes."
    $paths += @(& git -C $RepositoryRoot ls-files --others --exclude-standard)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked G9A2 changes."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
            $_.Replace("\", "/") } | Sort-Object -Unique)
}

function Assert-ExactSet {
    param(
        [Parameter(Mandatory)] [string[]]$Actual,
        [Parameter(Mandatory)] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualSorted = @($Actual | Sort-Object -Unique)
    $expectedSorted = @($Expected | Sort-Object -Unique)
    Assert-Condition -Condition ($actualSorted.Count -eq $Actual.Count) `
        -Message "$Description actual set contains duplicates."
    Assert-Condition -Condition ($expectedSorted.Count -eq $Expected.Count) `
        -Message "$Description expected set contains duplicates."
    Assert-Condition -Condition (($actualSorted -join "`n") -ceq
            ($expectedSorted -join "`n")) `
        -Message "$Description differs.`nActual:`n$($actualSorted -join "`n")`nExpected:`n$($expectedSorted -join "`n")"
}

function Assert-HashManifest {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$ExpectedTargets
    )

    $manifestPath = Resolve-RequiredFile -RelativePath $RelativePath
    $manifestTargets = [Collections.Generic.List[string]]::new()
    $validated = 0
    foreach ($line in @(Get-Content -LiteralPath $manifestPath)) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2 -and
                $parts[0] -match '^[0-9a-fA-F]{64}$') `
            -Message "Malformed G9A2 hash-manifest line: $line"
        $target = $parts[1].Trim().Replace("\", "/")
        $manifestTargets.Add($target)
        Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                    -RelativePath $target) -eq $parts[0].ToLowerInvariant()) `
            -Message "G9A2 canonical-LF hash mismatch: $target"
        $validated++
    }
    Assert-Condition -Condition ($validated -gt 0) `
        -Message "G9A2 hash manifest contains no entries: $RelativePath"
    Assert-ExactSet -Actual $manifestTargets.ToArray() `
        -Expected $ExpectedTargets `
        -Description "$RelativePath hash-manifest targets"
}

function Assert-TagAnchor {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$Object,
        [Parameter(Mandatory)] [string]$Commit
    )

    $ref = (& git -C $RepositoryRoot rev-parse "refs/tags/$Name").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $ref -eq $Object) `
        -Message "$Name does not resolve to its approved annotated tag object."
    $type = (& git -C $RepositoryRoot cat-file -t $Object).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $type -eq "tag") `
        -Message "$Name is not an annotated tag object."
    $peeled = (& git -C $RepositoryRoot rev-parse "$Object^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $peeled -eq $Commit) `
        -Message "$Name does not peel to its approved commit."
    & git -C $RepositoryRoot merge-base --is-ancestor $Commit HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current HEAD does not descend from $Name."
}

function Assert-SourceBoundary {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    $productive = @($Evidence.sourceBoundary.productivePaths)
    $tests = @($Evidence.sourceBoundary.testPaths)
    $fixtures = @($Evidence.sourceBoundary.fixturePaths)
    $models = @($Evidence.sourceBoundary.modelPaths)
    $validation = @($Evidence.sourceBoundary.validationPaths)
    $supporting = @($Evidence.sourceBoundary.supportingPaths)
    foreach ($group in @($productive, $tests, $fixtures, $models,
            $validation, $supporting)) {
        Assert-Condition -Condition ($group.Count -gt 0) `
            -Message "Every G9A2 evidence path group must be nonempty."
    }
    Assert-ExactSet -Actual $ChangedPaths `
        -Expected @($productive + $tests + $fixtures + $models + $validation +
            $supporting) -Description "G9A2 candidate path set"

    foreach ($path in $productive) {
        $additiveSpatial = $path -match `
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/(?:[^/]+/)*[^/]+\.java$'
        Assert-Condition -Condition ($additiveSpatial -or
                $path -in $AllowedHostProductivePaths) `
            -Message "Unapproved productive G9A2 path: $path"
    }
    foreach ($path in $tests) {
        Assert-Condition -Condition ($path -match `
                '^source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A2[^/]+Test\.java$') `
            -Message "G9A2 focused test is outside its private package: $path"
    }
    foreach ($path in $fixtures) {
        Assert-Condition -Condition ($path -match `
                '^source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a2/[^/]+\.(?:xml|ggb|json)$') `
            -Message "G9A2 fixture is outside its private resource package: $path"
    }
    foreach ($path in $models) {
        Assert-Condition -Condition ($path -match `
                '^models/regression/g9a2-spatial-point-pilot/(?:[^/]+/)*[^/]+$' -or
                $path -in @("models/manifests/catalog.yml",
                    "geocedg/validation/regression/catalog.yml")) `
            -Message "G9A2 model path is outside its canonical regression package: $path"
    }
    foreach ($path in $validation) {
        Assert-Condition -Condition ($path -match `
                '^geocedg/validation/spatial/g9a2/[^/]+\.(?:json|py|sha256)$') `
            -Message "G9A2 analytic validation path is outside its package: $path"
    }
    foreach ($path in $supporting) {
        $allowed = $path -in @(
            "docs/architecture/g9a2_spatial_semantic_point_design.md",
            "docs/roadmap/geocedg_roadmap.md",
            "docs/upstream/modified-files.yml",
            "docs/validation/g9a2_spatial_point_evidence.json",
            "docs/validation/g9a2_spatial_point_evidence.sha256",
            "docs/validation/g9a2_spatial_semantic_point_report.md",
            "tools/agent/verify-g9a1-spatial-identity.ps1",
            "tools/agent/verify-g9a2-spatial-point.ps1",
            "tools/agent/verify.ps1")
        Assert-Condition -Condition $allowed `
            -Message "G9A2 supporting path is outside its boundary: $path"
    }

    foreach ($path in $ChangedPaths) {
        foreach ($pattern in @(
                '^apps/', '^python/', '^packaging/', '^source/web/',
                '^source/desktop/', '/(?:locus|export)/',
                '/(?:commands|localization)/',
                'geogebra3D/(?:euclidian3D|kernel3D)/',
                '(?i)g9a3', '(?i)g9b', '(?i)g9c', '(?i)g9u[0-2]',
                '(?i)g9x1', '(?i)g10[a-z0-9]')) {
            Assert-Condition -Condition ($path -notmatch $pattern) `
                -Message "Forbidden G9A2 scope path: $path"
        }
    }

    $registeredManifest = Read-JsonFile -RelativePath $ModifiedFilesPath
    $registered = @($registeredManifest.modifications |
        ForEach-Object { $_.path })
    Assert-Condition -Condition (@($registered | Group-Object |
            Where-Object Count -gt 1).Count -eq 0) `
        -Message "The upstream modified-file inventory contains duplicates."
    foreach ($path in @($ChangedPaths | Where-Object {
                $_.StartsWith("source/") })) {
        Assert-Condition -Condition ($path -in $registered) `
            -Message "Changed source/test/fixture path is not registered: $path"
    }

    # Inspect the whole text of additive files and only the candidate-added
    # lines of already tracked host/substrate files.  The latter can contain
    # unrelated historical GeoGebra/G9A1 behavior that is outside G9A2's
    # authority; only new G9A2 code is required to satisfy these static guards.
    $productiveText = [Collections.Generic.List[string]]::new()
    foreach ($path in $productive) {
        & git -C $RepositoryRoot ls-files --error-unmatch -- $path 2>$null |
            Out-Null
        if ($LASTEXITCODE -eq 0) {
            $diffArguments = @("diff", "--unified=0", $EntrySha)
            if ($null -ne $AuthorityCommit) {
                $diffArguments += $AuthorityCommit
            }
            $diffArguments += @("--", $path)
            foreach ($line in @(& git -C $RepositoryRoot @diffArguments)) {
                if ($line.StartsWith("+") -and
                        -not $line.StartsWith("+++")) {
                    $productiveText.Add($line.Substring(1))
                }
            }
            Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
                -Message "Unable to inspect productive G9A2 diff: $path"
        } else {
            foreach ($line in @(Get-Content -LiteralPath (
                        Resolve-RequiredFile -RelativePath $path))) {
                $productiveText.Add($line)
            }
        }
    }
    $joined = $productiveText -join "`n"
    foreach ($forbidden in @(
            'import\s+org\.geogebra\.common\.euclidian',
            'import\s+.*\.euclidian3D\.',
            'import\s+.*\.(?:Renderer|Drawable3D)\s*;',
            '\.(?:getLabel(?:Simple)?|lookupLabel)\s*\(',
            '\bceID\b', '\.getConstructionIndex\s*\(', '\.getLayer\s*\(',
            '\b(?:EventListener|registerAddListener|ScheduledExecutor|Timer)\b',
            '\b(?:Cmd\w+|CommandProcessor|CommandDispatcher)\b',
            '\b(?:SpatialLine|SpatialSegment|SpatialRay|SpatialVector|SpatialPlane|SpatialCircle|SpatialConic|SpatialCurve|ProjectiveBoundary|SpatialSurface|SpatialSolid)\w*\b')) {
        Assert-Condition -Condition ($joined -notmatch $forbidden) `
            -Message "Forbidden G9A2 authority or later-scope pattern: $forbidden"
    }
}

function Assert-Evidence {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    Assert-Condition -Condition ($Evidence.phase -eq "G9A2" -and
            $Evidence.status -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.provenance.entrySha -eq $EntrySha -and
            $Evidence.provenance.branch -eq $ExpectedBranch -and
            $Evidence.provenance.canonicalPromptCanonicalLfSha256 -eq
                $PromptSha256 -and
            $Evidence.provenance.spatialSpecificationCanonicalLfSha256 -eq
                $SpecificationSha256 -and
            $Evidence.provenance.adr0010CanonicalLfSha256 -eq $Adr10Sha256 -and
            $Evidence.provenance.adr0011CanonicalLfSha256 -eq $Adr11Sha256) `
        -Message "G9A2 evidence provenance/status is inconsistent."
    Assert-Condition -Condition (-not [bool]$Evidence.approval.selfApproved -and
            -not [bool]$Evidence.approval.authorApproved -and
            -not [bool]$Evidence.approval.passClaimed -and
            [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PENDING_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.G9A2 -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.G9A3AndLater -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $Evidence.phaseDisposition.productiveG10 -eq
                "NOT_AUTHORIZED_NOT_STARTED") `
        -Message "G9A2 evidence claims ungranted approval or later-phase authority."

    Assert-SourceBoundary -Evidence $Evidence -ChangedPaths $ChangedPaths

    $scenarioIds = @($Evidence.validation.scenarios |
        ForEach-Object { $_.id })
    Assert-ExactSet -Actual $scenarioIds -Expected $RequiredScenarioIds `
        -Description "G9A2 scenario evidence"
    $ambiguousDisposition = @($Evidence.validation.scenarios | Where-Object {
            $_.id -eq "A2-POINT-07" })
    Assert-Condition -Condition ($ambiguousDisposition.Count -eq 1 -and
            $ambiguousDisposition[0].disposition -eq "NOT_APPLICABLE") `
        -Message "A2-POINT-07 must record its linear-point not-applicable disposition."
    foreach ($scenario in @($Evidence.validation.scenarios | Where-Object {
                $_.id -ne "A2-POINT-07" })) {
        Assert-Condition -Condition ($scenario.disposition -eq "PASSED") `
            -Message "G9A2 scenario is not passed: $($scenario.id)"
    }

    Assert-ExactSet -Actual @($Evidence.semanticCoverage.projectionSystemStates) `
        -Expected @("NOT_EVALUATED", "CONSISTENT", "INCONSISTENT",
            "DEGENERATE", "UNDEFINED") `
        -Description "G9A2 projection-system state coverage"
    Assert-ExactSet -Actual @($Evidence.semanticCoverage.certificateStates) `
        -Expected @("NOT_EVALUATED", "VALID", "UNDERDETERMINED", "AMBIGUOUS",
            "INCONSISTENT_PROJECTIONS", "DEGENERATE", "UNDEFINED") `
        -Description "G9A2 certificate-state coverage"

    $classes = @($Evidence.tests.focused.classes)
    Assert-Condition -Condition ($classes.Count -gt 0) `
        -Message "G9A2 evidence declares no focused test classes."
    $classNames = @($classes | ForEach-Object { $_.name })
    Assert-Condition -Condition (@($classNames | Sort-Object -Unique).Count -eq
            $classNames.Count) `
        -Message "G9A2 focused evidence contains duplicate classes."
    foreach ($testClass in $classes) {
        Assert-Condition -Condition ($testClass.name -match
                '^org\.geocedg\.common\.spatial\.G9A2[^.]+Test$' -and
                [int]$testClass.tests -gt 0) `
            -Message "Invalid G9A2 focused class/count: $($testClass.name)"
    }
    $testPaths = @($Evidence.sourceBoundary.testPaths)
    $expectedTestPaths = @($classNames | ForEach-Object {
            "source/shared/common-jre/src/test/java/{0}.java" -f
                $_.Replace(".", "/")
        })
    Assert-ExactSet -Actual $testPaths -Expected $expectedTestPaths `
        -Description "G9A2 focused test source set"
    $focusedTotal = [int](($classes | Measure-Object -Property tests -Sum).Sum)
    Assert-Condition -Condition ($focusedTotal -eq
            [int]$Evidence.tests.focused.total.tests -and
            [int]$Evidence.tests.focused.total.failures -eq 0 -and
            [int]$Evidence.tests.focused.total.errors -eq 0 -and
            [int]$Evidence.tests.focused.total.skipped -eq 0) `
        -Message "G9A2 expected focused totals are inconsistent."

    foreach ($counter in $RequiredHardZeroCounters) {
        Assert-Condition -Condition ([int64]$Evidence.hardZeroCounters.$counter -eq 0) `
            -Message "G9A2 forbidden-authority counter is nonzero: $counter"
    }
    foreach ($counter in $RequiredZeroScopeCounters) {
        Assert-Condition -Condition ([int64]$Evidence.scopeAudit.$counter -eq 0) `
            -Message "G9A2 forbidden-scope counter is nonzero: $counter"
    }
    Assert-Condition -Condition (
            [bool]$Evidence.validation.realHostXmlRoundTrip -and
            [bool]$Evidence.validation.certificateRecomputedAfterLoad -and
            [bool]$Evidence.validation.atomicFailureWithdrawsPayload -and
            [bool]$Evidence.validation.deterministicRerunAsserted -and
            [bool]$Evidence.oneWayView.sharedKernelAdapterReal -and
            [bool]$Evidence.oneWayView.hostEditingRejectedOrAuthorityPreserved -and
            [bool]$Evidence.oneWayView.failureWithdrawsDerivedPoint -and
            $Evidence.numericPolicy.path -eq $NumericPolicyPath -and
            $Evidence.analyticReference.path -eq $ReferencePath -and
            @($Evidence.scopeDeviations).Count -eq 0) `
        -Message "G9A2 host/persistence/view/numeric evidence is incomplete."

    Assert-HashManifest -RelativePath $EvidenceHashPath `
        -ExpectedTargets @($EvidencePath)
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G9A2 JUnit result: $path"
    [xml]$result = Get-Content -Raw -LiteralPath $path
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("{0} is not clean: tests={1}, failures={2}, errors={3}, " +
            "skipped={4}." -f $ClassName, $suite.tests, $suite.failures,
            $suite.errors, $suite.skipped)
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures/errors/skips."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G9A2 Checkstyle result contains $($errors.Count) violations: $RelativePath"
}

function Invoke-G9A2Tests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test",
        "--tests", "org.geocedg.common.spatial.G9A2*",
        ":shared:common:checkstyleMain",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain",
        "--no-problems-report")
    $logPath = Join-Path $LogDirectory "g9a2-spatial-point-tests.log"
    $exitCode = $null
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($null -ne $exitCode -and $exitCode -eq 0) `
        -Message "G9A2 Gradle gate failed. See $logPath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g9a2"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($required in $AlwaysRequiredPaths) {
        [void](Resolve-RequiredFile -RelativePath $required)
    }
    Assert-HashManifest -RelativePath $ReferenceHashPath `
        -ExpectedTargets @($NumericPolicyPath, $ReferencePath,
            $ReferenceGeneratorPath)
    Invoke-GeneratedEvidenceChecks

    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    & git -C $RepositoryRoot show-ref --verify --quiet "refs/tags/$G9A2TagName"
    $g9a2TagExists = $LASTEXITCODE -eq 0
    Assert-Condition -Condition ($g9a2TagExists -or $LASTEXITCODE -eq 1) `
        -Message "Unable to resolve the G9A2 completion tag."
    if ($g9a2TagExists) {
        $tagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$G9A2TagName").Trim()
        Assert-Condition -Condition ((& git -C $RepositoryRoot cat-file -t `
                    $tagObject).Trim() -eq "tag") `
            -Message "The G9A2 completion tag is not annotated."
        $AuthorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$tagObject^{}").Trim()
        & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $AuthorityCommit
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "G9A2 completion does not descend from its entry baseline."
        & git -C $RepositoryRoot merge-base --is-ancestor $AuthorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not descend from G9A2 completion."
    } else {
        $localMain = (& git -C $RepositoryRoot rev-parse main).Trim()
        $originMain = (& git -C $RepositoryRoot rev-parse origin/main).Trim()
        Assert-Condition -Condition ($branch -eq $ExpectedBranch -and
                $head -eq $EntrySha -and $localMain -eq $EntrySha -and
                $originMain -eq $EntrySha) `
            -Message "G9A2 branch/HEAD/main/origin entry authority drifted."
    }

    Assert-TagAnchor -Name $G9A1TagName -Object $G9A1TagObject `
        -Commit $G9A1Commit
    foreach ($authority in @(
            @($PromptPath, $PromptSha256, "Canonical G9A2 prompt"),
            @($SpecificationPath, $SpecificationSha256,
                "Author-approved spatial specification"),
            @($Adr10Path, $Adr10Sha256, "Accepted ADR 0010"),
            @($Adr11Path, $Adr11Sha256, "Accepted ADR 0011"),
            @($ValidationPlanPath, $ValidationPlanSha256,
                "Author-approved spatial validation plan"),
            @($ScientificTraceabilityPath, $ScientificTraceabilitySha256,
                "Spatial scientific traceability"))) {
        Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                    -RelativePath $authority[0]) -eq $authority[1]) `
            -Message "$($authority[2]) LF SHA-256 mismatch."
    }

    $specificationText = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $SpecificationPath)
    $adr10Text = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $Adr10Path)
    $adr11Text = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $Adr11Path)
    Assert-Condition -Condition ($specificationText.Contains(
            "NORMATIVE / AUTHOR APPROVED") -and
            $adr10Text.Contains("Status: **Accepted**") -and
            $adr11Text.Contains("Status: **Accepted**")) `
        -Message "G9A2 normative status gate is not author-approved/Accepted."

    $roadmapText = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/roadmap/geocedg_roadmap.md")
    Assert-Condition -Condition ($roadmapText.Contains(
            "G9A1 = PASS — AUTHOR APPROVED") -and
            $roadmapText.Contains(
                "G9A2 = PASS — AUTHOR APPROVED")) `
        -Message "Living G9A2 author-closeout status is inconsistent."

    $featureManifest = Read-JsonFile -RelativePath $FeatureManifestPath
    $spatialFeatures = @($featureManifest.features | Where-Object {
            $_.id -eq "cedg.spatial.semantics" })
    Assert-Condition -Condition ($spatialFeatures.Count -eq 1 -and
            $spatialFeatures[0].maturity -eq "experimental" -and
            -not [bool]$spatialFeatures[0].enabled_by_default) `
        -Message "G9A2 spatial semantics must remain experimental and disabled by default."

    $changedPaths = @(Get-CandidateChangedPaths)
    $hasEvidence = Test-Path -LiteralPath (Join-Path $RepositoryRoot `
        $EvidencePath.Replace("/", "\")) -PathType Leaf
    $hasCandidateSource = @($changedPaths | Where-Object {
            $_ -match '^source/.+/G9A2|^source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/'
        }).Count -gt 0
    if ($hasEvidence) {
        [void](Resolve-RequiredFile -RelativePath $EvidenceHashPath)
        $evidence = Read-JsonFile -RelativePath $EvidencePath
        Assert-Evidence -Evidence $evidence -ChangedPaths $changedPaths
    } else {
        Assert-Condition -Condition (-not $hasCandidateSource) `
            -Message "Productive/test G9A2 source exists without candidate evidence JSON."
        Write-Host "G9A2 evidence JSON is not present; bootstrap static validation only."
        $evidence = $null
    }

    if (-not $SkipBuild -and $null -ne $evidence) {
        Invoke-G9A2Tests
        $classes = @($evidence.tests.focused.classes)
        foreach ($testClass in $classes) {
            Assert-TestResult -ClassName $testClass.name `
                -ExpectedTests ([int]$testClass.tests)
        }
        $actualResults = @(Get-ChildItem -LiteralPath $TestResultRoot `
            -Filter "TEST-org.geocedg.common.spatial.G9A2*.xml" -File |
            ForEach-Object { $_.BaseName.Substring(5) } | Sort-Object -Unique)
        Assert-ExactSet -Actual $actualResults `
            -Expected @($classes | ForEach-Object { $_.name }) `
            -Description "G9A2 focused JUnit class set"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
    } elseif ($SkipBuild) {
        Write-Host "Skipping G9A2 Gradle tests because -SkipBuild was supplied."
    } else {
        Write-Host "Skipping G9A2 Gradle tests during evidence bootstrap."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9A2."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9A2."

    if ($null -ne $evidence) {
        Write-Host ("G9A2 focused author-approved verification passed ({0} tests)." -f
            [int]$evidence.tests.focused.total.tests)
        Write-Host "G9A2 = PASS — AUTHOR APPROVED."
    } else {
        Write-Host "G9A2 verifier bootstrap validation passed."
    }
    Write-Host "Current later-phase status is governed by the living roadmap and separate author decisions."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error "G9A2 focused verification failed: $($_.Exception.Message)"
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G9A2 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G9A2 verification."
            exit 1
        }
    }
}

exit 0

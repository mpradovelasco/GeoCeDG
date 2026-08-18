[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9a3-spatial-lifecycle")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "1efa338414cdbe76cbb913bbb45ea26c7108bba3"
$ExpectedBranch = "feature/g9a3-spatial-lifecycle-migration"
$G9A2TagName = "geocedg-g9a2-pass"
$G9A2TagObject = "96434555950a65d420360a22282fc4cbb2db9c78"
$G9A2Commit = "1efa338414cdbe76cbb913bbb45ea26c7108bba3"
$G9A3TagName = "geocedg-g9a3-pass"
$PromptPath = ".github/prompts/tasks/g9a3-spatial-lifecycle-migration.prompt.md"
$PromptSha256 = "f12d9f66eb4f2f9df8afe715f9f9039e8a45e8a377a08491e79252891f6f7651"
$SpecificationPath = "geocedg/specs/spatial/g9-spatial-projection-semantics.md"
$SpecificationSha256 = "11e1327a6518a25178133a1bfc0720a6d73adabab7d127b5203b6da86b25ca56"
$Adr10Path = "docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md"
$Adr10Sha256 = "25b85c8f29488df3c313f3a1e67cea1cb25714253aa01d13625f8791ad20586d"
$Adr11Path = "docs/adr/0011-g9-spatial-persistence-and-phase-gates.md"
$Adr11Sha256 = "42fd3fdc0a7493f6bde28c1ba2c597e093e138b14fd73619204cb22d001ebf41"
$ValidationPlanPath = "docs/validation/g9_spatial_validation_and_benchmark_plan.md"
$ValidationPlanSha256 = "b50fb1b9d2582ee7478d29a67d1403799642fc2db3ee1dc4a9cda8cd436e1dda"
$DesignPath = "docs/architecture/g9a3_spatial_lifecycle_migration_design.md"
$ScenarioPath = "docs/validation/g9a3_spatial_lifecycle_scenarios.json"
$CorpusPath = "docs/validation/g9a3_spatial_compatibility_corpus.json"
$CorpusHashPath = "docs/validation/g9a3_spatial_compatibility_corpus.sha256"
$CompatibilityMatrixPath = "docs/validation/g9a3_spatial_compatibility_matrix.md"
$ReportPath = "docs/validation/g9a3_spatial_lifecycle_migration_report.md"
$EvidencePath = "docs/validation/g9a3_spatial_lifecycle_evidence.json"
$EvidenceHashPath = "docs/validation/g9a3_spatial_lifecycle_evidence.sha256"
$ModifiedFilesPath = "docs/upstream/modified-files.yml"
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$InitialStatus = $null
$GeneratedSnapshot = $null
$AuthorityCommit = $null

$RequiredPaths = @(
    $PromptPath,
    $SpecificationPath,
    $Adr10Path,
    $Adr11Path,
    $ValidationPlanPath,
    $DesignPath,
    $ScenarioPath,
    $CorpusPath,
    $CorpusHashPath,
    $CompatibilityMatrixPath,
    $ReportPath,
    $EvidencePath,
    $EvidenceHashPath,
    "docs/roadmap/geocedg_roadmap.md",
    $ModifiedFilesPath,
    "geocedg/features/experimental.yml",
    "models/regression/g9a2-spatial-point-pilot/g9a2-spatial-point-pilot.ggb",
    "source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A3SpatialGraphSnapshot.java",
    "tools/agent/verify-g9a1-spatial-identity.ps1",
    "tools/agent/verify-g9a2-spatial-point.ps1",
    "tools/agent/verify-g9a3-spatial-lifecycle.ps1",
    "tools/agent/verify.ps1"
)

$RequiredScenarioIds = @(
    (1..10 | ForEach-Object { "G9A3-LIFE{0:D2}" -f $_ })
    (1..8 | ForEach-Object { "G9A3-COPY{0:D2}" -f $_ })
    (1..25 | ForEach-Object { "G9A3-REDEF{0:D2}" -f $_ })
    (1..5 | ForEach-Object { "G9A3-SNAP{0:D2}" -f $_ })
    (1..10 | ForEach-Object { "G9A3-XML{0:D2}" -f $_ })
    (1..6 | ForEach-Object { "G9A3-MIG{0:D2}" -f $_ })
    (1..5 | ForEach-Object { "G9A3-COMPAT{0:D2}" -f $_ })
    (1..3 | ForEach-Object { "G9A3-AUTH{0:D2}" -f $_ })
)

$ExpectedG9A3TestCounts = [ordered]@{
    "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest" = 10
    "org.geocedg.common.spatial.G9A3SpatialCopyClosureTest" = 8
    "org.geocedg.common.spatial.G9A3SpatialExplicitMigrationTest" = 6
    "org.geocedg.common.spatial.G9A3SpatialLifecycleInstrumentationTest" = 3
    "org.geocedg.common.spatial.G9A3SpatialMutationLifecycleTest" = 10
    "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest" = 5
    "org.geocedg.common.spatial.G9A3SpatialRedefineHostTest" = 15
    "org.geocedg.common.spatial.G9A3SpatialRedefineTransactionTest" = 10
    "org.geocedg.common.spatial.G9A3SpatialSnapshotRecoveryTest" = 5
}

$AllowedHostProductivePaths = @(
    ("source/shared/common-jre/src/main/java/org/geogebra/common/jre/headless/" +
        "EuclidianController3DNoGui.java"),
    ("source/shared/common/src/main/java/org/geogebra/common/geogebra3D/" +
        "euclidian3D/EuclidianController3D.java"),
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java",
    ("source/shared/common/src/main/java/org/geogebra/common/kernel/algos/" +
        "AlgoDispatcher.java"),
    ("source/shared/common/src/main/java/org/geogebra/common/kernel/commands/" +
        "AlgebraProcessor.java")
)

$RequiredHardZeroCounterNames = @(
    "identity.labelAuthorityUses",
    "identity.coordinateAuthorityUses",
    "identity.constructionOrderAuthorityUses",
    "identity.xmlPositionAuthorityUses",
    "identity.outputOrdinalAuthorityUses",
    "identity.javaInstanceAuthorityUses",
    "identity.viewportAuthorityUses",
    "identity.dpiAuthorityUses",
    "identity.cameraAuthorityUses",
    "identity.rendererAuthorityUses",
    "identity.screenStateAuthorityUses",
    "semantic.labelFallbackLookups",
    "semantic.coordinateAssociationAttempts",
    "semantic.creationOrderAssociationAttempts",
    "semantic.xmlPositionAssociationAttempts",
    "semantic.outputIndexAssociationAttempts",
    "semantic.javaReferenceIdentityAssumptions",
    "semantic.visibleDiagramAssociationAttempts",
    "semantic.stalePayloadPublications",
    "semantic.mixedAuthorityRevisionPublications",
    "semantic.hiddenGraphRecomputations",
    "semantic.renderCacheReads",
    "semantic.rendererReads",
    "semantic.viewportReads",
    "semantic.screenCoordinateReads",
    "semantic.dpiReads",
    "semantic.cameraTransformReads",
    "semantic.layerOrVisibilityReads"
)

$RequiredZeroScopeCounters = @(
    "generalPrimitiveSchemas",
    "composedSpatialObjects",
    "publicCommands",
    "publicProcedures",
    "guiChanges",
    "locusChanges",
    "dxfChanges",
    "pythonProductChanges",
    "labelOrGeometricMigrationInference",
    "universalMergeSplitGenealogy",
    "silentRepair",
    "laterG9PhaseImplementation",
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
        -Message "Required G9A3 artifact is missing: $RelativePath"
    return $absolute
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

function Assert-ExactSet {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Actual,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualUnique = @($Actual | Sort-Object -Unique)
    $expectedUnique = @($Expected | Sort-Object -Unique)
    $duplicateCount = @($Actual).Count - $actualUnique.Count
    $missing = @($expectedUnique | Where-Object { $_ -notin $actualUnique })
    $unexpected = @($actualUnique | Where-Object { $_ -notin $expectedUnique })
    Assert-Condition -Condition ($duplicateCount -eq 0 -and
            $missing.Count -eq 0 -and $unexpected.Count -eq 0) `
        -Message ("{0} mismatch. duplicates={1}; missing={2}; unexpected={3}" -f
            $Description, $duplicateCount, ($missing -join ", "),
            ($unexpected -join ", "))
}

function Assert-TagAnchor {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$Object,
        [Parameter(Mandatory)] [string]$Commit
    )

    $actualObject = (& git -C $RepositoryRoot rev-parse "refs/tags/$Name").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $actualObject -eq $Object) `
        -Message "Tag object drifted for $Name."
    Assert-Condition -Condition ((& git -C $RepositoryRoot cat-file -t `
            $actualObject).Trim() -eq "tag") `
        -Message "$Name is not an annotated tag."
    $actualCommit = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$Name^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $actualCommit -eq $Commit) `
        -Message "Peeled commit drifted for $Name."
}

function Assert-HashManifest {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$ExpectedTargets
    )

    $entries = @()
    foreach ($line in Get-Content -LiteralPath (Resolve-RequiredFile `
            -RelativePath $RelativePath)) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#")) {
            continue
        }
        Assert-Condition -Condition ($line -match `
            '^([0-9a-f]{64})  ([A-Za-z0-9._/\-]+)$') `
            -Message "Malformed SHA-256 manifest line in ${RelativePath}: $line"
        $entries += [pscustomobject]@{
            Hash = $Matches[1]
            Path = $Matches[2]
        }
    }
    Assert-ExactSet -Actual @($entries | ForEach-Object { $_.Path }) `
        -Expected $ExpectedTargets -Description "$RelativePath target set"
    foreach ($entry in $entries) {
        $actualHash = if ($entry.Path.EndsWith(".ggb",
                [StringComparison]::OrdinalIgnoreCase)) {
            Get-BinarySha256 -RelativePath $entry.Path
        } else {
            Get-CanonicalTextSha256 -RelativePath $entry.Path
        }
        Assert-Condition -Condition ($actualHash -eq $entry.Hash) `
            -Message "SHA-256 mismatch for $($entry.Path)."
    }
}

function Get-G9A3TestMethods {
    param([Parameter(Mandatory)] [string]$ClassName)

    $relativePath = "source/shared/common-jre/src/test/java/{0}.java" -f
        $ClassName.Replace(".", "/")
    $source = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath $relativePath)
    $pattern = '(?ms)@Test\s+(?:@[^\r\n]+\s+)*' +
        '(?:(?:public|protected|private)\s+)?(?:final\s+)?' +
        'void\s+([A-Za-z0-9_]+)\s*\('
    return @([regex]::Matches($source, $pattern) | ForEach-Object {
            $_.Groups[1].Value
        })
}

function Get-ScenarioIdFromTestMethod {
    param([Parameter(Mandatory)] [string]$MethodName)

    Assert-Condition -Condition ($MethodName -match
            '^(life|copy|redef|snap|xml|mig|compat|auth)([0-9]{2})') `
        -Message "G9A3 test method lacks a canonical scenario prefix: $MethodName"
    return "G9A3-{0}{1}" -f $Matches[1].ToUpperInvariant(), $Matches[2]
}

function Assert-ScenarioAuthority {
    $scenario = Read-JsonFile -RelativePath $ScenarioPath
    Assert-Condition -Condition ($scenario.phase -eq "G9A3" -and
            $scenario.status -eq
                "IMPLEMENTATION_CANDIDATE_SCENARIOS_FROZEN" -and
            -not [bool]$scenario.authorApprovalClaimed -and
            [bool]$scenario.countsFrozen -and
            [int]$scenario.expectedScenarioCount -eq 72) `
        -Message "G9A3 scenario status/count policy is inconsistent."
    $ids = @($scenario.groups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.id })
    Assert-ExactSet -Actual $ids -Expected $RequiredScenarioIds `
        -Description "G9A3 lifecycle scenarios"
    Assert-Condition -Condition ([int]$scenario.testExecution.frozenInheritedCount `
            -eq 181 -and [int]$scenario.testExecution.g9a3Count -eq 72 -and
            [int]$scenario.testExecution.focusedTotal -eq 253) `
        -Message "Frozen G9A3 scenario/test totals are inconsistent."

    $mappedClasses = [Collections.Generic.List[string]]::new()
    $mappedIds = [Collections.Generic.List[string]]::new()
    foreach ($group in @($scenario.groups)) {
        $groupClasses = @(if ($null -ne
                $group.PSObject.Properties["testClass"]) {
            [string]$group.testClass
        } else {
            $group.testClasses | ForEach-Object { [string]$_ }
        })
        Assert-Condition -Condition ($groupClasses.Count -gt 0) `
            -Message "Scenario group $($group.id) has no test class."
        $groupMethodIds = [Collections.Generic.List[string]]::new()
        foreach ($className in $groupClasses) {
            $mappedClasses.Add($className)
            foreach ($method in @(Get-G9A3TestMethods -ClassName $className)) {
                $methodId = Get-ScenarioIdFromTestMethod -MethodName $method
                $mappedIds.Add($methodId)
                $groupMethodIds.Add($methodId)
            }
        }
        Assert-ExactSet -Actual $groupMethodIds.ToArray() `
            -Expected @($group.cases | ForEach-Object { $_.id }) `
            -Description "G9A3 scenario-to-test mapping for $($group.id)"
    }
    Assert-ExactSet -Actual $mappedClasses.ToArray() `
        -Expected @($ExpectedG9A3TestCounts.Keys) `
        -Description "G9A3 scenario test-class set"
    Assert-ExactSet -Actual $mappedIds.ToArray() -Expected $RequiredScenarioIds `
        -Description "G9A3 one-to-one scenario method mapping"
    foreach ($className in @($ExpectedG9A3TestCounts.Keys)) {
        Assert-Condition -Condition (@(Get-G9A3TestMethods `
                    -ClassName $className).Count -eq
                [int]$ExpectedG9A3TestCounts[$className]) `
            -Message "G9A3 source test count drifted for $className."
    }
}

function Assert-CompatibilityCorpus {
    $corpus = Read-JsonFile -RelativePath $CorpusPath
    Assert-Condition -Condition ($corpus.phase -eq "G9A3" -and
            $corpus.status -eq "IMPLEMENTATION_CANDIDATE_CORPUS_FROZEN" -and
            -not [bool]$corpus.authorApprovalClaimed -and
            [bool]$corpus.hashesFrozen -and
            -not [bool]$corpus.boundary.lossyConversionAuthorized -and
            $corpus.boundary.externalUpstreamRuntimeEvidence -eq
                "NOT_EXECUTED_OR_CLAIMED_BY_THIS_CORPUS") `
        -Message "G9A3 compatibility boundary overclaims support or approval."
    $entries = @($corpus.entries)
    Assert-Condition -Condition ($entries.Count -eq 13 -and
            @($entries | Group-Object id | Where-Object Count -gt 1).Count -eq 0 -and
            @($entries | Group-Object path | Where-Object Count -gt 1).Count -eq 0) `
        -Message "G9A3 compatibility corpus has duplicate or missing entries."
    foreach ($entry in $entries) {
        Assert-Condition -Condition ($entry.sha256 -match '^[0-9a-f]{64}$') `
            -Message "Invalid corpus hash for $($entry.id)."
        $actualHash = if ($entry.path.EndsWith(".ggb",
                [StringComparison]::OrdinalIgnoreCase)) {
            Get-BinarySha256 -RelativePath $entry.path
        } else {
            Get-CanonicalTextSha256 -RelativePath $entry.path
        }
        Assert-Condition -Condition ($actualHash -eq $entry.sha256) `
            -Message "Corpus entry hash drifted for $($entry.id)."
    }
    $targets = @($CorpusPath) + @($entries | ForEach-Object { $_.path })
    Assert-HashManifest -RelativePath $CorpusHashPath -ExpectedTargets $targets

    foreach ($entry in @($entries | Where-Object { $_.path.EndsWith(".xml") })) {
        try {
            [void][xml](Get-Content -Raw -LiteralPath (
                Resolve-RequiredFile -RelativePath $entry.path))
        } catch {
            throw "Malformed XML fixture $($entry.path): $($_.Exception.Message)"
        }
    }
    $lossShapePath = "source/shared/common-jre/src/test/resources/" +
        "org/geocedg/common/spatial/g9a3/" +
        "external-upstream-no-spatial-loss-shape.xml"
    $lossShape = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath $lossShapePath)
    Assert-Condition -Condition ($lossShape.Contains(
            "not an externally produced artifact") -and
            -not $lossShape.Contains("<geocedgSpatial")) `
        -Message "The external loss-shape fixture has unsafe provenance or semantics."
}

function Get-CandidateChangedPaths {
    if ($null -ne $AuthorityCommit) {
        $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
            $EntrySha $AuthorityCommit --)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to enumerate frozen G9A3 changes."
        return @($paths | Where-Object {
                -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
                $_.Replace("\", "/") } | Sort-Object -Unique)
    }

    $paths = [Collections.Generic.List[string]]::new()
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only $EntrySha --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $paths.Add($path.Replace("\", "/"))
        }
    }
    foreach ($path in @(& git -C $RepositoryRoot ls-files --others `
            --exclude-standard)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    return @($paths | Sort-Object -Unique)
}

function Assert-BootstrapScope {
    param([Parameter(Mandatory)] [string[]]$ChangedPaths)

    foreach ($path in $ChangedPaths) {
        foreach ($pattern in @(
                '^apps/', '^python/', '^packaging/', '^source/web/',
                '/(?:locus|export)/', '/localization/',
                '(?i)g9b', '(?i)g9c', '(?i)g9u[0-2]', '(?i)g9x1',
                '(?i)g10[a-z0-9]')) {
            Assert-Condition -Condition ($path -notmatch $pattern) `
                -Message "Forbidden G9A3 scope path: $path"
        }
        if ($path -match '/commands/' -and $path -ne
                'source/shared/common/src/main/java/org/geogebra/common/' +
                'kernel/commands/AlgebraProcessor.java') {
            throw "Forbidden G9A3 command-surface path: $path"
        }
    }

    $migrationSources = @($ChangedPaths | Where-Object {
        $_ -match '^source/shared/common/src/main/java/org/geocedg/common/' +
            'kernel/spatial/.+(?:Lifecycle|Migration|Copy|Graph|Association).+\.java$'
    })
    $sourceText = ($migrationSources | ForEach-Object {
        Get-Content -Raw -LiteralPath (Resolve-RequiredFile -RelativePath $_)
    }) -join "`n"
    foreach ($forbidden in @(
            '\.(?:getLabel(?:Simple)?|lookupLabel|getConstructionIndex|getLayer)\s*\(',
            'import\s+.*\.(?:euclidian|Renderer|Drawable)',
            '\b(?:CommandProcessor|CommandDispatcher|Cmd[A-Z]\w*)\b',
            '\b(?:SpatialLine|SpatialSegment|SpatialRay|SpatialPlane|SpatialCircle|SpatialConic|SpatialCurve|SpatialSurface|SpatialSolid|ProjectiveBoundary)\w*\b')) {
        Assert-Condition -Condition ($sourceText -notmatch $forbidden) `
            -Message "Forbidden G9A3 migration authority pattern: $forbidden"
    }
}

function Assert-SourceBoundary {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    $productive = @($Evidence.sourceBoundary.productivePaths)
    $tests = @($Evidence.sourceBoundary.testPaths)
    $testSupport = @($Evidence.sourceBoundary.testSupportPaths)
    $fixtures = @($Evidence.sourceBoundary.fixturePaths)
    $corpus = @($Evidence.sourceBoundary.corpusPaths)
    $validation = @($Evidence.sourceBoundary.validationPaths)
    $supporting = @($Evidence.sourceBoundary.supportingPaths)
    $all = @($productive + $tests + $testSupport + $fixtures + $corpus +
        $validation + $supporting)

    Assert-Condition -Condition (
            $Evidence.sourceBoundary.inventoryStatus -eq "FROZEN" -and
            [int]$Evidence.sourceBoundary.totalPaths -eq 81 -and
            [int]$Evidence.sourceBoundary.counts.productive -eq 46 -and
            [int]$Evidence.sourceBoundary.counts.tests -eq 12 -and
            [int]$Evidence.sourceBoundary.counts.testSupport -eq 2 -and
            [int]$Evidence.sourceBoundary.counts.fixtures -eq 9 -and
            [int]$Evidence.sourceBoundary.counts.corpus -eq 2 -and
            [int]$Evidence.sourceBoundary.counts.validation -eq 5 -and
            [int]$Evidence.sourceBoundary.counts.supporting -eq 5 -and
            [int]$Evidence.sourceBoundary.counts.modified -eq 35 -and
            [int]$Evidence.sourceBoundary.counts.new -eq 46 -and
            $all.Count -eq 81) `
        -Message "G9A3 frozen source-boundary counts are inconsistent."
    Assert-ExactSet -Actual $ChangedPaths -Expected $all `
        -Description "G9A3 exact candidate path inventory"

    foreach ($path in $productive) {
        $additiveSpatial = $path -match
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/' +
            '(?:[^/]+/)*[^/]+\.java$'
        Assert-Condition -Condition ($additiveSpatial -or
                $path -in $AllowedHostProductivePaths) `
            -Message "Unapproved productive G9A3 path: $path"
    }
    $inheritedTestPaths = @(
        ("source/shared/common-jre/src/test/java/org/geocedg/common/spatial/" +
            "G9A1SpatialIdentityRedefineHostTest.java"),
        ("source/shared/common-jre/src/test/java/org/geocedg/common/spatial/" +
            "G9A1SpatialRedefineTransactionTest.java"),
        ("source/shared/common-jre/src/test/java/org/geocedg/common/spatial/" +
            "G9A2SpatialSemanticRuntimeTest.java")
    )
    foreach ($path in $tests) {
        Assert-Condition -Condition ($path -in $inheritedTestPaths -or
                $path -match '^source/shared/common-jre/src/test/java/' +
                    'org/geocedg/common/spatial/G9A3[^/]+Test\.java$') `
            -Message "G9A3 test path is outside its exact package: $path"
    }
    Assert-ExactSet -Actual $testSupport -Expected @(
        ("source/shared/common-jre/src/test/java/org/geocedg/common/spatial/" +
            "G9A3SpatialGraphSnapshot.java"),
        ("source/shared/common-jre/src/test/java/org/geocedg/common/spatial/" +
            "G9A3SpatialRedefineTestSupport.java")) `
        -Description "G9A3 test-support path set"
    foreach ($path in $fixtures) {
        Assert-Condition -Condition ($path -match
                '^source/shared/common-jre/src/test/resources/' +
                'org/geocedg/common/spatial/g9a3/[^/]+\.xml$') `
            -Message "G9A3 fixture is outside its private package: $path"
    }
    Assert-ExactSet -Actual $corpus -Expected @($CorpusPath, $CorpusHashPath) `
        -Description "G9A3 corpus artifact set"
    Assert-ExactSet -Actual $validation -Expected @(
        $CompatibilityMatrixPath,
        $EvidencePath,
        $EvidenceHashPath,
        $ReportPath,
        $ScenarioPath) -Description "G9A3 validation artifact set"
    Assert-ExactSet -Actual $supporting -Expected @(
        $DesignPath,
        "docs/roadmap/geocedg_roadmap.md",
        $ModifiedFilesPath,
        "tools/agent/verify-g9a3-spatial-lifecycle.ps1",
        "tools/agent/verify.ps1") -Description "G9A3 supporting artifact set"

    $manifest = Read-JsonFile -RelativePath $ModifiedFilesPath
    $registered = @($manifest.modifications | ForEach-Object { $_.path })
    Assert-Condition -Condition (@($registered | Group-Object | Where-Object {
                    $_.Count -gt 1 }).Count -eq 0) `
        -Message "The upstream modified-file inventory contains duplicates."
    foreach ($path in @($ChangedPaths | Where-Object {
                $_.StartsWith("source/") })) {
        Assert-Condition -Condition ($path -in $registered) `
            -Message "Changed source/test/fixture path is not registered: $path"
    }

    $changedGenerated = @($ChangedPaths | Where-Object {
            $_ -match '(^|/)(?:artifacts|build|\.gradle|\.kotlin)(?:/|$)'
        })
    Assert-Condition -Condition ($changedGenerated.Count -eq 0) `
        -Message "Tracked generated G9A3 path detected: $($changedGenerated -join ', ')"
}

function Assert-ReportStatus {
    $report = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath $ReportPath)
    Assert-Condition -Condition ($report.Contains(
            "IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW") -and
            $report.Contains('**Author approved:** `false`') -and
            $report.Contains('**Pass claimed:** `false`') -and
            -not ($report -match '(?m)^G9A3 = PASS')) `
        -Message "G9A3 candidate report claims ungranted approval."

    $roadmap = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/roadmap/geocedg_roadmap.md")
    Assert-Condition -Condition ($roadmap.Contains(
            'G9A3 | `PASS — AUTHOR APPROVED`') -and
            $roadmap.Contains(
                'G9A2 | `PASS — AUTHOR APPROVED`') -and
            $roadmap.Contains(
                "G9A3 = PASS — AUTHOR APPROVED") -and
            $roadmap.Contains(
                "G9A = PASS — AUTHOR APPROVED")) `
        -Message "Living G9A3 roadmap status is inconsistent."
}

function Assert-Evidence {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    Assert-HashManifest -RelativePath $EvidenceHashPath `
        -ExpectedTargets @($EvidencePath)
    Assert-Condition -Condition ($Evidence.phase -eq "G9A3" -and
            $Evidence.status -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.provenance.entrySha -eq $EntrySha -and
            $Evidence.provenance.branch -eq $ExpectedBranch -and
            $Evidence.provenance.canonicalPromptCanonicalLfSha256 -eq
                $PromptSha256 -and
            $Evidence.provenance.spatialSpecificationCanonicalLfSha256 -eq
                $SpecificationSha256 -and
            $Evidence.provenance.adr0010CanonicalLfSha256 -eq $Adr10Sha256 -and
            $Evidence.provenance.adr0011CanonicalLfSha256 -eq $Adr11Sha256 -and
            $Evidence.provenance.validationPlanCanonicalLfSha256 -eq
                $ValidationPlanSha256 -and
            $Evidence.provenance.g9a2TagObject -eq $G9A2TagObject -and
            $Evidence.provenance.g9a2PeeledCommit -eq $G9A2Commit) `
        -Message "G9A3 evidence provenance/status is inconsistent."
    Assert-Condition -Condition (-not [bool]$Evidence.approval.selfApproved -and
            -not [bool]$Evidence.approval.authorApproved -and
            -not [bool]$Evidence.approval.passClaimed -and
            [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PENDING_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.G9A2 -eq "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.G9A3 -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.G9A -eq
                "NOT_CLOSED_PENDING_G9A3_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.laterG9Phases -eq
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $Evidence.phaseDisposition.productiveG10 -eq
                "NOT_AUTHORIZED_NOT_STARTED") `
        -Message "G9A3 evidence claims ungranted approval or later-phase authority."

    Assert-SourceBoundary -Evidence $Evidence -ChangedPaths $ChangedPaths
    Assert-Condition -Condition (
            $Evidence.scenarioAuthority.path -eq $ScenarioPath -and
            $Evidence.scenarioAuthority.status -eq
                "IMPLEMENTATION_CANDIDATE_SCENARIOS_FROZEN" -and
            [int]$Evidence.scenarioAuthority.scenarioCount -eq 72 -and
            $Evidence.scenarioAuthority.sourceMethodMapping -eq
                "EXACT_ONE_TO_ONE_BY_CASE_PREFIX_AND_NUMBER" -and
            $Evidence.compatibilityCorpus.path -eq $CorpusPath -and
            $Evidence.compatibilityCorpus.manifestPath -eq $CorpusHashPath -and
            $Evidence.compatibilityCorpus.status -eq
                "IMPLEMENTATION_CANDIDATE_CORPUS_FROZEN" -and
            [int]$Evidence.compatibilityCorpus.entryCount -eq 13 -and
            $Evidence.compatibilityCorpus.corpusCanonicalLfSha256 -eq
                (Get-CanonicalTextSha256 -RelativePath $CorpusPath) -and
            $Evidence.compatibilityCorpus.externalUpstreamRuntimeEvidence -eq
                "NOT_EXECUTED_OR_CLAIMED" -and
            -not [bool]$Evidence.compatibilityCorpus.lossyConversionAuthorized) `
        -Message "G9A3 scenario/corpus evidence is inconsistent."

    $classes = @($Evidence.tests.focused.classes)
    Assert-ExactSet -Actual @($classes | ForEach-Object { $_.name }) `
        -Expected @($ExpectedG9A3TestCounts.Keys) `
        -Description "G9A3 evidence test-class set"
    foreach ($class in $classes) {
        Assert-Condition -Condition ([int]$class.tests -eq
                [int]$ExpectedG9A3TestCounts[$class.name]) `
            -Message "G9A3 evidence test count drifted for $($class.name)."
    }
    Assert-Condition -Condition (
            [int]$Evidence.tests.focused.expected.tests -eq 72 -and
            [int]$Evidence.tests.focused.expected.failures -eq 0 -and
            [int]$Evidence.tests.focused.expected.errors -eq 0 -and
            [int]$Evidence.tests.focused.expected.skipped -eq 0 -and
            [int]$Evidence.tests.inheritedRegression.expectedTests -eq 181 -and
            [int]$Evidence.tests.combined.expectedTests -eq 253) `
        -Message "G9A3 expected test totals are inconsistent."
    foreach ($executionStatus in @(
            $Evidence.tests.focused.executionStatus,
            $Evidence.tests.inheritedRegression.executionStatus,
            $Evidence.tests.combined.executionStatus)) {
        Assert-Condition -Condition ($executionStatus -in @(
                "PENDING_FINAL_VERIFICATION", "PASSED")) `
            -Message "Invalid G9A3 execution disposition: $executionStatus"
    }
    if ($Evidence.tests.focused.executionStatus -eq "PASSED") {
        Assert-Condition -Condition (
                [int]$Evidence.tests.focused.observed.tests -eq 72 -and
                [int]$Evidence.tests.focused.observed.failures -eq 0 -and
                [int]$Evidence.tests.focused.observed.errors -eq 0 -and
                [int]$Evidence.tests.focused.observed.skipped -eq 0) `
            -Message "Passed G9A3 focused evidence has incomplete totals."
    } else {
        Assert-Condition -Condition ($null -eq
                $Evidence.tests.focused.observed.tests) `
            -Message "Pending G9A3 focused evidence claims observed totals."
    }
    if ($Evidence.tests.inheritedRegression.executionStatus -eq "PASSED") {
        Assert-Condition -Condition (
                [int]$Evidence.tests.inheritedRegression.observedTests -eq 181) `
            -Message "Passed inherited G9A regression evidence drifted."
    } else {
        Assert-Condition -Condition ($null -eq
                $Evidence.tests.inheritedRegression.observedTests) `
            -Message "Pending inherited G9A evidence claims observed totals."
    }
    if ($Evidence.tests.combined.executionStatus -eq "PASSED") {
        Assert-Condition -Condition ([int]$Evidence.tests.combined.observedTests `
                -eq 253) `
            -Message "Passed combined G9A evidence drifted."
    } else {
        Assert-Condition -Condition ($null -eq
                $Evidence.tests.combined.observedTests) `
            -Message "Pending combined G9A evidence claims observed totals."
    }

    $hardZeroProperties = @($Evidence.requiredHardZeroCounters.PSObject.Properties)
    Assert-ExactSet -Actual @($hardZeroProperties | ForEach-Object { $_.Name }) `
        -Expected $RequiredHardZeroCounterNames `
        -Description "G9A3 hard-zero counter set"
    foreach ($property in $hardZeroProperties) {
        Assert-Condition -Condition ([int64]$property.Value -eq 0) `
            -Message "G9A3 forbidden-authority requirement is nonzero: $($property.Name)"
    }
    $scopeProperties = @($Evidence.scopeAudit.PSObject.Properties)
    Assert-ExactSet -Actual @($scopeProperties | ForEach-Object { $_.Name }) `
        -Expected $RequiredZeroScopeCounters `
        -Description "G9A3 forbidden-scope counter set"
    foreach ($property in $scopeProperties) {
        Assert-Condition -Condition ([int64]$property.Value -eq 0) `
            -Message "G9A3 forbidden-scope counter is nonzero: $($property.Name)"
    }
    Assert-Condition -Condition (
            [bool]$Evidence.semanticCoverage.pointOnly -and
            [bool]$Evidence.semanticCoverage.atomicLifecycleMutation -and
            [bool]$Evidence.semanticCoverage.completeCopyClosure -and
            [bool]$Evidence.semanticCoverage.explicitCompatibleRedefine -and
            [bool]$Evidence.semanticCoverage.freshIncompatibleReplacement -and
            [bool]$Evidence.semanticCoverage.deleteRecreateFreshIdentity -and
            [bool]$Evidence.semanticCoverage.snapshotIdentityGraphRestoration -and
            [bool]$Evidence.semanticCoverage.explicitMigrationOnly -and
            [bool]$Evidence.semanticCoverage.legacyUnassociatedByDefault -and
            [bool]$Evidence.semanticCoverage.malformedReferenceDiagnostics -and
            [bool]$Evidence.semanticCoverage.oneWayNonAuthoritativeDerivedAdapter -and
            [bool]$Evidence.documentation.userGuideReviewed -and
            -not [bool]$Evidence.documentation.userGuideChanged -and
            @($Evidence.scopeDeviations).Count -eq 0) `
        -Message "G9A3 semantic/documentation evidence is incomplete."

    $savedExecutions = @(
        @($Evidence.savedExecutions.focusedFinal,
            (".\tools\agent\verify-g9a3-spatial-lifecycle.ps1 " +
                "-KeepBuildOutputs -LogDirectory " +
                "artifacts\g9a3\candidate\focused-final-green"),
            "artifacts/g9a3/candidate/focused-final-green"),
        @($Evidence.savedExecutions.focusedDeterministicRerun,
            (".\tools\agent\verify-g9a3-spatial-lifecycle.ps1 " +
                "-KeepBuildOutputs -LogDirectory " +
                "artifacts\g9a3\candidate\focused-deterministic-green"),
            "artifacts/g9a3/candidate/focused-deterministic-green"),
        @($Evidence.savedExecutions.composedWithoutSkipBuild,
            (".\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory " +
                "artifacts\g9a3\candidate\composed-final-pass"),
            "artifacts/g9a3/candidate/composed-final-pass")
    )
    foreach ($saved in $savedExecutions) {
        $execution = $saved[0]
        Assert-Condition -Condition ($execution.command -eq $saved[1] -and
                $execution.logDirectory -eq $saved[2]) `
            -Message "G9A3 saved-execution command/log authority drifted."
        Assert-Condition -Condition ($execution.status -in @("PENDING", "PASSED")) `
            -Message "Invalid G9A3 saved-execution status: $($execution.status)"
        if ($execution.status -eq "PASSED") {
            Assert-Condition -Condition ([int]$execution.exitCode -eq 0) `
                -Message "Passed G9A3 execution has a nonzero exit code."
        } else {
            Assert-Condition -Condition ($null -eq $execution.exitCode) `
                -Message "Pending G9A3 execution has an exit-code claim."
        }
    }

    $focusedExecutions = @(
        $Evidence.savedExecutions.focusedFinal,
        $Evidence.savedExecutions.focusedDeterministicRerun
    )
    foreach ($execution in $focusedExecutions) {
        Assert-Condition -Condition ($execution.status -eq "PASSED" -and
                [int]$execution.exitCode -eq 0 -and
                $execution.buildStatus -eq "BUILD_SUCCESSFUL" -and
                [int]$execution.g9a3Tests -eq 72 -and
                [int]$execution.inheritedRegressionTests -eq 181 -and
                [int]$execution.combinedTests -eq 253 -and
                [int]$execution.testFailures -eq 0 -and
                [int]$execution.testErrors -eq 0 -and
                [int]$execution.testSkipped -eq 0 -and
                $execution.mainCheckstyle -eq "CLEAN" -and
                $execution.testCheckstyle -eq "CLEAN" -and
                $execution.verifierOutcome -eq "PASSED_CANDIDATE_ONLY") `
            -Message "G9A3 focused final execution evidence is incomplete."
    }
    Assert-Condition -Condition (
            [bool]$Evidence.savedExecutions.focusedDeterministicRerun.matchesFocusedFinal) `
        -Message "G9A3 deterministic focused-rerun equality is not recorded."

    $composedExecution = $Evidence.savedExecutions.composedWithoutSkipBuild
    Assert-Condition -Condition ($composedExecution.status -eq "PASSED" -and
            [int]$composedExecution.exitCode -eq 0 -and
            $composedExecution.terminalOutcome -eq
                "ALL_GEOCEDG_VERIFICATION_GATES_PASSED" -and
            $composedExecution.g9a3BuildStatus -eq "BUILD_SUCCESSFUL" -and
            [int]$composedExecution.g9a3Tests -eq 72 -and
            [int]$composedExecution.inheritedRegressionTests -eq 181 -and
            [int]$composedExecution.combinedTests -eq 253 -and
            [int]$composedExecution.testFailures -eq 0 -and
            [int]$composedExecution.testErrors -eq 0 -and
            [int]$composedExecution.testSkipped -eq 0 -and
            $composedExecution.mainCheckstyle -eq "CLEAN" -and
            $composedExecution.testCheckstyle -eq "CLEAN" -and
            $composedExecution.verifierOutcome -eq "PASSED_CANDIDATE_ONLY") `
        -Message "G9A3 composed final execution evidence is incomplete."

    $diagnostics = @($Evidence.nonAuthoritativeDiagnostics)
    Assert-Condition -Condition ($diagnostics.Count -eq 4) `
        -Message "G9A3 non-authoritative diagnostic inventory drifted."
    $environmentDiagnostic = $diagnostics | Where-Object {
        $_.id -eq "FOCUSED_FINAL_ENVIRONMENT"
    }
    Assert-Condition -Condition ($null -ne $environmentDiagnostic -and
            $environmentDiagnostic.status -eq
                "ENVIRONMENT_FAILURE_BEFORE_TESTS_NONFINAL" -and
            $environmentDiagnostic.command -eq
                (".\tools\agent\verify-g9a3-spatial-lifecycle.ps1 " +
                    "-KeepBuildOutputs -LogDirectory " +
                    "artifacts\g9a3\candidate\focused-final") -and
            $environmentDiagnostic.logDirectory -eq
                "artifacts/g9a3/candidate/focused-final" -and
            [int]$environmentDiagnostic.verifierExitCode -eq 1 -and
            -not [bool]$environmentDiagnostic.testsExecuted -and
            $environmentDiagnostic.classification -eq
                "SANDBOX_APPDATA_KOTLIN_GENERATED_DEPENDENCY_STATE" -and
            -not [bool]$environmentDiagnostic.productFailure -and
            -not [bool]$environmentDiagnostic.finalAuthority) `
        -Message "G9A3 environment diagnostic is inconsistent."

    $checkstyleDiagnostic = $diagnostics | Where-Object {
        $_.id -eq "FOCUSED_FINAL_PRECLEAN_CHECKSTYLE"
    }
    Assert-Condition -Condition (
            $null -ne $checkstyleDiagnostic -and
            $checkstyleDiagnostic.status -eq
                "TESTS_PASSED_CHECKSTYLE_FAILED_NONFINAL" -and
            $checkstyleDiagnostic.command -eq
                (".\tools\agent\verify-g9a3-spatial-lifecycle.ps1 " +
                    "-KeepBuildOutputs -LogDirectory " +
                    "artifacts\g9a3\candidate\focused-final-escalated") -and
            $checkstyleDiagnostic.logDirectory -eq
                "artifacts/g9a3/candidate/focused-final-escalated" -and
            [int]$checkstyleDiagnostic.g9a3Tests -eq 72 -and
            [int]$checkstyleDiagnostic.inheritedRegressionTests -eq 181 -and
            [int]$checkstyleDiagnostic.combinedTests -eq 253 -and
            [int]$checkstyleDiagnostic.testFailures -eq 0 -and
            [int]$checkstyleDiagnostic.testErrors -eq 0 -and
            [int]$checkstyleDiagnostic.testSkipped -eq 0 -and
            [int]$checkstyleDiagnostic.checkstyleWarnings -eq 48 -and
            [int]$checkstyleDiagnostic.verifierExitCode -eq 1 -and
            $checkstyleDiagnostic.blockingGate -eq "CHECKSTYLE" -and
            -not [bool]$checkstyleDiagnostic.productFailure -and
            -not [bool]$checkstyleDiagnostic.finalAuthority -and
            $checkstyleDiagnostic.disposition -eq
                "DIAGNOSTIC_ONLY_SUPERSEDED_BY_CLEAN_FOCUSED_FINAL_RUNS") `
        -Message "G9A3 pre-final Checkstyle diagnostic is inconsistent."

    $crlfDiagnostic = $diagnostics | Where-Object {
        $_.id -eq "COMPOSED_FINAL_G9A2_CRLF_FALSE_STALENESS"
    }
    Assert-Condition -Condition ($null -ne $crlfDiagnostic -and
            $crlfDiagnostic.status -eq
                "G9A2_GENERATED_EVIDENCE_FALSE_STALENESS_NONFINAL" -and
            $crlfDiagnostic.command -eq
                (".\tools\agent\verify.ps1 -KeepBuildOutputs " +
                    "-LogDirectory artifacts\g9a3\candidate\composed-final") -and
            $crlfDiagnostic.logDirectory -eq
                "artifacts/g9a3/candidate/composed-final" -and
            [int]$crlfDiagnostic.verifierExitCode -eq 1 -and
            $crlfDiagnostic.blockingGate -eq "G9A2_GENERATED_EVIDENCE_CHECK" -and
            $crlfDiagnostic.classification -eq
                "CORE_AUTOCRLF_CRLF_MATERIALIZATION" -and
            -not [bool]$crlfDiagnostic.g9a3Reached -and
            -not [bool]$crlfDiagnostic.productFailure -and
            -not [bool]$crlfDiagnostic.finalAuthority -and
            $crlfDiagnostic.disposition -eq
                "DIAGNOSTIC_ONLY_TRACKED_BLOBS_AND_CANONICAL_LF_OUTPUTS_EXACT") `
        -Message "G9A3 composed CRLF diagnostic is inconsistent."

    $pycacheDiagnostic = $diagnostics | Where-Object {
        $_.id -eq "COMPOSED_FINAL_UNEXPECTED_PYCACHE"
    }
    Assert-Condition -Condition ($null -ne $pycacheDiagnostic -and
            $pycacheDiagnostic.status -eq
                "UNEXPECTED_DIAGNOSTIC_ARTIFACT_NONFINAL" -and
            $pycacheDiagnostic.command -eq
                (".\tools\agent\verify.ps1 -KeepBuildOutputs " +
                    "-LogDirectory artifacts\g9a3\candidate\composed-final-green") -and
            $pycacheDiagnostic.logDirectory -eq
                "artifacts/g9a3/candidate/composed-final-green" -and
            [int]$pycacheDiagnostic.verifierExitCode -eq 1 -and
            $pycacheDiagnostic.unexpectedDiagnostic -eq "__pycache__" -and
            -not [bool]$pycacheDiagnostic.productFailure -and
            -not [bool]$pycacheDiagnostic.finalAuthority -and
            $pycacheDiagnostic.disposition -eq
                "DIAGNOSTIC_ONLY_SUPERSEDED_BY_COMPOSED_FINAL_PASS") `
        -Message "G9A3 composed __pycache__ diagnostic is inconsistent."
}

function Invoke-G9A3Tests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test",
        "--tests", "org.geocedg.common.spatial.G9A1*",
        "--tests", "org.geocedg.common.spatial.G9A2*",
        "--tests", "org.geocedg.common.spatial.G9A3*",
        "--tests", "org.geogebra.common.kernel.commands.RedefineTest",
        ":shared:common:checkstyleMain",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain",
        "--no-problems-report")
    $logPath = Join-Path $LogDirectory "g9a3-spatial-lifecycle-tests.log"
    $exitCode = $null
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($null -ne $exitCode -and $exitCode -eq 0) `
        -Message "G9A3 Gradle gate failed. See $logPath"
}

function Get-CleanTestTotal {
    param([Parameter(Mandatory)] [string]$ClassPattern)

    $total = 0
    foreach ($path in @(Get-ChildItem -LiteralPath $TestResultRoot `
            -Filter "TEST-${ClassPattern}.xml" -File)) {
        [xml]$result = Get-Content -Raw -LiteralPath $path.FullName
        $suite = $result.testsuite
        Assert-Condition -Condition ([int]$suite.failures -eq 0 -and
                [int]$suite.errors -eq 0 -and [int]$suite.skipped -eq 0) `
            -Message "JUnit result is not clean: $($path.FullName)"
        $total += [int]$suite.tests
    }
    return $total
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Resolve-RequiredFile -RelativePath (
        "source/shared/common-jre/build/test-results/test/TEST-${ClassName}.xml")
    [xml]$result = Get-Content -Raw -LiteralPath $path
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "G9A3 JUnit result drifted for $ClassName."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G9A3 Checkstyle result contains violations: $RelativePath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g9a3"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($required in $RequiredPaths) {
        [void](Resolve-RequiredFile -RelativePath $required)
    }
    Assert-TagAnchor -Name $G9A2TagName -Object $G9A2TagObject `
        -Commit $G9A2Commit
    foreach ($authority in @(
            @($PromptPath, $PromptSha256, "Canonical G9A3 prompt"),
            @($SpecificationPath, $SpecificationSha256, "Spatial specification"),
            @($Adr10Path, $Adr10Sha256, "Accepted ADR 0010"),
            @($Adr11Path, $Adr11Sha256, "Accepted ADR 0011"),
            @($ValidationPlanPath, $ValidationPlanSha256,
                "Spatial validation plan"))) {
        Assert-Condition -Condition ((Get-CanonicalTextSha256 `
            -RelativePath $authority[0]) -eq $authority[1]) `
            -Message "$($authority[2]) LF SHA-256 mismatch."
    }

    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    & git -C $RepositoryRoot show-ref --verify --quiet "refs/tags/$G9A3TagName"
    $g9a3TagExists = $LASTEXITCODE -eq 0
    Assert-Condition -Condition ($g9a3TagExists -or $LASTEXITCODE -eq 1) `
        -Message "Unable to resolve the G9A3 completion tag."
    if ($g9a3TagExists) {
        $tagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$G9A3TagName").Trim()
        Assert-Condition -Condition ((& git -C $RepositoryRoot cat-file -t `
                    $tagObject).Trim() -eq "tag") `
            -Message "The G9A3 completion tag is not annotated."
        $AuthorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$tagObject^{}").Trim()
        & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha $AuthorityCommit
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "G9A3 completion does not descend from its entry baseline."
        & git -C $RepositoryRoot merge-base --is-ancestor $AuthorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not descend from G9A3 completion."
    } else {
        $main = (& git -C $RepositoryRoot rev-parse main).Trim()
        $originMain = (& git -C $RepositoryRoot rev-parse origin/main).Trim()
        $upstream = (& git -C $RepositoryRoot rev-parse --abbrev-ref `
            --symbolic-full-name "@{upstream}").Trim()
        $divergence = (& git -C $RepositoryRoot rev-list --left-right --count `
            "@{upstream}...HEAD").Trim()
        Assert-Condition -Condition ($branch -eq $ExpectedBranch -and
                $head -eq $EntrySha -and $main -eq $EntrySha -and
                $originMain -eq $EntrySha -and
                $upstream -eq "origin/$ExpectedBranch" -and
                $divergence -eq "0`t0") `
            -Message "G9A3 branch/HEAD/main/origin/upstream entry authority drifted."
    }

    Assert-ScenarioAuthority
    Assert-CompatibilityCorpus
    Assert-ReportStatus
    $changedPaths = @(Get-CandidateChangedPaths)
    Assert-BootstrapScope -ChangedPaths $changedPaths
    $evidence = Read-JsonFile -RelativePath $EvidencePath
    Assert-Evidence -Evidence $evidence -ChangedPaths $changedPaths

    if (-not $SkipBuild) {
        Invoke-G9A3Tests
        $g9a1 = Get-CleanTestTotal `
            -ClassPattern "org.geocedg.common.spatial.G9A1*"
        $g9a2 = Get-CleanTestTotal `
            -ClassPattern "org.geocedg.common.spatial.G9A2*"
        $g9a3 = Get-CleanTestTotal `
            -ClassPattern "org.geocedg.common.spatial.G9A3*"
        $redefine = Get-CleanTestTotal `
            -ClassPattern "org.geogebra.common.kernel.commands.RedefineTest"
        Assert-Condition -Condition (($g9a1 + $g9a2 + $redefine) -eq 181) `
            -Message "Frozen inherited G9A regression total drifted."
        Assert-Condition -Condition ($g9a3 -eq 72 -and
                ($g9a1 + $g9a2 + $redefine + $g9a3) -eq 253) `
            -Message "Frozen G9A3 or combined test total drifted."
        foreach ($className in @($ExpectedG9A3TestCounts.Keys)) {
            Assert-TestResult -ClassName $className `
                -ExpectedTests ([int]$ExpectedG9A3TestCounts[$className])
        }
        $actualG9A3Results = @(Get-ChildItem -LiteralPath $TestResultRoot `
            -Filter "TEST-org.geocedg.common.spatial.G9A3*.xml" -File |
            ForEach-Object { $_.BaseName.Substring(5) })
        Assert-ExactSet -Actual $actualG9A3Results `
            -Expected @($ExpectedG9A3TestCounts.Keys) `
            -Description "G9A3 focused JUnit class set"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
        Write-Host ("Focused totals: inherited={0}; G9A3={1}; combined={2}." -f
            ($g9a1 + $g9a2 + $redefine), $g9a3,
            ($g9a1 + $g9a2 + $redefine + $g9a3))
    } else {
        Write-Host "Skipping G9A3 Gradle tests because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9A3."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9A3."
    Write-Host "G9A3 author-approved closeout verification passed (72 G9A3; 253 combined expected tests)."
    Write-Host "G9A3 = PASS — AUTHOR APPROVED."
    Write-Host "G9A = PASS — AUTHOR APPROVED."
    Write-Host "Later G9/G10 implementation remains governed by separate author decisions."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error "G9A3 focused verification failed: $($_.Exception.Message)"
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G9A3 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G9A3 verification."
            exit 1
        }
    }
}

exit 0

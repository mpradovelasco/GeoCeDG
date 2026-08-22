[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$VerifyPackagingArtifacts,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [switch]$HistoricalRegressionsAlreadyRecorded,
    [string]$PackagingArtifactRoot,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r2-product-refinement")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "ce022b756b51fe12497e1932ba3ae58093dd1405"
$ExpectedBranch = "feature/g9u0-r2-product-refinement"
$PlanningTagName = "geocedg-g9u0-r2-planning-pass"
$PlanningTagObject = "40076933fe204f3e3f0ab23485b1e564b47f17e6"
$PromptPath = ".github/prompts/tasks/g9u0-r2-product-refinement.prompt.md"
$AdrPath = "docs/adr/0016-native-geocedg-document-identity.md"
$LocusSpecPath = "geocedg/specs/locus/locus-v2-presentation.md"
$DocumentSpecPath = "geocedg/specs/ui/native-document-identity.md"
$AuthorityBlobIds = [ordered]@{
    $PromptPath = "eeb750888c66a90bc7e26926d8367a9e81c07882"
    $AdrPath = "24ed10b62340fdf5ae36fd9f39b221080ba6d252"
    $LocusSpecPath = "5bf619c7048dbe6a9190846e00bc31318b0a17e9"
    $DocumentSpecPath = "c814aeea03ba0e5ed1602785529ef9901baabff3"
}
$EvidenceAuthorityBlobIds = [ordered]@{
    canonicalPrompt = $AuthorityBlobIds[$PromptPath]
    adr0016 = $AuthorityBlobIds[$AdrPath]
    locusPresentationSpecification = $AuthorityBlobIds[$LocusSpecPath]
    nativeDocumentSpecification = $AuthorityBlobIds[$DocumentSpecPath]
}
$ValidationRoot = "geocedg/validation/g9u0-r2"
$ScenarioPath = "$ValidationRoot/g9u0-r2-product-refinement-scenarios.json"
$EvidencePath = "$ValidationRoot/g9u0-r2-product-refinement-evidence.json"
$EvidenceHashPath = "$ValidationRoot/g9u0-r2-evidence.sha256"
$CorpusPath = "$ValidationRoot/g9u0-r2-document-compatibility-corpus.json"
$CorpusHashPath = "$ValidationRoot/g9u0-r2-document-compatibility-corpus.sha256"
$ArchitecturePath =
    "docs/architecture/g9u0_r2_product_refinement_implementation.md"
$ReportPath =
    "docs/validation/g9u0_r2_product_refinement_implementation_candidate_report.md"
$RoadmapPath = "docs/roadmap/geocedg_roadmap.md"
$DeveloperGuidePath = "docs/developer/geocedg_developer_guide.md"
$UserGuidePath = "docs/user/geocedg_user_guide.md"
$DocumentationArchitecturePath =
    "docs/architecture/geocedg_documentation_architecture.md"
$TraceabilityPath = "docs/validation/g9_documentation_bundle_traceability.md"
$PublicMatrixPath = "docs/validation/g9_public_workspace_validation_matrix.md"
$SpecificationIndexPath = "geocedg/specs/README.md"
$UpstreamImpactPath = "docs/upstream/modified-files.yml"
$PackagingVerifier = Join-Path $PSScriptRoot "verify-packaging.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
if ([string]::IsNullOrWhiteSpace($CanonicalSummaryPath)) {
    $CanonicalSummaryPath = Join-Path $LogDirectory "canonical-summary.json"
} else {
    $CanonicalSummaryPath = [IO.Path]::GetFullPath($CanonicalSummaryPath)
}
if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
    $CompareCanonicalSummaryPath =
        [IO.Path]::GetFullPath($CompareCanonicalSummaryPath)
    if ($CompareCanonicalSummaryPath.Equals($CanonicalSummaryPath,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Canonical and comparison summary paths must be distinct."
    }
}

. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

$ExpectedTestCounts = [ordered]@{
    "org.geocedg.common.locus.G9U0R2LocusPresentationTest" = 11
    "org.geocedg.common.locus.G9U0R2LocusRenderContinuityTest" = 4
    "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest" = 16
    "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest" = 5
    "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest" = 10
    "org.geocedg.desktop.GeoCeDGDocumentLifecycleTest" = 16
}
$R2TestClasses = @(
    "org.geocedg.common.locus.G9U0R2LocusPresentationTest",
    "org.geocedg.common.locus.G9U0R2LocusRenderContinuityTest",
    "org.geocedg.desktop.GeoCeDGDocumentLifecycleTest"
)
$SupportingPersistenceTestClasses = @(
    "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest",
    "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest",
    "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest"
)
$ExpectedSupportingCoverage = [ordered]@{
    "R2-D09" = @(
        "org.geocedg.desktop.GeoCeDGDocumentLifecycleTest#corruptNativeArchiveIsRejectedBeforeLiveLoad",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p15UnknownSemanticVersionFailsWithoutLegacyRepair",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p16CorruptOrPartialTokenLedgerFailsAtomically",
        "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest#xml06DuplicateNativeIdentityRejectsAtomicallyWithoutRemap",
        "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest#xml07MalformedRolesFamiliesAndSchemaRejectBeforePublication",
        "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest#xml08UnknownAttributesAndFutureVersionsFailWithoutDowngrade"
    )
    "R2-D10" = @(
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p01EveryPublicGeneratorSaveAndReopenRestoresExactDescriptor",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p05NativeSaveReopenSaveIsByteIdentical",
        "org.geocedg.common.locus.G9U0R2LocusPresentationTest#completePresentationPersistsAcrossTwoNativeXmlReopens",
        "org.geocedg.common.locus.G9U0R2LocusPresentationTest#everyOrdinaryLineTypePersistsWithoutChangingSubpaths"
    )
    "R2-D11" = @(
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p02MetricQuerySaveAndReopenRestoresRichParent",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p03EveryIntersectionFamilyReopensWithExactQueryAndLedger",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p04BoundAndTokenPointsSaveAndReopenWithExactAddresses",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p16CorruptOrPartialTokenLedgerFailsAtomically"
    )
    "R2-D12" = @(
        "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest#compat01GeoCeDGLoadsRecomputesSavesAndReopensNativePointExactly",
        "org.geocedg.common.spatial.G9A3SpatialCompatibilityXmlTest#xml10RepeatedReopenProducesOneCanonicalIdentityGraph"
    )
    "R2-D14" = @(
        "org.geocedg.desktop.GeoCeDGDocumentLifecycleTest#classicKeepsClassicDefaultWhileNativeExtensionIsSupported",
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p13GeoCeDGClassicPreservesNativeV2WithoutEnablingCreation",
        "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest#compat02ForkClassicUsesSameKernelForExactNativeRoundTrip",
        "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest#compat03ClassicCreationStaysDisabledWhileNativeDataIsPreserved"
    )
    "R2-D15" = @(
        "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest#p14ExternalUpstreamBoundaryNeverDowngradesToPolyline",
        "org.geocedg.common.spatial.G9A3SpatialNativeCompatibilityTest#compat04ExternalUpstreamBoundaryIsUnsupportedWithZeroDowngrade"
    )
}
$RequiredHardZeroNames = @(
    "genericPathAdditions",
    "parallelLocusStyleAuthorities",
    "semanticRevisionChangesFromPresentation",
    "crossingDrawableTopologyInputs",
    "serializedRenderCaches",
    "filenameSemanticIdentityInferences",
    "archiveFormatMigrations",
    "xmlAppCodeChanges",
    "ggbSourceOverwrites",
    "lossyDowngrades",
    "g9u1Implementation",
    "g9bImplementation",
    "g9cImplementation",
    "g9u2Implementation",
    "productiveG10Implementation",
    "generatedTrackedArtifacts"
)
$HistoricalVerifiers = [ordered]@{
    "R2-R01 G9U0-R1" = "verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1"
    "R2-R02 historical G9U0" = "verify-g9u0-locus-v2-public-surface.ps1"
    "R2-R03 G9X1" = "verify-g9x1-extended-dxf.ps1"
    "R2-R04 G5" = "verify-dxf.ps1"
    "R2-R05 relevant G9A" = "verify-g9a3-spatial-lifecycle.ps1"
    "R2-R06 legacy Locus" = "verify-locus-v2.ps1"
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

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)))
    $rootPrefix = $RepositoryRoot.TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes the repository: $RelativePath"
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9U0-R2 artifact is missing: $RelativePath"
    return $absolute
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $RelativePath) |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON: $($_.Exception.Message)"
    }
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $text = [IO.File]::ReadAllText((Resolve-RequiredFile `
        -RelativePath $RelativePath))
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $hash = [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes($canonical)))
    return $hash.ToLowerInvariant()
}

function Get-BinarySha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return (Get-FileHash -Algorithm SHA256 -LiteralPath (
        Resolve-RequiredFile -RelativePath $RelativePath)).Hash.ToLowerInvariant()
}

function Get-ByteArraySha256 {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $hash = [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($Bytes))
    return $hash.ToLowerInvariant()
}

function Get-CanonicalArchiveInventory {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $archive = [IO.Compression.ZipFile]::OpenRead(
        (Resolve-RequiredFile -RelativePath $RelativePath))
    try {
        $entries = @($archive.Entries | Sort-Object FullName -CaseSensitive |
            ForEach-Object {
                $stream = $_.Open()
                $memory = [IO.MemoryStream]::new()
                try {
                    $stream.CopyTo($memory)
                    [byte[]]$bytes = $memory.ToArray()
                } finally {
                    $memory.Dispose()
                    $stream.Dispose()
                }
                $record = [ordered]@{
                    name = $_.FullName
                    length = $bytes.Length
                    entrySha256 = Get-ByteArraySha256 -Bytes $bytes
                    canonicalXmlLfSha256 = $null
                }
                if ($_.FullName.EndsWith(".xml",
                        [StringComparison]::OrdinalIgnoreCase)) {
                    $xmlText = [Text.UTF8Encoding]::new(
                        $false, $true).GetString($bytes)
                    $null = [xml]$xmlText
                    $normalizedXml = $xmlText.Replace("`r`n", "`n").Replace(
                        "`r", "`n")
                    $record.canonicalXmlLfSha256 = Get-ByteArraySha256 -Bytes (
                        [Text.UTF8Encoding]::new($false).GetBytes($normalizedXml))
                }
                [pscustomobject]$record
            })
        return [ordered]@{
            path = $RelativePath
            sourceSha256 = Get-BinarySha256 -RelativePath $RelativePath
            entryCount = $entries.Count
            entries = $entries
        }
    } finally {
        $archive.Dispose()
    }
}

function Get-SortedUniqueStrings {
    param([AllowEmptyCollection()] [object[]]$Values)

    [string[]]$result = @($Values | ForEach-Object { [string]$_ } |
        Sort-Object -CaseSensitive -Unique)
    return $result
}

function Assert-ExactSet {
    param(
        [AllowEmptyCollection()] [string[]]$Actual,
        [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualSorted = @(Get-SortedUniqueStrings -Values $Actual)
    $expectedSorted = @(Get-SortedUniqueStrings -Values $Expected)
    $duplicates = @($Actual).Count - $actualSorted.Count
    $delta = @(Compare-Object -ReferenceObject $expectedSorted `
        -DifferenceObject $actualSorted -CaseSensitive)
    Assert-Condition -Condition ($duplicates -eq 0 -and $delta.Count -eq 0) `
        -Message ("$Description mismatch. Duplicates=$duplicates`n" +
            ($delta | Out-String))
}

function Assert-HashManifest {
    param(
        [Parameter(Mandatory)] [string]$ManifestPath,
        [Parameter(Mandatory)] [string[]]$ExpectedTargets
    )

    $records = @{}
    foreach ($line in Get-Content -LiteralPath (
            Resolve-RequiredFile -RelativePath $ManifestPath)) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        Assert-Condition -Condition ($line -match '^([0-9a-f]{64})  (.+)$') `
            -Message "Malformed SHA-256 line in ${ManifestPath}: $line"
        Assert-Condition -Condition (-not $records.ContainsKey($Matches[2])) `
            -Message "Duplicate SHA-256 target in ${ManifestPath}: $($Matches[2])"
        $records[$Matches[2]] = $Matches[1]
    }
    Assert-ExactSet -Actual @($records.Keys) -Expected $ExpectedTargets `
        -Description "$ManifestPath target"
    foreach ($target in $ExpectedTargets) {
        Assert-Condition -Condition (
                (Get-CanonicalLfSha256 -RelativePath $target) -ceq
                    $records[$target]) `
            -Message "Canonical-LF SHA-256 mismatch for $target"
    }
}

function Assert-PlanningAuthority {
    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$PlanningTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagObject -ceq $PlanningTagObject) `
        -Message "The G9U0-R2 planning tag object changed."
    $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagType -eq "tag") `
        -Message "$PlanningTagName is not an annotated tag."
    $peeled = (& git -C $RepositoryRoot rev-parse `
        "$PlanningTagName^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $peeled -ceq $EntrySha) `
        -Message "The planning tag no longer peels to the approved entry commit."
    $tagText = @(& git -C $RepositoryRoot cat-file tag $tagObject) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagText.Contains("G9U0-R2") -and
            $tagText.Contains("PASS — AUTHOR APPROVED")) `
        -Message "The planning tag lacks the approved disposition."

    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    $branch = ((@(& git -C $RepositoryRoot branch --show-current) -join "")).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $head -ceq $EntrySha -and $branch -ceq $ExpectedBranch) `
        -Message ("The uncommitted R2 candidate must remain on " +
            "$ExpectedBranch at $EntrySha.")
    $staged = @(& git -C $RepositoryRoot diff --cached --name-only --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $staged.Count -eq 0) `
        -Message "G9U0-R2 verification requires an empty index."

    foreach ($entry in $AuthorityBlobIds.GetEnumerator()) {
        [void](Resolve-RequiredFile -RelativePath $entry.Key)
        $taggedBlob = (& git -C $RepositoryRoot rev-parse `
            "$PlanningTagName`:$($entry.Key)").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $taggedBlob -ceq $entry.Value) `
            -Message "Planning authority blob drifted for $($entry.Key)."
        $currentBlob = (& git -C $RepositoryRoot hash-object -- `
            $entry.Key).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $currentBlob -ceq $entry.Value) `
            -Message "Current authority bytes differ for $($entry.Key)."
    }
}

function Get-CandidatePaths {
    $tracked = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked R2 candidate paths."
    $untracked = @(& git -C $RepositoryRoot ls-files --others `
        --exclude-standard)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked R2 candidate paths."
    $combined = @($tracked + $untracked)
    return @(Get-SortedUniqueStrings -Values @($combined |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        ForEach-Object { $_.Replace("\", "/") }))
}

function Get-TestSourcePath {
    param([Parameter(Mandatory)] [string]$ClassName)

    $root = if ($ClassName.StartsWith("org.geocedg.desktop.")) {
        "source/desktop/desktop/src/test/java"
    } else {
        "source/shared/common-jre/src/test/java"
    }
    return "$root/$($ClassName.Replace('.', '/')).java"
}

function Assert-ScenarioAuthority {
    param([Parameter(Mandatory)] [object]$Scenarios)

    Assert-Condition -Condition ($Scenarios.schemaVersion -eq 1 -and
            $Scenarios.phase -eq "G9U0-R2" -and
            $Scenarios.status -eq
                "IMPLEMENTATION_CANDIDATE_SCENARIOS_SOURCE_COMPLETE" -and
            -not [bool]$Scenarios.authorApprovalClaimed -and
            [bool]$Scenarios.countsFrozen) `
        -Message "G9U0-R2 scenario authority status is invalid."
    $expectedGroups = [ordered]@{
        "L-PRESENTATION" = 11
        "L-RENDER" = 4
        "D-DOCUMENT" = 16
        "D-PACKAGING" = 1
        "R-REGRESSION" = 7
    }
    $groups = @($Scenarios.groups)
    Assert-ExactSet -Actual @($groups | ForEach-Object { $_.id }) `
        -Expected @($expectedGroups.Keys) -Description "R2 scenario group"
    $allIds = @($groups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.id })
    $expectedIds = @(
        (1..15 | ForEach-Object { "R2-L{0:D2}" -f $_ })
        (1..17 | ForEach-Object { "R2-D{0:D2}" -f $_ })
        (1..7 | ForEach-Object { "R2-R{0:D2}" -f $_ })
    )
    Assert-ExactSet -Actual $allIds -Expected $expectedIds `
        -Description "R2 matrix scenario ID"
    foreach ($group in $groups) {
        Assert-Condition -Condition (@($group.cases).Count -eq
                [int]$expectedGroups[[string]$group.id]) `
            -Message "R2 scenario group count drifted: $($group.id)"
        foreach ($case in @($group.cases)) {
            Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace(
                    [string]$case.title)) `
                -Message "R2 scenario lacks a title: $($case.id)"
        }
        if ($group.executionKind -eq "JUNIT") {
            $sourcePath = Get-TestSourcePath -ClassName $group.testClass
            $content = Get-Content -Raw -LiteralPath (
                Resolve-RequiredFile -RelativePath $sourcePath)
            Assert-Condition -Condition ($content -notmatch
                    '(?im)^\s*@(Disabled|Ignore)\b|\b(TODO|FIXME)\b') `
                -Message "Focused R2 test source has a disabled/TODO seam: $sourcePath"
            foreach ($case in @($group.cases)) {
                $count = [regex]::Matches($content,
                    [regex]::Escape([string]$case.id)).Count
                Assert-Condition -Condition ($count -eq 1) `
                    -Message ("$($case.id) must occur once in $sourcePath; " +
                        "found $count.")
            }
        } elseif ($group.id -eq "D-PACKAGING") {
            $content = Get-Content -Raw -LiteralPath (
                Resolve-RequiredFile -RelativePath $group.source)
            Assert-Condition -Condition ([regex]::Matches($content,
                    [regex]::Escape("R2-D17")).Count -eq 1) `
                -Message "R2-D17 must occur once in the packaging verifier."
        } elseif ($group.id -eq "R-REGRESSION") {
            foreach ($case in @($group.cases)) {
                [void](Resolve-RequiredFile -RelativePath $case.verifier)
            }
        }
    }

    $coverageRows = @($Scenarios.supportingPersistenceCoverage)
    Assert-ExactSet -Actual @($coverageRows | ForEach-Object { $_.scenario }) `
        -Expected @($ExpectedSupportingCoverage.Keys) `
        -Description "R2 supporting-persistence scenario"
    foreach ($row in $coverageRows) {
        Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace(
                [string]$row.reason)) `
            -Message "Supporting coverage lacks a rationale: $($row.scenario)"
        $actualTokens = @($row.authorities | ForEach-Object {
                $testClass = [string]$_.testClass
                $sourcePath = Get-TestSourcePath -ClassName $testClass
                $content = Get-Content -Raw -LiteralPath (
                    Resolve-RequiredFile -RelativePath $sourcePath)
                foreach ($method in @($_.methods)) {
                    $methodName = [string]$method
                    $methodCount = [regex]::Matches($content,
                        "\bvoid\s+" + [regex]::Escape($methodName) +
                            "\s*\(").Count
                    Assert-Condition -Condition ($methodCount -eq 1) `
                        -Message ("Supporting method $testClass#$methodName " +
                            "must occur once; found $methodCount.")
                    "$testClass#$methodName"
                }
            })
        Assert-ExactSet -Actual $actualTokens `
            -Expected @($ExpectedSupportingCoverage[[string]$row.scenario]) `
            -Description "Supporting coverage for $($row.scenario)"
    }
    Assert-Condition -Condition (
            $Scenarios.expectedCounts.locus -eq 15 -and
            $Scenarios.expectedCounts.document -eq 17 -and
            $Scenarios.expectedCounts.regression -eq 7 -and
            $Scenarios.expectedCounts.matrixTotal -eq 39 -and
            $Scenarios.expectedCounts.sharedJUnit -eq 15 -and
            $Scenarios.expectedCounts.desktopJUnit -eq 16 -and
            $Scenarios.expectedCounts.focusedJUnitTotal -eq 31 -and
            $Scenarios.expectedCounts.supportingPersistenceJUnit -eq 31 -and
            $Scenarios.expectedCounts.packagingProbe -eq 1) `
        -Message "R2 scenario totals are not frozen at 15/17/7 = 39."
    Assert-Condition -Condition ($Scenarios.testExecution.status -in
            @("PENDING", "PASSED")) `
        -Message "R2 scenario execution status is invalid."
    if ($Scenarios.testExecution.status -eq "PASSED") {
        Assert-Condition -Condition (
                $Scenarios.testExecution.sharedJUnit -eq 15 -and
                $Scenarios.testExecution.desktopJUnit -eq 16 -and
                $Scenarios.testExecution.focusedJUnitTotal -eq 31 -and
                $Scenarios.testExecution.supportingPersistenceJUnit -eq 31 -and
                $Scenarios.testExecution.packagingProbe -eq 1 -and
                $Scenarios.testExecution.failures -eq 0 -and
                $Scenarios.testExecution.errors -eq 0 -and
                $Scenarios.testExecution.skipped -eq 0) `
            -Message "Recorded R2 scenario execution is not clean 31 + 1."
    }
}

function Assert-EvidenceContract {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [object]$Scenarios
    )

    Assert-Condition -Condition ($Evidence.schemaVersion -eq 1 -and
            $Evidence.phase -eq "G9U0-R2" -and
            $Evidence.status -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            [bool]$Evidence.implementationStarted) `
        -Message "R2 evidence is not an implementation candidate."
    Assert-Condition -Condition (-not [bool]$Evidence.approval.selfApproved -and
            -not [bool]$Evidence.approval.authorApproved -and
            -not [bool]$Evidence.approval.passClaimed -and
            [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PENDING_AUTHOR_REVIEW") `
        -Message "R2 evidence contains an approval or PASS claim."
    Assert-Condition -Condition (
            $Evidence.provenance.entrySha -eq $EntrySha -and
            $Evidence.provenance.branch -eq $ExpectedBranch -and
            $Evidence.provenance.planningTag -eq $PlanningTagName -and
            $Evidence.provenance.planningTagObject -eq $PlanningTagObject -and
            $Evidence.provenance.planningTagPeeledCommit -eq $EntrySha) `
        -Message "R2 evidence provenance is inconsistent."
    $recordedAuthority = $Evidence.provenance.authorityBlobIds
    Assert-ExactSet -Actual @($recordedAuthority.PSObject.Properties.Name) `
        -Expected @($EvidenceAuthorityBlobIds.Keys) `
        -Description "R2 evidence authority blob name"
    foreach ($entry in $EvidenceAuthorityBlobIds.GetEnumerator()) {
        Assert-Condition -Condition (
                [string]$recordedAuthority.($entry.Key) -ceq $entry.Value) `
            -Message "R2 evidence authority blob drifted: $($entry.Key)"
    }
    Assert-Condition -Condition (
            $Evidence.phaseDisposition.G9U0R2Planning -eq
                "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.G9U0R2Implementation -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.phaseDisposition.G9U1 -eq "DESIGNED_NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9B -eq "NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9C -eq "NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9U2 -eq "BLOCKED" -and
            $Evidence.phaseDisposition.productiveG10 -eq "NOT_AUTHORIZED") `
        -Message "R2 phase disposition is inconsistent."
    Assert-Condition -Condition (
            [bool]$Evidence.architecture.ordinaryGeoElementStyleIsSoleAuthority -and
            -not [bool]$Evidence.architecture.genericPathAdded -and
            -not [bool]$Evidence.architecture.parallelStyleModelAdded -and
            -not [bool]$Evidence.architecture.presentationChangesSemanticRevision -and
            [bool]$Evidence.architecture.renderCacheRemainsDerived -and
            -not [bool]$Evidence.architecture.crossingObjectsAreRenderTopologyInputs -and
            $Evidence.architecture.nativeExtension -eq "cedg" -and
            $Evidence.architecture.compatibilityInputExtension -eq "ggb" -and
            -not [bool]$Evidence.architecture.archiveOrXmlFormatChanged -and
            $Evidence.architecture.xmlAppCode -eq "classic" -and
            -not [bool]$Evidence.architecture.filenameInfersSemanticIdentity -and
            -not [bool]$Evidence.architecture.lossyDowngradeAdded -and
            -not [bool]$Evidence.architecture.classicCreationEnabled -and
            [bool]$Evidence.architecture.secondLiveParseFailureRestoresLiveDocument -and
            [bool]$Evidence.architecture.undoBaselineCommitFailureRestoresLiveDocument) `
        -Message "R2 architecture evidence violates the accepted contract."
    Assert-Condition -Condition (
            $Evidence.scenarioAuthority.locus -eq 15 -and
            $Evidence.scenarioAuthority.document -eq 17 -and
            $Evidence.scenarioAuthority.regression -eq 7 -and
            $Evidence.scenarioAuthority.matrixTotal -eq 39 -and
            $Evidence.scenarioAuthority.focusedR2JUnit -eq 31 -and
            $Evidence.scenarioAuthority.supportingPersistenceJUnit -eq 31 -and
            $Evidence.scenarioAuthority.durableIdentityCorpusRegression -eq
                "R2-R05" -and
            $Evidence.scenarioAuthority.path -eq $ScenarioPath -and
            $Evidence.documentCompatibilityCorpus.path -eq $CorpusPath -and
            $Evidence.documentCompatibilityCorpus.manifestPath -eq
                $CorpusHashPath -and
            $Evidence.documentCompatibilityCorpus.entryCount -eq 8 -and
            -not [bool]$Evidence.documentCompatibilityCorpus.sourceBytesChanged) `
        -Message "R2 scenario/corpus evidence is inconsistent."
    Assert-ExactSet -Actual @(
            $Evidence.scenarioAuthority.supportingCoverageScenarios) `
        -Expected @($ExpectedSupportingCoverage.Keys) `
        -Description "R2 evidence supporting-coverage scenario"
    Assert-Condition -Condition (
            $Evidence.manualAuthorSmoke.status -eq "PENDING_AUTHOR" -and
            -not [bool]$Evidence.manualAuthorSmoke.passed -and
            [bool]$Evidence.manualAuthorSmoke.authorEvidenceRequired -and
            $Evidence.manualAuthorSmoke.checklistPath -eq $ReportPath -and
            $Evidence.manualAuthorSmoke.requiredSteps -eq 9 -and
            @($Evidence.manualAuthorSmoke.evidencePaths).Count -eq 0 -and
            $null -eq $Evidence.manualAuthorSmoke.completedBy -and
            $null -eq $Evidence.manualAuthorSmoke.completedAt) `
        -Message "Manual author smoke must remain pending."

    $inventoryStatus = [string]$Evidence.sourceBoundary.inventoryStatus
    Assert-Condition -Condition ($inventoryStatus -in @(
            "OPEN_PENDING_IMPLEMENTATION_FREEZE", "FROZEN")) `
        -Message "Unknown R2 source inventory state."
    if ($inventoryStatus -eq "OPEN_PENDING_IMPLEMENTATION_FREEZE") {
        Assert-Condition -Condition (
                @($Evidence.sourceBoundary.candidatePaths).Count -eq 0 -and
                $null -eq $Evidence.sourceBoundary.pathCount) `
            -Message "Open R2 inventory contains guessed candidate values."
    } else {
        Assert-Condition -Condition (
                $Evidence.sourceBoundary.frozenAgainst -eq $EntrySha -and
                $Evidence.sourceBoundary.pathCount -eq
                    @($Evidence.sourceBoundary.candidatePaths).Count) `
            -Message "Frozen R2 inventory count/base is inconsistent."
        $expectedPaths = @(Get-SortedUniqueStrings -Values @(
            $Evidence.sourceBoundary.candidatePaths))
        Assert-Condition -Condition ($expectedPaths.Count -eq
                @($Evidence.sourceBoundary.candidatePaths).Count) `
            -Message "Frozen R2 inventory contains duplicates."
        Assert-ExactSet -Actual @(Get-CandidatePaths) -Expected $expectedPaths `
            -Description "Exact R2 candidate path inventory"
        $categoryTotal = 0
        foreach ($property in
                $Evidence.sourceBoundary.categoryCounts.PSObject.Properties) {
            Assert-Condition -Condition ($null -ne $property.Value) `
                -Message "Frozen R2 category is null: $($property.Name)"
            $categoryTotal += [int]$property.Value
        }
        Assert-Condition -Condition ($categoryTotal -eq $expectedPaths.Count) `
            -Message "R2 inventory categories do not equal the path count."
    }

    $hardZeroStatus = [string]$Evidence.hardZeroCounters.status
    Assert-Condition -Condition ($hardZeroStatus -in @(
            "PENDING_VALIDATION", "FROZEN_ZERO")) `
        -Message "Unknown R2 hard-zero status."
    Assert-ExactSet -Actual @(
            $Evidence.hardZeroCounters.values.PSObject.Properties.Name) `
        -Expected $RequiredHardZeroNames -Description "R2 hard-zero counter"
    foreach ($property in $Evidence.hardZeroCounters.values.PSObject.Properties) {
        if ($hardZeroStatus -eq "FROZEN_ZERO") {
            Assert-Condition -Condition ($property.Value -eq 0) `
                -Message "R2 hard-zero counter is nonzero: $($property.Name)"
        } else {
            Assert-Condition -Condition ($null -eq $property.Value) `
                -Message "Pending R2 hard-zero counter is prematurely populated."
        }
    }

    $focusedStatus = [string]$Evidence.validation.focused.status
    Assert-Condition -Condition ($focusedStatus -in @("PENDING", "PASSED")) `
        -Message "Unknown R2 focused validation status."
    Assert-Condition -Condition ($Scenarios.testExecution.status -eq
            $focusedStatus) `
        -Message "R2 scenario and evidence focused statuses disagree."
    $staticScaffold = $Evidence.validation.staticScaffold
    $packagingStatic = $Evidence.validation.packagingStatic
    foreach ($record in @($staticScaffold, $packagingStatic,
            $Evidence.validation.gitDiffCheck,
            $Evidence.validation.gitDiffCachedCheck)) {
        Assert-Condition -Condition ($record.status -in @("PENDING", "PASSED")) `
            -Message "Unknown R2 simple validation-record status."
        if ($record.status -eq "PENDING") {
            Assert-Condition -Condition (
                    $null -eq $record.command -and
                    $null -eq $record.exitCode -and
                    $null -eq $record.logPath) `
                -Message "Pending R2 validation record contains result evidence."
        } else {
            Assert-Condition -Condition (
                    -not [string]::IsNullOrWhiteSpace([string]$record.command) -and
                    $record.exitCode -eq 0 -and
                    -not [string]::IsNullOrWhiteSpace([string]$record.logPath)) `
                -Message "Passed R2 validation record lacks command/exit/log evidence."
        }
    }

    $packagingArtifact = $Evidence.validation.packagingArtifactProbe
    Assert-Condition -Condition ($packagingArtifact.status -in @(
            "NOT_REQUESTED", "PASSED")) `
        -Message "Unknown R2 packaging-artifact status."
    if ($packagingArtifact.status -eq "NOT_REQUESTED") {
        Assert-Condition -Condition (
                $null -eq $packagingArtifact.command -and
                $null -eq $packagingArtifact.exitCode -and
                $null -eq $packagingArtifact.logPath -and
                $null -eq $packagingArtifact.artifactRoot) `
            -Message "Unrequested packaging probe contains result evidence."
    } else {
        Assert-Condition -Condition (
                -not [string]::IsNullOrWhiteSpace(
                    [string]$packagingArtifact.command) -and
                $packagingArtifact.exitCode -eq 0 -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$packagingArtifact.logPath) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$packagingArtifact.artifactRoot)) `
            -Message "Packaging artifact PASS lacks command/exit/log/root evidence."
    }

    $history = [ordered]@{
        "R2-R01" = $Evidence.validation.g9u0R1Regression
        "R2-R02" = $Evidence.validation.historicalG9U0Regression
        "R2-R03" = $Evidence.validation.g9x1Regression
        "R2-R04" = $Evidence.validation.g5Regression
        "R2-R05" = $Evidence.validation.g9aRegression
        "R2-R06" = $Evidence.validation.legacyLocusRegression
    }
    foreach ($entry in $history.GetEnumerator()) {
        $record = $entry.Value
        Assert-Condition -Condition (
                $record.scenario -eq $entry.Key -and
                $record.status -in @("PENDING", "PASSED")) `
            -Message "Invalid historical regression record: $($entry.Key)"
        if ($record.status -eq "PENDING") {
            Assert-Condition -Condition (
                    $null -eq $record.command -and
                    $null -eq $record.exitCode -and
                    $null -eq $record.tests -and
                    $null -eq $record.failures -and
                    $null -eq $record.errors -and
                    $null -eq $record.skipped -and
                    @($record.logPaths).Count -eq 0) `
                -Message "Pending historical record contains result evidence."
        } else {
            Assert-Condition -Condition (
                    -not [string]::IsNullOrWhiteSpace([string]$record.command) -and
                    $record.exitCode -eq 0 -and
                    $record.tests -ge 0 -and
                    $record.failures -eq 0 -and
                    $record.errors -eq 0 -and
                    $record.skipped -eq 0 -and
                    @($record.logPaths).Count -gt 0) `
                -Message "Historical PASS lacks command/count/exit/log evidence."
        }
    }

    $checkstyle = $Evidence.validation.checkstyle
    Assert-Condition -Condition ($checkstyle.status -in @("PENDING", "PASSED")) `
        -Message "Unknown R2 Checkstyle status."
    if ($checkstyle.status -eq "PENDING") {
        Assert-Condition -Condition (
                $null -eq $checkstyle.commands.shared -and
                $null -eq $checkstyle.commands.desktop -and
                $null -eq $checkstyle.exitCodes.shared -and
                $null -eq $checkstyle.exitCodes.desktop -and
                @($checkstyle.reportPaths).Count -eq 0 -and
                $null -eq $checkstyle.errorCount) `
            -Message "Pending Checkstyle record contains result evidence."
    } else {
        Assert-Condition -Condition (
                -not [string]::IsNullOrWhiteSpace(
                    [string]$checkstyle.commands.shared) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$checkstyle.commands.desktop) -and
                $checkstyle.exitCodes.shared -eq 0 -and
                $checkstyle.exitCodes.desktop -eq 0 -and
                @($checkstyle.reportPaths).Count -eq 4 -and
                $checkstyle.errorCount -eq 0) `
            -Message "Checkstyle PASS lacks command/exit/report evidence."
    }

    $deterministic = $Evidence.validation.focusedDeterministicRerun
    Assert-Condition -Condition ($deterministic.status -in @(
            "PENDING", "PASSED")) `
        -Message "Unknown R2 deterministic-rerun status."
    if ($deterministic.status -eq "PENDING") {
        Assert-Condition -Condition (
                $null -eq $deterministic.commands.shared -and
                $null -eq $deterministic.commands.desktop -and
                $null -eq $deterministic.exitCodes.shared -and
                $null -eq $deterministic.exitCodes.desktop -and
                $null -eq $deterministic.matchesFocused -and
                $null -eq $deterministic.sharedR2 -and
                $null -eq $deterministic.desktopR2 -and
                $null -eq $deterministic.r2Total -and
                $null -eq $deterministic.supportingPersistence -and
                $null -eq $deterministic.executedJUnitTotal -and
                $null -eq $deterministic.failures -and
                $null -eq $deterministic.errors -and
                $null -eq $deterministic.skipped -and
                @($deterministic.testResultPaths).Count -eq 0 -and
                $null -eq $deterministic.canonicalSummaryPath -and
                $null -eq $deterministic.canonicalSummarySha256 -and
                $null -eq $deterministic.comparedSummaryPath -and
                $null -eq $deterministic.comparedSummarySha256) `
            -Message "Pending deterministic record contains result evidence."
    }
    if ($focusedStatus -eq "PENDING") {
        Assert-Condition -Condition (
                $null -eq $Evidence.validation.focused.commands.shared -and
                $null -eq $Evidence.validation.focused.commands.desktop -and
                $null -eq $Evidence.validation.focused.exitCodes.shared -and
                $null -eq $Evidence.validation.focused.exitCodes.desktop -and
                @($Evidence.validation.focused.testResultPaths).Count -eq 0 -and
                $null -eq $Evidence.validation.focused.canonicalSummaryPath -and
                $null -eq $Evidence.validation.focused.canonicalSummarySha256 -and
                $Evidence.documentCompatibilityCorpus.sourceHashesVerified -eq
                    "PENDING" -and
                $null -eq $Evidence.documentCompatibilityCorpus.manifestCanonicalSha256 -and
                $deterministic.status -eq "PENDING") `
            -Message "Pending focused record contains result evidence."
    } else {
        Assert-Condition -Condition (
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.command) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.commands.shared) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.commands.desktop) -and
                $Evidence.validation.focused.exitCodes.shared -eq 0 -and
                $Evidence.validation.focused.exitCodes.desktop -eq 0 -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.logPaths.shared) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.logPaths.desktop) -and
                @($Evidence.validation.focused.testResultPaths).Count -eq 6 -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$Evidence.validation.focused.canonicalSummaryPath) -and
                [string]$Evidence.validation.focused.canonicalSummarySha256 -match
                    '^[0-9a-f]{64}$' -and
                $Evidence.documentCompatibilityCorpus.sourceHashesVerified -eq
                    "PASSED" -and
                [string]$Evidence.documentCompatibilityCorpus.manifestCanonicalSha256 -match
                    '^[0-9a-f]{64}$' -and
                $Evidence.validation.focused.sharedR2 -eq 15 -and
                $Evidence.validation.focused.desktopR2 -eq 16 -and
                $Evidence.validation.focused.r2Total -eq 31 -and
                $Evidence.validation.focused.supportingPersistence -eq 31 -and
                $Evidence.validation.focused.executedJUnitTotal -eq 62 -and
                $Evidence.validation.focused.failures -eq 0 -and
                $Evidence.validation.focused.errors -eq 0 -and
                $Evidence.validation.focused.skipped -eq 0 -and
                $deterministic.status -eq "PASSED" -and
                [bool]$deterministic.matchesFocused -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.command) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.commands.shared) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.commands.desktop) -and
                $deterministic.exitCodes.shared -eq 0 -and
                $deterministic.exitCodes.desktop -eq 0 -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.logPaths.shared) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.logPaths.desktop) -and
                $deterministic.sharedR2 -eq 15 -and
                $deterministic.desktopR2 -eq 16 -and
                $deterministic.r2Total -eq 31 -and
                $deterministic.supportingPersistence -eq 31 -and
                $deterministic.executedJUnitTotal -eq 62 -and
                $deterministic.failures -eq 0 -and
                $deterministic.errors -eq 0 -and
                $deterministic.skipped -eq 0 -and
                @($deterministic.testResultPaths).Count -eq 6 -and
                [string]$deterministic.canonicalSummarySha256 -match
                    '^[0-9a-f]{64}$' -and
                $deterministic.canonicalSummarySha256 -ceq
                    $Evidence.validation.focused.canonicalSummarySha256 -and
                $deterministic.comparedSummarySha256 -ceq
                    $Evidence.validation.focused.canonicalSummarySha256 -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.canonicalSummaryPath) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$deterministic.comparedSummaryPath) -and
                $Scenarios.testExecution.status -eq "PASSED") `
            -Message "R2 focused/deterministic validation tuple is incomplete."
    }

    $composed = $Evidence.validation.composedWithoutSkipBuild
    Assert-Condition -Condition ($composed.status -in @("PENDING", "PASSED")) `
        -Message "Unknown R2 composed-verification status."
    if ($composed.status -eq "PENDING") {
        Assert-Condition -Condition (
                $null -eq $composed.command -and
                $null -eq $composed.exitCode -and
                $null -eq $composed.terminalOutcome -and
                $null -eq $composed.logPath -and
                $null -eq $composed.logDirectory) `
            -Message "Pending composed record contains result evidence."
    } else {
        Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace([string]$composed.command) -and
                $composed.exitCode -eq 0 -and
                $composed.terminalOutcome -eq
                    "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
                -not [string]::IsNullOrWhiteSpace([string]$composed.logPath) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string]$composed.logDirectory) -and
                $focusedStatus -eq "PASSED" -and
                $deterministic.status -eq "PASSED" -and
                @($history.Values | Where-Object {
                        $_.status -ne "PASSED"
                    }).Count -eq 0 -and
                $staticScaffold.status -eq "PASSED" -and
                $packagingStatic.status -eq "PASSED" -and
                $checkstyle.status -eq "PASSED" -and
                $Evidence.validation.gitDiffCheck.status -eq "PASSED" -and
                $Evidence.validation.gitDiffCachedCheck.status -eq "PASSED" -and
                $hardZeroStatus -eq "FROZEN_ZERO") `
            -Message "Composed PASS lacks complete automated candidate evidence."
    }
}

function Assert-Corpus {
    $corpus = Read-JsonDocument -RelativePath $CorpusPath
    Assert-Condition -Condition ($corpus.schemaVersion -eq 1 -and
            $corpus.phase -eq "G9U0-R2" -and
            $corpus.sourceExtension -eq "ggb" -and
            $corpus.nativeTargetExtension -eq "cedg" -and
            @($corpus.entries).Count -eq 8 -and
            [bool]$corpus.invariants.sourceFilesAreReadOnlyEvidence -and
            [bool]$corpus.invariants.sourceBytesMustRemainUnchanged -and
            [bool]$corpus.invariants.extensionDoesNotMigrateSemantics -and
            [bool]$corpus.invariants.archiveAndXmlFormatRemainUnchanged -and
            $corpus.invariants.xmlAppCode -eq "classic" -and
            -not [bool]$corpus.invariants.externalUpstreamSupportClaimed) `
        -Message "R2 compatibility corpus contract is inconsistent."
    Assert-HashManifest -ManifestPath $CorpusHashPath `
        -ExpectedTargets @($CorpusPath)
    foreach ($entry in @($corpus.entries)) {
        $path = [string]$entry.path
        $absolute = Resolve-RequiredFile -RelativePath $path
        Assert-Condition -Condition ((Get-Item -LiteralPath $absolute).Length -eq
                [long]$entry.size) `
            -Message "R2 corpus size drifted: $path"
        Assert-Condition -Condition ((Get-BinarySha256 -RelativePath $path) -ceq
                [string]$entry.sha256) `
            -Message "R2 corpus SHA-256 drifted: $path"
    }
}

function Assert-SourceBoundary {
    param([Parameter(Mandatory)] [object]$Evidence)

    $candidatePaths = @(Get-CandidatePaths)
    foreach ($path in $candidatePaths) {
        Assert-Condition -Condition (
                $path -notmatch '(^|/)(artifacts|build|\.gradle|\.kotlin)(/|$)' -and
                $path -notmatch '(^|/)(source/web|python)(/|$)' -and
                $path -notmatch '(?i)(g9u1|g9b|g9c|g9u2|productive-g10)') `
            -Message "R2 candidate contains forbidden/generated scope: $path"
        Assert-Condition -Condition ($path -notin @(
                "source/shared/common/src/main/java/org/geogebra/common/kernel/Path.java",
                "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoLocus.java",
                "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java",
                "source/shared/common-jre/src/main/java/org/geogebra/common/jre/io/MyXMLioJre.java")) `
            -Message "R2 candidate changed a forbidden semantic/serializer authority: $path"
    }

    $trackedGenerated = @(& git -C $RepositoryRoot ls-files |
        Where-Object { $_ -match
            '(^|/)(artifacts|build|\.gradle|\.kotlin|__pycache__)(/|$)|\.pyc$' } |
        Where-Object { $_ -ne "artifacts/README.md" })
    Assert-Condition -Condition ($trackedGenerated.Count -eq 0) `
        -Message "Tracked generated artifacts detected: $($trackedGenerated -join ', ')"

    if ($Evidence.sourceBoundary.inventoryStatus -eq "FROZEN") {
        $impact = Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $UpstreamImpactPath)
        foreach ($sourcePath in @($candidatePaths | Where-Object {
                    $_.StartsWith("source/", [StringComparison]::Ordinal)
                })) {
            Assert-Condition -Condition ($impact.Contains($sourcePath)) `
                -Message "Upstream impact manifest omits R2 source path: $sourcePath"
        }
    }
}

function Assert-ProductStaticContracts {
    $locus = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java")
    Assert-Condition -Condition (
            $locus.Contains("boolean showLineProperties()") -and
            $locus.Contains("getLineStyleXML(builder)") -and
            $locus.Contains("void updatePresentationRepaint()") -and
            $locus -notmatch '(?s)class\s+GeoLocusV2[^\{]+\bPath\b') `
        -Message "GeoLocusV2 ordinary line capability/non-Path boundary drifted."

    $geoElement = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java")
    Assert-Condition -Condition (
            $geoElement.Contains("public void updatePresentationRepaint()") -and
            $geoElement -match
                '(?s)void\s+updatePresentationRepaint\s*\(\s*\).*?updateRepaint\s*\(\s*\)') `
        -Message "Ordinary GeoElement presentation refresh no longer preserves its default cascade."
    foreach ($propertyPath in @(
            "source/shared/common/src/main/java/org/geogebra/common/properties/impl/objects/CaptionProperty.java",
            "source/shared/common/src/main/java/org/geogebra/common/properties/impl/objects/CaptionStyleProperty.java",
            "source/shared/common/src/main/java/org/geogebra/common/properties/impl/objects/LabelProperty.java",
            "source/shared/common/src/main/java/org/geogebra/common/properties/impl/objects/ThicknessProperty.java")) {
        $propertySource = Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $propertyPath)
        Assert-Condition -Condition (
                $propertySource.Contains("updatePresentationRepaint();")) `
            -Message "Properties presentation hook is absent from $propertyPath."
    }

    $drawable = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/DrawLocusV2.java")
    Assert-Condition -Condition (
            $drawable.Contains("renderCache.getOrBuild(locus") -and
            $drawable.Contains("updateStrokes(locus)") -and
            $drawable.Contains("getObjectColor()") -and
            $drawable -notmatch '\bGeo(Line|Conic|Circle)\b') `
        -Message "DrawLocusV2 no longer has a derived/independent render boundary."

    $extensions = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geogebra/common/util/FileExtensions.java")
    Assert-Condition -Condition ($extensions.Contains('GEOCEDG("cedg"') -and
            $extensions.Contains('GEOGEBRA("ggb"')) `
        -Message "The native/compatibility extension enum is incomplete."

    $policy = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentPolicy.java")
    foreach ($fragment in @("requiresNativeSaveAs", "isCompatibilityInput",
            "normalizeNativeSuffix", "hasConflictingSuffix",
            "FileExtensions.GEOCEDG")) {
        Assert-Condition -Condition ($policy.Contains($fragment)) `
            -Message "Native document policy lacks $fragment."
    }

    $config = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/main/settings/config/AppConfigGeoCeDG.java")
    $constants = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geogebra/common/GeoGebraConstants.java")
    Assert-Condition -Condition (
            $config.Contains("return GeoGebraConstants.CLASSIC_APPCODE;") -and
            $constants -match 'CLASSIC_APPCODE\s*=\s*"classic"') `
        -Message "G9U0-R2 changed app_code: classic."

    $app = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java")
    Assert-Condition -Condition ($app.Contains("GeoCeDGDocumentPolicy.isNative") -and
            $app.Contains("createDocumentPreflightConfig")) `
        -Message "GeoCeDG native-save/preflight boundary is missing."
    [void](Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/io/AtomicDocumentFileWriter.java")
    [void](Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/io/DocumentArchivePreflight.java")

    $fileHandler = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/headless/GFileHandler.java")
    $legacyLoader = [regex]::Match($fileHandler,
        '(?s)public static boolean loadXML\s*\(.*?(?=/\*\*\s*\r?\n\s*\* Parses an already-preflighted)').Value
    $contentLoader = [regex]::Match($fileHandler,
        '(?s)public static boolean loadPreflightedNativeXML\s*\(.*?\n\s*}\s*\n}').Value
    Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace($contentLoader) -and
            $contentLoader.Contains("readZipFromInputStream(input, false)") -and
            -not $contentLoader.Contains("Base64") -and
            -not $contentLoader.Contains("isMacroFile") -and
            -not $contentLoader.Contains("initUndoInfo") -and
            -not $contentLoader.Contains("setSaved") -and
            -not $contentLoader.Contains("resetCurrentFile") -and
            -not $contentLoader.Contains("updateCommandDictionary") -and
            -not $contentLoader.Contains("catch (MyError") -and
            -not $contentLoader.Contains("showError")) `
        -Message "GFileHandler content parsing is no longer separated from publication/undo commit."
    Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace($legacyLoader) -and
            $legacyLoader.Contains("return app.loadXML(zipFile);") -and
            $legacyLoader.Contains("readZipFromInputStream(bis,") -and
            $legacyLoader.Contains("isMacroFile") -and
            $legacyLoader.Contains("initUndoInfo") -and
            $legacyLoader.Contains("setSaved") -and
            $legacyLoader.Contains("resetCurrentFile") -and
            $legacyLoader.Contains("updateCommandDictionary") -and
            $legacyLoader.Contains("catch (MyError") -and
            $legacyLoader.Contains("showError")) `
        -Message "GFileHandler legacy loadXML/base64 behavior drifted."

    $appD = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java")
    foreach ($fragment in @("loadNativeArchive", "captureNativeDocumentLoadState",
            "restoreNativeDocumentLoadState", "doLoadNativeXML",
            "NativeDocumentRollbackException",
            "GFileHandler.loadPreflightedNativeXML", "prepareUndoBaseline",
            "beforeNativeUndoBaselineCommit", "commitUndoBaseline")) {
        Assert-Condition -Condition ($appD.Contains($fragment)) `
            -Message "Native document transaction lacks $fragment."
    }
    $nativeLoader = [regex]::Match($appD,
        '(?s)private boolean loadNativeArchive\s*\(.*?(?=/\*\*\s*\r?\n\s*\* Hook immediately before)').Value
    Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace($nativeLoader) -and
            $nativeLoader -match '(?s)prepareUndoBaseline\(\).*?updateCommandDictionary\(\).*?setCurrentFile\(file\).*?setSaved\(\).*?beforeNativeUndoBaselineCommit\(\);\s*undoManager\.commitUndoBaseline\(undoBaseline\)' -and
            $nativeLoader -match '(?s)catch \(Exception \| MyError \| CommandNotLoadedError failure\).*?loaded = false;.*?undoBaseline\.close\(\).*?initing = wasIniting;.*?if \(!loaded\).*?restoreNativeDocumentLoadState\(previousState, loadFailure\)' -and
            -not $nativeLoader.Contains("initUndoInfo") -and
            $appD -match '(?s)restoreNativeDocumentLoadState.*?readZipFromInputStream' -and
            $appD -match '(?s)boolean nativeDocument = !isMacroFile && isNativeDocument\(url\).*?catch \(Exception e\).*?if \(!nativeDocument\)\s*\{\s*setCurrentFile\(null\);' -and
            -not $appD.Contains("GFileHandler.loadXMLContent")) `
        -Message "Native parse/undo-baseline rollback and commit ordering drifted."

    $undoManagerD = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/main/undo/UndoManagerD.java")
    foreach ($fragment in @("prepareUndoBaseline", "commitUndoBaseline",
            "class PreparedUndoBaseline", "undoHistoryGeneration",
            "doStoreUndoInfo(final StringBuilder undoXML,",
            "scheduledGeneration != undoHistoryGeneration")) {
        Assert-Condition -Condition ($undoManagerD.Contains($fragment)) `
            -Message "UndoManagerD atomic native baseline lacks $fragment."
    }
    Assert-Condition -Condition (
            $undoManagerD -match '(?s)commitUndoBaseline.*?clearUndoInfo\(\);\s*maybeStoreUndoCommand\(command\);\s*undoHistoryGeneration\+\+;\s*baseline\.markCommitted\(\)' -and
            $undoManagerD -match '(?s)void close\(\).*?disposableCommand\.delete\(\).*?catch \(RuntimeException cleanupFailure\).*?Log\.debug\(cleanupFailure\)') `
        -Message "Prepared undo baseline commit/cleanup is not atomic and nonthrowing."

    $desktopLifecycleTest = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGDocumentLifecycleTest.java")
    foreach ($fragment in @("corruptNativeArchiveIsRejectedBeforeLiveLoad",
            "setUserStopsLoading(true)", "getHistorySize()", "undoPossible()",
            "redoPossible()", "UndoCommitFailingApp",
            "failNextUndoBaselineCommit()")) {
        Assert-Condition -Condition ($desktopLifecycleTest.Contains($fragment)) `
            -Message "R2-D09 transactional rollback coverage lacks $fragment."
    }

    $packageProfile = Read-JsonDocument -RelativePath `
        "packaging/windows/package.yml"
    Assert-Condition -Condition (
            $packageProfile.file_association.extension -eq "cedg" -and
            [bool]$packageProfile.file_association.installers_only) `
        -Message "Windows native association profile is invalid."
    $association = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "packaging/windows/file-associations.properties")
    Assert-Condition -Condition ($association.Contains("extension=cedg") -and
            -not $association.Contains("extension=ggb") -and
            -not $association.Contains("application/vnd.geogebra.file")) `
        -Message "Windows association claims .ggb or upstream MIME identity."
}

function Assert-DocumentationContracts {
    param([Parameter(Mandatory)] [object]$Evidence)

    foreach ($path in @($ArchitecturePath, $ReportPath)) {
        $content = Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $path)
        foreach ($fragment in @(
                "IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW",
                "selfApproved = false", "authorApproved = false",
                "passClaimed = false", "G9U1 = DESIGNED — NOT AUTHORIZED")) {
            Assert-Condition -Condition ($content.Contains($fragment)) `
                -Message "$path is missing the candidate boundary: $fragment"
        }
    }
    $report = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $ReportPath)
    Assert-Condition -Condition ($report.Contains("PENDING AUTHOR; NOT PASSED") -and
            $report.Contains("Only the author may mark this checklist passed") -and
            $report.Contains("prepareUndoBaseline()") -and
            $report.Contains("commitUndoBaseline()") -and
            $report.Contains("undo-commit") -and
            $report.Contains("PENDING correction rerun") -and
            -not $report.Contains("no general construction or undo rollback")) `
        -Message "The R2 report does not preserve the pending transactional correction boundary."
    $architecture = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $ArchitecturePath)
    Assert-Condition -Condition ($architecture.Contains("loadPreflightedNativeXML") -and
            $architecture.Contains("NativeDocumentRollbackException") -and
            $architecture.Contains("prepareUndoBaseline()") -and
            $architecture.Contains("commitUndoBaseline()") -and
            $architecture.Contains("history generation") -and
            $architecture.Contains("nonthrowing") -and
            $architecture.Contains("correction validation") -and
            -not $architecture.Contains("no general construction or undo rollback")) `
        -Message "The R2 architecture does not record the transactional undo-baseline correction."

    foreach ($path in @(
            $RoadmapPath, $DeveloperGuidePath, $UserGuidePath,
            $DocumentationArchitecturePath, $TraceabilityPath,
            $PublicMatrixPath, $SpecificationIndexPath)) {
        $content = Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $path)
        Assert-Condition -Condition (
                $content.Contains("G9U0-R2") -and
                $content.Contains(".cedg") -and
                $content -match '(?i)candidate|candidato') `
            -Message "$path lacks the current R2 candidate/document boundary."
    }

    if ($Evidence.validation.focused.status -eq "PASSED") {
        foreach ($path in @($RoadmapPath, $DeveloperGuidePath, $UserGuidePath)) {
            $content = Get-Content -Raw -LiteralPath (
                Resolve-RequiredFile -RelativePath $path)
            Assert-Condition -Condition ($content.Contains("G9U0-R2") -and
                    $content.Contains(".cedg") -and
                    $content.Contains("PENDING AUTHOR REVIEW")) `
                -Message "$path lacks the validated R2 candidate boundary."
        }
    }
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [string]$LogFileName
    )

    if (-not $AllowToolchainDownload) {
        $Arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogFileName
    Write-Host "`n==> $Description"
    Write-Host "    log: $logPath"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "$Description failed with exit code $exitCode."
}

function Invoke-LoggedPackagingVerifier {
    param([Parameter(Mandatory)] [hashtable]$Parameters)

    $logPath = Join-Path $LogDirectory "g9u0-r2-packaging.log"
    Write-Host "`n==> R2-D17 Windows packaging association boundary"
    Write-Host "    log: $logPath"
    & $PackagingVerifier @Parameters 2>&1 | Tee-Object -FilePath $logPath
    $exitCode = $LASTEXITCODE
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "R2-D17 packaging verification failed with exit code $exitCode."
}

function Get-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $module = if ($ClassName.StartsWith("org.geocedg.desktop.")) {
        "source/desktop/desktop"
    } else {
        "source/shared/common-jre"
    }
    $relativePath = "$module/build/test-results/test/TEST-$ClassName.xml"
    [xml]$result = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $relativePath)
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("$ClassName is not clean: tests=$($suite.tests), " +
            "failures=$($suite.failures), errors=$($suite.errors), " +
            "skipped=$($suite.skipped).")
    return [ordered]@{
        class = $ClassName
        tests = [int]$suite.tests
        failures = [int]$suite.failures
        errors = [int]$suite.errors
        skipped = [int]$suite.skipped
    }
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $errors = @(Select-String -LiteralPath (
            Resolve-RequiredFile -RelativePath $RelativePath) `
        -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle errors."
}

function Write-CanonicalSummary {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object[]]$TestResults,
        [Parameter(Mandatory)] [string[]]$CandidatePaths
    )

    $mutableCloseoutPaths = @($CandidatePaths | Where-Object {
            $_.StartsWith("docs/", [StringComparison]::Ordinal) -or
            $_ -in @(
                $ScenarioPath,
                $EvidencePath,
                $EvidenceHashPath,
                $SpecificationIndexPath)
        } | Sort-Object -CaseSensitive)
    $deterministicPaths = @($CandidatePaths | Where-Object {
            $_ -notin $mutableCloseoutPaths
        } | Sort-Object -CaseSensitive)
    $pathHashes = @($deterministicPaths |
        ForEach-Object {
            [ordered]@{
                path = $_
                sha256 = Get-BinarySha256 -RelativePath $_
            }
        })
    $corpus = Read-JsonDocument -RelativePath $CorpusPath
    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9U0-R2"
        entrySha = $EntrySha
        planningTagObject = $PlanningTagObject
        scenarioIds = @($Scenarios.groups | ForEach-Object { $_.cases } |
            ForEach-Object { $_.id } | Sort-Object -CaseSensitive)
        testResults = @($TestResults | Sort-Object { $_.class })
        focusedR2Tests = 31
        supportingPersistenceTests = 31
        executedJUnitTests = 62
        packagingStaticProbe = "PASSED"
        corpus = @($corpus.entries | ForEach-Object {
                [ordered]@{ path = $_.path; sha256 = $_.sha256 }
            } | Sort-Object { $_.path })
        archiveNormalization =
            "entry-name order; raw entry SHA-256; XML UTF-8 LF SHA-256"
        archiveInventories = @($corpus.entries | Sort-Object path |
            ForEach-Object {
                Get-CanonicalArchiveInventory -RelativePath $_.path
            })
        candidatePaths = @($CandidatePaths | Sort-Object -CaseSensitive)
        mutableCloseoutPathsExcludedFromHashes = $mutableCloseoutPaths
        deterministicCandidatePathHashes = $pathHashes
        semanticContract = [ordered]@{
            ordinaryGeoElementStyleOnly = $true
            genericPathAdded = $false
            crossingDrawableTopologyInput = $false
            nativeExtension = "cedg"
            compatibilityInputExtension = "ggb"
            archiveXmlChanged = $false
            appCode = "classic"
            filenameSemanticAuthority = $false
            secondLiveParseFailureRestoresLiveDocument = $true
            undoBaselineCommitFailureRestoresLiveDocument = $true
        }
    }
    $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
    $parent = Split-Path -Parent $CanonicalSummaryPath
    [void](New-Item -ItemType Directory -Path $parent -Force)
    [IO.File]::WriteAllText($CanonicalSummaryPath, $json,
        [Text.UTF8Encoding]::new($false))
    $summaryHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
        $CanonicalSummaryPath).Hash.ToLowerInvariant()
    Write-Host "Canonical summary: $CanonicalSummaryPath"
    Write-Host "Canonical summary SHA-256: $summaryHash"

    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "Comparison summary is missing: $CompareCanonicalSummaryPath"
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message ("Deterministic R2 summary mismatch: $summaryHash != " +
                $comparisonHash)
        Write-Host "Deterministic summary comparison: MATCH"
    }
}

function Invoke-HistoricalVerifier {
    param(
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [string]$ScriptName
    )

    $scriptPath = Join-Path $PSScriptRoot $ScriptName
    $parameters = @{
        LogDirectory = Join-Path $LogDirectory (
            [IO.Path]::GetFileNameWithoutExtension($ScriptName))
    }
    if ($AllowToolchainDownload) {
        $parameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $parameters.KeepBuildOutputs = $true
    }
    Write-Host "`n==> $Description"
    & $scriptPath @parameters
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "$Description failed with exit code $LASTEXITCODE."
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null
[string]$FailureContext = $null

try {
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    foreach ($path in @(
            $PromptPath, $AdrPath, $LocusSpecPath, $DocumentSpecPath,
            $ScenarioPath, $EvidencePath, $EvidenceHashPath, $CorpusPath,
            $CorpusHashPath, $ArchitecturePath, $ReportPath, $RoadmapPath,
            $DeveloperGuidePath, $UserGuidePath, $DocumentationArchitecturePath,
            $TraceabilityPath, $PublicMatrixPath, $SpecificationIndexPath,
            $UpstreamImpactPath,
            "tools/agent/verify-g9u0-r2-product-refinement.ps1",
            "tools/agent/verify.ps1")) {
        [void](Resolve-RequiredFile -RelativePath $path)
    }

    Assert-PlanningAuthority
    Assert-HashManifest -ManifestPath $EvidenceHashPath `
        -ExpectedTargets @($EvidencePath, $ScenarioPath)
    $scenarios = Read-JsonDocument -RelativePath $ScenarioPath
    $evidence = Read-JsonDocument -RelativePath $EvidencePath
    Assert-ScenarioAuthority -Scenarios $scenarios
    Assert-EvidenceContract -Evidence $evidence -Scenarios $scenarios
    Assert-Corpus
    Assert-SourceBoundary -Evidence $evidence
    Assert-ProductStaticContracts
    Assert-DocumentationContracts -Evidence $evidence

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9U0-R2."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9U0-R2."

    $packagingParameters = @{}
    if ($VerifyPackagingArtifacts) {
        $packagingParameters.CheckToolchain = $true
        $packagingParameters.RequireArtifacts = $true
        if (-not [string]::IsNullOrWhiteSpace($PackagingArtifactRoot)) {
            $packagingParameters.ArtifactRoot =
                [IO.Path]::GetFullPath($PackagingArtifactRoot)
        }
    }
    Invoke-LoggedPackagingVerifier -Parameters $packagingParameters

    if ($SkipBuild) {
        Write-Host "G9U0-R2 static/scaffold verification passed."
        if ($evidence.sourceBoundary.inventoryStatus -eq
                "OPEN_PENDING_IMPLEMENTATION_FREEZE") {
            Write-Host ("Productive inventory is open; no R2 product test " +
                "or implementation PASS is claimed.")
        } else {
            Write-Host "This invocation did not rerun focused product tests."
        }
    } else {
        Assert-Condition -Condition ($evidence.sourceBoundary.inventoryStatus -eq
                "FROZEN") `
            -Message "Freeze the exact R2 inventory before build validation."
        $GeneratedState = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9u0-r2"

        $sharedArguments = @(
            ":shared:common-jre:test"
        )
        foreach ($class in @($ExpectedTestCounts.Keys | Where-Object {
                    -not $_.StartsWith("org.geocedg.desktop.")
                })) {
            $sharedArguments += @("--tests", $class)
        }
        $sharedArguments += @(
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $sharedArguments `
            -Description "G9U0-R2 shared focused and persistence-support tests" `
            -LogFileName "g9u0-r2-focused-shared-gradle.log"

        $testResults = @()
        foreach ($entry in @($ExpectedTestCounts.GetEnumerator() |
                Where-Object {
                    -not $_.Key.StartsWith("org.geocedg.desktop.")
                })) {
            $testResults += Get-TestResult -ClassName $entry.Key `
                -ExpectedTests ([int]$entry.Value)
        }
        foreach ($checkstylePath in @(
                "source/shared/common/build/reports/checkstyle/main.xml",
                "source/shared/common-jre/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $checkstylePath
        }

        $desktopArguments = @(
            ":desktop:desktop:test"
        )
        foreach ($class in @($ExpectedTestCounts.Keys | Where-Object {
                    $_.StartsWith("org.geocedg.desktop.")
                })) {
            $desktopArguments += @("--tests", $class)
        }
        $desktopArguments += @(
            ":desktop:desktop:checkstyleMain",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $desktopArguments `
            -Description "G9U0-R2 Desktop document-lifecycle tests" `
            -LogFileName "g9u0-r2-focused-desktop-gradle.log"
        foreach ($entry in @($ExpectedTestCounts.GetEnumerator() |
                Where-Object {
                    $_.Key.StartsWith("org.geocedg.desktop.")
                })) {
            $testResults += Get-TestResult -ClassName $entry.Key `
                -ExpectedTests ([int]$entry.Value)
        }
        foreach ($checkstylePath in @(
                "source/desktop/desktop/build/reports/checkstyle/main.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $checkstylePath
        }
        Assert-Condition -Condition ((@($R2TestClasses | ForEach-Object {
                    [int]$ExpectedTestCounts[$_]
                }) | Measure-Object -Sum).Sum -eq 31 -and
                (@($SupportingPersistenceTestClasses | ForEach-Object {
                    [int]$ExpectedTestCounts[$_]
                }) | Measure-Object -Sum).Sum -eq 31) `
            -Message "R2/supporting focused count split drifted from 31/31."
        Write-CanonicalSummary -Scenarios $scenarios `
            -TestResults $testResults -CandidatePaths @(Get-CandidatePaths)

        if (-not ($HistoricalRegressionsAlreadyComposed -or
                $HistoricalRegressionsAlreadyRecorded)) {
            foreach ($entry in $HistoricalVerifiers.GetEnumerator()) {
                Invoke-HistoricalVerifier -Description $entry.Key `
                    -ScriptName $entry.Value
            }
        } else {
            foreach ($property in @(
                    $evidence.validation.g9u0R1Regression,
                    $evidence.validation.historicalG9U0Regression,
                    $evidence.validation.g9x1Regression,
                    $evidence.validation.g5Regression,
                    $evidence.validation.g9aRegression,
                    $evidence.validation.legacyLocusRegression)) {
                Assert-Condition -Condition ($property.status -eq "PASSED") `
                    -Message ("Composed R2 invocation requires prior clean " +
                        "historical-regression evidence.")
            }
            $historySource = if ($HistoricalRegressionsAlreadyComposed) {
                "the current composed authority"
            } else {
                "separate saved regression invocations"
            }
            Write-Host ("Historical R2-R01..R06 execution was already performed " +
                "by $historySource.")
        }

        Write-Host "G9U0-R2 focused result: 31 R2 JUnit + 31 support JUnit."
        Write-Host "G9U0-R2 packaging static result: R2-D17 passed."
        Write-Host "G9U0-R2 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW"
        Write-Host "Author approval and implementation PASS are not claimed."
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9U0-R2 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during R2 verification.`n" +
                    "Before:`n$InitialStatus`nAfter:`n$finalStatus")
            }
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
            $FailureContext = $_.ScriptStackTrace
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup/status failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    $failureMessage = $Failure.Message
    if (-not [string]::IsNullOrWhiteSpace($FailureContext)) {
        $failureMessage += "`n$FailureContext"
    }
    Write-Error $failureMessage
    exit 1
}

exit 0

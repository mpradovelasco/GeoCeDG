[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-locus-v2-public-surface")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "d4de0e480b0a6439c940a0f6e0cfde51c5e56bd2"
$ExpectedBranch = "feature/g9u0-locus-v2-public-surface"
$G9A3TagName = "geocedg-g9a3-pass"
$G9A3TagObject = "4dd65df131030799465bc47ee4b715cbe043d98e"
$G9U0TagName = "geocedg-g9u0-pass"
$G9U0TagObject = "612845c42925bc519f68443d09fd400ff4365251"
$G9U0PromotionCommit = "bdd20da3e9e711dcc35e818d857d604d7b217385"
$PreAuthorReviewEvidenceSha256 =
    "480c6b022e8614f0b61b3eb39f40efe791e38e37abcafafdfaaa850622bbd47c"
$ConcurrentMainObserved = "6eafd868121824b7cf7a4eea6a0c9cce2b936f3f"
$ExpectedMainOnlyCommits = @(
    "1aacf910017cd074fd52a8d76a2e73a8d545652b",
    "3ccf3bf491c22091c368fd9f49f0e7f6abbe901c",
    "09a0d7f24326d96100f97da8d2d141491b662a78",
    "802d13e0b6370bb82f41da69744b5bed5471f79b",
    "c6217cdc1bf66feb4b55822372c7a50ccd1116bf",
    "6eafd868121824b7cf7a4eea6a0c9cce2b936f3f"
)
$ExpectedMainOnlyPaths = @(
    ".gitattributes",
    ".github/workflows/verify.yml",
    "tools/agent/verify-operational.ps1"
)
$ExpectedPromotionMetadataPaths = @(
    "docs/roadmap/geocedg_roadmap.md",
    "geocedg/validation/locus-v2/g9u0/g9u0-evidence.sha256",
    "geocedg/validation/locus-v2/g9u0/g9u0-public-surface-evidence.json"
)
$PromptPath = ".github/prompts/tasks/g9u0-locus-v2-public-surface.prompt.md"
$PromptSha256 = "160dae2e7dd56fc51fa8910ee9ddecceb2cffa0a3594a655393de264f29cbdfe"
$SpecificationPath = "geocedg/specs/locus/locus-v2-public-surface.md"
$SpecificationSha256 = "5fd65d1f0ce629b063afb196b946157b9ee9d6d3b778ac9c158aa26d886bdb72"
$AdrPath = "docs/adr/0013-public-locus-v2-surface-and-token-selection.md"
$AdrSha256 = "a67352f064c3c9a89adc94cbe4ad025577e440fb9b9d24bace4634d68e52220c"
$MatrixPath = "docs/validation/g9_public_workspace_validation_matrix.md"
$MatrixSha256 = "73e8b13f86f92a4a0987869d63b60c5204f90de673afe45d11ab220c7e5cf9c9"
$EvidenceRoot = "geocedg/validation/locus-v2/g9u0"
$EvidencePath = "$EvidenceRoot/g9u0-public-surface-evidence.json"
$ScenarioPath = "$EvidenceRoot/g9u0-public-surface-scenarios.json"
$EvidenceHashPath = "$EvidenceRoot/g9u0-evidence.sha256"
$CorpusPath = "$EvidenceRoot/g9u0-compatibility-corpus.json"
$CorpusHashPath = "$EvidenceRoot/g9u0-compatibility-corpus.sha256"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$InitialStatus = $null
$GeneratedSnapshot = $null
$EvidenceIsApproved = $false
$CandidateBoundaryMode = $null
$CandidateCommit = $null

$RequiredPaths = @(
    $PromptPath,
    $SpecificationPath,
    $AdrPath,
    $MatrixPath,
    $EvidencePath,
    $ScenarioPath,
    $EvidenceHashPath,
    $CorpusPath,
    $CorpusHashPath,
    "docs/architecture/locus_v2_public_surface_implementation.md",
    "docs/developer/locus_v2_public_api.md",
    "docs/validation/g9u0_locus_v2_public_surface_migration_report.md",
    "docs/validation/g9u0_locus_v2_public_surface_traceability_matrix.md",
    "docs/validation/g9u0_locus_v2_public_surface_compatibility_matrix.md",
    "tools/agent/verify-g9u0-locus-v2-public-surface.ps1",
    "tools/agent/verify-g8b-intersections.ps1",
    "tools/agent/verify-locus-v2.ps1",
    "tools/agent/verify.ps1"
)

$RequiredScenarioIds = @(
    (1..14 | ForEach-Object { "U0-C{0:D2}" -f $_ })
    (1..22 | ForEach-Object { "U0-G{0:D2}" -f $_ })
    (1..11 | ForEach-Object { "U0-M{0:D2}" -f $_ })
    (1..18 | ForEach-Object { "U0-I{0:D2}" -f $_ })
    (1..16 | ForEach-Object { "U0-P{0:D2}" -f $_ })
    (1..4 | ForEach-Object { "U0-F{0:D2}" -f $_ })
    (1..4 | ForEach-Object { "U0-L{0:D2}" -f $_ })
    "U0-T01"
    "U0-T01A"
    "U0-T02"
    "U0-T03"
)

$ExpectedTestCounts = [ordered]@{
    "org.geocedg.common.locus.G9U0CommandSurfaceTest" = 14
    "org.geocedg.common.locus.G9U0GeneratorSuiteTest" = 22
    "org.geocedg.common.locus.G9U0MetricPositionTest" = 11
    "org.geocedg.common.locus.G9U0IntersectionTokenTest" = 18
    "org.geocedg.common.locus.G9U0PersistenceCompatibilityTest" = 16
    "org.geocedg.desktop.locus.G9U0RuntimeFeatureTest" = 4
    "org.geocedg.desktop.locus.G9U0LocalizationHelpTest" = 4
    "org.geocedg.desktop.locus.G9U0ToolSurfaceTest" = 4
}

$RequiredHardZeroCounterNames = @(
    "generator.labelAuthorityUses",
    "generator.coordinateAuthorityUses",
    "generator.sliderVisibilityAuthorityUses",
    "generator.ancestorInferenceAttempts",
    "generator.dependentStateAssignments",
    "generator.renderCacheReads",
    "generator.renderSampleReads",
    "generator.viewportReads",
    "generator.pixelToleranceReads",
    "position.coordinateRepairAttempts",
    "position.nearestPreimageRepairAttempts",
    "position.ordinalAuthorityUses",
    "position.revisionAsDurableIdentityUses",
    "intersection.tokenFromOrderUses",
    "intersection.tokenFromCoordinateUses",
    "intersection.proximityPersistenceUses",
    "metric.scalarAdapterIndependentCalculations",
    "compatibility.legacyLocusRedirects",
    "compatibility.legacyLengthSemanticChanges",
    "compatibility.lossyDowngrades",
    "compatibility.automaticMigrations",
    "lifecycle.serializedRenderSnapshots",
    "lifecycle.stalePayloadPublications",
    "evaluator.hiddenGraphEdges"
)

$RequiredZeroScopeCounterNames = @(
    "legacyGeoLocusChanges",
    "legacyMode47Changes",
    "genericPathAdditions",
    "unsupportedDriverExposure",
    "spatialPrimitiveImplementation",
    "dxfImplementation",
    "workspaceImplementation",
    "g9x1Implementation",
    "g9u1Implementation",
    "g9bImplementation",
    "g9cImplementation",
    "g9u2Implementation",
    "productiveG10Implementation",
    "generatedTrackedArtifacts"
)

$FocusedFinalLogDirectory =
    "artifacts/g9u0/candidate/focused-final-pass-escalated"
$FocusedDeterministicLogDirectory =
    "artifacts/g9u0/candidate/focused-deterministic-pass-escalated"
$ComposedFinalLogDirectory = "artifacts/g9u0/candidate/composed-final-pass"
$FocusedFinalCommand =
    ".\tools\agent\verify-g9u0-locus-v2-public-surface.ps1 " +
    "-KeepBuildOutputs -LogDirectory " +
    "artifacts\g9u0\candidate\focused-final-pass-escalated"
$FocusedDeterministicCommand =
    ".\tools\agent\verify-g9u0-locus-v2-public-surface.ps1 " +
    "-KeepBuildOutputs -LogDirectory " +
    "artifacts\g9u0\candidate\focused-deterministic-pass-escalated"
$ComposedFinalCommand =
    ".\tools\agent\verify.ps1 -KeepBuildOutputs -LogDirectory " +
    "artifacts\g9u0\candidate\composed-final-pass"

$Documents = @(
    "docs/architecture/locus_v2_public_surface_implementation.md",
    "docs/developer/locus_v2_public_api.md",
    "docs/validation/g9u0_locus_v2_public_surface_migration_report.md",
    "docs/validation/g9u0_locus_v2_public_surface_traceability_matrix.md",
    "docs/validation/g9u0_locus_v2_public_surface_compatibility_matrix.md"
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
        -Message "Required G9U0 artifact is missing: $RelativePath"
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
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString($sha.ComputeHash(
            [Text.UTF8Encoding]::new($false).GetBytes($canonical))).ToLowerInvariant()
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
    $duplicates = @($Actual).Count - $actualUnique.Count
    $missing = @($expectedUnique | Where-Object { $_ -notin $actualUnique })
    $unexpected = @($actualUnique | Where-Object { $_ -notin $expectedUnique })
    Assert-Condition -Condition ($duplicates -eq 0 -and
            $missing.Count -eq 0 -and $unexpected.Count -eq 0) `
        -Message ("{0} mismatch. duplicates={1}; missing={2}; unexpected={3}" -f
            $Description, $duplicates, ($missing -join ", "),
            ($unexpected -join ", "))
}

function Assert-ExactSequence {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Actual,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    Assert-Condition -Condition ($Actual.Count -eq $Expected.Count -and
            (($Actual -join "`n") -ceq ($Expected -join "`n"))) `
        -Message "$Description sequence drifted."
}

function Get-OptionalPropertyValue {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string]$Name
    )

    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

function Assert-GitCommitObject {
    param(
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [string]$Description
    )

    & git -C $RepositoryRoot cat-file -e "$Commit`^{commit}" 2>$null
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "$Description is not a present Git commit: $Commit"
}

function Get-GitPathsBetween {
    param(
        [Parameter(Mandatory)] [string]$From,
        [Parameter(Mandatory)] [string]$To
    )

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $From $To --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate Git paths between $From and $To."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
            $_.Replace("\", "/") } | Sort-Object -Unique)
}

function Assert-ConcurrentMainCloseout {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [object]$Closeout
    )

    Assert-Condition -Condition ($Closeout.authorDecision -eq
            "G9U0_PASS_AUTHOR_APPROVED" -and
            $Closeout.authorReviewDate -eq "2026-08-20" -and
            $Closeout.concurrentMainObservedAtCloseoutEntry -eq
                $ConcurrentMainObserved -and
            $Closeout.mergeBase -eq $EntrySha -and
            $Closeout.mainOnlyClassification -eq
                "INDEPENDENT_OPERATIONAL_NOT_G9U0_SCOPE" -and
            $Closeout.laterPhaseExecution -eq "NONE") `
        -Message "G9U0 author-closeout metadata is inconsistent."
    Assert-ExactSequence -Actual @($Closeout.mainOnlyCommits) `
        -Expected $ExpectedMainOnlyCommits `
        -Description "G9U0 concurrent-main commit"
    Assert-ExactSet -Actual @($Closeout.mainOnlyPaths) `
        -Expected $ExpectedMainOnlyPaths `
        -Description "G9U0 concurrent-main path"

    Assert-GitCommitObject -Commit $ConcurrentMainObserved `
        -Description "Concurrent main"
    foreach ($commit in $ExpectedMainOnlyCommits) {
        Assert-GitCommitObject -Commit $commit `
            -Description "Concurrent-main history member"
    }
    $mainHistory = @(& git -C $RepositoryRoot rev-list --reverse `
        --ancestry-path "$EntrySha..$ConcurrentMainObserved")
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate concurrent-main history."
    Assert-ExactSequence -Actual $mainHistory -Expected $ExpectedMainOnlyCommits `
        -Description "Entry-to-concurrent-main history"
    $mainMergeBase = (& git -C $RepositoryRoot merge-base $EntrySha `
        $ConcurrentMainObserved).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $mainMergeBase -eq $EntrySha) `
        -Message "Concurrent main no longer branches at the G9U0 entry commit."
    Assert-ExactSet -Actual @(Get-GitPathsBetween -From $EntrySha `
            -To $ConcurrentMainObserved) -Expected $ExpectedMainOnlyPaths `
        -Description "Concurrent-main operational change"

    $candidatePaths = @(
        "productivePaths", "testPaths", "testSupportPaths", "fixturePaths",
        "modelPaths", "corpusPaths", "validationPaths", "supportingPaths" |
        ForEach-Object { @($Evidence.sourceBoundary.$_) })
    $overlap = @($candidatePaths | Where-Object {
            $_ -in $ExpectedMainOnlyPaths })
    Assert-Condition -Condition ($overlap.Count -eq 0) `
        -Message ("Concurrent-main paths were classified as G9U0: " +
            ($overlap -join ", "))
}

function Assert-HashManifest {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$ExpectedTargets
    )

    $entries = [Collections.Generic.List[object]]::new()
    foreach ($line in Get-Content -LiteralPath (Resolve-RequiredFile `
            -RelativePath $RelativePath)) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        Assert-Condition -Condition ($line -match
                '^([0-9a-f]{64})\s{2}(.+)$') `
            -Message "Invalid SHA-256 line in ${RelativePath}: $line"
        $entries.Add([pscustomobject]@{ Hash = $Matches[1]; Path = $Matches[2] })
    }
    Assert-ExactSet -Actual @($entries | ForEach-Object { $_.Path }) `
        -Expected $ExpectedTargets -Description "$RelativePath target set"
    foreach ($entry in $entries) {
        $actual = if ($entry.Path.EndsWith(".ggb",
                [StringComparison]::OrdinalIgnoreCase)) {
            Get-BinarySha256 -RelativePath $entry.Path
        } else {
            Get-CanonicalTextSha256 -RelativePath $entry.Path
        }
        Assert-Condition -Condition ($actual -eq $entry.Hash) `
            -Message "SHA-256 mismatch for $($entry.Path) in $RelativePath"
    }
}

function Assert-Authority {
    Assert-Condition -Condition ((Get-CanonicalTextSha256 $PromptPath) -eq
            $PromptSha256) -Message "The canonical G9U0 prompt hash changed."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 $SpecificationPath) -eq
            $SpecificationSha256) -Message "The public Locus V2 spec hash changed."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 $AdrPath) -eq
            $AdrSha256) -Message "ADR 0013 hash changed."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 $MatrixPath) -eq
            $MatrixSha256) -Message "The G9 public validation matrix hash changed."

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$G9A3TagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagObject -eq $G9A3TagObject) `
        -Message "The annotated G9A3 tag object changed."
    Assert-Condition -Condition ((& git -C $RepositoryRoot cat-file -t `
            $tagObject).Trim() -eq "tag") `
        -Message "$G9A3TagName is not an annotated tag."
    $peeled = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$G9A3TagName^{}").Trim()
    Assert-Condition -Condition ($peeled -eq $EntrySha) `
        -Message "The G9A3 tag no longer peels to the G9U0 entry commit."
    & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "HEAD does not descend from approved G9A3."
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $Documents) {
        $document = Resolve-RequiredFile -RelativePath $relativeDocument
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

function Get-TestSourcePath {
    param([Parameter(Mandatory)] [string]$ClassName)

    if ($ClassName.StartsWith("org.geocedg.common.")) {
        $root = "source/shared/common-jre/src/test/java"
    } elseif ($ClassName.StartsWith("org.geocedg.desktop.")) {
        $root = "source/desktop/desktop/src/test/java"
    } else {
        throw "Unsupported G9U0 test package: $ClassName"
    }
    return "$root/$($ClassName.Replace('.', '/')).java"
}

function Get-TestMethods {
    param([Parameter(Mandatory)] [string]$ClassName)

    $source = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath (Get-TestSourcePath -ClassName $ClassName))
    $pattern = '(?ms)@Test\s+(?:@[^\r\n]+\s+)*' +
        '(?:(?:public|protected|private)\s+)?(?:final\s+)?' +
        'void\s+([A-Za-z0-9_]+)\s*\('
    return @([regex]::Matches($source, $pattern) | ForEach-Object {
            $_.Groups[1].Value
        })
}

function Get-ScenarioIdFromMethod {
    param(
        [Parameter(Mandatory)] [string]$Group,
        [Parameter(Mandatory)] [string]$MethodName
    )

    $lower = $Group.ToLowerInvariant()
    $pattern = if ($Group -eq "T") {
        "^${lower}([0-9]{2})(a?)"
    } else {
        "^${lower}([0-9]{2})"
    }
    Assert-Condition -Condition ($MethodName -match $pattern) `
        -Message "G9U0 test method lacks a canonical prefix: $MethodName"
    $hasAlphaSuffix = $Group -eq "T" -and
        -not [string]::IsNullOrEmpty($Matches[2])
    $suffix = if (-not $hasAlphaSuffix) {
        $Matches[1]
    } else {
        "$($Matches[1])A"
    }
    return "U0-${Group}${suffix}"
}

function Assert-ScenarioAuthority {
    param([Parameter(Mandatory)] [bool]$RequireTestSources)

    $scenario = Read-JsonFile -RelativePath $ScenarioPath
    Assert-Condition -Condition ($scenario.phase -eq "G9U0" -and
            $scenario.status -eq
                "IMPLEMENTATION_CANDIDATE_SCENARIOS_SOURCE_COMPLETE" -and
            -not [bool]$scenario.authorApprovalClaimed -and
            [bool]$scenario.countsFrozen -and
            [int]$scenario.expectedScenarioCount -eq 93 -and
            [int]$scenario.testExecution.commonCount -eq 81 -and
            [int]$scenario.testExecution.desktopCount -eq 12 -and
            [int]$scenario.testExecution.focusedTotal -eq 93) `
        -Message "G9U0 scenario status or totals are inconsistent."
    $ids = @($scenario.groups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.id })
    Assert-ExactSet -Actual $ids -Expected $RequiredScenarioIds `
        -Description "G9U0 scenario IDs"
    Assert-ExactSet -Actual @($scenario.groups | ForEach-Object {
            $_.testClass }) -Expected @($ExpectedTestCounts.Keys) `
        -Description "G9U0 planned test classes"

    foreach ($group in @($scenario.groups)) {
        Assert-Condition -Condition ([int]$ExpectedTestCounts[$group.testClass] `
                -eq @($group.cases).Count) `
            -Message "Scenario/class count mismatch for group $($group.id)."
        if ($RequireTestSources) {
            $methodIds = @(Get-TestMethods -ClassName $group.testClass |
                ForEach-Object {
                    Get-ScenarioIdFromMethod -Group $group.id -MethodName $_
                })
            Assert-ExactSet -Actual $methodIds `
                -Expected @($group.cases | ForEach-Object { $_.id }) `
                -Description "G9U0 source-method mapping for $($group.id)"
        }
    }
}

function Assert-CandidateCloseoutBoundary {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [object]$Closeout
    )

    $candidate = Get-OptionalPropertyValue -Object $Closeout `
        -Name "candidateCommit"
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve the G9U0 checkout HEAD."

    if ($null -eq $candidate) {
        Assert-Condition -Condition ($Closeout.candidateFreezeStatus -eq
                "PENDING_FIRST_COMMIT" -and
                $Closeout.reconciliationStatus -eq "NOT_STARTED" -and
                $branch -eq $ExpectedBranch -and $head -eq $EntrySha) `
            -Message ("Pre-freeze G9U0 closeout requires the expected feature " +
                "branch at the entry commit.")
        $script:CandidateBoundaryMode = "WORKTREE"
        $script:CandidateCommit = $null
        return
    }

    Assert-Condition -Condition ("$candidate" -cmatch '^[0-9a-f]{40}$' -and
            $Closeout.candidateFreezeStatus -eq "FROZEN" -and
            -not [string]::IsNullOrWhiteSpace(
                "$($Closeout.reconciliationStatus)")) `
        -Message "Frozen G9U0 candidate metadata is incomplete."
    Assert-GitCommitObject -Commit $candidate -Description "G9U0 candidate"
    $candidateRecord = @((& git -C $RepositoryRoot rev-list --parents -n 1 `
        $candidate).Trim() -split '\s+')
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $candidateRecord.Count -eq 2 -and
            $candidateRecord[0] -eq $candidate -and
            $candidateRecord[1] -eq $EntrySha) `
        -Message "G9U0 candidate must have the entry commit as its sole parent."
    $candidateMainMergeBase = (& git -C $RepositoryRoot merge-base $candidate `
        $ConcurrentMainObserved).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $candidateMainMergeBase -eq $EntrySha) `
        -Message "G9U0 candidate and concurrent main no longer meet at entry."
    $promotionMetadataPaths = Get-OptionalPropertyValue -Object $Closeout `
        -Name "promotionMetadataPaths"
    Assert-Condition -Condition ($null -ne $promotionMetadataPaths) `
        -Message "Frozen G9U0 closeout lacks promotion-metadata paths."
    Assert-ExactSet -Actual @($promotionMetadataPaths) `
        -Expected $ExpectedPromotionMetadataPaths `
        -Description "G9U0 promotion-metadata path"

    $script:CandidateBoundaryMode = "COMMIT"
    $script:CandidateCommit = "$candidate"
    if ($head -eq $candidate) {
        Assert-Condition -Condition ($branch -eq $ExpectedBranch) `
            -Message "The frozen candidate HEAD is not on the G9U0 feature branch."
        return
    }

    $mergeHeadOutput = @(& git -C $RepositoryRoot rev-parse -q --verify `
        MERGE_HEAD 2>$null)
    $mergeInProgress = $LASTEXITCODE -eq 0
    $mergeHead = if ($mergeInProgress -and $mergeHeadOutput.Count -eq 1) {
        $mergeHeadOutput[0].Trim()
    } else {
        ""
    }
    if ($mergeInProgress) {
        Assert-Condition -Condition ($head -eq $ConcurrentMainObserved -and
                $mergeHead -eq $candidate) `
            -Message ("A no-commit G9U0 reconciliation must merge the exact " +
                "candidate into the recorded concurrent main.")
        $unmerged = @(& git -C $RepositoryRoot diff --name-only `
            --diff-filter=U)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $unmerged.Count -eq 0) `
            -Message "G9U0 reconciliation still contains unmerged paths."
        $reconciledPaths = @(& git -C $RepositoryRoot diff --name-only `
            --no-renames $candidate --)
    } else {
        $tagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$G9U0TagName").Trim()
        $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
        $peeledPromotion = (& git -C $RepositoryRoot rev-parse `
            "$G9U0TagName^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagObject -eq $G9U0TagObject -and $tagType -eq "tag" -and
                $peeledPromotion -eq $G9U0PromotionCommit) `
            -Message "The annotated G9U0 author-pass tag is not the frozen authority."
        & git -C $RepositoryRoot merge-base --is-ancestor $candidate `
            $G9U0PromotionCommit
        $candidateIsAncestor = $LASTEXITCODE -eq 0
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $ConcurrentMainObserved $G9U0PromotionCommit
        $mainIsAncestor = $LASTEXITCODE -eq 0
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $G9U0PromotionCommit HEAD
        $promotionIsAncestor = $LASTEXITCODE -eq 0
        Assert-Condition -Condition ($candidateIsAncestor -and $mainIsAncestor) `
            -Message "The G9U0 promotion lost the candidate or concurrent main."
        Assert-Condition -Condition $promotionIsAncestor `
            -Message "The current HEAD does not retain the G9U0 promotion commit."
        $promotionRecord = @((& git -C $RepositoryRoot rev-list --parents -n 1 `
            $G9U0PromotionCommit).Trim() -split '\s+')
        Assert-Condition -Condition ($promotionRecord.Count -eq 3 -and
                $promotionRecord[1] -eq $ConcurrentMainObserved -and
                $promotionRecord[2] -eq $candidate) `
            -Message ("The G9U0 promotion commit must have concurrent main and " +
                "the frozen candidate as its ordered parents.")
        $reconciledPaths = @(& git -C $RepositoryRoot diff --name-only `
            --no-renames $candidate $G9U0PromotionCommit --)
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate the reconciled G9U0 tree."
    $expectedReconciledPaths = @($ExpectedMainOnlyPaths +
        $ExpectedPromotionMetadataPaths)
    Assert-ExactSet -Actual @($reconciledPaths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
            $_.Replace("\", "/") }) -Expected $expectedReconciledPaths `
        -Description "Candidate-to-reconciled change"
}

function Get-CandidateChangedPaths {
    if ($CandidateBoundaryMode -eq "COMMIT") {
        return @(Get-GitPathsBetween -From $EntrySha -To $CandidateCommit)
    }

    Assert-Condition -Condition ($CandidateBoundaryMode -eq "WORKTREE") `
        -Message "G9U0 candidate boundary mode was not established."
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only `
            --no-renames $EntrySha --)) {
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

function Assert-OpenInventory {
    param([Parameter(Mandatory)] [object]$Boundary)

    Assert-Condition -Condition ($null -eq $Boundary.totalPaths) `
        -Message "Open G9U0 inventory must not claim a total."
    foreach ($property in @($Boundary.counts.PSObject.Properties)) {
        Assert-Condition -Condition ($null -eq $property.Value) `
            -Message "Open G9U0 inventory must not claim $($property.Name)."
    }
    foreach ($name in @("productivePaths", "testPaths", "testSupportPaths",
            "fixturePaths", "modelPaths", "corpusPaths", "validationPaths",
            "supportingPaths")) {
        Assert-Condition -Condition (@($Boundary.$name).Count -eq 0) `
            -Message "Open G9U0 inventory must not seal $name."
    }
}

function Assert-FrozenInventory {
    param(
        [Parameter(Mandatory)] [object]$Boundary,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    $categories = @("productivePaths", "testPaths", "testSupportPaths",
        "fixturePaths", "modelPaths", "corpusPaths", "validationPaths",
        "supportingPaths")
    $all = @($categories | ForEach-Object { @($Boundary.$_) })
    Assert-ExactSet -Actual $ChangedPaths -Expected $all `
        -Description "G9U0 exact candidate path inventory"
    Assert-Condition -Condition ([int]$Boundary.totalPaths -eq $all.Count -and
            [int]$Boundary.counts.productive -eq
                @($Boundary.productivePaths).Count -and
            [int]$Boundary.counts.tests -eq @($Boundary.testPaths).Count -and
            [int]$Boundary.counts.testSupport -eq
                @($Boundary.testSupportPaths).Count -and
            [int]$Boundary.counts.fixtures -eq @($Boundary.fixturePaths).Count -and
            [int]$Boundary.counts.models -eq @($Boundary.modelPaths).Count -and
            [int]$Boundary.counts.corpus -eq @($Boundary.corpusPaths).Count -and
            [int]$Boundary.counts.validation -eq
                @($Boundary.validationPaths).Count -and
            [int]$Boundary.counts.supporting -eq
                @($Boundary.supportingPaths).Count) `
        -Message "Frozen G9U0 source-boundary counts are inconsistent."
    Assert-Condition -Condition (([int]$Boundary.counts.modified +
            [int]$Boundary.counts.new) -eq $all.Count) `
        -Message "Frozen G9U0 modified/new partition is inconsistent."

    foreach ($forbidden in @(
            "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocus.java",
            "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoLocus.java",
            "source/shared/common/src/main/java/org/geogebra/common/kernel/Path.java")) {
        Assert-Condition -Condition ($forbidden -notin $ChangedPaths) `
            -Message "G9U0 changed a forbidden legacy/Path authority: $forbidden"
    }
    foreach ($path in $ChangedPaths) {
        foreach ($forbiddenScope in @("/kernel3D/", "/geogebra3D/",
                "/export/dxf", "source/web/", "python/", "g9u1-", "g9x1-",
                "g9b-", "g9c-", "g9u2-", "g10")) {
            Assert-Condition -Condition (-not $path.ToLowerInvariant().Contains(
                    $forbiddenScope.ToLowerInvariant())) `
                -Message "G9U0 escaped its authorized source boundary: $path"
        }
    }

    $trackedGenerated = @(& git -C $RepositoryRoot ls-files |
        Where-Object { $_ -match
            '(^|/)(artifacts|build|\.gradle|\.kotlin|__pycache__)(/|$)|\.pyc$' } |
        Where-Object { $_ -ne "artifacts/README.md" })
    Assert-Condition -Condition ($trackedGenerated.Count -eq 0) `
        -Message "Tracked generated artifacts detected: $($trackedGenerated -join ', ')"
}

function Assert-ZeroCounterEvidence {
    param(
        [Parameter(Mandatory)] [object]$Observed,
        [Parameter(Mandatory)] [string[]]$ExpectedNames,
        [Parameter(Mandatory)] [string]$Description
    )

    Assert-ExactSet -Actual @($Observed.PSObject.Properties | ForEach-Object {
            $_.Name
        }) -Expected $ExpectedNames -Description "$Description names"
    foreach ($name in $ExpectedNames) {
        $property = $Observed.PSObject.Properties[$name]
        Assert-Condition -Condition ($null -ne $property.Value -and
                "$($property.Value)" -ceq "0") `
            -Message "$Description must record exact zero for $name."
    }
}

function Assert-FocusedSavedExecution {
    param(
        [Parameter(Mandatory)] [object]$Execution,
        [Parameter(Mandatory)] [string]$ExpectedCommand,
        [Parameter(Mandatory)] [string]$ExpectedLogDirectory,
        [Parameter(Mandatory)] [string]$Description
    )

    Assert-Condition -Condition ($Execution.status -eq "PASSED" -and
            $Execution.command -ceq $ExpectedCommand -and
            $Execution.logDirectory -ceq $ExpectedLogDirectory -and
            [int]$Execution.exitCode -eq 0 -and
            $Execution.buildStatus -eq "BUILD_SUCCESSFUL" -and
            [int]$Execution.commonTests -eq 81 -and
            [int]$Execution.desktopTests -eq 12 -and
            [int]$Execution.tests -eq 93 -and
            [int]$Execution.failures -eq 0 -and
            [int]$Execution.errors -eq 0 -and
            [int]$Execution.skipped -eq 0 -and
            $Execution.sharedMainCheckstyle -eq "CLEAN" -and
            $Execution.sharedTestCheckstyle -eq "CLEAN" -and
            $Execution.desktopMainCheckstyle -eq "CLEAN" -and
            $Execution.verifierOutcome -eq "PASSED_CANDIDATE_ONLY") `
        -Message "$Description saved execution evidence is inconsistent."
}

function Assert-FinalExecutionEvidence {
    param([Parameter(Mandatory)] [object]$Evidence)

    Assert-ExactSet -Actual @($Evidence.requiredHardZeroCounterNames) `
        -Expected $RequiredHardZeroCounterNames `
        -Description "G9U0 required hard-zero counter names"
    Assert-ExactSet -Actual @($Evidence.requiredZeroScopeCounters) `
        -Expected $RequiredZeroScopeCounterNames `
        -Description "G9U0 required zero-scope counter names"

    $claimsFinalExecution = (
        $Evidence.tests.focused.executionStatus -eq "PASSED" -or
        $Evidence.tests.deterministicRerun.executionStatus -eq "PASSED" -or
        $Evidence.tests.composedWithoutSkipBuild.executionStatus -eq "PASSED" -or
        $Evidence.savedExecutions.focusedFinal.status -eq "PASSED" -or
        $Evidence.savedExecutions.focusedDeterministicRerun.status -eq "PASSED" -or
        $Evidence.savedExecutions.composedWithoutSkipBuild.status -eq "PASSED" -or
        $null -ne $Evidence.observedHardZeroCounters -or
        $null -ne $Evidence.observedScopeCounters)
    if (-not $claimsFinalExecution) {
        return
    }

    Assert-Condition -Condition ($Evidence.tests.focused.executionStatus -eq
                "PASSED" -and
            [int]$Evidence.tests.focused.observed.tests -eq 93 -and
            [int]$Evidence.tests.focused.observed.failures -eq 0 -and
            [int]$Evidence.tests.focused.observed.errors -eq 0 -and
            [int]$Evidence.tests.focused.observed.skipped -eq 0 -and
            $Evidence.tests.deterministicRerun.executionStatus -eq "PASSED" -and
            [bool]$Evidence.tests.deterministicRerun.matchesFocused -and
            $Evidence.tests.composedWithoutSkipBuild.executionStatus -eq
                "PASSED" -and
            [int]$Evidence.tests.composedWithoutSkipBuild.exitCode -eq 0 -and
            $Evidence.tests.composedWithoutSkipBuild.terminalOutcome -eq
                "ALL_GEOCEDG_VERIFICATION_GATES_PASSED") `
        -Message "G9U0 final execution totals are not an atomic PASS set."

    Assert-ZeroCounterEvidence -Observed $Evidence.observedHardZeroCounters `
        -ExpectedNames $RequiredHardZeroCounterNames `
        -Description "G9U0 observed hard-zero counters"
    Assert-ZeroCounterEvidence -Observed $Evidence.observedScopeCounters `
        -ExpectedNames $RequiredZeroScopeCounterNames `
        -Description "G9U0 observed zero-scope counters"

    $scenario = Read-JsonFile -RelativePath $ScenarioPath
    $expectedTraceabilityStatus = if ($EvidenceIsApproved) {
        "93_SCENARIOS_EXECUTED_PASSED_AUTHOR_APPROVED"
    } else {
        "93_SCENARIOS_EXECUTED_PASSED_PENDING_AUTHOR_REVIEW"
    }
    Assert-Condition -Condition ($scenario.testExecution.executionStatus -eq
                "PASSED" -and
            $Evidence.documentation.traceabilityStatus -eq
                $expectedTraceabilityStatus) `
        -Message "G9U0 final scenario/documentation evidence is inconsistent."

    Assert-FocusedSavedExecution `
        -Execution $Evidence.savedExecutions.focusedFinal `
        -ExpectedCommand $FocusedFinalCommand `
        -ExpectedLogDirectory $FocusedFinalLogDirectory `
        -Description "G9U0 focused final"
    Assert-FocusedSavedExecution `
        -Execution $Evidence.savedExecutions.focusedDeterministicRerun `
        -ExpectedCommand $FocusedDeterministicCommand `
        -ExpectedLogDirectory $FocusedDeterministicLogDirectory `
        -Description "G9U0 focused deterministic rerun"
    Assert-Condition -Condition (
            [bool]$Evidence.savedExecutions.focusedDeterministicRerun.matchesFocusedFinal) `
        -Message "The deterministic saved execution does not match focused final."

    $composed = $Evidence.savedExecutions.composedWithoutSkipBuild
    Assert-Condition -Condition ($composed.status -eq "PASSED" -and
            $composed.command -ceq $ComposedFinalCommand -and
            $composed.logDirectory -ceq $ComposedFinalLogDirectory -and
            [bool]$composed.withoutSkipBuild -and
            [int]$composed.exitCode -eq 0 -and
            $composed.terminalOutcome -eq
                "ALL_GEOCEDG_VERIFICATION_GATES_PASSED" -and
            [int]$composed.g9u0Tests -eq 93 -and
            [int]$composed.g9u0Failures -eq 0 -and
            [int]$composed.g9u0Errors -eq 0 -and
            [int]$composed.g9u0Skipped -eq 0 -and
            $composed.g9u0Checkstyle -eq "CLEAN" -and
            $composed.verifierOutcome -eq "PASSED_CANDIDATE_ONLY") `
        -Message "G9U0 composed saved execution evidence is inconsistent."
}

function Assert-Evidence {
    param([Parameter(Mandatory)] [object]$Evidence)

    $closeout = Get-OptionalPropertyValue -Object $Evidence -Name "closeout"
    $isPending = ($Evidence.phase -eq "G9U0" -and
            $Evidence.status -eq
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            -not [bool]$Evidence.approval.selfApproved -and
            -not [bool]$Evidence.approval.authorApproved -and
            -not [bool]$Evidence.approval.passClaimed -and
            [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PENDING_AUTHOR_REVIEW" -and
            $null -eq $closeout)
    $isApproved = ($Evidence.phase -eq "G9U0" -and
            $Evidence.status -eq "PASS_AUTHOR_APPROVED" -and
            -not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed -and
            -not [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq "PASS_AUTHOR_APPROVED" -and
            $null -ne $closeout)
    Assert-Condition -Condition ($isPending -xor $isApproved) `
        -Message "G9U0 evidence has a mixed or unsupported approval tuple."
    $script:EvidenceIsApproved = $isApproved

    Assert-Condition -Condition ($Evidence.provenance.entrySha -eq $EntrySha -and
            $Evidence.provenance.branch -eq $ExpectedBranch -and
            $Evidence.provenance.canonicalPromptCanonicalLfSha256 -eq
                $PromptSha256 -and
            $Evidence.provenance.publicSpecificationCanonicalLfSha256 -eq
                $SpecificationSha256 -and
            $Evidence.provenance.adr0013CanonicalLfSha256 -eq $AdrSha256 -and
            $Evidence.provenance.validationMatrixCanonicalLfSha256 -eq
                $MatrixSha256) `
        -Message "G9U0 evidence provenance is inconsistent."
    if ($isApproved) {
        Assert-Condition -Condition (
                $Evidence.provenance.preAuthorReviewEvidenceCanonicalLfSha256 -eq
                    $PreAuthorReviewEvidenceSha256) `
            -Message "The pre-author-review G9U0 evidence anchor drifted."
    }

    $expectedG9U0Disposition = if ($isApproved) {
        "PASS_AUTHOR_APPROVED"
    } else {
        "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW"
    }
    Assert-Condition -Condition ($Evidence.phaseDisposition.G9A3 -eq
            "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.G9A -eq "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.G9U0 -eq
                $expectedG9U0Disposition -and
            $Evidence.phaseDisposition.laterG9Phases -eq
                "NOT_EXECUTED_BY_G9U0" -and
            $Evidence.phaseDisposition.productiveG10 -eq
                "NOT_AUTHORIZED_NOT_EXECUTED") `
        -Message "G9U0 phase disposition is inconsistent."
    $expectedApiStatus = if ($isApproved) {
        "AUTHOR_APPROVED_EXPERIMENTAL_SELECTION"
    } else {
        "IMPLEMENTATION_CANDIDATE_SELECTION_PENDING_AUTHOR_REVIEW"
    }
    Assert-Condition -Condition ($Evidence.apiSelection.status -eq
                $expectedApiStatus `
            -and -not [bool]$Evidence.apiSelection.legacyLocusRedirected `
            -and -not [bool]$Evidence.apiSelection.genericPathAdded `
            -and [bool]$Evidence.apiSelection.mappedScalarSpellingFrozen) `
        -Message "The G9U0 API selection is inconsistent."
    Assert-Condition -Condition ($Evidence.scenarioAuthority.scenarioCount -eq 93 `
            -and $Evidence.scenarioAuthority.commonCount -eq 81 -and
            $Evidence.scenarioAuthority.desktopCount -eq 12) `
        -Message "G9U0 evidence scenario totals drifted."
    Assert-Condition -Condition ($Evidence.tests.focused.expected.tests -eq 93 `
            -and $Evidence.tests.focused.expected.failures -eq 0 -and
            $Evidence.tests.focused.expected.errors -eq 0 -and
            $Evidence.tests.focused.expected.skipped -eq 0) `
        -Message "G9U0 evidence expected test totals drifted."
    $expectedCorpusStatus = if ($Evidence.sourceBoundary.inventoryStatus -eq
            "FROZEN") {
        "IMPLEMENTATION_CANDIDATE_CORPUS_FROZEN"
    } else {
        "IMPLEMENTATION_CANDIDATE_CORPUS_PROVISIONAL"
    }
    $expectedDesignStatus = if ($isApproved) {
        "PASS_AUTHOR_APPROVED"
    } else {
        "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW"
    }
    $expectedTraceabilityStatus = if ($isApproved) {
        "93_SCENARIOS_EXECUTED_PASSED_AUTHOR_APPROVED"
    } else {
        "93_SCENARIOS_EXECUTED_PASSED_PENDING_AUTHOR_REVIEW"
    }
    $expectedGuideDisposition = if ($isApproved) {
        "UPDATED_FOR_EXPERIMENTAL_DEFAULT_OFF_AUTHOR_APPROVED_PUBLIC_SURFACE"
    } else {
        "UPDATED_FOR_EXPERIMENTAL_DEFAULT_OFF_PUBLIC_CANDIDATE"
    }
    Assert-Condition -Condition ($Evidence.compatibilityCorpus.status -eq
            $expectedCorpusStatus -and
            [int]$Evidence.compatibilityCorpus.entryCount -eq 13 -and
            $Evidence.compatibilityCorpus.externalUpstreamRuntimeEvidence -eq
                "NOT_EXECUTED_OR_CLAIMED" -and
            -not [bool]$Evidence.compatibilityCorpus.lossyConversionAuthorized -and
            $Evidence.documentation.implementationDesignStatus -eq
                $expectedDesignStatus -and
            $Evidence.documentation.apiStatus -eq $expectedDesignStatus -and
            $Evidence.documentation.traceabilityStatus -eq
                $expectedTraceabilityStatus -and
            [bool]$Evidence.documentation.userGuideReviewed -and
            [bool]$Evidence.documentation.userGuideChanged -and
            $Evidence.documentation.userGuideDisposition -eq
                $expectedGuideDisposition) `
        -Message "G9U0 corpus or user-guide evidence is inconsistent."

    if ($isApproved) {
        Assert-ConcurrentMainCloseout -Evidence $Evidence -Closeout $closeout
        Assert-CandidateCloseoutBoundary -Evidence $Evidence -Closeout $closeout
    } else {
        $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
        $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $branch -eq $ExpectedBranch -and $head -eq $EntrySha) `
            -Message ("Pending G9U0 evidence is valid only on the expected " +
                "feature branch at its entry commit.")
        $script:CandidateBoundaryMode = "WORKTREE"
        $script:CandidateCommit = $null
    }
    Assert-FinalExecutionEvidence -Evidence $Evidence
}

function Assert-CompatibilityCorpus {
    param([Parameter(Mandatory)] [bool]$RequireFrozen)

    $corpus = Read-JsonFile -RelativePath $CorpusPath
    $expectedStatus = if ($RequireFrozen) {
        "IMPLEMENTATION_CANDIDATE_CORPUS_FROZEN"
    } else {
        "IMPLEMENTATION_CANDIDATE_CORPUS_PROVISIONAL"
    }
    Assert-Condition -Condition ($corpus.phase -eq "G9U0" -and
            $corpus.status -eq $expectedStatus -and
            -not [bool]$corpus.authorApprovalClaimed -and
            [bool]$corpus.hashesFrozen -eq $RequireFrozen -and
            -not [bool]$corpus.boundary.lossyConversionAuthorized -and
            $corpus.boundary.externalUpstreamRuntimeEvidence -eq
                "NOT_EXECUTED_OR_CLAIMED_BY_THIS_CORPUS" -and
            @($corpus.entries).Count -eq 13 -and
            [int]$corpus.entryCount -eq 13) `
        -Message "The G9U0 compatibility corpus is inconsistent."
    Assert-HashManifest -RelativePath $CorpusHashPath `
        -ExpectedTargets @($corpus.entries | ForEach-Object { $_.path })
    Assert-ExactSet -Actual @($corpus.entries | ForEach-Object { $_.id }) `
        -Expected @(
            "G9P-GENERAL", "G9P-GRAFT", "G9P-FOCAL", "G9P-CONNECTIONS",
            "G6-LEGACY-THREE-LEVEL", "G6-LEGACY-TWO-LEVEL",
            "G9U0-PUBLIC-SURFACE-MANIFEST", "G9U0-FUTURE-PROVIDER",
            "G9U0-MISSING-GENERATOR", "G9U0-MISSING-SUPPORT",
            "G9U0-MISSING-TOKEN", "G9U0-DUPLICATE-ID",
            "G9U0-EXTERNAL-UPSTREAM-NO-DOWNGRADE") `
        -Description "G9U0 compatibility corpus IDs"
    foreach ($entry in @($corpus.entries)) {
        $actual = if ($entry.path.EndsWith(".ggb",
                [StringComparison]::OrdinalIgnoreCase)) {
            Get-BinarySha256 -RelativePath $entry.path
        } else {
            Get-CanonicalTextSha256 -RelativePath $entry.path
        }
        Assert-Condition -Condition ($actual -eq $entry.sha256) `
            -Message "Corpus JSON SHA-256 mismatch for $($entry.path)."
    }
    $manifest = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0/public-surface-provider-query-manifest.xml")
    Assert-Condition -Condition ($manifest.Contains(
            "not native GeoGebra construction XML") -and
            -not $manifest.Contains("<geogebra")) `
        -Message "The provider/query manifest falsely claims native construction XML."
}

function Assert-SourceContracts {
    $testPaths = @($ExpectedTestCounts.Keys | ForEach-Object {
            Get-TestSourcePath -ClassName $_
        })
    foreach ($path in $testPaths) {
        $source = Get-Content -Raw -LiteralPath (Resolve-RequiredFile $path)
        Assert-Condition -Condition ($source -notmatch
                '@Disabled|@Ignore|TODO|FIXME') `
            -Message "Focused G9U0 source contains a disabled/TODO seam: $path"
    }

    $facade = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java")
    foreach ($operation in @("createPointDriven", "createScalar",
            "createSemanticPoint", "totalMetric", "betweenMetric",
            "scalarLength", "intersect", "selectIntersectionPoint")) {
        Assert-Condition -Condition ($facade -match
                "public static [^{;]+\b$operation\s*\(") `
            -Message "Public G9U0 facade operation is missing: $operation"
    }

    $pointAlgo = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java")
    Assert-Condition -Condition ($pointAlgo.Contains(
            "findExactPointAdmissibleSolution") -and
            $pointAlgo.Contains("rebaseCopiedPointAdmissibleSolution") -and
            -not $pointAlgo.Contains("findPointAdmissibleSolutionByLineage")) `
        -Message "Token-point selection contains a non-ledger lineage fallback."

    $tokenCodec = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusSemanticIntersectionToken2D.java")
    Assert-Condition -Condition ($tokenCodec.Contains(
            'private static final String PREFIX = "locus-root/v3/"') -and
            $tokenCodec.Contains('"locus-root-local/v1/"') -and
            $tokenCodec.Contains("createRevisionLocalHandle") -and
            $tokenCodec.Contains("isRevisionLocalHandle") -and
            $tokenCodec.Contains("sha256Hex(material.getBytes") -and
            $tokenCodec.Contains("long incarnation")) `
        -Message ("The durable digest/incarnation codec or its explicit " +
            "non-durable revision-local handle boundary is absent.")
    $createBody = [regex]::Match($tokenCodec,
        '(?s)public static String create\(.+?\n\t\}').Value
    Assert-Condition -Condition ($createBody -notmatch
            'getEvaluatedPoint|getSemanticParameter|candidateIndex|sampleIndex|ordinal') `
        -Message "Token material contains coordinate/order-derived identity."

    $ledger = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java")
    foreach ($contract in @("prepareAttachedOwner", "rebaseCopiedToken",
            "validatePreattachmentContext", "validatePersistedCopySource",
            "validatesCurrentToken", "revisionLocalHandle", "exportState",
            "importState", "burnedSemanticKeys")) {
        Assert-Condition -Condition ($ledger.Contains($contract)) `
            -Message "Persistent token-ledger contract is missing: $contract"
    }
    Assert-Condition -Condition ($ledger.Contains(
            "entry.addressProof.equals(addressProof)") -and
            $ledger.Contains("Math.addExact(nextIncarnation, 1)") -and
            $ledger.Contains("duplicateSemanticKeys") -and
            $ledger.Contains(
                "parsedCurrent == null && parsedCopySource != null")) `
        -Message ("Token-ledger exact-address continuation, overflow " +
            "preflight or strict copy-source import validation is missing.")

    $publicTarget = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicTargetIntersectionCapability2D.java")
    Assert-Condition -Condition ($publicTarget.Contains(
            "IntersectionTokenLineage2D.stableComponentLineage") -and
            $publicTarget.Contains("G8_UNIQUE_ROOT_PREFIX") -and
            $publicTarget.Contains("DURABLE_UNIQUE_ROOT_PREFIX") -and
            $publicTarget.Contains("expectedProof.equals(delegatedProof)") -and
            $publicTarget.Contains(
                'DURABLE_UNIQUE_ROOT_PREFIX + lineage.length() + ":" + lineage')) `
        -Message ("Public target continuation must validate the delegated " +
            "unique-root proof before using stable semantic component lineage.")

    $intersectionTests = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0IntersectionTokenTest.java")
    foreach ($requiredFragment in @(
            "G8BIntersectionFixtures::completeEmpty",
            "G8BIntersectionFixtures.completeRoots",
            "G8BIntersectionFixtures.overlap",
            "GeometryKind.MIXED_FINITE_OVERLAP",
            "u0-i15/duplicate-semantic/v1",
            "LocusSemanticIntersectionToken2D::isRevisionLocalHandle",
            "LocusSemanticIntersectionToken2D.decode(handle).isEmpty()",
            "result.findExactPointAdmissibleSolution(handle).isEmpty()",
            "value(duplicate).findPointAdmissibleSolutionByLineage(",
            "firstDuplicate.getIdentity().getExplicitContinuationKey()",
            "assertEquals(before, after)",
            "assertNotEquals(uniqueBefore, uniqueAfter)",
            "assertFalse(oldUniquePoint.isDefined())")) {
        Assert-Condition -Condition ($intersectionTests.Contains(
                $requiredFragment)) `
            -Message "Focused intersection evidence is missing: $requiredFragment"
    }

    $metricTotal = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java")
    $metricBetween = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusBetweenMetricV2.java")
    Assert-Condition -Condition ($metricTotal.Contains(
            'current.toExternalForm() + "/total-metric-consumer"') -and
            $metricBetween.Contains(
                'current.toExternalForm() + "/between-metric-consumer"')) `
        -Message "Public metric consumer IDs are not derived from durable results."

    $rich = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java")
    Assert-Condition -Condition ($rich.Contains(
            'startTag("locusIntersectionTokenLedger")') -and
            $rich.Contains("implements PersistentGeoIdentityListener") -and
            $rich.Contains("validatePersistentGeoIdentityAttachment") -and
            $rich.Contains("validatePreattachmentContext")) `
        -Message ("Rich intersection XML/listener persistence or atomic " +
            "preattachment validation seam is missing.")

    $scalar = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java")
    Assert-Condition -Condition ($scalar -match
            'GeoLocusMetricResult\s+rich(?:Input|Result)' -and
            $scalar -notmatch 'new LocusMetricEngine2D|new LocusMetricIndex') `
        -Message "Scalar Length no longer depends solely on its rich parent."

    foreach ($fixture in @("public-surface-provider-query-manifest.xml", "future-provider.xml",
            "missing-generator.xml", "missing-support.xml", "missing-token.xml",
            "duplicate-id.xml", "external-upstream-no-downgrade.xml")) {
        [void](Resolve-RequiredFile -RelativePath `
            "source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0/$fixture")
    }
    foreach ($frontendPath in @(
            "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java",
            "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLocusV2Dialogs.java",
            "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGExternalCompatibilityWarning.java",
            "source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGProfileTest.java")) {
        [void](Resolve-RequiredFile -RelativePath $frontendPath)
    }

    $guide = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "docs/user/geocedg_user_guide.md")
    $guideStatusFragment = if ($EvidenceIsApproved) {
        "G9U0 = PASS — AUTHOR APPROVED"
    } else {
        "G9U0 **IMPLEMENTATION CANDIDATE"
    }
    foreach ($guideFragment in @(
            $guideStatusFragment,
            "--enableLocusV2=true", "LocusV2[Q,P]", "LocusLength[L]",
            'Intersect[R,"token"]', "GeoCeDG Classic")) {
        Assert-Condition -Condition ($guide.Contains($guideFragment)) `
            -Message "The user guide is missing the G9U0 boundary: $guideFragment"
    }
    if ($EvidenceIsApproved) {
        $statusDocuments = @($Documents + "docs/user/geocedg_user_guide.md")
        foreach ($statusDocument in $statusDocuments) {
            $content = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
                $statusDocument)
            Assert-Condition -Condition ($content.Contains(
                    "G9U0 = PASS — AUTHOR APPROVED") -and
                    $content -notmatch '(?i)pending author review') `
                -Message ("Approved G9U0 documentation is stale: " +
                    $statusDocument)
        }
    }
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
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "$Description failed with exit code $exitCode. See $logPath"
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $root = if ($ClassName.StartsWith("org.geocedg.common.")) {
        "source/shared/common-jre/build/test-results/test"
    } else {
        "source/desktop/desktop/build/test-results/test"
    }
    $path = Resolve-RequiredFile -RelativePath "$root/TEST-${ClassName}.xml"
    [xml]$result = Get-Content -Raw -LiteralPath $path
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("{0} is not clean: tests={1}, failures={2}, errors={3}, " +
            "skipped={4}." -f $ClassName, $suite.tests, $suite.failures,
            $suite.errors, $suite.skipped)
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle violations."
}

try {
    foreach ($required in $RequiredPaths) {
        [void](Resolve-RequiredFile -RelativePath $required)
    }
    $InitialStatus = (& git -C $RepositoryRoot status --short) -join "`n"
    Assert-Authority
    Assert-MarkdownLinks
    Assert-HashManifest -RelativePath $EvidenceHashPath `
        -ExpectedTargets @($EvidencePath)
    $evidence = Read-JsonFile -RelativePath $EvidencePath
    Assert-Evidence -Evidence $evidence
    $inventoryFrozen = $evidence.sourceBoundary.inventoryStatus -eq "FROZEN"
    Assert-Condition -Condition ($inventoryFrozen -or
            $evidence.sourceBoundary.inventoryStatus -eq
                "OPEN_PENDING_SOURCE_FREEZE") `
        -Message "Unknown G9U0 source inventory state."
    Assert-CompatibilityCorpus -RequireFrozen:$inventoryFrozen
    Assert-ScenarioAuthority -RequireTestSources:$true
    Assert-SourceContracts

    if (-not $inventoryFrozen) {
        Assert-OpenInventory -Boundary $evidence.sourceBoundary
        Assert-Condition -Condition ($evidence.tests.focused.executionStatus -eq
                "NOT_EXECUTED" -and
                $evidence.tests.deterministicRerun.executionStatus -eq
                    "NOT_EXECUTED" -and
                $evidence.tests.composedWithoutSkipBuild.executionStatus -eq
                    "NOT_EXECUTED") `
            -Message "Open G9U0 evidence must not claim executed tests."
        Write-Host "G9U0 validation scaffold is internally consistent."
        Write-Host "Productive inventory remains open; no implementation PASS is claimed."
    } else {
        $changedPaths = @(Get-CandidateChangedPaths)
        Assert-FrozenInventory -Boundary $evidence.sourceBoundary `
            -ChangedPaths $changedPaths
        if (-not $SkipBuild) {
            [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
            $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "g9u0-public-surface"
            Invoke-LoggedGradle -LogName "g9u0-common.log" `
                -Description "G9U0 shared command, generator and lifecycle tests" `
                -Arguments @(
                    ":shared:common-jre:test", "--tests",
                    "org.geocedg.common.locus.G9U0*",
                    ":shared:common:checkstyleMain",
                    ":shared:common-jre:checkstyleTest",
                    "--rerun-tasks", "--no-daemon", "--console=plain",
                    "--no-problems-report"
                )
            Invoke-LoggedGradle -LogName "g9u0-desktop.log" `
                -Description "G9U0 runtime feature, localization and tool tests" `
                -Arguments @(
                    ":desktop:desktop:test", "--tests",
                    "org.geocedg.desktop.locus.G9U0*",
                    ":desktop:desktop:checkstyleMain",
                    "--rerun-tasks", "--no-daemon", "--console=plain",
                    "--no-problems-report"
                )
            foreach ($entry in $ExpectedTestCounts.GetEnumerator()) {
                Assert-TestResult -ClassName $entry.Key `
                    -ExpectedTests ([int]$entry.Value)
            }
            Assert-CheckstyleResult -RelativePath `
                "source/shared/common/build/reports/checkstyle/main.xml"
            Assert-CheckstyleResult -RelativePath `
                "source/shared/common-jre/build/reports/checkstyle/test.xml"
            Assert-CheckstyleResult -RelativePath `
                "source/desktop/desktop/build/reports/checkstyle/main.xml"
        } else {
            Write-Host "Skipping G9U0 Gradle probes because -SkipBuild was supplied."
        }
        if ($SkipBuild) {
            if ($evidence.tests.focused.executionStatus -eq "PASSED") {
                Write-Host ("G9U0 frozen source boundary passed static " +
                    "validation. This invocation did not rerun Gradle; sealed " +
                    "evidence records two clean 93/93 focused executions and " +
                    "a composed no-SkipBuild PASS.")
            } else {
                Write-Host ("G9U0 frozen source boundary passed static " +
                    "validation; the 93 tests are not yet claimed.")
            }
        } else {
            $verifiedDisposition = if ($EvidenceIsApproved) {
                "author-approved"
            } else {
                "implementation-candidate"
            }
            Write-Host ("G9U0 $verifiedDisposition verification passed " +
                "(93/93 scenarios).")
        }
        if ($EvidenceIsApproved) {
            Write-Host "G9U0 = PASS — AUTHOR APPROVED."
            Write-Host ("Saved executions retain their candidate-only " +
                "provenance; no later G9/G10 phase is claimed.")
        } else {
            Write-Host "Author approval and PASS are not claimed."
        }
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G9U0 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = (& git -C $RepositoryRoot status --short) -join "`n"
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G9U0 verification."
            exit 1
        }
    }
}

exit 0

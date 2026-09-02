[CmdletBinding()]
param(
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u1-preexecution-design")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$ExpectedBranch = "feature/g9u1-construction-workspace-planning-after-r6"
$G9S1PassTagName = "geocedg-g9s1-pass"
$G9S1PassTagObject = "ece0ca6f00299d3347e57fac38b7a28cade28644"
$G9S1PassCommit = "de33f3a80102adb051aaa7547a72b7e97409c58c"
$R6PassTagName = "geocedg-g9u0-r6-pass"
$R6PassTagObject = "2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e"
$R6PassCommit = "3942af594e4507e479f2c75019cef62e3d9fea6f"
$ProtectedPlanningBranch = "feature/g9u1-construction-workspace-planning"
$ProtectedPlanningCommit = "857de6628489bda0b65a5ba5145e62ca0795fc32"
$LiveV1ProfilePath = "apps/geocedg/application-profile.yml"
$LiveV1ProfileBlob = "fc2a3ebd128fc79ca76840bc391598221bfa02c6"
$HistoricalPromptCanonicalLfSha256 =
    "2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322"
$PeriodicRisk = "G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP"

$PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r6.prompt.md"
$HistoricalPromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md"
$SchemaPath = "geocedg/specs/ui/application-profile-v2.candidate.schema.json"
$ManifestPath = "geocedg/specs/ui/application-profile-v2.candidate.yml"
$InteractionSpecPath = "geocedg/specs/ui/g9u1-construction-interaction.md"
$WorkspaceSpecPath = "geocedg/specs/ui/cedg-workspaces.md"
$ArchitecturePath = "docs/architecture/cedg_workspace_architecture.md"
$CompletenessMatrixPath =
    "docs/validation/g9u1_workspace_completeness_matrix.md"
$CommandMatrixPath =
    "docs/validation/g9u1_command_tool_consistency_matrix.md"
$PublicMatrixPath = "docs/validation/g9_public_workspace_validation_matrix.md"
$RoadmapPath = "docs/roadmap/geocedg_roadmap.md"
$TraceabilityPath = "docs/validation/g9_documentation_bundle_traceability.md"
$SpecsIndexPath = "geocedg/specs/README.md"
$ReportPath = "docs/validation/g9u1_preexecution_design_candidate_report.md"
$ScenarioPath =
    "geocedg/validation/g9u1/g9u1-preexecution-scenarios.json"
$EvidencePath =
    "geocedg/validation/g9u1/g9u1-preexecution-design-evidence.json"
$EvidenceManifestPath =
    "geocedg/validation/g9u1/g9u1-preexecution-design-evidence.sha256"
$VerifierRelativePath = "tools/agent/verify-g9u1-preexecution-design.ps1"
$ComposedVerifierPath = "tools/agent/verify.ps1"
$SummaryLog = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
    "g9u1-preexecution-design.log"

$RequiredArtifacts = @(
    $PromptPath,
    $HistoricalPromptPath,
    $SchemaPath,
    $ManifestPath,
    $InteractionSpecPath,
    $WorkspaceSpecPath,
    $ArchitecturePath,
    $CompletenessMatrixPath,
    $CommandMatrixPath,
    $PublicMatrixPath,
    $RoadmapPath,
    $TraceabilityPath,
    $SpecsIndexPath,
    $ReportPath,
    $ScenarioPath,
    $EvidencePath,
    $EvidenceManifestPath,
    $VerifierRelativePath
)

# This is the complete planning-candidate boundary. In particular, the live v1
# profile and the composed verifier are deliberately absent.
$ExpectedCandidatePaths = @($RequiredArtifacts | Sort-Object -Unique)

$ExpectedScenarioGroups = [ordered]@{
    "U1-S" = 5
    "U1-W" = 17
    "U1-R5" = 7
    "U1-S1" = 6
    "U1-D" = 8
    "U1-L" = 2
    "U1-V" = 2
    "U1-I" = 14
    "U1-B" = 3
    "U1-A" = 4
    "U1-G" = 3
    "U1-C" = 20
    "U1-Q" = 5
    "U1-P" = 2
    "U1-PNT" = 20
}
$ExpectedScenarioCount = 118
$ExpectedBroadFamilyCount = 11
$ExpectedOperationalClusterCount = 18
$ExpectedStableActionCount = 110
$ExpectedEvidenceEntryCount = $RequiredArtifacts.Count - 1

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RepositoryPath {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $fullPath = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $RelativePath))
    $rootPrefix = $RepositoryRoot.TrimEnd('\', '/') +
        [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($fullPath.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Path escapes repository root: $RelativePath"
    return $fullPath
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $fullPath = Resolve-RepositoryPath -RelativePath $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $fullPath -PathType Leaf) `
        -Message "Required G9U1 planning artifact is missing: $RelativePath"
    return $fullPath
}

function Get-CanonicalLfSha256FromBytes {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        $Bytes, $offset, $Bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes(
                $canonical))).ToLowerInvariant()
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    return Get-CanonicalLfSha256FromBytes -Bytes (
        [IO.File]::ReadAllBytes($path))
}

function Get-RequiredText {
    param([Parameter(Mandatory)] [string]$RelativePath)

    return [IO.File]::ReadAllText(
        (Resolve-RequiredFile -RelativePath $RelativePath),
        [Text.UTF8Encoding]::new($false, $true))
}

function Get-RequiredJson {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return Get-RequiredText -RelativePath $RelativePath | ConvertFrom-Json
    } catch {
        throw "Invalid JSON/JSON-compatible YAML in ${RelativePath}: " +
            $_.Exception.Message
    }
}

function Assert-Contains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$RequiredText
    )

    $content = Get-RequiredText -RelativePath $RelativePath
    foreach ($text in $RequiredText) {
        Assert-Condition -Condition ($content.Contains($text)) `
            -Message "$RelativePath does not contain required text: $text"
    }
}

function Assert-NotContains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$ForbiddenText
    )

    $content = Get-RequiredText -RelativePath $RelativePath
    foreach ($text in $ForbiddenText) {
        Assert-Condition -Condition (-not $content.Contains($text)) `
            -Message "$RelativePath contains forbidden text: $text"
    }
}

function Get-ChangedPaths {
    $paths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($commandOutput in @(
            @(& git -C $RepositoryRoot diff --name-only --no-renames HEAD --),
            @(& git -C $RepositoryRoot diff --cached --name-only --no-renames --),
            @(& git -C $RepositoryRoot ls-files --others --exclude-standard --))) {
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to enumerate the G9U1 planning-candidate paths."
        foreach ($path in $commandOutput) {
            if (-not [string]::IsNullOrWhiteSpace($path)) {
                [void]$paths.Add($path.Replace("\", "/"))
            }
        }
    }
    return @($paths | Sort-Object)
}

function Assert-PlanningScope {
    $changedPaths = @(Get-ChangedPaths)
    $expected = @($ExpectedCandidatePaths | Sort-Object)
    Assert-Condition -Condition ($changedPaths.Count -eq $expected.Count) `
        -Message ("Unexpected G9U1 planning inventory count: expected " +
            "$($expected.Count), actual $($changedPaths.Count).")
    for ($index = 0; $index -lt $expected.Count; $index++) {
        Assert-Condition -Condition ($changedPaths[$index] -eq $expected[$index]) `
            -Message ("G9U1 planning inventory mismatch at $index`: expected " +
                "'$($expected[$index])', actual '$($changedPaths[$index])'.")
    }

    foreach ($path in $changedPaths) {
        foreach ($forbiddenPrefix in @(
                "source/", "apps/", "packaging/", "gradle/", "python/",
                "geocedg/features/", "geocedg/resources/", "artifacts/")) {
            Assert-Condition -Condition (-not $path.StartsWith(
                    $forbiddenPrefix, [StringComparison]::OrdinalIgnoreCase)) `
                -Message "Productive/generated path changed in G9U1 planning: $path"
        }
        Assert-Condition -Condition ($path -notmatch
                '\.(java|kt|kts|gradle|class|jar|exe|dll|msi)$') `
            -Message "Productive or binary path changed in G9U1 planning: $path"
    }

    & git -C $RepositoryRoot diff --quiet HEAD -- $LiveV1ProfilePath
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The live schema-v1 product profile changed during planning."
    $liveBlob = (& git -C $RepositoryRoot rev-parse `
        "HEAD`:$LiveV1ProfilePath").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $liveBlob -eq $LiveV1ProfileBlob) `
        -Message "The live schema-v1 profile authority is not the sealed post-R6 blob."
    & git -C $RepositoryRoot diff --quiet HEAD -- $ComposedVerifierPath
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The unapproved planning verifier was inserted into verify.ps1."
    & git -C $RepositoryRoot diff --cached --quiet
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The G9U1 preexecution planning candidate must have an empty index."

    return $changedPaths.Count
}

function Assert-ApprovedEntryAuthority {
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $branch -eq $ExpectedBranch) `
        -Message "Unexpected planning branch: '$branch'."

    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $head -eq $R6PassCommit) `
        -Message "Post-R6 G9U1 planning does not start at the approved R6 commit."
    $tagType = (& git -C $RepositoryRoot cat-file -t `
        $G9S1PassTagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagType -eq "tag") `
        -Message "The G9S1 PASS tag object is missing or not annotated."
    $tagRef = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$G9S1PassTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagRef -eq $G9S1PassTagObject) `
        -Message "The G9S1 PASS tag reference changed."
    $tagPeel = (& git -C $RepositoryRoot rev-parse `
        "$G9S1PassTagObject^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagPeel -eq $G9S1PassCommit) `
        -Message "The G9S1 PASS tag does not peel to the approved commit."

    $r6TagType = (& git -C $RepositoryRoot cat-file -t `
        $R6PassTagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $r6TagType -eq "tag") `
        -Message "The R6 PASS tag object is missing or not annotated."
    $r6TagRef = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$R6PassTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $r6TagRef -eq $R6PassTagObject) `
        -Message "The R6 PASS tag reference changed."
    $r6TagPeel = (& git -C $RepositoryRoot rev-parse `
        "$R6PassTagObject^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $r6TagPeel -eq $R6PassCommit) `
        -Message "The R6 PASS tag does not peel to the approved commit."
    & git -C $RepositoryRoot merge-base --is-ancestor `
        $G9S1PassCommit $R6PassCommit
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The approved G9S1 commit is not an ancestor of R6."

    foreach ($planningRef in @(
            $ProtectedPlanningBranch,
            "origin/$ProtectedPlanningBranch")) {
        $planningCommit = (& git -C $RepositoryRoot rev-parse `
            $planningRef).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $planningCommit -eq $ProtectedPlanningCommit) `
            -Message "Protected pre-R6 planning ref changed: $planningRef."
    }

    $historicalPromptHash = Get-CanonicalLfSha256 `
        -RelativePath $HistoricalPromptPath
    Assert-Condition -Condition ($historicalPromptHash -eq
            $HistoricalPromptCanonicalLfSha256) `
        -Message "The protected post-G9S1 prompt content changed."
}

function Assert-ManifestContract {
    $schema = Get-RequiredJson -RelativePath $SchemaPath
    $manifest = Get-RequiredJson -RelativePath $ManifestPath

    Assert-Condition -Condition ($schema.'$schema' -eq
            "https://json-schema.org/draft/2020-12/schema" -and
            $schema.properties.schema_version.const -eq 2 -and
            $schema.properties.status.const -eq
                "design-approved-not-runtime") `
        -Message "The candidate schema header/status is inconsistent."
    Assert-Condition -Condition ($manifest.schema_version -eq 2 -and
            $manifest.status -eq "design-approved-not-runtime" -and
            $manifest.'$schema' -eq
                "application-profile-v2.candidate.schema.json" -and
            $manifest.live_profile.path -eq $LiveV1ProfilePath -and
            $manifest.live_profile.schema_version -eq 1 -and
            $manifest.live_profile.policy -eq
                "unchanged-until-authorized-g9u1-implementation") `
        -Message "The schema-v2 candidate does not preserve the live-v1 boundary."

    $families = @($manifest.taxonomy.broad_families)
    $taxonomyClusters = @($manifest.taxonomy.operational_clusters)
    $clusters = @($manifest.clusters)
    $actions = @($manifest.actions)
    Assert-Condition -Condition ($families.Count -eq
            $ExpectedBroadFamilyCount) `
        -Message "Expected 11 broad workspace families; found $($families.Count)."
    Assert-Condition -Condition ($taxonomyClusters.Count -eq
            $ExpectedOperationalClusterCount -and
            $clusters.Count -eq $ExpectedOperationalClusterCount) `
        -Message "Expected 18 operational clusters in taxonomy and placement."
    Assert-Condition -Condition ($actions.Count -eq $ExpectedStableActionCount) `
        -Message ("Expected 110 stable actions; found $($actions.Count).")

    $familyIds = @($families | ForEach-Object { $_.id })
    $taxonomyClusterIds = @($taxonomyClusters | ForEach-Object { $_.id })
    $clusterIds = @($clusters | ForEach-Object { $_.id })
    $actionIds = @($actions | ForEach-Object { $_.id })
    foreach ($identitySet in @(
            @($familyIds), @($taxonomyClusterIds), @($clusterIds),
            @($actionIds))) {
        Assert-Condition -Condition (($identitySet | Sort-Object -Unique).Count -eq
                $identitySet.Count) `
            -Message "Duplicate stable ID in the candidate profile."
    }
    Assert-Condition -Condition (@(Compare-Object `
                ($taxonomyClusterIds | Sort-Object) `
                ($clusterIds | Sort-Object)).Count -eq 0) `
        -Message "Taxonomy clusters and placement clusters do not match."
    Assert-Condition -Condition (@(Compare-Object `
                (1..$ExpectedBroadFamilyCount) `
                @($families.order | Sort-Object)).Count -eq 0) `
        -Message "Broad-family order is not exactly 1..11."
    Assert-Condition -Condition (@(Compare-Object `
                (1..$ExpectedOperationalClusterCount) `
                @($taxonomyClusters.order | Sort-Object)).Count -eq 0) `
        -Message "Operational-cluster order is not exactly 1..18."

    foreach ($cluster in $taxonomyClusters) {
        Assert-Condition -Condition ($familyIds -contains
                $cluster.broad_family_id) `
            -Message "Unknown broad family for taxonomy cluster $($cluster.id)."
    }
    $referencedActionIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($cluster in $clusters) {
        Assert-Condition -Condition ($familyIds -contains
                $cluster.broad_family_id) `
            -Message "Unknown broad family for placement cluster $($cluster.id)."
        foreach ($property in @(
                "toolbar_action_ids", "overflow_action_ids", "menu_action_ids",
                "context_action_ids", "inspector_action_ids",
                "settings_action_ids")) {
            foreach ($actionId in @($cluster.$property)) {
                Assert-Condition -Condition ($actionIds -contains $actionId) `
                    -Message "Unknown action '$actionId' in cluster $($cluster.id)."
                [void]$referencedActionIds.Add([string]$actionId)
            }
        }
    }

    $effectIds = @($manifest.policy_profiles.effects.id)
    $availabilityIds = @($manifest.policy_profiles.availability.id)
    $commandSurfaceIds = @($manifest.policy_profiles.command_surfaces.id)
    $featureIds = @($manifest.features.id)
    foreach ($action in $actions) {
        Assert-Condition -Condition ($effectIds -contains
                $action.effect_profile_id) `
            -Message "Unknown effect policy on action $($action.id)."
        Assert-Condition -Condition ($availabilityIds -contains
                $action.availability_profile_id) `
            -Message "Unknown availability policy on action $($action.id)."
        Assert-Condition -Condition ($commandSurfaceIds -contains
                $action.command_surface_profile_id) `
            -Message "Unknown command-surface policy on action $($action.id)."
        foreach ($feature in @($action.feature_requirements)) {
            Assert-Condition -Condition ($featureIds -contains $feature) `
                -Message "Unknown feature '$feature' on action $($action.id)."
        }
    }
    foreach ($route in @($manifest.diagnostic_routes)) {
        Assert-Condition -Condition ($actionIds -contains $route.action_id) `
            -Message "Unknown diagnostic-route action: $($route.action_id)."
        [void]$referencedActionIds.Add([string]$route.action_id)
    }
    foreach ($action in $actions) {
        if ($action.planning_disposition -ne "deferred" -and
                $action.planning_disposition -ne
                    "out-of-scope-new-kernel-phase") {
            Assert-Condition -Condition ($referencedActionIds.Contains(
                    [string]$action.id)) `
                -Message "Active action is unreachable from all clusters: $($action.id)."
        }
    }

    foreach ($workspace in @($manifest.workspaces)) {
        foreach ($familyId in @($workspace.toolbar_broad_family_ids)) {
            Assert-Condition -Condition ($familyIds -contains $familyId) `
                -Message "Unknown workspace broad family: $familyId."
        }
        foreach ($clusterId in @($workspace.menu_cluster_ids)) {
            Assert-Condition -Condition ($clusterIds -contains $clusterId) `
                -Message "Unknown workspace cluster: $clusterId."
        }
    }

    Assert-Condition -Condition (
            $manifest.product_policies.continuity.host_setting -eq
                "continuity" -and
            -not [bool]$manifest.product_policies.continuity.value -and
            [bool]$manifest.product_policies.continuity.locked) `
        -Message "GeoCeDG Continuity OFF is not a locked reuse of the host setting."
    Assert-Condition -Condition (
            @($manifest.product_policies.languages.offered).Count -eq 2 -and
            $manifest.product_policies.languages.offered[0] -eq "en" -and
            $manifest.product_policies.languages.offered[1] -eq "es" -and
            $manifest.product_policies.languages.fallback -eq "en") `
        -Message "The required English/Spanish product locale policy drifted."
    Assert-Condition -Condition (
            -not [bool]$manifest.product_policies.materialization.
                kernel_recompute_creates_new_points -and
            $manifest.product_policies.materialization.
                existing_point_reactivation -eq "kernel-dag" -and
            $manifest.product_policies.materialization.
                new_point_auto_materialization -eq
                    "explicit-visible-undoable-frontend-action") `
        -Message "Materialization/reactivation layering is inconsistent."

    $completenessText = Get-RequiredText -RelativePath $CompletenessMatrixPath
    $completenessOrders = @([regex]::Matches(
            $completenessText,
            '(?m)^\|\s*(?<order>(?:[1-9]|1[0-8]))\s*\|') |
        ForEach-Object { [int]$_.Groups["order"].Value } |
        Sort-Object -Unique)
    Assert-Condition -Condition (@(Compare-Object (1..18) `
                $completenessOrders).Count -eq 0) `
        -Message "The completeness matrix does not map all 18 cluster orders."
    foreach ($actionId in $actionIds) {
        Assert-Condition -Condition ($completenessText.Contains($actionId)) `
            -Message "Workspace completeness matrix omits action '$actionId'."
    }

    return [pscustomobject]@{
        FamilyCount = $families.Count
        ClusterCount = $clusters.Count
        ActionCount = $actions.Count
    }
}

function Add-U1IdsFromJsonNode {
    param(
        [AllowNull()] [object]$Node,
        [Parameter(Mandatory)]
        [AllowEmptyCollection()]
        [Collections.Generic.List[string]]$Result
    )

    if ($null -eq $Node) {
        return
    }
    if ($Node -is [string]) {
        if ($Node -match '^U1-(?:(?:R5|S1|PNT)-\d{2}|(?:S|W|D|L|V|I|B|A|G|C|Q|P)\d{2})$') {
            $Result.Add($Node)
        }
        return
    }
    if ($Node -is [Collections.IDictionary]) {
        foreach ($value in $Node.Values) {
            Add-U1IdsFromJsonNode -Node $value -Result $Result
        }
        return
    }
    if ($Node -is [Collections.IEnumerable]) {
        foreach ($item in $Node) {
            Add-U1IdsFromJsonNode -Node $item -Result $Result
        }
        return
    }
    foreach ($property in $Node.PSObject.Properties) {
        Add-U1IdsFromJsonNode -Node $property.Value -Result $Result
    }
}

function Assert-ScenarioInventory {
    $scenarioJson = Get-RequiredJson -RelativePath $ScenarioPath
    $scenarioIdsRaw = [Collections.Generic.List[string]]::new()
    Add-U1IdsFromJsonNode -Node $scenarioJson -Result $scenarioIdsRaw
    $scenarioIds = @($scenarioIdsRaw | Sort-Object -Unique)
    Assert-Condition -Condition ($scenarioIdsRaw.Count -eq $scenarioIds.Count) `
        -Message "Duplicate U1 scenario ID in the JSON inventory."
    Assert-Condition -Condition ($scenarioIds.Count -eq $ExpectedScenarioCount) `
        -Message ("Expected 118 unique U1 scenario IDs in JSON; found " +
            "$($scenarioIds.Count).")

    $matrixText = Get-RequiredText -RelativePath $PublicMatrixPath
    $matrixIds = @([regex]::Matches(
            $matrixText, '(?m)^\|\s*(?<id>U1-(?:(?:R5|S1|PNT)-\d{2}|(?:S|W|D|L|V|I|B|A|G|C|Q|P)\d{2}))\s*\|') |
        ForEach-Object { $_.Groups["id"].Value })
    $matrixUnique = @($matrixIds | Sort-Object -Unique)
    Assert-Condition -Condition ($matrixIds.Count -eq $matrixUnique.Count) `
        -Message "Duplicate U1 row ID in the public validation matrix."
    Assert-Condition -Condition ($matrixUnique.Count -eq $ExpectedScenarioCount) `
        -Message ("Expected 118 unique U1 public-matrix rows; found " +
            "$($matrixUnique.Count).")
    Assert-Condition -Condition (@(Compare-Object $scenarioIds `
                $matrixUnique).Count -eq 0) `
        -Message "Scenario JSON and public-matrix U1 IDs do not match exactly."

    foreach ($group in $ExpectedScenarioGroups.GetEnumerator()) {
        $suffix = if ($group.Key -in @("U1-R5", "U1-S1", "U1-PNT")) {
            '-\d{2}$'
        } else {
            '\d{2}$'
        }
        $count = @($scenarioIds | Where-Object {
                $_ -match ('^' + [regex]::Escape($group.Key) + $suffix)
            }).Count
        Assert-Condition -Condition ($count -eq $group.Value) `
            -Message ("Unexpected $($group.Key) scenario count: expected " +
                "$($group.Value), actual $count.")
    }
    return $scenarioIds.Count
}

function Assert-RelativeMarkdownLinks {
    param([Parameter(Mandatory)] [string[]]$RelativePaths)

    $checked = 0
    foreach ($relativePath in @($RelativePaths | Where-Object {
                $_.EndsWith(".md", [StringComparison]::OrdinalIgnoreCase)
            } | Sort-Object -Unique)) {
        $content = Get-RequiredText -RelativePath $relativePath
        foreach ($match in [regex]::Matches(
                $content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $match.Groups["target"].Value.Trim().Trim("<", ">")
            if ($target.StartsWith("#") -or
                    $target -match '^[A-Za-z][A-Za-z0-9+.-]*:') {
                continue
            }
            $pathPart = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($pathPart)) {
                continue
            }
            $pathPart = [Uri]::UnescapeDataString($pathPart)
            $documentDirectory = Split-Path -Parent $relativePath
            $joined = if ([string]::IsNullOrWhiteSpace($documentDirectory)) {
                $pathPart
            } else {
                Join-Path $documentDirectory $pathPart
            }
            $resolved = [IO.Path]::GetFullPath(
                (Join-Path $RepositoryRoot $joined))
            $rootPrefix = $RepositoryRoot.TrimEnd('\', '/') +
                [IO.Path]::DirectorySeparatorChar
            Assert-Condition -Condition ($resolved.StartsWith(
                    $rootPrefix, [StringComparison]::OrdinalIgnoreCase) -and
                    (Test-Path -LiteralPath $resolved)) `
                -Message "Broken relative Markdown link in ${relativePath}: $target"
        }
        $checked++
    }
    return $checked
}

function Assert-EvidenceIntegrity {
    $promptHash = Get-CanonicalLfSha256 -RelativePath $PromptPath
    $evidenceText = Get-RequiredText -RelativePath $EvidencePath
    $reportText = Get-RequiredText -RelativePath $ReportPath
    Assert-Condition -Condition ($evidenceText.Contains($promptHash) -and
            $reportText.Contains($promptHash)) `
        -Message "Current canonical prompt hash is not recorded in evidence/report."
    Assert-Condition -Condition ($evidenceText.Contains(
            $HistoricalPromptCanonicalLfSha256) -and
            $reportText.Contains($HistoricalPromptCanonicalLfSha256)) `
        -Message "Historical post-G9S1 prompt hash is not preserved."

    $manifestText = Get-RequiredText -RelativePath $EvidenceManifestPath
    $hashedPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    $validated = 0
    foreach ($line in $manifestText.Replace("`r`n", "`n").Replace(
            "`r", "`n").Split("`n")) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        $parts = $line -split '\s+', 2
        Assert-Condition -Condition ($parts.Count -eq 2 -and
                $parts[0] -match '^[0-9a-fA-F]{64}$') `
            -Message "Malformed G9U1 evidence hash line: $line"
        $relative = $parts[1].Trim().TrimStart('*').Replace("\", "/")
        Assert-Condition -Condition ($relative -ne $EvidenceManifestPath) `
            -Message "The G9U1 evidence manifest cannot hash itself."
        Assert-Condition -Condition ($hashedPaths.Add($relative)) `
            -Message "Duplicate G9U1 evidence hash entry: $relative"
        $actual = Get-CanonicalLfSha256 -RelativePath $relative
        Assert-Condition -Condition ($actual -eq
                $parts[0].ToLowerInvariant()) `
            -Message "G9U1 canonical-LF evidence mismatch: $relative"
        $validated++
    }
    foreach ($requiredHashPath in @($RequiredArtifacts | Where-Object {
                $_ -ne $EvidenceManifestPath
            })) {
        Assert-Condition -Condition ($hashedPaths.Contains($requiredHashPath)) `
            -Message "Evidence manifest omits required authority: $requiredHashPath"
    }
    Assert-Condition -Condition ($validated -eq $ExpectedEvidenceEntryCount) `
        -Message ("Expected $ExpectedEvidenceEntryCount canonical-LF evidence " +
            "entries; found $validated.")
    return [pscustomobject]@{
        PromptHash = $promptHash
        IntegrityCount = $validated
    }
}

function Assert-PlanningDisposition {
    Assert-Contains -RelativePath $PromptPath -RequiredText @(
        "POST-R6 RECONCILED",
        "DESIGN PASS — AUTHOR APPROVED",
        "IMPLEMENTATION NOT AUTHORIZED",
        "G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT",
        "PASS — AUTHOR APPROVED",
        "neither reimplements nor broadens it",
        "LocusPointInteractionQuery2D",
        "LocusPointInteractionResolver2D.resolve(...)",
        "LocusPointInteractionResult2D",
        "LocusPointInteractionStatus2D",
        "createInteractiveSemanticPoint(...)",
        "moveInteractiveSemanticPoint(...)",
        "UNIQUE_ADMISSIBLE_PREIMAGE",
        "MULTIPLE_SEMANTIC_PREIMAGES",
        "UNRESOLVED_NUMERICAL_SEARCH",
        "DEGENERATE_SOURCE_IMAGE",
        "reuses and locks the existing kernel setting",
        "ZoomWindow",
        "Spline×Spline",
        $PeriodicRisk,
        "all 118 scenarios",
        "G9U1 DESIGN = PASS — AUTHOR APPROVED",
        "G9U1 IMPLEMENTATION = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW")
    Assert-Contains -RelativePath $InteractionSpecPath -RequiredText @(
        "DESIGN PASS — AUTHOR APPROVED",
        "POST-R6 RECONCILED",
        "G9U0-R6",
        "LocusPointInteractionResolver2D",
        "Continuity",
        "stroke-only",
        "Locus V2 × Locus V2 rich-only",
        $PeriodicRisk)
    Assert-Contains -RelativePath $RoadmapPath -RequiredText @(
        "G9S1 = PASS — AUTHOR APPROVED",
        "G9U0-R6",
        "G9U1",
        "DESIGN = PASS — AUTHOR APPROVED",
        "NOT AUTHORIZED",
        $PeriodicRisk)
    Assert-Contains -RelativePath $ReportPath -RequiredText @(
        "G9S1 = PASS — AUTHOR APPROVED",
        "G9U0-R6 = PASS — AUTHOR APPROVED",
        "G9U1",
        "POST-R6 RECONCILED",
        "DESIGN PASS — AUTHOR APPROVED",
        "IMPLEMENTATION = NOT AUTHORIZED / NOT STARTED",
        "implementationStarted = false",
        "implementationAuthorized = false",
        "selfApproved = false",
        "authorApprovedDesign = true",
        "passClaimedImplementation = false",
        $PeriodicRisk)
    Assert-Contains -RelativePath $EvidencePath -RequiredText @(
        "G9S1",
        "PASS_AUTHOR_APPROVED",
        "G9U0-R6",
        "POST_R6_RECONCILED",
        "G9U1-DESIGN",
        "PASS_AUTHOR_APPROVED",
        "G9U1-IMPLEMENTATION",
        "NOT_AUTHORIZED_NOT_STARTED",
        $PeriodicRisk)

    foreach ($path in @(
            $PromptPath, $InteractionSpecPath, $ArchitecturePath,
            $CompletenessMatrixPath, $CommandMatrixPath, $ReportPath)) {
        Assert-Contains -RelativePath $path -RequiredText @(
            "Continuity", "OFF")
    }
    foreach ($path in @(
            $PromptPath, $InteractionSpecPath, $ArchitecturePath,
            $RoadmapPath, $ReportPath)) {
        Assert-Contains -RelativePath $path -RequiredText @(
            "ZoomWindow", "G12")
    }
    Assert-Contains -RelativePath $CompletenessMatrixPath -RequiredText @(
        "ZoomWindow")
    Assert-Contains -RelativePath $PromptPath -RequiredText @(
        "Spline×Spline", "rich-result-only")
    foreach ($path in @(
            $InteractionSpecPath, $CompletenessMatrixPath, $ReportPath)) {
        Assert-Contains -RelativePath $path -RequiredText @("rich-only")
    }
    foreach ($path in @(
            $PromptPath, $InteractionSpecPath, $ArchitecturePath,
            $ReportPath)) {
        Assert-Contains -RelativePath $path -RequiredText @(
            "G9U0-R6", "LocusPointInteractionResolver2D")
    }
    Assert-Contains -RelativePath $CompletenessMatrixPath -RequiredText @(
        "preimage", "ambiguity")

    Assert-NotContains -RelativePath $PromptPath -ForbiddenText @(
        "G9U1 = PASS — AUTHOR APPROVED", "implementationAuthorized = true",
        "current kernel provides the exact forward command",
        "R6 must establish")
    Assert-NotContains -RelativePath $ReportPath -ForbiddenText @(
        "G9U1 = PASS — AUTHOR APPROVED", "implementationAuthorized = true",
        "authorApproved = true", "passClaimed = true",
        "G9U0-R6 = PROPOSED", "G9U0-R6 = NOT AUTHORIZED")
    foreach ($path in @(
            $PromptPath, $InteractionSpecPath, $ArchitecturePath,
            $CompletenessMatrixPath, $ReportPath)) {
        Assert-NotContains -RelativePath $path -ForbiddenText @(
            "future G9U0-R6", "G9U0-R6 = NOT AUTHORIZED / NOT IMPLEMENTED",
            "no approved deterministic inverse semantic-address resolver exists")
    }
}

function Assert-PowerShellParses {
    $tokens = $null
    $errors = $null
    [void][Management.Automation.Language.Parser]::ParseFile(
        (Resolve-RequiredFile -RelativePath $VerifierRelativePath),
        [ref]$tokens, [ref]$errors)
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message ("PowerShell parse failure in $VerifierRelativePath`: " +
            (($errors | ForEach-Object { $_.Message }) -join "; "))
}

try {
    New-Item -ItemType Directory -Force -Path `
        ([IO.Path]::GetFullPath($LogDirectory)) | Out-Null
    $initialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    foreach ($artifact in $RequiredArtifacts) {
        [void](Resolve-RequiredFile -RelativePath $artifact)
    }
    Assert-ApprovedEntryAuthority
    $scopeCount = Assert-PlanningScope
    $manifestSummary = Assert-ManifestContract
    $scenarioCount = Assert-ScenarioInventory
    Assert-PlanningDisposition
    $linkCount = Assert-RelativeMarkdownLinks -RelativePaths $RequiredArtifacts
    $integrity = Assert-EvidenceIntegrity
    Assert-PowerShellParses

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $finalStatus -eq $initialStatus) `
        -Message "Repository status changed during G9U1 design verification."

    $summary = @(
        "G9U1 preexecution design verification passed.",
        "G9S1 authority: $G9S1PassTagName -> $G9S1PassCommit.",
        "R6 authority: $R6PassTagName -> $R6PassCommit.",
        "Protected pre-R6 planning checkpoint: $ProtectedPlanningCommit.",
        "Planning inventory: $scopeCount exact paths; live schema v1 unchanged.",
        "Workspace taxonomy: $($manifestSummary.FamilyCount) broad families, " +
            "$($manifestSummary.ClusterCount) operational clusters, " +
            "$($manifestSummary.ActionCount) stable actions.",
        "Validation inventory: $scenarioCount unique scenarios/matrix rows.",
        "Canonical prompt SHA-256: $($integrity.PromptHash).",
        "Evidence manifest: $($integrity.IntegrityCount) canonical-LF entries.",
        "Relative-link documents checked: $linkCount.",
        "G9U0-R6 is PASS — AUTHOR APPROVED; G9U1 design is PASS — AUTHOR APPROVED; implementation remains unexecuted and not authorized."
    )
    [IO.File]::WriteAllLines($SummaryLog, $summary,
        [Text.UTF8Encoding]::new($false))
    $summary | ForEach-Object { Write-Host $_ }
    Write-Host "Log: $SummaryLog"
} catch {
    $message = "G9U1 preexecution design verification failed: " +
        $_.Exception.Message + "`n" + $_.ScriptStackTrace
    try {
        New-Item -ItemType Directory -Force -Path `
            ([IO.Path]::GetFullPath($LogDirectory)) | Out-Null
        [IO.File]::WriteAllText($SummaryLog, $message + "`n",
            [Text.UTF8Encoding]::new($false))
    } catch {
        # Preserve the original verification failure.
    }
    Write-Error $message
    exit 1
}

exit 0

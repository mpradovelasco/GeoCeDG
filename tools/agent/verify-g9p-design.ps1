[CmdletBinding()]
param(
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-g9p-design")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$G8TagObject = "fed1bfbeea77a48acce285429b397eda77054df1"
$G8Commit = "e7810171179825a03b22d8c6eba28c672f468281"
$G9PTagObject = "6ce37f03df6f742aa448323d2150dd1655c986a5"
$G9PCommit = "94f92f49a44560e44bae9e75ba52595067471368"
$G9PTagName = "geocedg-g9p-pass"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\g9p"
$EvidencePath = Join-Path $EvidenceRoot "g9p-design-evidence.json"
$IntegrityPath = Join-Path $EvidenceRoot "g9p-evidence.sha256"
$PromptCatalogPath = Join-Path $EvidenceRoot "g9p-prompt-catalog.json"
$ReferenceManifestPath = Join-Path $RepositoryRoot `
    "docs\references\cedg\models\g9p\g9p-reference-inputs.json"
$SummaryLog = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "g9p-design.log"
$InitialStatus = $null

. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

$PromptPaths = @(
    ".github/prompts/tasks/g9p-integrated-analysis-predesign.prompt.md",
    ".github/prompts/tasks/g9o1-source-knowledge-bundles-and-guides.prompt.md",
    ".github/prompts/tasks/g9a1-spatial-identity-persistence-foundation.prompt.md",
    ".github/prompts/tasks/g9a2-spatial-semantic-point-pilot.prompt.md",
    ".github/prompts/tasks/g9a3-spatial-lifecycle-migration.prompt.md",
    ".github/prompts/tasks/g9u0-locus-v2-public-surface.prompt.md",
    ".github/prompts/tasks/g9x1-extended-dxf-curves.prompt.md",
    ".github/prompts/tasks/g9u1-construction-workspace.prompt.md",
    ".github/prompts/tasks/g9b-canonical-primitive-projections.prompt.md",
    ".github/prompts/tasks/g9c-composed-spatial-objects.prompt.md",
    ".github/prompts/tasks/g9u2-dihedral-procedures-workspace.prompt.md"
)

$SpecificationPaths = @(
    "geocedg/specs/spatial/g9-spatial-projection-semantics.md",
    "geocedg/specs/locus/locus-v2-public-surface.md",
    "geocedg/specs/ui/cedg-workspaces.md",
    "geocedg/specs/export/dxf-curve-fidelity-and-approximation.md",
    "geocedg/specs/operations/documentation-maintenance.md",
    "geocedg/specs/operations/knowledge-bundles.md"
)

$AdrPaths = @(
    "docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md",
    "docs/adr/0011-g9-spatial-persistence-and-phase-gates.md",
    "docs/adr/0012-manifest-defined-geocedg-workspaces.md",
    "docs/adr/0013-public-locus-v2-surface-and-token-selection.md",
    "docs/adr/0014-export-only-dxf-approximation-and-sidecar.md",
    "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md"
)

$RequiredArtifacts = @(
    $PromptPaths
    $SpecificationPaths
    $AdrPaths
    @(
        "README.md",
        ".gitattributes",
        "geocedg/specs/README.md",
        "docs/references/cedg/models/g9p/g9p-reference-inputs.json",
        "docs/references/cedg/models/g9p/g9p-reference-workflow-audit.md",
        "docs/architecture/g9p_integrated_plan.md",
        "docs/architecture/proposed_spatial_projection_semantics.md",
        "docs/architecture/g9_spatial_semantic_model.md",
        "docs/architecture/g9_projection_sufficiency_and_primitives.md",
        "docs/architecture/g9_spatial_persistence_and_upstream_impact.md",
        "docs/architecture/cedg_workspace_architecture.md",
        "docs/architecture/locus_v2_public_surface_architecture.md",
        "docs/architecture/g9_extended_dxf_architecture.md",
        "docs/architecture/geocedg_documentation_architecture.md",
        "docs/architecture/knowledge_bundle_architecture.md",
        "docs/user/geocedg_user_guide.md",
        "docs/user/geocedg_mathematical_reference.md",
        "docs/developer/geocedg_developer_guide.md",
        "docs/developer/geocedg_agent_prompt_guide.md",
        "docs/validation/g9_spatial_validation_and_benchmark_plan.md",
        "docs/validation/g9_spatial_scientific_traceability.md",
        "docs/validation/g9_public_workspace_validation_matrix.md",
        "docs/validation/g9_extended_dxf_validation_and_benchmark_plan.md",
        "docs/validation/g9_documentation_bundle_traceability.md",
        "docs/validation/g9p_author_decisions.md",
        "docs/validation/g9p_design_report.md",
        "docs/roadmap/geocedg_roadmap.md",
        "geocedg/specs/operations/knowledge-bundle.schema.json",
        "geocedg/specs/operations/knowledge-bundle-profiles.json",
        "geocedg/validation/g9p/g9p-design-evidence.json",
        "geocedg/validation/g9p/g9p-prompt-catalog.json",
        "geocedg/validation/g9p/g9p-evidence.sha256",
        "tools/agent/evidence-integrity.ps1",
        "tools/agent/verify-locus-v2.ps1",
        "tools/agent/verify-g9p-design.ps1"
    )
) | ForEach-Object { $_ } | Sort-Object -Unique

$HistoricalManifests = @(
    "geocedg/validation/locus-v2/g7a/metric-reference-values.sha256",
    "geocedg/validation/locus-v2/g7a-r1/g7a-r1-evidence.sha256",
    "geocedg/validation/locus-v2/g7b/g7b-evidence.sha256",
    "geocedg/validation/locus-v2/g8b/g8b-evidence.sha256",
    "geocedg/validation/locus-v2/g8c/g8c-design-evidence.sha256",
    "geocedg/validation/locus-v2/g8c1/g8c1-evidence.sha256",
    "geocedg/validation/locus-v2/g8c2/g8c2-contract-evidence.sha256",
    "geocedg/validation/locus-v2/g8c2/g8c2-evidence.sha256"
)

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

    return Get-GeoCeDGFrozenCanonicalTextSha256 `
        -RepositoryRoot $RepositoryRoot -Path $Path -Commit $G9PCommit
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    Assert-Condition -Condition (Test-GeoCeDGFrozenPath `
            -RepositoryRoot $RepositoryRoot -Path $RelativePath `
            -Commit $G9PCommit) `
        -Message "Required frozen G9P artifact is missing: $RelativePath"
    return $RelativePath.Replace("\", "/")
}

function Assert-Contains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Text
    )

    $path = Assert-RequiredFile -RelativePath $RelativePath
    $content = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
        -Path $path -Commit $G9PCommit
    foreach ($requiredText in $Text) {
        Assert-Condition -Condition ($content.Contains($requiredText)) `
            -Message "$RelativePath does not contain: $requiredText"
    }
}

function Assert-NotContains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Text
    )

    $path = Assert-RequiredFile -RelativePath $RelativePath
    $content = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
        -Path $path -Commit $G9PCommit
    foreach ($forbiddenText in $Text) {
        Assert-Condition -Condition (-not $content.Contains($forbiddenText)) `
            -Message "$RelativePath contains obsolete/forbidden text: $forbiddenText"
    }
}

function Assert-JsonFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        [void](Assert-RequiredFile -RelativePath $RelativePath)
        return Get-GeoCeDGFrozenJson -RepositoryRoot $RepositoryRoot `
            -Path $RelativePath -Commit $G9PCommit
    } catch {
        throw "Invalid JSON in ${RelativePath}: $($_.Exception.Message)"
    }
}

function Assert-MarkdownLinks {
    param([Parameter(Mandatory)] [string[]]$RelativePaths)

    $documents = @($RelativePaths | Where-Object { $_.EndsWith(".md") } |
        Sort-Object -Unique)
    $checked = 0
    foreach ($relativeDocument in $documents) {
        $document = Assert-RequiredFile -RelativePath $relativeDocument
        $content = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
            -Path $document -Commit $G9PCommit
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
            $syntheticRoot = [IO.Path]::GetFullPath($RepositoryRoot)
            $documentDirectory = Split-Path -Parent $document
            $relativeJoin = if ([string]::IsNullOrWhiteSpace(
                    $documentDirectory)) {
                [Uri]::UnescapeDataString($pathPart)
            } else {
                Join-Path $documentDirectory `
                    ([Uri]::UnescapeDataString($pathPart))
            }
            $resolved = [IO.Path]::GetFullPath((Join-Path $syntheticRoot `
                $relativeJoin))
            $relativeTarget = [IO.Path]::GetRelativePath(
                $syntheticRoot, $resolved).Replace("\", "/")
            Assert-Condition -Condition (Test-GeoCeDGFrozenPath `
                    -RepositoryRoot $RepositoryRoot -Path $relativeTarget `
                    -Commit $G9PCommit) `
                -Message "Broken Markdown link in ${relativeDocument}: $target"
        }
        $checked++
    }
    return $checked
}

function Assert-IntegrityManifest {
    $validated = 0
    $manifestText = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
        -Path "geocedg/validation/g9p/g9p-evidence.sha256" `
        -Commit $G9PCommit
    foreach ($line in $manifestText.Replace("`r`n", "`n").Replace(
            "`r", "`n").Split("`n")) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2 -and
                $parts[0] -match '^[0-9a-fA-F]{64}$') `
            -Message "Malformed G9P integrity line: $line"
        $relative = $parts[1].Trim().Replace("\", "/")
        Assert-Condition -Condition ($relative -ne
                "geocedg/validation/g9p/g9p-evidence.sha256") `
            -Message "The G9P integrity manifest cannot hash itself."
        [void](Assert-RequiredFile -RelativePath $relative)
        $actual = Get-CanonicalTextSha256 -Path $relative
        Assert-Condition -Condition ($actual -eq $parts[0].ToLowerInvariant()) `
            -Message "G9P integrity mismatch: $relative"
        $validated++
    }
    Assert-Condition -Condition ($validated -ge 45) `
        -Message "G9P integrity manifest is unexpectedly incomplete: $validated"
    return $validated
}

function Assert-ReferenceInputs {
    $rawHashes = [ordered]@{
        "docs/references/cedg/models/g9p/geocedg-reference-general-construction-workflow.ggb" =
            "738e3edcf44e10f0c07b846d1f53127c4d8450cf0ad209ea9a0e3e8cc2c36a2e"
        "docs/references/cedg/models/g9p/geocedg-reference-locus-cylindrical-graft-development.ggb" =
            "9e220a695a4b1ee2bf60adc77872133e8074740443ae659de1404539de8141f2"
        "docs/references/cedg/models/g9p/geocedg-reference-locus-focal-sphere-illumination.ggb" =
            "baec7131aa95676864457d1602f73f8fef3ce37674fc0e48b28efd4feac204a0"
        "docs/references/cedg/models/g9p/geocedg-reference-locus-truncated-cone-cylinder-connections.ggb" =
            "18c6fad4d53fc3bb03a1546021a0677656fded2c853b7ec198312b96ee55e155"
        "docs/references/cedg/models/g9p/geocedg-reference-construction-workspace.png" =
            "3c30ddc0ae4aa54d02292a66bae3ed37447911803f52b64315cc1c6b71f966ba"
        "docs/references/cedg/models/g9p/geocedg-reference-workflows-notes.md" =
            "d30f08d8ff278e40fbf65e26a030969d129c56e5e5bda8f18b5fdee760a8b6a0"
        "docs/references/cedg/models/g9p/Prompt.md" =
            "b29a919c5c3a7ba781f720d3a9c646f2892c6be19b87419001493481077a9e08"
        "models/legacy/template-v7/original/Templatev7.ggb" =
            "f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113"
    }
    $canonicalTextHashes = [ordered]@{
        "docs/references/cedg/models/g9p/geocedg-reference-workflows-notes.md" =
            "9eefa014dfd6951263cf42b70bb02737c834d4a515278efd8e41f64173b7c16b"
        "docs/references/cedg/models/g9p/Prompt.md" =
            "02d05478f719f5e5f14c5ee21d062a7446602721893f85b2f289bfeac700f495"
    }
    foreach ($item in $rawHashes.GetEnumerator()) {
        [void](Assert-RequiredFile -RelativePath $item.Key)
        $bytes = Get-GeoCeDGFrozenBlobBytes -RepositoryRoot $RepositoryRoot `
            -Path $item.Key -Commit $G9PCommit
        if ($canonicalTextHashes.Contains($item.Key)) {
            $actual = Get-GeoCeDGSha256FromBytes `
                -Bytes (ConvertTo-GeoCeDGCanonicalLfBytes -Bytes $bytes)
            Assert-Condition -Condition ($actual -eq
                    $canonicalTextHashes[$item.Key]) `
                -Message "Immutable reference canonical hash mismatch: $($item.Key)"
        } else {
            $actual = Get-GeoCeDGSha256FromBytes -Bytes $bytes
            Assert-Condition -Condition ($actual -eq $item.Value) `
                -Message "Immutable reference raw hash mismatch: $($item.Key)"
        }
    }
    $promptCanonical = Get-CanonicalTextSha256 `
        -Path "docs/references/cedg/models/g9p/Prompt.md"
    Assert-Condition -Condition ($promptCanonical -eq
            "02d05478f719f5e5f14c5ee21d062a7446602721893f85b2f289bfeac700f495") `
        -Message "Canonical-LF author prompt hash mismatch."

    $manifest = Assert-JsonFile -RelativePath `
        "docs/references/cedg/models/g9p/g9p-reference-inputs.json"
    foreach ($textPath in $canonicalTextHashes.Keys) {
        $manifestRecords = @($manifest.author_inputs | Where-Object {
                $_.path -eq $textPath })
        Assert-Condition -Condition ($manifestRecords.Count -eq 1 -and
                $manifestRecords[0].sha256 -eq $rawHashes[$textPath]) `
            -Message "Recorded author-input raw hash drift: $textPath"
    }
    Assert-Condition -Condition ([bool]$manifest.inspection.inputs_unchanged -and
            $manifest.author_inputs.Count -eq 7 -and
            $manifest.context_inputs.Count -eq 1 -and
            $manifest.ggb_archives.Count -eq 5 -and
            $manifest.toolbar_sets.'template-v7-toolbar'.top_level_group_count -eq 19 -and
            $manifest.macro_sets.'template-v7-macros-24'.names_in_archive_order.Count -eq 24 -and
            $manifest.archive_entry_sets.'template-v7-shared-resources-16'.entries.Count -eq 16) `
        -Message "Reference-input manifest inventory is inconsistent."
    foreach ($archiveRecord in $manifest.ggb_archives) {
        Assert-Condition -Condition ($archiveRecord.entry_count -eq 22 -and
                $archiveRecord.macros.embedded_count -eq 24) `
            -Message "Reference archive inventory is inconsistent: $($archiveRecord.path)"
        [void](Assert-RequiredFile -RelativePath $archiveRecord.path)
        $archiveBytes = Get-GeoCeDGFrozenBlobBytes `
            -RepositoryRoot $RepositoryRoot -Path $archiveRecord.path `
            -Commit $G9PCommit
        $stream = [IO.MemoryStream]::new($archiveBytes, $false)
        $archive = [IO.Compression.ZipArchive]::new(
            $stream, [IO.Compression.ZipArchiveMode]::Read, $false)
        try {
            Assert-Condition -Condition ($archive.Entries.Count -eq 22 -and
                    $null -ne $archive.GetEntry("geogebra.xml") -and
                    $null -ne $archive.GetEntry("geogebra_macro.xml")) `
                -Message "Reference GGB structure changed: $($archiveRecord.path)"
        } finally {
            $archive.Dispose()
            $stream.Dispose()
        }
    }
    return $rawHashes.Count
}

function Assert-PromptCatalog {
    $catalog = Assert-JsonFile -RelativePath `
        "geocedg/validation/g9p/g9p-prompt-catalog.json"
    Assert-Condition -Condition ($catalog.schemaVersion -eq 2 -and
            $catalog.prompts.Count -eq 11 -and
            $catalog.sourcePrompt.rawSha256 -eq
                "b29a919c5c3a7ba781f720d3a9c646f2892c6be19b87419001493481077a9e08" -and
            $catalog.sourcePrompt.canonicalLfSha256 -eq
                "02d05478f719f5e5f14c5ee21d062a7446602721893f85b2f289bfeac700f495") `
        -Message "G9P prompt catalog header is inconsistent."

    $requiredHeadings = @(
        "# Objective", "# Authority and evidence hierarchy", "# Scope",
        "# Explicitly forbidden scope", "# Architectural placement",
        "# Required design/specification",
        "# Geometric invariants and degeneracies",
        "# Compatibility and serialization", "# Required tests and commands",
        "# Required artifacts", "# Stop conditions")
    $dependencyHeadings = @(
        "# Hard dependencies", "# Recommended execution predecessor",
        "# Global/release gate")
    $futureCount = 0
    $authorizedFutureCount = 0
    $unauthorizedFutureCount = 0
    foreach ($entry in $catalog.prompts) {
        $path = Assert-RequiredFile -RelativePath $entry.path
        Assert-Condition -Condition ((Get-CanonicalTextSha256 -Path $path) -eq
                $entry.canonicalLfSha256) `
            -Message "Canonical prompt hash mismatch: $($entry.path)"
        $content = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
            -Path $path -Commit $G9PCommit
        foreach ($heading in $requiredHeadings) {
            Assert-Condition -Condition ($content.Contains($heading)) `
                -Message "$($entry.path) is missing prompt heading: $heading"
        }
        if ($entry.phase -ne "G9P") {
            Assert-Condition -Condition ($content.Contains("UNEXECUTED")) `
                -Message "Future prompt is not explicitly unexecuted: $($entry.path)"
            if ($entry.phase -eq "G9O1") {
                Assert-Condition -Condition ($entry.state -eq
                        "AUTHORIZED_UNEXECUTED" -and
                        $content.Contains("AUTHORIZED CANONICAL PROMPT") -and
                        $content.Contains("NOT STARTED")) `
                    -Message "G9O1 prompt is not in the required authorized/not-started state: $($entry.path)"
                $authorizedFutureCount++
            } else {
                Assert-Condition -Condition ($entry.state.ToString().StartsWith(
                        "PROPOSED_UNEXECUTED") -and
                        $content.Contains("NOT AUTHORIZED")) `
                    -Message "Future prompt authorization drift: $($entry.path)"
                $unauthorizedFutureCount++
            }
            foreach ($heading in $dependencyHeadings) {
                Assert-Condition -Condition ($content.Contains($heading)) `
                    -Message "$($entry.path) is missing dependency heading: $heading"
            }
            Assert-Condition -Condition ($entry.hardDependencies.Count -ge 1 -and
                    -not [string]::IsNullOrWhiteSpace(
                        $entry.recommendedExecutionPredecessor) -and
                    -not [string]::IsNullOrWhiteSpace($entry.globalReleaseGate)) `
                -Message "Future prompt catalog dependency metadata is incomplete: $($entry.phase)"
            $futureCount++
        }
    }
    Assert-Condition -Condition ($futureCount -eq 10) `
        -Message "Unexpected future prompt count: $futureCount"
    Assert-Condition -Condition ($authorizedFutureCount -eq 1 -and
            $unauthorizedFutureCount -eq 9) `
        -Message "Unexpected future prompt authorization split: authorized=$authorizedFutureCount unauthorized=$unauthorizedFutureCount"
    return $catalog.prompts.Count
}

function Assert-Scope {
    $changedPaths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $G8Commit $G9PCommit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate G9P changed paths."
    $changedPaths = @($changedPaths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object -Unique)

    $allowedToolFiles = @(
        "tools/agent/evidence-integrity.ps1",
        "tools/agent/verify-g7a-metrics.ps1",
        "tools/agent/verify-g7b-metrics.ps1",
        "tools/agent/verify-g8b-intersections.ps1",
        "tools/agent/verify-g8c-intersections-design.ps1",
        "tools/agent/verify-g8c1-intersections.ps1",
        "tools/agent/verify-g8c2-contract.ps1",
        "tools/agent/verify-g8c2-intersections.ps1",
        "tools/agent/verify-locus-v2.ps1",
        "tools/agent/verify-g9p-design.ps1",
        "tools/agent/verify.ps1")
    foreach ($path in $changedPaths) {
        $normalized = $path.Replace("\", "/")
        $allowed = $normalized -eq "README.md" -or
            $normalized -eq ".gitattributes" -or
            $normalized.StartsWith("docs/") -or
            $normalized.StartsWith("geocedg/specs/") -or
            $normalized.StartsWith("geocedg/validation/g9p/") -or
            $normalized -match '^\.github/prompts/tasks/g9[a-z0-9-]+\.prompt\.md$' -or
            $allowedToolFiles.Contains($normalized)
        Assert-Condition -Condition $allowed `
            -Message "Path outside the G9P design boundary: $normalized"
        foreach ($forbiddenPrefix in @(
                "source/", "apps/", "python/", "packaging/", "tools/knowledge/",
                "geocedg/features/", "geocedg/resources/", "artifacts/")) {
            Assert-Condition -Condition (-not $normalized.StartsWith(
                    $forbiddenPrefix)) `
                -Message "Productive/generated G9P scope edit: $normalized"
        }
        Assert-Condition -Condition ($normalized -notmatch
                '\.(java|kt|kts|gradle|class|jar)$') `
            -Message "Productive or compiled source changed in G9P: $normalized"
    }
    return $changedPaths.Count
}

function Assert-PowerShellParses {
    $scripts = @(
        "tools/agent/evidence-integrity.ps1",
        "tools/agent/verify-g7a-metrics.ps1",
        "tools/agent/verify-g7b-metrics.ps1",
        "tools/agent/verify-g8b-intersections.ps1",
        "tools/agent/verify-g8c-intersections-design.ps1",
        "tools/agent/verify-g8c1-intersections.ps1",
        "tools/agent/verify-g8c2-contract.ps1",
        "tools/agent/verify-g8c2-intersections.ps1",
        "tools/agent/verify-locus-v2.ps1",
        "tools/agent/verify-g9p-design.ps1",
        "tools/agent/verify.ps1")
    foreach ($relative in $scripts) {
        [void](Assert-RequiredFile -RelativePath $relative)
        $content = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
            -Path $relative -Commit $G9PCommit
        $tokens = $null
        $errors = $null
        [void][Management.Automation.Language.Parser]::ParseInput(
            $content, [ref]$tokens, [ref]$errors)
        Assert-Condition -Condition ($errors.Count -eq 0) `
            -Message "PowerShell parse failure in $relative"
    }
    return $scripts.Count
}

try {
    New-Item -ItemType Directory -Force -Path `
        ([IO.Path]::GetFullPath($LogDirectory)) | Out-Null
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    foreach ($artifact in $RequiredArtifacts) {
        [void](Assert-RequiredFile -RelativePath $artifact)
    }

    $frozen = Assert-GeoCeDGFrozenG8Anchor -RepositoryRoot $RepositoryRoot
    Assert-Condition -Condition ($frozen -eq $G8Commit) `
        -Message "Unexpected frozen G8 commit."
    $tagType = (& git -C $RepositoryRoot cat-file -t $G8TagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagType -eq "tag") `
        -Message "Approved G8 tag object is missing or not annotated."
    $g9pTagType = (& git -C $RepositoryRoot cat-file -t $G9PTagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $g9pTagType -eq "tag") `
        -Message "Approved G9P tag object is missing or not annotated."
    $g9pRef = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$G9PTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $g9pRef -eq $G9PTagObject) `
        -Message "Approved G9P tag reference does not resolve to its tag object."
    $g9pPeeled = (& git -C $RepositoryRoot rev-parse `
        "$G9PTagObject^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $g9pPeeled -eq $G9PCommit) `
        -Message "Approved G9P tag does not peel to its accepted commit."
    & git -C $RepositoryRoot merge-base --is-ancestor $G9PCommit HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current HEAD does not descend from the accepted G9P commit."
    $historicalEntries = 0
    foreach ($manifest in $HistoricalManifests) {
        $historicalEntries += Assert-GeoCeDGFrozenHashManifest `
            -RepositoryRoot $RepositoryRoot -ManifestPath $manifest
    }
    Assert-Condition -Condition ($historicalEntries -eq 306) `
        -Message "Unexpected frozen G7/G8 evidence entry count: $historicalEntries"

    $referenceCount = Assert-ReferenceInputs
    $promptCount = Assert-PromptCatalog
    $scopeCount = Assert-Scope
    $scriptCount = Assert-PowerShellParses

    foreach ($specification in $SpecificationPaths) {
        Assert-Contains -RelativePath $specification -Text @(
            "NORMATIVE / AUTHOR APPROVED")
    }
    foreach ($adr in $AdrPaths) {
        Assert-Contains -RelativePath $adr -Text @("Status: **Accepted**")
    }

    $bundleSchema = Assert-JsonFile -RelativePath `
        "geocedg/specs/operations/knowledge-bundle.schema.json"
    $bundleProfiles = Assert-JsonFile -RelativePath `
        "geocedg/specs/operations/knowledge-bundle-profiles.json"
    Assert-Condition -Condition ($null -ne $bundleSchema -and
            $null -ne $bundleProfiles -and
            $bundleSchema.description.Contains("NORMATIVE / AUTHOR APPROVED") -and
            $bundleProfiles.status -eq "NORMATIVE_AUTHOR_APPROVED" -and
            $bundleProfiles.implementation_status -eq "AUTHORIZED_NOT_STARTED") `
        -Message "Knowledge-bundle schema/profile design is missing."

    $evidence = Assert-JsonFile -RelativePath `
        "geocedg/validation/g9p/g9p-design-evidence.json"
    Assert-Condition -Condition ($evidence.status -eq
            "G9P_R1_PASS_AUTHOR_APPROVED" -and
            $evidence.g9pStatus -eq "PASS_AUTHOR_APPROVED" -and
            $evidence.g8Authority.status -eq "PASS_AUTHOR_APPROVED" -and
            -not [bool]$evidence.approval.selfApproved -and
            [bool]$evidence.approval.authorApproved -and
            [bool]$evidence.approval.specificationsNormative -and
            [bool]$evidence.approval.adrsAccepted -and
            [bool]$evidence.approval.g9o1Authorized -and
            -not [bool]$evidence.approval.otherImplementationAuthorized -and
            -not [bool]$evidence.approval.productiveG9Started -and
            $evidence.phaseDisposition.G9O1 -eq "AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.G9A1 -eq "DESIGNED_NOT_AUTHORIZED" -and
            $evidence.canonicalPrompts.authorizedNotStarted.Count -eq 1 -and
            $evidence.canonicalPrompts.authorizedNotStarted[0] -eq "G9O1" -and
            $evidence.scopeAudit.productiveSourceFilesChanged -eq 0 -and
            $evidence.scopeAudit.productiveGuiFilesChanged -eq 0 -and
            $evidence.scopeAudit.productiveDxfFilesChanged -eq 0 -and
            $evidence.scopeAudit.bundleGeneratorsChanged -eq 0) `
        -Message "G9P disposition or scope evidence is inconsistent."

    $hardEdges = @($evidence.dependencyModel.hardEdges |
        ForEach-Object { "$($_.from)->$($_.to)" })
    foreach ($requiredEdge in @(
            "G9A1->G9A2", "G9A2->G9A3", "G9A3->G9U0",
            "G9A3->G9B", "G9B->G9C", "G9U0->G9U1",
            "G9X1->G9U1", "G9U1->G9U2")) {
        Assert-Condition -Condition ($hardEdges.Contains($requiredEdge)) `
            -Message "Missing refined hard dependency: $requiredEdge"
    }
    $globalEdges = @($evidence.dependencyModel.globalGateEdges |
        ForEach-Object { "$($_.from)->$($_.to)" })
    foreach ($requiredEdge in @(
            "G9O1->G9_GLOBAL_CLOSEOUT", "G9U1->G9_GLOBAL_CLOSEOUT",
            "G9C->G9_GLOBAL_CLOSEOUT", "G9_GLOBAL_CLOSEOUT->G9U2")) {
        Assert-Condition -Condition ($globalEdges.Contains($requiredEdge)) `
            -Message "Missing refined global-gate dependency: $requiredEdge"
    }
    Assert-Condition -Condition (-not $hardEdges.Contains("G9U1->G9B") -and
            -not $hardEdges.Contains("G9U0->G9X1") -and
            -not $hardEdges.Contains("G9O1->G9A1") -and
            $evidence.dependencyModel.recommendedSequence[0] -eq "G9O1" -and
            $evidence.dependencyModel.externalHardDependencies.G9X1.Contains(
                "G5_AUTHOR_APPROVED") -and
            $evidence.dependencyModel.externalHardDependencies.G9X1.Contains(
                "G6_G8_INTERNAL_SEMANTIC_AUTHORITY")) `
        -Message "Refined hard/recommended/global dependency model is inconsistent."

    Assert-Contains -RelativePath `
        "geocedg/specs/spatial/g9-spatial-projection-semantics.md" -Text @(
        "ProjectionSystemId", "ProjectionDiagramMapId",
        "p_m=\delta_m(\pi_i(x))",
        "Explicit target-based semantically compatible redefine",
        "True semantic replacement or type-incompatible redefine")
    Assert-Contains -RelativePath `
        "geocedg/specs/locus/locus-v2-public-surface.md" -Text @(
        "SemanticGenerator1D<S>", "LocusV2[Q,t,s,D]",
        "durable semantic preimage address",
        "direct or indirect dependency cycle",
        "LocusMetricResult2D.isScalarAdmissible()",
        "No scalar-mapping command spelling is frozen by G9P",
        'G9U0 must expose the standard total `Length[GeoLocusV2]` operation',
        "GeoCeDG Classic diagnostic launcher/path",
        "external upstream GeoGebra")
    Assert-Contains -RelativePath `
        "geocedg/specs/export/dxf-curve-fidelity-and-approximation.md" -Text @(
        'A wholly `EXACT` export may omit',
        "partial component output is disabled and strict reject/stop is the default",
        "explicit closed semantic parameter subdomain required",
        'must never become a `GeoElement`')
    Assert-Contains -RelativePath "docs/architecture/g9p_integrated_plan.md" `
        -Text @(
        "Hard semantic/contract dependencies",
        "recommended execution predecessor",
        "global/release gate",
        "G9X1 does not semantically depend on G9U0",
        "G9B depends on G9A3 and not on G9U1")
    Assert-Contains -RelativePath `
        ".github/prompts/tasks/g9b-canonical-primitive-projections.prompt.md" `
        -Text @("G9U1 is explicitly", "q_i=pi_i(x)",
            "p_i=delta_i(q_i)")
    Assert-NotContains -RelativePath `
        ".github/prompts/tasks/g9b-canonical-primitive-projections.prompt.md" `
        -Text @("author-approved G9U1 PASS")
    Assert-NotContains -RelativePath `
        ".github/prompts/tasks/g9x1-extended-dxf-curves.prompt.md" `
        -Text @("Require author-approved G9U0 PASS")
    Assert-Contains -RelativePath `
        ".github/prompts/tasks/g9o1-source-knowledge-bundles-and-guides.prompt.md" `
        -Text @("AUTHORIZED CANONICAL PROMPT", "NOT STARTED / UNEXECUTED")
    Assert-Contains -RelativePath `
        ".github/prompts/tasks/g9u0-locus-v2-public-surface.prompt.md" `
        -Text @("actual GeoGebra command/overload/localization/XML conventions",
            "standard total scalar", "external-upstream unsupported-open")
    Assert-Contains -RelativePath `
        ".github/prompts/tasks/g9a3-spatial-lifecycle-migration.prompt.md" `
        -Text @("GeoCeDG Classic diagnostic path", "zero lossy conversion")
    Assert-Contains -RelativePath `
        ".github/prompts/tasks/g9x1-extended-dxf-curves.prompt.md" `
        -Text @("conditional mandatory", "an all-exact export may omit it",
            "Strict reject/stop is the partiality")
    Assert-Contains -RelativePath `
        "docs/validation/g9p_author_decisions.md" -Text @(
        "| D1 | **APPROVE**", "| D2 | **APPROVE**",
        "| D3 | **APPROVE**", "| D4 | **APPROVE**",
        "| D5 | **APPROVE WITH API DEFERRAL**", "| D6 | **APPROVE**",
        "| D7 | **APPROVE**", "| D8 | **APPROVE**")

    Assert-Contains -RelativePath "docs/roadmap/geocedg_roadmap.md" -Text @(
        "G9P-R1 = PASS — AUTHOR APPROVED",
        "G9P = PASS — AUTHOR APPROVED",
        "G9 SPECIFICATIONS = NORMATIVE / AUTHOR APPROVED",
        "ADR 0010–0015 = ACCEPTED",
        "G9O1 = AUTHORIZED — NOT STARTED",
        "G9A / G9B / G9C = DESIGNED — NOT AUTHORIZED",
        "G9U2 = DESIGNED — BLOCKED ON THE APPROVED G9 GATE",
        "G9 PRODUCTIVE IMPLEMENTATION = NOT STARTED")
    Assert-Contains -RelativePath "docs/validation/g9p_design_report.md" -Text @(
        "G8 = PASS — AUTHOR APPROVED",
        "G9P-R1 = PASS — AUTHOR APPROVED",
        "G9P = PASS — AUTHOR APPROVED",
        "G9 SPECIFICATIONS = NORMATIVE / AUTHOR APPROVED",
        "ADR 0010–0015 = ACCEPTED",
        "G9O1 = AUTHORIZED — NOT STARTED",
        "G9 PRODUCTIVE IMPLEMENTATION = NOT STARTED")
    Assert-Contains -RelativePath ".gitattributes" -Text @(
        "docs/references/cedg/models/g9p/*.ggb binary",
        "docs/references/cedg/models/g9p/*.png binary",
        "geocedg-reference-workflows-notes.md text -whitespace")

    $markdownCount = Assert-MarkdownLinks -RelativePaths $RequiredArtifacts
    $integrityCount = Assert-IntegrityManifest

    & git -C $RepositoryRoot diff --check $G8Commit $G9PCommit
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Frozen G9P diff whitespace check failed."
    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $finalStatus -eq $InitialStatus) `
        -Message "Repository status changed during G9P verification."

    $summary = @(
        "Frozen G9P/R1 author closeout verification passed.",
        "Historical target: $G9PTagName -> $G9PCommit.",
        "References: $referenceCount immutable inputs/context files.",
        "Frozen historical evidence: $historicalEntries entries.",
        "Canonical prompts: $promptCount (10 future, unexecuted; G9O1 authorized/not started).",
        "Frozen G9P integrity: $integrityCount files.",
        "Markdown documents: $markdownCount.",
        "PowerShell scripts parsed: $scriptCount.",
        "Frozen scoped changed paths: $scopeCount.",
        "Historical G9P disposition: productive G9 implementation NOT STARTED."
    )
    [IO.File]::WriteAllLines($SummaryLog, $summary,
        [Text.UTF8Encoding]::new($false))
    $summary | ForEach-Object { Write-Host $_ }
    Write-Host "Log: $SummaryLog"
} catch {
    $message = "G9P design verification failed: $($_.Exception.Message)"
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

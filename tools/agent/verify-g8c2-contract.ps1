[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$G8C1Commit = "3c72e889a436e4bbccde177e1f24423196575f04"
$G8C1Tag = "geocedg-g8c1-pass"
$PromptSha = "e7f0535332a9c5a2789f98476aef2f9f143e84f9bda0ad3b7d657f070d99e58b"
$EvidenceRoot = Join-Path $RepositoryRoot "geocedg\validation\locus-v2\g8c2"
$EvidencePath = Join-Path $EvidenceRoot "g8c2-contract-review-evidence.json"
$EvidenceHashes = Join-Path $EvidenceRoot "g8c2-contract-evidence.sha256"
$ImplementationEvidencePath = Join-Path $EvidenceRoot `
    "g8c2-intersection-kernel-evidence.json"
$PromptPath = ".github/prompts/tasks/g8c2-locus-v2-locus-intersections.prompt.md"
$InitialStatus = $null

$Documents = @(
    $PromptPath,
    "docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md",
    "docs/architecture/locus_v2_extended_intersection_architecture.md",
    "docs/architecture/locus_v2_extended_intersection_capability_matrix.md",
    "docs/architecture/locus_v2_extended_intersection_semantic_model.md",
    "docs/architecture/locus_v2_extended_intersection_upstream_impact.md",
    "docs/developer/locus_v2_extended_intersection_api.md",
    "docs/roadmap/g8_locus_v2_intersections_plan.md",
    "docs/roadmap/g8c_locus_v2_extended_intersections_design.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/user/geocedg_user_guide.md",
    "docs/validation/g8c2_locus_v2_locus_intersection_contract_review.md",
    "docs/validation/g8c_locus_v2_extended_intersection_benchmark_plan.md",
    "docs/validation/g8c_locus_v2_extended_intersection_scientific_traceability.md",
    "docs/validation/g8c_locus_v2_extended_intersection_validation_matrix.md",
    "geocedg/specs/locus/locus-v2-extended-intersections.md",
    "geocedg/specs/locus/locus-v2-intersections.md"
)

. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    Assert-Condition -Condition (Test-Path -LiteralPath `
            (Join-Path $RepositoryRoot $RelativePath) -PathType Leaf) `
        -Message "Required G8C2 contract artifact is missing: $RelativePath"
}

function Assert-Contains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Text
    )

    $content = Get-Content -LiteralPath `
        (Join-Path $RepositoryRoot $RelativePath) -Raw
    foreach ($requiredText in $Text) {
        Assert-Condition -Condition ($content.Contains($requiredText)) `
            -Message "$RelativePath does not contain: $requiredText"
    }
}

function Assert-EvidenceHashes {
    [void](Assert-GeoCeDGFrozenHashManifest -RepositoryRoot $RepositoryRoot `
        -ManifestPath $EvidenceHashes)
}

function Assert-MarkdownLinks {
    foreach ($relativeDocument in $Documents) {
        if (-not $relativeDocument.EndsWith(".md")) {
            continue
        }
        $document = Join-Path $RepositoryRoot $relativeDocument
        $content = Get-Content -LiteralPath $document -Raw
        foreach ($match in [regex]::Matches(
                $content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
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
    Write-Host "G8C2 contract Markdown links resolved: $($Documents.Count) documents."
}

try {
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    [void](Assert-GeoCeDGFrozenG8Anchor -RepositoryRoot $RepositoryRoot)
    $implementationPresent = Test-GeoCeDGFrozenPath `
        -RepositoryRoot $RepositoryRoot -Path $ImplementationEvidencePath
    foreach ($file in $Documents + @(
            "geocedg/validation/locus-v2/g8c2/g8c2-contract-review-evidence.json",
            "geocedg/validation/locus-v2/g8c2/g8c2-contract-evidence.sha256",
            "tools/agent/verify-g8c2-contract.ps1") +
            $(if ($implementationPresent) {
                    @("geocedg/validation/locus-v2/g8c2/g8c2-intersection-kernel-evidence.json")
                } else {
                    @()
                })) {
        Assert-RequiredFile -RelativePath $file
    }

    & git -C $RepositoryRoot merge-base --is-ancestor $G8C1Commit HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current checkout does not descend from the G8C1 closeout."
    $tagTarget = (& git -C $RepositoryRoot rev-list -n 1 $G8C1Tag).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and `
            $tagTarget -eq $G8C1Commit) `
        -Message "$G8C1Tag does not identify the G8C2 entry baseline."

    Assert-Condition -Condition ((Get-GeoCeDGFrozenCanonicalTextSha256 `
                -RepositoryRoot $RepositoryRoot -Path $PromptPath) -eq $PromptSha) `
        -Message "Canonical G8C2 prompt hash changed."

    Assert-Contains -RelativePath `
        "geocedg/specs/locus/locus-v2-extended-intersections.md" -Text @(
            "G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED",
            $(if ($implementationPresent) {
                    "G8C2 contract status: NORMATIVE — AUTHOR APPROVED. Implementation status:"
                } else {
                    "G8C2 status: NORMATIVE — AUTHOR APPROVED / AUTHORIZED — NOT STARTED"
                }),
            "canonical source pair", "Option B", "Local isolation",
            "finite component products and periodic branches",
            "global G8 closeout", "G9 productive implementation remains unauthorized")
    Assert-Contains -RelativePath `
        "docs/adr/0009-locus-v2-locus-intersection-pair-semantics.md" -Text @(
            "Status: **Accepted**", "Accepted: 2026-08-15",
            "canonical unordered pair", "dedicated query-local dual-parameter solver",
            "No substantive contradiction")
    Assert-Contains -RelativePath $PromptPath -Text @(
        "Canonical execution prompt", "# Mandatory entry gate",
        "# Explicitly forbidden scope", "two-parameter",
        "canonical source pair", "Local isolation and tangency",
        "Identity and Option B", "Overlap and completeness",
        "Do not execute it during contract review", "G9")
    Assert-Contains -RelativePath `
        "docs/roadmap/geocedg_roadmap.md" -Text @(
            "G8C1 = PASS — AUTHOR APPROVED",
            "G8C2 CONTRACT = NORMATIVE — AUTHOR APPROVED",
            "ADR 0009 = ACCEPTED",
            $(if ($implementationPresent) {
                    "G8C2 = PASS — AUTHOR APPROVED"
                } else {
                    "G8C2 = AUTHORIZED — NOT STARTED"
                }), $(if ($implementationPresent) {
                    "G9 DESIGN = AUTHORIZED — NOT STARTED"
                } else {
                    "G9 = NOT STARTED"
                }))
    Assert-Contains -RelativePath `
        "docs/validation/g8c2_locus_v2_locus_intersection_contract_review.md" `
        -Text @("PASS — AUTHOR APPROVED", "no substantive contradiction",
            $PromptSha, "G8C2 = AUTHORIZED — NOT STARTED")

    $evidence = Get-GeoCeDGFrozenJson -RepositoryRoot $RepositoryRoot `
        -Path $EvidencePath
    Assert-Condition -Condition ($evidence.status -eq "PASS_AUTHOR_APPROVED" `
            -and $evidence.g8c1Baseline.commit -eq $G8C1Commit `
            -and $evidence.g8c1Baseline.tag -eq $G8C1Tag `
            -and [bool]$evidence.g8c1Baseline.tagPeeledToBaseline `
            -and -not [bool]$evidence.comparison.substantiveContradictionFound `
            -and [bool]$evidence.comparison.sharedRichResultFrameworkRetained `
            -and -not [bool]$evidence.comparison.oneParameterAdapterUsedAsPairSolver `
            -and [bool]$evidence.comparison.dedicatedDualParameterSolverRequired) `
        -Message "G8C2 review provenance or G8C1 comparison is inconsistent."
    Assert-Condition -Condition ($evidence.authorDecisions.contract -eq `
            "NORMATIVE_AUTHOR_APPROVED" -and
            $evidence.authorDecisions.adr0009 -eq "ACCEPTED" -and
            $evidence.authorDecisions.implementation -eq `
                "AUTHORIZED_NOT_STARTED" -and
            $evidence.authorDecisions.optionB -eq "PRESERVED" -and
            $evidence.authorDecisions.state -eq "QUERY_LOCAL") `
        -Message "G8C2 author-decision evidence is inconsistent."
    Assert-Condition -Condition ($evidence.canonicalPrompt.path -eq $PromptPath `
            -and $evidence.canonicalPrompt.canonicalLfSha256 -eq $PromptSha `
            -and -not [bool]$evidence.canonicalPrompt.executed) `
        -Message "G8C2 prompt evidence is inconsistent."
    foreach ($scopeGate in $evidence.scopeAudit.PSObject.Properties) {
        Assert-Condition -Condition ([int]$scopeGate.Value -eq 0) `
            -Message "G8C2 forbidden scope gate is nonzero: $($scopeGate.Name)"
    }
    Assert-Condition -Condition ($evidence.phaseDisposition.g8c1 -eq `
            "PASS_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.g8c2Contract -eq `
                "NORMATIVE_AUTHOR_APPROVED" -and
            $evidence.phaseDisposition.adr0009 -eq "ACCEPTED" -and
            $evidence.phaseDisposition.g8c2 -eq "AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.g8 -eq "IN_PROGRESS" -and
            $evidence.phaseDisposition.g9 -eq "NOT_STARTED") `
        -Message "G8C2 phase disposition is inconsistent."

    $changedPaths = @(Get-GeoCeDGFrozenChangedPaths `
        -RepositoryRoot $RepositoryRoot -BaseCommit $G8C1Commit)
    foreach ($path in $changedPaths) {
        if ($path.StartsWith("source/")) {
            $allowedImplementationPath = $implementationPresent -and (
                $path -eq "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java" -or
                $path -match '^source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/[^/]+\.java$' -or
                $path -match '^source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2[^/]+\.java$')
            Assert-Condition -Condition $allowedImplementationPath `
                -Message "Productive or test source changed outside G8C2 implementation: $path"
        }
        foreach ($forbiddenPath in @(
                "/commands/", "CmdIntersect", "AlgoDispatcher", "GeoLocus.java",
                "/Path.java", "GeoFactory", "/io/", "geogebra3D", "kernel3D",
                "/export/", "python/", "artifacts/")) {
            Assert-Condition -Condition (-not $path.Contains($forbiddenPath)) `
                -Message "Forbidden G8C2 contract-review scope edit: $path"
        }
    }

    Assert-EvidenceHashes
    Assert-MarkdownLinks

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and `
            $finalStatus -eq $InitialStatus) `
        -Message "Repository status changed during G8C2 contract verification."

    Write-Host "G8C2 contract review verification passed."
    Write-Host "G8C2 contract is normative/author-approved; ADR 0009 is Accepted."
    if ($implementationPresent) {
        Write-Host "G8C2/global G8 are author-approved; G9 design is authorized/not started."
    } else {
        Write-Host "G8C2 is authorized/not started; G9 remains not started."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

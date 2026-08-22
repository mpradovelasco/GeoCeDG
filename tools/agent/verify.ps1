[CmdletBinding()]
param(
    [switch]$FullTests,
    [switch]$LaunchDesktop,
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [switch]$RunBenchmarks,
    [switch]$VerifyPackagingArtifacts,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify"),
    [string]$BenchmarkOutputPath,
    [string]$PackagingArtifactRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$OperationalVerifier = Join-Path $PSScriptRoot "verify-operational.ps1"
$WorkstationVerifier = Join-Path $PSScriptRoot "verify-workstation.ps1"
$BaselineVerifier = Join-Path $PSScriptRoot "verify-baseline.ps1"
$FrontendVerifier = Join-Path $PSScriptRoot "verify-frontend.ps1"
$LegacyVerifier = Join-Path $PSScriptRoot "verify-legacy.ps1"
$PackagingVerifier = Join-Path $PSScriptRoot "verify-packaging.ps1"
$DxfVerifier = Join-Path $PSScriptRoot "verify-dxf.ps1"
$LocusV2Verifier = Join-Path $PSScriptRoot "verify-locus-v2.ps1"
$G7AMetricVerifier = Join-Path $PSScriptRoot "verify-g7a-metrics.ps1"
$G7BMetricVerifier = Join-Path $PSScriptRoot "verify-g7b-metrics.ps1"
$G8AIntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8a-intersections.ps1"
$G8BIntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8b-intersections.ps1"
$G8CIntersectionDesignVerifier = Join-Path $PSScriptRoot `
    "verify-g8c-intersections-design.ps1"
$G8C1IntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8c1-intersections.ps1"
$G8C2ContractVerifier = Join-Path $PSScriptRoot `
    "verify-g8c2-contract.ps1"
$G8C2IntersectionVerifier = Join-Path $PSScriptRoot `
    "verify-g8c2-intersections.ps1"
$G9PDesignVerifier = Join-Path $PSScriptRoot "verify-g9p-design.ps1"
$KnowledgeBundleVerifier = Join-Path $PSScriptRoot `
    "verify-knowledge-bundles.ps1"
$G9A1SpatialIdentityVerifier = Join-Path $PSScriptRoot `
    "verify-g9a1-spatial-identity.ps1"
$G9A2SpatialPointVerifier = Join-Path $PSScriptRoot `
    "verify-g9a2-spatial-point.ps1"
$G9A3SpatialLifecycleVerifier = Join-Path $PSScriptRoot `
    "verify-g9a3-spatial-lifecycle.ps1"
$G9U0LocusPublicSurfaceVerifier = Join-Path $PSScriptRoot `
    "verify-g9u0-locus-v2-public-surface.ps1"
$G9U0R1PublicCreationLifecycleVerifier = Join-Path $PSScriptRoot `
    "verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1"
$G9X1ExtendedDxfVerifier = Join-Path $PSScriptRoot `
    "verify-g9x1-extended-dxf.ps1"
$G9U0R2ProductRefinementVerifier = Join-Path $PSScriptRoot `
    "verify-g9u0-r2-product-refinement.ps1"
$BenchmarkRunner = Join-Path $RepositoryRoot "tools\benchmark\run.ps1"
$InitialStatus = $null

. (Join-Path $PSScriptRoot "repository-state.ps1")

function Assert-LastScriptSuccess {
    param([Parameter(Mandatory)] [string]$Description)

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

try {
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read initial repository status."
    }

    $repositoryState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $RepositoryRoot
    Write-Host "Repository state"
    Write-Host "  Branch: $($repositoryState.Branch)"
    Write-Host "  Commit: $($repositoryState.Commit)"
    Write-Host "  Latest included phase: $($repositoryState.LatestIncludedPhase)"

    Write-Host "`n==> GeoCeDG operational contracts"
    & $OperationalVerifier
    Assert-LastScriptSuccess -Description "GeoCeDG operational contracts"

    Write-Host "`n==> Windows workstation operational contracts"
    & $WorkstationVerifier
    Assert-LastScriptSuccess -Description "Windows workstation operational contracts"

    Write-Host "`n==> Controlled legacy CeDG integration"
    & $LegacyVerifier
    Assert-LastScriptSuccess -Description "Controlled legacy CeDG integration"

    Write-Host "`n==> Native 2D geometry and DXF export"
    $dxfParameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "dxf"
    }
    if ($SkipBuild) {
        $dxfParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $dxfParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $dxfParameters.KeepBuildOutputs = $true
    }
    & $DxfVerifier @dxfParameters
    Assert-LastScriptSuccess -Description "Native 2D geometry and DXF export"

    Write-Host "`n==> G6 Locus V2 characterization and experimental kernel"
    $locusV2Parameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "locus-v2"
    }
    if ($SkipBuild) {
        $locusV2Parameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $locusV2Parameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $locusV2Parameters.KeepBuildOutputs = $true
    }
    & $LocusV2Verifier @locusV2Parameters
    Assert-LastScriptSuccess -Description "G6 Locus V2"

    if ($repositoryState.LatestIncludedPhaseNumber -ge 7) {
        Write-Host "`n==> G7A Locus V2 metric characterization"
        $g7aMetricParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g7a-metrics"
        }
        if ($SkipBuild) {
            $g7aMetricParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g7aMetricParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g7aMetricParameters.KeepBuildOutputs = $true
        }
        & $G7AMetricVerifier @g7aMetricParameters
        Assert-LastScriptSuccess `
            -Description "G7A Locus V2 metric characterization"

        Write-Host "`n==> G7B native Locus V2 metric kernel"
        $g7bMetricParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g7b-metrics"
        }
        if ($SkipBuild) {
            $g7bMetricParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g7bMetricParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g7bMetricParameters.KeepBuildOutputs = $true
        }
        & $G7BMetricVerifier @g7bMetricParameters
        Assert-LastScriptSuccess -Description "G7B native Locus V2 metric kernel"
    }

    if ($repositoryState.LatestIncludedPhaseNumber -ge 8) {
        Write-Host "`n==> G8A Locus V2 intersection characterization and closeout"
        $g8aIntersectionParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8a-intersections"
            RequireFinalEvidence = $true
        }
        if ($SkipBuild) {
            $g8aIntersectionParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8aIntersectionParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8aIntersectionParameters.KeepBuildOutputs = $true
        }
        & $G8AIntersectionVerifier @g8aIntersectionParameters
        Assert-LastScriptSuccess `
            -Description "G8A Locus V2 intersection characterization and closeout"

        Write-Host "`n==> G8B native Locus V2 intersection kernel"
        $g8bIntersectionParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8b-intersections"
        }
        if ($SkipBuild) {
            $g8bIntersectionParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8bIntersectionParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8bIntersectionParameters.KeepBuildOutputs = $true
        }
        & $G8BIntersectionVerifier @g8bIntersectionParameters
        Assert-LastScriptSuccess `
            -Description "G8B native Locus V2 intersection kernel"

        Write-Host "`n==> G8C extended Locus V2 intersection design"
        $g8cDesignParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g8c-intersections-design"
        }
        if ($SkipBuild) {
            $g8cDesignParameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g8cDesignParameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g8cDesignParameters.KeepBuildOutputs = $true
        }
        & $G8CIntersectionDesignVerifier @g8cDesignParameters
        Assert-LastScriptSuccess `
            -Description "G8C extended Locus V2 intersection design"

        $g8c1Evidence = Join-Path $RepositoryRoot `
            "geocedg\validation\locus-v2\g8c1\g8c1-intersection-kernel-evidence.json"
        if (Test-Path -LiteralPath $g8c1Evidence -PathType Leaf) {
            Write-Host "`n==> G8C1 extended one-parameter intersection kernel"
            $g8c1IntersectionParameters = @{
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g8c1-intersections"
            }
            if ($SkipBuild) {
                $g8c1IntersectionParameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g8c1IntersectionParameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g8c1IntersectionParameters.KeepBuildOutputs = $true
            }
            & $G8C1IntersectionVerifier @g8c1IntersectionParameters
            Assert-LastScriptSuccess `
                -Description "G8C1 extended one-parameter intersection kernel"
        }

        $g8c2ImplementationEvidence = Join-Path $RepositoryRoot `
            "geocedg\validation\locus-v2\g8c2\g8c2-intersection-kernel-evidence.json"
        $g8c2ContractEvidence = Join-Path $RepositoryRoot `
            "geocedg\validation\locus-v2\g8c2\g8c2-contract-review-evidence.json"
        if (Test-Path -LiteralPath $g8c2ImplementationEvidence -PathType Leaf) {
            Write-Host "`n==> G8C2 Locus V2 x Locus V2 intersection kernel"
            $g8c2IntersectionParameters = @{
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g8c2-intersections"
            }
            if ($SkipBuild) {
                $g8c2IntersectionParameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g8c2IntersectionParameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g8c2IntersectionParameters.KeepBuildOutputs = $true
            }
            & $G8C2IntersectionVerifier @g8c2IntersectionParameters
            Assert-LastScriptSuccess `
                -Description "G8C2 Locus V2 x Locus V2 intersection kernel"
        } elseif (Test-Path -LiteralPath $g8c2ContractEvidence -PathType Leaf) {
            Write-Host "`n==> G8C2 locus-locus contract review"
            & $G8C2ContractVerifier
            Assert-LastScriptSuccess `
                -Description "G8C2 locus-locus contract review"
        }
    }

    $g9pEvidence = Join-Path $RepositoryRoot `
        "geocedg\validation\g9p\g9p-design-evidence.json"
    if (Test-Path -LiteralPath $g9pEvidence -PathType Leaf) {
        Write-Host "`n==> G9P/R1 author-approved design closeout"
        $g9pParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9p-design"
        }
        & $G9PDesignVerifier @g9pParameters
        Assert-LastScriptSuccess `
            -Description "G9P/R1 author-approved design closeout"
    }

    $g9o1Evidence = Join-Path $RepositoryRoot `
        "geocedg\validation\g9o1\g9o1-evidence.json"
    if (Test-Path -LiteralPath $g9o1Evidence -PathType Leaf) {
        Write-Host "`n==> G9O1 deterministic source/knowledge bundles"
        $knowledgeBundleParameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9o1-knowledge-bundles"
        }
        & $KnowledgeBundleVerifier @knowledgeBundleParameters
        Assert-LastScriptSuccess `
            -Description "G9O1 deterministic source/knowledge bundles"
    }

    $g9a1Evidence = Join-Path $RepositoryRoot `
        "docs\validation\g9a1_spatial_identity_evidence.json"
    $hasG9A1Evidence = Test-Path -LiteralPath $g9a1Evidence -PathType Leaf
    $hasG9A1Verifier = Test-Path -LiteralPath $G9A1SpatialIdentityVerifier `
        -PathType Leaf
    if ($hasG9A1Evidence -ne $hasG9A1Verifier) {
        throw "Incomplete G9A1 verifier/evidence integration; both artifacts are required."
    }
    if ($hasG9A1Evidence) {
        Write-Host "`n==> G9A1 durable spatial identity and persistence foundation"
        $g9a1Parameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9a1-spatial-identity"
        }
        if ($SkipBuild) {
            $g9a1Parameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g9a1Parameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g9a1Parameters.KeepBuildOutputs = $true
        }
        & $G9A1SpatialIdentityVerifier @g9a1Parameters
        Assert-LastScriptSuccess `
            -Description "G9A1 durable spatial identity and persistence foundation"
    }

    if (Test-Path -LiteralPath $G9A2SpatialPointVerifier -PathType Leaf) {
        Write-Host "`n==> G9A2 spatial semantic core and projection-defined point pilot"
        $g9a2Parameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9a2-spatial-point"
        }
        if ($SkipBuild) {
            $g9a2Parameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g9a2Parameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g9a2Parameters.KeepBuildOutputs = $true
        }
        & $G9A2SpatialPointVerifier @g9a2Parameters
        Assert-LastScriptSuccess `
            -Description "G9A2 spatial semantic point pilot"
    }

    $g9a3IntegrationArtifacts = @(
        $G9A3SpatialLifecycleVerifier,
        (Join-Path $RepositoryRoot `
            "docs\architecture\g9a3_spatial_lifecycle_migration_design.md"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_lifecycle_scenarios.json"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_compatibility_corpus.json"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_compatibility_corpus.sha256"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_compatibility_matrix.md"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_lifecycle_evidence.json"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_lifecycle_evidence.sha256"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9a3_spatial_lifecycle_migration_report.md")
    )
    $g9a3PresentCount = @($g9a3IntegrationArtifacts | Where-Object {
        Test-Path -LiteralPath $_ -PathType Leaf
    }).Count
    if ($g9a3PresentCount -ne 0 -and
            $g9a3PresentCount -ne $g9a3IntegrationArtifacts.Count) {
        throw "Incomplete G9A3 verifier/support integration; all paired artifacts are required."
    }
    if ($g9a3PresentCount -eq $g9a3IntegrationArtifacts.Count) {
        Write-Host "`n==> G9A3 spatial lifecycle and explicit migration hardening"
        $g9a3Parameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9a3-spatial-lifecycle"
        }
        if ($SkipBuild) {
            $g9a3Parameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g9a3Parameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g9a3Parameters.KeepBuildOutputs = $true
        }
        & $G9A3SpatialLifecycleVerifier @g9a3Parameters
        Assert-LastScriptSuccess `
            -Description "G9A3 spatial lifecycle and migration hardening"
    }

    $g9u0EvidenceRoot = Join-Path $RepositoryRoot `
        "geocedg\validation\locus-v2\g9u0"
    $g9u0IntegrationArtifacts = @(
        $G9U0LocusPublicSurfaceVerifier,
        (Join-Path $g9u0EvidenceRoot `
            "g9u0-public-surface-evidence.json"),
        (Join-Path $g9u0EvidenceRoot `
            "g9u0-public-surface-scenarios.json"),
        (Join-Path $g9u0EvidenceRoot "g9u0-evidence.sha256"),
        (Join-Path $g9u0EvidenceRoot "g9u0-compatibility-corpus.json"),
        (Join-Path $g9u0EvidenceRoot "g9u0-compatibility-corpus.sha256"),
        (Join-Path $RepositoryRoot `
            "docs\architecture\locus_v2_public_surface_implementation.md"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9u0_locus_v2_public_surface_migration_report.md")
    )
    $g9u0PresentCount = @($g9u0IntegrationArtifacts | Where-Object {
        Test-Path -LiteralPath $_ -PathType Leaf
    }).Count
    if ($g9u0PresentCount -ne 0 -and
            $g9u0PresentCount -ne $g9u0IntegrationArtifacts.Count) {
        throw "Incomplete G9U0 verifier/support integration; all paired artifacts are required."
    }
    if ($g9u0PresentCount -eq $g9u0IntegrationArtifacts.Count) {
        $g9u0Evidence = Get-Content -Raw -LiteralPath (
            Join-Path $g9u0EvidenceRoot `
                "g9u0-public-surface-evidence.json") |
            ConvertFrom-Json -Depth 100
        if ($g9u0Evidence.sourceBoundary.inventoryStatus -eq "FROZEN") {
            Write-Host "`n==> G9U0 experimental Locus V2 public surface"
            $g9u0Parameters = @{
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9u0-locus-v2-public-surface"
            }
            if ($SkipBuild) {
                $g9u0Parameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g9u0Parameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g9u0Parameters.KeepBuildOutputs = $true
            }
            & $G9U0LocusPublicSurfaceVerifier @g9u0Parameters
            Assert-LastScriptSuccess `
                -Description "G9U0 experimental Locus V2 public surface"
        } elseif ($g9u0Evidence.sourceBoundary.inventoryStatus -eq
                "OPEN_PENDING_SOURCE_FREEZE") {
            Write-Host "`n==> G9U0 validation scaffold detected"
            & $G9U0LocusPublicSurfaceVerifier -SkipBuild `
                -LogDirectory (Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9u0-locus-v2-public-surface-scaffold")
            Assert-LastScriptSuccess `
                -Description "G9U0 validation scaffold"
            Write-Host ("    Productive inventory is open; the G9U0 " +
                "implementation gate is not executed.")
        } else {
            throw "Unknown G9U0 source inventory state in candidate evidence."
        }
    }

    $g9u0R1Report = Join-Path $RepositoryRoot `
        "docs\validation\g9u0_r1_locus_v2_public_creation_lifecycle_candidate_report.md"
    $g9u0R1IntegrationArtifacts = @(
        $G9U0R1PublicCreationLifecycleVerifier,
        $g9u0R1Report
    )
    $g9u0R1PresentCount = @($g9u0R1IntegrationArtifacts | Where-Object {
        Test-Path -LiteralPath $_ -PathType Leaf
    }).Count
    if ($g9u0R1PresentCount -ne 0 -and
            $g9u0R1PresentCount -ne $g9u0R1IntegrationArtifacts.Count) {
        throw "Incomplete G9U0-R1 verifier/report integration; both artifacts are required."
    }
    if ($g9u0R1PresentCount -eq $g9u0R1IntegrationArtifacts.Count) {
        Write-Host "`n==> G9U0-R1 public creation and Desktop lifecycle hardening"
        $g9u0R1Parameters = @{
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "g9u0-r1-locus-v2-public-creation-lifecycle"
        }
        if ($SkipBuild) {
            $g9u0R1Parameters.SkipBuild = $true
        }
        if ($AllowToolchainDownload) {
            $g9u0R1Parameters.AllowToolchainDownload = $true
        }
        if ($KeepBuildOutputs) {
            $g9u0R1Parameters.KeepBuildOutputs = $true
        }
        & $G9U0R1PublicCreationLifecycleVerifier @g9u0R1Parameters
        Assert-LastScriptSuccess `
            -Description "G9U0-R1 public creation and Desktop lifecycle hardening"
    }

    $g9x1EvidenceRoot = Join-Path $RepositoryRoot `
        "geocedg\validation\export\g9x1"
    $g9x1EvidencePath = Join-Path $g9x1EvidenceRoot "g9x1-evidence.json"
    $g9x1IntegrationArtifacts = @(
        $G9X1ExtendedDxfVerifier,
        $g9x1EvidencePath,
        (Join-Path $g9x1EvidenceRoot "g9x1-scenarios.json"),
        (Join-Path $g9x1EvidenceRoot "g9x1-evidence.sha256"),
        (Join-Path $RepositoryRoot `
            "docs\architecture\g9_extended_dxf_architecture.md"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9x1_extended_dxf_implementation_candidate_report.md")
    )
    $g9x1PresentCount = @($g9x1IntegrationArtifacts | Where-Object {
        Test-Path -LiteralPath $_ -PathType Leaf
    }).Count
    if ($g9x1PresentCount -ne 0 -and
            $g9x1PresentCount -ne $g9x1IntegrationArtifacts.Count) {
        throw "Incomplete G9X1 verifier/support integration; all paired artifacts are required."
    }
    if ($g9x1PresentCount -eq $g9x1IntegrationArtifacts.Count) {
        $g9x1Evidence = Get-Content -Raw -LiteralPath $g9x1EvidencePath |
            ConvertFrom-Json -Depth 100
        if ($g9x1Evidence.sourceBoundary.inventoryStatus -eq "FROZEN") {
            if ($g9x1Evidence.validation.focused.status -ne "PASSED" -or
                    $g9x1Evidence.validation.focusedDeterministicRerun.status -ne
                        "PASSED" -or
                    -not [bool]$g9x1Evidence.validation.focusedDeterministicRerun.matchesFocused -or
                    $g9x1Evidence.validation.g5Regression.status -ne "PASSED" -or
                    $g9x1Evidence.validation.checkstyle -ne "PASSED" -or
                    $g9x1Evidence.validation.staticVerifier -ne "PASSED" -or
                    $g9x1Evidence.validation.gitDiffCheck -ne "PASSED" -or
                    $g9x1Evidence.validation.gitDiffCachedCheck -ne "PASSED") {
                throw ("Frozen G9X1 evidence must record clean focused, " +
                    "deterministic, G5, Checkstyle, static and diff gates " +
                    "before composed verification.")
            }
            Write-Host "`n==> G9X1 extended exact/approximate DXF export"
            $g9x1Parameters = @{
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9x1-extended-dxf"
            }
            if ($SkipBuild) {
                $g9x1Parameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g9x1Parameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g9x1Parameters.KeepBuildOutputs = $true
            }
            & $G9X1ExtendedDxfVerifier @g9x1Parameters
            Assert-LastScriptSuccess `
                -Description "G9X1 extended exact/approximate DXF export"
        } elseif ($g9x1Evidence.sourceBoundary.inventoryStatus -eq
                "OPEN_PENDING_IMPLEMENTATION_FREEZE") {
            Write-Host "`n==> G9X1 validation scaffold detected"
            & $G9X1ExtendedDxfVerifier -SkipBuild `
                -LogDirectory (Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9x1-extended-dxf-scaffold")
            Assert-LastScriptSuccess -Description "G9X1 validation scaffold"
            Write-Host ("    Productive inventory is open; the G9X1 " +
                "focused implementation gate is not executed.")
        } else {
            throw "Unknown G9X1 source inventory state in candidate evidence."
        }
    }

    $g9u0R2EvidenceRoot = Join-Path $RepositoryRoot `
        "geocedg\validation\g9u0-r2"
    $g9u0R2EvidencePath = Join-Path $g9u0R2EvidenceRoot `
        "g9u0-r2-product-refinement-evidence.json"
    $g9u0R2IntegrationArtifacts = @(
        $G9U0R2ProductRefinementVerifier,
        $g9u0R2EvidencePath,
        (Join-Path $g9u0R2EvidenceRoot `
            "g9u0-r2-product-refinement-scenarios.json"),
        (Join-Path $g9u0R2EvidenceRoot "g9u0-r2-evidence.sha256"),
        (Join-Path $g9u0R2EvidenceRoot `
            "g9u0-r2-document-compatibility-corpus.json"),
        (Join-Path $g9u0R2EvidenceRoot `
            "g9u0-r2-document-compatibility-corpus.sha256"),
        (Join-Path $RepositoryRoot `
            "docs\architecture\g9u0_r2_product_refinement_implementation.md"),
        (Join-Path $RepositoryRoot `
            "docs\validation\g9u0_r2_product_refinement_implementation_candidate_report.md")
    )
    $g9u0R2PresentCount = @($g9u0R2IntegrationArtifacts | Where-Object {
        Test-Path -LiteralPath $_ -PathType Leaf
    }).Count
    if ($g9u0R2PresentCount -ne 0 -and
            $g9u0R2PresentCount -ne $g9u0R2IntegrationArtifacts.Count) {
        throw ("Incomplete G9U0-R2 verifier/support integration; all paired " +
            "artifacts are required.")
    }
    if ($g9u0R2PresentCount -eq $g9u0R2IntegrationArtifacts.Count) {
        $g9u0R2Evidence = Get-Content -Raw -LiteralPath $g9u0R2EvidencePath |
            ConvertFrom-Json -Depth 100
        if ($g9u0R2Evidence.sourceBoundary.inventoryStatus -eq "FROZEN") {
            $g9u0R2Historical = @(
                $g9u0R2Evidence.validation.g9u0R1Regression,
                $g9u0R2Evidence.validation.historicalG9U0Regression,
                $g9u0R2Evidence.validation.g9x1Regression,
                $g9u0R2Evidence.validation.g5Regression,
                $g9u0R2Evidence.validation.g9aRegression,
                $g9u0R2Evidence.validation.legacyLocusRegression
            )
            $g9u0R2HistoryClean = @($g9u0R2Historical | Where-Object {
                $_.status -eq "PASSED" -and $_.exitCode -eq 0
            }).Count -eq $g9u0R2Historical.Count
            if ($g9u0R2Evidence.status -ne "PASS_AUTHOR_APPROVED" -or
                    [bool]$g9u0R2Evidence.approval.selfApproved -or
                    -not [bool]$g9u0R2Evidence.approval.authorApproved -or
                    -not [bool]$g9u0R2Evidence.approval.passClaimed -or
                    [bool]$g9u0R2Evidence.approval.reviewRequired -or
                    $g9u0R2Evidence.manualAuthorSmoke.status -ne
                        "PASS_AUTHOR_APPROVED" -or
                    -not [bool]$g9u0R2Evidence.manualAuthorSmoke.passed -or
                    $g9u0R2Evidence.manualAuthorSmoke.correction.status -ne
                        "AUTHOR_VERIFIED_COMPLETE" -or
                    $g9u0R2Evidence.manualAuthorSmoke.reSmoke.status -ne
                        "PASS_AUTHOR_APPROVED" -or
                    -not [bool]$g9u0R2Evidence.manualAuthorSmoke.reSmoke.passed -or
                    $g9u0R2Evidence.validation.focused.status -ne "PASSED" -or
                    $g9u0R2Evidence.validation.focusedDeterministicRerun.status -ne
                        "PASSED" -or
                    -not [bool]$g9u0R2Evidence.validation.focusedDeterministicRerun.matchesFocused -or
                    $g9u0R2Evidence.validation.packagingStatic.status -ne
                        "PASSED" -or
                    $g9u0R2Evidence.validation.checkstyle.status -ne "PASSED" -or
                    $g9u0R2Evidence.validation.gitDiffCheck.status -ne "PASSED" -or
                    $g9u0R2Evidence.validation.gitDiffCachedCheck.status -ne
                        "PASSED" -or
                    -not $g9u0R2HistoryClean) {
                throw ("Frozen G9U0-R2 PASS evidence must preserve the " +
                    "historical failed smoke and record the author-approved " +
                    "re-smoke plus clean focused, deterministic, packaging, " +
                    "Checkstyle, diff and R2-R01..R06 gates before composed " +
                    "verification.")
            }
            Write-Host "`n==> G9U0-R2 product/document refinement"
            $g9u0R2Parameters = @{
                HistoricalRegressionsAlreadyComposed = $true
                LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9u0-r2-product-refinement"
            }
            if ($SkipBuild) {
                $g9u0R2Parameters.SkipBuild = $true
            }
            if ($AllowToolchainDownload) {
                $g9u0R2Parameters.AllowToolchainDownload = $true
            }
            if ($KeepBuildOutputs) {
                $g9u0R2Parameters.KeepBuildOutputs = $true
            }
            if ($VerifyPackagingArtifacts) {
                $g9u0R2Parameters.VerifyPackagingArtifacts = $true
                if (-not [string]::IsNullOrWhiteSpace($PackagingArtifactRoot)) {
                    $g9u0R2Parameters.PackagingArtifactRoot =
                        [IO.Path]::GetFullPath($PackagingArtifactRoot)
                }
            }
            & $G9U0R2ProductRefinementVerifier @g9u0R2Parameters
            Assert-LastScriptSuccess `
                -Description "G9U0-R2 product/document refinement"
        } elseif ($g9u0R2Evidence.sourceBoundary.inventoryStatus -eq
                "OPEN_PENDING_IMPLEMENTATION_FREEZE") {
            Write-Host "`n==> G9U0-R2 validation scaffold detected"
            & $G9U0R2ProductRefinementVerifier -SkipBuild `
                -LogDirectory (Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                    "g9u0-r2-product-refinement-scaffold")
            Assert-LastScriptSuccess -Description "G9U0-R2 validation scaffold"
            Write-Host ("    Productive inventory is open; no R2 product " +
                "tests, phase PASS or G9U1 authorization are claimed.")
        } else {
            throw "Unknown G9U0-R2 source inventory state in candidate evidence."
        }
    }

    Write-Host "`n==> Standalone Windows packaging contracts"
    $packagingParameters = @{}
    if ($VerifyPackagingArtifacts) {
        $packagingParameters.CheckToolchain = $true
        $packagingParameters.RequireArtifacts = $true
        if (-not [string]::IsNullOrWhiteSpace($PackagingArtifactRoot)) {
            $packagingParameters.ArtifactRoot = [IO.Path]::GetFullPath(
                $PackagingArtifactRoot)
        }
    }
    & $PackagingVerifier @packagingParameters
    Assert-LastScriptSuccess -Description "Standalone Windows packaging"

    $baselineParameters = @{
        LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
    }
    if ($FullTests) {
        $baselineParameters.FullTests = $true
    }
    if ($LaunchDesktop) {
        $baselineParameters.LaunchDesktop = $true
    }
    if ($SkipBuild) {
        $baselineParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $baselineParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $baselineParameters.KeepBuildOutputs = $true
    }
    Write-Host "`n==> Pinned GeoGebra baseline"
    & $BaselineVerifier @baselineParameters
    Assert-LastScriptSuccess -Description "Pinned GeoGebra baseline"

    $frontendParameters = @{
        LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "frontend"
    }
    if ($SkipBuild) {
        $frontendParameters.SkipBuild = $true
    }
    if ($AllowToolchainDownload) {
        $frontendParameters.AllowToolchainDownload = $true
    }
    if ($KeepBuildOutputs) {
        $frontendParameters.KeepBuildOutputs = $true
    }
    Write-Host "`n==> GeoCeDG frontend profile"
    & $FrontendVerifier @frontendParameters
    Assert-LastScriptSuccess -Description "GeoCeDG frontend profile"

    if ($RunBenchmarks) {
        if ([string]::IsNullOrWhiteSpace($BenchmarkOutputPath)) {
            $BenchmarkOutputPath = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) `
                "operational-benchmark.json"
        }
        $benchmarkParameters = @{
            OutputPath = [IO.Path]::GetFullPath($BenchmarkOutputPath)
        }
        Write-Host "`n==> Informational operational benchmark"
        & $BenchmarkRunner @benchmarkParameters
        Assert-LastScriptSuccess -Description "Informational operational benchmark"
    }

    & git -C $RepositoryRoot diff --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --check failed with exit code $LASTEXITCODE."
    }
    & git -C $RepositoryRoot diff --cached --check
    if ($LASTEXITCODE -ne 0) {
        throw "git diff --cached --check failed with exit code $LASTEXITCODE."
    }

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read final repository status."
    }
    if ($finalStatus -ne $InitialStatus) {
        throw "Repository status changed during verification.`nBefore:`n$InitialStatus`nAfter:`n$finalStatus"
    }

    Write-Host "`nAll GeoCeDG verification gates passed."
    Write-Host "Logs: $([IO.Path]::GetFullPath($LogDirectory))"
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

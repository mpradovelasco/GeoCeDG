#requires -Version 7.2
[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [ValidateSet("AUTO", "PRECOMMIT_CANDIDATE", "COMMITTED_CANDIDATE", "AUTHOR_CLOSEOUT",
        "PUBLISHED_REGRESSION")]
    [string]$LifecycleMode = "AUTO",
    [string]$ReviewedTechnicalCommit,
    [string]$CloseoutRecordPath,
    [string]$TechnicalEvidenceBundleDirectory,
    [string]$TechnicalEvidenceBundleSha256,
    [switch]$AuthorCloseoutOnly,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-g9u1")
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")
. (Join-Path $PSScriptRoot "phase-lifecycle.ps1")
. (Join-Path $PSScriptRoot "repository-input-identity.ps1")
. (Join-Path $PSScriptRoot "workspace-profile-validation.ps1")
$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$BaseCommit = "f8a21a087234b18fc13741a0ac2baf80608e9022"
$ReviewCheckpoint = "b492194082f1adc9f981d85d92a58ef57490196f"
$ReviewCheckpointTag = "geocedg-g9u1-author-review-checkpoint-1"
$ReviewCheckpointTagObject = "755f22bd2b101d4ca2ad6bea98429bc2ba941af9"
$Round1CandidateCommit = "fa6339204b87385af79331e434778ca16cd8dcf0"
$Round1CandidateBranch = "codex/g9u1-author-review-stabilization-1"
$Round2AuthorInputCommit = "01c0bec77a30b43b7ebcf75acacdd098840fa2fe"
$Round2CandidateCommit = "5f492d4ee77289d9def89aa6ed431226d2de3457"
$Round2CandidateBranch = "codex/g9u1-author-review-stabilization-2"
$Round3CandidateCommit = "56cf32c922baefeb30c7dff02dbdd5091107ea1a"
$Round3CandidateBranch = "codex/g9u1-author-review-stabilization-3"
$FinalPolishCandidateCommit = "34ffdd9af5f94ded2765e7d495ee66543d4d751f"
$FinalMicroCandidateCommit = "e4ef3d48ea95a0c3243e57dfc703b539d455c33e"
$FinalMicroCandidateTree = "6fbee30b71076cdb35d561e8f179a92c88a2be38"
$ImplementationCommit = "28f7843184cfb202bbfcca1cbcc56a25a7a77bca"
$ImplementationTree = "d08d7beb45d04d6e0f0a478f4c04eb0e97e7e667"
$LifecyclePolicyPath = "geocedg/validation/operations/g9u1-lifecycle-policy.json"
$DefaultCloseoutRecordPath = "geocedg/validation/g9u1/g9u1-author-closeout.json"
$PassTagName = "geocedg-g9u1-pass"
$PassTagMessage = "GeoCeDG G9U1 — PASS — AUTHOR APPROVED"
$Round2AuthorityBlobs = [ordered]@{
    "docs/roadmap/geocedg_roadmap.md" = "7eb5f4dceb402e8188afffed83f6ce6d07bfe3a3"
    "docs/validation/g9u1_author_review_round2.md" = "7c9baa952abc87786d92bbb0b68fe0c7d35d0755"
    "docs/validation/g9u1_construction_workspace_implementation_candidate_report.md" =
        "df9570fe2f8d0793142128c24dd8e01ba54640b2"
    "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json" =
        "41f3e2bfe7997109897b27bcc1e5f63ae76a4b45"
    "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.sha256" =
        "525d91f3304580a5769f86d771810b8b17135b67"
    "geocedg/validation/g9u1/g9u1-construction-workspace-scenarios.json" =
        "ef5b8faa2039325156e65e1f93b43a42b7210b69"
    "tools/agent/verify-g9u1-construction-workspace.ps1" =
        "598abd1816d05721d3908bf2e77d9944674991e0"
}
$Round1AuthorityBlobs = [ordered]@{
    "docs/roadmap/geocedg_roadmap.md" = "78e91680174b2b44070c004b9cfe9c2ed35ac0bf"
    "docs/validation/g9u1_construction_workspace_implementation_candidate_report.md" =
        "06222556c2f1a7483473a5a4b89250d849386472"
    "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json" =
        "22f3fedb4a254768349ac19335b2566961239a8d"
    "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.sha256" =
        "ff988c9d397bc79f6b5f7a781ba799c56aab1a96"
    "geocedg/validation/g9u1/g9u1-construction-workspace-scenarios.json" =
        "f46b5151033bd81889887ff5c8d44d5408c86f87"
    "tools/agent/verify-g9u1-construction-workspace.ps1" =
        "18a462f83fe2507c4a25a47be9c176cfb0642f1f"
}
# These two producer/presentation repairs implement the explicit author-review
# correction. They do not authorize another kernel path or new geometry.
$ReviewKernelPaths = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java"
)
$ReviewScenarioIds = @(1..15 | ForEach-Object { "U1-RV{0:D2}" -f $_ })
$Round2ScenarioIds = @(1..10 | ForEach-Object { "U1-R2-{0:D2}" -f $_ })
$Round2AuthorInputPath = "docs/validation/g9u1_author_resmoke_checklist.md"
$Round2AuthorInputEntryCanonicalHash = "b87a74b6a1e421e6909c6949a442bd3e935920b57a60f9a571a7ec34f6b89f02"
$Round2AuthorInputEntryBlobOid = "b4a2cbb5cca0176be43e1d0c5dad4705683a31ea"
$Round2AuthorInputLiveCanonicalHash = "ba036c052dfc8e03837c1bae2672623b3e0a813b529f1a87c4ecf6647f0ec26b"
$Round2AuthorInputLiveBlobOid = "b253da52983049938dfdb74571b89bc76e112ee4"
$Round2AuthorInputHardBreakCount = 28
$Round2SharedHostPaths = @(
    "source/shared/common-jre/src/main/java/org/geogebra/common/jre/io/MyXMLioJre.java"
)
$Round2SharedLifecyclePaths = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java"
)
$Round3ScenarioIds = @(1..14 | ForEach-Object { "U1-R3-{0:D2}" -f $_ })
$FinalPolishScenarioIds = @(1..8 | ForEach-Object { "U1-FP-{0:D2}" -f $_ })
$FinalMicroScenarioIds = @(1..7 | ForEach-Object { "U1-FM-{0:D2}" -f $_ })
$Round3DispositionPath = "docs/validation/g9u1_author_review_round3.md"
$FinalPolishDispositionPath = "docs/validation/g9u1_final_presentation_polish.md"
$FinalMicroDispositionPath = "docs/validation/g9u1_final_micro_presentation_adjustments.md"
$FinalPolishMainPaths = @(
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGWorkspaceController.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/resources/GeoCeDGBrandingResource.java",
    "source/desktop/desktop/src/main/java/org/geogebra/desktop/GeoGebra.java",
    "source/desktop/desktop/src/main/java/org/geogebra/desktop/SplashWindow.java",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-startup-361x480.png"
)
$FinalPolishTestPaths = @(
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ActionRegistryTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ProfileCompilerTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGBrandingResourceTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGProfileTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGSplashWindowTest.java"
)
$FinalMicroMainPaths = @(
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGToolbarContainer.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserTools.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGWorkspaceController.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/resources/GeoCeDGToolImageResource.java"
)
$FinalMicroTestPaths = @(
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ProfileCompilerTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1UserToolLibraryTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java"
)
$FinalToolbarVisualMainPaths = @(
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGToolbarContainer.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserTools.java",
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGWorkspaceController.java"
)
$FinalToolbarVisualTestPaths = @(
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1UserToolLibraryTest.java",
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java"
)
$FinalToolbarVisualScenarioIds = @("U1-FM-03", "U1-FM-04", "U1-FM-06")
$FinalToolbarVisualMethods = @(
    "org.geocedg.desktop.G9U1WorkspaceSurfaceTest#toolbarUsesExactProfileOrderAndNormalLastUsedMixedFlyoutsAtHighDpi",
    "org.geocedg.desktop.G9U1UserToolLibraryTest#iconlessPinnedButtonsAndGroupsStayCompactWithFullAccessibleIdentity",
    "org.geocedg.desktop.G9U1UserToolLibraryTest#pngPinIconPersistsDigestAndTransparentAspectPaddingWithoutDocumentMutation",
    "org.geocedg.desktop.G9U1UserToolLibraryTest#groupedPngPinsKeepIndividualPopupIconsWithoutChangingToolIdentity"
)
$FinalPolishBrandingPins = [ordered]@{
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixSnapshot.png" =
        @(251689, "abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637")
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-startup-361x480.png" =
        @(94272, "664ece93d38a6fc57ae3b29ede79161f928265b4fb069b5e72519b9aab494195")
}
$Round3SharedInstrumentationPaths = @()
$Round3SharedLocalizationPaths = @(
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_en.properties",
    "source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_es.properties"
)
$Round3ExternalDiagnostic = [ordered]@{
    className = "org.geocedg.desktop.G9U1Revision3AlgebraEditingDiagnostic"
    source = "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1Revision3AlgebraEditingDiagnostic.java"
    method = "authorRevision3ExercisesEveryOrdinaryAlgebraEditRoute"
    input = "artifacts/smoke-test-g9u1/Revision3.cedg"
    result = "artifacts/g9u1-author-review-round3/dev-revision3-diagnostic-01/verification-result.json"
    resultRawSha256 = "a5c97a14bc0d01a42118d270febb6ec1e96da656ad8eeb71fe680b365d2af0e9"
    summaryRawSha256 = "234269d032c6e78bd53d7fe5fa0448be242c9aeaaa23a9fef5a198881040f46b"
    junitRawSha256 = "2a1aa315d0cf8ddeb2eff1916cab97dd3f83500111ca8e43cb7b73d788963bf1"
}
$Round3BrandingResourcePins = [ordered]@{
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixTopBar.png" =
        @(113783, "08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1")
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixSnapshot.png" =
        @(251689, "abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637")
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-application-icon-64.png" =
        @(5676, "448ea5b510f952d27ddddec3005911b4e1f1203ee35f55fcca857098e53fed97")
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-application.ico" =
        @(65873, "e5dac1dd3a556f4ce9747f00d272281e9a571ecc5e757180ba1c6750b664cd73")
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-startup-542x720.png" =
        @(155029, "04d5e79b99b0f25536690b8af9d71ba034fbaf2b0d401ae40b5822942b917a78")
}
$Round3LocalAuthorInputPins = [ordered]@{
    "artifacts/smoke-test-g9u1/Revision2.cedg" =
        @(17209, "527f96b516afdd93923e228ad0ffe0a3fc0bebd79e30b228bec7e7455ed53ab6")
    "artifacts/smoke-test-g9u1/Revision3.cedg" =
        @(14110, "351955499d47d0407ab11c906da6e9b6d2ab636b0beef4e67c3edfddecccd939")
    "artifacts/author-input/g9u1-branding/helixTopBar.png" =
        @(113783, "08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1")
    "artifacts/author-input/g9u1-branding/helixSnapshot.png" =
        @(251689, "abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637")
}
$CanonicalBinaryInventoryPaths = @(
    "source/desktop/desktop/src/test/resources/org/geocedg/desktop/g9u1-review/TestBasic1.cedg",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixTopBar.png",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/helixSnapshot.png",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-application-icon-64.png",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-application.ico",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-startup-542x720.png",
    "source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/derived/geocedg-startup-361x480.png"
)
$Round3MethodReplacements = [ordered]@{
    "duplicateMenuClusterFailsClosed" = "duplicateMenuPresentationGroupFailsClosed"
    "unknownAndDuplicateDirectMenuActionsFailClosed" =
        "unknownAndDuplicatePresentationActionsFailClosed"
    "menuOrderOptionsProjectionAndActionIdentityComeFromOneCatalog" =
        "menuOrderPresentationAndActionIdentityComeFromOneCatalog"
    "documentCollisionAtInstallOrActivationDoesNotReplaceMacro" =
        "equivalentEmbeddedMacroIsAdoptedWithoutDuplicateOrReplacement"
}
$FinalPolishMethodReplacements = [ordered]@{
    "toolbarContainsThirtyTwoCuratedModesWhileCatalogRetainsAllSixtySix" =
        "nativeToolbarContainsFortyFourCuratedModesWhileCatalogRetainsAllSixtySix"
    "compactProductToolbarUsesOnlyDeclaredNonModeActionsAtHighDpi" =
        "profileFlyoutsGroupMixedActionsWithoutDetachedSplineButtonAtHighDpi"
}
$FinalMicroMethodReplacements = [ordered]@{
    "profileFlyoutsGroupMixedActionsWithoutDetachedSplineButtonAtHighDpi" =
        "toolbarUsesExactProfileOrderAndNormalLastUsedMixedFlyoutsAtHighDpi"
}
$ReviewMethodReplacements = [ordered]@{
    "toolbarContainsExactlySixtySixUniqueRealModeIds" =
        "toolbarContainsThirtyTwoCuratedModesWhileCatalogRetainsAllSixtySix"
    "familyPaletteRetainsAllElevenFamiliesAtHighDpi" =
        "compactProductToolbarUsesOnlyDeclaredNonModeActionsAtHighDpi"
    "keyboardFocusScrollsOffscreenFamilyIntoNarrowViewport" =
        "realApplicationMenuIsAboveContentAndToolbarIsItsStrictSubset"
}
$PromptPath = ".github/prompts/tasks/g9u1-construction-workspace-after-g9s1-r1.prompt.md"
$EvidencePath = "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json"
$ScenarioPath = "geocedg/validation/g9u1/g9u1-construction-workspace-scenarios.json"
$HashPath = "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.sha256"
$ReportPath = "docs/validation/g9u1_construction_workspace_implementation_candidate_report.md"
$LifecycleContext = $null
$SelectedLifecycleMode = $LifecycleMode
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
if ([string]::IsNullOrWhiteSpace($CanonicalSummaryPath)) {
    $CanonicalSummaryPath = Join-Path $LogDirectory "canonical-summary.json"
}

function Assert-U1 {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}
function Read-U1 {
    param([string]$Path)
    $full = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $Path "G9U1 authority"
    Assert-U1 (Test-Path -LiteralPath $full -PathType Leaf) "Missing G9U1 authority: $Path"
    return [IO.File]::ReadAllText($full, [Text.UTF8Encoding]::new($false, $true))
}
function Get-U1Hash {
    param([string]$Path)
    return Get-GeoCeDGPhaseLifecycleHash ([Text.UTF8Encoding]::new($false).GetBytes(
        (Read-U1 $Path).Replace("`r`n", "`n").Replace("`r", "`n")))
}
function Get-U1Git {
    param([string[]]$Arguments)
    return Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot $Arguments
}
function Get-U1CommitHash {
    param([string]$Commit, [string]$Path)
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        (Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $Commit $Path))
    if ($text.Length -gt 0 -and $text[0] -eq [char]0xfeff) { $text = $text.Substring(1) }
    return Get-GeoCeDGPhaseLifecycleHash ([Text.UTF8Encoding]::new($false).GetBytes(
        $text.Replace("`r`n", "`n").Replace("`r", "`n")))
}
function Get-U1ManifestAuthorityHash {
    param([string]$Path)
    # The committed-candidate manifest is part of the author-reviewed product
    # checkpoint. Later lifecycle plumbing is authenticated independently and
    # must not make the historical candidate manifest self-referential.
    if ($SelectedLifecycleMode -ceq "COMMITTED_CANDIDATE") {
        return Get-U1CommitHash $ImplementationCommit $Path
    }
    if ($SelectedLifecycleMode -cin @("AUTHOR_CLOSEOUT", "PUBLISHED_REGRESSION")) {
        $policy = if ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION") {
            Assert-U1 ($null -ne $LifecycleContext -and
                -not [string]::IsNullOrWhiteSpace([string]$LifecycleContext.closeoutCommit)) `
                "Published manifest authority requires an authenticated tagged closeout."
            ConvertFrom-GeoCeDGPhaseLifecycleJson `
                (Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
                    ([string]$LifecycleContext.closeoutCommit) $LifecyclePolicyPath) `
                "published tagged G9U1 lifecycle policy"
        } else {
            Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot `
                (Join-Path $RepositoryRoot $LifecyclePolicyPath)
        }
        $statusPaths = @($policy.closeout.literalReplacements | ForEach-Object {
                [string]$_.path
            } | Sort-Object -Unique -CaseSensitive)
        if ($Path -cnotin $statusPaths) {
            return Get-U1CommitHash $ImplementationCommit $Path
        }
    }
    return Get-U1Hash $Path
}
function Get-U1SourceAuthority {
    param([string]$Path)
    $entry = (Get-U1Git @("ls-files", "--stage", "--", $Path)).Trim()
    if ($entry) {
        $match = [regex]::Match($entry, '^([0-7]{6}) [0-9a-f]{40} 0\t')
        Assert-U1 $match.Success "Unsupported/unmerged tracked input: $Path"
        return [ordered]@{
            path = $Path; kind = "TRACKED_GIT_CANONICAL"
            mode = $match.Groups[1].Value
            blobOid = (Get-U1Git @("hash-object", "--path=$Path", "--", $Path)).Trim()
        }
    }
    $file = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $Path "G9U1 untracked input"
    return [ordered]@{
        path = $Path; kind = "CONSUMED_UNTRACKED_RAW"
        sha256 = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
function Assert-U1Set {
    param([string[]]$Actual, [string[]]$Expected, [string]$Description)
    $a = @($Actual | Sort-Object -Unique -CaseSensitive)
    $e = @($Expected | Sort-Object -Unique -CaseSensitive)
    Assert-U1 ($a.Count -eq $Actual.Count -and $e.Count -eq $Expected.Count -and
        @((Compare-Object $a $e -CaseSensitive)).Count -eq 0) "$Description differs or contains duplicates."
}

function Get-U1CandidatePaths {
    if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
        return @(Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot $BaseCommit `
            $ImplementationCommit)
    }
    $paths = @((Get-U1Git @("diff", "--name-only", "--no-renames", $BaseCommit)).Split("`n") +
        (Get-U1Git @("ls-files", "--others", "--exclude-standard")).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
    return $paths
}

function Get-U1FinalMicroPresentationDeltaPaths {
    if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
        return @(Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot `
            $FinalPolishCandidateCommit $ImplementationCommit)
    }
    return @((Get-U1Git @("diff", "--name-only", "--no-renames",
        $FinalPolishCandidateCommit)).Split("`n") +
        (Get-U1Git @("ls-files", "--others", "--exclude-standard")).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
}

function Get-U1FinalToolbarDeltaPaths {
    if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
        return @(Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot `
            $FinalMicroCandidateCommit $ImplementationCommit)
    }
    return @((Get-U1Git @("diff", "--name-only", "--no-renames",
        $FinalMicroCandidateCommit)).Split("`n") +
        (Get-U1Git @("ls-files", "--others", "--exclude-standard")).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
}

function Initialize-U1Lifecycle {
    if ($SelectedLifecycleMode -ceq "AUTO") {
        $hasCloseoutInput = $AuthorCloseoutOnly -or
            -not [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -or
            -not [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -or
            -not [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -or
            -not [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)
        $hasPublishedTag = -not $AuthorCloseoutOnly -and
            (Get-U1Git @("tag", "--list", $PassTagName)).Trim() -ceq $PassTagName
        $script:SelectedLifecycleMode = if ($hasCloseoutInput) {
            "AUTHOR_CLOSEOUT"
        } elseif ($hasPublishedTag) {
            "PUBLISHED_REGRESSION"
        } elseif (Test-Path -LiteralPath (Join-Path $RepositoryRoot $LifecyclePolicyPath) `
                -PathType Leaf) {
            "COMMITTED_CANDIDATE"
        } else {
            "PRECOMMIT_CANDIDATE"
        }
    }
    if ($SelectedLifecycleMode -ceq "PRECOMMIT_CANDIDATE") {
        Assert-U1 (-not $AuthorCloseoutOnly -and
            [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "Historical precommit mode cannot accept closeout evidence."
        return
    }
    Assert-U1 (($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT") -eq
        [bool]$AuthorCloseoutOnly) `
        "AUTHOR_CLOSEOUT is an explicit documentary check; committed mode runs live gates."
    if ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION") {
        Assert-U1 ([string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "Published regression cannot accept closeout overrides."
        $script:LifecycleContext = Get-GeoCeDGPhasePublishedTagRegressionContext `
            -RepositoryRoot $RepositoryRoot -PassTagName $PassTagName `
            -ExpectedTagMessage $PassTagMessage -ExpectedPhase "G9U1" `
            -ExpectedImplementationCommit $ImplementationCommit `
            -ExpectedPolicyPath $LifecyclePolicyPath
        return
    }
    if ($SelectedLifecycleMode -ceq "COMMITTED_CANDIDATE") {
        Assert-U1 ([string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "Committed-candidate mode cannot accept author-closeout authority."
    } else {
        Assert-U1 (-not [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
            -not [string]::IsNullOrWhiteSpace($CloseoutRecordPath) -and
            -not [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleDirectory) -and
            -not [string]::IsNullOrWhiteSpace($TechnicalEvidenceBundleSha256)) `
            "AUTHOR_CLOSEOUT requires an exact reviewed SHA, record and evidence bundle."
    }
    $parameters = @{
        RepositoryRoot = $RepositoryRoot
        PolicyPath = (Join-Path $RepositoryRoot $LifecyclePolicyPath)
        ExpectedImplementationCommit = $ImplementationCommit
        Mode = $SelectedLifecycleMode
    }
    if ($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT") {
        $resolvedCloseoutRecord = if ([IO.Path]::IsPathRooted($CloseoutRecordPath)) {
            [IO.Path]::GetFullPath($CloseoutRecordPath)
        } else {
            [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $CloseoutRecordPath))
        }
        $expectedCloseoutRecord = [IO.Path]::GetFullPath(
            (Join-Path $RepositoryRoot $DefaultCloseoutRecordPath))
        Assert-U1 ($resolvedCloseoutRecord.Equals($expectedCloseoutRecord,
            [StringComparison]::OrdinalIgnoreCase)) `
            "AUTHOR_CLOSEOUT record path differs from the frozen G9U1 authority."
        $parameters.ReviewedTechnicalCommit = $ReviewedTechnicalCommit
        $parameters.CloseoutRecordPath = $resolvedCloseoutRecord
        $parameters.BundleDirectory = if (
            [IO.Path]::IsPathRooted($TechnicalEvidenceBundleDirectory)) {
            [IO.Path]::GetFullPath($TechnicalEvidenceBundleDirectory)
        } else {
            [IO.Path]::GetFullPath(
                (Join-Path $RepositoryRoot $TechnicalEvidenceBundleDirectory))
        }
        $parameters.BundleSha256 = $TechnicalEvidenceBundleSha256
        $parameters.PendingCloseout = (Get-U1Git @("rev-parse", "HEAD")).Trim() -ceq
            $ReviewedTechnicalCommit
    }
    $script:LifecycleContext = Get-GeoCeDGPhaseLifecycleContext @parameters
}

function Assert-U1LifecycleEntry {
    $head = (Get-U1Git @("rev-parse", "HEAD")).Trim()
    Assert-U1 ((Get-U1Git @("rev-parse", "$ImplementationCommit^{tree}")).Trim() -ceq
        $ImplementationTree) "The author-reviewed G9U1 implementation tree changed."
    if ($SelectedLifecycleMode -ceq "PRECOMMIT_CANDIDATE") {
        Assert-U1 ($head -ceq $FinalMicroCandidateCommit) `
            "Historical G9U1 precommit mode requires the exact entry HEAD."
        Assert-U1 ((Get-U1Git @("branch", "--show-current")).Trim() -ceq
            $Round3CandidateBranch) "Unexpected historical G9U1 candidate branch."
    } else {
        Assert-U1 ($null -ne $LifecycleContext -and
            $head -ceq $LifecycleContext.CurrentHead -and
            $LifecycleContext.ImplementationCommit -ceq $ImplementationCommit) `
            "Committed G9U1 lifecycle authority is not authenticated."
    }
}

function Resolve-U1TestReference {
    param([string]$Reference)
    $parts = $Reference -split '#', 2
    Assert-U1 ($parts.Count -eq 2) "Invalid G9U1 test reference: $Reference"
    $method = $parts[1]
    if ($Round3MethodReplacements.Contains($method)) {
        $method = $Round3MethodReplacements[$method]
    }
    if ($FinalPolishMethodReplacements.Contains($method)) {
        $method = $FinalPolishMethodReplacements[$method]
    }
    if ($FinalMicroMethodReplacements.Contains($method)) {
        $method = $FinalMicroMethodReplacements[$method]
    }
    return "$($parts[0])#$method"
}

function Read-U1Commit {
    param([string]$Commit, [string]$Path)
    return [Text.UTF8Encoding]::new($false, $true).GetString(
        (Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $Commit $Path))
}
function Read-U1Checkpoint {
    param([string]$Path)
    return Read-U1Commit $ReviewCheckpoint $Path
}
function Assert-U1ReviewContracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $ReviewCheckpoint, "HEAD"))
    [void](Get-U1Git @("merge-base", "--is-ancestor", $ReviewCheckpoint, $Round1CandidateCommit))
    Assert-U1 ((Get-U1Git @("rev-parse", "refs/tags/$ReviewCheckpointTag")).Trim() -ceq
        $ReviewCheckpointTagObject) "The non-PASS author-review checkpoint tag changed."
    Assert-U1 ((Get-U1Git @("cat-file", "-t", "refs/tags/$ReviewCheckpointTag")).Trim() -ceq
        "tag") "The protected author-review checkpoint is not annotated."
    Assert-U1 ((Get-U1Git @("rev-parse", "refs/tags/$ReviewCheckpointTag^{}")).Trim() -ceq
        $ReviewCheckpoint) "The protected author-review checkpoint peel changed."
    foreach ($ref in @("refs/heads/codex/g9u1-construction-workspace-after-r1",
        "refs/remotes/origin/codex/g9u1-construction-workspace-after-r1")) {
        Assert-U1 ((Get-U1Git @("rev-parse", $ref)).Trim() -ceq $ReviewCheckpoint) "The protected candidate ref changed: $ref"
    }
    $review = $Evidence.authorReviewStabilization
    Assert-U1 ($review.checkpoint.commit -ceq $ReviewCheckpoint -and
        $review.checkpoint.tag -ceq $ReviewCheckpointTag -and
        $review.checkpoint.tagObject -ceq $ReviewCheckpointTagObject -and
        $review.historicalAuthorReview -ceq "COMPLETED_WITH_FINDINGS_NOT_PASS" -and
        $review.authorResmoke -ceq "PENDING" -and
        $review.noNewGeometricSemantics -eq $true) "Missing or inconsistent author-review successor authority."
    $historicalEvidence = Read-U1Checkpoint $EvidencePath | ConvertFrom-Json -Depth 100
    $historicalScenarios = Read-U1Checkpoint $ScenarioPath | ConvertFrom-Json -Depth 100
    $round1Evidence = Read-U1Commit $Round1CandidateCommit $EvidencePath | ConvertFrom-Json -Depth 100
    Assert-U1 (($review | ConvertTo-Json -Depth 100 -Compress) -ceq
        ($round1Evidence.authorReviewStabilization | ConvertTo-Json -Depth 100 -Compress)) `
        "The published round-one review record was rewritten."
    Assert-U1 (@($historicalEvidence.inventory.paths).Count -eq 96 -and
        $historicalEvidence.inventory.pathCount -eq 96 -and
        @($historicalScenarios.scenarios).Count -eq 138 -and
        @($historicalScenarios.focusedJUnit.classes.methods).Count -eq 132 -and
        $review.checkpoint.pathCount -eq 96 -and $review.checkpoint.focusedTests -eq 132 -and
        $review.checkpoint.scenarioCount -eq 138) "Historical 96-path/132-test/138-scenario cohort was reinterpreted."
    foreach ($pin in @($review.checkpoint.authorityBlobs)) {
        Assert-U1 ($pin.path -cin @($EvidencePath, $ScenarioPath, $HashPath, $ReportPath,
            "tools/agent/verify-g9u1-construction-workspace.ps1")) "Unknown historical review authority."
        $tree = (Get-U1Git @("ls-tree", $ReviewCheckpoint, "--", $pin.path)).Trim()
        Assert-U1 ($tree -ceq "$($pin.mode) blob $($pin.blobOid)`t$($pin.path)") "Historical review authority blob changed: $($pin.path)"
    }
    Assert-U1Set @($review.checkpoint.authorityBlobs.path) @($EvidencePath, $ScenarioPath,
        $HashPath, $ReportPath, "tools/agent/verify-g9u1-construction-workspace.ps1") "Historical review authority pins"
    Assert-U1Set @($review.allowedSharedKernelCorrections.path) $ReviewKernelPaths "Author-review kernel correction allowlist"
    $kernel = @($Paths | Where-Object { $_ -match '^source/.*/org/geocedg/common/kernel/' })
    Assert-U1Set $kernel $ReviewKernelPaths "Only the two expressly authorized kernel implementation repairs are allowed"
    $delta = @((Get-U1Git @("diff", "--name-only", $ReviewCheckpoint,
        $Round1CandidateCommit)).Split("`n") | Where-Object { $_ } |
        Sort-Object -Unique -CaseSensitive)
    Assert-U1Set $delta @($review.inventory.deltaPaths) "Exact author-review successor delta"
    Assert-U1 ($review.inventory.deltaPathCount -eq $delta.Count -and
        $Evidence.inventory.pathCount -eq $Paths.Count -and
        $Evidence.inventory.sourcePathCount -eq @($Paths | Where-Object { $_ -cmatch '^source/' }).Count) "Review inventory counters drifted."
    foreach ($original in @($historicalScenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $original.id })
        Assert-U1 ($current.Count -eq 1) "Historical scenario was dropped/duplicated: $($original.id)"
        foreach ($field in @("id", "group", "assertion", "topic", "procedure")) {
            Assert-U1 ($current[0].$field -ceq $original.$field) "Historical scenario meaning changed: $($original.id)/$field"
        }
    }
    foreach ($historicalClass in @($historicalScenarios.focusedJUnit.classes)) {
        $currentClass = @($Scenarios.focusedJUnit.classes | Where-Object { $_.name -ceq $historicalClass.name })
        Assert-U1 ($currentClass.Count -eq 1 -and $currentClass[0].source -ceq $historicalClass.source -and
            $currentClass[0].module -ceq $historicalClass.module) "Historical focused class changed: $($historicalClass.name)"
        foreach ($method in @($historicalClass.methods)) {
            $mapped = if ($ReviewMethodReplacements.Contains($method)) {
                $ReviewMethodReplacements[$method]
            } else { $method }
            if ($Round3MethodReplacements.Contains($mapped)) {
                $mapped = $Round3MethodReplacements[$mapped]
            }
            if ($FinalPolishMethodReplacements.Contains($mapped)) {
                $mapped = $FinalPolishMethodReplacements[$mapped]
            }
            if ($FinalMicroMethodReplacements.Contains($mapped)) {
                $mapped = $FinalMicroMethodReplacements[$mapped]
            }
            Assert-U1 ($mapped -cin @($currentClass[0].methods)) "Historical focused obligation was dropped: $($historicalClass.name)#$method"
        }
    }
    $fixture = $review.authorFixture
    Assert-U1 ($fixture.path -ceq "source/desktop/desktop/src/test/resources/org/geocedg/desktop/g9u1-review/TestBasic1.cedg" -and
        $fixture.bytes -eq 31885 -and $fixture.rawSha256 -ceq
        "0791895e1133d4a44ff26c88760cfc951db787c42056a8b5758c79a9b5687be0") "Author archive provenance changed."
    $fixturePath = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $fixture.path "author archive"
    Assert-U1 ((Get-Item -LiteralPath $fixturePath).Length -eq $fixture.bytes -and
        (Get-FileHash -LiteralPath $fixturePath -Algorithm SHA256).Hash.ToLowerInvariant() -ceq
        $fixture.rawSha256) "The historical malformed author archive was modified."
    Assert-U1 ($review.requiredVerification.PHASE -ceq "FRESH_SUCCESSOR_COHORT" -and
        $review.requiredVerification.FULL -ceq "FRESH_CLEAN_SUCCESSOR_COHORT" -and
        $review.requiredVerification.COMPOSED -ceq "EXECUTED_WITHIN_FULL_NOT_A_SEPARATE_ROOT" -and
        $review.requiredVerification.historicalExecutionReusedAsNew -eq $false) "Review verification cannot reuse the previous product cohort as new execution."
    Assert-U1Set @($review.documentationPaths) @(
        "docs/user/geocedg_construction_quick_guide.md",
        "docs/validation/g9u1_author_manual_review_round1.md",
        "docs/validation/g9u1_author_resmoke_checklist.md",
        "docs/validation/g9u1_frontend_review_matrix.md",
        "docs/validation/g9u1_icon_review.md",
        "docs/validation/g9u1_native_lifecycle_review.md",
        "docs/validation/g9u1_user_tools_review.md") "Required author-review documentation"
    foreach ($path in @($review.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text, '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot, [IO.Path]::GetFullPath(
                (Join-Path (Split-Path -Parent (Join-Path $RepositoryRoot $path)) $target))).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative "author-review documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) "Broken review link: $path -> $target"
        }
    }
}

function Assert-U1Round2Contracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $Round1CandidateCommit, "HEAD"))
    Assert-U1 ((Get-U1Git @("rev-parse", "$Round2AuthorInputCommit^")).Trim() -ceq
        $Round1CandidateCommit) "The round-two author-input commit parent changed."
    [void](Get-U1Git @("merge-base", "--is-ancestor", $Round2AuthorInputCommit, "HEAD"))
    $authorInputDelta = @((Get-U1Git @("diff-tree", "--no-commit-id", "--name-only", "-r",
        $Round2AuthorInputCommit)).Split("`n") | Where-Object { $_ })
    Assert-U1Set $authorInputDelta @($Round2AuthorInputPath) `
        "Round-two author-input provenance commit delta"
    $authorInputTree = (Get-U1Git @("ls-tree", $Round2AuthorInputCommit, "--",
        $Round2AuthorInputPath)).Trim()
    Assert-U1 ($authorInputTree -ceq
        "100644 blob $Round2AuthorInputEntryBlobOid`t$Round2AuthorInputPath") `
        "The round-two author-input provenance blob changed."
    foreach ($ref in @("refs/heads/$Round1CandidateBranch",
        "refs/remotes/origin/$Round1CandidateBranch")) {
        Assert-U1 ((Get-U1Git @("rev-parse", $ref)).Trim() -ceq $Round1CandidateCommit) `
            "The published round-one stabilization ref changed: $ref"
    }
    foreach ($pin in $Round1AuthorityBlobs.GetEnumerator()) {
        $tree = (Get-U1Git @("ls-tree", $Round1CandidateCommit, "--", $pin.Key)).Trim()
        Assert-U1 ($tree -ceq "100644 blob $($pin.Value)`t$($pin.Key)") `
            "Round-one stabilization authority changed: $($pin.Key)"
    }
    $round2 = $Evidence.authorReviewStabilizationRound2
    Assert-U1 ($round2.baseline.commit -ceq $Round1CandidateCommit -and
        $round2.baseline.branch -ceq $Round1CandidateBranch -and
        $round2.historicalRound1 -ceq "PUBLISHED_CANDIDATE_PRESERVED" -and
        $round2.nextAuthorResmoke -ceq "PENDING" -and
        $round2.noNewGeometricSemantics -eq $true) `
        "Missing or inconsistent round-two successor authority."
    Assert-U1Set @($round2.baseline.authorityBlobs.path) @($Round1AuthorityBlobs.Keys) `
        "Round-one successor authority pins"
    foreach ($pin in @($round2.baseline.authorityBlobs)) {
        Assert-U1 ($pin.mode -ceq "100644" -and
            $Round1AuthorityBlobs[$pin.path] -ceq $pin.blobOid) `
            "Round-one authority pin differs: $($pin.path)"
    }
    $round2Delta = @((Get-U1Git @("diff", "--name-only", $Round1CandidateCommit,
        $Round2CandidateCommit)).Split("`n") | Where-Object { $_ } |
        Sort-Object -Unique -CaseSensitive)
    Assert-U1Set $round2Delta @($round2.inventory.deltaPaths) `
        "Exact author-review round-two successor delta"
    Assert-U1 ($round2.inventory.baseCommit -ceq $Round1CandidateCommit -and
        $round2.inventory.deltaPathCount -eq $round2Delta.Count -and
        $round2.inventory.sourcePathCount -eq
            @($round2Delta | Where-Object { $_ -cmatch '^source/' }).Count) `
        "Round-two inventory counters drifted."
    $round2Shared = @($round2Delta | Where-Object {
        $_ -cmatch '^source/shared/common-jre/src/main/' })
    Assert-U1Set $round2Shared $Round2SharedHostPaths `
        "Only the expressly authorized shared macro-host correction is allowed"
    $round2SharedLifecycle = @($round2Delta | Where-Object {
        $_ -cmatch '^source/shared/common/src/main/' })
    Assert-U1Set $round2SharedLifecycle $Round2SharedLifecyclePaths `
        "Only the expressly authorized existing metric-DAG lifecycle correction is allowed"
    Assert-U1Set @($round2.allowedSharedMacroHostCorrection.path) $Round2SharedHostPaths `
        "Round-two shared macro-host allowlist"
    Assert-U1Set @($round2.allowedSharedMetricLifecycleCorrection.path) `
        $Round2SharedLifecyclePaths "Round-two metric lifecycle allowlist"
    $authorInput = $round2.authorInput
    Assert-U1 ($authorInput.path -ceq $Round2AuthorInputPath -and
        $authorInput.commit -ceq $Round2AuthorInputCommit -and
        $authorInput.parentCommit -ceq $Round1CandidateCommit -and
        @($authorInput.commitDeltaPaths).Count -eq 1 -and
        $authorInput.commitDeltaPaths[0] -ceq $Round2AuthorInputPath -and
        $authorInput.entryRawSha256 -ceq $Round2AuthorInputEntryCanonicalHash -and
        $authorInput.entryCanonicalLfSha256 -ceq $Round2AuthorInputEntryCanonicalHash -and
        $authorInput.entryGitCanonicalBlobOid -ceq $Round2AuthorInputEntryBlobOid -and
        $authorInput.classification -ceq "AUTHOR_INPUT_NOT_AGENT_EXECUTION" -and
        $authorInput.authorStatementsModified -eq $false -and
        $authorInput.liveRepresentation.canonicalLfSha256 -ceq
            $Round2AuthorInputLiveCanonicalHash -and
        $authorInput.liveRepresentation.gitCanonicalBlobOid -ceq
            $Round2AuthorInputLiveBlobOid -and
        $authorInput.liveRepresentation.replacementCount -eq
            $Round2AuthorInputHardBreakCount -and
        $authorInput.liveRepresentation.authorStatementsModified -eq $false -and
        $authorInput.liveRepresentation.exactlyReversibleToProvenanceBlob -eq $true -and
        $authorInput.liveRepresentation.classification -ceq
            "COMMONMARK_PRESENTATION_NORMALIZATION_NOT_AGENT_EXECUTION_EVIDENCE") `
        "Round-two author-input authority differs."
    $authorInputEntryBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
        $Round2AuthorInputCommit $Round2AuthorInputPath
    Assert-U1 ((Get-GeoCeDGPhaseLifecycleHash $authorInputEntryBytes) -ceq
        $Round2AuthorInputEntryCanonicalHash) `
        "The byte-exact author-input provenance content changed."
    $authorInputEntry = [Text.UTF8Encoding]::new($false, $true).GetString(
        $authorInputEntryBytes).Replace("`r`n", "`n").Replace("`r", "`n")
    $entryHorizontalWhitespace = @([regex]::Matches(
        $authorInputEntry, '(?m)[ \t]+(?=\n|\z)'))
    Assert-U1 ($entryHorizontalWhitespace.Count -eq $Round2AuthorInputHardBreakCount -and
        @($entryHorizontalWhitespace | Where-Object { $_.Value -cne "  " }).Count -eq 0) `
        "The author-input provenance hard-break inventory changed."
    $authorInputLive = (Read-U1 $Round2AuthorInputPath).Replace(
        "`r`n", "`n").Replace("`r", "`n")
    $expectedLive = $authorInputEntry.Replace("  `n", "<br>`n")
    Assert-U1 ($authorInputLive -ceq $expectedLive) `
        "The live author checklist is not the exact presentation-only normalization."
    Assert-U1 (@([regex]::Matches($authorInputLive, '(?m)<br>(?=\n|\z)')).Count -eq
        $Round2AuthorInputHardBreakCount) "The normalized CommonMark hard-break count differs."
    Assert-U1 (@([regex]::Matches($authorInputLive, '(?m)[ \t]+(?=\n|\z)')).Count -eq 0) `
        "The normalized author checklist retains horizontal trailing whitespace."
    Assert-U1 ($authorInputLive.Replace("<br>`n", "  `n") -ceq $authorInputEntry) `
        "The author checklist normalization is not exactly reversible."
    Assert-U1 ((Get-U1Hash $Round2AuthorInputPath) -ceq
        $Round2AuthorInputLiveCanonicalHash) `
        "The live author checklist normalized representation changed."
    Assert-U1 ((Get-U1Git @("hash-object", "--path=$Round2AuthorInputPath", "--",
        $Round2AuthorInputPath)).Trim() -ceq $Round2AuthorInputLiveBlobOid) `
        "The live author checklist normalized Git content changed."
    $round1Scenarios = Read-U1Commit $Round1CandidateCommit $ScenarioPath |
        ConvertFrom-Json -Depth 100
    Assert-U1 (@($round1Scenarios.scenarios).Count -eq 153 -and
        @($round1Scenarios.focusedJUnit.classes).Count -eq 18 -and
        @($round1Scenarios.focusedJUnit.classes.methods).Count -eq 183) `
        "Published round-one scenario/test authority differs."
    foreach ($historical in @($round1Scenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $historical.id })
        Assert-U1 ($current.Count -eq 1) "Round-one scenario was dropped/duplicated: $($historical.id)"
        Assert-U1 (($current[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historical | ConvertTo-Json -Depth 100 -Compress)) `
            "Published round-one scenario meaning changed: $($historical.id)"
    }
    Assert-U1 ($Scenarios.authorReviewRound2.baselineCommit -ceq $Round1CandidateCommit -and
        $Scenarios.authorReviewRound2.authorInputCommit -ceq $Round2AuthorInputCommit -and
        $Scenarios.authorReviewRound2.authorInput -ceq $Round2AuthorInputPath -and
        $Scenarios.authorReviewRound2.authorInputEntryCanonicalLfSha256 -ceq
            $Round2AuthorInputEntryCanonicalHash -and
        $Scenarios.authorReviewRound2.authorInputLiveCanonicalLfSha256 -ceq
            $Round2AuthorInputLiveCanonicalHash -and
        $Scenarios.authorReviewRound2.authorInputEntryBlobOid -ceq
            $Round2AuthorInputEntryBlobOid -and
        $Scenarios.authorReviewRound2.authorInputLiveBlobOid -ceq
            $Round2AuthorInputLiveBlobOid -and
        $Scenarios.authorReviewRound2.hardBreakNormalization -ceq
            "TWO_ASCII_SPACES_PLUS_LF_TO_EXPLICIT_HTML_BR_PLUS_LF" -and
        $Scenarios.authorReviewRound2.hardBreakReplacementCount -eq
            $Round2AuthorInputHardBreakCount -and
        $Scenarios.authorReviewRound2.exactlyReversibleToProvenanceBlob -eq $true -and
        $Scenarios.authorReviewRound2.authorStatementsModified -eq $false -and
        $Scenarios.authorReviewRound2.liveRepresentationClassification -ceq
            "COMMONMARK_PRESENTATION_NORMALIZATION_NOT_AGENT_EXECUTION_EVIDENCE" -and
        $Scenarios.authorReviewRound2.historicalScenarioCount -eq 153 -and
        $Scenarios.authorReviewRound2.historicalFocusedTests -eq 183 -and
        $Scenarios.authorReviewRound2.supersededHistoricalScenario -ceq "U1-RV06" -and
        $Scenarios.authorReviewRound2.manualAuthorResmoke -ceq "PENDING") `
        "Round-two scenario provenance differs."
    Assert-U1Set @($Scenarios.authorReviewRound2.additionalScenarioIds) $Round2ScenarioIds `
        "Round-two scenario IDs"
    Assert-U1 ($round2.focusedInventory.classes -eq 21 -and
        $round2.focusedInventory.methods -eq 204 -and
        $round2.focusedInventory.desktopMethods -eq 190 -and
        $round2.focusedInventory.sharedMethods -eq 14 -and
        $round2.focusedInventory.scenarios -eq 163 -and
        $round2.focusedInventory.historicalScenarios -eq 153 -and
        $round2.focusedInventory.addedRound2Scenarios -eq 10) `
        "Round-two focused inventory differs."
    Assert-U1 ($round2.workspace.families -eq 11 -and
        $round2.workspace.clusters -eq 18 -and $round2.workspace.actions -eq 110 -and
        $round2.workspace.normalMenus -eq 7) "Round-two workspace counts differ."
    Assert-U1 ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or
        [string]::IsNullOrEmpty((Get-U1Git @("tag", "--list", $PassTagName)).Trim())) `
        "A G9U1 PASS tag exists before author closeout."
    foreach ($path in @($round2.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text, '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot, [IO.Path]::GetFullPath(
                (Join-Path (Split-Path -Parent (Join-Path $RepositoryRoot $path)) $target))).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative `
                "round-two documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) `
                "Broken round-two review link: $path -> $target"
        }
    }
}

function Assert-U1Round3Contracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    Assert-U1 ((Get-U1Git @("rev-parse", "$Round2CandidateCommit^" )).Trim() -ceq
        $Round2AuthorInputCommit) "The round-two candidate parent changed."
    [void](Get-U1Git @("merge-base", "--is-ancestor", $Round2CandidateCommit, "HEAD"))
    foreach ($ref in @("refs/heads/$Round2CandidateBranch",
        "refs/remotes/origin/$Round2CandidateBranch")) {
        Assert-U1 ((Get-U1Git @("rev-parse", $ref)).Trim() -ceq $Round2CandidateCommit) `
            "The published round-two stabilization ref changed: $ref"
    }
    foreach ($pin in $Round2AuthorityBlobs.GetEnumerator()) {
        $tree = (Get-U1Git @("ls-tree", $Round2CandidateCommit, "--", $pin.Key)).Trim()
        Assert-U1 ($tree -ceq "100644 blob $($pin.Value)`t$($pin.Key)") `
            "Round-two stabilization authority changed: $($pin.Key)"
    }

    $round2Evidence = Read-U1Commit $Round2CandidateCommit $EvidencePath |
        ConvertFrom-Json -Depth 100
    $round2Scenarios = Read-U1Commit $Round2CandidateCommit $ScenarioPath |
        ConvertFrom-Json -Depth 100
    Assert-U1 (($Evidence.authorReviewStabilizationRound2 |
        ConvertTo-Json -Depth 100 -Compress) -ceq
        ($round2Evidence.authorReviewStabilizationRound2 |
        ConvertTo-Json -Depth 100 -Compress)) `
        "The published round-two review record was rewritten."
    Assert-U1 (@($round2Evidence.inventory.paths).Count -eq 131 -and
        $round2Evidence.inventory.pathCount -eq 131 -and
        $round2Evidence.inventory.sourcePathCount -eq 76 -and
        @($round2Scenarios.scenarios).Count -eq 163 -and
        @($round2Scenarios.focusedJUnit.classes).Count -eq 21 -and
        @($round2Scenarios.focusedJUnit.classes.methods).Count -eq 204) `
        "Published round-two inventory/test/scenario authority differs."
    foreach ($historical in @($round2Scenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $historical.id })
        Assert-U1 ($current.Count -eq 1) `
            "Round-two scenario was dropped/duplicated: $($historical.id)"
        Assert-U1 (($current[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historical | ConvertTo-Json -Depth 100 -Compress)) `
            "Published round-two scenario meaning changed: $($historical.id)"
    }
    foreach ($historicalClass in @($round2Scenarios.focusedJUnit.classes)) {
        $currentClass = @($Scenarios.focusedJUnit.classes | Where-Object {
            $_.name -ceq $historicalClass.name })
        Assert-U1 ($currentClass.Count -eq 1 -and
            $currentClass[0].source -ceq $historicalClass.source -and
            $currentClass[0].module -ceq $historicalClass.module) `
            "Round-two focused class changed: $($historicalClass.name)"
        foreach ($method in @($historicalClass.methods)) {
            $mapped = if ($Round3MethodReplacements.Contains($method)) {
                $Round3MethodReplacements[$method]
            } else { $method }
            if ($FinalPolishMethodReplacements.Contains($mapped)) {
                $mapped = $FinalPolishMethodReplacements[$mapped]
            }
            if ($FinalMicroMethodReplacements.Contains($mapped)) {
                $mapped = $FinalMicroMethodReplacements[$mapped]
            }
            Assert-U1 ($mapped -cin @($currentClass[0].methods)) `
                "Round-two focused obligation was dropped: $($historicalClass.name)#$method"
        }
    }

    [void](Get-U1Git @("merge-base", "--is-ancestor", $Round3CandidateCommit, "HEAD"))
    $round3Evidence = Read-U1Commit $Round3CandidateCommit $EvidencePath |
        ConvertFrom-Json -Depth 100
    $round3Scenarios = Read-U1Commit $Round3CandidateCommit $ScenarioPath |
        ConvertFrom-Json -Depth 100
    $round3 = $Evidence.authorReviewStabilizationRound3
    Assert-U1 (($round3 | ConvertTo-Json -Depth 100 -Compress) -ceq
        ($round3Evidence.authorReviewStabilizationRound3 |
            ConvertTo-Json -Depth 100 -Compress)) `
        "The published Round-three evidence record was rewritten."
    Assert-U1 (($Scenarios.authorReviewRound3 | ConvertTo-Json -Depth 100 -Compress) -ceq
        ($round3Scenarios.authorReviewRound3 | ConvertTo-Json -Depth 100 -Compress)) `
        "The published Round-three scenario provenance was rewritten."
    Assert-U1 ($round3.baseline.commit -ceq $Round2CandidateCommit -and
        $round3.baseline.branch -ceq $Round2CandidateBranch -and
        $round3.historicalRound2 -ceq "PUBLISHED_CANDIDATE_PRESERVED" -and
        $round3.dispositionRecord -ceq $Round3DispositionPath -and
        $round3.nextAuthorCloseout -ceq "EXACT_TECHNICAL_COMMIT_REQUIRED" -and
        $round3.manualAuthorResmoke -ceq "PENDING" -and
        $round3.noNewGeometricSemantics -eq $true) `
        "Missing or inconsistent round-three successor authority."
    Assert-U1Set @($round3.baseline.authorityBlobs.path) @($Round2AuthorityBlobs.Keys) `
        "Round-two authority pins in round three"
    foreach ($pin in @($round3.baseline.authorityBlobs)) {
        Assert-U1 ($pin.mode -ceq "100644" -and
            $Round2AuthorityBlobs[$pin.path] -ceq $pin.blobOid) `
            "Round-two authority pin differs: $($pin.path)"
    }

    $round3Delta = @((Get-U1Git @("diff", "--name-only", $Round2CandidateCommit,
        $Round3CandidateCommit)).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
    Assert-U1Set $round3Delta @($round3.inventory.deltaPaths) `
        "Exact author-review round-three successor delta"
    Assert-U1 ($round3.inventory.baseCommit -ceq $Round2CandidateCommit -and
        $round3.inventory.deltaPathCount -eq $round3Delta.Count -and
        $round3.inventory.sourcePathCount -eq
            @($round3Delta | Where-Object { $_ -cmatch '^source/' }).Count -and
        $Evidence.inventory.pathCount -eq $Paths.Count -and
        $Evidence.inventory.sourcePathCount -eq
            @($Paths | Where-Object { $_ -cmatch '^source/' }).Count) `
        "Round-three inventory counters drifted."
    $round3SharedMain = @($round3Delta | Where-Object {
        $_ -cmatch '^source/shared/.*/src/main/' })
    Assert-U1Set $round3SharedMain @($Round3SharedInstrumentationPaths +
        $Round3SharedLocalizationPaths) `
        "Only the authorized shared instrumentation/localization round-three paths are allowed"
    Assert-U1Set @($round3.allowedSharedInstrumentationCorrection |
        ForEach-Object { $_.path }) `
        $Round3SharedInstrumentationPaths "Round-three shared instrumentation allowlist"
    Assert-U1Set @($round3.allowedSharedLocalization.path) `
        $Round3SharedLocalizationPaths "Round-three shared localization allowlist"

    Assert-U1Set @($round3.branding.resources.path) @($Round3BrandingResourcePins.Keys) `
        "Round-three promoted/derived branding pins"
    Assert-U1 ($round3.branding.packagingAuthority -ceq
            "HASH_PINNED_TRACKED_DERIVATIVES_WITH_DIRECT_ICO_STRUCTURE_VALIDATION" -and
        $round3.branding.packagingGeneratorDependency -eq $false -and
        $round3.branding.generatorRole -ceq "SEPARATE_VERIFY_ONLY_PROVENANCE_CHECK") `
        "Round-three packaging branding authority differs."
    foreach ($pin in @($round3.branding.resources)) {
        $expected = $Round3BrandingResourcePins[$pin.path]
        Assert-U1 ($pin.bytes -eq $expected[0] -and $pin.rawSha256 -ceq $expected[1]) `
            "Branding evidence pin differs: $($pin.path)"
        $full = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $pin.path `
            "round-three branding resource"
        Assert-U1 ((Get-Item -LiteralPath $full).Length -eq $pin.bytes -and
            (Get-FileHash -LiteralPath $full -Algorithm SHA256).Hash.ToLowerInvariant() `
                -ceq $pin.rawSha256) "Branding resource differs: $($pin.path)"
    }
    Assert-U1Set @($round3.localAuthorInputs.path) @($Round3LocalAuthorInputPins.Keys) `
        "Round-three optional local author-input pins"
    foreach ($input in @($round3.localAuthorInputs)) {
        $expected = $Round3LocalAuthorInputPins[$input.path]
        Assert-U1 ($input.bytes -eq $expected[0] -and
            $input.rawSha256 -ceq $expected[1] -and
            $input.requiredForCleanCheckout -eq $false) `
            "Optional local author-input evidence pin differs: $($input.path)"
        Assert-U1 ($input.path -cnotin $Paths) `
            "Ignored author input entered the canonical tracked/untracked inventory: $($input.path)"
        Assert-U1 ((Get-U1Git @("check-ignore", "--no-index", "--", $input.path)).Trim() `
                -ceq $input.path) `
            "Round-three local author input is not isolated by repository ignore policy: $($input.path)"
    }

    $external = $round3.externalDiagnostics.revision3AlgebraEditing
    Assert-U1 ($external.state -ceq "PASS_SCOPED_NOT_ACCEPTANCE" -and
        $external.tests -eq 1 -and $external.failures -eq 0 -and
        $external.acceptanceAuthority -eq $false -and
        $external.canonicalSuiteDependency -eq $false -and
        $external.className -ceq $Round3ExternalDiagnostic.className -and
        $external.source -ceq $Round3ExternalDiagnostic.source -and
        $external.method -ceq $Round3ExternalDiagnostic.method -and
        $external.input -ceq $Round3ExternalDiagnostic.input -and
        $external.result -ceq $Round3ExternalDiagnostic.result -and
        $external.resultRawSha256 -ceq $Round3ExternalDiagnostic.resultRawSha256 -and
        $external.summaryRawSha256 -ceq $Round3ExternalDiagnostic.summaryRawSha256 -and
        $external.junitRawSha256 -ceq $Round3ExternalDiagnostic.junitRawSha256) `
        "Round-three external Revision3 diagnostic contract differs."
    Assert-U1 (@($Scenarios.externalDiagnosticMethods).Count -eq 1) `
        "Round three requires exactly one non-acceptance external diagnostic."
    $scenarioExternal = $Scenarios.externalDiagnosticMethods[0]
    Assert-U1 ($scenarioExternal.className -ceq $Round3ExternalDiagnostic.className -and
        $scenarioExternal.source -ceq $Round3ExternalDiagnostic.source -and
        $scenarioExternal.method -ceq $Round3ExternalDiagnostic.method -and
        $scenarioExternal.input -ceq $Round3ExternalDiagnostic.input -and
        $scenarioExternal.state -ceq "PASS_SCOPED_NOT_ACCEPTANCE" -and
        $scenarioExternal.acceptanceAuthority -eq $false -and
        $scenarioExternal.canonicalSuiteDependency -eq $false) `
        "Round-three external diagnostic scenario metadata differs."
    Assert-U1 (@($Scenarios.focusedJUnit.classes | Where-Object {
        $_.name -ceq $Round3ExternalDiagnostic.className }).Count -eq 0) `
        "The ignored Revision3 diagnostic entered the canonical focused suite."
    Assert-U1 ($Round3ExternalDiagnostic.className -cnotmatch 'Test$') `
        "The external author-input diagnostic must not enter default JUnit discovery."
    $externalSource = Read-U1 $Round3ExternalDiagnostic.source
    $externalMethods = @([regex]::Matches($externalSource,
        '(?s)@Test\s+(?:public\s+)?void\s+([A-Za-z0-9_]+)\s*\(') |
        ForEach-Object { $_.Groups[1].Value })
    Assert-U1Set $externalMethods @($Round3ExternalDiagnostic.method) `
        "Round-three external Revision3 diagnostic methods"
    $algebraGestureSource = Read-U1 `
        "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1AlgebraGestureEditingTest.java"
    Assert-U1 ($algebraGestureSource -cmatch
        '(?s)void deterministicFixtureCoversAllAlgebraEditRoutes\s*\([^)]*\)\s*throws Exception\s*\{\s*for \(EditRoute route : EditRoute\.values\(\)\)\s*\{\s*assertGestureLifecycle\(route, directory\);') `
        "The canonical Algebra route regression is not an unconditional deterministic fixture."

    $geogebraSource = Read-U1Commit $Round3CandidateCommit `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/GeoGebra.java"
    $geocedgSource = Read-U1Commit $Round3CandidateCommit `
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDG.java"
    $laboratorySource = Read-U1Commit $Round3CandidateCommit `
        "source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2Laboratory.java"
    Assert-U1 ($geogebraSource -cmatch
        '(?s)public static void doMain\(String\[\] cmdArgs, Supplier<GeoGebraFrame> frameFactory\)\s*\{\s*doMain\(cmdArgs, frameFactory,.*?GuiResourcesD\.SPLASH\.getFilename\(\)\),\s*false\);\s*\}') `
        "Classic two-argument Desktop startup no longer preserves direct initialization."
    Assert-U1 ($geogebraSource -cmatch
        '(?s)public static void doMain\(String\[\] cmdArgs, Supplier<GeoGebraFrame> frameFactory,\s*Supplier<URL> splashResource\)\s*\{\s*doMain\(cmdArgs, frameFactory, splashResource, true\);\s*\}') `
        "The product-specific three-argument startup does not request Swing initialization."
    Assert-U1 ($geocedgSource -cmatch
        '(?s)GeoGebra\.doMain\(effectiveArguments, GeoCeDGFrame::new,\s*GeoCeDG::getSplashResource\);') `
        "GeoCeDG main does not consume the product-specific three-argument startup seam."
    Assert-U1 ($laboratorySource -cmatch
        '(?s)GeoGebra\.doMain\(withoutUpstreamSplash\(laboratoryArguments\),\s*LocusV2LaboratoryFrame::new, \(\) -> null\);') `
        "The GeoCeDG Locus V2 laboratory does not consume the Swing-owned three-argument startup seam."

    Assert-U1 ($round3Scenarios.authorReviewRound3.baselineCommit -ceq
        $Round2CandidateCommit -and
        $round3Scenarios.authorReviewRound3.historicalScenarioCount -eq 163 -and
        $round3Scenarios.authorReviewRound3.historicalFocusedTests -eq 204 -and
        $round3Scenarios.authorReviewRound3.manualAuthorResmoke -ceq "PENDING" -and
        $round3Scenarios.authorReviewRound3.authorChecklistModified -eq $false) `
        "Round-three scenario provenance differs."
    Assert-U1Set @($round3Scenarios.authorReviewRound3.additionalScenarioIds) `
        $Round3ScenarioIds "Round-three scenario IDs"
    foreach ($entry in $Round3MethodReplacements.GetEnumerator()) {
        Assert-U1 ($round3Scenarios.authorReviewRound3.methodReplacements.$($entry.Key) -ceq
            $entry.Value) "Round-three test-method mapping differs: $($entry.Key)"
    }
    foreach ($historical in @($round3Scenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $historical.id })
        Assert-U1 ($current.Count -eq 1) `
            "Round-three scenario was dropped/duplicated: $($historical.id)"
        Assert-U1 (($current[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historical | ConvertTo-Json -Depth 100 -Compress)) `
            "Published Round-three scenario meaning changed: $($historical.id)"
    }
    foreach ($historicalClass in @($round3Scenarios.focusedJUnit.classes)) {
        $currentClass = @($Scenarios.focusedJUnit.classes | Where-Object {
            $_.name -ceq $historicalClass.name })
        Assert-U1 ($currentClass.Count -eq 1 -and
            $currentClass[0].source -ceq $historicalClass.source -and
            $currentClass[0].module -ceq $historicalClass.module) `
            "Round-three focused class changed: $($historicalClass.name)"
        foreach ($method in @($historicalClass.methods)) {
            $mapped = if ($FinalPolishMethodReplacements.Contains($method)) {
                $FinalPolishMethodReplacements[$method]
            } else { $method }
            if ($FinalMicroMethodReplacements.Contains($mapped)) {
                $mapped = $FinalMicroMethodReplacements[$mapped]
            }
            Assert-U1 ($mapped -cin @($currentClass[0].methods)) `
                "Round-three focused obligation was dropped: $($historicalClass.name)#$method"
        }
    }
    Assert-U1 ($round3.focusedInventory.classes -eq 23 -and
        $round3.focusedInventory.methods -eq 231 -and
        $round3.focusedInventory.desktopMethods -eq 217 -and
        $round3.focusedInventory.sharedMethods -eq 14 -and
        $round3.focusedInventory.scenarios -eq 177 -and
        $round3.focusedInventory.historicalScenarios -eq 163 -and
        $round3.focusedInventory.addedRound3Scenarios -eq 14) `
        "Round-three focused inventory differs."
    Assert-U1 ($round3.workspace.families -eq 11 -and
        $round3.workspace.clusters -eq 18 -and
        $round3.workspace.actions -eq 110 -and
        $round3.workspace.normalMenus -eq 7 -and
        $round3.workspace.presentationGroups -eq 28 -and
        $round3.workspace.toolbarGroups -eq 12) `
        "Round-three workspace counts differ."
    Assert-U1Set @($round3.supersededHistoricalScenarios) @("U1-R2-07", "U1-R2-08") `
        "Round-three superseded-but-preserved scenarios"
    Assert-U1 ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or
        [string]::IsNullOrEmpty((Get-U1Git @("tag", "--list", $PassTagName)).Trim())) `
        "A G9U1 PASS tag exists before author closeout."
    foreach ($path in @($round3.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text, '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot, [IO.Path]::GetFullPath(
                (Join-Path (Split-Path -Parent (Join-Path $RepositoryRoot $path)) $target))).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative `
                "round-three documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) `
                "Broken round-three review link: $path -> $target"
        }
    }
}

function Assert-U1FinalPresentationPolishContracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $FinalPolishCandidateCommit, "HEAD"))
    $final = $Evidence.finalPresentationPolish
    Assert-U1 ($final.baseline.commit -ceq $Round3CandidateCommit -and
        $final.baseline.branch -ceq $Round3CandidateBranch -and
        $final.historicalRound3 -ceq "PUBLISHED_TECHNICAL_CANDIDATE_PRESERVED" -and
        $final.dispositionRecord -ceq $FinalPolishDispositionPath -and
        $final.nextAuthorCloseout -ceq "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED" -and
        $final.manualAuthorResmoke -ceq "PENDING" -and
        $final.noNewGeometricSemantics -eq $true) `
        "Missing or inconsistent final-presentation-polish authority."

    $finalDelta = @((Get-U1Git @("diff", "--name-only", $Round3CandidateCommit,
        $FinalPolishCandidateCommit)).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
    Assert-U1Set $finalDelta @($final.inventory.deltaPaths) `
        "Exact final-presentation-polish delta"
    Assert-U1 ($final.inventory.baseCommit -ceq $Round3CandidateCommit -and
        $final.inventory.deltaPathCount -eq $finalDelta.Count -and
        $final.inventory.sourcePathCount -eq
            @($finalDelta | Where-Object { $_ -cmatch '^source/' }).Count -and
        $Evidence.inventory.pathCount -eq $Paths.Count -and
        $Evidence.inventory.sourcePathCount -eq
            @($Paths | Where-Object { $_ -cmatch '^source/' }).Count) `
        "Final-presentation-polish inventory counters drifted."
    $mainPaths = @($finalDelta | Where-Object { $_ -cmatch '^source/.*/src/main/' })
    $testPaths = @($finalDelta | Where-Object { $_ -cmatch '^source/.*/src/test/' })
    Assert-U1Set $mainPaths $FinalPolishMainPaths `
        "Final polish productive paths must remain bounded to Desktop presentation/resources"
    Assert-U1Set $testPaths $FinalPolishTestPaths `
        "Final polish test paths"
    Assert-U1 (@($finalDelta | Where-Object { $_ -cmatch '^source/shared/' }).Count -eq 0) `
        "Final presentation polish must not change shared kernel sources or tests."

    Assert-U1Set @($final.branding.resources.path) @($FinalPolishBrandingPins.Keys) `
        "Final polish branding pins"
    foreach ($pin in @($final.branding.resources)) {
        $expected = $FinalPolishBrandingPins[$pin.path]
        Assert-U1 ($pin.bytes -eq $expected[0] -and $pin.rawSha256 -ceq $expected[1]) `
            "Final polish branding evidence pin differs: $($pin.path)"
        $full = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $pin.path `
            "final polish branding resource"
        Assert-U1 ((Get-Item -LiteralPath $full).Length -eq $pin.bytes -and
            (Get-FileHash -LiteralPath $full -Algorithm SHA256).Hash.ToLowerInvariant() `
                -ceq $pin.rawSha256) "Final polish branding resource differs: $($pin.path)"
    }
    & (Join-Path $RepositoryRoot "tools/resources/generate-geocedg-branding.ps1") `
        -VerifyOnly
    Assert-U1 ($LASTEXITCODE -eq 0) `
        "The final GeoCeDG branding derivatives are not deterministically reproducible."

    $geogebraSource = Read-U1 `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/GeoGebra.java"
    $splashSource = Read-U1 `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/SplashWindow.java"
    Assert-U1 ($geogebraSource -cmatch
        '(?s)doMain\(cmdArgs, frameFactory,.*?GuiResourcesD\.SPLASH\.getFilename\(\)\),\s*false, false\);') `
        "Classic startup no longer retains its non-foreground policy."
    Assert-U1 ($geogebraSource -cmatch
        '(?s)doMain\(cmdArgs, frameFactory, splashResource, true, true\);') `
        "GeoCeDG startup does not request the product foreground policy."
    Assert-U1 ($splashSource -cmatch
        '(?s)if \(foreground\).*?setAlwaysOnTop\(true\).*?setVisible\(true\).*?toFront\(\).*?return;.*?toFront\(\).*?setVisible\(true\);') `
        "Splash foreground/Classic presentation ordering differs."

    Assert-U1 ($Scenarios.finalPresentationPolish.baselineCommit -ceq
        $Round3CandidateCommit -and
        $Scenarios.finalPresentationPolish.historicalScenarioCount -eq 177 -and
        $Scenarios.finalPresentationPolish.historicalFocusedTests -eq 231 -and
        $Scenarios.finalPresentationPolish.authorChecklistModified -eq $false -and
        $Scenarios.finalPresentationPolish.newStableActionIds -eq 0 -and
        $Scenarios.finalPresentationPolish.manualAuthorResmoke -ceq "PENDING" -and
        $Scenarios.finalPresentationPolish.nextAuthorCloseout -ceq
            "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED") `
        "Final-presentation-polish scenario provenance differs."
    Assert-U1Set @($Scenarios.finalPresentationPolish.additionalScenarioIds) `
        $FinalPolishScenarioIds "Final-presentation-polish scenario IDs"
    foreach ($entry in $FinalPolishMethodReplacements.GetEnumerator()) {
        Assert-U1 ($Scenarios.finalPresentationPolish.methodReplacements.$($entry.Key) -ceq
            $entry.Value) "Final-polish test-method mapping differs: $($entry.Key)"
    }
    Assert-U1 ($final.focusedInventory.classes -eq 24 -and
        $final.focusedInventory.methods -eq 238 -and
        $final.focusedInventory.desktopMethods -eq 224 -and
        $final.focusedInventory.sharedMethods -eq 14 -and
        $final.focusedInventory.scenarios -eq 185 -and
        $final.focusedInventory.historicalScenarios -eq 177 -and
        $final.focusedInventory.addedFinalPolishScenarios -eq 8) `
        "Final-presentation-polish focused inventory differs."
    Assert-U1 ($final.workspace.families -eq 11 -and
        $final.workspace.clusters -eq 18 -and
        $final.workspace.actions -eq 110 -and
        $final.workspace.normalMenus -eq 7 -and
        $final.workspace.presentationGroups -eq 28 -and
        $final.workspace.toolbarGroups -eq 11 -and
        $final.workspace.toolbarActions -eq 52 -and
        $final.workspace.nativeToolbarModes -eq 44 -and
        $final.workspace.profileFlyouts -eq 2) `
        "Final-presentation-polish workspace counts differ."
    foreach ($path in @($final.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text, '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot, [IO.Path]::GetFullPath(
                (Join-Path (Split-Path -Parent (Join-Path $RepositoryRoot $path)) $target))).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative `
                "final-polish documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) `
                "Broken final-polish documentation link: $path -> $target"
        }
    }
    Assert-U1 ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or
        [string]::IsNullOrEmpty((Get-U1Git @("tag", "--list", $PassTagName)).Trim())) `
        "A G9U1 PASS tag exists before author closeout."
}

function Assert-U1FinalMicroPresentationContracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $FinalPolishCandidateCommit,
        "HEAD"))
    $micro = $Evidence.finalMicroPresentation
    Assert-U1 ($micro.baseline.commit -ceq $FinalPolishCandidateCommit -and
        $micro.baseline.branch -ceq $Round3CandidateBranch -and
        $micro.historicalFinalPolish -ceq
            "FUNCTIONALLY_ACCEPTED_TECHNICAL_CHECKPOINT_PRESERVED" -and
        $micro.dispositionRecord -ceq $FinalMicroDispositionPath -and
        $micro.nextAuthorCloseout -ceq "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED" -and
        $micro.manualAuthorResmoke -ceq "PENDING" -and
        $micro.noNewGeometricSemantics -eq $true) `
        "Missing or inconsistent final-micro-presentation authority."

    $historicalEvidence = Read-U1Commit $FinalPolishCandidateCommit $EvidencePath |
        ConvertFrom-Json -Depth 100
    Assert-U1 (($micro.baseline.pathCount -eq $historicalEvidence.inventory.pathCount) -and
        ($micro.baseline.sourcePathCount -eq $historicalEvidence.inventory.sourcePathCount)) `
        "Final-micro baseline inventory differs from the immutable checkpoint."
    Assert-U1 (($Evidence.finalPresentationPolish | ConvertTo-Json -Depth 100 -Compress) -ceq
        ($historicalEvidence.finalPresentationPolish | ConvertTo-Json -Depth 100 -Compress)) `
        "The immutable final-presentation-polish authority was rewritten."

    $microDelta = @(Get-U1FinalMicroPresentationDeltaPaths)
    Assert-U1Set $microDelta @($micro.inventory.deltaPaths) `
        "Exact final-micro-presentation delta"
    Assert-U1 ($micro.inventory.baseCommit -ceq $FinalPolishCandidateCommit -and
        $micro.inventory.deltaPathCount -eq $microDelta.Count -and
        $micro.inventory.sourcePathCount -eq
            @($microDelta | Where-Object { $_ -cmatch '^source/' }).Count -and
        $Evidence.inventory.pathCount -eq $Paths.Count -and
        $Evidence.inventory.sourcePathCount -eq
            @($Paths | Where-Object { $_ -cmatch '^source/' }).Count) `
        "Final-micro-presentation inventory counters drifted."
    $mainPaths = @($microDelta | Where-Object { $_ -cmatch '^source/.*/src/main/' })
    $testPaths = @($microDelta | Where-Object { $_ -cmatch '^source/.*/src/test/' })
    Assert-U1Set $mainPaths $FinalMicroMainPaths `
        "Final micro productive paths must remain bounded to Desktop presentation"
    Assert-U1Set $testPaths $FinalMicroTestPaths "Final micro focused test paths"
    Assert-U1 (@($microDelta | Where-Object { $_ -cmatch '^source/shared/' }).Count -eq 0) `
        "Final micro presentation must not change shared kernel sources or tests."

    $historicalScenarios = Read-U1Commit $FinalPolishCandidateCommit $ScenarioPath |
        ConvertFrom-Json -Depth 100
    Assert-U1 (@($historicalScenarios.scenarios).Count -eq 185) `
        "The immutable final-polish scenario count differs."
    foreach ($historical in @($historicalScenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $historical.id })
        Assert-U1 ($current.Count -eq 1) `
            "Final-polish scenario was dropped/duplicated: $($historical.id)"
        Assert-U1 (($current[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historical | ConvertTo-Json -Depth 100 -Compress)) `
            "Immutable final-polish scenario meaning changed: $($historical.id)"
    }
    foreach ($historicalClass in @($historicalScenarios.focusedJUnit.classes)) {
        $currentClass = @($Scenarios.focusedJUnit.classes | Where-Object {
            $_.name -ceq $historicalClass.name })
        Assert-U1 ($currentClass.Count -eq 1 -and
            $currentClass[0].source -ceq $historicalClass.source -and
            $currentClass[0].module -ceq $historicalClass.module) `
            "Final-polish focused class changed: $($historicalClass.name)"
        foreach ($method in @($historicalClass.methods)) {
            $mapped = if ($FinalMicroMethodReplacements.Contains($method)) {
                $FinalMicroMethodReplacements[$method]
            } else { $method }
            Assert-U1 ($mapped -cin @($currentClass[0].methods)) `
                "Final-polish focused obligation was dropped: $($historicalClass.name)#$method"
        }
    }

    Assert-U1 ($Scenarios.finalMicroPresentation.baselineCommit -ceq
        $FinalPolishCandidateCommit -and
        $Scenarios.finalMicroPresentation.historicalScenarioCount -eq 185 -and
        $Scenarios.finalMicroPresentation.historicalFocusedTests -eq 238 -and
        $Scenarios.finalMicroPresentation.authorChecklistModified -eq $false -and
        $Scenarios.finalMicroPresentation.newStableActionIds -eq 0 -and
        $Scenarios.finalMicroPresentation.manualAuthorResmoke -ceq "PENDING" -and
        $Scenarios.finalMicroPresentation.nextAuthorCloseout -ceq
            "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED") `
        "Final-micro-presentation scenario provenance differs."
    Assert-U1Set @($Scenarios.finalMicroPresentation.additionalScenarioIds) `
        $FinalMicroScenarioIds "Final-micro-presentation scenario IDs"
    Assert-U1Set @($Scenarios.finalMicroPresentation.supersededHistoricalScenarios) `
        @("U1-FP-05", "U1-FP-07", "U1-FP-08") `
        "Final-micro superseded historical scenarios"
    foreach ($entry in $FinalMicroMethodReplacements.GetEnumerator()) {
        Assert-U1 ($Scenarios.finalMicroPresentation.methodReplacements.$($entry.Key) -ceq
            $entry.Value) "Final-micro test-method mapping differs: $($entry.Key)"
    }
    Assert-U1 ($micro.focusedInventory.classes -eq 24 -and
        $micro.focusedInventory.methods -eq 238 -and
        $micro.focusedInventory.desktopMethods -eq 224 -and
        $micro.focusedInventory.sharedMethods -eq 14 -and
        $micro.focusedInventory.scenarios -eq 192 -and
        $micro.focusedInventory.historicalScenarios -eq 185 -and
        $micro.focusedInventory.addedFinalMicroScenarios -eq 7) `
        "Final-micro-presentation focused inventory differs."
    Assert-U1 ($micro.workspace.families -eq 11 -and
        $micro.workspace.clusters -eq 18 -and
        $micro.workspace.actions -eq 110 -and
        $micro.workspace.normalMenus -eq 7 -and
        $micro.workspace.presentationGroups -eq 28 -and
        $micro.workspace.toolbarGroups -eq 11 -and
        $micro.workspace.toolbarActions -eq 52 -and
        $micro.workspace.nativeToolbarModes -eq 44 -and
        $micro.workspace.profileFlyouts -eq 2 -and
        (@($micro.workspace.toolbarGroupIds) -join ',') -ceq
            'construction-move,construction-relations,construction-lines,' +
            'construction-polygons,construction-derived,construction-curves,' +
            'construction-semantic-curves,construction-metrics,' +
            'construction-transforms,construction-parameters,view-navigation') `
        "Final-micro-presentation workspace authority differs."
    Assert-U1 ($micro.preservedContracts.singleActionAuthority -eq $true -and
        $micro.preservedContracts.stableActionIds -eq 110 -and
        $micro.preservedContracts.newStableActionIds -eq 0 -and
        $micro.preservedContracts.continuityLockedOff -eq $true -and
        $micro.preservedContracts.classicIsolationUnchanged -eq $true -and
        $micro.preservedContracts.inputHelpRemainsFarRight -eq $true -and
        $micro.preservedContracts.kernelScientificPersistenceChanged -eq $false -and
        $micro.preservedContracts.authorApprovedImplementation -eq $false -and
        $micro.preservedContracts.passClaimedImplementation -eq $false) `
        "Final-micro-presentation preserved contracts differ."
    foreach ($path in @($micro.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text,
            '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $document = Join-Path $RepositoryRoot $path
            $targetPath = Join-Path (Split-Path -Parent $document) $target
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot,
                [IO.Path]::GetFullPath($targetPath)).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative `
                "final-micro documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) `
                "Broken final-micro documentation link: $path -> $target"
        }
    }
    Assert-U1 ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or
        [string]::IsNullOrEmpty((Get-U1Git @("tag", "--list", $PassTagName)).Trim())) `
        "A G9U1 PASS tag exists before author closeout."
}

function Assert-U1FinalToolbarVisualNormalizationContracts {
    param([object]$Evidence, [object]$Scenarios)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $FinalMicroCandidateCommit,
        "HEAD"))
    Assert-U1 ((Get-U1Git @("rev-parse", "$FinalMicroCandidateCommit^{tree}")).Trim() `
        -ceq $FinalMicroCandidateTree) "The accepted final-micro checkpoint tree changed."

    $visual = $Evidence.finalToolbarVisualNormalization
    Assert-U1 ($visual.baseline.commit -ceq $FinalMicroCandidateCommit -and
        $visual.baseline.tree -ceq $FinalMicroCandidateTree -and
        $visual.baseline.branch -ceq $Round3CandidateBranch -and
        $visual.historicalFinalMicro -ceq
            "FUNCTIONALLY_ACCEPTED_TECHNICAL_CHECKPOINT_PRESERVED" -and
        $visual.dispositionRecord -ceq $FinalMicroDispositionPath -and
        $visual.nextAuthorCloseout -ceq "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED" -and
        $visual.manualAuthorResmoke -ceq "PENDING" -and
        $visual.scope -ceq "DESKTOP_SWING_PRESENTATION_ONLY") `
        "Missing or inconsistent final toolbar visual-normalization authority."

    $historicalEvidence = Read-U1Commit $FinalMicroCandidateCommit $EvidencePath |
        ConvertFrom-Json -Depth 100
    $historicalScenarios = Read-U1Commit $FinalMicroCandidateCommit $ScenarioPath |
        ConvertFrom-Json -Depth 100
    Assert-U1 ($visual.baseline.pathCount -eq $historicalEvidence.inventory.pathCount -and
        $visual.baseline.sourcePathCount -eq
            $historicalEvidence.inventory.sourcePathCount) `
        "Final toolbar visual baseline inventory differs from the immutable checkpoint."
    Assert-U1 (($Evidence.finalMicroPresentation | ConvertTo-Json -Depth 100 -Compress) `
        -ceq ($historicalEvidence.finalMicroPresentation |
            ConvertTo-Json -Depth 100 -Compress)) `
        "The immutable final-micro evidence authority was rewritten."
    Assert-U1 (($Scenarios.finalMicroPresentation | ConvertTo-Json -Depth 100 -Compress) `
        -ceq ($historicalScenarios.finalMicroPresentation |
            ConvertTo-Json -Depth 100 -Compress)) `
        "The immutable final-micro scenario authority was rewritten."

    $visualDelta = @(Get-U1FinalToolbarDeltaPaths)
    Assert-U1Set $visualDelta @($visual.inventory.deltaPaths) `
        "Exact final toolbar visual-normalization delta"
    Assert-U1 ($visual.inventory.baseCommit -ceq $FinalMicroCandidateCommit -and
        $visual.inventory.deltaPathCount -eq $visualDelta.Count -and
        $visual.inventory.sourcePathCount -eq
            @($visualDelta | Where-Object { $_ -cmatch '^source/' }).Count) `
        "Final toolbar visual-normalization inventory counters drifted."
    Assert-U1Set @($visualDelta | Where-Object { $_ -cmatch '^source/.*/src/main/' }) `
        $FinalToolbarVisualMainPaths `
        "Final toolbar visual productive paths must remain bounded to Desktop Swing"
    Assert-U1Set @($visualDelta | Where-Object { $_ -cmatch '^source/.*/src/test/' }) `
        $FinalToolbarVisualTestPaths "Final toolbar visual focused test paths"
    Assert-U1 (@($visualDelta | Where-Object {
        $_ -cmatch '^source/shared/|^apps/geocedg/application-profile\.yml$'
    }).Count -eq 0) `
        "Final toolbar visual normalization changed shared or profile authority."

    Assert-U1 (@($historicalScenarios.scenarios).Count -eq 192 -and
        @($Scenarios.scenarios).Count -eq 192) `
        "Final toolbar visual normalization must not alter the scenario inventory."
    foreach ($historical in @($historicalScenarios.scenarios)) {
        $current = @($Scenarios.scenarios | Where-Object { $_.id -ceq $historical.id })
        Assert-U1 ($current.Count -eq 1 -and
            ($current[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historical | ConvertTo-Json -Depth 100 -Compress)) `
            "Final toolbar visual normalization rewrote scenario: $($historical.id)"
    }
    foreach ($historicalClass in @($historicalScenarios.focusedJUnit.classes)) {
        $currentClass = @($Scenarios.focusedJUnit.classes | Where-Object {
            $_.name -ceq $historicalClass.name })
        Assert-U1 ($currentClass.Count -eq 1 -and
            ($currentClass[0] | ConvertTo-Json -Depth 100 -Compress) -ceq
            ($historicalClass | ConvertTo-Json -Depth 100 -Compress)) `
            "Final toolbar visual normalization changed focused inventory: $($historicalClass.name)"
    }
    $scenarioRecord = $Scenarios.finalToolbarVisualNormalization
    Assert-U1 ($scenarioRecord.baselineCommit -ceq $FinalMicroCandidateCommit -and
        $scenarioRecord.historicalScenarioCount -eq 192 -and
        $scenarioRecord.historicalFocusedTests -eq 238 -and
        @($scenarioRecord.newScenarioIds).Count -eq 0 -and
        $scenarioRecord.strengthenedContract -ceq
            "EXACT_LIVE_NATIVE_TOOL_GEOMETRY_ALIGNMENT_AND_CONTAINER_AT_HIDPI" -and
        $scenarioRecord.authorChecklistModified -eq $false -and
        $scenarioRecord.newStableActionIds -eq 0 -and
        $scenarioRecord.manualAuthorResmoke -ceq "PENDING" -and
        $scenarioRecord.nextAuthorCloseout -ceq
            "EXACT_NEW_TECHNICAL_COMMIT_REQUIRED") `
        "Final toolbar visual scenario provenance differs."
    Assert-U1Set @($scenarioRecord.reusedScenarioIds) $FinalToolbarVisualScenarioIds `
        "Final toolbar visual strengthened scenarios"
    Assert-U1Set @($scenarioRecord.strengthenedTestMethods) $FinalToolbarVisualMethods `
        "Final toolbar visual strengthened test methods"

    Assert-U1 ($visual.focusedInventory.classes -eq 24 -and
        $visual.focusedInventory.methods -eq 238 -and
        $visual.focusedInventory.desktopMethods -eq 224 -and
        $visual.focusedInventory.sharedMethods -eq 14 -and
        $visual.focusedInventory.scenarios -eq 192 -and
        $visual.focusedInventory.historicalScenarios -eq 192 -and
        $visual.focusedInventory.addedScenarios -eq 0) `
        "Final toolbar visual focused inventory differs."
    Assert-U1Set @($visual.focusedInventory.strengthenedScenarios) `
        $FinalToolbarVisualScenarioIds "Final toolbar visual evidence scenarios"
    Assert-U1Set @($visual.nativePresentationAuthority.copiedProperties) @(
        "preferredSize", "minimumSize", "maximumSize", "margin", "border",
        "alignmentX", "alignmentY", "horizontalAlignment", "verticalAlignment",
        "horizontalTextPosition", "verticalTextPosition", "iconTextGap",
        "componentOrientation") "Native toolbar presentation properties"
    Assert-U1 ($visual.nativePresentationAuthority.component -ceq
            "ModeToggleMenuD/ToolToggleButton" -and
        $visual.nativePresentationAuthority.liveReference -ceq
            "FIRST_NATIVE_BUTTON_IN_PROFILE_TOOLBAR" -and
        $visual.nativePresentationAuthority.mixedFlyoutContainer -ceq
            "JPANEL_BOXLAYOUT_X_AXIS_MATCHING_MODE_TOGGLE_MENU_D" -and
        $visual.nativePresentationAuthority.screenshotGolden -eq $false -and
        $visual.nativePresentationAuthority.arbitraryButtonSizeConstant -eq $false) `
        "Final toolbar native presentation contract differs."
    Assert-U1 ($visual.preservedContracts.checkpointImmutable -eq $true -and
        $visual.preservedContracts.singleActionAuthority -eq $true -and
        $visual.preservedContracts.profileByteUnchanged -eq $true -and
        $visual.preservedContracts.stableActionIds -eq 110 -and
        $visual.preservedContracts.newStableActionIds -eq 0 -and
        $visual.preservedContracts.toolbarGroups -eq 11 -and
        $visual.preservedContracts.toolbarActions -eq 52 -and
        $visual.preservedContracts.kernelScientificPersistenceChanged -eq $false -and
        $visual.preservedContracts.authorApprovedImplementation -eq $false -and
        $visual.preservedContracts.passClaimedImplementation -eq $false) `
        "Final toolbar visual preserved contracts differ."

    $toolbarSource = Read-U1 $FinalToolbarVisualMainPaths[0]
    $userToolsSource = Read-U1 $FinalToolbarVisualMainPaths[1]
    $workspaceSource = Read-U1 $FinalToolbarVisualMainPaths[2]
    Assert-U1 (($toolbarSource + $userToolsSource + $workspaceSource) -cnotmatch
        'getScaledIconSize\(\)\s*\+\s*12') `
        "Final toolbar adapters retain the rejected local button-size approximation."
    Assert-U1 ($toolbarSource -cmatch 'applyNativeToolPresentation' -and
        $toolbarSource -cmatch 'new BoxLayout\(panel, BoxLayout\.X_AXIS\)' -and
        $workspaceSource -cmatch 'nativeVisualReference' -and
        $userToolsSource -cmatch 'applyNativeToolPresentation') `
        "Final toolbar adapters do not consume the native Swing presentation seam."
    foreach ($path in @($visual.documentationPaths)) {
        $text = Read-U1 $path
        foreach ($link in [regex]::Matches($text,
            '(?<!!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $link.Groups["target"].Value.Trim().Trim([char[]]"<>")
            if ($target -match '^(https?://|mailto:|#)') { continue }
            $target = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($target)) { continue }
            $document = Join-Path $RepositoryRoot $path
            $targetPath = Join-Path (Split-Path -Parent $document) $target
            $relative = [IO.Path]::GetRelativePath($RepositoryRoot,
                [IO.Path]::GetFullPath($targetPath)).Replace('\', '/')
            $resolved = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relative `
                "final toolbar visual documentation link"
            Assert-U1 (Test-Path -LiteralPath $resolved) `
                "Broken final toolbar visual documentation link: $path -> $target"
        }
    }
    Assert-U1 ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or
        [string]::IsNullOrEmpty((Get-U1Git @("tag", "--list", $PassTagName)).Trim())) `
        "A G9U1 PASS tag exists before author closeout."
}

function Invoke-U1Gradle {
    param([string[]]$Arguments, [string]$Description, [string]$LogName)
    $effective = @($Arguments) + @("--rerun-tasks", "--no-build-cache", "--no-daemon",
        "--no-problems-report", "--console=plain")
    if (-not $AllowToolchainDownload) { $effective += "-Dorg.gradle.java.installations.auto-download=false" }
    $log = Join-Path $LogDirectory $LogName
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath -RepositoryRoot $RepositoryRoot `
            -WorkingDirectory $RepositoryRoot -Arguments $effective -LogPath $log `
            -Description $Description -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $effective = @(ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $effective -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Push-Location $RepositoryRoot
    try { & (Join-Path $RepositoryRoot "gradlew.bat") @effective 2>&1 | Tee-Object -FilePath $log; $code = $LASTEXITCODE }
    finally { Pop-Location }
    Assert-U1 ($code -eq 0) "$Description failed ($code); see $log"
}
function Get-U1ClassResult {
    param([object]$Class)
    $moduleRoot = if ($Class.module -ceq "desktop") { "source/desktop/desktop" } else { "source/shared/common-jre" }
    [xml]$xml = Read-U1 "$moduleRoot/build/test-results/test/TEST-$($Class.name).xml"
    $suite = $xml.testsuite
    # Jupiter may append injected @TempDir argument types to the method name.
    # The exact source-declared method set and one executed case per method
    # remain required; parameterized/custom display-name suites need a contract.
    $methods = @($suite.testcase | ForEach-Object { [string]$_.name -creplace '\([^)]*\)$', '' })
    Assert-U1Set $methods @($Class.methods) "G9U1 live JUnit methods: $($Class.name)"
    Assert-U1 ([int]$suite.tests -eq @($Class.methods).Count -and [int]$suite.errors -eq 0 -and
        [int]$suite.failures -eq 0 -and [int]$suite.skipped -eq 0) "G9U1 failed/skipped/count mismatch: $($Class.name)"
    return [ordered]@{ class = $Class.name; tests = [int]$suite.tests; failures = 0; errors = 0; skipped = 0;
        methods = @($methods | Sort-Object -CaseSensitive) }
}
function Assert-U1Contracts {
    param([object]$Evidence, [object]$Scenarios)
    Assert-U1LifecycleEntry
    Assert-U1 ($Evidence.baseCommit -ceq $BaseCommit) "G9U1 explicit base differs."
    [void](Get-U1Git @("merge-base", "--is-ancestor", $BaseCommit, "HEAD"))
    foreach ($pin in @(
        @("geocedg-g9s1-pass", "de33f3a80102adb051aaa7547a72b7e97409c58c", "ece0ca6f00299d3347e57fac38b7a28cade28644"),
        @("geocedg-g9u0-r6-pass", "3942af594e4507e479f2c75019cef62e3d9fea6f", "2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e"),
        @("geocedg-g9s1-r1-pass", "af459d856f1cdc384805f3035203acce8e6f6104", "2b8caf1d0628be568c9afb062628d79d8f6a1342"))) {
        Assert-U1 ((Get-U1Git @("rev-parse", "refs/tags/$($pin[0])")).Trim() -ceq $pin[2]) "Changed annotated tag object $($pin[0])"
        Assert-U1 ((Get-U1Git @("cat-file", "-t", "refs/tags/$($pin[0])")).Trim() -ceq "tag") "Missing annotated tag $($pin[0])"
        Assert-U1 ((Get-U1Git @("rev-parse", "refs/tags/$($pin[0])^{}")).Trim() -ceq $pin[1]) "Changed phase tag $($pin[0])"
        [void](Get-U1Git @("merge-base", "--is-ancestor", $pin[1], $BaseCommit))
    }
    foreach ($protectedRef in @("feature/g9u1-construction-workspace-planning-after-r6",
        "origin/feature/g9u1-construction-workspace-planning-after-r6")) {
        Assert-U1 ((Get-U1Git @("rev-parse", $protectedRef)).Trim() -ceq
            "00982e7e148a634cd57ed928f322774df267d5e3") "Protected approved design changed."
    }
    $authorCloseout = $SelectedLifecycleMode -cin @("AUTHOR_CLOSEOUT", "PUBLISHED_REGRESSION")
    if ($authorCloseout) {
        Assert-U1 ($Evidence.status -ceq "PASS_AUTHOR_APPROVED" -and
            $Evidence.implementationAuthorized -eq $true -and
            $Evidence.implementationStarted -eq $true -and
            $Evidence.implementationComplete -eq $true -and
            $Evidence.authorApprovedDesign -eq $true -and
            $Evidence.manualAuthorSmoke -ceq "PASS" -and
            $Evidence.selfApproved -eq $false -and
            $Evidence.authorApprovedImplementation -eq $true -and
            $Evidence.passClaimedImplementation -eq $true) `
            "G9U1 AUTHOR_CLOSEOUT approval/status flags are inconsistent."
    } else {
        Assert-U1 ($Evidence.status -ceq "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $Evidence.implementationAuthorized -eq $true -and
            $Evidence.implementationStarted -eq $true -and
            $Evidence.implementationComplete -eq $true -and
            $Evidence.authorApprovedDesign -eq $true -and
            $Evidence.manualAuthorSmoke -ceq "PENDING" -and
            $Evidence.selfApproved -eq $false -and
            $Evidence.authorApprovedImplementation -eq $false -and
            $Evidence.passClaimedImplementation -eq $false) `
            "G9U1 candidate authorization/approval flags are inconsistent."
    }
    Assert-U1 ($Evidence.canonicalPromptLfSha256 -ceq (Get-U1Hash $PromptPath)) "G9U1 canonical execution prompt changed."
    $paths = @(Get-U1CandidatePaths)
    Assert-U1Set $paths @($Evidence.inventory.paths) "G9U1 exact source candidate inventory"
    $materialization = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    [void](Assert-GeoCeDGMaterializationAttributes -RepositoryRoot $RepositoryRoot `
        -Paths $paths -ConfiguredFilterDrivers $materialization.configuredFilterDrivers)
    Assert-U1ReviewContracts $Evidence $Scenarios $paths
    Assert-U1Round2Contracts $Evidence $Scenarios $paths
    Assert-U1Round3Contracts $Evidence $Scenarios $paths
    Assert-U1FinalPresentationPolishContracts $Evidence $Scenarios $paths
    Assert-U1FinalMicroPresentationContracts $Evidence $Scenarios $paths
    Assert-U1FinalToolbarVisualNormalizationContracts $Evidence $Scenarios
    Assert-U1 (@($paths | Where-Object { $_ -match '^artifacts/|^book/' }).Count -eq 0) "Generated/independent-book path in candidate."
    $profile = Read-U1 "apps/geocedg/application-profile.yml" | ConvertFrom-Json -Depth 100 -AsHashtable
    [void](Assert-GeoCeDGLiveWorkspaceProfile -RepositoryRoot $RepositoryRoot)
    Assert-U1 ($profile.schema_version -eq 2 -and $profile.profile_id -ceq "geocedg-desktop") "G9U1 requires live schema v2."
    Assert-U1 (@($profile.actions).Count -eq 110 -and @($profile.clusters).Count -eq 18 -and
        @($profile.taxonomy.broad_families).Count -eq 11) "G9U1 11/18/110 action authority differs."
    Assert-U1 (@($profile.presentation_groups).Count -eq 28 -and
        @($profile.toolbar_group_ids).Count -eq 11) `
        "G9U1 final presentation/toolbar groups differ."
    Assert-U1 ($profile.product_policies.continuity.value -eq $false -and
        $profile.product_policies.continuity.locked -eq $true) "GeoCeDG Continuity OFF is not locked."
    Assert-U1Set @($profile.product_policies.languages.offered) @("en", "es") "G9U1 product languages"
    Assert-U1 ($profile.product_policies.languages.fallback -ceq "en") "G9U1 English fallback absent."
    Assert-U1Set @($profile.menu_sections.id) @("file", "edit", "view", "construction",
        "options", "automation", "help") "G9U1 round-two menu IDs"
    Assert-U1 ((@($profile.menu_sections.id) -join ',') -ceq
        "file,edit,view,construction,options,automation,help") `
        "G9U1 round-two menu order differs."
    $ids = @($Scenarios.scenarios | ForEach-Object { $_.id })
    $approved = Read-U1 "geocedg/validation/g9u1/g9u1-preexecution-scenarios.json" | ConvertFrom-Json -Depth 100
    $approvedIds = @($approved.groups | ForEach-Object { $_.scenarioIds })
    Assert-U1 ($approvedIds.Count -eq 138) "The historical approved scenario baseline changed."
    Assert-U1Set $ids @($approvedIds + $ReviewScenarioIds + $Round2ScenarioIds +
        $Round3ScenarioIds + $FinalPolishScenarioIds + $FinalMicroScenarioIds) `
        "All historical and bounded author-review scenarios"
    Assert-U1 ($ids.Count -eq 192) `
        "G9U1 requires 138 historical plus 15 round-one, 10 round-two, 14 round-three, 8 final-polish and 7 final-micro scenarios."
    $methodKeys = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($class in $Scenarios.focusedJUnit.classes) {
        Assert-U1 ($class.module -cin @("shared", "desktop") -and @($class.methods).Count -gt 0) "Empty/invalid G9U1 test class."
        $source = Read-U1 $class.source
        Assert-U1 ($source -cnotmatch '@ParameterizedTest|@DisplayName|@RepeatedTest') "Unsupported method-display/count contract: $($class.name)"
        $declared = @([regex]::Matches($source, '(?s)@Test\s+(?:public\s+)?void\s+([A-Za-z0-9_]+)\s*\(') |
            ForEach-Object { $_.Groups[1].Value })
        Assert-U1Set $declared @($class.methods) "G9U1 source test methods: $($class.name)"
        foreach ($method in $class.methods) { [void]$methodKeys.Add("$($class.name)#$method") }
    }
    foreach ($scenario in $Scenarios.scenarios) {
        # All normative scenarios remain visible. Runtime support is not a
        # claim that an author visual review or an unproved risk experiment ran.
        $exception = switch -CaseSensitive ($scenario.id) {
            "U1-V02" { "AUTHOR_REVIEW_PENDING" }
            { $_ -cin @("U1-A01", "U1-A02", "U1-A03") } { "CONDITIONAL_NOT_APPLICABLE" }
            { $_ -cin @("U1-Q04", "U1-Q05") } { "RETAINED_RISK" }
            default { "" }
        }
        if ([string]::IsNullOrEmpty($exception)) {
            Assert-U1 ($scenario.coverage -ceq "PARTIAL_AUTOMATED_MANUAL_PENDING" -and
                @($scenario.tests).Count -gt 0) "Missing executable scenario support: $($scenario.id)"
        } else {
            Assert-U1 ($scenario.coverage -ceq $exception) "Unapproved scenario disposition: $($scenario.id)"
        }
        foreach ($test in $scenario.tests) {
            $resolvedTest = Resolve-U1TestReference $test
            Assert-U1 ($methodKeys.Contains($resolvedTest)) `
                "Unknown test mapping $test -> $resolvedTest ($($scenario.id))."
        }
        foreach ($field in @("assertion", "procedure", "automatedScope", "remaining")) {
            Assert-U1 (-not [string]::IsNullOrWhiteSpace($scenario.$field)) "Missing $field evidence meaning: $($scenario.id)"
        }
    }
    $hashed = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($line in (Read-U1 $HashPath).Replace("`r`n", "`n").Split("`n")) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#")) { continue }
        $parts = $line -split '  ', 2
        Assert-U1 ($parts.Count -eq 2 -and $parts[0] -cmatch '^[0-9a-f]{64}$') "Invalid G9U1 hash record."
        Assert-U1 ($hashed.Add($parts[1]) -and $parts[1] -cne $HashPath) "Duplicate/self G9U1 hash record."
        Assert-U1 ((Get-U1ManifestAuthorityHash $parts[1]) -ceq $parts[0]) `
            "G9U1 authority hash mismatch: $($parts[1])"
    }
    $expectedHashed = @($paths | Where-Object {
        $_ -cne $HashPath -and $_ -cnotin $CanonicalBinaryInventoryPaths
    })
    Assert-U1Set @($hashed) $expectedHashed `
        "G9U1 canonical-LF manifest coverage (binary/raw pins and self excluded)"
    foreach ($path in @($PromptPath, $EvidencePath, $ScenarioPath, $ReportPath,
        "tools/agent/verify-g9u1-construction-workspace.ps1")) {
        Assert-U1 ($hashed.Contains($path)) "Missing required G9U1 hash: $path"
    }
    Assert-U1 ((Get-GeoCeDGPhaseDefinition G9U1).Verifier -ceq "verify-g9u1-construction-workspace.ps1") "G9U1 PHASE not registered."
}

$initialStatus = $null
$initialHead = $null
$initialIndex = $null
$rawStart = $null
$generated = $null
$failure = $null
try {
    Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $RepositoryRoot -LogDirectory $LogDirectory
    [void][IO.Directory]::CreateDirectory($LogDirectory)
    $initialStatus = Get-RepositoryStatusText $RepositoryRoot
    $initialHead = (Get-U1Git @("rev-parse", "HEAD")).Trim()
    $initialIndex = Get-U1Git @("ls-files", "--stage", "-z")
    $rawStart = Get-GeoCeDGPhaseRawInputSnapshot $RepositoryRoot
    Initialize-U1Lifecycle
    $evidence = Read-U1 $EvidencePath | ConvertFrom-Json -Depth 100
    $scenarios = Read-U1 $ScenarioPath | ConvertFrom-Json -Depth 100
    Assert-U1Contracts $evidence $scenarios
    if ($AuthorCloseoutOnly) {
        Assert-U1 ($SelectedLifecycleMode -ceq "AUTHOR_CLOSEOUT" -and
            -not $SkipBuild -and [string]::IsNullOrWhiteSpace($BuildEvidencePath) -and
            -not $IncrementalBuild) `
            "AUTHOR_CLOSEOUT is documentary consistency, not build evidence."
        & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol `
            -C $RepositoryRoot diff --check
        Assert-U1 ($LASTEXITCODE -eq 0) "G9U1 AUTHOR_CLOSEOUT git diff --check failed."
        & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol `
            -C $RepositoryRoot diff --cached --check
        Assert-U1 ($LASTEXITCODE -eq 0) `
            "G9U1 AUTHOR_CLOSEOUT git diff --cached --check failed."
        $closeout = [ordered]@{
            schemaVersion = 1
            phase = "G9U1"
            state = "AUTHOR_CLOSEOUT_CONSISTENCY_LINKED_NOT_NEW_EXECUTION"
            lifecycle = $LifecycleContext
            productRuntimeExecuted = $false
            currentRunReceiptProduced = $false
            historicalExecutionRelabeled = $false
            authorDecisionCreatedByVerifier = $false
        }
        [IO.File]::WriteAllText((Join-Path $LogDirectory "author-closeout-result.json"),
            (($closeout | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"),
            [Text.UTF8Encoding]::new($false))
        Write-Host "G9U1 AUTHOR_CLOSEOUT consistency passed; linked technical evidence was not rerun."
    } else {
    & (Join-Path $PSScriptRoot "tests/workspace-profile-validation.Tests.ps1") -LogDirectory (Join-Path $LogDirectory "profile-infrastructure")
    Assert-U1 ($?) "G9U1 profile-validation fixtures failed."
    if (-not $HistoricalRegressionsAlreadyComposed) {
        & (Join-Path $PSScriptRoot "verify-g9s1-r1-spline-pair-materialization.ps1") -SkipBuild -LogDirectory (Join-Path $LogDirectory "published-r1")
        Assert-U1 ($LASTEXITCODE -eq 0) "Published R1 live static authority failed."
    }
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --check
    Assert-U1 ($LASTEXITCODE -eq 0) "G9U1 git diff --check failed."
    & git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol -C $RepositoryRoot diff --cached --check
    Assert-U1 ($LASTEXITCODE -eq 0) "G9U1 git diff --cached --check failed."
    if ($SkipBuild) {
        Write-Host "G9U1 static contracts coherent; this is not runtime acceptance."
    } else {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $generated = New-RepositoryGeneratedStateSnapshot -RepositoryRoot $RepositoryRoot -DirectoryNames @("build", ".gradle", ".kotlin") -Label "verify-g9u1" -KeepCurrentOutputs:$KeepBuildOutputs
        }
        foreach ($module in @("shared", "desktop")) {
            $classes = @($scenarios.focusedJUnit.classes | Where-Object { $_.module -ceq $module })
            Assert-U1 ($classes.Count -gt 0) "G9U1 focused perimeter omits $module."
            $task = if ($module -ceq "desktop") { ":desktop:desktop:test" } else { ":shared:common-jre:test" }
            $arguments = @($task)
            foreach ($class in $classes) { $arguments += @("--tests", $class.name) }
            Invoke-U1Gradle $arguments "G9U1 $module consumer tests" "g9u1-$module-gradle.log"
        }
        Invoke-U1Gradle @(":shared:common:checkstyleMain", ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleMain", ":desktop:desktop:checkstyleTest") "G9U1 affected Checkstyle" "g9u1-checkstyle.log"
        $results = @($scenarios.focusedJUnit.classes | ForEach-Object { Get-U1ClassResult $_ })
        foreach ($path in @("source/shared/common/build/reports/checkstyle/main.xml",
            "source/shared/common-jre/build/reports/checkstyle/test.xml",
            "source/desktop/desktop/build/reports/checkstyle/main.xml",
            "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            [xml]$style = Read-U1 $path
            Assert-U1 (@($style.SelectNodes("//error")).Count -eq 0) "G9U1 Checkstyle errors: $path"
        }
        $summary = [ordered]@{
            schemaVersion = 1; phase = "G9U1"; state = "FINAL_TOOLBAR_VISUAL_NORMALIZATION_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL"
            baseCommit = $BaseCommit; promptHash = Get-U1Hash $PromptPath
            authorReviewRound2Baseline = $Round1CandidateCommit
            authorReviewRound3Baseline = $Round2CandidateCommit
            finalPresentationPolishBaseline = $Round3CandidateCommit
            finalMicroPresentationBaseline = $FinalPolishCandidateCommit
            finalToolbarVisualNormalizationBaseline = $FinalMicroCandidateCommit
            authorReviewInputCommit = $Round2AuthorInputCommit
            authorReviewInputEntryHash = $Round2AuthorInputEntryCanonicalHash
            authorReviewInputHash = $Round2AuthorInputLiveCanonicalHash
            actions = 110; clusters = 18; families = 11; scenarios = @($scenarios.scenarios.id | Sort-Object -CaseSensitive)
            tests = @($results | Sort-Object { $_.class })
            sourceEvidence = @($evidence.inventory.paths | Sort-Object -CaseSensitive | ForEach-Object {
                Get-U1SourceAuthority $_
            })
            manualAuthorSmoke = $(if ($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION") {
                    "PASS"
                } else { "PENDING" })
            selfApproved = $false
            authorApprovedImplementation = [bool]($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION")
            passClaimedImplementation = [bool]($SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION")
        }
        if ($SelectedLifecycleMode -cne "PRECOMMIT_CANDIDATE") {
            $summary["lifecycle"] = [ordered]@{
                mode = $SelectedLifecycleMode
                implementationCommit = $LifecycleContext.ImplementationCommit
                currentHead = $LifecycleContext.CurrentHead
                infrastructurePaths = @($LifecycleContext.InfrastructurePaths)
                documentaryEvidenceLinked = [bool]$LifecycleContext.DocumentaryEvidenceLinked
            }
        }
        $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent ([IO.Path]::GetFullPath($CanonicalSummaryPath))))
        [IO.File]::WriteAllText($CanonicalSummaryPath, $json, [Text.UTF8Encoding]::new($false))
        $hash = (Get-FileHash $CanonicalSummaryPath -Algorithm SHA256).Hash.ToLowerInvariant()
        if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
            Assert-U1 ($hash -ceq (Get-FileHash $CompareCanonicalSummaryPath -Algorithm SHA256).Hash.ToLowerInvariant()) "G9U1 deterministic summary mismatch."
        }
        Write-Host "G9U1 focused cases: $(($results.tests | Measure-Object -Sum).Sum); canonical SHA-256: $hash"
        Write-Host "G9U1 technical focused gates passed; no author implementation approval is inferred."
    }
    }
} catch { $failure = $_.Exception.Message + "`n" + $_.ScriptStackTrace }
finally {
    try {
        if ($null -ne $generated) { Restore-RepositoryGeneratedStateSnapshot -Snapshot $generated -KeepCurrentOutputs:$KeepBuildOutputs -Description "G9U1 generated output" }
        if ($null -ne $initialStatus) { Assert-U1 ((Get-RepositoryStatusText $RepositoryRoot) -ceq $initialStatus) "G9U1 repository status changed during verification." }
        if ($null -ne $initialHead) { Assert-U1 ((Get-U1Git @("rev-parse", "HEAD")).Trim() -ceq $initialHead) "G9U1 HEAD changed during verification." }
        if ($null -ne $initialIndex) { Assert-U1 ((Get-U1Git @("ls-files", "--stage", "-z")) -ceq $initialIndex) "G9U1 index blob/mode authority changed during verification." }
        if ($null -ne $rawStart) {
            $rawEnd = Get-GeoCeDGPhaseRawInputSnapshot $RepositoryRoot
            Assert-U1 ($rawEnd.sha256 -ceq $rawStart.sha256) "G9U1 physical same-run input bytes changed."
        }
    } catch { $failure = "$failure`nCleanup/input failure: $($_.Exception.Message)" }
}
if ($failure) { Write-Error $failure; exit 1 }
exit 0

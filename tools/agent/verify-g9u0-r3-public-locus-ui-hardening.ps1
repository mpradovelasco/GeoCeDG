[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [switch]$HistoricalRegressionsAlreadyComposed,
    [string]$CanonicalSummaryPath,
    [string]$CompareCanonicalSummaryPath,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r3-public-locus-ui-hardening")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "88801ba540cceeaeb1c2366be3c3a8d705f1b09d"
$ExpectedBranch = "feature/g9u0-r3-public-locus-ui-hardening"
$R2PassTagName = "geocedg-g9u0-r2-pass"
$R2PassTagObject = "ec92e2deb6e850bc56e61db4ad169b8af5dc0ec7"
$R2PassCommit = "9694dd4c3c274f627839d0eb5d2827a7910bf0ca"
$PassTagName = "geocedg-g9u0-r3-pass"
$PassTagObject = "1c1be8ebb58be9ad4c4e7242bc56105f9f310068"
$PassCommitSha = "ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b"
$AuthorApprovedStatus = "PASS_AUTHOR_APPROVED"
$PromptPath =
    ".github/prompts/tasks/g9u0-r3-public-locus-ui-hardening.prompt.md"
$HistoricalG9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace.prompt.md"
$SuccessorG9U1PromptPath =
    ".github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r3.prompt.md"
$HistoricalG9U1PromptCanonicalLfSha256 =
    "502dabbac1f756e01d0f7935a337e389a3c5e26eaabf3452a6ffe953e83b6ddd"
$SuccessorG9U1PromptCanonicalLfSha256 =
    "46d2e8011188dd69488f52972eff558dbcb73dfd6fa6e111a9ddf515633f073e"
$ScenarioPath =
    "geocedg/validation/g9u0-r3/g9u0-r3-public-locus-ui-scenarios.json"
$EvidencePath =
    "geocedg/validation/g9u0-r3/g9u0-r3-public-locus-ui-evidence.json"
$EvidenceHashPath =
    "geocedg/validation/g9u0-r3/g9u0-r3-evidence.sha256"
$ArchitecturePath =
    "docs/architecture/g9u0_r3_public_locus_ui_hardening.md"
$ReportPath =
    "docs/validation/g9u0_r3_public_locus_ui_hardening_candidate_report.md"
$RoadmapPath = "docs/roadmap/geocedg_roadmap.md"
$TraceabilityPath = "docs/validation/g9_documentation_bundle_traceability.md"
$MatrixPath = "docs/validation/g9_public_workspace_validation_matrix.md"
$PublicSpecPath = "geocedg/specs/locus/locus-v2-public-ui-exposure.md"
$FrozenPublicSpecPath = "geocedg/specs/locus/locus-v2-public-surface.md"
$SpecificationIndexPath = "geocedg/specs/README.md"
$DeveloperGuidePath = "docs/developer/geocedg_developer_guide.md"
$UserGuidePath = "docs/user/geocedg_user_guide.md"
$UpstreamImpactPath = "docs/upstream/modified-files.yml"
$MenuSourcePath =
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMenuBar.java"
$DialogSourcePath =
    "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLocusV2Dialogs.java"
$MenuTestPath =
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R3MenuLifecycleTest.java"
$InspectorTestPath =
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U0R3InspectorWorkflowTest.java"
$FeatureServicePath =
    "source/shared/common/src/main/java/org/geocedg/common/main/feature/RuntimeFeatureService.java"
$VerifierPath = "tools/agent/verify-g9u0-r3-public-locus-ui-hardening.ps1"
$HistoricalR2VerifierPath =
    "tools/agent/verify-g9u0-r2-product-refinement.ps1"
$RepositoryStateVerifierPath = "tools/agent/verify-repository-state.ps1"
$ComposedVerifierPath = "tools/agent/verify.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$script:R3BoundaryMode = $null
$script:R3AuthorityCommit = $null

if ([string]::IsNullOrWhiteSpace($CanonicalSummaryPath)) {
    $CanonicalSummaryPath = Join-Path $LogDirectory "canonical-summary.json"
} else {
    $CanonicalSummaryPath = [IO.Path]::GetFullPath($CanonicalSummaryPath)
}
if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
    $CompareCanonicalSummaryPath =
        [IO.Path]::GetFullPath($CompareCanonicalSummaryPath)
}

. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

$ExpectedTestMethods = [ordered]@{
    "org.geocedg.desktop.G9U0R3MenuLifecycleTest" = @(
        "m01ProductMenuIsPopulatedAfterInitMenubar",
        "m02ProductMenuRemainsPopulatedAfterUpdateFonts",
        "m03ProductMenuRemainsPopulatedAfterRepeatedUpdateFonts",
        "m04LocalizationRefreshRebuildsLocalizedItems",
        "m05SingleLocusFlagExposesEveryApprovedProductAction",
        "m06FeatureOffKeepsLocusActionsUnavailable"
    )
    "org.geocedg.desktop.G9U0R3InspectorWorkflowTest" = @(
        "i01MenuClickInspectsAnAlgebraSelectedIntersectionResult",
        "i02SingleUnselectedRichResultIsDiscoveredFromConstruction",
        "i03SeveralRichResultsUseDeterministicConstructionOrderChooser",
        "i04CancelCreatesNoPointOrTokenChild",
        "i05AcceptCreatesExactlyOneExactTokenPoint",
        "i06ExactTokenPointSurvivesEstablishedRecompute",
        "i07MaterializationUsesNormalUndoAndRedo",
        "i08MetricRichResultRemainsSupportedByTheSameMenuAction",
        "i09NoRichResultsShowsTheExistingTypedMessage",
        "i10LongExactTokensHaveBoundedDistinctAccessibleChoices",
        "i11CompactChoiceUsesAndPersistsTheCompleteExactToken",
        "t01TokenAuxiliaryIsNotEuclidianVisible",
        "t02TokenRemainsExactAndHiddenAfterNativeSaveReopen",
        "t03ClosureCopyRemapsExactTokenAndKeepsAuxiliaryHidden",
        "n01IntersectAloneCreatesNoPersistentPointChildren",
        "n02R3IntroducesNoRichResultDrawableOrCandidateMarkerGeo"
    )
    "org.geocedg.desktop.GeoCeDGProfileTest" = @(
        "compilesManifestToolbarIntoExistingGrammar",
        "createsConservativeInitialPerspective",
        "usesTextualIdentityAndNoUpstreamSplash",
        "isolatesDefaultPreferencesPath",
        "exposesExperimentalDxfOnlyThroughGeoCeDGMenu"
    )
    "org.geocedg.desktop.locus.G9U0LocalizationHelpTest" = @(
        "l01CommandNamesAndSyntaxAreLocalizedInEnglishAndSpanish",
        "l02RichStatusAndDiagnosticLabelsAreLocalized",
        "l03GeoCeDGIconHasOwnedProvenanceAndTextFallback",
        "l04LegacyAndV2HelpRemainContextuallyDistinct"
    )
    "org.geocedg.desktop.locus.G9U0RuntimeFeatureTest" = @(
        "f01OneRuntimeDecisionControlsCommandsAndCreation",
        "f02DefaultProfileKeepsExperimentalCreationOff",
        "f03GeoCeDGOptInDoesNotEnableUpstreamClassic",
        "f04LaboratoryAndDualRunRemainSeparateFeatures"
    )
    "org.geocedg.desktop.locus.G9U0ToolSurfaceTest" = @(
        "t01CreationUsesADedicatedSelectionTransactionMode",
        "t01aPointToolRequiresExplicitPreimageDialog",
        "t02GeneralIntersectToolKeepsOneControllerDispatch",
        "t03ExactTokenChooserHasKeyboardAccessibleInspectionEntry"
    )
}

$RequiredCandidatePaths = @(
    $PromptPath,
    $ArchitecturePath,
    $DeveloperGuidePath,
    $RoadmapPath,
    $UpstreamImpactPath,
    $UserGuidePath,
    $TraceabilityPath,
    $MatrixPath,
    $ReportPath,
    $PublicSpecPath,
    $SpecificationIndexPath,
    $EvidenceHashPath,
    $EvidencePath,
    $ScenarioPath,
    $DialogSourcePath,
    $MenuSourcePath,
    $InspectorTestPath,
    $MenuTestPath,
    $HistoricalR2VerifierPath,
    $RepositoryStateVerifierPath,
    $VerifierPath,
    $ComposedVerifierPath
)
$AllowedCandidatePaths = @($RequiredCandidatePaths + $SuccessorG9U1PromptPath)
$DeterministicSourcePaths = @(
    $DialogSourcePath,
    $MenuSourcePath,
    $InspectorTestPath,
    $MenuTestPath
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

function Resolve-RepositoryPath {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)))
    $prefix = $RepositoryRoot.TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes repository: $RelativePath"
    return $absolute
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = Resolve-RepositoryPath $RelativePath
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9U0-R3 path is missing: $RelativePath"
    return $absolute
}

function Get-GitBlobBytes {
    param([Parameter(Mandatory)] [string]$Object)

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    [void]$startInfo.ArgumentList.Add("-C")
    [void]$startInfo.ArgumentList.Add($RepositoryRoot)
    [void]$startInfo.ArgumentList.Add("cat-file")
    [void]$startInfo.ArgumentList.Add("blob")
    [void]$startInfo.ArgumentList.Add($Object)

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $memory = [IO.MemoryStream]::new()
    try {
        [void]$process.Start()
        $copyTask = $process.StandardOutput.BaseStream.CopyToAsync($memory)
        $errorTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [void]$copyTask.GetAwaiter().GetResult()
        $errorText = $errorTask.GetAwaiter().GetResult()
        Assert-Condition -Condition ($process.ExitCode -eq 0) `
            -Message ("Unable to read sealed G9U0-R3 blob ${Object}: " +
                $errorText.Trim())
        return ,([byte[]]$memory.ToArray())
    } finally {
        $memory.Dispose()
        $process.Dispose()
    }
}

function Get-AuthorityBytes {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [void](Resolve-RepositoryPath $RelativePath)
    if ($script:R3BoundaryMode -ne "TAGGED_DESCENDANT" -or
            $null -eq $script:R3AuthorityCommit) {
        return ,([IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath)))
    }
    $normalized = $RelativePath.Replace("\", "/")
    $object = "$($script:R3AuthorityCommit):$normalized"
    return ,(Get-GitBlobBytes -Object $object)
}

function Convert-AuthorityBytesToText {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    return [Text.UTF8Encoding]::new($false, $true).GetString(
        $Bytes, $offset, $Bytes.Length - $offset)
}

function Get-AuthoritySourceText {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [byte[]]$bytes = Get-AuthorityBytes $RelativePath
    return Convert-AuthorityBytesToText -Bytes $bytes
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return (Get-AuthoritySourceText $RelativePath) |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON: $($_.Exception.Message)"
    }
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $text = Get-AuthoritySourceText $RelativePath
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes($canonical))).ToLowerInvariant()
}

function Get-BinarySha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    [byte[]]$bytes = Get-AuthorityBytes $RelativePath
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
}

function Assert-ExactSet {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Actual,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualUnique = @($Actual | Sort-Object -Unique)
    $expectedUnique = @($Expected | Sort-Object -Unique)
    $missing = @($expectedUnique | Where-Object { $_ -notin $actualUnique })
    $unexpected = @($actualUnique | Where-Object { $_ -notin $expectedUnique })
    Assert-Condition -Condition ($Actual.Count -eq $actualUnique.Count -and
            $missing.Count -eq 0 -and $unexpected.Count -eq 0) `
        -Message ("{0} mismatch. missing={1}; unexpected={2}" -f $Description,
            ($missing -join ", "), ($unexpected -join ", "))
}

function Get-WorktreeCandidatePaths {
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only `
            --no-renames $EntrySha --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $paths.Add($path.Replace("\", "/"))
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked R3 candidate paths."
    foreach ($path in @(& git -C $RepositoryRoot ls-files --others `
            --exclude-standard)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked R3 candidate paths."
    return @($paths | Sort-Object -Unique)
}

function Get-CommitCandidatePaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate committed R3 paths."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object { $_.Replace("\", "/") } |
        Sort-Object -Unique)
}

function Get-CandidatePaths {
    if ($script:R3BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeCandidatePaths)
    }
    Assert-Condition -Condition (
            $script:R3BoundaryMode -eq "TAGGED_DESCENDANT" -and
            $null -ne $script:R3AuthorityCommit) `
        -Message "The R3 source-boundary mode was not established."
    return @(Get-CommitCandidatePaths -Commit $script:R3AuthorityCommit)
}

function Assert-EntryAuthority {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()

    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$R2PassTagName").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagObject -eq $R2PassTagObject) `
        -Message "The R2 PASS annotated tag object changed."
    $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $tagType -eq "tag") `
        -Message "$R2PassTagName is not an annotated tag."
    $tagPeel = (& git -C $RepositoryRoot rev-parse "$tagObject^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $tagPeel -eq $R2PassCommit) `
        -Message "The R2 PASS tag peel changed."
    & git -C $RepositoryRoot merge-base --is-ancestor $R2PassCommit $EntrySha
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "R2 PASS is not an ancestor of the R3 entry."

    $between = @(& git -C $RepositoryRoot log --format="%H%x09%s" `
        "$R2PassCommit..$EntrySha")
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $between.Count -eq 1 -and
            $between[0] -eq ($EntrySha + "`tConsolidate BOOK-P0-post operations")) `
        -Message "The documented R2-to-R3 consolidation continuity changed."

    $r3TagObject = ((@(& git -C $RepositoryRoot rev-parse `
        "refs/tags/$PassTagName" 2>$null) -join "")).Trim()
    $hasR3PassTag = $LASTEXITCODE -eq 0
    if ($hasR3PassTag) {
        Assert-Condition -Condition ($r3TagObject -ceq $PassTagObject) `
            -Message "The G9U0-R3 PASS tag object changed."
        $tagType = (& git -C $RepositoryRoot cat-file -t $r3TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagType -eq "tag") `
            -Message "$PassTagName must be an annotated tag."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$r3TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $authorityCommit -ceq $PassCommitSha) `
            -Message "The G9U0-R3 PASS tag peel changed."
        $tagText = @(& git -C $RepositoryRoot cat-file tag $r3TagObject) -join "`n"
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagText.Contains("G9U0-R3") -and
                $tagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "The R3 PASS tag lacks the approved disposition."
        $closeoutRecord = @((& git -C $RepositoryRoot rev-list --parents `
            -n 1 $authorityCommit).Trim() -split '\s+')
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $closeoutRecord.Count -eq 2 -and
                $closeoutRecord[0] -eq $PassCommitSha -and
                $closeoutRecord[1] -eq $EntrySha) `
            -Message "The R3 closeout must remain one direct child of entry."
        & git -C $RepositoryRoot merge-base --is-ancestor $authorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged R3 closeout."
        $script:R3BoundaryMode = "TAGGED_DESCENDANT"
        $script:R3AuthorityCommit = $authorityCommit
    } else {
        Assert-Condition -Condition ($head -eq $EntrySha -and
                $branch -eq $ExpectedBranch) `
            -Message ("Pre-commit R3 verification requires entry HEAD on " +
                "$ExpectedBranch; promoted verification requires its PASS tag.")
        $script:R3BoundaryMode = "WORKTREE"
    }

    $staged = @(& git -C $RepositoryRoot diff --cached --name-only --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $staged.Count -eq 0) `
        -Message "G9U0-R3 verification requires an empty index."
}

function Assert-HashManifest {
    $lines = @((Get-AuthoritySourceText $EvidenceHashPath) -split "`r?`n" |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) -and
            -not $_.TrimStart().StartsWith("#") })
    Assert-Condition -Condition ($lines.Count -eq 2) `
        -Message "R3 hash manifest must contain exactly two durable targets."
    $actualTargets = [Collections.Generic.List[string]]::new()
    foreach ($line in $lines) {
        Assert-Condition -Condition ($line -cmatch `
                '^([0-9a-f]{64})  (.+)$') `
            -Message "Invalid R3 hash-manifest line: $line"
        $expectedHash = $Matches[1]
        $target = $Matches[2].Replace("\", "/")
        $actualTargets.Add($target)
        Assert-Condition -Condition ((Get-CanonicalLfSha256 $target) -ceq
                $expectedHash) `
            -Message "Canonical LF hash mismatch for $target."
    }
    Assert-ExactSet -Actual $actualTargets.ToArray() `
        -Expected @($EvidencePath, $ScenarioPath) `
        -Description "R3 durable hash targets"
}

function Assert-ScenarioContract {
    param([Parameter(Mandatory)] [object]$Scenarios)

    Assert-Condition -Condition ($Scenarios.phase -eq "G9U0-R3" -and
            $Scenarios.status -eq $AuthorApprovedStatus -and
            [bool]$Scenarios.countsFrozen -and
            $Scenarios.testExecution.status -eq "PASSED" -and
            [int]$Scenarios.testExecution.executedJUnit -eq 39 -and
            [int]$Scenarios.testExecution.failures -eq 0 -and
            [int]$Scenarios.testExecution.errors -eq 0 -and
            [int]$Scenarios.testExecution.skipped -eq 0 -and
            $Scenarios.authorReview.status -eq $AuthorApprovedStatus -and
            [bool]$Scenarios.authorReview.boundedWidthCorrectionValidated -and
            [bool]$Scenarios.authorReview.correctiveReSmokePassed -and
            $Scenarios.authorReview.completedBy -eq "AUTHOR") `
        -Message "R3 scenario authority is not frozen."
    $ids = @($Scenarios.groups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.id })
    $expectedIds = @(
        1..6 | ForEach-Object { "R3-M{0:D2}" -f $_ }
    ) + @(
        1..11 | ForEach-Object { "R3-I{0:D2}" -f $_ }
    ) + @(
        1..3 | ForEach-Object { "R3-T{0:D2}" -f $_ }
    ) + @("R3-N01", "R3-N02")
    Assert-ExactSet -Actual $ids -Expected $expectedIds `
        -Description "R3 scenario IDs"

    $scenarioMethods = @($Scenarios.groups | ForEach-Object { $_.cases } |
        ForEach-Object { $_.method })
    $expectedFocusedMethods = @(
        $ExpectedTestMethods["org.geocedg.desktop.G9U0R3MenuLifecycleTest"] +
        $ExpectedTestMethods["org.geocedg.desktop.G9U0R3InspectorWorkflowTest"])
    Assert-ExactSet -Actual $scenarioMethods -Expected $expectedFocusedMethods `
        -Description "R3 scenario methods"
    Assert-Condition -Condition (
            [int]$Scenarios.expectedCounts.focusedR3 -eq 22 -and
            [int]$Scenarios.expectedCounts.supportingFrontend -eq 17 -and
            [int]$Scenarios.expectedCounts.executedJUnit -eq 39) `
        -Message "R3 scenario counts drifted from 22 + 17 = 39."
}

function Assert-EvidenceContract {
    param([Parameter(Mandatory)] [object]$Evidence)

    Assert-Condition -Condition ($Evidence.phase -eq "G9U0-R3" -and
            $Evidence.status -eq $AuthorApprovedStatus -and
            [bool]$Evidence.implementationStarted -and
            -not [bool]$Evidence.approval.selfApproved -and
            [bool]$Evidence.approval.authorApproved -and
            [bool]$Evidence.approval.passClaimed -and
            -not [bool]$Evidence.approval.reviewRequired -and
            $Evidence.approval.disposition -eq $AuthorApprovedStatus -and
            $Evidence.approval.manualAuthorSmoke -eq "PASS" -and
            $Evidence.approval.manualAuthorReSmoke -eq "PASS") `
        -Message "R3 evidence does not record the author-approved closeout."
    Assert-Condition -Condition (
            $Evidence.phaseDisposition.'G9U0-R2' -eq "PASS_AUTHOR_APPROVED" -and
            $Evidence.phaseDisposition.'G9U0-R3' -eq $AuthorApprovedStatus -and
            $Evidence.phaseDisposition.G9U1 -eq "DESIGNED_NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.'G9U0-R4' -eq
                "PROPOSED_BOUNDED_INVESTIGATION_NOT_EXECUTED_NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9B -eq "NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9C -eq "NOT_AUTHORIZED" -and
            $Evidence.phaseDisposition.G9U2 -eq "BLOCKED" -and
            $Evidence.phaseDisposition.productiveG10 -eq "NOT_AUTHORIZED") `
        -Message "R3 evidence broadened or altered retained phase state."
    Assert-Condition -Condition (
            $Evidence.architecture.menuPlacement -eq
                "DESKTOP_APPLICATION_FRONTEND" -and
            $Evidence.architecture.tokenVisibilityPlacement -eq
                "DESKTOP_EXPLICIT_MATERIALIZATION_PRESENTATION_SEAM" -and
            $Evidence.architecture.singleLocusV2Argument -eq "enableLocusV2" -and
            -not [bool]$Evidence.architecture.kernelSemanticsChanged -and
            -not [bool]$Evidence.architecture.intersectionSolverChanged -and
            -not [bool]$Evidence.architecture.durableIdentityChanged -and
            -not [bool]$Evidence.architecture.tokenContentsOrLedgerChanged -and
            -not [bool]$Evidence.architecture.xmlSemanticsChanged -and
            -not [bool]$Evidence.architecture.candidateMarkersImplemented -and
            [bool]$Evidence.architecture.r2PassTaggedDescendantBoundary) `
        -Message "R3 architecture escaped the bounded frontend contract."
    foreach ($property in $Evidence.hardZeroCounters.PSObject.Properties) {
        Assert-Condition -Condition ([int]$property.Value -eq 0) `
            -Message "R3 hard-zero counter $($property.Name) is nonzero."
    }
    Assert-Condition -Condition (
            $Evidence.uiWidthCorrection.status -eq "AUTHOR_VERIFIED_COMPLETE" -and
            -not [bool]$Evidence.uiWidthCorrection.exactTokenChanged -and
            -not [bool]$Evidence.uiWidthCorrection.presentationPersisted -and
            -not [bool]$Evidence.uiWidthCorrection.g9u1SemanticsChanged -and
            $Evidence.uiWidthCorrection.postFixTargetedRegression -eq
                "PASSED_2_OF_2") `
        -Message "The author-verified bounded width correction drifted."
    Assert-Condition -Condition (
            $Evidence.g9u1ProspectiveRevision.g9u1Executed -eq $false -and
            $Evidence.g9u1ProspectiveRevision.g9u1Authorized -eq $false) `
        -Message "R3 evidence must not execute or authorize G9U1."
    Assert-Condition -Condition (
            $Evidence.manualAuthorSmoke.status -eq $AuthorApprovedStatus -and
            [bool]$Evidence.manualAuthorSmoke.passed -and
            $Evidence.manualAuthorSmoke.observation -eq
                "MANUAL SMOKE — FUNCTIONALLY PASSING, UI WIDTH DEFECT FOUND" -and
            $Evidence.manualAuthorSmoke.completedBy -eq "AUTHOR" -and
            $Evidence.manualAuthorSmoke.initialSmoke.status -eq
                "FUNCTIONALLY_PASSING_UI_WIDTH_DEFECT_FOUND" -and
            -not [bool]$Evidence.manualAuthorSmoke.initialSmoke.passed -and
            $Evidence.manualAuthorSmoke.reSmoke.status -eq
                $AuthorApprovedStatus -and
            [bool]$Evidence.manualAuthorSmoke.reSmoke.passed -and
            $Evidence.manualAuthorSmoke.reSmoke.completedBy -eq "AUTHOR") `
        -Message "R3 author smoke/re-smoke chronology is incomplete."

    $candidatePaths = @(Get-CandidatePaths)
    $declaredPaths = @($Evidence.sourceBoundary.candidatePaths)
    Assert-ExactSet -Actual $candidatePaths -Expected $declaredPaths `
        -Description "R3 frozen candidate paths"
    foreach ($required in $RequiredCandidatePaths) {
        Assert-Condition -Condition ($required -in $declaredPaths) `
            -Message "R3 candidate omits required path $required."
    }
    foreach ($path in $declaredPaths) {
        Assert-Condition -Condition ($path -in $AllowedCandidatePaths) `
            -Message "R3 candidate contains unapproved path $path."
    }
    Assert-Condition -Condition (
            $Evidence.sourceBoundary.inventoryStatus -eq "FROZEN" -and
            [int]$Evidence.sourceBoundary.pathCount -eq $declaredPaths.Count -and
            [int]$Evidence.sourceBoundary.productivePathCount -eq 2 -and
            [int]$Evidence.sourceBoundary.testPathCount -eq 2 -and
            [int]$Evidence.sourceBoundary.generatedTrackedArtifacts -eq 0) `
        -Message "R3 candidate inventory counters are inconsistent."

    $revisionStatus = [string]$Evidence.g9u1ProspectiveRevision.status
    if ($revisionStatus -eq "NOT_STARTED_UNTIL_R3_AUTOMATION_GREEN") {
        Assert-Condition -Condition ($SuccessorG9U1PromptPath -notin
                $declaredPaths -and
                $null -eq $Evidence.g9u1ProspectiveRevision.successorPrompt) `
            -Message "Deferred G9U1 revision cannot already declare a successor."
    } elseif ($revisionStatus -eq "PLANNING_REVISION_COMPLETE") {
        Assert-Condition -Condition ($SuccessorG9U1PromptPath -in
                $declaredPaths -and
                $Evidence.g9u1ProspectiveRevision.successorPrompt -eq
                    $SuccessorG9U1PromptPath) `
            -Message "Completed G9U1 revision must declare its successor prompt."
        Assert-Condition -Condition (
                $Evidence.g9u1ProspectiveRevision.historicalPrompt -eq
                    $HistoricalG9U1PromptPath -and
                $Evidence.g9u1ProspectiveRevision.historicalPromptCanonicalLfSha256 -eq
                    $HistoricalG9U1PromptCanonicalLfSha256 -and
                $Evidence.g9u1ProspectiveRevision.successorPromptCanonicalLfSha256 -eq
                    $SuccessorG9U1PromptCanonicalLfSha256 -and
                $Evidence.g9u1ProspectiveRevision.entryDependency -eq
                    "G9U0_R3_PASS_AUTHOR_APPROVED_PLUS_SEPARATE_G9U1_AUTHORIZATION" -and
                [bool]$Evidence.g9u1ProspectiveRevision.markerPresentationOnly -and
                $Evidence.g9u1ProspectiveRevision.brandLogicalRoles.topbar -eq
                    "geocedg.brand.topbar" -and
                $Evidence.g9u1ProspectiveRevision.brandLogicalRoles.startup -eq
                    "geocedg.brand.startup" -and
                $Evidence.g9u1ProspectiveRevision.intendedAuthorSourceFilenames.topbar -eq
                    "helixTopBar.png" -and
                $Evidence.g9u1ProspectiveRevision.intendedAuthorSourceFilenames.startup -eq
                    "helixSnapshot.png" -and
                -not [bool]$Evidence.g9u1ProspectiveRevision.authorBrandAssetsPresent -and
                -not [bool]$Evidence.g9u1ProspectiveRevision.brandingAssetsIntegratedByR3 -and
                -not [bool]$Evidence.g9u1ProspectiveRevision.g9u0R4EntryDependencyAdded) `
            -Message "Completed G9U1 planning evidence is inconsistent."
        Assert-Condition -Condition (
                (Get-CanonicalLfSha256 $HistoricalG9U1PromptPath) -eq
                    $HistoricalG9U1PromptCanonicalLfSha256 -and
                (Get-CanonicalLfSha256 $SuccessorG9U1PromptPath) -eq
                    $SuccessorG9U1PromptCanonicalLfSha256) `
            -Message "Historical or successor G9U1 prompt hash changed."

        $successor = Get-AuthoritySourceText $SuccessorG9U1PromptPath
        $requiredHeadings = @(
            "# Objective", "# Authority and evidence hierarchy", "# Scope",
            "# Explicitly forbidden scope", "# Architectural placement",
            "# Required design/specification",
            "# Geometric invariants and degeneracies",
            "# Compatibility and serialization",
            "# Required tests and commands", "# Required artifacts",
            "# Stop conditions"
        )
        $actualHeadings = @($successor -split "`r?`n" | Where-Object {
                $_.StartsWith("# ", [StringComparison]::Ordinal)
            })
        Assert-ExactSet -Actual $actualHeadings -Expected $requiredHeadings `
            -Description "post-R3 G9U1 successor headings"
        foreach ($fragment in @(
                "G9U0-R3", "PASS — AUTHOR APPROVED",
                "DESIGNED — NOT AUTHORIZED", "transient markers",
                "active/selected rich result", "create all admissible points",
                "never automatic persistent GeoPoints",
                "geocedg.brand.topbar", "geocedg.brand.startup",
                "helixTopBar.png", "helixSnapshot.png", "GeoCeDG Classic",
                "Inspect and construct", "Automation and import/export",
                "All GeoCeDG verification gates passed.")) {
            Assert-Condition -Condition ($successor.Contains($fragment,
                    [StringComparison]::OrdinalIgnoreCase)) `
                -Message "Post-R3 G9U1 successor lacks '$fragment'."
        }
    } else {
        throw "Unknown prospective G9U1 revision status: $revisionStatus"
    }

    if ($HistoricalRegressionsAlreadyComposed) {
        $historyRecords = @()
        foreach ($name in @(
                "g9u0Regression", "g9u0R1Regression", "g9u0R2Regression",
                "g9x1Regression", "g5Regression", "g9aRegression",
                "legacyLocusRegression")) {
            $historyRecords += $Evidence.validation.$name
        }
        $historyPassed = @($historyRecords | Where-Object {
                $_.status -eq "PASSED" -and $_.exitCode -eq 0
            }).Count -eq $historyRecords.Count
        $historyPendingThisComposed = @($historyRecords | Where-Object {
                $_.status -eq "PENDING_CURRENT_COMPOSED" -and
                $null -eq $_.exitCode
            }).Count -eq $historyRecords.Count -and
            $Evidence.validation.composedWithoutSkipBuild.status -eq "PENDING"
        Assert-Condition -Condition ($historyPassed -or
                $historyPendingThisComposed) `
            -Message ("Composed R3 invocation requires recorded historical " +
                "PASS or the explicit first-current-composed transition.")
    }
}

function Assert-ProductStaticContracts {
    $menu = Get-AuthoritySourceText $MenuSourcePath
    $dialog = Get-AuthoritySourceText $DialogSourcePath
    $feature = Get-AuthoritySourceText $FeatureServicePath
    $r2Verifier = Get-AuthoritySourceText $HistoricalR2VerifierPath

    Assert-Condition -Condition (
            ([regex]::Matches($menu,
                'private void populateProductMenu\(\)')).Count -eq 1 -and
            $menu.Contains("super.updateFonts();") -and
            $menu.Contains("populateProductMenu();") -and
            $menu.Contains("productMenu.removeAll();") -and
            $menu.Contains("inspectRichResultSelection()")) `
        -Message "GeoCeDG menu no longer uses one rebuild authority."
    foreach ($key in @(
            "LocusV2.Tool", "LocusV2.Point.Tool", "LocusLength.Total.Tool",
            "LocusLength.Partial.Tool", "LocusV2.Results.Inspect")) {
        Assert-Condition -Condition ($menu.Contains($key)) `
            -Message "GeoCeDG product menu lost action $key."
    }
    Assert-Condition -Condition (
            $dialog.Contains("tokenInput.setAuxiliaryObject(true);") -and
            $dialog.Contains("tokenInput.setEuclidianVisible(false);") -and
            $dialog.Contains("selectIntersectionPoint(construction,")) `
        -Message "Exact-token helper is not retained as hidden normal input."
    Assert-Condition -Condition (
            $dialog.Contains('"LocusV2.Results.Field.Solution"') -and
            $dialog.Contains("new TokenChoice(token, presentation)") -and
            $dialog.Contains("return presentation;") -and
            -not $dialog.Contains('return classification + " — " + token;')) `
        -Message ("Exact token must remain internal while the chooser uses " +
            "bounded localized presentation.")
    Assert-Condition -Condition (
            $feature.Contains('LOCUS_V2_ARGUMENT = "enableLocusV2"') -and
            -not $feature.Contains("enableLocusIntersection")) `
        -Message "R3 changed or multiplied the Locus V2 runtime opt-in."
    foreach ($fragment in @(
            '$PassTagName = "geocedg-g9u0-r2-pass"',
            '$PassTagObject = "ec92e2deb6e850bc56e61db4ad169b8af5dc0ec7"',
            '$PassCommitSha = "9694dd4c3c274f627839d0eb5d2827a7910bf0ca"',
            'merge-base --is-ancestor',
            '$script:R2BoundaryMode = "TAGGED_DESCENDANT"',
            'Get-CommitCandidatePaths -Commit $R2AuthorityCommit')) {
        Assert-Condition -Condition ($r2Verifier.Contains($fragment)) `
            -Message "R2 tagged-descendant boundary lost '$fragment'."
    }

    $changedProduct = @((Get-CandidatePaths) | Where-Object {
            $_.StartsWith("source/", [StringComparison]::Ordinal) -and
            $_ -notlike "*/src/test/*"
        })
    Assert-ExactSet -Actual $changedProduct `
        -Expected @($DialogSourcePath, $MenuSourcePath) `
        -Description "R3 productive source boundary"
}

function Assert-DocumentationContracts {
    foreach ($path in @(
            $PromptPath, $ArchitecturePath, $ReportPath, $RoadmapPath,
            $TraceabilityPath, $MatrixPath, $PublicSpecPath,
            $SpecificationIndexPath, $FrozenPublicSpecPath,
            $DeveloperGuidePath, $UserGuidePath, $HistoricalG9U1PromptPath,
            $SuccessorG9U1PromptPath,
            $UpstreamImpactPath, $ComposedVerifierPath)) {
        [void](Get-AuthorityBytes $path)
    }
    $combined = @(
        $PromptPath, $ArchitecturePath, $ReportPath, $RoadmapPath,
        $TraceabilityPath, $MatrixPath, $PublicSpecPath,
        $DeveloperGuidePath, $UserGuidePath, $SuccessorG9U1PromptPath
    ) | ForEach-Object {
        Get-AuthoritySourceText $_
    }
    $text = $combined -join "`n"
    foreach ($fragment in @(
            "G9U0-R3", "PASS — AUTHOR APPROVED", "enableLocusV2",
            "G9U1", "NOT AUTHORIZED", "candidate marker",
            "MANUAL SMOKE — FUNCTIONALLY PASSING, UI WIDTH DEFECT FOUND",
            "geocedg.brand.topbar", "geocedg.brand.startup")) {
        Assert-Condition -Condition ($text.Contains($fragment,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "R3 living documentation lacks '$fragment'."
    }

    $architecture = Get-AuthoritySourceText $ArchitecturePath
    $report = Get-AuthoritySourceText $ReportPath
    $publicSpec = Get-AuthoritySourceText $PublicSpecPath
    foreach ($fragment in @(
            "G9U0-R3 = PASS — AUTHOR APPROVED",
            "selfApproved = false", "authorApproved = true",
            "passClaimed = true", "manualAuthorSmoke = PASS",
            "manualAuthorReSmoke = PASS")) {
        Assert-Condition -Condition ($architecture.Contains($fragment) -or
                $report.Contains($fragment)) `
            -Message "R3 closeout documentation lacks '$fragment'."
    }
    Assert-Condition -Condition (
            $publicSpec.Contains("G9U0-R3 IMPLEMENTATION PASS — AUTHOR") -and
            $report.Contains("G9U0-R4 = PROPOSED BOUNDED INVESTIGATION") -and
            $report.Contains("G9U1 = DESIGNED — NOT AUTHORIZED")) `
        -Message "R3 normative/retained gate closeout is inconsistent."

    $impact = Read-JsonDocument $UpstreamImpactPath
    foreach ($path in @(
            $MenuSourcePath, $DialogSourcePath, $MenuTestPath,
            $InspectorTestPath)) {
        $matches = @($impact.modifications | Where-Object { $_.path -eq $path })
        Assert-Condition -Condition ($matches.Count -eq 1 -and
                [string]$matches[0].purpose -match "R3|menu|token") `
            -Message "Upstream impact lacks the R3 registration for $path."
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
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $Arguments -LogPath $logPath `
            -Description $Description -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $Arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $Arguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
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

function Get-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [string[]]$ExpectedMethods
    )

    $relativePath =
        "source/desktop/desktop/build/test-results/test/TEST-$ClassName.xml"
    [xml]$result = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile $relativePath)
    $suite = $result.testsuite
    $methods = @($suite.testcase | ForEach-Object {
            ([string]$_.name) -replace '\(.*\)$', ''
        })
    Assert-ExactSet -Actual $methods -Expected $ExpectedMethods `
        -Description "$ClassName test methods"
    Assert-Condition -Condition (
            [int]$suite.tests -eq $ExpectedMethods.Count -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "$ClassName test result is not clean."
    return [ordered]@{
        class = $ClassName
        tests = [int]$suite.tests
        failures = [int]$suite.failures
        errors = [int]$suite.errors
        skipped = [int]$suite.skipped
        methods = @($methods | Sort-Object -CaseSensitive)
    }
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $errors = @(Select-String -LiteralPath (
            Resolve-RequiredFile $RelativePath) -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle errors."
}

function Write-CanonicalSummary {
    param(
        [Parameter(Mandatory)] [object]$Scenarios,
        [Parameter(Mandatory)] [object[]]$TestResults
    )

    $summary = [ordered]@{
        schemaVersion = 1
        phase = "G9U0-R3"
        entrySha = $EntrySha
        r2PassTagObject = $R2PassTagObject
        r2PassCommit = $R2PassCommit
        scenarioIds = @($Scenarios.groups | ForEach-Object { $_.cases } |
            ForEach-Object { $_.id } | Sort-Object -CaseSensitive)
        focusedR3Tests = 22
        supportingFrontendTests = 17
        executedJUnitTests = 39
        testResults = @($TestResults | Sort-Object { $_.class })
        deterministicSourceHashes = @($DeterministicSourcePaths |
            Sort-Object -CaseSensitive | ForEach-Object {
                [ordered]@{ path = $_; sha256 = Get-BinarySha256 $_ }
            })
        contract = [ordered]@{
            menuPopulationAuthorities = 1
            singleLocusV2Argument = "enableLocusV2"
            tokenHelperEuclidianVisible = $false
            richResultDrawable = $false
            automaticPersistentPoints = 0
            candidateMarkerOverlays = 0
            kernelSemanticsChanged = $false
            xmlSemanticsChanged = $false
            g9u1Executed = $false
        }
    }
    $json = ($summary | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
    [void](New-Item -ItemType Directory `
        -Path (Split-Path -Parent $CanonicalSummaryPath) -Force)
    [IO.File]::WriteAllText($CanonicalSummaryPath, $json,
        [Text.UTF8Encoding]::new($false))
    $summaryHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
        $CanonicalSummaryPath).Hash.ToLowerInvariant()
    Write-Host "Canonical summary: $CanonicalSummaryPath"
    Write-Host "Canonical summary SHA-256: $summaryHash"

    if (-not [string]::IsNullOrWhiteSpace($CompareCanonicalSummaryPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath `
                $CompareCanonicalSummaryPath -PathType Leaf) `
            -Message "Comparison summary is missing."
        $comparisonHash = (Get-FileHash -Algorithm SHA256 -LiteralPath `
            $CompareCanonicalSummaryPath).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($summaryHash -ceq $comparisonHash) `
            -Message "Deterministic R3 summary mismatch."
        Write-Host "Deterministic summary comparison: MATCH"
    }
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

    Assert-EntryAuthority
    $authorityPaths = @($RequiredCandidatePaths + @(
            $HistoricalG9U1PromptPath,
            $SuccessorG9U1PromptPath,
            $FrozenPublicSpecPath,
            $FeatureServicePath))
    foreach ($path in @($authorityPaths | Sort-Object -Unique)) {
        [void](Get-AuthorityBytes $path)
    }
    foreach ($path in @(
            "gradlew.bat",
            "tools/agent/repository-generated-state.ps1",
            $MenuSourcePath, $DialogSourcePath, $MenuTestPath,
            $InspectorTestPath, $FeatureServicePath, $VerifierPath)) {
        [void](Resolve-RequiredFile $path)
    }

    Assert-HashManifest
    $scenarios = Read-JsonDocument $ScenarioPath
    $evidence = Read-JsonDocument $EvidencePath
    Assert-ScenarioContract -Scenarios $scenarios
    Assert-EvidenceContract -Evidence $evidence
    Assert-ProductStaticContracts
    Assert-DocumentationContracts

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9U0-R3."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9U0-R3."

    if ($SkipBuild) {
        Write-Host "G9U0-R3 static verification passed."
        Write-Host "G9U0-R3 = PASS — AUTHOR APPROVED (recorded author decision)."
        Write-Host "selfApproved=false; authorApproved=true; passClaimed=true"
    } else {
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedState = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "verify-g9u0-r3" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }
        $arguments = @(
            ":desktop:desktop:test"
        )
        foreach ($class in $ExpectedTestMethods.Keys) {
            $arguments += @("--tests", $class)
        }
        $arguments += @(
            ":desktop:desktop:checkstyleMain",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        Invoke-LoggedGradle -Arguments $arguments `
            -Description "G9U0-R3 focused and frontend-support tests" `
            -LogFileName "g9u0-r3-focused-desktop-gradle.log"

        $results = @()
        foreach ($entry in $ExpectedTestMethods.GetEnumerator()) {
            $results += Get-TestResult -ClassName $entry.Key `
                -ExpectedMethods @($entry.Value)
        }
        $total = (@($results | ForEach-Object { $_.tests }) |
            Measure-Object -Sum).Sum
        Assert-Condition -Condition ($total -eq 39) `
            -Message "R3 executed JUnit count drifted from 39."
        foreach ($path in @(
                "source/desktop/desktop/build/reports/checkstyle/main.xml",
                "source/desktop/desktop/build/reports/checkstyle/test.xml")) {
            Assert-CheckstyleResult -RelativePath $path
        }
        Write-CanonicalSummary -Scenarios $scenarios -TestResults $results
        Write-Host "G9U0-R3 focused result: 22 R3 + 17 frontend JUnit."
        Write-Host "G9U0-R3 = PASS — AUTHOR APPROVED (recorded author decision)."
        Write-Host "selfApproved=false; authorApproved=true; passClaimed=true"
    }
} catch {
    $Failure = $_.Exception
    $FailureContext = $_.ScriptStackTrace
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs `
                -Description "G9U0-R3 output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
                --untracked-files=all) -join "`n"
            if ($LASTEXITCODE -ne 0 -or $finalStatus -ne $InitialStatus) {
                throw ("Repository status changed during R3 verification.`n" +
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

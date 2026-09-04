#requires -Version 7.2
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
# These two producer/presentation repairs implement the explicit author-review
# correction. They do not authorize another kernel path or new geometry.
$ReviewKernelPaths = @(
    "source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java"
)
$ReviewScenarioIds = @(1..15 | ForEach-Object { "U1-RV{0:D2}" -f $_ })
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

function Read-U1Checkpoint {
    param([string]$Path)
    return [Text.UTF8Encoding]::new($false, $true).GetString(
        (Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $ReviewCheckpoint $Path))
}
function Assert-U1ReviewContracts {
    param([object]$Evidence, [object]$Scenarios, [string[]]$Paths)
    [void](Get-U1Git @("merge-base", "--is-ancestor", $ReviewCheckpoint, "HEAD"))
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
    $delta = @((Get-U1Git @("diff", "--name-only", $ReviewCheckpoint)).Split("`n") +
        (Get-U1Git @("ls-files", "--others", "--exclude-standard")).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
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
            $mapped = if ($ReviewMethodReplacements.Contains($method)) { $ReviewMethodReplacements[$method] } else { $method }
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
    Assert-U1 ($Evidence.status -ceq "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
        $Evidence.implementationAuthorized -eq $true -and $Evidence.implementationStarted -eq $true -and
        $Evidence.authorApprovedDesign -eq $true -and $Evidence.manualAuthorSmoke -ceq "PENDING" -and
        $Evidence.selfApproved -eq $false -and $Evidence.authorApprovedImplementation -eq $false -and
        $Evidence.passClaimedImplementation -eq $false) "G9U1 candidate authorization/approval flags are inconsistent."
    Assert-U1 ($Evidence.canonicalPromptLfSha256 -ceq (Get-U1Hash $PromptPath)) "G9U1 canonical execution prompt changed."
    $paths = @((Get-U1Git @("diff", "--name-only", $BaseCommit)).Split("`n") +
        (Get-U1Git @("ls-files", "--others", "--exclude-standard")).Split("`n") |
        Where-Object { $_ } | Sort-Object -Unique -CaseSensitive)
    Assert-U1Set $paths @($Evidence.inventory.paths) "G9U1 exact source candidate inventory"
    $materialization = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    [void](Assert-GeoCeDGMaterializationAttributes -RepositoryRoot $RepositoryRoot `
        -Paths $paths -ConfiguredFilterDrivers $materialization.configuredFilterDrivers)
    Assert-U1ReviewContracts $Evidence $Scenarios $paths
    Assert-U1 (@($paths | Where-Object { $_ -match '^artifacts/|^book/' }).Count -eq 0) "Generated/independent-book path in candidate."
    $profile = Read-U1 "apps/geocedg/application-profile.yml" | ConvertFrom-Json -Depth 100 -AsHashtable
    [void](Assert-GeoCeDGLiveWorkspaceProfile -RepositoryRoot $RepositoryRoot)
    Assert-U1 ($profile.schema_version -eq 2 -and $profile.profile_id -ceq "geocedg-desktop") "G9U1 requires live schema v2."
    Assert-U1 (@($profile.actions).Count -eq 110 -and @($profile.clusters).Count -eq 18 -and
        @($profile.taxonomy.broad_families).Count -eq 11) "G9U1 11/18/110 action authority differs."
    Assert-U1 ($profile.product_policies.continuity.value -eq $false -and
        $profile.product_policies.continuity.locked -eq $true) "GeoCeDG Continuity OFF is not locked."
    Assert-U1Set @($profile.product_policies.languages.offered) @("en", "es") "G9U1 product languages"
    Assert-U1 ($profile.product_policies.languages.fallback -ceq "en") "G9U1 English fallback absent."
    $ids = @($Scenarios.scenarios | ForEach-Object { $_.id })
    $approved = Read-U1 "geocedg/validation/g9u1/g9u1-preexecution-scenarios.json" | ConvertFrom-Json -Depth 100
    $approvedIds = @($approved.groups | ForEach-Object { $_.scenarioIds })
    Assert-U1 ($approvedIds.Count -eq 138) "The historical approved scenario baseline changed."
    Assert-U1Set $ids @($approvedIds + $ReviewScenarioIds) "All historical and bounded author-review scenarios"
    Assert-U1 ($ids.Count -eq 153) "G9U1 requires 138 historical plus 15 author-review scenarios."
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
        foreach ($test in $scenario.tests) { Assert-U1 ($methodKeys.Contains($test)) "Unknown test mapping $test ($($scenario.id))." }
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
        Assert-U1 ((Get-U1Hash $parts[1]) -ceq $parts[0]) "G9U1 authority hash mismatch: $($parts[1])"
    }
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
    $evidence = Read-U1 $EvidencePath | ConvertFrom-Json -Depth 100
    $scenarios = Read-U1 $ScenarioPath | ConvertFrom-Json -Depth 100
    Assert-U1Contracts $evidence $scenarios
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
            schemaVersion = 1; phase = "G9U1"; state = "TECHNICAL_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL"
            baseCommit = $BaseCommit; promptHash = Get-U1Hash $PromptPath
            actions = 110; clusters = 18; families = 11; scenarios = @($scenarios.scenarios.id | Sort-Object -CaseSensitive)
            tests = @($results | Sort-Object { $_.class })
            sourceEvidence = @($evidence.inventory.paths | Sort-Object -CaseSensitive | ForEach-Object {
                Get-U1SourceAuthority $_
            })
            manualAuthorSmoke = "PENDING"; selfApproved = $false; authorApprovedImplementation = $false; passClaimedImplementation = $false
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

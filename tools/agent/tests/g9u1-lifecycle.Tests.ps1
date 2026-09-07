#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot '../phase-lifecycle.ps1'),
    [string]$PolicyPath = (Join-Path $PSScriptRoot `
        '../../../geocedg/validation/operations/g9u1-lifecycle-policy.json'),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        'geocedg-g9u1-lifecycle-tests'),
    [switch]$SkipCommittedContext
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$HelperPath = [IO.Path]::GetFullPath($HelperPath)
$PolicyPath = [IO.Path]::GetFullPath($PolicyPath)
$RepairPolicyPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot `
    '../../../geocedg/validation/operations/g9u1-lifecycle-repair-policy.json'))
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$ProductCheckpoint = '28f7843184cfb202bbfcca1cbcc56a25a7a77bca'
$ProductTree = 'd08d7beb45d04d6e0f0a478f4c04eb0e97e7e667'
$EntryCommit = 'e4ef3d48ea95a0c3243e57dfc703b539d455c33e'
$ManifestPath = 'geocedg/validation/g9u1/g9u1-construction-workspace-evidence.sha256'
$ManifestAuthorityPathsSha256 = 'a05faf95b4527c9e55077085971312945028edc3e70c7ca0de5478caa051f617'
$R1PublishedImplementation = 'f761758bd664504057413539b9729ba444c904c1'
$R1PublishedTechnical = 'a38d4fcde846fc97c51abc8d958de6998302c436'
$R1PublishedCloseout = 'af459d856f1cdc384805f3035203acce8e6f6104'
$ExpectedInfrastructurePaths = @(
    'docs/architecture/g9u1_verifier_lifecycle.md',
    'docs/validation/g9u1_verifier_lifecycle_report.md',
    'geocedg/validation/operations/g9u1-lifecycle-policy.json',
    'geocedg/validation/operations/g9u1-lifecycle-repair-policy.json',
    'tools/agent/phase-lifecycle.ps1',
    'tools/agent/tests/g9u1-lifecycle.Tests.ps1',
    'tools/agent/verification-repair-equivalence.ps1',
    'tools/agent/verify-g9u1-construction-workspace.ps1',
    'tools/agent/verify-g9u1-lifecycle-repair.ps1'
)
$ExpectedCloseoutPaths = @(
    'docs/architecture/cedg_workspace_architecture.md',
    'docs/roadmap/geocedg_roadmap.md',
    'docs/user/geocedg_user_guide.md',
    'docs/validation/g9_documentation_bundle_traceability.md',
    'docs/validation/g9_public_workspace_validation_matrix.md',
    'docs/validation/g9u1_command_tool_consistency_matrix.md',
    'docs/validation/g9u1_construction_workspace_implementation_candidate_report.md',
    'docs/validation/g9u1_workspace_completeness_matrix.md',
    'geocedg/specs/ui/application-profile.md',
    'geocedg/specs/ui/cedg-workspaces.md',
    'geocedg/specs/ui/g9u1-construction-interaction.md',
    'geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json',
    $ManifestPath,
    'geocedg/validation/g9u1/g9u1-author-closeout.json'
)
$Results = [Collections.Generic.List[object]]::new()

. $HelperPath
. (Join-Path $PSScriptRoot '../repository-input-identity.ps1')

function Assert-G9U1LifecycleTest {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-G9U1LifecycleSet {
    param([string[]]$Actual, [string[]]$Expected, [string]$Message)
    $actualSorted = @($Actual | Sort-Object -Unique -CaseSensitive)
    $expectedSorted = @($Expected | Sort-Object -Unique -CaseSensitive)
    Assert-G9U1LifecycleTest ($actualSorted.Count -eq $Actual.Count -and
        $expectedSorted.Count -eq $Expected.Count -and
        ($actualSorted -join "`n") -ceq ($expectedSorted -join "`n")) $Message
}

function Invoke-G9U1LifecycleCase {
    param([string]$Name, [scriptblock]$Action)
    try {
        & $Action
        $Results.Add([ordered]@{ name = $Name; status = 'PASS' })
    } catch {
        $Results.Add([ordered]@{ name = $Name; status = 'FAIL'; error = $_.Exception.Message })
    }
}

function Assert-G9U1LifecycleThrows {
    param([scriptblock]$Action, [string]$Pattern, [string]$Message)
    try { & $Action } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match $Pattern) $Message
        return
    }
    throw $Message
}

function Get-G9U1LifecycleSha256 {
    param([byte[]]$Bytes)
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
}

function Get-G9U1LifecycleTextHash {
    param([string]$Text)
    return Get-G9U1LifecycleSha256 ([Text.UTF8Encoding]::new($false).GetBytes($Text))
}

function Get-G9U1LifecycleCanonicalLfHash {
    param([byte[]]$Bytes)
    $text = ConvertFrom-GeoCeDGStrictUtf8 $Bytes
    return Get-G9U1LifecycleTextHash $text.Replace("`r`n", "`n").Replace("`r", "`n")
}

function Invoke-G9U1LifecycleGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments
    )
    $output = @(& git --no-optional-locks -c commit.gpgSign=false -C $Root @Arguments 2>&1 |
        ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -ne 0) {
        throw "Fixture Git failed: git $($Arguments -join ' '): $($output -join ' ')"
    }
    return ($output -join "`n").TrimEnd("`r", "`n")
}

function New-G9U1NonRegularPublishedRecordFixture {
    # Keep the clone root short enough for the inherited long repository paths
    # on Windows. The isolated fixture remains outside source authority.
    $root = Join-Path ([IO.Path]::GetTempPath()) ('g9u1-record-' +
        [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($LogDirectory)
    $cloneOutput = @(& git --no-optional-locks -c commit.gpgSign=false clone --quiet --shared `
        --no-checkout $RepositoryRoot $root 2>&1 | ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot create isolated published-tag fixture: $($cloneOutput -join ' ')"
    }
    [void](Invoke-G9U1LifecycleGit $root @('config', 'core.autocrlf', 'false'))
    [void](Invoke-G9U1LifecycleGit $root @('config', 'user.name', 'GeoCeDG lifecycle fixture'))
    [void](Invoke-G9U1LifecycleGit $root @('config', 'user.email', 'fixture.invalid@example.invalid'))
    [void](Invoke-G9U1LifecycleGit $root @('checkout', '--quiet', '--detach', $R1PublishedTechnical))
    $paths = @((Invoke-G9U1LifecycleGit $root @('diff', '--name-only', '--no-renames',
                $R1PublishedTechnical, $R1PublishedCloseout)).Split("`n") |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    [void](Invoke-G9U1LifecycleGit $root (@('checkout', $R1PublishedCloseout, '--') + $paths))
    $recordPath = 'geocedg/validation/g9s1-r1/g9s1-r1-author-closeout.json'
    $recordOid = Invoke-G9U1LifecycleGit $root @('rev-parse', ":$recordPath")
    Assert-G9U1LifecycleTest ($recordOid -cmatch '^[0-9a-f]{40}$') `
        'Cannot resolve the synthetic published decision blob.'
    [void](Invoke-G9U1LifecycleGit $root @('update-index', '--add', '--cacheinfo',
            "120000,$recordOid,$recordPath"))
    [void](Invoke-G9U1LifecycleGit $root @('commit', '--quiet', '-m',
            'Synthetic closeout with forbidden decision mode'))
    $tag = 'geocedg-g9s1-r1-pass'
    if ((Invoke-G9U1LifecycleGit $root @('tag', '--list', $tag)) -ceq $tag) {
        [void](Invoke-G9U1LifecycleGit $root @('tag', '--delete', $tag))
    }
    [void](Invoke-G9U1LifecycleGit $root @('tag', '--annotate', $tag, '-m',
            'GeoCeDG G9S1-R1 — PASS — AUTHOR APPROVED'))
    return $root
}

function New-G9U1CloseoutModeFixture {
    $root = Join-Path ([IO.Path]::GetTempPath()) ('g9u1-closeout-mode-' +
        [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory((Join-Path $root 'docs'))
    [void](Invoke-G9U1LifecycleGit $root @('init', '--quiet', '--initial-branch', 'main'))
    [void](Invoke-G9U1LifecycleGit $root @('config', 'user.name', 'GeoCeDG lifecycle fixture'))
    [void](Invoke-G9U1LifecycleGit $root @('config', 'user.email', 'fixture.invalid@example.invalid'))
    [void](Invoke-G9U1LifecycleGit $root @('config', 'core.filemode', 'false'))
    [IO.File]::WriteAllText((Join-Path $root 'docs/status.md'), "candidate`n",
        [Text.UTF8Encoding]::new($false))
    [void](Invoke-G9U1LifecycleGit $root @('add', '--', 'docs/status.md'))
    [void](Invoke-G9U1LifecycleGit $root @('commit', '--quiet', '-m', 'technical'))
    return [pscustomobject][ordered]@{
        Root = $root
        Technical = Invoke-G9U1LifecycleGit $root @('rev-parse', 'HEAD')
        StatusPath = 'docs/status.md'
        RecordPath = 'docs/decision.json'
    }
}

function Set-G9U1CloseoutModeFixture {
    param([Parameter(Mandatory)] [object]$Fixture, [string]$ExecutablePath,
        [switch]$Commit)
    [IO.File]::WriteAllText((Join-Path $Fixture.Root $Fixture.StatusPath), "approved`n",
        [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText((Join-Path $Fixture.Root $Fixture.RecordPath), "{}`n",
        [Text.UTF8Encoding]::new($false))
    [void](Invoke-G9U1LifecycleGit $Fixture.Root @('add', '--',
            $Fixture.StatusPath, $Fixture.RecordPath))
    if (-not [string]::IsNullOrWhiteSpace($ExecutablePath)) {
        [void](Invoke-G9U1LifecycleGit $Fixture.Root @('update-index', '--chmod=+x', '--', $ExecutablePath))
    }
    if ($Commit) {
        [void](Invoke-G9U1LifecycleGit $Fixture.Root @('commit', '--quiet', '-m', 'closeout'))
        return Invoke-G9U1LifecycleGit $Fixture.Root @('rev-parse', 'HEAD')
    }
    return $null
}

$CheckpointBridgeFixture = $null
function Get-G9U1CheckpointBridgeFixture {
    if ($null -ne $script:CheckpointBridgeFixture) {
        return $script:CheckpointBridgeFixture
    }
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $tree = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot $ProductCheckpoint
    $tracked = Get-GeoCeDGRepositoryTrackedIdentity -RepositoryRoot $RepositoryRoot `
        -Commit $ProductCheckpoint
    $inventory = @($tree.Paths | ForEach-Object {
        [pscustomobject][ordered]@{
            path = [string]$_
            exists = $true
            bytes = [long]1
            sha256 = '1' * 64
        }
    })
    $statusText = @($policy.implementationPaths | ForEach-Object {
            " M $([string]$_.path)"
        } | Sort-Object -CaseSensitive) -join "`n"
    $statusHash = Get-G9U1LifecycleTextHash $statusText
    $rawTreeHash = '2' * 64
    $inventoryHash = '3' * 64
    $historical = [pscustomobject][ordered]@{
        rawTreeSha256 = $rawTreeHash
        rawFiles = [long]$inventory.Count
        rawBytes = [long]$inventory.Count
        statusSha256 = $statusHash
    }
    $checkpoint = [pscustomobject][ordered]@{
        schemaVersion = [long]1
        kind = 'G9U1_AUTHOR_REVIEWED_PRODUCT_CHECKPOINT_INPUT_FREEZE'
        productCheckpoint = $ProductCheckpoint
        productTree = $ProductTree
        parentCommit = $EntryCommit
        branch = 'synthetic/checkpoint-bridge'
        indexTree = $ProductTree
        trackedWorktreeClean = $true
        statusPorcelain = @()
        rawTreeSha256 = $rawTreeHash
        rawFiles = [long]$inventory.Count
        rawBytes = [long]$inventory.Count
        inventoryFile = 'input-inventory.json'
        inventoryFileSha256 = $inventoryHash
        historicalExecutionHead = $EntryCommit
        provenanceRule = 'historical execution does not reattribute to the checkpoint'
        authorApprovedPhase = $false
        passClaimed = $false
        selfApproved = $false
    }
    $materialization = [pscustomobject][ordered]@{
        schemaVersion = [long]1
        expectedCommit = $ProductCheckpoint
        treeOid = $ProductTree
        trackedSha256 = [string]$tracked.sha256
        trackedFiles = [long]$inventory.Count
        indexMatchesCommitTreeOutsideStatusOverlay = $true
        trackedWorktreeCleanOutsideStatusOverlay = $true
        allowedStatusPaths = @()
        consumedUntracked = [pscustomobject][ordered]@{
            count = [long]0
            identity = 'EXACT_RAW_SHA256'
            matched = $true
        }
        attributesSha256 = '5' * 64
        materializationConfiguration = [pscustomobject][ordered]@{}
        physicalByteEqualityRequiredAcrossCheckout = $false
    }
    $script:CheckpointBridgeFixture = [pscustomobject][ordered]@{
        Policy = $policy
        Authority = [pscustomobject][ordered]@{
            artifacts = @([pscustomobject][ordered]@{
                    role = 'CHECKPOINT_INPUT_INVENTORY'
                    recordedPath = 'synthetic/input-inventory.json'
                    sha256 = $inventoryHash
                })
        }
        Historical = $historical
        Inventory = $inventory
        Checkpoint = $checkpoint
        Materialization = $materialization
    }
    return $script:CheckpointBridgeFixture
}

function Invoke-G9U1CheckpointBridge {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [object]$HistoricalExecution = $Fixture.Historical,
        [object[]]$CheckpointInventory = $Fixture.Inventory
    )
    Assert-GeoCeDGPhaseCheckpointBridge -RepositoryRoot $RepositoryRoot `
        -Policy $Fixture.Policy -Authority $Fixture.Authority `
        -HistoricalExecution $HistoricalExecution -HistoricalInventory $Fixture.Inventory `
        -Checkpoint $Fixture.Checkpoint -CheckpointInventory $CheckpointInventory `
        -Materialization $Fixture.Materialization
}

function New-G9U1DevIntegrationFixture {
    $root = Join-Path $LogDirectory 'dev-integration-fixture'
    [void][IO.Directory]::CreateDirectory($root)
    $filter = 'org.geocedg.common.workspace.G9U1InputPolicyTest.continuityCannotBeEnabledInProduct'
    $recordedSummary = 'synthetic/dev-summary.json'
    $recordedArchive = 'synthetic/junit.xml'
    $archivePath = Join-Path $root 'junit.xml'
    [IO.File]::WriteAllText($archivePath, '<testsuite tests="1" failures="0" errors="0" />',
        [Text.UTF8Encoding]::new($false))
    $archiveHash = Get-G9U1LifecycleSha256 ([IO.File]::ReadAllBytes($archivePath))
    $rootDocument = [ordered]@{
        schemaVersion = 1; level = 'DEV'; repositoryCommit = $ProductCheckpoint
        module = 'shared'; testFilters = @($filter); state = 'PASS_SCOPED_NOT_ACCEPTANCE'
        exitCode = 0; failure = $null; authorApproved = $false; selfApproved = $false
        devEvidence = $recordedSummary
    }
    $summaryDocument = [ordered]@{
        schemaVersion = 1; level = 'DEV'; state = 'PASS_SCOPED_NOT_ACCEPTANCE'
        module = 'shared'; filters = @($filter); nativeExitCode = 0; tests = 1
        authorApproved = $false; selfApproved = $false
        tasks = @([ordered]@{ task = ':shared:common-jre:test'; outcome = 'EXECUTED' })
        junit = @([ordered]@{
                class = 'org.geocedg.common.workspace.G9U1InputPolicyTest'
                tests = 1; failures = 0; errors = 0; skipped = 0
                sha256 = $archiveHash; archivePath = $recordedArchive
                cases = @([ordered]@{ class = 'org.geocedg.common.workspace.G9U1InputPolicyTest'
                        name = 'continuityCannotBeEnabledInProduct'; status = 'PASS' })
            })
    }
    $rootPath = Join-Path $root 'root.json'
    $summaryPath = Join-Path $root 'summary.json'
    [IO.File]::WriteAllText($rootPath,
        (($rootDocument | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText($summaryPath,
        (($summaryDocument | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    $entries = @(
        [pscustomobject][ordered]@{ role = 'SHARED_INTEGRATION_ROOT'; path = 'root.json'
            recordedPath = 'synthetic/root.json'; sha256 = Get-G9U1LifecycleSha256 ([IO.File]::ReadAllBytes($rootPath)) }
        [pscustomobject][ordered]@{ role = 'SHARED_INTEGRATION_RESULT'; path = 'summary.json'
            recordedPath = $recordedSummary; sha256 = Get-G9U1LifecycleSha256 ([IO.File]::ReadAllBytes($summaryPath)) }
        [pscustomobject][ordered]@{ role = 'REPAIR_ARTIFACT'; path = 'junit.xml'
            recordedPath = $recordedArchive; sha256 = $archiveHash }
    )
    return [pscustomobject][ordered]@{
        Root = $root; RootPath = $rootPath; SummaryPath = $summaryPath
        RootDocument = $rootDocument; SummaryDocument = $summaryDocument
        Entries = $entries
        ByRole = @{
            SHARED_INTEGRATION_ROOT = $entries[0]
            SHARED_INTEGRATION_RESULT = $entries[1]
        }
        Authority = [pscustomobject][ordered]@{ module = 'shared'; filter = $filter }
    }
}

function New-G9U1BundleRoleFixture {
    $entries = @(
        [pscustomobject][ordered]@{ role = 'ONE'; path = 'one.json'; recordedPath = 'one.json'; sha256 = '1' * 64 },
        [pscustomobject][ordered]@{ role = 'TWO'; path = 'two.json'; recordedPath = 'two.json'; sha256 = '2' * 64 },
        [pscustomobject][ordered]@{ role = 'ARTIFACT'; path = 'artifact.bin'; recordedPath = 'history/a.bin'; sha256 = '3' * 64 },
        [pscustomobject][ordered]@{ role = 'REPAIR_ARTIFACT'; path = 'repair.bin'; recordedPath = 'repair/b.bin'; sha256 = '4' * 64 }
    )
    return [pscustomobject][ordered]@{
        Entries = $entries
        SingletonRoles = @('ONE', 'TWO')
        HistoricalPaths = @('history/a.bin')
        RepairPaths = @('repair/b.bin')
    }
}

function New-G9U1LegacyEvidenceBundleFixture {
    $relativeRoot = 'artifacts/g9u1-lifecycle-schema-v1-' + [guid]::NewGuid().ToString('N')
    $root = Join-Path $RepositoryRoot $relativeRoot
    [void][IO.Directory]::CreateDirectory($root)
    $manifestName = 'manifest.json'
    $manifestPath = Join-Path $root $manifestName
    $manifest = [ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_TECHNICAL_EVIDENCE_LINK'
        technicalCommit = $ProductCheckpoint
        files = @()
    }
    $json = ($manifest | ConvertTo-Json -Depth 10).Replace("`r`n", "`n") + "`n"
    [IO.File]::WriteAllText($manifestPath, $json, [Text.UTF8Encoding]::new($false))
    $hash = Get-G9U1LifecycleSha256 ([IO.File]::ReadAllBytes($manifestPath))
    $record = [pscustomobject][ordered]@{
        schemaVersion = [long]1
        phase = 'G9U1'
        mode = 'AUTHOR_CLOSEOUT'
        reviewedTechnicalCommit = $ProductCheckpoint
        authorDecision = 'PASS_AUTHOR_APPROVED'
        evidence = [pscustomobject][ordered]@{
            bundleDirectory = $relativeRoot
            bundleManifestPath = $manifestName
            bundleManifestSha256 = $hash
        }
        selfApproved = $false
    }
    return [pscustomobject][ordered]@{
        Root = $root
        ManifestSha256 = $hash
        Record = $record
    }
}

function New-G9U1R1CrossCheckoutFixture {
    $repair = Get-Content -Raw -LiteralPath $RepairPolicyPath | ConvertFrom-Json -Depth 100
    $authority = $repair.boundedIntegrations.r1CrossCheckout
    $head = Invoke-G9U1LifecycleGit $RepositoryRoot @('rev-parse', 'HEAD')
    $sources = @($authority.verificationCodeSources | ForEach-Object {
            [pscustomobject][ordered]@{ path = [string]$_.path; sha256 = 'a' * 64 }
        })
    $document = [pscustomobject][ordered]@{
        schemaVersion = [long]1
        state = 'AUTHOR_CLOSEOUT_GIT_IDENTITY_PASSED_NOT_NEW_TECHNICAL_EXECUTION'
        reviewedTechnicalCommit = [string]$authority.reviewedTechnicalCommit
        closeoutCommit = [string]$authority.closeoutCommit
        verificationCodeCommit = $head
        verificationCodeStatus = ''
        verificationCodeSources = $sources
        technicalEvidenceBundleSha256 = [string]$authority.bundleSha256
        targetContext = [pscustomobject][ordered]@{
            mode = 'AUTHOR_CLOSEOUT'; phase = 'G9S1-R1'
            reviewedTechnicalCommit = [string]$authority.reviewedTechnicalCommit
            closeoutCommit = [string]$authority.closeoutCommit
            repositoryIdentity = [pscustomobject][ordered]@{
                nonAllowlistedTrackedIdentity = 'GIT_IDENTICAL'
            }
            materialization = [pscustomobject][ordered]@{
                physicalByteEqualityRequiredAcrossCheckout = $false
            }
            documentaryEvidenceLinked = $true
            technicalExecutionRepeated = $false
            authorApproved = $true
            selfApproved = $false
        }
        productRuntimeExecuted = $false
        currentRunReceiptProduced = $false
        authorDecisionCreatedByVerifier = $false
        selfApproved = $false
        failure = $null
    }
    return [pscustomobject][ordered]@{ Authority = $authority; Document = $document; Head = $head }
}

Invoke-G9U1LifecycleCase 'policy pins exact reviewed product checkpoint' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    Assert-G9U1LifecycleTest ([string]$policy.phase -ceq 'G9U1' -and
        [string]$policy.entryCommit -ceq $EntryCommit -and
        [string]$policy.implementationCommit -ceq $ProductCheckpoint -and
        [string]$policy.implementationTree -ceq $ProductTree) `
        'G9U1 lifecycle checkpoint authority differs.'
}

Invoke-G9U1LifecycleCase 'implementation authority authenticates exact eleven-path commit' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    Assert-GeoCeDGPhaseImplementationAuthority $RepositoryRoot $policy
    Assert-G9U1LifecycleTest (@($policy.implementationPaths).Count -eq 11) `
        'G9U1 implementation authority must contain exactly eleven paths.'
}

Invoke-G9U1LifecycleCase 'operational successor allowlist is exhaustive and product-free' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    Assert-G9U1LifecycleSet @($policy.infrastructureFollowupPaths) `
        $ExpectedInfrastructurePaths 'G9U1 operational successor path set differs.'
    Assert-G9U1LifecycleTest ([int]$policy.maximumInfrastructureCommits -eq 1 -and
        @($policy.infrastructureFollowupPaths | Where-Object {
            $_ -cmatch '^(?:source/|apps/geocedg/application-profile\.yml$|.*\.gradle(?:\.kts)?$)'
        }).Count -eq 0) 'Operational successor permits a product/build path.'
}

Invoke-G9U1LifecycleCase 'closeout generator produces only the exact status allowlist' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RepositoryRoot `
        $ProductCheckpoint $policy
    Assert-G9U1LifecycleSet (@($expected.Keys) + [string]$policy.closeout.recordPath) `
        $ExpectedCloseoutPaths 'G9U1 closeout path set differs.'
}

Invoke-G9U1LifecycleCase 'ordered same-path replacements are applied without broad substitution' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RepositoryRoot `
        $ProductCheckpoint $policy
    $grouped = @($policy.closeout.literalReplacements | Group-Object path |
        Where-Object Count -gt 1)
    Assert-G9U1LifecycleTest ($grouped.Count -gt 0) `
        'G9U1 closeout does not exercise ordered same-path replacements.'
    foreach ($group in $grouped) {
        $path = [string]$group.Name
        $text = ConvertFrom-GeoCeDGStrictUtf8 ([byte[]]$expected[$path])
        foreach ($rule in @($group.Group)) {
            Assert-G9U1LifecycleTest (-not $text.Contains([string]$rule.before,
                [StringComparison]::Ordinal)) "Unreplaced closeout literal remains in $path."
        }
    }
}

Invoke-G9U1LifecycleCase 'canonical hash manifest preserves comments and 156 authorities' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RepositoryRoot `
        $ProductCheckpoint $policy
    $text = ConvertFrom-GeoCeDGStrictUtf8 ([byte[]]$expected[$ManifestPath])
    $lines = @($text.TrimEnd("`r", "`n") -split "`n")
    $comments = @($lines | Where-Object { $_.StartsWith('#', [StringComparison]::Ordinal) })
    $records = @($lines | Where-Object { $_ -cmatch '^[0-9a-f]{64}  .+$' })
    $paths = @($records | ForEach-Object { $_.Substring(66) })
    Assert-G9U1LifecycleTest ($comments.Count -eq 5 -and $records.Count -eq 156) `
        'G9U1 closeout manifest lost its five comments or 156 authorities.'
    Assert-G9U1LifecycleTest ((Get-G9U1LifecycleTextHash (($paths -join "`n") + "`n")) -ceq
        $ManifestAuthorityPathsSha256) 'G9U1 manifest authority-path digest differs.'
}

Invoke-G9U1LifecycleCase 'canonical hash manifest keeps product authority separate from status projection' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RepositoryRoot `
        $ProductCheckpoint $policy
    $text = ConvertFrom-GeoCeDGStrictUtf8 ([byte[]]$expected[$ManifestPath])
    $records = @{}
    foreach ($line in @($text.TrimEnd("`r", "`n") -split "`n" | Where-Object {
                $_ -cmatch '^[0-9a-f]{64}  .+$'
            })) {
        $records[$line.Substring(66)] = $line.Substring(0, 64)
    }
    $verifier = 'tools/agent/verify-g9u1-construction-workspace.ps1'
    $productHash = Get-G9U1LifecycleCanonicalLfHash `
        (Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $ProductCheckpoint $verifier)
    $liveHash = Get-G9U1LifecycleCanonicalLfHash ([IO.File]::ReadAllBytes(
            (Join-Path $RepositoryRoot $verifier)))
    Assert-G9U1LifecycleTest ($records[$verifier] -ceq $productHash -and
        $liveHash -cne $productHash) `
        'Operational verifier bytes leaked into the frozen product hash manifest.'
    $statusPath = 'geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json'
    $statusHash = Get-G9U1LifecycleCanonicalLfHash ([byte[]]$expected[$statusPath])
    Assert-G9U1LifecycleTest ($records[$statusPath] -ceq $statusHash) `
        'Expected status-only bytes were not projected into the closeout manifest.'
}

Invoke-G9U1LifecycleCase 'annotated-tag published regression authenticates actual R1 precedent' {
    $context = Get-GeoCeDGPhasePublishedTagRegressionContext -RepositoryRoot $RepositoryRoot `
        -PassTagName 'geocedg-g9s1-r1-pass' `
        -ExpectedTagMessage 'GeoCeDG G9S1-R1 — PASS — AUTHOR APPROVED' `
        -ExpectedPhase 'G9S1-R1' -ExpectedImplementationCommit $R1PublishedImplementation `
        -ExpectedPolicyPath 'geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json'
    Assert-G9U1LifecycleTest ($context.Mode -ceq 'PUBLISHED_REGRESSION' -and
        $context.reviewedTechnicalCommit -ceq $R1PublishedTechnical -and
        $context.closeoutCommit -ceq $R1PublishedCloseout -and
        $context.AuthorApprovedPhase -and $context.PassClaimed -and
        -not $context.SelfApproved) 'Actual R1 tagged closeout was not authenticated.'
}

Invoke-G9U1LifecycleCase 'annotated-tag published regression rejects wrong implementation authority' {
    try {
        [void](Get-GeoCeDGPhasePublishedTagRegressionContext -RepositoryRoot $RepositoryRoot `
            -PassTagName 'geocedg-g9s1-r1-pass' `
            -ExpectedTagMessage 'GeoCeDG G9S1-R1 — PASS — AUTHOR APPROVED' `
            -ExpectedPhase 'G9S1-R1' -ExpectedImplementationCommit ('0' * 40) `
            -ExpectedPolicyPath 'geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json')
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match 'phase/policy authority changed') `
            'Wrong published implementation failed for an unrelated reason.'
        return
    }
    throw 'Wrong published implementation authority was accepted.'
}

Invoke-G9U1LifecycleCase 'published verifier tag guards are mode-aware and policy is tag-bound' {
    $verifierSource = [IO.File]::ReadAllText((Join-Path $RepositoryRoot `
            'tools/agent/verify-g9u1-construction-workspace.ps1')).Replace("`r`n", "`n")
    $modeAwareGuard = '$SelectedLifecycleMode -ceq "PUBLISHED_REGRESSION" -or'
    $guardMessage = 'A G9U1 PASS tag exists before author closeout.'
    $unconditionalGuard = 'Assert-U1\s*\(\s*\[string\]::IsNullOrEmpty\(\(Get-U1Git\s+' +
        '@\("tag",\s*"--list",\s*\$PassTagName\)\)\.Trim\(\)\)\s*\)'
    Assert-G9U1LifecycleTest ([regex]::Matches($verifierSource,
            [regex]::Escape($modeAwareGuard)).Count -eq 5) `
        'The five historical G9U1 tag guards are not mode-aware.'
    Assert-G9U1LifecycleTest ([regex]::Matches($verifierSource,
            [regex]::Escape($guardMessage)).Count -eq 5) `
        'The historical G9U1 tag-guard contract count differs.'
    Assert-G9U1LifecycleTest ([regex]::Matches($verifierSource,
            $unconditionalGuard).Count -eq 0) `
        'An unconditional pre-closeout G9U1 tag guard remains.'
    Assert-G9U1LifecycleTest ($verifierSource.Contains(
            '([string]$LifecycleContext.closeoutCommit) $LifecyclePolicyPath',
            [StringComparison]::Ordinal) -and $verifierSource.Contains(
            'published tagged G9U1 lifecycle policy', [StringComparison]::Ordinal)) `
        'Published manifest authority is not bound to the tagged closeout policy blob.'
}

Invoke-G9U1LifecycleCase 'published tagged closeout rejects non-100644 decision record' {
    $fixtureRoot = New-G9U1NonRegularPublishedRecordFixture
    try {
        [void](Get-GeoCeDGPhasePublishedTagRegressionContext -RepositoryRoot $fixtureRoot `
            -PassTagName 'geocedg-g9s1-r1-pass' `
            -ExpectedTagMessage 'GeoCeDG G9S1-R1 — PASS — AUTHOR APPROVED' `
            -ExpectedPhase 'G9S1-R1' -ExpectedImplementationCommit $R1PublishedImplementation `
            -ExpectedPolicyPath 'geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json')
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match
            'normal non-executable tracked file') `
            'Non-100644 published decision record failed for an unrelated reason.'
        return
    }
    throw 'Published closeout accepted a non-100644 decision record.'
}

Invoke-G9U1LifecycleCase 'closeout path modes accept unchanged pending and committed authority' {
    $fixture = New-G9U1CloseoutModeFixture
    try {
        [void](Set-G9U1CloseoutModeFixture -Fixture $fixture)
        [void](Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $fixture.Root `
            -ReviewedTechnicalCommit $fixture.Technical -ExistingPaths @($fixture.StatusPath) `
            -RecordPath $fixture.RecordPath -PendingCloseout)
        $closeout = Set-G9U1CloseoutModeFixture -Fixture $fixture -Commit
        [void](Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $fixture.Root `
            -ReviewedTechnicalCommit $fixture.Technical -ExistingPaths @($fixture.StatusPath) `
            -RecordPath $fixture.RecordPath -CloseoutCommit $closeout)
    } finally {
        Remove-Item -LiteralPath $fixture.Root -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Invoke-G9U1LifecycleCase 'closeout path modes reject pending and committed mode changes' {
    foreach ($state in @('pending', 'committed')) {
        foreach ($target in @('status', 'record')) {
            $fixture = New-G9U1CloseoutModeFixture
            try {
                $path = if ($target -ceq 'status') { $fixture.StatusPath } else { $fixture.RecordPath }
                $closeout = Set-G9U1CloseoutModeFixture -Fixture $fixture -ExecutablePath $path `
                    -Commit:($state -ceq 'committed')
                Assert-G9U1LifecycleThrows {
                    [void](Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $fixture.Root `
                        -ReviewedTechnicalCommit $fixture.Technical `
                        -ExistingPaths @($fixture.StatusPath) -RecordPath $fixture.RecordPath `
                        -CloseoutCommit $closeout -PendingCloseout:($state -ceq 'pending'))
                } 'changed tracked mode|normal non-executable tracked file' `
                    "Closeout accepted a forbidden $state $target mode change."
            } finally {
                Remove-Item -LiteralPath $fixture.Root -Recurse -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

Invoke-G9U1LifecycleCase 'pending closeout requires complete staged mode authority' {
    foreach ($case in @('untracked-record', 'unstaged-overlay')) {
        $fixture = New-G9U1CloseoutModeFixture
        try {
            [IO.File]::WriteAllText((Join-Path $fixture.Root $fixture.StatusPath), "approved`n",
                [Text.UTF8Encoding]::new($false))
            [IO.File]::WriteAllText((Join-Path $fixture.Root $fixture.RecordPath), "{}`n",
                [Text.UTF8Encoding]::new($false))
            if ($case -ceq 'untracked-record') {
                [void](Invoke-G9U1LifecycleGit $fixture.Root @('add', '--', $fixture.StatusPath))
            } else {
                [void](Invoke-G9U1LifecycleGit $fixture.Root @('add', '--',
                        $fixture.StatusPath, $fixture.RecordPath))
                [IO.File]::WriteAllText((Join-Path $fixture.Root $fixture.StatusPath), "changed again`n",
                    [Text.UTF8Encoding]::new($false))
            }
            Assert-G9U1LifecycleThrows {
                [void](Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $fixture.Root `
                    -ReviewedTechnicalCommit $fixture.Technical `
                    -ExistingPaths @($fixture.StatusPath) -RecordPath $fixture.RecordPath `
                    -PendingCloseout)
            } 'staged mode authority|completely staged' `
                "Pending closeout accepted incomplete Git mode authority: $case."
        } finally {
            Remove-Item -LiteralPath $fixture.Root -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

Invoke-G9U1LifecycleCase 'strict input-identity summary rejects generic PASS document' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $genericPass = [pscustomobject][ordered]@{ status = 'PASS' }
    $structuralProof = [pscustomobject][ordered]@{
        reviewedExecutionPlanSha256 = '0' * 64
        candidateExecutionPlanSha256 = '0' * 64
        unchangedExecutionInputCount = [long]1
    }
    try {
        Assert-GeoCeDGPhaseInputIdentityRepairSummary -Document $genericPass `
            -Policy $policy -StructuralProof $structuralProof `
            -ExpectedPolicyCanonicalLfSha256 ('0' * 64)
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match
            'unsupported or missing property|not exact passing infrastructure evidence') `
            'Generic PASS input-identity evidence failed for an unrelated reason.'
        return
    }
    throw 'Generic PASS document was accepted as strict input-identity evidence.'
}

Invoke-G9U1LifecycleCase 'checkpoint bridge accepts exact inventory and status provenance' {
    $fixture = Get-G9U1CheckpointBridgeFixture
    Invoke-G9U1CheckpointBridge $fixture
}

Invoke-G9U1LifecycleCase 'checkpoint bridge rejects inventory tamper' {
    $fixture = Get-G9U1CheckpointBridgeFixture
    $tampered = @($fixture.Inventory | ForEach-Object {
            [pscustomobject][ordered]@{
                path = [string]$_.path
                exists = [bool]$_.exists
                bytes = [long]$_.bytes
                sha256 = [string]$_.sha256
            }
        })
    $tampered[0].sha256 = 'f' * 64
    try {
        Invoke-G9U1CheckpointBridge $fixture -CheckpointInventory $tampered
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match
            'raw byte bridge differs at record 0') `
            'Checkpoint inventory tamper failed for an unrelated reason.'
        return
    }
    throw 'Checkpoint bridge accepted a tampered raw inventory.'
}

Invoke-G9U1LifecycleCase 'checkpoint bridge rejects historical status tamper' {
    $fixture = Get-G9U1CheckpointBridgeFixture
    $tampered = [pscustomobject][ordered]@{
        rawTreeSha256 = [string]$fixture.Historical.rawTreeSha256
        rawFiles = [long]$fixture.Historical.rawFiles
        rawBytes = [long]$fixture.Historical.rawBytes
        statusSha256 = '0' * 64
    }
    try {
        Invoke-G9U1CheckpointBridge $fixture -HistoricalExecution $tampered
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match
            'status is not exactly the eleven-path implementation overlay') `
            'Historical status tamper failed for an unrelated reason.'
        return
    }
    throw 'Checkpoint bridge accepted a tampered historical status hash.'
}

Invoke-G9U1LifecycleCase 'receipt inventory bridge preserves flat cardinality' {
    $helperSource = [IO.File]::ReadAllText($HelperPath).Replace("`r`n", "`n")
    Assert-G9U1LifecycleTest ($helperSource.Contains(
            '$receiptInventories[$level] = Assert-GeoCeDGPhasePrecommitReceiptClosure `',
            [StringComparison]::Ordinal)) `
        'Receipt inventory is not assigned as the flat function result.'
    Assert-G9U1LifecycleTest (-not $helperSource.Contains(
            '$receiptInventories[$level] = @(Assert-GeoCeDGPhasePrecommitReceiptClosure `',
            [StringComparison]::Ordinal)) `
        'Receipt inventory is wrapped in a second array and loses cohort cardinality.'
    $fixture = { $records = @([pscustomobject]@{ path = 'a' }, [pscustomobject]@{ path = 'b' }); return ,$records }
    $direct = & $fixture
    $nested = @(& $fixture)
    Assert-G9U1LifecycleTest ($direct.Count -eq 2 -and $nested.Count -eq 1 -and
        $nested[0].Count -eq 2) 'PowerShell array-cardinality fixture changed unexpectedly.'
}

Invoke-G9U1LifecycleCase 'bounded DEV integration authenticates task case and artifact closure' {
    $fixture = New-G9U1DevIntegrationFixture
    $paths = @(Assert-GeoCeDGPhaseDevIntegration $fixture.Root $fixture.Entries `
        $fixture.ByRole $ProductCheckpoint 'SHARED' $fixture.Authority)
    Assert-G9U1LifecycleSet $paths @('synthetic/junit.xml') `
        'Bounded DEV archive closure differs.'
}

Invoke-G9U1LifecycleCase 'bounded DEV integration rejects provenance and task tampering' {
    $fixture = New-G9U1DevIntegrationFixture
    $rootTamper = [pscustomobject]$fixture.RootDocument
    $rootTamper.devEvidence = 'synthetic/other-summary.json'
    [IO.File]::WriteAllText($fixture.RootPath,
        (($rootTamper | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseDevIntegration $fixture.Root $fixture.Entries `
            $fixture.ByRole $ProductCheckpoint 'SHARED' $fixture.Authority)
    } 'does not name the authenticated evidence path' `
        'Bounded DEV accepted a mismatched root/summary provenance path.'
    [IO.File]::WriteAllText($fixture.RootPath,
        (($fixture.RootDocument | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    $summaryTamper = [pscustomobject]$fixture.SummaryDocument
    $summaryTamper.tasks = @([ordered]@{ task = ':shared:common-jre:test'; outcome = 'UP_TO_DATE' })
    [IO.File]::WriteAllText($fixture.SummaryPath,
        (($summaryTamper | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseDevIntegration $fixture.Root $fixture.Entries `
            $fixture.ByRole $ProductCheckpoint 'SHARED' $fixture.Authority)
    } 'task was not freshly executed' `
        'Bounded DEV accepted an unexecuted task.'
}

Invoke-G9U1LifecycleCase 'bounded DEV integration rejects case hash and archive tampering' {
    $fixture = New-G9U1DevIntegrationFixture
    $caseTamper = $fixture.SummaryDocument | ConvertTo-Json -Depth 20 | ConvertFrom-Json -Depth 20
    $caseTamper.junit[0].cases[0].status = 'FAIL'
    [IO.File]::WriteAllText($fixture.SummaryPath,
        (($caseTamper | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseDevIntegration $fixture.Root $fixture.Entries `
            $fixture.ByRole $ProductCheckpoint 'SHARED' $fixture.Authority)
    } 'does not contain the exact passing test' `
        'Bounded DEV accepted a missing exact passing case.'

    $fixture = New-G9U1DevIntegrationFixture
    $hashTamper = $fixture.SummaryDocument | ConvertTo-Json -Depth 20 | ConvertFrom-Json -Depth 20
    $hashTamper.junit[0].sha256 = '0' * 64
    [IO.File]::WriteAllText($fixture.SummaryPath,
        (($hashTamper | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
        [Text.UTF8Encoding]::new($false))
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseDevIntegration $fixture.Root $fixture.Entries `
            $fixture.ByRole $ProductCheckpoint 'SHARED' $fixture.Authority)
    } 'Missing or mismatched bounded SHARED JUnit archive' `
        'Bounded DEV accepted a mismatched JUnit archive hash.'
}

Invoke-G9U1LifecycleCase 'schema-v2 bundle role closure accepts exact singleton and artifact sets' {
    $fixture = New-G9U1BundleRoleFixture
    Assert-G9U1LifecycleTest (Assert-GeoCeDGPhaseG9U1BundleRoleClosure `
        -Entries $fixture.Entries -RequiredSingletonRoles $fixture.SingletonRoles `
        -HistoricalArtifactPaths $fixture.HistoricalPaths -RepairArtifactPaths $fixture.RepairPaths) `
        'Exact schema-v2 bundle role closure was rejected.'
}

Invoke-G9U1LifecycleCase 'schema-v2 bundle role closure rejects unknown duplicate and orphan entries' {
    $fixture = New-G9U1BundleRoleFixture
    $unknown = @($fixture.Entries) + [pscustomobject][ordered]@{
        role = 'UNKNOWN'; path = 'unknown'; recordedPath = 'unknown'; sha256 = '5' * 64
    }
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseG9U1BundleRoleClosure -Entries $unknown `
            -RequiredSingletonRoles $fixture.SingletonRoles `
            -HistoricalArtifactPaths $fixture.HistoricalPaths -RepairArtifactPaths $fixture.RepairPaths)
    } 'unknown role' 'Schema-v2 bundle accepted an unknown role.'
    $duplicate = @($fixture.Entries) + $fixture.Entries[0]
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseG9U1BundleRoleClosure -Entries $duplicate `
            -RequiredSingletonRoles $fixture.SingletonRoles `
            -HistoricalArtifactPaths $fixture.HistoricalPaths -RepairArtifactPaths $fixture.RepairPaths)
    } 'duplicate.*singleton' 'Schema-v2 bundle accepted a duplicate singleton role.'
    $orphanHistorical = @($fixture.Entries) + [pscustomobject][ordered]@{
        role = 'ARTIFACT'; path = 'orphan-a'; recordedPath = 'history/orphan.bin'; sha256 = '6' * 64
    }
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseG9U1BundleRoleClosure -Entries $orphanHistorical `
            -RequiredSingletonRoles $fixture.SingletonRoles `
            -HistoricalArtifactPaths $fixture.HistoricalPaths -RepairArtifactPaths $fixture.RepairPaths)
    } 'Historical receipt artifact closure' 'Schema-v2 bundle accepted an orphan historical artifact.'
    $orphanRepair = @($fixture.Entries) + [pscustomobject][ordered]@{
        role = 'REPAIR_ARTIFACT'; path = 'orphan-r'; recordedPath = 'repair/orphan.bin'; sha256 = '7' * 64
    }
    Assert-G9U1LifecycleThrows {
        [void](Assert-GeoCeDGPhaseG9U1BundleRoleClosure -Entries $orphanRepair `
            -RequiredSingletonRoles $fixture.SingletonRoles `
            -HistoricalArtifactPaths $fixture.HistoricalPaths -RepairArtifactPaths $fixture.RepairPaths)
    } 'Bounded DEV repair artifact closure' 'Schema-v2 bundle accepted an orphan repair artifact.'
}

Invoke-G9U1LifecycleCase 'G9U1 dispatcher rejects legacy schema-v1 closeout evidence' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $fixture = New-G9U1LegacyEvidenceBundleFixture
    try {
        Assert-G9U1LifecycleThrows {
            [void](Assert-GeoCeDGTechnicalEvidenceLink -RepositoryRoot $RepositoryRoot `
                -TechnicalCommit $ProductCheckpoint -CloseoutCommit $ProductCheckpoint `
                -Policy $policy -CloseoutRecord $fixture.Record `
                -BundleDirectory $fixture.Root -BundleSha256 $fixture.ManifestSha256 `
                -PendingCloseout)
        } 'requires exact schema-v2 evidence-preserving bundle authority' `
            'G9U1 accepted legacy schema-v1 evidence and bypassed strict closeout authority.'
    } finally {
        Remove-Item -LiteralPath $fixture.Root -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Invoke-G9U1LifecycleCase 'repair runner validates the sealed schema-v2 manifest bytes' {
    $runnerPath = Join-Path $RepositoryRoot 'tools/agent/verify-g9u1-lifecycle-repair.ps1'
    $runnerSource = [IO.File]::ReadAllText($runnerPath).Replace("`r`n", "`n")
    Assert-G9U1LifecycleTest ($runnerSource.Contains(
            '$sealedManifest = ConvertFrom-GeoCeDGPhaseLifecycleJson `',
            [StringComparison]::Ordinal) -and $runnerSource.Contains(
            '([IO.File]::ReadAllBytes($manifestPath)) ''sealed G9U1 evidence-preserving manifest''',
            [StringComparison]::Ordinal) -and $runnerSource.Contains(
            '-TechnicalCommit $CandidateCommit -Policy $lifecycle -Manifest $sealedManifest `',
            [StringComparison]::Ordinal)) `
        'Repair runner does not validate the exact sealed schema-v2 manifest bytes.'
    Assert-G9U1LifecycleTest (-not $runnerSource.Contains(
            '-TechnicalCommit $CandidateCommit -Policy $lifecycle -Manifest $manifest `',
            [StringComparison]::Ordinal)) `
        'Repair runner still passes an in-memory ordered map as strict manifest authority.'
}

Invoke-G9U1LifecycleCase 'committed presentation deltas stop at exact product checkpoint' {
    $verifierPath = Join-Path $RepositoryRoot 'tools/agent/verify-g9u1-construction-workspace.ps1'
    $verifierSource = [IO.File]::ReadAllText($verifierPath).Replace("`r`n", "`n")
    Assert-G9U1LifecycleTest ($verifierSource.Contains(
            'function Get-U1FinalMicroPresentationDeltaPaths {',
            [StringComparison]::Ordinal) -and $verifierSource.Contains(
            '$microDelta = @(Get-U1FinalMicroPresentationDeltaPaths)',
            [StringComparison]::Ordinal)) `
        'Committed verifier does not route final-micro inventory through lifecycle authority.'
    $evidenceBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $ProductCheckpoint `
        'geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json'
    $evidence = ConvertFrom-GeoCeDGPhaseLifecycleJson $evidenceBytes `
        'frozen G9U1 product evidence'
    $baseline = [string]$evidence.finalMicroPresentation.baseline.commit
    $actual = @(Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot $baseline $ProductCheckpoint)
    Assert-G9U1LifecycleSet $actual @($evidence.finalMicroPresentation.inventory.deltaPaths) `
        'Committed final-micro product-checkpoint delta'
}

if (-not $SkipCommittedContext) {
    Invoke-G9U1LifecycleCase 'R1 cross-checkout contract authenticates Git code identity and context' {
        $fixture = New-G9U1R1CrossCheckoutFixture
        Assert-G9U1LifecycleTest (Assert-GeoCeDGPhaseR1CrossCheckoutIntegration `
            -Document $fixture.Document -Authority $fixture.Authority `
            -TechnicalCommit $fixture.Head -RepositoryRoot $RepositoryRoot) `
            'Exact R1 cross-checkout integration contract was rejected.'
    }

    Invoke-G9U1LifecycleCase 'R1 cross-checkout contract rejects code status source and context tampering' {
        foreach ($case in @('commit', 'status', 'source', 'git-source', 'context')) {
            $fixture = New-G9U1R1CrossCheckoutFixture
            switch ($case) {
                'commit' { $fixture.Document.verificationCodeCommit = $ProductCheckpoint }
                'status' { $fixture.Document.verificationCodeStatus = ' M tools/agent/phase-lifecycle.ps1' }
                'source' { $fixture.Document.verificationCodeSources[0].sha256 = 'not-a-sha' }
                'git-source' { $fixture.Authority.verificationCodeSources[0].blob = '0' * 40 }
                'context' {
                    $fixture.Document.targetContext.materialization.physicalByteEqualityRequiredAcrossCheckout = $true
                }
            }
            Assert-G9U1LifecycleThrows {
                [void](Assert-GeoCeDGPhaseR1CrossCheckoutIntegration `
                    -Document $fixture.Document -Authority $fixture.Authority `
                    -TechnicalCommit $fixture.Head -RepositoryRoot $RepositoryRoot)
            } 'failed or relabelled|raw verification-code evidence|Git blob/mode differs|target context' `
                "R1 cross-checkout contract accepted $case tampering."
        }
    }
}

Invoke-G9U1LifecycleCase 'checkpoint evidence retains candidate and false approval flags' {
    $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $ProductCheckpoint `
        'geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json'
    $evidence = ConvertFrom-GeoCeDGPhaseLifecycleJson $bytes 'G9U1 checkpoint evidence'
    Assert-G9U1LifecycleTest ([string]$evidence.status -ceq
        'IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW' -and
        $evidence.selfApproved -is [bool] -and -not $evidence.selfApproved -and
        $evidence.authorApprovedImplementation -is [bool] -and
        -not $evidence.authorApprovedImplementation -and
        $evidence.passClaimedImplementation -is [bool] -and
        -not $evidence.passClaimedImplementation) `
        'Frozen product checkpoint claims approval or PASS.'
}

Invoke-G9U1LifecycleCase 'wrong implementation hash fails closed' {
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $PolicyPath
    $copy = ($policy | ConvertTo-Json -Depth 100 | ConvertFrom-Json -Depth 100)
    $copy.implementationPaths[0].sha256 = '0' * 64
    try {
        Assert-GeoCeDGPhaseImplementationAuthority $RepositoryRoot $copy
    } catch {
        Assert-G9U1LifecycleTest ($_.Exception.Message -match 'blob hash changed') `
            'Wrong implementation hash failed for an unrelated reason.'
        return
    }
    throw 'Wrong implementation hash was accepted.'
}

if (-not $SkipCommittedContext) {
Invoke-G9U1LifecycleCase 'committed context authenticates exact operational ancestry when clean' {
    $status = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot `
        @('status', '--porcelain=v1', '--untracked-files=all')
    Assert-G9U1LifecycleTest ([string]::IsNullOrEmpty($status)) `
        'Final committed-context fixture requires a clean repository.'
    $context = Get-GeoCeDGPhaseLifecycleContext -RepositoryRoot $RepositoryRoot `
        -PolicyPath $PolicyPath -ExpectedImplementationCommit $ProductCheckpoint `
        -Mode COMMITTED_CANDIDATE
    Assert-G9U1LifecycleTest ($context.Mode -ceq 'COMMITTED_CANDIDATE' -and
        $context.ImplementationCommit -ceq $ProductCheckpoint -and
        @($context.CandidatePaths).Count -eq 11 -and
        @($context.InfrastructurePaths).Count -eq 9 -and
        -not $context.AuthorApprovedPhase -and -not $context.PassClaimed -and
        -not $context.SelfApproved) 'Committed lifecycle context drifted.'
}
}

[void][IO.Directory]::CreateDirectory($LogDirectory)
$failed = @($Results | Where-Object status -CEQ 'FAIL')
$summary = [ordered]@{
    schemaVersion = 1
    evidenceKind = 'G9U1_LIFECYCLE_FOCUSED_OPERATIONAL_TESTS_NOT_PRODUCT_EXECUTION'
    tests = $Results.Count
    passed = $Results.Count - $failed.Count
    failed = $failed.Count
    results = @($Results | ForEach-Object {
        [ordered]@{ name = $_.name; status = $_.status }
    })
    productCheckpoint = $ProductCheckpoint
    productBytesChanged = $false
    authorApprovalInferred = $false
    priorExecutionRelabeled = $false
}
$json = ($summary | ConvertTo-Json -Depth 30).Replace("`r`n", "`n") + "`n"
$summaryPath = Join-Path $LogDirectory 'canonical-summary.json'
[IO.File]::WriteAllText($summaryPath, $json, [Text.UTF8Encoding]::new($false))
$hash = Get-G9U1LifecycleTextHash $json
[IO.File]::WriteAllText((Join-Path $LogDirectory 'canonical-summary.sha256'),
    "$hash`n", [Text.UTF8Encoding]::new($false))
Write-Host "G9U1 lifecycle focused tests: $($summary.passed)/$($summary.tests) PASS; SHA-256 $hash"
if ($failed.Count -ne 0) {
    foreach ($failure in $failed) { Write-Error "$($failure.name): $($failure.error)" -ErrorAction Continue }
    exit 1
}
exit 0

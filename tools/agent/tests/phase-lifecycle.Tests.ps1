#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot '../phase-lifecycle.ps1'),
    [string]$R1VerifierPath = (Join-Path $PSScriptRoot `
        '../verify-g9s1-r1-spline-pair-materialization.ps1'),
    [string]$RootVerifierPath = (Join-Path $PSScriptRoot '../verify.ps1'),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        'geocedg-phase-lifecycle-tests')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$Lf = [string][char]10
$ImplementationCommit = 'f761758bd664504057413539b9729ba444c904c1'
$EntryCommit = '109f077fc5e2a40bcde45d3271eb928ee66fdfcc'
$RealRepository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$HelperPath = [IO.Path]::GetFullPath($HelperPath)
$R1VerifierPath = [IO.Path]::GetFullPath($R1VerifierPath)
$RootVerifierPath = [IO.Path]::GetFullPath($RootVerifierPath)
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)

if (-not (Test-Path -LiteralPath $HelperPath -PathType Leaf)) {
    throw "Phase lifecycle helper missing: $HelperPath"
}
. $HelperPath

function Assert-Case {
    param([Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message)
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-Throws {
    param([Parameter(Mandatory)] [scriptblock]$Action,
        [Parameter(Mandatory)] [string]$Pattern,
        [Parameter(Mandatory)] [string]$Message)
    try { & $Action | Out-Null } catch {
        Assert-Case ($_.Exception.Message -match $Pattern) `
            "$Message failed for the wrong reason: $($_.Exception.Message)"
        return
    }
    throw "TEST FAILURE: $Message unexpectedly succeeded."
}

function Write-Text {
    param([Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    [IO.File]::WriteAllText($Path, $Text,
        [Text.UTF8Encoding]::new($false))
}

function Invoke-Git {
    param([Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments)
    $hooks = Join-Path ([IO.Path]::GetTempPath()) `
        'geocedg-phase-lifecycle-empty-hooks'
    [void][IO.Directory]::CreateDirectory($hooks)
    $output = @(& git --no-optional-locks -c "core.hooksPath=$hooks" `
        -c commit.gpgSign=false -C $Root @Arguments 2>&1 |
        ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -ne 0) {
        throw "Fixture git failed: git $($Arguments -join ' '): $($output -join ' ')"
    }
    return $output
}

function Commit-Fixture {
    param([Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Message)
    [void](Invoke-Git $Root @('add', '--all'))
    [void](Invoke-Git $Root @('-c', 'user.name=GeoCeDG lifecycle fixture',
        '-c', 'user.email=fixture@example.invalid', 'commit', '--quiet',
        '-m', $Message))
    return (@(Invoke-Git $Root @('rev-parse', 'HEAD')))[0]
}

function Get-Sha256 {
    param([Parameter(Mandatory)] [string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-CanonicalLfSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    $text = [IO.File]::ReadAllText($Path, [Text.UTF8Encoding]::new($false, $true))
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
}

function Write-Json {
    param([Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Value)
    Write-Text $Path (($Value | ConvertTo-Json -Depth 100).Replace(
            "`r`n", "`n") + "`n")
}

function Read-Json {
    param([Parameter(Mandatory)] [string]$Path)
    return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json -Depth 100
}

function Get-GitBlobText {
    param([Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Object)
    return (@(Invoke-Git $Root @('show', $Object)) -join "`n") + "`n"
}

function Get-AstElementText {
    param([Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [ValidateSet('Function', 'Assignment')]
        [string]$Kind, [Parameter(Mandatory)] [string]$Name)
    $tokens = $null
    $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseInput(
        $Text, [ref]$tokens, [ref]$errors)
    Assert-Case (@($errors).Count -eq 0) "PowerShell parse failure for $Name"
    if ($Kind -ceq 'Function') {
        $matches = @($ast.FindAll({ param($node)
            $node -is [Management.Automation.Language.FunctionDefinitionAst] -and
            $node.Name -ceq $Name
        }, $true))
    } else {
        $matches = @($ast.FindAll({ param($node)
            if ($node -isnot [Management.Automation.Language.AssignmentStatementAst]) {
                return $false
            }
            return $node.Left.Extent.Text -ceq ('$' + $Name)
        }, $true))
    }
    Assert-Case ($matches.Count -eq 1) "Expected one AST element for $Name"
    return $matches[0].Extent.Text.Replace("`r`n", "`n").Replace("`r", "`n")
}

function Assert-TopLevelPreservedWithG9U1 {
    param([Parameter(Mandatory)] [string]$Current,
        [Parameter(Mandatory)] [string]$Sealed)
    $currentLf = $Current.Replace("`r`n", "`n")
    $sealedLf = $Sealed.Replace("`r`n", "`n")
    if ($currentLf -ceq $sealedLf) { return }
    # This is the exact additive G9U1 integration, not an erase-by-prefix rule.
    # Every prior character, task/filter argument and assertion must survive.
    $declaration = @'
$G9U1ConstructionVerifier = Join-Path $PSScriptRoot `
    "verify-g9u1-construction-workspace.ps1"
'@
    $consumer = @'
    $g9u1IntegrationArtifacts = @(
        $G9U1ConstructionVerifier,
        (Join-Path $RepositoryRoot "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.json"),
        (Join-Path $RepositoryRoot "geocedg/validation/g9u1/g9u1-construction-workspace-scenarios.json"),
        (Join-Path $RepositoryRoot "geocedg/validation/g9u1/g9u1-construction-workspace-evidence.sha256"),
        (Join-Path $RepositoryRoot "docs/validation/g9u1_construction_workspace_implementation_candidate_report.md")
    )
    $g9u1Present = @($g9u1IntegrationArtifacts | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf }).Count
    if ($g9u1Present -ne 0 -and $g9u1Present -ne $g9u1IntegrationArtifacts.Count) {
        throw "Incomplete G9U1 integration: focused verifier, evidence, scenarios, hash and report must be paired."
    }
    if ($g9u1Present -eq $g9u1IntegrationArtifacts.Count) {
        Write-Host "`n==> G9U1 CeDG Construction workspace"
        $g9u1Parameters = @{
            HistoricalRegressionsAlreadyComposed = $true
            LogDirectory = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) "g9u1-construction-workspace"
        }
        if ($SkipBuild) { $g9u1Parameters.SkipBuild = $true }
        if ($AllowToolchainDownload) { $g9u1Parameters.AllowToolchainDownload = $true }
        if ($KeepBuildOutputs) { $g9u1Parameters.KeepBuildOutputs = $true }
        Add-CurrentBuildEvidence -Parameters $g9u1Parameters
        & $G9U1ConstructionVerifier @g9u1Parameters
        Assert-LastScriptSuccess -Description "G9U1 CeDG Construction workspace"
    }
'@
    $declarationAnchor = '$BenchmarkRunner = Join-Path $RepositoryRoot "tools\benchmark\run.ps1"'
    $consumerAnchor = '    Write-Host "`n==> Standalone Windows packaging contracts"'
    foreach ($anchor in @($declarationAnchor, $consumerAnchor)) {
        Assert-Case ([regex]::Matches($sealedLf, [regex]::Escape($anchor)).Count -eq 1) `
            'Top-level historical anchor is missing or ambiguous'
    }
    $expected = $sealedLf.Replace($declarationAnchor,
        $declaration.Replace("`r`n", "`n") + "`n" + $declarationAnchor).Replace(
        $consumerAnchor, $consumer.Replace("`r`n", "`n") + "`n`n" + $consumerAnchor)
    Assert-Case ($currentLf -ceq $expected) `
        'Top-level verifier differs outside the exact additive G9U1 integration'
}

function Assert-R1ScientificSourcePreserved {
    $current = [IO.File]::ReadAllText($R1VerifierPath)
    $sealed = Get-GitBlobText $RealRepository `
        "$ImplementationCommit`:tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1"
    foreach ($name in @('RequiredTestClasses', 'RequiredScenarioIds',
        'MandatoryMethodAnchors', 'AuthorityPaths')) {
        Assert-Case ((Get-AstElementText $current Assignment $name) -ceq
            (Get-AstElementText $sealed Assignment $name)) `
            "Scientific assignment changed: $name"
    }
    foreach ($name in @('Get-R1TextHash', 'Get-R1FileHash',
        'Get-R1SourceBytes', 'Assert-R1Set', 'Assert-R1PreservedD2',
        'Invoke-R1Gradle', 'Get-R1TestResult')) {
        Assert-Case ((Get-AstElementText $current Function $name) -ceq
            (Get-AstElementText $sealed Function $name)) `
            "Scientific helper changed: $name"
    }
    $sealedContracts = Get-AstElementText $sealed Function 'Assert-R1Contracts'
    $currentContracts = Get-AstElementText $current Function 'Assert-R1Contracts'
    $scientificAnchor = '$matrix = Get-Content -Raw'
    Assert-Case ($sealedContracts.Contains($scientificAnchor) -and
        $currentContracts.Contains($scientificAnchor)) `
        'Cannot isolate the R1 scientific contract tail'
    $sealedTail = $sealedContracts.Substring($sealedContracts.IndexOf(
            $scientificAnchor, [StringComparison]::Ordinal))
    $currentTail = $currentContracts.Substring($currentContracts.IndexOf(
            $scientificAnchor, [StringComparison]::Ordinal))
    Assert-Case ($sealedTail -ceq $currentTail) `
        'R1 scientific contract tail changed after lifecycle repair'
    $sealedTop = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    $currentTop = [IO.File]::ReadAllText($RootVerifierPath)
    Assert-TopLevelPreservedWithG9U1 -Current $currentTop -Sealed $sealedTop
}

function Assert-R1LifecycleGuardSource {
    $source = [IO.File]::ReadAllText($R1VerifierPath)
    foreach ($literal in @(
            'R1 approval field must be Boolean: $field',
            'R1 author-closeout approval flags are inconsistent.',
            'Committed R1 must retain candidate status.',
            'R1 design authorization is not phase PASS.',
            'Precommit mode cannot accept closeout evidence.',
            'AUTHOR_CLOSEOUT_CONSISTENCY_LINKED_NOT_NEW_EXECUTION')) {
        Assert-Case ($source.Contains($literal)) `
            "R1 lifecycle guard is missing: $literal"
    }
}

function New-LifecycleFixture {
    param([Parameter(Mandatory)] [string]$Root,
        [ValidateRange(1, 4)] [int]$MaximumInfrastructureCommits = 1)
    [void][IO.Directory]::CreateDirectory($Root)
    [void](Invoke-Git $Root @('init', '--quiet', '--initial-branch=fixture-main'))
    [void](Invoke-Git $Root @('config', 'core.autocrlf', 'false'))
    Write-Text (Join-Path $Root '.gitignore') "artifacts/`n"
    $candidateFiles = [ordered]@{
        '.gitignore' = "artifacts/`n"
        'source/product.java' = "final class Product { int value = 1; }`n"
        'source/ProductTest.java' = "final class ProductTest { void scientificCase() {} }`n"
        'tools/phase-verifier.ps1' = "# scientific verifier`n"
        'geocedg/tolerance.yml' = "absolute: 1e-9`n"
        'geocedg/scenarios.json' = "{`"scenarioIds`":[]}`n"
        'geocedg/approved-evidence.sha256' = "pending evidence manifest`n"
        'geocedg/evidence.json' = "{`"status`":`"ENTRY`"}`n"
        'docs/report.md' = "PHASE = ENTRY`n"
        'docs/reference.md' = "scientific reference v1`n"
        'tools/infra-a.txt' = "candidate infra A`n"
        'tools/infra-b.txt' = "candidate infra B`n"
    }
    foreach ($entry in $candidateFiles.GetEnumerator()) {
        Write-Text (Join-Path $Root $entry.Key) ("entry:`n" + $entry.Value)
    }
    $entryCommit = Commit-Fixture $Root 'Synthetic entry'
    foreach ($entry in $candidateFiles.GetEnumerator()) {
        Write-Text (Join-Path $Root $entry.Key) $entry.Value
    }
    $candidateEvidence = [ordered]@{
        status = 'IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW'
        approval = [ordered]@{
            designApproved = $true
            implementationAuthorized = $true
            selfApproved = $false
            authorApprovedPhase = $false
            passClaimed = $false
            manualAuthorSmoke = 'PENDING'
        }
    }
    Write-Json (Join-Path $Root 'geocedg/evidence.json') $candidateEvidence
    Write-Text (Join-Path $Root 'docs/report.md') `
        "G9S1-R1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`n"
    $implementationCommit = Commit-Fixture $Root 'Synthetic implementation candidate'
    $implementationTree = (@(Invoke-Git $Root @('rev-parse', 'HEAD^{tree}')))[0]
    $changed = @((@(Invoke-Git $Root @('diff', '--name-only', '--no-renames',
        $entryCommit, $implementationCommit, '--'))) | Sort-Object -CaseSensitive)
    $implementationPaths = foreach ($path in $changed) {
        $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $Root $implementationCommit $path
        [ordered]@{
            path = $path
            sha256 = Get-GeoCeDGPhaseLifecycleHash $bytes
        }
    }
    $approvedEvidence = [ordered]@{
        status = 'PASS_AUTHOR_APPROVED'
        approval = [ordered]@{
            designApproved = $true
            implementationAuthorized = $true
            selfApproved = $false
            authorApprovedPhase = $true
            passClaimed = $true
            manualAuthorSmoke = 'PASS'
        }
    }
    $candidateEvidenceText = ([IO.File]::ReadAllText(
        (Join-Path $Root 'geocedg/evidence.json')))
    $approvedEvidenceText = (($approvedEvidence | ConvertTo-Json -Depth 10).
        Replace("`r`n", "`n") + "`n")
    $candidateReport = [IO.File]::ReadAllText((Join-Path $Root 'docs/report.md'))
    $approvedReport = "G9S1-R1 = PASS — AUTHOR APPROVED`n"
    $policyPath = 'geocedg/lifecycle-policy.json'
    $recordPath = 'geocedg/author-closeout.json'
    $hashManifestPath = 'geocedg/approved-evidence.sha256'
    $policy = [ordered]@{
        schemaVersion = 1
        phase = 'G9S1-R1-FIXTURE'
        entryCommit = $entryCommit
        implementationCommit = $implementationCommit
        implementationTree = $implementationTree
        implementationPaths = @($implementationPaths)
        infrastructureFollowupPaths = @($policyPath, 'tools/infra-a.txt',
            'tools/infra-b.txt')
        maximumInfrastructureCommits = $MaximumInfrastructureCommits
        closeout = [ordered]@{
            recordPath = $recordPath
            literalReplacements = @(
                [ordered]@{ path = 'geocedg/evidence.json';
                    before = $candidateEvidenceText; after = $approvedEvidenceText;
                    occurrences = 1 },
                [ordered]@{ path = 'docs/report.md'; before = $candidateReport;
                    after = $approvedReport; occurrences = 1 }
            )
            canonicalLfHashManifests = @(
                [ordered]@{ path = $hashManifestPath;
                    authorityPaths = @('docs/report.md', 'geocedg/evidence.json') }
            )
        }
    }
    Write-Json (Join-Path $Root $policyPath) $policy
    $technicalCommit = Commit-Fixture $Root 'Synthetic infrastructure follow-up'
    return [pscustomobject][ordered]@{
        Root = $Root
        EntryCommit = $entryCommit
        ImplementationCommit = $implementationCommit
        TechnicalCommit = $technicalCommit
        PolicyPath = $policyPath
        RecordPath = $recordPath
        HashManifestPath = $hashManifestPath
    }
}

function Copy-LifecycleFixture {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [string]$Name)
    $root = Join-Path (Split-Path -Parent $Fixture.Root) $Name
    $parent = Split-Path -Parent $Fixture.Root
    $hooks = Join-Path $Fixture.Root '.git/empty-hooks'
    $output = @(& git --no-optional-locks -c "core.hooksPath=$hooks" `
        -c commit.gpgSign=false -c core.autocrlf=false -C $parent clone `
        --quiet --no-hardlinks $Fixture.Root $root 2>&1 |
        ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -ne 0) {
        throw "Fixture clone failed: $($output -join ' ')"
    }
    [void](Invoke-Git $root @('config', 'core.autocrlf', 'false'))
    return [pscustomobject][ordered]@{
        Root = $root
        EntryCommit = $Fixture.EntryCommit
        ImplementationCommit = $Fixture.ImplementationCommit
        TechnicalCommit = $Fixture.TechnicalCommit
        PolicyPath = $Fixture.PolicyPath
        RecordPath = $Fixture.RecordPath
        HashManifestPath = $Fixture.HashManifestPath
    }
}

function Get-FixturePolicy {
    param([Parameter(Mandatory)] [object]$Fixture)
    return Read-GeoCeDGPhaseLifecyclePolicy $Fixture.Root $Fixture.PolicyPath
}

function Set-FixturePolicy {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [scriptblock]$Mutation)
    $policy = Read-Json (Join-Path $Fixture.Root $Fixture.PolicyPath)
    [void](& $Mutation $policy)
    Write-Json (Join-Path $Fixture.Root $Fixture.PolicyPath) $policy
}

function Update-ReceiptFingerprint {
    param([Parameter(Mandatory)] [object]$Receipt)
    $identityJson = $Receipt.inputIdentity | ConvertTo-Json -Depth 20 -Compress
    [void]($Receipt.inputFingerprint = Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($identityJson)))
}

function Get-FixtureIndexAuthority {
    param([Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit)
    Assert-Case (@(Invoke-Git $Root @('diff', '--cached', '--name-only',
        $Commit, '--')).Count -eq 0) 'Fixture index differs from technical commit'
    # Independent producer oracle: never generate receipt identity with the
    # commit-to-index reconstruction helper that the consumer is testing.
    $lines = @(Invoke-Git $Root @('-c', 'core.quotepath=false',
        'ls-files', '--stage'))
    $paths = foreach ($line in $lines) {
        Assert-Case ($line -cmatch '^[0-7]{6} [0-9a-f]{40} 0\t.+$') `
            'Malformed independent fixture index entry'
        ($line -split "`t", 2)[1]
    }
    return [pscustomobject]@{
        Lines = $lines
        Paths = @($paths)
        IndexSha256 = Get-GeoCeDGPhaseLifecycleHash `
            ([Text.UTF8Encoding]::new($false).GetBytes($lines -join "`n"))
    }
}

function New-TechnicalBundle {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [string]$Name,
        [scriptblock]$Mutation)
    $Root = Join-Path $Fixture.Root "artifacts/$Name"
    $TechnicalCommit = $Fixture.TechnicalCommit
    [void][IO.Directory]::CreateDirectory($Root)
    $tree = Get-FixtureIndexAuthority $Fixture.Root $TechnicalCommit
    $inventory = [Collections.Generic.List[object]]::new()
    [long]$rawBytes = 0
    foreach ($path in @($tree.Paths | Sort-Object -CaseSensitive)) {
        $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $Fixture.Root `
            $TechnicalCommit $path
        $rawBytes += [long]$bytes.Length
        $inventory.Add([ordered]@{
            path = $path
            exists = $true
            bytes = [long]$bytes.Length
            sha256 = Get-GeoCeDGPhaseLifecycleHash $bytes
        })
    }
    $inventoryJson = @($inventory) | ConvertTo-Json -Depth 10 -Compress
    $rawTreeSha256 = Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($inventoryJson))
    $emptySha256 = Get-GeoCeDGPhaseLifecycleHash ([byte[]]::new(0))
    $documents = [ordered]@{}
    $documents['phase-a-root.json'] = [ordered]@{ level = 'PHASE'; phase = 'G9S1-R1-FIXTURE';
        repositoryCommit = $TechnicalCommit; exitCode = 0;
        state = 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL'; failure = $null;
        authorApproved = $false; selfApproved = $false }
    $documents['phase-b-root.json'] = [ordered]@{ level = 'PHASE'; phase = 'G9S1-R1-FIXTURE';
        repositoryCommit = $TechnicalCommit; exitCode = 0;
        state = 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL'; failure = $null;
        authorApproved = $false; selfApproved = $false }
    $phaseSummary = [ordered]@{
        phase = 'G9S1-R1-FIXTURE'
        state = 'TECHNICAL_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL'
        selfApproved = $false
        authorApprovedPhase = $false
        passClaimed = $false
    }
    $documents['phase-a-summary.json'] = $phaseSummary | ConvertTo-Json -Depth 10 | ConvertFrom-Json -Depth 10
    $documents['phase-b-summary.json'] = $phaseSummary | ConvertTo-Json -Depth 10 | ConvertFrom-Json -Depth 10
    foreach ($level in @('COMPOSED', 'FULL')) {
        $prefix = $level.ToLowerInvariant()
        $receiptRecorded = "records/$prefix/current-run.json"
        $artifactRecords = [ordered]@{
            junit = [pscustomobject]@{ file = "artifact-$prefix-junit.xml";
                recorded = "archive/$prefix/junit.xml" }
            audit = [pscustomobject]@{ file = "artifact-$prefix-audit.json";
                recorded = "archive/$prefix/audit.json" }
            checkstyle = [pscustomobject]@{ file = "artifact-$prefix-checkstyle.xml";
                recorded = "archive/$prefix/checkstyle.xml" }
            native = [pscustomobject]@{ file = "artifact-$prefix-native.log";
                recorded = "archive/$prefix/native.log" }
            inventory = [pscustomobject]@{ file = "artifact-$prefix-input-inventory.json";
                recorded = "archive/$prefix/input-inventory.json" }
        }
        Write-Text (Join-Path $Root $artifactRecords.junit.file) `
            '<testsuite tests="2" failures="0" errors="0" skipped="0" />'
        Write-Text (Join-Path $Root $artifactRecords.audit.file) `
            "{`"status`":`"PASS`"}`n"
        Write-Text (Join-Path $Root $artifactRecords.checkstyle.file) `
            '<checkstyle version="10.12" />'
        Write-Text (Join-Path $Root $artifactRecords.native.file) `
            "$level native fixture PASS`n"
        Write-Json (Join-Path $Root $artifactRecords.inventory.file) @($inventory)
        foreach ($record in $artifactRecords.Values) {
            $record | Add-Member -NotePropertyName sha256 -NotePropertyValue `
                (Get-Sha256 (Join-Path $Root $record.file))
        }
        $inputIdentity = [ordered]@{
            head = $TechnicalCommit
            statusSha256 = $emptySha256
            indexSha256 = $tree.IndexSha256
            rawTreeSha256 = $rawTreeSha256
            rawFiles = [int]$inventory.Count
            rawBytes = [long]$rawBytes
        }
        $documents["$prefix-root.json"] = [ordered]@{ level = $level;
            repositoryCommit = $TechnicalCommit; exitCode = 0;
            state = 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL'; failure = $null;
            authorApproved = $false; selfApproved = $false;
            canonicalReceipt = $receiptRecorded }
        $documents["$prefix-receipt.json"] = [ordered]@{
            schemaVersion = 1
            kind = 'CURRENT_RUN_BUILD_EVIDENCE'
            level = $level
            state = 'TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING'
            inputIdentity = $inputIdentity
            inputFingerprint = ''
            authorApproved = $false
            selfApproved = $false
            testResultReuseAcrossRuns = $false
            selections = [ordered]@{
                shared = [ordered]@{ unfiltered = ($level -ceq 'FULL'); filters = @() }
                desktop = [ordered]@{ unfiltered = ($level -ceq 'FULL'); filters = @() }
            }
            tasks = @(
                [ordered]@{ task = ':shared:common-jre:test'; outcome = 'EXECUTED' },
                [ordered]@{ task = ':desktop:desktop:test'; outcome = 'EXECUTED' }
            )
            junit = @([ordered]@{ class = 'org.geocedg.fixture.ScienceTest';
                module = 'shared';
                tests = 2; failures = 0; errors = 0; skipped = 0;
                cases = @(
                    [ordered]@{ class = 'org.geocedg.fixture.ScienceTest';
                        name = 'caseA'; status = 'PASSED' },
                    [ordered]@{ class = 'org.geocedg.fixture.ScienceTest';
                        name = 'caseB'; status = 'PASSED' }
                ); archivePath = $artifactRecords.junit.recorded;
                sha256 = $artifactRecords.junit.sha256 })
            tests = 2
            skippedUpstreamTests = 0
            auditArtifacts = @(
                [ordered]@{ path = $artifactRecords.audit.recorded;
                    sha256 = $artifactRecords.audit.sha256 },
                [ordered]@{ path = $artifactRecords.native.recorded;
                    sha256 = $artifactRecords.native.sha256 },
                [ordered]@{ path = $artifactRecords.inventory.recorded;
                    sha256 = $artifactRecords.inventory.sha256 }
            )
            checkstyle = [ordered]@{ archivePath = $artifactRecords.checkstyle.recorded;
                sha256 = $artifactRecords.checkstyle.sha256 }
            nativeRuns = @([ordered]@{ logPath = $artifactRecords.native.recorded;
                exitCode = 0 })
        }
        Update-ReceiptFingerprint $documents["$prefix-receipt.json"]
    }
    if ($null -ne $Mutation) { [void](& $Mutation $documents) }
    foreach ($entry in $documents.GetEnumerator()) {
        Write-Json (Join-Path $Root $entry.Key) $entry.Value
    }
    $roleFiles = [ordered]@{
        PHASE_A_ROOT = 'phase-a-root.json'
        PHASE_A_SUMMARY = 'phase-a-summary.json'
        PHASE_B_ROOT = 'phase-b-root.json'
        PHASE_B_SUMMARY = 'phase-b-summary.json'
        COMPOSED_ROOT = 'composed-root.json'
        COMPOSED_RECEIPT = 'composed-receipt.json'
        FULL_ROOT = 'full-root.json'
        FULL_RECEIPT = 'full-receipt.json'
    }
    $files = [Collections.Generic.List[object]]::new()
    foreach ($entry in $roleFiles.GetEnumerator()) {
        $recorded = if ($entry.Key -ceq 'COMPOSED_RECEIPT') {
            'records/composed/current-run.json'
        } elseif ($entry.Key -ceq 'FULL_RECEIPT') {
            'records/full/current-run.json'
        } else { '' }
        $files.Add([ordered]@{ role = $entry.Key; path = $entry.Value;
            recordedPath = $recorded; sha256 = Get-Sha256 (Join-Path $Root $entry.Value) })
    }
    foreach ($level in @('composed', 'full')) {
        $recorded = [ordered]@{
            'junit.xml' = "archive/$level/junit.xml"
            'audit.json' = "archive/$level/audit.json"
            'checkstyle.xml' = "archive/$level/checkstyle.xml"
            'native.log' = "archive/$level/native.log"
            'input-inventory.json' = "archive/$level/input-inventory.json"
        }
        foreach ($entry in $recorded.GetEnumerator()) {
            $path = "artifact-$level-$($entry.Key)"
            $files.Add([ordered]@{ role = 'ARTIFACT'; path = $path;
                recordedPath = $entry.Value; sha256 = Get-Sha256 (Join-Path $Root $path) })
        }
    }
    $manifestPath = Join-Path $Root 'manifest.json'
    Write-Json $manifestPath ([ordered]@{ schemaVersion = 1;
        kind = 'GEOCEDG_TECHNICAL_EVIDENCE_LINK';
        technicalCommit = $TechnicalCommit; files = @($files) })
    return [pscustomobject][ordered]@{
        Root = $Root
        RelativeDirectory = [IO.Path]::GetRelativePath(
            $Fixture.Root, $Root).Replace('\', '/')
        ManifestPath = 'manifest.json'
        ManifestSha256 = Get-Sha256 $manifestPath
    }
}

function Sync-BundleManifest {
    param([Parameter(Mandatory)] [object]$Bundle)
    $path = Join-Path $Bundle.Root $Bundle.ManifestPath
    $manifest = Read-Json $path
    foreach ($entry in @($manifest.files)) {
        [void]($entry.sha256 = Get-Sha256 `
            (Join-Path $Bundle.Root $entry.path))
    }
    Write-Json $path $manifest
    [void]($Bundle.ManifestSha256 = Get-Sha256 $path)
}

function Update-BundleManifest {
    param([Parameter(Mandatory)] [object]$Bundle,
        [Parameter(Mandatory)] [scriptblock]$Mutation)
    $path = Join-Path $Bundle.Root $Bundle.ManifestPath
    $manifest = Read-Json $path
    [void](& $Mutation $manifest)
    Write-Json $path $manifest
    [void]($Bundle.ManifestSha256 = Get-Sha256 $path)
}

function Set-CloseoutState {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [object]$Bundle)
    $policy = Get-FixturePolicy $Fixture
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $Fixture.Root `
        $Fixture.TechnicalCommit $policy
    foreach ($entry in $expected.GetEnumerator()) {
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent `
            (Join-Path $Fixture.Root $entry.Key)))
        [IO.File]::WriteAllBytes((Join-Path $Fixture.Root $entry.Key),
            [byte[]]$entry.Value)
    }
    Write-Json (Join-Path $Fixture.Root $Fixture.RecordPath) ([ordered]@{
        schemaVersion = 1
        phase = 'G9S1-R1-FIXTURE'
        mode = 'AUTHOR_CLOSEOUT'
        reviewedTechnicalCommit = $Fixture.TechnicalCommit
        authorDecision = 'PASS_AUTHOR_APPROVED'
        evidence = [ordered]@{ bundleDirectory = $Bundle.RelativeDirectory;
            bundleManifestPath = $Bundle.ManifestPath;
            bundleManifestSha256 = $Bundle.ManifestSha256 }
        selfApproved = $false
    })
}

function Invoke-FixtureContext {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [ValidateSet('PRECOMMIT',
            'COMMITTED_CANDIDATE', 'AUTHOR_CLOSEOUT')] [string]$Mode,
        [object]$Bundle, [switch]$PendingCloseout,
        [string]$ReviewedTechnicalCommit = $Fixture.TechnicalCommit,
        [string]$ExpectedImplementationCommit = $Fixture.ImplementationCommit)
    $parameters = @{
        RepositoryRoot = $Fixture.Root
        PolicyPath = $Fixture.PolicyPath
        ExpectedImplementationCommit = $ExpectedImplementationCommit
        Mode = $Mode
    }
    if ($Mode -ceq 'AUTHOR_CLOSEOUT') {
        $parameters.ReviewedTechnicalCommit = $ReviewedTechnicalCommit
        $parameters.CloseoutRecordPath = $Fixture.RecordPath
        $parameters.BundleDirectory = $Bundle.Root
        $parameters.BundleSha256 = $Bundle.ManifestSha256
        if ($PendingCloseout) { $parameters.PendingCloseout = $true }
    }
    return Get-GeoCeDGPhaseLifecycleContext @parameters
}

function Update-BundleJson {
    param([Parameter(Mandatory)] [object]$Bundle,
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [scriptblock]$Mutation)
    $path = Join-Path $Bundle.Root $RelativePath
    $value = Read-Json $path
    [void](& $Mutation $value)
    Write-Json $path $value
    Sync-BundleManifest $Bundle
}

function New-PendingAuthorFixture {
    param([Parameter(Mandatory)] [object]$Base,
        [Parameter(Mandatory)] [string]$Name,
        [scriptblock]$BundleMutation)
    $fixture = Copy-LifecycleFixture $Base $Name
    $bundle = New-TechnicalBundle $fixture $Name $BundleMutation
    Set-CloseoutState $fixture $bundle
    return [pscustomobject]@{ Fixture = $fixture; Bundle = $bundle }
}

$RunId = [guid]::NewGuid().ToString('N')
$EvidenceRoot = Join-Path $LogDirectory $RunId
$RunRoot = Join-Path ([IO.Path]::GetTempPath()) ("geocedg-lifecycle-$RunId")
if ((Test-Path -LiteralPath $EvidenceRoot) -or (Test-Path -LiteralPath $RunRoot)) {
    throw 'Lifecycle fixture root already exists.'
}
[void][IO.Directory]::CreateDirectory($EvidenceRoot)
[void][IO.Directory]::CreateDirectory($RunRoot)
Write-Host "Retained phase-lifecycle fixture root: $RunRoot"
$Results = [Collections.Generic.List[object]]::new()
$CaseNumber = 0

function Invoke-LifecycleCase {
    param([Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [scriptblock]$Action)
    $script:CaseNumber++
    try {
        & $Action
        $Results.Add([ordered]@{ name = $Name; status = 'PASS' })
        Write-Host "PASS $($script:CaseNumber): $Name"
    } catch {
        $Results.Add([ordered]@{ name = $Name; status = 'FAIL';
            message = $_.Exception.Message; stack = $_.ScriptStackTrace })
        Write-Host "FAIL $($script:CaseNumber): $Name — $($_.Exception.Message)"
        Write-Host $_.ScriptStackTrace
    }
}

function New-CaseFixture {
    param([Parameter(Mandatory)] [object]$Base,
        [Parameter(Mandatory)] [string]$Stem)
    return Copy-LifecycleFixture $Base (('{0:d2}-{1}' -f
            ($script:CaseNumber + 1), $Stem))
}

function Set-TrackedFileFromCommit {
    param([Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [string]$Path)
    $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $Fixture.Root $Commit $Path
    $target = Join-Path $Fixture.Root $Path
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $target))
    [IO.File]::WriteAllBytes($target, $bytes)
}

$Base = New-LifecycleFixture (Join-Path $RunRoot 'base')
$WideBase = New-LifecycleFixture (Join-Path $RunRoot 'wide-base') `
    -MaximumInfrastructureCommits 4

Invoke-LifecycleCase 'sealed scientific and composed verifier authority is unchanged' {
    Assert-R1ScientificSourcePreserved
    Assert-R1LifecycleGuardSource
}

Invoke-LifecycleCase 'historical top-level bytes remain accepted without G9U1 additions' {
    $sealed = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    Assert-TopLevelPreservedWithG9U1 -Current $sealed -Sealed $sealed
}

Invoke-LifecycleCase 'G9U1 integration cannot mutate historical test-filter forwarding' {
    $sealed = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    $current = [IO.File]::ReadAllText($RootVerifierPath)
    $mutated = $current.Replace('-TestFilter $TestFilter', '-TestFilter "different.scope"')
    Assert-Case ($mutated -cne $current) 'Historical filter mutation fixture did not change input'
    Assert-Throws { Assert-TopLevelPreservedWithG9U1 -Current $mutated -Sealed $sealed } `
        'outside the exact additive' 'Historical test-filter mutation'
}

Invoke-LifecycleCase 'G9U1 integration rejects an unrelated top-level addition' {
    $sealed = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    $current = [IO.File]::ReadAllText($RootVerifierPath) + "`nWrite-Host 'unapproved extra command'`n"
    Assert-Throws { Assert-TopLevelPreservedWithG9U1 -Current $current -Sealed $sealed } `
        'outside the exact additive' 'Unrelated top-level addition'
}

Invoke-LifecycleCase 'G9U1 integration rejects a changed phase acceptance gate' {
    $sealed = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    $current = [IO.File]::ReadAllText($RootVerifierPath)
    $mutated = $current.Replace('Assert-LastScriptSuccess -Description "G9U1 CeDG Construction workspace"',
        'Write-Host "G9U1 CeDG Construction workspace"')
    Assert-Case ($mutated -cne $current) 'G9U1 gate mutation fixture did not change input'
    Assert-Throws { Assert-TopLevelPreservedWithG9U1 -Current $mutated -Sealed $sealed } `
        'outside the exact additive' 'Changed G9U1 acceptance gate'
}

Invoke-LifecycleCase 'G9U1 integration rejects changed placement or declaration' {
    $sealed = Get-GitBlobText $RealRepository "$ImplementationCommit`:tools/agent/verify.ps1"
    $current = [IO.File]::ReadAllText($RootVerifierPath)
    $mutated = $current.Replace('    "verify-g9u1-construction-workspace.ps1"',
        '    "verify-other-workspace.ps1"')
    Assert-Case ($mutated -cne $current) 'G9U1 declaration fixture did not change input'
    Assert-Throws { Assert-TopLevelPreservedWithG9U1 -Current $mutated -Sealed $sealed } `
        'outside the exact additive' 'Changed G9U1 declaration'
}

Invoke-LifecycleCase 'commit index matches independent Git mode and blob lines' {
    $fixture = New-CaseFixture $Base 'index-reconstruction'
    Write-Text (Join-Path $fixture.Root 'docs/café point.txt') "UTF-8 path`n"
    [void](Invoke-Git $fixture.Root @('add', '--all'))
    [void](Invoke-Git $fixture.Root @('update-index', '--chmod=+x',
        'tools/infra-a.txt'))
    $commit = Commit-Fixture $fixture.Root 'Independent index oracle'
    $oracle = Get-FixtureIndexAuthority $fixture.Root $commit
    $blob = (@(Invoke-Git $fixture.Root @('rev-parse',
        "$commit`:tools/infra-a.txt")))[0]
    Assert-Case ($oracle.Lines -ccontains "100755 $blob 0`ttools/infra-a.txt") `
        'Executable mode and exact blob are absent from oracle'
    $actual = Get-GeoCeDGPhaseCommitIndexAuthority $fixture.Root $commit
    Assert-Case ($actual.IndexSha256 -ceq $oracle.IndexSha256 -and
        ($actual.Paths -join "`n") -ceq ($oracle.Paths -join "`n")) `
        'Reconstructed index differs from independent producer mode/blob lines'
}

Invoke-LifecycleCase 'historical 11201-entry technical index retains its recorded hash' {
    # Reuse immutable Git history, not a duplicated 11,201-line fixture.
    $actual = Get-GeoCeDGPhaseCommitIndexAuthority $RealRepository `
        '0d621a91696e3de530f4410d22932c4fd6759f3e'
    Assert-Case ($actual.Paths.Count -eq 11201 -and $actual.IndexSha256 -ceq
        'cd92f76f15479852d9b6512311dbd71fa3b0225a61901636d447a050ad41afc5') `
        'Historical technical index mode/blob reconstruction regressed'
}

Invoke-LifecycleCase 'receipt rejects malformed reordered mode blob and literal index tampering' {
    $oracle = Get-FixtureIndexAuthority $Base.Root $Base.TechnicalCommit
    foreach ($variant in @('malformed', 'reordered', 'mode', 'blob', 'literal')) {
        $lines = [string[]]$oracle.Lines.Clone()
        switch ($variant) {
            'malformed' { $lines[0] = 'not an index entry' }
            'reordered' { [array]::Reverse($lines) }
            'mode' { $lines[0] = $lines[0] -creplace '^100644 ', '100755 ' }
            'blob' { $lines[0] = $lines[0] -creplace '[0-9a-f]{40}', ('0' * 40) }
            'literal' {
                $lines = @($lines | ForEach-Object {
                    $_ -creplace '^[0-7]{6} [0-9a-f]{40}', '{0} {1}'
                })
            }
        }
        $forgedHash = Get-GeoCeDGPhaseLifecycleHash `
            ([Text.UTF8Encoding]::new($false).GetBytes($lines -join "`n"))
        Assert-Case ($forgedHash -cne $oracle.IndexSha256) `
            "Index tamper was not effective: $variant"
        $state = New-PendingAuthorFixture $Base "index-$variant" {
            param($documents)
            $receipt = $documents['composed-receipt.json']
            $receipt.inputIdentity.indexSha256 = $forgedHash
            Update-ReceiptFingerprint $receipt
        }
        Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
                -Bundle $state.Bundle -PendingCloseout } `
            'index does not represent the exact technical commit' `
            "Forged index authority: $variant"
    }
}

Invoke-LifecycleCase 'PRECOMMIT authenticates the entry and exact pending paths' {
    $fixture = New-CaseFixture $Base 'precommit-positive'
    $policyBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $fixture.Root `
        $fixture.TechnicalCommit $fixture.PolicyPath
    [void](Invoke-Git $fixture.Root @('checkout', '--quiet', $fixture.EntryCommit))
    $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson -Bytes $policyBytes `
        -Description 'fixture lifecycle policy'
    foreach ($record in @($policy.implementationPaths)) {
        Set-TrackedFileFromCommit $fixture $fixture.ImplementationCommit `
            ([string]$record.path)
    }
    $policyTarget = Join-Path $fixture.Root $fixture.PolicyPath
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $policyTarget))
    [IO.File]::WriteAllBytes($policyTarget, $policyBytes)
    $context = Invoke-FixtureContext $fixture PRECOMMIT
    $expected = @($policy.implementationPaths.path) + $fixture.PolicyPath
    Assert-GeoCeDGPhaseLifecycleSet @($context.CandidatePaths) $expected `
        'PRECOMMIT candidate paths'
    Assert-Case ($context.CurrentHead -ceq $fixture.EntryCommit -and
        -not $context.AuthorApprovedPhase -and -not $context.PassClaimed -and
        -not $context.SelfApproved -and -not $context.consumableBuildReceipt) `
        'PRECOMMIT exposed approval or receipt authority'
}

Invoke-LifecycleCase 'PRECOMMIT rejects any non-entry HEAD' {
    $fixture = New-CaseFixture $Base 'precommit-wrong-head'
    Assert-Throws { Invoke-FixtureContext $fixture PRECOMMIT } `
        'PRECOMMIT requires entry HEAD' 'Wrong PRECOMMIT HEAD'
}

Invoke-LifecycleCase 'COMMITTED_CANDIDATE is branch-name independent' {
    $fixture = New-CaseFixture $Base 'committed-branch-independent'
    [void](Invoke-Git $fixture.Root @('branch', '-m', 'renamed-arbitrary-branch'))
    $context = Invoke-FixtureContext $fixture COMMITTED_CANDIDATE
    Assert-Case ($context.CurrentHead -ceq $fixture.TechnicalCommit -and
        @($context.InfrastructurePaths).Count -eq 1 -and
        $context.InfrastructurePaths[0] -ceq $fixture.PolicyPath -and
        -not $context.AuthorApprovedPhase -and -not $context.PassClaimed) `
        'Renamed branch changed committed candidate authority'
    [void](Invoke-Git $fixture.Root @('checkout', '--quiet', '--detach'))
    $detached = Invoke-FixtureContext $fixture COMMITTED_CANDIDATE
    Assert-Case ($detached.CurrentHead -ceq $fixture.TechnicalCommit) `
        'Detached HEAD changed committed candidate authority'
}

Invoke-LifecycleCase 'COMMITTED_CANDIDATE rejects the wrong implementation SHA' {
    $fixture = New-CaseFixture $Base 'committed-wrong-t'
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE `
            -ExpectedImplementationCommit $fixture.EntryCommit } `
        'Unexpected implementation commit' 'Wrong implementation SHA'
}

Invoke-LifecycleCase 'implementation tree and blob seals reject policy tampering' {
    $treeFixture = New-CaseFixture $Base 'wrong-tree'
    Set-FixturePolicy $treeFixture { param($policy)
        $policy.implementationTree = '0' * 40
    }
    Assert-Throws { Invoke-FixtureContext $treeFixture COMMITTED_CANDIDATE } `
        'Implementation tree changed' 'Wrong implementation tree'
    $blobFixture = New-CaseFixture $Base 'wrong-blob'
    Set-FixturePolicy $blobFixture { param($policy)
        $policy.implementationPaths[0].sha256 = '0' * 64
    }
    Assert-Throws { Invoke-FixtureContext $blobFixture COMMITTED_CANDIDATE } `
        'Implementation blob hash changed' 'Wrong implementation blob'
}

Invoke-LifecycleCase 'implementation inventory rejects missing and unsafe paths' {
    $missing = New-CaseFixture $Base 'wrong-inventory'
    Set-FixturePolicy $missing { param($policy)
        $policy.implementationPaths = @($policy.implementationPaths |
            Select-Object -Skip 1)
    }
    Assert-Throws { Invoke-FixtureContext $missing COMMITTED_CANDIDATE } `
        'Implementation path authority mismatch' 'Missing implementation path'
    $unsafe = New-CaseFixture $Base 'unsafe-path'
    Set-FixturePolicy $unsafe { param($policy)
        $policy.infrastructureFollowupPaths[0] = '../escape'
    }
    Assert-Throws { Invoke-FixtureContext $unsafe COMMITTED_CANDIDATE } `
        'Unsafe infrastructure path' 'Unsafe lifecycle path'
}

Invoke-LifecycleCase 'COMMITTED_CANDIDATE requires implementation ancestry' {
    $fixture = New-CaseFixture $Base 'wrong-ancestry'
    $policyBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $fixture.Root `
        $fixture.TechnicalCommit $fixture.PolicyPath
    [void](Invoke-Git $fixture.Root @('checkout', '--quiet', $fixture.EntryCommit))
    $target = Join-Path $fixture.Root $fixture.PolicyPath
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $target))
    [IO.File]::WriteAllBytes($target, $policyBytes)
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'Implementation authority is not ancestral' 'Wrong candidate ancestry'
}

Invoke-LifecycleCase 'COMMITTED_CANDIDATE requires a clean tree and index' {
    $fixture = New-CaseFixture $Base 'dirty-candidate'
    Write-Text (Join-Path $fixture.Root 'tools/infra-a.txt') "dirty`n"
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'requires a clean tree and index' 'Dirty committed candidate'
}

$forbiddenDescendants = [ordered]@{
    product = 'source/product.java'
    test = 'source/ProductTest.java'
    verifier = 'tools/phase-verifier.ps1'
    tolerance = 'geocedg/tolerance.yml'
    unknown = 'unclassified/hidden.txt'
}
foreach ($entry in $forbiddenDescendants.GetEnumerator()) {
    Invoke-LifecycleCase ("infrastructure history rejects {0} descendant changes" -f
        $entry.Key) {
        $fixture = New-CaseFixture $WideBase ("forbidden-$($entry.Key)")
        Write-Text (Join-Path $fixture.Root $entry.Value) `
            "forbidden $($entry.Key) descendant`n"
        [void](Commit-Fixture $fixture.Root "Forbidden $($entry.Key) descendant")
        Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
            'changed a forbidden path' "Forbidden $($entry.Key) descendant"
    }
}

Invoke-LifecycleCase 'infrastructure history detects a productive edit hidden by revert' {
    $fixture = New-CaseFixture $WideBase 'hidden-edit-revert'
    $original = Get-GeoCeDGPhaseLifecycleBlobBytes $fixture.Root `
        $fixture.TechnicalCommit 'source/product.java'
    Write-Text (Join-Path $fixture.Root 'source/product.java') `
        "final class Product { int hidden = 99; }`n"
    [void](Commit-Fixture $fixture.Root 'Hidden productive edit')
    [IO.File]::WriteAllBytes((Join-Path $fixture.Root 'source/product.java'),
        $original)
    [void](Commit-Fixture $fixture.Root 'Revert hidden productive edit')
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'changed a forbidden path' 'Productive edit hidden by later revert'
}

Invoke-LifecycleCase 'infrastructure history rejects nonlinear merge ancestry' {
    $fixture = New-CaseFixture $WideBase 'nonlinear-history'
    $mainBranch = (@(Invoke-Git $fixture.Root @('branch', '--show-current')))[0]
    [void](Invoke-Git $fixture.Root @('checkout', '--quiet', '-b', 'side'))
    Write-Text (Join-Path $fixture.Root 'tools/infra-a.txt') "side`n"
    [void](Commit-Fixture $fixture.Root 'Side infrastructure')
    [void](Invoke-Git $fixture.Root @('checkout', '--quiet', $mainBranch))
    Write-Text (Join-Path $fixture.Root 'tools/infra-b.txt') "main`n"
    [void](Commit-Fixture $fixture.Root 'Main infrastructure')
    [void](Invoke-Git $fixture.Root @('-c', 'user.name=GeoCeDG lifecycle fixture',
        '-c', 'user.email=fixture@example.invalid', 'merge', '--quiet', '--no-ff',
        '-m', 'Nonlinear infrastructure merge', 'side'))
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'not a linear single-parent chain' 'Nonlinear infrastructure history'
}

Invoke-LifecycleCase 'infrastructure history enforces its exact commit bound' {
    $fixture = New-CaseFixture $Base 'too-many-infra'
    Write-Text (Join-Path $fixture.Root 'tools/infra-a.txt') "second infra`n"
    [void](Commit-Fixture $fixture.Root 'Second infrastructure follow-up')
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'Too many infrastructure follow-up commits' 'Infrastructure count bound'
}

Invoke-LifecycleCase 'two authorized infrastructure commits do not authorize a third' {
    $fixture = New-LifecycleFixture (Join-Path $RunRoot 'two-infra-bound') `
        -MaximumInfrastructureCommits 2
    Write-Text (Join-Path $fixture.Root 'tools/infra-a.txt') "bounded hotfix`n"
    $hotfix = Commit-Fixture $fixture.Root 'Second authorized infrastructure commit'
    $context = Invoke-FixtureContext $fixture COMMITTED_CANDIDATE
    Assert-Case ($context.CurrentHead -ceq $hotfix -and
        -not $context.AuthorApprovedPhase -and -not $context.PassClaimed) `
        'Bounded second infrastructure commit did not remain a candidate'
    Write-Text (Join-Path $fixture.Root 'tools/infra-b.txt') "unauthorized third`n"
    [void](Commit-Fixture $fixture.Root 'Unauthorized third infrastructure commit')
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'Too many infrastructure follow-up commits' 'Two-commit bound'
}

Invoke-LifecycleCase 'three authorized infrastructure commits do not authorize a fourth' {
    $fixture = New-LifecycleFixture (Join-Path $RunRoot 'three-infra-bound') `
        -MaximumInfrastructureCommits 3
    foreach ($number in @(2, 3)) {
        Write-Text (Join-Path $fixture.Root 'tools/infra-a.txt') "bounded hotfix $number`n"
        $hotfix = Commit-Fixture $fixture.Root "Authorized infrastructure commit $number"
    }
    $context = Invoke-FixtureContext $fixture COMMITTED_CANDIDATE
    Assert-Case ($context.CurrentHead -ceq $hotfix -and
        -not $context.AuthorApprovedPhase -and -not $context.PassClaimed) `
        'Bounded third infrastructure commit did not remain a technical candidate'
    Write-Text (Join-Path $fixture.Root 'tools/infra-b.txt') "unauthorized fourth`n"
    [void](Commit-Fixture $fixture.Root 'Unauthorized fourth infrastructure commit')
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'Too many infrastructure follow-up commits' 'Three-commit bound'
}

# Execute only the actual Git whitespace command ASTs in disposable repositories.
# This never invokes the real AUTHOR_CLOSEOUT or writes a real approval file.
$WhitespaceVerifiers = @(
    'verify-g9u0-r4-intersection-admissibility-continuation.ps1',
    'verify-g9u0-r5-locus-v2-similarity-transformations.ps1',
    'verify-g9s1-semantic-spline-2d-capability.ps1',
    'verify-g9u0-r6-semantic-locus-point-interaction-support.ps1',
    'verify-g9s1-r1-spline-pair-materialization.ps1'
)
foreach ($verifierName in $WhitespaceVerifiers) {
    Invoke-LifecycleCase "logical EOL and raw evidence remain distinct: $verifierName" {
        $sourcePath = Join-Path $RealRepository "tools/agent/$verifierName"
        $tokens = $null
        $errors = $null
        $ast = [Management.Automation.Language.Parser]::ParseFile(
            $sourcePath, [ref]$tokens, [ref]$errors)
        Assert-Case (@($errors).Count -eq 0) 'Whitespace verifier must parse'
        $commands = @($ast.FindAll({ param($node)
            $node -is [Management.Automation.Language.CommandAst] -and
            $node.GetCommandName() -ceq 'git' -and
            @($node.CommandElements | Where-Object { $_.Extent.Text -ceq 'diff' }).Count -eq 1 -and
            @($node.CommandElements | Where-Object { $_.Extent.Text -ceq '--check' }).Count -eq 1
        }, $true))
        Assert-Case ($commands.Count -eq 2) 'Expected exact unstaged and staged whitespace checks'
        $RepositoryRoot = Join-Path $RunRoot ('whitespace-' + $verifierName.Replace('.ps1', ''))
        [void][IO.Directory]::CreateDirectory($RepositoryRoot)
        [void](Invoke-Git $RepositoryRoot @('init', '--quiet'))
        [void](Invoke-Git $RepositoryRoot @('config', 'core.autocrlf', 'false'))
        $path = Join-Path $RepositoryRoot 'logical-lines.txt'
        Write-Text $path "before`n"
        $roadmapPath = 'docs/roadmap/geocedg_roadmap.md'
        $technical = '22f9ef4198e34ca79f542eb82a4f72b1f8e51e56'
        $originalRoadmap = Get-GeoCeDGPhaseLifecycleBlobBytes $RealRepository $technical $roadmapPath
        $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson `
            (Get-GeoCeDGPhaseLifecycleBlobBytes $RealRepository $technical `
                'geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json') 'frozen whitespace policy fixture'
        $projected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RealRepository $technical $policy
        $approvedRoadmap = [byte[]]$projected[$roadmapPath]
        Assert-Case ((Get-GeoCeDGPhaseLifecycleHash $approvedRoadmap) -ceq
            '04c5eaed53b437fa7fcc9dbb51a8ef1f392b7ec6999422dcff5564776fb8907f') `
            'Approved roadmap fixture bytes changed'
        $fixtureRoadmap = Join-Path $RepositoryRoot $roadmapPath
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent $fixtureRoadmap))
        [IO.File]::WriteAllBytes($fixtureRoadmap, $originalRoadmap)
        [void](Commit-Fixture $RepositoryRoot 'Whitespace fixture baseline')
        $cases = @(
            @{ Name = 'LF'; Text = "alpha`nbeta`n"; Pass = $true },
            @{ Name = 'CRLF'; Text = "alpha`r`nbeta`r`n"; Pass = $true },
            @{ Name = 'space-LF'; Text = "alpha `nbeta`n"; Pass = $false },
            @{ Name = 'tab-LF'; Text = "alpha`t`nbeta`n"; Pass = $false },
            @{ Name = 'space-CRLF'; Text = "alpha `r`nbeta`r`n"; Pass = $false },
            @{ Name = 'tab-CRLF'; Text = "alpha`t`r`nbeta`r`n"; Pass = $false },
            @{ Name = 'double-CR'; Text = "alpha`r`r`nbeta`n"; Pass = $false },
            @{ Name = 'approved-roadmap-CRLF'; Text = "before`n"; Pass = $true }
        )
        $lfHash = Get-GeoCeDGPhaseLifecycleHash ([Text.Encoding]::UTF8.GetBytes($cases[0].Text))
        $crlfHash = Get-GeoCeDGPhaseLifecycleHash ([Text.Encoding]::UTF8.GetBytes($cases[1].Text))
        Assert-Case ($lfHash -cne $crlfHash) 'Raw authority must distinguish LF from CRLF'
        foreach ($case in $cases) {
            foreach ($inheritedPolicy in @('blank-at-eol', '-blank-at-eol,-blank-at-eof,-space-before-tab')) {
                [void](Invoke-Git $RepositoryRoot @('config', 'core.whitespace', $inheritedPolicy))
                # read-tree resets only this disposable index, not the real repository.
                [void](Invoke-Git $RepositoryRoot @('read-tree', 'HEAD'))
                Write-Text $path $case.Text
                [IO.File]::WriteAllBytes($fixtureRoadmap, $(if ($case.Name -ceq 'approved-roadmap-CRLF') {
                    $approvedRoadmap
                } else { $originalRoadmap }))
                $beforeHash = Get-Sha256 $path
                $roadmapHash = Get-Sha256 $fixtureRoadmap
                foreach ($command in $commands) {
                    $staged = $command.Extent.Text.Contains('--cached')
                    if ($staged) { [void](Invoke-Git $RepositoryRoot @('add', '--all')) }
                    $output = @(. ([scriptblock]::Create($command.Extent.Text)) 2>&1 |
                        ForEach-Object { $_.ToString() })
                    $code = $LASTEXITCODE
                    Assert-Case (($code -eq 0) -eq $case.Pass) `
                        "$($case.Name), staged=$staged, inherited=${inheritedPolicy}: exit $code; $($output -join '; ')"
                    if (-not $case.Pass) {
                        Assert-Case (($output -join "`n") -match 'trailing whitespace') `
                            'Negative fixture did not fail for actual trailing whitespace'
                    }
                    Assert-Case ((Get-Sha256 $path) -ceq $beforeHash -and
                        (Get-Sha256 $fixtureRoadmap) -ceq $roadmapHash) `
                        'Whitespace predicate changed raw authority bytes'
                }
            }
        }
    }
}

Invoke-LifecycleCase 'lifecycle policy strict schema rejects added properties' {
    $fixture = New-CaseFixture $Base 'policy-extra-property'
    Set-FixturePolicy $fixture { param($policy)
        $policy | Add-Member -NotePropertyName unauthorized -NotePropertyValue $true
    }
    Assert-Throws { Invoke-FixtureContext $fixture COMMITTED_CANDIDATE } `
        'unsupported or missing property' 'Lifecycle-policy schema extension'
}

Invoke-LifecycleCase 'AUTHOR_CLOSEOUT accepts only exact pending status outputs' {
    $state = New-PendingAuthorFixture $Base 'author-pending-positive'
    $context = Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
        -Bundle $state.Bundle -PendingCloseout
    Assert-Case ($context.AuthorApprovedPhase -and $context.PassClaimed -and
        -not $context.SelfApproved -and $context.DocumentaryEvidenceLinked -and
        -not $context.consumableBuildReceipt -and
        -not ($context.PSObject.Properties.Name -ccontains 'EvidencePath')) `
        'Pending author closeout exposed the wrong authority'
}

Invoke-LifecycleCase 'AUTHOR_CLOSEOUT accepts an exact direct status-only commit' {
    $state = New-PendingAuthorFixture $Base 'author-committed-positive'
    $closeout = Commit-Fixture $state.Fixture.Root 'Synthetic author closeout'
    $context = Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
        -Bundle $state.Bundle
    Assert-Case ($context.CurrentHead -ceq $closeout -and
        $context.sourceAuthorityCommit -ceq $state.Fixture.TechnicalCommit -and
        $context.AuthorApprovedPhase -and -not $context.SelfApproved) `
        'Committed author closeout did not preserve reviewed technical authority'
}

Invoke-LifecycleCase 'AUTHOR_CLOSEOUT rejects absent or malformed approval records' {
    $absent = New-CaseFixture $Base 'author-record-absent'
    $bundle = New-TechnicalBundle $absent 'absent-bundle'
    Assert-Throws { Invoke-FixtureContext $absent AUTHOR_CLOSEOUT `
            -Bundle $bundle -PendingCloseout } `
        'Could not find file|does not exist|closeout record' 'Absent approval record'
    $state = New-PendingAuthorFixture $Base 'author-record-malformed'
    $path = Join-Path $state.Fixture.Root $state.Fixture.RecordPath
    $record = Read-Json $path
    $record.authorDecision = 'PENDING_AUTHOR_REVIEW'
    Write-Json $path $record
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle -PendingCloseout } `
        'Invalid author-closeout record' 'Premature approval record'
}

Invoke-LifecycleCase 'AUTHOR_CLOSEOUT rejects wrong reviewed SHA and ancestry' {
    $state = New-PendingAuthorFixture $Base 'author-wrong-reviewed'
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle -PendingCloseout `
            -ReviewedTechnicalCommit $state.Fixture.ImplementationCommit } `
        'Invalid author-closeout record|reviewedTechnicalCommit' `
        'Wrong reviewed technical SHA'
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle -PendingCloseout `
            -ReviewedTechnicalCommit $state.Fixture.EntryCommit } `
        'Implementation authority is not ancestral' 'Nontechnical reviewed ancestry'
}

Invoke-LifecycleCase 'AUTHOR_CLOSEOUT record requires strict Boolean and exact schema' {
    $typed = New-PendingAuthorFixture $Base 'author-record-typed'
    $typedPath = Join-Path $typed.Fixture.Root $typed.Fixture.RecordPath
    $record = Read-Json $typedPath
    $record.selfApproved = 'false'
    Write-Json $typedPath $record
    Assert-Throws { Invoke-FixtureContext $typed.Fixture AUTHOR_CLOSEOUT `
            -Bundle $typed.Bundle -PendingCloseout } `
        'Invalid author-closeout record' 'String self-approval value'
    $extra = New-PendingAuthorFixture $Base 'author-record-extra'
    $extraPath = Join-Path $extra.Fixture.Root $extra.Fixture.RecordPath
    $record = Read-Json $extraPath
    $record | Add-Member -NotePropertyName inferredApproval `
        -NotePropertyValue $true
    Write-Json $extraPath $record
    Assert-Throws { Invoke-FixtureContext $extra.Fixture AUTHOR_CLOSEOUT `
            -Bundle $extra.Bundle -PendingCloseout } `
        'unsupported or missing property' 'Extra author-closeout property'
}

Invoke-LifecycleCase 'pending closeout rejects altered status and hash-manifest content' {
    $status = New-PendingAuthorFixture $Base 'bad-status-content'
    Write-Text (Join-Path $status.Fixture.Root 'docs/report.md') `
        "G9S1-R1 = PASS — AUTHOR APPROVED but altered`n"
    Assert-Throws { Invoke-FixtureContext $status.Fixture AUTHOR_CLOSEOUT `
            -Bundle $status.Bundle -PendingCloseout } `
        'Pending closeout content mismatch' 'Altered closeout status'
    $manifest = New-PendingAuthorFixture $Base 'bad-derived-manifest'
    Write-Text (Join-Path $manifest.Fixture.Root `
            $manifest.Fixture.HashManifestPath) (('0' * 64) + "  wrong`n")
    Assert-Throws { Invoke-FixtureContext $manifest.Fixture AUTHOR_CLOSEOUT `
            -Bundle $manifest.Bundle -PendingCloseout } `
        'Pending closeout content mismatch' 'Altered derived hash manifest'
}

Invoke-LifecycleCase 'pending closeout rejects an unauthorized extra path' {
    $state = New-PendingAuthorFixture $Base 'author-extra-path'
    Write-Text (Join-Path $state.Fixture.Root 'unexpected-closeout.txt') `
        "unauthorized`n"
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle -PendingCloseout } `
        'Pending closeout paths mismatch' 'Unauthorized closeout path'
}

Invoke-LifecycleCase 'committed closeout rejects a second descendant commit' {
    $state = New-PendingAuthorFixture $Base 'author-not-direct-child'
    [void](Commit-Fixture $state.Fixture.Root 'Synthetic author closeout')
    [void](Invoke-Git $state.Fixture.Root @('-c',
        'user.name=GeoCeDG lifecycle fixture', '-c',
        'user.email=fixture@example.invalid', 'commit', '--quiet',
        '--allow-empty', '-m', 'Unauthorized post-closeout descendant'))
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle } `
        'one direct status-only child' 'Post-closeout descendant'
}

Invoke-LifecycleCase 'evidence link rejects PHASE A/B mismatch and premature approval' {
    $mismatch = New-PendingAuthorFixture $Base 'phase-mismatch' {
        param($documents)
        $documents['phase-b-summary.json'] | Add-Member -NotePropertyName diagnostic `
            -NotePropertyValue 'different-valid-summary-bytes'
    }
    Assert-Throws { Invoke-FixtureContext $mismatch.Fixture AUTHOR_CLOSEOUT `
            -Bundle $mismatch.Bundle -PendingCloseout } `
        'canonical summaries differ' 'PHASE A/B mismatch'
    $approved = New-PendingAuthorFixture $Base 'phase-premature-approval' {
        param($documents)
        $documents['phase-a-root.json'].authorApproved = $true
    }
    Assert-Throws { Invoke-FixtureContext $approved.Fixture AUTHOR_CLOSEOUT `
            -Bundle $approved.Bundle -PendingCloseout } `
        'Invalid PHASE final root' 'Premature technical-root approval'
}

Invoke-LifecycleCase 'evidence link rejects receipt HEAD, task and counter tampering' {
    $head = New-PendingAuthorFixture $Base 'receipt-head' {
        param($documents)
        $receipt = $documents['composed-receipt.json']
        $receipt.inputIdentity.head = '0' * 40
        Update-ReceiptFingerprint $receipt
    }
    Assert-Throws { Invoke-FixtureContext $head.Fixture AUTHOR_CLOSEOUT `
            -Bundle $head.Bundle -PendingCloseout } `
        'Invalid COMPOSED build receipt' 'Receipt HEAD tamper'
    $task = New-PendingAuthorFixture $Base 'receipt-task' {
        param($documents)
        $documents['composed-receipt.json'].tasks[0].outcome = 'UP-TO-DATE'
    }
    Assert-Throws { Invoke-FixtureContext $task.Fixture AUTHOR_CLOSEOUT `
            -Bundle $task.Bundle -PendingCloseout } `
        'Mandatory test task was not freshly executed' 'Receipt task tamper'
    $counter = New-PendingAuthorFixture $Base 'receipt-counter' {
        param($documents)
        $documents['composed-receipt.json'].tests = 3
    }
    Assert-Throws { Invoke-FixtureContext $counter.Fixture AUTHOR_CLOSEOUT `
            -Bundle $counter.Bundle -PendingCloseout } `
        'receipt counters disagree' 'Receipt counter tamper'
}

Invoke-LifecycleCase 'evidence link rejects bundle manifest authority tampering' {
    $state = New-PendingAuthorFixture $Base 'bundle-manifest-authority'
    Update-BundleManifest $state.Bundle { param($manifest)
        $manifest.technicalCommit = '0' * 40
    }
    Set-CloseoutState $state.Fixture $state.Bundle
    Assert-Throws { Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
            -Bundle $state.Bundle -PendingCloseout } `
        'Wrong evidence-bundle authority' 'Bundle technical authority tamper'
}

Invoke-LifecycleCase 'evidence link rejects manifest, archive and missing-file tampering' {
    $manifest = New-PendingAuthorFixture $Base 'bundle-manifest-hash'
    [IO.File]::AppendAllText((Join-Path $manifest.Bundle.Root `
            $manifest.Bundle.ManifestPath), " `n")
    Assert-Throws { Invoke-FixtureContext $manifest.Fixture AUTHOR_CLOSEOUT `
            -Bundle $manifest.Bundle -PendingCloseout } `
        'manifest hash mismatch' 'Bundle-manifest byte tamper'
    $archive = New-PendingAuthorFixture $Base 'bundle-archive-hash'
    [IO.File]::AppendAllText((Join-Path $archive.Bundle.Root `
            'artifact-composed-junit.xml'), 'tamper')
    Assert-Throws { Invoke-FixtureContext $archive.Fixture AUTHOR_CLOSEOUT `
            -Bundle $archive.Bundle -PendingCloseout } `
        'bundle file hash mismatch' 'Archived artifact byte tamper'
    $missing = New-PendingAuthorFixture $Base 'bundle-file-missing'
    Remove-Item -LiteralPath (Join-Path $missing.Bundle.Root `
        'artifact-composed-native.log') -Force
    Assert-Throws { Invoke-FixtureContext $missing.Fixture AUTHOR_CLOSEOUT `
            -Bundle $missing.Bundle -PendingCloseout } `
        'Missing evidence bundle file' 'Missing archived artifact'
}

Invoke-LifecycleCase 'evidence link rejects raw-input fingerprint and tree tampering' {
    $fingerprint = New-PendingAuthorFixture $Base 'input-fingerprint' {
        param($documents)
        $documents['composed-receipt.json'].inputFingerprint = '0' * 64
    }
    Assert-Throws { Invoke-FixtureContext $fingerprint.Fixture AUTHOR_CLOSEOUT `
            -Bundle $fingerprint.Bundle -PendingCloseout } `
        'input fingerprint is internally inconsistent' 'Input fingerprint tamper'
    $tree = New-PendingAuthorFixture $Base 'input-raw-tree' {
        param($documents)
        $receipt = $documents['composed-receipt.json']
        $receipt.inputIdentity.rawTreeSha256 = '0' * 64
        Update-ReceiptFingerprint $receipt
    }
    Assert-Throws { Invoke-FixtureContext $tree.Fixture AUTHOR_CLOSEOUT `
            -Bundle $tree.Bundle -PendingCloseout } `
        'Raw input inventory summary is inconsistent' 'Raw-tree identity tamper'
}

Invoke-LifecycleCase 'evidence link rejects clean-status and index identity tampering' {
    $status = New-PendingAuthorFixture $Base 'input-status' {
        param($documents)
        $receipt = $documents['composed-receipt.json']
        $receipt.inputIdentity.statusSha256 = '0' * 64
        Update-ReceiptFingerprint $receipt
    }
    Assert-Throws { Invoke-FixtureContext $status.Fixture AUTHOR_CLOSEOUT `
            -Bundle $status.Bundle -PendingCloseout } `
        'not produced from a clean status' 'Raw status identity tamper'
    $index = New-PendingAuthorFixture $Base 'input-index' {
        param($documents)
        $receipt = $documents['composed-receipt.json']
        $receipt.inputIdentity.indexSha256 = '0' * 64
        Update-ReceiptFingerprint $receipt
    }
    Assert-Throws { Invoke-FixtureContext $index.Fixture AUTHOR_CLOSEOUT `
            -Bundle $index.Bundle -PendingCloseout } `
        'index does not represent the exact technical commit' `
        'Technical index identity tamper'
}

Invoke-LifecycleCase 'evidence link rejects a mismatched bundle locator and hash' {
    $state = New-PendingAuthorFixture $Base 'bundle-locator'
    Assert-Throws {
        Get-GeoCeDGPhaseLifecycleContext -RepositoryRoot $state.Fixture.Root `
            -PolicyPath $state.Fixture.PolicyPath `
            -ExpectedImplementationCommit $state.Fixture.ImplementationCommit `
            -Mode AUTHOR_CLOSEOUT `
            -ReviewedTechnicalCommit $state.Fixture.TechnicalCommit `
            -CloseoutRecordPath $state.Fixture.RecordPath `
            -BundleDirectory (Join-Path $state.Fixture.Root 'artifacts/wrong') `
            -BundleSha256 $state.Bundle.ManifestSha256 -PendingCloseout
    } 'bundle directory differs' 'Mismatched bundle directory'
    Assert-Throws {
        Get-GeoCeDGPhaseLifecycleContext -RepositoryRoot $state.Fixture.Root `
            -PolicyPath $state.Fixture.PolicyPath `
            -ExpectedImplementationCommit $state.Fixture.ImplementationCommit `
            -Mode AUTHOR_CLOSEOUT `
            -ReviewedTechnicalCommit $state.Fixture.TechnicalCommit `
            -CloseoutRecordPath $state.Fixture.RecordPath `
            -BundleDirectory $state.Bundle.Root -BundleSha256 ('0' * 64) `
            -PendingCloseout
    } 'Invalid author-closeout record|manifest hash mismatch' `
        'Mismatched bundle hash'
}

Invoke-LifecycleCase 'documentary linkage cannot be consumed as active build evidence' {
    $state = New-PendingAuthorFixture $Base 'documentary-not-receipt'
    $context = Invoke-FixtureContext $state.Fixture AUTHOR_CLOSEOUT `
        -Bundle $state.Bundle -PendingCloseout
    Assert-Case (-not $context.consumableBuildReceipt -and
        -not ($context.PSObject.Properties.Name -ccontains 'ReceiptPath') -and
        -not ($context.PSObject.Properties.Name -ccontains 'EvidencePath')) `
        'Documentary linkage exposed active receipt material'
    $module = Import-Module (Join-Path $RealRepository `
        'tools/agent/verification-runtime.psm1') -Force -PassThru
    try {
        Assert-Throws {
            Confirm-GeoCeDGBuildEvidence `
                -EvidencePath (Join-Path $state.Bundle.Root `
                    $state.Bundle.ManifestPath) `
                -RepositoryRoot $state.Fixture.Root `
                -WorkingDirectory $state.Fixture.Root `
                -Arguments @(':shared:common-jre:test') `
                -LogPath (Join-Path $state.Fixture.Root 'consumer.log') `
                -Description 'Forbidden documentary receipt consumption'
        } 'not owned by a completed, active invocation' `
            'Documentary bundle as active receipt'
    } finally { Remove-Module $module -Force }
}

function Invoke-ExplicitTargetFixtureContext {
    param([Parameter(Mandatory)] [object]$State,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [string]$ReviewedCommit = $State.Fixture.TechnicalCommit)
    return Get-GeoCeDGPhaseAuthorCloseoutTargetContext `
        -RepositoryRoot $State.Fixture.Root `
        -ReviewedTechnicalCommit $ReviewedCommit -CloseoutCommit $CloseoutCommit `
        -PolicyPath $State.Fixture.PolicyPath `
        -BundleDirectory $State.Bundle.Root -BundleSha256 $State.Bundle.ManifestSha256
}

Invoke-LifecycleCase 'explicit target links reviewed T to exact clean closeout C' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-positive'
    $closeout = Commit-Fixture $state.Fixture.Root 'Explicit target closeout'
    $context = Invoke-ExplicitTargetFixtureContext $state $closeout
    Assert-Case ($context.reviewedTechnicalCommit -ceq $state.Fixture.TechnicalCommit -and
        $context.closeoutCommit -ceq $closeout -and $context.documentaryEvidenceLinked -and
        -not $context.technicalExecutionRepeated -and $context.authorApproved -and -not $context.selfApproved) `
        'Explicit target conflated technical execution, checkout or approval authority'
}

Invoke-LifecycleCase 'explicit target accepts different LF CRLF materialization of same tracked verifier' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-crlf'
    $closeout = Commit-Fixture $state.Fixture.Root 'Explicit target CRLF closeout'
    $lf = Invoke-ExplicitTargetFixtureContext $state $closeout
    $path = Join-Path $state.Fixture.Root 'tools/phase-verifier.ps1'
    $rawLf = Get-Sha256 $path
    [void](Invoke-Git $state.Fixture.Root @('config', 'core.autocrlf', 'true'))
    $text = [IO.File]::ReadAllText($path)
    Write-Text $path $text.Replace("`n", "`r`n")
    $rawCrlf = Get-Sha256 $path
    $crlf = Invoke-ExplicitTargetFixtureContext $state $closeout
    Assert-Case ($rawLf -cne $rawCrlf -and
        $lf.materialization.trackedSha256 -ceq $crlf.materialization.trackedSha256 -and
        $crlf.documentaryEvidenceLinked -and -not $crlf.technicalExecutionRepeated) `
        'Cross-checkout proof changed tracked identity or relabeled a historical execution'
}

Invoke-LifecycleCase 'explicit target rejects wrong reviewed technical SHA' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-wrong-reviewed'
    $closeout = Commit-Fixture $state.Fixture.Root 'Explicit target wrong T'
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $closeout `
            -ReviewedCommit $state.Fixture.ImplementationCommit } `
        'Git.*failed|does not exist|Invalid author-closeout record|Wrong evidence-bundle authority' `
        'Explicit target wrong reviewed commit'
}

Invoke-LifecycleCase 'explicit target rejects wrong closeout SHA independently of branch HEAD' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-wrong-closeout'
    [void](Commit-Fixture $state.Fixture.Root 'Explicit target wrong C')
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $state.Fixture.TechnicalCommit } `
        'Index differs from expected commit tree' 'Explicit target wrong closeout commit'
}

Invoke-LifecycleCase 'explicit target rejects product delta outside closeout allowlist' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-product-delta'
    Write-Text (Join-Path $state.Fixture.Root 'source/product.java') "final class Product { int value = 2; }`n"
    $closeout = Commit-Fixture $state.Fixture.Root 'Forbidden product delta'
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $closeout } `
        'exact closeout allowlist' 'Explicit target product mutation'
}

Invoke-LifecycleCase 'explicit target rejects verifier delta outside closeout allowlist' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-verifier-delta'
    Write-Text (Join-Path $state.Fixture.Root 'tools/phase-verifier.ps1') "# unauthorized verifier behavior`n"
    $closeout = Commit-Fixture $state.Fixture.Root 'Forbidden verifier delta'
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $closeout } `
        'exact closeout allowlist' 'Explicit target verifier mutation'
}

Invoke-LifecycleCase 'explicit target rejects tampered historical evidence manifest' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-tampered-evidence'
    $closeout = Commit-Fixture $state.Fixture.Root 'Explicit target evidence tamper'
    Write-Text (Join-Path $state.Bundle.Root $state.Bundle.ManifestPath) "{}`n"
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $closeout } `
        'Evidence-bundle manifest hash mismatch' 'Explicit target evidence tamper'
}

Invoke-LifecycleCase 'explicit target rejects missing author decision despite candidate execution success' {
    $state = New-PendingAuthorFixture $Base 'explicit-target-no-approval'
    $path = Join-Path $state.Fixture.Root $state.Fixture.RecordPath
    $record = Read-Json $path
    $record.authorDecision = 'PENDING_AUTHOR_REVIEW'
    Write-Json $path $record
    $closeout = Commit-Fixture $state.Fixture.Root 'Explicit target absent decision'
    Assert-Throws { Invoke-ExplicitTargetFixtureContext $state $closeout } `
        'Invalid author-closeout record' 'Explicit target technical success is not approval'
}

function New-PublishedRegressionFixture {
    param([Parameter(Mandatory)] [string]$Name)
    $state = New-PendingAuthorFixture $Base $Name
    $closeout = Commit-Fixture $state.Fixture.Root 'Synthetic published phase closeout'
    $authorityPath = 'geocedg/published-regression-authority.json'
    $authority = [ordered]@{
        schemaVersion = 1
        phase = 'G9S1-R1-FIXTURE'
        mode = 'PUBLISHED_REGRESSION'
        reviewedTechnicalCommit = $state.Fixture.TechnicalCommit
        closeoutCommit = $closeout
        policyPath = $state.Fixture.PolicyPath
        recordPath = $state.Fixture.RecordPath
        authorDecision = 'PASS_AUTHOR_APPROVED'
    }
    Write-Json (Join-Path $state.Fixture.Root $authorityPath) $authority
    $current = Commit-Fixture $state.Fixture.Root 'Synthetic operational successor'
    return [pscustomobject]@{ State = $state; CloseoutCommit = $closeout; CurrentCommit = $current
        AuthorityPath = $authorityPath; Authority = $authority }
}

function Invoke-PublishedRegressionFixtureContext {
    param([Parameter(Mandatory)] [object]$Published,
        [string]$ReviewedCommit = $Published.State.Fixture.TechnicalCommit,
        [string]$CloseoutCommit = $Published.CloseoutCommit)
    return Get-GeoCeDGPhasePublishedRegressionContext `
        -RepositoryRoot $Published.State.Fixture.Root -PublishedAuthorityPath $Published.AuthorityPath `
        -ExpectedReviewedTechnicalCommit $ReviewedCommit -ExpectedCloseoutCommit $CloseoutCommit `
        -ExpectedPolicyPath $Published.State.Fixture.PolicyPath
}

Invoke-LifecycleCase 'published regression authenticates old approval but requires live scientific execution' {
    $published = New-PublishedRegressionFixture 'published-live-positive'
    $context = Invoke-PublishedRegressionFixtureContext $published
    Assert-Case ($context.Mode -ceq 'PUBLISHED_REGRESSION' -and
        $context.sourceAuthorityCommit -ceq $published.CurrentCommit -and
        $context.reviewedTechnicalCommit -ceq $published.State.Fixture.TechnicalCommit -and
        $context.closeoutCommit -ceq $published.CloseoutCommit -and
        $context.historicalApprovalAuthenticated -and $context.liveScientificVerificationRequired -and
        -not $context.DocumentaryEvidenceLinked -and -not $context.consumableBuildReceipt -and
        -not $context.currentCohortEquivalentToHistoricalTechnicalExecution) `
        'Published regression incorrectly substituted historical tests for current scientific execution'
}

Invoke-LifecycleCase 'published regression rejects wrong exact reviewed or closeout SHA' {
    $published = New-PublishedRegressionFixture 'published-wrong-targets'
    Assert-Throws { Invoke-PublishedRegressionFixtureContext $published `
            -ReviewedCommit $published.State.Fixture.ImplementationCommit } `
        'differs from exact approved targets' 'Published wrong reviewed technical target'
    Assert-Throws { Invoke-PublishedRegressionFixtureContext $published `
            -CloseoutCommit $published.State.Fixture.TechnicalCommit } `
        'differs from exact approved targets' 'Published wrong closeout target'
}

Invoke-LifecycleCase 'published regression rejects premature approval indicators' {
    $published = New-PublishedRegressionFixture 'published-premature-approval'
    $published.Authority.authorDecision = 'PENDING_AUTHOR_REVIEW'
    Write-Json (Join-Path $published.State.Fixture.Root $published.AuthorityPath) $published.Authority
    Assert-Throws { Invoke-PublishedRegressionFixtureContext $published } `
        'differs from exact approved targets' 'Published premature author approval'
}

Invoke-LifecycleCase 'published regression requires closeout ancestry of current execution' {
    $published = New-PublishedRegressionFixture 'published-no-ancestry'
    [void](Invoke-Git $published.State.Fixture.Root @('checkout', '--detach', '--quiet',
        $published.State.Fixture.TechnicalCommit))
    Write-Json (Join-Path $published.State.Fixture.Root $published.AuthorityPath) $published.Authority
    Assert-Throws { Invoke-PublishedRegressionFixtureContext $published } `
        'not ancestral to current execution' 'Published missing closeout ancestry'
}

$summary = [ordered]@{
    schemaVersion = 1
    evidenceKind = 'FAKE_FIRST_PHASE_LIFECYCLE_TESTS_NOT_BUILD_OR_AUTHOR_EVIDENCE'
    helperPath = $HelperPath
    helperSha256 = Get-Sha256 $HelperPath
    implementationCommit = $ImplementationCommit
    fixtureRoot = $RunRoot
    tests = $Results.Count
    passed = @($Results | Where-Object status -ceq 'PASS').Count
    failed = @($Results | Where-Object status -ceq 'FAIL').Count
    gradleExecutions = 0
    authorApproved = $false
    selfApproved = $false
    results = @($Results)
}
$summaryPath = Join-Path $EvidenceRoot 'phase-lifecycle-tests.json'
Write-Json $summaryPath $summary
$canonicalSummary = [ordered]@{
    schemaVersion = 1
    evidenceKind = 'DETERMINISTIC_LIFECYCLE_INFRASTRUCTURE_FIXTURES'
    tests = $summary.tests
    passed = $summary.passed
    failed = $summary.failed
    gradleExecutions = 0
    authorApproved = $false
    selfApproved = $false
    results = @($Results | ForEach-Object { [ordered]@{ name = $_.name; status = $_.status } })
}
$canonicalSummaryPath = Join-Path $EvidenceRoot 'canonical-summary.json'
Write-Json $canonicalSummaryPath $canonicalSummary
$canonicalSummaryHash = Get-CanonicalLfSha256 $canonicalSummaryPath
Write-Text (Join-Path $EvidenceRoot 'canonical-summary.sha256') ($canonicalSummaryHash + "`n")
Write-Host "Phase lifecycle fake-first tests: $($summary.passed)/$($summary.tests) passed."
Write-Host "Deterministic lifecycle fixture SHA-256: $canonicalSummaryHash"
Write-Host "Saved lifecycle test evidence: $summaryPath"
Write-Host "Retained lifecycle fixtures: $RunRoot"
if ($summary.failed -ne 0) { exit 1 }
exit 0

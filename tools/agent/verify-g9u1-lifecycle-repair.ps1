#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$CandidateCommit,
    [Parameter(Mandatory)] [string]$LogDirectory,
    [string]$LifecyclePolicyPath = (Join-Path $PSScriptRoot `
        '../../geocedg/validation/operations/g9u1-lifecycle-policy.json'),
    [string]$RepairPolicyPath = (Join-Path $PSScriptRoot `
        '../../geocedg/validation/operations/g9u1-lifecycle-repair-policy.json')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$LifecyclePolicyPath = [IO.Path]::GetFullPath($LifecyclePolicyPath)
$RepairPolicyPath = [IO.Path]::GetFullPath($RepairPolicyPath)
$ProductCheckpoint = '28f7843184cfb202bbfcca1cbcc56a25a7a77bca'
$ProductTree = 'd08d7beb45d04d6e0f0a478f4c04eb0e97e7e667'
$HistoricalHead = 'e4ef3d48ea95a0c3243e57dfc703b539d455c33e'
$HistoricalIndexSha256 = '9a93fb584699f85de0d30c0d4835c0cb17090b827d5b392aba360900f71c0dfe'
$HistoricalStatusSha256 = 'e1872ce83ce416f4d0ec71f7de299321b170f616b3b1541034c03db6b59417c3'
$HistoricalRawTreeSha256 = '556982f1572e6c98289a7130fb4a746a2ff3da4dedecfed7d3134ce4da0ee557'
$HistoricalInputInventorySha256 = '6841a6adcfe7317d772ad53461216cd73fe629a29735cf72e98beff2d45ec3c4'
$HistoricalInputFingerprint = '1091b2786245cd119b01e344eb7d480a4cda89b2411e1fab2c0b10135ec4c9b8'
$ExpectedMain = 'f8a21a087234b18fc13741a0ac2baf80608e9022'
$R1Reviewed = 'a38d4fcde846fc97c51abc8d958de6998302c436'
$R1Closeout = 'af459d856f1cdc384805f3035203acce8e6f6104'

. (Join-Path $PSScriptRoot 'phase-lifecycle.ps1')
. (Join-Path $PSScriptRoot 'verification-repair-equivalence.ps1')
. (Join-Path $PSScriptRoot 'repository-generated-state.ps1')

function Assert-G9U1Repair {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Get-G9U1RepairSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Write-G9U1RepairJson {
    param([Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Value)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    $text = ($Value | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n"
    [IO.File]::WriteAllText($Path, $text, [Text.UTF8Encoding]::new($false))
}

function Invoke-G9U1RepairGit {
    param([Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments)
    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $Root -Arguments $Arguments -AllowFailure
    if ($result.ExitCode -ne 0) {
        $errorText = [string]$result.StandardError
        throw "git $($Arguments -join ' ') failed: $errorText"
    }
    if (@($result.Bytes).Count -eq 0) { return '' }
    return ConvertFrom-GeoCeDGStrictUtf8 $result.Bytes
}

function Invoke-G9U1RepairProcess {
    param([Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$FileName,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$WorkingDirectory)
    $start = [Diagnostics.ProcessStartInfo]::new($FileName)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false, $true)
    $start.StandardErrorEncoding = [Text.UTF8Encoding]::new($false, $true)
    $start.WorkingDirectory = $WorkingDirectory
    foreach ($argument in $Arguments) { $start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        [void]$process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $text = $stdout.GetAwaiter().GetResult() + $stderr.GetAwaiter().GetResult()
        [IO.File]::WriteAllText((Join-Path $LogDirectory "$Name.log"), $text,
            [Text.UTF8Encoding]::new($false))
        Assert-G9U1Repair ($process.ExitCode -eq 0) `
            "$Name failed with exit $($process.ExitCode); see $Name.log."
        return [pscustomobject]@{ Name = $Name; ExitCode = $process.ExitCode; Output = $text }
    } finally { $process.Dispose() }
}

function Invoke-G9U1RepairPowerShell {
    param([string]$Name, [string]$Script, [string[]]$Arguments)
    return Invoke-G9U1RepairProcess $Name (Join-Path $PSHOME 'pwsh.exe') `
        (@('-NoProfile', '-File', [IO.Path]::GetFullPath($Script)) + $Arguments) $RepositoryRoot
}

function Assert-G9U1HistoricalEvidence {
    $paths = [ordered]@{
        focusedA = 'artifacts/g9u1-final-toolbar-normalization/focused-a/canonical-summary.json'
        focusedB = 'artifacts/g9u1-final-toolbar-normalization/focused-b/canonical-summary.json'
        phaseRoot = 'artifacts/g9u1-final-toolbar-normalization/phase/verification-result.json'
        phaseSummary = 'artifacts/g9u1-final-toolbar-normalization/phase/phase-g9u1/canonical-summary.json'
        composedRoot = 'artifacts/g9u1-final-toolbar-normalization/composed/verification-result.json'
        composedReceipt = 'artifacts/g9u1-final-toolbar-normalization/composed/canonical-build/f35b45b6e386475e8095b10d8aa04b97/build-evidence.json'
        fullRoot = 'artifacts/g9u1-final-toolbar-normalization/full/verification-result.json'
        fullReceipt = 'artifacts/g9u1-final-toolbar-normalization/full/canonical-build/b68cfd83a91e4f6ca4aad96529d2864c/build-evidence.json'
        checkpoint = 'artifacts/g9u1-lifecycle-repair/checkpoint-28f/checkpoint-authority.json'
        checkpointInventory = 'artifacts/g9u1-lifecycle-repair/checkpoint-28f/input-inventory.json'
        materialization = 'artifacts/g9u1-lifecycle-repair/checkpoint-28f/materialization-proof.json'
    }
    $expectedHashes = [ordered]@{
        focusedA = 'a20509ffda779665d6a60cfa041b1fe6568ef70d120e8faf6b79872d84b685bf'
        focusedB = 'a20509ffda779665d6a60cfa041b1fe6568ef70d120e8faf6b79872d84b685bf'
        phaseRoot = 'b3959bc4da983349886c34ad8953af3e80f4dbd6124b3aab0167d72ca01cc4c0'
        phaseSummary = 'a20509ffda779665d6a60cfa041b1fe6568ef70d120e8faf6b79872d84b685bf'
        composedRoot = 'ffc18e155f1e0ba8d74bdb01d64eff04c453eb1249bf6ab0d06b8d8e06c8c856'
        composedReceipt = '71233ba374607240230fa97e045a2cf7e3aa92be6872b04749ca2fc6ad0fde00'
        fullRoot = '4c5c4001d0bf56028e2f2ae0025df7fc1bdaeba638412874d4a4773a083fe5d6'
        fullReceipt = '838a011c4b8eb627b0f6ebe63d60e7174dd56ee630c878990fa0fc719a20d3b8'
        checkpoint = '57b08b28ba85b4d2deecfaf7ea808a827da53151f6c7924848f80f50faf07143'
        checkpointInventory = '979129d3ff8bf78a483b64898a272b1d0fdba5f08e440ef091a572c42394e963'
        materialization = '997b2633c9c466c71c6b7d641e579dc8bb9c4e3c36f938748f21ecee5d242bd2'
    }
    foreach ($name in $paths.Keys) {
        $absolute = Join-Path $RepositoryRoot $paths[$name]
        Assert-G9U1Repair (Test-Path -LiteralPath $absolute -PathType Leaf) `
            "Missing historical evidence: $($paths[$name])"
        if ($expectedHashes.Contains($name)) {
            Assert-G9U1Repair ((Get-G9U1RepairSha256 $absolute) -ceq $expectedHashes[$name]) `
                "Historical evidence hash differs: $($paths[$name])"
        }
    }
    $phase = Get-Content -Raw (Join-Path $RepositoryRoot $paths.phaseRoot) | ConvertFrom-Json -Depth 100
    Assert-G9U1Repair ($phase.repositoryCommit -ceq $HistoricalHead -and
        $phase.level -ceq 'PHASE' -and $phase.phase -ceq 'G9U1' -and
        [int]$phase.exitCode -eq 0 -and $null -eq $phase.failure) `
        'Historical G9U1 PHASE root is not the accepted precommit execution.'
    $receipts = @(
        Get-Content -Raw (Join-Path $RepositoryRoot $paths.composedReceipt) | ConvertFrom-Json -Depth 100
        Get-Content -Raw (Join-Path $RepositoryRoot $paths.fullReceipt) | ConvertFrom-Json -Depth 100
    )
    foreach ($receipt in $receipts) {
        Assert-G9U1Repair ($receipt.inputIdentity.head -ceq $HistoricalHead -and
            $receipt.inputIdentity.indexSha256 -ceq $HistoricalIndexSha256 -and
            $receipt.inputIdentity.statusSha256 -ceq $HistoricalStatusSha256 -and
            $receipt.inputIdentity.rawTreeSha256 -ceq $HistoricalRawTreeSha256 -and
            [int]$receipt.inputIdentity.rawFiles -eq 11299 -and
            [long]$receipt.inputIdentity.rawBytes -eq 187439751 -and
            $receipt.inputFingerprint -ceq $HistoricalInputFingerprint -and
            -not $receipt.authorApproved -and -not $receipt.selfApproved) `
            'Historical receipt provenance or false-approval state differs.'
    }
    Assert-G9U1Repair ([int]$receipts[0].tests -eq 1515 -and
        [int]$receipts[0].skippedUpstreamTests -eq 0 -and
        [int]$receipts[1].tests -eq 8015 -and
        [int]$receipts[1].skippedUpstreamTests -eq 11) `
        'Historical COMPOSED/FULL counters differ.'
    $checkpoint = Get-Content -Raw (Join-Path $RepositoryRoot $paths.checkpoint) | ConvertFrom-Json -Depth 100
    $materialization = Get-Content -Raw (Join-Path $RepositoryRoot $paths.materialization) | ConvertFrom-Json -Depth 100
    Assert-G9U1Repair ($checkpoint.productCheckpoint -ceq $ProductCheckpoint -and
        $checkpoint.productTree -ceq $ProductTree -and
        $checkpoint.historicalExecutionHead -ceq $HistoricalHead -and
        $checkpoint.rawTreeSha256 -ceq $HistoricalRawTreeSha256 -and
        [int]$checkpoint.rawFiles -eq 11299 -and [long]$checkpoint.rawBytes -eq 187439751 -and
        $materialization.expectedCommit -ceq $ProductCheckpoint -and
        $materialization.treeOid -ceq $ProductTree -and
        $materialization.indexMatchesCommitTreeOutsideStatusOverlay -and
        $materialization.trackedWorktreeCleanOutsideStatusOverlay -and
        -not $materialization.physicalByteEqualityRequiredAcrossCheckout) `
        'Checkpoint byte/materialization bridge differs.'
    $historicalInventoryPath = Join-Path $RepositoryRoot `
        'artifacts/g9u1-final-toolbar-normalization/composed/canonical-build/f35b45b6e386475e8095b10d8aa04b97/input-inventory.json'
    Assert-G9U1Repair ((Get-G9U1RepairSha256 $historicalInventoryPath) -ceq
        $HistoricalInputInventorySha256) 'Historical input inventory hash differs.'
    $historical = @(Get-Content -Raw $historicalInventoryPath | ConvertFrom-Json -Depth 20)
    $frozen = @(Get-Content -Raw (Join-Path $RepositoryRoot $paths.checkpointInventory) |
        ConvertFrom-Json -Depth 20)
    Assert-G9U1Repair ($historical.Count -eq 11299 -and $frozen.Count -eq $historical.Count) `
        'Frozen/current input inventory cardinality differs.'
    for ($index = 0; $index -lt $historical.Count; $index++) {
        Assert-G9U1Repair ($historical[$index].path -ceq $frozen[$index].path -and
            [bool]$historical[$index].exists -eq [bool]$frozen[$index].exists -and
            [long]$historical[$index].bytes -eq [long]$frozen[$index].bytes -and
            $historical[$index].sha256 -ceq $frozen[$index].sha256) `
            "Checkpoint raw byte bridge differs at input $index."
    }
    return [pscustomobject]@{ Paths = $paths; Receipts = $receipts }
}

function New-G9U1BundleEntry {
    param([string]$Role, [string]$SourcePath, [string]$RecordedPath,
        [string]$BundleRoot, [int]$Number)
    $leaf = [IO.Path]::GetFileName($SourcePath) -replace '[^A-Za-z0-9._-]', '_'
    $relative = 'files/{0:D4}-{1}' -f $Number, $leaf
    $destination = Join-Path $BundleRoot $relative
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $destination))
    Copy-Item -LiteralPath $SourcePath -Destination $destination
    return [ordered]@{ role = $Role; path = $relative.Replace('\', '/');
        recordedPath = $RecordedPath; sha256 = Get-G9U1RepairSha256 $destination }
}

Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $RepositoryRoot `
    -LogDirectory $LogDirectory
Assert-G9U1Repair (-not (Test-Path -LiteralPath $LogDirectory)) `
    'Use a fresh G9U1 lifecycle-repair evidence directory.'
[void][IO.Directory]::CreateDirectory($LogDirectory)
Assert-G9U1Repair ($CandidateCommit -cmatch '^[0-9a-f]{40}$') `
    'CandidateCommit must be an exact lowercase SHA.'
$head = (Invoke-G9U1RepairGit $RepositoryRoot @('rev-parse', 'HEAD')).Trim()
$tree = (Invoke-G9U1RepairGit $RepositoryRoot @('rev-parse', 'HEAD^{tree}')).Trim()
$status = Invoke-G9U1RepairGit $RepositoryRoot @('status', '--porcelain=v1', '--untracked-files=all')
Assert-G9U1Repair ($head -ceq $CandidateCommit -and [string]::IsNullOrEmpty($status)) `
    'Final lifecycle-repair validation requires exact clean CandidateCommit HEAD.'
Assert-G9U1Repair ((Invoke-G9U1RepairGit $RepositoryRoot @('rev-parse', 'main')).Trim() -ceq $ExpectedMain -and
    (Invoke-G9U1RepairGit $RepositoryRoot @('rev-parse', 'origin/main')).Trim() -ceq $ExpectedMain) `
    'G9U1 lifecycle repair must not move main.'
$remoteMain = (Invoke-G9U1RepairGit $RepositoryRoot @('ls-remote', '--refs', 'origin',
        'refs/heads/main')).Trim()
Assert-G9U1Repair ($remoteMain -cmatch '^([0-9a-f]{40})\trefs/heads/main$' -and
    $Matches[1] -ceq $ExpectedMain) 'Live remote main differs from the frozen product authority.'
Assert-G9U1Repair ([string]::IsNullOrEmpty((Invoke-G9U1RepairGit $RepositoryRoot `
    @('tag', '--list', 'geocedg-g9u1-pass')).Trim())) 'G9U1 PASS tag exists before author closeout.'
$remoteTag = Invoke-G9U1RepairGit $RepositoryRoot @('ls-remote', '--tags', 'origin',
    'refs/tags/geocedg-g9u1-pass', 'refs/tags/geocedg-g9u1-pass^{}')
Assert-G9U1Repair ([string]::IsNullOrEmpty($remoteTag.Trim())) `
    'Live remote G9U1 PASS tag exists before author closeout.'

$lifecycle = Read-GeoCeDGPhaseLifecyclePolicy $RepositoryRoot $LifecyclePolicyPath
Assert-G9U1Repair ($lifecycle.phase -ceq 'G9U1' -and
    $lifecycle.implementationCommit -ceq $ProductCheckpoint -and
    $lifecycle.implementationTree -ceq $ProductTree) 'Wrong G9U1 lifecycle policy.'
Assert-GeoCeDGPhaseImplementationAuthority $RepositoryRoot $lifecycle
Assert-GeoCeDGPhaseInfrastructureHistory $RepositoryRoot $lifecycle $CandidateCommit
$repair = Get-Content -Raw $RepairPolicyPath | ConvertFrom-Json -Depth 100
Assert-G9U1Repair ($repair.schemaVersion -eq 1 -and
    $repair.kind -ceq 'BOUNDED_INPUT_IDENTITY_REPAIR_POLICY' -and
    $repair.reviewedTechnicalCommit -ceq $ProductCheckpoint -and
    $repair.closeoutCommit -ceq $ProductCheckpoint) 'Wrong G9U1 repair policy.'

$historical = Assert-G9U1HistoricalEvidence

$identityHashes = [Collections.Generic.List[string]]::new()
$lifecycleHashes = [Collections.Generic.List[string]]::new()
foreach ($run in @('a', 'b')) {
    $identityDirectory = Join-Path $LogDirectory "focused-$run/input-identity"
    [void](Invoke-G9U1RepairPowerShell "focused-$run-input-identity" `
        (Join-Path $PSScriptRoot 'verify-input-identity-repair.ps1') @(
            '-LogDirectory', $identityDirectory,
            '-PolicyPath', $RepairPolicyPath,
            '-CandidateCommit', $CandidateCommit))
    $identityHashes.Add((Get-G9U1RepairSha256 (Join-Path $identityDirectory 'canonical-summary.json')))
    $lifecycleDirectory = Join-Path $LogDirectory "focused-$run/g9u1-lifecycle"
    [void](Invoke-G9U1RepairPowerShell "focused-$run-g9u1-lifecycle" `
        (Join-Path $PSScriptRoot 'tests/g9u1-lifecycle.Tests.ps1') @(
            '-LogDirectory', $lifecycleDirectory,
            '-PolicyPath', $LifecyclePolicyPath))
    $lifecycleHashes.Add((Get-G9U1RepairSha256 (Join-Path $lifecycleDirectory 'canonical-summary.json')))
}
Assert-G9U1Repair ($identityHashes[0] -ceq $identityHashes[1] -and
    $lifecycleHashes[0] -ceq $lifecycleHashes[1]) `
    'Focused lifecycle/input-identity A/B summaries are not deterministic.'
$staticDirectory = Join-Path $LogDirectory 'static-g9u1-committed'
[void](Invoke-G9U1RepairPowerShell 'static-g9u1-committed' `
    (Join-Path $PSScriptRoot 'verify-g9u1-construction-workspace.ps1') @(
        '-SkipBuild', '-HistoricalRegressionsAlreadyComposed',
        '-LifecycleMode', 'COMMITTED_CANDIDATE', '-LogDirectory', $staticDirectory))
$staticLogPath = Join-Path $LogDirectory 'static-g9u1-committed.log'
Assert-G9U1Repair (Test-Path -LiteralPath $staticLogPath -PathType Leaf) `
    'Committed-candidate static verifier did not emit its process log.'
$staticRecordPath = Join-Path $LogDirectory 'parser-static-integration.json'

$sharedDirectory = Join-Path $LogDirectory 'shared-live-integration'
[void](Invoke-G9U1RepairPowerShell 'shared-live-integration' (Join-Path $PSScriptRoot 'verify.ps1') @(
        '-Level', 'DEV', '-Module', 'shared', '-TestFilter',
        'org.geocedg.common.workspace.G9U1InputPolicyTest.continuityCannotBeEnabledInProduct',
        '-KeepBuildOutputs', '-LogDirectory', $sharedDirectory))
$sharedRoot = Get-Content -Raw (Join-Path $sharedDirectory 'verification-result.json') | ConvertFrom-Json -Depth 30
Assert-G9U1Repair ($sharedRoot.level -ceq 'DEV' -and [int]$sharedRoot.exitCode -eq 0 -and
    $sharedRoot.repositoryCommit -ceq $CandidateCommit) 'Bounded shared live integration failed.'
$sharedRootPath = Join-Path $sharedDirectory 'verification-result.json'
$sharedRecordPath = [IO.Path]::GetFullPath([string]$sharedRoot.devEvidence)
Assert-G9U1Repair (Test-Path -LiteralPath $sharedRecordPath -PathType Leaf) `
    'Bounded shared live integration did not emit its DEV evidence.'

$desktopDirectory = Join-Path $LogDirectory 'desktop-live-integration'
[void](Invoke-G9U1RepairPowerShell 'desktop-live-integration' (Join-Path $PSScriptRoot 'verify.ps1') @(
        '-Level', 'DEV', '-Module', 'desktop', '-TestFilter',
        'org.geocedg.desktop.G9U1ProfileCompilerTest.liveProfileStrictlyCompilesOneHundredTenActions',
        '-KeepBuildOutputs', '-LogDirectory', $desktopDirectory))
$desktopRoot = Get-Content -Raw (Join-Path $desktopDirectory 'verification-result.json') | ConvertFrom-Json -Depth 30
Assert-G9U1Repair ($desktopRoot.level -ceq 'DEV' -and [int]$desktopRoot.exitCode -eq 0 -and
    $desktopRoot.repositoryCommit -ceq $CandidateCommit) 'Bounded Desktop live integration failed.'
$desktopRootPath = Join-Path $desktopDirectory 'verification-result.json'
$desktopRecordPath = [IO.Path]::GetFullPath([string]$desktopRoot.devEvidence)
Assert-G9U1Repair (Test-Path -LiteralPath $desktopRecordPath -PathType Leaf) `
    'Bounded Desktop live integration did not emit its DEV evidence.'

$r1Clone = Join-Path $LogDirectory 'r1-crlf-target'
[void](Invoke-G9U1RepairGit $RepositoryRoot @('clone', '--shared', '--no-checkout', '--quiet',
        $RepositoryRoot, $r1Clone))
[void](Invoke-G9U1RepairGit $r1Clone @('config', 'core.autocrlf', 'true'))
[void](Invoke-G9U1RepairGit $r1Clone @('checkout', '--detach', '--quiet', $R1Closeout))
$r1BundleRelative = 'artifacts/g9s1-r1-whitespace-hotfix'
$r1BundleTarget = Join-Path $r1Clone $r1BundleRelative
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $r1BundleTarget))
Copy-Item -LiteralPath (Join-Path $RepositoryRoot $r1BundleRelative) `
    -Destination $r1BundleTarget -Recurse
$r1Output = Join-Path $LogDirectory 'r1-cross-checkout-result'
[void](Invoke-G9U1RepairPowerShell 'r1-cross-checkout' `
    (Join-Path $PSScriptRoot 'verify-phase-author-closeout.ps1') @(
        '-TargetRepositoryRoot', $r1Clone,
        '-ReviewedTechnicalCommit', $R1Reviewed,
        '-CloseoutCommit', $R1Closeout,
        '-PolicyPath', 'geocedg/validation/g9s1-r1/g9s1-r1-lifecycle-policy.json',
        '-TechnicalEvidenceBundleDirectory', $r1BundleTarget,
        '-TechnicalEvidenceBundleSha256', '0757b2d52d3aca85f85961c48dce3b90992efed589df387d41aabc2938585418',
        '-LogDirectory', $r1Output))
$r1Result = Get-Content -Raw (Join-Path $r1Output 'author-closeout-result.json') | ConvertFrom-Json -Depth 100
Assert-G9U1Repair ($r1Result.state -ceq
    'AUTHOR_CLOSEOUT_GIT_IDENTITY_PASSED_NOT_NEW_TECHNICAL_EXECUTION' -and
    $r1Result.reviewedTechnicalCommit -ceq $R1Reviewed -and
    $r1Result.closeoutCommit -ceq $R1Closeout -and
    -not $r1Result.productRuntimeExecuted -and -not $r1Result.currentRunReceiptProduced) `
    'Actual R1 CRLF cross-checkout regression failed or relabeled execution.'
foreach ($source in @($r1Result.verificationCodeSources)) {
    $sourcePath = Join-Path $RepositoryRoot ([string]$source.path)
    Assert-G9U1Repair ((Test-Path -LiteralPath $sourcePath -PathType Leaf) -and
        (Get-G9U1RepairSha256 $sourcePath) -ceq [string]$source.sha256) `
        "R1 closeout same-run verification-code bytes changed: $($source.path)"
}
$r1RecordPath = Join-Path $r1Output 'author-closeout-result.json'

$comparison = Get-GeoCeDGVerificationRepairEquivalence -RepositoryRoot $RepositoryRoot `
    -ReviewedTechnicalCommit $ProductCheckpoint -CloseoutCommit $ProductCheckpoint `
    -CandidateCommit $CandidateCommit -CloseoutPaths @() -RepairPaths @($repair.repairPaths)
$structuralPath = Join-Path $LogDirectory 'execution-plan-proof.json'
Write-G9U1RepairJson $structuralPath $comparison
Assert-G9U1Repair ($comparison.executionPlanEquivalent -and
    $comparison.reviewedExecutionPlanSha256 -ceq $comparison.candidateExecutionPlanSha256) `
    'Execution-plan fingerprint differs.'

$parseRecords = [Collections.Generic.List[object]]::new()
foreach ($record in @($repair.repairPaths)) {
    if ([string]$record.path -notmatch '\.ps(m)?1$') { continue }
    $tokens = $null; $errors = $null
    [void][Management.Automation.Language.Parser]::ParseFile(
        (Join-Path $RepositoryRoot ([string]$record.path)), [ref]$tokens, [ref]$errors)
    Assert-G9U1Repair (@($errors).Count -eq 0) "Parser rejected $($record.path)."
    $parseRecords.Add([ordered]@{ path = [string]$record.path; state = 'PASS' })
}
Write-G9U1RepairJson $staticRecordPath ([ordered]@{
        schemaVersion = 1
        state = 'PASS'
        mode = 'COMMITTED_CANDIDATE'
        technicalCommit = $CandidateCommit
        productRuntimeExecuted = $false
        skipBuildIsAcceptanceEvidence = $false
        purpose = 'LIFECYCLE_AND_STATIC_INTEGRATION_ONLY'
        staticLogSha256 = Get-G9U1RepairSha256 $staticLogPath
        parserFiles = $parseRecords
    })
$diffCommands = @(
    "git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check $ProductCheckpoint..$CandidateCommit"
    'git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check'
    'git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --cached --check'
)
foreach ($arguments in @(
        @('-c', 'core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol',
            'diff', '--check', "$ProductCheckpoint..$CandidateCommit"),
        @('-c', 'core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol',
            'diff', '--check'),
        @('-c', 'core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol',
            'diff', '--cached', '--check'))) {
    [void](Invoke-G9U1RepairGit $RepositoryRoot $arguments)
}
$diffPath = Join-Path $LogDirectory 'git-diff-check.json'
Write-G9U1RepairJson $diffPath ([ordered]@{ schemaVersion = 1; state = 'PASS';
        base = $ProductCheckpoint; candidate = $CandidateCommit;
        commands = $diffCommands;
        paths = @($comparison.changedPaths.path | Sort-Object -CaseSensitive);
        productPaths = @($comparison.changedPaths.path | Where-Object { $_ -cmatch '^source/' }) })

$conditionText = @(
    'No product Java change.',
    'No Desktop product or UI change.',
    'No scientific-test change.',
    'No numerical reference or tolerance change.',
    'No Gradle or build-script change.',
    'No test task/filter selection change.',
    'No required Java/toolchain change.',
    'No numerical-command change.',
    'No JUnit pass/fail acceptance-semantics change.',
    'No generated-state lifecycle change affecting test execution.',
    'Changed executable functions are limited to input identity, provenance and cross-checkout closeout validation; focused fixtures validate that scope.',
    'Prior successful PHASE/COMPOSED/FULL evidence is sealed and names the exact scientific/product cohort to which the linkage applies.',
    'The complete dedicated identity/lifecycle infrastructure suite passes twice with deterministic results, including tampering and materialization negatives.',
    'Bounded real canonical shared-module and Desktop verification integrations pass, alongside parser/static checks, Git whitespace checks and the actual previously failing R1 closeout/materialization case.',
    'An authenticated deterministic execution-plan/impact comparison establishes unchanged tasks, module roots, test filters, Checkstyle requirements, numerical/reference commands, JVM/toolchain requirements, relevant execution environment/system-property policy and result acceptance semantics.'
)
$conditions = for ($index = 0; $index -lt $conditionText.Count; $index++) {
    [ordered]@{ id = $index + 1; requirement = $conditionText[$index]; result = 'PASS';
        evidence = if ($index -lt 10) { @('REPAIR_EQUIVALENCE') }
            elseif ($index -eq 10) { @('REPAIR_EQUIVALENCE', 'INPUT_IDENTITY_A_SUMMARY',
                'INPUT_IDENTITY_B_SUMMARY', 'G9U1_LIFECYCLE_A_SUMMARY',
                'G9U1_LIFECYCLE_B_SUMMARY') }
            elseif ($index -eq 11) { @('FOCUSED_A_SUMMARY', 'FOCUSED_B_SUMMARY',
                'PHASE_ROOT', 'PHASE_SUMMARY', 'COMPOSED_ROOT', 'COMPOSED_RECEIPT',
                'FULL_ROOT', 'FULL_RECEIPT', 'CHECKPOINT_AUTHORITY',
                'CHECKPOINT_INPUT_INVENTORY', 'CHECKPOINT_MATERIALIZATION_PROOF') }
            elseif ($index -eq 12) { @('INPUT_IDENTITY_A_SUMMARY',
                'INPUT_IDENTITY_B_SUMMARY', 'G9U1_LIFECYCLE_A_SUMMARY',
                'G9U1_LIFECYCLE_B_SUMMARY') }
            elseif ($index -eq 13) { @('SHARED_INTEGRATION_RESULT',
                'SHARED_INTEGRATION_ROOT', 'DESKTOP_INTEGRATION_RESULT',
                'DESKTOP_INTEGRATION_ROOT', 'R1_CROSS_CHECKOUT_RESULT',
                'PARSER_STATIC_RESULT', 'GIT_DIFF_RESULT') }
            else { @('REPAIR_EQUIVALENCE') } }
}
$matrixPath = Join-Path $LogDirectory 'adr-0024-condition-matrix.json'
Write-G9U1RepairJson $matrixPath ([ordered]@{ schemaVersion = 1;
        kind = 'ADR0024_EVIDENCE_PRESERVING_VERIFIER_REPAIR_CONDITION_MATRIX';
        reviewedProductCheckpoint = $ProductCheckpoint; operationalSuccessor = $CandidateCommit;
        conditions = $conditions; passed = 15; failed = 0;
        evidencePreservingVerifierRepair = $true; heavyScientificExecutionRepeated = $false;
        historicalExecutionRelabeled = $false; selfApproved = $false })

$bundleRoot = Join-Path $LogDirectory 'technical-evidence-bundle'
[void][IO.Directory]::CreateDirectory($bundleRoot)
$entries = [Collections.Generic.List[object]]::new()
$counter = 0
function Add-G9U1Bundle {
    param([string]$Role, [string]$Source, [string]$Recorded)
    $script:counter++
    $entries.Add((New-G9U1BundleEntry $Role $Source $Recorded $bundleRoot $script:counter))
}
foreach ($special in @(
        @('FOCUSED_A_SUMMARY', $historical.Paths.focusedA),
        @('FOCUSED_B_SUMMARY', $historical.Paths.focusedB),
        @('PHASE_ROOT', $historical.Paths.phaseRoot),
        @('PHASE_SUMMARY', $historical.Paths.phaseSummary),
        @('COMPOSED_ROOT', $historical.Paths.composedRoot),
        @('FULL_ROOT', $historical.Paths.fullRoot))) {
    Add-G9U1Bundle $special[0] (Join-Path $RepositoryRoot $special[1]) $special[1]
}
foreach ($receipt in @(
        @('COMPOSED_RECEIPT', $historical.Paths.composedReceipt, $historical.Paths.composedRoot),
        @('FULL_RECEIPT', $historical.Paths.fullReceipt, $historical.Paths.fullRoot))) {
    Add-G9U1Bundle $receipt[0] (Join-Path $RepositoryRoot $receipt[1]) $receipt[1]
}
$recorded = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach ($receipt in @($historical.Receipts)) {
    $references = @($receipt.auditArtifacts | ForEach-Object {
            [pscustomobject]@{ path = [string]$_.path; sha256 = [string]$_.sha256 }
        }) + @($receipt.junit | ForEach-Object {
            [pscustomobject]@{ path = [string]$_.archivePath; sha256 = [string]$_.sha256 }
        }) + @($receipt.checkstyle | ForEach-Object {
            [pscustomobject]@{ path = [string]$_.archivePath; sha256 = [string]$_.sha256 }
        })
    foreach ($reference in $references) {
        if (-not $recorded.Add($reference.path)) { continue }
        $source = if ([IO.Path]::IsPathRooted($reference.path)) { $reference.path }
            else { Join-Path $RepositoryRoot $reference.path }
        Assert-G9U1Repair ((Test-Path -LiteralPath $source -PathType Leaf) -and
            (Get-G9U1RepairSha256 $source) -ceq $reference.sha256) `
            "Historical receipt artifact missing or changed: $($reference.path)"
        Add-G9U1Bundle 'ARTIFACT' $source $reference.path
    }
}
foreach ($artifact in @(
        @('CHECKPOINT_AUTHORITY', (Join-Path $RepositoryRoot $historical.Paths.checkpoint), $historical.Paths.checkpoint),
        @('CHECKPOINT_INPUT_INVENTORY', (Join-Path $RepositoryRoot $historical.Paths.checkpointInventory), $historical.Paths.checkpointInventory),
        @('CHECKPOINT_MATERIALIZATION_PROOF', (Join-Path $RepositoryRoot $historical.Paths.materialization), $historical.Paths.materialization),
        @('REPAIR_POLICY', $RepairPolicyPath, 'geocedg/validation/operations/g9u1-lifecycle-repair-policy.json'),
        @('REPAIR_CONDITION_MATRIX', $matrixPath, 'adr-0024-condition-matrix.json'),
        @('REPAIR_EQUIVALENCE', $structuralPath, 'execution-plan-proof.json'),
        @('INPUT_IDENTITY_A_SUMMARY', (Join-Path $LogDirectory 'focused-a/input-identity/canonical-summary.json'), 'focused-a/input-identity/canonical-summary.json'),
        @('INPUT_IDENTITY_B_SUMMARY', (Join-Path $LogDirectory 'focused-b/input-identity/canonical-summary.json'), 'focused-b/input-identity/canonical-summary.json'),
        @('G9U1_LIFECYCLE_A_SUMMARY', (Join-Path $LogDirectory 'focused-a/g9u1-lifecycle/canonical-summary.json'), 'focused-a/g9u1-lifecycle/canonical-summary.json'),
        @('G9U1_LIFECYCLE_B_SUMMARY', (Join-Path $LogDirectory 'focused-b/g9u1-lifecycle/canonical-summary.json'), 'focused-b/g9u1-lifecycle/canonical-summary.json'),
        @('G9U1_COMMITTED_STATIC_LOG', $staticLogPath, 'static-g9u1-committed.log'),
        @('SHARED_INTEGRATION_RESULT', $sharedRecordPath, $sharedRecordPath),
        @('SHARED_INTEGRATION_ROOT', $sharedRootPath, 'shared-live-verification-result.json'),
        @('DESKTOP_INTEGRATION_RESULT', $desktopRecordPath, $desktopRecordPath),
        @('DESKTOP_INTEGRATION_ROOT', $desktopRootPath, 'desktop-live-verification-result.json'),
        @('R1_CROSS_CHECKOUT_RESULT', $r1RecordPath, 'r1-cross-checkout-integration.json'),
        @('PARSER_STATIC_RESULT', $staticRecordPath, 'parser-static-integration.json'),
        @('GIT_DIFF_RESULT', $diffPath, 'git-diff-check.json'))) {
    Add-G9U1Bundle $artifact[0] $artifact[1] $artifact[2]
}
foreach ($integrationEvidence in @($sharedRecordPath, $desktopRecordPath)) {
    $document = Get-Content -Raw -LiteralPath $integrationEvidence | ConvertFrom-Json -Depth 100
    foreach ($reference in @($document.junit)) {
        if (-not $recorded.Add([string]$reference.archivePath)) { continue }
        Add-G9U1Bundle 'REPAIR_ARTIFACT' ([string]$reference.archivePath) ([string]$reference.archivePath)
    }
}
$manifest = [ordered]@{
    schemaVersion = 2
    kind = 'GEOCEDG_EVIDENCE_PRESERVING_PRECOMMIT_LINK'
    phase = 'G9U1'
    reviewedTechnicalCommit = $CandidateCommit
    productCheckpoint = [ordered]@{ commit = $ProductCheckpoint; tree = $ProductTree }
    historicalExecution = [ordered]@{
        head = $HistoricalHead; indexSha256 = $HistoricalIndexSha256;
        statusSha256 = $HistoricalStatusSha256; rawTreeSha256 = $HistoricalRawTreeSha256;
        rawFiles = 11299; rawBytes = 187439751;
        inputInventorySha256 = $HistoricalInputInventorySha256;
        inputFingerprint = $HistoricalInputFingerprint;
        executionPlanSha256 = $comparison.reviewedExecutionPlanSha256;
        provenance = 'PRECOMMIT HEAD=e4ef3d48 plus exact candidate worktree; NOT_REATTRIBUTED to 28f or successor'
    }
    repair = [ordered]@{
        evidencePreservingVerifierRepair = $true
        repairPolicyRole = 'REPAIR_POLICY'
        repairPolicyPath = 'geocedg/validation/operations/g9u1-lifecycle-repair-policy.json'
        conditionMatrixRole = 'REPAIR_CONDITION_MATRIX'
        structuralProofRole = 'REPAIR_EQUIVALENCE'
        checkpointAuthorityRole = 'CHECKPOINT_AUTHORITY'
        checkpointInventoryRole = 'CHECKPOINT_INPUT_INVENTORY'
        checkpointMaterializationRole = 'CHECKPOINT_MATERIALIZATION_PROOF'
        inputIdentityARole = 'INPUT_IDENTITY_A_SUMMARY'
        inputIdentityBRole = 'INPUT_IDENTITY_B_SUMMARY'
        lifecycleARole = 'G9U1_LIFECYCLE_A_SUMMARY'
        lifecycleBRole = 'G9U1_LIFECYCLE_B_SUMMARY'
        committedStaticRole = 'G9U1_COMMITTED_STATIC_LOG'
        sharedIntegrationRole = 'SHARED_INTEGRATION_RESULT'
        sharedIntegrationRootRole = 'SHARED_INTEGRATION_ROOT'
        desktopIntegrationRole = 'DESKTOP_INTEGRATION_RESULT'
        desktopIntegrationRootRole = 'DESKTOP_INTEGRATION_ROOT'
        r1CrossCheckoutRole = 'R1_CROSS_CHECKOUT_RESULT'
        parserStaticRole = 'PARSER_STATIC_RESULT'
        gitDiffRole = 'GIT_DIFF_RESULT'
        technicalExecutionRepeated = $false
        priorExecutionRelabeled = $false
    }
    files = @($entries)
}
$manifestPath = Join-Path $bundleRoot 'technical-evidence-manifest.json'
Write-G9U1RepairJson $manifestPath $manifest
$manifestSha256 = Get-G9U1RepairSha256 $manifestPath
$sealedManifest = ConvertFrom-GeoCeDGPhaseLifecycleJson `
    ([IO.File]::ReadAllBytes($manifestPath)) 'sealed G9U1 evidence-preserving manifest'

if (Get-Command Assert-GeoCeDGEvidencePreservingPrecommitLink -ErrorAction SilentlyContinue) {
    $validated = Assert-GeoCeDGEvidencePreservingPrecommitLink -RepositoryRoot $RepositoryRoot `
        -TechnicalCommit $CandidateCommit -Policy $lifecycle -Manifest $sealedManifest `
        -BundleDirectory $bundleRoot -BundleSha256 $manifestSha256 -CohortOnly
    Assert-G9U1Repair ($validated.documentaryEvidenceLinked -and
        -not $validated.technicalExecutionRepeated -and -not $validated.priorExecutionRelabeled) `
        'Schema-v2 evidence validator returned an invalid linkage result.'
} else { throw 'Schema-v2 G9U1 evidence-link validator is unavailable.' }

$final = [ordered]@{
    schemaVersion = 1
    kind = 'G9U1_LIFECYCLE_REPAIR_FINAL_TECHNICAL_EVIDENCE'
    productCheckpoint = $ProductCheckpoint
    reviewedTechnicalCommitCandidate = $CandidateCommit
    candidateTree = $tree
    historicalExecution = [ordered]@{ head = $HistoricalHead;
        worktree = 'PRECOMMIT_CANDIDATE'; relabeled = $false }
    focusedInputIdentityA = $identityHashes[0]
    focusedInputIdentityB = $identityHashes[1]
    focusedG9U1LifecycleA = $lifecycleHashes[0]
    focusedG9U1LifecycleB = $lifecycleHashes[1]
    evidencePreservingVerifierRepair = $true
    conditionsPassed = 15
    phaseComposedFullRepeated = $false
    technicalEvidenceBundle = [ordered]@{
        directory = [IO.Path]::GetRelativePath($RepositoryRoot, $bundleRoot).Replace('\', '/')
        manifestPath = 'technical-evidence-manifest.json'
        manifestSha256 = $manifestSha256
    }
    authorApprovedImplementation = $false
    passClaimedImplementation = $false
    selfApproved = $false
}
$finalPath = Join-Path $LogDirectory 'canonical-summary.json'
Write-G9U1RepairJson $finalPath $final
$finalHash = Get-G9U1RepairSha256 $finalPath
[IO.File]::WriteAllText((Join-Path $LogDirectory 'canonical-summary.sha256'),
    "$finalHash`n", [Text.UTF8Encoding]::new($false))
Write-Host "G9U1 lifecycle repair: ADR 0024 section 11.2 = 15/15 PASS."
Write-Host "EVIDENCE_PRESERVING_VERIFIER_REPAIR = true"
Write-Host "Technical evidence bundle SHA-256: $manifestSha256"
Write-Host "Final repair summary SHA-256: $finalHash"
Write-Host 'Historical PHASE/COMPOSED/FULL were linked, not repeated or reattributed.'
exit 0

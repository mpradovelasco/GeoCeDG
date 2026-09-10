#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$closeout = Join-Path $repository 'tools/agent/phase-closeout.ps1'
$authorCloseout = Join-Path $repository 'tools/agent/verify-phase-author-closeout.ps1'
$receiptModule = Import-Module (Join-Path $repository 'tools/agent/verification-receipt.psm1') -Force -PassThru
Import-Module (Join-Path $repository 'tools/agent/verification-io.psm1') -Force
. (Join-Path $PSScriptRoot 'fixtures/verification-environment-fixture.ps1')
$pwsh = (Get-Process -Id $PID).Path
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-closeout-compat-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-closeout-compatibility', [Text.UTF8Encoding]::new($false))
$fixture = Join-Path $root 'repository'
[void][IO.Directory]::CreateDirectory($fixture)
$script:Cases = 0
$script:Assertions = 0

function Assert-Case([bool]$Condition, [string]$Message) {
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}
function Invoke-Case([string]$Name, [scriptblock]$Body) {
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}
function Invoke-ScriptCapture([string]$Path, [string[]]$Arguments) {
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $pwsh
    $start.WorkingDirectory = $fixture
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false)
    $start.StandardErrorEncoding = [Text.UTF8Encoding]::new($false)
    foreach ($argument in @('-NoLogo', '-NoProfile', '-File', $Path) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw "Could not start $Path" }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        return [pscustomobject]@{
            exit_code = $process.ExitCode
            stdout = $stdoutTask.GetAwaiter().GetResult()
            stderr = $stderrTask.GetAwaiter().GetResult()
        }
    } finally { $process.Dispose() }
}
function Get-GitSnapshot {
    $head = (& git -C $fixture rev-parse HEAD).Trim()
    $tree = (& git -C $fixture rev-parse 'HEAD^{tree}').Trim()
    $status = (& git -C $fixture status --porcelain=v1 --untracked-files=all) -join [char]10
    $refs = (& git -C $fixture for-each-ref --format='%(refname) %(objectname)') -join [char]10
    return [pscustomobject]@{ head = $head; tree = $tree; status = $status; refs = $refs }
}

try {
    & git -C $fixture init --quiet
    & git -C $fixture config user.name Fixture
    & git -C $fixture config user.email fixture@example.invalid
    [IO.File]::WriteAllText((Join-Path $fixture 'content.txt'), ('identity' + [char]10), [Text.UTF8Encoding]::new($false))
    & git -C $fixture add content.txt
    & git -C $fixture commit --quiet -m fixture
    if ($LASTEXITCODE -ne 0) { throw 'Could not create closeout fixture commit.' }
    $snapshot = Get-GitSnapshot
    $environmentIdentity = New-VerificationTestEnvironmentIdentity `
        -BaseCommit $null -BaseTree $null -CandidateCommit $snapshot.head `
        -CandidateTree $snapshot.tree -CompatibilitySignature ('4' * 64)
    $receipt = [ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-receipt.schema.json'
        schema_version = 3
        receipt_kind = 'GEOCEDG_ACCEPTANCE_RECEIPT'
        receipt_id = ('0' * 64)
        accepted_report_hash = ('1' * 64)
        base_commit = $null
        base_tree = $null
        candidate_commit = $snapshot.head
        candidate_tree = $snapshot.tree
        execution_plan_hash = ('2' * 64)
        checker_identity_hash = ('3' * 64)
        environment_contract = $environmentIdentity.environment_contract
        environment_compatibility_signature = `
            $environmentIdentity.environment_compatibility_signature
        environment_observation = $environmentIdentity.environment_observation
        environment_observation_hash = $environmentIdentity.environment_observation_hash
        input_identity_hash = ('5' * 64)
        accepted_profiles = @('FINAL')
        semantic_domains = @('MIXED', 'PRODUCT', 'SCIENTIFIC')
        diagnostic_hashes = @('6' * 64)
        issued_at = '2026-01-01T00:00:00.0000000Z'
    }
    $receipt.receipt_id = & $receiptModule {
        param($Value)
        Get-VerificationDeterministicHash -Value (Get-VerificationReceiptIdentityPayload -Receipt $Value)
    } $receipt
    $receiptPath = Join-Path $root 'acceptance-receipt.json'
    [IO.File]::WriteAllText($receiptPath,
        ((ConvertTo-Json $receipt -Depth 20).Replace(
                ([string][char]13 + [char]10), [string][char]10) + [char]10),
        [Text.UTF8Encoding]::new($false))

    Invoke-Case 'identity-only closeout accepts an exact receipt without Git mutation' {
        $before = Get-GitSnapshot
        $result = Invoke-ScriptCapture $closeout @(
            '-Action', 'INSPECT', '-CandidateCommit', $snapshot.head,
            '-ReceiptPath', $receiptPath, '-RepositoryRoot', $fixture,
            '-ApprovedCommit', $snapshot.head)
        $after = Get-GitSnapshot
        Assert-Case ($result.exit_code -eq 0) 'Exact identity inspection failed.'
        $payload = $result.stdout | ConvertFrom-Json -Depth 20
        Assert-Case ($payload.identity_confirmed -and -not $payload.repository_mutated -and
            -not $payload.verification_executed -and -not $payload.author_approval_recorded -and
            -not $payload.publication_performed) 'Closeout reported a mutating operation.'
        Assert-Case (($before | ConvertTo-Json -Compress) -ceq
            ($after | ConvertTo-Json -Compress)) 'Identity inspection changed Git state.'
    }

    Invoke-Case 'public author-closeout adapter delegates only identity inspection' {
        $before = Get-GitSnapshot
        $result = Invoke-ScriptCapture $authorCloseout @(
            '-TargetRepositoryRoot', $fixture,
            '-ReviewedTechnicalCommit', $snapshot.head,
            '-ReceiptPath', $receiptPath,
            '-ApprovedCommit', $snapshot.head)
        $after = Get-GitSnapshot
        Assert-Case ($result.exit_code -eq 0) 'Author-closeout adapter failed exact identity.'
        Assert-Case (($before | ConvertTo-Json -Compress) -ceq
            ($after | ConvertTo-Json -Compress)) 'Author-closeout adapter changed Git state.'
    }

    Invoke-Case 'retired mutation actions fail before changing Git' {
        foreach ($action in @('PREPARE', 'FINALIZE')) {
            $before = Get-GitSnapshot
            $result = Invoke-ScriptCapture $closeout @(
                '-Action', $action, '-CandidateCommit', $snapshot.head,
                '-ReceiptPath', $receiptPath, '-RepositoryRoot', $fixture)
            $after = Get-GitSnapshot
            Assert-Case ($result.exit_code -ne 0) "$action unexpectedly succeeded."
            Assert-Case (($before | ConvertTo-Json -Compress) -ceq
                ($after | ConvertTo-Json -Compress)) "$action changed Git state."
        }
    }

    Invoke-Case 'receipt identity mismatch is rejected without mutation' {
        $bad = [ordered]@{}
        foreach ($property in $receipt.GetEnumerator()) { $bad[$property.Key] = $property.Value }
        $bad.candidate_tree = ('f' * 40)
        $bad.receipt_id = & $receiptModule {
            param($Value)
            Get-VerificationDeterministicHash -Value (Get-VerificationReceiptIdentityPayload -Receipt $Value)
        } $bad
        $badPath = Join-Path $root 'mismatched-receipt.json'
        [IO.File]::WriteAllText($badPath,
            ((ConvertTo-Json $bad -Depth 20).Replace(
                    ([string][char]13 + [char]10), [string][char]10) + [char]10),
            [Text.UTF8Encoding]::new($false))
        $before = Get-GitSnapshot
        $result = Invoke-ScriptCapture $closeout @(
            '-CandidateCommit', $snapshot.head, '-ReceiptPath', $badPath,
            '-RepositoryRoot', $fixture)
        $after = Get-GitSnapshot
        Assert-Case ($result.exit_code -eq 3) 'Receipt mismatch did not fail safely.'
        Assert-Case (($before | ConvertTo-Json -Compress) -ceq
            ($after | ConvertTo-Json -Compress)) 'Rejected receipt changed Git state.'
    }
} finally {
    Remove-Module $receiptModule -Force -ErrorAction SilentlyContinue
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-closeout-compatibility') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else { throw "Unsafe closeout fixture cleanup target: $resolved" }
}

Write-Host "verification-closeout-compatibility.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

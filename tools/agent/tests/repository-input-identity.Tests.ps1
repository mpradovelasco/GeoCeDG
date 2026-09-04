#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot '../repository-input-identity.ps1'),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) 'geocedg-repository-identity-tests')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
. ([IO.Path]::GetFullPath($HelperPath))
$runRoot = Join-Path ([IO.Path]::GetTempPath()) ('geocedg-identity-' + [Guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($runRoot)
$results = [Collections.Generic.List[object]]::new()

function Write-FixtureText {
    param([string]$Path, [AllowEmptyString()] [string]$Text)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    [IO.File]::WriteAllText($Path, $Text, [Text.UTF8Encoding]::new($false))
}

function Invoke-FixtureGit {
    param([string]$Root, [string[]]$Arguments)
    $hooks = Join-Path $runRoot 'empty-hooks'
    [void][IO.Directory]::CreateDirectory($hooks)
    $result = Invoke-GeoCeDGGitByteCommand $Root (@('-c', "core.hooksPath=$hooks",
        '-c', 'commit.gpgSign=false') + $Arguments)
    return (ConvertFrom-GeoCeDGRepositoryIdentityGit $result).TrimEnd("`r", "`n")
}

function Commit-Fixture {
    param([string]$Root, [string]$Message)
    [void](Invoke-FixtureGit $Root @('add', '--all'))
    [void](Invoke-FixtureGit $Root @('commit', '--quiet', '-m', $Message))
    return Invoke-FixtureGit $Root @('rev-parse', 'HEAD')
}

function New-Fixture {
    $root = Join-Path $runRoot ([Guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($root)
    [void](Invoke-FixtureGit $root @('init', '--quiet'))
    [void](Invoke-FixtureGit $root @('config', 'core.autocrlf', 'false'))
    [void](Invoke-FixtureGit $root @('config', 'core.filemode', 'false'))
    [void](Invoke-FixtureGit $root @('config', 'user.name', 'GeoCeDG isolated identity fixture'))
    [void](Invoke-FixtureGit $root @('config', 'user.email', 'fixture@example.invalid'))
    Write-FixtureText (Join-Path $root '.gitattributes') "*.txt text`n*.bin -text`n"
    Write-FixtureText (Join-Path $root 'source.txt') "first line`nsecond line`n"
    Write-FixtureText (Join-Path $root 'status.txt') "CANDIDATE`n"
    [IO.File]::WriteAllBytes((Join-Path $root 'binary.bin'), [byte[]]@(0, 13, 10, 255, 128, 9, 0))
    $commit = Commit-Fixture $root 'base'
    return [pscustomobject]@{ root = $root; commit = $commit }
}

function Assert-Test {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-Rejected {
    param([scriptblock]$Action, [string]$Pattern)
    try { & $Action | Out-Null } catch {
        Assert-Test ($_.Exception.Message -match $Pattern) "Wrong rejection: $($_.Exception.Message)"
        return
    }
    throw "TEST FAILURE: expected rejection /$Pattern/."
}

function Test-Case {
    param([string]$Name, [scriptblock]$Action)
    & $Action
    $results.Add([pscustomobject][ordered]@{ name = $Name; outcome = 'PASS' })
    Write-Host "PASS $Name"
}

Test-Case 'same commit LF Git materialization' {
    $f = New-Fixture
    $result = Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit
    Assert-Test ($result.trackedFiles -eq 4) 'Expected complete tracked tree.'
}

foreach ($autocrlf in @('true', 'false', 'input')) {
    Test-Case "LF and CRLF same Git identity autocrlf=$autocrlf" {
        $f = New-Fixture
        [void](Invoke-FixtureGit $f.root @('config', 'core.autocrlf', $autocrlf))
        $before = Get-GeoCeDGRepositoryTrackedIdentity $f.root $f.commit
        $lfHash = (Get-FileHash -LiteralPath (Join-Path $f.root 'source.txt')).Hash
        Write-FixtureText (Join-Path $f.root 'source.txt') "first line`r`nsecond line`r`n"
        $crlfHash = (Get-FileHash -LiteralPath (Join-Path $f.root 'source.txt')).Hash
        Assert-Test ($lfHash -cne $crlfHash) 'Physical hashes must remain different.'
        $after = Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit
        Assert-Test ($before.sha256 -ceq $after.trackedSha256) 'Tracked source authority changed under EOL only.'
    }
}

foreach ($eol in @('lf', 'crlf')) {
    Test-Case "explicit text eol=$eol" {
        $f = New-Fixture
        Write-FixtureText (Join-Path $f.root '.gitattributes') "*.txt text eol=$eol`n*.bin -text`n"
        $commit = Commit-Fixture $f.root 'explicit EOL'
        $text = if ($eol -eq 'lf') { "first line`nsecond line`n" } else { "first line`r`nsecond line`r`n" }
        Write-FixtureText (Join-Path $f.root 'source.txt') $text
        [void](Assert-GeoCeDGWorktreeMaterialization $f.root $commit)
    }
}

Test-Case 'binary tracked blob remains byte authoritative' {
    $f = New-Fixture
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit)
    [IO.File]::WriteAllBytes((Join-Path $f.root 'binary.bin'), [byte[]]@(0, 10, 255, 128, 9, 0))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'not a clean Git materialization'
}

foreach ($historicalEol in @('CRLF', 'MIXED')) {
    Test-Case "exact historical $historicalEol blob remains valid under current autocrlf true" {
        $f = New-Fixture
        # Match historical repository text that predates explicit text attrs.
        # Forced `text` would request renormalization, not this legacy contract.
        Write-FixtureText (Join-Path $f.root '.gitattributes') "*.bin -text`n"
        [void](Commit-Fixture $f.root 'Historical unspecified text attributes')
        $text = if ($historicalEol -eq 'CRLF') { "first line`r`nsecond line`r`n" }
            else { "first line`r`nsecond line`n" }
        Write-FixtureText (Join-Path $f.root 'source.txt') $text
        $rawOid = Invoke-FixtureGit $f.root @('hash-object', '-w', '--no-filters', 'source.txt')
        [void](Invoke-FixtureGit $f.root @('update-index', '--cacheinfo', "100644,$rawOid,source.txt"))
        [void](Invoke-FixtureGit $f.root @('commit', '--quiet', '-m', 'Historical raw line endings'))
        $commit = Invoke-FixtureGit $f.root @('rev-parse', 'HEAD')
        [void](Invoke-FixtureGit $f.root @('config', 'core.autocrlf', 'true'))
        [void](Invoke-FixtureGit $f.root @('checkout-index', '-f', '-u', '--', 'source.txt'))
        [void](Assert-GeoCeDGWorktreeMaterialization $f.root $commit)
    }
}

Test-Case 'EOL change without Git text conversion remains a real blob mutation' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root '.gitattributes') "* -text`n"
    $commit = Commit-Fixture $f.root 'disable text conversion'
    Write-FixtureText (Join-Path $f.root 'source.txt') "first line`r`nsecond line`r`n"
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $commit } 'not a clean Git materialization'
}

Test-Case 'unchanged executable Git mode is supported even when host filemode false' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('update-index', '--chmod=+x', '--', 'source.txt'))
    [void](Invoke-FixtureGit $f.root @('commit', '--quiet', '-m', 'mode'))
    $commit = Invoke-FixtureGit $f.root @('rev-parse', 'HEAD')
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $commit)
    $identity = Get-GeoCeDGRepositoryTrackedIdentity $f.root $commit
    Assert-Test (($identity.entries | Where-Object path -eq 'source.txt').mode -eq '100755') 'Git mode lost.'
}

foreach ($change in @('blob', 'mode', 'rename', 'extra')) {
    Test-Case "nonallowlisted tracked $change rejected" {
        $f = New-Fixture
        switch ($change) {
            'blob' { Write-FixtureText (Join-Path $f.root 'source.txt') "semantic change`n" }
            'mode' { [void](Invoke-FixtureGit $f.root @('update-index', '--chmod=+x', '--', 'source.txt')) }
            'rename' { [void](Invoke-FixtureGit $f.root @('mv', 'source.txt', 'renamed.txt')) }
            'extra' { Write-FixtureText (Join-Path $f.root 'extra.txt') "new input`n" }
        }
        $changed = Commit-Fixture $f.root 'forbidden delta'
        Assert-Rejected { Assert-GeoCeDGRepositoryTreeDelta $f.root $f.commit $changed @() } 'exact closeout allowlist'
    }
}

Test-Case 'exact closeout delta and valid current materialization' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'status.txt') "AUTHOR_APPROVED`n"
    $closeout = Commit-Fixture $f.root 'approval only'
    $proof = Assert-GeoCeDGRepositoryTreeDelta $f.root $f.commit $closeout @('status.txt')
    Assert-Test ($proof.changedPaths.Count -eq 1) 'Expected exact closeout delta.'
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $closeout)
}

Test-Case 'index staged blob mismatch rejected' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'source.txt') "changed`n"
    [void](Invoke-FixtureGit $f.root @('add', 'source.txt'))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Index differs'
}

Test-Case 'index staged mode mismatch rejected' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('update-index', '--chmod=+x', '--', 'source.txt'))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Index differs'
}

Test-Case 'real unstaged source mutation rejected' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'source.txt') "first LINE`nsecond line`n"
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'not a clean Git materialization'
}

Test-Case 'same-size mutation with restored mtime cannot hide in Git stat cache' {
    $f = New-Fixture
    $path = Join-Path $f.root 'source.txt'
    $mtime = [IO.File]::GetLastWriteTimeUtc($path)
    $length = (Get-Item -LiteralPath $path).Length
    Write-FixtureText $path "first LINE`nsecond line`n"
    [IO.File]::SetLastWriteTimeUtc($path, $mtime)
    Assert-Test ((Get-Item -LiteralPath $path).Length -eq $length) 'Fixture must retain physical length.'
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'not a clean Git materialization'
}

Test-Case 'tracked path with spaces retains exact path identity' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('mv', 'source.txt', 'source with spaces.txt'))
    $commit = Commit-Fixture $f.root 'space path'
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $commit)
}

Test-Case 'tracked symlink mode rejected without following link' {
    $f = New-Fixture
    $oid = Invoke-FixtureGit $f.root @('rev-parse', 'HEAD:source.txt')
    [void](Invoke-FixtureGit $f.root @('update-index', '--cacheinfo', "120000,$oid,source.txt"))
    [void](Invoke-FixtureGit $f.root @('commit', '--quiet', '-m', 'unsupported symlink'))
    $commit = Invoke-FixtureGit $f.root @('rev-parse', 'HEAD')
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $commit } 'Unsupported materialization mode'
}

Test-Case 'tracked submodule mode rejected' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('update-index', '--add', '--cacheinfo', "160000,$($f.commit),submodule"))
    [void](Invoke-FixtureGit $f.root @('commit', '--quiet', '-m', 'unsupported submodule'))
    $commit = Invoke-FixtureGit $f.root @('rev-parse', 'HEAD')
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $commit } 'Unsupported materialization mode'
}

Test-Case 'assume unchanged cannot hide tracked mutation' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('update-index', '--assume-unchanged', '--', 'source.txt'))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Unsupported index visibility'
}

Test-Case 'skip worktree cannot hide tracked mutation' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('update-index', '--skip-worktree', '--', 'source.txt'))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Unsupported index visibility'
}

foreach ($attribute in @('filter=untrusted', 'working-tree-encoding=UTF-16LE', 'ident')) {
    Test-Case "unsupported materialization attribute $attribute rejected before clean" {
        $f = New-Fixture
        [void](Invoke-FixtureGit $f.root @('config', 'filter.untrusted.clean', 'false'))
        [void](Invoke-FixtureGit $f.root @('config', 'filter.untrusted.required', 'true'))
        Write-FixtureText (Join-Path $f.root '.gitattributes') "*.txt text $attribute`n*.bin -text`n"
        Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Unsupported materialization attribute'
    }
}

Test-Case 'unused configured external filter is reported but never invoked' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('config', 'filter.unused.clean', 'false'))
    $proof = Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit
    Assert-Test ($proof.materializationConfiguration.filterPolicy -match 'ACTIVE_FILTER_REJECTED') 'Missing filter policy.'
}

Test-Case 'filter driver named unset cannot impersonate the unset attribute state' {
    $f = New-Fixture
    [void](Invoke-FixtureGit $f.root @('config', 'filter.unset.clean', 'false'))
    [void](Invoke-FixtureGit $f.root @('config', 'filter.unset.required', 'true'))
    Write-FixtureText (Join-Path $f.root '.gitattributes') "*.txt text filter=unset`n*.bin -text`n"
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Unsupported materialization attribute'
}

Test-Case 'consumed untracked raw bytes accepted and one-byte change rejected' {
    $f = New-Fixture
    $path = Join-Path $f.root 'local-input.bin'
    [IO.File]::WriteAllBytes($path, [byte[]]@(10, 20, 30))
    $authority = @([pscustomobject]@{ path = 'local-input.bin'; bytes = 3
        sha256 = (Get-FileHash -LiteralPath $path).Hash.ToLowerInvariant() })
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit -ConsumedUntrackedInputs $authority)
    [IO.File]::WriteAllBytes($path, [byte[]]@(10, 21, 30))
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit -ConsumedUntrackedInputs $authority } `
        'Consumed untracked byte authority mismatch'
}

Test-Case 'unauthenticated untracked input rejected by default' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'unclaimed.txt') "new input`n"
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } 'Unauthenticated nonignored untracked'
}

Test-Case 'tampered consumed untracked manifest rejected' {
    $f = New-Fixture
    $path = Join-Path $f.root 'local-input.bin'
    [IO.File]::WriteAllBytes($path, [byte[]]@(10, 20, 30))
    $authority = @([pscustomobject]@{ path = 'local-input.bin'; bytes = 3; sha256 = ('0' * 64) })
    Assert-Rejected { Assert-GeoCeDGConsumedUntrackedInputs $f.root $authority } 'byte authority mismatch'
}

Test-Case 'wrong reviewed commit not ancestral rejected' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'status.txt') "one`n"
    $later = Commit-Fixture $f.root 'later'
    Assert-Rejected { Assert-GeoCeDGRepositoryTreeDelta $f.root $later $f.commit @('status.txt') } 'not an ancestor'
}

Test-Case 'wrong closeout commit rejected by exact delta' {
    $f = New-Fixture
    Assert-Rejected { Assert-GeoCeDGRepositoryTreeDelta $f.root $f.commit $f.commit @('status.txt') } 'exact closeout allowlist'
}

Test-Case 'symbolic commit authority rejected' {
    $f = New-Fixture
    Assert-Rejected { Get-GeoCeDGRepositoryTrackedIdentity $f.root 'HEAD' } 'explicit full commit OID'
}

Test-Case 'missing reviewed SHA rejected' {
    $f = New-Fixture
    Assert-Rejected { Get-GeoCeDGRepositoryTrackedIdentity $f.root ('0' * 40) } 'failed'
}

Test-Case 'pending exact status overlay does not excuse productive changes' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'status.txt') "AUTHOR_APPROVED`n"
    Write-FixtureText (Join-Path $f.root 'approval.json') "{} `n"
    [void](Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit -AllowedStatusPaths @('status.txt', 'approval.json'))
    Write-FixtureText (Join-Path $f.root 'source.txt') "forbidden`n"
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit -AllowedStatusPaths @('status.txt', 'approval.json') } `
        'not a clean Git materialization'
}

Test-Case 'pending status overlay cannot exempt attributes' {
    $f = New-Fixture
    Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit -AllowedStatusPaths @('.gitattributes') } `
        'Unsafe or duplicate pending status overlay'
}

foreach ($override in @('GIT_WORK_TREE', 'GIT_INDEX_FILE')) {
    Test-Case "explicit target rejects $override environment substitution" {
        $f = New-Fixture
        $previous = [Environment]::GetEnvironmentVariable($override)
        try {
            [Environment]::SetEnvironmentVariable($override, (Join-Path $f.root 'different-target'))
            Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } `
                'Unsupported Git repository-identity environment override'
        } finally {
            if ($null -eq $previous) { Remove-Item -LiteralPath "Env:$override" -ErrorAction SilentlyContinue }
            else { [Environment]::SetEnvironmentVariable($override, $previous) }
        }
    }
}

Test-Case 'present empty alternate-index variable is not an absent override' {
    $f = New-Fixture
    $previous = [Environment]::GetEnvironmentVariable('GIT_INDEX_FILE')
    try {
        [Environment]::SetEnvironmentVariable('GIT_INDEX_FILE', '')
        Assert-Rejected { Assert-GeoCeDGWorktreeMaterialization $f.root $f.commit } `
            'Unsupported Git repository-identity environment override'
    } finally {
        if ($null -eq $previous) { Remove-Item -LiteralPath 'Env:GIT_INDEX_FILE' -ErrorAction SilentlyContinue }
        else { [Environment]::SetEnvironmentVariable('GIT_INDEX_FILE', $previous) }
    }
}

Test-Case 'explicit repository root cannot name a subdirectory of another worktree' {
    $f = New-Fixture
    $subdir = Join-Path $f.root 'nested'
    [void][IO.Directory]::CreateDirectory($subdir)
    Assert-Rejected { Get-GeoCeDGRepositoryTrackedIdentity $subdir $f.commit } `
        'Git top-level worktree differs from explicit RepositoryRoot'
}

Test-Case 'Git replacement objects cannot rewrite immutable commit authority' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root 'status.txt') "later`n"
    $later = Commit-Fixture $f.root 'replacement target'
    [void](Invoke-FixtureGit $f.root @('replace', $f.commit, $later))
    Assert-Rejected { Get-GeoCeDGRepositoryTrackedIdentity $f.root $f.commit } `
        'Git replace objects are unsupported'
}

Test-Case 'Git grafts cannot rewrite immutable commit ancestry' {
    $f = New-Fixture
    Write-FixtureText (Join-Path $f.root '.git/info/grafts') ($f.commit + "`n")
    Assert-Rejected { Get-GeoCeDGRepositoryTrackedIdentity $f.root $f.commit } 'Git grafts are unsupported'
}

$summary = [pscustomobject][ordered]@{ schemaVersion = 1; suite = 'repository-input-identity'
    acceptance = 'FOCUSED_VERIFICATION_INFRASTRUCTURE'; total = $results.Count; passed = $results.Count
    cases = @($results); temporaryPathsAreAuthority = $false; rawSameRunIdentityChanged = $false }
$canonical = ($summary | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"
[void][IO.Directory]::CreateDirectory($LogDirectory)
Write-FixtureText (Join-Path $LogDirectory 'canonical-summary.json') $canonical
$hash = Get-GeoCeDGRepositoryIdentityHash $canonical
Write-FixtureText (Join-Path $LogDirectory 'canonical-summary.sha256') ($hash + "`n")
Write-Host "Repository input identity fixtures: $($results.Count)/$($results.Count) PASS; SHA-256 $hash"

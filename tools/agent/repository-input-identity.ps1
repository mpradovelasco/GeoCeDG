#requires -Version 7.2

# Cross-checkout tracked identity only. Live verification-run byte inventories
# remain a separate authority and are deliberately not normalized here.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'evidence-integrity.ps1')

function Assert-GeoCeDGRepositoryIdentity {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Get-GeoCeDGRepositoryIdentityHash {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
        [Text.UTF8Encoding]::new($false).GetBytes($Text))).ToLowerInvariant()
}

function Invoke-GeoCeDGRepositoryIdentityGit {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [AllowEmptyString()] [string]$InputText,
        [switch]$AllowFailure
    )
    # Do not allow optional index writes, external diff/textconv, or fsmonitor
    # hooks to become an unrecorded input of this read-only proof.
    $safeArguments = @('-c', 'core.fsmonitor=false') + $Arguments
    if (-not $PSBoundParameters.ContainsKey('InputText')) {
        return Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
            -Arguments $safeArguments -AllowFailure:$AllowFailure
    }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = 'git'
    $start.WorkingDirectory = $RepositoryRoot
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @('--no-optional-locks', '-C', $RepositoryRoot) + $safeArguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    $output = [IO.MemoryStream]::new()
    try {
        Assert-GeoCeDGRepositoryIdentity ($process.Start()) 'Cannot start Git identity query.'
        $errorTask = $process.StandardError.ReadToEndAsync()
        $outputTask = $process.StandardOutput.BaseStream.CopyToAsync($output)
        $inputBytes = [Text.UTF8Encoding]::new($false).GetBytes($InputText)
        $process.StandardInput.BaseStream.Write($inputBytes, 0, $inputBytes.Length)
        $process.StandardInput.Close()
        [void]$outputTask.GetAwaiter().GetResult()
        $process.WaitForExit()
        $errorText = $errorTask.GetAwaiter().GetResult()
        Assert-GeoCeDGRepositoryIdentity ($AllowFailure -or $process.ExitCode -eq 0) `
            "Git identity query failed: $($Arguments -join ' '): $errorText"
        return [pscustomobject]@{ ExitCode = $process.ExitCode; Bytes = $output.ToArray()
            StandardError = $errorText }
    } finally { $output.Dispose(); $process.Dispose() }
}

function ConvertFrom-GeoCeDGRepositoryIdentityGit {
    param([Parameter(Mandatory)] [object]$Result)
    if ($Result.Bytes.Length -eq 0) { return '' }
    return ConvertFrom-GeoCeDGStrictUtf8 -Bytes $Result.Bytes
}

function Resolve-GeoCeDGRepositoryIdentityCommit {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Commit)
    Assert-GeoCeDGRepositoryIdentity ($Commit -cmatch '^([0-9a-f]{40}|[0-9a-f]{64})$') `
        "Repository identity requires an explicit full commit OID: $Commit"
    $resolved = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('rev-parse', '--verify', "${Commit}^{commit}"))).TrimEnd("`r", "`n")
    Assert-GeoCeDGRepositoryIdentity ($resolved -ceq $Commit) "Wrong commit authority: $Commit"
    return $resolved
}

function Assert-GeoCeDGRepositoryIdentityPath {
    param([Parameter(Mandatory)] [string]$Path)
    Assert-GeoCeDGRepositoryIdentity (-not [IO.Path]::IsPathRooted($Path) -and
        $Path -notmatch '[\x00-\x1f\x7f\\:"]' -and
        @($Path.Split('/') | Where-Object { $_ -in @('', '.', '..') }).Count -eq 0) `
        "Unsupported repository identity path: $Path"
}

function Assert-GeoCeDGRepositoryIdentityEnvironment {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    foreach ($name in @('GIT_DIR', 'GIT_WORK_TREE', 'GIT_INDEX_FILE', 'GIT_COMMON_DIR',
            'GIT_OBJECT_DIRECTORY', 'GIT_ALTERNATE_OBJECT_DIRECTORIES', 'GIT_REPLACE_REF_BASE', 'GIT_NAMESPACE')) {
        Assert-GeoCeDGRepositoryIdentity ($null -eq [Environment]::GetEnvironmentVariable($name)) `
            "Unsupported Git repository-identity environment override: $name"
    }
    # Effective GIT_CONFIG_* options remain supported and are audited below;
    # unlike an alternate index/worktree they do not silently change the target.
    $top = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('rev-parse', '--show-toplevel'))).TrimEnd("`r", "`n")
    $comparison = if ($IsWindows) { [StringComparison]::OrdinalIgnoreCase } else { [StringComparison]::Ordinal }
    Assert-GeoCeDGRepositoryIdentity ([IO.Path]::GetFullPath($top).TrimEnd('/', '\').Equals(
        [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('/', '\'), $comparison)) `
        'Git top-level worktree differs from explicit RepositoryRoot.'
    $replacements = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('replace', '-l'))
    Assert-GeoCeDGRepositoryIdentity ([string]::IsNullOrWhiteSpace($replacements)) `
        'Git replace objects are unsupported for immutable repository identity.'
    $grafts = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('rev-parse', '--git-path', 'info/grafts'))).TrimEnd("`r", "`n")
    if (-not [IO.Path]::IsPathRooted($grafts)) { $grafts = Join-Path $RepositoryRoot $grafts }
    Assert-GeoCeDGRepositoryIdentity (-not (Test-Path -LiteralPath $grafts -PathType Leaf) -or
        (Get-Item -LiteralPath $grafts).Length -eq 0) 'Git grafts are unsupported for immutable repository identity.'
}

function Get-GeoCeDGRepositoryTrackedIdentity {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Commit)
    Assert-GeoCeDGRepositoryIdentityEnvironment $RepositoryRoot
    $commitOid = Resolve-GeoCeDGRepositoryIdentityCommit $RepositoryRoot $Commit
    $treeOid = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('rev-parse', "${commitOid}^{tree}"))).TrimEnd("`r", "`n")
    $text = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('ls-tree', '-r', '-z', '--full-tree', $commitOid))
    $entries = [Collections.Generic.List[object]]::new()
    $names = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($row in $text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        Assert-GeoCeDGRepositoryIdentity ($row -cmatch '^(\d{6}) (blob|commit) ([0-9a-f]+)\t(.+)$') `
            'Unsupported Git tree record.'
        $mode = $Matches[1]; $kind = $Matches[2]; $oid = $Matches[3]; $path = $Matches[4]
        Assert-GeoCeDGRepositoryIdentityPath $path
        Assert-GeoCeDGRepositoryIdentity ($names.Add($path)) "Duplicate tracked path: $path"
        $entries.Add([pscustomobject][ordered]@{ path = $path; mode = $mode; blobOid = $oid; kind = $kind })
    }
    $orderedPaths = [string[]]@($names)
    [Array]::Sort($orderedPaths, [StringComparer]::Ordinal)
    $map = [Collections.Generic.Dictionary[string,object]]::new([StringComparer]::Ordinal)
    foreach ($entry in $entries) { $map.Add($entry.path, $entry) }
    $ordered = @($orderedPaths | ForEach-Object { $map[$_] })
    $canonical = ($ordered | ForEach-Object { "$($_.path)`0$($_.mode)`0$($_.blobOid)`n" }) -join ''
    return [pscustomobject][ordered]@{ schemaVersion = 1; commit = $commitOid; treeOid = $treeOid
        sha256 = Get-GeoCeDGRepositoryIdentityHash $canonical; count = $ordered.Count; entries = $ordered }
}

function Assert-GeoCeDGRepositoryTreeDelta {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$AllowedPaths
    )
    $before = Get-GeoCeDGRepositoryTrackedIdentity $RepositoryRoot $ReviewedCommit
    $after = Get-GeoCeDGRepositoryTrackedIdentity $RepositoryRoot $CloseoutCommit
    $ancestor = Invoke-GeoCeDGRepositoryIdentityGit $RepositoryRoot `
        @('merge-base', '--is-ancestor', $ReviewedCommit, $CloseoutCommit) -AllowFailure
    Assert-GeoCeDGRepositoryIdentity ($ancestor.ExitCode -eq 0) 'Reviewed commit is not an ancestor of closeout commit.'
    $allow = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($path in $AllowedPaths) {
        Assert-GeoCeDGRepositoryIdentityPath $path
        Assert-GeoCeDGRepositoryIdentity ($allow.Add($path)) "Duplicate closeout allowlist path: $path"
    }
    $text = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('diff-tree', '--no-commit-id', '-r', '--name-only', '--no-renames', '-z',
            $ReviewedCommit, $CloseoutCommit, '--'))
    $changed = @($text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries))
    Assert-GeoCeDGRepositoryIdentity ($changed.Count -eq $allow.Count -and
        @($changed | Where-Object { -not $allow.Contains($_) }).Count -eq 0) `
        "Git tree delta differs from exact closeout allowlist: $($changed -join ', ')"
    return [pscustomobject][ordered]@{ reviewedCommit = $ReviewedCommit; closeoutCommit = $CloseoutCommit
        reviewedTreeOid = $before.treeOid; closeoutTreeOid = $after.treeOid
        reviewedTrackedSha256 = $before.sha256; closeoutTrackedSha256 = $after.sha256
        changedPaths = $changed; nonAllowlistedTrackedIdentity = 'GIT_IDENTICAL' }
}

function Get-GeoCeDGMaterializationConfig {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    Assert-GeoCeDGRepositoryIdentityEnvironment $RepositoryRoot
    $text = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('config', '--null', '--list'))
    $config = [Collections.Generic.Dictionary[string,string]]::new([StringComparer]::OrdinalIgnoreCase)
    $relevant = [Collections.Generic.List[string]]::new()
    $filterDrivers = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($record in $text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        $pair = $record.Split([char]10, 2)
        $value = if ($pair.Count -eq 2) { $pair[1] } else { '' }
        $config[$pair[0]] = $value
        if ($pair[0] -match '^filter\.(.+)\.(clean|smudge|process|required)$') {
            [void]$filterDrivers.Add($Matches[1])
        }
        if ($pair[0] -match '^(core\.(autocrlf|eol|attributesfile|filemode|symlinks|fsmonitor|sparsecheckout)|filter\.|extensions\.)') {
            # Configuration may embed credentials in unused filter commands.
            # Preserve effective provenance without disclosing those values.
            $relevant.Add($pair[0] + '=' + (Get-GeoCeDGRepositoryIdentityHash $value))
        }
    }
    $autocrlf = if ($config.ContainsKey('core.autocrlf')) { $config['core.autocrlf'] } else { 'false' }
    $eol = if ($config.ContainsKey('core.eol')) { $config['core.eol'] } else { 'native' }
    Assert-GeoCeDGRepositoryIdentity ($autocrlf -in @('true', 'false', 'input')) 'Unsupported core.autocrlf contract.'
    Assert-GeoCeDGRepositoryIdentity ($eol -in @('lf', 'crlf', 'native')) 'Unsupported core.eol contract.'
    Assert-GeoCeDGRepositoryIdentity (-not $config.ContainsKey('core.sparsecheckout') -or
        $config['core.sparsecheckout'] -in @('false', 'no', 'off', '0')) 'Sparse checkout is unsupported for identity proof.'
    return [pscustomobject][ordered]@{ autocrlf = $autocrlf; eol = $eol
        configuredFilterDrivers = @($filterDrivers)
        relevantConfiguration = @($relevant); relevantConfigurationSha256 =
            Get-GeoCeDGRepositoryIdentityHash (($relevant.ToArray()) -join "`0")
        filterPolicy = 'CONFIGURED_BUT_UNUSED_REPORTED; ACTIVE_FILTER_REJECTED' }
}

function Assert-GeoCeDGMaterializationAttributes {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Paths,
        [string[]]$ConfiguredFilterDrivers = @(),
        [switch]$Cached)
    if ($Paths.Count -eq 0) { return Get-GeoCeDGRepositoryIdentityHash '' }
    $arguments = @('check-attr', '-z')
    if ($Cached) { $arguments += '--cached' }
    $arguments += @('--stdin', 'text', 'eol', 'working-tree-encoding', 'ident', 'filter')
    $text = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        -RepositoryRoot $RepositoryRoot -Arguments $arguments -InputText (($Paths -join "`0") + "`0"))
    $items = $text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)
    Assert-GeoCeDGRepositoryIdentity ($items.Count -eq $Paths.Count * 15) 'Incomplete Git attribute audit.'
    for ($i = 0; $i -lt $items.Count; $i += 3) {
        $path = $items[$i]; $name = $items[$i + 1]; $value = $items[$i + 2]
        $valid = switch ($name) {
            'text' { $value -in @('set', 'unset', 'unspecified', 'auto') }
            'eol' { $value -in @('lf', 'crlf', 'unset', 'unspecified') }
            'working-tree-encoding' { $value -in @('unset', 'unspecified') }
            'ident' { $value -in @('unset', 'unspecified') }
            # check-attr renders -filter and filter=unset identically. Reject
            # even that reserved-looking value if a driver could be invoked.
            'filter' { $value -in @('unset', 'unspecified') -and $value -notin $ConfiguredFilterDrivers }
            default { $false }
        }
        Assert-GeoCeDGRepositoryIdentity $valid "Unsupported materialization attribute: $path $name=$value"
    }
    return Get-GeoCeDGRepositoryIdentityHash $text
}

function Assert-GeoCeDGWorktreeMaterialization {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ExpectedCommit,
        [string[]]$AllowedStatusPaths = @(),
        [object[]]$ConsumedUntrackedInputs = @())
    # A pending closeout caller may supply an already byte-validated, exhaustive
    # status overlay. This does not validate or authorize that overlay itself.
    # Clean explicit-commit proofs use the empty/default overlay.
    $allowed = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($path in $AllowedStatusPaths) {
        Assert-GeoCeDGRepositoryIdentityPath $path
        Assert-GeoCeDGRepositoryIdentity ($allowed.Add($path) -and
            [IO.Path]::GetFileName($path) -cne '.gitattributes') 'Unsafe or duplicate pending status overlay.'
    }
    $identity = Get-GeoCeDGRepositoryTrackedIdentity $RepositoryRoot $ExpectedCommit
    $config = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    $stageText = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('ls-files', '--stage', '-z'))
    $index = [Collections.Generic.Dictionary[string,object]]::new([StringComparer]::Ordinal)
    foreach ($row in $stageText.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        Assert-GeoCeDGRepositoryIdentity ($row -cmatch '^(\d{6}) ([0-9a-f]+) 0\t(.+)$') `
            'Unmerged or unsupported Git index record.'
        $mode = $Matches[1]; $oid = $Matches[2]; $path = $Matches[3]
        Assert-GeoCeDGRepositoryIdentityPath $path
        Assert-GeoCeDGRepositoryIdentity (-not $index.ContainsKey($path)) "Duplicate index path: $path"
        $index.Add($path, [pscustomobject]@{ mode = $mode; blobOid = $oid })
    }
    $expectedPaths = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $physicalNames = [Collections.Generic.HashSet[string]]::new(
        $(if ($IsWindows) { [StringComparer]::OrdinalIgnoreCase } else { [StringComparer]::Ordinal }))
    $inspectedPhysicalPaths = [Collections.Generic.HashSet[string]]::new(
        $(if ($IsWindows) { [StringComparer]::OrdinalIgnoreCase } else { [StringComparer]::Ordinal }))
    foreach ($entry in $identity.entries) { [void]$expectedPaths.Add($entry.path) }
    Assert-GeoCeDGRepositoryIdentity (@($index.Keys | Where-Object {
        -not $expectedPaths.Contains($_) -and -not $allowed.Contains($_)
    }).Count -eq 0) 'Index differs from expected commit tree (extra path).'
    foreach ($entry in $identity.entries) {
        Assert-GeoCeDGRepositoryIdentity ($physicalNames.Add($entry.path)) `
            "Tracked paths alias on the current filesystem: $($entry.path)"
        Assert-GeoCeDGRepositoryIdentity ($entry.kind -eq 'blob' -and $entry.mode -in @('100644', '100755')) `
            "Unsupported materialization mode: $($entry.path) $($entry.mode)"
        Assert-GeoCeDGRepositoryIdentity ($allowed.Contains($entry.path) -or ($index.ContainsKey($entry.path) -and
            $index[$entry.path].mode -ceq $entry.mode -and $index[$entry.path].blobOid -ceq $entry.blobOid) `
            ) `
            "Index differs from expected commit tree: $($entry.path)"
        $path = Join-Path $RepositoryRoot $entry.path
        $cursor = [IO.Path]::GetFullPath($path)
        $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('/', '\')
        while (-not $cursor.Equals($root, [StringComparison]::OrdinalIgnoreCase)) {
            if (-not $inspectedPhysicalPaths.Add($cursor)) { break }
            if (Test-Path -LiteralPath $cursor) {
                Assert-GeoCeDGRepositoryIdentity (-not ((Get-Item -LiteralPath $cursor -Force).Attributes -band
                    [IO.FileAttributes]::ReparsePoint)) "Tracked materialization traverses a reparse point: $($entry.path)"
            }
            $cursor = Split-Path -Parent $cursor
        }
    }
    $flags = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
        $RepositoryRoot @('ls-files', '-v', '-z'))
    foreach ($row in $flags.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries)) {
        Assert-GeoCeDGRepositoryIdentity ($row.StartsWith('H ', [StringComparison]::Ordinal)) `
            "Unsupported index visibility flag (assume-unchanged/skip-worktree): $row"
    }
    foreach ($path in $index.Keys) {
        Assert-GeoCeDGRepositoryIdentity ($index[$path].mode -in @('100644', '100755')) `
            "Unsupported index materialization mode: $path"
    }
    $paths = [string[]]@($expectedPaths)
    foreach ($path in $index.Keys) { if (-not $expectedPaths.Contains($path)) { $paths += $path } }
    [Array]::Sort($paths, [StringComparer]::Ordinal)
    # Both index and physical .gitattributes are audited before any command
    # capable of invoking clean filters. check-attr itself invokes no filters.
    $cachedAttributes = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot $paths `
        -ConfiguredFilterDrivers $config.configuredFilterDrivers -Cached
    $physicalAttributes = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot $paths `
        -ConfiguredFilterDrivers $config.configuredFilterDrivers
    Assert-GeoCeDGRepositoryIdentity ($cachedAttributes -ceq $physicalAttributes) `
        'Physical and index attribute authorities differ.'
    # Git diff can trust a stat-cache entry. Rehash each supported physical file
    # through Git's audited clean conversion as well (read-only, never -w), so
    # a same-size edit with restored mtime cannot hide behind that cache.
    $hashPaths = [string[]]@($paths | Where-Object { -not $allowed.Contains($_) })
    if ($hashPaths.Count -gt 0) {
        $hashText = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
            -RepositoryRoot $RepositoryRoot -Arguments @('hash-object', '--stdin-paths') `
            -InputText (($hashPaths -join "`n") + "`n"))
        $oids = @($hashText.TrimEnd("`r", "`n") -split "`n")
        Assert-GeoCeDGRepositoryIdentity ($oids.Count -eq $hashPaths.Count) 'Incomplete Git materialization blob proof.'
        $rawPaths = [Collections.Generic.List[string]]::new()
        for ($i = 0; $i -lt $hashPaths.Count; $i++) {
            if ($oids[$i].TrimEnd("`r") -cne $index[$hashPaths[$i]].blobOid) { $rawPaths.Add($hashPaths[$i]) }
        }
        # A historical Git blob may itself contain CRLF/mixed delimiters. An
        # exact byte copy of that blob is legitimate even when today's clean
        # conversion would renormalize it. Accept only exact raw Git OID in
        # this case, never arbitrary LF normalization of the historical blob.
        if ($rawPaths.Count -gt 0) {
            $rawText = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
                -RepositoryRoot $RepositoryRoot -Arguments @('hash-object', '--no-filters', '--stdin-paths') `
                -InputText (($rawPaths -join "`n") + "`n"))
            $rawOids = @($rawText.TrimEnd("`r", "`n") -split "`n")
            Assert-GeoCeDGRepositoryIdentity ($rawOids.Count -eq $rawPaths.Count) 'Incomplete raw Git blob proof.'
            for ($i = 0; $i -lt $rawPaths.Count; $i++) {
                Assert-GeoCeDGRepositoryIdentity ($rawOids[$i].TrimEnd("`r") -ceq $index[$rawPaths[$i]].blobOid) `
                    "Tracked worktree is not a clean Git materialization: $($rawPaths[$i])"
            }
        }
    }
    $diff = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit $RepositoryRoot `
        @('diff', '--name-only', '-z', '--no-ext-diff', '--no-textconv', '--ignore-submodules=none', '--'))
    $dirtyPaths = @($diff.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries))
    Assert-GeoCeDGRepositoryIdentity (@($dirtyPaths | Where-Object { -not $allowed.Contains($_) }).Count -eq 0) `
        'Tracked worktree is not a clean Git materialization.'
    $untrackedResult = Assert-GeoCeDGConsumedUntrackedInputs $RepositoryRoot $ConsumedUntrackedInputs
    $untrackedNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($input in $ConsumedUntrackedInputs) { [void]$untrackedNames.Add($input.path) }
    $otherText = ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit $RepositoryRoot `
        @('ls-files', '--others', '--exclude-standard', '-z'))
    $others = @($otherText.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries))
    Assert-GeoCeDGRepositoryIdentity (@($others | Where-Object {
        -not $allowed.Contains($_) -and -not $untrackedNames.Contains($_)
    }).Count -eq 0) 'Unauthenticated nonignored untracked input in current checkout.'
    return [pscustomobject][ordered]@{ schemaVersion = 1; expectedCommit = $ExpectedCommit
        treeOid = $identity.treeOid; trackedSha256 = $identity.sha256; trackedFiles = $identity.count
        indexMatchesCommitTreeOutsideStatusOverlay = $true; trackedWorktreeCleanOutsideStatusOverlay = $true
        allowedStatusPaths = @($AllowedStatusPaths); consumedUntracked = $untrackedResult
        attributesSha256 = $cachedAttributes; materializationConfiguration = $config
        physicalByteEqualityRequiredAcrossCheckout = $false }
}

function Assert-GeoCeDGConsumedUntrackedInputs {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [object[]]$Inputs)
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($input in $Inputs) {
        Assert-GeoCeDGRepositoryIdentityPath $input.path
        Assert-GeoCeDGRepositoryIdentity ($seen.Add($input.path)) "Duplicate consumed untracked path: $($input.path)"
        Assert-GeoCeDGRepositoryIdentity ($input.sha256 -cmatch '^[0-9a-f]{64}$' -and $input.bytes -ge 0) `
            "Invalid consumed untracked authority: $($input.path)"
        $tracked = Invoke-GeoCeDGRepositoryIdentityGit $RepositoryRoot `
            @('ls-files', '--error-unmatch', '--', $input.path) -AllowFailure
        Assert-GeoCeDGRepositoryIdentity ($tracked.ExitCode -eq 1) "Consumed untracked input is tracked: $($input.path)"
        $path = Join-Path $RepositoryRoot $input.path
        Assert-GeoCeDGRepositoryIdentity (Test-Path -LiteralPath $path -PathType Leaf) "Missing consumed untracked input: $($input.path)"
        $cursor = [IO.Path]::GetFullPath($path)
        $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('/', '\')
        while (-not $cursor.Equals($root, [StringComparison]::OrdinalIgnoreCase)) {
            Assert-GeoCeDGRepositoryIdentity (-not ((Get-Item -LiteralPath $cursor -Force).Attributes -band
                [IO.FileAttributes]::ReparsePoint)) "Consumed untracked input traverses a reparse point: $($input.path)"
            $cursor = Split-Path -Parent $cursor
        }
        $bytes = [IO.File]::ReadAllBytes($path)
        $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
        Assert-GeoCeDGRepositoryIdentity ($bytes.Length -eq $input.bytes -and $hash -ceq $input.sha256) `
            "Consumed untracked byte authority mismatch: $($input.path)"
    }
    return [pscustomobject]@{ count = $Inputs.Count; identity = 'EXACT_RAW_SHA256'; matched = $true }
}

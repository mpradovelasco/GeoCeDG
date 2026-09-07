#requires -Version 7.2

# Read-only phase lifecycle and historical technical-evidence linkage helpers.
# A historical evidence link is documentary authority only.  Nothing in this
# file creates, activates, or returns a verification-runtime build receipt.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

function Assert-GeoCeDGPhaseLifecycle {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Get-GeoCeDGPhaseLifecycleHash {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    if ($Bytes.Length -eq 0) {
        return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
    }
    return Get-GeoCeDGSha256FromBytes -Bytes $Bytes
}

function Assert-GeoCeDGPhaseLifecycleProperties {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string[]]$Names,
        [Parameter(Mandatory)] [string]$Description
    )
    $actual = @($Object.PSObject.Properties.Name)
    Assert-GeoCeDGPhaseLifecycle ($actual.Count -eq $Names.Count -and
        @($actual | Where-Object { $_ -cnotin $Names }).Count -eq 0 -and
        @($Names | Where-Object { $_ -cnotin $actual }).Count -eq 0) `
        "$Description has an unsupported or missing property."
}

function ConvertFrom-GeoCeDGPhaseLifecycleJson {
    param([Parameter(Mandatory)] [byte[]]$Bytes, [Parameter(Mandatory)] [string]$Description)
    $text = ConvertFrom-GeoCeDGStrictUtf8 -Bytes $Bytes
    try {
        $document = [Text.Json.JsonDocument]::Parse($text)
        try {
            $walk = $null
            $walk = {
                param([Text.Json.JsonElement]$Element)
                if ($Element.ValueKind -eq [Text.Json.JsonValueKind]::Object) {
                    $names = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
                    foreach ($property in $Element.EnumerateObject()) {
                        Assert-GeoCeDGPhaseLifecycle ($names.Add($property.Name)) `
                            "$Description contains duplicate JSON property '$($property.Name)'."
                        & $walk $property.Value
                    }
                } elseif ($Element.ValueKind -eq [Text.Json.JsonValueKind]::Array) {
                    foreach ($item in $Element.EnumerateArray()) { & $walk $item }
                }
            }
            & $walk $document.RootElement
        } finally { $document.Dispose() }
        return ($text | ConvertFrom-Json -Depth 100)
    } catch {
        throw "Invalid $Description JSON: $($_.Exception.Message)"
    }
}

function ConvertTo-GeoCeDGPhaseLifecyclePath {
    param([Parameter(Mandatory)] [string]$Path, [Parameter(Mandatory)] [string]$Description)
    Assert-GeoCeDGPhaseLifecycle (-not [string]::IsNullOrWhiteSpace($Path) -and
        -not [IO.Path]::IsPathRooted($Path) -and $Path -notmatch '[\x00-\x1f\x7f\\:]') `
        "Unsafe $Description path: $Path"
    $parts = @($Path -split '/')
    Assert-GeoCeDGPhaseLifecycle (@($parts | Where-Object { $_ -in @('', '.', '..') }).Count -eq 0) `
        "Unsafe $Description path: $Path"
    return $Path
}

function Resolve-GeoCeDGPhaseLifecycleChild {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$Description
    )
    $relative = ConvertTo-GeoCeDGPhaseLifecyclePath $RelativePath $Description
    $rootPath = [IO.Path]::GetFullPath($Root).TrimEnd('/', '\')
    $path = [IO.Path]::GetFullPath((Join-Path $rootPath $relative.Replace('/', '\')))
    Assert-GeoCeDGPhaseLifecycle ($path.StartsWith($rootPath + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase)) "Unsafe $Description path: $RelativePath"
    $cursor = $path
    while (-not $cursor.Equals($rootPath, [StringComparison]::OrdinalIgnoreCase)) {
        if (Test-Path -LiteralPath $cursor) {
            $item = Get-Item -LiteralPath $cursor -Force
            Assert-GeoCeDGPhaseLifecycle (-not ($item.Attributes -band [IO.FileAttributes]::ReparsePoint)) `
                "$Description traverses a reparse point: $RelativePath"
        }
        $cursor = Split-Path -Parent $cursor
    }
    return $path
}

function Invoke-GeoCeDGPhaseLifecycleGitText {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string[]]$Arguments)
    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot -Arguments $Arguments
    if (@($result.Bytes).Count -eq 0) { return '' }
    return (ConvertFrom-GeoCeDGStrictUtf8 $result.Bytes).TrimEnd("`r", "`n")
}

function Resolve-GeoCeDGPhaseLifecycleCommit {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string]$Commit)
    Assert-GeoCeDGPhaseLifecycle ($Commit -cmatch '^[0-9a-f]{40}$') "Expected a full lowercase commit SHA: $Commit"
    $resolved = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-parse', '--verify', "${Commit}^{commit}")
    Assert-GeoCeDGPhaseLifecycle ($resolved -ceq $Commit) "Commit authority does not resolve exactly: $Commit"
    return $resolved
}

function Get-GeoCeDGPhaseLifecycleBlobBytes {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [string]$Path
    )
    $safe = ConvertTo-GeoCeDGPhaseLifecyclePath $Path 'repository'
    return ,(Get-GeoCeDGFrozenBlobBytes -RepositoryRoot $RepositoryRoot -Commit $Commit -Path $safe)
}

function Get-GeoCeDGPhaseLifecycleChangedPaths {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$From,
        [Parameter(Mandatory)] [string]$To
    )
    if ($From -ceq $To) { return @() }
    $text = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('-c', 'core.quotepath=false',
        'diff', '--name-only', '--no-renames', $From, $To, '--')
    if ([string]::IsNullOrEmpty($text)) { return @() }
    return @($text -split "`n" | ForEach-Object {
        ConvertTo-GeoCeDGPhaseLifecyclePath $_ 'changed repository'
    } | Sort-Object -Unique -CaseSensitive)
}

function Assert-GeoCeDGPhaseCloseoutPathModes {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$ExistingPaths,
        [Parameter(Mandatory)] [string]$RecordPath,
        [string]$CloseoutCommit,
        [switch]$PendingCloseout
    )
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $ReviewedTechnicalCommit)
    if (-not $PendingCloseout) {
        Assert-GeoCeDGPhaseLifecycle (-not [string]::IsNullOrWhiteSpace($CloseoutCommit)) `
            'Committed closeout mode proof requires an exact closeout commit.'
        [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $CloseoutCommit)
    } else {
        $required = @($ExistingPaths + $RecordPath | Sort-Object -Unique -CaseSensitive)
        $sets = Get-GeoCeDGPhaseLifecycleStatusPaths $RepositoryRoot
        Assert-GeoCeDGPhaseLifecycleSet $sets.Staged $required 'Pending closeout staged mode authority'
        Assert-GeoCeDGPhaseLifecycle ($sets.Unstaged.Count -eq 0 -and $sets.Untracked.Count -eq 0) `
            'Pending closeout must be completely staged before mode verification.'
    }
    foreach ($candidate in $ExistingPaths) {
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath $candidate 'closeout mode'
        $beforeLine = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot `
            @('ls-tree', $ReviewedTechnicalCommit, '--', $path)
        $before = [regex]::Match($beforeLine,
            '^([0-7]{6}) blob [0-9a-f]{40}\t(.+)$', [Text.RegularExpressions.RegexOptions]::CultureInvariant)
        Assert-GeoCeDGPhaseLifecycle ($before.Success -and $before.Groups[2].Value -ceq $path) `
            "Reviewed closeout path has no exact Git mode authority: $path"
        $afterLine = if ($PendingCloseout) {
            Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('ls-files', '--stage', '--', $path)
        } else {
            Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('ls-tree', $CloseoutCommit, '--', $path)
        }
        $afterPattern = if ($PendingCloseout) {
            '^([0-7]{6}) [0-9a-f]{40} 0\t(.+)$'
        } else {
            '^([0-7]{6}) blob [0-9a-f]{40}\t(.+)$'
        }
        $after = [regex]::Match($afterLine, $afterPattern,
            [Text.RegularExpressions.RegexOptions]::CultureInvariant)
        Assert-GeoCeDGPhaseLifecycle ($after.Success -and $after.Groups[2].Value -ceq $path -and
            $after.Groups[1].Value -ceq $before.Groups[1].Value) `
            "Closeout changed tracked mode: $path"
    }
    $record = ConvertTo-GeoCeDGPhaseLifecyclePath $RecordPath 'closeout record mode'
    $recordLine = if ($PendingCloseout) {
        Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('ls-files', '--stage', '--', $record)
    } else {
        Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('ls-tree', $CloseoutCommit, '--', $record)
    }
    $recordPattern = if ($PendingCloseout) {
        '^100644 [0-9a-f]{40} 0\t(.+)$'
    } else {
        '^100644 blob [0-9a-f]{40}\t(.+)$'
    }
    $recordMatch = [regex]::Match($recordLine, $recordPattern,
        [Text.RegularExpressions.RegexOptions]::CultureInvariant)
    Assert-GeoCeDGPhaseLifecycle ($recordMatch.Success -and
        $recordMatch.Groups[1].Value -ceq $record) `
        'Closeout decision must be a normal non-executable tracked file.'
}

function Assert-GeoCeDGPhaseLifecycleSet {
    param([string[]]$Actual, [string[]]$Expected, [string]$Description)
    $a = @($Actual | Sort-Object -Unique -CaseSensitive)
    $e = @($Expected | Sort-Object -Unique -CaseSensitive)
    Assert-GeoCeDGPhaseLifecycle ($a.Count -eq $Actual.Count -and $e.Count -eq $Expected.Count -and
        $a.Count -eq $e.Count -and @($a | Where-Object { $_ -cnotin $e }).Count -eq 0) `
        "$Description mismatch."
}

function Read-GeoCeDGPhaseLifecyclePolicy {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string]$PolicyPath)
    $relativePolicy = ConvertTo-GeoCeDGRepositoryPath -RepositoryRoot $RepositoryRoot -Path $PolicyPath
    $path = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $relativePolicy 'policy'
    Assert-GeoCeDGPhaseLifecycle (Test-Path -LiteralPath $path -PathType Leaf) "Lifecycle policy is missing: $PolicyPath"
    $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($path)) 'lifecycle policy'
    Assert-GeoCeDGPhaseLifecycleProperties $policy @('schemaVersion', 'phase', 'entryCommit',
        'implementationCommit', 'implementationTree', 'implementationPaths',
        'infrastructureFollowupPaths', 'maximumInfrastructureCommits', 'closeout') 'Lifecycle policy'
    Assert-GeoCeDGPhaseLifecycle ($policy.schemaVersion -is [long] -and $policy.schemaVersion -eq 1) `
        'Unsupported lifecycle-policy schema.'
    foreach ($name in @('entryCommit', 'implementationCommit', 'implementationTree')) {
        Assert-GeoCeDGPhaseLifecycle ([string]$policy.$name -cmatch '^[0-9a-f]{40}$') "Invalid policy $name."
    }
    Assert-GeoCeDGPhaseLifecycle ($policy.maximumInfrastructureCommits -is [long] -and
        $policy.maximumInfrastructureCommits -ge 0 -and $policy.maximumInfrastructureCommits -le 4) `
        'Invalid infrastructure commit bound.'
    Assert-GeoCeDGPhaseLifecycleProperties $policy.closeout @('recordPath', 'literalReplacements',
        'canonicalLfHashManifests') 'Lifecycle closeout policy'
    [void](ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$policy.closeout.recordPath) 'closeout record')
    return $policy
}

function Assert-GeoCeDGPhaseImplementationAuthority {
    param([string]$RepositoryRoot, [object]$Policy)
    $implementation = Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot ([string]$Policy.implementationCommit)
    $parentText = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-list', '--parents', '-n', '1',
        $implementation)
    $parents = @($parentText -split ' ')
    Assert-GeoCeDGPhaseLifecycle ($parents.Count -eq 2 -and $parents[1] -ceq [string]$Policy.entryCommit) `
        'Implementation commit is not the required single-parent child of entry authority.'
    $tree = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-parse', "${implementation}^{tree}")
    Assert-GeoCeDGPhaseLifecycle ($tree -ceq [string]$Policy.implementationTree) 'Implementation tree changed.'
    $changed = Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot ([string]$Policy.entryCommit) $implementation
    $declared = @($Policy.implementationPaths | ForEach-Object {
        Assert-GeoCeDGPhaseLifecycleProperties $_ @('path', 'sha256') 'Implementation-path record'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$_.path) 'implementation'
        Assert-GeoCeDGPhaseLifecycle ([string]$_.sha256 -cmatch '^[0-9a-f]{64}$') "Invalid blob hash: $path"
        $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $implementation $path
        Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $bytes) -ceq [string]$_.sha256) `
            "Implementation blob hash changed: $path"
        $path
    })
    Assert-GeoCeDGPhaseLifecycleSet $changed $declared 'Implementation path authority'
}

function Assert-GeoCeDGPhaseInfrastructureHistory {
    param([string]$RepositoryRoot, [object]$Policy, [string]$Head)
    $implementation = [string]$Policy.implementationCommit
    $ancestor = Invoke-GeoCeDGGitByteCommand $RepositoryRoot @('merge-base', '--is-ancestor', $implementation, $Head) -AllowFailure
    Assert-GeoCeDGPhaseLifecycle ($ancestor.ExitCode -eq 0) 'Implementation authority is not ancestral.'
    $commitsText = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-list', '--reverse',
        "${implementation}..${Head}")
    $commits = @(if ([string]::IsNullOrEmpty($commitsText)) { @() } else { @($commitsText -split "`n") })
    Assert-GeoCeDGPhaseLifecycle ($commits.Count -le [int]$Policy.maximumInfrastructureCommits) `
        'Too many infrastructure follow-up commits.'
    $allowed = @($Policy.infrastructureFollowupPaths | ForEach-Object {
        ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$_) 'infrastructure'
    })
    Assert-GeoCeDGPhaseLifecycle ($allowed.Count -eq @($allowed | Sort-Object -Unique -CaseSensitive).Count) `
        'Duplicate infrastructure path authority.'
    $parent = $implementation
    foreach ($commit in $commits) {
        $lineText = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-list', '--parents', '-n', '1', $commit)
        $line = @($lineText -split ' ')
        Assert-GeoCeDGPhaseLifecycle ($line.Count -eq 2 -and $line[1] -ceq $parent) `
            'Infrastructure history is not a linear single-parent chain.'
        $changed = Get-GeoCeDGPhaseLifecycleChangedPaths $RepositoryRoot $parent $commit
        Assert-GeoCeDGPhaseLifecycle (@($changed | Where-Object { $_ -cnotin $allowed }).Count -eq 0) `
            'Infrastructure history changed a forbidden path.'
        $parent = $commit
    }
}

function Get-GeoCeDGPhaseRawInputSnapshot {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    $runtimePath = Join-Path $PSScriptRoot 'verification-runtime.psm1'
    $module = Import-Module $runtimePath -PassThru
    # Get-RawInputInventory is deliberately invoked inside its defining module.
    # It is a pure read-only inventory operation and does not inspect or mutate
    # the module's private current-run receipt capability.
    return & $module { param($Root) Get-RawInputInventory -RepositoryRoot $Root } $RepositoryRoot
}

function Get-GeoCeDGPhaseCommitIndexAuthority {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string]$Commit)
    $text = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('-c', 'core.quotepath=false',
        'ls-tree', '-r', '--full-tree', $Commit)
    $paths = [Collections.Generic.List[string]]::new()
    $indexLines = [Collections.Generic.List[string]]::new()
    if (-not [string]::IsNullOrEmpty($text)) {
        foreach ($line in @($text -split "`n")) {
            $match = [regex]::Match($line, '^([0-7]{6}) (blob|commit) ([0-9a-f]{40})\t(.+)$')
            Assert-GeoCeDGPhaseLifecycle ($match.Success -and $match.Groups[2].Value -ceq 'blob') `
                'Technical tree contains an unsupported entry.'
            $path = ConvertTo-GeoCeDGPhaseLifecyclePath $match.Groups[4].Value 'technical tree'
            $paths.Add($path)
            $indexLines.Add((('{0} {1} 0' + "`t" + '{2}') -f $match.Groups[1].Value,
                $match.Groups[3].Value, $path))
        }
    }
    $joined = $indexLines -join "`n"
    return [pscustomobject]@{
        Paths = @($paths)
        IndexSha256 = Get-GeoCeDGPhaseLifecycleHash ([Text.UTF8Encoding]::new($false).GetBytes($joined))
    }
}

function Assert-GeoCeDGPhaseRawEvidenceClosure {
    param(
        [string]$RepositoryRoot,
        [string]$TechnicalCommit,
        [object]$Receipt,
        [object[]]$BundleEntries,
        [string]$BundleDirectory,
        [string[]]$MetadataPaths,
        [string]$CloseoutCommit,
        [switch]$PendingCloseout
    )
    $identityJson = $Receipt.inputIdentity | ConvertTo-Json -Depth 20 -Compress
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($identityJson))) -ceq [string]$Receipt.inputFingerprint) `
        'Receipt input fingerprint is internally inconsistent.'
    $emptyHash = Get-GeoCeDGPhaseLifecycleHash ([byte[]]::new(0))
    Assert-GeoCeDGPhaseLifecycle ([string]$Receipt.inputIdentity.statusSha256 -ceq $emptyHash) `
        'Reviewed technical evidence was not produced from a clean status.'
    $tree = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot $TechnicalCommit
    Assert-GeoCeDGPhaseLifecycle ([string]$Receipt.inputIdentity.indexSha256 -ceq $tree.IndexSha256) `
        'Receipt index does not represent the exact technical commit.'
    $inventoryReferences = @($Receipt.auditArtifacts | Where-Object {
        [string]$_.path -cmatch '(?i)(?:^|[\\/])input-inventory\.json$'
    })
    Assert-GeoCeDGPhaseLifecycle ($inventoryReferences.Count -eq 1) 'Receipt has no unique raw input inventory.'
    $reference = $inventoryReferences[0]
    $entry = @($BundleEntries | Where-Object { $_.role -ceq 'ARTIFACT' -and
        [string]$_.recordedPath -ceq [string]$reference.path })
    Assert-GeoCeDGPhaseLifecycle ($entry.Count -eq 1 -and
        [string]$entry[0].sha256 -ceq [string]$reference.sha256) 'Raw input inventory is not hash-bound.'
    $inventoryPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$entry[0].path) 'input inventory'
    $inventory = @(ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($inventoryPath)) 'input inventory')
    $paths = [Collections.Generic.List[string]]::new(); [long]$bytes = 0
    foreach ($record in $inventory) {
        Assert-GeoCeDGPhaseLifecycleProperties $record @('path', 'exists', 'bytes', 'sha256') 'Input-inventory record'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$record.path) 'input inventory'
        Assert-GeoCeDGPhaseLifecycle ($record.exists -is [bool] -and $record.exists -and
            $record.bytes -is [long] -and $record.bytes -ge 0 -and
            [string]$record.sha256 -cmatch '^[0-9a-f]{64}$') "Invalid input-inventory record: $path"
        $paths.Add($path); $bytes += [long]$record.bytes
    }
    Assert-GeoCeDGPhaseLifecycleSet @($paths) @($tree.Paths) 'Technical input/tree paths'
    $inventoryJson = @($inventory) | ConvertTo-Json -Depth 10 -Compress
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($inventoryJson))) -ceq
        [string]$Receipt.inputIdentity.rawTreeSha256 -and
        $inventory.Count -eq [int]$Receipt.inputIdentity.rawFiles -and
        $bytes -eq [long]$Receipt.inputIdentity.rawBytes) 'Raw input inventory summary is inconsistent.'
    # The inventory above is the immutable physical cohort actually executed.
    # It is NOT a durable byte representation for a later Git checkout (ADR 0024).
    # The caller separately proves the exact allowed status-content projection.
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    if ($PendingCloseout) {
        [void](Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $RepositoryRoot `
            -ExpectedCommit $TechnicalCommit -AllowedStatusPaths $MetadataPaths)
    } else {
        [void](Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $RepositoryRoot `
            -ReviewedCommit $TechnicalCommit -CloseoutCommit $CloseoutCommit -AllowedPaths $MetadataPaths)
        [void](Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $RepositoryRoot `
            -ExpectedCommit $CloseoutCommit)
    }
}

function Get-GeoCeDGPhaseLifecycleStatusPaths {
    param([string]$RepositoryRoot)
    $commands = @(
        @('diff', '--name-only', '--no-renames', '--'),
        @('diff', '--cached', '--name-only', '--no-renames', '--'),
        @('ls-files', '--others', '--exclude-standard')
    )
    $sets = [ordered]@{}
    $names = @('Unstaged', 'Staged', 'Untracked')
    $index = 0
    foreach ($arguments in $commands) {
        $text = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot $arguments
        $sets[$names[$index]] = @(if ([string]::IsNullOrEmpty($text)) { @() } else {
            @($text -split "`n" | ForEach-Object {
                ConvertTo-GeoCeDGPhaseLifecyclePath $_ 'working-tree status'
            })
        })
        $index++
    }
    return [pscustomobject]$sets
}

function Get-GeoCeDGPhaseManifestAuthorityPaths {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$AuthorityCommit,
        [Parameter(Mandatory)] [string]$ManifestPath,
        [Parameter(Mandatory)] [long]$ExpectedPathCount,
        [Parameter(Mandatory)] [string]$ExpectedPathListSha256,
        [Parameter(Mandatory)] [long]$PreserveLeadingComments
    )
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $AuthorityCommit)
    Assert-GeoCeDGPhaseLifecycle ($ExpectedPathCount -gt 0) 'Hash-manifest authority path count is invalid.'
    Assert-GeoCeDGPhaseLifecycle ($ExpectedPathListSha256 -cmatch '^[0-9a-f]{64}$') `
        'Hash-manifest authority path-list hash is invalid.'
    Assert-GeoCeDGPhaseLifecycle ($PreserveLeadingComments -gt 0) `
        'The bounded hash-manifest authority must preserve a positive exact leading-comment count.'
    $path = ConvertTo-GeoCeDGPhaseLifecyclePath $ManifestPath 'hash-manifest authority'
    $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $AuthorityCommit $path
    $text = ConvertFrom-GeoCeDGStrictUtf8 (ConvertTo-GeoCeDGCanonicalLfBytes $bytes)
    $lines = @($text -split "`n")
    if ($lines.Count -gt 0 -and $lines[-1] -ceq '') {
        $lines = if ($lines.Count -eq 1) { @() } else { @($lines[0..($lines.Count - 2)]) }
    }
    $comments = [Collections.Generic.List[string]]::new()
    $paths = [Collections.Generic.List[string]]::new()
    $recordsStarted = $false
    foreach ($line in $lines) {
        if (-not $recordsStarted -and $line.StartsWith('#', [StringComparison]::Ordinal)) {
            $comments.Add($line)
            continue
        }
        $recordsStarted = $true
        $match = [regex]::Match($line, '^([0-9a-f]{64})  (.+)$')
        Assert-GeoCeDGPhaseLifecycle $match.Success `
            "Malformed canonical-LF hash-manifest authority: $path"
        $paths.Add((ConvertTo-GeoCeDGPhaseLifecyclePath $match.Groups[2].Value 'hash authority'))
    }
    Assert-GeoCeDGPhaseLifecycle ($comments.Count -eq [int]$PreserveLeadingComments) `
        'Hash-manifest authority leading-comment count changed.'
    Assert-GeoCeDGPhaseLifecycle ($paths.Count -eq [int]$ExpectedPathCount -and
        $paths.Count -eq @($paths | Sort-Object -Unique -CaseSensitive).Count) `
        'Hash-manifest authority path count or uniqueness changed.'
    $pathBytes = [Text.UTF8Encoding]::new($false).GetBytes((@($paths) -join "`n") + "`n")
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $pathBytes) -ceq
        $ExpectedPathListSha256) 'Hash-manifest authority path-list hash changed.'
    return [pscustomobject][ordered]@{
        Comments = @($comments)
        Paths = @($paths)
        PathCount = $paths.Count
        PathListSha256 = $ExpectedPathListSha256
    }
}

function Get-GeoCeDGPhaseExpectedCloseoutBytes {
    param([string]$RepositoryRoot, [string]$TechnicalCommit, [object]$Policy)
    $expected = [ordered]@{}
    foreach ($rule in @($Policy.closeout.literalReplacements)) {
        Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'before', 'after', 'occurrences') 'Literal replacement'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$rule.path) 'replacement'
        Assert-GeoCeDGPhaseLifecycle ($rule.occurrences -is [long] -and $rule.occurrences -gt 0) `
            "Invalid replacement count: $path"
        $bytes = if ($expected.Contains($path)) { [byte[]]$expected[$path] } else {
            Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $TechnicalCommit $path
        }
        $text = ConvertFrom-GeoCeDGStrictUtf8 $bytes
        $matches = [regex]::Matches($text, [regex]::Escape([string]$rule.before)).Count
        Assert-GeoCeDGPhaseLifecycle ($matches -eq [int]$rule.occurrences -and
            -not [string]::IsNullOrEmpty([string]$rule.before)) "Replacement authority mismatch: $path"
        $result = $text.Replace([string]$rule.before, [string]$rule.after)
        $expected[$path] = [Text.UTF8Encoding]::new($false).GetBytes($result)
    }
    foreach ($rule in @($Policy.closeout.canonicalLfHashManifests)) {
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$rule.path) 'hash manifest'
        Assert-GeoCeDGPhaseLifecycle (-not $expected.Contains($path)) "Duplicate closeout output: $path"
        $properties = @($rule.PSObject.Properties.Name)
        $authorityPaths = @()
        $leadingComments = @()
        $unchangedAuthorityCommit = $TechnicalCommit
        if ($properties.Count -eq 2 -and 'path' -cin $properties -and 'authorityPaths' -cin $properties) {
            # Schema-v1/R1 behavior remains byte-for-byte compatible: the
            # policy itself supplies the complete ordered path list and no
            # comment header is introduced.
            Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'authorityPaths') 'Hash-manifest rule'
            $authorityPaths = @($rule.authorityPaths)
        } else {
            Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'authorityCommit', 'authorityPaths',
                'authorityPathCount', 'authorityPathListSha256', 'preserveLeadingComments') `
                'Authority-backed hash-manifest rule'
            $authorityCommit = Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot ([string]$rule.authorityCommit)
            Assert-GeoCeDGPhaseLifecycle ($authorityCommit -ceq [string]$Policy.implementationCommit) `
                'Hash-manifest path-list authority is not the exact implementation checkpoint.'
            $unchangedAuthorityCommit = $authorityCommit
            Assert-GeoCeDGPhaseLifecycle ($rule.authorityPathCount -is [long] -and
                $rule.preserveLeadingComments -is [long] -and
                $rule.preserveLeadingComments -gt 0) 'Invalid authority-backed hash-manifest rule.'
            $authority = Get-GeoCeDGPhaseManifestAuthorityPaths -RepositoryRoot $RepositoryRoot `
                -AuthorityCommit $authorityCommit -ManifestPath $path `
                -ExpectedPathCount ([long]$rule.authorityPathCount) `
                -ExpectedPathListSha256 ([string]$rule.authorityPathListSha256) `
                -PreserveLeadingComments ([long]$rule.preserveLeadingComments)
            $declaredPaths = @($rule.authorityPaths | ForEach-Object {
                ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$_) 'declared hash authority'
            })
            Assert-GeoCeDGPhaseLifecycle ($declaredPaths.Count -eq $authority.Paths.Count -and
                ($declaredPaths -join "`n") -ceq (@($authority.Paths) -join "`n")) `
                'Declared hash-manifest authority path order changed.'
            $authorityPaths = $declaredPaths
            $leadingComments = @($authority.Comments)
        }
        $lines = foreach ($authority in $authorityPaths) {
            $authorityPath = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$authority) 'hash authority'
            $authorityBytes = if ($expected.Contains($authorityPath)) { [byte[]]$expected[$authorityPath] } else {
                Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $unchangedAuthorityCommit $authorityPath
            }
            $hash = Get-GeoCeDGPhaseLifecycleHash (ConvertTo-GeoCeDGCanonicalLfBytes $authorityBytes)
            "$hash  $authorityPath"
        }
        $outputLines = @($leadingComments) + @($lines)
        $expected[$path] = [Text.UTF8Encoding]::new($false).GetBytes(($outputLines -join "`n") + "`n")
    }
    return $expected
}

function Assert-GeoCeDGPhaseCloseoutRecord {
    param([object]$Record, [object]$Policy, [string]$TechnicalCommit, [string]$BundleSha256)
    Assert-GeoCeDGPhaseLifecycleProperties $Record @('schemaVersion', 'phase', 'mode',
        'reviewedTechnicalCommit', 'authorDecision', 'evidence', 'selfApproved') 'Closeout record'
    Assert-GeoCeDGPhaseLifecycleProperties $Record.evidence @('bundleDirectory', 'bundleManifestPath',
        'bundleManifestSha256') 'Closeout evidence record'
    Assert-GeoCeDGPhaseLifecycle ($Record.schemaVersion -is [long] -and $Record.schemaVersion -eq 1 -and
        [string]$Record.phase -ceq [string]$Policy.phase -and [string]$Record.mode -ceq 'AUTHOR_CLOSEOUT' -and
        [string]$Record.reviewedTechnicalCommit -ceq $TechnicalCommit -and
        [string]$Record.authorDecision -ceq 'PASS_AUTHOR_APPROVED' -and
        $Record.selfApproved -is [bool] -and -not $Record.selfApproved -and
        [string]$Record.evidence.bundleManifestSha256 -ceq $BundleSha256) 'Invalid author-closeout record.'
    [void](ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$Record.evidence.bundleManifestPath) 'bundle manifest')
    [void](ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$Record.evidence.bundleDirectory) 'bundle directory')
}

function Get-GeoCeDGPhaseBundleFile {
    param([string]$BundleDirectory, [object]$Entry)
    Assert-GeoCeDGPhaseLifecycleProperties $Entry @('role', 'path', 'recordedPath', 'sha256') 'Bundle-file record'
    Assert-GeoCeDGPhaseLifecycle ([string]$Entry.role -cmatch '^[A-Z][A-Z0-9_]*$' -and
        [string]$Entry.sha256 -cmatch '^[0-9a-f]{64}$') 'Invalid bundle-file record.'
    $path = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$Entry.path) 'bundle'
    Assert-GeoCeDGPhaseLifecycle (Test-Path -LiteralPath $path -PathType Leaf) "Missing evidence bundle file: $($Entry.path)"
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash ([IO.File]::ReadAllBytes($path))) -ceq
        [string]$Entry.sha256) "Evidence bundle file hash mismatch: $($Entry.path)"
    return $path
}

function Get-GeoCeDGPhaseUniqueBundleRole {
    param(
        [Parameter(Mandatory)] [object[]]$Entries,
        [Parameter(Mandatory)] [string]$Role
    )
    $matches = @($Entries | Where-Object { [string]$_.role -ceq $Role })
    Assert-GeoCeDGPhaseLifecycle ($matches.Count -eq 1) `
        "Evidence bundle must contain exactly one $Role."
    return $matches[0]
}

function Assert-GeoCeDGPhaseInputIdentityRepairSummary {
    param(
        [Parameter(Mandatory)] [object]$Document,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [object]$StructuralProof,
        [Parameter(Mandatory)] [string]$ExpectedPolicyCanonicalLfSha256
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Document @('schemaVersion', 'kind',
        'reviewedTechnicalCommit', 'closeoutCommit', 'policyCanonicalLfSha256', 'suites',
        'tests', 'passed', 'failed', 'executionPlanEquivalent', 'reviewedExecutionPlanSha256',
        'candidateExecutionPlanSha256', 'unchangedExecutionInputCount',
        'sameRunPhysicalInputsUnchanged', 'parser', 'whitespace', 'scientificExecutionRepeated',
        'heavyEvidenceReuseApprovedByThisRunAlone', 'authorApprovalInferred') `
        'Input-identity focused summary'
    Assert-GeoCeDGPhaseLifecycle ($Document.schemaVersion -is [long] -and
        $Document.schemaVersion -eq 1 -and [string]$Document.kind -ceq
        'INPUT_IDENTITY_REPAIR_FOCUSED_INFRASTRUCTURE' -and
        [string]$Document.reviewedTechnicalCommit -ceq [string]$Policy.implementationCommit -and
        [string]$Document.closeoutCommit -ceq [string]$Policy.implementationCommit -and
        [string]$Document.policyCanonicalLfSha256 -ceq $ExpectedPolicyCanonicalLfSha256 -and
        $Document.tests -is [double] -and $Document.tests -gt 0 -and
        $Document.tests -eq [math]::Truncate([double]$Document.tests) -and
        $Document.passed -is [double] -and $Document.passed -eq $Document.tests -and
        $Document.failed -is [long] -and $Document.failed -eq 0 -and
        $Document.executionPlanEquivalent -is [bool] -and $Document.executionPlanEquivalent -and
        [string]$Document.reviewedExecutionPlanSha256 -ceq
            [string]$StructuralProof.reviewedExecutionPlanSha256 -and
        [string]$Document.candidateExecutionPlanSha256 -ceq
            [string]$StructuralProof.candidateExecutionPlanSha256 -and
        [long]$Document.unchangedExecutionInputCount -eq
            [long]$StructuralProof.unchangedExecutionInputCount -and
        $Document.sameRunPhysicalInputsUnchanged -is [bool] -and
            $Document.sameRunPhysicalInputsUnchanged -and
        [string]$Document.parser -ceq 'PASS' -and [string]$Document.whitespace -ceq 'PASS' -and
        $Document.scientificExecutionRepeated -is [bool] -and
            -not $Document.scientificExecutionRepeated -and
        $Document.heavyEvidenceReuseApprovedByThisRunAlone -is [bool] -and
            -not $Document.heavyEvidenceReuseApprovedByThisRunAlone -and
        $Document.authorApprovalInferred -is [bool] -and -not $Document.authorApprovalInferred) `
        'Input-identity focused summary is not exact passing infrastructure evidence.'
    $expectedSuites = @('generated-state', 'phase-lifecycle', 'repair-equivalence',
        'repository-identity', 'verification-runtime')
    $suites = @($Document.suites)
    Assert-GeoCeDGPhaseLifecycleSet @($suites | ForEach-Object { [string]$_.suite }) `
        $expectedSuites 'Input-identity focused suites'
    [long]$tests = 0
    foreach ($suite in $suites) {
        Assert-GeoCeDGPhaseLifecycleProperties $suite @('suite', 'tests', 'passed', 'failed', 'results') `
            'Input-identity focused suite'
        $results = @($suite.results)
        Assert-GeoCeDGPhaseLifecycle (($suite.tests -is [long] -or $suite.tests -is [double]) -and
            [double]$suite.tests -gt 0 -and [double]$suite.tests -eq
            [math]::Truncate([double]$suite.tests) -and
            ($suite.passed -is [long] -or $suite.passed -is [double]) -and
            [double]$suite.passed -eq [double]$suite.tests -and
            $suite.failed -is [long] -and $suite.failed -eq 0 -and
            $results.Count -eq [int]$suite.tests -and
            @($results | Where-Object { [string]$_.status -cne 'PASS' }).Count -eq 0) `
            "Input-identity focused suite failed or is incomplete: $($suite.suite)"
        $tests += [long]$suite.tests
    }
    Assert-GeoCeDGPhaseLifecycle ($tests -eq [long]$Document.tests) `
        'Input-identity focused suite counters disagree.'
}

function Assert-GeoCeDGPhaseG9U1LifecycleSummary {
    param(
        [Parameter(Mandatory)] [object]$Document,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [string[]]$ExpectedTestNames
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Document @('schemaVersion', 'evidenceKind', 'tests',
        'passed', 'failed', 'results', 'productCheckpoint', 'productBytesChanged',
        'authorApprovalInferred', 'priorExecutionRelabeled') 'G9U1 lifecycle focused summary'
    $results = @($Document.results)
    Assert-GeoCeDGPhaseLifecycle ($Document.schemaVersion -is [long] -and
        $Document.schemaVersion -eq 1 -and [string]$Document.evidenceKind -ceq
        'G9U1_LIFECYCLE_FOCUSED_OPERATIONAL_TESTS_NOT_PRODUCT_EXECUTION' -and
        [string]$Document.productCheckpoint -ceq [string]$Policy.implementationCommit -and
        $Document.tests -is [long] -and $Document.tests -eq $ExpectedTestNames.Count -and
        $Document.passed -is [long] -and $Document.passed -eq $Document.tests -and
        $Document.failed -is [long] -and $Document.failed -eq 0 -and
        $results.Count -eq [int]$Document.tests -and
        @($results | Where-Object { [string]$_.status -cne 'PASS' }).Count -eq 0 -and
        $Document.productBytesChanged -is [bool] -and -not $Document.productBytesChanged -and
        $Document.authorApprovalInferred -is [bool] -and -not $Document.authorApprovalInferred -and
        $Document.priorExecutionRelabeled -is [bool] -and -not $Document.priorExecutionRelabeled) `
        'G9U1 lifecycle focused summary is not exact passing infrastructure evidence.'
    Assert-GeoCeDGPhaseLifecycleSet @($results | ForEach-Object { [string]$_.name }) `
        $ExpectedTestNames 'G9U1 lifecycle focused tests'
}

function Assert-GeoCeDGPhaseCheckpointBridge {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [object]$Authority,
        [Parameter(Mandatory)] [object]$HistoricalExecution,
        [Parameter(Mandatory)] [object[]]$HistoricalInventory,
        [Parameter(Mandatory)] [object]$Checkpoint,
        [Parameter(Mandatory)] [object[]]$CheckpointInventory,
        [Parameter(Mandatory)] [object]$Materialization
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Checkpoint @('schemaVersion', 'kind',
        'productCheckpoint', 'productTree', 'parentCommit', 'branch', 'indexTree',
        'trackedWorktreeClean', 'statusPorcelain', 'rawTreeSha256', 'rawFiles', 'rawBytes',
        'inventoryFile', 'inventoryFileSha256', 'historicalExecutionHead', 'provenanceRule',
        'authorApprovedPhase', 'passClaimed', 'selfApproved') 'G9U1 checkpoint authority'
    $inventoryAuthority = @($Authority.artifacts | Where-Object {
        [string]$_.role -ceq 'CHECKPOINT_INPUT_INVENTORY'
    })
    Assert-GeoCeDGPhaseLifecycle ($inventoryAuthority.Count -eq 1) `
        'Checkpoint inventory hash authority is missing or duplicated.'
    Assert-GeoCeDGPhaseLifecycle ($Checkpoint.schemaVersion -is [long] -and
        $Checkpoint.schemaVersion -eq 1 -and [string]$Checkpoint.kind -ceq
        'G9U1_AUTHOR_REVIEWED_PRODUCT_CHECKPOINT_INPUT_FREEZE' -and
        [string]$Checkpoint.productCheckpoint -ceq [string]$Policy.implementationCommit -and
        [string]$Checkpoint.productTree -ceq [string]$Policy.implementationTree -and
        [string]$Checkpoint.parentCommit -ceq [string]$Policy.entryCommit -and
        [string]$Checkpoint.indexTree -ceq [string]$Policy.implementationTree -and
        $Checkpoint.trackedWorktreeClean -is [bool] -and $Checkpoint.trackedWorktreeClean -and
        @($Checkpoint.statusPorcelain).Count -eq 0 -and
        [string]$Checkpoint.rawTreeSha256 -ceq [string]$HistoricalExecution.rawTreeSha256 -and
        [long]$Checkpoint.rawFiles -eq [long]$HistoricalExecution.rawFiles -and
        [long]$Checkpoint.rawBytes -eq [long]$HistoricalExecution.rawBytes -and
        [string]$Checkpoint.inventoryFile -ceq 'input-inventory.json' -and
        [string]$Checkpoint.inventoryFileSha256 -ceq [string]$inventoryAuthority[0].sha256 -and
        [string]$Checkpoint.historicalExecutionHead -ceq [string]$Policy.entryCommit -and
        [string]$Checkpoint.provenanceRule -cmatch 'does not reattribute' -and
        $Checkpoint.authorApprovedPhase -is [bool] -and -not $Checkpoint.authorApprovedPhase -and
        $Checkpoint.passClaimed -is [bool] -and -not $Checkpoint.passClaimed -and
        $Checkpoint.selfApproved -is [bool] -and -not $Checkpoint.selfApproved) `
        'G9U1 checkpoint authority changed or relabelled execution.'
    Assert-GeoCeDGPhaseLifecycleProperties $Materialization @('schemaVersion', 'expectedCommit',
        'treeOid', 'trackedSha256', 'trackedFiles', 'indexMatchesCommitTreeOutsideStatusOverlay',
        'trackedWorktreeCleanOutsideStatusOverlay', 'allowedStatusPaths', 'consumedUntracked',
        'attributesSha256', 'materializationConfiguration', 'physicalByteEqualityRequiredAcrossCheckout') `
        'G9U1 checkpoint materialization proof'
    Assert-GeoCeDGPhaseLifecycle ($Materialization.schemaVersion -is [long] -and
        $Materialization.schemaVersion -eq 1 -and [string]$Materialization.expectedCommit -ceq
        [string]$Policy.implementationCommit -and [string]$Materialization.treeOid -ceq
        [string]$Policy.implementationTree -and [long]$Materialization.trackedFiles -eq
        [long]$HistoricalExecution.rawFiles -and
        $Materialization.indexMatchesCommitTreeOutsideStatusOverlay -is [bool] -and
        $Materialization.indexMatchesCommitTreeOutsideStatusOverlay -and
        $Materialization.trackedWorktreeCleanOutsideStatusOverlay -is [bool] -and
        $Materialization.trackedWorktreeCleanOutsideStatusOverlay -and
        @($Materialization.allowedStatusPaths).Count -eq 0 -and
        [long]$Materialization.consumedUntracked.count -eq 0 -and
        [string]$Materialization.consumedUntracked.identity -ceq 'EXACT_RAW_SHA256' -and
        $Materialization.consumedUntracked.matched -is [bool] -and
        $Materialization.consumedUntracked.matched -and
        $Materialization.physicalByteEqualityRequiredAcrossCheckout -is [bool] -and
        -not $Materialization.physicalByteEqualityRequiredAcrossCheckout) `
        'G9U1 checkpoint was not a valid clean Git materialization.'
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $tracked = Get-GeoCeDGRepositoryTrackedIdentity -RepositoryRoot $RepositoryRoot `
        -Commit ([string]$Policy.implementationCommit)
    Assert-GeoCeDGPhaseLifecycle ([string]$tracked.treeOid -ceq [string]$Materialization.treeOid -and
        [string]$tracked.sha256 -ceq [string]$Materialization.trackedSha256 -and
        [long]$tracked.count -eq [long]$Materialization.trackedFiles) `
        'Checkpoint materialization Git identity is not the actual product tree.'
    $checkpointPaths = [Collections.Generic.List[string]]::new()
    foreach ($record in $CheckpointInventory) {
        Assert-GeoCeDGPhaseLifecycleProperties $record @('path', 'exists', 'bytes', 'sha256') `
            'Checkpoint input-inventory record'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$record.path) `
            'checkpoint input inventory'
        Assert-GeoCeDGPhaseLifecycle ($record.exists -is [bool] -and $record.exists -and
            $record.bytes -is [long] -and $record.bytes -ge 0 -and
            [string]$record.sha256 -cmatch '^[0-9a-f]{64}$') `
            "Invalid checkpoint input-inventory record: $path"
        $checkpointPaths.Add($path)
    }
    Assert-GeoCeDGPhaseLifecycle ($checkpointPaths.Count -eq
        @($checkpointPaths | Sort-Object -Unique -CaseSensitive).Count) `
        'Checkpoint input inventory contains duplicate paths.'
    Assert-GeoCeDGPhaseLifecycle ($HistoricalInventory.Count -eq $CheckpointInventory.Count -and
        $HistoricalInventory.Count -eq [int]$HistoricalExecution.rawFiles) `
        'Historical/checkpoint raw inventory cardinality differs.'
    for ($index = 0; $index -lt $HistoricalInventory.Count; $index++) {
        $old = $HistoricalInventory[$index]
        $new = $CheckpointInventory[$index]
        Assert-GeoCeDGPhaseLifecycle ([string]$old.path -ceq [string]$new.path -and
            [bool]$old.exists -eq [bool]$new.exists -and [long]$old.bytes -eq [long]$new.bytes -and
            [string]$old.sha256 -ceq [string]$new.sha256) `
            "Historical/checkpoint raw byte bridge differs at record $index."
    }
    $statusText = @($Policy.implementationPaths | ForEach-Object { " M $([string]$_.path)" } |
        Sort-Object -CaseSensitive) -join "`n"
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($statusText))) -ceq
        [string]$HistoricalExecution.statusSha256) `
        'Historical precommit status is not exactly the eleven-path implementation overlay.'
    $tree = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot ([string]$Policy.implementationCommit)
    Assert-GeoCeDGPhaseLifecycleSet @($checkpointPaths) `
        @($tree.Paths) 'Checkpoint inventory/product tree paths'
}

function Assert-GeoCeDGPhasePrecommitReceiptClosure {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [object]$HistoricalExecution,
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [object[]]$BundleEntries,
        [Parameter(Mandatory)] [string]$BundleDirectory
    )
    $identityJson = $Receipt.inputIdentity | ConvertTo-Json -Depth 20 -Compress
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($identityJson))) -ceq [string]$Receipt.inputFingerprint) `
        'Historical receipt input fingerprint is internally inconsistent.'
    Assert-GeoCeDGPhaseLifecycle ([string]$Receipt.inputFingerprint -ceq
        [string]$HistoricalExecution.inputFingerprint) `
        'Historical receipt input fingerprint changed.'
    foreach ($name in @('head', 'indexSha256', 'statusSha256', 'rawTreeSha256')) {
        Assert-GeoCeDGPhaseLifecycle ([string]$Receipt.inputIdentity.$name -ceq
            [string]$HistoricalExecution.$name) "Historical receipt $name changed."
    }
    Assert-GeoCeDGPhaseLifecycle ([long]$Receipt.inputIdentity.rawFiles -eq
        [long]$HistoricalExecution.rawFiles -and [long]$Receipt.inputIdentity.rawBytes -eq
        [long]$HistoricalExecution.rawBytes) 'Historical receipt raw inventory counters changed.'
    $entryIndex = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot ([string]$Policy.entryCommit)
    Assert-GeoCeDGPhaseLifecycle ([string]$HistoricalExecution.indexSha256 -ceq $entryIndex.IndexSha256) `
        'Historical precommit index does not represent the exact entry commit.'
    $emptyHash = Get-GeoCeDGPhaseLifecycleHash ([byte[]]::new(0))
    Assert-GeoCeDGPhaseLifecycle ([string]$HistoricalExecution.statusSha256 -cne $emptyHash) `
        'Historical precommit evidence unexpectedly claims a clean status.'
    $inventoryReferences = @($Receipt.auditArtifacts | Where-Object {
        [string]$_.path -cmatch '(?i)(?:^|[\\/])input-inventory\.json$'
    })
    Assert-GeoCeDGPhaseLifecycle ($inventoryReferences.Count -eq 1) `
        'Historical receipt has no unique raw input inventory.'
    $reference = $inventoryReferences[0]
    Assert-GeoCeDGPhaseLifecycle ([string]$reference.sha256 -ceq
        [string]$HistoricalExecution.inputInventorySha256) `
        'Historical input-inventory authority changed.'
    $entry = @($BundleEntries | Where-Object { $_.role -ceq 'ARTIFACT' -and
        [string]$_.recordedPath -ceq [string]$reference.path })
    Assert-GeoCeDGPhaseLifecycle ($entry.Count -eq 1 -and
        [string]$entry[0].sha256 -ceq [string]$reference.sha256) `
        'Historical raw input inventory is not hash-bound.'
    $inventoryPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$entry[0].path) `
        'historical input inventory'
    $inventoryBytes = [IO.File]::ReadAllBytes($inventoryPath)
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $inventoryBytes) -ceq
        [string]$HistoricalExecution.inputInventorySha256) 'Historical input-inventory file changed.'
    $inventory = @(ConvertFrom-GeoCeDGPhaseLifecycleJson $inventoryBytes 'historical input inventory')
    $paths = [Collections.Generic.List[string]]::new()
    [long]$totalBytes = 0
    foreach ($record in $inventory) {
        Assert-GeoCeDGPhaseLifecycleProperties $record @('path', 'exists', 'bytes', 'sha256') `
            'Historical input-inventory record'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$record.path) 'historical input inventory'
        Assert-GeoCeDGPhaseLifecycle ($record.exists -is [bool] -and $record.exists -and
            $record.bytes -is [long] -and $record.bytes -ge 0 -and
            [string]$record.sha256 -cmatch '^[0-9a-f]{64}$') `
            "Invalid historical input-inventory record: $path"
        $paths.Add($path)
        $totalBytes += [long]$record.bytes
    }
    Assert-GeoCeDGPhaseLifecycle ($paths.Count -eq @($paths | Sort-Object -Unique -CaseSensitive).Count) `
        'Historical input inventory contains duplicate paths.'
    $productTree = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot ([string]$Policy.implementationCommit)
    Assert-GeoCeDGPhaseLifecycleSet @($paths) @($productTree.Paths) `
        'Historical precommit/product-checkpoint input paths'
    $inventoryJson = @($inventory) | ConvertTo-Json -Depth 10 -Compress
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash `
        ([Text.UTF8Encoding]::new($false).GetBytes($inventoryJson))) -ceq
        [string]$HistoricalExecution.rawTreeSha256 -and $inventory.Count -eq
        [int]$HistoricalExecution.rawFiles -and $totalBytes -eq
        [long]$HistoricalExecution.rawBytes) 'Historical raw input inventory summary is inconsistent.'
    foreach ($implementationPath in @($Policy.implementationPaths)) {
        $path = [string]$implementationPath.path
        $records = @($inventory | Where-Object { [string]$_.path -ceq $path })
        Assert-GeoCeDGPhaseLifecycle ($records.Count -eq 1 -and
            [string]$records[0].sha256 -ceq [string]$implementationPath.sha256) `
            "Historical precommit bytes differ from the product checkpoint: $path"
        $blob = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
            ([string]$Policy.implementationCommit) $path
        Assert-GeoCeDGPhaseLifecycle ([long]$records[0].bytes -eq $blob.Length) `
            "Historical precommit byte length differs from the product checkpoint: $path"
    }
    return ,$inventory
}

function Get-GeoCeDGPhaseBundleJsonRole {
    param(
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [hashtable]$ByRole,
        [Parameter(Mandatory)] [string]$Role
    )
    $entry = $ByRole[$Role]
    Assert-GeoCeDGPhaseLifecycle ($null -ne $entry) "Missing authenticated bundle role: $Role"
    $path = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$entry.path) 'bundle'
    return ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($path)) $Role
}

function Assert-GeoCeDGPhaseG9U1BundleRoleClosure {
    param(
        [Parameter(Mandatory)] [object[]]$Entries,
        [Parameter(Mandatory)] [string[]]$RequiredSingletonRoles,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$HistoricalArtifactPaths,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$RepairArtifactPaths
    )
    foreach ($role in $RequiredSingletonRoles) {
        Assert-GeoCeDGPhaseLifecycle (@($Entries | Where-Object {
                    [string]$_.role -ceq $role
                }).Count -eq 1) "Missing or duplicate evidence-preserving singleton role: $role"
    }
    $allowedRoles = @($RequiredSingletonRoles + @('ARTIFACT', 'REPAIR_ARTIFACT'))
    Assert-GeoCeDGPhaseLifecycle (@($Entries | Where-Object {
                [string]$_.role -cnotin $allowedRoles
            }).Count -eq 0) 'Evidence-preserving bundle contains an unknown role.'
    Assert-GeoCeDGPhaseLifecycleSet @($Entries | Where-Object {
            [string]$_.role -ceq 'ARTIFACT'
        } | ForEach-Object { [string]$_.recordedPath }) $HistoricalArtifactPaths `
        'Historical receipt artifact closure'
    Assert-GeoCeDGPhaseLifecycleSet @($Entries | Where-Object {
            [string]$_.role -ceq 'REPAIR_ARTIFACT'
        } | ForEach-Object { [string]$_.recordedPath }) $RepairArtifactPaths `
        'Bounded DEV repair artifact closure'
    return $true
}

function Assert-GeoCeDGPhaseDevIntegration {
    param(
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [object[]]$Entries,
        [Parameter(Mandatory)] [hashtable]$ByRole,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$Prefix,
        [Parameter(Mandatory)] [object]$Authority
    )
    $root = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole "${Prefix}_INTEGRATION_ROOT"
    $summary = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole "${Prefix}_INTEGRATION_RESULT"
    $module = [string]$Authority.module
    $filter = [string]$Authority.filter
    Assert-GeoCeDGPhaseLifecycle ($module -cin @('shared', 'desktop') -and
        -not [string]::IsNullOrWhiteSpace($filter)) "Invalid $Prefix integration authority."
    Assert-GeoCeDGPhaseLifecycle ($root.schemaVersion -is [long] -and $root.schemaVersion -eq 1 -and
        [string]$root.level -ceq 'DEV' -and [string]$root.repositoryCommit -ceq $TechnicalCommit -and
        [string]$root.module -ceq $module -and @($root.testFilters).Count -eq 1 -and
        [string]$root.testFilters[0] -ceq $filter -and [string]$root.state -ceq
        'PASS_SCOPED_NOT_ACCEPTANCE' -and [int]$root.exitCode -eq 0 -and $null -eq $root.failure -and
        $root.authorApproved -is [bool] -and -not $root.authorApproved -and
        $root.selfApproved -is [bool] -and -not $root.selfApproved -and
        -not [string]::IsNullOrWhiteSpace([string]$root.devEvidence)) `
        "Invalid bounded $Prefix DEV root."
    Assert-GeoCeDGPhaseLifecycle ($summary.schemaVersion -is [long] -and
        $summary.schemaVersion -eq 1 -and [string]$summary.level -ceq 'DEV' -and
        [string]$summary.state -ceq 'PASS_SCOPED_NOT_ACCEPTANCE' -and
        [string]$summary.module -ceq $module -and @($summary.filters).Count -eq 1 -and
        [string]$summary.filters[0] -ceq $filter -and [int]$summary.nativeExitCode -eq 0 -and
        [long]$summary.tests -gt 0 -and $summary.authorApproved -is [bool] -and
        -not $summary.authorApproved -and $summary.selfApproved -is [bool] -and
        -not $summary.selfApproved) "Invalid bounded $Prefix DEV evidence."
    $summaryEntry = $ByRole["${Prefix}_INTEGRATION_RESULT"]
    Assert-GeoCeDGPhaseLifecycle ([string]$root.devEvidence -ceq
        [string]$summaryEntry.recordedPath) `
        "Bounded $Prefix DEV root does not name the authenticated evidence path."
    $expectedTask = if ($module -ceq 'shared') {
        ':shared:common-jre:test'
    } else { ':desktop:desktop:test' }
    Assert-GeoCeDGPhaseLifecycle (@($summary.tasks | Where-Object {
            [string]$_.task -ceq $expectedTask -and [string]$_.outcome -ceq 'EXECUTED'
        }).Count -eq 1) "Bounded $Prefix DEV task was not freshly executed."
    $separator = $filter.LastIndexOf('.')
    Assert-GeoCeDGPhaseLifecycle ($separator -gt 0 -and $separator -lt ($filter.Length - 1)) `
        "Bounded $Prefix filter is not an exact class/method authority."
    $expectedClass = $filter.Substring(0, $separator)
    $expectedMethod = $filter.Substring($separator + 1)
    $matchingCases = @($summary.junit | ForEach-Object { @($_.cases) } | Where-Object {
            [string]$_.class -ceq $expectedClass -and
            ([string]$_.name -ceq $expectedMethod -or [string]$_.name -ceq "$expectedMethod()") -and
            [string]$_.status -ceq 'PASS'
        })
    Assert-GeoCeDGPhaseLifecycle ($matchingCases.Count -eq 1) `
        "Bounded $Prefix DEV evidence does not contain the exact passing test."
    $tests = 0
    foreach ($report in @($summary.junit)) {
        Assert-GeoCeDGPhaseLifecycle ([int]$report.failures -eq 0 -and
            [int]$report.errors -eq 0 -and [int]$report.tests -gt 0 -and
            [string]$report.sha256 -cmatch '^[0-9a-f]{64}$') `
            "Invalid bounded $Prefix JUnit evidence."
        $tests += [int]$report.tests
        $matches = @($Entries | Where-Object { [string]$_.role -ceq 'REPAIR_ARTIFACT' -and
            [string]$_.recordedPath -ceq [string]$report.archivePath })
        Assert-GeoCeDGPhaseLifecycle ($matches.Count -eq 1 -and
            [string]$matches[0].sha256 -ceq [string]$report.sha256) `
            "Missing or mismatched bounded $Prefix JUnit archive."
    }
    Assert-GeoCeDGPhaseLifecycle ($tests -eq [int]$summary.tests) `
        "Bounded $Prefix DEV counters disagree."
    return @($summary.junit | ForEach-Object { [string]$_.archivePath })
}

function Assert-GeoCeDGPhaseR1CrossCheckoutIntegration {
    param(
        [Parameter(Mandatory)] [object]$Document,
        [Parameter(Mandatory)] [object]$Authority,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$RepositoryRoot
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Authority @('reviewedTechnicalCommit',
        'closeoutCommit', 'bundleSha256', 'verificationCodeSources') `
        'R1 cross-checkout integration authority'
    Assert-GeoCeDGPhaseLifecycle ($Document.schemaVersion -is [long] -and
        $Document.schemaVersion -eq 1 -and [string]$Document.state -ceq
        'AUTHOR_CLOSEOUT_GIT_IDENTITY_PASSED_NOT_NEW_TECHNICAL_EXECUTION' -and
        [string]$Document.reviewedTechnicalCommit -ceq [string]$Authority.reviewedTechnicalCommit -and
        [string]$Document.closeoutCommit -ceq [string]$Authority.closeoutCommit -and
        [string]$Document.technicalEvidenceBundleSha256 -ceq [string]$Authority.bundleSha256 -and
        [string]$Document.verificationCodeCommit -ceq $TechnicalCommit -and
        [string]::IsNullOrEmpty([string]$Document.verificationCodeStatus) -and
        $Document.productRuntimeExecuted -is [bool] -and -not $Document.productRuntimeExecuted -and
        $Document.currentRunReceiptProduced -is [bool] -and
        -not $Document.currentRunReceiptProduced -and
        $Document.authorDecisionCreatedByVerifier -is [bool] -and
        -not $Document.authorDecisionCreatedByVerifier -and $Document.selfApproved -is [bool] -and
        -not $Document.selfApproved -and $null -eq $Document.failure) `
        'Actual R1 cross-checkout lifecycle regression failed or relabelled execution.'
    $expectedSources = @($Authority.verificationCodeSources)
    Assert-GeoCeDGPhaseLifecycleSet @($Document.verificationCodeSources | ForEach-Object {
            [string]$_.path
        }) @($expectedSources | ForEach-Object { [string]$_.path }) `
        'R1 cross-checkout verification-code sources'
    foreach ($expected in $expectedSources) {
        Assert-GeoCeDGPhaseLifecycleProperties $expected @('path', 'mode', 'blob') `
            'R1 verification-code Git authority'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$expected.path) `
            'R1 verification-code source'
        Assert-GeoCeDGPhaseLifecycle ([string]$expected.mode -cmatch '^100(?:644|755)$' -and
            [string]$expected.blob -cmatch '^[0-9a-f]{40}$') `
            "Invalid R1 verification-code Git authority: $path"
        $line = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('ls-tree', $TechnicalCommit,
            '--', $path)
        $match = [regex]::Match($line, '^([0-7]{6}) blob ([0-9a-f]{40})\t(.+)$')
        Assert-GeoCeDGPhaseLifecycle ($match.Success -and $match.Groups[1].Value -ceq
            [string]$expected.mode -and $match.Groups[2].Value -ceq [string]$expected.blob -and
            $match.Groups[3].Value -ceq $path) `
            "R1 verification-code Git blob/mode differs: $path"
        $actual = @($Document.verificationCodeSources | Where-Object {
                [string]$_.path -ceq $path
            })
        Assert-GeoCeDGPhaseLifecycle ($actual.Count -eq 1 -and
            [string]$actual[0].sha256 -cmatch '^[0-9a-f]{64}$') `
            "R1 same-run raw verification-code evidence is invalid: $path"
    }
    $context = $Document.targetContext
    Assert-GeoCeDGPhaseLifecycle ([string]$context.mode -ceq 'AUTHOR_CLOSEOUT' -and
        [string]$context.phase -ceq 'G9S1-R1' -and
        [string]$context.reviewedTechnicalCommit -ceq [string]$Authority.reviewedTechnicalCommit -and
        [string]$context.closeoutCommit -ceq [string]$Authority.closeoutCommit -and
        [string]$context.repositoryIdentity.nonAllowlistedTrackedIdentity -ceq 'GIT_IDENTICAL' -and
        $context.materialization.physicalByteEqualityRequiredAcrossCheckout -is [bool] -and
        -not $context.materialization.physicalByteEqualityRequiredAcrossCheckout -and
        $context.documentaryEvidenceLinked -is [bool] -and $context.documentaryEvidenceLinked -and
        $context.technicalExecutionRepeated -is [bool] -and
        -not $context.technicalExecutionRepeated -and $context.authorApproved -is [bool] -and
        $context.authorApproved -and $context.selfApproved -is [bool] -and
        -not $context.selfApproved) `
        'Actual R1 cross-checkout target context is not the approved Git-identity closeout.'
    return $true
}

function Assert-GeoCeDGPhaseG9U1RepairBundle {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [object]$LifecyclePolicy,
        [Parameter(Mandatory)] [object]$HistoricalExecution,
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [object[]]$Entries,
        [Parameter(Mandatory)] [hashtable]$ByRole
    )
    $repairPath = 'geocedg/validation/operations/g9u1-lifecycle-repair-policy.json'
    $repairEntry = $ByRole['REPAIR_POLICY']
    Assert-GeoCeDGPhaseLifecycle ([string]$repairEntry.recordedPath -ceq $repairPath) `
        'Repair-policy bundle path changed.'
    $repairBytes = [IO.File]::ReadAllBytes((Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory `
        ([string]$repairEntry.path) 'repair policy'))
    $frozenRepairBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $TechnicalCommit $repairPath
    Assert-GeoCeDGPhaseLifecycle ($repairBytes.Length -eq $frozenRepairBytes.Length -and
        [Linq.Enumerable]::SequenceEqual([byte[]]$repairBytes, [byte[]]$frozenRepairBytes)) `
        'Bundled repair policy is not the exact technical-commit blob.'
    $repairPolicy = ConvertFrom-GeoCeDGPhaseLifecycleJson $repairBytes 'frozen G9U1 repair policy'
    Assert-GeoCeDGPhaseLifecycleProperties $repairPolicy @('schemaVersion', 'kind',
        'reviewedTechnicalCommit', 'closeoutCommit', 'contract', 'historicalEvidenceAuthority',
        'boundedIntegrations', 'focusedInputIdentitySuites', 'focusedLifecycleTests',
        'closeoutPaths', 'repairPaths') `
        'Frozen G9U1 repair policy'
    Assert-GeoCeDGPhaseLifecycle ($repairPolicy.schemaVersion -is [long] -and
        $repairPolicy.schemaVersion -eq 1 -and [string]$repairPolicy.kind -ceq
        'BOUNDED_INPUT_IDENTITY_REPAIR_POLICY' -and [string]$repairPolicy.reviewedTechnicalCommit -ceq
        [string]$LifecyclePolicy.implementationCommit -and [string]$repairPolicy.closeoutCommit -ceq
        [string]$LifecyclePolicy.implementationCommit -and [string]$repairPolicy.contract -ceq
        'ADR_0024_SECTION_11_2_G9U1_EVIDENCE_PRESERVING_LIFECYCLE_REPAIR' -and
        @($repairPolicy.closeoutPaths).Count -eq 0) `
        'Frozen G9U1 repair policy has the wrong authority.'
    $authority = $repairPolicy.historicalEvidenceAuthority
    Assert-GeoCeDGPhaseLifecycleProperties $authority @('head', 'indexSha256', 'statusSha256',
        'rawTreeSha256', 'rawFiles', 'rawBytes', 'inputInventorySha256', 'inputFingerprint',
        'executionPlanSha256', 'focusedTests', 'composedTests', 'composedSkipped', 'fullTests',
        'fullSkipped', 'artifacts') 'Historical G9U1 evidence authority'
    foreach ($name in @('head', 'indexSha256', 'statusSha256', 'rawTreeSha256',
            'inputInventorySha256', 'inputFingerprint', 'executionPlanSha256')) {
        Assert-GeoCeDGPhaseLifecycle ([string]$HistoricalExecution.$name -ceq [string]$authority.$name) `
            "Historical G9U1 authority differs: $name"
    }
    Assert-GeoCeDGPhaseLifecycle ([long]$HistoricalExecution.rawFiles -eq [long]$authority.rawFiles -and
        [long]$HistoricalExecution.rawBytes -eq [long]$authority.rawBytes) `
        'Historical G9U1 raw-input counters differ from frozen policy.'
    $artifactRoles = @($authority.artifacts | ForEach-Object { [string]$_.role })
    Assert-GeoCeDGPhaseLifecycleSet $artifactRoles @('FOCUSED_A_SUMMARY', 'FOCUSED_B_SUMMARY',
        'PHASE_ROOT', 'PHASE_SUMMARY', 'COMPOSED_ROOT', 'COMPOSED_RECEIPT', 'FULL_ROOT',
        'FULL_RECEIPT', 'CHECKPOINT_AUTHORITY', 'CHECKPOINT_INPUT_INVENTORY',
        'CHECKPOINT_MATERIALIZATION_PROOF') 'Frozen historical evidence roles'
    foreach ($record in @($authority.artifacts)) {
        Assert-GeoCeDGPhaseLifecycleProperties $record @('role', 'recordedPath', 'sha256') `
            'Frozen historical evidence record'
        $entry = $ByRole[[string]$record.role]
        Assert-GeoCeDGPhaseLifecycle ($null -ne $entry -and [string]$entry.recordedPath -ceq
            [string]$record.recordedPath -and [string]$entry.sha256 -ceq [string]$record.sha256) `
            "Frozen historical artifact differs: $($record.role)"
    }
    foreach ($role in @('FOCUSED_A_SUMMARY', 'FOCUSED_B_SUMMARY', 'PHASE_SUMMARY')) {
        $summary = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole $role
        [long]$focusedTests = 0
        foreach ($suite in @($summary.tests)) {
            Assert-GeoCeDGPhaseLifecycle ([long]$suite.tests -gt 0 -and
                [long]$suite.failures -eq 0 -and [long]$suite.errors -eq 0) `
                "Historical G9U1 focused suite failed: $role/$($suite.class)"
            $focusedTests += [long]$suite.tests
        }
        Assert-GeoCeDGPhaseLifecycle ($focusedTests -eq [long]$authority.focusedTests) `
            "Historical G9U1 focused test count differs: $role"
    }
    . (Join-Path $PSScriptRoot 'verification-repair-equivalence.ps1')
    $recomputed = Get-GeoCeDGVerificationRepairEquivalence -RepositoryRoot $RepositoryRoot `
        -ReviewedTechnicalCommit ([string]$LifecyclePolicy.implementationCommit) `
        -CloseoutCommit ([string]$LifecyclePolicy.implementationCommit) `
        -CandidateCommit $TechnicalCommit -CloseoutPaths @() -RepairPaths @($repairPolicy.repairPaths)
    $bundledStructural = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'REPAIR_EQUIVALENCE'
    $expectedStructuralText = ConvertTo-Json -InputObject $recomputed -Depth 100 -Compress
    $actualStructuralText = ConvertTo-Json -InputObject $bundledStructural -Depth 100 -Compress
    Assert-GeoCeDGPhaseLifecycle ($actualStructuralText -ceq $expectedStructuralText -and
        $recomputed.executionPlanEquivalent -and [string]$recomputed.reviewedExecutionPlanSha256 -ceq
        [string]$authority.executionPlanSha256 -and [string]$recomputed.candidateExecutionPlanSha256 -ceq
        [string]$authority.executionPlanSha256) `
        'Bundled structural proof is not the live recomputation from frozen policy.'
    $policyCanonicalHash = Get-GeoCeDGPhaseLifecycleHash (ConvertTo-GeoCeDGCanonicalLfBytes $repairBytes)
    $identityA = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'INPUT_IDENTITY_A_SUMMARY'
    $identityB = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'INPUT_IDENTITY_B_SUMMARY'
    Assert-GeoCeDGPhaseInputIdentityRepairSummary $identityA $LifecyclePolicy $recomputed $policyCanonicalHash
    Assert-GeoCeDGPhaseInputIdentityRepairSummary $identityB $LifecyclePolicy $recomputed $policyCanonicalHash
    foreach ($identity in @($identityA, $identityB)) {
        [long]$focusedTotal = 0
        foreach ($property in @($repairPolicy.focusedInputIdentitySuites.PSObject.Properties)) {
            $matches = @($identity.suites | Where-Object { [string]$_.suite -ceq [string]$property.Name })
            Assert-GeoCeDGPhaseLifecycle ($matches.Count -eq 1 -and
                [long]$matches[0].tests -eq [long]$property.Value -and
                [long]$matches[0].passed -eq [long]$property.Value -and
                [long]$matches[0].failed -eq 0) `
                "Input-identity suite count changed: $($property.Name)"
            $focusedTotal += [long]$property.Value
        }
        Assert-GeoCeDGPhaseLifecycle ([long]$identity.tests -eq $focusedTotal) `
            'Input-identity focused total differs from frozen suite authority.'
    }
    Assert-GeoCeDGPhaseLifecycle ([string]$ByRole['INPUT_IDENTITY_A_SUMMARY'].sha256 -ceq
        [string]$ByRole['INPUT_IDENTITY_B_SUMMARY'].sha256) `
        'Input-identity focused A/B results are not byte-deterministic.'
    $expectedTests = @($repairPolicy.focusedLifecycleTests | ForEach-Object { [string]$_ })
    $lifecycleA = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'G9U1_LIFECYCLE_A_SUMMARY'
    $lifecycleB = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'G9U1_LIFECYCLE_B_SUMMARY'
    Assert-GeoCeDGPhaseG9U1LifecycleSummary $lifecycleA $LifecyclePolicy $expectedTests
    Assert-GeoCeDGPhaseG9U1LifecycleSummary $lifecycleB $LifecyclePolicy $expectedTests
    Assert-GeoCeDGPhaseLifecycle ([string]$ByRole['G9U1_LIFECYCLE_A_SUMMARY'].sha256 -ceq
        [string]$ByRole['G9U1_LIFECYCLE_B_SUMMARY'].sha256) `
        'G9U1 lifecycle focused A/B results are not byte-deterministic.'
    $checkpoint = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'CHECKPOINT_AUTHORITY'
    $checkpointInventory = @(Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole `
        'CHECKPOINT_INPUT_INVENTORY')
    $materialization = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole `
        'CHECKPOINT_MATERIALIZATION_PROOF'
    $receiptInventories = @{}
    foreach ($level in @('COMPOSED', 'FULL')) {
        $receipt = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole "${level}_RECEIPT"
        $receiptInventories[$level] = Assert-GeoCeDGPhasePrecommitReceiptClosure `
            -RepositoryRoot $RepositoryRoot -Policy $LifecyclePolicy -HistoricalExecution $HistoricalExecution `
            -Receipt $receipt -BundleEntries $Entries -BundleDirectory $BundleDirectory
        $testProperty = "$($level.ToLowerInvariant())Tests"
        $skippedProperty = "$($level.ToLowerInvariant())Skipped"
        Assert-GeoCeDGPhaseLifecycle ([int]$receipt.tests -eq
            [int]$authority.PSObject.Properties[$testProperty].Value -and
            [int]$receipt.skippedUpstreamTests -eq
            [int]$authority.PSObject.Properties[$skippedProperty].Value) `
            "Historical $level counters differ from frozen policy."
        Assert-GeoCeDGPhaseCheckpointBridge -RepositoryRoot $RepositoryRoot -Policy $LifecyclePolicy `
            -Authority $authority -HistoricalExecution $HistoricalExecution `
            -HistoricalInventory $receiptInventories[$level] -Checkpoint $checkpoint `
            -CheckpointInventory $checkpointInventory -Materialization $materialization
    }
    $staticLogEntry = $ByRole['G9U1_COMMITTED_STATIC_LOG']
    $staticLogBytes = [IO.File]::ReadAllBytes((Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory `
        ([string]$staticLogEntry.path) 'committed-candidate static log'))
    $staticLog = [Text.UTF8Encoding]::new($false, $true).GetString($staticLogBytes)
    Assert-GeoCeDGPhaseLifecycle ($staticLog -cmatch
        'G9U1 static contracts coherent; this is not runtime acceptance\.' -and
        $staticLog -cnotmatch '(?m)^(?:Write-Error: )|FAILED|PASS — AUTHOR APPROVED') `
        'Committed-candidate G9U1 static verifier did not pass cleanly.'
    $parser = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'PARSER_STATIC_RESULT'
    $expectedParsers = @($repairPolicy.repairPaths | Where-Object { [string]$_.path -cmatch '\.ps(m)?1$' } |
        ForEach-Object { [string]$_.path })
    Assert-GeoCeDGPhaseLifecycle ($parser.schemaVersion -is [long] -and $parser.schemaVersion -eq 1 -and
        [string]$parser.state -ceq 'PASS' -and [string]$parser.mode -ceq 'COMMITTED_CANDIDATE' -and
        [string]$parser.technicalCommit -ceq $TechnicalCommit -and
        $parser.productRuntimeExecuted -is [bool] -and -not $parser.productRuntimeExecuted -and
        $parser.skipBuildIsAcceptanceEvidence -is [bool] -and -not $parser.skipBuildIsAcceptanceEvidence -and
        [string]$parser.purpose -ceq 'LIFECYCLE_AND_STATIC_INTEGRATION_ONLY' -and
        [string]$parser.staticLogSha256 -ceq [string]$staticLogEntry.sha256 -and
        @($parser.parserFiles | Where-Object { [string]$_.state -cne 'PASS' }).Count -eq 0) `
        'Parser/static integration evidence is invalid.'
    Assert-GeoCeDGPhaseLifecycleSet @($parser.parserFiles | ForEach-Object { [string]$_.path }) `
        $expectedParsers 'Parser/static input paths'
    $repairArtifactPaths = @(
        Assert-GeoCeDGPhaseDevIntegration $BundleDirectory $Entries $ByRole $TechnicalCommit 'SHARED' `
            $repairPolicy.boundedIntegrations.shared
        Assert-GeoCeDGPhaseDevIntegration $BundleDirectory $Entries $ByRole $TechnicalCommit 'DESKTOP' `
            $repairPolicy.boundedIntegrations.desktop
    )
    Assert-GeoCeDGPhaseLifecycleSet @($Entries | Where-Object {
            [string]$_.role -ceq 'REPAIR_ARTIFACT'
        } | ForEach-Object { [string]$_.recordedPath }) $repairArtifactPaths `
        'Bounded DEV repair artifacts'
    $r1 = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'R1_CROSS_CHECKOUT_RESULT'
    $r1Authority = $repairPolicy.boundedIntegrations.r1CrossCheckout
    [void](Assert-GeoCeDGPhaseR1CrossCheckoutIntegration -Document $r1 `
        -Authority $r1Authority -TechnicalCommit $TechnicalCommit -RepositoryRoot $RepositoryRoot)
    $diff = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'GIT_DIFF_RESULT'
    $expectedCommands = @(
        "git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check $($LifecyclePolicy.implementationCommit)..$TechnicalCommit",
        'git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check',
        'git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --cached --check'
    )
    Assert-GeoCeDGPhaseLifecycle ($diff.schemaVersion -is [long] -and $diff.schemaVersion -eq 1 -and
        [string]$diff.state -ceq 'PASS' -and [string]$diff.base -ceq
        [string]$LifecyclePolicy.implementationCommit -and [string]$diff.candidate -ceq $TechnicalCommit -and
        @($diff.productPaths).Count -eq 0) 'Git diff evidence is invalid.'
    Assert-GeoCeDGPhaseLifecycleSet @($diff.commands | ForEach-Object { [string]$_ }) `
        $expectedCommands 'Git diff commands'
    Assert-GeoCeDGPhaseLifecycleSet @($diff.paths | ForEach-Object { [string]$_ }) `
        @($recomputed.changedPaths | ForEach-Object { [string]$_.path }) 'Git diff repair paths'
    $matrix = Get-GeoCeDGPhaseBundleJsonRole $BundleDirectory $ByRole 'REPAIR_CONDITION_MATRIX'
    Assert-GeoCeDGPhaseLifecycle ($matrix.schemaVersion -is [long] -and $matrix.schemaVersion -eq 1 -and
        [string]$matrix.kind -ceq 'ADR0024_EVIDENCE_PRESERVING_VERIFIER_REPAIR_CONDITION_MATRIX' -and
        [string]$matrix.reviewedProductCheckpoint -ceq [string]$LifecyclePolicy.implementationCommit -and
        [string]$matrix.operationalSuccessor -ceq $TechnicalCommit -and
        [long]$matrix.passed -eq 15 -and [long]$matrix.failed -eq 0 -and
        $matrix.evidencePreservingVerifierRepair -is [bool] -and
        $matrix.evidencePreservingVerifierRepair -and
        $matrix.heavyScientificExecutionRepeated -is [bool] -and
        -not $matrix.heavyScientificExecutionRepeated -and
        $matrix.historicalExecutionRelabeled -is [bool] -and
        -not $matrix.historicalExecutionRelabeled -and $matrix.selfApproved -is [bool] -and
        -not $matrix.selfApproved -and @($matrix.conditions).Count -eq 15) `
        'ADR 0024 condition matrix is invalid.'
    $conditionEvidence = @{
        1=@('REPAIR_EQUIVALENCE');2=@('REPAIR_EQUIVALENCE');3=@('REPAIR_EQUIVALENCE');
        4=@('REPAIR_EQUIVALENCE');5=@('REPAIR_EQUIVALENCE');6=@('REPAIR_EQUIVALENCE');
        7=@('REPAIR_EQUIVALENCE');8=@('REPAIR_EQUIVALENCE');9=@('REPAIR_EQUIVALENCE');
        10=@('REPAIR_EQUIVALENCE');
        11=@('REPAIR_EQUIVALENCE','INPUT_IDENTITY_A_SUMMARY','INPUT_IDENTITY_B_SUMMARY',
            'G9U1_LIFECYCLE_A_SUMMARY','G9U1_LIFECYCLE_B_SUMMARY');
        12=@('FOCUSED_A_SUMMARY','FOCUSED_B_SUMMARY','PHASE_ROOT','PHASE_SUMMARY',
            'COMPOSED_ROOT','COMPOSED_RECEIPT','FULL_ROOT','FULL_RECEIPT','CHECKPOINT_AUTHORITY',
            'CHECKPOINT_INPUT_INVENTORY','CHECKPOINT_MATERIALIZATION_PROOF');
        13=@('INPUT_IDENTITY_A_SUMMARY','INPUT_IDENTITY_B_SUMMARY','G9U1_LIFECYCLE_A_SUMMARY',
            'G9U1_LIFECYCLE_B_SUMMARY');
        14=@('SHARED_INTEGRATION_RESULT','SHARED_INTEGRATION_ROOT','DESKTOP_INTEGRATION_RESULT',
            'DESKTOP_INTEGRATION_ROOT','R1_CROSS_CHECKOUT_RESULT','PARSER_STATIC_RESULT','GIT_DIFF_RESULT');
        15=@('REPAIR_EQUIVALENCE')
    }
    $ids = [Collections.Generic.List[int]]::new()
    foreach ($condition in @($matrix.conditions)) {
        Assert-GeoCeDGPhaseLifecycleProperties $condition @('id', 'requirement', 'result', 'evidence') `
            'ADR 0024 repair condition'
        $id = [int]$condition.id
        Assert-GeoCeDGPhaseLifecycle ($id -ge 1 -and $id -le 15 -and
            [string]$condition.result -ceq 'PASS') 'ADR 0024 repair condition did not pass.'
        Assert-GeoCeDGPhaseLifecycleSet @($condition.evidence | ForEach-Object { [string]$_ }) `
            @($conditionEvidence[$id]) "ADR 0024 condition $id evidence roles"
        $ids.Add($id)
    }
    Assert-GeoCeDGPhaseLifecycle (($ids | Sort-Object) -join ',' -ceq ((1..15) -join ',')) `
        'ADR 0024 condition IDs are incomplete or duplicated.'
    return [pscustomobject][ordered]@{
        RepairPolicy = $repairPolicy
        StructuralProof = $recomputed
        PolicyCanonicalLfSha256 = $policyCanonicalHash
        ConditionsAuthenticated = 15
        RepairArtifactPaths = $repairArtifactPaths
    }
}

function Assert-GeoCeDGEvidencePreservingPrecommitLink {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [string]$BundleSha256,
        [Parameter(Mandatory)] [object]$Manifest,
        [string]$CloseoutCommit,
        [object]$CloseoutRecord,
        [switch]$PendingCloseout,
        [switch]$CohortOnly
    )
    Assert-GeoCeDGPhaseLifecycle ($BundleSha256 -cmatch '^[0-9a-f]{64}$') `
        'Invalid evidence-preserving bundle-manifest hash.'
    if ($CohortOnly) {
        Assert-GeoCeDGPhaseLifecycle ([string]::IsNullOrWhiteSpace($CloseoutCommit) -and
            $null -eq $CloseoutRecord -and -not $PendingCloseout) `
            'Cohort-only linkage cannot carry author-closeout authority.'
    } else {
        Assert-GeoCeDGPhaseLifecycle (-not [string]::IsNullOrWhiteSpace($CloseoutCommit) -and
            $null -ne $CloseoutRecord) 'Author closeout requires explicit commit and decision record.'
    }
    Assert-GeoCeDGPhaseLifecycleProperties $Manifest @('schemaVersion', 'kind', 'phase',
        'reviewedTechnicalCommit', 'productCheckpoint', 'historicalExecution', 'repair', 'files') `
        'Evidence-preserving precommit-link manifest'
    Assert-GeoCeDGPhaseLifecycle ($Manifest.schemaVersion -is [long] -and $Manifest.schemaVersion -eq 2 -and
        [string]$Manifest.kind -ceq 'GEOCEDG_EVIDENCE_PRESERVING_PRECOMMIT_LINK' -and
        [string]$Manifest.phase -ceq [string]$Policy.phase -and
        [string]$Manifest.phase -ceq 'G9U1' -and
        [string]$Manifest.reviewedTechnicalCommit -ceq $TechnicalCommit) `
        'Wrong evidence-preserving precommit-link authority.'
    Assert-GeoCeDGPhaseLifecycleProperties $Manifest.productCheckpoint @('commit', 'tree') `
        'Precommit-link product checkpoint'
    Assert-GeoCeDGPhaseLifecycle ([string]$Manifest.productCheckpoint.commit -ceq
        [string]$Policy.implementationCommit -and [string]$Manifest.productCheckpoint.tree -ceq
        [string]$Policy.implementationTree) 'Precommit-link product checkpoint changed.'
    Assert-GeoCeDGPhaseLifecycleProperties $Manifest.historicalExecution @('head', 'indexSha256',
        'statusSha256', 'rawTreeSha256', 'rawFiles', 'rawBytes', 'inputInventorySha256',
        'inputFingerprint', 'executionPlanSha256', 'provenance') 'Historical precommit execution'
    $historical = $Manifest.historicalExecution
    Assert-GeoCeDGPhaseLifecycle ([string]$historical.head -ceq [string]$Policy.entryCommit -and
        [string]$historical.provenance -cmatch 'PRECOMMIT' -and
        [string]$historical.provenance -cmatch 'NOT_REATTRIBUTED') `
        'Historical execution provenance was relabelled or detached from entry HEAD.'
    foreach ($name in @('indexSha256', 'statusSha256', 'rawTreeSha256', 'inputInventorySha256',
            'inputFingerprint', 'executionPlanSha256')) {
        Assert-GeoCeDGPhaseLifecycle ([string]$historical.$name -cmatch '^[0-9a-f]{64}$') `
            "Invalid historical execution hash: $name"
    }
    Assert-GeoCeDGPhaseLifecycle ($historical.rawFiles -is [long] -and $historical.rawFiles -gt 0 -and
        $historical.rawBytes -is [long] -and $historical.rawBytes -gt 0) `
        'Invalid historical execution inventory counters.'
    Assert-GeoCeDGPhaseLifecycleProperties $Manifest.repair @('evidencePreservingVerifierRepair',
        'repairPolicyRole', 'repairPolicyPath', 'conditionMatrixRole', 'structuralProofRole',
        'checkpointAuthorityRole', 'checkpointInventoryRole', 'checkpointMaterializationRole',
        'inputIdentityARole', 'inputIdentityBRole', 'lifecycleARole', 'lifecycleBRole',
        'committedStaticRole', 'sharedIntegrationRole', 'sharedIntegrationRootRole',
        'desktopIntegrationRole', 'desktopIntegrationRootRole', 'r1CrossCheckoutRole',
        'parserStaticRole', 'gitDiffRole', 'technicalExecutionRepeated', 'priorExecutionRelabeled') `
        'Evidence-preserving repair record'
    $repair = $Manifest.repair
    Assert-GeoCeDGPhaseLifecycle ($repair.evidencePreservingVerifierRepair -is [bool] -and
        $repair.evidencePreservingVerifierRepair -and $repair.technicalExecutionRepeated -is [bool] -and
        -not $repair.technicalExecutionRepeated -and $repair.priorExecutionRelabeled -is [bool] -and
        -not $repair.priorExecutionRelabeled) 'Evidence-preserving repair truth flags are invalid.'
    $expectedRepairRoles = [ordered]@{
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
    }
    foreach ($property in $expectedRepairRoles.Keys) {
        Assert-GeoCeDGPhaseLifecycle ([string]$repair.$property -ceq
            [string]$expectedRepairRoles[$property]) "Unexpected repair evidence role: $property"
    }
    $entries = @($Manifest.files)
    Assert-GeoCeDGPhaseLifecycle ($entries.Count -gt 0) 'Evidence-preserving bundle is empty.'
    $paths = @($entries | ForEach-Object {
        Assert-GeoCeDGPhaseLifecycleProperties $_ @('role', 'path', 'recordedPath', 'sha256') `
            'Evidence-preserving bundle-file record'
        Assert-GeoCeDGPhaseLifecycle ([string]$_.role -cmatch '^[A-Z][A-Z0-9_]*$' -and
            [string]$_.sha256 -cmatch '^[0-9a-f]{64}$') 'Invalid evidence-preserving bundle-file record.'
        [void](Get-GeoCeDGPhaseBundleFile $BundleDirectory $_)
        ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$_.path) 'evidence-preserving bundle'
    })
    Assert-GeoCeDGPhaseLifecycle ($paths.Count -eq @($paths | Sort-Object -Unique -CaseSensitive).Count) `
        'Duplicate evidence-preserving bundle path.'
    $historicalRoles = @('FOCUSED_A_SUMMARY', 'FOCUSED_B_SUMMARY', 'PHASE_ROOT', 'PHASE_SUMMARY',
        'COMPOSED_ROOT', 'COMPOSED_RECEIPT', 'FULL_ROOT', 'FULL_RECEIPT')
    $repairRoles = @($expectedRepairRoles.GetEnumerator() | Where-Object {
        [string]$_.Key -cne 'repairPolicyPath'
    } | ForEach-Object { [string]$_.Value })
    $byRole = @{}
    foreach ($role in $historicalRoles + $repairRoles) {
        $byRole[$role] = Get-GeoCeDGPhaseUniqueBundleRole $entries $role
    }
    $allowedRoles = @($historicalRoles + $repairRoles + @('ARTIFACT', 'REPAIR_ARTIFACT'))
    Assert-GeoCeDGPhaseLifecycle (@($entries | Where-Object {
        [string]$_.role -cnotin $allowedRoles
    }).Count -eq 0) 'Evidence-preserving bundle contains an unknown role.'
    Assert-GeoCeDGPhaseLifecycle ([string]$byRole['FOCUSED_A_SUMMARY'].sha256 -ceq
        [string]$byRole['FOCUSED_B_SUMMARY'].sha256 -and
        [string]$byRole['FOCUSED_A_SUMMARY'].sha256 -ceq
        [string]$byRole['PHASE_SUMMARY'].sha256) `
        'Historical G9U1 focused/PHASE canonical summaries differ.'
    foreach ($role in @('FOCUSED_A_SUMMARY', 'FOCUSED_B_SUMMARY', 'PHASE_SUMMARY')) {
        $summaryPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$byRole[$role].path) 'summary'
        $summary = ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($summaryPath)) $role
        Assert-GeoCeDGPhaseLifecycle ([string]$summary.phase -ceq 'G9U1' -and
            [string]$summary.state -ceq 'FINAL_TOOLBAR_VISUAL_NORMALIZATION_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL' -and
            $summary.selfApproved -is [bool] -and -not $summary.selfApproved -and
            $summary.authorApprovedImplementation -is [bool] -and -not $summary.authorApprovedImplementation -and
            $summary.passClaimedImplementation -is [bool] -and -not $summary.passClaimedImplementation) `
            "Historical G9U1 summary changed: $role"
    }
    $readRole = {
        param([string]$Role)
        $path = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$byRole[$Role].path) 'bundle'
        ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($path)) $Role
    }
    $authenticatedRepair = Assert-GeoCeDGPhaseG9U1RepairBundle -RepositoryRoot $RepositoryRoot `
        -TechnicalCommit $TechnicalCommit -LifecyclePolicy $Policy -HistoricalExecution $historical `
        -BundleDirectory $BundleDirectory -Entries $entries -ByRole $byRole
    $phaseRoot = & $readRole 'PHASE_ROOT'
    Assert-GeoCeDGPhaseLifecycle ([string]$phaseRoot.level -ceq 'PHASE' -and
        [string]$phaseRoot.phase -ceq 'G9U1' -and [string]$phaseRoot.repositoryCommit -ceq
        [string]$historical.head -and [int]$phaseRoot.exitCode -eq 0 -and
        [string]$phaseRoot.state -ceq 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL' -and
        $null -eq $phaseRoot.failure -and $phaseRoot.authorApproved -is [bool] -and
        -not $phaseRoot.authorApproved -and $phaseRoot.selfApproved -is [bool] -and
        -not $phaseRoot.selfApproved) 'Invalid historical G9U1 PHASE root.'
    $historicalArtifactPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($level in @('COMPOSED', 'FULL')) {
        $root = & $readRole "${level}_ROOT"
        $receipt = & $readRole "${level}_RECEIPT"
        $receiptRecordedPath = [string]$byRole["${level}_RECEIPT"].recordedPath
        $canonicalReceipt = ([string]$root.canonicalReceipt).Replace('\', '/')
        $canonicalReceiptSuffix = "/$receiptRecordedPath"
        Assert-GeoCeDGPhaseLifecycle ([string]$root.level -ceq $level -and
            [string]$root.repositoryCommit -ceq [string]$historical.head -and
            [int]$root.exitCode -eq 0 -and
            [string]$root.state -ceq 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL' -and
            $null -eq $root.failure -and $root.authorApproved -is [bool] -and
            -not $root.authorApproved -and $root.selfApproved -is [bool] -and
            -not $root.selfApproved -and [IO.Path]::IsPathRooted([string]$root.canonicalReceipt) -and
            $canonicalReceipt.EndsWith($canonicalReceiptSuffix, [StringComparison]::Ordinal)) `
            "Invalid historical G9U1 $level root."
        Assert-GeoCeDGPhaseLifecycle ([int]$receipt.schemaVersion -eq 1 -and
            [string]$receipt.kind -ceq 'CURRENT_RUN_BUILD_EVIDENCE' -and
            [string]$receipt.level -ceq $level -and
            [string]$receipt.state -ceq 'TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING' -and
            $receipt.authorApproved -is [bool] -and -not $receipt.authorApproved -and
            $receipt.selfApproved -is [bool] -and -not $receipt.selfApproved -and
            $receipt.testResultReuseAcrossRuns -is [bool] -and
            -not $receipt.testResultReuseAcrossRuns) "Invalid historical G9U1 $level receipt."
        foreach ($module in @('shared', 'desktop')) {
            $selection = $receipt.selections.$module
            if ($level -ceq 'FULL') {
                Assert-GeoCeDGPhaseLifecycle ([bool]$selection.unfiltered -and
                    @($selection.filters).Count -eq 0) "Historical FULL $module selection was filtered."
            }
            $task = if ($module -ceq 'shared') { ':shared:common-jre:test' } else { ':desktop:desktop:test' }
            Assert-GeoCeDGPhaseLifecycle (@($receipt.tasks | Where-Object {
                $_.task -ceq $task -and $_.outcome -ceq 'EXECUTED'
            }).Count -eq 1) "Historical mandatory task was not freshly executed: $task"
        }
        $tests = 0
        $skipped = 0
        foreach ($report in @($receipt.junit)) {
            Assert-GeoCeDGPhaseLifecycle ([int]$report.failures -eq 0 -and [int]$report.errors -eq 0) `
                "Historical failed JUnit evidence: $($report.class)"
            $tests += [int]$report.tests
            $skipped += [int]$report.skipped
        }
        Assert-GeoCeDGPhaseLifecycle ($tests -eq [int]$receipt.tests -and
            $skipped -eq [int]$receipt.skippedUpstreamTests -and $tests -gt 0) `
            "Historical $level receipt counters disagree."
        $references = @($receipt.auditArtifacts | ForEach-Object {
            [pscustomobject]@{ path = $_.path; sha256 = $_.sha256 }
        }) + @($receipt.junit | ForEach-Object {
            [pscustomobject]@{ path = $_.archivePath; sha256 = $_.sha256 }
        }) + @($receipt.checkstyle | ForEach-Object {
            [pscustomobject]@{ path = $_.archivePath; sha256 = $_.sha256 }
        })
        foreach ($reference in $references) {
            [void]$historicalArtifactPaths.Add([string]$reference.path)
            $matches = @($entries | Where-Object { $_.role -ceq 'ARTIFACT' -and
                [string]$_.recordedPath -ceq [string]$reference.path })
            Assert-GeoCeDGPhaseLifecycle ($matches.Count -eq 1 -and
                [string]$matches[0].sha256 -ceq [string]$reference.sha256) `
                "Missing, duplicate, or mismatched historical artifact: $($reference.path)"
        }
        [void](Assert-GeoCeDGPhasePrecommitReceiptClosure -RepositoryRoot $RepositoryRoot `
            -Policy $Policy -HistoricalExecution $historical -Receipt $receipt `
            -BundleEntries $entries -BundleDirectory $BundleDirectory)
    }
    [void](Assert-GeoCeDGPhaseG9U1BundleRoleClosure -Entries $entries `
        -RequiredSingletonRoles @($historicalRoles + $repairRoles) `
        -HistoricalArtifactPaths @($historicalArtifactPaths) `
        -RepairArtifactPaths @($authenticatedRepair.RepairArtifactPaths))
    $matrixPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory `
        ([string]$byRole['REPAIR_CONDITION_MATRIX'].path) 'repair condition matrix'
    $matrix = ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($matrixPath)) `
        'repair condition matrix'
    Assert-GeoCeDGPhaseLifecycleProperties $matrix @('schemaVersion', 'kind',
        'reviewedProductCheckpoint', 'operationalSuccessor', 'conditions', 'passed', 'failed',
        'evidencePreservingVerifierRepair', 'heavyScientificExecutionRepeated',
        'historicalExecutionRelabeled', 'selfApproved') 'Repair condition matrix'
    Assert-GeoCeDGPhaseLifecycle ($matrix.schemaVersion -is [long] -and $matrix.schemaVersion -eq 1 -and
        [string]$matrix.kind -ceq 'ADR0024_EVIDENCE_PRESERVING_VERIFIER_REPAIR_CONDITION_MATRIX' -and
        [string]$matrix.reviewedProductCheckpoint -ceq [string]$Policy.implementationCommit -and
        [string]$matrix.operationalSuccessor -ceq $TechnicalCommit -and
        $matrix.passed -is [long] -and $matrix.passed -eq 15 -and
        $matrix.failed -is [long] -and $matrix.failed -eq 0 -and
        $matrix.evidencePreservingVerifierRepair -is [bool] -and
        $matrix.evidencePreservingVerifierRepair -and
        $matrix.heavyScientificExecutionRepeated -is [bool] -and
        -not $matrix.heavyScientificExecutionRepeated -and
        $matrix.historicalExecutionRelabeled -is [bool] -and
        -not $matrix.historicalExecutionRelabeled -and
        $matrix.selfApproved -is [bool] -and -not $matrix.selfApproved) `
        'Repair condition-matrix authority is invalid.'
    $conditions = @($matrix.conditions)
    Assert-GeoCeDGPhaseLifecycle ($conditions.Count -eq 15) `
        'Repair condition matrix must contain exactly fifteen conditions.'
    $requirements = @(
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
    $conditionIds = @($conditions | ForEach-Object {
        Assert-GeoCeDGPhaseLifecycleProperties $_ @('id', 'requirement', 'result', 'evidence') `
            'Repair condition'
        Assert-GeoCeDGPhaseLifecycle ([string]$_.id -cmatch '^(?:ADR0024-)?(?:0?[1-9]|1[0-5])$' -and
            [string]$_.result -ceq 'PASS' -and $null -ne $_.evidence -and
            -not [string]::IsNullOrWhiteSpace([string]$_.evidence)) 'Invalid repair condition result.'
        $id = [int]([string]$_.id -replace '^ADR0024-', '')
        Assert-GeoCeDGPhaseLifecycle ([string]$_.requirement -ceq $requirements[$id - 1]) `
            "Repair condition $id does not preserve the full normative requirement."
        $id
    })
    Assert-GeoCeDGPhaseLifecycle (($conditionIds | Sort-Object) -join ',' -ceq
        ((1..15) -join ',')) 'Repair condition IDs are incomplete or duplicated.'
    $structuralPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory `
        ([string]$byRole['REPAIR_EQUIVALENCE'].path) 'repair structural proof'
    $structural = ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($structuralPath)) `
        'repair structural proof'
    Assert-GeoCeDGPhaseLifecycle ([bool]$structural.executionPlanEquivalent -and
        [string]$structural.reviewedExecutionPlanSha256 -ceq [string]$historical.executionPlanSha256 -and
        [string]$structural.candidateExecutionPlanSha256 -ceq [string]$historical.executionPlanSha256) `
        'Repair execution-plan equivalence is not authenticated.'
    Assert-GeoCeDGPhaseLifecycle ($authenticatedRepair.ConditionsAuthenticated -eq 15) `
        'Authenticated ADR 0024 repair condition count changed.'
    $metadataPaths = @(@($Policy.closeout.literalReplacements | ForEach-Object { [string]$_.path }) +
        @($Policy.closeout.canonicalLfHashManifests | ForEach-Object { [string]$_.path }) +
        [string]$Policy.closeout.recordPath | Sort-Object -Unique -CaseSensitive)
    if (-not $CohortOnly) {
        [void](Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $RepositoryRoot `
            -ReviewedTechnicalCommit $TechnicalCommit `
            -ExistingPaths @($metadataPaths | Where-Object {
                $_ -cne [string]$Policy.closeout.recordPath
            }) -RecordPath ([string]$Policy.closeout.recordPath) `
            -CloseoutCommit $CloseoutCommit -PendingCloseout:$PendingCloseout)
    }
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    if ($CohortOnly) {
        [void](Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $RepositoryRoot `
            -ExpectedCommit $TechnicalCommit)
    } elseif ($PendingCloseout) {
        Assert-GeoCeDGPhaseLifecycle ($CloseoutCommit -ceq $TechnicalCommit) `
            'Pending closeout has no distinct closeout commit.'
        [void](Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $RepositoryRoot `
            -ExpectedCommit $TechnicalCommit -AllowedStatusPaths $metadataPaths)
    } else {
        [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $CloseoutCommit)
        $lineText = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-list', '--parents', '-n', '1',
            $CloseoutCommit)
        $line = @($lineText -split ' ')
        Assert-GeoCeDGPhaseLifecycle ($line.Count -eq 2 -and $line[1] -ceq $TechnicalCommit) `
            'Closeout commit is not the direct child of reviewed technical authority.'
        [void](Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $RepositoryRoot `
            -ReviewedCommit $TechnicalCommit -CloseoutCommit $CloseoutCommit -AllowedPaths $metadataPaths)
        [void](Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $RepositoryRoot `
            -ExpectedCommit $CloseoutCommit)
    }
    return [pscustomobject][ordered]@{
        kind = $(if ($CohortOnly) { 'TECHNICAL_EVIDENCE_PRESERVING_PRECOMMIT_LINK' } else {
                'DOCUMENTARY_EVIDENCE_PRESERVING_PRECOMMIT_LINK'
            })
        phase = [string]$Policy.phase
        technicalCommit = $TechnicalCommit
        productCheckpoint = [string]$Policy.implementationCommit
        historicalExecutionHead = [string]$historical.head
        closeoutCommit = $(if ($CohortOnly -or $PendingCloseout) { $null } else { $CloseoutCommit })
        evidencePreservingVerifierRepair = $true
        technicalExecutionRepeated = $false
        priorExecutionRelabeled = $false
        authorApproved = [bool](-not $CohortOnly)
        documentaryEvidenceLinked = $true
        consumableBuildReceipt = $false
    }
}

function Assert-GeoCeDGTechnicalEvidenceLink {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory)] [object]$Policy,
        [Parameter(Mandatory)] [object]$CloseoutRecord,
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [string]$BundleSha256,
        [switch]$PendingCloseout
    )
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $TechnicalCommit)
    Assert-GeoCeDGPhaseLifecycle ($BundleSha256 -cmatch '^[0-9a-f]{64}$') 'Invalid bundle-manifest hash.'
    Assert-GeoCeDGPhaseCloseoutRecord $CloseoutRecord $Policy $TechnicalCommit $BundleSha256
    $declaredBundle = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot `
        ([string]$CloseoutRecord.evidence.bundleDirectory) 'bundle directory'
    Assert-GeoCeDGPhaseLifecycle ([IO.Path]::GetFullPath($BundleDirectory).Equals($declaredBundle,
        [StringComparison]::OrdinalIgnoreCase)) 'Bundle directory differs from closeout record.'
    $manifestRelative = [string]$CloseoutRecord.evidence.bundleManifestPath
    $manifestPath = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory $manifestRelative 'bundle manifest'
    $manifestBytes = [IO.File]::ReadAllBytes($manifestPath)
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $manifestBytes) -ceq $BundleSha256) `
        'Evidence-bundle manifest hash mismatch.'
    $manifest = ConvertFrom-GeoCeDGPhaseLifecycleJson $manifestBytes 'evidence-bundle manifest'
    $isEvidencePreservingPrecommitLink = $manifest.schemaVersion -is [long] -and
        $manifest.schemaVersion -eq 2 -and
        [string]$manifest.kind -ceq 'GEOCEDG_EVIDENCE_PRESERVING_PRECOMMIT_LINK'
    if ([string]$Policy.phase -ceq 'G9U1') {
        Assert-GeoCeDGPhaseLifecycle $isEvidencePreservingPrecommitLink `
            'G9U1 author closeout requires exact schema-v2 evidence-preserving bundle authority.'
    }
    if ($isEvidencePreservingPrecommitLink) {
        return Assert-GeoCeDGEvidencePreservingPrecommitLink -RepositoryRoot $RepositoryRoot `
            -TechnicalCommit $TechnicalCommit -CloseoutCommit $CloseoutCommit -Policy $Policy `
            -CloseoutRecord $CloseoutRecord -BundleDirectory $BundleDirectory `
            -BundleSha256 $BundleSha256 -Manifest $manifest -PendingCloseout:$PendingCloseout
    }
    Assert-GeoCeDGPhaseLifecycleProperties $manifest @('schemaVersion', 'kind', 'technicalCommit', 'files') `
        'Evidence-bundle manifest'
    Assert-GeoCeDGPhaseLifecycle ($manifest.schemaVersion -is [long] -and $manifest.schemaVersion -eq 1 -and
        [string]$manifest.kind -ceq 'GEOCEDG_TECHNICAL_EVIDENCE_LINK' -and
        [string]$manifest.technicalCommit -ceq $TechnicalCommit) 'Wrong evidence-bundle authority.'
    $entries = @($manifest.files)
    $paths = @($entries | ForEach-Object { [string]$_.path })
    Assert-GeoCeDGPhaseLifecycle ($paths.Count -eq @($paths | Sort-Object -Unique -CaseSensitive).Count) `
        'Duplicate bundle path.'
    foreach ($entry in $entries) { [void](Get-GeoCeDGPhaseBundleFile $BundleDirectory $entry) }
    $requiredRoles = @('PHASE_A_ROOT', 'PHASE_A_SUMMARY', 'PHASE_B_ROOT', 'PHASE_B_SUMMARY',
        'COMPOSED_ROOT', 'COMPOSED_RECEIPT', 'FULL_ROOT', 'FULL_RECEIPT')
    foreach ($role in $requiredRoles) {
        Assert-GeoCeDGPhaseLifecycle (@($entries | Where-Object { $_.role -ceq $role }).Count -eq 1) `
            "Evidence bundle must contain exactly one $role."
    }
    $byRole = @{}
    foreach ($role in $requiredRoles) { $byRole[$role] = @($entries | Where-Object role -ceq $role)[0] }
    $readEntry = {
        param([string]$Role)
        $path = Resolve-GeoCeDGPhaseLifecycleChild $BundleDirectory ([string]$byRole[$Role].path) 'bundle'
        ConvertFrom-GeoCeDGPhaseLifecycleJson ([IO.File]::ReadAllBytes($path)) $Role
    }
    $phaseA = & $readEntry 'PHASE_A_ROOT'; $phaseB = & $readEntry 'PHASE_B_ROOT'
    foreach ($root in @($phaseA, $phaseB)) {
        Assert-GeoCeDGPhaseLifecycle ([string]$root.level -ceq 'PHASE' -and
            [string]$root.phase -ceq [string]$Policy.phase -and
            [string]$root.repositoryCommit -ceq $TechnicalCommit -and [int]$root.exitCode -eq 0 -and
            [string]$root.state -ceq 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL' -and
            $null -eq $root.failure -and $root.authorApproved -is [bool] -and
            -not $root.authorApproved -and $root.selfApproved -is [bool] -and -not $root.selfApproved) `
            'Invalid PHASE final root.'
    }
    $summaryAEntry = $byRole['PHASE_A_SUMMARY']; $summaryBEntry = $byRole['PHASE_B_SUMMARY']
    Assert-GeoCeDGPhaseLifecycle ([string]$summaryAEntry.sha256 -ceq [string]$summaryBEntry.sha256) `
        'PHASE A/B canonical summaries differ.'
    $summaryA = & $readEntry 'PHASE_A_SUMMARY'; $summaryB = & $readEntry 'PHASE_B_SUMMARY'
    foreach ($summary in @($summaryA, $summaryB)) {
        Assert-GeoCeDGPhaseLifecycle ([string]$summary.phase -ceq [string]$Policy.phase -and
            [string]$summary.state -ceq 'TECHNICAL_FOCUSED_PASSED_NOT_AUTHOR_APPROVAL' -and
            $summary.selfApproved -is [bool] -and -not $summary.selfApproved -and
            $summary.authorApprovedPhase -is [bool] -and -not $summary.authorApprovedPhase -and
            $summary.passClaimed -is [bool] -and -not $summary.passClaimed) `
            'Invalid PHASE canonical summary.'
    }
    $roots = @(
        [pscustomobject]@{ Level = 'COMPOSED'; Root = & $readEntry 'COMPOSED_ROOT'; Receipt = & $readEntry 'COMPOSED_RECEIPT'; ReceiptEntry = $byRole['COMPOSED_RECEIPT'] },
        [pscustomobject]@{ Level = 'FULL'; Root = & $readEntry 'FULL_ROOT'; Receipt = & $readEntry 'FULL_RECEIPT'; ReceiptEntry = $byRole['FULL_RECEIPT'] }
    )
    $metadataPaths = @(@($Policy.closeout.literalReplacements.path) +
        @($Policy.closeout.canonicalLfHashManifests.path) + [string]$Policy.closeout.recordPath |
        Sort-Object -Unique -CaseSensitive)
    $mandatoryUpstream = @('org.geogebra.common.kernel.commands.RedefineTest',
        'org.geogebra.common.euclidian.DrawablesTest',
        'org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest')
    foreach ($pair in $roots) {
        $root = $pair.Root; $receipt = $pair.Receipt
        Assert-GeoCeDGPhaseLifecycle ([string]$root.level -ceq $pair.Level -and
            [string]$root.repositoryCommit -ceq $TechnicalCommit -and [int]$root.exitCode -eq 0 -and
            [string]$root.state -ceq 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL' -and
            $null -eq $root.failure -and $root.authorApproved -is [bool] -and
            -not $root.authorApproved -and $root.selfApproved -is [bool] -and -not $root.selfApproved -and
            [string]$root.canonicalReceipt -ceq [string]$pair.ReceiptEntry.recordedPath) `
            "Invalid $($pair.Level) final root."
        Assert-GeoCeDGPhaseLifecycle ([int]$receipt.schemaVersion -eq 1 -and
            [string]$receipt.kind -ceq 'CURRENT_RUN_BUILD_EVIDENCE' -and
            [string]$receipt.level -ceq $pair.Level -and
            [string]$receipt.state -ceq 'TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING' -and
            [string]$receipt.inputIdentity.head -ceq $TechnicalCommit -and
            $receipt.authorApproved -is [bool] -and -not $receipt.authorApproved -and
            $receipt.selfApproved -is [bool] -and -not $receipt.selfApproved -and
            $receipt.testResultReuseAcrossRuns -is [bool] -and
            -not $receipt.testResultReuseAcrossRuns) "Invalid $($pair.Level) build receipt."
        foreach ($module in @('shared', 'desktop')) {
            $selection = $receipt.selections.$module
            if ($pair.Level -ceq 'FULL') {
                Assert-GeoCeDGPhaseLifecycle ([bool]$selection.unfiltered -and @($selection.filters).Count -eq 0) `
                    "FULL $module selection was not unfiltered."
            }
            $testTask = if ($module -ceq 'shared') { ':shared:common-jre:test' } else { ':desktop:desktop:test' }
            Assert-GeoCeDGPhaseLifecycle (@($receipt.tasks | Where-Object {
                $_.task -ceq $testTask -and $_.outcome -ceq 'EXECUTED'
            }).Count -eq 1) "Mandatory test task was not freshly executed: $testTask"
        }
        $tests = 0; $skipped = 0
        foreach ($report in @($receipt.junit)) {
            Assert-GeoCeDGPhaseLifecycle ([int]$report.failures -eq 0 -and [int]$report.errors -eq 0) `
                "Failed JUnit evidence: $($report.class)"
            if ([string]$report.class -like 'org.geocedg.*' -or [string]$report.class -cin $mandatoryUpstream) {
                Assert-GeoCeDGPhaseLifecycle ([int]$report.skipped -eq 0) "Mandatory JUnit skip: $($report.class)"
            }
            $cases = @($report.cases)
            Assert-GeoCeDGPhaseLifecycle ($cases.Count -eq [int]$report.tests -and
                @($cases | Where-Object status -eq 'FAILURE').Count -eq [int]$report.failures -and
                @($cases | Where-Object status -eq 'ERROR').Count -eq [int]$report.errors -and
                @($cases | Where-Object status -eq 'SKIPPED').Count -eq [int]$report.skipped) `
                "JUnit case counters disagree: $($report.class)"
            $tests += [int]$report.tests; $skipped += [int]$report.skipped
        }
        Assert-GeoCeDGPhaseLifecycle ($tests -eq [int]$receipt.tests -and
            $skipped -eq [int]$receipt.skippedUpstreamTests -and $tests -gt 0) `
            "$($pair.Level) receipt counters disagree."
        $recordedReferences = @($receipt.auditArtifacts | ForEach-Object {
            [pscustomobject]@{ path = $_.path; sha256 = $_.sha256 }
        }) + @($receipt.junit | ForEach-Object {
            [pscustomobject]@{ path = $_.archivePath; sha256 = $_.sha256 }
        }) + @($receipt.checkstyle | ForEach-Object {
            [pscustomobject]@{ path = $_.archivePath; sha256 = $_.sha256 }
        })
        foreach ($reference in $recordedReferences) {
            $matches = @($entries | Where-Object { $_.role -ceq 'ARTIFACT' -and
                [string]$_.recordedPath -ceq [string]$reference.path })
            Assert-GeoCeDGPhaseLifecycle ($matches.Count -eq 1 -and
                [string]$matches[0].sha256 -ceq [string]$reference.sha256) `
                "Missing, duplicate, or mismatched archived artifact: $($reference.path)"
        }
        foreach ($run in @($receipt.nativeRuns)) {
            Assert-GeoCeDGPhaseLifecycle ([int]$run.exitCode -eq 0) 'Canonical native execution failed.'
            Assert-GeoCeDGPhaseLifecycle (@($receipt.auditArtifacts | Where-Object {
                [string]$_.path -ceq [string]$run.logPath
            }).Count -eq 1) 'Native log is not part of the audit inventory.'
        }
        Assert-GeoCeDGPhaseRawEvidenceClosure $RepositoryRoot $TechnicalCommit $receipt $entries `
            $BundleDirectory $metadataPaths -CloseoutCommit $CloseoutCommit -PendingCloseout:$PendingCloseout
    }
    if ($PendingCloseout) {
        Assert-GeoCeDGPhaseLifecycle ($CloseoutCommit -ceq $TechnicalCommit) `
            'Pending closeout has no distinct closeout commit.'
    } else {
        [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $CloseoutCommit)
        $lineText = Invoke-GeoCeDGPhaseLifecycleGitText $RepositoryRoot @('rev-list', '--parents', '-n', '1',
            $CloseoutCommit)
        $line = @($lineText -split ' ')
        Assert-GeoCeDGPhaseLifecycle ($line.Count -eq 2 -and $line[1] -ceq $TechnicalCommit) `
            'Closeout commit is not the direct child of reviewed technical authority.'
    }
    return [pscustomobject][ordered]@{
        kind = 'DOCUMENTARY_TECHNICAL_EVIDENCE_LINK'
        phase = [string]$Policy.phase
        technicalCommit = $TechnicalCommit
        closeoutCommit = $(if ($PendingCloseout) { $null } else { $CloseoutCommit })
        authorApproved = $true
        documentaryEvidenceLinked = $true
        consumableBuildReceipt = $false
    }
}

function Get-GeoCeDGPhaseAuthorCloseoutTargetContext {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [Parameter(Mandatory)] [string]$BundleSha256
    )
    # Verification implementation lives in this script's cohort. RepositoryRoot
    # names the independently checked-out phase target, never an inferred HEAD.
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $root $ReviewedTechnicalCommit)
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $root $CloseoutCommit)
    $materialization = Assert-GeoCeDGWorktreeMaterialization -RepositoryRoot $root -ExpectedCommit $CloseoutCommit
    $relativePolicy = ConvertTo-GeoCeDGRepositoryPath $root $PolicyPath
    $policyT = Get-GeoCeDGPhaseLifecycleBlobBytes $root $ReviewedTechnicalCommit $relativePolicy
    $policyC = Get-GeoCeDGPhaseLifecycleBlobBytes $root $CloseoutCommit $relativePolicy
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $policyT) -ceq
        (Get-GeoCeDGPhaseLifecycleHash $policyC)) 'Closeout changed its reviewed lifecycle policy.'
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $root (Join-Path $root $relativePolicy)
    Assert-GeoCeDGPhaseImplementationAuthority $root $policy
    Assert-GeoCeDGPhaseInfrastructureHistory $root $policy $ReviewedTechnicalCommit
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $root $ReviewedTechnicalCommit $policy
    $recordPath = [string]$policy.closeout.recordPath
    $allowed = @($expected.Keys) + $recordPath
    $treeProof = Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $root `
        -ReviewedCommit $ReviewedTechnicalCommit -CloseoutCommit $CloseoutCommit -AllowedPaths $allowed
    foreach ($path in $expected.Keys) {
        $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $root $CloseoutCommit $path
        Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $actual) -ceq
            (Get-GeoCeDGPhaseLifecycleHash ([byte[]]$expected[$path]))) "Closeout target content mismatch: $path"
        $before = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $ReviewedTechnicalCommit, '--', $path)
        $after = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $CloseoutCommit, '--', $path)
        Assert-GeoCeDGPhaseLifecycle (($before -split ' ')[0] -ceq ($after -split ' ')[0]) `
            "Closeout changed metadata mode: $path"
    }
    $recordEntry = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $CloseoutCommit, '--', $recordPath)
    Assert-GeoCeDGPhaseLifecycle ($recordEntry.StartsWith('100644 blob ', [StringComparison]::Ordinal)) `
        'Closeout decision must be a normal non-executable tracked file.'
    $record = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (Get-GeoCeDGPhaseLifecycleBlobBytes $root $CloseoutCommit $recordPath) 'closeout target record'
    Assert-GeoCeDGPhaseCloseoutRecord $record $policy $ReviewedTechnicalCommit $BundleSha256
    $link = Assert-GeoCeDGTechnicalEvidenceLink -RepositoryRoot $root -TechnicalCommit $ReviewedTechnicalCommit `
        -CloseoutCommit $CloseoutCommit -Policy $policy -CloseoutRecord $record `
        -BundleDirectory $BundleDirectory -BundleSha256 $BundleSha256
    return [pscustomobject][ordered]@{
        mode = 'AUTHOR_CLOSEOUT'
        phase = [string]$policy.phase
        reviewedTechnicalCommit = $ReviewedTechnicalCommit
        closeoutCommit = $CloseoutCommit
        repositoryIdentity = $treeProof
        materialization = $materialization
        documentaryEvidenceLinked = [bool]$link.documentaryEvidenceLinked
        technicalExecutionRepeated = $false
        authorApproved = $true
        selfApproved = $false
    }
}

function Get-GeoCeDGPhasePublishedRegressionContext {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$PublishedAuthorityPath,
        [Parameter(Mandatory)] [string]$ExpectedReviewedTechnicalCommit,
        [Parameter(Mandatory)] [string]$ExpectedCloseoutCommit,
        [Parameter(Mandatory)] [string]$ExpectedPolicyPath
    )
    # Historical approval is proven against exact immutable T/C. This context
    # does not authorize reuse of their tests on the current execution cohort:
    # the caller must still execute all live scientific/current-run assertions.
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    [void](Get-GeoCeDGMaterializationConfig $root)
    $authorityPath = ConvertTo-GeoCeDGPhaseLifecyclePath $PublishedAuthorityPath 'published authority'
    $authority = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        ([IO.File]::ReadAllBytes((Resolve-GeoCeDGPhaseLifecycleChild $root $authorityPath 'published authority'))) `
        'published regression authority'
    Assert-GeoCeDGPhaseLifecycleProperties $authority @('schemaVersion', 'phase', 'mode',
        'reviewedTechnicalCommit', 'closeoutCommit', 'policyPath', 'recordPath', 'authorDecision') `
        'Published regression authority'
    Assert-GeoCeDGPhaseLifecycle ($authority.schemaVersion -is [long] -and $authority.schemaVersion -eq 1 -and
        [string]$authority.mode -ceq 'PUBLISHED_REGRESSION' -and
        [string]$authority.reviewedTechnicalCommit -ceq $ExpectedReviewedTechnicalCommit -and
        [string]$authority.closeoutCommit -ceq $ExpectedCloseoutCommit -and
        [string]$authority.policyPath -ceq $ExpectedPolicyPath -and
        [string]$authority.authorDecision -ceq 'PASS_AUTHOR_APPROVED') `
        'Published regression authority differs from exact approved targets.'
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $ExpectedReviewedTechnicalCommit
    $closeout = Resolve-GeoCeDGPhaseLifecycleCommit $root $ExpectedCloseoutCommit
    $policyPath = ConvertTo-GeoCeDGPhaseLifecyclePath $ExpectedPolicyPath 'published policy'
    $policyT = Get-GeoCeDGPhaseLifecycleBlobBytes $root $technical $policyPath
    $policyC = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $policyPath
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $policyT) -ceq
        (Get-GeoCeDGPhaseLifecycleHash $policyC)) 'Published closeout changed its reviewed lifecycle policy.'
    $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson $policyT 'reviewed published lifecycle policy'
    Assert-GeoCeDGPhaseLifecycleProperties $policy @('schemaVersion', 'phase', 'entryCommit',
        'implementationCommit', 'implementationTree', 'implementationPaths',
        'infrastructureFollowupPaths', 'maximumInfrastructureCommits', 'closeout') 'Reviewed published policy'
    Assert-GeoCeDGPhaseLifecycle ($policy.schemaVersion -is [long] -and $policy.schemaVersion -eq 1 -and
        [string]$policy.phase -ceq [string]$authority.phase -and
        [string]$policy.closeout.recordPath -ceq [string]$authority.recordPath) 'Published phase/policy mismatch.'
    Assert-GeoCeDGPhaseImplementationAuthority $root $policy
    Assert-GeoCeDGPhaseInfrastructureHistory $root $policy $technical
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $root $technical $policy
    $recordPath = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$policy.closeout.recordPath) 'published decision'
    $treeProof = Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $root -ReviewedCommit $technical `
        -CloseoutCommit $closeout -AllowedPaths (@($expected.Keys) + $recordPath)
    $parent = @(Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-list', '--parents', '-n', '1', $closeout)) -split ' '
    Assert-GeoCeDGPhaseLifecycle ($parent.Count -eq 2 -and $parent[1] -ceq $technical) `
        'Published closeout is not the direct status-only child of reviewed technical authority.'
    foreach ($path in $expected.Keys) {
        $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $path
        Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $actual) -ceq
            (Get-GeoCeDGPhaseLifecycleHash ([byte[]]$expected[$path]))) "Published closeout content mismatch: $path"
        $before = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $technical, '--', $path)
        $after = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $closeout, '--', $path)
        Assert-GeoCeDGPhaseLifecycle (($before -split ' ')[0] -ceq ($after -split ' ')[0]) `
            "Published closeout changed metadata mode: $path"
    }
    $recordEntry = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $closeout, '--', $recordPath)
    Assert-GeoCeDGPhaseLifecycle ($recordEntry.StartsWith('100644 blob ', [StringComparison]::Ordinal)) `
        'Published author decision must be a normal non-executable tracked file.'
    $record = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $recordPath) 'published author decision'
    Assert-GeoCeDGPhaseCloseoutRecord $record $policy $technical ([string]$record.evidence.bundleManifestSha256)
    $head = Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-parse', 'HEAD')
    $ancestor = Invoke-GeoCeDGGitByteCommand $root @('merge-base', '--is-ancestor', $closeout, $head) -AllowFailure
    Assert-GeoCeDGPhaseLifecycle ($ancestor.ExitCode -eq 0) 'Published closeout is not ancestral to current execution.'
    return [pscustomobject][ordered]@{
        Mode = 'PUBLISHED_REGRESSION'; CurrentHead = $head; phase = [string]$policy.phase
        ImplementationCommit = [string]$policy.implementationCommit
        CandidatePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.entryCommit) ([string]$policy.implementationCommit))
        InfrastructurePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.implementationCommit) $technical)
        sourceAuthorityCommit = $head; reviewedTechnicalCommit = $technical; closeoutCommit = $closeout
        repositoryIdentity = $treeProof; AuthorApprovedPhase = $true; PassClaimed = $true; SelfApproved = $false
        DocumentaryEvidenceLinked = $false; consumableBuildReceipt = $false
        historicalApprovalAuthenticated = $true; liveScientificVerificationRequired = $true
        currentCohortEquivalentToHistoricalTechnicalExecution = $false
    }
}

function Get-GeoCeDGPhasePublishedTagRegressionContext {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$PassTagName,
        [Parameter(Mandatory)] [string]$ExpectedTagMessage,
        [Parameter(Mandatory)] [string]$ExpectedPhase,
        [Parameter(Mandatory)] [string]$ExpectedImplementationCommit,
        [Parameter(Mandatory)] [string]$ExpectedPolicyPath
    )
    # The annotated phase tag supplies the exact immutable closeout commit.
    # The closeout record supplies the exact reviewed technical commit.  No
    # branch, current-HEAD convention, or "latest" selection participates.
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    [void](Get-GeoCeDGMaterializationConfig $root)
    Assert-GeoCeDGPhaseLifecycle ($PassTagName -cmatch '^[a-z0-9][a-z0-9.-]+$') `
        'Invalid published phase tag name.'
    $tagRef = "refs/tags/$PassTagName"
    $tagObject = Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-parse', '--verify', "${tagRef}^{tag}")
    Assert-GeoCeDGPhaseLifecycle ($tagObject -cmatch '^[0-9a-f]{40}$' -and
        (Invoke-GeoCeDGPhaseLifecycleGitText $root @('cat-file', '-t', $tagObject)) -ceq 'tag') `
        'Published phase authority is not an annotated tag.'
    $payload = Invoke-GeoCeDGPhaseLifecycleGitText $root @('cat-file', '-p', $tagObject)
    $parts = [regex]::Split($payload, '\r?\n\r?\n', 2)
    Assert-GeoCeDGPhaseLifecycle ($parts.Count -eq 2 -and $parts[1] -ceq $ExpectedTagMessage) `
        'Published phase tag message changed.'
    $headers = @($parts[0] -split '\r?\n')
    $objectHeader = @($headers | Where-Object { $_ -cmatch '^object [0-9a-f]{40}$' })
    Assert-GeoCeDGPhaseLifecycle ($objectHeader.Count -eq 1 -and
        @($headers | Where-Object { $_ -ceq 'type commit' }).Count -eq 1 -and
        @($headers | Where-Object { $_ -ceq "tag $PassTagName" }).Count -eq 1) `
        'Published phase tag header changed.'
    $closeout = $objectHeader[0].Substring(7)
    Assert-GeoCeDGPhaseLifecycle ((Invoke-GeoCeDGPhaseLifecycleGitText $root `
        @('rev-parse', '--verify', "${tagRef}^{}")) -ceq $closeout) `
        'Published phase tag peel differs from its commit object.'
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $root $closeout)
    $policyPath = ConvertTo-GeoCeDGPhaseLifecyclePath $ExpectedPolicyPath 'published policy'
    $policyC = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $policyPath
    $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson $policyC 'published tagged lifecycle policy'
    Assert-GeoCeDGPhaseLifecycleProperties $policy @('schemaVersion', 'phase', 'entryCommit',
        'implementationCommit', 'implementationTree', 'implementationPaths',
        'infrastructureFollowupPaths', 'maximumInfrastructureCommits', 'closeout') `
        'Published tagged lifecycle policy'
    Assert-GeoCeDGPhaseLifecycle ($policy.schemaVersion -is [long] -and $policy.schemaVersion -eq 1 -and
        [string]$policy.phase -ceq $ExpectedPhase -and [string]$policy.implementationCommit -ceq
        $ExpectedImplementationCommit) 'Published tagged phase/policy authority changed.'
    Assert-GeoCeDGPhaseImplementationAuthority $root $policy
    $recordPath = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$policy.closeout.recordPath) `
        'published tagged decision'
    $recordEntry = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $closeout, '--', $recordPath)
    Assert-GeoCeDGPhaseLifecycle ($recordEntry.StartsWith('100644 blob ', [StringComparison]::Ordinal)) `
        'Published tagged author decision must be a normal non-executable tracked file.'
    $record = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $recordPath) 'published tagged decision'
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root ([string]$record.reviewedTechnicalCommit)
    $policyT = Get-GeoCeDGPhaseLifecycleBlobBytes $root $technical $policyPath
    Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $policyT) -ceq
        (Get-GeoCeDGPhaseLifecycleHash $policyC)) 'Published tagged closeout changed its lifecycle policy.'
    Assert-GeoCeDGPhaseInfrastructureHistory $root $policy $technical
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $root $technical $policy
    $parent = @(Invoke-GeoCeDGPhaseLifecycleGitText $root `
        @('rev-list', '--parents', '-n', '1', $closeout)) -split ' '
    Assert-GeoCeDGPhaseLifecycle ($parent.Count -eq 2 -and $parent[1] -ceq $technical) `
        'Published tagged closeout is not the direct status-only child of reviewed authority.'
    $treeProof = Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $root `
        -ReviewedCommit $technical -CloseoutCommit $closeout `
        -AllowedPaths (@($expected.Keys) + $recordPath)
    foreach ($path in $expected.Keys) {
        $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $path
        Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $actual) -ceq
            (Get-GeoCeDGPhaseLifecycleHash ([byte[]]$expected[$path]))) `
            "Published tagged closeout content mismatch: $path"
        $before = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $technical, '--', $path)
        $after = Invoke-GeoCeDGPhaseLifecycleGitText $root @('ls-tree', $closeout, '--', $path)
        Assert-GeoCeDGPhaseLifecycle (($before -split ' ')[0] -ceq ($after -split ' ')[0]) `
            "Published tagged closeout changed metadata mode: $path"
    }
    Assert-GeoCeDGPhaseCloseoutRecord $record $policy $technical `
        ([string]$record.evidence.bundleManifestSha256)
    $head = Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-parse', 'HEAD')
    $ancestor = Invoke-GeoCeDGGitByteCommand $root @('merge-base', '--is-ancestor', $closeout, $head) `
        -AllowFailure
    Assert-GeoCeDGPhaseLifecycle ($ancestor.ExitCode -eq 0) `
        'Published tagged closeout is not ancestral to current execution.'
    return [pscustomobject][ordered]@{
        Mode = 'PUBLISHED_REGRESSION'; CurrentHead = $head; phase = [string]$policy.phase
        ImplementationCommit = [string]$policy.implementationCommit
        CandidatePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root `
            ([string]$policy.entryCommit) ([string]$policy.implementationCommit))
        InfrastructurePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root `
            ([string]$policy.implementationCommit) $technical)
        sourceAuthorityCommit = $head; reviewedTechnicalCommit = $technical; closeoutCommit = $closeout
        repositoryIdentity = $treeProof; AuthorApprovedPhase = $true; PassClaimed = $true
        SelfApproved = $false; DocumentaryEvidenceLinked = $false; consumableBuildReceipt = $false
        historicalApprovalAuthenticated = $true; liveScientificVerificationRequired = $true
        currentCohortEquivalentToHistoricalTechnicalExecution = $false; passTagObject = $tagObject
    }
}

function Get-GeoCeDGPhaseLifecycleContext {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [string]$ExpectedImplementationCommit,
        [ValidateSet('PRECOMMIT', 'COMMITTED_CANDIDATE', 'AUTHOR_CLOSEOUT')] [string]$Mode,
        [string]$ReviewedTechnicalCommit,
        [string]$CloseoutRecordPath,
        [string]$BundleDirectory,
        [string]$BundleSha256,
        [switch]$PendingCloseout
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $policy = Read-GeoCeDGPhaseLifecyclePolicy $root $PolicyPath
    if (-not [string]::IsNullOrWhiteSpace($ExpectedImplementationCommit)) {
        Assert-GeoCeDGPhaseLifecycle ($ExpectedImplementationCommit -ceq [string]$policy.implementationCommit) `
            'Unexpected implementation commit in lifecycle policy.'
    }
    Assert-GeoCeDGPhaseImplementationAuthority $root $policy
    $head = Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-parse', 'HEAD')
    if ([string]::IsNullOrWhiteSpace($Mode)) {
        $Mode = if (-not [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit)) { 'AUTHOR_CLOSEOUT' }
            elseif ($head -ceq [string]$policy.entryCommit) { 'PRECOMMIT' } else { 'COMMITTED_CANDIDATE' }
    }
    if ($Mode -ceq 'PRECOMMIT') {
        Assert-GeoCeDGPhaseLifecycle ($head -ceq [string]$policy.entryCommit) 'PRECOMMIT requires entry HEAD.'
        $sets = Get-GeoCeDGPhaseLifecycleStatusPaths $root
        return [pscustomobject][ordered]@{ Mode = $Mode; CurrentHead = $head; phase = [string]$policy.phase;
            ImplementationCommit = [string]$policy.implementationCommit;
            CandidatePaths = @($sets.Unstaged + $sets.Staged + $sets.Untracked | Sort-Object -Unique -CaseSensitive);
            InfrastructurePaths = @(); sourceAuthorityCommit = $null; AuthorApprovedPhase = $false;
            PassClaimed = $false; SelfApproved = $false; DocumentaryEvidenceLinked = $false;
            consumableBuildReceipt = $false }
    }
    if ($Mode -ceq 'COMMITTED_CANDIDATE') {
        Assert-GeoCeDGPhaseInfrastructureHistory $root $policy $head
        $status = Invoke-GeoCeDGPhaseLifecycleGitText $root @('status', '--porcelain=v1', '--untracked-files=all')
        Assert-GeoCeDGPhaseLifecycle ([string]::IsNullOrEmpty($status)) 'COMMITTED_CANDIDATE requires a clean tree and index.'
        return [pscustomobject][ordered]@{ Mode = $Mode; CurrentHead = $head; phase = [string]$policy.phase;
            ImplementationCommit = [string]$policy.implementationCommit;
            CandidatePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.entryCommit) ([string]$policy.implementationCommit));
            InfrastructurePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.implementationCommit) $head);
            sourceAuthorityCommit = $head; AuthorApprovedPhase = $false; PassClaimed = $false;
            SelfApproved = $false; DocumentaryEvidenceLinked = $false; consumableBuildReceipt = $false }
    }
    Assert-GeoCeDGPhaseLifecycle (-not [string]::IsNullOrWhiteSpace($ReviewedTechnicalCommit) -and
        -not [string]::IsNullOrWhiteSpace($CloseoutRecordPath)) `
        'AUTHOR_CLOSEOUT requires exact reviewed authority and closeout record.'
    $relativeRecord = ConvertTo-GeoCeDGRepositoryPath -RepositoryRoot $root -Path $CloseoutRecordPath
    Assert-GeoCeDGPhaseLifecycle ($relativeRecord -ceq [string]$policy.closeout.recordPath) `
        'Unexpected closeout-record path.'
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $root $ReviewedTechnicalCommit)
    Assert-GeoCeDGPhaseInfrastructureHistory $root $policy $ReviewedTechnicalCommit
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $root $ReviewedTechnicalCommit $policy
    $effectivePending = $PendingCloseout -or $head -ceq $ReviewedTechnicalCommit
    $recordBytes = if ($effectivePending) {
        [IO.File]::ReadAllBytes((Resolve-GeoCeDGPhaseLifecycleChild $root $relativeRecord 'closeout record'))
    } else { Get-GeoCeDGPhaseLifecycleBlobBytes $root $head $relativeRecord }
    $record = ConvertFrom-GeoCeDGPhaseLifecycleJson $recordBytes 'closeout record'
    if ([string]::IsNullOrWhiteSpace($BundleDirectory)) {
        $BundleDirectory = Resolve-GeoCeDGPhaseLifecycleChild $root `
            ([string]$record.evidence.bundleDirectory) 'bundle directory'
    } else {
        $declaredBundle = Resolve-GeoCeDGPhaseLifecycleChild $root `
            ([string]$record.evidence.bundleDirectory) 'bundle directory'
        Assert-GeoCeDGPhaseLifecycle ([IO.Path]::GetFullPath($BundleDirectory).Equals($declaredBundle,
            [StringComparison]::OrdinalIgnoreCase)) 'Explicit bundle directory differs from closeout record.'
    }
    if ([string]::IsNullOrWhiteSpace($BundleSha256)) {
        $BundleSha256 = [string]$record.evidence.bundleManifestSha256
    }
    Assert-GeoCeDGPhaseCloseoutRecord $record $policy $ReviewedTechnicalCommit $BundleSha256
    if ($effectivePending) {
        Assert-GeoCeDGPhaseLifecycle ($head -ceq $ReviewedTechnicalCommit) 'Pending closeout requires reviewed technical HEAD.'
        $sets = Get-GeoCeDGPhaseLifecycleStatusPaths $root
        $union = @($sets.Unstaged + $sets.Staged + $sets.Untracked |
            Sort-Object -Unique -CaseSensitive)
        Assert-GeoCeDGPhaseLifecycleSet $union (@($expected.Keys) + $relativeRecord) 'Pending closeout paths'
        Assert-GeoCeDGPhaseLifecycle (@($sets.Unstaged | Where-Object { $_ -cin $sets.Staged }).Count -eq 0) `
            'A pending closeout path cannot have different staged and unstaged content.'
        Assert-GeoCeDGPhaseLifecycle (@($sets.Untracked | Where-Object { $_ -cne $relativeRecord }).Count -eq 0) `
            'Pending closeout contains an unauthorized untracked path.'
        foreach ($path in $expected.Keys) {
            $live = [IO.File]::ReadAllBytes((Resolve-GeoCeDGPhaseLifecycleChild $root $path 'pending closeout'))
            Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $live) -ceq
                (Get-GeoCeDGPhaseLifecycleHash ([byte[]]$expected[$path]))) "Pending closeout content mismatch: $path"
            $indexBlob = Invoke-GeoCeDGGitByteCommand $root @('show', ":$path")
            $indexExpected = if ($path -cin $sets.Staged) { [byte[]]$expected[$path] } else {
                Get-GeoCeDGPhaseLifecycleBlobBytes $root $ReviewedTechnicalCommit $path
            }
            Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $indexBlob.Bytes) -ceq
                (Get-GeoCeDGPhaseLifecycleHash $indexExpected)) "Pending closeout index mismatch: $path"
        }
    } else {
        $lineText = Invoke-GeoCeDGPhaseLifecycleGitText $root @('rev-list', '--parents', '-n', '1', $head)
        $line = @($lineText -split ' ')
        Assert-GeoCeDGPhaseLifecycle ($line.Count -eq 2 -and $line[1] -ceq $ReviewedTechnicalCommit) `
            'Committed closeout must be one direct status-only child.'
        Assert-GeoCeDGPhaseLifecycleSet (Get-GeoCeDGPhaseLifecycleChangedPaths $root $ReviewedTechnicalCommit $head) `
            (@($expected.Keys) + $relativeRecord) 'Committed closeout paths'
        foreach ($path in $expected.Keys) {
            $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $root $head $path
            Assert-GeoCeDGPhaseLifecycle ((Get-GeoCeDGPhaseLifecycleHash $actual) -ceq
                (Get-GeoCeDGPhaseLifecycleHash ([byte[]]$expected[$path]))) "Committed closeout content mismatch: $path"
        }
        $status = Invoke-GeoCeDGPhaseLifecycleGitText $root @('status', '--porcelain=v1', '--untracked-files=all')
        Assert-GeoCeDGPhaseLifecycle ([string]::IsNullOrEmpty($status)) 'Committed closeout requires a clean tree and index.'
    }
    $link = Assert-GeoCeDGTechnicalEvidenceLink -RepositoryRoot $root -TechnicalCommit $ReviewedTechnicalCommit `
        -CloseoutCommit $head -Policy $policy -CloseoutRecord $record -BundleDirectory $BundleDirectory `
        -BundleSha256 $BundleSha256 -PendingCloseout:$effectivePending
    return [pscustomobject][ordered]@{ Mode = $Mode; CurrentHead = $head; phase = [string]$policy.phase;
        ImplementationCommit = [string]$policy.implementationCommit;
        CandidatePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.entryCommit) ([string]$policy.implementationCommit));
        InfrastructurePaths = @(Get-GeoCeDGPhaseLifecycleChangedPaths $root ([string]$policy.implementationCommit) $ReviewedTechnicalCommit);
        sourceAuthorityCommit = $ReviewedTechnicalCommit; AuthorApprovedPhase = $true;
        PassClaimed = $true; SelfApproved = $false;
        DocumentaryEvidenceLinked = [bool]$link.documentaryEvidenceLinked; consumableBuildReceipt = $false }
}

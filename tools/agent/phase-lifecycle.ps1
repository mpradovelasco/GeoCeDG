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
        [string[]]$MetadataPaths
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
    $current = Get-GeoCeDGPhaseRawInputSnapshot $RepositoryRoot
    $recordPath = @($MetadataPaths | Select-Object -Last 1)
    $expectedCurrentPaths = @($paths) + @($recordPath | Where-Object { $_ -cnotin $paths })
    Assert-GeoCeDGPhaseLifecycleSet @($current.Records.path) $expectedCurrentPaths 'Current closeout input paths'
    $archived = @{}; foreach ($record in $inventory) { $archived[[string]$record.path] = $record }
    foreach ($record in @($current.Records)) {
        $path = [string]$record.path
        if ($path -cin $MetadataPaths) { continue }
        Assert-GeoCeDGPhaseLifecycle ($archived.ContainsKey($path) -and [bool]$record.exists -and
            [long]$record.bytes -eq [long]$archived[$path].bytes -and
            [string]$record.sha256 -ceq [string]$archived[$path].sha256) `
            "Current raw input differs from reviewed evidence: $path"
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

function Get-GeoCeDGPhaseExpectedCloseoutBytes {
    param([string]$RepositoryRoot, [string]$TechnicalCommit, [object]$Policy)
    $expected = [ordered]@{}
    foreach ($rule in @($Policy.closeout.literalReplacements)) {
        Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'before', 'after', 'occurrences') 'Literal replacement'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$rule.path) 'replacement'
        Assert-GeoCeDGPhaseLifecycle (-not $expected.Contains($path)) "Duplicate closeout output: $path"
        Assert-GeoCeDGPhaseLifecycle ($rule.occurrences -is [long] -and $rule.occurrences -gt 0) `
            "Invalid replacement count: $path"
        $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $TechnicalCommit $path
        $text = ConvertFrom-GeoCeDGStrictUtf8 $bytes
        $matches = [regex]::Matches($text, [regex]::Escape([string]$rule.before)).Count
        Assert-GeoCeDGPhaseLifecycle ($matches -eq [int]$rule.occurrences -and
            -not [string]::IsNullOrEmpty([string]$rule.before)) "Replacement authority mismatch: $path"
        $result = $text.Replace([string]$rule.before, [string]$rule.after)
        $expected[$path] = [Text.UTF8Encoding]::new($false).GetBytes($result)
    }
    foreach ($rule in @($Policy.closeout.canonicalLfHashManifests)) {
        Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'authorityPaths') 'Hash-manifest rule'
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$rule.path) 'hash manifest'
        Assert-GeoCeDGPhaseLifecycle (-not $expected.Contains($path)) "Duplicate closeout output: $path"
        $lines = foreach ($authority in @($rule.authorityPaths)) {
            $authorityPath = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$authority) 'hash authority'
            $authorityBytes = if ($expected.Contains($authorityPath)) { [byte[]]$expected[$authorityPath] } else {
                Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $TechnicalCommit $authorityPath
            }
            $hash = Get-GeoCeDGPhaseLifecycleHash (ConvertTo-GeoCeDGCanonicalLfBytes $authorityBytes)
            "$hash  $authorityPath"
        }
        $expected[$path] = [Text.UTF8Encoding]::new($false).GetBytes(($lines -join "`n") + "`n")
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
    $metadataPaths = @($Policy.closeout.literalReplacements.path) +
        @($Policy.closeout.canonicalLfHashManifests.path) + [string]$Policy.closeout.recordPath
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
            $BundleDirectory $metadataPaths
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

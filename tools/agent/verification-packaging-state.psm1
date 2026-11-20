#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

# Repository-integrity inventory (operational simplification, 2026-11-20).
#
# The protected repository state is derived from Git inventories, never from a
# recursive walk of the whole checkout:
#   - every tracked path (index), hashed from its raw working-tree bytes;
#   - every untracked, non-ignored path;
#   - every Git-ignored entry that is not a declared generated output.
# Declared generated outputs are Git-ignored and are excluded from the global
# integrity hash; they are not enumerated or read:
#   - the top-level `artifacts` tree (generated evidence and outputs); and
#   - Git-ignored directories named `build`, `.gradle` or `.kotlin` (Gradle
#     outputs and caches).
# A tracked file inside an excluded tree is still protected through the tracked
# inventory. `.git` is never traversed. Links and junctions are recorded by
# their target and never followed. Operation-specific generated files (for
# example packaging ZIP/MSI/EXE, SBOM and checksums) keep their own explicit
# checks; this module only answers whether the repository was preserved.
$script:VerificationPackagingInventoryScheme = 'GIT_INVENTORY_V1'
$script:VerificationPackagingExcludedTopLevelRoots = [string[]]@('artifacts')
$script:VerificationPackagingGeneratedDirectoryNames = [string[]]@('build', '.gradle', '.kotlin')

function Resolve-VerificationPackagingWriteRoots {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DeclaredWriteRoots
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    $resolved = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($value in $DeclaredWriteRoots) {
        $transportValues = ([string]$value).Split(',', [StringSplitOptions]::RemoveEmptyEntries)
        foreach ($transportValue in $transportValues) {
            $relative = ([string]$transportValue).Replace('\', '/').Trim()
            while ($relative.StartsWith('./', [StringComparison]::Ordinal)) {
                $relative = $relative.Substring(2)
            }
            if ([string]::IsNullOrWhiteSpace($relative) -or
                    $relative.StartsWith('/', [StringComparison]::Ordinal) -or
                    [IO.Path]::IsPathRooted($relative) -or
                    $relative -match '(^|/)\.\.(/|$)' -or
                    $relative -match '(^|/)//' -or
                    $relative -eq '.git' -or
                    $relative.StartsWith('.git/', [StringComparison]::Ordinal)) {
                throw "Declared packaging write root is not repository-contained: $transportValue"
            }
            $candidate = [IO.Path]::GetFullPath((Join-Path $root ($relative.Replace('/', [IO.Path]::DirectorySeparatorChar))))
            $candidateRelative = [IO.Path]::GetRelativePath($root, $candidate).Replace('\', '/')
            if ($candidateRelative -eq '..' -or $candidateRelative.StartsWith('../', [StringComparison]::Ordinal)) {
                throw "Declared packaging write root escapes the repository: $transportValue"
            }
            [void]$resolved.Add($candidateRelative.Trim('/'))
        }
    }
    if ($resolved.Count -eq 0) {
        throw 'At least one declared packaging write root is required.'
    }
    $values = [string[]]@($resolved.GetEnumerator())
    [Array]::Sort($values, [StringComparer]::Ordinal)
    return $values
}

function Test-VerificationPackagingPathUnderRoot {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Roots
    )
    foreach ($root in $Roots) {
        if ($RelativePath -ceq $root -or
                $RelativePath.StartsWith($root + '/', [StringComparison]::Ordinal)) {
            return $true
        }
    }
    return $false
}

function Get-VerificationPackagingGitEntries {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments
    )

    $git = Get-Command git -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $git) { throw 'Git executable is unavailable.' }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = [IO.Path]::GetFullPath($git.Path)
    $start.WorkingDirectory = [IO.Path]::GetFullPath($RepositoryRoot)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false)
    foreach ($argument in @('-c', 'core.quotePath=false', '-C', $RepositoryRoot) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Git inventory inspection did not start.' }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            throw ("Git inventory inspection exited {0}: {1}" -f
                $process.ExitCode, $stderr.GetAwaiter().GetResult().Trim())
        }
        $entries = [Collections.Generic.List[string]]::new()
        foreach ($entry in $stdout.GetAwaiter().GetResult().Split([char]0,
                [StringSplitOptions]::RemoveEmptyEntries)) {
            $entries.Add(([string]$entry).Replace('\', '/'))
        }
        return [string[]]$entries.ToArray()
    } finally {
        $process.Dispose()
    }
}

function Get-VerificationPackagingTrackedPaths {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $paths = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($path in Get-VerificationPackagingGitEntries -RepositoryRoot $RepositoryRoot `
            -Arguments @('ls-files', '--cached', '-z', '--')) {
        [void]$paths.Add($path)
    }
    return , $paths
}

function Test-VerificationPackagingExcludedGeneratedEntry {
    # Classifies one Git-ignored entry as reported by
    # `git ls-files --others --ignored --exclude-standard --directory`; a
    # directory entry ends with '/'. Only ignored entries are ever classified.
    param([Parameter(Mandatory)] [string]$Entry)

    $isDirectory = $Entry.EndsWith('/', [StringComparison]::Ordinal)
    $path = $Entry.TrimEnd('/')
    if ([string]::IsNullOrEmpty($path)) { return $false }
    $segments = $path.Split('/')
    if ($script:VerificationPackagingExcludedTopLevelRoots -ccontains $segments[0]) {
        return $true
    }
    return ($isDirectory -and
        $script:VerificationPackagingGeneratedDirectoryNames -ccontains $segments[-1])
}

function New-VerificationPackagingEntryRecord {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [IO.FileSystemInfo]$Item,
        [Parameter(Mandatory)] [hashtable]$Metrics
    )

    if (($Item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
        $Metrics.links++
        $target = $Item.LinkTarget
        return [ordered]@{
            kind = 'LINK'
            path = $RelativePath
            target = $(if ($null -eq $target) { '<unresolved-reparse-point>' } else { [string]$target })
        }
    }
    $stream = [IO.File]::Open($Item.FullName, [IO.FileMode]::Open, [IO.FileAccess]::Read,
        [IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete)
    try {
        $digest = [Security.Cryptography.SHA256]::HashData($stream)
    } finally {
        $stream.Dispose()
    }
    $length = [long]$Item.Length
    $Metrics.files_hashed++
    $Metrics.bytes_hashed += $length
    return [ordered]@{
        bytes = $length
        path = $RelativePath
        sha256 = [Convert]::ToHexString($digest).ToLowerInvariant()
    }
}

function Add-VerificationPackagingPathRecords {
    # Adds the record of one repository-relative path. A directory is walked
    # without following links or junctions and without entering nested Git
    # metadata; an absent tracked path is recorded explicitly.
    # Inside a Git-ignored entry every descendant is ignored as well, so a
    # descendant directory named like a Gradle output is excluded there too
    # (Git may report an ignored parent before its ignored output children).
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [hashtable]$Records,
        [Parameter(Mandatory)] [hashtable]$Metrics,
        [Collections.Generic.HashSet[string]]$ExcludedDirectories
    )

    $relative = $RelativePath.TrimEnd('/')
    if ([string]::IsNullOrEmpty($relative) -or $Records.ContainsKey($relative)) { return }
    $full = [IO.Path]::Combine($Root, $relative.Replace('/', [IO.Path]::DirectorySeparatorChar))
    $item = $null
    if ([IO.Directory]::Exists($full)) {
        $item = [IO.DirectoryInfo]::new($full)
    } elseif ([IO.File]::Exists($full)) {
        $item = [IO.FileInfo]::new($full)
    } else {
        $link = [IO.FileInfo]::new($full)
        if ($null -ne $link.LinkTarget) { $item = $link }
    }
    if ($null -eq $item) {
        $Records[$relative] = [ordered]@{ kind = 'ABSENT'; path = $relative }
        return
    }
    if ($item -is [IO.FileInfo] -or
            (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0)) {
        $Records[$relative] = New-VerificationPackagingEntryRecord -RelativePath $relative `
            -Item $item -Metrics $Metrics
        return
    }
    $pending = [Collections.Generic.Stack[object]]::new()
    $pending.Push([pscustomobject]@{ relative = $relative; directory = $item })
    while ($pending.Count -gt 0) {
        $current = $pending.Pop()
        $Metrics.directories_walked++
        foreach ($child in $current.directory.EnumerateFileSystemInfos()) {
            $childRelative = $current.relative + '/' + $child.Name
            if ($Records.ContainsKey($childRelative)) { continue }
            $isLink = ($child.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0
            if ($child.Name -ceq '.git' -and -not $isLink) {
                $Records[$childRelative] = [ordered]@{ kind = 'NESTED_GIT_METADATA'; path = $childRelative }
                continue
            }
            if ($child -is [IO.DirectoryInfo] -and -not $isLink -and $null -ne $ExcludedDirectories -and
                    $script:VerificationPackagingGeneratedDirectoryNames -ccontains $child.Name) {
                [void]$ExcludedDirectories.Add($childRelative)
                continue
            }
            if ($child -is [IO.DirectoryInfo] -and -not $isLink) {
                $pending.Push([pscustomobject]@{ relative = $childRelative; directory = $child })
                continue
            }
            $Records[$childRelative] = New-VerificationPackagingEntryRecord `
                -RelativePath $childRelative -Item $child -Metrics $Metrics
        }
    }
}

function Get-VerificationPackagingStateSnapshot {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DeclaredWriteRoots
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    $roots = Resolve-VerificationPackagingWriteRoots -RepositoryRoot $root -DeclaredWriteRoots $DeclaredWriteRoots
    $trackedPaths = Get-VerificationPackagingTrackedPaths -RepositoryRoot $root
    $untracked = @(Get-VerificationPackagingGitEntries -RepositoryRoot $root `
        -Arguments @('ls-files', '--others', '--exclude-standard', '-z', '--'))
    $ignored = @(Get-VerificationPackagingGitEntries -RepositoryRoot $root `
        -Arguments @('ls-files', '--others', '--ignored', '--exclude-standard', '--directory', '-z', '--'))

    $metrics = @{ files_hashed = 0; bytes_hashed = [long]0; links = 0; directories_walked = 0 }
    $records = @{}
    foreach ($path in $trackedPaths) {
        Add-VerificationPackagingPathRecords -Root $root -RelativePath $path -Records $records -Metrics $metrics
    }
    foreach ($entry in $untracked) {
        Add-VerificationPackagingPathRecords -Root $root -RelativePath $entry -Records $records -Metrics $metrics
    }
    $excludedEntries = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $walkedIgnored = [Collections.Generic.List[string]]::new()
    foreach ($entry in $ignored) {
        if (Test-VerificationPackagingExcludedGeneratedEntry -Entry $entry) {
            [void]$excludedEntries.Add($entry.TrimEnd('/'))
        } else {
            $walkedIgnored.Add($entry)
        }
    }
    foreach ($entry in $walkedIgnored) {
        $relative = $entry.TrimEnd('/')
        $insideExcluded = $false
        foreach ($excludedEntry in $excludedEntries) {
            if ($relative.StartsWith($excludedEntry + '/', [StringComparison]::Ordinal)) {
                $insideExcluded = $true
                break
            }
        }
        if ($insideExcluded) { continue }
        Add-VerificationPackagingPathRecords -Root $root -RelativePath $entry -Records $records `
            -Metrics $metrics -ExcludedDirectories $excludedEntries
    }

    $orderedPaths = [string[]]@($records.Keys)
    [Array]::Sort($orderedPaths, [StringComparer]::Ordinal)
    $declared = [Collections.Generic.List[object]]::new()
    $protected = [Collections.Generic.List[object]]::new()
    foreach ($path in $orderedPaths) {
        $isInside = Test-VerificationPackagingPathUnderRoot -RelativePath $path -Roots $roots
        if ($trackedPaths.Contains($path)) { $isInside = $false }
        if ($isInside) { $declared.Add($records[$path]) } else { $protected.Add($records[$path]) }
    }
    $excluded = [string[]]@($excludedEntries)
    [Array]::Sort($excluded, [StringComparer]::Ordinal)
    return [ordered]@{
        inventory_scheme = $script:VerificationPackagingInventoryScheme
        declared_write_roots = $roots
        declared_state_sha256 = Get-VerificationDeterministicHash -Value ([object[]]$declared.ToArray())
        declared_file_count = $declared.Count
        protected_state_sha256 = Get-VerificationDeterministicHash -Value ([object[]]$protected.ToArray())
        protected_file_count = $protected.Count
        excluded_generated_entries = $excluded
        excluded_generated_entry_count = $excluded.Count
        files_hashed = [int]$metrics.files_hashed
        bytes_hashed = [long]$metrics.bytes_hashed
        links_recorded = [int]$metrics.links
    }
}

Export-ModuleMember -Function @(
    'Resolve-VerificationPackagingWriteRoots',
    'Get-VerificationPackagingStateSnapshot',
    'Test-VerificationPackagingExcludedGeneratedEntry'
)

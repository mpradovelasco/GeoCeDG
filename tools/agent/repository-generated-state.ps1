Set-StrictMode -Version Latest

function Assert-VerificationLogDirectoryOutsideGeneratedState {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$LogDirectory
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $logs = [IO.Path]::GetFullPath($LogDirectory)
    foreach ($path in @($root, $logs)) {
        if ($path.StartsWith('\\?\', [StringComparison]::Ordinal) -or
                $path.StartsWith('\\.\', [StringComparison]::Ordinal)) {
            throw "Verification log safety requires ordinary filesystem paths, not device-prefixed aliases: $path"
        }
    }
    if ($root.Length -gt [IO.Path]::GetPathRoot($root).Length) { $root = $root.TrimEnd('/', '\') }
    if ($logs.Length -gt [IO.Path]::GetPathRoot($logs).Length) { $logs = $logs.TrimEnd('/', '\') }
    $rootPrefix = $root.TrimEnd('/', '\') + [IO.Path]::DirectorySeparatorChar
    if ($logs.Equals($root, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Verification logs cannot use the repository root: $logs"
    }
    if ($logs.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        $relative = [IO.Path]::GetRelativePath($root, $logs)
        foreach ($component in ($relative -split '[\\/]')) {
            if ($component -in @('build', '.gradle', '.kotlin')) {
                throw "Verification logs must stay outside generated-state directories (build/.gradle/.kotlin): $logs"
            }
        }
    }
    # Snapshot cleanup owns this tree regardless of Keep/Clean flags. Evidence
    # must not become a child of a backup that a later restoration can remove.
    $backupRoot = [IO.Path]::GetFullPath((Join-Path ([IO.Path]::GetTempPath()) `
            'geocedg-generated-state')).TrimEnd('/', '\')
    if ($logs.Equals($backupRoot, [StringComparison]::OrdinalIgnoreCase) -or
            $logs.StartsWith($backupRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
        throw "Verification logs must stay outside the generated-state backup tree: $logs"
    }

    # Inspect only each existing path/ancestor, including the repository's own
    # ancestry. No enumeration, Git, creation, snapshot or mutation is needed.
    $inspected = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($candidate in @($root, $logs)) {
        $ancestor = $candidate
        $volumeRoot = [IO.Path]::GetPathRoot($candidate).TrimEnd('/', '\')
        while (-not [string]::IsNullOrWhiteSpace($ancestor)) {
            if (-not $inspected.Add($ancestor)) { break }
            $item = $null
            try { $item = Get-Item -LiteralPath $ancestor -Force -ErrorAction Stop }
            catch [System.Management.Automation.ItemNotFoundException] { }
            if ($null -ne $item -and ($item.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
                throw "Verification logs cannot use linked repository/log ancestry: $ancestor"
            }
            if ($ancestor.TrimEnd('/', '\').Equals($volumeRoot, [StringComparison]::OrdinalIgnoreCase)) { break }
            $ancestor = Split-Path -Parent $ancestor
        }
    }
}

function Get-RepositoryStatusText {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $status = & git -C $RepositoryRoot status --porcelain=v1 --untracked-files=all
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read repository status."
    }
    return ($status -join "`n")
}

function Get-RepositoryGeneratedDirectories {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DirectoryNames
    )

    $paths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase)
    $entries = @(& git -C $RepositoryRoot ls-files --others --directory `
        --exclude-standard)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate untracked generated directories."
    }
    $entries += @(& git -C $RepositoryRoot ls-files --others --directory `
        --ignored --exclude-standard)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate ignored generated directories."
    }

    foreach ($entry in $entries) {
        if ([string]::IsNullOrWhiteSpace($entry)) {
            continue
        }
        $relative = $entry.TrimEnd("/", "\")
        if ($DirectoryNames -contains (Split-Path -Leaf $relative)) {
            [void]$paths.Add([IO.Path]::GetFullPath(
                    (Join-Path $RepositoryRoot $relative)))
        }
    }

    $selected = [Collections.Generic.List[string]]::new()
    foreach ($candidate in @($paths | Sort-Object Length)) {
        $covered = @($selected | Where-Object {
                $candidate.StartsWith(
                    $_ + [IO.Path]::DirectorySeparatorChar,
                    [StringComparison]::OrdinalIgnoreCase)
            }).Count -gt 0
        if (-not $covered) {
            $selected.Add($candidate)
        }
    }
    return @($selected)
}

function Assert-GeneratedDirectoryTarget {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string[]]$DirectoryNames
    )

    $rootPrefix = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not $absolute.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to manage generated output outside repository: $absolute"
    }
    if ($DirectoryNames -notcontains (Split-Path -Leaf $absolute)) {
        throw "Refusing to manage unexpected generated directory: $absolute"
    }
    $ancestor = $absolute
    while ($ancestor.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        if (Test-Path -LiteralPath $ancestor) {
            $item = Get-Item -LiteralPath $ancestor -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Refusing to manage generated output through a linked path: $ancestor"
            }
        }
        $ancestor = Split-Path -Parent $ancestor
    }
    if ((Test-Path -LiteralPath $RepositoryRoot) -and
            ((Get-Item -LiteralPath $RepositoryRoot -Force).Attributes -band
                [IO.FileAttributes]::ReparsePoint)) {
        throw "Refusing to manage generated output through a linked repository root: $RepositoryRoot"
    }
}

function New-RepositoryGeneratedStateSnapshot {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DirectoryNames,
        [ValidatePattern('^[A-Za-z0-9_-]+$')] [string]$Label = "verification",
        [switch]$KeepCurrentOutputs
    )

    # Opt-in retention needs no backup, enumeration, or restoration. In particular,
    # do not copy the whole build tree only to delete that copy at the end.
    if ($KeepCurrentOutputs) {
        return [pscustomobject]@{
            RepositoryRoot = [IO.Path]::GetFullPath($RepositoryRoot)
            DirectoryNames = @($DirectoryNames)
            SnapshotRoot = $null
            Entries = @()
            SnapshotSkipped = $true
        }
    }

    $snapshotRoot = Join-Path ([IO.Path]::GetTempPath()) (
        "geocedg-generated-state\{0}-{1}" -f $Label, [guid]::NewGuid())
    [void](Assert-GeneratedStateSnapshotRoot -Snapshot ([pscustomobject]@{
        SnapshotRoot = $snapshotRoot
    }))
    [void](New-Item -ItemType Directory -Path $snapshotRoot -Force)
    $entries = [Collections.Generic.List[object]]::new()
    $index = 0
    foreach ($path in @(Get-RepositoryGeneratedDirectories `
            -RepositoryRoot $RepositoryRoot -DirectoryNames $DirectoryNames)) {
        Assert-GeneratedDirectoryTarget -RepositoryRoot $RepositoryRoot `
            -Path $path -DirectoryNames $DirectoryNames
        $backup = Join-Path $snapshotRoot ("entry-{0:D4}" -f $index)
        Copy-Item -LiteralPath $path -Destination $backup -Recurse -Force
        $entries.Add([pscustomobject]@{
                OriginalPath = $path
                BackupPath = $backup
            })
        $index++
    }

    return [pscustomobject]@{
        RepositoryRoot = [IO.Path]::GetFullPath($RepositoryRoot)
        DirectoryNames = @($DirectoryNames)
        SnapshotRoot = $snapshotRoot
        Entries = @($entries)
        SnapshotSkipped = $false
    }
}

function Assert-GeneratedStateSnapshotRoot {
    param([Parameter(Mandatory)] [object]$Snapshot)

    $allowedRoot = [IO.Path]::GetFullPath((Join-Path ([IO.Path]::GetTempPath()) `
            "geocedg-generated-state")).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    $snapshotRoot = [IO.Path]::GetFullPath([string]$Snapshot.SnapshotRoot)
    if (-not $snapshotRoot.StartsWith(
            $allowedRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to manage unexpected generated-state snapshot: $snapshotRoot"
    }
    if ((Split-Path -Parent $snapshotRoot).TrimEnd('/', '\') -ne
            $allowedRoot.TrimEnd('/', '\')) {
        throw "Refusing to manage a nested/non-owned generated-state snapshot: $snapshotRoot"
    }
    if ((Test-Path -LiteralPath $allowedRoot) -and
            ((Get-Item -LiteralPath $allowedRoot -Force).Attributes -band
                [IO.FileAttributes]::ReparsePoint)) {
        throw "Refusing to manage a snapshot through a linked generated-state parent: $allowedRoot"
    }
    if (Test-Path -LiteralPath $Snapshot.SnapshotRoot -PathType Container) {
        if ((Get-Item -LiteralPath $snapshotRoot -Force).Attributes -band
                [IO.FileAttributes]::ReparsePoint) {
            throw "Refusing to manage a linked generated-state snapshot: $snapshotRoot"
        }
    }
    return $snapshotRoot
}

function Remove-RepositoryGeneratedStateSnapshot {
    param([Parameter(Mandatory)] [object]$Snapshot)

    if ($null -ne $Snapshot.PSObject.Properties['SnapshotSkipped'] -and
            $Snapshot.SnapshotSkipped) {
        return
    }
    $snapshotRoot = Assert-GeneratedStateSnapshotRoot -Snapshot $Snapshot
    if (Test-Path -LiteralPath $snapshotRoot -PathType Container) {
        Remove-Item -LiteralPath $snapshotRoot -Recurse -Force
    }
}

function Restore-RepositoryGeneratedStateSnapshot {
    param(
        [Parameter(Mandatory)] [object]$Snapshot,
        [switch]$KeepCurrentOutputs,
        [string]$Description = "generated output"
    )

    if ($KeepCurrentOutputs) {
        Write-Host "Keeping $Description because -KeepBuildOutputs was supplied."
        Remove-RepositoryGeneratedStateSnapshot -Snapshot $Snapshot
        return
    }
    if ($null -ne $Snapshot.PSObject.Properties['SnapshotSkipped'] -and
            $Snapshot.SnapshotSkipped) {
        throw "Cannot restore outputs from a snapshot explicitly skipped for retention."
    }

    try {
        # Validate every backup before deleting any current output. A failed
        # restoration retains all recovery data instead of destroying it in finally.
        $snapshotRoot = Assert-GeneratedStateSnapshotRoot -Snapshot $Snapshot
        if (-not (Test-Path -LiteralPath $snapshotRoot -PathType Container)) {
            throw "Generated-state snapshot root is missing: $snapshotRoot"
        }
        $backupPrefix = [IO.Path]::GetFullPath([string]$Snapshot.SnapshotRoot).TrimEnd(
            '/', '\') + [IO.Path]::DirectorySeparatorChar
        foreach ($entry in @($Snapshot.Entries)) {
            $backup = [IO.Path]::GetFullPath([string]$entry.BackupPath)
            Assert-GeneratedDirectoryTarget -RepositoryRoot $Snapshot.RepositoryRoot `
                -Path $entry.OriginalPath -DirectoryNames $Snapshot.DirectoryNames
            if (-not $backup.StartsWith($backupPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                    -not (Test-Path -LiteralPath $backup -PathType Container)) {
                throw "Generated-state recovery entry is missing or outside its snapshot: $backup"
            }
            $ancestor = $backup
            while ($ancestor.StartsWith($backupPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                    $ancestor.Equals($backupPrefix.TrimEnd('/', '\'), [StringComparison]::OrdinalIgnoreCase)) {
                if ((Get-Item -LiteralPath $ancestor -Force).Attributes -band
                        [IO.FileAttributes]::ReparsePoint) {
                    throw "Generated-state recovery entry uses a linked path: $ancestor"
                }
                $ancestor = Split-Path -Parent $ancestor
            }
        }
        # New current outputs need the same complete-list preflight as backups:
        # a late invalid target must not follow removal of an earlier valid one.
        $targets = @(Get-RepositoryGeneratedDirectories `
            -RepositoryRoot $Snapshot.RepositoryRoot `
            -DirectoryNames $Snapshot.DirectoryNames)
        foreach ($path in $targets) {
            Assert-GeneratedDirectoryTarget -RepositoryRoot $Snapshot.RepositoryRoot `
                -Path $path -DirectoryNames $Snapshot.DirectoryNames
        }
        foreach ($path in $targets) {
            if (Test-Path -LiteralPath $path -PathType Container) {
                Write-Host "Removing current ${Description}: $path"
                Remove-Item -LiteralPath $path -Recurse -Force
            }
        }

        foreach ($entry in @($Snapshot.Entries)) {
            Assert-GeneratedDirectoryTarget -RepositoryRoot $Snapshot.RepositoryRoot `
                -Path $entry.OriginalPath -DirectoryNames $Snapshot.DirectoryNames
            [void](New-Item -ItemType Directory `
                -Path (Split-Path -Parent $entry.OriginalPath) -Force)
            Copy-Item -LiteralPath $entry.BackupPath `
                -Destination $entry.OriginalPath -Recurse -Force
            Write-Host "Restored pre-existing ${Description}: $($entry.OriginalPath)"
        }
    } catch {
        throw ("Generated-state restoration failed; recovery data retained at " +
            "$($Snapshot.SnapshotRoot). $($_.Exception.Message)")
    }
    Remove-RepositoryGeneratedStateSnapshot -Snapshot $Snapshot
}

function Clear-RepositoryGeneratedOutputs {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DirectoryNames
    )

    # Only callers owning a snapshot or explicitly retaining replacement outputs
    # may request this clean-output path. Never touches the dependency/user cache.
    $targets = @(Get-RepositoryGeneratedDirectories -RepositoryRoot $RepositoryRoot `
        -DirectoryNames $DirectoryNames)
    foreach ($path in $targets) {
        Assert-GeneratedDirectoryTarget -RepositoryRoot $RepositoryRoot `
            -Path $path -DirectoryNames $DirectoryNames
    }
    foreach ($path in $targets) {
        if (Test-Path -LiteralPath $path -PathType Container) {
            Write-Host "Clearing generated build output for clean verification: $path"
            Remove-Item -LiteralPath $path -Recurse -Force
        }
    }
}

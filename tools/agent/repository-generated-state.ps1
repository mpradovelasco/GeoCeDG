Set-StrictMode -Version Latest

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
}

function New-RepositoryGeneratedStateSnapshot {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DirectoryNames,
        [string]$Label = "verification"
    )

    $snapshotRoot = Join-Path ([IO.Path]::GetTempPath()) (
        "geocedg-generated-state\{0}-{1}" -f $Label, [guid]::NewGuid())
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
    }
}

function Remove-RepositoryGeneratedStateSnapshot {
    param([Parameter(Mandatory)] [object]$Snapshot)

    $allowedRoot = [IO.Path]::GetFullPath((Join-Path ([IO.Path]::GetTempPath()) `
            "geocedg-generated-state")).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    $snapshotRoot = [IO.Path]::GetFullPath([string]$Snapshot.SnapshotRoot)
    if (-not $snapshotRoot.StartsWith(
            $allowedRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove unexpected generated-state snapshot: $snapshotRoot"
    }
    if (Test-Path -LiteralPath $Snapshot.SnapshotRoot -PathType Container) {
        Remove-Item -LiteralPath $Snapshot.SnapshotRoot -Recurse -Force
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

    try {
        foreach ($path in @(Get-RepositoryGeneratedDirectories `
                -RepositoryRoot $Snapshot.RepositoryRoot `
                -DirectoryNames $Snapshot.DirectoryNames)) {
            Assert-GeneratedDirectoryTarget -RepositoryRoot $Snapshot.RepositoryRoot `
                -Path $path -DirectoryNames $Snapshot.DirectoryNames
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
    } finally {
        Remove-RepositoryGeneratedStateSnapshot -Snapshot $Snapshot
    }
}

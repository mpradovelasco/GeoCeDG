#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

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

function Get-VerificationPackagingTrackedPaths {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

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
    foreach ($argument in @('-C', $RepositoryRoot, 'ls-files', '--cached', '-z', '--')) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Git tracked-path inspection did not start.' }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            throw ("Git tracked-path inspection exited {0}: {1}" -f
                $process.ExitCode, $stderr.GetAwaiter().GetResult().Trim())
        }
        $paths = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
        foreach ($path in $stdout.GetAwaiter().GetResult().Split([char]0,
                [StringSplitOptions]::RemoveEmptyEntries)) {
            [void]$paths.Add(([string]$path).Replace('\', '/'))
        }
        return $paths
    } finally {
        $process.Dispose()
    }
}

function Get-VerificationPackagingFileRecords {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DeclaredWriteRoots,
        [Parameter(Mandatory)] [Collections.Generic.HashSet[string]]$TrackedPaths,
        [Parameter(Mandatory)] [bool]$InsideDeclaredRoots
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    $records = [Collections.Generic.List[object]]::new()
    foreach ($file in Get-ChildItem -LiteralPath $root -File -Recurse -Force -ErrorAction Stop |
            Sort-Object FullName) {
        $relative = [IO.Path]::GetRelativePath($root, $file.FullName).Replace('\', '/')
        if ($relative -eq '.git' -or $relative.StartsWith('.git/', [StringComparison]::Ordinal)) {
            continue
        }
        $isInside = Test-VerificationPackagingPathUnderRoot -RelativePath $relative -Roots $DeclaredWriteRoots
        if ($TrackedPaths.Contains($relative)) { $isInside = $false }
        if ($isInside -ne $InsideDeclaredRoots) { continue }
        $records.Add([ordered]@{
            bytes = [long]$file.Length
            path = $relative
            sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        })
    }
    return [object[]]$records.ToArray()
}

function Get-VerificationPackagingStateSnapshot {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$DeclaredWriteRoots
    )

    $roots = Resolve-VerificationPackagingWriteRoots -RepositoryRoot $RepositoryRoot -DeclaredWriteRoots $DeclaredWriteRoots
    $trackedPaths = Get-VerificationPackagingTrackedPaths -RepositoryRoot $RepositoryRoot
    $declared = @(Get-VerificationPackagingFileRecords -RepositoryRoot $RepositoryRoot `
        -DeclaredWriteRoots $roots -TrackedPaths $trackedPaths -InsideDeclaredRoots $true
    )
    $protected = @(Get-VerificationPackagingFileRecords -RepositoryRoot $RepositoryRoot `
        -DeclaredWriteRoots $roots -TrackedPaths $trackedPaths -InsideDeclaredRoots $false
    )
    return [ordered]@{
        declared_write_roots = $roots
        declared_state_sha256 = Get-VerificationDeterministicHash -Value $declared
        declared_file_count = $declared.Count
        protected_state_sha256 = Get-VerificationDeterministicHash -Value $protected
        protected_file_count = $protected.Count
    }
}

Export-ModuleMember -Function @(
    'Resolve-VerificationPackagingWriteRoots',
    'Get-VerificationPackagingStateSnapshot'
)

# GeoCeDG historical-evidence helpers.
#
# These functions are read-only. They deliberately read committed Git blobs as
# bytes so PowerShell newline conversion cannot alter historical evidence.

$script:GeoCeDGFrozenG8TagObject =
    "fed1bfbeea77a48acce285429b397eda77054df1"
$script:GeoCeDGFrozenG8Commit =
    "e7810171179825a03b22d8c6eba28c672f468281"
$script:GeoCeDGFrozenG8TagName = "geocedg-g8-pass"

function Assert-GeoCeDGGitInvocationEnvironment {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    # These variables can redirect object, ref, index, worktree, ancestry,
    # attribute, pathspec, executable, or remote authority outside the explicit
    # repository/configuration inspected by the lifecycle verifier.  Failing
    # closed is preferable to recording evidence for an injected Git view.
    $unsupported = @(
        'GIT_DIR', 'GIT_WORK_TREE', 'GIT_INDEX_FILE', 'GIT_COMMON_DIR',
        'GIT_OBJECT_DIRECTORY', 'GIT_ALTERNATE_OBJECT_DIRECTORIES',
        'GIT_QUARANTINE_PATH', 'GIT_SHALLOW_FILE', 'GIT_REPLACE_REF_BASE',
        'GIT_NAMESPACE', 'GIT_CONFIG', 'GIT_CONFIG_PARAMETERS',
        'GIT_CONFIG_SYSTEM', 'GIT_CONFIG_GLOBAL', 'GIT_CONFIG_NOSYSTEM',
        'GIT_EXEC_PATH', 'GIT_EXTERNAL_DIFF', 'GIT_DIFF_OPTS',
        'GIT_GLOB_PATHSPECS', 'GIT_NOGLOB_PATHSPECS',
        'GIT_LITERAL_PATHSPECS', 'GIT_ICASE_PATHSPECS', 'GIT_ATTR_SOURCE',
        'GIT_CEILING_DIRECTORIES', 'GIT_DISCOVERY_ACROSS_FILESYSTEM',
        'GIT_SSH', 'GIT_SSH_COMMAND', 'GIT_PROXY_COMMAND'
    )
    foreach ($name in $unsupported) {
        if ($null -ne [Environment]::GetEnvironmentVariable(
                $name, [EnvironmentVariableTarget]::Process)) {
            throw "Unsupported Git authority environment override: $name"
        }
    }

    # Codex may inject safe.directory entries for the mounted checkout.  They
    # affect only Git's ownership trust gate and are therefore the sole allowed
    # GIT_CONFIG_COUNT payload.  All other injected config (for example
    # url.*.insteadOf, core.hooksPath, or protocol helpers) is rejected.
    $environment = [Environment]::GetEnvironmentVariables(
        [EnvironmentVariableTarget]::Process)
    $indexedNames = @($environment.Keys | ForEach-Object { [string]$_ } |
        Where-Object { $_ -cmatch '^GIT_CONFIG_(?:KEY|VALUE)_[0-9]+$' })
    $countText = [Environment]::GetEnvironmentVariable(
        'GIT_CONFIG_COUNT', [EnvironmentVariableTarget]::Process)
    if ($null -eq $countText) {
        if ($indexedNames.Count -ne 0) {
            throw 'Orphan GIT_CONFIG_KEY/VALUE environment entry is unsupported.'
        }
        return
    }

    $count = 0
    if (-not [int]::TryParse($countText,
            [Globalization.NumberStyles]::None,
            [Globalization.CultureInfo]::InvariantCulture, [ref]$count) -or
            $count -lt 0 -or $count -gt 64) {
        throw 'GIT_CONFIG_COUNT must be a bounded non-negative integer.'
    }
    $expectedNames = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    for ($index = 0; $index -lt $count; $index++) {
        $keyName = "GIT_CONFIG_KEY_$index"
        $valueName = "GIT_CONFIG_VALUE_$index"
        [void]$expectedNames.Add($keyName)
        [void]$expectedNames.Add($valueName)
        $key = [Environment]::GetEnvironmentVariable(
            $keyName, [EnvironmentVariableTarget]::Process)
        $value = [Environment]::GetEnvironmentVariable(
            $valueName, [EnvironmentVariableTarget]::Process)
        if ($key -cne 'safe.directory' -or
                [string]::IsNullOrWhiteSpace($value) -or
                $value -match '[\x00-\x1f\x7f]') {
            throw "Unsupported injected Git config at index $index."
        }
        $pathValue = $value
        if ($pathValue.EndsWith('/*', [StringComparison]::Ordinal) -or
                $pathValue.EndsWith('\*', [StringComparison]::Ordinal)) {
            $pathValue = $pathValue.Substring(0, $pathValue.Length - 2)
        }
        if (-not [IO.Path]::IsPathRooted($pathValue)) {
            throw "Injected safe.directory must be an absolute path: $value"
        }
    }
    foreach ($name in $indexedNames) {
        if (-not $expectedNames.Contains($name)) {
            throw "Unexpected injected Git config entry: $name"
        }
    }
}

function Invoke-GeoCeDGGitByteCommand {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [switch]$AllowFailure
    )

    Assert-GeoCeDGGitInvocationEnvironment -RepositoryRoot $RepositoryRoot
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.WorkingDirectory = $RepositoryRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.Environment['GIT_OPTIONAL_LOCKS'] = '0'
    $startInfo.Environment['GIT_PAGER'] = 'cat'
    foreach ($argument in @("--no-replace-objects", "--no-optional-locks",
        "--no-pager", "-C", $RepositoryRoot) +
        $Arguments) {
        [void]$startInfo.ArgumentList.Add($argument)
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $output = [IO.MemoryStream]::new()
    try {
        if (-not $process.Start()) {
            throw "Unable to start Git."
        }
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.StandardOutput.BaseStream.CopyTo($output)
        $process.WaitForExit()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        $exitCode = $process.ExitCode
        if ($exitCode -ne 0 -and -not $AllowFailure) {
            throw ("Git {0} failed with exit code {1}: {2}" -f
                ($Arguments -join " "), $exitCode, $stderr.Trim())
        }
        return [pscustomobject]@{
            ExitCode = $exitCode
            Bytes = $output.ToArray()
            StandardError = $stderr
        }
    } finally {
        $output.Dispose()
        $process.Dispose()
    }
}

function ConvertFrom-GeoCeDGStrictUtf8 {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $encoding = [Text.UTF8Encoding]::new($false, $true)
    return $encoding.GetString($Bytes, $offset, $Bytes.Length - $offset)
}

function ConvertTo-GeoCeDGCanonicalLfBytes {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $text = ConvertFrom-GeoCeDGStrictUtf8 -Bytes $Bytes
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return ,([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function Get-GeoCeDGSha256FromBytes {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha256.ComputeHash($Bytes)).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function ConvertTo-GeoCeDGRepositoryPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    $absolute = if ([IO.Path]::IsPathRooted($Path)) {
        [IO.Path]::GetFullPath($Path)
    } else {
        [IO.Path]::GetFullPath((Join-Path $root $Path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)))
    }
    $prefix = $root + [IO.Path]::DirectorySeparatorChar
    if (-not $absolute.StartsWith($prefix,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Evidence path escapes the repository: $Path"
    }
    return ([IO.Path]::GetRelativePath($root, $absolute)).Replace("\", "/")
}

function Get-GeoCeDGFrozenBlobBytes {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $repositoryPath = ConvertTo-GeoCeDGRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $Path
    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("cat-file", "blob", "${Commit}:$repositoryPath")
    return ,$result.Bytes
}

function Get-GeoCeDGFrozenText {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $bytes = Get-GeoCeDGFrozenBlobBytes -RepositoryRoot $RepositoryRoot `
        -Path $Path -Commit $Commit
    return ConvertFrom-GeoCeDGStrictUtf8 -Bytes $bytes
}

function Get-GeoCeDGFrozenJson {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    return (Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
        -Path $Path -Commit $Commit | ConvertFrom-Json -Depth 100)
}

function Get-GeoCeDGFrozenCanonicalTextSha256 {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $bytes = Get-GeoCeDGFrozenBlobBytes -RepositoryRoot $RepositoryRoot `
        -Path $Path -Commit $Commit
    $canonicalBytes = ConvertTo-GeoCeDGCanonicalLfBytes -Bytes $bytes
    return Get-GeoCeDGSha256FromBytes -Bytes $canonicalBytes
}

function Test-GeoCeDGFrozenPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $repositoryPath = ConvertTo-GeoCeDGRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $Path
    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("cat-file", "-e", "${Commit}:$repositoryPath") `
        -AllowFailure
    return $result.ExitCode -eq 0
}

function Assert-GeoCeDGFrozenG8Anchor {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $typeResult = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("cat-file", "-t", $script:GeoCeDGFrozenG8TagObject)
    $type = (ConvertFrom-GeoCeDGStrictUtf8 -Bytes $typeResult.Bytes).Trim()
    if ($type -ne "tag") {
        throw "The frozen G8 object is not an annotated tag object: $type"
    }

    $refResult = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("rev-parse",
            "refs/tags/$($script:GeoCeDGFrozenG8TagName)")
    $tagRef = (ConvertFrom-GeoCeDGStrictUtf8 -Bytes $refResult.Bytes).Trim()
    if ($tagRef -ne $script:GeoCeDGFrozenG8TagObject) {
        throw ("$($script:GeoCeDGFrozenG8TagName) resolves to $tagRef, not " +
            "the approved annotated tag object.")
    }

    $peelResult = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("rev-parse", "$($script:GeoCeDGFrozenG8TagObject)^{}")
    $peeled = (ConvertFrom-GeoCeDGStrictUtf8 -Bytes $peelResult.Bytes).Trim()
    if ($peeled -ne $script:GeoCeDGFrozenG8Commit) {
        throw "Frozen G8 tag peels to $peeled, not the approved commit."
    }

    $ancestor = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("merge-base", "--is-ancestor",
            $script:GeoCeDGFrozenG8Commit, "HEAD") -AllowFailure
    if ($ancestor.ExitCode -ne 0) {
        throw "Current HEAD is not a descendant of the frozen G8 commit."
    }
    return $script:GeoCeDGFrozenG8Commit
}

function Get-GeoCeDGFrozenChangedPaths {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$BaseCommit,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @("diff", "--name-only", "--no-renames", $BaseCommit,
            $Commit, "--")
    $text = ConvertFrom-GeoCeDGStrictUtf8 -Bytes $result.Bytes
    return @($text.Replace("`r`n", "`n").Replace("`r", "`n").Split("`n") |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        Sort-Object -Unique)
}

function Assert-GeoCeDGFrozenHashManifest {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ManifestPath,
        [string]$Commit = $script:GeoCeDGFrozenG8Commit
    )

    $manifestRepositoryPath = ConvertTo-GeoCeDGRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $ManifestPath
    $currentManifestPath = Join-Path $RepositoryRoot `
        $manifestRepositoryPath.Replace("/", [IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $currentManifestPath -PathType Leaf)) {
        throw "Historical evidence manifest is missing: $manifestRepositoryPath"
    }
    $currentManifestCanonicalBytes = ConvertTo-GeoCeDGCanonicalLfBytes `
        -Bytes ([IO.File]::ReadAllBytes($currentManifestPath))
    $frozenManifestBytes = Get-GeoCeDGFrozenBlobBytes `
        -RepositoryRoot $RepositoryRoot -Path $manifestRepositoryPath `
        -Commit $Commit
    $frozenManifestCanonicalBytes = ConvertTo-GeoCeDGCanonicalLfBytes `
        -Bytes $frozenManifestBytes
    $currentManifestHash = Get-GeoCeDGSha256FromBytes `
        -Bytes $currentManifestCanonicalBytes
    $frozenManifestHash = Get-GeoCeDGSha256FromBytes `
        -Bytes $frozenManifestCanonicalBytes
    if ($currentManifestHash -ne $frozenManifestHash) {
        throw ("Checked-out historical manifest differs from frozen G8 " +
            "manifest: $manifestRepositoryPath")
    }
    $manifestText = ConvertFrom-GeoCeDGStrictUtf8 `
        -Bytes $frozenManifestBytes
    $manifestDirectory = [IO.Path]::GetDirectoryName(
        $manifestRepositoryPath.Replace("/", [IO.Path]::DirectorySeparatorChar))
    $validated = 0
    foreach ($line in $manifestText.Replace("`r`n", "`n").Replace(
            "`r", "`n").Split("`n")) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        $parts = $line -split "\s+", 2
        if ($parts.Count -ne 2 -or
                $parts[0] -notmatch "^[0-9a-fA-F]{64}$") {
            throw "Malformed frozen evidence hash line: $line"
        }
        $entry = $parts[1].Trim().Replace("\", "/")
        $target = if ($entry.Contains("/")) {
            $entry
        } elseif ([string]::IsNullOrEmpty($manifestDirectory)) {
            $entry
        } else {
            ($manifestDirectory.Replace("\", "/") + "/" + $entry)
        }
        $actual = Get-GeoCeDGFrozenCanonicalTextSha256 `
            -RepositoryRoot $RepositoryRoot -Path $target -Commit $Commit
        if ($actual -ne $parts[0].ToLowerInvariant()) {
            throw "Frozen evidence hash mismatch: $target"
        }
        $validated++
    }
    if ($validated -eq 0) {
        throw "Frozen evidence manifest contains no entries: $manifestRepositoryPath"
    }
    return $validated
}

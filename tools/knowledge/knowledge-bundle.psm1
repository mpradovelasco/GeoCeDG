Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$script:KnowledgeBundleGeneratorName = "geocedg-knowledge-bundle"
$script:KnowledgeBundleGeneratorVersion = "1.0.0"
$script:KnowledgeBundleSchemaVersion = 1
$script:KnowledgeBundleArchiveTimestamp = [DateTimeOffset]::new(
    1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
$script:KnowledgeBundleGlobRegexCache =
    [Collections.Generic.Dictionary[string, string]]::new(
        [StringComparer]::Ordinal)
$script:KnowledgeBundleCompiledGlobCache =
    [Collections.Generic.Dictionary[string, regex]]::new(
        [StringComparer]::Ordinal)

function Assert-KnowledgeBundleCondition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Get-KnowledgeBundleSha256 {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)

    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha256.ComputeHash($Bytes)).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
}

function ConvertFrom-KnowledgeBundleStrictUtf8 {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)

    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $encoding = [Text.UTF8Encoding]::new($false, $true)
    return $encoding.GetString($Bytes, $offset, $Bytes.Length - $offset)
}

function ConvertTo-KnowledgeBundleCanonicalTextBytes {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)

    $text = ConvertFrom-KnowledgeBundleStrictUtf8 -Bytes $Bytes
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return ,([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function ConvertTo-KnowledgeBundleJsonBytes {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Value)

    $json = $Value | ConvertTo-Json -Depth 100
    $canonical = $json.Replace("`r`n", "`n").Replace("`r", "`n") + "`n"
    return ,([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function Get-KnowledgeBundleOrdinalStrings {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Values,
        [switch]$Unique
    )
    $copy = [string[]]::new(0)
    if ($Unique) {
        $set = [Collections.Generic.HashSet[string]]::new(
            [StringComparer]::Ordinal)
        foreach ($value in $Values) { [void]$set.Add($value) }
        $copy = [string[]]::new($set.Count)
        $set.CopyTo($copy)
    } else {
        $copy = [string[]]::new($Values.Count)
        [Array]::Copy($Values, $copy, $Values.Count)
    }
    [Array]::Sort($copy, [StringComparer]::Ordinal)
    return $copy
}

function Assert-KnowledgeBundleUniqueStrings {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Values,
        [Parameter(Mandatory)] [string]$Description,
        [switch]$RequireNonEmpty
    )
    if ($RequireNonEmpty) {
        Assert-KnowledgeBundleCondition -Condition ($Values.Count -gt 0) `
            -Message "$Description must not be empty."
    }
    $seen = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($value in $Values) {
        Assert-KnowledgeBundleCondition -Condition (
            -not [string]::IsNullOrWhiteSpace($value) -and $seen.Add($value)) `
            -Message "$Description contains an empty or duplicate value."
    }
}

function Invoke-KnowledgeBundleGitBytes {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.WorkingDirectory = $RepositoryRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in @("-C", $RepositoryRoot) + $Arguments) {
        [void]$startInfo.ArgumentList.Add($argument)
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $output = [IO.MemoryStream]::new()
    try {
        Assert-KnowledgeBundleCondition -Condition $process.Start() `
            -Message "Unable to start Git."
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.StandardOutput.BaseStream.CopyTo($output)
        $process.WaitForExit()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -notin $AllowedExitCodes) {
            throw ("git {0} failed with exit code {1}: {2}" -f
                ($Arguments -join " "), $process.ExitCode, $stderr.Trim())
        }
        return [pscustomobject]@{
            ExitCode = $process.ExitCode
            Bytes = $output.ToArray()
            StandardError = $stderr
        }
    } finally {
        $output.Dispose()
        $process.Dispose()
    }
}

function Invoke-KnowledgeBundleGitText {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $result = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments $Arguments -AllowedExitCodes $AllowedExitCodes
    $text = ConvertFrom-KnowledgeBundleStrictUtf8 -Bytes $result.Bytes
    return [pscustomobject]@{
        ExitCode = $result.ExitCode
        Text = $text.Replace("`r`n", "`n").Replace("`r", "`n")
        StandardError = $result.StandardError
    }
}

function Resolve-KnowledgeBundleRepositoryRoot {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    $candidate = [IO.Path]::GetFullPath($Path)
    $result = Invoke-KnowledgeBundleGitText -RepositoryRoot $candidate `
        -Arguments @("rev-parse", "--show-toplevel")
    $root = [IO.Path]::GetFullPath($result.Text.Trim()).TrimEnd(
        [IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    Assert-KnowledgeBundleCondition -Condition (
        Test-Path -LiteralPath (Join-Path $root ".git")) `
        -Message "Repository root has no .git authority: $root"
    return $root
}

function Test-KnowledgeBundleRepositoryPath {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or
            [IO.Path]::IsPathRooted($Path) -or
            $Path.Contains("\") -or
            $Path.Contains(":") -or
            $Path.Contains([char]0) -or
            $Path -match '[\x00-\x1F]' -or
            $Path -match '^[A-Za-z]:' -or
            $Path -match '(?:^|/)\.(?:/|$)' -or
            $Path -match '(?:^|/)\.\.(?:/|$)' -or
            $Path.StartsWith("/")) {
        return $false
    }
    return $true
}

function Test-KnowledgeBundleSafeRemoteUrl {
    param([Parameter(Mandatory)] [string]$Url)
    if ([string]::IsNullOrWhiteSpace($Url) -or
            $Url -match '[\x00-\x1F]' -or
            [IO.Path]::IsPathRooted($Url) -or $Url.Contains("\") -or
            $Url.StartsWith(".") -or $Url -match '(?i)^file:') {
        return $false
    }
    if ($Url -match '^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+:[^\\]+$') {
        return $true
    }
    $uri = $null
    if (-not [Uri]::TryCreate($Url, [UriKind]::Absolute, [ref]$uri) -or
            $uri.Scheme -notin @("https", "http", "ssh", "git") -or
            [string]::IsNullOrWhiteSpace($uri.Host) -or
            -not [string]::IsNullOrEmpty($uri.Query) -or
            -not [string]::IsNullOrEmpty($uri.Fragment) -or
            $uri.UserInfo.Contains(":")) {
        return $false
    }
    if ($uri.Scheme -in @("https", "http") -and
            -not [string]::IsNullOrEmpty($uri.UserInfo)) {
        return $false
    }
    return $true
}

function Assert-KnowledgeBundleNoReparsePoint {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$AbsolutePath
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    $target = [IO.Path]::GetFullPath($AbsolutePath)
    $relative = [IO.Path]::GetRelativePath($root, $target)
    $current = $root
    foreach ($component in $relative.Split(
            [IO.Path]::DirectorySeparatorChar,
            [StringSplitOptions]::RemoveEmptyEntries)) {
        $current = Join-Path $current $component
        if (Test-Path -LiteralPath $current) {
            $item = Get-Item -LiteralPath $current -Force
            Assert-KnowledgeBundleCondition -Condition (
                ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -eq 0) `
                -Message "Repository path contains a reparse point: $current"
        }
    }
}

function ConvertTo-KnowledgeBundleRepositoryPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [switch]$RequireFile
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
    Assert-KnowledgeBundleCondition -Condition (
        $absolute.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Path escapes the repository: $Path"
    if ($RequireFile) {
        Assert-KnowledgeBundleCondition -Condition (
            Test-Path -LiteralPath $absolute -PathType Leaf) `
            -Message "Required repository file is missing: $Path"
    }
    Assert-KnowledgeBundleNoReparsePoint -RepositoryRoot $root `
        -AbsolutePath $absolute
    $relative = [IO.Path]::GetRelativePath($root, $absolute).Replace("\", "/")
    Assert-KnowledgeBundleCondition -Condition (
        Test-KnowledgeBundleRepositoryPath -Path $relative) `
        -Message "Unsafe repository path: $relative"
    return $relative
}

function ConvertTo-KnowledgeBundleGlobRegex {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Pattern)

    Assert-KnowledgeBundleCondition -Condition (
        Test-KnowledgeBundleRepositoryPath -Path $Pattern) `
        -Message "Unsafe bundle glob: $Pattern"
    if ($script:KnowledgeBundleGlobRegexCache.ContainsKey($Pattern)) {
        return $script:KnowledgeBundleGlobRegexCache[$Pattern]
    }
    $builder = [Text.StringBuilder]::new("^")
    $index = 0
    while ($index -lt $Pattern.Length) {
        $character = $Pattern[$index]
        if ($character -eq '*') {
            if ($index + 1 -lt $Pattern.Length -and
                    $Pattern[$index + 1] -eq '*') {
                if ($index + 2 -lt $Pattern.Length -and
                        $Pattern[$index + 2] -eq '/') {
                    [void]$builder.Append("(?:.*/)?")
                    $index += 3
                } else {
                    [void]$builder.Append(".*")
                    $index += 2
                }
            } else {
                [void]$builder.Append("[^/]*")
                $index++
            }
        } elseif ($character -eq '?') {
            [void]$builder.Append("[^/]")
            $index++
        } else {
            [void]$builder.Append([regex]::Escape([string]$character))
            $index++
        }
    }
    [void]$builder.Append('$')
    $result = $builder.ToString()
    $script:KnowledgeBundleGlobRegexCache[$Pattern] = $result
    return $result
}

function Test-KnowledgeBundleGlob {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Pattern
    )

    $regex = ConvertTo-KnowledgeBundleGlobRegex -Pattern $Pattern
    if (-not $script:KnowledgeBundleCompiledGlobCache.ContainsKey($Pattern)) {
        $script:KnowledgeBundleCompiledGlobCache[$Pattern] = [regex]::new(
            $regex, [Text.RegularExpressions.RegexOptions]::CultureInvariant)
    }
    return $script:KnowledgeBundleCompiledGlobCache[$Pattern].IsMatch($Path)
}

function Test-KnowledgeBundleAnyGlob {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string[]]$Patterns
    )

    foreach ($pattern in $Patterns) {
        if (Test-KnowledgeBundleGlob -Path $Path -Pattern $pattern) {
            return $true
        }
    }
    return $false
}

function ConvertFrom-KnowledgeBundleNullRecords {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)

    if ($Bytes.Length -eq 0) {
        return @()
    }
    $text = ConvertFrom-KnowledgeBundleStrictUtf8 -Bytes $Bytes
    return @($text.Split([char]0) | Where-Object {
            -not [string]::IsNullOrEmpty($_) })
}

function Get-KnowledgeBundleIndexEntries {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $result = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("ls-files", "--stage", "-z")
    $entries = [ordered]@{}
    foreach ($record in ConvertFrom-KnowledgeBundleNullRecords -Bytes $result.Bytes) {
        $match = [regex]::Match($record,
            '^(?<mode>[0-9]{6}) (?<object>[0-9a-f]{40}) (?<stage>[0-3])\t(?<path>.+)$')
        Assert-KnowledgeBundleCondition -Condition $match.Success `
            -Message "Malformed Git index record."
        $path = $match.Groups["path"].Value.Replace("\", "/")
        Assert-KnowledgeBundleCondition -Condition (
            Test-KnowledgeBundleRepositoryPath -Path $path) `
            -Message "Unsafe path in Git index: $path"
        Assert-KnowledgeBundleCondition -Condition (
            $match.Groups["stage"].Value -eq "0") `
            -Message "Unmerged Git index entry is not bundle-safe: $path"
        Assert-KnowledgeBundleCondition -Condition (-not $entries.Contains($path)) `
            -Message "Duplicate Git index path: $path"
        $entries[$path] = [pscustomobject]@{
            Path = $path
            Mode = $match.Groups["mode"].Value
            ObjectId = $match.Groups["object"].Value
        }
    }
    return $entries
}

function Get-KnowledgeBundleTreeEntries {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Commit
    )

    $result = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("ls-tree", "-r", "-z", $Commit)
    $entries = [ordered]@{}
    foreach ($record in ConvertFrom-KnowledgeBundleNullRecords -Bytes $result.Bytes) {
        $match = [regex]::Match($record,
            '^(?<mode>[0-9]{6}) (?<type>\S+) (?<object>[0-9a-f]{40})\t(?<path>.+)$')
        Assert-KnowledgeBundleCondition -Condition $match.Success `
            -Message "Malformed Git tree record for $Commit."
        $path = $match.Groups["path"].Value.Replace("\", "/")
        Assert-KnowledgeBundleCondition -Condition (
            Test-KnowledgeBundleRepositoryPath -Path $path) `
            -Message "Unsafe path in Git tree: $path"
        $entries[$path] = [pscustomobject]@{
            Path = $path
            Mode = $match.Groups["mode"].Value
            ObjectId = $match.Groups["object"].Value
            Type = $match.Groups["type"].Value
        }
    }
    return $entries
}

function Get-KnowledgeBundleDirtyState {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $status = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("status", "--porcelain=v1", "-z", "--untracked-files=all")
    $isDirty = $status.Bytes.Length -gt 0
    $staged = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("diff", "--cached", "--binary", "--full-index",
            "--no-ext-diff", "--no-textconv", "--no-color", "--no-renames",
            "--diff-algorithm=myers", "--no-indent-heuristic",
            "--src-prefix=a/", "--dst-prefix=b/", "--")
    $unstaged = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("diff", "--binary", "--full-index", "--no-ext-diff",
            "--no-textconv", "--no-color", "--no-renames",
            "--diff-algorithm=myers", "--no-indent-heuristic",
            "--src-prefix=a/", "--dst-prefix=b/", "--")
    $untrackedResult = Invoke-KnowledgeBundleGitBytes `
        -RepositoryRoot $RepositoryRoot `
        -Arguments @("ls-files", "--others", "--exclude-standard", "-z")
    $untracked = [Collections.Generic.List[object]]::new()
    $untrackedPaths = [string[]]@(ConvertFrom-KnowledgeBundleNullRecords `
        -Bytes $untrackedResult.Bytes)
    foreach ($path in Get-KnowledgeBundleOrdinalStrings `
            -Values $untrackedPaths) {
        Assert-KnowledgeBundleCondition -Condition (
            Test-KnowledgeBundleRepositoryPath -Path $path) `
            -Message "Unsafe untracked path: $path"
        $absolute = Join-Path $RepositoryRoot $path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        if (Test-Path -LiteralPath $absolute -PathType Leaf) {
            $untrackedItem = Get-Item -LiteralPath $absolute -Force
            Assert-KnowledgeBundleCondition -Condition (
                ($untrackedItem.Attributes -band
                    [IO.FileAttributes]::ReparsePoint) -eq 0) `
                -Message "Untracked reparse point is forbidden: $path"
            $raw = [IO.File]::ReadAllBytes($absolute)
            $untracked.Add([ordered]@{
                    path = $path
                    raw_sha256 = Get-KnowledgeBundleSha256 -Bytes $raw
                    bytes = $raw.Length
                })
        } else {
            throw "Untracked path is not a regular readable file: $path"
        }
    }
    $descriptor = [ordered]@{
        warning = "NON_RELEASE_EVIDENCE"
        staged_diff_sha256 = Get-KnowledgeBundleSha256 -Bytes $staged.Bytes
        unstaged_diff_sha256 = Get-KnowledgeBundleSha256 -Bytes $unstaged.Bytes
        untracked = @($untracked)
    }
    $descriptorBytes = ConvertTo-KnowledgeBundleJsonBytes -Value $descriptor
    return [pscustomobject]@{
        IsDirty = $isDirty
        Descriptor = $descriptor
        DescriptorSha256 = Get-KnowledgeBundleSha256 -Bytes $descriptorBytes
        StagedBytes = $staged.Bytes
        UnstagedBytes = $unstaged.Bytes
        Untracked = @($untracked)
        StatusSha256 = Get-KnowledgeBundleSha256 -Bytes $status.Bytes
    }
}

function Get-KnowledgeBundleModifiedPaths {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $result = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("diff", "--name-only", "--no-renames", "-z", "HEAD", "--")
    $paths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($path in ConvertFrom-KnowledgeBundleNullRecords -Bytes $result.Bytes) {
        [void]$paths.Add($path.Replace("\", "/"))
    }
    return ,$paths
}

function Get-KnowledgeBundleProfileConfiguration {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ProfilesPath,
        [Parameter(Mandatory)] [string]$ProfileId,
        [int]$MaximumFiles,
        [long]$MaximumBytes,
        [long]$MaximumTokens,
        [long]$MaximumChunkTokens
    )

    $relativeProfiles = ConvertTo-KnowledgeBundleRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $ProfilesPath -RequireFile
    $absoluteProfiles = Join-Path $RepositoryRoot $relativeProfiles.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    try {
        $catalog = Get-Content -Raw -LiteralPath $absoluteProfiles |
            ConvertFrom-Json -Depth 100
    } catch {
        throw "Invalid knowledge-bundle profiles: $($_.Exception.Message)"
    }
    Assert-KnowledgeBundleCondition -Condition (
        $catalog.schema_version -eq 1 -and
        $catalog.status -eq "NORMATIVE_AUTHOR_APPROVED") `
        -Message "Knowledge-bundle profile authority is not normative version 1."
    foreach ($property in @(
            "manifest_schema", "implementation_status", "profiles",
            "default_exclusions", "default_budgets")) {
        Assert-KnowledgeBundleCondition -Condition (
            $null -ne $catalog.PSObject.Properties[$property]) `
            -Message "Knowledge-bundle catalog property is missing: $property"
    }
    Assert-KnowledgeBundleCondition -Condition (
        $catalog.manifest_schema -is [string] -and
        $catalog.implementation_status -is [string] -and
        -not [string]::IsNullOrWhiteSpace($catalog.implementation_status) -and
        (Test-KnowledgeBundleRepositoryPath `
            -Path ([string]$catalog.manifest_schema))) `
        -Message "Knowledge-bundle catalog schema path is unsafe."
    Assert-KnowledgeBundleCondition -Condition (
        $catalog.profiles -is [array] -and $catalog.profiles.Count -gt 0 -and
        $catalog.default_exclusions -is [array]) `
        -Message "Knowledge-bundle catalog collections are invalid."
    $allowedOwnership = @(
        "GEOCEDG_NATIVE", "UPSTREAM_MODIFIED",
        "UPSTREAM_UNCHANGED_REFERENCE")
    $profileIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($candidate in @($catalog.profiles)) {
        foreach ($property in @(
                "id", "kind", "description", "ownership", "include",
                "include_related_tests", "include_unified_diffs",
                "reading_order")) {
            Assert-KnowledgeBundleCondition -Condition (
                $null -ne $candidate.PSObject.Properties[$property]) `
                -Message "Bundle profile property is missing: $property"
        }
        $candidateId = [string]$candidate.id
        Assert-KnowledgeBundleCondition -Condition (
            $candidate.id -is [string] -and
            $candidate.kind -is [string] -and
            $candidate.description -is [string] -and
            $candidateId -match '^[a-z0-9]+(?:[.-][a-z0-9]+)*$' -and
            $profileIds.Add($candidateId)) `
            -Message "Bundle profile ID is invalid or duplicated: $candidateId"
        Assert-KnowledgeBundleCondition -Condition (
            $candidate.kind -in @("source", "knowledge", "thematic") -and
            -not [string]::IsNullOrWhiteSpace([string]$candidate.description) -and
            $candidate.ownership -is [array] -and
            $candidate.include -is [array] -and
            $candidate.reading_order -is [array] -and
            @($candidate.ownership | Where-Object {
                    $_ -isnot [string] }).Count -eq 0 -and
            @($candidate.include | Where-Object {
                    $_ -isnot [string] }).Count -eq 0 -and
            @($candidate.reading_order | Where-Object {
                    $_ -isnot [string] }).Count -eq 0 -and
            $candidate.include_related_tests -is [bool] -and
            $candidate.include_unified_diffs -is [bool]) `
            -Message "Bundle profile metadata is invalid: $candidateId"
        $candidateIncludes = [string[]]@($candidate.include | ForEach-Object {
                [string]$_ })
        $candidateOwnership = [string[]]@($candidate.ownership |
            ForEach-Object { [string]$_ })
        $candidateReading = [string[]]@($candidate.reading_order |
            ForEach-Object { [string]$_ })
        Assert-KnowledgeBundleUniqueStrings -Values $candidateIncludes `
            -Description "$candidateId include rules" -RequireNonEmpty
        Assert-KnowledgeBundleUniqueStrings -Values $candidateOwnership `
            -Description "$candidateId ownership rules" -RequireNonEmpty
        Assert-KnowledgeBundleUniqueStrings -Values $candidateReading `
            -Description "$candidateId reading order" -RequireNonEmpty
        foreach ($pattern in $candidateIncludes) {
            [void](ConvertTo-KnowledgeBundleGlobRegex -Pattern $pattern)
        }
        foreach ($class in $candidateOwnership) {
            Assert-KnowledgeBundleCondition -Condition (
                $class -in $allowedOwnership) `
                -Message "Profile requests forbidden ownership class: $class"
        }
        if ($null -ne $candidate.PSObject.Properties["themes"]) {
            Assert-KnowledgeBundleCondition -Condition (
                $candidate.themes -is [array] -and
                @($candidate.themes | Where-Object {
                        $_ -isnot [string] }).Count -eq 0) `
                -Message "Bundle profile themes are not an array: $candidateId"
            Assert-KnowledgeBundleUniqueStrings -Values @(
                $candidate.themes | ForEach-Object { [string]$_ }) `
                -Description "$candidateId themes"
        }
    }
    $exclusions = [string[]]@($catalog.default_exclusions |
        ForEach-Object { [string]$_ })
    Assert-KnowledgeBundleUniqueStrings -Values $exclusions `
        -Description "default exclusions" -RequireNonEmpty
    Assert-KnowledgeBundleCondition -Condition (
        @($catalog.default_exclusions | Where-Object {
                $_ -isnot [string] }).Count -eq 0) `
        -Message "Default exclusions must contain only strings."
    foreach ($pattern in $exclusions) {
        [void](ConvertTo-KnowledgeBundleGlobRegex -Pattern $pattern)
    }
    foreach ($budgetName in @(
            "maximum_files", "maximum_bytes", "maximum_tokens",
            "maximum_chunk_tokens")) {
        Assert-KnowledgeBundleCondition -Condition (
            $null -ne $catalog.default_budgets.PSObject.Properties[$budgetName] -and
            $catalog.default_budgets.$budgetName -is [long] -and
            [long]$catalog.default_budgets.$budgetName -gt 0) `
            -Message "Default bundle budget is missing/invalid: $budgetName"
    }
    $profiles = @($catalog.profiles | Where-Object { $_.id -eq $ProfileId })
    Assert-KnowledgeBundleCondition -Condition ($profiles.Count -eq 1) `
        -Message "Expected exactly one bundle profile '$ProfileId'."
    $profile = $profiles[0]
    Assert-KnowledgeBundleCondition -Condition (
        $profile.kind -in @("source", "knowledge", "thematic")) `
        -Message "Unsupported profile kind: $($profile.kind)"
    $include = @($profile.include | ForEach-Object { [string]$_ })
    $ownership = @($profile.ownership | ForEach-Object { [string]$_ })
    $readingOrder = @($profile.reading_order | ForEach-Object { [string]$_ })
    $budgets = [ordered]@{
        maximum_files = if ($MaximumFiles -gt 0) {
            $MaximumFiles
        } else {
            [int]$catalog.default_budgets.maximum_files
        }
        maximum_bytes = if ($MaximumBytes -gt 0) {
            $MaximumBytes
        } else {
            [long]$catalog.default_budgets.maximum_bytes
        }
        maximum_tokens = if ($MaximumTokens -gt 0) {
            $MaximumTokens
        } else {
            [long]$catalog.default_budgets.maximum_tokens
        }
        maximum_chunk_tokens = if ($MaximumChunkTokens -gt 0) {
            $MaximumChunkTokens
        } elseif ($null -ne $catalog.default_budgets.PSObject.Properties[
                "maximum_chunk_tokens"]) {
            [long]$catalog.default_budgets.maximum_chunk_tokens
        } else {
            16000L
        }
    }
    foreach ($budget in $budgets.GetEnumerator()) {
        Assert-KnowledgeBundleCondition -Condition ($budget.Value -gt 0) `
            -Message "Bundle budget must be positive: $($budget.Key)"
    }
    Assert-KnowledgeBundleCondition -Condition (
        $budgets.maximum_chunk_tokens -le ([long]::MaxValue / 4L)) `
        -Message "Maximum chunk-token budget is too large."
    $themes = @()
    if ($null -ne $profile.PSObject.Properties["themes"]) {
        $themes = @(Get-KnowledgeBundleOrdinalStrings -Values @(
                $profile.themes | ForEach-Object { [string]$_ }))
    }
    $normalized = [ordered]@{
        profile_id = [string]$profile.id
        profile_kind = [string]$profile.kind
        themes = @($themes)
        ownership = $ownership
        include = $include
        include_related_tests = [bool]$profile.include_related_tests
        include_diffs = [bool]$profile.include_unified_diffs
        reading_order = $readingOrder
        exclusions = $exclusions
        budgets = $budgets
    }
    $canonicalBytes = ConvertTo-KnowledgeBundleJsonBytes -Value $normalized
    return [pscustomobject]@{
        Catalog = $catalog
        Profile = $profile
        Normalized = $normalized
        CanonicalSha256 = Get-KnowledgeBundleSha256 -Bytes $canonicalBytes
        ProfilesPath = $relativeProfiles
    }
}

function Test-KnowledgeBundleRestrictedPath {
    param([Parameter(Mandatory)] [string]$Path)

    $restrictedPatterns = @(
        "docs/references/cedg/**/*.pdf",
        "docs/references/cedg/models/g9p/**/*.ggb",
        "docs/references/cedg/models/g9p/**/*.png",
        "models/legacy/**/original/**",
        "**/local.properties",
        "**/*secret*",
        "**/node_modules/**",
        "**/vendor/**",
        "**/third_party/**"
    )
    if (Test-KnowledgeBundleAnyGlob -Path $Path `
            -Patterns $restrictedPatterns) {
        return $true
    }
    $extension = [IO.Path]::GetExtension($Path).ToLowerInvariant()
    return $extension -in @(
        ".pdf", ".ggb", ".ggt", ".png", ".jpg", ".jpeg", ".gif",
        ".svg", ".webp", ".ico", ".icns", ".ttf", ".otf", ".woff",
        ".woff2", ".jar", ".class", ".zip", ".7z", ".msi", ".exe",
        ".dll", ".so", ".dylib")
}

function Test-KnowledgeBundleGeneratedPath {
    param([Parameter(Mandatory)] [string]$Path)

    return Test-KnowledgeBundleAnyGlob -Path $Path -Patterns @(
        "**/build/**", ".gradle/**", ".kotlin/**", "artifacts/**",
        "packaging/output/**", "**/*.log", "**/*.tmp")
}

function Test-KnowledgeBundleNativePath {
    param([Parameter(Mandatory)] [string]$Path)

    if ($Path -eq "docs/upstream/GEOGEBRA_README.md") {
        return $false
    }
    $nativePatterns = @(
        "AGENTS.md", "FIRST_AGENT_TASK.md", "README.md", "UPSTREAM.md",
        "NOTICE.md", "THIRD_PARTY.md", "LICENSE", "LICENSES/**",
        ".github/**", "ai-shell/**", "apps/geocedg/**", "geocedg/**",
        "docs/**", "tools/**", "packaging/**", "models/**",
        "benchmarks/**", ".gitattributes", ".gitignore",
        "source/**/org/geocedg/**"
    )
    return Test-KnowledgeBundleAnyGlob -Path $Path -Patterns $nativePatterns
}

function Get-KnowledgeBundleOwnershipContext {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$BaselinePath,
        [Parameter(Mandatory)] [string]$ModifiedInventoryPath,
        [Parameter(Mandatory)] [object]$IndexEntries
    )

    $baselineRelative = ConvertTo-KnowledgeBundleRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $BaselinePath -RequireFile
    $baselineAbsolute = Join-Path $RepositoryRoot $baselineRelative.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    $baseline = ([IO.File]::ReadAllText($baselineAbsolute)).Trim()
    Assert-KnowledgeBundleCondition -Condition (
        $baseline -match '^[0-9a-f]{40}$') `
        -Message "Pinned baseline commit is invalid: $baseline"
    [void](Invoke-KnowledgeBundleGitText -RepositoryRoot $RepositoryRoot `
        -Arguments @("cat-file", "-e", "${baseline}^{commit}"))
    $baselineEntries = Get-KnowledgeBundleTreeEntries `
        -RepositoryRoot $RepositoryRoot -Commit $baseline

    $inventoryRelative = ConvertTo-KnowledgeBundleRepositoryPath `
        -RepositoryRoot $RepositoryRoot -Path $ModifiedInventoryPath -RequireFile
    $inventoryAbsolute = Join-Path $RepositoryRoot $inventoryRelative.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    $inventory = Get-Content -Raw -LiteralPath $inventoryAbsolute |
        ConvertFrom-Json -Depth 100
    Assert-KnowledgeBundleCondition -Condition (
        $inventory.schema_version -eq 1 -and
        $inventory.baseline_sha -eq $baseline) `
        -Message "Modified-file inventory is not pinned to the baseline."
    $inventoryMap = [ordered]@{}
    foreach ($record in @($inventory.modifications)) {
        $path = ([string]$record.path).Replace("\", "/")
        Assert-KnowledgeBundleCondition -Condition (
            Test-KnowledgeBundleRepositoryPath -Path $path) `
            -Message "Unsafe modified-file inventory path: $path"
        Assert-KnowledgeBundleCondition -Condition (
            -not $inventoryMap.Contains($path)) `
            -Message "Duplicate modified-file inventory path: $path"
        Assert-KnowledgeBundleCondition -Condition (
            $IndexEntries.Contains($path)) `
            -Message "Inventory path is missing from the Git index: $path"
        $baselineContains = $baselineEntries.Contains($path)
        $isChanged = -not $baselineContains -or
            $baselineEntries[$path].ObjectId -ne $IndexEntries[$path].ObjectId
        Assert-KnowledgeBundleCondition -Condition $isChanged `
            -Message "Inventory/Git disagreement; path is unchanged: $path"
        if ($record.change -eq "added") {
            Assert-KnowledgeBundleCondition -Condition (-not $baselineContains) `
                -Message "Inventory says added but baseline contains: $path"
        } elseif ($record.change -eq "modified") {
            Assert-KnowledgeBundleCondition -Condition $baselineContains `
                -Message "Inventory says modified but baseline lacks: $path"
        } else {
            throw "Unsupported inventory change '$($record.change)' for $path"
        }
        $inventoryMap[$path] = $record
    }
    return [pscustomobject]@{
        Baseline = $baseline
        BaselineEntries = $baselineEntries
        Inventory = $inventory
        InventoryMap = $inventoryMap
        BaselinePath = $baselineRelative
        InventoryPath = $inventoryRelative
    }
}

function Get-KnowledgeBundleOwnership {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$IndexEntry,
        [Parameter(Mandatory)] [object]$OwnershipContext,
        [Parameter(Mandatory)] [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$ModifiedPaths
    )

    if (Test-KnowledgeBundleRestrictedPath -Path $Path) {
        return "THIRD_PARTY_OR_RESTRICTED"
    }
    if (Test-KnowledgeBundleGeneratedPath -Path $Path) {
        return "GENERATED"
    }
    if ($Path -eq "docs/upstream/GEOGEBRA_README.md") {
        return "UPSTREAM_UNCHANGED_REFERENCE"
    }
    if (Test-KnowledgeBundleNativePath -Path $Path) {
        return "GEOCEDG_NATIVE"
    }
    $baselineContains = $OwnershipContext.BaselineEntries.Contains($Path)
    if ($OwnershipContext.InventoryMap.Contains($Path)) {
        return "UPSTREAM_MODIFIED"
    }
    if ($baselineContains) {
        if ($ModifiedPaths.Contains($Path)) {
            throw "Modified upstream path is absent from inventory: $Path"
        }
        if ($OwnershipContext.BaselineEntries[$Path].ObjectId -eq
                $IndexEntry.ObjectId) {
            return "UPSTREAM_UNCHANGED_REFERENCE"
        }
        throw "Modified upstream path is absent from inventory: $Path"
    }
    throw "Ownership is ambiguous for selected path: $Path"
}

function Test-KnowledgeBundleTextPath {
    param([Parameter(Mandatory)] [string]$Path)

    $name = [IO.Path]::GetFileName($Path).ToLowerInvariant()
    if ($name -in @("license", ".gitignore", ".gitattributes")) {
        return $true
    }
    $extension = [IO.Path]::GetExtension($Path).ToLowerInvariant()
    return $extension -in @(
        ".md", ".txt", ".json", ".yml", ".yaml", ".ps1", ".psm1",
        ".java", ".kt", ".kts", ".gradle", ".xml", ".properties",
        ".js", ".ts", ".css", ".scss", ".html", ".csv", ".tsv",
        ".ggs", ".toml", ".ini", ".cfg", ".bat", ".cmd", ".sh",
        ".py", ".sha256")
}

function Get-KnowledgeBundleLanguage {
    param([Parameter(Mandatory)] [string]$Path)

    $extension = [IO.Path]::GetExtension($Path).ToLowerInvariant()
    $languages = @{
        ".md" = "Markdown"; ".txt" = "Text"; ".json" = "JSON"
        ".yml" = "YAML"; ".yaml" = "YAML"; ".ps1" = "PowerShell"
        ".psm1" = "PowerShell"; ".java" = "Java"; ".kt" = "Kotlin"
        ".kts" = "Kotlin"; ".gradle" = "Gradle"; ".xml" = "XML"
        ".properties" = "Properties"; ".js" = "JavaScript"
        ".ts" = "TypeScript"; ".css" = "CSS"; ".scss" = "SCSS"
        ".html" = "HTML"; ".csv" = "CSV"; ".tsv" = "TSV"
        ".ggs" = "GeoGebraScript"; ".toml" = "TOML"
        ".py" = "Python"
        ".sha256" = "SHA-256 manifest"
    }
    if ($languages.ContainsKey($extension)) {
        return $languages[$extension]
    }
    return "Text"
}

function Get-KnowledgeBundleSourceBytes {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [object]$IndexEntry,
        [Parameter(Mandatory)] [bool]$Dirty,
        [Parameter(Mandatory)] [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$ModifiedPaths
    )

    if ($IndexEntry.Mode -eq "120000") {
        throw "Selected symbolic link is forbidden: $($IndexEntry.Path)"
    }
    if ($IndexEntry.Mode -eq "160000") {
        throw "Selected Git submodule is forbidden: $($IndexEntry.Path)"
    }
    Assert-KnowledgeBundleCondition -Condition (
        $IndexEntry.Mode -in @("100644", "100755")) `
        -Message "Unsupported Git mode $($IndexEntry.Mode): $($IndexEntry.Path)"
    if ($Dirty -and $ModifiedPaths.Contains($IndexEntry.Path)) {
        $absolute = Join-Path $RepositoryRoot $IndexEntry.Path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        Assert-KnowledgeBundleCondition -Condition (
            Test-Path -LiteralPath $absolute -PathType Leaf) `
            -Message "Dirty selected source is missing: $($IndexEntry.Path)"
        $dirtyItem = Get-Item -LiteralPath $absolute -Force
        Assert-KnowledgeBundleCondition -Condition (
            ($dirtyItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -eq 0) `
            -Message "Dirty selected source is a reparse point: $($IndexEntry.Path)"
        return ,([IO.File]::ReadAllBytes($absolute))
    }
    $result = Invoke-KnowledgeBundleGitBytes -RepositoryRoot $RepositoryRoot `
        -Arguments @("cat-file", "blob", $IndexEntry.ObjectId)
    return ,$result.Bytes
}

function Get-KnowledgeBundleLicenseProvenance {
    param([Parameter(Mandatory)] [string]$OwnershipClass)

    switch ($OwnershipClass) {
        "GEOCEDG_NATIVE" {
            return "GeoCeDG-authored; project-wide license not approved; internal evaluation only."
        }
        "UPSTREAM_MODIFIED" {
            return "Pinned upstream source with GeoCeDG modifications; preserve file notices and review component terms."
        }
        "UPSTREAM_UNCHANGED_REFERENCE" {
            return "Pinned upstream reference explicitly selected by profile; preserve notices and component terms."
        }
        default {
            throw "No inclusion provenance for ownership class: $OwnershipClass"
        }
    }
}

function Get-KnowledgeBundleReadingRank {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string[]]$ReadingOrder
    )

    $candidates = [Collections.Generic.List[string]]::new()
    if ($Path -eq "AGENTS.md") {
        $candidates.Add("authority"); $candidates.Add("governance")
    }
    if ($Path -match '(^|/)src/test/' -or $Path -match '(^|/)tests?(/|\.)') {
        $candidates.Add("tests")
    }
    if ($Path -in @("docs/upstream/modified-files.yml",
            "docs/upstream/BASELINE_COMMIT.txt")) {
        $candidates.Add("ownership")
    }
    if ($Path -match '^docs/upstream/') {
        $candidates.Add("baseline"); $candidates.Add("provenance")
    }
    if ($Path -match '(^|/)(knowledge-bundle-profiles|stable|experimental)\.' -or
            $Path -match '^apps/geocedg/') {
        $candidates.Add("profiles"); $candidates.Add("profile")
    }
    if ($Path -eq "geocedg/specs/operations/documentation-maintenance.md") {
        $candidates.Add("maintenance-contract")
    }
    if ($Path -match '^geocedg/specs/operations/knowledge-bundle') {
        $candidates.Add("bundle-contract")
    }
    if ($Path -match '^geocedg/specs/ui/') { $candidates.Add("ui-contracts") }
    if ($Path -match '^geocedg/specs/export/') {
        $candidates.Add("export-contracts")
    }
    if ($Path -match 'locus-v2-semantics') { $candidates.Add("semantics") }
    if ($Path -match 'metric') { $candidates.Add("metrics") }
    if ($Path -match 'intersection') { $candidates.Add("intersections") }
    if ($Path -match '^geocedg/specs/') { $candidates.Add("specifications") }
    if ($Path -match '^docs/adr/') {
        $candidates.Add("decision"); $candidates.Add("decisions")
    }
    if ($Path -match 'g9_spatial_semantic_model') {
        $candidates.Add("semantic-model")
    }
    if ($Path -match 'persistence') { $candidates.Add("persistence") }
    if ($Path -match '^docs/architecture/') { $candidates.Add("architecture") }
    if ($Path -match '^docs/roadmap/') { $candidates.Add("roadmap") }
    if ($Path -match '^docs/(developer|user)/') { $candidates.Add("guides") }
    if ($Path -match '^\.github/prompts/') {
        $candidates.Add("prompt"); $candidates.Add("prompts")
    }
    if ($Path -eq "tools/agent/evidence-integrity.ps1") {
        $candidates.Add("evidence-integrity")
    }
    if ($Path -match '^tools/agent/') {
        $candidates.Add("verifiers"); $candidates.Add("verification")
    }
    if ($Path -match '^source/' -or $Path -match '^apps/') {
        $candidates.Add("productive-source"); $candidates.Add("implementation")
    }
    if ($Path -match '^tools/knowledge/') { $candidates.Add("implementation") }
    if ($Path -match 'validation') {
        $candidates.Add("evidence"); $candidates.Add("validation-design")
        $candidates.Add("validation")
    }
    if ($Path -match 'catalog') { $candidates.Add("scientific-index") }
    if ($Path -match 'NOTICE|THIRD_PARTY|licens') {
        $candidates.Add("licensing"); $candidates.Add("provenance")
    }
    $candidates.Add("implementation")
    foreach ($category in $candidates) {
        $index = [Array]::IndexOf($ReadingOrder, $category)
        if ($index -ge 0) {
            return $index
        }
    }
    return $ReadingOrder.Count
}

function Get-KnowledgeBundleLineCount {
    param([Parameter(Mandatory)] [string]$Text)

    if ($Text.Length -eq 0) {
        return 1
    }
    $count = ([regex]::Matches($Text, "`n")).Count
    if ($Text.EndsWith("`n")) {
        return [Math]::Max(1, $count)
    }
    return $count + 1
}

function Get-KnowledgeBundleTextSlice {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [int]$Start,
        [Parameter(Mandatory)] [int]$End
    )

    $lines = $Text.Split("`n")
    $lineCount = Get-KnowledgeBundleLineCount -Text $Text
    Assert-KnowledgeBundleCondition -Condition (
        $Start -ge 1 -and $End -ge $Start -and $End -le $lineCount) `
        -Message "Invalid source line slice: $Start-$End/$lineCount"
    if ($Text.Length -eq 0) {
        return ""
    }
    $selected = @($lines[($Start - 1)..($End - 1)])
    $slice = $selected -join "`n"
    if ($End -lt $lineCount -or ($End -eq $lineCount -and
            $Text.EndsWith("`n"))) {
        $slice += "`n"
    }
    return $slice
}

function Get-KnowledgeBundleSemanticUnits {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [string]$Path
    )

    $lineCount = Get-KnowledgeBundleLineCount -Text $Text
    if ($lineCount -eq 1) {
        return ,([pscustomobject]@{ Start = 1; End = 1 })
    }
    $lines = $Text.Split("`n")
    $starts = [Collections.Generic.List[int]]::new()
    $starts.Add(1)
    for ($line = 2; $line -le $lineCount; $line++) {
        $value = $lines[$line - 1]
        $boundary = if ($Path.EndsWith(".md")) {
            $value -match '^#{1,6}\s+\S'
        } else {
            $value -match '^(?:function\s+|(?:public|protected|private|internal|abstract|final|sealed|static)\s+(?:class|interface|enum|record|struct)\s+|(?:class|interface|enum|record|struct|namespace|module|def|async\s+def)\s+)'
        }
        if ($boundary) {
            $starts.Add($line)
        }
    }
    $units = [Collections.Generic.List[object]]::new()
    for ($index = 0; $index -lt $starts.Count; $index++) {
        $end = if ($index + 1 -lt $starts.Count) {
            $starts[$index + 1] - 1
        } else {
            $lineCount
        }
        $units.Add([pscustomobject]@{ Start = $starts[$index]; End = $end })
    }
    return @($units)
}

function Get-KnowledgeBundleChunkRanges {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [long]$MaximumChunkTokens
    )

    $maximumBytes = [Math]::Max(1L, $MaximumChunkTokens * 4L)
    $allBytes = [Text.UTF8Encoding]::new($false).GetBytes($Text)
    $lineCount = Get-KnowledgeBundleLineCount -Text $Text
    if ($allBytes.Length -le $maximumBytes) {
        return ,([pscustomobject]@{ Start = 1; End = $lineCount })
    }

    $ranges = [Collections.Generic.List[object]]::new()
    $pendingStart = 0
    $pendingEnd = 0
    foreach ($unit in Get-KnowledgeBundleSemanticUnits -Text $Text -Path $Path) {
        $unitText = Get-KnowledgeBundleTextSlice -Text $Text `
            -Start $unit.Start -End $unit.End
        $unitBytes = [Text.UTF8Encoding]::new($false).GetByteCount($unitText)
        if ($unitBytes -gt $maximumBytes) {
            if ($pendingStart -gt 0) {
                $ranges.Add([pscustomobject]@{
                        Start = $pendingStart; End = $pendingEnd })
                $pendingStart = 0
            }
            $partStart = $unit.Start
            $partBytes = 0L
            for ($line = $unit.Start; $line -le $unit.End; $line++) {
                $lineText = Get-KnowledgeBundleTextSlice -Text $Text `
                    -Start $line -End $line
                $lineBytes = [Text.UTF8Encoding]::new($false).GetByteCount($lineText)
                Assert-KnowledgeBundleCondition -Condition (
                    $lineBytes -le $maximumBytes) `
                    -Message ("Chunk budget cannot be satisfied without " +
                        "splitting source line ${line}: $Path")
                if ($line -gt $partStart -and
                        $partBytes + $lineBytes -gt $maximumBytes) {
                    $ranges.Add([pscustomobject]@{
                            Start = $partStart; End = $line - 1 })
                    $partStart = $line
                    $partBytes = 0
                }
                $partBytes += $lineBytes
            }
            $ranges.Add([pscustomobject]@{
                    Start = $partStart; End = $unit.End })
            continue
        }
        if ($pendingStart -eq 0) {
            $pendingStart = $unit.Start
            $pendingEnd = $unit.End
            continue
        }
        $candidateText = Get-KnowledgeBundleTextSlice -Text $Text `
            -Start $pendingStart -End $unit.End
        $candidateBytes = [Text.UTF8Encoding]::new($false).GetByteCount(
            $candidateText)
        if ($candidateBytes -le $maximumBytes) {
            $pendingEnd = $unit.End
        } else {
            $ranges.Add([pscustomobject]@{
                    Start = $pendingStart; End = $pendingEnd })
            $pendingStart = $unit.Start
            $pendingEnd = $unit.End
        }
    }
    if ($pendingStart -gt 0) {
        $ranges.Add([pscustomobject]@{
                Start = $pendingStart; End = $pendingEnd })
    }
    return @($ranges)
}

function Get-KnowledgeBundleSafeChunkName {
    param([Parameter(Mandatory)] [string]$Path)

    $name = [IO.Path]::GetFileName($Path)
    $safe = [regex]::Replace($name.ToLowerInvariant(), '[^a-z0-9.-]+', '-')
    if ($safe.Length -gt 60) {
        $safe = $safe.Substring(0, 60)
    }
    return $safe.Trim('-')
}

function New-KnowledgeBundleChunkRecords {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$OwnershipClass,
        [Parameter(Mandatory)] [string]$RawSha256,
        [Parameter(Mandatory)] [string]$CanonicalSha256,
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [long]$MaximumChunkTokens,
        [Parameter(Mandatory)] [int]$Order
    )

    $ranges = @(Get-KnowledgeBundleChunkRanges -Text $Text -Path $Path `
        -MaximumChunkTokens $MaximumChunkTokens)
    $continuationInput = [Text.UTF8Encoding]::new($false).GetBytes(
        "$Path`n$CanonicalSha256`n")
    $continuationId = "kb-cont-" + (
        Get-KnowledgeBundleSha256 -Bytes $continuationInput)
    $safeName = Get-KnowledgeBundleSafeChunkName -Path $Path
    $records = [Collections.Generic.List[object]]::new()
    for ($index = 0; $index -lt $ranges.Count; $index++) {
        $sequence = $index + 1
        $range = $ranges[$index]
        $body = Get-KnowledgeBundleTextSlice -Text $Text `
            -Start $range.Start -End $range.End
        $header = @(
            "FILE: $Path"
            "OWNERSHIP: $OwnershipClass"
            "SOURCE_RAW_SHA256: $RawSha256"
            "SOURCE_CANONICAL_SHA256: $CanonicalSha256"
            "LINES: $($range.Start)-$($range.End)"
            "CHUNK: $sequence/$($ranges.Count)"
            "CONTINUATION_ID: $continuationId"
            "---"
            ""
        ) -join "`n"
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($header + $body)
        $archivePath = "chunks/{0:D6}-{1:D4}-{2}-{3}.txt" -f
            $Order, $sequence, $safeName, $CanonicalSha256.Substring(0, 12)
        $records.Add([pscustomobject]@{
                Manifest = [ordered]@{
                    archive_path = $archivePath
                    line_range = [ordered]@{
                        start = $range.Start
                        end = $range.End
                    }
                    continuation_id = $continuationId
                    sequence = $sequence
                    total = $ranges.Count
                    complete_source_sha256 = $CanonicalSha256
                    sha256 = Get-KnowledgeBundleSha256 -Bytes $bytes
                    bytes = $bytes.Length
                    estimated_tokens = [Math]::Ceiling($bytes.Length / 4.0)
                }
                Bytes = $bytes
            })
    }
    return @($records)
}

function Write-KnowledgeBundleFile {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$ArchivePath,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes
    )

    Assert-KnowledgeBundleCondition -Condition (
        Test-KnowledgeBundleRepositoryPath -Path $ArchivePath) `
        -Message "Unsafe bundle archive path: $ArchivePath"
    $absolute = [IO.Path]::GetFullPath((Join-Path $Root $ArchivePath.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)))
    $rootPrefix = [IO.Path]::GetFullPath($Root).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-KnowledgeBundleCondition -Condition (
        $absolute.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Bundle output path escapes staging root: $ArchivePath"
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($absolute)) |
        Out-Null
    [IO.File]::WriteAllBytes($absolute, $Bytes)
}

function New-KnowledgeBundleArchive {
    param(
        [Parameter(Mandatory)] [string]$BundleRoot,
        [Parameter(Mandatory)] [string]$ArchivePath
    )

    Add-Type -AssemblyName System.IO.Compression
    $files = [object[]]@(Get-ChildItem -LiteralPath $BundleRoot -Recurse -File |
        Where-Object { $_.FullName -ne $ArchivePath } |
        ForEach-Object {
            [pscustomobject]@{
                Absolute = $_.FullName
                Relative = [IO.Path]::GetRelativePath(
                    $BundleRoot, $_.FullName).Replace("\", "/")
            }
        })
    $fileComparison = [Comparison[object]] {
        param($left, $right)
        return [StringComparer]::Ordinal.Compare(
            $left.Relative, $right.Relative)
    }
    [Array]::Sort($files, $fileComparison)
    $stream = [IO.File]::Open($ArchivePath, [IO.FileMode]::Create,
        [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    $zip = [IO.Compression.ZipArchive]::new(
        $stream, [IO.Compression.ZipArchiveMode]::Create, $false)
    try {
        # ZIP external attributes: regular Unix file, mode 0644 (0100644 << 16).
        $externalAttributes = [int]-2119958528
        foreach ($file in $files) {
            $entry = $zip.CreateEntry($file.Relative,
                [IO.Compression.CompressionLevel]::NoCompression)
            $entry.LastWriteTime = $script:KnowledgeBundleArchiveTimestamp
            $entry.ExternalAttributes = $externalAttributes
            $entryStream = $entry.Open()
            try {
                $bytes = [IO.File]::ReadAllBytes($file.Absolute)
                $entryStream.Write($bytes, 0, $bytes.Length)
            } finally {
                $entryStream.Dispose()
            }
        }
    } finally {
        $zip.Dispose()
        $stream.Dispose()
    }
}

function Get-KnowledgeBundleVersion {
    return $script:KnowledgeBundleGeneratorVersion
}

function New-GeoCeDGKnowledgeBundle {
    [CmdletBinding()]
    param(
        [string]$RepositoryRoot = (Get-Location).Path,
        [Parameter(Mandatory)] [string]$ProfileId,
        [string]$OutputDirectory = "artifacts/knowledge",
        [string]$ProfilesPath =
            "geocedg/specs/operations/knowledge-bundle-profiles.json",
        [string]$SchemaPath =
            "geocedg/specs/operations/knowledge-bundle.schema.json",
        [string]$BaselinePath = "docs/upstream/BASELINE_COMMIT.txt",
        [string]$ModifiedInventoryPath = "docs/upstream/modified-files.yml",
        [switch]$AllowDirty,
        [int]$MaximumFiles = 0,
        [long]$MaximumBytes = 0,
        [long]$MaximumTokens = 0,
        [long]$MaximumChunkTokens = 0
    )

    Assert-KnowledgeBundleCondition -Condition (
        $MaximumFiles -ge 0 -and $MaximumBytes -ge 0 -and
        $MaximumTokens -ge 0 -and $MaximumChunkTokens -ge 0) `
        -Message "Bundle budget overrides cannot be negative."
    $root = Resolve-KnowledgeBundleRepositoryRoot -Path $RepositoryRoot
    $head = (Invoke-KnowledgeBundleGitText -RepositoryRoot $root `
        -Arguments @("rev-parse", "--verify", "HEAD")).Text.Trim()
    Assert-KnowledgeBundleCondition -Condition ($head -match '^[0-9a-f]{40}$') `
        -Message "Invalid HEAD commit: $head"
    $branchResult = Invoke-KnowledgeBundleGitText -RepositoryRoot $root `
        -Arguments @("symbolic-ref", "--quiet", "--short", "HEAD") `
        -AllowedExitCodes @(0, 1)
    $branch = if ($branchResult.ExitCode -eq 0) {
        $branchResult.Text.Trim()
    } else {
        $null
    }
    $dirtyState = Get-KnowledgeBundleDirtyState -RepositoryRoot $root
    if ($dirtyState.IsDirty -and -not $AllowDirty) {
        throw "Knowledge bundles require a clean tree unless -AllowDirty is explicit."
    }

    $profileConfiguration = Get-KnowledgeBundleProfileConfiguration `
        -RepositoryRoot $root -ProfilesPath $ProfilesPath `
        -ProfileId $ProfileId -MaximumFiles $MaximumFiles `
        -MaximumBytes $MaximumBytes -MaximumTokens $MaximumTokens `
        -MaximumChunkTokens $MaximumChunkTokens
    $schemaRelative = ConvertTo-KnowledgeBundleRepositoryPath `
        -RepositoryRoot $root -Path $SchemaPath -RequireFile
    Assert-KnowledgeBundleCondition -Condition (
        [string]$profileConfiguration.Catalog.manifest_schema -eq
            $schemaRelative) `
        -Message "Profile catalog and requested manifest schema disagree."
    $indexEntries = Get-KnowledgeBundleIndexEntries -RepositoryRoot $root
    $ownershipContext = Get-KnowledgeBundleOwnershipContext `
        -RepositoryRoot $root -BaselinePath $BaselinePath `
        -ModifiedInventoryPath $ModifiedInventoryPath `
        -IndexEntries $indexEntries
    foreach ($authorityPath in @(
            $profileConfiguration.ProfilesPath, $schemaRelative,
            $ownershipContext.BaselinePath, $ownershipContext.InventoryPath)) {
        Assert-KnowledgeBundleCondition -Condition (
            $indexEntries.Contains($authorityPath) -and
            $indexEntries[$authorityPath].Mode -in @("100644", "100755")) `
            -Message "Bundle control input is not a tracked regular file: $authorityPath"
    }
    $modifiedPaths = Get-KnowledgeBundleModifiedPaths -RepositoryRoot $root

    $selected = [Collections.Generic.List[object]]::new()
    foreach ($indexEntry in $indexEntries.Values) {
        $path = $indexEntry.Path
        if (-not (Test-KnowledgeBundleAnyGlob -Path $path `
                -Patterns $profileConfiguration.Normalized.include)) {
            continue
        }
        $ownership = Get-KnowledgeBundleOwnership -Path $path `
            -IndexEntry $indexEntry -OwnershipContext $ownershipContext `
            -ModifiedPaths $modifiedPaths
        if (Test-KnowledgeBundleAnyGlob -Path $path `
                -Patterns $profileConfiguration.Normalized.exclusions) {
            continue
        }
        if ($ownership -in @("GENERATED", "THIRD_PARTY_OR_RESTRICTED")) {
            continue
        }
        if ($ownership -notin $profileConfiguration.Normalized.ownership) {
            continue
        }
        Assert-KnowledgeBundleCondition -Condition (
            Test-KnowledgeBundleTextPath -Path $path) `
            -Message "Selected file has no explicit UTF-8 text policy: $path"
        $rank = Get-KnowledgeBundleReadingRank -Path $path `
            -ReadingOrder $profileConfiguration.Normalized.reading_order
        $selected.Add([pscustomobject]@{
                IndexEntry = $indexEntry
                Ownership = $ownership
                Rank = $rank
            })
    }
    $selectedArray = [object[]]$selected.ToArray()
    $selectionComparison = [Comparison[object]] {
        param($left, $right)
        $rankComparison = $left.Rank.CompareTo($right.Rank)
        if ($rankComparison -ne 0) { return $rankComparison }
        return [StringComparer]::Ordinal.Compare(
            $left.IndexEntry.Path, $right.IndexEntry.Path)
    }
    [Array]::Sort($selectedArray, $selectionComparison)
    $selected = $selectedArray
    Assert-KnowledgeBundleCondition -Condition ($selected.Count -gt 0) `
        -Message "Bundle profile selected no admissible files: $ProfileId"
    Assert-KnowledgeBundleCondition -Condition (
        $selected.Count -le $profileConfiguration.Normalized.budgets.maximum_files) `
        -Message "Bundle file budget exceeded: $($selected.Count)"

    $identity = [ordered]@{
        schema_version = $script:KnowledgeBundleSchemaVersion
        commit = $head
        configuration_sha256 = $profileConfiguration.CanonicalSha256
    }
    $identityBytes = ConvertTo-KnowledgeBundleJsonBytes -Value $identity
    $bundleId = "kb-v1-" + (Get-KnowledgeBundleSha256 -Bytes $identityBytes)

    $outputRelative = ConvertTo-KnowledgeBundleRepositoryPath `
        -RepositoryRoot $root -Path $OutputDirectory
    Assert-KnowledgeBundleCondition -Condition (
        $outputRelative -eq "artifacts/knowledge" -or
        $outputRelative.StartsWith("artifacts/knowledge/")) `
        -Message "Bundle output must remain below artifacts/knowledge."
    $outputAbsolute = Join-Path $root $outputRelative.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    Assert-KnowledgeBundleNoReparsePoint -RepositoryRoot $root `
        -AbsolutePath $outputAbsolute
    [IO.Directory]::CreateDirectory($outputAbsolute) | Out-Null
    $temporaryRoot = Join-Path $outputAbsolute (
        ".tmp-" + [Guid]::NewGuid().ToString("N"))
    [IO.Directory]::CreateDirectory($temporaryRoot) | Out-Null

    $entries = [Collections.Generic.List[object]]::new()
    $totalCanonicalBytes = 0L
    $totalEstimatedTokens = 0L
    try {
        $order = 0
        foreach ($selection in $selected) {
            $indexEntry = $selection.IndexEntry
            $rawBytes = Get-KnowledgeBundleSourceBytes `
                -RepositoryRoot $root -IndexEntry $indexEntry `
                -Dirty $dirtyState.IsDirty -ModifiedPaths $modifiedPaths
            $canonicalBytes = ConvertTo-KnowledgeBundleCanonicalTextBytes `
                -Bytes $rawBytes
            $text = ConvertFrom-KnowledgeBundleStrictUtf8 -Bytes $canonicalBytes
            $rawHash = Get-KnowledgeBundleSha256 -Bytes $rawBytes
            $canonicalHash = Get-KnowledgeBundleSha256 -Bytes $canonicalBytes
            $estimatedTokens = [long][Math]::Ceiling(
                $canonicalBytes.Length / 4.0)
            $totalCanonicalBytes += $canonicalBytes.Length
            $totalEstimatedTokens += $estimatedTokens
            $fileArchivePath = "files/$($indexEntry.Path)"
            Write-KnowledgeBundleFile -Root $temporaryRoot `
                -ArchivePath $fileArchivePath -Bytes $canonicalBytes

            $chunks = @(New-KnowledgeBundleChunkRecords `
                -Path $indexEntry.Path -OwnershipClass $selection.Ownership `
                -RawSha256 $rawHash -CanonicalSha256 $canonicalHash `
                -Text $text -MaximumChunkTokens (
                    $profileConfiguration.Normalized.budgets.maximum_chunk_tokens) `
                -Order $order)
            foreach ($chunk in $chunks) {
                Write-KnowledgeBundleFile -Root $temporaryRoot `
                    -ArchivePath $chunk.Manifest.archive_path -Bytes $chunk.Bytes
            }

            $baselineEntry = if ($ownershipContext.BaselineEntries.Contains(
                    $indexEntry.Path)) {
                $ownershipContext.BaselineEntries[$indexEntry.Path]
            } else {
                $null
            }
            $changeType = if ($null -eq $baselineEntry) {
                "ADDED"
            } elseif ($baselineEntry.ObjectId -eq $indexEntry.ObjectId -and
                    -not $modifiedPaths.Contains($indexEntry.Path)) {
                "UNCHANGED_REFERENCE"
            } else {
                "MODIFIED"
            }
            $inventoryRecord = if ($ownershipContext.InventoryMap.Contains(
                    $indexEntry.Path)) {
                $ownershipContext.InventoryMap[$indexEntry.Path]
            } else {
                $null
            }
            $changeSummary = if ($null -ne $inventoryRecord) {
                [string]$inventoryRecord.purpose
            } elseif ($selection.Ownership -eq "GEOCEDG_NATIVE") {
                "GeoCeDG-owned source selected by the declared profile."
            } else {
                "Pinned upstream reference selected explicitly by profile."
            }
            $unifiedDiffPath = $null
            if ($profileConfiguration.Normalized.include_diffs -and
                    $selection.Ownership -eq "UPSTREAM_MODIFIED") {
                $diffResult = Invoke-KnowledgeBundleGitBytes `
                    -RepositoryRoot $root `
                    -Arguments @("diff", "--binary", "--full-index",
                        "--no-ext-diff", "--no-textconv", "--no-color",
                        "--no-renames", "--diff-algorithm=myers",
                        "--no-indent-heuristic", "--src-prefix=a/", "--dst-prefix=b/",
                        $ownershipContext.Baseline, "--",
                        $indexEntry.Path)
                $diffBytes = ConvertTo-KnowledgeBundleCanonicalTextBytes `
                    -Bytes $diffResult.Bytes
                $unifiedDiffPath = "diffs/$($indexEntry.Path).diff"
                Write-KnowledgeBundleFile -Root $temporaryRoot `
                    -ArchivePath $unifiedDiffPath -Bytes $diffBytes
            }

            $relatedSpecs = [Collections.Generic.List[string]]::new()
            $relatedAdrs = [Collections.Generic.List[string]]::new()
            $relatedTests = [Collections.Generic.List[string]]::new()
            if ($indexEntry.Path.StartsWith("geocedg/specs/")) {
                $relatedSpecs.Add($indexEntry.Path)
            }
            if ($indexEntry.Path.StartsWith("docs/adr/")) {
                $relatedAdrs.Add($indexEntry.Path)
            }
            if ($null -ne $inventoryRecord -and
                    $null -ne $inventoryRecord.authority) {
                $authority = [string]$inventoryRecord.authority
                if ($authority.StartsWith("geocedg/specs/")) {
                    $relatedSpecs.Add($authority)
                } elseif ($authority.StartsWith("docs/adr/")) {
                    $relatedAdrs.Add($authority)
                }
            }
            if ($indexEntry.Path -match '(^|/)src/test/' -or
                    $indexEntry.Path -match '(^|/)tests?(/|\.)') {
                $relatedTests.Add($indexEntry.Path)
            }
            if ($indexEntry.Path.StartsWith("tools/knowledge/") -or
                    $indexEntry.Path.Contains("knowledge-bundle")) {
                $relatedSpecs.Add(
                    "geocedg/specs/operations/knowledge-bundles.md")
                $relatedAdrs.Add(
                    "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md")
            }
            $entry = [ordered]@{
                source_path = $indexEntry.Path
                bundle_path = $fileArchivePath
                ownership_class = $selection.Ownership
                language = Get-KnowledgeBundleLanguage -Path $indexEntry.Path
                encoding = "UTF-8"
                line_range = [ordered]@{
                    start = 1
                    end = Get-KnowledgeBundleLineCount -Text $text
                }
                raw_sha256 = $rawHash
                canonical_sha256 = $canonicalHash
                raw_bytes = $rawBytes.Length
                canonical_bytes = $canonicalBytes.Length
                estimated_tokens = $estimatedTokens
                current_blob_sha = $indexEntry.ObjectId
                baseline_blob_sha = if ($null -ne $baselineEntry) {
                    $baselineEntry.ObjectId
                } else {
                    $null
                }
                change_type = $changeType
                change_summary = $changeSummary
                related_specs = @(Get-KnowledgeBundleOrdinalStrings `
                    -Values @($relatedSpecs) -Unique)
                related_adrs = @(Get-KnowledgeBundleOrdinalStrings `
                    -Values @($relatedAdrs) -Unique)
                related_phases = @("G9O1")
                related_tests = @(Get-KnowledgeBundleOrdinalStrings `
                    -Values @($relatedTests) -Unique)
                license_provenance = Get-KnowledgeBundleLicenseProvenance `
                    -OwnershipClass $selection.Ownership
                order = $order
                chunks = @($chunks | ForEach-Object { $_.Manifest })
            }
            if ($null -ne $unifiedDiffPath) {
                $entry.unified_diff_path = $unifiedDiffPath
            }
            $entries.Add($entry)
            $order++
        }

        Assert-KnowledgeBundleCondition -Condition (
            $totalCanonicalBytes -le
            $profileConfiguration.Normalized.budgets.maximum_bytes) `
            -Message "Bundle byte budget exceeded: $totalCanonicalBytes"
        Assert-KnowledgeBundleCondition -Condition (
            $totalEstimatedTokens -le
            $profileConfiguration.Normalized.budgets.maximum_tokens) `
            -Message "Bundle token estimate budget exceeded: $totalEstimatedTokens"

        if ($dirtyState.IsDirty) {
            Write-KnowledgeBundleFile -Root $temporaryRoot `
                -ArchivePath "state/staged.diff" -Bytes $dirtyState.StagedBytes
            Write-KnowledgeBundleFile -Root $temporaryRoot `
                -ArchivePath "state/unstaged.diff" -Bytes $dirtyState.UnstagedBytes
            $untrackedLines = @($dirtyState.Untracked | ForEach-Object {
                    "$($_.raw_sha256)  $($_.path)"
                }) -join "`n"
            if ($untrackedLines.Length -gt 0) {
                $untrackedLines += "`n"
            }
            Write-KnowledgeBundleFile -Root $temporaryRoot `
                -ArchivePath "state/untracked.sha256" `
                -Bytes ([Text.UTF8Encoding]::new($false).GetBytes($untrackedLines))
        }

        $originUrl = (Invoke-KnowledgeBundleGitText -RepositoryRoot $root `
            -Arguments @("remote", "get-url", "origin")).Text.Trim()
        $upstreamUrl = (Invoke-KnowledgeBundleGitText -RepositoryRoot $root `
            -Arguments @("remote", "get-url", "upstream") `
            -AllowedExitCodes @(0, 2)).Text.Trim()
        if ([string]::IsNullOrWhiteSpace($upstreamUrl)) {
            $upstreamUrl = "https://github.com/geogebra/geogebra.git"
        }
        Assert-KnowledgeBundleCondition -Condition (
            (Test-KnowledgeBundleSafeRemoteUrl -Url $originUrl) -and
            (Test-KnowledgeBundleSafeRemoteUrl -Url $upstreamUrl)) `
            -Message "Repository remote URL is local, unsafe or credential-bearing."
        $upstreamDocument = Join-Path $root "UPSTREAM.md"
        $version = "unknown"
        if (Test-Path -LiteralPath $upstreamDocument -PathType Leaf) {
            $versionMatch = [regex]::Match(
                [IO.File]::ReadAllText($upstreamDocument),
                '(?m)^\| Upstream commit \|.*$|^\| GeoGebra version \| `(?<version>[^`]+)` \|$')
            $allVersionMatches = [regex]::Matches(
                [IO.File]::ReadAllText($upstreamDocument),
                '(?m)^\| GeoGebra version \| `(?<version>[^`]+)` \|$')
            if ($allVersionMatches.Count -eq 1) {
                $version = $allVersionMatches[0].Groups["version"].Value
            }
        }
        $configuration = [ordered]@{}
        foreach ($item in $profileConfiguration.Normalized.GetEnumerator()) {
            $configuration[$item.Key] = $item.Value
        }
        $configuration.canonical_sha256 =
            $profileConfiguration.CanonicalSha256
        $repository = [ordered]@{
            url = $originUrl
            branch = $branch
            commit = $head
            dirty = $dirtyState.IsDirty
        }
        if ($dirtyState.IsDirty) {
            $repository.dirty_diff_sha256 = $dirtyState.DescriptorSha256
            $repository.dirty_state = $dirtyState.Descriptor
        }
        $manifest = [ordered]@{
            '$schema' = $schemaRelative
            schema_version = $script:KnowledgeBundleSchemaVersion
            generator = [ordered]@{
                name = $script:KnowledgeBundleGeneratorName
                version = $script:KnowledgeBundleGeneratorVersion
            }
            repository = $repository
            baseline = [ordered]@{
                repository = $upstreamUrl
                version = $version
                commit = $ownershipContext.Baseline
            }
            configuration = $configuration
            bundle_id = $bundleId
            profile = [string]$profileConfiguration.Profile.kind
            reading_order =
                @($profileConfiguration.Normalized.reading_order)
            summary = [ordered]@{
                files = $entries.Count
                canonical_bytes = $totalCanonicalBytes
                estimated_tokens = $totalEstimatedTokens
                chunks = @($entries | ForEach-Object {
                        @($_.chunks).Count
                    } | Measure-Object -Sum).Sum
                token_estimate = "ceil(canonical UTF-8 bytes / 4)"
            }
            entries = @($entries)
        }
        $manifestBytes = ConvertTo-KnowledgeBundleJsonBytes -Value $manifest
        $manifestText = ConvertFrom-KnowledgeBundleStrictUtf8 `
            -Bytes $manifestBytes
        Assert-KnowledgeBundleCondition -Condition (
            $null -ne (Get-Command Test-Json -ErrorAction SilentlyContinue)) `
            -Message "PowerShell Test-Json is required for manifest validation."
        $schemaAbsolute = Join-Path $root $schemaRelative.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        Assert-KnowledgeBundleCondition -Condition ($manifestText | Test-Json `
                -SchemaFile $schemaAbsolute -ErrorAction Stop) `
            -Message "Generated manifest does not satisfy its JSON schema."
        Write-KnowledgeBundleFile -Root $temporaryRoot `
            -ArchivePath "manifest.json" -Bytes $manifestBytes

        $roundTrip = ConvertFrom-KnowledgeBundleStrictUtf8 -Bytes (
            [IO.File]::ReadAllBytes((Join-Path $temporaryRoot "manifest.json"))) |
            ConvertFrom-Json -Depth 100
        Assert-KnowledgeBundleCondition -Condition (
            $roundTrip.bundle_id -eq $bundleId -and
            @($roundTrip.entries).Count -eq $entries.Count) `
            -Message "Manifest independent re-read failed."

        $archivePath = Join-Path $temporaryRoot "bundle.zip"
        New-KnowledgeBundleArchive -BundleRoot $temporaryRoot `
            -ArchivePath $archivePath

        $headAfter = (Invoke-KnowledgeBundleGitText -RepositoryRoot $root `
            -Arguments @("rev-parse", "--verify", "HEAD")).Text.Trim()
        Assert-KnowledgeBundleCondition -Condition ($headAfter -eq $head) `
            -Message "HEAD changed while the bundle was generated."
        $dirtyAfter = Get-KnowledgeBundleDirtyState -RepositoryRoot $root
        Assert-KnowledgeBundleCondition -Condition (
            $dirtyAfter.IsDirty -eq $dirtyState.IsDirty -and
            $dirtyAfter.DescriptorSha256 -eq $dirtyState.DescriptorSha256 -and
            $dirtyAfter.StatusSha256 -eq $dirtyState.StatusSha256) `
            -Message "Working-tree state changed while the bundle was generated."

        $bundleName = if ($dirtyState.IsDirty) {
            "$ProfileId-$bundleId-dirty-$($dirtyState.DescriptorSha256.Substring(0, 12))"
        } else {
            "$ProfileId-$bundleId"
        }
        $destination = Join-Path $outputAbsolute $bundleName
        if (Test-Path -LiteralPath $destination) {
            $destinationFull = [IO.Path]::GetFullPath($destination)
            $knowledgePrefix = [IO.Path]::GetFullPath(
                (Join-Path $root "artifacts\knowledge")).TrimEnd(
                [IO.Path]::DirectorySeparatorChar) +
                [IO.Path]::DirectorySeparatorChar
            Assert-KnowledgeBundleCondition -Condition (
                $destinationFull.StartsWith($knowledgePrefix,
                    [StringComparison]::OrdinalIgnoreCase)) `
                -Message "Refusing to replace output outside artifacts/knowledge."
            Assert-KnowledgeBundleCondition -Condition (
                Test-Path -LiteralPath $destinationFull -PathType Container) `
                -Message "Existing bundle destination is not a directory."
            Assert-KnowledgeBundleNoReparsePoint -RepositoryRoot $root `
                -AbsolutePath $destinationFull
            Remove-Item -LiteralPath $destinationFull -Recurse -Force
        }
        Move-Item -LiteralPath $temporaryRoot -Destination $destination
        $temporaryRoot = $null
        $finalArchive = Join-Path $destination "bundle.zip"
        return [pscustomobject]@{
            BundleId = $bundleId
            BundleDirectory = $destination
            ManifestPath = Join-Path $destination "manifest.json"
            ArchivePath = $finalArchive
            ArchiveSha256 = Get-KnowledgeBundleSha256 `
                -Bytes ([IO.File]::ReadAllBytes($finalArchive))
            Dirty = $dirtyState.IsDirty
            FileCount = $entries.Count
            ChunkCount = $manifest.summary.chunks
            CanonicalBytes = $totalCanonicalBytes
            EstimatedTokens = $totalEstimatedTokens
        }
    } finally {
        if ($null -ne $temporaryRoot -and
                (Test-Path -LiteralPath $temporaryRoot)) {
            Remove-Item -LiteralPath $temporaryRoot -Recurse -Force
        }
    }
}

Export-ModuleMember -Function @(
    "ConvertFrom-KnowledgeBundleStrictUtf8",
    "ConvertTo-KnowledgeBundleCanonicalTextBytes",
    "ConvertTo-KnowledgeBundleGlobRegex",
    "Get-KnowledgeBundleSha256",
    "Get-KnowledgeBundleVersion",
    "New-GeoCeDGKnowledgeBundle",
    "Test-KnowledgeBundleGlob",
    "Test-KnowledgeBundleRepositoryPath"
)

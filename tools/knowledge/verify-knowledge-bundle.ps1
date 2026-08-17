[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..")).Path,
    [Parameter(Mandatory)] [string]$BundleDirectory,
    [string]$ProfilesPath =
        "geocedg/specs/operations/knowledge-bundle-profiles.json",
    [string]$SchemaPath =
        "geocedg/specs/operations/knowledge-bundle.schema.json",
    [string]$BaselinePath = "docs/upstream/BASELINE_COMMIT.txt",
    [string]$ModifiedInventoryPath = "docs/upstream/modified-files.yml"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$script:GlobRegexCache =
    [Collections.Generic.Dictionary[string, string]]::new(
        [StringComparer]::Ordinal)
$script:CompiledGlobCache =
    [Collections.Generic.Dictionary[string, regex]]::new(
        [StringComparer]::Ordinal)

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Get-Sha256 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha.ComputeHash($Bytes)).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function ConvertFrom-StrictUtf8 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    $offset = 0
    if ($Bytes.Length -ge 3 -and $Bytes[0] -eq 0xEF -and
            $Bytes[1] -eq 0xBB -and $Bytes[2] -eq 0xBF) {
        $offset = 3
    }
    return [Text.UTF8Encoding]::new($false, $true).GetString(
        $Bytes, $offset, $Bytes.Length - $offset)
}

function ConvertTo-CanonicalBytes {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    $text = ConvertFrom-StrictUtf8 -Bytes $Bytes
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return ,([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function ConvertTo-JsonBytes {
    param([Parameter(Mandatory)] [object]$Value)
    $json = $Value | ConvertTo-Json -Depth 100
    $canonical = $json.Replace("`r`n", "`n").Replace("`r", "`n") + "`n"
    return ,([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function Get-OrdinalStrings {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Values,
        [switch]$Unique
    )
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

function Assert-UniqueStrings {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Values,
        [Parameter(Mandatory)] [string]$Description,
        [switch]$RequireNonEmpty
    )
    if ($RequireNonEmpty) {
        Assert-Condition -Condition ($Values.Count -gt 0) `
            -Message "$Description must not be empty."
    }
    $seen = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($value in $Values) {
        Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace($value) -and $seen.Add($value)) `
            -Message "$Description contains an empty or duplicate value."
    }
}

function Invoke-GitBytes {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.WorkingDirectory = $Root
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in @("-C", $Root) + $Arguments) {
        [void]$startInfo.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $output = [IO.MemoryStream]::new()
    try {
        Assert-Condition -Condition $process.Start() `
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
        }
    } finally {
        $output.Dispose()
        $process.Dispose()
    }
}

function Invoke-GitText {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )
    $result = Invoke-GitBytes -Root $Root -Arguments $Arguments `
        -AllowedExitCodes $AllowedExitCodes
    return [pscustomobject]@{
        ExitCode = $result.ExitCode
        Text = (ConvertFrom-StrictUtf8 -Bytes $result.Bytes).Replace(
            "`r`n", "`n").Replace("`r", "`n")
    }
}

function Test-RepositoryPath {
    param([Parameter(Mandatory)] [string]$Path)
    return -not ([string]::IsNullOrWhiteSpace($Path) -or
        [IO.Path]::IsPathRooted($Path) -or $Path.Contains("\") -or
        $Path.Contains(":") -or
        $Path.Contains([char]0) -or $Path -match '^[A-Za-z]:' -or
        $Path -match '[\x00-\x1F]' -or
        $Path -match '(?:^|/)\.(?:/|$)' -or
        $Path -match '(?:^|/)\.\.(?:/|$)' -or $Path.StartsWith("/"))
}

function Test-SafeRemoteUrl {
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

function Assert-NoReparsePoint {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$AbsolutePath
    )
    $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd(
        [IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    $target = [IO.Path]::GetFullPath($AbsolutePath)
    $current = $rootFull
    foreach ($component in ([IO.Path]::GetRelativePath(
                $rootFull, $target)).Split(
            [IO.Path]::DirectorySeparatorChar,
            [StringSplitOptions]::RemoveEmptyEntries)) {
        $current = Join-Path $current $component
        if (Test-Path -LiteralPath $current) {
            $item = Get-Item -LiteralPath $current -Force
            Assert-Condition -Condition (
                ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -eq 0) `
                -Message "Path contains a reparse point: $current"
        }
    }
}

function Resolve-RepositoryPath {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [switch]$RequireFile
    )
    $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd(
        [IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    $absolute = if ([IO.Path]::IsPathRooted($Path)) {
        [IO.Path]::GetFullPath($Path)
    } else {
        [IO.Path]::GetFullPath((Join-Path $rootFull $Path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)))
    }
    $prefix = $rootFull + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Path escapes repository: $Path"
    if ($RequireFile) {
        Assert-Condition -Condition (
            Test-Path -LiteralPath $absolute -PathType Leaf) `
            -Message "Required file is missing: $Path"
    }
    Assert-NoReparsePoint -Root $rootFull -AbsolutePath $absolute
    return $absolute
}

function ConvertTo-GlobRegex {
    param([Parameter(Mandatory)] [string]$Pattern)
    Assert-Condition -Condition (Test-RepositoryPath -Path $Pattern) `
        -Message "Unsafe profile glob: $Pattern"
    if ($script:GlobRegexCache.ContainsKey($Pattern)) {
        return $script:GlobRegexCache[$Pattern]
    }
    $builder = [Text.StringBuilder]::new("^")
    $index = 0
    while ($index -lt $Pattern.Length) {
        if ($Pattern[$index] -eq '*') {
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
        } elseif ($Pattern[$index] -eq '?') {
            [void]$builder.Append("[^/]")
            $index++
        } else {
            [void]$builder.Append([regex]::Escape([string]$Pattern[$index]))
            $index++
        }
    }
    [void]$builder.Append('$')
    $result = $builder.ToString()
    $script:GlobRegexCache[$Pattern] = $result
    return $result
}

function Test-AnyGlob {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string[]]$Patterns
    )
    foreach ($pattern in $Patterns) {
        if (-not $script:CompiledGlobCache.ContainsKey($pattern)) {
            $script:CompiledGlobCache[$pattern] = [regex]::new(
                (ConvertTo-GlobRegex -Pattern $pattern),
                [Text.RegularExpressions.RegexOptions]::CultureInvariant)
        }
        if ($script:CompiledGlobCache[$pattern].IsMatch($Path)) {
            return $true
        }
    }
    return $false
}

function Assert-ProfileCatalog {
    param(
        [Parameter(Mandatory)] [object]$Catalog,
        [Parameter(Mandatory)] [string]$ExpectedSchema
    )
    foreach ($property in @(
            "manifest_schema", "schema_version", "status",
            "implementation_status", "profiles", "default_exclusions",
            "default_budgets")) {
        Assert-Condition -Condition (
            $null -ne $Catalog.PSObject.Properties[$property]) `
            -Message "Profile catalog property is missing: $property"
    }
    Assert-Condition -Condition (
        $Catalog.schema_version -eq 1 -and
        $Catalog.status -eq "NORMATIVE_AUTHOR_APPROVED" -and
        $Catalog.manifest_schema -is [string] -and
        $Catalog.implementation_status -is [string] -and
        -not [string]::IsNullOrWhiteSpace(
            [string]$Catalog.implementation_status) -and
        $Catalog.manifest_schema -eq $ExpectedSchema -and
        (Test-RepositoryPath -Path ([string]$Catalog.manifest_schema)) -and
        $Catalog.profiles -is [array] -and $Catalog.profiles.Count -gt 0 -and
        $Catalog.default_exclusions -is [array]) `
        -Message "Profile catalog/schema authority is invalid."

    $allowedOwnership = @(
        "GEOCEDG_NATIVE", "UPSTREAM_MODIFIED",
        "UPSTREAM_UNCHANGED_REFERENCE")
    $profileIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($candidate in @($Catalog.profiles)) {
        foreach ($property in @(
                "id", "kind", "description", "ownership", "include",
                "include_related_tests", "include_unified_diffs",
                "reading_order")) {
            Assert-Condition -Condition (
                $null -ne $candidate.PSObject.Properties[$property]) `
                -Message "Bundle profile property is missing: $property"
        }
        $candidateId = [string]$candidate.id
        Assert-Condition -Condition (
            $candidate.id -is [string] -and
            $candidate.kind -is [string] -and
            $candidate.description -is [string] -and
            $candidateId -match '^[a-z0-9]+(?:[.-][a-z0-9]+)*$' -and
            $profileIds.Add($candidateId) -and
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
        $includes = [string[]]@($candidate.include | ForEach-Object {
                [string]$_ })
        $ownership = [string[]]@($candidate.ownership | ForEach-Object {
                [string]$_ })
        $readingOrder = [string[]]@($candidate.reading_order |
            ForEach-Object { [string]$_ })
        Assert-UniqueStrings -Values $includes `
            -Description "$candidateId include rules" -RequireNonEmpty
        Assert-UniqueStrings -Values $ownership `
            -Description "$candidateId ownership rules" -RequireNonEmpty
        Assert-UniqueStrings -Values $readingOrder `
            -Description "$candidateId reading order" -RequireNonEmpty
        foreach ($pattern in $includes) {
            [void](ConvertTo-GlobRegex -Pattern $pattern)
        }
        foreach ($class in $ownership) {
            Assert-Condition -Condition ($class -in $allowedOwnership) `
                -Message "Profile requests forbidden ownership class: $class"
        }
        if ($null -ne $candidate.PSObject.Properties["themes"]) {
            Assert-Condition -Condition ($candidate.themes -is [array] -and
                @($candidate.themes | Where-Object {
                        $_ -isnot [string] }).Count -eq 0) `
                -Message "Bundle profile themes are not an array: $candidateId"
            Assert-UniqueStrings -Values @($candidate.themes |
                ForEach-Object { [string]$_ }) `
                -Description "$candidateId themes"
        }
    }

    $exclusions = [string[]]@($Catalog.default_exclusions |
        ForEach-Object { [string]$_ })
    Assert-UniqueStrings -Values $exclusions -Description "default exclusions" `
        -RequireNonEmpty
    Assert-Condition -Condition (@($Catalog.default_exclusions |
            Where-Object { $_ -isnot [string] }).Count -eq 0) `
        -Message "Default exclusions must contain only strings."
    foreach ($pattern in $exclusions) {
        [void](ConvertTo-GlobRegex -Pattern $pattern)
    }
    foreach ($budgetName in @(
            "maximum_files", "maximum_bytes", "maximum_tokens",
            "maximum_chunk_tokens")) {
        Assert-Condition -Condition (
            $null -ne $Catalog.default_budgets.PSObject.Properties[$budgetName] -and
            $Catalog.default_budgets.$budgetName -is [long] -and
            [long]$Catalog.default_budgets.$budgetName -gt 0) `
            -Message "Default bundle budget is missing/invalid: $budgetName"
    }
}

function ConvertFrom-NullRecords {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    if ($Bytes.Length -eq 0) {
        return @()
    }
    return @((ConvertFrom-StrictUtf8 -Bytes $Bytes).Split([char]0) |
        Where-Object { -not [string]::IsNullOrEmpty($_) })
}

function Get-IndexEntries {
    param([Parameter(Mandatory)] [string]$Root)
    $result = Invoke-GitBytes -Root $Root `
        -Arguments @("ls-files", "--stage", "-z")
    $entries = [ordered]@{}
    foreach ($record in ConvertFrom-NullRecords -Bytes $result.Bytes) {
        $match = [regex]::Match($record,
            '^(?<mode>[0-9]{6}) (?<object>[0-9a-f]{40}) (?<stage>[0-3])\t(?<path>.+)$')
        Assert-Condition -Condition $match.Success `
            -Message "Malformed Git index record."
        $path = $match.Groups["path"].Value.Replace("\", "/")
        Assert-Condition -Condition (Test-RepositoryPath -Path $path) `
            -Message "Unsafe Git index path: $path"
        Assert-Condition -Condition ($match.Groups["stage"].Value -eq "0") `
            -Message "Unmerged Git index entry: $path"
        $entries[$path] = [pscustomobject]@{
            Path = $path
            Mode = $match.Groups["mode"].Value
            ObjectId = $match.Groups["object"].Value
        }
    }
    return $entries
}

function Get-TreeEntries {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )
    $result = Invoke-GitBytes -Root $Root `
        -Arguments @("ls-tree", "-r", "-z", $Commit)
    $entries = [ordered]@{}
    foreach ($record in ConvertFrom-NullRecords -Bytes $result.Bytes) {
        $match = [regex]::Match($record,
            '^(?<mode>[0-9]{6}) (?<type>\S+) (?<object>[0-9a-f]{40})\t(?<path>.+)$')
        Assert-Condition -Condition $match.Success `
            -Message "Malformed baseline tree record."
        $path = $match.Groups["path"].Value.Replace("\", "/")
        $entries[$path] = [pscustomobject]@{
            Path = $path
            Mode = $match.Groups["mode"].Value
            ObjectId = $match.Groups["object"].Value
        }
    }
    return $entries
}

function Get-DirtyState {
    param([Parameter(Mandatory)] [string]$Root)
    $status = Invoke-GitBytes -Root $Root -Arguments @(
        "status", "--porcelain=v1", "-z", "--untracked-files=all")
    $staged = Invoke-GitBytes -Root $Root -Arguments @(
        "diff", "--cached", "--binary", "--full-index", "--no-ext-diff",
        "--no-textconv", "--no-color", "--no-renames",
        "--diff-algorithm=myers", "--no-indent-heuristic",
        "--src-prefix=a/", "--dst-prefix=b/", "--")
    $unstaged = Invoke-GitBytes -Root $Root -Arguments @(
        "diff", "--binary", "--full-index", "--no-ext-diff",
        "--no-textconv", "--no-color", "--no-renames",
        "--diff-algorithm=myers", "--no-indent-heuristic",
        "--src-prefix=a/", "--dst-prefix=b/", "--")
    $untrackedResult = Invoke-GitBytes -Root $Root -Arguments @(
        "ls-files", "--others", "--exclude-standard", "-z")
    $untracked = [Collections.Generic.List[object]]::new()
    $untrackedPaths = [string[]]@(ConvertFrom-NullRecords `
        -Bytes $untrackedResult.Bytes)
    foreach ($path in Get-OrdinalStrings -Values $untrackedPaths) {
        Assert-Condition -Condition (Test-RepositoryPath -Path $path) `
            -Message "Unsafe untracked path: $path"
        $absolute = Join-Path $Root $path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        if (Test-Path -LiteralPath $absolute -PathType Leaf) {
            $untrackedItem = Get-Item -LiteralPath $absolute -Force
            Assert-Condition -Condition (
                ($untrackedItem.Attributes -band
                    [IO.FileAttributes]::ReparsePoint) -eq 0) `
                -Message "Untracked reparse point is forbidden: $path"
            $bytes = [IO.File]::ReadAllBytes($absolute)
            $untracked.Add([ordered]@{
                    path = $path
                    raw_sha256 = Get-Sha256 -Bytes $bytes
                    bytes = $bytes.Length
                })
        } else {
            throw "Untracked path is not a regular readable file: $path"
        }
    }
    $descriptor = [ordered]@{
        warning = "NON_RELEASE_EVIDENCE"
        staged_diff_sha256 = Get-Sha256 -Bytes $staged.Bytes
        unstaged_diff_sha256 = Get-Sha256 -Bytes $unstaged.Bytes
        untracked = @($untracked)
    }
    return [pscustomobject]@{
        IsDirty = $status.Bytes.Length -gt 0
        Descriptor = $descriptor
        DescriptorSha256 = Get-Sha256 `
            -Bytes (ConvertTo-JsonBytes -Value $descriptor)
        StagedBytes = $staged.Bytes
        UnstagedBytes = $unstaged.Bytes
        Untracked = @($untracked)
    }
}

function Get-ModifiedPaths {
    param([Parameter(Mandatory)] [string]$Root)
    $result = Invoke-GitBytes -Root $Root `
        -Arguments @("diff", "--name-only", "--no-renames", "-z", "HEAD", "--")
    $paths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($path in ConvertFrom-NullRecords -Bytes $result.Bytes) {
        [void]$paths.Add($path.Replace("\", "/"))
    }
    return ,$paths
}

function Test-RestrictedPath {
    param([Parameter(Mandatory)] [string]$Path)
    if (Test-AnyGlob -Path $Path -Patterns @(
            "docs/references/cedg/**/*.pdf",
            "docs/references/cedg/models/g9p/**/*.ggb",
            "docs/references/cedg/models/g9p/**/*.png",
            "models/legacy/**/original/**", "**/local.properties",
            "**/*secret*", "**/node_modules/**",
            "**/vendor/**", "**/third_party/**")) {
        return $true
    }
    return [IO.Path]::GetExtension($Path).ToLowerInvariant() -in @(
        ".pdf", ".ggb", ".ggt", ".png", ".jpg", ".jpeg", ".gif",
        ".svg", ".webp", ".ico", ".icns", ".ttf", ".otf", ".woff",
        ".woff2", ".jar", ".class", ".zip", ".7z", ".msi", ".exe",
        ".dll", ".so", ".dylib")
}

function Test-GeneratedPath {
    param([Parameter(Mandatory)] [string]$Path)
    return Test-AnyGlob -Path $Path -Patterns @(
        "**/build/**", ".gradle/**", ".kotlin/**", "artifacts/**",
        "packaging/output/**", "**/*.log", "**/*.tmp")
}

function Test-NativePath {
    param([Parameter(Mandatory)] [string]$Path)
    if ($Path -eq "docs/upstream/GEOGEBRA_README.md") {
        return $false
    }
    return Test-AnyGlob -Path $Path -Patterns @(
        "AGENTS.md", "FIRST_AGENT_TASK.md", "README.md", "UPSTREAM.md",
        "NOTICE.md", "THIRD_PARTY.md", "LICENSE", "LICENSES/**",
        ".github/**", "ai-shell/**", "apps/geocedg/**", "geocedg/**",
        "docs/**", "tools/**", "packaging/**", "models/**",
        "benchmarks/**", ".gitattributes", ".gitignore",
        "source/**/org/geocedg/**")
}

function Test-TextPath {
    param([Parameter(Mandatory)] [string]$Path)
    $name = [IO.Path]::GetFileName($Path).ToLowerInvariant()
    if ($name -in @("license", ".gitignore", ".gitattributes")) {
        return $true
    }
    return [IO.Path]::GetExtension($Path).ToLowerInvariant() -in @(
        ".md", ".txt", ".json", ".yml", ".yaml", ".ps1", ".psm1",
        ".java", ".kt", ".kts", ".gradle", ".xml", ".properties",
        ".js", ".ts", ".css", ".scss", ".html", ".csv", ".tsv",
        ".ggs", ".toml", ".ini", ".cfg", ".bat", ".cmd", ".sh",
        ".py", ".sha256")
}

function Get-Ownership {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$IndexEntry,
        [Parameter(Mandatory)] [object]$BaselineEntries,
        [Parameter(Mandatory)] [object]$InventoryMap,
        [Parameter(Mandatory)] [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$ModifiedPaths
    )
    if (Test-RestrictedPath -Path $Path) {
        return "THIRD_PARTY_OR_RESTRICTED"
    }
    if (Test-GeneratedPath -Path $Path) {
        return "GENERATED"
    }
    if ($Path -eq "docs/upstream/GEOGEBRA_README.md") {
        return "UPSTREAM_UNCHANGED_REFERENCE"
    }
    if (Test-NativePath -Path $Path) {
        return "GEOCEDG_NATIVE"
    }
    if ($InventoryMap.Contains($Path)) {
        return "UPSTREAM_MODIFIED"
    }
    if ($BaselineEntries.Contains($Path)) {
        if ($ModifiedPaths.Contains($Path)) {
            throw "Unregistered upstream modification: $Path"
        }
        if ($BaselineEntries[$Path].ObjectId -eq $IndexEntry.ObjectId) {
            return "UPSTREAM_UNCHANGED_REFERENCE"
        }
        throw "Unregistered upstream modification: $Path"
    }
    throw "Ambiguous selected ownership: $Path"
}

function Get-ReadingRank {
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

function Get-LineCount {
    param([Parameter(Mandatory)] [string]$Text)
    if ($Text.Length -eq 0) {
        return 1
    }
    $newlines = ([regex]::Matches($Text, "`n")).Count
    if ($Text.EndsWith("`n")) {
        return [Math]::Max(1, $newlines)
    }
    return $newlines + 1
}

function Get-SafeChunkName {
    param([Parameter(Mandatory)] [string]$Path)
    $name = [IO.Path]::GetFileName($Path)
    $safe = [regex]::Replace($name.ToLowerInvariant(), '[^a-z0-9.-]+', '-')
    if ($safe.Length -gt 60) {
        $safe = $safe.Substring(0, 60)
    }
    return $safe.Trim('-')
}

function Get-Language {
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

function Get-LicenseProvenance {
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
        default { throw "Unsupported inclusion provenance: $OwnershipClass" }
    }
}

function Get-TextSlice {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [int]$Start,
        [Parameter(Mandatory)] [int]$End
    )
    if ($Text.Length -eq 0) {
        return ""
    }
    $lines = $Text.Split("`n")
    $lineCount = Get-LineCount -Text $Text
    Assert-Condition -Condition ($Start -ge 1 -and $End -ge $Start -and
            $End -le $lineCount) -Message "Invalid chunk line range."
    $slice = @($lines[($Start - 1)..($End - 1)]) -join "`n"
    if ($End -lt $lineCount -or ($End -eq $lineCount -and
            $Text.EndsWith("`n"))) {
        $slice += "`n"
    }
    return $slice
}

function Get-SemanticUnits {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [string]$Path
    )
    $lineCount = Get-LineCount -Text $Text
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
        if ($boundary) { $starts.Add($line) }
    }
    $units = [Collections.Generic.List[object]]::new()
    for ($index = 0; $index -lt $starts.Count; $index++) {
        $end = if ($index + 1 -lt $starts.Count) {
            $starts[$index + 1] - 1
        } else { $lineCount }
        $units.Add([pscustomobject]@{ Start = $starts[$index]; End = $end })
    }
    return @($units)
}

function Get-ChunkRanges {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [long]$MaximumChunkTokens
    )
    $maximumBytes = [Math]::Max(1L, $MaximumChunkTokens * 4L)
    $allBytes = [Text.UTF8Encoding]::new($false).GetBytes($Text)
    $lineCount = Get-LineCount -Text $Text
    if ($allBytes.Length -le $maximumBytes) {
        return ,([pscustomobject]@{ Start = 1; End = $lineCount })
    }
    $ranges = [Collections.Generic.List[object]]::new()
    $pendingStart = 0
    $pendingEnd = 0
    foreach ($unit in Get-SemanticUnits -Text $Text -Path $Path) {
        $unitText = Get-TextSlice -Text $Text -Start $unit.Start -End $unit.End
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
                $lineText = Get-TextSlice -Text $Text -Start $line -End $line
                $lineBytes = [Text.UTF8Encoding]::new($false).GetByteCount(
                    $lineText)
                Assert-Condition -Condition ($lineBytes -le $maximumBytes) `
                    -Message "Unsatisfiable chunk budget during verification: $Path"
                if ($line -gt $partStart -and
                        $partBytes + $lineBytes -gt $maximumBytes) {
                    $ranges.Add([pscustomobject]@{
                            Start = $partStart; End = $line - 1 })
                    $partStart = $line
                    $partBytes = 0L
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
        $candidateText = Get-TextSlice -Text $Text `
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

function Get-SourceBytes {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [object]$IndexEntry,
        [Parameter(Mandatory)] [bool]$Dirty,
        [Parameter(Mandatory)] [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$Modified
    )
    Assert-Condition -Condition ($IndexEntry.Mode -in @("100644", "100755")) `
        -Message "Forbidden selected Git mode: $($IndexEntry.Path)"
    if ($Dirty -and $Modified.Contains($IndexEntry.Path)) {
        $absolute = Join-Path $Root $IndexEntry.Path.Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        Assert-Condition -Condition (
            Test-Path -LiteralPath $absolute -PathType Leaf) `
            -Message "Dirty source is missing: $($IndexEntry.Path)"
        $dirtyItem = Get-Item -LiteralPath $absolute -Force
        Assert-Condition -Condition (
            ($dirtyItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -eq 0) `
            -Message "Dirty source is a reparse point: $($IndexEntry.Path)"
        return ,([IO.File]::ReadAllBytes($absolute))
    }
    return ,(Invoke-GitBytes -Root $Root -Arguments @(
            "cat-file", "blob", $IndexEntry.ObjectId)).Bytes
}

try {
    $root = [IO.Path]::GetFullPath((Invoke-GitText `
            -Root ([IO.Path]::GetFullPath($RepositoryRoot)) `
            -Arguments @("rev-parse", "--show-toplevel")).Text.Trim())
    $bundleRoot = if ([IO.Path]::IsPathRooted($BundleDirectory)) {
        [IO.Path]::GetFullPath($BundleDirectory)
    } else {
        [IO.Path]::GetFullPath((Join-Path $root $BundleDirectory))
    }
    $knowledgePrefix = [IO.Path]::GetFullPath(
        (Join-Path $root "artifacts\knowledge")).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) +
        [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($bundleRoot.StartsWith(
            $knowledgePrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Bundle must be below artifacts/knowledge."
    Assert-NoReparsePoint -Root $root -AbsolutePath $bundleRoot
    $manifestPath = Join-Path $bundleRoot "manifest.json"
    $archivePath = Join-Path $bundleRoot "bundle.zip"
    Assert-Condition -Condition (
        (Test-Path -LiteralPath $manifestPath -PathType Leaf) -and
        (Test-Path -LiteralPath $archivePath -PathType Leaf)) `
        -Message "Bundle manifest/archive is missing."

    $manifestBytes = [IO.File]::ReadAllBytes($manifestPath)
    $manifestText = ConvertFrom-StrictUtf8 -Bytes $manifestBytes
    $manifest = $manifestText | ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ((Get-Sha256 -Bytes $manifestBytes) -eq
            (Get-Sha256 -Bytes (ConvertTo-JsonBytes -Value $manifest))) `
        -Message "Manifest JSON bytes are not canonical."
    $schemaAbsolute = Resolve-RepositoryPath -Root $root `
        -Path $SchemaPath -RequireFile
    Assert-Condition -Condition (
        $null -ne (Get-Command Test-Json -ErrorAction SilentlyContinue)) `
        -Message "PowerShell Test-Json is required for manifest validation."
    Assert-Condition -Condition ($manifestText | Test-Json `
            -SchemaFile $schemaAbsolute -ErrorAction Stop) `
        -Message "Manifest does not satisfy its JSON schema."
    foreach ($property in @(
            '$schema', 'schema_version', 'generator', 'repository', 'baseline',
            'configuration', 'bundle_id', 'profile', 'entries', 'summary')) {
        Assert-Condition -Condition (
            $null -ne $manifest.PSObject.Properties[$property]) `
            -Message "Manifest property is missing: $property"
    }
    Assert-Condition -Condition ($manifest.schema_version -eq 1 -and
            $manifest.'$schema' -eq $SchemaPath.Replace("\", "/") -and
            $manifest.generator.name -eq "geocedg-knowledge-bundle" -and
            $manifest.generator.version -match '^\d+\.\d+\.\d+$') `
        -Message "Manifest generator/schema identity is invalid."

    $head = (Invoke-GitText -Root $root -Arguments @(
            "rev-parse", "--verify", "HEAD")).Text.Trim()
    Assert-Condition -Condition ($manifest.repository.commit -eq $head) `
        -Message "Bundle is stale relative to HEAD."
    $dirtyState = Get-DirtyState -Root $root
    Assert-Condition -Condition (
        [bool]$manifest.repository.dirty -eq $dirtyState.IsDirty) `
        -Message "Bundle clean/dirty state does not match the repository."
    if ($dirtyState.IsDirty) {
        Assert-Condition -Condition (
            $manifest.repository.dirty_diff_sha256 -eq
            $dirtyState.DescriptorSha256) `
            -Message "Dirty-state hash does not match the repository."
        $manifestDirtyHash = Get-Sha256 -Bytes (
            ConvertTo-JsonBytes -Value ([ordered]@{
                    warning = [string]$manifest.repository.dirty_state.warning
                    staged_diff_sha256 =
                        [string]$manifest.repository.dirty_state.staged_diff_sha256
                    unstaged_diff_sha256 =
                        [string]$manifest.repository.dirty_state.unstaged_diff_sha256
                    untracked = @($manifest.repository.dirty_state.untracked |
                        ForEach-Object {
                            [ordered]@{
                                path = [string]$_.path
                                raw_sha256 = if ($null -eq $_.raw_sha256) {
                                    $null
                                } else { [string]$_.raw_sha256 }
                                bytes = [long]$_.bytes
                            }
                        })
                }))
        Assert-Condition -Condition ($manifestDirtyHash -eq
                $manifest.repository.dirty_diff_sha256) `
            -Message "Manifest dirty-state descriptor is not canonical."
    }

    $originUrl = (Invoke-GitText -Root $root -Arguments @(
            "remote", "get-url", "origin")).Text.Trim()
    $branchResult = Invoke-GitText -Root $root -Arguments @(
        "symbolic-ref", "--quiet", "--short", "HEAD") `
        -AllowedExitCodes @(0, 1)
    $branch = if ($branchResult.ExitCode -eq 0) {
        $branchResult.Text.Trim()
    } else { $null }
    Assert-Condition -Condition ($manifest.repository.url -eq $originUrl -and
            $manifest.repository.branch -eq $branch -and
            (Test-SafeRemoteUrl -Url $originUrl)) `
        -Message "Manifest repository URL/branch provenance mismatch."

    $config = $manifest.configuration
    $normalizedConfig = [ordered]@{
        profile_id = [string]$config.profile_id
        profile_kind = [string]$config.profile_kind
        themes = @($config.themes | ForEach-Object { [string]$_ })
        ownership = @($config.ownership | ForEach-Object { [string]$_ })
        include = @($config.include | ForEach-Object { [string]$_ })
        include_related_tests = [bool]$config.include_related_tests
        include_diffs = [bool]$config.include_diffs
        reading_order = @($config.reading_order | ForEach-Object { [string]$_ })
        exclusions = @($config.exclusions | ForEach-Object { [string]$_ })
        budgets = [ordered]@{
            maximum_files = [int]$config.budgets.maximum_files
            maximum_bytes = [long]$config.budgets.maximum_bytes
            maximum_tokens = [long]$config.budgets.maximum_tokens
            maximum_chunk_tokens = [long]$config.budgets.maximum_chunk_tokens
        }
    }
    $configurationHash = Get-Sha256 -Bytes (
        ConvertTo-JsonBytes -Value $normalizedConfig)
    Assert-Condition -Condition ($configurationHash -eq
            $config.canonical_sha256) `
        -Message "Canonical configuration hash mismatch."
    $identity = [ordered]@{
        schema_version = 1
        commit = $head
        configuration_sha256 = $configurationHash
    }
    $expectedBundleId = "kb-v1-" + (Get-Sha256 -Bytes (
            ConvertTo-JsonBytes -Value $identity))
    Assert-Condition -Condition ($manifest.bundle_id -eq $expectedBundleId) `
        -Message "Bundle ID mismatch."
    Assert-Condition -Condition ($manifest.profile -eq $config.profile_kind) `
        -Message "Manifest profile kind mismatch."
    Assert-Condition -Condition ((@($manifest.reading_order) |
                ConvertTo-Json -Compress) -eq (@($config.reading_order) |
                ConvertTo-Json -Compress)) `
        -Message "Manifest reading order differs from canonical configuration."
    foreach ($budgetName in @(
            "maximum_files", "maximum_bytes", "maximum_tokens",
            "maximum_chunk_tokens")) {
        Assert-Condition -Condition ($config.budgets.$budgetName -gt 0) `
            -Message "Manifest carries a non-positive budget: $budgetName"
    }
    Assert-Condition -Condition ($config.budgets.maximum_chunk_tokens -le
            ([long]::MaxValue / 4L)) `
        -Message "Manifest chunk-token budget is too large."

    $profilesAbsolute = Resolve-RepositoryPath -Root $root `
        -Path $ProfilesPath -RequireFile
    $profiles = Get-Content -Raw -LiteralPath $profilesAbsolute |
        ConvertFrom-Json -Depth 100
    Assert-ProfileCatalog -Catalog $profiles `
        -ExpectedSchema ([string]$manifest.'$schema')
    $selectedProfiles = @($profiles.profiles | Where-Object {
            $_.id -eq $config.profile_id })
    Assert-Condition -Condition ($selectedProfiles.Count -eq 1) `
        -Message "Manifest profile is absent or duplicated in configuration."
    $profile = $selectedProfiles[0]
    $profileThemes = @()
    if ($null -ne $profile.PSObject.Properties["themes"]) {
        $profileThemes = @(Get-OrdinalStrings -Values @(
                $profile.themes | ForEach-Object { [string]$_ }))
    }
    Assert-Condition -Condition ($profile.kind -eq $config.profile_kind -and
            ((@($profile.include) | ConvertTo-Json -Compress) -eq
                (@($config.include) | ConvertTo-Json -Compress)) -and
            ((@($profile.ownership) | ConvertTo-Json -Compress) -eq
                (@($config.ownership) | ConvertTo-Json -Compress)) -and
            ((@($profiles.default_exclusions) | ConvertTo-Json -Compress) -eq
                (@($config.exclusions) | ConvertTo-Json -Compress)) -and
            ((@($profile.reading_order) | ConvertTo-Json -Compress) -eq
                (@($config.reading_order) | ConvertTo-Json -Compress)) -and
            ((@($profileThemes) | ConvertTo-Json -Compress) -eq
                (@($config.themes) | ConvertTo-Json -Compress)) -and
            [bool]$profile.include_related_tests -eq
                [bool]$config.include_related_tests -and
            [bool]$profile.include_unified_diffs -eq
                [bool]$config.include_diffs) `
        -Message "Manifest selection configuration drifted from the profile."

    $baselineAbsolute = Resolve-RepositoryPath -Root $root `
        -Path $BaselinePath -RequireFile
    $baseline = [IO.File]::ReadAllText($baselineAbsolute).Trim()
    Assert-Condition -Condition ($baseline -eq $manifest.baseline.commit) `
        -Message "Manifest baseline commit mismatch."
    $upstreamResult = Invoke-GitText -Root $root -Arguments @(
        "remote", "get-url", "upstream") -AllowedExitCodes @(0, 2)
    $upstreamUrl = $upstreamResult.Text.Trim()
    if ([string]::IsNullOrWhiteSpace($upstreamUrl)) {
        $upstreamUrl = "https://github.com/geogebra/geogebra.git"
    }
    Assert-Condition -Condition (Test-SafeRemoteUrl -Url $upstreamUrl) `
        -Message "Upstream remote URL is local, unsafe or credential-bearing."
    $upstreamDocument = Join-Path $root "UPSTREAM.md"
    $upstreamVersion = "unknown"
    if (Test-Path -LiteralPath $upstreamDocument -PathType Leaf) {
        $matches = [regex]::Matches(
            [IO.File]::ReadAllText($upstreamDocument),
            '(?m)^\| GeoGebra version \| `(?<version>[^`]+)` \|$')
        if ($matches.Count -eq 1) {
            $upstreamVersion = $matches[0].Groups["version"].Value
        }
    }
    Assert-Condition -Condition (
        $manifest.baseline.repository -eq $upstreamUrl -and
        $manifest.baseline.version -eq $upstreamVersion) `
        -Message "Manifest upstream repository/version provenance mismatch."
    $indexEntries = Get-IndexEntries -Root $root
    $baselineEntries = Get-TreeEntries -Root $root -Commit $baseline
    $inventoryAbsolute = Resolve-RepositoryPath -Root $root `
        -Path $ModifiedInventoryPath -RequireFile
    foreach ($authorityAbsolute in @(
            $profilesAbsolute, $schemaAbsolute, $baselineAbsolute,
            $inventoryAbsolute)) {
        $authorityPath = [IO.Path]::GetRelativePath(
            $root, $authorityAbsolute).Replace("\", "/")
        Assert-Condition -Condition (
            $indexEntries.Contains($authorityPath) -and
            $indexEntries[$authorityPath].Mode -in @("100644", "100755")) `
            -Message "Bundle control input is not a tracked regular file: $authorityPath"
    }
    $inventory = Get-Content -Raw -LiteralPath $inventoryAbsolute |
        ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ($inventory.schema_version -eq 1 -and
            $inventory.baseline_sha -eq $baseline) `
        -Message "Modified-file inventory baseline mismatch."
    $inventoryMap = [ordered]@{}
    foreach ($record in @($inventory.modifications)) {
        $path = ([string]$record.path).Replace("\", "/")
        Assert-Condition -Condition (Test-RepositoryPath -Path $path) `
            -Message "Unsafe modified-file inventory path: $path"
        Assert-Condition -Condition (-not $inventoryMap.Contains($path)) `
            -Message "Duplicate inventory path: $path"
        Assert-Condition -Condition ($indexEntries.Contains($path)) `
            -Message "Inventory path missing from index: $path"
        $baselineContains = $baselineEntries.Contains($path)
        Assert-Condition -Condition (-not $baselineContains -or
                $baselineEntries[$path].ObjectId -ne
                $indexEntries[$path].ObjectId) `
            -Message "Inventory/Git disagreement: $path"
        if ($record.change -eq "added") {
            Assert-Condition -Condition (-not $baselineContains) `
                -Message "Inventory added/modified disagreement: $path"
        } elseif ($record.change -eq "modified") {
            Assert-Condition -Condition $baselineContains `
                -Message "Inventory modified/added disagreement: $path"
        } else {
            throw "Unsupported inventory change: $($record.change)"
        }
        $inventoryMap[$path] = $record
    }

    $modifiedPaths = Get-ModifiedPaths -Root $root
    $expectedSelections = [Collections.Generic.List[object]]::new()
    foreach ($indexEntry in $indexEntries.Values) {
        if (-not (Test-AnyGlob -Path $indexEntry.Path `
                -Patterns @($config.include))) {
            continue
        }
        $ownership = Get-Ownership -Path $indexEntry.Path `
            -IndexEntry $indexEntry -BaselineEntries $baselineEntries `
            -InventoryMap $inventoryMap `
            -ModifiedPaths $modifiedPaths
        if (Test-AnyGlob -Path $indexEntry.Path `
                -Patterns @($config.exclusions)) {
            continue
        }
        if ($ownership -in @($config.ownership)) {
            Assert-Condition -Condition (Test-TextPath -Path $indexEntry.Path) `
                -Message "Selected file has no explicit UTF-8 text policy: $($indexEntry.Path)"
            $expectedSelections.Add([pscustomobject]@{
                    Path = $indexEntry.Path
                    Ownership = $ownership
                    Rank = Get-ReadingRank -Path $indexEntry.Path `
                        -ReadingOrder @($config.reading_order)
                })
        }
    }
    $expectedSelectionArray = [object[]]$expectedSelections.ToArray()
    $selectionComparison = [Comparison[object]] {
        param($left, $right)
        $rankComparison = $left.Rank.CompareTo($right.Rank)
        if ($rankComparison -ne 0) { return $rankComparison }
        return [StringComparer]::Ordinal.Compare($left.Path, $right.Path)
    }
    [Array]::Sort($expectedSelectionArray, $selectionComparison)
    $expectedSelections = $expectedSelectionArray
    $entries = @($manifest.entries)
    Assert-Condition -Condition ($entries.Count -eq
            $expectedSelections.Count) `
        -Message "Manifest profile membership count mismatch."
    Assert-Condition -Condition ($entries.Count -le
            $config.budgets.maximum_files) `
        -Message "Manifest exceeds file budget."

    $canonicalByteTotal = 0L
    $tokenTotal = 0L
    $chunkTotal = 0L
    $expectedDiskPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    [void]$expectedDiskPaths.Add("manifest.json")
    if ($dirtyState.IsDirty) {
        foreach ($statePath in @(
                "state/staged.diff", "state/unstaged.diff",
                "state/untracked.sha256")) {
            [void]$expectedDiskPaths.Add($statePath)
        }
        $stateBytes = [ordered]@{
            "state/staged.diff" = $dirtyState.StagedBytes
            "state/unstaged.diff" = $dirtyState.UnstagedBytes
        }
        $untrackedLines = @($dirtyState.Untracked | ForEach-Object {
                "$($_.raw_sha256)  $($_.path)"
            }) -join "`n"
        if ($untrackedLines.Length -gt 0) {
            $untrackedLines += "`n"
        }
        $stateBytes["state/untracked.sha256"] =
            [Text.UTF8Encoding]::new($false).GetBytes($untrackedLines)
        foreach ($statePath in $stateBytes.Keys) {
            $stateAbsolute = Join-Path $bundleRoot $statePath.Replace(
                "/", [IO.Path]::DirectorySeparatorChar)
            Assert-Condition -Condition (
                (Test-Path -LiteralPath $stateAbsolute -PathType Leaf) -and
                (Get-Sha256 -Bytes ([IO.File]::ReadAllBytes($stateAbsolute))) -eq
                    (Get-Sha256 -Bytes $stateBytes[$statePath])) `
                -Message "Dirty-state artifact mismatch: $statePath"
        }
    }
    for ($index = 0; $index -lt $entries.Count; $index++) {
        $entry = $entries[$index]
        $expected = $expectedSelections[$index]
        Assert-Condition -Condition ($entry.order -eq $index -and
                $entry.source_path -eq $expected.Path -and
                $entry.ownership_class -eq $expected.Ownership) `
            -Message "Manifest order/ownership mismatch at entry $index."
        Assert-Condition -Condition (Test-RepositoryPath `
                -Path $entry.source_path) `
            -Message "Unsafe manifest source path."
        Assert-Condition -Condition ($entry.ownership_class -notin @(
                "GENERATED", "THIRD_PARTY_OR_RESTRICTED")) `
            -Message "Forbidden ownership entered the bundle."
        $indexEntry = $indexEntries[$entry.source_path]
        $rawBytes = Get-SourceBytes -Root $root -IndexEntry $indexEntry `
            -Dirty $dirtyState.IsDirty -Modified $modifiedPaths
        $canonicalBytes = ConvertTo-CanonicalBytes -Bytes $rawBytes
        $text = ConvertFrom-StrictUtf8 -Bytes $canonicalBytes
        Assert-Condition -Condition (
            $entry.raw_sha256 -eq (Get-Sha256 -Bytes $rawBytes) -and
            $entry.canonical_sha256 -eq (
                Get-Sha256 -Bytes $canonicalBytes) -and
            $entry.raw_bytes -eq $rawBytes.Length -and
            $entry.canonical_bytes -eq $canonicalBytes.Length -and
            $entry.current_blob_sha -eq $indexEntry.ObjectId -and
            $entry.estimated_tokens -eq
                [Math]::Ceiling($canonicalBytes.Length / 4.0) -and
            $entry.language -eq (Get-Language -Path $entry.source_path) -and
            $entry.encoding -eq "UTF-8" -and
            $entry.license_provenance -eq (Get-LicenseProvenance `
                -OwnershipClass $entry.ownership_class)) `
            -Message "Raw/canonical source evidence mismatch: $($entry.source_path)"
        $canonicalByteTotal += $canonicalBytes.Length
        $tokenTotal += [long][Math]::Ceiling($canonicalBytes.Length / 4.0)
        Assert-Condition -Condition ($entry.bundle_path -eq
                "files/$($entry.source_path)") `
            -Message "Unexpected complete-file archive path."
        [void]$expectedDiskPaths.Add([string]$entry.bundle_path)
        $fileAbsolute = Join-Path $bundleRoot (
            [string]$entry.bundle_path).Replace(
            "/", [IO.Path]::DirectorySeparatorChar)
        Assert-Condition -Condition (Test-Path -LiteralPath $fileAbsolute) `
            -Message "Complete bundle file is missing: $($entry.bundle_path)"
        Assert-Condition -Condition ((Get-Sha256 -Bytes (
                    [IO.File]::ReadAllBytes($fileAbsolute))) -eq
                $entry.canonical_sha256) `
            -Message "Complete bundle file hash mismatch."

        $lineCount = Get-LineCount -Text $text
        Assert-Condition -Condition ($entry.line_range.start -eq 1 -and
                $entry.line_range.end -eq $lineCount) `
            -Message "Complete source line range mismatch."
        $chunks = @($entry.chunks)
        $expectedRanges = @(Get-ChunkRanges -Text $text `
            -Path ([string]$entry.source_path) `
            -MaximumChunkTokens $config.budgets.maximum_chunk_tokens)
        Assert-Condition -Condition ($chunks.Count -eq $expectedRanges.Count -and
                $chunks.Count -gt 0) `
            -Message "Source chunk count differs from the deterministic plan."
        $nextLine = 1
        $continuationBytes = [Text.UTF8Encoding]::new($false).GetBytes(
            "$($entry.source_path)`n$($entry.canonical_sha256)`n")
        $expectedContinuation = "kb-cont-" + (
            Get-Sha256 -Bytes $continuationBytes)
        $safeName = Get-SafeChunkName -Path $entry.source_path
        for ($chunkIndex = 0; $chunkIndex -lt $chunks.Count; $chunkIndex++) {
            $chunk = $chunks[$chunkIndex]
            $expectedRange = $expectedRanges[$chunkIndex]
            $expectedChunkPath = "chunks/{0:D6}-{1:D4}-{2}-{3}.txt" -f
                $index, ($chunkIndex + 1), $safeName,
                $entry.canonical_sha256.Substring(0, 12)
            Assert-Condition -Condition ($chunk.sequence -eq $chunkIndex + 1 -and
                    $chunk.total -eq $chunks.Count -and
                    $chunk.line_range.start -eq $expectedRange.Start -and
                    $chunk.line_range.end -eq $expectedRange.End -and
                    $chunk.line_range.start -eq $nextLine -and
                    $chunk.line_range.end -ge $nextLine -and
                    $chunk.complete_source_sha256 -eq
                    $entry.canonical_sha256 -and
                    $chunk.continuation_id -eq $expectedContinuation -and
                    $chunk.archive_path -eq $expectedChunkPath -and
                    (Test-RepositoryPath -Path $chunk.archive_path)) `
                -Message "Invalid continuation topology: $($entry.source_path)"
            $body = Get-TextSlice -Text $text `
                -Start $chunk.line_range.start -End $chunk.line_range.end
            $header = @(
                "FILE: $($entry.source_path)"
                "OWNERSHIP: $($entry.ownership_class)"
                "SOURCE_RAW_SHA256: $($entry.raw_sha256)"
                "SOURCE_CANONICAL_SHA256: $($entry.canonical_sha256)"
                "LINES: $($chunk.line_range.start)-$($chunk.line_range.end)"
                "CHUNK: $($chunk.sequence)/$($chunk.total)"
                "CONTINUATION_ID: $($chunk.continuation_id)"
                "---"
                ""
            ) -join "`n"
            $expectedChunkBytes = [Text.UTF8Encoding]::new($false).GetBytes(
                $header + $body)
            $bodyBytes = [Text.UTF8Encoding]::new($false).GetByteCount($body)
            Assert-Condition -Condition ([Math]::Ceiling(
                    $bodyBytes / 4.0) -le
                    $config.budgets.maximum_chunk_tokens) `
                -Message "Chunk payload exceeds configured token budget."
            $chunkAbsolute = Join-Path $bundleRoot (
                [string]$chunk.archive_path).Replace(
                "/", [IO.Path]::DirectorySeparatorChar)
            Assert-Condition -Condition (
                Test-Path -LiteralPath $chunkAbsolute -PathType Leaf) `
                -Message "Chunk file is missing: $($chunk.archive_path)"
            $actualChunkBytes = [IO.File]::ReadAllBytes($chunkAbsolute)
            Assert-Condition -Condition (
                $chunk.sha256 -eq (Get-Sha256 -Bytes $actualChunkBytes) -and
                $chunk.sha256 -eq (Get-Sha256 -Bytes $expectedChunkBytes) -and
                $chunk.bytes -eq $actualChunkBytes.Length -and
                $chunk.estimated_tokens -eq
                [Math]::Ceiling($actualChunkBytes.Length / 4.0)) `
                -Message "Chunk content/hash mismatch."
            [void]$expectedDiskPaths.Add([string]$chunk.archive_path)
            $nextLine = $chunk.line_range.end + 1
            $chunkTotal++
        }
        Assert-Condition -Condition ($nextLine -eq $lineCount + 1) `
            -Message "Chunk ranges do not cover the complete source."

        $baselineEntry = if ($baselineEntries.Contains($entry.source_path)) {
            $baselineEntries[$entry.source_path]
        } else { $null }
        $expectedChangeType = if ($null -eq $baselineEntry) {
            "ADDED"
        } elseif ($baselineEntry.ObjectId -eq $indexEntry.ObjectId -and
                -not $modifiedPaths.Contains($entry.source_path)) {
            "UNCHANGED_REFERENCE"
        } else { "MODIFIED" }
        $expectedBaselineBlob = if ($null -eq $baselineEntry) {
            $null
        } else { $baselineEntry.ObjectId }
        $inventoryRecord = if ($inventoryMap.Contains($entry.source_path)) {
            $inventoryMap[$entry.source_path]
        } else { $null }
        $expectedChangeSummary = if ($null -ne $inventoryRecord) {
            [string]$inventoryRecord.purpose
        } elseif ($entry.ownership_class -eq "GEOCEDG_NATIVE") {
            "GeoCeDG-owned source selected by the declared profile."
        } else {
            "Pinned upstream reference selected explicitly by profile."
        }
        $relatedSpecs = [Collections.Generic.List[string]]::new()
        $relatedAdrs = [Collections.Generic.List[string]]::new()
        $relatedTests = [Collections.Generic.List[string]]::new()
        if ($entry.source_path.StartsWith("geocedg/specs/")) {
            $relatedSpecs.Add([string]$entry.source_path)
        }
        if ($entry.source_path.StartsWith("docs/adr/")) {
            $relatedAdrs.Add([string]$entry.source_path)
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
        if ($entry.source_path -match '(^|/)src/test/' -or
                $entry.source_path -match '(^|/)tests?(/|\.)') {
            $relatedTests.Add([string]$entry.source_path)
        }
        if ($entry.source_path.StartsWith("tools/knowledge/") -or
                $entry.source_path.Contains("knowledge-bundle")) {
            $relatedSpecs.Add(
                "geocedg/specs/operations/knowledge-bundles.md")
            $relatedAdrs.Add(
                "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md")
        }
        $expectedSpecs = @(Get-OrdinalStrings -Values @($relatedSpecs) -Unique)
        $expectedAdrs = @(Get-OrdinalStrings -Values @($relatedAdrs) -Unique)
        $expectedTests = @(Get-OrdinalStrings -Values @($relatedTests) -Unique)
        Assert-Condition -Condition ($entry.change_type -eq
                $expectedChangeType -and
                $entry.baseline_blob_sha -eq $expectedBaselineBlob -and
                $entry.change_summary -eq $expectedChangeSummary -and
                ((@($entry.related_specs) | ConvertTo-Json -Compress) -eq
                    ($expectedSpecs | ConvertTo-Json -Compress)) -and
                ((@($entry.related_adrs) | ConvertTo-Json -Compress) -eq
                    ($expectedAdrs | ConvertTo-Json -Compress)) -and
                ((@($entry.related_tests) | ConvertTo-Json -Compress) -eq
                    ($expectedTests | ConvertTo-Json -Compress)) -and
                ((@($entry.related_phases) | ConvertTo-Json -Compress) -eq
                    (@("G9O1") | ConvertTo-Json -Compress))) `
            -Message "Entry change/baseline/relationship provenance mismatch."

        if ($entry.ownership_class -eq "UPSTREAM_MODIFIED") {
            Assert-Condition -Condition ($inventoryMap.Contains(
                    $entry.source_path) -and
                    $null -ne $entry.baseline_blob_sha -and
                    $entry.baseline_blob_sha -eq
                    $baselineEntries[$entry.source_path].ObjectId -and
                    -not [string]::IsNullOrWhiteSpace($entry.change_summary)) `
                -Message "Upstream-modified provenance is incomplete."
        }
        $hasDiff = $null -ne $entry.PSObject.Properties["unified_diff_path"]
        $requiresDiff = [bool]$config.include_diffs -and
            $entry.ownership_class -eq "UPSTREAM_MODIFIED"
        Assert-Condition -Condition ($hasDiff -eq $requiresDiff) `
            -Message "Unified-diff presence does not match profile policy."
        if ($hasDiff) {
            Assert-Condition -Condition ($entry.ownership_class -eq
                    "UPSTREAM_MODIFIED" -and
                    $entry.unified_diff_path -eq
                        "diffs/$($entry.source_path).diff") `
                -Message "Only upstream-modified entries may carry diffs."
            $diffResult = Invoke-GitBytes -Root $root -Arguments @(
                "diff", "--binary", "--full-index", "--no-ext-diff",
                "--no-textconv", "--no-color", "--no-renames",
                "--diff-algorithm=myers", "--no-indent-heuristic",
                "--src-prefix=a/", "--dst-prefix=b/", $baseline, "--",
                [string]$entry.source_path)
            $expectedDiff = ConvertTo-CanonicalBytes -Bytes $diffResult.Bytes
            $diffAbsolute = Join-Path $bundleRoot (
                [string]$entry.unified_diff_path).Replace(
                "/", [IO.Path]::DirectorySeparatorChar)
            Assert-Condition -Condition ((Get-Sha256 -Bytes (
                        [IO.File]::ReadAllBytes($diffAbsolute))) -eq
                    (Get-Sha256 -Bytes $expectedDiff)) `
                -Message "Derived unified diff mismatch."
            [void]$expectedDiskPaths.Add([string]$entry.unified_diff_path)
        }
    }
    Assert-Condition -Condition ($canonicalByteTotal -eq
            $manifest.summary.canonical_bytes -and
            $entries.Count -eq $manifest.summary.files -and
            $canonicalByteTotal -le $config.budgets.maximum_bytes -and
            $tokenTotal -eq $manifest.summary.estimated_tokens -and
            $tokenTotal -le $config.budgets.maximum_tokens -and
            $chunkTotal -eq $manifest.summary.chunks -and
            $manifest.summary.token_estimate -eq
                "ceil(canonical UTF-8 bytes / 4)") `
        -Message "Bundle totals/budgets mismatch."

    $diskPaths = @(Get-ChildItem -LiteralPath $bundleRoot -Recurse -File |
        Where-Object { $_.FullName -ne $archivePath } |
        ForEach-Object {
            [IO.Path]::GetRelativePath($bundleRoot, $_.FullName).Replace("\", "/")
        })
    Assert-Condition -Condition ($diskPaths.Count -eq
            $expectedDiskPaths.Count) `
        -Message "Unexpected or missing files in bundle directory."
    foreach ($path in $diskPaths) {
        Assert-Condition -Condition ($expectedDiskPaths.Contains($path)) `
            -Message "Unexpected bundle file: $path"
    }

    Add-Type -AssemblyName System.IO.Compression
    $archive = [IO.Compression.ZipFile]::OpenRead($archivePath)
    try {
        $archiveNames = @($archive.Entries | ForEach-Object {
                $_.FullName
            })
        $sortedArchiveNames = @(Get-OrdinalStrings -Values `
            ([string[]]$archiveNames))
        Assert-Condition -Condition (($archiveNames -join "`n") -eq
                ($sortedArchiveNames -join "`n") -and
                $archiveNames.Count -eq $diskPaths.Count) `
            -Message "Archive entries are not complete and stably ordered."
        foreach ($entry in $archive.Entries) {
            Assert-Condition -Condition (Test-RepositoryPath `
                    -Path $entry.FullName) `
                -Message "Unsafe archive entry path: $($entry.FullName)"
            $fixedTimestamp = $entry.LastWriteTime.Year -eq 1980 -and
                $entry.LastWriteTime.Month -eq 1 -and
                $entry.LastWriteTime.Day -eq 1 -and
                $entry.LastWriteTime.Hour -eq 0 -and
                $entry.LastWriteTime.Minute -eq 0 -and
                $entry.LastWriteTime.Second -eq 0
            Assert-Condition -Condition ($fixedTimestamp -and
                    $entry.CompressedLength -eq $entry.Length -and
                    $entry.ExternalAttributes -eq [int]-2119958528) `
                -Message "Archive metadata/compression is not deterministic."
            $diskPath = Join-Path $bundleRoot $entry.FullName.Replace(
                "/", [IO.Path]::DirectorySeparatorChar)
            $entryStream = $entry.Open()
            $memory = [IO.MemoryStream]::new()
            try {
                $entryStream.CopyTo($memory)
                Assert-Condition -Condition ((Get-Sha256 -Bytes (
                            $memory.ToArray())) -eq (Get-Sha256 -Bytes (
                            [IO.File]::ReadAllBytes($diskPath)))) `
                    -Message "Archive entry bytes differ from staged file."
            } finally {
                $memory.Dispose()
                $entryStream.Dispose()
            }
        }
    } finally {
        $archive.Dispose()
    }

    $result = [pscustomobject]@{
        BundleId = [string]$manifest.bundle_id
        ProfileId = [string]$config.profile_id
        Dirty = [bool]$manifest.repository.dirty
        FileCount = $entries.Count
        ChunkCount = $chunkTotal
        ArchiveSha256 = Get-Sha256 -Bytes (
            [IO.File]::ReadAllBytes($archivePath))
    }
    Write-Host "Knowledge-bundle verification passed."
    Write-Host "  ID: $($result.BundleId)"
    Write-Host "  Files: $($result.FileCount)"
    Write-Host "  Chunks: $($result.ChunkCount)"
    Write-Host "  Archive SHA-256: $($result.ArchiveSha256)"
    $result
} catch {
    Write-Error "Knowledge-bundle verification failed: $($_.Exception.Message)"
    exit 1
}

exit 0

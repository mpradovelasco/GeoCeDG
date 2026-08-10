Set-StrictMode -Version Latest

function Assert-GeoCeDGUpstreamBoundary {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ExpectedBaseline,
        [string]$ManifestPath = "docs/upstream/modified-files.yml",
        [string[]]$UpstreamPaths = @(
            "source", "gradle", "gradle.properties", "settings.gradle.kts",
            "gradlew", "gradlew.bat", "doc/dev"
        ),
        [string[]]$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $manifestAbsolute = [IO.Path]::GetFullPath((Join-Path $root $ManifestPath))
    if (-not $manifestAbsolute.StartsWith(
            $root + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Upstream modification manifest escapes the repository: $ManifestPath"
    }
    if (-not (Test-Path -LiteralPath $manifestAbsolute -PathType Leaf)) {
        throw "Upstream modification manifest is missing: $ManifestPath"
    }

    try {
        $manifest = Get-Content -Raw -LiteralPath $manifestAbsolute |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$ManifestPath is not valid JSON-compatible YAML: $($_.Exception.Message)"
    }
    if ($manifest.schema_version -ne 1) {
        throw "$ManifestPath has unsupported schema_version."
    }
    if ($manifest.baseline_sha -ne $ExpectedBaseline) {
        throw "$ManifestPath records baseline $($manifest.baseline_sha); expected $ExpectedBaseline."
    }

    $declared = [Collections.Generic.Dictionary[string, object]]::new(
        [StringComparer]::Ordinal)
    foreach ($entry in @($manifest.modifications)) {
        $path = ([string]$entry.path).Replace("\", "/").TrimStart("/")
        if ([string]::IsNullOrWhiteSpace($path) -or
                [IO.Path]::IsPathRooted([string]$entry.path) -or
                $path.Contains("../")) {
            throw "Invalid upstream modification path: $($entry.path)"
        }
        $isUpstream = @($UpstreamPaths | Where-Object {
                $path -eq $_ -or $path.StartsWith("$_/", [StringComparison]::Ordinal)
            }).Count -gt 0
        if (-not $isUpstream) {
            throw "Registered path is outside the upstream boundary: $path"
        }
        if (-not $declared.TryAdd($path, $entry)) {
            throw "Duplicate upstream modification path: $path"
        }
        $absolute = [IO.Path]::GetFullPath((Join-Path $root $path))
        if (-not $absolute.StartsWith(
                $root + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase) -or
                -not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
            throw "Registered upstream modification is not a current file: $path"
        }
        & git -C $root cat-file -e "${ExpectedBaseline}:$path" 2>$null
        $existedAtBaseline = $LASTEXITCODE -eq 0
        if ($entry.change -eq "modified" -and -not $existedAtBaseline) {
            throw "Registered modified file did not exist at baseline: $path"
        }
        if ($entry.change -eq "added" -and $existedAtBaseline) {
            throw "Registered added file already existed at baseline: $path"
        }
        if ($entry.change -notin @("added", "modified")) {
            throw "Unsupported change kind '$($entry.change)' for $path"
        }
        if ([string]::IsNullOrWhiteSpace([string]$entry.purpose) -or
                [string]::IsNullOrWhiteSpace([string]$entry.authority)) {
            throw "Registered upstream modification lacks purpose or authority: $path"
        }
    }

    $actual = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    $commands = @(
        [pscustomobject]@{
            Arguments = @("diff", "--name-only", "$ExpectedBaseline..HEAD", "--") +
                $UpstreamPaths
        },
        [pscustomobject]@{
            Arguments = @("diff", "--name-only", "--") + $UpstreamPaths
        },
        [pscustomobject]@{
            Arguments = @("diff", "--cached", "--name-only", "--") + $UpstreamPaths
        },
        [pscustomobject]@{
            Arguments = @("ls-files", "--others", "--exclude-standard", "--") +
                $UpstreamPaths
        }
    )
    foreach ($command in $commands) {
        $arguments = @($command.Arguments)
        $paths = & git -C $root @arguments
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to inspect the upstream boundary with git $($arguments -join ' ')."
        }
        foreach ($candidate in @($paths)) {
            $normalized = $candidate.Replace("\", "/")
            $segments = $normalized -split "/"
            if (@($segments | Where-Object {
                        $GeneratedDirectoryNames -contains $_
                    }).Count -eq 0) {
                [void]$actual.Add($normalized)
            }
        }
    }

    $unexpected = @($actual | Where-Object { -not $declared.ContainsKey($_) } | Sort-Object)
    $missing = @($declared.Keys | Where-Object { -not $actual.Contains($_) } | Sort-Object)
    if ($unexpected.Count -gt 0 -or $missing.Count -gt 0) {
        $parts = [Collections.Generic.List[string]]::new()
        if ($unexpected.Count -gt 0) {
            $parts.Add("Unregistered upstream changes: $($unexpected -join ', ')")
        }
        if ($missing.Count -gt 0) {
            $parts.Add("Registered paths without a baseline difference: $($missing -join ', ')")
        }
        throw $parts -join [Environment]::NewLine
    }

    Write-Host "Controlled upstream boundary: $($declared.Count) registered file(s)."
}

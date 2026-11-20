<#
.SYNOPSIS
Inspects or explicitly delegates to the independent GeoCeDG book worktree.

.DESCRIPTION
Resolves the root-level book link, proves that it belongs to a separate Git
repository outside the GeoCeDG worktree, and reports both repository states.
The default Status action and Alignment are read-only. BaselineCandidate writes
only ignored GeoCeDG evidence. Evidence delegates to the existing G9O1 bundle
generator. Verify and Build only invoke the corresponding book-owned
PowerShell entry point when it already exists.

This script never installs tools or stages, commits, merges, tags, fetches,
pulls or pushes either repository. It is deliberately not part of
tools/agent/verify.ps1.

.PARAMETER Action
Status reports the validated two-repository state and technical alignment.
Alignment reports the alignment classification alone. BaselineCandidate emits
a deterministic published-state candidate without changing the book.
Evidence composes existing G9O1 profiles. Verify and Build delegate to the
book-owned tools/verify.ps1 and tools/build.ps1 entry points.

.PARAMETER BookArguments
Optional arguments forwarded to an explicitly selected book-owned script.

.PARAMETER OutputPath
Repository-relative destination for BaselineCandidate evidence.

.PARAMETER EvidenceProfiles
Existing G9O1 profiles to generate for the Evidence action.

.PARAMETER EvidenceOutputDirectory
Repository-relative parent directory for generated G9O1 evidence.

.PARAMETER AllowDirtyEvidence
Explicitly permits dirty or unpublished NON_RELEASE evidence and forwards the
dirty-evidence opt-in to the G9O1 generator.

.PARAMETER ReferenceTag
Optional explicit technical reference: an annotated phase-checkpoint tag
(geocedg-*-pass) or an annotated version tag (vMAJOR.MINOR.PATCH) backed by a
valid release record under docs/releases in published authority. Without it the
nearest eligible reference to origin/main is selected deterministically.
#>
[CmdletBinding()]
param(
    [ValidateSet("Status", "Alignment", "BaselineCandidate", "Evidence",
        "Verify", "Build")]
    [string]$Action = "Status",
    [string[]]$BookArguments = @(),
    [string]$OutputPath =
        "artifacts/book/technical-baseline-candidate.json",
    [string[]]$EvidenceProfiles = @("knowledge"),
    [string]$EvidenceOutputDirectory = "artifacts/knowledge/book",
    [switch]$AllowDirtyEvidence,
    [string]$ReferenceTag = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedBookRepository = "mpradovelasco/geocedg_book"
$ExpectedGeoCeDGRepository = "mpradovelasco/geocedg"
$FingerprintSchemaVersion = 2
$QuarantinedBookCommit = "e2e5676f39b59b757db109ffcc0edd9d84019d54"
$TechnicalBaselinesPath =
    "editorial/source-mapping/TECHNICAL_BASELINES.json"
# Typed technical references. A phase checkpoint is an annotated approval tag;
# a public release is an annotated version tag backed by a release record read
# from published GeoCeDG authority. Neither type is an editorial acceptance.
$PhaseCheckpointTagPattern = '^geocedg-[a-z0-9-]+-pass$'
$PublicReleaseTagPattern =
    '^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$'
$ReleaseRecordDirectory = "docs/releases"
$PackageProfilePath = "packaging/windows/package.yml"
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$BookLink = Join-Path $RepositoryRoot "book"
. (Join-Path $RepositoryRoot "tools\agent\repository-state.ps1")

function Invoke-RepositoryGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $lines = @(& git --no-optional-locks -C $Root @Arguments 2>&1 |
        ForEach-Object {
            $_.ToString()
        })
    $exitCode = $LASTEXITCODE
    if ($exitCode -notin $AllowedExitCodes) {
        $details = if ($lines.Count -gt 0) {
            "$([Environment]::NewLine)$($lines -join [Environment]::NewLine)"
        } else {
            ""
        }
        throw "git $($Arguments -join ' ') failed with exit code " +
            "${exitCode}.${details}"
    }
    return [pscustomobject]@{
        ExitCode = $exitCode
        Lines = @($lines)
        Text = ($lines -join "`n")
    }
}

function Get-NormalizedFullPath {
    param([Parameter(Mandatory)] [string]$Path)

    return [IO.Path]::GetFullPath($Path).TrimEnd("/", "\")
}

function Test-SameOrNestedPath {
    param(
        [Parameter(Mandatory)] [string]$Candidate,
        [Parameter(Mandatory)] [string]$Container
    )

    $candidatePath = Get-NormalizedFullPath -Path $Candidate
    $containerPath = Get-NormalizedFullPath -Path $Container
    if ($candidatePath.Equals(
            $containerPath, [StringComparison]::OrdinalIgnoreCase)) {
        return $true
    }
    return $candidatePath.StartsWith(
        $containerPath + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase)
}

function Test-PathsOverlap {
    param(
        [Parameter(Mandatory)] [string]$First,
        [Parameter(Mandatory)] [string]$Second
    )

    return (Test-SameOrNestedPath -Candidate $First -Container $Second) -or
        (Test-SameOrNestedPath -Candidate $Second -Container $First)
}

function Resolve-GitPath {
    param(
        [Parameter(Mandatory)] [string]$WorktreeRoot,
        [Parameter(Mandatory)] [string]$GitPath
    )

    if ([IO.Path]::IsPathRooted($GitPath)) {
        return Get-NormalizedFullPath -Path $GitPath
    }
    return Get-NormalizedFullPath -Path (Join-Path $WorktreeRoot $GitPath)
}

function ConvertTo-GitHubRepositoryName {
    param([Parameter(Mandatory)] [string]$Origin)

    $value = $Origin.Trim().TrimEnd("/")
    $patterns = @(
        '^https://github\.com/(?<owner>[^/]+)/(?<repository>[^/]+)$',
        '^git@github\.com:(?<owner>[^/]+)/(?<repository>[^/]+)$',
        '^ssh://git@github\.com/(?<owner>[^/]+)/(?<repository>[^/]+)$'
    )
    foreach ($pattern in $patterns) {
        $match = [regex]::Match(
            $value, $pattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)
        if ($match.Success) {
            $repository = $match.Groups["repository"].Value
            if ($repository.EndsWith(
                    ".git", [StringComparison]::OrdinalIgnoreCase)) {
                $repository = $repository.Substring(0, $repository.Length - 4)
            }
            return ("{0}/{1}" -f $match.Groups["owner"].Value,
                $repository).ToLowerInvariant()
        }
    }
    return ""
}

function Get-OptionalGitCommit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Revision
    )

    $result = Invoke-RepositoryGit -Root $Root `
        -Arguments @("rev-parse", "--verify", "${Revision}^{commit}") `
        -AllowedExitCodes @(0, 128)
    if ($result.ExitCode -ne 0) {
        return $null
    }
    $commit = $result.Text.Trim().ToLowerInvariant()
    if ($commit -notmatch '^[0-9a-f]{40}$') {
        throw "Git returned an invalid commit for ${Revision}: $commit"
    }
    return $commit
}

function Get-GitRepositoryState {
    param([Parameter(Mandatory)] [string]$Root)

    $commit = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("rev-parse", "--verify", "HEAD")).Text.Trim()
    if ($commit -notmatch '^[0-9a-fA-F]{40}$') {
        throw "Git returned an invalid HEAD commit for ${Root}: $commit"
    }

    $branchResult = Invoke-RepositoryGit -Root $Root `
        -Arguments @("symbolic-ref", "--quiet", "--short", "HEAD") `
        -AllowedExitCodes @(0, 1)
    $branch = if ($branchResult.ExitCode -eq 0) {
        $branchResult.Text.Trim()
    } else {
        "detached HEAD"
    }
    if ([string]::IsNullOrWhiteSpace($branch)) {
        throw "Git returned an empty branch name for $Root."
    }

    $origin = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("remote", "get-url", "origin")).Text.Trim()
    if ([string]::IsNullOrWhiteSpace($origin)) {
        throw "Git returned an empty origin URL for $Root."
    }

    $status = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("status", "--porcelain=v1", "--untracked-files=all")).Lines
    $mainCommit = Get-OptionalGitCommit -Root $Root `
        -Revision "refs/heads/main"
    $originMainCommit = Get-OptionalGitCommit -Root $Root `
        -Revision "refs/remotes/origin/main"
    $ahead = $null
    $behind = $null
    if ($null -ne $mainCommit -and $null -ne $originMainCommit) {
        $counts = (Invoke-RepositoryGit -Root $Root `
            -Arguments @("rev-list", "--left-right", "--count",
                "refs/heads/main...refs/remotes/origin/main")).Text.Trim() `
            -split '\s+'
        if ($counts.Count -ne 2 -or $counts[0] -notmatch '^\d+$' -or
                $counts[1] -notmatch '^\d+$') {
            throw "Git returned invalid main/origin-main counts for $Root."
        }
        $ahead = [int]$counts[0]
        $behind = [int]$counts[1]
    }
    return [pscustomobject]@{
        Branch = $branch
        Commit = $commit.ToLowerInvariant()
        Origin = $origin
        Status = @($status)
        MainCommit = $mainCommit
        OriginMainCommit = $originMainCommit
        Ahead = $ahead
        Behind = $behind
    }
}

function Write-GitRepositoryState {
    param(
        [Parameter(Mandatory)] [string]$Label,
        [Parameter(Mandatory)] [object]$State
    )

    Write-Host "${Label} branch: $($State.Branch)"
    Write-Host "${Label} HEAD: $($State.Commit)"
    Write-Host "${Label} main: $($State.MainCommit)"
    Write-Host "${Label} origin/main: $($State.OriginMainCommit)"
    if ($null -ne $State.Ahead -and $null -ne $State.Behind) {
        Write-Host ("${Label} main ahead/behind origin/main: " +
            "$($State.Ahead)/$($State.Behind)")
    } else {
        Write-Host "${Label} main ahead/behind origin/main: unavailable"
    }
    $repositoryName = ConvertTo-GitHubRepositoryName -Origin $State.Origin
    $safeOrigin = if ([string]::IsNullOrWhiteSpace($repositoryName)) {
        "unrecognized remote (URL redacted)"
    } else {
        $repositoryName
    }
    Write-Host "${Label} origin repository: $safeOrigin"
    if ($State.Status.Count -eq 0) {
        Write-Host "${Label} status: clean"
    } else {
        Write-Host "${Label} status: dirty"
        foreach ($line in $State.Status) {
            Write-Host "  $line"
        }
    }
}

function ConvertTo-CanonicalJson {
    param([Parameter(Mandatory)] [object]$Value)

    $json = $Value | ConvertTo-Json -Depth 100
    return $json.Replace("`r`n", "`n").Replace("`r", "`n") + "`n"
}

function Get-TextSha256 {
    param([Parameter(Mandatory)] [string]$Text)

    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($Text)
        return [Convert]::ToHexString(
            $sha.ComputeHash($bytes)).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Assert-NoReparsePathComponents {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )

    $rootPath = Get-NormalizedFullPath -Path $Root
    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not (Test-SameOrNestedPath -Candidate $absolute `
            -Container $rootPath)) {
        throw "$Description escapes its allowed root."
    }
    $relative = [IO.Path]::GetRelativePath($rootPath, $absolute)
    $cursor = $rootPath
    foreach ($component in @($relative -split '[\\/]')) {
        if ([string]::IsNullOrWhiteSpace($component) -or $component -eq '.') {
            continue
        }
        $cursor = Join-Path $cursor $component
        if (Test-Path -LiteralPath $cursor) {
            $item = Get-Item -LiteralPath $cursor -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "$Description contains a filesystem link: $cursor"
            }
        }
    }
}

function Assert-NoReparseAbsolutePathComponents {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )

    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $absolute -PathType Container)) {
        throw "$Description is unavailable."
    }
    $pathRoot = [IO.Path]::GetPathRoot($absolute)
    if ([string]::IsNullOrWhiteSpace($pathRoot) -or
            -not (Test-Path -LiteralPath $pathRoot -PathType Container)) {
        throw "$Description has no valid filesystem root."
    }
    $rootItem = Get-Item -LiteralPath $pathRoot -Force
    if ($rootItem.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "$Description contains a filesystem link or alias."
    }
    $relative = $absolute.Substring($pathRoot.Length)
    $cursor = $pathRoot
    foreach ($component in @($relative -split '[\\/]')) {
        if ([string]::IsNullOrWhiteSpace($component)) {
            continue
        }
        $cursor = Join-Path $cursor $component
        if (-not (Test-Path -LiteralPath $cursor)) {
            throw "$Description is unavailable."
        }
        $item = Get-Item -LiteralPath $cursor -Force
        if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "$Description contains a filesystem link or alias."
        }
    }
}

function Get-GitControlFileLine {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )

    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "$Description is unavailable."
    }
    Assert-NoReparseAbsolutePathComponents -Path (Split-Path -Parent $absolute) `
        -Description "$Description parent"
    $item = Get-Item -LiteralPath $absolute -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "$Description contains a filesystem link or alias."
    }
    $text = ConvertFrom-GeoCeDGStrictUtf8 `
        -Bytes ([IO.File]::ReadAllBytes($absolute))
    $match = [regex]::Match($text, '\A(?<line>[^\r\n]+)(?:\r?\n)?\z')
    if (-not $match.Success) {
        throw "$Description must contain exactly one line."
    }
    return $match.Groups['line'].Value
}

function Get-RawWorktreeGitMetadata {
    param(
        [Parameter(Mandatory)] [string]$WorktreeRoot,
        [Parameter(Mandatory)] [string]$Description
    )

    $dotGit = [IO.Path]::GetFullPath((Join-Path $WorktreeRoot '.git'))
    if (-not (Test-Path -LiteralPath $dotGit)) {
        throw "$Description .git control entry is unavailable."
    }
    $dotGitItem = Get-Item -LiteralPath $dotGit -Force
    if ($dotGitItem.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "$Description .git control entry contains a filesystem link or alias."
    }
    if ($dotGitItem -is [IO.DirectoryInfo]) {
        Assert-NoReparseAbsolutePathComponents -Path $dotGit `
            -Description "$Description Git directory"
        $gitDirectory = $dotGit
    } elseif ($dotGitItem -is [IO.FileInfo]) {
        $controlLine = Get-GitControlFileLine -Path $dotGit `
            -Description "$Description .git control file"
        $gitDirMatch = [regex]::Match($controlLine,
            '^gitdir:\s*(?<path>.+?)\s*$')
        if (-not $gitDirMatch.Success -or
                [string]::IsNullOrWhiteSpace(
                    $gitDirMatch.Groups['path'].Value)) {
            throw "$Description .git control file is invalid."
        }
        $gitDirectory = Resolve-GitPath -WorktreeRoot $WorktreeRoot `
            -GitPath $gitDirMatch.Groups['path'].Value
        Assert-NoReparseAbsolutePathComponents -Path $gitDirectory `
            -Description "$Description Git directory"
    } else {
        throw "$Description .git control entry is not a regular file or directory."
    }

    $commonControl = Join-Path $gitDirectory 'commondir'
    if (Test-Path -LiteralPath $commonControl) {
        $commonLine = (Get-GitControlFileLine -Path $commonControl `
                -Description "$Description commondir control file").Trim()
        if ([string]::IsNullOrWhiteSpace($commonLine)) {
            throw "$Description commondir control file is invalid."
        }
        $commonDirectory = Resolve-GitPath -WorktreeRoot $gitDirectory `
            -GitPath $commonLine
        Assert-NoReparseAbsolutePathComponents -Path $commonDirectory `
            -Description "$Description Git common directory"
    } else {
        $commonDirectory = $gitDirectory
    }

    return [pscustomobject]@{
        GitDirectory = Get-NormalizedFullPath -Path $gitDirectory
        CommonDirectory = Get-NormalizedFullPath -Path $commonDirectory
    }
}

function Resolve-GeoCeDGArtifactPath {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$RequiredSubtree,
        [switch]$RequireFileName
    )

    if ([string]::IsNullOrWhiteSpace($RelativePath) -or
            [IO.Path]::IsPathRooted($RelativePath) -or
            $RelativePath.Contains([char]0)) {
        throw "Artifact path must be a non-empty repository-relative path."
    }
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $RelativePath))
    $artifactRoot = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
            $RequiredSubtree))
    if (-not (Test-SameOrNestedPath -Candidate $absolute `
            -Container $artifactRoot)) {
        throw "Book operational evidence must remain under " +
            "${RequiredSubtree}/: $RelativePath"
    }
    if ($RequireFileName -and [string]::IsNullOrWhiteSpace(
            [IO.Path]::GetFileName($absolute))) {
        throw "Artifact output must name a file: $RelativePath"
    }
    Assert-NoReparsePathComponents -Root $RepositoryRoot -Path $absolute `
        -Description "Book operational evidence path"
    return $absolute
}

function Get-GitFileText {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [string]$RepositoryPath
    )

    return Get-GeoCeDGFrozenText -RepositoryRoot $Root -Path $RepositoryPath `
        -Commit $Commit
}

function Get-GitSourceRecord {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [string]$RepositoryPath
    )

    $result = Invoke-RepositoryGit -Root $Root `
        -Arguments @("rev-parse", "--verify", "${Commit}:$RepositoryPath") `
        -AllowedExitCodes @(0, 128)
    $objectId = if ($result.ExitCode -eq 0) {
        $result.Text.Trim().ToLowerInvariant()
    } else {
        $null
    }
    if ($null -ne $objectId -and $objectId -notmatch '^[0-9a-f]{40,64}$') {
        throw "Git returned an invalid object identifier for $RepositoryPath."
    }
    return [pscustomobject][ordered]@{
        path = $RepositoryPath
        git_object = $objectId
    }
}

function Get-NormativePhaseSnapshot {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    return Get-GeoCeDGPhaseSnapshot -RepositoryRoot $Root -Revision $Commit
}

function Get-FeatureStateSnapshot {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    $featurePaths = @(
        "geocedg/features/stable.yml",
        "geocedg/features/experimental.yml"
    )
    $features = [Collections.Generic.List[object]]::new()
    foreach ($path in $featurePaths) {
        $text = Get-GitFileText -Root $Root -Commit $Commit `
            -RepositoryPath $path
        try {
            $catalog = $text | ConvertFrom-Json -Depth 100 -NoEnumerate
        } catch {
            throw "Published feature authority is invalid at ${Commit}:$path."
        }
        foreach ($feature in @($catalog.features)) {
            $dependencies = @($feature.depends_on | ForEach-Object {
                    [string]$_
                } | Sort-Object -CaseSensitive)
            $features.Add([pscustomobject][ordered]@{
                    id = [string]$feature.id
                    maturity = [string]$feature.maturity
                    enabled_by_default = [bool]$feature.enabled_by_default
                    specification = [string]$feature.specification
                    depends_on = $dependencies
                    source = $path
                })
        }
    }
    return @($features | Sort-Object -Property id -CaseSensitive)
}

function Get-ApplicationProfileSnapshot {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    $profilePath = "apps/geocedg/application-profile.yml"
    $text = Get-GitFileText -Root $Root -Commit $Commit `
        -RepositoryPath $profilePath
    # Hashtable keys are case-sensitive; localized_text may hold keys that
    # differ only by case, which a PSCustomObject cannot represent.
    try {
        $profile = $text | ConvertFrom-Json -Depth 100 -AsHashtable
    } catch {
        throw "Published application profile is invalid at $Commit."
    }
    if ($profile -isnot [Collections.IDictionary] -or
            -not $profile.Contains('schema_version') -or
            -not $profile.Contains('profile_id') -or
            -not $profile.Contains('features') -or
            -not $profile.Contains('serialization') -or
            $profile['serialization'] -isnot [Collections.IDictionary] -or
            -not $profile['serialization'].Contains('app_code') -or
            -not $profile['serialization'].Contains('policy')) {
        throw "Published application profile is invalid at $Commit."
    }
    $schemaVersion = [int]$profile.schema_version
    # Schema 1 lists enabled feature identifiers; schema 2 lists feature
    # bindings whose enabled_by_default flag selects the enabled ones.
    $enabledFeatures = @(switch ($schemaVersion) {
        1 {
            $profile.features | ForEach-Object { [string]$_ }
        }
        2 {
            $profile.features | ForEach-Object {
                if ($_ -isnot [Collections.IDictionary] -or
                        -not $_.Contains('id') -or
                        $_['enabled_by_default'] -isnot [bool]) {
                    throw "Published application profile is invalid at $Commit."
                }
                if ($_['enabled_by_default']) { [string]$_['id'] }
            }
        }
        default {
            throw "Unsupported application profile schema at ${Commit}: $schemaVersion"
        }
    })
    return [pscustomobject][ordered]@{
        source = $profilePath
        schema_version = $schemaVersion
        profile_id = [string]$profile.profile_id
        serialization_app_code = [string]$profile.serialization.app_code
        serialization_policy = [string]$profile.serialization.policy
        enabled_features = @($enabledFeatures | Sort-Object -CaseSensitive)
    }
}

function Get-BundleProvenanceSnapshot {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    $profilesPath =
        "geocedg/specs/operations/knowledge-bundle-profiles.json"
    $text = Get-GitFileText -Root $Root -Commit $Commit `
        -RepositoryPath $profilesPath
    try {
        $catalog = $text | ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "Published G9O1 profile authority is invalid at $Commit."
    }
    $profiles = @($catalog.profiles | ForEach-Object {
            [pscustomobject][ordered]@{
                id = [string]$_.id
                kind = [string]$_.kind
            }
        } | Sort-Object -Property id -CaseSensitive)
    return [pscustomobject][ordered]@{
        source = $profilesPath
        schema_version = [int]$catalog.schema_version
        status = [string]$catalog.status
        implementation_status = [string]$catalog.implementation_status
        profiles = $profiles
    }
}

function Get-TechnicalAuthoritySnapshot {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    $categoryDefinitions = @(
        [ordered]@{
            category = "phase_status"
            paths = @("docs/roadmap/geocedg_roadmap.md")
        },
        [ordered]@{
            category = "feature_public_surface"
            paths = @("geocedg/features", "apps/geocedg/application-profile.yml")
        },
        [ordered]@{
            category = "kernel_api_semantics"
            paths = @("source/shared/common", "source/shared/common-jre")
        },
        [ordered]@{
            category = "command_public_api"
            paths = @(
                "source/shared/common/src/main/java/org/geogebra/common/kernel/commands",
                "source/shared/common/src/main/java/org/geocedg/common",
                "geocedg/specs/commands")
        },
        [ordered]@{
            category = "persistence_compatibility"
            paths = @(
                "source/shared/common/src/main/java/org/geogebra/common/io",
                "source/desktop/desktop/src/main/java/org/geogebra/desktop/io",
                "geocedg/specs/ui/native-document-identity.md",
                "docs/adr/0016-native-geocedg-document-identity.md")
        },
        [ordered]@{
            category = "gui_workflow"
            paths = @(
                "apps/geocedg",
                "source/desktop/desktop/src/main/java/org/geocedg",
                "source/desktop/desktop/src/main/java/org/geogebra/desktop/gui",
                "docs/user")
        },
        [ordered]@{
            category = "validation_model_evidence"
            paths = @("geocedg/validation", "docs/validation", "models")
        },
        [ordered]@{
            category = "technical_specs_architecture"
            paths = @("geocedg/specs", "docs/architecture", "docs/adr")
        },
        [ordered]@{
            category = "bundle_source_provenance"
            paths = @(
                "geocedg/specs/operations/knowledge-bundle-profiles.json",
                "geocedg/specs/operations/knowledge-bundle.schema.json",
                "tools/knowledge", "docs/references/cedg", "models/manifests")
        },
        [ordered]@{
            category = "packaging_export_workflow"
            paths = @("packaging", "tools/release")
        }
    )

    $categories = [Collections.Generic.List[object]]::new()
    foreach ($definition in $categoryDefinitions) {
        $records = @($definition.paths | ForEach-Object {
                Get-GitSourceRecord -Root $Root -Commit $Commit `
                    -RepositoryPath $_
            })
        $categoryBasis = [ordered]@{
            fingerprint_schema_version = $FingerprintSchemaVersion
            category = $definition.category
            sources = $records
        }
        $categories.Add([pscustomobject][ordered]@{
                category = $definition.category
                fingerprint = Get-TextSha256 -Text (
                    ConvertTo-CanonicalJson -Value $categoryBasis)
                sources = $records
            })
    }
    $categoryArray = @($categories)
    $fingerprintBasis = [ordered]@{
        fingerprint_schema_version = $FingerprintSchemaVersion
        categories = @($categoryArray | ForEach-Object {
                [ordered]@{
                    category = $_.category
                    fingerprint = $_.fingerprint
                }
            })
    }
    $sourceRecords = @($categoryArray.sources | Sort-Object -Property path `
            -Unique -CaseSensitive)
    return [pscustomobject][ordered]@{
        commit = $Commit
        fingerprint_schema_version = $FingerprintSchemaVersion
        technical_authority_fingerprint = Get-TextSha256 -Text (
            ConvertTo-CanonicalJson -Value $fingerprintBasis)
        normative_phase_status = Get-NormativePhaseSnapshot -Root $Root `
            -Commit $Commit
        feature_state = @(Get-FeatureStateSnapshot -Root $Root -Commit $Commit)
        application_profile = Get-ApplicationProfileSnapshot -Root $Root `
            -Commit $Commit
        bundle_provenance = Get-BundleProvenanceSnapshot -Root $Root `
            -Commit $Commit
        authority_categories = $categoryArray
        source_records = $sourceRecords
    }
}

function Get-ChangedAuthorityCategories {
    param(
        [Parameter(Mandatory)] [object]$AcceptedSnapshot,
        [Parameter(Mandatory)] [object]$PublishedSnapshot
    )

    $accepted = @{}
    foreach ($category in @($AcceptedSnapshot.authority_categories)) {
        $accepted[[string]$category.category] = [string]$category.fingerprint
    }
    $changed = [Collections.Generic.List[string]]::new()
    foreach ($category in @($PublishedSnapshot.authority_categories)) {
        $name = [string]$category.category
        if (-not $accepted.ContainsKey($name) -or
                $accepted[$name] -ne [string]$category.fingerprint) {
            $changed.Add($name)
        }
    }
    return @($changed | Sort-Object -CaseSensitive)
}

function Test-GitAncestor {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Ancestor,
        [Parameter(Mandatory)] [string]$Descendant
    )

    $result = Invoke-RepositoryGit -Root $Root `
        -Arguments @("merge-base", "--is-ancestor", $Ancestor, $Descendant) `
        -AllowedExitCodes @(0, 1)
    return $result.ExitCode -eq 0
}

function ConvertTo-OrderedData {
    param([AllowNull()] [object]$Value)

    if ($null -eq $Value) {
        return $null
    }
    if ($Value -is [Collections.IDictionary]) {
        $ordered = [ordered]@{}
        foreach ($key in @($Value.Keys | ForEach-Object { [string]$_ } |
                Sort-Object -CaseSensitive)) {
            $ordered[$key] = ConvertTo-OrderedData -Value $Value[$key]
        }
        return $ordered
    }
    if ($Value -is [string] -or $Value -is [ValueType]) {
        return $Value
    }
    if ($Value -is [Collections.IEnumerable]) {
        return , @($Value | ForEach-Object { ConvertTo-OrderedData -Value $_ })
    }
    return $Value
}

function Get-TagIdentity {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$TagName
    )

    $tagRef = "refs/tags/$TagName"
    $typeResult = Invoke-RepositoryGit -Root $Root `
        -Arguments @("cat-file", "-t", $tagRef) -AllowedExitCodes @(0, 128)
    if ($typeResult.ExitCode -ne 0) {
        return $null
    }
    $objectType = $typeResult.Text.Trim()
    $tagObject = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("rev-parse", "--verify", $tagRef)
    ).Text.Trim().ToLowerInvariant()
    $commit = Get-OptionalGitCommit -Root $Root -Revision $tagRef
    if ($null -eq $commit -or $tagObject -notmatch '^[0-9a-f]{40,64}$') {
        throw "TECHNICAL CONTRADICTION: tag $TagName does not resolve to a commit."
    }
    $tree = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("rev-parse", "--verify", "${commit}^{tree}")
    ).Text.Trim().ToLowerInvariant()
    if ($tree -notmatch '^[0-9a-f]{40,64}$') {
        throw "TECHNICAL CONTRADICTION: tag $TagName has no valid tree identity."
    }
    if ($objectType -eq "tag") {
        $body = Invoke-RepositoryGit -Root $Root `
            -Arguments @("cat-file", "-p", $tagRef)
        foreach ($line in $body.Lines) {
            if ($line -cmatch '^Commit:\s*([0-9a-fA-F]{40,64})\s*$' -and
                    $Matches[1].ToLowerInvariant() -cne $commit) {
                throw "TECHNICAL CONTRADICTION: the message of tag $TagName " +
                    "declares a commit that the tag does not resolve to."
            }
            if ($line -cmatch '^Tree:\s*([0-9a-fA-F]{40,64})\s*$' -and
                    $Matches[1].ToLowerInvariant() -cne $tree) {
                throw "TECHNICAL CONTRADICTION: the message of tag $TagName " +
                    "declares a tree that its commit does not have."
            }
        }
    }
    return [pscustomobject][ordered]@{
        name = $TagName
        annotated = [bool]($objectType -eq "tag")
        tag_object = $tagObject
        commit = $commit
        tree = $tree
    }
}

function Assert-ReleaseRecordKeys {
    param(
        [AllowNull()] [object]$Table,
        [Parameter(Mandatory)] [string[]]$Required,
        [string[]]$Optional = @(),
        [Parameter(Mandatory)] [string]$Where
    )

    if ($Table -isnot [Collections.IDictionary]) {
        throw "TECHNICAL CONTRADICTION: release record $Where is not an object."
    }
    foreach ($key in $Required) {
        if (-not $Table.Contains($key)) {
            throw "TECHNICAL CONTRADICTION: release record $Where lacks '$key'."
        }
    }
    foreach ($key in @($Table.Keys)) {
        if ([string]$key -cnotin ($Required + $Optional)) {
            throw "TECHNICAL CONTRADICTION: release record $Where has the " +
                "unknown property '$key'."
        }
    }
}

function Get-ReleaseRecordEvaluation {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$AuthorityCommit,
        [Parameter(Mandatory)] [object]$Identity
    )

    $tagName = [string]$Identity.name
    $path = "$ReleaseRecordDirectory/$tagName.release.json"
    $source = Get-GitSourceRecord -Root $Root -Commit $AuthorityCommit `
        -RepositoryPath $path
    if ($null -eq $source.git_object) {
        return [pscustomobject]@{
            State = "MISSING"
            Reason = "VERSION_TAG_WITHOUT_RELEASE_RECORD"
            Record = $null
            Source = $source
        }
    }
    try {
        $record = Get-GitFileText -Root $Root -Commit $AuthorityCommit `
            -RepositoryPath $path | ConvertFrom-Json -Depth 100 -AsHashtable
    } catch {
        throw "TECHNICAL CONTRADICTION: release record $path is not valid JSON."
    }
    Assert-ReleaseRecordKeys -Table $record -Where $path -Required @(
        "schema_version", "kind", "version", "version_date", "tag", "commit",
        "tree", "distribution", "publication_contrast",
        "author_authorization", "verification", "limits") -Optional @(
        "predecessor_release", "nature", "inherited_evidence")
    Assert-ReleaseRecordKeys -Table $record["tag"] -Where "$path tag" `
        -Required @("name", "object", "annotated", "signed")
    Assert-ReleaseRecordKeys -Table $record["distribution"] `
        -Where "$path distribution" -Required @(
        "profile", "platform", "release_url", "published_at", "assets")
    Assert-ReleaseRecordKeys -Table $record["publication_contrast"] `
        -Where "$path publication_contrast" `
        -Required @("status", "date", "method") -Optional @("scope")
    Assert-ReleaseRecordKeys -Table $record["author_authorization"] `
        -Where "$path author_authorization" `
        -Required @("date", "kind", "record")
    Assert-ReleaseRecordKeys -Table $record["verification"] `
        -Where "$path verification" -Required @(
        "accepted_runs", "not_executed_on_release_commit",
        "not_established_on_release_commit", "final_receipt",
        "evidence_class")
    foreach ($run in @($record["verification"]["accepted_runs"])) {
        Assert-ReleaseRecordKeys -Table $run -Where "$path accepted run" `
            -Required @("profile", "run_id", "acceptance", "coverage",
            "result_hash")
    }
    foreach ($asset in @($record["distribution"]["assets"])) {
        Assert-ReleaseRecordKeys -Table $asset -Where "$path asset" `
            -Required @("name", "size", "sha256")
        if ([string]$asset["sha256"] -cnotmatch '^[0-9a-f]{64}$') {
            throw "TECHNICAL CONTRADICTION: release record $path has an " +
                "invalid asset digest."
        }
    }
    foreach ($limit in @($record["limits"])) {
        Assert-ReleaseRecordKeys -Table $limit -Where "$path limit" `
            -Required @("id", "statement_ref")
    }

    $versionMatch = [regex]::Match($tagName, '^v(?<version>.+)$')
    $expected = [ordered]@{
        "schema_version" = ($record["schema_version"] -eq 1)
        "kind" = ([string]$record["kind"] -ceq
            "GEOCEDG_PUBLIC_RELEASE_RECORD")
        "version" = ([string]$record["version"] -ceq
            $versionMatch.Groups["version"].Value)
        "tag.name" = ([string]$record["tag"]["name"] -ceq $tagName)
        "tag.object" = ([string]$record["tag"]["object"] -ceq
            [string]$Identity.tag_object)
        "tag.annotated" = ($record["tag"]["annotated"] -is [bool] -and
            $record["tag"]["annotated"] -and [bool]$Identity.annotated)
        "commit" = ([string]$record["commit"] -ceq [string]$Identity.commit)
        "tree" = ([string]$record["tree"] -ceq [string]$Identity.tree)
    }
    foreach ($field in $expected.Keys) {
        if (-not $expected[$field]) {
            throw "TECHNICAL CONTRADICTION: release record $path contradicts " +
                "Git identity or its contract in '$field'."
        }
    }
    try {
        $package = Get-GitFileText -Root $Root -Commit $Identity.commit `
            -RepositoryPath $PackageProfilePath |
            ConvertFrom-Json -Depth 100 -AsHashtable
        $packageVersion = [string]$package["application"]["version"]
        $packageDate = [string]$package["application"]["version_date"]
    } catch {
        throw "TECHNICAL CONTRADICTION: the package profile is unavailable " +
            "or invalid at the commit of tag $tagName."
    }
    if ($packageVersion -cne [string]$record["version"] -or
            $packageDate -cne [string]$record["version_date"]) {
        throw "TECHNICAL CONTRADICTION: the package profile at tag $tagName " +
            "declares a version or version date other than its release record."
    }

    $sufficient = [string]$record["publication_contrast"]["status"] -ceq
        "CONTRASTED_WITH_GITHUB" -and
        -not [string]::IsNullOrWhiteSpace(
            [string]$record["distribution"]["release_url"]) -and
        -not [string]::IsNullOrWhiteSpace(
            [string]$record["distribution"]["published_at"]) -and
        -not [string]::IsNullOrWhiteSpace(
            [string]$record["author_authorization"]["record"])
    return [pscustomobject]@{
        State = if ($sufficient) { "VALID" } else { "INSUFFICIENT" }
        Reason = if ($sufficient) {
            $null
        } else {
            "INSUFFICIENT_PUBLICATION_PROVENANCE"
        }
        Record = $record
        Source = $source
    }
}

function Get-ReachableTechnicalReferences {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Commit
    )

    $eligible = [Collections.Generic.List[object]]::new()
    $ineligible = [Collections.Generic.List[object]]::new()
    $tagNames = (Invoke-RepositoryGit -Root $Root `
        -Arguments @("tag", "--merged", $Commit, "--list",
            "geocedg-*-pass", "v[0-9]*")
    ).Lines | Where-Object {
        -not [string]::IsNullOrWhiteSpace($_)
    } | Sort-Object -CaseSensitive
    foreach ($tagName in $tagNames) {
        $type = if ($tagName -cmatch $PhaseCheckpointTagPattern) {
            "PHASE_CHECKPOINT"
        } elseif ($tagName -cmatch $PublicReleaseTagPattern) {
            "PUBLIC_RELEASE"
        } else {
            continue
        }
        $identity = Get-TagIdentity -Root $Root -TagName $tagName
        if ($null -eq $identity) {
            continue
        }
        if (-not $identity.annotated) {
            $ineligible.Add([pscustomobject][ordered]@{
                    tag = $tagName
                    type = $type
                    reason = "LIGHTWEIGHT_TAG"
                })
            continue
        }
        $distance = (Invoke-RepositoryGit -Root $Root `
            -Arguments @("rev-list", "--count",
                "$($identity.commit)..${Commit}")).Text.Trim()
        if ($distance -notmatch '^\d+$') {
            throw "Invalid annotated tag provenance: $tagName"
        }
        $release = $null
        if ($type -eq "PUBLIC_RELEASE") {
            $evaluation = Get-ReleaseRecordEvaluation -Root $Root `
                -AuthorityCommit $Commit -Identity $identity
            if ($evaluation.State -ne "VALID") {
                $ineligible.Add([pscustomobject][ordered]@{
                        tag = $tagName
                        type = $type
                        reason = $evaluation.Reason
                    })
                continue
            }
            $release = $evaluation
        }
        $eligible.Add([pscustomobject][ordered]@{
                type = $type
                id = "${type}:$tagName"
                name = $tagName
                tag_object = $identity.tag_object
                commit = $identity.commit
                tree = $identity.tree
                commits_to_reference = [int]$distance
                type_rank = if ($type -eq "PUBLIC_RELEASE") { 0 } else { 1 }
                release = $release
            })
    }
    return [pscustomobject]@{
        Eligible = @($eligible | Sort-Object -Property `
                @{ Expression = 'commits_to_reference'; Ascending = $true },
                @{ Expression = 'type_rank'; Ascending = $true },
                @{ Expression = 'name'; Ascending = $true })
        Ineligible = @($ineligible | Sort-Object -Property tag -CaseSensitive)
    }
}

function Get-TechnicalBaselineCandidate {
    param(
        [Parameter(Mandatory)] [object]$GeoCeDGState,
        [string]$SelectedReferenceTag = ""
    )

    $repositoryName = ConvertTo-GitHubRepositoryName `
        -Origin $GeoCeDGState.Origin
    if (-not $repositoryName.Equals(
            $ExpectedGeoCeDGRepository,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Unexpected GeoCeDG origin identity; expected " +
            "$ExpectedGeoCeDGRepository. The URL was redacted."
    }
    if ($null -eq $GeoCeDGState.OriginMainCommit) {
        throw "GeoCeDG origin/main is unavailable; published authority is missing."
    }

    $referenceCommit = $GeoCeDGState.OriginMainCommit
    $references = Get-ReachableTechnicalReferences `
        -Root $RepositoryRoot -Commit $referenceCommit
    $eligible = @($references.Eligible)
    if ([string]::IsNullOrWhiteSpace($SelectedReferenceTag)) {
        if ($eligible.Count -eq 0) {
            throw "No eligible GeoCeDG technical reference is reachable from " +
                "origin/main: an annotated phase-checkpoint tag or an " +
                "annotated version tag with a valid release record is required."
        }
        $releases = @($eligible | Where-Object { $_.type -eq "PUBLIC_RELEASE" })
        for ($index = 1; $index -lt $releases.Count; $index++) {
            $nearer = [version]$releases[$index - 1].name.Substring(1)
            $farther = [version]$releases[$index].name.Substring(1)
            if ($releases[$index - 1].commit -ne $releases[$index].commit -and
                    $nearer -le $farther) {
                throw "VERSION_ORDER_ANOMALY: public release " +
                    "$($releases[$index - 1].name) is nearer to origin/main " +
                    "than $($releases[$index].name); select one explicitly " +
                    "with -ReferenceTag."
            }
        }
        $selected = $eligible[0]
    } else {
        $selected = @($eligible | Where-Object {
                $_.name -ceq $SelectedReferenceTag
            }) | Select-Object -First 1
        if ($null -eq $selected) {
            $rejected = @($references.Ineligible | Where-Object {
                    $_.tag -ceq $SelectedReferenceTag
                }) | Select-Object -First 1
            $reason = if ($null -eq $rejected) {
                "REFERENCE_NOT_FOUND"
            } else {
                $rejected.reason
            }
            throw "The requested reference tag '$SelectedReferenceTag' is not " +
                "an eligible technical reference reachable from origin/main: " +
                "$reason."
        }
    }
    $publishedCommit = $selected.commit
    $distance = $selected.commits_to_reference
    $snapshot = Get-TechnicalAuthoritySnapshot -Root $RepositoryRoot `
        -Commit $publishedCommit
    $mainChangedCategories = @()
    if ($distance -gt 0) {
        try {
            $mainSnapshot = Get-TechnicalAuthoritySnapshot `
                -Root $RepositoryRoot -Commit $referenceCommit
            $mainChangedCategories = @(Get-ChangedAuthorityCategories `
                    -AcceptedSnapshot $snapshot -PublishedSnapshot $mainSnapshot)
        } catch {
            $mainChangedCategories = $null
        }
    }

    $releaseBlock = $null
    if ($null -ne $selected.release) {
        $record = $selected.release.Record
        $releaseBlock = [ordered]@{
            record = [ordered]@{
                path = $selected.release.Source.path
                git_object = $selected.release.Source.git_object
                authority_commit = $referenceCommit
            }
            bridge_check = "RECORD_COHERENT_WITH_GIT_OBJECTS — PUBLIC " +
                "AVAILABILITY NOT RECHECKED BY THE OFFLINE BRIDGE"
            version = [string]$record["version"]
            version_date = [string]$record["version_date"]
            distribution = [ordered]@{
                profile = [string]$record["distribution"]["profile"]
                platform = [string]$record["distribution"]["platform"]
                release_url = [string]$record["distribution"]["release_url"]
                published_at = [string]$record["distribution"]["published_at"]
            }
            publication_contrast = ConvertTo-OrderedData `
                -Value $record["publication_contrast"]
            author_authorization = ConvertTo-OrderedData `
                -Value $record["author_authorization"]
            verification = ConvertTo-OrderedData -Value $record["verification"]
            limits = ConvertTo-OrderedData -Value $record["limits"]
        }
    }

    $commitTimestamp = (Invoke-RepositoryGit -Root $RepositoryRoot `
        -Arguments @("show", "-s", "--format=%cI",
            $publishedCommit)).Text.Trim()
    return [pscustomobject][ordered]@{
        schema_version = 2
        kind = "GEOCEDG_BOOK_TECHNICAL_BASELINE_CANDIDATE"
        status = "REVIEW_CANDIDATE_ONLY"
        repository = $ExpectedGeoCeDGRepository
        technical_source = [ordered]@{
            authority_ref = "refs/remotes/origin/main"
            commit = $referenceCommit
        }
        published_reference = [ordered]@{
            type = $selected.type
            id = $selected.id
            tag = $selected.name
            tag_object = $selected.tag_object
            commit = $publishedCommit
            tree = $selected.tree
            commit_timestamp = $commitTimestamp
            commits_to_origin_main = $distance
        }
        release = $releaseBlock
        coincident_references = @($eligible | Where-Object {
                $_.commit -eq $publishedCommit -and $_.name -cne $selected.name
            } | Sort-Object -Property name -CaseSensitive | ForEach-Object {
                [ordered]@{
                    type = $_.type
                    id = $_.id
                    tag = $_.name
                    tag_object = $_.tag_object
                }
            })
        main_delta = [ordered]@{
            commits = $distance
            changed_authority_categories = $mainChangedCategories
        }
        technical_authority_fingerprint =
            $snapshot.technical_authority_fingerprint
        fingerprint_schema_version = $FingerprintSchemaVersion
        normative_phase_status = $snapshot.normative_phase_status
        feature_state = $snapshot.feature_state
        application_profile = $snapshot.application_profile
        bundle_provenance = $snapshot.bundle_provenance
        authority_categories = $snapshot.authority_categories
        reachable_references = @($eligible |
            Sort-Object -Property name -CaseSensitive | ForEach-Object {
                [ordered]@{
                    type = $_.type
                    id = $_.id
                    tag = $_.name
                    tag_object = $_.tag_object
                    commit = $_.commit
                    tree = $_.tree
                    commits_to_reference = $_.commits_to_reference
                }
            })
        ineligible_tags = @($references.Ineligible)
        source_records = $snapshot.source_records
    }
}

function Write-TechnicalBaselineCandidate {
    param(
        [Parameter(Mandatory)] [object]$Candidate,
        [Parameter(Mandatory)] [string]$RelativeOutputPath
    )

    $absolute = Resolve-GeoCeDGArtifactPath `
        -RelativePath $RelativeOutputPath -RequiredSubtree "artifacts/book" `
        -RequireFileName
    $repositoryPath = [IO.Path]::GetRelativePath(
        $RepositoryRoot, $absolute).Replace("\", "/")
    $tracked = Invoke-RepositoryGit -Root $RepositoryRoot `
        -Arguments @("ls-files", "--", $repositoryPath)
    if (-not [string]::IsNullOrWhiteSpace($tracked.Text)) {
        throw "BaselineCandidate refuses to overwrite a tracked file: " +
            $repositoryPath
    }
    $parent = Split-Path -Parent $absolute
    [void](New-Item -ItemType Directory -Path $parent -Force)
    $json = ConvertTo-CanonicalJson -Value $Candidate
    [IO.File]::WriteAllText(
        $absolute, $json, [Text.UTF8Encoding]::new($false))
    return $absolute
}

function New-AlignmentResult {
    param(
        [Parameter(Mandatory)] [string]$Classification,
        [Parameter(Mandatory)] [string]$Message,
        [Parameter(Mandatory)] [object]$Candidate,
        [AllowNull()] [object]$Registry,
        [AllowNull()] [object]$CurrentBaseline,
        [AllowNull()] [string]$BookAuthorityCommit,
        [string[]]$ChangedCategories = @()
    )

    return [pscustomobject][ordered]@{
        classification = $Classification
        message = $Message
        published_tag = $Candidate.published_reference.tag
        published_commit = $Candidate.published_reference.commit
        published_fingerprint =
            $Candidate.technical_authority_fingerprint
        current_editorial_baseline = $CurrentBaseline
        book_authority_ref = "refs/remotes/origin/main"
        book_authority_commit = $BookAuthorityCommit
        changed_authority_categories = @($ChangedCategories)
        historical_phase_baselines = if ($null -eq $Registry) {
            @()
        } else {
            @($Registry.historical_phase_baselines)
        }
    }
}

function Get-BookTechnicalAlignment {
    param(
        [Parameter(Mandatory)] [string]$BookRoot,
        [Parameter(Mandatory)] [object]$Candidate
    )

    $bookAuthorityCommit = Get-OptionalGitCommit -Root $BookRoot `
        -Revision "refs/remotes/origin/main"
    if ($null -eq $bookAuthorityCommit) {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "The published book origin/main authority is absent." `
            -Candidate $Candidate -Registry $null -CurrentBaseline $null `
            -BookAuthorityCommit $null
    }
    $registryObject = Invoke-RepositoryGit -Root $BookRoot `
        -Arguments @("cat-file", "-e",
            "${bookAuthorityCommit}:$TechnicalBaselinesPath") `
        -AllowedExitCodes @(0, 128)
    if ($registryObject.ExitCode -ne 0) {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "Published book origin/main has no accepted technical-baseline registry." `
            -Candidate $Candidate -Registry $null -CurrentBaseline $null `
            -BookAuthorityCommit $bookAuthorityCommit
    }
    try {
        $registry = Get-GitFileText -Root $BookRoot `
            -Commit $bookAuthorityCommit -RepositoryPath $TechnicalBaselinesPath |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The published book technical-baseline registry is invalid JSON." `
            -Candidate $Candidate -Registry $null -CurrentBaseline $null `
            -BookAuthorityCommit $bookAuthorityCommit
    }
    $authorityProperty = $registry.PSObject.Properties['authority']
    $validAuthority = $null -ne $authorityProperty -and
        [string]$authorityProperty.Value.repository -eq $ExpectedBookRepository -and
        [string]$authorityProperty.Value.policy -eq
        "editorial/source-mapping/TECHNICAL_BASELINE_POLICY.md"
    $registrySchema = $registry.schema_version
    if (($registrySchema -ne 1 -and $registrySchema -ne 2) -or
            -not $validAuthority) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The published book registry schema or authority is invalid." `
            -Candidate $Candidate -Registry $registry -CurrentBaseline $null `
            -BookAuthorityCommit $bookAuthorityCommit
    }
    if ($null -eq $registry.current_editorial_technical_baseline) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The registry has no current editorial baseline field." `
            -Candidate $Candidate -Registry $registry -CurrentBaseline $null `
            -BookAuthorityCommit $bookAuthorityCommit
    }
    $current = $registry.current_editorial_technical_baseline
    $status = [string]$current.status
    if ($status -eq "NOT_YET_REFRESHED") {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "Current editorial technical baseline: NOT YET REFRESHED." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    if ($status -ne "ACCEPTED") {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "Unsupported current editorial baseline status: $status" `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }

    $acceptedCommit = [string]$current.geocedg_commit
    $acceptedFingerprint = [string]$current.technical_authority_fingerprint
    $acceptedTag = [string]$current.published_tag
    $acceptedSchemaProperty =
        $current.PSObject.Properties['fingerprint_schema_version']
    $acceptedSchema = if ($null -eq $acceptedSchemaProperty) {
        $null
    } else {
        $acceptedSchemaProperty.Value
    }
    # Ledger schema 1 has no reference type: its records are phase
    # checkpoints. Schema 2 declares the type explicitly.
    $typeProperty = $current.PSObject.Properties['reference_type']
    $acceptedType = if ($registrySchema -eq 1 -and $null -eq $typeProperty) {
        "PHASE_CHECKPOINT"
    } elseif ($registrySchema -eq 2 -and $null -ne $typeProperty) {
        [string]$typeProperty.Value
    } else {
        ""
    }
    $acceptedTagPattern = switch -CaseSensitive ($acceptedType) {
        "PHASE_CHECKPOINT" { $PhaseCheckpointTagPattern }
        "PUBLIC_RELEASE" { $PublicReleaseTagPattern }
        default { '(?!)' }
    }
    $tagObjectProperty = $current.PSObject.Properties['tag_object']
    $treeProperty = $current.PSObject.Properties['geocedg_tree']
    $acceptedTagObject = if ($null -eq $tagObjectProperty) {
        $null
    } else {
        [string]$tagObjectProperty.Value
    }
    $acceptedTree = if ($null -eq $treeProperty) {
        $null
    } else {
        [string]$treeProperty.Value
    }
    $identityPinsMalformed =
        ($acceptedType -eq "PUBLIC_RELEASE" -and
            ($null -eq $acceptedTagObject -or $null -eq $acceptedTree)) -or
        ($null -ne $acceptedTagObject -and
            $acceptedTagObject -cnotmatch '^[0-9a-f]{40,64}$') -or
        ($null -ne $acceptedTree -and
            $acceptedTree -cnotmatch '^[0-9a-f]{40,64}$')
    if ($acceptedCommit -notmatch '^[0-9a-fA-F]{40}$' -or
            $acceptedFingerprint -notmatch '^[0-9a-fA-F]{64}$' -or
            $acceptedTag -cnotmatch $acceptedTagPattern -or
            $identityPinsMalformed -or
            $acceptedSchema -ne $FingerprintSchemaVersion) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "Accepted editorial baseline identifiers are malformed." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    $acceptedCommit = $acceptedCommit.ToLowerInvariant()
    $acceptedFingerprint = $acceptedFingerprint.ToLowerInvariant()
    $exists = Invoke-RepositoryGit -Root $RepositoryRoot `
        -Arguments @("cat-file", "-e", "${acceptedCommit}^{commit}") `
        -AllowedExitCodes @(0, 128)
    if ($exists.ExitCode -ne 0) {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "The accepted GeoCeDG commit is unavailable locally." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }

    $tagRef = "refs/tags/$acceptedTag"
    $tagType = Invoke-RepositoryGit -Root $RepositoryRoot `
        -Arguments @("cat-file", "-t", $tagRef) -AllowedExitCodes @(0, 128)
    if ($tagType.ExitCode -ne 0) {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "The accepted annotated GeoCeDG pass tag is unavailable." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    $tagCommit = Get-OptionalGitCommit -Root $RepositoryRoot -Revision $tagRef
    if ($tagType.Text.Trim() -ne "tag" -or $tagCommit -ne $acceptedCommit) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The accepted pass tag does not annotate the accepted commit." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    try {
        $acceptedIdentity = Get-TagIdentity -Root $RepositoryRoot `
            -TagName $acceptedTag
    } catch {
        $acceptedIdentity = $null
    }
    if ($null -eq $acceptedIdentity -or
            ($null -ne $acceptedTagObject -and
                $acceptedTagObject -cne $acceptedIdentity.tag_object) -or
            ($null -ne $acceptedTree -and
                $acceptedTree -cne $acceptedIdentity.tree)) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The accepted tag object or tree contradicts the recorded identity." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    if ($acceptedType -eq "PUBLIC_RELEASE") {
        try {
            $acceptedRelease = Get-ReleaseRecordEvaluation `
                -Root $RepositoryRoot `
                -AuthorityCommit $Candidate.technical_source.commit `
                -Identity $acceptedIdentity
        } catch {
            return New-AlignmentResult `
                -Classification "TECHNICAL CONTRADICTION" `
                -Message "The release record of the accepted public release is contradictory." `
                -Candidate $Candidate -Registry $registry `
                -CurrentBaseline $current `
                -BookAuthorityCommit $bookAuthorityCommit
        }
        if ($acceptedRelease.State -ne "VALID") {
            return New-AlignmentResult -Classification "REFERENCE MISSING" `
                -Message "The accepted public release has no valid release record in published GeoCeDG authority." `
                -Candidate $Candidate -Registry $registry `
                -CurrentBaseline $current `
                -BookAuthorityCommit $bookAuthorityCommit
        }
    }
    try {
        $acceptedSnapshot = Get-TechnicalAuthoritySnapshot `
            -Root $RepositoryRoot -Commit $acceptedCommit
    } catch {
        return New-AlignmentResult -Classification "REFERENCE MISSING" `
            -Message "The accepted technical authority cannot be reconstructed." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    if ($acceptedSnapshot.technical_authority_fingerprint -ne
            $acceptedFingerprint) {
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The stored accepted fingerprint contradicts its commit." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }

    $publishedCommit = $Candidate.published_reference.commit
    if ($acceptedCommit -eq $publishedCommit) {
        if ($acceptedFingerprint -eq
                $Candidate.technical_authority_fingerprint) {
            return New-AlignmentResult -Classification "ALIGNED" `
                -Message "Accepted editorial baseline matches published authority." `
                -Candidate $Candidate -Registry $registry `
                -CurrentBaseline $current `
                -BookAuthorityCommit $bookAuthorityCommit
        }
        return New-AlignmentResult `
            -Classification "TECHNICAL CONTRADICTION" `
            -Message "The accepted commit matches, but its fingerprint does not." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }

    if (Test-GitAncestor -Root $RepositoryRoot -Ancestor $acceptedCommit `
            -Descendant $publishedCommit) {
        if ($acceptedFingerprint -eq
                $Candidate.technical_authority_fingerprint) {
            return New-AlignmentResult -Classification "ALIGNED" `
                -Message "Newer commits do not change the bounded authority snapshot." `
                -Candidate $Candidate -Registry $registry `
                -CurrentBaseline $current `
                -BookAuthorityCommit $bookAuthorityCommit
        }
        $publishedSnapshot = [pscustomobject]@{
            authority_categories = $Candidate.authority_categories
        }
        $changedCategories = @(Get-ChangedAuthorityCategories `
                -AcceptedSnapshot $acceptedSnapshot `
                -PublishedSnapshot $publishedSnapshot)
        return New-AlignmentResult `
            -Classification "EDITORIAL BASELINE STALE" `
            -Message "Published authority materially differs from the accepted baseline." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit `
            -ChangedCategories $changedCategories
    }
    if (Test-GitAncestor -Root $RepositoryRoot -Ancestor $publishedCommit `
            -Descendant $acceptedCommit) {
        return New-AlignmentResult `
            -Classification "UNPUBLISHED PRODUCT STATE" `
            -Message "The editorial baseline points beyond published GeoCeDG authority." `
            -Candidate $Candidate -Registry $registry `
            -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
    }
    return New-AlignmentResult -Classification "TECHNICAL CONTRADICTION" `
        -Message "The editorial and published GeoCeDG histories diverge." `
        -Candidate $Candidate -Registry $registry `
        -CurrentBaseline $current -BookAuthorityCommit $bookAuthorityCommit
}

function Assert-NoBookSubmoduleDeclaration {
    param([Parameter(Mandatory)] [string]$Root)

    $gitModules = Join-Path $Root ".gitmodules"
    if (-not (Test-Path -LiteralPath $gitModules -PathType Leaf)) {
        return
    }
    $entries = Invoke-RepositoryGit -Root $Root `
        -Arguments @("config", "--file", $gitModules, "--get-regexp",
            '^submodule\..*\.path$') -AllowedExitCodes @(0, 1)
    foreach ($line in $entries.Lines) {
        $parts = $line -split '\s+', 2
        $declaredPath = if ($parts.Count -eq 2) {
            ($parts[1].Trim().Replace("\", "/") -replace '/+', '/')
        } else {
            ""
        }
        while ($declaredPath.StartsWith("./", [StringComparison]::Ordinal)) {
            $declaredPath = $declaredPath.Substring(2)
        }
        if ($declaredPath.Trim("/").Equals(
                "book", [StringComparison]::OrdinalIgnoreCase)) {
            throw "The GeoCeDG repository declares book as a submodule."
        }
    }
}

function Get-ValidatedBookContext {
    $geoCeDGRoot = Get-NormalizedFullPath -Path (
        (Invoke-RepositoryGit -Root $RepositoryRoot `
            -Arguments @("rev-parse", "--show-toplevel")).Text.Trim())
    if (-not $geoCeDGRoot.Equals(
            (Get-NormalizedFullPath -Path $RepositoryRoot),
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "The script location is not the GeoCeDG Git worktree root."
    }
    $trackedBook = Invoke-RepositoryGit -Root $geoCeDGRoot `
        -Arguments @("ls-files", "-s", "--", "book")
    if (-not [string]::IsNullOrWhiteSpace($trackedBook.Text)) {
        throw "The GeoCeDG index contains the local book path."
    }
    Assert-NoBookSubmoduleDeclaration -Root $geoCeDGRoot

    if (-not (Test-Path -LiteralPath $BookLink -PathType Container)) {
        throw "The optional local book link is not configured: $BookLink"
    }
    $linkItem = Get-Item -LiteralPath $BookLink -Force
    if (-not ($linkItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "The root-level book path must be a filesystem link, not a " +
            "directory inside the GeoCeDG worktree: $BookLink"
    }
    $targetItem = $linkItem.ResolveLinkTarget($true)
    if ($null -eq $targetItem -or -not $targetItem.Exists -or
            -not ($targetItem -is [IO.DirectoryInfo])) {
        throw "The root-level book link does not resolve to a directory."
    }
    $resolvedBookRoot = Get-NormalizedFullPath -Path $targetItem.FullName

    $insideBook = (Invoke-RepositoryGit -Root $resolvedBookRoot `
        -Arguments @("rev-parse", "--is-inside-work-tree")).Text.Trim()
    if ($insideBook -ne "true") {
        throw "The book link target is not a Git worktree."
    }
    $bookRoot = Get-NormalizedFullPath -Path (
        (Invoke-RepositoryGit -Root $resolvedBookRoot `
            -Arguments @("rev-parse", "--show-toplevel")).Text.Trim())
    if (-not $bookRoot.Equals(
            $resolvedBookRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "The book link must resolve to the root of its Git worktree."
    }
    if ((Test-SameOrNestedPath -Candidate $bookRoot -Container $geoCeDGRoot) -or
            (Test-SameOrNestedPath -Candidate $geoCeDGRoot -Container $bookRoot)) {
        throw "GeoCeDG and the book must be exterior, non-nested worktrees."
    }

    $geoCeDGCommonDir = Resolve-GitPath -WorktreeRoot $geoCeDGRoot -GitPath (
        (Invoke-RepositoryGit -Root $geoCeDGRoot `
            -Arguments @("rev-parse", "--git-common-dir")).Text.Trim())
    $bookCommonDir = Resolve-GitPath -WorktreeRoot $bookRoot -GitPath (
        (Invoke-RepositoryGit -Root $bookRoot `
            -Arguments @("rev-parse", "--git-common-dir")).Text.Trim())
    $geoCeDGGitDir = Resolve-GitPath -WorktreeRoot $geoCeDGRoot -GitPath (
        (Invoke-RepositoryGit -Root $geoCeDGRoot `
            -Arguments @("rev-parse", "--git-dir")).Text.Trim())
    $bookGitDir = Resolve-GitPath -WorktreeRoot $bookRoot -GitPath (
        (Invoke-RepositoryGit -Root $bookRoot `
            -Arguments @("rev-parse", "--git-dir")).Text.Trim())
    $rawGeoCeDGMetadata = Get-RawWorktreeGitMetadata `
        -WorktreeRoot $geoCeDGRoot -Description "GeoCeDG"
    $rawBookMetadata = Get-RawWorktreeGitMetadata `
        -WorktreeRoot $bookRoot -Description "Book"
    foreach ($comparison in @(
            @($geoCeDGGitDir, $rawGeoCeDGMetadata.GitDirectory,
                "GeoCeDG Git directory"),
            @($geoCeDGCommonDir, $rawGeoCeDGMetadata.CommonDirectory,
                "GeoCeDG Git common directory"),
            @($bookGitDir, $rawBookMetadata.GitDirectory,
                "Book Git directory"),
            @($bookCommonDir, $rawBookMetadata.CommonDirectory,
                "Book Git common directory"))) {
        if (-not ([string]$comparison[0]).Equals(
                [string]$comparison[1],
                [StringComparison]::OrdinalIgnoreCase)) {
            throw "$($comparison[2]) contradicts its raw worktree control data."
        }
    }
    $geoMetadataPaths = @(
        [pscustomobject]@{
            Description = "GeoCeDG Git common directory"
            Path = $geoCeDGCommonDir
        },
        [pscustomobject]@{
            Description = "GeoCeDG Git worktree directory"
            Path = $geoCeDGGitDir
        }
    )
    $bookMetadataPaths = @(
        [pscustomobject]@{
            Description = "Book Git common directory"
            Path = $bookCommonDir
        },
        [pscustomobject]@{
            Description = "Book Git worktree directory"
            Path = $bookGitDir
        }
    )
    foreach ($metadata in @($geoMetadataPaths + $bookMetadataPaths)) {
        Assert-NoReparseAbsolutePathComponents -Path $metadata.Path `
            -Description $metadata.Description
    }
    foreach ($geoMetadata in $geoMetadataPaths) {
        foreach ($bookMetadata in $bookMetadataPaths) {
            if ($geoMetadata.Path.Equals(
                    $bookMetadata.Path,
                    [StringComparison]::OrdinalIgnoreCase)) {
                throw "GeoCeDG and the book resolve to the same Git authority."
            }
            if (Test-PathsOverlap -First $geoMetadata.Path `
                    -Second $bookMetadata.Path) {
                throw "GeoCeDG and the book have nested Git metadata authority."
            }
        }
    }
    foreach ($bookMetadata in $bookMetadataPaths) {
        if (Test-PathsOverlap -First $bookMetadata.Path -Second $geoCeDGRoot) {
            throw "GeoCeDG and the book have nested Git metadata authority."
        }
    }
    foreach ($geoMetadata in $geoMetadataPaths) {
        if (Test-PathsOverlap -First $geoMetadata.Path -Second $bookRoot) {
            throw "GeoCeDG and the book have nested Git metadata authority."
        }
    }

    $superproject = (Invoke-RepositoryGit -Root $bookRoot `
        -Arguments @("rev-parse", "--show-superproject-working-tree")).Text.Trim()
    if (-not [string]::IsNullOrWhiteSpace($superproject)) {
        throw "The book repository is registered as a Git submodule."
    }

    $ignored = Invoke-RepositoryGit -Root $geoCeDGRoot `
        -Arguments @("check-ignore", "-q", "--", "book") `
        -AllowedExitCodes @(0, 1)
    if ($ignored.ExitCode -ne 0) {
        throw "The root-level book path is not ignored by GeoCeDG."
    }
    $visibleBook = Invoke-RepositoryGit -Root $geoCeDGRoot `
        -Arguments @("status", "--porcelain=v1", "--untracked-files=all",
            "--", "book")
    if (-not [string]::IsNullOrWhiteSpace($visibleBook.Text)) {
        throw "Book content is visible in the GeoCeDG worktree status."
    }

    $geoCeDGState = Get-GitRepositoryState -Root $geoCeDGRoot
    $bookState = Get-GitRepositoryState -Root $bookRoot
    $bookRepositoryName = ConvertTo-GitHubRepositoryName `
        -Origin $bookState.Origin
    if (-not $bookRepositoryName.Equals(
            $ExpectedBookRepository, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Unexpected book origin identity; expected " +
            "$ExpectedBookRepository. The URL was redacted."
    }
    $quarantineAnchors = @(
        Get-OptionalGitCommit -Root $bookRoot -Revision $QuarantinedBookCommit
        Get-OptionalGitCommit -Root $bookRoot `
            -Revision "refs/remotes/origin/copilot/geo-ce-dg-book-analysis"
    ) | Where-Object { $null -ne $_ } | Sort-Object -Unique
    $quarantinedBranchName = $bookState.Branch.Equals(
        "copilot/geo-ce-dg-book-analysis",
        [StringComparison]::OrdinalIgnoreCase)
    $quarantinedHistory = $false
    foreach ($anchor in $quarantineAnchors) {
        if (Test-GitAncestor -Root $bookRoot -Ancestor $anchor `
                -Descendant $bookState.Commit) {
            $quarantinedHistory = $true
            break
        }
    }
    $bookAuthorityDisposition = if ($quarantinedBranchName -or
            $quarantinedHistory) {
        "QUARANTINED GENERATED BRANCH — NON-AUTHORITATIVE"
    } elseif ($bookState.Branch -eq "main" -and
            $bookState.Commit -eq $bookState.MainCommit -and
            $bookState.MainCommit -eq $bookState.OriginMainCommit) {
        "PUBLISHED CANONICAL MAIN"
    } elseif ($bookState.Commit -eq $bookState.MainCommit) {
        "EDITORIAL CANDIDATE WORKTREE"
    } else {
        "NON-MAIN WORKTREE"
    }

    return [pscustomobject]@{
        LinkType = [string]$linkItem.LinkType
        LinkPath = $BookLink
        BookRoot = $bookRoot
        GeoCeDGRoot = $geoCeDGRoot
        GeoCeDGState = $geoCeDGState
        BookState = $bookState
        BookAuthorityDisposition = $bookAuthorityDisposition
    }
}

function Get-PowerShellExecutable {
    $powerShell = Join-Path $PSHOME "pwsh.exe"
    if (-not (Test-Path -LiteralPath $powerShell -PathType Leaf)) {
        $powerShell = Join-Path $PSHOME "pwsh"
    }
    if (-not (Test-Path -LiteralPath $powerShell -PathType Leaf)) {
        throw "Unable to locate the current PowerShell executable."
    }
    return $powerShell
}

function Assert-OrdinaryFileInsideRoot {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Description
    )

    $absolute = [IO.Path]::GetFullPath($Path)
    if (-not (Test-SameOrNestedPath -Candidate $absolute -Container $Root) -or
            -not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "$Description is unavailable: $absolute"
    }
    $item = Get-Item -LiteralPath $absolute -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "$Description must not be a filesystem link: $absolute"
    }
    $normalizedRoot = Get-NormalizedFullPath -Path $Root
    $parent = $item.Directory
    while ($null -ne $parent -and -not $parent.FullName.Equals(
            $normalizedRoot, [StringComparison]::OrdinalIgnoreCase)) {
        if ($parent.Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "$Description has a filesystem-link parent: $($parent.FullName)"
        }
        $parent = $parent.Parent
    }
    if ($null -eq $parent) {
        throw "$Description cannot be proven inside its repository root."
    }
}

function Assert-TrackedRegularRepositoryFile {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$RepositoryPath,
        [Parameter(Mandatory)] [string]$Description
    )

    $entry = Invoke-RepositoryGit -Root $Root `
        -Arguments @("ls-files", "-s", "--", $RepositoryPath)
    if ($entry.Lines.Count -ne 1 -or
            $entry.Text -notmatch '^(100644|100755) [0-9a-fA-F]{40,64} 0\s+') {
        throw "$Description must be one tracked regular repository file."
    }
}

function Invoke-BookOwnedAction {
    param(
        [Parameter(Mandatory)] [object]$Context,
        [Parameter(Mandatory)] [string]$SelectedAction,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Arguments
    )

    if ($Context.BookAuthorityDisposition -eq
            "QUARANTINED GENERATED BRANCH — NON-AUTHORITATIVE") {
        throw "Refusing to invoke book tooling from the quarantined generated branch."
    }
    $relativeScript = if ($SelectedAction -eq "Verify") {
        "tools\verify.ps1"
    } else {
        "tools\build.ps1"
    }
    $bookScript = [IO.Path]::GetFullPath((Join-Path $Context.BookRoot `
            $relativeScript))
    Assert-OrdinaryFileInsideRoot -Path $bookScript -Root $Context.BookRoot `
        -Description "The book-owned $SelectedAction entry point"
    Assert-TrackedRegularRepositoryFile -Root $Context.BookRoot `
        -RepositoryPath $relativeScript.Replace("\", "/") `
        -Description "The book-owned $SelectedAction entry point"

    $powerShell = Get-PowerShellExecutable
    Write-Host "Action: $SelectedAction (explicit book-owned delegation)"
    Write-Host "Book entry point: $relativeScript"
    Push-Location -LiteralPath $Context.BookRoot
    try {
        & $powerShell -NoProfile -File $bookScript @Arguments
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "Book $SelectedAction failed with exit code $exitCode."
    }
    Write-Host "Book $SelectedAction completed with exit code 0."
}

function Get-FileSha256Lower {
    param([Parameter(Mandatory)] [string]$Path)

    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-G9O1ReportedArtifactPath {
    param(
        [Parameter(Mandatory)] [string[]]$OutputLines,
        [Parameter(Mandatory)] [string]$Label,
        [Parameter(Mandatory)] [string]$Profile,
        [Parameter(Mandatory)] [string]$ProfileRoot,
        [Parameter(Mandatory)] [string]$ExpectedFileName
    )

    $pattern = '^\s*' + [regex]::Escape($Label) + ':\s*(?<path>.+?)\s*$'
    $reportedPaths = [Collections.Generic.List[string]]::new()
    foreach ($line in $OutputLines) {
        $match = [regex]::Match($line, $pattern)
        if ($match.Success) {
            $reportedPaths.Add($match.Groups['path'].Value)
        }
    }
    if ($reportedPaths.Count -ne 1) {
        throw "G9O1 profile '$Profile' did not report exactly one $Label path."
    }
    $reportedPath = $reportedPaths[0]
    $absolute = if ([IO.Path]::IsPathRooted($reportedPath)) {
        [IO.Path]::GetFullPath($reportedPath)
    } else {
        [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $reportedPath))
    }
    Assert-OrdinaryFileInsideRoot -Path $absolute -Root $ProfileRoot `
        -Description "The G9O1 $Profile $Label artifact"
    Assert-NoReparsePathComponents -Root $RepositoryRoot -Path $absolute `
        -Description "The G9O1 $Profile $Label artifact"
    if (-not [IO.Path]::GetFileName($absolute).Equals(
            $ExpectedFileName, [StringComparison]::OrdinalIgnoreCase)) {
        throw "G9O1 profile '$Profile' reported an unexpected $Label filename."
    }
    return $absolute
}

function Get-G9O1ProfileEvidenceRecord {
    param(
        [Parameter(Mandatory)] [string]$Profile,
        [Parameter(Mandatory)] [string[]]$OutputLines,
        [Parameter(Mandatory)] [string]$ProfileRoot,
        [Parameter(Mandatory)] [string]$EvidenceRoot
    )

    $manifest = Get-G9O1ReportedArtifactPath -OutputLines $OutputLines `
        -Label "Manifest" -Profile $Profile -ProfileRoot $ProfileRoot `
        -ExpectedFileName "manifest.json"
    $archive = Get-G9O1ReportedArtifactPath -OutputLines $OutputLines `
        -Label "Archive" -Profile $Profile -ProfileRoot $ProfileRoot `
        -ExpectedFileName "bundle.zip"
    $manifestParent = Get-NormalizedFullPath -Path (Split-Path -Parent $manifest)
    $archiveParent = Get-NormalizedFullPath -Path (Split-Path -Parent $archive)
    if (-not $manifestParent.Equals(
            $archiveParent, [StringComparison]::OrdinalIgnoreCase)) {
        throw "G9O1 profile '$Profile' reported artifacts from different bundles."
    }
    return [pscustomobject][ordered]@{
        profile = $Profile
        manifest = [ordered]@{
            path = [IO.Path]::GetRelativePath(
                $EvidenceRoot, $manifest).Replace("\", "/")
            sha256 = Get-FileSha256Lower -Path $manifest
        }
        archive = [ordered]@{
            path = [IO.Path]::GetRelativePath(
                $EvidenceRoot, $archive).Replace("\", "/")
            sha256 = Get-FileSha256Lower -Path $archive
        }
    }
}

function Write-EvidenceExportSidecar {
    param(
        [Parameter(Mandatory)] [string]$RelativeOutputDirectory,
        [Parameter(Mandatory)] [object]$GeoCeDGState,
        [Parameter(Mandatory)] [object]$Candidate,
        [Parameter(Mandatory)] [string]$Disposition,
        [Parameter(Mandatory)] [object[]]$ProfileOutputs
    )

    $relativeSidecar = ($RelativeOutputDirectory.TrimEnd("/", "\") +
        "/book-evidence-export.v2.json").Replace("\", "/")
    $absolute = Resolve-GeoCeDGArtifactPath -RelativePath $relativeSidecar `
        -RequiredSubtree "artifacts/knowledge/book" -RequireFileName
    $repositoryPath = [IO.Path]::GetRelativePath(
        $RepositoryRoot, $absolute).Replace("\", "/")
    $tracked = Invoke-RepositoryGit -Root $RepositoryRoot `
        -Arguments @("ls-files", "--", $repositoryPath)
    if (-not [string]::IsNullOrWhiteSpace($tracked.Text)) {
        throw "Evidence refuses to overwrite a tracked sidecar: $repositoryPath"
    }
    $sidecar = [ordered]@{
        schema_version = 2
        kind = "GEOCEDG_BOOK_EVIDENCE_EXPORT"
        disposition = $Disposition
        authority = [ordered]@{
            operational_bridge = "tools/book/book-worktree.ps1"
            evidence_generator = "tools/knowledge/build-knowledge-bundle.ps1"
            evidence_system = "G9O1"
        }
        geocedg_source_state = [ordered]@{
            head_commit = $GeoCeDGState.Commit
            working_tree_dirty = [bool]($GeoCeDGState.Status.Count -gt 0)
            origin_main_commit = $Candidate.technical_source.commit
            reference_type = $Candidate.published_reference.type
            reference_tag = $Candidate.published_reference.tag
            reference_commit = $Candidate.published_reference.commit
            head_matches_published_reference = [bool](
                $GeoCeDGState.Commit -eq $Candidate.published_reference.commit)
        }
        profile_outputs = @($ProfileOutputs | Sort-Object -Property profile `
                -CaseSensitive)
    }
    $parent = Split-Path -Parent $absolute
    [void](New-Item -ItemType Directory -Path $parent -Force)
    [IO.File]::WriteAllText($absolute,
        (ConvertTo-CanonicalJson -Value $sidecar),
        [Text.UTF8Encoding]::new($false))
    return $absolute
}

function Invoke-EvidenceExport {
    param(
        [Parameter(Mandatory)] [string[]]$Profiles,
        [Parameter(Mandatory)] [string]$RelativeOutputDirectory,
        [switch]$AllowDirty
    )

    if ($Profiles.Count -eq 0) {
        throw "Evidence requires at least one existing G9O1 profile."
    }
    $normalizedProfiles = @($Profiles | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | Sort-Object -Unique -CaseSensitive)
    if ($normalizedProfiles.Count -ne $Profiles.Count) {
        throw "Evidence profile names must be non-empty and unique."
    }
    $geoCeDGState = Get-GitRepositoryState -Root $RepositoryRoot
    $candidate = Get-TechnicalBaselineCandidate -GeoCeDGState $geoCeDGState `
        -SelectedReferenceTag $ReferenceTag
    $releaseQuality = $geoCeDGState.Status.Count -eq 0 -and
        $geoCeDGState.Commit -eq $candidate.published_reference.commit
    if (-not $releaseQuality -and -not $AllowDirty) {
        throw "Release-quality book evidence requires a clean checkout exactly " +
            "at the commit of the published reference. Use -AllowDirtyEvidence " +
            "only for explicit NON_RELEASE evidence."
    }
    $disposition = if ($releaseQuality) {
        "RELEASE_QUALITY_PUBLISHED_STATE"
    } else {
        "NON_RELEASE_DIRTY_OR_UNPUBLISHED_STATE"
    }
    if ($releaseQuality) {
        Write-Host "Evidence disposition: $disposition"
    } else {
        Write-Warning "Evidence disposition: $disposition"
    }
    $evidenceRoot = Resolve-GeoCeDGArtifactPath `
        -RelativePath $RelativeOutputDirectory `
        -RequiredSubtree "artifacts/knowledge/book"

    $builder = Join-Path $RepositoryRoot `
        "tools\knowledge\build-knowledge-bundle.ps1"
    Assert-OrdinaryFileInsideRoot -Path $builder -Root $RepositoryRoot `
        -Description "The G9O1 knowledge-bundle generator"
    Assert-TrackedRegularRepositoryFile -Root $RepositoryRoot `
        -RepositoryPath "tools/knowledge/build-knowledge-bundle.ps1" `
        -Description "The G9O1 knowledge-bundle generator"
    $powerShell = Get-PowerShellExecutable
    $profileRecords = [Collections.Generic.List[object]]::new()
    foreach ($profile in $normalizedProfiles) {
        if ($profile -notmatch '^[a-z0-9][a-z0-9-]*$') {
            throw "Invalid G9O1 profile identifier: $profile"
        }
        $profileOutput = ($RelativeOutputDirectory.TrimEnd("/", "\") +
            "/$profile").Replace("\", "/")
        $profileRoot = Resolve-GeoCeDGArtifactPath `
            -RelativePath $profileOutput `
            -RequiredSubtree "artifacts/knowledge/book"
        $arguments = @(
            "-NoProfile", "-File", $builder,
            "-RepositoryRoot", $RepositoryRoot,
            "-Profile", $profile,
            "-OutputDirectory", $profileOutput
        )
        if ($AllowDirty) {
            $arguments += "-AllowDirty"
        }
        Write-Host "Generating existing G9O1 profile: $profile"
        $builderOutput = @(& $powerShell @arguments 2>&1 | ForEach-Object {
                $line = $_.ToString()
                if (-not [string]::IsNullOrWhiteSpace($line)) {
                    $line
                }
            })
        $builderExitCode = $LASTEXITCODE
        foreach ($line in $builderOutput) {
            Write-Host $line
        }
        if ($builderExitCode -ne 0) {
            throw "G9O1 evidence profile '$profile' failed with exit code " +
                "$builderExitCode."
        }
        $profileRecords.Add((Get-G9O1ProfileEvidenceRecord `
                -Profile $profile -OutputLines $builderOutput `
                -ProfileRoot $profileRoot -EvidenceRoot $evidenceRoot))
    }
    $sidecarPath = Write-EvidenceExportSidecar `
        -RelativeOutputDirectory $RelativeOutputDirectory `
        -GeoCeDGState $geoCeDGState -Candidate $candidate `
        -Disposition $disposition -ProfileOutputs @($profileRecords)
    Write-Host "Evidence export sidecar: $sidecarPath"
    Write-Host "Evidence export completed through existing G9O1 authority."
}

function Write-PublishedAndAlignmentState {
    param(
        [Parameter(Mandatory)] [object]$GeoCeDGState,
        [Parameter(Mandatory)] [object]$Candidate,
        [Parameter(Mandatory)] [object]$Alignment
    )

    $distance = $Candidate.published_reference.commits_to_origin_main
    $mainState = if ($distance -eq 0) {
        "PUBLISHED REFERENCE CURRENT"
    } else {
        $categories = if ($null -eq
                $Candidate.main_delta.changed_authority_categories) {
            "unavailable"
        } elseif (@($Candidate.main_delta.changed_authority_categories
                ).Count -eq 0) {
            "none"
        } else {
            @($Candidate.main_delta.changed_authority_categories) -join ", "
        }
        "MAIN AHEAD OF PUBLISHED REFERENCE ($distance commits; changed " +
            "authority categories: $categories)"
    }
    $checkoutState = if ($GeoCeDGState.Commit -eq
            $Candidate.published_reference.commit -and
            $GeoCeDGState.Status.Count -eq 0) {
        "AT PUBLISHED REFERENCE"
    } else {
        "NOT AT PUBLISHED REFERENCE"
    }
    Write-Host ("GeoCeDG technical source SHA: " +
        $GeoCeDGState.Commit)
    Write-Host ("GeoCeDG published reference: " +
        "$($Candidate.published_reference.id) @ " +
        $Candidate.published_reference.commit)
    if ($null -ne $Candidate.release) {
        Write-Host ("GeoCeDG release record: " +
            "$($Candidate.release.record.path) (coherent with Git objects; " +
            "public availability recorded, not rechecked offline)")
    }
    foreach ($rejected in @($Candidate.ineligible_tags)) {
        Write-Host ("GeoCeDG ineligible tag: " +
            "$($rejected.tag) ($($rejected.reason))")
    }
    Write-Host ("GeoCeDG origin/main relationship: " + $mainState)
    Write-Host ("GeoCeDG checkout relationship: " + $checkoutState)
    Write-Host ("GeoCeDG latest closed phase: " +
        $Candidate.normative_phase_status.latest_closed_phase)
    Write-Host ("GeoCeDG next gate: " +
        $Candidate.normative_phase_status.next_gate)
    Write-Host "Book technical alignment: $($Alignment.classification)"
    Write-Host "Book alignment detail: $($Alignment.message)"
    if (@($Alignment.changed_authority_categories).Count -gt 0) {
        Write-Host ("Book alignment material drift: " +
            (@($Alignment.changed_authority_categories) -join ", "))
    }
}

try {
    if ($Action -eq "Evidence") {
        Invoke-EvidenceExport -Profiles $EvidenceProfiles `
            -RelativeOutputDirectory $EvidenceOutputDirectory `
            -AllowDirty:$AllowDirtyEvidence
        exit 0
    }

    $geoCeDGState = Get-GitRepositoryState -Root $RepositoryRoot
    if ($Action -eq "BaselineCandidate") {
        $candidate = Get-TechnicalBaselineCandidate `
            -GeoCeDGState $geoCeDGState -SelectedReferenceTag $ReferenceTag
        $writtenPath = Write-TechnicalBaselineCandidate `
            -Candidate $candidate -RelativeOutputPath $OutputPath
        Write-Host "Action: BaselineCandidate (GeoCeDG evidence only)"
        Write-Host "Published reference: $($candidate.published_reference.id)"
        Write-Host "Published commit: $($candidate.published_reference.commit)"
        Write-Host ("Technical authority fingerprint: " +
            $candidate.technical_authority_fingerprint)
        Write-Host "Candidate path: $writtenPath"
        Write-Host "No editorial file was changed or accepted."
        exit 0
    }

    $context = Get-ValidatedBookContext
    Write-Host "GeoCeDG external book worktree boundary: PASS"
    Write-Host "Book link: $($context.LinkPath)"
    Write-Host "Link type: $($context.LinkType)"
    Write-Host "Resolved book root: $($context.BookRoot)"
    Write-Host ("Book authority disposition: " +
        $context.BookAuthorityDisposition)
    Write-GitRepositoryState -Label "GeoCeDG" -State $context.GeoCeDGState
    Write-GitRepositoryState -Label "Book" -State $context.BookState

    if ($Action -in @("Status", "Alignment")) {
        if ($context.BookAuthorityDisposition -eq
                "QUARANTINED GENERATED BRANCH — NON-AUTHORITATIVE") {
            throw "The quarantined generated book branch cannot be audited as authority."
        }
        $candidate = Get-TechnicalBaselineCandidate `
            -GeoCeDGState $context.GeoCeDGState `
            -SelectedReferenceTag $ReferenceTag
        $alignment = Get-BookTechnicalAlignment `
            -BookRoot $context.BookRoot -Candidate $candidate
        Write-PublishedAndAlignmentState `
            -GeoCeDGState $context.GeoCeDGState -Candidate $candidate `
            -Alignment $alignment
        Write-Host "Action: $Action (read-only)"
        exit 0
    }

    Invoke-BookOwnedAction -Context $context -SelectedAction $Action `
        -Arguments $BookArguments
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

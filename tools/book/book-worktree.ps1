<#
.SYNOPSIS
Inspects or explicitly delegates to the independent GeoCeDG book worktree.

.DESCRIPTION
Resolves the root-level book link, proves that it belongs to a separate Git
repository outside the GeoCeDG worktree, and reports both repository states.
The default Status action is read-only. Verify and Build only invoke the
corresponding book-owned PowerShell entry point when it already exists.

This script never installs tools or stages, commits, merges, tags, fetches,
pulls or pushes either repository. It is deliberately not part of
tools/agent/verify.ps1.

.PARAMETER Action
Status reports the validated two-repository state. Verify delegates to
tools/verify.ps1 in the book repository. Build delegates to tools/build.ps1 in
the book repository.

.PARAMETER BookArguments
Optional arguments forwarded to an explicitly selected book-owned script.
#>
[CmdletBinding()]
param(
    [ValidateSet("Status", "Verify", "Build")]
    [string]$Action = "Status",
    [string[]]$BookArguments = @()
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedBookRepository = "mpradovelasco/geocedg_book"
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$BookLink = Join-Path $RepositoryRoot "book"

function Invoke-RepositoryGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $lines = @(& git -C $Root @Arguments 2>&1 | ForEach-Object {
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
    return [pscustomobject]@{
        Branch = $branch
        Commit = $commit.ToLowerInvariant()
        Origin = $origin
        Status = @($status)
    }
}

function Write-GitRepositoryState {
    param(
        [Parameter(Mandatory)] [string]$Label,
        [Parameter(Mandatory)] [object]$State
    )

    Write-Host "${Label} branch: $($State.Branch)"
    Write-Host "${Label} HEAD: $($State.Commit)"
    Write-Host "${Label} origin: $($State.Origin)"
    if ($State.Status.Count -eq 0) {
        Write-Host "${Label} status: clean"
    } else {
        Write-Host "${Label} status: dirty"
        foreach ($line in $State.Status) {
            Write-Host "  $line"
        }
    }
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
        if ($parts.Count -eq 2 -and
                $parts[1].Trim().Replace("\", "/").Trim("/").Equals(
                    "book", [StringComparison]::OrdinalIgnoreCase)) {
            throw "The GeoCeDG repository declares book as a submodule."
        }
    }
}

function Get-ValidatedBookContext {
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

    $geoCeDGRoot = Get-NormalizedFullPath -Path (
        (Invoke-RepositoryGit -Root $RepositoryRoot `
            -Arguments @("rev-parse", "--show-toplevel")).Text.Trim())
    if (-not $geoCeDGRoot.Equals(
            (Get-NormalizedFullPath -Path $RepositoryRoot),
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "The script location is not the GeoCeDG Git worktree root."
    }

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
    if ($geoCeDGCommonDir.Equals(
            $bookCommonDir, [StringComparison]::OrdinalIgnoreCase)) {
        throw "GeoCeDG and the book resolve to the same Git authority."
    }

    $trackedBook = Invoke-RepositoryGit -Root $geoCeDGRoot `
        -Arguments @("ls-files", "-s", "--", "book")
    if (-not [string]::IsNullOrWhiteSpace($trackedBook.Text)) {
        throw "The GeoCeDG index contains the local book path."
    }
    Assert-NoBookSubmoduleDeclaration -Root $geoCeDGRoot

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
        throw "Unexpected book origin '$($bookState.Origin)'; expected " +
            "$ExpectedBookRepository."
    }

    return [pscustomobject]@{
        LinkType = [string]$linkItem.LinkType
        LinkPath = $BookLink
        BookRoot = $bookRoot
        GeoCeDGRoot = $geoCeDGRoot
        GeoCeDGState = $geoCeDGState
        BookState = $bookState
    }
}

try {
    $context = Get-ValidatedBookContext

    Write-Host "GeoCeDG external book worktree boundary: PASS"
    Write-Host "Book link: $($context.LinkPath)"
    Write-Host "Link type: $($context.LinkType)"
    Write-Host "Resolved book root: $($context.BookRoot)"
    Write-GitRepositoryState -Label "GeoCeDG" -State $context.GeoCeDGState
    Write-GitRepositoryState -Label "Book" -State $context.BookState

    if ($Action -eq "Status") {
        Write-Host "Action: Status (read-only)"
        exit 0
    }

    $relativeScript = if ($Action -eq "Verify") {
        "tools\verify.ps1"
    } else {
        "tools\build.ps1"
    }
    $bookScript = [IO.Path]::GetFullPath((Join-Path $context.BookRoot `
            $relativeScript))
    if (-not (Test-SameOrNestedPath -Candidate $bookScript `
            -Container $context.BookRoot) -or
            -not (Test-Path -LiteralPath $bookScript -PathType Leaf)) {
        throw "The book-owned $Action entry point is unavailable: $relativeScript"
    }

    $powerShell = Join-Path $PSHOME "pwsh.exe"
    if (-not (Test-Path -LiteralPath $powerShell -PathType Leaf)) {
        $powerShell = Join-Path $PSHOME "pwsh"
    }
    if (-not (Test-Path -LiteralPath $powerShell -PathType Leaf)) {
        throw "Unable to locate the current PowerShell executable."
    }

    Write-Host "Action: $Action (explicit book-owned delegation)"
    Write-Host "Book entry point: $relativeScript"
    Push-Location -LiteralPath $context.BookRoot
    try {
        & $powerShell -NoProfile -File $bookScript @BookArguments
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "Book $Action failed with exit code $exitCode."
    }
    Write-Host "Book $Action completed with exit code 0."
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

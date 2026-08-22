[CmdletBinding()]
param(
    [string]$LogDirectory = "artifacts/agent/book-operations",
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$PowerShell = Join-Path $PSHOME "pwsh.exe"
if (-not (Test-Path -LiteralPath $PowerShell -PathType Leaf)) {
    $PowerShell = Join-Path $PSHOME "pwsh"
}

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RepositoryPath {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [switch]$RequireFile,
        [switch]$RequireArtifact
    )

    Assert-Condition -Condition (-not [IO.Path]::IsPathRooted($RelativePath)) `
        -Message "Repository path must be relative: $RelativePath"
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $RelativePath))
    $rootPrefix = $RepositoryRoot.TrimEnd("/", "\") +
        [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition $absolute.StartsWith(
        $rootPrefix, [StringComparison]::OrdinalIgnoreCase) `
        -Message "Repository path escapes the root: $RelativePath"
    if ($RequireArtifact) {
        $artifactRoot = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
                "artifacts"))
        Assert-Condition -Condition $absolute.StartsWith(
            $artifactRoot + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase) `
            -Message "Log path must remain below artifacts/: $RelativePath"
    }
    if ($RequireFile) {
        Assert-Condition -Condition (Test-Path -LiteralPath $absolute `
                -PathType Leaf) -Message "Required file is missing: $RelativePath"
    }
    return $absolute
}

function Assert-NoReparsePathComponents {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )

    $rootPath = [IO.Path]::GetFullPath($Root)
    $absolute = [IO.Path]::GetFullPath($Path)
    $relative = [IO.Path]::GetRelativePath($rootPath, $absolute)
    $cursor = $rootPath
    foreach ($component in @($relative -split '[\\\\/]')) {
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

try {
    $requiredFiles = @(
        "tools/book/book-worktree.ps1",
        "tools/book/tests/book-operations.tests.ps1",
        "tools/agent/verify-book-operations.ps1",
        "docs/developer/book_repository_workflow.md",
        ".github/prompts/canonical/book/operations.prompt.md"
    )
    foreach ($path in $requiredFiles) {
        [void](Resolve-RepositoryPath -RelativePath $path -RequireFile)
    }

    $ignore = Get-Content -LiteralPath (Join-Path $RepositoryRoot ".gitignore") `
        -Raw
    Assert-Condition -Condition ([regex]::IsMatch($ignore,
            '(?m)^/book\s*$')) `
        -Message "GeoCeDG must keep the root book link ignored."

    $workflow = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
            "docs\developer\book_repository_workflow.md") -Raw
    foreach ($requiredText in @(
            "GeoCeDG and its future book use independent Git repositories",
            "TECHNICAL_BASELINES.json",
            "ALIGNED",
            "EDITORIAL BASELINE STALE",
            "TECHNICAL CONTRADICTION",
            "REFERENCE MISSING",
            "UNPUBLISHED PRODUCT STATE",
            "BOOK-P1 = NOT AUTHORIZED",
            "BOOK MANUSCRIPT EXECUTION = PARKED")) {
        Assert-Condition -Condition $workflow.Contains($requiredText) `
            -Message "Book workflow is missing required contract: $requiredText"
    }

    $prompt = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
            ".github\prompts\canonical\book\operations.prompt.md") -Raw
    foreach ($requiredText in @(
            "No BOOK roadmap decision",
            "No BOOK roadmap decision, editorial acceptance, manuscript prose",
            "A prompt or successful command never self-authorizes a BOOK-P phase")) {
        Assert-Condition -Condition $prompt.Contains($requiredText) `
            -Message "Canonical book prompt is missing safety text: $requiredText"
    }

    $mainVerifier = Get-Content -LiteralPath (Join-Path $RepositoryRoot `
            "tools\agent\verify.ps1") -Raw
    Assert-Condition -Condition (-not $mainVerifier.Contains(
            "book-worktree.ps1")) `
        -Message "Normal product verification must not invoke the real book link."

    $logRoot = Resolve-RepositoryPath -RelativePath $LogDirectory `
        -RequireArtifact
    Assert-NoReparsePathComponents -Root $RepositoryRoot -Path $logRoot `
        -Description "Book operational verification log path"
    [void](New-Item -ItemType Directory -Path $logRoot -Force)
    $testScript = Join-Path $RepositoryRoot `
        "tools\book\tests\book-operations.tests.ps1"
    $output = @(& $PowerShell -NoProfile -File $testScript 2>&1 |
        ForEach-Object { $_.ToString() })
    $exitCode = $LASTEXITCODE
    $logPath = Join-Path $logRoot "book-operations-tests.log"
    [IO.File]::WriteAllText($logPath,
        (($output -join "`n") + "`n"), [Text.UTF8Encoding]::new($false))
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message ("Book operational fixture tests failed with exit code " +
            "$exitCode. See $logPath")
    Assert-Condition -Condition (($output -join "`n").Contains(
            "BOOK-P0-post book operational fixture tests passed.")) `
        -Message "Book fixture tests did not emit their success marker."

    & git --no-optional-locks -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Working-tree whitespace check failed."
    & git --no-optional-locks -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Index whitespace check failed."

    if (-not $Quiet) {
        Write-Host "BOOK-P0-post book operational verification passed."
        Write-Host "Fixture test log: $logPath"
        Write-Host "Real external book availability was not required."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

[CmdletBinding()]
param(
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$TemporaryBase = [IO.Path]::GetFullPath((Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-repository-state-contract"))
$TemporaryRoot = Join-Path $TemporaryBase ([guid]::NewGuid().ToString("N"))
$TemporaryRepository = Join-Path $TemporaryRoot "repository"

. (Join-Path $PSScriptRoot "repository-state.ps1")

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Invoke-TestGit {
    param([Parameter(Mandatory)] [string[]]$Arguments)

    & git -C $TemporaryRepository @Arguments 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Temporary git $($Arguments -join ' ') failed with exit code $LASTEXITCODE."
    }
}

try {
    $actualState = Get-GeoCeDGRepositoryState -RepositoryRoot $RepositoryRoot
    Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace(
            $actualState.LatestIncludedPhase)) `
        -Message "The current checkout did not resolve an included phase."

    [void](New-Item -ItemType Directory -Path $TemporaryRoot -Force)
    & git init --quiet --initial-branch=main $TemporaryRepository
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to initialize the repository-state contract fixture."
    $fixtureRoadmapDirectory = Join-Path $TemporaryRepository "docs\roadmap"
    [void](New-Item -ItemType Directory -Path $fixtureRoadmapDirectory -Force)
    Copy-Item -LiteralPath (Join-Path $RepositoryRoot `
            "docs\roadmap\geocedg_roadmap.md") `
        -Destination (Join-Path $fixtureRoadmapDirectory "geocedg_roadmap.md")
    Invoke-TestGit -Arguments @("add", "docs/roadmap/geocedg_roadmap.md")
    Invoke-TestGit -Arguments @("-c", "user.name=GeoCeDG contract",
        "-c", "user.email=contract@geocedg.invalid", "commit", "--quiet",
        "-m", "repository state contract fixture")

    $mainState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $TemporaryRepository
    Assert-Condition -Condition ($mainState.Branch -eq "main" -and
            -not $mainState.IsDetached) `
        -Message "Repository-state resolution failed on main."

    Invoke-TestGit -Arguments @("switch", "--quiet", "-c",
        "feature/repository-state-contract")
    $branchState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $TemporaryRepository
    Assert-Condition -Condition ($branchState.Branch -eq
            "feature/repository-state-contract" -and
            -not $branchState.IsDetached) `
        -Message "Repository-state resolution failed on a normal work branch."

    Invoke-TestGit -Arguments @("checkout", "--quiet", "--detach", "HEAD")
    $detachedState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $TemporaryRepository
    Assert-Condition -Condition ($detachedState.Branch -eq "detached HEAD" -and
            $detachedState.IsDetached) `
        -Message "Repository-state resolution failed on detached HEAD."

    foreach ($state in @($mainState, $branchState, $detachedState)) {
        Assert-Condition -Condition ($state.Commit -eq $mainState.Commit) `
            -Message "Branch state changed the resolved commit."
        Assert-Condition -Condition ($state.LatestIncludedPhase -eq
                $actualState.LatestIncludedPhase) `
            -Message "Branch state changed the roadmap-derived phase."
    }

    if (-not $Quiet) {
        Write-Host "Repository-state contracts passed: main, work branch, detached HEAD."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if (Test-Path -LiteralPath $TemporaryRoot -PathType Container) {
        $resolvedTemporaryRoot = [IO.Path]::GetFullPath($TemporaryRoot)
        $temporaryPrefix = $TemporaryBase.TrimEnd(
            [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
        if (-not $resolvedTemporaryRoot.StartsWith(
                $temporaryPrefix, [StringComparison]::OrdinalIgnoreCase)) {
            Write-Error "Refusing to remove unexpected contract fixture: $resolvedTemporaryRoot"
            exit 1
        }
        Remove-Item -LiteralPath $resolvedTemporaryRoot -Recurse -Force
    }
}

exit 0

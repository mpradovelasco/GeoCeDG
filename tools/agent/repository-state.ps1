Set-StrictMode -Version Latest

function Invoke-GeoCeDGRepositoryGit {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $output = @(& git -C $RepositoryRoot @Arguments 2>$null)
    $exitCode = $LASTEXITCODE
    if ($exitCode -notin $AllowedExitCodes) {
        throw "git $($Arguments -join ' ') failed with exit code $exitCode."
    }
    return [pscustomobject]@{
        ExitCode = $exitCode
        Output = ($output -join "`n")
    }
}

function Get-GeoCeDGRepositoryState {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$RepositoryRoot)

    $root = (Resolve-Path -LiteralPath $RepositoryRoot).Path
    $commitResult = Invoke-GeoCeDGRepositoryGit -RepositoryRoot $root `
        -Arguments @("rev-parse", "--verify", "HEAD")
    $commit = $commitResult.Output.Trim()
    if ($commit -notmatch '^[0-9a-fA-F]{40}$') {
        throw "Git returned an invalid HEAD commit: $commit"
    }

    $branchResult = Invoke-GeoCeDGRepositoryGit -RepositoryRoot $root `
        -Arguments @("symbolic-ref", "--quiet", "--short", "HEAD") `
        -AllowedExitCodes @(0, 1)
    if ($branchResult.ExitCode -eq 0) {
        $branch = $branchResult.Output.Trim()
        if ([string]::IsNullOrWhiteSpace($branch)) {
            throw "Git reported a symbolic HEAD without a branch name."
        }
        $isDetached = $false
    } else {
        $branch = "detached HEAD"
        $isDetached = $true
    }

    $roadmapPath = Join-Path $root "docs\roadmap\geocedg_roadmap.md"
    if (-not (Test-Path -LiteralPath $roadmapPath -PathType Leaf)) {
        throw "The normative phase roadmap is missing: $roadmapPath"
    }
    $roadmap = Get-Content -Raw -LiteralPath $roadmapPath
    $phaseRows = @([regex]::Matches($roadmap,
            '(?m)^\|\s*Última fase cerrada\s*\|\s*(?<phase>[^|\r\n]+?)\s*\|\s*$'))
    if ($phaseRows.Count -ne 1) {
        throw "The roadmap must contain exactly one 'Última fase cerrada' row."
    }
    $latestPhase = $phaseRows[0].Groups["phase"].Value.Trim()
    $phaseMatch = [regex]::Match($latestPhase,
        '^G(?<number>[0-9]+)(?<suffix>[A-Z][A-Z0-9-]*)?\s+[—-]\s+\S')
    if (-not $phaseMatch.Success) {
        throw "The roadmap has an invalid closed-phase value: $latestPhase"
    }
    $phaseNumber = [int]$phaseMatch.Groups["number"].Value
    $phaseSuffix = $phaseMatch.Groups["suffix"].Value

    return [pscustomobject]@{
        Branch = $branch
        Commit = $commit.ToLowerInvariant()
        IsDetached = $isDetached
        LatestIncludedPhase = $latestPhase
        LatestIncludedPhaseId = "G${phaseNumber}${phaseSuffix}"
        LatestIncludedPhaseNumber = $phaseNumber
        PhaseAuthority = "docs/roadmap/geocedg_roadmap.md"
    }
}

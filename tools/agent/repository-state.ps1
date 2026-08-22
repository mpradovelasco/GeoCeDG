Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

function Invoke-GeoCeDGRepositoryGit {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $output = @(& git --no-optional-locks -C $RepositoryRoot @Arguments 2>$null)
    $exitCode = $LASTEXITCODE
    if ($exitCode -notin $AllowedExitCodes) {
        throw "git $($Arguments -join ' ') failed with exit code $exitCode."
    }
    return [pscustomobject]@{
        ExitCode = $exitCode
        Output = ($output -join "`n")
    }
}

function Get-GeoCeDGMarkdownTableValue {
    param(
        [Parameter(Mandatory)] [string]$Markdown,
        [Parameter(Mandatory)] [string]$Label
    )

    $pattern = '^\|\s*' + [regex]::Escape($Label) +
        '\s*\|\s*(?<value>.*?)\s*\|\s*$'
    $values = @(
        foreach ($line in $Markdown -split "`n") {
            $match = [regex]::Match($line, $pattern)
            if ($match.Success) {
                $match.Groups['value'].Value.Trim()
            }
        }
    )
    if ($values.Count -eq 0) {
        throw "The normative roadmap field is missing: $Label"
    }
    if ($values.Count -ne 1) {
        throw "The normative roadmap field must appear exactly once: $Label"
    }
    return $values[0]
}

function Get-GeoCeDGPhaseSnapshot {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [string]$Revision = 'HEAD'
    )

    $root = (Resolve-Path -LiteralPath $RepositoryRoot).Path
    $commitResult = Invoke-GeoCeDGRepositoryGit -RepositoryRoot $root `
        -Arguments @('rev-parse', '--verify', "${Revision}^{commit}")
    $commit = $commitResult.Output.Trim().ToLowerInvariant()
    if ($commit -notmatch '^[0-9a-f]{40}$') {
        throw "Git returned an invalid commit for ${Revision}: $commit"
    }

    $roadmapRepositoryPath = 'docs/roadmap/geocedg_roadmap.md'
    $roadmap = Get-GeoCeDGFrozenText -RepositoryRoot $root `
        -Path $roadmapRepositoryPath -Commit $commit
    $latestPhase = Get-GeoCeDGMarkdownTableValue -Markdown $roadmap `
        -Label 'Última fase cerrada'
    $phaseMatch = [regex]::Match($latestPhase,
        '^G(?<number>[0-9]+)(?<suffix>[A-Z][A-Z0-9-]*)?\s+[—-]\s+\S')
    if (-not $phaseMatch.Success) {
        throw "The roadmap has an invalid closed-phase value: $latestPhase"
    }
    $phaseNumber = [int]$phaseMatch.Groups['number'].Value
    $phaseSuffix = $phaseMatch.Groups['suffix'].Value

    return [pscustomobject][ordered]@{
        source = $roadmapRepositoryPath
        commit = $commit
        document_version = Get-GeoCeDGMarkdownTableValue -Markdown $roadmap `
            -Label 'Versión documental'
        review_date = Get-GeoCeDGMarkdownTableValue -Markdown $roadmap `
            -Label 'Fecha de revisión'
        current_state = Get-GeoCeDGMarkdownTableValue -Markdown $roadmap `
            -Label 'Estado actual'
        latest_closed_phase = $latestPhase
        latest_executed_phase = Get-GeoCeDGMarkdownTableValue `
            -Markdown $roadmap -Label 'Última fase ejecutada'
        next_gate = Get-GeoCeDGMarkdownTableValue -Markdown $roadmap `
            -Label 'Siguiente puerta'
        latest_closed_phase_id = "G${phaseNumber}${phaseSuffix}"
        latest_closed_phase_number = $phaseNumber
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

    $phase = Get-GeoCeDGPhaseSnapshot -RepositoryRoot $root -Revision $commit

    return [pscustomobject]@{
        Branch = $branch
        Commit = $commit.ToLowerInvariant()
        IsDetached = $isDetached
        LatestIncludedPhase = $phase.latest_closed_phase
        LatestIncludedPhaseId = $phase.latest_closed_phase_id
        LatestIncludedPhaseNumber = $phase.latest_closed_phase_number
        PhaseAuthority = $phase.source
    }
}

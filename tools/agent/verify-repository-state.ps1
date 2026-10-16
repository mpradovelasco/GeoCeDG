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
    $fixtureRoadmapPath = Join-Path $fixtureRoadmapDirectory `
        "geocedg_roadmap.md"
    $committedRoadmap = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
        -Path "docs/roadmap/geocedg_roadmap.md" -Commit $actualState.Commit
    [IO.File]::WriteAllText($fixtureRoadmapPath, $committedRoadmap,
        [Text.UTF8Encoding]::new($false))
    Invoke-TestGit -Arguments @("add", "docs/roadmap/geocedg_roadmap.md")
    Invoke-TestGit -Arguments @("-c", "user.name=GeoCeDG contract",
        "-c", "user.email=contract@geocedg.invalid", "commit", "--quiet",
        "-m", "repository state contract fixture")

    $mainState = Get-GeoCeDGRepositoryState `
        -RepositoryRoot $TemporaryRepository
    $explicitSnapshot = Get-GeoCeDGPhaseSnapshot `
        -RepositoryRoot $TemporaryRepository -Revision $mainState.Commit
    Assert-Condition -Condition (
        $explicitSnapshot.commit -eq $mainState.Commit -and
        $explicitSnapshot.latest_closed_phase -eq $mainState.LatestIncludedPhase) `
        -Message "Explicit-ref phase-state resolution diverged from HEAD."
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

    Invoke-TestGit -Arguments @("switch", "--quiet", "main")
    [IO.File]::AppendAllText($fixtureRoadmapPath,
        "`n| Última fase cerrada | G999 — CONFLICTING FIXTURE |`n",
        [Text.UTF8Encoding]::new($false))
    Invoke-TestGit -Arguments @("add", "docs/roadmap/geocedg_roadmap.md")
    Invoke-TestGit -Arguments @("-c", "user.name=GeoCeDG contract",
        "-c", "user.email=contract@geocedg.invalid", "commit", "--quiet",
        "-m", "conflicting roadmap authority fixture")
    $duplicateRejected = $false
    try {
        [void](Get-GeoCeDGRepositoryState `
                -RepositoryRoot $TemporaryRepository)
    } catch {
        $duplicateRejected = $_.Exception.Message.Contains(
            "must appear exactly once: Última fase cerrada")
    }
    Assert-Condition -Condition $duplicateRejected `
        -Message "Repository-state resolution accepted duplicate phase authority."

    $closedPhaseCases = @(
        @('G1 — TEST `PASS — AUTHOR APPROVED`', 'G1', 1),
        @('G9U0-R2 — PLANNING / DESIGN `PASS — AUTHOR APPROVED`', 'G9U0-R2', 9),
        @('G10P — fixture', 'G10P', 10),
        @('G8 - hyphen separator', 'G8', 8),
        @('`G9U1` — `PASS — AUTHOR APPROVED`', 'G9U1', 9),
        @('`PRE-G9B-R7` — `PASS — AUTHOR APPROVED` (fixture)', 'PRE-G9B-R7', $null),
        @('POST-G9U1-A6 — `PASS — AUTHOR APPROVED`', 'POST-G9U1-A6', $null),
        @('PRE-G9B-S1, incluido R1/R2 — `PASS — AUTHOR APPROVED`', 'PRE-G9B-S1', $null),
        @('PRE-G9B-R2-E0/E1 — `PASS — AUTHOR APPROVED`', 'PRE-G9B-R2-E0/E1', $null),
        @('PRE-G9B-R6-plus PLANNING — `PASS — AUTHOR APPROVED`', 'PRE-G9B-R6-plus', $null),
        @('PRE-G9B-R6-plus-F4 (agregado) — `COMPLETE — AUTHOR APPROVED`',
            'PRE-G9B-R6-plus-F4', $null),
        @('PRE-G9B-R6-plus-E3 con su sucesor PRE-G9B-R6-plus-E3-R1 — `PASS`',
            'PRE-G9B-R6-plus-E3', $null)
    )
    foreach ($case in $closedPhaseCases) {
        $parsed = ConvertFrom-GeoCeDGClosedPhaseValue -Value $case[0]
        Assert-Condition -Condition ($parsed.Id -ceq $case[1] -and
                $parsed.Number -eq $case[2]) `
            -Message "Closed-phase identifier was misparsed: $($case[0])"
    }
    foreach ($value in @('', 'G — fixture', 'g9 — lowercase family', 'G9U1',
            'G9U1 —', 'G9U1x — lowercase suffix', '`G9U1 — unbalanced span',
            '``PRE-G9B-R7`` — double span', 'PRE-G9B-R7',
            'PRE-G9B-R7, sin separador', 'PRE-G9B--R7 — empty segment',
            'PRE-G9B-R7- — trailing hyphen', 'X-G9B-R7 — unknown family',
            'VERIFICATION-INFRASTRUCTURE — registry-only identifier',
            ' G9U1 — leading space')) {
        $rejected = $false
        try {
            [void](ConvertFrom-GeoCeDGClosedPhaseValue -Value $value)
        } catch {
            $rejected = $_.Exception.Message.StartsWith(
                "The roadmap has an invalid closed-phase value:")
        }
        Assert-Condition -Condition $rejected `
            -Message "An invalid closed-phase value was accepted: '$value'"
    }

    foreach ($fixture in @(
            @('`PRE-G9B-R7` — `PASS — AUTHOR APPROVED` (fixture)', 'PRE-G9B-R7'),
            @('Fase sin identificador — fixture', $null))) {
        $fixtureRoadmap = "| Campo | Valor |`n|---|---|`n" +
            "| Versión documental | 1.0 |`n| Fecha de revisión | fixture |`n" +
            "| Estado actual | fixture |`n" +
            "| Última fase cerrada | $($fixture[0]) |`n" +
            "| Última fase ejecutada | fixture |`n" +
            "| Siguiente puerta | fixture |`n"
        [IO.File]::WriteAllText($fixtureRoadmapPath, $fixtureRoadmap,
            [Text.UTF8Encoding]::new($false))
        Invoke-TestGit -Arguments @("add", "docs/roadmap/geocedg_roadmap.md")
        Invoke-TestGit -Arguments @("-c", "user.name=GeoCeDG contract",
            "-c", "user.email=contract@geocedg.invalid", "commit", "--quiet",
            "-m", "closed-phase identifier fixture")
        if ($null -ne $fixture[1]) {
            $trackSnapshot = Get-GeoCeDGPhaseSnapshot `
                -RepositoryRoot $TemporaryRepository
            Assert-Condition -Condition (
                $trackSnapshot.latest_closed_phase -ceq $fixture[0] -and
                $trackSnapshot.latest_closed_phase_id -ceq $fixture[1] -and
                $null -eq $trackSnapshot.latest_closed_phase_number) `
                -Message "A track closed-phase identifier was not resolved."
        } else {
            $invalidRejected = $false
            try {
                [void](Get-GeoCeDGPhaseSnapshot `
                        -RepositoryRoot $TemporaryRepository)
            } catch {
                $invalidRejected = $_.Exception.Message.StartsWith(
                    "The roadmap has an invalid closed-phase value:")
            }
            Assert-Condition -Condition $invalidRejected `
                -Message "Repository-state resolution accepted an invalid closed phase."
        }
    }

    # Published roadmap history: one commit per historical value shape.
    $historicalClosedPhases = @(
        @('80a14805d0f2e06b5c4e2879c2b935b0d2355898', 'G5', 5),
        @('6529a4ebfafa5dc9dca3cc1b4c3e7a89ebcba375', 'G8A', 8),
        @('ce022b756b51fe12497e1932ba3ae58093dd1405', 'G9U0-R2', 9),
        @('5934d706fd9b30ea11b34d6ff0fe293e971cfc3f', 'G10P', 10),
        @('af459d856f1cdc384805f3035203acce8e6f6104', 'G9S1-R1', 9),
        @('678fa49abce28e1f838f67fb2d421c7f9befbfd9', 'G9U1', 9),
        @('866c4a3e38d1e7ef1998b99aba3963fdbcc0b534', 'POST-G9U1-A6', $null),
        @('f502ac8ead5e1b2bf6e70a8583fedfc0a26d9c4a', 'PRE-G9B-S1', $null),
        @('89fab9d1ee09f95f5c21f61bced46a102366b2ef', 'PRE-G9B-R2-E0/E1', $null),
        @('babf20c7c03d0454e2da6760bf5d1fc3fa546f6b', 'PRE-G9B-R6-plus', $null),
        @('31286a2355cabebc69c3937442e7f15636da12ba', 'PRE-G9B-R6-plus-E3', $null),
        @('e70718fec3612814144cd405d46f6c08864ceadd', 'PRE-G9B-R6-plus-F2', $null),
        @('19f8f206b5ed7df8d203b3cebddd59eb1183d589', 'PRE-G9B-R6-plus-F4', $null),
        @('372da89f67c5a3c07100512b6b414a0028319ec5', 'PRE-G9B-R7', $null)
    )
    foreach ($case in $historicalClosedPhases) {
        $historicalRoadmap = Get-GeoCeDGFrozenText -RepositoryRoot $RepositoryRoot `
            -Path "docs/roadmap/geocedg_roadmap.md" -Commit $case[0]
        $parsed = ConvertFrom-GeoCeDGClosedPhaseValue -Value (
            Get-GeoCeDGMarkdownTableValue -Markdown $historicalRoadmap `
                -Label 'Última fase cerrada')
        Assert-Condition -Condition ($parsed.Id -ceq $case[1] -and
                $parsed.Number -eq $case[2]) `
            -Message "Historical closed phase changed meaning at $($case[0])."
    }

    if (-not $Quiet) {
        Write-Host ("Repository-state contracts passed: main, work branch, " +
            "detached HEAD, duplicate authority rejection, closed-phase " +
            "identifier grammar and published history.")
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

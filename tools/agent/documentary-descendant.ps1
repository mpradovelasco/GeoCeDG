#requires -Version 7.2
<#
.SYNOPSIS
    Read-only assessment of a documentary correction committed after a
    technical verification.

.DESCRIPTION
    Given the exact technical commit T whose verification result stands and a
    descendant D that is meant to correct documentation only, the assessment
    proves ancestry and classifies every path changed by T..D:

      EXECUTABLE   product, build, verifier, validation, model, packaging or
                   prompt input, or a path cited by an executable authority of
                   T (product/test sources, canonical checks and modules, the
                   registry-referenced verifier files, release helpers and the
                   operations catalogs);
      NORMATIVE    approved specifications, ADRs and agent authorities, whose
                   change is a decision, not a documentary correction;
      DOCUMENTARY  every other path.

    Classification uses evidence (location and citation by executable
    authority), never the file extension alone. A documentary-only T..D
    keeps the technical result of T: no CORE, PHASE or FINAL rerun is
    required for D. Any other class requires an explicit decision. The script
    runs no verification, writes no receipt and changes no Git state; the
    optional result file is ordinary evidence.

    Exit status: 0 DOCUMENTARY_ONLY, 5 REQUIRES_DECISION, 3 INVALID.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$TechnicalCommit,
    [Parameter(Mandatory)] [string]$DocumentCommit,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../..'),
    [string]$ResultPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
$repository = [IO.Path]::GetFullPath($RepositoryRoot)

$script:ExecutableRoots = [string[]]@('source/', 'gradle/', 'tools/', 'packaging/', 'python/',
    'models/', 'benchmarks/', 'apps/', 'geocedg/validation/', 'geocedg/resources/',
    'geocedg/features/', 'LICENSES/', '.github/', 'ai-shell/')
$script:ExecutableFiles = [string[]]@('settings.gradle.kts', 'build.gradle.kts', 'gradle.properties',
    'gradlew', 'gradlew.bat', '.gitattributes', '.gitignore', '.gitmodules')
$script:NormativeRoots = [string[]]@('geocedg/specs/', 'docs/adr/')
$script:NormativeFiles = [string[]]@('AGENTS.md')
$script:GenericBasenames = [string[]]@('README.md', 'index.md', 'CHANGELOG.md', 'NOTES.md')
# Executable acceptance authorities whose literal citation of a path makes it
# an input: product/test sources, the canonical checks, modules and helpers,
# release helpers, the operations catalogs and every file the registry argv
# names (added at run time). Citations by other tooling (legacy phase
# verifiers, status reporters) are reported as informational only.
$script:CitationPathspecs = [string[]]@('source', 'tools/agent/checks', 'tools/release',
    'tools/agent/verify.ps1', 'tools/agent/*.psm1', 'tools/agent/repository-generated-state.ps1',
    'tools/agent/repository-input-identity.ps1', 'geocedg/specs/operations/*.json')
$script:InformationalPathspecs = [string[]]@('tools')

function Invoke-DocumentaryGit {
    param([string[]]$Arguments, [switch]$AllowFailure)
    $git = Get-Command git -CommandType Application -ErrorAction Stop | Select-Object -First 1
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = [IO.Path]::GetFullPath($git.Path)
    $start.WorkingDirectory = $repository
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    foreach ($argument in @('-c', 'core.quotePath=false', '-C', $repository) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Git did not start.' }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $result = [pscustomobject]@{
            exit_code = $process.ExitCode
            stdout = $stdout.GetAwaiter().GetResult()
            stderr = $stderr.GetAwaiter().GetResult()
        }
        if (-not $AllowFailure -and $result.exit_code -ne 0) {
            throw ("git {0} exited {1}: {2}" -f ($Arguments -join ' '), $result.exit_code, $result.stderr.Trim())
        }
        return $result
    } finally { $process.Dispose() }
}

function Resolve-DocumentaryCommit([string]$Value) {
    $commit = (Invoke-DocumentaryGit @('rev-parse', '--verify', "$Value^{commit}")).stdout.Trim()
    $tree = (Invoke-DocumentaryGit @('rev-parse', '--verify', "$commit^{tree}")).stdout.Trim()
    if ($commit -cnotmatch '^[0-9a-f]{40}$' -or $tree -cnotmatch '^[0-9a-f]{40}$') {
        throw "Commit does not resolve to an exact object: $Value"
    }
    return [pscustomobject]@{ commit = $commit; tree = $tree }
}

function Get-RegistryCitedPaths([string]$Commit) {
    $show = Invoke-DocumentaryGit @('show', "${Commit}:geocedg/specs/operations/verification-registry.json") -AllowFailure
    if ($show.exit_code -ne 0) { return [string[]]@() }
    $paths = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($match in [regex]::Matches($show.stdout, '\{repository_root\}/([A-Za-z0-9_./-]+)')) {
        [void]$paths.Add($match.Groups[1].Value)
    }
    return [string[]]@($paths)
}

function Find-DocumentaryCitations {
    param([string]$Commit, [string]$Needle, [string[]]$Pathspecs)
    $result = Invoke-DocumentaryGit (@('grep', '-l', '-F', '-e', $Needle, $Commit, '--') + $Pathspecs) -AllowFailure
    if ($result.exit_code -eq 1) { return [string[]]@() }
    if ($result.exit_code -ne 0) { throw "git grep failed for citation search: $($result.stderr.Trim())" }
    return [string[]]@($result.stdout.Split("`n", [StringSplitOptions]::RemoveEmptyEntries) |
        ForEach-Object { $_.Trim().Substring($Commit.Length + 1) })
}

function Get-DocumentaryClassification {
    param([string]$Path, [string]$OldMode, [string]$NewMode, [string]$Commit, [string[]]$Pathspecs)
    foreach ($mode in @($OldMode, $NewMode)) {
        if ($mode -cne '000000' -and $mode -cne '100644') {
            return [ordered]@{ classification = 'EXECUTABLE'; reason = "non-regular or executable mode $mode"; cited_by = [string[]]@() }
        }
    }
    foreach ($root in $script:ExecutableRoots) {
        if ($Path.StartsWith($root, [StringComparison]::Ordinal)) {
            return [ordered]@{ classification = 'EXECUTABLE'; reason = "executable root $root"; cited_by = [string[]]@() }
        }
    }
    $leaf = $Path.Split('/')[-1]
    if ($script:ExecutableFiles -ccontains $Path -or $leaf -cmatch '\.gradle(\.kts)?$') {
        return [ordered]@{ classification = 'EXECUTABLE'; reason = 'build or repository configuration'; cited_by = [string[]]@() }
    }
    if ($Path.StartsWith('geocedg/specs/', [StringComparison]::Ordinal) -and $leaf -cnotmatch '\.md$') {
        return [ordered]@{ classification = 'EXECUTABLE'; reason = 'machine-read specification catalog or schema'; cited_by = [string[]]@() }
    }
    if ($leaf -ieq 'claude.md' -or $script:NormativeFiles -ccontains $Path) {
        return [ordered]@{ classification = 'NORMATIVE'; reason = 'agent authority'; cited_by = [string[]]@() }
    }
    foreach ($root in $script:NormativeRoots) {
        if ($Path.StartsWith($root, [StringComparison]::Ordinal)) {
            return [ordered]@{ classification = 'NORMATIVE'; reason = "normative root $root"; cited_by = [string[]]@() }
        }
    }
    $cited = [Collections.Generic.SortedSet[string]]::new([StringComparer]::Ordinal)
    foreach ($hit in Find-DocumentaryCitations -Commit $Commit -Needle $Path -Pathspecs $Pathspecs) { [void]$cited.Add($hit) }
    if ($script:GenericBasenames -cnotcontains $leaf -and $leaf.Length -ge 8) {
        foreach ($hit in Find-DocumentaryCitations -Commit $Commit -Needle $leaf -Pathspecs $Pathspecs) { [void]$cited.Add($hit) }
    }
    if ($cited.Count -gt 0) {
        return [ordered]@{ classification = 'EXECUTABLE'; reason = 'cited by an executable authority'; cited_by = [string[]]@($cited) }
    }
    $informational = @(Find-DocumentaryCitations -Commit $Commit -Needle $Path -Pathspecs $script:InformationalPathspecs)
    return [ordered]@{ classification = 'DOCUMENTARY'; reason = 'not an executable or normative input'
        cited_by = [string[]]@(); informational_citations = [string[]]$informational }
}

$assessment = [ordered]@{
    schema_version = 1
    kind = 'GEOCEDG_DOCUMENTARY_DESCENDANT_ASSESSMENT'
    technical_commit = $null; technical_tree = $null
    document_commit = $null; document_tree = $null
    ancestry_verified = $false
    changed_paths = [object[]]@()
    verdict = 'INVALID'
    cause = $null
    technical_result_reusable_for = $null
    rerun_required = $null
}
$exitCode = 3
try {
    $technical = Resolve-DocumentaryCommit $TechnicalCommit
    $document = Resolve-DocumentaryCommit $DocumentCommit
    $assessment.technical_commit = $technical.commit; $assessment.technical_tree = $technical.tree
    $assessment.document_commit = $document.commit; $assessment.document_tree = $document.tree
    if ($technical.commit -ceq $document.commit) { throw 'The documentary commit must be a strict descendant of the technical commit.' }
    $ancestor = Invoke-DocumentaryGit @('merge-base', '--is-ancestor', $technical.commit, $document.commit) -AllowFailure
    if ($ancestor.exit_code -ne 0) { throw 'The technical commit is not an ancestor of the documentary commit.' }
    $assessment.ancestry_verified = $true

    $pathspecs = [Collections.Generic.List[string]]::new()
    foreach ($spec in $script:CitationPathspecs) { $pathspecs.Add($spec) }
    foreach ($cited in Get-RegistryCitedPaths $technical.commit) { $pathspecs.Add($cited) }
    $raw = (Invoke-DocumentaryGit @('diff-tree', '-r', '--no-renames', '--raw', '-z',
        $technical.commit, $document.commit, '--')).stdout
    $fields = $raw.Split([char]0)
    $changes = [Collections.Generic.List[object]]::new()
    for ($i = 0; $i + 1 -lt $fields.Length; $i += 2) {
        if ($fields[$i] -cnotmatch '^:(\d{6}) (\d{6}) [0-9a-f]+ [0-9a-f]+ ([A-Z])') { continue }
        $oldMode = $Matches[1]; $newMode = $Matches[2]; $status = $Matches[3]
        $path = $fields[$i + 1]
        $class = Get-DocumentaryClassification -Path $path -OldMode $oldMode -NewMode $newMode `
            -Commit $technical.commit -Pathspecs ([string[]]$pathspecs.ToArray())
        $record = [ordered]@{ path = $path; status = $status; classification = $class.classification
            reason = $class.reason; cited_by = $class.cited_by }
        if ($class.Contains('informational_citations') -and @($class.informational_citations).Count -gt 0) {
            $record.informational_citations = $class.informational_citations
        }
        $changes.Add($record)
    }
    if ($changes.Count -eq 0) { throw 'The documentary commit changes no tracked path.' }
    $assessment.changed_paths = [object[]]$changes.ToArray()
    if (@($changes | Where-Object { $_.classification -cne 'DOCUMENTARY' }).Count -eq 0) {
        $assessment.verdict = 'DOCUMENTARY_ONLY'
        $assessment.technical_result_reusable_for = $technical.commit
        $assessment.rerun_required = $false
        $exitCode = 0
    } else {
        $assessment.verdict = 'REQUIRES_DECISION'
        $assessment.cause = 'At least one changed path is an executable or normative input.'
        $assessment.rerun_required = $null
        $exitCode = 5
    }
} catch {
    $assessment.cause = $_.Exception.Message
    $exitCode = 3
}

$json = (ConvertTo-Json -InputObject $assessment -Depth 8).Replace("`r`n", "`n") + "`n"
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    $full = [IO.Path]::GetFullPath($ResultPath)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
    [IO.File]::WriteAllText($full, $json, $utf8)
}
Write-Output $json
exit $exitCode

#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$SafetyResultPath,
    [string]$DiagnosticResultPath,
    [string]$BaselinePath,
    [string[]]$DeclaredWriteRoots = @(),
    [ValidateSet('REPOSITORY', 'PACKAGING_BASELINE', 'PACKAGING')]
    [string]$ContractMode = 'REPOSITORY'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
Import-Module (Join-Path $PSScriptRoot '../verification-packaging-state.psm1')
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1')

function Write-Result {
    param([string]$Path, [object]$Value)
    $full = [IO.Path]::GetFullPath($Path)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
    $json = (ConvertTo-Json -InputObject $Value -Depth 20).Replace("`r`n", "`n") + "`n"
    [IO.File]::WriteAllText($full, $json, $utf8)
}

function Invoke-GitRead {
    param([string[]]$Arguments)
    $git = Get-Command git -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $git) { throw 'Git executable is unavailable.' }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = [IO.Path]::GetFullPath($git.Path)
    $start.WorkingDirectory = [IO.Path]::GetFullPath($RepositoryRoot)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in $Arguments) { [void]$start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Git process did not start.' }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        return [pscustomobject]@{
            exit_code = $process.ExitCode
            stdout = $stdout.GetAwaiter().GetResult()
            stderr = $stderr.GetAwaiter().GetResult()
            executable = $start.FileName
            argv = [string[]]$Arguments
        }
    } finally {
        $process.Dispose()
    }
}

function Get-PackagingRepositorySnapshot {
    param([string[]]$WriteRoots)
    $top = Invoke-GitRead @('rev-parse', '--show-toplevel')
    $head = Invoke-GitRead @('rev-parse', 'HEAD')
    $tree = Invoke-GitRead @('rev-parse', 'HEAD^{tree}')
    $status = Invoke-GitRead @('status', '--porcelain=v1', '--untracked-files=all')
    foreach ($run in @($top, $head, $tree, $status)) {
        if ($run.exit_code -ne 0) { throw 'Git repository-state inspection failed.' }
    }
    $reportedRoot = $top.stdout.TrimEnd("`r", "`n")
    $expectedRoot = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    $actualRoot = [IO.Path]::GetFullPath($reportedRoot).TrimEnd('\', '/')
    if (-not $actualRoot.Equals($expectedRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Git resolved a different repository root.'
    }
    $state = Get-VerificationPackagingStateSnapshot -RepositoryRoot $actualRoot `
        -DeclaredWriteRoots $WriteRoots
    return [ordered]@{
        schema_version = 1
        evidence_kind = 'PACKAGING_REPOSITORY_BASELINE'
        evidence_state = 'PRESENT'
        cause = $null
        repository_root = $actualRoot
        git_toplevel = $reportedRoot
        head = $head.stdout.TrimEnd("`r", "`n")
        tree = $tree.stdout.TrimEnd("`r", "`n")
        status = $status.stdout.Replace("`r`n", "`n").TrimEnd("`n")
        declared_write_roots = $state.declared_write_roots
        declared_state_sha256 = $state.declared_state_sha256
        declared_file_count = $state.declared_file_count
        protected_state_sha256 = $state.protected_state_sha256
        protected_file_count = $state.protected_file_count
    }
}

if ($ContractMode -ceq 'PACKAGING_BASELINE') {
    try {
        $baseline = Get-PackagingRepositorySnapshot -WriteRoots $DeclaredWriteRoots
    } catch {
        $baseline = [ordered]@{
            schema_version = 1
            evidence_kind = 'PACKAGING_REPOSITORY_BASELINE'
            evidence_state = 'UNTRUSTED'
            cause = $_.Exception.Message
            repository_root = [IO.Path]::GetFullPath($RepositoryRoot)
            git_toplevel = $null
            head = $null
            tree = $null
            status = $null
            declared_write_roots = $null
            declared_state_sha256 = $null
            declared_file_count = $null
            protected_state_sha256 = $null
            protected_file_count = $null
        }
    }
    Write-Result $SafetyResultPath $baseline
    if ($baseline.evidence_state -ceq 'PRESENT') { exit 0 }
    exit 3
}

$safety = [ordered]@{
    contract_class = 'SAFETY'
    outcome = 'EVIDENCE_UNTRUSTED'
    cause = $null
    check_kind = $(if ($ContractMode -ceq 'PACKAGING') {
        'PACKAGING_REPOSITORY_PRESERVATION'
    } else { 'REPOSITORY_BOUNDARY' })
    repository_root = [IO.Path]::GetFullPath($RepositoryRoot)
    git_toplevel = $null
    subcontracts = @()
}
$diagnostic = [ordered]@{
    contract_class = 'STYLE_DIAGNOSTIC'
    outcome = 'DIAGNOSTIC_UNAVAILABLE'
    cause = $null
    working_tree_output = ''
    index_output = ''
}

try {
    $top = Invoke-GitRead @('rev-parse', '--show-toplevel')
    if ($top.exit_code -ne 0) {
        $safety.cause = "Git repository-root inspection exited $($top.exit_code)."
    } else {
        $reportedRoot = $top.stdout.TrimEnd("`r", "`n")
        $safety.git_toplevel = $reportedRoot
        $expected = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
        $actual = [IO.Path]::GetFullPath($reportedRoot).TrimEnd('\', '/')
        if ($actual.Equals($expected, [StringComparison]::OrdinalIgnoreCase)) {
            $safety.outcome = 'CONTRACT_SATISFIED'
        } else {
            $safety.outcome = 'CONTRACT_VIOLATED'
            $safety.cause = 'Git resolved a different repository root.'
        }
    }

    if ($ContractMode -ceq 'PACKAGING') {
        $subcontracts = [Collections.Generic.List[object]]::new()
        if ([string]::IsNullOrWhiteSpace($BaselinePath) -or
                -not (Test-Path -LiteralPath $BaselinePath -PathType Leaf)) {
            $subcontracts.Add([ordered]@{
                contract_id = 'packaging.repository-baseline'
                status = 'EVIDENCE_UNTRUSTED'
                expected = 'canonical pre-execution repository baseline'
                observed = $BaselinePath
                cause = 'The canonical runner did not provide packaging repository-state evidence.'
            })
        } else {
            try {
                $baselineText = [IO.File]::ReadAllText(
                    [IO.Path]::GetFullPath($BaselinePath),
                    [Text.UTF8Encoding]::new($false, $true))
                $baseline = $baselineText | ConvertFrom-Json -Depth 30
                if ([string]$baseline.evidence_kind -cne 'PACKAGING_REPOSITORY_BASELINE' -or
                        [string]$baseline.evidence_state -cne 'PRESENT') {
                    throw 'Packaging baseline evidence is not complete and trusted.'
                }
                $roots = Resolve-VerificationPackagingWriteRoots -RepositoryRoot $RepositoryRoot `
                    -DeclaredWriteRoots $DeclaredWriteRoots
                $baselineRoots = Resolve-VerificationPackagingWriteRoots -RepositoryRoot $RepositoryRoot `
                    -DeclaredWriteRoots ([string[]]@($baseline.declared_write_roots))
                if ((ConvertTo-VerificationCanonicalJson -Value $roots) -cne
                        (ConvertTo-VerificationCanonicalJson -Value $baselineRoots)) {
                    throw 'Packaging baseline declared write roots differ from the current contract.'
                }
                if ([string]::IsNullOrWhiteSpace([string]$baseline.protected_state_sha256) -or
                        $null -eq $baseline.protected_file_count) {
                    throw 'Packaging baseline lacks protected repository-state evidence.'
                }
                $current = Get-PackagingRepositorySnapshot -WriteRoots $roots
                $identityEqual = [string]$baseline.repository_root -ceq [string]$current.repository_root -and
                    [string]$baseline.git_toplevel -ceq [string]$current.git_toplevel -and
                    [string]$baseline.head -ceq [string]$current.head -and
                    [string]$baseline.tree -ceq [string]$current.tree
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.repository-identity-preserved'
                    status = $(if ($identityEqual) { 'SATISFIED' } else { 'VIOLATED' })
                    expected = [ordered]@{ repository_root=$baseline.repository_root; git_toplevel=$baseline.git_toplevel; head=$baseline.head; tree=$baseline.tree }
                    observed = [ordered]@{ repository_root=$current.repository_root; git_toplevel=$current.git_toplevel; head=$current.head; tree=$current.tree }
                    cause = $(if ($identityEqual) { $null } else {
                        'Packaging verification changed or escaped the repository identity.'
                    })
                })
                $currentStatus = [string]$current.status
                $statusEqual = $currentStatus -ceq [string]$baseline.status
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.worktree-preserved'
                    status = $(if ($statusEqual) { 'SATISFIED' } else { 'VIOLATED' })
                    expected = [string]$baseline.status
                    observed = $currentStatus
                    cause = $(if ($statusEqual) { $null } else {
                        'Packaging verification changed the repository worktree or index.'
                    })
                })
                $protectedHash = [string]$current.protected_state_sha256
                $protectedEqual = $protectedHash -ceq [string]$baseline.protected_state_sha256
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.generated-state-preserved'
                    status = $(if ($protectedEqual) { 'SATISFIED' } else { 'VIOLATED' })
                    expected = [string]$baseline.protected_state_sha256
                    observed = $protectedHash
                    cause = $(if ($protectedEqual) { $null } else {
                        'Packaging verification changed repository state outside declared producer roots.'
                    })
                })
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.declared-write-scope'
                    status = 'SATISFIED'
                    expected = $roots
                    observed = [string[]]$current.declared_write_roots
                    cause = $null
                })
            } catch {
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.repository-baseline'
                    status = 'EVIDENCE_UNTRUSTED'
                    expected = 'valid UTF-8 JSON baseline'
                    observed = $BaselinePath
                    cause = $_.Exception.Message
                })
            }
        }
        $safety.subcontracts = [object[]]$subcontracts.ToArray()
        if (@($safety.subcontracts | Where-Object status -CEQ 'EVIDENCE_UNTRUSTED').Count -gt 0) {
            $safety.outcome = 'EVIDENCE_UNTRUSTED'
            $safety.cause = 'Packaging repository-state evidence is incomplete or malformed.'
        } elseif (@($safety.subcontracts | Where-Object status -CEQ 'VIOLATED').Count -gt 0) {
            $safety.outcome = 'CONTRACT_VIOLATED'
            $safety.cause = 'Packaging verification did not preserve repository state.'
        }
    } else {
        $working = Invoke-GitRead @('diff', '--check')
        $index = Invoke-GitRead @('diff', '--cached', '--check')
        $diagnostic.working_tree_output = $working.stdout + $working.stderr
        $diagnostic.index_output = $index.stdout + $index.stderr
        if ($working.exit_code -eq 0 -and $index.exit_code -eq 0) {
            $diagnostic.outcome = 'DIAGNOSTIC_CLEAR'
        } else {
            $diagnostic.outcome = 'DIAGNOSTIC_FINDING'
            $diagnostic.cause = 'Git reported whitespace findings.'
        }
    }
} catch {
    if ([string]::IsNullOrWhiteSpace([string]$safety.cause)) {
        $safety.cause = $_.Exception.Message
    }
    $diagnostic.cause = $_.Exception.Message
}

Write-Result $SafetyResultPath $safety
if ($ContractMode -ceq 'REPOSITORY') {
    if ([string]::IsNullOrWhiteSpace($DiagnosticResultPath)) {
        throw 'DiagnosticResultPath is required in REPOSITORY mode.'
    }
    Write-Result $DiagnosticResultPath $diagnostic
}
if ($safety.outcome -ceq 'CONTRACT_VIOLATED') { exit 2 }
if ($safety.outcome -ceq 'EVIDENCE_UNTRUSTED') { exit 3 }
if ($ContractMode -ceq 'REPOSITORY' -and
        $diagnostic.outcome -ceq 'DIAGNOSTIC_FINDING') { exit 7 }
exit 0

#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$SafetyResultPath,
    [string]$DiagnosticResultPath,
    [ValidateSet('REPOSITORY', 'PACKAGING')]
    [string]$ContractMode = 'REPOSITORY'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8

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

function Get-PackagingGeneratedState {
    param([string]$Root)
    $records = [Collections.Generic.List[object]]::new()
    foreach ($name in @('build', '.gradle', '.kotlin')) {
        $container = Join-Path $Root $name
        if (-not (Test-Path -LiteralPath $container -PathType Container)) { continue }
        foreach ($file in Get-ChildItem -LiteralPath $container -File -Recurse -Force |
                Sort-Object FullName) {
            $records.Add([ordered]@{
                bytes = [long]$file.Length
                path = [IO.Path]::GetRelativePath($Root, $file.FullName).Replace('\', '/')
                sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            })
        }
    }
    return [object[]]$records.ToArray()
}

function Get-CanonicalStateHash {
    param([object]$Value)
    $json = ConvertTo-Json -InputObject $Value -Depth 20 -Compress
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
            $utf8.GetBytes($json))).ToLowerInvariant()
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
        $baselinePath = [Environment]::GetEnvironmentVariable(
            'GEOCEDG_PACKAGING_BASELINE_PATH')
        if ([string]::IsNullOrWhiteSpace($baselinePath) -or
                -not (Test-Path -LiteralPath $baselinePath -PathType Leaf)) {
            $subcontracts.Add([ordered]@{
                contract_id = 'packaging.repository-baseline'
                status = 'EVIDENCE_UNTRUSTED'
                expected = 'canonical pre-execution repository baseline'
                observed = $baselinePath
                cause = 'The canonical runner did not provide packaging repository-state evidence.'
            })
        } else {
            try {
                $baselineText = [IO.File]::ReadAllText(
                    [IO.Path]::GetFullPath($baselinePath),
                    [Text.UTF8Encoding]::new($false, $true))
                $baseline = $baselineText | ConvertFrom-Json -Depth 30
                $statusRun = Invoke-GitRead @('status', '--porcelain=v1', '--untracked-files=all')
                if ($statusRun.exit_code -ne 0) {
                    throw "Git status inspection exited $($statusRun.exit_code)."
                }
                $currentStatus = $statusRun.stdout.Replace("`r`n", "`n").TrimEnd("`n")
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
                $generated = Get-PackagingGeneratedState -Root $safety.repository_root
                $generatedHash = Get-CanonicalStateHash -Value $generated
                $generatedEqual = $generatedHash -ceq [string]$baseline.generated_state_sha256
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.generated-state-preserved'
                    status = $(if ($generatedEqual) { 'SATISFIED' } else { 'VIOLATED' })
                    expected = [string]$baseline.generated_state_sha256
                    observed = $generatedHash
                    cause = $(if ($generatedEqual) { $null } else {
                        'Packaging verification changed generated repository state.'
                    })
                })
            } catch {
                $subcontracts.Add([ordered]@{
                    contract_id = 'packaging.repository-baseline'
                    status = 'EVIDENCE_UNTRUSTED'
                    expected = 'valid UTF-8 JSON baseline'
                    observed = $baselinePath
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

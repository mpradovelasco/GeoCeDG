#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$SafetyResultPath,
    [Parameter(Mandatory)] [string]$DiagnosticResultPath
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

$safety = [ordered]@{
    contract_class = 'SAFETY'
    outcome = 'EVIDENCE_UNTRUSTED'
    cause = $null
    repository_root = [IO.Path]::GetFullPath($RepositoryRoot)
    git_toplevel = $null
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
} catch {
    if ([string]::IsNullOrWhiteSpace([string]$safety.cause)) {
        $safety.cause = $_.Exception.Message
    }
    $diagnostic.cause = $_.Exception.Message
}

Write-Result $SafetyResultPath $safety
Write-Result $DiagnosticResultPath $diagnostic
if ($safety.outcome -ceq 'CONTRACT_VIOLATED') { exit 2 }
if ($safety.outcome -ceq 'EVIDENCE_UNTRUSTED') { exit 3 }
if ($diagnostic.outcome -ceq 'DIAGNOSTIC_FINDING') { exit 7 }
exit 0

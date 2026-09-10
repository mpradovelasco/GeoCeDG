#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$EvidencePath,
    [Parameter(Mandatory)] [string]$ScriptPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..'),
    [string]$OutputDirectory = (Join-Path ([IO.Path]::GetTempPath()) ('geocedg-python-check-' + [guid]::NewGuid().ToString('N'))),
    [string]$EnvironmentPrefix
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
$utf8Strict = [Text.UTF8Encoding]::new($false, $true)
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$scriptFull = [IO.Path]::GetFullPath((Join-Path $repository $ScriptPath))
$output = [IO.Path]::GetFullPath($OutputDirectory)
[void][IO.Directory]::CreateDirectory($output)
$identityPath = Join-Path $output 'python-identity.json'
$identityStdoutPath = Join-Path $output 'identity.stdout.log'
$identityStderrPath = Join-Path $output 'identity.stderr.log'
$stdoutPath = Join-Path $output 'check.stdout.log'
$stderrPath = Join-Path $output 'check.stderr.log'
$inventoryPath = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.conda/environments.txt'
$probe = @'
import json, os, pathlib, platform, sys
import mpmath
path = pathlib.Path(sys.argv[1])
value = {
    "python_implementation": platform.python_implementation(),
    "python_version": platform.python_version(),
    "executable": sys.executable,
    "sys_prefix": sys.prefix,
    "conda_prefix": os.environ.get("CONDA_PREFIX"),
    "conda_default_env": os.environ.get("CONDA_DEFAULT_ENV"),
    "mpmath_version": mpmath.__version__,
    "mpmath_file": mpmath.__file__,
}
path.write_text(json.dumps(value, ensure_ascii=False, sort_keys=True) + "\n", encoding="utf-8")
'@

function Test-ContainedPath {
    param([string]$Root, [string]$Candidate)
    $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd('\', '/')
    $candidateFull = [IO.Path]::GetFullPath($Candidate)
    return $candidateFull.StartsWith(
        $rootFull + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase)
}

function Get-CedgEnvironmentInventory {
    if (-not (Test-Path -LiteralPath $inventoryPath -PathType Leaf)) {
        throw 'The read-only Conda environment inventory is unavailable.'
    }
    $values = [Collections.Generic.List[string]]::new()
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($line in [IO.File]::ReadAllLines($inventoryPath, $utf8Strict)) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $candidate = [IO.Path]::GetFullPath($line.Trim())
        if ([IO.Path]::GetFileName($candidate) -cne 'cedg_env') { continue }
        if ($seen.Add($candidate)) { $values.Add($candidate) }
    }
    return [string[]]$values.ToArray()
}

function Resolve-CedgEnvironmentPrefix {
    $registered = [string[]]@(Get-CedgEnvironmentInventory)
    $requested = $null
    if (-not [string]::IsNullOrWhiteSpace($EnvironmentPrefix)) {
        $requested = [IO.Path]::GetFullPath($EnvironmentPrefix)
    } elseif ([Environment]::GetEnvironmentVariable('CONDA_DEFAULT_ENV') -ceq 'cedg_env' -and
            -not [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable('CONDA_PREFIX'))) {
        $requested = [IO.Path]::GetFullPath([Environment]::GetEnvironmentVariable('CONDA_PREFIX'))
    }
    if ($null -ne $requested) {
        if (-not ($registered -contains $requested)) {
            throw 'The requested cedg_env prefix is absent from the Conda inventory.'
        }
        $candidates = @($requested)
    } else {
        $candidates = @($registered)
        [Array]::Reverse($candidates)
    }
    foreach ($candidate in $candidates) {
        if ([IO.Path]::GetFileName($candidate) -cne 'cedg_env') { continue }
        $python = Join-Path $candidate 'python.exe'
        $history = Join-Path $candidate 'conda-meta/history'
        if ((Test-Path -LiteralPath $python -PathType Leaf) -and
                (Test-Path -LiteralPath $history -PathType Leaf) -and
                (Test-ContainedPath $candidate $python)) {
            return [pscustomobject]@{
                prefix = $candidate
                python = [IO.Path]::GetFullPath($python)
                inventory_path = [IO.Path]::GetFullPath($inventoryPath)
            }
        }
    }
    throw 'No valid registered cedg_env prefix contains Python and Conda metadata.'
}

function Invoke-PythonEvidence {
    param([string]$Python, [string]$Prefix, [string[]]$Arguments,
        [string]$StdoutPath, [string]$StderrPath)
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $Python
    $start.WorkingDirectory = $repository
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    $start.Environment['CONDA_PREFIX'] = $Prefix
    $start.Environment['CONDA_DEFAULT_ENV'] = 'cedg_env'
    $start.Environment['PATH'] = $Prefix + [IO.Path]::PathSeparator + [string]$start.Environment['PATH']
    foreach ($argument in $Arguments) { [void]$start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Python process did not start.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [IO.File]::WriteAllText($StdoutPath, $stdoutTask.GetAwaiter().GetResult(), $utf8)
        [IO.File]::WriteAllText($StderrPath, $stderrTask.GetAwaiter().GetResult(), $utf8)
        return [pscustomobject]@{ exit_code = $process.ExitCode }
    } finally { $process.Dispose() }
}

$identityArgs = @('-c', $probe, $identityPath)
$checkArgs = @($scriptFull, '--check')
$state = 'LAUNCH_FAILED'
$cause = $null
$exitCode = $null
$identityExitCode = $null
$resolved = $null
$identityValue = $null
$identityState = 'ABSENT'
$started = [datetime]::UtcNow
$stopwatch = [Diagnostics.Stopwatch]::StartNew()
try {
    $resolved = Resolve-CedgEnvironmentPrefix
    $identityRun = Invoke-PythonEvidence $resolved.python $resolved.prefix `
        $identityArgs $identityStdoutPath $identityStderrPath
    $identityExitCode = $identityRun.exit_code
    $state = 'COMPLETED'
    if (Test-Path -LiteralPath $identityPath -PathType Leaf) {
        try {
            $identityValue = [IO.File]::ReadAllText($identityPath, $utf8Strict) |
                ConvertFrom-Json -Depth 20
            $identityState = 'PRESENT'
        } catch {
            $identityState = 'MALFORMED'
            $cause = $_.Exception.Message
        }
    }
    if ($identityExitCode -eq 0 -and $identityState -ceq 'PRESENT') {
        $checkRun = Invoke-PythonEvidence $resolved.python $resolved.prefix `
            $checkArgs $stdoutPath $stderrPath
        $exitCode = $checkRun.exit_code
    }
} catch { $cause = $_.Exception.Message }
$stopwatch.Stop()
foreach ($path in @($identityStdoutPath, $identityStderrPath, $stdoutPath, $stderrPath)) {
    if (-not (Test-Path -LiteralPath $path)) { [IO.File]::WriteAllText($path, '', $utf8) }
}
$e = [ordered]@{
    schema_version = 2
    producer_kind = 'PYTHON_CHECK'
    completion_state = $state
    completion_cause = $cause
    command = $(if ($null -eq $resolved) { $null } else { $resolved.python })
    arguments = $checkArgs
    identity_arguments = $identityArgs
    working_directory = $repository
    environment_name = 'cedg_env'
    environment_prefix = $(if ($null -eq $resolved) { $null } else { $resolved.prefix })
    conda_inventory_path = [IO.Path]::GetFullPath($inventoryPath)
    conda_inventory_member = $null -ne $resolved
    script = $scriptFull
    identity_exit_code = $identityExitCode
    identity_state = $identityState
    identity = $identityValue
    inner_exit_code = $exitCode
    identity_stdout_log = $identityStdoutPath
    identity_stderr_log = $identityStderrPath
    stdout_log = $stdoutPath
    stderr_log = $stderrPath
    duration_ms = $stopwatch.Elapsed.TotalMilliseconds
    started_at = $started.ToString('o')
    finished_at = [datetime]::UtcNow.ToString('o')
}
$full = [IO.Path]::GetFullPath($EvidencePath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $e -Depth 30).Replace("`r`n", "`n") + "`n"), $utf8)
exit 0

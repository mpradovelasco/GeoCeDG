#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$EvidencePath,
    [Parameter(Mandatory)] [string]$ScriptPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..'),
    [string]$OutputDirectory = (Join-Path ([IO.Path]::GetTempPath()) ('geocedg-python-check-' + [guid]::NewGuid().ToString('N')))
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8=[Text.UTF8Encoding]::new($false)
$repository=[IO.Path]::GetFullPath($RepositoryRoot)
$scriptFull=[IO.Path]::GetFullPath((Join-Path $repository $ScriptPath))
$output=[IO.Path]::GetFullPath($OutputDirectory); [void][IO.Directory]::CreateDirectory($output)
$identityPath=Join-Path $output 'python-identity.json'
$identityStdoutPath=Join-Path $output 'identity.stdout.log';$identityStderrPath=Join-Path $output 'identity.stderr.log'
$stdoutPath=Join-Path $output 'check.stdout.log';$stderrPath=Join-Path $output 'check.stderr.log'
$probe=@'
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
$identityArgs=@('run','--no-capture-output','-n','cedg_env','python','-c',$probe,$identityPath)
$checkArgs=@('run','--no-capture-output','-n','cedg_env','python',$scriptFull,'--check')
$state='LAUNCH_FAILED';$cause=$null;$exitCode=$null;$identityExitCode=$null;$conda=$null
$identityValue=$null;$identityState='ABSENT';$started=[datetime]::UtcNow;$sw=[Diagnostics.Stopwatch]::StartNew()

function Invoke-CondaEvidence {
    param([string[]]$Arguments,[string]$StdoutPath,[string]$StderrPath)
    $start=[Diagnostics.ProcessStartInfo]::new();$start.FileName=$conda;$start.WorkingDirectory=$repository
    $start.UseShellExecute=$false;$start.CreateNoWindow=$true;$start.RedirectStandardOutput=$true;$start.RedirectStandardError=$true
    $start.StandardOutputEncoding=$utf8;$start.StandardErrorEncoding=$utf8
    foreach($argument in $Arguments){[void]$start.ArgumentList.Add($argument)}
    $process=[Diagnostics.Process]::new();$process.StartInfo=$start
    try {
        if(-not $process.Start()){throw 'Conda process did not start.'}
        $stdoutTask=$process.StandardOutput.ReadToEndAsync();$stderrTask=$process.StandardError.ReadToEndAsync()
        $process.WaitForExit();$stdout=$stdoutTask.GetAwaiter().GetResult();$stderr=$stderrTask.GetAwaiter().GetResult()
        [IO.File]::WriteAllText($StdoutPath,$stdout,$utf8);[IO.File]::WriteAllText($StderrPath,$stderr,$utf8)
        return [pscustomobject]@{exit_code=$process.ExitCode}
    } finally {$process.Dispose()}
}

try {
    $conda=(Get-Command conda -CommandType Application -ErrorAction Stop|Select-Object -First 1).Path
    $identityRun=Invoke-CondaEvidence $identityArgs $identityStdoutPath $identityStderrPath
    $identityExitCode=$identityRun.exit_code;$state='COMPLETED'
    if(Test-Path -LiteralPath $identityPath -PathType Leaf){
        try{$identityValue=[IO.File]::ReadAllText($identityPath,[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 20;$identityState='PRESENT'}catch{$identityState='MALFORMED';$cause=$_.Exception.Message}
    }
    if($identityExitCode -eq 0 -and $identityState -ceq 'PRESENT'){
        $checkRun=Invoke-CondaEvidence $checkArgs $stdoutPath $stderrPath;$exitCode=$checkRun.exit_code
    }
} catch {$cause=$_.Exception.Message}
$sw.Stop()
foreach($path in @($identityStdoutPath,$identityStderrPath,$stdoutPath,$stderrPath)){if(-not(Test-Path -LiteralPath $path)){[IO.File]::WriteAllText($path,'',$utf8)}}
$e=[ordered]@{schema_version=1;producer_kind='PYTHON_CHECK';completion_state=$state;completion_cause=$cause;command=$conda;arguments=$checkArgs;identity_arguments=$identityArgs;working_directory=$repository;environment_name='cedg_env';script=$scriptFull;identity_exit_code=$identityExitCode;identity_state=$identityState;identity=$identityValue;inner_exit_code=$exitCode;identity_stdout_log=$identityStdoutPath;identity_stderr_log=$identityStderrPath;stdout_log=$stdoutPath;stderr_log=$stderrPath;duration_ms=$sw.Elapsed.TotalMilliseconds;started_at=$started.ToString('o');finished_at=[datetime]::UtcNow.ToString('o')}
$full=[IO.Path]::GetFullPath($EvidencePath);[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full));[IO.File]::WriteAllText($full,((ConvertTo-Json $e -Depth 30).Replace("`r`n","`n")+"`n"),$utf8)
exit 0

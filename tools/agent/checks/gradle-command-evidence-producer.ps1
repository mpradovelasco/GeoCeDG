#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$EvidencePath,
    [Parameter(Mandatory)] [string]$GradleTask,
    [string[]]$AdditionalArguments = @(),
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..'),
    [string]$OutputDirectory = (Join-Path ([IO.Path]::GetTempPath()) ('geocedg-gradle-command-' + [guid]::NewGuid().ToString('N')))
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$wrapper = Join-Path $repository 'gradlew.bat'
$output = [IO.Path]::GetFullPath($OutputDirectory)
[void][IO.Directory]::CreateDirectory($output)
$stdoutPath = Join-Path $output 'stdout.log'
$stderrPath = Join-Path $output 'stderr.log'
$effective = [Collections.Generic.List[string]]::new()
$effective.Add($GradleTask)
foreach ($argument in $AdditionalArguments) { $effective.Add($argument) }
foreach ($flag in @('--rerun-tasks','--no-build-cache','--no-daemon','--no-problems-report',
        '--console=plain','-Dorg.gradle.java.installations.auto-download=false')) {
    if (-not $effective.Contains($flag)) { $effective.Add($flag) }
}
$started = [datetime]::UtcNow
$stopwatch = [Diagnostics.Stopwatch]::StartNew()
$state = 'LAUNCH_FAILED'; $exitCode = $null; $cause = $null; $stdout = ''; $stderr = ''
try {
    if (-not (Test-Path -LiteralPath $wrapper -PathType Leaf)) { throw "Gradle Wrapper is missing: $wrapper" }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $wrapper
    $start.WorkingDirectory = $repository
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    foreach ($argument in $effective) { [void]$start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new(); $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Gradle process did not start.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        $exitCode = $process.ExitCode
        $state = 'COMPLETED'
    } finally { $process.Dispose() }
} catch { $cause = $_.Exception.Message }
$stopwatch.Stop()
[IO.File]::WriteAllText($stdoutPath, $stdout, $utf8)
[IO.File]::WriteAllText($stderrPath, $stderr, $utf8)
$evidence = [ordered]@{
    schema_version = 1; producer_kind = 'GRADLE_COMMAND'; completion_state = $state
    completion_cause = $cause; command = $wrapper; arguments = [string[]]$effective.ToArray()
    working_directory = $repository; environment = [ordered]@{ toolchain_auto_download = 'false' }
    inner_exit_code = $exitCode; stdout_log = $stdoutPath; stderr_log = $stderrPath
    stdout_sha256 = (Get-FileHash $stdoutPath -Algorithm SHA256).Hash.ToLowerInvariant()
    stderr_sha256 = (Get-FileHash $stderrPath -Algorithm SHA256).Hash.ToLowerInvariant()
    started_at = $started.ToString('o'); finished_at = [datetime]::UtcNow.ToString('o')
    duration_ms = $stopwatch.Elapsed.TotalMilliseconds
}
$full = [IO.Path]::GetFullPath($EvidencePath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $evidence -Depth 30).Replace("`r`n","`n")+"`n"), $utf8)
exit 0

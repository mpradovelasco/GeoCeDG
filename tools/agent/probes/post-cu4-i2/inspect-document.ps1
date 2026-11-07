#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$Source,
    [Parameter(Mandatory)] [string]$Receiver,
    [Parameter(Mandatory)] [string]$Jdk,
    [Parameter(Mandatory)] [string]$Classpath,
    [Parameter(Mandatory)] [string]$OutputDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$sourceFile = (Resolve-Path -LiteralPath $Source).Path
$receiverFile = (Resolve-Path -LiteralPath $Receiver).Path
$java = (Resolve-Path -LiteralPath (Join-Path $Jdk 'bin/java.exe')).Path
$classEntries = @($Classpath.Split([IO.Path]::PathSeparator) | ForEach-Object {
    [IO.Path]::GetFullPath($_)
})
$output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $output) { throw "Output directory already exists: $output" }
$scratch = Join-Path $output 'worker'
[void][IO.Directory]::CreateDirectory($scratch)

$start = [Diagnostics.ProcessStartInfo]::new()
$start.FileName = $java
$start.WorkingDirectory = $scratch
$start.UseShellExecute = $false
$start.CreateNoWindow = $true
$start.RedirectStandardOutput = $true
$start.RedirectStandardError = $true
$start.StandardOutputEncoding = [Text.Encoding]::UTF8
$start.StandardErrorEncoding = [Text.Encoding]::UTF8
foreach ($variable in @('JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS')) {
    [void]$start.Environment.Remove($variable)
}
$start.Environment['APPDATA'] = $scratch
$start.Environment['LOCALAPPDATA'] = $scratch
$argv = @('-Xmx512m', '-Dfile.encoding=UTF-8', "-Duser.home=$scratch",
    "-Djava.io.tmpdir=$scratch", '--add-exports=java.base/java.lang=ALL-UNNAMED',
    '--add-exports=java.desktop/sun.awt=ALL-UNNAMED',
    '--add-exports=java.desktop/sun.java2d=ALL-UNNAMED',
    '--enable-native-access=ALL-UNNAMED', '-cp',
    ($classEntries -join [IO.Path]::PathSeparator),
    'org.geocedg.desktop.GeoCeDGExpertInspection', $sourceFile, $receiverFile,
    (Join-Path $scratch 'portable.properties'), (Join-Path $output 'report.json'))
foreach ($argument in $argv) { $start.ArgumentList.Add($argument) }

$process = [Diagnostics.Process]::new()
$process.StartInfo = $start
$state = 'LAUNCH_FAILED'
$code = 2
try {
    if (-not $process.Start()) { throw 'Cannot launch the Desktop inspection worker.' }
    $stdout = $process.StandardOutput.ReadToEndAsync()
    $stderr = $process.StandardError.ReadToEndAsync()
    if ($process.WaitForExit(60000)) {
        $code = $process.ExitCode
        if ($code -eq 0 -and (Test-Path -LiteralPath (Join-Path $output 'report.json') -PathType Leaf)) {
            $state = 'COMPLETED'
        } else {
            $state = 'TECHNICAL_FAILURE'
            if ($code -eq 0) { $code = 2 }
        }
    } else {
        $process.Kill($true)
        $process.WaitForExit()
        $state = 'TIME_LIMIT'
    }
    [IO.File]::WriteAllText((Join-Path $output 'stdout.log'), $stdout.GetAwaiter().GetResult())
    [IO.File]::WriteAllText((Join-Path $output 'stderr.log'), $stderr.GetAwaiter().GetResult())
} finally {
    $process.Dispose()
    [ordered]@{ state = $state; exit_code = $code; worker = $java; arguments = $argv } |
        ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $output 'invocation.json')
    $resolvedScratch = (Resolve-Path -LiteralPath $scratch).Path
    if ($resolvedScratch -ceq (Join-Path $output 'worker')) {
        Remove-Item -LiteralPath $resolvedScratch -Recurse -Force
    }
}
Get-Content -LiteralPath (Join-Path $output 'stdout.log')
exit $code

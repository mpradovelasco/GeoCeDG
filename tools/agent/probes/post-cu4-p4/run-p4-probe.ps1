#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$Jdk,
    [Parameter(Mandatory)] [string]$ClasspathArgs,
    [Parameter(Mandatory)] [string]$BuildEvidence,
    [Parameter(Mandatory)] [ValidatePattern('^run-[0-9]+$')] [string]$Run,
    [switch]$PortablePreferences
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..'))
Set-Location -LiteralPath $repo
$headCommit, $headTree = git rev-parse HEAD 'HEAD^{tree}'
if ($LASTEXITCODE -ne 0) { throw 'Cannot resolve the source commit.' }
$trackedBuildDelta = git diff --name-only HEAD -- source gradle apps/geocedg
if ($LASTEXITCODE -ne 0) { throw 'Cannot record the build source delta.' }
$buildEvidencePath = (Resolve-Path -LiteralPath $BuildEvidence).Path
$build = Get-Content -LiteralPath $buildEvidencePath -Raw | ConvertFrom-Json
if ($build.producer_kind -ne 'GRADLE_COMMAND' -or
        $build.completion_state -ne 'COMPLETED' -or $build.inner_exit_code -ne 0 -or
        $build.arguments[0] -ne ':desktop:desktop:test') {
    throw 'Expected evidence of a successful Desktop Gradle test build.'
}
$bin = Join-Path $Jdk 'bin'
$reference = (Resolve-Path -LiteralPath $ClasspathArgs).Path
$classPathLine = @(Get-Content -LiteralPath $reference | Where-Object {
    $_ -match '^".*desktop[/\\]build[/\\]classes[/\\]java[/\\]test'
})
if ($classPathLine.Count -ne 1) { throw 'Cannot identify the Desktop test classpath.' }
$entries = $classPathLine[0].Trim('"') -split ';'
$testEntry = @($entries | Where-Object {
    $_ -match 'source[/\\]desktop[/\\]desktop[/\\]build[/\\]classes[/\\]java[/\\]test$'
})
if ($testEntry.Count -ne 1) { throw 'Cannot identify the first Desktop test entry.' }
$start = [array]::IndexOf($entries, $testEntry[0])
$entries = $entries[$start..($entries.Count - 1)]
foreach ($entry in $entries) {
    if (-not (Test-Path -LiteralPath $entry)) { throw "Missing classpath entry $entry" }
}
$classPath = $entries -join ';'

$output = Join-Path $repo "artifacts/agent/post-cu4-increments/i1-p4/$Run"
if (Test-Path -LiteralPath $output) { throw "Run already exists $output" }
$classes = Join-Path $output 'classes'
$tmp = Join-Path $output 'tmp'
[void][IO.Directory]::CreateDirectory($classes)
[void][IO.Directory]::CreateDirectory($tmp)
$sources = @(
    "$PSScriptRoot/P4ProbeRunner.java"
    "$PSScriptRoot/PostCu4P4Probe.java"
)
$java = Join-Path $bin 'java.exe'
& $java -version *> "$output/java-version.txt"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
@{
    head = $headCommit
    tree = $headTree
    tracked_build_delta = @($trackedBuildDelta)
    build_evidence = Get-FileHash -LiteralPath $buildEvidencePath
    classpath_source = $reference
    classpath_sha256 = (Get-FileHash -LiteralPath $reference).Hash
    classpath_entries = $entries
    sources = @(Get-FileHash -LiteralPath ($sources + $PSCommandPath))
    product_classes = @(Get-FileHash -LiteralPath @(
        'source/shared/common/build/libs/common.jar'
        'source/desktop/desktop/build/classes/java/main/org/geocedg/desktop/AppGeoCeDG.class'
        'source/desktop/desktop/build/classes/java/main/org/geocedg/desktop/GeoCeDGDocumentInsertion.class'
        'source/desktop/desktop/build/classes/java/main/org/geogebra/desktop/util/CopyPasteD.class'
        'source/desktop/desktop/build/classes/java/test/org/geocedg/desktop/PreG9BR6PlusF3InsertionTest.class'
    ))
    portable_preferences = [bool]$PortablePreferences
} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath "$output/inputs.json"

function Quote-Java([string]$Value) {
    '"' + $Value.Replace('\', '/') + '"'
}

$compile = Join-Path $output 'javac.args'
@('-proc:none', '-encoding', 'UTF-8', '-cp', (Quote-Java $classPath),
    '-d', (Quote-Java $classes)) + @($sources | ForEach-Object { Quote-Java $_ }) |
    Set-Content -LiteralPath $compile
& (Join-Path $bin 'javac.exe') "@$compile" *> "$output/javac.log"
$code = $LASTEXITCODE
$code | Set-Content -LiteralPath "$output/javac.exit.txt"
if ($code -ne 0) {
    Get-Content -LiteralPath "$output/javac.log" -Tail 70
    exit $code
}

$failed = $false
$originalAppData = $env:APPDATA
try {
    foreach ($suite in @('f3', 'study', 'catalog')) {
        $env:APPDATA = Join-Path $output "$suite-appdata"
        [void][IO.Directory]::CreateDirectory($env:APPDATA)
        # The catalog regression exercises the fallback used by the installed backend.
        $preference = if ($PortablePreferences -and $suite -ne 'catalog') {
            "$output/$suite.properties"
        } else { '-' }
        $runArgs = Join-Path $output "$suite.args"
        @('-ea', '-Dfile.encoding=UTF-8', (Quote-Java "-Djava.io.tmpdir=$tmp"),
            (Quote-Java "-Dgeocedg.p4.dir=$output"),
            '-Djava.util.prefs.PreferencesFactory=P4ProbeRunner$MemoryPreferencesFactory',
            '-cp', (Quote-Java "$classes;$classPath"),
            'P4ProbeRunner', (Quote-Java $preference), $suite) |
            Set-Content -LiteralPath $runArgs
        & $java "@$runArgs" *> "$output/$suite.log"
        $code = $LASTEXITCODE
        $code | Set-Content -LiteralPath "$output/$suite.exit.txt"
        Select-String -LiteralPath "$output/$suite.log" -Pattern '^P4PROBE|tests (found|successful|failed)' |
            ForEach-Object Line
        if ($code -ne 0) {
            $failed = $true
            Get-Content -LiteralPath "$output/$suite.log" -Tail 70
        }
    }
} finally {
    $env:APPDATA = $originalAppData
}
if ($failed) { exit 1 }
exit 0

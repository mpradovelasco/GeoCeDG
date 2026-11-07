#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$Jdk,
    [Parameter(Mandatory)] [string]$ClasspathArgs,
    [Parameter(Mandatory)] [ValidatePattern('^run-[0-9]+$')] [string]$Run
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../../..'))
Set-Location -LiteralPath $repo
$base = 'd5332e400b0ab68afd14273495b9ae5f03e6c911'
$headCommit = git rev-parse HEAD
if ($LASTEXITCODE -ne 0) { throw 'Cannot resolve the source commit.' }
git diff --quiet $base -- source gradle apps/geocedg
if ($LASTEXITCODE -ne 0) { throw 'Probe requires the baseline product sources.' }
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

$output = Join-Path $repo "artifacts/agent/post-cu4-insertion-review/p4/$Run"
if (Test-Path -LiteralPath $output) { throw "Run already exists $output" }
$classes = Join-Path $output 'classes'
$tmp = Join-Path $output 'tmp'
[void][IO.Directory]::CreateDirectory($classes)
[void][IO.Directory]::CreateDirectory($tmp)
$main = 'source/desktop/desktop/src/main/java/org/geocedg/desktop'
$test = 'source/desktop/desktop/src/test/java/org/geocedg/desktop'
$sources = @(
    "$PSScriptRoot/P4ProbeRunner.java"
    "$PSScriptRoot/PostCu4P4Probe.java"
    "$main/GeoCeDGDocumentInsertion.java"
    "$main/GeoCeDGInsertionAdmission.java"
    "$main/GeoCeDGInsertionFidelity.java"
    "$main/GeoCeDGInsertionCopyPaste.java"
    "$main/GeoCeDGAngularSourceArchive.java"
    'source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java'
    'source/shared/common/src/main/java/org/geocedg/common/kernel/units/MixedAngleInsertionProof.java'
    "$test/PreG9BR6PlusF3InsertionTest.java"
    "$test/PreG9BCu4InsertionTest.java"
    "$test/Cu4B2InsertionTest.java"
)
$java = Join-Path $bin 'java.exe'
& $java -version *> "$output/java-version.txt"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
@{
    product_base = $base
    head = $headCommit
    classpath_source = $reference
    classpath_entries = $entries
    sources = @(Get-FileHash -LiteralPath ($sources + $PSCommandPath))
    common_jar = Get-FileHash -LiteralPath 'source/shared/common/build/libs/common.jar'
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

$runArgs = Join-Path $output 'java.args'
@('-Dfile.encoding=UTF-8', (Quote-Java "-Djava.io.tmpdir=$tmp"),
    (Quote-Java "-Dgeocedg.p4.dir=$output"),
    '-cp', (Quote-Java "$classes;$classPath"),
    'P4ProbeRunner', (Quote-Java "$output/portable.properties")) |
    Set-Content -LiteralPath $runArgs
& $java "@$runArgs" *> "$output/java.log"
$code = $LASTEXITCODE
$code | Set-Content -LiteralPath "$output/java.exit.txt"
Select-String -LiteralPath "$output/java.log" -Pattern '^P4PROBE|tests (found|successful|failed)' |
    ForEach-Object Line
if ($code -ne 0) { Get-Content -LiteralPath "$output/java.log" -Tail 100 }
exit $code

#requires -Version 7.2
<#
.SYNOPSIS
Regenerates or verifies the curated GGT library (PRE-G9B-R6-plus-E1-L).

.DESCRIPTION
The generator is the Desktop test-source class CuratedGgtLibraryGenerator, run by the
golden test PreG9BR6PlusE1LCuratedLibraryTest.e1l02LibraryRegeneratesByteForByte on the
Gradle Desktop test JVM. Its inputs are the immutable
models/legacy/template-v7/original/Templatev7.ggb, the authored
models/curated/ggt-library/curation.yml and the owned SVG icons under
geocedg/resources/icons/ggt-library/. Its outputs are generated and never hand-edited:
models/curated/ggt-library/library-manifest.json and tools/<command>.ggt.

With -VerifyOnly the script only runs the golden test and fails when the tracked library
is not reproduced byte-for-byte. Without it, a complete regenerated set is copied into the
tracked paths and the golden test is run again. Nothing outside
models/curated/ggt-library/ is ever written, and models/legacy/** is only read.
#>
[CmdletBinding()]
param(
    [switch]$VerifyOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$gradle = Join-Path $repository 'gradlew.bat'
$test = 'org.geocedg.desktop.PreG9BR6PlusE1LCuratedLibraryTest.e1l02LibraryRegeneratesByteForByte'
$output = Join-Path $repository 'source\desktop\desktop\build\geocedg-curated-ggt-library'
$library = Join-Path $repository 'models\curated\ggt-library'
$allowed = '^models/curated/ggt-library/(library-manifest\.json|tools/[\p{L}][\p{L}\p{N}_]*\.ggt)$'

function Invoke-GoldenTest {
    & $gradle -p $repository ':desktop:desktop:test' '--tests' $test '--rerun' '--console=plain' |
        Out-Host
    return $LASTEXITCODE
}

if (Test-Path -LiteralPath $output) {
    Remove-Item -LiteralPath $output -Recurse -Force
}
if ((Invoke-GoldenTest) -eq 0) {
    Write-Host 'Curated GGT library reproduces byte-for-byte.'
    exit 0
}
if ($VerifyOnly) {
    throw 'Curated GGT library is not reproduced byte-for-byte; see the golden-test report.'
}

$complete = Join-Path $output 'COMPLETE'
if (-not (Test-Path -LiteralPath $complete -PathType Leaf)) {
    throw 'Generation did not complete; nothing was copied.'
}
$paths = @(Get-Content -LiteralPath $complete | Where-Object { $_ })
foreach ($path in $paths) {
    if ($path -cnotmatch $allowed) {
        throw "Unexpected generated path: $path"
    }
    if (-not (Test-Path -LiteralPath (Join-Path $output $path) -PathType Leaf)) {
        throw "Generated file missing: $path"
    }
}
$tools = Join-Path $library 'tools'
[void](New-Item -ItemType Directory -Path $tools -Force)
foreach ($stale in @(Get-ChildItem -LiteralPath $tools -File)) {
    if ("models/curated/ggt-library/tools/$($stale.Name)" -cnotin $paths) {
        Remove-Item -LiteralPath $stale.FullName -Force
    }
}
foreach ($path in $paths) {
    Copy-Item -LiteralPath (Join-Path $output $path) `
        -Destination (Join-Path $repository $path) -Force
}
if ((Invoke-GoldenTest) -ne 0) {
    throw 'The copied library does not verify; inspect the golden-test report.'
}
Write-Host "Curated GGT library regenerated: $($paths.Count) files."

#requires -Version 7.2
[CmdletBinding()]
param([string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) 'geocedg-dxf-authority-tests'))
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot '../dxf-authority-validation.ps1')
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$appPath = 'source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java'
$exportPath = 'source/shared/common/src/main/java/org/geocedg/common/export/DxfExporter.java'
$appSource = [IO.File]::ReadAllText((Join-Path $repository $appPath)).Replace("`r`n", "`n")
$rows = [Collections.Generic.List[object]]::new()
function Test-DxfAuthorityCase {
    param([string]$Name, [string]$Path, [string]$Source, [bool]$ShouldPass)
    $accepted = $true
    try { Assert-GeoCeDGDxfSourceAuthority -RelativePath $Path -Content $Source }
    catch { $accepted = $false }
    if ($accepted -ne $ShouldPass) { throw "Unexpected DXF authority outcome: $Name" }
    $rows.Add([ordered]@{
        name = $Name; expected = $(if ($ShouldPass) { 'ACCEPT' } else { 'REJECT' }); result = 'PASS'
    })
}

Test-DxfAuthorityCase 'actual application factory LF' $appPath $appSource $true
Test-DxfAuthorityCase 'actual application factory CRLF' $appPath ($appSource.Replace("`n", "`r`n")) $true
Test-DxfAuthorityCase 'historical application without a view factory' $appPath 'class AppGeoCeDG {}' $true
$evidence = Get-Content -Raw (Join-Path $repository 'geocedg/validation/export/g9x1/g9x1-evidence.json') |
    ConvertFrom-Json -Depth 100
foreach ($path in @($evidence.sourceBoundary.candidatePaths | Where-Object {
            $_ -match '^source/.+/src/main/java/.+\.java$' })) {
    Test-DxfAuthorityCase "actual G9X1 source $path" $path ([IO.File]::ReadAllText((Join-Path $repository $path))) $true
}
foreach ($forbidden in @('evaluateForRender', 'LocusRenderCache', 'myPointList',
        'EuclidianView', 'System.nanoTime', 'System.currentTimeMillis',
        'ThreadLocalRandom', 'Math.random', 'parallelStream')) {
    Test-DxfAuthorityCase "DXF forbids $forbidden" $exportPath "class DxfExporter { $forbidden source; }" $false
    Test-DxfAuthorityCase "application export seam forbids $forbidden" $appPath ($appSource + "`n$forbidden") $false
}
foreach ($gitInvocation in @('new ProcessBuilder("git", "status");', 'Runtime.getRuntime().exec("git status");')) {
    Test-DxfAuthorityCase "DXF forbids $gitInvocation" $exportPath "class DxfExporter { $gitInvocation }" $false
    Test-DxfAuthorityCase "application export seam forbids $gitInvocation" $appPath ($appSource + "`n$gitInvocation") $false
}
Test-DxfAuthorityCase 'view factory body cannot acquire viewport authority' $appPath `
    ($appSource.Replace('getSettings().getEuclidian(1)', 'getActiveEuclidianView().getSettings()')) $false
Test-DxfAuthorityCase 'view factory cannot change its approved constructor' $appPath `
    ($appSource.Replace('new GeoCeDGEuclidianView(', 'new EuclidianView(')) $false
Test-DxfAuthorityCase 'view import cannot be omitted' $appPath `
    ($appSource.Replace("import org.geogebra.common.euclidian.EuclidianView;`n", '')) $false
Test-DxfAuthorityCase 'view import cannot be duplicated' $appPath `
    ($appSource + "`nimport org.geogebra.common.euclidian.EuclidianView;`n") $false
Test-DxfAuthorityCase 'application factory exception never applies to an exporter' $exportPath $appSource $false
$factory = [regex]::Match($appSource, '(?ms)^\t@Override\n\tprotected EuclidianView newEuclidianView\([^\n]*\) \{.*?^\t\}').Value
if ([string]::IsNullOrWhiteSpace($factory)) { throw 'Missing actual view factory fixture.' }
Test-DxfAuthorityCase 'view factory cannot be duplicated' $appPath ($appSource + "`n$factory") $false

if (@($rows.name | Sort-Object -Unique -CaseSensitive).Count -ne $rows.Count) { throw 'Duplicate DXF fixture identity.' }
$json = ([ordered]@{ schemaVersion = 1; cases = @($rows); tests = $rows.Count; failures = 0 } |
    ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"
[void][IO.Directory]::CreateDirectory([IO.Path]::GetFullPath($LogDirectory))
$resultPath = Join-Path $LogDirectory 'dxf-authority-tests.json'
[IO.File]::WriteAllText($resultPath, $json, [Text.UTF8Encoding]::new($false))
$hash = (Get-FileHash -LiteralPath $resultPath -Algorithm SHA256).Hash.ToLowerInvariant()
Write-Host "DXF authority validation: $($rows.Count)/$($rows.Count) PASS; SHA-256 $hash; $resultPath"

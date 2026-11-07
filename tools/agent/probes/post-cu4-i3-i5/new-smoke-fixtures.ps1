#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$OutputDirectory)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$directory = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $directory) { throw 'The fixture directory must be new.' }
[void][IO.Directory]::CreateDirectory($directory)

function New-Archive {
    param([string]$Name, [string]$Construction)

    $path = Join-Path $directory "$Name.ggb"
    $stream = [IO.File]::Open($path, [IO.FileMode]::CreateNew)
    try {
        $zip = [IO.Compression.ZipArchive]::new($stream, 'Create', $true)
        try {
            $entry = $zip.CreateEntry('geogebra.xml', 'NoCompression')
            $entry.LastWriteTime = [DateTimeOffset]::new(2000, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
            $writer = [IO.StreamWriter]::new($entry.Open(), [Text.UTF8Encoding]::new($false))
            try {
                $writer.Write((@"
<?xml version="1.0" encoding="utf-8"?>
<geogebra format="5.0" version="5.4.928.0" app="classic" platform="d">
<kernel><continuous val="false"/><angleUnit val="degree"/></kernel>
<construction title="" author="" date="">
$Construction
</construction>
</geogebra>
"@).Replace("`r`n", "`n"))
            } finally { $writer.Dispose() }
        } finally { $zip.Dispose() }
    } finally { $stream.Dispose() }
}

New-Archive 'receiver-layer-four' @'
<expression label="A" exp="(-2, 0)" type="point"/>
<element type="point" label="A">
  <show object="true" label="true"/>
  <layer val="4"/>
  <coords x="-2" y="0" z="1"/>
</element>
'@

$arc = @'
<expression label="A" exp="(0, 0)" type="point"/>
<element type="point" label="A"><show object="true" label="true"/><layer val="0"/><coords x="0" y="0" z="1"/></element>
<expression label="B" exp="(2, 0)" type="point"/>
<element type="point" label="B"><show object="true" label="true"/><layer val="0"/><coords x="2" y="0" z="1"/></element>
<expression label="C" exp="(0, 2)" type="point"/>
<element type="point" label="C"><show object="true" label="true"/><layer val="0"/><coords x="0" y="2" z="1"/></element>
<command name="CircleArc"><input a0="A" a1="B" a2="C"/><output a0="arc"/></command>
<element type="conicpart" label="arc"><show object="true" label="true"/><layer val="0"/></element>
<expression label="u" exp="0.5"/>
<element type="numeric" label="u"><show object="false" label="false"/><value val="0.5"/></element>
'@
New-Archive 'source-arc' $arc
New-Archive 'source-broken-arc' ($arc.Replace('a2="C"', 'a2="MissingPoint"'))
$unusable = $arc.Replace('exp="(0, 2)"', 'exp="(0, 0)"')
New-Archive 'source-unusable-direction' ($unusable.Replace('x="0" y="2" z="1"', 'x="0" y="0" z="1"'))
[IO.File]::WriteAllText((Join-Path $directory 'source-invalid.ggb'), 'invalid fixture archive')

Get-ChildItem -LiteralPath $directory -File | Sort-Object Name | ForEach-Object {
    $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  $($_.Name)"
} | Set-Content -LiteralPath (Join-Path $directory 'SHA256SUMS.txt') -Encoding utf8NoBOM

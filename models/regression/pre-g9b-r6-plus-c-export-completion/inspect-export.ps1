# GeoCeDG
# Copyright (c) 2026 GeoCeDG contributors
# SPDX-License-Identifier: EUPL-1.2
#
# PRE-G9B-R6-plus-C author-smoke inspection helper. Read-only: prints the facts
# of exported files that the smoke checklist asks about, so that no DXF, XML or
# binary file has to be opened or edited by hand.
#
#   .dxf   $INSUNITS, GeoCeDG 999 comments, LAYER table (62 < 0 = OFF), entity
#          counts per type and layer, entities with group 60 = 1
#   .json  paired fidelity sidecar: schema, units, layers, export area, outcomes
#   .emf   EMR_HEADER rclBounds (device) and rclFrame (0.01 mm) as millimetres
#   .pdf   MediaBox in points and millimetres
#   .svg   width, height and viewBox
#   .png   pixel size and the pHYs resolution when present
#   .tex .txt .asy  GeoCeDG comment lines and the unit lines of the dialect
#
# Usage: pwsh -NoProfile -File inspect-export.ps1 <file> [<file> ...]

[CmdletBinding()]
param(
    [Parameter(Mandatory, ValueFromRemainingArguments)] [string[]]$Path
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Threading.Thread]::CurrentThread.CurrentCulture = [Globalization.CultureInfo]::InvariantCulture

function Read-Int32LE([byte[]]$bytes, [int]$offset) {
    return [BitConverter]::ToInt32($bytes, $offset)
}

function Read-UInt32BE([byte[]]$bytes, [int]$offset) {
    return ([uint32]$bytes[$offset] -shl 24) -bor ([uint32]$bytes[$offset + 1] -shl 16) -bor
        ([uint32]$bytes[$offset + 2] -shl 8) -bor [uint32]$bytes[$offset + 3]
}

function Show-Dxf([string]$file) {
    $lines = [IO.File]::ReadAllLines($file, [Text.Encoding]::ASCII)
    $pairs = New-Object System.Collections.Generic.List[object]
    for ($i = 0; $i + 1 -lt $lines.Length; $i += 2) {
        $pairs.Add([pscustomobject]@{ Code = $lines[$i].Trim(); Value = $lines[$i + 1].Trim() })
    }
    $section = $null; $table = $null; $insunits = '(absent)'
    $comments = New-Object System.Collections.Generic.List[string]
    $layers = [ordered]@{}
    $entities = @{}
    $hiddenEntities = 0
    $current = $null
    for ($k = 0; $k -lt $pairs.Count; $k++) {
        $p = $pairs[$k]
        if ($p.Code -eq '999') { $comments.Add($p.Value); continue }
        if ($p.Code -eq '0' -and $p.Value -eq 'SECTION') { $section = $pairs[$k + 1].Value; continue }
        if ($p.Code -eq '0' -and $p.Value -eq 'ENDSEC') { $section = $null; continue }
        if ($p.Code -eq '9' -and $p.Value -eq '$INSUNITS') { $insunits = $pairs[$k + 1].Value; continue }
        if ($section -eq 'TABLES' -and $p.Code -eq '0' -and $p.Value -eq 'TABLE') { $table = $pairs[$k + 1].Value; continue }
        if ($section -eq 'TABLES' -and $table -eq 'LAYER' -and $p.Code -eq '0' -and $p.Value -eq 'LAYER') {
            $name = $null; $color = $null
            for ($j = $k + 1; $j -lt $pairs.Count -and $pairs[$j].Code -ne '0'; $j++) {
                if ($pairs[$j].Code -eq '2') { $name = $pairs[$j].Value }
                if ($pairs[$j].Code -eq '62') { $color = [int]$pairs[$j].Value }
            }
            if ($null -ne $name) { $layers[$name] = $color }
            continue
        }
        if ($section -eq 'ENTITIES' -and $p.Code -eq '0') {
            $current = [pscustomobject]@{ Type = $p.Value; Layer = '0'; Hidden = $false }
            for ($j = $k + 1; $j -lt $pairs.Count -and $pairs[$j].Code -ne '0'; $j++) {
                if ($pairs[$j].Code -eq '8') { $current.Layer = $pairs[$j].Value }
                if ($pairs[$j].Code -eq '60' -and $pairs[$j].Value -eq '1') { $current.Hidden = $true }
            }
            $key = "$($current.Type) on layer $($current.Layer)"
            if (-not $entities.ContainsKey($key)) { $entities[$key] = 0 }
            $entities[$key]++
            if ($current.Hidden) { $hiddenEntities++ }
        }
    }
    "  `$INSUNITS = $insunits  (0 unitless, 4 mm, 5 cm, 6 m)"
    foreach ($c in $comments) { "  999 $c" }
    foreach ($name in $layers.Keys) {
        $state = if ($null -eq $layers[$name]) { 'no 62' } elseif ($layers[$name] -lt 0) { 'OFF' } else { 'on' }
        "  LAYER $name  62 = $($layers[$name])  ($state)"
    }
    foreach ($key in ($entities.Keys | Sort-Object)) { "  $($entities[$key]) x $key" }
    "  entities with 60 = 1 (individually hidden): $hiddenEntities"
}

function Show-Sidecar([string]$file) {
    $j = Get-Content -Raw -LiteralPath $file | ConvertFrom-Json
    "  schema $($j.schema) version $($j.schema_version)"
    "  dxf: source_unit $($j.dxf.source_unit), target_unit $($j.dxf.target_unit), insunits $($j.dxf.insunits), sha256 $($j.dxf.sha256)"
    if ($j.PSObject.Properties['units']) {
        $u = $j.units
        "  units: state $($u.state), construction_unit $($u.construction_unit), insunits $($u.insunits), meters_per_unit $($u.meters_per_unit)"
        if ($null -ne $u.usm) { "  usm: $($u.usm | ConvertTo-Json -Compress)" }
    }
    if ($j.PSObject.Properties['layers']) {
        "  hidden GeoCeDG layers: [$(@($j.layers.hidden_geocedg_layers) -join ', ')]; DXF layers OFF: [$(@($j.layers.dxf_layers_off) -join ', ')]"
    }
    if ($j.PSObject.Properties['export_area']) {
        $a = $j.export_area
        "  export area: producer $($a.resolved_producer), boundary $($a.boundary), bounds $($a.bounds | ConvertTo-Json -Compress), outside $($a.outside_export_area)"
    }
    $groups = @($j.outcomes | Group-Object fidelity | ForEach-Object { "$($_.Name) $($_.Count)" })
    "  outcomes: $($groups -join ', ')"
    if ($j.PSObject.Properties['outside_export_area']) {
        foreach ($o in @($j.outside_export_area)) { "  outside the export area: $($o.source_label) ($($o.source_id))" }
    }
    foreach ($w in @($j.warnings)) { "  warning $($w.code): $($w.message)" }
}

function Show-Emf([string]$file) {
    $b = [IO.File]::ReadAllBytes($file)
    if ((Read-Int32LE $b 0) -ne 1) { throw "$file is not an EMF (no EMR_HEADER)" }
    $bounds = @((Read-Int32LE $b 8), (Read-Int32LE $b 12), (Read-Int32LE $b 16), (Read-Int32LE $b 20))
    $frame = @((Read-Int32LE $b 24), (Read-Int32LE $b 28), (Read-Int32LE $b 32), (Read-Int32LE $b 36))
    "  rclBounds (device) = [$($bounds -join ', ')]  -> $($bounds[2] - $bounds[0]) x $($bounds[3] - $bounds[1]) device units"
    "  rclFrame (0.01 mm) = [$($frame -join ', ')]  -> $(($frame[2] - $frame[0]) / 100) mm x $(($frame[3] - $frame[1]) / 100) mm"
}

function Show-Pdf([string]$file) {
    $text = [Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes($file))
    $m = [regex]::Match($text, '/MediaBox\s*\[\s*([-0-9.]+)\s+([-0-9.]+)\s+([-0-9.]+)\s+([-0-9.]+)\s*\]')
    if (-not $m.Success) { '  no MediaBox found'; return }
    $v = 1..4 | ForEach-Object { [double]::Parse($m.Groups[$_].Value, [Globalization.CultureInfo]::InvariantCulture) }
    $w = $v[2] - $v[0]; $h = $v[3] - $v[1]
    "  MediaBox = [$($v -join ' ')]  -> $w x $h pt = $([math]::Round($w * 25.4 / 72, 4)) x $([math]::Round($h * 25.4 / 72, 4)) mm"
}

function Show-Svg([string]$file) {
    $head = (Get-Content -LiteralPath $file -TotalCount 40) -join ' '
    foreach ($name in 'width', 'height', 'viewBox') {
        $m = [regex]::Match($head, "\s$name=`"([^`"]*)`"")
        "  $name = $(if ($m.Success) { $m.Groups[1].Value } else { '(absent)' })"
    }
}

function Show-Png([string]$file) {
    $b = [IO.File]::ReadAllBytes($file)
    "  $(Read-UInt32BE $b 16) x $(Read-UInt32BE $b 20) pixels"
    $offset = 8
    while ($offset + 8 -le $b.Length) {
        $length = [int](Read-UInt32BE $b $offset)
        $type = [Text.Encoding]::ASCII.GetString($b, $offset + 4, 4)
        if ($type -eq 'pHYs') {
            $x = Read-UInt32BE $b ($offset + 8)
            $unit = $b[$offset + 16]
            if ($unit -eq 1) { "  pHYs: $x pixels per metre = $([math]::Round($x * 0.0254, 2)) DPI" } else { "  pHYs: $x (unit unknown)" }
        }
        if ($type -eq 'IEND') { break }
        $offset += 12 + $length
    }
}

function Show-Latex([string]$file) {
    foreach ($line in [IO.File]::ReadAllLines($file)) {
        if ($line -match '^\s*(%|//) GeoCeDG' -or $line -match 'xunit|yunit|x=[0-9.]+cm|unitsize|\\begin\{(tikzpicture|pspicture)\}') {
            "  $line"
        }
    }
}

foreach ($item in $Path) {
    $file = (Resolve-Path -LiteralPath $item).ProviderPath
    "== $file"
    switch -Regex ([IO.Path]::GetExtension($file).ToLowerInvariant()) {
        '^\.dxf$' { Show-Dxf $file }
        '^\.json$' { Show-Sidecar $file }
        '^\.emf$' { Show-Emf $file }
        '^\.pdf$' { Show-Pdf $file }
        '^\.svg$' { Show-Svg $file }
        '^\.png$' { Show-Png $file }
        '^\.(tex|txt|asy)$' { Show-Latex $file }
        default { "  (no inspector for this extension)" }
    }
}

#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../diagnostics/guide-structure.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
function Assert-Case([bool]$Condition,[string]$Message){$script:Assertions++;if(-not $Condition){throw "TEST FAILURE: $Message"}}
function Invoke-Case([string]$Name,[scriptblock]$Action){$script:Cases++;&$Action;Write-Host "PASS: $Name"}
function Rules([object]$Structure){return [string[]]@($Structure.findings | ForEach-Object rule)}

# The cases verify the audit itself on fixtures. The live editions are audited
# by the diagnostic leaf, whose findings stay documentation diagnostics.
$valid = (@(
    '<!-- geocedg-guide-section: overview -->',
    '# Title',
    'Text.',
    '<!-- geocedg-guide-section: drawing -->',
    '## 1. Drawing',
    '### 1.1 Points',
    '```text',
    '### not a heading inside a fence',
    '<!-- geocedg-guide-section: fenced -->',
    '```',
    '#### 1.1.1 Deeper headings are not navigation entries',
    '<!-- geocedg-guide-section: measuring -->',
    '## 2. Measuring',
    '### 2.1 Lengths') -join "`n")

Invoke-Case 'a valid fixture derives marker and numbering anchors only' {
    $structure = Get-GeoCeDGGuideStructure -Text $valid
    Assert-Case (@($structure.findings).Count -eq 0) "The valid fixture has findings: $(Rules $structure)"
    Assert-Case ($structure.entries[0].anchor -ceq 'overview' -and $structure.entries[0].level -eq 1) `
        'The title does not take the first marker.'
    Assert-Case ($structure.headings.h1 -eq 1 -and $structure.headings.h2 -eq 2 -and
        $structure.headings.h3 -eq 2) 'Fenced or deeper headings were counted.'
    Assert-Case ((@($structure.entries.anchor) -join ',') -ceq
        'overview,drawing,drawing/1.1,measuring,measuring/2.1') 'Unexpected derived anchors.'
    Assert-Case ($structure.markers -eq 3) 'A fenced marker was counted.'
    Assert-Case ((@($structure.entries | ForEach-Object numbering) -join ',') -ceq ',1,1.1,2,2.1') `
        'Unexpected numbering tokens.'
}
Invoke-Case 'translated heading text never changes the vector' {
    $translated = $valid.Replace('# Title', '# Título').Replace('1. Drawing', '1. Dibujo').
        Replace('1.1 Points', '1.1 Puntos').Replace('2. Measuring', '2. Medir').
        Replace('2.1 Lengths', '2.1 Longitudes')
    $english = Get-GeoCeDGGuideStructure -Text $valid
    $spanish = Get-GeoCeDGGuideStructure -Text $translated
    Assert-Case (@(Compare-GeoCeDGGuideStructure -English $english -Spanish $spanish).Count -eq 0) `
        'Translated prose changed the structural vector.'
}
Invoke-Case 'a missing marker is reported at its heading' {
    $structure = Get-GeoCeDGGuideStructure -Text $valid.Replace(
        "<!-- geocedg-guide-section: measuring -->`n", '')
    $finding = @($structure.findings | Where-Object rule -CEQ 'H1_OR_H2_WITHOUT_MARKER')
    Assert-Case ($finding.Count -eq 1 -and $finding[0].line -eq 12) 'Missing marker was not located.'
    Assert-Case (@(Rules $structure) -ccontains 'H3_WITHOUT_ENCLOSING_SECTION') `
        'The orphaned level-3 heading was not reported.'
}
Invoke-Case 'a misplaced marker is reported on both sides' {
    $structure = Get-GeoCeDGGuideStructure -Text $valid.Replace(
        "<!-- geocedg-guide-section: drawing -->`n## 1. Drawing",
        "<!-- geocedg-guide-section: drawing -->`n`n## 1. Drawing")
    $rules = Rules $structure
    Assert-Case ($rules -ccontains 'MARKER_NOT_FOLLOWED_BY_H1_OR_H2') 'Misplaced marker was not reported.'
    Assert-Case ($rules -ccontains 'H1_OR_H2_WITHOUT_MARKER') 'Unmarked section heading was not reported.'
    $malformed = Get-GeoCeDGGuideStructure -Text $valid.Replace(
        'geocedg-guide-section: drawing', 'geocedg-guide-section: Drawing')
    Assert-Case ((Rules $malformed) -ccontains 'MALFORMED_MARKER') 'Malformed marker was not reported.'
}
Invoke-Case 'a level-3 heading without a numeric prefix is reported' {
    $structure = Get-GeoCeDGGuideStructure -Text $valid.Replace('### 2.1 Lengths', '### Lengths')
    $finding = @($structure.findings | Where-Object rule -CEQ 'H3_WITHOUT_NUMERIC_PREFIX')
    Assert-Case ($finding.Count -eq 1 -and $finding[0].line -eq 14) 'Unnumbered heading was not located.'
}
Invoke-Case 'a duplicate derived anchor is reported' {
    $structure = Get-GeoCeDGGuideStructure -Text $valid.Replace('### 2.1 Lengths',
        "### 2.1 Lengths`n### 2.1 Repeated")
    $finding = @($structure.findings | Where-Object rule -CEQ 'DUPLICATE_DERIVED_ANCHOR')
    Assert-Case ($finding.Count -eq 1 -and $finding[0].line -eq 15) 'Duplicate anchor was not located.'
    $sections = Get-GeoCeDGGuideStructure -Text $valid.Replace('section: measuring', 'section: drawing')
    Assert-Case ((Rules $sections) -ccontains 'DUPLICATE_DERIVED_ANCHOR') 'Duplicate marker id was not reported.'
}
Invoke-Case 'an EN/ES structure mismatch names the exact element' {
    $english = Get-GeoCeDGGuideStructure -Text $valid
    $spanish = Get-GeoCeDGGuideStructure -Text $valid.Replace('### 1.1 Points', '### 1.2 Points')
    $mismatch = @(Compare-GeoCeDGGuideStructure -English $english -Spanish $spanish)
    Assert-Case ($mismatch.Count -eq 1) 'Mismatch count is wrong.'
    Assert-Case ($mismatch[0].index -eq 2 -and $mismatch[0].en -ceq '3|1.1|drawing' -and
        $mismatch[0].es -ceq '3|1.2|drawing' -and $mismatch[0].es_line -eq 6) 'Mismatch is not exact.'
    $shorter = Get-GeoCeDGGuideStructure -Text $valid.Replace("`n### 2.1 Lengths", '')
    $counts = @(Compare-GeoCeDGGuideStructure -English $english -Spanish $shorter)
    Assert-Case ($counts.Count -eq 1 -and $counts[0].rule -ceq 'EN_ES_ENTRY_COUNT_MISMATCH') `
        'An entry-count mismatch was not reported.'
    $section = Get-GeoCeDGGuideStructure -Text $valid.Replace('section: measuring', 'section: measures')
    $renamed = @(Compare-GeoCeDGGuideStructure -English $english -Spanish $section)
    Assert-Case ($renamed.Count -eq 2 -and $renamed[0].index -eq 3) 'A section-id mismatch was not reported.'
}

Write-Host "guide-structure-diagnostic.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2
# PRE-G9B-R5-A structural audit of the bilingual user guide. Pure text
# extraction only: the rule is section 10 of
# geocedg/specs/operations/documentation-maintenance.md. Heading text is never
# interpreted, compared across editions or slugified.
Set-StrictMode -Version Latest

$script:Marker = [regex]'^<!-- geocedg-guide-section: ([a-z0-9-]+) -->$'
$script:Heading = [regex]'^(#{1,6})\s+(.*)$'
$script:Numbering = [regex]'^(\d+(?:\.\d+)*)\.?\s'
$script:Fence = [regex]'^```\s*(\w*)\s*$'

function New-GuideStructureFinding([string]$Rule, [int]$Line, [string]$Detail) {
    return [pscustomobject][ordered]@{ rule = $Rule; line = $Line; detail = $Detail }
}

function Get-GeoCeDGGuideStructure {
    <#
    Extracts the ordered navigation vector of one edition and the findings of
    its marker, numbering and uniqueness rules. Lines are 1-based.
    #>
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)

    $lines = $Text.Replace("`r`n", "`n").Replace("`r", "`n").Split("`n")
    $entries = [Collections.Generic.List[object]]::new()
    $findings = [Collections.Generic.List[object]]::new()
    $anchors = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $markerCount = 0
    $section = $null
    $fenced = $false
    for ($index = 0; $index -lt $lines.Length; $index++) {
        $line = $lines[$index].TrimEnd()
        $number = $index + 1
        if ($script:Fence.IsMatch($line)) { $fenced = -not $fenced; continue }
        if ($fenced) { continue }
        $marker = $script:Marker.Match($line)
        if ($marker.Success) {
            $markerCount++
            $next = if ($index + 1 -lt $lines.Length) { $lines[$index + 1].TrimEnd() } else { '' }
            $nextHeading = $script:Heading.Match($next)
            if (-not $nextHeading.Success -or $nextHeading.Groups[1].Value.Length -gt 2) {
                $findings.Add((New-GuideStructureFinding 'MARKER_NOT_FOLLOWED_BY_H1_OR_H2' `
                    $number "marker '$($marker.Groups[1].Value)' is not immediately followed by a level-1 or level-2 heading"))
            }
            continue
        }
        if ($line.Contains('geocedg-guide-section')) {
            $findings.Add((New-GuideStructureFinding 'MALFORMED_MARKER' $number `
                'a section-marker comment does not match the exact marker form'))
            continue
        }
        $heading = $script:Heading.Match($line)
        if (-not $heading.Success) { continue }
        $level = $heading.Groups[1].Value.Length
        if ($level -gt 3) { continue }
        $token = $script:Numbering.Match($heading.Groups[2].Value)
        $numbering = if ($token.Success) { $token.Groups[1].Value } else { '' }
        $anchor = $null
        if ($level -le 2) {
            $previous = if ($index -gt 0) { $script:Marker.Match($lines[$index - 1].TrimEnd()) } else { $null }
            $section = if ($null -ne $previous -and $previous.Success) { $previous.Groups[1].Value } else { $null }
            if ($null -eq $section) {
                $findings.Add((New-GuideStructureFinding 'H1_OR_H2_WITHOUT_MARKER' $number `
                    "level-$level heading is not immediately preceded by a section marker"))
            } else {
                $anchor = $section
            }
        } elseif ($numbering.Length -eq 0) {
            $findings.Add((New-GuideStructureFinding 'H3_WITHOUT_NUMERIC_PREFIX' $number `
                'level-3 heading has no decimal numbering prefix'))
        } elseif ($null -eq $section) {
            $findings.Add((New-GuideStructureFinding 'H3_WITHOUT_ENCLOSING_SECTION' $number `
                'level-3 heading has no enclosing marked section'))
        } else {
            $anchor = "$section/$numbering"
        }
        if ($null -ne $anchor -and -not $anchors.Add($anchor)) {
            $findings.Add((New-GuideStructureFinding 'DUPLICATE_DERIVED_ANCHOR' $number `
                "derived anchor '$anchor' is not unique"))
        }
        $entries.Add([pscustomobject][ordered]@{
            line = $number; level = $level; numbering = $numbering
            section_id = $(if ($null -eq $section) { '' } else { $section })
            anchor = $anchor
        })
    }
    return [pscustomobject][ordered]@{
        markers = $markerCount
        headings = [ordered]@{
            h1 = @($entries | Where-Object level -EQ 1).Count
            h2 = @($entries | Where-Object level -EQ 2).Count
            h3 = @($entries | Where-Object level -EQ 3).Count
        }
        entries = [object[]]$entries.ToArray()
        findings = [object[]]$findings.ToArray()
    }
}

function Compare-GeoCeDGGuideStructure {
    <#
    Compares the ordered (heading level, numbering prefix, enclosing section id)
    vectors of two editions element for element and names every mismatch.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$English,
        [Parameter(Mandatory)] [object]$Spanish
    )

    $mismatches = [Collections.Generic.List[object]]::new()
    $en = [object[]]@($English.entries)
    $es = [object[]]@($Spanish.entries)
    $shared = [Math]::Min($en.Length, $es.Length)
    for ($index = 0; $index -lt $shared; $index++) {
        $left = "$($en[$index].level)|$($en[$index].numbering)|$($en[$index].section_id)"
        $right = "$($es[$index].level)|$($es[$index].numbering)|$($es[$index].section_id)"
        if ($left -cne $right) {
            $mismatches.Add([pscustomobject][ordered]@{
                rule = 'EN_ES_STRUCTURE_MISMATCH'; index = $index
                en_line = $en[$index].line; en = $left
                es_line = $es[$index].line; es = $right
            })
        }
    }
    if ($en.Length -ne $es.Length) {
        $mismatches.Add([pscustomobject][ordered]@{
            rule = 'EN_ES_ENTRY_COUNT_MISMATCH'; index = $shared
            en_line = $null; en = "$($en.Length) entries"
            es_line = $null; es = "$($es.Length) entries"
        })
    }
    return [object[]]$mismatches.ToArray()
}

Export-ModuleMember -Function Get-GeoCeDGGuideStructure, Compare-GeoCeDGGuideStructure

#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath,
    [string]$EnglishGuidePath = 'docs/user/geocedg_user_guide_en.md',
    [string]$SpanishGuidePath = 'docs/user/geocedg_user_guide_es.md')
Set-StrictMode -Version Latest
# PRE-G9B-R5-A: structural EN/ES alignment of the user guide. Findings are
# documentation diagnostics and never change acceptance or the exit status.
Import-Module (Join-Path $PSScriptRoot 'guide-structure.psm1') -Force
$root = [IO.Path]::GetFullPath($RepositoryRoot)
$rule = 'geocedg/specs/operations/documentation-maintenance.md#10-user-guide-structure-and-derived-navigation'
$result = [ordered]@{ contract_class = 'DOCUMENTATION_DIAGNOSTIC' }
try {
    $strict = [Text.UTF8Encoding]::new($false, $true)
    $english = Get-GeoCeDGGuideStructure -Text ([IO.File]::ReadAllText(
        (Join-Path $root $EnglishGuidePath), $strict))
    $spanish = Get-GeoCeDGGuideStructure -Text ([IO.File]::ReadAllText(
        (Join-Path $root $SpanishGuidePath), $strict))
    $alignment = @(Compare-GeoCeDGGuideStructure -English $english -Spanish $spanish)
    $total = @($english.findings).Count + @($spanish.findings).Count + $alignment.Count
    $result.outcome = $(if ($total -eq 0) { 'DIAGNOSTIC_CLEAR' } else { 'DIAGNOSTIC_FINDING' })
    $result.cause = $(if ($total -eq 0) { $null } else {
        "$total structural guide finding(s); see evidence for the exact rule and line."
    })
    $result.evidence = [ordered]@{
        rule = $rule
        editions = @(
            [ordered]@{ path = $EnglishGuidePath; markers = $english.markers
                headings = $english.headings; entries = @($english.entries).Count
                findings = @($english.findings) },
            [ordered]@{ path = $SpanishGuidePath; markers = $spanish.markers
                headings = $spanish.headings; entries = @($spanish.entries).Count
                findings = @($spanish.findings) })
        alignment = [ordered]@{ compared = [Math]::Min(@($english.entries).Count,
                @($spanish.entries).Count); mismatches = $alignment }
    }
} catch {
    $result.outcome = 'DIAGNOSTIC_UNAVAILABLE'
    $result.cause = "The guide structure could not be read: $($_.Exception.Message)"
    $result.evidence = [ordered]@{ rule = $rule; english = $EnglishGuidePath
        spanish = $SpanishGuidePath }
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 20) + [char]10),
    [Text.UTF8Encoding]::new($false))
exit 0

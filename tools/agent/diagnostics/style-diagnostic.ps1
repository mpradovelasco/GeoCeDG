#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath)
Set-StrictMode -Version Latest
$root = [IO.Path]::GetFullPath($RepositoryRoot)
$paths = @(
    '.github/prompts/tasks/task-template.prompt.md',
    '.github/prompts/reviews/change-review.prompt.md',
    '.github/prompts/tasks/author-direct-change.prompt.md'
)
$documents = @($paths | ForEach-Object {
    $path = Join-Path $root $_
    $text = [IO.File]::ReadAllText($path, [Text.UTF8Encoding]::new($false, $true))
    [ordered]@{
        path = $_
        h1_titles = @([regex]::Matches($text, '(?m)^#\s+[^#\r\n].*$')).Count
        h2_sections = @([regex]::Matches($text, '(?m)^##\s+[^#\r\n].*$')).Count
    }
})
$findings = @($documents | Where-Object { $_.h1_titles -ne 1 })
$result = [ordered]@{
    contract_class = 'STYLE_DIAGNOSTIC'
    outcome = $(if ($findings.Count -eq 0) { 'DIAGNOSTIC_CLEAR' } else {
        'DIAGNOSTIC_FINDING'
    })
    cause = $(if ($findings.Count -eq 0) { $null } else {
        'Prompt title counts are advisory and one or more differ from one.'
    })
    evidence = [ordered]@{ documents = $documents; findings = $findings }
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 20) + [char]10),
    [Text.UTF8Encoding]::new($false))
exit 0

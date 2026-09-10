#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath)
Set-StrictMode -Version Latest
$root = [IO.Path]::GetFullPath($RepositoryRoot)
$documents = @('README.md', 'docs/user/geocedg_user_guide.md',
    'docs/developer/geocedg_developer_guide.md')
$missing = @($documents | Where-Object {
    -not [IO.File]::ReadAllText((Join-Path $root $_)).Contains(
        '.\tools\agent\verify.ps1 -Profile WORKSTATION')
})
$result = [ordered]@{
    contract_class = 'DOCUMENTATION_DIAGNOSTIC'
    outcome = $(if ($missing.Count -eq 0) { 'DIAGNOSTIC_CLEAR' } else { 'DIAGNOSTIC_FINDING' })
    cause = $(if ($missing.Count -eq 0) { $null } else {
        'One or more guides omit the canonical WORKSTATION command.'
    })
    evidence = [ordered]@{ checked = $documents; missing = $missing }
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 10) + "`n"),
    [Text.UTF8Encoding]::new($false))
exit 0

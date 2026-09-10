#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath)
Set-StrictMode -Version Latest
$result = [ordered]@{
    contract_class = 'HISTORICAL_CONSISTENCY_DIAGNOSTIC'
    outcome = 'DIAGNOSTIC_UNAVAILABLE'
    cause = 'Phase-specific historical projections are not live acceptance machinery; immutable Git objects remain the historical authority.'
    evidence = [ordered]@{ repository_root = [IO.Path]::GetFullPath($RepositoryRoot) }
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 10) + "`n"),
    [Text.UTF8Encoding]::new($false))
exit 0

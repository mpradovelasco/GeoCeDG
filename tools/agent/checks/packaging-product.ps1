#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$ResultPath)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$result = [ordered]@{
    contract_class = 'PRODUCT_SEMANTIC'
    outcome = 'EVIDENCE_UNTRUSTED'
    cause = 'Pure packaging evidence summary is deferred until the authorized Cohort 3 verify-packaging.ps1 adaptation.'
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
$json = (ConvertTo-Json -InputObject $result -Depth 10).Replace("`r`n", "`n") + "`n"
[IO.File]::WriteAllText($full, $json, [Text.UTF8Encoding]::new($false))
exit 3

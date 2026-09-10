#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath)
Set-StrictMode -Version Latest
$result = [ordered]@{
    contract_class = 'GOVERNANCE_DIAGNOSTIC'
    outcome = 'DIAGNOSTIC_FINDING'
    cause = 'Legacy governance remains historical context during the temporary recovery protocol.'
    evidence = [ordered]@{ recovery_protocol = 'VERIFICATION_INFRASTRUCTURE_RECOVERY_PROTOCOL_V1' }
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 10) + "`n"),
    [Text.UTF8Encoding]::new($false))
exit 0

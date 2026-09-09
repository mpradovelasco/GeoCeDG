#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
$contracts = [Collections.Generic.List[object]]::new()

function Add-ProductObservation {
    param([string]$Id, [string]$Status, [string]$Cause)
    $contracts.Add([pscustomobject][ordered]@{
        contract_id = $Id
        status = $Status
        cause = $Cause
    })
}

$repository = [IO.Path]::GetFullPath($RepositoryRoot)
try {
    . (Join-Path $repository 'tools/agent/workspace-profile-validation.ps1')
    [void](Assert-GeoCeDGLiveWorkspaceProfile -RepositoryRoot $repository)
    Add-ProductObservation 'workspace-profile.live-authority' SATISFIED $null
} catch {
    Add-ProductObservation 'workspace-profile.live-authority' VIOLATED $_.Exception.Message
}

try {
    $desktopBuildPath = Join-Path $repository 'source/desktop/desktop/build.gradle.kts'
    $desktopBuild = [IO.File]::ReadAllText(
        $desktopBuildPath, [Text.UTF8Encoding]::new($false, $true))
    Assert-GeoCeDGProfileResourcePackaging -DesktopBuild $desktopBuild
    Add-ProductObservation 'workspace-profile.packaged-resources' SATISFIED $null
} catch {
    Add-ProductObservation 'workspace-profile.packaged-resources' VIOLATED $_.Exception.Message
}

$outcome = if (@($contracts | Where-Object { $_.status -ceq 'VIOLATED' }).Count -gt 0) {
    'CONTRACT_VIOLATED'
} else { 'CONTRACT_SATISFIED' }
$result = [ordered]@{
    contract_class = 'PRODUCT_SEMANTIC'
    outcome = $outcome
    subcontracts = [object[]]$contracts.ToArray()
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
$json = (ConvertTo-Json -InputObject $result -Depth 20).Replace("`r`n", "`n") + "`n"
[IO.File]::WriteAllText($full, $json, $utf8)
if ($outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
exit 1

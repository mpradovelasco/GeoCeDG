#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$CatalogPath,
    [Parameter(Mandatory)] [string]$ContractId,
    [Parameter(Mandatory)] [string]$ResultPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..')
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$utf8=[Text.UTF8Encoding]::new($false)
$repository=[IO.Path]::GetFullPath($RepositoryRoot)
$catalog=[IO.File]::ReadAllText([IO.Path]::GetFullPath($CatalogPath),[Text.UTF8Encoding]::new($false,$true))|ConvertFrom-Json -Depth 100
$matches=@($catalog.contracts|Where-Object contract_id -CEQ $ContractId)
if($matches.Count -ne 1){throw "Static contract is not unique: $ContractId"}
$contract=$matches[0];$observations=[Collections.Generic.List[object]]::new();$untrusted=$false
foreach($input in [object[]]$contract.inputs){
    $full=[IO.Path]::GetFullPath((Join-Path $repository ([string]$input.path)))
    if(-not $full.StartsWith($repository.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){$untrusted=$true;$observations.Add([ordered]@{path=$input.path;state='OUTSIDE_REPOSITORY'});continue}
    if(-not(Test-Path -LiteralPath $full -PathType Leaf)){$observations.Add([ordered]@{path=$input.path;state='MISSING'});continue}
    $actual=(Get-FileHash $full -Algorithm SHA256).Hash.ToLowerInvariant();$state=if([string]::IsNullOrWhiteSpace([string]$input.sha256)-or$actual-ceq[string]$input.sha256){'SATISFIED'}else{'VIOLATED'}
    $observations.Add([ordered]@{path=$input.path;state=$state;sha256=$actual})
}
$violations=@($observations|Where-Object state -in @('MISSING','VIOLATED')).Count
$outcome=if($untrusted){'EVIDENCE_UNTRUSTED'}elseif($violations-gt0){'CONTRACT_VIOLATED'}else{'CONTRACT_SATISFIED'}
$result=[ordered]@{contract_class='SEMANTIC';semantic_domain=[string]$contract.semantic_domain;outcome=$outcome;contract_id=$ContractId;observations=[object[]]$observations.ToArray()}
$fullResult=[IO.Path]::GetFullPath($ResultPath);[void][IO.Directory]::CreateDirectory((Split-Path -Parent $fullResult));[IO.File]::WriteAllText($fullResult,((ConvertTo-Json $result -Depth 30).Replace("`r`n","`n")+"`n"),$utf8)
if($outcome-ceq'CONTRACT_SATISFIED'){exit 0};if($outcome-ceq'CONTRACT_VIOLATED'){exit 1};exit 3

#requires -Version 7.2
[CmdletBinding()]param()
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop'
$script:Cases=0;$script:Assertions=0
function A([bool]$c,[string]$m){$script:Assertions++;if(-not$c){throw "TEST FAILURE: $m"}}
function C([string]$n,[scriptblock]$b){$script:Cases++;&$b;Write-Host "PASS: $n"}
$catalog=Get-Content (Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-static-contracts.json') -Raw|ConvertFrom-Json -Depth 30
C 'catalog has pure static semantic contracts' {A(@($catalog.contracts).Count -eq 3) 'contract count differs';A(@($catalog.contracts|Where-Object semantic_domain -CEQ 'PRODUCT').Count -eq 2) 'product domains differ';A(@($catalog.contracts|Where-Object semantic_domain -CEQ 'SCIENTIFIC').Count -eq 1) 'scientific domain differs'}
C 'all static inputs are immutable and present' {foreach($c in $catalog.contracts){foreach($i in $c.inputs){$p=Join-Path $PSScriptRoot ('../../..\'+$i.path);A(Test-Path -LiteralPath $p -PathType Leaf) "missing $($i.path)";A((Get-FileHash $p -Algorithm SHA256).Hash.ToLowerInvariant()-ceq$i.sha256) "hash differs $($i.path)"}}}
Write-Host "verification-static-semantic.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

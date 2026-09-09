#requires -Version 7.2
[CmdletBinding()]param()
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-process-projections.psm1') -Force
$script:Cases=0;$script:Assertions=0
function A([bool]$c,[string]$m){$script:Assertions++;if(-not$c){throw "TEST FAILURE: $m"}}
function C([string]$n,[scriptblock]$b){$script:Cases++;&$b;Write-Host "PASS: $n"}
C 'semantic zero is satisfied' {$r=ConvertFrom-VerificationProcessEvidence ([pscustomobject]@{completion_state='COMPLETED';inner_exit_code=0}) SEMANTIC SCIENTIFIC;A($r.outcome-ceq'CONTRACT_SATISFIED') 'zero exit changed'}
C 'semantic nonzero is violated' {$r=ConvertFrom-VerificationProcessEvidence ([pscustomobject]@{completion_state='COMPLETED';inner_exit_code=9}) SEMANTIC PRODUCT;A($r.outcome-ceq'CONTRACT_VIOLATED') 'nonzero was not semantic violation'}
C 'lost evidence is untrusted' {$r=ConvertFrom-VerificationProcessEvidence ([pscustomobject]@{completion_state='LAUNCH_FAILED';inner_exit_code=$null}) SAFETY;A($r.outcome-ceq'EVIDENCE_UNTRUSTED') 'lost evidence was classified'}
C 'domain is mandatory only for semantic' {try{ConvertFrom-VerificationProcessEvidence ([pscustomobject]@{completion_state='COMPLETED';inner_exit_code=0}) SEMANTIC|Out-Null;throw 'unexpected'}catch{A($_.Exception.Message-match'semantic_domain') 'missing domain not rejected'}}
C 'diagnostic exits remain separate from acceptance' {$ok=[pscustomobject]@{completion_state='COMPLETED';inner_exit_code=0};$bad=[pscustomobject]@{completion_state='COMPLETED';inner_exit_code=7};$missing=[pscustomobject]@{completion_state='LAUNCH_FAILED';inner_exit_code=$null};A((ConvertFrom-VerificationProcessEvidence $ok STYLE_DIAGNOSTIC).outcome-ceq'DIAGNOSTIC_CLEAR') 'diagnostic zero not clear';A((ConvertFrom-VerificationProcessEvidence $bad STYLE_DIAGNOSTIC).outcome-ceq'DIAGNOSTIC_FINDING') 'diagnostic nonzero not finding';A((ConvertFrom-VerificationProcessEvidence $missing STYLE_DIAGNOSTIC).outcome-ceq'DIAGNOSTIC_UNAVAILABLE') 'diagnostic missing evidence not unavailable'}
Write-Host "verification-process-projection.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

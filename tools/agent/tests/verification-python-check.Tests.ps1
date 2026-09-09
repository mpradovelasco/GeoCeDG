#requires -Version 7.2
[CmdletBinding()]param()
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-process-projections.psm1') -Force
$script:Cases=0;$script:Assertions=0
function A([bool]$c,[string]$m){$script:Assertions++;if(-not$c){throw "TEST FAILURE: $m"}}
function C([string]$n,[scriptblock]$b){$script:Cases++;&$b;Write-Host "PASS: $n"}
$source=[IO.File]::ReadAllText((Join-Path $PSScriptRoot '../checks/python-check-evidence-producer.ps1'))
C 'producer uses exact cedg_env check command' {A($source.Contains("`$checkArgs=@('run','--no-capture-output','-n','cedg_env','python',`$scriptFull,'--check')")) 'command array differs';A(-not$source.Contains('om_env')) 'producer references om_env'}
C 'producer has no text severity inference' {A(-not($source-match'(?i)stdout.*match|stderr.*match')) 'producer parses prose';A(-not($source-match'(?i)timeout|Kill\(')) 'producer contains timeout/kill'}
C 'five unique Python check commands are catalogued' {$catalog=Get-Content (Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-static-contracts.json') -Raw|ConvertFrom-Json -Depth 30;$c=@($catalog.contracts|Where-Object contract_id -CEQ 'scientific.python-reference-inputs')[0];A(@($c.inputs).Count -eq 5) 'unique Python inputs differ';A($source.Contains("environment_name='cedg_env'")) 'environment provenance missing'}
C 'exact interpreter and import provenance is required before scientific exit is interpreted' {
    $prefix=Join-Path ([IO.Path]::GetTempPath()) 'cedg_env'
    $identity=[pscustomobject]@{python_implementation='CPython';python_version='3.12.13';executable=(Join-Path $prefix 'python.exe');sys_prefix=$prefix;conda_prefix=$prefix;conda_default_env='cedg_env';mpmath_version='1.4.1';mpmath_file=(Join-Path $prefix 'Lib/site-packages/mpmath/__init__.py')}
    $evidence=[pscustomobject]@{completion_state='COMPLETED';inner_exit_code=0;identity_exit_code=0;identity_state='PRESENT';environment_name='cedg_env';identity=$identity}
    $pass=ConvertFrom-VerificationPythonCheckEvidence $evidence SCIENTIFIC
    A($pass.outcome -ceq 'CONTRACT_SATISFIED') 'valid Python identity was rejected'
    $evidence.inner_exit_code=8
    A((ConvertFrom-VerificationPythonCheckEvidence $evidence SCIENTIFIC).outcome -ceq 'CONTRACT_VIOLATED') 'nonzero --check was not violated'
    $evidence.inner_exit_code=0;$identity.mpmath_file=Join-Path ([IO.Path]::GetTempPath()) 'outside/mpmath.py'
    A((ConvertFrom-VerificationPythonCheckEvidence $evidence SCIENTIFIC).outcome -ceq 'EVIDENCE_UNTRUSTED') 'outside import origin was trusted'
}
Write-Host "verification-python-check.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

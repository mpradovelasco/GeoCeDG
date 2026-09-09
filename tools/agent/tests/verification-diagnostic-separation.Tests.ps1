#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
function Assert-Case([bool]$Condition,[string]$Message){$script:Assertions++;if(-not $Condition){throw "TEST FAILURE: $Message"}}
function Invoke-Case([string]$Name,[scriptblock]$Action){$script:Cases++;&$Action;Write-Host "PASS: $Name"}

$identity=[pscustomobject]@{base_commit=$null;base_tree=$null;candidate_commit=('1'*40);candidate_tree=('2'*40);environment_fingerprint=('3'*64)}
$semantic=[pscustomobject][ordered]@{check_id='semantic.contract';node_kind='ACCEPTANCE_LEAF';contract_class='SEMANTIC';semantic_domain='MIXED';status='CONTRACT_SATISFIED';command_identity=('4'*64);exit_code=0;cause=$null;evidence=[object[]]@();dependencies=[string[]]@();duration_ms=1}

Invoke-Case 'arbitrarily many diagnostics do not alter acceptance coverage or exit' {
    $diagnostics=foreach($index in 1..64){[pscustomobject][ordered]@{check_id="style.$index";node_kind='DIAGNOSTIC_LEAF';contract_class='STYLE_DIAGNOSTIC';outcome='DIAGNOSTIC_FINDING';command_identity=('5'*64);exit_code=9;cause='fixture';evidence=[object[]]@();dependencies=[string[]]@();duration_ms=$index}}
    $report=New-VerificationAggregatedReport -RunId diagnostics -Profile FINAL -Identity $identity -ExecutionPlanHash ('6'*64) -AcceptanceResults @($semantic) -DiagnosticResults $diagnostics -RequiredAcceptanceIds @('semantic.contract')
    Assert-Case ($report.acceptance_verdict -ceq 'ACCEPTED') 'Diagnostics changed acceptance.'
    Assert-Case ($report.coverage_verdict -ceq 'COMPLETE') 'Diagnostics changed coverage.'
    Assert-Case ($report.diagnostic_summary.findings -eq 64) 'Diagnostic findings were not all retained.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 0) 'Diagnostics changed the process exit.'
    Assert-Case (@($report.coverage.required_check_ids) -ccontains 'semantic.contract') 'Semantic coverage was omitted.'
    Assert-Case (@($report.coverage.required_check_ids | Where-Object { $_ -like 'style.*' }).Count -eq 0) 'Diagnostics entered acceptance coverage.'
}
Invoke-Case 'diagnostic unavailability remains non-acceptance data' {
    $diagnostic=[pscustomobject][ordered]@{check_id='history.unavailable';node_kind='DIAGNOSTIC_LEAF';contract_class='HISTORICAL_CONSISTENCY_DIAGNOSTIC';outcome='DIAGNOSTIC_UNAVAILABLE';command_identity=('7'*64);exit_code=$null;cause='raw diagnostic projection unavailable';evidence=[object[]]@();dependencies=[string[]]@();duration_ms=2}
    $report=New-VerificationAggregatedReport -RunId unavailable -Profile FINAL -Identity $identity -ExecutionPlanHash ('8'*64) -AcceptanceResults @($semantic) -DiagnosticResults @($diagnostic) -RequiredAcceptanceIds @('semantic.contract')
    Assert-Case ($report.acceptance_verdict -ceq 'ACCEPTED') 'Unavailable diagnostic changed acceptance.'
    Assert-Case ($report.coverage_verdict -ceq 'COMPLETE') 'Unavailable diagnostic changed coverage.'
    Assert-Case ($report.diagnostic_summary.unavailable -eq 1) 'Unavailable diagnostic was not visible.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 0) 'Unavailable diagnostic changed exit.'
}

Write-Host "verification-diagnostic-separation.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
. (Join-Path $PSScriptRoot 'fixtures/verification-environment-fixture.ps1')

$script:Cases = 0
$script:Assertions = 0
$schemaPath = Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-result.schema.json'
$identity = New-VerificationTestEnvironmentIdentity -BaseCommit ('a' * 40) `
    -BaseTree ('b' * 40) -CandidateCommit ('c' * 40) `
    -CandidateTree ('d' * 40) -CompatibilitySignature ('e' * 64)
$planHash = 'f' * 64
$commandHash = '1' * 64

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}
function Assert-CaseThrows {
    param([scriptblock]$Action, [string]$Pattern)
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_.Exception }
    Assert-Case ($null -ne $observed) "Expected exception matching $Pattern."
    Assert-Case ($observed.Message -match $Pattern) "Unexpected exception: $($observed.Message)"
}
function Assert-CaseSchemaRejected {
    param([scriptblock]$Action)
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_ }
    Assert-Case ($null -ne $observed) 'Expected JSON schema rejection.'
    Assert-Case ($observed.FullyQualifiedErrorId -ceq `
            'InvalidJsonAgainstSchemaDetailed,Microsoft.PowerShell.Commands.TestJsonCommand') `
        'Schema rejection did not retain the nominal Test-Json error identity.'
    Assert-Case ($observed.CategoryInfo.Category -eq [Management.Automation.ErrorCategory]::InvalidData) `
        'Schema rejection did not retain the InvalidData category.'
}
function Invoke-Case {
    param([string]$Name, [scriptblock]$Action)
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}
function New-Acceptance {
    param([string]$Id, [string]$Contract, [string]$Status, [double]$Duration = 1)
    $value = [ordered]@{
        check_id = $Id
        node_kind = 'ACCEPTANCE_LEAF'
        contract_class = $Contract
        status = $Status
        coverage_state = $(if ($Status -ceq 'EVIDENCE_UNTRUSTED') {
                'UNTRUSTED'
            } elseif ($Status -ceq 'NOT_RUN_DEPENDENCY') {
                'INCOMPLETE'
            } else { 'COMPLETE' })
        command_identity = $commandHash
        exit_code = $(if ($Status -ceq 'CONTRACT_SATISFIED') { 0 } else { 1 })
        cause = $null
        evidence = [object[]]@()
        dependencies = [string[]]@()
        duration_ms = $Duration
    }
    if ($Contract -ceq 'SEMANTIC') { $value.semantic_domain = 'PRODUCT' }
    return [pscustomobject]$value
}
function New-Diagnostic {
    param([string]$Id, [string]$Class, [string]$Outcome, [double]$Duration = 1)
    return [pscustomobject][ordered]@{
        check_id = $Id
        node_kind = 'DIAGNOSTIC_LEAF'
        contract_class = $Class
        outcome = $Outcome
        command_identity = $commandHash
        exit_code = $(if ($Outcome -ceq 'DIAGNOSTIC_CLEAR') { 0 } else { 9 })
        cause = $null
        evidence = [object[]]@()
        dependencies = [string[]]@()
        duration_ms = $Duration
    }
}

Invoke-Case 'diagnostic findings remain separate from accepted result' {
    $acceptance = New-Acceptance product.ok SEMANTIC CONTRACT_SATISFIED
    $diagnostics = [Collections.Generic.List[object]]::new()
    foreach ($index in 1..32) {
        $diagnostics.Add((New-Diagnostic ("style.$index") STYLE_DIAGNOSTIC DIAGNOSTIC_FINDING))
    }
    $report = New-VerificationAggregatedReport -RunId report-one -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @($acceptance) -DiagnosticResults ([object[]]$diagnostics.ToArray()) -RequiredAcceptanceIds @('product.ok')
    Assert-Case ($report.acceptance_verdict -ceq 'ACCEPTED') 'Diagnostics altered acceptance verdict.'
    Assert-Case ($report.coverage_verdict -ceq 'COMPLETE') 'Diagnostics altered coverage.'
    Assert-Case ($report.diagnostic_summary.findings -eq 32) 'Diagnostic count changed.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 0) 'Diagnostics altered exit code.'
    Assert-Case (Assert-VerificationJsonSchema $report $schemaPath) 'Accepted report failed its schema.'
}
Invoke-Case 'result hash excludes duration and run timestamps' {
    $first = New-VerificationAggregatedReport -RunId first -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance product.ok SEMANTIC CONTRACT_SATISFIED 1)
    ) -RequiredAcceptanceIds @('product.ok') -StartedAt ([datetime]'2026-01-01T00:00:00Z') -FinishedAt ([datetime]'2026-01-01T00:00:01Z')
    $second = New-VerificationAggregatedReport -RunId second -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance product.ok SEMANTIC CONTRACT_SATISFIED 999999)
    ) -RequiredAcceptanceIds @('product.ok') -StartedAt ([datetime]'2027-01-01T00:00:00Z') -FinishedAt ([datetime]'2027-02-01T00:00:00Z')
    Assert-Case ($first.result_hash -ceq $second.result_hash) 'Informational timing changed result identity.'
    $second.environment_observation.effective_user = 'another-user'
    $second.environment_observation.repository_root = 'D:\another\checkout'
    $second.environment_observation.resolved_cedg_env_prefix = 'D:\portable\cedg_env'
    $second.environment_observation_hash = Get-VerificationDeterministicHash `
        -Value $second.environment_observation
    Assert-Case ($first.result_hash -ceq (Get-VerificationResultHash $second)) `
        'Volatile environment observation changed result identity.'
    $first.acceptance_results[0].evidence = @([pscustomobject]@{ name = 'PAYLOAD'; state = 'PRESENT'; path = 'C:\task-one\payload'; sha256 = ('2' * 64) })
    $second.acceptance_results[0].evidence = @([pscustomobject]@{ name = 'PAYLOAD'; state = 'PRESENT'; path = 'D:\task-two\payload'; sha256 = ('2' * 64) })
    $first.acceptance_results[0].cause = 'host detail at C:\task-one'
    $second.acceptance_results[0].cause = 'host detail at D:\task-two'
    Assert-Case ((Get-VerificationResultHash $first) -ceq (Get-VerificationResultHash $second)) 'Task-owned evidence location changed result identity.'
    $second.acceptance_results[0].status = 'CONTRACT_VIOLATED'
    Assert-Case ($first.result_hash -cne (Get-VerificationResultHash $second)) 'Contract change did not change result identity.'
}
Invoke-Case 'semantic safety and core rejections have stable exits' {
    $semantic = New-VerificationAggregatedReport -RunId semantic -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance science.bad SEMANTIC CONTRACT_VIOLATED)
    ) -RequiredAcceptanceIds @('science.bad')
    $safety = New-VerificationAggregatedReport -RunId safety -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance safety.bad SAFETY CONTRACT_VIOLATED)
    ) -RequiredAcceptanceIds @('safety.bad')
    $core = New-VerificationAggregatedReport -RunId core -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance core.bad VERIFICATION_CORE CONTRACT_VIOLATED)
    ) -RequiredAcceptanceIds @('core.bad')
    Assert-Case ($semantic.acceptance_verdict -ceq 'REJECTED_SEMANTIC' -and (Get-VerificationSupervisorExitCode $semantic) -eq 1) 'Semantic exit contract changed.'
    Assert-Case ($safety.acceptance_verdict -ceq 'REJECTED_SAFETY' -and (Get-VerificationSupervisorExitCode $safety) -eq 2) 'Safety exit contract changed.'
    Assert-Case ($core.acceptance_verdict -ceq 'REJECTED_VERIFICATION_CORE' -and (Get-VerificationSupervisorExitCode $core) -eq 3) 'Core exit contract changed.'
}
Invoke-Case 'untrusted coverage is distinct from contract violation' {
    $report = New-VerificationAggregatedReport -RunId untrusted -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance product.unknown SEMANTIC EVIDENCE_UNTRUSTED)
    ) -RequiredAcceptanceIds @('product.unknown')
    Assert-Case ($report.coverage_verdict -ceq 'UNTRUSTED') 'Untrusted coverage was not explicit.'
    Assert-Case ($report.acceptance_verdict -ceq 'REJECTED_VERIFICATION_CORE') 'Required coverage gap was accepted.'
    Assert-Case ((Get-VerificationSupervisorExitCode $report) -eq 3) 'Coverage exit contract changed.'
}
Invoke-Case 'semantic violation and incomplete evidence remain simultaneously visible' {
    $result = New-Acceptance science.partial SEMANTIC CONTRACT_VIOLATED
    $result.coverage_state = 'INCOMPLETE'
    $report = New-VerificationAggregatedReport -RunId partial -Profile FINAL `
        -Identity $identity -ExecutionPlanHash $planHash `
        -AcceptanceResults @($result) -RequiredAcceptanceIds @('science.partial')
    Assert-Case ($report.acceptance_results[0].status -ceq 'CONTRACT_VIOLATED') `
        'Semantic violation was masked by incomplete coverage.'
    Assert-Case ($report.coverage_verdict -ceq 'INCOMPLETE') `
        'Simultaneous coverage loss was not retained.'
    Assert-Case ($report.acceptance_verdict -ceq 'REJECTED_VERIFICATION_CORE') `
        'Incomplete required coverage did not retain precedence in the global verdict.'
}
Invoke-Case 'schema prohibits mixed channel terminology' {
    $report = New-VerificationAggregatedReport -RunId schema-negative -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @(
        (New-Acceptance product.ok SEMANTIC CONTRACT_SATISFIED)
    ) -DiagnosticResults @(
        (New-Diagnostic style.bad STYLE_DIAGNOSTIC DIAGNOSTIC_FINDING)
    ) -RequiredAcceptanceIds @('product.ok')
    $report.diagnostic_findings[0].outcome = 'PASS'
    Assert-CaseSchemaRejected { Assert-VerificationJsonSchema $report $schemaPath }
}
Invoke-Case 'aggregation rejects duplicate or uncovered acceptance results' {
    $result = New-Acceptance product.ok SEMANTIC CONTRACT_SATISFIED
    Assert-CaseThrows {
        New-VerificationAggregatedReport -RunId duplicate -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @($result, $result) -RequiredAcceptanceIds @('product.ok')
    } 'unique, nonempty result identifiers'
    Assert-CaseThrows {
        New-VerificationAggregatedReport -RunId uncovered -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @($result) -RequiredAcceptanceIds @('other.contract')
    } 'absent from required coverage'
}
Invoke-Case 'incomplete durable journal produces no acceptance verdict' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-verification-journal-' + [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($root)
    $marker = Join-Path $root '.fixture-owner'
    [IO.File]::WriteAllText($marker, 'verification-journal', [Text.UTF8Encoding]::new($false))
    try {
        $journalPath = Join-Path $root 'run-journal.json'
        [void](Write-VerificationAtomicJson $journalPath ([ordered]@{
            schema_version = 1
            journal_kind = 'GEOCEDG_VERIFICATION_RUN_JOURNAL'
            run_id = 'interrupted-run'
            state = 'RUNNING'
            completed_check_ids = @('static.one')
        }))
        $interrupted = Get-VerificationInterruptedRun $journalPath
        Assert-Case ($interrupted.run_state -ceq 'RUN_INTERRUPTED') 'Interrupted state was lost.'
        Assert-Case ($interrupted.acceptance_verdict -ceq 'NOT_PRODUCED') 'Interrupted run produced an acceptance verdict.'
        Assert-Case ((Get-VerificationSupervisorExitCode $interrupted) -eq 4) 'Interrupted run exit contract changed.'
        Assert-Case ($interrupted.completed_check_ids -ccontains 'static.one') 'Completed journal evidence was lost.'
    } finally {
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker -PathType Leaf) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-journal') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else {
            throw "Fixture cleanup refused unexpected path: $resolved"
        }
    }
}

Write-Host "verification-report.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

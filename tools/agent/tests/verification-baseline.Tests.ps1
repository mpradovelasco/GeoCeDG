#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$commandHash = '1' * 64
$planHash = '2' * 64
$environmentHash = '3' * 64

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}

function Invoke-Case {
    param([string]$Name, [scriptblock]$Body)
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}

function New-Observation {
    param(
        [string]$Id,
        [string]$Status,
        [string]$EvidenceHash = ('4' * 64),
        [string]$CommandHash = $commandHash
    )
    return [pscustomobject][ordered]@{
        check_id = $Id
        node_kind = 'ACCEPTANCE_LEAF'
        contract_class = 'SEMANTIC'
        semantic_domain = 'SCIENTIFIC'
        status = $Status
        coverage_state = $(if ($Status -ceq 'EVIDENCE_UNTRUSTED') {
                'UNTRUSTED'
            } elseif ($Status -ceq 'NOT_RUN_DEPENDENCY') {
                'INCOMPLETE'
            } else { 'COMPLETE' })
        command_identity = $CommandHash
        exit_code = $(if ($Status -ceq 'CONTRACT_SATISFIED') { 0 } else { 1 })
        cause = $null
        evidence = @([pscustomobject]@{
            name = 'SCIENTIFIC_PAYLOAD'
            state = 'PRESENT'
            path = 'task-owned/payload.json'
            sha256 = $EvidenceHash
        })
        dependencies = [string[]]@()
        duration_ms = 0
    }
}

function New-Report {
    param([string]$CommitDigit, [object[]]$Results, [string]$Plan = $planHash,
        [string]$Environment = $environmentHash, [string]$BaseDigit)
    $identity = [pscustomobject]@{
        base_commit = $(if ($BaseDigit) { $BaseDigit * 40 } else { $null })
        base_tree = $(if ($BaseDigit) { $BaseDigit * 40 } else { $null })
        candidate_commit = $CommitDigit * 40
        candidate_tree = $CommitDigit * 40
        environment_fingerprint = $Environment
    }
    return New-VerificationAggregatedReport -RunId ('baseline-' + $CommitDigit) `
        -Profile FINAL -Identity $identity -ExecutionPlanHash $Plan `
        -AcceptanceResults $Results `
        -RequiredAcceptanceIds ([string[]]@($Results | ForEach-Object { $_.check_id }))
}

Invoke-Case 'pass to fail is a candidate regression and not acceptable' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_SATISFIED))) `
        (New-Report b @((New-Observation science.one CONTRACT_VIOLATED)) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'PASS_TO_FAIL') 'PASS to FAIL was not classified.'
    Assert-Case $comparison.candidate_regression 'PASS to FAIL did not mark a regression.'
    Assert-Case ($comparison.final_acceptability -ceq 'NOT_ACCEPTABLE') 'A failing candidate was accepted.'
}

Invoke-Case 'same preexisting failure is non-regression but still not acceptable' {
    $failure = New-Observation science.one CONTRACT_VIOLATED
    $comparison = Compare-VerificationBaselineResults (New-Report a @($failure)) `
        (New-Report b @((New-Observation science.one CONTRACT_VIOLATED)) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'FAIL_TO_SAME_FAIL') 'Identical failure was not recognized.'
    Assert-Case (-not $comparison.candidate_regression) 'Identical preexisting failure became a regression.'
    Assert-Case ($comparison.final_acceptability -ceq 'NOT_ACCEPTABLE') 'Preexisting semantic failure was treated as acceptable.'
}

Invoke-Case 'changed failure requires causal analysis' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_VIOLATED ('4' * 64)))) `
        (New-Report b @((New-Observation science.one CONTRACT_VIOLATED ('5' * 64))) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'FAIL_CHANGED') 'Changed failure was not identified.'
    Assert-Case $comparison.candidate_regression 'Changed failure did not mark a regression.'
}

Invoke-Case 'failure to pass records repair and final acceptability' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_VIOLATED))) `
        (New-Report b @((New-Observation science.one CONTRACT_SATISFIED)) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'FAIL_TO_PASS') 'Repair was not classified.'
    Assert-Case (-not $comparison.candidate_regression) 'Repair became a regression.'
    Assert-Case ($comparison.final_acceptability -ceq 'ACCEPTABLE') 'Passing current semantic result was not acceptable.'
}

Invoke-Case 'newly exposed failure is explicit' {
    $comparison = Compare-VerificationBaselineResults (New-Report a @()) `
        (New-Report b @((New-Observation science.new CONTRACT_VIOLATED)) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'NOT_RUN_TO_FAIL') 'Newly exposed failure was hidden.'
    Assert-Case $comparison.candidate_regression 'Newly exposed failure did not require analysis.'
}

Invoke-Case 'incomparable plan and environment identities are rejected' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_SATISFIED))) `
        (New-Report b @((New-Observation science.one CONTRACT_SATISFIED)) ('6' * 64) ('7' * 64) a)
    Assert-Case ($comparison.state -ceq 'NOT_COMPARABLE') 'Identity mismatch was compared.'
    Assert-Case (@($comparison.identity_mismatches).Count -eq 2) 'Identity mismatch fields were not complete.'
    Assert-Case ($null -eq $comparison.candidate_regression) 'Incomparable evidence received a regression verdict.'
}

Invoke-Case 'checker identity change is explicit and uses the current result' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_SATISFIED))) `
        (New-Report b @((New-Observation science.one CONTRACT_SATISFIED ('4' * 64) ('8' * 64))) -BaseDigit a)
    Assert-Case ($comparison.comparisons[0].comparison -ceq 'CHECK_IDENTITY_CHANGED') 'Checker identity change was reused.'
    Assert-Case ($comparison.comparisons[0].final_acceptable) 'Passing current checker evidence was discarded.'
    Assert-Case (-not $comparison.candidate_regression) 'Passing current checker was called a regression.'
}

Invoke-Case 'baseline artifact must match the candidate declared base objects' {
    $comparison = Compare-VerificationBaselineResults `
        (New-Report a @((New-Observation science.one CONTRACT_SATISFIED))) `
        (New-Report b @((New-Observation science.one CONTRACT_SATISFIED)))
    Assert-Case ($comparison.state -ceq 'NOT_COMPARABLE') 'Unbound baseline artifact was reused.'
    Assert-Case (@($comparison.identity_mismatches) -ccontains 'base_commit') 'Base commit mismatch was omitted.'
    Assert-Case (@($comparison.identity_mismatches) -ccontains 'base_tree') 'Base tree mismatch was omitted.'
}

Write-Host "verification-baseline.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

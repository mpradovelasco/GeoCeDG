#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$pwshPath = (Get-Process -Id $PID).Path

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}
function Invoke-Case {
    param([string]$Name, [scriptblock]$Action)
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}
function New-Node {
    param([string]$Id, [string[]]$Locks = @(), [string[]]$Dependencies = @())
    return [pscustomobject][ordered]@{
        check_id = $Id
        node_kind = 'ACCEPTANCE_LEAF'
        contract_class = 'VERIFICATION_CORE'
        tier = 'INFRA_UNIT'
        command = [ordered]@{ kind = 'PROCESS'; identity = $Id; executable = $pwshPath }
        argv = @('-NoProfile', '-Command', 'exit 0')
        working_directory = '.'
        dependencies = $Dependencies
        platforms = @('WINDOWS')
        impact_paths = @()
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = $Locks
        side_effects = @('TASK_OWNED_OUTPUT')
        output_adapter = 'EXIT_CODE_V1'
        required_for_profiles = @('FINAL')
    }
}
function New-Registry {
    param([object[]]$Nodes)
    return [pscustomobject][ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-registry.schema.json'
        schema_version = 1
        registry_id = 'resource.fixture'
        profiles = @([ordered]@{
            profile_id = 'FINAL'; coverage_state = 'COMPLETE'; missing_check_ids = @()
        })
        compatibility_mappings = @()
        phase_selections = @()
        nodes = $Nodes
    }
}

$nodes = @(
    (New-Node alpha @('gradle')),
    (New-Node beta @('gradle')),
    (New-Node gamma @('docs')),
    (New-Node delta),
    (New-Node epsilon @() @('alpha'))
)

Invoke-Case 'resource locks never occur twice in one executable batch' {
    $plan = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
    $batches = Get-VerificationExecutionBatches $plan
    foreach ($batch in $batches) {
        $observed = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
        foreach ($id in $batch.check_ids) {
            $node = @($plan.nodes | Where-Object { $_.check_id -ceq $id })[0]
            foreach ($lock in $node.resource_locks) {
                Assert-Case ($observed.Add([string]$lock)) "Resource lock $lock was duplicated in a batch."
            }
        }
    }
}
Invoke-Case 'independent checks share a batch while conflicting checks do not' {
    $plan = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
    $batches = Get-VerificationExecutionBatches $plan
    $alphaBatch = @($batches | Where-Object { $_.check_ids -ccontains 'alpha' })[0]
    $betaBatch = @($batches | Where-Object { $_.check_ids -ccontains 'beta' })[0]
    Assert-Case ($alphaBatch.batch_index -ne $betaBatch.batch_index) 'Conflicting resource users shared a batch.'
    Assert-Case ($alphaBatch.check_ids -ccontains 'gamma') 'Independent locked check was not grouped safely.'
    Assert-Case ($alphaBatch.check_ids -ccontains 'delta') 'Independent unlocked check was not grouped safely.'
}
Invoke-Case 'dependencies never share an execution batch' {
    $plan = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
    $batches = Get-VerificationExecutionBatches $plan
    $alphaIndex = @($batches | Where-Object { $_.check_ids -ccontains 'alpha' })[0].batch_index
    $epsilonIndex = @($batches | Where-Object { $_.check_ids -ccontains 'epsilon' })[0].batch_index
    Assert-Case ($epsilonIndex -gt $alphaIndex) 'Dependent check was scheduled before its dependency completed.'
}
Invoke-Case 'each check is scheduled exactly once' {
    $plan = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
    $all = [string[]]@((Get-VerificationExecutionBatches $plan).check_ids)
    Assert-Case ($all.Count -eq $nodes.Count) 'Batch plan changed check count.'
    Assert-Case (@($all | Sort-Object -Unique -CaseSensitive).Count -eq $nodes.Count) 'Batch plan duplicated a check.'
    Assert-Case (($all | Sort-Object -CaseSensitive) -join [char]0 -ceq
        (($nodes.check_id | Sort-Object -CaseSensitive) -join [char]0)) 'Batch plan omitted or added a check.'
}
Invoke-Case 'plan and batch order are deterministic across registry input order' {
    $forward = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
    $reverse = Resolve-VerificationRegistryPlan (New-Registry ([object[]]@($nodes | Sort-Object check_id -Descending))) FINAL
    Assert-Case ($forward.execution_plan_hash -ceq $reverse.execution_plan_hash) 'Plan hash depended on registry order.'
    $forwardIds = [string[]]@((Get-VerificationExecutionBatches $forward).check_ids)
    $reverseIds = [string[]]@((Get-VerificationExecutionBatches $reverse).check_ids)
    Assert-Case (($forwardIds -join [char]0) -ceq ($reverseIds -join [char]0)) 'Batch order depended on registry order.'
}
Invoke-Case 'plan and batch identities are independent of current culture' {
    $previousCulture = [Globalization.CultureInfo]::CurrentCulture
    try {
        [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('en-US')
        $english = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
        $englishIds = [string[]]@((Get-VerificationExecutionBatches $english).check_ids)
        [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('tr-TR')
        $turkish = Resolve-VerificationRegistryPlan (New-Registry $nodes) FINAL
        $turkishIds = [string[]]@((Get-VerificationExecutionBatches $turkish).check_ids)
    } finally {
        [Globalization.CultureInfo]::CurrentCulture = $previousCulture
    }
    Assert-Case ($english.execution_plan_hash -ceq $turkish.execution_plan_hash) 'Current culture changed plan identity.'
    Assert-Case (($englishIds -join [char]0) -ceq ($turkishIds -join [char]0)) 'Current culture changed batch order.'
}

Write-Host "verification-concurrency.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$schemaPath = Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-registry.schema.json'
$pwshPath = (Get-Process -Id $PID).Path

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
function Invoke-Case {
    param([string]$Name, [scriptblock]$Action)
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}
function Copy-Value {
    param([object]$Value)
    return ConvertFrom-Json (ConvertTo-Json -InputObject $Value -Depth 100) -Depth 100
}
function New-ProcessNode {
    param(
        [string]$Id,
        [string]$Kind,
        [string]$Contract,
        [string[]]$Dependencies = @(),
        [string[]]$Profiles = @('FINAL')
    )
    $node = [ordered]@{
        check_id = $Id
        node_kind = $Kind
        tier = 'STATIC'
        command = [ordered]@{ kind = 'PROCESS'; identity = $Id; executable = $pwshPath }
        argv = @('-NoProfile', '-Command', 'exit 0')
        working_directory = '.'
        dependencies = $Dependencies
        platforms = @('WINDOWS', 'LINUX', 'MACOS')
        impact_paths = @('docs/café point.txt')
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = @()
        side_effects = @('TASK_OWNED_OUTPUT')
        output_adapter = 'EXIT_CODE_V1'
        required_for_profiles = $Profiles
    }
    if ($Kind -ceq 'PROCESS_PRODUCER') {
        $node.output_adapter = 'RAW_PROCESS_V1'
        $node.produces_evidence = @('PROCESS_RESULT')
    } else {
        $node.contract_class = $Contract
        if ($Contract -ceq 'SEMANTIC') { $node.semantic_domain = 'SCIENTIFIC' }
    }
    return [pscustomobject]$node
}
function New-ProjectionNode {
    param(
        [string]$Id,
        [string]$Producer,
        [string]$Contract,
        [string]$Projection = 'EXIT_CODE_ZERO',
        [string]$Adapter = 'PROJECTION_EXIT_CODE_V1',
        [string]$EvidenceName
    )
    $command = [ordered]@{
        kind = 'BUILTIN_PROJECTION'
        identity = $Id
        projection = $Projection
    }
    if (-not [string]::IsNullOrWhiteSpace($EvidenceName)) {
        $command.evidence_name = $EvidenceName
    }
    $node = [ordered]@{
        check_id = $Id
        node_kind = 'EVIDENCE_PROJECTION'
        contract_class = $Contract
        tier = 'STATIC'
        command = $command
        argv = @()
        working_directory = '.'
        dependencies = @($Producer)
        platforms = @('WINDOWS', 'LINUX', 'MACOS')
        impact_paths = @()
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = @()
        side_effects = @('NONE')
        output_adapter = $Adapter
        required_for_profiles = @('FINAL')
        producer_dependency = $Producer
    }
    if ($Contract -ceq 'SEMANTIC') { $node.semantic_domain = 'SCIENTIFIC' }
    return [pscustomobject]$node
}
function New-Registry {
    $producer = New-ProcessNode 'gradle.producer' 'PROCESS_PRODUCER' ''
    $scientific = New-ProjectionNode 'science.projection' 'gradle.producer' 'SEMANTIC'
    $diagnostic = New-ProjectionNode 'style.projection' 'gradle.producer' 'STYLE_DIAGNOSTIC'
    return [pscustomobject][ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-registry.schema.json'
        schema_version = 2
        registry_id = 'fixture.registry'
        catalogs = [object[]]@((ConvertFrom-Json ([IO.File]::ReadAllText((Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-registry.json'))) -Depth 100).catalogs)
        profiles = @([ordered]@{
            profile_id = 'FINAL'; coverage_state = 'COMPLETE'; missing_check_ids = @()
        })
        compatibility_mappings = @()
        phase_selections = @()
        nodes = @($producer, $scientific, $diagnostic)
    }
}

Invoke-Case 'valid producer and pure projections satisfy schema and graph' {
    $registry = New-Registry
    Assert-Case (Assert-VerificationRegistry $registry -SchemaPath $schemaPath) 'Valid registry was rejected.'
    $plan = Resolve-VerificationRegistryPlan $registry FINAL
    Assert-Case ($plan.nodes.Count -eq 3) 'Resolved plan did not include all nodes.'
    Assert-Case ($plan.nodes[0].check_id -ceq 'gradle.producer') 'Producer was not ordered before projections.'
    Assert-Case ($plan.execution_plan_hash -cmatch '^[0-9a-f]{64}$') 'Plan hash is invalid.'
}
Invoke-Case 'duplicate IDs are rejected' {
    $registry = New-Registry
    $registry.nodes += Copy-Value $registry.nodes[0]
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'Duplicate'
}
Invoke-Case 'missing dependencies are rejected' {
    $registry = New-Registry
    $registry.nodes[1].dependencies = @('missing.producer')
    $registry.nodes[1].producer_dependency = 'missing.producer'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'missing dependency'
}
Invoke-Case 'dependency cycles are rejected' {
    $registry = New-Registry
    $first = New-ProcessNode 'cycle.first' 'ACCEPTANCE_LEAF' 'VERIFICATION_CORE' @('cycle.second')
    $second = New-ProcessNode 'cycle.second' 'ACCEPTANCE_LEAF' 'VERIFICATION_CORE' @('cycle.first')
    $registry.nodes = @($first, $second)
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'cycle'
}
Invoke-Case 'invalid node kind is rejected' {
    $registry = New-Registry
    $registry.nodes[0].node_kind = 'MIXED'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'node_kind'
}
Invoke-Case 'mixed contract classes are rejected' {
    $registry = New-Registry
    $registry.nodes[1].contract_class = @('SEMANTIC', 'STYLE_DIAGNOSTIC')
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'Mixed contract'
}
Invoke-Case 'producer contract class is prohibited' {
    $registry = New-Registry
    $registry.nodes[0] | Add-Member -NotePropertyName contract_class -NotePropertyValue VERIFICATION_CORE
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'must not declare'
}
Invoke-Case 'schema independently enforces pure node-kind contracts' {
    $registry = New-Registry
    $registry.nodes[0] | Add-Member -NotePropertyName contract_class -NotePropertyValue VERIFICATION_CORE
    Assert-CaseThrows {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    } 'contract_class|schema'

    $diagnostic = New-ProcessNode 'bad.diagnostic' 'DIAGNOSTIC_LEAF' 'SEMANTIC'
    $registry = New-Registry
    $registry.nodes = @($diagnostic)
    Assert-CaseThrows {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    } 'enum|schema|contract_class'
}
Invoke-Case 'projection requires its declared producer dependency' {
    $registry = New-Registry
    $registry.nodes[1].dependencies = @()
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'must list producer_dependency'
}
Invoke-Case 'projection producer dependency must name a producer' {
    $registry = New-Registry
    $leaf = New-ProcessNode 'plain.leaf' 'ACCEPTANCE_LEAF' 'SEMANTIC'
    $registry.nodes = @($leaf, (New-ProjectionNode 'bad.projection' 'plain.leaf' 'SEMANTIC'))
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'PROCESS_PRODUCER'
}
Invoke-Case 'diagnostic-to-acceptance dependencies are prohibited' {
    $registry = New-Registry
    $diagnostic = New-ProcessNode 'diagnostic.leaf' 'DIAGNOSTIC_LEAF' 'GOVERNANCE_DIAGNOSTIC'
    $acceptance = New-ProcessNode 'acceptance.leaf' 'ACCEPTANCE_LEAF' 'SEMANTIC' @('diagnostic.leaf')
    $registry.nodes = @($diagnostic, $acceptance)
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'cannot depend on diagnostic'
}
Invoke-Case 'later-tier dependencies are rejected' {
    $registry = New-Registry
    $registry.nodes[0].tier = 'FINAL'
    $registry.nodes[1].tier = 'STATIC'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'later tier'
}
Invoke-Case 'dependencies cover every declared caller platform' {
    $registry = New-Registry
    $registry.nodes[0].platforms = @('WINDOWS')
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'unavailable on LINUX|unavailable on MACOS'
}
Invoke-Case 'projection and adapter must agree' {
    $registry = New-Registry
    $registry.nodes[1].output_adapter = 'PROJECTION_EVIDENCE_PRESENT_V1'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'does not match'
}
Invoke-Case 'file projection names exactly one declared producer artifact' {
    $registry = New-Registry
    $registry.nodes[0].produces_evidence = @('PROCESS_RESULT', 'PAYLOAD')
    $projection = New-ProjectionNode 'payload.projection' 'gradle.producer' 'SEMANTIC' 'EVIDENCE_PRESENT' 'PROJECTION_EVIDENCE_PRESENT_V1' 'PAYLOAD'
    $registry.nodes = @($registry.nodes[0], $projection)
    Assert-Case (Assert-VerificationRegistry $registry -SchemaPath $schemaPath) 'Named file projection was rejected.'

    $projection.command.evidence_name = 'UNDECLARED'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'declared file evidence'
}
Invoke-Case 'file evidence name is required only for file projections' {
    $registry = New-Registry
    $registry.nodes[1].command.evidence_name = 'PAYLOAD'
    Assert-CaseThrows {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    } 'evidence_name|schema'

    $registry = New-Registry
    $registry.nodes[0].impact_paths = @('.')
    Assert-Case (Assert-VerificationRegistry $registry -SchemaPath $schemaPath) 'Repository-wide impact path was rejected.'
}
Invoke-Case 'structured leaves require an explicit structured result' {
    $registry = New-Registry
    $leaf = New-ProcessNode 'structured.leaf' 'ACCEPTANCE_LEAF' 'VERIFICATION_CORE'
    $leaf.output_adapter = 'STRUCTURED_CONTRACT_V1'
    $registry.nodes = @($leaf)
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'requires structured_output'
}
Invoke-Case 'producer structured declarations are paired exactly' {
    $registry = New-Registry
    $registry.nodes[0].produces_evidence = @('STRUCTURED_RESULT')
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'together'
}
Invoke-Case 'empty registries and empty profile selections are rejected' {
    $registry = New-Registry
    $registry.nodes = @()
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'at least one node'
    $registry = New-Registry
    foreach ($node in $registry.nodes) { $node.required_for_profiles = @() }
    Assert-CaseThrows { Resolve-VerificationRegistryPlan $registry FINAL } 'selects no nodes'
}
Invoke-Case 'profile identities and coverage declarations are consistent' {
    $registry = New-Registry
    $registry.profiles += Copy-Value $registry.profiles[0]
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'Duplicate verification profile_id'

    $registry = New-Registry
    $registry.profiles[0].coverage_state = 'INCOMPLETE'
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'disagrees with missing_check_ids'
}
Invoke-Case 'compatibility selectors name existing canonical profiles exactly once' {
    $registry = New-Registry
    $registry.compatibility_mappings = @(
        [ordered]@{ selector = 'FULL'; profile = 'FINAL' },
        [ordered]@{ selector = 'FULL'; profile = 'FINAL' })
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'Duplicate verification compatibility selector'

    $registry = New-Registry
    $registry.compatibility_mappings = @([ordered]@{ selector = 'FULL'; profile = 'MISSING' })
    Assert-CaseThrows { Assert-VerificationRegistry $registry } 'targets missing profile'
}
Invoke-Case 'phase selection is exact and explicitly incomplete' {
    $registry = New-Registry
    $registry.profiles += [ordered]@{
        profile_id = 'PHASE'; coverage_state = 'INCOMPLETE';
        missing_check_ids = @('phase.default')
    }
    $registry.phase_selections = @([ordered]@{
        phase_id = 'G9A2'; coverage_state = 'INCOMPLETE';
        missing_check_ids = @('phase.g9a2'); required_check_ids = @('science.projection')
    })
    $plan = Resolve-VerificationRegistryPlan $registry PHASE G9A2
    Assert-Case ($plan.profile -ceq 'PHASE' -and $plan.selection -ceq 'G9A2') 'Exact PHASE selection changed.'
    Assert-Case ($plan.coverage_state -ceq 'INCOMPLETE') 'Incomplete phase claimed complete coverage.'
    Assert-Case (@($plan.missing_check_ids) -ccontains 'phase.g9a2') 'Phase-specific coverage gap was omitted.'
    Assert-CaseThrows { Resolve-VerificationRegistryPlan $registry PHASE missing } 'Unknown PHASE'
}

Write-Host "verification-registry.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

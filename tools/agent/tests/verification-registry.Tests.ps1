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
function Assert-CaseSchemaRejected {
    param([scriptblock]$Action)
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_ }
    Assert-Case ($null -ne $observed) 'Expected JSON schema rejection.'
    Assert-Case ($observed.FullyQualifiedErrorId -ceq 'VerificationJsonSchemaRejected') `
        'Schema rejection did not retain the verifier schema-rejection identity.'
    Assert-Case ($observed.CategoryInfo.Category -eq [Management.Automation.ErrorCategory]::InvalidData) `
        'Schema rejection did not retain the InvalidData category.'
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
    Assert-CaseSchemaRejected {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    }

    $diagnostic = New-ProcessNode 'bad.diagnostic' 'DIAGNOSTIC_LEAF' 'SEMANTIC'
    $registry = New-Registry
    $registry.nodes = @($diagnostic)
    Assert-CaseSchemaRejected {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    }
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
    Assert-CaseSchemaRejected {
        Assert-VerificationRegistry $registry -SchemaPath $schemaPath
    }

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

Invoke-Case 'CU7 general point interaction includes new tests and historical semantic gates' {
    $root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
    $registry = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-registry.json')
    [void](Assert-VerificationRegistry $registry -SchemaPath $schemaPath)
    $plan = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION
    $sharedSemantic = @($plan.nodes | Where-Object {
        $_.check_id -ceq 'junit.shared.pre-g9b-cu7-general-point-interaction.semantic'
    })[0]
    $desktopSemantic = @($plan.nodes | Where-Object {
        $_.check_id -ceq 'junit.desktop.pre-g9b-cu7-general-point-interaction.semantic'
    })[0]
    Assert-Case ($sharedSemantic.semantic_domain -ceq 'MIXED') (
        'CU7 shared geometric/state evidence must identify its mixed scientific/product domain.')
    Assert-Case ($desktopSemantic.semantic_domain -ceq 'PRODUCT') (
        'CU7 Desktop evidence must retain its product domain.')
    foreach ($required in @(
        'compile.shared.semantic',
        'compile.desktop.semantic',
        'workspace-profile.product',
        'junit.shared.pre-g9b-cu7-i1-r2-kernel.semantic',
        'junit.shared.pre-g9b-cu7-i1-r2-kernel-h1.semantic',
        'junit.desktop.pre-g9b-cu7-i1-r1.semantic',
        'junit.desktop.pre-g9b-cu7-i1-r2-desktop.semantic',
        'junit.desktop.g9u1-isolated.semantic',
        'junit.shared.pre-g9b-cu7-general-point-interaction.semantic',
        'junit.desktop.pre-g9b-cu7-general-point-interaction.semantic')) {
        Assert-Case (@($plan.nodes.check_id) -ccontains $required) (
            "CU7 general interaction omitted its required gate: $required")
    }
    $inventory = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-junit-inventory.json')
    $expected = @{
        shared = @(
            'org.geocedg.common.locus.IndividualForwardPointCreationTest',
            'org.geocedg.common.locus.NativeConicPointPersistenceTest',
            'org.geocedg.common.kernel.locus.intersection.LocalContinuationRoundingTest',
            'org.geocedg.common.export.G9X1LocusV2ExportTest',
            'org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityIntegrationTest',
            'org.geocedg.common.kernel.locus.constructive.StructuralPointTest',
            'org.geocedg.common.kernel.locus.intersection.PreG9BCu2ClassV3ModelTest',
            'org.geocedg.common.locus.GeneralPointContinuationTest',
            'org.geocedg.common.locus.M08PointCreationKernelTest',
            'org.geocedg.common.locus.G9S1SemanticSpline2DTest',
            'org.geocedg.common.locus.G9S1R1StructuralSplineContinuityTest',
            'org.geocedg.common.locus.G9S1R1StructuralSplineLifecycleTest',
            'org.geocedg.common.locus.PostG9U1A1SplineConstructorProvenanceTest')
        desktop = @(
            'org.geocedg.desktop.GeneralPointInteractionDesktopTest',
            'org.geocedg.desktop.Cu7M08SGeneralCreationTest',
            'org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest',
            'org.geocedg.desktop.G9U0R6NativeArchivePersistenceTest',
            'org.geocedg.desktop.Cu7M08SCorrectionTest',
            'org.geocedg.desktop.Cu7M08SDesktopEventsTest',
            'org.geocedg.desktop.Cu7M08SReferenceToolTest',
            'org.geocedg.desktop.PostE2P1R2SmokeFollowUpTest',
            'org.geocedg.desktop.PreG9BR6PlusD1DocumentUnitsTest',
            'org.geocedg.desktop.PreG9BR6PlusD1LegacyByteIdentityTest',
            'org.geocedg.desktop.PreG9BR6PlusE1LBundledCatalogTest',
            'org.geocedg.desktop.PreG9BR6PlusE1LCuratedLibraryTest')
    }
    foreach ($module in @('shared', 'desktop')) {
        $id = 'pre-g9b-cu7-general-point-interaction.' + $module
        $selection = @($inventory.selections | Where-Object selection_id -CEQ $id)
        Assert-Case ($selection.Count -eq 1) "CU7 selection is not unique: $id"
        foreach ($filter in $expected[$module]) {
            Assert-Case (@($selection[0].test_filters) -ccontains $filter) (
                "CU7 effective test selection omitted: $filter")
        }
        Assert-Case (@($selection[0].excluded_test_filters).Count -eq 0) (
            "CU7 focused selection unexpectedly excludes tests: $id")
    }
}

Invoke-Case 'CU7 R3 preserves historical gates and selects every corrective regression' {
    $root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
    $registry = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-registry.json')
    [void](Assert-VerificationRegistry $registry -SchemaPath $schemaPath)
    $historical = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION
    $plan = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R3
    Assert-Case ($plan.coverage_state -ceq 'COMPLETE') 'CU7 R3 has incomplete registry coverage.'
    Assert-Case (@($plan.nodes).Count -eq 19) 'CU7 R3 changed its historical 19-node closure.'
    $expectedIds = @($historical.nodes.check_id | ForEach-Object {
        $_.Replace('pre-g9b-cu7-general-point-interaction.',
            'pre-g9b-cu7-general-point-interaction-r3.')
    } | Sort-Object -CaseSensitive)
    $actualIds = @($plan.nodes.check_id | Sort-Object -CaseSensitive)
    Assert-Case (($actualIds -join "`n") -ceq ($expectedIds -join "`n")) (
        'CU7 R3 must preserve every historical gate and replace only its focused selections.')
    $inventory = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-junit-inventory.json')
    $addedFilters = @{
        shared = @(
            'org.geocedg.common.locus.R3NativeStateRecoveryTest',
            'org.geocedg.common.locus.R3NativeStateGrammarTest',
            'org.geocedg.common.locus.R3PointContinuationTest',
            'org.geocedg.common.locus.R3NativeFamilyCompatibilityTest',
            'org.geocedg.common.locus.R3PointInteractionPerformanceTest')
        desktop = @(
            'org.geocedg.desktop.R3PointInteractionDesktopTest',
            'org.geocedg.desktop.R3NativeUndoRecoveryDesktopTest',
            'org.geocedg.desktop.R3NativeUndoLifecycleDesktopTest',
            'org.geocedg.desktop.PreG9BR6PlusF2AuthoringMemoryTest',
            'org.geocedg.desktop.PostE2P3DocumentToolsTest')
    }
    foreach ($module in @('shared', 'desktop')) {
        $oldId = 'pre-g9b-cu7-general-point-interaction.' + $module
        $id = 'pre-g9b-cu7-general-point-interaction-r3.' + $module
        $historicalSelection = @($inventory.selections | Where-Object selection_id -CEQ $oldId)
        $selection = @($inventory.selections | Where-Object selection_id -CEQ $id)
        Assert-Case ($historicalSelection.Count -eq 1 -and $selection.Count -eq 1) (
            "CU7 R3 or historical selection is not unique: $id")
        $expectedFilters = @($historicalSelection[0].test_filters) + $addedFilters[$module]
        Assert-Case ((@($selection[0].test_filters) -join "`n") -ceq ($expectedFilters -join "`n")) (
            "CU7 R3 effective filters differ from the complete corrective selection: $id")
        Assert-Case (@($selection[0].excluded_test_filters).Count -eq 0) (
            "CU7 R3 unexpectedly excludes tests: $id")
        $expectedArguments = @($selection[0].gradle_task)
        foreach ($filter in $expectedFilters) { $expectedArguments += @('--tests', $filter) }
        $expectedArguments += @('--rerun-tasks', '--no-build-cache', '--no-daemon',
            '--no-problems-report', '--console=plain',
            '-Dorg.gradle.java.installations.auto-download=false')
        Assert-Case ((@($selection[0].effective_arguments) -join "`n") -ceq
            ($expectedArguments -join "`n")) "CU7 R3 execution arguments differ: $id"
        $producerId = 'junit.' + $module + '.pre-g9b-cu7-general-point-interaction-r3.producer'
        $producer = @($plan.nodes | Where-Object check_id -CEQ $producerId)
        $semanticId = 'junit.' + $module + '.pre-g9b-cu7-general-point-interaction-r3.semantic'
        $semantic = @($plan.nodes | Where-Object check_id -CEQ $semanticId)
        Assert-Case ($producer.Count -eq 1 -and $semantic.Count -eq 1) (
            "CU7 R3 producer/projection is not unique: $id")
        Assert-Case ($producer[0].node_kind -ceq 'PROCESS_PRODUCER' -and
            $producer[0].output_adapter -ceq 'RAW_PROCESS_V1') (
            "CU7 R3 producer must remain a raw evidence process: $id")
        $selectionArgument = [Array]::IndexOf([string[]]@($producer[0].argv), '-SelectionId')
        Assert-Case ($selectionArgument -ge 0 -and
            $producer[0].argv[$selectionArgument + 1] -ceq $id) (
            "CU7 R3 producer does not execute the registered corrective selection: $id")
        Assert-Case ((@($producer[0].resource_locks) -join ',') -ceq 'gradle-build-tree') (
            "CU7 R3 lost the serialized Gradle build resource: $id")
        Assert-Case ($semantic[0].node_kind -ceq 'EVIDENCE_PROJECTION' -and
            $semantic[0].contract_class -ceq 'SEMANTIC' -and
            $semantic[0].output_adapter -ceq 'PROJECTION_JUNIT_SELECTION_V1' -and
            $semantic[0].producer_dependency -ceq $producerId -and
            (@($semantic[0].dependencies) -join ',') -ceq $producerId) (
            "CU7 R3 semantic verdict is not projected from its own producer: $id")
        $expectedDomain = if ($module -ceq 'shared') { 'MIXED' } else { 'PRODUCT' }
        Assert-Case ($semantic[0].semantic_domain -ceq $expectedDomain) (
            "CU7 R3 semantic domain changed: $id")
        Assert-Case (@($producer[0].required_for_profiles).Count -eq 0 -and
            @($semantic[0].required_for_profiles).Count -eq 0) (
            "CU7 R3 focal checks must not change historical profile unions: $id")
    }
}

Invoke-Case 'CU7 R4 preserves the R3 gates and includes all four P2 regressions' {
    $root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
    $registry = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-registry.json')
    [void](Assert-VerificationRegistry $registry -SchemaPath $schemaPath)
    $historical = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R3
    $plan = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R4
    Assert-Case ($plan.coverage_state -ceq 'COMPLETE') 'CU7 R4 has incomplete registry coverage.'
    Assert-Case (@($plan.nodes).Count -eq 19) 'CU7 R4 changed the R3 19-node gate closure.'
    $expectedIds = @($historical.nodes.check_id | ForEach-Object {
        $_.Replace('pre-g9b-cu7-general-point-interaction-r3.',
            'pre-g9b-cu7-general-point-interaction-r4.')
    } | Sort-Object -CaseSensitive)
    $actualIds = @($plan.nodes.check_id | Sort-Object -CaseSensitive)
    Assert-Case (($actualIds -join "`n") -ceq ($expectedIds -join "`n")) (
        'CU7 R4 must preserve every R3 gate and replace only its focused selections.')
    $inventory = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-junit-inventory.json')
    $addedFilters = @{
        shared = @(
            'org.geocedg.common.locus.R4PointContinuationChainTest',
            'org.geocedg.common.locus.R4SimilarityContinuationTest',
            'org.geocedg.common.locus.R4NativeFidelityBudgetTest',
            'org.geocedg.common.locus.R4ContinuationPerformanceTest')
        desktop = @(
            'org.geocedg.desktop.R4ViewActivationDesktopTest',
            'org.geocedg.desktop.R4ContinuationGestureDesktopTest',
            'org.geocedg.desktop.R4NativeBudgetDesktopTest')
    }
    foreach ($module in @('shared', 'desktop')) {
        $oldId = 'pre-g9b-cu7-general-point-interaction-r3.' + $module
        $id = 'pre-g9b-cu7-general-point-interaction-r4.' + $module
        $historicalSelection = @($inventory.selections | Where-Object selection_id -CEQ $oldId)
        $selection = @($inventory.selections | Where-Object selection_id -CEQ $id)
        Assert-Case ($historicalSelection.Count -eq 1 -and $selection.Count -eq 1) (
            "CU7 R4 or R3 inventory selection is not unique: $id")
        $expectedFilters = @($historicalSelection[0].test_filters) + $addedFilters[$module]
        Assert-Case ((@($selection[0].test_filters) -join "`n") -ceq ($expectedFilters -join "`n")) (
            "CU7 R4 effective filters differ from the complete corrective selection: $id")
        Assert-Case (@($selection[0].excluded_test_filters).Count -eq 0) (
            "CU7 R4 unexpectedly excludes tests: $id")
        $expectedArguments = @($selection[0].gradle_task)
        foreach ($filter in $expectedFilters) { $expectedArguments += @('--tests', $filter) }
        $expectedArguments += @('--rerun-tasks', '--no-build-cache', '--no-daemon',
            '--no-problems-report', '--console=plain',
            '-Dorg.gradle.java.installations.auto-download=false')
        Assert-Case ((@($selection[0].effective_arguments) -join "`n") -ceq
            ($expectedArguments -join "`n")) "CU7 R4 execution arguments differ: $id"
        $producerId = 'junit.' + $module + '.pre-g9b-cu7-general-point-interaction-r4.producer'
        $semanticId = 'junit.' + $module + '.pre-g9b-cu7-general-point-interaction-r4.semantic'
        $producer = @($plan.nodes | Where-Object check_id -CEQ $producerId)
        $semantic = @($plan.nodes | Where-Object check_id -CEQ $semanticId)
        Assert-Case ($producer.Count -eq 1 -and $semantic.Count -eq 1) (
            "CU7 R4 producer/projection is not unique: $id")
        Assert-Case ($producer[0].node_kind -ceq 'PROCESS_PRODUCER' -and
            $producer[0].output_adapter -ceq 'RAW_PROCESS_V1') (
            "CU7 R4 producer must remain a raw evidence process: $id")
        $selectionArgument = [Array]::IndexOf([string[]]@($producer[0].argv), '-SelectionId')
        Assert-Case ($selectionArgument -ge 0 -and
            $producer[0].argv[$selectionArgument + 1] -ceq $id) (
            "CU7 R4 producer does not execute its registered selection: $id")
        Assert-Case ((@($producer[0].resource_locks) -join ',') -ceq 'gradle-build-tree') (
            "CU7 R4 lost the serialized Gradle build resource: $id")
        Assert-Case ($semantic[0].node_kind -ceq 'EVIDENCE_PROJECTION' -and
            $semantic[0].contract_class -ceq 'SEMANTIC' -and
            $semantic[0].output_adapter -ceq 'PROJECTION_JUNIT_SELECTION_V1' -and
            $semantic[0].producer_dependency -ceq $producerId -and
            (@($semantic[0].dependencies) -join ',') -ceq $producerId) (
            "CU7 R4 verdict is not projected from its own producer: $id")
        $expectedDomain = if ($module -ceq 'shared') { 'MIXED' } else { 'PRODUCT' }
        Assert-Case ($semantic[0].semantic_domain -ceq $expectedDomain) (
            "CU7 R4 semantic domain changed: $id")
        Assert-Case (@($producer[0].required_for_profiles).Count -eq 0 -and
            @($semantic[0].required_for_profiles).Count -eq 0) (
            "CU7 R4 focal checks must not change historical profile unions: $id")
    }
}

Invoke-Case 'CU7 R5 preserves R4 gates and includes actual pointer and creation entry routes' {
    $root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
    $registry = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-registry.json')
    [void](Assert-VerificationRegistry $registry -SchemaPath $schemaPath)
    $historical = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-GENERAL-POINT-INTERACTION-R4
    $plan = Resolve-VerificationRegistryPlan $registry PHASE PRE-G9B-CU7-R5
    Assert-Case ($plan.coverage_state -ceq 'COMPLETE') 'CU7 R5 registry coverage is incomplete.'
    Assert-Case (@($plan.nodes).Count -eq 19) 'CU7 R5 changed the R4 19-node gate closure.'
    $expectedIds = @($historical.nodes.check_id | ForEach-Object {
        $_.Replace('pre-g9b-cu7-general-point-interaction-r4.', 'pre-g9b-cu7-r5.')
    } | Sort-Object -CaseSensitive)
    Assert-Case ((@($plan.nodes.check_id | Sort-Object -CaseSensitive) -join "`n") -ceq
        ($expectedIds -join "`n")) 'CU7 R5 must preserve every R4 gate.'
    $inventory = Read-VerificationJson -Path (
        Join-Path $root 'geocedg/specs/operations/verification-junit-inventory.json')
    $added = @{
        shared = @('org.geocedg.common.locus.R5ForwardCreationRoutingTest')
        desktop = @('org.geocedg.desktop.R5SemanticPointPointerDesktopTest',
            'org.geocedg.desktop.R5ReferenceCreationDesktopTest',
            'org.geocedg.desktop.PostE2P4GraphicsContextMenuTest')
    }
    foreach ($module in @('shared', 'desktop')) {
        $old = @($inventory.selections | Where-Object selection_id -CEQ (
            'pre-g9b-cu7-general-point-interaction-r4.' + $module))
        $id = 'pre-g9b-cu7-r5.' + $module
        $selection = @($inventory.selections | Where-Object selection_id -CEQ $id)
        Assert-Case ($old.Count -eq 1 -and $selection.Count -eq 1) (
            "CU7 R5/R4 inventory selection is not unique: $id")
        $filters = @($old[0].test_filters) + $added[$module]
        Assert-Case ((@($selection[0].test_filters) -join "`n") -ceq ($filters -join "`n")) (
            "CU7 R5 omits a new or historical filter: $id")
        Assert-Case (@($selection[0].excluded_test_filters).Count -eq 0) (
            "CU7 R5 unexpectedly excludes tests: $id")
        $arguments = @($selection[0].gradle_task)
        foreach ($filter in $filters) { $arguments += @('--tests', $filter) }
        $arguments += @('--rerun-tasks', '--no-build-cache', '--no-daemon',
            '--no-problems-report', '--console=plain',
            '-Dorg.gradle.java.installations.auto-download=false')
        Assert-Case ((@($selection[0].effective_arguments) -join "`n") -ceq
            ($arguments -join "`n")) "CU7 R5 execution arguments differ: $id"
        $producerId = 'junit.' + $module + '.pre-g9b-cu7-r5.producer'
        $semanticId = 'junit.' + $module + '.pre-g9b-cu7-r5.semantic'
        $producer = @($plan.nodes | Where-Object check_id -CEQ $producerId)
        $semantic = @($plan.nodes | Where-Object check_id -CEQ $semanticId)
        Assert-Case ($producer.Count -eq 1 -and $semantic.Count -eq 1) (
            "CU7 R5 producer/projection is not unique: $id")
        $argumentIndex = [Array]::IndexOf([string[]]@($producer[0].argv), '-SelectionId')
        Assert-Case ($producer[0].node_kind -ceq 'PROCESS_PRODUCER' -and
            $producer[0].output_adapter -ceq 'RAW_PROCESS_V1' -and
            $argumentIndex -ge 0 -and $producer[0].argv[$argumentIndex + 1] -ceq $id) (
            "CU7 R5 producer does not execute its exact registered selection: $id")
        Assert-Case ((@($producer[0].resource_locks) -join ',') -ceq 'gradle-build-tree') (
            "CU7 R5 lost serialized Gradle build ownership: $id")
        $domain = if ($module -ceq 'shared') { 'MIXED' } else { 'PRODUCT' }
        Assert-Case ($semantic[0].node_kind -ceq 'EVIDENCE_PROJECTION' -and
            $semantic[0].contract_class -ceq 'SEMANTIC' -and
            $semantic[0].semantic_domain -ceq $domain -and
            $semantic[0].output_adapter -ceq 'PROJECTION_JUNIT_SELECTION_V1' -and
            $semantic[0].producer_dependency -ceq $producerId -and
            (@($semantic[0].dependencies) -join ',') -ceq $producerId) (
            "CU7 R5 verdict is not projected from its own producer: $id")
        Assert-Case (@($producer[0].required_for_profiles).Count -eq 0 -and
            @($semantic[0].required_for_profiles).Count -eq 0) (
            "CU7 R5 must not change the historical global profile unions: $id")
    }
}

Write-Host "verification-registry.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-verification-supervisor-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-supervisor', [Text.UTF8Encoding]::new($false))
$fixture = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'fixtures/verification-child-fixture.ps1'))
$pwshPath = (Get-Process -Id $PID).Path
$registrySchema = Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-registry.schema.json'
$resultSchema = Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-result.schema.json'
$identity = [pscustomobject]@{
    base_commit = ('1' * 40)
    base_tree = ('2' * 40)
    candidate_commit = ('3' * 40)
    candidate_tree = ('4' * 40)
    environment_fingerprint = ('5' * 64)
}

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
function New-Leaf {
    param(
        [string]$Id,
        [string]$Kind,
        [string]$Contract,
        [string]$Mode,
        [string]$Outcome,
        [int]$ExitCode,
        [string]$Adapter = 'STRUCTURED_CONTRACT_V1',
        [string[]]$Dependencies = @()
    )
    $command = [ordered]@{
        kind = 'PROCESS'
        identity = $Id
        executable = $pwshPath
        structured_output = 'child-result.json'
    }
    $arguments = @(
        '-NoProfile', '-File', $fixture, '-Mode', $Mode,
        '-ResultPath', '{structured_output}', '-ContractClass', $Contract,
        '-Outcome', $Outcome, '-ExitCode', [string]$ExitCode
    )
    if ($Contract -ceq 'SEMANTIC') { $arguments += @('-SemanticDomain', 'SCIENTIFIC') }
    if ($Mode -cin @('Throw', 'Absent', 'InvalidUtf8')) {
        $command.Remove('structured_output')
        $arguments = @('-NoProfile', '-File', $fixture, '-Mode', $Mode, '-ExitCode', [string]$ExitCode)
    }
    $node = [ordered]@{
        check_id = $Id
        node_kind = $Kind
        contract_class = $Contract
        tier = 'INFRA_UNIT'
        command = $command
        argv = $arguments
        working_directory = '.'
        dependencies = $Dependencies
        platforms = @('WINDOWS')
        impact_paths = @()
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = @()
        side_effects = @('TASK_OWNED_OUTPUT')
        output_adapter = $Adapter
        required_for_profiles = @('FINAL')
    }
    if ($Contract -ceq 'SEMANTIC') { $node.semantic_domain = 'SCIENTIFIC' }
    return [pscustomobject]$node
}
function New-Producer {
    param([string]$Id, [int]$ExitCode, [string[]]$Dependencies = @())
    $node = [ordered]@{
        check_id = $Id
        node_kind = 'PROCESS_PRODUCER'
        tier = 'INFRA_UNIT'
        command = [ordered]@{ kind = 'PROCESS'; identity = $Id; executable = $pwshPath }
        argv = @('-NoProfile', '-File', $fixture, '-Mode', 'Absent', '-ExitCode', [string]$ExitCode)
        working_directory = '.'
        dependencies = $Dependencies
        platforms = @('WINDOWS')
        impact_paths = @()
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = @()
        side_effects = @('TASK_OWNED_OUTPUT')
        output_adapter = 'RAW_PROCESS_V1'
        required_for_profiles = @('FINAL')
        produces_evidence = @('PROCESS_RESULT')
    }
    return [pscustomobject]$node
}
function New-Projection {
    param([string]$Id, [string]$Producer, [string]$Contract, [string]$EvidenceName)
    $command = [ordered]@{
        kind = 'BUILTIN_PROJECTION'
        identity = $Id
        projection = 'EXIT_CODE_ZERO'
    }
    if (-not [string]::IsNullOrWhiteSpace($EvidenceName)) {
        $command.evidence_name = $EvidenceName
    }
    $node = [ordered]@{
        check_id = $Id
        node_kind = 'EVIDENCE_PROJECTION'
        contract_class = $Contract
        tier = 'INFRA_UNIT'
        command = $command
        argv = @()
        working_directory = '.'
        dependencies = @($Producer)
        platforms = @('WINDOWS')
        impact_paths = @()
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = @()
        side_effects = @('NONE')
        output_adapter = 'PROJECTION_EXIT_CODE_V1'
        required_for_profiles = @('FINAL')
        producer_dependency = $Producer
    }
    if ($Contract -ceq 'SEMANTIC') { $node.semantic_domain = 'SCIENTIFIC' }
    return [pscustomobject]$node
}
function New-Registry {
    param([object[]]$Nodes)
    return [pscustomobject][ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-registry.schema.json'
        schema_version = 2
        registry_id = 'supervisor.fixture'
        catalogs = [object[]]@((ConvertFrom-Json ([IO.File]::ReadAllText((Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-registry.json'))) -Depth 100).catalogs)
        profiles = @([ordered]@{
            profile_id = 'FINAL'; coverage_state = 'COMPLETE'; missing_check_ids = @()
        })
        compatibility_mappings = @()
        phase_selections = @()
        nodes = $Nodes
    }
}
function Invoke-FixtureSupervisor {
    param([string]$Name, [object[]]$Nodes)
    return Invoke-VerificationSupervisor -Registry (New-Registry $Nodes) -Profile FINAL -RepositoryRoot $root -OutputDirectory (Join-Path $root $Name) -Identity $identity -RegistrySchemaPath $registrySchema -ResultSchemaPath $resultSchema
}

try {
    Invoke-Case 'diagnostic findings never alter accepted verdict or exit' {
        $semantic = New-Leaf semantic.ok ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $first = New-Leaf style.finding DIAGNOSTIC_LEAF STYLE_DIAGNOSTIC Structured DIAGNOSTIC_FINDING 7
        $second = New-Leaf governance.finding DIAGNOSTIC_LEAF GOVERNANCE_DIAGNOSTIC Structured DIAGNOSTIC_FINDING 8
        $run = Invoke-FixtureSupervisor diagnostics @($semantic, $first, $second)
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED') 'Diagnostic findings changed acceptance.'
        Assert-Case ($run.report.coverage_verdict -ceq 'COMPLETE') 'Diagnostics changed coverage.'
        Assert-Case ($run.exit_code -eq 0) 'Diagnostic findings changed process exit.'
        Assert-Case ($run.report.diagnostic_summary.findings -eq 2) 'Not all diagnostic findings were reported.'
        $evidenceNames = [string[]]$run.report.acceptance_results[0].evidence.name
        Assert-Case ($evidenceNames -ccontains 'STDOUT' -and $evidenceNames -ccontains 'STDERR' -and $evidenceNames -ccontains 'STRUCTURED_RESULT') 'Direct child evidence was not retained in the report.'
    }
    Invoke-Case 'equivalent executions have stable command and result identities' {
        $semantic = New-Leaf deterministic.ok ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $first = Invoke-FixtureSupervisor deterministic-first @($semantic)
        $second = Invoke-FixtureSupervisor deterministic-second @($semantic)
        Assert-Case ($first.report.acceptance_results[0].command_identity -ceq $second.report.acceptance_results[0].command_identity) 'Task-owned output paths changed command identity.'
        Assert-Case ($first.report.result_hash -ceq $second.report.result_hash) 'Task-owned output paths or timing changed result identity.'
    }
    Invoke-Case 'verdict-neutral producer is interpreted only by pure projections' {
        $producer = New-Producer gradle.producer 11
        $science = New-Projection science.projection gradle.producer SEMANTIC
        $product = New-Projection product.projection gradle.producer SEMANTIC
        $safety = New-Projection safety.projection gradle.producer SAFETY
        $style = New-Projection style.projection gradle.producer STYLE_DIAGNOSTIC
        $run = Invoke-FixtureSupervisor projections @($producer, $science, $product, $safety, $style)
        Assert-Case ($run.report.process_producers.Count -eq 1) 'Producer evidence count changed.'
        Assert-Case ($run.report.process_producers[0].exit_code -eq 11) 'Producer exit was not preserved.'
        Assert-Case ($run.report.process_producers[0].PSObject.Properties.Name -cnotcontains 'contract_class') 'Producer acquired a contract class.'
        Assert-Case (@($run.report.acceptance_results | Where-Object { $_.status -ceq 'CONTRACT_VIOLATED' }).Count -eq 3) 'Pure acceptance projections were not all evaluated.'
        Assert-Case ($run.report.diagnostic_summary.findings -eq 1) 'Pure diagnostic projection was not evaluated.'
        Assert-Case ($run.report.diagnostic_findings[0].PSObject.Properties.Name -cnotcontains
            'semantic_domain') 'Diagnostic projection acquired a semantic domain.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'REJECTED_SAFETY') 'Safety projection did not determine rejection.'
        Assert-Case ($run.exit_code -eq 2) 'Safety rejection exit is incorrect.'
        $repeat = Invoke-FixtureSupervisor projections-repeat @($producer, $science, $product, $safety, $style)
        Assert-Case ($run.report.result_hash -ceq $repeat.report.result_hash) 'Producer output locations or timing changed result identity.'
    }
    Invoke-Case 'structured producer projection preserves semantic payload identity' {
        $producer = New-Producer structured.producer 0
        $producer.command.structured_output = 'child-result.json'
        $producer.produces_evidence = @('PROCESS_RESULT', 'STRUCTURED_RESULT')
        $producer.argv = @(
            '-NoProfile', '-File', $fixture, '-Mode', 'Structured',
            '-ResultPath', '{structured_output}', '-ContractClass', 'SEMANTIC',
            '-SemanticDomain', 'SCIENTIFIC',
            '-Outcome', 'CONTRACT_SATISFIED', '-SemanticPath', 'models/reference-a', '-ExitCode', '0'
        )
        $projection = New-Projection structured.science structured.producer SEMANTIC
        $projection.command.projection = 'STRUCTURED_CONTRACT'
        $projection.command.evidence_name = 'STRUCTURED_RESULT'
        $projection.output_adapter = 'PROJECTION_STRUCTURED_CONTRACT_V1'
        $run = Invoke-FixtureSupervisor structured-projection @($producer, $projection)
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED') 'Valid structured projection was not accepted.'
        Assert-Case ($run.report.process_producers[0].structured_output.value.path -ceq 'models/reference-a') 'Structured semantic path was not preserved.'
        $originalHash = $run.report.result_hash
        $run.report.process_producers[0].structured_output.value.path = 'models/reference-b'
        Assert-Case ($originalHash -cne (Get-VerificationResultHash $run.report)) 'Semantic path content was omitted from result identity.'
        $run.report.process_producers[0].structured_output.value = @()
        $emptyArrayHash = Get-VerificationResultHash $run.report
        $run.report.process_producers[0].structured_output.value = $null
        Assert-Case ($emptyArrayHash -cne (Get-VerificationResultHash $run.report)) 'Empty-array semantic evidence collapsed into null.'
    }
    Invoke-Case 'malformed acceptance evidence makes required coverage untrusted' {
        $leaf = New-Leaf malformed.acceptance ACCEPTANCE_LEAF SEMANTIC Malformed CONTRACT_SATISFIED 0
        $run = Invoke-FixtureSupervisor malformed-acceptance @($leaf)
        Assert-Case ($run.report.acceptance_results[0].status -ceq 'EVIDENCE_UNTRUSTED') 'Malformed acceptance evidence was misclassified.'
        Assert-Case ($run.report.coverage_verdict -ceq 'UNTRUSTED') 'Malformed acceptance evidence did not affect coverage.'
        Assert-Case ($run.exit_code -eq 3) 'Untrusted coverage exit is incorrect.'
    }
    Invoke-Case 'malformed diagnostic evidence is unavailable and nonblocking' {
        $semantic = New-Leaf product.ok ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $diagnostic = New-Leaf malformed.diagnostic DIAGNOSTIC_LEAF GOVERNANCE_DIAGNOSTIC Malformed DIAGNOSTIC_CLEAR 0
        $run = Invoke-FixtureSupervisor malformed-diagnostic @($semantic, $diagnostic)
        Assert-Case ($run.report.diagnostic_findings[0].outcome -ceq 'DIAGNOSTIC_UNAVAILABLE') 'Malformed diagnostic was misclassified.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED') 'Unavailable diagnostic blocked acceptance.'
        Assert-Case ($run.exit_code -eq 0) 'Unavailable diagnostic changed exit.'
    }
    Invoke-Case 'missing producer files make projections unavailable, not violated' {
        $producer = New-Producer evidence.producer 0
        $producer.produces_evidence = @('PROCESS_RESULT', 'PAYLOAD')
        $science = New-Projection science.evidence evidence.producer SEMANTIC PAYLOAD
        $science.command.projection = 'EVIDENCE_PRESENT'
        $science.output_adapter = 'PROJECTION_EVIDENCE_PRESENT_V1'
        $diagnostic = New-Projection style.evidence evidence.producer STYLE_DIAGNOSTIC PAYLOAD
        $diagnostic.command.projection = 'EVIDENCE_PRESENT'
        $diagnostic.output_adapter = 'PROJECTION_EVIDENCE_PRESENT_V1'
        $run = Invoke-FixtureSupervisor missing-producer-evidence @($producer, $science, $diagnostic)
        Assert-Case ($run.report.acceptance_results[0].status -ceq 'EVIDENCE_UNTRUSTED') 'Missing scientific evidence was called a contract violation.'
        Assert-Case ($run.report.diagnostic_findings[0].outcome -ceq 'DIAGNOSTIC_UNAVAILABLE') 'Missing diagnostic evidence was misclassified.'
        Assert-Case ($run.report.coverage_verdict -ceq 'UNTRUSTED') 'Missing required evidence did not mark coverage untrusted.'
    }
    Invoke-Case 'producer launch failure makes projected acceptance evidence untrusted' {
        $producer = New-Producer unavailable.producer 0
        $producer.command.executable = Join-Path $root 'missing-verifier.exe'
        $science = New-Projection unavailable.science unavailable.producer SEMANTIC
        $run = Invoke-FixtureSupervisor unavailable-producer @($producer, $science)
        Assert-Case ($run.report.process_producers[0].completion_state -ceq 'LAUNCH_FAILED') 'Producer launch failure was not preserved.'
        Assert-Case (-not [string]::IsNullOrWhiteSpace($run.report.process_producers[0].completion_cause)) 'Producer launch cause was omitted.'
        Assert-Case ($run.report.acceptance_results[0].status -ceq 'EVIDENCE_UNTRUSTED') 'Unavailable producer evidence was not marked untrusted.'
        Assert-Case ($run.report.coverage_verdict -ceq 'UNTRUSTED') 'Unavailable producer did not make coverage untrusted.'
        Assert-Case ($run.exit_code -eq 3) 'Unavailable acceptance evidence returned the wrong exit.'
    }
    Invoke-Case 'unavailable diagnostic dependency skips its producer without blocking acceptance' {
        $semantic = New-Leaf independent.product ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $diagnostic = New-Leaf unavailable.governance DIAGNOSTIC_LEAF GOVERNANCE_DIAGNOSTIC Malformed DIAGNOSTIC_CLEAR 0
        $producer = New-Producer diagnostic.producer 0 @('unavailable.governance')
        $projection = New-Projection projected.governance diagnostic.producer GOVERNANCE_DIAGNOSTIC
        $run = Invoke-FixtureSupervisor skipped-diagnostic-producer @(
            $semantic, $diagnostic, $producer, $projection)
        $producerRecord = @($run.report.process_producers | Where-Object {
            $_.check_id -ceq 'diagnostic.producer'
        })[0]
        $projected = @($run.report.diagnostic_findings | Where-Object {
            $_.check_id -ceq 'projected.governance'
        })[0]
        Assert-Case ($producerRecord.completion_state -ceq 'NOT_RUN_DEPENDENCY') 'Skipped producer was not recorded neutrally.'
        Assert-Case ($producerRecord.PSObject.Properties.Name -cnotcontains 'contract_class') 'Skipped producer acquired a contract class.'
        Assert-Case ($projected.outcome -ceq 'DIAGNOSTIC_UNAVAILABLE') 'Dependent diagnostic projection was not unavailable.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED') 'Unavailable diagnostic path blocked acceptance.'
        Assert-Case ($run.report.coverage_verdict -ceq 'COMPLETE') 'Unavailable diagnostic path changed acceptance coverage.'
        Assert-Case ($run.exit_code -eq 0) 'Unavailable diagnostic path changed process exit.'
    }
    Invoke-Case 'failed acceptance dependency propagates through a neutral producer as not run' {
        $prerequisite = New-Leaf producer.prerequisite ACCEPTANCE_LEAF VERIFICATION_CORE Structured CONTRACT_VIOLATED 1
        $producer = New-Producer skipped.producer 0 @('producer.prerequisite')
        $projection = New-Projection skipped.science skipped.producer SEMANTIC
        $independent = New-Leaf still.independent ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $run = Invoke-FixtureSupervisor skipped-acceptance-producer @(
            $prerequisite, $producer, $projection, $independent)
        $projected = @($run.report.acceptance_results | Where-Object {
            $_.check_id -ceq 'skipped.science'
        })[0]
        Assert-Case ($run.report.process_producers[0].completion_state -ceq 'NOT_RUN_DEPENDENCY') 'Dependent producer was not explicitly skipped.'
        Assert-Case ($projected.status -ceq 'NOT_RUN_DEPENDENCY') 'Projected acceptance dependency was not marked not run.'
        Assert-Case (@($run.report.acceptance_results | Where-Object {
            $_.check_id -ceq 'still.independent' -and $_.status -ceq 'CONTRACT_SATISFIED'
        }).Count -eq 1) 'Independent acceptance check was not preserved.'
        Assert-Case ($run.report.coverage_verdict -ceq 'INCOMPLETE') 'Skipped projected acceptance did not mark coverage incomplete.'
    }
    Invoke-Case 'all independent violations are aggregated' {
        $product = New-Leaf product.bad ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_VIOLATED 1
        $science = New-Leaf science.bad ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_VIOLATED 1
        $diagnostic = New-Leaf docs.finding DIAGNOSTIC_LEAF DOCUMENTATION_DIAGNOSTIC Structured DIAGNOSTIC_FINDING 1
        $run = Invoke-FixtureSupervisor aggregate @($product, $science, $diagnostic)
        Assert-Case ($run.report.acceptance_results.Count -eq 2) 'Independent acceptance results were lost.'
        Assert-Case ($run.report.diagnostic_findings.Count -eq 1) 'Independent diagnostic result was lost.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'REJECTED_SEMANTIC') 'Semantic violations did not reject.'
        Assert-Case ($run.exit_code -eq 1) 'Semantic rejection exit is incorrect.'
    }
    Invoke-Case 'failed dependency is reported without hiding independent checks' {
        $prerequisite = New-Leaf prerequisite.bad ACCEPTANCE_LEAF VERIFICATION_CORE Structured CONTRACT_VIOLATED 1
        $dependent = New-Leaf dependent.notrun ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0 STRUCTURED_CONTRACT_V1 @('prerequisite.bad')
        $independent = New-Leaf independent.ok ACCEPTANCE_LEAF SEMANTIC Structured CONTRACT_SATISFIED 0
        $run = Invoke-FixtureSupervisor dependency @($prerequisite, $dependent, $independent)
        $notRun = @($run.report.acceptance_results | Where-Object { $_.check_id -ceq 'dependent.notrun' })[0]
        Assert-Case ($notRun.status -ceq 'NOT_RUN_DEPENDENCY') 'Dependent result was not explicit.'
        Assert-Case (@($run.report.acceptance_results | Where-Object { $_.check_id -ceq 'independent.ok' -and $_.status -ceq 'CONTRACT_SATISFIED' }).Count -eq 1) 'Independent check did not run.'
        Assert-Case ($run.report.coverage_verdict -ceq 'INCOMPLETE') 'Dependency gap did not affect coverage.'
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker -PathType Leaf) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-supervisor') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else {
        throw "Fixture cleanup refused unexpected path: $resolved"
    }
}

Write-Host "verification-supervisor.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

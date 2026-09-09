#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force

$script:AcceptanceClasses = @(
    'PRODUCT_SEMANTIC',
    'SCIENTIFIC_SEMANTIC',
    'SAFETY',
    'VERIFICATION_CORE'
)
$script:DiagnosticClasses = @(
    'GOVERNANCE_DIAGNOSTIC',
    'DOCUMENTATION_DIAGNOSTIC',
    'HISTORICAL_CONSISTENCY_DIAGNOSTIC',
    'STYLE_DIAGNOSTIC',
    'PERFORMANCE_TELEMETRY'
)
$script:TierRank = @{
    STATIC = 0
    INFRA_UNIT = 1
    FOCUSED = 2
    INTEGRATION = 3
    FINAL = 4
}

function Test-VerificationObjectProperty {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string]$Name
    )
    if ($Object -is [Collections.IDictionary]) { return $Object.Contains($Name) }
    return $null -ne $Object.PSObject.Properties[$Name]
}

function Get-VerificationObjectProperty {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string]$Name,
        [switch]$Required
    )
    if ($Object -is [Collections.IDictionary]) {
        if ($Object.Contains($Name)) { return $Object[$Name] }
    } else {
        $property = $Object.PSObject.Properties[$Name]
        if ($null -ne $property) { return $property.Value }
    }
    if ($Required) { throw "Missing required verification property '$Name'." }
    return $null
}

function Test-VerificationAcceptanceClass {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$ContractClass)
    return $script:AcceptanceClasses -ccontains $ContractClass
}

function Test-VerificationDiagnosticClass {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$ContractClass)
    return $script:DiagnosticClasses -ccontains $ContractClass
}

function Get-VerificationRegistryNodeMap {
    param([Parameter(Mandatory)] [object[]]$Nodes)

    $map = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    foreach ($node in $Nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        if (-not $map.TryAdd($id, $node)) {
            throw "Duplicate verification check_id: $id"
        }
    }
    return $map
}

function Assert-VerificationRegistry {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Registry,
        [string]$SchemaPath
    )

    if (-not [string]::IsNullOrWhiteSpace($SchemaPath)) {
        [void](Assert-VerificationJsonSchema -Value $Registry -SchemaPath $SchemaPath)
    }
    if ((Get-VerificationObjectProperty $Registry 'schema_version' -Required) -ne 1) {
        throw 'Verification registry schema_version must be 1.'
    }
    $nodes = [object[]]@(Get-VerificationObjectProperty $Registry 'nodes' -Required)
    if ($nodes.Count -eq 0) { throw 'Verification registry must contain at least one node.' }
    $nodeMap = Get-VerificationRegistryNodeMap -Nodes $nodes

    foreach ($node in $nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        if ($id -cnotmatch '^[a-z0-9]+(?:[._-][a-z0-9]+)*$') {
            throw "Invalid verification check_id: $id"
        }
        $kind = [string](Get-VerificationObjectProperty $node 'node_kind' -Required)
        if ($kind -cnotin @('PROCESS_PRODUCER', 'ACCEPTANCE_LEAF', 'DIAGNOSTIC_LEAF', 'EVIDENCE_PROJECTION')) {
            throw ("Invalid verification node_kind for {0}: {1}" -f $id, $kind)
        }
        $tier = [string](Get-VerificationObjectProperty $node 'tier' -Required)
        if (-not $script:TierRank.ContainsKey($tier)) {
            throw ("Invalid verification tier for {0}: {1}" -f $id, $tier)
        }
        $command = Get-VerificationObjectProperty $node 'command' -Required
        $commandKind = [string](Get-VerificationObjectProperty $command 'kind' -Required)
        $adapter = [string](Get-VerificationObjectProperty $node 'output_adapter' -Required)
        $structuredOutput = [string](Get-VerificationObjectProperty $command 'structured_output')
        $hasContract = Test-VerificationObjectProperty $node 'contract_class'
        $contract = Get-VerificationObjectProperty $node 'contract_class'

        if ($hasContract -and $contract -is [Collections.IEnumerable] -and $contract -isnot [string]) {
            throw "Mixed contract classes are prohibited for $id."
        }
        switch ($kind) {
            'PROCESS_PRODUCER' {
                if ($hasContract) { throw "PROCESS_PRODUCER $id must not declare contract_class." }
                if ($commandKind -cne 'PROCESS' -or $adapter -cne 'RAW_PROCESS_V1') {
                    throw "PROCESS_PRODUCER $id requires PROCESS and RAW_PROCESS_V1."
                }
                $produces = [object[]]@(Get-VerificationObjectProperty $node 'produces_evidence' -Required)
                if ($produces.Count -eq 0) { throw "PROCESS_PRODUCER $id must declare produced evidence." }
                $declaresStructured = $produces -ccontains 'STRUCTURED_RESULT'
                if ($declaresStructured -ne (-not [string]::IsNullOrWhiteSpace($structuredOutput))) {
                    throw "PROCESS_PRODUCER $id must declare STRUCTURED_RESULT and structured_output together."
                }
                if (Test-VerificationObjectProperty $node 'producer_dependency') {
                    throw "PROCESS_PRODUCER $id cannot declare producer_dependency."
                }
            }
            'ACCEPTANCE_LEAF' {
                if (-not $hasContract -or -not (Test-VerificationAcceptanceClass ([string]$contract))) {
                    throw "ACCEPTANCE_LEAF $id requires exactly one acceptance contract_class."
                }
                if ($commandKind -cne 'PROCESS' -or $adapter -cnotin @('EXIT_CODE_V1', 'STRUCTURED_CONTRACT_V1')) {
                    throw "ACCEPTANCE_LEAF $id has an incompatible command or output adapter."
                }
                if ($adapter -ceq 'STRUCTURED_CONTRACT_V1' -and
                        [string]::IsNullOrWhiteSpace($structuredOutput)) {
                    throw "ACCEPTANCE_LEAF $id requires structured_output for STRUCTURED_CONTRACT_V1."
                }
                if ((Test-VerificationObjectProperty $node 'produces_evidence') -or
                        (Test-VerificationObjectProperty $node 'producer_dependency')) {
                    throw "ACCEPTANCE_LEAF $id cannot declare producer-only fields."
                }
            }
            'DIAGNOSTIC_LEAF' {
                if (-not $hasContract -or -not (Test-VerificationDiagnosticClass ([string]$contract))) {
                    throw "DIAGNOSTIC_LEAF $id requires exactly one diagnostic contract_class."
                }
                if ($commandKind -cne 'PROCESS' -or $adapter -cnotin @('EXIT_CODE_V1', 'STRUCTURED_CONTRACT_V1')) {
                    throw "DIAGNOSTIC_LEAF $id has an incompatible command or output adapter."
                }
                if ($adapter -ceq 'STRUCTURED_CONTRACT_V1' -and
                        [string]::IsNullOrWhiteSpace($structuredOutput)) {
                    throw "DIAGNOSTIC_LEAF $id requires structured_output for STRUCTURED_CONTRACT_V1."
                }
                if ((Test-VerificationObjectProperty $node 'produces_evidence') -or
                        (Test-VerificationObjectProperty $node 'producer_dependency')) {
                    throw "DIAGNOSTIC_LEAF $id cannot declare producer-only fields."
                }
            }
            'EVIDENCE_PROJECTION' {
                if (-not $hasContract -or
                        (-not (Test-VerificationAcceptanceClass ([string]$contract)) -and
                         -not (Test-VerificationDiagnosticClass ([string]$contract)))) {
                    throw "EVIDENCE_PROJECTION $id requires exactly one contract_class."
                }
                if ($commandKind -cne 'BUILTIN_PROJECTION' -or
                        $adapter -cnotin @(
                            'PROJECTION_EXIT_CODE_V1',
                            'PROJECTION_STRUCTURED_CONTRACT_V1',
                            'PROJECTION_EVIDENCE_PRESENT_V1')) {
                    throw "EVIDENCE_PROJECTION $id has an incompatible command or output adapter."
                }
                $projection = [string](Get-VerificationObjectProperty $command 'projection' -Required)
                $expectedAdapter = @{
                    EXIT_CODE_ZERO = 'PROJECTION_EXIT_CODE_V1'
                    STRUCTURED_CONTRACT = 'PROJECTION_STRUCTURED_CONTRACT_V1'
                    EVIDENCE_PRESENT = 'PROJECTION_EVIDENCE_PRESENT_V1'
                }[$projection]
                if ([string]::IsNullOrEmpty($expectedAdapter) -or $adapter -cne $expectedAdapter) {
                    throw "EVIDENCE_PROJECTION $id does not match its declared projection."
                }
                if (Test-VerificationObjectProperty $node 'produces_evidence') {
                    throw "EVIDENCE_PROJECTION $id cannot declare produced evidence."
                }
            }
        }

        $sideEffects = [object[]]@(Get-VerificationObjectProperty $node 'side_effects' -Required)
        if ($sideEffects.Count -eq 0) { throw "Verification node $id must declare side effects." }
        if ($sideEffects.Count -gt 1 -and $sideEffects -ccontains 'NONE') {
            throw "Verification node $id cannot combine NONE with another side effect."
        }
        $working = [string](Get-VerificationObjectProperty $node 'working_directory' -Required)
        if ($working -cne '.') { [void](ConvertTo-VerificationGitPath $working) }
        foreach ($impactPath in [object[]]@(Get-VerificationObjectProperty $node 'impact_paths' -Required)) {
            if ([string]$impactPath -cne '.') {
                [void](ConvertTo-VerificationGitPath ([string]$impactPath))
            }
        }
        foreach ($dependency in [object[]]@(Get-VerificationObjectProperty $node 'dependencies' -Required)) {
            $dependencyId = [string]$dependency
            if (-not $nodeMap.ContainsKey($dependencyId)) {
                throw "Verification node $id has missing dependency $dependencyId."
            }
            if ($dependencyId -ceq $id) { throw "Verification node $id depends on itself." }
            $dependencyTier = [string](Get-VerificationObjectProperty $nodeMap[$dependencyId] 'tier' -Required)
            if ($script:TierRank[$dependencyTier] -gt $script:TierRank[$tier]) {
                throw "Verification node $id depends on later tier $dependencyId."
            }
        }
    }

    foreach ($node in $nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        $kind = [string](Get-VerificationObjectProperty $node 'node_kind' -Required)
        $contract = [string](Get-VerificationObjectProperty $node 'contract_class')
        if ($kind -ceq 'EVIDENCE_PROJECTION') {
            $producerId = [string](Get-VerificationObjectProperty $node 'producer_dependency' -Required)
            if (-not $nodeMap.ContainsKey($producerId) -or
                    [string](Get-VerificationObjectProperty $nodeMap[$producerId] 'node_kind' -Required) -cne
                        'PROCESS_PRODUCER') {
                throw "EVIDENCE_PROJECTION $id requires an existing PROCESS_PRODUCER dependency."
            }
            if ([object[]]@(Get-VerificationObjectProperty $node 'dependencies' -Required) -cnotcontains
                    $producerId) {
                throw "EVIDENCE_PROJECTION $id must list producer_dependency $producerId in dependencies."
            }
            $projectionCommand = Get-VerificationObjectProperty $node 'command' -Required
            $projection = [string](
                Get-VerificationObjectProperty $projectionCommand 'projection' -Required)
            $producerEvidence = [object[]]@(
                Get-VerificationObjectProperty $nodeMap[$producerId] 'produces_evidence' -Required)
            if ($projection -ceq 'STRUCTURED_CONTRACT' -and
                    $producerEvidence -cnotcontains 'STRUCTURED_RESULT') {
                throw "EVIDENCE_PROJECTION $id requires producer STRUCTURED_RESULT evidence."
            }
            if ($projection -ceq 'EVIDENCE_PRESENT') {
                $evidenceName = [string](
                    Get-VerificationObjectProperty $projectionCommand 'evidence_name' -Required)
                if ($evidenceName -cin @('PROCESS_RESULT', 'STRUCTURED_RESULT') -or
                        $producerEvidence -cnotcontains $evidenceName) {
                    throw "EVIDENCE_PROJECTION $id requires declared file evidence $evidenceName."
                }
            } elseif (Test-VerificationObjectProperty $projectionCommand 'evidence_name') {
                throw "EVIDENCE_PROJECTION $id declares evidence_name for a non-file projection."
            }
        }
        if ($kind -cne 'PROCESS_PRODUCER' -and (Test-VerificationAcceptanceClass $contract)) {
            $pending = [Collections.Generic.Stack[string]]::new()
            foreach ($dependency in [object[]]@(Get-VerificationObjectProperty $node 'dependencies' -Required)) {
                $pending.Push([string]$dependency)
            }
            $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
            while ($pending.Count -gt 0) {
                $dependencyId = $pending.Pop()
                if (-not $seen.Add($dependencyId)) { continue }
                $dependencyNode = $nodeMap[$dependencyId]
                $dependencyKind = [string](Get-VerificationObjectProperty $dependencyNode 'node_kind' -Required)
                $dependencyContract = [string](Get-VerificationObjectProperty $dependencyNode 'contract_class')
                if ($dependencyKind -cne 'PROCESS_PRODUCER' -and
                        (Test-VerificationDiagnosticClass $dependencyContract)) {
                    throw "Acceptance node $id cannot depend on diagnostic node $dependencyId."
                }
                foreach ($ancestor in [object[]]@(
                        Get-VerificationObjectProperty $dependencyNode 'dependencies' -Required)) {
                    $pending.Push([string]$ancestor)
                }
            }
        }
    }

    $indegree = [Collections.Generic.Dictionary[string, int]]::new([StringComparer]::Ordinal)
    $children = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    foreach ($node in $nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        $dependencies = [object[]]@(Get-VerificationObjectProperty $node 'dependencies' -Required)
        $indegree[$id] = $dependencies.Count
        $children[$id] = [Collections.Generic.List[string]]::new()
    }
    foreach ($node in $nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        foreach ($dependency in [object[]]@(Get-VerificationObjectProperty $node 'dependencies' -Required)) {
            $children[[string]$dependency].Add($id)
        }
    }
    $queue = [Collections.Generic.List[string]]::new()
    foreach ($id in $indegree.Keys) {
        if ($indegree[$id] -eq 0) { $queue.Add($id) }
    }
    $visited = 0
    while ($queue.Count -gt 0) {
        $queueValues = [string[]]$queue.ToArray()
        [Array]::Sort($queueValues, [StringComparer]::Ordinal)
        $next = $queueValues[0]
        [void]$queue.Remove($next)
        $visited++
        foreach ($child in $children[$next]) {
            $indegree[$child]--
            if ($indegree[$child] -eq 0) { $queue.Add($child) }
        }
    }
    if ($visited -ne $nodes.Count) { throw 'Verification dependency graph contains a cycle.' }
    return $true
}

function Resolve-VerificationRegistryPlan {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Registry,
        [Parameter(Mandatory)] [string]$Profile
    )

    [void](Assert-VerificationRegistry -Registry $Registry)
    $nodes = [object[]]@(Get-VerificationObjectProperty $Registry 'nodes' -Required)
    $map = Get-VerificationRegistryNodeMap -Nodes $nodes
    $selected = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $pending = [Collections.Generic.Stack[string]]::new()
    foreach ($node in $nodes) {
        if ([object[]]@(Get-VerificationObjectProperty $node 'required_for_profiles' -Required) -ccontains
                $Profile) {
            $pending.Push([string](Get-VerificationObjectProperty $node 'check_id' -Required))
        }
    }
    while ($pending.Count -gt 0) {
        $id = $pending.Pop()
        if (-not $selected.Add($id)) { continue }
        foreach ($dependency in [object[]]@(Get-VerificationObjectProperty $map[$id] 'dependencies' -Required)) {
            $pending.Push([string]$dependency)
        }
    }
    if ($selected.Count -eq 0) {
        throw "Verification profile $Profile selects no nodes."
    }

    $ordered = [Collections.Generic.List[object]]::new()
    $emitted = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    while ($emitted.Count -lt $selected.Count) {
        $ready = [string[]]@($selected | Where-Object {
            if ($emitted.Contains([string]$_)) { return $false }
            $candidate = $map[$_]
            @([object[]]@(Get-VerificationObjectProperty $candidate 'dependencies' -Required) |
                Where-Object {
                    $selected.Contains([string]$_) -and -not $emitted.Contains([string]$_)
                }).Count -eq 0
        })
        if ($ready.Count -eq 0) { throw 'Unable to resolve verification execution plan.' }
        $minimumTier = [int]::MaxValue
        foreach ($candidateId in $ready) {
            $rank = $script:TierRank[
                [string](Get-VerificationObjectProperty $map[$candidateId] 'tier' -Required)]
            if ($rank -lt $minimumTier) { $minimumTier = $rank }
        }
        $readyAtTier = [string[]]@($ready | Where-Object {
            $script:TierRank[
                [string](Get-VerificationObjectProperty $map[$_] 'tier' -Required)] -eq
                $minimumTier
        })
        [Array]::Sort($readyAtTier, [StringComparer]::Ordinal)
        $nextId = $readyAtTier[0]
        $ordered.Add($map[$nextId])
        [void]$emitted.Add($nextId)
    }
    $orderedArray = [object[]]$ordered.ToArray()
    $planHash = Get-VerificationDeterministicHash -Value ([ordered]@{
        registry_id = [string](Get-VerificationObjectProperty $Registry 'registry_id' -Required)
        schema_version = 1
        profile = $Profile
        nodes = $orderedArray
    })
    return [pscustomobject]@{
        profile = $Profile
        nodes = $orderedArray
        execution_plan_hash = $planHash
    }
}

function Get-VerificationExecutionBatches {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Plan)

    $nodes = [object[]]@(Get-VerificationObjectProperty $Plan 'nodes' -Required)
    $map = Get-VerificationRegistryNodeMap -Nodes $nodes
    $remaining = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($node in $nodes) {
        [void]$remaining.Add([string](Get-VerificationObjectProperty $node 'check_id' -Required))
    }
    $completed = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $batches = [Collections.Generic.List[object]]::new()
    while ($remaining.Count -gt 0) {
        $ready = [string[]]@($remaining | Where-Object {
            $candidate = $map[$_]
            @([object[]]@(Get-VerificationObjectProperty $candidate 'dependencies' -Required) |
                Where-Object {
                    $remaining.Contains([string]$_)
                }).Count -eq 0
        })
        if ($ready.Count -eq 0) { throw 'Unable to construct verification execution batches.' }
        $minimumTier = [int]::MaxValue
        foreach ($candidateId in $ready) {
            $rank = $script:TierRank[
                [string](Get-VerificationObjectProperty $map[$candidateId] 'tier' -Required)]
            if ($rank -lt $minimumTier) { $minimumTier = $rank }
        }
        $readyAtTier = [string[]]@($ready | Where-Object {
            $script:TierRank[
                [string](Get-VerificationObjectProperty $map[[string]$_] 'tier' -Required)] -eq $minimumTier
        })
        [Array]::Sort($readyAtTier, [StringComparer]::Ordinal)
        while ($readyAtTier.Count -gt 0) {
            $locks = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
            $batchIds = [Collections.Generic.List[string]]::new()
            foreach ($candidateId in $readyAtTier) {
                $id = [string]$candidateId
                $candidateLocks = [object[]]@(
                    Get-VerificationObjectProperty $map[$id] 'resource_locks' -Required)
                if (@($candidateLocks | Where-Object { $locks.Contains([string]$_) }).Count -gt 0) {
                    continue
                }
                $batchIds.Add($id)
                foreach ($lock in $candidateLocks) { [void]$locks.Add([string]$lock) }
            }
            $ids = [string[]]$batchIds.ToArray()
            $lockValues = [string[]]@($locks)
            [Array]::Sort($lockValues, [StringComparer]::Ordinal)
            $batches.Add([pscustomobject]@{
                batch_index = $batches.Count
                tier = [string](Get-VerificationObjectProperty $map[$ids[0]] 'tier' -Required)
                check_ids = $ids
                resource_locks = $lockValues
            })
            foreach ($id in $ids) {
                [void]$remaining.Remove($id)
                [void]$completed.Add($id)
            }
            $readyAtTier = @($readyAtTier | Where-Object { $remaining.Contains([string]$_) })
        }
    }
    return ,([object[]]$batches.ToArray())
}

Export-ModuleMember -Function @(
    'Test-VerificationAcceptanceClass',
    'Test-VerificationDiagnosticClass',
    'Assert-VerificationRegistry',
    'Resolve-VerificationRegistryPlan',
    'Get-VerificationExecutionBatches'
)

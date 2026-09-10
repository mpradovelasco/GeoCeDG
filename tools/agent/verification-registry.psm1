#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

$script:AcceptanceClasses = @(
    'SEMANTIC',
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

function Get-VerificationCurrentPlatform {
    [CmdletBinding()]
    param()
    if ($IsWindows) { return 'WINDOWS' }
    if ($IsLinux) { return 'LINUX' }
    if ($IsMacOS) { return 'MACOS' }
    throw 'The current platform is not supported by the verification registry.'
}

function Get-VerificationProfileMap {
    param([Parameter(Mandatory)] [object[]]$Profiles)
    $map = [Collections.Generic.Dictionary[string, object]]::new(
        [StringComparer]::Ordinal)
    foreach ($profile in $Profiles) {
        $id = [string](Get-VerificationObjectProperty $profile 'profile_id' -Required)
        if (-not $map.TryAdd($id, $profile)) {
            throw "Duplicate verification profile_id: $id"
        }
    }
    return $map
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
    $schemaVersion = [int](Get-VerificationObjectProperty $Registry 'schema_version' -Required)
    $legacyInMemoryFixture = $schemaVersion -eq 1 -and [string]::IsNullOrWhiteSpace($SchemaPath)
    if ($schemaVersion -ne 2 -and -not $legacyInMemoryFixture) {
        throw 'Verification registry schema_version must be 2.'
    }
    $catalogIds = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $catalogValues = if ($legacyInMemoryFixture) { @() } else {
        [object[]]@(Get-VerificationObjectProperty $Registry 'catalogs' -Required)
    }
    foreach ($catalog in $catalogValues) {
        $catalogId = [string](Get-VerificationObjectProperty $catalog 'catalog_id' -Required)
        if (-not $catalogIds.Add($catalogId)) { throw "Duplicate verification catalog: $catalogId" }
        $catalogPath = [string](Get-VerificationObjectProperty $catalog 'path' -Required)
        [void](ConvertTo-VerificationGitPath $catalogPath)
        $expectedHash = [string](Get-VerificationObjectProperty $catalog 'sha256' -Required)
        if ($expectedHash -cnotmatch '^[0-9a-f]{64}$') { throw "Invalid catalog hash: $catalogId" }
        if (-not [string]::IsNullOrWhiteSpace($SchemaPath)) {
            $root = [IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $SchemaPath) '../../..'))
            $fullCatalog = Resolve-VerificationContainedPath -Root $root -Path $catalogPath
            if (-not (Test-Path -LiteralPath $fullCatalog -PathType Leaf) -or
                    (Get-VerificationFileSha256 $fullCatalog) -cne $expectedHash) {
                throw "Verification catalog identity mismatch: $catalogId"
            }
        }
    }
    if (-not $legacyInMemoryFixture -and
            -not ($catalogIds.Contains('junit_inventory') -and $catalogIds.Contains('static_contracts'))) {
        throw 'Verification registry requires the JUnit and static-contract catalogs.'
    }
    $profiles = [object[]]@(Get-VerificationObjectProperty $Registry 'profiles' -Required)
    if ($profiles.Count -eq 0) { throw 'Verification registry must define profiles.' }
    $profileMap = Get-VerificationProfileMap -Profiles $profiles
    foreach ($profile in $profiles) {
        $profileId = [string](Get-VerificationObjectProperty $profile 'profile_id' -Required)
        if ($profileId -cnotmatch '^[A-Z][A-Z0-9_]*$') {
            throw "Invalid verification profile_id: $profileId"
        }
        $coverageState = [string](
            Get-VerificationObjectProperty $profile 'coverage_state' -Required)
        $missing = [string[]]@(
            Get-VerificationObjectProperty $profile 'missing_check_ids' -Required)
        if ($coverageState -cnotin @('COMPLETE', 'INCOMPLETE')) {
            throw "Invalid coverage_state for profile $($profileId): $coverageState"
        }
        if (($coverageState -ceq 'COMPLETE') -ne ($missing.Count -eq 0)) {
            throw "Profile $profileId coverage_state disagrees with missing_check_ids."
        }
    }
    $compatibility = [object[]]@(
        Get-VerificationObjectProperty $Registry 'compatibility_mappings' -Required)
    $compatibilityIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($mapping in $compatibility) {
        $selector = [string](
            Get-VerificationObjectProperty $mapping 'selector' -Required)
        $target = [string](Get-VerificationObjectProperty $mapping 'profile' -Required)
        if (-not $compatibilityIds.Add($selector)) {
            throw "Duplicate verification compatibility selector: $selector"
        }
        if (-not $profileMap.ContainsKey($target)) {
            throw "Compatibility selector $selector targets missing profile $target."
        }
    }
    $phaseSelections = [object[]]@(
        Get-VerificationObjectProperty $Registry 'phase_selections' -Required)
    $phaseIds = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($phaseSelection in $phaseSelections) {
        $phaseId = [string](
            Get-VerificationObjectProperty $phaseSelection 'phase_id' -Required)
        if (-not $phaseIds.Add($phaseId)) {
            throw "Duplicate verification phase selection: $phaseId"
        }
        $phaseMissing = [object[]]@(Get-VerificationObjectProperty $phaseSelection 'missing_check_ids' -Required)
        $phaseRequired = [object[]]@(Get-VerificationObjectProperty $phaseSelection 'required_check_ids' -Required)
        $phaseState = [string](Get-VerificationObjectProperty $phaseSelection 'coverage_state' -Required)
        if ($phaseState -cnotin @('COMPLETE','INCOMPLETE') -or
                (($phaseState -ceq 'COMPLETE') -ne ($phaseMissing.Count -eq 0)) -or
                $phaseRequired.Count -eq 0) {
            throw "Phase selection $phaseId has inconsistent derived coverage."
        }
    }
    if ($phaseSelections.Count -gt 0 -and -not $profileMap.ContainsKey('PHASE')) {
        throw 'Phase selections require a PHASE profile.'
    }
    $nodes = [object[]]@(Get-VerificationObjectProperty $Registry 'nodes' -Required)
    if ($nodes.Count -eq 0) { throw 'Verification registry must contain at least one node.' }
    $nodeMap = Get-VerificationRegistryNodeMap -Nodes $nodes

    foreach ($node in $nodes) {
        $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
        if ($id -cnotmatch '^[a-z0-9]+(?:[._-][a-z0-9]+)*$') {
            throw "Invalid verification check_id: $id"
        }
        foreach ($profileId in [object[]]@(
                Get-VerificationObjectProperty $node 'required_for_profiles' -Required)) {
            if (-not $profileMap.ContainsKey([string]$profileId)) {
                throw "Verification node $id references missing profile $profileId."
            }
        }
        $kind = [string](Get-VerificationObjectProperty $node 'node_kind' -Required)
        if ($kind -cnotin @('PROCESS_PRODUCER', 'ACCEPTANCE_LEAF', 'DIAGNOSTIC_LEAF', 'EVIDENCE_PROJECTION')) {
            throw ("Invalid verification node_kind for {0}: {1}" -f $id, $kind)
        }
        $hasDeclaredWriteRoots = Test-VerificationObjectProperty $node 'declared_write_roots'
        if ($hasDeclaredWriteRoots) {
            if ($kind -notin @('PROCESS_PRODUCER', 'ACCEPTANCE_LEAF')) {
                throw "declared_write_roots is valid only for producer or acceptance nodes: $id"
            }
            $declaredWriteRoots = [object[]]@(Get-VerificationObjectProperty $node 'declared_write_roots' -Required)
            if ($declaredWriteRoots.Count -eq 0) {
                throw "Verification node $id must declare at least one write root."
            }
            foreach ($writeRoot in $declaredWriteRoots) {
                $writeRootText = ([string]$writeRoot).Replace('\', '/').Trim()
                if ($writeRootText -notmatch '^[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*$' -or
                        $writeRootText -eq '.git' -or $writeRootText.StartsWith('.git/', [StringComparison]::Ordinal) -or
                        $writeRootText -match '(^|/)\.\.(?:/|$)') {
                    throw "Verification node $id declares an unsafe write root: $writeRoot"
                }
            }
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
        $hasSemanticDomain = Test-VerificationObjectProperty $node 'semantic_domain'
        $semanticDomain = [string](Get-VerificationObjectProperty $node 'semantic_domain')

        if ($hasContract -and $contract -is [Collections.IEnumerable] -and $contract -isnot [string]) {
            throw "Mixed contract classes are prohibited for $id."
        }
        if ([string]$contract -ceq 'SEMANTIC') {
            if (-not $hasSemanticDomain -or $semanticDomain -cnotin @('PRODUCT','SCIENTIFIC','MIXED')) {
                throw "SEMANTIC node $id requires exactly one semantic_domain."
            }
        } elseif ($hasSemanticDomain) {
            throw "semantic_domain is valid only for SEMANTIC node $id."
        }
        switch ($kind) {
            'PROCESS_PRODUCER' {
                if ($hasContract) { throw "PROCESS_PRODUCER $id must not declare contract_class." }
                if ($hasSemanticDomain) { throw "PROCESS_PRODUCER $id must not declare semantic_domain." }
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
                            'PROJECTION_EVIDENCE_PRESENT_V1',
                            'PROJECTION_JUNIT_SELECTION_V1',
                            'PROJECTION_JUNIT_DIAGNOSTIC_V1',
                            'PROJECTION_PROCESS_EXIT_V1',
                            'PROJECTION_PYTHON_CHECK_V1')) {
                    throw "EVIDENCE_PROJECTION $id has an incompatible command or output adapter."
                }
                $projection = [string](Get-VerificationObjectProperty $command 'projection' -Required)
                $expectedAdapter = @{
                    EXIT_CODE_ZERO = 'PROJECTION_EXIT_CODE_V1'
                    STRUCTURED_CONTRACT = 'PROJECTION_STRUCTURED_CONTRACT_V1'
                    EVIDENCE_PRESENT = 'PROJECTION_EVIDENCE_PRESENT_V1'
                    JUNIT_SELECTION = 'PROJECTION_JUNIT_SELECTION_V1'
                    JUNIT_DIAGNOSTIC = 'PROJECTION_JUNIT_DIAGNOSTIC_V1'
                    PROCESS_EXIT = 'PROJECTION_PROCESS_EXIT_V1'
                    PYTHON_CHECK = 'PROJECTION_PYTHON_CHECK_V1'
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
        $nodePlatforms = [object[]]@(
            Get-VerificationObjectProperty $node 'platforms' -Required)
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
            $dependencyPlatforms = [object[]]@(
                Get-VerificationObjectProperty $nodeMap[$dependencyId] 'platforms' -Required)
            foreach ($platformName in $nodePlatforms) {
                if ($dependencyPlatforms -cnotcontains [string]$platformName) {
                    throw "Verification node $id depends on $dependencyId, which is unavailable on $platformName."
                }
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
            if ($projection -ceq 'STRUCTURED_CONTRACT') {
                $evidenceName = [string](
                    Get-VerificationObjectProperty $projectionCommand 'evidence_name' -Required)
                if ($producerEvidence -cnotcontains $evidenceName) {
                    throw "EVIDENCE_PROJECTION $id requires declared structured evidence $evidenceName."
                }
            } elseif ($projection -ceq 'EVIDENCE_PRESENT') {
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

    foreach ($phaseSelection in $phaseSelections) {
        foreach ($requiredId in [object[]]@(Get-VerificationObjectProperty $phaseSelection 'required_check_ids' -Required)) {
            if (-not $nodeMap.ContainsKey([string]$requiredId)) {
                throw "Phase selection $($phaseSelection.phase_id) references missing check $requiredId."
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
        [Parameter(Mandatory)] [string]$Profile,
        [string]$Selection,
        [ValidateSet('WINDOWS', 'LINUX', 'MACOS')]
        [string]$Platform = (Get-VerificationCurrentPlatform)
    )

    [void](Assert-VerificationRegistry -Registry $Registry)
    $profiles = [object[]]@(Get-VerificationObjectProperty $Registry 'profiles' -Required)
    $profileMap = Get-VerificationProfileMap -Profiles $profiles
    $requestedProfile = $Profile
    if (-not $profileMap.ContainsKey($Profile)) {
        $mapping = @([object[]]@(
            Get-VerificationObjectProperty $Registry 'compatibility_mappings' -Required) |
            Where-Object { [string]$_.selector -ceq $Profile })
        if ($mapping.Count -ne 1) {
            throw "Unknown verification profile or compatibility selector: $Profile"
        }
        $Profile = [string]$mapping[0].profile
    }
    $profileDefinition = $profileMap[$Profile]
    $coverageState = [string](
        Get-VerificationObjectProperty $profileDefinition 'coverage_state' -Required)
    $missingCheckIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($missing in [object[]]@(
            Get-VerificationObjectProperty $profileDefinition 'missing_check_ids' -Required)) {
        [void]$missingCheckIds.Add([string]$missing)
    }
    $resolvedSelection = $null
    $phaseRequiredIds = [string[]]@()
    if ($Profile -ceq 'PHASE') {
        if ([string]::IsNullOrWhiteSpace($Selection)) {
            throw 'PHASE requires an exact registered phase selection.'
        }
        $resolvedSelection = $Selection.ToUpperInvariant()
        $phase = @([object[]]@(
            Get-VerificationObjectProperty $Registry 'phase_selections' -Required) |
            Where-Object { [string]$_.phase_id -ceq $resolvedSelection })
        if ($phase.Count -ne 1) { throw "Unknown PHASE '$Selection'." }
        $coverageState = [string]$phase[0].coverage_state
        $phaseRequiredIds = [string[]]@($phase[0].required_check_ids)
        foreach ($missing in [object[]]$phase[0].missing_check_ids) {
            [void]$missingCheckIds.Add([string]$missing)
        }
    } elseif (-not [string]::IsNullOrWhiteSpace($Selection)) {
        throw "Profile $Profile does not accept a phase selection."
    }
    $nodes = [object[]]@(Get-VerificationObjectProperty $Registry 'nodes' -Required)
    $map = Get-VerificationRegistryNodeMap -Nodes $nodes
    $selected = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    $pending = [Collections.Generic.Stack[string]]::new()
    $unsupported = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($node in $nodes) {
        $selectedForProfile = if ($Profile -ceq 'PHASE') {
            $phaseRequiredIds -ccontains [string]$node.check_id
        } else {
            [object[]]@(Get-VerificationObjectProperty $node 'required_for_profiles' -Required) -ccontains $Profile
        }
        if ($selectedForProfile) {
            $id = [string](Get-VerificationObjectProperty $node 'check_id' -Required)
            if ([object[]]@(Get-VerificationObjectProperty $node 'platforms' -Required) -ccontains
                    $Platform) {
                $pending.Push($id)
            } elseif ([string](Get-VerificationObjectProperty $node 'node_kind' -Required) -cne
                    'DIAGNOSTIC_LEAF') {
                [void]$unsupported.Add($id)
            }
        }
    }
    while ($pending.Count -gt 0) {
        $id = $pending.Pop()
        if (-not $selected.Add($id)) { continue }
        foreach ($dependency in [object[]]@(Get-VerificationObjectProperty $map[$id] 'dependencies' -Required)) {
            $pending.Push([string]$dependency)
        }
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
    if ($coverageState -ceq 'COMPLETE' -and $unsupported.Count -gt 0) {
        $coverageState = 'INCOMPLETE'
        foreach ($id in $unsupported) { [void]$missingCheckIds.Add($id) }
    }
    if ($coverageState -ceq 'COMPLETE' -and $orderedArray.Count -eq 0) {
        throw "Complete verification profile $Profile selects no nodes."
    }
    $missingArray = [string[]]@($missingCheckIds)
    [Array]::Sort($missingArray, [StringComparer]::Ordinal)
    # Registry JSON is loaded as PSCustomObject. Convert the resolved boundary to
    # dictionaries so an intentionally empty environment-variable map remains a
    # first-class empty object in canonical plan identity.
    $resolvedNodes = [object[]]@(ConvertFrom-Json -AsHashtable -Depth 100 `
        -InputObject (ConvertTo-Json -InputObject $orderedArray -Depth 100 -Compress))
    $planHash = Get-VerificationDeterministicHash -Value ([ordered]@{
        registry_id = [string](Get-VerificationObjectProperty $Registry 'registry_id' -Required)
        schema_version = [int](Get-VerificationObjectProperty $Registry 'schema_version' -Required)
        profile = $Profile
        selection = $resolvedSelection
        platform = $Platform
        coverage_state = $coverageState
        missing_check_ids = $missingArray
        nodes = $resolvedNodes
    })
    return [pscustomobject]@{
        requested_profile = $requestedProfile
        profile = $Profile
        selection = $resolvedSelection
        platform = $Platform
        coverage_state = $coverageState
        missing_check_ids = $missingArray
        nodes = $resolvedNodes
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
    'Get-VerificationCurrentPlatform',
    'Assert-VerificationRegistry',
    'Resolve-VerificationRegistryPlan',
    'Get-VerificationExecutionBatches'
)

#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-registry.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-junit.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-process-projections.psm1')
. (Join-Path $PSScriptRoot 'verification-check-host.ps1')

function Get-VerificationSupervisorProperty {
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
    if ($Required) { throw "Missing supervisor property '$Name'." }
    return $null
}

function Sort-VerificationSupervisorObjects {
    param(
        [object[]]$Values = @(),
        [Parameter(Mandatory)] [string]$PropertyName
    )

    $sorted = [Collections.Generic.List[object]]::new()
    foreach ($value in $Values) { $sorted.Add($value) }
    $comparison = [Comparison[object]]{
        param($left, $right)
        return [StringComparer]::Ordinal.Compare(
            [string](Get-VerificationSupervisorProperty $left $PropertyName -Required),
            [string](Get-VerificationSupervisorProperty $right $PropertyName -Required))
    }
    $sorted.Sort($comparison)
    return [object[]]$sorted.ToArray()
}

function Get-VerificationResultHash {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Report)

    return Get-VerificationResultIdentityHash -Report $Report
}

function Get-VerificationSupervisorExitCode {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Report)

    $verdict = [string](Get-VerificationSupervisorProperty $Report 'acceptance_verdict' -Required)
    $coverage = [string](Get-VerificationSupervisorProperty $Report 'coverage_verdict' -Required)
    if ($verdict -ceq 'NOT_PRODUCED') { return 4 }
    if ($coverage -cne 'COMPLETE') { return 3 }
    switch ($verdict) {
        'ACCEPTED' { return 0 }
        'REJECTED_SEMANTIC' { return 1 }
        'REJECTED_SAFETY' { return 2 }
        'REJECTED_VERIFICATION_CORE' { return 3 }
        default { throw "Unknown acceptance verdict: $verdict" }
    }
}

function Write-VerificationRunJournal {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Journal
    )
    [void](Write-VerificationAtomicJson -Path $Path -Value $Journal)
}

function Get-VerificationInterruptedRun {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$JournalPath)

    $journal = Read-VerificationJson -Path $JournalPath
    if ([string](Get-VerificationSupervisorProperty $journal 'journal_kind' -Required) -cne
            'GEOCEDG_VERIFICATION_RUN_JOURNAL') {
        throw 'Unexpected verification journal kind.'
    }
    $state = [string](Get-VerificationSupervisorProperty $journal 'state' -Required)
    if ($state -cne 'RUNNING') { throw 'Verification journal is not incomplete.' }
    return [pscustomobject]@{
        run_state = 'RUN_INTERRUPTED'
        acceptance_verdict = 'NOT_PRODUCED'
        coverage_verdict = 'INCOMPLETE'
        run_id = [string](Get-VerificationSupervisorProperty $journal 'run_id' -Required)
        completed_check_ids = [string[]]@(
            Get-VerificationSupervisorProperty $journal 'completed_check_ids' -Required)
        journal_path = [IO.Path]::GetFullPath($JournalPath)
    }
}

function Expand-VerificationArgument {
    param(
        [Parameter(Mandatory)] [string]$Value,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$OutputRoot,
        [Parameter(Mandatory)] [string]$NodeOutput,
        [string]$StructuredOutput,
        [Parameter(Mandatory)] [Collections.IDictionary]$EvidencePaths
    )
    $expanded = $Value.Replace('{repository_root}', $RepositoryRoot).Replace(
        '{output_root}', $OutputRoot).Replace('{node_output}', $NodeOutput)
    if (-not [string]::IsNullOrWhiteSpace($StructuredOutput)) {
        $expanded = $expanded.Replace('{structured_output}', $StructuredOutput)
    }
    foreach ($name in $EvidencePaths.Keys) {
        $expanded = $expanded.Replace(('{evidence_' + [string]$name + '}'), [string]$EvidencePaths[$name])
    }
    return $expanded
}

function Resolve-VerificationSupervisorExecutable {
    param(
        [Parameter(Mandatory)] [string]$Executable,
        [Parameter(Mandatory)] [string]$RepositoryRoot
    )
    $resolved = switch -CaseSensitive ($Executable) {
        'PWSH' {
            $current = (Get-Process -Id $PID).Path
            if ($PSVersionTable.PSVersion -lt [version]'7.2') {
                throw 'PWSH registry commands require PowerShell 7.2 or newer.'
            }
            $current
            break
        }
        'GIT' {
            $command = Get-Command git -CommandType Application -ErrorAction SilentlyContinue |
                Select-Object -First 1
            if ($null -eq $command) { throw 'The GIT registry executable is unavailable.' }
            $command.Path
            break
        }
        default {
            if ([IO.Path]::IsPathRooted($Executable)) {
                [IO.Path]::GetFullPath($Executable)
            } else {
                Resolve-VerificationContainedPath -Root $RepositoryRoot -Path $Executable
            }
        }
    }
    return [IO.Path]::GetFullPath([string]$resolved)
}

function Get-VerificationNodeCommandIdentity {
    param([Parameter(Mandatory)] [object]$Node)

    return Get-VerificationDeterministicHash -Value ([ordered]@{
        check_id = [string](Get-VerificationSupervisorProperty $Node 'check_id' -Required)
        node_kind = [string](Get-VerificationSupervisorProperty $Node 'node_kind' -Required)
        command = Get-VerificationSupervisorProperty $Node 'command' -Required
        argv = [string[]]@(Get-VerificationSupervisorProperty $Node 'argv' -Required)
        working_directory = [string](
            Get-VerificationSupervisorProperty $Node 'working_directory' -Required)
        environment_contract = Get-VerificationSupervisorProperty $Node 'environment_contract' -Required
        declared_write_roots = [string[]]@(Get-VerificationSupervisorProperty $Node 'declared_write_roots')
        resource_locks = [string[]]@(Get-VerificationSupervisorProperty $Node 'resource_locks' -Required)
        output_adapter = [string](
            Get-VerificationSupervisorProperty $Node 'output_adapter' -Required)
    })
}

function New-VerificationSyntheticCoreResult {
    param(
        [Parameter(Mandatory)] [string]$CheckId,
        [Parameter(Mandatory)] [string]$Cause,
        [string[]]$Dependencies = @()
    )
    return [pscustomobject]@{
        check_id = $CheckId + '.verification-core'
        node_kind = 'ACCEPTANCE_LEAF'
        contract_class = 'VERIFICATION_CORE'
        status = 'CONTRACT_VIOLATED'
        coverage_state = 'COMPLETE'
        command_identity = Get-VerificationDeterministicHash -Value ([ordered]@{
            check_id = $CheckId
            operation = 'supervisor_execution'
        })
        exit_code = $null
        cause = $Cause
        evidence = [object[]]@()
        dependencies = [string[]]$Dependencies
        duration_ms = 0.0
    }
}

function New-VerificationObservation {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [string]$Outcome,
        [Parameter(Mandatory)] [string]$CommandIdentity,
        [AllowNull()] [object]$ExitCode,
        [AllowNull()] [string]$Cause,
        [object[]]$Evidence = @(),
        [double]$Duration = 0.0,
        [ValidateSet('COMPLETE', 'INCOMPLETE', 'UNTRUSTED')]
        [string]$CoverageState
    )
    $kind = [string](Get-VerificationSupervisorProperty $Node 'node_kind' -Required)
    $contract = [string](Get-VerificationSupervisorProperty $Node 'contract_class' -Required)
    $value = [ordered]@{
        check_id = [string](Get-VerificationSupervisorProperty $Node 'check_id' -Required)
        node_kind = $kind
        contract_class = $contract
        command_identity = $CommandIdentity
        exit_code = $ExitCode
        cause = $Cause
        evidence = [object[]]$Evidence
        dependencies = [string[]]@(
            Get-VerificationSupervisorProperty $Node 'dependencies' -Required)
        duration_ms = [math]::Max(0.0, $Duration)
    }
    if ($contract -ceq 'SEMANTIC') {
        $value.semantic_domain = [string](
            Get-VerificationSupervisorProperty $Node 'semantic_domain' -Required)
    }
    if (Test-VerificationAcceptanceClass $contract) {
        $value.status = $Outcome
        $value.coverage_state = $(if ($PSBoundParameters.ContainsKey('CoverageState')) {
                $CoverageState
            } elseif ($Outcome -ceq 'EVIDENCE_UNTRUSTED') {
                'UNTRUSTED'
            } elseif ($Outcome -ceq 'NOT_RUN_DEPENDENCY') {
                'INCOMPLETE'
            } else { 'COMPLETE' })
    } else {
        $value.outcome = $Outcome
    }
    return [pscustomobject]$value
}

function Invoke-VerificationRegistryProcess {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$OutputRoot
    )

    $id = [string](Get-VerificationSupervisorProperty $Node 'check_id' -Required)
    $nodeOutput = Resolve-VerificationContainedPath -Root $OutputRoot -Path ('checks/' + $id)
    $command = Get-VerificationSupervisorProperty $Node 'command' -Required
    $executable = Resolve-VerificationSupervisorExecutable -Executable ([string](
        Get-VerificationSupervisorProperty $command 'executable' -Required)) `
        -RepositoryRoot $RepositoryRoot
    $workingValue = [string](Get-VerificationSupervisorProperty $Node 'working_directory' -Required)
    $workingDirectory = if ($workingValue -ceq '.') {
        [IO.Path]::GetFullPath($RepositoryRoot)
    } else {
        Resolve-VerificationContainedPath -Root $RepositoryRoot -Path $workingValue -AllowRoot
    }
    $structuredOutput = $null
    $structuredOutputName = [string](
        Get-VerificationSupervisorProperty $command 'structured_output')
    if (-not [string]::IsNullOrWhiteSpace($structuredOutputName)) {
        $structuredOutput = Join-Path $nodeOutput $structuredOutputName
    }
    $evidencePaths = @{}
    $declaredEvidence = Get-VerificationSupervisorProperty $Node 'produces_evidence'
    if ($null -ne $declaredEvidence) {
        foreach ($nameValue in [object[]]$declaredEvidence) {
            $name = [string]$nameValue
            if ($name -cin @('PROCESS_RESULT', 'STRUCTURED_RESULT')) {
                continue
            }
            $evidencePaths[$name] = Join-Path $nodeOutput ($name.ToLowerInvariant() + '.evidence')
        }
    }
    $arguments = [Collections.Generic.List[string]]::new()
    foreach ($argument in [object[]](Get-VerificationSupervisorProperty $Node 'argv' -Required)) {
        if ([string]$argument -ceq '{declared_write_roots}') {
            $roots = [string[]]@(Get-VerificationSupervisorProperty $Node 'declared_write_roots' -Required)
            $arguments.Add(($roots -join ','))
            continue
        }
        $expanded = Expand-VerificationArgument -Value ([string]$argument) `
            -RepositoryRoot $RepositoryRoot -OutputRoot $OutputRoot `
            -NodeOutput $nodeOutput -StructuredOutput $structuredOutput `
            -EvidencePaths $evidencePaths
        $arguments.Add($expanded)
    }
    $hostArguments = @{
        CheckId = $id
        NodeKind = [string](Get-VerificationSupervisorProperty $Node 'node_kind' -Required)
        Command = $executable
        Arguments = [string[]]$arguments.ToArray()
        WorkingDirectory = $workingDirectory
        EnvironmentContract = Get-VerificationSupervisorProperty $Node 'environment_contract' -Required
        OutputDirectory = $nodeOutput
        StructuredOutputPath = $structuredOutput
        EvidencePaths = $evidencePaths
    }
    return Invoke-VerificationChildProcess @hostArguments
}

function ConvertTo-VerificationProducerRecord {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [object]$Raw
    )
    $command = Get-VerificationSupervisorProperty $Node 'command' -Required
    return [pscustomobject]@{
        check_id = [string]$Raw.check_id
        node_kind = 'PROCESS_PRODUCER'
        command = [string]$Raw.command
        arguments = [string[]]@(Get-VerificationSupervisorProperty $Node 'argv' -Required)
        working_directory = [string](
            Get-VerificationSupervisorProperty $Node 'working_directory' -Required)
        environment = Get-VerificationSupervisorProperty $Node 'environment_contract' -Required
        command_identity = Get-VerificationNodeCommandIdentity $Node
        completion_state = [string]$Raw.completion_state
        exit_code = $Raw.exit_code
        completion_cause = $Raw.completion_cause
        stdout_log = [string]$Raw.stdout_log
        stderr_log = [string]$Raw.stderr_log
        stdout_sha256 = [string]$Raw.stdout_sha256
        stderr_sha256 = [string]$Raw.stderr_sha256
        stdout_encoding = [string]$Raw.stdout_encoding
        stderr_encoding = [string]$Raw.stderr_encoding
        stdout_text = $Raw.stdout_text
        stderr_text = $Raw.stderr_text
        structured_output = $Raw.structured_output
        generated_evidence = [object[]]$Raw.generated_evidence
        started_at = [string]$Raw.started_at
        finished_at = [string]$Raw.finished_at
        duration_ms = [double]$Raw.duration_ms
    }
}

function Get-VerificationRawProcessEvidence {
    param([Parameter(Mandatory)] [object]$Raw)

    $evidence = [Collections.Generic.List[object]]::new()
    $evidence.Add([pscustomobject]@{
        name = 'STDOUT'
        state = 'PRESENT'
        path = [string]$Raw.stdout_log
        sha256 = [string]$Raw.stdout_sha256
    })
    $evidence.Add([pscustomobject]@{
        name = 'STDERR'
        state = 'PRESENT'
        path = [string]$Raw.stderr_log
        sha256 = [string]$Raw.stderr_sha256
    })
    if ([string]$Raw.structured_output.state -cne 'NOT_DECLARED') {
        $evidence.Add([pscustomobject]@{
            name = 'STRUCTURED_RESULT'
            state = $(if ($null -ne $Raw.structured_output.sha256) { 'PRESENT' } else { 'ABSENT' })
            path = [string]$Raw.structured_output.path
            sha256 = $Raw.structured_output.sha256
        })
    }
    foreach ($item in [object[]]$Raw.generated_evidence) { $evidence.Add($item) }
    return ,([object[]]$evidence.ToArray())
}

function New-VerificationDependencyProducerRecord {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [string]$Cause
    )

    $command = Get-VerificationSupervisorProperty $Node 'command' -Required
    $structuredName = [string](
        Get-VerificationSupervisorProperty $command 'structured_output')
    $structuredDeclared = -not [string]::IsNullOrWhiteSpace($structuredName)
    $generatedEvidence = [Collections.Generic.List[object]]::new()
    foreach ($nameValue in [object[]](
            Get-VerificationSupervisorProperty $Node 'produces_evidence' -Required)) {
        $name = [string]$nameValue
        if ($name -cin @('PROCESS_RESULT', 'STRUCTURED_RESULT')) { continue }
        $generatedEvidence.Add([pscustomobject]@{
            name = $name
            state = 'ABSENT'
            path = $null
            sha256 = $null
        })
    }
    return [pscustomobject]@{
        check_id = [string](Get-VerificationSupervisorProperty $Node 'check_id' -Required)
        node_kind = 'PROCESS_PRODUCER'
        command = [string](Get-VerificationSupervisorProperty $command 'executable' -Required)
        arguments = [string[]]@(Get-VerificationSupervisorProperty $Node 'argv' -Required)
        working_directory = [string](
            Get-VerificationSupervisorProperty $Node 'working_directory' -Required)
        environment = Get-VerificationSupervisorProperty $Node 'environment_contract' -Required
        command_identity = Get-VerificationNodeCommandIdentity $Node
        completion_state = 'NOT_RUN_DEPENDENCY'
        exit_code = $null
        completion_cause = $Cause
        stdout_log = $null
        stderr_log = $null
        stdout_sha256 = $null
        stderr_sha256 = $null
        stdout_encoding = $null
        stderr_encoding = $null
        stdout_text = $null
        stderr_text = $null
        structured_output = [pscustomobject]@{
            state = $(if ($structuredDeclared) { 'ABSENT' } else { 'NOT_DECLARED' })
            path = $null
            sha256 = $null
            value = $null
            cause = $(if ($structuredDeclared) { $Cause } else { $null })
        }
        generated_evidence = [object[]]$generatedEvidence.ToArray()
        started_at = $null
        finished_at = $null
        duration_ms = $null
    }
}

function ConvertFrom-VerificationProcessObservation {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [object]$Raw
    )

    $contract = [string](Get-VerificationSupervisorProperty $Node 'contract_class' -Required)
    $isAcceptance = Test-VerificationAcceptanceClass $contract
    $unavailable = if ($isAcceptance) { 'EVIDENCE_UNTRUSTED' } else { 'DIAGNOSTIC_UNAVAILABLE' }
    $positive = if ($isAcceptance) { 'CONTRACT_SATISFIED' } else { 'DIAGNOSTIC_CLEAR' }
    $negative = if ($isAcceptance) { 'CONTRACT_VIOLATED' } else { 'DIAGNOSTIC_FINDING' }
    $observation = @{
        Node = $Node
        CommandIdentity = Get-VerificationNodeCommandIdentity $Node
        ExitCode = $Raw.exit_code
        Evidence = Get-VerificationRawProcessEvidence $Raw
        Duration = [double]$Raw.duration_ms
    }
    if ([string]$Raw.completion_state -cne 'COMPLETED') {
        $observation.Outcome = $unavailable
        $observation.Cause = [string]$Raw.completion_cause
        return New-VerificationObservation @observation
    }
    $adapter = [string](Get-VerificationSupervisorProperty $Node 'output_adapter' -Required)
    if ($adapter -ceq 'EXIT_CODE_V1') {
        $observation.Outcome = if ([int]$Raw.exit_code -eq 0) { $positive } else { $negative }
        $observation.Cause = if ([int]$Raw.exit_code -eq 0) {
            $null
        } else {
            "Child exited with code $($Raw.exit_code)."
        }
        return New-VerificationObservation @observation
    }
    if ($adapter -cne 'STRUCTURED_CONTRACT_V1') {
        throw "Unsupported direct output adapter: $adapter"
    }
    if ([string]$Raw.structured_output.state -cne 'PRESENT') {
        $observation.Outcome = $unavailable
        $observation.Cause = [string]$Raw.structured_output.cause
        return New-VerificationObservation @observation
    }
    try {
        $value = $Raw.structured_output.value
        $reportedClass = [string](Get-VerificationSupervisorProperty $value 'contract_class' -Required)
        $reportedOutcome = [string](Get-VerificationSupervisorProperty $value 'outcome' -Required)
        if ($reportedClass -cne $contract) { throw 'Structured result contract_class mismatch.' }
        if ($contract -ceq 'SEMANTIC' -and
                [string](Get-VerificationSupervisorProperty $value 'semantic_domain' -Required) -cne
                [string](Get-VerificationSupervisorProperty $Node 'semantic_domain' -Required)) {
            throw 'Structured result semantic_domain mismatch.'
        }
        if ($isAcceptance) {
            if ($reportedOutcome -cnotin @(
                    'CONTRACT_SATISFIED', 'CONTRACT_VIOLATED',
                    'EVIDENCE_UNTRUSTED', 'NOT_RUN_DEPENDENCY')) {
                throw 'Structured acceptance outcome is invalid.'
            }
        } elseif ($reportedOutcome -cnotin @(
                'DIAGNOSTIC_CLEAR', 'DIAGNOSTIC_FINDING', 'DIAGNOSTIC_UNAVAILABLE')) {
            throw 'Structured diagnostic outcome is invalid.'
        }
        $observation.Outcome = $reportedOutcome
        $observation.Cause = $null
        return New-VerificationObservation @observation
    } catch {
        $observation.Outcome = $unavailable
        $observation.Cause = $_.Exception.Message
        return New-VerificationObservation @observation
    }
}

function ConvertFrom-VerificationProducerProjection {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [object]$Producer
    )

    $contract = [string](Get-VerificationSupervisorProperty $Node 'contract_class' -Required)
    $isAcceptance = Test-VerificationAcceptanceClass $contract
    $unavailable = if ($isAcceptance) { 'EVIDENCE_UNTRUSTED' } else { 'DIAGNOSTIC_UNAVAILABLE' }
    $positive = if ($isAcceptance) { 'CONTRACT_SATISFIED' } else { 'DIAGNOSTIC_CLEAR' }
    $negative = if ($isAcceptance) { 'CONTRACT_VIOLATED' } else { 'DIAGNOSTIC_FINDING' }
    $projectionIdentity = Get-VerificationDeterministicHash -Value ([ordered]@{
        descriptor = $Node.command
        producer_command_identity = [string]$Producer.command_identity
    })
    $observation = @{
        Node = $Node
        CommandIdentity = $projectionIdentity
        ExitCode = $Producer.exit_code
        Evidence = [object[]]$Producer.generated_evidence
        Duration = 0.0
    }
    if ([string]$Producer.completion_state -cne 'COMPLETED') {
        $observation.Outcome = if ($isAcceptance -and
                [string]$Producer.completion_state -ceq 'NOT_RUN_DEPENDENCY') {
            'NOT_RUN_DEPENDENCY'
        } else {
            $unavailable
        }
        $observation.Cause = [string]$Producer.completion_cause
        return New-VerificationObservation @observation
    }
    $adapter = [string](Get-VerificationSupervisorProperty $Node 'output_adapter' -Required)
    switch ($adapter) {
        'PROJECTION_EXIT_CODE_V1' {
            $observation.Outcome = if ([int]$Producer.exit_code -eq 0) { $positive } else { $negative }
            $observation.Cause = if ([int]$Producer.exit_code -eq 0) {
                $null
            } else {
                "Projected producer exit code $($Producer.exit_code)."
            }
        }
        'PROJECTION_EVIDENCE_PRESENT_V1' {
            $projectionCommand = Get-VerificationSupervisorProperty $Node 'command' -Required
            $evidenceName = [string](
                Get-VerificationSupervisorProperty $projectionCommand 'evidence_name' -Required)
            $projectedEvidence = [object[]]@(
                [object[]]$Producer.generated_evidence | Where-Object {
                    [string]$_.name -ceq $evidenceName
                })
            $observation.Evidence = $projectedEvidence
            $evidenceAvailable = $projectedEvidence.Count -eq 1 -and
                [string]$projectedEvidence[0].state -ceq 'PRESENT'
            $observation.Outcome = if ($evidenceAvailable) { $positive } else { $unavailable }
            $observation.Cause = if ($evidenceAvailable) {
                $null
            } else {
                "Declared evidence '$evidenceName' is unavailable."
            }
        }
        'PROJECTION_STRUCTURED_CONTRACT_V1' {
            $projectionCommand = Get-VerificationSupervisorProperty $Node 'command' -Required
            $evidenceName = [string](
                Get-VerificationSupervisorProperty $projectionCommand 'evidence_name' -Required)
            $structuredEvidence = $null
            if ($evidenceName -ceq 'STRUCTURED_RESULT') {
                $structuredEvidence = $Producer.structured_output
                $observation.Evidence = @([pscustomobject]@{
                    name = 'STRUCTURED_RESULT'
                    state = $(if ($null -ne $Producer.structured_output.sha256) {
                        'PRESENT'
                    } else { 'ABSENT' })
                    path = $Producer.structured_output.path
                    sha256 = $Producer.structured_output.sha256
                })
            } else {
                $namedEvidence = @([object[]]$Producer.generated_evidence | Where-Object {
                    [string]$_.name -ceq $evidenceName
                })
                $observation.Evidence = $namedEvidence
                if ($namedEvidence.Count -eq 1 -and
                        [string]$namedEvidence[0].state -ceq 'PRESENT') {
                    try {
                        $structuredEvidence = [pscustomobject]@{
                            state = 'PRESENT'
                            value = Read-VerificationJson -Path ([string]$namedEvidence[0].path)
                            cause = $null
                        }
                    } catch {
                        $structuredEvidence = [pscustomobject]@{
                            state = 'MALFORMED'
                            value = $null
                            cause = $_.Exception.Message
                        }
                    }
                } else {
                    $structuredEvidence = [pscustomobject]@{
                        state = 'ABSENT'
                        value = $null
                        cause = "Declared structured evidence '$evidenceName' is unavailable."
                    }
                }
            }
            if ([string]$structuredEvidence.state -cne 'PRESENT') {
                $observation.Outcome = $unavailable
                $observation.Cause = [string]$structuredEvidence.cause
                return New-VerificationObservation @observation
            }
            try {
                $value = $structuredEvidence.value
                $reportedClass = [string](Get-VerificationSupervisorProperty $value 'contract_class' -Required)
                $reportedOutcome = [string](Get-VerificationSupervisorProperty $value 'outcome' -Required)
                if ($reportedClass -cne $contract) {
                    throw 'Projected structured contract_class does not match the projection.'
                }
                if ($isAcceptance) {
                    if ($reportedOutcome -cnotin @(
                            'CONTRACT_SATISFIED', 'CONTRACT_VIOLATED',
                            'EVIDENCE_UNTRUSTED', 'NOT_RUN_DEPENDENCY')) {
                        throw 'Projected acceptance outcome is invalid.'
                    }
                } elseif ($reportedOutcome -cnotin @(
                        'DIAGNOSTIC_CLEAR', 'DIAGNOSTIC_FINDING', 'DIAGNOSTIC_UNAVAILABLE')) {
                    throw 'Projected diagnostic outcome is invalid.'
                }
                $observation.Outcome = $reportedOutcome
                $observation.Cause = $null
            } catch {
                $observation.Outcome = $unavailable
                $observation.Cause = $_.Exception.Message
            }
        }
        'PROJECTION_JUNIT_SELECTION_V1' {
            if ([string]$Producer.structured_output.state -cne 'PRESENT') {
                $observation.Outcome = $unavailable
                $observation.Cause = [string]$Producer.structured_output.cause
                break
            }
            $projected = ConvertFrom-VerificationJUnitSelection `
                -ProcessEvidence $Producer.structured_output.value `
                -Selection $Producer.structured_output.value.selection `
                -SemanticDomain ([string](Get-VerificationSupervisorProperty $Node 'semantic_domain' -Required))
            $observation.Outcome = [string]$projected.outcome
            $observation.Cause = [string]$projected.cause
            $observation.CoverageState = [string]$projected.coverage_state
            $observation.Evidence = @([pscustomobject]@{
                name='STRUCTURED_RESULT'; state='PRESENT'; path=$Producer.structured_output.path
                sha256=$Producer.structured_output.sha256
            })
        }
        'PROJECTION_JUNIT_DIAGNOSTIC_V1' {
            if ([string]$Producer.structured_output.state -cne 'PRESENT') {
                $observation.Outcome = $unavailable
                $observation.Cause = [string]$Producer.structured_output.cause
                break
            }
            $projected = ConvertFrom-VerificationJUnitDiagnosticSelection `
                -ProcessEvidence $Producer.structured_output.value `
                -Selection $Producer.structured_output.value.selection `
                -DiagnosticClass $contract
            $observation.Outcome = [string]$projected.diagnostic_outcome
            $observation.Cause = [string]$projected.cause
            $observation.Evidence = @([pscustomobject]@{
                name='STRUCTURED_RESULT'; state='PRESENT'; path=$Producer.structured_output.path
                sha256=$Producer.structured_output.sha256
            })
        }
        'PROJECTION_PROCESS_EXIT_V1' {
            if ([string]$Producer.structured_output.state -cne 'PRESENT') {
                $observation.Outcome = $unavailable
                $observation.Cause = [string]$Producer.structured_output.cause
                break
            }
            $projected = if ($contract -ceq 'SEMANTIC') {
                ConvertFrom-VerificationProcessEvidence `
                    -Evidence $Producer.structured_output.value `
                    -ContractClass $contract `
                    -SemanticDomain ([string](Get-VerificationSupervisorProperty `
                            $Node 'semantic_domain' -Required))
            } else {
                ConvertFrom-VerificationProcessEvidence `
                    -Evidence $Producer.structured_output.value `
                    -ContractClass $contract
            }
            $observation.Outcome = [string]$projected.outcome
            $observation.Cause = [string]$projected.cause
        }
        'PROJECTION_PYTHON_CHECK_V1' {
            if ([string]$Producer.structured_output.state -cne 'PRESENT') {
                $observation.Outcome = $unavailable
                $observation.Cause = [string]$Producer.structured_output.cause
                break
            }
            $projected = ConvertFrom-VerificationPythonCheckEvidence `
                -Evidence $Producer.structured_output.value `
                -SemanticDomain ([string](Get-VerificationSupervisorProperty $Node 'semantic_domain' -Required))
            $observation.Outcome = [string]$projected.outcome
            $observation.Cause = [string]$projected.cause
        }
        default { throw "Unknown producer projection adapter: $adapter" }
    }
    return New-VerificationObservation @observation
}

function New-VerificationDependencyObservation {
    param(
        [Parameter(Mandatory)] [object]$Node,
        [Parameter(Mandatory)] [string[]]$UnavailableDependencies
    )
    $contract = [string](Get-VerificationSupervisorProperty $Node 'contract_class' -Required)
    $outcome = if (Test-VerificationAcceptanceClass $contract) {
        'NOT_RUN_DEPENDENCY'
    } else {
        'DIAGNOSTIC_UNAVAILABLE'
    }
    $arguments = @{
        Node = $Node
        Outcome = $outcome
        CommandIdentity = Get-VerificationDeterministicHash -Value ([ordered]@{
            descriptor = Get-VerificationSupervisorProperty $Node 'command' -Required
            unavailable_dependencies = [string[]]$UnavailableDependencies
        })
        ExitCode = $null
        Cause = 'Required evidence was unavailable from: ' + ($UnavailableDependencies -join ', ')
        Evidence = [object[]]@()
        Duration = 0.0
    }
    return New-VerificationObservation @arguments
}

function Test-VerificationDependencyAvailable {
    param(
        [Parameter(Mandatory)] [string]$DependencyId,
        [Parameter(Mandatory)] [object]$ProducerMap,
        [Parameter(Mandatory)] [object]$ObservationMap
    )
    $hasProducer = if ($null -ne $ProducerMap.PSObject.Methods['ContainsKey']) {
        $ProducerMap.ContainsKey($DependencyId)
    } else {
        $ProducerMap.Contains($DependencyId)
    }
    if ($hasProducer) {
        return [string]$ProducerMap[$DependencyId].completion_state -ceq 'COMPLETED'
    }
    $hasObservation = if ($null -ne $ObservationMap.PSObject.Methods['ContainsKey']) {
        $ObservationMap.ContainsKey($DependencyId)
    } else {
        $ObservationMap.Contains($DependencyId)
    }
    if (-not $hasObservation) { return $false }
    $observation = $ObservationMap[$DependencyId]
    if ($null -ne $observation.PSObject.Properties['status']) {
        return [string]$observation.status -ceq 'CONTRACT_SATISFIED'
    }
    if ($null -ne $observation.PSObject.Properties['outcome']) {
        return [string]$observation.outcome -cin @('DIAGNOSTIC_CLEAR', 'DIAGNOSTIC_FINDING')
    }
    return $false
}

function Test-VerificationMapContains {
    param(
        [Parameter(Mandatory)] [object]$Map,
        [Parameter(Mandatory)] [string]$Key
    )
    if ($null -ne $Map.PSObject.Methods['ContainsKey']) {
        return $Map.ContainsKey($Key)
    }
    return $Map.Contains($Key)
}

function Compare-VerificationBaselineResults {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$BaselineReport,
        [Parameter(Mandatory)] [object]$CandidateReport
    )
    foreach ($entry in @(
            @{ Name = 'baseline'; Report = $BaselineReport },
            @{ Name = 'candidate'; Report = $CandidateReport })) {
        $declaredHash = [string](
            Get-VerificationSupervisorProperty $entry.Report 'result_hash' -Required)
        if ($declaredHash -cne (Get-VerificationResultHash $entry.Report)) {
            throw "$($entry.Name) report has an invalid deterministic result hash."
        }
    }
    $identityFields = @('profile', 'execution_plan_hash', 'environment_fingerprint')
    $identityMismatches = [Collections.Generic.List[string]]::new()
    foreach ($field in $identityFields) {
        if ([string](Get-VerificationSupervisorProperty $BaselineReport $field -Required) -cne
                [string](Get-VerificationSupervisorProperty $CandidateReport $field -Required)) {
            $identityMismatches.Add($field)
        }
    }
    foreach ($field in @('commit', 'tree')) {
        $baselineField = 'candidate_' + $field
        $candidateField = 'base_' + $field
        $baselineValue = [string](
            Get-VerificationSupervisorProperty $BaselineReport $baselineField -Required)
        $candidateValue = [string](
            Get-VerificationSupervisorProperty $CandidateReport $candidateField)
        if ([string]::IsNullOrWhiteSpace($candidateValue) -or
                $candidateValue -cne $baselineValue) {
            $identityMismatches.Add($candidateField)
        }
    }
    if ($identityMismatches.Count -gt 0) {
        return [pscustomobject][ordered]@{
            state = 'NOT_COMPARABLE'
            identity_mismatches = [string[]]$identityMismatches.ToArray()
            candidate_regression = $null
            final_acceptability = 'REQUIRES_CURRENT_RESULT'
            comparisons = [object[]]@()
        }
    }
    $baseResults = [object[]]@(
        Get-VerificationSupervisorProperty $BaselineReport 'acceptance_results' -Required)
    $candidateResults = [object[]]@(
        Get-VerificationSupervisorProperty $CandidateReport 'acceptance_results' -Required)
    $baseResults = [object[]]@($baseResults | Where-Object { $null -ne $_ })
    $candidateResults = [object[]]@($candidateResults | Where-Object { $null -ne $_ })
    $ids = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($result in [object[]]@($baseResults + $candidateResults)) {
        [void]$ids.Add([string]$result.check_id)
    }
    $orderedIds = [string[]]@($ids)
    [Array]::Sort($orderedIds, [StringComparer]::Ordinal)
    $comparisons = [Collections.Generic.List[object]]::new()
    foreach ($id in $orderedIds) {
        $base = @($baseResults | Where-Object { [string]$_.check_id -ceq $id })
        $candidate = @($candidateResults | Where-Object { [string]$_.check_id -ceq $id })
        $baseStatus = if ($base.Count -eq 1) { [string]$base[0].status } else { 'NOT_RUN' }
        $candidateStatus = if ($candidate.Count -eq 1) {
            [string]$candidate[0].status
        } else { 'NOT_RUN' }
        $commandComparable = $base.Count -eq 1 -and $candidate.Count -eq 1 -and
            [string]$base[0].command_identity -ceq [string]$candidate[0].command_identity
        $classification = if (-not $commandComparable -and $base.Count -eq 1 -and
                $candidate.Count -eq 1) {
            'CHECK_IDENTITY_CHANGED'
        } elseif ($baseStatus -ceq 'CONTRACT_SATISFIED' -and
                $candidateStatus -cne 'CONTRACT_SATISFIED') {
            'PASS_TO_FAIL'
        } elseif ($baseStatus -ceq 'CONTRACT_VIOLATED' -and
                $candidateStatus -ceq 'CONTRACT_SATISFIED') {
            'FAIL_TO_PASS'
        } elseif ($baseStatus -ceq 'CONTRACT_VIOLATED' -and
                $candidateStatus -ceq 'CONTRACT_VIOLATED') {
            if ((Get-VerificationObservationIdentityHash $base[0]) -ceq
                    (Get-VerificationObservationIdentityHash $candidate[0])) {
                'FAIL_TO_SAME_FAIL'
            } else { 'FAIL_CHANGED' }
        } elseif ($baseStatus -eq 'NOT_RUN' -and $candidateStatus -cne
                'CONTRACT_SATISFIED') {
            'NOT_RUN_TO_FAIL'
        } elseif ($baseStatus -ceq $candidateStatus) {
            'UNCHANGED'
        } else {
            'CHANGED_RESULT'
        }
        $comparisons.Add([pscustomobject][ordered]@{
            check_id = $id
            contract_class = $(if ($candidate.Count -eq 1) {
                [string]$candidate[0].contract_class
            } elseif ($base.Count -eq 1) { [string]$base[0].contract_class } else { $null })
            base_status = $baseStatus
            candidate_status = $candidateStatus
            comparison = $classification
            candidate_regression = $classification -cin @(
                'PASS_TO_FAIL', 'FAIL_CHANGED', 'NOT_RUN_TO_FAIL')
            final_acceptable = $candidateStatus -ceq 'CONTRACT_SATISFIED'
        })
    }
    $values = [object[]]$comparisons.ToArray()
    return [pscustomobject][ordered]@{
        state = 'COMPARED'
        identity_mismatches = [string[]]@()
        candidate_regression = @($values | Where-Object { $_.candidate_regression }).Count -gt 0
        final_acceptability = $(if (@($values | Where-Object {
                    -not $_.final_acceptable
                }).Count -eq 0) { 'ACCEPTABLE' } else { 'NOT_ACCEPTABLE' })
        comparisons = $values
    }
}

function New-VerificationAggregatedReport {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RunId,
        [Parameter(Mandatory)] [string]$Profile,
        [Parameter(Mandatory)] [object]$Identity,
        [Parameter(Mandatory)] [string]$ExecutionPlanHash,
        [object[]]$ProcessProducers = @(),
        [object[]]$AcceptanceResults = @(),
        [object[]]$DiagnosticResults = @(),
        [string[]]$RequiredAcceptanceIds = @(),
        [datetime]$StartedAt = [datetime]::UtcNow,
        [datetime]$FinishedAt = [datetime]::UtcNow
    )

    $acceptance = [object[]]@(Sort-VerificationSupervisorObjects $AcceptanceResults check_id)
    $diagnostics = [object[]]@(Sort-VerificationSupervisorObjects $DiagnosticResults check_id)
    $producers = [object[]]@(Sort-VerificationSupervisorObjects $ProcessProducers check_id)
    $allResultIds = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($result in [object[]]@($producers + $acceptance + $diagnostics)) {
        $resultId = [string](Get-VerificationSupervisorProperty $result 'check_id' -Required)
        if ([string]::IsNullOrWhiteSpace($resultId) -or -not $allResultIds.Add($resultId)) {
            throw 'Verification aggregation requires unique, nonempty result identifiers.'
        }
    }
    $requiredIdSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($requiredId in [string[]]$RequiredAcceptanceIds) {
        if ([string]::IsNullOrWhiteSpace($requiredId) -or
                -not $requiredIdSet.Add($requiredId)) {
            throw 'Verification aggregation requires unique, nonempty coverage identifiers.'
        }
    }
    foreach ($result in $acceptance) {
        $resultId = [string](Get-VerificationSupervisorProperty $result 'check_id' -Required)
        if (-not $requiredIdSet.Contains($resultId)) {
            throw "Acceptance result '$resultId' is absent from required coverage."
        }
    }
    $required = [string[]]@($requiredIdSet)
    [Array]::Sort($required, [StringComparer]::Ordinal)
    $completedIds = [Collections.Generic.List[string]]::new()
    $untrustedIds = [Collections.Generic.List[string]]::new()
    $notRunIds = [Collections.Generic.List[string]]::new()
    foreach ($id in $required) {
        $result = @($acceptance | Where-Object { $_.check_id -ceq $id })
        if ($result.Count -eq 0) {
            $notRunIds.Add($id)
            continue
        }
        switch ([string]$result[0].coverage_state) {
            'UNTRUSTED' { $untrustedIds.Add($id) }
            'INCOMPLETE' { $notRunIds.Add($id) }
            default { $completedIds.Add($id) }
        }
    }
    $coverageVerdict = if ($untrustedIds.Count -gt 0) {
        'UNTRUSTED'
    } elseif ($notRunIds.Count -gt 0) {
        'INCOMPLETE'
    } else {
        'COMPLETE'
    }
    $violated = @($acceptance | Where-Object { $_.status -ceq 'CONTRACT_VIOLATED' })
    $coreViolation = @($violated | Where-Object { $_.contract_class -ceq 'VERIFICATION_CORE' })
    $safetyViolation = @($violated | Where-Object { $_.contract_class -ceq 'SAFETY' })
    $semanticViolation = @($violated | Where-Object {
        $_.contract_class -ceq 'SEMANTIC'
    })
    $acceptanceVerdict = if ($coverageVerdict -cne 'COMPLETE' -or $coreViolation.Count -gt 0) {
        'REJECTED_VERIFICATION_CORE'
    } elseif ($safetyViolation.Count -gt 0) {
        'REJECTED_SAFETY'
    } elseif ($semanticViolation.Count -gt 0) {
        'REJECTED_SEMANTIC'
    } else {
        'ACCEPTED'
    }
    $diagnosticSummary = [ordered]@{
        clear = @($diagnostics | Where-Object { $_.outcome -ceq 'DIAGNOSTIC_CLEAR' }).Count
        findings = @($diagnostics | Where-Object { $_.outcome -ceq 'DIAGNOSTIC_FINDING' }).Count
        unavailable = @($diagnostics | Where-Object { $_.outcome -ceq 'DIAGNOSTIC_UNAVAILABLE' }).Count
    }
    $report = [ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-result.schema.json'
        schema_version = 2
        run_id = $RunId
        profile = $Profile
        run_state = 'COMPLETED'
        base_commit = Get-VerificationSupervisorProperty $Identity 'base_commit'
        base_tree = Get-VerificationSupervisorProperty $Identity 'base_tree'
        candidate_commit = [string](
            Get-VerificationSupervisorProperty $Identity 'candidate_commit' -Required)
        candidate_tree = [string](
            Get-VerificationSupervisorProperty $Identity 'candidate_tree' -Required)
        execution_plan_hash = $ExecutionPlanHash
        environment_fingerprint = [string](
            Get-VerificationSupervisorProperty $Identity 'environment_fingerprint' -Required)
        started_at = $StartedAt.ToUniversalTime().ToString('o')
        finished_at = $FinishedAt.ToUniversalTime().ToString('o')
        process_producers = $producers
        acceptance_results = $acceptance
        diagnostic_findings = $diagnostics
        coverage = [ordered]@{
            required_check_ids = $required
            completed_check_ids = [string[]]$completedIds.ToArray()
            untrusted_check_ids = [string[]]$untrustedIds.ToArray()
            not_run_check_ids = [string[]]$notRunIds.ToArray()
        }
        acceptance_verdict = $acceptanceVerdict
        coverage_verdict = $coverageVerdict
        diagnostic_summary = $diagnosticSummary
        summary = [ordered]@{
            acceptance_satisfied = @($acceptance | Where-Object {
                $_.status -ceq 'CONTRACT_SATISFIED'
            }).Count
            acceptance_violated = $violated.Count
            acceptance_untrusted = @($acceptance | Where-Object {
                $_.status -ceq 'EVIDENCE_UNTRUSTED'
            }).Count
            acceptance_not_run = @($acceptance | Where-Object {
                $_.status -ceq 'NOT_RUN_DEPENDENCY'
            }).Count
            producer_count = $producers.Count
            diagnostic_count = $diagnostics.Count
        }
        result_hash = ''
    }
    $report.result_hash = Get-VerificationResultHash -Report $report
    return [pscustomobject]$report
}

function Invoke-VerificationSupervisor {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Registry,
        [Parameter(Mandatory)] [string]$Profile,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$OutputDirectory,
        [Parameter(Mandatory)] [object]$Identity,
        [string]$Selection,
        [string]$RegistrySchemaPath,
        [string]$ResultSchemaPath
    )

    [void](Assert-VerificationRegistry -Registry $Registry -SchemaPath $RegistrySchemaPath)
    $plan = Resolve-VerificationRegistryPlan -Registry $Registry -Profile $Profile `
        -Selection $Selection
    $outputRoot = [IO.Path]::GetFullPath($OutputDirectory)
    if (Test-Path -LiteralPath $outputRoot) {
        throw "Supervisor output directory must be unique and initially absent: $outputRoot"
    }
    [void][IO.Directory]::CreateDirectory($outputRoot)
    $runId = 'verification-' + [guid]::NewGuid().ToString('N')
    $started = [datetime]::UtcNow
    $journalPath = Join-Path $outputRoot 'run-journal.json'
    $reportPath = Join-Path $outputRoot 'verification-result.json'
    $journal = [ordered]@{
        schema_version = 1
        journal_kind = 'GEOCEDG_VERIFICATION_RUN_JOURNAL'
        run_id = $runId
        state = 'RUNNING'
        profile = [string]$plan.profile
        execution_plan_hash = [string]$plan.execution_plan_hash
        candidate_commit = [string](
            Get-VerificationSupervisorProperty $Identity 'candidate_commit' -Required)
        candidate_tree = [string](
            Get-VerificationSupervisorProperty $Identity 'candidate_tree' -Required)
        completed_check_ids = [string[]]@()
        report_path = $null
        report_hash = $null
    }
    Write-VerificationRunJournal -Path $journalPath -Journal $journal
    $producerMap = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    $observationMap = [Collections.Generic.Dictionary[string, object]]::new([StringComparer]::Ordinal)
    $producers = [Collections.Generic.List[object]]::new()
    $acceptance = [Collections.Generic.List[object]]::new()
    $diagnostics = [Collections.Generic.List[object]]::new()
    $completed = [Collections.Generic.List[string]]::new()

    $executionNodes = [Collections.Generic.List[object]]::new()
    if ([string]$plan.coverage_state -ceq 'COMPLETE') {
        $nodeMap = [Collections.Generic.Dictionary[string, object]]::new(
            [StringComparer]::Ordinal)
        foreach ($node in [object[]]$plan.nodes) {
            if (-not $nodeMap.TryAdd([string]$node.check_id, $node)) {
                throw "Resolved plan duplicates check $($node.check_id)."
            }
        }
        $scheduledIds = [Collections.Generic.HashSet[string]]::new(
            [StringComparer]::Ordinal)
        foreach ($batch in [object[]](Get-VerificationExecutionBatches -Plan $plan)) {
            $activeLocks = [Collections.Generic.HashSet[string]]::new(
                [StringComparer]::Ordinal)
            foreach ($idValue in [object[]]$batch.check_ids) {
                $idValue = [string]$idValue
                if (-not $scheduledIds.Add($idValue)) {
                    throw "Resolved plan schedules check more than once: $idValue"
                }
                $scheduledNode = $nodeMap[$idValue]
                foreach ($lock in [object[]]$scheduledNode.resource_locks) {
                    if (-not $activeLocks.Add([string]$lock)) {
                        throw "Execution batch reuses resource lock $lock."
                    }
                }
                $executionNodes.Add($scheduledNode)
            }
        }
        if ($scheduledIds.Count -ne $nodeMap.Count) {
            throw 'Resolved execution batches omit one or more checks.'
        }
    }

    foreach ($node in [object[]]$executionNodes.ToArray()) {
        $id = [string](Get-VerificationSupervisorProperty $node 'check_id' -Required)
        $kind = [string](Get-VerificationSupervisorProperty $node 'node_kind' -Required)
        $dependencies = [string[]]@(
            Get-VerificationSupervisorProperty $node 'dependencies' -Required)
        $projectionProducer = if ($kind -ceq 'EVIDENCE_PROJECTION') {
            [string](Get-VerificationSupervisorProperty $node 'producer_dependency' -Required)
        } else {
            $null
        }
        $unavailableDependencies = [string[]]@($dependencies | Where-Object {
            if ($kind -ceq 'EVIDENCE_PROJECTION' -and $_ -ceq $projectionProducer) {
                return -not (Test-VerificationMapContains -Map $producerMap -Key $_)
            }
            -not (Test-VerificationDependencyAvailable -DependencyId $_ -ProducerMap $producerMap -ObservationMap $observationMap)
        })
        try {
            if ($unavailableDependencies.Count -gt 0 -and $kind -cne 'PROCESS_PRODUCER') {
                $observation = New-VerificationDependencyObservation -Node $node -UnavailableDependencies $unavailableDependencies
                $observationMap[$id] = $observation
                if ($null -ne $observation.PSObject.Properties['status']) {
                    $acceptance.Add($observation)
                } else {
                    $diagnostics.Add($observation)
                }
            } elseif ($kind -ceq 'PROCESS_PRODUCER') {
                if ($unavailableDependencies.Count -gt 0) {
                    $record = New-VerificationDependencyProducerRecord -Node $node -Cause (
                        'Required evidence was unavailable from: ' +
                        ($unavailableDependencies -join ', '))
                } else {
                    $raw = Invoke-VerificationRegistryProcess -Node $node -RepositoryRoot $RepositoryRoot -OutputRoot $outputRoot
                    $record = ConvertTo-VerificationProducerRecord -Node $node -Raw $raw
                }
                $producerMap[$id] = $record
                $producers.Add($record)
            } elseif ($kind -ceq 'EVIDENCE_PROJECTION') {
                $producerId = [string](
                    Get-VerificationSupervisorProperty $node 'producer_dependency' -Required)
                $observation = ConvertFrom-VerificationProducerProjection -Node $node -Producer $producerMap[$producerId]
                $observationMap[$id] = $observation
                if ($null -ne $observation.PSObject.Properties['status']) {
                    $acceptance.Add($observation)
                } else {
                    $diagnostics.Add($observation)
                }
            } else {
                $raw = Invoke-VerificationRegistryProcess -Node $node -RepositoryRoot $RepositoryRoot -OutputRoot $outputRoot
                $observation = ConvertFrom-VerificationProcessObservation -Node $node -Raw $raw
                $observationMap[$id] = $observation
                if ($null -ne $observation.PSObject.Properties['status']) {
                    $acceptance.Add($observation)
                } else {
                    $diagnostics.Add($observation)
                }
            }
        } catch {
            $core = New-VerificationSyntheticCoreResult -CheckId $id -Cause $_.Exception.Message -Dependencies $dependencies
            $acceptance.Add($core)
            $observationMap[$core.check_id] = $core
        }
        $completed.Add($id)
        $journal.completed_check_ids = [string[]]$completed.ToArray()
        Write-VerificationRunJournal -Path $journalPath -Journal $journal
    }

    $requiredAcceptanceSet = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($requiredNode in [object[]]@($plan.nodes | Where-Object {
            $_.node_kind -cne 'PROCESS_PRODUCER' -and
            (Test-VerificationAcceptanceClass ([string]$_.contract_class))
        })) {
        [void]$requiredAcceptanceSet.Add([string]$requiredNode.check_id)
    }
    foreach ($result in [object[]]$acceptance.ToArray()) {
        [void]$requiredAcceptanceSet.Add([string]$result.check_id)
    }
    foreach ($missingId in [string[]]$plan.missing_check_ids) {
        [void]$requiredAcceptanceSet.Add($missingId)
    }
    $requiredAcceptance = [string[]]@($requiredAcceptanceSet)
    [Array]::Sort($requiredAcceptance, [StringComparer]::Ordinal)
    $report = New-VerificationAggregatedReport -RunId $runId `
        -Profile ([string]$plan.profile) -Identity $Identity `
        -ExecutionPlanHash ([string]$plan.execution_plan_hash) `
        -ProcessProducers ([object[]]$producers.ToArray()) `
        -AcceptanceResults ([object[]]$acceptance.ToArray()) `
        -DiagnosticResults ([object[]]$diagnostics.ToArray()) `
        -RequiredAcceptanceIds $requiredAcceptance -StartedAt $started `
        -FinishedAt ([datetime]::UtcNow)
    if (-not [string]::IsNullOrWhiteSpace($ResultSchemaPath)) {
        [void](Assert-VerificationJsonSchema -Value $report -SchemaPath $ResultSchemaPath)
    }
    [void](Write-VerificationAtomicJson -Path $reportPath -Value $report)
    $journal.state = 'COMPLETED'
    $journal.report_path = $reportPath
    $journal.report_hash = [string]$report.result_hash
    Write-VerificationRunJournal -Path $journalPath -Journal $journal
    return [pscustomobject]@{
        report = $report
        report_path = $reportPath
        journal_path = $journalPath
        plan = $plan
        exit_code = Get-VerificationSupervisorExitCode -Report $report
    }
}

Export-ModuleMember -Function @(
    'Get-VerificationResultHash',
    'Get-VerificationSupervisorExitCode',
    'Get-VerificationInterruptedRun',
    'Compare-VerificationBaselineResults',
    'New-VerificationAggregatedReport',
    'Invoke-VerificationSupervisor'
)

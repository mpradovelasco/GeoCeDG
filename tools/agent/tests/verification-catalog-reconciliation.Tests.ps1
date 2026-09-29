#requires -Version 7.2
# Focused suite for the post-run deterministic catalog reconciliation
# (verification-levels.md section 11.3, ADR 0030). It builds a temporary Git
# fixture over the real FINAL registry plan and synthetic, fully sealed source
# runs. It is deliberately not a registered FINAL leaf: registering it would
# change the FINAL execution plan.
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$agent = Join-Path $repository 'tools/agent'
Import-Module (Join-Path $agent 'verification-io.psm1') -Force
Import-Module (Join-Path $agent 'verification-junit.psm1') -Force
Import-Module (Join-Path $agent 'verification-registry.psm1') -Force
Import-Module (Join-Path $agent 'verification-supervisor.psm1') -Force
$receiptModule = Import-Module (Join-Path $agent 'verification-receipt.psm1') -Force -PassThru
$reconciliationModule = Import-Module (Join-Path $agent 'verification-catalog-reconciliation.psm1') `
    -Force -PassThru
. (Join-Path $PSScriptRoot 'fixtures/verification-environment-fixture.ps1')
$pwsh = (Get-Process -Id $PID).Path
$utf8 = [Text.UTF8Encoding]::new($false)
$closeout = Join-Path $agent 'phase-closeout.ps1'
$cli = Join-Path $agent 'reconcile-verification-catalog.ps1'
$resultSchema = Join-Path $repository 'geocedg/specs/operations/verification-result.schema.json'
$receiptSchema = Join-Path $repository 'geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json'
$inventoryPath = 'geocedg/specs/operations/verification-junit-inventory.json'
$registryPath = 'geocedg/specs/operations/verification-registry.json'
$staleCause = 'Executed JUnit identities differ from the tracked selection inventory.'
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-catalog-reconciliation-tests-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-catalog-reconciliation', $utf8)
$fixture = Join-Path $root 'repository'
$outside = Join-Path $root 'outside'
[void][IO.Directory]::CreateDirectory($fixture)
[void][IO.Directory]::CreateDirectory($outside)
$script:Cases = 0
$script:Assertions = 0

function Assert-Case([bool]$Condition, [string]$Message) {
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}
function Invoke-Case([string]$Name, [scriptblock]$Body) {
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}
function Write-Text([string]$Path, [string]$Text) {
    $parent = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $parent)) { [void][IO.Directory]::CreateDirectory($parent) }
    [IO.File]::WriteAllText($Path, $Text, $utf8)
}
function Write-FixtureText([string]$Relative, [string]$Text) { Write-Text (Join-Path $fixture $Relative) $Text }
function Add-FixtureText([string]$Relative, [string]$Text) {
    $path = Join-Path $fixture $Relative
    [IO.File]::WriteAllText($path, [IO.File]::ReadAllText($path) + $Text, $utf8)
}
function Copy-RepositoryFile([string]$Relative) {
    $target = Join-Path $fixture $Relative
    $parent = Split-Path -Parent $target
    if (-not (Test-Path -LiteralPath $parent)) { [void][IO.Directory]::CreateDirectory($parent) }
    [IO.File]::WriteAllBytes($target, [IO.File]::ReadAllBytes((Join-Path $repository $Relative)))
}
function Invoke-FixtureGit([string[]]$Arguments) {
    $output = & git -C $fixture @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Fixture Git failed: $($Arguments -join ' ')" }
    return (([string[]]@($output)) -join [char]10).Trim()
}
function Save-FixtureCommit([string]$Message) {
    [void](Invoke-FixtureGit @('add', '--all', '--', '.'))
    [void](Invoke-FixtureGit @('commit', '--quiet', '-m', $Message))
    return Invoke-FixtureGit @('rev-parse', 'HEAD')
}
function Get-FixtureTree([string]$Commit) { return Invoke-FixtureGit @('rev-parse', "$Commit^{tree}") }
function Select-FixtureCommit([string]$Commit) { [void](Invoke-FixtureGit @('checkout', '--quiet', '--detach', $Commit)) }
function Get-FixtureSnapshot {
    return [pscustomobject]@{
        head = Invoke-FixtureGit @('rev-parse', 'HEAD')
        status = Invoke-FixtureGit @('status', '--porcelain=v1', '--untracked-files=all')
        refs = Invoke-FixtureGit @('for-each-ref', '--format=%(refname) %(objectname)')
    } | ConvertTo-Json -Compress
}
function Get-DirectorySnapshot([string]$Directory) {
    return (@(Get-ChildItem -LiteralPath $Directory -Recurse -File -Force | Sort-Object FullName |
        ForEach-Object { $_.FullName + '=' + (Get-VerificationFileSha256 -Path $_.FullName) }) -join [char]10)
}
function Invoke-Capture([string]$Path, [string[]]$Arguments) {
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $pwsh
    $start.WorkingDirectory = $fixture
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    foreach ($argument in @('-NoLogo', '-NoProfile', '-File', $Path) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw "Could not start $Path" }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        return [pscustomobject]@{ exit_code = $process.ExitCode
            stdout = $stdoutTask.GetAwaiter().GetResult(); stderr = $stderrTask.GetAwaiter().GetResult() }
    } finally { $process.Dispose() }
}

# --- synthetic JUnit and Gradle evidence -----------------------------------
function New-JUnitRun([string]$Directory, [Collections.IDictionary]$Classes, [string[]]$Failing = @()) {
    [void][IO.Directory]::CreateDirectory($Directory)
    $files = [Collections.Generic.List[object]]::new()
    foreach ($className in $Classes.Keys) {
        $lines = [Collections.Generic.List[string]]::new()
        $lines.Add('<?xml version="1.0" encoding="UTF-8"?>')
        $lines.Add("<testsuite name=`"$className`" tests=`"$(@($Classes[$className]).Count)`">")
        foreach ($name in @($Classes[$className])) {
            if ($Failing -ccontains "$className::$name") {
                $lines.Add("  <testcase name=`"$name`" classname=`"$className`" time=`"0.001`">" +
                    '<failure message="fixture failure" type="java.lang.AssertionError">fixture</failure></testcase>')
            } else {
                $lines.Add("  <testcase name=`"$name`" classname=`"$className`" time=`"0.001`"/>")
            }
        }
        $lines.Add('</testsuite>')
        $path = Join-Path $Directory "TEST-$className.xml"
        Write-Text $path (($lines -join [char]10) + [char]10)
        $files.Add([ordered]@{ path = $path; sha256 = Get-VerificationFileSha256 -Path $path })
    }
    return ,([object[]]$files.ToArray())
}
function New-GradleEvidence([string]$Path, [object]$Selection, [object[]]$JUnitFiles, [bool]$DryRun) {
    $value = [ordered]@{
        schema_version = 1; producer_kind = 'GRADLE_TEST'
        selection_id = [string]$Selection.selection_id; selection = $Selection
        completion_state = 'COMPLETED'; completion_cause = $null
        command = 'gradlew.bat'; arguments = @('fixture'); working_directory = $fixture
        environment = [ordered]@{}; init_script = $null; init_script_sha256 = $null
        inner_exit_code = 0; stdout_log = $null; stderr_log = $null
        junit_files = $JUnitFiles; test_dry_run = $DryRun; duration_ms = 1.0
    }
    [void](Write-VerificationAtomicJson -Path $Path -Value $value)
    return $Path
}
function Invoke-FixtureUpdater([string[]]$Discovery, [string[]]$Selection = @()) {
    $quote = { param([string]$Value) "'" + $Value.Replace("'", "''") + "'" }
    $command = "& $(& $quote (Join-Path $fixture 'tools/agent/update-verification-junit-inventory.ps1')) " +
        "-InventoryPath $(& $quote (Join-Path $fixture $inventoryPath)) " +
        "-DiscoveryEvidencePath @($(($Discovery | ForEach-Object { & $quote $_ }) -join ','))"
    if ($Selection.Count -gt 0) {
        $command += " -SelectionEvidencePath @($(($Selection | ForEach-Object { & $quote $_ }) -join ','))"
    }
    & $pwsh -NoLogo -NoProfile -NonInteractive -Command $command
    if ($LASTEXITCODE -ne 0) { throw 'Fixture inventory updater failed.' }
}
function Set-FixtureRegistryPin {
    $pin = Get-VerificationCanonicalTextSha256 -Path (Join-Path $fixture $inventoryPath)
    $path = Join-Path $fixture $registryPath
    $text = [IO.File]::ReadAllText($path)
    $current = [string](@(($text | ConvertFrom-Json -Depth 100).catalogs |
        Where-Object catalog_id -CEQ 'junit_inventory')[0].sha256)
    if ($current -cne $pin) { [IO.File]::WriteAllText($path, $text.Replace($current, $pin), $utf8) }
    return $pin
}
function Get-CommitJson([string]$Commit, [string]$Path) {
    return (Invoke-FixtureGit @('show', "${Commit}:$Path")) | ConvertFrom-Json -Depth 100
}
function Copy-JsonValue([object]$Value) {
    return (ConvertTo-Json -InputObject $Value -Depth 100) | ConvertFrom-Json -Depth 100
}
function Get-ArgvSelection([object]$Node) {
    $argv = [string[]]@($Node.argv)
    for ($index = 0; $index -lt $argv.Count - 1; $index++) {
        if ($argv[$index] -ceq '-SelectionId') { return $argv[$index + 1] }
    }
    return $null
}

# --- synthetic sealed FINAL source runs ------------------------------------
function New-FixtureSourceRun {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$Commit,
        [Parameter(Mandatory)] [Collections.IDictionary]$Executed,
        [string[]]$Failing = @(),
        [scriptblock]$MutateSelection,
        [scriptblock]$MutateRecords
    )
    $runDirectory = Join-Path $outside "runs/$Name"
    $registry = Get-CommitJson $Commit $registryPath
    $inventory = Get-CommitJson $Commit $inventoryPath
    $plan = Resolve-VerificationRegistryPlan -Registry $registry -Profile 'FINAL' -Selection $null
    $producers = [Collections.Generic.List[object]]::new()
    $acceptance = [Collections.Generic.List[object]]::new()
    $diagnostics = [Collections.Generic.List[object]]::new()
    $structuredById = @{}
    foreach ($node in @($plan.nodes | Where-Object { [string]$_.node_kind -ceq 'PROCESS_PRODUCER' })) {
        $id = [string]$node.check_id
        $directory = Join-Path $runDirectory "checks/$id"
        [void][IO.Directory]::CreateDirectory($directory)
        $stdout = Join-Path $directory 'stdout.bin'
        $stderr = Join-Path $directory 'stderr.bin'
        [IO.File]::WriteAllBytes($stdout, [byte[]]@())
        [IO.File]::WriteAllBytes($stderr, [byte[]]@())
        $command = $node.command
        $declared = $(if ($command.Contains('structured_output')) { [string]$command['structured_output'] } else { '' })
        $structured = [ordered]@{ state = 'NOT_DECLARED'; path = $null; sha256 = $null; value = $null; cause = $null }
        if (-not [string]::IsNullOrWhiteSpace($declared)) {
            $path = Join-Path $directory $declared
            $selectionId = Get-ArgvSelection $node
            if ($null -ne $selectionId) {
                $selection = Copy-JsonValue (@($inventory.selections | Where-Object selection_id -CEQ $selectionId)[0])
                if ($null -ne $MutateSelection) { $selection = & $MutateSelection $selectionId $selection }
                $junit = New-JUnitRun (Join-Path $directory 'junit') $Executed[$selectionId] $Failing
                [void](New-GradleEvidence $path $selection $junit $false)
            } else {
                [void](Write-VerificationAtomicJson -Path $path -Value ([ordered]@{ fixture = $true; check_id = $id }))
            }
            $structured = [ordered]@{ state = 'PRESENT'; path = $path
                sha256 = Get-VerificationFileSha256 -Path $path; value = Read-VerificationJson -Path $path; cause = $null }
            $structuredById[$id] = $structured
        }
        $producers.Add([pscustomobject][ordered]@{
            check_id = $id; node_kind = 'PROCESS_PRODUCER'; command = 'pwsh'; arguments = @('-NoProfile')
            working_directory = $fixture; environment = [ordered]@{ inherit = $true; variables = [ordered]@{} }
            command_identity = Get-VerificationDeterministicHash -Value $id
            completion_state = 'COMPLETED'; exit_code = 0; completion_cause = $null
            stdout_log = $stdout; stderr_log = $stderr
            stdout_sha256 = Get-VerificationFileSha256 -Path $stdout
            stderr_sha256 = Get-VerificationFileSha256 -Path $stderr
            stdout_encoding = 'UTF8'; stderr_encoding = 'UTF8'; stdout_text = ''; stderr_text = ''
            structured_output = $structured; generated_evidence = @()
            started_at = '2026-01-01T00:00:00.0000000Z'; finished_at = '2026-01-01T00:00:01.0000000Z'
            duration_ms = 1.0
        })
    }
    foreach ($node in @($plan.nodes | Where-Object { [string]$_.node_kind -cne 'PROCESS_PRODUCER' })) {
        $id = [string]$node.check_id
        $class = [string]$node.contract_class
        if ($class -cin @('SEMANTIC', 'SAFETY', 'VERIFICATION_CORE')) {
            $record = [ordered]@{ check_id = $id; node_kind = [string]$node.node_kind; contract_class = $class
                coverage_state = 'COMPLETE'; status = 'CONTRACT_SATISFIED'
                command_identity = Get-VerificationDeterministicHash -Value $id; exit_code = 0; cause = $null
                evidence = @(); dependencies = [string[]]@($node.dependencies); duration_ms = 0.0 }
            if ($class -ceq 'SEMANTIC') { $record.semantic_domain = [string]$node.semantic_domain }
            if ([string]$node.output_adapter -ceq 'PROJECTION_JUNIT_SELECTION_V1') {
                $structured = $structuredById[[string]$node.producer_dependency]
                $projection = ConvertFrom-VerificationJUnitSelection -ProcessEvidence $structured.value `
                    -Selection $structured.value.selection -SemanticDomain ([string]$node.semantic_domain)
                $record.status = [string]$projection.outcome
                $record.coverage_state = [string]$projection.coverage_state
                $record.cause = $projection.cause
                $record.evidence = @([ordered]@{ name = 'STRUCTURED_RESULT'; state = 'PRESENT'
                    path = $structured.path; sha256 = $structured.sha256 })
            }
            $acceptance.Add([pscustomobject]$record)
        } else {
            $diagnostics.Add([pscustomobject][ordered]@{ check_id = $id; node_kind = [string]$node.node_kind; contract_class = $class
                outcome = 'DIAGNOSTIC_CLEAR'; command_identity = Get-VerificationDeterministicHash -Value $id
                exit_code = 0; cause = $null; evidence = @(); dependencies = [string[]]@($node.dependencies)
                duration_ms = 0.0 })
        }
    }
    if ($null -ne $MutateRecords) { & $MutateRecords $producers $acceptance $diagnostics }
    $required = [string[]]@(@($acceptance | ForEach-Object { [string]$_.check_id }) +
        [string[]]@($plan.missing_check_ids) | Sort-Object -CaseSensitive -Unique)
    $identity = New-VerificationTestEnvironmentIdentity -BaseCommit $null -BaseTree $null `
        -CandidateCommit $Commit -CandidateTree (Get-FixtureTree $Commit)
    # A [string] parameter turns $null into ''; a FINAL without a base records null.
    $identity.base_commit = $null
    $identity.base_tree = $null
    $report = New-VerificationAggregatedReport -RunId ('verification-' + [guid]::NewGuid().ToString('N')) `
        -Profile 'FINAL' -Identity $identity -ExecutionPlanHash ([string]$plan.execution_plan_hash) `
        -ProcessProducers ([object[]]$producers.ToArray()) -AcceptanceResults ([object[]]$acceptance.ToArray()) `
        -DiagnosticResults ([object[]]$diagnostics.ToArray()) -RequiredAcceptanceIds $required
    $path = Join-Path $runDirectory 'verification-result.json'
    [void](Write-VerificationAtomicJson -Path $path -Value $report)
    [void](Assert-VerificationJsonSchema -Value (Read-VerificationJson -Path $path) -SchemaPath $resultSchema)
    return [pscustomobject]@{ path = $path; sha256 = Get-VerificationFileSha256 -Path $path
        run_id = [string]$report.run_id; directory = $runDirectory
        selectionEvidence = [string[]]@($structuredById.Keys | Sort-Object | Where-Object {
            $null -ne $structuredById[$_].value.PSObject.Properties['junit_files'] } |
            ForEach-Object { [string]$structuredById[$_].path }) }
}
function New-ResultVariant([object]$Source, [string]$Name, [scriptblock]$Mutate, [switch]$KeepHash) {
    $report = Read-VerificationJson -Path $Source.path
    & $Mutate $report
    if (-not $KeepHash) { $report.result_hash = Get-VerificationResultIdentityHash -Report $report }
    $path = Join-Path $outside "variants/$Name/verification-result.json"
    [void](Write-VerificationAtomicJson -Path $path -Value $report)
    return [pscustomobject]@{ path = $path; sha256 = Get-VerificationFileSha256 -Path $path
        run_id = [string]$report.run_id; directory = Split-Path -Parent $path }
}
function Invoke-Reconciliation {
    param([string]$Action = 'VALIDATE', [string]$T, [string]$G, [string]$R, [object]$Source,
        [string[]]$Discovery, [string]$RunId, [string]$Sha256)
    $parameters = @{
        Action = $Action; RepositoryRoot = $fixture; ReviewedTechnicalCommit = $T
        SourceResultPath = $Source.path
        SourceRunId = $(if ($RunId) { $RunId } else { $Source.run_id })
        ExpectedSourceResultSha256 = $(if ($Sha256) { $Sha256 } else { $Source.sha256 })
        DiscoveryEvidencePath = $(if ($Discovery) { $Discovery } else { $script:Discovery })
    }
    if ($G) { $parameters.MechanismCommit = $G }
    if ($R) { $parameters.ReconciliationCommit = $R }
    return Invoke-VerificationCatalogReconciliation @parameters
}
function Assert-Rejected([object]$Run, [string]$Code, [string]$Label) {
    Assert-Case ([string]$Run.status -ceq 'REJECTED') "$Label was not rejected: $($Run.status)."
    Assert-Case ([string]$Run.code -ceq $Code) "$Label rejected as $($Run.code) instead of ${Code}: $($Run.message)"
    Assert-Case ($null -eq $Run.receipt) "$Label produced a receipt."
}

# --- the standard reconciliation edit (official updater + derived pin) ------
function Invoke-StandardReconciliationEdit([object]$Source, [string[]]$Discovery, [string]$Suffix = '') {
    Invoke-FixtureUpdater $Discovery $Source.selectionEvidence
    [void](Set-FixtureRegistryPin)
    Write-FixtureText "docs/validation/fixture_reconciliation$Suffix.md" "# Fixture reconciliation record`n"
    Write-FixtureText "geocedg/validation/fixture-x1/reconciliation$Suffix.json" "{ `"fixture`": true }`n"
    Add-FixtureText 'docs/roadmap/geocedg_roadmap.md' "Reconciled technical candidate$Suffix.`n"
}
function New-VariantCommit([string]$Parent, [string]$Name, [scriptblock]$Edit) {
    Select-FixtureCommit $Parent
    & $Edit
    $commit = Save-FixtureCommit "variant $Name"
    Select-FixtureCommit $script:R
    return $commit
}
function New-FinalReceipt([string]$Commit) {
    $tree = Get-FixtureTree $Commit
    $environment = New-VerificationTestEnvironmentIdentity -BaseCommit $null -BaseTree $null `
        -CandidateCommit $Commit -CandidateTree $tree -CompatibilitySignature ('4' * 64)
    $receipt = [ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-receipt.schema.json'
        schema_version = 3; receipt_kind = 'GEOCEDG_ACCEPTANCE_RECEIPT'; receipt_id = ('0' * 64)
        accepted_report_hash = ('1' * 64); base_commit = $null; base_tree = $null
        candidate_commit = $Commit; candidate_tree = $tree; execution_plan_hash = ('2' * 64)
        checker_identity_hash = ('3' * 64); environment_contract = $environment.environment_contract
        environment_compatibility_signature = $environment.environment_compatibility_signature
        environment_observation = $environment.environment_observation
        environment_observation_hash = $environment.environment_observation_hash
        input_identity_hash = ('5' * 64); accepted_profiles = @('FINAL')
        semantic_domains = @('MIXED', 'PRODUCT', 'SCIENTIFIC'); diagnostic_hashes = @('6' * 64)
        issued_at = '2026-01-01T00:00:00.0000000Z'
    }
    $receipt.receipt_id = & $receiptModule {
        param($Value)
        Get-VerificationDeterministicHash -Value (Get-VerificationReceiptIdentityPayload -Receipt $Value)
    } $receipt
    $path = Join-Path $outside 'receipts/final-acceptance-receipt.json'
    [void](Write-VerificationAtomicJson -Path $path -Value $receipt)
    return $path
}
function Write-ForgedReceipt([string]$Name, [scriptblock]$Mutate, [switch]$KeepId) {
    $receipt = Read-VerificationJson -Path $script:ReceiptPath
    & $Mutate $receipt
    if (-not $KeepId) { $receipt.receipt_id = Get-VerificationCatalogReconciliationReceiptId -Receipt $receipt }
    $path = Join-Path $outside "receipts/forged-$Name.json"
    [void](Write-VerificationAtomicJson -Path $path -Value $receipt)
    return $path
}
function Invoke-Closeout([string[]]$Arguments) {
    return Invoke-Capture $closeout (@('-Action', 'INSPECT', '-RepositoryRoot', $fixture) + $Arguments)
}
function Get-CliArguments([string]$T, [string]$G, [string]$R, [object]$Source, [string]$ReceiptPath) {
    $arguments = @('-Action', 'VALIDATE', '-RepositoryRoot', $fixture, '-ReviewedTechnicalCommit', $T,
        '-SourceResultPath', $Source.path, '-SourceRunId', $Source.run_id,
        '-ExpectedSourceResultSha256', $Source.sha256,
        '-DiscoveryEvidencePath', ($script:Discovery -join [IO.Path]::PathSeparator))
    if ($G) { $arguments += @('-MechanismCommit', $G) }
    if ($R) { $arguments += @('-ReconciliationCommit', $R) }
    if ($ReceiptPath) { $arguments += @('-ReceiptPath', $ReceiptPath) }
    return [string[]]$arguments
}

try {
    # ---------------------------------------------------------------------
    # Fixture: T (stale final.shared), G (mechanism adoption), R (derived fix)
    # ---------------------------------------------------------------------
    [void](Invoke-FixtureGit @('init', '--quiet'))
    foreach ($setting in @(@('user.name', 'Fixture'), @('user.email', 'fixture@example.invalid'),
            @('core.autocrlf', 'false'), @('core.safecrlf', 'false'))) {
        [void](Invoke-FixtureGit (@('config') + $setting))
    }
    $registryText = [IO.File]::ReadAllText((Join-Path $repository $registryPath))
    $realRegistry = $registryText | ConvertFrom-Json -Depth 100
    $realPlan = Resolve-VerificationRegistryPlan -Registry $realRegistry -Profile 'FINAL' -Selection $null
    $finalSelections = [ordered]@{}
    foreach ($node in @($realPlan.nodes | Where-Object { [string]$_.node_kind -ceq 'PROCESS_PRODUCER' })) {
        $selectionId = Get-ArgvSelection $node
        if ($null -ne $selectionId) {
            $finalSelections[$selectionId] = $(if ([string]$node.check_id -clike 'junit.shared.*') { 'shared' } else { 'desktop' })
        }
    }
    Assert-Case ($finalSelections.Contains('final.shared')) 'The FINAL plan has no final.shared selection.'
    $baseCohort = [ordered]@{
        'org.fixture.AlphaTest' = @('first()', 'second()'); 'org.fixture.BetaTest' = @('third()') }
    $executedShared = [ordered]@{
        'org.fixture.AlphaTest' = @('first()', 'second()'); 'org.fixture.BetaTest' = @('third()')
        'org.fixture.GammaTest' = @('added()') }
    $executed = [ordered]@{}
    $desktopDiscovery = [ordered]@{}
    $ordinal = 0
    foreach ($selectionId in $finalSelections.Keys) {
        if ($finalSelections[$selectionId] -ceq 'shared') { $executed[$selectionId] = $executedShared; continue }
        $ordinal++
        $className = "org.fixture.desktop.Desktop${ordinal}Test"
        $executed[$selectionId] = [ordered]@{ $className = @('opens()', 'closes()') }
        $desktopDiscovery[$className] = @('opens()', 'closes()')
    }

    foreach ($path in @('tools/agent/verification-io.psm1', 'tools/agent/verification-junit.psm1',
            'tools/agent/verification-registry.psm1', 'tools/agent/repository-input-identity.ps1',
            'tools/agent/evidence-integrity.ps1', 'tools/agent/update-verification-junit-inventory.ps1',
            'tools/agent/verification-receipt.psm1', 'tools/agent/verification-process-projections.psm1',
            'tools/agent/verification-supervisor.psm1', 'tools/agent/verify.ps1',
            'geocedg/specs/operations/verification-registry.schema.json',
            'geocedg/specs/operations/verification-result.schema.json',
            'geocedg/specs/operations/verification-receipt.schema.json',
            'geocedg/specs/operations/verification-static-contracts.json')) {
        Copy-RepositoryFile $path
    }
    Write-FixtureText 'tools/agent/phase-closeout.ps1' ([IO.File]::ReadAllText($closeout) + "`n# fixture technical candidate`n")
    Write-FixtureText '.gitignore' "/artifacts/*`n"
    Write-FixtureText 'README.md' "Fixture repository.`n"
    Write-FixtureText 'source/build.gradle' "// fixture build`n"
    Write-FixtureText 'source/shared/common-jre/src/main/java/org/fixture/Product.java' "class Product {}`n"
    Write-FixtureText 'source/shared/common-jre/src/test/java/org/fixture/AlphaTest.java' "class AlphaTest {}`n"
    Write-FixtureText 'geocedg/validation/fixture-reference/reference.json' "{ `"reference`": 1 }`n"
    Write-FixtureText 'tools/bootstrap/fixture.ps1' "# fixture toolchain`n"
    Write-FixtureText 'docs/roadmap/geocedg_roadmap.md' "# Fixture roadmap`n"
    Write-FixtureText 'docs/developer/geocedg_developer_guide.md' "# Fixture developer guide`n"
    Write-FixtureText 'docs/validation/fixture_candidate_report.md' "# Frozen candidate report`n"
    Write-FixtureText 'docs/adr/0001-fixture.md' "# ADR 0001`n"
    Write-FixtureText 'geocedg/specs/operations/verification-levels.md' "# Verification levels (fixture)`n"
    Write-FixtureText '.github/prompts/canonical/verification.prompt.md' "# Verification prompt (fixture)`n"

    $pairs = [Collections.Generic.List[object]]::new()
    $pairs.Add(@('discovery.shared', 'shared'))
    $pairs.Add(@('discovery.desktop', 'desktop'))
    foreach ($selectionId in $finalSelections.Keys) { $pairs.Add(@($selectionId, $finalSelections[$selectionId])) }
    $templateSelections = foreach ($pair in $pairs) {
        $task = $(if ($pair[1] -ceq 'shared') { ':shared:common-jre:test' } else { ':desktop:desktop:test' })
        [ordered]@{ selection_id = $pair[0]; module = $pair[1]; gradle_task = $task; test_filters = @()
            excluded_test_filters = @(); effective_arguments = @($task, '--rerun-tasks')
            expected_identity_count = 1; expected_identities_sha256 = ('0' * 64)
            not_applicable_allowlist = @(); input_identities = @() }
    }
    $template = [ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-junit-inventory.schema.json'
        schema_version = 1; catalog_id = 'junit_inventory'; identity_scheme = 'module-class-display-occurrence-v2'
        base_commit = $null; base_tree = $null; generated_at = $null
        modules = @(
            [ordered]@{ module = 'shared'; gradle_task = ':shared:common-jre:test'; discovery_evidence_sha256 = ('0' * 64)
                discovered_identity_count = 0; discovered_identities_sha256 = ('0' * 64) },
            [ordered]@{ module = 'desktop'; gradle_task = ':desktop:desktop:test'; discovery_evidence_sha256 = ('0' * 64)
                discovered_identity_count = 0; discovered_identities_sha256 = ('0' * 64) })
        selections = [object[]]@($templateSelections)
    }
    [void](Write-VerificationAtomicJson -Path (Join-Path $fixture $inventoryPath) -Value $template)
    $templateInventory = Read-VerificationJson -Path (Join-Path $fixture $inventoryPath)
    $selectionOf = { param([string]$Id) @($templateInventory.selections | Where-Object selection_id -CEQ $Id)[0] }
    $discoveryShared = New-GradleEvidence (Join-Path $outside 'discovery/shared.json') (& $selectionOf 'discovery.shared') `
        (New-JUnitRun (Join-Path $outside 'discovery/shared') $executedShared) $true
    $discoveryDesktop = New-GradleEvidence (Join-Path $outside 'discovery/desktop.json') (& $selectionOf 'discovery.desktop') `
        (New-JUnitRun (Join-Path $outside 'discovery/desktop') $desktopDiscovery) $true
    $script:Discovery = [string[]]@($discoveryShared, $discoveryDesktop)
    $baseEvidence = foreach ($selectionId in $finalSelections.Keys) {
        $classes = $(if ($selectionId -ceq 'final.shared') { $baseCohort } else { $executed[$selectionId] })
        New-GradleEvidence (Join-Path $outside "base/$selectionId.json") (& $selectionOf $selectionId) `
            (New-JUnitRun (Join-Path $outside "base/$selectionId") $classes) $false
    }
    Invoke-FixtureUpdater $script:Discovery ([string[]]@($baseEvidence))
    $realPin = [string](@($realRegistry.catalogs | Where-Object catalog_id -CEQ 'junit_inventory')[0].sha256)
    Write-FixtureText $registryPath $registryText
    Assert-Case ((Set-FixtureRegistryPin) -cne $realPin) 'The fixture inventory pin collides with the real one.'
    $T = Save-FixtureCommit 'technical candidate T'
    $script:T = $T
    $main = New-FixtureSourceRun -Name 'main' -Commit $T -Executed $executed
    $mainSnapshot = Get-DirectorySnapshot $main.directory

    foreach ($path in @('geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json',
            'tools/agent/reconcile-verification-catalog.ps1',
            'tools/agent/tests/verification-catalog-reconciliation.Tests.ps1',
            'tools/agent/verification-catalog-reconciliation.psm1')) {
        Copy-RepositoryFile $path
    }
    Copy-RepositoryFile 'tools/agent/phase-closeout.ps1'
    Add-FixtureText 'geocedg/specs/operations/verification-levels.md' "11.3 post-run catalog reconciliation.`n"
    Add-FixtureText '.github/prompts/canonical/verification.prompt.md' "See verification levels 11.3.`n"
    Write-FixtureText 'docs/adr/0030-fixture-reconciliation.md' "# ADR 0030 (fixture)`n"
    Add-FixtureText 'docs/developer/geocedg_developer_guide.md' "Reconciliation route.`n"
    $G = Save-FixtureCommit 'mechanism adoption G'
    $script:G = $G

    Invoke-Case 'ANALYZE establishes eligibility read-only before the reconciliation commit exists' {
        $before = Get-FixtureSnapshot
        $run = Invoke-Reconciliation -Action ANALYZE -T $T -G $G -Source $main
        Assert-Case ([string]$run.status -ceq 'ELIGIBLE') "ANALYZE was not eligible: $($run.code) $($run.message)"
        Assert-Case ((@($run.analysis.sourceUntrustedChecks) -join ',') -ceq 'junit.shared.final.semantic') 'Wrong stale check.'
        Assert-Case ($run.analysis.sourceExpectedIdentityCount -eq 3 -and
            $run.analysis.executedIdentityCount -eq 4) 'ANALYZE did not re-derive the executed cohort.'
        Assert-Case ((Get-FixtureSnapshot) -ceq $before) 'ANALYZE changed the fixture repository.'
        $script:Proposed = [string]$run.analysis.proposedInventoryHash
    }

    Invoke-StandardReconciliationEdit $main $script:Discovery
    $R = Save-FixtureCommit 'reconciliation R'
    $script:R = $R

    Invoke-Case 'the committed catalog is the exact official derivation proposed by ANALYZE' {
        Assert-Case ((Get-VerificationCanonicalTextSha256 -Path (Join-Path $fixture $inventoryPath)) -ceq
            $script:Proposed) 'R inventory differs from the ANALYZE proposal.'
        $changed = Invoke-FixtureGit @('diff', '--name-only', $G, $R)
        Assert-Case (($changed -split "`n" | Sort-Object) -join ',' -ceq (@(
            'docs/roadmap/geocedg_roadmap.md', 'docs/validation/fixture_reconciliation.md',
            'geocedg/specs/operations/verification-junit-inventory.json',
            'geocedg/specs/operations/verification-registry.json',
            'geocedg/validation/fixture-x1/reconciliation.json') -join ',')) "Unexpected G..R delta: $changed"
    }

    Invoke-Case 'VALIDATE accepts the bootstrap chain and composes a separate reconciliation receipt' {
        $run = Invoke-Reconciliation -T $T -G $G -R $R -Source $main
        Assert-Case ([string]$run.status -ceq 'ACCEPTED') "VALIDATE rejected: $($run.code) $($run.message)"
        $receipt = $run.receipt
        [void](Assert-VerificationJsonSchema -Value $receipt -SchemaPath $receiptSchema)
        Assert-Case ([bool](Assert-VerificationCatalogReconciliationReceipt -Receipt $receipt)) 'Receipt self-check failed.'
        Assert-Case ($receipt.receipt_kind -ceq 'GEOCEDG_POST_RUN_CATALOG_RECONCILIATION' -and
            $receipt.bootstrapAdoption -and $receipt.mechanismCommit -ceq $G -and
            $receipt.reviewedTechnicalCommit -ceq $T -and $receipt.reconciliationCommit -ceq $R) 'Receipt binds wrong commits.'
        Assert-Case ($receipt.sourceVerificationRun -ceq $main.run_id -and
            $receipt.sourceVerificationResultFileSha256 -ceq $main.sha256 -and
            $receipt.sourceVerificationStatus -ceq 'REJECTED_VERIFICATION_CORE / UNTRUSTED' -and
            -not $receipt.sourceRunReinterpreted) 'Receipt does not preserve the historical source run.'
        Assert-Case ($receipt.sourceExpectedIdentityCount -eq 3 -and $receipt.executedIdentityCount -eq 4 -and
            $receipt.reconciledExpectedIdentityCount -eq 4 -and
            $receipt.reconciledExpectedIdentitiesSha256 -ceq $receipt.executedIdentitiesSha256) 'Receipt counts differ.'
        Assert-Case ($receipt.counterfactualProjection.sourceSelectionCause -ceq $staleCause -and
            $receipt.counterfactualProjection.reconciledSelectionOutcome -ceq 'CONTRACT_SATISFIED') 'Counterfactual differs.'
        Assert-Case (-not $receipt.heavyCampaignRerun -and $receipt.technicalAcceptanceEligible -and
            $receipt.reconciliationStatus -ceq 'ACCEPTED' -and
            $receipt.reconciledExecutionPlanHash -ceq $receipt.sourceExecutionPlanHash) 'Receipt claims differ.'
        Assert-Case (@($receipt.otherJunitSelections).Count -eq ($finalSelections.Count - 2)) `
            'Every other JUnit acceptance selection must be re-projected.'
        Assert-Case ($receipt.registryPinAfter -ceq $receipt.inventoryHashAfter -and
            $receipt.registryPinBefore -ceq $receipt.inventoryHashBefore) 'Pins are not the canonical catalog hashes.'
        $script:FirstReceipt = $receipt
    }

    Invoke-Case 'validation is deterministic across repeated runs' {
        $second = Invoke-Reconciliation -T $T -G $G -R $R -Source $main
        Assert-Case ([string]$second.status -ceq 'ACCEPTED') 'Second validation rejected.'
        Assert-Case ($second.receipt.receipt_id -ceq $script:FirstReceipt.receipt_id) 'Receipt identity is not deterministic.'
        Assert-Case ((ConvertTo-VerificationCanonicalJson -Value $second.receipt -ExcludeProperties @('issued_at')) -ceq
            (ConvertTo-VerificationCanonicalJson -Value $script:FirstReceipt -ExcludeProperties @('issued_at'))) `
            'Receipt content is not deterministic.'
    }

    Invoke-Case 'the CLI writes identical receipts outside the source run and outside tracked Git' {
        $before = Get-FixtureSnapshot
        $external = Join-Path $outside 'receipts/receipt-1.json'
        $ignored = Join-Path $fixture 'artifacts/receipt-2.json'
        $first = Invoke-Capture $cli (Get-CliArguments $T $G $R $main $external)
        $second = Invoke-Capture $cli (Get-CliArguments $T $G $R $main $ignored)
        Assert-Case ($first.exit_code -eq 0 -and $second.exit_code -eq 0) "CLI validation failed: $($first.stderr) $($second.stderr)"
        $one = Read-VerificationJson -Path $external
        $two = Read-VerificationJson -Path $ignored
        Assert-Case ($one.receipt_id -ceq $two.receipt_id -and $one.receipt_id -ceq $script:FirstReceipt.receipt_id) `
            'CLI receipts are not identical.'
        $summary = $first.stdout | ConvertFrom-Json -Depth 20
        Assert-Case (-not $summary.source_run_written -and -not $summary.final_receipt_issued -and
            -not $summary.heavy_campaign_run) 'CLI summary claims a forbidden operation.'
        Assert-Case ((Get-FixtureSnapshot) -ceq $before) 'CLI validation changed the fixture repository.'
        $script:ReceiptPath = $external
    }

    Invoke-Case 'CLI output locations are fail-closed' {
        $cases = @(
            (Join-Path $main.directory 'reconciliation-receipt.json'),
            (Join-Path $fixture 'receipt.json'),
            $script:ReceiptPath)
        foreach ($target in $cases) {
            $existed = Test-Path -LiteralPath $target
            $result = Invoke-Capture $cli (Get-CliArguments $T $G $R $main $target)
            Assert-Case ($result.exit_code -ne 0) "CLI accepted an unsafe receipt location: $target"
            Assert-Case ($existed -or -not (Test-Path -LiteralPath $target)) "Rejected output was created: $target"
        }
        $analyze = Invoke-Capture $cli @('-Action', 'ANALYZE', '-RepositoryRoot', $fixture,
            '-ReviewedTechnicalCommit', $T, '-SourceResultPath', $main.path, '-SourceRunId', $main.run_id,
            '-ExpectedSourceResultSha256', $main.sha256,
            '-DiscoveryEvidencePath', ($script:Discovery -join [IO.Path]::PathSeparator),
            '-ReceiptPath', (Join-Path $outside 'receipts/analyze.json'))
        Assert-Case ($analyze.exit_code -ne 0) 'ANALYZE accepted a receipt output.'
    }

    Invoke-Case 'receipt generation requires a clean checkout of exactly R' {
        Select-FixtureCommit $G
        $wrong = Invoke-Capture $cli (Get-CliArguments $T $G $R $main (Join-Path $outside 'receipts/at-g.json'))
        Select-FixtureCommit $R
        Assert-Case ($wrong.exit_code -eq 3 -and
            ($wrong.stdout | ConvertFrom-Json).rejection_code -ceq 'CHECKOUT_NOT_RECONCILIATION_COMMIT') `
            'VALIDATE accepted a checkout other than R.'
        Write-FixtureText 'untracked.txt' "dirty`n"
        try {
            $dirty = Invoke-Capture $cli (Get-CliArguments $T $G $R $main (Join-Path $outside 'receipts/dirty.json'))
        } finally { Remove-Item -LiteralPath (Join-Path $fixture 'untracked.txt') -Force }
        Assert-Case ($dirty.exit_code -eq 3 -and
            ($dirty.stdout | ConvertFrom-Json).rejection_code -ceq 'CHECKOUT_NOT_RECONCILIATION_COMMIT') `
            'VALIDATE accepted a dirty checkout.'
        Assert-Case (-not (Test-Path -LiteralPath (Join-Path $outside 'receipts/at-g.json')) -and
            -not (Test-Path -LiteralPath (Join-Path $outside 'receipts/dirty.json'))) 'A rejected validation wrote a receipt.'
    }

    Invoke-Case 'source identity negatives fail closed' {
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $main `
            -RunId 'verification-00000000000000000000000000000000') 'WRONG_SOURCE_RUN' 'wrong run'
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $main -Sha256 ('a' * 64)) `
            'SOURCE_RESULT_MODIFIED' 'wrong result file hash'
        $tampered = New-ResultVariant $main 'tampered-verdict' { param($r) $r.coverage_verdict = 'INCOMPLETE' } -KeepHash
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $tampered) 'SOURCE_RESULT_MODIFIED' 'tampered result'
        Assert-Rejected (Invoke-Reconciliation -T $G -R $R -Source $main) 'WRONG_SOURCE_CANDIDATE' 'wrong candidate'
        $plan = New-ResultVariant $main 'plan' { param($r) $r.execution_plan_hash = ('a' * 64) }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $plan) 'SOURCE_PLAN_MISMATCH' 'foreign plan'
    }

    Invoke-Case 'incomplete or failed source campaigns fail closed' {
        $interrupted = New-ResultVariant $main 'interrupted' { param($r) $r.run_state = 'RUN_INTERRUPTED' }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $interrupted) 'SOURCE_RUN_INCOMPLETE' 'interrupted'
        $exit = New-ResultVariant $main 'producer-exit' {
            param($r) @($r.process_producers | Where-Object check_id -CEQ 'compile.shared.producer')[0].exit_code = 1 }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $exit) 'SOURCE_RUN_INCOMPLETE' 'failed producer'
        $missing = New-ResultVariant $main 'producer-missing' {
            param($r) $r.process_producers = @($r.process_producers | Where-Object check_id -CNE 'compile.shared.producer') }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $missing) 'SOURCE_RUN_INCOMPLETE' 'missing producer'
        $notRun = New-FixtureSourceRun -Name 'not-run' -Commit $T -Executed $executed -MutateRecords {
            param($p, $a, $d)
            $record = @($a | Where-Object { $_.check_id -ceq 'infra.io' })[0]
            $record.coverage_state = 'INCOMPLETE'
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $notRun) 'SOURCE_RUN_INCOMPLETE' 'incomplete coverage'
        $failing = New-FixtureSourceRun -Name 'failing' -Commit $T -Executed $executed `
            -Failing @('org.fixture.GammaTest::added()')
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $failing) 'SOURCE_TEST_FAILURE' 'stale selection masking a failure'
    }

    Invoke-Case 'only the narrow stale-inventory rejection is reconcilable' {
        $violation = New-FixtureSourceRun -Name 'violation' -Commit $T -Executed $executed -MutateRecords {
            param($p, $a, $d)
            $record = @($a | Where-Object { $_.check_id -ceq 'infra.io' })[0]
            $record.status = 'CONTRACT_VIOLATED'; $record.cause = 'fixture violation'
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $violation) 'SEMANTIC_OR_SAFETY_VIOLATION' 'violation'
        $other = New-FixtureSourceRun -Name 'other-untrusted' -Commit $T -Executed $executed -MutateRecords {
            param($p, $a, $d)
            $record = @($a | Where-Object { $_.check_id -ceq 'infra.io' })[0]
            $record.status = 'EVIDENCE_UNTRUSTED'; $record.coverage_state = 'UNTRUSTED'; $record.cause = 'fixture'
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $other) 'UNSUPPORTED_REJECTION' 'second gate'
        $twoStale = [ordered]@{}
        foreach ($key in $executed.Keys) { $twoStale[$key] = $executed[$key] }
        Assert-Case ($finalSelections.Contains('final.desktop')) 'The FINAL plan has no final.desktop selection.'
        $twoStale['final.desktop'] = [ordered]@{ 'org.fixture.desktop.ExtraTest' = @('extra()') }
        $two = New-FixtureSourceRun -Name 'two-stale' -Commit $T -Executed $twoStale
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $two) 'UNSUPPORTED_REJECTION' 'two stale selections'
        $acceptedExecuted = [ordered]@{}
        foreach ($key in $executed.Keys) { $acceptedExecuted[$key] = $executed[$key] }
        $acceptedExecuted['final.shared'] = $baseCohort
        $accepted = New-FixtureSourceRun -Name 'accepted' -Commit $T -Executed $acceptedExecuted
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $accepted) 'UNSUPPORTED_SOURCE_STATUS' 'accepted source'
        $embedded = New-FixtureSourceRun -Name 'embedded-selection' -Commit $T -Executed $executed -MutateSelection {
            param($id, $selection)
            if ($id -ceq 'final.shared') { $selection.test_filters = @('org.fixture.*') }
            return $selection
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $embedded) 'SELECTION_CONTRACT_CHANGED' 'changed selection'
    }

    Invoke-Case 'missing or mutated source evidence fails closed' {
        $absent = New-ResultVariant $main 'log-absent' {
            param($r) @($r.process_producers | Where-Object check_id -CEQ 'compile.shared.producer')[0].stdout_log =
                (Join-Path $outside 'absent/stdout.bin') }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $absent) 'SOURCE_EVIDENCE_MISSING' 'absent log'
        $hash = New-ResultVariant $main 'log-hash' {
            param($r) @($r.process_producers | Where-Object check_id -CEQ 'compile.shared.producer')[0].stdout_sha256 = ('b' * 64) }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $hash) 'SOURCE_EVIDENCE_MUTATED' 'log hash'
        $mutated = New-FixtureSourceRun -Name 'mutated-xml' -Commit $T -Executed $executed
        $xml = @(Get-ChildItem -LiteralPath (Join-Path $mutated.directory 'checks/junit.shared.final.producer/junit') -File)[0].FullName
        [IO.File]::AppendAllText($xml, '<!-- tamper -->', $utf8)
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $mutated) 'SOURCE_EVIDENCE_MUTATED' 'JUnit XML tamper'
    }

    Invoke-Case 'discovery evidence must authenticate against the tracked inventory of T' {
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $main -Discovery @($discoveryShared)) `
            'DISCOVERY_EVIDENCE_UNAUTHENTICATED' 'missing module evidence'
        $copy = Join-Path $outside 'discovery/desktop-copy.json'
        Write-Text $copy ([IO.File]::ReadAllText($discoveryDesktop) + ' ')
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $main -Discovery @($discoveryShared, $copy)) `
            'DISCOVERY_EVIDENCE_UNAUTHENTICATED' 'modified discovery evidence'
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $R -Source $main `
                -Discovery @($discoveryShared, $discoveryDesktop, $copy)) `
            'DISCOVERY_EVIDENCE_UNAUTHENTICATED' 'unmatched discovery evidence'
    }

    Invoke-Case 'hand-entered, stale or non-derivable catalogs fail closed' {
        $handHash = New-VariantCommit $G 'hand-hash' {
            Invoke-StandardReconciliationEdit $main $script:Discovery
            $path = Join-Path $fixture $inventoryPath
            $inventory = Read-VerificationJson -Path $path
            @($inventory.selections | Where-Object selection_id -CEQ 'final.shared')[0].expected_identities_sha256 = ('c' * 64)
            Write-Text $path ((ConvertTo-Json $inventory -Depth 100).Replace("`r`n", "`n") + "`n")
            [void](Set-FixtureRegistryPin)
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $handHash -Source $main) 'CATALOG_NOT_DERIVABLE' 'hand-entered hash'
        $ghost = New-VariantCommit $G 'unexecuted-identity' {
            Invoke-StandardReconciliationEdit $main $script:Discovery
            $path = Join-Path $fixture $inventoryPath
            $inventory = Read-VerificationJson -Path $path
            $selection = @($inventory.selections | Where-Object selection_id -CEQ 'final.shared')[0]
            $identities = [string[]]@('shared::org.fixture.AlphaTest::first()::invocation[1/1]',
                'shared::org.fixture.AlphaTest::second()::invocation[1/1]',
                'shared::org.fixture.BetaTest::third()::invocation[1/1]',
                'shared::org.fixture.GammaTest::added()::invocation[1/1]',
                'shared::org.fixture.GhostTest::ghost()::invocation[1/1]')
            $selection.expected_identity_count = $identities.Count
            $selection.expected_identities_sha256 = Get-VerificationDeterministicHash -Value $identities
            Write-Text $path ((ConvertTo-Json $inventory -Depth 100).Replace("`r`n", "`n") + "`n")
            [void](Set-FixtureRegistryPin)
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $ghost -Source $main) 'CATALOG_NOT_DERIVABLE' 'unexecuted identity'
        $omitted = New-VariantCommit $G 'omitted' {
            Write-FixtureText 'docs/validation/fixture_reconciliation.md' "# Record without catalog`n"
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $omitted -Source $main) 'CATALOG_NOT_DERIVABLE' 'stale catalog kept'
        $pin = New-VariantCommit $G 'stale-pin' {
            Invoke-FixtureUpdater $script:Discovery $main.selectionEvidence
            Write-FixtureText 'docs/validation/fixture_reconciliation.md' "# Record`n"
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $pin -Source $main) 'REGISTRY_PIN_MISMATCH' 'stale pin'
        $plan = New-VariantCommit $G 'plan-change' {
            Invoke-StandardReconciliationEdit $main $script:Discovery
            $path = Join-Path $fixture $registryPath
            $text = [IO.File]::ReadAllText($path)
            Assert-Case ($text.Contains('"junit.shared.final.producer-v1"')) 'Registry fixture lacks the producer identity.'
            [IO.File]::WriteAllText($path, $text.Replace('"junit.shared.final.producer-v1"', '"junit.shared.final.producer-v2"'), $utf8)
        }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $plan -Source $main) 'EXECUTION_PLAN_CHANGED' 'plan change'
    }

    Invoke-Case 'execution-authority and bounded-delta violations fail closed' {
        $expectations = [ordered]@{
            'PRODUCT_PAYLOAD_CHANGED' = { Add-FixtureText 'source/shared/common-jre/src/main/java/org/fixture/Product.java' "// x`n" }
            'SCIENTIFIC_TESTS_CHANGED' = { Add-FixtureText 'source/shared/common-jre/src/test/java/org/fixture/AlphaTest.java' "// x`n" }
            'BUILD_AUTHORITY_CHANGED' = { Add-FixtureText 'source/build.gradle' "// x`n" }
            'NUMERICAL_REFERENCE_CHANGED' = { Write-FixtureText 'geocedg/validation/fixture-reference/reference.json' "{ `"reference`": 2 }`n" }
            'TOOLCHAIN_CHANGED' = { Add-FixtureText 'tools/bootstrap/fixture.ps1' "# x`n" }
            'VERIFIER_EXECUTION_CHANGED' = { Add-FixtureText 'tools/agent/verification-supervisor.psm1' "# x`n" }
            'UPDATER_AUTHORITY_CHANGED' = { Add-FixtureText 'tools/agent/update-verification-junit-inventory.ps1' "# x`n" }
            'MECHANISM_CHANGED' = { Add-FixtureText 'tools/agent/verification-catalog-reconciliation.psm1' "# x`n" }
        }
        foreach ($code in $expectations.Keys) {
            $mutation = $expectations[$code]
            $variant = New-VariantCommit $G $code { Invoke-StandardReconciliationEdit $main $script:Discovery; & $mutation }
            Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $variant -Source $main) $code $code
        }
        $unpermitted = [ordered]@{
            'readme' = { Add-FixtureText 'README.md' "x`n" }
            'frozen-report' = { Add-FixtureText 'docs/validation/fixture_candidate_report.md' "PASS`n" }
            'adr-edit' = { Add-FixtureText 'docs/adr/0001-fixture.md' "x`n" }
            'input-directory-record' = { Write-FixtureText 'geocedg/validation/pre-g9b-d1/new-record.json' "{}`n" }
            'consumer-after-adoption' = { Add-FixtureText 'tools/agent/phase-closeout.ps1' "# x`n" }
            'policy-after-adoption' = { Add-FixtureText 'geocedg/specs/operations/verification-levels.md' "x`n" }
        }
        foreach ($name in $unpermitted.Keys) {
            $mutation = $unpermitted[$name]
            $variant = New-VariantCommit $G $name { Invoke-StandardReconciliationEdit $main $script:Discovery; & $mutation }
            Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $variant -Source $main) 'UNPERMITTED_DELTA' $name
        }
        $wideAdoption = New-VariantCommit $T 'wide-adoption' {
            foreach ($path in @('geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json',
                    'tools/agent/reconcile-verification-catalog.ps1',
                    'tools/agent/tests/verification-catalog-reconciliation.Tests.ps1',
                    'tools/agent/verification-catalog-reconciliation.psm1')) { Copy-RepositoryFile $path }
            Add-FixtureText 'README.md' "x`n"
        }
        $wideR = New-VariantCommit $wideAdoption 'wide-adoption-r' { Invoke-StandardReconciliationEdit $main $script:Discovery }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $wideAdoption -R $wideR -Source $main) 'UNPERMITTED_DELTA' 'wide adoption'
    }

    Invoke-Case 'ancestry and bootstrap structure are exact' {
        $grandchild = New-VariantCommit $R 'grandchild' { Add-FixtureText 'docs/roadmap/geocedg_roadmap.md' "x`n" }
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $grandchild -Source $main) 'ANCESTRY_INVALID' 'grandchild'
        $orphan = Invoke-FixtureGit @('commit-tree', (Get-FixtureTree $R), '-m', 'orphan')
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $orphan -Source $main) 'ANCESTRY_INVALID' 'orphan'
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R $G -Source $main) 'CATALOG_NOT_DERIVABLE' 'R equal to G'
        Assert-Rejected (Invoke-Reconciliation -T $T -R $R -Source $main) 'MECHANISM_CHANGED' 'ordinary use without adoption'
        Assert-Rejected (Invoke-Reconciliation -T $T -G $G -R 'HEAD' -Source $main) 'ANCESTRY_INVALID' 'symbolic R'
    }

    Invoke-Case 'the execution-authority comparator classifies every authority family' {
        $probe = {
            param([string]$Path, [string]$Status = 'M')
            & $reconciliationModule {
                param($probePath, $probeStatus)
                Get-ReconciliationAuthorityFlags ([object[]]@([pscustomobject]@{ path = $probePath; status = $probeStatus }))
            } $Path $Status
        }
        $expect = [ordered]@{
            'source/shared/common-jre/src/main/java/A.java' = 'productPayloadUnchanged'
            'source/desktop/desktop/src/main/java/B.java' = 'productPayloadUnchanged'
            'source/shared/common-jre/src/test/java/C.java' = 'scientificTestsUnchanged'
            'source/shared/common-jre/build.gradle.kts' = 'buildAuthorityUnchanged'
            'gradle/libs.versions.toml' = 'buildAuthorityUnchanged'
            'geocedg/validation/locus-v2/g7a/references.json' = 'numericalReferencesUnchanged'
            'python/check.py' = 'numericalReferencesUnchanged'
            'tools/release/build-windows-package.ps1' = 'toolchainUnchanged'
            'tools/agent/verify.ps1' = 'verifierExecutionUnchanged'
            'tools/agent/checks/gradle-test-evidence-producer.ps1' = 'verifierExecutionUnchanged'
            'geocedg/specs/operations/verification-static-contracts.json' = 'verifierExecutionUnchanged'
        }
        foreach ($path in $expect.Keys) {
            $result = & $probe $path
            Assert-Case (-not $result[$expect[$path]]) "Comparator missed $path"
            Assert-Case (@($result.Values | Where-Object { -not $_ }).Count -eq 1) "Comparator over-classified $path"
        }
        foreach ($path in @('docs/roadmap/geocedg_roadmap.md', 'tools/agent/phase-closeout.ps1',
                'tools/agent/verification-catalog-reconciliation.psm1', $inventoryPath, $registryPath)) {
            $result = & $probe $path
            Assert-Case (@($result.Values | Where-Object { -not $_ }).Count -eq 0) "Comparator rejected $path"
        }
        $added = & $probe 'geocedg/validation/pre-g9b-r3-x1/reconciliation.json' 'A'
        Assert-Case (@($added.Values | Where-Object { -not $_ }).Count -eq 0) 'Comparator rejected an added record.'
    }

    Invoke-Case 'closeout route B consumes only the exact T and R pair' {
        $before = Get-FixtureSnapshot
        $ok = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $script:ReceiptPath,
            '-ReconciliationCommit', $R, '-ApprovedCommit', $T)
        Assert-Case ($ok.exit_code -eq 0) "Route B rejected the exact pair: $($ok.stderr)"
        $payload = $ok.stdout | ConvertFrom-Json -Depth 20
        Assert-Case ($payload.identity_confirmed -and $payload.closeout_route -ceq 'POST_RUN_CATALOG_RECONCILIATION' -and
            $payload.reconciliation_commit -ceq $R -and $payload.candidate_commit -ceq $T -and
            $payload.reconciliation_revalidated -and -not $payload.verification_executed -and
            -not $payload.repository_mutated -and -not $payload.author_approval_recorded -and
            -not $payload.source_run_reinterpreted) 'Route B payload differs.'
        $negatives = [ordered]@{
            'wrong R' = @('-CandidateCommit', $T, '-ReceiptPath', $script:ReceiptPath, '-ReconciliationCommit', $G)
            'wrong T' = @('-CandidateCommit', $G, '-ReceiptPath', $script:ReceiptPath, '-ReconciliationCommit', $R)
            'approved R' = @('-CandidateCommit', $T, '-ReceiptPath', $script:ReceiptPath, '-ReconciliationCommit', $R,
                '-ApprovedCommit', $R)
        }
        foreach ($name in $negatives.Keys) {
            $result = Invoke-Closeout $negatives[$name]
            Assert-Case ($result.exit_code -eq 3) "Route B accepted $name."
        }
        $noR = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $script:ReceiptPath)
        Assert-Case ($noR.exit_code -ne 0) 'Route B accepted a receipt without the reconciliation commit.'
        $sourceAsReceipt = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $main.path)
        Assert-Case ($sourceAsReceipt.exit_code -ne 0) 'The historical source result was consumed as a receipt.'
        $sourceAsReceiptB = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $main.path, '-ReconciliationCommit', $R)
        Assert-Case ($sourceAsReceiptB.exit_code -ne 0) 'The historical source result was consumed with R.'
        Assert-Case ((Get-FixtureSnapshot) -ceq $before) 'Route B changed Git state.'
    }

    Invoke-Case 'closeout route B revalidates forged and tampered receipts' {
        $product = New-VariantCommit $G 'closeout-product' {
            Invoke-StandardReconciliationEdit $main $script:Discovery
            Add-FixtureText 'source/shared/common-jre/src/main/java/org/fixture/Product.java' "// x`n"
        }
        $forged = @{
            'product-delta' = @((Write-ForgedReceipt 'product' {
                param($r) $r.reconciliationCommit = $product; $r.reconciliationTree = (Get-FixtureTree $product) }), $product)
            'wrong-run' = @((Write-ForgedReceipt 'run' {
                param($r) $r.sourceVerificationRun = 'verification-11111111111111111111111111111111' }), $R)
            're-identified-tamper' = @((Write-ForgedReceipt 'count' {
                param($r) $r.reconciledExpectedIdentityCount = 5 }), $R)
        }
        foreach ($name in $forged.Keys) {
            $result = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $forged[$name][0],
                '-ReconciliationCommit', $forged[$name][1])
            Assert-Case ($result.exit_code -eq 3) "Route B accepted a forged receipt: $name"
        }
        $unidentified = Write-ForgedReceipt 'no-id' { param($r) $r.executedIdentityCount = 5 } -KeepId
        $result = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $unidentified, '-ReconciliationCommit', $R)
        Assert-Case ($result.exit_code -ne 0) 'Route B accepted a receipt with an invalid identity.'
        $schemaBreak = Write-ForgedReceipt 'schema' { param($r) $r.heavyCampaignRerun = $true }
        $result = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $schemaBreak, '-ReconciliationCommit', $R)
        Assert-Case ($result.exit_code -ne 0) 'Route B accepted a schema-invalid receipt.'
        $revalidated = Test-VerificationCatalogReconciliationReceipt -Receipt (Read-VerificationJson -Path $forged['wrong-run'][0]) `
            -RepositoryRoot $fixture -ReviewedTechnicalCommit $T -ReconciliationCommit $R
        Assert-Case (-not $revalidated.valid -and $revalidated.rejectionCode -ceq 'WRONG_SOURCE_RUN') 'Revalidation code differs.'
    }

    Invoke-Case 'closeout route A for an ordinary FINAL receipt is unchanged' {
        $finalReceipt = New-FinalReceipt $T
        $before = Get-FixtureSnapshot
        $result = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $finalReceipt, '-ApprovedCommit', $T)
        Assert-Case ($result.exit_code -eq 0) "Route A rejected an exact FINAL receipt: $($result.stderr)"
        $payload = $result.stdout | ConvertFrom-Json -Depth 20
        $names = [string[]]@($payload.PSObject.Properties.Name)
        Assert-Case (($names -join ',') -ceq (@('schema_version', 'operation', 'candidate_commit', 'candidate_tree',
            'receipt_id', 'receipt_path', 'approved_commit', 'identity_confirmed', 'mismatches',
            'verification_executed', 'repository_mutated', 'author_approval_recorded',
            'publication_performed') -join ',')) "Route A payload shape changed: $($names -join ',')"
        $withR = Invoke-Closeout @('-CandidateCommit', $T, '-ReceiptPath', $finalReceipt, '-ReconciliationCommit', $R)
        Assert-Case ($withR.exit_code -ne 0) 'Route A accepted a reconciliation commit.'
        $wrong = Invoke-Closeout @('-CandidateCommit', $G, '-ReceiptPath', $finalReceipt)
        Assert-Case ($wrong.exit_code -eq 3) 'Route A accepted a wrong candidate.'
        Assert-Case ((Get-FixtureSnapshot) -ceq $before) 'Route A changed Git state.'
    }

    Invoke-Case 'a later ordinary use requires the adopted mechanism unchanged' {
        Select-FixtureCommit $R
        Write-FixtureText 'source/shared/common-jre/src/test/java/org/fixture/DeltaTest.java' "class DeltaTest {}`n"
        $sharedTwo = [ordered]@{}
        foreach ($key in $executedShared.Keys) { $sharedTwo[$key] = $executedShared[$key] }
        $sharedTwo['org.fixture.DeltaTest'] = @('delta()')
        $templateTwo = Read-VerificationJson -Path (Join-Path $fixture $inventoryPath)
        $discoveryTwo = New-GradleEvidence (Join-Path $outside 'discovery/shared-two.json') `
            (@($templateTwo.selections | Where-Object selection_id -CEQ 'discovery.shared')[0]) `
            (New-JUnitRun (Join-Path $outside 'discovery/shared-two') $sharedTwo) $true
        $discovery2 = [string[]]@($discoveryTwo, $discoveryDesktop)
        Invoke-FixtureUpdater $discovery2
        [void](Set-FixtureRegistryPin)
        $T2 = Save-FixtureCommit 'technical candidate T2'
        $executedTwo = [ordered]@{}
        foreach ($key in $executed.Keys) { $executedTwo[$key] = $executed[$key] }
        $executedTwo['final.shared'] = $sharedTwo
        $second = New-FixtureSourceRun -Name 'second' -Commit $T2 -Executed $executedTwo
        Invoke-StandardReconciliationEdit $second $discovery2 '_2'
        $R2 = Save-FixtureCommit 'reconciliation R2'
        $script:R = $R2
        $run = Invoke-Reconciliation -T $T2 -R $R2 -Source $second -Discovery $discovery2
        Assert-Case ([string]$run.status -ceq 'ACCEPTED') "Ordinary use rejected: $($run.code) $($run.message)"
        Assert-Case (-not $run.receipt.bootstrapAdoption -and $null -eq $run.receipt.mechanismCommit -and
            $run.receipt.sourceExpectedIdentityCount -eq 4 -and $run.receipt.executedIdentityCount -eq 5) `
            'Ordinary receipt differs.'
        Assert-Case (@($run.receipt.delta.reviewedToMechanism).Count -eq 0) 'Ordinary receipt claims an adoption delta.'
        $mechanism = New-VariantCommit $T2 'ordinary-mechanism' {
            Invoke-StandardReconciliationEdit $second $discovery2 '_2'
            Add-FixtureText 'tools/agent/verification-catalog-reconciliation.psm1' "# x`n"
        }
        Assert-Rejected (Invoke-Reconciliation -T $T2 -R $mechanism -Source $second -Discovery $discovery2) `
            'MECHANISM_CHANGED' 'ordinary mechanism change'
        $consumer = New-VariantCommit $T2 'ordinary-consumer' {
            Invoke-StandardReconciliationEdit $second $discovery2 '_2'
            Add-FixtureText 'tools/agent/phase-closeout.ps1' "# x`n"
        }
        Assert-Rejected (Invoke-Reconciliation -T $T2 -R $consumer -Source $second -Discovery $discovery2) `
            'UNPERMITTED_DELTA' 'ordinary consumer change'
        $policy = New-VariantCommit $T2 'ordinary-policy' {
            Invoke-StandardReconciliationEdit $second $discovery2 '_2'
            Add-FixtureText 'geocedg/specs/operations/verification-levels.md' "x`n"
        }
        Assert-Rejected (Invoke-Reconciliation -T $T2 -R $policy -Source $second -Discovery $discovery2) `
            'UNPERMITTED_DELTA' 'ordinary policy change'
        Assert-Rejected (Invoke-Reconciliation -T $T2 -G $R -R $R2 -Source $second -Discovery $discovery2) `
            'ANCESTRY_INVALID' 'ordinary use presented as an adoption'
    }

    Invoke-Case 'the source run is never written and no FINAL receipt is created' {
        Assert-Case ((Get-DirectorySnapshot $main.directory) -ceq $mainSnapshot) 'The source run directory changed.'
        $receipts = @(Get-ChildItem -LiteralPath (Join-Path $outside 'runs') -Recurse -File |
            Where-Object { $_.Name -like '*receipt*' })
        Assert-Case ($receipts.Count -eq 0) 'A receipt was written inside a source run directory.'
        $report = Read-VerificationJson -Path $main.path
        Assert-Case ($report.acceptance_verdict -ceq 'REJECTED_VERIFICATION_CORE' -and
            $report.coverage_verdict -ceq 'UNTRUSTED') 'The historical classification changed.'
    }
} catch {
    Write-Host $_.ScriptStackTrace
    throw
} finally {
    Remove-Module $receiptModule -Force -ErrorAction SilentlyContinue
    Remove-Module $reconciliationModule -Force -ErrorAction SilentlyContinue
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-catalog-reconciliation') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else { throw "Unsafe catalog reconciliation fixture cleanup target: $resolved" }
}

Write-Host "verification-catalog-reconciliation.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

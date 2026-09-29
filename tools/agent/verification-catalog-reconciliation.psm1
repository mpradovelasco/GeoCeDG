#requires -Version 7.2

# GOV-R post-run deterministic catalog reconciliation (verification-levels.md
# section 11.3, ADR 0030). A read-only post-run consumer: it never writes the
# source run, never issues a FINAL receipt and never changes the heavy verifier.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-junit.psm1')
Import-Module (Join-Path $PSScriptRoot 'verification-registry.psm1')
. (Join-Path $PSScriptRoot 'repository-input-identity.ps1')

$script:ReceiptKind = 'GEOCEDG_POST_RUN_CATALOG_RECONCILIATION'
$script:MechanismVersion = 'post-run-catalog-reconciliation/v1'
$script:Reason = 'STALE_DERIVED_JUNIT_SELECTION_INVENTORY'
$script:StaleCause = 'Executed JUnit identities differ from the tracked selection inventory.'
$script:SourceStatus = 'REJECTED_VERIFICATION_CORE / UNTRUSTED'
$script:InventoryPath = 'geocedg/specs/operations/verification-junit-inventory.json'
$script:RegistryPath = 'geocedg/specs/operations/verification-registry.json'
$script:RegistrySchemaPath = 'geocedg/specs/operations/verification-registry.schema.json'
$script:ResultSchemaPath = 'geocedg/specs/operations/verification-result.schema.json'
$script:ReceiptSchemaPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot `
    '../../geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json'))
$script:ReceiptSchemaRelative = 'geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json'
# The reconciliation mechanism itself. An initial adoption may add exactly these
# paths; every later use requires them present and unchanged.
$script:MechanismPaths = [string[]]@(
    'geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json',
    'tools/agent/reconcile-verification-catalog.ps1',
    'tools/agent/tests/verification-catalog-reconciliation.Tests.ps1',
    'tools/agent/verification-catalog-reconciliation.psm1')
$script:ConsumerPaths = [string[]]@('tools/agent/phase-closeout.ps1')
$script:GovernancePaths = [string[]]@(
    '.github/prompts/canonical/verification.prompt.md',
    'geocedg/specs/operations/verification-levels.md')
# The official catalog derivation and the verifier modules that decide
# acceptance. They must be Git-identical between T and R.
$script:UpdaterPaths = [string[]]@(
    'tools/agent/update-verification-junit-inventory.ps1',
    'tools/agent/verification-io.psm1',
    'tools/agent/verification-junit.psm1')
$script:AcceptanceSemanticsPaths = [string[]]@(
    'tools/agent/verification-io.psm1',
    'tools/agent/verification-junit.psm1',
    'tools/agent/verification-process-projections.psm1',
    'tools/agent/verification-receipt.psm1',
    'tools/agent/verification-registry.psm1',
    'tools/agent/verification-supervisor.psm1',
    'tools/agent/verify.ps1',
    'geocedg/specs/operations/verification-receipt.schema.json',
    'geocedg/specs/operations/verification-registry.schema.json',
    'geocedg/specs/operations/verification-result.schema.json')

class GeoCeDGCatalogReconciliationRejection : System.Exception {
    [string]$Code
    GeoCeDGCatalogReconciliationRejection([string]$code, [string]$message) : base($message) {
        $this.Code = $code
    }
}

function Stop-CatalogReconciliation {
    param([Parameter(Mandatory)] [string]$Code, [Parameter(Mandatory)] [string]$Message)
    throw [GeoCeDGCatalogReconciliationRejection]::new($Code, "${Code}: $Message")
}

function Get-ReconciliationProperty {
    param([AllowNull()] [object]$Object, [Parameter(Mandatory)] [string]$Name)
    if ($null -eq $Object) { return $null }
    if ($Object -is [Collections.IDictionary]) {
        if ($Object.Contains($Name)) { return $Object[$Name] }
        return $null
    }
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) { return $null }
    return $property.Value
}

function Get-ReconciliationGitText {
    param([string]$Root, [string[]]$Arguments, [switch]$AllowFailure)
    $result = Invoke-GeoCeDGRepositoryIdentityGit $Root $Arguments -AllowFailure:$AllowFailure
    if ($AllowFailure -and $result.ExitCode -ne 0) { return $null }
    return (ConvertFrom-GeoCeDGRepositoryIdentityGit $result)
}

function Resolve-ReconciliationCommit {
    param([string]$Root, [string]$Commit, [string]$Role)
    if ([string]::IsNullOrWhiteSpace($Commit) -or
            $Commit -cnotmatch '^([0-9a-f]{40}|[0-9a-f]{64})$') {
        Stop-CatalogReconciliation 'ANCESTRY_INVALID' "$Role must be an exact full commit OID."
    }
    try { return Resolve-GeoCeDGRepositoryIdentityCommit $Root $Commit }
    catch { Stop-CatalogReconciliation 'ANCESTRY_INVALID' "$Role does not resolve: $Commit" }
}

function Get-ReconciliationTree {
    param([string]$Root, [string]$Commit)
    return (Get-ReconciliationGitText $Root @('rev-parse', "${Commit}^{tree}")).Trim()
}

function Get-ReconciliationParents {
    param([string]$Root, [string]$Commit)
    $line = (Get-ReconciliationGitText $Root @('rev-list', '--parents', '-n', '1', $Commit)).Trim()
    return [string[]]@($line.Split(' ', [StringSplitOptions]::RemoveEmptyEntries) | Select-Object -Skip 1)
}

function Test-ReconciliationPathAtCommit {
    param([string]$Root, [string]$Commit, [string]$Path)
    $result = Invoke-GeoCeDGRepositoryIdentityGit $Root @('cat-file', '-e', "${Commit}:$Path") -AllowFailure
    return $result.ExitCode -eq 0
}

function Get-ReconciliationBlobOid {
    param([string]$Root, [string]$Commit, [string]$Path)
    $value = Get-ReconciliationGitText $Root @('rev-parse', '--verify', '--quiet', "${Commit}:$Path") -AllowFailure
    if ($null -eq $value) { return $null }
    return $value.Trim()
}

function Get-ReconciliationBlobText {
    param([string]$Root, [string]$Commit, [string]$Path)
    if (-not (Test-ReconciliationPathAtCommit $Root $Commit $Path)) {
        Stop-CatalogReconciliation 'ANCESTRY_INVALID' "$Path is absent at $Commit."
    }
    return Get-ReconciliationGitText $Root @('cat-file', 'blob', "${Commit}:$Path")
}

function Get-ReconciliationTextSha256 {
    param([AllowEmptyString()] [string]$Text)
    $canonical = ConvertTo-VerificationCanonicalLf -Text $Text
    return Get-VerificationSha256 -Bytes ([Text.UTF8Encoding]::new($false).GetBytes($canonical))
}

function Get-ReconciliationDelta {
    param([string]$Root, [string]$From, [string]$To)
    $text = Get-ReconciliationGitText $Root @('diff-tree', '-r', '--no-commit-id',
        '--name-status', '--no-renames', '-z', $From, $To, '--')
    $parts = [string[]]@($text.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries))
    if ($parts.Count % 2 -ne 0) { Stop-CatalogReconciliation 'UNPERMITTED_DELTA' 'Unparseable Git delta.' }
    $entries = [Collections.Generic.List[object]]::new()
    for ($index = 0; $index -lt $parts.Count; $index += 2) {
        $status = $parts[$index]
        if ($status -cnotin @('A', 'M', 'D')) {
            Stop-CatalogReconciliation 'UNPERMITTED_DELTA' "Unsupported delta status $status for $($parts[$index + 1])."
        }
        $entries.Add([pscustomobject][ordered]@{ path = $parts[$index + 1]; status = $status })
    }
    return [object[]]@($entries | Sort-Object -Property path -CaseSensitive)
}

function Get-ReconciliationPathClass {
    param([string]$Path, [string]$Status)
    if ($Path -ceq $script:InventoryPath) { return 'DERIVED_JUNIT_INVENTORY' }
    if ($Path -ceq $script:RegistryPath) { return 'DERIVED_REGISTRY_PIN' }
    if ($script:MechanismPaths -ccontains $Path) { return 'RECONCILIATION_MECHANISM' }
    if ($script:ConsumerPaths -ccontains $Path) { return 'RECONCILIATION_CLOSEOUT_CONSUMER' }
    if ($script:GovernancePaths -ccontains $Path) { return 'GOVERNANCE_POLICY_DOCUMENT' }
    # Non-executed documentation: new ADRs and validation records, and edits of
    # the roadmap and developer guides. Diagnostics may read them; no acceptance
    # node, package or product payload does. Existing records stay frozen.
    if (($Path -cmatch '^docs/(adr|validation)/[^/]+\.md$' -and $Status -ceq 'A') -or
            ($Path -cmatch '^docs/(roadmap|developer)/[^/]+\.md$' -and $Status -cin @('A', 'M'))) {
        return 'DOCUMENTATION'
    }
    if ($Path -cmatch '^geocedg/validation/[a-z0-9][a-z0-9._-]*/[a-z0-9][a-z0-9._-]*\.json$' -and
            $Status -ceq 'A') {
        return 'STATUS_RECORD'
    }
    return $null
}

function Get-ReconciliationAuthorityFlags {
    param([object[]]$Delta)
    $flags = [ordered]@{
        productPayloadUnchanged = $true
        scientificTestsUnchanged = $true
        buildAuthorityUnchanged = $true
        numericalReferencesUnchanged = $true
        toolchainUnchanged = $true
        verifierExecutionUnchanged = $true
    }
    $allowedVerifier = [string[]]@($script:MechanismPaths + $script:ConsumerPaths +
        $script:InventoryPath + $script:RegistryPath)
    foreach ($entry in $Delta) {
        $path = [string]$entry.path
        if ($path -cmatch '(^|/)(build\.gradle(\.kts)?|settings\.gradle(\.kts)?|gradle\.properties|gradlew(\.bat)?)$' -or
                $path.StartsWith('gradle/', [StringComparison]::Ordinal) -or
                $path.StartsWith('source/build-logic/', [StringComparison]::Ordinal)) {
            $flags.buildAuthorityUnchanged = $false
        } elseif ($path -cmatch '^source/.+/src/(test|testFixtures)/') {
            $flags.scientificTestsUnchanged = $false
        } elseif ($path -cmatch '^(source|packaging|apps|geocedg/resources|geocedg/features)/') {
            $flags.productPayloadUnchanged = $false
        } elseif ($path -cmatch '^(models|python|benchmarks)/' -or
                ($path.StartsWith('geocedg/validation/', [StringComparison]::Ordinal) -and
                    [string]$entry.status -cne 'A')) {
            $flags.numericalReferencesUnchanged = $false
        } elseif ($path -cmatch '^(tools/bootstrap|tools/build|tools/release)/' -or
                $path -cmatch '^(environment[^/]*\.ya?ml|\.java-version|\.gitattributes)$') {
            $flags.toolchainUnchanged = $false
        } elseif (($path.StartsWith('tools/agent/', [StringComparison]::Ordinal) -or
                    $path -cmatch '^geocedg/specs/operations/[^/]+\.json$') -and
                $allowedVerifier -cnotcontains $path) {
            $flags.verifierExecutionUnchanged = $false
        }
    }
    return $flags
}

function Get-ReconciliationReferencedPaths {
    param([object]$Registry, [string]$StaticContractsText)
    $references = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($node in [object[]]@(Get-ReconciliationProperty $Registry 'nodes')) {
        foreach ($value in [object[]]@(Get-ReconciliationProperty $node 'argv') +
                [object[]]@(Get-ReconciliationProperty $node 'impact_paths')) {
            if ($null -eq $value) { continue }
            $text = ([string]$value).Replace('{repository_root}/', '')
            if (-not [string]::IsNullOrWhiteSpace($text)) { [void]$references.Add($text) }
        }
    }
    $static = $StaticContractsText | ConvertFrom-Json -Depth 100
    foreach ($contract in [object[]]@(Get-ReconciliationProperty $static 'contracts')) {
        foreach ($input in [object[]]@(Get-ReconciliationProperty $contract 'inputs')) {
            [void]$references.Add([string](Get-ReconciliationProperty $input 'path'))
        }
    }
    return ,$references
}

function Assert-ReconciliationDeltaSegment {
    param([object[]]$Delta, [string[]]$AllowedClasses, [string]$Segment,
        [Collections.Generic.HashSet[string]]$References)
    $classified = [Collections.Generic.List[object]]::new()
    foreach ($entry in $Delta) {
        $class = Get-ReconciliationPathClass ([string]$entry.path) ([string]$entry.status)
        if ($class -ceq 'RECONCILIATION_MECHANISM' -and
                ($AllowedClasses -cnotcontains $class -or [string]$entry.status -cne 'A')) {
            Stop-CatalogReconciliation 'MECHANISM_CHANGED' "$Segment changes the reconciliation mechanism: $($entry.path)."
        }
        if ($null -eq $class -or $AllowedClasses -cnotcontains $class) {
            Stop-CatalogReconciliation 'UNPERMITTED_DELTA' "$Segment changes $($entry.path) ($($entry.status))."
        }
        if ($class -cin @('RECONCILIATION_CLOSEOUT_CONSUMER', 'GOVERNANCE_POLICY_DOCUMENT',
                    'DERIVED_JUNIT_INVENTORY', 'DERIVED_REGISTRY_PIN') -and
                [string]$entry.status -cne 'M') {
            Stop-CatalogReconciliation 'UNPERMITTED_DELTA' "$Segment adds or deletes $($entry.path)."
        }
        if ($class -ceq 'STATUS_RECORD') {
            # A new record may not enter a directory that any node or static
            # contract reads, so no glob can consume it as an input.
            $directory = ([string]$entry.path).Substring(0, ([string]$entry.path).LastIndexOf('/'))
            foreach ($reference in $References) {
                $normalized = $reference.TrimEnd('/')
                if ($normalized -ceq '.' -or [string]::IsNullOrWhiteSpace($normalized)) { continue }
                if ($normalized -ceq $directory -or
                        $normalized.StartsWith($directory + '/', [StringComparison]::Ordinal) -or
                        $directory.StartsWith($normalized + '/', [StringComparison]::Ordinal)) {
                    Stop-CatalogReconciliation 'UNPERMITTED_DELTA' "$Segment record enters an execution-input directory: $($entry.path)."
                }
            }
        }
        $classified.Add([pscustomobject][ordered]@{ path = [string]$entry.path
            status = [string]$entry.status; class = $class })
    }
    return [object[]]$classified.ToArray()
}

function Read-ReconciliationEvidenceFile {
    param([string]$Path, [string]$ExpectedSha256, [string]$Role)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MISSING' "$Role is absent: $Path"
    }
    $actual = Get-VerificationFileSha256 -Path $Path
    if ($actual -cne $ExpectedSha256) {
        Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MUTATED' "$Role hash differs: $Path"
    }
    return $actual
}

function Get-ReconciliationDirectorySnapshot {
    param([string]$Directory)
    $root = [IO.Path]::GetFullPath($Directory)
    $records = foreach ($file in @(Get-ChildItem -LiteralPath $root -Recurse -File -Force |
            Sort-Object FullName -CaseSensitive)) {
        [IO.Path]::GetRelativePath($root, $file.FullName).Replace('\', '/') + [char]0 +
            (Get-VerificationFileSha256 -Path $file.FullName)
    }
    return Get-VerificationDeterministicHash -Value ([string[]]@($records))
}

function Assert-ReconciliationProducerEvidence {
    param([object]$Producer)
    $id = [string]$Producer.check_id
    if ([string]$Producer.completion_state -cne 'COMPLETED' -or $null -eq $Producer.exit_code -or
            [int]$Producer.exit_code -ne 0) {
        Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' "Producer $id did not complete with exit 0."
    }
    foreach ($stream in @('stdout', 'stderr')) {
        $path = [string](Get-ReconciliationProperty $Producer "${stream}_log")
        $hash = [string](Get-ReconciliationProperty $Producer "${stream}_sha256")
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            [void](Read-ReconciliationEvidenceFile $path $hash "$id $stream log")
        }
    }
    $structured = $Producer.structured_output
    if ([string]$structured.state -ceq 'PRESENT') {
        [void](Read-ReconciliationEvidenceFile ([string]$structured.path) ([string]$structured.sha256) `
            "$id structured output")
        $file = Read-VerificationJson -Path ([string]$structured.path)
        if ((ConvertTo-VerificationCanonicalJson -Value $file) -cne
                (ConvertTo-VerificationCanonicalJson -Value $structured.value)) {
            Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MUTATED' "$id structured output differs from the sealed value."
        }
    } elseif ([string]$structured.state -cne 'NOT_DECLARED') {
        Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MISSING' "$id structured output is $($structured.state)."
    }
    foreach ($evidence in [object[]]@($Producer.generated_evidence)) {
        if ([string]$evidence.state -ceq 'PRESENT') {
            [void](Read-ReconciliationEvidenceFile ([string]$evidence.path) ([string]$evidence.sha256) `
                "$id generated evidence $($evidence.name)")
        }
    }
}

function Assert-ReconciliationObservationEvidence {
    param([object]$Observation)
    foreach ($evidence in [object[]]@($Observation.evidence)) {
        if ([string]$evidence.state -ceq 'PRESENT' -and -not [string]::IsNullOrWhiteSpace([string]$evidence.path)) {
            [void](Read-ReconciliationEvidenceFile ([string]$evidence.path) ([string]$evidence.sha256) `
                "$($Observation.check_id) evidence $($evidence.name)")
        }
    }
}

function Get-ReconciliationJUnitFiles {
    param([object]$Value, [string]$Role)
    $files = [object[]]@($Value.junit_files)
    if ($files.Count -eq 0) { Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MISSING' "$Role has no JUnit XML." }
    $records = [Collections.Generic.List[object]]::new()
    foreach ($file in $files) {
        $hash = Read-ReconciliationEvidenceFile ([string]$file.path) ([string]$file.sha256) "$Role JUnit XML"
        $records.Add([pscustomobject][ordered]@{
            name = [IO.Path]::GetFileName([string]$file.path); sha256 = $hash })
    }
    return [pscustomobject]@{
        paths = [string[]]@($files | ForEach-Object { [string]$_.path })
        count = $files.Count
        sha256 = Get-VerificationDeterministicHash -Value ([object[]]@(
            $records | Sort-Object -Property name -CaseSensitive))
    }
}

function Get-ReconciliationSelectionById {
    param([object]$Inventory, [string]$SelectionId)
    $found = [object[]]@([object[]]@($Inventory.selections) |
        Where-Object { [string]$_.selection_id -ceq $SelectionId })
    if ($found.Count -ne 1) {
        Stop-CatalogReconciliation 'SELECTION_CONTRACT_CHANGED' "Inventory selection is not unique: $SelectionId"
    }
    return $found[0]
}

function Get-ReconciliationSelectionContract {
    param([object]$Selection)
    return ConvertTo-VerificationCanonicalJson -Value $Selection `
        -ExcludeProperties @('expected_identity_count', 'expected_identities_sha256')
}

function Get-ReconciliationProducerSelectionId {
    param([object]$ProducerNode)
    $argv = [string[]]@($ProducerNode.argv)
    for ($index = 0; $index -lt $argv.Count - 1; $index++) {
        if ($argv[$index] -ceq '-SelectionId') { return $argv[$index + 1] }
    }
    Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' "Producer $($ProducerNode.check_id) declares no selection."
}

function Get-ReconciliationTemporaryDirectory {
    $path = Join-Path ([IO.Path]::GetTempPath()) ('geocedg-catalog-reconciliation-' +
        [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($path)
    [IO.File]::WriteAllText((Join-Path $path '.owner'), 'geocedg-catalog-reconciliation',
        [Text.UTF8Encoding]::new($false))
    return $path
}

function Remove-ReconciliationTemporaryDirectory {
    param([string]$Path)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) { return }
    $marker = Join-Path $Path '.owner'
    $temp = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
    if ([IO.Path]::GetFullPath($Path).StartsWith($temp, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker) -and
            [IO.File]::ReadAllText($marker) -ceq 'geocedg-catalog-reconciliation') {
        Remove-Item -LiteralPath $Path -Recurse -Force
    }
}

function Invoke-ReconciliationUpdater {
    param([string]$UpdaterPath, [string]$InventoryPath, [string[]]$Discovery, [string[]]$Selection)
    $quote = { param([string]$Value) "'" + $Value.Replace("'", "''") + "'" }
    $discoveryList = ($Discovery | ForEach-Object { & $quote $_ }) -join ','
    $selectionList = ($Selection | ForEach-Object { & $quote $_ }) -join ','
    $command = "try { & $(& $quote $UpdaterPath) -InventoryPath $(& $quote $InventoryPath) " +
        "-DiscoveryEvidencePath @($discoveryList) -SelectionEvidencePath @($selectionList); exit 0 } " +
        'catch { [Console]::Error.WriteLine($_.Exception.Message); exit 3 }'
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = (Get-Process -Id $PID).Path
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @('-NoLogo', '-NoProfile', '-NonInteractive', '-Command', $command)) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Could not start the official inventory updater.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [void]$stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            Stop-CatalogReconciliation 'CATALOG_NOT_DERIVABLE' "Official updater failed: $($stderr.Trim())"
        }
    } finally { $process.Dispose() }
}

function Assert-ReconciliationWorkingBlob {
    param([string]$Root, [string]$Path, [string]$ExpectedOid)
    $full = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
        Stop-CatalogReconciliation 'UPDATER_AUTHORITY_CHANGED' "Working authority is absent: $Path"
    }
    $actual = (Get-ReconciliationGitText $Root @('hash-object', '--', $Path)).Trim()
    if ($actual -cne $ExpectedOid) {
        Stop-CatalogReconciliation 'UPDATER_AUTHORITY_CHANGED' "Working authority differs from its commit blob: $Path"
    }
}

function Resolve-ReconciliationPlan {
    param([string]$Root, [string]$Commit, [string]$TemporaryDirectory)
    $registryText = Get-ReconciliationBlobText $Root $Commit $script:RegistryPath
    $schemaFile = Join-Path $TemporaryDirectory ("registry-schema-" + $Commit + '.json')
    [IO.File]::WriteAllText($schemaFile, (Get-ReconciliationBlobText $Root $Commit $script:RegistrySchemaPath),
        [Text.UTF8Encoding]::new($false))
    $registry = $registryText | ConvertFrom-Json -Depth 100
    [void](Assert-VerificationJsonSchema -Value $registry -SchemaPath $schemaFile)
    [void](Assert-VerificationRegistry -Registry $registry)
    $plan = Resolve-VerificationRegistryPlan -Registry $registry -Profile 'FINAL' -Selection $null
    return [pscustomobject]@{ registry = $registry; text = $registryText; plan = $plan }
}

function Get-ReconciliationCatalogPin {
    param([object]$Registry, [string]$CatalogId)
    $found = [object[]]@([object[]]@($Registry.catalogs) |
        Where-Object { [string]$_.catalog_id -ceq $CatalogId })
    if ($found.Count -ne 1) { Stop-CatalogReconciliation 'REGISTRY_PIN_MISMATCH' "Catalog $CatalogId is not unique." }
    return $found[0]
}

function Invoke-VerificationCatalogReconciliation {
    <#
    .SYNOPSIS
    Establishes eligibility (ANALYZE) or validates a reconciliation descendant
    (VALIDATE) under verification-levels.md section 11.3. Read-only for the
    repository and the source run; returns a typed result.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [ValidateSet('ANALYZE', 'VALIDATE')] [string]$Action,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
        [string]$MechanismCommit,
        [string]$ReconciliationCommit,
        [Parameter(Mandatory)] [string]$SourceResultPath,
        [Parameter(Mandatory)] [string]$SourceRunId,
        [Parameter(Mandatory)] [string]$ExpectedSourceResultSha256,
        [Parameter(Mandatory)] [string[]]$DiscoveryEvidencePath,
        [switch]$RequireReconciliationCheckout
    )

    $temporary = $null
    $snapshotBefore = $null
    $sourceDirectory = $null
    try {
        $root = (Resolve-Path -LiteralPath ([IO.Path]::GetFullPath($RepositoryRoot))).Path
        $temporary = Get-ReconciliationTemporaryDirectory
        $t = Resolve-ReconciliationCommit $root $ReviewedTechnicalCommit 'ReviewedTechnicalCommit'
        $tTree = Get-ReconciliationTree $root $t
        $g = $null; $gTree = $null
        if (-not [string]::IsNullOrWhiteSpace($MechanismCommit)) {
            $g = Resolve-ReconciliationCommit $root $MechanismCommit 'MechanismCommit'
            $gTree = Get-ReconciliationTree $root $g
        }
        $r = $null; $rTree = $null
        if ($Action -ceq 'VALIDATE') {
            $r = Resolve-ReconciliationCommit $root $ReconciliationCommit 'ReconciliationCommit'
            $rTree = Get-ReconciliationTree $root $r
        } elseif (-not [string]::IsNullOrWhiteSpace($ReconciliationCommit)) {
            Stop-CatalogReconciliation 'ANCESTRY_INVALID' 'ANALYZE runs before the reconciliation commit exists.'
        }
        # The commit whose working authorities the derivation uses.
        $reference = if ($null -ne $r) { $r } elseif ($null -ne $g) { $g } else { $t }

        # R1: exact source result and candidate.
        $sourcePath = [IO.Path]::GetFullPath($SourceResultPath)
        if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
            Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MISSING' "Source result is absent: $sourcePath"
        }
        $sourceDirectory = Split-Path -Parent $sourcePath
        $snapshotBefore = Get-ReconciliationDirectorySnapshot $sourceDirectory
        $sourceFileSha = Get-VerificationFileSha256 -Path $sourcePath
        if ($sourceFileSha -cne $ExpectedSourceResultSha256) {
            Stop-CatalogReconciliation 'SOURCE_RESULT_MODIFIED' 'Source result file SHA-256 differs from the declared source run.'
        }
        $result = Read-VerificationJson -Path $sourcePath
        $resultSchema = Join-Path $temporary 'result-schema.json'
        [IO.File]::WriteAllText($resultSchema, (Get-ReconciliationBlobText $root $t $script:ResultSchemaPath),
            [Text.UTF8Encoding]::new($false))
        try { [void](Assert-VerificationJsonSchema -Value $result -SchemaPath $resultSchema) }
        catch { Stop-CatalogReconciliation 'SOURCE_RESULT_MODIFIED' 'Source result does not satisfy the result schema of T.' }
        if ([string]$result.result_hash -cne (Get-VerificationResultIdentityHash -Report $result)) {
            Stop-CatalogReconciliation 'SOURCE_RESULT_MODIFIED' 'Source result identity hash is invalid.'
        }
        if ([string]$result.run_id -cne $SourceRunId) {
            Stop-CatalogReconciliation 'WRONG_SOURCE_RUN' "Source result is $($result.run_id), not $SourceRunId."
        }
        if ([string]$result.candidate_commit -cne $t -or [string]$result.candidate_tree -cne $tTree) {
            Stop-CatalogReconciliation 'WRONG_SOURCE_CANDIDATE' 'Source result does not bind the reviewed technical commit and tree.'
        }
        if ([string]$result.profile -cne 'FINAL' -or [string]$result.run_state -cne 'COMPLETED') {
            Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' 'Source run is not a completed FINAL campaign.'
        }
        if ([string]$result.acceptance_verdict -cne 'REJECTED_VERIFICATION_CORE' -or
                [string]$result.coverage_verdict -cne 'UNTRUSTED') {
            Stop-CatalogReconciliation 'UNSUPPORTED_SOURCE_STATUS' `
                "Source status $($result.acceptance_verdict) / $($result.coverage_verdict) is not reconcilable."
        }
        $sourcePlan = Resolve-ReconciliationPlan $root $t $temporary
        if ([string]$sourcePlan.plan.execution_plan_hash -cne [string]$result.execution_plan_hash) {
            Stop-CatalogReconciliation 'SOURCE_PLAN_MISMATCH' 'Source execution plan differs from the FINAL plan of T.'
        }

        # R2: complete campaign.
        # Required coverage exactly as the supervisor derives it from the plan.
        $planNodes = [object[]]@($sourcePlan.plan.nodes)
        $requiredIds = [string[]]@(@($planNodes | Where-Object {
            [string]$_.node_kind -cne 'PROCESS_PRODUCER' -and
                [string](Get-ReconciliationProperty $_ 'contract_class') -cin
                    @('SEMANTIC', 'SAFETY', 'VERIFICATION_CORE')
        } | ForEach-Object { [string]$_.check_id }) + [string[]]@($sourcePlan.plan.missing_check_ids) |
            Sort-Object -CaseSensitive -Unique)
        $recordedRequired = [string[]]@([string[]]@($result.coverage.required_check_ids) | Sort-Object -CaseSensitive)
        $recordedResults = [string[]]@([object[]]@($result.acceptance_results) |
            ForEach-Object { [string]$_.check_id } | Sort-Object -CaseSensitive)
        if (($requiredIds -join "`n") -cne ($recordedRequired -join "`n") -or
                ($requiredIds -join "`n") -cne ($recordedResults -join "`n")) {
            Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' 'Source coverage or results differ from the FINAL plan.'
        }
        if (@($result.coverage.not_run_check_ids).Count -gt 0) {
            Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' 'Source run has not-run checks.'
        }
        $plannedProducers = [string[]]@($planNodes | Where-Object { [string]$_.node_kind -ceq 'PROCESS_PRODUCER' } |
            ForEach-Object { [string]$_.check_id } | Sort-Object -CaseSensitive)
        $producers = [object[]]@($result.process_producers)
        $recordedProducers = [string[]]@($producers | ForEach-Object { [string]$_.check_id } | Sort-Object -CaseSensitive)
        if (($plannedProducers -join "`n") -cne ($recordedProducers -join "`n")) {
            Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' 'Source producers differ from the FINAL plan.'
        }
        # R7/R14: every sealed evidence file exists unchanged.
        foreach ($producer in $producers) { Assert-ReconciliationProducerEvidence $producer }
        foreach ($observation in [object[]]@($result.acceptance_results) + [object[]]@($result.diagnostic_findings)) {
            Assert-ReconciliationObservationEvidence $observation
        }

        # R4/R5/R15: the rejection is exactly one stale selection inventory.
        $acceptance = [object[]]@($result.acceptance_results)
        if (@($acceptance | Where-Object { [string]$_.status -ceq 'CONTRACT_VIOLATED' }).Count -gt 0) {
            Stop-CatalogReconciliation 'SEMANTIC_OR_SAFETY_VIOLATION' 'Source run contains a violated acceptance contract.'
        }
        $nodeMap = @{}
        foreach ($node in $planNodes) { $nodeMap[[string]$node.check_id] = $node }
        $untrusted = [object[]]@($acceptance | Where-Object { [string]$_.status -cne 'CONTRACT_SATISFIED' })
        foreach ($entry in $untrusted) {
            $node = $nodeMap[[string]$entry.check_id]
            if ([string]$entry.status -cne 'EVIDENCE_UNTRUSTED' -or [string]$entry.coverage_state -cne 'UNTRUSTED' -or
                    [string]$entry.node_kind -cne 'EVIDENCE_PROJECTION' -or $null -eq $node -or
                    [string](Get-ReconciliationProperty $node 'output_adapter') -cne 'PROJECTION_JUNIT_SELECTION_V1' -or
                    [string]$entry.cause -cne $script:StaleCause) {
                Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' `
                    "Acceptance result $($entry.check_id) is $($entry.status): $($entry.cause)"
            }
        }
        if ($untrusted.Count -ne 1) {
            Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' "Version 1 reconciles exactly one stale selection, found $($untrusted.Count)."
        }
        if ((@($result.coverage.untrusted_check_ids) -join "`n") -cne [string]$untrusted[0].check_id) {
            Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' 'Untrusted coverage differs from the stale selection.'
        }
        foreach ($entry in $acceptance) {
            if ([string]$entry.status -ceq 'CONTRACT_SATISFIED' -and [string]$entry.coverage_state -cne 'COMPLETE') {
                Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' "Satisfied result $($entry.check_id) has incomplete coverage."
            }
        }
        $staleId = [string]$untrusted[0].check_id

        # Inventories, discovery evidence and the official derivation (R9/R10).
        $invTText = Get-ReconciliationBlobText $root $t $script:InventoryPath
        $invT = $invTText | ConvertFrom-Json -Depth 100
        $discoveryRecords = [Collections.Generic.List[object]]::new()
        $discoveryPaths = [Collections.Generic.List[string]]::new()
        $supplied = @{}
        foreach ($path in $DiscoveryEvidencePath) {
            $full = [IO.Path]::GetFullPath($path)
            if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
                Stop-CatalogReconciliation 'DISCOVERY_EVIDENCE_UNAUTHENTICATED' "Discovery evidence is absent: $full"
            }
            $supplied[(Get-VerificationFileSha256 -Path $full)] = $full
        }
        foreach ($module in [object[]]@($invT.modules)) {
            $expected = [string]$module.discovery_evidence_sha256
            if (-not $supplied.ContainsKey($expected)) {
                Stop-CatalogReconciliation 'DISCOVERY_EVIDENCE_UNAUTHENTICATED' `
                    "No supplied discovery evidence matches module $($module.module)."
            }
            $evidence = Read-VerificationJson -Path $supplied[$expected]
            if (-not [bool]$evidence.test_dry_run -or [string]$evidence.completion_state -cne 'COMPLETED' -or
                    [string]$evidence.selection.module -cne [string]$module.module) {
                Stop-CatalogReconciliation 'DISCOVERY_EVIDENCE_UNAUTHENTICATED' `
                    "Discovery evidence of $($module.module) is not a completed dry run."
            }
            $junit = Get-ReconciliationJUnitFiles $evidence "discovery $($module.module)"
            $discoveryRecords.Add([pscustomobject][ordered]@{ module = [string]$module.module
                sha256 = $expected; junitFileCount = $junit.count })
            $discoveryPaths.Add([string]$supplied[$expected])
        }
        if ($discoveryPaths.Count -ne $supplied.Count) {
            Stop-CatalogReconciliation 'DISCOVERY_EVIDENCE_UNAUTHENTICATED' 'Unmatched discovery evidence was supplied.'
        }
        foreach ($path in $script:UpdaterPaths) {
            $oidT = Get-ReconciliationBlobOid $root $t $path
            $oidRef = Get-ReconciliationBlobOid $root $reference $path
            if ($null -eq $oidT -or $oidT -cne $oidRef) {
                Stop-CatalogReconciliation 'UPDATER_AUTHORITY_CHANGED' "Derivation authority changed: $path"
            }
            Assert-ReconciliationWorkingBlob $root $path $oidT
        }

        # R3/R6/R8: every JUnit acceptance selection re-read from its own evidence.
        $selections = [Collections.Generic.List[object]]::new()
        $selectionEvidence = [Collections.Generic.List[string]]::new()
        foreach ($node in @($planNodes | Where-Object {
                [string](Get-ReconciliationProperty $_ 'output_adapter') -ceq 'PROJECTION_JUNIT_SELECTION_V1'
            } | Sort-Object -Property check_id -CaseSensitive)) {
            $producerId = [string]$node.producer_dependency
            $producer = [object[]]@($producers | Where-Object { [string]$_.check_id -ceq $producerId })
            $historical = [object[]]@($acceptance | Where-Object { [string]$_.check_id -ceq [string]$node.check_id })
            if ($producer.Count -ne 1 -or $historical.Count -ne 1 -or
                    [string]$producer[0].structured_output.state -cne 'PRESENT') {
                Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MISSING' "JUnit selection $($node.check_id) lacks its evidence."
            }
            $value = $producer[0].structured_output.value
            $selectionId = Get-ReconciliationProducerSelectionId $nodeMap[$producerId]
            if ([string]$value.selection_id -cne $selectionId -or [string]$value.selection.selection_id -cne $selectionId -or
                    [bool]$value.test_dry_run -or [string]$value.completion_state -cne 'COMPLETED' -or
                    $null -eq $value.inner_exit_code -or [int]$value.inner_exit_code -ne 0) {
                Stop-CatalogReconciliation 'SOURCE_RUN_INCOMPLETE' "JUnit producer $producerId did not complete its selection."
            }
            $junit = Get-ReconciliationJUnitFiles $value $producerId
            $cases = [object[]]@(Read-VerificationJUnitFiles -Path $junit.paths -Module ([string]$value.selection.module))
            $failed = @($cases | Where-Object { [string]$_.state -cin @('FAILED', 'ERRORED') }).Count
            if ($cases.Count -eq 0 -or $failed -gt 0) {
                Stop-CatalogReconciliation 'SOURCE_TEST_FAILURE' "$producerId has $failed failed or errored case(s)."
            }
            $selectionT = Get-ReconciliationSelectionById $invT $selectionId
            if ((ConvertTo-VerificationCanonicalJson -Value $value.selection) -cne
                    (ConvertTo-VerificationCanonicalJson -Value $selectionT)) {
                Stop-CatalogReconciliation 'SELECTION_CONTRACT_CHANGED' "The run of $selectionId did not use the tracked selection of T."
            }
            $replay = ConvertFrom-VerificationJUnitSelection -ProcessEvidence $value -Selection $value.selection `
                -SemanticDomain ([string]$node.semantic_domain)
            if ([string]$replay.outcome -cne [string]$historical[0].status -or
                    [string]$replay.cause -cne [string]$historical[0].cause) {
                Stop-CatalogReconciliation 'SOURCE_RESULT_MODIFIED' "Replay of $($node.check_id) does not reproduce the historical outcome."
            }
            $selections.Add([pscustomobject]@{ checkId = [string]$node.check_id; producerId = $producerId
                selectionId = $selectionId; value = $value; junit = $junit; semanticDomain = [string]$node.semantic_domain
                executedCount = $cases.Count; executedSha = Get-VerificationJUnitIdentityHash -Cases $cases
                stale = [string]$node.check_id -ceq $staleId; selectionT = $selectionT })
            $selectionEvidence.Add([string]$producer[0].structured_output.path)
        }
        $staleSelections = [object[]]@($selections | Where-Object { $_.stale })
        if ($staleSelections.Count -ne 1) {
            Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' 'The stale check is not a planned JUnit selection projection.'
        }
        $stale = $staleSelections[0]

        $inventoryCopy = Join-Path $temporary 'inventory.json'
        [IO.File]::WriteAllText($inventoryCopy, $invTText, [Text.UTF8Encoding]::new($false))
        Invoke-ReconciliationUpdater (Join-Path $root 'tools/agent/update-verification-junit-inventory.ps1') `
            $inventoryCopy ([string[]]$discoveryPaths.ToArray()) ([string[]]$selectionEvidence.ToArray())
        $proposedText = [IO.File]::ReadAllText($inventoryCopy, [Text.UTF8Encoding]::new($false, $true))
        $proposed = $proposedText | ConvertFrom-Json -Depth 100
        $expectedProposal = $invTText | ConvertFrom-Json -Depth 100
        $proposalSelection = Get-ReconciliationSelectionById $expectedProposal $stale.selectionId
        $proposalSelection.expected_identity_count = $stale.executedCount
        $proposalSelection.expected_identities_sha256 = $stale.executedSha
        if ((ConvertTo-VerificationCanonicalJson -Value $proposed -ExcludeProperties @('generated_at')) -cne
                (ConvertTo-VerificationCanonicalJson -Value $expectedProposal -ExcludeProperties @('generated_at'))) {
            Stop-CatalogReconciliation 'CATALOG_IDENTITY_MISMATCH' 'The official derivation changes more than the stale executed selection.'
        }

        $invRText = $proposedText
        if ($null -ne $r) {
            $invRText = Get-ReconciliationBlobText $root $r $script:InventoryPath
            if ((Get-ReconciliationTextSha256 $invRText) -cne (Get-ReconciliationTextSha256 $proposedText)) {
                Stop-CatalogReconciliation 'CATALOG_NOT_DERIVABLE' 'The tracked inventory of R is not the official derivation.'
            }
        }
        $invR = $invRText | ConvertFrom-Json -Depth 100
        $otherSelections = [Collections.Generic.List[object]]::new()
        foreach ($selection in $selections) {
            $selectionR = Get-ReconciliationSelectionById $invR $selection.selectionId
            if ((Get-ReconciliationSelectionContract $selectionR) -cne (Get-ReconciliationSelectionContract $selection.selectionT)) {
                Stop-CatalogReconciliation 'SELECTION_CONTRACT_CHANGED' "Selection contract of $($selection.selectionId) changed."
            }
            if ([int]$selectionR.expected_identity_count -ne $selection.executedCount -or
                    [string]$selectionR.expected_identities_sha256 -cne $selection.executedSha) {
                Stop-CatalogReconciliation 'CATALOG_IDENTITY_MISMATCH' "Catalog of $($selection.selectionId) differs from the executed identities."
            }
            if (-not $selection.stale -and
                    (ConvertTo-VerificationCanonicalJson -Value $selectionR) -cne
                    (ConvertTo-VerificationCanonicalJson -Value $selection.selectionT)) {
                Stop-CatalogReconciliation 'CATALOG_IDENTITY_MISMATCH' "A satisfied selection changed: $($selection.selectionId)."
            }
            $counterfactual = ConvertFrom-VerificationJUnitSelection -ProcessEvidence $selection.value `
                -Selection $selectionR -SemanticDomain $selection.semanticDomain
            if ([string]$counterfactual.outcome -cne 'CONTRACT_SATISFIED' -or
                    [string]$counterfactual.coverage_state -cne 'COMPLETE') {
                Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' `
                    "Selection $($selection.selectionId) is not satisfied with the reconciled catalog: $($counterfactual.cause)"
            }
            if (-not $selection.stale) {
                $otherSelections.Add([pscustomobject][ordered]@{ checkId = $selection.checkId
                    selectionId = $selection.selectionId; executedIdentityCount = $selection.executedCount
                    executedIdentitiesSha256 = $selection.executedSha; reconciledOutcome = 'CONTRACT_SATISFIED' })
            }
        }
        $staleReplay = ConvertFrom-VerificationJUnitSelection -ProcessEvidence $stale.value `
            -Selection $stale.selectionT -SemanticDomain $stale.semanticDomain
        if ([string]$staleReplay.outcome -cne 'EVIDENCE_UNTRUSTED' -or [string]$staleReplay.cause -cne $script:StaleCause) {
            Stop-CatalogReconciliation 'UNSUPPORTED_REJECTION' 'The historical stale-inventory outcome is not reproduced.'
        }
        $inventoryBefore = Get-ReconciliationTextSha256 $invTText
        $inventoryAfter = Get-ReconciliationTextSha256 $invRText
        $pinBefore = [string](Get-ReconciliationCatalogPin $sourcePlan.registry 'junit_inventory').sha256
        if ($pinBefore -cne $inventoryBefore) {
            Stop-CatalogReconciliation 'REGISTRY_PIN_MISMATCH' 'The registry pin of T does not identify the inventory of T.'
        }

        $analysis = [ordered]@{
            eligible = $true
            reviewedTechnicalCommit = $t
            reviewedTechnicalTree = $tTree
            mechanismCommit = $g
            sourceVerificationRun = [string]$result.run_id
            sourceVerificationResultHash = [string]$result.result_hash
            sourceVerificationResultFileSha256 = $sourceFileSha
            sourceVerificationStatus = $script:SourceStatus
            sourceExecutionPlanHash = [string]$result.execution_plan_hash
            reconciliationReason = $script:Reason
            sourceUntrustedChecks = [string[]]@($staleId)
            sourceSelectionId = $stale.selectionId
            sourceExpectedIdentityCount = [int]$stale.selectionT.expected_identity_count
            sourceExpectedIdentitiesSha256 = [string]$stale.selectionT.expected_identities_sha256
            executedIdentityCount = $stale.executedCount
            executedIdentitiesSha256 = $stale.executedSha
            proposedInventoryHash = Get-ReconciliationTextSha256 $proposedText
            proposedRegistryPin = Get-ReconciliationTextSha256 $proposedText
            inventoryHashBefore = $inventoryBefore
            registryPinBefore = $pinBefore
        }
        if ($Action -ceq 'ANALYZE') {
            return [pscustomobject]@{ status = 'ELIGIBLE'; code = $null; message = $null
                analysis = [pscustomobject]$analysis; receipt = $null }
        }

        # Registry and heavy plan (R12).
        $reconciledPlan = Resolve-ReconciliationPlan $root $r $temporary
        $pinAfter = [string](Get-ReconciliationCatalogPin $reconciledPlan.registry 'junit_inventory').sha256
        if ($pinAfter -cne $inventoryAfter) {
            Stop-CatalogReconciliation 'REGISTRY_PIN_MISMATCH' 'The registry pin of R does not identify the inventory of R.'
        }
        $registryR = $reconciledPlan.text | ConvertFrom-Json -Depth 100
        (Get-ReconciliationCatalogPin $registryR 'junit_inventory').sha256 = $pinBefore
        if ((ConvertTo-VerificationCanonicalJson -Value $registryR) -cne
                (ConvertTo-VerificationCanonicalJson -Value $sourcePlan.registry)) {
            Stop-CatalogReconciliation 'EXECUTION_PLAN_CHANGED' 'The registry of R differs from T beyond the inventory pin.'
        }
        if ([string]$reconciledPlan.plan.execution_plan_hash -cne [string]$result.execution_plan_hash) {
            Stop-CatalogReconciliation 'EXECUTION_PLAN_CHANGED' 'The FINAL plan of R differs from the source plan.'
        }

        # Ancestry and bounded delta (R11/R13/R17).
        $ancestor = Invoke-GeoCeDGRepositoryIdentityGit $root @('merge-base', '--is-ancestor', $t, $r) -AllowFailure
        if ($ancestor.ExitCode -ne 0 -or $t -ceq $r) {
            Stop-CatalogReconciliation 'ANCESTRY_INVALID' 'T is not a strict ancestor of R.'
        }
        $fullDelta = Get-ReconciliationDelta $root $t $r
        $flags = Get-ReconciliationAuthorityFlags $fullDelta
        $flagCodes = [ordered]@{
            productPayloadUnchanged = 'PRODUCT_PAYLOAD_CHANGED'
            scientificTestsUnchanged = 'SCIENTIFIC_TESTS_CHANGED'
            buildAuthorityUnchanged = 'BUILD_AUTHORITY_CHANGED'
            numericalReferencesUnchanged = 'NUMERICAL_REFERENCE_CHANGED'
            toolchainUnchanged = 'TOOLCHAIN_CHANGED'
            verifierExecutionUnchanged = 'VERIFIER_EXECUTION_CHANGED'
        }
        foreach ($name in $flagCodes.Keys) {
            if (-not $flags[$name]) { Stop-CatalogReconciliation $flagCodes[$name] 'T..R changes an execution authority.' }
        }
        foreach ($path in $script:AcceptanceSemanticsPaths) {
            if ((Get-ReconciliationBlobOid $root $t $path) -cne (Get-ReconciliationBlobOid $root $r $path)) {
                Stop-CatalogReconciliation 'ACCEPTANCE_SEMANTICS_CHANGED' "Acceptance authority changed: $path"
            }
        }
        $staticPath = [string](Get-ReconciliationCatalogPin $reconciledPlan.registry 'static_contracts').path
        $references = Get-ReconciliationReferencedPaths $reconciledPlan.registry `
            (Get-ReconciliationBlobText $root $r $staticPath)
        foreach ($reference in (Get-ReconciliationReferencedPaths $sourcePlan.registry `
                (Get-ReconciliationBlobText $root $t $staticPath))) { [void]$references.Add($reference) }
        $derivedClasses = @('DERIVED_JUNIT_INVENTORY', 'DERIVED_REGISTRY_PIN', 'DOCUMENTATION', 'STATUS_RECORD')
        $bootstrap = $null -ne $g
        $deltaTG = [object[]]@()
        $deltaGR = [object[]]@()
        if ($bootstrap) {
            $parentsG = [string[]]@(Get-ReconciliationParents $root $g)
            $parentsR = [string[]]@(Get-ReconciliationParents $root $r)
            if ($parentsG.Count -ne 1 -or $parentsG[0] -cne $t -or $parentsR.Count -ne 1 -or $parentsR[0] -cne $g) {
                Stop-CatalogReconciliation 'ANCESTRY_INVALID' 'An initial adoption requires T -> G -> R as direct single-parent children.'
            }
            foreach ($path in $script:MechanismPaths) {
                if (Test-ReconciliationPathAtCommit $root $t $path) {
                    Stop-CatalogReconciliation 'MECHANISM_CHANGED' "The mechanism already exists at T: $path"
                }
            }
            $deltaTG = [object[]]@(Assert-ReconciliationDeltaSegment (Get-ReconciliationDelta $root $t $g) `
                @('RECONCILIATION_MECHANISM', 'RECONCILIATION_CLOSEOUT_CONSUMER', 'GOVERNANCE_POLICY_DOCUMENT',
                    'DOCUMENTATION', 'STATUS_RECORD') 'T..G' $references)
            foreach ($path in $script:MechanismPaths) {
                if (@($deltaTG | Where-Object { $_.path -ceq $path -and $_.status -ceq 'A' }).Count -ne 1) {
                    Stop-CatalogReconciliation 'MECHANISM_CHANGED' "The initial adoption does not add $path."
                }
            }
            $deltaGR = [object[]]@(Assert-ReconciliationDeltaSegment (Get-ReconciliationDelta $root $g $r) `
                $derivedClasses 'G..R' $references)
            $grPaths = [string[]]@($deltaGR | ForEach-Object { $_.path })
            if ($grPaths -cnotcontains $script:InventoryPath -or $grPaths -cnotcontains $script:RegistryPath) {
                Stop-CatalogReconciliation 'UNPERMITTED_DELTA' 'G..R must update the derived inventory and registry pin.'
            }
        } else {
            foreach ($path in $script:MechanismPaths) {
                $oidT = Get-ReconciliationBlobOid $root $t $path
                if ($null -eq $oidT -or $oidT -cne (Get-ReconciliationBlobOid $root $r $path)) {
                    Stop-CatalogReconciliation 'MECHANISM_CHANGED' "The mechanism must be present in T and unchanged in R: $path"
                }
            }
        }
        $deltaTR = [object[]]@(Assert-ReconciliationDeltaSegment $fullDelta $(if ($bootstrap) {
                @('RECONCILIATION_MECHANISM', 'RECONCILIATION_CLOSEOUT_CONSUMER', 'GOVERNANCE_POLICY_DOCUMENT') +
                    $derivedClasses
            } else { $derivedClasses }) 'T..R' $references)
        $trPaths = [string[]]@($deltaTR | ForEach-Object { $_.path })
        if ($trPaths -cnotcontains $script:InventoryPath -or $trPaths -cnotcontains $script:RegistryPath) {
            Stop-CatalogReconciliation 'UNPERMITTED_DELTA' 'T..R must update the derived inventory and registry pin.'
        }

        if ($RequireReconciliationCheckout) {
            $head = (Get-ReconciliationGitText $root @('rev-parse', 'HEAD')).Trim()
            $status = Get-ReconciliationGitText $root @('status', '--porcelain=v1', '--untracked-files=all')
            if ($head -cne $r -or -not [string]::IsNullOrWhiteSpace($status)) {
                Stop-CatalogReconciliation 'CHECKOUT_NOT_RECONCILIATION_COMMIT' 'Receipt generation requires a clean checkout of exactly R.'
            }
        }
        $snapshotAfter = Get-ReconciliationDirectorySnapshot $sourceDirectory
        if ($snapshotAfter -cne $snapshotBefore) {
            Stop-CatalogReconciliation 'SOURCE_EVIDENCE_MUTATED' 'The source run directory changed during reconciliation.'
        }

        $producersCompleted = @($producers | Where-Object { [string]$_.completion_state -ceq 'COMPLETED' }).Count
        $receipt = [ordered]@{
            '$schema' = $script:ReceiptSchemaRelative
            schema_version = 1
            receipt_kind = $script:ReceiptKind
            receipt_id = ''
            mechanismVersion = $script:MechanismVersion
            bootstrapAdoption = $bootstrap
            mechanismCommit = $g
            mechanismTree = $gTree
            reviewedTechnicalCommit = $t
            reviewedTechnicalTree = $tTree
            reconciliationCommit = $r
            reconciliationTree = $rTree
            sourceVerificationRun = [string]$result.run_id
            sourceVerificationProfile = 'FINAL'
            sourceVerificationResultHash = [string]$result.result_hash
            sourceVerificationResultFileSha256 = $sourceFileSha
            sourceVerificationStatus = $script:SourceStatus
            sourceAcceptanceVerdict = [string]$result.acceptance_verdict
            sourceCoverageVerdict = [string]$result.coverage_verdict
            sourceRunReinterpreted = $false
            sourceExecutionPlanHash = [string]$result.execution_plan_hash
            reconciledExecutionPlanHash = [string]$reconciledPlan.plan.execution_plan_hash
            reconciliationReason = $script:Reason
            sourceUntrustedChecks = [string[]]@($staleId)
            sourceAcceptanceSummary = [ordered]@{
                required = $requiredIds.Count
                satisfied = @($acceptance | Where-Object { [string]$_.status -ceq 'CONTRACT_SATISFIED' }).Count
                violated = 0; untrusted = 1; notRun = 0
                producers = $producers.Count; producersCompleted = $producersCompleted
            }
            sourceSelectionId = $stale.selectionId
            sourceProjectionCheckId = $stale.checkId
            sourceProducerCheckId = $stale.producerId
            sourceExpectedIdentityCount = [int]$stale.selectionT.expected_identity_count
            sourceExpectedIdentitiesSha256 = [string]$stale.selectionT.expected_identities_sha256
            executedIdentityCount = $stale.executedCount
            executedIdentitiesSha256 = $stale.executedSha
            executedEvidenceHashes = [ordered]@{
                structuredOutputSha256 = [string](@($producers | Where-Object {
                    [string]$_.check_id -ceq $stale.producerId })[0].structured_output.sha256)
                junitFileCount = $stale.junit.count
                junitFilesSha256 = $stale.junit.sha256
                failedOrErrored = 0
            }
            reconciledExpectedIdentityCount = [int](Get-ReconciliationSelectionById $invR $stale.selectionId).expected_identity_count
            reconciledExpectedIdentitiesSha256 = [string](Get-ReconciliationSelectionById $invR $stale.selectionId).expected_identities_sha256
            counterfactualProjection = [ordered]@{
                sourceSelectionOutcome = [string]$staleReplay.outcome
                sourceSelectionCause = [string]$staleReplay.cause
                reconciledSelectionOutcome = 'CONTRACT_SATISFIED'
                reconciledSelectionCoverage = 'COMPLETE'
            }
            otherJunitSelections = [object[]]$otherSelections.ToArray()
            inventoryHashBefore = $inventoryBefore
            inventoryHashAfter = $inventoryAfter
            registryPinBefore = $pinBefore
            registryPinAfter = $pinAfter
            discoveryEvidence = [object[]]@($discoveryRecords | Sort-Object -Property module -CaseSensitive)
            delta = [ordered]@{
                reviewedToMechanism = [object[]]$deltaTG
                mechanismToReconciliation = [object[]]$deltaGR
                reviewedToReconciliation = [object[]]$deltaTR
            }
            productPayloadUnchanged = $flags.productPayloadUnchanged
            scientificTestsUnchanged = $flags.scientificTestsUnchanged
            buildAuthorityUnchanged = $flags.buildAuthorityUnchanged
            numericalReferencesUnchanged = $flags.numericalReferencesUnchanged
            toolchainUnchanged = $flags.toolchainUnchanged
            verifierExecutionUnchanged = $flags.verifierExecutionUnchanged
            heavyExecutionAuthoritiesUnchanged = $true
            heavyExecutionPlanUnchanged = $true
            acceptanceSemanticsUnchanged = $true
            heavyCampaignRerun = $false
            reconciliationStatus = 'ACCEPTED'
            technicalAcceptanceEligible = $true
            checkerIdentity = [ordered]@{
                reconcilerAuthoritySha256 = [string](Get-GeoCeDGVerificationAuthorityIdentity $root $r).sha256
                sourceAuthoritySha256 = [string](Get-GeoCeDGVerificationAuthorityIdentity $root $t).sha256
            }
            inputIdentity = [ordered]@{
                reviewedTrackedSha256 = [string](Get-GeoCeDGRepositoryTrackedIdentity $root $t).sha256
                reconciliationTrackedSha256 = [string](Get-GeoCeDGRepositoryTrackedIdentity $root $r).sha256
            }
            environment = [ordered]@{
                sourceEnvironmentCompatibilitySignature = [string]$result.environment_compatibility_signature
                sourceEnvironmentContractSha256 = Get-VerificationDeterministicHash -Value $result.environment_contract
            }
            locators = [ordered]@{
                sourceResultPath = $sourcePath
                discoveryEvidencePaths = [string[]]@($discoveryPaths | Sort-Object -CaseSensitive)
            }
            issued_at = [datetime]::UtcNow.ToString('o')
        }
        $receipt.receipt_id = Get-VerificationCatalogReconciliationReceiptId -Receipt $receipt
        [void](Assert-VerificationJsonSchema -Value ([pscustomobject]$receipt) -SchemaPath $script:ReceiptSchemaPath)
        return [pscustomobject]@{ status = 'ACCEPTED'; code = $null; message = $null
            analysis = [pscustomobject]$analysis; receipt = [pscustomobject]$receipt }
    } catch [GeoCeDGCatalogReconciliationRejection] {
        return [pscustomobject]@{ status = 'REJECTED'; code = $_.Exception.Code
            message = $_.Exception.Message; analysis = $null; receipt = $null }
    } catch {
        return [pscustomobject]@{ status = 'REJECTED'; code = 'RECONCILIATION_INTERNAL_FAILURE'
            message = $_.Exception.Message; analysis = $null; receipt = $null }
    } finally {
        Remove-ReconciliationTemporaryDirectory $temporary
    }
}

function Get-VerificationCatalogReconciliationReceiptId {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Receipt)
    return Get-VerificationDeterministicHash -Value $Receipt `
        -ExcludeProperties @('receipt_id', 'issued_at', 'locators')
}

function Assert-VerificationCatalogReconciliationReceipt {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Receipt)
    [void](Assert-VerificationJsonSchema -Value $Receipt -SchemaPath $script:ReceiptSchemaPath)
    if ([string](Get-ReconciliationProperty $Receipt 'receipt_kind') -cne $script:ReceiptKind) {
        throw 'Unexpected catalog reconciliation receipt kind.'
    }
    if ([string](Get-ReconciliationProperty $Receipt 'receipt_id') -cne
            (Get-VerificationCatalogReconciliationReceiptId -Receipt $Receipt)) {
        throw 'Catalog reconciliation receipt identity hash is invalid.'
    }
    if ([string]$Receipt.reconciliationStatus -cne 'ACCEPTED' -or -not [bool]$Receipt.technicalAcceptanceEligible) {
        throw 'Catalog reconciliation receipt is not an accepted technical-acceptance receipt.'
    }
    return $true
}

function Test-VerificationCatalogReconciliationReceipt {
    <#
    .SYNOPSIS
    Closeout route B: revalidates an accepted reconciliation receipt read-only for
    the exact reviewed technical commit T and reconciliation commit R.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$ReviewedTechnicalCommit,
        [Parameter(Mandatory)] [string]$ReconciliationCommit
    )
    [void](Assert-VerificationCatalogReconciliationReceipt -Receipt $Receipt)
    $mismatches = [Collections.Generic.List[string]]::new()
    if ([string]$Receipt.reviewedTechnicalCommit -cne $ReviewedTechnicalCommit) { $mismatches.Add('reviewed_technical_commit') }
    if ([string]$Receipt.reconciliationCommit -cne $ReconciliationCommit) { $mismatches.Add('reconciliation_commit') }
    $recomputed = $null
    $code = $null
    if ($mismatches.Count -eq 0) {
        $run = Invoke-VerificationCatalogReconciliation -Action VALIDATE -RepositoryRoot $RepositoryRoot `
            -ReviewedTechnicalCommit $ReviewedTechnicalCommit -ReconciliationCommit $ReconciliationCommit `
            -MechanismCommit ([string]$Receipt.mechanismCommit) `
            -SourceResultPath ([string]$Receipt.locators.sourceResultPath) `
            -SourceRunId ([string]$Receipt.sourceVerificationRun) `
            -ExpectedSourceResultSha256 ([string]$Receipt.sourceVerificationResultFileSha256) `
            -DiscoveryEvidencePath ([string[]]@($Receipt.locators.discoveryEvidencePaths))
        if ([string]$run.status -cne 'ACCEPTED') {
            $mismatches.Add('reconciliation_revalidation')
            $code = [string]$run.code
        } else {
            $recomputed = [string]$run.receipt.receipt_id
            if ($recomputed -cne [string]$Receipt.receipt_id) { $mismatches.Add('receipt_id') }
        }
    }
    return [pscustomobject]@{
        valid = $mismatches.Count -eq 0
        mismatches = [string[]]$mismatches.ToArray()
        rejectionCode = $code
        recomputedReceiptId = $recomputed
    }
}

function Assert-VerificationCatalogReconciliationOutputPath {
    <#
    .SYNOPSIS
    R14: an output is initially absent, outside the source run directory and,
    when inside the repository, Git-ignored and untracked.
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$SourceResultPath,
        [Parameter(Mandatory)] [string]$Path
    )
    $full = [IO.Path]::GetFullPath($Path)
    if (Test-Path -LiteralPath $full) { throw "Reconciliation output must be initially absent: $full" }
    $comparison = if ($IsWindows) { [StringComparison]::OrdinalIgnoreCase } else { [StringComparison]::Ordinal }
    $sourceDirectory = (Split-Path -Parent ([IO.Path]::GetFullPath($SourceResultPath))).TrimEnd('\', '/')
    if ($full.StartsWith($sourceDirectory + [IO.Path]::DirectorySeparatorChar, $comparison)) {
        throw "Reconciliation output must stay outside the source run directory: $full"
    }
    $root = (Resolve-Path -LiteralPath ([IO.Path]::GetFullPath($RepositoryRoot))).Path.TrimEnd('\', '/')
    $relative = [IO.Path]::GetRelativePath($root, $full)
    if ([IO.Path]::IsPathRooted($relative) -or $relative -eq '..' -or
            $relative.StartsWith('..' + [IO.Path]::DirectorySeparatorChar, [StringComparison]::Ordinal)) {
        return $full
    }
    $gitPath = $relative.Replace('\', '/')
    $tracked = Invoke-GeoCeDGRepositoryIdentityGit $root @('ls-files', '--error-unmatch', '--', $gitPath) -AllowFailure
    $ignored = Invoke-GeoCeDGRepositoryIdentityGit $root @('check-ignore', '-q', '--', $gitPath) -AllowFailure
    if ($tracked.ExitCode -eq 0 -or $ignored.ExitCode -ne 0) {
        throw "Reconciliation output inside the repository must be Git-ignored and untracked: $gitPath"
    }
    return $full
}

function Write-VerificationCatalogReconciliationReceipt {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Receipt
    )
    [void](Assert-VerificationCatalogReconciliationReceipt -Receipt $Receipt)
    $full = Assert-VerificationCatalogReconciliationOutputPath -RepositoryRoot $RepositoryRoot `
        -SourceResultPath ([string]$Receipt.locators.sourceResultPath) -Path $Path
    [void](Write-VerificationAtomicJson -Path $full -Value $Receipt)
    return $full
}

Export-ModuleMember -Function @(
    'Invoke-VerificationCatalogReconciliation',
    'Get-VerificationCatalogReconciliationReceiptId',
    'Assert-VerificationCatalogReconciliationReceipt',
    'Assert-VerificationCatalogReconciliationOutputPath',
    'Test-VerificationCatalogReconciliationReceipt',
    'Write-VerificationCatalogReconciliationReceipt'
)

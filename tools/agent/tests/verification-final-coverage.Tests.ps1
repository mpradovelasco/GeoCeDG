#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force

$script:Cases=0;$script:Assertions=0
function Assert-Case([bool]$Condition,[string]$Message){$script:Assertions++;if(-not$Condition){throw "TEST FAILURE: $Message"}}
function Invoke-Case([string]$Name,[scriptblock]$Action){$script:Cases++;&$Action;Write-Host "PASS: $Name"}
function Has-Field([object]$Value,[string]$Name){return (($Value -is [Collections.IDictionary] -and $Value.Contains($Name)) -or $Value.PSObject.Properties.Name -ccontains $Name)}

$repositoryRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$registryPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.json'
$schemaPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.schema.json'
$inventoryPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-junit-inventory.json'
$inventorySchemaPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-junit-inventory.schema.json'
$staticPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-static-contracts.json'
$staticSchemaPath=Join-Path $repositoryRoot 'geocedg/specs/operations/verification-static-contracts.schema.json'
$registry=Read-VerificationJson $registryPath
$inventory=Read-VerificationJson $inventoryPath

Invoke-Case 'registry is the single executive authority and its catalogs are hash bound' {
    Assert-Case (Assert-VerificationRegistry $registry -SchemaPath $schemaPath) 'Live registry is invalid.'
    Assert-Case (-not(Test-Path (Join-Path $repositoryRoot 'geocedg/specs/operations/verification-coverage.json'))) 'Second coverage authority exists.'
    Assert-Case (-not(Test-Path (Join-Path $repositoryRoot 'geocedg/specs/operations/verification-coverage.schema.json'))) 'Second coverage schema exists.'
    foreach($catalog in $registry.catalogs){
        $full=Join-Path $repositoryRoot $catalog.path
        Assert-Case ((Get-VerificationFileSha256 $full) -ceq $catalog.sha256) "Catalog hash differs: $($catalog.catalog_id)"
    }
}
Invoke-Case 'tracked JUnit inventory is compact and selection based' {
    Assert-Case (Assert-VerificationJsonSchema $inventory $inventorySchemaPath) 'JUnit inventory schema failed.'
    Assert-Case (Assert-VerificationJsonSchema (Read-VerificationJson $staticPath) $staticSchemaPath) 'Static catalog schema failed.'
    $invalidInventory=ConvertFrom-Json (ConvertTo-Json $inventory -Depth 100) -Depth 100
    $invalidInventory.selections[0].expected_identities_sha256=$null
    $inventoryRejected=$false
    try { Assert-VerificationJsonSchema $invalidInventory $inventorySchemaPath|Out-Null }
    catch { $inventoryRejected=$true }
    Assert-Case $inventoryRejected 'JUnit inventory accepted a missing selection fingerprint.'
    Assert-Case (@($inventory.modules).Count -eq 2) 'Module discovery count changed.'
    Assert-Case (@($inventory.modules|Where-Object{$_.PSObject.Properties.Name -ccontains 'identities'}).Count -eq 0) 'Per-method inventory was tracked.'
    Assert-Case (@($inventory.selections).Count -eq 5) 'Selection inventory count changed.'
    foreach($selection in $inventory.selections){
        Assert-Case ($selection.expected_identity_count -gt 0) "Selection has zero inventory: $($selection.selection_id)"
        Assert-Case ([string]$selection.expected_identities_sha256 -cmatch '^[0-9a-f]{64}$') "Selection fingerprint is invalid: $($selection.selection_id)"
    }
    Assert-Case (@($inventory.selections|Where-Object selection_id -CEQ 'g8b.narrow')[0].expected_identity_count -eq 9) 'G8B inventory changed.'
}
Invoke-Case 'all 25 PHASE selections resolve as complete pure plans' {
    Assert-Case (@($registry.phase_selections).Count -eq 25) 'PHASE selection count changed.'
    foreach($phase in $registry.phase_selections){
        $plan=Resolve-VerificationRegistryPlan $registry PHASE $phase.phase_id WINDOWS
        Assert-Case ($plan.coverage_state -ceq 'COMPLETE') "PHASE is incomplete: $($phase.phase_id)"
        Assert-Case (@($plan.missing_check_ids).Count -eq 0) "PHASE reports missing coverage: $($phase.phase_id)"
        Assert-Case (@($plan.nodes|Where-Object{(Has-Field $_ 'contract_class') -and (Test-VerificationAcceptanceClass ([string]$_.contract_class))}).Count -gt 0) "PHASE has no acceptance coverage: $($phase.phase_id)"
        Assert-Case (@($plan.nodes.check_id|Sort-Object -Unique -CaseSensitive).Count -eq @($plan.nodes).Count) "PHASE duplicates a node: $($phase.phase_id)"
    }
}
Invoke-Case 'INTEGRATION and FINAL are complete unions of pure nodes' {
    foreach($profile in @('INTEGRATION','FINAL')){
        $plan=Resolve-VerificationRegistryPlan $registry $profile -Platform WINDOWS
        Assert-Case ($plan.coverage_state -ceq 'COMPLETE') "$profile is incomplete."
        Assert-Case (@($plan.missing_check_ids).Count -eq 0) "$profile reports missing coverage."
        Assert-Case (@($plan.nodes|Where-Object{(Has-Field $_ 'contract_class') -and $_.contract_class -is [Collections.IEnumerable] -and $_.contract_class -isnot [string]}).Count -eq 0) "$profile contains a mixed contract declaration."
        Assert-Case (@($plan.nodes|Where-Object{$_.node_kind -ceq 'PROCESS_PRODUCER' -and (Has-Field $_ 'contract_class')}).Count -eq 0) "$profile producer has a verdict class."
        $legacy=@($plan.nodes|Where-Object{(@($_.argv)-join ' ') -cmatch '[/\\]verify-[^/\\ ]+\.ps1'})
        Assert-Case ($legacy.Count -eq 0) "$profile contains a legacy mixed wrapper."
    }
}
Invoke-Case 'FINAL JUnit compilation and Python evidence boundaries are explicit' {
    $plan=Resolve-VerificationRegistryPlan $registry FINAL -Platform WINDOWS
    $junit=@($plan.nodes|Where-Object output_adapter -CEQ 'PROJECTION_JUNIT_SELECTION_V1')
    Assert-Case ($junit.Count -eq 2) 'FINAL does not have exactly two Gradle-selection JUnit contracts.'
    Assert-Case (@($junit|Where-Object contract_class -CNE 'SEMANTIC').Count -eq 0) 'JUnit projection is not semantic.'
    Assert-Case (@($plan.nodes|Where-Object{$_.check_id -like 'compile.*.semantic' -and $_.output_adapter -cne 'PROJECTION_PROCESS_EXIT_V1'}).Count -eq 0) 'Compilation is not projected separately.'
    Assert-Case (@($plan.nodes|Where-Object{$_.check_id -like 'python.*.producer'}).Count -eq 5) 'Five Python commands are not represented exactly once.'
    Assert-Case (@($plan.nodes|Where-Object{$_.check_id -like 'python.*.semantic' -and $_.output_adapter -cne 'PROJECTION_PYTHON_CHECK_V1'}).Count -eq 0) 'Python provenance projection is missing.'
    Assert-Case (@($plan.nodes|Where-Object{$_.check_id -like 'python.*.producer'}|Group-Object check_id|Where-Object Count -ne 1).Count -eq 0) 'Python producer is duplicated.'
}
Invoke-Case 'diagnostics cannot contribute to acceptance coverage' {
    $plan=Resolve-VerificationRegistryPlan $registry FINAL -Platform WINDOWS
    $diagnosticIds=[string[]]@($plan.nodes|Where-Object{(Has-Field $_ 'contract_class') -and (Test-VerificationDiagnosticClass ([string]$_.contract_class))}|ForEach-Object check_id)
    $acceptance=@($plan.nodes|Where-Object{(Has-Field $_ 'contract_class') -and (Test-VerificationAcceptanceClass ([string]$_.contract_class))})
    foreach($node in $acceptance){
        Assert-Case (@($node.dependencies|Where-Object{$diagnosticIds -ccontains $_}).Count -eq 0) "Acceptance depends on diagnostic: $($node.check_id)"
    }
}
Invoke-Case 'Checkstyle is separate diagnostic evidence' {
    $producers=@($registry.nodes|Where-Object check_id -like 'checkstyle.*.producer')
    $diagnostics=@($registry.nodes|Where-Object check_id -like 'checkstyle.*.diagnostic')
    Assert-Case ($producers.Count -eq 4) 'Checkstyle producer count differs.'
    Assert-Case ($diagnostics.Count -eq 4) 'Checkstyle diagnostic projection count differs.'
    Assert-Case (@($diagnostics|Where-Object contract_class -CNE 'STYLE_DIAGNOSTIC').Count -eq 0) 'Checkstyle entered acceptance.'
    Assert-Case (@($producers|Where-Object {$_.resource_locks -cnotcontains 'gradle-build-tree'}).Count -eq 0) 'Checkstyle lacks the Gradle resource lock.'
}
Invoke-Case 'no duration or process-control policy enters the executive registry' {
    $text=[IO.File]::ReadAllText($registryPath)
    Assert-Case (-not($text -match '(?i)timeout|watchdog|retry|kill')) 'Forbidden process-control field entered the registry.'
    Assert-Case (-not($text -match '(?i)duration_ms|performance.*(gate|warning)')) 'Telemetry entered acceptance configuration.'
}

Write-Host "verification-final-coverage.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

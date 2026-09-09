#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force
$script:VerificationResultSchemaPath = [IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot '../../geocedg/specs/operations/verification-result.schema.json'))
$script:VerificationReceiptSchemaPath = [IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot '../../geocedg/specs/operations/verification-receipt.schema.json'))

function Get-VerificationReceiptProperty {
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
    if ($Required) { throw "Missing receipt property '$Name'." }
    return $null
}

function Get-VerificationReceiptIdentityPayload {
    param([Parameter(Mandatory)] [object]$Receipt)

    return [ordered]@{
        schema_version = [int](Get-VerificationReceiptProperty $Receipt 'schema_version' -Required)
        receipt_kind = [string](Get-VerificationReceiptProperty $Receipt 'receipt_kind' -Required)
        accepted_report_hash = [string](Get-VerificationReceiptProperty $Receipt 'accepted_report_hash' -Required)
        base_commit = Get-VerificationReceiptProperty $Receipt 'base_commit'
        base_tree = Get-VerificationReceiptProperty $Receipt 'base_tree'
        candidate_commit = [string](Get-VerificationReceiptProperty $Receipt 'candidate_commit' -Required)
        candidate_tree = [string](Get-VerificationReceiptProperty $Receipt 'candidate_tree' -Required)
        execution_plan_hash = [string](Get-VerificationReceiptProperty $Receipt 'execution_plan_hash' -Required)
        checker_identity_hash = [string](Get-VerificationReceiptProperty $Receipt 'checker_identity_hash' -Required)
        environment_fingerprint = [string](Get-VerificationReceiptProperty $Receipt 'environment_fingerprint' -Required)
        input_identity_hash = [string](Get-VerificationReceiptProperty $Receipt 'input_identity_hash' -Required)
        accepted_profiles = [string[]]@(Get-VerificationReceiptProperty $Receipt 'accepted_profiles' -Required)
        diagnostic_hashes = [string[]]@(
            Get-VerificationReceiptProperty $Receipt 'diagnostic_hashes' -Required)
    }
}

function Get-VerificationReceiptReportHash {
    param([Parameter(Mandatory)] [object]$Report)

    return Get-VerificationResultIdentityHash -Report $Report
}

function New-VerificationAcceptanceReceipt {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Report,
        [Parameter(Mandatory)] [string]$CheckerIdentityHash,
        [Parameter(Mandatory)] [string]$InputIdentityHash,
        [Parameter(Mandatory)] [string[]]$AcceptedProfiles,
        [datetime]$IssuedAt = [datetime]::UtcNow
    )

    [void](Assert-VerificationJsonSchema -Value $Report -SchemaPath $script:VerificationResultSchemaPath)
    if ([string](Get-VerificationReceiptProperty $Report 'run_state' -Required) -cne 'COMPLETED') {
        throw 'No receipt may be created for an incomplete verification run.'
    }
    if ([string](Get-VerificationReceiptProperty $Report 'acceptance_verdict' -Required) -cne 'ACCEPTED') {
        throw 'No receipt may be created for rejected acceptance.'
    }
    if ([string](Get-VerificationReceiptProperty $Report 'coverage_verdict' -Required) -cne 'COMPLETE') {
        throw 'No receipt may be created for incomplete or untrusted coverage.'
    }
    $actualReportHash = [string](Get-VerificationReceiptProperty $Report 'result_hash' -Required)
    if ($actualReportHash -cne (Get-VerificationReceiptReportHash $Report)) {
        throw 'No receipt may be created from a report with an invalid result hash.'
    }
    $acceptanceResults = [object[]]@(
        Get-VerificationReceiptProperty $Report 'acceptance_results' -Required)
    $coverage = Get-VerificationReceiptProperty $Report 'coverage' -Required
    $requiredIds = [string[]]@(
        Get-VerificationReceiptProperty $coverage 'required_check_ids' -Required)
    if ($acceptanceResults.Count -eq 0 -or $requiredIds.Count -eq 0) {
        throw 'No receipt may be created without required acceptance results.'
    }
    if (@($acceptanceResults | Where-Object {
            [string](Get-VerificationReceiptProperty $_ 'status' -Required) -cne
                'CONTRACT_SATISFIED'
        }).Count -gt 0) {
        throw 'No receipt may be created unless every acceptance result is satisfied.'
    }
    $acceptanceResultIds = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($result in $acceptanceResults) {
        $resultId = [string](Get-VerificationReceiptProperty $result 'check_id' -Required)
        if ([string]::IsNullOrWhiteSpace($resultId) -or
                -not $acceptanceResultIds.Add($resultId)) {
            throw 'No receipt may be created from duplicate or empty acceptance result identifiers.'
        }
    }
    $requiredIdSet = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($requiredId in $requiredIds) {
        if ([string]::IsNullOrWhiteSpace($requiredId) -or
                -not $requiredIdSet.Add($requiredId)) {
            throw 'No receipt may be created from duplicate or empty required check identifiers.'
        }
        if (-not $acceptanceResultIds.Contains($requiredId)) {
            throw "Required acceptance result '$requiredId' is missing from the report."
        }
    }
    if ($acceptanceResultIds.Count -ne $requiredIdSet.Count) {
        throw 'Acceptance results do not exactly match the required coverage set.'
    }
    $completedIds = [string[]]@(
        Get-VerificationReceiptProperty $coverage 'completed_check_ids' -Required)
    $untrustedIds = [string[]]@(
        Get-VerificationReceiptProperty $coverage 'untrusted_check_ids' -Required)
    $notRunIds = [string[]]@(
        Get-VerificationReceiptProperty $coverage 'not_run_check_ids' -Required)
    if ($untrustedIds.Count -gt 0 -or $notRunIds.Count -gt 0) {
        throw 'Complete receipt coverage cannot contain untrusted or not-run checks.'
    }
    $completedIdSet = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($completedId in $completedIds) {
        if ([string]::IsNullOrWhiteSpace($completedId) -or
                -not $completedIdSet.Add($completedId)) {
            throw 'No receipt may be created from duplicate or empty completed check identifiers.'
        }
    }
    if ($completedIdSet.Count -ne $requiredIdSet.Count) {
        throw 'Completed coverage does not exactly match the required coverage set.'
    }
    foreach ($requiredId in $requiredIdSet) {
        if (-not $completedIdSet.Contains($requiredId)) {
            throw "Required check '$requiredId' is absent from completed coverage."
        }
    }
    if ($CheckerIdentityHash -cnotmatch '^[0-9a-f]{64}$' -or
            $InputIdentityHash -cnotmatch '^[0-9a-f]{64}$') {
        throw 'Receipt identity hashes must be lowercase SHA-256 values.'
    }
    $profileSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($profile in $AcceptedProfiles) {
        if (-not [string]::IsNullOrWhiteSpace($profile)) { [void]$profileSet.Add($profile) }
    }
    $profiles = [string[]]@($profileSet)
    [Array]::Sort($profiles, [StringComparer]::Ordinal)
    if ($profiles.Count -eq 0) { throw 'A receipt must name at least one accepted profile.' }
    $reportProfile = [string](Get-VerificationReceiptProperty $Report 'profile' -Required)
    if ($profiles -cnotcontains $reportProfile) {
        throw "Receipt profiles do not include the accepted report profile $reportProfile."
    }
    $diagnosticHashes = [Collections.Generic.List[string]]::new()
    foreach ($diagnostic in [object[]]@(Get-VerificationReceiptProperty $Report 'diagnostic_findings' -Required)) {
        $diagnosticHashes.Add((Get-VerificationObservationIdentityHash -Observation $diagnostic))
    }
    $diagnosticHashValues = [string[]]$diagnosticHashes.ToArray()
    [Array]::Sort($diagnosticHashValues, [StringComparer]::Ordinal)
    $receipt = [ordered]@{
        '$schema' = 'geocedg/specs/operations/verification-receipt.schema.json'
        schema_version = 1
        receipt_kind = 'GEOCEDG_ACCEPTANCE_RECEIPT'
        receipt_id = ''
        accepted_report_hash = [string](Get-VerificationReceiptProperty $Report 'result_hash' -Required)
        base_commit = Get-VerificationReceiptProperty $Report 'base_commit'
        base_tree = Get-VerificationReceiptProperty $Report 'base_tree'
        candidate_commit = [string](Get-VerificationReceiptProperty $Report 'candidate_commit' -Required)
        candidate_tree = [string](Get-VerificationReceiptProperty $Report 'candidate_tree' -Required)
        execution_plan_hash = [string](Get-VerificationReceiptProperty $Report 'execution_plan_hash' -Required)
        checker_identity_hash = $CheckerIdentityHash
        environment_fingerprint = [string](
            Get-VerificationReceiptProperty $Report 'environment_fingerprint' -Required)
        input_identity_hash = $InputIdentityHash
        accepted_profiles = $profiles
        diagnostic_hashes = $diagnosticHashValues
        issued_at = $IssuedAt.ToUniversalTime().ToString('o')
    }
    $receipt.receipt_id = Get-VerificationDeterministicHash -Value (
        Get-VerificationReceiptIdentityPayload -Receipt $receipt)
    return [pscustomobject]$receipt
}

function Assert-VerificationAcceptanceReceipt {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [string]$SchemaPath
    )

    $effectiveSchemaPath = if ([string]::IsNullOrWhiteSpace($SchemaPath)) {
        $script:VerificationReceiptSchemaPath
    } else {
        $SchemaPath
    }
    [void](Assert-VerificationJsonSchema -Value $Receipt -SchemaPath $effectiveSchemaPath)
    if ([string](Get-VerificationReceiptProperty $Receipt 'receipt_kind' -Required) -cne
            'GEOCEDG_ACCEPTANCE_RECEIPT') {
        throw 'Unexpected verification receipt kind.'
    }
    $expectedId = Get-VerificationDeterministicHash -Value (
        Get-VerificationReceiptIdentityPayload -Receipt $Receipt)
    $actualId = [string](Get-VerificationReceiptProperty $Receipt 'receipt_id' -Required)
    if ($actualId -cne $expectedId) { throw 'Verification receipt identity hash is invalid.' }
    return $true
}

function Test-VerificationReceiptIdentity {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [object]$ExpectedIdentity
    )

    $fields = @(
        'base_commit',
        'base_tree',
        'candidate_commit',
        'candidate_tree',
        'execution_plan_hash',
        'checker_identity_hash',
        'environment_fingerprint',
        'input_identity_hash'
    )
    $mismatches = [Collections.Generic.List[string]]::new()
    foreach ($field in $fields) {
        $actual = Get-VerificationReceiptProperty $Receipt $field
        $expected = Get-VerificationReceiptProperty $ExpectedIdentity $field -Required
        if ([string]$actual -cne [string]$expected) { $mismatches.Add($field) }
    }
    return [pscustomobject]@{
        valid = $mismatches.Count -eq 0
        mismatches = [string[]]$mismatches.ToArray()
    }
}

function Write-VerificationAcceptanceReceipt {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Receipt,
        [string]$SchemaPath
    )
    $fullPath = [IO.Path]::GetFullPath($Path)
    if (Test-Path -LiteralPath $fullPath) {
        throw "Acceptance receipt output must be initially absent: $fullPath"
    }
    [void](Assert-VerificationAcceptanceReceipt -Receipt $Receipt -SchemaPath $SchemaPath)
    [void](Write-VerificationAtomicJson -Path $fullPath -Value $Receipt)
    return $fullPath
}

Export-ModuleMember -Function @(
    'New-VerificationAcceptanceReceipt',
    'Assert-VerificationAcceptanceReceipt',
    'Test-VerificationReceiptIdentity',
    'Write-VerificationAcceptanceReceipt'
)

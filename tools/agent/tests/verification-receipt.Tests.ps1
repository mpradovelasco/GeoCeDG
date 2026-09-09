#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-receipt.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$schemaPath = Join-Path $PSScriptRoot '../../../geocedg/specs/operations/verification-receipt.schema.json'
$identity = [pscustomobject]@{
    base_commit = ('1' * 40)
    base_tree = ('2' * 40)
    candidate_commit = ('3' * 40)
    candidate_tree = ('4' * 40)
    environment_fingerprint = ('5' * 64)
}
$planHash = '6' * 64
$checkerHash = '7' * 64
$inputHash = '8' * 64

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
function New-Report {
    param(
        [string]$AcceptanceStatus = 'CONTRACT_SATISFIED',
        [string]$DiagnosticOutcome = 'DIAGNOSTIC_FINDING'
    )
    $acceptance = [pscustomobject][ordered]@{
        check_id = 'product.contract'
        node_kind = 'ACCEPTANCE_LEAF'
        contract_class = 'SEMANTIC'
        semantic_domain = 'PRODUCT'
        status = $AcceptanceStatus
        command_identity = ('9' * 64)
        exit_code = $(if ($AcceptanceStatus -ceq 'CONTRACT_SATISFIED') { 0 } else { 1 })
        cause = $null
        evidence = [object[]]@()
        dependencies = [string[]]@()
        duration_ms = 1.0
    }
    $diagnostic = [pscustomobject][ordered]@{
        check_id = 'style.diagnostic'
        node_kind = 'DIAGNOSTIC_LEAF'
        contract_class = 'STYLE_DIAGNOSTIC'
        outcome = $DiagnosticOutcome
        command_identity = ('a' * 64)
        exit_code = 5
        cause = 'fixture diagnostic'
        evidence = [object[]]@()
        dependencies = [string[]]@()
        duration_ms = 2.0
    }
    return New-VerificationAggregatedReport -RunId receipt -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash -AcceptanceResults @($acceptance) -DiagnosticResults @($diagnostic) -RequiredAcceptanceIds @('product.contract')
}

Invoke-Case 'accepted report with diagnostics produces a valid receipt' {
    $report = New-Report
    $receipt = New-VerificationAcceptanceReceipt -Report $report -CheckerIdentityHash $checkerHash -InputIdentityHash $inputHash -AcceptedProfiles @('FINAL')
    Assert-Case ($receipt.diagnostic_hashes.Count -eq 1) 'Diagnostic trace hash was omitted.'
    Assert-Case ($receipt.semantic_domains.Count -eq 1 -and
        $receipt.semantic_domains[0] -ceq 'PRODUCT') 'Semantic domain identity was omitted.'
    Assert-Case (Assert-VerificationAcceptanceReceipt $receipt -SchemaPath $schemaPath) 'Receipt validation failed.'
    $expected = [pscustomobject]@{
        base_commit = $identity.base_commit
        base_tree = $identity.base_tree
        candidate_commit = $identity.candidate_commit
        candidate_tree = $identity.candidate_tree
        execution_plan_hash = $planHash
        checker_identity_hash = $checkerHash
        environment_fingerprint = $identity.environment_fingerprint
        input_identity_hash = $inputHash
    }
    $match = Test-VerificationReceiptIdentity $receipt $expected
    Assert-Case ($match.valid) 'Exact receipt identity did not match.'
    Assert-Case ($receipt.receipt_id -cmatch '^[0-9a-f]{64}$') 'Receipt ID is invalid.'
}
Invoke-Case 'receipt identity excludes informational dates' {
    $report = New-Report
    $first = New-VerificationAcceptanceReceipt $report $checkerHash $inputHash @('FINAL') ([datetime]'2026-01-01T00:00:00Z')
    $second = New-VerificationAcceptanceReceipt $report $checkerHash $inputHash @('FINAL') ([datetime]'2027-01-01T00:00:00Z')
    Assert-Case ($first.receipt_id -ceq $second.receipt_id) 'Issue date changed receipt identity.'
}
Invoke-Case 'diagnostic trace identity excludes task locations and host wording' {
    $firstReport = New-Report
    $secondReport = New-Report
    $firstReport.diagnostic_findings[0].evidence = @([pscustomobject]@{
        name = 'DETAIL'; state = 'PRESENT'; path = 'C:\task-one\detail'; sha256 = ('b' * 64)
    })
    $secondReport.diagnostic_findings[0].evidence = @([pscustomobject]@{
        name = 'DETAIL'; state = 'PRESENT'; path = 'D:\task-two\detail'; sha256 = ('b' * 64)
    })
    $firstReport.diagnostic_findings[0].cause = 'detail at C:\task-one'
    $secondReport.diagnostic_findings[0].cause = 'detail at D:\task-two'
    $firstReport.result_hash = Get-VerificationResultHash $firstReport
    $secondReport.result_hash = Get-VerificationResultHash $secondReport
    $first = New-VerificationAcceptanceReceipt $firstReport $checkerHash $inputHash @('FINAL')
    $second = New-VerificationAcceptanceReceipt $secondReport $checkerHash $inputHash @('FINAL')
    Assert-Case ($firstReport.result_hash -ceq $secondReport.result_hash) 'Host-specific diagnostic wording changed report identity.'
    Assert-Case ($first.receipt_id -ceq $second.receipt_id) 'Task-owned diagnostic location changed receipt identity.'
}
Invoke-Case 'rejected incomplete and untrusted reports cannot produce receipts' {
    $rejected = New-Report CONTRACT_VIOLATED
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $rejected $checkerHash $inputHash @('FINAL')
    } 'rejected'
    $untrusted = New-Report EVIDENCE_UNTRUSTED
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $untrusted $checkerHash $inputHash @('FINAL')
    } 'rejected|untrusted'
    $interrupted = New-Report
    $interrupted.run_state = 'RUN_INTERRUPTED'
    $interrupted.acceptance_verdict = 'NOT_PRODUCED'
    $interrupted.coverage_verdict = 'INCOMPLETE'
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $interrupted $checkerHash $inputHash @('FINAL')
    } 'incomplete'
}
Invoke-Case 'every acceptance identity field invalidates reuse' {
    $receipt = New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('FINAL')
    $expected = [ordered]@{
        base_commit = $identity.base_commit
        base_tree = $identity.base_tree
        candidate_commit = $identity.candidate_commit
        candidate_tree = $identity.candidate_tree
        execution_plan_hash = $planHash
        checker_identity_hash = $checkerHash
        environment_fingerprint = $identity.environment_fingerprint
        input_identity_hash = $inputHash
    }
    foreach ($field in @('base_commit', 'base_tree', 'candidate_commit', 'candidate_tree', 'execution_plan_hash',
            'checker_identity_hash', 'environment_fingerprint', 'input_identity_hash')) {
        $changed = [ordered]@{}
        foreach ($name in $expected.Keys) { $changed[$name] = $expected[$name] }
        $length = ([string]$changed[$field]).Length
        $changed[$field] = '0' * $length
        $result = Test-VerificationReceiptIdentity $receipt ([pscustomobject]$changed)
        Assert-Case (-not $result.valid) "Receipt stayed valid after $field changed."
        Assert-Case ($result.mismatches -ccontains $field) "Receipt did not identify $field mismatch."
    }
}
Invoke-Case 'receipt creation rejects forged or acceptance-free reports' {
    $forgedHash = New-Report
    $forgedHash.result_hash = '0' * 64
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $forgedHash $checkerHash $inputHash @('FINAL')
    } 'invalid result hash'

    $forgedVerdict = New-Report
    $forgedVerdict.acceptance_results[0].status = 'CONTRACT_VIOLATED'
    $forgedVerdict.result_hash = Get-VerificationResultHash $forgedVerdict
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $forgedVerdict $checkerHash $inputHash @('FINAL')
    } 'every acceptance result'

    $empty = New-VerificationAggregatedReport -RunId empty -Profile FINAL -Identity $identity -ExecutionPlanHash $planHash
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $empty $checkerHash $inputHash @('FINAL')
    } 'without required acceptance'

    $missingResult = New-Report
    $missingResult.coverage.required_check_ids = @('missing.contract')
    $missingResult.coverage.completed_check_ids = @('missing.contract')
    $missingResult.result_hash = Get-VerificationResultHash $missingResult
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $missingResult $checkerHash $inputHash @('FINAL')
    } 'missing from the report'

    $incoherentCoverage = New-Report
    $incoherentCoverage.coverage.untrusted_check_ids = @('product.contract')
    $incoherentCoverage.result_hash = Get-VerificationResultHash $incoherentCoverage
    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt $incoherentCoverage $checkerHash $inputHash @('FINAL')
    } 'cannot contain untrusted or not-run'

    Assert-CaseThrows {
        New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('WORKSTATION')
    } 'do not include'
}
Invoke-Case 'diagnostic trace hashes are bound but never block acceptance' {
    $receipt = New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('FINAL')
    $receipt.diagnostic_hashes[0] = '0' * 64
    Assert-CaseThrows { Assert-VerificationAcceptanceReceipt $receipt } 'identity hash'
}
Invoke-Case 'semantic domains are visible and bound to receipt identity' {
    $receipt = New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('FINAL')
    $receipt.semantic_domains[0] = 'SCIENTIFIC'
    Assert-CaseThrows { Assert-VerificationAcceptanceReceipt $receipt } 'identity hash'
}
Invoke-Case 'tampered receipt ID is rejected' {
    $receipt = New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('FINAL')
    $receipt.receipt_id = '0' * 64
    Assert-CaseThrows { Assert-VerificationAcceptanceReceipt $receipt } 'identity hash'
}
Invoke-Case 'receipt writing is atomic and refuses replacement' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-verification-receipt-' + [guid]::NewGuid().ToString('N'))
    [void][IO.Directory]::CreateDirectory($root)
    $marker = Join-Path $root '.fixture-owner'
    [IO.File]::WriteAllText($marker, 'verification-receipt', [Text.UTF8Encoding]::new($false))
    try {
        $path = Join-Path $root 'acceptance-receipt.json'
        $receipt = New-VerificationAcceptanceReceipt (New-Report) $checkerHash $inputHash @('FINAL')
        $written = Write-VerificationAcceptanceReceipt $path $receipt
        Assert-Case ($written -ceq [IO.Path]::GetFullPath($path)) 'Receipt writer changed its target.'
        $saved = ConvertFrom-Json -InputObject ([IO.File]::ReadAllText($path)) -Depth 100
        Assert-Case ($saved.receipt_id -ceq $receipt.receipt_id) 'Written receipt identity changed.'
        Assert-CaseThrows { Write-VerificationAcceptanceReceipt $path $receipt } 'initially absent'
    } finally {
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker -PathType Leaf) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-receipt') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else {
            throw "Fixture cleanup refused unexpected path: $resolved"
        }
    }
}

Write-Host "verification-receipt.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

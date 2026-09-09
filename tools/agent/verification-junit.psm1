#requires -Version 7.2
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

function Read-VerificationJUnitFiles {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string[]]$Path)

    $cases = [Collections.Generic.List[object]]::new()
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($candidate in [string[]]@($Path | Sort-Object -CaseSensitive)) {
        $full = [IO.Path]::GetFullPath($candidate)
        if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
            throw [IO.InvalidDataException]::new("JUnit XML is absent: $full")
        }
        $settings = [Xml.XmlReaderSettings]::new()
        $settings.DtdProcessing = [Xml.DtdProcessing]::Prohibit
        $settings.XmlResolver = $null
        $settings.CheckCharacters = $true
        $stream = [IO.FileStream]::new($full, [IO.FileMode]::Open,
            [IO.FileAccess]::Read, [IO.FileShare]::Read)
        try {
            $reader = [IO.StreamReader]::new($stream,
                [Text.UTF8Encoding]::new($false, $true), $false)
            try {
                $xmlReader = [Xml.XmlReader]::Create($reader, $settings)
                try {
                    $document = [Xml.XmlDocument]::new()
                    $document.XmlResolver = $null
                    $document.Load($xmlReader)
                } finally { $xmlReader.Dispose() }
            } finally { $reader.Dispose() }
        } catch {
            throw [IO.InvalidDataException]::new("JUnit XML is not trusted UTF-8 XML: $full", $_.Exception)
        } finally { $stream.Dispose() }
        foreach ($testcase in @($document.SelectNodes("//*[local-name()='testcase']"))) {
            $className = [string]$testcase.GetAttribute('classname')
            $testName = [string]$testcase.GetAttribute('name')
            if ([string]::IsNullOrWhiteSpace($className) -or
                    [string]::IsNullOrWhiteSpace($testName)) {
                throw [IO.InvalidDataException]::new("JUnit testcase lacks classname or name: $full")
            }
            $identity = $className + '::' + $testName
            if (-not $seen.Add($identity)) {
                throw [IO.InvalidDataException]::new("Duplicate JUnit identity: $identity")
            }
            $failure = @($testcase.SelectNodes("./*[local-name()='failure']"))
            $error = @($testcase.SelectNodes("./*[local-name()='error']"))
            $skipped = @($testcase.SelectNodes("./*[local-name()='skipped']"))
            $state = if ($failure.Count -gt 0) { 'FAILED' }
                elseif ($error.Count -gt 0) { 'ERRORED' }
                elseif ($skipped.Count -gt 0) { 'SKIPPED_LIKE' }
                else { 'PASSED' }
            $cases.Add([pscustomobject][ordered]@{
                identity = $identity
                class_name = $className
                test_name = $testName
                state = $state
                file = $full
                failure_type = $(if ($failure.Count -gt 0) { [string]$failure[0].GetAttribute('type') }
                    elseif ($error.Count -gt 0) { [string]$error[0].GetAttribute('type') } else { $null })
                failure_message = $(if ($failure.Count -gt 0) { [string]$failure[0].GetAttribute('message') }
                    elseif ($error.Count -gt 0) { [string]$error[0].GetAttribute('message') } else { $null })
            })
        }
    }
    return [object[]]$cases.ToArray()
}

function Get-VerificationJUnitIdentityHash {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object[]]$Cases)
    $identities = [string[]]@($Cases | ForEach-Object { [string]$_.identity })
    [Array]::Sort($identities, [StringComparer]::Ordinal)
    return Get-VerificationDeterministicHash -Value ([string[]]$identities)
}

function ConvertFrom-VerificationJUnitSelection {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$ProcessEvidence,
        [Parameter(Mandatory)] [object]$Selection,
        [Parameter(Mandatory)] [ValidateSet('PRODUCT','SCIENTIFIC','MIXED')]
        [string]$SemanticDomain
    )
    $result = [ordered]@{
        contract_class = 'SEMANTIC'
        semantic_domain = $SemanticDomain
        outcome = 'EVIDENCE_UNTRUSTED'
        cause = $null
        selection_id = [string]$Selection.selection_id
        expected_count = [int]$Selection.expected_identity_count
        observed_count = 0
        observed_identity_sha256 = $null
        cases = [object[]]@()
    }
    try {
        if ([string]$ProcessEvidence.completion_state -cne 'COMPLETED' -or
                $null -eq $ProcessEvidence.inner_exit_code) {
            throw 'Gradle producer did not preserve complete process evidence.'
        }
        $paths = [string[]]@($ProcessEvidence.junit_files | ForEach-Object { [string]$_.path })
        if ($paths.Count -eq 0) { throw 'Gradle producer emitted no JUnit XML.' }
        $cases = [object[]]@(Read-VerificationJUnitFiles -Path $paths)
        if ($cases.Count -eq 0) {
            $result.outcome = 'EVIDENCE_UNTRUSTED'
            $result.cause = 'The declared Gradle selection matched zero tests.'
            return [pscustomobject]$result
        }
        $hash = Get-VerificationJUnitIdentityHash -Cases $cases
        $result.observed_count = $cases.Count
        $result.observed_identity_sha256 = $hash
        if ($cases.Count -ne [int]$Selection.expected_identity_count -or
                $hash -cne [string]$Selection.expected_identities_sha256) {
            $result.outcome = 'EVIDENCE_UNTRUSTED'
            $result.cause = 'Executed JUnit identities differ from the tracked selection inventory.'
            return [pscustomobject]$result
        }
        $allow = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
        foreach ($identity in [object[]]$Selection.not_applicable_allowlist) {
            [void]$allow.Add([string]$identity)
        }
        $projectedCases = foreach ($case in $cases) {
            $caseOutcome = switch ([string]$case.state) {
                'PASSED' { 'CONTRACT_SATISFIED' }
                'FAILED' { 'CONTRACT_VIOLATED' }
                'ERRORED' { 'CONTRACT_VIOLATED' }
                'SKIPPED_LIKE' {
                    if ($allow.Contains([string]$case.identity)) { 'NOT_APPLICABLE' }
                    else { 'COVERAGE_INCOMPLETE' }
                }
            }
            [pscustomobject][ordered]@{
                identity = [string]$case.identity
                class_name = [string]$case.class_name
                test_name = [string]$case.test_name
                evidence_state = [string]$case.state
                contract_outcome = $caseOutcome
                failure_type = $case.failure_type
                failure_message = $case.failure_message
            }
        }
        $result.cases = [object[]]$projectedCases
        $requiredSkipped = @($cases | Where-Object {
            $_.state -ceq 'SKIPPED_LIKE' -and -not $allow.Contains([string]$_.identity)
        })
        if ($requiredSkipped.Count -gt 0) {
            $result.outcome = 'NOT_RUN_DEPENDENCY'
            $result.cause = 'A required JUnit identity was skipped or aborted.'
            return [pscustomobject]$result
        }
        $violations = @($cases | Where-Object { $_.state -cin @('FAILED','ERRORED') })
        if ($violations.Count -gt 0) {
            $result.outcome = 'CONTRACT_VIOLATED'
            $result.cause = "$($violations.Count) structured JUnit case(s) failed or errored."
            return [pscustomobject]$result
        }
        if ([int]$ProcessEvidence.inner_exit_code -ne 0) {
            $result.outcome = 'EVIDENCE_UNTRUSTED'
            $result.cause = 'Gradle exited nonzero although complete JUnit evidence contains no violation.'
            return [pscustomobject]$result
        }
        $result.outcome = 'CONTRACT_SATISFIED'
        return [pscustomobject]$result
    } catch {
        $result.outcome = 'EVIDENCE_UNTRUSTED'
        $result.cause = $_.Exception.Message
        return [pscustomobject]$result
    }
}

Export-ModuleMember -Function @(
    'Read-VerificationJUnitFiles',
    'Get-VerificationJUnitIdentityHash',
    'ConvertFrom-VerificationJUnitSelection'
)

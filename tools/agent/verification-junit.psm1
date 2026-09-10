#requires -Version 7.2
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

function Read-VerificationJUnitFiles {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string[]]$Path,
        [Parameter(Mandatory)] [ValidateSet('shared', 'desktop')]
        [string]$Module
    )

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
        $testcases = [object[]]@($document.SelectNodes("//*[local-name()='testcase']"))
        $occurrenceCounts = [Collections.Generic.Dictionary[string,int]]::new(
            [StringComparer]::Ordinal)
        foreach ($testcase in $testcases) {
            $className = [string]$testcase.GetAttribute('classname')
            $testName = [string]$testcase.GetAttribute('name')
            if ([string]::IsNullOrWhiteSpace($className) -or
                    [string]::IsNullOrWhiteSpace($testName)) {
                throw [IO.InvalidDataException]::new("JUnit testcase lacks classname or name: $full")
            }
            $key = $className + [char]0 + $testName
            if (-not $occurrenceCounts.ContainsKey($key)) { $occurrenceCounts[$key] = 0 }
            $occurrenceCounts[$key]++
        }
        $occurrenceOrdinals = [Collections.Generic.Dictionary[string,int]]::new(
            [StringComparer]::Ordinal)
        foreach ($testcase in $testcases) {
            $className = [string]$testcase.GetAttribute('classname')
            $testName = [string]$testcase.GetAttribute('name')
            $key = $className + [char]0 + $testName
            if (-not $occurrenceOrdinals.ContainsKey($key)) { $occurrenceOrdinals[$key] = 0 }
            $occurrenceOrdinals[$key]++
            $identity = $Module + '::' + $className + '::' + $testName +
                '::invocation[' + $occurrenceOrdinals[$key] + '/' +
                $occurrenceCounts[$key] + ']'
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

function Get-VerificationGitBlobIdentity {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Path
    )

    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $full = [IO.Path]::GetFullPath($Path)
    $relative = [IO.Path]::GetRelativePath($root, $full).Replace('\', '/')
    $output = [string[]]@(& git -C $root hash-object -- $relative)
    if ($LASTEXITCODE -ne 0 -or $output.Count -ne 1 -or
            $output[0] -cnotmatch '^[0-9a-f]{40}$') {
        throw "Unable to calculate the filtered Git blob identity for: $relative"
    }
    return $output[0]
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
        coverage_state = 'UNTRUSTED'
        cases = [object[]]@()
    }
    try {
        if ([string]$ProcessEvidence.completion_state -cne 'COMPLETED' -or
                $null -eq $ProcessEvidence.inner_exit_code) {
            throw 'Gradle producer did not preserve complete process evidence.'
        }
        $paths = [string[]]@($ProcessEvidence.junit_files | ForEach-Object { [string]$_.path })
        if ($paths.Count -eq 0) { throw 'Gradle producer emitted no JUnit XML.' }
        $cases = [object[]]@(Read-VerificationJUnitFiles -Path $paths `
            -Module ([string]$Selection.module))
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
        $repositoryRoot = [IO.Path]::GetFullPath([string]$ProcessEvidence.working_directory)
        foreach ($entry in [object[]]$Selection.not_applicable_allowlist) {
            $identity = [string]$entry.identity
            if (-not $allow.Add($identity)) {
                throw "Duplicate not-applicable JUnit identity: $identity"
            }
            $sourcePath = Resolve-VerificationContainedPath -Root $repositoryRoot `
                -Path ([string]$entry.source_path)
            if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
                throw "Not-applicable JUnit source is absent: $sourcePath"
            }
            if ((Get-VerificationFileSha256 -Path $sourcePath) -cne
                    [string]$entry.source_sha256 -or
                    (Get-VerificationGitBlobIdentity -RepositoryRoot $repositoryRoot `
                        -Path $sourcePath) -cne
                    [string]$entry.source_blob) {
                throw "Not-applicable JUnit source identity changed: $identity"
            }
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
        $violations = @($cases | Where-Object { $_.state -cin @('FAILED','ERRORED') })
        $result.coverage_state = $(if ($requiredSkipped.Count -gt 0) {
                'INCOMPLETE'
            } else { 'COMPLETE' })
        if ($violations.Count -gt 0) {
            $result.outcome = 'CONTRACT_VIOLATED'
            $result.cause = "$($violations.Count) structured JUnit case(s) failed or errored."
            if ($requiredSkipped.Count -gt 0) {
                $result.cause += " $($requiredSkipped.Count) required JUnit identity or identities were skipped or aborted."
            }
            return [pscustomobject]$result
        }
        if ($requiredSkipped.Count -gt 0) {
            $result.outcome = 'CONTRACT_SATISFIED'
            $result.cause = 'A required JUnit identity was skipped or aborted.'
            return [pscustomobject]$result
        }
        if ([int]$ProcessEvidence.inner_exit_code -ne 0) {
            $result.outcome = 'EVIDENCE_UNTRUSTED'
            $result.coverage_state = 'UNTRUSTED'
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

function ConvertFrom-VerificationJUnitDiagnosticSelection {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$ProcessEvidence,
        [Parameter(Mandatory)] [object]$Selection,
        [Parameter(Mandatory)]
        [ValidateSet('GOVERNANCE_DIAGNOSTIC','DOCUMENTATION_DIAGNOSTIC',
            'HISTORICAL_CONSISTENCY_DIAGNOSTIC','STYLE_DIAGNOSTIC',
            'PERFORMANCE_TELEMETRY')]
        [string]$DiagnosticClass
    )

    # Reuse the strict XML, inventory, and source-bound allowlist boundary. The
    # intermediate semantic domain is intentionally discarded; this public
    # result is diagnostic-only and cannot affect acceptance or coverage.
    $projected = ConvertFrom-VerificationJUnitSelection `
        -ProcessEvidence $ProcessEvidence -Selection $Selection `
        -SemanticDomain PRODUCT
    $findingCases = [object[]]@($projected.cases | Where-Object {
        [string]$_.contract_outcome -ceq 'CONTRACT_VIOLATED'
    })
    $outcome = if ([string]$projected.outcome -ceq 'EVIDENCE_UNTRUSTED' -or
            [string]$projected.coverage_state -cne 'COMPLETE') {
        'DIAGNOSTIC_UNAVAILABLE'
    } elseif ($findingCases.Count -gt 0) {
        'DIAGNOSTIC_FINDING'
    } else {
        'DIAGNOSTIC_CLEAR'
    }
    return [pscustomobject][ordered]@{
        contract_class = $DiagnosticClass
        diagnostic_outcome = $outcome
        cause = [string]$projected.cause
        selection_id = [string]$projected.selection_id
        observed_count = [int]$projected.observed_count
        observed_identity_sha256 = [string]$projected.observed_identity_sha256
        findings = [object[]]$findingCases
        cases = [object[]]$projected.cases
    }
}

Export-ModuleMember -Function @(
    'Read-VerificationJUnitFiles',
    'Get-VerificationJUnitIdentityHash',
    'ConvertFrom-VerificationJUnitSelection',
    'ConvertFrom-VerificationJUnitDiagnosticSelection'
)

#requires -Version 7.2
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function ConvertFrom-VerificationProcessEvidence {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)]
        [ValidateSet(
            'SEMANTIC', 'SAFETY', 'VERIFICATION_CORE',
            'GOVERNANCE_DIAGNOSTIC', 'DOCUMENTATION_DIAGNOSTIC',
            'HISTORICAL_CONSISTENCY_DIAGNOSTIC', 'STYLE_DIAGNOSTIC',
            'PERFORMANCE_TELEMETRY')]
        [string]$ContractClass,
        [ValidateSet('PRODUCT', 'SCIENTIFIC', 'MIXED')]
        [string]$SemanticDomain
    )

    if ($ContractClass -ceq 'SEMANTIC' -and
            [string]::IsNullOrWhiteSpace($SemanticDomain)) {
        throw 'SEMANTIC process projection requires semantic_domain.'
    }
    if ($ContractClass -cne 'SEMANTIC' -and
            -not [string]::IsNullOrWhiteSpace($SemanticDomain)) {
        throw 'semantic_domain is valid only for SEMANTIC process projections.'
    }
    $isDiagnostic = $ContractClass -in @(
        'GOVERNANCE_DIAGNOSTIC', 'DOCUMENTATION_DIAGNOSTIC',
        'HISTORICAL_CONSISTENCY_DIAGNOSTIC', 'STYLE_DIAGNOSTIC',
        'PERFORMANCE_TELEMETRY')
    if ([string]$Evidence.completion_state -cne 'COMPLETED' -or
            $null -eq $Evidence.inner_exit_code) {
        return [pscustomobject][ordered]@{
            contract_class = $ContractClass
            semantic_domain = $(if ($ContractClass -ceq 'SEMANTIC') { $SemanticDomain } else { $null })
            outcome = $(if ($isDiagnostic) { 'DIAGNOSTIC_UNAVAILABLE' } else {
                    'EVIDENCE_UNTRUSTED'
                })
            cause = 'The declared process did not produce complete exit evidence.'
        }
    }
    return [pscustomobject][ordered]@{
        contract_class = $ContractClass
        semantic_domain = $(if ($ContractClass -ceq 'SEMANTIC') { $SemanticDomain } else { $null })
        outcome = $(if ($isDiagnostic) {
                if ([int]$Evidence.inner_exit_code -eq 0) {
                    'DIAGNOSTIC_CLEAR'
                } else { 'DIAGNOSTIC_FINDING' }
            } elseif ([int]$Evidence.inner_exit_code -eq 0) {
                'CONTRACT_SATISFIED'
            } else { 'CONTRACT_VIOLATED' })
        cause = $(if ([int]$Evidence.inner_exit_code -eq 0) { $null } else {
                "Declared process exited with code $($Evidence.inner_exit_code)."
            })
    }
}

function ConvertFrom-VerificationPythonCheckEvidence {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [ValidateSet('SCIENTIFIC','MIXED')]
        [string]$SemanticDomain
    )

    $result = [ordered]@{
        contract_class = 'SEMANTIC'
        semantic_domain = $SemanticDomain
        outcome = 'EVIDENCE_UNTRUSTED'
        cause = $null
        identity = $null
    }
    try {
        if ([string]$Evidence.completion_state -cne 'COMPLETED' -or
                $null -eq $Evidence.inner_exit_code -or
                $null -eq $Evidence.identity_exit_code) {
            throw 'Python producer did not preserve complete process evidence.'
        }
        if ([int]$Evidence.identity_exit_code -ne 0 -or
                [string]$Evidence.identity_state -cne 'PRESENT' -or
                $null -eq $Evidence.identity) {
            throw 'Python producer did not preserve trusted interpreter identity evidence.'
        }
        if ([string]$Evidence.environment_name -cne 'cedg_env') {
            throw 'Python producer did not declare cedg_env.'
        }
        if ($Evidence.conda_inventory_member -ne $true -or
                [string]::IsNullOrWhiteSpace([string]$Evidence.conda_inventory_path)) {
            throw 'Python producer did not prove read-only Conda inventory membership.'
        }
        $identity = $Evidence.identity
        $result.identity = $identity
        if ([string]$identity.python_implementation -cne 'CPython' -or
                [string]$identity.python_version -cne '3.12.13' -or
                [string]$identity.mpmath_version -cne '1.4.1' -or
                [string]$identity.conda_default_env -cne 'cedg_env') {
            throw 'Python or mpmath identity does not satisfy the cedg_env contract.'
        }
        $prefix = [IO.Path]::GetFullPath([string]$identity.sys_prefix)
        $condaPrefix = [IO.Path]::GetFullPath([string]$identity.conda_prefix)
        $producerPrefix = [IO.Path]::GetFullPath([string]$Evidence.environment_prefix)
        if (-not $prefix.Equals($condaPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                -not $prefix.Equals($producerPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                [IO.Path]::GetFileName($prefix) -cne 'cedg_env') {
            throw 'CONDA_PREFIX and sys.prefix do not identify cedg_env.'
        }
        $prefixBoundary = $prefix.TrimEnd('\','/') + [IO.Path]::DirectorySeparatorChar
        foreach ($candidate in @([string]$identity.executable, [string]$identity.mpmath_file)) {
            $resolved = [IO.Path]::GetFullPath($candidate)
            if (-not $resolved.StartsWith($prefixBoundary, [StringComparison]::OrdinalIgnoreCase)) {
                throw 'Python executable or mpmath import is outside cedg_env.'
            }
        }
        if (-not ([IO.Path]::GetFullPath([string]$Evidence.command)).Equals(
                [IO.Path]::GetFullPath([string]$identity.executable),
                [StringComparison]::OrdinalIgnoreCase)) {
            throw 'The executed Python does not match the identified cedg_env interpreter.'
        }
        if ([int]$Evidence.inner_exit_code -eq 0) {
            $result.outcome = 'CONTRACT_SATISFIED'
        } else {
            $result.outcome = 'CONTRACT_VIOLATED'
            $result.cause = "Python --check exited with code $($Evidence.inner_exit_code)."
        }
    } catch {
        $result.outcome = 'EVIDENCE_UNTRUSTED'
        $result.cause = $_.Exception.Message
    }
    return [pscustomobject]$result
}

Export-ModuleMember -Function @(
    'ConvertFrom-VerificationProcessEvidence',
    'ConvertFrom-VerificationPythonCheckEvidence'
)

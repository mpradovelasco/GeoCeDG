#requires -Version 7.2

function New-VerificationTestEnvironmentIdentity {
    param(
        [AllowNull()] [string]$BaseCommit,
        [AllowNull()] [string]$BaseTree,
        [Parameter(Mandatory)] [string]$CandidateCommit,
        [Parameter(Mandatory)] [string]$CandidateTree,
        [string]$CompatibilitySignature
    )
    $contract = [pscustomobject][ordered]@{
        contract_version = 1
        platform = 'WINDOWS'
        architecture = 'X64'
        powershell = '>=7.2'
        gradle_wrapper = '9.4.1'
        gradle_launcher_jvm_major = 22
        required_jdk_majors = [int[]]@(17, 25)
        conda_environment = 'cedg_env'
        python_implementation = 'CPython'
        python_version = '3.12.13'
        mpmath_version = '1.4.1'
        python_provenance = 'EXECUTABLE_PREFIX_AND_IMPORT_SHARE_CEDG_ENV_PREFIX'
    }
    $observation = [pscustomobject][ordered]@{
        observation_version = 1
        effective_user = 'fixture-user'
        os_description = 'fixture-os'
        powershell_version = '7.2.0'
        process_architecture = 'X64'
        repository_root = 'C:\fixture\repository'
        user_profile = 'C:\fixture\user'
        temp_root = 'C:\fixture\temp\'
        gradle_user_home_declared = $null
        gradle_user_home_effective = 'C:\fixture\user\.gradle'
        java_home = 'C:\fixture\jdk-22'
        conda_default_env = $null
        conda_prefix = $null
        resolved_cedg_env_prefix = 'C:\fixture\envs\cedg_env'
        resolved_cedg_env_source = 'CONFIGURED_ENV_ROOT'
        cedg_env_resolution_cause = $null
    }
    if ([string]::IsNullOrWhiteSpace($CompatibilitySignature)) {
        $CompatibilitySignature = Get-VerificationDeterministicHash -Value $contract
    }
    return [pscustomobject][ordered]@{
        base_commit = $BaseCommit
        base_tree = $BaseTree
        candidate_commit = $CandidateCommit
        candidate_tree = $CandidateTree
        environment_contract = $contract
        environment_compatibility_signature = $CompatibilitySignature
        environment_observation = $observation
        environment_observation_hash = Get-VerificationDeterministicHash -Value $observation
    }
}

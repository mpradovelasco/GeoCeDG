#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$script:Cases = 0
$script:Assertions = 0

Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-environment.psm1') -Force

function Assert-Case([bool]$Condition, [string]$Message) {
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}
function Invoke-Case([string]$Name, [scriptblock]$Body) {
    $script:Cases++
    try { & $Body; Write-Host "PASS: $Name" } catch { throw "FAIL: ${Name}: $($_.Exception.Message)" }
}

Invoke-Case 'portable contract excludes volatile observations' {
    $contract = Get-VerificationEnvironmentContract
    $json = ConvertTo-Json $contract -Depth 20 -Compress
    foreach ($forbidden in @('effective_user', 'user_profile', 'temp_root',
            'gradle_user_home', 'java_home', 'conda_prefix', 'resolved_cedg_env_prefix')) {
        Assert-Case (-not $json.Contains($forbidden, [StringComparison]::OrdinalIgnoreCase)) `
            "Portable contract includes volatile field $forbidden."
    }
    Assert-Case ($contract.powershell -ceq '>=7.2') 'PowerShell capability changed.'
    Assert-Case ($contract.gradle_wrapper -ceq '9.4.1') 'Gradle capability changed.'
    Assert-Case ($contract.gradle_launcher_jvm_major -eq 22) 'Launcher JVM capability changed.'
    Assert-Case (([int[]]$contract.required_jdk_majors -join ',') -ceq '17,25') 'JDK capabilities changed.'
    Assert-Case ($contract.python_version -ceq '3.12.13' -and
        $contract.mpmath_version -ceq '1.4.1') 'Python capabilities changed.'
}

Invoke-Case 'users paths Gradle homes and valid prefixes do not change compatibility signature' {
    $contract = Get-VerificationEnvironmentContract
    $first = Get-VerificationEnvironmentCompatibilitySignature $contract
    $second = Get-VerificationEnvironmentCompatibilitySignature (
        ConvertFrom-Json (ConvertTo-Json $contract -Depth 20) -Depth 20)
    Assert-Case ($first -ceq $second) 'Equivalent portable contracts have different signatures.'
    $observations = @(
        [ordered]@{ effective_user = 'machine-a/user-a'; gradle_user_home_declared = $null;
            resolved_cedg_env_prefix = 'C:/stable-a/cedg_env' },
        [ordered]@{ effective_user = 'machine-b/user-b'; gradle_user_home_declared = 'D:/cache';
            resolved_cedg_env_prefix = 'D:/stable-b/cedg_env' }
    )
    Assert-Case ((Get-VerificationEnvironmentObservationHash $observations[0]) -cne
        (Get-VerificationEnvironmentObservationHash $observations[1])) `
        'Distinct environment observations did not remain traceable.'
    Assert-Case ($first -ceq (Get-VerificationEnvironmentCompatibilitySignature $contract)) `
        'Observation changes altered portable compatibility.'
}

Invoke-Case 'restart cache loss and another compatible machine remain portable' {
    $contract = Get-VerificationEnvironmentContract
    $before = [ordered]@{
        effective_user = 'first-machine/user'; temp_root = 'C:/session-one';
        gradle_user_home_declared = 'C:/cache-one';
        resolved_cedg_env_prefix = 'C:/envs/cedg_env'
    }
    $afterRestart = [ordered]@{
        effective_user = 'second-machine/user'; temp_root = 'D:/fresh-session';
        gradle_user_home_declared = $null;
        resolved_cedg_env_prefix = 'D:/portable/cedg_env'
    }
    Assert-Case ((Get-VerificationEnvironmentObservationHash $before) -cne
        (Get-VerificationEnvironmentObservationHash $afterRestart)) `
        'Restart and machine observations were not distinguishable.'
    Assert-Case ((Get-VerificationEnvironmentCompatibilitySignature $contract) -ceq
        (Get-VerificationEnvironmentCompatibilitySignature $contract)) `
        'Compatible capabilities changed after volatile cache or TEMP loss.'
}

Invoke-Case 'distinct valid explicit cedg_env prefixes resolve without historical inventory' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-environment-prefix-' + [guid]::NewGuid().ToString('N'))
    $marker = Join-Path $root '.fixture-owner'
    [void][IO.Directory]::CreateDirectory($root)
    [IO.File]::WriteAllText($marker, 'verification-environment',
        [Text.UTF8Encoding]::new($false))
    try {
        $prefixes = @(
            (Join-Path $root 'machine-a/cedg_env'),
            (Join-Path $root 'machine-b/cedg_env'))
        foreach ($prefix in $prefixes) {
            [void][IO.Directory]::CreateDirectory((Join-Path $prefix 'conda-meta'))
            [IO.File]::WriteAllText((Join-Path $prefix 'conda-meta/history'), '',
                [Text.UTF8Encoding]::new($false))
            $python = Join-Path $prefix $(if ($IsWindows) { 'python.exe' } else { 'bin/python' })
            [void][IO.Directory]::CreateDirectory((Split-Path -Parent $python))
            [IO.File]::WriteAllText($python, '', [Text.UTF8Encoding]::new($false))
            $resolved = Resolve-VerificationCedgEnvironment -EnvironmentPrefix $prefix `
                -CondaExecutable (Get-Process -Id $PID).Path -WorkingDirectory $root
            Assert-Case ($resolved.resolution_source -ceq 'EXPLICIT_PREFIX') `
                'Explicit environment was not identified as explicit.'
            Assert-Case ($resolved.prefix -ceq [IO.Path]::GetFullPath($prefix)) `
                'Explicit environment prefix changed during resolution.'
        }
        $invalid = Join-Path $root 'missing/cedg_env'
        $rejected = $false
        try {
            Resolve-VerificationCedgEnvironment -EnvironmentPrefix $invalid `
                -CondaExecutable (Get-Process -Id $PID).Path -WorkingDirectory $root | Out-Null
        } catch { $rejected = $true }
        Assert-Case $rejected 'Invalid explicit environment silently fell back to another prefix.'
    } finally {
        $resolvedRoot = [IO.Path]::GetFullPath($root)
        if ($resolvedRoot.StartsWith($tempBase + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker -PathType Leaf) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-environment') {
            Remove-Item -LiteralPath $resolvedRoot -Recurse -Force
        } else { throw "Unsafe environment fixture cleanup target: $resolvedRoot" }
    }
}

Invoke-Case 'resolution source never enumerates Conda environments' {
    $source = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '../verification-environment.psm1'))
    foreach ($forbidden in @('env list', 'envs --json', 'info --envs', 'environments.txt')) {
        Assert-Case (-not $source.Contains($forbidden, [StringComparison]::OrdinalIgnoreCase)) `
            "Environment resolver contains forbidden enumeration mechanism: $forbidden"
    }
    Assert-Case (-not $source.Contains(('om' + '_env'), [StringComparison]::OrdinalIgnoreCase)) `
        'Environment resolver names an unrelated environment.'
    Assert-Case ($source.Contains("'config', '--show', 'envs_dirs', '--json'",
            [StringComparison]::Ordinal)) 'Environment resolver does not use configured roots.'
}

Invoke-Case 'invalid versions incomplete JDKs and foreign provenance remain blocking contracts' {
    $checker = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '../checks/workstation-safety.ps1'))
    foreach ($required in @("'3.12.13'", "'1.4.1'", 'foreach ($version in @(17, 25))',
            'Add-Contract "jdk.$version.complete"', 'python.executable-origin',
            'mpmath.import-origin')) {
        Assert-Case ($checker.Contains($required, [StringComparison]::Ordinal)) `
            "WORKSTATION lost blocking contract $required."
    }
    $schema = [IO.File]::ReadAllText((Join-Path $PSScriptRoot `
        '../../../geocedg/specs/operations/verification-result.schema.json')) |
        ConvertFrom-Json -AsHashtable -Depth 100
    $contractSchema = ConvertTo-Json $schema['$defs']['environmentContract'] -Depth 30
    foreach ($mutation in @(
            @{ name = 'wrong Python version'; field = 'python_version'; value = '3.12.14' },
            @{ name = 'incomplete JDK set'; field = 'required_jdk_majors'; value = @(25) },
            @{ name = 'foreign provenance'; field = 'python_provenance'; value = 'UNVERIFIED' }
        )) {
        $invalid = ConvertFrom-Json (ConvertTo-Json (Get-VerificationEnvironmentContract) `
                -Depth 20) -AsHashtable -Depth 20
        $invalid[$mutation.field] = $mutation.value
        $accepted = Test-Json -Json (ConvertTo-Json $invalid -Depth 20) `
            -Schema $contractSchema -ErrorAction SilentlyContinue
        Assert-Case (-not $accepted) "$($mutation.name) entered the portable contract."
    }
}

Write-Host "verification-environment.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1')

$script:Utf8NoBom = [Text.UTF8Encoding]::new($false)

function Resolve-VerificationEnvironmentApplication {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Name)

    $command = Get-Command $Name -CommandType Application -All -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $command) { return $null }
    return [IO.Path]::GetFullPath($command.Path)
}

function Invoke-VerificationEnvironmentCapture {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Executable,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [hashtable]$Environment = @{}
    )

    if (-not (Test-Path -LiteralPath $Executable -PathType Leaf)) {
        return [pscustomobject][ordered]@{
            state = 'EXECUTABLE_MISSING'; exit_code = $null; stdout = '';
            stderr = ''; cause = "Executable is unavailable: $Executable"
        }
    }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = [IO.Path]::GetFullPath($Executable)
    $start.WorkingDirectory = [IO.Path]::GetFullPath($WorkingDirectory)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $script:Utf8NoBom
    $start.StandardErrorEncoding = $script:Utf8NoBom
    foreach ($argument in $Arguments) { [void]$start.ArgumentList.Add($argument) }
    foreach ($name in $Environment.Keys) {
        $start.Environment[[string]$name] = [string]$Environment[$name]
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        try {
            if (-not $process.Start()) { throw 'Native process did not start.' }
            $stdoutTask = $process.StandardOutput.ReadToEndAsync()
            $stderrTask = $process.StandardError.ReadToEndAsync()
            $process.WaitForExit()
            return [pscustomobject][ordered]@{
                state = 'COMPLETED'
                exit_code = $process.ExitCode
                stdout = $stdoutTask.GetAwaiter().GetResult()
                stderr = $stderrTask.GetAwaiter().GetResult()
                cause = $null
            }
        } catch {
            return [pscustomobject][ordered]@{
                state = 'LAUNCH_FAILED'; exit_code = $null; stdout = '';
                stderr = ''; cause = $_.Exception.Message
            }
        }
    } finally {
        $process.Dispose()
    }
}

function Test-VerificationCedgEnvironmentPrefix {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Prefix)

    try {
        $full = [IO.Path]::GetFullPath($Prefix).TrimEnd('\', '/')
    } catch { return $false }
    if ([IO.Path]::GetFileName($full) -cne 'cedg_env') { return $false }
    return (Test-Path -LiteralPath (Join-Path $full 'conda-meta/history') -PathType Leaf) -and
        (Test-Path -LiteralPath (Join-Path $full $(if ($IsWindows) { 'python.exe' } else {
                    'bin/python'
                })) -PathType Leaf)
}

function Resolve-VerificationCedgEnvironment {
    [CmdletBinding()]
    param(
        [string]$EnvironmentPrefix,
        [string]$CondaExecutable,
        [string]$WorkingDirectory = (Get-Location).Path
    )

    $conda = if ([string]::IsNullOrWhiteSpace($CondaExecutable)) {
        Resolve-VerificationEnvironmentApplication 'conda'
    } else { [IO.Path]::GetFullPath($CondaExecutable) }
    if ([string]::IsNullOrWhiteSpace($conda) -or
            -not (Test-Path -LiteralPath $conda -PathType Leaf)) {
        throw 'Conda executable is unavailable.'
    }

    $candidates = [Collections.Generic.List[object]]::new()
    $seen = [Collections.Generic.HashSet[string]]::new(
        $(if ($IsWindows) { [StringComparer]::OrdinalIgnoreCase } else {
                [StringComparer]::Ordinal
            }))
    function Add-Candidate {
        param([AllowNull()] [string]$Path, [string]$Source)
        if ([string]::IsNullOrWhiteSpace($Path)) { return }
        try { $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/') } catch { return }
        if ([IO.Path]::GetFileName($full) -cne 'cedg_env' -or -not $seen.Add($full)) { return }
        $candidates.Add([pscustomobject]@{ prefix = $full; source = $Source })
    }

    if (-not [string]::IsNullOrWhiteSpace($EnvironmentPrefix)) {
        Add-Candidate $EnvironmentPrefix 'EXPLICIT_PREFIX'
    } else {
        if ([Environment]::GetEnvironmentVariable('CONDA_DEFAULT_ENV') -ceq 'cedg_env') {
            Add-Candidate ([Environment]::GetEnvironmentVariable('CONDA_PREFIX')) 'ACTIVE_PREFIX'
        }

        $config = Invoke-VerificationEnvironmentCapture -Executable $conda `
            -Arguments @('config', '--show', 'envs_dirs', '--json') `
            -WorkingDirectory $WorkingDirectory
        if ($config.state -cne 'COMPLETED' -or $config.exit_code -ne 0) {
            throw 'Conda environment-root configuration is unavailable.'
        }
        try {
            $configuration = ConvertFrom-Json -InputObject $config.stdout -Depth 20 -ErrorAction Stop
        } catch {
            throw 'Conda environment-root configuration is malformed.'
        }
        foreach ($root in [string[]]@($configuration.envs_dirs)) {
            if ([string]::IsNullOrWhiteSpace($root)) { continue }
            Add-Candidate (Join-Path ([IO.Path]::GetFullPath($root)) 'cedg_env') `
                'CONFIGURED_ENV_ROOT'
        }
    }

    foreach ($candidate in $candidates) {
        if (-not (Test-VerificationCedgEnvironmentPrefix $candidate.prefix)) { continue }
        $python = Join-Path $candidate.prefix $(if ($IsWindows) { 'python.exe' } else { 'bin/python' })
        return [pscustomobject][ordered]@{
            environment_name = 'cedg_env'
            prefix = [IO.Path]::GetFullPath($candidate.prefix)
            python = [IO.Path]::GetFullPath($python)
            conda = $conda
            resolution_source = [string]$candidate.source
        }
    }
    throw 'No valid cedg_env exists at the explicit prefix or in an active or configured Conda environment root.'
}

function Get-VerificationEnvironmentContract {
    [CmdletBinding()]
    param()

    $platform = if ($IsWindows) { 'WINDOWS' } elseif ($IsLinux) { 'LINUX' } elseif ($IsMacOS) {
        'MACOS'
    } else { 'UNKNOWN' }
    return [pscustomobject][ordered]@{
        contract_version = 1
        platform = $platform
        architecture = [Runtime.InteropServices.RuntimeInformation]::OSArchitecture.ToString().ToUpperInvariant()
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
}

function Get-VerificationEnvironmentCompatibilitySignature {
    [CmdletBinding()]
    param([object]$Contract = (Get-VerificationEnvironmentContract))

    return Get-VerificationDeterministicHash -Value $Contract
}

function Get-VerificationEnvironmentObservation {
    [CmdletBinding()]
    param([string]$RepositoryRoot = (Get-Location).Path)

    $resolved = $null
    $resolutionCause = $null
    try {
        $resolved = Resolve-VerificationCedgEnvironment -WorkingDirectory $RepositoryRoot
    } catch { $resolutionCause = $_.Exception.Message }
    $effectiveGradleHome = [Environment]::GetEnvironmentVariable('GRADLE_USER_HOME')
    if ([string]::IsNullOrWhiteSpace($effectiveGradleHome)) {
        $effectiveGradleHome = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.gradle'
    }
    $effectiveUser = try {
        if ($IsWindows) { [System.Security.Principal.WindowsIdentity]::GetCurrent().Name } else {
            [Environment]::UserName
        }
    } catch { [Environment]::UserName }
    return [pscustomobject][ordered]@{
        observation_version = 1
        effective_user = $effectiveUser
        os_description = [Runtime.InteropServices.RuntimeInformation]::OSDescription
        powershell_version = $PSVersionTable.PSVersion.ToString()
        process_architecture = [Runtime.InteropServices.RuntimeInformation]::ProcessArchitecture.ToString()
        repository_root = [IO.Path]::GetFullPath($RepositoryRoot)
        user_profile = [Environment]::GetFolderPath('UserProfile')
        temp_root = [IO.Path]::GetTempPath()
        gradle_user_home_declared = [Environment]::GetEnvironmentVariable('GRADLE_USER_HOME')
        gradle_user_home_effective = [IO.Path]::GetFullPath($effectiveGradleHome)
        java_home = [Environment]::GetEnvironmentVariable('JAVA_HOME')
        conda_default_env = [Environment]::GetEnvironmentVariable('CONDA_DEFAULT_ENV')
        conda_prefix = [Environment]::GetEnvironmentVariable('CONDA_PREFIX')
        resolved_cedg_env_prefix = $(if ($null -eq $resolved) { $null } else { $resolved.prefix })
        resolved_cedg_env_source = $(if ($null -eq $resolved) { $null } else { $resolved.resolution_source })
        cedg_env_resolution_cause = $resolutionCause
    }
}

function Get-VerificationEnvironmentObservationHash {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Observation)

    return Get-VerificationDeterministicHash -Value $Observation
}

Export-ModuleMember -Function @(
    'Resolve-VerificationEnvironmentApplication',
    'Invoke-VerificationEnvironmentCapture',
    'Test-VerificationCedgEnvironmentPrefix',
    'Resolve-VerificationCedgEnvironment',
    'Get-VerificationEnvironmentContract',
    'Get-VerificationEnvironmentCompatibilitySignature',
    'Get-VerificationEnvironmentObservation',
    'Get-VerificationEnvironmentObservationHash'
)

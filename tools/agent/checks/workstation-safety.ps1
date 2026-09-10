#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath,
    [Parameter(Mandatory)] [string]$ScratchDirectory,
    [ValidateSet('WORKSTATION', 'PACKAGING')]
    [string]$ContractMode = 'WORKSTATION'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
Import-Module (Join-Path $PSScriptRoot '../verification-environment.psm1') -Force
$script:Contracts = [Collections.Generic.List[object]]::new()
$script:NativeEvidence = [Collections.Generic.List[object]]::new()

function Add-Contract {
    param(
        [string]$Id,
        [ValidateSet('SATISFIED', 'VIOLATED', 'EVIDENCE_UNTRUSTED')]
        [string]$Status,
        [AllowNull()] [object]$Expected,
        [AllowNull()] [object]$Observed,
        [AllowNull()] [string]$Cause
    )
    $script:Contracts.Add([pscustomobject][ordered]@{
        contract_id = $Id
        status = $Status
        expected = $Expected
        observed = $Observed
        cause = $Cause
    })
}

function Resolve-Application {
    param([string]$Name)
    $command = Get-Command $Name -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $command) { return $null }
    return [IO.Path]::GetFullPath($command.Path)
}

function Resolve-CachedGradle {
    $gradleHome = Get-GradleUserHome
    $distributionRoot = Join-Path ([IO.Path]::GetFullPath($gradleHome)) `
        'wrapper/dists/gradle-9.4.1-bin'
    if (-not (Test-Path -LiteralPath $distributionRoot -PathType Container)) {
        return $null
    }
    $relative = if ($IsWindows) {
        'gradle-9.4.1/bin/gradle.bat'
    } else { 'gradle-9.4.1/bin/gradle' }
    $candidates = @(Get-ChildItem -LiteralPath $distributionRoot -Directory |
        ForEach-Object { Join-Path $_.FullName $relative } |
        Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
        Sort-Object)
    if ($candidates.Count -eq 0) { return $null }
    return [IO.Path]::GetFullPath($candidates[0])
}

function Get-GradleUserHome {
    $gradleHome = [Environment]::GetEnvironmentVariable('GRADLE_USER_HOME')
    if ([string]::IsNullOrWhiteSpace($gradleHome)) {
        $gradleHome = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.gradle'
    }
    return [IO.Path]::GetFullPath($gradleHome)
}

function Invoke-NativeCapture {
    param(
        [string]$Id,
        [string]$Executable,
        [string[]]$Arguments,
        [string]$WorkingDirectory,
        [hashtable]$Environment = @{}
    )
    if ([string]::IsNullOrWhiteSpace($Executable) -or
            -not (Test-Path -LiteralPath $Executable -PathType Leaf)) {
        return [pscustomobject]@{
            state = 'EXECUTABLE_MISSING'; exit_code = $null; stdout = '';
            stderr = ''; cause = "Executable is unavailable: $Executable"
        }
    }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $extension = [IO.Path]::GetExtension($Executable)
    if ($IsWindows -and $extension -cin @('.bat', '.cmd')) {
        $commandProcessor = [Environment]::GetEnvironmentVariable('ComSpec')
        if ([string]::IsNullOrWhiteSpace($commandProcessor)) {
            $commandProcessor = Join-Path ([Environment]::GetFolderPath('System')) 'cmd.exe'
        }
        $start.FileName = [IO.Path]::GetFullPath($commandProcessor)
        foreach ($argument in @('/d', '/c', [IO.Path]::GetFullPath($Executable)) +
                [string[]]$Arguments) {
            [void]$start.ArgumentList.Add($argument)
        }
    } else {
        $start.FileName = [IO.Path]::GetFullPath($Executable)
        foreach ($argument in [string[]]$Arguments) {
            [void]$start.ArgumentList.Add($argument)
        }
    }
    $start.WorkingDirectory = [IO.Path]::GetFullPath($WorkingDirectory)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
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
            $result = [pscustomobject]@{
                state = 'COMPLETED'
                exit_code = $process.ExitCode
                stdout = $stdoutTask.GetAwaiter().GetResult()
                stderr = $stderrTask.GetAwaiter().GetResult()
                cause = $null
            }
        } catch {
            $result = [pscustomobject]@{
                state = 'LAUNCH_FAILED'; exit_code = $null; stdout = '';
                stderr = ''; cause = $_.Exception.Message
            }
        }
    } finally {
        $process.Dispose()
    }
    $script:NativeEvidence.Add([pscustomobject][ordered]@{
        command_id = $Id
        executable = $start.FileName
        arguments = [string[]]$start.ArgumentList
        state = $result.state
        exit_code = $result.exit_code
        stdout = $result.stdout
        stderr = $result.stderr
        cause = $result.cause
    })
    return $result
}

function Test-ContainedPath {
    param([string]$Path, [string]$Root)
    if ([string]::IsNullOrWhiteSpace($Path) -or
            [string]::IsNullOrWhiteSpace($Root)) { return $false }
    try {
        $fullPath = [IO.Path]::GetFullPath($Path)
        $fullRoot = [IO.Path]::GetFullPath($Root).TrimEnd('\', '/')
        if ($fullPath.Equals($fullRoot,
                $(if ($IsWindows) { [StringComparison]::OrdinalIgnoreCase } else {
                    [StringComparison]::Ordinal
                }))) { return $false }
        $prefix = $fullRoot + [IO.Path]::DirectorySeparatorChar
        return $fullPath.StartsWith($prefix,
            $(if ($IsWindows) { [StringComparison]::OrdinalIgnoreCase } else {
                [StringComparison]::Ordinal
            }))
    } catch { return $false }
}

function Get-JavaMajor {
    param([string]$Text)
    $match = [regex]::Match($Text,
        '(?m)(?:version\s+"|^javac\s+|^(?:openjdk|java)\s+)(\d+)(?:\.|"|\s|$)')
    if (-not $match.Success) { return $null }
    return [int]$match.Groups[1].Value
}

function Get-InstalledJdkCandidates {
    param([string]$RepositoryRoot)

    $comparison = if ($IsWindows) {
        [StringComparer]::OrdinalIgnoreCase
    } else { [StringComparer]::Ordinal }
    $seen = [Collections.Generic.HashSet[string]]::new($comparison)
    $candidates = [Collections.Generic.List[string]]::new()

    function Add-Candidate {
        param([AllowNull()] [string]$Path)
        if ([string]::IsNullOrWhiteSpace($Path)) { return }
        try { $full = [IO.Path]::GetFullPath($Path) } catch { return }
        if ((Test-Path -LiteralPath $full -PathType Container) -and $seen.Add($full)) {
            $candidates.Add($full)
        }
    }

    foreach ($variable in Get-ChildItem Env: | Where-Object {
            $_.Name -match '^(?:JAVA|JDK)(?:_?[A-Z0-9]+)?_HOME$' -or
            $_.Name -ceq 'JAVA_HOME'
        }) {
        Add-Candidate $variable.Value
    }
    foreach ($commandName in @('java', 'javac')) {
        foreach ($command in @(Get-Command $commandName -CommandType Application -All `
                    -ErrorAction SilentlyContinue)) {
            try {
                Add-Candidate (Split-Path -Parent (Split-Path -Parent $command.Path))
            } catch { }
        }
    }

    $gradleHome = Get-GradleUserHome
    $gradleJdks = Join-Path $gradleHome 'jdks'
    if (Test-Path -LiteralPath $gradleJdks -PathType Container) {
        foreach ($directory in Get-ChildItem -LiteralPath $gradleJdks -Directory) {
            Add-Candidate $directory.FullName
        }
    }

    # IDE-managed JDKs are machine capabilities too. Their absolute location is
    # observation data; discover the conventional per-user container without
    # binding acceptance to a historical user or cache path.
    $userJdks = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.jdks'
    if (Test-Path -LiteralPath $userJdks -PathType Container) {
        foreach ($directory in Get-ChildItem -LiteralPath $userJdks -Directory) {
            Add-Candidate $directory.FullName
        }
    }

    foreach ($propertiesPath in @(
            (Join-Path $gradleHome 'gradle.properties'),
            (Join-Path $RepositoryRoot 'gradle.properties')
        )) {
        if (-not (Test-Path -LiteralPath $propertiesPath -PathType Leaf)) { continue }
        foreach ($line in [IO.File]::ReadAllLines($propertiesPath)) {
            if ($line -notmatch '^\s*org\.gradle\.java\.installations\.paths\s*=\s*(.+?)\s*$') {
                continue
            }
            foreach ($configuredPath in $Matches[1] -split ',') {
                $decodedPath = $configuredPath.Trim().Replace('\:', ':').Replace('\\', '\')
                Add-Candidate $decodedPath
            }
        }
    }

    return [string[]]$candidates.ToArray()
}

function Write-FinalResult {
    $outcome = if (@($script:Contracts | Where-Object {
                $_.status -ceq 'EVIDENCE_UNTRUSTED'
            }).Count -gt 0) {
        'EVIDENCE_UNTRUSTED'
    } elseif (@($script:Contracts | Where-Object {
                $_.status -ceq 'VIOLATED'
            }).Count -gt 0) {
        'CONTRACT_VIOLATED'
    } else {
        'CONTRACT_SATISFIED'
    }
    $result = [ordered]@{
        contract_class = 'SAFETY'
        outcome = $outcome
        check_kind = $(if ($ContractMode -ceq 'PACKAGING') {
            'LIVE_PACKAGING_TOOLCHAIN'
        } else { 'LIVE_WORKSTATION_INSTALLATION' })
        mutation_performed = $false
        subcontracts = [object[]]$script:Contracts.ToArray()
        native_evidence = [object[]]$script:NativeEvidence.ToArray()
    }
    $full = [IO.Path]::GetFullPath($ResultPath)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
    $json = (ConvertTo-Json -InputObject $result -Depth 30).Replace("`r`n", "`n") + "`n"
    [IO.File]::WriteAllText($full, $json, $utf8)
    return $outcome
}

$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$scratch = [IO.Path]::GetFullPath($ScratchDirectory)
[void][IO.Directory]::CreateDirectory($scratch)

if ($ContractMode -ceq 'PACKAGING') {
    $toolchainRequested = [Environment]::GetEnvironmentVariable(
        'GEOCEDG_PACKAGING_CHECK_TOOLCHAIN') -ceq 'true'
    if (-not $toolchainRequested) {
        Add-Contract 'packaging.toolchain-request' SATISFIED 'not requested' 'not requested' $null
        $outcome = Write-FinalResult
        exit 0
    }
    $jdk25 = $null
    foreach ($candidate in Get-InstalledJdkCandidates -RepositoryRoot $repository) {
        $javaPath = Join-Path $candidate $(if ($IsWindows) { 'bin/java.exe' } else { 'bin/java' })
        $javacPath = Join-Path $candidate $(if ($IsWindows) { 'bin/javac.exe' } else { 'bin/javac' })
        $jpackagePath = Join-Path $candidate $(if ($IsWindows) { 'bin/jpackage.exe' } else { 'bin/jpackage' })
        $javaRun = Invoke-NativeCapture 'packaging.java' $javaPath @('-version') $repository
        $javacRun = Invoke-NativeCapture 'packaging.javac' $javacPath @('-version') $repository
        if ((Get-JavaMajor ($javaRun.stdout + $javaRun.stderr)) -eq 25 -and
                (Get-JavaMajor ($javacRun.stdout + $javacRun.stderr)) -eq 25 -and
                (Test-Path -LiteralPath $jpackagePath -PathType Leaf)) {
            $jdk25 = [pscustomobject]@{ root = $candidate; jpackage = $jpackagePath }
            break
        }
    }
    Add-Contract 'packaging.jdk25-jpackage' $(if ($null -ne $jdk25) {
        'SATISFIED'
    } else { 'VIOLATED' }) 'complete JDK 25 with jpackage' $jdk25 $(if ($null -eq $jdk25) {
        'A complete JDK 25 containing jpackage is unavailable.'
    } else { $null })
    if ($null -ne $jdk25) {
        $jpackageRun = Invoke-NativeCapture 'packaging.jpackage-version' $jdk25.jpackage @('--version') $repository
        $jpackageVersion = ($jpackageRun.stdout + $jpackageRun.stderr).Trim()
        Add-Contract 'packaging.jpackage-version' $(if ($jpackageRun.state -ceq 'COMPLETED' -and
                $jpackageRun.exit_code -eq 0 -and $jpackageVersion -match '^25(?:\.|$)') {
            'SATISFIED'
        } elseif ($jpackageRun.state -ceq 'COMPLETED') { 'VIOLATED' } else {
            'EVIDENCE_UNTRUSTED'
        }) 'jpackage 25' $jpackageVersion $jpackageRun.cause
    } else {
        Add-Contract 'packaging.jpackage-version' VIOLATED 'jpackage 25' $null `
            'jpackage cannot be inspected without the required JDK 25.'
    }

    $dotnet = Resolve-Application 'dotnet'
    $dotnetRun = Invoke-NativeCapture 'packaging.dotnet-sdks' $dotnet @('--list-sdks') $repository
    $hasDotnet6 = $dotnetRun.state -ceq 'COMPLETED' -and $dotnetRun.exit_code -eq 0 -and
        [regex]::IsMatch($dotnetRun.stdout, '(?m)^(?:[6-9]|[1-9]\d+)\.')
    Add-Contract 'packaging.dotnet-sdk' $(if ($hasDotnet6) { 'SATISFIED' } elseif (
            $dotnetRun.state -ceq 'EXECUTABLE_MISSING') { 'VIOLATED' } else {
            'EVIDENCE_UNTRUSTED'
        }) '.NET SDK 6 or newer' $dotnetRun.stdout $dotnetRun.cause

    $wix = Resolve-Application 'wix'
    $wixVersionRun = Invoke-NativeCapture 'packaging.wix-version' $wix @('--version') $repository
    $wixVersion = ($wixVersionRun.stdout + $wixVersionRun.stderr).Trim()
    Add-Contract 'packaging.wix-version' $(if ($wixVersionRun.state -ceq 'COMPLETED' -and
            $wixVersionRun.exit_code -eq 0 -and $wixVersion.StartsWith('5.0.2', [StringComparison]::Ordinal)) {
        'SATISFIED'
    } elseif ($wixVersionRun.state -ceq 'EXECUTABLE_MISSING' -or
            $wixVersionRun.state -ceq 'COMPLETED') { 'VIOLATED' } else {
        'EVIDENCE_UNTRUSTED'
    }) 'WiX 5.0.2' $wixVersion $wixVersionRun.cause
    $wixExtensionsRun = Invoke-NativeCapture 'packaging.wix-extensions' $wix @(
        'extension', 'list', '-g') $repository
    foreach ($extension in @('WixToolset.Util.wixext', 'WixToolset.UI.wixext')) {
        $found = $wixExtensionsRun.state -ceq 'COMPLETED' -and
            $wixExtensionsRun.exit_code -eq 0 -and
            [regex]::IsMatch($wixExtensionsRun.stdout,
                '(?m)^' + [regex]::Escape($extension) + '\s+5\.0\.2\s*$')
        Add-Contract ('packaging.wix-extension.' + $extension.ToLowerInvariant()) $(if ($found) {
            'SATISFIED'
        } elseif ($wixExtensionsRun.state -ceq 'COMPLETED') { 'VIOLATED' } else {
            'EVIDENCE_UNTRUSTED'
        }) "$extension/5.0.2" $wixExtensionsRun.stdout $wixExtensionsRun.cause
    }
    $outcome = Write-FinalResult
    if ($outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
    if ($outcome -ceq 'CONTRACT_VIOLATED') { exit 2 }
    exit 3
}

$wrapper = Join-Path $repository $(if ($IsWindows) { 'gradlew.bat' } else { 'gradlew' })
$wrapperJar = Join-Path $repository 'gradle/wrapper/gradle-wrapper.jar'
$wrapperProperties = Join-Path $repository 'gradle/wrapper/gradle-wrapper.properties'
$wrapperComplete = (Test-Path -LiteralPath $wrapper -PathType Leaf) -and
    (Test-Path -LiteralPath $wrapperJar -PathType Leaf) -and
    (Test-Path -LiteralPath $wrapperProperties -PathType Leaf)
if ($wrapperComplete) {
    Add-Contract 'gradle.wrapper.present' SATISFIED $wrapper $wrapper $null
} else {
    Add-Contract 'gradle.wrapper.present' VIOLATED 'complete repository Gradle Wrapper' ([ordered]@{
        script = Test-Path -LiteralPath $wrapper -PathType Leaf
        jar = Test-Path -LiteralPath $wrapperJar -PathType Leaf
        properties = Test-Path -LiteralPath $wrapperProperties -PathType Leaf
    }) 'Gradle Wrapper is incomplete.'
}
$wrapperVersionConfigured = $false
if (Test-Path -LiteralPath $wrapperProperties -PathType Leaf) {
    try {
        $propertiesText = [IO.File]::ReadAllText(
            $wrapperProperties, [Text.UTF8Encoding]::new($false, $true))
        $wrapperVersionConfigured = $propertiesText -match
            '(?m)^distributionUrl=.*gradle-9\.4\.1-bin\.zip\s*$'
    } catch { $wrapperVersionConfigured = $false }
}
Add-Contract 'gradle.wrapper.configuration' $(if ($wrapperVersionConfigured) {
    'SATISFIED'
} else { 'VIOLATED' }) 'Gradle 9.4.1 binary distribution' $wrapperVersionConfigured $(if (
    $wrapperVersionConfigured) { $null } else { 'Wrapper distribution is not Gradle 9.4.1 binary.' })

# Execute only an already materialized wrapper distribution. Invoking gradlew
# here could download the distribution, which is prohibited for inspection.
$gradleExecutable = Resolve-CachedGradle
Add-Contract 'gradle.distribution.cached' $(if ($null -ne $gradleExecutable) {
    'SATISFIED'
} else { 'VIOLATED' }) 'installed Gradle 9.4.1 wrapper distribution' $gradleExecutable $(if (
    $null -ne $gradleExecutable) { $null } else { 'Gradle 9.4.1 is not materialized locally.' })

$gradleVersion = Invoke-NativeCapture 'gradle.version' $gradleExecutable @(
    '--version', '--no-daemon', '--no-problems-report', '--console=plain',
    '-Dorg.gradle.java.installations.auto-download=false') $repository
if ($gradleVersion.state -cne 'COMPLETED' -or $gradleVersion.exit_code -ne 0) {
    Add-Contract 'gradle.version' EVIDENCE_UNTRUSTED '9.4.1' $gradleVersion.exit_code $gradleVersion.cause
    Add-Contract 'gradle.launcher-jvm' EVIDENCE_UNTRUSTED 22 $null 'Gradle version output is unavailable.'
} else {
    $versionMatch = [regex]::Match($gradleVersion.stdout, '(?m)^Gradle\s+(\S+)\s*$')
    if (-not $versionMatch.Success) {
        Add-Contract 'gradle.version' EVIDENCE_UNTRUSTED '9.4.1' $gradleVersion.stdout 'Gradle version output is malformed.'
    } else {
        Add-Contract 'gradle.version' $(if ($versionMatch.Groups[1].Value -ceq '9.4.1') {
            'SATISFIED'
        } else { 'VIOLATED' }) '9.4.1' $versionMatch.Groups[1].Value $null
    }
    $launcherMatch = [regex]::Match($gradleVersion.stdout,
        '(?m)^Launcher JVM:\s*(\d+)(?:\.|\s|$)')
    if (-not $launcherMatch.Success) {
        Add-Contract 'gradle.launcher-jvm' EVIDENCE_UNTRUSTED 22 $gradleVersion.stdout 'Gradle launcher JVM output is malformed.'
    } else {
        Add-Contract 'gradle.launcher-jvm' $(if ([int]$launcherMatch.Groups[1].Value -eq 22) {
            'SATISFIED'
        } else { 'VIOLATED' }) 22 ([int]$launcherMatch.Groups[1].Value) $null
    }
}

$binaryName = if ($IsWindows) { @{ java = 'java.exe'; javac = 'javac.exe' } } else {
    @{ java = 'java'; javac = 'javac' }
}
$jdkInventory = [Collections.Generic.List[object]]::new()
foreach ($candidateRoot in Get-InstalledJdkCandidates -RepositoryRoot $repository) {
    $javaPath = Join-Path $candidateRoot "bin/$($binaryName.java)"
    $javacPath = Join-Path $candidateRoot "bin/$($binaryName.javac)"
    if (-not (Test-Path -LiteralPath $javaPath -PathType Leaf) -or
            -not (Test-Path -LiteralPath $javacPath -PathType Leaf)) {
        continue
    }
    $javaProbe = Invoke-NativeCapture "jdk.probe.$($jdkInventory.Count).java" `
        $javaPath @('-version') $repository
    $javacProbe = Invoke-NativeCapture "jdk.probe.$($jdkInventory.Count).javac" `
        $javacPath @('-version') $repository
    $jdkInventory.Add([pscustomobject][ordered]@{
        root = $candidateRoot
        java = [ordered]@{
            path = $javaPath
            major = $(if ($javaProbe.state -ceq 'COMPLETED' -and
                    $javaProbe.exit_code -eq 0) {
                Get-JavaMajor ($javaProbe.stdout + "`n" + $javaProbe.stderr)
            } else { $null })
        }
        javac = [ordered]@{
            path = $javacPath
            major = $(if ($javacProbe.state -ceq 'COMPLETED' -and
                    $javacProbe.exit_code -eq 0) {
                Get-JavaMajor ($javacProbe.stdout + "`n" + $javacProbe.stderr)
            } else { $null })
        }
    })
}
foreach ($version in @(17, 25)) {
    $candidate = @($jdkInventory | Where-Object {
            $_.java.major -eq $version -and $_.javac.major -eq $version
        } | Select-Object -First 1)
    if ($candidate.Count -eq 1) {
        Add-Contract "jdk.$version.complete" SATISFIED "full JDK $version" `
            $candidate[0] $null
    } else {
        $unreadableCandidates = @($jdkInventory | Where-Object {
                $null -eq $_.java.major -or $null -eq $_.javac.major
            })
        Add-Contract "jdk.$version.complete" $(if ($unreadableCandidates.Count -gt 0) {
            'EVIDENCE_UNTRUSTED'
        } else { 'VIOLATED' }) "full JDK $version" `
            ([object[]]$jdkInventory.ToArray()) $(if ($unreadableCandidates.Count -gt 0) {
            'Installed JDK candidate version output is unavailable or malformed.'
        } else { 'Required complete JDK is missing.' })
    }
}

$conda = Resolve-VerificationEnvironmentApplication 'conda'
$condaRun = Invoke-NativeCapture 'conda.version' $conda @('--version') $repository
Add-Contract 'conda.available' $(if ($condaRun.state -ceq 'COMPLETED' -and
        $condaRun.exit_code -eq 0 -and $condaRun.stdout -match '(?m)^conda\s+\S+') {
    'SATISFIED'
} elseif ($condaRun.state -ceq 'EXECUTABLE_MISSING') { 'VIOLATED' } else {
    'EVIDENCE_UNTRUSTED'
}) 'Conda executable' $(if ($null -ne $conda) { $conda } else { $condaRun.state }) $condaRun.cause

$resolvedEnvironment = $null
$environmentResolutionCause = $null
try {
    $resolvedEnvironment = Resolve-VerificationCedgEnvironment `
        -CondaExecutable $conda -WorkingDirectory $repository
} catch { $environmentResolutionCause = $_.Exception.Message }
$resolvedName = if ($null -eq $resolvedEnvironment) { $null } else {
    [string]$resolvedEnvironment.environment_name
}
Add-Contract 'conda.environment-name' $(if ($resolvedName -ceq 'cedg_env') {
    'SATISFIED'
} else { 'VIOLATED' }) 'cedg_env' $resolvedName $(if ($resolvedName -ceq 'cedg_env') {
    $null
} else { $environmentResolutionCause })
$environmentPrefix = if ($null -eq $resolvedEnvironment) { $null } else {
    [string]$resolvedEnvironment.prefix
}
$prefixState = 'VIOLATED'
$prefixObserved = $environmentPrefix
$prefixCause = $environmentResolutionCause
try {
    $resolvedEnvironmentPrefix = [IO.Path]::GetFullPath($environmentPrefix).TrimEnd('\', '/')
    $prefixObserved = $resolvedEnvironmentPrefix
    if ([IO.Path]::IsPathFullyQualified($environmentPrefix) -and
            (Test-Path -LiteralPath $resolvedEnvironmentPrefix -PathType Container) -and
            (Split-Path -Leaf $resolvedEnvironmentPrefix) -ceq 'cedg_env') {
        $prefixState = 'SATISFIED'
        $prefixCause = $null
    }
} catch { }
Add-Contract 'conda.environment-prefix' $prefixState `
    'existing absolute prefix named cedg_env' $prefixObserved $prefixCause
$python = if ($null -eq $resolvedEnvironment) { $null } else {
    [string]$resolvedEnvironment.python
}
$probeSource = "import json,os,platform,sys,mpmath;print('GEOCEDG_WORKSTATION:'+json.dumps({'python':platform.python_version(),'implementation':platform.python_implementation(),'executable':sys.executable,'prefix':sys.prefix,'conda_prefix':os.environ.get('CONDA_PREFIX',''),'mpmath':mpmath.__version__,'mpmath_file':mpmath.__file__},sort_keys=True))"
$pythonEnvironment = @{}
if ($null -ne $resolvedEnvironment) {
    $pythonEnvironment['CONDA_PREFIX'] = [string]$resolvedEnvironment.prefix
    $pythonEnvironment['CONDA_DEFAULT_ENV'] = 'cedg_env'
    $pythonEnvironment['PATH'] = [string]$resolvedEnvironment.prefix +
        [IO.Path]::PathSeparator + [Environment]::GetEnvironmentVariable('PATH')
}
$pythonRun = Invoke-NativeCapture 'python.identity' $python @('-c', $probeSource) `
    $repository $pythonEnvironment
if ($pythonRun.state -cne 'COMPLETED' -or $pythonRun.exit_code -ne 0) {
    foreach ($id in @('python.version', 'python.implementation', 'python.prefix',
            'conda.prefix-inheritance',
            'python.executable-origin', 'mpmath.version', 'mpmath.import-origin')) {
        Add-Contract $id $(if ($pythonRun.state -ceq 'EXECUTABLE_MISSING') {
            'VIOLATED'
        } else { 'EVIDENCE_UNTRUSTED' }) $null $pythonRun.exit_code $pythonRun.cause
    }
} else {
    $records = @($pythonRun.stdout -split "`r?`n" | Where-Object {
        $_.StartsWith('GEOCEDG_WORKSTATION:', [StringComparison]::Ordinal)
    })
    $facts = $null
    if ($records.Count -eq 1) {
        try {
            $facts = ConvertFrom-Json -InputObject $records[0].Substring(
                'GEOCEDG_WORKSTATION:'.Length) -Depth 20 -ErrorAction Stop
        } catch { $facts = $null }
    }
    if ($null -eq $facts) {
        foreach ($id in @('python.version', 'python.implementation', 'python.prefix',
                'conda.prefix-inheritance',
                'python.executable-origin', 'mpmath.version', 'mpmath.import-origin')) {
            Add-Contract $id EVIDENCE_UNTRUSTED $null $pythonRun.stdout 'Python identity output is malformed.'
        }
    } else {
        Add-Contract 'python.version' $(if ([string]$facts.python -ceq '3.12.13') {
            'SATISFIED'
        } else { 'VIOLATED' }) '3.12.13' ([string]$facts.python) $null
        Add-Contract 'python.implementation' $(if ([string]$facts.implementation -ceq 'CPython') {
            'SATISFIED'
        } else { 'VIOLATED' }) 'CPython' ([string]$facts.implementation) $null
        $samePrefix = $false
        $prefixInherited = $false
        $childCondaPrefix = [string]$facts.conda_prefix
        try {
            $comparison = if ($IsWindows) {
                [StringComparison]::OrdinalIgnoreCase
            } else { [StringComparison]::Ordinal }
            $samePrefix = [IO.Path]::GetFullPath([string]$facts.prefix).TrimEnd('\', '/').Equals(
                [IO.Path]::GetFullPath($childCondaPrefix).TrimEnd('\', '/'), $comparison)
            $prefixInherited = [IO.Path]::GetFullPath($childCondaPrefix).TrimEnd('\', '/').Equals(
                [IO.Path]::GetFullPath([string]$environmentPrefix).TrimEnd('\', '/'), $comparison)
        } catch {
            $samePrefix = $false
            $prefixInherited = $false
        }
        Add-Contract 'python.prefix' $(if ($samePrefix) { 'SATISFIED' } else { 'VIOLATED' }) 'sys.prefix == CONDA_PREFIX' ([ordered]@{ sys_prefix = $facts.prefix; conda_prefix = $childCondaPrefix }) $null
        Add-Contract 'conda.prefix-inheritance' $(if ($prefixInherited) { 'SATISFIED' } else { 'VIOLATED' }) 'child CONDA_PREFIX == verifier CONDA_PREFIX' ([ordered]@{ child = $childCondaPrefix; verifier = $environmentPrefix }) $null
        Add-Contract 'python.executable-origin' $(if (Test-ContainedPath $facts.executable $childCondaPrefix) {
            'SATISFIED'
        } else { 'VIOLATED' }) 'inside CONDA_PREFIX' ([string]$facts.executable) $null
        Add-Contract 'mpmath.version' $(if ([string]$facts.mpmath -ceq '1.4.1') {
            'SATISFIED'
        } else { 'VIOLATED' }) '1.4.1' ([string]$facts.mpmath) $null
        Add-Contract 'mpmath.import-origin' $(if (Test-ContainedPath $facts.mpmath_file $childCondaPrefix) {
            'SATISFIED'
        } else { 'VIOLATED' }) 'inside CONDA_PREFIX' ([string]$facts.mpmath_file) $null
    }
}

$outcome = Write-FinalResult
if ($outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
if ($outcome -ceq 'CONTRACT_VIOLATED') { exit 2 }
exit 3

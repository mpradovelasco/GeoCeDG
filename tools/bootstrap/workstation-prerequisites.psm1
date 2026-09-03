#requires -Version 7.2
Set-StrictMode -Version Latest

function Stop-WorkstationPrerequisite {
    param([string]$Message, [string]$Classification = "unsupported-or-missing-workstation-prerequisite", [string]$Stage = "workstation preflight")
    $failure = [InvalidOperationException]::new($Message)
    $failure.Data["FailureClassification"] = $Classification
    $failure.Data["Stage"] = $Stage
    throw $failure
}

function Get-WorkstationRequirements {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [scriptblock]$ReadSource = { param($Path) Get-Content -Raw -LiteralPath $Path })
    $references = @(
        @("geocedg/validation/locus-v2/g7a/generate_metric_references.py", "tools/agent/verify-g7a-metrics.ps1"),
        @("geocedg/validation/locus-v2/g8a/generate_intersection_references.py", "tools/agent/verify-g8a-intersections.ps1"),
        @("geocedg/validation/locus-v2/g8c/generate_extended_intersection_references.py", "tools/agent/verify-g8c-intersections-design.ps1")
    )
    $pins = @()
    foreach ($reference in $references) {
        $source = & $ReadSource (Join-Path $RepositoryRoot $reference[0])
        $python = [regex]::Match($source, '(?m)^EXPECTED_PYTHON\s*=\s*["'']([^"'']+)["'']\s*$')
        $mpmath = [regex]::Match($source, '(?m)^EXPECTED_MPMATH\s*=\s*["'']([^"'']+)["'']\s*$')
        $implementation = [regex]::Match($source, '["'']implementation["'']\s*:\s*["'']([^"'']+)["'']')
        $verifier = & $ReadSource (Join-Path $RepositoryRoot $reference[1])
        $environment = [regex]::Match($verifier, '-n\s+([A-Za-z0-9_-]+)\s+python')
        if (-not $python.Success -or -not $mpmath.Success -or -not $implementation.Success -or -not $environment.Success) {
            Stop-WorkstationPrerequisite "Cannot derive the current numerical prerequisites from $($reference[0]) / $($reference[1]). Review bootstrap impact; do not guess versions." "source-contract-contradiction"
        }
        $pins += [pscustomobject]@{ Python = $python.Groups[1].Value; Implementation = $implementation.Groups[1].Value; Mpmath = $mpmath.Groups[1].Value; Environment = $environment.Groups[1].Value }
    }
    if (@($pins.Python | Select-Object -Unique).Count -ne 1 -or @($pins.Implementation | Select-Object -Unique).Count -ne 1 -or @($pins.Mpmath | Select-Object -Unique).Count -ne 1 -or @($pins.Environment | Select-Object -Unique).Count -ne 1) {
        Stop-WorkstationPrerequisite "Current numerical generators/verifiers disagree on Python version/implementation, mpmath, or Conda environment. Resolve the source contradiction explicitly." "source-contract-contradiction"
    }
    $conventionPath = "source/build-logic/convention/src/main/kotlin/java-conventions.gradle.kts"
    $convention = & $ReadSource (Join-Path $RepositoryRoot $conventionPath)
    $compiler = [regex]::Match($convention, 'JavaLanguageVersion\.of\((\d+)\)')
    $desktopPath = "source/desktop/desktop/build.gradle.kts"
    $desktop = & $ReadSource (Join-Path $RepositoryRoot $desktopPath)
    $runtime = [regex]::Match($desktop, '(?s)val\s+desktopJavaLauncher\s*=\s*project\.javaToolchains\.launcherFor\s*\{\s*languageVersion\.set\(JavaLanguageVersion\.of\((\d+)\)\)')
    $profile = (& $ReadSource (Join-Path $RepositoryRoot "packaging/windows/package.yml")) | ConvertFrom-Json
    if (-not $compiler.Success -or -not $runtime.Success -or [int]$profile.toolchain.gradle_java -le 0 -or [int]$profile.toolchain.desktop_java -ne [int]$runtime.Groups[1].Value) {
        Stop-WorkstationPrerequisite "Compiler/Desktop/profile Java requirements cannot be derived consistently. Review bootstrap impact instead of assuming a JDK version." "source-contract-contradiction"
    }
    return [pscustomobject]@{
        CompilerJava = [int]$compiler.Groups[1].Value
        GradleJava = [int]$profile.toolchain.gradle_java
        DesktopJava = [int]$runtime.Groups[1].Value
        Python = $pins[0].Python
        PythonImplementation = $pins[0].Implementation
        Mpmath = $pins[0].Mpmath
        CondaEnvironment = $pins[0].Environment
        Authorities = @($conventionPath, $desktopPath, "packaging/windows/package.yml") + @($references | ForEach-Object { $_[0]; $_[1] })
    }
}

function Get-EffectiveGradleJavaPath {
    param([AllowNull()] [AllowEmptyString()] [string]$JavaHome, [AllowNull()] [AllowEmptyString()] [string]$PathJava, [Parameter(Mandatory)] [string]$WorkingDirectory)
    # Match the batch wrapper: a defined, nonempty home wins, even if invalid.
    if (-not [string]::IsNullOrEmpty($JavaHome)) {
        $unquoted = $JavaHome.Replace('"', '')
        if ([string]::IsNullOrWhiteSpace($unquoted)) {
            Stop-WorkstationPrerequisite "JAVA_HOME is defined but blank/invalid; the wrapper will not fall back to PATH." "toolchain-incompatibility" "effective Gradle Java"
        }
        $homePath = [IO.Path]::GetFullPath($unquoted, $WorkingDirectory)
        return [pscustomobject]@{ Path = Join-Path $homePath "bin/java.exe"; Selection = "JAVA_HOME"; JavaHome = $homePath; PathJava = $PathJava }
    }
    if ([string]::IsNullOrWhiteSpace($PathJava)) {
        Stop-WorkstationPrerequisite "Neither JAVA_HOME nor a PATH Java executable is available." "unsupported-or-missing-workstation-prerequisite" "effective Gradle Java"
    }
    return [pscustomobject]@{ Path = $PathJava; Selection = "PATH"; JavaHome = $null; PathJava = $PathJava }
}

function ConvertFrom-JavaVersionOutput {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [AllowEmptyString()] [string[]]$Output)
    $text = $Output -join [Environment]::NewLine
    $match = [regex]::Match($text, '(?m)(?:version\s+"|^javac\s+|^(?:openjdk|java)\s+)(\d+)(?:\.|"|\s|$)')
    if (-not $match.Success) {
        Stop-WorkstationPrerequisite "Unable to parse Java version output: $text" "toolchain-incompatibility" "Java version"
    }
    return [int]$match.Groups[1].Value
}

function ConvertFrom-GradleToolchainOutput {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [AllowEmptyString()] [string[]]$Output)
    $text = $Output -join [Environment]::NewLine
    foreach ($block in [regex]::Split($text, '(?m)^\s+\+\s')) {
        $version = [regex]::Match($block, '(?m)^\s*\|\s*Language Version:\s*(\d+)\s*$')
        if (-not $version.Success) { continue }
        $location = [regex]::Match($block, '(?m)^\s*\|\s*Location:\s*(.+?)\s*$')
        $vendor = [regex]::Match($block, '(?m)^\s*\|\s*Vendor:\s*(.+?)\s*$')
        $isJdk = [regex]::Match($block, '(?m)^\s*\|\s*Is JDK:\s*(true|false)\s*$')
        [pscustomobject]@{
            LanguageVersion = [int]$version.Groups[1].Value
            Location = if ($location.Success) { $location.Groups[1].Value.Trim() } else { "" }
            Vendor = if ($vendor.Success) { $vendor.Groups[1].Value.Trim() } else { "not reported" }
            IsJdk = ($isJdk.Success -and $isJdk.Groups[1].Value -eq "true")
        }
    }
}

function Get-CondaPrerequisiteProbe {
    # Embedded double quotes are stripped by legacy Windows native marshalling.
    # Python single-quoted literals keep this one -c argument compatible with it.
    return "import json,os,platform,sys,mpmath; print('GEOCE_WORKSTATION:'+json.dumps({'python':platform.python_version(),'implementation':platform.python_implementation(),'python_executable':sys.executable,'python_prefix':sys.prefix,'environment_name':os.environ.get('CONDA_DEFAULT_ENV',''),'environment_prefix':os.environ.get('CONDA_PREFIX',''),'mpmath':mpmath.__version__,'mpmath_file':mpmath.__file__}))"
}

function Test-WorkstationPathWithin {
    param([string]$Path, [string]$Root)
    if ([string]::IsNullOrWhiteSpace($Path) -or [string]::IsNullOrWhiteSpace($Root)) { return $false }
    try {
        $fullPath = [IO.Path]::GetFullPath($Path)
        $fullRoot = [IO.Path]::GetFullPath($Root).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
        return $fullPath.StartsWith($fullRoot, [StringComparison]::OrdinalIgnoreCase)
    } catch { return $false }
}

function ConvertFrom-CondaProbeOutput {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [AllowEmptyString()] [string[]]$Output, [Parameter(Mandatory)] [object]$Requirements)
    $records = @($Output | Where-Object { $_.StartsWith("GEOCE_WORKSTATION:", [StringComparison]::Ordinal) })
    if ($records.Count -ne 1) {
        Stop-WorkstationPrerequisite "Conda probe did not return exactly one structured environment record. Review its native log." "workstation-environment" "Conda environment probe"
    }
    try { $facts = $records[0].Substring("GEOCE_WORKSTATION:".Length) | ConvertFrom-Json -ErrorAction Stop }
    catch { Stop-WorkstationPrerequisite "Conda probe JSON is invalid: $($_.Exception.Message)" "workstation-environment" "Conda environment probe" }
    foreach ($required in @("python", "implementation", "python_executable", "python_prefix", "environment_name", "environment_prefix", "mpmath", "mpmath_file")) {
        if ($null -eq $facts.PSObject.Properties[$required] -or [string]::IsNullOrWhiteSpace([string]$facts.$required)) {
            Stop-WorkstationPrerequisite "Conda probe is missing $required." "workstation-environment" "Conda environment probe"
        }
    }
    if ($facts.python -cne $Requirements.Python -or $facts.mpmath -cne $Requirements.Mpmath) {
        Stop-WorkstationPrerequisite "Conda $($Requirements.CondaEnvironment) requires Python $($Requirements.Python) / mpmath $($Requirements.Mpmath) from current generators; observed $($facts.python) / $($facts.mpmath)." "toolchain-incompatibility" "Conda environment probe"
    }
    if ($facts.implementation -cne $Requirements.PythonImplementation) {
        Stop-WorkstationPrerequisite "Conda $($Requirements.CondaEnvironment) requires Python implementation $($Requirements.PythonImplementation) declared by current reference generators; observed $($facts.implementation)." "toolchain-incompatibility" "Conda environment probe"
    }
    $environmentPrefix = [IO.Path]::GetFullPath([string]$facts.environment_prefix).TrimEnd('\', '/')
    $samePrefix = [IO.Path]::GetFullPath([string]$facts.python_prefix).TrimEnd('\', '/').Equals($environmentPrefix, [StringComparison]::OrdinalIgnoreCase)
    $environmentIdentityMatches = $facts.environment_name -ceq $Requirements.CondaEnvironment
    # Conda reports an absolute prefix in CONDA_DEFAULT_ENV outside a parent named envs.
    # Named selection remains the explicit conda run -n argument, not a basename inference.
    if (-not $environmentIdentityMatches -and [IO.Path]::IsPathFullyQualified([string]$facts.environment_name) -and [IO.Path]::IsPathFullyQualified([string]$facts.environment_prefix)) {
        try {
            $environmentIdentityMatches = [IO.Path]::GetFullPath([string]$facts.environment_name).TrimEnd('\', '/').Equals($environmentPrefix, [StringComparison]::OrdinalIgnoreCase)
        } catch { $environmentIdentityMatches = $false }
    }
    if (-not $environmentIdentityMatches -or -not $samePrefix -or -not (Test-WorkstationPathWithin $facts.python_executable $facts.environment_prefix) -or -not (Test-WorkstationPathWithin $facts.mpmath_file $facts.environment_prefix)) {
        Stop-WorkstationPrerequisite "Conda probe origin is inconsistent with named environment $($Requirements.CondaEnvironment): CONDA_DEFAULT_ENV=$($facts.environment_name); executable=$($facts.python_executable); prefix=$($facts.python_prefix); CONDA_PREFIX=$($facts.environment_prefix); mpmath=$($facts.mpmath_file)." "workstation-environment" "Conda import origin"
    }
    return $facts
}

function Invoke-WorkstationProbeCommand {
    param([scriptblock]$Runner, [object]$Command, [string[]]$Arguments, [string]$Description)
    $result = & $Runner $Command $Arguments $Description
    if ($null -eq $result -or $null -eq $result.PSObject.Properties["ExitCode"] -or $null -eq $result.PSObject.Properties["Output"]) {
        Stop-WorkstationPrerequisite "Command runner did not return native evidence for $Description." "unknown" $Description
    }
    if ($result.ExitCode -ne 0) {
        $failure = [InvalidOperationException]::new("$Description failed with exit code $($result.ExitCode). " + (@($result.Output) -join [Environment]::NewLine))
        $failure.Data["FailureClassification"] = if ((@($result.Output) -join " ") -match "AccessDeniedException|UnauthorizedAccessException|Access is denied") { "permissions-filesystem" } else { "unknown-native-failure" }
        $failure.Data["Stage"] = $Description
        $failure.Data["NativeExitCode"] = $result.ExitCode
        throw $failure
    }
    return @($result.Output | ForEach-Object { $_.ToString() })
}

function Invoke-WorkstationPrerequisiteCheck {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [AllowNull()] [AllowEmptyString()] [string]$JavaHome,
        [Parameter(Mandatory)] [hashtable]$Commands,
        [Parameter(Mandatory)] [scriptblock]$CommandRunner,
        [scriptblock]$TestFile = { param($Path) Test-Path -LiteralPath $Path -PathType Leaf },
        [object]$Requirements
    )
    if ($null -eq $Requirements) { $Requirements = Get-WorkstationRequirements $RepositoryRoot }
    $selection = Get-EffectiveGradleJavaPath -JavaHome $JavaHome -PathJava $Commands.Java -WorkingDirectory $RepositoryRoot
    if (-not (& $TestFile $selection.Path)) {
        Stop-WorkstationPrerequisite "Selected Gradle Java is missing: $($selection.Path). Selection=$($selection.Selection); PATH=$($selection.PathJava). Correct JAVA_HOME/PATH manually." "toolchain-incompatibility" "effective Gradle Java"
    }
    $javaOutput = @(Invoke-WorkstationProbeCommand $CommandRunner $selection.Path @("-version") "effective Gradle Java version")
    $major = ConvertFrom-JavaVersionOutput $javaOutput
    if ($major -ne $Requirements.GradleJava) {
        Stop-WorkstationPrerequisite "Effective Gradle launcher must be Java $($Requirements.GradleJava), found $major at $($selection.Path) selected by $($selection.Selection). PATH Java is $($selection.PathJava). No environment variable was changed." "toolchain-incompatibility" "effective Gradle Java"
    }
    if (-not $Commands.ContainsKey("Conda") -or $null -eq $Commands.Conda) {
        Stop-WorkstationPrerequisite "Conda is unavailable. Prepare named environment $($Requirements.CondaEnvironment) using the current canonical numerical requirements; global Python is not a substitute." "unsupported-or-missing-workstation-prerequisite" "Conda command"
    }
    $condaOutput = @(Invoke-WorkstationProbeCommand $CommandRunner $Commands.Conda @("run", "--no-capture-output", "-n", $Requirements.CondaEnvironment, "python", "-c", (Get-CondaPrerequisiteProbe)) "Conda named-environment version and import-origin probe")
    $condaFacts = ConvertFrom-CondaProbeOutput -Output $condaOutput -Requirements $Requirements
    $wrapper = Join-Path $RepositoryRoot "gradlew.bat"
    $gradleOutput = @(Invoke-WorkstationProbeCommand $CommandRunner $wrapper @("--version", "--no-daemon", "--no-problems-report", "--console=plain") "Gradle wrapper launcher inventory")
    $launcher = @($gradleOutput | Where-Object { $_ -match "^Launcher JVM:" })
    if ($launcher.Count -ne 1 -or $launcher[0] -notmatch "^Launcher JVM:\s*(\d+)" -or [int]$Matches[1] -ne $Requirements.GradleJava) {
        Stop-WorkstationPrerequisite "Actual wrapper Launcher JVM metadata does not confirm Java $($Requirements.GradleJava). Review preflight logs." "toolchain-incompatibility" "Gradle wrapper launcher inventory"
    }
    $toolchainOutput = @(Invoke-WorkstationProbeCommand $CommandRunner $wrapper @("-q", "javaToolchains", "--no-daemon", "--no-problems-report", "--console=plain", "-Dorg.gradle.java.installations.auto-download=false") "Gradle JDK inventory")
    $toolchains = @(ConvertFrom-GradleToolchainOutput $toolchainOutput)
    $selected = @{}
    foreach ($version in @($Requirements.CompilerJava, $Requirements.DesktopJava) | Select-Object -Unique) {
        $candidate = @($toolchains | Where-Object {
            $_.LanguageVersion -eq $version -and $_.IsJdk -and -not [string]::IsNullOrWhiteSpace($_.Location) -and
            (& $TestFile (Join-Path $_.Location "bin/java.exe")) -and (& $TestFile (Join-Path $_.Location "bin/javac.exe"))
        } | Select-Object -First 1)
        if ($candidate.Count -ne 1) {
            Stop-WorkstationPrerequisite "Gradle did not report a usable full JDK $version. Install/configure that JDK manually; automatic download is disabled. Review the actual inventory before assuming a machine path." "unsupported-or-missing-workstation-prerequisite" "Gradle JDK inventory"
        }
        foreach ($binary in @("java", "javac")) {
            $output = @(Invoke-WorkstationProbeCommand $CommandRunner (Join-Path $candidate[0].Location "bin/$binary.exe") @("-version") "JDK $version $binary version")
            if ((ConvertFrom-JavaVersionOutput $output) -ne $version) {
                Stop-WorkstationPrerequisite "Reported JDK $version has inconsistent $binary version at $($candidate[0].Location)." "toolchain-incompatibility" "Gradle JDK inventory"
            }
        }
        $selected[$version] = $candidate[0]
    }
    return [pscustomobject]@{
        Requirements = $Requirements
        EffectiveJava = $selection
        Conda = $condaFacts
        CompilerToolchain = $selected[$Requirements.CompilerJava]
        DesktopToolchain = $selected[$Requirements.DesktopJava]
        GradleVersionOutput = $gradleOutput
        ToolchainOutput = $toolchainOutput
    }
}

Export-ModuleMember -Function Get-WorkstationRequirements, Get-EffectiveGradleJavaPath, ConvertFrom-JavaVersionOutput, ConvertFrom-GradleToolchainOutput, Get-CondaPrerequisiteProbe, ConvertFrom-CondaProbeOutput, Invoke-WorkstationPrerequisiteCheck

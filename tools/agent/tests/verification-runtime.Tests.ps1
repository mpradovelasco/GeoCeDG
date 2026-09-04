#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$ModulePath = (Join-Path $PSScriptRoot "../verification-runtime.psm1"),
    [string]$RootVerifierPath = (Join-Path $PSScriptRoot "../verify.ps1"),
    [string]$OperationalVerifierPath = (Join-Path $PSScriptRoot "../verify-operational.ps1"),
    [string]$BaselineVerifierPath = (Join-Path $PSScriptRoot "../verify-baseline.ps1"),
    [string]$InfrastructureVerifierPath = (Join-Path $PSScriptRoot "../verify-verification-infrastructure.ps1"),
    [string]$GeneratedStateTestsPath = (Join-Path $PSScriptRoot "generated-state.tests.ps1"),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verification-runtime-tests")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Fake-first operational tests. No Gradle, Java, network, or real project build
# is invoked. Each case imports a uniquely named fixture copy of the module.
# The fixture copy's private native-launch function is replaced. Focused path
# safety and diagnostic-publication cases also replace named private I/O seams;
# their production guard functions remain unchanged. Unsafe-path cases do not
# initialize Git, launch native processes, create links, or remove real files.
# Exports, identity hashing, receipt ownership, XML parsing, and consumers remain
# unchanged. Fixture repositories and logs are retained as explicit evidence.
$ModulePath = (Resolve-Path -LiteralPath $ModulePath).Path
$RuntimeFixturePath = $PSCommandPath
$ModuleSha256 = (Get-FileHash -LiteralPath $ModulePath -Algorithm SHA256).Hash.ToLowerInvariant()
$RunId = [guid]::NewGuid().ToString("N")
$EvidenceRoot = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) $RunId
# Git-created fixture repositories must not inherit arbitrary report-path depth.
# Keep a uniquely owned, retained working tree in TEMP; publish its exact locator
# in the requested evidence directory. This changes no Git/global path policy.
$FixtureTempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$RunRoot = Join-Path $FixtureTempBase ("geocedg-vrf-" + $RunId)
if (Test-Path -LiteralPath $RunRoot) { throw "Fixture working root already exists: $RunRoot" }
$FixtureAncestor = $FixtureTempBase
while (-not [string]::IsNullOrWhiteSpace($FixtureAncestor)) {
    if ((Test-Path -LiteralPath $FixtureAncestor) -and
            ((Get-Item -LiteralPath $FixtureAncestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "Fixture working root refuses linked ancestry: $FixtureAncestor"
    }
    $FixtureAncestor = Split-Path -Parent $FixtureAncestor
}
[void](New-Item -ItemType Directory -Path $RunRoot -ErrorAction Stop)
Write-Host ("Retained fixture working root: " + $RunRoot)
[void][IO.Directory]::CreateDirectory($EvidenceRoot)
$Results = [Collections.Generic.List[object]]::new()
$Lf = [string][char]10
$CaseNumber = 0

function Assert-TestCondition {
    param([Parameter(Mandatory)] [bool]$Condition, [Parameter(Mandatory)] [string]$Message)
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-TestSequence {
    param([string[]]$Expected, [string[]]$Actual, [Parameter(Mandatory)] [string]$Message)
    Assert-TestCondition -Condition (($Expected -join [char]0) -ceq ($Actual -join [char]0)) -Message (
        "$Message; expected: $($Expected -join ' | '); actual: $($Actual -join ' | ')")
}

function Assert-TestThrows {
    param(
        [Parameter(Mandatory)] [scriptblock]$Action,
        [Parameter(Mandatory)] [string]$Pattern,
        [Parameter(Mandatory)] [string]$Message
    )
    try { & $Action | Out-Null } catch {
        Assert-TestCondition -Condition ($_.Exception.Message -match $Pattern) -Message (
            "$Message failed for the wrong reason: $($_.Exception.Message)")
        return
    }
    throw "TEST FAILURE: $Message unexpectedly succeeded."
}

function Write-FixtureText {
    param([Parameter(Mandatory)] [string]$Path, [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    [IO.File]::WriteAllText($Path, $Text, [Text.UTF8Encoding]::new($false))
}

function Set-FixtureBuildRootProperties {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [string]$JvmArguments = "-Xmx4g -XX:MaxMetaspaceSize=4g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
    )
    $common = "org.gradle.daemon=true" + $Lf + "org.gradle.parallel=true" + $Lf +
        "org.gradle.jvmargs=" + $JvmArguments + $Lf
    Write-FixtureText (Join-Path $RepositoryRoot "gradle.properties") $common
    Write-FixtureText (Join-Path $RepositoryRoot "source/shared/gradle.properties") ($common + "org.gradle.caching=true" + $Lf)
    Write-FixtureText (Join-Path $RepositoryRoot "source/desktop/gradle.properties") $common
}

function Set-FixtureRuntimeSourceMutation {
    param([Parameter(Mandatory)] [object]$Fixture)
    $copyPath = [IO.Path]::GetFullPath($Fixture.ModulePath)
    $ownedPrefix = [IO.Path]::GetFullPath($Fixture.Root).TrimEnd('/', '\') + [IO.Path]::DirectorySeparatorChar
    Assert-TestCondition ($copyPath.StartsWith($ownedPrefix, [StringComparison]::OrdinalIgnoreCase) -and
        -not $copyPath.Equals($ModulePath, [StringComparison]::OrdinalIgnoreCase) -and
        [IO.Path]::GetFileName($copyPath) -cmatch '^verification-runtime-fixture-[0-9a-f]{32}\.psm1$') "Source mutation must target only the owned disposable module copy."
    $timestamp = [IO.File]::GetLastWriteTimeUtc($copyPath)
    [IO.File]::AppendAllText($copyPath, $Lf + '# FAKE-FIRST disposable loaded-source mutation.' + $Lf,
        [Text.UTF8Encoding]::new($false))
    [IO.File]::SetLastWriteTimeUtc($copyPath, $timestamp)
}

function Invoke-FixtureGit {
    param([Parameter(Mandatory)] [string]$Root, [Parameter(Mandatory)] [string[]]$Arguments)
    $emptyHooks = Join-Path $Root ".git/empty-fixture-hooks"
    [void][IO.Directory]::CreateDirectory($emptyHooks)
    $output = @(& git --no-optional-locks -c "core.hooksPath=$emptyHooks" -c commit.gpgSign=false -C $Root @Arguments 2>&1 |
        ForEach-Object { $_.ToString() })
    $nativeExitCode = $LASTEXITCODE
    if ($nativeExitCode -ne 0) { throw "Fixture git failed ($nativeExitCode): $($output -join ' ')" }
}

function Install-FixtureNative {
    param([Parameter(Mandatory)] [object]$Fixture)
    & $Fixture.Module {
        param($RepositoryRoot, $JdkDirectory)
        $script:FixtureNativeCalls = [Collections.Generic.List[object]]::new()
        $script:FixtureSettings = @{
            RepositoryRoot = $RepositoryRoot
            JdkDirectory = $JdkDirectory
            TestJdkDirectory = $JdkDirectory
            EmitTestJvmLaunch = $true
            VersionExitCode = 0
            PreparationExitCode = 0
            ToolchainExitCode = 0
            SharedExitCode = 0
            DesktopExitCode = 0
            TestOutcome = "EXECUTED"
            TestTaskMultiplicity = 1
            ExtraTestOutcome = ""
            TestHeadingTaskOverride = ""
            EmitJUnitReports = $true
            StyleOutcome = "EXECUTED"
            JUnitMode = "PASS"
            JUnitTargetClass = "org.geocedg.fixture.SharedTest"
            JUnitCaseName = "caseA"
            SelectionMethodName = "caseA"
        }
        function script:Invoke-VerificationNative {
            param(
                [Parameter(Mandatory)] [string]$FilePath,
                [Parameter(Mandatory)] [string[]]$Arguments,
                [Parameter(Mandatory)] [string]$WorkingDirectory,
                [Parameter(Mandatory)] [string]$LogPath,
                [Parameter(Mandatory)] [string]$Description
            )
            $fixtureRoot = $script:FixtureSettings.RepositoryRoot
            $sharedContext = ([IO.Path]::GetFullPath($WorkingDirectory)).Equals(
                (Join-Path $fixtureRoot "source/shared"), [StringComparison]::OrdinalIgnoreCase)
            if (-not $sharedContext -and -not ([IO.Path]::GetFullPath($WorkingDirectory)).Equals(
                    $fixtureRoot, [StringComparison]::OrdinalIgnoreCase)) {
                throw "Fixture native launcher received a foreign working directory."
            }
            if (-not $FilePath.EndsWith("gradlew.bat", [StringComparison]::OrdinalIgnoreCase)) {
                throw "Fixture native launcher received an unexpected executable."
            }
            $script:FixtureNativeCalls.Add([pscustomobject]@{
                arguments = @($Arguments); workingDirectory = $WorkingDirectory; description = $Description
            })
            $code = 0
            $output = [Collections.Generic.List[string]]::new()
            if ($Arguments -ccontains "--version") {
                $code = $script:FixtureSettings.VersionExitCode
                $output.Add("Gradle 9.4.1")
                $output.Add("Launcher JVM: 17.0.18 (GeoCeDG Fake Fixture)")
                $output.Add("Daemon JVM: " + $script:FixtureSettings.JdkDirectory)
                $output.Add("FAKE-FIRST identity; no Gradle or JVM executable launched")
            } elseif ($Arguments -ccontains ":shared:common-jre:compileTestJava" -or
                    $Arguments -ccontains ":desktop:desktop:compileTestJava") {
                $tasks = @($Arguments | Where-Object { $_.StartsWith(":", [StringComparison]::Ordinal) })
                if (($tasks -join [char]0) -cne (@(":shared:common-jre:compileTestJava",
                            ":desktop:desktop:compileTestJava") -join [char]0) -or
                        $Arguments -ccontains "--tests" -or $Arguments -ccontains "--rerun") {
                    throw "Fixture preparation must contain only the two explicit compileTestJava tasks, not Test execution."
                }
                $code = $script:FixtureSettings.PreparationExitCode
                foreach ($task in $tasks) { $output.Add("> Task $task UP-TO-DATE") }
                $output.Add("FAKE-FIRST toolchain preparation; no Test task, JUnit, or test JVM produced")
                $output.Add($(if ($code -eq 0) { "BUILD SUCCESSFUL in 1s" } else { "BUILD FAILED in 1s" }))
            } elseif ($Arguments -ccontains "javaToolchains") {
                $code = $script:FixtureSettings.ToolchainExitCode
                $output.Add("+ GeoCeDG Fake Fixture JDK 17")
                $output.Add("    | Location: " + $script:FixtureSettings.JdkDirectory)
                $output.Add("    | Language Version: 17")
                $output.Add("    | Vendor: GeoCeDG Fake Fixture")
            } else {
                $module = if ($Arguments -ccontains ":shared:common-jre:test" -or
                    ($sharedContext -and $Arguments -ccontains ":common-jre:test")) { "shared" } else { "desktop" }
                $definition = $script:ModuleDefinitions[$module]
                $requestedTask = if ($sharedContext -and $module -ceq "shared") { ":common-jre:test" } else { $definition.Task }
                if ($Arguments -cnotcontains $requestedTask) { throw "Fixture native launcher received no expected Test task." }
                $code = if ($module -ceq "shared") {
                    $script:FixtureSettings.SharedExitCode
                } else { $script:FixtureSettings.DesktopExitCode }
                $resultDirectory = Join-Path $fixtureRoot $definition.ResultDirectory
                [void][IO.Directory]::CreateDirectory($resultDirectory)
                $classes = if ($module -ceq "shared") {
                    @("org.geocedg.fixture.SharedTest") + @($script:MandatoryUpstreamClasses)
                } else { @("org.geocedg.fixture.DesktopTest") }
                if ($module -ceq "shared" -and $Arguments -cnotcontains "--tests") {
                    $classes += "org.geogebra.fixture.OptionalUpstreamTest"
                }
                $filters = @()
                for ($argumentIndex = 0; $argumentIndex -lt $Arguments.Count; $argumentIndex++) {
                    if ($Arguments[$argumentIndex] -ceq "--tests") {
                        $argumentIndex++
                        $filters += $Arguments[$argumentIndex]
                    }
                }
                if ($filters.Count -gt 0) {
                    $classes = @($classes | Where-Object {
                        $candidateClass = $_
                        @($filters | Where-Object {
                            $pattern = "\A" + [regex]::Escape($_).Replace("\*", ".*") + "\z"
                            $selectionClass = if ([char]::IsUpper($_[0])) {
                                $candidateClass.Substring($candidateClass.LastIndexOf('.') + 1)
                            } else { $candidateClass }
                            # Native selection identity is independent of the
                            # saved XML display name used by the DEV consumer.
                            $selectionClass -cmatch $pattern -or
                                ($selectionClass + "." + $script:FixtureSettings.SelectionMethodName) -cmatch $pattern
                        }).Count -gt 0
                    })
                }
                if (-not $script:FixtureSettings.EmitJUnitReports) { $classes = @() }
                foreach ($class in $classes) {
                    $mode = if ($class -ceq $script:FixtureSettings.JUnitTargetClass) {
                        $script:FixtureSettings.JUnitMode
                    } else { "PASS" }
                    $failures = 0
                    $errors = 0
                    $skipped = 0
                    $tests = 1
                    $child = ""
                    switch ($mode) {
                        "FAILURE" { $failures = 1; $child = '<failure message="fake-first failure" />' }
                        "ERROR" { $errors = 1; $child = '<error message="fake-first error" />' }
                        "SKIPPED" { $skipped = 1; $child = '<skipped />' }
                        "COUNTER_MISMATCH" { $failures = 1 }
                        "ZERO" { $tests = 0 }
                        "PASS" { }
                        default { throw "Unknown fixture JUnit mode: $mode" }
                    }
                    $case = if ($tests -eq 0) { "" } else {
                        $escapedName = [Security.SecurityElement]::Escape($script:FixtureSettings.JUnitCaseName)
                        '<testcase classname="' + $class + '" name="' + $escapedName + '">' + $child + '</testcase>'
                    }
                    $xml = '<testsuite name="' + $class + '" tests="' + $tests +
                        '" failures="' + $failures + '" errors="' + $errors + '" skipped="' +
                        $skipped + '">' + $case + '</testsuite>'
                    [IO.File]::WriteAllText((Join-Path $resultDirectory ("TEST-" + $class + ".xml")),
                        $xml, [Text.UTF8Encoding]::new($false))
                }
                $compileTasks = if ($module -ceq "shared") {
                    @(":shared:canvas-base:compileJava", ":shared:renderer-base:compileJava",
                        ":shared:common:compileJava", ":shared:common-jre:compileJava")
                } else { @(":desktop:desktop:compileJava") }
                foreach ($task in $compileTasks) {
                    $compileHeading = if ($sharedContext) { $task.Substring(7) } else { $task }
                    $output.Add("> Task $compileHeading UP-TO-DATE")
                }
                if ($script:FixtureSettings.EmitTestJvmLaunch) {
                    $javaPath = Join-Path $script:FixtureSettings.TestJdkDirectory "bin/java.exe"
                    $output.Add("Starting process 'Gradle Test Executor 1'. Working directory: " +
                        $WorkingDirectory + " Command: " + $javaPath +
                        " -Dfile.encoding=UTF-8 -Xmx512m worker.org.gradle.process.internal.worker.GradleWorkerMain")
                }
                $testSuffix = if ($script:FixtureSettings.TestOutcome -ceq "EXECUTED") {
                    ""
                } else { " " + $script:FixtureSettings.TestOutcome }
                $testHeading = if ([string]::IsNullOrEmpty($script:FixtureSettings.TestHeadingTaskOverride)) {
                    $requestedTask
                } else { $script:FixtureSettings.TestHeadingTaskOverride }
                for ($index = 0; $index -lt $script:FixtureSettings.TestTaskMultiplicity; $index++) {
                    $output.Add("> Task " + $testHeading + $testSuffix)
                }
                if (-not [string]::IsNullOrEmpty($script:FixtureSettings.ExtraTestOutcome)) {
                    $output.Add("> Task " + $testHeading + " " + $script:FixtureSettings.ExtraTestOutcome)
                }
                foreach ($style in $definition.Styles.GetEnumerator()) {
                    if ($Arguments -cnotcontains $style.Key) { continue }
                    $path = Join-Path $fixtureRoot $style.Value
                    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $path))
                    [IO.File]::WriteAllText($path, '<checkstyle version="10.12"></checkstyle>',
                        [Text.UTF8Encoding]::new($false))
                    $styleSuffix = if ($script:FixtureSettings.StyleOutcome -ceq "EXECUTED") {
                        ""
                    } else { " " + $script:FixtureSettings.StyleOutcome }
                    $output.Add("> Task " + $style.Key + $styleSuffix)
                }
                $output.Add($(if ($code -eq 0) { "BUILD SUCCESSFUL in 1s" } else { "BUILD FAILED in 1s" }))
            }
            $text = $output -join $script:Lf
            [void][IO.Directory]::CreateDirectory((Split-Path -Parent $LogPath))
            [IO.File]::WriteAllText($LogPath, $text + $script:Lf, [Text.UTF8Encoding]::new($false))
            return [pscustomobject]@{
                file = $FilePath; arguments = @($Arguments); workingDirectory = $WorkingDirectory
                logPath = $LogPath; startedUtc = [datetime]::UtcNow.ToString("o")
                finishedUtc = [datetime]::UtcNow.ToString("o"); elapsedSeconds = 0.0
                exitCode = $code; text = $text
            }
        }
    } $Fixture.RepositoryRoot $Fixture.JdkDirectory
}

function Set-FixtureSettings {
    param([Parameter(Mandatory)] [object]$Fixture, [Parameter(Mandatory)] [hashtable]$Values)
    & $Fixture.Module {
        param($Values)
        foreach ($key in $Values.Keys) {
            if (-not $script:FixtureSettings.ContainsKey($key)) { throw "Unknown fixture setting: $key" }
            $script:FixtureSettings[$key] = $Values[$key]
        }
    } $Values
}

function Install-FixtureRemovalProbe {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [ValidateSet("shared", "desktop")] [string]$Module,
        [ValidateSet("repository", "source", "build", "test-results", "test", "report-file", "none")]
        [string]$LinkedTarget
    )
    Assert-TestCondition (-not $Fixture.GitInitialized) "Path-safety probes must not initialize Git."
    & $Fixture.Module {
        param($Root, $Module, $LinkedTarget)
        $directory = Resolve-VerificationChildPath $Root $script:ModuleDefinitions[$Module].ResultDirectory
        $rootPath = [IO.Path]::GetFullPath($Root).TrimEnd('/', '\')
        $ancestors = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
        $ancestor = $directory
        while ($true) {
            [void]$ancestors.Add($ancestor)
            if ($ancestor.Equals($rootPath, [StringComparison]::OrdinalIgnoreCase)) { break }
            $ancestor = Split-Path -Parent $ancestor
            if ([string]::IsNullOrWhiteSpace($ancestor)) { throw "Invalid synthetic fixture ancestry." }
        }
        $linkedPath = switch ($LinkedTarget) {
            "repository" { $rootPath }
            "source" { Join-Path $rootPath "source" }
            "build" { Split-Path -Parent (Split-Path -Parent $directory) }
            "test-results" { Split-Path -Parent $directory }
            "test" { $directory }
            default { "" }
        }
        $script:FixtureRemovalProbe = @{
            Directory = $directory; Ancestors = $ancestors; LinkedPath = $linkedPath
            LinkedFile = ($LinkedTarget -ceq "report-file")
            ItemReads = [Collections.Generic.List[string]]::new()
            Removed = [Collections.Generic.List[string]]::new()
            Enumerations = 0; GitCalls = 0
        }
        # These functions shadow I/O only in this uniquely imported module copy.
        # Remove-CurrentJUnitReports itself is the unchanged production function.
        function script:Test-Path {
            param([string]$LiteralPath, [string]$PathType)
            return $script:FixtureRemovalProbe.Ancestors.Contains($LiteralPath)
        }
        function script:Get-Item {
            param([string]$LiteralPath, [switch]$Force)
            if (-not $script:FixtureRemovalProbe.Ancestors.Contains($LiteralPath)) {
                throw "Unexpected synthetic ancestor read: $LiteralPath"
            }
            $script:FixtureRemovalProbe.ItemReads.Add($LiteralPath)
            $attributes = [IO.FileAttributes]::Directory
            if ($LiteralPath.Equals($script:FixtureRemovalProbe.LinkedPath, [StringComparison]::OrdinalIgnoreCase)) {
                $attributes = $attributes -bor [IO.FileAttributes]::ReparsePoint
            }
            [pscustomobject]@{ FullName = $LiteralPath; Attributes = $attributes }
        }
        function script:Get-ChildItem {
            param([string]$LiteralPath, [string]$Filter, [switch]$File)
            if (-not $LiteralPath.Equals($script:FixtureRemovalProbe.Directory, [StringComparison]::OrdinalIgnoreCase) -or
                    $Filter -cne "TEST-*.xml" -or -not $File) {
                throw "Unexpected synthetic JUnit enumeration."
            }
            $script:FixtureRemovalProbe.Enumerations++
            # A safe file deliberately precedes the linked file. The verifier
            # must inspect the complete collection before its first removal.
            [pscustomobject]@{
                FullName = Join-Path $LiteralPath "TEST-first-safe.xml"
                Attributes = [IO.FileAttributes]::Normal
            }
            [pscustomobject]@{
                FullName = Join-Path $LiteralPath "TEST-second.xml"
                Attributes = $(if ($script:FixtureRemovalProbe.LinkedFile) {
                    [IO.FileAttributes]::ReparsePoint
                } else { [IO.FileAttributes]::Normal })
            }
        }
        function script:Remove-Item {
            param([string]$LiteralPath, [switch]$Force)
            # Never forwards to the filesystem, even if the guard regresses.
            $script:FixtureRemovalProbe.Removed.Add($LiteralPath)
        }
        function script:Invoke-VerificationGit {
            param([string]$RepositoryRoot, [string[]]$Arguments)
            $script:FixtureRemovalProbe.GitCalls++
            throw "Synthetic path-safety probe must not invoke Git."
        }
    } $Fixture.RepositoryRoot $Module $LinkedTarget
}

function New-TestFixture {
    param([switch]$UseOriginalNative, [switch]$WithoutGit)
    $caseRoot = Join-Path $RunRoot ("case-" + $CaseNumber.ToString("000") + "-" + [guid]::NewGuid().ToString("N"))
    $repository = Join-Path $caseRoot "repository"
    $jdk = Join-Path $caseRoot "fake-jdk"
    $gradleHome = Join-Path $caseRoot "fake-gradle-user-home"
    [void][IO.Directory]::CreateDirectory($repository)
    [void][IO.Directory]::CreateDirectory($gradleHome)
    Write-FixtureText (Join-Path $repository ".gitignore") ("**/build/" + $Lf + ".gradle/" + $Lf + "ignored-input.txt" + $Lf)
    Write-FixtureText (Join-Path $repository "source.txt") ("SOURCE-A" + $Lf)
    Write-FixtureText (Join-Path $repository "gradlew.bat") ("@echo off" + $Lf + "exit /b 99" + $Lf)
    Write-FixtureText (Join-Path $repository "gradle/wrapper/gradle-wrapper.properties") (
        "distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip" + $Lf)
    Set-FixtureBuildRootProperties -RepositoryRoot $repository
    # Real module roots contain tracked build declarations. Without them Git's
    # --directory ignored inventory can also collapse the untracked parent of
    # an otherwise legitimate build directory. Model the real source topology;
    # do not loosen the production ignored-input contract for a sparse fixture.
    foreach ($relative in @("source/shared/common/build.gradle.kts",
            "source/shared/common-jre/build.gradle.kts", "source/desktop/desktop/build.gradle.kts")) {
        Write-FixtureText (Join-Path $repository $relative) ("// FAKE-FIRST tracked module declaration; never executed." + $Lf)
    }
    foreach ($relative in @("release", "bin/java.exe", "bin/javac.exe")) {
        Write-FixtureText (Join-Path $jdk $relative) ("FAKE-FIRST fixture " + $relative + $Lf)
    }
    Write-FixtureText (Join-Path $jdk "release") ('JAVA_VERSION="17.0.18"' + $Lf + 'IMPLEMENTOR="GeoCeDG Fake Fixture"' + $Lf)
    if (-not $WithoutGit) {
        Invoke-FixtureGit $repository @("init", "--initial-branch=main")
        Invoke-FixtureGit $repository @("config", "user.name", "GeoCeDG Verification Fixture")
        Invoke-FixtureGit $repository @("config", "user.email", "verification-fixture@example.invalid")
        Invoke-FixtureGit $repository @("config", "core.autocrlf", "false")
        Invoke-FixtureGit $repository @("add", "--force", ".gitignore", "source.txt", "gradlew.bat",
            "gradle/wrapper/gradle-wrapper.properties", "gradle.properties",
            "source/shared/gradle.properties", "source/desktop/gradle.properties",
            "source/shared/common/build.gradle.kts", "source/shared/common-jre/build.gradle.kts",
            "source/desktop/desktop/build.gradle.kts")
        Invoke-FixtureGit $repository @("commit", "--quiet", "-m", "Fake-first verification fixture")
    }
    $copyPath = Join-Path $caseRoot ("verification-runtime-fixture-" + [guid]::NewGuid().ToString("N") + ".psm1")
    [IO.File]::Copy($ModulePath, $copyPath, $false)
    if ((Get-FileHash -LiteralPath $copyPath -Algorithm SHA256).Hash.ToLowerInvariant() -cne $ModuleSha256) {
        throw "Runtime module changed during fixture creation; mixed-source tests are invalid."
    }
    $module = Import-Module -Name $copyPath -PassThru
    $fixture = [pscustomobject]@{
        Root = $caseRoot; RepositoryRoot = $repository; JdkDirectory = $jdk
        GradleUserHome = $gradleHome; Module = $module; ModulePath = $copyPath
        Logs = Join-Path $caseRoot "canonical-logs"; ConsumerLog = Join-Path $caseRoot "consumer.log"
        GitInitialized = -not [bool]$WithoutGit
    }
    if (-not $UseOriginalNative) { Install-FixtureNative $fixture }
    if ($WithoutGit) {
        & $module {
            $script:FixtureNoGitRequests = 0
            function script:Invoke-VerificationGit {
                param([string]$RepositoryRoot, [string[]]$Arguments)
                $script:FixtureNoGitRequests++
                throw "This fixture prohibits Git execution."
            }
        }
    }
    return $fixture
}

function Invoke-CanonicalFixture {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [ValidateSet("COMPOSED", "FULL")] [string]$Level = "COMPOSED",
        [switch]$AllowToolchainDownload
    )
    & $Fixture.Module {
        param($RepositoryRoot, $Logs, $Level, $AllowDownload)
        Invoke-GeoCeDGCanonicalBuild -RepositoryRoot $RepositoryRoot -LogDirectory $Logs -Level $Level -AllowToolchainDownload:$AllowDownload
    } $Fixture.RepositoryRoot $Fixture.Logs $Level ([bool]$AllowToolchainDownload)
}

function Invoke-DevFixture {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [ValidateSet("shared", "desktop")] [string]$Module = "shared",
        [AllowEmptyCollection()] [AllowEmptyString()] [AllowNull()] [string[]]$Filters
    )
    & $Fixture.Module {
        param($Root, $Logs, $Module, $Filters)
        Invoke-GeoCeDGDevVerification -RepositoryRoot $Root -Module $Module -TestFilter $Filters -LogDirectory $Logs
    } $Fixture.RepositoryRoot $Fixture.Logs $Module $Filters
}

function Clear-FixtureIndependentReports {
    param([Parameter(Mandatory)] [object]$Fixture, [ValidateSet("shared", "desktop")] [string]$Module)
    & $Fixture.Module {
        param($Root, $Module)
        Clear-GeoCeDGIndependentFullTestReports -RepositoryRoot $Root -Module $Module
    } $Fixture.RepositoryRoot $Module
}

function Invoke-IndependentFixture {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [ValidateSet("shared", "desktop")] [string]$Module,
        [switch]$AlreadyCleared
    )
    if (-not $AlreadyCleared) { Clear-FixtureIndependentReports $Fixture -Module $Module }
    $runRoot = Join-Path $Fixture.Logs ("independent-" + [guid]::NewGuid().ToString("N"))
    $cwd = if ($Module -ceq "shared") { Join-Path $Fixture.RepositoryRoot "source/shared" } else { $Fixture.RepositoryRoot }
    [void][IO.Directory]::CreateDirectory($cwd)
    $task = if ($Module -ceq "shared") { ":common-jre:test" } else { ":desktop:desktop:test" }
    $native = & $Fixture.Module {
        param($Root, $Cwd, $Task, $LogPath)
        Invoke-VerificationNative -FilePath (Join-Path $Root "gradlew.bat") -WorkingDirectory $Cwd -Arguments @(
            $Task, "--info", "--console=plain") -LogPath $LogPath -Description "FAKE-FIRST independent FULL output"
    } $Fixture.RepositoryRoot $cwd $task (Join-Path $runRoot "gradle.log")
    [pscustomobject]@{
        Module = $Module; WorkingDirectory = $cwd; LogPath = $native.logPath
        ArchiveDirectory = Join-Path $runRoot "junit"; Native = $native
    }
}

function Assert-FixtureIndependentOutcome {
    param([Parameter(Mandatory)] [object]$Fixture, [Parameter(Mandatory)] [object]$Run, [string]$WorkingDirectory)
    if ([string]::IsNullOrEmpty($WorkingDirectory)) { $WorkingDirectory = $Run.WorkingDirectory }
    & $Fixture.Module {
        param($Root, $Module, $Cwd, $LogPath, $Archive)
        Assert-GeoCeDGIndependentFullTestOutcome -RepositoryRoot $Root -Module $Module -WorkingDirectory $Cwd -LogPath $LogPath -ArchiveDirectory $Archive
    } $Fixture.RepositoryRoot $Run.Module $WorkingDirectory $Run.LogPath $Run.ArchiveDirectory
}

function Assert-FixtureCanonicalPreflightReject {
    param([Parameter(Mandatory)] [object]$Fixture, [Parameter(Mandatory)] [string]$Pattern)
    $previousLogs = $Fixture.Logs
    $Fixture.Logs = Join-Path $previousLogs ("preflight-" + [guid]::NewGuid().ToString("N"))
    try {
        Assert-TestThrows { Invoke-CanonicalFixture $Fixture } $Pattern "Canonical external-input preflight"
        Assert-TestCondition (@(& $Fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Unsafe external inputs reached a native probe."
        Assert-NoConsumableFixtureReceipt $Fixture
    } finally { $Fixture.Logs = $previousLogs }
}

function Confirm-FixtureEvidence {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [object]$Build,
        [string[]]$Arguments = @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest"),
        [string]$WorkingDirectory,
        [string]$RepositoryRoot,
        [string]$EvidencePath,
        [switch]$AllowToolchainDownload
    )
    if ([string]::IsNullOrEmpty($WorkingDirectory)) { $WorkingDirectory = $Fixture.RepositoryRoot }
    if ([string]::IsNullOrEmpty($RepositoryRoot)) { $RepositoryRoot = $Fixture.RepositoryRoot }
    if ([string]::IsNullOrEmpty($EvidencePath)) { $EvidencePath = $Build.EvidencePath }
    & $Fixture.Module {
        param($EvidencePath, $RepositoryRoot, $WorkingDirectory, $Arguments, $LogPath, $AllowDownload)
        Confirm-GeoCeDGBuildEvidence -EvidencePath $EvidencePath -RepositoryRoot $RepositoryRoot -WorkingDirectory $WorkingDirectory -Arguments $Arguments -LogPath $LogPath -Description "FAKE-FIRST receipt consumer" -AllowToolchainDownload:$AllowDownload
    } $EvidencePath $RepositoryRoot $WorkingDirectory $Arguments $Fixture.ConsumerLog ([bool]$AllowToolchainDownload)
}

function Close-FixtureEvidence {
    param([Parameter(Mandatory)] [object]$Fixture, [Parameter(Mandatory)] [object]$Build, [switch]$ExpectInvalidEvidence)
    if ($ExpectInvalidEvidence) {
        Assert-TestThrows {
            & $Fixture.Module { param($Token) Close-GeoCeDGBuildEvidence -OwnerToken $Token } $Build.OwnerToken
        } "inputs/environment changed|Ignored non-generated|Missing or altered current-run report|receipt changed|receipt.*altered|artifact.*changed|artifact.*altered" "Completion revalidation of altered evidence"
        Assert-TestThrows { Confirm-FixtureEvidence $Fixture $Build } "not owned.*active invocation" "Failed completion retained an active capability"
    } else {
        & $Fixture.Module { param($Token) Close-GeoCeDGBuildEvidence -OwnerToken $Token } $Build.OwnerToken
    }
}

function Assert-NoConsumableFixtureReceipt {
    param([Parameter(Mandatory)] [object]$Fixture)
    $receipts = @(Get-ChildItem -LiteralPath $Fixture.Logs -Filter "build-evidence.json" -Recurse -File)
    Assert-TestCondition ($receipts.Count -eq 0) "A failed producer emitted consumable receipt bytes."
    $failures = @(Get-ChildItem -LiteralPath $Fixture.Logs -Filter "failed-build.json" -Recurse -File)
    Assert-TestCondition ($failures.Count -eq 1) "A failed producer did not preserve one failure record."
    $record = Get-Content -LiteralPath $failures[0].FullName -Raw | ConvertFrom-Json
    Assert-TestCondition ($record.state -ceq "FAILED_NO_CONSUMABLE_RECEIPT") "Failure record has the wrong state."
    $dummy = [pscustomobject]@{ EvidencePath = Join-Path $Fixture.Root "not-a-receipt.json" }
    Assert-TestThrows { Confirm-FixtureEvidence $Fixture $dummy } "not owned.*active invocation" "Failed producer left consumable module state"
}

function Invoke-RuntimeTest {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [scriptblock]$Body,
        [switch]$UseOriginalNative,
        [switch]$WithoutGit
    )
    $script:CaseNumber++
    $savedEnvironment = @{}
    foreach ($environmentName in @("GRADLE_USER_HOME", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "GRADLE_OPTS", "CI")) {
        $savedEnvironment[$environmentName] = [Environment]::GetEnvironmentVariable($environmentName, [EnvironmentVariableTarget]::Process)
    }
    $fixture = $null
    $watch = [Diagnostics.Stopwatch]::StartNew()
    $status = "PASS"
    $errorText = $null
    Write-Host ("==> FAKE-FIRST " + $Name)
    try {
        $fixture = New-TestFixture -UseOriginalNative:$UseOriginalNative -WithoutGit:$WithoutGit
        [Environment]::SetEnvironmentVariable("GRADLE_USER_HOME", $fixture.GradleUserHome, [EnvironmentVariableTarget]::Process)
        foreach ($environmentName in @("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "GRADLE_OPTS")) {
            [Environment]::SetEnvironmentVariable($environmentName, $null, [EnvironmentVariableTarget]::Process)
        }
        & $Body $fixture
    } catch {
        $status = "FAIL"
        $errorText = $_.Exception.Message
        Write-Warning ($Name + ": " + $errorText)
    } finally {
        $watch.Stop()
        if ($null -ne $fixture) { Remove-Module -ModuleInfo $fixture.Module -ErrorAction SilentlyContinue }
        foreach ($environmentName in $savedEnvironment.Keys) {
            [Environment]::SetEnvironmentVariable($environmentName, $savedEnvironment[$environmentName], [EnvironmentVariableTarget]::Process)
        }
    }
    $Results.Add([ordered]@{
        name = $Name; status = $status; elapsedSeconds = $watch.Elapsed.TotalSeconds
        error = $errorText; fixture = $(if ($null -eq $fixture) { $null } else { $fixture.Root })
    })
}

Invoke-RuntimeTest "operational entrypoints declare the PowerShell 7.2 native-stderr floor" -WithoutGit {
    param($fixture)
    $minimum = [version]"7.2"
    foreach ($entryPath in @($RootVerifierPath, $ModulePath, $InfrastructureVerifierPath,
            $RuntimeFixturePath, $GeneratedStateTestsPath)) {
        $tokens = $null
        $errors = $null
        $resolvedPath = (Resolve-Path -LiteralPath $entryPath).Path
        $entryAst = [Management.Automation.Language.Parser]::ParseFile($resolvedPath, [ref]$tokens, [ref]$errors)
        Assert-TestCondition (@($errors).Count -eq 0) "PowerShell floor authority does not parse: $resolvedPath"
        Assert-TestCondition ($null -ne $entryAst.ScriptRequirements -and
            $entryAst.ScriptRequirements.RequiredPSVersion -eq $minimum) "Missing exact PowerShell 7.2 minimum: $resolvedPath"
    }
    Assert-TestCondition (-not $fixture.GitInitialized -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "AST-floor fixture requested Git or native work."
}

Invoke-RuntimeTest "baseline committed whitespace check is CRLF-invariant but rejects real trailing blanks" {
    param($fixture)
    $configuration = "blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol"
    $baselineSource = Get-Content -Raw -LiteralPath $BaselineVerifierPath
    Assert-TestCondition (@([regex]::Matches($baselineSource,
                [regex]::Escape('core.whitespace=$CanonicalGitWhitespaceConfiguration'))).Count -eq 3) `
        "Baseline verifier must bind all three whitespace checks to one explicit canonical policy."
    Assert-TestCondition ($baselineSource.Contains(
            '$CanonicalGitWhitespaceConfiguration =')) `
        "Baseline verifier does not declare the canonical whitespace policy."

    $path = Join-Path $fixture.RepositoryRoot "committed-whitespace.txt"
    [IO.File]::WriteAllText($path, "alpha`nbeta`n", [Text.UTF8Encoding]::new($false))
    Invoke-FixtureGit $fixture.RepositoryRoot @("add", "committed-whitespace.txt")
    Invoke-FixtureGit $fixture.RepositoryRoot @("commit", "-m", "LF baseline")
    $lfCommit = (& git -C $fixture.RepositoryRoot rev-parse HEAD).Trim()

    [IO.File]::WriteAllText($path, "alpha`r`nbeta`r`n", [Text.UTF8Encoding]::new($false))
    Invoke-FixtureGit $fixture.RepositoryRoot @("add", "committed-whitespace.txt")
    Invoke-FixtureGit $fixture.RepositoryRoot @("commit", "-m", "CRLF representation")
    $crlfCommit = (& git -C $fixture.RepositoryRoot rev-parse HEAD).Trim()
    Assert-TestCondition (([Text.Encoding]::UTF8.GetString(
                [IO.File]::ReadAllBytes($path))).Contains("`r`n")) `
        "Controlled CRLF fixture lost its physical CRLF representation."
    $PSNativeCommandUseErrorActionPreference = $false
    $crlfOutput = @(& git -c "core.whitespace=$configuration" -C $fixture.RepositoryRoot `
            diff --check "$lfCommit..$crlfCommit" 2>&1 | ForEach-Object { $_.ToString() })
    Assert-TestCondition ($LASTEXITCODE -eq 0 -and $crlfOutput.Count -eq 0) `
        "Canonical whitespace check treated CRLF representation as trailing blanks."

    [IO.File]::WriteAllText($path, "alpha  `r`nbeta`r`n", [Text.UTF8Encoding]::new($false))
    Invoke-FixtureGit $fixture.RepositoryRoot @("add", "committed-whitespace.txt")
    Invoke-FixtureGit $fixture.RepositoryRoot @("commit", "-m", "Real trailing blanks")
    $blankCommit = (& git -C $fixture.RepositoryRoot rev-parse HEAD).Trim()
    $blankOutput = @(& git -c "core.whitespace=$configuration" -C $fixture.RepositoryRoot `
            diff --check "$crlfCommit..$blankCommit" 2>&1 | ForEach-Object { $_.ToString() })
    $blankExit = $LASTEXITCODE
    Assert-TestCondition ($blankExit -ne 0 -and
        (($blankOutput -join $Lf) -match "trailing whitespace")) `
        "Canonical CRLF policy failed to detect a real committed trailing-space mutation."
}

Invoke-RuntimeTest "incremental policy retains selectors and context, forces only Test tasks" {
    param($fixture)
    $original = @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest",
        ":shared:common:checkstyleMain", "--rerun-tasks", "--no-build-cache", "--no-daemon",
        "--configuration-cache", "--parallel", "--console=plain", "--max-workers=2",
        "-Dorg.gradle.java.installations.auto-download=false")
    $before = @($original)
    $actual = @(& $fixture.Module { param($Items) ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $Items -KeepBuildOutputs } $original)
    $expected = @(":shared:common-jre:test", "--rerun", "--tests", "org.geocedg.fixture.SharedTest",
        ":shared:common:checkstyleMain", "--console=plain", "--max-workers=2",
        "-Dorg.gradle.java.installations.auto-download=false", "--build-cache",
        "--no-configuration-cache", "--no-parallel", "--daemon")
    Assert-TestSequence $expected $actual "Incremental transformation changed task/filter context"
    Assert-TestSequence $before $original "Incremental transformation mutated its caller's array"
    Assert-TestCondition ($actual -cnotcontains "clean") "Incremental transformation introduced clean."
}

Invoke-RuntimeTest "incremental policy is idempotent and defaults to isolated daemon/one worker" {
    param($fixture)
    $inputArgs = @(":shared:common-jre:test", "--rerun", "--tests", "org.geocedg.fixture.SharedTest",
        ":desktop:desktop:test", "--tests", "org.geocedg.fixture.DesktopTest", "--build-cache")
    $first = @(& $fixture.Module { param($Items) ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $Items } $inputArgs)
    $second = @(& $fixture.Module { param($Items) ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $Items } $first)
    Assert-TestSequence $first $second "Repeated policy application is not idempotent"
    Assert-TestCondition (@($first | Where-Object { $_ -ceq "--rerun" }).Count -eq 2) "Each Test task must have exactly one adjacent rerun."
    Assert-TestCondition ($first -ccontains "--no-daemon" -and $first -ccontains "--max-workers=1") "Default isolation/worker policy is missing."
    Assert-TestCondition ($first -cnotcontains "--rerun-tasks") "Dependency reruns leaked into incremental mode."
}

Invoke-RuntimeTest "task headings retain distinct names and reject conflicting execution outcomes" -WithoutGit {
    param($fixture)
    $tasks = @(& $fixture.Module {
        $text = @(
            "> Task :shared:common-jre:test",
            "> Task :shared:common-jre:test",
            "> Task :shared:common:checkstyleMain FROM-CACHE",
            "> Task :desktop:desktop:test",
            "> Task :desktop:desktop:test FAILED"
        ) -join $script:Lf
        Get-GradleTaskEvidence -Text $text
    })
    Assert-TestCondition ($tasks.Count -eq 3 -and @($tasks | Where-Object { [string]::IsNullOrEmpty($_.task) }).Count -eq 0) "Task dictionaries lost their names during grouping."
    $shared = @($tasks | Where-Object { $_.task -ceq ":shared:common-jre:test" })
    $style = @($tasks | Where-Object { $_.task -ceq ":shared:common:checkstyleMain" })
    Assert-TestCondition ($shared.Count -eq 1 -and $shared[0].headingOccurrences -eq 2 -and $shared[0].outcome -ceq "EXECUTED") "Repeated info headings became another execution or a different task."
    Assert-TestCondition ($style.Count -eq 1 -and $style[0].outcome -ceq "FROM-CACHE") "Style cache evidence was mixed with Test execution."
    & $fixture.Module { param($Tasks) Assert-FreshTestTask -Tasks $Tasks -Task ":shared:common-jre:test" } $tasks
    Assert-TestThrows {
        & $fixture.Module { param($Tasks) Assert-FreshTestTask -Tasks $Tasks -Task ":desktop:desktop:test" } $tasks
    } "unambiguous fresh execution" "A failed/conflicting Test heading became successful evidence"
    Assert-TestCondition (-not $fixture.GitInitialized -and @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Pure heading regression invoked Git or a fake build."
}

Invoke-RuntimeTest "child modes reject evidence/static/incremental combinations" {
    param($fixture)
    & $fixture.Module {
        Assert-GeoCeDGChildVerificationMode
        Assert-GeoCeDGChildVerificationMode -SkipBuild
        Assert-GeoCeDGChildVerificationMode -IncrementalBuild
        Assert-GeoCeDGChildVerificationMode -BuildEvidencePath "fixture-receipt"
    }
    foreach ($flags in @(
            @{ SkipBuild = $true; IncrementalBuild = $true },
            @{ SkipBuild = $true; BuildEvidencePath = "fixture-receipt" },
            @{ IncrementalBuild = $true; BuildEvidencePath = "fixture-receipt" },
            @{ SkipBuild = $true; IncrementalBuild = $true; BuildEvidencePath = "fixture-receipt" })) {
        Assert-TestThrows { & $fixture.Module { param($Flags) Assert-GeoCeDGChildVerificationMode @Flags } $flags } "cannot be combined|static-only" "Incompatible child execution modes"
    }
}

Invoke-RuntimeTest "G9U1 phase registration retains explicit scope and no implicit build" -WithoutGit {
    param($fixture)
    $phase = & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9U1' }
    Assert-TestCondition ($phase.Phase -ceq 'G9U1' -and
        $phase.Verifier -ceq 'verify-g9u1-construction-workspace.ps1') "G9U1 PHASE mapping is not exact."
    Assert-TestThrows {
        & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9U1-UNKNOWN' }
    } "Unknown PHASE" "Unknown workspace phase must not broaden scope"
    Assert-TestCondition (-not $fixture.GitInitialized -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Phase lookup launched a build."
}

Invoke-RuntimeTest "R1 phase registration is exact and cannot broaden an unknown phase" -WithoutGit {
    param($fixture)
    $phase = & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9S1-R1' }
    Assert-TestCondition ($phase.Phase -ceq 'G9S1-R1' -and
        $phase.Verifier -ceq 'verify-g9s1-r1-spline-pair-materialization.ps1') "R1 PHASE did not select its bounded scientific verifier."
    $lowerCase = & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'g9s1-r1' }
    Assert-TestCondition ($lowerCase.Phase -ceq $phase.Phase -and
        $lowerCase.Verifier -ceq $phase.Verifier) "Canonical case normalization changed the R1 phase authority."
    Assert-TestThrows {
        & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9S1-R1-UNKNOWN' }
    } "Unknown PHASE" "Unknown R1-like phase must not silently select R1 or a broader gate"
    Assert-TestCondition (-not $fixture.GitInitialized -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Phase lookup launched Git or a fake build."
    $r1Verifier = Join-Path (Split-Path -Parent $RootVerifierPath) $phase.Verifier
    $tokens = $null
    $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseFile(
        $r1Verifier, [ref]$tokens, [ref]$errors)
    $sum = @($ast.FindAll({ param($node)
        $node -is [Management.Automation.Language.AssignmentStatementAst] -and
            $node.Left.Extent.Text -ceq '$total'
    }, $true))
    Assert-TestCondition ($errors.Count -eq 0 -and $sum.Count -eq 1) "R1 total expression is ambiguous."
    $actual = & {
        param($assignment)
        $results = @([ordered]@{ class = 'shared'; tests = 150 },
            [ordered]@{ class = 'desktop'; tests = 3 })
        . $assignment
        $total
    } ([scriptblock]::Create($sum[0].Extent.Text))
    Assert-TestCondition ($actual -eq 153) "R1 reporting must sum validated ordered-dictionary counts."
}

Invoke-RuntimeTest "R4 descendant lexical authority is sealed while current compatibility tests remain live" -WithoutGit {
    param($fixture)
    $r4Verifier = Join-Path (Split-Path -Parent $RootVerifierPath) `
        "verify-g9u0-r4-intersection-admissibility-continuation.ps1"
    $tokens = $null
    $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseFile(
        $r4Verifier, [ref]$tokens, [ref]$errors)
    Assert-TestCondition ($errors.Count -eq 0) "R4 authority helper source does not parse."
    $functionNames = @("Assert-Condition", "Resolve-RepositoryPath", "Resolve-RequiredFile",
        "Get-SourceAuthorityBytes", "Get-SourceAuthorityText")
    $functions = @($ast.FindAll({ param($node)
        $node -is [Management.Automation.Language.FunctionDefinitionAst] -and
            $node.Name -cin $functionNames
    }, $true))
    Assert-TestCondition ($functions.Count -eq $functionNames.Count) "R4 exact authority helpers are missing."
    $definitions = ($functions | ForEach-Object { $_.Extent.Text }) -join $Lf
    $productContract = @($ast.FindAll({ param($node)
        $node -is [Management.Automation.Language.FunctionDefinitionAst] -and
            $node.Name -ceq "Assert-ProductStaticContracts"
    }, $true))
    Assert-TestCondition ($productContract.Count -eq 1 -and
        $productContract[0].Extent.Text.Contains('$ledger = Get-SourceAuthorityText $LedgerPath') -and
        $productContract[0].Extent.Text.Contains('$authorTest = [IO.File]::ReadAllText(') -and
        $productContract[0].Extent.Text.Contains('$ledgerTest = [IO.File]::ReadAllText(') -and
        $productContract[0].Extent.Text.Contains('$nativeArchiveTest = [IO.File]::ReadAllText(')) `
        "R4 product history or current compatibility-test source boundary drifted."
    $relativePath = "source/fixture-ledger.java"
    $workingPath = Join-Path $fixture.RepositoryRoot $relativePath
    Write-FixtureText $workingPath 'private static final String FORMAT_VERSION = "5";'
    $authorityModule = New-Module -Name ("R4AuthorityFixture" + [guid]::NewGuid().ToString("N")) `
        -ArgumentList $definitions, $fixture.RepositoryRoot -ScriptBlock {
        param($Definitions, $Root)
        . ([scriptblock]::Create($Definitions))
        $script:RepositoryRoot = $Root
        $script:R4BoundaryMode = "TAGGED_DESCENDANT"
        $script:R4AuthorityCommit = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        $script:LastBlobObject = $null
        function Get-GitBlobBytes {
            param([Parameter(Mandatory)] [string]$Object)
            $script:LastBlobObject = $Object
            if ($Object -cne "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa:source/fixture-ledger.java") {
                throw "Unexpected historical blob request: $Object"
            }
            return ,([Text.UTF8Encoding]::new($false).GetBytes(
                'private static final String FORMAT_VERSION = "4";'))
        }
    }
    try {
        $sealed = & $authorityModule { param($Path) Get-SourceAuthorityText $Path } $relativePath
        Assert-TestCondition ($sealed -ceq 'private static final String FORMAT_VERSION = "4";') `
            "Current v5 product source replaced sealed v4 historical authority."
        Write-FixtureText $workingPath 'private static final String FORMAT_VERSION = "SEMANTIC_MUTATION";'
        $sealedAgain = & $authorityModule { param($Path) Get-SourceAuthorityText $Path } $relativePath
        Assert-TestCondition ($sealedAgain -ceq $sealed) "A working-tree mutation changed sealed historical authority."
        $live = & $authorityModule {
            param($Path)
            $script:R4BoundaryMode = "WORKTREE"
            $script:R4AuthorityCommit = $null
            $script:LastBlobObject = $null
            Get-SourceAuthorityText $Path
        } $relativePath
        Assert-TestCondition ($live -ceq 'private static final String FORMAT_VERSION = "SEMANTIC_MUTATION";' -and
            $null -eq (& $authorityModule { $script:LastBlobObject })) `
            "An unsealed candidate failed to consume actual current source bytes."
        Assert-TestThrows {
            & $authorityModule { Get-SourceAuthorityText "../outside.java" }
        } "escapes repository" "Historical helper allowed a source path to escape the repository"
    } finally { Remove-Module -ModuleInfo $authorityModule -ErrorAction SilentlyContinue }
    Assert-TestCondition (-not $fixture.GitInitialized -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) `
        "Sealed-authority regression invoked Git or a fake build."
}

Invoke-RuntimeTest "canonical producer runs two isolated module selections and honest consumers" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        $calls = @(& $fixture.Module { @($script:FixtureNativeCalls) })
        Assert-TestCondition ($calls.Count -eq 4) "Canonical producer must use version/toolchains and two Test launches."
        $testCalls = @($calls | Where-Object { $_.arguments -ccontains ":shared:common-jre:test" -or $_.arguments -ccontains ":desktop:desktop:test" })
        Assert-TestCondition ($testCalls.Count -eq 2) "Shared and Desktop Test contexts were combined or repeated."
        Assert-TestCondition (($testCalls[0].arguments -ccontains ":shared:common-jre:test") -and
            ($testCalls[0].arguments -cnotcontains ":desktop:desktop:test")) "First Test batch is not shared-only."
        Assert-TestCondition (($testCalls[1].arguments -ccontains ":desktop:desktop:test") -and
            ($testCalls[1].arguments -cnotcontains ":shared:common-jre:test")) "Second Test batch is not Desktop-only."
        Assert-TestCondition ($testCalls[1].arguments -cnotcontains "org.geogebra.common.kernel.commands.RedefineTest") "Shared upstream selector leaked into Desktop."
        $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
        Assert-TestCondition ($receipt.state -ceq "TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING") "Producer claimed phase acceptance."
        Assert-TestCondition (-not $receipt.authorApproved -and -not $receipt.selfApproved -and -not $receipt.testResultReuseAcrossRuns) "Producer claimed approval or cross-run reuse."
        $output = @(Confirm-FixtureEvidence $fixture $build -Arguments @(
            ":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest",
            ":shared:common:checkstyleMain", ":shared:common-jre:checkstyleTest"))
        Assert-TestCondition ($output.Count -eq 0) "Consumer emitted a pipeline success value."
        $text = Get-Content -LiteralPath $fixture.ConsumerLog -Raw
        Assert-TestCondition ($text.Contains("was not executed again") -and $text.Contains("own original XML/method/count/style assertions")) "Consumer log misrepresents native work or phase obligations."
        Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 4) "Evidence consumer launched native work."
        Confirm-FixtureEvidence $fixture $build -WorkingDirectory (Join-Path $fixture.RepositoryRoot "source/shared") -Arguments @(":canvas-base:compileJava", ":renderer-base:compileJava")
        Confirm-FixtureEvidence $fixture $build -WorkingDirectory (Join-Path $fixture.RepositoryRoot "source/shared") -Arguments @(":common-jre:test", "--tests", "org.geocedg.fixture.SharedTest")
        Confirm-FixtureEvidence $fixture $build -Arguments @(":desktop:desktop:test", "--tests", "org.geocedg.fixture.DesktopTest")
        Import-Module -Name $fixture.ModulePath
        Confirm-FixtureEvidence $fixture $build
    } finally { Close-FixtureEvidence $fixture $build }
}

Invoke-RuntimeTest "stale imported runtime rejects every non-close entrypoint before work" -WithoutGit {
    param($fixture)
    $loadedIdentity = & $fixture.Module { $script:ModuleIdentity }
    $loadedHash = & $fixture.Module { $script:LoadedRuntimeSha256 }
    Set-FixtureRuntimeSourceMutation $fixture
    $reimported = Import-Module -Name $fixture.ModulePath -PassThru
    Assert-TestCondition ((& $reimported { $script:ModuleIdentity }) -ceq $loadedIdentity) "Unforced import unexpectedly replaced the loaded module instance."
    Assert-TestCondition ((Get-FileHash -LiteralPath $fixture.ModulePath -Algorithm SHA256).Hash.ToLowerInvariant() -cne $loadedHash) "Disposable module mutation did not change raw source bytes."
    & $fixture.Module {
        $script:FixtureRemovalRequests = 0
        function script:Remove-CurrentJUnitReports {
            param($RepositoryRoot, $Module)
            $script:FixtureRemovalRequests++
            throw "FIXTURE_GUARD_STALE_RUNTIME_REACHED_REMOVAL"
        }
    }
    $entrypoints = @(
        [pscustomobject]@{ Name = "incremental arguments"; Action = {
            param($case)
            & $case.Module { ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments @(':shared:common-jre:test') }
        } },
        [pscustomobject]@{ Name = "child mode, including static-only PHASE"; Action = {
            param($case)
            & $case.Module { Assert-GeoCeDGChildVerificationMode -SkipBuild }
        } },
        [pscustomobject]@{ Name = "canonical producer"; Action = {
            param($case)
            Invoke-CanonicalFixture $case
        } },
        [pscustomobject]@{ Name = "receipt consumer"; Action = {
            param($case)
            Confirm-FixtureEvidence $case ([pscustomobject]@{ EvidencePath = Join-Path $case.Root 'uncreated-receipt.json' })
        } },
        [pscustomobject]@{ Name = "phase lookup"; Action = {
            param($case)
            & $case.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9U0-R6' }
        } },
        [pscustomobject]@{ Name = "DEV producer"; Action = {
            param($case)
            Invoke-DevFixture $case -Module shared -Filters @('org.geocedg.fixture.SharedTest')
        } },
        [pscustomobject]@{ Name = "independent FULL clear"; Action = {
            param($case)
            Clear-FixtureIndependentReports $case -Module shared
        } },
        [pscustomobject]@{ Name = "independent FULL outcome"; Action = {
            param($case)
            & $case.Module {
                param($Root, $Logs)
                Assert-GeoCeDGIndependentFullTestOutcome -RepositoryRoot $Root -Module shared `
                    -WorkingDirectory $Root -LogPath (Join-Path $Logs 'not-created.log') `
                    -ArchiveDirectory (Join-Path $Logs 'not-created-archive')
            } $case.RepositoryRoot $case.Logs
        } }
    )
    foreach ($entrypoint in $entrypoints) {
        Assert-TestThrows { & $entrypoint.Action $fixture } "Loaded verification runtime is stale.*fresh PowerShell session" ("Stale imported " + $entrypoint.Name)
    }
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0 -and
        (& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0 -and
        (& $fixture.Module { $script:FixtureRemovalRequests }) -eq 0) "Stale source reached native, inventory, or mutation work."
    Assert-TestCondition ((& $fixture.Module { $null -eq $script:ActiveEvidence }) -and
        -not (Test-Path -LiteralPath $fixture.Logs) -and
        -not (Test-Path -LiteralPath $fixture.ConsumerLog)) "Stale source created ownership or verification artifacts."
}

Invoke-RuntimeTest "fresh runtime load binds changed copy bytes and rejects the old receipt" {
    param($fixture)
    $oldIdentity = & $fixture.Module { $script:ModuleIdentity }
    $oldHash = & $fixture.Module { $script:LoadedRuntimeSha256 }
    $oldBuild = Invoke-CanonicalFixture $fixture
    Close-FixtureEvidence $fixture $oldBuild
    Set-FixtureRuntimeSourceMutation $fixture
    # Explicitly unload only after closing ownership; never force a consumer import.
    Remove-Module -ModuleInfo $fixture.Module
    $fixture.Module = Import-Module -Name $fixture.ModulePath -PassThru
    Install-FixtureNative $fixture
    $newHash = (Get-FileHash -LiteralPath $fixture.ModulePath -Algorithm SHA256).Hash.ToLowerInvariant()
    Assert-TestCondition ($newHash -cne $oldHash -and
        (& $fixture.Module { $script:LoadedRuntimeSha256 }) -ceq $newHash -and
        (& $fixture.Module { $script:ModuleIdentity }) -cne $oldIdentity) "Fresh import did not bind the new raw bytes and module instance."
    $phase = & $fixture.Module { Get-GeoCeDGPhaseDefinition -Phase 'G9U0-R6' }
    Assert-TestCondition ($phase.Phase -ceq 'G9U0-R6') "Fresh runtime did not allow current source execution."
    Assert-TestThrows { Confirm-FixtureEvidence $fixture $oldBuild } "not owned.*active invocation" "Old receipt after fresh source import"
    $newBuild = Invoke-CanonicalFixture $fixture
    try {
        Confirm-FixtureEvidence $fixture $newBuild
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $oldBuild } "Foreign build-evidence path or repository" "Old receipt during a fresh-source run"
    } finally { Close-FixtureEvidence $fixture $newBuild }
}

Invoke-RuntimeTest "stale runtime consumption fails and close still clears ownership" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    $nativeCount = @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count
    Set-FixtureRuntimeSourceMutation $fixture
    Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "Loaded verification runtime is stale" "Consumption after imported source drift"
    Assert-TestThrows {
        & $fixture.Module { param($Token) Close-GeoCeDGBuildEvidence -OwnerToken $Token } $build.OwnerToken
    } "Loaded verification runtime is stale" "Close after imported source drift"
    Assert-TestCondition ((& $fixture.Module { $null -eq $script:ActiveEvidence }) -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq $nativeCount) "Stale close retained ownership or launched new native work."
}

foreach ($removalModule in @("shared", "desktop")) {
    foreach ($linkedTarget in @("repository", "source", "build", "test-results", "test", "report-file")) {
        Invoke-RuntimeTest ("JUnit removal rejects synthetic " + $removalModule + " " + $linkedTarget + " link before any removal") -WithoutGit {
            param($fixture)
            Install-FixtureRemovalProbe $fixture -Module $removalModule -LinkedTarget $linkedTarget
            $pattern = if ($linkedTarget -ceq "report-file") { "linked JUnit report" } else { "through a linked path" }
            Assert-TestThrows {
                & $fixture.Module { param($Root, $Module) Remove-CurrentJUnitReports -RepositoryRoot $Root -Module $Module } $fixture.RepositoryRoot $removalModule
            } $pattern "Unsafe JUnit removal path"
            $probe = & $fixture.Module { $script:FixtureRemovalProbe }
            Assert-TestCondition ($probe.Removed.Count -eq 0) "A report removal was attempted before every link check passed."
            Assert-TestCondition ($probe.Enumerations -eq $(if ($linkedTarget -ceq "report-file") { 1 } else { 0 })) "Ancestor rejection did not precede enumeration."
            Assert-TestCondition ($probe.GitCalls -eq 0 -and -not $fixture.GitInitialized) "Synthetic path guard used Git."
            Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Synthetic path guard requested native work."
        }
    }
}

Invoke-RuntimeTest "JUnit removal synthetic safe-path control reaches only fixture removal recorder" -WithoutGit {
    param($fixture)
    Install-FixtureRemovalProbe $fixture -Module shared -LinkedTarget none
    & $fixture.Module { param($Root) Remove-CurrentJUnitReports -RepositoryRoot $Root -Module shared } $fixture.RepositoryRoot
    $probe = & $fixture.Module { $script:FixtureRemovalProbe }
    Assert-TestCondition ($probe.Enumerations -eq 1 -and $probe.Removed.Count -eq 2) "Safe-path control did not exercise the unmodified removal branch."
    Assert-TestCondition ($probe.ItemReads.Count -eq $probe.Ancestors.Count) "Safe-path control did not inspect every ancestor through the repository."
    Assert-TestCondition ($probe.GitCalls -eq 0 -and -not $fixture.GitInitialized) "Synthetic safe-path control used Git."
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Synthetic safe-path control requested native work."
}

Invoke-RuntimeTest "closed receipt cannot be reused or self-authorized from disk" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    Close-FixtureEvidence $fixture $build
    Assert-TestCondition (Test-Path -LiteralPath $build.EvidencePath -PathType Leaf) "Stale-receipt fixture bytes disappeared."
    Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "not owned.*active invocation" "Closed receipt reuse"
}

Invoke-RuntimeTest "forged standalone receipt has no in-memory authority" {
    param($fixture)
    $path = Join-Path $fixture.Root "forged.json"
    Write-FixtureText $path '{"schemaVersion":1,"kind":"CURRENT_RUN_BUILD_EVIDENCE","state":"TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING","level":"FULL","authorApproved":true}'
    $build = [pscustomobject]@{ EvidencePath = $path }
    Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "not owned.*active invocation" "Forged receipt"
}

Invoke-RuntimeTest "sealed receipt byte tampering is rejected" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        $original = Get-Content -LiteralPath $build.EvidencePath -Raw
        Write-FixtureText $build.EvidencePath ($original.Replace('"selfApproved": false', '"selfApproved": true'))
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "receipt was altered" "Sealed receipt tampering"
    } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
}

Invoke-RuntimeTest "receipt path and repository ownership cannot be substituted" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        $copy = Join-Path $fixture.Root "copied-receipt.json"
        [IO.File]::Copy($build.EvidencePath, $copy, $false)
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -EvidencePath $copy } "Foreign build-evidence path or repository" "Receipt relocation"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -RepositoryRoot $fixture.Root } "Foreign build-evidence path or repository" "Repository substitution"
        Assert-TestThrows { & $fixture.Module { Close-GeoCeDGBuildEvidence -OwnerToken "forged-owner" } } "owning token" "Non-owner close"
        Confirm-FixtureEvidence $fixture $build
    } finally { Close-FixtureEvidence $fixture $build }
}

Invoke-RuntimeTest "receipt cannot cross a fresh module instance" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    $otherModule = $null
    try {
        $copy = Join-Path $fixture.Root ("verification-runtime-other-" + [guid]::NewGuid().ToString("N") + ".psm1")
        [IO.File]::Copy($ModulePath, $copy, $false)
        $otherModule = Import-Module -Name $copy -PassThru
        $parameters = @{
            EvidencePath = $build.EvidencePath; RepositoryRoot = $fixture.RepositoryRoot
            WorkingDirectory = $fixture.RepositoryRoot; Arguments = @(":shared:common-jre:test")
            LogPath = Join-Path $fixture.Root "foreign-consumer.log"; Description = "foreign module fixture"
        }
        Assert-TestThrows { & $otherModule { param($Parameters) Confirm-GeoCeDGBuildEvidence @Parameters } $parameters } "not owned.*active invocation" "Cross-module receipt reuse"
    } finally {
        if ($null -ne $otherModule) { Remove-Module -ModuleInfo $otherModule }
        Close-FixtureEvidence $fixture $build
    }
}

Invoke-RuntimeTest "source byte change is rejected even with identical size and mtime" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        $path = Join-Path $fixture.RepositoryRoot "source.txt"
        $before = Get-Item -LiteralPath $path
        $length = $before.Length
        $stamp = $before.LastWriteTimeUtc
        $rawBefore = & $fixture.Module { param($Root) (Get-RawInputInventory $Root).Sha256 } $fixture.RepositoryRoot
        Write-FixtureText $path ("SOURCE-B" + $Lf)
        [IO.File]::SetLastWriteTimeUtc($path, $stamp)
        $after = Get-Item -LiteralPath $path
        Assert-TestCondition ($after.Length -eq $length -and $after.LastWriteTimeUtc.Ticks -eq $stamp.Ticks) "Fixture failed to preserve size/mtime."
        $rawAfter = & $fixture.Module { param($Root) (Get-RawInputInventory $Root).Sha256 } $fixture.RepositoryRoot
        Assert-TestCondition ($rawBefore -cne $rawAfter) "Raw source inventory trusted size/mtime instead of bytes."
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "inputs/environment changed" "Changed source with preserved metadata"
    } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
}

Invoke-RuntimeTest "new untracked source invalidates current-run binding" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        Write-FixtureText (Join-Path $fixture.RepositoryRoot "new-input.txt") "untracked input"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "inputs/environment changed" "Untracked source addition"
    } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
}

Invoke-RuntimeTest "same-length toolchain byte change invalidates current-run binding" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        $path = Join-Path $fixture.JdkDirectory "release"
        $text = Get-Content -LiteralPath $path -Raw
        Write-FixtureText $path ($text.Replace("17.0.18", "17.0.19"))
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "inputs/environment changed" "Toolchain identity change"
    } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
}

Invoke-RuntimeTest "collapsed ignored boundaries are opaque and undeclared inputs still fail closed" -WithoutGit {
    param($fixture)
    # No Git command, real link, ignored-tree traversal or build is used. Only
    # the private Git response seam changes; Get-RawInputInventory is unchanged.
    Write-FixtureText (Join-Path $fixture.RepositoryRoot "artifacts/tracked.txt") "TRACKED_ARTIFACT_INPUT"
    Write-FixtureText (Join-Path $fixture.RepositoryRoot "build") "TRACKED_OPAQUE_BUILD_INPUT"
    Write-FixtureText (Join-Path $fixture.RepositoryRoot "bookkeeper.txt") "TRACKED_BOOKKEEPER_INPUT"
    Write-FixtureText (Join-Path $fixture.RepositoryRoot "source/ConsumedIgnored.java") "NEW_CONSUMED_IGNORED_INPUT"
    & $fixture.Module {
        $script:FixtureRawInput = [pscustomobject]@{
            Ignored = @("book", "book/", "artifacts/", "artifacts/knowledge/", "artifacts/knowledge/output.json",
                "build/", "module/build/", "module/build/result.bin", ".gradle/", "module/.kotlin/state.bin")
            CachedAndNonignored = @("source.txt", "artifacts/tracked.txt", "build", "bookkeeper.txt")
            Calls = [Collections.Generic.List[object]]::new()
        }
        function script:Invoke-VerificationGit {
            param([string]$RepositoryRoot, [string[]]$Arguments)
            $script:FixtureRawInput.Calls.Add([pscustomobject]@{ Arguments = @($Arguments) })
            $key = $Arguments -join [char]0
            if ($key -ceq (@("ls-files", "--others", "--ignored", "--exclude-standard", "--directory") -join [char]0)) {
                return @($script:FixtureRawInput.Ignored)
            }
            if ($key -ceq (@("ls-files", "--cached", "--others", "--exclude-standard") -join [char]0)) {
                return @($script:FixtureRawInput.CachedAndNonignored)
            }
            throw "Unexpected Git arguments in collapsed ignored-input fixture."
        }
    }
    $inventory = & $fixture.Module { param($Root) Get-RawInputInventory $Root } $fixture.RepositoryRoot
    Assert-TestSequence -Expected @("artifacts/tracked.txt", "bookkeeper.txt", "build", "source.txt") `
        -Actual @($inventory.Records.path) -Message "Ignored-tree exemptions leaked into tracked/nonignored inventory"
    Assert-TestCondition ($inventory.Files -eq 4 -and @($inventory.Records | Where-Object { -not $_.exists }).Count -eq 0) "Allowed opaque ignored entries altered raw input evidence."
    $calls = @(& $fixture.Module { @($script:FixtureRawInput.Calls) })
    Assert-TestCondition ($calls.Count -eq 2) "Allowed ignored directories did not proceed to exactly one unchanged cached/nonignored query."

    foreach ($unexpected in @("source/ConsumedIgnored.java", "source/ignored-directory/", "bookkeeper", "bookkeeper/",
            "docs/book/", "build", "source/build", ".gradle", "artifacts", "artifacts-extra/")) {
        & $fixture.Module { param($Path) $script:FixtureRawInput.Ignored = @($Path); $script:FixtureRawInput.Calls.Clear() } $unexpected
        Assert-TestThrows { & $fixture.Module { param($Root) Get-RawInputInventory $Root } $fixture.RepositoryRoot } `
            "Ignored non-generated" "Undeclared ignored input '$unexpected'"
        $calls = @(& $fixture.Module { @($script:FixtureRawInput.Calls) })
        Assert-TestCondition ($calls.Count -eq 1) "Undeclared ignored input reached the cached/nonignored query."
    }
    foreach ($malformed in @("warning: Filename too long: artifacts/knowledge/build/output", "C:/outside/build/output",
            "/outside/build/output", "../build/", "source//build/", 'source\build\output', '"source/build/output"')) {
        & $fixture.Module { param($Path) $script:FixtureRawInput.Ignored = @($Path); $script:FixtureRawInput.Calls.Clear() } $malformed
        Assert-TestThrows { & $fixture.Module { param($Root) Get-RawInputInventory $Root } $fixture.RepositoryRoot } `
            "Malformed ignored path evidence" "Malformed/warning evidence '$malformed'"
        $calls = @(& $fixture.Module { @($script:FixtureRawInput.Calls) })
        Assert-TestCondition ($calls.Count -eq 1) "Malformed ignored evidence was consumed as a generated tree."
    }
    Assert-TestCondition (-not $fixture.GitInitialized -and (& $fixture.Module { $script:FixtureNativeCalls.Count }) -eq 0) "Ignored inventory fixture escaped its no-Git/no-native scope."
}

Invoke-RuntimeTest "new ignored non-generated input is rejected rather than silently omitted" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        Write-FixtureText (Join-Path $fixture.RepositoryRoot "ignored-input.txt") "ignored but possibly operational"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build } "Ignored non-generated" "Ignored non-generated input"
    } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
}

Invoke-RuntimeTest "filtered receipt rejects full, foreign, missing, and cross-module scopes" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture
    try {
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test") } "unfiltered/FULL" "Filtered receipt used as FULL"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":desktop:desktop:publish") } "did not cover task" "Unobserved task"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test", "--tests", "org.geogebra.foreign.UnselectedTest") } "selection does not cover" "Unselected upstream filter"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.MissingTest") } "no inspectable current evidence" "Empty matching scope"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.DesktopTest") } "no inspectable current evidence" "Cross-module test scope"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @("--tests", "org.geocedg.fixture.SharedTest") } "no explicit test-task context" "Orphan test filter"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest", "-Dforeign=true") } "Unsupported build-evidence argument" "Unrecorded build property"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -WorkingDirectory (Join-Path $fixture.RepositoryRoot "unsupported") } "Unsupported Gradle working-directory" "Foreign cwd context"
        Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -AllowToolchainDownload } "different toolchain-download policy" "Changed download policy"
        foreach ($option in @("--parallel", "--configuration-cache", "--max-workers=2")) {
            Assert-TestThrows {
                Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest", $option)
            } "Unsupported|parallel|configuration-cache|max-workers" ("Contradictory execution context " + $option)
        }
    } finally { Close-FixtureEvidence $fixture $build }
}

Invoke-RuntimeTest "download-enabled receipt rejects explicit disable-download demand" {
    param($fixture)
    $build = Invoke-CanonicalFixture $fixture -AllowToolchainDownload
    try {
        $calls = @(& $fixture.Module { @($script:FixtureNativeCalls) })
        Assert-TestCondition ($calls.Count -eq 5) "Explicit download opt-in must add exactly one compiler preparation call."
        Assert-TestCondition ($calls[0].arguments -ccontains "--version" -and
            $calls[1].arguments -ccontains ":shared:common-jre:compileTestJava" -and
            $calls[1].arguments -ccontains ":desktop:desktop:compileTestJava" -and
            $calls[2].arguments -ccontains "javaToolchains" -and
            $calls[3].arguments -ccontains ":shared:common-jre:test" -and
            $calls[4].arguments -ccontains ":desktop:desktop:test") "Compiler preparation did not precede the sealed toolchain/Test sequence."
        $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
        $preparationArtifacts = @($receipt.auditArtifacts | Where-Object { [IO.Path]::GetFileName($_.path) -ceq "toolchain-preparation.log" })
        Assert-TestCondition ($preparationArtifacts.Count -eq 1 -and $receipt.nativeRuns.Count -eq 5) "Preparation provenance was not preserved in the receipt."
        $preparationText = Get-Content -LiteralPath $preparationArtifacts[0].path -Raw
        Assert-TestCondition ($preparationText -cnotmatch '(?m)^> Task .*:test(?:\s|$)' -and
            -not $preparationText.Contains("Starting process 'Gradle Test Executor")) "Compiler preparation supplied fabricated Test execution."
        Assert-TestThrows {
            Confirm-FixtureEvidence $fixture $build -AllowToolchainDownload -Arguments @(
                ":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest",
                "-Dorg.gradle.java.installations.auto-download=false")
        } "download|contradict" "Contradictory download semantics"
    } finally { Close-FixtureEvidence $fixture $build }
}

foreach ($target in @("live JUnit", "archived JUnit", "live Checkstyle")) {
    Invoke-RuntimeTest ("consumer rejects altered " + $target) {
        param($fixture)
        $build = Invoke-CanonicalFixture $fixture
        try {
            $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
            $report = @($receipt.junit | Where-Object { $_.class -ceq "org.geocedg.fixture.SharedTest" })[0]
            $arguments = @(":shared:common-jre:test", "--tests", "org.geocedg.fixture.SharedTest")
            $path = switch ($target) {
                "live JUnit" { Join-Path $fixture.RepositoryRoot $report.livePath }
                "archived JUnit" { $report.archivePath }
                "live Checkstyle" {
                    $style = @($receipt.checkstyle | Where-Object { $_.task -ceq ":shared:common:checkstyleMain" })[0]
                    $arguments = @(":shared:common:checkstyleMain")
                    Join-Path $fixture.RepositoryRoot $style.livePath
                }
            }
            $text = Get-Content -LiteralPath $path -Raw
            $stamp = [IO.File]::GetLastWriteTimeUtc($path)
            $changed = if ($target -ceq "live Checkstyle") { $text.Replace("10.12", "10.13") } else { $text.Replace("caseA", "caseB") }
            Assert-TestCondition ($text.Length -eq $changed.Length -and $text -cne $changed) "Tamper fixture did not change same-length bytes."
            Write-FixtureText $path $changed
            [IO.File]::SetLastWriteTimeUtc($path, $stamp)
            Assert-TestThrows { Confirm-FixtureEvidence $fixture $build -Arguments $arguments } "Missing or altered current-run report" ("Tampered " + $target)
        } finally { Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence }
    }
}

foreach ($outcome in @("UP-TO-DATE", "FROM-CACHE", "SKIPPED", "NO-SOURCE", "FAILED")) {
    Invoke-RuntimeTest ("canonical producer rejects Test outcome " + $outcome) {
        param($fixture)
        Set-FixtureSettings $fixture @{ TestOutcome = $outcome }
        Assert-TestThrows { Invoke-CanonicalFixture $fixture } "Test task did not provide unambiguous fresh execution evidence" ("Non-fresh Test task " + $outcome)
        Assert-NoConsumableFixtureReceipt $fixture
    }
}

Invoke-RuntimeTest "canonical producer rejects a missing Test-task heading" {
    param($fixture)
    Set-FixtureSettings $fixture @{ TestTaskMultiplicity = 0 }
    Assert-TestThrows { Invoke-CanonicalFixture $fixture } "Test task did not provide unambiguous fresh execution evidence" "Missing Test-task heading"
    Assert-NoConsumableFixtureReceipt $fixture
}

Invoke-RuntimeTest "canonical producer accepts repeated identical info headings with one fresh task identity" {
    param($fixture)
    Set-FixtureSettings $fixture @{ TestTaskMultiplicity = 2 }
    $build = Invoke-CanonicalFixture $fixture
    try {
        $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
        foreach ($taskName in @(":shared:common-jre:test", ":desktop:desktop:test")) {
            $tasks = @($receipt.tasks | Where-Object { $_.task -ceq $taskName })
            Assert-TestCondition ($tasks.Count -eq 1 -and $tasks[0].outcome -ceq "EXECUTED" -and
                $tasks[0].headingOccurrences -eq 2) "Repeated headings became duplicate execution identities."
            Assert-TestSequence @("EXECUTED") @($tasks[0].observedOutcomes) "Identical repeated headings changed the observed outcome"
        }
        $nativeLogs = @($receipt.nativeRuns | Where-Object { $_.arguments -ccontains ":shared:common-jre:test" -or $_.arguments -ccontains ":desktop:desktop:test" })
        foreach ($nativeLog in $nativeLogs) {
            $text = Get-Content -LiteralPath $nativeLog.logPath -Raw
            Assert-TestCondition ([regex]::Matches($text, "(?m)^Starting process 'Gradle Test Executor ").Count -eq 1) "Repeated-heading fixture fabricated multiple executor launches."
        }
        Confirm-FixtureEvidence $fixture $build
    } finally { Close-FixtureEvidence $fixture $build }
}

Invoke-RuntimeTest "canonical producer rejects conflicting outcomes for the same Test-task identity" {
    param($fixture)
    Set-FixtureSettings $fixture @{ ExtraTestOutcome = "UP-TO-DATE" }
    Assert-TestThrows { Invoke-CanonicalFixture $fixture } "Test task did not provide unambiguous fresh execution evidence" "Conflicting Test-task outcomes"
    $parsed = @(& $fixture.Module {
        Get-GradleTaskEvidence -Text (("> Task :shared:common-jre:test" + $script:Lf) +
            "> Task :shared:common-jre:test UP-TO-DATE")
    })
    Assert-TestCondition ($parsed.Count -eq 1 -and $parsed[0].outcome -ceq "CONFLICTING_LOG_OUTCOMES" -and
        $parsed[0].headingOccurrences -eq 2) "Conflicting task outcomes were not retained as ambiguous evidence."
    Assert-TestSequence @("EXECUTED", "UP-TO-DATE") @($parsed[0].observedOutcomes) "Conflicting raw outcomes were discarded"
    Assert-NoConsumableFixtureReceipt $fixture
}

foreach ($jvmCase in @("missing", "unbound")) {
    Invoke-RuntimeTest ("canonical producer rejects " + $jvmCase + " selected Test JVM evidence") {
        param($fixture)
        if ($jvmCase -ceq "missing") {
            Set-FixtureSettings $fixture @{ EmitTestJvmLaunch = $false }
        } else {
            $otherJdk = Join-Path $fixture.Root "unbound-jdk"
            Write-FixtureText (Join-Path $otherJdk "bin/java.exe") "fake unbound JVM"
            Set-FixtureSettings $fixture @{ TestJdkDirectory = $otherJdk }
        }
        Assert-TestThrows { Invoke-CanonicalFixture $fixture } "test-JVM launch evidence|Selected Test JVM was not bound" "Missing/unbound selected Test JVM"
        Assert-NoConsumableFixtureReceipt $fixture
    }
}

foreach ($settings in @(
        @{ VersionExitCode = 31 },
        @{ ToolchainExitCode = 32 },
        @{ SharedExitCode = 37 },
        @{ DesktopExitCode = 38 })) {
    Invoke-RuntimeTest ("canonical producer rejects native failure " + ($settings.Keys -join "")) {
        param($fixture)
        Set-FixtureSettings $fixture $settings
        Assert-TestThrows { Invoke-CanonicalFixture $fixture } "failed with exit (31|32|37|38)" "Native nonzero exit"
        Assert-NoConsumableFixtureReceipt $fixture
    }
}

Invoke-RuntimeTest "explicit toolchain preparation failure has no consumable evidence" -WithoutGit {
    param($fixture)
    Set-FixtureSettings $fixture @{ PreparationExitCode = 33 }
    Assert-TestThrows { Invoke-CanonicalFixture $fixture -AllowToolchainDownload } "Toolchain preparation failed with exit 33" "Native preparation failure"
    $calls = @(& $fixture.Module { @($script:FixtureNativeCalls) })
    Assert-TestCondition ($calls.Count -eq 2 -and $calls[0].arguments -ccontains "--version" -and
        $calls[1].arguments -ccontains ":shared:common-jre:compileTestJava") "Preparation failure continued into toolchain inventory or Test execution."
    Assert-NoConsumableFixtureReceipt $fixture
}

Invoke-RuntimeTest "failure-record publication failure preserves primary native failure and clears authority" -WithoutGit {
    param($fixture)
    Set-FixtureSettings $fixture @{ VersionExitCode = 31 }
    & $fixture.Module {
        $script:FixtureOriginalJsonWriter = (Get-Command -Name Write-VerificationJson -CommandType Function).ScriptBlock
        $script:FixtureDiagnosticAttempts = 0
        $script:FixtureUnexpectedGitCalls = 0
        function script:Write-VerificationJson {
            param([Parameter(Mandatory)] [string]$Path, [Parameter(Mandatory)] [object]$Value)
            if ([IO.Path]::GetFileName($Path) -ceq "failed-build.json") {
                $script:FixtureDiagnosticAttempts++
                throw "FAKE_FIRST_DIAGNOSTIC_PUBLICATION_FAILURE"
            }
            & $script:FixtureOriginalJsonWriter -Path $Path -Value $Value
        }
        function script:Invoke-VerificationGit {
            param([string]$RepositoryRoot, [string[]]$Arguments)
            $script:FixtureUnexpectedGitCalls++
            throw "Version failure must precede Git input inventory."
        }
    }
    $failure = $null
    try { Invoke-CanonicalFixture $fixture | Out-Null } catch { $failure = $_ }
    Assert-TestCondition ($null -ne $failure -and $failure.Exception.Message -match "Gradle version probe failed with exit 31") "Diagnostic I/O replaced the primary native failure."
    Assert-TestCondition ($failure.Exception.Data["DiagnosticPublicationFailure"] -ceq "FAKE_FIRST_DIAGNOSTIC_PUBLICATION_FAILURE") "Diagnostic I/O failure was not attached to the primary exception."
    $state = & $fixture.Module {
        [pscustomobject]@{
            Active = ($null -ne $script:ActiveEvidence)
            Attempts = $script:FixtureDiagnosticAttempts
            GitCalls = $script:FixtureUnexpectedGitCalls
            NativeCalls = $script:FixtureNativeCalls.Count
        }
    }
    Assert-TestCondition (-not $state.Active -and $state.Attempts -eq 1) "Failed diagnostic publication retained an active receipt or retried unexpectedly."
    Assert-TestCondition ($state.GitCalls -eq 0 -and $state.NativeCalls -eq 1 -and -not $fixture.GitInitialized) "Primary version failure continued into unrelated work."
    Assert-TestCondition (@(Get-ChildItem -LiteralPath $fixture.Logs -Filter "build-evidence.json" -Recurse -File).Count -eq 0) "Diagnostic publication failure left consumable receipt bytes."
    Assert-TestCondition (@(Get-ChildItem -LiteralPath $fixture.Logs -Filter "failed-build.json" -Recurse -File).Count -eq 0) "Diagnostic failure fixture unexpectedly wrote a failure record."
    $versionLogs = @(Get-ChildItem -LiteralPath $fixture.Logs -Filter "gradle-version.log" -Recurse -File)
    Assert-TestCondition ($versionLogs.Count -eq 1 -and [IO.File]::ReadAllText($versionLogs[0].FullName).Contains("FAKE-FIRST identity")) "Primary native log was lost along with diagnostic publication."
    $dummy = [pscustomobject]@{ EvidencePath = Join-Path $fixture.Root "not-a-receipt.json" }
    Assert-TestThrows { Confirm-FixtureEvidence $fixture $dummy } "not owned.*active invocation" "Failed publication retained consumable module state"
}

Invoke-RuntimeTest "JUnit structural checks retain realistic suite and case name attributes" -WithoutGit {
    param($fixture)
    $class = "org.geocedg.fixture.NamedSuiteTest"
    $path = Join-Path $fixture.RepositoryRoot "source/shared/common-jre/build/test-results/test/TEST-named-suite.xml"
    $archive = Join-Path $fixture.Root "named-suite-archive"
    Write-FixtureText $path ('<testsuite name="' + $class + '" tests="1" failures="0" errors="0" skipped="0" time="0.001">' +
        '<properties><property name="fixture" value="fake-first" /></properties>' +
        '<testcase classname="' + $class + '" name="namedCase()" time="0.001"><system-out>fixture output</system-out></testcase>' +
        '<system-out /><system-err /></testsuite>')
    $reports = @(& $fixture.Module {
        param($Root, $Archive)
        Get-JUnitEvidence -RepositoryRoot $Root -Module shared -ArchiveDirectory $Archive -Level DEV
    } $fixture.RepositoryRoot $archive)
    Assert-TestCondition ($reports.Count -eq 1 -and $reports[0].class -ceq $class -and
        $reports[0].cases.Count -eq 1 -and $reports[0].cases[0].name -ceq "namedCase()" -and
        $reports[0].cases[0].status -ceq "PASS") "Named XML attributes obscured structural node names or changed saved test identity."
    Assert-TestCondition ((Get-FileHash -LiteralPath $path).Hash -ceq
        (Get-FileHash -LiteralPath (Join-Path $archive "TEST-named-suite.xml")).Hash) "Named-suite XML was not archived exactly."
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "XML-only fixture launched a native command."
}

Invoke-RuntimeTest "JUnit rejects namespaced and unsupported outcome shapes instead of false PASS" -WithoutGit {
    param($fixture)
    $suiteOpen = '<testsuite name="org.geocedg.fixture.XmlShapeTest" tests="1" failures="0" errors="0" skipped="0">'
    $caseOpen = '<testcase classname="org.geocedg.fixture.XmlShapeTest" name="caseA">'
    $path = Join-Path $fixture.RepositoryRoot "source/shared/common-jre/build/test-results/test/TEST-shape.xml"
    $shapes = @(
        @{ Name = "prefixed root"; Xml = '<j:testsuite xmlns:j="urn:fixture" name="org.geocedg.fixture.XmlShapeTest" tests="1" failures="0" errors="0" skipped="0">' + $caseOpen + '</testcase></j:testsuite>' },
        @{ Name = "default root namespace"; Xml = $suiteOpen.Replace('<testsuite ', '<testsuite xmlns="urn:fixture" ') + $caseOpen + '</testcase></testsuite>' },
        @{ Name = "namespaced testcase"; Xml = $suiteOpen + '<j:testcase xmlns:j="urn:fixture" classname="org.geocedg.fixture.XmlShapeTest" name="caseA" /></testsuite>' },
        @{ Name = "unknown suite child"; Xml = $suiteOpen + $caseOpen + '</testcase><unexpected /></testsuite>' },
        @{ Name = "unknown testcase outcome"; Xml = $suiteOpen + $caseOpen + '<unexpected /></testcase></testsuite>' }
    )
    foreach ($outcome in @("failure", "error", "skipped")) {
        $shapes += @{ Name = "namespaced $outcome"; Xml = $suiteOpen + $caseOpen +
            '<j:' + $outcome + ' xmlns:j="urn:fixture" /></testcase></testsuite>' }
        $shapes += @{ Name = "default namespace $outcome"; Xml = $suiteOpen + $caseOpen +
            '<' + $outcome + ' xmlns="urn:fixture" /></testcase></testsuite>' }
    }
    foreach ($shape in $shapes) {
        Write-FixtureText $path $shape.Xml
        $archive = Join-Path $fixture.Root ("shape-archive-" + [guid]::NewGuid().ToString("N"))
        Assert-TestThrows {
            & $fixture.Module {
                param($Root, $Archive)
                Get-JUnitEvidence -RepositoryRoot $Root -Module shared -ArchiveDirectory $Archive -Level FULL
            } $fixture.RepositoryRoot $archive
        } "Unsupported JUnit" ("Unsupported XML " + $shape.Name)
    }
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "XML-only fixture launched a native command."
}

Invoke-RuntimeTest "Checkstyle rejects namespaced or unsupported diagnostics" -WithoutGit {
    param($fixture)
    $paths = @(& $fixture.Module {
        foreach ($definition in $script:ModuleDefinitions.Values) { $definition.Styles.Values }
    })
    foreach ($relative in $paths) {
        Write-FixtureText (Join-Path $fixture.RepositoryRoot $relative) '<checkstyle version="10.12"><file name="NamedSource.java" /></checkstyle>'
    }
    $clean = @(& $fixture.Module {
        param($Root, $Archive)
        Get-CheckstyleEvidence -RepositoryRoot $Root -ArchiveDirectory $Archive
    } $fixture.RepositoryRoot (Join-Path $fixture.Root "clean-style-archive"))
    Assert-TestCondition ($clean.Count -eq $paths.Count) "Unqualified clean Checkstyle reports were not accepted."
    foreach ($xml in @(
            '<j:checkstyle xmlns:j="urn:fixture" />',
            '<checkstyle xmlns="urn:fixture"><file name="Source.java"><error message="hidden" /></file></checkstyle>',
            '<checkstyle><j:file xmlns:j="urn:fixture" name="Source.java" /></checkstyle>',
            '<checkstyle><file name="Source.java"><j:error xmlns:j="urn:fixture" message="hidden" /></file></checkstyle>',
            '<checkstyle><file name="Source.java"><unexpected /></file></checkstyle>')) {
        Write-FixtureText (Join-Path $fixture.RepositoryRoot $paths[0]) $xml
        Assert-TestThrows {
            & $fixture.Module {
                param($Root, $Archive)
                Get-CheckstyleEvidence -RepositoryRoot $Root -ArchiveDirectory $Archive
            } $fixture.RepositoryRoot (Join-Path $fixture.Root ("invalid-style-archive-" + [guid]::NewGuid().ToString("N")))
        } "Checkstyle evidence is not clean" "Unsupported Checkstyle XML"
    }
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "XML-only fixture launched a native command."
}

foreach ($mode in @("FAILURE", "ERROR", "SKIPPED", "COUNTER_MISMATCH")) {
    Invoke-RuntimeTest ("successful fake Gradle cannot hide JUnit " + $mode) {
        param($fixture)
        Set-FixtureSettings $fixture @{ JUnitMode = $mode }
        Assert-TestThrows { Invoke-CanonicalFixture $fixture } "JUnit failures/errors|Mandatory tests were skipped|counters do not match" ("Invalid JUnit " + $mode)
        Assert-NoConsumableFixtureReceipt $fixture
    }
}

foreach ($outcome in @("SKIPPED", "NO-SOURCE")) {
    Invoke-RuntimeTest ("canonical producer rejects unvalidated Checkstyle outcome " + $outcome) {
        param($fixture)
        Set-FixtureSettings $fixture @{ StyleOutcome = $outcome }
        # Even syntactically clean XML cannot validate a skipped Checkstyle task.
        Assert-TestThrows { Invoke-CanonicalFixture $fixture } "Checkstyle task|Checkstyle.*valid|Checkstyle.*execut" ("Unvalidated Checkstyle " + $outcome)
        Assert-NoConsumableFixtureReceipt $fixture
    }
}

Invoke-RuntimeTest "FULL is unfiltered and consumes all reports without claiming approval" {
    param($fixture)
    Set-FixtureSettings $fixture @{
        JUnitTargetClass = "org.geogebra.fixture.OptionalUpstreamTest"; JUnitMode = "SKIPPED"
    }
    $build = Invoke-CanonicalFixture $fixture -Level FULL
    try {
        $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
        Assert-TestCondition ($receipt.selections.shared.unfiltered -and $receipt.selections.desktop.unfiltered) "FULL receipt is filtered."
        $calls = @(& $fixture.Module { @($script:FixtureNativeCalls) })
        Assert-TestCondition (@($calls | Where-Object { $_.arguments -ccontains "--tests" }).Count -eq 0) "FULL native plan contains a filter."
        Assert-TestCondition ($receipt.skippedUpstreamTests -eq 1) "FULL did not disclose the optional upstream skip."
        Confirm-FixtureEvidence $fixture $build -Arguments @(":shared:common-jre:test")
        Confirm-FixtureEvidence $fixture $build -Arguments @(":desktop:desktop:test")
    } finally { Close-FixtureEvidence $fixture $build }
}

Invoke-RuntimeTest "independent FULL accepts fresh shared aliases and Desktop, including repeated headings" -WithoutGit {
    param($fixture)
    foreach ($moduleName in @("shared", "desktop")) {
        foreach ($headingCount in @(1, 2)) {
            Set-FixtureSettings $fixture @{ TestTaskMultiplicity = $headingCount }
            $run = Invoke-IndependentFixture $fixture -Module $moduleName
            Assert-TestCondition ($run.Native.exitCode -eq 0) "Positive independent fixture has nonzero native status."
            Assert-FixtureIndependentOutcome $fixture $run
            $definition = & $fixture.Module { param($Name) $script:ModuleDefinitions[$Name] } $moduleName
            $archived = @(Get-ChildItem -LiteralPath $run.ArchiveDirectory -Filter "TEST-*.xml" -File)
            Assert-TestCondition ($archived.Count -gt 0) "Independent FULL did not archive actual XML."
            foreach ($report in $archived) {
                $live = Join-Path (Join-Path $fixture.RepositoryRoot $definition.ResultDirectory) $report.Name
                Assert-TestCondition ((Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash -ceq
                    (Get-FileHash -LiteralPath $report.FullName -Algorithm SHA256).Hash) "Independent FULL archive differs from fresh live XML."
            }
            if ($moduleName -ceq "shared") {
                Assert-TestCondition ($run.Native.text.Contains("> Task :common-jre:test")) "Shared fixture did not exercise its cwd-relative task alias."
            }
        }
    }
}

Invoke-RuntimeTest "independent FULL rejects CI JUnit failures and errors despite native zero" -WithoutGit {
    param($fixture)
    [Environment]::SetEnvironmentVariable("CI", "true", [EnvironmentVariableTarget]::Process)
    foreach ($moduleName in @("shared", "desktop")) {
        $class = if ($moduleName -ceq "shared") { "org.geocedg.fixture.SharedTest" } else { "org.geocedg.fixture.DesktopTest" }
        foreach ($mode in @("FAILURE", "ERROR", "COUNTER_MISMATCH")) {
            Set-FixtureSettings $fixture @{ JUnitTargetClass = $class; JUnitMode = $mode }
            $run = Invoke-IndependentFixture $fixture -Module $moduleName
            Assert-TestCondition ($run.Native.exitCode -eq 0) "CI outcome fixture must preserve successful native status."
            Assert-TestThrows { Assert-FixtureIndependentOutcome $fixture $run } "JUnit failures/errors|counters do not match" "Independent FULL CI ignoreFailures guard"
            if ($mode -cne "COUNTER_MISMATCH") {
                Assert-TestCondition (Test-Path -LiteralPath (Join-Path $run.ArchiveDirectory ("TEST-" + $class + ".xml"))) "Failure/error XML was not archived before rejection."
            }
        }
    }
}

Invoke-RuntimeTest "independent FULL allows optional upstream skip but rejects mandatory skips" -WithoutGit {
    param($fixture)
    foreach ($scenario in @(
            @{ Module = "shared"; Class = "org.geogebra.fixture.OptionalUpstreamTest"; Allowed = $true },
            @{ Module = "shared"; Class = "org.geocedg.fixture.SharedTest"; Allowed = $false },
            @{ Module = "shared"; Class = "org.geogebra.common.kernel.commands.RedefineTest"; Allowed = $false },
            @{ Module = "desktop"; Class = "org.geocedg.fixture.DesktopTest"; Allowed = $false })) {
        Set-FixtureSettings $fixture @{ JUnitTargetClass = $scenario.Class; JUnitMode = "SKIPPED" }
        $run = Invoke-IndependentFixture $fixture -Module $scenario.Module
        if ($scenario.Allowed) { Assert-FixtureIndependentOutcome $fixture $run } else {
            Assert-TestThrows { Assert-FixtureIndependentOutcome $fixture $run } "Mandatory tests were skipped" "Independent FULL mandatory skip"
        }
    }
}

Invoke-RuntimeTest "independent FULL rejects nonfresh, missing, conflicting, and wrong-context task evidence" -WithoutGit {
    param($fixture)
    foreach ($moduleName in @("shared", "desktop")) {
        foreach ($scenario in @(
                @{ TestOutcome = "UP-TO-DATE" }, @{ TestOutcome = "FROM-CACHE" },
                @{ TestOutcome = "SKIPPED" }, @{ TestOutcome = "NO-SOURCE" }, @{ TestOutcome = "FAILED" },
                @{ TestTaskMultiplicity = 0 }, @{ ExtraTestOutcome = "UP-TO-DATE" },
                @{ TestHeadingTaskOverride = ":wrong:module:test" })) {
            Set-FixtureSettings $fixture @{ TestOutcome = "EXECUTED"; TestTaskMultiplicity = 1; ExtraTestOutcome = ""; TestHeadingTaskOverride = "" }
            Set-FixtureSettings $fixture $scenario
            $run = Invoke-IndependentFixture $fixture -Module $moduleName
            Assert-TestThrows { Assert-FixtureIndependentOutcome $fixture $run } "unambiguous fresh execution evidence" "Independent FULL task evidence"
        }
    }
    Set-FixtureSettings $fixture @{ TestOutcome = "EXECUTED"; TestTaskMultiplicity = 1; ExtraTestOutcome = ""; TestHeadingTaskOverride = "" }
    $run = Invoke-IndependentFixture $fixture -Module shared
    Assert-TestThrows { Assert-FixtureIndependentOutcome $fixture $run -WorkingDirectory $fixture.RepositoryRoot } "unambiguous fresh execution evidence" "Shared alias attributed to repository-root context"
}

Invoke-RuntimeTest "independent FULL clears only direct stale JUnit XML and archives its replacement" -WithoutGit {
    param($fixture)
    foreach ($moduleName in @("shared", "desktop")) {
        $definition = & $fixture.Module { param($Name) $script:ModuleDefinitions[$Name] } $moduleName
        $class = if ($moduleName -ceq "shared") { "org.geocedg.fixture.SharedTest" } else { "org.geocedg.fixture.DesktopTest" }
        $directory = Join-Path $fixture.RepositoryRoot $definition.ResultDirectory
        $stale = Join-Path $directory ("TEST-" + $class + ".xml")
        Write-FixtureText $stale ('<testsuite name="' + $class + '" tests="1" failures="0" errors="0" skipped="0"><testcase classname="' + $class + '" name="staleCase" /></testsuite>')
        $staleHash = (Get-FileHash -LiteralPath $stale -Algorithm SHA256).Hash
        $retained = @((Join-Path $directory "metadata.xml"), (Join-Path $directory "nested/TEST-retained.xml"),
            (Join-Path $fixture.Root ("outside-report-sentinel-" + $moduleName + ".txt")))
        foreach ($path in $retained) { Write-FixtureText $path "retain exact fixture bytes" }
        Clear-FixtureIndependentReports $fixture -Module $moduleName
        Assert-TestCondition (-not (Test-Path -LiteralPath $stale)) "Independent clear retained stale direct JUnit XML."
        foreach ($path in $retained) {
            Assert-TestCondition ([IO.File]::ReadAllText($path) -ceq "retain exact fixture bytes") "Independent clear modified a nontarget file."
        }
        Set-FixtureSettings $fixture @{ JUnitCaseName = "replacementCase()" }
        $run = Invoke-IndependentFixture $fixture -Module $moduleName -AlreadyCleared
        Assert-FixtureIndependentOutcome $fixture $run
        $archive = Join-Path $run.ArchiveDirectory ("TEST-" + $class + ".xml")
        Assert-TestCondition ((Get-FileHash -LiteralPath $stale -Algorithm SHA256).Hash -cne $staleHash -and
            (Get-FileHash -LiteralPath $stale -Algorithm SHA256).Hash -ceq
            (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash) "Independent FULL used stale bytes instead of the new report."
    }
}

Invoke-RuntimeTest "independent FULL clear rejects active canonical state before any mutation" -WithoutGit {
    param($fixture)
    $sentinel = Join-Path $fixture.RepositoryRoot "source/shared/common-jre/build/test-results/test/TEST-sentinel.xml"
    Write-FixtureText $sentinel "active evidence sentinel"
    & $fixture.Module {
        $script:ActiveEvidence = [pscustomobject]@{ State = "RUNNING" }
        $script:FixtureIndependentClearCalls = 0
        function script:Remove-CurrentJUnitReports {
            param([string]$RepositoryRoot, [string]$Module)
            $script:FixtureIndependentClearCalls++
            throw "Active evidence guard was bypassed."
        }
    }
    try {
        foreach ($moduleName in @("shared", "desktop")) {
            Assert-TestThrows { Clear-FixtureIndependentReports $fixture -Module $moduleName } "while canonical evidence is active" "Independent clear during active canonical evidence"
        }
        Assert-TestCondition ((& $fixture.Module { $script:FixtureIndependentClearCalls }) -eq 0) "Active clear reached the removal helper."
        Assert-TestCondition ([IO.File]::ReadAllText($sentinel) -ceq "active evidence sentinel") "Active clear changed report bytes."
    } finally { & $fixture.Module { $script:ActiveEvidence = $null } }
}

Invoke-RuntimeTest "independent FULL cannot accept stale reports when fake native produces no replacement" -WithoutGit {
    param($fixture)
    Set-FixtureSettings $fixture @{ EmitJUnitReports = $false }
    foreach ($moduleName in @("shared", "desktop")) {
        $definition = & $fixture.Module { param($Name) $script:ModuleDefinitions[$Name] } $moduleName
        $stale = Join-Path (Join-Path $fixture.RepositoryRoot $definition.ResultDirectory) "TEST-stale.xml"
        Write-FixtureText $stale '<testsuite name="org.geocedg.fixture.StaleTest" tests="1" failures="0" errors="0" skipped="0"><testcase classname="org.geocedg.fixture.StaleTest" name="oldPass" /></testsuite>'
        $run = Invoke-IndependentFixture $fixture -Module $moduleName
        Assert-TestCondition ($run.Native.exitCode -eq 0 -and -not (Test-Path -LiteralPath $stale)) "Missing-replacement fixture retained old evidence or changed native status."
        Assert-TestThrows { Assert-FixtureIndependentOutcome $fixture $run } "No fresh JUnit reports" "Independent FULL without replacement XML"
    }
}

Invoke-RuntimeTest "canonical context accepts the reviewed three roots and shared-only cache property" -WithoutGit {
    param($fixture)
    $configuration = & $fixture.Module { param($Root) Get-ExternalGradleConfiguration -RepositoryRoot $Root } $fixture.RepositoryRoot
    foreach ($relative in @("gradle.properties", "source/shared/gradle.properties", "source/desktop/gradle.properties")) {
        $path = Join-Path $fixture.RepositoryRoot $relative
        $records = @($configuration.Files | Where-Object { $_.path -ceq $path })
        Assert-TestCondition ($records.Count -eq 1 -and $records[0].kind -ceq "file" -and
            $records[0].sha256 -ceq (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()) "A reviewed build root's property bytes are missing from external provenance."
    }
    foreach ($context in @(
            @{ Relative = ""; Task = ":shared:common-jre:test"; Expected = ":shared:common-jre:test" },
            @{ Relative = "source/shared"; Task = ":common-jre:test"; Expected = ":shared:common-jre:test" },
            @{ Relative = "source/desktop"; Task = ":desktop:test"; Expected = ":desktop:desktop:test" })) {
        $cwd = if ($context.Relative.Length -eq 0) { $fixture.RepositoryRoot } else { Join-Path $fixture.RepositoryRoot $context.Relative }
        $requirements = @(& $fixture.Module {
            param($Root, $Cwd, $Task)
            Get-RequestedBuildRequirements -RepositoryRoot $Root -WorkingDirectory $Cwd -Arguments @($Task)
        } $fixture.RepositoryRoot $cwd $context.Task)
        Assert-TestCondition ($requirements.Count -eq 1 -and $requirements[0].Task -ceq $context.Expected) "Reviewed build-root alias did not retain its explicit context."
    }
    $arguments = @(& $fixture.Module { Get-TestBuildArguments -Module shared -IncludeCheckstyle })
    Assert-TestCondition ($arguments -ccontains "--build-cache" -and $arguments -ccontains "--no-parallel" -and
        $arguments -ccontains "--no-daemon") "Canonical policy no longer explicitly overrides the three allowed build-policy properties."
    Assert-TestCondition ((& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0 -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Build-root inspection requested Git or native work."
}

Invoke-RuntimeTest "different build-local JVM arguments fail on the first canonical preflight" -WithoutGit {
    param($fixture)
    foreach ($relative in @("source/shared/gradle.properties", "source/desktop/gradle.properties")) {
        Set-FixtureBuildRootProperties -RepositoryRoot $fixture.RepositoryRoot
        $path = Join-Path $fixture.RepositoryRoot $relative
        Write-FixtureText $path (([IO.File]::ReadAllText($path)).Replace("-Xmx4g", "-Xmx2g"))
        Assert-FixtureCanonicalPreflightReject $fixture -Pattern "Different build-root org.gradle.jvmargs"
    }
    Assert-TestCondition ((& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0) "Divergent build-local JVM arguments reached Git."
}

Invoke-RuntimeTest "new semantic build-local properties fail even before a receipt exists" -WithoutGit {
    param($fixture)
    foreach ($scenario in @(
            @{ Relative = "source/shared/gradle.properties"; Text = "systemProp.fixture.semantic=changed" },
            @{ Relative = "source/desktop/gradle.properties"; Text = "fixtureFeature=changed" },
            @{ Relative = "gradle.properties"; Text = "org.gradle.java.home=elsewhere" },
            @{ Relative = "all"; Text = "systemProp.fixture.semantic=identical-but-unreviewed" })) {
        Set-FixtureBuildRootProperties -RepositoryRoot $fixture.RepositoryRoot
        $relatives = if ($scenario.Relative -ceq "all") {
            @("gradle.properties", "source/shared/gradle.properties", "source/desktop/gradle.properties")
        } else { @($scenario.Relative) }
        foreach ($relative in $relatives) {
            $path = Join-Path $fixture.RepositoryRoot $relative
            Write-FixtureText $path ([IO.File]::ReadAllText($path) + $scenario.Text + $Lf)
        }
        Assert-FixtureCanonicalPreflightReject $fixture -Pattern "Unsupported build-local Gradle property"
    }
    Assert-TestCondition ((& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0) "Unreviewed build-local properties reached Git."
}

Invoke-RuntimeTest "external JVM inputs in either included root fail on the first canonical preflight" -WithoutGit {
    param($fixture)
    foreach ($relative in @("source/shared/gradle.properties", "source/desktop/gradle.properties")) {
        foreach ($jvmArguments in @("-javaagent:fixture.jar", "@fixture.args")) {
            Set-FixtureBuildRootProperties -RepositoryRoot $fixture.RepositoryRoot
            Write-FixtureText (Join-Path $fixture.RepositoryRoot $relative) ("org.gradle.jvmargs=" + $jvmArguments + $Lf)
            Assert-FixtureCanonicalPreflightReject $fixture -Pattern "externally dependent org.gradle.jvmargs"
        }
    }
    Assert-TestCondition ((& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0) "Included-root external JVM inputs reached Git."
}

Invoke-RuntimeTest "external properties accept normal heap, default layout, and last effective wrapper URL" -WithoutGit {
    param($fixture)
    $wrapper = Join-Path $fixture.RepositoryRoot "gradle/wrapper/gradle-wrapper.properties"
    $effectiveDistribution = Join-Path $fixture.GradleUserHome "wrapper/dists/gradle-9.4.1-bin/fixture-hash/gradle-9.4.1"
    $propertyPaths = @((Join-Path $fixture.RepositoryRoot "gradle.properties"),
        (Join-Path $fixture.RepositoryRoot "source/shared/gradle.properties"),
        (Join-Path $fixture.RepositoryRoot "source/desktop/gradle.properties"),
        (Join-Path $fixture.GradleUserHome "gradle.properties"), (Join-Path $effectiveDistribution "gradle.properties"))
    foreach ($path in $propertyPaths) {
        Write-FixtureText $path ('# normal fixture properties' + $Lf + 'org.gradle.jvmargs=-javaagent:overridden.jar' +
            $Lf + 'org.gradle.jvmargs : -Xmx2g -Dfile.encoding=UTF-8' + $Lf)
    }
    $defaultUrl = 'distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip'
    foreach ($wrapperText in @(
            $defaultUrl,
            ($defaultUrl + $Lf + 'distributionBase=GRADLE_USER_HOME' + $Lf + 'distributionPath=wrapper/dists'),
            ('distributionUrl=https\://services.gradle.org/distributions/gradle-9.3-bin.zip' + $Lf + $defaultUrl))) {
        Write-FixtureText $wrapper ($wrapperText + $Lf)
        $configuration = & $fixture.Module { param($Root) Get-ExternalGradleConfiguration -RepositoryRoot $Root } $fixture.RepositoryRoot
        foreach ($path in $propertyPaths) {
            $records = @($configuration.Files | Where-Object { $_.path -ceq $path })
            Assert-TestCondition ($records.Count -eq 1 -and $records[0].kind -ceq "file" -and
                $records[0].sha256 -ceq (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()) "Effective Gradle property bytes were not inventoried."
        }
        Assert-TestCondition (@($configuration.Files | Where-Object { $_.path.Contains("gradle-9.3-bin") }).Count -eq 0) "Wrapper inventory used a shadowed distributionUrl."
    }
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Property inspection unexpectedly launched native work."
}

Invoke-RuntimeTest "external property syntax and JVM inputs fail before any native probe" -WithoutGit {
    param($fixture)
    $propertyPaths = @((Join-Path $fixture.RepositoryRoot "gradle.properties"),
        (Join-Path $fixture.GradleUserHome "gradle.properties"),
        (Join-Path $fixture.GradleUserHome "wrapper/dists/gradle-9.4.1-bin/fixture-hash/gradle-9.4.1/gradle.properties"))
    foreach ($scenario in @(
            @{ Index = 0; Text = 'org.gradle.jvm\u0061rgs=-Xmx1g'; Pattern = "escaped Java-properties key" },
            @{ Index = 1; Text = ('org.gradle.jvmargs=-Xmx1g \' + $Lf + '-javaagent:fixture.jar'); Pattern = "line continuation" },
            @{ Index = 2; Text = 'org.gradle.jvmargs=-javaagent:fixture.jar'; Pattern = "externally dependent org.gradle.jvmargs" },
            @{ Index = 0; Text = 'org.gradle.jvmargs=@fixture.args'; Pattern = "externally dependent org.gradle.jvmargs" },
            @{ Index = 1; Text = 'org.gradle.jvmargs=-XX:Flags=fixture.flags'; Pattern = "externally dependent org.gradle.jvmargs" },
            @{ Index = 2; Text = 'org.gradle.jvmargs=-Dfixture.path=C:\escaped'; Pattern = "Escaped or externally dependent" },
            @{ Index = 0; Text = 'org.gradle.jvmargs=-Duser.home=elsewhere'; Pattern = "externally dependent org.gradle.jvmargs" })) {
        Set-FixtureBuildRootProperties -RepositoryRoot $fixture.RepositoryRoot -JvmArguments "-Xmx1g"
        foreach ($path in $propertyPaths) { Write-FixtureText $path ('org.gradle.jvmargs=-Xmx1g' + $Lf) }
        Write-FixtureText $propertyPaths[$scenario.Index] ($scenario.Text + $Lf)
        Assert-FixtureCanonicalPreflightReject $fixture -Pattern $scenario.Pattern
    }
}

Invoke-RuntimeTest "custom or uninspectable wrapper layouts fail before any native probe" -WithoutGit {
    param($fixture)
    $wrapper = Join-Path $fixture.RepositoryRoot "gradle/wrapper/gradle-wrapper.properties"
    $defaultUrl = 'distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip'
    foreach ($scenario in @(
            @{ Extra = 'distributionBase=PROJECT'; Pattern = "Unsupported wrapper distributionBase" },
            @{ Extra = 'distributionPath=elsewhere'; Pattern = "Unsupported wrapper distributionPath" },
            @{ Extra = 'distributionPath=wrapper/dists/'; Pattern = "Unsupported wrapper distributionPath" },
            @{ Extra = 'distributionPath=wrapper/dists '; Pattern = "Unsupported wrapper distributionPath" },
            @{ Extra = 'distributionUrl=not-a-gradle-distribution'; Pattern = "Cannot determine the pinned wrapper distribution" },
            @{ Extra = 'distribution\u0050ath=wrapper/dists'; Pattern = "escaped Java-properties key" })) {
        Write-FixtureText $wrapper ($defaultUrl + $Lf + $scenario.Extra + $Lf)
        Assert-FixtureCanonicalPreflightReject $fixture -Pattern $scenario.Pattern
    }
}

Invoke-RuntimeTest "external JVM environment agents and argument files fail before any native probe" -WithoutGit {
    param($fixture)
    $jvmVariables = @("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "GRADLE_OPTS")
    foreach ($scenario in @(
            @{ Name = "JAVA_TOOL_OPTIONS"; Value = "-javaagent:fixture.jar"; Pattern = "external JVM input" },
            @{ Name = "JDK_JAVA_OPTIONS"; Value = "@fixture.args"; Pattern = "external JVM input" },
            @{ Name = "_JAVA_OPTIONS"; Value = "-agentpath:fixture.dll"; Pattern = "external JVM input" },
            @{ Name = "JAVA_OPTS"; Value = "-agentlib:fixture"; Pattern = "external JVM input" },
            @{ Name = "GRADLE_OPTS"; Value = "-XX:Flags=fixture.flags"; Pattern = "external JVM input" },
            @{ Name = "GRADLE_OPTS"; Value = "-Dgradle.user.home=elsewhere"; Pattern = "overrides a home directory" })) {
        foreach ($name in $jvmVariables) { [Environment]::SetEnvironmentVariable($name, $null, [EnvironmentVariableTarget]::Process) }
        [Environment]::SetEnvironmentVariable($scenario.Name, $scenario.Value, [EnvironmentVariableTarget]::Process)
        Assert-FixtureCanonicalPreflightReject $fixture -Pattern $scenario.Pattern
    }
}

foreach ($artifactName in @("shared-gradle.log", "input-inventory.json", "external-configuration.json")) {
    Invoke-RuntimeTest ("completion rejects altered audit artifact " + $artifactName) {
        param($fixture)
        $build = Invoke-CanonicalFixture $fixture
        $receipt = Get-Content -LiteralPath $build.EvidencePath -Raw | ConvertFrom-Json
        $artifacts = @($receipt.auditArtifacts | Where-Object { [IO.Path]::GetFileName($_.path) -ceq $artifactName })
        Assert-TestCondition ($artifacts.Count -eq 1) "Expected exactly one hashed fixture audit artifact."
        $path = $artifacts[0].path
        Write-FixtureText $path ((Get-Content -LiteralPath $path -Raw) + "fake-first tamper")
        Close-FixtureEvidence $fixture $build -ExpectInvalidEvidence
    }
}

foreach ($devCase in @(
        @{ Label = "simple class"; Filters = @("SharedTest"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "simple method"; Filters = @("SharedTest.caseA"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "fully qualified class"; Filters = @("org.geocedg.fixture.SharedTest"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "fully qualified method"; Filters = @("org.geocedg.fixture.SharedTest.caseA"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "simple star pattern"; Filters = @("Shared*.case*"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "leading-star fully qualified pattern"; Filters = @("*.SharedTest.caseA"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "cross-package star pattern"; Filters = @("org.*.SharedTest.caseA"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "multiple independently matched filters"; Filters = @("SharedTest", "org.geocedg.fixture.SharedTest.caseA", "*.SharedTest.case*"); XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "literal question mark and brackets"; Filters = @("SharedTest.case?[2]"); XmlName = "case?[2]"; Method = "case?[2]" },
        @{ Label = "literal parameter index with star"; Filters = @("*SharedTest.*[2]"); XmlName = "caseA[2]"; Method = "caseA[2]" },
        @{ Label = "reported name containing spaces"; Filters = @("SharedTest.some method containing spaces"); XmlName = "some method containing spaces"; Method = "some method containing spaces" },
        @{ Label = "literal regex metacharacters"; Filters = @("SharedTest.value+[2]"); XmlName = "value+[2]"; Method = "value+[2]" },
        @{ Label = "exact nonempty parenthetical display text"; Filters = @("SharedTest.some case (note)"); XmlName = "some case (note)"; Method = "some case (note)" })) {
    Invoke-RuntimeTest ("DEV accepts inspectable " + $devCase.Label + " from saved XML") -WithoutGit {
        param($fixture)
        Set-FixtureSettings $fixture @{ JUnitCaseName = $devCase.XmlName; SelectionMethodName = $devCase.Method }
        $run = Invoke-DevFixture $fixture -Filters $devCase.Filters
        $summary = Get-Content -LiteralPath $run.SummaryPath -Raw | ConvertFrom-Json
        Assert-TestCondition ($summary.state -ceq "PASS_SCOPED_NOT_ACCEPTANCE" -and $summary.nativeExitCode -eq 0 -and
            -not $summary.authorApproved -and -not $summary.selfApproved) "DEV shorthand changed the scoped-only result contract."
        Assert-TestSequence $devCase.Filters @($summary.filters) "DEV rewrote the requested filter tokens"
        $report = @($summary.junit | Where-Object { $_.class -ceq "org.geocedg.fixture.SharedTest" })
        Assert-TestCondition ($report.Count -eq 1 -and $report[0].cases.Count -eq 1 -and
            $report[0].cases[0].name -ceq $devCase.XmlName) "DEV evidence did not retain the exact reported XML display name."
        $live = Join-Path $fixture.RepositoryRoot $report[0].livePath
        Assert-TestCondition ((Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash -ceq
            (Get-FileHash -LiteralPath $report[0].archivePath -Algorithm SHA256).Hash) "DEV did not preserve raw XML bytes."
        $calls = @(& $fixture.Module { @($script:FixtureNativeCalls) })
        Assert-TestCondition ($calls.Count -eq 1 -and $calls[0].arguments -ccontains ":shared:common-jre:test" -and
            $calls[0].arguments -cnotcontains ":desktop:desktop:test") "DEV shorthand launched extra or cross-module native work."
    }
}

foreach ($devCase in @(
        @{ Label = "missing simple class"; Filter = "MissingTest"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "missing simple method"; Filter = "SharedTest.missingMethod"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "wrong module class"; Filter = "DesktopTest.caseA"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "lowercase simple class"; Filter = "sharedTest"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "wrong-case fully qualified class"; Filter = "org.geocedg.fixture.sharedTest"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "question mark is not a wildcard"; Filter = "SharedTest.case?"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "brackets are not a character class"; Filter = "SharedTest.case[A]"; XmlName = "caseA()"; Method = "caseA" },
        @{ Label = "literal brackets require observed brackets"; Filter = "SharedTest.case[2]"; XmlName = "case2"; Method = "case2" },
        @{ Label = "nonempty parenthetical text cannot be erased"; Filter = "SharedTest.caseA"; XmlName = "caseA(note)"; Method = "caseA(note)" },
        @{ Label = "custom display name cannot identify an unseen method"; Filter = "SharedTest.caseA"; XmlName = "renamed behavior"; Method = "caseA" })) {
    Invoke-RuntimeTest ("DEV rejects unmatched filter despite a matching class: " + $devCase.Label) -WithoutGit {
        param($fixture)
        Set-FixtureSettings $fixture @{ JUnitCaseName = $devCase.XmlName; SelectionMethodName = $devCase.Method }
        # The valid class filter causes actual fixture XML to be emitted. The
        # independently unmatched second filter must still invalidate DEV.
        Assert-TestThrows {
            Invoke-DevFixture $fixture -Filters @("org.geocedg.fixture.SharedTest", $devCase.Filter)
        } "requested DEV filter has no inspectable" ("Unmatched DEV XML evidence: " + $devCase.Label)
        Assert-TestCondition (@(Get-ChildItem -LiteralPath $fixture.Logs -Filter "dev-summary.json" -Recurse -File).Count -eq 0) "Partly unmatched DEV filters emitted a PASS summary."
        Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 1) "Unmatched DEV evidence triggered a second native attempt."
    }
}

Invoke-RuntimeTest "DEV rejects whitespace filter before removal and native work" -WithoutGit {
    param($fixture)
    & $fixture.Module {
        $script:FixtureUnexpectedDevRemoval = 0
        function script:Remove-CurrentJUnitReports {
            param([string]$RepositoryRoot, [string]$Module)
            $script:FixtureUnexpectedDevRemoval++
            throw "Whitespace DEV filter must fail before report removal."
        }
    }
    foreach ($filters in @(@(" "), @("SharedTest", " "), @([string][char]9))) {
        Assert-TestThrows { Invoke-DevFixture $fixture -Filters $filters } "DEV requires explicit, nonempty test filters" "Whitespace DEV preflight"
    }
    Assert-TestCondition ((& $fixture.Module { $script:FixtureUnexpectedDevRemoval }) -eq 0) "Whitespace DEV filter reached report removal."
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Whitespace DEV filter requested native work."
}

Invoke-RuntimeTest "DEV rejects a mixed matching and missing filter without claiming scoped PASS" -WithoutGit {
    param($fixture)
    Assert-TestThrows {
        & $fixture.Module {
            param($Root, $Logs)
            Invoke-GeoCeDGDevVerification -RepositoryRoot $Root -Module shared -TestFilter @(
                "org.geocedg.fixture.SharedTest", "org.geocedg.fixture.MissingTest") -LogDirectory $Logs
        } $fixture.RepositoryRoot $fixture.Logs
    } "requested DEV filter has no inspectable" "Mixed matching/missing DEV filters"
    Assert-TestCondition (@(Get-ChildItem -LiteralPath $fixture.Logs -Filter "dev-summary.json" -Recurse -File).Count -eq 0) "Failed DEV selection emitted a PASS summary."
}

Invoke-RuntimeTest "default operational benchmark preserves full scope and informational budget" -WithoutGit {
    param($fixture)
    $rootPath = (Resolve-Path -LiteralPath $RootVerifierPath).Path
    $repositoryRoot = [IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $rootPath) "../.."))
    $suitePath = Join-Path $repositoryRoot "benchmarks/suites/operational-smoke.yml"
    $suite = ConvertFrom-Json -InputObject ([IO.File]::ReadAllText($suitePath)) -Depth 100 -NoEnumerate
    Assert-TestCondition ($suite.schema_version -eq 1 -and
        $suite.id -ceq "cedg.operational.smoke" -and
        $suite.budget_mode -ceq "informational") "Default benchmark identity/budget mode changed."
    $cases = @($suite.cases)
    Assert-TestCondition ($cases.Count -eq 1) "Default benchmark must retain exactly one complete operational case."
    $case = $cases[0]
    Assert-TestCondition ($case.id -ceq "verify-operational" -and
        $case.script -ceq "tools/agent/verify-operational.ps1") "Default benchmark no longer runs the complete operational verifier."
    Assert-TestSequence @("-Quiet") @($case.arguments) "Default benchmark arguments changed"
    Assert-TestCondition ($case.warmup_iterations -eq 1 -and
        $case.measurement_iterations -eq 3) "Default benchmark repeat counts changed."
    Assert-TestCondition ($case.timeout_seconds -eq 600) "Default benchmark must retain the reviewed finite 600-second child timeout."
    Assert-TestCondition ($case.budget.metric -ceq "median_elapsed_ms" -and
        $case.budget.warning_threshold_ms -eq 5000) "Default benchmark informational warning was weakened."
    Assert-TestCondition (@(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Default benchmark contract fixture requested native work."
}

Invoke-RuntimeTest "operational defaults isolate five invocations and preserve explicit evidence" -WithoutGit {
    param($fixture)
    $operationalPath = (Resolve-Path -LiteralPath $OperationalVerifierPath).Path
    $generatedTestsPath = (Resolve-Path -LiteralPath $GeneratedStateTestsPath).Path
    $operationalHash = (Get-FileHash -LiteralPath $operationalPath -Algorithm SHA256).Hash
    $generatedTestsHash = (Get-FileHash -LiteralPath $generatedTestsPath -Algorithm SHA256).Hash
    $tokens = $null
    $errors = $null
    $operationalAst = [Management.Automation.Language.Parser]::ParseInput(
        [IO.File]::ReadAllText($operationalPath), [ref]$tokens, [ref]$errors)
    Assert-TestCondition ($errors.Count -eq 0 -and $null -ne $operationalAst.ParamBlock) "Operational parameter source does not parse."
    $parameterBlock = $operationalAst.ParamBlock
    $logParameters = @($parameterBlock.Parameters | Where-Object { $_.Name.VariablePath.UserPath -ceq "LogDirectory" })
    Assert-TestCondition ($logParameters.Count -eq 1 -and $null -ne $logParameters[0].DefaultValue) "Cannot isolate the operational LogDirectory default."
    foreach ($command in @($parameterBlock.FindAll({ param($node)
                $node -is [Management.Automation.Language.CommandAst]
            }, $true))) {
        Assert-TestCondition ($command.GetCommandName() -ceq "Join-Path") "Parameter-only fixture would execute an unexpected command."
    }
    # Bind the actual ParamBlock only. No operational body, default directory I/O,
    # Git, Gradle, infrastructure process, or benchmark process is executed.
    $capture = $Lf + '[pscustomobject]@{ LogDirectory = $LogDirectory; IsExplicit = $PSBoundParameters.ContainsKey("LogDirectory"); Quiet = [bool]$Quiet }'
    $bind = [scriptblock]::Create($parameterBlock.Extent.Text + $capture)
    $defaultParent = [IO.Path]::GetFullPath((Join-Path ([IO.Path]::GetTempPath()) "geocedg-operational"))
    $assertDefaultBindings = {
        param([object[]]$Bindings)
        Assert-TestCondition ($Bindings.Count -eq 5) "Expected precheck, warmup and three measured default bindings."
        $distinct = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
        foreach ($binding in $Bindings) { [void]$distinct.Add($binding.LogDirectory) }
        Assert-TestCondition ($distinct.Count -eq 5) "Default operational evidence paths are not distinct."
        foreach ($binding in $Bindings) {
            $path = [IO.Path]::GetFullPath($binding.LogDirectory)
            Assert-TestCondition ($binding.Quiet -and -not $binding.IsExplicit -and
                [IO.Path]::GetDirectoryName($path).Equals($defaultParent, [StringComparison]::OrdinalIgnoreCase) -and
                [IO.Path]::GetFileName($path) -cmatch '^[0-9a-f]{32}$') "Default binding is not a fresh implicit GUID child."
        }
    }
    $defaults = @(1..5 | ForEach-Object { & $bind -Quiet })
    & $assertDefaultBindings -Bindings $defaults
    # Replace only this default expression in memory to prove the old shared path
    # fails the same uniqueness assertion, without changing historical source.
    $oldDefault = '(Join-Path ([IO.Path]::GetTempPath()) "geocedg-operational")'
    $defaultExtent = $logParameters[0].DefaultValue.Extent
    $relativeOffset = $defaultExtent.StartOffset - $parameterBlock.Extent.StartOffset
    $legacyParameters = $parameterBlock.Extent.Text.Remove($relativeOffset,
        $defaultExtent.EndOffset - $defaultExtent.StartOffset).Insert($relativeOffset, $oldDefault)
    $legacyBind = [scriptblock]::Create($legacyParameters + $capture)
    $legacyDefaults = @(1..5 | ForEach-Object { & $legacyBind -Quiet })
    Assert-TestThrows { & $assertDefaultBindings -Bindings $legacyDefaults } "Default operational evidence paths are not distinct" "Prior fixed-default regression"

    # Exercise the generated-state fixture's exact publication refusal, not a
    # synthetic CreateNew surrogate or the fixture/helper execution body.
    $generatedAst = [Management.Automation.Language.Parser]::ParseInput(
        [IO.File]::ReadAllText($generatedTestsPath), [ref]$tokens, [ref]$errors)
    Assert-TestCondition ($errors.Count -eq 0) "Generated-state fixture source does not parse."
    $publicationGuards = @($generatedAst.FindAll({ param($node)
        $node -is [Management.Automation.Language.IfStatementAst] -and
        $node.Extent.Text.Contains('throw "Refusing to overwrite existing fixture summary: $summaryPath"')
    }, $true))
    Assert-TestCondition ($publicationGuards.Count -eq 1) "Cannot isolate the exact generated-state publication refusal."
    $guardCommands = @($publicationGuards[0].FindAll({ param($node)
        $node -is [Management.Automation.Language.CommandAst]
    }, $true))
    $guardMethods = @($publicationGuards[0].FindAll({ param($node)
        $node -is [Management.Automation.Language.InvokeMemberExpressionAst]
    }, $true))
    Assert-TestCondition ($guardCommands.Count -eq 1 -and
        $guardCommands[0].GetCommandName() -ceq "Test-Path" -and $guardMethods.Count -eq 0) "Publication guard fixture would execute unexpected work."
    $publicationGuard = [scriptblock]::Create($publicationGuards[0].Extent.Text)
    $ownedPrefix = [IO.Path]::GetFullPath($fixture.Root).TrimEnd('/', '\') + [IO.Path]::DirectorySeparatorChar
    $ownedDirectories = [Collections.Generic.List[string]]::new()
    foreach ($binding in $defaults) {
        $ownedDirectories.Add((Join-Path (Join-Path $fixture.Root "default-evidence") ([IO.Path]::GetFileName($binding.LogDirectory))))
    }
    foreach ($explicitDirectory in @((Join-Path $fixture.Root "explicit-evidence"),
            (Join-Path $fixture.Root "explicit evidence with spaces"))) {
        foreach ($attempt in 1..2) {
            $binding = & $bind -Quiet -LogDirectory $explicitDirectory
            Assert-TestCondition ($binding.Quiet -and $binding.IsExplicit -and
                $binding.LogDirectory -ceq $explicitDirectory) "Explicit operational LogDirectory was not preserved exactly."
        }
        $ownedDirectories.Add($explicitDirectory)
    }
    foreach ($directory in $ownedDirectories) {
        $summaryPath = [IO.Path]::GetFullPath((Join-Path $directory "generated-state-tests.json"))
        Assert-TestCondition ($summaryPath.StartsWith($ownedPrefix, [StringComparison]::OrdinalIgnoreCase)) "Sentinel evidence escaped the owned fixture root."
        & $publicationGuard
        Write-FixtureText $summaryPath "Owned sentinel: prior evidence must remain unchanged."
        $sentinelHash = (Get-FileHash -LiteralPath $summaryPath -Algorithm SHA256).Hash
        Assert-TestThrows { & $publicationGuard } "Refusing to overwrite existing fixture summary" "Exact generated-state publication refusal"
        Assert-TestCondition ((Get-FileHash -LiteralPath $summaryPath -Algorithm SHA256).Hash -ceq $sentinelHash) "Publication refusal changed preserved sentinel bytes."
    }
    Assert-TestCondition ((Get-FileHash -LiteralPath $operationalPath -Algorithm SHA256).Hash -ceq $operationalHash -and
        (Get-FileHash -LiteralPath $generatedTestsPath -Algorithm SHA256).Hash -ceq $generatedTestsHash) "Parameter/publication fixture changed source authority."
    Assert-TestCondition (-not $fixture.GitInitialized -and
        (& $fixture.Module { $script:FixtureNoGitRequests }) -eq 0 -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "Parameter/publication fixture requested Git or native work."
}

Invoke-RuntimeTest "operational CI guard binds literal FULL to one canonical command AST" -WithoutGit {
    param($fixture)
    $operationalPath = (Resolve-Path -LiteralPath $OperationalVerifierPath).Path
    $operationalSource = [IO.File]::ReadAllText($operationalPath)
    $tokens = $null
    $errors = $null
    $operationalAst = [Management.Automation.Language.Parser]::ParseInput($operationalSource, [ref]$tokens, [ref]$errors)
    Assert-TestCondition ($errors.Count -eq 0) "Operational verifier source does not parse."
    $starts = @($operationalAst.FindAll({ param($node)
        $node -is [Management.Automation.Language.AssignmentStatementAst] -and
        $node.Left.Extent.Text -ceq '$ciTokens'
    }, $true))
    $ends = @($operationalAst.FindAll({ param($node)
        $node -is [Management.Automation.Language.PipelineAst] -and
        $node.Extent.Text.Contains('CI authority must use literal FULL coverage.')
    }, $true))
    Assert-TestCondition ($starts.Count -eq 1 -and $ends.Count -eq 1 -and
        [object]::ReferenceEquals($starts[0].Parent, $ends[0].Parent) -and
        $starts[0].Extent.StartOffset -lt $ends[0].Extent.EndOffset) "Cannot isolate the exact current CI AST guard."
    # Copy the actual source span; do not reimplement its binding predicates or
    # invoke verify-operational.ps1/root/Gradle. Only Assert-Condition is a fixture.
    $guardSource = $operationalSource.Substring($starts[0].Extent.StartOffset,
        $ends[0].Extent.EndOffset - $starts[0].Extent.StartOffset)
    $guardAst = [Management.Automation.Language.Parser]::ParseInput($guardSource, [ref]$tokens, [ref]$errors)
    Assert-TestCondition ($errors.Count -eq 0) "Isolated CI guard does not parse."
    $guardCommands = @($guardAst.FindAll({ param($node)
        $node -is [Management.Automation.Language.CommandAst]
    }, $true))
    foreach ($command in $guardCommands) {
        Assert-TestCondition ($command.GetCommandName() -cin @("Assert-Condition", "Where-Object")) "CI guard fixture would execute an unexpected command."
    }
    $guard = [scriptblock]::Create($guardSource)
    Write-FixtureText (Join-Path $fixture.Root "exact-operational-ci-guard.ps1") $guardSource
    function Invoke-FixtureCiAuthorityGuard {
        param([scriptblock]$Guard, [string]$Command)
        $ciCommands = [Collections.Generic.List[string]]::new()
        $ciCommands.Add($Command)
        function Assert-Condition {
            param([bool]$Condition, [string]$Message)
            if (-not $Condition) { throw $Message }
        }
        & $Guard
    }
    foreach ($commandText in @(
            '.\tools\agent\verify.ps1 -Level FULL',
            ('.\tools\agent\verify.ps1 `' + $Lf + '    -Level FULL -KeepBuildOutputs'),
            ('.\tools\agent\verify.ps1 `' + $Lf + '    -Level FULL `' + $Lf +
                '    -LogDirectory $logRoot `' + $Lf + '    -RunBenchmarks `' + $Lf +
                '    -BenchmarkOutputPath $benchmarkPath'))) {
        Invoke-FixtureCiAuthorityGuard $guard $commandText
    }
    foreach ($commandText in @(
            '.\tools\agent\verify.ps1',
            '.\tools\agent\verify.ps1 -FullTests',
            '.\tools\agent\verify.ps1 -Level COMPOSED',
            '.\tools\agent\verify.ps1 -Level full',
            '.\tools\agent\verify.ps1 -Level FULLER',
            '.\tools\agent\verify.ps1 -Level $desiredLevel',
            '.\tools\agent\verify.ps1 -Level $("FULL")',
            '.\tools\agent\verify-baseline.ps1 -Level FULL',
            '.\tools\agent\verify.ps1 -Level COMPOSED; Write-Output FULL',
            '.\tools\agent\verify.ps1 -Level FULL; Write-Output FULL',
            '.\tools\agent\verify.ps1 -Level FULL | Out-Null',
            '.\tools\agent\verify.ps1 -Level (Write-Output FULL)',
            '.\tools\agent\verify.ps1 -Level FULL -Level COMPOSED',
            '.\tools\agent\verify.ps1 -Level -KeepBuildOutputs FULL',
            '.\tools\agent\verify.ps1 # -Level FULL')) {
        Assert-TestThrows { Invoke-FixtureCiAuthorityGuard $guard $commandText } "CI authority|CI coverage|CI must" ("Detached/nonliteral FULL token: " + $commandText)
    }
    Assert-TestCondition (-not $fixture.GitInitialized -and
        @(& $fixture.Module { @($script:FixtureNativeCalls) }).Count -eq 0) "CI AST binding fixture requested Git or native work."
}

Invoke-RuntimeTest "root rejects invalid CleanBuild and execution-level combinations before work" -WithoutGit {
    param($fixture)
    $rootSource = (Resolve-Path -LiteralPath $RootVerifierPath).Path
    $agentDirectory = Join-Path $fixture.RepositoryRoot "tools/agent"
    [void][IO.Directory]::CreateDirectory($agentDirectory)
    $rootCopy = Join-Path $agentDirectory "verify.ps1"
    [IO.File]::Copy($rootSource, $rootCopy, $false)
    [IO.File]::Copy($ModulePath, (Join-Path $agentDirectory "verification-runtime.psm1"), $false)
    $guardPath = Join-Path $fixture.Root "unexpected-root-execution.txt"
    $quotedGuard = $guardPath.Replace("'", "''")
    Write-FixtureText (Join-Path $agentDirectory "repository-state.ps1") (
        "function Get-GeoCeDGRepositoryState { [IO.File]::WriteAllText('" + $quotedGuard +
        "', 'unexpected execution'); throw 'FIXTURE_GUARD_INVALID_MODE_REACHED_EXECUTION' }" + $Lf +
        "function global:git { [IO.File]::WriteAllText('" + $quotedGuard +
        "', 'unexpected Git request'); throw 'FIXTURE_GUARD_INVALID_MODE_REACHED_GIT' }" + $Lf)
    Write-FixtureText (Join-Path $agentDirectory "repository-generated-state.ps1") (
        "function New-RepositoryGeneratedStateSnapshot { throw 'FIXTURE_GUARD_SNAPSHOT_REACHED' }" + $Lf)
    $cases = @(
        @{ Arguments = @("-CleanBuild"); Pattern = "CleanBuild requires canonical FULL" },
        @{ Arguments = @("-Level", "FULL", "-CleanBuild", "-IndependentBuilds"); Pattern = "CleanBuild requires canonical FULL" },
        @{ Arguments = @("-Level", "FULL", "-SkipBuild"); Pattern = "SkipBuild is static-only" },
        @{ Arguments = @("-SkipBuild", "-IndependentBuilds"); Pattern = "SkipBuild is static-only" },
        @{ Arguments = @("-SkipBuild", "-LaunchDesktop"); Pattern = "SkipBuild is static-only" },
        @{ Arguments = @("-Level", "PHASE"); Pattern = "PHASE requires" },
        @{ Arguments = @("-Level", "PHASE", "-Phase", "G9U0-R6", "-CleanBuild"); Pattern = "CleanBuild requires canonical FULL" },
        @{ Arguments = @("-Level", "PHASE", "-Phase", "G9U0-R6", "-IndependentBuilds"); Pattern = "IndependentBuilds is a COMPOSED/FULL" },
        @{ Arguments = @("-Level", "PHASE", "-Phase", "G9U0-R6", "-LaunchDesktop"); Pattern = "options require COMPOSED or FULL" },
        @{ Arguments = @("-Module", "shared"); Pattern = "Module and TestFilter are DEV-only" },
        @{ Arguments = @("-TestFilter", "org.geocedg.fixture.SharedTest"); Pattern = "Module and TestFilter are DEV-only" },
        @{ Arguments = @("-Phase", "G9U0-R6"); Pattern = "Phase is valid only" },
        @{ Arguments = @("-Level", "DEV"); Pattern = "DEV requires explicit" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared"); FilterLiteral = "@('')"; Pattern = "DEV requires explicit" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared"); FilterLiteral = "@('   ')"; Pattern = "DEV requires explicit" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared"); FilterLiteral = "@([string][char]9)"; Pattern = "DEV requires explicit" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared"); FilterLiteral = "@('SharedTest', ' ')"; Pattern = "DEV requires explicit" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared", "-TestFilter", "org.geocedg.fixture.SharedTest", "-SkipBuild"); Pattern = "SkipBuild is static-only" },
        @{ Arguments = @("-Level", "DEV", "-Module", "shared", "-TestFilter", "org.geocedg.fixture.SharedTest", "-CleanBuild"); Pattern = "CleanBuild requires canonical FULL" },
        @{ Arguments = @("-Level", "DEV", "-FullTests"); Pattern = "FullTests selects FULL" }
    )
    $pwsh = Join-Path $PSHOME $(if ($IsWindows) { "pwsh.exe" } else { "pwsh" })
    $PSNativeCommandUseErrorActionPreference = $false
    $index = 0
    foreach ($case in $cases) {
        $index++
        $arguments = @("-NoProfile", "-File", $rootCopy, "-LogDirectory", (Join-Path $fixture.Root ("root-mode-" + $index))) + $case.Arguments
        if ($case.ContainsKey("FilterLiteral")) {
            # A fixture script preserves empty string/array elements even on
            # PowerShell 7.2's legacy native-argument marshalling. The copied
            # root verifier is unchanged and its early guard still owns exit 1.
            $invocationPath = Join-Path $fixture.Root ("root-mode-" + $index + "-invocation.ps1")
            $quotedRoot = $rootCopy.Replace("'", "''")
            $quotedLog = (Join-Path $fixture.Root ("root-mode-" + $index)).Replace("'", "''")
            Write-FixtureText $invocationPath ("& '" + $quotedRoot + "' -Level DEV -Module shared -LogDirectory '" +
                $quotedLog + "' -TestFilter " + $case.FilterLiteral + $Lf + 'exit $LASTEXITCODE' + $Lf)
            $arguments = @("-NoProfile", "-File", $invocationPath)
        }
        $output = @(& $pwsh @arguments 2>&1 | ForEach-Object { $_.ToString() })
        $nativeExitCode = $LASTEXITCODE
        $text = $output -join $Lf
        Write-FixtureText (Join-Path $fixture.Root ("root-mode-" + $index + ".log")) $text
        Assert-TestCondition ($nativeExitCode -eq 1 -and $text -match $case.Pattern) ("Invalid root flags failed for the wrong reason: " + ($case.Arguments -join " ") + "; " + $text)
        Assert-TestCondition (-not (Test-Path -LiteralPath $guardPath)) "Invalid root flags reached execution beyond validation."
    }
}

Invoke-RuntimeTest "original native wrapper ignores caller shadows and captures real child exits" -UseOriginalNative {
    param($fixture)
    $pwsh = Join-Path $PSHOME $(if ($IsWindows) { "pwsh.exe" } else { "pwsh" })
    $scriptPath = Join-Path $fixture.Root "fake-native-exit.ps1"
    foreach ($expected in @(0, 1, 23, 37)) {
        Write-FixtureText $scriptPath (
            '[Console]::Out.WriteLine("fixture stdout")' + $Lf +
            '[Console]::Error.WriteLine("fixture stderr")' + $Lf + "exit $expected" + $Lf)
        $logPath = Join-Path $fixture.Root ("native-exit-" + $expected + ".log")
        $run = & $fixture.Module {
            param($Executable, $ScriptPath, $Root, $LogPath)
            $PSNativeCommandUseErrorActionPreference = $true
            $script:LASTEXITCODE = 97
            $result = Invoke-VerificationNative -FilePath $Executable -Arguments @("-NoProfile", "-File", $ScriptPath) -WorkingDirectory $Root -LogPath $LogPath -Description "FAKE-FIRST PowerShell native exit fixture"
            if ($script:LASTEXITCODE -ne 97) { throw "Fixture module shadow was overwritten." }
            return $result
        } $pwsh $scriptPath $fixture.Root $logPath
        Assert-TestCondition ($run.exitCode -eq $expected) "Actual native exit was lost behind a caller shadow."
        Assert-TestCondition ($run.text.Contains("fixture stdout") -and $run.text.Contains("fixture stderr")) "Native stdout/stderr were not retained."
        Assert-TestCondition ([IO.File]::ReadAllText($logPath).Contains("fixture stderr")) "Saved native log lost stderr."
    }
    $missingLog = Join-Path $fixture.Root "missing-native-exit.log"
    Assert-TestThrows {
        & $fixture.Module {
            param($Root, $LogPath)
            function script:Invoke-FixtureWithoutNativeExit { param($Mode) Write-Output "fixture adapter: $Mode" }
            Invoke-VerificationNative -FilePath "Invoke-FixtureWithoutNativeExit" -Arguments @("no-exit") -WorkingDirectory $Root -LogPath $LogPath -Description "FAKE-FIRST no native exit"
        } $fixture.Root $missingLog
    } "Native command/provenance failed" "An invocation with no native result must not inherit prior success"
    Assert-TestCondition ([IO.File]::ReadAllText($missingLog).Contains("fixture adapter: no-exit")) "No-exit fixture did not reach its non-native adapter."
    Assert-TestThrows {
        & $fixture.Module { param($Root) Invoke-VerificationGit -RepositoryRoot $Root -Arguments @("--geocedg-fixture-invalid-option") } $fixture.RepositoryRoot
    } "git exited [1-9]" "Actual failing Git process must not read the module shadow"
}

$summary = [ordered]@{
    schemaVersion = 1
    evidenceKind = "FAKE_FIRST_OPERATIONAL_TESTS_NOT_GEOMETRIC_OR_REAL_GRADLE_EVIDENCE"
    modulePath = $ModulePath
    moduleSha256 = $ModuleSha256
    fixtureRoot = $RunRoot
    evidenceRoot = $EvidenceRoot
    fixtureLocationPolicy = "Unique retained TEMP workspace, independent of report-path depth; no global Git configuration change."
    tests = $Results.Count
    passed = @($Results | Where-Object { $_.status -ceq "PASS" }).Count
    failed = @($Results | Where-Object { $_.status -ceq "FAIL" }).Count
    gradleExecutions = 0
    authorApproved = $false
    selfApproved = $false
    results = @($Results)
}
$summaryPath = Join-Path $EvidenceRoot "verification-runtime-tests.json"
Write-FixtureText $summaryPath (($summary | ConvertTo-Json -Depth 12) + $Lf)
Write-Host ("Fake-first tests: " + $summary.passed + "/" + $summary.tests + " passed.")
Write-Host ("Saved test evidence and retained fixtures: " + $summaryPath)
Write-Host ("Retained fixture working root: " + $RunRoot)
if ($summary.failed -ne 0) { exit 1 }
exit 0

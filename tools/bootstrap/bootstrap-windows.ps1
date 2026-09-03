#requires -Version 7.2
<#
.SYNOPSIS
Prepares and verifies a Windows workstation for GeoCeDG development.

.DESCRIPTION
Inspects the clone, configures the official GeoGebra upstream remote only when
it is absent, fetches provenance refs, checks the pinned baseline and local
toolchains, then delegates repository gates to tools/agent/verify.ps1.
No software is installed and no interactive application is opened by default.

.PARAMETER SkipFetch
Uses existing local refs and tags without contacting origin or upstream.

.PARAMETER SkipBuild
Runs static and toolchain gates but skips compilation. It cannot be combined
with LaunchDesktop.

.PARAMETER RunBenchmarks
Runs the informational G1 operational benchmark through verify.ps1.

.PARAMETER LaunchDesktop
Runs the explicit interactive Desktop launch gate. Close the application
window to let the verification finish.

.PARAMETER LogDirectory
Parent directory for a unique run folder containing transcript, preflight native
logs, structured summary and delegated verification logs. Defaults to TEMP/geocedg-bootstrap.

.PARAMETER InstallPackagingPrerequisites
Runs the focused prerequisite installer and exits without fetching remotes or
executing repository verification. It installs only an approved missing .NET 8
SDK, pinned WiX 5.0.2, and the pinned WiX extensions. JDK installation is never
automated.
#>
[CmdletBinding()]
param(
    [switch]$SkipFetch,
    [switch]$SkipBuild,
    [switch]$RunBenchmarks,
    [switch]$LaunchDesktop,
    [switch]$InstallPackagingPrerequisites,
    [string]$LogDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedOrigin = "https://github.com/mpradovelasco/GeoCeDG.git"
$ExpectedUpstream = "https://github.com/geogebra/geogebra.git"
$ExpectedBaseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
$ExpectedTag = "geogebra-baseline-5.4.928.0"
$ExpectedGradleJava = $null
$ExpectedDesktopJava = $null
$ExpectedWix = "5.0.2"
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$Verifier = Join-Path $RepositoryRoot "tools\agent\verify.ps1"
$PackagingPrerequisiteInstaller = Join-Path $RepositoryRoot `
    "tools\bootstrap\install-packaging-prerequisites.ps1"
$BaselineFile = Join-Path $RepositoryRoot "docs\upstream\BASELINE_COMMIT.txt"
$GradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
$RequestedLogDirectory = $LogDirectory
$LogDirectory = $null
$RunId = [DateTime]::UtcNow.ToString("yyyyMMddTHHmmssfffZ") + "-" + [Guid]::NewGuid().ToString("N")
$CurrentStage = "diagnostic initialization"
$TranscriptStarted = $false
$Outcome = "FAIL"
$FailureDetails = $null
$WorkstationFacts = $null
$NativeRecords = [Collections.Generic.List[object]]::new()
$WarningRecords = [Collections.Generic.List[object]]::new()
$BootstrapClock = [Diagnostics.Stopwatch]::StartNew()
$Warnings = [Collections.Generic.List[string]]::new()
$InitialRepositoryStatus = $null
$PackagingJpackage = "not detected"
$PackagingDotNet = "not detected"
$PackagingWix = "not detected"
$PackagingWixExtensions = "not detected"

function Write-Step {
    param([Parameter(Mandatory)] [string]$Message)

    $script:CurrentStage = $Message
    Write-Host "`n==> $Message"
}

function Add-Warning {
    param([Parameter(Mandatory)] [string]$Message, [string]$Classification = "")
    if ([string]::IsNullOrWhiteSpace($Classification)) {
        $Classification = if ($CurrentStage -eq "Optional Windows packaging prerequisites") { "optional-packaging-prerequisite" } else { "operational-warning" }
    }
    $Warnings.Add($Message)
    $WarningRecords.Add([pscustomobject]@{ Stage = $CurrentStage; Classification = $Classification; Message = $Message })
    Write-Warning $Message
}

function Start-BootstrapDiagnostics {
    $parent = if ([string]::IsNullOrWhiteSpace($RequestedLogDirectory)) {
        Join-Path ([IO.Path]::GetTempPath()) "geocedg-bootstrap"
    } else {
        [IO.Path]::GetFullPath($RequestedLogDirectory)
    }
    $candidateLogDirectory = Join-Path $parent $RunId
    Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $RepositoryRoot -LogDirectory $candidateLogDirectory
    # Publish the usable path only after the guard succeeds. Finally must not
    # write a failure summary into a rejected requested directory.
    $script:LogDirectory = $candidateLogDirectory
    [void](New-Item -ItemType Directory -Path (Join-Path $LogDirectory "preflight") -Force)
    [void](New-Item -ItemType Directory -Path (Join-Path $LogDirectory "verification") -Force)
    Start-Transcript -Path (Join-Path $LogDirectory "bootstrap-transcript.log") -UseMinimalHeader | Out-Null
    $script:TranscriptStarted = $true
}

function Complete-BootstrapDiagnostics {
    param(
        [Parameter(Mandatory)] [Collections.IDictionary]$Summary,
        [AllowNull()] [AllowEmptyString()] [string]$LogDirectory,
        [bool]$TranscriptStarted,
        [scriptblock]$StopTranscript = { Stop-Transcript | Out-Null },
        [scriptblock]$SaveSummary = {
            param($Path, $Json)
            # Publish only a completely written summary; preserve pending evidence on failure.
            $pendingPath = $Path + ".pending"
            if ((Test-Path -LiteralPath $Path) -or (Test-Path -LiteralPath $pendingPath)) { throw "Refusing to overwrite bootstrap diagnostic evidence: $Path" }
            [IO.File]::WriteAllText($pendingPath, $Json, [Text.UTF8Encoding]::new($false))
            [IO.File]::Move($pendingPath, $Path)
        }
    )

    $finalizationErrors = [Collections.Generic.List[object]]::new()
    $transcriptStopped = $false
    $summarySaved = $false
    $summaryPath = $null
    if ($TranscriptStarted) {
        try { & $StopTranscript | Out-Null; $transcriptStopped = $true }
        catch { $finalizationErrors.Add([pscustomobject]@{ Stage = "transcript finalization"; Message = $_.Exception.Message }) }
    } else {
        $finalizationErrors.Add([pscustomobject]@{ Stage = "transcript finalization"; Message = "Required bootstrap transcript was not started." })
    }
    $Summary["Diagnostics"] = [ordered]@{
        TranscriptStarted = $TranscriptStarted; TranscriptStopped = $transcriptStopped
        SummaryPublication = "atomic pending-file move; pending files are not successful evidence"
    }
    if ($finalizationErrors.Count -gt 0) {
        $Summary["Outcome"] = "FAIL"
        if ($null -eq $Summary["Failure"]) {
            $Summary["Failure"] = [ordered]@{ Stage = "diagnostic finalization"; Classification = "diagnostic-finalization-failure"; Message = $finalizationErrors[0].Message; NativeExitCode = $null }
        }
    }
    $Summary["FinalizationErrors"] = @($finalizationErrors.ToArray())
    try {
        if ([string]::IsNullOrWhiteSpace($LogDirectory)) { throw "No bootstrap diagnostic directory was initialized." }
        $summaryPath = Join-Path $LogDirectory "bootstrap-result.json"
        & $SaveSummary $summaryPath ($Summary | ConvertTo-Json -Depth 12) | Out-Null
        $summarySaved = $true
    } catch {
        $finalizationErrors.Add([pscustomobject]@{ Stage = "summary publication"; Message = $_.Exception.Message })
        $Summary["Outcome"] = "FAIL"
        if ($null -eq $Summary["Failure"]) {
            $Summary["Failure"] = [ordered]@{ Stage = "diagnostic finalization"; Classification = "diagnostic-finalization-failure"; Message = $_.Exception.Message; NativeExitCode = $null }
        }
        $Summary["FinalizationErrors"] = @($finalizationErrors.ToArray())
    }
    return [pscustomobject]@{
        Outcome = $Summary["Outcome"]; Failure = $Summary["Failure"]
        SummarySaved = $summarySaved; SummaryPath = $summaryPath
        FinalizationErrors = @($finalizationErrors.ToArray())
    }
}

function Invoke-NativeResult {
    param([Parameter(Mandatory)] [object]$Command, [Parameter(Mandatory)] [string[]]$ArgumentList, [Parameter(Mandatory)] [string]$Description)
    $displayPath = if ($Command -is [Management.Automation.CommandInfo]) {
        if ([string]::IsNullOrWhiteSpace($Command.Source)) { $Command.Name } else { $Command.Source }
    } else { [string]$Command }
    $sequence = $NativeRecords.Count + 1
    $stem = [regex]::Replace($Description.ToLowerInvariant(), "[^a-z0-9]+", "-").Trim("-")
    $logPath = Join-Path $LogDirectory ("preflight/{0:D3}-{1}.log" -f $sequence, $stem)
    $clock = [Diagnostics.Stopwatch]::StartNew()
    $output = @()
    $code = $null
    $invocationFailure = $null
    try {
        $PSNativeCommandUseErrorActionPreference = $false
        # PowerShell writes native exits in global scope; a local sentinel
        # would shadow the actual process result. Null is not a native success.
        $global:LASTEXITCODE = $null
        $output = @(& $Command @ArgumentList 2>&1 | ForEach-Object { $_.ToString() })
        $code = $global:LASTEXITCODE
        if ($null -eq $code) { throw "The invocation completed without a native exit code." }
    } catch {
        $output += $_.Exception.ToString()
        $invocationFailure = [InvalidOperationException]::new("$Description failed before a native exit code was captured. Log: $logPath", $_.Exception)
        $invocationFailure.Data["NativeExitCode"] = $null
        $invocationFailure.Data["FailureClassification"] = "native-invocation-failure"
        $invocationFailure.Data["Stage"] = $Description
        $invocationFailure.Data["NativeOutput"] = $output -join [Environment]::NewLine
    } finally { $clock.Stop() }
    $record = [pscustomobject]@{
        Stage = $CurrentStage; Description = $Description; Command = $displayPath
        Arguments = $ArgumentList; ExitCode = $code; ElapsedSeconds = $clock.Elapsed.TotalSeconds; LogPath = $logPath; LogSaved = $false
        InvocationFailed = ($null -ne $invocationFailure)
        InvocationError = if ($null -eq $invocationFailure) { $null } else { $invocationFailure.InnerException.ToString() }
        DiagnosticFailure = $null
    }
    $NativeRecords.Add($record)
    try {
        [IO.File]::WriteAllLines($logPath, [string[]]$output, [Text.UTF8Encoding]::new($false))
        $record.LogSaved = $true
    } catch {
        $record.DiagnosticFailure = [pscustomobject]@{ Stage = "$Description native log publication"; Classification = "diagnostic-write-failure"; Message = $_.Exception.Message; LogPath = $logPath }
        if ($null -ne $invocationFailure) {
            # Publication is secondary to the original invocation error. Keep
            # both, without inventing a native exit or returning a null result.
            $invocationFailure.Data["DiagnosticFailure"] = $record.DiagnosticFailure
            throw $invocationFailure
        }
        $failure = [IO.IOException]::new("Required native diagnostic log could not be saved for $Description (native exit $code): $logPath. $($_.Exception.Message)", $_.Exception)
        $failure.Data["NativeExitCode"] = $code
        $failure.Data["FailureClassification"] = "diagnostic-write-failure"
        $failure.Data["Stage"] = $Description
        $failure.Data["NativeOutput"] = $output -join [Environment]::NewLine
        throw $failure
    }
    # Direct consumers (including WiX's integer exit parameter) must never
    # receive a launch/invocation failure as a nullable successful result.
    if ($null -ne $invocationFailure) { throw $invocationFailure }
    return [pscustomobject]@{ Output = $output; ExitCode = $code; LogPath = $logPath }
}

function Invoke-Native {
    param([Parameter(Mandatory)] [string]$FilePath, [Parameter(Mandatory)] [string[]]$ArgumentList, [Parameter(Mandatory)] [string]$Description)
    $result = Invoke-NativeResult -Command $FilePath -ArgumentList $ArgumentList -Description $Description
    if ($result.ExitCode -ne 0) {
        $failure = [InvalidOperationException]::new("$Description failed with exit code $($result.ExitCode). Log: $($result.LogPath)" + [Environment]::NewLine + ($result.Output -join [Environment]::NewLine))
        $failure.Data["NativeExitCode"] = $result.ExitCode
        $failure.Data["Stage"] = $Description
        $failure.Data["FailureClassification"] = if (($result.Output -join " ") -match "AccessDeniedException|UnauthorizedAccessException|Access is denied") { "permissions-filesystem" } else { "unknown-native-failure" }
        throw $failure
    }
    return @($result.Output)
}

function Get-CommandPath {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$ManualResolution
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        $failure = [InvalidOperationException]::new("$Name was not found. $ManualResolution")
        $failure.Data["FailureClassification"] = "unsupported-or-missing-workstation-prerequisite"
        $failure.Data["Stage"] = "External commands"
        throw $failure
    }
    return $command.Source
}

function Normalize-GitHubUrl {
    param([Parameter(Mandatory)] [string]$Url)

    $normalized = $Url.Trim().TrimEnd("/")
    if ($normalized.EndsWith(".git", [StringComparison]::OrdinalIgnoreCase)) {
        $normalized = $normalized.Substring(0, $normalized.Length - 4)
    }
    return $normalized
}

function Add-ProcessPath {
    param([Parameter(Mandatory)] [string]$Path)

    if ((Test-Path -LiteralPath $Path -PathType Container) -and
        (($env:PATH -split ";") -notcontains $Path)) {
        $env:PATH = "$Path;$env:PATH"
    }
}

function Get-CompatibleDotNetSdk {
    param([Parameter(Mandatory)] [string]$DotNetPath)

    $sdks = Invoke-Native -FilePath $DotNetPath -ArgumentList @("--list-sdks") `
        -Description ".NET SDK inventory"
    return $sdks | Where-Object {
        $_ -match "^(\d+)\." -and [int]$Matches[1] -ge 6
    } | Select-Object -First 1
}

try {
    . (Join-Path $RepositoryRoot "tools/agent/repository-generated-state.ps1")
    Start-BootstrapDiagnostics
    Import-Module (Join-Path $PSScriptRoot "workstation-prerequisites.psm1") -Force
    Import-Module (Join-Path $PSScriptRoot "packaging-prerequisites.psm1") -Force
    Write-Host "GeoCeDG Windows workstation bootstrap"
    Write-Host "Repository candidate: $RepositoryRoot"

    if ($InstallPackagingPrerequisites) {
        if ($SkipFetch -or $SkipBuild -or $RunBenchmarks -or $LaunchDesktop) {
            throw "-InstallPackagingPrerequisites is an independent action and cannot be combined with onboarding or verification options."
        }
        Write-Step "Focused Windows packaging-prerequisite installation"
        try {
            & $PackagingPrerequisiteInstaller -Install
            $installerExitCode = $LASTEXITCODE
        } catch {
            $failure = $_.Exception
            $failure.Data["InstallerReturnedNormally"] = $false
            $failure.Data["InstallerExitCode"] = $null
            if (-not $failure.Data.Contains("FailureClassification")) { $failure.Data["FailureClassification"] = "explicit-packaging-installation-failure" }
            if (-not $failure.Data.Contains("Stage")) { $failure.Data["Stage"] = $CurrentStage }
            throw
        }
        if ($installerExitCode -ne 0) {
            $failure = [InvalidOperationException]::new("Focused packaging-prerequisite installation failed with exit code $installerExitCode.")
            $failure.Data["NativeExitCode"] = $null
            $failure.Data["InstallerExitCode"] = $installerExitCode
            $failure.Data["InstallerReturnedNormally"] = $true
            $failure.Data["FailureClassification"] = "explicit-packaging-installation-failure"
            throw $failure
        }
        $Outcome = "PASS"
    } else {

    if ($SkipBuild -and $LaunchDesktop) {
        throw "-SkipBuild cannot be combined with -LaunchDesktop."
    }
    if ($PSVersionTable.PSVersion -lt [version]"7.2") {
        throw "PowerShell 7.2 or newer is required for redirected native stderr handling. Install a supported PowerShell manually and rerun with pwsh."
    }

    Write-Step "External commands"
    $gitCommand = Get-CommandPath -Name "git" -ManualResolution `
        "Install Git for Windows manually, restart PowerShell, and rerun."
    $pwshCommand = Get-CommandPath -Name "pwsh" -ManualResolution `
        "Install PowerShell 7 manually and rerun from pwsh."
    $javaCommand = Get-CommandPath -Name "java" -ManualResolution `
        "Make a Java executable available on PATH for baseline inspection; the wrapper-selected launcher must match the current profile."
    $gitVersion = (Invoke-Native -FilePath $gitCommand -ArgumentList @("--version") `
        -Description "Git version") -join " "
    $pwshVersion = (Invoke-Native -FilePath $pwshCommand -ArgumentList @("--version") `
        -Description "PowerShell version") -join " "
    $javaVersionOutput = Invoke-Native -FilePath $javaCommand `
        -ArgumentList @("-version") -Description "Java version"
    Write-Host "Git: $gitVersion"
    Write-Host "PowerShell: $pwshVersion"
    Write-Host "Java on PATH: $($javaVersionOutput[0])"

    Write-Step "GeoCeDG clone identity"
    $insideWorkTree = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "--is-inside-work-tree"
        ) -Description "Git worktree check")[-1].Trim()
    if ($insideWorkTree -ne "true") {
        throw "$RepositoryRoot is not a Git worktree. Clone GeoCeDG and run the script from that clone."
    }
    $resolvedGitRoot = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "--show-toplevel"
        ) -Description "Git repository root")[-1].Trim()
    if ([IO.Path]::GetFullPath($resolvedGitRoot) -ne $RepositoryRoot) {
        throw "Script root $RepositoryRoot does not match Git root $resolvedGitRoot."
    }
    foreach ($requiredPath in @(
            "AGENTS.md", "UPSTREAM.md", "docs\upstream\BASELINE_COMMIT.txt",
            "tools\agent\verify.ps1", "gradlew.bat")) {
        if (-not (Test-Path -LiteralPath (Join-Path $RepositoryRoot $requiredPath) `
                -PathType Leaf)) {
            throw "Required GeoCeDG repository marker is missing: $requiredPath"
        }
    }
    if (-not (Test-Path -LiteralPath $GradleWrapper -PathType Leaf)) {
        throw "The repository Gradle wrapper is missing: $GradleWrapper"
    }
    $InitialRepositoryStatus = @(Invoke-Native -FilePath $gitCommand -ArgumentList @("-C", $RepositoryRoot, "status", "--porcelain=v1", "--untracked-files=all") -Description "Initial repository status")

    $remoteNames = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "remote"
        ) -Description "Git remote inspection")
    if ($remoteNames -notcontains "origin") {
        throw "The clone has no origin remote. Clone https://github.com/mpradovelasco/GeoCeDG.git again or repair origin manually."
    }
    $originUrl = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "remote", "get-url", "origin"
        ) -Description "Inspect origin")[-1].Trim()
    Write-Host "origin: $originUrl (inspected, not modified)"
    if ((Normalize-GitHubUrl -Url $originUrl) -ne
        (Normalize-GitHubUrl -Url $ExpectedOrigin)) {
        Add-Warning "origin differs from the canonical GeoCeDG URL; it was not modified: $originUrl"
    }

    if ($remoteNames -notcontains "upstream") {
        Write-Host "Adding missing upstream: $ExpectedUpstream"
        [void](Invoke-Native -FilePath $gitCommand -ArgumentList @(
                "-C", $RepositoryRoot, "remote", "add", "upstream",
                $ExpectedUpstream
            ) -Description "Add official upstream")
        $upstreamUrl = $ExpectedUpstream
    } else {
        $upstreamUrl = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
                "-C", $RepositoryRoot, "remote", "get-url", "upstream"
            ) -Description "Inspect upstream")[-1].Trim()
        if (-not $upstreamUrl.Equals(
                $ExpectedUpstream, [StringComparison]::OrdinalIgnoreCase)) {
            throw "upstream is $upstreamUrl; expected exactly $ExpectedUpstream. It was not overwritten. Correct it manually only after reviewing the remote."
        }
    }
    Write-Host "upstream: $upstreamUrl"

    Write-Step "Pinned refs and tags"
    if ($SkipFetch) {
        Add-Warning "Fetch was skipped; baseline checks use existing local refs and tags."
    } else {
        [void](Invoke-Native -FilePath $gitCommand -ArgumentList @(
                "-C", $RepositoryRoot, "fetch", "upstream", "--prune", "--tags"
            ) -Description "Fetch upstream and tags")
        [void](Invoke-Native -FilePath $gitCommand -ArgumentList @(
                "-C", $RepositoryRoot, "fetch", "origin", "--prune", "--tags"
            ) -Description "Fetch origin tags")
    }
    $tagType = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "cat-file", "-t", "refs/tags/$ExpectedTag"
        ) -Description "Inspect baseline tag")[-1].Trim()
    if ($tagType -ne "tag") {
        throw "$ExpectedTag must be an annotated tag; found Git object type '$tagType'."
    }
    $tagTarget = @(Invoke-Native -FilePath $gitCommand -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "refs/tags/${ExpectedTag}^{}"
        ) -Description "Resolve baseline tag")[-1].Trim()
    if ($tagTarget -ne $ExpectedBaseline) {
        throw "$ExpectedTag resolves to $tagTarget; expected $ExpectedBaseline."
    }
    $recordedBaseline = (Get-Content -Raw -LiteralPath $BaselineFile).Trim()
    if ($recordedBaseline -ne $ExpectedBaseline) {
        throw "BASELINE_COMMIT.txt contains $recordedBaseline; expected $ExpectedBaseline."
    }
    Write-Host "$ExpectedTag -> $tagTarget"

    Write-Step "Current workstation prerequisite and import-origin checks"
    $requirements = Get-WorkstationRequirements -RepositoryRoot $RepositoryRoot
    $ExpectedGradleJava = $requirements.GradleJava
    $ExpectedDesktopJava = $requirements.DesktopJava
    $condaCommand = Get-Command conda -ErrorAction SilentlyContinue
    $runner = {
        param($Command, $Arguments, $Description)
        Invoke-NativeResult -Command $Command -ArgumentList $Arguments -Description $Description
    }
    Push-Location -LiteralPath $RepositoryRoot
    try {
        $WorkstationFacts = Invoke-WorkstationPrerequisiteCheck -RepositoryRoot $RepositoryRoot -JavaHome $env:JAVA_HOME -Commands @{ Java = $javaCommand; Conda = $condaCommand } -CommandRunner $runner -Requirements $requirements
    } finally { Pop-Location }
    Write-Host "Effective Gradle Java: $($WorkstationFacts.EffectiveJava.Path) ($($WorkstationFacts.EffectiveJava.Selection))"
    Write-Host "Compiler JDK: $($WorkstationFacts.CompilerToolchain.LanguageVersion) at $($WorkstationFacts.CompilerToolchain.Location)"
    Write-Host "Desktop JDK: $($WorkstationFacts.DesktopToolchain.LanguageVersion) at $($WorkstationFacts.DesktopToolchain.Location)"
    Write-Host "Conda Python: $($WorkstationFacts.Conda.python_executable); prefix=$($WorkstationFacts.Conda.python_prefix)"
    Write-Host "mpmath origin: $($WorkstationFacts.Conda.mpmath_file)"

    Write-Step "GeoCeDG verification authority"
    $verifyParameters = @{
        LogDirectory = Join-Path $LogDirectory "verification"
    }
    if ($SkipBuild) {
        $verifyParameters.SkipBuild = $true
        Add-Warning "Compilation was skipped; static, provenance and toolchain gates still run." -Classification "verification-scope"
    }
    if ($RunBenchmarks) {
        $verifyParameters.RunBenchmarks = $true
        $verifyParameters.BenchmarkOutputPath = Join-Path $LogDirectory `
            "operational-benchmark.json"
    }
    if ($LaunchDesktop) {
        $verifyParameters.LaunchDesktop = $true
    }
    & $Verifier @verifyParameters
    $verificationExitCode = $LASTEXITCODE
    if ($verificationExitCode -ne 0) {
        $failure = [InvalidOperationException]::new("tools/agent/verify.ps1 failed with exit code $verificationExitCode. Review $($verifyParameters.LogDirectory); a delegated failure is not automatically a product regression.")
        $failure.Data["NativeExitCode"] = $verificationExitCode
        $failure.Data["FailureClassification"] = "delegated-verification-failure"
        throw $failure
    }

    $gradleVersionPath = Join-Path $verifyParameters.LogDirectory "gradle-version.log"
    $toolchainPath = Join-Path $verifyParameters.LogDirectory "java-toolchains.log"
    if (-not (Test-Path -LiteralPath $gradleVersionPath -PathType Leaf) -or
        -not (Test-Path -LiteralPath $toolchainPath -PathType Leaf)) {
        throw "Verification did not produce the expected Gradle/toolchain evidence in $LogDirectory."
    }
    $gradleVersion = Get-Content -LiteralPath $gradleVersionPath
    $launcherJvm = $gradleVersion | Where-Object { $_ -match "^Launcher JVM:" } |
        Select-Object -First 1
    $daemonJvm = $gradleVersion | Where-Object { $_ -match "^Daemon JVM:" } |
        Select-Object -First 1
    $confirmedToolchains = @(ConvertFrom-GradleToolchainOutput -Output (Get-Content -LiteralPath $toolchainPath))
    foreach ($version in @($requirements.CompilerJava, $requirements.DesktopJava)) {
        if (@($confirmedToolchains | Where-Object { $_.LanguageVersion -eq $version -and $_.IsJdk }).Count -eq 0) {
            throw "Delegated verification did not confirm required JDK $version. Review $toolchainPath."
        }
    }
    $desktopToolchain = $WorkstationFacts.DesktopToolchain

    Write-Step "Optional Windows packaging prerequisites"
    $jpackagePath = Join-Path $desktopToolchain.Location "bin\jpackage.exe"
    if (Test-Path -LiteralPath $jpackagePath -PathType Leaf) {
        $PackagingJpackage = @(Invoke-Native -FilePath $jpackagePath `
            -ArgumentList @("--version") -Description "jpackage version")[-1].Trim()
        if ($PackagingJpackage -notmatch "^$ExpectedDesktopJava(?:\.|$)") {
            Add-Warning "Desktop jpackage is $PackagingJpackage; G4 was validated with Java $ExpectedDesktopJava. Select the validated JDK toolchain manually."
        }
    } else {
        Add-Warning "jpackage is missing from the Desktop JDK: $jpackagePath. Install a full JDK $ExpectedDesktopJava manually; the bootstrap does not install JDKs."
    }

    $dotnetCommand = Get-Command dotnet -ErrorAction SilentlyContinue
    $compatibleSdk = $null
    if ($null -ne $dotnetCommand) {
        $compatibleSdk = Get-CompatibleDotNetSdk -DotNetPath $dotnetCommand.Source
    }
    if ($null -eq $compatibleSdk) {
        Add-Warning ".NET SDK 6+ is missing; it is required only for MSI/EXE packaging. Recommended: winget install --id Microsoft.DotNet.SDK.8 --exact"
    } else {
        $PackagingDotNet = @(Invoke-Native -FilePath $dotnetCommand.Source `
            -ArgumentList @("--version") -Description ".NET SDK version")[-1].Trim()
    }

    $globalToolPath = Join-Path (
        [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)) `
        ".dotnet\tools"
    Add-ProcessPath -Path $globalToolPath
    $wixCommand = Get-Command wix -ErrorAction SilentlyContinue
    $observedWix = $null
    if ($null -ne $wixCommand) {
        $observedWix = @(Invoke-Native -FilePath $wixCommand.Source `
            -ArgumentList @("--version") -Description "WiX version")[-1].Trim()
    }
    if ($null -eq $observedWix) {
        Add-Warning "WiX $ExpectedWix is missing; it is required only for MSI/EXE packaging. Recommended: dotnet tool install --global wix --version $ExpectedWix --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources"
    } elseif (-not (Test-PinnedWixVersion -Version $observedWix -ExpectedVersion $ExpectedWix)) {
        Add-Warning "WiX $observedWix is installed; G4 requires pinned $ExpectedWix. Recommended: dotnet tool update --global wix --version $ExpectedWix --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources"
        $PackagingWix = $observedWix
    } else {
        $PackagingWix = $observedWix
    }
    if (Test-PinnedWixVersion -Version $observedWix -ExpectedVersion $ExpectedWix) {
        $extensionResult = Invoke-NativeResult -Command $wixCommand.Source -ArgumentList @("extension", "list", "-g") -Description "WiX global extension inventory"
        $extensionOutput = @(ConvertFrom-WixExtensionInventory -ExitCode $extensionResult.ExitCode -Output $extensionResult.Output)
        $requiredWixExtensions = @(
            "WixToolset.Util.wixext",
            "WixToolset.UI.wixext"
        )
        $missingWixExtensions = @($requiredWixExtensions | Where-Object {
            $extensionName = $_
            $null -eq ($extensionOutput | Where-Object {
                $_.ToString() -match "^$([regex]::Escape($extensionName))\s+$([regex]::Escape($ExpectedWix))$"
            } | Select-Object -First 1)
        })
        if ($missingWixExtensions.Count -gt 0) {
            Add-Warning "Pinned WiX extensions are missing: $($missingWixExtensions -join ', '). From packaging/windows run: wix extension add -g WixToolset.Util.wixext/$ExpectedWix; wix extension add -g WixToolset.UI.wixext/$ExpectedWix"
        } else {
            $PackagingWixExtensions = "Util $ExpectedWix, UI $ExpectedWix"
        }
    }

    $finalStatus = @(Invoke-Native -FilePath $gitCommand -ArgumentList @("-C", $RepositoryRoot, "status", "--porcelain=v1", "--untracked-files=all") -Description "Final repository status")
    if (($finalStatus -join "`n") -ne ($InitialRepositoryStatus -join "`n")) {
        throw "Repository status changed during bootstrap. No worktree change was expected."
    }

    Write-Step "Workstation summary"
    Write-Host "Repository: $RepositoryRoot"
    Write-Host "Baseline: $ExpectedBaseline"
    Write-Host "Tag: $ExpectedTag"
    Write-Host "Gradle launcher: $launcherJvm"
    Write-Host "Gradle daemon: $daemonJvm"
    Write-Host "Desktop run requires Java: $ExpectedDesktopJava"
    Write-Host "Detected Desktop toolchain: $($desktopToolchain.Vendor), $($desktopToolchain.Location)"
    Write-Host "jpackage: $PackagingJpackage"
    Write-Host ".NET SDK: $PackagingDotNet"
    Write-Host "WiX: $PackagingWix"
    Write-Host "WiX extensions: $PackagingWixExtensions"
    if ($LaunchDesktop) {
        Write-Host "Desktop toolchain use: exercised by :desktop:desktop:run"
    } else {
        Write-Host "Desktop toolchain use: detected but not launched; use -LaunchDesktop for the interactive gate"
    }
    Write-Host "Repository status entries: $($finalStatus.Count) (preserved by verification)"
    Write-Host "Logs: $LogDirectory"
    Write-Host "Evidence scope: existing delegated verification; optional packaging inventory is not native artifact acceptance."
    Write-Host "Current process user profile: $([Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile))"

    $Outcome = if ($Warnings.Count -eq 0) { "PASS" } else { "PASS WITH WARNINGS" }
    }
} catch {
    $Outcome = "FAIL"
    $failure = $_.Exception
    $classification = if ($failure.Data.Contains("FailureClassification")) { [string]$failure.Data["FailureClassification"] } elseif ($failure -is [UnauthorizedAccessException] -or $failure -is [IO.IOException] -or $_.CategoryInfo.Category -in @("PermissionDenied", "WriteError", "OpenError")) { "permissions-filesystem" } elseif ($CurrentStage -eq "diagnostic initialization") { "diagnostic-initialization-failure" } else { "unknown" }
    $failedStage = if ($failure.Data.Contains("Stage")) { [string]$failure.Data["Stage"] } else { $CurrentStage }
    $FailureDetails = [ordered]@{ Stage = $failedStage; Classification = $classification; Message = $failure.Message; NativeExitCode = $failure.Data["NativeExitCode"]; NativeOutput = $failure.Data["NativeOutput"]; DiagnosticFailure = $failure.Data["DiagnosticFailure"]; InstallerExitCode = $failure.Data["InstallerExitCode"]; InstallerReturnedNormally = $failure.Data["InstallerReturnedNormally"] }
    Write-Host "FAIL at $failedStage [$classification]"
    Write-Host "Logs: $LogDirectory"
    Write-Error $failure.Message -ErrorAction Continue
} finally {
    $BootstrapClock.Stop()
    try {
        $profilePath = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
        $summary = [ordered]@{
            SchemaVersion = 1; RunId = $RunId; Outcome = $Outcome; Repository = $RepositoryRoot
            ElapsedSeconds = $BootstrapClock.Elapsed.TotalSeconds; Failure = $FailureDetails
            Warnings = @($WarningRecords.ToArray()); NativeCommands = @($NativeRecords.ToArray()); WorkstationFacts = $WorkstationFacts
            Context = [ordered]@{
                Scope = "current process only; sandbox profile absence is not proof of host absence"
                UserProfile = $profilePath; PowerShell = $PSVersionTable.PSVersion.ToString()
                JavaHome = $env:JAVA_HOME
                GradleUserHomeAssumption = if ([string]::IsNullOrWhiteSpace($env:GRADLE_USER_HOME)) { Join-Path $profilePath ".gradle" } else { $env:GRADLE_USER_HOME }
                GradleUserHomeScope = "process environment/default-profile assumption only; JVM property and initialization overrides are not resolved by this field"
            }
        }
        $diagnostics = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $LogDirectory -TranscriptStarted $TranscriptStarted
        $Outcome = $diagnostics.Outcome
        $FailureDetails = $diagnostics.Failure
        foreach ($diagnosticFailure in $diagnostics.FinalizationErrors) {
            Write-Error ("Required bootstrap diagnostics failed at " + $diagnosticFailure.Stage + ": " + $diagnosticFailure.Message + ". Primary failure: " + $FailureDetails.Message) -ErrorAction Continue
        }
    } catch {
        $Outcome = "FAIL"
        $primaryMessage = if ($null -eq $FailureDetails) { "none recorded before finalization" } else { $FailureDetails.Message }
        Write-Error "Unexpected bootstrap diagnostic finalization failure: $($_.Exception.Message). Primary failure: $primaryMessage" -ErrorAction Continue
    }
}

if ($Outcome -notin @("PASS", "PASS WITH WARNINGS")) { exit 1 }
Write-Host ([Environment]::NewLine + $Outcome)
foreach ($warning in $Warnings) { Write-Host "- $warning" }
exit 0

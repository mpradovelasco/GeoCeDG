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
#>
[CmdletBinding()]
param(
    [switch]$SkipFetch,
    [switch]$SkipBuild,
    [switch]$RunBenchmarks,
    [switch]$LaunchDesktop
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedOrigin = "https://github.com/mpradovelasco/GeoCeDG.git"
$ExpectedUpstream = "https://github.com/geogebra/geogebra.git"
$ExpectedBaseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
$ExpectedTag = "geogebra-baseline-5.4.928.0"
$ExpectedGradleJava = 22
$ExpectedDesktopJava = 25
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$Verifier = Join-Path $RepositoryRoot "tools\agent\verify.ps1"
$BaselineFile = Join-Path $RepositoryRoot "docs\upstream\BASELINE_COMMIT.txt"
$GradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
$LogDirectory = Join-Path ([IO.Path]::GetTempPath()) "geocedg-bootstrap"
$Warnings = [Collections.Generic.List[string]]::new()
$InitialRepositoryStatus = $null

function Write-Step {
    param([Parameter(Mandatory)] [string]$Message)

    Write-Host "`n==> $Message"
}

function Add-Warning {
    param([Parameter(Mandatory)] [string]$Message)

    $Warnings.Add($Message)
    Write-Warning $Message
}

function Invoke-Native {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$Description
    )

    $output = @(& $FilePath @ArgumentList 2>&1)
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        $rendered = @($output | ForEach-Object { $_.ToString() }) -join `
            [Environment]::NewLine
        throw "$Description failed with exit code $exitCode.$([Environment]::NewLine)$rendered"
    }
    return @($output | ForEach-Object { $_.ToString() })
}

function Get-CommandPath {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$ManualResolution
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        throw "$Name was not found. $ManualResolution"
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

function Get-GradleToolchain {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [int]$LanguageVersion
    )

    $lines = Get-Content -LiteralPath $Path
    for ($index = 0; $index -lt $lines.Count; $index++) {
        if ($lines[$index] -notmatch "Language Version:\s*$LanguageVersion\s*$") {
            continue
        }
        $first = $index
        while ($first -gt 0 -and $lines[$first] -notmatch "^\s+\+\s") {
            $first--
        }
        $last = $index
        while ($last + 1 -lt $lines.Count -and
            $lines[$last + 1] -notmatch "^\s+\+\s") {
            $last++
        }
        $window = @($lines[$first..$last])
        $locationLine = $window | Where-Object { $_ -match "Location:\s*(.+)$" } |
            Select-Object -First 1
        $vendorLine = $window | Where-Object { $_ -match "Vendor:\s*(.+)$" } |
            Select-Object -First 1
        return [pscustomobject]@{
            LanguageVersion = $LanguageVersion
            Location = if ($locationLine -match "Location:\s*(.+)$") {
                $Matches[1].Trim()
            } else {
                "not reported"
            }
            Vendor = if ($vendorLine -match "Vendor:\s*(.+)$") {
                $Matches[1].Trim()
            } else {
                "not reported"
            }
        }
    }
    return $null
}

try {
    Write-Host "GeoCeDG Windows workstation bootstrap"
    Write-Host "Repository candidate: $RepositoryRoot"

    if ($SkipBuild -and $LaunchDesktop) {
        throw "-SkipBuild cannot be combined with -LaunchDesktop."
    }
    if ($PSVersionTable.PSVersion.Major -lt 7) {
        throw "PowerShell 7 or newer is required. Install PowerShell 7 manually and rerun this script with pwsh."
    }

    Write-Step "External commands"
    $gitCommand = Get-CommandPath -Name "git" -ManualResolution `
        "Install Git for Windows manually, restart PowerShell, and rerun."
    $pwshCommand = Get-CommandPath -Name "pwsh" -ManualResolution `
        "Install PowerShell 7 manually and rerun from pwsh."
    $javaCommand = Get-CommandPath -Name "java" -ManualResolution `
        "Install a JDK 22 manually and place its java executable on PATH."
    $gitVersion = (Invoke-Native -FilePath $gitCommand -ArgumentList @("--version") `
        -Description "Git version") -join " "
    $pwshVersion = (Invoke-Native -FilePath $pwshCommand -ArgumentList @("--version") `
        -Description "PowerShell version") -join " "
    $javaVersionOutput = Invoke-Native -FilePath $javaCommand `
        -ArgumentList @("-version") -Description "Java version"
    $javaVersionText = $javaVersionOutput -join [Environment]::NewLine
    $javaVersionMatch = [regex]::Match($javaVersionText, 'version "(\d+)(?:\.|"|$)')
    if (-not $javaVersionMatch.Success) {
        throw "Unable to determine the Java launcher major version from: $javaVersionText"
    }
    if ([int]$javaVersionMatch.Groups[1].Value -ne $ExpectedGradleJava) {
        throw "Gradle must be launched with Java $ExpectedGradleJava for the validated workstation profile; found Java $($javaVersionMatch.Groups[1].Value). Select a JDK $ExpectedGradleJava manually through JAVA_HOME/PATH and rerun."
    }
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
    $InitialRepositoryStatus = @(& $gitCommand -C $RepositoryRoot status `
        --porcelain=v1 --untracked-files=all)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect initial repository status."
    }

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

    Write-Step "GeoCeDG verification authority"
    $verifyParameters = @{
        LogDirectory = $LogDirectory
    }
    if ($SkipBuild) {
        $verifyParameters.SkipBuild = $true
        Add-Warning "Compilation was skipped; static, provenance and toolchain gates still run."
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
    if ($LASTEXITCODE -ne 0) {
        throw "tools/agent/verify.ps1 failed with exit code $LASTEXITCODE. Review $LogDirectory and resolve the reported prerequisite or gate."
    }

    $gradleVersionPath = Join-Path $LogDirectory "gradle-version.log"
    $toolchainPath = Join-Path $LogDirectory "java-toolchains.log"
    if (-not (Test-Path -LiteralPath $gradleVersionPath -PathType Leaf) -or
        -not (Test-Path -LiteralPath $toolchainPath -PathType Leaf)) {
        throw "Verification did not produce the expected Gradle/toolchain evidence in $LogDirectory."
    }
    $gradleVersion = Get-Content -LiteralPath $gradleVersionPath
    $launcherJvm = $gradleVersion | Where-Object { $_ -match "^Launcher JVM:" } |
        Select-Object -First 1
    $daemonJvm = $gradleVersion | Where-Object { $_ -match "^Daemon JVM:" } |
        Select-Object -First 1
    $desktopToolchain = Get-GradleToolchain -Path $toolchainPath `
        -LanguageVersion $ExpectedDesktopJava
    if ($null -eq $desktopToolchain) {
        throw "Gradle did not detect a Java $ExpectedDesktopJava toolchain required by Desktop run. Install a JDK $ExpectedDesktopJava manually and rerun; automatic download is disabled."
    }

    $finalStatus = @(& $gitCommand -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect final repository status."
    }
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
    if ($LaunchDesktop) {
        Write-Host "Desktop toolchain use: exercised by :desktop:desktop:run"
    } else {
        Write-Host "Desktop toolchain use: detected but not launched; use -LaunchDesktop for the interactive gate"
    }
    Write-Host "Repository status entries: $($finalStatus.Count) (preserved by verification)"
    Write-Host "Logs: $LogDirectory"

    $result = if ($Warnings.Count -eq 0) { "PASS" } else { "PASS WITH WARNINGS" }
    Write-Host "`n$result"
    if ($Warnings.Count -gt 0) {
        foreach ($warning in $Warnings) {
            Write-Host "- $warning"
        }
    }
    exit 0
} catch {
    Write-Host "`n==> Workstation summary"
    Write-Host "Repository: $RepositoryRoot"
    Write-Host "Logs: $LogDirectory"
    Write-Host "FAIL"
    Write-Error $_.Exception.Message
    exit 1
}

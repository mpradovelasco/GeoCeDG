[CmdletBinding()]
param(
    [switch]$FullTests,
    [switch]$LaunchDesktop,
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-baseline")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedBaseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
$ExpectedVersion = "5.4.928.0"
$ExpectedTag = "geogebra-baseline-5.4.928.0"
$ExpectedDesktopJava = "25"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$SharedBuildRoot = Join-Path $RepositoryRoot "source\shared"
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$BaselineFile = Join-Path $RepositoryRoot "docs\upstream\BASELINE_COMMIT.txt"
$ArchivedReadme = Join-Path $RepositoryRoot "docs\upstream\GEOGEBRA_README.md"
$VersionFile = Join-Path $RepositoryRoot `
    "source\shared\common\src\main\java\org\geogebra\common\GeoGebraConstants.java"
$DesktopBuildFile = Join-Path $RepositoryRoot "source\desktop\desktop\build.gradle.kts"
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
. (Join-Path $PSScriptRoot "upstream-boundary.ps1")
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

function Invoke-CapturedNative {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$Description
    )

    $output = & $FilePath @ArgumentList 2>&1
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        $rendered = $output -join [Environment]::NewLine
        throw "$Description failed with exit code $exitCode.$([Environment]::NewLine)$rendered"
    }
    return $output
}

function Assert-NativeSuccess {
    param(
        [Parameter(Mandatory)] [scriptblock]$Command,
        [Parameter(Mandatory)] [string]$Description
    )

    & $Command
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "$Description failed with exit code $exitCode."
    }
}

function Invoke-LoggedNative {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [string]$LogName,
        [Parameter(Mandatory)] [string]$Description
    )

    $logPath = Join-Path $LogDirectory $LogName
    Write-Host "`n==> $Description"
    Write-Host "    cwd: $WorkingDirectory"
    Write-Host "    log: $logPath"

    $exitCode = -1
    Push-Location -LiteralPath $WorkingDirectory
    try {
        & $FilePath @ArgumentList 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }

    if ($exitCode -ne 0) {
        throw "$Description failed with exit code $exitCode. See $logPath"
    }
}

function Get-GradleArguments {
    param([Parameter(Mandatory)] [string[]]$Tasks)

    $arguments = @($Tasks) + @(
        "--rerun-tasks",
        "--no-build-cache",
        "--no-daemon",
        "--no-problems-report",
        "--console=plain",
        "--stacktrace"
    )
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    return $arguments
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null

try {
    if (-not (Test-Path -LiteralPath (Join-Path $RepositoryRoot ".git"))) {
        throw "Repository root was not resolved correctly: $RepositoryRoot"
    }
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null

    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    $GeneratedState = New-RepositoryGeneratedStateSnapshot `
        -RepositoryRoot $RepositoryRoot -DirectoryNames $GeneratedDirectoryNames `
        -Label "verify-baseline"

    Write-Host "GeoCeDG baseline verification"
    Write-Host "Repository: $RepositoryRoot"

    $head = (Invoke-CapturedNative -FilePath "git" -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "HEAD"
        ) -Description "Read HEAD").Trim()
    Write-Host "HEAD: $head"

    $recordedBaseline = (Get-Content -Raw -LiteralPath $BaselineFile).Trim()
    if ($recordedBaseline -ne $ExpectedBaseline) {
        throw "Baseline file contains $recordedBaseline; expected $ExpectedBaseline."
    }
    Write-Host "Recorded baseline: $recordedBaseline"

    Assert-NativeSuccess -Description "Baseline ancestry check" -Command {
        & git -C $RepositoryRoot merge-base --is-ancestor $ExpectedBaseline HEAD
    }

    $tagTarget = (Invoke-CapturedNative -FilePath "git" -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "$ExpectedTag^{}"
        ) -Description "Resolve baseline tag").Trim()
    if ($tagTarget -ne $ExpectedBaseline) {
        throw "Tag $ExpectedTag resolves to $tagTarget; expected $ExpectedBaseline."
    }
    Write-Host "Tag: $ExpectedTag -> $tagTarget"

    $expectedReadmeBlob = (Invoke-CapturedNative -FilePath "git" -ArgumentList @(
            "-C", $RepositoryRoot, "rev-parse", "${ExpectedTag}:README.md"
        ) -Description "Resolve baseline README blob").Trim()
    $archivedReadmeBlob = (Invoke-CapturedNative -FilePath "git" -ArgumentList @(
            "-C", $RepositoryRoot, "hash-object", "--no-filters",
            $ArchivedReadme
        ) -Description "Hash archived baseline README").Trim()
    if ($archivedReadmeBlob -ne $expectedReadmeBlob) {
        throw "Archived GeoGebra README does not match $ExpectedTag exactly."
    }
    Write-Host "Archived upstream README blob: $archivedReadmeBlob"

    Assert-GeoCeDGUpstreamBoundary -RepositoryRoot $RepositoryRoot `
        -ExpectedBaseline $ExpectedBaseline `
        -GeneratedDirectoryNames $GeneratedDirectoryNames

    Assert-NativeSuccess -Description "Working-tree whitespace check" -Command {
        & git -C $RepositoryRoot diff --check
    }
    Assert-NativeSuccess -Description "Index whitespace check" -Command {
        & git -C $RepositoryRoot diff --cached --check
    }
    Assert-NativeSuccess -Description "Bootstrap tree whitespace check" -Command {
        & git -C $RepositoryRoot diff --check $ExpectedBaseline
    }

    $versionText = Get-Content -Raw -LiteralPath $VersionFile
    $versionMatch = [regex]::Match($versionText, 'VERSION_STRING\s*=\s*"([^"]+)"')
    if (-not $versionMatch.Success -or $versionMatch.Groups[1].Value -ne $ExpectedVersion) {
        throw "GeoGebra version is not $ExpectedVersion in $VersionFile."
    }
    Write-Host "GeoGebra version: $($versionMatch.Groups[1].Value)"

    $desktopBuildText = Get-Content -Raw -LiteralPath $DesktopBuildFile
    $toolchainMatch = [regex]::Match(
        $desktopBuildText,
        'JavaLanguageVersion\.of\((\d+)\)')
    if (-not $toolchainMatch.Success) {
        throw "Desktop run Java toolchain request was not found."
    }
    if ($toolchainMatch.Groups[1].Value -ne $ExpectedDesktopJava) {
        throw "Desktop run requests Java $($toolchainMatch.Groups[1].Value); expected $ExpectedDesktopJava."
    }
    Write-Host "Desktop run requests Java: $($toolchainMatch.Groups[1].Value)"

    if ($SkipBuild -and ($FullTests -or $LaunchDesktop)) {
        throw "-SkipBuild cannot be combined with -FullTests or -LaunchDesktop."
    }

    $javaCommand = (Get-Command java -ErrorAction Stop).Source
    Invoke-LoggedNative -FilePath $javaCommand -ArgumentList @("-version") `
        -WorkingDirectory $RepositoryRoot -LogName "java-version.log" `
        -Description "Java launcher version"

    Invoke-LoggedNative -FilePath $RootGradle -ArgumentList @(
            "--version", "--no-daemon", "--no-problems-report"
        ) -WorkingDirectory $RepositoryRoot -LogName "gradle-version.log" `
        -Description "Gradle wrapper version"

    $toolchainArguments = @(
        "-q", "javaToolchains", "--no-daemon", "--no-problems-report"
    )
    if (-not $AllowToolchainDownload) {
        $toolchainArguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    Invoke-LoggedNative -FilePath $RootGradle -ArgumentList $toolchainArguments `
        -WorkingDirectory $RepositoryRoot -LogName "java-toolchains.log" `
        -Description "Detected Gradle Java toolchains"

    if ($SkipBuild) {
        Write-Host "Skipping compilation because -SkipBuild was supplied."
    } else {
        Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
            Get-GradleArguments -Tasks @(
                ":canvas-base:compileJava",
                ":renderer-base:compileJava"
            )) -WorkingDirectory $SharedBuildRoot -LogName "shared-compile.log" `
            -Description "Shared canvas/renderer compilation"

        Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
            Get-GradleArguments -Tasks @(
                ":desktop:desktop:compileJava"
            )) -WorkingDirectory $RepositoryRoot -LogName "desktop-compile.log" `
            -Description "Desktop composite compilation"

        if ($FullTests) {
            Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
                Get-GradleArguments -Tasks @(
                    ":common-jre:test"
                )) -WorkingDirectory $SharedBuildRoot -LogName "shared-tests.log" `
                -Description "Shared JRE tests"

            Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
                Get-GradleArguments -Tasks @(
                    ":desktop:desktop:test"
                )) -WorkingDirectory $RepositoryRoot -LogName "desktop-tests.log" `
                -Description "Desktop tests"
        }

        if ($LaunchDesktop) {
            Write-Host "Close the GeoGebra Desktop window to complete the launch gate."
            Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
                Get-GradleArguments -Tasks @(
                    ":desktop:desktop:run"
                )) -WorkingDirectory $RepositoryRoot -LogName "desktop-run.log" `
                -Description "Interactive Desktop launch"
        }
    }
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs -Description "build output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
            if ($finalStatus -ne $InitialStatus) {
                throw "Repository status changed during verification.`nBefore:`n$InitialStatus`nAfter:`n$finalStatus"
            }
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup/status failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    Write-Error $Failure.Message
    exit 1
}

Write-Host "`nAll requested baseline gates passed."
Write-Host "Logs: $LogDirectory"
exit 0

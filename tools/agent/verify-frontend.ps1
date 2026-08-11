[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-frontend")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$SharedBuildRoot = Join-Path $RepositoryRoot "source\shared"
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$ProfilePath = Join-Path $RepositoryRoot "apps\geocedg\application-profile.yml"
$DesktopBuildFile = Join-Path $RepositoryRoot "source\desktop\desktop\build.gradle.kts"
$SharedResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.main.settings.config.AppConfigGeoCeDGTest.xml")
$DesktopResult = Join-Path $RepositoryRoot (
    "source\desktop\desktop\build\test-results\test\" +
    "TEST-org.geocedg.desktop.GeoCeDGProfileTest.xml")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

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
        "--rerun-tasks", "--no-build-cache", "--no-daemon",
        "--no-problems-report", "--console=plain", "--stacktrace"
    )
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    return $arguments
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description did not produce $Path"
    }
    [xml]$result = Get-Content -Raw -LiteralPath $Path
    $suite = $result.testsuite
    if ([int]$suite.tests -ne $ExpectedTests -or [int]$suite.failures -ne 0 -or
            [int]$suite.errors -ne 0 -or [int]$suite.skipped -ne 0) {
        throw "$Description result is not clean: tests=$($suite.tests), " +
            "failures=$($suite.failures), errors=$($suite.errors), skipped=$($suite.skipped)."
    }
    Write-Host "$Description result: $($suite.tests) tests, 0 failures."
}

$InitialStatus = $null
$GeneratedState = $null
[Exception]$Failure = $null

try {
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    $GeneratedState = New-RepositoryGeneratedStateSnapshot `
        -RepositoryRoot $RepositoryRoot -DirectoryNames $GeneratedDirectoryNames `
        -Label "verify-frontend"

    if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
        throw "GeoCeDG application profile is missing: $ProfilePath"
    }
    try {
        $profile = Get-Content -Raw -LiteralPath $ProfilePath |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "GeoCeDG application profile is invalid JSON-compatible YAML: " +
            $_.Exception.Message
    }
    if ($profile.schema_version -ne 1 -or $profile.profile_id -ne "geocedg-desktop" -or
            $profile.application.name -ne "GeoCeDG" -or
            $profile.serialization.app_code -ne "classic") {
        throw "GeoCeDG application profile identity or serialization policy is invalid."
    }

    $desktopBuild = Get-Content -Raw -LiteralPath $DesktopBuildFile
    foreach ($requiredFragment in @(
            'tasks.register<JavaExec>("runGeoCeDG")',
            'mainClass = "org.geocedg.desktop.GeoCeDG"',
            'apps/geocedg/application-profile.yml')) {
        if (-not $desktopBuild.Contains($requiredFragment)) {
            throw "Desktop build does not contain required frontend contract: $requiredFragment"
        }
    }
    Write-Host "GeoCeDG frontend contract validation passed."

    if ($SkipBuild) {
        Write-Host "Skipping focused frontend tests because -SkipBuild was supplied."
    } else {
		Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
			Get-GradleArguments -Tasks @(
				":common:checkstyleMain", ":common-jre:checkstyleTest",
				":common-jre:test", "--tests",
                "org.geocedg.common.main.settings.config.AppConfigGeoCeDGTest"
            )) -WorkingDirectory $SharedBuildRoot -LogName "frontend-shared-test.log" `
            -Description "GeoCeDG shared profile tests"
        Assert-TestResult -Path $SharedResult -Description "Shared profile tests" `
            -ExpectedTests 3

		Invoke-LoggedNative -FilePath $RootGradle -ArgumentList (
			Get-GradleArguments -Tasks @(
				":desktop:desktop:checkstyleMain",
				":desktop:desktop:checkstyleTest", ":desktop:desktop:test",
				"--tests", "org.geocedg.desktop.GeoCeDGProfileTest"
            )) -WorkingDirectory $RepositoryRoot -LogName "frontend-desktop-test.log" `
            -Description "GeoCeDG Desktop profile tests"
        Assert-TestResult -Path $DesktopResult -Description "Desktop profile tests" `
            -ExpectedTests 5
    }
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -KeepCurrentOutputs:$KeepBuildOutputs -Description "frontend output"
        }
        if ($null -ne $InitialStatus) {
            $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
            if ($finalStatus -ne $InitialStatus) {
                throw "Repository status changed during frontend verification.`nBefore:`n" +
                    "$InitialStatus`nAfter:`n$finalStatus"
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

Write-Host "`nAll requested GeoCeDG frontend gates passed."
Write-Host "Logs: $LogDirectory"
exit 0

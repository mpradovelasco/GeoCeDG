[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$Bootstrap = Join-Path $RepositoryRoot "tools\bootstrap\bootstrap-windows.ps1"
$Installer = Join-Path $RepositoryRoot `
    "tools\bootstrap\install-packaging-prerequisites.ps1"
$PlanModule = Join-Path $RepositoryRoot `
    "tools\bootstrap\packaging-prerequisites.psm1"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
Import-Module $PlanModule -Force
. $GeneratedStateHelper

function Assert-Condition {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

function Assert-Plan {
    param(
        [Parameter(Mandatory)] [object]$Plan,
        [Parameter(Mandatory)] [string]$DotNetAction,
        [Parameter(Mandatory)] [string]$WixAction,
        [Parameter(Mandatory)] [int]$MissingExtensionCount
    )

    Assert-Condition ($Plan.DotNetAction -eq $DotNetAction) `
        "Unexpected .NET action: $($Plan.DotNetAction)"
    Assert-Condition ($Plan.WixAction -eq $WixAction) `
        "Unexpected WiX action: $($Plan.WixAction)"
    Assert-Condition ($Plan.MissingWixExtensions.Count -eq $MissingExtensionCount) `
        "Unexpected missing-extension count: $($Plan.MissingWixExtensions.Count)"
    Assert-Condition (-not $Plan.InstallsJdk) `
        "The packaging prerequisite plan must never install a JDK."
}

$temporaryRepository = $null
try {
    Write-Host "==> Packaging prerequisite decision matrix"
    $extensions = @(
        "WixToolset.Util.wixext 5.0.2",
        "WixToolset.UI.wixext 5.0.2"
    )
    Assert-Plan -Plan (New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $true -WixGlobalToolVersion "5.0.2" `
        -InstalledWixExtensions $extensions -JpackageVersion "25.0.1" `
        -WingetAvailable $true) -DotNetAction "NONE" -WixAction "NONE" `
        -MissingExtensionCount 0
    Assert-Plan -Plan (New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $true -WixGlobalToolVersion "" `
        -InstalledWixExtensions @() -JpackageVersion "25" `
        -WingetAvailable $true) -DotNetAction "NONE" -WixAction "INSTALL_WIX" `
        -MissingExtensionCount 2
    $missingDotNet = New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $false -WixGlobalToolVersion "" `
        -InstalledWixExtensions @() -JpackageVersion "25" `
        -WingetAvailable $true
    Assert-Condition ($missingDotNet.DotNetAction -eq "INSTALL_DOTNET_8" -and
        -not $missingDotNet.InstallationBlocked) `
        "Missing .NET with WinGet must select only the approved .NET 8 install path."
    $manualDotNet = New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $false -WixGlobalToolVersion "" `
        -InstalledWixExtensions @() -JpackageVersion "25" `
        -WingetAvailable $false
    Assert-Condition ($manualDotNet.DotNetAction -eq "MANUAL_DOTNET_REQUIRED" -and
        $manualDotNet.InstallationBlocked) `
        "Missing .NET without WinGet must produce an actionable manual blocker."
    Assert-Plan -Plan (New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $true -WixGlobalToolVersion "6.0.2" `
        -InstalledWixExtensions $extensions -JpackageVersion "25" `
        -WingetAvailable $true) -DotNetAction "NONE" -WixAction "UPDATE_WIX" `
        -MissingExtensionCount 0
    Assert-Plan -Plan (New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $true -WixGlobalToolVersion "5.0.2" `
        -InstalledWixExtensions @("WixToolset.Util.wixext 5.0.2") `
        -JpackageVersion "25" -WingetAvailable $true) `
        -DotNetAction "NONE" -WixAction "NONE" -MissingExtensionCount 1
    $missingJdk = New-PackagingPrerequisitePlan `
        -DotNetSdkCompatible $true -WixGlobalToolVersion "5.0.2" `
        -InstalledWixExtensions $extensions -JpackageVersion "" `
        -WingetAvailable $true
    Assert-Condition (-not $missingJdk.Jpackage25Available -and
        -not $missingJdk.InstallsJdk) `
        "Missing JDK 25 must be reported without an installation action."

    Write-Host "==> Focused bootstrap ordering and authority"
    $bootstrapText = Get-Content -Raw -LiteralPath $Bootstrap
    $installBranch = $bootstrapText.IndexOf(
        'if ($InstallPackagingPrerequisites)', [StringComparison]::Ordinal)
    $externalCommands = $bootstrapText.IndexOf(
        'Write-Step "External commands"', [StringComparison]::Ordinal)
    $composedVerifier = $bootstrapText.IndexOf(
        '& $Verifier @verifyParameters', [StringComparison]::Ordinal)
    Assert-Condition ($installBranch -ge 0 -and
        $installBranch -lt $externalCommands -and
        $installBranch -lt $composedVerifier) `
        "Packaging installation must precede and bypass onboarding verification."
    $installerText = Get-Content -Raw -LiteralPath $Installer
    foreach ($forbidden in @("verify.ps1", "verify-dxf.ps1", "gradlew", "git ")) {
        Assert-Condition (-not $installerText.Contains($forbidden)) `
            "Focused prerequisite installer unexpectedly references '$forbidden'."
    }
    $verifyText = Get-Content -Raw -LiteralPath (
        Join-Path $PSScriptRoot "verify.ps1")
    Assert-Condition ($verifyText.Contains("verify-packaging.ps1")) `
        "verify.ps1 must remain the composed packaging acceptance authority."

    Write-Host "==> Generated-state transaction"
    $temporaryRepository = Join-Path ([IO.Path]::GetTempPath()) (
        "geocedg-generated-state-test-" + [guid]::NewGuid())
    [void](New-Item -ItemType Directory -Path $temporaryRepository -Force)
    & git -C $temporaryRepository init --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to create the generated-state test repository."
    }
    foreach ($module in @("module", "second")) {
        $source = Join-Path $temporaryRepository "$module\source.txt"
        [void](New-Item -ItemType Directory -Path (Split-Path -Parent $source) -Force)
        [IO.File]::WriteAllText($source, "tracked fixture`n")
        & git -C $temporaryRepository -c core.autocrlf=false add "$module/source.txt"
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to stage the generated-state fixture source."
        }
    }
    $preexisting = Join-Path $temporaryRepository "module\build\preexisting.txt"
    [void](New-Item -ItemType Directory -Path (Split-Path -Parent $preexisting) -Force)
    [IO.File]::WriteAllText($preexisting, "before`n")
    $statusBefore = Get-RepositoryStatusText -RepositoryRoot $temporaryRepository
    $snapshot = New-RepositoryGeneratedStateSnapshot `
        -RepositoryRoot $temporaryRepository -DirectoryNames @(
            "build", ".gradle", ".kotlin") -Label "contract-test"
    [IO.File]::WriteAllText($preexisting, "changed`n")
    $added = Join-Path $temporaryRepository "module\build\added.txt"
    [IO.File]::WriteAllText($added, "generated`n")
    $newBuild = Join-Path $temporaryRepository "second\build\new.txt"
    [void](New-Item -ItemType Directory -Path (Split-Path -Parent $newBuild) -Force)
    [IO.File]::WriteAllText($newBuild, "generated`n")
    Restore-RepositoryGeneratedStateSnapshot -Snapshot $snapshot `
        -Description "contract-test output"
    $statusAfter = Get-RepositoryStatusText -RepositoryRoot $temporaryRepository
    Assert-Condition ($statusAfter -eq $statusBefore) `
        "Generated-state restoration changed Git-visible worktree state."
    Assert-Condition ((Get-Content -Raw -LiteralPath $preexisting) -eq "before`n") `
        "Pre-existing generated content was not restored."
    Assert-Condition (-not (Test-Path -LiteralPath $added) -and
        -not (Test-Path -LiteralPath $newBuild)) `
        "New generated content was not removed."

    Write-Host "Workstation operational contract verification passed."
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $temporaryRepository -and
            (Test-Path -LiteralPath $temporaryRepository -PathType Container)) {
        Remove-Item -LiteralPath $temporaryRepository -Recurse -Force
    }
}

exit 0

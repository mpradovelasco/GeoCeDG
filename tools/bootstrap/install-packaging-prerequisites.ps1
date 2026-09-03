#requires -Version 7.2
<#
.SYNOPSIS
Inspects or explicitly installs the pinned Windows packaging prerequisites.

.DESCRIPTION
This focused setup action is independent of repository verification. It may
install the approved .NET 8 SDK, WiX 5.0.2, and the pinned WiX extensions. It
never installs a JDK and never changes Git state or persistent environment
variables.

.PARAMETER Install
Performs the missing approved installations. Without this switch the script is
inspection-only.
#>
[CmdletBinding()]
param([switch]$Install)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ExpectedWix = "5.0.2"
$RequiredWixExtensions = @(
    "WixToolset.Util.wixext",
    "WixToolset.UI.wixext"
)
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$WixConfigRoot = Join-Path $RepositoryRoot "packaging\windows"
$ModulePath = Join-Path $PSScriptRoot "packaging-prerequisites.psm1"
Import-Module $ModulePath -Force

function Invoke-Native {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$Description
    )

    $PSNativeCommandUseErrorActionPreference = $false
    $output = @()
    $exitCode = $null
    try {
        $global:LASTEXITCODE = $null
        $output = @(& $FilePath @ArgumentList 2>&1)
        $exitCode = $global:LASTEXITCODE
        if ($null -eq $exitCode) { throw "The invocation completed without a native exit code." }
    } catch {
        $failure = [InvalidOperationException]::new("$Description could not launch or complete native invocation: $($_.Exception.Message)", $_.Exception)
        $failure.Data["NativeExitCode"] = $null
        $failure.Data["Stage"] = $Description
        $failure.Data["FailureClassification"] = "packaging-native-launch-failure"
        $failure.Data["NativeOutput"] = $output -join [Environment]::NewLine
        throw $failure
    }
    if ($exitCode -ne 0) {
        $failure = [InvalidOperationException]::new("$Description failed with exit code $exitCode." + [Environment]::NewLine + ($output -join [Environment]::NewLine))
        $failure.Data["NativeExitCode"] = $exitCode
        $failure.Data["Stage"] = $Description
        $failure.Data["FailureClassification"] = if ($Install) { "explicit-packaging-installation-failure" } else { "packaging-prerequisite-inspection-failure" }
        $failure.Data["NativeOutput"] = $output -join [Environment]::NewLine
        throw $failure
    }
    return @($output | ForEach-Object { $_.ToString() })
}

function Add-ProcessPath {
    param([Parameter(Mandatory)] [string]$Path)

    if ((Test-Path -LiteralPath $Path -PathType Container) -and
            (($env:PATH -split ";") -notcontains $Path)) {
        $env:PATH = "$Path;$env:PATH"
    }
}

function Resolve-DotNet {
    $command = Get-Command dotnet -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }
    $candidate = Join-Path $env:ProgramFiles "dotnet\dotnet.exe"
    if (Test-Path -LiteralPath $candidate -PathType Leaf) {
        return $candidate
    }
    return $null
}

function Get-CompatibleDotNetSdk {
    param([AllowNull()] [string]$DotNetPath)

    if ([string]::IsNullOrWhiteSpace($DotNetPath)) {
        return $null
    }
    $sdks = Invoke-Native -FilePath $DotNetPath -ArgumentList @("--list-sdks") `
        -Description ".NET SDK inventory"
    return $sdks | Where-Object {
        $_ -match "^(\d+)\." -and [int]$Matches[1] -ge 6
    } | Select-Object -First 1
}

function Get-WixGlobalToolVersion {
    param([AllowNull()] [string]$DotNetPath)

    if ([string]::IsNullOrWhiteSpace($DotNetPath)) {
        return ""
    }
    $tools = Invoke-Native -FilePath $DotNetPath `
        -ArgumentList @("tool", "list", "--global") `
        -Description "global .NET tool inventory"
    $row = $tools | Where-Object { $_ -match "^wix\s+(\S+)\s+wix\s*$" } |
        Select-Object -First 1
    if ($null -ne $row -and $row -match "^wix\s+(\S+)") {
        return $Matches[1]
    }
    return ""
}

function Resolve-Wix {
    $profile = [Environment]::GetFolderPath(
        [Environment+SpecialFolder]::UserProfile)
    $candidate = Join-Path $profile ".dotnet\tools\wix.exe"
    if (Test-Path -LiteralPath $candidate -PathType Leaf) {
        return $candidate
    }
    $command = Get-Command wix -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }
    return $null
}

function Get-WixExtensions {
    param([AllowNull()] [string]$WixPath)

    if ([string]::IsNullOrWhiteSpace($WixPath)) {
        return @()
    }
    $PSNativeCommandUseErrorActionPreference = $false
    $output = @()
    $exitCode = $null
    try {
        $global:LASTEXITCODE = $null
        $output = @(& $WixPath extension list -g 2>&1)
        $exitCode = $global:LASTEXITCODE
        if ($null -eq $exitCode) { throw "The invocation completed without a native exit code." }
        return @(ConvertFrom-WixExtensionInventory -ExitCode $exitCode -Output $output)
    } catch {
        $failure = [InvalidOperationException]::new("WiX extension inventory failed: $($_.Exception.Message)", $_.Exception)
        $failure.Data["NativeExitCode"] = $exitCode
        $failure.Data["Stage"] = "WiX extension inventory"
        $failure.Data["FailureClassification"] = if ($null -eq $exitCode) { "packaging-native-launch-failure" } elseif ($Install) { "explicit-packaging-installation-failure" } else { "packaging-prerequisite-inspection-failure" }
        $failure.Data["NativeOutput"] = $output -join [Environment]::NewLine
        throw $failure
    }
}

function Find-Jpackage25 {
    $candidates = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase)
    $command = Get-Command jpackage -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        [void]$candidates.Add($command.Source)
    }
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        [void]$candidates.Add((Join-Path $env:JAVA_HOME "bin\jpackage.exe"))
    }
    $profile = [Environment]::GetFolderPath(
        [Environment+SpecialFolder]::UserProfile)
    $gradleJdks = Join-Path $profile ".gradle\jdks"
    if (Test-Path -LiteralPath $gradleJdks -PathType Container) {
        foreach ($candidate in Get-ChildItem -LiteralPath $gradleJdks `
                -Filter "jpackage.exe" -File -Recurse -ErrorAction SilentlyContinue) {
            [void]$candidates.Add($candidate.FullName)
        }
    }
    foreach ($candidate in $candidates) {
        if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
            continue
        }
        $version = @(Invoke-Native -FilePath $candidate -ArgumentList @("--version") `
            -Description "jpackage version")[-1].Trim()
        if ($version -match "^25(?:\.|$)") {
            return [pscustomobject]@{ Path = $candidate; Version = $version }
        }
    }
    return $null
}

try {
    if (-not $IsWindows) {
        throw "Windows packaging prerequisites can only be prepared on Windows."
    }
    if ($PSVersionTable.PSVersion -lt [version]"7.2") {
        throw "PowerShell 7.2 or newer is required for redirected native stderr handling."
    }

    Write-Host "GeoCeDG focused Windows packaging-prerequisite setup"
    Write-Host "Mode: $(if ($Install) { 'INSTALL' } else { 'INSPECT ONLY' })"
    Write-Host "Repository verification is not executed by this action."

    $inventory = {
        Get-PackagingPrerequisiteInventory -Probe {
            param($Name, $Value)
            switch ($Name) {
                "DotNet" { Resolve-DotNet }
                "CompatibleSdk" { Get-CompatibleDotNetSdk -DotNetPath $Value }
                "WixVersion" { Get-WixGlobalToolVersion -DotNetPath $Value }
                "WixPath" { Resolve-Wix }
                "WixExtensions" { Get-WixExtensions -WixPath $Value }
                "Jpackage" { Find-Jpackage25 }
                "Winget" { Get-Command winget -ErrorAction SilentlyContinue }
                default { throw "Unknown packaging inventory action: $Name" }
            }
        }
    }
    $apply = {
        param($Action, $State, $Value)
        switch ($Action) {
            "InstallDotNetSdk" {
                Write-Host "Installing the repository-approved .NET 8 SDK through WinGet."
                [void](Invoke-Native -FilePath $State.Winget.Source -ArgumentList @("install", "--id", "Microsoft.DotNet.SDK.8", "--exact", "--accept-package-agreements", "--accept-source-agreements", "--disable-interactivity") -Description "Install .NET 8 SDK")
                Add-ProcessPath -Path (Join-Path $env:ProgramFiles "dotnet")
            }
            { $_ -in @("InstallWix", "UpdateWix") } {
                $verb = if ($Action -eq "InstallWix") { "install" } else { "update" }
                Write-Host "$verb WiX $ExpectedWix as a pinned global .NET tool."
                [void](Invoke-Native -FilePath $State.DotNet -ArgumentList @("tool", $verb, "--global", "wix", "--version", $ExpectedWix, "--add-source", "https://api.nuget.org/v3/index.json", "--ignore-failed-sources") -Description "$verb WiX $ExpectedWix")
                $globalToolPath = Join-Path ([Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)) ".dotnet/tools"
                Add-ProcessPath -Path $globalToolPath
            }
            "AddWixExtension" {
                Push-Location -LiteralPath $WixConfigRoot
                try {
                    Write-Host "Installing $Value/$ExpectedWix."
                    [void](Invoke-Native -FilePath $State.WixPath -ArgumentList @("extension", "add", "-g", "$Value/$ExpectedWix") -Description "Install $Value $ExpectedWix")
                } finally { Pop-Location }
            }
            default { throw "Unknown packaging installation action: $Action" }
        }
    }
    $result = Invoke-PackagingPrerequisiteWorkflow -Install:$Install -Inventory $inventory -Apply $apply -ExpectedWixVersion $ExpectedWix -RequiredWixExtensions $RequiredWixExtensions
    $state = $result.Inventory
    $dotnet = $state.DotNet
    $compatibleSdk = $state.CompatibleSdk
    $wixVersion = $state.WixGlobalToolVersion
    $wix = $state.WixPath
    $extensions = $state.Extensions
    $jpackage = $state.Jpackage
    $finalPlan = $result.Plan
    if ($state.WixInventoryDeferred) {
        Write-Host "WiX inventory deferred: a compatible .NET SDK is not available; no SDK-dependent command was attempted."
    }

    Write-Host "`n==> Packaging prerequisite summary"
    Write-Host ".NET SDK 6+: $(if ($null -eq $compatibleSdk) { 'not detected' } else { $compatibleSdk })"
    Write-Host "WiX global tool: $(if ([string]::IsNullOrWhiteSpace($wixVersion)) { 'not detected' } else { $wixVersion })"
    $extensionState = if ($finalPlan.MissingWixExtensions.Count -eq 0) {
        "Util $ExpectedWix, UI $ExpectedWix"
    } else {
        "missing: $($finalPlan.MissingWixExtensions -join ', ')"
    }
    Write-Host "WiX extensions: $extensionState"
    Write-Host "jpackage 25: $(if ($null -eq $jpackage) { 'not detected; install a full JDK 25 manually' } else { "$($jpackage.Version) at $($jpackage.Path)" })"
    Write-Host "JDK installation performed: NEVER"

    $packagingReady = ($null -ne $compatibleSdk -and
        ($wixVersion -split "\+")[0] -eq $ExpectedWix -and
        $finalPlan.MissingWixExtensions.Count -eq 0)
    if ($Install -and -not $packagingReady) {
        throw "The requested .NET/WiX prerequisite installation is incomplete."
    }
    if (-not $packagingReady -or $null -eq $jpackage) {
        Write-Host "PASS WITH WARNINGS"
        if (-not $Install) {
            Write-Host "Run .\tools\bootstrap\bootstrap-windows.ps1 -InstallPackagingPrerequisites to install the approved .NET/WiX prerequisites."
        }
    } else {
        Write-Host "PASS"
    }
    exit 0
} catch {
    Write-Host "FAIL"
    # Preserve the original exception/Data across an in-process bootstrap caller.
    # An uncaught terminating error still makes standalone -File invocation fail.
    throw
}

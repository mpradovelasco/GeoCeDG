#requires -Version 7.2
Set-StrictMode -Version Latest

function New-PackagingPrerequisitePlan {
    param(
        [Parameter(Mandatory)] [bool]$DotNetSdkCompatible,
        [AllowEmptyString()] [string]$WixGlobalToolVersion,
        [string[]]$InstalledWixExtensions = @(),
        [AllowEmptyString()] [string]$JpackageVersion,
        [Parameter(Mandatory)] [bool]$WingetAvailable,
        [string]$ExpectedWixVersion = "5.0.2",
        [string[]]$RequiredWixExtensions = @(
            "WixToolset.Util.wixext", "WixToolset.UI.wixext")
    )

    $dotNetAction = if ($DotNetSdkCompatible) {
        "NONE"
    } elseif ($WingetAvailable) {
        "INSTALL_DOTNET_8"
    } else {
        "MANUAL_DOTNET_REQUIRED"
    }

    $normalizedWix = if ([string]::IsNullOrWhiteSpace($WixGlobalToolVersion)) {
        ""
    } else {
        ($WixGlobalToolVersion -split "\+")[0]
    }
    $wixAction = if ([string]::IsNullOrWhiteSpace($normalizedWix)) {
        "INSTALL_WIX"
    } elseif ($normalizedWix -ne $ExpectedWixVersion) {
        "UPDATE_WIX"
    } else {
        "NONE"
    }

    $missingExtensions = @($RequiredWixExtensions | Where-Object {
            $name = $_
            $null -eq ($InstalledWixExtensions | Where-Object {
                    $_ -match "^$([regex]::Escape($name))\s+$([regex]::Escape($ExpectedWixVersion))$"
                } | Select-Object -First 1)
        })

    return [pscustomobject]@{
        DotNetAction = $dotNetAction
        WixAction = $wixAction
        MissingWixExtensions = $missingExtensions
        Jpackage25Available = (-not [string]::IsNullOrWhiteSpace($JpackageVersion) -and
            $JpackageVersion -match "^25(?:\.|$)")
        InstallsJdk = $false
        InstallationBlocked = ($dotNetAction -eq "MANUAL_DOTNET_REQUIRED")
    }
}

function Test-PinnedWixVersion {
    param([AllowNull()] [AllowEmptyString()] [string]$Version, [string]$ExpectedVersion = "5.0.2")
    return (-not [string]::IsNullOrWhiteSpace($Version) -and ($Version.Trim() -split "\+")[0] -ceq $ExpectedVersion)
}

function ConvertFrom-WixExtensionInventory {
    param([int]$ExitCode, [AllowEmptyCollection()] [object[]]$Output = @())
    $lines = @($Output | ForEach-Object { $_.ToString() })
    if ($ExitCode -eq 0 -or ($ExitCode -eq 2 -and $lines.Count -eq 0)) { return $lines }
    $failure = [InvalidOperationException]::new("WiX global extension inventory failed with exit code $ExitCode." + [Environment]::NewLine + ($lines -join [Environment]::NewLine))
    $failure.Data["FailureClassification"] = "optional-packaging-inspection-failure"
    $failure.Data["NativeExitCode"] = $ExitCode
    $failure.Data["Stage"] = "WiX global extension inventory"
    throw $failure
}

function Get-PackagingPrerequisiteInventory {
    param([Parameter(Mandatory)] [scriptblock]$Probe)
    $dotnet = & $Probe "DotNet" $null
    $sdk = & $Probe "CompatibleSdk" $dotnet
    $wixVersion = ""
    $wix = $null
    $extensions = @()
    # A runtime-only installation cannot execute SDK-dependent global-tool commands.
    if ($null -ne $sdk) {
        $wixVersion = & $Probe "WixVersion" $dotnet
        $wix = & $Probe "WixPath" $null
        $extensions = @(& $Probe "WixExtensions" $wix)
    }
    return [pscustomobject]@{
        DotNet = $dotnet
        CompatibleSdk = $sdk
        WixGlobalToolVersion = [string]$wixVersion
        WixPath = $wix
        Extensions = $extensions
        Jpackage = & $Probe "Jpackage" $null
        Winget = & $Probe "Winget" $null
        WixInventoryDeferred = ($null -eq $sdk)
    }
}

function Invoke-PackagingPrerequisiteWorkflow {
    param(
        [switch]$Install,
        [Parameter(Mandatory)] [scriptblock]$Inventory,
        [Parameter(Mandatory)] [scriptblock]$Apply,
        [string]$ExpectedWixVersion = "5.0.2",
        [string[]]$RequiredWixExtensions = @("WixToolset.Util.wixext", "WixToolset.UI.wixext")
    )
    function Get-CurrentPlan {
        param([object]$State)
        $jpackageVersion = if ($null -eq $State.Jpackage) { "" } else { $State.Jpackage.Version }
        New-PackagingPrerequisitePlan -DotNetSdkCompatible ($null -ne $State.CompatibleSdk) -WixGlobalToolVersion $State.WixGlobalToolVersion -InstalledWixExtensions $State.Extensions -JpackageVersion $jpackageVersion -WingetAvailable ($null -ne $State.Winget) -ExpectedWixVersion $ExpectedWixVersion -RequiredWixExtensions $RequiredWixExtensions
    }
    $state = & $Inventory
    $plan = Get-CurrentPlan $state
    $initialPlan = $plan
    if ($Install) {
        if ($plan.DotNetAction -eq "MANUAL_DOTNET_REQUIRED") {
            throw "A compatible .NET SDK is missing and WinGet is unavailable. Install a supported .NET SDK manually, reopen PowerShell, and inspect again."
        }
        if ($plan.DotNetAction -eq "INSTALL_DOTNET_8") {
            & $Apply "InstallDotNetSdk" $state $null
            $state = & $Inventory
            if ($null -eq $state.CompatibleSdk) { throw ".NET installation completed but no compatible SDK is visible. Reopen PowerShell and inspect again." }
            $plan = Get-CurrentPlan $state
        }
        if ($plan.WixAction -ne "NONE") {
            $action = if ($plan.WixAction -eq "INSTALL_WIX") { "InstallWix" } else { "UpdateWix" }
            & $Apply $action $state $null
            $state = & $Inventory
            $plan = Get-CurrentPlan $state
            if ($plan.WixAction -ne "NONE") { throw "Pinned WiX installation/update did not produce the required version $ExpectedWixVersion." }
        }
        foreach ($extension in $plan.MissingWixExtensions) {
            if ([string]::IsNullOrWhiteSpace($state.WixPath)) { throw "WiX is unavailable; its pinned extensions cannot be installed." }
            & $Apply "AddWixExtension" $state $extension
        }
        $state = & $Inventory
        $plan = Get-CurrentPlan $state
    }
    $ready = ($null -ne $state.CompatibleSdk -and (Test-PinnedWixVersion $state.WixGlobalToolVersion $ExpectedWixVersion) -and $plan.MissingWixExtensions.Count -eq 0)
    if ($Install -and -not $ready) { throw "The requested .NET/WiX prerequisite installation is incomplete." }
    return [pscustomobject]@{ InitialPlan = $initialPlan; Inventory = $state; Plan = $plan; PackagingReady = $ready }
}

Export-ModuleMember -Function New-PackagingPrerequisitePlan, Test-PinnedWixVersion, ConvertFrom-WixExtensionInventory, Get-PackagingPrerequisiteInventory, Invoke-PackagingPrerequisiteWorkflow

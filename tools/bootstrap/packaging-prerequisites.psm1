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

Export-ModuleMember -Function New-PackagingPrerequisitePlan

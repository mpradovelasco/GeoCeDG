<#
.SYNOPSIS
Validates the GeoCeDG G4 Windows packaging contracts and optional artifacts.

.PARAMETER CheckToolchain
Requires the validated Java 25 jpackage, .NET SDK 6+, and WiX 5.0.2.

.PARAMETER RequireArtifacts
Validates a complete app-image/ZIP/MSI/EXE build and implies CheckToolchain.

.PARAMETER ArtifactRoot
Generated artifact root. Defaults to artifacts/packaging/windows.
#>
[CmdletBinding()]
param(
    [switch]$CheckToolchain,
    [switch]$RequireArtifacts,
    [string]$ArtifactRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$ExpectedMarker = "INTERNAL EVALUATION — NOT FOR REDISTRIBUTION"
$ExpectedWix = "5.0.2"
$ExpectedNativeExtension = "cedg"
$ExpectedInternalMimeType = "application/x-geocedg-cedg"
$ExpectedMimeBasis = "jdk25-jpackage-required-internal-unregistered"
$ExpectedAssociationDescription = "GeoCeDG document (internal evaluation)"
$ExpectedProgIdStrategy = "jdk25-jpackage-generated-geocedg-owned"
$UpstreamGeoGebraMimeType = "application/vnd.geogebra.file"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
if ([string]::IsNullOrWhiteSpace($ArtifactRoot)) {
    $ArtifactRoot = Join-Path $RepositoryRoot "artifacts\packaging\windows"
} else {
    $ArtifactRoot = [IO.Path]::GetFullPath($ArtifactRoot)
}
. (Join-Path $PSScriptRoot "repository-generated-state.ps1")

function Write-Step {
    param([Parameter(Mandatory)] [string]$Message)

    Write-Host "==> $Message"
}

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Read-JsonFile {
    param([Parameter(Mandatory)] [string]$Path)

    Assert-Condition -Condition (Test-Path -LiteralPath $Path -PathType Leaf) `
        -Message "Required JSON-compatible document is missing: $Path"
    try {
        return Get-Content -Raw -LiteralPath $Path |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$Path is not valid JSON: $($_.Exception.Message)"
    }
}

function Invoke-Captured {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$Description
    )

    $output = @(& $FilePath @ArgumentList 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE.`n$($output -join "`n")"
    }
    return @($output | ForEach-Object { $_.ToString() })
}

function Resolve-Jpackage {
    $gradle = Join-Path $RepositoryRoot "gradlew.bat"
    $output = Invoke-Captured -FilePath $gradle -ArgumentList @(
        "-q", "javaToolchains", "--no-daemon", "--console=plain"
    ) -Description "Gradle Java toolchain discovery"
    for ($index = 0; $index -lt $output.Count; $index++) {
        if ($output[$index] -notmatch "Language Version:\s*25\s*$") {
            continue
        }
        $first = $index
        while ($first -gt 0 -and $output[$first] -notmatch "^\s+\+\s") {
            $first--
        }
        $last = $index
        while ($last + 1 -lt $output.Count -and
            $output[$last + 1] -notmatch "^\s+\+\s") {
            $last++
        }
        $location = $output[$first..$last] |
            Where-Object { $_ -match "Location:\s*(.+)$" } |
            Select-Object -First 1
        if ($location -match "Location:\s*(.+)$") {
            return Join-Path $Matches[1].Trim() "bin\jpackage.exe"
        }
    }
    throw "Gradle did not report the Java 25 Desktop toolchain."
}

function Assert-MsiNativeAssociation {
    param(
        [Parameter(Mandatory)] [string]$MsiPath,
        [Parameter(Mandatory)] [string]$WixPath,
        [Parameter(Mandatory)] [string]$InspectionRoot
    )

    [void](New-Item -ItemType Directory -Path $InspectionRoot -Force)
    $msiHashValue = Get-FileHash -LiteralPath $MsiPath -Algorithm SHA256
    $msiHash = $msiHashValue.Hash.ToLowerInvariant()
    $decompiledPath = Join-Path $InspectionRoot "msi-$msiHash.wxs"
    if (Test-Path -LiteralPath $decompiledPath -PathType Leaf) {
        Remove-Item -LiteralPath $decompiledPath -Force
    }
    [void](Invoke-Captured -FilePath $WixPath -ArgumentList @(
        "msi", "decompile", "-sui", "-o", $decompiledPath, $MsiPath
    ) -Description "WiX MSI association inspection")

    try {
        [xml]$source = Get-Content -Raw -LiteralPath $decompiledPath
    } catch {
        throw "WiX MSI decompilation did not produce valid XML: $($_.Exception.Message)"
    }
    $extensionNodes = @($source.SelectNodes("//*[local-name()='Extension']"))
    $nativeExtensions = @($extensionNodes | Where-Object {
        $_.GetAttribute("Id") -ceq $ExpectedNativeExtension
    })
    Assert-Condition -Condition ($nativeExtensions.Count -eq 1) `
        -Message "MSI must contain exactly one .$ExpectedNativeExtension extension registration."
    Assert-Condition -Condition (@($extensionNodes | Where-Object {
        $_.GetAttribute("Id") -ieq "ggb"
    }).Count -eq 0) `
        -Message "MSI must not claim the .ggb compatibility extension."

    $nativeExtension = $nativeExtensions[0]
    Assert-Condition -Condition (
        $nativeExtension.GetAttribute("ContentType") -ceq
        $ExpectedInternalMimeType) `
        -Message "MSI native extension has an unexpected MIME value."
    $mimeNodes = @($nativeExtension.SelectNodes("./*[local-name()='MIME']"))
    Assert-Condition -Condition (
        $mimeNodes.Count -eq 1 -and
        $mimeNodes[0].GetAttribute("ContentType") -ceq
        $ExpectedInternalMimeType) `
        -Message "MSI native extension lacks its jpackage-required internal MIME record."
    Assert-Condition -Condition (@($source.SelectNodes("//*[@ContentType]") |
        Where-Object {
            $_.GetAttribute("ContentType") -ceq $UpstreamGeoGebraMimeType
        }).Count -eq 0) `
        -Message "MSI must not reuse the upstream GeoGebra MIME identity."

    $progId = $nativeExtension.ParentNode
    Assert-Condition -Condition (
        $progId.LocalName -ceq "ProgId" -and
        -not [string]::IsNullOrWhiteSpace($progId.GetAttribute("Id")) -and
        $progId.GetAttribute("Description") -ceq
        $ExpectedAssociationDescription -and
        $progId.GetAttribute("Id") -notmatch "(?i)geogebra") `
        -Message "MSI native extension is not owned by a GeoCeDG-described jpackage ProgID."

    $openVerbs = @($nativeExtension.SelectNodes(
        "./*[local-name()='Verb' and @Id='open']"))
    Assert-Condition -Condition ($openVerbs.Count -eq 1) `
        -Message "MSI native extension must contain exactly one open verb."
    $targetFileId = $openVerbs[0].GetAttribute("TargetFile")
    Assert-Condition -Condition (
        -not [string]::IsNullOrWhiteSpace($targetFileId) -and
        $openVerbs[0].GetAttribute("Argument").Contains("%1")) `
        -Message "MSI native open verb does not pass the selected document to a launcher."
    $targetFiles = @($source.SelectNodes("//*[local-name()='File']") |
        Where-Object { $_.GetAttribute("Id") -ceq $targetFileId })
    Assert-Condition -Condition ($targetFiles.Count -eq 1) `
        -Message "MSI native open verb does not resolve to exactly one packaged file."
    $launcherIdentity = @(
        $targetFiles[0].GetAttribute("Name"),
        $targetFiles[0].GetAttribute("Source")
    ) -join "|"
    Assert-Condition -Condition ($launcherIdentity -match "(?i)GeoCeDG\.exe") `
        -Message "MSI native open verb does not target the GeoCeDG launcher."

    Write-Host "MSI association: .$ExpectedNativeExtension -> $($progId.GetAttribute('Id')) -> GeoCeDG.exe"
    Write-Host "MSI association inspection: $decompiledPath"
}

$InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
$GeneratedState = $null
if ($CheckToolchain -or $RequireArtifacts) {
    $GeneratedState = New-RepositoryGeneratedStateSnapshot `
        -RepositoryRoot $RepositoryRoot `
        -DirectoryNames $GeneratedDirectoryNames -Label "verify-packaging"
}
[Exception]$Failure = $null

try {
    Write-Step "R2-D17 Windows native association and portable-boundary contracts"
    $requiredFiles = @(
        "docs\adr\0004-standalone-windows-packaging.md",
        "docs\adr\0016-native-geocedg-document-identity.md",
        "geocedg\specs\packaging\windows-packaging.md",
        "geocedg\specs\operations\package-profile.schema.json",
        "geocedg\specs\ui\native-document-identity.md",
        "packaging\windows\package.yml",
        "packaging\windows\NuGet.Config",
        "packaging\windows\file-associations.properties",
        "packaging\windows\INTERNAL_EVALUATION_ONLY.txt",
        "tools\release\build-windows-package.ps1",
        "LICENSE",
        "LICENSES\README.md",
        "NOTICE.md",
        "THIRD_PARTY.md",
        "geocedg\resources\assets-manifest.yml"
    )
    foreach ($relative in $requiredFiles) {
        Assert-Condition -Condition (Test-Path -LiteralPath (
            Join-Path $RepositoryRoot $relative) -PathType Leaf) `
            -Message "Required G4 file is missing: $relative"
    }

    $profile = Read-JsonFile -Path (
        Join-Path $RepositoryRoot "packaging\windows\package.yml")
    $schema = Read-JsonFile -Path (
        Join-Path $RepositoryRoot "geocedg\specs\operations\package-profile.schema.json")
    $assets = Read-JsonFile -Path (
        Join-Path $RepositoryRoot "geocedg\resources\assets-manifest.yml")
    $associationPath = Join-Path $RepositoryRoot `
        "packaging\windows\file-associations.properties"
    try {
        $association = Get-Content -Raw -LiteralPath $associationPath |
            ConvertFrom-StringData
    } catch {
        throw "$associationPath is not valid Java properties data: $($_.Exception.Message)"
    }
    Assert-Condition -Condition (
        $schema.type -eq "object" -and $schema.'$id' -eq
        "https://geocedg.local/schemas/package-profile-v1") `
        -Message "Package schema identity is invalid."
    Assert-Condition -Condition (
        $profile.schema_version -eq 1 -and
        $profile.profile_id -eq "geocedg-windows-internal" -and
        $profile.application.name -eq "GeoCeDG" -and
        $profile.application.main_class -eq "org.geocedg.desktop.GeoCeDG" -and
        $profile.toolchain.gradle_java -eq 22 -and
        $profile.toolchain.desktop_java -eq 25 -and
        $profile.toolchain.wix -eq $ExpectedWix) `
        -Message "Package profile identity or toolchain contract is invalid."
    Assert-Condition -Condition (
        @($profile.toolchain.wix_extensions).Count -eq 2 -and
        $profile.toolchain.wix_extensions[0] -eq
        "WixToolset.Util.wixext/5.0.2" -and
        $profile.toolchain.wix_extensions[1] -eq
        "WixToolset.UI.wixext/5.0.2") `
        -Message "Pinned WiX extension contract is invalid."
    Assert-Condition -Condition (
        @($profile.outputs).Count -eq 4 -and
        (@($profile.outputs) -join ",") -eq "app-image,zip,msi,exe") `
        -Message "Package profile must declare app-image, ZIP, MSI, and EXE."
    Assert-Condition -Condition (
        $profile.file_association.installers_only -and
        $profile.file_association.extension -ceq $ExpectedNativeExtension -and
        $profile.file_association.mime_type -ceq $ExpectedInternalMimeType -and
        $profile.file_association.mime_basis -ceq $ExpectedMimeBasis -and
        $profile.file_association.description -ceq
        $ExpectedAssociationDescription -and
        $profile.file_association.progid_strategy -ceq
        $ExpectedProgIdStrategy) `
        -Message "The native .cedg association profile is invalid."
    Assert-Condition -Condition (
        $schema.properties.file_association.properties.extension.const -ceq
        $ExpectedNativeExtension -and
        $schema.properties.file_association.properties.mime_type.const -ceq
        $ExpectedInternalMimeType -and
        $schema.properties.file_association.properties.mime_basis.const -ceq
        $ExpectedMimeBasis -and
        $schema.properties.file_association.properties.description.const -ceq
        $ExpectedAssociationDescription -and
        $schema.properties.file_association.properties.progid_strategy.const -ceq
        $ExpectedProgIdStrategy) `
        -Message "Package schema does not freeze the implemented internal association record."
    Assert-Condition -Condition (
        $association.Count -eq 3 -and
        $association["extension"] -ceq $ExpectedNativeExtension -and
        $association["mime-type"] -ceq $ExpectedInternalMimeType -and
        $association["description"] -ceq $ExpectedAssociationDescription) `
        -Message "jpackage association properties do not match the package profile."
    Assert-Condition -Condition (
        $association["extension"] -cne "ggb" -and
        $association["mime-type"] -cne $UpstreamGeoGebraMimeType) `
        -Message "GeoCeDG packaging must not claim .ggb or the upstream MIME identity."
    Assert-Condition -Condition (
        $profile.distribution.marker -ceq $ExpectedMarker -and
        $profile.distribution.public_redistribution -eq
        "blocked-pending-license-and-asset-approval") `
        -Message "Internal distribution status is not explicit."
    Assert-Condition -Condition (
        $assets.distribution_marker -ceq $ExpectedMarker -and
        $null -eq $assets.package_icon -and
        @($assets.deliberate_exclusions).Count -ge 5) `
        -Message "Asset manifest does not preserve the G4 exclusion boundary."
    foreach ($legalPath in @("LICENSE", "LICENSES\README.md", "NOTICE.md", "THIRD_PARTY.md")) {
        $content = Get-Content -Raw -LiteralPath (Join-Path $RepositoryRoot $legalPath)
        Assert-Condition -Condition $content.Contains($ExpectedMarker) `
            -Message "$legalPath lacks the internal-evaluation marker."
    }

    $buildScript = Get-Content -Raw -LiteralPath (
        Join-Path $RepositoryRoot "tools\release\build-windows-package.ps1")
    foreach ($requiredText in @(
            "org.geocedg.desktop.GeoCeDG",
            "--type", "app-image", "msi", "exe",
            "--file-associations",
            $ExpectedNativeExtension,
            $ExpectedInternalMimeType,
            $ExpectedMimeBasis,
            "geocedg-windows.cdx.json",
            "SHA256SUMS.txt",
            $ExpectedMarker)) {
        Assert-Condition -Condition $buildScript.Contains($requiredText) `
            -Message "Package builder is missing required contract '$requiredText'."
    }
    Assert-Condition -Condition (-not $buildScript.Contains(
        "source\desktop\desktop\build\scripts")) `
        -Message "Package builder must not consume the upstream Classic start scripts."
    $associationSwitch = '"--file-associations"'
    Assert-Condition -Condition (
        [regex]::Matches($buildScript, [regex]::Escape(
            $associationSwitch)).Count -eq 1 -and
        $buildScript.IndexOf($associationSwitch,
            [StringComparison]::Ordinal) -gt
        $buildScript.LastIndexOf('if ($requiresInstaller)',
            [StringComparison]::Ordinal)) `
        -Message "File association must be applied exactly once inside the MSI/EXE installer path."

    if ($RequireArtifacts) {
        $CheckToolchain = $true
    }
    if ($CheckToolchain) {
        Write-Step "G4 packaging toolchain"
        $jpackage = Resolve-Jpackage
        Assert-Condition -Condition (Test-Path -LiteralPath $jpackage -PathType Leaf) `
            -Message "Java 25 jpackage is missing: $jpackage"
        $jpackageVersion = @(Invoke-Captured -FilePath $jpackage `
            -ArgumentList @("--version") -Description "jpackage version")[-1].Trim()
        Assert-Condition -Condition ($jpackageVersion -match "^25(?:\.|$)") `
            -Message "jpackage 25 is required; found $jpackageVersion."

        $dotnet = Get-Command dotnet -ErrorAction SilentlyContinue
        Assert-Condition -Condition ($null -ne $dotnet) `
            -Message ".NET SDK 6+ is missing. Recommended: winget install --id Microsoft.DotNet.SDK.8 --exact"
        $sdks = Invoke-Captured -FilePath $dotnet.Source `
            -ArgumentList @("--list-sdks") -Description ".NET SDK inventory"
        Assert-Condition -Condition ($null -ne ($sdks | Where-Object {
            $_ -match "^(\d+)\." -and [int]$Matches[1] -ge 6
        } | Select-Object -First 1)) `
            -Message "A .NET SDK 6 or newer is required."

        $wix = Get-Command wix -ErrorAction SilentlyContinue
        Assert-Condition -Condition ($null -ne $wix) `
            -Message "WiX 5.0.2 is missing. Use the pinned dotnet tool command documented in README.md."
        $wixVersion = @(Invoke-Captured -FilePath $wix.Source `
            -ArgumentList @("--version") -Description "WiX version")[-1].Trim()
        Assert-Condition -Condition ($wixVersion.StartsWith(
            $ExpectedWix, [StringComparison]::Ordinal)) `
            -Message "WiX $ExpectedWix is required; found $wixVersion."
        $extensions = Invoke-Captured -FilePath $wix.Source `
            -ArgumentList @("extension", "list", "-g") `
            -Description "WiX global extension inventory"
        foreach ($extension in @("WixToolset.Util.wixext", "WixToolset.UI.wixext")) {
            Assert-Condition -Condition ($null -ne ($extensions | Where-Object {
                $_ -match "^$([regex]::Escape($extension))\s+$([regex]::Escape($ExpectedWix))$"
            } | Select-Object -First 1)) `
                -Message "WiX extension $extension/$ExpectedWix is missing."
        }
        Write-Host "jpackage: $jpackageVersion"
        Write-Host "WiX: $wixVersion"
    }

    if ($RequireArtifacts) {
        Write-Step "G4 generated package evidence"
        $appImage = Join-Path $ArtifactRoot "app-image\GeoCeDG"
        $launcher = Join-Path $appImage "GeoCeDG.exe"
        $markerPath = Join-Path $appImage "app\INTERNAL_EVALUATION_ONLY.txt"
        foreach ($path in @(
                $launcher,
                $markerPath,
                (Join-Path $ArtifactRoot "geocedg-windows.cdx.json"),
                (Join-Path $ArtifactRoot "build-manifest.json"),
                (Join-Path $ArtifactRoot "app-image.SHA256SUMS.txt"),
                (Join-Path $ArtifactRoot "SHA256SUMS.txt"))) {
            Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
                -Message "Required generated package evidence is missing: $path"
        }
        $zip = @(Get-ChildItem -LiteralPath $ArtifactRoot -File `
            -Filter "GeoCeDG-*-internal.zip")
        $msi = @(Get-ChildItem -LiteralPath (
            Join-Path $ArtifactRoot "packages") -File -Filter "GeoCeDG-*-internal.msi")
        $exe = @(Get-ChildItem -LiteralPath (
            Join-Path $ArtifactRoot "packages") -File -Filter "GeoCeDG-*-internal.exe")
        Assert-Condition -Condition (
            $zip.Count -eq 1 -and $msi.Count -eq 1 -and $exe.Count -eq 1) `
            -Message "A full build must contain exactly one internal ZIP, MSI, and EXE."
        $markerContent = Get-Content -Raw -LiteralPath $markerPath
        Assert-Condition -Condition $markerContent.Contains($ExpectedMarker) `
            -Message "Generated app-image marker is invalid."

        $forbiddenFiles = @(Get-ChildItem -LiteralPath $appImage -Recurse -File |
            Where-Object {
                $_.Extension -in @(".pdf", ".ggb", ".ggt") -or
                $_.Name -eq "Templatev7.ggb" -or
                $_.Name -match "(?i)-natives-(linux|macosx)-"
            })
        Assert-Condition -Condition ($forbiddenFiles.Count -eq 0) `
            -Message "Forbidden files are present in app-image: $(@($forbiddenFiles | ForEach-Object FullName) -join ', ')"
        $config = @(Get-ChildItem -LiteralPath (Join-Path $appImage "app") `
            -File -Filter "*.cfg") | Select-Object -First 1
        Assert-Condition -Condition ($null -ne $config) `
            -Message "jpackage application configuration is missing."
        $configText = Get-Content -Raw -LiteralPath $config.FullName
        Assert-Condition -Condition $configText.Contains(
            "org.geocedg.desktop.GeoCeDG") `
            -Message "Generated app-image does not select the GeoCeDG launcher."

        $sbom = Read-JsonFile -Path (
            Join-Path $ArtifactRoot "geocedg-windows.cdx.json")
        Assert-Condition -Condition (
            $sbom.bomFormat -eq "CycloneDX" -and
            $sbom.specVersion -eq "1.5" -and
            @($sbom.components).Count -gt 0) `
            -Message "Generated CycloneDX SBOM is invalid or empty."
        $buildManifest = Read-JsonFile -Path (
            Join-Path $ArtifactRoot "build-manifest.json")
        Assert-Condition -Condition (
            $buildManifest.target -eq "All" -and
            $buildManifest.distribution_marker -ceq $ExpectedMarker -and
            $buildManifest.public_redistribution -eq
            "BLOCKED PENDING LICENSE/ASSET APPROVAL" -and
            $buildManifest.file_association.enabled_for_target -eq $true -and
            $buildManifest.file_association.registration_scope -ceq
            "msi-exe-installers-only" -and
            $buildManifest.file_association.extension -ceq
            $ExpectedNativeExtension -and
            $buildManifest.file_association.mime_type -ceq
            $ExpectedInternalMimeType -and
            $buildManifest.file_association.mime_basis -ceq
            $ExpectedMimeBasis -and
            $buildManifest.file_association.progid_strategy -ceq
            $ExpectedProgIdStrategy -and
            $buildManifest.file_association.portable_outputs_association_free -eq
            $true -and
            $buildManifest.file_association.compatibility_extension_claimed -eq
            $false -and
            @($buildManifest.runtime.excluded_non_windows_native_jars).Count -gt 0) `
            -Message "Generated build manifest does not describe a full internal build."

        Assert-MsiNativeAssociation -MsiPath $msi[0].FullName `
            -WixPath $wix.Source -InspectionRoot (
                Join-Path $ArtifactRoot "verification")

        $hashFile = Join-Path $ArtifactRoot "SHA256SUMS.txt"
        foreach ($line in Get-Content -LiteralPath $hashFile) {
            Assert-Condition -Condition ($line -match "^([0-9a-f]{64})  (.+)$") `
                -Message "Malformed SHA256SUMS entry: $line"
            $expected = $Matches[1]
            $relative = $Matches[2].Replace("/", "\")
            $file = [IO.Path]::GetFullPath((Join-Path $ArtifactRoot $relative))
            Assert-Condition -Condition (Test-Path -LiteralPath $file -PathType Leaf) `
                -Message "Hashed artifact is missing: $relative"
            $actual = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()
            Assert-Condition -Condition ($actual -ceq $expected) `
                -Message "SHA-256 mismatch: $relative"
        }
    }

    Write-Host "Packaging verification passed."
    Write-Host "PACKAGING TECHNICAL STATUS = PASS"
    Write-Host "PUBLIC REDISTRIBUTION STATUS = BLOCKED PENDING LICENSE/ASSET APPROVAL"
} catch {
    $Failure = $_.Exception
} finally {
    try {
        if ($null -ne $GeneratedState) {
            Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedState `
                -Description "packaging-verifier output"
        }
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            throw "Repository status changed during packaging verification.`n" +
                "Before:`n$InitialStatus`nAfter:`n$finalStatus"
        }
    } catch {
        if ($null -eq $Failure) {
            $Failure = $_.Exception
        } else {
            $Failure = [Exception]::new(
                "$($Failure.Message)`nCleanup failure: $($_.Exception.Message)",
                $Failure)
        }
    }
}

if ($null -ne $Failure) {
    Write-Error $Failure.Message
    exit 1
}

exit 0

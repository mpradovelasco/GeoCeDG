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
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
if ([string]::IsNullOrWhiteSpace($ArtifactRoot)) {
    $ArtifactRoot = Join-Path $RepositoryRoot "artifacts\packaging\windows"
} else {
    $ArtifactRoot = [IO.Path]::GetFullPath($ArtifactRoot)
}

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

function Get-UntrackedOutputDirectories {
    $paths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase)
    $entries = @(& git -C $RepositoryRoot ls-files --others --directory `
        --exclude-standard)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate untracked output directories."
    }
    $entries += @(& git -C $RepositoryRoot ls-files --others --directory `
        --ignored --exclude-standard)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enumerate ignored output directories."
    }
    foreach ($entry in $entries) {
        if ([string]::IsNullOrWhiteSpace($entry)) {
            continue
        }
        $relative = $entry.TrimEnd("/", "\")
        if ($GeneratedDirectoryNames -contains (Split-Path -Leaf $relative)) {
            [void]$paths.Add([IO.Path]::GetFullPath(
                (Join-Path $RepositoryRoot $relative)))
        }
    }
    return ,$paths
}

function Remove-NewOutputDirectories {
    param(
        [Parameter(Mandatory)]
        [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$Before
    )

    $after = Get-UntrackedOutputDirectories
    $rootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
    $selected = [Collections.Generic.List[string]]::new()
    foreach ($candidate in @($after | Where-Object { -not $Before.Contains($_) } |
            Sort-Object Length)) {
        $covered = $false
        foreach ($parent in $selected) {
            if ($candidate.StartsWith(
                    $parent + [IO.Path]::DirectorySeparatorChar,
                    [StringComparison]::OrdinalIgnoreCase)) {
                $covered = $true
                break
            }
        }
        if (-not $covered) {
            $selected.Add($candidate)
        }
    }
    foreach ($candidate in $selected) {
        if (-not $candidate.StartsWith(
                $rootPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                $GeneratedDirectoryNames -notcontains (Split-Path -Leaf $candidate)) {
            throw "Refusing to remove unexpected output directory: $candidate"
        }
        if (Test-Path -LiteralPath $candidate) {
            Write-Host "Removing generated packaging-verifier output: $candidate"
            Remove-Item -LiteralPath $candidate -Recurse -Force
        }
    }
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

$InitialOutputs = Get-UntrackedOutputDirectories
[Exception]$Failure = $null

try {
    Write-Step "G4 durable package contracts"
    $requiredFiles = @(
        "docs\adr\0004-standalone-windows-packaging.md",
        "geocedg\specs\packaging\windows-packaging.md",
        "geocedg\specs\operations\package-profile.schema.json",
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
        $profile.file_association.extension -eq "ggb") `
        -Message "The .ggb association is not constrained to installers."
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
            "geocedg-windows.cdx.json",
            "SHA256SUMS.txt",
            $ExpectedMarker)) {
        Assert-Condition -Condition $buildScript.Contains($requiredText) `
            -Message "Package builder is missing required contract '$requiredText'."
    }
    Assert-Condition -Condition (-not $buildScript.Contains(
        "source\desktop\desktop\build\scripts")) `
        -Message "Package builder must not consume the upstream Classic start scripts."

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
            @($buildManifest.runtime.excluded_non_windows_native_jars).Count -gt 0) `
            -Message "Generated build manifest does not describe a full internal build."

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
        Remove-NewOutputDirectories -Before $InitialOutputs
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

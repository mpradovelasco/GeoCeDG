<#
.SYNOPSIS
Builds internal-evaluation Windows packages for GeoCeDG.

.DESCRIPTION
Uses the repository Gradle wrapper to produce the Desktop installDist layout,
filters it to Windows runtime JARs, and invokes JDK 25 jpackage. Generated
outputs are always marked INTERNAL EVALUATION — NOT FOR REDISTRIBUTION and are
written below the ignored artifacts/packaging/windows boundary.

.PARAMETER Target
AppImage, Zip, Msi, Exe, or All. Every target first creates an app-image.

.PARAMETER SkipInstallDist
Reuses an existing installDist lib directory. Intended only for a repeated
packaging pass after the same source revision was already built.

.PARAMETER JdkHome
Optional Java 25 home. By default the script discovers the Java 25 Desktop
toolchain reported by the repository Gradle wrapper.
#>
[CmdletBinding()]
param(
    [ValidateSet("AppImage", "Zip", "Msi", "Exe", "All")]
    [string]$Target = "AppImage",
    [ValidateSet("INTERNAL", "NC", "COMMERCIAL")]
    [string]$DistributionProfile,
    [switch]$SkipInstallDist,
    [string]$JdkHome
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$ArtifactRoot = Join-Path $RepositoryRoot "artifacts\packaging\windows"
$ExpectedArtifactRoot = [IO.Path]::GetFullPath($ArtifactRoot)
$ProfilePath = Join-Path $RepositoryRoot "packaging\windows\package.yml"
$AssetsManifestPath = Join-Path $RepositoryRoot `
    "geocedg\resources\assets-manifest.yml"
$AssociationPath = Join-Path $RepositoryRoot `
    "packaging\windows\file-associations.properties"
$NoticePath = Join-Path $RepositoryRoot `
    "packaging\windows\INTERNAL_EVALUATION_ONLY.txt"
$GradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
$ResolvedComponentsExporter = Join-Path $RepositoryRoot `
    "tools\release\export-runtime-components.init.gradle"
$ResolvedComponentsBuildPath = Join-Path $RepositoryRoot `
    "source\desktop\desktop\build\reports\geocedg\resolved-runtime-components.json"
$LicenseManifestPath = Join-Path $RepositoryRoot "LICENSES\manifest.json"
$ComponentAuditPath = Join-Path $RepositoryRoot `
    "geocedg\validation\pre-g9b-d1\component-audit.json"
$ComponentDispositionPath = Join-Path $RepositoryRoot `
    "geocedg\validation\pre-g9b-d1\component-disposition.json"
$SourceAccessPath = Join-Path $RepositoryRoot `
    "geocedg\resources\source-access-manifest.json"
$InternalMarker = "INTERNAL EVALUATION — NOT FOR REDISTRIBUTION"
# Resolved from the selected distribution profile once package.yml is parsed.
$ExpectedMarker = $InternalMarker
$ExpectedNativeExtension = "cedg"
$ExpectedInternalMimeType = "application/x-geocedg-cedg"
$ExpectedMimeBasis = "jdk25-jpackage-required-internal-unregistered"
$ExpectedAssociationDescription = "GeoCeDG document (internal evaluation)"
$ExpectedProgIdStrategy = "jdk25-jpackage-generated-geocedg-owned"
$UpstreamGeoGebraMimeType = "application/vnd.geogebra.file"

function Write-Step {
    param([Parameter(Mandatory)] [string]$Message)

    Write-Host "`n==> $Message"
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

function Invoke-Native {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$ArgumentList,
        [Parameter(Mandatory)] [string]$Description,
        [switch]$Capture
    )

    if ($Capture) {
        $output = @(& $FilePath @ArgumentList 2>&1)
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) {
            $rendered = @($output | ForEach-Object { $_.ToString() }) -join `
                [Environment]::NewLine
            throw "$Description failed with exit code $exitCode.$([Environment]::NewLine)$rendered"
        }
        return @($output | ForEach-Object { $_.ToString() })
    }

    & $FilePath @ArgumentList
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

function Resolve-JavaToolchainHome {
    param([int]$LanguageVersion)

    $output = Invoke-Native -FilePath $GradleWrapper -ArgumentList @(
        "-q", "javaToolchains", "--no-daemon", "--console=plain"
    ) -Description "Gradle Java toolchain discovery" -Capture

    for ($index = 0; $index -lt $output.Count; $index++) {
        if ($output[$index] -notmatch "Language Version:\s*$LanguageVersion\s*$") {
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
            return [IO.Path]::GetFullPath($Matches[1].Trim())
        }
    }
    throw "Gradle did not report a Java $LanguageVersion toolchain. Install the validated Desktop JDK and rerun."
}

function Test-ExcludedNative {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [object[]]$Patterns
    )

    foreach ($pattern in $Patterns) {
        if ($Name -match [string]$pattern) {
            return $true
        }
    }
    return $false
}

function New-NormalizedZip {
    param(
        [Parameter(Mandatory)] [string]$SourceDirectory,
        [Parameter(Mandatory)] [string]$DestinationPath
    )

    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $stream = [IO.File]::Open(
        $DestinationPath, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write,
        [IO.FileShare]::None)
    try {
        $archive = [IO.Compression.ZipArchive]::new(
            $stream, [IO.Compression.ZipArchiveMode]::Create, $false)
        try {
            $parent = Split-Path -Parent $SourceDirectory
            $files = Get-ChildItem -LiteralPath $SourceDirectory -Recurse -File |
                Sort-Object { [IO.Path]::GetRelativePath($parent, $_.FullName) }
            foreach ($file in $files) {
                $entryName = [IO.Path]::GetRelativePath(
                    $parent, $file.FullName).Replace("\", "/")
                $entry = $archive.CreateEntry(
                    $entryName, [IO.Compression.CompressionLevel]::Optimal)
                $entry.LastWriteTime = [DateTimeOffset]::new(
                    2000, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
                $input = [IO.File]::OpenRead($file.FullName)
                try {
                    $output = $entry.Open()
                    try {
                        $input.CopyTo($output)
                    } finally {
                        $output.Dispose()
                    }
                } finally {
                    $input.Dispose()
                }
            }
        } finally {
            $archive.Dispose()
        }
    } finally {
        $stream.Dispose()
    }
}

function Get-FileEvidence {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$RelativeTo
    )

    return [ordered]@{
        path = [IO.Path]::GetRelativePath($RelativeTo, $Path).Replace("\", "/")
        size = (Get-Item -LiteralPath $Path).Length
        sha256 = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}

function Write-JsonFile {
    param(
        [Parameter(Mandatory)] [object]$Value,
        [Parameter(Mandatory)] [string]$Path,
        [int]$Depth = 20
    )

    $json = $Value | ConvertTo-Json -Depth $Depth
    [IO.File]::WriteAllText(
        $Path, $json + "`n", [Text.UTF8Encoding]::new($false))
}

try {
    Assert-Condition -Condition $IsWindows `
        -Message "G4 package generation is validated on Windows x64 only."
    Assert-Condition -Condition (
        [Runtime.InteropServices.RuntimeInformation]::OSArchitecture -eq
        [Runtime.InteropServices.Architecture]::X64) `
        -Message "G4 package generation requires Windows x64."
    foreach ($required in @(
            $ProfilePath, $AssociationPath, $NoticePath, $GradleWrapper,
            (Join-Path $RepositoryRoot "LICENSE"),
            (Join-Path $RepositoryRoot "NOTICE.md"),
            (Join-Path $RepositoryRoot "THIRD_PARTY.md"),
            (Join-Path $RepositoryRoot "LICENSES\README.md"),
            $LicenseManifestPath, $ResolvedComponentsExporter,
            $ComponentAuditPath, $ComponentDispositionPath, $SourceAccessPath,
            $AssetsManifestPath)) {
        Assert-Condition -Condition (Test-Path -LiteralPath $required -PathType Leaf) `
            -Message "Required package input is missing: $required"
    }

    $profile = Get-Content -Raw -LiteralPath $ProfilePath |
        ConvertFrom-Json -Depth 50 -NoEnumerate
    $assets = Get-Content -Raw -LiteralPath $AssetsManifestPath |
        ConvertFrom-Json -Depth 50 -NoEnumerate
    $licenseManifest = Get-Content -Raw -LiteralPath $LicenseManifestPath |
        ConvertFrom-Json -Depth 50 -NoEnumerate
    $componentAudit = Get-Content -Raw -LiteralPath $ComponentAuditPath |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    $componentDisposition = Get-Content -Raw -LiteralPath $ComponentDispositionPath |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    $sourceAccess = Get-Content -Raw -LiteralPath $SourceAccessPath |
        ConvertFrom-Json -Depth 50 -NoEnumerate
    Assert-Condition -Condition (
        $licenseManifest.schema_version -eq 1 -and
        $assets.schema_version -eq 2 -and
        $componentDisposition.schema_version -eq 2 -and
        $sourceAccess.schema_version -eq 1) `
        -Message "D1 legal/provenance evidence schema differs from the package contract."
    $declaredLegalPaths = @($licenseManifest.entries | ForEach-Object {
        [string]$_.path
    } | Sort-Object)
    $actualLegalPaths = @(Get-ChildItem -LiteralPath (
            Join-Path $RepositoryRoot "LICENSES") -Recurse -File |
        Where-Object Name -cne "manifest.json" |
        ForEach-Object {
            [IO.Path]::GetRelativePath($RepositoryRoot, $_.FullName).Replace("\", "/")
        } | Sort-Object)
    Assert-Condition -Condition (
        ($declaredLegalPaths -join "`n") -ceq ($actualLegalPaths -join "`n")) `
        -Message "LICENSES/manifest.json does not enumerate every bundled legal file exactly once."
    foreach ($entry in @($licenseManifest.entries)) {
        $entryPath = Join-Path $RepositoryRoot ([string]$entry.path).Replace("/", "\")
        $entryHash = (Get-FileHash -LiteralPath $entryPath -Algorithm SHA256).Hash.ToLowerInvariant()
        Assert-Condition -Condition ($entryHash -ceq [string]$entry.sha256) `
            -Message "Legal bundle hash mismatch: $($entry.path)"
    }
    $packageIconPath = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        ([string]$profile.application.icon).Replace("/", "\")))
    Assert-Condition -Condition (Test-Path -LiteralPath $packageIconPath -PathType Leaf) `
        -Message "Declared GeoCeDG package icon is missing: $packageIconPath"
    Assert-Condition -Condition (
        $assets.package_icon.path -ceq [string]$profile.application.icon -and
        $assets.package_icon.raw_sha256 -ceq
        (Get-FileHash -LiteralPath $packageIconPath -Algorithm SHA256).Hash.ToLowerInvariant()) `
        -Message "Package icon path/hash differs from the asset provenance authority."
    # Packaging consumes the tracked, hash-pinned derivative. Regeneration is a
    # separate provenance operation bound to the exact runtime recorded in the
    # asset manifest; it is not a workstation packaging prerequisite.
    Assert-Condition -Condition ($profile.schema_version -eq 1) `
        -Message "Unsupported package profile schema version."
    Assert-Condition -Condition ($profile.profile_id -eq "geocedg-windows-internal") `
        -Message "Unexpected package profile identity."

    # PRE-G9B-P1 distribution profile selection. The repository default stays
    # INTERNAL; a profile is never inferred from the environment.
    $requestedProfileId = if ([string]::IsNullOrWhiteSpace($DistributionProfile)) {
        [string]$profile.distribution.default_profile
    } else {
        $DistributionProfile
    }
    Assert-Condition -Condition (
        [string]$profile.distribution.default_profile -ceq "INTERNAL") `
        -Message "The package profile default distribution must remain INTERNAL."
    $selectedProfiles = @($profile.distribution.profiles |
        Where-Object { [string]$_.id -ceq $requestedProfileId })
    Assert-Condition -Condition ($selectedProfiles.Count -eq 1) `
        -Message "Unknown distribution profile: $requestedProfileId"
    $selected = $selectedProfiles[0]

    # Fail closed on any profile that is not an approved redistributable or the
    # internal-evaluation default. COMMERCIAL must name its pending terms.
    if (-not [bool]$selected.redistributable -and
            [string]$selected.id -cne "INTERNAL") {
        $blocking = @($selected.blocking_terms) -join "; "
        throw ("Distribution profile $($selected.id) is not authorized. " +
            "Status: $($selected.status). Pending external terms: $blocking")
    }
    if ([bool]$selected.redistributable) {
        Assert-Condition -Condition (
            -not [string]::IsNullOrWhiteSpace([string]$selected.licensing_profile) -and
            -not [string]::IsNullOrWhiteSpace([string]$selected.licensing_authority) -and
            -not [string]::IsNullOrWhiteSpace([string]$selected.notice_file) -and
            -not [string]::IsNullOrWhiteSpace(
                [string]$selected.association_properties)) `
            -Message "A redistributable profile must declare its legal authority and assets."
    }

    $ArtifactTag = [string]$selected.artifact_tag
    Assert-Condition -Condition ($ArtifactTag -cmatch '^[a-z0-9]+$') `
        -Message "The distribution profile must declare a simple artifact tag."
    $ExpectedMarker = [string]$selected.marker
    Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace($ExpectedMarker)) `
        -Message "The distribution profile must declare a distribution marker."
    if (-not [string]::IsNullOrWhiteSpace([string]$selected.notice_file)) {
        $NoticePath = [IO.Path]::GetFullPath(
            (Join-Path $RepositoryRoot ([string]$selected.notice_file)))
    }
    if (-not [string]::IsNullOrWhiteSpace(
            [string]$selected.association_properties)) {
        $AssociationPath = [IO.Path]::GetFullPath(
            (Join-Path $RepositoryRoot ([string]$selected.association_properties)))
    }
    $SelectedAssociationDescription = if (
            [string]::IsNullOrWhiteSpace([string]$selected.association_description)) {
        $ExpectedAssociationDescription
    } else {
        [string]$selected.association_description
    }
    Assert-Condition -Condition (Test-Path -LiteralPath $NoticePath -PathType Leaf) `
        -Message "The distribution notice for $($selected.id) is missing."
    Assert-Condition -Condition (
        Test-Path -LiteralPath $AssociationPath -PathType Leaf) `
        -Message "The association properties for $($selected.id) are missing."
    # A redistributable profile must not ship the internal-evaluation marker.
    $noticeText = Get-Content -Raw -LiteralPath $NoticePath
    if ([bool]$selected.redistributable) {
        Assert-Condition -Condition (-not $noticeText.Contains($InternalMarker)) `
            -Message "A redistributable notice must not carry the internal-evaluation marker."
    } else {
        Assert-Condition -Condition ($noticeText.Contains($ExpectedMarker)) `
            -Message "The non-redistributable notice must carry its distribution marker."
    }

    try {
        $association = Get-Content -Raw -LiteralPath $AssociationPath |
            ConvertFrom-StringData
    } catch {
        throw "File-association properties are invalid: $($_.Exception.Message)"
    }
    Assert-Condition -Condition ($profile.distribution.marker -ceq $InternalMarker) `
        -Message "Package profile does not contain the required distribution marker."
    Assert-Condition -Condition (
        $profile.distribution.public_redistribution -eq
        "blocked-pending-license-and-asset-approval") `
        -Message "Public redistribution must remain explicitly blocked."
    Assert-Condition -Condition ($profile.application.main_class -eq
        "org.geocedg.desktop.GeoCeDG") `
        -Message "Package entry point must be the G2 GeoCeDG launcher."
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
        $association.Count -eq 3 -and
        $association["extension"] -ceq $ExpectedNativeExtension -and
        $association["mime-type"] -ceq $ExpectedInternalMimeType -and
        $association["description"] -ceq $SelectedAssociationDescription) `
        -Message "jpackage association properties do not match the selected profile."
    Assert-Condition -Condition (
        $association["extension"] -cne "ggb" -and
        $association["mime-type"] -cne $UpstreamGeoGebraMimeType) `
        -Message "GeoCeDG installers must not claim the .ggb extension or upstream MIME identity."

    Write-Host $ExpectedMarker
    Write-Host "Distribution profile: $($selected.id) ($($selected.status))"
    Write-Host "Target: $Target"

    Write-Step "Desktop distribution input"
    $installDist = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        ([string]$profile.input.install_dist).Replace("/", "\")))
    if (-not $SkipInstallDist) {
        Invoke-Native -FilePath $GradleWrapper -ArgumentList @(
            [string]$profile.input.gradle_task,
            ":desktop:desktop:exportGeoCeDGRuntimeComponents",
            "-I", $ResolvedComponentsExporter,
            "-PgeocedgRuntimeComponentsOutput=$ResolvedComponentsBuildPath",
            "--rerun-tasks",
            "--no-build-cache",
            "--no-daemon",
            "--no-problems-report",
            "--console=plain"
        ) -Description "GeoCeDG Desktop installDist"
    } else {
        Invoke-Native -FilePath $GradleWrapper -ArgumentList @(
            ":desktop:desktop:exportGeoCeDGRuntimeComponents",
            "-I", $ResolvedComponentsExporter,
            "-PgeocedgRuntimeComponentsOutput=$ResolvedComponentsBuildPath",
            "--no-daemon", "--no-problems-report", "--console=plain"
        ) -Description "GeoCeDG resolved runtime component export"
    }
    Assert-Condition -Condition (Test-Path -LiteralPath $installDist -PathType Container) `
        -Message "installDist lib directory is missing: $installDist"
    Assert-Condition -Condition (Test-Path -LiteralPath $ResolvedComponentsBuildPath -PathType Leaf) `
        -Message "Resolved Gradle component evidence is missing: $ResolvedComponentsBuildPath"
    $resolvedComponents = Get-Content -Raw -LiteralPath $ResolvedComponentsBuildPath |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    Assert-Condition -Condition (
        $resolvedComponents.evidence_kind -ceq
        "GEOCEDG_RESOLVED_GRADLE_RUNTIME_COMPONENTS" -and
        @($resolvedComponents.components).Count -gt 0) `
        -Message "Resolved Gradle component evidence is invalid or empty."

    Write-Step "Java packaging toolchain"
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        $JdkHome = Resolve-JavaToolchainHome -LanguageVersion `
            ([int]$profile.toolchain.desktop_java)
    } else {
        $JdkHome = [IO.Path]::GetFullPath($JdkHome)
    }
    $jpackage = Join-Path $JdkHome "bin\jpackage.exe"
    $java = Join-Path $JdkHome "bin\java.exe"
    Assert-Condition -Condition (Test-Path -LiteralPath $jpackage -PathType Leaf) `
        -Message "jpackage is missing from the Java 25 toolchain: $jpackage"
    Assert-Condition -Condition (Test-Path -LiteralPath $java -PathType Leaf) `
        -Message "java is missing from the packaging toolchain: $java"
    $jpackageVersion = @(Invoke-Native -FilePath $jpackage -ArgumentList @("--version") `
        -Description "jpackage version" -Capture)[-1].Trim()
    $javaVersion = (Invoke-Native -FilePath $java -ArgumentList @("-version") `
        -Description "packaging Java version" -Capture) -join " | "
    Assert-Condition -Condition ($jpackageVersion -match "^25(?:\.|$)") `
        -Message "The validated package pipeline requires jpackage 25; found $jpackageVersion."
    Write-Host "Java home: $JdkHome"
    Write-Host "jpackage: $jpackageVersion"

    $requiresInstaller = $Target -in @("Msi", "Exe", "All")
    $dotnetVersion = $null
    $wixVersion = $null
    if ($requiresInstaller) {
        $dotnetCommand = Get-Command dotnet -ErrorAction SilentlyContinue
        Assert-Condition -Condition ($null -ne $dotnetCommand) `
            -Message "MSI/EXE require .NET SDK 6+ and WiX 5.0.2. Run tools/bootstrap/bootstrap-windows.ps1 -InstallPackagingPrerequisites."
        $sdkLines = Invoke-Native -FilePath $dotnetCommand.Source `
            -ArgumentList @("--list-sdks") -Description ".NET SDK inventory" -Capture
        $compatibleSdk = $sdkLines | Where-Object {
            $_ -match "^(\d+)\." -and [int]$Matches[1] -ge 6
        } | Select-Object -First 1
        Assert-Condition -Condition ($null -ne $compatibleSdk) `
            -Message "MSI/EXE require a .NET SDK 6 or newer. Recommended: winget install --id Microsoft.DotNet.SDK.8 --exact"
        $dotnetVersion = @(Invoke-Native -FilePath $dotnetCommand.Source `
            -ArgumentList @("--version") -Description ".NET SDK version" -Capture)[-1].Trim()

        $wixCommand = Get-Command wix -ErrorAction SilentlyContinue
        Assert-Condition -Condition ($null -ne $wixCommand) `
            -Message "WiX is missing. Run: dotnet tool install --global wix --version 5.0.2 --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources"
        $wixVersion = @(Invoke-Native -FilePath $wixCommand.Source `
            -ArgumentList @("--version") -Description "WiX version" -Capture)[-1].Trim()
        Assert-Condition -Condition ($wixVersion.StartsWith(
            [string]$profile.toolchain.wix, [StringComparison]::Ordinal)) `
            -Message "WiX $($profile.toolchain.wix) is required; found $wixVersion."
        $wixExtensions = Invoke-Native -FilePath $wixCommand.Source `
            -ArgumentList @("extension", "list", "-g") `
            -Description "WiX global extension inventory" -Capture
        foreach ($extension in @("WixToolset.Util.wixext", "WixToolset.UI.wixext")) {
            Assert-Condition -Condition ($null -ne ($wixExtensions | Where-Object {
                $_ -match "^$([regex]::Escape($extension))\s+$([regex]::Escape([string]$profile.toolchain.wix))$"
            } | Select-Object -First 1)) `
                -Message "WiX extension $extension/$($profile.toolchain.wix) is missing. Run the pinned wix extension add commands from packaging/windows."
        }
        Write-Host ".NET SDK: $dotnetVersion"
        Write-Host "WiX: $wixVersion"
    }

    Write-Step "Isolated Windows staging"
    Assert-Condition -Condition (
        [IO.Path]::GetFullPath($ArtifactRoot) -eq $ExpectedArtifactRoot -and
        $ExpectedArtifactRoot.StartsWith(
            [IO.Path]::GetFullPath((Join-Path $RepositoryRoot "artifacts")) +
            [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Refusing to clear an unexpected artifact root: $ArtifactRoot"
    if (Test-Path -LiteralPath $ArtifactRoot) {
        Remove-Item -LiteralPath $ArtifactRoot -Recurse -Force
    }
    $workRoot = Join-Path $ArtifactRoot "work"
    $inputRoot = Join-Path $workRoot "input"
    $appImageParent = Join-Path $ArtifactRoot "app-image"
    $packageRoot = Join-Path $ArtifactRoot "packages"
    [void](New-Item -ItemType Directory -Path $inputRoot -Force)
    [void](New-Item -ItemType Directory -Path $appImageParent -Force)

    $excluded = [Collections.Generic.List[string]]::new()
    $included = [Collections.Generic.List[IO.FileInfo]]::new()
    $patterns = @($profile.input.excluded_native_patterns)
    foreach ($jar in Get-ChildItem -LiteralPath $installDist -File -Filter "*.jar" |
            Sort-Object Name) {
        if (Test-ExcludedNative -Name $jar.Name -Patterns $patterns) {
            $excluded.Add($jar.Name)
            continue
        }
        Copy-Item -LiteralPath $jar.FullName -Destination $inputRoot
        $included.Add((Get-Item -LiteralPath (Join-Path $inputRoot $jar.Name)))
    }
    Assert-Condition -Condition (Test-Path -LiteralPath (
        Join-Path $inputRoot ([string]$profile.application.main_jar)) -PathType Leaf) `
        -Message "The staged input does not contain the declared main JAR."
    Assert-Condition -Condition ($excluded.Count -gt 0) `
        -Message "No non-Windows native JAR was excluded; dependency naming may have changed."

    Copy-Item -LiteralPath $NoticePath -Destination $inputRoot
    $legalRoot = Join-Path $inputRoot "legal"
    [void](New-Item -ItemType Directory -Path $legalRoot -Force)
    foreach ($relative in @("LICENSE", "NOTICE.md", "THIRD_PARTY.md")) {
        Copy-Item -LiteralPath (Join-Path $RepositoryRoot $relative) `
            -Destination $legalRoot
    }
    Copy-Item -LiteralPath (Join-Path $RepositoryRoot "LICENSES") `
        -Destination $legalRoot -Recurse
    Copy-Item -LiteralPath (
        Join-Path $RepositoryRoot "geocedg\resources\assets-manifest.yml") `
        -Destination $legalRoot
    Copy-Item -LiteralPath $SourceAccessPath -Destination $legalRoot
    Copy-Item -LiteralPath $ComponentAuditPath -Destination $legalRoot
    Copy-Item -LiteralPath $ComponentDispositionPath -Destination $legalRoot
    Copy-Item -LiteralPath $ResolvedComponentsBuildPath `
        -Destination (Join-Path $legalRoot "resolved-runtime-components.json")
    Write-Host "Included runtime JARs: $($included.Count)"
    Write-Host "Excluded non-Windows native JARs: $($excluded.Count)"

    Write-Step "jpackage app-image"
    $jpackageArguments = [Collections.Generic.List[string]]::new()
    foreach ($value in @(
            "--type", "app-image",
            "--dest", $appImageParent,
            "--name", [string]$profile.application.name,
            "--input", $inputRoot,
            "--main-jar", [string]$profile.application.main_jar,
            "--main-class", [string]$profile.application.main_class,
            "--icon", $packageIconPath,
            "--app-version", [string]$profile.application.version,
            "--vendor", [string]$profile.application.vendor,
            "--description", $ExpectedMarker,
            "--copyright", $ExpectedMarker)) {
        $jpackageArguments.Add($value)
    }
    foreach ($option in @($profile.application.jvm_options)) {
        $jpackageArguments.Add("--java-options")
        $jpackageArguments.Add([string]$option)
    }
    Invoke-Native -FilePath $jpackage -ArgumentList $jpackageArguments.ToArray() `
        -Description "jpackage app-image"
    $appImage = Join-Path $appImageParent ([string]$profile.application.name)
    $appLauncher = Join-Path $appImage "$($profile.application.name).exe"
    Assert-Condition -Condition (Test-Path -LiteralPath $appLauncher -PathType Leaf) `
        -Message "jpackage did not create the GeoCeDG launcher."
    $stagedNoticeName = [IO.Path]::GetFileName($NoticePath)
    Assert-Condition -Condition (Test-Path -LiteralPath (
        Join-Path $appImage "app\$stagedNoticeName") -PathType Leaf) `
        -Message "The app-image does not contain the $($selected.id) distribution notice."

    $artifactFiles = [Collections.Generic.List[string]]::new()
    if ($Target -in @("Zip", "All")) {
        Write-Step "Normalized portable ZIP"
        $zipName = "GeoCeDG-$($profile.application.version)-windows-x64-$ArtifactTag.zip"
        $zipPath = Join-Path $ArtifactRoot $zipName
        New-NormalizedZip -SourceDirectory $appImage -DestinationPath $zipPath
        $artifactFiles.Add($zipPath)
    }

    if ($requiresInstaller) {
        [void](New-Item -ItemType Directory -Path $packageRoot -Force)
        $installerTypes = if ($Target -eq "All") { @("msi", "exe") } else {
            @($Target.ToLowerInvariant())
        }
        foreach ($type in $installerTypes) {
            Write-Step "jpackage $($type.ToUpperInvariant()) installer"
            $installerArgs = @(
                "--type", $type,
                "--dest", $packageRoot,
                "--app-image", $appImage,
                "--name", [string]$profile.application.name,
                "--app-version", [string]$profile.application.version,
                "--vendor", [string]$profile.application.vendor,
                "--description", $ExpectedMarker,
                "--copyright", $ExpectedMarker,
                "--file-associations", $AssociationPath,
                "--win-per-user-install",
                "--win-menu",
                "--win-shortcut",
                "--win-dir-chooser",
                "--win-upgrade-uuid", [string]$profile.application.upgrade_uuid
            )
            Invoke-Native -FilePath $jpackage -ArgumentList $installerArgs `
                -Description "jpackage $type installer"
            $after = @(Get-ChildItem -LiteralPath $packageRoot -File `
                -Filter "*.$type" | Sort-Object LastWriteTimeUtc -Descending)
            $created = $after | Select-Object -First 1
            Assert-Condition -Condition ($null -ne $created) `
                -Message "jpackage did not create a .$type installer."
            $internalName = "GeoCeDG-$($profile.application.version)-windows-x64-$ArtifactTag.$type"
            $internalPath = Join-Path $packageRoot $internalName
            Move-Item -LiteralPath $created.FullName -Destination $internalPath
            $artifactFiles.Add($internalPath)
        }
    }

    Write-Step "SBOM, composition manifest, and hashes"
    $sourceRevision = @(Invoke-Native -FilePath "git" -ArgumentList @(
        "-C", $RepositoryRoot, "rev-parse", "HEAD"
    ) -Description "source revision" -Capture)[-1].Trim()
    $sourceTimestamp = @(Invoke-Native -FilePath "git" -ArgumentList @(
        "-C", $RepositoryRoot, "show", "-s", "--format=%cI", "HEAD"
    ) -Description "source timestamp" -Capture)[-1].Trim()
    $auditJarByHash = @{}
    foreach ($row in @($componentAudit.runtime_jars)) {
        $auditJarByHash[[string]$row.actual_distribution_evidence.sha256] = $row
    }
    $dependencyDispositionById = @{}
    foreach ($row in @($componentDisposition.dependency_dispositions)) {
        foreach ($componentId in @($row.component_ids)) {
            $dependencyDispositionById[[string]$componentId] = $row
        }
    }
    $fontDispositionById = @{}
    foreach ($row in @($componentDisposition.font_disposition_groups)) {
        foreach ($componentId in @($row.component_ids)) {
            $fontDispositionById[[string]$componentId] = $row
        }
    }

    $jarComponents = @($included | Sort-Object Name | ForEach-Object {
        $staged = $_
        $hash = (Get-FileHash -LiteralPath $staged.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        $resolvedMatches = @($resolvedComponents.components | Where-Object {
            [string]$_.sha256 -ceq $hash
        })
        $isMainJar = $staged.Name -ceq [string]$profile.application.main_jar
        Assert-Condition -Condition (
            ($isMainJar -and $resolvedMatches.Count -eq 0) -or
            (-not $isMainJar -and $resolvedMatches.Count -eq 1)) `
            -Message "Staged JAR does not join uniquely to resolved Gradle evidence: $($staged.Name) / $hash"

        $auditRow = $auditJarByHash[$hash]
        if (-not $isMainJar -and $resolvedMatches[0].component_type -ceq "module") {
            Assert-Condition -Condition ($null -ne $auditRow) `
                -Message "External resolved artifact is absent from immutable D1 audit: $($staged.Name) / $hash"
        }
        $resolved = if ($isMainJar) { $null } else { $resolvedMatches[0] }
        $componentId = if ($null -eq $auditRow) { $null } else {
            [string]$auditRow.component_id
        }
        $disposition = if ([string]::IsNullOrWhiteSpace($componentId)) {
            $null
        } else { $dependencyDispositionById[$componentId] }
        $componentName = if ($isMainJar) {
            "GeoCeDG Desktop"
        } elseif ($resolved.component_type -ceq "module") {
            [string]$resolved.module
        } else { [string]$resolved.project_name }
        $componentVersion = if ($isMainJar -or
                $resolved.component_type -ceq "project") {
            $sourceRevision
        } else { [string]$resolved.version }
        $resolvedPurl = if ($null -ne $resolved -and
                $resolved.PSObject.Properties.Name -contains "purl") {
            [string]$resolved.purl
        } else { $null }
        $bomRef = if ($isMainJar) {
            "pkg:generic/geocedg/desktop@${sourceRevision}?sha256=$hash"
        } elseif (-not [string]::IsNullOrWhiteSpace($resolvedPurl)) {
            "$resolvedPurl`?sha256=$hash"
        } else {
            "pkg:generic/geocedg/$componentName@${sourceRevision}?sha256=$hash"
        }
        $properties = [Collections.Generic.List[object]]::new()
        $properties.Add([ordered]@{
            name = "geocedg.packaging.path"; value = "app/$($staged.Name)"
        })
        $properties.Add([ordered]@{
            name = "geocedg.gradle.component.type"
            value = $(if ($isMainJar) { "project-main" } else {
                [string]$resolved.component_type
            })
        })
        if (-not $isMainJar) {
            $properties.Add([ordered]@{
                name = "geocedg.gradle.artifact"; value = [string]$resolved.artifact_name
            })
            $properties.Add([ordered]@{
                name = "geocedg.gradle.variant"; value = [string]$resolved.variant
            })
            if ($resolved.component_type -ceq "project") {
                $properties.Add([ordered]@{
                    name = "geocedg.gradle.project.path"
                    value = [string]$resolved.component_display_name
                })
            }
        }
        if ($null -ne $auditRow) {
            $licenseTerms = if ($null -ne $disposition -and
                    -not [string]::IsNullOrWhiteSpace([string]$disposition.license)) {
                [string]$disposition.license
            } else { [string]$auditRow.identified_terms }
            $properties.Add([ordered]@{
                name = "geocedg.audit.component-id"; value = $componentId
            })
            $properties.Add([ordered]@{
                name = "geocedg.license.terms"; value = $licenseTerms
            })
            $properties.Add([ordered]@{
                name = "geocedg.license.disposition"
                value = $(if ($null -eq $disposition) {
                    [string]$auditRow.disposition_candidate
                } else { [string]$disposition.resolution_class })
            })
            if ($null -ne $disposition) {
                $properties.Add([ordered]@{
                    name = "geocedg.source.upstream"
                    value = [string]$disposition.primary_upstream
                })
                $properties.Add([ordered]@{
                    name = "geocedg.source.access"
                    value = [string]$disposition.source_code_obligation
                })
            }
        }
        if ($componentName -in @("flatlaf", "jna", "javagiac")) {
            $properties.Add([ordered]@{
                name = "geocedg.nested-native.relationship"
                value = "native payloads embedded in this exact JAR; see component disposition"
            })
        }
        $component = [ordered]@{
            type = "library"
            name = $componentName
            version = $componentVersion
            "bom-ref" = $bomRef
            hashes = @([ordered]@{ alg = "SHA-256"; content = $hash })
            properties = @($properties)
        }
        if (-not $isMainJar -and
                -not [string]::IsNullOrWhiteSpace($resolvedPurl)) {
            $component.purl = $resolvedPurl
        }
        if ($null -ne $auditRow -and
                -not [string]::IsNullOrWhiteSpace($licenseTerms)) {
            $component.licenses = @([ordered]@{
                license = [ordered]@{ name = $licenseTerms }
            })
        }
        $component
    })
    Assert-Condition -Condition (
        $jarComponents.Count -eq 51 -and
        @($resolvedComponents.components | Where-Object {
            $_.component_type -ceq "module" -and
            $_.group -ceq "netscape.javascript" -and $_.module -ceq "jsobject"
        }).Count -eq 0 -and
        @($included | Where-Object Name -ceq "jsobject-1.jar").Count -eq 0) `
        -Message "The D1 staged JAR closure must contain 51 JARs and exclude jsobject."

    $fontComponents = @($componentAudit.fonts | Sort-Object name | ForEach-Object {
        $font = $_
        $fontDisposition = $fontDispositionById[[string]$font.component_id]
        Assert-Condition -Condition ($null -ne $fontDisposition) `
            -Message "Font has no D1 disposition: $($font.component_id)"
        $fontComponent = [ordered]@{
            type = "file"
            name = [string]$font.name
            "bom-ref" = "font:$($font.name):$($font.sha256)"
            hashes = @([ordered]@{
                alg = "SHA-256"; content = [string]$font.sha256
            })
            licenses = @([ordered]@{
                license = [ordered]@{ name = [string]$fontDisposition.license }
            })
            properties = @(
                [ordered]@{ name = "geocedg.packaging.path"; value = [string]$font.package_path },
                [ordered]@{ name = "geocedg.audit.component-id"; value = [string]$font.component_id },
                [ordered]@{ name = "geocedg.source.upstream"; value = [string]$fontDisposition.primary_source },
                [ordered]@{ name = "geocedg.license.disposition"; value = $(
                    if ($fontDisposition.PSObject.Properties.Name -contains "resolution_class") {
                        [string]$fontDisposition.resolution_class
                    } else { [string]$fontDisposition.disposition }) }
            )
        }
        if ($fontDisposition.PSObject.Properties.Name -contains
                "upstream_version" -and -not [string]::IsNullOrWhiteSpace(
                    [string]$fontDisposition.upstream_version)) {
            $fontComponent.version = [string]$fontDisposition.upstream_version
        }
        $fontComponent
    })

    $assetComponents = [Collections.Generic.List[object]]::new()
    foreach ($brand in @($assets.branding_assets)) {
        foreach ($asset in @($brand.promoted_source) + @($brand.derivatives)) {
            $assetVersion = "v$($brand.resource_version)"
            $assetId = if ($asset.PSObject.Properties.Name -contains "id") {
                [string]$asset.id
            } else { "$($brand.id).source-$assetVersion" }
            $resourcePath = [string]$asset.path
            $embeddedPath = $resourcePath -replace
                '^source/desktop/desktop/src/main/resources/',
                'app/desktop.jar!/'
            $assetComponents.Add([ordered]@{
                type = "file"
                name = $assetId
                version = $assetVersion
                "bom-ref" = "asset:$assetId`:$($asset.raw_sha256)"
                hashes = @([ordered]@{
                    alg = "SHA-256"; content = [string]$asset.raw_sha256
                })
                licenses = @([ordered]@{
                    license = [ordered]@{ name = "CC-BY-4.0 plus adopted separate GeoCeDG brand policy" }
                })
                properties = @(
                    [ordered]@{ name = "geocedg.packaging.path"; value = $embeddedPath },
                    [ordered]@{ name = "geocedg.asset.semantic-role"; value = [string]$brand.semantic_role },
                    [ordered]@{ name = "geocedg.asset.trademark-role"; value = [string]$brand.trademark_role }
                )
            })
        }
    }

    $runtimeComponent = [ordered]@{
        type = "framework"
        name = "Eclipse Temurin OpenJDK runtime"
        version = "25.0.4+7-LTS"
        "bom-ref" = "pkg:generic/eclipse-temurin@25.0.4%2B7?arch=x86_64&os=windows"
        licenses = @([ordered]@{
            license = [ordered]@{ name = "GPL-2.0-only with Classpath Exception and module-specific terms" }
        })
        properties = @(
            [ordered]@{ name = "geocedg.packaging.path"; value = "runtime/**" },
            [ordered]@{ name = "geocedg.runtime.vendor"; value = "Eclipse Adoptium" },
            [ordered]@{ name = "geocedg.runtime.release-asset.sha256"; value = "7caab7db43bf4b94a2e6252c699e70d90084f9aa7c943cd3414761fd540937ae" },
            [ordered]@{ name = "geocedg.runtime.legal"; value = "runtime/legal/** (195 files / 52 module directories in audited runtime)" }
        )
    }
    $components = [Collections.Generic.List[object]]::new()
    foreach ($component in $jarComponents) { $components.Add($component) }
    foreach ($component in $fontComponents) { $components.Add($component) }
    foreach ($component in $assetComponents) { $components.Add($component) }
    $components.Add($runtimeComponent)
    if ($Target -in @("Msi", "Exe", "All")) {
        $components.Add([ordered]@{
            type = "library"
            name = "WiX embedded MSI custom-action/UI payload"
            version = "5.0.2+aa65968c"
            "bom-ref" = "pkg:github/wixtoolset/wix@v5.0.2#embedded-msi-payload"
            licenses = @([ordered]@{
                license = [ordered]@{ name = "Microsoft Reciprocal License (MS-RL)" }
            })
            properties = @(
                [ordered]@{ name = "geocedg.packaging.path"; value = "MSI/EXE embedded Wix4UtilCA_X64 and WixUiCa_X64/UI resources" },
                [ordered]@{ name = "geocedg.tool.boundary"; value = "WiX CLI/build tools not shipped wholesale" }
            )
        })
    }
    $sbom = [ordered]@{
        bomFormat = "CycloneDX"
        specVersion = "1.5"
        version = 1
        metadata = [ordered]@{
            timestamp = $sourceTimestamp
            component = [ordered]@{
                type = "application"
                name = [string]$profile.application.name
                version = [string]$profile.application.version
                properties = @(
                    [ordered]@{ name = "geocedg.source.revision"; value = $sourceRevision },
                    [ordered]@{ name = "geocedg.distribution.status"; value = $ExpectedMarker }
                )
            }
            tools = [ordered]@{
                components = @([ordered]@{
                    type = "application"
                    name = "jpackage"
                    version = $jpackageVersion
                })
            }
        }
        components = @($components)
    }
    $sbomPath = Join-Path $ArtifactRoot "geocedg-windows.cdx.json"
    Write-JsonFile -Value $sbom -Path $sbomPath
    $resolvedEvidencePath = Join-Path $ArtifactRoot "resolved-runtime-components.json"
    Copy-Item -LiteralPath $ResolvedComponentsBuildPath `
        -Destination $resolvedEvidencePath

    $appFileHashPath = Join-Path $ArtifactRoot "app-image.SHA256SUMS.txt"
    $appHashLines = @(Get-ChildItem -LiteralPath $appImage -Recurse -File |
        Sort-Object { [IO.Path]::GetRelativePath($appImage, $_.FullName) } |
        ForEach-Object {
            $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            $relative = [IO.Path]::GetRelativePath(
                $appImage, $_.FullName).Replace("\", "/")
            "$hash  $relative"
        })
    [IO.File]::WriteAllLines(
        $appFileHashPath, $appHashLines, [Text.UTF8Encoding]::new($false))

    $packageEvidence = @($artifactFiles | ForEach-Object {
        Get-FileEvidence -Path $_ -RelativeTo $ArtifactRoot
    })
    $manifest = [ordered]@{
        schema_version = 2
        profile = "packaging/windows/package.yml"
        source_revision = $sourceRevision
        source_timestamp = $sourceTimestamp
        baseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
        target = $Target
        platform = "windows-x64"
        distribution_marker = $ExpectedMarker
        distribution_profile = [ordered]@{
            id = [string]$selected.id
            status = [string]$selected.status
            redistributable = [bool]$selected.redistributable
            artifact_tag = $ArtifactTag
            repository_default = [string]$profile.distribution.default_profile
        }
        public_redistribution = if ([bool]$selected.redistributable) {
            [string]$selected.public_redistribution
        } else {
            "BLOCKED PENDING LICENSE/ASSET APPROVAL"
        }
        application = [ordered]@{
            name = [string]$profile.application.name
            version = [string]$profile.application.version
            main_class = [string]$profile.application.main_class
            icon = Get-FileEvidence -Path $packageIconPath `
                -RelativeTo $RepositoryRoot
        }
        file_association = [ordered]@{
            enabled_for_target = $requiresInstaller
            registration_scope = "msi-exe-installers-only"
            extension = [string]$profile.file_association.extension
            mime_type = [string]$profile.file_association.mime_type
            mime_basis = [string]$profile.file_association.mime_basis
            description = [string]$profile.file_association.description
            progid_strategy = [string]$profile.file_association.progid_strategy
            portable_outputs_association_free = $true
            compatibility_extension_claimed = $false
        }
        toolchain = [ordered]@{
            java = $javaVersion
            jpackage = $jpackageVersion
            dotnet_sdk = $dotnetVersion
            wix = $wixVersion
        }
        runtime = [ordered]@{
            included_jar_count = $included.Count
            resolved_gradle_component_count = @($resolvedComponents.components).Count
            staged_external_jar_count = @($jarComponents | Where-Object {
                @($_.properties | Where-Object {
                    $_.name -ceq "geocedg.gradle.component.type" -and
                    $_.value -ceq "module"
                }).Count -eq 1
            }).Count
            sbom_font_count = $fontComponents.Count
            sbom_unknown_version_count = @($jarComponents | Where-Object {
                [string]::IsNullOrWhiteSpace([string]$_.version) -or
                $_.version -ceq "unknown"
            }).Count
            excluded_non_windows_native_jars = @($excluded)
        }
        legal_bundle = [ordered]@{
            manifest = Get-FileEvidence -Path $LicenseManifestPath `
                -RelativeTo $RepositoryRoot
            source_access = Get-FileEvidence -Path $SourceAccessPath `
                -RelativeTo $RepositoryRoot
            component_disposition = Get-FileEvidence `
                -Path $ComponentDispositionPath -RelativeTo $RepositoryRoot
            unresolved_payload_count = 0
            public_profile = if ([string]::IsNullOrWhiteSpace(
                    [string]$selected.licensing_profile)) {
                "PROFILE NC"
            } else {
                [string]$selected.licensing_profile
            }
            licensing_authority = [string]$selected.licensing_authority
            readiness = "TECHNICALLY/LICENSING-DOCKET READY — FINAL AUTHOR/LEGAL REVIEW REQUIRED"
        }
        component_identity = [ordered]@{
            evidence = Get-FileEvidence -Path $resolvedEvidencePath `
                -RelativeTo $ArtifactRoot
            join = "Gradle resolved artifact SHA-256 -> staged app JAR SHA-256"
            versions_inferred_from_filenames = $false
        }
        deliberate_exclusions = @(
            "scientific PDFs",
            "Templatev7.ggb and models/legacy",
            "repository documentation and knowledge sources",
            "upstream installers and explicit upstream branding assets",
            "Linux and macOS native JARs"
        )
        artifacts = $packageEvidence
    }
    $manifestPath = Join-Path $ArtifactRoot "build-manifest.json"
    Write-JsonFile -Value $manifest -Path $manifestPath

    $hashTargets = @($artifactFiles) + @(
        $sbomPath, $manifestPath, $appFileHashPath, $resolvedEvidencePath)
    $hashLines = @($hashTargets | Sort-Object | ForEach-Object {
        $hash = (Get-FileHash -LiteralPath $_ -Algorithm SHA256).Hash.ToLowerInvariant()
        "$hash  $([IO.Path]::GetRelativePath($ArtifactRoot, $_).Replace('\', '/'))"
    })
    $hashPath = Join-Path $ArtifactRoot "SHA256SUMS.txt"
    [IO.File]::WriteAllLines(
        $hashPath, $hashLines, [Text.UTF8Encoding]::new($false))

    Remove-Item -LiteralPath $workRoot -Recurse -Force

    Write-Step "Package summary"
    Write-Host "Application image: $appImage"
    foreach ($artifact in $artifactFiles) {
        Write-Host "Artifact: $artifact"
    }
    Write-Host "SBOM: $sbomPath"
    Write-Host "Manifest: $manifestPath"
    Write-Host "Hashes: $hashPath"
    Write-Host $ExpectedMarker
    Write-Host "PACKAGING TECHNICAL BUILD = PASS"
    Write-Host "PUBLIC REDISTRIBUTION = BLOCKED PENDING LICENSE/ASSET APPROVAL"
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

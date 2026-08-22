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
    [switch]$SkipInstallDist,
    [string]$JdkHome
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$ArtifactRoot = Join-Path $RepositoryRoot "artifacts\packaging\windows"
$ExpectedArtifactRoot = [IO.Path]::GetFullPath($ArtifactRoot)
$ProfilePath = Join-Path $RepositoryRoot "packaging\windows\package.yml"
$AssociationPath = Join-Path $RepositoryRoot `
    "packaging\windows\file-associations.properties"
$NoticePath = Join-Path $RepositoryRoot `
    "packaging\windows\INTERNAL_EVALUATION_ONLY.txt"
$GradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
$ExpectedMarker = "INTERNAL EVALUATION — NOT FOR REDISTRIBUTION"
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
            (Join-Path $RepositoryRoot "geocedg\resources\assets-manifest.yml"))) {
        Assert-Condition -Condition (Test-Path -LiteralPath $required -PathType Leaf) `
            -Message "Required package input is missing: $required"
    }

    $profile = Get-Content -Raw -LiteralPath $ProfilePath |
        ConvertFrom-Json -Depth 50 -NoEnumerate
    try {
        $association = Get-Content -Raw -LiteralPath $AssociationPath |
            ConvertFrom-StringData
    } catch {
        throw "File-association properties are invalid: $($_.Exception.Message)"
    }
    Assert-Condition -Condition ($profile.schema_version -eq 1) `
        -Message "Unsupported package profile schema version."
    Assert-Condition -Condition ($profile.profile_id -eq "geocedg-windows-internal") `
        -Message "Unexpected package profile identity."
    Assert-Condition -Condition ($profile.distribution.marker -ceq $ExpectedMarker) `
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
        $association["description"] -ceq $ExpectedAssociationDescription) `
        -Message "jpackage association properties do not match the native profile."
    Assert-Condition -Condition (
        $association["extension"] -cne "ggb" -and
        $association["mime-type"] -cne $UpstreamGeoGebraMimeType) `
        -Message "GeoCeDG installers must not claim the .ggb extension or upstream MIME identity."

    Write-Host $ExpectedMarker
    Write-Host "Target: $Target"

    Write-Step "Desktop distribution input"
    $installDist = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        ([string]$profile.input.install_dist).Replace("/", "\")))
    if (-not $SkipInstallDist) {
        Invoke-Native -FilePath $GradleWrapper -ArgumentList @(
            [string]$profile.input.gradle_task,
            "--rerun-tasks",
            "--no-build-cache",
            "--no-daemon",
            "--no-problems-report",
            "--console=plain"
        ) -Description "GeoCeDG Desktop installDist"
    }
    Assert-Condition -Condition (Test-Path -LiteralPath $installDist -PathType Container) `
        -Message "installDist lib directory is missing: $installDist"

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
    Assert-Condition -Condition (Test-Path -LiteralPath (
        Join-Path $appImage "app\INTERNAL_EVALUATION_ONLY.txt") -PathType Leaf) `
        -Message "The app-image does not contain the internal-evaluation notice."

    $artifactFiles = [Collections.Generic.List[string]]::new()
    if ($Target -in @("Zip", "All")) {
        Write-Step "Normalized portable ZIP"
        $zipName = "GeoCeDG-$($profile.application.version)-windows-x64-internal.zip"
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
            $internalName = "GeoCeDG-$($profile.application.version)-windows-x64-internal.$type"
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
    $components = @($included | Sort-Object Name | ForEach-Object {
        $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        [ordered]@{
            type = "library"
            name = $_.Name
            version = "unknown"
            "bom-ref" = "jar:$($_.Name):$hash"
            hashes = @([ordered]@{ alg = "SHA-256"; content = $hash })
            properties = @([ordered]@{
                name = "geocedg.packaging.path"
                value = "app/$($_.Name)"
            })
        }
    })
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
        components = $components
    }
    $sbomPath = Join-Path $ArtifactRoot "geocedg-windows.cdx.json"
    Write-JsonFile -Value $sbom -Path $sbomPath

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
        schema_version = 1
        profile = "packaging/windows/package.yml"
        source_revision = $sourceRevision
        source_timestamp = $sourceTimestamp
        baseline = "9b93256b7df401ff056c37b502d82df4d72b1522"
        target = $Target
        platform = "windows-x64"
        distribution_marker = $ExpectedMarker
        public_redistribution = "BLOCKED PENDING LICENSE/ASSET APPROVAL"
        application = [ordered]@{
            name = [string]$profile.application.name
            version = [string]$profile.application.version
            main_class = [string]$profile.application.main_class
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
            excluded_non_windows_native_jars = @($excluded)
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

    $hashTargets = @($artifactFiles) + @($sbomPath, $manifestPath, $appFileHashPath)
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

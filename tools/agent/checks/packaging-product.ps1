#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$ResultPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..'),
    [switch]$RequireArtifacts,
    [string]$ArtifactRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
if (-not $PSBoundParameters.ContainsKey('RequireArtifacts')) {
    $RequireArtifacts = [Environment]::GetEnvironmentVariable(
        'GEOCEDG_PACKAGING_REQUIRE_ARTIFACTS') -ceq 'true'
}
if ([string]::IsNullOrWhiteSpace($ArtifactRoot)) {
    $ArtifactRoot = [Environment]::GetEnvironmentVariable('GEOCEDG_PACKAGING_ARTIFACT_ROOT')
}
if ([string]::IsNullOrWhiteSpace($ArtifactRoot)) {
    $ArtifactRoot = Join-Path $repository 'artifacts/packaging/windows'
}
$ArtifactRoot = [IO.Path]::GetFullPath($ArtifactRoot)

$marker = 'INTERNAL EVALUATION — NOT FOR REDISTRIBUTION'
$iconPath = 'source/desktop/desktop/src/main/resources/' +
    'org/geocedg/desktop/branding/v1/derived/geocedg-application.ico'
$iconHash = 'e5dac1dd3a556f4ce9747f00d272281e9a571ecc5e757180ba1c6750b664cd73'
$contracts = [Collections.Generic.List[object]]::new()
function Add-Contract {
    param([string]$Id, [bool]$Satisfied, [object]$Expected, [object]$Observed,
        [string]$Failure)
    $contracts.Add([ordered]@{
        contract_id = $Id
        status = $(if ($Satisfied) { 'SATISFIED' } else { 'VIOLATED' })
        expected = $Expected
        observed = $Observed
        cause = $(if ($Satisfied) { $null } else { $Failure })
    })
}
function Read-JsonDocument {
    param([string]$Path)
    $text = [IO.File]::ReadAllText($Path, [Text.UTF8Encoding]::new($false, $true))
    return $text | ConvertFrom-Json -Depth 100 -ErrorAction Stop
}
function Test-Asset {
    param([object]$Asset)
    $path = Join-Path $repository ([string]$Asset.path)
    return (Test-Path -LiteralPath $path -PathType Leaf) -and
        (Get-FileHash $path -Algorithm SHA256).Hash.ToLowerInvariant() -ceq
            [string]$Asset.raw_sha256
}
function Test-PngEmbeddedIcon {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $false }
    $bytes = [IO.File]::ReadAllBytes($Path)
    if ($bytes.Length -lt 6 -or $bytes[0] -ne 0 -or $bytes[1] -ne 0 -or
            $bytes[2] -ne 1 -or $bytes[3] -ne 0) { return $false }
    $count = [BitConverter]::ToUInt16($bytes, 4)
    if ($count -ne 7) { return $false }
    $sizes = [Collections.Generic.List[int]]::new()
    for ($index = 0; $index -lt $count; $index++) {
        $entry = 6 + (16 * $index)
        if ($entry + 16 -gt $bytes.Length) { return $false }
        $width = if ($bytes[$entry] -eq 0) { 256 } else { [int]$bytes[$entry] }
        $height = if ($bytes[$entry + 1] -eq 0) { 256 } else { [int]$bytes[$entry + 1] }
        $planes = [BitConverter]::ToUInt16($bytes, $entry + 4)
        $bits = [BitConverter]::ToUInt16($bytes, $entry + 6)
        if ($width -ne $height -or $planes -ne 1 -or $bits -ne 32) {
            return $false
        }
        $sizes.Add($width)
        $offset = [BitConverter]::ToUInt32($bytes, $entry + 12)
        $length = [BitConverter]::ToUInt32($bytes, $entry + 8)
        if ($offset + $length -gt $bytes.Length -or $length -lt 8) { return $false }
        if ([Convert]::ToHexString($bytes[$offset..($offset + 7)]) -cne
                '89504E470D0A1A0A') { return $false }
    }
    return ($sizes -join ',') -ceq '16,24,32,48,64,128,256'
}
function Invoke-NativeCapture {
    param([string]$FilePath, [string[]]$ArgumentList, [string]$WorkingDirectory)
    if ([string]::IsNullOrWhiteSpace($FilePath)) {
        return [pscustomobject]@{ state = 'EXECUTABLE_MISSING'; exit_code = $null; stdout = ''; stderr = ''; cause = 'Executable is unavailable.' }
    }
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $FilePath
    $start.WorkingDirectory = $WorkingDirectory
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    foreach ($argument in $ArgumentList) { [void]$start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Native process could not start.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        return [pscustomobject]@{
            state = 'COMPLETED'
            exit_code = $process.ExitCode
            stdout = $stdoutTask.GetAwaiter().GetResult()
            stderr = $stderrTask.GetAwaiter().GetResult()
            cause = $null
        }
    } catch {
        return [pscustomobject]@{ state = 'EXECUTION_ERROR'; exit_code = $null; stdout = ''; stderr = ''; cause = $_.Exception.Message }
    } finally { $process.Dispose() }
}
function Test-MsiNativeAssociation {
    param([string]$MsiPath, [string]$InspectionRoot)
    $wix = Get-Command wix -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $wix) {
        return [pscustomobject]@{ state = 'EVIDENCE_UNTRUSTED'; cause = 'WiX is unavailable for MSI product-evidence projection.'; observed = $null }
    }
    [void][IO.Directory]::CreateDirectory($InspectionRoot)
    $msiHash = (Get-FileHash -LiteralPath $MsiPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $decompiled = Join-Path $InspectionRoot "msi-$msiHash.wxs"
    if (Test-Path -LiteralPath $decompiled -PathType Leaf) {
        Remove-Item -LiteralPath $decompiled -Force
    }
    $run = Invoke-NativeCapture $wix.Path @(
        'msi', 'decompile', '-sui', '-o', $decompiled, $MsiPath) $repository
    if ($run.state -cne 'COMPLETED' -or $run.exit_code -ne 0 -or
            -not (Test-Path -LiteralPath $decompiled -PathType Leaf)) {
        return [pscustomobject]@{ state = 'EVIDENCE_UNTRUSTED'; cause = 'WiX did not produce MSI XML evidence.'; observed = $run }
    }
    try { [xml]$source = [IO.File]::ReadAllText($decompiled) } catch {
        return [pscustomobject]@{ state = 'EVIDENCE_UNTRUSTED'; cause = 'WiX MSI evidence is not valid XML.'; observed = $_.Exception.Message }
    }
    $extensionNodes = @($source.SelectNodes("//*[local-name()='Extension']"))
    $native = @($extensionNodes | Where-Object { $_.GetAttribute('Id') -ceq 'cedg' })
    $details = [ordered]@{
        native_extension_count = $native.Count
        upstream_extension_count = @($extensionNodes | Where-Object { $_.GetAttribute('Id') -ieq 'ggb' }).Count
        internal_mime_count = 0
        total_mime_count = 0
        upstream_mime_count = @($source.SelectNodes('//*[@ContentType]') | Where-Object {
            $_.GetAttribute('ContentType') -ceq 'application/vnd.geogebra.file'
        }).Count
        owned_progid = $false
        open_verb_count = 0
        launcher_target_count = 0
        launcher_identity = $null
    }
    if ($native.Count -eq 1) {
        $extension = $native[0]
        $mimes = @($extension.SelectNodes("./*[local-name()='MIME']"))
        $details.total_mime_count = $mimes.Count
        $details.internal_mime_count = @($mimes | Where-Object {
            $_.GetAttribute('ContentType') -ceq 'application/x-geocedg-cedg'
        }).Count
        $progId = $extension.ParentNode
        $details.owned_progid = $progId.LocalName -ceq 'ProgId' -and
            -not [string]::IsNullOrWhiteSpace($progId.GetAttribute('Id')) -and
            $progId.GetAttribute('Description') -ceq 'GeoCeDG document (internal evaluation)' -and
            $progId.GetAttribute('Id') -notmatch '(?i)geogebra'
        $verbs = @($extension.SelectNodes("./*[local-name()='Verb' and @Id='open']"))
        $details.open_verb_count = $verbs.Count
        if ($verbs.Count -eq 1 -and $verbs[0].GetAttribute('Argument').Contains('%1')) {
            $targetId = $verbs[0].GetAttribute('TargetFile')
            $targets = @($source.SelectNodes("//*[local-name()='File']") | Where-Object {
                $_.GetAttribute('Id') -ceq $targetId
            })
            $details.launcher_target_count = $targets.Count
            if ($targets.Count -eq 1) {
                $details.launcher_identity = @(
                    $targets[0].GetAttribute('Name'), $targets[0].GetAttribute('Source')) -join '|'
            }
        }
        $contentTypeCorrect = $extension.GetAttribute('ContentType') -ceq
            'application/x-geocedg-cedg'
    } else { $contentTypeCorrect = $false }
    $valid = $native.Count -eq 1 -and $details.upstream_extension_count -eq 0 -and
        $contentTypeCorrect -and $details.total_mime_count -eq 1 -and
        $details.internal_mime_count -eq 1 -and
        $details.upstream_mime_count -eq 0 -and $details.owned_progid -and
        $details.open_verb_count -eq 1 -and $details.launcher_target_count -eq 1 -and
        $details.launcher_identity -match '(?i)GeoCeDG\.exe'
    return [pscustomobject]@{
        state = $(if ($valid) { 'SATISFIED' } else { 'VIOLATED' })
        cause = $(if ($valid) { $null } else { 'MSI native association differs from the approved GeoCeDG contract.' })
        observed = $details
    }
}

$outcome = 'EVIDENCE_UNTRUSTED'
$cause = $null
try {
    $required = @(
        'docs/adr/0004-standalone-windows-packaging.md',
        'docs/adr/0016-native-geocedg-document-identity.md',
        'geocedg/specs/packaging/windows-packaging.md',
        'geocedg/specs/operations/package-profile.schema.json',
        'geocedg/specs/ui/native-document-identity.md',
        'packaging/windows/package.yml', 'packaging/windows/NuGet.Config',
        'packaging/windows/file-associations.properties',
        'packaging/windows/INTERNAL_EVALUATION_ONLY.txt',
        'tools/release/build-windows-package.ps1', 'LICENSE', 'LICENSES/README.md',
        'NOTICE.md', 'THIRD_PARTY.md', 'geocedg/resources/assets-manifest.yml',
        'tools/resources/generate-geocedg-branding.ps1', $iconPath)
    $missing = @($required | Where-Object {
        -not (Test-Path -LiteralPath (Join-Path $repository $_) -PathType Leaf)
    })
    Add-Contract 'packaging.required-files' ($missing.Count -eq 0) $required $missing `
        'A required packaging source is missing.'
    if ($missing.Count -ne 0) { throw 'Required packaging sources are unavailable.' }

    $profile = Read-JsonDocument (Join-Path $repository 'packaging/windows/package.yml')
    $schema = Read-JsonDocument (Join-Path $repository 'geocedg/specs/operations/package-profile.schema.json')
    $assets = Read-JsonDocument (Join-Path $repository 'geocedg/resources/assets-manifest.yml')
    $association = [IO.File]::ReadAllText(
        (Join-Path $repository 'packaging/windows/file-associations.properties'),
        [Text.UTF8Encoding]::new($false, $true)) | ConvertFrom-StringData

    Add-Contract 'packaging.schema' ($schema.type -eq 'object' -and
        $schema.'$id' -eq 'https://geocedg.local/schemas/package-profile-v1') `
        'package-profile-v1' $schema.'$id' 'Package schema identity is invalid.'
    Add-Contract 'packaging.schema-icon' `
        ($schema.properties.application.properties.icon.const -ceq $iconPath) `
        $iconPath $schema.properties.application.properties.icon.const `
        'Package schema does not freeze the versioned GeoCeDG icon.'
    $identity = $profile.schema_version -eq 1 -and
        $profile.profile_id -eq 'geocedg-windows-internal' -and
        $profile.application.name -eq 'GeoCeDG' -and
        $profile.application.main_class -eq 'org.geocedg.desktop.GeoCeDG' -and
        $profile.application.icon -ceq $iconPath -and
        $profile.toolchain.gradle_java -eq 22 -and
        $profile.toolchain.desktop_java -eq 25 -and
        $profile.toolchain.wix -eq '5.0.2'
    Add-Contract 'packaging.profile' $identity 'approved package profile' $profile.profile_id `
        'Package profile identity or declared toolchain contract differs.'
    $extensions = @($profile.toolchain.wix_extensions)
    Add-Contract 'packaging.declared-extensions' ($extensions.Count -eq 2 -and
        $extensions[0] -ceq 'WixToolset.Util.wixext/5.0.2' -and
        $extensions[1] -ceq 'WixToolset.UI.wixext/5.0.2') `
        @('WixToolset.Util.wixext/5.0.2', 'WixToolset.UI.wixext/5.0.2') $extensions `
        'Pinned WiX extension declaration differs.'
    Add-Contract 'packaging.outputs' (@($profile.outputs).Count -eq 4 -and
        (@($profile.outputs) -join ',') -ceq 'app-image,zip,msi,exe') `
        @('app-image', 'zip', 'msi', 'exe') @($profile.outputs) `
        'Package output declaration differs.'
    $fileAssociation = $profile.file_association.installers_only -and
        $profile.file_association.extension -ceq 'cedg' -and
        $profile.file_association.mime_type -ceq 'application/x-geocedg-cedg' -and
        $profile.file_association.mime_basis -ceq
            'jdk25-jpackage-required-internal-unregistered' -and
        $profile.file_association.description -ceq
            'GeoCeDG document (internal evaluation)' -and
        $profile.file_association.progid_strategy -ceq
            'jdk25-jpackage-generated-geocedg-owned'
    Add-Contract 'packaging.association-profile' $fileAssociation `
        'approved internal .cedg association' $profile.file_association `
        'Native .cedg association profile differs.'
    $associationSchema = $schema.properties.file_association.properties
    $schemaAssociation = $associationSchema.extension.const -ceq 'cedg' -and
        $associationSchema.mime_type.const -ceq 'application/x-geocedg-cedg' -and
        $associationSchema.mime_basis.const -ceq
            'jdk25-jpackage-required-internal-unregistered' -and
        $associationSchema.description.const -ceq
            'GeoCeDG document (internal evaluation)' -and
        $associationSchema.progid_strategy.const -ceq
            'jdk25-jpackage-generated-geocedg-owned'
    Add-Contract 'packaging.association-schema' $schemaAssociation `
        'approved association schema constants' $associationSchema `
        'Association schema constants differ.'
    $propertiesValid = $association.Count -eq 3 -and
        $association.extension -ceq 'cedg' -and
        $association.'mime-type' -ceq 'application/x-geocedg-cedg' -and
        $association.description -ceq 'GeoCeDG document (internal evaluation)' -and
        $association.extension -cne 'ggb' -and
        $association.'mime-type' -cne 'application/vnd.geogebra.file'
    Add-Contract 'packaging.association-properties' $propertiesValid `
        'GeoCeDG-owned association properties' $association `
        'Association properties differ or claim upstream identity.'
    Add-Contract 'packaging.distribution' ($profile.distribution.marker -ceq $marker -and
        $profile.distribution.public_redistribution -eq
            'blocked-pending-license-and-asset-approval') $marker $profile.distribution `
        'Internal distribution status differs.'

    $top = @($assets.branding_assets | Where-Object id -CEQ 'geocedg.brand.topbar')
    $startup = @($assets.branding_assets | Where-Object id -CEQ 'geocedg.brand.startup')
    $assetAuthority = $assets.distribution_marker -ceq $marker -and
        @($assets.deliberate_exclusions).Count -ge 5 -and $top.Count -eq 1 -and
        $startup.Count -eq 1 -and $assets.package_icon.asset_id -ceq
            'geocedg.brand.topbar.windows-ico-v1' -and
        $assets.package_icon.path -ceq $iconPath -and
        $assets.package_icon.raw_sha256 -ceq $iconHash
    Add-Contract 'packaging.asset-authority' $assetAuthority 'approved asset manifest' `
        $assets.package_icon 'Asset authority differs.'
    if ($top.Count -eq 1 -and $startup.Count -eq 1) {
        $frame = @($top[0].derivatives | Where-Object id -CEQ 'geocedg.brand.topbar.frame-64-v1')
        $icon = @($top[0].derivatives | Where-Object id -CEQ 'geocedg.brand.topbar.windows-ico-v1')
        $splash = @($startup[0].derivatives | Where-Object id -CEQ 'geocedg.brand.startup.splash-361x480-v2')
        $authorInputs = $top[0].author_input.original_filename -ceq 'helixTopBar.png' -and
            $top[0].author_input.width -eq 969 -and $top[0].author_input.height -eq 815 -and
            $top[0].author_input.raw_sha256 -ceq
            '08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1' -and
            $startup[0].author_input.original_filename -ceq 'helixSnapshot.png' -and
            $startup[0].author_input.width -eq 1197 -and
            $startup[0].author_input.height -eq 1591 -and
            $startup[0].author_input.raw_sha256 -ceq
            'abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637'
        Add-Contract 'packaging.author-inputs' $authorInputs 'approved input hashes' $null `
            'Author branding input provenance differs.'
        $derived = $frame.Count -eq 1 -and $frame[0].width -eq 64 -and
            $frame[0].height -eq 64 -and $frame[0].raw_sha256 -ceq
            '448ea5b510f952d27ddddec3005911b4e1f1203ee35f55fcca857098e53fed97' -and
            $icon.Count -eq 1 -and $icon[0].raw_sha256 -ceq $iconHash -and
            (@($icon[0].png_embedded_sizes) -join ',') -ceq '16,24,32,48,64,128,256' -and
            $splash.Count -eq 1 -and $splash[0].width -eq 361 -and
            $splash[0].height -eq 480 -and $splash[0].raw_sha256 -ceq
            '664ece93d38a6fc57ae3b29ede79161f928265b4fb069b5e72519b9aab494195'
        Add-Contract 'packaging.derived-assets' $derived 'approved derivative hashes' $null `
            'Derived branding provenance differs.'
        $tracked = (Test-Asset $top[0].promoted_source) -and
            (Test-Asset $startup[0].promoted_source) -and $frame.Count -eq 1 -and
            (Test-Asset $frame[0]) -and $icon.Count -eq 1 -and
            (Test-Asset $icon[0]) -and $splash.Count -eq 1 -and (Test-Asset $splash[0])
        Add-Contract 'packaging.tracked-assets' $tracked 'tracked asset hashes' $tracked `
            'A tracked packaging resource is missing or has a different hash.'
        $derivation = $top[0].derivation.tool -ceq
            'tools/resources/generate-geocedg-branding.ps1' -and
            $startup[0].derivation.tool -ceq
            'tools/resources/generate-geocedg-branding.ps1' -and
            -not [string]::IsNullOrWhiteSpace(
                [string]$top[0].derivation.accepted_generation_runtime) -and
            $startup[0].derivation.accepted_generation_runtime -ceq
                $top[0].derivation.accepted_generation_runtime
        Add-Contract 'packaging.asset-derivation' $derivation `
            'approved branding derivation authority' $derivation `
            'Branding derivation runtime/provenance differs.'
    }
    $actualIconHash = (Get-FileHash (Join-Path $repository $iconPath) -Algorithm SHA256).Hash.ToLowerInvariant()
    Add-Contract 'packaging.icon-hash' ($actualIconHash -ceq $iconHash) $iconHash `
        $actualIconHash 'GeoCeDG package icon hash differs.'
    Add-Contract 'packaging.icon-embedded-pngs' `
        (Test-PngEmbeddedIcon (Join-Path $repository $iconPath)) `
        'PNG icon entries at 16,24,32,48,64,128,256' $iconPath `
        'GeoCeDG package icon structure differs.'
    foreach ($legal in @('LICENSE', 'LICENSES/README.md', 'NOTICE.md', 'THIRD_PARTY.md')) {
        Add-Contract ('packaging.marker.' + $legal.Replace('/', '-').ToLowerInvariant()) `
            ([IO.File]::ReadAllText((Join-Path $repository $legal)).Contains($marker)) `
            $marker $legal "$legal lacks the internal-evaluation marker."
    }
    $builder = [IO.File]::ReadAllText(
        (Join-Path $repository 'tools/release/build-windows-package.ps1'))
    $tokens = @('org.geocedg.desktop.GeoCeDG', '--type', 'app-image', 'msi', 'exe',
        '--icon', '--file-associations', 'cedg', 'application/x-geocedg-cedg',
        'jdk25-jpackage-required-internal-unregistered', 'geocedg-windows.cdx.json',
        'SHA256SUMS.txt', $marker)
    $missingTokens = @($tokens | Where-Object { -not $builder.Contains($_) })
    $iconSwitch = '"--icon"'
    $associationSwitch = '"--file-associations"'
    Add-Contract 'packaging.builder' ($missingTokens.Count -eq 0 -and
        -not $builder.Contains('source\desktop\desktop\build\scripts') -and
        [regex]::Matches($builder, [regex]::Escape($iconSwitch)).Count -eq 1 -and
        $builder.IndexOf($iconSwitch, [StringComparison]::Ordinal) -lt
            $builder.IndexOf('$appImage = Join-Path', [StringComparison]::Ordinal) -and
        [regex]::Matches($builder, [regex]::Escape($associationSwitch)).Count -eq 1 -and
        $builder.IndexOf($associationSwitch, [StringComparison]::Ordinal) -gt
            $builder.LastIndexOf('if ($requiresInstaller)', [StringComparison]::Ordinal)) `
        'approved package-builder contract' $missingTokens 'Package builder contract differs.'

    if ($RequireArtifacts) {
        $appImage = Join-Path $ArtifactRoot 'app-image/GeoCeDG'
        $evidence = @(
            (Join-Path $appImage 'GeoCeDG.exe'),
            (Join-Path $appImage 'app/INTERNAL_EVALUATION_ONLY.txt'),
            (Join-Path $ArtifactRoot 'geocedg-windows.cdx.json'),
            (Join-Path $ArtifactRoot 'build-manifest.json'),
            (Join-Path $ArtifactRoot 'app-image.SHA256SUMS.txt'),
            (Join-Path $ArtifactRoot 'SHA256SUMS.txt'))
        $missingEvidence = @($evidence | Where-Object {
            -not (Test-Path -LiteralPath $_ -PathType Leaf)
        })
        Add-Contract 'packaging.artifacts-present' ($missingEvidence.Count -eq 0) `
            $evidence $missingEvidence 'Required package evidence is missing.'
        if ($missingEvidence.Count -eq 0) {
            $zip = @(Get-ChildItem $ArtifactRoot -File -Filter 'GeoCeDG-*-internal.zip' -ErrorAction SilentlyContinue)
            $packages = Join-Path $ArtifactRoot 'packages'
            $msi = @(Get-ChildItem $packages -File -Filter 'GeoCeDG-*-internal.msi' -ErrorAction SilentlyContinue)
            $exe = @(Get-ChildItem $packages -File -Filter 'GeoCeDG-*-internal.exe' -ErrorAction SilentlyContinue)
            Add-Contract 'packaging.artifact-set' ($zip.Count -eq 1 -and $msi.Count -eq 1 -and
                $exe.Count -eq 1) 'one ZIP, MSI and EXE' `
                ([ordered]@{ zip = $zip.Count; msi = $msi.Count; exe = $exe.Count }) `
                'Generated package set differs.'
            Add-Contract 'packaging.artifact-marker' `
                ([IO.File]::ReadAllText($evidence[1]).Contains($marker)) $marker $evidence[1] `
                'Generated app-image marker differs.'
            $forbidden = @(Get-ChildItem $appImage -Recurse -File | Where-Object {
                $_.Extension -in @('.pdf', '.ggb', '.ggt') -or $_.Name -eq 'Templatev7.ggb' -or
                $_.Name -match '(?i)-natives-(linux|macosx)-'
            })
            Add-Contract 'packaging.portable-boundary' ($forbidden.Count -eq 0) `
                'no forbidden files' @($forbidden.FullName) 'Forbidden app-image content exists.'
            $config = @(Get-ChildItem (Join-Path $appImage 'app') -File -Filter '*.cfg') |
                Select-Object -First 1
            Add-Contract 'packaging.launcher-config' ($null -ne $config -and
                [IO.File]::ReadAllText($config.FullName).Contains('org.geocedg.desktop.GeoCeDG')) `
                'GeoCeDG main class' $config 'Generated launcher configuration differs.'
            $sbom = Read-JsonDocument $evidence[2]
            Add-Contract 'packaging.sbom' ($sbom.bomFormat -eq 'CycloneDX' -and
                $sbom.specVersion -eq '1.5' -and @($sbom.components).Count -gt 0) `
                'non-empty CycloneDX 1.5' $sbom 'Generated SBOM differs.'
            $manifest = Read-JsonDocument $evidence[3]
            $manifestValid = $manifest.target -eq 'All' -and
                $manifest.distribution_marker -ceq $marker -and
                $manifest.public_redistribution -eq 'BLOCKED PENDING LICENSE/ASSET APPROVAL' -and
                $manifest.application.icon.path -ceq $iconPath -and
                $manifest.application.icon.sha256 -ceq $iconHash -and
                $manifest.file_association.enabled_for_target -eq $true -and
                $manifest.file_association.registration_scope -ceq 'msi-exe-installers-only' -and
                $manifest.file_association.extension -ceq 'cedg' -and
                $manifest.file_association.mime_type -ceq 'application/x-geocedg-cedg' -and
                $manifest.file_association.mime_basis -ceq
                    'jdk25-jpackage-required-internal-unregistered' -and
                $manifest.file_association.progid_strategy -ceq
                    'jdk25-jpackage-generated-geocedg-owned' -and
                $manifest.file_association.portable_outputs_association_free -eq $true -and
                $manifest.file_association.compatibility_extension_claimed -eq $false -and
                @($manifest.runtime.excluded_non_windows_native_jars).Count -gt 0
            Add-Contract 'packaging.build-manifest' $manifestValid 'approved build manifest' `
                $manifest 'Generated build manifest differs.'
            if ($msi.Count -eq 1) {
                $msiAssociation = Test-MsiNativeAssociation -MsiPath $msi[0].FullName `
                    -InspectionRoot (Join-Path $ArtifactRoot 'verification')
                if ($msiAssociation.state -ceq 'EVIDENCE_UNTRUSTED') {
                    throw $msiAssociation.cause
                }
                Add-Contract 'packaging.msi-native-association' `
                    ($msiAssociation.state -ceq 'SATISFIED') `
                    'one GeoCeDG-owned .cedg association targeting GeoCeDG.exe' `
                    $msiAssociation.observed $msiAssociation.cause
            } else {
                Add-Contract 'packaging.msi-native-association' $false `
                    'one GeoCeDG-owned .cedg association targeting GeoCeDG.exe' `
                    $msi.Count 'MSI product evidence is unavailable.'
            }
            $hashErrors = [Collections.Generic.List[string]]::new()
            foreach ($line in [IO.File]::ReadAllLines($evidence[5])) {
                if ($line -cnotmatch '^([0-9a-f]{64})  (.+)$') {
                    $hashErrors.Add("malformed:$line")
                    continue
                }
                $file = [IO.Path]::GetFullPath((Join-Path $ArtifactRoot $Matches[2]))
                if (-not (Test-Path -LiteralPath $file -PathType Leaf) -or
                        (Get-FileHash $file -Algorithm SHA256).Hash.ToLowerInvariant() -cne
                            $Matches[1]) { $hashErrors.Add($Matches[2]) }
            }
            Add-Contract 'packaging.artifact-hashes' ($hashErrors.Count -eq 0) `
                'all SHA256SUMS entries match' @($hashErrors) 'Artifact hash mismatch.'
        }
    }
    $outcome = if (@($contracts | Where-Object status -CEQ 'VIOLATED').Count -eq 0) {
        'CONTRACT_SATISFIED'
    } else { 'CONTRACT_VIOLATED' }
} catch {
    $outcome = 'EVIDENCE_UNTRUSTED'
    $cause = $_.Exception.Message
}

$result = [ordered]@{
    contract_class = 'SEMANTIC'
    semantic_domain = 'PRODUCT'
    outcome = $outcome
    cause = $cause
    check_kind = 'PACKAGING_PRODUCT_CONTRACT'
    require_artifacts = [bool]$RequireArtifacts
    artifact_root = $ArtifactRoot
    subcontracts = [object[]]$contracts.ToArray()
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
$json = (ConvertTo-Json $result -Depth 100).Replace("`r`n", "`n") + "`n"
[IO.File]::WriteAllText($full, $json, $utf8)
if ($outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
if ($outcome -ceq 'CONTRACT_VIOLATED') { exit 1 }
exit 3

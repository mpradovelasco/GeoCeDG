[CmdletBinding(DefaultParameterSetName = "Inspect")]
param(
    [Parameter(Mandatory)]
    [ValidatePattern('^cedg\.legacy\.[a-z0-9]+(?:[.-][a-z0-9]+)*$')]
    [string]$ResourceId,

    [Parameter(Mandatory)]
    [string]$Source,

    [string]$TargetDirectory,
    [string]$CurationPath,
    [string]$InventoryPath,

    [Parameter(ParameterSetName = "Import")]
    [switch]$Import,

    [Parameter(ParameterSetName = "Check")]
    [switch]$Check
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
$LegacyRoot = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot "models\legacy"))
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$AllowedExtensions = @(".ggb", ".ggt", ".js", ".ggs")

function Resolve-RepositoryPath {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [switch]$RequireFile
    )

    $absolute = if ([IO.Path]::IsPathRooted($Path)) {
        [IO.Path]::GetFullPath($Path)
    } else {
        [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $Path))
    }
    if ($RequireFile -and -not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "Required file does not exist: $Path"
    }
    return $absolute
}

function Get-RepositoryRelativePath {
    param([Parameter(Mandatory)] [string]$Path)

    $absolute = [IO.Path]::GetFullPath($Path)
    if ($absolute.StartsWith($RootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        return $absolute.Substring($RootPrefix.Length).Replace("\", "/")
    }
    return $absolute.Replace("\", "/")
}

function Get-BytesSha256 {
    param([Parameter(Mandatory)] [byte[]]$Bytes)

    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return ([Convert]::ToHexString($sha.ComputeHash($Bytes))).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Read-ZipEntryBytes {
    param([Parameter(Mandatory)] [IO.Compression.ZipArchiveEntry]$Entry)

    $stream = $Entry.Open()
    $memory = [IO.MemoryStream]::new()
    try {
        $stream.CopyTo($memory)
        return $memory.ToArray()
    } finally {
        $memory.Dispose()
        $stream.Dispose()
    }
}

function ConvertTo-StableSlug {
    param([Parameter(Mandatory)] [string]$Value)

    $withBoundaries = [regex]::Replace($Value, '(?<=[a-z0-9])(?=[A-Z])', '-')
    $slug = [regex]::Replace($withBoundaries.ToLowerInvariant(), '[^a-z0-9]+', '-')
    return $slug.Trim('-')
}

function Get-OrderedAttributeValues {
    param([Parameter(Mandatory)] [System.Xml.XmlElement]$Element)

    if ($null -eq $Element) {
        return @()
    }
    return @($Element.Attributes | Sort-Object {
            if ($_.Name -match '^a(\d+)$') { [int]$Matches[1] } else { [int]::MaxValue }
        } | ForEach-Object { $_.Value })
}

function Read-Curation {
    param([string]$Path, [string]$ExpectedHash)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }
    $absolute = Resolve-RepositoryPath -Path $Path -RequireFile
    $curation = Get-Content -Raw -LiteralPath $absolute |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    if ($curation.schema_version -ne 1 -or $curation.resource_id -ne $ResourceId) {
        throw "Curation identity does not match $ResourceId."
    }
    if ($curation.source_artifact_sha256 -ne $ExpectedHash) {
        throw "Curation source hash does not match the original artifact."
    }
    return [ordered]@{
        absolute_path = $absolute
        relative_path = Get-RepositoryRelativePath -Path $absolute
        sha256 = (Get-FileHash -LiteralPath $absolute -Algorithm SHA256).Hash.ToLowerInvariant()
        value = $curation
    }
}

function New-UncuratedTool {
    param([Parameter(Mandatory)] [string]$Id, [Parameter(Mandatory)] [string]$Name)

    return [ordered]@{
        id = $Id
        source_command_name = $Name
        family = "unknown"
        category = "unknown"
        maturity = "legacy"
        architecture_recommendation = "remain-external-legacy"
        confidence = "low"
        observations = "Not yet curated; structural inventory only."
    }
}

function New-Inventory {
    param(
        [Parameter(Mandatory)] [string]$OriginalPath,
        [Parameter(Mandatory)] [string]$ArtifactHash,
        [object]$CurationRecord
    )

    $extension = [IO.Path]::GetExtension($OriginalPath).ToLowerInvariant()
    $entries = [Collections.Generic.List[object]]::new()
    $tools = [Collections.Generic.List[object]]::new()
    $customGroups = [Collections.Generic.List[object]]::new()
    $containerFormat = $extension.TrimStart('.')
    $geogebraVersion = $null
    $app = $null
    $platform = $null
    $toolbarDefinition = ""
    $globalJavascript = [ordered]@{
        present = $false
        entry = $null
        bytes = 0
        sha256 = $null
        functions = @()
    }
    $documentScriptBlocks = 0

    if ($extension -in @(".ggb", ".ggt")) {
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $archive = [IO.Compression.ZipFile]::OpenRead($OriginalPath)
        try {
            $entryBytes = @{}
            foreach ($entry in @($archive.Entries | Sort-Object FullName)) {
                $bytes = Read-ZipEntryBytes -Entry $entry
                $entryBytes[$entry.FullName] = $bytes
                $entries.Add([ordered]@{
                        name = $entry.FullName
                        bytes = [long]$entry.Length
                        compressed_bytes = [long]$entry.CompressedLength
                        sha256 = Get-BytesSha256 -Bytes $bytes
                    })
            }

            $documentXml = $null
            if ($entryBytes.ContainsKey("geogebra.xml")) {
                [xml]$documentXml = $Utf8NoBom.GetString($entryBytes["geogebra.xml"])
                $geogebraVersion = [string]$documentXml.geogebra.version
                $app = [string]$documentXml.geogebra.app
                $platform = [string]$documentXml.geogebra.platform
                $toolbarNode = $documentXml.SelectSingleNode(
                    "/geogebra/gui/perspectives/perspective/toolbar")
                if ($null -ne $toolbarNode) {
                    $toolbarDefinition = [string]$toolbarNode.items
                }
                foreach ($node in @($documentXml.SelectNodes("//ggbscript|//javascript"))) {
                    foreach ($attribute in @($node.Attributes)) {
                        if (-not [string]::IsNullOrWhiteSpace($attribute.Value)) {
                            $documentScriptBlocks++
                        }
                    }
                }
            }

            if ($entryBytes.ContainsKey("geogebra_javascript.js")) {
                $javascriptBytes = [byte[]]$entryBytes["geogebra_javascript.js"]
                $javascriptText = $Utf8NoBom.GetString($javascriptBytes)
                $globalJavascript = [ordered]@{
                    present = $true
                    entry = "geogebra_javascript.js"
                    bytes = $javascriptBytes.Length
                    sha256 = Get-BytesSha256 -Bytes $javascriptBytes
                    functions = @([regex]::Matches(
                            $javascriptText, '(?m)^\s*function\s+([A-Za-z0-9_]+)\s*\(') |
                            ForEach-Object { $_.Groups[1].Value })
                }
            }

            if ($entryBytes.ContainsKey("geogebra_macro.xml")) {
                [xml]$macroXml = $Utf8NoBom.GetString($entryBytes["geogebra_macro.xml"])
                $macroNodes = @($macroXml.geogebra.macro)
                $macroNames = @($macroNodes | ForEach-Object { [string]$_.cmdName })
                $locations = @{}

                if (-not [string]::IsNullOrWhiteSpace($toolbarDefinition)) {
                    $groupNumber = 0
                    foreach ($groupText in @($toolbarDefinition -split '\|')) {
                        $groupNumber++
                        $subgroupNumber = 0
                        $groupToolIds = [Collections.Generic.List[string]]::new()
                        foreach ($subgroupText in @($groupText -split ',')) {
                            $subgroupNumber++
                            $position = 0
                            foreach ($token in @($subgroupText.Trim() -split '\s+')) {
                                if ($token -notmatch '^100\d+$') {
                                    continue
                                }
                                $position++
                                $modeId = [int]$token
                                $macroIndex = $modeId - 100001
                                if ($macroIndex -ge 0 -and $macroIndex -lt $macroNames.Count) {
                                    $toolId = "$ResourceId.tool.$(ConvertTo-StableSlug $macroNames[$macroIndex])"
                                    $locations[$modeId] = [ordered]@{
                                        group = $groupNumber
                                        subgroup = $subgroupNumber
                                        position = $position
                                    }
                                    $groupToolIds.Add($toolId)
                                }
                            }
                        }
                        if ($groupToolIds.Count -gt 0) {
                            $customGroups.Add([ordered]@{
                                    group = $groupNumber
                                    raw = $groupText.Trim()
                                    tool_ids = @($groupToolIds)
                                })
                        }
                    }
                }

                $curationById = @{}
                if ($null -ne $CurationRecord) {
                    foreach ($item in @($CurationRecord.value.tools)) {
                        $curationById[[string]$item.id] = $item
                    }
                }

                for ($index = 0; $index -lt $macroNodes.Count; $index++) {
                    $macro = $macroNodes[$index]
                    $modeId = 100001 + $index
                    $commandName = [string]$macro.cmdName
                    $toolId = "$ResourceId.tool.$(ConvertTo-StableSlug $commandName)"
                    $elementTypes = @{}
                    foreach ($element in @($macro.construction.element)) {
                        $elementTypes[[string]$element.label] = [string]$element.type
                    }
                    $inputRecords = @((Get-OrderedAttributeValues -Element $macro.macroInput) |
                        ForEach-Object {
                            [ordered]@{
                                label = $_
                                type = if ($elementTypes.ContainsKey($_)) {
                                    $elementTypes[$_]
                                } else {
                                    "unknown"
                                }
                            }
                        })
                    $outputRecords = @((Get-OrderedAttributeValues -Element $macro.macroOutput) |
                        ForEach-Object {
                            [ordered]@{
                                label = $_
                                type = if ($elementTypes.ContainsKey($_)) {
                                    $elementTypes[$_]
                                } else {
                                    "unknown"
                                }
                            }
                        })
                    $commandDependencies = @($macro.construction.command |
                        ForEach-Object { [string]$_.name } | Sort-Object -Unique)
                    $toolDependencies = @($commandDependencies |
                        Where-Object { $macroNames -contains $_ } | Sort-Object -Unique)
                    $scriptCount = 0
                    foreach ($node in @($macro.SelectNodes(
                                ".//ggbscript|.//javascript"))) {
                        foreach ($attribute in @($node.Attributes)) {
                            if (-not [string]::IsNullOrWhiteSpace($attribute.Value)) {
                                $scriptCount++
                            }
                        }
                    }
                    $curation = if ($curationById.ContainsKey($toolId)) {
                        $curationById[$toolId]
                    } else {
                        New-UncuratedTool -Id $toolId -Name $commandName
                    }
                    if ([string]$curation.source_command_name -ne $commandName) {
                        throw "Curation command mismatch for $toolId."
                    }
                    $tools.Add([ordered]@{
                            id = $toolId
                            legacy_index = $index + 1
                            mode_id = $modeId
                            command_name = $commandName
                            tool_name = [string]$macro.toolName
                            help = [string]$macro.toolHelp
                            original_toolbar = if ($locations.ContainsKey($modeId)) {
                                $locations[$modeId]
                            } else {
                                $null
                            }
                            inputs = $inputRecords
                            outputs = $outputRecords
                            dependencies = [ordered]@{
                                embedded_tools = $toolDependencies
                                baseline_commands = $commandDependencies
                            }
                            contains_geogebrascript = ($scriptCount -gt 0)
                            family = [string]$curation.family
                            category = [string]$curation.category
                            maturity = [string]$curation.maturity
                            architecture_recommendation = [string]$curation.architecture_recommendation
                            confidence = [string]$curation.confidence
                            observations = [string]$curation.observations
                        })
                    [void]$curationById.Remove($toolId)
                }
                if ($curationById.Count -ne 0) {
                    throw "Curation contains tools not present in the source artifact: $($curationById.Keys -join ', ')"
                }
            }
        } finally {
            $archive.Dispose()
        }
    } else {
        $bytes = [IO.File]::ReadAllBytes($OriginalPath)
        $entries.Add([ordered]@{
                name = [IO.Path]::GetFileName($OriginalPath)
                bytes = $bytes.Length
                compressed_bytes = $null
                sha256 = Get-BytesSha256 -Bytes $bytes
            })
    }

    $generatorHash = (Get-FileHash -LiteralPath $PSCommandPath `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    return [ordered]@{
        '$schema' = "../../../../geocedg/specs/operations/legacy-tool-inventory.schema.json"
        schema_version = 1
        generator = [ordered]@{
            path = "tools/legacy/ingest.ps1"
            sha256 = $generatorHash
            curation_path = if ($null -eq $CurationRecord) {
                $null
            } else {
                $CurationRecord.relative_path
            }
            curation_sha256 = if ($null -eq $CurationRecord) {
                $null
            } else {
                $CurationRecord.sha256
            }
        }
        resource = [ordered]@{
            id = $ResourceId
            path = Get-RepositoryRelativePath -Path $OriginalPath
            sha256 = $ArtifactHash
            bytes = (Get-Item -LiteralPath $OriginalPath).Length
        }
        container = [ordered]@{
            format = $containerFormat
            geogebra_version = $geogebraVersion
            app = $app
            platform = $platform
            entries = @($entries)
        }
        scripts = [ordered]@{
            global_javascript = $globalJavascript
            document_geogebrascript_blocks = $documentScriptBlocks
        }
        legacy_toolbar = [ordered]@{
            status = "authoritative-legacy-reference"
            future_constraint = $false
            raw_definition = $toolbarDefinition
            custom_groups = @($customGroups)
        }
        tools = @($tools)
    }
}

try {
    $sourcePath = Resolve-RepositoryPath -Path $Source -RequireFile
    $extension = [IO.Path]::GetExtension($sourcePath).ToLowerInvariant()
    if ($AllowedExtensions -notcontains $extension) {
        throw "Unsupported legacy resource extension: $extension"
    }

    if (($Import -or $Check) -and [string]::IsNullOrWhiteSpace($TargetDirectory)) {
        throw "-TargetDirectory is required with -Import or -Check."
    }

    $originalPath = $sourcePath
    $targetPath = $null
    if (-not [string]::IsNullOrWhiteSpace($TargetDirectory)) {
        $targetPath = Resolve-RepositoryPath -Path $TargetDirectory
        if (-not $targetPath.StartsWith(
                $LegacyRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
            throw "TargetDirectory must be below models/legacy: $TargetDirectory"
        }
        $originalDirectory = Join-Path $targetPath "original"
        $originalPath = Join-Path $originalDirectory ([IO.Path]::GetFileName($sourcePath))
        if ($Import) {
            New-Item -ItemType Directory -Path $originalDirectory -Force | Out-Null
            if (Test-Path -LiteralPath $originalPath -PathType Leaf) {
                $sourceHash = (Get-FileHash -LiteralPath $sourcePath `
                    -Algorithm SHA256).Hash.ToLowerInvariant()
                $existingHash = (Get-FileHash -LiteralPath $originalPath `
                    -Algorithm SHA256).Hash.ToLowerInvariant()
                if ($sourceHash -ne $existingHash) {
                    throw "Refusing to overwrite a different original: $originalPath"
                }
            } else {
                Copy-Item -LiteralPath $sourcePath -Destination $originalPath
            }
        } elseif (-not (Test-Path -LiteralPath $originalPath -PathType Leaf)) {
            throw "Registered original does not exist: $originalPath"
        }
    }

    $artifactHash = (Get-FileHash -LiteralPath $originalPath `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    if ([string]::IsNullOrWhiteSpace($CurationPath) -and $null -ne $targetPath) {
        $candidate = Join-Path $targetPath "curation.yml"
        if (Test-Path -LiteralPath $candidate -PathType Leaf) {
            $CurationPath = $candidate
        }
    }
    $curationRecord = Read-Curation -Path $CurationPath -ExpectedHash $artifactHash
    $inventory = New-Inventory -OriginalPath $originalPath `
        -ArtifactHash $artifactHash -CurationRecord $curationRecord
    $inventoryText = $inventory | ConvertTo-Json -Depth 100
    $inventoryText += [Environment]::NewLine

    if ([string]::IsNullOrWhiteSpace($InventoryPath) -and $null -ne $targetPath) {
        $InventoryPath = Join-Path $targetPath "derived\tool-inventory.yml"
    }

    if ($Import) {
        $absoluteInventory = Resolve-RepositoryPath -Path $InventoryPath
        $targetPrefix = $targetPath.TrimEnd([IO.Path]::DirectorySeparatorChar) +
            [IO.Path]::DirectorySeparatorChar
        if (-not $absoluteInventory.StartsWith(
                $targetPrefix, [StringComparison]::OrdinalIgnoreCase)) {
            throw "InventoryPath must remain below TargetDirectory: $InventoryPath"
        }
        $inventoryDirectory = Split-Path -Parent $absoluteInventory
        New-Item -ItemType Directory -Path $inventoryDirectory -Force | Out-Null
        if (Test-Path -LiteralPath $absoluteInventory -PathType Leaf) {
            $existing = [IO.File]::ReadAllText($absoluteInventory)
            if ($existing -ne $inventoryText) {
                [IO.File]::WriteAllText($absoluteInventory, $inventoryText, $Utf8NoBom)
            }
        } else {
            [IO.File]::WriteAllText($absoluteInventory, $inventoryText, $Utf8NoBom)
        }
        Write-Host "Imported: $(Get-RepositoryRelativePath -Path $originalPath)"
        Write-Host "SHA-256: $artifactHash"
        Write-Host "Inventory: $(Get-RepositoryRelativePath -Path $absoluteInventory)"
    } elseif ($Check) {
        $absoluteInventory = Resolve-RepositoryPath -Path $InventoryPath -RequireFile
        $targetPrefix = $targetPath.TrimEnd([IO.Path]::DirectorySeparatorChar) +
            [IO.Path]::DirectorySeparatorChar
        if (-not $absoluteInventory.StartsWith(
                $targetPrefix, [StringComparison]::OrdinalIgnoreCase)) {
            throw "InventoryPath must remain below TargetDirectory: $InventoryPath"
        }
        $existing = [IO.File]::ReadAllText($absoluteInventory)
        if ($existing -ne $inventoryText) {
            throw "Derived inventory is stale: $(Get-RepositoryRelativePath -Path $absoluteInventory)"
        }
        Write-Host "Legacy ingest check passed: $ResourceId"
        Write-Host "SHA-256: $artifactHash"
    } else {
        Write-Output $inventoryText.TrimEnd()
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

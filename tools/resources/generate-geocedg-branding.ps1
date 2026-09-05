<#
.SYNOPSIS
Promotes the approved G9U1 Round-3 author artwork and derives runtime assets.

.DESCRIPTION
The two ignored author inputs remain immutable ingestion evidence. Generation
checks their exact SHA-256 and dimensions, copies their bytes into the tracked
versioned resource tree, and creates only the bounded Desktop/Windows assets.
Verification starts from those tracked promoted sources, so a clean checkout
does not depend on ignored ingestion files. All resizing uses contain
semantics: aspect ratio is preserved, no source pixel is cropped, and any
added square-icon padding is transparent.

.PARAMETER VerifyOnly
Validate the tracked promoted sources, generate every derivative in memory,
and compare it byte-for-byte with the tracked resources instead of writing
files. Ignored author inputs are neither required nor read in this mode.
#>
[CmdletBinding()]
param(
    [switch]$VerifyOnly,
    [string]$AuthorInputDirectory,
    [string]$DestinationDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
if ([string]::IsNullOrWhiteSpace($AuthorInputDirectory)) {
    $AuthorInputDirectory = Join-Path $RepositoryRoot `
        "artifacts\author-input\g9u1-branding"
} else {
    $AuthorInputDirectory = [IO.Path]::GetFullPath($AuthorInputDirectory)
}
if ([string]::IsNullOrWhiteSpace($DestinationDirectory)) {
    $DestinationDirectory = Join-Path $RepositoryRoot `
        "source\desktop\desktop\src\main\resources\org\geocedg\desktop\branding\v1"
} else {
    $DestinationDirectory = [IO.Path]::GetFullPath($DestinationDirectory)
}

$ExpectedDestination = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
    "source\desktop\desktop\src\main\resources\org\geocedg\desktop\branding\v1"))
if ($DestinationDirectory -ne $ExpectedDestination) {
    throw "Brand resources must be generated at the versioned Desktop authority: $ExpectedDestination"
}

Add-Type -AssemblyName System.Drawing

$Inputs = @(
    [ordered]@{
        Name = "helixTopBar.png"
        Sha256 = "08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1"
        Width = 969
        Height = 815
    },
    [ordered]@{
        Name = "helixSnapshot.png"
        Sha256 = "abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637"
        Width = 1197
        Height = 1591
    }
)
$IconSizes = @(16, 24, 32, 48, 64, 128, 256)

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Get-LowerSha256 {
    param([Parameter(Mandatory)] [string]$Path)

    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Read-CheckedSource {
    param(
        [Parameter(Mandatory)] [Collections.IDictionary]$Definition,
        [Parameter(Mandatory)] [string]$Directory
    )

    $path = Join-Path $Directory ([string]$Definition.Name)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Required author input is missing: $path"
    Assert-Condition -Condition ((Get-LowerSha256 $path) -ceq $Definition.Sha256) `
        -Message "Author input SHA-256 differs: $path"
    $image = [Drawing.Image]::FromFile($path)
    try {
        Assert-Condition -Condition (
            $image.Width -eq $Definition.Width -and
            $image.Height -eq $Definition.Height) `
            -Message "Author input dimensions differ: $path"
    } finally {
        $image.Dispose()
    }
    return [IO.File]::ReadAllBytes($path)
}

function New-ContainedPngBytes {
    param(
        [Parameter(Mandatory)] [byte[]]$SourceBytes,
        [Parameter(Mandatory)] [int]$CanvasWidth,
        [Parameter(Mandatory)] [int]$CanvasHeight
    )

    $inputStream = [IO.MemoryStream]::new($SourceBytes, $false)
    try {
        $source = [Drawing.Image]::FromStream($inputStream, $false, $true)
        try {
            $scale = [Math]::Min(
                [double]$CanvasWidth / $source.Width,
                [double]$CanvasHeight / $source.Height)
            $drawWidth = [Math]::Max(1, [int][Math]::Round(
                $source.Width * $scale, [MidpointRounding]::AwayFromZero))
            $drawHeight = [Math]::Max(1, [int][Math]::Round(
                $source.Height * $scale, [MidpointRounding]::AwayFromZero))
            $left = [int][Math]::Floor(($CanvasWidth - $drawWidth) / 2.0)
            $top = [int][Math]::Floor(($CanvasHeight - $drawHeight) / 2.0)

            $bitmap = [Drawing.Bitmap]::new(
                $CanvasWidth, $CanvasHeight,
                [Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                $bitmap.SetResolution(96.0, 96.0)
                $graphics = [Drawing.Graphics]::FromImage($bitmap)
                try {
                    $graphics.CompositingMode =
                        [Drawing.Drawing2D.CompositingMode]::SourceCopy
                    $graphics.CompositingQuality =
                        [Drawing.Drawing2D.CompositingQuality]::HighQuality
                    $graphics.InterpolationMode =
                        [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                    $graphics.PixelOffsetMode =
                        [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
                    $graphics.SmoothingMode =
                        [Drawing.Drawing2D.SmoothingMode]::HighQuality
                    $graphics.Clear([Drawing.Color]::Transparent)
                    $attributes = [Drawing.Imaging.ImageAttributes]::new()
                    try {
                        $attributes.SetWrapMode(
                            [Drawing.Drawing2D.WrapMode]::TileFlipXY)
                        $destination = [Drawing.Rectangle]::new(
                            $left, $top, $drawWidth, $drawHeight)
                        $graphics.DrawImage(
                            $source, $destination, 0, 0,
                            $source.Width, $source.Height,
                            [Drawing.GraphicsUnit]::Pixel, $attributes)
                    } finally {
                        $attributes.Dispose()
                    }
                } finally {
                    $graphics.Dispose()
                }
                $output = [IO.MemoryStream]::new()
                try {
                    $bitmap.Save($output, [Drawing.Imaging.ImageFormat]::Png)
                    return ,$output.ToArray()
                } finally {
                    $output.Dispose()
                }
            } finally {
                $bitmap.Dispose()
            }
        } finally {
            $source.Dispose()
        }
    } finally {
        $inputStream.Dispose()
    }
}

function New-PngEmbeddedIcoBytes {
    param([Parameter(Mandatory)] [Collections.IDictionary]$PngBySize)

    $orderedSizes = @($PngBySize.Keys | Sort-Object { [int]$_ })
    $headerLength = 6 + 16 * $orderedSizes.Count
    $offset = $headerLength
    $stream = [IO.MemoryStream]::new()
    $writer = [IO.BinaryWriter]::new($stream)
    try {
        $writer.Write([uint16]0)
        $writer.Write([uint16]1)
        $writer.Write([uint16]$orderedSizes.Count)
        foreach ($sizeKey in $orderedSizes) {
            $size = [int]$sizeKey
            [byte[]]$png = $PngBySize[$sizeKey]
            $writer.Write([byte]($(if ($size -eq 256) { 0 } else { $size })))
            $writer.Write([byte]($(if ($size -eq 256) { 0 } else { $size })))
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([uint16]1)
            $writer.Write([uint16]32)
            $writer.Write([uint32]$png.Length)
            $writer.Write([uint32]$offset)
            $offset += $png.Length
        }
        foreach ($sizeKey in $orderedSizes) {
            $writer.Write([byte[]]$PngBySize[$sizeKey])
        }
        $writer.Flush()
        return ,$stream.ToArray()
    } finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

function Publish-OrVerify {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [byte[]]$Bytes
    )

    $path = Join-Path $DestinationDirectory $RelativePath
    if ($VerifyOnly) {
        Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Tracked brand resource is missing: $path"
        $actual = [IO.File]::ReadAllBytes($path)
        $actualHash = [Convert]::ToHexString(
            [Security.Cryptography.SHA256]::HashData($actual))
        $expectedHash = [Convert]::ToHexString(
            [Security.Cryptography.SHA256]::HashData($Bytes))
        Assert-Condition -Condition (
            $actual.Length -eq $Bytes.Length -and
            $actualHash -ceq $expectedHash) `
            -Message "Tracked brand resource is not reproducible: $path"
    } else {
        $parent = Split-Path -Parent $path
        [void](New-Item -ItemType Directory -Path $parent -Force)
        [IO.File]::WriteAllBytes($path, $Bytes)
    }
    $hash = [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
    Write-Host "$hash  $RelativePath"
}

$sourceAuthorityDirectory = if ($VerifyOnly) {
    Join-Path $DestinationDirectory "source"
} else {
    $AuthorInputDirectory
}
$topBar = Read-CheckedSource -Definition $Inputs[0] `
    -Directory $sourceAuthorityDirectory
$snapshot = Read-CheckedSource -Definition $Inputs[1] `
    -Directory $sourceAuthorityDirectory

Publish-OrVerify -RelativePath "source\helixTopBar.png" -Bytes $topBar
Publish-OrVerify -RelativePath "source\helixSnapshot.png" -Bytes $snapshot

$iconPngs = [ordered]@{}
foreach ($size in $IconSizes) {
    $iconPngs[[string]$size] = New-ContainedPngBytes -SourceBytes $topBar `
        -CanvasWidth $size -CanvasHeight $size
}
Publish-OrVerify -RelativePath "derived\geocedg-application-icon-64.png" `
    -Bytes ([byte[]]$iconPngs["64"])
Publish-OrVerify -RelativePath "derived\geocedg-application.ico" `
    -Bytes (New-PngEmbeddedIcoBytes -PngBySize $iconPngs)

$splash = New-ContainedPngBytes -SourceBytes $snapshot `
    -CanvasWidth 542 -CanvasHeight 720
Publish-OrVerify -RelativePath "derived\geocedg-startup-542x720.png" `
    -Bytes $splash

Write-Host "Branding resources $($(if ($VerifyOnly) { 'verified' } else { 'generated' }))."

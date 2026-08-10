[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verify-dxf")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$SharedResult = Join-Path $RepositoryRoot (
    "source\shared\common-jre\build\test-results\test\" +
    "TEST-org.geocedg.common.export.GeometryExportFoundationTest.xml")
$DesktopResult = Join-Path $RepositoryRoot (
    "source\desktop\desktop\build\test-results\test\" +
    "TEST-org.geocedg.desktop.GeoCeDGProfileTest.xml")

function Get-RepositoryStatus {
    $status = & git -C $RepositoryRoot status --porcelain=v1 --untracked-files=all
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read repository status."
    }
    return ($status -join "`n")
}

function Get-OutputDirectories {
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
            [void]$paths.Add([IO.Path]::GetFullPath((Join-Path $RepositoryRoot $relative)))
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

    if ($KeepBuildOutputs) {
        Write-Host "Keeping G5 build outputs because -KeepBuildOutputs was supplied."
        return
    }
    $after = Get-OutputDirectories
    $rootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar
    $selected = [Collections.Generic.List[string]]::new()
    foreach ($candidate in @($after | Where-Object { -not $Before.Contains($_) } |
            Sort-Object Length)) {
        $covered = @($selected | Where-Object {
                $candidate.StartsWith(
                    $_ + [IO.Path]::DirectorySeparatorChar,
                    [StringComparison]::OrdinalIgnoreCase)
            }).Count -gt 0
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
            Write-Host "Removing generated G5 output: $candidate"
            Remove-Item -LiteralPath $candidate -Recurse -Force
        }
    }
}

function Assert-Condition {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

function Read-JsonDocument {
    param([Parameter(Mandatory)] [string]$RelativePath)
    $path = Join-Path $RepositoryRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required G5 file is missing: $RelativePath"
    }
    try {
        return Get-Content -Raw -LiteralPath $path |
            ConvertFrom-Json -Depth 100 -NoEnumerate
    } catch {
        throw "$RelativePath is not valid JSON-compatible YAML: $($_.Exception.Message)"
    }
}

function Invoke-LoggedGradle {
    param([Parameter(Mandatory)] [string[]]$Arguments)
    $logPath = Join-Path $LogDirectory "g5-dxf-gradle.log"
    Write-Host "`n==> G5 focused tests and checkstyle"
    Write-Host "    log: $logPath"
    $exitCode = -1
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($exitCode -ne 0) {
        throw "G5 Gradle validation failed with exit code $exitCode. See $logPath"
    }
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description did not produce $Path"
    }
    [xml]$result = Get-Content -Raw -LiteralPath $Path
    $suite = $result.testsuite
    if ([int]$suite.tests -ne $ExpectedTests -or [int]$suite.failures -ne 0 -or
            [int]$suite.errors -ne 0 -or [int]$suite.skipped -ne 0) {
        throw "$Description is not clean: tests=$($suite.tests), " +
            "failures=$($suite.failures), errors=$($suite.errors), " +
            "skipped=$($suite.skipped)."
    }
    Write-Host "${Description}: $($suite.tests) tests, 0 failures."
}

$InitialStatus = $null
$InitialOutputs = $null
[Exception]$Failure = $null

try {
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null
    $InitialStatus = Get-RepositoryStatus
    $InitialOutputs = Get-OutputDirectories

    foreach ($required in @(
            "docs/adr/0005-neutral-2d-geometry-export.md",
            "geocedg/specs/export/geometry-export-foundation.md",
            "models/regression/g5-dxf-foundation/construction.ggs",
            "models/regression/g5-dxf-foundation/expected-entities.yml",
            "models/regression/g5-dxf-foundation/manifest.yml",
            "source/shared/common/src/main/java/org/geocedg/common/export/GeometryExportModel.java",
            "source/shared/common/src/main/java/org/geocedg/common/export/GeoElementGeometryExportAdapter.java",
            "source/shared/common/src/main/java/org/geocedg/common/export/DxfExporter.java",
            "source/shared/common/src/main/java/org/geocedg/common/export/GeometryExportService.java",
            "source/shared/common-jre/src/test/java/org/geocedg/common/export/GeometryExportFoundationTest.java",
            "source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDxfExportController.java")) {
        Assert-Condition -Condition (Test-Path -LiteralPath (
                Join-Path $RepositoryRoot $required) -PathType Leaf) `
            -Message "Required G5 file is missing: $required"
    }

    $experimental = Read-JsonDocument "geocedg/features/experimental.yml"
    $dxfFeatures = @($experimental.features | Where-Object {
            $_.id -eq "cedg.export.dxf.2d" })
    Assert-Condition -Condition ($dxfFeatures.Count -eq 1 -and
            $dxfFeatures[0].maturity -eq "experimental" -and
            $dxfFeatures[0].enabled_by_default) `
        -Message "The experimental DXF feature manifest is invalid."

    $profile = Read-JsonDocument "apps/geocedg/application-profile.yml"
    Assert-Condition -Condition (@($profile.features) -contains
            "cedg.export.dxf.2d") `
        -Message "The GeoCeDG application profile does not register G5 DXF."

    $regression = Read-JsonDocument "geocedg/validation/regression/catalog.yml"
    $cases = @($regression.cases | Where-Object {
            $_.id -eq "cedg.regression.g5-dxf-foundation" })
    Assert-Condition -Condition ($cases.Count -eq 1 -and
            $cases[0].expected.comparison -eq "semantic-dxf-entities") `
        -Message "The G5 semantic regression catalog entry is invalid."

    $expected = Read-JsonDocument (
        "models/regression/g5-dxf-foundation/expected-entities.yml")
    Assert-Condition -Condition ($expected.format.acad_version -eq "AC1015" -and
            $expected.format.insunits -eq 0 -and
            $expected.exact_entities -eq 9 -and
            $expected.approximate_entities -eq 0 -and
            $expected.unsupported_entities -eq 1 -and
            $expected.invariants.zoom_invariant -and
            $expected.invariants.source_correspondence) `
        -Message "The G5 expected semantic evidence is invalid."

    $writerPath = Join-Path $RepositoryRoot `
        "source/shared/common/src/main/java/org/geocedg/common/export/DxfExporter.java"
    $writer = Get-Content -Raw -LiteralPath $writerPath
    foreach ($forbidden in @("org.geogebra", "GeoElement", "EuclidianView",
            "Kernel", "JFileChooser", "java.io.File")) {
        Assert-Condition -Condition (-not $writer.Contains($forbidden)) `
            -Message "DxfExporter crosses the neutral boundary through '$forbidden'."
    }
    Assert-Condition -Condition ($writer.Contains('ACAD_VERSION = "AC1015"')) `
        -Message "DxfExporter does not pin AC1015."

    $classicMenus = & git -C $RepositoryRoot grep -n -i "DXF" -- `
        "source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar"
    Assert-Condition -Condition ($LASTEXITCODE -eq 1 -and
            @($classicMenus).Count -eq 0) `
        -Message "Classic menu sources unexpectedly contain the GeoCeDG DXF action."

    if (-not $SkipBuild) {
        $arguments = @(
            ":shared:common-jre:test", "--tests",
            "org.geocedg.common.export.GeometryExportFoundationTest",
            ":desktop:desktop:test", "--tests",
            "org.geocedg.desktop.GeoCeDGProfileTest",
            ":shared:common:checkstyleMain",
            ":shared:common-jre:checkstyleTest",
            ":desktop:desktop:checkstyleMain",
            ":desktop:desktop:checkstyleTest",
            "--rerun-tasks", "--no-build-cache", "--no-daemon",
            "--no-problems-report", "--console=plain"
        )
        if (-not $AllowToolchainDownload) {
            $arguments += "-Dorg.gradle.java.installations.auto-download=false"
        }
        Invoke-LoggedGradle -Arguments $arguments
        Assert-TestResult -Path $SharedResult -Description "G5 shared tests" `
            -ExpectedTests 5
        Assert-TestResult -Path $DesktopResult -Description "G5 Desktop profile tests" `
            -ExpectedTests 5
    } else {
        Write-Host "Skipping G5 Gradle validation because -SkipBuild was supplied."
    }

    Write-Host "G5 DXF verification passed."
} catch {
    $Failure = $_.Exception
} finally {
    if ($null -ne $InitialOutputs) {
        try {
            Remove-NewOutputDirectories -Before $InitialOutputs
        } catch {
            if ($null -eq $Failure) {
                $Failure = $_.Exception
            } else {
                Write-Error "G5 cleanup also failed: $($_.Exception.Message)"
            }
        }
    }
}

if ($null -ne $Failure) {
    Write-Error $Failure.Message
    exit 1
}

$FinalStatus = Get-RepositoryStatus
if ($FinalStatus -ne $InitialStatus) {
    Write-Error "Repository status changed during G5 verification."
    exit 1
}

exit 0

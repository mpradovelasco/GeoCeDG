[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9a1-spatial-identity")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$ExpectedBranch = "feature/g9a1-spatial-identity-persistence-foundation"
$EntrySha = "001f7920a1154b09a22b54c190f7bc5f94b48e90"
$G9A1TagName = "geocedg-g9a1-pass"
$G8Commit = "e7810171179825a03b22d8c6eba28c672f468281"
$G8TagObject = "fed1bfbeea77a48acce285429b397eda77054df1"
$G8TagName = "geocedg-g8-pass"
$G9PCommit = "94f92f49a44560e44bae9e75ba52595067471368"
$G9PTagObject = "6ce37f03df6f742aa448323d2150dd1655c986a5"
$G9PTagName = "geocedg-g9p-pass"
$G9O1Commit = "3afabdddcabab4d5bbcd7bc8f34dfa6354a356ac"
$G9O1TagObject = "469bc098c89ec1e3a8ea138341c0c6f027d9605b"
$G9O1TagName = "geocedg-g9o1-pass"
$PromptPath =
    ".github/prompts/tasks/g9a1-spatial-identity-persistence-foundation.prompt.md"
$PromptSha256 =
    "50c665a399b7b6290b8dcf86cc2326bb78202d85d7b52b130fd8ebf2980127e1"
$SpecificationPath =
    "geocedg/specs/spatial/g9-spatial-projection-semantics.md"
$SpecificationSha256 =
    "11e1327a6518a25178133a1bfc0720a6d73adabab7d127b5203b6da86b25ca56"
$Adr10Path =
    "docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md"
$Adr10Sha256 =
    "25b85c8f29488df3c313f3a1e67cea1cb25714253aa01d13625f8791ad20586d"
$Adr11Path = "docs/adr/0011-g9-spatial-persistence-and-phase-gates.md"
$Adr11Sha256 =
    "42fd3fdc0a7493f6bde28c1ba2c597e093e138b14fd73619204cb22d001ebf41"
$EvidencePath = "docs/validation/g9a1_spatial_identity_evidence.json"
$ExpectedFocusedTests = @(
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityIdTest"
        tests = 5
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityLifecycleTest"
        tests = 6
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityMacroTest"
        tests = 2
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityRedefineHostTest"
        tests = 12
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityRegistryTest"
        tests = 15
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialIdentityXmlTest"
        tests = 11
    },
    [pscustomobject]@{
        name = "org.geocedg.common.spatial.G9A1SpatialRedefineTransactionTest"
        tests = 11
    }
)
$ExpectedFocusedTotal = 62
$ExpectedRedefineTests = 55
$ExpectedCombinedTotal = 117
$ExpectedHostParserClasses = @(
    "org.geocedg.common.spatial.G9A1SpatialIdentityLifecycleTest",
    "org.geocedg.common.spatial.G9A1SpatialIdentityMacroTest",
    "org.geocedg.common.spatial.G9A1SpatialIdentityXmlTest"
)
$ExpectedFixturePaths = @(
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/complete-forward-closure.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/cross-kind-token.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/duplicate-id.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/future-version.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/legacy-no-identities.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/malformed-id.xml",
    "source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/missing-reference.xml"
)
$ExpectedSupportingPaths = @(
    "docs/architecture/g9_spatial_persistence_and_upstream_impact.md",
    "docs/architecture/g9a1_spatial_identity_persistence_design.md",
    "docs/roadmap/geocedg_roadmap.md",
    "docs/upstream/modified-files.yml",
    "docs/validation/g9a1_spatial_identity_evidence.json",
    "docs/validation/g9a1_spatial_identity_evidence.sha256",
    "docs/validation/g9a1_spatial_identity_persistence_report.md",
    "geocedg/features/experimental.yml",
    "tools/agent/verify-g8a-intersections.ps1",
    "tools/agent/verify-g9a1-spatial-identity.ps1",
    "tools/agent/verify.ps1"
)
$RedefineTestPath =
    "source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/RedefineTest.java"
$TestResultRoot = Join-Path $RepositoryRoot `
    "source\shared\common-jre\build\test-results\test"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$InitialStatus = $null
$GeneratedSnapshot = $null
$AuthorityCommit = $null

$AllowedHostProductivePaths = @(
    "source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/ConsElementXMLHandler.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java",
    "source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/Macro.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoDispatcher.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoMacro.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/AlgebraProcessor.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/EvalInfo.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/ParametricProcessor.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/geos/PolygonFactory.java",
    "source/shared/common/src/main/java/org/geogebra/common/main/undo/UndoManager.java",
    "source/shared/common/src/main/java/org/geogebra/common/main/undo/UndoableDeletionExecutor.java",
    "source/shared/common/src/main/java/org/geogebra/common/util/InternalClipboard.java"
)

. $GeneratedStateHelper

function Assert-Condition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-RequiredFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot `
        $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)))
    $rootPrefix = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $rootPrefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes repository: $RelativePath"
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9A1 artifact is missing: $RelativePath"
    return $absolute
}

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $bytes = [IO.File]::ReadAllBytes((Resolve-RequiredFile `
            -RelativePath $RelativePath))
    $offset = 0
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        $bytes, $offset, $bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $canonicalBytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha.ComputeHash($canonicalBytes)).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Read-JsonFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    try {
        return Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
                -RelativePath $RelativePath) | ConvertFrom-Json -Depth 100
    } catch {
        throw "Invalid JSON in ${RelativePath}: $($_.Exception.Message)"
    }
}

function Get-CandidateChangedPaths {
    if ($null -ne $AuthorityCommit) {
        $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
            $EntrySha $AuthorityCommit --)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to enumerate frozen G9A1 changes."
        return @($paths | Where-Object {
                -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
                $_.Replace("\", "/") } | Sort-Object -Unique)
    }

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked G9A1 changes."
    $paths += @(& git -C $RepositoryRoot ls-files --others --exclude-standard)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked G9A1 changes."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
            $_.Replace("\", "/") } | Sort-Object -Unique)
}

function Assert-ExactPathSet {
    param(
        [Parameter(Mandatory)] [string[]]$Actual,
        [Parameter(Mandatory)] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualSorted = @($Actual | Sort-Object -Unique)
    $expectedSorted = @($Expected | Sort-Object -Unique)
    Assert-Condition -Condition ($actualSorted.Count -eq $Actual.Count) `
        -Message "$Description actual path set contains duplicates."
    Assert-Condition -Condition ($expectedSorted.Count -eq $Expected.Count) `
        -Message "$Description evidence path set contains duplicates."
    $actualText = $actualSorted -join "`n"
    $expectedText = $expectedSorted -join "`n"
    Assert-Condition -Condition ($actualText -ceq $expectedText) `
        -Message "$Description differs from evidence.`nActual:`n$actualText`nExpected:`n$expectedText"
}

function Assert-GitTagAnchor {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$Object,
        [Parameter(Mandatory)] [string]$Commit
    )

    $ref = (& git -C $RepositoryRoot rev-parse "refs/tags/$Name").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $ref -eq $Object) `
        -Message "$Name does not resolve to its approved annotated tag object."
    $type = (& git -C $RepositoryRoot cat-file -t $Object).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $type -eq "tag") `
        -Message "$Name is not an annotated tag object."
    $peeled = (& git -C $RepositoryRoot rev-parse "$Object^{}").Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and $peeled -eq $Commit) `
        -Message "$Name does not peel to its approved commit."
    & git -C $RepositoryRoot merge-base --is-ancestor $Commit HEAD
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Current HEAD does not descend from $Name."
}

function Assert-SupportingPath {
    param([Parameter(Mandatory)] [string]$Path)

    $allowed = $Path -eq "docs/upstream/modified-files.yml" -or
        $Path -eq "geocedg/features/experimental.yml" -or
        $Path -eq "tools/agent/verify-g8a-intersections.ps1" -or
        $Path -eq "tools/agent/verify-g9a1-spatial-identity.ps1" -or
        $Path -eq "tools/agent/verify.ps1" -or
        $Path -eq `
            "docs/architecture/g9_spatial_persistence_and_upstream_impact.md" -or
        $Path -match '^docs/architecture/g9a1_[^/]+\.md$' -or
        $Path -match '^docs/roadmap/(g9[^/]*|geocedg_roadmap)\.md$' -or
        $Path -match '^docs/validation/g9a1_[^/]+\.(json|md|sha256)$'
    Assert-Condition -Condition $allowed `
        -Message "Supporting path is outside the G9A1 boundary: $Path"
}

function Assert-SourceBoundary {
    param(
        [Parameter(Mandatory)] [object]$Evidence,
        [Parameter(Mandatory)] [string[]]$ChangedPaths
    )

    $productive = @($Evidence.sourceBoundary.productivePaths)
    $tests = @($Evidence.sourceBoundary.testPaths)
    $fixtures = @($Evidence.sourceBoundary.fixturePaths)
    $supporting = @($Evidence.sourceBoundary.supportingPaths)
    Assert-Condition -Condition ($productive.Count -gt 0 -and
            $tests.Count -gt 0 -and $fixtures.Count -gt 0 -and
            $supporting.Count -gt 0) `
        -Message "G9A1 evidence path groups must all be nonempty."
    $declared = @($productive + $tests + $fixtures + $supporting)
    Assert-ExactPathSet -Actual $ChangedPaths -Expected $declared `
        -Description "G9A1 candidate path set"

    $expectedTestPaths = @($ExpectedFocusedTests | ForEach-Object {
            "source/shared/common-jre/src/test/java/{0}.java" -f `
                $_.name.Replace(".", "/")
        })
    Assert-ExactPathSet -Actual $tests -Expected $expectedTestPaths `
        -Description "G9A1 focused test source set"
    Assert-ExactPathSet -Actual $fixtures -Expected $ExpectedFixturePaths `
        -Description "G9A1 XML fixture set"
    Assert-ExactPathSet -Actual $supporting -Expected $ExpectedSupportingPaths `
        -Description "G9A1 supporting artifact set"

    foreach ($path in $productive) {
        $isAdditive = $path -match `
            '^source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/[^/]+(?:/[^/]+)*\.java$'
        Assert-Condition -Condition ($isAdditive -or
                $path -in $AllowedHostProductivePaths) `
            -Message "Unapproved productive G9A1 path: $path"
    }
    foreach ($path in $tests) {
        Assert-Condition -Condition ($path -match `
                '^source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A1[^/]*\.java$') `
            -Message "G9A1 test is outside its private test package: $path"
    }
    foreach ($path in $fixtures) {
        Assert-Condition -Condition ($path -match `
                '^source/shared/common-jre/src/test/resources/org/geocedg/common/spatial/g9a1/[^/]+\.(xml|ggb)$') `
            -Message "G9A1 fixture is outside its private resource package: $path"
    }
    foreach ($path in $supporting) {
        Assert-SupportingPath -Path $path
    }

    foreach ($path in $ChangedPaths) {
        foreach ($forbiddenPattern in @(
                '^apps/', '^python/', '^packaging/', '^source/web/',
                '/(?:locus|export|euclidian|geogebra3D|kernel3D)/',
                'g9a[2-9]', 'g9[b-z][0-9]?')) {
            Assert-Condition -Condition ($path -notmatch $forbiddenPattern) `
                -Message "Forbidden G9A1 scope path: $path"
        }
    }

    $registeredManifest = Read-JsonFile `
        -RelativePath "docs/upstream/modified-files.yml"
    $registered = @($registeredManifest.modifications |
        ForEach-Object { $_.path })
    Assert-Condition -Condition (@($registered | Group-Object |
            Where-Object Count -gt 1).Count -eq 0) `
        -Message "The upstream modified-file inventory contains duplicates."
    foreach ($path in @($ChangedPaths | Where-Object {
                $_.StartsWith("source/") })) {
        Assert-Condition -Condition ($path -in $registered) `
            -Message "Changed source/test/fixture path is not registered: $path"
    }

    $addedProductiveLines = [Collections.Generic.List[string]]::new()
    $addedProductiveEntries = [Collections.Generic.List[object]]::new()
    foreach ($path in $productive) {
        & git -C $RepositoryRoot ls-files --error-unmatch -- $path 2>$null |
            Out-Null
        if ($LASTEXITCODE -eq 0) {
            $diffArguments = @("diff", "--unified=0", $EntrySha)
            if ($null -ne $AuthorityCommit) {
                $diffArguments += $AuthorityCommit
            }
            $diffArguments += @("--", $path)
            foreach ($line in @(& git -C $RepositoryRoot @diffArguments)) {
                if ($line.StartsWith("+") -and
                        -not $line.StartsWith("+++")) {
                    $productiveLine = $line.Substring(1)
                    $addedProductiveLines.Add($productiveLine)
                    $addedProductiveEntries.Add([pscustomobject]@{
                            path = $path
                            text = $productiveLine
                        })
                }
            }
            Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
                -Message "Unable to inspect productive G9A1 diff: $path"
        } else {
            foreach ($line in @(Get-Content -LiteralPath (
                    Resolve-RequiredFile -RelativePath $path))) {
                $addedProductiveLines.Add($line)
                $addedProductiveEntries.Add([pscustomobject]@{
                        path = $path
                        text = $line
                    })
            }
        }
    }
    $addedProductiveText = $addedProductiveLines -join "`n"
    foreach ($labelAccess in @($addedProductiveEntries | Where-Object {
                $_.text -match `
                    '\.(?:getLabel(?:Simple)?|lookupLabel)\s*\('
            })) {
        $trimmed = $labelAccess.text.Trim()
        $isLegacyRedefineFallback = $labelAccess.path -eq `
            "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/AlgebraProcessor.java" -and
            $trimmed -in @(
                "? newGeo.getLabelSimple()",
                ": replaceable.getLabelSimple();",
                "ret[0] = kernel.lookupLabel(newLabel);")
        $isHostInsertOverwrite = $labelAccess.path -eq `
            "source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java" -and
            $trimmed -eq `
                "GeoElement toRemove = toApp.getKernel().lookupLabel(label);"
        Assert-Condition -Condition ($isLegacyRedefineFallback -or
                $isHostInsertOverwrite) `
            -Message "Unapproved label access in productive G9A1 code: $($labelAccess.path): $trimmed"
    }
    foreach ($forbiddenAuthority in @(
            '\bceID\b', '\.getConstructionIndex\s*\(', '\.getLayer\s*\(',
            'import\s+org\.geogebra\.common\.euclidian',
            'import\s+.*\.GeoLocus\s*;',
            '(?:extends|new)\s+AlgoElement\b',
            'import\s+.*\.Geo(?:Point|Line|Plane)3D\s*;',
            '\bLocusRenderCache\w*\b', '\bDXF\w*\b')) {
        Assert-Condition -Condition ($addedProductiveText -notmatch
                $forbiddenAuthority) `
            -Message "Forbidden G9A1 identity/geometry authority pattern: $forbiddenAuthority"
    }
}

function Assert-TestResult {
    param(
        [Parameter(Mandatory)] [string]$ClassName,
        [Parameter(Mandatory)] [int]$ExpectedTests
    )

    $path = Join-Path $TestResultRoot ("TEST-{0}.xml" -f $ClassName)
    Assert-Condition -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
        -Message "Missing G9A1 JUnit result: $path"
    [xml]$result = Get-Content -Raw -LiteralPath $path
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $ExpectedTests -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message "G9A1 JUnit result is not clean: $ClassName"
    Write-Host "${ClassName}: $ExpectedTests tests, 0 failures/errors/skips."
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $path = Resolve-RequiredFile -RelativePath $RelativePath
    $errors = @(Select-String -LiteralPath $path -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "G9A1 Checkstyle result contains $($errors.Count) violations: $RelativePath"
}

function Invoke-G9A1Tests {
    $arguments = @()
    if (-not $AllowToolchainDownload) {
        $arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $arguments += @(
        ":shared:common-jre:test",
        "--tests", "org.geocedg.common.spatial.G9A1*",
        "--tests", "org.geogebra.common.kernel.commands.RedefineTest",
        ":shared:common:checkstyleMain",
        ":shared:common-jre:checkstyleTest",
        "--rerun-tasks", "--no-daemon", "--console=plain",
        "--no-problems-report")
    $logPath = Join-Path $LogDirectory "g9a1-spatial-identity-tests.log"
    $exitCode = $null
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($null -ne $exitCode -and $exitCode -eq 0) `
        -Message "G9A1 Gradle gate failed. See $logPath"
}

try {
    $InitialStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
    if (-not $SkipBuild) {
        $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
            -RepositoryRoot $RepositoryRoot `
            -DirectoryNames $GeneratedDirectoryNames -Label "g9a1"
    }
    [void](New-Item -ItemType Directory -Path $LogDirectory -Force)

    foreach ($required in @(
            $PromptPath, $SpecificationPath, $Adr10Path, $Adr11Path,
            "geocedg/validation/g9p/g9p-design-evidence.json",
            "geocedg/validation/g9o1/g9o1-evidence.json",
            $RedefineTestPath) + $ExpectedSupportingPaths) {
        [void](Resolve-RequiredFile -RelativePath $required)
    }

    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    & git -C $RepositoryRoot show-ref --verify --quiet `
        "refs/tags/$G9A1TagName"
    $g9a1TagExists = $LASTEXITCODE -eq 0
    Assert-Condition -Condition ($g9a1TagExists -or $LASTEXITCODE -eq 1) `
        -Message "Unable to resolve the G9A1 completion tag."
    if ($g9a1TagExists) {
        $g9a1TagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$G9A1TagName").Trim()
        $g9a1TagType = (& git -C $RepositoryRoot cat-file -t `
            $g9a1TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $g9a1TagType -eq "tag") `
            -Message "G9A1 completion tag is missing or not annotated."
        $AuthorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$g9a1TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to peel the G9A1 completion tag."
        & git -C $RepositoryRoot merge-base --is-ancestor $EntrySha `
            $AuthorityCommit
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "G9A1 completion does not descend from its entry baseline."
        & git -C $RepositoryRoot merge-base --is-ancestor $AuthorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not descend from G9A1 completion."
    } else {
        $localMain = (& git -C $RepositoryRoot rev-parse main).Trim()
        $originMain = (& git -C $RepositoryRoot rev-parse origin/main).Trim()
        $originFeature = (& git -C $RepositoryRoot rev-parse `
            "origin/$ExpectedBranch").Trim()
        Assert-Condition -Condition ($branch -eq $ExpectedBranch -and
                $head -eq $EntrySha -and $localMain -eq $EntrySha -and
                $originMain -eq $EntrySha -and $originFeature -eq $EntrySha) `
            -Message "G9A1 branch/HEAD/main/origin entry authority drifted."
    }
    Assert-GitTagAnchor -Name $G8TagName -Object $G8TagObject `
        -Commit $G8Commit
    Assert-GitTagAnchor -Name $G9PTagName -Object $G9PTagObject `
        -Commit $G9PCommit
    Assert-GitTagAnchor -Name $G9O1TagName -Object $G9O1TagObject `
        -Commit $G9O1Commit

    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                -RelativePath $PromptPath) -eq $PromptSha256) `
        -Message "Canonical G9A1 prompt LF SHA-256 mismatch."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                -RelativePath $SpecificationPath) -eq $SpecificationSha256) `
        -Message "Author-approved spatial specification LF SHA-256 mismatch."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                -RelativePath $Adr10Path) -eq $Adr10Sha256) `
        -Message "Accepted ADR 0010 LF SHA-256 mismatch."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
                -RelativePath $Adr11Path) -eq $Adr11Sha256) `
        -Message "Accepted ADR 0011 LF SHA-256 mismatch."

    $specificationText = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $SpecificationPath)
    $adr10Text = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $Adr10Path)
    $adr11Text = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $Adr11Path)
    Assert-Condition -Condition ($specificationText.Contains(
            "NORMATIVE / AUTHOR APPROVED") -and
            $adr10Text.Contains("Status: **Accepted**") -and
            $adr11Text.Contains("Status: **Accepted**")) `
        -Message "G9A1 normative status gate is not author-approved/Accepted."

    $roadmapText = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        -RelativePath "docs/roadmap/geocedg_roadmap.md")
    Assert-Condition -Condition ($roadmapText.Contains(
            "G9A1 = PASS — AUTHOR APPROVED") -and
            $roadmapText.Contains(
                "G9A2 = DESIGNED — NOT AUTHORIZED")) `
        -Message "Living G9A1/G9A2 closeout status is inconsistent."

    $g9pEvidence = Read-JsonFile `
        -RelativePath "geocedg/validation/g9p/g9p-design-evidence.json"
    $g9o1Evidence = Read-JsonFile `
        -RelativePath "geocedg/validation/g9o1/g9o1-evidence.json"
    Assert-Condition -Condition ($g9pEvidence.phase -eq "G9P" -and
            $g9pEvidence.status -eq "G9P_R1_PASS_AUTHOR_APPROVED" -and
            [bool]$g9pEvidence.approval.authorApproved -and
            [bool]$g9pEvidence.approval.specificationsNormative -and
            [bool]$g9pEvidence.approval.adrsAccepted) `
        -Message "G9P/R1 author-approval entry gate is inconsistent."
    Assert-Condition -Condition ($g9o1Evidence.phase -eq "G9O1" -and
            $g9o1Evidence.status -eq "PASS_AUTHOR_APPROVED" -and
            [bool]$g9o1Evidence.approval.authorApproved -and
            $g9o1Evidence.phaseDisposition.G9A1 -eq `
                "AUTHORIZED_NOT_STARTED") `
        -Message "G9O1 closeout/G9A1 authorization gate is inconsistent."

    & git -C $RepositoryRoot diff --quiet $EntrySha -- $RedefineTestPath
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "The required upstream RedefineTest must remain unchanged."

    $evidence = Read-JsonFile -RelativePath $EvidencePath
    Assert-Condition -Condition ($evidence.phase -eq "G9A1" -and
            $evidence.status -eq `
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $evidence.provenance.entrySha -eq $EntrySha -and
            $evidence.provenance.branch -eq $ExpectedBranch -and
            $evidence.provenance.canonicalPromptCanonicalLfSha256 -eq `
                $PromptSha256 -and
            $evidence.provenance.spatialSpecificationCanonicalLfSha256 -eq `
                $SpecificationSha256 -and
            $evidence.provenance.adr0010CanonicalLfSha256 -eq $Adr10Sha256 -and
            $evidence.provenance.adr0011CanonicalLfSha256 -eq $Adr11Sha256) `
        -Message "G9A1 candidate evidence provenance/status is inconsistent."
    Assert-Condition -Condition (-not [bool]$evidence.approval.selfApproved -and
            -not [bool]$evidence.approval.authorApproved -and
            -not [bool]$evidence.approval.passClaimed -and
            [bool]$evidence.approval.reviewRequired -and
            $evidence.approval.disposition -eq `
                "PENDING_AUTHOR_REVIEW" -and
            $evidence.phaseDisposition.G9A1 -eq `
                "IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW" -and
            $evidence.phaseDisposition.G9A2 -eq `
                "NOT_AUTHORIZED_NOT_STARTED" -and
            $evidence.phaseDisposition.laterG9 -eq `
                "NOT_AUTHORIZED_NOT_STARTED") `
        -Message "G9A1 evidence claims ungranted approval or later-phase authority."
    Assert-Condition -Condition (
            [bool]$evidence.entryGates.composedWithoutSkipBuild.passed -and
            [int]$evidence.entryGates.composedWithoutSkipBuild.exitCode -eq 0 -and
            -not [string]::IsNullOrWhiteSpace(
                $evidence.entryGates.composedWithoutSkipBuild.logDirectory)) `
        -Message "G9A1 evidence does not record the clean composed entry gate."
    Assert-Condition -Condition (-not [bool]$evidence.validation.privateParserOnly -and
            [bool]$evidence.validation.realHostXmlRoundTrip -and
            [bool]$evidence.validation.deterministicRerunAsserted -and
            @($evidence.validation.hostParserTestClasses).Count -gt 0) `
        -Message "G9A1 validation must use the real host XML route and deterministic rerun."

    foreach ($zeroScope in @(
            "spatialReconstruction", "projectionGeometryEvaluation",
            "threeDimensionalAuthority", "publicLocusChanges", "guiChanges",
            "dxfChanges", "labelOrProximityIdentity", "migrationInference",
            "g9a2OrLaterChanges", "publicCommands", "hiddenScheduler",
            "generatedTrackedArtifacts")) {
        Assert-Condition -Condition ([int]$evidence.scopeAudit.$zeroScope -eq 0) `
            -Message "G9A1 forbidden scope gate is nonzero: $zeroScope"
    }
    foreach ($zeroAuthority in @(
            "labels", "creationOrder", "xmlPosition", "coordinates", "ceID",
            "layers", "viewport", "dpi", "camera")) {
        Assert-Condition -Condition (
                [int]$evidence.identityAuthorityCounters.$zeroAuthority -eq 0) `
            -Message "Forbidden identity-authority counter is nonzero: $zeroAuthority"
    }

    $changedPaths = @(Get-CandidateChangedPaths)
    Assert-SourceBoundary -Evidence $evidence -ChangedPaths $changedPaths

    $focusedClasses = @($evidence.tests.focused.classes)
    Assert-Condition -Condition ($focusedClasses.Count -eq
            $ExpectedFocusedTests.Count) `
        -Message "G9A1 evidence must contain the exact seven focused test classes."
    $classNames = @($focusedClasses | ForEach-Object { $_.name })
    $expectedClassNames = @($ExpectedFocusedTests | ForEach-Object { $_.name })
    Assert-ExactPathSet -Actual $classNames -Expected $expectedClassNames `
        -Description "G9A1 focused evidence class set"
    foreach ($expectedTest in $ExpectedFocusedTests) {
        $observed = @($focusedClasses | Where-Object {
                $_.name -ceq $expectedTest.name
            })
        Assert-Condition -Condition ($observed.Count -eq 1 -and
                [int]$observed[0].tests -eq [int]$expectedTest.tests) `
            -Message "G9A1 focused evidence count is wrong: $($expectedTest.name)"
    }
    $hostParserClasses = @($evidence.validation.hostParserTestClasses)
    Assert-ExactPathSet -Actual $hostParserClasses `
        -Expected $ExpectedHostParserClasses `
        -Description "G9A1 real host-parser evidence class set"
    foreach ($hostParserClass in $hostParserClasses) {
        Assert-Condition -Condition ($hostParserClass -in $classNames) `
            -Message "Host-parser test class is not in the focused test set: $hostParserClass"
        $hostParserPath = "source/shared/common-jre/src/test/java/{0}.java" -f `
            $hostParserClass.Replace(".", "/")
        Assert-Condition -Condition ($hostParserPath -in
                @($evidence.sourceBoundary.testPaths)) `
            -Message "Host-parser test source is not declared: $hostParserPath"
        $hostParserText = Get-Content -Raw -LiteralPath (
            Resolve-RequiredFile -RelativePath $hostParserPath)
        Assert-Condition -Condition ($hostParserText.Contains("BaseUnitTest") -and
                $hostParserText -match '\b(?:reload|setXML|getXML)\s*\(') `
            -Message "Host-parser coverage does not exercise the real application XML route: $hostParserClass"
    }
    $focusedTotal = [int](($focusedClasses | Measure-Object `
            -Property tests -Sum).Sum)
    Assert-Condition -Condition ($focusedTotal -eq $ExpectedFocusedTotal -and
            $focusedTotal -eq
            [int]$evidence.tests.focused.total.tests -and
            [int]$evidence.tests.focused.total.failures -eq 0 -and
            [int]$evidence.tests.focused.total.errors -eq 0 -and
            [int]$evidence.tests.focused.total.skipped -eq 0 -and
            $evidence.tests.redefineRegression.class -eq `
                "org.geogebra.common.kernel.commands.RedefineTest" -and
            [int]$evidence.tests.redefineRegression.tests -eq `
                $ExpectedRedefineTests -and
            [int]$evidence.tests.redefineRegression.failures -eq 0 -and
            [int]$evidence.tests.redefineRegression.errors -eq 0 -and
            [int]$evidence.tests.redefineRegression.skipped -eq 0 -and
            [int]$evidence.tests.combined.tests -eq $ExpectedCombinedTotal -and
            [int]$evidence.tests.combined.tests -eq
                $focusedTotal + $ExpectedRedefineTests -and
            [int]$evidence.tests.combined.failures -eq 0 -and
            [int]$evidence.tests.combined.errors -eq 0 -and
            [int]$evidence.tests.combined.skipped -eq 0) `
        -Message "G9A1 expected test totals are inconsistent."

    if (-not $SkipBuild) {
        Invoke-G9A1Tests
        foreach ($testClass in $focusedClasses) {
            Assert-TestResult -ClassName $testClass.name `
                -ExpectedTests ([int]$testClass.tests)
        }
        Assert-TestResult -ClassName `
            "org.geogebra.common.kernel.commands.RedefineTest" `
            -ExpectedTests $ExpectedRedefineTests
        $actualFocusedResults = @(Get-ChildItem -LiteralPath $TestResultRoot `
            -Filter "TEST-org.geocedg.common.spatial.G9A1*.xml" -File |
            ForEach-Object { $_.BaseName.Substring(5) } | Sort-Object -Unique)
        Assert-ExactPathSet -Actual $actualFocusedResults `
            -Expected $classNames -Description "G9A1 focused JUnit class set"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult -RelativePath `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
    } else {
        Write-Host "Skipping G9A1 Gradle tests because -SkipBuild was supplied."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9A1."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9A1."

    Write-Host "G9A1 focused verification passed ($ExpectedFocusedTotal focused + $ExpectedRedefineTests unchanged redefine tests = $ExpectedCombinedTotal)."
    Write-Host "G9A1 = PASS — AUTHOR APPROVED."
    Write-Host "G9A2 remains designed, not authorized, and not executed."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error "G9A1 focused verification failed: $($_.Exception.Message)"
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G9A1 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = Get-RepositoryStatusText -RepositoryRoot $RepositoryRoot
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G9A1 verification."
            exit 1
        }
    }
}

exit 0

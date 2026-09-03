[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$AllowToolchainDownload,
    [switch]$KeepBuildOutputs,
    [string]$BuildEvidencePath,
    [switch]$IncrementalBuild,
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        "geocedg-verify-g9u0-r1-locus-v2-public-creation-lifecycle")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Import-Module (Join-Path $PSScriptRoot "verification-runtime.psm1")
Assert-GeoCeDGChildVerificationMode -SkipBuild:$SkipBuild `
    -BuildEvidencePath $BuildEvidencePath -IncrementalBuild:$IncrementalBuild

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootGradle = Join-Path $RepositoryRoot "gradlew.bat"
$EntrySha = "22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0"
$ExpectedBranch = "fix/g9u0-locus-v2-public-creation-lifecycle"
$R1PassTagName = "geocedg-g9u0-r1-pass"
$RoadmapPath = "docs/roadmap/geocedg_roadmap.md"
$HistoricalEvidencePath =
    "geocedg/validation/locus-v2/g9u0/g9u0-public-surface-evidence.json"
$HistoricalScenarioPath =
    "geocedg/validation/locus-v2/g9u0/g9u0-public-surface-scenarios.json"
$HistoricalVerifierPath = "tools/agent/verify-g9u0-locus-v2-public-surface.ps1"
$HistoricalEvidenceSha256 =
    "13bf2a6f483e17c64bf3df27ff898d32e8a067641361cf6282a673ec868e963b"
$HistoricalScenarioSha256 =
    "60214f3ff2b4024940e4caeb5499fd046064f2165261e7a890b812d242677ced"
$G9U0PassTagObject = "612845c42925bc519f68443d09fd400ff4365251"
$G9U0PromotionCommit = "bdd20da3e9e711dcc35e818d857d604d7b217385"
$ReportPath =
    "docs/validation/g9u0_r1_locus_v2_public_creation_lifecycle_candidate_report.md"
$GeneratedStateHelper = Join-Path $PSScriptRoot "repository-generated-state.ps1"
$GeneratedDirectoryNames = @("build", ".gradle", ".kotlin")
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$InitialStatus = $null
$GeneratedSnapshot = $null
$R1BoundaryMode = $null
$R1AuthorityCommit = $null

$ExpectedChangedPaths = @(
    $RoadmapPath,
    "docs/upstream/modified-files.yml",
    $ReportPath,
    "source/desktop/desktop/src/test/java/org/geocedg/desktop/locus/LocusV2DesktopLifecycleRegressionTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2PointDrivenCreationRegressionTest.java",
    "source/shared/common-jre/src/test/java/org/geocedg/common/spatial/SpatialSemanticInstrumentationSequentialHandoffTest.java",
    "source/shared/common-jre/src/test/java/org/geogebra/common/kernel/LocusV2InputPreviewLifecycleTest.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java",
    "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/SpatialSemanticInstrumentation.java",
    "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocusV2.java",
    $HistoricalVerifierPath,
    "tools/agent/verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1",
    "tools/agent/verify.ps1"
)

$ExpectedTestClasses = @(
    [pscustomobject]@{
        Marker = "R1-A01"
        ClassName = "org.geocedg.common.locus.LocusV2PointDrivenCreationRegressionTest"
        SourcePath = "source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2PointDrivenCreationRegressionTest.java"
        ResultRoot = "source/shared/common-jre/build/test-results/test"
        Methods = @("reportedCirclePointConstructionUsesNormalV2DagAndRecomputes")
    },
    [pscustomobject]@{
        Marker = "R1-B01"
        ClassName = "org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest"
        SourcePath = "source/shared/common-jre/src/test/java/org/geogebra/common/kernel/LocusV2InputPreviewLifecycleTest.java"
        ResultRoot = "source/shared/common-jre/build/test-results/test"
        Methods = @("inputBarPreviewSkipsDurablePublicationAndDefinitiveExecutionStillSucceeds")
    },
    [pscustomobject]@{
        Marker = "R1-C01,R1-C02"
        ClassName = "org.geocedg.common.spatial.SpatialSemanticInstrumentationSequentialHandoffTest"
        SourcePath = "source/shared/common-jre/src/test/java/org/geocedg/common/spatial/SpatialSemanticInstrumentationSequentialHandoffTest.java"
        ResultRoot = "source/shared/common-jre/build/test-results/test"
        Methods = @(
            "emptyStagedMergeMayCrossASequentialHostThreadHandoff",
            "nonEmptyStagedMergeStillRejectsForeignThread"
        )
    },
    [pscustomobject]@{
        Marker = "R1-B02,R1-C03"
        ClassName = "org.geocedg.desktop.locus.LocusV2DesktopLifecycleRegressionTest"
        SourcePath = "source/desktop/desktop/src/test/java/org/geocedg/desktop/locus/LocusV2DesktopLifecycleRegressionTest.java"
        ResultRoot = "source/desktop/desktop/build/test-results/test"
        Methods = @(
            "launcherCreatedConstructionSupportsScheduledPreviewOnEdt",
            "launcherCreatedConstructionSupportsDefinitiveEdtCreation"
        )
    }
)

$ExpectedMarkers = @(
    "R1-A01", "R1-B01", "R1-B02", "R1-C01", "R1-C02", "R1-C03"
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
    $prefix = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    Assert-Condition -Condition ($absolute.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase)) `
        -Message "Required path escapes the repository: $RelativePath"
    Assert-Condition -Condition (Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9U0-R1 path is missing: $RelativePath"
    return $absolute
}

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $bytes = [IO.File]::ReadAllBytes((Resolve-RequiredFile $RelativePath))
    $offset = 0
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $offset = 3
    }
    $text = [Text.UTF8Encoding]::new($false, $true).GetString(
        $bytes, $offset, $bytes.Length - $offset)
    $canonical = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
            [Text.UTF8Encoding]::new($false).GetBytes($canonical))).ToLowerInvariant()
}

function Assert-ExactSet {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Actual,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]]$Expected,
        [Parameter(Mandatory)] [string]$Description
    )

    $actualUnique = @($Actual | Sort-Object -Unique)
    $expectedUnique = @($Expected | Sort-Object -Unique)
    $missing = @($expectedUnique | Where-Object { $_ -notin $actualUnique })
    $unexpected = @($actualUnique | Where-Object { $_ -notin $expectedUnique })
    Assert-Condition -Condition ($Actual.Count -eq $actualUnique.Count -and
            $missing.Count -eq 0 -and $unexpected.Count -eq 0) `
        -Message ("{0} mismatch. missing={1}; unexpected={2}" -f $Description,
            ($missing -join ", "), ($unexpected -join ", "))
}

function Get-WorktreeChangedPaths {
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($path in @(& git -C $RepositoryRoot diff --name-only --no-renames `
            $EntrySha --)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $paths.Add($path.Replace("\", "/"))
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked G9U0-R1 changes."
    foreach ($path in @(& git -C $RepositoryRoot ls-files --others --exclude-standard)) {
        if (-not [string]::IsNullOrWhiteSpace($path)) {
            $normalized = $path.Replace("\", "/")
            if ($normalized -notin $paths) {
                $paths.Add($normalized)
            }
        }
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate untracked G9U0-R1 changes."
    return @($paths | Sort-Object -Unique)
}

function Get-CommitChangedPaths {
    param([Parameter(Mandatory)] [string]$Commit)

    $paths = @(& git -C $RepositoryRoot diff --name-only --no-renames `
        $EntrySha $Commit --)
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate committed G9U0-R1 changes."
    return @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } | ForEach-Object {
            $_.Replace("\", "/")
        } | Sort-Object -Unique)
}

function Assert-R1CommitShape {
    param([Parameter(Mandatory)] [string]$Commit)

    $recordText = (& git -C $RepositoryRoot rev-list --parents -n 1 `
        $Commit).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            -not [string]::IsNullOrWhiteSpace($recordText)) `
        -Message "Unable to inspect the G9U0-R1 authority commit."
    $record = @($recordText -split '\s+')
    Assert-Condition -Condition ($record.Count -eq 2 -and
            $record[0] -eq $Commit -and $record[1] -eq $EntrySha) `
        -Message "G9U0-R1 must be one closeout commit whose sole parent is entry."
}

function Initialize-R1Boundary {
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $head -cmatch '^[0-9a-f]{40}$') `
        -Message "Unable to resolve the current G9U0-R1 HEAD."

    & git -C $RepositoryRoot show-ref --verify --quiet `
        "refs/tags/$R1PassTagName" 2>$null
    $tagLookupExit = $LASTEXITCODE
    Assert-Condition -Condition ($tagLookupExit -eq 0 -or $tagLookupExit -eq 1) `
        -Message "Unable to resolve the G9U0-R1 PASS tag."

    if ($tagLookupExit -eq 0) {
        $tagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$R1PassTagName").Trim()
        $tagType = (& git -C $RepositoryRoot cat-file -t $tagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $tagType -eq "tag") `
            -Message "$R1PassTagName must be an annotated tag."
        $authorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$tagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $authorityCommit -cmatch '^[0-9a-f]{40}$') `
            -Message "Unable to peel the G9U0-R1 PASS tag."
        $tagLines = @(& git -C $RepositoryRoot cat-file tag $tagObject)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to inspect the G9U0-R1 PASS tag message."
        $tagText = $tagLines -join "`n"
        Assert-Condition -Condition ($tagText.Contains("G9U0-R1") -and
                $tagText.Contains("PASS — AUTHOR APPROVED")) `
            -Message "The G9U0-R1 annotated tag message lacks the approved PASS disposition."
        Assert-R1CommitShape -Commit $authorityCommit
        & git -C $RepositoryRoot merge-base --is-ancestor `
            $authorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not retain the tagged G9U0-R1 correction."
        $script:R1BoundaryMode = "TAGGED_DESCENDANT"
        $script:R1AuthorityCommit = $authorityCommit
        return
    }

    $branch = ((@(& git -C $RepositoryRoot branch --show-current) -join "")).Trim()
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to resolve the current G9U0-R1 branch."
    if ($head -eq $EntrySha) {
        Assert-Condition -Condition ($branch -eq $ExpectedBranch) `
            -Message "Pre-commit G9U0-R1 must remain on $ExpectedBranch at entry."
        $staged = @(& git -C $RepositoryRoot diff --cached --name-only --)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $staged.Count -eq 0) `
            -Message "Pre-commit G9U0-R1 requires an empty index."
        $script:R1BoundaryMode = "WORKTREE"
        $script:R1AuthorityCommit = $null
        return
    }

    Assert-Condition -Condition ($branch -eq $ExpectedBranch -or
            $branch -eq "main") `
        -Message "Untagged committed G9U0-R1 must be on its fix branch or main."
    Assert-R1CommitShape -Commit $head
    $status = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            [string]::IsNullOrWhiteSpace($status)) `
        -Message "Untagged committed G9U0-R1 requires a clean worktree and index."
    $script:R1BoundaryMode = "COMMITTED_PRETAG"
    $script:R1AuthorityCommit = $head
}

function Get-R1ChangedPaths {
    if ($R1BoundaryMode -eq "WORKTREE") {
        return @(Get-WorktreeChangedPaths)
    }
    Assert-Condition -Condition ($R1BoundaryMode -in @(
            "COMMITTED_PRETAG", "TAGGED_DESCENDANT") -and
            $null -ne $R1AuthorityCommit) `
        -Message "The G9U0-R1 source boundary mode was not established."
    return @(Get-CommitChangedPaths -Commit $R1AuthorityCommit)
}

function Get-TestMethods {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $source = Get-Content -Raw -LiteralPath (Resolve-RequiredFile $RelativePath)
    $pattern = '(?ms)@Test\s+(?:@[^\r\n]+\s+)*' +
        '(?:(?:public|protected|private)\s+)?(?:final\s+)?' +
        'void\s+([A-Za-z0-9_]+)\s*\('
    return @([regex]::Matches($source, $pattern) | ForEach-Object {
            $_.Groups[1].Value
        })
}

function Assert-HistoricalBoundary {
    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
            $HistoricalEvidencePath) -eq $HistoricalEvidenceSha256) `
        -Message "Author-approved G9U0 evidence changed during R1."
    Assert-Condition -Condition ((Get-CanonicalTextSha256 `
            $HistoricalScenarioPath) -eq $HistoricalScenarioSha256) `
        -Message "The frozen 93-case G9U0 scenario authority changed during R1."
    [void](Resolve-RequiredFile $HistoricalVerifierPath)
    $historicalVerifier = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile $HistoricalVerifierPath)
    foreach ($fragment in @(
            '$G9U0TagName = "geocedg-g9u0-pass"',
            ('$G9U0TagObject = "' + $G9U0PassTagObject + '"'),
            ('$G9U0PromotionCommit = "' + $G9U0PromotionCommit + '"'),
            'merge-base --is-ancestor', '$G9U0PromotionCommit HEAD')) {
        Assert-Condition -Condition ($historicalVerifier.Contains($fragment)) `
            -Message "Historical G9U0 descendant-HEAD anchor is missing: $fragment"
    }
    $scenario = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile $HistoricalScenarioPath) | ConvertFrom-Json -Depth 100
    Assert-Condition -Condition ([int]$scenario.expectedScenarioCount -eq 93 -and
            [int]$scenario.testExecution.commonCount -eq 81 -and
            [int]$scenario.testExecution.desktopCount -eq 12 -and
            [int]$scenario.testExecution.focusedTotal -eq 93) `
        -Message "Historical G9U0 93/93 partition drifted."
}

function Assert-SourceAndScopeBoundary {
    Initialize-R1Boundary
    $changed = @(Get-R1ChangedPaths)
    Assert-ExactSet -Actual $changed -Expected $ExpectedChangedPaths `
        -Description "G9U0-R1 exact closeout inventory"

    foreach ($path in $changed) {
        Assert-Condition -Condition ($path -notmatch
                '(?i)(g9x1|g9u1|g9b|g9c|g9u2|g10|export/dxf)') `
            -Message "G9U0-R1 escaped its authorized phase boundary: $path"
    }
    foreach ($historical in @(
            $HistoricalEvidencePath, $HistoricalScenarioPath,
            "geocedg/validation/locus-v2/g9u0/g9u0-evidence.sha256",
            "geocedg/specs/locus/locus-v2-public-surface.md",
            "docs/adr/0013-public-locus-v2-surface-and-token-selection.md")) {
        Assert-Condition -Condition ($historical -notin $changed) `
            -Message "G9U0-R1 rewrote historical or normative authority: $historical"
    }

    $trackedGenerated = @(& git -C $RepositoryRoot ls-files | Where-Object {
            $_ -match '(^|/)(artifacts|build|\.gradle|\.kotlin|__pycache__)(/|$)|\.pyc$'
        } | Where-Object { $_ -ne "artifacts/README.md" })
    Assert-Condition -Condition ($trackedGenerated.Count -eq 0) `
        -Message "Tracked generated artifacts detected: $($trackedGenerated -join ', ')"
}

function Assert-OrderedFragments {
    param(
        [Parameter(Mandatory)] [string]$Text,
        [Parameter(Mandatory)] [string[]]$Fragments,
        [Parameter(Mandatory)] [string]$Description
    )

    $offset = 0
    foreach ($fragment in $Fragments) {
        $index = $Text.IndexOf($fragment, $offset,
            [StringComparison]::Ordinal)
        Assert-Condition -Condition ($index -ge 0) `
            -Message "$Description is missing or out of order: $fragment"
        $offset = $index + $fragment.Length
    }
}

function Assert-TestAuthority {
    $allMethods = 0
    foreach ($testClass in $ExpectedTestClasses) {
        Assert-Condition -Condition (-not $testClass.ClassName.Contains(".G9U0")) `
            -Message "R1 tests must remain outside the historical G9U0* wildcard."
        $methods = @(Get-TestMethods $testClass.SourcePath)
        Assert-ExactSet -Actual $methods -Expected @($testClass.Methods) `
            -Description "R1 test methods for $($testClass.ClassName)"
        $allMethods += $methods.Count
    }
    Assert-Condition -Condition ($allMethods -eq 6) `
        -Message "G9U0-R1 must expose exactly 6 corrective test methods."

    $report = Get-Content -Raw -LiteralPath (Resolve-RequiredFile $ReportPath)
    foreach ($marker in $ExpectedMarkers) {
        Assert-Condition -Condition ($report.Contains($marker)) `
            -Message "The R1 report is missing test marker $marker."
    }
    Assert-OrderedFragments -Text $report -Description `
        "G9U0-R1 author-closeout tuple" -Fragments @(
            "G9U0-R1 = PASS — AUTHOR APPROVED",
            "selfApproved = false",
            "authorApproved = true",
            "passClaimed = true"
        )
    Assert-OrderedFragments -Text $report -Description `
        "Frozen G9X1 candidate tuple" -Fragments @(
            "G9X1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW",
            "selfApproved = false",
            "authorApproved = false",
            "passClaimed = false"
        )
    foreach ($fragment in @(
            "93/93", "81 shared + 12 Desktop", "6/6",
            "4 shared + 2 Desktop", "exactly 12 paths", "exactly 13 paths",
            "Manual author smoke test", "no false CAS error",
            "file-save concern")) {
        Assert-Condition -Condition ($report.Contains($fragment)) `
            -Message "The R1 report is missing governance/count evidence: $fragment"
    }

    $roadmap = Get-Content -Raw -LiteralPath (Resolve-RequiredFile $RoadmapPath)
    Assert-Condition -Condition ($roadmap.Contains(
            "G9U0-R1 = PASS — AUTHOR APPROVED") -and
            $roadmap.Contains("G9X1 = PASS — AUTHOR APPROVED")) `
        -Message ("Living roadmap does not preserve the bounded G9U0-R1 " +
            "closeout and the separately approved G9X1 closeout.")
}

function Assert-ImplementationSeams {
    $command = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocusV2.java")
    $previewGuard = $command.IndexOf("if (!info.isScripting())",
        [StringComparison]::Ordinal)
    $argumentResolution = $command.IndexOf("resArgs(command, info)",
        [StringComparison]::Ordinal)
    Assert-Condition -Condition ($previewGuard -ge 0 -and
            $argumentResolution -gt $previewGuard -and
            $command.Contains("return new GeoElement[0];")) `
        -Message "CmdLocusV2 no longer isolates preview before durable argument handling."

    $instrumentation = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/SpatialSemanticInstrumentation.java")
    foreach ($fragment in @(
            "staged.checkOwner();", "!staged.hasRecordedEvidence()",
            "checkOwner();", "instrumentation is thread-confined",
            "authoritativePublicationEpoch != 0",
            "!authoritativePublicationCounts.isEmpty()")) {
        Assert-Condition -Condition ($instrumentation.Contains($fragment)) `
            -Message "Construction-confined instrumentation seam is missing: $fragment"
    }

    $evaluator = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java")
    Assert-Condition -Condition ($evaluator.Contains(
            "construction.isConstantElement(current)") -and
            $evaluator.Contains("continue;")) `
        -Message "Reconstructible evaluation no longer preserves MacroKernel canonical constants."

    $identityRegistry = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java")
    Assert-Condition -Condition ($identityRegistry.Contains("GEO_NOT_SERIALIZABLE")) `
        -Message "G9U0-R1 must not weaken GEO_NOT_SERIALIZABLE."
}

function Assert-UpstreamManifest {
    $manifest = Get-Content -Raw -LiteralPath (Resolve-RequiredFile `
        "docs/upstream/modified-files.yml") | ConvertFrom-Json -Depth 100
    $required = @($ExpectedChangedPaths | Where-Object {
            $_.StartsWith("source/", [StringComparison]::Ordinal)
        })
    foreach ($path in $required) {
        $matches = @($manifest.modifications | Where-Object {
                "$($_.path)" -ceq $path
            })
        Assert-Condition -Condition ($matches.Count -eq 1 -and
                $matches[0].change -eq "added" -and
                -not [string]::IsNullOrWhiteSpace("$($matches[0].purpose)") -and
                -not [string]::IsNullOrWhiteSpace("$($matches[0].authority)")) `
            -Message "Upstream manifest must register exactly one complete entry for $path"
    }
}

function Invoke-LoggedGradle {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$LogName,
        [Parameter(Mandatory)] [string]$Description
    )

    if (-not $AllowToolchainDownload) {
        $Arguments += "-Dorg.gradle.java.installations.auto-download=false"
    }
    $logPath = Join-Path $LogDirectory $LogName
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
        Confirm-GeoCeDGBuildEvidence -EvidencePath $BuildEvidencePath `
            -RepositoryRoot $RepositoryRoot -WorkingDirectory $RepositoryRoot `
            -Arguments $Arguments -LogPath $logPath `
            -Description $Description -AllowToolchainDownload:$AllowToolchainDownload
        return
    }
    if ($IncrementalBuild) {
        $Arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
            -Arguments $Arguments -KeepBuildOutputs:$KeepBuildOutputs)
    }
    Write-Host "`n==> $Description"
    Write-Host "    log: $logPath"
    Push-Location -LiteralPath $RepositoryRoot
    try {
        & $RootGradle @Arguments 2>&1 | Tee-Object -FilePath $logPath
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    Assert-Condition -Condition ($exitCode -eq 0) `
        -Message "$Description failed with exit code $exitCode. See $logPath"
}

function Assert-TestResult {
    param([Parameter(Mandatory)] [object]$TestClass)

    $relative = "$($TestClass.ResultRoot)/TEST-$($TestClass.ClassName).xml"
    [xml]$result = Get-Content -Raw -LiteralPath (Resolve-RequiredFile $relative)
    $suite = $result.testsuite
    Assert-Condition -Condition ([int]$suite.tests -eq $TestClass.Methods.Count -and
            [int]$suite.failures -eq 0 -and [int]$suite.errors -eq 0 -and
            [int]$suite.skipped -eq 0) `
        -Message ("{0} is not clean: tests={1}, failures={2}, errors={3}, " +
            "skipped={4}." -f $TestClass.ClassName, $suite.tests,
            $suite.failures, $suite.errors, $suite.skipped)
}

function Assert-CheckstyleResult {
    param([Parameter(Mandatory)] [string]$RelativePath)

    $errors = @(Select-String -LiteralPath (Resolve-RequiredFile $RelativePath) `
        -Pattern "<error ")
    Assert-Condition -Condition ($errors.Count -eq 0) `
        -Message "$RelativePath contains $($errors.Count) Checkstyle violations."
}

try {
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to record initial G9U0-R1 repository status."

    foreach ($required in @($ExpectedChangedPaths + $HistoricalEvidencePath +
            $HistoricalScenarioPath + $HistoricalVerifierPath +
            "source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java")) {
        [void](Resolve-RequiredFile $required)
    }
    Assert-HistoricalBoundary
    Assert-SourceAndScopeBoundary
    Assert-TestAuthority
    Assert-ImplementationSeams
    Assert-UpstreamManifest

    if ($SkipBuild) {
        Write-Host ("Skipping G9U0-R1 Gradle probes because -SkipBuild was " +
            "supplied. Static author-approved closeout authority is consistent; " +
            "this invocation did not rerun the six focused tests.")
    } else {
        [void](New-Item -ItemType Directory -Path $LogDirectory -Force)
        if ([string]::IsNullOrWhiteSpace($BuildEvidencePath)) {
            $GeneratedSnapshot = New-RepositoryGeneratedStateSnapshot `
                -RepositoryRoot $RepositoryRoot `
                -DirectoryNames $GeneratedDirectoryNames -Label "g9u0-r1" `
                -KeepCurrentOutputs:$KeepBuildOutputs
        }

        $sharedArguments = [Collections.Generic.List[string]]::new()
        $sharedArguments.Add(":shared:common-jre:test")
        foreach ($testClass in @($ExpectedTestClasses | Where-Object {
                    $_.ResultRoot.StartsWith("source/shared/",
                        [StringComparison]::Ordinal)
                })) {
            $sharedArguments.Add("--tests")
            $sharedArguments.Add($testClass.ClassName)
        }
        foreach ($argument in @(
                ":shared:common:checkstyleMain",
                ":shared:common-jre:checkstyleTest",
                "--rerun-tasks", "--no-daemon", "--console=plain",
                "--no-problems-report")) {
            $sharedArguments.Add($argument)
        }
        Invoke-LoggedGradle -LogName "g9u0-r1-shared.log" `
            -Description "G9U0-R1 shared creation, preview and confinement regressions" `
            -Arguments @($sharedArguments)

        Invoke-LoggedGradle -LogName "g9u0-r1-desktop.log" `
            -Description "G9U0-R1 Desktop preview and EDT lifecycle regressions" `
            -Arguments @(
                ":desktop:desktop:test", "--tests",
                "org.geocedg.desktop.locus.LocusV2DesktopLifecycleRegressionTest",
                ":desktop:desktop:checkstyleTest", "--rerun-tasks",
                "--no-daemon", "--console=plain", "--no-problems-report"
            )

        foreach ($testClass in $ExpectedTestClasses) {
            Assert-TestResult -TestClass $testClass
        }
        Assert-CheckstyleResult `
            "source/shared/common/build/reports/checkstyle/main.xml"
        Assert-CheckstyleResult `
            "source/shared/common-jre/build/reports/checkstyle/test.xml"
        Assert-CheckstyleResult `
            "source/desktop/desktop/build/reports/checkstyle/test.xml"
        Write-Host "G9U0-R1 corrective regression authority passed (6/6: 4 shared + 2 Desktop)."
        Write-Host "Historical G9U0 remains a separate sealed authority (93/93: 81 shared + 12 Desktop)."
        Write-Host "G9U0-R1 = PASS — AUTHOR APPROVED."
    }

    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed."
    Write-Host "G9U0-R1 boundary mode: $R1BoundaryMode"
    Write-Host "G9U0-R1 = PASS — AUTHOR APPROVED."
    Write-Host "Logs: $LogDirectory"
} catch {
    Write-Error $_.Exception.Message
    exit 1
} finally {
    if ($null -ne $GeneratedSnapshot) {
        Restore-RepositoryGeneratedStateSnapshot -Snapshot $GeneratedSnapshot `
            -KeepCurrentOutputs:$KeepBuildOutputs `
            -Description "G9U0-R1 build output"
    }
    if ($null -ne $InitialStatus) {
        $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
            --untracked-files=all) -join "`n"
        if ($finalStatus -ne $InitialStatus) {
            Write-Error "Repository status changed during G9U0-R1 verification."
            exit 1
        }
    }
}

exit 0

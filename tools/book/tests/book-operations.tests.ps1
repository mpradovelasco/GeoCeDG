[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\..\.."))
$SourceScript = Join-Path $RepositoryRoot "tools\book\book-worktree.ps1"
$PowerShell = Join-Path $PSHOME "pwsh.exe"
if (-not (Test-Path -LiteralPath $PowerShell -PathType Leaf)) {
    $PowerShell = Join-Path $PSHOME "pwsh"
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

function Get-NormalizedFullPath {
    param([Parameter(Mandatory)] [string]$Path)

    return [IO.Path]::GetFullPath($Path).TrimEnd("/", "\")
}

function Write-Utf8File {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Content
    )

    $parent = Split-Path -Parent $Path
    [void](New-Item -ItemType Directory -Path $parent -Force)
    [IO.File]::WriteAllText(
        $Path, $Content.Replace("`r`n", "`n").Replace("`r", "`n"),
        [Text.UTF8Encoding]::new($false))
}

function Invoke-TestGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [int[]]$AllowedExitCodes = @(0)
    )

    $output = @(& git --no-optional-locks -C $Root @Arguments 2>&1 |
        ForEach-Object { $_.ToString() })
    if ($LASTEXITCODE -notin $AllowedExitCodes) {
        throw "Fixture git $($Arguments -join ' ') failed: $($output -join '; ')"
    }
    return ($output -join "`n")
}

function Initialize-TestRepository {
    param([Parameter(Mandatory)] [string]$Root)

    [void](New-Item -ItemType Directory -Path $Root -Force)
    [void](Invoke-TestGit -Root $Root -Arguments @("init", "-b", "main"))
    [void](Invoke-TestGit -Root $Root `
        -Arguments @("config", "user.name", "GeoCeDG Book Ops Test"))
    [void](Invoke-TestGit -Root $Root `
        -Arguments @("config", "user.email", "book-ops@example.invalid"))
}

function Invoke-BookOperation {
    param(
        [Parameter(Mandatory)] [string]$Script,
        [Parameter(Mandatory)] [string]$Action,
        [string[]]$Arguments = @()
    )

    $output = @(& $PowerShell -NoProfile -File $Script -Action $Action `
            @Arguments 2>&1 | ForEach-Object { $_.ToString() })
    return [pscustomobject]@{
        ExitCode = $LASTEXITCODE
        Text = ($output -join "`n")
    }
}

function Assert-OperationResult {
    param(
        [Parameter(Mandatory)] [object]$Result,
        [Parameter(Mandatory)] [int]$ExitCode,
        [Parameter(Mandatory)] [string]$Contains
    )

    Assert-Condition -Condition ($Result.ExitCode -eq $ExitCode) `
        -Message "Unexpected exit code $($Result.ExitCode): $($Result.Text)"
    Assert-Condition -Condition $Result.Text.Contains($Contains) `
        -Message "Operation output is missing '$Contains': $($Result.Text)"
}

function Get-RepositoryReadOnlySnapshot {
    param([Parameter(Mandatory)] [string]$Root)

    $parts = @(
        Invoke-TestGit -Root $Root -Arguments @("rev-parse", "HEAD")
        Invoke-TestGit -Root $Root -Arguments @("show-ref")
        Invoke-TestGit -Root $Root -Arguments @("ls-files", "-s")
        Invoke-TestGit -Root $Root `
            -Arguments @("status", "--porcelain=v1", "--untracked-files=all")
    )
    return ($parts -join "`n---`n")
}

function Set-BookBaselineRegistry {
    param(
        [Parameter(Mandatory)] [string]$BookRoot,
        [Parameter(Mandatory)] [string]$HistoricalCommit,
        [Parameter(Mandatory)] [string]$Status,
        [AllowNull()] [string]$Commit,
        [AllowNull()] [string]$Fingerprint,
        [AllowNull()] [string]$PublishedTag = $null,
        [AllowNull()] [object]$FingerprintSchemaVersion = $null
    )

    $registry = [ordered]@{
        schema_version = 1
        authority = [ordered]@{
            repository = "mpradovelasco/geocedg_book"
            policy = "editorial/source-mapping/TECHNICAL_BASELINE_POLICY.md"
            product_candidate_source = "GeoCeDG tools/book/book-worktree.ps1"
        }
        historical_phase_baselines = @(
            [ordered]@{
                book_phase = "BOOK-P0"
                status = "PASS_AUTHOR_APPROVED"
                geocedg_commit = $HistoricalCommit
                published_tag = "geocedg-test-pass"
                evidence = "editorial/source-mapping/AUDIT_BASELINE.md"
                immutable = $true
            }
        )
        current_editorial_technical_baseline = [ordered]@{
            status = $Status
            geocedg_commit = $Commit
            published_tag = $PublishedTag
            technical_authority_fingerprint = $Fingerprint
            fingerprint_schema_version = $FingerprintSchemaVersion
            accepted_in_book_phase = if ($Status -eq "ACCEPTED") {
                "BOOK-P1"
            } else { $null }
            candidate_source = if ($Status -eq "ACCEPTED") {
                "reviewed-fixture-candidate.json"
            } else { $null }
        }
        chapter_closeout_provenance = @()
    }
    $json = ($registry | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") +
        "`n"
    Write-Utf8File -Path (Join-Path $BookRoot `
            "editorial\source-mapping\TECHNICAL_BASELINES.json") `
        -Content $json
}

function Commit-BookBaselineRegistry {
    param(
        [Parameter(Mandatory)] [string]$BookRoot,
        [Parameter(Mandatory)] [string]$Message
    )

    [void](Invoke-TestGit -Root $BookRoot -Arguments @(
            "add", "--", "editorial/source-mapping/TECHNICAL_BASELINES.json"))
    [void](Invoke-TestGit -Root $BookRoot `
        -Arguments @("commit", "-m", $Message))
    $commit = (Invoke-TestGit -Root $BookRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $BookRoot -Arguments @(
            "update-ref", "refs/remotes/origin/main", $commit))
}

function New-GeoAuthorityFixture {
    param([Parameter(Mandatory)] [string]$GeoRoot)

    Initialize-TestRepository -Root $GeoRoot
    $fixtureScript = Join-Path $GeoRoot "tools\book\book-worktree.ps1"
    [void](New-Item -ItemType Directory -Path (Split-Path -Parent $fixtureScript) `
        -Force)
    Copy-Item -LiteralPath $SourceScript -Destination $fixtureScript
    $fixtureAgentDirectory = Join-Path $GeoRoot "tools\agent"
    [void](New-Item -ItemType Directory -Path $fixtureAgentDirectory -Force)
    foreach ($helper in @("repository-state.ps1", "evidence-integrity.ps1")) {
        Copy-Item -LiteralPath (Join-Path $RepositoryRoot "tools\agent\$helper") `
            -Destination (Join-Path $fixtureAgentDirectory $helper)
    }
    Write-Utf8File -Path (Join-Path $GeoRoot ".gitignore") `
        -Content "/book`n/artifacts/*`n"
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "docs\roadmap\geocedg_roadmap.md") -Content @"
# Fixture roadmap

| Campo | Valor |
|---|---|
| Versión documental | 1.0 |
| Fecha de revisión | 22 de agosto de 2026 |
| Estado actual | TEST = `PASS` |
| Última fase cerrada | G1 — TEST `PASS — AUTHOR APPROVED` |
| Última fase ejecutada | TEST implementation |
| Siguiente puerta | NEXT = `NOT AUTHORIZED` |
"@
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "geocedg\features\stable.yml") -Content @"
{
  "schema_version": 1,
  "set": "stable",
  "features": [
    {
      "id": "cedg.fixture.stable",
      "maturity": "stable",
      "specification": "geocedg/specs/fixture.md",
      "enabled_by_default": true,
      "depends_on": []
    }
  ]
}
"@
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "geocedg\features\experimental.yml") -Content @"
{
  "schema_version": 1,
  "set": "experimental",
  "features": []
}
"@
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "apps\geocedg\application-profile.yml") -Content @"
{
  "schema_version": 1,
  "profile_id": "geocedg-fixture",
  "serialization": {"app_code": "classic", "policy": "fixture"},
  "features": ["cedg.fixture.stable"]
}
"@
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "geocedg\specs\operations\knowledge-bundle-profiles.json") `
        -Content @"
{
  "schema_version": 1,
  "status": "NORMATIVE_AUTHOR_APPROVED",
  "implementation_status": "PASS_AUTHOR_APPROVED",
  "profiles": [
    {"id": "knowledge", "kind": "knowledge"},
    {"id": "source", "kind": "source"}
  ]
}
"@
    foreach ($path in @(
            "geocedg/specs/fixture.md",
            "geocedg/specs/operations/knowledge-bundle.schema.json",
            "models/manifests/catalog.yml",
            "geocedg/validation/regression/catalog.yml",
            "docs/references/cedg/catalog.yml")) {
        Write-Utf8File -Path (Join-Path $GeoRoot $path) -Content "{}`n"
    }
    Write-Utf8File -Path (Join-Path $GeoRoot `
            "tools\knowledge\build-knowledge-bundle.ps1") -Content @'
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$RepositoryRoot,
    [Parameter(Mandatory)][string]$Profile,
    [Parameter(Mandatory)][string]$OutputDirectory,
    [switch]$AllowDirty
)
$absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $OutputDirectory))
[void](New-Item -ItemType Directory -Path $absolute -Force)
$bundleRoot = Join-Path $absolute ("fixture-" + $Profile)
[void](New-Item -ItemType Directory -Path $bundleRoot -Force)
$head = (& git --no-optional-locks -C $RepositoryRoot rev-parse HEAD).Trim()
$dirty = @(& git --no-optional-locks -C $RepositoryRoot status --porcelain=v1 `
    --untracked-files=all).Count -gt 0
$manifest = [ordered]@{
    schema_version = 1
    bundle_id = "fixture-$Profile-$head"
    profile = $Profile
    repository = [ordered]@{
        commit = $head
        dirty = $dirty
    }
}
$manifestPath = Join-Path $bundleRoot 'manifest.json'
$manifestJson = ($manifest | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") +
    "`n"
[IO.File]::WriteAllText($manifestPath, $manifestJson,
    [Text.UTF8Encoding]::new($false))
$archivePath = Join-Path $bundleRoot 'bundle.zip'
$archivePayload = "profile=$Profile`nhead=$head`ndirty=$dirty`n"
[IO.File]::WriteAllText($archivePath, $archivePayload,
    [Text.UTF8Encoding]::new($false))
$archiveSha256 = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash
Write-Output "Knowledge bundle generated."
Write-Output "  Manifest: $manifestPath"
Write-Output "  Archive: $archivePath"
Write-Output "  Archive SHA-256: $archiveSha256"
Write-Output ""
exit 0
'@
    $paths = @(
        ".gitignore",
        "tools/book/book-worktree.ps1",
        "tools/agent/repository-state.ps1",
        "tools/agent/evidence-integrity.ps1",
        "docs/roadmap/geocedg_roadmap.md",
        "geocedg/features/stable.yml",
        "geocedg/features/experimental.yml",
        "apps/geocedg/application-profile.yml",
        "geocedg/specs/fixture.md",
        "geocedg/specs/operations/knowledge-bundle-profiles.json",
        "geocedg/specs/operations/knowledge-bundle.schema.json",
        "models/manifests/catalog.yml",
        "geocedg/validation/regression/catalog.yml",
        "docs/references/cedg/catalog.yml",
        "tools/knowledge/build-knowledge-bundle.ps1"
    )
    [void](Invoke-TestGit -Root $GeoRoot -Arguments (@("add", "--") + $paths))
    [void](Invoke-TestGit -Root $GeoRoot `
        -Arguments @("commit", "-m", "fixture published authority"))
    [void](Invoke-TestGit -Root $GeoRoot `
        -Arguments @("tag", "-a", "geocedg-test-pass", "-m", "test pass"))
    [void](Invoke-TestGit -Root $GeoRoot `
        -Arguments @("tag", "geocedg-lightweight-pass"))
    [void](Invoke-TestGit -Root $GeoRoot `
        -Arguments @("remote", "add", "origin",
            "https://github.com/mpradovelasco/GeoCeDG"))
    $commit = Invoke-TestGit -Root $GeoRoot -Arguments @("rev-parse", "HEAD")
    [void](Invoke-TestGit -Root $GeoRoot `
        -Arguments @("update-ref", "refs/remotes/origin/main", $commit))
    return [pscustomobject]@{
        Script = $fixtureScript
        Commit = $commit.Trim()
    }
}

function New-BookFixture {
    param(
        [Parameter(Mandatory)] [string]$BookRoot,
        [Parameter(Mandatory)] [string]$HistoricalCommit
    )

    Initialize-TestRepository -Root $BookRoot
    Write-Utf8File -Path (Join-Path $BookRoot "tools\verify.ps1") `
        -Content "Write-Output 'Fixture book verification passed.'`nexit 0`n"
    Write-Utf8File -Path (Join-Path $BookRoot "tools\build.ps1") `
        -Content "Write-Output 'Fixture book build passed.'`nexit 0`n"
    Set-BookBaselineRegistry -BookRoot $BookRoot `
        -HistoricalCommit $HistoricalCommit -Status "NOT_YET_REFRESHED" `
        -Commit $null -Fingerprint $null
    $paths = @(
        "tools/verify.ps1",
        "tools/build.ps1",
        "editorial/source-mapping/TECHNICAL_BASELINES.json"
    )
    [void](Invoke-TestGit -Root $BookRoot -Arguments (@("add", "--") + $paths))
    [void](Invoke-TestGit -Root $BookRoot `
        -Arguments @("commit", "-m", "fixture editorial authority"))
    [void](Invoke-TestGit -Root $BookRoot `
        -Arguments @("remote", "add", "origin",
            "https://github.com/mpradovelasco/geocedg_book"))
    $commit = Invoke-TestGit -Root $BookRoot -Arguments @("rev-parse", "HEAD")
    [void](Invoke-TestGit -Root $BookRoot `
        -Arguments @("update-ref", "refs/remotes/origin/main", $commit))
}

$tempBase = Get-NormalizedFullPath -Path ([IO.Path]::GetTempPath())
$testRoot = Join-Path $tempBase ("geocedg-book-ops-" + [guid]::NewGuid())
$geoRoot = Join-Path $testRoot "GeoCeDG"
$bookRoot = Join-Path $testRoot "geocedg_book"

try {
    $geo = New-GeoAuthorityFixture -GeoRoot $geoRoot
    New-BookFixture -BookRoot $bookRoot -HistoricalCommit $geo.Commit
    $bookLink = Join-Path $geoRoot "book"
    [void](New-Item -ItemType Junction -Path $bookLink -Target $bookRoot)

    $first = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" `
        -Arguments @("-OutputPath", "artifacts/book/run-a.json")
    Assert-OperationResult -Result $first -ExitCode 0 `
        -Contains "No editorial file was changed or accepted."
    $second = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" `
        -Arguments @("-OutputPath", "artifacts/book/run-b.json")
    Assert-OperationResult -Result $second -ExitCode 0 `
        -Contains "Technical authority fingerprint:"
    $runA = Join-Path $geoRoot "artifacts\book\run-a.json"
    $runB = Join-Path $geoRoot "artifacts\book\run-b.json"
    Assert-Condition -Condition ((Get-FileHash -LiteralPath $runA).Hash -eq
        (Get-FileHash -LiteralPath $runB).Hash) `
        -Message "Fixed-state baseline candidates are not byte-identical."
    $candidate = Get-Content -LiteralPath $runA -Raw |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    Assert-Condition -Condition (
        $candidate.published_reference.commit -eq $geo.Commit -and
        $candidate.status -eq "REVIEW_CANDIDATE_ONLY" -and
        $candidate.fingerprint_schema_version -eq 2 -and
        @($candidate.authority_categories).Count -ge 8) `
        -Message "Baseline candidate did not use published fixture authority."
    Assert-Condition -Condition (-not (@($candidate.reachable_pass_tags.name) `
                -contains "geocedg-lightweight-pass")) `
        -Message "A lightweight tag entered published pass-tag provenance."

    $dirtyNote = Join-Path $geoRoot "unpublished-working-note.txt"
    Write-Utf8File -Path $dirtyNote -Content "ignored by published authority`n"
    $dirtyCandidate = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" `
        -Arguments @("-OutputPath", "artifacts/book/run-dirty.json")
    Assert-OperationResult -Result $dirtyCandidate -ExitCode 0 `
        -Contains "No editorial file was changed or accepted."
    Assert-Condition -Condition ((Get-FileHash -LiteralPath $runA).Hash -eq
        (Get-FileHash -LiteralPath (Join-Path $geoRoot `
                    "artifacts\book\run-dirty.json")).Hash) `
        -Message "Dirty checkout context contaminated the published candidate."
    Remove-Item -LiteralPath $dirtyNote -Force

    foreach ($unsafePath in @(
            "artifacts/README.md",
            "artifacts/book/../../outside-candidate.json")) {
        $unsafe = Invoke-BookOperation -Script $geo.Script `
            -Action "BaselineCandidate" -Arguments @("-OutputPath", $unsafePath)
        Assert-OperationResult -Result $unsafe -ExitCode 1 `
            -Contains "must remain under artifacts/book/"
    }
    $trackedCandidate = Join-Path $geoRoot "artifacts\book\tracked.json"
    Write-Utf8File -Path $trackedCandidate -Content "tracked sentinel`n"
    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "add", "-f", "--", "artifacts/book/tracked.json"))
    $trackedOutput = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" `
        -Arguments @("-OutputPath", "artifacts/book/tracked.json")
    Assert-OperationResult -Result $trackedOutput -ExitCode 1 `
        -Contains "refuses to overwrite a tracked file"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("reset", "--", "artifacts/book/tracked.json"))
    Remove-Item -LiteralPath $trackedCandidate -Force

    $escapeRoot = Join-Path $testRoot "evidence-escape"
    [void](New-Item -ItemType Directory -Path $escapeRoot -Force)
    $escapeLink = Join-Path $geoRoot "artifacts\book\escape-link"
    [void](New-Item -ItemType Junction -Path $escapeLink -Target $escapeRoot)
    $escape = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" `
        -Arguments @("-OutputPath", "artifacts/book/escape-link/candidate.json")
    Assert-OperationResult -Result $escape -ExitCode 1 `
        -Contains "contains a filesystem link"
    Assert-Condition -Condition (-not (Test-Path -LiteralPath (
                Join-Path $escapeRoot "candidate.json"))) `
        -Message "Baseline candidate escaped through a filesystem link."
    Remove-Item -LiteralPath $escapeLink -Force

    $bookBeforeEvidence = Get-RepositoryReadOnlySnapshot -Root $bookRoot
    $evidence = Invoke-BookOperation -Script $geo.Script -Action "Evidence" `
        -Arguments @("-EvidenceOutputDirectory",
            "artifacts/knowledge/book/fixture")
    Assert-OperationResult -Result $evidence -ExitCode 0 `
        -Contains "Evidence disposition: RELEASE_QUALITY_PUBLISHED_STATE"
    $evidenceRoot = Join-Path $geoRoot `
        "artifacts\knowledge\book\fixture"
    $evidenceFile = Join-Path $evidenceRoot `
        "knowledge\fixture-knowledge\bundle.zip"
    $evidenceSidecarPath = Join-Path $evidenceRoot `
        "book-evidence-export.v1.json"
    $evidenceHash = (Get-FileHash -LiteralPath $evidenceFile).Hash
    $evidenceSidecarHash = (Get-FileHash `
        -LiteralPath $evidenceSidecarPath).Hash
    $evidenceSidecar = Get-Content -LiteralPath $evidenceSidecarPath -Raw |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    Assert-Condition -Condition (
        $evidenceSidecar.schema_version -eq 1 -and
        $evidenceSidecar.kind -eq "GEOCEDG_BOOK_EVIDENCE_EXPORT" -and
        $evidenceSidecar.disposition -eq
            "RELEASE_QUALITY_PUBLISHED_STATE" -and
        @($evidenceSidecar.profile_outputs).Count -eq 1 -and
        $evidenceSidecar.profile_outputs[0].profile -eq "knowledge" -and
        $evidenceSidecar.profile_outputs[0].archive.sha256 -eq
            $evidenceHash.ToLowerInvariant()) `
        -Message "Release evidence sidecar did not bind the generated bundle."
    $evidenceRepeat = Invoke-BookOperation -Script $geo.Script -Action "Evidence" `
        -Arguments @("-EvidenceOutputDirectory",
            "artifacts/knowledge/book/fixture")
    Assert-OperationResult -Result $evidenceRepeat -ExitCode 0 `
        -Contains "Evidence export completed through existing G9O1 authority."
    Assert-Condition -Condition ($evidenceHash -eq
        (Get-FileHash -LiteralPath $evidenceFile).Hash) `
        -Message "Fixed-state evidence delegation was not deterministic."
    Assert-Condition -Condition ($evidenceSidecarHash -eq
        (Get-FileHash -LiteralPath $evidenceSidecarPath).Hash) `
        -Message "Fixed-state evidence sidecar was not deterministic."
    Assert-Condition -Condition ($bookBeforeEvidence -eq
        (Get-RepositoryReadOnlySnapshot -Root $bookRoot)) `
        -Message "GeoCeDG evidence export mutated the book fixture."
    $invalidProfile = Invoke-BookOperation -Script $geo.Script `
        -Action "Evidence" -Arguments @("-EvidenceProfiles", "bad!")
    Assert-OperationResult -Result $invalidProfile -ExitCode 1 `
        -Contains "Invalid G9O1 profile identifier"

    $missing = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $missing -ExitCode 0 `
        -Contains "Book technical alignment: REFERENCE MISSING"

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $geo.Commit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-test-pass" -FingerprintSchemaVersion 2
    $uncommittedAcceptance = Invoke-BookOperation -Script $geo.Script `
        -Action "Alignment"
    Assert-OperationResult -Result $uncommittedAcceptance -ExitCode 0 `
        -Contains "Book technical alignment: REFERENCE MISSING"
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "accept fixture editorial baseline"
    $geoBefore = Get-RepositoryReadOnlySnapshot -Root $geoRoot
    $bookBefore = Get-RepositoryReadOnlySnapshot -Root $bookRoot
    $aligned = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $aligned -ExitCode 0 `
        -Contains "Book technical alignment: ALIGNED"
    Assert-Condition -Condition ($geoBefore -eq
        (Get-RepositoryReadOnlySnapshot -Root $geoRoot)) `
        -Message "Status mutated the GeoCeDG fixture."
    Assert-Condition -Condition ($bookBefore -eq
        (Get-RepositoryReadOnlySnapshot -Root $bookRoot)) `
        -Message "Status mutated the book fixture."

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "NOT_YET_REFRESHED" `
        -Commit $null -Fingerprint $null
    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "add", "--", "editorial/source-mapping/TECHNICAL_BASELINES.json"))
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("commit", "-m", "local-only editorial candidate"))
    $localBookMain = (Invoke-TestGit -Root $bookRoot `
        -Arguments @("rev-parse", "refs/heads/main")).Trim()
    $publishedBookMain = (Invoke-TestGit -Root $bookRoot `
        -Arguments @("rev-parse", "refs/remotes/origin/main")).Trim()
    Assert-Condition -Condition ($localBookMain -ne $publishedBookMain) `
        -Message "Book divergence fixture did not leave local main unpublished."
    $localOnlyBookCandidate = Invoke-BookOperation -Script $geo.Script `
        -Action "Status"
    Assert-OperationResult -Result $localOnlyBookCandidate -ExitCode 0 `
        -Contains "Book technical alignment: ALIGNED"
    Assert-Condition -Condition $localOnlyBookCandidate.Text.Contains(
        "Book main ahead/behind origin/main: 1/0") `
        -Message "Book divergence was not visible in status output."
    Assert-Condition -Condition $localOnlyBookCandidate.Text.Contains(
        "Book authority disposition: EDITORIAL CANDIDATE WORKTREE") `
        -Message "Local-only book main was presented as published authority."
    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $geo.Commit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-test-pass" -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "restore published editorial baseline"

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $geo.Commit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-missing-pass" -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "record missing accepted pass tag"
    $missingAcceptedTag = Invoke-BookOperation -Script $geo.Script `
        -Action "Alignment"
    Assert-OperationResult -Result $missingAcceptedTag -ExitCode 0 `
        -Contains "Book technical alignment: REFERENCE MISSING"
    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $geo.Commit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-test-pass" -FingerprintSchemaVersion 1
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "record unsupported fingerprint schema"
    $wrongFingerprintSchema = Invoke-BookOperation -Script $geo.Script `
        -Action "Alignment"
    Assert-OperationResult -Result $wrongFingerprintSchema -ExitCode 0 `
        -Contains "Book technical alignment: TECHNICAL CONTRADICTION"
    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $geo.Commit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-test-pass" -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "restore accepted fixture baseline"

    Write-Utf8File -Path (Join-Path $geoRoot "tools\book\bridge-note.md") `
        -Content "fixture operational-only change`n"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("add", "--", "tools/book/bridge-note.md"))
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("commit", "-m", "operational-only change"))
    $cleanUnpublishedEvidence = Invoke-BookOperation -Script $geo.Script `
        -Action "Evidence" -Arguments @("-EvidenceOutputDirectory",
            "artifacts/knowledge/book/unpublished")
    Assert-OperationResult -Result $cleanUnpublishedEvidence -ExitCode 1 `
        -Contains "Release-quality book evidence requires"
    $explicitNonReleaseEvidence = Invoke-BookOperation -Script $geo.Script `
        -Action "Evidence" -Arguments @("-EvidenceOutputDirectory",
            "artifacts/knowledge/book/unpublished", "-AllowDirtyEvidence")
    Assert-OperationResult -Result $explicitNonReleaseEvidence -ExitCode 0 `
        -Contains "NON_RELEASE_DIRTY_OR_UNPUBLISHED_STATE"
    $nonReleaseRoot = Join-Path $geoRoot `
        "artifacts\knowledge\book\unpublished"
    $nonReleaseSidecar = Get-Content -LiteralPath (Join-Path $nonReleaseRoot `
            "book-evidence-export.v1.json") -Raw |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    $nonReleaseArchive = Join-Path $nonReleaseRoot (
        [string]$nonReleaseSidecar.profile_outputs[0].archive.path)
    $nonReleaseArchiveHash = (Get-FileHash -LiteralPath $nonReleaseArchive `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    $nonReleaseHead = (Invoke-TestGit -Root $geoRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    Assert-Condition -Condition (
        $nonReleaseSidecar.schema_version -eq 1 -and
        $nonReleaseSidecar.disposition -eq
            "NON_RELEASE_DIRTY_OR_UNPUBLISHED_STATE" -and
        -not $nonReleaseSidecar.geocedg_source_state.working_tree_dirty -and
        -not $nonReleaseSidecar.geocedg_source_state.head_matches_published_pass -and
        $nonReleaseSidecar.geocedg_source_state.head_commit -eq
            $nonReleaseHead -and
        $nonReleaseSidecar.profile_outputs[0].archive.sha256 -eq
            $nonReleaseArchiveHash) `
        -Message "Clean unpublished evidence was not durably marked NON_RELEASE."
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("tag", "-a", "geocedg-test2-pass", "-m", "test 2 pass"))
    $secondCommit = (Invoke-TestGit -Root $geoRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-ref", "refs/remotes/origin/main", $secondCommit))
    $equivalent = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $equivalent -ExitCode 0 `
        -Contains "Newer commits do not change the bounded authority snapshot."

    Write-Utf8File -Path (Join-Path $geoRoot `
            "geocedg\features\stable.yml") -Content @"
{
  "schema_version": 1,
  "set": "stable",
  "features": [
    {
      "id": "cedg.fixture.changed",
      "maturity": "stable",
      "specification": "geocedg/specs/fixture.md",
      "enabled_by_default": true,
      "depends_on": []
    }
  ]
}
"@
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("add", "--", "geocedg/features/stable.yml"))
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("commit", "-m", "material feature change"))
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("tag", "-a", "geocedg-test3-pass", "-m", "test 3 pass"))
    $thirdCommit = (Invoke-TestGit -Root $geoRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-ref", "refs/remotes/origin/main", $thirdCommit))
    $stale = Invoke-BookOperation -Script $geo.Script -Action "Alignment"
    Assert-OperationResult -Result $stale -ExitCode 0 `
        -Contains "Book technical alignment: EDITORIAL BASELINE STALE"

    $materialCandidatePath = "artifacts/book/material-0.json"
    $materialCandidateResult = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate" -Arguments @("-OutputPath",
            $materialCandidatePath)
    Assert-OperationResult -Result $materialCandidateResult -ExitCode 0 `
        -Contains "Technical authority fingerprint:"
    $currentPublishedCandidate = Get-Content -LiteralPath (
        Join-Path $geoRoot $materialCandidatePath) -Raw |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    $currentPublishedCommit = $thirdCommit
    $currentPublishedTag = "geocedg-test3-pass"
    $materialCases = @(
        @{ Category = "kernel_api_semantics"; Path =
            "source/shared/common/fixture-kernel.txt" },
        @{ Category = "command_public_api"; Path =
            "source/shared/common/src/main/java/org/geogebra/common/kernel/commands/FixtureCommand.txt" },
        @{ Category = "persistence_compatibility"; Path =
            "source/desktop/desktop/src/main/java/org/geogebra/desktop/io/FixturePersistence.txt" },
        @{ Category = "gui_workflow"; Path =
            "source/desktop/desktop/src/main/java/org/geocedg/FixtureWorkflow.txt" },
        @{ Category = "validation_model_evidence"; Path =
            "geocedg/validation/fixture-evidence.json" },
        @{ Category = "bundle_source_provenance"; Path =
            "tools/knowledge/fixture-bundle-rule.txt" },
        @{ Category = "technical_specs_architecture"; Path =
            "geocedg/specs/fixture-contract.md" }
    )
    $materialIndex = 0
    foreach ($case in $materialCases) {
        $materialIndex++
        Write-Utf8File -Path (Join-Path $geoRoot $case.Path) `
            -Content "material fixture $materialIndex`n"
        [void](Invoke-TestGit -Root $geoRoot `
            -Arguments @("add", "--", $case.Path))
        [void](Invoke-TestGit -Root $geoRoot `
            -Arguments @("commit", "-m", "material $($case.Category) change"))
        $currentPublishedTag = "geocedg-material${materialIndex}-pass"
        [void](Invoke-TestGit -Root $geoRoot -Arguments @(
                "tag", "-a", $currentPublishedTag, "-m",
                "material fixture $materialIndex"))
        $currentPublishedCommit = (Invoke-TestGit -Root $geoRoot `
            -Arguments @("rev-parse", "HEAD")).Trim()
        [void](Invoke-TestGit -Root $geoRoot -Arguments @(
                "update-ref", "refs/remotes/origin/main",
                $currentPublishedCommit))
        $nextPath = "artifacts/book/material-${materialIndex}.json"
        $nextResult = Invoke-BookOperation -Script $geo.Script `
            -Action "BaselineCandidate" `
            -Arguments @("-OutputPath", $nextPath)
        Assert-OperationResult -Result $nextResult -ExitCode 0 `
            -Contains "Technical authority fingerprint:"
        $nextCandidate = Get-Content -LiteralPath (Join-Path $geoRoot $nextPath) `
            -Raw | ConvertFrom-Json -Depth 100 -NoEnumerate
        $beforeCategory = @($currentPublishedCandidate.authority_categories |
            Where-Object category -eq $case.Category)[0]
        $afterCategory = @($nextCandidate.authority_categories |
            Where-Object category -eq $case.Category)[0]
        Assert-Condition -Condition (
            $nextCandidate.technical_authority_fingerprint -ne
                $currentPublishedCandidate.technical_authority_fingerprint -and
            $afterCategory.fingerprint -ne $beforeCategory.fingerprint) `
            -Message "Material category was invisible: $($case.Category)"
        $currentPublishedCandidate = $nextCandidate
    }
    $categorizedStale = Invoke-BookOperation -Script $geo.Script `
        -Action "Alignment"
    Assert-OperationResult -Result $categorizedStale -ExitCode 0 `
        -Contains "Book alignment material drift:"

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $currentPublishedCommit `
        -Fingerprint $candidate.technical_authority_fingerprint `
        -PublishedTag $currentPublishedTag -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "record contradictory fixture fingerprint"
    $contradiction = Invoke-BookOperation -Script $geo.Script -Action "Alignment"
    Assert-OperationResult -Result $contradiction -ExitCode 0 `
        -Contains "Book technical alignment: TECHNICAL CONTRADICTION"

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit ("0" * 40) -Fingerprint ("0" * 64) `
        -PublishedTag $currentPublishedTag -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "record missing fixture reference"
    $referenceMissing = Invoke-BookOperation -Script $geo.Script `
        -Action "Alignment"
    Assert-OperationResult -Result $referenceMissing -ExitCode 0 `
        -Contains "Book technical alignment: REFERENCE MISSING"

    Write-Utf8File -Path (Join-Path $geoRoot "future-candidate.txt") `
        -Content "unpublished fixture state`n"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("add", "--", "future-candidate.txt"))
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("commit", "-m", "unpublished candidate"))
    $futureCommit = (Invoke-TestGit -Root $geoRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "tag", "-a", "geocedg-future-pass", "-m", "future fixture pass"))
    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "ACCEPTED" `
        -Commit $futureCommit `
        -Fingerprint $currentPublishedCandidate.technical_authority_fingerprint `
        -PublishedTag "geocedg-future-pass" -FingerprintSchemaVersion 2
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "record unpublished fixture baseline"
    $unpublished = Invoke-BookOperation -Script $geo.Script -Action "Alignment"
    Assert-OperationResult -Result $unpublished -ExitCode 0 `
        -Contains "Book technical alignment: UNPUBLISHED PRODUCT STATE"

    Set-BookBaselineRegistry -BookRoot $bookRoot `
        -HistoricalCommit $geo.Commit -Status "NOT_YET_REFRESHED" `
        -Commit $null -Fingerprint $null
    Commit-BookBaselineRegistry -BookRoot $bookRoot `
        -Message "reset fixture editorial baseline"
    $verify = Invoke-BookOperation -Script $geo.Script -Action "Verify"
    Assert-OperationResult -Result $verify -ExitCode 0 `
        -Contains "Fixture book verification passed."
    $build = Invoke-BookOperation -Script $geo.Script -Action "Build"
    Assert-OperationResult -Result $build -ExitCode 0 `
        -Contains "Fixture book build passed."
    Write-Utf8File -Path (Join-Path $bookRoot "tools\verify.ps1") `
        -Content "Write-Output 'Intentional fixture failure.'`nexit 7`n"
    $verifyFailure = Invoke-BookOperation -Script $geo.Script -Action "Verify"
    Assert-OperationResult -Result $verifyFailure -ExitCode 1 `
        -Contains "Book Verify failed with exit code 7."
    Write-Utf8File -Path (Join-Path $bookRoot "tools\verify.ps1") `
        -Content "Write-Output 'Fixture book verification passed.'`nexit 0`n"

    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "switch", "-c", "copilot/geo-ce-dg-book-analysis"))
    Write-Utf8File -Path (Join-Path $bookRoot "generated-fixture.md") `
        -Content "unauthorized generated content`n"
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("add", "--", "generated-fixture.md"))
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("commit", "-m", "unauthorized generated fixture"))
    $quarantineAnchor = (Invoke-TestGit -Root $bookRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "update-ref", "refs/remotes/origin/copilot/geo-ce-dg-book-analysis",
            $quarantineAnchor))
    Write-Utf8File -Path (Join-Path $bookRoot "generated-advance.md") `
        -Content "advanced unauthorized content`n"
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("add", "--", "generated-advance.md"))
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("commit", "-m", "advance unauthorized fixture"))
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("branch", "-m", "quarantine-advanced"))
    $quarantine = Invoke-BookOperation -Script $geo.Script -Action "Verify"
    Assert-OperationResult -Result $quarantine -ExitCode 1 `
        -Contains "quarantined generated branch"
    [void](Invoke-TestGit -Root $bookRoot -Arguments @("switch", "main"))
    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "update-ref", "-d",
            "refs/remotes/origin/copilot/geo-ce-dg-book-analysis"))

    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("remote", "set-url", "origin",
            "https://secret-token@example.invalid/geocedg_book"))
    $wrongOrigin = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $wrongOrigin -ExitCode 1 `
        -Contains "Unexpected book origin identity"
    Assert-Condition -Condition (-not $wrongOrigin.Text.Contains("secret-token")) `
        -Message "Book origin credentials leaked to output."
    [void](Invoke-TestGit -Root $bookRoot `
        -Arguments @("remote", "set-url", "origin",
            "https://github.com/mpradovelasco/geocedg_book"))

    Write-Utf8File -Path (Join-Path $geoRoot ".gitignore") `
        -Content "/artifacts/*`n"
    $unignored = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $unignored -ExitCode 1 `
        -Contains "not ignored by GeoCeDG"
    Write-Utf8File -Path (Join-Path $geoRoot ".gitignore") `
        -Content "/book`n/artifacts/*`n"

    Remove-Item -LiteralPath $bookLink -Force
    $absent = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $absent -ExitCode 1 `
        -Contains "optional local book link is not configured"
    [void](New-Item -ItemType Directory -Path $bookLink)
    $ordinary = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $ordinary -ExitCode 1 `
        -Contains "must be a filesystem link"
    Remove-Item -LiteralPath $bookLink -Force

    [void](New-Item -ItemType Junction -Path $bookLink `
        -Target (Join-Path $bookRoot "editorial"))
    $nonRootTarget = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $nonRootTarget -ExitCode 1 `
        -Contains "root of its Git worktree"
    Remove-Item -LiteralPath $bookLink -Force

    $independentBookWorktree = Join-Path $testRoot `
        "independent-book-worktree"
    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "worktree", "add", "--detach", $independentBookWorktree,
            "refs/remotes/origin/main"))
    [void](New-Item -ItemType Junction -Path $bookLink `
        -Target $independentBookWorktree)
    $independentWorktree = Invoke-BookOperation -Script $geo.Script `
        -Action "Status"
    Assert-OperationResult -Result $independentWorktree -ExitCode 0 `
        -Contains "GeoCeDG external book worktree boundary: PASS"
    Remove-Item -LiteralPath $bookLink -Force
    [void](Invoke-TestGit -Root $bookRoot -Arguments @(
            "worktree", "remove", "--force", $independentBookWorktree))

    $sharedAuthorityBook = Join-Path $testRoot "shared-authority-book"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("worktree", "add", "--detach", $sharedAuthorityBook, "HEAD"))
    [void](New-Item -ItemType Junction -Path $bookLink `
        -Target $sharedAuthorityBook)
    $sharedAuthority = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $sharedAuthority -ExitCode 1 `
        -Contains "same Git authority"
    Remove-Item -LiteralPath $bookLink -Force
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("worktree", "remove", "--force", $sharedAuthorityBook))

    $aliasedMetadataBook = Join-Path $testRoot "aliased-metadata-book"
    $actualAliasedMetadata = Join-Path $testRoot `
        "aliased-metadata-actual.git"
    $metadataAlias = Join-Path $testRoot "aliased-metadata-junction.git"
    [void](New-Item -ItemType Directory -Path $actualAliasedMetadata)
    [void](New-Item -ItemType Junction -Path $metadataAlias `
        -Target $actualAliasedMetadata)
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
            "init", "-b", "main", "--separate-git-dir=$metadataAlias",
            $aliasedMetadataBook))
    [void](Invoke-TestGit -Root $aliasedMetadataBook `
        -Arguments @("config", "user.name", "Aliased metadata fixture"))
    [void](Invoke-TestGit -Root $aliasedMetadataBook `
        -Arguments @("config", "user.email", "alias@example.invalid"))
    Write-Utf8File -Path (Join-Path $aliasedMetadataBook "README.md") `
        -Content "aliased metadata fixture`n"
    [void](Invoke-TestGit -Root $aliasedMetadataBook `
        -Arguments @("add", "--", "README.md"))
    [void](Invoke-TestGit -Root $aliasedMetadataBook `
        -Arguments @("commit", "-m", "aliased metadata fixture"))
    [void](Invoke-TestGit -Root $aliasedMetadataBook -Arguments @(
            "remote", "add", "origin",
            "https://github.com/mpradovelasco/geocedg_book"))
    $aliasedMetadataCommit = (Invoke-TestGit -Root $aliasedMetadataBook `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $aliasedMetadataBook -Arguments @(
            "update-ref", "refs/remotes/origin/main", $aliasedMetadataCommit))
    $gitPointerPath = Join-Path $aliasedMetadataBook ".git"
    [IO.File]::SetAttributes($gitPointerPath, [IO.FileAttributes]::Normal)
    $metadataAliasGitPath = $metadataAlias.Replace("\", "/")
    Write-Utf8File -Path $gitPointerPath `
        -Content "gitdir: $metadataAliasGitPath`n"
    [void](New-Item -ItemType Junction -Path $bookLink `
        -Target $aliasedMetadataBook)
    $aliasedMetadata = Invoke-BookOperation -Script $geo.Script `
        -Action "Status"
    Assert-OperationResult -Result $aliasedMetadata -ExitCode 1 `
        -Contains "contains a filesystem link or alias"
    Remove-Item -LiteralPath $bookLink -Force
    Remove-Item -LiteralPath $metadataAlias -Force

    $metadataBook = Join-Path $testRoot "metadata-book"
    $nestedGitMetadata = Join-Path $geoRoot ".nested-book-git"
    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "init", "-b", "main", "--separate-git-dir=$nestedGitMetadata",
            $metadataBook))
    [void](Invoke-TestGit -Root $metadataBook `
        -Arguments @("config", "user.name", "Nested metadata fixture"))
    [void](Invoke-TestGit -Root $metadataBook `
        -Arguments @("config", "user.email", "nested@example.invalid"))
    Write-Utf8File -Path (Join-Path $metadataBook "README.md") `
        -Content "nested metadata fixture`n"
    [void](Invoke-TestGit -Root $metadataBook `
        -Arguments @("add", "--", "README.md"))
    [void](Invoke-TestGit -Root $metadataBook `
        -Arguments @("commit", "-m", "nested metadata fixture"))
    [void](Invoke-TestGit -Root $metadataBook -Arguments @(
            "remote", "add", "origin",
            "https://github.com/mpradovelasco/geocedg_book"))
    [void](New-Item -ItemType Junction -Path $bookLink -Target $metadataBook)
    $nestedMetadata = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $nestedMetadata -ExitCode 1 `
        -Contains "nested Git metadata authority"
    Remove-Item -LiteralPath $bookLink -Force
    [void](New-Item -ItemType Junction -Path $bookLink -Target $bookRoot)

    $nestedBook = Join-Path $geoRoot "nested-book-repository"
    Initialize-TestRepository -Root $nestedBook
    Remove-Item -LiteralPath $bookLink -Force
    [void](New-Item -ItemType Junction -Path $bookLink -Target $nestedBook)
    $nested = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $nested -ExitCode 1 `
        -Contains "exterior, non-nested worktrees"
    Remove-Item -LiteralPath $bookLink -Force
    Remove-Item -LiteralPath $nestedBook -Recurse -Force
    [void](New-Item -ItemType Junction -Path $bookLink -Target $bookRoot)

    $blob = (Invoke-TestGit -Root $geoRoot `
        -Arguments @("rev-parse", "HEAD:tools/book/book-worktree.ps1")).Trim()
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-index", "--add", "--cacheinfo",
            "100644,$blob,book"))
    $tracked = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $tracked -ExitCode 1 `
        -Contains "index contains the local book path"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-index", "--force-remove", "book"))

    $bookCommitForGitlink = (Invoke-TestGit -Root $bookRoot `
        -Arguments @("rev-parse", "HEAD")).Trim()
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-index", "--add", "--cacheinfo",
            "160000,$bookCommitForGitlink,book"))
    $gitlink = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $gitlink -ExitCode 1 `
        -Contains "index contains the local book path"
    [void](Invoke-TestGit -Root $geoRoot `
        -Arguments @("update-index", "--force-remove", "book"))

    Write-Utf8File -Path (Join-Path $geoRoot ".gitmodules") -Content @"
[submodule "book"]
    path = ./book
    url = https://github.com/mpradovelasco/geocedg_book
"@
    $submodule = Invoke-BookOperation -Script $geo.Script -Action "Status"
    Assert-OperationResult -Result $submodule -ExitCode 1 `
        -Contains "declares book as a submodule"
    Remove-Item -LiteralPath (Join-Path $geoRoot ".gitmodules") -Force

    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "remote", "set-url", "origin",
            "https://geo-secret@example.invalid/GeoCeDG"))
    $wrongGeoOrigin = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate"
    Assert-OperationResult -Result $wrongGeoOrigin -ExitCode 1 `
        -Contains "Unexpected GeoCeDG origin identity"
    Assert-Condition -Condition (-not $wrongGeoOrigin.Text.Contains(
            "geo-secret")) `
        -Message "GeoCeDG origin credentials leaked to output."
    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "remote", "set-url", "origin",
            "https://github.com/mpradovelasco/GeoCeDG"))

    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "update-ref", "-d", "refs/remotes/origin/main"))
    $missingPublishedRef = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate"
    Assert-OperationResult -Result $missingPublishedRef -ExitCode 1 `
        -Contains "origin/main is unavailable"
    [void](Invoke-TestGit -Root $geoRoot -Arguments @(
            "update-ref", "refs/remotes/origin/main", $currentPublishedCommit))

    $tagInventory = Invoke-TestGit -Root $geoRoot -Arguments @(
        "for-each-ref", "--format=%(refname:short) %(objecttype)",
        "refs/tags/geocedg-*-pass")
    $annotatedTags = @(($tagInventory -split "`n") | Where-Object {
            $_ -match '\stag$'
        } | ForEach-Object { ($_ -split '\s+', 2)[0] })
    foreach ($tag in $annotatedTags) {
        [void](Invoke-TestGit -Root $geoRoot -Arguments @("tag", "-d", $tag))
    }
    $lightweightOnly = Invoke-BookOperation -Script $geo.Script `
        -Action "BaselineCandidate"
    Assert-OperationResult -Result $lightweightOnly -ExitCode 1 `
        -Contains "No annotated GeoCeDG pass tag"

    Write-Host "BOOK-P0-post book operational fixture tests passed."
} finally {
    if (Test-Path -LiteralPath $testRoot) {
        $resolvedTestRoot = [IO.Path]::GetFullPath($testRoot)
        Assert-Condition -Condition ($resolvedTestRoot.StartsWith(
                $tempBase + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) `
            -Message "Refusing to remove an unsafe fixture path: $resolvedTestRoot"
        $fixtureLink = Join-Path $geoRoot "book"
        if (Test-Path -LiteralPath $fixtureLink) {
            Remove-Item -LiteralPath $fixtureLink -Force
        }
        Remove-Item -LiteralPath $resolvedTestRoot -Recurse -Force
    }
}

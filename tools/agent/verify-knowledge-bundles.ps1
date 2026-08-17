[CmdletBinding()]
param(
    [string]$LogDirectory = (Join-Path -Path ([IO.Path]::GetTempPath()) `
        -ChildPath "geocedg-g9o1-knowledge-bundles")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (
    Join-Path $PSScriptRoot "..\..")).Path
$ExpectedBranch = "feature/g9o1-source-knowledge-bundles-guides"
$G9PCommit = "94f92f49a44560e44bae9e75ba52595067471368"
$G9PTagObject = "6ce37f03df6f742aa448323d2150dd1655c986a5"
$G9PTagName = "geocedg-g9p-pass"
$G9O1TagName = "geocedg-g9o1-pass"
$PromptPath =
    ".github/prompts/tasks/g9o1-source-knowledge-bundles-and-guides.prompt.md"
$PromptSha256 =
    "b0b04ee4095423fd76f4ecd18a9ea567c091d23197b9bdfd92c736c25b1b9ed6"
$G9A1PromptPath =
    ".github/prompts/tasks/g9a1-spatial-identity-persistence-foundation.prompt.md"
$G9A1PromptSha256 =
    "50c665a399b7b6290b8dcf86cc2326bb78202d85d7b52b130fd8ebf2980127e1"
$IntegrityManifestPath = "geocedg/validation/g9o1/g9o1-evidence.sha256"
$LogRoot = [IO.Path]::GetFullPath($LogDirectory)
$SummaryLog = Join-Path $LogRoot "g9o1-knowledge-bundles.log"
$TestLog = Join-Path $LogRoot "knowledge-bundle-tests.log"
$InitialStatus = $null
$AuthorityCommit = $null

. (Join-Path $PSScriptRoot "evidence-integrity.ps1")

$RequiredFiles = @(
    $PromptPath,
    $G9A1PromptPath,
    "tools/agent/evidence-integrity.ps1",
    "tools/knowledge/knowledge-bundle.psm1",
    "tools/knowledge/build-knowledge-bundle.ps1",
    "tools/knowledge/verify-knowledge-bundle.ps1",
    "tools/knowledge/tests/knowledge-bundle.tests.ps1",
    "tools/knowledge/tests/fixtures/knowledge-bundle-profiles.fixture.json",
    "tools/knowledge/tests/fixtures/README.md",
    "tools/agent/verify-knowledge-bundles.ps1",
    "geocedg/specs/operations/knowledge-bundles.md",
    "geocedg/specs/operations/documentation-maintenance.md",
    "geocedg/specs/operations/knowledge-bundle.schema.json",
    "geocedg/specs/operations/knowledge-bundle-profiles.json",
    "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md",
    "docs/architecture/knowledge_bundle_architecture.md",
    "docs/architecture/geocedg_documentation_architecture.md",
    "docs/developer/geocedg_developer_guide.md",
    "docs/developer/geocedg_agent_prompt_guide.md",
    "docs/developer/repository_map.md",
    "docs/user/geocedg_user_guide.md",
    "docs/validation/g9_documentation_bundle_traceability.md",
    "docs/validation/g9o1_source_knowledge_bundles_guides_report.md",
    "docs/roadmap/geocedg_roadmap.md",
    "geocedg/specs/README.md",
    "geocedg/validation/g9o1/g9o1-evidence.json",
    $IntegrityManifestPath)

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
        -Message "Required path escapes repository: $RelativePath"
    Assert-Condition -Condition (
        Test-Path -LiteralPath $absolute -PathType Leaf) `
        -Message "Required G9O1 artifact is missing: $RelativePath"
    return $absolute
}

function Get-CanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    $bytes = [IO.File]::ReadAllBytes($Path)
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

function Get-AuthorityText {
    param([Parameter(Mandatory)] [string]$RelativePath)
    if ($null -ne $AuthorityCommit) {
        $bytes = Get-GeoCeDGFrozenBlobBytes -RepositoryRoot $RepositoryRoot `
            -Path $RelativePath -Commit $AuthorityCommit
        return ConvertFrom-GeoCeDGStrictUtf8 -Bytes $bytes
    }
    return (Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile -RelativePath $RelativePath))
}

function Get-AuthorityCanonicalTextSha256 {
    param([Parameter(Mandatory)] [string]$RelativePath)
    if ($null -ne $AuthorityCommit) {
        return (Get-GeoCeDGFrozenCanonicalTextSha256 `
            -RepositoryRoot $RepositoryRoot -Path $RelativePath `
            -Commit $AuthorityCommit)
    }
    return (Get-CanonicalTextSha256 -Path (
        Resolve-RequiredFile -RelativePath $RelativePath))
}

function Read-JsonFile {
    param([Parameter(Mandatory)] [string]$RelativePath)
    try {
        return Get-AuthorityText -RelativePath $RelativePath |
            ConvertFrom-Json -Depth 100
    } catch {
        throw "Invalid JSON in ${RelativePath}: $($_.Exception.Message)"
    }
}

function Assert-Contains {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string[]]$Values
    )
    $content = Get-AuthorityText -RelativePath $RelativePath
    foreach ($value in $Values) {
        Assert-Condition -Condition $content.Contains($value) `
            -Message "$RelativePath does not contain required text: $value"
    }
}

function Assert-PowerShellParses {
    $scripts = @(
        "tools/agent/evidence-integrity.ps1",
        "tools/knowledge/knowledge-bundle.psm1",
        "tools/knowledge/build-knowledge-bundle.ps1",
        "tools/knowledge/verify-knowledge-bundle.ps1",
        "tools/knowledge/tests/knowledge-bundle.tests.ps1",
        "tools/agent/verify-knowledge-bundles.ps1",
        "tools/agent/verify-g9p-design.ps1",
        "tools/agent/verify-operational.ps1",
        "tools/agent/verify.ps1")
    foreach ($relativePath in $scripts) {
        $tokens = $null
        $errors = $null
        [void][Management.Automation.Language.Parser]::ParseFile(
            (Resolve-RequiredFile -RelativePath $relativePath),
            [ref]$tokens, [ref]$errors)
        Assert-Condition -Condition ($errors.Count -eq 0) `
            -Message "PowerShell parse failure: $relativePath"
    }
    return $scripts.Count
}

function Assert-MarkdownLinks {
    param([Parameter(Mandatory)] [string[]]$RelativePaths)
    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd(
        [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    $checked = 0
    foreach ($relativePath in $RelativePaths) {
        $document = Resolve-RequiredFile -RelativePath $relativePath
        $content = Get-Content -Raw -LiteralPath $document
        foreach ($match in [regex]::Matches(
                $content, '\[[^\]]*\]\((?<target>[^)]+)\)')) {
            $target = $match.Groups["target"].Value.Trim().Trim("<", ">")
            if ($target.StartsWith("#") -or
                    $target -match '^[A-Za-z][A-Za-z0-9+.-]*:') {
                continue
            }
            $pathPart = ($target -split '#', 2)[0]
            if ([string]::IsNullOrWhiteSpace($pathPart)) { continue }
            $resolved = [IO.Path]::GetFullPath((Join-Path `
                (Split-Path -Parent $document) `
                ([Uri]::UnescapeDataString($pathPart))))
            Assert-Condition -Condition ($resolved.StartsWith(
                    $root, [StringComparison]::OrdinalIgnoreCase) -and
                    (Test-Path -LiteralPath $resolved)) `
                -Message "Broken/unsafe Markdown link in ${relativePath}: $target"
        }
        $checked++
    }
    return $checked
}

function Assert-CurrentIntegrityManifest {
    $manifest = Resolve-RequiredFile -RelativePath $IntegrityManifestPath
    $seen = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    $validated = 0
    foreach ($line in (Get-Content -LiteralPath $manifest)) {
        if ([string]::IsNullOrWhiteSpace($line) -or
                $line.TrimStart().StartsWith("#")) {
            continue
        }
        $parts = $line -split "\s+", 2
        Assert-Condition -Condition ($parts.Count -eq 2 -and
                $parts[0] -match '^[0-9a-f]{64}$') `
            -Message "Malformed G9O1 integrity entry: $line"
        $relativePath = $parts[1].Trim().Replace("\", "/")
        Assert-Condition -Condition $seen.Add($relativePath) `
            -Message "Duplicate G9O1 integrity path: $relativePath"
        $actual = Get-CanonicalTextSha256 -Path (
            Resolve-RequiredFile -RelativePath $relativePath)
        Assert-Condition -Condition ($actual -eq $parts[0]) `
            -Message "G9O1 integrity mismatch: $relativePath"
        $validated++
    }
    return $validated
}

function Assert-Scope {
    $paths = if ($null -ne $AuthorityCommit) {
        @(& git -C $RepositoryRoot diff --name-only $G9PCommit `
            $AuthorityCommit --)
    } else {
        @(& git -C $RepositoryRoot diff --name-only $G9PCommit --)
    }
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to enumerate tracked G9O1 changes."
    if ($null -eq $AuthorityCommit) {
        $paths += @(& git -C $RepositoryRoot ls-files --others `
            --exclude-standard)
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to enumerate untracked G9O1 changes."
    }
    $paths = @($paths | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_) } | ForEach-Object {
            $_.Replace("\", "/") } | Sort-Object -Unique)

    $allowedExact = @(
        "README.md",
        "artifacts/README.md",
        "geocedg/specs/operations/knowledge-bundles.md",
        "geocedg/specs/operations/documentation-maintenance.md",
        "geocedg/specs/operations/knowledge-bundle.schema.json",
        "geocedg/specs/operations/knowledge-bundle-profiles.json",
        "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md",
        "docs/architecture/knowledge_bundle_architecture.md",
        "docs/architecture/geocedg_documentation_architecture.md",
        "docs/developer/geocedg_developer_guide.md",
        "docs/developer/geocedg_agent_prompt_guide.md",
        "docs/developer/repository_map.md",
        "docs/user/geocedg_user_guide.md",
        "docs/validation/g9_documentation_bundle_traceability.md",
        "docs/validation/g9o1_source_knowledge_bundles_guides_report.md",
        "docs/roadmap/geocedg_roadmap.md",
        "geocedg/specs/README.md",
        $G9A1PromptPath,
        "tools/agent/verify-knowledge-bundles.ps1",
        "tools/agent/verify-g9p-design.ps1",
        "tools/agent/verify-operational.ps1",
        "tools/agent/verify.ps1")
    foreach ($path in $paths) {
        $allowed = $allowedExact.Contains($path) -or
            $path.StartsWith("tools/knowledge/") -or
            $path.StartsWith("geocedg/validation/g9o1/")
        Assert-Condition -Condition $allowed `
            -Message "Path outside canonical G9O1 scope: $path"
        foreach ($forbiddenPrefix in @(
                "source/", "apps/", "python/", "packaging/",
                "geocedg/features/", "geocedg/resources/",
                "geocedg/specs/spatial/", "geocedg/specs/locus/",
                "geocedg/specs/ui/", "geocedg/specs/export/",
                ".github/prompts/")) {
            $isAuthorizedPromptStatus = $path -eq $G9A1PromptPath
            Assert-Condition -Condition ($isAuthorizedPromptStatus -or
                    -not $path.StartsWith($forbiddenPrefix)) `
                -Message "Forbidden G9O1 semantic/prompt scope edit: $path"
        }
        Assert-Condition -Condition ($path -notmatch
                '\.(java|kt|kts|gradle|class|jar)$') `
            -Message "Productive application/kernel source changed: $path"
    }
    return $paths.Count
}

function Invoke-FixtureTests {
    $testScript = Resolve-RequiredFile `
        -RelativePath "tools/knowledge/tests/knowledge-bundle.tests.ps1"
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "pwsh"
    $startInfo.WorkingDirectory = $RepositoryRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in @(
            "-NoLogo", "-NoProfile", "-File", $testScript,
            "-RepositoryRoot", $RepositoryRoot)) {
        [void]$startInfo.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    try {
        Assert-Condition -Condition $process.Start() `
            -Message "Unable to start the G9O1 fixture tests."
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        $combined = $stdout
        if (-not [string]::IsNullOrWhiteSpace($stderr)) {
            $combined += $stderr
        }
        [IO.File]::WriteAllText($TestLog,
            $combined.Replace("`r`n", "`n").Replace("`r", "`n"),
            [Text.UTF8Encoding]::new($false))
        Assert-Condition -Condition ($process.ExitCode -eq 0) `
            -Message "G9O1 fixture tests failed with exit code $($process.ExitCode). See $TestLog"
        Assert-Condition -Condition ($stdout.Contains(
                "G9O1 knowledge-bundle tests passed.")) `
            -Message "G9O1 fixture tests did not emit their success marker."
        return $process.ExitCode
    } finally {
        $process.Dispose()
    }
}

try {
    [IO.Directory]::CreateDirectory($LogRoot) | Out-Null
    $InitialStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "Unable to read initial repository status."

    $branch = (& git -C $RepositoryRoot branch --show-current).Trim()
    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    $localMain = (& git -C $RepositoryRoot rev-parse main).Trim()
    $originMain = (& git -C $RepositoryRoot rev-parse origin/main).Trim()
    & git -C $RepositoryRoot show-ref --verify --quiet `
        "refs/tags/$G9O1TagName"
    $g9o1TagExists = $LASTEXITCODE -eq 0
    Assert-Condition -Condition ($g9o1TagExists -or $LASTEXITCODE -eq 1) `
        -Message "Unable to resolve the G9O1 completion tag."
    if ($g9o1TagExists) {
        $g9o1TagObject = (& git -C $RepositoryRoot rev-parse `
            "refs/tags/$G9O1TagName").Trim()
        $g9o1TagType = (& git -C $RepositoryRoot cat-file -t `
            $g9o1TagObject).Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
                $g9o1TagType -eq "tag") `
            -Message "G9O1 completion tag is missing or not annotated."
        $AuthorityCommit = (& git -C $RepositoryRoot rev-parse `
            "$g9o1TagObject^{}").Trim()
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Unable to peel the G9O1 completion tag."
        & git -C $RepositoryRoot merge-base --is-ancestor $G9PCommit `
            $AuthorityCommit
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "G9O1 completion does not descend from G9P."
        & git -C $RepositoryRoot merge-base --is-ancestor $AuthorityCommit HEAD
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Current HEAD does not descend from G9O1 completion."
    } else {
        Assert-Condition -Condition ($branch -eq $ExpectedBranch -and
                $head -eq $G9PCommit -and $localMain -eq $G9PCommit -and
                $originMain -eq $G9PCommit) `
            -Message "G9O1 branch/HEAD/main entry authority drifted."
    }
    $tagObject = (& git -C $RepositoryRoot rev-parse `
        "refs/tags/$G9PTagName").Trim()
    $peeled = (& git -C $RepositoryRoot rev-parse "$G9PTagObject^{}").Trim()
    Assert-Condition -Condition ($tagObject -eq $G9PTagObject -and
            $peeled -eq $G9PCommit) `
        -Message "Published G9P tag authority drifted."

    foreach ($relativePath in $RequiredFiles) {
        [void](Resolve-RequiredFile -RelativePath $relativePath)
    }
    $promptHash = Get-AuthorityCanonicalTextSha256 -RelativePath $PromptPath
    Assert-Condition -Condition ($promptHash -eq $PromptSha256) `
        -Message "Canonical G9O1 prompt LF SHA-256 mismatch."
    $g9a1PromptHash = Get-AuthorityCanonicalTextSha256 `
        -RelativePath $G9A1PromptPath
    Assert-Condition -Condition ($g9a1PromptHash -eq $G9A1PromptSha256) `
        -Message "Authorized G9A1 prompt LF SHA-256 mismatch."

    foreach ($spec in @(
            "geocedg/specs/spatial/g9-spatial-projection-semantics.md",
            "geocedg/specs/locus/locus-v2-public-surface.md",
            "geocedg/specs/ui/cedg-workspaces.md",
            "geocedg/specs/export/dxf-curve-fidelity-and-approximation.md",
            "geocedg/specs/operations/documentation-maintenance.md",
            "geocedg/specs/operations/knowledge-bundles.md")) {
        Assert-Contains -RelativePath $spec `
            -Values @("NORMATIVE / AUTHOR APPROVED")
    }
    foreach ($adr in 10..15) {
        $adrRoot = Join-Path -Path $RepositoryRoot -ChildPath "docs\adr"
        $match = @(Get-ChildItem -LiteralPath $adrRoot `
            -Filter ("{0:D4}-*.md" -f $adr) -File)
        Assert-Condition -Condition ($match.Count -eq 1) `
            -Message "Expected exactly one ADR $adr."
        Assert-Condition -Condition ((Get-Content -Raw $match[0].FullName).
                Contains("Status: **Accepted**")) `
            -Message "ADR $adr is not Accepted."
    }

    $profiles = Read-JsonFile `
        -RelativePath "geocedg/specs/operations/knowledge-bundle-profiles.json"
    $schemaPath = Resolve-RequiredFile `
        -RelativePath "geocedg/specs/operations/knowledge-bundle.schema.json"
    Get-Content -Raw -LiteralPath $schemaPath | Test-Json -ErrorAction Stop |
        Out-Null
    Assert-Condition -Condition ($profiles.schema_version -eq 1 -and
            $profiles.status -eq "NORMATIVE_AUTHOR_APPROVED" -and
            $profiles.implementation_status -eq
                "PASS_AUTHOR_APPROVED" -and
            $profiles.profiles.Count -eq 7 -and
            $profiles.default_budgets.maximum_chunk_tokens -gt 0) `
        -Message "Knowledge-bundle profile authority/status is inconsistent."
    $sourceProfiles = @($profiles.profiles | Where-Object { $_.id -eq "source" })
    Assert-Condition -Condition ($sourceProfiles.Count -eq 1 -and
            "source/**" -in @($sourceProfiles[0].include) -and
            "UPSTREAM_MODIFIED" -in @($sourceProfiles[0].ownership)) `
        -Message "Source profile does not cover registered upstream modifications."

    $evidence = Read-JsonFile `
        -RelativePath "geocedg/validation/g9o1/g9o1-evidence.json"
    Assert-Condition -Condition ($evidence.phase -eq "G9O1" -and
            $evidence.status -eq "PASS_AUTHOR_APPROVED" -and
            -not [bool]$evidence.approval.selfApproved -and
            [bool]$evidence.approval.authorApproved -and
            [bool]$evidence.approval.passClaimed -and
            [bool]$evidence.approval.promotionAuthorized -and
            [bool]$evidence.approval.tagAuthorized -and
            [bool]$evidence.approval.nextBranchAuthorized -and
            $evidence.phaseDisposition.G9A1 -eq "AUTHORIZED_NOT_STARTED" -and
            -not [bool]$evidence.nextPhase.executed -and
            $evidence.scopeAudit.generatedBundleFilesTracked -eq 0 -and
            $evidence.phaseDisposition.productiveSpatialG9 -eq "NOT_STARTED") `
        -Message "G9O1 author-closeout evidence is inconsistent."

    Assert-Contains -RelativePath `
        "geocedg/specs/operations/knowledge-bundles.md" -Values @(
        "PASS — AUTHOR APPROVED",
        "tools/knowledge/build-knowledge-bundle.ps1",
        "tools/agent/verify-knowledge-bundles.ps1")
    Assert-Contains -RelativePath `
        "docs/developer/geocedg_agent_prompt_guide.md" -Values @(
        "build-knowledge-bundle.ps1", "-AllowDirty",
        "NON_RELEASE_EVIDENCE", "G9A1 is authorized but remains unexecuted")
    Assert-Contains -RelativePath `
        "docs/roadmap/geocedg_roadmap.md" -Values @(
        "G9O1 = PASS — AUTHOR APPROVED",
        "G9A1 = AUTHORIZED — NOT STARTED",
        "G9 PRODUCTIVE SPATIAL IMPLEMENTATION = NOT STARTED")
    Assert-Contains -RelativePath $G9A1PromptPath -Values @(
        "AUTHORIZED CANONICAL PROMPT — NOT STARTED / UNEXECUTED",
        "this file does not execute")

    $generatorText = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile `
            -RelativePath "tools/knowledge/knowledge-bundle.psm1")
    $independentText = Get-Content -Raw -LiteralPath (
        Resolve-RequiredFile `
            -RelativePath "tools/knowledge/verify-knowledge-bundle.ps1")
    foreach ($marker in @(
            'ls-files", "--stage", "-z"',
            "THIRD_PARTY_OR_RESTRICTED", "NON_RELEASE_EVIDENCE",
            "NoCompression", "1980, 1, 1",
            'UTF8Encoding]::new($false, $true)')) {
        Assert-Condition -Condition $generatorText.Contains($marker) `
            -Message "Generator is missing deterministic contract marker: $marker"
    }
    Assert-Condition -Condition (-not $independentText.Contains(
            "Import-Module") -and
            $independentText.Contains("Bundle is stale relative to HEAD") -and
            $independentText.Contains("Archive metadata/compression is not deterministic")) `
        -Message "Artifact verifier is not independent/freshness-aware."

    $trackedGenerated = @(& git -C $RepositoryRoot ls-files `
        "artifacts/knowledge/**")
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $trackedGenerated.Count -eq 0) `
        -Message "Generated knowledge bundles must not be tracked."

    $scriptCount = Assert-PowerShellParses
    $scopeCount = Assert-Scope
    $integrityCount = if ($null -ne $AuthorityCommit) {
        Assert-GeoCeDGFrozenHashManifest -RepositoryRoot $RepositoryRoot `
            -ManifestPath $IntegrityManifestPath -Commit $AuthorityCommit
    } else {
        Assert-CurrentIntegrityManifest
    }
    Assert-Condition -Condition ($integrityCount -eq 29) `
        -Message "Unexpected G9O1 integrity entry count: $integrityCount"
    $markdownCount = Assert-MarkdownLinks -RelativePaths @(
        "README.md",
        "docs/adr/0015-deterministic-source-knowledge-bundle-ownership.md",
        "docs/architecture/geocedg_documentation_architecture.md",
        "docs/architecture/knowledge_bundle_architecture.md",
        "docs/developer/geocedg_agent_prompt_guide.md",
        "docs/developer/geocedg_developer_guide.md",
        "docs/developer/repository_map.md",
        "docs/roadmap/geocedg_roadmap.md",
        "docs/user/geocedg_user_guide.md",
        "docs/validation/g9_documentation_bundle_traceability.md",
        "docs/validation/g9o1_source_knowledge_bundles_guides_report.md",
        "geocedg/specs/operations/documentation-maintenance.md",
        "geocedg/specs/operations/knowledge-bundles.md",
        "geocedg/specs/README.md")
    $testExitCode = Invoke-FixtureTests

    if ($null -ne $AuthorityCommit) {
        & git -C $RepositoryRoot diff --check $G9PCommit $AuthorityCommit
        Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
            -Message "Frozen G9O1 diff whitespace check failed."
    }
    & git -C $RepositoryRoot diff --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --check failed for G9O1."
    & git -C $RepositoryRoot diff --cached --check
    Assert-Condition -Condition ($LASTEXITCODE -eq 0) `
        -Message "git diff --cached --check failed for G9O1."

    $finalStatus = (& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all) -join "`n"
    Assert-Condition -Condition ($LASTEXITCODE -eq 0 -and
            $finalStatus -eq $InitialStatus) `
        -Message "Repository status changed during G9O1 verification."

    $summary = @(
        "G9O1 focused verification passed.",
        "Prompt canonical LF SHA-256: $promptHash.",
        "G9A1 prompt canonical LF SHA-256: $g9a1PromptHash.",
        "Scoped changed paths: $scopeCount.",
        "G9O1 integrity entries: $integrityCount.",
        "PowerShell files parsed: $scriptCount.",
        "Current Markdown documents checked: $markdownCount.",
        "Fixture test exit code: $testExitCode.",
        "Generated tracked bundles: 0.",
        "G9O1: PASS — AUTHOR APPROVED.",
        "G9A1: AUTHORIZED — NOT STARTED.",
        "G9 productive spatial implementation: NOT STARTED.")
    [IO.File]::WriteAllLines($SummaryLog, $summary,
        [Text.UTF8Encoding]::new($false))
    $summary | ForEach-Object { Write-Host $_ }
    Write-Host "Test log: $TestLog"
    Write-Host "Summary log: $SummaryLog"
} catch {
    $message = "G9O1 focused verification failed: $($_.Exception.Message)"
    try {
        [IO.Directory]::CreateDirectory($LogRoot) | Out-Null
        [IO.File]::WriteAllText($SummaryLog, $message + "`n",
            [Text.UTF8Encoding]::new($false))
    } catch {
        # Preserve the original verification failure.
    }
    Write-Error $message
    exit 1
}

exit 0

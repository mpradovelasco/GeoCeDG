[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Resolve-Path -LiteralPath (
        Join-Path $PSScriptRoot "..\..\..")).Path
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Assert-TestCondition {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) {
        throw "TEST FAILURE: $Message"
    }
}

function Write-TestBytes {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes
    )
    $absolute = Join-Path $Root $Path.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($absolute)) |
        Out-Null
    [IO.File]::WriteAllBytes($absolute, $Bytes)
}

function Write-TestText {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text
    )
    Write-TestBytes -Root $Root -Path $Path `
        -Bytes ([Text.UTF8Encoding]::new($false).GetBytes($Text))
}

function Copy-TestFile {
    param(
        [Parameter(Mandatory)] [string]$Source,
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path
    )
    Write-TestBytes -Root $Root -Path $Path `
        -Bytes ([IO.File]::ReadAllBytes($Source))
}

function Invoke-TestGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments
    )
    $output = & git -C $Root @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed: $($output -join [Environment]::NewLine)"
    }
    return ($output -join "`n").Trim()
}

function Assert-ExpectedFailure {
    param(
        [Parameter(Mandatory)] [scriptblock]$Action,
        [Parameter(Mandatory)] [string]$Pattern,
        [Parameter(Mandatory)] [string]$Name
    )
    try {
        & $Action
    } catch {
        Assert-TestCondition -Condition (
            $_.Exception.Message -match $Pattern) `
            -Message "$Name failed for the wrong reason: $($_.Exception.Message)"
        return
    }
    throw "TEST FAILURE: $Name unexpectedly succeeded."
}

function Get-TestSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Invoke-IndependentVerifier {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$BundleDirectory,
        [switch]$ExpectFailure
    )
    $script = Join-Path $Root "tools\knowledge\verify-knowledge-bundle.ps1"
    $output = & pwsh -NoLogo -NoProfile -File $script `
        -RepositoryRoot $Root -BundleDirectory $BundleDirectory 2>&1
    $exitCode = $LASTEXITCODE
    if ($ExpectFailure) {
        Assert-TestCondition -Condition ($exitCode -ne 0) `
            -Message "Independent verifier accepted an invalid/stale bundle."
    } else {
        Assert-TestCondition -Condition ($exitCode -eq 0) `
            -Message ("Independent verifier failed: " +
                ($output -join [Environment]::NewLine))
    }
}

$sourceRoot = [IO.Path]::GetFullPath($RepositoryRoot)
$temporaryBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd(
    [IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
$testRoot = Join-Path $temporaryBase (
    "geocedg-g9o1-tests-" + [Guid]::NewGuid().ToString("N"))

try {
    [IO.Directory]::CreateDirectory($testRoot) | Out-Null
    [void](Invoke-TestGit -Root $testRoot -Arguments @("init", "-b", "fixture"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "config", "user.name", "GeoCeDG G9O1 Test"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "config", "user.email", "g9o1-test@invalid.local"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "config", "core.autocrlf", "false"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "remote", "add", "origin", "https://example.invalid/geocedg.git"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "remote", "add", "upstream", "https://example.invalid/upstream.git"))

    Write-TestText -Root $testRoot -Path ".gitignore" `
        -Text "artifacts/**`n"
    Write-TestText -Root $testRoot `
        -Path "source/shared/common/src/main/java/org/example/Upstream.java" `
        -Text "package org.example;`npublic class Upstream {}`n"
    Write-TestText -Root $testRoot `
        -Path "source/shared/common/src/main/java/org/example/Reference.java" `
        -Text "package org.example;`npublic class Reference {}`n"
    Write-TestText -Root $testRoot `
        -Path "source/shared/common/src/main/java/org/example/Unregistered.java" `
        -Text "package org.example;`npublic class Unregistered {}`n"
    Write-TestBytes -Root $testRoot `
        -Path "docs/references/cedg/restricted.pdf" `
        -Bytes ([byte[]](0x25, 0x50, 0x44, 0x46, 0x2d, 0x31, 0x2e, 0x34))
    Write-TestText -Root $testRoot -Path "module/build/generated.txt" `
        -Text "generated`n"
    Write-TestText -Root $testRoot -Path "docs/excluded-by-profile.md" `
        -Text "# Declaratively excluded`n"
    [void](Invoke-TestGit -Root $testRoot -Arguments @("add", "."))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "add", "-f", "module/build/generated.txt"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "commit", "-m", "fixture upstream baseline"))
    $baseline = Invoke-TestGit -Root $testRoot -Arguments @(
        "rev-parse", "HEAD")

    $bomCrLf = [Collections.Generic.List[byte]]::new()
    $bomCrLf.AddRange([byte[]](0xEF, 0xBB, 0xBF))
    $bomCrLf.AddRange([Text.UTF8Encoding]::new($false).GetBytes(
            "# Fixture authority`r`n`r`nNative text.`r`n"))
    Write-TestBytes -Root $testRoot -Path "AGENTS.md" `
        -Bytes $bomCrLf.ToArray()
    Write-TestText -Root $testRoot -Path "UPSTREAM.md" -Text @"
| Field | Value |
| --- | --- |
| GeoGebra version | `5.2-fixture` |
"@
    Write-TestText -Root $testRoot `
        -Path "docs/upstream/BASELINE_COMMIT.txt" -Text "$baseline`n"
    $inventory = [ordered]@{
        schema_version = 1
        baseline_sha = $baseline
        modifications = @(
            [ordered]@{
                path = "source/shared/common/src/main/java/org/example/Upstream.java"
                change = "modified"
                purpose = "Fixture upstream modification."
                authority = "geocedg/specs/operations/knowledge-bundles.md"
            }
        )
    }
    Write-TestText -Root $testRoot `
        -Path "docs/upstream/modified-files.yml" `
        -Text (($inventory | ConvertTo-Json -Depth 20).Replace(
                "`r`n", "`n") + "`n")
    Copy-TestFile -Source (Join-Path $sourceRoot `
            "geocedg\specs\operations\knowledge-bundle.schema.json") `
        -Root $testRoot `
        -Path "geocedg/specs/operations/knowledge-bundle.schema.json"
    Copy-TestFile -Source (Join-Path $sourceRoot `
            "tools\knowledge\tests\fixtures\knowledge-bundle-profiles.fixture.json") `
        -Root $testRoot `
        -Path "geocedg/specs/operations/knowledge-bundle-profiles.json"
    foreach ($name in @(
            "knowledge-bundle.psm1", "build-knowledge-bundle.ps1",
            "verify-knowledge-bundle.ps1")) {
        Copy-TestFile -Source (Join-Path $sourceRoot "tools\knowledge\$name") `
            -Root $testRoot -Path "tools/knowledge/$name"
    }
    Write-TestText -Root $testRoot `
        -Path "source/shared/common/src/main/java/org/example/Upstream.java" `
        -Text "package org.example;`r`npublic class Upstream {`r`n    int value = 9;`r`n}`r`n"
    [void](Invoke-TestGit -Root $testRoot -Arguments @("add", "."))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "commit", "-m", "fixture geocedg state"))
    Assert-TestCondition -Condition ([string]::IsNullOrWhiteSpace(
            (Invoke-TestGit -Root $testRoot -Arguments @(
                "status", "--short")))) `
        -Message "Fixture repository is not clean before generation."

    Import-Module (Join-Path $testRoot `
            "tools\knowledge\knowledge-bundle.psm1") -Force
    Assert-TestCondition -Condition (-not (
            Test-KnowledgeBundleRepositoryPath -Path "files/name:stream")) `
        -Message "Windows alternate-data-stream path was not rejected."
    Assert-TestCondition -Condition (-not (
            Test-KnowledgeBundleRepositoryPath -Path "files/line`nbreak")) `
        -Message "Control-character path was not rejected."
    Assert-TestCondition -Condition (-not (
            Test-KnowledgeBundleRepositoryPath -Path "files/./alias")) `
        -Message "Dot-segment path was not rejected."
    $first = New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
        -ProfileId fixture -OutputDirectory "artifacts/knowledge/run-a"
    $second = New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
        -ProfileId fixture -OutputDirectory "artifacts/knowledge/run-b"
    Assert-TestCondition -Condition ($first.BundleId -eq $second.BundleId) `
        -Message "Clean rerun bundle IDs differ."
    Assert-TestCondition -Condition ((Get-TestSha256 $first.ManifestPath) -eq
            (Get-TestSha256 $second.ManifestPath)) `
        -Message "Clean rerun manifests are not byte-identical."
    Assert-TestCondition -Condition ((Get-TestSha256 $first.ArchivePath) -eq
            (Get-TestSha256 $second.ArchivePath)) `
        -Message "Clean rerun archives are not byte-identical."
    Invoke-IndependentVerifier -Root $testRoot `
        -BundleDirectory $first.BundleDirectory

    $manifest = Get-Content -Raw -LiteralPath $first.ManifestPath |
        ConvertFrom-Json -Depth 100
    $paths = @($manifest.entries | ForEach-Object { $_.source_path })
    foreach ($requiredPath in @(
            "AGENTS.md",
            "source/shared/common/src/main/java/org/example/Upstream.java",
            "source/shared/common/src/main/java/org/example/Reference.java",
            "source/shared/common/src/main/java/org/example/Unregistered.java",
            "tools/knowledge/knowledge-bundle.psm1")) {
        Assert-TestCondition -Condition ($requiredPath -in $paths) `
            -Message "Profile omitted required path: $requiredPath"
    }
    foreach ($forbiddenPath in @(
            "docs/references/cedg/restricted.pdf",
            "docs/excluded-by-profile.md",
            "module/build/generated.txt")) {
        Assert-TestCondition -Condition ($forbiddenPath -notin $paths) `
            -Message "Restricted/generated path entered bundle: $forbiddenPath"
    }
    $orderChecks = @(
        "AGENTS.md",
        "geocedg/specs/operations/knowledge-bundle.schema.json",
        "tools/knowledge/knowledge-bundle.psm1",
        "source/shared/common/src/main/java/org/example/Upstream.java",
        "docs/upstream/modified-files.yml")
    $previousOrder = -1
    foreach ($orderedPath in $orderChecks) {
        $currentOrder = [Array]::IndexOf($paths, $orderedPath)
        Assert-TestCondition -Condition ($currentOrder -gt $previousOrder) `
            -Message "Declared reading order was not applied to $orderedPath."
        $previousOrder = $currentOrder
    }
    $native = $manifest.entries | Where-Object { $_.source_path -eq "AGENTS.md" }
    $modified = $manifest.entries | Where-Object {
        $_.source_path -eq
            "source/shared/common/src/main/java/org/example/Upstream.java"
    }
    $unchanged = $manifest.entries | Where-Object {
        $_.source_path -eq
            "source/shared/common/src/main/java/org/example/Reference.java"
    }
    Assert-TestCondition -Condition (
        $native.ownership_class -eq "GEOCEDG_NATIVE" -and
        $modified.ownership_class -eq "UPSTREAM_MODIFIED" -and
        $unchanged.ownership_class -eq "UPSTREAM_UNCHANGED_REFERENCE") `
        -Message "Ownership precedence/classification is incorrect."
    Assert-TestCondition -Condition ($native.raw_sha256 -ne
            $native.canonical_sha256) `
        -Message "BOM/CRLF fixture did not preserve distinct raw/canonical hashes."
    Assert-TestCondition -Condition ($modified.baseline_blob_sha -match
            '^[0-9a-f]{40}$' -and
            -not [string]::IsNullOrWhiteSpace($modified.unified_diff_path)) `
        -Message "Upstream-modified complete-file provenance is incomplete."

    Assert-ExpectedFailure -Name "file budget" -Pattern "file budget" -Action {
        New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
            -ProfileId fixture -OutputDirectory "artifacts/knowledge/budget-files" `
            -MaximumFiles 1 | Out-Null
    }
    Assert-ExpectedFailure -Name "negative budget override" -Pattern "negative" `
        -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/budget-negative" `
                -MaximumFiles -1 | Out-Null
        }
    Assert-ExpectedFailure -Name "byte budget" -Pattern "byte budget" -Action {
        New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
            -ProfileId fixture -OutputDirectory "artifacts/knowledge/budget-bytes" `
            -MaximumBytes 1 | Out-Null
    }
    Assert-ExpectedFailure -Name "token budget" -Pattern "token estimate budget" `
        -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/budget-tokens" `
                -MaximumTokens 1 | Out-Null
        }
    Assert-ExpectedFailure -Name "unsplittable chunk budget" `
        -Pattern "Chunk budget cannot be satisfied" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/budget-chunk" `
                -MaximumChunkTokens 1 | Out-Null
        }
    Assert-ExpectedFailure -Name "output traversal" `
        -Pattern "escapes the repository|below artifacts/knowledge" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture -OutputDirectory "../escape" | Out-Null
        }
    $profilePath = "geocedg/specs/operations/knowledge-bundle-profiles.json"
    $profileAbsolute = Join-Path $testRoot $profilePath.Replace(
        "/", [IO.Path]::DirectorySeparatorChar)
    $unsafeProfiles = Get-Content -Raw -LiteralPath $profileAbsolute |
        ConvertFrom-Json -Depth 100
    $unsafeProfiles.profiles[0].include = @("../escape") +
        @($unsafeProfiles.profiles[0].include)
    Write-TestText -Root $testRoot -Path $profilePath `
        -Text (($unsafeProfiles | ConvertTo-Json -Depth 100).Replace(
                "`r`n", "`n") + "`n")
    Assert-ExpectedFailure -Name "profile traversal" `
        -Pattern "Unsafe (bundle|profile) glob" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/profile-traversal" `
                -AllowDirty | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "restore", "--worktree", "--", $profilePath))

    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "remote", "set-url", "origin", "C:\Users\private\GeoCeDG"))
    Assert-ExpectedFailure -Name "local remote provenance" `
        -Pattern "remote URL is local" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/local-remote" | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "remote", "set-url", "origin",
        "https://user:secret@example.invalid/geocedg.git"))
    Assert-ExpectedFailure -Name "credential-bearing remote provenance" `
        -Pattern "remote URL is local" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/credential-remote" | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "remote", "set-url", "origin", "https://example.invalid/geocedg.git"))

    Write-TestText -Root $testRoot -Path "AGENTS.md" `
        -Text "# Staged fixture`n"
    [void](Invoke-TestGit -Root $testRoot -Arguments @("add", "AGENTS.md"))
    Write-TestText -Root $testRoot -Path "AGENTS.md" `
        -Text "# Staged and unstaged fixture`n"
    Write-TestText -Root $testRoot -Path "notes/untracked.txt" `
        -Text "untracked evidence`n"
    Assert-ExpectedFailure -Name "implicit dirty mode" -Pattern "clean tree" `
        -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/dirty-rejected" | Out-Null
        }
    $dirtyFirst = New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
        -ProfileId fixture -OutputDirectory "artifacts/knowledge/dirty-a" `
        -AllowDirty
    $dirtySecond = New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
        -ProfileId fixture -OutputDirectory "artifacts/knowledge/dirty-b" `
        -AllowDirty
    Assert-TestCondition -Condition ($dirtyFirst.BundleId -eq $first.BundleId -and
            [IO.Path]::GetFileName($dirtyFirst.BundleDirectory) -ne
                [IO.Path]::GetFileName($first.BundleDirectory)) `
        -Message "Dirty output did not preserve bundle identity without colliding with clean output."
    Assert-TestCondition -Condition ((Get-TestSha256 $dirtyFirst.ArchivePath) -eq
            (Get-TestSha256 $dirtySecond.ArchivePath)) `
        -Message "Explicit dirty rerun archives are not byte-identical."
    $dirtyManifest = Get-Content -Raw -LiteralPath $dirtyFirst.ManifestPath |
        ConvertFrom-Json -Depth 100
    Assert-TestCondition -Condition (
        $dirtyManifest.repository.dirty -and
        $dirtyManifest.repository.dirty_state.warning -eq
            "NON_RELEASE_EVIDENCE" -and
        $dirtyManifest.repository.dirty_state.staged_diff_sha256 -ne
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" -and
        $dirtyManifest.repository.dirty_state.unstaged_diff_sha256 -ne
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" -and
        @($dirtyManifest.repository.dirty_state.untracked).Count -eq 1 -and
        $dirtyManifest.repository.dirty_state.untracked[0].path -eq
            "notes/untracked.txt") `
        -Message "Explicit dirty evidence is incomplete."
    Invoke-IndependentVerifier -Root $testRoot `
        -BundleDirectory $dirtyFirst.BundleDirectory
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "restore", "--staged", "--worktree", "--", "AGENTS.md"))
    [IO.File]::Delete((Join-Path $testRoot "notes\untracked.txt"))

    Write-TestText -Root $testRoot `
        -Path "source/shared/common/src/main/java/org/example/Unregistered.java" `
        -Text "package org.example;`npublic class Unregistered { int bad = 1; }`n"
    Assert-ExpectedFailure -Name "unstaged inventory disagreement" `
        -Pattern "absent from inventory" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/inventory-unstaged-error" `
                -AllowDirty | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "add", "source/shared/common/src/main/java/org/example/Unregistered.java"))
    Assert-ExpectedFailure -Name "staged inventory disagreement" `
        -Pattern "absent from inventory" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/inventory-error" `
                -AllowDirty | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "restore", "--staged", "--worktree", "--",
        "source/shared/common/src/main/java/org/example/Unregistered.java"))

    Write-TestText -Root $testRoot -Path "artifacts/link-target.txt" `
        -Text "link-target`n"
    $linkBlob = Invoke-TestGit -Root $testRoot -Arguments @(
        "hash-object", "-w", "artifacts/link-target.txt")
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "update-index", "--add", "--cacheinfo", "120000", $linkBlob,
        "tools/knowledge/link.txt"))
    Assert-ExpectedFailure -Name "symbolic-link selection" `
        -Pattern "symbolic link" -Action {
            New-GeoCeDGKnowledgeBundle -RepositoryRoot $testRoot `
                -ProfileId fixture `
                -OutputDirectory "artifacts/knowledge/link-error" `
                -AllowDirty | Out-Null
        }
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "update-index", "--force-remove", "tools/knowledge/link.txt"))

    Write-TestText -Root $testRoot -Path "AGENTS.md" `
        -Text "# Freshness changed`n"
    [void](Invoke-TestGit -Root $testRoot -Arguments @("add", "AGENTS.md"))
    [void](Invoke-TestGit -Root $testRoot -Arguments @(
        "commit", "-m", "advance fixture head"))
    Invoke-IndependentVerifier -Root $testRoot `
        -BundleDirectory $first.BundleDirectory -ExpectFailure

    Write-Host "G9O1 knowledge-bundle tests passed."
    Write-Host "  Clean deterministic rerun: PASS"
    Write-Host "  Independent verification: PASS"
    Write-Host "  Membership/ownership/hashes/exclusions: PASS"
    Write-Host "  Dirty mode/traversal/symlink/budgets/freshness: PASS"
} finally {
    if (Test-Path -LiteralPath $testRoot) {
        $resolved = [IO.Path]::GetFullPath($testRoot)
        Assert-TestCondition -Condition (
            $resolved.StartsWith($temporaryBase,
                [StringComparison]::OrdinalIgnoreCase) -and
            [IO.Path]::GetFileName($resolved).StartsWith(
                "geocedg-g9o1-tests-", [StringComparison]::Ordinal)) `
            -Message "Refusing to remove an unsafe fixture path: $resolved"
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}

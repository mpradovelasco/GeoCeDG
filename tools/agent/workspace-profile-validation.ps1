#requires -Version 7.2
# Read-only validation of the single live workspace authority; never a second catalog.
function Assert-GeoCeDGLiveWorkspaceProfile {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    $profilePath = Join-Path $RepositoryRoot "apps/geocedg/application-profile.yml"
    $schemaPath = Join-Path $RepositoryRoot "geocedg/specs/ui/application-profile.schema.json"
    $text = [IO.File]::ReadAllText($profilePath, [Text.UTF8Encoding]::new($false, $true))
    if (-not (Test-Json -Json $text -SchemaFile $schemaPath -ErrorAction Stop)) {
        throw "Live GeoCeDG profile does not satisfy its schema-v2 authority."
    }
    $profile = $text | ConvertFrom-Json -Depth 100 -AsHashtable
    if ($profile.schema_version -ne 2 -or $profile.profile_id -cne "geocedg-desktop" -or
        $profile.application.name -cne "GeoCeDG" -or $profile.application.preferences_key -cne "geocedg" -or
        $profile.application.windows_app_id -cne "org.geocedg.desktop" -or
        $profile.live_profile.path -cne "apps/geocedg/application-profile.yml" -or
        $profile.live_profile.schema_version -ne 2 -or
        $profile.live_profile.policy -cne "single-live-runtime-authority") {
        throw "GeoCeDG live profile identity/single-authority contract differs."
    }
    if ($profile.serialization.app_code -cne "classic" -or
        $profile.serialization.native_extension -cne ".cedg" -or
        $profile.serialization.compatibility_extension -cne ".ggb" -or
        $profile.serialization.policy -cne "preserve-approved-r2-contract") {
        throw "Workspace migration must preserve R2 native/Classic serialization."
    }
    $actionIds = @($profile.actions.id)
    $clusterIds = @($profile.clusters.id)
    $familyIds = @($profile.taxonomy.broad_families.id)
    if ($actionIds.Count -ne 110 -or @($actionIds | Sort-Object -Unique -CaseSensitive).Count -ne 110 -or
        $clusterIds.Count -ne 18 -or @($clusterIds | Sort-Object -Unique -CaseSensitive).Count -ne 18 -or
        $familyIds.Count -ne 11 -or @($familyIds | Sort-Object -Unique -CaseSensitive).Count -ne 11) {
        throw "GeoCeDG workspace requires 11 unique families, 18 clusters and 110 actions."
    }
    $candidate = Get-Content -Raw (Join-Path $RepositoryRoot "geocedg/specs/ui/application-profile-v2.candidate.yml") |
        ConvertFrom-Json -Depth 100
    $approvedIds = @($candidate.actions.id | Sort-Object -CaseSensitive)
    $liveIds = @($actionIds | Sort-Object -CaseSensitive)
    if (@(Compare-Object $approvedIds $liveIds -CaseSensitive).Count -ne 0) {
        throw "Live action IDs differ from the reconciled approved planning catalog."
    }
    $featureIds = @($profile.features.id)
    if ($featureIds.Count -ne @($candidate.features).Count -or
        @($featureIds | Sort-Object -Unique -CaseSensitive).Count -ne $featureIds.Count) {
        throw "Live feature records are missing, extra or duplicated."
    }
    foreach ($approvedFeature in $candidate.features) {
        $liveFeature = @($profile.features | Where-Object { $_.id -ceq $approvedFeature.id })
        if ($liveFeature.Count -ne 1 -or
            $liveFeature[0].source -cne $approvedFeature.source -or
            $liveFeature[0].enabled_by_default -ne $approvedFeature.enabled_by_default) {
            throw "Live feature identity/source/default differs: $($approvedFeature.id)"
        }
    }
    if ($profile.product_policies.continuity.value -ne $false -or
        $profile.product_policies.continuity.locked -ne $true -or
        $profile.product_policies.materialization.kernel_recompute_creates_new_points -ne $false -or
        $profile.product_policies.materialization.existing_point_reactivation -cne "kernel-dag") {
        throw "GeoCeDG deterministic/materialization policy changed."
    }
    $languages = @($profile.product_policies.languages.offered | Sort-Object -CaseSensitive)
    if (($languages -join ",") -cne "en,es" -or $profile.product_policies.languages.fallback -cne "en") {
        throw "GeoCeDG product languages must be EN/ES with deterministic English fallback."
    }
    return $profile
}

function Assert-GeoCeDGProfileResourcePackaging {
    param([Parameter(Mandatory)] [string]$DesktopBuild)
    $resources = [regex]::Matches($DesktopBuild,
        '(?ms)^tasks\.processResources\s*\{(?<body>.*?)^\}')
    if ($resources.Count -ne 1) { throw "Expected one Desktop processResources declaration." }
    $expected = @(
        @{ source = '../../apps/geocedg'; files = @('application-profile.yml', 'application-profile-v1.yml') },
        @{ source = '../../geocedg/specs/ui'; files = @('application-profile.schema.json', 'application-profile-v1.schema.json') }
    )
    foreach ($entry in $expected) {
        $pattern = 'from\(rootProject\.file\("' + [regex]::Escape($entry.source) +
            '"\)\)\s*\{\s*include\((?<files>[^)]*)\)\s*into\("org/geocedg/desktop"\)\s*\}'
        $blocks = [regex]::Matches($resources[0].Groups['body'].Value, $pattern)
        if ($blocks.Count -ne 1) { throw "Missing or ambiguous packaged profile source: $($entry.source)" }
        $arguments = $blocks[0].Groups['files'].Value
        if ($arguments -cnotmatch '^\s*"[^"\r\n]+"\s*,\s*"[^"\r\n]+"\s*$') {
            throw "Profile packaging must use exactly the two declared resource names."
        }
        $files = @([regex]::Matches($arguments, '"(?<file>[^"]+)"') |
            ForEach-Object { $_.Groups['file'].Value } | Sort-Object -CaseSensitive)
        if (@(Compare-Object @($entry.files | Sort-Object -CaseSensitive) $files -CaseSensitive).Count -ne 0) {
            throw "Profile packaging resource selection differs: $($entry.source)"
        }
    }
}

function Assert-GeoCeDGTaskPromptContracts {
    param([Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$RequiredHeadings)
    # Only these two protected planning snapshots use their original grammar.
    # Every other prompt, including their active successor, retains all exact
    # operational headings. History is authenticated, not grandfathered by name.
    . (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
    $materialization = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    $prefix = '.github/prompts/tasks/'
    $successor = $prefix + 'g9u1-construction-workspace-after-g9s1-r1.prompt.md'
    $pins = @{
        'g9u1-construction-workspace-after-g9s1.prompt.md' = @{
            commit = '857de6628489bda0b65a5ba5145e62ca0795fc32'
            blob = 'ae6b2d38bf5849a99576242dcba885b02c284e71'
            lfHash = '2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322'
        }
        'g9u1-construction-workspace-after-g9u0-r6.prompt.md' = @{
            commit = '00982e7e148a634cd57ed928f322774df267d5e3'
            blob = 'd6528bb1432b637eb64406dfda4aba395e86bfb8'
            lfHash = '561546019efc1e1d5e4367ddde73e9a2b0a0d767343eb9348b46d9e9c06f12df'
        }
    }
    $prompts = @(Get-ChildItem -LiteralPath (Join-Path $RepositoryRoot $prefix) -Filter '*.prompt.md' -File)
    foreach ($prompt in $prompts) {
        $relative = $prefix + $prompt.Name
        $content = [IO.File]::ReadAllText($prompt.FullName, [Text.UTF8Encoding]::new($false, $true))
        if ($pins.ContainsKey($prompt.Name)) {
            $pin = $pins[$prompt.Name]
            [void](Assert-GeoCeDGMaterializationAttributes -RepositoryRoot $RepositoryRoot `
                -Paths @($relative, $successor) -ConfiguredFilterDrivers $materialization.configuredFilterDrivers)
            if ($prompt.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Historical prompt cannot be a reparse point: $relative"
            }
            [void](Resolve-GeoCeDGRepositoryIdentityCommit $RepositoryRoot $pin.commit)
            $tree = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
                $RepositoryRoot @('ls-tree', $pin.commit, '--', $relative))).TrimEnd("`r", "`n")
            if ($tree -cne "100644 blob $($pin.blob)`t$relative") {
                throw "Historical prompt checkpoint/path/mode/blob differs: $relative"
            }
            $currentBlob = (ConvertFrom-GeoCeDGRepositoryIdentityGit (Invoke-GeoCeDGRepositoryIdentityGit `
                $RepositoryRoot @('hash-object', "--path=$relative", '--', $relative))).TrimEnd("`r", "`n")
            $lfHash = Get-GeoCeDGRepositoryIdentityHash ($content.Replace("`r`n", "`n").Replace("`r", "`n"))
            if ($currentBlob -cne $pin.blob -or $lfHash -cne $pin.lfHash) {
                throw "Protected historical prompt content changed: $relative"
            }
            $successorFile = Join-Path $RepositoryRoot $successor
            if (-not (Test-Path -LiteralPath $successorFile -PathType Leaf)) {
                throw "Historical prompt requires its explicit active successor: $successor"
            }
            $content = [IO.File]::ReadAllText($successorFile, [Text.UTF8Encoding]::new($false, $true))
            if (-not $content.Contains('Supersedes prospectively only') -or
                -not $content.Contains($prompt.Name) -or -not $content.Contains($pin.commit)) {
                throw "Active successor must explicitly preserve/supersede the protected checkpoint: $relative"
            }
        }
        foreach ($heading in $RequiredHeadings) {
            if (-not [regex]::IsMatch($content, "(?m)^$([regex]::Escape($heading))\r?$")) {
                throw "$relative or its required active successor is missing heading '$heading'."
            }
        }
    }
}

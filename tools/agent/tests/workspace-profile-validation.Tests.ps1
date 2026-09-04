#requires -Version 7.2
[CmdletBinding()]
param([string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) ("g9u1-profile-tests-" + [guid]::NewGuid().ToString("N"))))
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "../../.."))
. (Join-Path $PSScriptRoot "../workspace-profile-validation.ps1")
$scratch = Join-Path ([IO.Path]::GetTempPath()) ("geocedg-u1-profile-" + [guid]::NewGuid().ToString("N"))
$paths = @("apps/geocedg/application-profile.yml", "geocedg/specs/ui/application-profile.schema.json",
    "geocedg/specs/ui/application-profile-v2.candidate.yml")
$rows = [Collections.Generic.List[object]]::new()
$profilePath = Join-Path $scratch $paths[0]
$initial = Get-Content -Raw (Join-Path $repository $paths[0])
$desktopBuild = Get-Content -Raw (Join-Path $repository 'source/desktop/desktop/build.gradle.kts')
function Run-ProfileCase {
    param([string]$Name, [scriptblock]$Mutation, [bool]$ShouldPass)
    $profile = $initial | ConvertFrom-Json -Depth 100 -AsHashtable
    if ($null -ne $Mutation) { & $Mutation $profile }
    [IO.File]::WriteAllText($profilePath, ($profile | ConvertTo-Json -Depth 100), [Text.UTF8Encoding]::new($false))
    $passed = $true
    try { [void](Assert-GeoCeDGLiveWorkspaceProfile -RepositoryRoot $scratch) } catch { $passed = $false }
    if ($passed -ne $ShouldPass) { throw "Unexpected workspace profile result: $Name" }
    $rows.Add([ordered]@{ name = $Name; expected = $(if ($ShouldPass) { "ACCEPT" } else { "REJECT" }); result = "PASS" })
}
function Run-PackagingCase {
    param([string]$Name, [string]$Build, [bool]$ShouldPass)
    $passed = $true
    try { Assert-GeoCeDGProfileResourcePackaging -DesktopBuild $Build } catch { $passed = $false }
    if ($passed -ne $ShouldPass) { throw "Unexpected workspace packaging result: $Name" }
    $rows.Add([ordered]@{ name = $Name; expected = $(if ($ShouldPass) { "ACCEPT" } else { "REJECT" }); result = "PASS" })
}
function Run-PromptCase {
    param([string]$Name, [scriptblock]$Mutation, [bool]$ShouldPass,
        [string]$Fixture = $promptFixture)
    foreach ($promptFileName in $promptNames) {
        $target = Join-Path $Fixture ('.github/prompts/tasks/' + $promptFileName)
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent $target))
        Copy-Item -LiteralPath (Join-Path $repository ('.github/prompts/tasks/' + $promptFileName)) -Destination $target
    }
    $unknown = Join-Path $Fixture '.github/prompts/tasks/unknown-historical.prompt.md'
    if (Test-Path -LiteralPath $unknown) { Remove-Item -LiteralPath $unknown -Force }
    if ($null -ne $Mutation) { & $Mutation $Fixture }
    $passed = $true
    try { Assert-GeoCeDGTaskPromptContracts -RepositoryRoot $Fixture -RequiredHeadings $promptHeadings }
    catch { $passed = $false }
    if ($passed -ne $ShouldPass) { throw "Unexpected protected prompt result: $Name" }
    $rows.Add([ordered]@{ name = $Name; expected = $(if ($ShouldPass) { "ACCEPT" } else { "REJECT" }); result = "PASS" })
}
try {
    foreach ($relative in $paths) {
        $target = Join-Path $scratch $relative
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent $target))
        Copy-Item -LiteralPath (Join-Path $repository $relative) -Destination $target
    }
    Run-ProfileCase "exact live v2 catalog" $null $true
    Run-ProfileCase "live v1 must not silently replace v2" { param($p) $p.schema_version = 1 } $false
    Run-ProfileCase "unknown schema version" { param($p) $p.schema_version = 3 } $false
    Run-ProfileCase "wrong product identity" { param($p) $p.profile_id = "other" } $false
    Run-ProfileCase "parallel runtime authority" { param($p) $p.live_profile.path = "parallel.yml" } $false
    Run-ProfileCase "changed serialization app" { param($p) $p.serialization.app_code = "cedg" } $false
    Run-ProfileCase "native extension changed" { param($p) $p.serialization.native_extension = ".ggb" } $false
    Run-ProfileCase "Continuity enabled" { param($p) $p.product_policies.continuity.value = $true } $false
    Run-ProfileCase "Continuity unlocked" { param($p) $p.product_policies.continuity.locked = $false } $false
    Run-ProfileCase "kernel creates new points" { param($p) $p.product_policies.materialization.kernel_recompute_creates_new_points = $true } $false
    Run-ProfileCase "duplicate action ID" { param($p) $p.actions[1].id = $p.actions[0].id } $false
    Run-ProfileCase "unapproved action ID" { param($p) $p.actions[0].id = "unapproved.action" } $false
    Run-ProfileCase "duplicate cluster" { param($p) $p.clusters[1].id = $p.clusters[0].id } $false
    Run-ProfileCase "duplicate family" { param($p) $p.taxonomy.broad_families[1].id = $p.taxonomy.broad_families[0].id } $false
    Run-ProfileCase "missing Spanish" { param($p) $p.product_policies.languages.offered = @("en") } $false
    Run-ProfileCase "wrong fallback" { param($p) $p.product_policies.languages.fallback = "es" } $false
    Run-ProfileCase "duplicate typed feature ID" { param($p) $p.features[1].id = $p.features[0].id } $false
    Run-ProfileCase "missing typed feature" { param($p) $p.features = @($p.features | Select-Object -Skip 1) } $false
    Run-ProfileCase "wrong typed feature source" { param($p) $p.features[0].source = 'geocedg/features/experimental.yml' } $false
    Run-ProfileCase "required profile disabled" { param($p) $p.features[0].enabled_by_default = $false } $false
    Run-ProfileCase "Laboratory silently enabled" { param($p) ($p.features | Where-Object id -CEQ 'cedg.laboratory.legacy').enabled_by_default = $true } $false
    Run-PackagingCase "exact v2 and historical v1 resource blocks" $desktopBuild $true
    Run-PackagingCase "missing live profile resource" ($desktopBuild.Replace('"application-profile.yml", ', '')) $false
    Run-PackagingCase "missing historical fallback resource" ($desktopBuild.Replace(', "application-profile-v1.yml"', '')) $false
    Run-PackagingCase "wrong profile source directory" ($desktopBuild.Replace('../../apps/geocedg', '../../apps/other')) $false
    Run-PackagingCase "wrong packaged resource destination" ($desktopBuild.Replace('into("org/geocedg/desktop")', 'into("other")')) $false
    $promptFixture = Join-Path $scratch 'prompt-git'
    & git clone --quiet --shared --no-checkout $repository $promptFixture
    if ($LASTEXITCODE -ne 0) { throw 'Cannot create isolated protected-prompt Git fixture.' }
    $promptNames = @('g9u1-construction-workspace-after-g9s1.prompt.md',
        'g9u1-construction-workspace-after-g9u0-r6.prompt.md',
        'g9u1-construction-workspace-after-g9s1-r1.prompt.md')
    $promptHeadings = @(Get-Content (Join-Path $repository '.github/prompts/tasks/task-template.prompt.md') |
        Where-Object { $_ -cmatch '^# ' })
    Run-PromptCase 'exact protected checkpoints and strict active successor' $null $true
    Run-PromptCase 'protected LF materialization' { param($r)
        foreach ($n in $promptNames) {
            $p = Join-Path $r ('.github/prompts/tasks/' + $n)
            [IO.File]::WriteAllText($p, [IO.File]::ReadAllText($p).Replace("`r`n", "`n"), [Text.UTF8Encoding]::new($false))
        }
    } $true
    Run-PromptCase 'protected CRLF materialization' { param($r)
        foreach ($n in $promptNames) {
            $p = Join-Path $r ('.github/prompts/tasks/' + $n)
            [IO.File]::WriteAllText($p, [IO.File]::ReadAllText($p).Replace("`r`n", "`n").Replace("`n", "`r`n"), [Text.UTF8Encoding]::new($false))
        }
    } $true
    Run-PromptCase 'pre-R6 historical content tampering' { param($r)
        [IO.File]::AppendAllText((Join-Path $r ('.github/prompts/tasks/' + $promptNames[0])), 'tamper')
    } $false
    Run-PromptCase 'post-R6 historical content tampering' { param($r)
        [IO.File]::AppendAllText((Join-Path $r ('.github/prompts/tasks/' + $promptNames[1])), 'tamper')
    } $false
    Run-PromptCase 'missing active successor' { param($r)
        Remove-Item -LiteralPath (Join-Path $r ('.github/prompts/tasks/' + $promptNames[2])) -Force
    } $false
    Run-PromptCase 'active successor missing required heading' { param($r)
        $p = Join-Path $r ('.github/prompts/tasks/' + $promptNames[2])
        [IO.File]::WriteAllText($p, [IO.File]::ReadAllText($p).Replace('# Objective', '## Objective'))
    } $false
    Run-PromptCase 'successor omits exact protected authority' { param($r)
        $p = Join-Path $r ('.github/prompts/tasks/' + $promptNames[2])
        [IO.File]::WriteAllText($p, [IO.File]::ReadAllText($p).Replace('00982e7e148a634cd57ed928f322774df267d5e3', 'latest'))
    } $false
    Run-PromptCase 'unknown historical name cannot bypass headings' { param($r)
        Copy-Item -LiteralPath (Join-Path $r ('.github/prompts/tasks/' + $promptNames[0])) `
            -Destination (Join-Path $r '.github/prompts/tasks/unknown-historical.prompt.md')
    } $false
    Run-PromptCase 'ordinary conforming prompt still accepted' { param($r)
        Copy-Item -LiteralPath (Join-Path $repository '.github/prompts/tasks/task-template.prompt.md') `
            -Destination (Join-Path $r '.github/prompts/tasks/unknown-historical.prompt.md')
    } $true
    $noHistory = Join-Path $scratch 'prompt-no-history'
    & git init --quiet $noHistory
    if ($LASTEXITCODE -ne 0) { throw 'Cannot create missing-history prompt fixture.' }
    Run-PromptCase 'missing immutable checkpoint fails closed' $null $false $noHistory
    if (@($rows.name | Sort-Object -Unique -CaseSensitive).Count -ne $rows.Count) {
        throw 'Infrastructure case names must be unique; fixture variables must not replace case identity.'
    }
    $json = ([ordered]@{ schemaVersion = 1; cases = @($rows); tests = $rows.Count; failures = 0 } |
        ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"
    [void][IO.Directory]::CreateDirectory([IO.Path]::GetFullPath($LogDirectory))
    $resultPath = Join-Path $LogDirectory "workspace-profile-tests.json"
    [IO.File]::WriteAllText($resultPath, $json, [Text.UTF8Encoding]::new($false))
    Write-Host "G9U1 profile validation: $($rows.Count)/$($rows.Count) PASS; $resultPath"
} finally {
    $resolved = [IO.Path]::GetFullPath($scratch)
    $tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd("\", "/") + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase) -or
        (Split-Path -Leaf $resolved) -notmatch '^geocedg-u1-profile-[0-9a-f]{32}$') {
        throw "Unsafe profile-fixture cleanup target."
    }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}

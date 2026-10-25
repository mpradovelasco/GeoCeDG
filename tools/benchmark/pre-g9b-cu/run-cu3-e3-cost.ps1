[CmdletBinding()]
param(
    [string]$OutputDirectory
)

# PRE-G9B-CU3 increment E3 bench runner for the exact decisions of the conflict rule.
# Runs org.geocedg.desktop.PreG9BCu3E3CostProbe (sources in cu3-java/) once through the Desktop
# test task with the unchanged CU0 harness.init.gradle, writes the bench evidence and run
# metadata to a fresh directory (default: ignored artifacts/benchmarks/pre-g9b-cu/), then
# recompiles the Desktop test source set without the probe and removes any probe class or report
# left in the build tree. The kernel enforces constructive-identity-certificate-budget/v1; the
# bench only measures its deterministic counters. It reads copies of the author model only,
# is not a verification profile and assigns no verdict.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
    $OutputDirectory = Join-Path $RepositoryRoot `
        "artifacts\benchmarks\pre-g9b-cu\$stamp-cu3-e3-cost"
}
$Output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $Output) {
    throw "Probe output directory must be new: $Output"
}
[void][IO.Directory]::CreateDirectory($Output)

function Get-GitValue {
    param([Parameter(Mandatory)] [string[]]$Arguments)
    $value = & git -C $RepositoryRoot @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed."
    }
    return $value
}

$commit = ([string](Get-GitValue @('rev-parse', 'HEAD'))).Trim()
$tree = ([string](Get-GitValue @('rev-parse', 'HEAD^{tree}'))).Trim()
$status = @(Get-GitValue @('status', '--porcelain=v1', '--untracked-files=all'))
$productStatus = @(Get-GitValue @('status', '--porcelain=v1', '--untracked-files=all',
        '--', 'source') | Where-Object { $_ -notmatch '/src/test/' })

$gradle = Join-Path $RepositoryRoot 'gradlew.bat'
$initScript = Join-Path $PSScriptRoot 'harness.init.gradle'
$sources = Join-Path $PSScriptRoot 'cu3-java'
$arguments = @(
    '-p', $RepositoryRoot,
    ':desktop:desktop:test',
    '--tests', 'org.geocedg.desktop.PreG9BCu3E3CostProbe',
    '--init-script', $initScript,
    "-Pcu0HarnessSources=$sources",
    '-Pcu0Scenario=cu3-e3-cost',
    "-Pcu0Output=$Output",
    "-Pcu0Commit=$commit",
    "-Pcu0Tree=$tree",
    "-Pcu0ProductDirty=$(($productStatus.Count -gt 0).ToString().ToLowerInvariant())",
    '--console=plain'
)

$started = [DateTime]::UtcNow
& $gradle @arguments *> (Join-Path $Output 'gradle-probe.log')
$probeExit = $LASTEXITCODE
$finished = [DateTime]::UtcNow

$reports = Join-Path $RepositoryRoot 'source\desktop\desktop\build\test-results\test'
$probeReport = Join-Path $reports 'TEST-org.geocedg.desktop.PreG9BCu3E3CostProbe.xml'
if (Test-Path -LiteralPath $probeReport) {
    Copy-Item -LiteralPath $probeReport -Destination $Output
}

& $gradle -p $RepositoryRoot ':desktop:desktop:compileTestJava' '--console=plain' *> `
    (Join-Path $Output 'gradle-cleanup.log')
$cleanupExit = $LASTEXITCODE
$buildRoot = Join-Path $RepositoryRoot 'source\desktop\desktop\build'
$leftovers = @(foreach ($folder in @('classes', 'tmp', 'test-results')) {
        Get-ChildItem -File -Recurse -LiteralPath (Join-Path $buildRoot $folder) `
            -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'PreG9BCu3E3CostProbe*' -or
                $_.Name -like 'PreG9BCu3E2CostProbe*' -or
                $_.Name -like 'PreG9BCu3E1CostProbe*' -or
                $_.Name -like 'PreG9BCu3M3*' -or
                $_.Name -like 'PreG9BCu3PreparationProbe*' -or
                $_.Name -like 'PreG9BCu3AuthorityStudyProbe*' -or
                $_.Name -like 'PreG9BCu3ConstructiveIdentityProbe*' -or
                $_.Name -like 'TEST-org.geocedg.desktop.PreG9BCu3E3CostProbe*' }
    })
# Only the probe classes are removed: PRE-G9B-CU3 product and test classes stay in the build.
foreach ($file in $leftovers) {
    [IO.File]::Delete($file.FullName)
}

$outputs = [Collections.Generic.List[object]]::new()
foreach ($file in @(Get-ChildItem -File -LiteralPath $Output | Sort-Object Name)) {
    $outputs.Add([ordered]@{
            name = $file.Name
            bytes = $file.Length
            sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName).Hash.ToLowerInvariant()
        })
}
$metadata = [ordered]@{
    schema_version = 1
    scenario = 'cu3-e3-cost'
    source_commit = $commit
    source_tree = $tree
    worktree_clean = ($status.Count -eq 0)
    worktree_status = $status
    product_sources_modified = ($productStatus.Count -gt 0)
    started_utc = $started.ToString('o')
    duration_s = [Math]::Round(($finished - $started).TotalSeconds, 1)
    probe_exit_code = $probeExit
    cleanup_exit_code = $cleanupExit
    removed_build_leftovers = @($leftovers | ForEach-Object { $_.Name })
    environment = [ordered]@{
        os = [Environment]::OSVersion.VersionString
        processors = [Environment]::ProcessorCount
        powershell = $PSVersionTable.PSVersion.ToString()
    }
    outputs = $outputs
}
[IO.File]::WriteAllText((Join-Path $Output 'run-metadata.json'),
    ($metadata | ConvertTo-Json -Depth 10) + "`n", [Text.UTF8Encoding]::new($false))
Write-Output "Probe output: $Output"
Write-Output "Probe exit code: $probeExit; cleanup exit code: $cleanupExit"

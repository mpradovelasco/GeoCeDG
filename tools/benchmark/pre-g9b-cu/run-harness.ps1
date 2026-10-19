[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('references', 'composition', 'windowed')]
    [string]$Scenario,
    [string]$OutputDirectory,
    [switch]$Dense,
    [switch]$ProductRuntime
)

# PRE-G9B-CU0 reproducible harness runner. Runs one scenario of
# org.geocedg.desktop.PreG9BCu0Harness (references, composition) or
# org.geocedg.desktop.PreG9BCu0WindowedHarness (windowed) through the Desktop test task with
# harness.init.gradle, writes the scenario output and run metadata to a fresh
# directory (default: ignored artifacts/benchmarks/pre-g9b-cu/), then recompiles the
# Desktop test source set without the harness so that no harness class or report
# remains in the build tree. It is not a verification profile and assigns no verdict.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
    $OutputDirectory = Join-Path $RepositoryRoot `
        "artifacts\benchmarks\pre-g9b-cu\$stamp-$Scenario"
}
$Output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $Output) {
    throw "Harness output directory must be new: $Output"
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
$sources = Join-Path $PSScriptRoot 'java'
$arguments = @(
    '-p', $RepositoryRoot,
    ':desktop:desktop:test',
    '--tests', 'org.geocedg.desktop.PreG9BCu0*Harness',
    '--init-script', $initScript,
    "-Pcu0HarnessSources=$sources",
    "-Pcu0Scenario=$Scenario",
    "-Pcu0Output=$Output",
    "-Pcu0Commit=$commit",
    "-Pcu0Tree=$tree",
    "-Pcu0ProductDirty=$(($productStatus.Count -gt 0).ToString().ToLowerInvariant())",
    "-Pcu0Dense=$($Dense.IsPresent.ToString().ToLowerInvariant())",
    "-Pcu0ProductRuntime=$($ProductRuntime.IsPresent.ToString().ToLowerInvariant())",
    '--console=plain'
)

$started = [DateTime]::UtcNow
& $gradle @arguments *> (Join-Path $Output 'gradle-harness.log')
$harnessExit = $LASTEXITCODE
$finished = [DateTime]::UtcNow

$reports = Join-Path $RepositoryRoot 'source\desktop\desktop\build\test-results\test'
foreach ($name in @('PreG9BCu0Harness', 'PreG9BCu0WindowedHarness')) {
    $harnessReport = Join-Path $reports "TEST-org.geocedg.desktop.$name.xml"
    if (Test-Path -LiteralPath $harnessReport) {
        Copy-Item -LiteralPath $harnessReport -Destination $Output
    }
}

& $gradle -p $RepositoryRoot ':desktop:desktop:compileTestJava' '--console=plain' *> `
    (Join-Path $Output 'gradle-cleanup.log')
$cleanupExit = $LASTEXITCODE
$buildRoot = Join-Path $RepositoryRoot 'source\desktop\desktop\build'
$leftovers = @(foreach ($folder in @('classes', 'tmp', 'test-results')) {
        Get-ChildItem -File -Recurse -LiteralPath (Join-Path $buildRoot $folder) `
            -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'PreG9BCu0Harness*' -or
                $_.Name -like 'PreG9BCu0Windowed*' -or
                $_.Name -like 'TEST-org.geocedg.desktop.PreG9BCu0*Harness*' }
    })
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
    scenario = $Scenario
    dense = $Dense.IsPresent
    product_runtime = $ProductRuntime.IsPresent
    source_commit = $commit
    source_tree = $tree
    worktree_clean = ($status.Count -eq 0)
    worktree_status = $status
    product_sources_modified = ($productStatus.Count -gt 0)
    started_utc = $started.ToString('o')
    duration_s = [Math]::Round(($finished - $started).TotalSeconds, 1)
    harness_exit_code = $harnessExit
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
Write-Output "Harness output: $Output"
Write-Output "Harness exit code: $harnessExit; cleanup exit code: $cleanupExit"
exit $harnessExit

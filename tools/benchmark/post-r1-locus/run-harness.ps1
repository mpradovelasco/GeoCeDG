[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('variant', 'benchmark')]
    [string]$Scenario,
    [string]$OutputDirectory,
    # Root of the checkout to run. Defaults to this repository; another checkout of an
    # earlier commit can be measured with the same harness sources.
    [string]$CheckoutRoot
)

# Post-R1 reproducible harness runner. Runs one scenario of
# org.geocedg.desktop.PostR1Harness through the Desktop test task with harness.init.gradle,
# writes the scenario output and run metadata to a fresh directory (default: ignored
# artifacts/benchmarks/post-r1-locus/), then recompiles the Desktop test source set without
# the harness so that no harness class or report remains in the build tree. It is not a
# verification profile and assigns no verdict. The model, the transport command list and the
# harness sources are always read from this repository.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
if ([string]::IsNullOrWhiteSpace($CheckoutRoot)) {
    $CheckoutRoot = $RepositoryRoot
}
$Checkout = (Resolve-Path -LiteralPath $CheckoutRoot).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
    $OutputDirectory = Join-Path $RepositoryRoot `
        "artifacts\benchmarks\post-r1-locus\$stamp-$Scenario"
}
$Output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $Output) {
    throw "Harness output directory must be new: $Output"
}
[void][IO.Directory]::CreateDirectory($Output)

function Get-GitValue {
    param([Parameter(Mandatory)] [string[]]$Arguments)
    $value = & git -C $Checkout @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed."
    }
    return $value
}

$commit = ([string](Get-GitValue @('rev-parse', 'HEAD'))).Trim()
$tree = ([string](Get-GitValue @('rev-parse', 'HEAD^{tree}'))).Trim()
$status = @(Get-GitValue @('status', '--porcelain=v1', '--untracked-files=all'))

$gradle = Join-Path $Checkout 'gradlew.bat'
$initScript = Join-Path $PSScriptRoot 'harness.init.gradle'
$sources = Join-Path $PSScriptRoot 'java'
$arguments = @(
    '-p', $Checkout,
    ':desktop:desktop:test',
    '--tests', 'org.geocedg.desktop.PostR1Harness',
    '--init-script', $initScript,
    "-PpostR1HarnessSources=$sources",
    "-PpostR1Scenario=$Scenario",
    "-PpostR1Output=$Output",
    '--console=plain'
)

$started = [DateTime]::UtcNow
# The harness reads the model package from the checkout it runs in.
$package = 'models\regression\post-r1-locus-on-intersection'
$copied = $false
if ($Checkout -ne $RepositoryRoot -and
        -not (Test-Path -LiteralPath (Join-Path $Checkout $package))) {
    Copy-Item -Recurse -LiteralPath (Join-Path $RepositoryRoot $package) `
        -Destination (Join-Path $Checkout $package)
    $copied = $true
}
& $gradle @arguments *> (Join-Path $Output 'gradle-harness.log')
$harnessExit = $LASTEXITCODE
$finished = [DateTime]::UtcNow

& $gradle -p $Checkout ':desktop:desktop:compileTestJava' '--console=plain' *> `
    (Join-Path $Output 'gradle-cleanup.log')
$cleanupExit = $LASTEXITCODE
$buildRoot = Join-Path $Checkout 'source\desktop\desktop\build'
$leftovers = @(foreach ($folder in @('classes', 'tmp', 'test-results')) {
        Get-ChildItem -File -Recurse -LiteralPath (Join-Path $buildRoot $folder) `
            -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'PostR1Harness*' -or
                $_.Name -like 'TEST-org.geocedg.desktop.PostR1Harness*' }
    })
foreach ($file in $leftovers) {
    [IO.File]::Delete($file.FullName)
}
if ($copied) {
    [IO.Directory]::Delete((Join-Path $Checkout $package), $true)
}
$workFolder = Join-Path $Output 'work'
if (Test-Path -LiteralPath $workFolder) {
    [IO.Directory]::Delete($workFolder, $true)
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
    checkout = $Checkout
    source_commit = $commit
    source_tree = $tree
    worktree_clean = ($status.Count -eq 0)
    worktree_status = $status
    started_utc = $started.ToString('o')
    duration_s = [Math]::Round(($finished - $started).TotalSeconds, 1)
    harness_exit_code = $harnessExit
    cleanup_exit_code = $cleanupExit
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

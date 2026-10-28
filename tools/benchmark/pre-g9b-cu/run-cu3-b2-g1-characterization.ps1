[CmdletBinding()]
param(
    [string]$OutputDirectory
)

# PRE-G9B-CU3 G-B2-1 characterization of CU3-B2 (author authorization of 2026-10-28:
# characterization only, no product change). It writes, outside the repository tree, a shadow copy
# of LocusIntersectionTokenLedger2D from the product source at HEAD with two print points (the
# commit of an evaluation and the allocation of a fresh incarnation for a current root, both behind
# a static switch that is off by default), and runs
# cu3-b2-g1/probe-java/.../PreG9BCu3B2CharacterizationProbe twice in the common-jre test JVM with
# both directories added to the test source set. It records the probe lines of each run, their
# SHA-256 and the run identity. The common-jre test classes are recompiled without the probe at
# the end. It is not a verification profile and assigns no verdict.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
[Environment]::CurrentDirectory = $RepositoryRoot
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
    $OutputDirectory = Join-Path $RepositoryRoot "artifacts\benchmarks\pre-g9b-cu\$stamp-cu3-b2-g1"
}
$Output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $Output) {
    throw "Output directory must be new: $Output"
}
[void][IO.Directory]::CreateDirectory($Output)
$utf8 = New-Object System.Text.UTF8Encoding($false)

$commit = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
$tree = (& git -C $RepositoryRoot rev-parse 'HEAD^{tree}').Trim()
$ledgerPath = 'source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java'
$ledgerBlob = (& git -C $RepositoryRoot rev-parse "HEAD:$ledgerPath").Trim()

# Shadow ledger, LF-normalized, with two print points behind PROBE.
$shadowRoot = Join-Path $Output 'shadow-java'
$shadowDir = Join-Path $shadowRoot 'org\geocedg\common\kernel\locus\intersection'
[void][IO.Directory]::CreateDirectory($shadowDir)
$script:text = [IO.File]::ReadAllText((Join-Path $RepositoryRoot $ledgerPath)).Replace("`r`n", "`n")
function Set-Once {
    param([string]$Old, [string]$New)
    $i = $script:text.IndexOf($Old, [StringComparison]::Ordinal)
    if ($i -lt 0 -or $script:text.IndexOf($Old, $i + 1, [StringComparison]::Ordinal) -ge 0) {
        throw "Shadow anchor not unique: $Old"
    }
    $script:text = $script:text.Substring(0, $i) + $New + $script:text.Substring($i + $Old.Length)
}
Set-Once "	private long nextIncarnation = 1;" "	public static boolean PROBE = false;`n	private long nextIncarnation = 1;"
Set-Once "				: nextIncarnation;`n		evaluation.finished = true;" ("				: nextIncarnation;`n		if (PROBE) { System.out.println(" +
    "`"PROBE-COMMIT fresh=`" + evaluation.usedFreshEpoch + `" next=`" + nextIncarnation + `" -> `" + " +
    "committedNextIncarnation + `" starting=`" + (evaluation.startingSnapshot == null ? `"null`" : " +
    "String.valueOf(evaluation.startingSnapshot.entries.size()))); }`n		evaluation.finished = true;")
Set-Once "				long incarnation = allocateIncarnation();`n				selected = allocatedEntry(branch,`n						currentRootAllocationKey(contract, selector, incarnation)," ("				if (PROBE) { System.out.println(`"PROBE-ALLOC selector=`" + " +
    "String.valueOf(selector).replaceAll(`"\\s+`", `" `") + `" startingNull=`" + (startingSnapshot == null)); }`n" +
    "				long incarnation = allocateIncarnation();`n				selected = allocatedEntry(branch,`n						currentRootAllocationKey(contract, selector, incarnation),")
[IO.File]::WriteAllText((Join-Path $shadowDir 'LocusIntersectionTokenLedger2D.java'), $script:text, $utf8)

$probeRoot = Join-Path $PSScriptRoot 'cu3-b2-g1\probe-java'
$init = Join-Path $Output 'probe.init.gradle'
$initText = @"
def target = java.nio.file.Paths.get('source', 'shared', 'common-jre')
allprojects { p ->
    if (p.projectDir.toPath().endsWith(target)) {
        p.afterEvaluate {
            p.sourceSets.test.java.srcDir('$($probeRoot.Replace('\', '/'))')
            p.sourceSets.test.java.srcDir('$($shadowRoot.Replace('\', '/'))')
        }
    }
}
"@
[IO.File]::WriteAllText($init, $initText, $utf8)

$gradle = Join-Path $RepositoryRoot 'gradlew.bat'
$results = Join-Path $RepositoryRoot 'source\shared\common-jre\build\test-results\test'
$runs = @()
foreach ($run in 'A', 'B') {
    $log = Join-Path $Output "gradle-$run.log"
    & $gradle -p $RepositoryRoot --init-script $init ':shared:common-jre:test' --rerun `
        --tests 'org.geocedg.common.kernel.locus.constructive.PreG9BCu3B2CharacterizationProbe' -q *> $log
    $exit = $LASTEXITCODE
    $xml = Get-ChildItem -LiteralPath $results -Filter 'TEST-*PreG9BCu3B2CharacterizationProbe.xml'
    $lines = @(Select-String -LiteralPath $xml.FullName -Pattern '(PROBE[|-][^<]*)' |
            ForEach-Object { $_.Matches[0].Groups[1].Value.Replace('&amp;', '&') })
    $file = Join-Path $Output "probe-$run.txt"
    [IO.File]::WriteAllText($file, ($lines -join "`n") + "`n", $utf8)
    # Token material and lineage objects carry per-run random identifiers; the normalized lines
    # replace them so that two runs can be compared.
    $normalized = Join-Path $Output "probe-$run.normalized.txt"
    $normal = @($lines | ForEach-Object {
            ($_ -replace 'locus-root/v3/[0-9a-f/]+', 'TOKEN' -replace '\b[0-9a-f]{8}\b', 'H' `
                -replace 'lineage=\[[^\]]*\]', 'lineage=[L]')
        })
    [IO.File]::WriteAllText($normalized, ($normal -join "`n") + "`n", $utf8)
    $counts = (Select-String -LiteralPath $xml.FullName -Pattern 'tests="\d+" skipped="\d+" failures="\d+" errors="\d+"').Matches[0].Value
    $runs += [ordered]@{ run = $run; exitCode = $exit; junit = $counts; lines = $lines.Count;
        sha256 = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()
        normalizedSha256 = (Get-FileHash -LiteralPath $normalized -Algorithm SHA256).Hash.ToLowerInvariant() }
}

& $gradle -p $RepositoryRoot ':shared:common-jre:compileTestJava' -q *> (Join-Path $Output 'gradle-clean.log')
$summary = [ordered]@{
    tool = 'run-cu3-b2-g1-characterization.ps1'
    commit = $commit
    tree = $tree
    ledgerBlob = $ledgerBlob
    probe = 'tools/benchmark/pre-g9b-cu/cu3-b2-g1/probe-java/org/geocedg/common/kernel/locus/constructive/PreG9BCu3B2CharacterizationProbe.java'
    probeSha256 = (Get-FileHash -LiteralPath (Join-Path $probeRoot 'org\geocedg\common\kernel\locus\constructive\PreG9BCu3B2CharacterizationProbe.java') -Algorithm SHA256).Hash.ToLowerInvariant()
    runs = $runs
    deterministicModuloIdentifiers = ($runs[0].normalizedSha256 -eq $runs[1].normalizedSha256)
}
$summaryPath = Join-Path $Output 'summary.json'
[IO.File]::WriteAllText($summaryPath, ($summary | ConvertTo-Json -Depth 5), $utf8)
Get-Content -LiteralPath $summaryPath

[CmdletBinding()]
param(
    [string]$OutputDirectory,
    [switch]$SkipPerformance,
    [switch]$Candidate
)

# PRE-G9B-CU3 characterization of T7-a (author authorization of 2026-10-26: characterization
# only, no implementation). From the product source of RootCountCertificate2D at HEAD it
# generates, outside the repository tree, a shadow copy that routes every certify call through
# cu3-t7a/shadow-java/.../PreG9BCu3T7aLedger.java, and compiles it against the product jar. The
# shadow classes go first on the class path of the test JVMs only (cu3-t7a/t7a.init.gradle):
#   - mode compare: the product certificate is returned and the hypothetical T7-a certificate is
#     computed on the same source and box; both are recorded in a ledger;
#   - mode t7a: the T7-a certificate is returned, to observe published results and times;
#   - mode off: the product procedure, through the shadow, as a control of the shadow itself.
# It runs the PHASE test selections of CU3 and CU2 (shared and Desktop, from the JUnit inventory)
# and cu3-t7a/probe-java/PreG9BCu3T7aCorpusProbe (author model, dynamic configurations) in the
# modes compare and t7a, and, unless -SkipPerformance, the performance probe of
# run-cu3-performance-characterization.ps1 twice in the modes off and t7a. Nothing in the
# product changes; the Desktop test source set is recompiled without the probes at the end. It is
# not a verification profile and assigns no verdict; timings are informative.
#
# With -Candidate it compares the implementation of T7-a (author decision of 2026-10-26,
# cu3-certified-admission/v2) with the procedure of version 1 in the same product: the shadow
# keeps the early exact decision of the candidate behind the ledger's switch, so that the
# procedure the ledger calls current is version 1 (the procedure of the T6 candidate) and the one
# it calls t7a is the candidate; the mode off is then version 1.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..')).Path
[Environment]::CurrentDirectory = $RepositoryRoot
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ')
    $OutputDirectory = Join-Path $RepositoryRoot "artifacts\benchmarks\pre-g9b-cu\$stamp-cu3-t7a-characterization"
}
$Output = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $Output) {
    throw "Output directory must be new: $Output"
}
[void][IO.Directory]::CreateDirectory($Output)
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Get-GitValue {
    param([Parameter(Mandatory)] [string[]]$Arguments)
    $value = & git -C $RepositoryRoot @Arguments
    if ($LASTEXITCODE -ne 0) { throw "git $($Arguments -join ' ') failed." }
    return $value
}

$commit = ([string](Get-GitValue @('rev-parse', 'HEAD'))).Trim()
$tree = ([string](Get-GitValue @('rev-parse', 'HEAD^{tree}'))).Trim()
$status = @(Get-GitValue @('status', '--porcelain=v1', '--untracked-files=all'))
$productStatus = @(Get-GitValue @('status', '--porcelain=v1', '--untracked-files=all', '--', 'source') |
        Where-Object { $_ -notmatch '/src/test/' })
$productPath = 'source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RootCountCertificate2D.java'
$productBlob = ([string](Get-GitValue @('rev-parse', "HEAD:$productPath"))).Trim()
$productFile = Join-Path $RepositoryRoot $productPath
$workingBlob = ([string](& git -C $RepositoryRoot hash-object $productFile)).Trim()
if ($workingBlob -ne $productBlob) { throw 'RootCountCertificate2D differs from HEAD' }

$gradle = Join-Path $RepositoryRoot 'gradlew.bat'
$tool = Join-Path $PSScriptRoot 'cu3-t7a'
$t7aInit = Join-Path $tool 't7a.init.gradle'
$harnessInit = Join-Path $PSScriptRoot 'harness.init.gradle'
$logs = Join-Path $Output 'logs'
[void][IO.Directory]::CreateDirectory($logs)

# --- 1. Product jar and shadow classes ---------------------------------------------------------
& $gradle -p $RepositoryRoot ':shared:common:jar' '--console=plain' *> (Join-Path $logs 'jar.log')
if ($LASTEXITCODE -ne 0) { throw 'The product jar did not build.' }
$jar = Get-ChildItem -File -LiteralPath (Join-Path $RepositoryRoot 'source\shared\common\build\libs') -Filter '*.jar' |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $jar) { throw 'No product jar.' }

$source = $utf8.GetString([IO.File]::ReadAllBytes($productFile)).Replace("`r`n", "`n")
function Replace-Exactly([string]$text, [string]$anchor, [string]$replacement) {
    $count = ([regex]::Matches($text, [regex]::Escape($anchor))).Count
    if ($count -ne 1) { throw "Shadow anchor found $count times: $anchor" }
    return $text.Replace($anchor, $replacement)
}
$shadow = Replace-Exactly $source (@(
        "`tstatic RootCountCertificate2D certify(ResidualJetSource2D source, double lower,",
        "`t`t`tdouble upper) {",
        "`t`tSearch search = new Search(source);") -join "`n") (@(
        "`tstatic RootCountCertificate2D certify(ResidualJetSource2D source, double lower,",
        "`t`t`tdouble upper) {",
        "`t`treturn PreG9BCu3T7aLedger.certify(source, lower, upper);",
        "`t}",
        "",
        "`tstatic RootCountCertificate2D certifyWith(ResidualJetSource2D source, double lower,",
        "`t`t`tdouble upper) {",
        "`t`tSearch search = new Search(source);") -join "`n")
if ($Candidate) {
    $shadow = Replace-Exactly $shadow (@(
            "`t`t`tif (source.isConstructive()) {",
            "`t`t`t`t// cu3-certified-admission/v2: the first enclosure of the sign search, then the",
            "`t`t`t`t// exact decision before any subdivision; otherwise the search goes on from it.",
            "`t`t`t`tenclosed = enclose(low, high);",
            "`t`t`t`tif (enclosed == null) {",
            "`t`t`t`t`treturn invalid(a, b);",
            "`t`t`t`t}",
            "`t`t`t`tif (enclosed.sign(0) == ResidualJetEnclosure2D.UNDECIDED) {",
            "`t`t`t`t`tRootCountCertificate2D early = exactDouble(a, b, curvature, bracket);",
            "`t`t`t`t`tif (early != null && early.form == Form.ONE_DOUBLE) {",
            "`t`t`t`t`t`treturn early;",
            "`t`t`t`t`t}",
            "`t`t`t`t}",
            "`t`t`t}",
            "`t`t`tint value = fixedSign(low, high, 0, enclosed);") -join "`n") (@(
            "`t`t`tif (source.isConstructive() && PreG9BCu3T7aLedger.anticipate()) {",
            "`t`t`t`tenclosed = enclose(low, high);",
            "`t`t`t`tif (enclosed == null) {",
            "`t`t`t`t`treturn invalid(a, b);",
            "`t`t`t`t}",
            "`t`t`t`tif (enclosed.sign(0) == ResidualJetEnclosure2D.UNDECIDED) {",
            "`t`t`t`t`tlong anticipationBegin = System.nanoTime();",
            "`t`t`t`t`tRootCountCertificate2D early = exactDouble(a, b, curvature, bracket);",
            "`t`t`t`t`tPreG9BCu3T7aLedger.anticipation(bracket, early, System.nanoTime() - anticipationBegin);",
            "`t`t`t`t`tif (early != null && early.form == Form.ONE_DOUBLE) {",
            "`t`t`t`t`t`treturn early;",
            "`t`t`t`t`t}",
            "`t`t`t`t}",
            "`t`t`t}",
            "`t`t`tint before = evaluations;",
            "`t`t`tint value;",
            "`t`t`ttry {",
            "`t`t`t`tvalue = fixedSign(low, high, 0, enclosed);",
            "`t`t`t} catch (RuntimeException exhausted) {",
            "`t`t`t`tPreG9BCu3T7aLedger.critical(bracket, `"EXHAUSTED`", evaluations - before);",
            "`t`t`t`tthrow exhausted;",
            "`t`t`t}",
            "`t`t`tPreG9BCu3T7aLedger.critical(bracket, value == INVALID ? `"INVALID`" : value == 0 ? `"ZERO`"",
            "`t`t`t`t`t: value > 0 ? `"PLUS`" : `"MINUS`", evaluations - before);") -join "`n")
} else {
$shadow = Replace-Exactly $shadow (@(
        "`t`t`tdouble[] bracket = {low, high};",
        "`t`t`tint value = fixedSign(low, high, 0);") -join "`n") (@(
        "`t`t`tdouble[] bracket = {low, high};",
        "`t`t`tif (PreG9BCu3T7aLedger.anticipate()) {",
        "`t`t`t`tlong anticipationBegin = System.nanoTime();",
        "`t`t`t`tRootCountCertificate2D early = exactDouble(a, b, curvature, bracket);",
        "`t`t`t`tPreG9BCu3T7aLedger.anticipation(bracket, early, System.nanoTime() - anticipationBegin);",
        "`t`t`t`tif (early != null) {",
        "`t`t`t`t`treturn early;",
        "`t`t`t`t}",
        "`t`t`t}",
        "`t`t`tint before = evaluations;",
        "`t`t`tint value;",
        "`t`t`ttry {",
        "`t`t`t`tvalue = fixedSign(low, high, 0);",
        "`t`t`t} catch (RuntimeException exhausted) {",
        "`t`t`t`tPreG9BCu3T7aLedger.critical(bracket, `"EXHAUSTED`", evaluations - before);",
        "`t`t`t`tthrow exhausted;",
        "`t`t`t}",
        "`t`t`tPreG9BCu3T7aLedger.critical(bracket, value == INVALID ? `"INVALID`" : value == 0 ? `"ZERO`"",
        "`t`t`t`t`t: value > 0 ? `"PLUS`" : `"MINUS`", evaluations - before);") -join "`n")
}
$shadow = Replace-Exactly $shadow "`t`t`t`tRootCountCertificate2D exactDouble = exactDouble(a, b, curvature, bracket);" (@(
        "`t`t`t`tlong exactBegin = System.nanoTime();",
        "`t`t`t`tRootCountCertificate2D exactDouble = exactDouble(a, b, curvature, bracket);",
        "`t`t`t`tPreG9BCu3T7aLedger.exactConsulted(System.nanoTime() - exactBegin);") -join "`n")
$shadowSource = Join-Path $Output 'shadow-src\org\geocedg\common\kernel\locus\intersection'
[void][IO.Directory]::CreateDirectory($shadowSource)
[IO.File]::WriteAllBytes((Join-Path $shadowSource 'RootCountCertificate2D.java'), $utf8.GetBytes($shadow))
$ledgerSource = Join-Path $tool 'shadow-java\org\geocedg\common\kernel\locus\intersection\PreG9BCu3T7aLedger.java'
Copy-Item -LiteralPath $ledgerSource -Destination $shadowSource
$shadowClasses = Join-Path $Output 'shadow-classes'
[void][IO.Directory]::CreateDirectory($shadowClasses)
$javac = if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else { (Get-Command javac -CommandType Application | Select-Object -First 1).Source }
& $javac --release 17 -encoding UTF-8 -nowarn -cp $jar.FullName -d $shadowClasses `
    (Join-Path $shadowSource 'RootCountCertificate2D.java') (Join-Path $shadowSource 'PreG9BCu3T7aLedger.java') *> (Join-Path $logs 'javac.log')
if ($LASTEXITCODE -ne 0) { throw 'The shadow did not compile.' }

# --- 2. Runs -------------------------------------------------------------------------------------
$inventory = Get-Content -Raw (Join-Path $RepositoryRoot 'geocedg\specs\operations\verification-junit-inventory.json') | ConvertFrom-Json
function Get-Filters([string]$selection) {
    $entry = @($inventory.selections | Where-Object selection_id -CEQ $selection)
    if ($entry.Count -ne 1) { throw "Unknown selection $selection" }
    return @($entry[0].test_filters)
}
$runs = [Collections.Generic.List[object]]::new()
function Invoke-Gradle([string]$name, [string[]]$arguments, [string]$mode) {
    $ledger = Join-Path $Output "ledger\$name"
    $all = @('-p', $RepositoryRoot) + $arguments + @('--init-script', $t7aInit,
        "-Pt7aShadow=$shadowClasses", "-Pt7aMode=$mode", "-Pt7aLedger=$ledger",
        "-Dt7aShadow=$shadowClasses", "-Dt7aMode=$mode", "-Dt7aLedger=$ledger", '--console=plain',
        '-Dorg.gradle.java.installations.auto-download=false')
    $started = [DateTime]::UtcNow
    & $gradle @all *> (Join-Path $logs "$name.log")
    $code = $LASTEXITCODE
    $runs.Add([ordered]@{ name = $name; mode = $mode; exit_code = $code
            duration_s = [Math]::Round(([DateTime]::UtcNow - $started).TotalSeconds, 1) })
    return $code
}
function Copy-Results([string]$name, [string]$module) {
    $from = Join-Path $RepositoryRoot "source\$module\build\test-results\test"
    $to = Join-Path $Output "results\$name"
    [void][IO.Directory]::CreateDirectory($to)
    if (Test-Path $from) {
        Get-ChildItem -File -LiteralPath $from -Filter 'TEST-*.xml' | Copy-Item -Destination $to
    }
}
$sharedFilters = @(Get-Filters 'pre-g9b-cu3.shared') + @(Get-Filters 'pre-g9b-cu2.shared')
$desktopFilters = @(Get-Filters 'pre-g9b-cu3.desktop') + @(Get-Filters 'pre-g9b-cu2.desktop')
$harnessCommon = @('--init-script', $harnessInit, "-Pcu0Commit=$commit", "-Pcu0Tree=$tree",
    "-Pcu0ProductDirty=$(($productStatus.Count -gt 0).ToString().ToLowerInvariant())")
foreach ($mode in 'compare', 't7a') {
    $arguments = @(':shared:common-jre:test', '--rerun')
    foreach ($filter in $sharedFilters) { $arguments += @('--tests', $filter) }
    [void](Invoke-Gradle "shared-$mode" $arguments $mode)
    Copy-Results "shared-$mode" 'shared\common-jre'
    $arguments = @(':desktop:desktop:test', '--rerun')
    foreach ($filter in $desktopFilters) { $arguments += @('--tests', $filter) }
    [void](Invoke-Gradle "desktop-$mode" $arguments $mode)
    Copy-Results "desktop-$mode" 'desktop\desktop'
    $corpus = Join-Path $Output 'corpus'
    $arguments = @(':desktop:desktop:test', '--rerun', '--tests', 'org.geocedg.desktop.PreG9BCu3T7aCorpusProbe') +
        $harnessCommon + @("-Pcu0HarnessSources=$(Join-Path $tool 'probe-java')", '-Pcu0Scenario=cu3-t7a-corpus', "-Pcu0Output=$corpus")
    [void](Invoke-Gradle "corpus-$mode" $arguments $mode)
}
if (-not $SkipPerformance) {
    foreach ($mode in 'off', 't7a') {
        foreach ($run in 'A', 'B') {
            $performance = Join-Path $Output "performance-$mode-run$run"
            $arguments = @(':desktop:desktop:test', '--rerun', '--tests', 'org.geocedg.desktop.PreG9BCu3PerformanceCharacterizationProbe') +
                $harnessCommon + @("-Pcu0HarnessSources=$(Join-Path $PSScriptRoot 'cu3-java')", '-Pcu0Scenario=cu3-performance-characterization', "-Pcu0Output=$performance")
            [void](Invoke-Gradle "performance-$mode-run$run" $arguments $mode)
        }
    }
}

# --- 3. Cleanup of the Desktop test build --------------------------------------------------------
& $gradle -p $RepositoryRoot ':desktop:desktop:compileTestJava' '--console=plain' *> (Join-Path $logs 'cleanup.log')
$cleanupExit = $LASTEXITCODE
$buildRoot = Join-Path $RepositoryRoot 'source\desktop\desktop\build'
$leftovers = @(foreach ($folder in @('classes', 'tmp', 'test-results')) {
        Get-ChildItem -File -Recurse -LiteralPath (Join-Path $buildRoot $folder) -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like 'PreG9BCu3T7aCorpusProbe*' -or
                $_.Name -like 'PreG9BCu3PerformanceCharacterizationProbe*' -or
                $_.Name -like 'TEST-org.geocedg.desktop.PreG9BCu3T7aCorpusProbe*' -or
                $_.Name -like 'TEST-org.geocedg.desktop.PreG9BCu3PerformanceCharacterizationProbe*' }
    })
foreach ($file in $leftovers) { [IO.File]::Delete($file.FullName) }

# --- 4. Summary ----------------------------------------------------------------------------------
function Read-Ledger([string]$name) {
    $files = @(Get-ChildItem -File -LiteralPath (Join-Path $Output 'ledger') -Filter "$name-*.jsonl" -ErrorAction SilentlyContinue)
    $records = [Collections.Generic.List[object]]::new()
    foreach ($file in $files) {
        foreach ($line in [IO.File]::ReadAllLines($file.FullName)) {
            if ($line.Trim()) { $records.Add(($line | ConvertFrom-Json)) }
        }
    }
    return , $records
}
function Total($items, [scriptblock]$value) {
    $sum = 0.0
    foreach ($item in @($items)) { $sum += [double](& $value $item) }
    return [Math]::Round($sum, 4)
}
function Histogram($values) {
    $map = [ordered]@{}
    foreach ($group in @($values | Group-Object | Sort-Object Name)) { $map[[string]$group.Name] = $group.Count }
    return $map
}
function Summarize-Ledger([string]$name) {
    $records = Read-Ledger $name
    $row = [ordered]@{ certify_calls = $records.Count }
    if ($records.Count -eq 0) { return $row }
    $row.by_context = Histogram ($records | ForEach-Object { $_.context })
    $row.constructive_calls = @($records | Where-Object { $_.constructive }).Count
    $compare = @($records | Where-Object { $null -ne $_.current })
    $t7aPaths = @($records | ForEach-Object { $_.t7a })
    if ($compare.Count -gt 0) {
        $row.current_forms = Histogram ($compare | ForEach-Object { $_.current.form })
        $row.current_critical_value = Histogram ($compare | ForEach-Object { $_.current.critical_value })
        $row.current_work_limit = @($compare | Where-Object { $_.current.reason -like '*work limit*' }).Count
        $row.same_decision = @($compare | Where-Object { $_.same_decision }).Count
        $row.different_decision = @($compare | Where-Object { -not $_.same_decision }).Count
        $row.differences = @($compare | Where-Object { -not $_.same_decision } | Select-Object -First 25 | ForEach-Object {
                [ordered]@{ context = $_.context; box = $_.box; current = $_.current.signature
                    current_critical_value = $_.current.critical_value; t7a = $_.t7a.signature }
            })
        $row.evaluations_current = (Total $compare { param($r) $r.current.evaluations })
        $row.evaluations_t7a = (Total $compare { param($r) $r.t7a.evaluations })
        $row.critical_value_evaluations_current = (Total $compare { param($r) $r.current.critical_value_evaluations })
        $newExact = @($compare | Where-Object { $_.t7a.anticipation -ne 'NOT_CONSULTED' -and $_.current.exact_consultations_after_bisection -eq 0 })
        $row.t7a_consultations_where_the_product_consults_none = $newExact.Count
        $row.t7a_consultations_where_the_product_consults_none_by_current_critical_value = Histogram ($newExact | ForEach-Object { $_.current.critical_value })
        $row.t7a_consultation_ms_where_the_product_consults_none = (Total $newExact { param($r) $r.t7a.anticipation_ms })
        $row.current_ms = (Total $compare { param($r) $r.current.ms })
        $row.t7a_ms_after_the_product = (Total $compare { param($r) $r.t7a.ms })
    }
    $row.t7a_anticipation = Histogram ($t7aPaths | ForEach-Object { $_.anticipation })
    $row.t7a_forms = Histogram ($t7aPaths | ForEach-Object { $_.form })
    $row.t7a_critical_value = Histogram ($t7aPaths | ForEach-Object { $_.critical_value })
    $row.t7a_work_limit = @($t7aPaths | Where-Object { $_.reason -like '*work limit*' }).Count
    return $row
}
function Summarize-Results([string]$name) {
    $folder = Join-Path $Output "results\$name"
    $row = [ordered]@{ classes = 0; tests = 0; failures = 0; errors = 0; skipped = 0; failing = @() }
    foreach ($file in @(Get-ChildItem -File -LiteralPath $folder -Filter 'TEST-*.xml' -ErrorAction SilentlyContinue)) {
        $suite = ([xml][IO.File]::ReadAllText($file.FullName)).testsuite
        $row.classes++
        $row.tests += [int]$suite.tests
        $row.failures += [int]$suite.failures
        $row.errors += [int]$suite.errors
        $row.skipped += [int]$suite.skipped
        foreach ($case in @($suite.testcase)) {
            if ($case.SelectSingleNode('failure') -or $case.SelectSingleNode('error')) { $row.failing += "$($suite.name).$($case.name)" }
        }
    }
    return $row
}
function Compare-Corpus {
    # Raw, then without the durable identities that each load of the model copy creates (geo:<hash>
    # and the root-token parts derived from them), then also without the enclosure counts in the
    # certificate descriptions and the work counters, which T7-a changes by design.
    $folder = Join-Path $Output 'corpus'
    $a = Join-Path $folder 'cu3-t7a-corpus-compare.json'
    $b = Join-Path $folder 'cu3-t7a-corpus-t7a.json'
    if (-not (Test-Path $a) -or -not (Test-Path $b)) { return [ordered]@{ available = $false } }
    $left = (Get-Content -Raw $a | ConvertFrom-Json).configurations
    $right = (Get-Content -Raw $b | ConvertFrom-Json).configurations
    $identity = {
        param([string]$text)
        $text = [regex]::Replace($text, 'geo:[0-9a-f]{16,}', 'geo:#')
        $text = [regex]::Replace($text, 'locus-root-local/v1/[0-9a-f]+/', 'locus-root-local/v1/#/')
        [regex]::Replace($text, '\b[0-9a-f]{64}\b', '#64')
    }
    $render = {
        param($value, [bool]$withoutWork)
        $copy = $value | ConvertTo-Json -Depth 30 | ConvertFrom-Json
        if ($withoutWork -and $copy -and $copy.PSObject.Properties.Name -contains 'work') { $copy.work = '' }
        $copy | ConvertTo-Json -Depth 30 -Compress
    }
    $row = [ordered]@{ available = $true; objects = 0; raw_equal = 0; equal_modulo_identities = 0
        equal_modulo_identities_enclosure_counts_and_work = 0; different = 0; differences = @() }
    foreach ($configuration in $left.PSObject.Properties) {
        $other = $right.($configuration.Name)
        foreach ($object in $configuration.Value.PSObject.Properties) {
            $row.objects++
            $x = & $render $object.Value $false
            $y = & $render $other.($object.Name) $false
            if ($x -ceq $y) { $row.raw_equal++; continue }
            if ((& $identity $x) -ceq (& $identity $y)) { $row.equal_modulo_identities++; continue }
            $x = [regex]::Replace((& $identity (& $render $object.Value $true)), ', \d+ enclosures\)', ', N enclosures)')
            $y = [regex]::Replace((& $identity (& $render $other.($object.Name) $true)), ', \d+ enclosures\)', ', N enclosures)')
            if ($x -ceq $y) { $row.equal_modulo_identities_enclosure_counts_and_work++; continue }
            $row.different++
            $row.differences += "$($configuration.Name) / $($object.Name)"
        }
    }
    return $row
}
function Summarize-Performance([string]$folder) {
    $file = Join-Path $folder 'cu3-performance-characterization.json'
    if (-not (Test-Path $file)) { return $null }
    $json = Get-Content -Raw $file | ConvertFrom-Json
    $row = [ordered]@{}
    foreach ($configuration in $json.document_drag_reference.PSObject.Properties) {
        if ($configuration.Name -eq 'drags_per_measure') { continue }
        $c = $configuration.Value
        $row[$configuration.Name] = [ordered]@{
            drag_H = [ordered]@{ median_ms = $c.'drag of H, constructive'.median_ms; p95_ms = $c.'drag of H, constructive'.p95_ms }
            drag_B = [ordered]@{ median_ms = $c.'drag of B, constructive'.median_ms; p95_ms = $c.'drag of B, constructive'.p95_ms }
            adoption_ms = $c.adoption_ms
            used_heap_constructive_MB = $c.'used heap after collection, constructive, MB'
        }
    }
    $queries = [ordered]@{}
    foreach ($drag in @('drag of H', 'drag of B')) {
        foreach ($query in @('o_1 h_2', 'e_1 n', 'e_1 c_1', 'e_1 r')) {
            $d = $json.instrumented_queries.$drag.$query.during_the_drag
            $queries["$drag $query"] = [ordered]@{ box_enclosures = $d.box_enclosures; exact_contacts = $d.exact_contacts
                median_enclose_ms = $d.median_enclose_ms; median_exact_contacts_ms = $d.median_exact_contacts_ms }
        }
    }
    $row.queries_during_the_drag = $queries
    return $row
}

$summary = [ordered]@{
    schema_version = 1
    id = 'pre-g9b-cu3-t7a-characterization'
    status = 'CU3 T7-a CHARACTERIZATION - READ-ONLY WITH RESPECT TO THE PRODUCT; NO VERDICT OF ITS OWN; TIMINGS ARE INFORMATIVE'
    comparison = if ($Candidate) { 'candidate: current = version 1, t7a = cu3-certified-admission/v2' } else { 'characterization: current = product, t7a = hypothetical T7-a' }
    source_commit = $commit
    source_tree = $tree
    worktree_clean = ($status.Count -eq 0)
    product_sources_modified = ($productStatus.Count -gt 0)
    product_class = $productPath
    product_blob = $productBlob
    shadow_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $shadowSource 'RootCountCertificate2D.java')).Hash.ToLowerInvariant()
    ledger_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $ledgerSource).Hash.ToLowerInvariant()
    product_jar = $jar.Name
    runs = $runs
    tests = [ordered]@{
        shared_compare = Summarize-Results 'shared-compare'
        shared_t7a = Summarize-Results 'shared-t7a'
        desktop_compare = Summarize-Results 'desktop-compare'
        desktop_t7a = Summarize-Results 'desktop-t7a'
    }
    ledgers = [ordered]@{
        shared_compare = Summarize-Ledger 'shared-compare'
        desktop_compare = Summarize-Ledger 'desktop-compare'
        corpus_compare = Summarize-Ledger 'corpus-compare'
        shared_t7a = Summarize-Ledger 'shared-t7a'
        desktop_t7a = Summarize-Ledger 'desktop-t7a'
        corpus_t7a = Summarize-Ledger 'corpus-t7a'
    }
    corpus_published_results = Compare-Corpus
    performance = [ordered]@{}
    cleanup_exit_code = $cleanupExit
    removed_build_leftovers = @($leftovers | ForEach-Object { $_.Name })
}
foreach ($folder in @(Get-ChildItem -Directory -LiteralPath $Output -Filter 'performance-*' -ErrorAction SilentlyContinue | Sort-Object Name)) {
    $summary.performance[$folder.Name] = Summarize-Performance $folder.FullName
}
[IO.File]::WriteAllText((Join-Path $Output 'cu3-t7a-characterization.json'),
    (($summary | ConvertTo-Json -Depth 12) -replace "`r`n", "`n") + "`n", $utf8)
Write-Output "Output: $Output"
foreach ($run in $runs) { Write-Output "$($run.name): exit $($run.exit_code), $($run.duration_s) s" }

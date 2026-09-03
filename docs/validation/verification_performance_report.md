# Verification performance and infrastructure closeout

- Status: **Closed** — [author decision in ADR 0020](../adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout)
- Historical pre-final02 evidence checkpoint: original characterization and Gradle mechanism controls complete,
  including failed original FULL; applied DEV first/repeat, R6 PHASE, COMPOSED01,
  shared/Desktop correctness focuses, failed FULL01 and successful technical FULL02
  recorded; original union-repeat, phase-to-COMPOSED01 and bounded original-shared-
  to-FULL02 and DEV49 first/repeat comparisons complete; clean-output FULL and its
  coverage/context contrast with FULL02 also complete; two candidate bootstrap
  preflight failures and their bounded 150/158-assertion focuses are retained;
  bootstrap03 real/nested execution completed with exit 0 with the separate timestamp-link
  envelope failed with exit 2; archived reconciliation passed separately;
  final CI-profile attempt01 failed at benchmark precheck; its default-log
  correction/focus passed, while replacement attempt02 remains pending at this
  documentary checkpoint
- Scope: operational/build orchestration and governance, not a product phase
- Source contract: [verification levels](../../geocedg/specs/operations/verification-levels.md)
- Design: [accepted ADR 0020](../adr/0020-verification-levels-and-current-run-evidence.md)

The author closed this cross-cutting operational task on 2026-09-03 after the
independent review accepted implementation `2b82034dbedf6f26250ad4aefb9eead700e33e66`
without source corrections. The normative approval and scope are recorded in
ADR 0020, not inferred from test success or from this report.

Final `final-full-ci-profile-02` completed with native/root exit 0 in about
44 min 49 s: 7,585 passing cases, 11 inherited omissions, 703 XML and zero
failures/errors. Its archived audit revalidated 1,453 evidence entries. Clean
FULL, COMPOSED, native bootstrap03 and the independent review's fresh DEV, PHASE,
shared-Java and Desktop checks were accepted with their recorded source-cohort
boundaries. Status-only closeout leaves executable source unchanged and reuses
that evidence; it does not repeat the expensive campaign.

The completed operational benchmark median is 214.277636 s against an
informational 5 s target: accepted non-blocking technical debt, not a target met.
Configuration cache, additional parallelism and further optimization remain
deferred. Remote CI, interactive GUI and installer/packaging generation were
not performed in the local campaign.

The implementation chronology and pre-final02 checkpoint below are historical
records from the reviewed commit. Their pending wording is not current status;
failed attempts, provisional instruments and source-cohort distinctions remain
unchanged. This approval promotes no scientific/product phase or release.

## 1. Entry and authority

The work began at clean, published `main`. HEAD, local main, origin/main and
live remote main all resolved to `3942af594e4507e479f2c75019cef62e3d9fea6f`.
The annotated `geocedg-g9u0-r6-pass` object
`2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e` peeled to that commit and matched the
remote tag object. The latest normative closed phase was G9U0-R6, not G9U1.
The roadmap was v3.64. Unmerged G9U1 planning branches were not execution authority.

Task branch: `codex/verification-performance-bootstrap-governance`. At the
implementation checkpoint no merge, stash, history rewrite, unrelated
publication, author approval or phase tag had been performed.
`.github/copilot-instructions.md` was absent and was not approximated.

Inspected authority includes root AGENTS, canonical governance/verification
and task/review prompts, ADR 0002/0015/0019, current developer/user guidance,
root/shared/Desktop/build-logic Gradle configuration, all 33 verifier entrypoint
interfaces, the 23 build-verifier command/assertion paths, generated-state and
repository-state helpers, bootstrap/installer/packaging helpers, CI, numerical
generator pins, and targeted test state/concurrency contracts. The detailed
static inventory records source paths, original lines and SHA-256 values under
`artifacts/verification-performance-bootstrap/inventory/`.

## 2. Measurement conditions

| Item | Observed original host context |
|---|---|
| OS / CPU / RAM | Windows 11 Pro 10.0.26200; i7-13700F, 24 logical processors; 31.84 GiB |
| Shell / Git | PowerShell 7.6.5; Git 2.55.0.windows.3 |
| Wrapper | Gradle 9.4.1, pinned repository wrapper |
| Launcher | JAVA_HOME `C:\Program Files\Java\jdk-22`; Java 22.0.2+9-70 |
| PATH Java | Oracle javapath Java; same major/version observed on this host |
| Compile/test JVM | `C:\Users\usuario\.jdks\corretto-17.0.10` observed in compiler/Test logs |
| Gradle user home | `C:\Users\usuario\.gradle`; GRADLE_USER_HOME unset |
| Numerical runtime | named Conda `om_env`; expected CPython 3.12.13/mpmath 1.4.1 from generators |
| Desktop runtime | Eclipse Temurin JDK 25.0.4+7-LTS detected; jpackage 25.0.4; Desktop not launched |

Measurements run serially on the same managed host, without concurrent Gradle
experiments. Existing dependency caches and build outputs were present. Global
`--rerun-tasks --no-build-cache --no-daemon` forces task execution/single-use JVMs;
it does not establish an empty dependency/download cache or a pristine workstation.
First/repeat labels refer to this task's measurements, not a factory-clean machine.

The sandbox profile is a different execution context. The first narrow sandbox
attempt encountered Kotlin daemon temporary-file AccessDenied and was interrupted;
the same command passed with managed host escalation. This is permission/context
evidence, not a product fix. Its failed saved log was overwritten by the successful
rerun at the same path; failure detail survives only in the tool transcript.
Subsequent runs use distinct logs. No baseline/reference was changed for that error.

Info/profile instrumentation and output capture are identified per command.
Parallel task-duration sums are not wall time. Unlogged pre-build intervals may
include source/hash/reference checks and cannot all be attributed to copying.

## 3. Original timing ledger

All paths below are relative to
`artifacts/verification-performance-bootstrap/baseline/`.

| Scope | Run | Wall seconds | Exit | Runtime evidence | Saved log/evidence |
|---|---|---:|---:|---|---|
| DEV-like R6 class | 1 | 72.694 | 0 | 49 tests; 35/35 actionable tasks executed | `dev-r6-class-run1.log` |
| DEV-like R6 class | 2, info/profile | 71.165 | 0 | 49 tests; 35/35 executed; suite 2.52 s | `dev-r6-class-run2-info.log`, `dev-r6-class-run2-profile.html`, XML |
| PHASE-like R6 | 1 | 357.581 | 0 | 55/55: 52 shared + 3 Desktop; Checkstyle clean | `phase-r6-run1-transcript.log`, `phase-r6-run1/` |
| Original COMPOSED inside bootstrap | 1 | 9589.0164157 observed interval | 0 | All composed gates passed, including final frontend; 41 saved Gradle invocations / 34 Test-task identities per invocation | `bootstrap-run1-transcript.log`, delegated logs |
| Original bootstrap, SkipFetch | 1 | 9590.394 stopwatch | 0 | PASS WITH WARNINGS: only fetch skipped; status entries 0 | `bootstrap-run1-transcript.log`, `bootstrap-run1-measurement.json`, archived delegated logs |
| Original FULL, FullTests/KeepBuildOutputs | 1 | 5594.1837773 | 1 | Shared unfiltered: 6,408 cases, 6 failures, 10 skipped; unfiltered Desktop/final frontend not reached | `full-run1-transcript.log`, `full-run1/original-full-run-summary.json`, batch XML archives |

Original DEV command (repository root):

```powershell
.\gradlew.bat :shared:common-jre:test `
  --tests org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest `
  --rerun-tasks --no-build-cache --no-daemon --no-problems-report --console=plain
```

Run 2 adds `--info --profile`. These are controlled pre-Level-API DEV-like
measurements, not a claim of complete phase/repository acceptance.

```powershell
.\tools\agent\verify-g9u0-r6-semantic-locus-point-interaction-support.ps1 `
  -KeepBuildOutputs `
  -LogDirectory artifacts\verification-performance-bootstrap\baseline\phase-r6-run1
.\tools\bootstrap\bootstrap-windows.ps1 -SkipFetch
.\tools\agent\verify.ps1 -FullTests -KeepBuildOutputs `
  -LogDirectory artifacts\verification-performance-bootstrap\baseline\full-run1
```

Original FULL's measured invocation ran from `2026-09-02T18:54:50.5527108Z`
to `2026-09-02T20:28:04.7372042Z`. Its wall time includes info/streaming capture
and 7.0533695 s separately measured JUnit archive work. It is a failed original
baseline, **not a successful FULL member of a before/after acceptance pair**.
The COMPOSED interval is derived from host-observed delegation/completion
markers, not a separate native stopwatch; retain that distinction in comparisons.
The R6 canonical summary SHA-256 is
`7aaed6a558bf6f86ec93a5b45eb74155d45e66b52b47c373a9ad32f43b156cc9`.

The FULL summary also retains `gradleOptsRestored=false` and the original
instrumentation's started-then-FAILED task-heading false conflict. Neither
changes the six actual test failures. The saved isolated spreadsheet reproduction
`experiments/original-full-spreadsheet-isolated-repro/trial-result.json` reports
one case/one failure for
`KernelCellDragPasteHandlerTest.testDragPasteShouldResultInNonEmptySpreadsheetCells2`.
Its runner exited 1 after 84.0913142 s native time. Its recorded native exit 0 is
not reliable: the first runner version assigned a script-local `$LASTEXITCODE=0`
and then read that shadow instead of the native global value. The FAILED task,
BUILD FAILED output and archived failing XML remain independent failure evidence;
the original artifact was not rewritten. Later controls use immediate global
native-exit capture and record the instrument hash.

## 4. Original execution matrix

This table is static source inventory, not observed execution counts. Baseline
is shown first for grouping but runs near the end. Unknown runtime discoveries
must be reconciled with saved JUnit XML before claiming equivalence.

| Verifier | Gradle calls | Test task batches | Plain test annotations selected | New unique | Repeated earlier |
|---|---:|---:|---:|---:|---:|
| baseline | 4 | 0 | 0 | 0 | 0 |
| DXF | 1 | 2 | 10 | 10 | 0 |
| G6 | 2 | 2 | 76 | 76 | 0 |
| G7A | 1 | 1 | 51 | 51 | 0 |
| G7B | 2 | 2 | 65 | 62 | 3 |
| G8A | 2 | 1 | 65 | 65 | 0 |
| G8B | 1 | 1 | 49 | 49 | 0 |
| G8C | 1 | 1 | 32 | 32 | 0 |
| G8C1 | 1 | 1 | 38 | 38 | 0 |
| G8C2 | 1 | 1 | 34 | 34 | 0 |
| G9A1 | 1 | 1 | 117 | 117 | 0 |
| G9A2 | 1 | 1 | 64 | 64 | 0 |
| G9A3 | 1 | 1 | 253 | 72 | 181 |
| G9U0 | 2 | 2 | 226 | 226 | 0 |
| G9U0-R1 | 2 | 2 | 6 | 6 | 0 |
| G9X1 | 1 | 2 | 72 | 62 | 10 |
| G9U0-R2 | 2 | 2 | 62 | 16 | 46 |
| G9U0-R3 | 1 | 1 | 39 | 22 | 17 |
| G9U0-R4 | 3 | 2 | 58 | 31 | 27 |
| G9U0-R5 | 3 | 2 | 46 | 4 | 42 |
| G9S1 | 3 | 2 | 37 | 37 | 0 |
| G9U0-R6 | 3 | 2 | 55 | 6 | 49 |
| frontend | 2 | 2 | 8 | 3 | 5 |
| Total | 41 | 34 | 1,463 | 1,083 | 380 |

The matrix represents 111 concrete classes: 95 shared/989 annotations and
16 Desktop/94. The FULL plan adds two unfiltered Test invocations; the observed
failed FULL reached only shared. Baseline's four calls include version/toolchain metadata
and two compile calls. Each of the 23 build verifiers starts a whole-generated-
output transaction. Optional launch, packaging artifacts and benchmarks are
separate and not included in these default totals.

The full matrix/source excerpts additionally record cwd/task/filter tokens,
input authorities, live/frozen assertions, report paths, numerical processes,
and snapshot sites. They distinguish default AlreadyComposed behavior from
standalone PHASE recursion. `source/shared` cwd aliases must be normalized;
counts alone do not establish equivalent Gradle execution contexts.

### 4.1 Observed log and JUnit reconciliation

The validated diagnostic parsers ran only after original builds were idle.
Log harness: 9 cases/82 assertions, exit 0. JUnit harness: 11 cases, exit 0 after
a harness-only interpreter-path correction; the first failed harness run remains
saved. These are diagnostic-instrument checks, not repository acceptance.
Exact commands, input fingerprints, outputs and limitations are retained in the
[original evidence ledger](../../artifacts/verification-performance-bootstrap/inventory/original-evidence-analysis-20260902T205213544Z.md).

Each log analysis selected one transcript and its own saved-native-log root,
excluding preexisting copies and duplicate transcript ingestion. All three
analyses exited 0 with zero warnings/identical-content groups. A successful
analysis exit does not certify that the original build passed.

| Original run | Gradle single-call logs | BUILD success / failed | Test-task identities per invocation | Actionable total / executed / UP-TO-DATE / FROM-CACHE | Reported Gradle seconds sum |
|---|---:|---:|---:|---:|---:|
| Complete bootstrap/default COMPOSED | 41 | 39 / 0 | 34 | 1,524 / 1,441 / 83 / 0 | 3,196 |
| Standalone R6 | 3 | 3 / 0 | 2 | 117 / 117 / 0 / 0 | 262 |
| Interrupted FULL | 40 | 37 / 1 | 33 | 1,473 / 1,390 / 83 / 0 | 3,519 |

Bootstrap has 32 test-containing invocations; FULL has 31; R6 has two. G5 and
G9X1 each launch shared and Desktop Test tasks within one invocation. Gradle
metadata probes explain the two non-BUILD calls in bootstrap/FULL. Repeated
`--info` headings are not extra executions: raw Test headings were 63/4/66,
respectively. Single-use-daemon notices were 39/3/38; this is not an OS/JVM
process census. Gradle duration sums omit shell/static/snapshot work and are
not whole-run wall times.

JUnit selections were explicit original index IDs, not inferred by the parser:

| FULL archive selection | Analyzer exit | XML suites | Testcase occurrences | Raw display identity union | Extra occurrences | Failed / skipped |
|---|---:|---:|---:|---:|---:|---:|
| Pre-baseline G5-through-R6 phases, batches 1–30 | 0 | 142 | 1,455 | 1,080 | 375 | 0 / 0 |
| Unfiltered shared only, batch 31 | 1 | 636 | 6,408 | 6,389 | 19 | 6 / 10 |
| Complete captured FULL, batches 1–31 | 1 | 778 | 7,863 | 6,483 | 1,380 | 6 / 10 |

These overlapping selections must not be added as separate runs. Phase rows
contain 1,336 shared occurrences/986 raw identities and 119 Desktop occurrences/
94 identities. **Batches 1–30 are not the complete original default COMPOSED**:
that successful bootstrap also ran final frontend checks after the baseline.

The absent final frontend invocations select `AppConfigGeoCeDGTest` (3 methods)
and `GeoCeDGProfileTest` (5). The latter five methods already appear in phase
batches 1, 19 and 22; the shared profile class is absent from batches 1–30.
Thus 1,455+8=1,463 selections, 1,080+3=1,083 identities, 110+1=111 classes and
375+5=380 repeats reconcile the static plan. This is **not** an independently
archived/recounted 1,463-case bootstrap XML result: bootstrap has successful
frontend logs (105 s shared, 80 s Desktop) and original count assertions, but no
per-batch raw XML capture from that earlier run. Exact frontend method names
and source lines are listed in the evidence ledger.

The shared-only/combined parser exits retain 657 diagnostics: two incomplete/
metadata flags, 636 per-file inherited started-to-FAILED context flags and
19 duplicate-display diagnostics. The 19 extra rows represent 17 raw identities
in six upstream parameterized suites: `MmsExamTests`, `SliderInputDialogModelTests`,
`AlgebraOutputFormatTests`, `IntegralEditorTests`, `OperationFilterTests` and
`SpreadsheetControllerTest`. All these repeated rows passed; they are separate
from the six failing tests. The ledger preserves every exact display label.
A raw `(module,suite,classname,display-name)` tuple is not necessarily a unique
Java method/invocation. Preserve case **multisets and outcomes**, not only sets;
do not reject legitimate parameterized display collisions as stale/forged XML
or silently collapse them into fewer executed cases. Raw XML hashes bind each
saved report, not cross-run timestamp/duration equivalence.

### 4.2 Executed first-union XML identity/outcome comparison

The separately reviewed artifact-only comparator completed with exit 0,
`MATCHED_EXPECTED_COVERAGE_MULTISET_ADDITIONS`, zero differences and no diagnostic
failures. Evidence:
`inventory/junit-identity-comparison-runs/20260902T2142221376627Z-e5b2d8dfdbb646cb9045b094dd6d33a6/`.
Its `summary.json`, `differences.json`, original/canonical case-multiset JSON and
`input-fingerprints.json` preserve the result. Instrument:
`inventory/compare-original-union-junit.ps1`, executed SHA-256
`a3cbbc5df5fe289776e5a7afc2c6472eaa9e3b71dcb641ffaccbf33a56742674`.

| Module | Original phase occurrences / raw identities | First canonical occurrences / raw identities | Identity/outcome comparison |
|---|---:|---:|---|
| Shared | 1,336 / 986 | 989 / 989 | Every baseline identity retained; only three expected frontend additions |
| Desktop | 119 / 94 | 94 / 94 | Exact coverage/outcome multiset match |

The instrument hash-checked and reparsed the 142 original phase XML reports,
cross-checked their 1,080-identity union/1,455 occurrences against the validated
analyzer, then hash-checked the 111 canonical XML copies and their native logs
against the saved first-union trial ledgers. Within each original batch, raw
identity/outcome multiplicities are preserved. Across batches, expected coverage
is multiset union (maximum per-batch multiplicity), not a requirement to repeat
every historical selection in one union. No method names were normalized or
inferred from annotations.

Only these three additional pre-existing shared `AppConfigGeoCeDGTest` cases were admitted, each exactly
once with PASSED outcome: `hasIndependentProductIdentityAndPreferences()`,
`preservesClassicSerializationAppCode()` and
`gatesOnlyDedicatedLocusV2CommandsByDefault()`. No baseline identity was missing
and no outcome/multiplicity differed. All input hashes were rechecked before
publishing the comparison. Recorded original HEAD/clean-status contexts match;
this diagnostic performs no new source, environment or toolchain validation.
Different-run XML timestamps/durations are excluded from semantic equality and
do not prove freshness. The result establishes the observed first-union coverage
relationship, not applied consumer/COMPOSED/FULL acceptance. The separate executed
first/repeat and applied-COMPOSED01 relations below are not inferred from equal totals.

### 4.3 Executed repeat and applied-COMPOSED01 comparisons

The original first/repeat extension finished with exit 0 and
`MATCHED_PHASE_COVERAGE_AND_FIRST_REPEAT_MULTISET`: zero phase-to-first or repeat
differences, shared 989 and Desktop 94 exact raw identity/outcome occurrences in
each first/repeat pair. The original phase relation still admits only the three
frontend cases already listed above. Summary:
`inventory/junit-identity-comparison-runs/first-repeat-20260902-review-1-478347d28d534f719bd8e388ff4601ef/summary.json`,
SHA-256 `fc6899c871a583c109c99572017a22034b44d37f87e46b2440ee50cf5c86356d`.
The comparison rehashed 413 consumed saved inputs before completion.

The separately tested candidate loader then compared original phase batches 1–30
with the completed applied COMPOSED01, exit 0,
`MATCHED_SAVED_XML_RELATION_NOT_ACCEPTANCE`, zero differences. Original 1,455
occurrences / 1,080 raw identities yield the expected union plus eight exact PASSED
additions: the three frontend cases, three new executable GeoCeDG command cases
and two legacy-redefine cases. Candidate totals are 994 shared + 94 Desktop = 1,088,
across 113 XML archives; 273 consumed inputs were revalidated. Summary:
`inventory/candidate-junit-comparison-runs/phase-to-composed-applied01-20260903-694d2a60d37d464394c6e20c7bda8b0c/summary.json`,
SHA-256 `81ad84104c1b35648f09e22f5d7bc40e57893dfb604f73c1354b2020656835c2`.
Its prior synthetic loader harness passed 11/11 cases with exit 0; that fixture
result is not product acceptance. Keep the source sets distinct: archived phase
union = 986 shared + 94 Desktop = 1,080; original first/repeat union probes already
include the three pre-existing frontend cases, hence 989 + 94 = 1,083. Applied
COMPOSED01 is phase union plus those three and five new shared cases, hence
994 + 94 = 1,088—not 989 + 8. The later eight Desktop adapter cases extend the
declared composed perimeter to 102 Desktop; its next actual delegated execution
must still supply its own observed result.

Both compare module/suite/class/raw-display/outcome multisets, preserving legitimate
multiplicities. Phase union uses maximum per-batch multiplicity; first/repeat uses
exact total multiplicity. Neither establishes numerical-output determinism, new
fresh execution or author approval. COMPOSED01 predates the eight Desktop adapter
tests; it is not coverage evidence for those later additions. The subsequently
completed OriginalFullShared-to-FULL02 and archived DEV49 first/repeat relations
are recorded in section 8.2. These bounded repeated executions do not imply
universal numerical-output determinism or require an additional bootstrap repeat.

## 5. Bottlenecks and Gradle study

Completed original measurements support the following observations. Applied
workflow timings are recorded in section 8; they do not isolate the contribution
or causal ranking of every mechanism:

1. Repeated whole-build-tree copy/restore: original restoration intervals of
   approximately 106 s (G5) and 130 s (G6) occur before the next gate. Original
   KeepBuildOutputs still copies first and deletes that backup at cleanup.
   A separate unchanged-helper measurement is now complete: 23 generated roots,
   16,719 files and 315,057,949 logical bytes; inventory 0.6738304 s, snapshot copy
   82.5443079 s, temporary-backup discard 2.6049408 s, exit 0. Evidence:
   `experiments/original-keep-snapshot-run1/snapshot-measurement.json`.
   Helper SHA-256:
   `80aa0100c377ef9e386115b39713988fd995facd76b3d0f0ca7f279be36dca41`.
   This measured one post-baseline tree and the original explicit-retention
   path, not every earlier phase or default restoration. Copy includes the
   helper's own enumeration; the separate inventory precedes it. Current
   generated outputs were neither removed nor restored; dependency caches
   were not cleared. Logical bytes are not disk-allocation or transferred-I/O
   counters, and not every pre-build gap is a copy interval.
2. Forced dependency recompilation and repeated configuration/JVM starts:
   both narrow DEV runs execute all 35 actionable tasks although the second
   JUnit suite itself takes 2.52 s. Profile: total 71.04 s, startup 7.272 s,
   loading projects 17.132 s, configuring 2.810 s; summed parallel task duration
   is 81.79 s and must not be added as wall time.
3. Repeated test execution: the captured FULL phase subset has 375 extra raw
   testcase occurrences beyond its 1,080-identity union. The static default
   estimate of 380/1,463 additionally includes the final frontend rerun as
   reconciled above. These are coverage/accounting observations, not measured
   time savings; no test deletion is proposed.
4. Independent operational/reference work remains valuable. Book fixtures took
   130.782 s in the original bootstrap's observed interval (126.807 s in the
   completed original FULL); repeated extended-reference checks
   and frozen authority checks are not automatically equivalent or dispensable.

| Mechanism | Current fact | Required experiment / candidate decision |
|---|---|---|
| Task reuse | Test-only rerun controls retained 45 UP-TO-DATE task identities and executed the selected Test task | Preserve fresh Test execution; validate candidate whole-run consumers separately |
| Local build cache | Three common-jre compile tasks and all four selected Checkstyles recovered FROM-CACHE in targeted controls | This does not establish Desktop compile recovery or cached Test acceptance |
| Configuration cache | Exact 9.4.1 probe failed with two configuration-time Git process problems | Entry discarded; no repeat/global enablement; candidate keeps it disabled |
| Daemon | Matched explicit daemon controls measured 15.7421477 s then 9.0087025 s native time | Small selected-class observation only; candidate permits reuse only with explicit retained outputs |
| Parallelism | Root enables task parallelism; no new fork/JUnit parallel policy | Keep existing test isolation, no new test parallelism; characterize before any later adoption |
| Desktop Test guard | Upstream `outputs.upToDateWhen { false }` | Preserve unchanged; do not obtain speed by reusing stale tests |

Primary versioned Gradle sources and saved control logs support bounded
decisions. Cache/configuration/daemon benefits are not established by support
in Gradle alone; no global configuration-cache or upstream build change is
proposed from these results.

### 5.1 Original-checkout controls through daemon-repeat

All seven controls below selected exactly
`org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest` on
`:shared:common-jre:test`, one native invocation per trial. Each archived 49
testcases with zero failures/errors/skips; native and runner exits were 0.
The saved result explicitly says `FRESHNESS_AND_ACCEPTANCE_NOT_ESTABLISHED`:
these are original-checkout mechanism studies, **not candidate DEV timings or
complete repository acceptance**.

| Trial directory under `experiments/` | Native seconds | Total through evidence seconds | Distinct task outcome observations |
|---|---:|---:|---|
| `task-scoped-rerun-1` | 16.0773999 | 16.616143 | 1 executed-or-started, 45 UP-TO-DATE |
| `task-scoped-rerun-2` | 16.0031357 | 16.5172107 | 1 executed-or-started, 45 UP-TO-DATE |
| `build-cache-prime` | 80.7298171 | 81.3050328 | 46 executed-or-started; global rerun |
| `build-cache-recovery` | 19.1345292 | 19.657302 | 7 executed-or-started, 36 UP-TO-DATE, 3 FROM-CACHE |
| `build-cache-retained-repeat` | 16.0871709 | 16.6090194 | 1 executed-or-started, 45 UP-TO-DATE |
| `daemon-first-observed` | 15.7421477 | 16.2539937 | 1 executed-or-started, 45 UP-TO-DATE |
| `daemon-repeat` | 9.0087025 | 9.5232723 | 1 executed-or-started, 45 UP-TO-DATE |

Every row additionally records seven NO-SOURCE tasks and one SKIPPED build-logic
guard, not skipped tests. Repeated Test headings remain one task identity.
The recovered tasks were `:shared:common-jre:compileJava`,
`:shared:common-jre:compileTestFixturesJava` and
`:shared:common-jre:compileTestJava`; Test itself was not FROM-CACHE or UP-TO-DATE.
Retained-output controls demonstrate incremental reuse, not cache recovery.

Each trial's `trial-result.json` preserves the exact argv/cwd, native and wrapper
exit, raw/timestamped log paths and hashes, report/profile archives, bounded
wrapper hashes, original HEAD and before/after status/environment digests.
Instrument SHA-256 for these seven runs:
`59da95cb6cada0612e7b0d9c94602c3a33456b2d75c2d2fddc5060d8674e2294`.
Native timing includes streaming capture; preparation and evidence collection
are separate. Final JSON serialization/log hashing is outside those intervals.

Task-scoped trials use `:shared:common-jre:test --rerun --tests <class>` with
`--no-build-cache --no-daemon`; cache-prime uses global `--rerun-tasks` and
`--build-cache --no-daemon`; recovery/retained-repeat use task-scoped `--rerun`
and `--build-cache --no-daemon`. Both daemon trials use the same task-scoped
selection with `--build-cache --daemon --no-parallel --max-workers=1`.
All include `--no-problems-report --console=plain --stacktrace --info --profile`
and `-Dorg.gradle.java.installations.auto-download=false`. The daemon pair is a
matched first/repeat observation; comparison with earlier controls also changes
worker/parallel policy and cannot isolate daemon causality. No ratio here is a
candidate or whole-repository speed-up. Later one-worker and Checkstyle controls
are recorded separately below rather than silently merged into this pair.

### 5.2 Additional completed selection/configuration controls

The following additional completed original-checkout controls are included;
none is an applied candidate verification level.

| Trial directory under `experiments/` | Native / total-through-evidence seconds | Native / runner exit | Saved JUnit |
|---|---:|---:|---|
| `configuration-cache-probe` | 9.5464861 / 10.0609976 | 1 / 1 | 49 cases, zero failures/errors/skips; configuration-cache storage failed |
| `short-class-selection` | 8.5641931 / 9.0809939 | 0 / 0 | 49 cases, zero failures/errors/skips |
| `canonical-shared-union` | 32.1784833 / 35.0980907 | 0 / 0 | 989 cases in 95 archived suites, zero failures/errors/skips |
| `canonical-desktop-union` | 19.1507593 / 20.1475602 | 0 / 0 | 94 cases in 16 archived suites, zero failures/errors/skips |
| `canonical-shared-union-repeat` | 30.6325716 / 33.5668296 | 0 / 0 | 989 cases in 95 archived suites, zero failures/errors/skips |
| `canonical-desktop-union-repeat` | 18.6110230 / 19.5948666 | 0 / 0 | 94 cases in 16 archived suites, zero failures/errors/skips |
| `conservative-worker-policy` | 16.0754153 / 16.6081184 | 0 / 0 | 49 cases, zero failures/errors/skips |

The configuration-cache probe adds `--configuration-cache
--configuration-cache-problems=fail` to the matched daemon/one-worker R6-class
command. Its log reports 35 actionable tasks (1 executed, 34 UP-TO-DATE), then
discards the configuration-cache entry with two problems. The current Desktop
build script launches `git rev-parse HEAD` and
`git status --porcelain=v1 --untracked-files=normal` while resolving provenance
at configuration time (`source/desktop/desktop/build.gradle.kts`, lines 22–26,
48, 61 and 87). This is observed configuration-cache incompatibility, not a
failing 49-case test suite. The saved
`experiments/configuration-cache-probe/configuration-cache-report.html` SHA-256 is
`0e0f01cd9873e88e46943654ab4bf81e16cac16ba6a6575d4ce14dc30ea5ecba`.
No configuration-cache repeat or upstream workaround was performed. The
candidate's explicit no-configuration-cache policy is therefore supported by
the exact checkout/probe, not only a general precaution.

The short-class trial uses `--tests G9U0R6SemanticLocusPointInteractionTest`,
demonstrating this ordinary uppercase Gradle shorthand on pinned 9.4.1. It does
not prove every parameterized/custom-display filtering pattern. The shared
union selects `org.geocedg.*`, `org.geogebra.common.kernel.commands.RedefineTest`,
`org.geogebra.common.euclidian.DrawablesTest` and
`org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest` in one shared Test
invocation. Both use task-scoped `--rerun`, build cache, daemon,
`--no-parallel --max-workers=1 --no-configuration-cache`, the same info/profile
capture flags and disabled toolchain auto-download. Their saved task identities
show one executed-or-started Test and 45 UP-TO-DATE identities (35 actionable
tasks: one executed, 34 UP-TO-DATE). The shared result agrees with the 989 shared
static selections. The separate Desktop command selects `org.geocedg.*` on
`:desktop:desktop:test --rerun` with the same conservative policy and reports
94 cases. The shared repeat uses the identical shared argv; equal totals alone
do not establish identity/outcome equivalence or determinism. The one-worker
control uses the R6 class with `--no-daemon --no-parallel --max-workers=1` and
build cache. These are not all phase consumers, COMPOSED or candidate acceptance.
Exact commands/hashes remain in each unchanged `trial-result.json`. Section 4.2
records the executed first-union multiset comparison rather than inferring
coverage from 989+94 alone. Section 4.3 records the completed first/repeat raw
multiset comparison; equal case counts alone still do not establish determinism.

### 5.3 Checkstyle reuse and recovery

The controls select exactly `:shared:common:checkstyleMain`,
`:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain` and
`:desktop:desktop:checkstyleTest` under build cache, daemon, one worker,
no task parallelism and no configuration cache. Prime applies task-scoped
`--rerun` to each of the four; repeat/recovery do not. All use the same saved
info/profile capture and disabled toolchain auto-download flags.

| Trial under `experiments/` | Native / total-through-evidence seconds | Native / runner exit | Four selected Checkstyle outcomes |
|---|---:|---:|---|
| `checkstyle-cache-prime` | 60.9814148 / 61.4312042 | 0 / 0 | All executed-or-started |
| `checkstyle-retained-repeat` | 4.6368522 / 5.0834698 | 0 / 0 | All UP-TO-DATE |
| `checkstyle-cache-recovery` | 4.8452005 / 5.3258359 | 0 / 0 | All FROM-CACHE |

These distinguish retained-output reuse from actual cache recovery. They run
no canonical Test task: zero JUnit reports/cases here means **NO_TEST_ACCEPTANCE**,
not zero tests required by the repository. The saved trial/log evidence observes
Gradle/style outcomes, not the candidate's live/archive Checkstyle receipt
validation. No Desktop compilation-cache recovery claim follows from these four
style tasks. All original evidence remains under the named trial directories.

### 5.4 Candidate fingerprint cost, not end-to-end verification

The explicit preview-module measurement is recorded in
`inventory/fingerprint-cost-runs/20260902T2129125309964Z-3f9a0ba825e442c996f395ec8858466f/summary.json`,
exit 0, `MEASURED_CONSISTENT_INPUTS_NOT_ACCEPTANCE`. It invoked no Gradle probe,
canonical build, installation or cache/configuration mutation. The real source
runtime module did not yet exist. Measured preview SHA-256:
`77ee6edcb4ba8ad5111051baec9b984f5b206c8e5d1006fb866a6dba2a19e6ec`;
instrument SHA-256:
`70d9c1ce8ed23e9d6bf58951349734624e7b2e9ad2e03822eb9026153d34143c`.
Later fixture corrections mean these are version-bound observations, not a
measurement of every subsequent preview revision.

| Actual primitive call | Outer seconds | Runtime internal seconds |
|---|---:|---:|
| Raw inventory, first in this series | 30.4100415 | not instrumented |
| Raw inventory, repeated same module | 10.2536019 | not instrumented |
| Complete input identity, first after raw calls | 10.0951777 | 10.0932768 |
| Complete input identity, repeated | 10.1108372 | 10.1106780 |

All calls read an inventory of 11,137 existing source inputs, zero missing
entries, with 182,670,666 logical payload bytes. Both identity calls recorded
19 external entries / 13 existing files / 235,398 observed external-file bytes.
The raw-tree digest and identity/environment/configuration digests remained
consistent. Module import was 0.0364922 s. A separately excluded final identity
validation took 10.2135291 s outer / 10.2133761 s internal.

First means the first measured call in this fresh module/series, not cold OS or
disk caches. Raw calls precede identity calls, so subtracting their durations
does not isolate causal overhead. The outer timer includes the primitive call
and returned object; owner/hash guards and summary processing are outside it.
Internal identity timing excludes its final serialization/hash. Logical bytes
are not physical reads, and the closure does not include every generated file
or the independent book tree. Only explicitly supplied already-known toolchain
files were checked; no new toolchain discovery is inferred. Digests reject
observed changes, not modifications reverted between checks. No raw inventory,
environment plaintext or external path/content was persisted. This real cost
must remain included in whole-level measurements, not bypassed to make
verification faster.

Pinned 9.4.1 documentation was read directly (some direct opens initially failed;
the versioned DSL/manual hyperlinks resolved). The
[CLI](https://docs.gradle.org/9.4.1/userguide/command_line_interface.html#sec:rerun_tasks)
distinguishes global dependency rerun from task-scoped `--rerun`.
[TaskOutputs](https://docs.gradle.org/9.4.1/javadoc/org/gradle/api/tasks/TaskOutputs.html)
confirms that a false `upToDateWhen` predicate prevents both local output reuse
and loading those outputs from cache. The
[cache manual](https://docs.gradle.org/9.4.1/userguide/build_cache.html)
requires correct task inputs and incremental behavior; enabling a switch alone
does not establish cache correctness. The
[Test DSL](https://docs.gradle.org/9.4.1/dsl/org.gradle.api.tasks.testing.Test.html)
defines defaults `maxParallelForks=1`, `forkEvery=0`, and separate test JVMs.
The [daemon manual](https://docs.gradle.org/9.4.1/userguide/gradle_daemon.html)
explains why `--no-daemon` can still create a disposable build JVM when client
and build JVM arguments differ. Actual host behavior remains a measurement,
not an inference from these API defaults.

## 6. Redundancy and preserved scientific contracts

Exact execution candidates include G9A1/Redefine/G9A2 repeated by G9A3 (181
annotations), later R2/R4/R5/R6 tests already matched by the old G9U0 wildcard
(133), repeated profile checks (15), and repeated DXF/laboratory/persistence
support classes (51). These figures are static, not runtime savings.

No tests are removed, merged, demoted or declared obsolete. Different tests
covering one requirement, lifecycle/persistence variants, historical regressions
and independent numerical references remain. The performance mechanism does not
change scientific references, tolerances, serialization or geometric semantics.
The request's section 18 permits minimal, justified, documented and regression-
tested upstream/product repairs when correctness genuinely requires them; it is
not a blanket blocker on repairing the reproduced failures. Such a repair must
be classified and validated separately, never used to hide a failure or make
optimization easier. Those bounded repairs are now applied and documented in
[the compatibility design](../architecture/verification-baseline-compatibility-repair.md):
ordinary collected redefinition only completes captured participating spatial
contexts; retained command tests distinguish Classic-OFF from GeoCeDG-ON and the
coverage inventory discovers executable new command tests. Shared focus passed
85 tests, and FULL01's unfiltered shared run passed 6,417 cases with ten prior skips.
FULL01 then failed on four missing Desktop tool-resource routes. The subsequent
closed adapter renders the existing owned SVG, with two ImageManagerD seams;
its 34-case Desktop focus passed, including eight additive adapter tests and the
four unchanged resource tests. FULL02 then completed technical FULL gates for that
repaired checkpoint, as recorded in section 8.2; it does not turn either failed
FULL into a successful original baseline. All original tests remain
present; scientific references, tolerances, serialization and feature maturity are
unchanged.

The two opt-in `LegacyCeDGScientific...` benchmark bodies can return early
without `GEOCEDG_G6A_RUN_SCIENTIFIC_BENCHMARK=1`. JUnit method counts alone do
not prove those expensive bodies ran. Report their opt-in separately. Disabled
upstream FULL tests must be inventoried; normative GeoCeDG/required upstream
tests may not silently skip.

## 7. Candidate architecture and rejected shortcuts

The applied candidate level contract preserves all current applicable non-test and phase
assertions. COMPOSED runs `org.geocedg.*` separately per module, adding the three
required shared upstream classes RedefineTest, DrawablesTest and
LocusV2InputPreviewLifecycleTest. FULL executes unfiltered shared-JRE/Desktop
Test tasks. Every original consumer still checks its exact classes, methods,
counts, style XML, frozen authority, references and canonical summaries.

Same-invocation evidence is not a persistent test cache. Its module-owned
capability binds raw source, Git/index/status, process environment, relevant
external configuration, toolchains, actual Test JVMs, task outcomes, and raw
live/archive reports. Unsupported ignored inputs/init scripts fail closed.
Final closure checks all input/report/audit artifacts and invalidates ownership
even on failure. CI JUnit failures are rejected independently of ignoreFailures.

One outer default generated-state transaction replaces nested consumer copies.
Explicit KeepBuildOutputs does not copy. CleanBuild is FULL-only and clears
validated generated output targets, never the user/dependency cache. Recovery
data survives restoration failure.

Rejected shortcuts: using SkipBuild as acceptance, deleting historical tests,
loosening counts/tolerances, stale XML existence checks, persistent receipts,
global configuration-cache enablement, new test parallelism without proof,
broad upstream refactors, or unrelated GUI/export changes. The independent execution path remains
available for diagnosis and equivalence checks.

## 8. Before/after and validation

Completed applied-candidate observations appear in section 8.2; unresolved gates
are explicit in section 8.3. Original-checkout mechanism controls, preview fixtures
and fingerprint primitives retain their original scope and are not whole-level
candidate timings. DEV/PHASE workflow deltas below are context-qualified, not
pure Gradle causal estimates. There is no matched original top-level COMPOSED
stopwatch and no successful original FULL before member for an exhaustive pair.

Compare context-qualified testcase/outcome multisets and semantic summaries,
using stable method identities only where the framework actually exposes them;
raw display labels may collide. Do not compare raw JUnit bytes across runs as
semantic equality: timestamps/durations vary. Retain raw hashes per run for
integrity. A failed/incomplete original FULL cannot supply a successful FULL
acceptance or speed-up pair. Applied correctness repairs and their focused results
are recorded without deleting the original failure evidence; the completed
successful FULL02 retains that source/coverage distinction.
Record instrumentation overhead and incomparable conditions;
do not silently replace an unfinished whole-run measurement with an extrapolation.

### 8.1 Executed preview fixture evidence

The third preview infrastructure invocation completed with exit 0 and saved
`PASS_FAKE_FIRST_OPERATIONAL_ONLY` in
`artifacts/vp-preview-fixtures-03/verification-infrastructure.json`:
runtime **109/109** cases passed; generated-state **18 cases / 143 assertions**
passed. Saved logs are `verification-runtime.log` and `generated-state.log` in
that same directory. The wrapper recorded 65.43 s and 2.096 s, respectively;
these are fixture-run durations, not candidate product-performance timings.
The command invoked the unapplied
`proposals/files/tools/agent/verify-verification-infrastructure.ps1` with
`-LogDirectory artifacts/vp-preview-fixtures-03`, using explicit host
`pwsh -NoProfile` child processes and the preview module/helper paths.

Attempts 01 and 02 remain preserved, not overwritten: first 53/108 runtime
cases passed (55 failed), then 85/109 passed; both wrappers exited 1 before
generated-state execution. The saved classification in
`inventory/preview-fixture-first-failure.md` identifies candidate
OrderedDictionary task-grouping/idempotent-argument-normalization defects,
then a sparse synthetic-repository topology mismatch. Corrections were followed
by attempt 03; failure records and unchanged rejection assertions remain.
This is genuine fake-first preview evidence, not a successfully applied source
gate, Java regression suite, COMPOSED/FULL run or author approval.

### 8.2 Completed applied-candidate observations

The measurement-runner paths below are under
`artifacts/verification-performance-bootstrap/after/`; each retains exact argv,
cwd, raw output, source/environment context, native/wrapper exits and hashes in
`measurement-result.json`, plus finalized `authority/verification-result.json`.
The first four successful measured runs (DEV01/02, PHASE01 and COMPOSED01) had
57 bounded source-file hashes unchanged before/after and equal recorded inherited-
environment digests. They were applied worktree states, not the unchanged original
checkout or the later Desktop repair. FULL02 separately binds its 60-path checkpoint.

| Applied run | Fresh-process authority wall / root internal seconds | Finalized scope and result |
|---|---:|---|
| `dev-r6-applied-01` | 9.6219975 / 9.316 | DEV shared R6 class, 49 passed, zero skips, exit 0, PASS_SCOPED_NOT_ACCEPTANCE; retained outputs |
| `dev-r6-applied-02` | 9.6290870 / 9.306 | Same 49-case DEV scope, zero skips, exit 0; retained outputs |
| `phase-r6-applied-01` | 77.7441644 / 77.468 | Explicit R6 PHASE: 52 shared + 3 Desktop, Checkstyle and phase assertions; exit 0, technical gates passed; retained outputs |
| `composed-applied-01` | 1226.3377158 / 1226.049 | 994 shared + 94 Desktop, all mandatory tests passed, four Checkstyles and composed assertions; exit 0, technical gates passed; default restoration |
| `full-applied-01` | 466.6486504 / 466.312 | FAILED, native/wrapper/root exit 1: shared 6,417 cases with ten prior skips and no failures; Desktop 1,171 with one resource failure and one prior skip. No consumable receipt or completed composed assertion chain. |
| `full-applied-02` | 1180.2499605 / 1179.965 | Technical FULL gates passed; native/runner/root/environment-envelope exit 0. Shared 6,417 + Desktop 1,179 = 7,596 cases, 7,585 PASS + 11 upstream SKIP, no failures/errors, 703 XML; 60-file repaired checkpoint, retained outputs. |
| `clean-full-applied-01` | 1770.961057 / 1770.661 | Technical CleanBuild FULL passed; native/runner/root/environment-envelope exit 0. Same 7,596 cases / 703 XML / 11 retained skips, source60 unchanged, all four Checkstyles executed; 23 repository-generated paths cleared and reported restored, no dependency-cache reset. |
| `final-full-ci-profile-01` | 1732.4888133 / 1732.191 | **FAILED**, native/runner/tool/root exit 1, no instrumentation errors; source61/status/index/parent-environment unchanged. Benchmark precheck collided with existing generated-state evidence; no benchmark JSON, warmup or measured samples. A partial canonical receipt is not successful FULL evidence. |

The first seven rows used canonical level routes, not IndependentBuilds. Only their seventh
selected CleanBuild. DEV/PHASE and the first two FULL attempts retained outputs;
COMPOSED01 and clean-output FULL used default restoration. Those first seven recorded
launch, packaging artifacts, operational benchmarks and scientific benchmark bodies
as not requested. The two optional
scientific bodies therefore cannot be claimed from their JUnit method counts.

| Qualified workflow pair | Before seconds | After seconds | Reduction seconds | Observed workflow reduction |
|---|---:|---:|---:|---:|
| Original DEV2 → applied DEV01 | 71.165 | 9.6219975 | 61.5430025 | 86.4793% |
| Original DEV2 → applied DEV02 | 71.165 | 9.6290870 | 61.5359130 | 86.4694% |
| Original standalone R6 → applied PHASE01 | 357.581 | 77.7441644 | 279.8368356 | 78.2583% |

DEV before is the direct-Gradle 49-case run with `--info --profile`, global rerun,
no build cache and no daemon. After adds root/JUnit/archive/publication checks and
a fresh PowerShell child while deliberately changing incremental/cache/daemon/
worker policy. PHASE retains the 55-case standalone perimeter but adds dispatcher
and fresh-process overhead plus incremental policy. These are named workflow
deltas, not isolated Gradle speed-ups, variance estimates or stable-performance
claims. The separate original-source controls provide the mechanism evidence.

After wall includes fresh-pwsh startup and streamed capture but excludes runner
preparation/evidence work (DEV01 1.2052901 s, DEV02 1.1738299 s, PHASE01 1.1534868 s,
COMPOSED01 1.2060228 s, FULL01 1.1710575 s, FULL02 1.204947 s) and final measurement-summary publication.
Root internal stopwatches have a narrower boundary. Original FULL includes 7.0533695 s
of separately measured synchronous JUnit archival; no subtraction is used to invent
an uninstrumented wall. Original FULL and measured FULL01 retain the caller's
recorded info-logging environment treatment; the candidate runner does not inject
or rewrite environment values. FULL01's separate `measurement-environment.json`
records envelope exit 2 and `ExactPresenceAndValueRestored=false`: GRADLE_OPTS was
absent before and present with an empty value after. Preserve that instrument
restoration failure separately from the measurement/root exit 1 and actual Desktop
test failure; it is not a successfully restored environment or a product fix.

Do not calculate a COMPOSED percentage against the original 9589.0164157 s
nested-bootstrap marker interval: it is not the same boundary as the new top-level
stopwatch. No matched original top-level interval exists. The failed original FULL
did not reach Desktop; FULL01 also failed. Successful repaired FULL02 therefore has
no successful original exhaustive before member, and no FULL percentage is reported.
Clean-output FULL and the completed normal bootstrap03 workflow are recorded
separately below; neither supplies a successful original FULL timing pair.

COMPOSED01's canonical receipt records two Test-containing native calls (shared + two Checkstyles,
37.4608527 s; Desktop + two Checkstyles, 33.6021778 s) plus two metadata calls
(8.1136759 s combined). These four canonical calls total 79.1767064 s; both Test tasks EXECUTED.
Actionable summaries total 86 tasks: three executed, 83 UP-TO-DATE, zero FROM-CACHE.
Desktop checkstyleMain executed; the other three Checkstyles were UP-TO-DATE.
Two additional live baseline Gradle metadata calls are visible later in raw output
(lines 14685/14704), so the observed total is six production Gradle invocations,
not four; those extra calls have no saved native stopwatches. The 1147.1610094 s
difference from outer authority wall is unallocated orchestration time, including
those untimed calls, other checks, references, consumers, cleanup and process-boundary
work. It is not a measured snapshot/fingerprint total or causal breakdown.
Detailed saved task/context accounting is in
`inventory/applied-performance-closed-runs-20260903.md`.

The saved canonical-summary audit found five of six COMPOSED phase summaries
byte-identical to both preserved originals. R2 has exactly three changed current-
verifier raw-source hashes; all other fields/text match. Do not report all six as
byte-identical. Exact pairs/hashes/differences:
`inventory/composed-canonical-summary-comparison-01.{md,json}`.
Standalone R6 PHASE's summary is byte-identical to its original, SHA-256
`7aaed6a558bf6f86ec93a5b45eb74155d45e66b52b47c373a9ad32f43b156cc9`.
These saved-artifact comparisons are not additional executions.

The completed archived DEV49 relation additionally checks both DEV runs' 49
exact (module, suite, classname, raw display name, outcome) tuples and their
multiplicities, not only the totals. All are PASSED once; no failures/errors/skips.
The recorded before/after objects also agree across runs (57 selected source
records, status/index, environment digest and instrument), and argv agree after
replacing only each own LogDirectory. Both task logs show fresh execution,
35 actionable tasks = 1 executed + 34 UP-TO-DATE, and the same daemon's 13th/14th
builds. These are warm retained-output repetitions, not cold measurements or
whole-build-input/numerical determinism. No live source or live XML was reopened
by the analysis. Description:
`inventory/dev49-pair-description-d2f7020ad0d24c65b927a3368fccdd2e/description.json`,
SHA-256 `1149639704093ef1ea9023b6c268e83680ce4338e36d605451242a32d2cfcaec`;
native analysis exit 0. Its 570 descriptive guard observations are not product
tests. This supplements the independent original-source cache/daemon/repeat
controls and the exact saved-union relations in sections 4–5.

Separate correctness focuses, not DEV timing-pair substitutes:

- `artifacts/vp-java-focus-01/verification-result.json`: DEV shared exit 0,
  85 cases / 11 classes, zero failures/errors/skips; 55.314 s root,
  54.2198082 s inner. Required command/legacy/spreadsheet/spatial-redefine coverage
  ran; Checkstyle and whole-repository gates did not.
- `artifacts/vp-desktop-resource-focus-01/verification-result.json`: DEV Desktop
  exit 0, 34 cases = 8 new adapter + 4 unchanged resource + 16 inspector + 6 menu
  tests, zero failures/errors/skips; 29.963 s root, 29.0712652 s inner. The detailed
  `dev/adcc0d48ba3b43bea74771ed5e8b064d/dev-summary.json` binds archived XML.
  This includes the later resource fix but is not Checkstyle or FULL.

Both focuses retained outputs and returned PASS_SCOPED_NOT_ACCEPTANCE. Root-result
SHA-256 values are respectively
`f13b766719c872af9b450b6d829499d38f08ca70bb16c49714f1c0d306726aa6` and
`e030ce066b178193f873e6bf2f34e2d722f9ca02ce0486dcef09e7f204c9d041`.
COMPOSED01 root SHA-256 is
`cbeffecf4ae5b7a58d4a0e1ded3494a4fea73269412512259e43b541955a8b74`;
failed FULL01 root is
`80e46caf0292b5a94b1b8f7cd178e0e3271d48ff58d50b30bf1f9423fb617593`.


FULL02 finalized from 23:05:58.4909310Z to 23:25:38.4569707Z on 2026-09-02.
Its receipt, sealed 23:14:00.0895036Z, intentionally still says
`TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING` on disk. The later root result
says `TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL`, exit 0, with the raw final line
`FULL technical verification gates passed; author approval is separate.`
Use that final root/measurement chain, not the intermediate receipt, for completion.

Saved before/after 60-source path/hash/byte records, status and index are identical;
all 60 match the saved receipt input inventory (11,152 records / 183,194,927 bytes).
The input fingerprint is
`f2e28a81fadce94cc15c0f83ca87bea9dc1227609805cae1872c5120e900d6e3`.
This exact tested checkpoint includes the Desktop correction but not later proposed
framing edits. Do not refresh its historical raw hashes to claim later source coverage.

FULL02's four canonical native calls total 211.7131335 s: version 0.4312038 s,
javaToolchains 10.8727353 s, shared Test/style 109.6108765 s, Desktop Test/style
90.7983179 s. Two additional baseline Gradle metadata invocations are visible at
raw lines 93777/93798, without per-call stopwatches: six observed production Gradle
calls, not six Test calls or an OS/JVM process count. The 968.536827 s remaining
wall is unallocated orchestration, including those untimed metadata calls.
Actionable summaries total 86 = 4 executed + 82 UP-TO-DATE, zero FROM-CACHE.
Both Test tasks EXECUTED; both Desktop Checkstyles EXECUTED and both shared
Checkstyles were UP-TO-DATE. All four hashed archived style XML files reparse with
zero errors. Shared's three and Desktop's 25 identical Test headings each represent
one task identity, not extra runs.

Unlike FULL01's environment-envelope defect, FULL02's corrected process envelope
records absent→present→absent GRADLE_OPTS, exact presence/value restoration and
envelope exit 0. The info property was inherited by the fresh child, not injected
by the measurement runner; no persistent environment mutation is claimed.

The coordinator's independently executed v2 OriginalFullShared comparator returned
exit 0, zero differences, `MATCHED_SAVED_XML_RELATION_NOT_ACCEPTANCE`: only six
specified FAILED→PASSED transitions and nine named shared additions, preserving
the original ten shared skips and 19 legitimate extra raw-display occurrences
(6,417 shared cases / 6,398 raw identities). Desktop's 1,179 cases were inspected
but have no original FULL Desktop baseline. Summary:
`inventory/candidate-junit-comparison-v2-runs/original-full-shared-to-full02-20260903-main/summary.json`,
SHA-256 `03ff7948aa57d3cc6cf65348f96fbaba743d36fabbb501b54a2bd0612417c91e`.
All six FULL02 canonical summaries also match COMPOSED01 byte-for-byte, retaining
the earlier original/R2 hash distinction. These saved comparisons are not new builds.

Detailed FULL02 command, native/style/skip ledger, all 60 tested raw source hashes
and final-versus-intermediate distinction are in
`inventory/full02-final-evidence-audit.{md,json}`. Root SHA-256:
`cf853c675af567f2c634e19b4f987bb26c1290848cf1acdc1ccc4424d13ff57a`;
measurement SHA-256:
`8c062def710f95b34f03fa6bc5d48061f5a4f79c5f25d475fe931495859d8e51`.
FULL02 did not request operational/scientific benchmark bodies, desktop launch or
packaging artifacts. It does not validate the real CI `-RunBenchmarks` route,
CleanBuild, bootstrap or later source edits. The required bounded cache/repetition
evidence is recorded above; it is not a claim that every FULL scenario or numerical
output has been repeated identically.

### Clean-output FULL and saved coverage contrast

`clean-full-applied-01` ran `verify.ps1 -Level FULL -CleanBuild` with its unique
LogDirectory, default restoration and no KeepBuildOutputs. Root UTC interval:
2026-09-02T23:29:50.2996582Z–23:59:20.9621386Z. Outer authority wall was
1770.961057 s, root 1770.661 s, separate preparation/evidence 1.2186295 s.
All native/measurement/root/environment exits are 0. The receipt sealed at
23:44:31.7554256Z remains `TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING`;
the later final root establishes completed technical assertions and restoration.

Saved source/status/index before/after match, and the same 60 raw source records
match FULL02 and the receipt inventory. Input fingerprint remains
`f2e28a81fadce94cc15c0f83ca87bea9dc1227609805cae1872c5120e900d6e3`.
The log enumerates 23 current repository-generated paths cleared (lines381–403)
and the same 23 reported restored (95009–95031). No user/dependency cache was
emptied. This audit did not independently hash restored generated-tree contents.
The process-only info-logging envelope again restored exact absent/present/absent
GRADLE_OPTS state; no persisted environment mutation is claimed.

Both unfiltered Test tasks and all four Checkstyles EXECUTED. Test-containing
Gradle calls use global `--rerun-tasks --no-build-cache --no-daemon`, one worker,
no parallelism and no configuration cache. Canonical native seconds: wrapper
0.4540565; toolchains 25.5350309; shared Test/style 254.1230959; Desktop Test/style
177.6819634; total 457.7941467. Their two actionable summaries total 86, all executed,
zero UP-TO-DATE/FROM-CACHE. Two additional baseline Gradle metadata calls occur at
raw lines 94872/94893 with no native stopwatches: six production Gradle invocations
in total, not six test/JVM launches. Remaining wall 1313.1669103 s is unallocated;
it is not an attributed snapshot or hashing cost. The initial input fingerprint
alone is measured at 10.8625028 s.

The independent saved full/clean contrast returned exit0,
`SAVED_COVERAGE_CONTEXT_AND_SUMMARIES_MATCHED_NOT_REPEAT_ACCEPTANCE`: no coverage
or recorded-context differences; all six canonical summaries byte-identical.
Each run has 7,596 occurrences, 7,577 raw identities, 19 legitimate extras, 7,585 PASS
and 11 SKIP. Exact source60/raw closure 11,152 and recorded fingerprint match. Summary:
`inventory/full-clean-contrast-runs/full02-clean01-review-20260903-aef321a6e70f45a094b8529bfb8ab1e5/summary.json`,
SHA-256 `9f5a885072259f2fa4263b930b71c7d6e9b0d08852e5689a52e2736ef27d76ee`.
The profiles intentionally differ in cleaning, restoration, daemon, cache and
rerun policy. This is stronger clean-output coverage evidence, **not** a matched
timing improvement, same-profile CandidateRepeat or author acceptance.

Compact independent artifact audit: `inventory/clean-full-final-evidence-audit.{md,json}`.
Measurement SHA-256 `adabad607212f78c4cdd06773ab658f1eac1ab0fa07e22a4576b037307538e63`;
root `2123976dde67af09812dccb02602bec04c3320f7bf795d38bd59915019fb31d7`;
receipt `6166e0f038eb90f4298a1859badc3d538ea0227a9fc6a9e5ffcbad13a38b91d8`.
The audit checks 15 saved artifact hashes and all 60 source/receipt links without
opening live source/XML or rerunning a build. The separate contrast owns the
fresh archived-XML multiset comparison. Bootstrap, benchmarks, launch and packaging
were not requested by this clean run; later source edits remain unvalidated by it.

### Reproduced operational benchmark timeout and bounded repair

The exact existing default `tools/benchmark/run.ps1 -OutputPath <unique>/benchmark.json`
ran unchanged in `after/operational-benchmark-timeout-repro-01` from
2026-09-03T00:01:02.7720528Z to 00:05:15.6042753Z. It failed with native/wrapper
exit 1 after **252.8287378 s**, no instrumentation errors and no benchmark JSON.
The raw log reaches the complete operational preflight's upstream-boundary result,
then `==> Benchmark verify-operational`, then the real error
`Benchmark command timed out after 30 seconds.` Its saved 65 selected records
(the 60 task paths plus 3 other durable diagnostic inputs and 2 instruments), status
and inherited environment agree before/after. No product failure is inferred.

Classification: stale operational execution timeout, not a failed geometric test
or the informational performance-budget threshold. Quiet suppresses printing,
not the full infrastructure/book verifier body. The default runner performs one
complete preflight, one warm-up and three measured iterations. The timeout covers
only the four warm-up/measurement child processes; the synchronous preflight has
no separate suite timeout. The first timed
child exceeded the suite's 30-second bound; a later successful local run has not
yet been claimed. Do not compute preflight time as wall minus 30: that boundary was
not separately timed.

After preserving this failure, the suite alone changed timeout 30→600 seconds;
one additive static fixture pins its unchanged script/`-Quiet`, repeat counts,
5000-ms median warning and informational mode. The benchmark runner, scope,
assertions and fail-closed finite timeout behavior are unchanged. The 600-second
bound supplies margin for the observed complete operational workload; it is not
a speedup, relaxed 5000-ms budget or proof of successful benchmark completion.
The new source checkpoint has 61 paths, distinct from tested FULL02/CLEAN source60.
Only the final local CI-profile FULL below is planned to supply successful
benchmark integration; a second standalone successful benchmark is redundant.

Measurement SHA-256:
`47dd5b7ded75b640fa1f1e31c7ed6c59a6150bb34a37167919926c22637827cd`;
raw 14,225-byte log:
`6c42406a04317cdfcfa8bb04ff2ff974520c263644746e1d4d091077a159084a`.
The original suite SHA was
`fa61ffb2c66ede3ae852c06a3683320cedd5614275dd1065bf737fa8b526cc5a`;
applied suite SHA
`6ee78a444aa6c7f9942ea0b1a18c20008f4cb75a97c3ef84282093f3d009a88e`.
The updated runtime fixture SHA is
`6b37d14a58ab6e7ecc601b45019b63e7ec332b7e0bf5d501551d155e1678ba35`;
earlier 109-case results do not validate its new case. The later focused
`after/infra-benchmark-timeout-applied-01` run did: native/wrapper 0, 110/110 runtime
cases plus 18 generated-state cases/143 assertions, no instrumentation errors;
source61/status/environment unchanged. Outer 77.6114955 s, runtime 75.078 s and
generated-state 2.126 s. Measurement SHA-256
`d59ebf539e64a9e509c549a5b2e6f9b485195d903f8d507844ab22d3935f842e`;
infrastructure result
`d54cb612b81d6e03cc9f0e148636882afa42d96abc5fbea2b3fa37fb8b517d72`,
state `PASS_FAKE_FIRST_OPERATIONAL_ONLY`. No actual successful benchmark follows
from this fixture result.

With RunBenchmarks, FULL's initial operational step plus the benchmark's preflight
and four timed iterations constitute six direct operational-verifier invocations.
The workflow's 60-minute job limit also includes checkout and provisioning; the
local measurement runner does not impose that workflow limit. A successful local
profile therefore does not guarantee a remote CI run or provisioning duration,
and the final local command is not changed to hide this distinction.

### Candidate bootstrap false rejection and focused correction

The first real candidate bootstrap, `after/bootstrap-applied-01`, failed in the
early Conda origin preflight: native/measurement-wrapper 1, bootstrap FAIL, linked
envelope 2, outer **3.9414853 s** and bootstrap **3.5148015 s**. Its selected context
(61 task paths plus three measurement instruments), status/index and environment
were preserved; the envelope reports exact environment restoration. No Gradle
compilation, nested root verification or canonical receipt was reached.

The actual named `conda run -n om_env` probe returned 0 and recorded CPython
3.12.13/mpmath 1.4.1 with coherent interpreter/prefix/import origins. Its raw
`CONDA_DEFAULT_ENV` was the registered environment's absolute prefix, which is
the installed Conda behavior outside a parent named `envs`. The candidate's
extra short-name equality rejected that valid result. This is a real candidate
preflight false rejection, not a missing environment, native Conda failure,
scientific-reference failure or Java regression. The [bootstrap audit](bootstrap_workstation_report.md)
records B18 and the exact saved source/probe evidence.

The minimal module correction accepts the exact required short name or a fully
qualified identity equal to the fully qualified normalized `CONDA_PREFIX`.
It retains named selection, version/implementation pins, `sys.prefix` equality,
strict executable/import containment, and rejection of unrelated/relative
identities. No Conda configuration, installation or numerical source was changed.
The applied focus `after/workstation-conda-prefix-applied-01` completed
native/wrapper 0 in **7.5767061 s**: 150 assertions = 108 retained + 42 additive
assertions across 16 Conda cases, followed by the decision matrix, ordering and
real temporary Git/generated-state transaction. Preparation/evidence took
1.3900558 s, separately; final summary publication is outside that wall boundary.
Its 61 selected source records, status/index/HEAD and environment are equal
before/after. This is fake-first plus the named benign filesystem/native
boundaries, not a successful real Conda/bootstrap retry.

Failure measurement SHA-256:
`ea75cea4c2ccc0edb5da0dde2c5ce7001b9541ed86618b5fb995dd869c609426`;
bootstrap result `089d9c958a7eb1330239e03e350b13622e00f9852cb7b8f45fe6f9330643eb71`;
envelope `aae92b3576b86e4d870f8816b4434f139226ec479258f3e51255d7f2b5642ee3`.
Focused measurement:
`383d25cce38d9cab3b4795f773cd95684ba6b87964b94bf8fdb3ab0dab7cc2c4`;
its 1,424-byte raw log:
`bc16b850336a1b9eca838255ee72d0dbfdddd4718dd177e12fbe6c47dc9d5e7c`.
The measured module/test hashes are respectively
`d86139f05f9da05d3fbd25a779af3a5054bd7a2cca6051a7e25325874174008e` /
`e3d6d90f02cfa4ab80ae50832ac7afcba812d5ac78f74e7b721b1c77fd4fb8c8`.
They are new bytes of two existing task paths, not new paths or retroactive
validation of earlier 108-assertion measurements. The later three guide repairs
likewise stay within the same 61-path membership; a count does not identify bytes.
The next complete real attempt failed separately as described below.

The second attempt, `after/bootstrap-applied-02`, returned native/wrapper 1 and
linked-envelope 2, with **15.4725328 s** outer / **15.1073672 s** bootstrap.
The real Conda probe passed its corrected identity checks. All 15 native commands
exited 0, including both Gradle metadata commands, ending in a full JDK17/22/25 inventory. Ordinary native blank separator lines then caused
`Cannot bind argument to parameter 'Output' because it is an empty string.`
No JDK usability or nested product gate was reached. The 64 selected records
(61 task paths plus three instruments), status/index/HEAD and environment match
before/after; required transcript/summary finalization completed.

B19 is a candidate PowerShell parameter-contract defect, not a missing JDK or
native Gradle failure. The applied fix allows empty string elements in the
three native-text parser parameters while preserving all content/version/origin
checks. Blank-only Java/Conda data still fail, and a Gradle parse with no candidates
cannot satisfy required usable JDKs. No saved output is stripped or rewritten.
The focus `after/workstation-native-padding-applied-01` passed native/wrapper 0,
**6.9984691 s** outer, 1.4128554 s separate preparation/evidence, 158 assertions
(108 original + 42 Conda + 8 padding) followed by the retained operational
matrix/order/temporary transaction. Its 61 source records/status/index/environment
match before/after and no instrumentation error is recorded. It is not the
complete real `after/bootstrap-applied-03` retry described next.

Attempt -02 measurement:
`a29a8788e45416ab2fa7e31d77638bdcb15d79f3898e509f4559608a5ea4727f`;
bootstrap result `61bea40adf64135d4579af4f5d0451b6a18f17b98a46b413d9103a016dec7877`.
B19 focus measurement:
`d5b39ef308167a9f3e2e2076221139ceff45554d89a3573c62270d1ca89b344c`;
1,550-byte raw log `7b9927572b52cb5af2a6930ee9a45b76a8ef766f7541352c03914ff8e607ffdd`.
The measured module/test hashes are
`f866f6d65a6178b1d2d872db8377efa862bd69f4627596666ac8eb053e3e1718` /
`74c8e17e51a249b985ed719169c159b6134cf90a94a51a53e7688aba516c6b97`.
Both corrections stay inside the existing 61-path set but create distinct byte
cohorts; successful older 108/150-assertion records are never reassigned to them.

### Completed bootstrap03 and preserved artifact-layer failure

The exact measured entry was `tools/bootstrap/bootstrap-windows.ps1 -SkipFetch
-LogDirectory <trial>/authority`, from the repository root, with
`<trial>=artifacts/verification-performance-bootstrap/after/bootstrap-applied-03`.
The info-logging environment was supplied by the measurement parent, as for the
original bootstrap; the canonical runner did not rewrite argv or inject it.
This was normal verification, with default generated-state restoration and no
Keep/Clean, fetch, installation, GUI, packaging artifact or optional benchmark body.

The fresh PowerShell/native authority interval was
2026-09-03T00:43:14.665922Z to 01:08:29.2206004Z: **1514.5540847 s** outer,
**1514.1860467 s** internal bootstrap and **1498.913 s** nested COMPOSED.
Separate preparation/evidence was 1.2583274 s; final measurement publication is
outside the outer stopwatch. Native bootstrap, measurement runner and nested root
all exited 0. Bootstrap finalized **PASS WITH WARNINGS**, solely SkipFetch,
with null Failure, no finalization errors and a closed transcript. These are
real completed product/workstation results, not the earlier 158-assertion focus.

All 25 recorded bootstrap-native commands returned 0: 19 preflight, five optional
packaging inspections and final status. Actual preflight confirmed JAVA_HOME's
Java 22.0.2, full usable java/javac 17.0.10 and 25.0.4, CPython 3.12.13 and mpmath 1.4.1
from the named registered `om_env` prefix. Package-tool presence is inspection,
not installation or installer execution. The 64 selected source/instrument
records (61 task paths plus three instruments), status/index/HEAD and inherited
environment match before/after. The outer envelope restored the prior absent
GRADLE_OPTS presence/value. Default restoration reported all 23 generated paths;
no independent whole-tree post-restore content hash was taken.
The recorded index digest `76b8e834a6b4866023752cdc2ce52073d29824d3996e89a5e87d462f6b739688`
is the SHA-256 of LF-joined `git ls-files --stage` output: a logical staged-entry
snapshot, not a raw-byte hash of `.git/index`. Raw source-file hashes are separate.
The [closed workstation audit](../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-final-workstation-audit.md)
and its [ledger](../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-final-workstation-ledger.json)
retain the exact native, state, environment and diagnostic observations.

The nested current-run receipt records **994 shared cases in 97 XML + 102 Desktop
cases in 17 XML = 1,096**, no failures/errors/skips. Both Test tasks executed;
all four required Checkstyle tasks were UP-TO-DATE with validated reports.
The two Test calls reported 86 actionable tasks, two executed and 84 up-to-date.
Four canonical Gradle invocations took 75.8788625 s in aggregate; two later baseline
metadata calls and two bootstrap preflight metadata calls bring the whole
production workflow to **eight Gradle calls / two Test executions**, not eight
test/JVM launches. The raw input closure is 11,152 files/183,206,478 bytes;
initial fingerprinting took 11.9209655 s. The remaining wall is not assigned
wholly to hashing or copying without separate instrumentation.

The receipt remains `TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING` on disk,
sealed at 00:53:12.1755107Z. The later nested root final result establishes
completion of the original phase/reference assertions. A separate whole-byte
comparison against FULL02 found **six of six canonical summaries identical**,
with all 12 input hashes stable, no normalization or removed fields:
[comparison ledger](../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-canonical-summary-comparison.json),
SHA-256 `b2807eac0870ffc57b6dcfcc8329b8d098b9f3ee070f3da9ccd79a5577e1c2b1`.
This is saved-summary equality, not by itself equality of full JUnit perimeters.

The r3 linked envelope nevertheless remains **exit 2, Link=null** with
`Contract was not recorded before bootstrap started.`; the outer tool session
returned 1. The [temporal diagnosis](../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-temporal-envelope-diagnosis.md)
identifies mixed representations: an in-memory UTC string cast to local DateTime
was compared to JSON-parsed UTC DateTime ticks. Explicit UTC chronology puts
contract creation 00:43:13.5210737Z **1.1448483 s before** native start, not after.
This is an artifact-instrument bug, not a product failure or late contract.
No old envelope or result is rewritten. The separate archive-only consumer then
completed with native/saved/reconciliation/tool exit 0. Its mandatory nested-result
digest is explicitly a coordinator post-run review pin, not a successful original
producer link. Source-bound delegation was reviewed as one COMPOSED call; its
PowerShell child argv was not separately observed as a native-process event.

The [archived reconciliation](../../artifacts/verification-performance-bootstrap/inventory/bootstrap-temporal-reconciliation-runs/bootstrap03-reviewed-20260903-6e8ac3ba41d6465ebd6edcf2cf0df6f1/summary.json),
SHA-256 `203aafe2150c850184b5802ffd20f7b68c6e88d0534fdf3f723b2c3beea4e06f`,
rehashed 278 inputs and matched the raw case/outcome relation: original phase 1–30
has 1,455 occurrences / 1,080 union identities; candidate has 1,096 identities,
all PASSED, zero differences and zero outcome transitions. Exactly 16 named
additions are admitted: eight shared (three pre-existing final-frontend cases
outside that original phase union plus five new GeoCeDG cases) and eight new
Desktop resource cases. They are not 16 newly implemented tests. The separate
original native union-repeat control already included the three frontend cases
(989 shared + 94 Desktop = 1,083), so these populations must not be conflated.
This is archived evidence analysis, not new execution, numerical determinism,
authenticated receipt reuse, full-perimeter equality or author approval.

| Bootstrap03 saved record | SHA-256 |
|---|---|
| Measurement | `fb44ef870872679570b8db96c77da9036591bda2efc92bb654d8d850a2359410` |
| Bootstrap result | `f8a4b6aee146c5c51c16531c3343363d4b93dc557c75e8aed167f2916d71411a` |
| Nested final COMPOSED root | `5ea14764411ff17770438c87dd017e6c0a0baf85735f3b748c77c694d2be5561` |
| Intermediate canonical receipt | `02e09dc6e5cd2dad42b370b79eecbe02cf0b0cf30058787290654c5e1a34d14a` |
| Original failed r3 envelope | `e0ed64046bf34074343aec96108aa08501e8b6db8d69515366ab5429bd739dcf` |
| Raw output, 1,223,507 bytes | `065415275a54f4365ceadeecc4fa99c8a34a87004704284d2b7ac5e56a0ffda2` |

The original normal SkipFetch/bootstrap stopwatch was 9590.394 s; the completed
candidate outer workflow took 1514.5540847 s: **8075.8399153 s lower, 84.2076%**.
This is a **context-qualified complete-workflow reduction**, not a same-source
causal Gradle speed-up or a repeat/variance estimate. Both used the info-logging
treatment and default output restoration, but source bytes, additive test scope,
preflight/diagnostic work and existing output/dependency-cache state differ.
The original measured script/stream boundary and the candidate's fresh-process/
stream boundary are disclosed; candidate preparation/evidence is excluded.
The nested 1498.913 s COMPOSED interval gets no standalone percentage. This is
neither a cold workstation nor an empty dependency cache. Original-source
matched controls remain the evidence for individual cache/process mechanisms.


### Failed final CI-profile01: operational default evidence collision

The root command (paths abbreviated here; exact argv in the measurement) was
`verify.ps1 -Level FULL -LogDirectory <trial>/authority
-RunBenchmarks -BenchmarkOutputPath <trial>/authority/operational-benchmark.json`
ran with `<trial>=after/final-full-ci-profile-01`, default restoration, no
KeepBuildOutputs/CleanBuild/IndependentBuilds and no measurement-added info or
scientific-body opt-in. It **FAILED**: native/measurement-runner/tool/root exit 1,
root state `FAILED`, no instrumentation errors. Native UTC interval was
2026-09-03T01:52:35.7752113Z–02:21:28.2646630Z; root
01:52:36.0121259Z–02:21:28.2042171Z. Outer wall 1732.4888133 s, root 1732.191 s,
separate preparation/evidence 1.2846103 s. Saved source61, status/index/branch/HEAD
and inherited parent-process environment digest agree before/after. The same
61-path membership does not make this a successful completion or a timing pair
with earlier Keep/info FULL02.

The root reached benchmark precheck after the canonical Gradle and phase/baseline/
frontend checks. That precheck invokes the complete operational verifier with
`-Quiet` and no explicit LogDirectory. The fixed default
`TEMP/geocedg-operational` reused a generated-state evidence directory.
Its fixture correctly rejected the existing `generated-state-tests.json` before
running cases, and correctly refused to overwrite it while reporting failure.
Runtime fixtures in that precheck returned 0 in 63.069 s; generated-state returned
1 in 0.399 s. The pre-existing 18-case/143-assertion PASS is dated
00:02:11.4719255Z–00:02:12.8739990Z and belongs to another run, **not final01**.
No warmup, measured samples or benchmark JSON were produced.

A separate [partial archived-JUnit/style diagnostic](../../artifacts/verification-performance-bootstrap/inventory/final-full01-failed-partial-junit-note.md)
completed its read-only inline analysis with exit 0, **not FULL PASS**:
703 archived XML hashes match the receipt; 7,596 occurrences comprise 7,585 PASS
and 11 recorded SKIP, zero failures/errors. Shared has 6,417 cases and 19 extra raw
display occurrences; Desktop has 1,179. All four archived Checkstyles parse with
zero errors and their tasks were UP-TO-DATE; both unfiltered Test tasks EXECUTED.
Four canonical native records are linked, not a count of every workflow call.
The exact duplicate/skip rows and scope are retained in the
[diagnostic ledger](../../artifacts/verification-performance-bootstrap/inventory/final-full01-failed-partial-junit-ledger.json),
SHA `79cb07201405c18fc9e08648cef2d9da9a6df18378f63f62241e15cc09899f56`;
note SHA `8471ee5052e84daa4b87f516e06bccd9aee1f546f112aa69edcfa1927e709f91`.
It does not establish cross-run source/case equality, benchmark success or a new
skip-acceptance decision. Root/native/runner remain1; no live XML or source closure
was revalidated by that diagnostic.

This is a candidate operational orchestration defect, not Java, geometry,
numerical-reference, Conda, ACL, native-exit capture or the 600-second timeout.
Deleting the old report, treating it as current or weakening no-overwrite
handling is rejected. Even initially empty TEMP would collide at the warmup
after a successful precheck. Five relevant TEMP files were copied without
deletion/overwrite to
`after/final-full-ci-profile-01/failed-benchmark-precheck/`; the
[archive ledger](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/failed-benchmark-precheck/archive-ledger.json)
records original/archive bytes and unchanged source hashes.

The [bounded design](../../artifacts/verification-performance-bootstrap/inventory/final01-benchmark-evidence-collision-design.md)
changes only the operational default to `TEMP/geocedg-operational/<new GUID>`
per invocation and adds one runtime regression case. An explicit LogDirectory
still binds unchanged; existing evidence still fails closed. The regression binds the actual operational ParamBlock to check five distinct
implicit paths and unchanged explicit binding. Separately it invokes the actual
publication-guard AST from the generated-state fixture against owned sentinel
paths, checking no-overwrite and byte preservation. The publication guard is
not attributed to the ParamBlock; neither operational nor generated-state/helper
execution bodies run in this bounded case.
**FOCUSED_DEFAULT_LOG_FIX_RESULT — COMPLETED**, at
`after/infra-operational-log-isolation-applied-01`: the actual focused wrapper
returned native/measurement-runner/tool 0, state `PASS_FAKE_FIRST_OPERATIONAL_ONLY`,
no instrumentation errors. Runtime 111/111 passed in 75.914 s, including exactly
`operational defaults isolate five invocations and preserve explicit evidence`
(PASS, 0.1853418 s). Generated-state 18 cases/143 assertions passed in 1.995 s,
cleanup_errors empty. Outer 78.3426822 s, separate preparation/evidence 1.3400734 s.
The command was `verify-verification-infrastructure.ps1 -LogDirectory
<trial>/authority` via fresh pwsh -NoProfile; it executed no Gradle/product body.
Earlier110-case results do not validate this added case; this is its separate run.

Saved before/after 61 sources, status/index/branch/HEAD and the parent-process
environment digest match **within this focused run**. Its environment digest
`6d0da6734332a60fb67ea695f28019059691c88aba5a68ae00efb1433d1c513d`
differs from final01's `6fd54153498c06f5329d8967773b5aeabadbb1cf21da86985d260e5fb126cb45`;
no cross-launch environment equality or live-child snapshot is claimed.
Measured operational SHA
`25125bd1a021ba797c0a8e83302adb328a7942a2c40dcf146d0f91eac613bca2`;
runtime-fixture SHA
`8123b48f28a4446b3e5aa8dc27ea7b8548f58161e6b03d9d8087e5f1a34bf241`.
No successful benchmark or FULL follows from this focus. Neither benchmark runner, its
complete scope/1+3 iterations, 600-second bound/5000-ms informational budget,
generated-state machinery nor Java/scientific contracts are weakened.

Normal bootstrap supplies an explicit canonical log tree, so this default-only
change requires no new bootstrap behavior or arbitrary extra bootstrap run.
Bootstrap03 stays evidence of its earlier source cohort. Relative to bootstrap03,
the replacement final02 checkpoint changes **seven paths**: the same five
documentary files plus operational entrypoint and runtime fixture; membership
remains 61, with no new Appendix K path. Earlier FULL02/CLEAN source60 and final01
source61 records remain immutable.

| Failed final01 saved evidence | SHA-256 |
|---|---|
| [Measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/measurement-result.json) | `e33c7a9ee14a4966b57990a9ee3811934bfccd83fa914645884c5893e2b55b07` |
| [Failed root](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/authority/verification-result.json) | `5bb7b0fbb25f2717e5315a0d48b6b34e6a1ccc6d86e67a0dd7947b5dc3f6979d` |
| Raw output, 8,308,043 bytes | `42d68e81aa25222c282b8029424938bf33029dc1bfa4a77297dd1d5d4f224ffd` |
| Intermediate canonical receipt | `999483e51b5503350e6c47421a10b0402b2c310a3a44fa2f95fe101c5873f5b8` |
| Five-file TEMP archive ledger | `f103e3f327f8ed71ded53aa29d15d2977e0a253573c993c14c184d3cf1834e83` |
| Bounded correction design | `0243a1e8e916fb92f46863c6502b89775ae7d0429558348f3973461804767e6d` |

| Applied default-log focused record | SHA-256 |
|---|---|
| [Measurement](../../artifacts/verification-performance-bootstrap/after/infra-operational-log-isolation-applied-01/measurement-result.json) | `0c9c1a34d9cf75db4e251cccdb26f1b43f3c5d5e31bb294c2941b43ae83f5ed9` |
| [Infrastructure result](../../artifacts/verification-performance-bootstrap/after/infra-operational-log-isolation-applied-01/authority/verification-infrastructure.json) | `87bfa8926c8ee4f5d78b3293b811952b533d1cbef7e774a8db32bc027ae3abf2` |
| Runtime 111 result | `3ccddff39238bec1aafa99f03f16b3f482b80d7267af5d9942a5e9590952f8ad` |
| Generated-state 18/143 result | `befc7c5efb80b69d0c9b2f44abaaac59eb7f9db81bb6aee0c8bd509a5b880cc5` |
| Raw focused output | `263db71de534e170d42016ad07a56c5a1d72a38f6b6d9b6e3a6abcfc4c3a534e` |

The success-only final01 auditor version identified by `21ce` has **not** been
executed against this real failed run and supplies no result authority. Its
synthetic/SelfTest evidence is not a real-run audit. Use the saved measurement,
failed root and archived diagnostics above; never report PASS from the receipt.
Replacement final02 is reserved below and remains pending.

### 8.3 Reserved finalized-result slots

These slots do not claim a pending or provisional result. Populate them only from
completed saved evidence, with exact source state and requested optional gates.

| Gate / comparison | Historical checkpoint status and required completion record |
|---|---|
| BOOTSTRAP_NORMAL_RESULT | Attempts -01/-02 remain **FAILED AS OBSERVED**, B18/B19. Bootstrap03 actual normal path and nested COMPOSED completed native/runner/root 0, 1,096 tests/zero skips, only SkipFetch warning; original envelope 2 is preserved separately. Separate archive-only UTC/link/raw-case reconciliation completed with exit 0, retaining the failed envelope and explicit post-run pin. No fetch/install/GUI or arbitrary extra successful-bootstrap repetition is claimed. |
| CLEAN_FULL_RESULT | **COMPLETED**: `after/clean-full-applied-01`, final root/measurement/environment exit0, same source60 and 7,596 cases; detailed profile, restoration and coverage contrast above. It does not establish a pristine workstation or cleared dependency cache. |
| Repetition evidence / limits | DEV49 exact first/repeat, original union repeat, phase→COMPOSED01, original-failed-shared→FULL02 and FULL02-to-clean saved coverage/context/summary relations are complete. The clean/reuse profiles intentionally differ; this is not CandidateRepeat, a timing A/B, universal numerical determinism or stress-tested parallelism. |
| BENCHMARK_REPRO_AND_FOCUSED_REPAIR | Reproduction **FAILED AS OBSERVED** before repair: native/wrapper1, timeout 30, no benchmark JSON, no instrumentation error. Finite timeout 600 and one static-contract fixture are applied; focused runtime 110/110 and generated-state 18/143 pass on source61. The attempted final01 delegation failed during precheck on a separate fixed-path evidence collision; its root remains FAILED. The applied default-log correction passed 111 runtime cases and 18 generated-state cases/143 assertions; successful replacement delegation remains **PENDING at this documentary checkpoint**. Full quiet scope,1 warm-up/3 measurements and informational 5000 ms budget are unchanged. |
| FINAL_FULL_CI_PROFILE_RESULT | Attempt01 **FAILED** with exit 1 at benchmark precheck and no benchmark JSON; replacement attempt02 **PENDING**: `after/final-full-ci-profile-02`, exact command below. One successful final FULL delegates the exact existing benchmark runner and records all three benchmark measurements/JSON; no redundant standalone successful benchmark run is required. This local execution is not the remote CI workflow. |
| Final source, optional bodies and A–M closeout | Incorporate final whitespace/status/index/generated-state and all-path scope evidence after the final source/prose updates. State requested/executed/not requested launch, packaging and scientific bodies; retain fake-first labels, source-version boundaries and no author approval. |

Reserved replacement final02 local CI-profile command (not yet executed):

```powershell
.\tools\agent\verify.ps1 -Level FULL `
  -LogDirectory <trial>\authority -RunBenchmarks `
  -BenchmarkOutputPath <trial>\authority\operational-benchmark.json
```

Here `<trial>` is `artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02`.
Use default generated-state restoration/no-daemon behavior, no KeepBuildOutputs
and no measurement-added GRADLE_OPTS. This is functionally the workflow's FULL
plus benchmark delegation; the default-path correction must isolate implicit
operational precheck/warmup/measurement directories, not only the root's explicit
evidence path. Its output/environment/
daemon profile differs from FULL02's retained-output/info run, so it is not a
matched FULL02 performance pair. Verify final root completion and benchmark JSON,
not merely the benchmark child exit or an intermediate canonical receipt.

### Historical documentary checkpoint and final execution record

The document version retained in reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66` was frozen before replacement
exact-source FULL attempt `final-full-ci-profile-02`.
Its original pending language records that checkpoint, not the current closeout:
final02 subsequently passed. Attempt `final-full-ci-profile-01` remains
FAILED at the delegated benchmark precheck; an intermediate receipt does not
replace its failed root. Later completion is recorded in the designated
[measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/measurement-result.json),
[final root result](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/verification-result.json),
[delegated benchmark JSON](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-02/authority/operational-benchmark.json)
and [final candidate closeout](../../artifacts/verification-performance-bootstrap/inventory/final-candidate-closeout.md),
as separate saved execution evidence. These status-only closeout edits do not
alter executable inputs or those records. A missing or failed record
does not satisfy the gate. These ignored local artifacts require the retained
artifact tree or a separately supplied bundle; they do not confer author approval.

## 9. Governance and closeout boundary

The candidate centralizes level/perimeter and bootstrap-impact rules in one
specification, referenced by AGENTS, canonical verification/task/review prompts
and developer guidance. CI explicitly selects FULL and retains its existing
benchmark/evidence steps. Reliable wiring/fixture checks are not a keyword-based
claim that a substantive impact review occurred.

Applied impact declarations: `BOOTSTRAP IMPACT — UPDATED`; `GUIDE_IMPACT = UPDATED`.
Bootstrap/setup and their shared prerequisite modules now express the audited
PowerShell, Java and numerical-runtime requirements, early context checks, explicit
installation boundary, stage/native failures and unique-run logging. README and
user/developer guides describe those prerequisites, level scope and FULL obligations.
Exact affected paths appear in Appendix K; the bootstrap audit owns the completed
mode-by-mode evidence and remaining bootstrap/installation limits. The final guide
review also replaced stale README capability claims with living roadmap/manual
links, qualified user-guide acceptance by the required COMPOSED/FULL level and
human review, and distinguished the verification interface from its separately
justified Java correctness prerequisites in developer guidance. These are edits
within the existing 61 paths, not additional phase authorization.

The root instructions, canonical verification prompt, task template and review
prompt require substantive bootstrap and verification-infrastructure impact review,
not an automatic script edit or a keyword-only acceptance. ADR 0020 is now Accepted
and the level specification is normative through the separate
[author decision](../adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout).
That record also owns the status-only closeout impact declaration; the UPDATED
declarations above describe implementation changes. Technical results and impact
declarations alone do not grant phase authorization, release permission or a tag.

## 10. Historical pre-final02 closeout obligations

This checkpoint checklist is preserved from the reviewed implementation commit;
the completed outcome and author closeout at the top of this report supersede
its pending status without relabelling failed evidence.

Source application, the separately designed shared/Desktop correctness repairs,
their focused checks, DEV first/repeat, R6 PHASE, COMPOSED01 and the saved original
union-repeat/phase-to-COMPOSED01 comparisons are complete. FULL02 completed technical
gates for the repaired 60-source checkpoint; its bounded original-shared comparison,
six canonical-summary matches and the separately archived DEV49 repetition are
complete. Clean-output FULL and its exact saved coverage/context/summary contrast
with FULL02 are also complete. Original and candidate FULL01 failures remain
preserved, not pending repairs or PASS. The benchmark timeout reproduction and
bounded timeout repair/focus are complete. Final CI-profile01 then failed at
benchmark precheck on fixed-default evidence-directory reuse; that failure,
its unchanged source/context and archived stale result remain preserved.
The applied default-path correction/focused validation passed 111 runtime cases
and 18 generated-state cases/143 assertions. Successful actual delegated benchmark/
final02 evidence remains open. The two failed bootstraps and their applied B18/B19
corrections with successive 150/158-assertion focuses remain preserved.
Bootstrap03 completed the actual normal path and nested COMPOSED; its r3 envelope
failed instrumentally and is not rewritten. The archive-only UTC/link/raw-case relation completed separately with zero
differences, not by inferring completion from the intermediate receipt. Final-source CI-profile
FULL and optional-body reporting are explicit in section 8.3. Complete them from
saved results, not estimates or a relabeled earlier scope. Evidence remains
local/ignored unless archived separately.
At this documentary checkpoint the remaining final-run outcome is deliberately
not anticipated; use the designated exact-source records and final candidate
closeout above for its eventual status. A missing or failed required record is
not completion. A pre-existing stale product-state paragraph in
`docs/developer/geocedg_agent_prompt_guide.md` was identified separately and remains
deferred outside this task's 61-path set; current product authority is the living
roadmap/manual, not that historical wording. No frozen record is rewritten to
resolve it. The then-required status was
`IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW`, superseded by the explicit
author decision in ADR 0020, not by a test result.

## Appendix K. Every intentional changed path

Point-in-time inventory at 2026-09-03T00:07:43.0493287Z, not a final clean-worktree
or validation claim: 61 paths = 46 tracked worktree modifications + 15 new files;
no staged entries. Actual-role grouping: 38 infrastructure + 13 governance/docs
+ 10 Java compatibility. The Java group has six modified upstream-origin files
(two production, four tests) and four GeoCeDG-owned additions (one adapter, three
tests). Inherited directory location does not make an `org.geocedg.*` addition
upstream-origin. The existing owned SVG asset is unchanged.

The original 57- and later 60-path snapshots are preserved. Final01 preserves
its failed 61-path snapshot; the replacement final02 changes only existing
operational/runtime-fixture paths and the five documents, seven relative to
bootstrap03, with no added path or reclassification. ImageManagerD, the closed
resource adapter and its test added three paths to 57; the applied benchmark suite
change now adds the 61st. Existing runtime-fixture edits add no path. The living
upstream-modification inventory is documentation, not another Java file.
Ignored artifacts/caches are excluded. Recheck final membership/index and raw
hashes after all work. The 60-path classification remains historical evidence;
the new row and this timestamped 61-path membership are recorded in the closeout
patch manifest.

`M` = tracked worktree modification; `NEW` = intended new source/durable file.
Origin describes source provenance, not licensing clearance or release approval.

| Path | Change | Group / category | Role | Origin |
|---|---|---|---|---|
| `.github/prompts/canonical/verification.prompt.md` | M | Governance/docs / GOV | Canonical verification prompt references level/impact authority | GeoCeDG operational/durable source |
| `.github/prompts/reviews/change-review.prompt.md` | M | Governance/docs / GOV | Review prompt requires substantive infrastructure/level evidence | GeoCeDG operational/durable source |
| `.github/prompts/tasks/task-template.prompt.md` | M | Governance/docs / GOV | Task template records level and impact declarations | GeoCeDG operational/durable source |
| `.github/workflows/verify.yml` | M | Infrastructure / CI | GitHub Actions FULL authority, benchmark and evidence upload | GeoCeDG operational/durable source |
| `AGENTS.md` | M | Governance/docs / GOV | Root impact-review and validation obligations | GeoCeDG operational/durable source |
| `docs/adr/0020-verification-levels-and-current-run-evidence.md` | NEW | Governance/docs / GOV | Verification-level/current-run-evidence design decision | GeoCeDG operational/durable source |
| `docs/architecture/verification-baseline-compatibility-repair.md` | NEW | Governance/docs / COMPAT_DOC | Separate shared and Desktop FULL-failure diagnoses, bounded correctness designs, preserved contracts and validation requirements | GeoCeDG operational/durable source |
| `docs/developer/geocedg_developer_guide.md` | M | Governance/docs / GUIDE | Build/verification levels, toolchains and governance guidance | GeoCeDG operational/durable source |
| `docs/upstream/modified-files.yml` | M | Governance/docs / PROVENANCE | Living inventory of six existing upstream-origin Java edits and four new GeoCeDG Java additions; not Java source or frozen approval data | GeoCeDG operational/durable source |
| `docs/user/geocedg_user_guide.md` | M | Governance/docs / GUIDE | Supported workstation, bootstrap and optional packaging guidance | GeoCeDG operational/durable source |
| `docs/validation/bootstrap_workstation_report.md` | NEW | Governance/docs / REPORT | Bootstrap/workstation audit, evidence and limitations | GeoCeDG operational/durable source |
| `docs/validation/verification_performance_report.md` | NEW | Governance/docs / REPORT | Verification performance evidence, comparisons and limitations | GeoCeDG operational/durable source |
| `geocedg/specs/operations/verification-levels.md` | NEW | Governance/docs / GOV | Operational level, evidence and impact-review specification | GeoCeDG operational/durable source |
| `README.md` | M | Governance/docs / GUIDE | Windows prerequisites, delegation and diagnostics guidance | GeoCeDG operational/durable source |
| `benchmarks/suites/operational-smoke.yml` | M | Infrastructure / BENCHMARK_SUITE | Finite full-operational benchmark timeout; full quiet scope, repeats and informational budget preserved | GeoCeDG existing operational source |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/resources/GeoCeDGToolImageResource.java` | NEW | Java compatibility / GEOCEDG_PRODUCTION | Closed adapter renders the existing owned SVG into fresh loaded 64-pixel Toolkit images; no new artwork or generic SVG loader | New GeoCeDG-owned Java |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/util/ImageManagerD.java` | M | Java compatibility / UPSTREAM_PRODUCTION | Closed four-mode resource selection and typed GeoCeDG raster-loading seams; other PNG paths unchanged | Existing upstream-origin Java |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGToolImageResourceTest.java` | NEW | Java compatibility / GEOCEDG_TEST | Eight real Desktop raster, 2D/3D, border/cache, DPI, legacy-path and ToolImage regression tests | New GeoCeDG-owned Java |
| `source/shared/common-jre/src/test/java/org/geocedg/common/kernel/commands/GeoCeDGCommandsTest.java` | NEW | Java compatibility / GEOCEDG_TEST | Executable LocusV2/LocusLength/SplineV2 command coverage; not upstream-origin | New GeoCeDG-owned Java |
| `source/shared/common-jre/src/test/java/org/geocedg/common/spatial/LegacyCollectedRedefineCompatibilityTest.java` | NEW | Java compatibility / GEOCEDG_TEST | Ordinary redefinition/no-spatial-identity regression and empty completion rejection | New GeoCeDG-owned Java |
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/CommandDispatcherTest.java` | M | Java compatibility / UPSTREAM_TEST | Separate Classic-OFF and opted-in command registration coverage | Existing upstream-origin Java |
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/CommandFilterTest.java` | M | Java compatibility / UPSTREAM_TEST | Preserve no-CAS checks in OFF/ON contexts without command exclusions | Existing upstream-origin Java |
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/CommandsValidationTest.java` | M | Java compatibility / UPSTREAM_TEST | OFF feature rejection plus ON argument-count/type validation | Existing upstream-origin Java |
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/SelfTest.java` | M | Java compatibility / UPSTREAM_TEST | Discover executable GeoCeDG command tests in reflective coverage inventory | Existing upstream-origin Java |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java` | M | Java compatibility / UPSTREAM_PRODUCTION | Ordinary collected-redefinition completion guard; compatibility prerequisite | Existing upstream-origin Java |
| `tools/agent/repository-generated-state.ps1` | M | Infrastructure / OP_HELPER | Generated-output ownership, snapshot/restore/retention, containment and recovery | GeoCeDG operational/durable source |
| `tools/agent/tests/generated-state.tests.ps1` | NEW | Infrastructure / OP_TEST | Generated-state safety/retention/recovery fixtures; mocked enumeration and owned filesystem work | GeoCeDG operational/durable source |
| `tools/agent/tests/verification-runtime.Tests.ps1` | NEW | Infrastructure / OP_TEST | Runtime/evidence/cache/selection/native-boundary fixtures in isolated repositories | GeoCeDG operational/durable source |
| `tools/agent/verification-runtime.psm1` | NEW | Infrastructure / BUILD_VERIFY_HELPER | Current-run Gradle execution/evidence, input identity and incremental/cache policy | GeoCeDG operational/durable source |
| `tools/agent/verify-baseline.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Baseline metadata/build authority; current-run consumption and independent FULL freshness/failure checks | GeoCeDG operational/durable source |
| `tools/agent/verify-dxf.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-frontend.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Frontend/profile regression authority with current-run consumption; existing final-frontend assertions retained | GeoCeDG operational/durable source |
| `tools/agent/verify-g7a-metrics.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g7b-metrics.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g8a-intersections.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g8b-intersections.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g8c-intersections-design.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g8c1-intersections.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g8c2-intersections.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9a1-spatial-identity.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9a2-spatial-point.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9a3-spatial-lifecycle.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9s1-semantic-spline-2d-capability.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-locus-v2-public-surface.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r2-product-refinement.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r3-public-locus-ui-hardening.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r4-intersection-admissibility-continuation.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r5-locus-v2-similarity-transformations.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-g9x1-extended-dxf.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-locus-v2.ps1` | M | Infrastructure / CONSUMER_VERIFIER | Existing named verifier integrated with current-run/incremental evidence; assertions remain source-owned | GeoCeDG operational/durable source |
| `tools/agent/verify-operational.ps1` | M | Infrastructure / OP_VERIFIER | Operational repository/governance contracts and evidence routing | GeoCeDG operational/durable source |
| `tools/agent/verify-verification-infrastructure.ps1` | NEW | Infrastructure / OP_VERIFIER | Isolated runtime/generated-state PowerShell fixture wrapper | GeoCeDG operational/durable source |
| `tools/agent/verify-workstation.ps1` | M | Infrastructure / OP_VERIFIER | Workstation/packaging/bootstrap fixtures and retained real temporary Git transaction | GeoCeDG operational/durable source |
| `tools/agent/verify.ps1` | M | Infrastructure / ROOT_VERIFIER | DEV/PHASE/COMPOSED/FULL routing, transactions and delegation | GeoCeDG operational/durable source |
| `tools/bootstrap/bootstrap-windows.ps1` | M | Infrastructure / BOOTSTRAP | Normal workstation/provenance preflight and canonical delegation; separate explicit install route | GeoCeDG operational/durable source |
| `tools/bootstrap/install-packaging-prerequisites.ps1` | M | Infrastructure / BOOTSTRAP | Inspect-only by default; explicitly authorized packaging installation boundary | GeoCeDG operational/durable source |
| `tools/bootstrap/packaging-prerequisites.psm1` | M | Infrastructure / OP_HELPER | Packaging inventory, pinning and installation planning | GeoCeDG operational/durable source |
| `tools/bootstrap/tests/workstation-prerequisites.tests.ps1` | NEW | Infrastructure / OP_TEST | Fake workstation/packaging and benign native/diagnostic boundary fixtures | GeoCeDG operational/durable source |
| `tools/bootstrap/workstation-prerequisites.psm1` | NEW | Infrastructure / OP_HELPER | Source-derived Java/Conda prerequisites, identity/origin and native-probe parsing | GeoCeDG operational/durable source |

Historical phase records, approved source/hash locks and tags are not refreshed by
this inventory. The point-in-time count is not a substitute for final repository
scope validation or author review.

# Bootstrap and workstation audit report

Status: **Closed** — [author decision in ADR 0020][approval]. Native bootstrap03 and nested COMPOSED passed; the separate r3 envelope remains failed. Final02 and independent closeout review subsequently passed.

Evidence update: 2026-09-03; cited completed measurements span 2026-09-02–03 UTC. Individual saved records retain their exact UTC timestamps.

Layer: GeoCeDG-owned operational/bootstrap/build-verification infrastructure

Inspected original HEAD: 3942af594e4507e479f2c75019cef62e3d9fea6f

Task branch: codex/verification-performance-bootstrap-governance

## Current operational closeout

The author accepted reviewed implementation
`2b82034dbedf6f26250ad4aefb9eead700e33e66` on 2026-09-03; [ADR 0020][approval]
is the normative approval record. This status-only closeout changes no bootstrap,
workstation, Java, build or verification behavior and needs no repeated native
bootstrap. The reviewed source-cohort boundaries remain explicit: native
bootstrap03/nested COMPOSED exit 0 and the separate timestamp-link envelope exit 2
are different results. The archived reconciliation preserves the failed envelope.
Final02 FULL and the independent review complete the formerly pending gate.

JDK 22 launcher, build/test JDK 17, Desktop JDK 25, Gradle-wrapper authority and
the documented optional packaging support remain unchanged. Actual installation,
installer generation, interactive GUI and remote CI are not newly claimed.
The approval-only bootstrap/guide impact is recorded in ADR 0020; the UPDATED
declarations below belong to implementation, not to this metadata closeout.

### Historical implementation checkpoint

The following pre-final02 chronology is preserved from the reviewed commit. Its
pending wording and pre-approval status describe that checkpoint only.

This report records an applied implementation candidate, not a completed validation or approval record. The unchanged original bootstrap and its COMPOSED delegation completed successfully. Original FULL subsequently failed in the unfiltered shared-JRE suite before unfiltered Desktop ran; one spreadsheet failure also reproduced in isolation. Those are original baseline failures, not failures of the successful original bootstrap scope. The coordinator has since applied the candidate operational sources. Saved applied infrastructure/workstation checks and a real inspect-only packaging run are reported below. Representative DEV49 first/repeat, PHASE, COMPOSED01 and technical FULL02 have completed against their recorded checkpoints; the bounded DEV49 and original-shared-to-FULL02 relations are also complete. Clean-output FULL subsequently completed with the same source60 and saved coverage/context plus six identical canonical summaries as FULL02, under explicitly different clean/reuse policies. The first real candidate bootstrap falsely rejected a legitimate Conda display identity; its origin-preserving correction passed a 150-assertion workstation focus. The second attempt accepted that identity but failed when the Gradle parser's parameter rejected native blank lines. Both failures are retained below; neither reached nested product verification. The separate blank-line correction then passed its 158-assertion workstation focus. The corrected bootstrap03 normal SkipFetch path and nested COMPOSED then completed with native/runner/root exit 0. Its separate r3 linked envelope failed because of mixed local/UTC DateTime comparison; that failure and null Link are preserved, with artifact-only reconciliation separately reviewed. Final CI-profile01 subsequently failed at benchmark precheck because implicit operational invocations shared a fixed evidence directory. That failed root and prior generated-state result remain preserved. The applied default-only unique-directory correction passed 111 runtime cases and 18 generated-state cases/143 assertions; replacement final02 remains pending at this documentary checkpoint.

The bootstrap/workstation changes do not introduce geometric semantics, numerical tolerances, serialization changes, scientific-reference updates, feature promotion or licensing decisions. Separate baseline-compatibility changes and product-level validation must not be concealed by this operational scope statement. At this historical checkpoint the status was IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW; focused checks alone did not complete missing required gates. The later explicit author decision is recorded separately above.

## 1. Authority, scope and evidence levels

Current code, tests, build configuration and executable verifiers govern this report. Existing accepted specifications/ADRs and frozen evidence remain authoritative over generated notes. The entry-state check was completed by the coordinating task before proposal preparation; this report does not independently re-certify remote publication or grant approval to the latest product phase.

Inspection covered:

- bootstrap-windows.ps1, install-packaging-prerequisites.ps1 and packaging-prerequisites.psm1;
- verify.ps1 and the invoked verifier graph, especially baseline/workstation/operational, numerical-reference, laboratory, packaging and generated-state boundaries;
- Gradle wrapper, Java conventions, Desktop launcher/resource generation and relevant shared build inputs;
- current CI, package profile/NuGet configuration, canonical numerical generators and onboarding guidance;
- the applied bootstrap/workstation corrections, their fake-first tests, retained failed measurements and parent-owned generated-state integration.

Root AGENTS.md and applicable canonical operational/governance/verification prompts were read. The expected .github/copilot-instructions.md was absent during inspection. No additional external book, web account, Node installation or packaging tool was assumed solely from unrelated repository content.

Evidence labels used below:

| Label | Meaning |
| --- | --- |
| Source fact | Directly inspected original source/configuration at the HEAD above, or explicitly identified applied candidate source |
| Observed host | Saved log or explicitly attributed coordinating-task observation |
| Inference | A conclusion from a successful check and the check's current source predicates |
| Candidate/static | Reviewed candidate behavior not demonstrated by the cited execution |
| Applied fake-first | Saved operational fixtures against identified applied-source hashes; not product/scientific or installation evidence |
| Applied host inspection | Saved real installed-tool inventory with no installation action; not setup/idempotence or packaging-artifact evidence |
| Pending | Required result unavailable; neither PASS nor estimated improvement |

The detailed [execution matrix][matrix] and [audit working notes][audit-notes] support this report but are generated evidence, not a second verification authority.

## 2. Original bootstrap execution graph

The original [bootstrap entrypoint][original-bootstrap] has SkipFetch, SkipBuild, RunBenchmarks, LaunchDesktop and InstallPackagingPrerequisites. It has neither a verification-level selector nor a caller-selected LogDirectory.

~~~text
bootstrap-windows.ps1
  |
  +-- InstallPackagingPrerequisites
  |     -> install-packaging-prerequisites.ps1 -Install
  |     -> return before Git/build/product verification
  |
  +-- normal onboarding/verification
        -> reject SkipBuild + LaunchDesktop
        -> require PowerShell 7+, Git, pwsh and PATH Java 22
        -> validate repository root/markers and original status
        -> inspect origin; add exact upstream only when absent
        -> fetch origin/upstream unless SkipFetch
        -> verify annotated upstream baseline tag and recorded SHA
        -> tools/agent/verify.ps1, current composed authority
        -> consume baseline Gradle/version/toolchain logs
        -> detect Desktop Java 25, after product verification
        -> optional jpackage/.NET/WiX/extension inventory
        -> compare final repository status
        -> PASS / PASS WITH WARNINGS / FAIL
~~~

Normal bootstrap does not install software or open a GUI. The independent installation branch occurs at original line 182; verification delegation at line 319; optional packaging begins at line 357. Origin mismatch warns without rewriting origin. An existing upstream must match the expected official HTTPS URL. Missing upstream may be added. SkipFetch suppresses fetch only: it is not offline mode.

The pinned historical baseline remains 9b93256b7df401ff056c37b502d82df4d72b1522, annotated tag geogebra-baseline-5.4.928.0. These historical constants are distinct from the current repository HEAD and were not demonstrated stale.

### 2.1 Product delegation and indirect checks

The inspected verify.ps1 reaches the current G9U0-R6 gate. Bootstrap delegates that graph; it does not independently hard-code a latest-phase list. There is no source evidence that bootstrap omits R6 simply because its own prerequisite list is older.

| Group | Original composed entrypoints |
| --- | --- |
| Operational foundations | verify-operational.ps1; verify-workstation.ps1; verify-legacy.ps1 |
| Initial export/locus | verify-dxf.ps1; verify-locus-v2.ps1 |
| G7 metrics | verify-g7a-metrics.ps1; verify-g7b-metrics.ps1 |
| G8 intersections | verify-g8a-intersections.ps1; verify-g8b-intersections.ps1; verify-g8c-intersections-design.ps1; verify-g8c1-intersections.ps1; verify-g8c2-intersections.ps1, or its contract-only branch when implementation evidence is absent |
| Knowledge/spatial | verify-g9p-design.ps1; verify-knowledge-bundles.ps1; verify-g9a1-spatial-identity.ps1; verify-g9a2-spatial-point.ps1; verify-g9a3-spatial-lifecycle.ps1 |
| Public locus/export | verify-g9u0-locus-v2-public-surface.ps1; verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1; verify-g9x1-extended-dxf.ps1 |
| Refinement | verify-g9u0-r2-product-refinement.ps1; verify-g9u0-r3-public-locus-ui-hardening.ps1; verify-g9u0-r4-intersection-admissibility-continuation.ps1; verify-g9u0-r5-locus-v2-similarity-transformations.ps1 |
| Current spline/interaction | verify-g9s1-semantic-spline-2d-capability.ps1; verify-g9u0-r6-semantic-locus-point-interaction-support.ps1 |
| Remaining gates | verify-packaging.ps1; verify-baseline.ps1; verify-frontend.ps1; optional operational benchmark; final Git checks |

[Original verify.ps1][original-verify] supplies AlreadyComposed to the relevant later verifiers to suppress supported historical nesting, not their own assertions. Its per-verifier task/filter/non-test/evidence detail is preserved in the execution matrix.

Indirect dependencies matter to workstation readiness:

- Operational verification executes repository-state and disposable book-operation fixtures. The external author's real book is deliberately not required.
- Workstation verification tests packaging planning/order and generated-state behavior. Its original PASS is a fixture/contract result, not proof that the host has every current tool.
- Legacy/laboratory validation uses Check or ValidateOnly paths. Static mentions of runLocusV2Laboratory do not establish a normal interactive launch.
- Knowledge-bundle fixtures run in disposable repositories and exercise the builder/verifier contract; they require no external account.
- G7A/G8A/G8C numerical branches use conda run -n om_env python and canonical generators in --check mode. G9A2 uses Decimal-only Python generators through the same named environment; it is not itself an mpmath computation.
- Baseline always inspects PATH java, wrapper version and javaToolchains, even under SkipBuild. Normal compilation covers shared canvas/renderer and Desktop. FullTests adds unfiltered shared-JRE and Desktop tests. LaunchDesktop remains an explicit upstream Classic smoke test.
- Default packaging verification checks source/profile/association/license contracts, not a real native installer unless the applicable artifact option is requested.
- JavaCC parser outputs and Desktop provenance/profile resources are generated by canonical Gradle dependencies. They are not manually editable source authorities.
- The optional operational benchmark repeats operational contracts, including disposable fixtures. It is informational, not a substitute for scientific tests.

This is the repository-controlled execution/dependency graph, not an exhaustive inventory of all downloaded third-party Maven/Gradle/NuGet artifacts.

## 3. Original requirements versus observed workstation

### 3.1 Source-required contract

| Component | Current source requirement | Original bootstrap coverage |
| --- | --- | --- |
| Windows and PowerShell | Windows entrypoint; original guard checks PowerShell major >=7; Git and pwsh must resolve | Early check |
| Gradle launcher | Validated launcher profile Java 22; batch wrapper gives nonempty JAVA_HOME precedence over PATH | Checks PATH Java instead of effective wrapper selection |
| Compiler | Full JDK 17 from [Java convention][java-convention] | Not an explicit early prerequisite |
| Desktop runtime | JDK 25 from [Desktop launcher][desktop-launcher] | Detected only after composed verification |
| Gradle | Repository wrapper pinned to 9.4.1 | Delegated baseline inventory; no global Gradle required |
| Numerical environment | Named om_env; CPython 3.12.13 and mpmath 1.4.1 in current G7A/G8A/G8C generators | No early environment/version/import-origin check |
| Generated sources/resources | JavaCC and repository-owned Gradle resource/provenance tasks | Implicit build dependency |
| Native packaging, optional | jpackage 25; compatible .NET SDK 6+; WiX 5.0.2 and matching Util/UI extensions | Late optional inventory |
| Focused installer action | Explicit missing .NET 8 SDK via WinGet, pinned WiX/extensions; never install a JDK | Separate explicit installation branch |

The [G7A generator][g7a-generator] owns EXPECTED_PYTHON and EXPECTED_MPMATH and checks them before its reference comparison. The candidate derives agreed requirements from G7A/G8A/G8C generators/verifiers instead of introducing an independent version authority. Scientific generator/reference files and hashes remain untouched.

The candidate narrows the current operational floor to PowerShell **7.2+**. The original major-version guard remains an original-source fact, not the new support promise. This avoids rewriting inherited native captures: Microsoft documents that redirected native stderr is independent of ErrorActionPreference beginning in 7.2. The observed host is 7.6.5; **PowerShell 7.2 itself is UNTESTED**. [Microsoft preference behavior](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_preference_variables?view=powershell-7.5#erroractionpreference).

The Conda Python code uses single-quoted Python literals with no embedded double quotes. This preserves the intended argument under legacy Windows marshalling, which still matters for 7.2 and batch-file dispatch; it does not claim a completed Conda/7.2 runtime test. [Microsoft native argument parsing](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_parsing?view=powershell-7.6#passing-arguments-that-contain-quote-characters).

### 3.2 Saved observations and inference limits

| Fact | Evidence and permitted conclusion |
| --- | --- |
| Git 2.55.0.windows.3; PowerShell 7.6.5; PATH Java 22.0.2 | Direct original bootstrap transcript header, 16:14:33Z |
| Launcher/Java installation 22.0.2, Oracle, C:\Program Files\Java\jdk-22 | [DEV info log][dev-info], line 11; actual JVM metadata, not only a directory name |
| Compiler 17.0.10, Amazon Corretto, C:\Users\usuario\.jdks\corretto-17.0.10 | DEV info lines 248 and 539–540 show installation metadata and actual compiler/worker selection |
| Conda command discovery | Read-only discovery returned Application C:\Users\usuario\anaconda3\Scripts\conda.exe; discovery alone proves no environment readiness |
| om_env version requirements satisfied for the G7A reference check | Coordinating task reports the original current-reference check succeeded from 16:32:17.4987Z to 16:32:20.1335Z in [bootstrap transcript][bootstrap-transcript]. Combined with generator version assertions and the named wrapper, this supports an inference of Python 3.12.13/mpmath 1.4.1 for that check |
| Interpreter/package origins | No dedicated saved sys.executable/sys.prefix/mpmath.__file__ record exists for the original run. The inferred version result must not be presented as separately saved import-origin proof |
| Gradle user home | Saved host logs use C:\Users\usuario\.gradle. Coordinating-task inspection found no gradle.properties/init.gradle/init.gradle.kts/init.d there |
| Sandbox profile | Coordinating-task GetFolderPath(UserProfile) resolved C:\Users\CodexSandboxOnline in sandbox, unlike the managed host profile. Sandbox-only absence is not a host prerequisite failure |
| Desktop JDK 25 / packaging inventory | Original bootstrap detected Eclipse Temurin 25.0.4+7-LTS at C:\Users\usuario\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2, jpackage 25.0.4, .NET SDK 8.0.303, WiX 5.0.2+aa65968c and Util/UI extensions 5.0.2; this is inventory, not launch, packaging or installation evidence |

PATH Java and JAVA_HOME agree on this host. The mismatch counterexample below is a static/fake-first case, not a reproduced host mismatch.

### 3.3 Network, paths and omitted global dependencies

Normal execution can require network access for Git fetch, the pinned Gradle distribution, plugins and Maven/GeoGebra dependencies. Toolchain auto-download is disabled by the relevant wrappers unless explicitly opted in; that does not disable other downloads. Complete historical refs/tags/frozen blobs are needed by later verifiers even after the first baseline tag check.

Only explicit packaging installation adds WinGet/NuGet installation actions. The package source mapping is governed by packaging/windows/NuGet.Config. A global Python, global pip, Node/npm, global Gradle, Pester or another package manager is not added to the default workstation contract. No actual Node/npm invocation was found in the inspected normal path.

TEMP, repository-generated output locations, Gradle caches and Kotlin per-user daemon paths must be usable by the executing identity. Gradle inventory is not filesystem-pure. The focused installer's jpackage discovery checks PATH, JAVA_HOME and the default profile Gradle-JDK location; its omission of custom Gradle locations is a documented detection limitation, not justification to inject Gradle or product verification into that independent installer.

## 4. Findings, classification and corrections

| ID | Finding in original source or identified candidate | Classification/evidence | Candidate response |
| --- | --- | --- | --- |
| B01 | Preflight omits compiler 17 and named numerical prerequisites; Desktop 25 is checked after delegation | Demonstrated source/ordering gap; no missing-prerequisite host failure claimed | Derive requirements and check them before expensive product verification |
| B02 | PATH-major check differs from gradlew.bat JAVA_HOME precedence | Static toolchain-detection counterexample | Resolve effective launcher like the wrapper; retain PATH identity for baseline diagnostics |
| B03 | Original parser accepts the first reported matching major without checking Is JDK or binary usability | Static readiness-validation gap; the original unanchored field regex already accepts pipe-prefixed fields | Require a usable full JDK and exercise java/javac; the new candidate parser's initial pipe-format bug was corrected before integration |
| B04 | WiX StartsWith("5.0.2") accepts 5.0.20/prerelease | Demonstrated static pin inconsistency | Exact version after stripping only build metadata |
| B05 | Nonzero WiX extension-list exit becomes empty inventory | Demonstrated swallowed diagnostic/control-flow problem | Accept only success or exit-2 with genuinely empty output; preserve other failure code/output |
| B06 | Runtime-only dotnet can reach SDK-dependent global-tool inspection before planning; stale plan after tool changes | Static installer-ordering scenario, not a real installation failure reproduced | Defer SDK-dependent inventory; re-inventory/replan after SDK and WiX changes |
| B07 | Fixed TEMP/geocedg-bootstrap, incomplete early native logs and generic catch | Demonstrated evidence-routing/diagnostic gap | Unique run folder, native stage/code/log records, transcript and structured outcome |
| B08 | Per-gate generated-state copies/restores; retention originally still pays snapshot-copy cost | Source-established repeated work; isolated per-copy contribution not separately measured | Parent-owned outer transaction/Keep fast path; no reduced test assertions |
| B09 | Book fixture logs default outside central verification directory | Demonstrated routing limitation | Coordinated verification integration must route/archive them; not silently omitted |
| B10 | G9A2 process errors can be labeled stale evidence | Diagnostic ambiguity, not proof of a numerical mismatch | Preserve process/log evidence and classify before changing references |
| B11 | Custom Gradle locations absent from focused installer jpackage search | Documented discovery limitation | Preserve independent installer boundary; manual diagnosis for unsupported discovery |
| B12 | Early candidate finalization would warn on required logging failure after printing PASS | Candidate-only static review issue, never applied | Fail closed; print PASS only after required diagnostic finalization |
| B13 | Candidate native capture initialized local/script LASTEXITCODE to zero, hiding the global engine-updated result; the fixture also created a shadow before reading its child result | Confirmed candidate/instrument capture defect, separate from actual original FULL test failures | Applied global null sentinel, immediate global capture and missing-exit rejection; real child/fake-adapter boundary checks passed within the recorded infrastructure/workstation suites |
| B14 | Applied runtime fixtures nested disposable Git repositories below a long report path | infrastructure-applied-01: 45/109 cases failed with Git 128 and Filename too long | Unique retained TEMP working root, independent of report depth; infrastructure-applied-02 passed 109/109. Later root-separator/early-locator changes have a distinct source hash; FULL02 subsequently completed all 109 runtime cases on that checkpoint |
| B15 | Workstation fixture root was not a distinct LASTEXITCODE scope; its dynamic mock module also leaked fake cmdlets | workstation-applied-01 and -02: successive harness failures, not native production or log-guard failures | Explicit child scope preserves the shadow test; empty function/alias exports plus caller no-leak assertion isolate the mock; workstation-applied-03 passed 108 assertions and operational checks |
| B16 | Packaging measurement selected nonexistent packaging/windows/wix-extensions.json | packaging-inspect-applied-01: instrument exit 2, null native start/exit; no installer process ran | Corrected measurement source list names existing NuGet.Config; inspect-only -02 passed without installation |
| B17 | Optional default operational benchmark still bounded each timed quiet-verifier child at 30 s | Real unchanged-source reproduction: timeout after complete preflight, native/wrapper1; orchestration/configuration defect, not product geometry | Finite 600 s suite bound plus one additive static-contract case applied; unchanged 5000 ms informational warning and 1+3 iterations; source61 focused runtime 110/110 and generated-state 18/143 passed; successful final FULL delegation pending |
| B18 | New Conda preflight requires raw CONDA_DEFAULT_ENV to equal the short name om_env | bootstrap-applied-01: named native probe exited 0 with coherent pinned interpreter/import origins but Conda legitimately exposed its registered absolute prefix; candidate false rejection, not host/science failure | Exact short name or fully qualified identity equal to normalized fully qualified CONDA_PREFIX; all origin/version/implementation guards retained. Applied focus passed 150 assertions (108 retained + 42 across 16 Conda cases); real -02 passed this probe and then failed independently at B19. |
| B19 | Mandatory string-array parameters reject empty lines before the native-output parsers can inspect a successful Gradle inventory | bootstrap-applied-02: wrapper/version and JDK inventory native exits 0, inventory reports full JDK17/22/25; parameter-binding exception before Java usability/product gates | AllowEmptyString on the three native-output parsers, with all content checks retained; applied focus passed 158 assertions (108 + 42 Conda + 8 padding). Real bootstrap03 subsequently accepted the padded inventory and completed; blank-only/malformed required evidence still fails semantically. |
| B20 | Implicit operational benchmark precheck/warmup/measurements reuse fixed TEMP/geocedg-operational evidence | Final CI-profile01: native/runner/tool/root 1; generated-state fixture correctly rejects an existing summary at precheck; no benchmark JSON/samples. Candidate orchestration defect, not workstation/product failure | Default-only per-invocation GUID directory plus one bounded regression case; explicit binding and no-overwrite guard unchanged. Applied focused result111/111 runtime plus18 generated-state cases/143 assertions passed; replacement final02 pending; normal bootstrap supplies an explicit log tree and is unaffected by this default. |

Original source anchors: bootstrap lines 117, 198–225, 319–355, 393–418 and 461; installer lines 79–93, 127–157 and 171–247; generated-state helper and operational/book defaults. [Detailed original-source links][audit-notes] retain precise call-site context.

Not demonstrated stale: the historical baseline constants, current R6 delegation, optional packaging status, explicit install authorization, explicit Classic smoke-launch route and exclusion of the external real book/Node from normal prerequisites. No scientific failure was established by this static audit.

## 5. Reproduced failure and original-run status

### 5.1 Windows sandbox/Kotlin permissions

The original DEV command was:

~~~powershell
.\gradlew.bat :shared:common-jre:test --tests org.geocedg.common.locus.G9U0R6SemanticLocusPointInteractionTest --rerun-tasks --no-build-cache --no-daemon --no-problems-report --console=plain
~~~

The coordinating task observed java.nio.file.AccessDeniedException while Kotlin created a timestamp marker under C:\Users\usuario\AppData\Local\kotlin\daemon. The build attempted in-process fallback. The sandbox run was interrupted with exit 1. The same command under managed host escalation passed 49 tests, exit 0, in 72.694 seconds.

Classification: Windows sandbox/permissions/execution environment. It is not a bootstrap failure, a demonstrated production regression, an actual failed geometric assertion or evidence of stale numerical references.

Evidence limitation: the successful rerun overwrote the failed run's log path. The original failure survives only in tool history. [The saved DEV log][dev-log] is success evidence only. Later failed/rerun logs must use distinct paths.

### 5.2 Original bootstrap measurement

Exact entry command:

~~~powershell
.\tools\bootstrap\bootstrap-windows.ps1 -SkipFetch
~~~

Working directory: repository root. Execution: managed host escalation. No RunBenchmarks, LaunchDesktop, packaging-install switch, extra log option or output-retention switch was passed. Default snapshot/restoration behavior remained active.

Outer measurement timestamped redirected output and temporarily set GRADLE_OPTS to include -Dorg.gradle.logging.level=info. On completion the original default TEMP/geocedg-bootstrap logs were copied to baseline/bootstrap-run1; pre-existing host logs had been retained separately. Original executable source remained unchanged. These measurement conditions must match the candidate comparison or be disclosed as incomparable.

| Item | Current evidence |
| --- | --- |
| Bootstrap start | 2026-09-02T16:14:33.4586631Z, coordinating measurement |
| Nested composed start | 2026-09-02T16:14:34.0416261Z, saved transcript |
| Nested composed completion | 2026-09-02T18:54:23.0580418Z, saved completion marker |
| Original bootstrap final exit / elapsed time | Exit 0; **9590.394 s** stopwatch; **PASS_WITH_WARNINGS** (saved result text: PASS WITH WARNINGS; only SkipFetch); final repository-status entries 0 |
| Original composed outcome / elapsed interval | All composed gates passed; **9589.0164157 s**, derived from observed delegation/completion markers, not a separate stopwatch; detailed runtime ledger in the performance report |
| Original FULL | **FAILED**; effective/wrapper exit 1; **5594.1837773 s** instrumented wall time; shared-JRE **6408 tests, 6 failures, 10 skipped**; unfiltered Desktop **NOT EXECUTED** |
| Original isolated spreadsheet reproduction | **FAILED**; **1 test, 1 failure, 0 errors, 0 skipped**, with the same empty-redefine-context exception; native-exit metadata limitation described below |
| Corrected bootstrap03 | Native/measurement/nested COMPOSED exit 0; outer 1514.5540847 s, bootstrap 1514.1860467 s, nested 1498.913 s; only SkipFetch warning. Its separate r3 linked envelope remains failed 2, section 5.6; no successful historical link is inferred. |

The completed original transcript and [measurement record][bootstrap-measurement] establish success for the requested bootstrap/COMPOSED scope, including operational/workstation fixtures and numerical reference checks. They do not establish FULL or candidate success. The complete-workflow candidate timing and its context limitations are recorded in section 5.6; this original result alone does not establish an optimization effect. Existing dependency/output-cache conditions are not presented as an empty-cache/cold benchmark.

### 5.3 Completed original FULL and isolated failure

The [original FULL summary][full-summary] records start 2026-09-02T18:54:50.5527108Z and finish 2026-09-02T20:28:04.7372042Z, a terminating invocation and effective/wrapper exit 1. Its retained originalLastExitCode field is 0; that is not a successfully returned invocation or grounds to disregard the terminating shared-test error. The wall time includes capture; 7.0533695 s of archive work is reported separately, without inventing an uninstrumented duration. A conflicting task-heading/outcome grouping is also retained in the wrapper's instrumentation diagnostics, so boundary groups are not automatically treated as authenticated invocation counts.

The [shared-JRE log][full-shared-log] reports 6408 tests completed, 6 failed and 10 skipped at line 54837, followed by BUILD FAILED at line 54992. The run stopped before unfiltered Desktop; earlier focused Desktop results inside COMPOSED cannot fill that missing FULL coverage. Original sources remained unchanged. This completed failure is not equivalent to a completed green FULL gate.

One failure, KernelCellDragPasteHandlerTest.testDragPasteShouldResultInNonEmptySpreadsheetCells2, is recorded at shared-log lines 54375–54379: IllegalArgumentException, "Completed redefine context collection cannot be empty", through SpatialIdentityRegistry.java:3475, Construction.java:2847 and KernelCellDragPasteHandler.java:105. The separately saved [isolated reproduction log][spreadsheet-repro-log] records the same failure at lines 1007–1015; its [archived JUnit summary][spreadsheet-repro-summary] contains exactly one testcase and one failure. The isolated trial's NativeExitCode field remains 0 from the earlier shadowing-prone control instrument. That field is not trusted as the actual native exit; BUILD FAILED, the failed task and archived JUnit are preserved failure evidence. No saved field was repaired or replaced, and no missing native code is inferred.

Classification of this reproduced spreadsheet case: a pre-existing production host-integration/API-contract failure in the original baseline, not missing Conda, a sandbox permission symptom, bootstrap installation failure or a candidate regression. Static inspection shows an ordinary collected redefine can leave a non-null but empty spatial context map, which is then passed to the registry's nonempty-completion contract. Isolated reproduction shows the broader FULL ordering is not required to trigger this case. This report neither diagnoses the remaining five failures by analogy nor authorizes a production fix, test exclusion, assertion weakening or gate waiver. Compatibility design and resolution remain coordinator-owned.

### 5.4 Failure decision rules

Classify each future failure before changing code:

| Category | Required distinction/action |
| --- | --- |
| Genuine product regression / actual test failure | Identify failing current test/assertion and saved result; a child exit 1 alone is insufficient |
| Stale bootstrap orchestration / stale verifier invocation | Show the exact interface/source mismatch |
| Unsupported or missing prerequisite / toolchain incompatibility | Report selected command/environment/JDK, expected source requirement and actual version/origin |
| Environment / permissions-filesystem | Keep sandbox/managed-host identity separate; use appropriate managed escalation before treating permission symptoms as product defects |
| Network/transient dependency | Retain native fetch/resolution evidence; do not classify every native failure as network |
| Generated-output problem | Report transaction, exact target, recovery path and original/cleanup errors |
| Numerical/reference mismatch | Separate process/environment failure from a successful generator reporting a mismatch; never regenerate references merely to pass |
| Unknown | Preserve stage, native code, logs and context; do not guess |

Missing optional packaging tooling may warn. Failed product checks, failed required evidence publication and genuine native inspection failures must not become optional warnings.

### 5.5 Candidate bootstrap failures and separate corrective evidence

Both measured attempts use the normal verification path with explicit
`-SkipFetch -LogDirectory <trial>/authority`, not the installer or SkipBuild path.
Like the original comparison member, their measurement parent supplies the
recorded info-logging environment. This does not establish newly fetched remote
state, installation, GUI launch or a pristine workstation.

| Attempt | Final outcome and boundary | Actual cause |
| --- | --- | --- |
| `after/bootstrap-applied-01` | Native/measurement-wrapper 1, bootstrap FAIL, envelope 2; outer 3.9414853 s / bootstrap 3.5148015 s | B18 short-name assumption rejected valid raw Conda identity after its native exit 0; zero Gradle commands and no nested product verification |
| `after/bootstrap-applied-02` | Native/measurement-wrapper 1, bootstrap FAIL, envelope 2; outer 15.4725328 s / bootstrap 15.1073672 s | B18 corrected and real Conda accepted; B19 native-output parameter binding rejected empty strings after two successful Gradle metadata commands; no nested product verification |

For -01, the [raw named probe][bootstrap01-probe] records CPython 3.12.13 and
mpmath 1.4.1. `sys.prefix`, `CONDA_PREFIX` and raw `CONDA_DEFAULT_ENV` are
`C:\DesarrolloyDatos\Areas\Working\Python\om_env`; the executable is its
`python.exe` and mpmath is under its `Lib\site-packages`. The installed Conda
`activate.py:719–722` returns the full prefix outside a parent literally named
`envs`, as recorded in the [bounded diagnosis][conda-identity-diagnosis].
The candidate's extra short-name predicate was wrong. No environment, generator,
scientific reference or installed software was changed to repair it.

The applied B18 module now accepts either the exact required short name or a
fully qualified identity equal to normalized fully qualified `CONDA_PREFIX`.
It retains exact named invocation, Python/implementation/mpmath pins,
`sys.prefix` equality, strict executable/import containment and rejection of
relative, drive-relative, unrelated and prefix-lookalike identities. The raw
identity is included in rejection diagnostics rather than discarded.

The [focused B18 run][conda-identity-focus] completed native/wrapper 0 from
2026-09-03T00:27:48.514583Z to 00:27:56.0920178Z. Its 7.5767061 s authority wall
includes fresh PowerShell startup and streaming capture; 1.3900558 s
preparation/evidence and final summary publication have separate boundaries.
The raw log records 150 assertions: 108 retained plus 42 additional assertions
across 16 Conda cases, followed by the retained packaging matrix, bootstrap
ordering and real temporary Git/generated-state transaction. The 61 selected
source hashes, status/index/HEAD and environment match before/after.
This is focused fake-first/native/filesystem-boundary evidence, not a completed
real bootstrap. Earlier 108-assertion runs keep their own bytes and outcomes.

The real -02 Conda command returned 0 in 2.4853561 s and execution advanced to
Gradle wrapper inventory (0.4276678 s) and JDK inventory (11.1667086 s), both exit 0.
The [015 inventory][bootstrap02-jdk-inventory] contains real empty separator lines
and full JDK17/22/25 records. The final exception is
`Cannot bind argument to parameter 'Output' because it is an empty string.`
The candidate parser never reached its semantic inventory checks. This is a
PowerShell parameter-contract defect, not evidence of missing JDKs or a native
Gradle failure. Both final bootstrap summaries have no finalization errors; a
post-probe validation/binding exception correctly has no invented native exit.
A complete aggregate WorkstationFacts object was not returned in either failure.

All saved -01/-02 measurements preserve their 64 selected records (61 task paths
plus three instruments), status/index/HEAD and inherited environment before/after.
The linked envelopes separately return 2 because the bootstrap did not close
successfully; that does not replace the real native/wrapper 1. Their failure
records are immutable. The B19 correction, its own focused result and the new
real bootstrap cohort are recorded separately, never assigned to old hashes.

| Saved record | SHA-256 |
| --- | --- |
| -01 measurement | `ea75cea4c2ccc0edb5da0dde2c5ce7001b9541ed86618b5fb995dd869c609426` |
| -01 bootstrap result | `089d9c958a7eb1330239e03e350b13622e00f9852cb7b8f45fe6f9330643eb71` |
| -01 linked envelope | `aae92b3576b86e4d870f8816b4434f139226ec479258f3e51255d7f2b5642ee3` |
| B18 focus measurement | `383d25cce38d9cab3b4795f773cd95684ba6b87964b94bf8fdb3ab0dab7cc2c4` |
| B18 focus raw log, 1,424 bytes | `bc16b850336a1b9eca838255ee72d0dbfdddd4718dd177e12fbe6c47dc9d5e7c` |
| B18 measured module / test | `d86139f05f9da05d3fbd25a779af3a5054bd7a2cca6051a7e25325874174008e` / `e3d6d90f02cfa4ab80ae50832ac7afcba812d5ac78f74e7b721b1c77fd4fb8c8` |
| -02 measurement | `a29a8788e45416ab2fa7e31d77638bdcb15d79f3898e509f4559608a5ea4727f` |
| -02 bootstrap result | `61bea40adf64135d4579af4f5d0451b6a18f17b98a46b413d9103a016dec7877` |
| -02 linked envelope | `f91979f87f9526ac8cce8a8569fedfd2034319af9b85f202720a47e5d40db0e7` |
| -02 raw log, 1,034 bytes | `3d77606a0d875a06d5ef7df2557e708f20cdf63a1758d04f156d798e3c800983` |
| -02 015 JDK inventory | `e068a21b6fccb964d6976d138dac0b134d305d7dc2ba4f64c7047fad72c5477e` |

The applied [B19 correction design][native-padding-design] adds
`AllowEmptyString` only to the Java, Gradle-toolchain and Conda native-output
parser parameters; their bodies retain their original content checks.
Blank-only Java/Conda data and duplicate Conda records still fail. Empty Gradle
data parses to no candidates, which cannot satisfy the required usable JDKs.
The [focused B19 run][native-padding-focus] passed native/wrapper 0 in
6.9984691 s, plus 1.4128554 s separately measured preparation/evidence:
158 assertions = 108 retained + 42 Conda + 8 padding, followed by the unchanged
matrix, ordering and real temporary Git/generated-state transaction. Its 61-source,
status/index/environment before/after records match and instrumentation errors are
empty. This is a narrower result than the subsequent real bootstrap03 in section 5.6.

B19 focus measurement SHA-256:
`d5b39ef308167a9f3e2e2076221139ceff45554d89a3573c62270d1ca89b344c`;
raw 1,550 bytes:
`7b9927572b52cb5af2a6930ee9a45b76a8ef766f7541352c03914ff8e607ffdd`.
The tested module and fixture hashes are respectively
`f866f6d65a6178b1d2d872db8377efa862bd69f4627596666ac8eb053e3e1718`
(17,741 bytes) and
`74c8e17e51a249b985ed719169c159b6134cf90a94a51a53e7688aba516c6b97`
(49,716 bytes). These are successive bytes of two existing paths, not additional
paths or proof that the earlier 108/150-assertion runs tested this correction.

### 5.6 Completed real bootstrap03 and distinct envelope defect

The [closed independent workstation audit][bootstrap03-audit] records the normal
command `pwsh -NoProfile -File tools/bootstrap/bootstrap-windows.ps1 -SkipFetch
-LogDirectory <trial>/authority`, with
`<trial>=artifacts/verification-performance-bootstrap/after/bootstrap-applied-03`
and unique child `20260903T004314870Z-c7a569d25b5241ff97b521e91bccabe2`.
No installation, GUI, SkipBuild, optional benchmark, CleanBuild, KeepBuildOutputs
or toolchain-download switch was supplied. The measurement parent supplied
`-Dorg.gradle.logging.level=info` through GRADLE_OPTS, matching the original
logging treatment; the authority runner preserved its recorded argv/environment.

| Boundary | Completed saved result |
| --- | --- |
| Native bootstrap / measurement runner | Exit 0 / 0; outer 1514.5540847 s, no measurement errors |
| Bootstrap authority | PASS WITH WARNINGS; internal 1514.1860467 s; Failure=null, FinalizationErrors=[]; only skipped-fetch warning |
| Nested COMPOSED | Exit 0, TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL; 1498.913 s; no optional gates |
| Original linked envelope r3 | **Exit 2**, BOOTSTRAP_LINK_DIAGNOSTIC_FAILURE, Link=null; outer tool session 1; not overwritten |

The native interval is 2026-09-03T00:43:14.665922Z to 01:08:29.2206004Z.
The outer stopwatch includes fresh PowerShell startup and streamed capture,
excludes 1.2583274 s preparation/evidence and final summary publication.
Bootstrap has no explicit StartedUtc/FinishedUtc fields; its RunId time is not
substituted for them. The nested root provides its own narrower timestamps.

All 25 contiguous native records returned 0 with saved logs and no invocation or
diagnostic failure: 19 preflight, five packaging inspections, final Git status.
The 19 preflight raw files match their provisional hashes exactly. Final parsed
facts match the actual saved Conda/Gradle data, including both former failure
shapes: the valid absolute Conda identity and real empty Gradle separator lines.

| Current real prerequisite | Result |
| --- | --- |
| PowerShell / effective Gradle launcher | 7.6.5 / Oracle Java 22.0.2 from JAVA_HOME |
| Compiler / Desktop full JDKs | Corretto java/javac 17.0.10 / Temurin java/javac 25.0.4; Gradle discovery, auto-download disabled |
| Named numerical runtime | CPython 3.12.13, mpmath 1.4.1; exact named om_env invocation |
| Interpreter and prefix | `C:\DesarrolloyDatos\Areas\Working\Python\om_env\python.exe`; sys.prefix, CONDA_PREFIX and raw CONDA_DEFAULT_ENV equal its parent |
| Import origin | mpmath under that prefix's `Lib\site-packages\mpmath\__init__.py` |
| Optional inventory only | jpackage 25.0.4, .NET SDK 8.0.303, WiX 5.0.2 and UI/Util 5.0.2; no software installed or package produced |

Before/after status is byte-identical: 61 paths, 46 modified plus 15 new, none
staged. All 64 source/instrument path/hash/byte tuples agree across the prelaunch
contract, measurement before/after and envelope after records. HEAD/index and
inherited-environment digests agree. GRADLE_OPTS was absent before and after the
outer envelope; its temporary info property is separately hashed. These are
saved-record observations, not a new live-host scan or equality with earlier
source60 checkpoints. Transcript/native/summary publication completed, and the
transcript reports default restoration of the same 23 generated paths. No
independent raw hash of entire restored trees was taken.
The recorded index digest `76b8e834a6b4866023752cdc2ce52073d29824d3996e89a5e87d462f6b739688`
is the SHA-256 of LF-joined `git ls-files --stage` output: a logical staged-entry
snapshot, not a raw-byte hash of `.git/index`. Raw source-file hashes are separate.

The nested receipt holds 994 shared cases in 97 XML and 102 Desktop in 17 XML,
**1,096 cases, zero failures/errors/skips**. Both Test tasks executed; four
required Checkstyles were UP-TO-DATE with validated reports. The receipt is
still marked phase-assertions-pending; the later root establishes final
COMPOSED completion. All six selected canonical summaries match FULL02 as
whole byte sequences, no normalization/field removal, with 12 stable input hashes.
The [comparison ledger][bootstrap03-summaries] does not itself prove complete
JUnit-perimeter equality.

The [temporal diagnosis][bootstrap03-temporal] preserves the exact r3 failure:
`Contract was not recorded before bootstrap started.` An in-memory String
cast became local DateTime while JSON produced UTC DateTime. Their wall-clock
ticks were compared without UTC normalization. The saved instants show the
contract at 00:43:13.5210737Z, **1.1448483 s before** native start. This is an
instrumentation defect, not late provenance, a product/Conda failure or failed
restoration. The failed envelope still has Link=null; no source-bound nested
result digest was sealed in it. The separately reviewed archive-only consumer subsequently completed with
native/saved/reconciliation/tool exit 0 and all original UTC inequalities
preserved. Its mandatory nested-result pin is explicitly post-run review,
not historical producer linkage. No envelope was overwritten and no original
successful link manufactured; no second product bootstrap was required solely
to correct this artifact-layer defect.

The [archived reconciliation][bootstrap03-reconciliation] rehashed 278 inputs.
Original phase 1–30 contains 1,455 occurrences / 1,080 union identities; bootstrap03
has 1,096, all PASSED, zero differences/transitions, with exactly 16 named additions.
Eight are shared: three pre-existing final-frontend cases outside the phase union
and five new GeoCeDG cases. Eight are the new Desktop resource tests. Thus 16
is not a count of newly implemented tests. The separate native original union
control already includes the three frontend cases (989 shared + 94 Desktop);
do not mix its 1,083 with the 1,080 phase union. The consumer records reviewed
source-bound delegation, not natively observed argv of the in-process PowerShell
child. This is saved evidence analysis, not a fresh build, authenticated historical
signature, full-scope equality, scientific acceptance or author approval.

The complete original stopwatch was 9590.394 s; candidate outer 1514.5540847 s.
Both are the normal SkipFetch/bootstrap workflows with info logging, not FULL.
The new preflight/logging, fresh-process capture boundary, repaired/additive test
scope, changed source bytes and evolved output/dependency-cache state must be
disclosed. This is not a cold workstation, empty dependency cache, same-source
causal speed-up, variance estimate or standalone COMPOSED timing comparison.
With those boundaries and the completed archived relation explicit, the observed
complete-workflow reduction is **8075.8399153 s / 84.2076%**. It is not an isolated
Gradle effect or a universal/repeated-performance estimate; original-source
matched controls supply the separate mechanism evidence. The nested 1498.913 s
COMPOSED interval is not assigned a standalone percentage.

| Bootstrap03 evidence | SHA-256 |
| --- | --- |
| [Measurement][bootstrap03-measurement] | `fb44ef870872679570b8db96c77da9036591bda2efc92bb654d8d850a2359410` |
| [Bootstrap result][bootstrap03-result] | `f8a4b6aee146c5c51c16531c3343363d4b93dc557c75e8aed167f2916d71411a` |
| Nested COMPOSED final root | `5ea14764411ff17770438c87dd017e6c0a0baf85735f3b748c77c694d2be5561` |
| Intermediate canonical receipt | `02e09dc6e5cd2dad42b370b79eecbe02cf0b0cf30058787290654c5e1a34d14a` |
| Original failed r3 envelope | `e0ed64046bf34074343aec96108aa08501e8b6db8d69515366ab5429bd739dcf` |
| [Workstation audit ledger][bootstrap03-ledger] | `7ab926d168fca91d57dc021ef8d92e50539f81468fc46050046d3c242a6a2656` |
| Six-summary byte comparison | `b2807eac0870ffc57b6dcfcc8329b8d098b9f3ee070f3da9ccd79a5577e1c2b1` |
| Archive-only temporal-link/raw-case reconciliation | `203aafe2150c850184b5802ffd20f7b68c6e88d0534fdf3f723b2c3beea4e06f` |

### 5.7 Failed final CI-profile01: implicit operational evidence reuse

After bootstrap03, exact-source `after/final-full-ci-profile-01` attempted FULL
with delegated benchmarks, default restoration and no measurement-added info,
Keep, Clean, GUI or scientific-body opt-in. It **FAILED** with native/measurement/
tool/root exit 1 and no instrumentation errors: outer 1732.4888133 s,
root 1732.191 s, separate preparation/evidence 1.2846103 s. Source61, recorded
status/index/branch/HEAD and parent-process environment were unchanged.
The native interval was 2026-09-03T01:52:35.7752113Z–02:21:28.2646630Z.
[Measurement](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/measurement-result.json)
SHA `e33c7a9ee14a4966b57990a9ee3811934bfccd83fa914645884c5893e2b55b07`;
[failed root](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/authority/verification-result.json)
SHA `5bb7b0fbb25f2717e5315a0d48b6b34e6a1ccc6d86e67a0dd7947b5dc3f6979d`.

The benchmark precheck invokes full `verify-operational.ps1 -Quiet` without
LogDirectory. Its fixed default collides with a prior generated-state summary;
the fixture correctly refuses both execution against old evidence and overwrite
during failure publication. The precheck's runtime fixture returned 0/63.069 s;
generated-state returned 1/0.399 s before cases. The old 18-case/143-assertion PASS
is another run's 00:02:11–00:02:12 record, never final01 evidence. Five TEMP artifacts
were copied unchanged under `failed-benchmark-precheck/`; the
[archive ledger](../../artifacts/verification-performance-bootstrap/after/final-full-ci-profile-01/failed-benchmark-precheck/archive-ledger.json)
SHA `f103e3f327f8ed71ded53aa29d15d2977e0a253573c993c14c184d3cf1834e83`
records bytes, paths and unchanged source hashes. Nothing was deleted or overwritten.
No warmup, measured samples or benchmark JSON were produced. Canonical Gradle/
phase checks reaching precheck do not convert the failed root into FULL PASS.
The success-only auditor identified by `21ce` was not executed against this
real failed run and supplies no real-run audit authority. A separate
[partial archived-test/style diagnostic](../../artifacts/verification-performance-bootstrap/inventory/final-full01-failed-partial-junit-note.md)
reports 703 hashed XML/7,596 cases with 7,585 PASS and 11 recorded SKIP, four clean
style reports and a read-only analysis exit 0; **the product root remains FAILED**.
Its ledger SHA `79cb07201405c18fc9e08648cef2d9da9a6df18378f63f62241e15cc09899f56`
and note SHA `8471ee5052e84daa4b87f516e06bccd9aee1f546f112aa69edcfa1927e709f91`
are separate diagnostics, not a successful FULL or benchmark.

B20 is default operational evidence-path orchestration, not Java/Conda/numerical,
ACL, native-exit handling or timeout600. The
[bounded design](../../artifacts/verification-performance-bootstrap/inventory/final01-benchmark-evidence-collision-design.md)
(SHA `0243a1e8e916fb92f46863c6502b89775ae7d0429558348f3973461804767e6d`)
changes only the default to `TEMP/geocedg-operational/<new GUID>` per invocation
and adds one fake-first regression. Actual operational ParamBlock binding checks
implicit/default and explicit paths; separately, the actual generated-state
publication-guard AST checks owned sentinel rejection/byte preservation. The
guard is not part of the ParamBlock and no operational/helper body is run by
this case. It preserves exact explicit
LogDirectory binding, existing-summary failure, all operational bodies,
generated-state machinery, benchmark 600 s / 5000 ms / 1+3 and scientific contracts.
**FOCUSED_DEFAULT_LOG_FIX_RESULT — COMPLETED**:
`after/infra-operational-log-isolation-applied-01` returned native/measurement/
tool 0, `PASS_FAKE_FIRST_OPERATIONAL_ONLY`, no instrumentation errors.
Runtime 111/111 passed (75.914 s); the exact new case
`operational defaults isolate five invocations and preserve explicit evidence`
passed in 0.1853418 s. Generated-state 18 cases/143 assertions passed (1.995 s),
cleanup_errors empty. Outer 78.3426822 s; preparation/evidence 1.3400734 s.
Saved source61/status/index/HEAD/branch and parent-environment digest agree within
this run, not across launches; no live-child environment dump is inferred.
The focus digest `6d0da6734332a60fb67ea695f28019059691c88aba5a68ae00efb1433d1c513d`
differs from final01's recorded parent digest. Earlier110-case results remain
historical; only this new run validates the additional case.
[Measurement](../../artifacts/verification-performance-bootstrap/after/infra-operational-log-isolation-applied-01/measurement-result.json)
SHA `0c9c1a34d9cf75db4e251cccdb26f1b43f3c5d5e31bb294c2941b43ae83f5ed9`;
[infrastructure result](../../artifacts/verification-performance-bootstrap/after/infra-operational-log-isolation-applied-01/authority/verification-infrastructure.json)
SHA `87bfa8926c8ee4f5d78b3293b811952b533d1cbef7e774a8db32bc027ae3abf2`.
The performance report retains remaining result/source pins. No Gradle/product
or successful benchmark/FULL evidence is supplied by this focused fixture run.

For this bounded correction, **BOOTSTRAP IMPACT — NO CHANGE REQUIRED**:
normal bootstrap already passes an explicit canonical log tree, not the implicit
operational default; no prerequisite, installer or generated-state contract changes.
This is a correction-specific rationale, not reversal of the overall task's UPDATED
declaration. Bootstrap03 stays tied to its earlier cohort and its envelope2/archive
reconciliation0 remain separate. No arbitrary extra bootstrap is required.
The replacement final02 checkpoint still has 61 paths, but differs from bootstrap03
in seven: five documents plus operational entrypoint and runtime fixture. Earlier
clean-FULL source60 is unchanged historical evidence, not a new final02 clean run.

## 6. Candidate architecture and workstation contract

### 6.1 Applied candidate graph (normal path completed in bootstrap03)

~~~text
bootstrap
  -> unique run diagnostics
  +-- explicit packaging-install branch
  |     -> focused installer only
  |     -> preserve installer outcome/code
  |
  +-- normal onboarding
        -> existing clone/provenance checks
        -> source-derived prerequisites
        -> wrapper-effective Java 22
        -> named Conda version + interpreter/import-origin probe
        -> actual wrapper and full JDK 17/25 inventory
        -> one delegation to canonical verify.ps1 default COMPOSED
        -> actual baseline metadata remains evidence
        -> optional packaging inventory
        -> repository status preservation
  -> close required transcript
  -> publish complete summary via pending-file move
  -> print final PASS only if required diagnostics also succeeded
~~~

The candidate intentionally preserves the existing composed delegation for now. It does not introduce a new hidden product gate or remove historical/numerical checks from bootstrap without measured coverage-equivalence evidence. FULL remains available through the canonical verifier, not silently duplicated inside a new installer/preflight helper.

The new workstation module has internal source-reader/command-runner seams for fixtures, not public bypass switches. It records sys.executable, sys.prefix, CONDA_PREFIX, raw CONDA_DEFAULT_ENV, Python implementation/version and mpmath.__file__/version. The raw identity may be the exact required name or the same normalized fully qualified prefix; it is not assumed to be a short name. Correct versions from a wrong active/global interpreter cannot satisfy the named-environment check. The implementation requirement is derived from the generators' declared runtime metadata: G7A line 52, G8A line 95 and G8C line 51 all name CPython. Their existing guards compare versions, while the candidate additionally checks the observed implementation against that declared provenance. Generator/reference bytes remain untouched.

Both JDK 17 and 25 must be full usable installations reported by Gradle, including java/javac version agreement. Paths are discovered, not copied from this machine. Invalid nonempty JAVA_HOME must not silently fall back to PATH. Actual wrapper Launcher JVM metadata is checked as well.

The source contract in section 3 has actual preflight/end-to-end evidence from bootstrap03. Its successful authority results remain distinct from the failed r3 artifact-link envelope. Applied fake-first checks and standalone packaging inspection retain their narrower evidence scopes. The declared PowerShell floor is 7.2, while the measured host is 7.6.5; the minimum itself remains untested.

### 6.2 Diagnostics and failure propagation

New LogDirectory is a parent for a unique UTC/GUID child. The existing shared log-directory guard runs before directory creation, transcript startup or probes; the selected log path remains null after rejection so finalization cannot write into a rejected location. The accepted child contains:

- bootstrap-transcript.log;
- preflight/NNN-description.log, separate even for repeated descriptions;
- verification/ for delegated logs;
- bootstrap-result.json with outcome, failed stage/classification/native code, warnings, command timings and workstation facts.

Required transcript closure precedes summary publication. The summary is fully written to a .pending path then moved to the final name; pending files are not successful evidence. Missing/failed transcript or summary publication yields FAIL/exit 1. A primary installer/native failure is retained separately from every finalization error. A native-log write failure retains native exit/output metadata before reporting diagnostic failure.

The installer rethrows its original annotated failure rather than replacing it with a terminating Write-Error string. Bootstrap catches that same failure at the installer invocation boundary and preserves NativeExitCode, NativeOutput, stage and classification. InstallerReturnedNormally/InstallerExitCode distinguish a terminating installer exception from a returned installer code; a command-launch failure carries no invented native code. Transitive fixtures copy the actual entry scripts unchanged, replace only the packaging provider, and exercise a benign command exiting 73 and a missing-command case. They completed in workstation-applied-03. The fake provider permits no installation, network or Gradle action; this is not an executed real-installation failure route.

The generic candidate native adapter throws when invocation fails before a native exit is captured. NativeExitCode remains null, the original exception is retained, and a separate DiagnosticFailure records failed log publication. It never returns that null to WiX's integer exit-code consumer. The earlier draft's synthetic fallback code 1 was corrected before application; this does not mean the original bootstrap reproduced that draft defect. Workstation-applied-03 completed the real exit 0/1/23, absent-command/stale-LASTEXITCODE, direct WiX consumer and combined invocation/log-failure fixtures.

The [native-exit audit and hash ledger][native-exit-audit] confirmed a separate candidate capture defect: assigning LASTEXITCODE locally in the helper or measurement script hid PowerShell's global native result. Applied bootstrap and measurement adapters use a global null sentinel and immediate global capture; a missing native result fails closed. Installer, workstation child/Git and runtime Git/native boundaries use the same explicit convention, without claiming every site had a self-created shadow. Deliberate fixture shadows now live in explicit child/module scopes while fixture child capture reads global. Existing result-object adapters remain fake adapters, not evidence of native process capture. The completed applied suites below supplement, rather than retroactively change, the historical syntax-only ledger.

The recorded applied fixtures include benign real child exits 0/1/23 (runtime also 37), a WiX-shaped command returning 23, an output-only adapter with no native process, and the installer-to-bootstrap code-73/missing-command routes. They distinguish actual native failure, invocation failure and diagnostic-publication failure. Infrastructure-applied-02 and workstation-applied-03 provide the bounded executed evidence; neither substitutes for the separately completed real bootstrap03.

If final publication fails, no final saved result is claimed; stderr and retained pending material are the available diagnostics. Context.GradleUserHomeAssumption is explicitly process GRADLE_USER_HOME or current-profile default, not proof of the effective Gradle home in the presence of JVM/init overrides.

### 6.3 Focused installer isolation

The installer remains inspect-only without -Install. It has no WhatIf or separate DryRun contract; fake-first testing is not an undocumented public dry-run option.

Its sequence is inventory → plan → explicitly authorized actions → fresh inventory/plan. A runtime-only .NET installation defers SDK-dependent WiX inspection. After installing the approved missing SDK, discover existing WiX before deciding install versus update. After changing WiX, refresh extension inventory before adding missing extensions.

WiX accepts exact 5.0.2 or that version plus build metadata, not 5.0.20 or a prerelease. Extension-list exit 2 is empty inventory only with genuinely empty output; other errors remain errors.

No Git, Gradle, product test, global Python or new global package-tool dependency is introduced into the focused installer. Normal bootstrap packaging remains optional after the product gate. A native installer artifact, file association, GUI launch or redistribution permission is not inferred from prerequisite readiness.

The original bootstrap supplied inspection-only inventory evidence for existing host tools, not focused setup/idempotence evidence. The applied standalone inspector then completed as packaging-inspect-applied-02 (native/wrapper exit 0, 0.9653562 s): .NET SDK 8.0.303, WiX 5.0.2, Util/UI extensions 5.0.2 and jpackage 25.0.4. Its command supplied no -Install and its log explicitly says INSPECT ONLY and no repository verification. Actual installation, upgrade, recovery and repeated-install behavior remain unvalidated and require explicit authorization. No WhatIf/DryRun, packaging-build, GUI or successful setup claim follows from this inventory.

### 6.4 Output ownership and verification levels

An implicit operational invocation requires a new GUID child under its TEMP
log parent. Explicit caller LogDirectory values are not rewritten, and an existing
fixture result remains a failure rather than reusable current evidence. The B20
correction does not change bootstrap's explicit canonical log-tree delegation.

The applied parent-owned generated-state helper adds a Keep fast path without enumeration/copy, common snapshot-root validation before creation/restoration/deletion, linked-root/ancestry rejection and recovery-data retention on restore failure. CleanBuild is limited to enumerated allow-listed repository-generated directories; user/dependency caches and tracked source are not cleanup targets. Infrastructure-applied-02 completed 18 mock-enumeration/filesystem cases with 143 assertions; workstation-applied-03 separately completed the retained real temporary Git/generated-state transaction. Restore recovery is not atomic rollback, and mocked reparse attributes do not validate real junctions or race resistance.

| Entry | Role in candidate |
| --- | --- |
| Bootstrap | Workstation/provenance/toolchain diagnosis plus canonical COMPOSED delegation; no independent scientific authority |
| DEV | Explicit narrow module/tests; incomplete global assurance, never acceptance |
| PHASE | Existing named verifier and its declared regression perimeter |
| COMPOSED | All current applicable cross-phase/non-test/scientific/governance gates; only demonstrated duplicate execution may be shared |
| FULL | COMPOSED plus existing unfiltered shared-JRE/Desktop suites; required for bootstrap/verifier/toolchain infrastructure changes |
| CI | Applied workflow selects canonical FULL with existing benchmark/evidence capture; no CI execution result is certified by this report |

Original CI installs Java 17/22 and prepares om_env, then calls verify.ps1 -RunBenchmarks. It does not itself run bootstrap, FullTests or native packaging. The candidate must explain and validate any changed CI scope; it must not imply that historical CI already supplied FULL/interactive/packaging evidence.

SkipBuild is static/incomplete verification, not trusted test-result reuse. KeepBuildOutputs changes retention, not scope/freshness. Any current-run evidence sharing must retain each verifier's actual assertions, historical evidence checks and numerical checks. No test removal or new test parallelism is justified by this bootstrap report.

## 7. Test design and present evidence

| Check | Applied coverage | Present evidence |
| --- | --- | --- |
| Workstation prerequisites | Derived pins/contradictions, JAVA_HOME/PATH mismatch, invalid home, missing Conda/JDKs, JRE-before-JDK, custom paths, real pipe-prefixed Gradle format, wrong versions/interpreter/import origin; added name-or-prefix Conda identity cases | workstation-applied-03: 108 assertions at its checkpoint; later workstation-conda-prefix-applied-01: 150 (108 + 42 across 16 Conda cases), fake-first plus retained native/filesystem boundaries. Real -02 passed B18 but failed separately at B19. |
| Packaging workflow | Exact WiX pin, extension exit/error distinction, runtime-only SDK, refreshed SDK/WiX/extension plans, no-install default, missing WinGet blocker | Same applied fake-first suite passed; installation/upgrade untested |
| Diagnostics/native exit | Unique logs, global capture under explicit local shadows, missing exits, real child codes, WiX/installer propagation, combined primary/logging failures, required publication and no-write log guard | workstation-applied-03 passed; combines benign real child-process boundaries with fake adapters; no real bootstrap execution |
| Native-output blank lines | Padded real-shaped Java/Conda/Gradle records, raw retention, blank-only/duplicate/missing-JDK rejection | workstation-native-padding-applied-01: 158 assertions, including 8 new padding assertions; no real bootstrap inference |
| Runtime/infrastructure | Current-run evidence, input closure, selection, outcome/XML, capture and consumer contracts | infrastructure-applied-02: 109/109 runtime cases passed; zero Gradle/product runtime executions |
| Generated-state safety | Independent restore/retention/failure/containment/CleanBuild cases with explicit helper/log paths | infrastructure-applied-02: 18 cases, 143 assertions passed; mocked Git enumeration/reparse attributes |
| Existing workstation generated-state/Git fixtures | Original integration fixture retained, not replaced by mocked enumeration | workstation-applied-03 passed real temporary Git/state transaction |
| Corrected inspect-only installer | Real supported-host installed-tool inventory, empty entry arguments | packaging-inspect-applied-02: native/wrapper exit 0; no installation |
| Corrected normal bootstrap | Complete saved outcome, diagnostics and current workstation facts | Bootstrap03 native/runner/nested 0, actual prerequisites and normal delegation completed; original envelope 2 remains separate, section 5.6. Attempts -01/-02 and focused cohorts retain their own outcomes. |
| Product levels / repeated execution | DEV49 first/repeat, R6 PHASE, COMPOSED01 and FULL02; bounded saved-case relations | Completed at their identified source checkpoints; see performance report and section 7.2; no bootstrap inference |
| Clean-output / final-source CI profile | Explicit CleanBuild and final FULL with operational benchmarks | Clean-output FULL completed0 on source60; final CI-profile01 failed at benchmark precheck, replacement final02 remains **PENDING**; no benchmark/bootstrap inference |

[Preparation metadata][preparation] records the earlier staged hashes and syntax-only scope. That earlier patch was applied in memory to original source strings and matched its six staged review copies. The newer native-exit ledger records the subsequent hashes, seven-script AST inspection and unchanged original bases; bootstrap.patch was synchronized with those previews. Neither preparation record is an executed candidate verification or timing result.

The [generated-state suite][generated-tests] uses isolated dynamic-module enumeration/reparse mocks and real copy/remove only inside explicitly owned temporary fixtures, with exact resolved-path/marker/ancestry guards. It adds no Pester/Git/global dependency. Mocked reparse attributes prove branch intent only; they do not demonstrate Windows junction behavior, race resistance or real Git classification. Existing real-Git tests remain independent integration evidence.

No test result, bootstrap result, installer result, clean-output result or performance improvement is inferred from AST inspection.

### 7.1 Applied results and retained harness failures

The [applied evidence note][applied-note] records all seven trial paths, exact measurement SHA-256 values, raw-log hashes, exit codes and source-version limits. Available raw hashes were independently recomputed and matched their records. Every started trial recorded matching before/after explicit source hashes, status and parent-environment digest; this is a bounded measurement scope, not a clean-worktree or whole-source-closure claim.

| Completed trial | Native / wrapper exit | Authority wall seconds | Saved result |
| --- | --- | ---: | --- |
| [infrastructure-applied-02][applied-infrastructure] | 0 / 0 | 77.3535471 | [Raw log][applied-infrastructure-log]: 109/109 runtime cases plus 18 generated-state cases / 143 assertions; fake-first operational evidence |
| [workstation-applied-03][applied-workstation] | 0 / 0 | 7.1343293 | [Raw log][applied-workstation-log]: 108 prerequisite assertions and subsequent operational checks passed |
| [packaging-inspect-applied-02][applied-packaging] | 0 / 0 | 0.9653562 | [Raw log][applied-packaging-log]: installed .NET/WiX/extensions/jpackage inventory passed; INSPECT ONLY |
| infra-benchmark-timeout-applied-01 | 0 / 0 | 77.6114955 | After finite-timeout correction: 110/110 runtime cases plus 18 generated-state cases/143 assertions; source61/environment unchanged; not an actual benchmark execution |
| [workstation-conda-prefix-applied-01][conda-identity-focus] | 0 / 0 | 7.5767061 | B18 correction: 150 assertions (108 retained + 42 across 16 Conda cases), then original matrix/ordering/temporary Git transaction; 61-source checkpoint unchanged; real bootstrap result remains separate |
| [workstation-native-padding-applied-01][native-padding-focus] | 0 / 0 | 6.9984691 | B19 correction: 158 assertions (108 + 42 Conda + 8 padding), then original matrix/ordering/temporary Git transaction; separate 61-source checkpoint unchanged; real bootstrap03 is reported separately in section 5.6 |

These stopwatch durations include fresh pwsh startup and streaming capture; they exclude preparation/evidence overhead and final summary publication. They are not bootstrap timings or performance-acceptance measurements. The latest timeout-focused measurement SHA-256 is `d59ebf539e64a9e509c549a5b2e6f9b485195d903f8d507844ab22d3935f842e`; its infrastructure result SHA is `d54cb612b81d6e03cc9f0e148636882afa42d96abc5fbea2b3fa37fb8b517d72`, state PASS_FAKE_FIRST_OPERATIONAL_ONLY. Earlier 109-case results remain associated with their original source versions.

The failures remain preserved: infrastructure-applied-01 exited 1/1 with 64/109 runtime cases passing; all 45 failures were Git 128/Filename too long inside disposable fixture repositories. Workstation-applied-01 exited 1/1 at the distinct-caller-shadow assertion; -02 exited 1/1 after the pure guard module leaked its fake Get-Item/New-Item/Start-Transcript. Neither was repaired by weakening production native capture or bypassing the guard. The fixture now uses an explicit child shadow scope, exports no dynamic-module functions/aliases, asserts that caller names still resolve to Cmdlets, executes negative cases through the module object and unloads it. The complete -03 pass includes that new no-leak assertion.

Packaging-inspect-applied-01 never launched the authority: native exit/start/end are null, wrapper exit is 2. Its instrumentation selected nonexistent packaging/windows/wix-extensions.json and also recorded a secondary empty-path raw-evidence error. The corrected -02 source list uses existing packaging/windows/NuGet.Config. This is not a host packaging or installation failure.

Infrastructure-applied-02 measured runtime-fixture SHA-256 `031c92112aa7981490911d0b82428a99b7a45c53d4e483631c20ab35894d4e17`: its retained TEMP root solved the report-depth failure. Subsequent fixture-only corrections preserve a root TEMP trailing separator and print the retained locator before evidence-directory creation. The later fixture SHA-256 is `72b68ec43d2852fd21354df46f54174fc2c371a6244a344e05d13300be7b044d`. FULL02 subsequently records that source in its 60-path checkpoint and its [infrastructure result][full02-infrastructure] reports both child exits 0: 109/109 runtime cases and 18 generated-state cases / 143 assertions. That summary's SHA-256 is `7fcc40d048677c1c29d7993d3a3d7120053f26a7a3c2bc1201495c32665c4240`; the earlier infrastructure-02 pass is not retroactively assigned to these later bytes. Workstation-applied-03 measured fixture SHA-256 `b9058ff4fbb2445f702e7a35dad05d0a1cc57df9a6e1a0ea95a0bc0a7ef10488`.

### 7.2 Product evidence and remaining completion slots

The [performance report][performance-report] records exact commands, time boundaries,
saved hashes and limits for DEV49 first/repeat, R6 PHASE, COMPOSED01, the shared
85-case and Desktop 34-case focuses, failed FULL01 and successful technical FULL02.
FULL02 covers 7,596 cases (7,585 PASS, 11 retained SKIP), four Checkstyles and the
composed/reference assertions at the 60-source checkpoint. The final root result,
not its earlier on-disk receipt marked phase-assertions-pending, establishes that
technical completion. The six original shared failures have explicit preserved
FAILED→PASSED relations; this is not a successful original exhaustive baseline.

The completed [DEV49 relation][dev49-relation] checks all 49 exact raw case/outcome
multiplicities, recorded source/status/index/environment context and arguments
except each own log directory. Both Test tasks executed freshly with the same
warm daemon. Combined with original-source cache/reuse controls, this supplies
bounded repetition evidence. It does not establish universal numerical determinism
or require two normal bootstrap executions. No new parallelism was enabled.

| Slot | State and evidence to fill |
| --- | --- |
| BOOTSTRAP_NORMAL_RESULT | **COMPLETED actual authority path** in bootstrap03: native/runner/nested 0, PASS WITH WARNINGS only SkipFetch; exact command, cohort, UTC/timing and saved final results in section 5.6. Original r3 envelope2 is not relabeled. No fetch/install/GUI claim. |
| BOOTSTRAP_PREFLIGHT_FACTS | **COMPLETED** in bootstrap03: effective Java 22, usable full JDK 17/25, exact named CPython 3.12.13/mpmath 1.4.1 origins; all 25 bootstrap-native records 0, final WorkstationFacts matches saved raw records. Earlier partial -01/-02 facts remain partial. |
| BOOTSTRAP_STATE_AND_DIAGNOSTICS | **AUDITED**: 64 source/instrument tuples, 61 status paths/index/environment preserved; 23 generated paths reported restored; transcript closed, no bootstrap finalization errors, only SkipFetch warning. No independent whole-generated-tree hash. Failed r3 envelope and missing original Link remain explicit. |
| BOOTSTRAP_COMPARISON | Complete workflow times are 9590.394 s original / 1514.5540847 s candidate outer, with changed source/preflight/output-cache/test scope and explicit capture boundaries. Separate archive-only UTC/link/raw-case reconciliation completed with exit 0, with 16 named additions and zero differences; six selected canonical summaries match FULL02 byte-for-byte. Observed context-qualified complete-workflow reduction 8075.8399153 s / 84.2076%, not a causal/cold/repeat or standalone COMPOSED comparison. |
| CLEAN_FULL_RESULT | **COMPLETED**: `after/clean-full-applied-01` native/measurement/root/environment exit0; outer 1770.961057 s/root 1770.661 s; source60 unchanged,7,596 cases/11retained skips, four Checkstyles executed,23 generated paths cleared and reported restored. No dependency-cache reset or new-workstation provisioning. |
| BENCHMARK_REPRO_AND_REPAIR | Unchanged direct default benchmark reproduced timeout 30: native/wrapper 1, 252.8287378 s, no benchmark JSON or instrumentation error. Suite timeout 600 and one additive contract fixture are applied; full quiet scope, repeat counts and informational 5000 ms budget remain. Focused runtime 110/110 plus generated-state 18 cases/143 assertions passed on source61; final01 delegation subsequently failed at precheck on B20 fixed-default evidence reuse; its failed result is preserved. Applied default-directory correction passed 111 runtime cases plus18 generated-state cases/143 assertions; replacement final02 remains **PENDING at this documentary checkpoint**. |
| FINAL_FULL_CI_PROFILE_RESULT | Attempt01 **FAILED**, root/native1 and no benchmark JSON; replacement attempt02 **PENDING**: `after/final-full-ci-profile-02`, `verify.ps1 -Level FULL -RunBenchmarks`, unique authority log/benchmark paths, default restoration and no measurement-added GRADLE_OPTS; final root plus its delegated three-measurement benchmark JSON |

A successful final FULL's call to the exact existing benchmark runner supplies the
successful integration check; no additional standalone successful benchmark is
required. The unchanged direct failure reproduction is preserved in
`after/operational-benchmark-timeout-repro-01`, measurement SHA-256
`47dd5b7ded75b640fa1f1e31c7ed6c59a6150bb34a37167919926c22637827cd`,
with 252.8287378 s total wall. No wall-minus 30 preflight estimate is justified.
The 600-second bound applies only to the four warm-up/measurement children; the
benchmark's synchronous preflight has no separate suite timeout. Including FULL's
initial operational step, the successful profile makes six direct operational
invocations. The workflow's 60-minute job limit includes checkout/provisioning and
is not enforced by the local measurement runner. This local functional profile is
not a remote workflow run, a provisioning-time guarantee, or a matched timing
pair with FULL02's retained-output/info profile.

Bootstrap03's actual host facts and diagnostics now fill their own records;
its separate artifact-layer failure remains preserved. Complete the final-source
slot only from its saved result. Preserve all earlier failures and distinguish
inspection, normal verification and explicitly authorized installation. Do not
substitute an estimate, fixture pass or another level.

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

## 8. Governance and finalization obligations

The applied candidate [verification-level specification][level-spec], section 8, records the single bootstrap-impact and verification-infrastructure review contract; prompts/guides reference it instead of duplicating normative text. Integration is not author approval, and FULL remains required for these infrastructure changes.

The required recorded outcome is either BOOTSTRAP IMPACT — UPDATED, with affected paths/rationale/validation, or BOOTSTRAP IMPACT — NO CHANGE REQUIRED, with the inspected assumptions and rationale. Review is mandatory when consumed prerequisites, toolchains, Gradle/build/verification entrypoints, numerical/generated-source or packaging assumptions change. Arbitrary bootstrap edits are not required after every phase. A keyword's presence does not establish substantive compliance.

This task's bootstrap/workstation scope records:

~~~text
BOOTSTRAP IMPACT — UPDATED
Affected paths: tools/bootstrap/bootstrap-windows.ps1; tools/bootstrap/install-packaging-prerequisites.ps1; tools/bootstrap/packaging-prerequisites.psm1; tools/bootstrap/workstation-prerequisites.psm1; tools/bootstrap/tests/workstation-prerequisites.tests.ps1; tools/agent/verify-workstation.ps1; coordinated tools/agent verification/generated-state integration.
Rationale and validation: source-derived early prerequisites, effective Java, named Conda/import-origin checks, reliable native/diagnostic outcomes, optional packaging isolation and current-run/output ownership; focused applied evidence is in section 7.1. Actual normal bootstrap03 is separately documented in section 5.6; its r3 envelope failure is retained. Focused checks do not supply replacement final02 FULL after the preserved final01 failure; the B20 default-only correction has the explicit no-change bootstrap rationale in section 5.7. Separate clean-output FULL remains recorded in section 7.2.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = README.md; docs/user/geocedg_user_guide.md; docs/developer/geocedg_developer_guide.md
~~~

The inspected guides describe the PowerShell 7.2 minimum, Java 17/22/25 roles, named numerical environment, bootstrap/delegated-level boundary and logging/installation workflow. The subsequent applied review qualifies technical acceptance by the required COMPOSED/FULL level, links the separate correctness prerequisites, and replaces stale README capability claims with the living roadmap/manual rather than duplicating product semantics. This declaration follows the existing [documentation-impact protocol][documentation-impact]; it neither claims tested PowerShell 7.2 nor certifies installer idempotence. This report-only proposal changes no guide; it records the coordinator's already applied operational guide updates.

The historical pre-final02 completion checklist, subsequently assessed by the
independent closeout review, was:

1. Reconcile completed original bootstrap/COMPOSED/FULL native, child-log and coverage ledgers, including failed FULL, missing unfiltered Desktop coverage and the isolated reproduction's unreliable native-exit field. Resolve or explicitly retain baseline failures under the coordinator's compatibility authority; do not relabel them as passed.
2. Keep the declared PowerShell 7.2 floor aligned across applied scripts/tests/docs; do not claim an executed 7.2 compatibility result.
3. Retain completed infrastructure/workstation/generated-state and inspect-only results, failed earlier trials and their source hashes; FULL02 now covers the later TEMP-only fixture corrections on its recorded checkpoint.
4. Reconcile completed DEV49 repetition, PHASE, COMPOSED, FULL02 and clean-output FULL with completed normal bootstrap03, its preserved envelope failure and the final-source CI-profile FULL record in section 7.2. Interactive/native-artifact gates require their explicit applicable contracts.
5. Confirm real Conda origins and actual JDK inventories in saved structured preflight evidence; preserve distinct failure/rerun logs.
6. Verify source/index/status preservation, recovery behavior, no stale evidence acceptance, no hidden test omissions and equivalent/stronger final scientific coverage.
7. Publish comparable before/after measurements including preflight/fingerprint/snapshot overhead; otherwise report non-comparability, not a percentage improvement.
8. Retain unresolved warnings, unavailable runtimes and unrun gates explicitly. No required failed/missing check is a PASS.

Remaining limitations include the failed/incomplete original FULL baseline (now followed by the separately documented shared/Desktop repairs and successful technical FULL02), the isolated control's native-exit capture defect, focused installer discovery outside default JDK locations, unexecuted real installation/upgrade/idempotence paths, possible filesystem check/use races, original sandbox failed-log overwrite, the retained B18/B19 candidate preflight failures, the preserved r3 timestamp-link failure despite successful bootstrap03, the final01 operational evidence collision, and the still-unexecuted replacement final02 result at this documentary checkpoint. Completed operational/inspection/product checks do not fill that final gate. Scientific references and product semantics are not changed by the bootstrap/workstation portion of this task; the separate bounded Java correctness changes are recorded in the [compatibility design][compatibility-design].

This report preserves implementation evidence; it is not an independent product-validation or approval authority. Native bootstrap03 success alone did not confer approval. The former IMPLEMENTATION_CANDIDATE_PENDING_AUTHOR_REVIEW status is superseded only by the explicit [author decision in ADR 0020][approval], without granting a new product phase or release permission.

## Source and evidence references

Relative references resolve from docs/validation/. The two original scripts are pinned to the inspected commit so later operational edits cannot change their cited meaning. Applied fixture/specification links name current durable paths; cited measurement hashes identify the actually executed source versions. Their presence does not establish author approval or publication.

Files under `artifacts/verification-performance-bootstrap/` are ignored local
generated evidence, not files published by the Git repository. Their relative
links require the preserved local artifact tree or a separately supplied
evidence bundle; repository access alone does not make those artifacts available.

[approval]: ../adr/0020-verification-levels-and-current-run-evidence.md#author-approval-and-closeout
[original-bootstrap]: https://github.com/mpradovelasco/GeoCeDG/blob/3942af594e4507e479f2c75019cef62e3d9fea6f/tools/bootstrap/bootstrap-windows.ps1#L32
[original-verify]: https://github.com/mpradovelasco/GeoCeDG/blob/3942af594e4507e479f2c75019cef62e3d9fea6f/tools/agent/verify.ps1#L19
[java-convention]: ../../source/build-logic/convention/src/main/kotlin/java-conventions.gradle.kts
[desktop-launcher]: ../../source/desktop/desktop/build.gradle.kts
[g7a-generator]: ../../geocedg/validation/locus-v2/g7a/generate_metric_references.py
[audit-notes]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap-audit-notes.md
[matrix]: ../../artifacts/verification-performance-bootstrap/inventory/execution-matrix.json
[bootstrap-transcript]: ../../artifacts/verification-performance-bootstrap/baseline/bootstrap-run1-transcript.log
[dev-log]: ../../artifacts/verification-performance-bootstrap/baseline/dev-r6-class-run1.log
[dev-info]: ../../artifacts/verification-performance-bootstrap/baseline/dev-r6-class-run2-info.log
[preparation]: ../../artifacts/verification-performance-bootstrap/proposals/preparation-checks.json
[generated-tests]: ../../tools/agent/tests/generated-state.tests.ps1
[level-spec]: ../../geocedg/specs/operations/verification-levels.md
[bootstrap-measurement]: ../../artifacts/verification-performance-bootstrap/baseline/bootstrap-run1-measurement.json
[full-summary]: ../../artifacts/verification-performance-bootstrap/baseline/full-run1/original-full-run-summary.json
[full-shared-log]: ../../artifacts/verification-performance-bootstrap/baseline/full-run1/shared-tests.log
[spreadsheet-repro-log]: ../../artifacts/verification-performance-bootstrap/experiments/original-full-spreadsheet-isolated-repro/gradle-output.log
[spreadsheet-repro-summary]: ../../artifacts/verification-performance-bootstrap/experiments/original-full-spreadsheet-isolated-repro/trial-result.json
[native-exit-audit]: ../../artifacts/verification-performance-bootstrap/inventory/native-exit-capture-audit.md
[applied-note]: ../../artifacts/verification-performance-bootstrap/inventory/applied-bootstrap-workstation-evidence.md
[applied-infrastructure]: ../../artifacts/verification-performance-bootstrap/after/infrastructure-applied-02/measurement-result.json
[applied-infrastructure-log]: ../../artifacts/verification-performance-bootstrap/after/infrastructure-applied-02/raw-output.log
[applied-workstation]: ../../artifacts/verification-performance-bootstrap/after/workstation-applied-03/measurement-result.json
[applied-workstation-log]: ../../artifacts/verification-performance-bootstrap/after/workstation-applied-03/raw-output.log
[applied-packaging]: ../../artifacts/verification-performance-bootstrap/after/packaging-inspect-applied-02/measurement-result.json
[applied-packaging-log]: ../../artifacts/verification-performance-bootstrap/after/packaging-inspect-applied-02/raw-output.log
[documentation-impact]: ../../geocedg/specs/operations/documentation-maintenance.md#5-documentation-impact
[performance-report]: verification_performance_report.md
[full02-infrastructure]: ../../artifacts/verification-performance-bootstrap/after/full-applied-02/authority/operational/verification-infrastructure/verification-infrastructure.json
[dev49-relation]: ../../artifacts/verification-performance-bootstrap/inventory/dev49-archived-pair-description.md
[compatibility-design]: ../architecture/verification-baseline-compatibility-repair.md

[conda-identity-diagnosis]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap-conda-prefix-identity-diagnosis.md
[conda-identity-focus]: ../../artifacts/verification-performance-bootstrap/after/workstation-conda-prefix-applied-01/measurement-result.json
[bootstrap01-probe]: ../../artifacts/verification-performance-bootstrap/after/bootstrap-applied-01/authority/20260903T001019343Z-21c84f652bf644ecb78d28daa4a627d8/preflight/013-conda-named-environment-version-and-import-origin-probe.log
[bootstrap02-jdk-inventory]: ../../artifacts/verification-performance-bootstrap/after/bootstrap-applied-02/authority/20260903T003322240Z-a1bcf8bd0c4c40cd8243fa499b69b5de/preflight/015-gradle-jdk-inventory.log
[native-padding-design]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap-native-output-padding-design.md
[native-padding-focus]: ../../artifacts/verification-performance-bootstrap/after/workstation-native-padding-applied-01/measurement-result.json
[bootstrap03-audit]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-final-workstation-audit.md
[bootstrap03-ledger]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-final-workstation-ledger.json
[bootstrap03-temporal]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-temporal-envelope-diagnosis.md
[bootstrap03-summaries]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap03-canonical-summary-comparison.json
[bootstrap03-measurement]: ../../artifacts/verification-performance-bootstrap/after/bootstrap-applied-03/measurement-result.json
[bootstrap03-result]: ../../artifacts/verification-performance-bootstrap/after/bootstrap-applied-03/authority/20260903T004314870Z-c7a569d25b5241ff97b521e91bccabe2/bootstrap-result.json
[bootstrap03-reconciliation]: ../../artifacts/verification-performance-bootstrap/inventory/bootstrap-temporal-reconciliation-runs/bootstrap03-reviewed-20260903-6e8ac3ba41d6465ebd6edcf2cf0df6f1/summary.json

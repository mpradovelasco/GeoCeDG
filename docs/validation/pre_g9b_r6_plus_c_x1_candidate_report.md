# PRE-G9B-R6-plus-C-X1 — Classic Graphics View as Picture on the EDT: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-C-X1
OBSERVATION               = OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = BOUNDED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C-X1
                            (on the exact candidate commit and tree)
INTEGRATION               = NOT RUN (not required by BOUNDED_PHASE)
FINAL                     = NOT RUN (not required by BOUNDED_PHASE)
PRODUCT CHANGE            = YES (one upstream Desktop file: the Classic Graphics View
                            as Picture action runs on the EDT instead of a worker)
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE
GUIDE_IMPACT              = NONE
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
AUTHOR_SMOKE              = REQUIRED (not performed or attributed by the agent)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval and for the disposition of the
observation. The candidate commit cannot name itself, so its identity and the
acceptance run made on it are reported outside this file. The machine-readable
mirror is
[`pre-g9b-r6-plus-c-x1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-C-X1` on 2026-10-06, confirmed in writing
in the session; the instruction is recorded in the
[C-X1 preparation closeout and authorization record](pre_g9b_r6_plus_c_x1_prompt_closeout_record.md)
and the [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt.prompt.md)
was amended to `AUTHORIZED FOR IMPLEMENTATION` as the first tracked edit of the
phase.

```text
P_R6PLUS_C            = 39eb05bc16a18ae48167383e90fcf38b12a681b6
                        tree fc13f5f125a4e8d595dd450bfd47706be193e5cd
                        (PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED — PUBLISHED; not reopened)
T_R6PLUS_C_X1_PROMPT  = 50baef73e784f764e95c9faf33d50188986ec3ed
                        tree 245a8a7a814dc7146a8c1cf32214019da6317999
                        (preparation candidate; BRANCH_START; kept as provenance)
authorization commit  = 7a88a491104ecd3c75809d0b84fd1e7dcb3c0968
                        tree 2034b225d3fe5f790f7459e44bc2cb998f3c6a59
                        (record, JSON mirror, prompt amendment; no product change)
implementation branch = phase/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt (local)
local main = origin/main = live remote main = 39eb05bc16a18ae48167383e90fcf38b12a681b6
```

The source at the branch start equals the characterized source: the
`exportGraphicAction` block of `FileMenuD.java` is the blob characterized at
`39eb05bc` (`FileMenuD.java` blob `0acde7ceec84ac39c95a31dc65953dec033faa3b`),
and `GuiManagerD`, `GraphicExportDialog` and `PrintScalePanel` are unchanged
since `39eb05bc`.

## 2. Product delta

One production file, one action body:

```diff
--- a/source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/FileMenuD.java
+++ b/source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/FileMenuD.java
@@ -361,21 +361,20 @@ class FileMenuD extends BaseMenu {
             @Override
             public void actionPerformed(ActionEvent e) {
-                Thread runner = new Thread(() -> {
-                    app.setWaitCursor();
-                    try {
-
-                        app.getGuiManager().showGraphicExport();
-
-                    } catch (Exception e1) {
-                        Log.debug(
-                                "GraphicExportDialog not available for 3D view yet");
-                        // for 3D View
-                        app.copyGraphicsViewToClipboard();
-                    }
-                    app.setDefaultCursor();
-                });
-                runner.start();
+                // GeoCeDG (2026-10-06): PRE-G9B-R6-plus-C-X1 the dialog is built and
+                // shown in this EDT dispatch, no longer on a worker thread
+                app.setWaitCursor();
+                try {
+
+                    app.getGuiManager().showGraphicExport();
+
+                } catch (Exception e1) {
+                    Log.debug(
+                            "GraphicExportDialog not available for 3D view yet");
+                    // for 3D View
+                    app.copyGraphicsViewToClipboard();
+                }
+                app.setDefaultCursor();
             }
```

With whitespace ignored, the delta is the removal of `Thread runner = new
Thread(() -> {`, `});` and `runner.start();` and the addition of the two-line
EUPL modification marker. The body, its order, `showGraphicExport()`, the
`catch (Exception)` branch with its `Log.debug` and
`copyGraphicsViewToClipboard()` fallback, and the cursor calls are unchanged.
The existing `FileMenuD.java` entry of `docs/upstream/modified-files.yml` names
the purpose. No second production file changed; `showGraphicExport()`, the
GeoCeDG v2 route, `GraphicExportDialog`, `GuiManagerD` and every export class
are untouched.

Invariant enforced (`INV-X1`): construction and presentation of the Classic
Graphics View as Picture dialog — reached through its File > Export menu item
or its Ctrl+Shift+U accelerator — occur on the Swing EDT, within the EDT
dispatch of the menu action. The normal supported invocation of
`exportGraphicAction` is the Swing action dispatch, on the EDT; no supported
route was found that enters it off the EDT (the menu item, its accelerator
through the key bindings, and the GeoCeDG v1 fallback menu are all Swing
dispatches).

**Compiled delta (`T-X1-BYTECODE`).** `:desktop:desktop:compileJava` was run
with the original and with the corrected `FileMenuD.java` (all else equal), and
every one of the 1 458 main class files was hashed (SHA-256 lists:
original `e2859b2aeef48d85d51df86c0faecbf5b2a40a8689afd8a9feddd669973da589`,
corrected `3e902329d21e361dca542444509e27b1295f88f575ecc038877fb53e0feab45d`; a
second corrected build reproduced its list). 1 447 class files are
byte-identical; the 11 that differ are all in the `FileMenuD` nest
(`FileMenuD`, `FileMenuD$12` to `FileMenuD$21`). Their `javap -c -p`
disassembly (code without line-number tables) is identical except for
`FileMenuD$12`, the Graphics View as Picture action: the other ten differ only
in line numbers, because the file is one line shorter after the action. In
`FileMenuD$12`, `actionPerformed` was `new Thread(this::lambda$actionPerformed$0).start()`
and the body lived in the synthetic `lambda$actionPerformed$0`; now
`actionPerformed` contains the same body and the synthetic method is gone. No
export, dialog, kernel, serialization or GeoCeDG class differs.

## 3. Author dispositions as implemented

| ID | As implemented |
|---|---|
| `DQ-X1-1` | identifier `PRE-G9B-R6-plus-C-X1`, registry phase id `PRE-G9B-R6-PLUS-C-X1` |
| `DQ-X1-2` | option A exactly (§2); no marshalling in `showGraphicExport()`; GeoCeDG v2 untouched |
| `DQ-X1-3` | `BOUNDED_PHASE`; selection `PRE-G9B-R6-PLUS-C-X1` = `compile.shared.semantic`, `compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-c-x1.semantic` |
| `DQ-X1-4` | the adjacent sites are not changed and are recorded once as `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES` in the mini-track plan §14 (§9) |
| `DQ-X1-5` | stop state `TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`, `AUTHOR_SMOKE = REQUIRED`; checklist in §10 |
| `DQ-X1-6` | the windowed process-level test is tracked and in the selection (`T-X1-PROCESS`, §4); it is deterministic and bounded and asserts ownership |

## 4. Tests

Two new Desktop test classes, both in the phase selection.

`PreG9BR6PlusCX1ClassicPictureDialogTest` (embedded Classic and GeoCeDG
applications; each scenario runs in its own JVM of the test classpath — see
"Heap" below — with a test-owned preference file set through the host's
`setPropertyFileName`, an in-memory GeoCeDG unit-preference store, the Desktop
logger, and Classic hosts whose clipboard fallback only records, so no run
touches the system clipboard):

| Test | Obligations |
|---|---|
| `classicMenuAndAcceleratorBuildAndShowTheDialogOnTheEdt` | One Classic application. `T-X1-OWNERSHIP`, `T-X1-POPULATED`, `T-X1-NO-EDT-EXCEPTION`, `T-X1-CANCEL`, `T-X1-REPEAT`: 5 openings through the real `FileMenuD` item (items built through `menuSelected`); each dialog showing when the menu dispatch returns, valid, laid out and painting non-blank, closed by Cancel. `T-X1-CLASSIC-NO-GEOCEDG-UI` at every opening: `getExportScalePresentation() == null`, the three host mode labels, none of GeoCeDG's non-physical wording (`ExportScale.DeviceShort`, `DeviceStatement`, `DeviceCm`, `DeviceFixed`, `DevicePixels` from the GeoCeDG profile) and no `org.geocedg` component. `T-X1-ACCELERATOR`: the item's accelerator is Ctrl+Shift+U and its action — the one the key bindings invoke from a text field — opens a sixth dialog under the same invariant. Over the 6 openings: every container and hierarchy event of the dialog on the EDT, every lifecycle stage (constructor, `initGUI`, `pack`, `loadPreferences`, `updateComponentTreeUI`, `show`) observed on the EDT at least once per opening, no off-EDT repaint of the dialog, no uncaught EDT exception. Cancel saves the host preference keys and values (`300`, `png`). `T-X1-NO-SERIALIZATION`: the document XML outside the `<gui>` window record and the saved state are unchanged |
| `anExceptionOfTheDialogStillReachesTheUnchangedFallback` | `T-X1-FALLBACK`: a Classic host whose `showGraphicExport()` throws reaches the unchanged `catch` branch once, synchronously, on the EDT; the clipboard method is overridden to record only; the default cursor is restored; no dialog |
| `geocedgPictureRouteIsUnchanged` | `T-X1-GEOCEDG`: the GeoCeDG v2 `host.export.picture` action opens a populated dialog synchronously on the EDT with its own scale presentation and its own labelled device modes, different from the host modes; the same ownership invariant |

`PreG9BR6PlusCX1ClassicPictureProcessTest` (`T-X1-PROCESS`): a child JVM of the
test classpath with the product's `--add-exports`, launching `GeoGebra3D.main`
with a test-owned settings file — the Classic diagnostic as GeoCeDG starts it —
and asserting, through the real menu bar of the real Classic frame: 5 menu
openings, 2 Ctrl+Shift+U openings from the input-bar text field (the global
dispatcher declines, the menu accelerator fires) and 1 from the graphics view
(the global dispatcher consumes it); each dialog showing when its dispatch
returns, populated, with the host scale modes, and closed by Cancel; the
ownership recorder of the in-process test over the whole run; no uncaught EDT
exception; an EDT watchdog (30 s stall or 200 s total → lock-aware dump, halt);
a bounded parent wait of 240 s. Two deliberate harness choices, stated in the
class: the child runs `GeoGebra3D.main` on the EDT so that the independent,
pre-existing off-EDT Classic startup (§9) cannot perturb the assertions, and a
ByteBuddy advice (the `byte-buddy-agent` jar already on the test classpath)
replaces the host clipboard fallback inside the child by a counter, so no run
can touch the system clipboard. The child record is written into the JUnit
report.

Both classes **fail on the original `FileMenuD`** and pass on the candidate
(the original blob `0acde7ce` was restored temporarily in the worktree and the
candidate blob `db533f5d` put back afterwards):

| Run | Dialog test (3 tests) | Process test (1 test) |
|---|---|---|
| original `FileMenuD` | 2 failures, the two Classic scenarios: "cycle 1: the dialog is showing when the menu dispatch returns" and "the catch branch ran once, synchronously, on the EDT ==> expected `[true]` but was `[false]`" (the fallback ran on the worker); the GeoCeDG scenario passes, its route being already correct | 1 failure: "menu cycle 1: the dialog is showing when the dispatch returns" |
| candidate | 3 passed | 1 passed: 8 openings, `clipboardFallback=0` |

**Heap.** A first version of the dialog test ran its scenarios inside the test
JVM. With it, two complete Desktop runs (the `final.desktop` producer and a
scratch run with positional heap probes) ended with `OutOfMemoryError: Java
heap space` in `org.geogebra.desktop.gui.util.SvgLoadTest.testLoadSvgsGGB`,
while the same tree without the two new classes passed its complete Desktop
run. The positional probes measured about 4 MB retained by the in-process
scenarios in the full-suite context (402 → 406 MB; one more `AppGeoCeDG` and
two frames reachable), enough to exhaust the small headroom of the pre-existing
Desktop test-heap pressure. The failure was therefore attributable to the
candidate's tests and was removed, not recorded as debt: every scenario now
runs in a child JVM, and the probes measure no retention in the test JVM
(9 MB before and after the two classes, the level of a fresh JVM). The
pre-existing heap pressure itself is out of scope and unchanged.

The selection also carries the regressions `PreG9BR6PlusBExportSurfaceTest`
(the GeoCeDG export surface and the v1 fallback `FileMenuD` entries),
`PreG9BR6PlusCBaseIdentityTest` (Classic host seams and the byte identity of
Classic picture and LaTeX output with the pre-`C` base) and
`G9U1ActionRegistryTest` (GeoCeDG action routing).

`T-X1-BYTECODE` of the prompt is delivered as pre-freeze evidence (§2), not as a
tracked test: a tracked class-hash fixture would pin compiler- and JDK-specific
bytes of unrelated classes. Output-format invariance is mechanized by
`PreG9BR6PlusCBaseIdentityTest` in the selection.

## 5. Repetition evidence

The scratch probe of the preparation (one child JVM per run, ByteBuddy tracing
of the dialog lifecycle, AWT listener, thread-checking `RepaintManager`,
watchdog, clipboard fallback skipped) ran on the candidate tree with the JDK 25
runtime of the product. Its mode `CLASSIC_MENU` is now the product action
itself. 55 JVMs, 5 openings each:

| Batch | Mode | JVMs | Opens | Populated | Cancelled | Uncaught EDT | Off-EDT dialog operations | EDT/worker concurrency | Hang |
|---|---|---|---|---|---|---|---|---|---|
| natural | `CLASSIC_MENU` | 20 | 100 | 100 | 100 | 0 | 0 | 0 | 0 |
| stress amplifier | `CLASSIC_MENU` | 10 | 50 | 50 | 50 | 0 | 0 | 0 | 0 |
| author settings copy | `CLASSIC_MENU` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 |
| accelerator, text field | `CLASSIC_KEY_TEXT` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 |
| accelerator, graphics view | `CLASSIC_KEY` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 |
| Metal | `CLASSIC_MENU_METAL` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 |
| GeoCeDG v2 | `GEOCEDG_MENU` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 |

Every recorded lifecycle stage ran on `AWT-EventQueue-0`; the traces show
`FileMenuD$12.actionPerformed → GuiManagerD.showGraphicExport` on the EDT; no
record contains `edt=false`. Before the correction the same probe recorded, at
`P_R6PLUS_C`, 4 100 off-EDT dialog operations, 2 425 concurrent EDT events and
51 uncaught NPEs over the same 20 natural JVMs (characterization report §5).
Identities: campaign driver `campaign-x1.ps1`
`89fda26b59c1f83a94864afd1888972761a22ad4a4261639b7cc86748d4a684f`, probe source
`15b3bdd46218e7d3fce0a4be0eedf4892eef45b9b70ad8c4ce5b4e8d99ed95ad` (unchanged
from the preparation), aggregate
`dc665a953b01538a04f36b3157cd078f79b237c59b8e4e324a9793a215603b4b`.

Finite repetition does not prove the absence of every hang.

## 6. Classic and GeoCeDG

| Route | Before | Candidate |
|---|---|---|
| Classic File > Export > Graphics View as Picture | worker thread | EDT, synchronous |
| Classic Ctrl+Shift+U from a text field (menu accelerator) | worker thread | EDT, synchronous |
| Classic Ctrl+Shift+U from the graphics view (`GlobalKeyDispatcher`) | EDT | EDT (unchanged code) |
| GeoCeDG v2 `host.export.picture` | EDT | EDT (unchanged code) |
| GeoCeDG v1 legacy fallback (Classic `FileMenuD`) | worker thread | EDT (same action; no new semantic authority) |

Classic keeps the host dialog: `getExportScalePresentation() == null`, the host
modes `Scale in cm:` / `Fixed Size:` / `Size in pixels:`, no GeoCeDG
non-physical wording and no `org.geocedg` component. GeoCeDG v2 keeps its own
presentation (`Device scale (non-physical):` … and the non-physical statement
for a document without a construction unit).

Visible difference in Classic: the wait cursor set before construction is not
painted while the EDT builds the dialog, as on the GeoCeDG v2 route (about
0.22 s for the first opening of a session, about 0.02 s afterwards).

## 7. Residual uncertainty

```text
PROVEN         the Classic launch violated Swing EDT ownership, and the correction
               eliminates that race (deterministic tests on the candidate fail on the
               original; 0 off-EDT dialog operations in 275 traced openings)
NOT YET PROVEN the race is the sole necessary cause of every manually observed
               empty-dialog/hang event
```

The author's severe variant was never reproduced automatically, before or
after the correction; its mechanism is not established, and the probes do not
move the real pointer over a newly created native window. The author smoke is
the evidence for that symptom.

## 8. Unchanged semantics

Kernel geometry, DAG and CeDG spatial semantics, Locus V2, Spline V2,
`constructionUnit`, `presentationUnit`, `drawingScale`, `ExportArea`, DXF,
LaTeX and EMF semantics, document identity, serialization, undo/redo, the
modified state and every output format or schema are untouched: the only
product delta is the thread on which an unchanged action body runs. The
in-process test shows the document XML and saved state unchanged by opening
and cancelling; `PreG9BR6PlusCBaseIdentityTest` keeps the byte identity of
Classic outputs; the bytecode list of §2 confines the compiled delta to the
action class.

## 9. Observations

- `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` stays open; it is resolved only by
  the author's disposition after smoke.
- Recorded, not fixed (`DQ-X1-4`), as `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES`
  in the mini-track plan §14: the Classic startup through `GeoGebra.doMain`
  without the EDT (one startup failure in 100 Classic launches during the
  preparation; none in the 55 post-correction launches); `GeoGebraMenuBar.showPrintPreview`
  (also the GeoCeDG v2 Print Preview); the Save and Clipboard buttons of
  `GraphicExportDialog` (Classic and GeoCeDG); the worker-thread actions
  `newWindowAction`, `drawingPadToClipboardAction`, `exportWorksheet` and
  `exportGeoGebraTubeAction` of `FileMenuD`.
- Each opening still creates a new dialog that Cancel hides without disposing
  (pre-existing, unchanged, not a launch-lifecycle defect).
- New during the phase, resolved inside it: an in-process version of the
  dialog test tipped the pre-existing Desktop test-heap pressure into an
  `OutOfMemoryError` in `SvgLoadTest` (§4, "Heap"); the scenarios now run in
  child JVMs. The heap pressure itself (Gradle's 512 MB default, ~400 MB
  retained by the existing suite) is unchanged, pre-existing and out of scope.
- No new independent defect was found. The occasional Classic startup failure
  recorded by the preparation did not occur in the 55 post-correction
  launches; it remains under `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES`.
- `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`,
  `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` and
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` are unchanged.

## 10. Author-smoke checklist (not performed or attributed by the agent)

1. From GeoCeDG, open the Classic diagnostic application. File → Export →
   Graphics View as Picture: the dialog is populated, the application stays
   responsive, Cancel closes the dialog; no empty dialog and no hang. Repeat
   several times with the real mouse, including moving the pointer over the
   dialog while it appears.
2. In the Classic application, Ctrl+Shift+U from the input bar and from the
   graphics view: the same result.
3. Save a PNG and an SVG from the Classic dialog.
4. Quick GeoCeDG regression: Graphics View as Picture in GeoCeDG opens its
   dialog with the GeoCeDG scale presentation; Cancel; save a PNG.
5. The Laboratory application, if the author uses it.

## 11. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence,
not acceptance. The acceptance run on the frozen candidate is reported with
the candidate, outside this file.

| Run | Result |
|---|---|
| focal `PreG9BR6PlusCX1ClassicPictureDialogTest` + `PreG9BR6PlusCX1ClassicPictureProcessTest`, candidate | 3 + 1 tests, 0 failures/errors (also after the Checkstyle fix) |
| the same, original `FileMenuD` blob restored temporarily | 2 + 1 failures (§4); candidate blob `db533f5d` restored afterwards |
| post-correction repetition probe (55 JVMs, JDK 25) | §5 |
| bytecode lists, original and candidate builds | §2 |
| positional heap probes (`PreG9BR6PlusCX0HeapProbeTest`, `PreG9BR6PlusCX2HeapProbeTest`, scratch only) | in-process version: 402 → 406 MB in full-suite context; child-JVM version: no retention (9 → 9 MB) |
| complete Desktop runs, in-process version | 2 runs, `OutOfMemoryError` in `SvgLoadTest.testLoadSvgsGGB`; same tree without the two new classes: passed (§4, "Heap") |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` (XML reports) | main: no finding; test: no finding in the new classes after making two captured values `final`; the remaining `PreG9BR6PlusA1HiddenLayerTest.java:188` is pre-existing |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` | OK, 966 registered files (the two new test classes registered as `added`) |
| `git diff --cached --check` | clean |
| producer `discovery.shared` (`--test-dry-run`) | completed; 6 008 identities, unchanged |
| producer `discovery.desktop` (`--test-dry-run`) | completed; 1 842 identities |
| producer `pre-g9b-r6-plus-c-x1.desktop` (executed) | 41 tests, 0 failures/errors |
| producer `final.desktop` (executed, complete Desktop suite outside the isolated partition) | 1 836 cases: 1 835 passed, 1 skip (the pre-existing allowlisted one), inner exit 0 |
| development `INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, `verification-cfaef23e25e44521b9603aab5615499b` |
| development `STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3, `verification-720f0d6c5cd44c0ab19614d86b3f05d2`; the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; documentation, guide-structure, style and whitespace diagnostics clear |

**JUnit inventory and pins.** The current `junit_inventory` pin was first
reproduced from the `HEAD` blob with `Get-VerificationCanonicalTextSha256`
(`3aa283ca…`, equal to the registry). The selection
`pre-g9b-r6-plus-c-x1.desktop` was added with a placeholder count and the input
identity of the branch start (`50baef73`, tree `245a8a7a`).
`tools/agent/update-verification-junit-inventory.ps1`, run in session with
absolute paths, wrote every count and hash from the discovery evidence
(`discovery.shared`, `discovery.desktop`) and the executed evidence
(`pre-g9b-r6-plus-c-x1.desktop`, `final.desktop`): Desktop discovery 1 838 →
1 842 (`8de5d2f3…`), `final.desktop` 1 832 → 1 836 (`8c42236c…`), the new
selection 41 (`c9723f96…`); shared identities unchanged. The registry pin
`junit_inventory` is now `860d1fcd…`; `static_contracts` is unchanged. No
identity hash was typed by hand. The registry-shape pins of
`tools/agent/tests/verification-final-coverage.Tests.ps1` follow: 39
selections, 46 PHASE selections and the new selection count 41.

Scratch hygiene: the probe and heap-probe classes were compiled only through
Gradle init scripts that pointed at the session scratchpad; later builds
without them removed their classes, no stash copy remained, and nothing was
written under `artifacts/`.

## 12. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes; the process test uses the test JVM
and the byte-buddy-agent jar already on the test classpath.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: one registered PHASE selection and one Desktop JUnit selection through
the existing registry, inventory and updater; registry-shape pins in
tools/agent/tests follow the R5-A/C precedent; no verifier, schema or
profile-composition semantics change.
GUIDE_IMPACT = NONE
Rationale: the user and developer guides do not describe Classic threading;
no visible feature, label or workflow changes.
SERIALIZATION CHANGE = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE
```

## 13. Changed paths

- Product: `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/FileMenuD.java`.
- Provenance: `docs/upstream/modified-files.yml` (purpose of the existing `FileMenuD.java` entry; two `added` entries for the new test classes).
- Tests: `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusCX1ClassicPictureDialogTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusCX1ClassicPictureProcessTest.java`.
- Verification registration: `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Documentation and evidence: `docs/roadmap/geocedg_roadmap.md`, `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, `docs/validation/pre_g9b_r6_plus_c_x1_candidate_report.md`, `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-candidate-evidence.json`.

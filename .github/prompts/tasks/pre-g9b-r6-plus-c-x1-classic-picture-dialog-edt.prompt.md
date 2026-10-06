# PRE-G9B-R6-plus-C-X1 — Classic Graphics View as Picture launched on the EDT

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-06. That
instruction authorized a bounded post-`C` characterization, design and
implementation-preparation activity for the accepted pre-existing debt
`OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`, named the published `C` baseline
below, fixed the required probes, the architectural rule, the forbidden scope
and the stop conditions, and authorized only documentary and diagnostic
preparation: a characterization report, its machine-readable mirror, this
prompt in the `PREPARED — NOT AUTHORIZED` state and the minimal roadmap and
mini-track registration. It stated that it **does not authorize the product or
upstream fix**. The evidence behind the characterization below, and the inputs
fixed by that instruction, are in the
[C-X1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_c_x1_preparation_characterization_report.md).
The existence of this file is not authorization.

The identifier `PRE-G9B-R6-plus-C-X1` is **proposed by the preparation, not
author-approved** (`DQ-X1-1`). It follows the `PRE-G9B-R3-X1` precedent of an
`X` activity that resolves debt left open by a closed phase, and avoids the
`-R<n>` suffix, which this repository uses for revisions of the same phase
(`T_R6PLUS_C` is itself revision 1 of `C`). If the author chooses another
identifier, the authorizing instruction may rename this file and every
reference in the same amendment.

Execution requires a new explicit author instruction that names the activity
and the exact prepared candidate or base, and that disposes of the requested
decisions `DQ-X1-1` to `DQ-X1-6`. That instruction may authorize, as the first
tracked edit of the phase, an amendment of this prompt to the authorized state,
following the `A-1`, `B`, `D0`, `D1`, `A-2` and `C` precedent. This file is an
execution contract, not a second policy document: the verification classes are
defined once in `geocedg/specs/operations/verification-levels.md` §12.8, the
debt disposition once in the
[C closeout record](../../../docs/validation/pre_g9b_r6_plus_c_closeout_record.md),
and the probe evidence once in the characterization report; this prompt cites
them and does not restate them differently.

```text
PRE-G9B-R6-plus-C-X1 =
PREPARED — NOT AUTHORIZED

identifier               = PROPOSED — NOT AUTHOR APPROVED (DQ-X1-1)
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = UPSTREAM DESKTOP LIFECYCLE CORRECTION — ONE CLASSIC MENU
                           ACTION; NO GEOMETRY, NO SERIALIZATION, NO EXPORT SEMANTICS
DEPENDS_ON               = PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED — PUBLISHED
                           (P_R6PLUS_C 39eb05bc; the debt was accepted at its closeout)
RESOLVES                 = OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT (launch lifecycle
                           only; final disposition is the author's, after smoke)
PRECONDITION             = author dispositions of DQ-X1-1 to DQ-X1-6
NEXT_SUBPHASE            = none implied; E1, E2, E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this activity has
been author-approved. Technical verification never creates author approval.
Once authorized, the activity stops with one exact technically verified
candidate pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only:

1. in the upstream Classic `FileMenuD`, the Graphics View as Picture action
   (`exportGraphicAction`) runs its unchanged body directly in
   `actionPerformed`, which Swing calls on the event dispatch thread, instead
   of in a newly started `Thread`;
2. the EUPL modification marker on the changed lines and the updated purpose of
   the existing `FileMenuD` entry in `docs/upstream/modified-files.yml`;
3. the tests, phase registration and evidence of its class.

The lifecycle invariant the candidate must establish and test:

```text
INV-X1  For the Classic Graphics View as Picture action of FileMenuD — reached
        through its File > Export menu item or through that item's Ctrl+Shift+U
        accelerator — construction, component population, layout (pack and
        validate), preference loading (including the format listener's
        updateComponentTreeUI) and presentation (setVisible) of the
        GraphicExportDialog occur on the event dispatch thread, within the
        EDT dispatch of the menu action.
```

The wording is supported by the evidence for that action only. It is not a
claim about any other Swing call site; the adjacent same-pattern sites are
recorded, not changed (`DQ-X1-4`).

```text
before  exportGraphicAction.actionPerformed (EDT)
          └─ new Thread(...).start()          ← returns immediately
               └─ (worker) setWaitCursor; showGraphicExport(); setDefaultCursor
                         catch (Exception) → Log.debug; copyGraphicsViewToClipboard

after   exportGraphicAction.actionPerformed (EDT)
          └─ setWaitCursor; showGraphicExport(); setDefaultCursor
             catch (Exception) → Log.debug; copyGraphicsViewToClipboard
```

### Authorities and author decisions this prompt implements

- The author disposition at the `C` closeout (2026-10-06):
  `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` = reproduced by the author,
  pre-existing, not caused by `C`, accepted debt, not `C`-blocking, deferred to
  a separate post-`C` activity with its own characterization and authorization
  ([C closeout record](../../../docs/validation/pre_g9b_r6_plus_c_closeout_record.md)).
- The author's preparation instruction of 2026-10-06 (recorded in the
  characterization report §0): upstream Desktop lifecycle correction, smallest
  host-compatible fix, no GeoCeDG semantics in Classic, no export-semantic,
  serialization, kernel or dialog-design change.
- `AGENTS.md` §3.1 (minimum necessary upstream changes, justified and
  recorded), §7 (modification marking) and §16.

### Architecture decision: where the fix lives

The defect is a thread-ownership violation at **one call site**: the Classic
menu action hands a Swing dialog lifecycle to a worker thread. The dialog
class, `GuiManagerD.showGraphicExport()` and the export code are correct when
called on the EDT, as the GeoCeDG route, the Classic `GlobalKeyDispatcher`
route and the scratch prototype show (characterization X5, X6, X8). The owner
of the defect is therefore the call site, and the fix belongs there.

| Option | Shape | Disposition |
|---|---|---|
| **A — call site (recommended)** | remove `new Thread`/`start()` in `exportGraphicAction`; the body is unchanged | smallest; matches the actions of the same menu that open their UI directly on the EDT (Animated GIF, PSTricks, PGF/TikZ, Asymptote, Open, Save, Save As), the Classic `Ctrl+Shift+U` dispatcher route and the GeoCeDG v2 route to the same method; GeoCeDG v2 untouched |
| B — callee self-marshal | `GuiManagerD.showGraphicExport()` re-posts itself to the EDT when called off it (`invokeAndWait` or `invokeLater`) | rejected: changes a shared host method that the GeoCeDG v2 route, the Classic dispatcher and `GuiManagerInterface` callers use, against "GeoCeDG unchanged"; `invokeAndWait` from a worker adds a lock-ordering hazard; `invokeLater` changes the synchronous contract and moves exceptions away from the existing `catch`; no other off-EDT caller exists (X1) |
| C — worker plus `invokeLater` | keep the thread, post the body to the EDT | rejected: equivalent to A with an extra hop and a changed exception path |
| D — dialog redesign or a Classic-specific abstraction | — | forbidden by the instruction |

Option A is the only option compatible with every constraint of the
instruction. `DQ-X1-2` asks the author to confirm it; the preparation does not
select it on the author's behalf.

### Characterization results at the base

Evidence, commands and raw records are in the characterization report; this
section states the conclusions the contract relies on.

#### X1. Execution path and thread ownership

| Stage | Code | Thread before | Thread after (A) |
|---|---|---|---|
| menu action | `FileMenuD.exportGraphicAction.actionPerformed` (`FileMenuD.java:356-380`) | EDT | EDT |
| worker | `new Thread(...)` / `runner.start()` (`:364`, `:378`) | `Thread-N` | — |
| `showGraphicExport()` | `GuiManagerD.java:2963-2970`: `clearSelectedGeos`, `updateSelection`, `new GraphicExportDialog`, `setVisible(true)` | `Thread-N` | EDT |
| constructor | `GraphicExportDialog(AppD, EuclidianViewD)` (`GraphicExportDialog.java:148-162`), `super(app.getFrame(), false)`, `checkBrailleFont()` | `Thread-N` | EDT |
| population | `initGUI()` (`:217-410`) | `Thread-N` | EDT |
| pack | `centerOnScreen()` → `pack()` (`:638-642`); creates the native peers | `Thread-N` | EDT |
| preference load | `setVisible(true)` → `loadPreferences()` (`:205-214`, `:481-517`); `cbFormat.setSelectedIndex` fires the format listener, which runs `SwingUtilities.updateComponentTreeUI(p)` (`:304-350`) on the displayable dialog | `Thread-N` | EDT |
| show | `super.setVisible(true)` | `Thread-N` | EDT |
| Cancel | `setVisible(false)` → `savePreferences()` (`:362`) | EDT | EDT |
| Save / Clipboard | own `new Thread` → `setVisible(false)`; `doExport` (`:364-381`) | `Thread-N` | unchanged (out of scope, `DQ-X1-4`) |

Other routes to the same dialog: Classic `Ctrl+Shift+U` with the graphics view
focused goes through `GlobalKeyDispatcher` (`GlobalKeyDispatcher.java:801-805`)
and is already on the EDT; with a text field focused, `GlobalKeyDispatcherD`
ignores the event (`GlobalKeyDispatcherD.java:82`) and the menu accelerator
(`FileMenuD.java:162`) fires the threaded action. GeoCeDG v2 reaches
`showGraphicExport()` through `GeoCeDGActionRegistry` (`:336-337`) on the EDT.
GeoCeDG in the v1 legacy fallback builds the Classic `GeoGebraMenuBar`
(`GuiManagerGeoCeDG.java:34`) and therefore the same threaded `FileMenuD`
action.

#### X2. Modal and lifecycle facts

`GraphicExportDialog` is **modeless** (`super(app.getFrame(), false)`), owned by
the main frame. No secondary event loop is involved. Cancel hides the dialog
and does not dispose it; each launch creates a new dialog (one hidden window
retained per launch). These facts are unchanged by option A.

#### X3. The race

Off the EDT, `pack()` makes the dialog displayable; the EDT then processes the
dialog's window events and validates it (`Window.dispatchEventImpl` →
`validate`) while the worker is still inside `loadPreferences()` →
`updateComponentTreeUI` re-installing the combo-box UIs. Both threads mutate
the same component tree without synchronization. The recorded consequences are
EDT `NullPointerException`s in `BasicComboBoxUI.getDisplaySize` /
`DefaultListCellRenderer` (`list` is null) and in `BoxLayout` /
`SizeRequirements` (`total` is null), each captured while the worker was inside
`GraphicExportDialog.loadPreferences`.

#### X4. Causality of the severe variant

The automated campaign reproduces the race and its EDT exceptions on the
threaded route only, and never on the EDT routes. It did not reproduce the
author's "empty dialog + hang". The connection between the severe variant and
the off-EDT launch is a technical inference, not an automated reproduction: it
is the only Classic-specific difference from the routes that never fail, Swing
gives no guarantee for components mutated off the EDT, and an un-laid-out
displayable dialog is a direct outcome of an aborted EDT validation. The
remaining uncertainty is recorded in the report and in `DQ-X1-5`.

#### X5. FlatLaf

The exceptions are thrown in `javax.swing.plaf.basic` code that FlatLaf
delegates to; under `MetalLookAndFeel` the same off-EDT ownership, the same
EDT/worker concurrency and the same `list is null` exception occur. FlatLaf
exposes the violation; it is not its cause. No FlatLaf change, upgrade or setting is part of this activity.

#### X6. Scratch prototype

Executing the unchanged body directly on the EDT through the real File menu
item removed, in every run, the off-EDT dialog work, the EDT/worker concurrency
and the uncaught EDT exceptions; every dialog opened populated and closed on
Cancel. Under a synthetic EDT-validation amplifier the prototype stayed clean
while the threaded route kept failing. Absence of failure in those runs is
supporting evidence, not proof; the invariant `INV-X1` is the proof obligation.

#### X7. Base comparison

`FileMenuD`'s `exportGraphicAction` and `GuiManagerD.showGraphicExport()` are
byte-identical in launch semantics at the pinned upstream baseline
`9b93256b`, at the pre-`C` base `e6135028` and at `P_R6PLUS_C`; `C` changed
only the sizing and export bodies of `GraphicExportDialog`/`PrintScalePanel`,
and Classic takes the host defaults of every `C` seam. The defect is
pre-existing and `C` did not alter it.

#### X8. Upstream

The current upstream GeoGebra `main` (`a9ad4789`, 2026-10-05) still launches the
dialog from `new Thread`; its post-baseline commits to these files are
formatting and lint changes. No later upstream correction exists to adopt, so
no upstream provenance conflicts with the pin.

### Decisions requested before authorization

| ID | Question | Preparation recommendation |
|---|---|---|
| `DQ-X1-1` | Canonical identifier of the activity | `PRE-G9B-R6-plus-C-X1`, registry phase id `PRE-G9B-R6-PLUS-C-X1` |
| `DQ-X1-2` | Repair option | option A (call site), exactly as in *Objective* |
| `DQ-X1-3` | Verification class | `BOUNDED_PHASE`, planned acceptance one registered `PHASE`; no `INTEGRATION`, no `FINAL` |
| `DQ-X1-4` | Adjacent same-pattern sites stay out of scope: the dialog's own Save/Clipboard threads (shared with GeoCeDG); `GeoGebraMenuBar.showPrintPreview`, which builds and shows `PrintPreviewD` on a worker thread for Classic and for the GeoCeDG v2 `host.document.print-preview` action; the threaded `newWindowAction`, `drawingPadToClipboardAction`, `exportWorksheet` and `exportGeoGebraTubeAction` of `FileMenuD`; and the Classic application startup, which `GeoGebra3D` runs off the EDT through the upstream `GeoGebra.doMain` overload (`initializeOnEventDispatchThread = false`; GeoCeDG passes `true`), observed once by the probes as a startup layout failure | keep out of scope; record them as observed adjacent debt for a separate disposition |
| `DQ-X1-5` | Closure rule for the observation | the candidate may record "launch race eliminated (automated)"; the observation becomes resolved only after an author smoke that no longer reproduces the empty dialog and hang |
| `DQ-X1-6` | Status of the process-isolated windowed regression test | tracked, inside the registered phase selection, asserting the deterministic ownership invariant rather than the absence of a random exception |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_C = 39eb05bc16a18ae48167383e90fcf38b12a681b6
tree       = fc13f5f125a4e8d595dd450bfd47706be193e5cd
```

The implementation branch starts from the exact commit the authorizing
instruction names: either `P_R6PLUS_C` or the preparation candidate that
contains this prompt (documentary only, no product delta). Entry gate: local
`main`, `origin/main` and the live remote `main` agree with the named base, the
worktree is clean, and `C` is still `PASS — AUTHOR APPROVED — PUBLISHED`.

## Authority and evidence hierarchy

1. `AGENTS.md`, then current code, build and tests at the base.
2. `geocedg/specs/operations/verification-levels.md` and the typed registry.
3. The author instructions: the `C` closeout disposition and the authorizing
   instruction of this activity.
4. The characterization report and its JSON mirror (evidence, not authority).
5. Earlier scratch probe outputs, screenshots and session notes (evidence of
   the lowest rank; never a substitute for re-establishing a fact).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Upstream Desktop (one file)

- `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/FileMenuD.java`:
  only the body of `exportGraphicAction.actionPerformed`. The thread is
  removed; `setWaitCursor`, `showGraphicExport`, the `catch (Exception)`
  branch with its `Log.debug` and `copyGraphicsViewToClipboard` fallback, and
  `setDefaultCursor` are kept in order and unchanged. A `GeoCeDG (date):`
  modification comment, in the style of the file's existing markers.

### Supporting changes

- `docs/upstream/modified-files.yml`: the existing `FileMenuD.java` entry's
  purpose extended to name this activity (no new entry; the path is already
  registered by `B`).
- New Desktop tests under `source/desktop/desktop/src/test/java/org/geocedg/desktop/`
  and their registration (*Required tests and commands*).
- The phase selection, the JUnit inventory update through the official
  mechanism, and the registry-shape pins that registration moves.
- The candidate report, its machine-readable evidence, and the roadmap and
  mini-track status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- `GraphicExportDialog`, `PrintScalePanel`, `GuiManagerD`, `GeoGebraMenuBar`,
  `GlobalKeyDispatcher(D)`, `AppD`, `GeoCeDGActionRegistry`, `AppGeoCeDG` and
  every export, picture, print, LaTeX or DXF class: no change.
- The dialog's Save and Clipboard worker threads, `GeoGebraMenuBar.showPrintPreview`,
  the other threaded actions of `FileMenuD`, and the Classic startup thread of
  `GeoGebra.doMain`/`GeoGebra3D` (`DQ-X1-4`).
- Any GeoCeDG-specific semantics, seam or state in Classic; routing Classic
  through the GeoCeDG export-scale, unit or `drawingScale` state.
- Kernel geometry, construction semantics, serialization, preferences format,
  undo, and the physical semantics or byte output of any export format.
- FlatLaf version, settings or native window decorations; the Look and Feel
  installation.
- Redesign of the export dialog, its layout, its modality or its ownership;
  disposal policy of hidden dialogs.
- Any other Swing/EDT debt, Desktop heap work, `exportPDF` UTF-8, AutoColor,
  verification architecture, packaging, DXF, units or export debt.
- `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`,
  `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`,
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`.
- `PRE-G9B-R6-plus-E1`, `E2`, `E3`, `F1`, `F2`, `F3` implementation, `G`,
  `PRE-G9B-R7`, `G9B`.

## Architectural placement

Upstream Desktop frontend, Swing event-thread ownership of one Classic menu
action. Not kernel, not document model, not export service, not GeoCeDG
application layer. `AGENTS.md` §3.1 applies: minimum necessary change in an
upstream path, recorded in `docs/upstream/modified-files.yml`.

## Required design/specification

None beyond this prompt. No specification or ADR governs Swing thread
ownership, and the activity changes no product contract. If the authorizing
instruction requires an ADR, it names it; the agent does not create one.

## Geometric invariants and degeneracies

Not applicable: no geometric object, domain, branch, tolerance or dependency is
read or changed.

## Compatibility and serialization

```text
SERIALIZATION CHANGE   = NONE
PREFERENCES CHANGE     = NONE (same keys, same values, now written on the EDT)
EXPORT OUTPUT CHANGE   = NONE (no export class changes; bytecode identity outside
                         FileMenuD and its nested classes)
CLASSIC BEHAVIOUR      = same dialog, same content, same modeless ownership, same
                         Cancel, same 3D fallback; created synchronously on the EDT
GEOCEDG v2 BEHAVIOUR   = unchanged (does not use FileMenuD)
GEOCEDG v1 FALLBACK    = same thread change as Classic (it builds FileMenuD)
```

Known visible difference: the wait cursor set before construction is no longer
painted while the EDT builds the dialog (about 0.22 s for the first open of a
session in the probes, mostly the font scan of `checkBrailleFont()`, and about
0.02 s afterwards). This matches the GeoCeDG v2 route and the
Classic dispatcher route.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation; they run in the phase selection:

| ID | Obligation | Kind |
|---|---|---|
| `T-X1-OWNERSHIP` | with a Classic application, the real `FileMenuD` item (built through `menuSelected`) clicked on the EDT; every container and hierarchy event of the `GraphicExportDialog` and every lifecycle stage of X1 (constructor, `initGUI`, `pack`, `loadPreferences`/`updateComponentTreeUI`, `show`) occurs on the EDT; the dialog exists and is showing when the menu dispatch returns | deterministic |
| `T-X1-POPULATED` | the dialog is showing, valid, laid out (non-zero Save and Cancel bounds, content pane populated) and paints non-blank | deterministic |
| `T-X1-NO-EDT-EXCEPTION` | no uncaught exception on the EDT during open, settle and Cancel | deterministic under `INV-X1` |
| `T-X1-CANCEL` | Cancel hides the dialog; preferences saved as before | deterministic |
| `T-X1-REPEAT` | at least five open/Cancel cycles in one application | deterministic |
| `T-X1-ACCELERATOR` | the `Ctrl+Shift+U` accelerator from a focused text field reaches the same action on the EDT; from the graphics view the dispatcher route is unchanged | deterministic |
| `T-X1-FALLBACK` | an exception from `showGraphicExport()` still reaches the unchanged `catch` branch; the clipboard sink is stubbed, never the system clipboard | deterministic |
| `T-X1-GEOCEDG` | GeoCeDG v2 `host.export.picture` unchanged: EDT, populated, GeoCeDG scale presentation present; the `B` v1-fallback entry test still passes | regression |
| `T-X1-CLASSIC-NO-GEOCEDG-UI` | the Classic dialog has `getExportScalePresentation() == null`, the host mode combo with the three host labels, and no GeoCeDG construction-unit or `drawingScale` control | deterministic |
| `T-X1-BYTECODE` | class-hash list of `:desktop:desktop` main classes equal to the base except `FileMenuD` and its nested classes; no export or dialog class differs | deterministic |
| `T-X1-PROCESS` | one process-isolated windowed test: `GeoGebra3D.main` with an isolated settings file in a child JVM, the real File > Export item, several cycles, ownership, populated and Cancel assertions, an EDT watchdog with a lock-aware thread dump on timeout, and a sandboxed clipboard fallback | deterministic ownership; windowed |
| `T-SMOKE` | author-smoke checklist (*Required artifacts*); the agent does not perform author smoke | author |

Deterministic assertions are the ownership invariant and its direct
consequences; they never rely on the absence of a randomly timed failure. The
absence of the author's empty-dialog-and-hang variant is established only by
author smoke.

Harness rules: in `T-X1-PROCESS` the child JVM invokes `GeoGebra3D.main` from
the EDT (`SwingUtilities.invokeAndWait`), so the independent Classic startup
race recorded under `DQ-X1-4` cannot perturb the picture-launch assertions; the
test states this deviation from the product launcher, which concerns startup
only (expected to be feasible because GeoCeDG already runs the same
`GeoGebraFrame.init` on the EDT; not exercised by the preparation probes, so it
is re-established at implementation); never touch the system clipboard (stub the fallback sink or
sandbox it in the child JVM); never fire `Ctrl+Shift+C`, `Ctrl+Shift+M` or
`Ctrl+Shift+B`; isolated settings files, never the author's `%APPDATA%`
files; install `LoggerD` per test; dispose created windows; child processes
halt themselves and are killed on a bounded timeout; follow the existing
Desktop-test harness rules for modal dialogs and the asynchronous undo store.

Adjacent regressions before freezing (development evidence, not acceptance):
`PreG9BR6PlusBExportSurfaceTest`, `PreG9BR6PlusBPictureFidelityTest`, the
`PreG9BR6PlusC*` Desktop classes, `G9U1ActionRegistryTest`,
`GeoCeDGProfileTest`; Checkstyle `:desktop:desktop:checkstyleMain` and
`:desktop:desktop:checkstyleTest` (read the XML reports; ASCII escapes in
non-test sources); `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline
9b93256b7df401ff056c37b502d82df4d72b1522`; `git diff --check`.

Registration and catalog: register the phase selection `PRE-G9B-R6-PLUS-C-X1`
with `compile.shared.semantic`, `compile.desktop.semantic` and
`junit.desktop.pre-g9b-r6-plus-c-x1.semantic`, following the `PRE-G9B-R5-A`
shape (Desktop-only phase). Discovery dry-run evidence and **executed**
selection evidence through `tools/agent/checks/gradle-test-evidence-producer.ps1`,
then `tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand. When registry-shape pins change,
run a development `INFRA_UNIT` on the staged tree before freezing.

Acceptance (the planned `BOUNDED_PHASE` contract), on one clean immutable
committed candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C-X1 -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C-X1 -LogDirectory <log root>
```

One `ACCEPTED / COMPLETE` run on the exact commit and tree; no further commit
afterwards. `INTEGRATION` and `FINAL` are not required by this class and are not
run; a request to run either is a `VERIFICATION_ESCALATION_REQUEST` for the
author. If product or test code changes after acceptance, that candidate is no
longer the accepted candidate: stop and report. The standing diagnostics are
pre-existing and do not affect acceptance. A failure proven to predate the
candidate and lie outside its delta is retained baseline debt per the task
template, not phase scope. Report exact commands, exit codes, run ids, plan and
result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

This prompt authorizes nothing. Its existence records a prepared contract.
Execution requires an explicit author instruction naming the activity, the
exact branch start, the frozen class and the dispositions of `DQ-X1-1` to
`DQ-X1-6`; that instruction may authorize local implementation, local commits,
technical verification, the registered `PHASE` run and one frozen technical
candidate for author review and smoke. No instruction derived from this file
authorizes self-approval, author smoke by the agent, an `INTEGRATION` or
`FINAL` run, or any change of the class.

This activity authorizes nothing that follows it. `E1`, `E2`, `E3`, `F1`, `F2`,
`F3`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every open
observation and enhancement not named here. Author approval is never created
by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local implementation branch from the exact branch start and local commits
only. Push, branch publication, merge, promotion to `main`, rebase, squash,
amend after freeze, force push, tag, release and binary publication are
forbidden; each needs a separate explicit author instruction naming the exact
candidate SHA. Acceptance evidence never grants publication authority.

## Acceptance and closeout

The activity stops with one technically verified candidate pending author
review and author smoke, with `selfApproved = false`. Author approval is an
explicit decision naming the exact accepted commit. The candidate report and
its evidence record `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later
closeout record is the sole authority for approval and for the disposition of
`OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT` (`DQ-X1-5`). Closeout and
publication are separately authorized documentary steps. No receipt is
expected for this class.

## Required artifacts

- The one-file product delta, the tests, their registration and the
  `modified-files.yml` purpose update.
- A candidate report under `docs/validation/` with: entry-gate evidence; X1–X8
  re-established at the implementation base with every correction; the exact
  diff; the ownership trace before and after (route by route); the bytecode
  identity list; the obligation-to-test map; the adjacent debt of `DQ-X1-4`
  restated unchanged; residual risks; the author-smoke checklist.
- The author-smoke checklist covers at least: the Classic diagnostic
  application from GeoCeDG, File > Export > Graphics View as Picture opened
  and cancelled repeatedly with the real mouse, including moving the pointer
  over the dialog while it appears; the same through `Ctrl+Shift+U` from the
  input bar and from the graphics view; a PNG and an SVG saved from Classic; the
  GeoCeDG Picture dialog and a PNG saved from it; the Laboratory application if
  the author uses it.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale (expected: no change), the
  verification-infrastructure-impact assessment (phase registration only),
  `GUIDE_IMPACT` (expected: none; the guides do not describe Classic
  threading), `SERIALIZATION CHANGE = NONE`, and the exact `PHASE` command,
  exit code and log path.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- re-establishing X1–X8 at the base contradicts the characterization;
- `INV-X1` cannot be met by option A alone, or the fix needs a change in
  `GraphicExportDialog`, `GuiManagerD` or any other file than `FileMenuD`;
- the fix would change export output, preferences, serialization or the
  GeoCeDG v2 route;
- GeoCeDG-specific code would need to enter Classic;
- an EDT deadlock, a nested event loop or a modal interaction appears that the
  characterization did not record;
- the windowed test cannot run deterministically on the supported Windows
  verification environment;
- a `DQ-X1` question is reached without an author disposition;
- current governance requires a verification class other than `BOUNDED_PHASE`;
- the `PHASE` run is rejected for a cause attributable to the candidate, or its
  coverage is incomplete or untrusted;
- product or test code would change after acceptance.

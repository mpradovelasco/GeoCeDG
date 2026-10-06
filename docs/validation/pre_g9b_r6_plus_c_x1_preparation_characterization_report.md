# PRE-G9B-R6-plus-C-X1 preparation characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (post-C canonical prompt)
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

OBSERVATION               = OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT
PROPOSED IDENTIFIER       = PRE-G9B-R6-plus-C-X1 (not author-approved; DQ-X1-1)
PROMPT STATE              = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`C-X1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt.prompt.md)
(sections X1–X8 and `DQ-X1-1` to `DQ-X1-6`). The prompt holds the resulting
contract; this report does not restate it and carries no authority of its own.
Its machine-readable mirror is
[`pre-g9b-r6-plus-c-x1-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-preparation-characterization.json).

## 0. Preparation inputs

The author's instruction of 2026-10-06 is the authority of this preparation.
It is recorded here, instead of in a separate author-decision record, because
it fixes no product decision and asks no question that a record would carry;
every open decision is a `DQ-X1` row of the prompt.

- Baseline: `PRE-G9B-R6-plus-C = PASS — AUTHOR APPROVED — PUBLISHED`,
  `P_R6PLUS_C = 39eb05bc16a18ae48167383e90fcf38b12a681b6`, tree
  `fc13f5f125a4e8d595dd450bfd47706be193e5cd`; comparison base where useful
  `e613502831e3b412780d69424d4a1e433a4ae688`.
- Authorized: bounded post-`C` characterization, design and implementation
  preparation for `OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT`; diagnostic probes
  outside product code; a characterization report, a machine-readable mirror, a
  canonical prompt in `PREPARED — NOT AUTHORIZED`, minimal roadmap and
  mini-track registration. Not authorized: the product or upstream fix, a
  product candidate, publication.
- Starting evidence to verify, not to inherit: the Classic route through
  `org.geogebra.desktop.GeoGebra3D` and upstream `FileMenuD`, the off-EDT
  construction, the FlatLaf/`BoxLayout` NPEs, reproduction on `e6135028` and on
  the `C` candidate, populated dialogs in automated runs, the author-only severe
  variant, and the EDT-correct GeoCeDG route.
- Required probes P1–P6, upstream inspection, implementation contract, test
  design, verification classification, naming proposal, forbidden scope
  (`E1`–`G`, `PRE-G9B-R7`, `G9B`, three named observations, unrelated debt) and
  the stop conditions of the instruction's §14.
- The `C` closeout is not reinterpreted: the observation stays
  `REPRODUCED BY AUTHOR — PRE-EXISTING — NOT CAUSED BY C — ACCEPTED DEBT`.

## 1. Entry gate

```text
P_R6PLUS_C           = 39eb05bc16a18ae48167383e90fcf38b12a681b6
                       tree fc13f5f125a4e8d595dd450bfd47706be193e5cd
local main           = 39eb05bc16a18ae48167383e90fcf38b12a681b6
origin/main          = 39eb05bc16a18ae48167383e90fcf38b12a681b6
live remote main     = 39eb05bc16a18ae48167383e90fcf38b12a681b6
                       (git ls-remote origin refs/heads/main)
worktree             = clean before any tracked edit
C state              = PASS — AUTHOR APPROVED — PUBLISHED
                       (AUTHOR_SMOKE = PASS WITH ACCEPTED PRE-EXISTING DEBT)
preparation branch   = phase/pre-g9b-r6-plus-c-x1-prompt, created from the exact
                       commit above (local; not pushed)
upstream pin         = 9b93256b7df401ff056c37b502d82df4d72b1522 (5.4.928.0)
```

No other local branch was created, deleted or moved.

## 2. Method

- **Static reading** of every cited seam at `P_R6PLUS_C`, each line re-read
  before it was cited.
- **Base identity.** The launch blocks were extracted from the three
  revisions with `git show <rev>:<path>` and hashed (§8).
- **Upstream inspection** through the GitHub API (read-only): the commit
  history of `FileMenuD.java` and `GraphicExportDialog.java` on
  `geogebra/geogebra` `main`, and `FileMenuD.java` at its head (§9).
- **One scratch-only probe class**, `PostCClassicPictureProbeTest`, kept in the
  session scratchpad and added to the Desktop test source set only through a
  Gradle init script that also redirected the JUnit XML to the scratchpad and
  selected the JDK 25 launcher (Temurin 25.0.4, the product's runtime). The JUnit
  method only orchestrates; **every run is a separate child JVM** started with
  the test classpath and the main class `PostCClassicPictureProbeTest$Driver`,
  which launches `GeoGebra3D.main` (Classic) or `GeoCeDG.main` with an
  isolated, fresh settings file in the scratchpad (never `%APPDATA%`), and
  halts itself.
- **Instrumentation inside each child JVM**, none of it in product code:
  - ByteBuddy inline advice (the `byte-buddy-agent` jar already on the test
    classpath, as `-javaagent`) on `FileMenuD$*.actionPerformed` and its
    lambdas, `GuiManagerD.showGraphicExport`, and the `GraphicExportDialog`
    constructors, `initGUI`, `centerOnScreen`, `loadPreferences`, `setVisible`,
    `savePreferences` and `doExport`: thread name, `isEventDispatchThread()`,
    caller chain, thrown exceptions;
  - a Toolkit `AWTEventListener` for container and hierarchy events of the
    dialog, which AWT dispatches synchronously on the mutating thread; each
    event is classified by its stack (`constructor`, `initGUI` population,
    `pack`, `loadPreferences`, `updateComponentTreeUI`, `show`, `hide`) and
    counted as **concurrent** when it occurs on the EDT while a worker thread
    is inside a dialog method;
  - a thread-checking `RepaintManager` recording `addInvalidComponent` and
    `addDirtyRegion` calls made off the EDT;
  - a default uncaught-exception handler that also records which worker
    methods were active at that instant;
  - an EDT watchdog (100 ms ping, hang after 8 s without an answer) that
    writes `findDeadlockedThreads` and a lock-aware `dumpAllThreads(true,
    true)` before halting;
  - **clipboard protection**: advice that skips
    `AppD.copyGraphicsViewToClipboard` and `AppD.simpleExportToClipboard` and
    records any attempt, so the product's exception fallback can never touch
    the system clipboard.
- **Drive.** The real File menu is opened (`doClick`), its lazily built items
  materialized through `menuSelected`, the Export submenu opened, and the real
  "Graphics View as Picture (png, svg) ..." item clicked; after 0.7 s the dialog
  is inspected on the EDT (content, validity, laid-out Save/Cancel, zero-size
  components, an off-screen paint of the root pane with a colour count, modal
  type, owner, texts) and closed with its own Cancel button; this is repeated
  for 5 cycles per process.
- **Modes.**

| Mode | Route |
|---|---|
| `CLASSIC_MENU` | Classic, real File > Export item, unchanged product code (threaded) |
| `CLASSIC_MENU_EDT` | the same real item; its `Action` listener is replaced, in the child JVM only, by the **scratch prototype**: the unchanged action body executed directly in `actionPerformed` on the EDT (option A of the prompt) |
| `CLASSIC_MENU_METAL` | `CLASSIC_MENU` after switching the running application to `MetalLookAndFeel` |
| `CLASSIC_KEY` | `Ctrl+Shift+U` dispatched to `AppD.dispatchKeyEvent` from the graphics-view panel, then to the key bindings if not consumed |
| `CLASSIC_KEY_TEXT` | the same from the input-bar text field, after the File menu was built once |
| `GEOCEDG_MENU` | GeoCeDG v2, the real menu item whose action target is `host.export.picture` |
| `+stress` | a synthetic amplifier posting, every 2 ms, an EDT `invalidate()`/`validate()` of any displayable export dialog |

- **Settings variants**: fresh isolated settings (all modes) and a copy of the
  author's current `classic-diagnostic.properties` (recent-file list only).
- No product, test, build, registry, schema, verifier, specification or ADR
  file changed. No `PHASE`, `INTEGRATION` or `FINAL` ran.

Orchestration: `gradlew.bat :desktop:desktop:test --tests
'org.geocedg.desktop.PostCClassicPictureProbeTest' --rerun --init-script
<scratch>\postc-probe.gradle -Dpostc.label=<label> -Dpostc.modes=<modes>
-Dpostc.runs=<n> -Dpostc.cycles=5 [-Dpostc.stress=true]
[-Dpostc.settingsTemplate=<copy>]`, driven batch by batch by
`<scratch>\campaign.ps1`; every Gradle invocation exited 0. Results were
tallied by `<scratch>\aggregate.ps1` from the per-process records.

| Item | SHA-256 |
|---|---|
| probe source `PostCClassicPictureProbeTest.java` | `15b3bdd46218e7d3fce0a4be0eedf4892eef45b9b70ad8c4ce5b4e8d99ed95ad` |
| init script `postc-probe.gradle` | `6f229eb0990e2d427637b5dfda0337f1459abe402d2b3d6415b766a0caea7aa1` |
| campaign driver `campaign.ps1` | `b17e640c513abb9271c26fbebd714da2ec4501b78db8785d27b6a77cbef5f29a` |
| aggregator `aggregate.ps1` | `f77ad782ddb73cf14516ef8261d9fe6041d115a1d062b54921ef53bbec231e16` |
| aggregate of the `P_R6PLUS_C` campaign | `43a55e2d8c8550fd7c1d298bfd6c8c79ce9e3a88c60903f0f048eaa49c7f34a5` |
| concatenated per-batch summaries (`cur-*-summary.txt`, name order) | `168ed45f4ee6e0aab450e209eeb0e131b1287d7af7e92444fbd0c24c4e2fb004` |

The `P_R6PLUS_C` campaign launched 110 child JVMs and produced 109 records; the
missing one is the startup failure of §12.

## 3. P1/P2 — execution path and thread ownership

Representative trace, `CLASSIC_MENU`, first cycle (times from the child JVM's
start):

```text
MENU ACTION                                   AWT-EventQueue-0  edt=true
FileMenuD$12.actionPerformed                  AWT-EventQueue-0  edt=true
GuiManagerD.showGraphicExport                 Thread-2          edt=false
    caller: FileMenuD$12.lambda$actionPerformed$0 < Thread.run
GraphicExportDialog.<init>                    Thread-2          edt=false
GraphicExportDialog.initGUI                   Thread-2          edt=false
GraphicExportDialog.centerOnScreen (pack)     Thread-2          edt=false
GraphicExportDialog.setVisible(true)          Thread-2          edt=false
GraphicExportDialog.loadPreferences           Thread-2          edt=false
  └ format listener → updateComponentTreeUI   Thread-2          edt=false
super.setVisible (show)                       Thread-2          edt=false
Cancel → setVisible(false) → savePreferences  AWT-EventQueue-0  edt=true
```

Stage counts of the dialog's Swing events, by thread (the AWT listener;
`cur-natural` run 1 of each mode, 5 cycles):

| Stage (event kind) | `CLASSIC_MENU` | `CLASSIC_MENU_EDT` (prototype) |
|---|---|---|
| constructor (container / hierarchy) | 5 / 20 worker | 5 / 20 EDT |
| `initGUI` population | 140 / 205 worker | 140 / 205 EDT |
| `pack` | 10 / 250 worker | 10 / 250 EDT |
| `loadPreferences` | 30 / 75 worker | 30 / 75 EDT |
| `loadPreferences` → `updateComponentTreeUI` | 120 / 180 worker | 120 / 180 EDT |
| `show` (hierarchy) | 210 worker | 210 EDT |
| Cancel → hide (hierarchy) | 210 EDT | 210 EDT |
| other EDT events on the dialog (layout, renderer panes) | 140 / 210 EDT, **125 of them while the worker was inside a dialog method** | 100 / 150 EDT, none concurrent |
| off-EDT repaint requests on the dialog | 205 | 0 |

The `RepaintManager` recorded off-EDT repaint requests on the dialog in every
worker stage (population, `pack`, `loadPreferences`, `updateComponentTreeUI`),
and further off-EDT requests on components not yet attached to a window. No
off-EDT `addInvalidComponent` was recorded, as expected: `JComponent.revalidate`
re-posts itself to the EDT when called off it, so the invalidations reach the
EDT asynchronously while the worker continues.

`CLASSIC_MENU_EDT`, `CLASSIC_KEY` and `GEOCEDG_MENU` record every one of the
stages above on `AWT-EventQueue-0` with `edt=true`, inside the EDT dispatch of
the action, with zero off-EDT repaint requests.

Routes, as observed:

| Route | Reaches | Thread of the dialog lifecycle |
|---|---|---|
| Classic File > Export > Graphics View as Picture | `FileMenuD.exportGraphicAction` → `new Thread` | worker |
| Classic `Ctrl+Shift+U`, graphics view focused | `GlobalKeyDispatcher` `case U` → `showGraphicExport()`; consumed, the accelerator never fires | EDT |
| Classic `Ctrl+Shift+U`, text field focused | `GlobalKeyDispatcherD` ignores text components; the menu accelerator fires `exportGraphicAction` | worker |
| GeoCeDG v2 Picture | `GeoCeDGActionRegistry.invoke` → `execute` → `showGraphicExport()` | EDT |
| GeoCeDG v1 legacy fallback | the Classic `GeoGebraMenuBar` and `FileMenuD` (`GuiManagerGeoCeDG.java:34`) | worker (static reading; not run) |

## 4. P3 — exceptions, deadlock evidence, modality

Modality: every dialog recorded `modal=false`, `modality=MODELESS`, owner the
main frame (`GeoGebraFrame3D` for Classic, `GeoCeDGFrame` for GeoCeDG). The
menu action returns immediately; no secondary event loop is entered, so no
nested-loop or modal-ownership deadlock path exists on this route.

Uncaught exceptions over the whole `P_R6PLUS_C` campaign, all on
`AWT-EventQueue-0`, all `NullPointerException`:

| Message | Thrown in | Natural | Author settings | Key from text field | Metal | Stress | Metal + stress |
|---|---|---|---|---|---|---|---|
| `"list" is null` | `DefaultListCellRenderer` ← `BasicComboBoxUI.getDisplaySize` | 33 | 12 | 11 | 7 | 41 | 46 |
| `"total" is null` | `SizeRequirements.calculateAlignedPositions` ← `BoxLayout.layoutContainer` | 18 | 3 | 7 | 0 | 202 | 95 |
| `"this.paddingBorder" is null` | `FlatComboBoxUI$CellPaddingBorder.uninstall` ← `FlatComboBoxUI.getDisplaySize` | 0 | 0 | 0 | 0 | 9 | 0 |
| total | | 51 | 15 | 18 | 7 | 252 | 141 |

No uncaught exception occurred in any EDT route (prototype, prototype under
stress, `Ctrl+Shift+U` from the graphics view, GeoCeDG v2), and no exception
was thrown out of `showGraphicExport()`, so the product's clipboard fallback
was never reached (`clipboardBlocked = 0` everywhere).

Every uncaught exception was recorded while the worker thread was inside
`GraphicExportDialog.setVisible` → `loadPreferences`. Representative
concurrency sample (first recorded instance):

```text
CONCURRENT EDT HIERARCHY_CHANGED (DISPLAYABILITY_CHANGED) on a combo-box renderer
  while bg in [Thread-2: GraphicExportDialog.loadPreferences, Thread-2: GraphicExportDialog.setVisible]
    at java.awt.Component.addNotify
    at java.awt.Container.addImpl
    at javax.swing.CellRendererPane.addImpl
    at javax.swing.plaf.basic.BasicComboBoxUI.getSizeForComponent
    at com.formdev.flatlaf.ui.FlatComboBoxUI.getSizeForComponent
    at javax.swing.plaf.basic.BasicComboBoxUI.getDisplaySize
    at com.formdev.flatlaf.ui.FlatComboBoxUI.getDisplaySize
    at javax.swing.plaf.basic.BasicComboBoxUI.getMinimumSize
    at com.formdev.flatlaf.ui.FlatComboBoxUI.getMinimumSize
    at javax.swing.JComponent.getMinimumSize
    at java.awt.FlowLayout.minimumLayoutSize
```

The EDT is laying out a combo box of the dialog (and mutating its
`CellRendererPane`) at the same time as the worker re-installs the combo-box
UIs through `updateComponentTreeUI`. The `list is null` NPE is consistent with
the EDT reading a combo UI between the worker's `uninstallUI` and `installUI`;
the `total is null` NPE with the EDT's `BoxLayout` pass over a panel whose
children the worker is re-installing. All 773 recorded NPEs (484 at
`P_R6PLUS_C`, 289 at the base) carry the worker state
`setVisible` + `loadPreferences`. The probe keeps 15 frames per exception; in
the 113 traces deep enough to show the EDT entry point, the validation was
entered from the dialog's own window event (`Window.dispatchEventImpl` →
`validate`), which the EDT receives once `pack()` has made the dialog
displayable.

Hang and deadlock evidence: the watchdog never fired (no EDT stall of 8 s in
any run, so no thread dump and no `findDeadlockedThreads` result was
produced), every dialog was showing, valid, laid out and painted non-blank
0.7 s after the click, and every Cancel closed its dialog. The severe variant
was not reproduced, including under the synthetic amplifier and with the
author's settings.

## 5. P4 — repetition

Each row is a set of separate child JVMs; 5 open/Cancel cycles per JVM.

| Batch | Mode | JVMs | Opens | Populated | Cancelled | JVMs with NPE | NPEs | Off-EDT dialog repaints | Concurrent EDT events | Hang | Defective dialog |
|---|---|---|---|---|---|---|---|---|---|---|---|
| natural | `CLASSIC_MENU` | 20 | 100 | 100 | 100 | 19 | 51 | 4100 | 2425 | 0 | 0 |
| natural | `CLASSIC_MENU_EDT` | 20 | 100 | 100 | 100 | 0 | 0 | 0 | 0 | 0 | 0 |
| author settings | `CLASSIC_MENU` | 10 | 50 | 50 | 50 | 6 | 15 | 2050 | 1200 | 0 | 0 |
| keys | `CLASSIC_KEY` | 5 | 25 | 25 | 25 | 0 | 0 | 0 | 0 | 0 | 0 |
| keys | `CLASSIC_KEY_TEXT` | 5 | 25 | 25 | 25 | 5 | 18 | 975 | 525 | 0 | 0 |
| metal | `CLASSIC_MENU_METAL` | 9 (+1 startup failure) | 45 | 45 | 45 | 4 | 7 | 1350 | 1050 | 0 | 0 |
| geocedg | `GEOCEDG_MENU` | 10 | 50 | 50 | 50 | 0 | 0 | 0 | 0 | 0 | 0 |
| stress | `CLASSIC_MENU` | 10 | 50 | 50 | 50 | 10 | 252 | 2050 | 1345 | 0 | 0 |
| stress | `CLASSIC_MENU_EDT` | 10 | 50 | 50 | 50 | 0 | 0 | 0 | 0 | 0 | 0 |
| stress | `CLASSIC_MENU_METAL` | 10 | 50 | 50 | 50 | 10 | 141 | 1500 | 1380 | 0 | 0 |

"Off-EDT dialog repaints" counts `RepaintManager.addDirtyRegion` calls made
off the EDT for components inside the dialog; off-EDT calls for components not
yet attached to a window are recorded separately and are also zero on every
EDT route.

The failure is a race: its frequency depends on timing, and its absence in a
run proves nothing. What is deterministic is the ownership recorded in §3: on
the threaded route the worker performs every stage of the dialog lifecycle in
every run, and the EDT validates the displayable dialog concurrently in every
run.

## 6. P5 — scratch corrected-path prototype

The prototype keeps the real menu item, its text and accelerator, and runs the
unchanged body of `exportGraphicAction` (`setWaitCursor`;
`showGraphicExport()`; `catch (Exception)` → `copyGraphicsViewToClipboard`,
skipped by the probe advice; `setDefaultCursor`) directly in `actionPerformed`.

| | Threaded original (natural + stress) | Prototype (natural + stress) |
|---|---|---|
| JVMs / opens | 30 / 150 | 30 / 150 |
| FlatLaf / `BoxLayout` NPEs | 303 (in 29 JVMs) | 0 |
| off-EDT dialog repaint requests | 6150 | 0 |
| EDT events on the dialog while the worker was inside it | 3770 | 0 |
| partially built dialog | 0 recorded | 0 |
| Cancel failed | 0 | 0 |
| watchdog timeout | 0 | 0 |

The prototype eliminated, in every run: the FlatLaf/`BoxLayout` NPEs; every
off-EDT repaint request and every EDT/worker concurrency event; partially built
dialogs (none was recorded in any mode); inability to cancel (every Cancel
closed its dialog); watchdog timeouts (none in any mode). On the EDT,
`showGraphicExport()` took about 0.22 s for the first open of a process
(mostly `checkBrailleFont()`'s font enumeration) and about 0.02 s for later
opens (entry to exit of `showGraphicExport`, prototype runs 1 and 7).

Why marshalling is **sufficient** for the launch: after option A, the menu
action, `showGraphicExport()`, the constructor, `initGUI`, `pack`,
`loadPreferences` with its `updateComponentTreeUI`, and `show` run in one EDT
dispatch. The EDT cannot process the dialog's window events, nor validate or
paint it, until that dispatch returns, so no interleaving of the two parties
of §4 remains. The GeoCeDG v2 route and the Classic dispatcher route already
work this way with the same dialog class (§3). Why it is **necessary at the
call site**: the callee and the dialog are correct on the EDT, and the only
off-EDT caller found is `FileMenuD` (static search of `showGraphicExport()`
callers: `FileMenuD`, `GlobalKeyDispatcher`, `GeoCeDGActionRegistry`, and the
web `GuiManagerW` implementation).

## 7. FlatLaf

`CLASSIC_MENU_METAL` runs the same threaded route under `MetalLookAndFeel`:
the ownership is identical (every lifecycle stage on the worker), the EDT
again validates the dialog concurrently (1050 concurrent events in 9 JVMs,
1380 under stress), and the same `list is null` NPE occurs (7 in 4 of 9 JVMs;
141 `list`/`total` NPEs under stress). Only one exception kind was
FlatLaf-specific (`CellPaddingBorder.uninstall`, 9 occurrences under stress),
itself a symptom of the same concurrent re-installation. The `list is null` NPE is thrown in
`javax.swing.DefaultListCellRenderer` called from
`javax.swing.plaf.basic.BasicComboBoxUI.getDisplaySize`, and the `total is null`
NPE in `javax.swing.SizeRequirements` from `BoxLayout`; FlatLaf frames appear
only as delegating overrides. FlatLaf 3.7 (unchanged from the upstream pin)
exposes the violation; it is not causal. Static reading of FlatLaf's Windows
title-bar hit-test callback (`FlatWindowsNativeWindowBorder$WndProc.onNcHitTest`
→ `FlatTitlePane.captionHitTest`) found no `invokeAndWait` and no explicit
monitor, so no FlatLaf-mediated deadlock could be established from the
bytecode.

## 8. Base comparison

SHA-1 (`git hash-object`) of each extracted block:

| Block | `9b93256b` (upstream pin) | `e6135028` (pre-`C`) | `39eb05bc` (`P_R6PLUS_C`) |
|---|---|---|---|
| `FileMenuD` `exportGraphicAction` | `3727b2e6f6…` | `3727b2e6f6…` | `3727b2e6f6…` |
| `GuiManagerD.showGraphicExport()` | `3fb799f546…` | `3fb799f546…` | `3fb799f546…` |
| `GraphicExportDialog(AppD, EuclidianViewD)` | `799812c347…` | `799812c347…` | `799812c347…` |
| `GraphicExportDialog.setVisible` | `b3610100b0…` | `b3610100b0…` | `b3610100b0…` |
| `GraphicExportDialog.initGUI` | `e1d040f189…` | `e1d040f189…` | `e1d040f189…` |
| `GraphicExportDialog.loadPreferences` | `3c7bdf5667…` | `3c7bdf5667…` | `3c7bdf5667…` |

`C` changed `GraphicExportDialog.updateSizeLabel`, `doExport` and the
EMF/PDF export bodies, and `PrintScalePanel`'s constructor and
`enableAbsoluteSize`; each new seam returns the host value in Classic
(`App.getPhysicalExportScale()` = `NaN`, `AppD.getExportScalePresentation()` =
`null`), so the Classic dialog keeps the host labels (§10). The launch
lifecycle is identical at all three revisions.

Dynamic base comparison: the same probe ran on a `git archive` of
`e6135028` (verified blob-identical to the commit for `FileMenuD`,
`GuiManagerD`, `GraphicExportDialog`, `PrintScalePanel` and `AppD`), with the
same JDK and harness, mode `CLASSIC_MENU`:

| Batch | JVMs | Opens | Populated | Cancelled | JVMs with NPE | NPEs (`list` / `total` / `paddingBorder`) | Off-EDT dialog repaints | Concurrent EDT events | Hang |
|---|---|---|---|---|---|---|---|---|---|
| base natural | 10 | 50 | 50 | 50 | 9 | 24 (15 / 8 / 1) | 2050 | 1225 | 0 |
| base stress | 10 | 50 | 50 | 50 | 10 | 265 (43 / 216 / 6) | 2050 | 1295 | 0 |

Aggregate SHA-256 `d80259d9d526f8947f9712e8baffe1ce2156a74a38b0f318330e58c4068fc3ae`.
The ownership counts are identical to `P_R6PLUS_C` (2050 off-EDT dialog
repaints per 10 JVMs on both), and the exception kinds are the same: the
defect is pre-existing and `C` did not alter it.

## 9. Upstream comparison

| Item | Value |
|---|---|
| upstream `main` head inspected | `a9ad4789c45e14811ee704c0cf1b00ebb26638f7` (2026-10-05) |
| post-pin commits touching `FileMenuD.java` | `fe033eaa` (2026-09-11, "Make desktop tests PMD compliant"), `22e32efa` (2026-09-17, "No ratchet") |
| post-pin commits touching `GraphicExportDialog.java` | `8215b120` (2026-09-02, "Remove useless parentheses"), `fe033eaa`, `22e32efa` |
| `exportGraphicAction` at upstream head | still `new Thread(...)` around `showGraphicExport()`, reformatted only |

No later upstream correction exists; the behaviour is inherited unchanged from
the pin. The `GraphicExportDialog` commits were classified from their messages
and were not content-diffed; they are not a dependency of option A, which does
not touch that class. The established in-file pattern for the EDT is the
direct call: Animated GIF, PSTricks, PGF/TikZ, Asymptote, Open, Save and
Save As open their UI directly in `actionPerformed`, and so do the Classic
dispatcher (`GlobalKeyDispatcher.java:801-805`) and the GeoCeDG v2 route. The
worker-thread pattern is shared by `newWindowAction`,
`drawingPadToClipboardAction`, `exportWorksheet`, `exportGeoGebraTubeAction`
and `GeoGebraMenuBar.showPrintPreview`; the prompt keeps them out of scope
(`DQ-X1-4`).

## 10. P6 — GeoCeDG comparison

`GEOCEDG_MENU`: 10 JVMs, 50 opens, every stage on the EDT through
`GeoCeDGActionRegistry.invoke` → `execute` → `showGraphicExport()`, 50 populated
dialogs, 50 Cancels, zero NPEs, zero off-EDT work, zero concurrency. Dialog
content, unspecified construction unit:

| | Classic | GeoCeDG v2 |
|---|---|---|
| owner | `GeoGebraFrame3D` | `GeoCeDGFrame` |
| scale-mode combo | `Scale in cm:` / `Fixed Size:` / `Size in pixels:` | `Device scale (non-physical):` / `Device scale from the screen (non-physical):` / `Size in pixels (device):` |
| statement | none | `Non-physical: no construction unit` |

The GeoCeDG route is independently correct and does not use `FileMenuD`;
option A neither redirects it nor adds a Classic-specific abstraction. GeoCeDG
in the v1 legacy fallback uses `FileMenuD` and would receive the same thread
change as Classic.

## 11. Root cause, confidence and uncertainty

```text
ROOT CAUSE (launch race)       = CONFIRMED — the Classic FileMenuD action builds,
                                 populates, packs, loads preferences into and shows
                                 GraphicExportDialog on a worker thread while the EDT
                                 validates the displayable dialog
FIX SUFFICIENCY FOR THE RACE   = CONFIRMED by construction (single EDT dispatch) and
                                 by the prototype runs
FLATLAF                        = NOT CAUSAL (exposes the violation; also under Metal)
C                              = NOT CAUSAL (launch blocks identical at 9b93256b,
                                 e6135028 and 39eb05bc)
SEVERE VARIANT (empty + hang)  = NOT REPRODUCED AUTOMATICALLY; connection to the
                                 off-EDT launch inferred, not demonstrated
```

The inference rests on: the severe variant was observed only on the Classic
menu route; the off-EDT launch is the only Classic-specific difference from
the EDT routes, which never failed; Swing defines no behaviour for components
mutated concurrently; an aborted EDT validation leaves a displayable dialog
without a completed layout, which is the "empty" symptom. Not established: the
mechanism of the author's hang. No deadlock was observed, no thread dump of it
exists, and the probes cannot reproduce real pointer movement over a newly
created native window (the probes dispatch events and never move the user's
mouse). The modeless ownership rules out a modal-loop deadlock on this route.
The remaining uncertainty is closed only by author smoke after the fix
(`DQ-X1-5`); the instruction's stop condition "the severe hang cannot
reasonably be connected to the off-EDT path" is not met, but the connection is
a technical inference that the author should weigh.

## 12. Observations recorded, not acted on

- The dialog's Save and Clipboard buttons start their own worker thread, which
  hides the dialog and runs `doExport` (including the save chooser) off the EDT,
  for Classic and GeoCeDG alike (`GraphicExportDialog.java:363-381`).
- `GeoGebraMenuBar.showPrintPreview` builds and shows `PrintPreviewD` on a
  worker thread; GeoCeDG v2's `host.document.print-preview` calls it
  (`GeoGebraMenuBar.java:252-272`, `GeoCeDGActionRegistry.java:311-312`).
- `FileMenuD`'s `newWindowAction`, `drawingPadToClipboardAction`,
  `exportWorksheet` and `exportGeoGebraTubeAction` use the same worker pattern.
- Cancel hides each dialog without disposing it, and every launch creates a new
  one: the probes retained one hidden `GraphicExportDialog` window per open,
  on every route. Pre-existing; not a lifecycle defect of the launch.
- **Classic startup off the EDT (independent).** `GeoGebra3D.main` calls the
  upstream `GeoGebra.doMain(cmdArgs, GeoGebraFrame3D::new)` overload, which
  passes `initializeOnEventDispatchThread = false`, so `GeoGebraFrame.init`
  builds and shows the Classic main frame on the main thread
  (`GeoGebra.java:53-57`, `:107-113`); GeoCeDG's overload passes `true`.
  In the `P_R6PLUS_C` campaign one of 100 Classic launches
  (`cur-metal` run 10) ended with exit code 10 before the probe's first step:
  a `NullPointerException` in `Component.getMaximumSize` from `BoxLayout`,
  during `JToolBar` layout inside `GeoGebraFrame.setVisible` ←
  `GeoGebraFrame.createNewWindow` ← `GeoGebraFrame.init` ←
  `GeoGebra.doMain` on the main thread. No GeoCeDG launch failed (10 of 10).
  This is a different call site and a different lifecycle from
  the picture dialog; it neither causes nor blocks the launch defect, and
  option A does not change it. It may, however, disturb an author smoke of the
  Classic application, so it is reported here for a separate author
  disposition.
- `showGraphicExport()` calls `clearSelectedGeos` and `updateSelection` first;
  on the threaded route these kernel-selection updates also run off the EDT.
  Option A moves them to the EDT, where the GeoCeDG v2 route already runs them.

## 13. Verification classification

`geocedg/specs/operations/verification-levels.md` §12.8 selects the class from
the concrete impact. The candidate delta is one upstream Desktop file, one menu
action of the Classic menu bar (and of GeoCeDG's v1 fallback), no shared or
kernel code, no serialization, no export bytes, no profile catalog, no verifier
infrastructure beyond the ordinary registration of a phase selection (which the
mini-track plan §12 classifies as phase-local work). No concrete cross-module
integration obligation exists that `INTEGRATION` would add: the GeoCeDG v2
route is unchanged and is covered by the existing Desktop tests and the phase's
regression test. `BOUNDED_PHASE` (registered `PHASE` is sufficient) is
therefore proposed; it was not silently inherited from `C`'s
`INTEGRATED_PHASE`. Escalation triggers are listed in the prompt's stop
conditions. The class is frozen only by the authorizing instruction
(`DQ-X1-3`).

## 14. Naming

Precedent: `PRE-G9B-R3-X1` resolved technical debt left by a closed phase
under an `X` suffix; `-R<n>` names revisions of the same phase in this track
(`R5-B-R1`, `R5-B-R2`, `T_R6PLUS_C` as revision 1 of `C`), and `C-R1` would
collide with that use. Proposed: `PRE-G9B-R6-plus-C-X1`, registry phase id
`PRE-G9B-R6-PLUS-C-X1`, prompt
`.github/prompts/tasks/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt.prompt.md`.
Not author-approved (`DQ-X1-1`).

## 15. Changed paths of the preparation

- `.github/prompts/tasks/pre-g9b-r6-plus-c-x1-classic-picture-dialog-edt.prompt.md` (new)
- `docs/validation/pre_g9b_r6_plus_c_x1_preparation_characterization_report.md` (new, this report)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-x1-preparation-characterization.json` (new)
- `docs/roadmap/geocedg_roadmap.md` (one status row; documental version 4.63)
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` (status lines only)

The prompt is not added to `geocedg/specs/operations/prompt-contracts.json`,
following the `PRE-G9B-R6-plus` precedent; its contract fields were validated
with `Test-PromptContractDocument` against the `task` profile (no missing,
duplicate, unknown or out-of-order field; execution-safe). The `STATIC` run of
the frozen preparation candidate is reported outside this artifact, because it
cannot name its own commit.

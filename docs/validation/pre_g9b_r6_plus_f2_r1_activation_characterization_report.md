# PRE-G9B-R6-plus-F2-R1 — dual-mode Parallel/Perpendicular activation: characterization report

- Activity: `PRE-G9B-R6-plus-F2-R1-ACTIVATION-CHARACTERIZATION` (Stage 1; author
  instruction of 2026-10-11, "Characterization of Dual-Mode Parallel and
  Perpendicular Tool Activation")
- Status: **CHARACTERIZATION COMPLETE — DOCUMENTARY CANDIDATE — PENDING AUTHOR REVIEW**
- Authorization: focused characterization and design alternatives only;
  product implementation and publication **not authorized**
- Published `main`: `6d9bafb04235a71c8dfadef9631cd6057964b1f9`, tree
  `6bc65715cd37f5872b804eea7949d2f9fea96092`
- Frozen F2 technical candidate (not author approved, not published):
  `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873`, tree
  `3d412a801024411ebfcbaf8469691ec00f7c4fee`, parent (authorization)
  `bdb9b6a94cf95ccebd1beb2f3f31f2bab523c75a`
- Machine-readable mirror:
  [`pre-g9b-r6-plus-f2-r1-activation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-activation-characterization.json)
- Alternatives: [activation alternatives candidate](../architecture/pre_g9b_r6_plus_f2_r1_activation_alternatives_candidate.md);
  questions: [author-decision preparation record](pre_g9b_r6_plus_f2_r1_author_decision_preparation_record.md)
- `selfApproved = false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`

This report characterizes; it decides nothing. The approved F2 decisions
`DQ-F2-1` to `DQ-F2-10`, the F2 reconciled design, the F2 specification and the
F2 candidate are unchanged.

Evidence classes: **S** proven from source at `6af85ee3`; **P** proven by the
scratch probe of `R12`; **I** inferred (standard JDK behavior or reading, not
executed); **NE** `NOT_ESTABLISHED` in the test environment.

Abbreviations: `desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`;
`geocedg/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`;
`common/` = `source/shared/common/src/main/java/org/geogebra/common/`; **TT** =
`desktop/gui/toolbar/ToolToggleButton.java`; **MTM** =
`desktop/gui/toolbar/ModeToggleMenuD.java`; **GCEC** =
`geocedg/GeoCeDGEuclidianController.java`.

## R0 — Entry gate

| Check | Result |
|---|---|
| local `main`, `origin/main`, live remote `main` | `6d9bafb0` (all three) |
| F2 candidate | `6af85ee3` / `3d412a80`, parent `bdb9b6a9` / `674fcfd4`, whose parent is `6d9bafb0` |
| F2 candidate published | no (not on any remote ref; not contained in a remote branch) |
| F2 acceptance | `PHASE` `verification-ac975f8c79ec4d58ba018ca4eb2a4cbd` 2/2, `STATIC` `verification-49e939c13b71429bbcf246ca08480c3f` 3/3, `INFRA_UNIT` `verification-2a4c7afa3282477ca3412c0a8de49f33` 22/22, all `ACCEPTED / COMPLETE` on `6af85ee3` / `3d412a80` |
| worktree | clean |
| characterization branch | `phase/pre-g9b-r6-plus-f2-r1-activation` (local), from `6af85ee3`; the F2 candidate, its branch and its receipts are untouched |

Author-reported context: the interactive smoke of the existing F2 behavior was
satisfactory; the author requests an interaction-contract revision before F2
closeout.

## R1 — Method

Source reading of the toolbar, menu, registry, key-dispatch and mode-setting
paths at `6af85ee3`; one scratch JUnit probe (`F2R1ActivationProbeTest`) in the
Desktop test source set through a Gradle init script, never in a tracked path.
The probe builds the real GeoCeDG toolbar (`buildApplicationPanel`,
`GeoCeDGToolbarContainer.buildGui`), finds the native group that holds
`construction.parallel-line`, and **posts** real `MouseEvent`s to the system
`EventQueue`, so Swing fills the button's `ActionEvent` modifiers as it does in
the product. A listener added to the button after construction — as a GeoCeDG
adapter would be — records the `ActionEvent` modifiers, the current AWT event,
`app.getMode()` at notification time and `AppD.getControlDown()`; a client
`EventListener` records `SET_MODE` events. Afterwards `compileTestJava` was
rerun without the init script and the probe test-result XML and compile-stash
copies were removed.

## R2 — Activation event chain

```text
keyboard modifier state     AppD.controlDown (global; key events only)       S
        |                   or the click's own InputEvent modifiers           S/P
toolbar interaction         ToolToggleButton (inherited, package-private)     S
        |                   BasicButtonListener press/release -> ActionEvent  I/P
tool activation event       ToolToggleButton.actionPerformed(e): TT:89-95     S
        |                   reads only e.getActionCommand(); modifiers lost   S
App.setMode / ModeSetter    App.setMode(int) -> setMode(mode, TOOLBAR)        S
        |                   (common/main/App.java:1660-1662, 2384-2400)
GuiManager.setMode          kernel.notifyModeChanged -> view.setMode          S
        |                   (common/gui/GuiManager.java:522-576; Kernel.java:3776-3782)
EuclidianView.setMode       same mode (except MODE_IMAGE): return             S
        |                   (common/euclidian/EuclidianView.java:783-786)
GeoCeDG controller          GCEC.setMode(int, ModeSetter) (:603-628):         S
        |                   gesture cleanup, similarity reset, zoom cancel
operating behaviour         none today: F2 memory is always active            S
        |
construction                GCEC.switchModeForProcessMode -> processLineReferenceTool (:752-765)  S
```

- The flyout route: `MTM.MenuItemListener.actionPerformed` (MTM:210-230) calls
  `selectItem(item, TOOLBAR)` (visual only, MTM:130-149) and then
  `tbutton.doClick()`; the mode change therefore also goes through
  `TT.actionPerformed` (S). The toolbar sync back from a mode change
  (`ProfileToolbar.setMode` → `MTM.selectMode` → `selectItem(…, DOCK_PANEL)`)
  never calls `app.setMode` (S).
- No route passes modifiers into `App.setMode`, `GuiManager.setMode` or the
  controller; both `setMode` signatures carry only the mode and the
  `ModeSetter`, which is `TOOLBAR` for every user route (toolbar, menu,
  registry, user tools, scripting `GgbAPI.setMode`) (S).
- `ModeSetter` values: `DOCK_PANEL`, `TOOLBAR`, `CAS_VIEW`,
  `EXIT_TEMPORARY_MODE`, `CAS_BLUR` (`common/kernel/ModeSetter.java`) (S).

## R3 — Where Ctrl is available

| Source | Availability | Reliability |
|---|---|---|
| `ActionEvent` of the toolbar button | **present** (`CTRL_MASK`) for direct clicks, flyout selections and reclicks, because Swing builds it from the AWT event being dispatched (P: `R4` A02, A04, A08, A09) | authoritative for that activation |
| `EventQueue.getCurrentEvent()` during the button action | the `MouseEvent` release with its own modifiers (P) | authoritative, but only inside the dispatch |
| `AppD.getControlDown()` | the last **key** event seen by the application `KeyEventDispatcher` (`desktop/main/AppD.java:3848-3855`, 3933-3937); never reset on focus loss; updated before the source-window check; not updated by consumed key events (S) | **not** authoritative: stale after Alt-Tab, a dialog or a key event elsewhere (I); probe A02: `false` while the click carried Ctrl (the test host has no window key dispatcher) |
| `App.setMode`, `ModeSetter`, `GCEC.setMode` | absent (S) | — |

**Stable point:** an `ActionListener` on the native group's toggle button sees
the activation's own modifiers **before** `app.setMode` runs: Swing notifies
listeners last-added first, and the button registers itself in its constructor
(TT:74-76). Probe: `modeAtListener` is always the previous mode (P).

**Owned seam:** GeoCeDG builds every native group itself in
`GeoCeDGToolbarContainer.ProfileToolbar.addNativeGroup`
(`geocedg/GeoCeDGToolbarContainer.java:200-221`), which holds the
`ModeToggleMenuD` and can reach its button through the public
`getJToggleButton()` (MTM:98-100) and the public `JToggleButton.addActionListener`.
No inherited class needs a change (S). Parallel and Perpendicular are in the
native group `construction-derived` (`apps/geocedg/application-profile.yml:3563-3567`)
with Perpendicular Bisector, Angle Bisector, Angle with Given Size and Tangent;
the group button shows the most recently selected tool of the group (S).

## R4 — Activation matrix

Probe output `f2r1.activation.txt` (P unless stated). "Ctrl on event" = the
button `ActionEvent` modifier; "setMode" = a `SET_MODE` event; "controller" =
whether `GCEC.setMode` is reached (S: only on a real mode change, `R5`).

| Id | Scenario | Originating event | Ctrl on event | `AppD.getControlDown` | setMode | Controller activation | Classifiable | Inherited gesture affected | Inherited change needed |
|---|---|---|---|---|---|---|---|---|---|
| A01 | normal click, Parallel button | button release | no | false | yes (`TOOLBAR`) | yes | NORMAL | no | no |
| A02 | Ctrl + click, Parallel button | button release | **yes** | false | yes | yes | MEMORY_ENABLED | no | no |
| A03 | normal click, button showing Perpendicular | button release | no | false | yes | yes | NORMAL | no | no |
| A04 | Ctrl + click, Perpendicular button | button release | **yes** | false | yes | yes | MEMORY_ENABLED | no | no |
| A05 | Ctrl released right after activation | key dispatcher | yes (click) | true → false | once | yes | MEMORY_ENABLED kept by an activation-time rule | no | no |
| A06 | Ctrl pressed/released during construction | key dispatcher | — | true → false | **no** | no | no new activation (a construction-time Ctrl is the inherited canvas modifier only) | no | no |
| A07 | normal reclick of the active tool | button release | no | false | **yes** | **no** (`EuclidianView.setMode` same-mode return); pending selection kept (1 → 1) | the button event is classifiable; the controller sees nothing | no | no |
| A08 | Ctrl + reclick of the active tool | button release | **yes** | false | **yes** | **no**; pending selection kept | button event classifiable | no | no |
| A09 | tool chosen from the flyout | menu item → `tbutton.doClick()` | follows the dispatched event (yes with Ctrl, no without) | false | yes | yes (new mode) | yes | no | no |
| A09b | flyout reselects the active tool, Ctrl | as A09 | yes | false | yes | no | yes | no | no |
| A10 | menu / action registry (`GeoCeDGActionRegistry.invoke`) | `ActionEvent` with `CTRL_MASK` | **discarded** (registry calls `app.setMode(mode)`, `GeoCeDGActionRegistry.java:246-255`) | false | yes | yes | not from the button; only if the GeoCeDG registry were made to classify | no | no (registry is GeoCeDG-owned) |
| A11 | keyboard (Space on the focused button) | key on button | — | — | none | none | **NE** (the headless host has no focus owner); I: Swing fires the same `actionPerformed` with the key event's modifiers | — | — |
| A12 | focus between toolbar and Graphics View | — | — | — | — | — | **NE** headless; S: the button event does not depend on focus; `AppD.controlDown` does | — | — |
| A13 | other tool, then Ctrl-click back | button release | yes | false | yes | yes | MEMORY_ENABLED | no | no |
| A14 | switch Parallel → Perpendicular (flyout) | flyout | as dispatched | false | yes | yes | yes | no | no |
| A15 | modal dialog opened and closed before activation | — | — | possibly stale (S/I) | — | — | **NE** headless; the button event is unaffected, `AppD.controlDown` is not reliable | — | — |
| A16 | Escape, then Ctrl-click | button release | yes | false | yes | yes | MEMORY_ENABLED | no | no |
| A17a | `button.doClick()` with no input event (harness) | `InvocationEvent` | **no** | false | yes | yes | NORMAL (synthetic) | no | no |
| A17b | Ctrl mouse events dispatched directly to the button | button | yes (`Component.dispatchEvent` sets the current event) | false | yes | yes | yes | no | no |
| A18a | Ctrl + Shift + click | button release | yes (shift also) | false | yes | yes | yes (other modifiers ignorable) | no | no |
| A18b | Ctrl + right button | — | — | false | **no action** | no | not an activation (Swing fires actions on the left button only; the right press may start the flyout timer, TT:215-234) | no | no |

## R5 — Already-active tool (A07, A08)

- The button fires `actionPerformed` on every click, including a reclick of the
  selected toggle; `App.setMode`, `GuiManager.setMode`, `kernel.notifyModeChanged`
  and the toolbar sync run, and a `SET_MODE` event is dispatched (P).
- `EuclidianView.setMode` returns early for the same mode (S:
  `common/euclidian/EuclidianView.java:783-786`), so **the GeoCeDG controller
  receives no new activation**: `GCEC.setMode` is not called, the pending
  selection survives (P: 1 → 1 in A07 and A08).
- A reclick is therefore observable **only** at the button listener (or at an
  `AppGeoCeDG.setMode` override, which would have no modifiers). It is not
  equivalent to a mode change and must not be treated as one; its effect on the
  operating behaviour needs an explicit rule (`DQ-F2R1-2`).

## R6 — Flyout, keyboard and indirect activation

- **Flyout** (A09, A14): the item listener ends in `tbutton.doClick()`, whose
  `ActionEvent` modifiers come from the event being dispatched — the mouse
  release on the popup item in the product (I), the probe's posted event in the
  test (P). The popup itself was not shown headless; the real popup release is
  **I**, its consequence (classification through the same button listener) is P
  with a simulated dispatch.
- **Menu and registry** (A10): `GeoCeDGMenuBar` items are `JMenuItem(action)`
  (`GeoCeDGMenuBar.java:184-199`) invoking `GeoCeDGActionRegistry.invoke`, which
  discards the event and calls `app.setMode(mode)`; the button listener is not
  reached (the toolbar sync uses `selectItem`, not `doClick`) (S, P). Classifying
  menu activations would need the GeoCeDG-owned registry to read its
  `ActionEvent` — possible, not required.
- **Keyboard** (A11): no toolbar key handling and no accelerators for these
  modes (S); Space on a focused button is **NE** here (I: same action path).
- **Other routes**: profile flyouts of mixed actions build a modifier-less
  `ActionEvent` (`GeoCeDGWorkspaceController.java:~377-378`, not used for these
  modes); pinned user tools, scripting `GgbAPI.setMode`, `setActiveToolbar`
  (`DOCK_PANEL`) and dock-panel toolbars of undocked views (inherited
  `ToolbarD`) carry no modifier (S). Every route other than the GeoCeDG native
  toolbar button is unclassifiable by Ctrl.

## R7 — Platform implications

- Windows 11 (primary): Ctrl on the click is `CTRL_MASK` (P on Windows).
- macOS: `AppD.isControlDown(meta, ctrl)` treats **Cmd** as the multiple-selection
  modifier (`AppD.java:3948-3964`); Ctrl+click on macOS is conventionally a
  secondary (context) click. A Mac rule should use Cmd (`META_MASK`) through the
  same mapping (I; not tested).
- Linux: Ctrl+click usually reaches the application; some window managers bind
  modifier-drag gestures (I; not tested).

## R8 — Feasibility of the activation-state model

- A per-tool, session-only activation behaviour (`NORMAL` / `MEMORY_ENABLED`)
  can live next to the slots in `GeoCeDGLineReferenceMemory` or in the
  controller, independent of the slots (S).
- Setting it: the button listener classifies every native-toolbar activation of
  `MODE_PARALLEL`/`MODE_ORTHOGONAL` from the event's own modifier, before
  `app.setMode`; non-toolbar routes carry no modifier (R6).
- Reading it: `processLineReferenceTool` would skip both injection and the
  certified write in `NORMAL`; the slots, the listener-based invalidation and
  the lazy currentness check stay as they are, so a normal-mode construction
  never writes or clears a slot and an invalidated slot can never be revived by
  a later memory-enabled activation (S: invalidation is independent of reuse).
- Persistence of the behaviour: Ctrl release (A05) and Ctrl presses during
  construction (A06) produce no activation (P), so an activation-time rule is
  stable by construction.
- Reset: entering the tool through any route reaches `GCEC.setMode` (a real mode
  change), which can apply a default; reclicks do not (R5).

Conclusion: **Candidate A is implementable within GeoCeDG-owned Desktop code**
(`ProfileToolbar.addNativeGroup`, `GeoCeDGEuclidianController`,
`GeoCeDGLineReferenceMemory`), with **no inherited-class change**, provided that
non-toolbar routes default to a fixed behaviour and that reclicks follow an
explicit rule. Details and the comparison with Candidates B and C are in the
[alternatives candidate](../architecture/pre_g9b_r6_plus_f2_r1_activation_alternatives_candidate.md).

## R9 — Cost facts for the alternatives

- **Explicit toggle (B):** one declarative product action costs, on the F1
  precedent: the profile entry and menu/group placement; five localized keys in
  EN and ES; a `GeoCeDGActionRegistry` target with `execute` and `checked` cases;
  the catalog count in `GeoCeDGProfile.java:340` (131 → 132); count pins in
  twelve assertions across nine test classes (listed in the JSON mirror); the
  approved-id block of
  `tools/agent/workspace-profile-validation.ps1` with its static-contract pin;
  the `workspace-profile.product` node in the phase (S). A status-bar segment
  (`GeoCeDGStatusBar`, ordered map of segments, no count pin found) costs no
  catalog change but needs localized texts in the profile (S).
- **Separate tool entries (C):** product mode ids live in the upstream
  `common/euclidian/EuclidianConstants.java` (`:451-474`, `getModeText`
  `:793-827`) with tool names in shared `menu*.properties`; the profile loader
  rejects duplicate modes and duplicate audited ids (`GeoCeDGProfile.java:296,
  312-314, 591-606`), and the native toolbar identifies items by mode number
  (MTM:106-117). Two entries per construction type therefore need new mode ids in
  inherited/shared files (S) — outside the approved Desktop-only architecture.

## R10 — Original F2 decisions affected

| Decision | Effect of the dual-mode contract |
|---|---|
| `DQ-F2-1` (memory survives tool changes) | slots still survive; **reuse** becomes conditional on a memory-enabled activation — amended |
| `DQ-F2-2` (replacement after a successful construction) | replacement only in `MEMORY_ENABLED`; a normal-mode construction never writes — amended |
| `DQ-F2-3` (with a valid memory a point or empty click starts a construction) | only in `MEMORY_ENABLED`; `NORMAL` is the inherited tool — amended |
| `DQ-F2-6` (no preview or indicator in v1) | an indication of the active behaviour may be added (`DQ-F2R1-5`) — possibly amended |
| `DQ-F2-10` (Desktop-only; GeoCeDG-owned companion; `BOUNDED_PHASE`) | adds a GeoCeDG-owned toolbar adapter; still Desktop-only — refined |
| `DQ-F2-4`, `DQ-F2-5`, `DQ-F2-7`, `DQ-F2-8`, `DQ-F2-9` | unchanged (types, identity and lifecycle, angle hook, angle visibility, working-layer exclusion) |

## R11 — Verification-class assessment

A corrective F2-R1 implementing Candidate A changes only GeoCeDG-owned Desktop
classes (`GeoCeDGToolbarContainer`, `GeoCeDGEuclidianController`,
`GeoCeDGLineReferenceMemory`) and their tests; with a status-bar indication it
also adds localized profile texts. It can remain **`BOUNDED_PHASE`** with its
own registered phase (proposed `PRE-G9B-R6-PLUS-F2-R1`), whose selection adds the
toolbar classes (e.g. `G9U1WorkspaceSurfaceTest`, `PreG9BR4OrderedListToolTest`)
to the F2 selection, plus `workspace-profile.product` if profile texts change.
Candidate B also fits `BOUNDED_PHASE` (F1 precedent: catalog 130 → 131).
Candidate C needs shared/inherited edits and would escalate. The F2 candidate
`6af85ee3` and its receipts remain historical evidence; a corrective successor
needs its own authorization and fresh verification.

## R12 — Probe record

| Item | SHA-256 |
|---|---|
| output `f2r1.activation.txt` | `0c0570baeee1a7c6af9f18ee10d690eec8dd370526386b16b9eca3280d53208c` |
| probe source `F2R1ActivationProbeTest.java` | `cf88bc602ebb02bbc321b4afcf7dd55bcc593ec41454c33f61a7fa65f0aef492` |
| init script `f2r1probe.gradle` | `1310c6da6d972311084460444f841158dc3e7ca2c730f4051a499ee53c6f68f4` |

One run, 1 test, exit 0. Not established in this environment: real popup
display, keyboard focus traversal and Space activation, focus changes between
windows, modal dialogs, the application-wide `KeyEventDispatcher` (removed by
the test host), macOS and Linux.

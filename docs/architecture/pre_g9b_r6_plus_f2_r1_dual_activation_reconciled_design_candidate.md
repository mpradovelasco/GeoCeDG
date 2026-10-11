# PRE-G9B-R6-plus-F2-R1 — dual-mode activation: reconciled design candidate

- Status: **PROPOSED — RECONCILED DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
  (not author approved; implementation not authorized)
- Produced by: `PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION` (Stage 2; author
  instruction of 2026-10-11/12)
- Bases: frozen F2 candidate `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873` (tree
  `3d412a801024411ebfcbaf8469691ec00f7c4fee`); Stage 1 characterization
  `b7faad3af2cbce0c2254cbdc7366c96a3df5ad48` (tree
  `980207638c8bd045621879b2a28f407104b08bc4`)
- Decisions: [F2-R1 author-decision record](../validation/pre_g9b_r6_plus_f2_r1_author_decisions_record.md)
  v1.0 (`DQ-F2R1-1` to `DQ-F2R1-6`); tests and smoke:
  [test matrix and author-smoke plan](../validation/pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md);
  contract: [canonical corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)
- Evidence: [Stage 1 characterization report](../validation/pre_g9b_r6_plus_f2_r1_activation_characterization_report.md)
  (`R0`–`R12`) and the Stage 2 probe recorded in the decision record §3
- Preserved unchanged: the [F2 author-decision record](../validation/pre_g9b_r6_plus_f2_author_decisions_record.md)
  v1.1, the [F2 reconciled design](pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md),
  the [F2 specification](../../geocedg/specs/ui/authoring-memory.md) (`PROPOSED`),
  the F2 candidate and its receipts. `selfApproved = false`.

Abbreviations: **GCEC** = `GeoCeDGEuclidianController`; **memory** =
`GeoCeDGLineReferenceMemory`; **PAR**/**PERP** = `MODE_PARALLEL` (3) /
`MODE_ORTHOGONAL` (4).

## 1. What changes and what does not

The correction amends **only** when the Parallel and Perpendicular tools read
and write their F2 memory. Everything else in F2 stays in force: exact-instance
identity and currentness, two independent slots, the success certificate,
admissibility (including the constant-function limitation), suspension and
clearing (eager listener plus lazy check), no serialization, no undo step, the
inherited algorithms and gestures, Graphics View 1 scope, Graphics View 2 and
Classic unchanged, Angle with Given Size unchanged, working-layer entry excluded.

| Behaviour of the active tool | Memory read | Memory write | Memory clear | Geometry |
|---|---|---|---|---|
| `NORMAL` | never | never | never (only the F2 lifecycle clears) | inherited tool |
| `MEMORY_ENABLED` | current, usable slot only (F2 §3.3) | certified construction only (F2 §4.3) | never (only the F2 lifecycle clears) | inherited algorithms with F2 release-time injection |

The modifier is never a geometric input: it is read at activation only and has
no effect on hits, selection, preview or the construction.

## 2. Components (all GeoCeDG-owned Desktop code)

| Component | Role |
|---|---|
| **Toolbar activation listener** in `GeoCeDGToolbarContainer.ProfileToolbar.addNativeGroup` | one `ActionListener` per native group button (public `getJToggleButton().addActionListener`); for an action whose command is PAR or PERP it records an *activation request* `{mode, behaviour, token}` where behaviour = `MEMORY_ENABLED` iff the `ActionEvent` carries the platform memory modifier (Ctrl on Windows/Linux; Cmd/`META_MASK` intended on macOS, unverified) and token = `EventQueue.getCurrentEvent()` |
| **Activation coordinator** in `AppGeoCeDG.setMode(int, ModeSetter)` (override of the public, non-final `App.setMode`) | sees **every** mode activation, including same-mode activations from the toolbar, menus, registry, API and scripts (Stage 2 probe `O01`–`O04`); for PAR/PERP it resolves the behaviour: the toolbar request if its mode matches and its token **is** the current AWT event, otherwise `NORMAL`; for any other mode it resets both tools to `NORMAL`; then calls `super.setMode` |
| **Controller application** `GCEC.applyActivationBehaviour(mode, behaviour)` | holds the behaviour of the active tool; on a same-mode activation that **changes** the behaviour it cancels the partial gesture with the inherited protected `initNewMode(mode)` (selections, preview, unfinished free point removed, no undo point, memory untouched — probe `C`); on an unchanged behaviour it does nothing (inherited partial selection preserved); on a mode change the inherited `setMode` → `initNewMode` already cancels the gesture |
| **Memory gate** in `GCEC.processLineReferenceTool` | `NORMAL`: call the inherited `super.switchModeForProcessMode` only (no injection, no read-back, no write); `MEMORY_ENABLED`: the F2 path unchanged |
| **Status indication** in `GeoCeDGStatusBar` | one segment hidden by default, shown only while the active tool is `MEMORY_ENABLED`, text `"<tool name> · Memoria"` / `"<tool name> · Memory"`; refreshed synchronously on the EDT when the behaviour changes and when the language changes |

Nothing in `source/shared/**`, in an inherited toolbar or controller class, in
commands, algorithms, XML, identity, Graphics View 2 or Classic changes.

## 3. Activation routes (contract)

| Route | Evidence | Behaviour | Classification |
|---|---|---|---|
| direct toolbar click on the group button | Stage 1 `A01`–`A04`, `A13`, `A16` | Ctrl → `MEMORY_ENABLED`, else `NORMAL` | **CONFIRMED** |
| reclick of the already selected button | Stage 1 `A07`, `A08`; Stage 2 `O02` | as a direct click; reaches `App.setMode` (`SET_MODE` dispatched); `EuclidianView.setMode` stops before the controller, hence the coordinator calls the controller itself | **CONFIRMED** (observation); resolution **CONDITIONALLY SUPPORTED** until implemented and tested |
| real flyout popup selection (e.g. Perpendicular) | Stage 2 `F01`: shown popup, real `JMenuItem`, posted mouse press/release; the button `ActionEvent` carries Ctrl and the current event is the item's `MouseEvent` | Ctrl → `MEMORY_ENABLED`, else `NORMAL` | **CONFIRMED** (Swing event level; OS-level input injection not used) |
| keyboard: Space on the focused toolbar button | Stage 2 `K01`: plain Space activates with no modifier; Ctrl+Space fires **no** action | `NORMAL` only; the keyboard cannot request memory | **CONFIRMED** |
| menu item / action registry (`GeoCeDGActionRegistry.invoke`) | Stage 1 `A10`; Stage 2 `O03` (same mode reaches `App.setMode`) | `NORMAL`, including when the tool is already active | **CONDITIONALLY SUPPORTED** (coordinator default; to be tested) |
| scripting / API (`GgbAPI.setMode`) and other programmatic `app.setMode` | Stage 2 `O04` | `NORMAL`, also for the same mode | **CONDITIONALLY SUPPORTED** |
| pinned user tools, profile flyouts of mixed actions | Stage 1 `R6` (no modifier) | `NORMAL` | **CONDITIONALLY SUPPORTED** |
| undocked dock-panel toolbars (inherited `ToolbarD`, no GeoCeDG listener) | Stage 1 `R6` | `NORMAL` (no request) | **NOT_ESTABLISHED** at runtime; `NORMAL` by construction |
| Escape | Stage 2 `O11`: `GlobalKeyDispatcher` → `setMoveMode` → `App.setMode(MOVE)` | leaving the tool → `NORMAL` | **CONFIRMED** (route) |
| language change, File → New | Stage 2 `O05`, `O12`: `setMoveMode` | leaving the tool → `NORMAL` | **CONFIRMED** (route) |
| dock focus changes, creating Graphics View 2, undo, redo, clearing `setXML` | Stage 2 `O06`–`O10`: **no** `setMode` call | behaviour unchanged (memory still follows the F2 lifecycle) | **CONFIRMED** |
| focus between windows, modal dialogs | button event independent of `AppD.controlDown` (Stage 1 `R3`) | as the activation event says | **NOT_ESTABLISHED** (no real window focus or modal dialog exercised) |
| macOS Cmd | not executed | intended `MEMORY_ENABLED` via `META_MASK` | **NOT_ESTABLISHED** — not claimed |
| Linux Ctrl | not executed | as Windows | **NOT_ESTABLISHED** — not claimed |

**Why the coordinator, not the controller:** `EuclidianView.setMode` returns
before the controller for a same-mode activation (Stage 1 `R5`), so a reset done
only in `GCEC.setMode` would miss menu, script or toolbar reactivations of an
already active tool. `App.setMode(int, ModeSetter)` has no same-mode check and is
reached by every observed route; overriding it in the GeoCeDG-owned
`AppGeoCeDG` is the smallest seam that sees them all.

**Why a token:** the toolbar listener runs immediately before the button's own
`actionPerformed` calls `app.setMode` in the **same** AWT dispatch. Matching the
recorded `EventQueue.getCurrentEvent()` object binds the request to that
activation only; a request left unused (e.g. a press on the flyout triangle,
where the button does not call `setMode`) can never leak into a later,
unrelated activation.

## 4. Behavioural state matrix

State per tool: behaviour ∈ {`NORMAL`, `MEMORY_ENABLED`}; slots per F2
(`EMPTY` / `HELD` / `SUSPENDED`), independent of the behaviour.

| Current | Event (origin) | Next behaviour | Partial selection | Stored references | Preview | Construction / undo |
|---|---|---|---|---|---|---|
| any tool, NORMAL | Ctrl + Parallel activation (toolbar/flyout) | Parallel `MEMORY_ENABLED` | cancelled by the inherited mode change | unchanged | inherited | none |
| Parallel `MEMORY_ENABLED` | normal Parallel reactivation (toolbar) | `NORMAL` | **cancelled** (`initNewMode`) | unchanged | reset | none; no undo point |
| Parallel `MEMORY_ENABLED` | Ctrl + Parallel reactivation | `MEMORY_ENABLED` | **kept** (inherited) | unchanged | unchanged | none |
| Parallel `NORMAL` | normal reactivation | `NORMAL` | **kept** (inherited) | unchanged | unchanged | none |
| Parallel `NORMAL` | Ctrl + reactivation | `MEMORY_ENABLED` | **cancelled** (`initNewMode`) | unchanged | reset | none; no undo point |
| Parallel `MEMORY_ENABLED` | switch to Perpendicular without Ctrl (flyout/toolbar) | Perpendicular `NORMAL`; Parallel reset `NORMAL` | cancelled by the mode change | unchanged (both slots) | inherited | none |
| Parallel `MEMORY_ENABLED` | switch to Perpendicular with Ctrl | Perpendicular `MEMORY_ENABLED` | cancelled by the mode change | unchanged | inherited | none |
| either tool, any | menu / registry / script activation of the same tool | `NORMAL` | cancelled if the behaviour was `MEMORY_ENABLED`, else kept | unchanged | reset if cancelled | none |
| either, `MEMORY_ENABLED` | Escape / another tool / language change / New | `NORMAL` | cancelled by the mode change (inherited) | unchanged by the reset (New clears by the F2 lifecycle) | inherited | none |
| either | Ctrl pressed or released later | unchanged | unchanged | unchanged | unchanged | none |
| either | undo / redo / rebuild | unchanged | — | cleared (F2 eager + lazy); no stale reuse | — | inherited undo |
| either | Open / document replacement | unchanged by the load itself (no `setMode`, probe `O10`) | — | cleared (F2 document boundary) | — | — |
| `NORMAL` | point-first or reference-first gesture | `NORMAL` | inherited | **not read, not written, not cleared** | inherited | inherited tool, one undo point per creation |
| `MEMORY_ENABLED` | point-first click with a `HELD` slot | `MEMORY_ENABLED` | injected at release only | read (exact instance) | inherited | inherited algorithm; one undo point |
| `MEMORY_ENABLED` | certified construction (any gesture) | `MEMORY_ENABLED` | — | slot of this tool written | — | one undo point (memory none) |

Properties demonstrated by the matrix:

- `NORMAL` never consults, updates or clears a slot; a valid remembered reference
  survives any number of normal-mode constructions with other references.
- `MEMORY_ENABLED` uses only current, usable references, writes only after a
  certified construction, keeps the two slots independent and never infers
  identity from labels.
- A slot invalidated while `NORMAL` stays invalid: enabling memory later cannot
  revive it (currentness is checked at every use; the lifecycle runs regardless
  of the behaviour).
- Changing the behaviour never creates an object or an undo point.

## 5. Status indication

- Segment `tool-memory` added to `GeoCeDGStatusBar` (ordered segments, hidden by
  default like `sheet-coherence` and `paste-notice`), with its own hidden
  separator.
- Visible iff the active mode is PAR or PERP **and** its behaviour is
  `MEMORY_ENABLED`; text from the profile `localized_text`, e.g. key
  `F2R1.Memory.Status` = `"%0 · Memoria"` / `"%0 · Memory"` with `%0` the localized
  tool name (`app.getToolName(mode)`); optional tooltip explaining the Ctrl
  activation.
- It states the behaviour, not the slot: it never implies that a reference is
  remembered or valid.
- Refreshed synchronously on the EDT by the coordinator and on language change
  (memories: deferred Swing refresh races; visibility must be checked with laid-out
  bounds, not `isVisible()` alone).
- Cost: two to four localized text keys (EN, ES) in
  `apps/geocedg/application-profile.yml`; no action, catalog count, toolbar icon or
  registry change. The implementation entry gate must confirm that free
  `localized_text` keys pass `workspace-profile-validation.ps1` (existing
  precedent: `Units.Status.*`, `Workspace.Layer.Status`).
- Smaller alternative if that gate fails: a tooltip on the status bar's existing
  layer segment is **not** proposed (it would mix concerns); instead the author
  may choose no indicator (`DQ-F2R1-5` I0).

## 6. Classes and tests affected by an implementation (forecast)

- Production (GeoCeDG-owned): `AppGeoCeDG`, `GeoCeDGToolbarContainer`,
  `GeoCeDGEuclidianController`, `GeoCeDGLineReferenceMemory` (behaviour fields only
  if kept there), `GeoCeDGStatusBar`; profile texts.
- Tests: a new `PreG9BR6PlusF2R1DualActivationTest`; the F2 test class
  `PreG9BR6PlusF2AuthoringMemoryTest` must activate `MEMORY_ENABLED` explicitly
  (today it activates with `app.setMode`, which becomes `NORMAL`) — its
  obligations are unchanged; status-bar and toolbar regression classes join the
  phase selection.

## 7. Verification class (proposal)

`BOUNDED_PHASE`, frozen at the implementation entry gate, with a **new**
registered phase `PRE-G9B-R6-PLUS-F2-R1`: `compile.desktop.semantic`,
`junit.desktop.pre-g9b-r6-plus-f2-r1.semantic` and `workspace-profile.product`
(profile texts). Justification: Desktop-only GeoCeDG-owned code, no shared,
inherited, kernel, XML or catalog change; the precedent F1 used the same class
with a larger profile delta. The F2 receipts remain historical and are not
inherited by the successor.

## 8. Stop conditions

- The behaviour cannot be resolved for a same-mode indirect activation without a
  change outside GeoCeDG-owned code.
- A host path calls `app.setMode` with the active PAR/PERP mode in a way that
  would silently reset an intentional `MEMORY_ENABLED` (none observed; Stage 2
  `O06`–`O10`).
- `initNewMode` cancellation removes or changes anything other than the partial
  gesture, or stores an undo point.
- The status texts require a catalog or schema change.
- Any `source/shared/**`, inherited-class, command, algorithm, XML or identity
  change becomes necessary.

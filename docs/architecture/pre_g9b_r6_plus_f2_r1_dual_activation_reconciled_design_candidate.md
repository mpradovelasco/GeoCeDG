# PRE-G9B-R6-plus-F2-R1 — dual-mode activation: reconciled design

- Status: **DESIGN — AUTHOR APPROVED** (author decisions on `DQ-F2R1-1` to
  `DQ-F2R1-6` recorded in the [F2-R1 author-decision record](../validation/pre_g9b_r6_plus_f2_r1_author_decisions_record.md)
  v1.1, which prevails over this design wherever they differ). The approval
  covers the design only; **implementation is not authorized**; feasibility items
  `FR-1` to `FR-4` (§8) are resolved at the implementation entry gate.
- Scripting clarification: record v1.3 §7 (author disposition after the `FR-2`
  stop) makes reference memory interactive-only and replaces the scripting seam
  of §2, §4 and §8 by Option O1 (single-argument `setMode(int)` origin); the
  rows marked "v1.3" below follow it.
- Produced by: `PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION` (Stage 2),
  reconciled by `PRE-G9B-R6-plus-F2-R1-DESIGN-CLOSEOUT`
- Bases: frozen F2 candidate `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873` (tree
  `3d412a801024411ebfcbaf8469691ec00f7c4fee`, not author approved, historical
  technical evidence); Stage 1 `b7faad3af2cbce0c2254cbdc7366c96a3df5ad48`; Stage 2
  `d630b8dd3ace0cafd28f9896a8964778dd346e2e` (tree `3ed8c9689f11a7a877eaae08c1b97f7fbaf54c12`)
- Tests and smoke: [test matrix and author-smoke plan](../validation/pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md);
  contract: [canonical corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)
- Evidence: [Stage 1 characterization report](../validation/pre_g9b_r6_plus_f2_r1_activation_characterization_report.md)
  (`R0`–`R12`); Stage 2 probe (decision record §3)
- Preserved unchanged: the [F2 author-decision record](../validation/pre_g9b_r6_plus_f2_author_decisions_record.md)
  v1.1, the [F2 reconciled design](pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md),
  the [F2 specification](../../geocedg/specs/ui/authoring-memory.md) (`PROPOSED`, not
  promoted), the F2 candidate and every earlier receipt. `selfApproved = false`.

Abbreviations: **GCEC** = `GeoCeDGEuclidianController`; **PAR**/**PERP** =
`MODE_PARALLEL` (3) / `MODE_ORTHOGONAL` (4).

## 1. What changes and what does not

F2-R1 refines only the **interaction conditions** of `DQ-F2-1`, `DQ-F2-2`,
`DQ-F2-3`, `DQ-F2-6` and `DQ-F2-10`. Geometric admissibility (including the
constant-function limitation), reference identity, currentness, suspension,
lifecycle, the successful-construction certificate, no serialization, no memory
undo step, inherited algorithms and gestures, Graphics View 1 scope, Graphics
View 2 and Classic, Angle with Given Size and the working-layer exclusion are
unchanged.

| Behaviour of the active tool | Memory read | Memory write | Memory clear | Geometry |
|---|---|---|---|---|
| `NORMAL` | never | never | never (only the F2 lifecycle clears) | inherited tool |
| `MEMORY_ENABLED` | current, usable slot only | certified construction only | never (only the F2 lifecycle clears) | inherited algorithms with F2 release-time injection |

The modifier is read only from the activation event; it is never a geometric
input and has no effect on hits, selection, preview or construction.

## 2. Activation requests and host synchronization

The implementation distinguishes **genuine activation requests** from **host
UI synchronization** (`DQ-F2R1-4`) by marking requests at their GeoCeDG-owned
origins:

| Origin (all GeoCeDG-owned) | Request |
|---|---|
| native toolbar button / native flyout (`ProfileToolbar.addNativeGroup` listener; keyboard Space on the focused button reaches the same action) | PAR/PERP with `MEMORY_ENABLED` iff the `ActionEvent` carries Ctrl (Windows), else `NORMAL`; bound to the current AWT event |
| menu items and the action registry (`GeoCeDGActionRegistry.invoke`) | `NORMAL` |
| pinned user tools and other GeoCeDG `app.setMode` call sites for these modes | `NORMAL` |
| scripting (`setMode` of the scripting API) | v1.3: `NORMAL`, also for the active tool, through the unmarked single-argument `AppGeoCeDG.setMode(int)` route (Option O1); the original `GgbAPID` is kept; a script never selects `MEMORY_ENABLED` |
| any other unmarked single-argument `setMode(int)` | v1.3: genuine request → `NORMAL`; a direct two-argument `setMode(int, ModeSetter)` call stays host synchronization |

Resolution in the GeoCeDG-owned `AppGeoCeDG.setMode(int, ModeSetter)` override,
which sees every mode activation including same-mode ones (Stage 2 `O01`–`O04`):

1. a pending request for the target mode → its behaviour (same mode included);
2. no request, target mode differs from the active mode → **tool departure**:
   both tools return to `NORMAL`; if the target is PAR/PERP, it starts `NORMAL`;
3. no request, same mode → **host synchronization**: behaviour unchanged.

The request is consumed or discarded at the end of the activation that produced
it (toolbar: identity of the current AWT event; registry and other call sites: a
scoped marker around their own `app.setMode` call), so an unused request never
leaks into a later activation.

Applying a behaviour (`GCEC.applyActivationBehaviour`): same mode and unchanged
behaviour → nothing (inherited partial selection kept); same mode and changed
behaviour → cancel the incomplete gesture with the inherited protected
`initNewMode(mode)` (verified conditions in §6); mode change → the inherited
`setMode` path already cancels the gesture.

## 3. Language change (`DQ-F2R1-3`)

The inherited `AppD.setLanguage(Locale)` calls `setMoveMode()` before reloading
resources (`desktop/main/AppD.java:2171-2180`, call at `:2179`; Stage 2 `O05`), i.e. the host
leaves the active tool. The author requires that a language change by itself
does not reset the active behaviour or the remembered references:

- references: unaffected (no `reset()`, no `REMOVE`; Stage 1 census `C06`);
- behaviour: a GeoCeDG-owned `AppGeoCeDG.setLanguage(Locale)` override captures
  the active PAR/PERP mode and its behaviour, treats the inherited
  `setMoveMode` as host synchronization (no departure reset), and after the
  inherited change restores the same tool with the same behaviour as a
  synchronization request (no gesture survives the inherited switch); the status
  indication is re-rendered in the new language. Feasibility: `FR-1`.

## 4. Activation routes (contract)

| Route | Evidence | Behaviour | Classification |
|---|---|---|---|
| direct toolbar click | Stage 1 `A01`–`A04`, `A13`, `A16` | Ctrl → `MEMORY_ENABLED`, else `NORMAL` | **CONFIRMED** (Windows) |
| reactivation of the selected button | Stage 1 `A07`, `A08`; Stage 2 `O02` | as a direct click | observation **CONFIRMED**; resolution to be verified |
| native Parallel/Perpendicular flyout | Stage 2 `F01` (shown popup, real item, posted events) | as a direct click | **CONFIRMED** (Swing event level) |
| keyboard Space on the focused button | Stage 2 `K01` | `NORMAL`; Ctrl+Space fires no activation and is **not** claimed | **CONFIRMED** |
| menu / registry, also for the active tool | Stage 1 `A10`; Stage 2 `O03` | `NORMAL` | design-supported; to be verified |
| scripting API, also for the active tool | Stage 2 `O04` | v1.3: `NORMAL` (Option O1); memory never read, written or cleared by the script | **CONDITIONAL** (revised `FR-2` gate) |
| pinned user tools | Stage 1 `R6` | `NORMAL` | design-supported |
| undocked dock-panel toolbars (inherited, no listener) | Stage 1 `R6` | target differs → departure `NORMAL`; same mode → unchanged | **NOT_ESTABLISHED** at runtime |
| host re-assertion of the active mode | none observed (Stage 2 `O06`–`O10`) | unchanged | design rule |
| Escape, another tool | Stage 2 `O11` | departure → `NORMAL`; references kept | **CONFIRMED** route |
| language change | Stage 2 `O05` | behaviour and references kept (§3) | **CONDITIONAL** (`FR-1`) |
| File → New, Open, rebuilds, undo/redo | Stage 2 `O08`–`O12` | New: departure `NORMAL`; references per F2 lifecycle; never a stale reuse | **CONFIRMED** routes |
| window focus, modal dialogs | — | as the activation event says | **NOT_ESTABLISHED** |
| macOS (Cmd), Linux (Ctrl) | — | not claimed | **NOT_ESTABLISHED** |

## 5. Behavioural state matrix

Per tool: behaviour ∈ {`NORMAL`, `MEMORY_ENABLED`}; slots per F2 (`EMPTY` /
`HELD` / `SUSPENDED`), independent of the behaviour.

| Current | Event (origin) | Next behaviour | Partial selection | Stored references | Construction / undo |
|---|---|---|---|---|---|
| any, `NORMAL` | Ctrl + Parallel activation (toolbar/flyout) | Parallel `MEMORY_ENABLED` | cancelled by the inherited mode change | unchanged | none |
| Parallel `MEMORY_ENABLED` | normal Parallel reactivation | `NORMAL` | **cancelled** (`initNewMode`) | unchanged | none; no undo step |
| Parallel `MEMORY_ENABLED` | Ctrl + Parallel reactivation | `MEMORY_ENABLED` | **kept** | unchanged | none |
| Parallel `NORMAL` | normal reactivation | `NORMAL` | **kept** | unchanged | none |
| Parallel `NORMAL` | Ctrl + reactivation | `MEMORY_ENABLED` | **cancelled** (`initNewMode`) | unchanged | none; no undo step |
| Parallel `MEMORY_ENABLED` | switch to Perpendicular without Ctrl | Perpendicular `NORMAL`; Parallel `NORMAL` | cancelled by the mode change | unchanged (both slots) | none |
| Parallel `MEMORY_ENABLED` | switch to Perpendicular with Ctrl | Perpendicular `MEMORY_ENABLED` | cancelled by the mode change | unchanged | none |
| either, any | menu / registry / user-tool activation of the same tool | `NORMAL` | cancelled iff the behaviour changes | unchanged | none |
| either, any | host synchronization of the same mode | unchanged | unchanged | unchanged | none |
| either, `MEMORY_ENABLED` | Escape / another tool | `NORMAL` | cancelled by the mode change | unchanged | none |
| either, any | language change | unchanged (tool restored, `FR-1`) | lost by the inherited switch | unchanged | none |
| either | Ctrl pressed or released later | unchanged | unchanged | unchanged | none |
| either | undo / redo / rebuild | unchanged | — | F2 eager + lazy invalidation; never a stale reuse | inherited undo |
| either | File → New / Open / document replacement | New: `NORMAL`; Open: unchanged by the load itself | — | F2 document-boundary invalidation | — |
| `NORMAL` | any construction gesture | `NORMAL` | inherited | **not read, written or cleared** | inherited tool; one undo point per creation |
| `MEMORY_ENABLED` | point-first click with a `HELD` slot | `MEMORY_ENABLED` | injected at release only | read (exact instance) | one undo point |
| `MEMORY_ENABLED` | certified construction | `MEMORY_ENABLED` | — | slot of this tool written | one undo point (memory none) |

Demonstrated properties: `NORMAL` never consults, updates or clears a slot;
`MEMORY_ENABLED` uses only current, usable references, writes only after a
certified construction, keeps the two slots independent and never infers
identity from labels; a slot invalidated while `NORMAL` cannot be revived by a
later memory activation; a behaviour change is never a geometric construction.

## 6. Behaviour-change cancellation (`DQ-F2R1-2`)

`initNewMode(mode)` is the inherited protected cancellation of a tool change,
called from the GeoCeDG controller subclass only when a same-mode activation
changes the behaviour. Stage 2 probe `C`: selection cleared, unfinished
empty-space point removed, no undo store, mode and memory unchanged. The
implementation must additionally verify (`FR-3`): only provisional geometry of
that incomplete gesture is discarded; completed constructions are intact; no
undo or redo step is introduced; the saved/unsaved document state stays coherent;
remembered references are preserved unless invalidated by the F2 lifecycle.

## 7. Status indication (`DQ-F2R1-5`)

- Segment `tool-memory` (plus its own hidden separator) in `GeoCeDGStatusBar`,
  hidden by default like `sheet-coherence` and `paste-notice`, compatible with
  the single-row layout.
- Visible iff the active mode is PAR or PERP and its behaviour is
  `MEMORY_ENABLED`; it states the behaviour, never whether a reference is held or
  usable; hidden in `NORMAL` and for every other tool; updated synchronously on
  the EDT at every activation, reactivation, departure and language change.
- Texts (profile `localized_text`, EN/ES): Parallel → "Parallel Line · Memory" /
  "Paralela · Memoria"; Perpendicular → "Perpendicular Line · Memory" /
  "Perpendicular · Memoria". No action, catalog entry or toolbar icon.
- Entry-gate check (`FR-4`): free `localized_text` keys pass
  `workspace-profile-validation.ps1`; visibility tested with laid-out bounds.

## 8. Feasibility items for the implementation entry gate

| Id | Item | If it fails |
|---|---|---|
| `FR-1` | Language change: an `AppGeoCeDG.setLanguage(Locale)` override can capture and restore the active PAR/PERP tool and its behaviour around the inherited `setMoveMode` without breaking the GUI refresh, Input Help or the saved state | stop for author disposition (the inherited switch to Move would otherwise reset the behaviour) |
| `FR-2` | Scripting `setMode` can be marked as a genuine `NORMAL` request through a GeoCeDG-owned seam (e.g. a GeoCeDG subclass of `GgbAPID` returned by `AppGeoCeDG`) without an inherited-class change. **Failed** for the `GgbAPID` subclass (JavaScript sandbox; historical). v1.3: revised to Option O1 — the unmarked single-argument `AppGeoCeDG.setMode(int)` route is a genuine `NORMAL` request; `GgbAPID`, the class shutter and the shared allow-list unchanged | stop and report; no global Ctrl flag, stack-trace inference, API replacement or sandbox change |
| `FR-3` | `initNewMode` cancellation conditions of §6, including saved-state coherence | stop for author disposition |
| `FR-4` | free status texts accepted by the profile validator; indication laid out in the single-row bar | stop; the author may choose a smaller indication |

## 9. Components and verification

- Production (GeoCeDG-owned): `GeoCeDGToolbarContainer` (listener),
  `AppGeoCeDG` (`setMode` and `setLanguage` overrides), `GeoCeDGActionRegistry` and
  user-tool call sites (request markers), `GeoCeDGEuclidianController` (behaviour,
  `applyActivationBehaviour`, `NORMAL` gate), `GeoCeDGLineReferenceMemory` (no rule
  change), `GeoCeDGStatusBar` (segment); profile texts; a scripting-API seam only
  under `FR-2`.
- Tests: a new `PreG9BR6PlusF2R1DualActivationTest`;
  `PreG9BR6PlusF2AuthoringMemoryTest` adapted to activate `MEMORY_ENABLED`
  explicitly (obligations unchanged).
- Class: `BOUNDED_PHASE`, conditioned on the entry gate; phase
  `PRE-G9B-R6-PLUS-F2-R1` with `compile.desktop.semantic`,
  `junit.desktop.pre-g9b-r6-plus-f2-r1.semantic`, `workspace-profile.product`. No
  inherited toolbar or controller modification, shared change, new mode id, XML
  change or geometric command. The F2 receipts are not inherited.

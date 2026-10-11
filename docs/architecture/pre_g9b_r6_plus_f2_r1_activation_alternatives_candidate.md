# PRE-G9B-R6-plus-F2-R1 — dual-mode activation: alternatives candidate

- Status: **PROPOSED — ALTERNATIVES CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-F2-R1-ACTIVATION-CHARACTERIZATION` (Stage 1;
  characterization and design alternatives only)
- Base: the frozen F2 technical candidate `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873`
  (tree `3d412a801024411ebfcbaf8469691ec00f7c4fee`), on published `main`
  `6d9bafb04235a71c8dfadef9631cd6057964b1f9`
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_f2_r1_activation_characterization_report.md)
  (`R0`–`R12`); questions: [author-decision preparation record](../validation/pre_g9b_r6_plus_f2_r1_author_decision_preparation_record.md)
- Unchanged and still governing the existing F2: the
  [F2 author-decision record](../validation/pre_g9b_r6_plus_f2_author_decisions_record.md),
  the [F2 reconciled design](pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md)
  and the [F2 specification](../../geocedg/specs/ui/authoring-memory.md)
- Nothing here is approved or authorized. `selfApproved = false`.

## 1. Requested contract (author request of 2026-10-11)

Two operating behaviours per tool, no new geometric command:

```text
Tool:                 PARALLEL | PERPENDICULAR
Activation behaviour: NORMAL | MEMORY_ENABLED   (chosen at activation)

NORMAL:          memory read = NO; memory write = NO; inherited tool behaviour
MEMORY_ENABLED:  memory read = existing F2 rules; write = certified construction only
BOTH:            invalidation = existing F2 lifecycle; serialization, kernel and
                 identity unchanged
```

Invariants carried into every candidate:

1. A normal-mode construction never writes, replaces or clears a slot.
2. An invalidated slot is never reused because memory mode is activated later
   (invalidation runs regardless of the behaviour).
3. A Ctrl release after activation does not disable memory behaviour; a Ctrl
   press during construction does not enable it (the canvas Ctrl stays the
   inherited multiple-selection modifier).
4. Slots and behaviour are independent session state; neither is serialized
   nor an undo point.
5. Kernel algorithms, commands, XML, identity, Graphics View 2, Classic, Angle
   with Given Size and the working-layer exclusion are unchanged.

## 2. Candidate A — Ctrl (Cmd on macOS) at toolbar activation

**Mechanism (implementation design, not code):**

- In `GeoCeDGToolbarContainer.ProfileToolbar.addNativeGroup` (GeoCeDG-owned),
  add one `ActionListener` to each native group's toggle button (public
  `getJToggleButton()` and `addActionListener`). Swing notifies it before the
  button's own `actionPerformed`, i.e. before `app.setMode` (report `R3`).
- On an action whose command is `MODE_PARALLEL` or `MODE_ORTHOGONAL`, classify
  from the event's own modifiers — `CTRL_MASK` (Windows/Linux) or `META_MASK`
  (macOS), mirroring `AppD.isControlDown(meta, ctrl)` — as `MEMORY_ENABLED`, else
  `NORMAL`. Never read the global `AppD.getControlDown()` (stale, `R3`).
- Store the behaviour per tool next to the slots (`GeoCeDGLineReferenceMemory`).
  `processLineReferenceTool` skips injection and the certified write in `NORMAL`.
- Mode changes into either tool reach `GeoCeDGEuclidianController.setMode`; a
  classification issued by the button listener for that mode during the same
  activation is adopted there, otherwise the default applies (`DQ-F2R1-4`).
  Reclicks never reach the controller (`R5`), so the listener applies the
  reclick rule itself (`DQ-F2R1-2`).

**Assessment:**

| # | Criterion | Finding |
|---|---|---|
| 1 | correctness, unambiguous activation | unambiguous for native-toolbar clicks, flyout selections and reclicks (P); not available for menu, registry, keyboard, scripting, user tools, undocked dock-panel toolbars (they default) |
| 2 | inherited normal behaviour | preserved: `NORMAL` is the inherited tool; the toolbar itself is unchanged |
| 3 | existing F2 memory | preserved; slots, lifecycle and certificate unchanged |
| 4 | switching repeatedly | yes, by Ctrl-click / click on the tool or its flyout |
| 5 | feedback | none by default; the active behaviour is invisible unless an indication is added (`DQ-F2R1-5`) |
| 6 | keyboard and accessibility | weak: a modifier-click is mouse-only; keyboard activation is NE and would default to `NORMAL` |
| 7 | GV1, GV2, Classic | GV1 only (memory is GV1); GV2 and Classic unaffected |
| 8 | product code | small: one listener in `ProfileToolbar`, a behaviour field, a guard in the controller |
| 9 | inherited/upstream changes | none |
| 10 | testability | automated with posted `EventQueue` events (probe precedent) for clicks, flyouts and reclicks; keyboard, focus and platform behaviour by author smoke |
| 11 | verification class | `BOUNDED_PHASE` (own phase; adds toolbar test classes) |
| 12 | maintenance | low code, but hidden modifier semantics to document |

## 3. Candidate B — explicit session-only memory toggle

**Mechanism:** a GeoCeDG-owned ON/OFF state, either one shared toggle for both
tools or one per tool, exposed as:

- B1 — a declarative checked product action (Construction menu and/or the
  `construction-derived` presentation group), registry target with `execute` and
  `checked`, on the F1 orientation-cue precedent; or
- B2 — a clickable status-bar segment (`GeoCeDGStatusBar`, like the working-layer
  segment), with no catalog change.

**Assessment:**

| # | Criterion | Finding |
|---|---|---|
| 1 | correctness | unambiguous for every activation route (state is not tied to the activation event) |
| 2 | inherited behaviour | preserved when OFF |
| 3 | existing F2 memory | preserved |
| 4 | switching repeatedly | yes, but a separate action from choosing the tool |
| 5 | feedback | built in (checked menu item or status text) |
| 6 | keyboard and accessibility | B1 good (menu, keyboard navigation); B2 mouse-oriented unless made focusable |
| 7 | GV1, GV2, Classic | GV1 semantics; GV2 and Classic unaffected |
| 8 | product code | moderate (B1: profile entry, texts, registry target; B2: segment and texts) |
| 9 | inherited/upstream changes | none |
| 10 | testability | fully automated (action invoke / segment click) |
| 11 | verification class | `BOUNDED_PHASE` with `workspace-profile.product`; B1 also updates the catalog count 131 → 132 and its pins (report `R9`) |
| 12 | maintenance | catalog and localization upkeep; explicit, discoverable semantics |

Note: a single shared toggle cannot express "Parallel in memory mode,
Perpendicular normal" at the same time; per-tool toggles double the surface.

## 4. Candidate C — two product-facing entries per construction type

**Mechanism:** separate toolbar/menu entries ("Parallel line", "Parallel line
(remembered reference)", and the same for Perpendicular) reusing the unchanged
algorithms.

**Assessment:** the native toolbar and the profile identify tools by mode
number and reject duplicate modes and audited ids (report `R9`); distinct
entries therefore need new mode ids in the upstream `EuclidianConstants` and
tool texts in shared `menu*.properties`, plus four profile actions, icons,
toolbar placement, Input Help and pins. This requires inherited/shared changes
and leaves the approved Desktop-only architecture; it would escalate above
`BOUNDED_PHASE`. Benefits (explicit, discoverable, keyboard-accessible) do not
outweigh the scope. **Not recommended.**

## 5. Recommendation (agent proposal, not an author decision)

**Candidate A**, because it is the interaction the author asked for, it is
implementable entirely in GeoCeDG-owned Desktop code with no inherited change,
and the probe shows the activation's own modifier at a stable point before
`app.setMode`. It should be adopted with these conditions, each an author
question:

- the modifier is **Ctrl on Windows/Linux and Cmd on macOS**, read only from the
  activation event (`DQ-F2R1-1`);
- every native-toolbar activation, including a reclick of the active tool,
  sets the behaviour from its own modifier — "the last activation wins"; a
  reclick does not clear a pending selection (inherited) (`DQ-F2R1-2`);
- the behaviour lasts while the tool stays active and resets to `NORMAL`
  whenever another tool is chosen (Escape included) (`DQ-F2R1-3`);
- every activation that carries no toolbar modifier (menu, registry, keyboard,
  scripting, user tools, undocked toolbars) is `NORMAL` (`DQ-F2R1-4`);
- a small, presentation-only indication of `MEMORY_ENABLED` is shown, preferably
  as status-bar text (no catalog change) (`DQ-F2R1-5`);
- corrective class `BOUNDED_PHASE`, own phase `PRE-G9B-R6-PLUS-F2-R1` (`DQ-F2R1-6`).

If the author values discoverability and keyboard access above directness,
**Candidate B1** is the fallback; both are compatible with the same behaviour
state, so B could later be added to A without changing the memory contract.

## 6. State machine (Candidate A, as recommended)

| Event | Behaviour of the tool | Slots |
|---|---|---|
| toolbar click/flyout on the tool without the modifier | `NORMAL` | unchanged |
| toolbar click/flyout on the tool with the modifier | `MEMORY_ENABLED` | unchanged |
| reclick of the active tool (with/without modifier) | set from that click | unchanged; pending selection kept |
| activation through menu, keyboard, scripting, user tools | `NORMAL` | unchanged |
| Ctrl/Cmd pressed or released later | unchanged | unchanged |
| another tool chosen (including Escape) | reset to `NORMAL` | unchanged (F2 `DQ-F2-1`) |
| construction in `NORMAL` | — | never read, written or cleared |
| certified construction in `MEMORY_ENABLED` | — | written (F2 rules) |
| undo, redo, rebuild, New, Open, removal, replacement | unchanged | F2 lifecycle (cleared / stale) |

## 7. Tests a corrective F2-R1 would need (proposal)

Posted-event toolbar tests (clicks, Ctrl/Cmd clicks, flyout with and without the
modifier, reclicks, Escape and tool changes), the F2 memory tests run in
`MEMORY_ENABLED`, the same scenarios in `NORMAL` proving no read and no write
(with a still-valid slot preserved), menu/registry and scripting activations
defaulting to `NORMAL`, the indication (if any), and unchanged GV2/Classic and
Angle with Given Size. Keyboard, focus, dialogs and platform modifiers: author
smoke.

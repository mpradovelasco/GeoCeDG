# PRE-G9B-R6-plus-F2-R1 — author-decision preparation record

- Activity: `PRE-G9B-R6-plus-F2-R1-ACTIVATION-CHARACTERIZATION` (Stage 1;
  author instruction of 2026-10-11)
- Status: **QUESTIONS PREPARED — ALL PENDING — NO DECISION RECORDED**
- Base: frozen F2 technical candidate `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873`
  (tree `3d412a801024411ebfcbaf8469691ec00f7c4fee`); published `main`
  `6d9bafb04235a71c8dfadef9631cd6057964b1f9`
- Evidence: [characterization report](pre_g9b_r6_plus_f2_r1_activation_characterization_report.md)
  (`R0`–`R12`); alternatives: [alternatives candidate](../architecture/pre_g9b_r6_plus_f2_r1_activation_alternatives_candidate.md)
- Prior decisions: `DQ-F2-1` to `DQ-F2-10` ([F2 author-decision record](pre_g9b_r6_plus_f2_author_decisions_record.md)
  v1.1) remain the approved decisions of the original F2 design; the author's
  request of 2026-10-11 (two operating behaviours per tool selected at
  activation; Ctrl at activation investigated first) is the input of this record.

This record formulates questions and recommendations. It decides nothing.
`selfApproved = false`.

## DQ-F2R1-1 — Mechanism

| Alternative | Consequence |
|---|---|
| **A — Ctrl (Cmd on macOS) at toolbar activation, read from the activation event (recommended)** | no new action, control or text required; works for native toolbar clicks, flyouts and reclicks (report `R4`); GeoCeDG-owned listener in `ProfileToolbar.addNativeGroup`; no inherited change; hidden unless indicated (`DQ-F2R1-5`); mouse-oriented |
| A′ — as A, but reading the global `AppD.getControlDown()` | rejected: the global flag follows the last key event, is not reset on focus loss and can be stale (`R3`) |
| B1 — declarative checked toggle (menu/group) | explicit and accessible; catalog 131 → 132, texts, registry target, pins, `workspace-profile.product` |
| B2 — status-bar toggle segment | explicit; no catalog change; texts; mouse-oriented |
| C — separate tool entries | needs new upstream mode ids and shared texts; escalates; not recommended |

Affected original clauses: `DQ-F2-1`, `DQ-F2-2`, `DQ-F2-3` (reuse and writes
become conditional on `MEMORY_ENABLED`); `DQ-F2-10` (adds a GeoCeDG-owned
toolbar adapter).

## DQ-F2R1-2 — Reactivating an already selected tool

Evidence: a reclick fires the button action and `App.setMode` but never reaches
the controller (`EuclidianView.setMode` same-mode return); a pending selection
survives (`R5`).

| Alternative | Consequence |
|---|---|
| **R1 — every reclick sets the behaviour from its own modifier ("last activation wins") (recommended)** | Ctrl-reclick enables, plain reclick disables; deterministic; the pending selection is kept (inherited) and the new behaviour applies from the next process step |
| R2 — only a Ctrl-reclick changes anything (enables); a plain reclick keeps the current behaviour | memory mode can only be left by choosing another tool |
| R3 — reclicks are ignored | the behaviour is fixed until another tool is chosen |
| R4 — a reclick toggles the behaviour | mode depends on history, not on the gesture; harder to predict |

## DQ-F2R1-3 — Duration and reset

| Alternative | Consequence |
|---|---|
| **D1 — the behaviour lasts while the tool stays active; choosing any other tool (including Escape → Move) resets that tool to `NORMAL` (recommended)** | each entry into the tool is a fresh choice; no hidden carry-over |
| D2 — the behaviour is remembered per tool for the session | returning without Ctrl would still use memory; contradicts "normal activation = inherited behaviour" |
| D3 — one global behaviour shared by both tools | switching Parallel ↔ Perpendicular carries the choice; less explicit |

In all alternatives Ctrl/Cmd presses or releases after activation have no effect
(`R4` A05, A06), slots are never affected by the behaviour, and the F2 lifecycle
clears stale slots regardless of the behaviour.

## DQ-F2R1-4 — Flyout, keyboard and indirect activations

| Route | Evidence | Recommendation |
|---|---|---|
| flyout item | ends in the same button action; modifier from the dispatched event (`R6`) | classify like a direct click |
| menu / action registry | registry discards the event (`R6`, A10) | **`NORMAL`** (alternative: let the GeoCeDG registry classify its own `ActionEvent`) |
| keyboard on the toolbar | NE in the test host; same action path (I) | classify like a click if the event carries the modifier, else `NORMAL`; author smoke |
| scripting `SetMode`/API, pinned user tools, profile flyouts, undocked dock-panel toolbars | no modifier (`R6`) | `NORMAL` |

## DQ-F2R1-5 — Indication of the active behaviour

| Alternative | Consequence |
|---|---|
| I0 — none (as F2 `DQ-F2-6`) | smallest; the user must remember which activation was used |
| **I1 — status-bar text while a tool is in `MEMORY_ENABLED` (recommended)** | presentation only; GeoCeDG-owned `GeoCeDGStatusBar`; localized texts (EN, ES) in the profile; no catalog change |
| I2 — tooltip or button decoration on the toolbar | touches the inherited button painting or needs an adapter; more fragile |
| I3 — cursor change in Graphics View 1 | conflicts with existing cursor semantics |

Affected original clause: `DQ-F2-6` ("no indicator in v1") if I1–I3.

## DQ-F2R1-6 — Scope and verification class of the correction

| Alternative | Consequence |
|---|---|
| **V1 — a corrective successor `F2-R1` on the frozen F2 candidate, `BOUNDED_PHASE`, own registered phase `PRE-G9B-R6-PLUS-F2-R1` (recommended)** | Desktop-only; selection = F2 class (updated for the two behaviours) + toolbar classes (`G9U1WorkspaceSurfaceTest`, `PreG9BR4OrderedListToolTest`) + the existing F2 regression classes; adds `workspace-profile.product` if I1 texts are added; F2 receipts stay historical |
| V2 — fold the change into a re-frozen F2 candidate | would replace the verified candidate; the instruction forbids amending it |
| V3 — Candidate C | escalates above `BOUNDED_PHASE` (shared/inherited changes) |

Unchanged original clauses under every alternative: `DQ-F2-4` (types),
`DQ-F2-5` (identity and lifecycle), `DQ-F2-7` and `DQ-F2-8` (Angle with Given
Size), `DQ-F2-9` (working-layer exclusion).

## Summary

| Id | Recommendation | State |
|---|---|---|
| `DQ-F2R1-1` | A — Ctrl/Cmd read from the toolbar activation event | PENDING |
| `DQ-F2R1-2` | R1 — every reclick sets the behaviour from its own modifier | PENDING |
| `DQ-F2R1-3` | D1 — per activation; reset to `NORMAL` when another tool is chosen | PENDING |
| `DQ-F2R1-4` | flyouts like clicks; every modifier-less route `NORMAL` | PENDING |
| `DQ-F2R1-5` | I1 — status-bar text while memory-enabled | PENDING |
| `DQ-F2R1-6` | V1 — corrective `F2-R1`, `BOUNDED_PHASE`, own phase | PENDING |

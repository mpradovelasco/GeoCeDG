# PRE-G9B-R6-plus-F3 — recovery design addendum candidate

- Status: **SUPERSEDED — NOT AUTHOR APPROVED** — superseded by the recovery design addendum successor (author decision of 2026-10-13, [F3 author design closeout record](../validation/pre_g9b_r6_plus_f3_author_design_closeout_record.md); implementation NOT AUTHORIZED); kept as history
- Produced by: `PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE`
  (author instruction of 2026-10-12; documentary)
- Base: `T_F3_RECOVERY_CORRECTIVE` `6e41bf4426b9bdcb137f42e783038b02827ff3bd`
- Amends, without modifying them: §2–§3 of the
  [restricted insertion design successor candidate](pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
  (recovery), which already defers to the DQ-F3-21 assessments; all other
  design content stands
- Normative source for the procedure: [final option assessment](../validation/pre_g9b_r6_plus_f3_dq21_final_option_assessment.md)
  §2 and §5; evidence: [Undo history semantic integrity report](../validation/pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md)
- `selfApproved = false`

## 1. Ownership additions (U0)

| Concern | Owner |
|---|---|
| GeoCeDG undo-manager subclass (proposed `GeoCeDGUndoManager extends UndoManagerD`): detach/attach of original commands, pending-store tracker | GeoCeDG Desktop |
| installation for every GeoCeDG document | `AppGeoCeDG.getUndoManager(Construction)` override |
| barrier transaction, guarded reload, certificate | orchestration (proposed `GeoCeDGDocumentInsertion`) |
| modal isolation after a serious failure | GeoCeDG Desktop UI (dialog), using `setBlockUpdateScripts`, animation stop, `Kernel.setUndoActive`, native preflight, ordinary open |
| inherited undo classes, `UndoCommand` copy contract | unchanged |

## 2. Contract of the subclass

- `detach()`: under the manager monitor, returns the original command list and
  cursor and clears the manager; never copies.
- `attach(held)`: installs exactly those objects and cursor
  (`undoHistoryFrom`); called in a `finally` block of every operation.
- `pending()`: number of scheduled asynchronous stores not yet completed;
  reset to 0 by `commitUndoBaseline` (earlier stores are cancelled by the
  generation advance).
- No change of any inherited behavior: overrides add bookkeeping and delegate.

## 3. Tests added to the design's test list

`T-F3R-HISTORY-METADATA` (labels, grouping, delete behavior; H2–H5),
`T-F3R-HISTORY-OWNERSHIP` (files, repeated cycles, interruption; O1–O4),
`T-F3R-PENDING-TRACKER` (P1–P4 and an action head, on the EDT),
`T-F3R-ISOLATION` (modal isolation; no edit, save or export of the unverified
state), regression `ER-F3R-3`.

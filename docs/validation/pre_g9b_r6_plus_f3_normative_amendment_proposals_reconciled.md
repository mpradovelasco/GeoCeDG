# PRE-G9B-R6-plus-F3 — normative amendment proposals (reconciled)

- Activity: `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **PROPOSALS — NOT APPROVED — NOT APPLIED**
- Base: `T_F3_UNDO_HISTORY_INTEGRITY` `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`
- Successor of (unchanged, history): the [normative amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals.md)
  `P1`–`P6`
- Approved texts these would amend (unchanged): `F3-D2` in the
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md);
  `DQ-D1-7` in the [D1 prompt closeout record](pre_g9b_r6_plus_d1_prompt_closeout_record.md)
- Evidence: [independent audit](pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md);
  [audit reconciliation report](pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md)
- `selfApproved = false`

## Status of each proposal

| Proposal | Subject | Change in this successor |
|---|---|---|
| `P1` | `F3-D2` items 1–5 | unchanged; item 4's reference to failures now reads "P6 and P7"; item 2 uses the `DQ-F3-23` S1 name |
| `P2` | `DQ-D1-7` replacement (operation-scoped insertion buffer) | unchanged |
| `P3` | M1 macro policy | unchanged |
| `P4` | admissibility | unchanged |
| `P5` | T0 style operation | unchanged |
| `P6` | failure and recovery diagnostics | **replaced** below (isolation exits; no export) |
| `P7` | recovery and isolation contract | **new** below (integrates audit G1, G2, N1) |

## P1 item 4 (replacement text)

> 4. Insert File inserts an admissible source construction into the current
>    document completely or not at all, under the restricted insertion contract
>    (P3, P4, P7): admission and a strict dry run precede any mutation;
>    existing objects are never overwritten, replaced or rebound; conflicting
>    labels of inserted objects are renamed deterministically; required macros
>    follow P3; the `D1` provenance and notice are kept through an
>    operation-scoped buffer (P2) and coordinates are never scaled; failures
>    follow P6 and P7.

## P6 — insertion failure and recovery diagnostics (replacement)

> **Rejected before change** (gate, snapshot, admission, dry run): "*name* was
> not inserted: *reason*." Nothing in the drawing, its undo history or the
> session changed. **Failed after commit, compensated without reload**: "*name*
> was not inserted: *reason*. The drawing is unchanged." **Failed after commit,
> restored by reload**: "*name* was not inserted: *reason*. The drawing was
> restored to its state before the insertion; the selection and the remembered
> references of Parallel/Perpendicular were reset[, and the export area was
> unlinked from its ISO A sheet]." **Serious failure**: "The drawing could not
> be verified after a failed insertion of *name*. You can save the last
> verified version to a new file, reopen the document from its file, or close
> it." No message after a change ever claims partial success.

## P7 — recovery and isolation contract (new)

> An insertion starts only when GeoCeDG can prove that no undo state is still
> being recorded, can capture and validate a complete native copy of the
> drawing as it is, and can reload its current state; otherwise it is rejected
> and nothing changes. While it runs, the drawing's Undo and Redo history is
> kept aside unchanged — every step, its grouping and its references — and is
> put back only when nothing can still change it. The insertion is accepted only
> when admission, the dry run, the commit and the strict verification all
> succeed; it is then exactly one undo step, previous steps keep their
> behavior, the drawing keeps its document identity, the selection is cleared
> and the Move tool is active. A failure after the commit restores the drawing
> exactly — construction, durable identities, document identity, units,
> layers, macros, saved state and the complete Undo and Redo history with its
> grouping; the selection is cleared and the Move tool is active. When no
> identity-bearing object was created, live objects, the remembered references
> of Parallel/Perpendicular and the IsoABorder export link are kept; otherwise
> the drawing is reloaded and those are reset as after an Undo, and the user is
> told so. If the restoration fails or cannot be verified, GeoCeDG isolates the
> drawing: it cannot be edited, saved, exported or changed by scripts,
> animation, Undo or Redo; the only actions are to save the complete native
> copy taken before the insertion to a new file — with the same document
> identity and never over the existing file — to reopen the document from its
> file, or to close it, none of which asks to save the unverified drawing.
> The unverified drawing is never written or exported.

## Application

None of these texts is normative until the author approves it explicitly. On
approval, `F3-D2` items 1–5 are amended by `P1`, `DQ-D1-7` by `P2`, and `P3`–`P7`
become the F3 contracts recorded in the F3 author-closeout record.

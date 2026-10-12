# PRE-G9B-R6-plus-F3 — final author-decision preparation record

- Record version: **4.0** (successor of v3.0, v2.0 and v1; all unchanged and kept
  as history)
- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **PROPOSED — PENDING FINAL AUTHOR DISPOSITION — NOT AN AUTHOR
  DECISION RECORD**
- Base: `T_F3_RISK` `8b41af11316f90d7a068ce6af4859213240ce6a6`, tree
  `8be60e86e09f957f1735990b966190bd44ffab2a`
- Reconciliation: [decision reconciliation report](pre_g9b_r6_plus_f3_author_decision_reconciliation_report.md)
  (matrix §3); [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md);
  [normative amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals.md)
  (`P1`–`P6`); [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_reconciliation.md)
- Design: [restricted insertion design successor](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
- Approved texts (unchanged): `F3-D1`–`F3-D6`; `DQ-D1-7`
- `selfApproved = false`

## 1. Author preferences recorded (not dispositions)

R; S as fallback; M1; L1; independent insertion buffer; D1 preserved and never
scaled; specialized objects admitted individually; T0; U0 preferred; no
general spatial import.

## 2. Established technical facts (not questions)

These are settled by executed evidence and are not put to the author:

1. Pre-commit rejection (admission, strict dry run) changes neither the
   document nor the session.
2. Without a strict pairwise algorithm check, objects can degrade silently.
3. M1 needs the transitive macro closure; the curated library digest identifies
   identical tools; reuse must suppress the transfer.
4. L1 renaming is deterministic and keeps closures internal.
5. The Locus V2 family, Spline V2, Linear and Aligned dimensions and IsoABorder
   insert, recompute, reopen and undo correctly under their conditions.
6. The independent buffer leaves every window buffer and provenance intact.
7. T0 changes only six properties when specialized outputs are excluded.
8. The identity-bearing paste stores its own undo state asynchronously; an
   immediate restore lets a failed insertion reappear; await + undo + prune
   does not; subtractive removal keeps live instances only when the paste stored
   no state.
9. The inherited paste clears the selection and activates Move in every path.

## 3. Pending decisions (all)

`DQ-F3-1` to `DQ-F3-24`, with the recommendations of the reconciliation report
§3. None is approved.

## 4. Recommendation summary

| Id | Recommendation |
|---|---|
| `DQ-F3-21` | K1 refined (K1-S + K1-R + serious failure); S only per excluded payload class |
| `DQ-F3-22` | MU-1 — transferred macros stay after Undo of a successful insertion |
| `DQ-F3-23` | "Apply Styles…" / "Aplicar estilos…" |
| `DQ-F3-24` | reject identity + macros before mutation, with diagnostic |
| `DQ-F3-1`–`-20` | as reconciliation report §3 |

## 5. Blocking conditions for implementation authorization

Implementation cannot be authorized until the author has disposed of the
questions marked **blocking** in §6 and has approved or rejected `P1`
(`F3-D2`) and `P2` (`DQ-D1-7`). The class is frozen only at that
authorization, after the governance review of `verification-levels.md` §12.8.

## 6. Author Decision Sheet

Seven questions; technical facts of §2 are not asked.

### Q1 — Commit-time failure recovery (`DQ-F3-21`) — **blocking**

- **Recommended:** K1 refined — no-reload compensation (K1-S) for payloads
  without identity records; reload from the last undo state after taking back
  the paste's own undo state (K1-R) for identity-bearing payloads and for
  failures inside the inherited paste, with the declared reset of live
  instances, Parallel/Perpendicular memory, IsoABorder export link and
  selection; a distinct serious error if recovery fails.
- **Alternatives:** K2 (non-rebuilding shared rollback and deferred paste undo:
  shared changes, reclassification likely `GLOBAL_IMPACT`); K1-S only, with
  identity-bearing payloads rejected before mutation (S) — loses Locus V2 and
  Spline V2 insertion.
- **Consequences:** K1 is U0 and `INTEGRATED_PHASE`; K2 delays F3 and changes
  Classic and every paste.
- **Residual risk:** session reset in K1-R cases; uncertified state after a
  failed recovery (reported).

### Q2 — Insert File contract and admissibility (`DQ-F3-13`, `-14`, `-5`, `-6`, `-18`) — **blocking**

- **Recommended:** approve R as in `P1` items 3–4 and `P4`, with L1 renaming,
  no overwrite, admission + strict dry run + strict verification, one undo
  step.
- **Alternatives:** S (elementary 2D types only, L0); narrower R excluding a
  named type.
- **Consequences:** amends `F3-D2` items 3–4.
- **Residual risk:** types outside the matrix are rejected even if they would
  insert correctly.

### Q3 — Macros (`DQ-F3-15`, `-22`, `-24`) — **blocking**

- **Recommended:** M1 as in `P3`; transferred tools remain after Undo of a
  successful insertion (MU-1); identity + macros rejected with diagnostic.
- **Alternatives:** remove transferred macros on Undo and re-add on Redo
  (MU-2, not established, risks Redo); M0.
- **Consequences:** after Insert + Undo + Save, the document contains the
  transferred tool.
- **Residual risk:** semantically identical user macros from independent
  documents are rejected.

### Q4 — `D1` amendment (`DQ-F3-16`, `-7`) — **blocking**

- **Recommended:** approve `P2` as the amended `DQ-D1-7`.
- **Alternatives:** keep `DQ-D1-7` (Insert File replaces the window buffer),
  which requires an inherited `CopyPasteD` correction to avoid placeholder
  labels on later Ctrl+V.
- **Consequences:** a normative change of an approved D1 decision; guide
  sentence updated.
- **Residual risk:** none established.

### Q5 — Style operation (`DQ-F3-17`, `-23`, `-8`, `-9`) — **blocking**

- **Recommended:** approve `P5` (T0, six properties, exclusions) and the name
  "Apply Styles…" / "Aplicar estilos…".
- **Alternatives:** keep the name "Apply Template…"; include a specialized type
  only with a separate contract.
- **Consequences:** amends `F3-D2` items 2 and 5; construction defaults and
  labeling style deferred.
- **Residual risk:** narrower than the upstream operation.

### Q6 — Debt and deferrals (`DQ-F3-20`) — blocking for closeout, not for authorization

- **Recommended:** `DEBT-R6PLUS-FILE-INSERT-SURFACE` closes with F3 when the
  amended `F3-D2` is approved with the candidate; deferred items are recorded
  as named debts.
- **Alternatives:** keep the debt open until deferred items are delivered.
- **Consequences:** `G` can proceed after F3 closeout.

### Q7 — Surface, compatibility, architecture (`DQ-F3-1`–`-4`, `-10`–`-12`, `-19`) — **blocking**

- **Recommended:** accept the recommendations as a group (N1 with the name of
  Q5; A1; X1; OFF-B; H-1/H-3; F-1; U0 only; `INTEGRATED_PHASE` to be frozen at
  authorization).
- **Alternatives:** dispose of any of them individually.
- **Consequences:** none beyond the listed.
- **Residual risk:** the duplicated Open-dialog setup must follow upstream.

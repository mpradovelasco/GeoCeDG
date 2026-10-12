# PRE-G9B-R6-plus-F3 — Final F3 Author Decision Sheet

- Record version: **5.0** — consolidates the
  [final author decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md)
  v4.0 (Q2–Q7), the [corrective update](pre_g9b_r6_plus_f3_author_decision_sheet_corrective_update.md)
  v4.2 (Q1) and the [Undo history semantic integrity report](pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md);
  earlier versions unchanged, kept as history
- Activity: `PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE` (author
  instruction of 2026-10-12)
- Status: **PROPOSED — PENDING FINAL AUTHOR DISPOSITION — NOT AN AUTHOR
  DECISION RECORD**
- Final technical gate: **READY FOR AUTHOR DISPOSITION** (this is not
  implementation readiness, product verification or approval)
- Base: `T_F3_RECOVERY_CORRECTIVE` `6e41bf4426b9bdcb137f42e783038b02827ff3bd`
- Approved and unchanged until the author disposes: `F3-D1`–`F3-D6`
  ([F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md));
  the `D1` contracts including `DQ-D1-7`
- Proposed texts: [normative amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals.md)
  `P1`–`P6`; recovery: [final option assessment](pre_g9b_r6_plus_f3_dq21_final_option_assessment.md);
  design: [design successor](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
  with the [recovery design addendum](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_candidate.md)
- `DQ-F3-1..24 — PENDING FINAL AUTHOR DISPOSITION`
- `selfApproved = false`

## Coverage

| Question | DQ identifiers | Proposed texts |
|---|---|---|
| Q1 | `DQ-F3-21` | final option assessment §2, §4, §5; `P6` |
| Q2 | `DQ-F3-5`, `-6`, `-13`, `-14`, `-18` | `P1` items 3–4, `P4` |
| Q3 | `DQ-F3-15`, `-22`, `-24` | `P3` |
| Q4 | `DQ-F3-7`, `-16` | `P2` |
| Q5 | `DQ-F3-8`, `-9`, `-17`, `-23` | `P5`, `P1` items 2 and 5 |
| Q6 | `DQ-F3-20` | — |
| Q7 | `DQ-F3-1`, `-2`, `-3`, `-4`, `-10`, `-11`, `-12`, `-19` | `P1` items 1–2 |

All twenty-four `DQ-F3-*` are covered exactly once. No question requires a
shared (upstream) correction; every recommendation is U0.

## Q1 — Commit-time recovery contract (`DQ-F3-21`)

- **Recommended:** K1 in the history-preserving barrier form: K1-S for payloads
  without identity records, K1-R-H guarded reload for identity-bearing payloads
  and inherited-rollback failures; GeoCeDG undo-manager subclass that moves the
  original history out and back and tracks pending stores; destination UUID
  kept on success; modal isolation after a serious failure; S rejection when no
  guarantee can be established.
- **Alternatives:** S for identity-bearing payloads (loses Locus V2 and Spline V2
  insertion); containment allowing continued editing with a warning; UUID
  regeneration on success; K2 / shared copy-constructor fix (authorization and
  reclassification review, likely `GLOBAL_IMPACT`).
- **Evidence:** `DQ21-Q1`–`Q5`; `ER-F3R-1`, `ER-F3R-2`, `ER-F3R-3`; recovery
  safety, corrective feasibility and history integrity reports.
- **Restrictions:** pending store or unloadable baseline → rejection before
  mutation; declared session reset for identity payloads; selection cleared and
  Move active (inherited).
- **Implementation obligations:** final option assessment gates 1–8 (EDT runs,
  history metadata and ownership tests, tracker, barrier, guarded reload,
  success path, modal isolation, regressions).
- **Residual risks:** isolation not yet built; EDT behavior to be verified;
  persistent reload failure ends in isolation, not in recovery.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `DQ-F3-21` is resolved as K1 in the history-preserving barrier form of the
  > final option assessment §2, with its normative wording §4 and implementation
  > gates §5; the destination document UUID is preserved on successful
  > insertion; after a serious recovery failure the drawing is isolated as
  > described there.

## Q2 — Restricted Native Insertion R, L1 and admissibility (`DQ-F3-5`, `-6`, `-13`, `-14`, `-18`)

- **Recommended:** R as `P1` items 3–4 and `P4`; L1 deterministic renaming, no
  overwrite, no label dialog; admission + strict dry run + commit + strict
  verification; one undo step.
- **Alternatives:** S (elementary 2D types only, L0); narrower R excluding a
  named type.
- **Evidence:** focused feasibility G2–G5; risk candidate matrix; reconciliation
  report §3.
- **Restrictions:** no scripts; 3D, projection bindings and unlisted types
  rejected; Locus V2 / Spline V2 only when the target allows Locus V2 and the
  source needs no macros.
- **Implementation obligations:** matrix tests per row; dry-run cost on a large
  document; strict pairwise algorithm verification.
- **Residual risks:** admissible-looking types outside the matrix are rejected.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `F3-D2` items 3–4 are amended as `P1` items 3–4; the admissibility matrix is
  > `P4`; `DQ-F3-5`, `-6`, `-13`, `-14` and `-18` are resolved as recommended in
  > the Final F3 Author Decision Sheet Q2.

## Q3 — M1 macros, Undo lifecycle, identity + macros (`DQ-F3-15`, `-22`, `-24`)

- **Recommended:** M1 as `P3` (transitive closure, digest reuse, rejection of
  different definitions); transferred tools stay after Undo of a successful
  insertion (MU-1); payloads needing identity records and macros rejected
  before mutation with the diagnostic of the reconciliation report §5.
- **Alternatives:** MU-2 (remove on Undo, not established); M0.
- **Evidence:** G3, V4, V5, V7; macro K1-S recovery (corrective and safety
  reports).
- **Restrictions:** user macros from independent documents compare unequal
  (fail closed); failed insertions remove transferred macros.
- **Implementation obligations:** closure and digest tests; macro cleanup on
  every failure path.
- **Residual risks:** after Insert + Undo + Save the document keeps an unused
  transferred tool.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `DQ-F3-15` is resolved as M1 per `P3`; `DQ-F3-22` as MU-1; `DQ-F3-24` as
  > rejection before mutation with diagnostic.

## Q4 — Independent insertion buffer and `DQ-D1-7` (`DQ-F3-7`, `-16`)

- **Recommended:** approve `P2` as the amended `DQ-D1-7`; provenance through the
  operation-scoped buffer; window buffer untouched; no scaling.
- **Alternatives:** keep `DQ-D1-7` (Insert File replaces the window buffer) plus
  an inherited `CopyPasteD` correction.
- **Evidence:** G6; window buffer unchanged in every recovery path.
- **Restrictions:** —
- **Implementation obligations:** provenance and Ctrl+V tests.
- **Residual risks:** none established.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `DQ-D1-7` is amended as `P2`; `DQ-F3-7` and `DQ-F3-16` are resolved
  > accordingly.

## Q5 — T0 Apply Styles (`DQ-F3-8`, `-9`, `-17`, `-23`)

- **Recommended:** `P5` (six properties; native-dimension, IsoABorder and Locus
  V2 outputs excluded); name "Apply Styles…" / "Aplicar estilos…"; layers never
  touched.
- **Alternatives:** keep "Apply Template…"; include a specialized type under a
  separate contract.
- **Evidence:** G7.
- **Restrictions:** construction defaults and labeling style deferred.
- **Implementation obligations:** whitelist and exclusion tests; one undo step.
- **Residual risks:** narrower than the upstream operation.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `F3-D2` items 2 and 5 are amended as `P1` items 2 and 5 and `P5`;
  > `DQ-F3-8`, `-9`, `-17` and `-23` are resolved as recommended.

## Q6 — Debt closeout (`DQ-F3-20`)

- **Recommended:** C20-1 — `DEBT-R6PLUS-FILE-INSERT-SURFACE` closes with the F3
  closeout when the amended `F3-D2` is delivered; deferred items (template
  defaults and labeling, M2, scripts, 3D, unlisted types, identity + macros)
  recorded as named debts.
- **Alternatives:** keep the debt open until deferred items are delivered.
- **Restrictions / obligations:** the closeout record names every deferred item.
- **Residual risks:** none.
- **Shared correction required:** no.
- **Blocking:** for closeout only, not for implementation authorization.
- **Proposed approval wording:**
  > `DQ-F3-20` is resolved as C20-1.

## Q7 — Compatibility, architecture, class (`DQ-F3-1`–`-4`, `-10`–`-12`, `-19`)

- **Recommended:** N1 surface (`document.insert-file`, `document.apply-styles`);
  A1 chooser; X1 containers; OFF-B; H-1 / H-3 helper rules; F-1 (Classic
  unchanged, v1 fallback inherited); U0 only (no optional Classic fixes in F3);
  `INTEGRATED_PHASE`, frozen at authorization, with integration coverage of the
  existing Desktop undo, document-lifecycle and identity suites because the
  GeoCeDG undo-manager subclass serves every GeoCeDG document.
- **Alternatives:** dispose of any item individually; reclassification if a
  shared change is chosen in Q1.
- **Evidence:** K3, K4, K8, K10, G8; history integrity report §5.
- **Restrictions:** the duplicated Open-dialog setup must follow upstream.
- **Implementation obligations:** pins, localization, Classic regression,
  fallback tests.
- **Residual risks:** none beyond the listed.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `DQ-F3-1`–`-4`, `-10`–`-12` and `-19` are resolved as recommended in the
  > Final F3 Author Decision Sheet Q7; the planned class is `INTEGRATED_PHASE`
  > with the stated integration coverage, to be frozen at implementation
  > authorization.

## Separate gates (not decided by this sheet)

Design approval · implementation authorization (exact base, prompt amendment) ·
implementation · technical verification (frozen class) · author smoke · phase
closeout · publication — each needs its own explicit author act.

# PRE-G9B-R6-plus-F3 — risk register at the author design closeout

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-DESIGN-CLOSEOUT` (author decision of
  2026-10-13)
- Status: **DESIGN-CLOSEOUT REGISTER** — records author dispositions; no
  product verification exists
- Base: approved design candidate `9d130b43fbeb731a25d5dcf6413ed0dd0439f61e`
- Authority: [F3 author design closeout record](pre_g9b_r6_plus_f3_author_design_closeout_record.md);
  previous register: [audit risk register](pre_g9b_r6_plus_f3_risk_register_audit_reconciliation.md)
  (unchanged, history)
- `selfApproved = false`

Kinds: **resolved design uncertainty** (the author chose); **accepted author
restriction**; **implementation gate** (must be built and shown; maps to a
binding stop condition); **open product-verification obligation**;
**deferred**; **future, unauthorized**. Nothing here is `CLOSED — PRODUCT
VERIFIED`; the audit's `GO WITH CONDITIONS` is not an F3 PASS.

| Risk | Status after the design closeout | Kind | Stop condition |
|---|---|---|---|
| `F3R-ATOMICITY-COMMIT` | **CLOSED — DESIGN DISPOSED** (K1 corrected approved, `DQ-F3-21`) | resolved design uncertainty | — |
| `F3R-ATOMICITY` (pre-commit) | CLOSED WITH EXPLICIT SCOPE RESTRICTION | accepted author restriction (admission + strict dry run) | FR-F3-7 |
| `F3R-UNDO-PENDING-ACCOUNTING` | **OPEN — IMPLEMENTATION GATE** (C1 approved; invariant to be proven) | implementation gate | FR-F3-1 |
| `F3R-UNDO-EQUAL-CONTENT-PENDING` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (barrier; gate depends on C1) | accepted author restriction | FR-F3-1 |
| `F3R-UNDO-HISTORY-COPY-SEMANTICS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (original-command transfer approved) | implementation gate | FR-F3-2 |
| `F3R-UNDO-SNAPSHOT-OWNERSHIP` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | implementation gate | FR-F3-2 |
| `F3R-UNDO-ATTACH-RACE` | **OPEN — IMPLEMENTATION GATE** | implementation gate | FR-F3-1, FR-F3-2 |
| `F3R-RECOVERY-SOURCE-CAPTURE` | **OPEN — IMPLEMENTATION GATE** | implementation gate | FR-F3-2 |
| `F3R-NATIVE-RECOVERY-SNAPSHOT` | **OPEN — IMPLEMENTATION GATE** (contract approved) | implementation gate | FR-F3-3 |
| `F3R-RECOVERY-FAILURE` (containment) | **OPEN — NOT ESTABLISHED** (contract approved: three exits, no uncertified export, no save prompt) | open product-verification obligation | FR-F3-4 |
| `F3R-EDT` | **OPEN — NOT ESTABLISHED** | open product-verification obligation | FR-F3-5 |
| `F3R-FACTORY-LIFECYCLE` | **OPEN — IMPLEMENTATION GATE** | implementation gate | FR-F3-8 |
| `F3R-EXCEPTION-PRESERVATION` | **OPEN — IMPLEMENTATION GATE** | implementation gate | FR-F3-2 |
| `F3R-DOCUMENT-IDENTITY` / `-SUCCESS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (preservation approved for success and recovery copy) | resolved design uncertainty; implementation gate | FR-F3-6 |
| `F3R-SAVED-STATE` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | implementation gate | FR-F3-2 |
| `F3R-REDO-PRESERVATION` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | implementation gate | FR-F3-2 |
| `F3R-MACRO-M1` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (M1/MU-1 approved; MU-1 accepted functional limitation) | accepted author restriction | FR-F3-7 |
| `F3R-LABEL-L1` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | accepted author restriction | FR-F3-7 |
| `F3R-SPECIALIZED-*` (Locus V2, Spline V2, dimensions, IsoABorder) | CLOSED WITH EXPLICIT SCOPE RESTRICTION (`P4` conditions) | accepted author restriction; open product-verification obligation | FR-F3-7 |
| `F3R-CLIPBOARD-D1` | CLOSED — EVIDENCE SUPPORTED (characterization); `P2` approved | open product-verification obligation | FR-F3-7 |
| `F3R-TEMPLATE-T0` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (`P5` approved) | accepted author restriction | — |
| `F3R-ARCHITECTURE` | CLOSED — EVIDENCE SUPPORTED (U0 approved) | resolved design uncertainty | FR-F3-8 |
| `F3R-INHERITED-SESSION-EFFECTS`, `F3R-INHERITED-STORE-RACE` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (inherited, documented) | accepted author restriction | — |
| identity + macro import, forensic export, scripts, 3D, unlisted types, M2, template defaults | deferred (closeout record §8) | future, unauthorized | — |

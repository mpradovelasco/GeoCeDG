# PRE-G9B-R6-plus-F3 — Final F3 Author Decision Sheet (v6.0)

- Record version: **6.0** — successor of the [Final F3 Author Decision Sheet](pre_g9b_r6_plus_f3_final_author_decision_sheet.md)
  v5.0 (unchanged, history), reconciled with the
  [independent final design safety audit](pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md)
  (`GO WITH CONDITIONS`)
- Activity: `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **PROPOSED — PENDING FINAL AUTHOR DISPOSITION — NOT AN AUTHOR
  DECISION RECORD**
- Base: `T_F3_UNDO_HISTORY_INTEGRITY` `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`
- Unchanged until the author disposes: `F3-D1`–`F3-D6`
  ([F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md)),
  the `D1` contracts including `DQ-D1-7`
- Texts: [reconciled amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals_reconciled.md)
  `P1`–`P7`; design: [design successor](../architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
  with the [recovery design addendum successor](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_successor_candidate.md);
  reconciliation: [audit reconciliation report](pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md);
  risks: [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_audit_reconciliation.md)
- `DQ-F3-1..24 — PENDING FINAL AUTHOR DISPOSITION`
- `selfApproved = false`

## Author directions recorded (not dispositions)

2026-10-12, earlier: R; S as fallback; M1; L1; independent insertion buffer;
`D1` preserved and never scaled; specialized objects admitted individually; T0;
U0 preferred; no general spatial import.

2026-10-12, for this reconciliation (accepted as preferred route, **not yet
ratified**):

1. **Pending-store accounting:** conservative, fail-closed accounting C1 — no
   false negatives; every exit path handled; generation-aware; explicit
   serious-failure behavior; tests for injected failures and delayed
   notifications; if C1 proves unworkable during implementation, STOP and
   request an author disposition before changing architecture.
2. **Recovery document UUID:** the verified recovery copy keeps the original
   document UUID (same lineage), is saved to a separate path and never
   overwrites the existing file; a successful insertion also keeps the
   destination UUID.
3. **Export of uncertified geometry:** excluded from F3; a document that cannot
   be certified is not exported as an ordinary valid CeDG document; any future
   forensic export needs its own design and authorization.

## Coverage

| Q | DQ identifiers | Texts |
|---|---|---|
| Q1 | `DQ-F3-21` | `P6`, `P7`; addendum successor |
| Q2 | `DQ-F3-5`, `-6`, `-13`, `-14`, `-18` | `P1` items 3–4, `P4` |
| Q3 | `DQ-F3-15`, `-22`, `-24` | `P3` |
| Q4 | `DQ-F3-7`, `-16` | `P2` |
| Q5 | `DQ-F3-8`, `-9`, `-17`, `-23` | `P5`, `P1` items 2 and 5 |
| Q6 | `DQ-F3-20` | — |
| Q7 | `DQ-F3-1`, `-2`, `-3`, `-4`, `-10`, `-11`, `-12`, `-19` | `P1` items 1–2 |

All twenty-four `DQ-F3-*` exactly once. No question requires a shared
correction.

## Q1 — Commit-time recovery contract (`DQ-F3-21`)

- **Previous direction:** fail closed; R with S fallback; U0; specialized objects.
- **Recommended:** K1 in the history-preserving barrier form with **both audit
  corrections**: a distinct verified **native recovery snapshot** (complete
  native archive, preflighted, provenance recorded, owned until isolation ends,
  never the undo archive, never deleted by unconditional cleanup) and
  **conservative fail-closed pending-store accounting C1**; K1-S and K1-R-H;
  attach only when quiescent (closing barrier otherwise); recovery source
  captured by reference; destination UUID kept on success; isolation with three
  exits (save verified copy to a new file with the original UUID, reopen,
  close), no save prompt, no export of the uncertified state; S rejection
  whenever a guarantee cannot be established.
- **Alternatives:** accounting C2 (GeoCeDG-owned store body) or C3 (shared
  correction, authorization + reclassification); a new UUID for the recovery
  copy; a diagnostic export of the uncertified state; S for identity payloads.
- **Evidence:** recovery safety, corrective feasibility and history integrity
  reports; `ER-F3R-1`–`ER-F3R-3`; independent audit §3–§7.
- **Audit impact:** G1 and G2 are approval conditions and are incorporated;
  G3–G7 become implementation gates; N1 wording restored; N4 digest certificate.
- **Restrictions:** selection cleared and Move tool active on success and on
  certified failure; declared session reset for identity payloads; insertion may
  be refused temporarily while accounting is degraded or pending.
- **Mandatory implementation gates:** addendum successor §2–§6 tests; stop
  conditions FR-F3-1 to FR-F3-6 of the prepared prompt.
- **Residual risks:** effective containment and EDT behavior not yet
  established (NE1, NE2); temporary refusal after a store failure until the next
  open or New.
- **Shared correction required:** no.
- **Proposed approval wording:**
  > `DQ-F3-21` is resolved as K1 in the history-preserving barrier form of the
  > recovery design addendum successor, with the normative text `P6` and `P7`:
  > a verified complete native recovery snapshot owned until isolation ends;
  > conservative fail-closed pending-store accounting (C1); the destination
  > document UUID kept on successful insertion; a recovery copy that keeps the
  > original UUID and is written only to a new file; isolation with three exits
  > and no export of the uncertified state; implementation stops for an author
  > decision if any of FR-F3-1 to FR-F3-6 fails.

## Q2 — Restricted Native Insertion R, L1, admissibility (`DQ-F3-5`, `-6`, `-13`, `-14`, `-18`)

- **Previous direction:** R; L1 without overwrite; specialized objects individually.
- **Recommended:** R per `P1` items 3–4 (item 4 as reconciled) and `P4`; L1;
  admission + strict dry run + commit + strict verification; one undo step.
- **Alternative:** S (elementary 2D types, L0); narrower R.
- **Evidence:** focused feasibility G2–G5; risk candidate matrix.
- **Audit impact:** none (audit §7: no type admitted without its closure).
- **Restrictions:** no scripts; 3D, projection bindings and unlisted types
  rejected; Locus V2 / Spline V2 only with Locus V2 enabled and no macros.
- **Gates:** per-row matrix tests; dry-run cost; strict pairwise verification;
  FR-F3-7.
- **Residual risks:** admissible-looking unlisted types rejected.
- **Shared correction:** no.
- **Proposed approval wording:**
  > `F3-D2` items 3–4 are amended as `P1` items 3–4 (reconciled); the
  > admissibility matrix is `P4`; `DQ-F3-5`, `-6`, `-13`, `-14` and `-18` are
  > resolved as recommended in sheet v6.0 Q2.

## Q3 — M1 macros (`DQ-F3-15`, `-22`, `-24`)

- **Previous direction:** M1 objective, M0 only for unsupported cases.
- **Recommended:** M1 per `P3`; MU-1; identity + macros rejected with diagnostic.
- **Alternative:** MU-2 (not established); M0.
- **Evidence:** G3, V4, V5, V7.
- **Audit impact:** N4 — the recovery certificate compares macro definition
  digests.
- **Restrictions:** user macros from independent documents compare unequal;
  failed insertions remove transferred macros.
- **Gates:** closure and digest tests; cleanup on every failure path.
- **Residual risks:** an unused transferred tool can remain after Insert + Undo + Save.
- **Shared correction:** no.
- **Proposed approval wording:**
  > `DQ-F3-15` is resolved as M1 per `P3`; `DQ-F3-22` as MU-1; `DQ-F3-24` as
  > rejection before mutation with diagnostic.

## Q4 — Independent insertion buffer and `DQ-D1-7` (`DQ-F3-7`, `-16`)

- **Previous direction:** independent buffer; `D1` preserved; no scaling.
- **Recommended:** approve `P2` as the amended `DQ-D1-7`.
- **Alternative:** keep `DQ-D1-7` plus an inherited `CopyPasteD` correction.
- **Evidence:** G6.
- **Audit impact:** none.
- **Gates:** provenance and Ctrl+V tests; ordinary clipboard suites.
- **Residual risks:** none established.
- **Shared correction:** no.
- **Proposed approval wording:**
  > `DQ-D1-7` is amended as `P2`; `DQ-F3-7` and `DQ-F3-16` are resolved
  > accordingly.

## Q5 — T0 Apply Styles (`DQ-F3-8`, `-9`, `-17`, `-23`)

- **Previous direction:** T0.
- **Recommended:** `P5`; "Apply Styles…" / "Aplicar estilos…"; layers untouched.
- **Alternative:** "Apply Template…"; specialized types under a separate contract.
- **Evidence:** G7 (focused feasibility).
- **Audit impact:** none.
- **Gates:** whitelist and exclusion tests; one undo step.
- **Residual risks:** narrower than upstream.
- **Shared correction:** no.
- **Proposed approval wording:**
  > `F3-D2` items 2 and 5 are amended as `P1` items 2 and 5 and `P5`;
  > `DQ-F3-8`, `-9`, `-17` and `-23` are resolved as recommended.

## Q6 — Debt closeout (`DQ-F3-20`)

- **Recommended:** C20-1; deferred items named (template defaults and labeling,
  M2, scripts, 3D, unlisted types, identity + macros, forensic export of
  uncertified states).
- **Alternative:** keep the debt open until deferred items are delivered.
- **Audit impact:** the excluded uncertified export joins the deferred list.
- **Blocking:** closeout only.
- **Proposed approval wording:**
  > `DQ-F3-20` is resolved as C20-1 with the deferred items listed in sheet
  > v6.0 Q6.

## Q7 — Compatibility, architecture, class (`DQ-F3-1`–`-4`, `-10`–`-12`, `-19`)

- **Previous direction:** small declarative surface; U0; Classic unchanged.
- **Recommended:** N1, A1, X1, OFF-B, H-1/H-3, F-1, U0 only; GeoCeDG undo-manager
  subclass through a stateless factory override; `INTEGRATED_PHASE` (PHASE +
  INTEGRATION) frozen at authorization, with the `AppGeoCeDG`-based suites of
  the reconciliation report §9 named in the frozen plan and the new F3 focused
  tests.
- **Alternative:** individual disposition; reclassification only if a shared
  correction is chosen.
- **Audit impact:** G6, N5 — class confirmed; coverage must use
  `AppGeoCeDG`-based suites.
- **Gates:** FR-F3-8 (no unauthorized shared or Classic change).
- **Residual risks:** none beyond the listed.
- **Shared correction:** no.
- **Proposed approval wording:**
  > `DQ-F3-1`–`-4`, `-10`–`-12` and `-19` are resolved as recommended in sheet
  > v6.0 Q7; the planned class is `INTEGRATED_PHASE` with the stated
  > integration coverage, to be frozen at implementation authorization.

## Separate gates

1. Author approval of the F3 design (this sheet; a documentary author-closeout act).
2. Authorization to implement F3 (exact base; prompt amendment to the authorized state).
3. Technical candidate verification (frozen class).
4. Author smoke.
5. Final closeout.
6. Publication.

# PRE-G9B-R6-plus-F3 — risk register (independent audit reconciliation)

- Activity: `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `T_F3_UNDO_HISTORY_INTEGRITY` `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`
- Updates, without amending them, the [history integrity risk update](pre_g9b_r6_plus_f3_risk_register_history_integrity_update.md)
  and earlier registers; unlisted entries stand
- Evidence: [independent audit](pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md);
  [audit reconciliation report](pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md)
- `selfApproved = false`

An implementation gate is an obligation, not an implemented fix.

| Risk | Previous status | Status now | Audit | Gate |
|---|---|---|---|---|
| `F3R-UNDO-HISTORY-COPY-SEMANTICS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** (independently confirmed) | B1 | factory-installed subclass tests |
| `F3R-UNDO-SNAPSHOT-OWNERSHIP` | CLOSED — EVIDENCE SUPPORTED | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — undo-history snapshots: unchanged; the earlier claim was premature for the barrier baseline's isolation role, now separated (G1) | G1 | `T-F3R-HISTORY-OWNERSHIP` |
| `F3R-UNDO-PENDING-ACCOUNTING` (new; supersedes the tracker part of `F3R-UNDO-EQUAL-CONTENT-PENDING`) | — | **OPEN — IMPLEMENTATION GATE** — conservative fail-closed contract C1 specified with proof obligations; the earlier tracker under-counted after a stale-store exception; "P1–P4 validated" corrected (N2) | G2, N2 | FR-F3-1, `T-F3R-ACCOUNTING` |
| `F3R-UNDO-EQUAL-CONTENT-PENDING` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** for the barrier (generation cancellation, source guarantee); its gate depends on `F3R-UNDO-PENDING-ACCOUNTING` | C2 | — |
| `F3R-UNDO-ATTACH-RACE` (new) | — | **OPEN — IMPLEMENTATION GATE** — attach only when quiescent with an unchanged temporary list, else a closing barrier | G3 | `T-F3R-ATTACH` |
| `F3R-RECOVERY-SOURCE-CAPTURE` (new) | — | **OPEN — IMPLEMENTATION GATE** — baseline captured by reference in the barrier block; `HS-9` qualified | G4 | `T-F3R-SOURCE-CAPTURE` |
| `F3R-NATIVE-RECOVERY-SNAPSHOT` (new) | — | **OPEN — IMPLEMENTATION GATE** — complete native archive, preflighted, owned until isolation ends; capture failure rejects before mutation | G1 | FR-F3-3, `T-F3R-NATIVE-SNAPSHOT` |
| `F3R-RECOVERY-FAILURE` (containment) | OPEN — NOT ESTABLISHED | **OPEN — NOT ESTABLISHED** (effective containment, NE2); contract corrected: three exits, no export of the uncertified state, save-prompt bypass, Undo re-enabled before reopen | G5, NE2 | FR-F3-4, `T-F3R-ISOLATION-*` |
| `F3R-EDT` (new) | — | **OPEN — NOT ESTABLISHED** (NE1); no lock cycle at source level | NE1 | FR-F3-5, `T-F3R-EDT` |
| `F3R-FACTORY-LIFECYCLE` (new) | — | **OPEN — IMPLEMENTATION GATE** — stateless override | G6 | `T-F3R-FACTORY` |
| `F3R-EXCEPTION-PRESERVATION` (new) | — | **OPEN — IMPLEMENTATION GATE** — install-then-refresh attach; suppressed exceptions | G7 | `T-F3R-ATTACH` |
| `F3R-DOCUMENT-IDENTITY-SUCCESS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (technical) | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — UUID kept on success and in the recovery copy (author preference, pending ratification) | D4 | FR-F3-6 |
| `F3R-ATOMICITY-COMMIT` | OPEN — PENDING AUTHOR DISPOSITION | unchanged; closes only with `DQ-F3-21` | — | — |
| `F3R-INHERITED-STORE-RACE` (observation N3) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — inherited, not changed by F3; cannot cause a false negative of C1 (thread-local marker) | N3 | — |

```text
CLOSED — EVIDENCE SUPPORTED             F3R-CLIPBOARD-D1, F3R-ARCHITECTURE (unchanged)
CLOSED WITH EXPLICIT SCOPE RESTRICTION  earlier entries plus F3R-UNDO-SNAPSHOT-OWNERSHIP
                                        (downgraded from evidence-supported),
                                        F3R-INHERITED-STORE-RACE
OPEN — IMPLEMENTATION GATE              F3R-UNDO-PENDING-ACCOUNTING, F3R-UNDO-ATTACH-RACE,
                                        F3R-RECOVERY-SOURCE-CAPTURE,
                                        F3R-NATIVE-RECOVERY-SNAPSHOT, F3R-FACTORY-LIFECYCLE,
                                        F3R-EXCEPTION-PRESERVATION
OPEN — NOT ESTABLISHED                  F3R-RECOVERY-FAILURE (effective containment), F3R-EDT
OPEN — PENDING AUTHOR DISPOSITION       F3R-ATOMICITY-COMMIT
BLOCKED                                 none
```

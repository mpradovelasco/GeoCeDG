# PRE-G9B-R6-plus-F3 — risk register update (Undo history semantic integrity)

- Activity: `PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE` (author
  instruction of 2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `T_F3_RECOVERY_CORRECTIVE` `6e41bf4426b9bdcb137f42e783038b02827ff3bd`
- Updates, without amending them, the [corrective risk update](pre_g9b_r6_plus_f3_risk_register_corrective_update.md)
  and earlier registers; entries not listed stand
- Evidence: [Undo history semantic integrity report](pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md)
- `selfApproved = false`

A technical status is not an approval and not an implementation.

| Risk | Status at `6e41bf44` | Status now | Evidence | Validity conditions | Kind |
|---|---|---|---|---|---|
| `F3R-UNDO-HISTORY-COPY-SEMANTICS` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — the inherited copy loses labels and grouping (observable: H2, H3, H4); moving the original commands through a GeoCeDG undo-manager subclass preserves them exactly (H4, H5, O3) | report §3–§5 | C2 subclass installed for every GeoCeDG document; attach in `finally` | implementation gates 1–2 |
| `F3R-UNDO-SNAPSHOT-OWNERSHIP` (new) | — | **CLOSED — EVIDENCE SUPPORTED** for detach/attach (no copies; files intact over four cycles; interruption recoverable); inherited copy only safe under a one-active-list invariant | report §4 | `finally` reinstatement | implementation gate 2 |
| `F3R-UNDO-EQUAL-CONTENT-PENDING` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION — restriction updated**: the content gate is undefined for action heads (`HS-6`); the pending-store tracker (P1–P4) plus the generation barrier replace it | report §5 | tracker on the EDT | implementation gate 3 |
| `F3R-RECOVERY-FAILURE` | OPEN — NOT ESTABLISHED (containment only) | **OPEN — NOT ESTABLISHED (containment only)**, containment contract made precise: modal isolation (scripts and animation stopped, Undo/Redo suspended, only save-verified-state / reopen / close) | report §6 D2 | implementation and runtime validation | implementation gate 7 |
| `F3R-ATOMICITY-COMMIT` | OPEN — PENDING AUTHOR DISPOSITION | unchanged; closes only with `DQ-F3-21` | — | — | author decision |
| `F3R-DOCUMENT-IDENTITY-SUCCESS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION (technical) | unchanged; confirmed with a mixed history (H5) | report §6 D3 | — | author contract (Q1) |

```text
CLOSED — EVIDENCE SUPPORTED             + F3R-UNDO-SNAPSHOT-OWNERSHIP
CLOSED WITH EXPLICIT SCOPE RESTRICTION  + F3R-UNDO-HISTORY-COPY-SEMANTICS;
                                          F3R-UNDO-EQUAL-CONTENT-PENDING (restriction updated)
OPEN — PENDING AUTHOR DISPOSITION       F3R-ATOMICITY-COMMIT
OPEN — NOT ESTABLISHED                  F3R-RECOVERY-FAILURE (containment only; implementation gate)
BLOCKED                                 none
```

# PRE-G9B-R6-plus-F3 — risk register update (recovery safety gate)

- Activity: `PRE-G9B-R6-plus-F3-DQ21-FOCUSED-RECOVERY-SAFETY-GATE` (author
  instruction of 2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `T_F3_DECISION_RECONCILIATION` `3e24ed3aca422eef4e844a6c1230a3a3fc599c21`
- Updates, without amending it: the
  [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_reconciliation.md)
  of `3e24ed3a` (kept as history, including the correction `ER-F3R-1`)
- Evidence: [focused recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
- `selfApproved = false`

Only risks touched by new evidence are listed; every other entry of the
reconciled register stands unchanged.

## 1. Correction

`ER-F3R-2` — The previous assessment stated that, for identity-bearing
payloads, "await the paste's store, `undo()`, `pruneStateList()`" leaves the
undo history identical. That holds only for a linear history: the paste's own
store prunes every pre-existing Redo state before any recovery can run (U2,
U3). The previous "await" also relied on a parameterless listener, which
cannot identify the paste's store among concurrent stores. Both statements are
superseded by the history-isolated reload and its gates.

## 2. Updated and new entries

| Risk | Status at `3e24ed3a` | Status now | Evidence | Validity conditions | Author decision |
|---|---|---|---|---|---|
| `F3R-ATOMICITY-COMMIT` | OPEN — NOT ESTABLISHED, PENDING AUTHOR DISPOSITION | **OPEN — PENDING AUTHOR DISPOSITION** (not closed by recommendation); technically: K1-S and K1-R-H certified under gates, K1-R as proposed unacceptable | report §5–§9 | gates 1–9 of the updated assessment | `DQ-F3-21` |
| `F3R-UNDO-RACE` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION — restriction replaced**: quiescence gate before mutation, content-identified bounded await, monitor-atomic swap; listener-only await unsafe | A1–A6, Q0–Q1, I1–I2 | equal-content pending stores excluded (see next row) | `DQ-F3-21` |
| `F3R-UNDO-EQUAL-CONTENT-PENDING` (new) | — | **OPEN — NOT ESTABLISHED** — a pending store whose content equals the history head passes the gate and could prune the reinstated Redo branch after recovery; no geometric or identity effect | `RG-S1`, `RG-S5` | closable by a GeoCeDG-owned pending-store counter (not probed) or accepted as residual | `DQ-F3-21` |
| `F3R-REDO-PRESERVATION` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — K1-S, inherited rollback and K1-R-H keep U1–U4 histories identical; K1-R as proposed destroys Redo (ALTERED — UNACCEPTABLE) | U1–U4 × paths | K1-R-H history isolation mandatory for identity-bearing payloads | `DQ-F3-21` |
| `F3R-DOCUMENT-IDENTITY` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** for failure paths — UUID captured and restored in K1-S; reload paths restore it from the undo state; full-XML equality certified; `tubeId` unaffected on Desktop | report §5 | restore mandatory; certificate includes the root `id` | `DQ-F3-21` |
| `F3R-DOCUMENT-IDENTITY-SUCCESS` (new) | — | **OPEN — PENDING AUTHOR DISPOSITION** — every successful paste, including Insert File and ordinary Ctrl+V, gives the document a new UUID (inherited) | `RG-S6`; `A-success-*`, `A-ordinary-paste` | outside `DQ-F3-21`; affects the success contract of `DQ-F3-13` only if the author requires document identity to survive a successful insertion | author (note in the decision sheet) |
| `F3R-SAVED-STATE` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — clean and modified flags restored in every path, no late flip, no save, path and bytes unchanged | report §8 | restore only after the paste store is identified | `DQ-F3-21` |
| `F3R-RECOVERY-FAILURE` | OPEN — NOT ESTABLISHED | **OPEN — NOT ESTABLISHED** — detection established (exceptions, certificate; E1, E5); K1-S failure has a certified second line; a persistent reload failure leaves the history cursor invalid (E2b) and the inherited reload swallows single failures (E2); containment designed, not established | report §9 | serious path; restricted operational state (design) | `DQ-F3-21` |
| `F3R-SPECIALIZED-ISOA` | CLOSED WITH RESTRICTION | unchanged; the link survival in K1-S is now **verified** (was expected) | report §10 | — | `DQ-F3-14` |
| `F3R-INHERITED-SESSION-EFFECTS` | CLOSED WITH EXPLICIT SCOPE RESTRICTION | unchanged; additionally: every successful paste regenerates the document UUID (see `F3R-DOCUMENT-IDENTITY-SUCCESS`) and non-drawable numbers and lists change runtime layer after any reload (not serialized) | report §5–§6 | documented behavior | none (factual) |

## 3. Summary

```text
CLOSED — EVIDENCE SUPPORTED             F3R-CLIPBOARD-D1, F3R-ARCHITECTURE (unchanged)
CLOSED WITH EXPLICIT SCOPE RESTRICTION  unchanged entries of the reconciled register, plus
                                        F3R-UNDO-RACE (restriction replaced),
                                        F3R-REDO-PRESERVATION, F3R-DOCUMENT-IDENTITY,
                                        F3R-SAVED-STATE
OPEN — PENDING AUTHOR DISPOSITION       F3R-ATOMICITY-COMMIT, F3R-DOCUMENT-IDENTITY-SUCCESS
OPEN — NOT ESTABLISHED                  F3R-RECOVERY-FAILURE, F3R-UNDO-EQUAL-CONTENT-PENDING
BLOCKED                                 none
```

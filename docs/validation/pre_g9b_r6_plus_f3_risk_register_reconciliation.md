# PRE-G9B-R6-plus-F3 — risk register reconciliation

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `T_F3_RISK` `8b41af11316f90d7a068ce6af4859213240ce6a6`, tree
  `8be60e86e09f957f1735990b966190bd44ffab2a`
- Supersedes as the current register (without amending it): the
  [risk closeout candidate](pre_g9b_r6_plus_f3_risk_closeout_candidate.md) of
  `T_F3_RISK`, kept as historical evidence
- Evidence: [focused feasibility report](pre_g9b_r6_plus_f3_focused_feasibility_report.md);
  [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md) (`DQ21-*`)
- `selfApproved = false`

## 1. Correction

`ER-F3R-1` — The focused closeout reported, for the stage faults A5, A7 and A8
(identity-bearing payload, compensation by `restoreCurrentUndoInfo()`), a
byte-identical restored XML. `DQ21-Q1` shows that this depended on timing: the
inherited identity-bearing paste stores a full undo state asynchronously; when
that store lands before or around the compensation, the restore keeps the
inserted state and a later Undo makes the failed insertion **reappear**. The
earlier rows remain correct as observed runs but do **not** establish the
compensation procedure. The procedure "await the paste's store, `undo()`,
`pruneStateList()`" is established by `DQ21-Q2`.

## 2. Register

| Risk | Previous status (`T_F3_RISK`) | Current status | Evidence | Validity conditions and limitations | Author decision |
|---|---|---|---|---|---|
| `F3R-ATOMICITY` (pre-commit) | CLOSED WITH EXPLICIT SCOPE RESTRICTION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** (unchanged) | G2 A0, A6d; V1/V2 detection | admission and strict dry run mandatory | `DQ-F3-18` |
| `F3R-ATOMICITY-COMMIT` | OPEN — NOT ESTABLISHED | **OPEN — NOT ESTABLISHED, PENDING AUTHOR DISPOSITION** — technically resolved into options: K1-S proven for payloads without identity records; K1-R proven only with the await/undo/prune ordering; the immediate-restore procedure is incompatible; recovery failure surfaced but uncertified | `DQ21-Q1`–`Q5`, `ER-F3R-1` | closes only with the author's `DQ-F3-21` disposition and the implementation gates of the assessment §5 | `DQ-F3-21` |
| `F3R-MACRO-M1` | CLOSED WITH RESTRICTION | unchanged | G3, V4, V5, V7 | transitive closure; digest reuse; no identity + macros; macros kept after Undo pending `DQ-F3-22` | `DQ-F3-15`, `-22`, `-24` |
| `F3R-LABEL-L1` | CLOSED WITH RESTRICTION | unchanged | G4 | no scripts, no label-bearing static strings | `DQ-F3-5` |
| `F3R-SPECIALIZED-LOCUS` | CLOSED WITH RESTRICTION | unchanged; recovery of a Locus V2 payload uses K1-R (identity) | G5, `DQ21-Q2` | no macros in payload; Locus V2 enabled in the target | `DQ-F3-14`, `-21` |
| `F3R-SPECIALIZED-SPLINE` | CLOSED WITH RESTRICTION | unchanged (as Locus) | G5 | as Locus | `DQ-F3-14` |
| `F3R-SPECIALIZED-DIMENSIONS` | CLOSED WITH RESTRICTION | unchanged; recovery uses K1-S (not identity-bearing) | G5, `RK-dynamic` | text in target units | `DQ-F3-14` |
| `F3R-SPECIALIZED-ISOA` | CLOSED WITH RESTRICTION | unchanged; target link survives K1-S (expected, not probed), lost by K1-R | G5, X-iso-link | no link imported | `DQ-F3-14`, `-21` |
| `F3R-CLIPBOARD-D1` | CLOSED — EVIDENCE SUPPORTED | unchanged (independent buffer unaffected by every recovery path) | G6 | `DQ-D1-7` amendment | `DQ-F3-16` |
| `F3R-TEMPLATE-T0` | CLOSED WITH RESTRICTION | unchanged | G7 | exclusions; six properties | `DQ-F3-17`, `-23` |
| `F3R-ARCHITECTURE` | CLOSED — EVIDENCE SUPPORTED (U0) | unchanged for K1/S; K2 requires shared changes | G8, assessment §3 | — | `DQ-F3-19`, `-21` |
| `F3R-UNDO-RACE` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — the asynchronous paste store must be awaited before any compensation and before the orchestration's own success-path decision | `DQ21-Q1`, `Q2`; S `UndoManagerD.java:58-69` | implementation gate 1–2 of the assessment | `DQ-F3-21` |
| `F3R-RECOVERY-FAILURE` (new) | — | **OPEN — NOT ESTABLISHED** for the resulting state; surfaced as a distinct exception | `DQ21-Q5` | serious-failure path required | `DQ-F3-21` |
| `F3R-INHERITED-SESSION-EFFECTS` (new) | — | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — the inherited paste clears the selection and activates Move in every path, including success | S `CopyPasteD.java:487-490`, `:552`; G2 | documented behavior | none (factual) |

```text
CLOSED — EVIDENCE SUPPORTED             F3R-CLIPBOARD-D1, F3R-ARCHITECTURE
CLOSED WITH EXPLICIT SCOPE RESTRICTION  F3R-ATOMICITY, F3R-MACRO-M1, F3R-LABEL-L1,
                                        F3R-SPECIALIZED-LOCUS, -SPLINE, -DIMENSIONS, -ISOA,
                                        F3R-TEMPLATE-T0, F3R-UNDO-RACE,
                                        F3R-INHERITED-SESSION-EFFECTS
OPEN — PENDING AUTHOR DISPOSITION       F3R-ATOMICITY-COMMIT
OPEN — NOT ESTABLISHED                  F3R-RECOVERY-FAILURE (state after a failed recovery)
BLOCKED                                 none
```

# PRE-G9B-R6-plus-F3 — risk register update (corrective feasibility gate)

- Activity: `PRE-G9B-R6-plus-F3-DQ21-RECOVERY-CORRECTIVE-FEASIBILITY-GATE`
  (author instruction of 2026-10-12)
- Status: **CANDIDATE — PENDING AUTHOR REVIEW**
- Base: `T_F3_RECOVERY_SAFETY` `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6`
- Updates only the three risks in scope of the
  [recovery-gate risk update](pre_g9b_r6_plus_f3_risk_register_recovery_gate_update.md)
  (unchanged, history); every other entry stands
- Evidence: [corrective feasibility report](pre_g9b_r6_plus_f3_dq21_corrective_feasibility_report.md)
- `selfApproved = false`

A technical closure below does not approve any proposed contract; the author
decides `DQ-F3-21`.

| Risk | Status at `6c07e01a` | Status now | Evidence | Validity conditions | Author decision |
|---|---|---|---|---|---|
| `F3R-UNDO-EQUAL-CONTENT-PENDING` | OPEN — NOT ESTABLISHED | **CLOSED WITH EXPLICIT SCOPE RESTRICTION** — the generation barrier cancels every store scheduled before the operation, whatever its content (source guarantee `CF-S1`/`CF-S2`); runtime A-Q1, A-Q3, A-Q4b; the content gate rejects differing pending stores before mutation (A-Q4a) | report §3 | gate, preflight and barrier in one monitor block; the operation on one thread; unexpected post-barrier store → serious; EDT repetition of A-Q1 as implementation gate | `DQ-F3-21` |
| `F3R-RECOVERY-FAILURE` | OPEN — NOT ESTABLISHED | **OPEN — NOT ESTABLISHED (containment only)**, decomposed: detection **CLOSED — EVIDENCE SUPPORTED** (B-F1, B-F3, B-F4); completion **CLOSED WITH EXPLICIT SCOPE RESTRICTION** (one bounded retry; B-F2, B-F3); history preservation on persistent failure **CLOSED — EVIDENCE SUPPORTED** (B-F4, B-F5; the inherited fallback is bypassed); containment **OPEN — NOT ESTABLISHED**: Undo/Redo suspension proven, save guard and insertion lock specified at existing GeoCeDG override points but not implemented, editing not blockable with existing mechanisms | report §4 | guarded reload, B3 guards as implementation gate 6 | `DQ-F3-21` (contract: what the user may do with an uncertified drawing) |
| `F3R-DOCUMENT-IDENTITY-SUCCESS` | OPEN — PENDING AUTHOR DISPOSITION | **CLOSED WITH EXPLICIT SCOPE RESTRICTION (technical)** — the UUID is a document-lineage id (`CF-S7`), already protected by GeoCeDG on rejected opens (`CF-S8`); preservation on successful insertion is feasible and verified for elementary, Locus V2, Spline V2 and macro payloads, through Undo, Redo, Save, Save As and reopen; ordinary Ctrl+V unchanged | report §5 | UUID restored before the orchestration's single undo store; the paste's own store replaced for identity payloads | contract choice: preserve (recommended) or allow regeneration |

Unchanged: `F3R-ATOMICITY-COMMIT` — OPEN — PENDING AUTHOR DISPOSITION (closes
only with `DQ-F3-21`).

```text
CLOSED WITH EXPLICIT SCOPE RESTRICTION  + F3R-UNDO-EQUAL-CONTENT-PENDING,
                                          F3R-DOCUMENT-IDENTITY-SUCCESS (technical)
OPEN — PENDING AUTHOR DISPOSITION       F3R-ATOMICITY-COMMIT
OPEN — NOT ESTABLISHED                  F3R-RECOVERY-FAILURE (containment only)
BLOCKED                                 none
```

# PRE-G9B-R6-plus-F3 — author decision preparation update (recovery safety gate)

- Record version: **4.1** — updates §2, §4 and Q1 of the
  [final author decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md)
  v4.0 (unchanged, history); everything not stated here stands as in v4.0
- Activity: `PRE-G9B-R6-plus-F3-DQ21-FOCUSED-RECOVERY-SAFETY-GATE` (author
  instruction of 2026-10-12)
- Status: **PROPOSED — PENDING FINAL AUTHOR DISPOSITION — NOT AN AUTHOR
  DECISION RECORD**
- Base: `T_F3_DECISION_RECONCILIATION` `3e24ed3aca422eef4e844a6c1230a3a3fc599c21`
- Evidence: [focused recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md);
  [updated DQ-F3-21 assessment](pre_g9b_r6_plus_f3_dq21_recovery_option_assessment.md);
  [risk register update](pre_g9b_r6_plus_f3_risk_register_recovery_gate_update.md)
- `DQ-F3-1..24 — PENDING FINAL AUTHOR DISPOSITION`
- `selfApproved = false`

## 1. Established technical facts added to v4.0 §2 (not questions)

10. The identity-bearing paste's own undo store prunes every pre-existing Redo
    state; K1-R as proposed (await, `undo`, `prune`) therefore destroys Redo.
11. Keeping only the history prefix live during the commit (public inherited
    API) and reinstating the full history on failure preserves the complete
    Undo/Redo history for Locus V2 and Spline V2 payloads, with no
    reappearance.
12. Every successful paste gives the document a new UUID; on failure the UUID
    is restorable and the full document XML becomes byte-identical.
13. The saved flag is restorable in every path without saving.
14. Recovery before the paste store lands either undoes the user's previous
    action or lets the failed insertion reappear; the store can be identified
    by content, and a pending store with different content can be detected
    before mutation.
15. A persistent failure while an undo state loads makes the inherited code
    walk the history cursor backwards to an invalid position.

## 2. Q1 — Commit-time failure recovery (`DQ-F3-21`) — **blocking** (replaces v4.0 Q1)

- **Recommended:** K1 in its corrected form — K1-S (subtractive, UUID restore,
  second-line reload) for payloads without identity records; **K1-R-H**
  (history-isolated reload) for identity-bearing payloads and inherited
  rollback failures; both behind a pre-commit quiescence gate, with
  content-identified store await, an atomic history swap, the full certificate
  and the serious-failure path. K1-R as proposed is withdrawn.
- **Alternatives:** S for identity-bearing payloads (no K1-R-H; loses Locus V2
  and Spline V2 insertion); K2 (shared changes, likely `GLOBAL_IMPACT`); adding
  a GeoCeDG-owned pending-store counter to close the equal-content residual
  (classification review).
- **Consequences:** `INTEGRATED_PHASE` and U0 hold for K1-S, K1-R-H and S; the
  implementation gains the nine gates of the updated assessment §4.
- **Residual risk:** declared session reset in K1-R-H; an equal-content pending
  store could still prune the reinstated Redo branch (no geometric effect);
  persistent reload failure → serious, history unusable until reopen.
- **Note for the author (no new question):** a *successful* Insert File, like
  every paste, gives the document a new UUID. If document identity must
  survive a successful insertion, that belongs to the `DQ-F3-13` contract (Q2)
  and needs its own design; otherwise it is recorded as inherited behavior.

Q2–Q7 of v4.0 are unchanged: no new evidence contradicts them.

## 3. Author Decision Update (answers)

| # | Question | Established fact | Conditional feasibility | Unresolved risk |
|---|---|---|---|---|
| 1 | Is K1-S demonstrably safe under its proposed contract? | yes, with UUID restore: byte-identical XML, identical history including Redo (U1–U4), instances, F2 memory and IsoABorder link kept, macros and saved flag restored, save/reopen exact | without UUID restore it is not (UUID changed) | removal failure needs the second-line reload (verified once, E1) |
| 2 | Is K1-R demonstrably safe with a pre-existing Redo branch? | **no** as proposed: Redo destroyed (U2, U3) | **yes** as K1-R-H: Redo intact in U2/U3 for Locus V2 and Spline V2 | equal-content pending store; persistent reload failure |
| 3 | Can document identity and saved/modified state be preserved? | yes on every failure path (UUID restore or reload; flag restored; no save; path and bytes unchanged) | the saved flag is correct only after the paste store is identified | a successful insertion changes the UUID (inherited; author note) |
| 4 | Can asynchronous stores be controlled without race conditions? | the paste store is identified by content (5/5); a pending store with different content is detected before mutation | yes with the quiescence gate, content-identified bounded await and monitor-atomic swap | store order is not defined by the source; an equal-content pending store is not detectable |
| 5 | Is failure during recovery detectable and contained? | detectable by exceptions and the certificate; K1-S failure has a certified second line | containment (no further mutation, serious message, restricted state) is designed | inherited reload swallows single failures and a persistent one corrupts the history cursor; containment not established |
| 6 | Does K1 remain the recommended option? | K1-R as proposed does not | K1 in the corrected form (K1-S + K1-R-H + gates) is recommended; S is the fallback per payload class | author acceptance of the K1-R-H session reset |
| 7 | Are additional technical gates required before author approval? | yes: the nine gates of the updated assessment §4 replace the earlier six | they are implementation gates; author approval of `DQ-F3-21` can name them as conditions | `F3R-RECOVERY-FAILURE` and `F3R-UNDO-EQUAL-CONTENT-PENDING` stay open until implementation evidence |

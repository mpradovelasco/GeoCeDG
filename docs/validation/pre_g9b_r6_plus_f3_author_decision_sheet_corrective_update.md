# PRE-G9B-R6-plus-F3 — author decision preparation update (corrective feasibility gate)

- Record version: **4.2** — replaces Q1 of the
  [recovery update](pre_g9b_r6_plus_f3_author_decision_sheet_recovery_update.md)
  v4.1 (unchanged, history); Q2–Q7 stand as in the
  [final author decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md)
  v4.0 §6
- Activity: `PRE-G9B-R6-plus-F3-DQ21-RECOVERY-CORRECTIVE-FEASIBILITY-GATE`
  (author instruction of 2026-10-12)
- Status: **PROPOSED — PENDING FINAL AUTHOR DISPOSITION — NOT AN AUTHOR
  DECISION RECORD**
- Base: `T_F3_RECOVERY_SAFETY` `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6`
- Evidence: [corrective feasibility report](pre_g9b_r6_plus_f3_dq21_corrective_feasibility_report.md);
  [corrected option assessment](pre_g9b_r6_plus_f3_dq21_corrected_option_assessment.md);
  [risk update](pre_g9b_r6_plus_f3_risk_register_corrective_update.md)
- `DQ-F3-1..24 — PENDING FINAL AUTHOR DISPOSITION`
- `selfApproved = false`

## 1. Established technical facts added (not questions)

16. Every undo store scheduled before an insertion can be cancelled with
    existing public API (generation barrier); a pending store whose content
    differs from the drawing is detected before mutation.
17. With the barrier, both recovery paths keep the complete previous Undo/Redo
    history, including with equal-content pending stores and late landings.
18. A guarded reload surfaces failures without touching the history; a single
    fault recovers with one retry; a persistent fault leaves the history intact.
19. Undo/Redo can be suspended without losing the history; GeoCeDG has no
    mechanism today that stops editing or saving an uncertified drawing.
20. The document UUID is a document-lineage id; it can be kept on a successful
    insertion through Undo, Redo, Save, Save As and reopen.

## 2. Q1 — Commit-time failure recovery (`DQ-F3-21`) — **blocking** (replaces v4.1 Q1)

- **Recommended:** K1 in the barrier form (corrected assessment §2): K1-S for
  payloads without identity records, K1-R-H guarded reload for identity
  payloads and inherited rollback failures, S rejection for a differing pending
  store or an unloadable recovery source; destination document UUID preserved
  on success; serious-failure containment with Undo/Redo suspended, no
  overwrite of the current file, no further insertions and a persistent
  message, while **ordinary editing remains possible with that warning**.
- **Alternatives:** containment that also blocks editing (needs a new
  edit-lock mechanism, not characterized); allow UUID regeneration on success
  (inherited behavior); S for identity-bearing payloads (loses Locus V2 and
  Spline V2 insertion); K2 (shared, likely `GLOBAL_IMPACT`, no extra F3
  guarantee).
- **Consequences:** U0, `INTEGRATED_PHASE`; seven implementation gates
  (assessment §4).
- **Residual risk:** declared session reset for identity payloads; a
  persistent reload failure leaves an uncertified drawing that the user can
  still edit (with warning) and save under a new name.

Exact wording proposed for the approval, if the author agrees:

> `DQ-F3-21` is resolved as K1 in the barrier form of the corrected option
> assessment §2, with its normative wording and implementation gates 1–7; the
> destination document UUID is preserved on successful insertion; after a
> serious recovery failure, Undo/Redo is suspended, the current file is not
> overwritten, further insertions are refused and editing remains possible
> with a persistent warning.

## 3. Author Decision Update (answers)

| # | Question | Demonstrated fact | Conditional feasibility | Author-level choice | Blocking uncertainty |
|---|---|---|---|---|---|
| 1 | Can equal-content pending stores be controlled without shared changes? | yes: generation barrier cancels every pre-operation store (source `CF-S1`; A-Q1, A-Q3, A-Q4b, A-Q-K1S) | gate + barrier in one monitor block, operation on one thread, EDT repetition as gate | none | none |
| 2 | Does K1-R-H preserve the entire previous history under all admitted scheduling conditions? | yes, in the barrier form (Locus V2, Spline V2; Redo C→D; late landings discarded) | conditions of answer 1; differing pending stores rejected before mutation | none | none |
| 3 | Can persistent recovery failures be detected and contained without further uncontrolled mutation? | detection and history preservation yes (B-F1–B-F5); no automatic retry beyond one | containment via Undo/Redo suspension (proven) plus save guard and insertion lock (specified, not implemented) | whether editing an uncertified drawing is allowed with a warning | none technical; the guards are implementation work at existing GeoCeDG override points |
| 4 | Should successful insertion preserve the destination UUID? | the UUID is a document-lineage id; preservation verified through Undo, Redo, Save, Save As, reopen | — | preserve (recommended) or allow regeneration | none |
| 5 | Is U0 still justified? | yes: every guarantee uses public inherited API | — | — | none |
| 6 | Does `INTEGRATED_PHASE` remain appropriate? | Desktop-only change set (`verification-levels.md` §12.8) | frozen only at authorization | — | none |
| 7 | Can `DQ-F3-21` be submitted for final disposition? | **yes** | — | the two choices above | none |
| 8 | Smallest remaining unresolved dependency | none that requires investigation | implementation gates 1–7 (notably EDT repetition of A-Q1 and B-F4, and the containment guards) | — | — |

**Recommendation:** proceed to the final author disposition of Q1 (this
version) and Q2–Q7 (v4.0 §6).

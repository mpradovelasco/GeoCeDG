# PRE-G9B-R6-plus-F3 — DQ-F3-21 corrected option assessment

- Activity: `PRE-G9B-R6-plus-F3-DQ21-RECOVERY-CORRECTIVE-FEASIBILITY-GATE`
  (author instruction of 2026-10-12)
- Status: **ASSESSMENT — RECOMMENDATION ONLY — PENDING AUTHOR DISPOSITION**
- Base: `T_F3_RECOVERY_SAFETY` `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6`
- Successor of (all unchanged, history): the
  [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md)
  (`3e24ed3a`) and the [recovery option assessment](pre_g9b_r6_plus_f3_dq21_recovery_option_assessment.md)
  (`6c07e01a`)
- Evidence: [corrective feasibility report](pre_g9b_r6_plus_f3_dq21_corrective_feasibility_report.md)
  (`CF-S1`–`CF-S8`, A-Q1–A-Q6, B-F1–B-F6, Focus C);
  [recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
- `selfApproved = false`

## 1. History of the option (kept visible)

| Step | Finding | Status |
|---|---|---|
| `DQ21-Q1` / `ER-F3R-1` (`3e24ed3a`) | an immediate restore after an identity-bearing commit lets a failed insertion reappear | still valid; restore forbidden while the paste store can land |
| K1-R (`3e24ed3a`): await, `undo`, `pruneStateList` | — | **withdrawn** by `ER-F3R-2` |
| `ER-F3R-2` (`6c07e01a`) | the paste's own store prunes every pre-existing Redo state | still valid |
| K1-S (`6c07e01a`) | certified with UUID restore and second-line reload | still valid; now runs inside the barrier transaction |
| K1-R-H prefix form (`6c07e01a`) | Redo kept, but an equal-content pending store landing after the swap prunes Redo (A-Q3: NOT CERTIFIED) | **superseded** by the barrier form |
| K1-R-H barrier form (this activity) | every pre-barrier store cancelled (`CF-S1`); guarded reload without inherited fallback | recommended |

## 2. Recommended procedure — barrier transaction

```text
0 BARRIER     in one block synchronized on the undo manager:
              a) the history head archive equals the live undo XML, else REJECT
                 BEFORE MUTATION ("try again"; nothing changed)
              b) the head archive passes DocumentArchivePreflight, else REJECT
                 BEFORE MUTATION
              c) capture UUID, tubeId, saved flag, macro names
              d) undoHistoryTo(held); prepareUndoBaseline + commitUndoBaseline
                 -> generation advanced: every earlier store, of any content,
                    is cancelled whenever it lands
1 COMMIT      insertFrom(...) after admission and dry run
2 IDENTIFY    identity payload: exactly one post-barrier store, bounded wait, its
              content equal to the post-commit document; elementary payload:
              none expected; anything else -> SERIOUS
3 VERIFY      strict verification
4 SUCCESS     restore the captured UUID; history := held prefix + one store made
              by the orchestration (the paste's own store is discarded); held
              Redo files deleted as for any new action; await that store
5 K1-S        no identity records: remove created objects in reverse order,
              remove transferred macros, restore UUID, reinstate held history,
              restore saved flag, certify; a removal failure goes to 6
6 RELOAD      identity payload, inherited rollback, or K1-S second line: guarded
              reload of the held head archive (readZipFromMemory; no history
              access; no inherited fallback), at most one retry; reinstate held
              history; restore saved flag; certify
7 SERIOUS     reload failed twice, certificate failed, unexpected store, or
              inherited rollback threw: reinstate held history unchanged;
              suspend Undo/Redo (Kernel.setUndoActive(false)); refuse saving over
              the current file (Save As allowed); refuse further insertions;
              persistent serious message; document marked modified; no further
              automatic change
```

## 3. Decision table

| Alternative | Criterion | Assessment |
|---|---|---|
| K1-S | guarantees | full XML byte-identical (UUID restored), identities, DAG, complete Undo/Redo history, live instances, Parallel/Perpendicular memory, IsoABorder link, macros, saved flag, file; selection cleared and Move active (inherited); second line certified with declared session reset |
| K1-S | restrictions | barrier step 0 mandatory (a late store would otherwise disturb the history certificate) |
| K1-R-H | equal-content pending store | **cancelled by the generation barrier** — source guarantee + runtime A-Q1, A-Q3, A-Q4b |
| K1-R-H | recovery failure containment | detection established; completion within one retry established; history preserved on persistent failure; containment needs the B3 guards (not implemented) |
| K1-R-H | Undo/Redo preservation | complete previous history identical (list, cursor, every reachable state) under every scheduling condition admitted by the gate |
| K1-R-H | identity and persistence | UUID, identities, saved flag, file restored and certified; declared reset of live instances, F2 memory, IsoABorder link, selection, runtime layer of non-drawables |
| S | coverage | rejection before mutation for: differing pending store, unloadable recovery source, inadmissible payload; as a payload-class fallback if the author rejects K1-R-H's declared reset |
| K2-minimal | exact shared changes | (a) pending-store identity or drain API in `UndoManagerD`; (b) fail-fast `loadUndoInfo` without recursive fallback; (c) clipboard import that keeps the document id |
| K2-minimal | Classic impact | (a) none functionally; (b) changes every Undo failure path in Classic and GeoCeDG; (c) changes every paste |
| K2-minimal | verification | shared code consumed by every frontend → author reclassification review, likely `GLOBAL_IMPACT` (FULL) |
| K2-minimal | stronger than K1-R-H for F3? | **no** — the barrier already cancels every pending store; the guarded reload already bypasses (b); F3 can preserve the UUID without (c). K2 would only extend guarantees to non-F3 operations |

## 4. Recommendation (agent proposal; author disposition pending)

**K1 (K1-S + K1-R-H) in the barrier form of §2**, U0, `INTEGRATED_PHASE`;
**S** for the rejection cases of step 0 and as the payload-class fallback;
**K2 not recommended** (no additional F3 guarantee; global impact);
**preserve the destination document UUID** on successful insertion.

### Proposed normative wording (replaces the previous assessment's wording)

> An insertion starts only when the drawing's last undo state matches the
> drawing and can be reloaded; otherwise it is rejected and nothing changes.
> Undo states still being recorded at that moment are discarded; they repeat
> the last undo state. An insertion is accepted only when admission, the dry
> run, the commit and the strict verification all succeed; it is then exactly
> one undo step, and the drawing keeps its document identity. A failure after
> the commit restores the drawing exactly — construction, durable identities,
> document identity, units, layers, macros, saved state and the complete Undo
> and Redo history. When no identity-bearing object was created, live objects,
> the remembered references of Parallel/Perpendicular and the IsoABorder export
> link are kept; otherwise the drawing is reloaded and those, with the
> selection, are reset as after an Undo, and the user is told so. The selection
> is always cleared and the Move tool activated. If the restoration fails or
> cannot be verified, GeoCeDG says so with a distinct serious message, keeps
> the Undo and Redo history intact but suspended, does not overwrite the
> current file, refuses further insertions and makes no further automatic
> change; it never reports the case as an ordinary rejection.

### Implementation gates (replace gates 1–9 of the previous assessment)

1. Step 0 as one monitor-protected block; tests: equal-content pending store
   cancelled (A-Q1), differing pending store rejected (A-Q4a), late landing
   after recovery discarded (A-Q3), all on the EDT.
2. Own-store identification by generation and content; unexpected store →
   serious.
3. Guarded reload without inherited fallback, one retry; tests B-F2–B-F5.
4. Success path: UUID preserved, one undo step, paste store replaced for
   identity payloads; tests for the four payload classes with prior Redo,
   Save, Save As, reopen.
5. Certificate: full XML, root id, identities, macros, history list and
   cursor, saved flag, file association; runtime state compared separately.
6. Serious path and containment: Undo/Redo suspension, save guard on the
   current file, insertion lock, persistent message; tests B-F4, B-F6.
7. Regression tests for `ER-F3R-1` and `ER-F3R-2`.

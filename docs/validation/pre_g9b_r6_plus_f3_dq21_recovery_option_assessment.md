# PRE-G9B-R6-plus-F3 — DQ-F3-21 recovery option assessment (successor)

- Activity: `PRE-G9B-R6-plus-F3-DQ21-FOCUSED-RECOVERY-SAFETY-GATE` (author
  instruction of 2026-10-12)
- Status: **ASSESSMENT — RECOMMENDATION ONLY — PENDING AUTHOR DISPOSITION**; no
  option selected
- Base: `T_F3_DECISION_RECONCILIATION` `3e24ed3aca422eef4e844a6c1230a3a3fc599c21`
- Successor of (unchanged, history): the
  [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md)
  (`DQ21-Q1`–`Q5`, recommendation "K1 refined"); the correction `ER-F3R-1` of
  the [risk register reconciliation](pre_g9b_r6_plus_f3_risk_register_reconciliation.md)
  stays valid and visible
- Evidence: [focused recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
  (`RG-S*`, A–E); machine-readable
  [recovery-state matrix](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-dq21-recovery-state-matrix.json)
- `selfApproved = false`

## 1. What changed since the previous assessment

| Previous statement | Status now | Evidence |
|---|---|---|
| `ER-F3R-1`: an immediate restore lets a failed insertion reappear | **confirmed deterministically** (A3b); additionally an Undo issued before the store undoes the user's previous action (A3a) | report §7 |
| K1-R: "await the paste store, `undo()`, `pruneStateList()` keeps the undo history identical" | **true only for a linear history**; the paste store itself destroys any pre-existing Redo branch (U2, U3) | report §6 |
| "await the store" via the store listener | **unsafe as specified**: the listener has no argument and concurrent stores have no defined order; content identification is required | report §7 |
| K1-S "the XML differs only in the root document id" | the id is restorable through `App.setUniqueId`; with the restore the full XML is byte-identical and save/reopen is exact | report §5 |
| K1-S keeps the IsoABorder export link | **verified** | report §10 |
| "immediate `restoreCurrentUndoInfo()` forbidden" | refined: forbidden **while the paste store can still land**; permitted after that store has been identified and removed from an isolated history | report §6–§7 |
| recovery failure surfaced as an exception | partly: an undo-load failure is swallowed and retried by inherited code; a persistent one walks the history cursor to an invalid position | report §9 |

## 2. Options, re-evaluated

### K1-S — subtractive recovery (payloads without identity records)

The paste stores nothing (`RG-S3`). Recovery: remove the created objects in
reverse construction order, remove transferred macros, restore the captured
document UUID and the saved flag, then certify. If the removal fails or the
certificate fails, a **second line** reloads the history head (which is the
pre-insertion state because nothing was stored) with `restoreCurrentUndoInfo`
and certifies again (E1).

### K1-R as proposed — await, `undo()`, `pruneStateList()`

**Not acceptable** under the safe-rejection contract: destroys a pre-existing
Redo branch (U2, U3); depends on an unidentified listener notification. Kept
only as history.

### K1-R-H — history-isolated reload (identity-bearing payloads)

A narrowly scoped Desktop-owned correction using only public inherited API
(`RG-S10`), demonstrated by the probes:

```text
0 QUIESCENCE   the content of the history head file equals the live undo XML;
               otherwise reject before mutation ("try again"; nothing changed)
1 ISOLATE      undoHistoryTo(held); undoHistoryFrom(copy of the prefix up to the cursor)
2 COMMIT       insertFrom(...) as in the design (admission and dry run already passed)
3 IDENTIFY     capture the live undo XML when insertFrom returns; wait, bounded, until
               the history head content equals it (the paste's own store); else SERIOUS
4 VERIFY       strict verification as designed
5a SUCCESS     keep the live prefix + paste store (one undo step); delete the held
               Redo files (as any new action would)
5b FAILURE     inside one monitor-protected step: delete the operation's entries,
               clearUndoInfo(), undoHistoryFrom(held), restoreCurrentUndoInfo();
               remove transferred macros (none for identity payloads under DQ-F3-24);
               restore the saved flag; certify (full XML incl. UUID, history list
               and cursor, identities); else SERIOUS
```

Inherited rollback inside the paste (exception during evaluation): the
history is never touched and the UUID is restored by the rollback XML; the
orchestration restores the saved flag, reinstates the held history unchanged
and certifies (U1–U4 identical).

### K2 — shared non-rebuilding transaction

Unchanged from the previous assessment: subtractive rollback in
`InternalClipboard`, deferred or suppressed paste store, optional synchronous
store API — shared and inherited changes, every paste in GeoCeDG and Classic,
author reclassification review, likely `GLOBAL_IMPACT`. It would also have to
solve the Redo pruning and the store identification (`RG-S1`, `RG-S5`).
Nothing about K2 was probed.

### S — preventive rejection

Applies per payload class: if the author rejects the session reset of K1-R-H
or its gates, identity-bearing payloads (Locus V2 family, Spline V2) are
rejected before mutation and only K1-S payloads are admitted. The quiescence
gate (step 0) is itself an S-type rejection and is recommended for every path.

## 3. Assessment table

| Criterion | K1-S | K1-R as proposed | K1-R-H | K2 | S |
|---|---|---|---|---|---|
| Geometry/DAG | preserved — verified | restored — verified | restored — verified | not established | preserved |
| Semantic identities | preserved — verified | restored — verified | restored — verified | not established | preserved |
| Document UUID / metadata | preserved with mandatory `setUniqueId` restore — verified | restored by reload — verified | restored by reload — verified | not established | preserved |
| Undo/Redo | complete history identical, Redo intact (U1–U4) | **Redo destroyed (U2, U3)** | complete history identical, Redo intact (U1–U4) | not established | untouched |
| Asynchronous operations | no paste store; quiescence gate needed for the certificate | listener-based await unsafe (A3a, A4) | quiescence gate + content identification + monitor-atomic swap; equal-content pending store unresolved | would need a new store API | none |
| Saved/modified flag | restored — verified (clean, modified) | restored after await — verified | restored — verified | not established | untouched |
| Session state | preserved (instances, F2 memory, IsoABorder link); selection cleared and Move active (inherited) | reset (declared) | reset (declared): instances, F2 memory, IsoABorder link, selection; runtime layer of non-drawables | not established | untouched |
| Macros | transferred removed, existing kept — verified | identity + macros rejected (`DQ-F3-24`) | same | — | untouched |
| Failure of recovery | removal failure detectable → second-line reload certified (E1) | undo-load failure swallowed or history corrupted (E2, E2b) | same reload risk as K1-R (E2b) → serious | — | — |
| Architecture | Desktop, U0 | Desktop, U0 | Desktop, U0 (public inherited API; reads undo files) | shared + inherited | Desktop |
| Classic impact | none | none | none | required | none |
| Verification class | `INTEGRATED_PHASE` | — | `INTEGRATED_PHASE` | reclassification review, likely `GLOBAL_IMPACT` | `INTEGRATED_PHASE` |
| Residual risks | inherited success-path UUID change; selection/Move | — | session reset; equal-content pending store may prune the reinstated Redo; persistent reload failure | not assessed | loses Locus V2 / Spline V2 insertion |

## 4. Recommendation (agent proposal; author disposition pending)

**K1 remains the recommended family, in a corrected form:** K1-S (with UUID
restore and second-line reload) for payloads without identity records, and
**K1-R-H** for identity-bearing payloads and inherited-rollback failures, both
behind the quiescence gate, with content-identified store await, a
monitor-atomic history swap, the full certificate and the serious-failure path.
**K1-R as proposed is withdrawn.** S remains the fallback for identity-bearing
payloads if the author rejects K1-R-H's declared session reset or its gates.
K2 is not needed to meet the contract except for the unresolved equal-content
pending-store case, which a GeoCeDG-owned pending-store counter could also
close (not probed).

Established facts, conditional feasibility and unresolved risk are separated in
the [author decision sheet update](pre_g9b_r6_plus_f3_author_decision_sheet_recovery_update.md).

### Proposed normative wording (replaces the wording of the previous assessment §5)

> An insertion starts only when the drawing's last undo state matches the
> drawing; otherwise it is rejected before any change. It is accepted only when
> admission, the dry run, the commit and the strict verification all succeed;
> it is then one undo step. A failure before the commit leaves the drawing, its
> undo history and the session unchanged. A failure after a commit that created
> no identity-bearing object is compensated by removing exactly the created
> objects and restoring the drawing's identity, macros and saved state; the
> drawing, its objects, its complete Undo and Redo history and the session are
> as before, except that the selection is cleared and the Move tool is active.
> Any other commit-time failure restores the drawing from its last undo state
> after the insertion's own undo state has been identified and removed; the
> construction, durable identities, drawing identity, units, layers, saved
> state and the complete Undo and Redo history are as before; live objects, the
> remembered references of Parallel/Perpendicular, the IsoABorder export link
> and the selection are reset as after an Undo, and the user is told so. If a
> restoration fails, or its result cannot be verified, GeoCeDG reports a
> distinct serious error, makes no further automatic change, marks the drawing
> modified, and never reports the case as an ordinary rejection.

### Mandatory implementation gates (replace the six gates of the previous assessment)

1. **Quiescence:** before mutation, the history head content equals the live
   undo XML; otherwise reject before mutation.
2. **History isolation:** identity-bearing commits run with only the prefix up
   to the cursor live; the held history is reinstated unchanged on failure and
   its Redo files deleted on success.
3. **Store identification:** the paste's own store is identified by content,
   with a bounded wait; an unidentified or missing store is a serious failure.
   No recovery step runs before it (A3a, A3b regression tests).
4. **Atomic swap:** removal of the operation's entries, history reinstatement
   and reload run as one step synchronized on the undo manager.
5. **Document identity:** the UUID is captured before the commit and restored
   in K1-S; full-XML equality is part of every certificate.
6. **Saved state:** restored only after the store is identified; never by
   saving.
7. **Certificate:** full XML, identities, construction fingerprint, macros,
   history list and cursor, saved flag, file association — runtime state
   compared separately and declared resets listed.
8. **Serious path:** reachable by injected faults in removal (E1 first line),
   reload (E2b) and inherited rollback (`DQ21-Q5`); never produces a success or
   ordinary-rejection message.
9. **Tests:** U1–U4 for each path and payload class, A3a/A3b reproductions,
   quiescence rejection, D1/D2 saved-state cases, save/reopen.

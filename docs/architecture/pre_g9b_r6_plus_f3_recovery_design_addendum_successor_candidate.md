# PRE-G9B-R6-plus-F3 — recovery design addendum successor candidate

- Status: **DESIGN — AUTHOR APPROVED** (author decision of 2026-10-13, [F3 author design closeout record](../validation/pre_g9b_r6_plus_f3_author_design_closeout_record.md); implementation NOT AUTHORIZED)
- Produced by: `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
  instruction of 2026-10-12; documentary)
- Base: `T_F3_UNDO_HISTORY_INTEGRITY` `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`
- Successor of (unchanged, history): the
  [recovery design addendum candidate](pre_g9b_r6_plus_f3_recovery_design_addendum_candidate.md);
  it amends the recovery parts of the
  [restricted insertion design successor candidate](pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
  (§2–§3), whose other content stands
- Evidence: [independent audit](../validation/pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md);
  [audit reconciliation report](../validation/pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md);
  [Undo history semantic integrity report](../validation/pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md)
- `selfApproved = false`

Scope unchanged: R, L1, M1, the independent buffer, D1 provenance, T0 and the
admissibility matrix stand as proposed. The withdrawn K1-R procedure and the
copied-history procedure are not revived. No spatial or 3D import is added.

## 1. Components (U0, GeoCeDG Desktop)

| Component | Contract |
|---|---|
| `GeoCeDGUndoManager extends UndoManagerD` (proposed) | detach/attach of original commands; conservative pending-store accounting (§4); overrides delegate unchanged |
| `AppGeoCeDG.getUndoManager(Construction)` override | **stateless**: constructs the manager from `cons` only; may run during the super-constructor (G6) |
| orchestration (`GeoCeDGDocumentInsertion`, proposed) | barrier transaction (§2) |
| native recovery snapshot holder (proposed) | captured bytes, provenance, ownership (§3) |
| isolation controller (proposed) | modal isolation, guards, exits (§5) |

## 2. Transaction

```text
0 BARRIER BLOCK  (EDT; synchronized on the undo manager; nothing mutated before it ends)
  a  accounting gate: pending() == 0 and not degraded; else release the monitor, bounded
     wait, re-enter; still not quiescent -> REJECT BEFORE MUTATION
  b  native recovery snapshot: writeGeoGebraFile(OutputStream, false) into memory;
     DocumentArchivePreflight true; archive UUID equals live UUID; provenance recorded
     -> any failure: REJECT BEFORE MUTATION
  c  capture UUID, tubeId, saved flag, macro digest set
  d  held := detach()   (original command objects and cursor; never copied)
  e  prepare + commit baseline (generation advanced; every earlier store cancelled);
     baselineState := getCurrentUndoInfo() captured BY REFERENCE here (G4);
     baseline archive preflight; failure -> attach(held), REJECT
1 COMMIT, 2 IDENTIFY own store (generation + content, bounded), 3 STRICT VERIFY
4 SUCCESS        restore UUID; ATTACH BLOCK with the held prefix; one orchestration store;
                 held Redo originals deleted as for any new action; release snapshot
5 K1-S           subtractive removal, transferred macros, UUID; ATTACH BLOCK; saved flag;
                 certify; removal failure -> 6
6 RELOAD         guarded reload of baselineState (readZipFromMemory, no history access,
                 no inherited fallback, one retry); ATTACH BLOCK; saved flag; certify
7 SERIOUS        ATTACH BLOCK (or held kept if attach is unsafe); snapshot ownership to the
                 isolation controller; isolation (§5)
ATTACH BLOCK     synchronized: pending() == 0, not degraded, and the temporary list is
                 exactly [baseline (+ own store)]; else a closing barrier (throw-away
                 baseline) first (G3); attach installs first, refreshes actions
                 best-effort (log, never throw) (G7); re-checked during certification
FINALLY          attach if still detached; delete the barrier baseline and own store files;
                 the native snapshot is NOT released here; a failure in finally is added
                 as suppressed to the original failure, never replaces it (G7)
```

Certificate: full XML (root UUID included), identity registry, construction
fingerprint, **macro definition digests** (N4), history list and cursor with
per-entry labels and grouping (by object identity), saved flag, file
association; runtime state compared separately; declared resets listed.

## 3. Native recovery snapshot (G1)

Lifecycle `CAPTURE REQUESTED → CAPTURE COMPLETE → NATIVE ARCHIVE VALIDATED →
VERIFIED SNAPSHOT HELD → INSERTION ATTEMPT → SUCCESS / CERTIFIED RECOVERY /
UNCERTIFIED FAILURE → RELEASE or RETAIN`:

| Outcome | Snapshot |
|---|---|
| capture or validation failed | operation rejected before mutation; bytes discarded |
| success | released after certification |
| certified recovery | released after certification |
| uncertified failure | ownership moves to the isolation controller; released only after the user's exit completes |

Never the undo archive; never deleted by an unconditional cleanup; never
written over the current file. Recovery copy: user-chosen new path, refusal
for the current file, explicit confirmation for another existing file, atomic
publication from the held bytes, published bytes re-validated, original UUID
kept, session file association unchanged.

## 4. Pending-store accounting C1 (G2)

| Element | Rule |
|---|---|
| state (under the monitor) | `pending`, `degraded`, `epoch` |
| registration | `storeUndoInfo(StringBuilder)`: `pending++` then `super`; `super` throws → `degraded` |
| synchronous marker | thread-local marker set by the `storeAndNotifyUnsaved` and `commitUndoBaseline` overrides around `super` |
| success flag | `pruneStateList` without the marker → thread-local success flag on the store thread |
| `onStoreUndo` | marker → no accounting; success flag → `pending--` (0 → `degraded`); neither → `degraded`, no decrement; clear thread-locals |
| `commitUndoBaseline` | after `super`: `pending = 0`, `degraded = false`, `epoch++` |
| other `Error` | no callback: `pending` stays high |
| use | gate and attach block require `pending == 0 && !degraded`; else reject (gate) or closing barrier (attach) |
| re-establishment | the next native open / New baseline; never a history-discarding baseline by F3 |

Proof obligations and alternatives C2 (owned store body) / C3 (shared fix):
[audit reconciliation report](../validation/pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md) §6.

## 5. Isolation (G5, NE2)

Entered only from step 7. Guards: modal dialog; GeoCeDG isolation flag
refusing Save, Save As, Export, Insert File, Apply Styles, clipboard and any
save of the live state; update scripts blocked and global listeners
suppressed; animation stopped; Undo/Redo suspended; file drop and in-process
API mutation refused. Exits (no inherited save prompt): **Save verified copy**
(§3), **Reopen** (re-enable Undo first, then native open of the existing file),
**Close** (dispose without prompt). No export of the uncertified state. The
flag is cleared only after the exit completes.

## 6. Tests added to the design's list

`T-F3R-NATIVE-SNAPSHOT` (capture, validation, provenance, reopen of the saved
copy with macros, images, defaults, hidden layers, UUID), `T-F3R-ACCOUNTING`
(success, stale discard, stale and current exception exits, `Error` exit,
thread-creation failure, delayed notification, synchronous callers),
`T-F3R-ATTACH` (late operation-generation store after attach, closing barrier,
injected `updateActions` failure, suppressed exceptions), `T-F3R-SOURCE-CAPTURE`
(`ER-F3R-1` with an identity payload after the paste store lands),
`T-F3R-ISOLATION-ROUTES` and `T-F3R-ISOLATION-EXITS` (no prompt, no overwrite,
Undo re-enabled before reopen), `T-F3R-FACTORY` (startup construction order),
`T-F3R-EDT` (barrier, bounded wait and reload on the EDT); plus
`T-F3R-HISTORY-METADATA` and `T-F3R-HISTORY-OWNERSHIP` with the
factory-installed subclass.

# PRE-G9B-R6-plus-F3 — Undo history semantic integrity report

- Activity: `PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE` (author
  instruction of 2026-10-12; final focused characterization before author
  disposition)
- Status: **CHARACTERIZATION COMPLETE — FINAL TECHNICAL GATE: READY FOR AUTHOR
  DISPOSITION** (not implementation-ready, not verified in product, not author
  approved)
- Base: `T_F3_RECOVERY_CORRECTIVE` `6e41bf4426b9bdcb137f42e783038b02827ff3bd`,
  tree `8b614863de45a7fccb93ec751871498c8ee7bbd1`, parent
  `6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6` (STATIC
  `verification-f633b1d5d5c64363a626ebb14ae9fb90` `ACCEPTED / COMPLETE`)
- Companions: [final option assessment](pre_g9b_r6_plus_f3_dq21_final_option_assessment.md);
  [risk update](pre_g9b_r6_plus_f3_risk_register_history_integrity_update.md);
  [Final F3 Author Decision Sheet](pre_g9b_r6_plus_f3_final_author_decision_sheet.md);
  [recovery design addendum](../architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_candidate.md)
- Earlier evidence (unchanged): [corrective feasibility report](pre_g9b_r6_plus_f3_dq21_corrective_feasibility_report.md),
  [recovery safety report](pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
- `selfApproved = false`

No product code, permanent test, specification, ADR or author decision was
changed. Probes ran from the scratchpad; build traces were removed (§8).

## 1. Entry gate (2026-10-12)

| Check | Result |
|---|---|
| HEAD, tree, parent | `6e41bf44…`, `8b614863…`, `6c07e01a…` |
| linear chain | `e70718fe` → `480c9729` → `0eb50311` → `8b41af11` → `3e24ed3a` → `6c07e01a` → `6e41bf44` |
| `main`, `origin/main`, live remote `main` | `e70718fe` (local candidates intentionally unpublished) |
| receipt | `verification-f633b1d5…` `ACCEPTED / COMPLETE`, `COMPLETED`, bound to `6e41bf44`/`8b614863` |
| worktree | clean |
| product delta since `main` (`source/`, `apps/`, `tools/`, `geocedg/specs/`, `docs/adr/`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/`) | none |
| corrective report, corrected assessment, decision sheet v4.2, corrective matrix | present, unchanged |
| branch | `phase/pre-g9b-r6-plus-f3-undo-history-semantic-integrity` (local, from `6e41bf44`) |
| result | **PASS** |

## 2. Source-level findings

| Id | Fact | Source |
|---|---|---|
| `HS-1` | `UndoCommand(UndoCommand)` copies `appState`, `action`, `args`, `slideID`, `undoArgs`, `undoAction`; it does **not** copy `labels` (public field) or `stitchToNext` (protected field); `appState` and the argument arrays are shared references | `UndoCommand.java:31-59` |
| `HS-2` | `undoHistoryTo` replaces every entry by such a copy and clears the list; `undoHistoryFrom` installs the given command objects **as they are** (no copy) with the given cursor | `UndoManager.java:606-631` |
| `HS-3` | `labels` drives `removeActionsWithLabel` (`hasLabel`), reached through `GgbAPI.deleteObject` (scripts and API); `stitchToNext` drives grouped `undo`/`redo` | `UndoManager.java:157-185,667-683`; `GgbAPI.java:968-976` |
| `HS-4` | labeled commands are produced by ordinary GeoCeDG Desktop editing: every Move-tool drag stores an `UPDATE` action with labels through `UpdateActionStore` (`storeSelection` on drag, `storeUndo` on release); also non-identity `storeAddGeo`, pen updates, style/order/content/deletion stores | `EuclidianController.java:520,7609-7613,10430-10441`; `UpdateActionStore.java:59-150`; `UndoManager.java:502-514` |
| `HS-5` | `stitchToNext` is set only by `UpdateActionStore` with stitching (stroke split, delete-drag over strokes) and by the stroke-split action; whether the GeoCeDG profile exposes these routes was not established, so stitched commands are treated as admissible history content | `EuclidianController.java:7972,7988-7996`; `ModeDeleteLocus.java:223` |
| `HS-6` | consequence: after ordinary moves the history head is an **action** command with no snapshot; the content gate of the corrective design (head archive equals live XML) is undefined for such histories | `UndoCommand.java:110-174` |
| `HS-7` | a GeoCeDG subclass of `UndoManagerD` can read `undoInfoList` and `app` (protected), override `storeUndoInfo(StringBuilder)`, `storeAndNotifyUnsaved`, `onStoreUndo` and `commitUndoBaseline`; `AppD.getUndoManager(Construction)` is the existing, overridable factory used lazily by `Construction` | `UndoManager.java:55-62,190-194,457-462`; `UndoManagerD.java:57-139`; `AppD.java:4383-4385`; `Construction.java:143,4290-4300` |
| `HS-8` | the only lock on the store path is the manager monitor; store threads do not wait for the EDT (no `invokeAndWait` outside startup), so EDT + monitor cannot form a lock cycle; store threads call `app.updateActions` off the EDT (inherited) | `UndoManagerD.java:58-101`; `GeoGebra.java:133` |
| `HS-9` | `getCurrentUndoInfo()` is public and returns the current `AppState`; after a barrier it is the barrier baseline, usable as the recovery source independently of the history head kind | `UndoManager.java:215-219` |

## 3. Focus A — Undo metadata (`f3-probe.history-a.txt`)

Fixture ("mixed history"): baseline; checkpoint `H1`; checkpoint `G`; labeled
move of `H1`; stitched move of `H1`; labeled move of `G` (group of two);
checkpoint `H2`; one Undo (Redo = `H2`); Locus V2 in the target. The moves use
the production `UpdateActionStore`.

| Probe | Result |
|---|---|
| H1 field fidelity | every action copy lost `labels` (`[H1]`, `[H1]`, `[G]` → `null`) and `stitchToNext` (`true` → `false`); `appState` and `args` shared |
| H2 label behavior | control: `deleteObject(G)` removes `G`'s action (cursor 6 → 5), later Undo leaves 14 objects without `G`; after a plain `undoHistoryTo/From`: nothing removed (cursor stays 6), later Undo leaves 15 objects **with `G`** — a different outcome for the same user operation |
| H3 grouping | control: 4 user-visible Undo steps to the baseline (the stitched pair undone together); after transfer: **5 steps** (group split) |
| H4 mixed, failed Locus V2 insertion, recovery with the corrective design (`undoHistoryTo` copies) | signature differs (metadata lost), walk differs (extra position 5), delete behavior differs; history files present; insertion does not reappear |
| H4 mixed, recovery with detach/attach of the **original** commands (scratch subclass) | signature, walk and delete behavior **identical** to the control |
| H5 success, detach/attach | previous prefix kept with its labels and stitch; insertion exactly one undo step; held Redo discarded as for any new action; UUID kept |

Observable consequence: the corrective design's barrier (`ER-F3R-3`) silently
changes grouping and label-associated behavior of the restored history for
**both** K1-S and K1-R-H, since both take the history aside the same way.
Earlier tests did not see it because their histories contained checkpoints
only.

## 4. Focus B — Snapshot ownership (`f3-probe.history-b.txt`)

| Probe | Inherited copy | Detach/attach of originals |
|---|---|---|
| O1 validity after transfer | every snapshot file present and loadable (walk) | same |
| O2 cleanup | only the barrier baseline and the paste store are deleted; no referenced file deleted | same |
| O3 four failed insertions on one document | files stable; baseline deleted each cycle; metadata lost once, then stable (walk differs) | files, signature and walk identical every cycle |
| O4 interruption after detach | — | live history empty while held; reattach in `finally` restores it identically |

Ownership: with the inherited copy, original and copy briefly share each
`FileAppState`; the originals are dropped by `clearUndoInfo` without deleting
files, so the copies become the only owners — safe only under the invariant
that one history list is active at a time and that nothing else deletes the
originals. With detach/attach there is no copy: ownership simply moves out and
back. Discarded pending stores delete their own file (`UndoManagerD.java:83-85`);
remaining undo files are `deleteOnExit`.

Classification: inherited copy — **SAFE WITH EXPLICIT RESTRICTION** for files,
**UNSAFE** for metadata; detach/attach — **SAFE — SOURCE AND RUNTIME
EVIDENCE**, with the obligation that the held originals are reinstated in a
`finally` block.

## 5. Focus C — Correction

| Option | Assessment |
|---|---|
| C1 no correction | **rejected**: labeled commands arise from ordinary Move drags (`HS-4`); loss is observable (H2, H3, H4) |
| C2 Desktop-owned | **recommended**: a GeoCeDG subclass of `UndoManagerD` installed by an `AppGeoCeDG.getUndoManager(Construction)` override, adding (a) `detach()` / `attach()` that move the original command objects out and back (no copy; `undoHistoryFrom` installs them as they are) and (b) a pending-store tracker (count in `storeUndoInfo`, completion in `onStoreUndo` on store threads, reset in `commitUndoBaseline`) that replaces the content gate for every head kind; no inherited or shared source change; Classic unaffected; prototype validated (H4, H5, O3, O4, P1–P4) |
| C3 minimal shared correction | copy `labels` (and `stitchToNext`) in the `UndoCommand` copy constructor: fixes fidelity, keeps reference sharing of snapshots, also changes Web sub-app history switching (`AppWFull`, `UndoManagerW`); shared change → author authorization and reclassification review (likely `GLOBAL_IMPACT`); does not solve the action-head gate; not needed given C2 |
| C4 restricted admission | reject when the history contains labeled or stitched commands: checkable, but rejects insertion after any Move drag — unacceptable coverage loss; not recommended |

Pending-store tracker (P-probes): pending = 1 while a store is held, 0 after it
lands; synchronous action stores do not count; a store discarded by the barrier
is reset to 0 and stays 0; steady state 0 after several stores. The insertion
gate becomes "no pending store", with a bounded wait outside the monitor, else
rejection before mutation. The recovery source becomes the barrier baseline
(`HS-9`), which exists for every head kind.

Verification impact: the subclass is installed for every GeoCeDG document, so
ordinary GeoCeDG Undo runs through it (bookkeeping only). The frozen plan must
include integration coverage of the existing Desktop undo, lifecycle and
G9A/G9U identity suites; `INTEGRATED_PHASE` remains appropriate with that
explicit coverage; C3 would require a classification review.

## 6. Focus D — Implementation gates

- **D1 EDT.** No lock cycle at source level (`HS-8`). The barrier block holds
  the monitor while serializing the baseline (bounded by document size); the
  tracker wait must happen outside the monitor with a bounded timeout. EDT
  execution was **not** run in this activity and remains a mandatory
  implementation gate.
- **D2 containment.** The previous proposal (Undo suspension + save guard +
  insertion lock + editing allowed with a warning) does not cover all mutation
  routes (scripts, timers, animation, clipboard, exports). Smallest safe
  alternative, recommended: **modal isolation** of the uncertified drawing —
  block update scripts and stop animation, suspend Undo/Redo, and present a
  modal serious-failure dialog whose only exits are (1) save the last verified
  state (the barrier baseline archive, which passes the native preflight) to a
  new file, (2) reopen the document from its file, (3) close it, and optionally
  save a copy of the uncertified state explicitly labelled as such; the
  uncertified state is never offered for normal editing, saving or export. The
  building blocks exist (`setBlockUpdateScripts`, animation stop,
  `Kernel.setUndoActive`, file copy, preflight, ordinary open); the dialog and
  wiring do not; runtime validation is an implementation gate.
- **D3 UUID.** Unchanged: successful insertion keeps the destination UUID (H5
  confirms it with a mixed history).
- **D4 K1-S.** Unchanged except that its history take-out must also use C2.

## 7. Final technical gate

All eight criteria of the task's acceptance gate are met: the copy concern is
resolved by evidence; labels and grouping are preserved by a precise,
prototyped Desktop correction; snapshot ownership is characterized; no
accepted path destroys history (C2 + `finally`); the remaining corrections are
scoped to GeoCeDG Desktop; EDT and containment have testable gates; no new
unresolved safety risk blocks an informed decision.

**READY FOR AUTHOR DISPOSITION.**

## 8. Limitations and probe record

- The subclass was installed by reflection into `Construction.undoManager`
  after app creation; the product would install it at creation through the
  factory, so app-level undo listeners attach to it — an implementation test
  must confirm.
- The tracker distinguishes store threads by exclusion of known caller threads;
  in the product all callers are the EDT; this must be re-verified on the EDT.
- Stitched commands were produced with the production `UpdateActionStore`; the
  stroke-split UI route itself was not exercised.
- No EDT run, no containment prototype.

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.history-a.txt` | H1–H5 | `57aca043c31082a8890dcf35e6877a9ef0cbbd07b06848d0ecf51a3e909cbe38` |
| `f3-probe.history-b.txt` | O3, O4, P1–P4 | `140b16b4c8a9c77b967680ab0d9f32bb07ae5a0c62ce099873156ebf2a150fca` |
| probe source | `F3HistoryIntegrityProbeTest.java` (570 lines; reuses scratch helpers) | `e259f51a74d51ca28319b5855402446468d3fdc0f73f479a425a08390392ba43` |

Runs: `focusA`, `focusB` (`:desktop:desktop:test --tests …F3HistoryIntegrityProbeTest.<method>`
with the scratch init script, exit 0). Clean-up: `:desktop:desktop:compileTestJava`
without the init script (exit 0); 24 probe class, stash and JUnit XML files
removed; `git status` clean. Source revision of every probe: `6e41bf44`.

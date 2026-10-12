# PRE-G9B-R6-plus-F3 — DQ-F3-21 final option assessment

- Activity: `PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE` (author
  instruction of 2026-10-12)
- Status: **ASSESSMENT — RECOMMENDATION ONLY — PENDING AUTHOR DISPOSITION**
- Base: `T_F3_RECOVERY_CORRECTIVE` `6e41bf4426b9bdcb137f42e783038b02827ff3bd`
- Successor of (unchanged, history): the
  [corrected option assessment](pre_g9b_r6_plus_f3_dq21_corrected_option_assessment.md)
  (`6e41bf44`), the [recovery option assessment](pre_g9b_r6_plus_f3_dq21_recovery_option_assessment.md)
  (`6c07e01a`) and the [DQ-F3-21 option assessment](pre_g9b_r6_plus_f3_dq21_option_assessment.md)
  (`3e24ed3a`)
- Evidence: [Undo history semantic integrity report](pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md)
- `selfApproved = false`

## 1. History of findings (kept)

| Id | Finding | Status |
|---|---|---|
| `ER-F3R-1` | immediate restore lets a failed insertion reappear | valid |
| `ER-F3R-2` | the paste store prunes pre-existing Redo; original K1-R withdrawn | valid |
| generation barrier | cancels every pre-operation store | valid (source guarantee) |
| `ER-F3R-3` (new) | taking the history aside with `undoHistoryTo` loses `labels` and `stitchToNext` of action commands (both K1-S and K1-R-H); the content gate is undefined when the head is an action | corrected by the Desktop undo-manager subclass (C2) |

## 2. Recommended procedure (replaces step 0 and the history handling of the corrected assessment §2)

```text
0 GATE        GeoCeDG undo manager reports no pending store (bounded wait outside
              the monitor, else REJECT BEFORE MUTATION)
  BARRIER     in one block synchronized on the undo manager: detach the original
              history (no copy); prepare + commit a baseline (generation advanced);
              the baseline archive (getCurrentUndoInfo) must pass the native
              preflight, else re-attach and REJECT; capture UUID, saved flag, macros
1-3           commit, own-store identification, strict verification (unchanged)
4 SUCCESS     restore UUID; attach the original prefix up to the cursor; one
              orchestration store; held Redo originals deleted as for any new action
5 K1-S        subtractive removal, macros, UUID; attach originals; saved flag; certify
6 RELOAD      guarded reload of the barrier baseline archive (no history access,
              one retry); attach originals; saved flag; certify
7 SERIOUS     attach originals unchanged; modal isolation (report §6 D2)
ALWAYS        attach in a finally block; delete the barrier baseline and the
              operation's own store
```

## 3. Decision table

| Alternative | Assessment |
|---|---|
| K1-S | certified for checkpoint and mixed histories when the history is moved, not copied; instances, F2 memory, IsoABorder link kept |
| K1-R-H | certified with complete history semantics (labels, grouping, Redo, cursor, snapshots) under C2; declared session reset |
| S | rejection before mutation for a pending store, an unloadable baseline, inadmissible payloads; payload-class fallback |
| K2 / C3 (shared copy-constructor fidelity) | conditional alternative only; not required; would need author authorization and a reclassification review |
| C4 (reject histories with labeled/stitched commands) | not recommended (rejects after any Move drag) |
| architecture | GeoCeDG Desktop only: undo-manager subclass + `AppGeoCeDG` factory override + orchestration; U0 (no inherited source change); Classic unaffected |
| class | `INTEGRATED_PHASE`, with integration coverage of the existing Desktop undo, document-lifecycle and identity suites named in the frozen plan |

## 4. Proposed normative wording (supersedes the corrected assessment's wording)

> An insertion starts only when no undo state is still being recorded and the
> drawing's current state can be reloaded; otherwise it is rejected and nothing
> changes. While it runs, the drawing's Undo and Redo history is kept aside
> unchanged — every step, its grouping and its references. It is accepted only
> when admission, the dry run, the commit and the strict verification all
> succeed; it is then exactly one undo step, previous steps keep their
> behavior, and the drawing keeps its document identity. A failure after the
> commit restores the drawing exactly — construction, durable identities,
> document identity, units, layers, macros, saved state and the complete Undo
> and Redo history with its grouping. When no identity-bearing object was
> created, live objects, the remembered references of Parallel/Perpendicular
> and the IsoABorder export link are kept; otherwise the drawing is reloaded and
> those, with the selection, are reset as after an Undo, and the user is told
> so. If the restoration fails or cannot be verified, GeoCeDG isolates the
> drawing: it stops scripts and animation, suspends Undo and Redo, and offers
> only to save the last verified state to a new file, to reopen the document or
> to close it; it never reports the case as an ordinary rejection and never
> lets the unverified state be edited or saved as if it were verified.

## 5. Implementation gates (replace gates 1–7 of the corrected assessment)

1. GeoCeDG undo-manager subclass installed through the factory for every
   GeoCeDG document; ordinary Undo behavior unchanged (existing Desktop undo,
   lifecycle and identity suites as integration coverage).
2. Detach/attach of original commands; tests H2, H3, H4, H5, O3, O4 (labels,
   grouping, delete behavior, files, interruption).
3. Pending-store tracker and bounded gate; tests P1–P4 plus a pending store under
   an action head; on the EDT.
4. Barrier with baseline preflight; A-Q1, A-Q3, A-Q4 on the EDT.
5. Guarded reload of the barrier baseline; B-F2–B-F5.
6. Success path: UUID kept, one undo step, prefix metadata kept; four payload
   classes, Save, Save As, reopen.
7. Modal isolation on serious failure: scripts and animation stopped, Undo/Redo
   suspended, only the three exits; no export or ordinary save of the
   unverified state.
8. Regression tests for `ER-F3R-1`, `ER-F3R-2`, `ER-F3R-3`.

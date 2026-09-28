# PRE-G9B-R3-C1 validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Each row maps an obligation of the author's `PRE-G9B-R3-C1` instruction to its
executable evidence and to the behaviour on the base `89d481b1…`. The
[design record](../architecture/pre_g9b_r3_c1_clipboard_atomicity_and_closure_design.md)
gives the mechanism. Base behaviour comes from scratch probes run on the exact base
(see the [candidate report](pre_g9b_r3_c1_candidate_report.md) §3). Every new test
was also run on an extracted copy of the base tree, where all of them fail except
the control B.

```text
CLIP = org.geocedg.common.locus.PreG9bR3C1ClipboardCorrectnessTest
DESK = org.geocedg.desktop.PreG9bR3C1DesktopClipboardTest
```

## 1. Required tests

| # | Obligation | Evidence | Base behaviour |
|---|---|---|---|
| A | axis-input witness: copying only `a` copies the complete closure and pastes dependent geometry | `CLIP#anAxisInputPredecessorIsCopiedAndTheLocusPastesDependent` | `f` renamed but not serialized; paste throws |
| B | control on free lines copies the same construction as before | `CLIP#aFreeLineControlStillCopiesTheSameConstruction` | already correct; unchanged |
| C | real Desktop Ctrl+C / Ctrl+V | `DESK#ctrlCAndCtrlVCopyTheAxisWitnessWithItsCompleteClosure` (key events through `GlobalKeyDispatcherD`) | paste throws; scripts stay blocked |
| D | fresh identities; internal references remapped; immediate copy lineage | `CLIP#anAxisInputPredecessorIsCopiedAndTheLocusPastesDependent`, `CLIP#aFreeLineControlStillCopiesTheSameConstruction`, `DESK#ctrlCAndCtrlVCopyTheAxisWitnessWithItsCompleteClosure` | no copy |
| E | no silent free-geo degradation; an unrepresentable closure is rejected atomically | `CLIP#anAxisDependentHelperOfTheClosureStaysDependent`, `CLIP#aPayloadThatWouldDegradeAHelperIsRejectedInsteadOfPastedFree` | helper `M` pasted as free geometry; paste reports success |
| F | failure after mutation began: exact rollback | `CLIP#aHostMutationBeforeAFailedImportIsRolledBack`, `CLIP#anUnrebuildableIdentityPayloadIsRejectedAndRestoresEverything`, `DESK#aFailedInsertAfterTheOverwriteRestoresTheOverwrittenObject` | outer rollback throws; host flags leak |
| G | script policy `false` stays `false`; save and reopen keep it `false` | `CLIP#anUnrebuildableIdentityPayloadIsRejectedAndRestoresEverything` | becomes `true`, saved as `<scripting blocked="true">`, kept on reopen |
| H | script policy `true` stays `true` | `CLIP#scriptsAlreadyBlockedStayBlockedAfterARejectedPaste` | `true` only by the leak; kernel flags still leak |
| I | a secondary rollback failure keeps the originating cause | `CLIP#aRollbackFailureKeepsTheImportFailureAsSuppressedCause` | original failure absent from the chain |
| J | save and reopen after a successful paste keep identities, lineage and dependencies | `CLIP#anAxisInputPredecessorIsCopiedAndTheLocusPastesDependent` (fresh application) | no successful paste |
| K | successful paste is one undo step and undo/redo restore it; a failed paste adds none | `CLIP#aSuccessfulPasteIsOneUndoStepThatUndoAndRedoRestoreExactly`, undo fields of every rejection test, `DESK#ctrlCAndCtrlVCopyTheAxisWitnessWithItsCompleteClosure`, `DESK#aFailedInsertAfterTheOverwriteRestoresTheOverwrittenObject` | failed paste adds none |
| L | related degradation of the same cause | `CLIP#anAxisDependentHelperOfTheClosureStaysDependent`, `CLIP#aPayloadThatWouldDegradeAHelperIsRejectedInsteadOfPastedFree` | degraded silently |
| M | Insert File with overwrite shares the atomic import | `DESK#aFailedInsertAfterTheOverwriteRestoresTheOverwrittenObject` | shares the defective outer rollback (same method) |

## 2. Invariants

| Invariant | Evidence |
|---|---|
| 8.1 failure atomicity: construction, order, IDs, records, DAG, spatial section, ledgers, undo, script policy, kernel loading mode, view notification, command lookup, file loading | full application XML, spatial section, label-to-ID map, undo history size and flags, script policy, kernel and construction flags compared before and after, in every rejection test (`HostState`) |
| 8.1 save/reopen after a failure yields the original state | `CLIP#anUnrebuildableIdentityPayloadIsRejectedAndRestoresEverything` |
| 8.2 rollback enters and leaves the runtime protocol; no guard weakened | F, G and H pass with the unchanged `SpatialSemanticRuntime` checks |
| 8.3 exception fidelity | I |
| 8.4 successful copy is complete, fresh, remapped, deterministic and persistent | A, B, D, J, K; A also checks that two copies produce the same payload |
| 8.5 constants gain no identity and are not copied | A and C: axes keep no durable ID; commands reference `xAxis`/`yAxis` by name |
| 8.6 fail closed instead of degrading | E |

## 3. Preserved regressions

The clipboard-, macro- and lifecycle-related suites and the complete shared and
Desktop suites were run on the candidate tree; see the candidate report §8.

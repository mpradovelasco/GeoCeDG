# PRE-G9B-R3-C1-U1 validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Each row maps an obligation of the author's `PRE-G9B-R3-C1-U1` instruction to its
executable evidence and to the behaviour on the base `68169dc0…`. The
[design record](../architecture/pre_g9b_r3_c1_u1_ordinary_clipboard_closure_design.md)
gives the mechanism. Every new test was also run on an extracted copy of the base
tree; only the two control tests pass there.

```text
BASE  = org.geocedg.common.spatial.PreG9bR3C1U1OrdinaryClosureTestBase
PLAIN = org.geocedg.common.spatial.PreG9bR3C1U1PlainClipboardClosureTest   (default upstream-style app)
CEDG  = org.geocedg.common.spatial.PreG9bR3C1U1CedgClipboardClosureTest    (GeoCeDG app, no participants)
DESK  = org.geocedg.desktop.PreG9bR3C1U1DesktopClipboardTest
```

`BASE` methods run in both `PLAIN` and `CEDG`.

## 1. Required tests

| # | Obligation | Evidence | Base behaviour |
|---|---|---|---|
| A | reproducer: source and pasted geo dependent, same parent algorithm semantics | `BASE#aParentAlgorithmWithAnAxisInputIsCopiedAndThePastedGeoStaysDependent`, `BASE#aParentAlgorithmWithTwoOutputsAndAnAxisInputIsCompleted` (in `PLAIN` and `CEDG`) | `f`, `D` renamed but not serialized; pasted `M` is free |
| B | source and pasted objects stay non-participating; no identity section or attribute | `BASE#noGeoInvolvedIsAnIdentityParticipant` | fails only because the paste is incomplete |
| C | control without host constants: existing closure unchanged | `BASE#aControlWithoutHostConstantsCopiesTheSameConstruction`, `BASE#theClosureAddsParentsOfCopiedGeosOnlyAndNeverTheirDependents` | passes on base and candidate (unchanged) |
| D | dependency dynamics: the pasted dependent follows its pasted predecessor and not the source | `BASE#aPastedDependentFollowsItsPastedPredecessorAndNotTheSource` | no dependent pasted |
| E | undo/redo: one coherent host operation | `BASE#aSuccessfulPasteIsOneUndoStepAndUndoAndRedoKeepTheDependency` | no dependent pasted |
| F | save and reopen keeps the ordinary dependency and adds no identity | `BASE#savingAndReopeningKeepsTheOrdinaryDependencyWithoutIdentity` (fresh application of each configuration) | no dependent pasted |
| G | same-window paste | `DESK#ctrlCAndCtrlVUseTheSameWindowFastPasteAndKeepTheDependency` (the undo state is unchanged between copy and paste, which selects the fast payload) and `DESK#ctrlVAfterAnotherUndoPointUsesTheFullPayloadAndKeepsTheDependency` (a later undo point selects the full payload) | both fail |
| H | real Desktop Ctrl+C / Ctrl+V | both `DESK` tests use key events through `GlobalKeyDispatcherD` | both fail |
| I | generic shared path independent of Desktop | `PLAIN` and `CEDG` through `InternalClipboard` | fail |
| J | identity-bearing regression | `PreG9bR3C1ClipboardCorrectnessTest` (9) and `PreG9bR3C1DesktopClipboardTest` (2), unchanged and passing | not applicable |
| K | macro and group regression | the existing suites `InternalClipboardTest`, `CopyPasteTest`, `GroupTest`, `G9U1MacroNativeArchivePersistenceTest`, `G9U1UserToolLibraryTest` and the other clipboard and macro suites, unchanged and passing (see the candidate report) | not applicable |
| L | non-reconstructible input | `BASE#theCompletionNeverReferencesAnObjectThatIsNotCopied` (helper unit test; the guard never adds an algorithm whose non-constant inputs are not copied, and completion is idempotent) | helper absent |
| M | base-versus-candidate causality | all tests run on an extracted copy of `68169dc0…`: 6 of 8 shared tests fail in each configuration and both `DESK` tests fail; the two controls pass | see report |

## 2. Invariants

| Invariant | Evidence |
|---|---|
| dependent before copy never becomes free after paste because the closure omitted its algorithm | A, D, E, F, G, H |
| constants and axes are referenced, never copied, and gain no identity | A (inputs are the construction's own `xAxis`/`yAxis`), B |
| no ordinary geo becomes an identity participant; no spatial record is synthesized | B, F, `DESK` (registry empty) |
| no over-expansion | C (`copy P` yields exactly `P`) |
| C1 identity-bearing behaviour is unchanged | J |
| no serialization, schema or migration change | F; no `.cedg` schema file touched |

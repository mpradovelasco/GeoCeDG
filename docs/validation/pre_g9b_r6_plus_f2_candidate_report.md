# PRE-G9B-R6-plus-F2 — authoring-memory refinements: technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F2
AUTHORIZATION             = AUTHOR AUTHORIZED — BOUNDED IMPLEMENTATION AND TECHNICAL
                            VERIFICATION (2026-10-11; F2 author-decision record v1.1 §8)
IMPLEMENTATION BASE       = 6d9bafb04235a71c8dfadef9631cd6057964b1f9
                            tree 6bc65715cd37f5872b804eea7949d2f9fea96092
AUTHORIZATION COMMIT      = bdb9b6a94cf95ccebd1beb2f3f31f2bab523c75a
                            tree 674fcfd479e377f70f511ddd22eb95136add0252
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen at the entry gate, before the first
                            productive edit)
REGISTERED PHASE          = PRE-G9B-R6-PLUS-F2
TECHNICAL CANDIDATE       = the commit that contains this report (frozen); its identity
                            and the acceptance runs are reported outside this file
AUTHOR_SMOKE              = PENDING (required)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false; authorApproved = false; passClaimed = false
```

Machine-readable mirror:
[`pre-g9b-r6-plus-f2-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-candidate-evidence.json).
Contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-authoring-memory.prompt.md)
(`AUTHORIZED FOR IMPLEMENTATION`); decisions: [F2 author-decision record](pre_g9b_r6_plus_f2_author_decisions_record.md)
v1.1; design: [reconciled design](../architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); specification:
[`authoring-memory.md`](../../geocedg/specs/ui/authoring-memory.md) (`PROPOSED`).

## 1. Entry gate

| Check | Result |
|---|---|
| local `main`, `origin/main`, live remote `main` | `6d9bafb0` (all three) |
| worktree | clean |
| frozen preparation evidence | unchanged since `09bb6302` |
| existing F2 code | none |
| branch | `phase/pre-g9b-r6-plus-f2` (local), from `6d9bafb0` |
| first tracked change | `bdb9b6a9`: prompt `AUTHORIZED FOR IMPLEMENTATION`, record v1.1 §8 (preparation publication, authorization, entry gate, `OTQ-F2-1` to `OTQ-F2-3` resolved, linear-function domain, class frozen), roadmap 5.17, mini-track status |

The entry-gate resolutions (record §8.1–§8.4) are the authority for the three
open items and the function domain; this report applies them.

## 2. Implementation

| Element | Change |
|---|---|
| `GeoCeDGLineReferenceMemory` (new) | two independent slots (`PARALLEL`, `PERPENDICULAR`); admissibility (non-3D `GeoLine`, non-3D `GeoVector`, non-3D `GeoFunction` with finite `getX()`); currentness by exact-instance membership in `getGeoSetConstructionOrder()`; usability = admissible ∧ defined ∧ Euclidian-visible ∧ visible in GV1 ∧ layer shown; states `EMPTY`/`HELD`/`SUSPENDED` (a non-current value is cleared when read); a client `EventListener` registered on the application `EventDispatcher` at the first slot write: `reset()`, `UNDO`, `REDO` clear both slots, `REMOVE`, `REDEFINE` clear non-current slots |
| `GeoCeDGControllerCompanion` (new) | subclass of `EuclidianControllerFor3DCompanion`; `createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)` calls `super`, then — only while the controller is in `MODE_ANGLE_FIXED` — `setEuclidianVisible(false)` and `updateRepaint()` on the returned angle |
| `GeoCeDGEuclidianController` (+108 lines, additive) | memory field (not reset in `setMode`); `newCompanion()` returns the GeoCeDG companion; in `switchModeForProcessMode`, for `MODE_PARALLEL` and `MODE_ORTHOGONAL` only, `processLineReferenceTool`: release-time injection (non-preview; empty point, line, vector and function selections; hits contain a point; slot `HELD`) through `selection.addToSelectionList`, the inherited `super.switchModeForProcessMode`, withdrawal of an injected reference that is still selected, and the success certificate (exactly one just-created object; defined; exact-instance member; parent algorithm exactly the tool's line or vector algorithm; `input[1]` admissible) |
| unchanged | `EuclidianController.parallel`/`orthogonal`, the press path and issue #2610 handling, double click, preview, hit priority, `DialogManager`, `AlgoDispatcher.angle`, every command and algorithm, XML, undo manager, `source/shared/**`, Graphics View 2 controller, Classic |

No inherited class, kernel, command, algorithm, XML or identity change was
needed; no stop condition was reached.

## 3. Tests and evidence

`PreG9BR6PlusF2AuthoringMemoryTest` — 19 tests, 0 failures. Gestures drive the
real Graphics View 1 controller with `MouseEventD` press/release events on an
800 × 600 view; undo assertions use the synchronous undo baseline and store
listeners; the Angle with Given Size dialog is a Mockito spy of the real
dialog manager that answers with the dialog's own confirm call
(`DialogManager.createAngleFixed` with the received segments, points and
controller); the double-click Redefine dialog is recorded by the same spy, so
no real window opens.

| Test | Obligations (design §9) |
|---|---|
| `parallelPointFirstReusesEveryAdmittedReference` | `T-F2-PAR-POINT-FIRST` for line, segment, ray, polygon edge, 2D vector, linear function; injected reference never stays selected |
| `perpendicularPointFirstReusesEveryAdmittedReference` | `T-F2-PERP-POINT-FIRST` (same six references) |
| `pointFirstBeforeAnyReferenceWaitsForAnExplicitReference` | `T-F2-NO-MEMORY`; point-then-reference commits the used reference |
| `referenceFirstReplacesTheSlotAfterSuccess` | `T-F2-REF-FIRST-REPLACES` |
| `slotsAreIndependentAndSurviveToolChangesAndEscape` | `T-F2-SLOTS-INDEPENDENT`, `T-F2-CANCEL` (tool changes, Escape) |
| `emptySpaceWithAndWithoutMemory` | `T-F2-EMPTY-SPACE`; one undo point for point and line; undo removes both |
| `inheritedPointOnPathGestureIsUnchanged` | `T-F2-2610-UNCHANGED` (second path; same line after the delay) |
| `inheritedDoubleClickRedefineIsUnchanged` | `T-F2-2610-UNCHANGED` (double click: identical outcome with and without memory; memory unchanged) |
| `previewAndCancellationNeverCreateOrWrite` | `T-F2-PREVIEW-NO-CREATION`, `T-F2-CANCEL` (abandoned reference-first gesture keeps the slot) |
| `functionReferencesFollowTheKernelLinearityContract` | `T-F2-TYPES`, `T-F2-CERTIFICATE`: constant and non-linear functions yield no line and no slot; `a x² + x + 8` remembered at `a = 0`, suspended at `a = 1`, reused again at `a = 0` |
| `hiddenUndefinedAndHiddenLayerReferencesAreSuspended` | `T-F2-LIFECYCLE` (hidden, hidden layer, not in GV1, undefined; suspension never rebinds) |
| `currentnessAcrossRenameRecomputeRedefineAndDelete` | `T-F2-LIFECYCLE` (rename, recompute, redefine, delete, recreated label never adopted) |
| `rebuildsAndDocumentBoundariesClearBothSlots` | `T-F2-LIFECYCLE`, `T-F2-UNDO` (undo, redo, rebuilding redefinition, clearing `setXML`, `loadXML`, New) |
| `memoryIsNotAnUndoPointAndNotSerialized` | `T-F2-UNDO`, `T-F2-NO-SERIALIZATION` |
| `commandsAreUnchangedAndCreateTheSameConstructions` | `T-F2-COMMANDS-UNCHANGED` |
| `angleWithGivenSizeHidesOnlyTheAngleOnBothRoutesAndOrientations` | `T-F2-ANGLE-HIDDEN`, `T-F2-ANGLE-UNDO-REDO` (two points and segment; counter-clockwise and clockwise; one undo point; redo restores the hidden angle) |
| `hiddenAngleIsOrdinaryPersistedVisibilityAndRecoverable` | `T-F2-ANGLE-PERSISTENCE`, `T-F2-ANGLE-USER-SHOW` (`<show object="false">`, clearing `setXML` and `loadXML`) |
| `angleCommandScriptingApiAndOtherModesAreUnchanged` | `T-F2-ANGLE-COMMAND-UNCHANGED`, `T-F2-ANGLE-ROUTE-ONLY` (command, `Execute`, API, companion outside the tool mode) |
| `graphicsView2AndClassicKeepTheInheritedBehavior` | `T-F2-VIEWS`, `T-F2-CLASSIC-UNCHANGED` |

Not covered by an automated test: a right click (the inherited popup opens a
real window in the test host). By source, a right-click release is diverted to
`processRightReleased` (`EuclidianController.java:10621-10624`), whose body
(`:10774-10865`) calls no process step, so it neither reaches
`switchModeForProcessMode` nor writes the memory. It is in the smoke checklist.

**Regression classes** (all passing on the working tree): `G9U1IntersectionSessionTest`
27, `G9U1SemanticPointInteractionTest` 20, `G9U1SimilarityToolsTest` 8,
`PostE2P3DocumentToolsTest` 7, `PostE2P4GraphicsContextMenuTest` 9,
`PostE2P4OffsetConversionTest` 11, `PostG9U1A7NavigationTest` 7,
`PreG9BR6PlusA1LayerWorkspaceTest` 22, `PreG9BR6PlusE2NativeDimensionDesktopTest` 14,
`PreG9BR6PlusE3IsoABorderDesktopTest` 13, `PreG9BR6PlusF1OrientationCueTest` 14
(171 tests with the F2 class). Checkstyle (`checkstyleMain`, `checkstyleTest`):
no finding after replacing non-ASCII section signs in two new comments.

## 4. Registration, provenance and inventory

- Phase `PRE-G9B-R6-PLUS-F2`: `compile.desktop.semantic`,
  `junit.desktop.pre-g9b-r6-plus-f2.semantic` (producer and projection nodes
  with the F2 impact paths).
- Selection `pre-g9b-r6-plus-f2.desktop`: the F2 class and the eleven
  regression classes above.
- JUnit inventory regenerated with the official updater from executed and
  dry-run producer evidence; counts in §4.1; registry `junit_inventory` pin
  updated; coverage pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- `docs/upstream/modified-files.yml`: the controller entry extended; three new
  entries (memory, companion, test).
- Specification `geocedg/specs/ui/authoring-memory.md` (`PROPOSED`).
- Guides: one paragraph each in EN and ES §5.2 (remembered reference) and §5.5
  (Angle with given size); no heading change.

### 4.1 Inventory

| Selection | Identities |
|---|---|
| `pre-g9b-r6-plus-f2.desktop` (new) | 171 (12 classes, executed, 0 failures) |
| `discovery.desktop` | 2010 → 2029 (dry run) |
| `final.desktop` | 2004 → 2023 (executed: 2023 tests, 0 failures, 1 skipped) |
| `discovery.shared` | 6075 (unchanged) |
| `final.shared` | 6992 (unchanged) |
| selections / `PHASE` selections | 57 → 58 / 62 → 63 |
| `junit_inventory` catalog pin | `e36b70a59fe43603813cb5cd1fef813f964a92dd9e6a6688448454805e7026ef` → `c94c72d0dcbb4a305104551879f0dda90ab8f0c33d818cbf992b7fbe286b3e89` |

Other development checks on the working tree: guide tests
`PostP1BilingualUserGuideTest` 16, `PreG9BR5AGuideOutlineTest` 20,
`PreG9BR5AGuideNavigationTest` 12, `PostP1GuideRenderingTest` 17 (0
failures); `INFRA_UNIT` `verification-bbefef808af44f22ac3e1f3630f14f69` and
`STATIC` `verification-e58aadd00f2e44ba94db157502f22a81`, both `ACCEPTED /
COMPLETE` (`STATIC` with its standing `diagnostic.governance` finding and
`diagnostic.historical-consistency` unavailable); `git diff --check` clean.

## 5. Changed paths (from `bdb9b6a9`)

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLineReferenceMemory.java` (new)
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGControllerCompanion.java` (new)
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusF2AuthoringMemoryTest.java` (new)
- `geocedg/specs/ui/authoring-memory.md` (new)
- `geocedg/specs/operations/verification-registry.json`
- `geocedg/specs/operations/verification-junit-inventory.json`
- `tools/agent/tests/verification-final-coverage.Tests.ps1`
- `docs/upstream/modified-files.yml`
- `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`
- `docs/validation/pre_g9b_r6_plus_f2_candidate_report.md` (this file, new)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-candidate-evidence.json` (new)
- `docs/roadmap/geocedg_roadmap.md`, `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`

## 6. Verification

Development runs are listed in the JSON mirror. The acceptance runs —
`PHASE -Phase PRE-G9B-R6-PLUS-F2`, `STATIC` and `INFRA_UNIT` — run on the frozen
candidate commit (`BOUNDED_PHASE`, fresh log directories under
`artifacts/agent/`); their results are reported outside this file, which
cannot name its own commit. No `INTEGRATION` or `FINAL`.

## 7. Author smoke checklist (not performed by the agent)

Use Graphics 1 unless stated; repeat the key steps in English and Spanish
(Options → Language).

1. Parallel line: click a line, then a point: the parallel appears. Click two
   more points: each gets a parallel to the same line at once.
2. Click a different segment, then a point: the parallel uses the segment.
   Click another point: the segment is now the remembered reference.
3. Perpendicular line: click a point first: it waits for a reference (its own
   memory is empty). Click a line: the perpendicular appears. Switch back to
   Parallel and click a point: it still uses the segment of step 2;
   Perpendicular keeps its own line.
4. Change to Move, then to Point, press Esc, return to Parallel: a point click
   still uses the remembered reference.
5. Empty space with a memory: the click creates a point and its parallel; one
   Undo removes both. Empty space in a new document (no memory): only a point
   appears and the tool waits.
6. Select a line, then click a second line: a point on the second line and the
   parallel through it (unchanged behaviour). Double-click a line: the Redefine
   dialog opens as before. Right-click in the tool: the context menu as before;
   the memory is unchanged afterwards.
7. Click a reference and press Esc before clicking a point: the previous memory
   is still used.
8. Hide the remembered line (or hide its layer, or make it undefined by moving
   a defining point): a point click waits for a reference. Show it again: the
   next point click reuses it.
9. Rename the remembered line: still reused. Undo once, or Redo, or redefine
   the line, or delete it, or File → New / Open: the next point click asks for
   a reference.
10. A vector, a polygon side and a linear function such as `f(x)=2x+1` as
    references: reused. `k(x)=3`: no line is created (as before) and nothing
    is remembered.
11. Angle with Given Size: click two points, enter 40°, counter-clockwise: the
    rotated point is visible, the angle is not drawn, and it is listed in the
    Algebra View. Show it there; it appears. Repeat clockwise, and from a
    segment. Undo and Redo: the angle comes back hidden. Save and reopen: hidden
    angles stay hidden, shown ones stay shown.
12. Type `Angle(B, A, 40°)`: the angle is visible (unchanged).
13. Open Graphics 2 and use Parallel and Angle with Given Size there: no memory,
    and the angle is visible (unchanged behaviour). An angle created in
    Graphics 1 is hidden in Graphics 2 too.

## 8. Final state

```text
PRE-G9B-R6-plus-F2      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR_SMOKE            = PENDING
AUTHOR_APPROVAL         = PENDING
PUBLICATION             = NOT AUTHORIZED
selfApproved            = false
```

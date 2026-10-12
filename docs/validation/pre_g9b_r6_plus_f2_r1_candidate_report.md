# PRE-G9B-R6-plus-F2-R1 — dual-mode Parallel/Perpendicular activation: technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F2-R1 (resumed by
                            PRE-G9B-R6-plus-F2-R1-FR2-AUTHOR-DISPOSITION)
AUTHORIZATION             = AUTHOR AUTHORIZED — CONDITIONAL BOUNDED CORRECTIVE
                            IMPLEMENTATION (F2-R1 decision record v1.2 §6); FR-2 CONTRACT
                            REFINEMENT AND CONDITIONAL RESUMPTION (record v1.3 §7)
DESIGN BASE               = e13bfbd231700019a8f81c115b22a5bb8c44f7bb
                            tree 2e8ee23addebcb05c4505c30e1fa593c8a3d4fa7
CHAIN                     = e13bfbd2 → de5c2677 (authorization) → 7027cf93 (gate stop
                            at FR-2) → fdb55179 (FR-2 disposition) → 72b14a9b (revised
                            FR-2 gate, class frozen) → the candidate (this report)
FEASIBILITY               = FR-1 PASS; FR-2 PASS (REVISED O1 CONTRACT); FR-3 PASS;
                            FR-4 PASS (the GgbAPID-subclass FR-2 failure stays historical)
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen by the revised gate, 72b14a9b)
REGISTERED PHASE          = PRE-G9B-R6-PLUS-F2-R1
ORIGINAL F2 CANDIDATE     = 6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873 — FROZEN — HISTORICAL
                            EVIDENCE (not the acceptance result of F2-R1)
WIP BRANCH                = wip/pre-g9b-r6-plus-f2-r1-fr2-blocked (654f7021) — reviewed,
                            selectively reused, NOT A CANDIDATE, kept unchanged
TECHNICAL CANDIDATE       = the commit that contains this report (frozen); its identity
                            and the acceptance runs are reported outside this file
AUTHOR_SMOKE              = PENDING (required)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false; authorApproved = false; passClaimed = false
```

Machine-readable mirror:
[`pre-g9b-r6-plus-f2-r1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-candidate-evidence.json).
Contract: [corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md);
decisions: [F2-R1 decision record](pre_g9b_r6_plus_f2_r1_author_decisions_record.md) v1.3;
design: [dual-activation design](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`, scripting rows clarified by v1.3); gates:
[feasibility gate report](pre_g9b_r6_plus_f2_r1_feasibility_gate_report.md) (stop at
`FR-2`, historical) and [revised `FR-2` gate report](pre_g9b_r6_plus_f2_r1_fr2_o1_gate_report.md);
specification: [`authoring-memory.md`](../../geocedg/specs/ui/authoring-memory.md) v1.1 §11
(`PROPOSED`); tests and smoke: [test matrix and smoke plan](pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md).

## 1. Implementation

| Element | Change |
|---|---|
| `GeoCeDGToolActivation` (new) | behaviour per tool (`NORMAL` / `MEMORY_ENABLED`), session state only; toolbar marks `{mode, behaviour from the ActionEvent's own Ctrl through AppD.isControlDown(meta, ctrl), current AWT event}`; `runAsSingleArgumentRequest` (unmarked = genuine `NORMAL`; no request inside an explicit request or host-synchronization scope); scoped requests consumed by the first activation they decide; `resolve`: toolbar mark of the same event and mode first, then a scoped request; another mode without request = departure (both `NORMAL`); same mode without request = unchanged; language-change snapshot and restoration |
| `AppGeoCeDG` | `setMode(int)` override (Option O1): the inherited single-argument call inside `runAsSingleArgumentRequest`; `setMode(int, ModeSetter)` override: resolves the behaviour, cancels the partial gesture on a same-mode behaviour change, then `super`, then refreshes the status segment; `setLanguage(Locale)` override: the inherited call as host synchronization, then restoration of the captured Parallel/Perpendicular tool and behaviour unless a genuine activation intervened. `getGgbApi()` is **not** overridden; `GgbAPID` stays the scripting API |
| `GeoCeDGToolbarContainer` | `ProfileToolbar.addNativeGroup` adds the toolbar-mark listener to each native group button (notified before the inherited button action; reclicks and flyout items included) |
| `GeoCeDGEuclidianController` | `NORMAL` gate at the top of `processLineReferenceTool` (inherited `super.switchModeForProcessMode`, memory neither read nor written); `cancelPartialGesture(mode)` calls the inherited protected `initNewMode(mode)` |
| `GeoCeDGStatusBar` | hidden-by-default `tool-memory` segment and separator, refreshed synchronously from `updateText` and every activation / language change |
| `apps/geocedg/application-profile.yml` | `localized_text` keys `AuthoringMemory.Status.Parallel`, `.Perpendicular`, `.Tooltip` (EN, ES); no action, catalog, schema or icon change |
| unchanged | `GeoCeDGLineReferenceMemory` (slots, admissibility, currentness, certificate, lifecycle), Angle with Given Size, `GeoCeDGActionRegistry` (covered by the single-argument route), every inherited class, `source/shared/**`, `GgbAPID`, `CallJavaScript.SandboxClassShutter`, `ScriptUtil.ALLOWED`, commands, algorithms, XML, identity, Graphics View 2, Classic |

From the WIP branch (`654f7021`) the coordinator, the `setMode(int, ModeSetter)`
and `setLanguage` overrides, the toolbar listener, the controller gate and
cancellation, the status segment, the profile texts and the adapted F2 tests
were reused after review. Not reused: `GeoCeDGScriptingApi`, the
`scriptingApi` field and `GgbAPID` import, and the scoped marker in
`GeoCeDGActionRegistry.invoke` (redundant under O1). Added: the
`setMode(int)` override, `runAsSingleArgumentRequest` and the one-shot
consumption of scoped requests.

## 2. Tests

`PreG9BR6PlusF2R1DualActivationTest` — 16 tests, 0 failures. The real
application panel is shown in a 1280 × 960 window; toolbar activations are
mouse press/release events posted to the system event queue on the real
native button or the shown flyout item (Swing fills the `ActionEvent`
modifiers); JavaScript runs through the real `ScriptManagerD` with the original
`GgbAPID`; gestures drive the real Graphics View 1 controller; repeated clicks on
one object are spaced beyond the inherited double-click delay.

| Test | Obligations (test matrix §1; record v1.3 §7) |
|---|---|
| `plainToolbarActivationIsNormalAndNeverReadsAValidSlot` | `T-R1-ACT-NORMAL`, `T-R1-NORMAL-NO-READ` (point-first and empty space), `T-R1-NORMAL-NO-CLEAR` |
| `ctrlToolbarActivationEnablesMemoryAndIsLatched` | `T-R1-ACT-MEMORY`, `T-R1-LATCH` (Ctrl key pressed/released, Ctrl-click in the view) |
| `sameBehaviourReactivationKeepsThePartialGesture` | `T-R1-REACT-SAME` (Ctrl reclick in `MEMORY_ENABLED`, plain reclick in `NORMAL`) |
| `behaviourChangeCancelsOnlyThePartialGesture` | `T-R1-REACT-CHANGE`, `T-R1-NO-DOC`, `FR-3` (provisional point removed, identical completed objects, history, undo/redo availability, saved state, XML, slots, mode) |
| `flyoutSelectsPerpendicularWithItsOwnIndependentBehaviourAndSlot` | `T-R1-FLYOUT`, `T-R1-SLOTS` |
| `keyboardSpaceActivatesNormalAndCtrlSpaceHasNoBinding` | `T-R1-KEYBOARD` (the button's own Space bindings; no Ctrl+Space binding) |
| `normalKeepsTheInheritedPointOnPathGestureAndNeverWritesTheSlot` | `T-R1-INHERITED` (issue #2610 gesture in `NORMAL`), `T-R1-NORMAL-NO-WRITE` |
| `menuRegistryActivationIsNormalAlsoForTheActiveMemoryTool` | `T-R1-INDIRECT` (registry/menu on the active memory tool cancels the gesture; single-argument other tool departs) |
| `scriptingSelectsNormalAndNeverTouchesTheMemory` | `T-R1-SCRIPTING`; `FR-2` sequences: `NORMAL` → script → `NORMAL`; memory → script reselect → `NORMAL`; Java API route; `NORMAL` construction with another reference keeps the slot; memory → script Perpendicular → `NORMAL`; Ctrl after scripts reuses the memory |
| `javaScriptDocumentLoadingKeepsTheOriginalApiAndCommands` | JavaScript document loading (clearing load and library script) without a class-shutter error; same `GgbAPID` instance; `evalCommand` constructions unchanged; commands never write a slot |
| `hostSynchronizationNeverChangesTheBehaviourOrTheGesture` | `T-R1-SYNC`, `T-R1-NO-RESET` (two-argument same mode keeps behaviour and gesture; toolbar rebuild, dock focus, Graphics View 2, undo, redo, clearing `setXML`); two-argument other mode departs |
| `anUnusedToolbarMarkNeverReachesALaterActivation` | `T-R1-TOKEN` |
| `departureEscapeAndNewResetToNormal` | `T-R1-RESET` |
| `languageChangeKeepsToolBehaviourSlotsAndRerendersTheStatus` | `T-R1-LANGUAGE`, `FR-1` |
| `aSlotInvalidatedWhileNormalIsNotRevivedByALaterCtrlActivation` | `T-R1-STALE` |
| `statusSegmentIsLaidOutOnlyWhileMemoryIsEnabled` | `T-R1-STATUS`, `FR-4` (laid-out bounds on the bar's row, full text, EN and ES, independent of a held slot, never in the document) |

`PreG9BR6PlusF2AuthoringMemoryTest` — 19 tests, 0 failures: every `T-F2-*`
obligation unchanged, with the tool activated explicitly in `MEMORY_ENABLED`
through the coordinator's test seam (`T-F2-*` in `MEMORY_ENABLED`; Angle with
Given Size, commands, Graphics View 2 and Classic: `T-R1-UNCHANGED`).

Not automated: macOS and Linux modifiers, undocked toolbars, window focus and
modal dialogs (not claimed), and the operating-system-level Ctrl+click; the
author smoke covers the Windows interaction.

Status-bar regression classes adapted (expected component order only):
`PreG9BR6PlusD1DocumentUnitsTest.statusSegmentsPresentTheEffectiveUnitsAndOpenTheDialog`
and `PostE2P1R2SmokeFollowUpTest.theBarShowsTheSessionDrawingScaleWithoutTouchingTheDocument`
list every bar component in order; they now include the hidden `tool-memory`
segment and its separator between `sheet-coherence` and `paste-notice`.

Development defects found and corrected in the tests (not product defects):

- a `File → New` issued before the asynchronous undo store had landed opened the
  inherited save prompt (modal) and hung the run — the test now waits for the
  store and marks the document saved on the event thread;
- a Ctrl-click in the view is the inherited multi-selection gesture and creates
  nothing — the latch test asserts the behaviour only for it and
  presses/releases Ctrl through the key dispatcher between construction clicks;
- in the full Desktop suite (`final.desktop`, executed producer) every posted
  toolbar event of the class was discarded: classes that run earlier leave four
  application-modal "GeoGebra - Error" dialogs showing (a pre-existing test
  hygiene leak, outside F2-R1), and AWT modality blocks every other window — the
  class's own window is now excluded from application modality
  (`APPLICATION_EXCLUDE`), the other tests' windows are left untouched, and each
  toolbar activation asserts that the native button actually fired, with a
  diagnostic of the window and event state on failure;
- the scene is built on the event thread (the shown view paints there; an
  off-thread build raced a repaint), and the press point on the native button
  stays inside the icon band (below it the inherited button opens its flyout).

Other development checks on the working tree: the selection
`pre-g9b-r6-plus-f2-r1.desktop` 301 tests in 24 classes and `final.desktop` 2039
tests (1 skipped), 0 failures (executed producer evidence, third run); guide
tests `PostP1BilingualUserGuideTest` 16, `PostP1GuideRenderingTest` 17,
`PreG9BR5AGuideNavigationTest` 12, `PreG9BR5AGuideOutlineTest` 20 (0 failures);
checkstyle without a new finding (the existing `PreG9BR6PlusA1HiddenLayerTest`
line-length warning remains); `INFRA_UNIT` `verification-b4a48331f2b646758cebedd2579ac6a6` and `STATIC` `verification-88cdab7c4aff4952987c2d8c19842369`, both `ACCEPTED / COMPLETE` (`STATIC` with its standing `diagnostic.governance` finding and `diagnostic.historical-consistency` unavailable);
`git diff --check` clean.

## 3. Registration, provenance and inventory

- Phase `PRE-G9B-R6-PLUS-F2-R1`: `compile.desktop.semantic`,
  `workspace-profile.product`, `junit.desktop.pre-g9b-r6-plus-f2-r1.semantic`
  (producer and projection nodes with the F2-R1 impact paths).
- Selection `pre-g9b-r6-plus-f2-r1.desktop`: the F2-R1 and adapted F2 classes;
  toolbar and registry (`G9U1ActionRegistryTest`, `G9U1WorkspaceSurfaceTest`,
  `PreG9BR4OrderedListToolTest`, `PreG9BR4SplineV2AuthoringTest`,
  `PostG9U1A7NavigationTest`); status bar (`PreG9BR6PlusA1LayerWorkspaceTest`,
  `PreG9BR6PlusD1PasteNoticeVisibilityTest`, `PreG9BR6PlusD1DocumentUnitsTest`,
  `PostE2P1R2SmokeFollowUpTest`, `PreG9BR6PlusE3IsoABorderDesktopTest`);
  profile (`GeoCeDGProfileTest`); scripting and language
  (`PreG9BR5BCanonicalScriptTest`, `PreG9BR6ScriptSurfaceBoundaryTest`,
  `G9U1ScriptWorkflowTest`); and the F2 regression classes
  (`G9U1IntersectionSessionTest`, `G9U1SemanticPointInteractionTest`,
  `G9U1SimilarityToolsTest`, `PostE2P3DocumentToolsTest`,
  `PostE2P4GraphicsContextMenuTest`, `PostE2P4OffsetConversionTest`,
  `PreG9BR6PlusE2NativeDimensionDesktopTest`, `PreG9BR6PlusF1OrientationCueTest`).
- JUnit inventory regenerated with the official updater from executed and
  dry-run producer evidence; registry `junit_inventory` pin updated; coverage
  pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- `docs/upstream/modified-files.yml`: `AppGeoCeDG`, controller, status bar,
  toolbar container, F2 test and the two adapted status-bar test entries
  extended; two new entries (`GeoCeDGToolActivation`, the F2-R1 test).
- Specification `authoring-memory.md` v1.1 §11 (`PROPOSED`).
- Guides: the "Remembered reference" paragraph of EN and ES §5.2 rewritten for
  the Ctrl activation; no heading change.

### 3.1 Inventory

| Selection | Identities |
|---|---|
| `pre-g9b-r6-plus-f2-r1.desktop` (new) | 301 (24 classes, executed, 0 failures) |
| `discovery.desktop` | 2029 → 2045 (dry run) |
| `final.desktop` | 2023 → 2039 (executed: 2039 tests, 0 failures, 1 skipped) |
| `discovery.shared` / `final.shared` | 6075 / 6992 (unchanged) |
| selections / `PHASE` selections | 58 → 59 / 63 → 64 |
| `junit_inventory` catalog pin | `c94c72d0dcbb4a305104551879f0dda90ab8f0c33d818cbf992b7fbe286b3e89` → `58902e6162aee7b1e1952716ad12019867435f31d66e6997a096415959af785a` |

## 4. Changed paths (from `72b14a9b`)

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGToolActivation.java` (new)
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGToolbarContainer.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGStatusBar.java`
- `apps/geocedg/application-profile.yml`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusF2R1DualActivationTest.java` (new)
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusF2AuthoringMemoryTest.java`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusD1DocumentUnitsTest.java`,
  `source/desktop/desktop/src/test/java/org/geocedg/desktop/PostE2P1R2SmokeFollowUpTest.java`
- `geocedg/specs/ui/authoring-memory.md`
- `geocedg/specs/operations/verification-registry.json`
- `geocedg/specs/operations/verification-junit-inventory.json`
- `tools/agent/tests/verification-final-coverage.Tests.ps1`
- `docs/upstream/modified-files.yml`
- `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`
- `docs/validation/pre_g9b_r6_plus_f2_r1_candidate_report.md` (this file, new)
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-candidate-evidence.json` (new)
- `docs/roadmap/geocedg_roadmap.md`, `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`

## 5. Verification

Development runs are listed in the JSON mirror. The acceptance runs —
`PHASE -Phase PRE-G9B-R6-PLUS-F2-R1`, `STATIC` and `INFRA_UNIT` — run on the
frozen candidate commit (`BOUNDED_PHASE`, fresh log directories under
`artifacts/agent/`); their results are reported outside this file, which
cannot name its own commit. No `INTEGRATION` or `FINAL`.

## 6. Author smoke checklist (not performed by the agent)

Windows 11, Graphics 1, English and Spanish (Options → Language); the
[test matrix and smoke plan](pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md)
§2 is the source; condensed:

1. Click **Parallel line** normally: click a line, then a point — a parallel.
   Click another point: it waits for a reference. No status text.
2. **Ctrl + click** Parallel line: the status bar shows "Parallel Line · Memory"
   ("Paralela · Memoria"). Click a line, then a point; then two more points:
   each gets a parallel to that line at once.
3. While clicking points, press and release Ctrl: nothing changes.
4. Click the active tool normally: the status text disappears and a point click
   waits. Ctrl + click again: the text returns and the remembered line is used.
5. In memory mode, click empty space with no remembered reference for that tool
   (or click a line), then click the tool normally: the half-made gesture
   disappears; Undo is unaffected. Ctrl + click when already in memory mode
   keeps a pending point.
6. Open the toolbar menu and choose **Perpendicular line** normally (normal
   tool), then again with Ctrl (memory mode, its own memory).
7. Change the language in memory mode: tool, mode and remembered line stay; the
   status text changes language.
8. Esc or another tool: the text disappears; return without Ctrl → normal; with
   Ctrl → the still-valid reference is reused.
9. In normal mode build a parallel to a different line, then Ctrl + click: the
   old remembered line is still used.
10. Undo, Redo, File → New, Open in memory mode: the next point click asks for a
    reference.
11. **Construction** menu → Parallel line while it is active in memory mode: it
    becomes normal.
12. Tab to the toolbar and press Space on the Parallel button: normal mode.
13. Optional, if scripting is used: a JavaScript button script
    `ggbApplet.setMode(3)` selecting Parallel gives the normal tool, also when
    Parallel is active in memory mode; documents with JavaScript open normally.
14. Angle with Given Size, `Angle(B, A, 40°)` and Graphics 2: unchanged.

## 7. Final state

```text
FR-2                    = PASS (REVISED O1 CONTRACT)
PRE-G9B-R6-plus-F2-R1   = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR_SMOKE            = PENDING
AUTHOR_APPROVAL         = PENDING
PUBLICATION             = NOT AUTHORIZED
selfApproved            = false
```

# PRE-G9B-R6-plus-A-1 — layer workspace with session-only layer state: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-A-1
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-A-1
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)
PRODUCT CHANGE            = YES (Desktop session and view presentation)
SERIALIZATION CHANGE      = NONE
LAYER DOMAIN              = UNCHANGED (0..9)
GUIDE_IMPACT              = UPDATED
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval. The candidate commit cannot name
itself, so its identity and the two acceptance runs made on it are reported
outside this file. The machine-readable mirror is
[`pre-g9b-r6-plus-a1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a1-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-A-1` for implementation and technical
verification on 2026-10-02, exclusively for the scope of its canonical prompt,
on the published closeout of the A-1 preparation package.

| Identity | Value |
|---|---|
| implementation base `P_R6PLUS_A1_PROMPT` | `069c7a03da92f2c30f015815a0aa278e7356b356` |
| base tree | `707c0b557ed93de3aaf4fea7d76bb643fb20411a` |
| local `main` = `origin/main` = live remote `main` after `git fetch` | `069c7a03da92f2c30f015815a0aa278e7356b356` |
| worktree at entry | clean |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-plus-a1-layer-workspace` |

Entry states re-read from disk: `PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED —
PUBLISHED`; the A-1 preparation package `PASS — AUTHOR APPROVED — PUBLISHED`
([closeout](pre_g9b_r6_plus_a1_prompt_closeout_record.md)); `A-1` `NOT
AUTHORIZED` immediately before the instruction; `A-2`, `B` … `G`, `PRE-G9B-R7`
and `G9B` not authorized.

## 2. Canonical prompt amendment

The first tracked edit, in its own local commit, amended
`.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md`
from the published prepared prompt (blob
`38ac4be386b6b6ddb11eb422214a1357bfc59ad6`) into the execution prompt (blob
`237c8f303249f72e6cfd95624e1895e8c5675a77`): the base and tree above,
`AUTHORIZED FOR IMPLEMENTATION`, `implementationAuthorized = true`,
`selfApproved`, `authorApproved` and `passClaimed` kept `false`,
`VERIFICATION_CLASS = INTEGRATED_PHASE` frozen, and the author precision on
`AQ-L7` / `T-L7`: a hidden layer named through the chooser or the status-bar
control is shown and becomes the working layer; the click mode never selects an
object on a hidden layer, and `T-HIDE-HIT` is not weakened.
`Test-PromptContractDocument` (`task` profile): `execution_safe = true`.

## 3. What changed

### 3.1 Shared seams (upstream files, host-identical defaults)

| Seam | File | Host default |
|---|---|---|
| `App.getLayerForNewObject(construction, upstreamLayer)` | `common/main/App.java` | returns `upstreamLayer` |
| `App.isLayerShown(layer)` | `common/main/App.java` | returns `true` |
| default layer of a new object asks the seam | `common/kernel/ConstructionDefaults.java` | `min(8, maxLayerUsed)` unchanged |
| macro output layer asks the seam | `common/kernel/algos/AlgoMacro.java` | `maxLayerUsed` unchanged |
| painting skips hidden layers (geometric, action, mask and measurement passes) | `common/euclidian/EuclidianView.java`, `DrawableList.java` | every layer shown |
| label, bounding-box-handle and input-box hits skip hidden layers | `common/euclidian/EuclidianView.java` | every layer shown |
| point, rectangle and intersection hits skip hidden layers, before the top-layer rule | `common/euclidian/HitDetector.java` | every layer shown |
| `MODE_WORKING_LAYER = 141`, `WorkingLayer.Tool` | `common/euclidian/EuclidianConstants.java` | mode unused by Classic |
| numeric `Sort by Layer` order | `desktop/gui/view/algebra/AlgebraViewD.java` | identical to the text order within 0..9 |
| `WorkingLayer.Tool`, `WorkingLayer.Help` (default, `en`, `es`) | `menu*.properties` | new keys only |

`141` was re-validated as free before use: no `EuclidianConstants` field, profile
action or `audited_numeric_id` used it.

### 3.2 GeoCeDG-owned code

- `GeoCeDGLayerWorkspace` (new): the working layer and the hidden-layer set,
  `SESSION` only. Interactive creation is creation in the document construction
  while neither `isFileLoading()` nor the kernel loading mode nor a New/Open
  transition is active; every other creation keeps the upstream layer.
- `AppGeoCeDG`: answers the two seams, owns the workspace (activated only after
  the product constructor), attaches the status bar to the free SOUTH slot of
  the application panel, resets the session on New (`clearConstruction`) and
  initializes it after Open (`loadExistingFile`, `loadXML(String)`).
- `GeoCeDGEuclidianController`: one-shot mode 141.
- `GeoCeDGWorkingLayerChooser` (new): a list of exactly 0..9.
- `GeoCeDGStatusBar` (new): extensible segments; A-1 delivers `Layer: n`.
- `GeoCeDGAlgebraView`: per-layer eye and hidden marker for `Sort by Layer`
  groups; the eye press is handled before the host controller.
- `AlgoLocusV2`: the V2 output asks the seam (closes
  `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` for new objects).
- Profile: action `construction.working-layer` (upstream mode 141,
  `preference-only`, `product-only`) in the `edit-selection` group and the
  `selection-move-inspect` cluster after `construction.move-rotate`; 116
  actions; five `Workspace.Layer.*` texts (en/es). Icon: the inherited host
  `mode_copyvisualstyle` artwork, reused explicitly; no new asset, so the asset
  manifest is unchanged.

## 4. Deviations from the P0 design candidate

| # | P0 design candidate | Candidate | Reason |
|---|---|---|---|
| D1 | hit testing: post-filter of `EuclidianView.setHits` results | filter inside the `HitDetector` loops (point, rectangle, intersection) and the view's label, handle and input-box hit loops | `HitDetector` keeps only the hits on the top layer; a post-filter would let an object on a hidden top layer shadow a shown object below it (`T-HIDE-HIT` proves it) |
| D2 | status bar re-added by an `AppGeoCeDG` override of `updateApplicationLayout` | override of `buildApplicationPanel` | the free SOUTH slot belongs to the local `applicationPanel`, which `updateApplicationLayout` cannot reach and never clears; the `southPanel` it rebuilds hosts the bottom input bar |
| D3 | `getLayerForNewObject()` | `getLayerForNewObject(construction, upstreamLayer)` | macro (tool) constructions and other kernels must keep the upstream value; the host default is the identity |
| D4 | painting hook in `drawGeometricObjects` | also the action, mask and measurement passes of `drawObjects`; not a direct `drawActionObjects` call, not the background pass | dropdowns, input boxes and masks are drawn in those passes; the SVG route calls `drawActionObjects` directly and shares the background pass, so leaving those paths at host behavior keeps SVG unchanged (`B`) |
| D5 | Algebra View eye through a click listener | interception in `GeoCeDGAlgebraView.processMouseEvent` | a listener runs after the host controller, which would expand the group or select all its objects on the same press |
| D6 | `IsVisible` in `T-OBJECT-VISIBILITY` | the API `getVisible(label)` and `getVisible(label, view)` | the host (GeoGebra 5.4.928) has no `IsVisible` command; the object-visibility query is the API |
| D7 | numeric order in a GeoCeDG override | one-line change in `AlgebraViewD.getParentNode` | the group map and root are private; the change is behavior-identical within 0..9 |
| D8 | "an owned icon" | explicit reuse of host artwork, as R4 did for mode 140 | a new owned asset needs an asset-manifest entry with an author-approved redistribution status |

Registration facts the prompt did not state:

- The registry schema requires an upper-case `phase_id`, so the selection is
  registered as `PRE-G9B-R6-PLUS-A-1`. `verify.ps1` upper-cases `-Phase` before
  resolving it, so the authorized command `-Phase PRE-G9B-R6-plus-A-1` resolves
  to it unchanged.
- Adding the action required the bounded profile-authority amendment of
  `tools/agent/workspace-profile-validation.ps1` (one approved action id, the R4
  pattern) and its input pin in `verification-static-contracts.json`. No
  validator logic changed.

## 5. Obligation → test map

`LW` = `PreG9BR6PlusA1LayerWorkspaceTest`, `HL` = `PreG9BR6PlusA1HiddenLayerTest`
(Desktop), `LS` = `PreG9BR6PlusA1LayerSeamTest` (shared).

| ID | Tests |
|---|---|
| `T-WL-ROUTES` | `LW.algebraInputAndInputBarCreateOnTheWorkingLayer`, `LW.toolsCreateOnTheWorkingLayer`, `LW.ggbScriptAndNestedExecuteCreateOnTheWorkingLayer`, `LW.macroOutputsCreateOnTheWorkingLayer`, `LW.locusV2AndSplineV2CreateOnTheWorkingLayer`, `LS.constructionDefaultsAskTheSeamWithTheOwningConstruction`, `LS.macroOutputsAskTheSeam`, `LS.locusV2FamilyOutputsAskTheSeam` |
| `T-WL-REBUILD` | `LW.undoAndRedoRestoreStoredLayersAndNeverUseTheWorkingLayer`, `LW.redefinitionRebuildKeepsStoredLayers`, `LW.openInitializesFromTheDocumentAndWritesNothing` |
| `T-WL-LIFECYCLE` | `LW.newStartsAtZeroAndClearsHiddenLayers`, `LW.openInitializesFromTheDocumentAndWritesNothing`, `LW.anOpenedDocumentWithoutDrawablesStartsAtZero`, `LW.undoAndRedo…`, `LW.redefinitionRebuild…` |
| `T-PASTE` | `LW.pasteKeepsTheCopiedLayersForEveryWorkingLayer` |
| `T-MODE` | `LW.theModeIsAGeoCeDGMoveGroupModeWithLocalizedText`, `LW.aClickOnAnObjectAdoptsItsLayerAndReturnsToMove`, `LW.axesAreNotObjectsOfTheMode`, `LW.emptySpaceOpensTheBoundedChooserAndCancelChangesNothing`, `LW.selectionPreviewNeverChoosesALayer`, `LW.theProductDialogOffersExactlyTheDomainAndNeverWraps` |
| `T-PROFILE` | `LW.theMoveGroupGainsExactlyOneActionAndClassicIsUnchanged`; updated pins in `G9U1ProfileCompilerTest`, `G9U1ActionRegistryTest`, `G9U1WorkspaceSurfaceTest`, `GeoCeDGProfileTest` (115 → 116 actions, 46 → 47 toolbar modes, 70 → 71 catalog modes, 56 → 57 toolbar ids) |
| `T-STATUS` | `LW.theStatusBarSurvivesLayoutRebuildsAndTracksTheWorkingLayer` |
| `T-AV-ORDER` | `LW.sortByLayerOrdersGroupsNumerically` |
| `T-AV-EYE` | `LW.theEyeTogglesOnlyTheSessionStateAndNeverTheObjects` |
| `T-L7` | `LW.theWorkingLayerCannotBeHiddenAndAChosenHiddenLayerIsShown`, `HL.theWorkingLayerModeCannotAdoptAHiddenLayerByClicking` |
| `T-HIDE-PAINT` | `HL.graphicsOneOmitsObjectsOnHiddenLayersWithoutLosingDrawables`, `HL.graphicsTwoIsAHostViewAndObeysTheSameRule`, `HL.otherLayersAndHiddenObjectsBehaveAsAtTheBase` |
| `T-HIDE-HIT` | `HL.objectsOnHiddenLayersAreNotHittableAndNeverShadowShownOnes`, `HL.theWorkingLayerModeCannotAdoptAHiddenLayerByClicking` |
| `T-HIDE-STATE` | `HL.togglingIsSessionOnlyAndSurvivesUndoAndRedo`, `HL.macroAndClipboardXmlCarryNoLayerState`, `LW.newStarts…`, `LW.openInitializes…` |
| `T-HIDE-GEOMETRY` | `HL.slopeFieldAndOdeGeometryIsUnchangedByHiding` |
| `T-OBJECT-VISIBILITY` | `HL.hostLayerCommandsStillWriteObjectVisibilityOnly`, `LW.theEyeTogglesOnlyTheSessionState…` (marble) |
| `T-CLASSIC` | `HL.theHostSeamsKeepTheUpstreamRuleUntilTheProductIsReady`, `LS.hostSeamsReturnTheUpstreamValueAndShowEveryLayer`, `LS.hostCreationKeepsTheUpstreamTopLayerRule`, `LS.hostDrawAllDrawsWhatThePredicateFormDrawsWithAnAdmittingPredicate`, `LS.anUpstreamAnswerLeavesLocusV2WhereTheHostPutsIt` |
| `T-EXPORT-INHERITED` | `HL.picturesExportedThroughTheViewPaintingOmitHiddenLayers` (PNG through `getExportImage`, print through `exportPaint`; PDF and EMF call the same `exportPaint`, `GraphicExportDialog.java` exports at the EMF and PDF call sites) |
| `T-EXPORT-INTERIM` | `HL.svgLatexAndDxfAreUnchangedByAHiddenLayerUntilBAndC` (SVG compared after normalizing its generated clip-path UUIDs, which differ between any two exports) |
| `T-SMOKE` | §8 |

## 6. Temporary export inconsistency between A-1 and B/C

| Export | Object on a hidden layer after A-1 | Owner |
|---|---|---|
| PNG, PDF, EMF, print, graphics image copy | omitted, inherited from the shared view painting (`exportPaint`) | `B` confirms within the export surface |
| SVG | written as at the base (own drawable loop, direct `drawActionObjects`, shared background pass) | `B` |
| PGF/TikZ, PSTricks, Asymptote | written as at the base | `C` (exclusion decided, `AQ-L3`) |
| DXF | written as at the base | `C` (policy open) |

The inconsistency is documented in the user guide (§12.5 and §15, both
editions) and in the developer guide.

## 7. Open, New and byte identity

- New: working layer 0, no hidden layer.
- Open: the working layer becomes the highest layer used by a drawable object;
  hidden layers are cleared. Open writes nothing:
  `LW.openInitializesFromTheDocumentAndWritesNothing` compares the construction
  XML of the saved and reopened document (equal), keeps an individually hidden
  object hidden, and re-saves and reopens the document with an equal
  construction XML.
- Undo, redo and redefinition never read or reset the session.
- A document opened inside the host constructor (command line) initializes the
  session once the product constructor finishes.

## 8. Author-smoke checklist (not performed or attributed by the agent)

1. New document: the status bar shows `Layer: 0`; create a point; it is on layer 0.
2. Choose **Working Layer** in the Move group; click empty space; choose 4; the
   tool returns to Move and the status bar shows `Layer: 4`.
3. Create a point with a tool, one in Algebra Input and one with a script button;
   all are on layer 4 (Properties → Advanced → Layer).
4. With Working Layer, click the first point; the working layer becomes 0 and the
   tool returns to Move.
5. Algebra View → Sort by Layer: groups appear in numeric order with an eye.
   Click the eye of layer 4: its objects disappear from Graphics 1 and Graphics 2
   and cannot be selected there; their marbles do not change; the group reads
   "(hidden)". Click it again: they return.
6. The eye of the working layer is greyed and does nothing.
7. Hide layer 4, then choose layer 4 in the status-bar chooser: it is shown and
   becomes the working layer.
8. Hide a layer, then save, close and reopen: every layer is shown and the
   document is unchanged; undo and redo keep a hidden layer hidden.
9. Copy and paste an object of layer 4 with working layer 0: the copy is on
   layer 4.
10. `HideLayer(4)` / `ShowLayer(4)` still hide and show each object of layer 4
    (marbles change), independently of the eye.
11. With a layer hidden, export PNG or PDF (object absent) and SVG (object still
    present); this is the documented temporary inconsistency.
12. Spanish UI: `Capa: n`, **Capa de trabajo**, "(oculta)".

## 9. Observations and residual risks

| ID | Observation | Owner |
|---|---|---|
| `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | Graphics 2 uses the upstream `EuclidianControllerFor3DD`, so GeoCeDG modes (140, the Locus V2 tools and now 141) do not act there. Pre-existing; painting and hit testing do apply in Graphics 2. | later frontend phase |
| `OBS-A1-BACKGROUND-IMAGE-LAYER` | Background images are drawn by the background pass outside the layered drawable list (shared with SVG) and are not hidden with their layer. | `B` / author |
| `OBS-A1-3D-VIEW` | Hidden layers do not apply to the 3D view, which has its own renderer and hits. | author |
| `OBS-A1-NON-HIT-SELECTION` | Selection that is not hit testing (keyboard cycling, Algebra View, Select All) can still select objects on hidden layers. | author |
| `OBS-A1-RECOMPUTE-OUTPUTS` | Outputs an algorithm creates during a later recompute (for example additional labelled intersection points) take the working layer where the host takes its maximum used layer. | none required |
| `OBS-A1-URL-OPEN` | The API-only `loadXML(URL)` route does not reset the session; its objects still keep their stored layers because the XML is read with `isFileLoading()`. | none required |

## 10. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance campaigns. The acceptance campaigns on the frozen candidate are
reported with the candidate, outside this file.

| Run | Result |
|---|---|
| focal `PreG9BR6PlusA1LayerSeamTest` (shared) | 7 tests, 0 failures/errors |
| focal `PreG9BR6PlusA1LayerWorkspaceTest` (Desktop) | 22 tests, 0 failures/errors |
| focal `PreG9BR6PlusA1HiddenLayerTest` (Desktop) | 12 tests, 0 failures/errors |
| development run `:shared:common-jre:test :desktop:desktop:test` | shared 6 857 tests, 0 failures/errors, 10 skips; Desktop 1 697 tests, 0 failures/errors, 1 skip |
| guide suites after the §12.5 pin update | `PreG9BR5AGuideOutlineTest` 20, `PreG9BR5AGuideNavigationTest` 12, `PostP1GuideRenderingTest` 17, `PostP1BilingualUserGuideTest` 16, `G9U1WorkspaceSurfaceTest` 25; 0 failures/errors |
| producer `discovery.shared` (`--test-dry-run`) | completed, 5 940 identities |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 697 identities |
| producer `pre-g9b-r6-plus-a1.shared` (executed) | 17 tests, 0 failures/errors |
| producer `pre-g9b-r6-plus-a1.desktop` (executed) | 173 tests, 0 failures/errors |
| producer `final.shared` (executed, complete shared suite) | 6 857 tests, 0 failures/errors, 10 skips |
| producer `final.desktop` (executed, complete Desktop suite outside the isolated partition) | 1 691 tests, 0 failures/errors, 1 skip |
| Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest`) | no finding in the touched main sources; five declaration-distance warnings in the new tests fixed by `final` |
| `-PlanOnly` | `PHASE -Phase PRE-G9B-R6-plus-A-1` `COMPLETE`, 9 nodes; `INTEGRATION` 46, `FINAL` 68, `STATIC` 10, `INFRA_UNIT` 22 nodes, all `COMPLETE` |
| development `STATIC` and `INFRA_UNIT` on the uncommitted candidate tree | `ACCEPTED / COMPLETE` (`verification-25ccc70b055b4e5eb49a7fd2a0eb639b`, `verification-1a7ab6fa5393404ca1d7702301d82a3b`); not acceptance evidence |

The skips are the pre-existing allowlisted ones.

**JUnit inventory and pins.** The current pins were reproduced first
(`junit_inventory` `a611c831…`, `static_contracts` `fa2004a1…`,
`workspace-profile-validation.ps1` input `44d9c8d7…`). Discovery evidence came
from `--test-dry-run` producer runs, executed evidence from the passing producer
runs above, and `tools/agent/update-verification-junit-inventory.ps1` wrote
every count and hash: shared module discovery 5 933 → 5 940
(`798f6f2e…`), Desktop 1 663 → 1 697 (`3ba9d9ae…`), `final.shared`
6 850 → 6 857 (`5d5f3546…`), `final.desktop` 1 657 → 1 691 (`8f9df112…`), and
the new `pre-g9b-r6-plus-a1.shared` (17, `df27b441…`) and
`pre-g9b-r6-plus-a1.desktop` (173, `f78d36e4…`). Every other selection is
unchanged. Pins rewritten with `Get-VerificationCanonicalTextSha256`:
`junit_inventory` `b6e11fe4…`, `static_contracts` `c2b6e388…`, and the
`workspace-profile-validation.ps1` input `f52472d6…`. No identity hash was
typed by hand; the selection skeletons carried placeholders only until the
updater filled them. The registry-shape pins in
`tools/agent/tests/verification-final-coverage.Tests.ps1` follow: 34
selections, 43 PHASE selections, and the two new selection counts.

## 11. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: one registered PHASE selection with two JUnit selections through the
existing registry, inventory and updater; registry-shape pins in
tools/agent/tests and the bounded profile-authority amendment follow the R4/R6
precedent; no verifier, schema or profile-composition semantics change.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md;
  geocedg/specs/ui/cedg-workspaces.md
SERIALIZATION CHANGE = NONE
```

## 12. Changed paths

- Governance (first commit): `.github/prompts/tasks/pre-g9b-r6-plus-a1-layer-workspace-session.prompt.md`.
- Shared upstream seams: `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_en.properties`, `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_es.properties`, `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties`, `source/shared/common/src/main/java/org/geogebra/common/euclidian/DrawableList.java`, `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianConstants.java`, `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianView.java`, `source/shared/common/src/main/java/org/geogebra/common/euclidian/HitDetector.java`, `source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoMacro.java`, `source/shared/common/src/main/java/org/geogebra/common/kernel/ConstructionDefaults.java`, `source/shared/common/src/main/java/org/geogebra/common/main/App.java`.
- Shared GeoCeDG-owned: `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusV2.java`.
- Desktop upstream: `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/view/algebra/AlgebraViewD.java`.
- Desktop GeoCeDG-owned: `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGAlgebraView.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGLayerWorkspace.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGStatusBar.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGWorkingLayerChooser.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/resources/GeoCeDGToolImageResource.java`.
- Profile: `apps/geocedg/application-profile.yml`.
- Tests: `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ActionRegistryTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ProfileCompilerTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGProfileTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR5AGuideOutlineTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusA1HiddenLayerTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusA1LayerWorkspaceTest.java`, `source/shared/common-jre/src/test/java/org/geocedg/common/kernel/PreG9BR6PlusA1LayerSeamTest.java`.
- Verification registration: `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json`, `geocedg/specs/operations/verification-static-contracts.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1`, `tools/agent/workspace-profile-validation.ps1`.
- Documentation and evidence: `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, `docs/developer/geocedg_developer_guide.md`, `docs/roadmap/geocedg_roadmap.md`, `docs/upstream/modified-files.yml`, `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`, `docs/validation/pre_g9b_r6_plus_a1_candidate_report.md`, `geocedg/specs/ui/cedg-workspaces.md`, `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a1-candidate-evidence.json`.

# PRE-G9B-R6-plus-F1 — orientation cue: technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F1
AUTHORIZATION             = AUTHOR AUTHORIZED — BOUNDED IMPLEMENTATION AND TECHNICAL
                            VERIFICATION (2026-10-11; F1 author-decision record v1.2 §8)
IMPLEMENTATION BASE       = c52f565e9f2c4c3515a68f6ac120ffc9c393dfbc
                            tree fdfbc327983a18d6761411591daf606942f15fda (published F1 preparation)
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen at the readiness gate, before the first productive edit)
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this file
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
AUTHOR_SMOKE              = PENDING
PUBLICATION               = NOT AUTHORIZED
selfApproved = false
```

Execution contract:
[canonical F1 prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md)
(`AUTHORIZED FOR IMPLEMENTATION`). Decisions:
[F1 author-decision record](pre_g9b_r6_plus_f1_author_decisions_record.md) v1.2.
Design: [reconciled design](../architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`). Contract of the candidate:
[`geocedg/specs/presentation/orientation-cue.md`](../../geocedg/specs/presentation/orientation-cue.md)
(`PROPOSED`). Evidence of the preparation:
[characterization report](pre_g9b_r6_plus_f1_preparation_characterization_report.md).
Machine-readable mirror:
[`pre-g9b-r6-plus-f1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-candidate-evidence.json).

## 1. Entry gate and readiness

```text
local main = origin/main = live remote main = c52f565e9f2c4c3515a68f6ac120ffc9c393dfbc
tree fdfbc327983a18d6761411591daf606942f15fda; worktree clean
F1 not implemented at the base (no orientation-cue code, key or test)
branch phase/pre-g9b-r6-plus-f1 created from that commit
first tracked change: f1166e3e (canonical prompt AUTHORIZED FOR IMPLEMENTATION,
                      record v1.2 §8 with the readiness gate, BOUNDED_PHASE frozen)
```

Readiness (record §8): a Graphics View 1 overlay in the GeoCeDG-owned
`GeoCeDGEuclidianView.paint`; the checkbox in the GeoCeDG-owned
`GeoCeDGPresentationOptionsPanel` without any `AppD` or `OptionsLayoutD`
change; a bounded catalog delta 130 → 131; no shared drawable, kernel, XML or
export change; the phase `PRE-G9B-R6-PLUS-F1`. The implementation stayed inside
that envelope.

## 2. Implementation

| Part | Change |
|---|---|
| Cue model | new `GeoCeDGOrientationCue`: eligibility matrix v1 (`A1`–`A17` by exact parent-algorithm class, recursive fail-closed admission of orientation-bearing inputs, conservative macro rule through `AlgoMacro.getMacro`, `Macro.getMacroInput/Output`, `getInput`, `getOutput`), predicate of spec §4, kernel path parametrization, Liang–Barsky clipping to the view inset by 10 px, anchors of spec §5 |
| Rendering | new `GeoCeDGOrientationCueRenderer`: closed triangle, tip 2/3·10 px ahead of the anchor, base 4 px, background fill, 1 px outline in the object colour with its line opacity |
| Seam | `GeoCeDGEuclidianView.paint` calls the renderer after `super.paint` and the export-area overlay when the preference is on |
| Preference | new `GeoCeDGOrientationCuePreference` (`geocedg.view.orientation-cue.v1`, `shown`/`hidden`, default OFF, read never writes, test seam) |
| Owner | `AppGeoCeDG`: lazy preference owner, `setOrientationCueShown`/`toggleOrientationCue`, listeners, propagation to every GeoCeDG frame, action refresh and repaint; no document, saved-state or undo call |
| Panel | `GeoCeDGPresentationOptionsPanel`: one titled group with the checkbox below the unchanged six-row size grid |
| Action | `presentation.orientation-cue` in the profile (`product-action`, target `geocedg.presentation.orientation-cue`, `preference-only`, `product-only`, `action-only`, `experimental`), View group `view-visibility-style`, cluster `presentation-visibility-style`, five EN/ES texts and three panel texts; `GeoCeDGActionRegistry` target, `execute` and `checked`; `GeoCeDGProfile` count 131 |

Unchanged: kernel, `GeoLine`, shared drawables, `EuclidianView`, exporters,
`ExportViewport`, `PictureExportService`, print and clipboard routes, document
and preferences XML, undo, Classic, Graphics View 2, `AppD`, `OptionsLayoutD`.

## 3. Tests and evidence

`PreG9BR6PlusF1OrientationCueTest` (14 tests):

| Obligation | Test | Result |
|---|---|---|
| `T-F1-SENSE`, `T-F1-ELIGIBILITY` (admitted) | `everyAdmittedRouteShowsTheKernelSense` | `A1`–`A16` and `A17` (macro join and macro parallel), two-line bisectors, both parallel tangents, polygon edges, a segment image: cue present, world sense = `getDirection`, agrees with the kernel path parameter, screen unit = normalized `(dx·xscale, −dy·yscale)` |
| `T-F1-ELIGIBILITY` (excluded) | `everyExcludedRouteKeepsItsKernelSenseButShowsNoCue` | explicit, implicit and parametric equation lines, a dependent line, tangents at and from a point, the three-point bisector, tangent to a function, parallel and perpendicular to a function, asymptotes, polar, parallel to and image of an equation line, a shear: kernel sense non-zero, not admitted, no cue (≥ 17 lines checked); axis, list member, native dimension outputs and a macro with an excluded orientation-bearing input: no cue; the same macro with an admitted input: cue |
| `T-F1-REVERSAL`, `T-F1-DYNAMIC` | `reversalsAndMotionFollowTheKernelWithoutSmoothing` | `(A,B)`/`(B,A)` for line, segment, ray, parallel and perpendicular opposite; `v` reversed reverses `Line(P,v)`; `B` onto `A`: no cue for the line and its parallel; `B` behind `A`: the cue follows the reversed kernel sense; identities kept; a redefinition shows its own sense |
| `T-F1-DEGENERATE` | `degeneraciesShowNoCueAndNeverThrow` | coincident points (zero-length segment defined but no cue), zero vector, below-tolerance coincidence, dilation by 0, infinite (reported defined) and `NaN` coefficients, the undefined bisector of parallel lines: no cue, no exception, paint succeeds |
| `T-F1-VISIBILITY` | `onlyEffectivelyVisibleObjectsCarryACue` | hidden object, hidden layer, thickness 0, false show condition, not visible in Graphics 1, three midpoint arrow decorations: no cue; auxiliary object and endpoint arrow style: cue |
| `T-F1-PLACEMENT` | `placementIsDeterministicAndScreenOnly` | segment midpoint, ray start + 30 px, line point nearest the centre, off-screen ray start, corner-cutting line clamped onto the chord and on the line, 20 px segment suppressed, pan/zoom/unequal ratio keep the sense, resize, logarithmic axis suppresses |
| `T-F1-RENDERING`, `T-F1-HIT` | `theLivePaintDrawsAHollowTenByFourArrowheadOnly` | axial length 10, base 4; option off/on: only pixels within 9 px of the anchor change; outline in the object colour, light interior; hit lists at and beside the arrowhead identical |
| `T-F1-VIEWS` | `graphicsTwoNeverShowsTheCue` | Graphics 2 is the upstream view class; its paint is pixel-identical with the option off and on |
| `T-F1-EXPORT` | `noExportPrintOrClipboardRouteShowsTheCue` | option off/on identical: picture-service PNG (the clipboard image route), picture-service print, PDF (date-normalized), SVG (clip ids normalized), EMF bytes, host export image, host print paint, PGF/TikZ, PSTricks, Asymptote, DXF |
| `T-F1-NO-MUTATION` | `togglingChangesNoConstructionXmlUndoIdentityOrSavedState` | construction XML, document XML, object XML, undo/redo availability, saved state and identities unchanged after three toggles; no `orientation-cue` text in document or preferences XML |
| `T-F1-PREFERENCE` | `theGlobalPreferenceIsOffByDefaultPersistentAndDocumentIndependent` | default OFF, reading never writes, `shown`/`hidden`, a new application on the same store keeps it (restart), an invalid value reads OFF and is not rewritten, New and Open keep it |
| `T-F1-UI-SYNC` | `theMenuActionAndThePanelCheckboxShareOnePreference` | menu action and panel checkbox follow each other in both directions; six size controls preserved; localized texts |
| `T-F1-PROFILE` | `theCatalogHoldsOneDeclarativePreferenceActionInTheViewMenu` | catalog 131, record, View group, cluster, EN/ES texts |
| `T-F1-CLASSIC` | `classicHasNoCueAndNeverReadsThePreference` | the Classic view is not a GeoCeDG view; the store is untouched |

Pin and regression classes (all passing): `G9U1ActionRegistryTest` (14),
`G9U1ProfileCompilerTest` (32), `G9U1WorkspaceSurfaceTest` (25),
`GeoCeDGProfileTest` (5), `PreG9BS4PresentationSizingTest` (12; the panel pin
now counts the six-row grid plus the orientation-cue group),
`PreG9BP0PresentationThemeTest` (18), `PreG9BR6PlusA1LayerWorkspaceTest` (22),
`PreG9BR6PlusA1HiddenLayerTest` (12), `PreG9BR6PlusBExportSurfaceTest` (20),
`PreG9BR6PlusBPictureFidelityTest` (15), `PreG9BR6PlusE1LCuratedLibraryTest` (9),
`PreG9BR6PlusE2NativeDimensionDesktopTest` (14),
`PreG9BR6PlusE3IsoABorderDesktopTest` (13); guide tests
`PostP1BilingualUserGuideTest` (16), `PreG9BR5AGuideOutlineTest` (20),
`PreG9BR5AGuideNavigationTest` (12), `PostP1GuideRenderingTest` (17).

Limitations of the evidence: the CLI `--export` route and the system clipboard
are not driven directly; both go through `PictureExportService`, whose PNG,
SVG, PDF, EMF and print outputs are compared. The preference is written to the
portable properties file when a window closes or with Save Settings, like every
GeoCeDG preference; a process kill before that loses an unsaved change.

## 4. Registration, provenance and inventory

- Phase `PRE-G9B-R6-PLUS-F1`: `compile.shared.semantic`,
  `compile.desktop.semantic`, `workspace-profile.product`,
  `junit.desktop.pre-g9b-r6-plus-f1.semantic`.
- JUnit selection `pre-g9b-r6-plus-f1.desktop`: the F1 class and the 13 pin
  and regression classes of §3 (14 classes).
- Profile authority: the approved-id block of
  `tools/agent/workspace-profile-validation.ps1` (`R6-plus-F1`), its
  static-contract input pin (`6a131d75…`) and the `static_contracts` registry
  pin (`b6b0ba8a…`), each reproduced from the previous value first.
- `docs/upstream/modified-files.yml`: purposes appended for the five product
  files and nine tests; the three new product classes and the new test
  registered.
- JUnit inventory regenerated by the in-session updater from discovery and
  executed evidence: `pre-g9b-r6-plus-f1.desktop` 225 identities;
  `discovery.desktop` 1996 → 2010 and `final.desktop` 1990 → 2004 (the 14 new
  tests); `discovery.shared` 6075 and `final.shared` 6992 unchanged; 57
  selections, 62 phase selections; catalog pin `21bfda2e…` → `e36b70a5…`
  (reproduced from the registry first); every producer used `COMPLETED`, inner
  exit 0. A first `final.desktop` run (13.4 min) had one failure,
  `G9U1SemanticPointInteractionTest.ownedDragUndoRedoRestoresExactSemanticAddress`
  (Locus V2 drag undo/redo, a path F1 does not touch; the cue is OFF by
  default). It passed 3 of 3 times alone and in a complete rerun (13.3 min;
  2004 tests, 0 failures, 1 skipped), which is the evidence used. The failed run
  is kept as scratch evidence. The failure is recorded as an intermittent
  observation: no F1 code is on its path and it did not reproduce, but its root
  cause was not investigated here and its pre-existence was not probed on the
  base.
- Coverage pins: 57 selections, 62 phase selections, the F1 selection count.

## 5. Changed paths

```text
M  .github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md (authorization status; commit f1166e3e)
M  docs/validation/pre_g9b_r6_plus_f1_author_decisions_record.md (v1.2; commit f1166e3e)
M  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-author-decisions.json (v1.2; commit f1166e3e)
A  geocedg/specs/presentation/orientation-cue.md
A  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGOrientationCue.java
A  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGOrientationCueRenderer.java
A  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGOrientationCuePreference.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianView.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGPresentationOptionsPanel.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java
M  source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java
A  source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusF1OrientationCueTest.java
M  source/desktop/desktop/src/test/java/org/geocedg/desktop/{G9U1ActionRegistryTest, G9U1ProfileCompilerTest,
   G9U1WorkspaceSurfaceTest, GeoCeDGProfileTest, PreG9BR6PlusA1LayerWorkspaceTest,
   PreG9BR6PlusE1LCuratedLibraryTest, PreG9BR6PlusE2NativeDimensionDesktopTest,
   PreG9BR6PlusE3IsoABorderDesktopTest, PreG9BS4PresentationSizingTest}.java (pins)
M  apps/geocedg/application-profile.yml
M  tools/agent/workspace-profile-validation.ps1
M  geocedg/specs/operations/verification-static-contracts.json
M  geocedg/specs/operations/verification-registry.json
M  geocedg/specs/operations/verification-junit-inventory.json
M  tools/agent/tests/verification-final-coverage.Tests.ps1
M  geocedg/specs/ui/cedg-workspaces.md (catalog 131)
M  docs/upstream/modified-files.yml
M  docs/user/geocedg_user_guide_en.md, docs/user/geocedg_user_guide_es.md (one paragraph in §5.2)
A  docs/validation/pre_g9b_r6_plus_f1_candidate_report.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-candidate-evidence.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md (status)
M  docs/roadmap/geocedg_roadmap.md (status)
```

## 6. Verification

The registered `PHASE PRE-G9B-R6-PLUS-F1` and the acceptance `STATIC` and
`INFRA_UNIT` run on the frozen candidate commit (`BOUNDED_PHASE`); their
results are reported outside this file, which cannot name its own commit.
Development checks on the working tree before the commit: the F1 class 14
tests, 0 failures; the 13 pin and regression classes and four guide classes
all passing; Checkstyle (Desktop main and test) with no finding in an F1 file
(one pre-existing warning in `PreG9BR6PlusA1HiddenLayerTest`); development
`INFRA_UNIT` `verification-0da1faa7678246af931088420b525841` and `STATIC`
`verification-d80eecaa7ed648f38a08f199bab0ce64` `ACCEPTED` / `COMPLETE`
(`STATIC` with the standing governance finding and historical-consistency
`UNAVAILABLE`); `git diff --check` clean.

## 7. Focused author smoke

Not performed by the agent.

1. Start GeoCeDG: the option is OFF; **View → Show orientation cue** is
   unchecked and no arrowhead is drawn.
2. Turn it on from the View menu; open **Options → Preferences… → Layout &
   Presentation**: the orientation-cue checkbox is checked. Turn it off there:
   the View menu item is unchecked and the arrowheads disappear. Turn it on again.
3. Build `A`, `B`, `P`, `v`: `Line(A,B)`, `Segment(A,B)`, `Ray(A,B)`,
   `Ray(A,v)`, `Line(P,v)`, a parallel and a perpendicular through `P`, a
   perpendicular bisector, `Reflect` and `Rotate` of a line. Each shows one small
   hollow arrowhead in its colour, pointing from `A` to `B` (or along `v`, or by
   the inherited rule).
4. `Line(B,A)`, `Segment(B,A)`: the arrowhead reverses. Drag `B` across `A` and
   back: the arrowhead disappears while the line is undefined and points along
   the current `B − A` afterwards, without smoothing.
5. `y = 2x + 1`, `2x − y + 1 = 0`, a tangent to a circle at a point or from an
   external point, `AngleBisector(A,B,C)`, `Tangent(1, f)`: no arrowhead.
6. Hide an object, hide its layer, set a segment's thickness to 0 or give it a
   midpoint arrow decoration: no arrowhead. An endpoint arrow style keeps its
   arrowhead.
7. Pan, zoom and resize the window: arrowheads move with the objects, keep their
   size and never reverse.
8. Open Graphics 2 with the same objects: no arrowhead there.
9. Export PNG, PDF and SVG, copy the Graphics view to the clipboard, print
   preview: no arrowhead anywhere.
10. Close and restart GeoCeDG: the option is still on. **File → New** and
    **File → Open**: the option is unchanged; the document is not marked
    modified and **Undo** has nothing new to undo.
11. Repeat 2–3 in Spanish (**Vista → Mostrar indicación de orientación**).

## 8. Final state

```text
PRE-G9B-R6-plus-F1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTHOR_SMOKE       = PENDING
PUBLICATION        = NOT AUTHORIZED
F2 / F3 / F4 / G   = NOT AUTHORIZED
selfApproved       = false
```

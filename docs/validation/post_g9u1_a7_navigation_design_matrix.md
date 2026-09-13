# POST-G9U1-A7 navigation design and future validation matrix

- Design status: **AUTHOR APPROVED**
- Implementation status: **CORRECTED IMPLEMENTATION CANDIDATE — PENDING AUTHOR SMOKE**
- Base commit: `71483011be9bb9fdfe896f00af0ba8323f9c0834`
- Product phase effect: **BOUNDED IMPLEMENTATION CANDIDATE**
- Product implementation authorized: **true, bounded to the approved design**
- Product code changed: **true, candidate pending author smoke**
- Design authority candidate:
  [A7 navigation design](../architecture/post_g9u1_a7_navigation_design.md)

This matrix preserves characterization and records the implemented candidate
coverage. It does not itself claim author approval of the implementation.
The author-approved design is `98280d81771758258f29bfb80c6d025a192b3dca`;
interactive smoke of initial candidate
`b249a62aa6e0e7e844ec350ccbfd600ae5553b2c` authorized the bounded corrected
evidence below without reopening broader G12.
That candidate's successor `ee0f6acd9ea17541cfef2cb5d9984ab96029a741`
passed automated verification; the second author smoke found missing immediate
validation feedback and non-operative real menu/toolbar activation. The current
successor adds only those two bounded integration regressions.

## 1. Characterization matrix

| ID | Question | Evidence seam | Finding |
|---|---|---|---|
| A7-C01 | Desktop wheel uses cursor point | `EuclidianControllerD.mouseWheelMoved` -> `wrapMouseWheelMoved(x,y,...)` | OBSERVED — inherited unchanged |
| A7-C02 | Wheel anchor reaches affine view transform | `wrapMouseWheelMoved` -> `setAnimatedCoordSystem`; `CoordSystemAnimation` preserves `s + (o-s)f` | ESTABLISHED BY SOURCE |
| A7-C03 | Zoom-tool click is cursor centred | `EuclidianController` modes `MODE_ZOOM_IN` / `MODE_ZOOM_OUT` call `view.zoom(mouseLoc.x,mouseLoc.y,...)` | OBSERVED — inherited unchanged |
| A7-C04 | Pinch is cursor centred | `EuclidianController.onPinch(x,y,...)` calls `zoomInOut(...,x,y)` | OBSERVED — inherited unchanged |
| A7-C05 | Keyboard zoom uses valid cursor | `GlobalKeyDispatcher.getZoomPoint` returns `ec.getMouseLoc()` when non-null | OBSERVED — inherited unchanged |
| A7-C06 | Mouse exit/currentness can be represented safely | `wrapMouseExited` normally clears `mouseLoc`, but its text-field-focus early return can leave it non-null | PARTIAL — implementation needs an explicit view-local validity flag invalidated before that early return |
| A7-C07 | Keyboard centre fallback is correct | fallback returns `(width/2,width/2)` | DIVERGENCE — Y is wrong for non-square views; future bounded correction required |
| A7-C08 | Scale degeneracy is bounded | `EuclidianView.setCoordSystem` rejects NaN and scales outside kernel precision bounds | OBSERVED — reuse, do not widen |
| A7-C09 | Existing ZoomWindow is a view operation | `GeoCeDGEuclidianController` selection rectangle -> `setAnimatedRealWorldCoordSystem` | ESTABLISHED; creates no `GeoElement` |
| A7-C10 | Existing ZoomWindow has direct configurable key | only DXF receives `Action.ACCELERATOR_KEY`; ZoomWindow receives none | MISSING |
| A7-C11 | Existing ZoomWindow targets active supported view | `GeoCeDGActionRegistry.controller()` always returns view 1 controller | PARTIAL — primary view only; must fail safe when another view is active |
| A7-C12 | Preference layer exists | isolated GeoCeDG properties file and `GeoGebraPreferencesD.load/savePreference`; workspace uses same profile-preference seam | ESTABLISHED |
| A7-C13 | Host offers a general configurable shortcut registry | global shortcuts are switch-based in `GlobalKeyDispatcher`; menu/root input bindings are separate | NOT AVAILABLE; bounded A7 policy required |
| A7-C14 | Classic is isolated from GeoCeDG action policy | `AppGeoCeDG`/`GuiManagerGeoCeDG` construct product actions; Classic uses inherited app/menu/controller | ESTABLISHED |

## 2. Corrected behavioral tests

| ID | Required scenario | Minimum assertion | Preferred layer |
|---|---|---|---|
| A7-T01 | cursor-anchored zoom in | world point under cursor maps to same screen point after zoom | shared view/controller test |
| A7-T02 | cursor-anchored zoom out | inverse-factor path preserves the same anchor | shared view/controller test |
| A7-T03 | repeated zoom | no systematic anchor drift beyond transform/display precision | shared deterministic test |
| A7-T04 | no geometric side effects | construction count/DAG, coordinates, durable IDs and representative Locus V2 address are unchanged | shared + GeoCeDG Desktop |
| A7-T05 | existing ZoomWindow reuse and fit | finalization calls the G9U1 rectangle real-world seam, contains wide/tall selections and preserves `xScale / yScale` | Desktop controller test |
| A7-T06 | explicit and configured action dispatch | Navigation exposes distinct ZoomWindow plus `navigation.zoom-factor-in/out`; inherited `+/-` is unchanged | shared + Desktop action/input test |
| A7-T07 | application preference persistence | factor 10 and two initially unbound chords survive preference reload, remain absent from document XML and reset to defaults | Desktop preference test |
| A7-T08 | live atomic conflict/validation UX | every chord immediately reports Available/Unassigned, Invalid or its conflicting action; Apply follows that same typed result and remains disabled until the whole draft is valid; prior preferences remain unchanged | Desktop panel/policy/dialog-state test |
| A7-T09 | no/stale cursor | keyboard zoom uses `(width/2,height/2)`; after exit (including text-field focus), re-entry without movement or workspace replacement, ZoomWindow enters `WAIT_FOR_DRAG` without fabricated extent | shared + Desktop test |
| A7-T10 | Classic regression | ordinary Classic bindings and cursor zoom remain unchanged except corrected true-centre fallback | Desktop Classic regression |
| A7-T11 | old-file save/reopen | no shortcut or semantic association is manufactured; view presentation remains host-compatible | Desktop round trip |
| A7-T12 | extreme supported scales | finite deterministic transform at both supported sides; out-of-range proposal is rejected | shared view test |
| A7-T13 | valid cursor ZoomWindow activation | current primary-view cursor becomes first corner; next click uses existing finalization | Desktop controller test |
| A7-T14 | cancel paths | Escape in WAIT_FOR_DRAG or preview, right-click, focus loss and tool change clear state and change neither view nor Construction | Desktop controller/key test |
| A7-T15 | unsupported active view | action never silently operates on view 1 when secondary 2D/3D is active | Desktop action test |
| A7-T16 | DPI/viewport independence | changing component size/DPI affects only view mapping, not geometry/metrics/identity | shared + semantic regression |
| A7-T17 | configured factor reciprocity | factor In multiplies both scales, Out divides them, both preserve current cursor/true-centre anchor, and In+Out restores the transform | Desktop controller/action test |
| A7-T18 | real menu/toolbar ZoomWindow wiring | the actual menu item and profile-flyout item share one registry action, defer arming past focus/mode notifications, consume the next primary rectangle, reach G9U1 finalization and preserve Escape cancellation | Desktop menu/toolbar/controller integration test |

Tests must compare model state independently from serialized view presentation.
Pixel screenshots and painting timing are not acceptance authority.

## 3. Planned implementation and verification perimeter

The implementation candidate introduces one additive phase selector:

```text
PHASE_ID = POST-G9U1-A7
EXPECTED_DEVELOPMENT_PROFILE = focused shared/Desktop JUnit + checkstyle
EXPECTED_FINAL_ACCEPTANCE = PHASE -Phase POST-G9U1-A7
INTEGRATION = not required absent a concrete uncovered obligation
FINAL = not required absent a concrete uncovered obligation
```

The selection contains the focused A7 shared and Desktop classes, the existing
G9U1 ZoomWindow regression, the Classic keyboard regression and the minimum
semantic-purity regression. Inventory/registry additions require `INFRA_UNIT`
under current governance.

`PostG9U1A7CursorZoomTest` proves the inherited wheel and keyboard affine anchor,
repeated operations, finite supported extreme scales and the corrected
non-square Classic fallback. `PostG9U1A7NavigationTest` proves fresh/stale and
focus-loss cursor lifecycle, Escape cancellation, ratio-preserving wide/tall
ZoomWindow fits, unchanged construction/semantic identity, factor-10 defaults,
reciprocal factor actions, preference reload, document-XML exclusion, atomic
in-panel validation, primary-view containment and G9U1 action reuse. The Desktop
selection also retains `G9U1SemanticPointInteractionTest`, `GeoCeDGProfileTest`,
`G9U1ProfileCompilerTest` and `G9U1WorkspaceSurfaceTest` as direct rectangle,
schema/catalog and menu/toolbar regressions. Final PHASE evidence remains
candidate-bound and does not imply author approval.

## 4. Scope and non-authorities

Deferred: `ZoomPrevious`, `FitSelection`, `FitLayer`, named views, general scale
profiles, printing scale, general navigation redesign, secondary-view
ZoomWindow, 3D camera and Web.

Screen position, shortcut token, viewport, zoom, camera, DPI, toolbar/menu
placement and serialized presentation are never authority for coordinates,
metric truth, incidence, semantic identity, provenance, projection identity,
spatial reconstruction or DAG membership.

## 5. Author repeat-smoke checklist

1. Edit both factor shortcuts and confirm Available/conflict feedback changes
   before pressing Apply; Apply remains disabled for an invalid whole draft.
2. Choose **View → Navigation → Zoom to rectangle** and confirm the next normal
   rectangle drag is consumed by ZoomWindow.
3. Choose ZoomWindow from the toolbar flyout and confirm the same interaction.
4. Complete a wide/tall rectangle and confirm containment without X/Y stretching.
5. Activate from menu and toolbar, then press Escape before/during drag; confirm
   no viewport change and restoration of ordinary interaction.

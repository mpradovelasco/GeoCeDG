# POST-G9U1-A7 navigation design and future validation matrix

- Design status: **AUTHOR APPROVED**
- Implementation status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `71483011be9bb9fdfe896f00af0ba8323f9c0834`
- Product phase effect: **NONE**
- Product implementation authorized: **true, bounded to the approved design**
- Product code changed: **true, candidate pending author review**
- Design authority candidate:
  [A7 navigation design](../architecture/post_g9u1_a7_navigation_design.md)

This matrix preserves characterization and records the implemented candidate
coverage. It does not itself claim author approval of the implementation.

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

## 2. Future behavioral tests

| ID | Required scenario | Minimum assertion | Preferred layer |
|---|---|---|---|
| A7-T01 | cursor-anchored zoom in | world point under cursor maps to same screen point after zoom | shared view/controller test |
| A7-T02 | cursor-anchored zoom out | inverse-factor path preserves the same anchor | shared view/controller test |
| A7-T03 | repeated zoom | no systematic anchor drift beyond transform/display precision | shared deterministic test |
| A7-T04 | no geometric side effects | construction count/DAG, coordinates, durable IDs and representative Locus V2 address are unchanged | shared + GeoCeDG Desktop |
| A7-T05 | existing ZoomWindow reuse | finalization calls the existing rectangle real-world seam; no second rectangle transform exists | Desktop controller test |
| A7-T06 | configured action dispatch | accepted chord invokes the one `navigation.zoom-window` action and no other action | Desktop action/input test |
| A7-T07 | application preference persistence | binding survives app restart/reload, is absent from document/construction XML and reset returns to unbound | Desktop preference test |
| A7-T08 | conflict policy | GeoCeDG, menu/root and inherited-reserved conflicts are rejected without changing the prior binding | Desktop policy test |
| A7-T09 | no/stale cursor | keyboard zoom uses `(width/2,height/2)`; after exit (including text-field focus), re-entry without movement or workspace replacement, ZoomWindow enters `WAIT_FOR_DRAG` without fabricated extent | shared + Desktop test |
| A7-T10 | Classic regression | ordinary Classic bindings and cursor zoom remain unchanged except corrected true-centre fallback | Desktop Classic regression |
| A7-T11 | old-file save/reopen | no shortcut or semantic association is manufactured; view presentation remains host-compatible | Desktop round trip |
| A7-T12 | extreme supported scales | finite deterministic transform at both supported sides; out-of-range proposal is rejected | shared view test |
| A7-T13 | valid cursor ZoomWindow activation | current primary-view cursor becomes first corner; next click uses existing finalization | Desktop controller test |
| A7-T14 | cancel paths | Escape/right-click/focus loss/tool change clear preview and change neither view nor Construction | Desktop controller test |
| A7-T15 | unsupported active view | action never silently operates on view 1 when secondary 2D/3D is active | Desktop action test |
| A7-T16 | DPI/viewport independence | changing component size/DPI affects only view mapping, not geometry/metrics/identity | shared + semantic regression |

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

`PostG9U1A7CursorZoomTest` proves the inherited affine anchor for zoom-in/out,
repeated operations, finite supported extreme scales and the corrected
non-square Classic fallback. `PostG9U1A7NavigationTest` proves fresh/stale and
focus-loss cursor lifecycle, anchored and WAIT_FOR_DRAG ZoomWindow activation,
unchanged construction/semantic identity, the unassigned default, preference
reload, document-XML exclusion, atomic conflict rejection, primary-view
containment and reuse of the G9U1 action. The Desktop selection also retains
`G9U1SemanticPointInteractionTest`, `GeoCeDGProfileTest` and
`G9U1WorkspaceSurfaceTest` as direct ZoomWindow, schema/catalog and
menu/toolbar regressions. Final PHASE evidence remains candidate-bound and does
not imply author approval.

## 4. Scope and non-authorities

Deferred: `ZoomPrevious`, `FitSelection`, `FitLayer`, named views, general scale
profiles, printing scale, general navigation redesign, secondary-view
ZoomWindow, 3D camera and Web.

Screen position, shortcut token, viewport, zoom, camera, DPI, toolbar/menu
placement and serialized presentation are never authority for coordinates,
metric truth, incidence, semantic identity, provenance, projection identity,
spatial reconstruction or DAG membership.

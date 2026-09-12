# POST-G9U1-A7 — cursor-centred navigation characterization and design

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Published base commit: `71483011be9bb9fdfe896f00af0ba8323f9c0834`
- Published base tree: `81e690b68ed7ec2caa3f59ba6d8c6abf90ab3f38`
- Product implementation authorized: **false**
- Product code changed: **false**
- Owning programme layer: **G12 view/workspace navigation**
- G9B hard dependency: **false**
- Self approval: **false**

This document characterizes the pinned GeoGebra 5.4.928.0 host and proposes the
smallest A7 implementation slice. It does not authorize that implementation.
The accompanying [validation matrix](../validation/post_g9u1_a7_navigation_design_matrix.md)
defines the future acceptance perimeter.

## 1. Characterized authority

The relevant inherited sources are byte-unchanged from the pinned upstream
commit `9b93256b7df401ff056c37b502d82df4d72b1522`:

- `EuclidianController`, `EuclidianView` and `CoordSystemAnimation` in shared
  common;
- `GlobalKeyDispatcher` in shared common;
- `EuclidianControllerD` and `GeoGebraPreferencesD` in Desktop.

GeoCeDG adds the product-only `AppGeoCeDG`, `GeoCeDGEuclidianController`,
`GeoCeDGActionRegistry`, menu/toolbar projections and isolated preferences file.
G9U1 already added `navigation.zoom-window`; its controller owns only the
gesture state and delegates the actual rectangle-to-view operation to the host.

### 1.1 Ordinary zoom

**OBSERVED.** Desktop wheel events pass their component-local `(x,y)` to
`EuclidianController.wrapMouseWheelMoved`. That method clamps the point through
`setMouseLocation`, chooses a scale factor and calls
`EuclidianView.setAnimatedCoordSystem(px, py, factor, newScale, ...)`.
Zoom-tool clicks and pinch gestures reach the same anchored zoom family.

`EuclidianView.zoom` initializes `CoordSystemAnimation` with the screen anchor.
For scale ratio `f`, the animation changes the screen origin from `o` to

```text
o' = s + (o - s) f
```

and the scale from `q` to `q' = q f`. With
`x = V_before^-1(s)`, substitution in the inherited affine transform gives
`V_after(x) = s`, subject only to floating-point/display precision and the
host's supported-scale guard. The algorithm changes view settings and drawables,
not construction inputs or model coordinates.

**Finding:** inherited wheel, pinch and Zoom In/Out tool behavior already meets
the ordinary cursor-centred invariant. A7 needs contract tests, not a second zoom
algorithm.

### 1.2 Keyboard zoom divergence

**OBSERVED.** `Ctrl`+`+` / `Ctrl`+`-` in `GlobalKeyDispatcher` uses the active
view controller's current `mouseLoc` and calls `zoomInOut`, so it is cursor
centred while the pointer has a valid context. `wrapMouseExited` normally clears
`mouseLoc`; however, its text-field-focus early return can leave the raw field
non-null. Non-null `mouseLoc` is therefore not sufficient currentness evidence.

When no point exists, `getZoomPoint` currently returns
`(view.width / 2, view.width / 2)`. The Y coordinate is therefore wrong for a
non-square view. This is an inherited view-layer defect, not a geometric-kernel
defect. The separately authorized implementation should make the generic,
behaviorally safe correction to `(view.width / 2, view.height / 2)` and cover
Classic as well as GeoCeDG. No GeoCeDG semantic conditional is justified.

### 1.3 G9U1 ZoomWindow

**OBSERVED.** The existing path is:

```text
navigation.zoom-window
  -> GeoCeDGActionRegistry
  -> GeoCeDGEuclidianController.activateZoomWindow()
  -> inherited selection rectangle
  -> EuclidianView.setAnimatedRealWorldCoordSystem(...)
  -> CoordSystemAnimation.initRW(...)
  -> EuclidianView.setRealWorldCoordSystemVisible(...)
```

The controller converts the two rectangle corners with
`toRealWorldCoordX/Y`; it creates no `GeoElement`. Rectangles below 10 by 10
screen units and tool-change cancellation do not change the view. The action is
present in the single profile action catalog and projected into menu and toolbar.

The current action always obtains the controller of Euclidian view 1 and has no
direct configurable `Action.ACCELERATOR_KEY`. “Keyboard-accessible” in the G9U1
surface is therefore the existing menu route plus inherited zoom shortcuts, not
the direct configurable ZoomWindow chord planned by A7.

## 2. Selected bounded contract

```text
A7_INCLUDED = [
  inherited cursor-centred wheel/pinch/Zoom-In/Zoom-Out behavior made explicit
    and regression tested,
  correct center fallback for inherited keyboard zoom when no cursor is valid,
  one configurable direct binding for the existing navigation.zoom-window action,
  optional current-cursor first-corner capture for that action in the supported
    primary 2D GeoCeDG view
]

A7_DEFERRED_TO_BROADER_G12 = [
  ZoomPrevious,
  FitSelection,
  FitLayer,
  named views,
  general scale profiles and extreme-scale expansion,
  drawing/printing scale,
  general keyboard-navigation redesign,
  secondary 2D-view and 3D-camera ZoomWindow integration
]
```

No public geometry command, construction object, semantic address, spatial
binding or persistence schema is added.

## 3. Cursor and transform invariants

For every admitted anchored zoom event:

```text
s = valid screen point in the target Euclidian view
x = V_before^-1(s)
V_after(x) = s
```

The comparison tolerance is derived from the current affine transform and
floating-point/display precision; it is not a geometry tolerance. Repetition
must not create systematic anchor drift beyond that bound.

The existing `EuclidianView.setCoordSystem` supported-scale interval remains
authoritative. A proposed scale outside that finite interval is rejected by the
existing guard; A7 does not clamp geometry, expand numerical range or invent an
approximate model result.

View actions may update document presentation state and the application's
unsaved marker according to inherited behavior. They must not change:

- construction membership, inputs or DAG edges;
- object values or durable identity;
- Locus V2 branches, components or semantic addresses;
- metric/intersection results or spatial/projection bindings.

## 4. ZoomWindow keyboard gesture

The stable action identity remains `navigation.zoom-window`. A keyboard chord is
only a configurable presentation binding to that action.

When the action is invoked while the primary GeoCeDG 2D view has a non-null,
in-bounds current `mouseLoc`, the future implementation may capture that point
as the first rectangle corner in a bounded `KEYBOARD_ANCHORED` gesture state.
Pointer movement previews the same inherited selection rectangle; the next
primary click completes the second corner through the existing G9U1
rectangle-to-view seam. Escape, right-click, focus/view loss or a tool change
cancels without changing the view or Construction.

Valid cursor context is controller-local presentation state: a real enter/move
event in the active primary GeoCeDG view establishes it, while view exit (before
the inherited focus-sensitive early return), view disposal, workspace/document
replacement or unsupported-view activation invalidates it. A stale non-null
`mouseLoc` never reactivates the context.

When no valid cursor context exists, the action uses the existing explicit
`WAIT_FOR_DRAG` behavior: it arms ZoomWindow and waits for the next genuine
press-drag-release to establish both corners. It does **not** manufacture a
rectangle around the view centre or query the OS pointer. This fallback is
visible interaction state, not hidden geometric authority.

Menu and toolbar invocation naturally have no valid view cursor after pointer
exit in the normal Desktop lifecycle and therefore retain the G9U1 drag flow.
The action must not distinguish semantic behavior from Swing event source.

The bounded implementation supports the primary GeoCeDG Graphics view only.
If another Euclidian view is active, the action is unavailable/no-op rather than
silently changing view 1. Secondary 2D and 3D-camera integration remain G12.

## 5. Keyboard/configuration contract

### 5.1 Action, default and storage

Only the direct binding for `navigation.zoom-window` is configurable in A7.
Inherited `Ctrl`+`+` / `Ctrl`+`-`, menu mnemonics and other host shortcuts keep
their meanings.

The factory default is **unbound**. The existing keyboard menu route remains
available, while A7 does not invent an author-unselected global chord. A user may
assign exactly one key-pressed chord with explicit modifiers through a bounded
GeoCeDG setting. Reset removes the preference and returns to unbound.

The binding is stored under the isolated GeoCeDG `GeoGebraPreferencesD` file as
an application/profile preference, using a versioned canonical representation
of key code plus normalized modifiers. It is not written to `.ggb`/`.cedg`,
workspace layout XML or Construction XML. The runtime projects the accepted
binding onto the existing Swing `Action.ACCELERATOR_KEY`; menu and any other
projection continue to consume the same action object.

### 5.2 Conflict and validity policy

Configuration is accepted only when the normalized chord is not already owned
by:

1. a GeoCeDG action accelerator;
2. a current menu/root-pane input binding; or
3. an inherited non-enumerable `GlobalKeyDispatcher` shortcut in the pinned host
   baseline's audited reserved set.

Conflict, invalid key code, modifier-only input, unsupported typed-character
binding, AltGraph/locale-ambiguous input, or an unavailable platform modifier is
rejected atomically. The prior valid binding remains active; no command is
overridden. The same policy is applied on preference load. An invalid/stale
stored value yields the unbound default and a presentation diagnostic.

The small audited reserved set is a compatibility boundary necessitated by the
host's switch-based global dispatcher, not a new general shortcut registry. A
future upstream baseline change must re-audit it. A7 does not redesign all host
keyboard handling.

## 6. Persistence and compatibility

| State | Owner | A7 rule |
|---|---|---|
| zoom factor, origin, viewport | view/document presentation as inherited | may save/reopen as presentation; never geometric authority |
| ZoomWindow armed/anchored preview | controller/session | transient; never serialized |
| direct ZoomWindow chord | GeoCeDG application/profile preference | survives restart; absent in old files; resettable |
| workspace layout | existing GeoCeDG workspace preference/document presentation | unchanged |
| construction and CeDG identities | shared geometric/semantic kernel | untouched |

Classic continues to use inherited actions and preferences. The generic
non-square-centre correction is safe for Classic because it fixes the stated
centre fallback without adding GeoCeDG policy. Older `.ggb`/`.cedg` files need no
migration and cannot acquire semantic associations. Files do not carry the A7
shortcut. The G9U1 ZoomWindow action and rectangle path remain the only
ZoomWindow implementation.

## 7. Proposed implementation seams

The separately authorized implementation should be limited to:

1. `GlobalKeyDispatcher.getZoomPoint`: correct the no-cursor Y centre and add a
   focused shared regression test;
2. a small GeoCeDG Desktop navigation-preference/policy component using
   `GeoGebraPreferencesD`, not a new configuration subsystem;
3. `GeoCeDGActionRegistry`: apply the accepted chord to the existing action and
   dispatch only to the supported active primary view;
4. `GeoCeDGEuclidianController`: add explicit cursor-context validity plus the
   bounded keyboard-anchored gesture state while retaining the existing
   rectangle finalization seam;
5. the existing profile catalog/menu settings projection and localization for
   one bounded shortcut setting;
6. focused shared/Desktop tests and an additive `POST-G9U1-A7` PHASE selection.

No Java geometric-kernel change is required. One shared/common **view-input**
correction is required for the inherited keyboard fallback; all A7-specific
configuration and gesture work remains GeoCeDG Desktop. Web and 3D do not change.

## 8. Rejected alternatives

- A second rectangle-to-world algorithm: rejected because G9U1 already owns the
  exact seam.
- OS-global pointer lookup: rejected because it is unreliable across focus,
  multiple monitors, headless tests and view ownership.
- Centre-manufactured ZoomWindow rectangle: rejected because no second corner
  or extent has authority.
- Storing shortcuts in document XML: rejected because bindings are application
  preferences, not document semantics.
- Silent conflict override or “last binding wins”: rejected as nondeterministic.
- A general shortcut editor/registry or all-view navigation rewrite: deferred to
  broader G12.
- Kernel objects or dependencies for view navigation: rejected because no
  geometric semantics require them.

## 9. Design answers

1. **Inherited cursor centring:** yes for wheel, pinch, zoom-tool clicks and
   keyboard zoom while `mouseLoc` is valid. The no-cursor keyboard fallback is
   wrong on non-square views because it uses width for both axes.
2. **Authoritative transform seam:** `EuclidianView.setAnimatedCoordSystem` /
   `zoom` backed by `CoordSystemAnimation`; no new mathematics.
3. **ZoomWindow seam:** the G9U1 `setAnimatedRealWorldCoordSystem` path listed in
   section 1.3.
4. **Included actions:** existing ordinary anchored zoom plus configurable
   activation of existing ZoomWindow. The broader G12 list in section 2 remains
   deferred.
5. **Keyboard scope:** one optional direct chord for the stable ZoomWindow action
   and correction of the inherited no-cursor centre fallback.
6. **Persistence:** isolated GeoCeDG application/profile preferences.
7. **Conflict:** reject atomically and retain the previous valid binding; never
   override silently.
8. **No cursor:** keyboard zoom uses the true view centre; ZoomWindow waits for a
   real drag rather than fabricating an anchor/extent.
9. **Shared/common changes:** only the view-input centre correction; no semantic
   or geometric-kernel changes.
10. **Kernel boundary:** yes, A7 remains entirely outside geometric semantics.
11. **No-side-effect evidence:** compare construction membership/DAG, object
    coordinates, durable IDs and representative Locus V2 addresses before/after
    every navigation path, independently of view XML.
12. **Smallest future slice:** the six seams in section 7, one bounded PHASE,
    Classic 5 Desktop only, pending separate author authorization.

## 10. ADR and unresolved questions

`ADR_REQUIRED = false`. The design specializes existing view, action and
application-preference contracts; it introduces no durable cross-frontend
semantic decision.

No architectural blocker remains. Exact localized labels and the concrete
allowed-key capture widget are implementation details constrained by section 5,
not open semantic choices. Secondary views, Web and 3D remain explicitly
unsupported by this slice rather than unresolved.

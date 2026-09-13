# POST-G9U1-A7 — cursor-centred navigation characterization and design

- Design status: **AUTHOR APPROVED**
- Implementation status: **CORRECTED IMPLEMENTATION CANDIDATE — PENDING AUTHOR SMOKE**
- Published base commit: `71483011be9bb9fdfe896f00af0ba8323f9c0834`
- Published base tree: `81e690b68ed7ec2caa3f59ba6d8c6abf90ab3f38`
- Product implementation authorized: **true, bounded to this design**
- Product code changed: **true, implementation candidate**
- Owning programme layer: **G12 view/workspace navigation**
- G9B hard dependency: **false**
- Self approval: **false**

This document characterizes the pinned GeoGebra 5.4.928.0 host and governs the
smallest A7 implementation slice. Design approval authorized the bounded
implementation, but neither design nor automated verification approves its result.
The accompanying [validation matrix](../validation/post_g9u1_a7_navigation_design_matrix.md)
defines the future acceptance perimeter.

The author approved the original design at
`98280d81771758258f29bfb80c6d025a192b3dca`. The first implementation
candidate `b249a62aa6e0e7e844ec350ccbfd600ae5553b2c` established the cursor
currentness and non-square fallback foundations. Its interactive smoke exposed
three bounded corrections: explicit ratio-preserving ZoomWindow behavior,
Escape cancellation and in-dialog validation, plus replacement of the proposed
ZoomWindow chord by two configurable factor-zoom actions. This successor keeps
the original characterization as history and records the author-authorized
correction below.

The corrected candidate `ee0f6acd9ea17541cfef2cb5d9984ab96029a741`
passed its automated A7 verification. A second author smoke then found two
bounded integration defects: shortcut validity was visible only after Apply,
and the real menu/toolbar projections did not leave an operative ZoomWindow
gesture armed. The successor correction keeps the approved behavior but uses
one live typed draft-validation result for presentation and Apply, and arms the
shared controller action after Swing focus/mode selection has settled. That
successor, `ff45382d23d8e7b8d6f742a6d449d67c57daac43`, also passed automated
verification, but repeated interactive smoke established that its deferred arm
was still cancelled by a later canvas focus-loss transition and that a long
conflict label could collapse the editors. The present bounded successor records
the corrected persistent interaction and layout contracts without reopening G12.

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
  -> the shared GeoCeDG menu/toolbar Action
  -> GeoCeDGActionRegistry (invalidate stale cursor, prepare Move synchronously)
  -> GeoCeDGEuclidianController.activateZoomWindow()
  -> persistent product-local interactive state
  -> inherited selection rectangle
  -> EuclidianView.setAnimatedRealWorldCoordSystem(...)
  -> CoordSystemAnimation.initRW(...)
  -> EuclidianView.setRealWorldCoordSystemVisible(...)
```

The controller converts the two rectangle corners with
`toRealWorldCoordX/Y`; it creates no `GeoElement`. Rectangles below 10 by 10
screen units and tool-change cancellation do not change the view. The action is
present in the single profile action catalog and projected into menu and toolbar.

The first A7 candidate kept this explicit action and proposed a direct
configurable chord. The author smoke retained the distinct action but redirected
the configurable keyboard scope to factor In/Out. ZoomWindow itself remains
reachable through the Navigation menu and toolbar; it is not conflated with the
inherited zoom tools.

Menu and toolbar are projections of the same registry `Action`. The UI path
invalidates only the stale canvas cursor, synchronously selects the inherited
Move mode as internal preparation, and then enters one persistent action-backed
interaction state. Focus transfer caused by the activating menu/toolbar control
does not cancel that state. The next primary-button rectangle is therefore
consumed by the product controller rather than Pan/selection. Completion,
Escape, or a later external tool change cancels the state and updates the shared
Action/toolbar selection; no timing, retry, timer or second host mode is used.

## 2. Selected bounded contract

```text
A7_INCLUDED = [
  inherited cursor-centred wheel/pinch/Zoom-In/Zoom-Out behavior made explicit
    and regression tested,
  correct center fallback for inherited keyboard zoom when no cursor is valid,
  the distinct existing navigation.zoom-window action with Escape cancellation
    and ratio-preserving rectangle fit,
  optional current-cursor first-corner capture for that action in the supported
    primary 2D GeoCeDG view,
  one application-preference factor (default 10) and independent configurable
    bindings for navigation.zoom-factor-in and navigation.zoom-factor-out
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

## 4. Explicit GeoCeDG ZoomWindow

The stable action identity remains `navigation.zoom-window`, distinct from the
inherited Zoom In/Out tools and `+/-` shortcuts. It remains an explicit GeoCeDG
Navigation menu action and reuses the G9U1 rectangle interaction.

When the action is invoked while the primary GeoCeDG 2D view has a non-null,
in-bounds current `mouseLoc`, the future implementation may capture that point
as the first rectangle corner in a bounded `KEYBOARD_ANCHORED` gesture state.
Pointer movement previews the same inherited selection rectangle; the next
primary click completes the second corner through the existing G9U1
rectangle-to-view seam. Escape, right-click, focus/view loss or a tool change
cancels without changing the view or Construction.

Finalization expands the selected world-coordinate rectangle, when necessary,
so that it fits the non-square viewport while preserving the pre-operation
`xScale / yScale` ratio. The expanded bounds are passed to the same inherited
`setAnimatedRealWorldCoordSystem` seam. Unused horizontal or vertical margin is
valid; independent axis stretching is not. Escape reaches the normal Desktop
key/mode cancellation path in both `WAIT_FOR_DRAG` and an active preview, clears
the transient rectangle and leaves the view transform unchanged.

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

## 5. Factor-zoom keyboard/configuration contract

### 5.1 Action, default and storage

Inherited `Ctrl`+`+` / `Ctrl`+`-`, wheel/pinch, Zoom In/Out tools and menu
mnemonics keep their meanings. A7 adds two distinct product actions:
`navigation.zoom-factor-in` and `navigation.zoom-factor-out`. Both use one
finite factor `F > 1`, default **10**. In multiplies both view scales by `F`;
Out divides them by `F`. They preserve the current `xScale / yScale` relation
and use the current valid cursor as affine anchor, or the true active-view
centre when no current cursor exists. Consecutive In/Out at the same anchor are
reciprocal within view-transform floating-point precision.

Each factor action has an independent, initially unbound key-pressed chord with
explicit modifiers. The factor and both chords are stored in the isolated
GeoCeDG `GeoGebraPreferencesD` file as application/profile preferences. They are
not written to `.ggb`/`.cedg`, workspace layout XML or Construction XML. Runtime
projects accepted chords onto the two existing Swing actions.

### 5.2 Conflict and validity policy

The complete factor/two-chord proposal is validated before any value is
persisted. Configuration is accepted only when each normalized chord is not already owned
by:

1. a GeoCeDG action accelerator;
2. a current menu/root-pane input binding; or
3. an inherited non-enumerable `GlobalKeyDispatcher` shortcut in the pinned host
   baseline's audited reserved set.

The proposed chords must also be distinct. Conflict, invalid factor, invalid key
code, modifier-only input, unsupported typed-character
binding, AltGraph/locale-ambiguous input, or an unavailable platform modifier is
rejected atomically. The same typed draft result drives immediate per-field
Available/Unassigned, Invalid or conflict-with-action feedback, Apply enablement
and final persistence. The dialog stays open so the draft can be corrected. The
prior valid configuration remains active; Cancel writes nothing and no command
is overridden. Invalid stored values fail to the documented factor/unbound defaults.

The small audited reserved set is a compatibility boundary necessitated by the
host's switch-based global dispatcher, not a new general shortcut registry. A
future upstream baseline change must re-audit it. A7 does not redesign all host
keyboard handling.

## 6. Persistence and compatibility

| State | Owner | A7 rule |
|---|---|---|
| zoom factor, origin, viewport | view/document presentation as inherited | may save/reopen as presentation; never geometric authority |
| ZoomWindow armed/anchored preview | controller/session | transient; never serialized |
| factor and two factor-zoom chords | GeoCeDG application/profile preference | survive restart; absent in old files; resettable to 10/unbound |
| workspace layout | existing GeoCeDG workspace preference/document presentation | unchanged |
| construction and CeDG identities | shared geometric/semantic kernel | untouched |

Classic continues to use inherited actions and preferences. The generic
non-square-centre correction is safe for Classic because it fixes the stated
centre fallback without adding GeoCeDG policy. Older `.ggb`/`.cedg` files need no
migration and cannot acquire semantic associations. Files do not carry the A7
factor or shortcuts. The G9U1 ZoomWindow action and rectangle path remain the only
ZoomWindow implementation.

## 7. Proposed implementation seams

The separately authorized implementation should be limited to:

1. `GlobalKeyDispatcher.getZoomPoint`: correct the no-cursor Y centre and add a
   focused shared regression test;
2. a small GeoCeDG Desktop navigation-preference/policy component using
   `GeoGebraPreferencesD`, not a new configuration subsystem;
3. `GeoCeDGActionRegistry`: dispatch the explicit ZoomWindow and two factor
   actions only to the supported active primary view;
4. `GeoCeDGEuclidianController`: add explicit cursor-context validity plus the
   bounded keyboard-anchored gesture state while retaining the existing
   rectangle finalization seam;
5. the existing profile catalog/menu settings projection and localization for
   one bounded factor/two-shortcut setting;
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
4. **Included actions:** inherited ordinary anchored zoom, explicit ZoomWindow,
   and the two configured factor actions. The broader G12 list remains deferred.
5. **Keyboard scope:** independent optional chords for factor In/Out plus the
   inherited no-cursor centre correction; inherited `+/-` remains unchanged.
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
12. **Bounded implementation:** the six seams in section 7, one bounded PHASE,
    Classic 5 Desktop only; the corrected candidate remains pending author smoke.

## 10. ADR and unresolved questions

`ADR_REQUIRED = false`. The design specializes existing view, action and
application-preference contracts; it introduces no durable cross-frontend
semantic decision.

No architectural blocker remains. Exact localized labels and the concrete
allowed-key capture widget are implementation details constrained by section 5,
not open semantic choices. Secondary views, Web and 3D remain explicitly
unsupported by this slice rather than unresolved.

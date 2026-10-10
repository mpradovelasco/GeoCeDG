# PRE-G9B-R6-plus-F1 — orientation cue: reconciled design candidate

- Status: **PROPOSED — RECONCILED CANDIDATE — NOT AUTHOR APPROVED — NOT NORMATIVE**
- Produced by: `PRE-G9B-R6-plus-F1-PREP` (author instruction of 2026-10-11;
  characterization and design preparation only)
- Base: published `main` `bde2827bb21e4a28e080db6a65b4abc892071cf8`, tree
  `1b74102cea5151b8a6c571cb36b96b33af6fb3c8`
- Evidence: [F1 preparation characterization report](../validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md)
  (sections `K0`–`K17`) and its JSON mirror
- Decisions: [F1 author-decision preparation record](../validation/pre_g9b_r6_plus_f1_author_decision_preparation_record.md)
  (`DQ-F1-1` to `DQ-F1-9`, all **pending**)
- Supersedes as working design, without modifying it: the `P0`
  [F1 design candidate](pre_g9b_r6_plus_f1_orientation_design_candidate.md),
  which stays historical evidence
- Claim vocabulary: **proposed**. Every value below that is not marked
  AUTHOR-FIXED or INHERITED is a proposal until the author disposes of the
  matching `DQ-F1-*`. This document is not a specification; the first
  deliverable of an authorized `F1` turns the approved parts into one.

Path abbreviations as in the characterization report.

## 1. Author-fixed scope and inherited contracts

AUTHOR-FIXED (mini-track plan §2): an optional, discreet presentation cue,
preferably a hollow arrow, for the actual direction the kernel uses for a
line, segment or ray; derived from kernel semantics, never from the viewport;
no change of geometry or command behavior.

INHERITED: `AGENTS.md` §4 (presentation belongs outside the kernel unless
semantics require otherwise), §17 (object existence, geometric visibility and
layer/UI visibility are not conflated); `unit-system.md` §17.2 state classes;
`verification-levels.md` §12.8 classes; the `A-2` layer workspace (hidden
layers are not painted or hittable).

## 2. Invariants (proposed, every option)

```text
Construction geometry   : unchanged
DAG                     : unchanged (no new algorithm, input or output)
Object identity         : unchanged (no GeoElement is created, replaced or relabelled)
Serialization           : unchanged (document XML, macro XML and preferences XML byte-identical)
Geometric algorithms    : unchanged
Command behavior        : unchanged (no command, alias or syntax)
Hit testing, selection  : unchanged
Undo / redo             : no undo point; cue state is never in an undo snapshot
Screen presentation     : optional derived cue, recomputed from current state at paint time
Classic / upstream      : unchanged behavior (GeoCeDG-owned Desktop code only)
```

## 3. Semantic direction

**3.1 Accessor.** The only source of the sense is `GeoLine.getDirection`
(`(y, −x)`, `common/kernel/geos/GeoLine.java:444-457`), which every kernel
consumer shares (path parameters, `UnitVector`/`Direction`, oriented `Angle`;
report §K3). `getDirectionInD3`, `DrawLine`'s screen point order, endpoints
order on screen, labels, indices and screen coordinates are never used to
choose the sense.

**3.2 Route classes.** The report §K6 classifies routes as `O-CONSTRUCTIVE`,
`O-INHERITED`, `O-CONFIGURATION`, `O-REPRESENTATION` and `O-UNKNOWN`. The
classification is computed from the **identity of the parent algorithm class**
(and, for `AlgoMacro`, of the macro-internal algorithm that produced the
corresponding output by position in the macro definition; for an affine
transform, of the source object), never from labels, names, definitions as
text, coordinates or construction indices.

| Class | Algorithms (at the base) |
|---|---|
| `O-CONSTRUCTIVE` | `AlgoJoinPoints`, `AlgoJoinPointsSegment` (incl. polygon edges and segment images), `AlgoJoinPointsRay`, `AlgoRayPointVector`, `AlgoLinePointVector` |
| `O-INHERITED` | `AlgoLinePointLine` (reference not a `GeoFunction`), `AlgoOrthoLinePointLine` (same condition), `AlgoOrthoLinePointVector`, `AlgoLineBisector`, `AlgoLineBisectorSegment`, `AlgoAngularBisectorLines`, `AlgoTangentLine`, `AlgoMirror`, `AlgoRotate*`, `AlgoTranslate`, `AlgoDilate` (class of the source) |
| `O-CONFIGURATION` | `AlgoAngularBisectorPoints`, `AlgoTangentPoint` with the point off the conic |
| `O-REPRESENTATION` | independent `GeoLine`, `AlgoDependentLine`, `AlgoTangentPoint` with the point on the conic, `AlgoTangentFunctionNumber`, `AlgoTangentFunctionPoint`, `AlgoLinePointLine`/`AlgoOrthoLinePointLine` with a `GeoFunction` reference, `AlgoAsymptote`, `AlgoPolarLine` |
| `O-UNKNOWN` | everything else, including 3D line types, lines inside lists and projective transforms |

`AlgoTangentPoint` changes class with the configuration (polar route on the
conic, two-point route off it). Under the recommended option it is excluded in
both branches. If the author admits `O-CONFIGURATION` (option E-C), the branch
has to come from the kernel's own incidence predicate through existing public
API, never from coordinates compared by the overlay; if that needs a change in
shared code, the route stays excluded and the change is reported as an
escalation.

The segment midpoint decorations are `DECORATION_SEGMENT_ONE_ARROW`,
`DECORATION_SEGMENT_TWO_ARROWS` and `DECORATION_SEGMENT_THREE_ARROWS`
(`common/kernel/kernelND/GeoElementND.java:123-127`).

**3.3 Admissibility (`DQ-F1-5`).** Options:

- **E-A — every defined line.** Show the kernel sense on every admitted type.
  Truthful about what the kernel does, but an arrow on `2x − y + 1 = 0` points
  the other way from the same line typed `y = 2x + 1`, which a user can read
  as geometric meaning that does not exist.
- **E-B — constructive and inherited only (recommended).** Fail-closed
  allow-list: `O-CONSTRUCTIVE` and `O-INHERITED` whose sources are themselves
  admitted. `O-CONFIGURATION`, `O-REPRESENTATION` and `O-UNKNOWN`: no cue.
- **E-C — E-B plus configuration.** Adds `O-CONFIGURATION`, accepting the jumps
  at the straight angle and at the conic (report §K7 D12–D18).
- **E-D — E-B plus an explicit parametric exception.** Treats independent lines
  entered in parametric form as constructive. Not recommended: only the display
  form records it and the user can change that form without changing the
  sense (report §K6 (b)).

Macro outputs inherit the class of their inner route under every option.

## 4. Eligibility predicate (proposed, evaluated per view at paint time)

A cue is drawn for a geo `g` in view `V` only if all hold:

1. `g` is a 2D `GeoLine`, `GeoSegment` or `GeoRay` (not `GeoAxis`, not a 3D
   type, not a list member) with a top-level drawable in `V`
   (`V.getDrawableFor(g) != null`);
2. `g.isDefined()`, `g.isEuclidianVisible()`, `g.isVisibleInView(V)`,
   `app.isLayerShown(g.getLayer())` and line thickness > 0;
3. the coefficients are finite and `getDirection` is finite with
   `|(x, y)|` not zero by `DoubleUtil.isZero` (rejects the zero-length segment
   and infinite coefficients, report §K7 D02, D09);
4. the route class is admitted (§3.3);
5. neither axis of `V` is logarithmic;
6. the visible chord (§5) is at least `4·L` long on screen, `L` the arrow
   length;
7. for a segment, none of the three midpoint arrow decorations of §3.2 is set
   — `DQ-F1-7`.

Undefined, degenerate, non-finite, hidden, hidden-layer and not-drawn objects
therefore show nothing. The predicate never changes the object.

## 5. Placement (presentation only; `DQ-F1-4`)

Let `s = normalize(dx·xscale, −dy·yscale)` be the screen image of
`(dx, dy) = getDirection` (the mapping `DrawRay` already uses,
`DrawRay.java:116-142`). `s` places and rotates the arrowhead; it never decides
its sense. All geometry is in screen pixels of `V`; the "visible chord" is the
part of the object inside the view rectangle inset by `L`.

| Object | Anchor (arrow centroid) | Notes |
|---|---|---|
| segment | screen midpoint of the segment if it lies on the visible chord; otherwise the midpoint of the visible chord | P0 proposal kept; the segment label sits 16 px off the midpoint on one side (`DrawSegment.java:178-195`); an overlap is possible and accepted (alternative: the 2/3 point) |
| ray | start point + `3L` along `s` if that point is on the visible chord; otherwise the point of the visible chord nearest the view centre | P0 "near the start" made total for off-screen starts |
| line | the point of the visible chord nearest the view centre (screen-space projection, clamped to the chord) | P0 "visible point nearest the view centre", made exact: the unclamped foot can fall outside the view when the line cuts a corner |

Determinism: the anchor is a pure function of the object's current
coefficients/endpoints, the view transform and size, and `L`. Pan, zoom and
resize move or suppress the cue but never reverse it; non-uniform axis scales
change only the screen angle (report §K12).

## 6. Rendering (`DQ-F1-4`)

- Shape: isosceles triangle, tip forward along `s`, length `L`, half-width `W`,
  in device pixels, independent of zoom (proposed `L = 10 px`, `W = 4 px`,
  multiplied by the view's style scale for HiDPI, like the existing endings).
- Hollow: 1 px outline in the object's colour and line opacity; interior
  filled with the view background colour so the line does not show through
  (alternative: outline only, line visible inside).
- Geometry family: the same triangle as the `ARROW_OUTLINE` ending
  (`DrawSegmentWithEndings.java:282-312`), but that method is `private`, sized
  by line thickness and tied to segment endpoints. Proposed: a small
  GeoCeDG-owned Desktop helper that builds the triangle path; no change to the
  shared drawables. Rejected for `BOUNDED_PHASE`: making the upstream helper
  public.
- No label, no selection highlight of its own, no hit area.

## 7. Architecture seams (`DQ-F1-8`, `DQ-F1-2`, `DQ-F1-3`)

| Seam | Description | Reaches exports | Views | Shared/upstream change | Fit |
|---|---|---|---|---|---|
| **S1 (recommended)** | GV1 overlay in `GeoCeDGEuclidianView.paint` after `super.paint` (`geocedg/GeoCeDGEuclidianView.java:69-112`), iterating the view's drawables | no — no export, print or clipboard route calls `paint` (report §K8) | GV1 | none | `BOUNDED_PHASE` |
| S2 | S1 plus a GeoCeDG GV2 class: `GuiManagerGeoCeDG` overrides `GuiManager3D.newEuclidianView(…, viewId)` | no | GV1, GV2 | none in shared code; GV2 class identity changes (a new Desktop class and its own regression) | bounded, larger test surface |
| S3 | cue inside `DrawLine`, `DrawRay`, `DrawSegment` | **yes** (picture, print, clipboard, CLI); not LaTeX or DXF | every 2D view | shared drawables, upstream files, `modified-files.yml`, Classic gating | escalates (`INTEGRATED_PHASE` at least) |
| S4 | kernel object or command producing arrows | n/a | n/a | kernel, serialization, DAG | **rejected** (violates §2 invariants and the author scope) |

S1 needs no kernel change: the sense is already shared semantics
(`AGENTS.md` §4 does not require a kernel change for a read-only consumer).

## 8. Option state and lifecycle (`DQ-F1-6`, `DQ-F1-9`)

Proposed (P0 key retained as a proposal):

- `USER_PREFERENCE` key `geocedg.view.orientation-cue.v1` in the portable
  GeoCeDG properties, values `shown` / `hidden` (precedent
  `GeoCeDGUnitPreferences`), anything else read as the default, read never
  writes back; a test seam like `useStoreForTesting`.
- Initial default: **hidden** (the cue is optional; existing documents and
  author smoke baselines look unchanged until the user enables it).
- Persists across restarts through the store's normal write on window close or
  Save Settings; not written to document XML, macro XML or preferences XML; not
  an undo step; unaffected by New, Open, document replacement, undo, redo and
  redefinition; shared by all windows of the process (a change repaints every
  `AppGeoCeDG` window).
- UI surface (`DQ-F1-9`): a checkbox action in a menu (catalog 130 → 131 and
  its pins, report §K14), a row in an existing GeoCeDG-owned Layout &
  Presentation panel (no catalog change, no upstream hook), or both.

Rejected: per-object document style (changes documents; `GLOBAL_IMPACT`);
`SESSION` (lost on restart for a preference-like setting) — unless the author
prefers it under `DQ-F1-6`.

## 9. Lifecycle and dynamics

The overlay holds no object reference between paints. On every repaint it
re-reads the view's current drawables and each object's current state, so:
moving inputs, reversing order, redefinition (new object, same label), undo,
redo, New, Open and document replacement need no handling; temporary
invalidity hides the cue and recovery shows it again with the then-current
sense; identity is never touched (report §K7 D05, D08). Repaint triggers are
those of the view; a preference change requests a repaint.

## 10. Validation design (proposed test matrix)

| ID | Obligation |
|---|---|
| `T-F1-SENSE` | every admitted route of report §K4: the cue's world sense equals `getDirection` (positive dot product), via a pure read-only cue model, not pixels |
| `T-F1-REVERSAL` | `Line/Segment/Ray(A,B)` vs `(B,A)`, `Line(P,v)` vs `Line(P,−v)`, parallel to a reversed reference: cue reverses |
| `T-F1-DYNAMIC` | move inputs through the reversal and back; same object identities; no cue while undefined |
| `T-F1-DEGENERATE` | coincident points, zero vector, zero-length segment, `±∞` and `NaN` coefficients, near-zero direction: no cue, no exception |
| `T-F1-DISCONTINUITY` | angle bisector through the straight angle, tangent point crossing the conic: behavior per `DQ-F1-5` |
| `T-F1-ELIGIBILITY` | each class of §3.2 admitted or excluded per `DQ-F1-5`; unknown algorithm excluded; macro inherits; transform inherits |
| `T-F1-VISIBILITY` | hidden object, hidden layer, thickness 0 (polygon edge), not visible in view, auxiliary object, list member, axis: as §4 |
| `T-F1-PLACEMENT` | deterministic anchor for segment/ray/line; pan, zoom, resize, non-uniform scale, off-screen start, corner-cutting line, short chord, log axis |
| `T-F1-VIEWS` | GV1 cue; GV2 per `DQ-F1-2` |
| `T-F1-EXPORT` | with the cue on: PNG, PDF, SVG, EMF, print, clipboard, CLI, PGF/PSTricks/Asymptote and DXF byte-identical to cue off (screen-only) |
| `T-F1-NO-MUTATION` | document XML, undo state, construction order, object styles, layers, labels and selection identical with the cue on and off |
| `T-F1-PREFERENCE` | default, `shown`/`hidden` round trip, invalid value, persistence across a new app instance with the same store, New/Open unaffected, not in document or preferences XML |
| `T-F1-HIT` | hit testing and selection results identical with the cue on and off |
| `T-F1-CLASSIC` | Classic/upstream application: no cue, no preference read |
| `T-F1-PROFILE` | catalog pins per `DQ-F1-9` |
| `T-SMOKE` | author smoke (not by the agent) |

## 11. Verification class (proposal, `DQ-F1-8`)

`BOUNDED_PHASE`, one registered `PHASE` (`PRE-G9B-R6-PLUS-F1`), for S1 with
the screen-only answer of `DQ-F1-3`, a Desktop preference and its UI surface,
no kernel, serialization, export or shared-drawable change. Escalation
triggers, each requiring an author disposition before work continues: S3 or
any export participation; GV2 through S2 (bounded but with a new view class);
a new `AppD`/`OptionsLayoutD` hook (upstream files); per-object persistence
(`GLOBAL_IMPACT`); any kernel change.

## 12. Rejected alternatives

- A kernel arrow object or command (violates the invariants and the author
  scope).
- `getDirectionInD3` or any screen order as the sense (report §K3.3, §K9).
- Inferring the sense or the route from labels, names, captions, definitions as
  text, coordinates, construction indices or visual proximity.
- Reusing `DECORATION_SEGMENT_ONE_ARROW` (document style, serialized, segment
  only).
- Exporting through drawables in a bounded phase.

## 13. Decision table (all pending)

| Id | Topic | Recommendation |
|---|---|---|
| `DQ-F1-1` | global option or per-object | global |
| `DQ-F1-2` | GV1 only or GV1 + GV2 | GV1 only |
| `DQ-F1-3` | screen-only or exported | screen-only |
| `DQ-F1-4` | style, size, colour, placement | §5, §6 |
| `DQ-F1-5` | eligibility per route class | E-B |
| `DQ-F1-6` | preference class, key, default, lifecycle | §8, default hidden |
| `DQ-F1-7` | suppression and overlap rules | §4 |
| `DQ-F1-8` | seam and verification class | S1, `BOUNDED_PHASE` |
| `DQ-F1-9` | UI surface and catalog | see the decision record |

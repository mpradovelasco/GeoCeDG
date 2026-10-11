# PRE-G9B-R6-plus-F1 — orientation cue: reconciled design

- Status: **DESIGN — AUTHOR APPROVED** (author decisions of 2026-10-11 on
  `DQ-F1-1` to `DQ-F1-9` and, at `PRE-G9B-R6-plus-F1-PREP-FINAL`, on
  `OTQ-F1-1` to `OTQ-F1-4`;
  [F1 author-decision record](../validation/pre_g9b_r6_plus_f1_author_decisions_record.md)
  v1.1, which is their authority and prevails over this file). The approval
  concerns this design, **not its implementation**: implementation is
  `NOT AUTHORIZED` and needs a separate explicit author authorization. The
  author authorized publication of the documentary chain that contains this
  file; no technical item is open.
- Not a specification and not an ADR: the durable specification is the first
  deliverable of an authorized F1 (canonical prompt, *Required
  design/specification*).
- Produced by: `PRE-G9B-R6-plus-F1-PREP` (2026-10-11), reconciled and approved
  by `PRE-G9B-R6-plus-F1-PREP-CLOSEOUT` (2026-10-11); no implementation
- Bases: `P_C1` = `bde2827bb21e4a28e080db6a65b4abc892071cf8` (tree
  `1b74102cea5151b8a6c571cb36b96b33af6fb3c8`); preparation candidate
  `T_R6PLUS_F1_PREP` = `732292e6cb9bf3a0f29367e43eb39bdc6b4ad22b` (tree
  `0839dd764ffceefc2fb009155b25f6495becaae1`), whose version of this file is
  the proposal the author decided on
- Evidence: [F1 preparation characterization report](../validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md)
  (`K0`–`K17`, frozen; corrections `ER-1`, `ER-2` in the decision record §4)
- Supersedes as working design, without modifying it: the `P0`
  [F1 design candidate](pre_g9b_r6_plus_f1_orientation_design_candidate.md),
  historical evidence
- Execution contract when authorized: [canonical F1 prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)

Path abbreviations as in the characterization report. Tags: **AUTHOR
DECISION**, **NORMATIVE INHERITANCE**, **IMPLEMENTATION DESIGN** (reviewable,
not an author decision), as in the decision record.

## 1. Scope

AUTHOR-FIXED (mini-track plan §2): an optional, discreet presentation cue,
preferably a hollow arrow, for the actual direction the kernel uses for a
line, segment or ray; derived from kernel semantics, never from the viewport;
no change of geometry or command behavior.

AUTHOR DECISION (`DQ-F1-1`, `DQ-F1-2`, `DQ-F1-3`, `DQ-F1-8`): one global
application presentation option; Graphics View 1 only; screen-only; a
presentation-only overlay in GeoCeDG Desktop Graphics View 1.

NORMATIVE INHERITANCE: `AGENTS.md` §4, §17; `unit-system.md` §17.2;
`verification-levels.md` §12.8; the `A-2` layer workspace.

## 2. Invariants

```text
Construction geometry   : unchanged
DAG                     : unchanged (no new algorithm, input, output or dependency)
Object identity         : unchanged (no GeoElement created, replaced or relabelled)
Serialization           : unchanged (document, macro and preferences XML byte-identical)
Geometric algorithms    : unchanged
Command behavior        : unchanged (no command, alias or syntax)
Shared drawables        : unchanged
Hit testing, selection  : unchanged
Undo / redo             : no undo point; the cue and the option are never in a snapshot
Exports                 : unchanged (print, clipboard, image, LaTeX, DXF, CLI)
Classic / upstream      : unchanged behavior
Screen presentation     : optional derived cue in GV1, recomputed at every paint
```

## 3. Semantic direction and eligibility

**3.1 Authority (AUTHOR DECISION, `DQ-F1-5`).** The sense is
`GeoLine.getDirection` = `(y, −x)` (`common/kernel/geos/GeoLine.java:444-457`),
validated against kernel path parametrization (report `K3.4`) and the
construction semantics of each admitted route (report `K5`).
`getDirectionInD3`, `DrawLine`'s screen point order, labels, names, captions,
definition text, coordinates, construction indices and visual proximity are
never used to choose or classify the sense.

**3.2 Kernel orientation versus intrinsic sense.** A line as a point set has
no sense. The kernel nevertheless uses one sense for every line (path
parameters, `UnitVector`, oriented `Angle`). The cue displays that kernel sense
only where it is **constructively oriented** (the user's ordered inputs define
it) or **demonstrably inherited** (a fixed, source-proven rule carries an
admitted orientation over). Elsewhere the kernel sense exists and is
documented (report `K4`, `K6`), but it may depend on how an equation was typed
or how a conic is normalized, and the cue is suppressed.

**3.3 Eligibility matrix v1 (IMPLEMENTATION DESIGN applying `DQ-F1-5`).**
Classification is by the identity of the geo's class and parent-algorithm
class, and of its orienting inputs, read through existing public API.
"Admitted input" means: a `GeoPoint` (as an ordered input), a `GeoVector`
(oriented by definition), or a `GeoLine`/`GeoSegment`/`GeoRay` that is itself
admitted by this matrix (recursive over the acyclic construction graph,
fail-closed).

Admitted:

| Id | Parent algorithm | Route | Displayed sense | Kind | Evidence |
|---|---|---|---|---|---|
| `A1` | `AlgoJoinPoints` | `Line(A,B)` | `B − A` | constructive | `K4` R01/R02, `K5` |
| `A2` | `AlgoJoinPointsSegment` | `Segment(A,B)`, polygon edges, segment images | start → end | constructive | R03/R04, R17, R18f |
| `A3` | `AlgoJoinPointsRay` | `Ray(A,B)` | `B − A` | constructive | R05 |
| `A4` | `AlgoRayPointVector` | `Ray(A,v)` | `v` | constructive | R06 |
| `A5` | `AlgoLinePointVector` | `Line(P,v)` | `v` | constructive | R07 |
| `A6` | `AlgoLinePointLine`, reference an admitted line/segment/ray | parallel through a point | reference sense | inherited | R08, R19 |
| `A7` | `AlgoOrthoLinePointLine`, reference an admitted line/segment/ray | perpendicular through a point | reference sense rotated +90° | inherited | R09 |
| `A8` | `AlgoOrthoLinePointVector` | `PerpendicularLine(P,v)` | `v` rotated +90° | inherited | R09c |
| `A9` | `AlgoLineBisector` | `PerpendicularBisector(A,B)` | `B − A` rotated +90° | inherited | R10 |
| `A10` | `AlgoLineBisectorSegment`, admitted segment | `PerpendicularBisector(s)` | (end − start) rotated +90° | inherited | R10c |
| `A11` | `AlgoAngularBisectorLines`, both inputs admitted | `AngleBisector(g,h)` | kernel rule from both senses (output 1 = output 0 rotated +90°) | inherited | R11c/d |
| `A12` | `AlgoTangentLine`, admitted line | `Tangent(g, conic)` | sense of `g` | inherited | R12g |
| `A13` | `AlgoMirror` at a point or a line, admitted source | `Reflect` | image of the source sense | inherited | R18, R18b, R18g, `ER-1` |
| `A14` | `AlgoRotatePoint`, `AlgoRotate`, admitted source | `Rotate` | rotated source sense | inherited | R18c, `ER-1` |
| `A15` | `AlgoTranslate`, admitted source | `Translate` | source sense | inherited | R18d, `ER-1` |
| `A16` | `AlgoDilate`, admitted source | `Dilate` | source sense times the sign of the factor | inherited | R18e, `ER-1` |
| `A17` | `AlgoMacro` | macro output | sense of the inner route, only when the inner route and its whole orientation-bearing dependency chain are established reliably through existing supported APIs and every orientation-bearing input in the chain is admitted; otherwise suppressed (AUTHOR DECISION `OTQ-F1-1`; no new identity, macro serialization or provenance infrastructure) | inherited | R23, R23b |

Excluded initially (kernel facts retained; no cue until separately admitted):

| Id | Routes | Reason | Evidence |
|---|---|---|---|
| `X1` | independent `GeoLine` (implicit, explicit or parametric input) | sense from equation sides or stored coefficients; a directed parametrization does not make it constructive | R15–R15h, `K6` (b) |
| `X2` | `AlgoDependentLine` | as `X1` | D09 |
| `X3` | `AlgoTangentPoint` (point on the conic, and external point) | conic-equation sign; configuration jump at the conic | R12–R12f, D12–D15 |
| `X4` | `AlgoAngularBisectorPoints` | configuration-defined; jumps through the straight angle | R11, D16–D18 |
| `X5` | `AlgoTangentFunctionNumber`, `AlgoTangentFunctionPoint` | graph convention (+x) | R13 |
| `X6` | `AlgoLinePointLine` / `AlgoOrthoLinePointLine` with a `GeoFunction` reference | function-as-line convention (−x) | R14, R14b |
| `X7` | `AlgoAsymptote`, `AlgoPolarLine` | conic-coefficient convention | R21, R22 |
| `X8` | any admitted-kind route whose orienting input is excluded or unknown | inherits an unadmitted sense | `K6` (d) |
| `X9` | `AlgoShearOrStretch`, `AlgoAttachCopyToView`, `AlgoMirror` at a conic, other transforms | not verified | `ER-1` |
| `X10` | every other algorithm (GeoCeDG dimension outputs, 3D line types, list members, axes, unresolved macros) | unknown | `K10`, `K11` |

Every admitted row needs its own `T-F1-SENSE` test at implementation; a row
whose test fails is excluded and reported, never patched by guessing.

## 4. Eligibility predicate and degenerations (`DQ-F1-7`)

AUTHOR DECISION: suppress for undefined, non-finite or degenerate objects,
effectively hidden objects, insufficient visible extent and redundant
directional decorations; never draw for an invisible or inadmissible object;
dynamic inversions stay faithful to the kernel.

IMPLEMENTATION DESIGN — a cue is drawn for `g` in Graphics View 1 only if all
hold:

1. `g` is a 2D `GeoLine`, `GeoSegment` or `GeoRay`, not `GeoAxis`, with a
   top-level drawable in GV1;
2. `g.isDefined()`, `g.isEuclidianVisible()`, `g.isVisibleInView(GV1)`,
   `app.isLayerShown(g.getLayer())`, line thickness > 0;
3. finite coefficients, finite `getDirection`, `|(x, y)|` not zero by
   `DoubleUtil.isZero`;
4. admitted by §3.3;
5. no logarithmic axis in GV1;
6. visible chord ≥ `4L` on screen;
7. for a segment, none of `DECORATION_SEGMENT_ONE_ARROW`, `TWO_ARROWS`,
   `THREE_ARROWS` (`common/kernel/kernelND/GeoElementND.java:123-127`); arrow
   ending styles do not suppress and keep their existing presentation
   semantics (AUTHOR DECISION `OTQ-F1-2`: a deliberate v1 boundary; a broader
   style-interaction policy needs separate characterization).

| Degeneration | Kernel fact (report) | Cue |
|---|---|---|
| coincident defining points (`A1`, `A3`, `A9`) | undefined (D02) | none |
| zero-length segment (`A2`) | defined, direction (0, 0) (D02) | none (rule 3) |
| near-coincidence below `10⁻⁸` | line undefined (D03b) | none |
| zero vector (`A4`, `A5`, `A8`) | undefined (D06) | none |
| infinite coefficient | `isDefined() = true` (D09) | none (rule 3) |
| `NaN` coefficient | undefined (D10) | none |
| undefined orienting input | output undefined or input inadmissible | none |
| parallel inputs of `A11` | one output undefined | none on that output; the other follows the kernel |
| dilation factor 0 (`A16`) | zero coefficients | none |
| tangent parallel to `g` absent (`A12`) | undefined | none |
| reversal by motion (B through A, v through −v) | sense follows the kernel (D04, D07) | follows at the next paint; no smoothing |
| redefinition | new object, same label (D08) | derived from the new object |
| temporary invalidity and recovery | same identity (D05) | hidden, then shown with the current sense |
| hidden object, hidden layer, not visible in GV1, thickness 0, not drawn | no drawable or not painted (`K11`) | none |
| off-screen or short visible part, log axis | — | none |

## 5. Placement (IMPLEMENTATION DESIGN, `DQ-F1-4`)

Let `s = normalize(dx·xscale, −dy·yscale)` be the screen image of
`(dx, dy) = getDirection` (as `DrawRay.java:116-142`). `s` places and rotates
the arrowhead and never decides its sense. All geometry is in GV1 screen
pixels; the visible chord is the part inside the view rectangle inset by `L`.

| Object | Anchor (arrow centroid) |
|---|---|
| segment | screen midpoint if on the visible chord; otherwise the midpoint of the visible chord |
| ray | start point + `3L` along `s` if on the visible chord; otherwise the point of the visible chord nearest the view centre |
| line | the point of the visible chord nearest the view centre (screen projection clamped to the chord) |

The anchor is a pure function of the current object state, the GV1 transform
and size, and `L`. Pan, zoom, resize and non-uniform axis ratios move, rotate
on screen or suppress the cue; they never reverse it. Label overlap is
accepted.

## 6. Rendering (AUTHOR DECISIONS `DQ-F1-4`, `OTQ-F1-3`)

- Discreet hollow isosceles triangle in the object's colour, tip forward along
  `s`: axial length `L = 10` device pixels, total base width 4 device pixels,
  half-width `W = 2` device pixels; independent of zoom. The direction comes
  from the admitted kernel orientation; the pixel geometry determines only the
  appearance; no document geometry, scale or exported coordinate changes.
- 1 px outline in the object's colour and line opacity; interior filled with
  the view background so the line does not show through.
- Built by a small GeoCeDG-owned Desktop path builder (same triangle family as
  the private `ARROW_OUTLINE` ending, `DrawSegmentWithEndings.java:282-312`); no
  shared drawable is changed.
- No label, no own highlight, no hit area.

## 7. Seam (AUTHOR DECISION `DQ-F1-8`)

The overlay runs in `GeoCeDGEuclidianView.paint` after `super.paint`
(`geocedg/GeoCeDGEuclidianView.java:69-112`), iterating GV1's drawables and
reading kernel state only. It is screen-only by construction: no print,
clipboard, image, LaTeX, DXF or CLI route calls that method, and
`ExportViewport` is not a `GeoCeDGEuclidianView` (report `K8`). Graphics View 2
(`EuclidianViewFor3DD`) is not touched. Rejected: a GeoCeDG GV2 class, the
shared drawables, any kernel object or command.

## 8. Option and user interface (AUTHOR DECISIONS `DQ-F1-6`, `DQ-F1-9`)

- `USER_PREFERENCE` `geocedg.view.orientation-cue.v1`, default OFF, values
  `shown` / `hidden` (anything else reads OFF, never written back), survives
  restart through the portable GeoCeDG properties, not part of document, macro
  or preferences XML, not an undo step, unchanged by New, Open, document
  replacement, undo, redo and redefinition; one value per process store; a
  change repaints every GeoCeDG window; never read by Classic.
- One declarative menu action (catalog 130 → 131). IMPLEMENTATION DESIGN:
  `presentation.orientation-cue`, `product-action`, target
  `geocedg.presentation.orientation-cue`, `preference-only`, `product-only`,
  `action-only`, maturity `experimental`, checkbox state from the preference,
  View menu group `view-visibility-style`.
- One synchronized control in Layout & Presentation (AUTHOR DECISION
  `OTQ-F1-4`): a clearly differentiated orientation-cue checkbox in the existing
  GeoCeDG-owned `GeoCeDGPresentationOptionsPanel`, created through the existing
  `AppGeoCeDG.newProductPresentationOptionsPanel()`, preserving the
  presentation-size controls; no new hook in `AppD` or `OptionsLayoutD`. Menu
  checkbox and panel checkbox read and write the same preference owner in
  `AppGeoCeDG` and refresh each other; neither modifies document state, XML or
  undo history.
- Stop for author review if either control needs an unexpected inherited-class
  modification, cannot be integrated through this GeoCeDG-owned surface, or
  broadens the product contract.

## 9. Lifecycle

The overlay keeps no object reference between paints. Moving inputs,
reversal, redefinition, undo, redo, New, Open and document replacement need no
handling: each paint re-reads GV1's drawables and the objects' current state.
Identity is never touched.

## 10. Validation design

| ID | Obligation |
|---|---|
| `T-F1-SENSE` | each admitted row `A1`–`A17` of §3.3: cue world sense has a positive dot product with `getDirection` and agrees with the path parameter; assertions on the read-only cue model, not on pixels alone |
| `T-F1-REVERSAL` | `(A,B)` versus `(B,A)`; `v` versus `−v`; reversed references for `A6`, `A7`, `A11`, `A12`; reversed sources for `A13`–`A16` |
| `T-F1-ELIGIBILITY` | each `X1`–`X10` route shows no cue; recursive exclusion (`X8`); unknown algorithm; macro per `OTQ-F1-1` |
| `T-F1-DYNAMIC` | inversions by motion follow the kernel with no smoothing; identity kept through invalidity |
| `T-F1-DEGENERATE` | every row of the §4 degeneration table: no cue, no exception |
| `T-F1-VISIBILITY` | hidden object, hidden layer, thickness 0, not visible in GV1, auxiliary object drawn normally, list member, axis |
| `T-F1-PLACEMENT` | deterministic anchors; pan, zoom, resize, non-uniform ratio, off-screen start, corner-cutting line, short chord, log axis |
| `T-F1-RENDERING` | hollow triangle of axial length 10 and total base 4 device pixels (half-width 2), object colour, background interior; midpoint-decoration suppression; endpoint styles unchanged |
| `T-F1-VIEWS` | cue in GV1; never in GV2 |
| `T-F1-EXPORT` | with the option on: PNG, PDF, SVG, EMF, print, clipboard, CLI, PGF, PSTricks, Asymptote and DXF byte-identical to option off |
| `T-F1-NO-MUTATION` | document XML, undo state, construction order, styles, layers, labels, selection identical with the option on and off |
| `T-F1-HIT` | hit testing and selection identical with the option on and off |
| `T-F1-PREFERENCE` | default OFF; `shown`/`hidden`; invalid value; persistence across a new app on the same store; New/Open unchanged; absent from document and preferences XML; Classic never reads it |
| `T-F1-UI-SYNC` | menu checkbox and Layout & Presentation row stay synchronized; repaint of every window |
| `T-F1-PROFILE` | catalog completeness 131, stable IDs, projections (menus, groups, clusters), status synchronization, every pin of report `K14` |
| `T-SMOKE` | author smoke (not by the agent) |

## 11. Verification class (AUTHOR DECISIONS `DQ-F1-8`, record §7.5)

`BOUNDED_PHASE`, finalized for planning, one registered `PHASE`
`PRE-G9B-R6-PLUS-F1`, conditioned on the implementation entry gate confirming
the scope: a GeoCeDG Desktop Graphics View 1 overlay; one persistent user
preference; one declarative menu action; one synchronized existing
product-options panel control; no shared drawable, kernel, export or
document-serialization change. The catalog delta 130 → 131 alone does not
require `INTEGRATED_PHASE`; the `PHASE` covers the full profile/action-catalog
impact (counts, pins, enablement, synchronization, presentation lifecycle).
Any change violating this bounded architecture stops the work and requests
author reclassification. The class is frozen at implementation entry.

## 12. Rejected alternatives

- Per-object attribute; GV2 coverage; export participation (author decisions).
- Admitting equation-defined, convention-dependent or configuration-defined
  routes in v1 (author decision `DQ-F1-5`).
- `getDirectionInD3` or any screen order as the sense.
- A kernel arrow object, command or dependency; a shared drawable change.
- Reusing `DECORATION_SEGMENT_ONE_ARROW` (document style, serialized).
- A new Layout & Presentation panel through `AppD`/`OptionsLayoutD` hooks.

# GeoCeDG orientation cue

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR APPROVED** (author closeout of 2026-10-11, [F1 closeout record](../../../docs/validation/pre_g9b_r6_plus_f1_closeout_record.md)); the approved implementation is the technical candidate `1cea3c89e34d32ab6321a434e7bb873bc1a33f83`. Earlier: `PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW` |
| Approval scope | the behavior of the F1 author decisions (`DQ-F1-1` to `DQ-F1-9`, `OTQ-F1-1` to `OTQ-F1-4`) as implemented by the approved candidate; it does not extend to the excluded routes `X1`–`X10` or to Graphics View 2 |
| Version | `1.0` |
| Owner phase | `PRE-G9B-R6-plus-F1` (orientation cue) |
| Governing author decisions | [F1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f1_author_decisions_record.md) v1.2 (`DQ-F1-1` to `DQ-F1-9`, `OTQ-F1-1` to `OTQ-F1-4`, implementation authorization §8) |
| Approved design transcribed | [F1 reconciled design](../../../docs/architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md) (`DESIGN — AUTHOR APPROVED`) |
| Evidence | [F1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md) `K0`–`K17` |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §17.2 (state classes); `geocedg/specs/operations/verification-levels.md` §12.8 |
| Serialization effect | none |

## 0. Status and normative language

This specification is the durable contract of the F1 orientation cue. It was
written as the contract of the F1 technical candidate and became normative
through the author closeout record of 2026-10-11, without any change of
requirement. It transcribes the approved design without
semantic change; §9 records how the candidate implements it. `MUST`,
`MUST NOT`, `SHOULD` and `MAY` have their usual meaning. Where an author
decision and this text differ, the decision prevails and this text is
defective.

Provenance markers: **[AD]** author decision, **[NI]** normative inheritance,
**[CE]** characterization evidence, **[ID]** implementation design inside the
approved scope (reviewable, not an author decision).

## 1. Scope

1.1 **[AD]** The orientation cue is an optional, discreet, presentation-only
indication of the direction the kernel uses for a line, segment or ray. It is
one global application option, shown only in Graphics View 1, never in Graphics
View 2 and never in any export (`DQ-F1-1`, `DQ-F1-2`, `DQ-F1-3`).

1.2 **[AD]** The cue MUST NOT create, change or remove any `GeoElement`,
algorithm, dependency, construction order, object style, layer, label,
visibility flag, document XML, macro XML, preferences XML, undo point, hit
area or selection (`DQ-F1-8`).

## 2. Kernel orientation versus intrinsic sense

2.1 **[AD]** The only authority of the displayed sense is
`GeoLine.getDirection` = `(y, −x)` for the stored coefficients
`x·X + y·Y + z = 0`, which the kernel path parameters, `UnitVector`/`Direction`
and oriented `Angle` share **[CE]** (`K3`). `getDirectionInD3`, screen point
order, labels, names, captions, definition text, coordinates, construction
indices, presentation state and visual proximity MUST NOT choose or classify the
sense (`DQ-F1-5`, `OTQ-F1-1`).

2.2 A line as a point set has no sense. The cue displays the kernel sense only
where it is constructively oriented (the user's ordered inputs define it) or
demonstrably inherited (a fixed, source-proven rule carries an admitted
orientation over). Where the kernel sense depends on how an equation was typed,
how a conic is normalized or on a configuration, the sense exists and is
documented **[CE]** (`K4`, `K6`) but the cue is suppressed.

## 3. Eligibility matrix v1

3.1 **[AD]** Eligibility is a positive matrix, fail-closed. Classification uses
only the classes of the object, of its parent algorithm and of its
orientation-bearing inputs, read through existing public kernel API. An
orientation-bearing input is admitted when it is a finite `GeoPoint` (ordered
input), a `GeoVector`, or a `GeoLine`/`GeoSegment`/`GeoRay` that this matrix
admits (recursively over the acyclic construction graph). The cue object
itself MUST be exactly a 2D `GeoLine`, `GeoSegment` or `GeoRay` (not an axis,
not a 3D type, not a subclass).

3.2 Admitted routes:

| Id | Parent algorithm | Condition | Displayed sense |
|---|---|---|---|
| `A1` | `AlgoJoinPoints` | both points finite | `B − A` |
| `A2` | `AlgoJoinPointsSegment` | both points finite (polygon edges and segment images included) | start → end |
| `A3` | `AlgoJoinPointsRay` | both points finite | `B − A` |
| `A4` | `AlgoRayPointVector` | finite point, vector | `v` |
| `A5` | `AlgoLinePointVector` | finite point, vector | `v` |
| `A6` | `AlgoLinePointLine` | finite point; reference an admitted line, segment or ray (never a function) | reference sense |
| `A7` | `AlgoOrthoLinePointLine` | as `A6` | reference sense rotated +90° |
| `A8` | `AlgoOrthoLinePointVector` | finite point, vector | `v` rotated +90° |
| `A9` | `AlgoLineBisector` | both points finite | `B − A` rotated +90° |
| `A10` | `AlgoLineBisectorSegment` | admitted segment | (end − start) rotated +90° |
| `A11` | `AlgoAngularBisectorLines` | both lines admitted | the kernel rule of both senses |
| `A12` | `AlgoTangentLine` | admitted line | sense of the line |
| `A13` | `AlgoMirror` | mirror a point or a 2D line; admitted source | image of the source sense |
| `A14` | `AlgoRotate`, `AlgoRotatePoint` | admitted source | rotated source sense |
| `A15` | `AlgoTranslate` | admitted source | source sense |
| `A16` | `AlgoDilate` | admitted source | source sense times the sign of the factor |
| `A17` | `AlgoMacro` | see §3.4 | sense of the inner route |

3.3 Excluded initially (kernel facts retained in the evidence; no cue until
separately admitted):

| Id | Routes |
|---|---|
| `X1` | independent `GeoLine` (implicit, explicit or parametric input) |
| `X2` | `AlgoDependentLine` |
| `X3` | `AlgoTangentPoint` (point on the conic and external point) |
| `X4` | `AlgoAngularBisectorPoints` |
| `X5` | `AlgoTangentFunctionNumber`, `AlgoTangentFunctionPoint` |
| `X6` | `AlgoLinePointLine` / `AlgoOrthoLinePointLine` with a `GeoFunction` reference |
| `X7` | `AlgoAsymptote`, `AlgoPolarLine` |
| `X8` | any admitted-kind route whose orientation-bearing input is excluded or unknown |
| `X9` | `AlgoShearOrStretch`, `AlgoAttachCopyToView`, `AlgoMirror` at a conic, every other transform |
| `X10` | every other algorithm, 3D line types, lines inside lists, axes, macros whose provenance cannot be established |

3.4 **[AD]** Macros (`OTQ-F1-1`): a macro output is admitted only when its
internal construction route and the whole orientation-bearing dependency chain
are established reliably through existing supported APIs and every
orientation-bearing input satisfies this contract; otherwise the cue is
suppressed. No new kernel identity, macro serialization or provenance
infrastructure is introduced. **[ID]** The output is matched to the
macro-construction object by its position in the macro definition
(`AlgoMacro.getOutput(i)` ↔ `Macro.getMacroOutput()[i]`), the macro inputs to
the actual inputs likewise (`Macro.getMacroInput()[i]` ↔ `AlgoMacro.getInput()[i]`);
the inner route is classified by this matrix, and an inner reference that is a
macro input is followed to the actual input.

## 4. Eligibility predicate and degenerations

4.1 **[AD]** No cue is ever drawn for an undefined, non-finite or degenerate
object, an effectively hidden object, an object with insufficient visible
extent, a segment with a redundant directional decoration, or an object the
matrix does not admit (`DQ-F1-7`). Kernel inversions are shown as they occur:
the sense is re-read at every paint, with no memory, hysteresis, smoothing or
reassignment.

4.2 **[ID]** A cue is drawn for `g` in Graphics View 1 only if all hold: (1)
`g` is a cue type of §3.1 with a top-level drawable in Graphics View 1; (2)
`isDefined()`, `isEuclidianVisible()` (which includes the condition to show
the object and zero thickness of segments), `isVisibleInView(1)`, the layer is
shown, line thickness > 0; (3) finite coefficients and a finite `getDirection`
that is not zero by the kernel tolerance; (4) admitted by §3; (5) no
logarithmic axis; (6) visible chord ≥ 40 px; (7) for a segment, none of the
midpoint arrow decorations `DECORATION_SEGMENT_ONE_ARROW`, `TWO_ARROWS`,
`THREE_ARROWS` — **[AD]** `OTQ-F1-2`, a deliberate v1 boundary; endpoint
arrow styles keep their own semantics and do not suppress.

4.3 Degenerations: coincident defining points, zero vector, near-coincidence
below the kernel tolerance, `NaN` or infinite coefficients (which the kernel
reports as defined), an undefined orientation-bearing input, the undefined
output of `A11` for parallel inputs, a dilation by 0, a missing tangent, a
hidden or zero-thickness object, a hidden layer, no drawable, an off-screen or
short visible part and a logarithmic axis show no cue and raise no exception.
Temporary invalidity creates no identity; recovery shows the then-current
sense; a redefinition is a new object with its own sense.

## 5. Placement

5.1 **[AD]** Geometry and placement are presentation-only (`DQ-F1-4`).

5.2 **[ID]** With `(dx, dy)` = `getDirection`, the screen direction is
`s = normalize(dx·xscale, −dy·yscale)`; it places and rotates the arrowhead and
never decides its sense. The object is parametrized as the kernel path
`P(t) = origin + t·(dx, dy)` (`t ∈ [0, 1]` for a segment from its start,
`t ≥ 0` for a ray from its start, `t ∈ ℝ` for a line from its finite start
point or the foot of the perpendicular from the origin) and clipped to the view
rectangle inset by 10 px. The anchor (arrow centroid) is: the segment midpoint
if visible, else the midpoint of the visible chord; the ray start plus 30 px if
visible, else the point of the visible chord nearest the view centre; for a
line, the point of the visible chord nearest the view centre. The anchor is a
pure function of the object state, the view transform and size.

## 6. Rendering

6.1 **[AD]** A discreet hollow triangular arrowhead in the object's colour:
axial length 10 device pixels, total base width 4, half-width 2 (`OTQ-F1-3`).
The pixel geometry determines only the appearance; no document geometry, scale
or exported coordinate changes.

6.2 **[ID]** The tip lies two thirds of the length ahead of the anchor along
`s`; the 1 px outline uses the object colour with its line opacity; the
interior is filled with the view background. The arrowhead has no label,
highlight or hit area.

## 7. Option and user interface

7.1 **[AD]** One global `USER_PREFERENCE` **[NI]** `geocedg.view.orientation-cue.v1`,
default OFF; it survives an application restart; it is not part of document
serialization; New and Open do not modify it (`DQ-F1-6`).

7.2 **[ID]** Values `shown` and `hidden` in the portable GeoCeDG properties;
any other value reads as OFF and is not rewritten; the value is written with
the store (window close or Save Settings, like every GeoCeDG preference); never
read by Classic; one value per process, a change repaints every GeoCeDG window.

7.3 **[AD]** One declarative menu action `presentation.orientation-cue` and one
clearly differentiated checkbox in the existing GeoCeDG-owned
`GeoCeDGPresentationOptionsPanel` (Layout & Presentation), created through
`AppGeoCeDG.newProductPresentationOptionsPanel()`, both on the same preference;
no new hook in `AppD` or `OptionsLayoutD`; the catalog grows from 130 to 131
actions (`DQ-F1-9`, `OTQ-F1-4`). Neither control changes document state, XML or
undo history; a change updates the view immediately.

7.4 **[ID]** The action is a `product-action` with target
`geocedg.presentation.orientation-cue`, effect `preference-only`, availability
`product-only`, command surface `action-only`, maturity `experimental`, in the
View menu group `view-visibility-style` and the cluster
`presentation-visibility-style`; its checkbox state is the preference.

## 8. Invariants

Construction geometry, DAG, identity, serialization, geometric algorithms,
command behavior, shared drawables, hit testing, selection, undo history,
exports (print, clipboard, PNG, PDF, SVG, EMF, PGF/TikZ, PSTricks, Asymptote,
DXF, CLI) and Classic behavior are unchanged with the option on and off.

## 9. Implementation (approved candidate `1cea3c89`)

- Seam: `GeoCeDGEuclidianView.paint`, after `super.paint` and the export-area
  overlay; no export, print or clipboard route calls this method **[CE]**
  (`K8`).
- Read-only model: `org.geocedg.desktop.GeoCeDGOrientationCue` (matrix,
  predicate, placement); rendering helper:
  `org.geocedg.desktop.GeoCeDGOrientationCueRenderer`; preference:
  `org.geocedg.desktop.GeoCeDGOrientationCuePreference`; owner and window
  propagation in `AppGeoCeDG`; checkbox in `GeoCeDGPresentationOptionsPanel`;
  action target in `GeoCeDGActionRegistry`.
- Verification: `PreG9BR6PlusF1OrientationCueTest` and the phase
  `PRE-G9B-R6-PLUS-F1` (`BOUNDED_PHASE`).

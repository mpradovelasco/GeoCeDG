# PRE-G9B-MAINT-A-N1 — candidate validation matrix

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR REVIEW.** `selfApproved=false`. A plan of tests, not evidence: no case is executed by `MAINT-A-N1` |
| Contracts covered | [`bspline-2d`](../../geocedg/specs/curves/bspline-2d.md) (family), [`dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md) (reference import), reserved approximation interface; [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md) (proposed) |
| Executing increments | `MAINT-A2` (BS), `MAINT-A-DXF0` (DX), `MAINT-A-DXF1` (AP, reserved), `MAINT-A3` (BS-14); none is authorized |
| Identifiers | BS-1 to BS-15 and DX-1 to DX-7 keep the identifiers and the intent of the [proposal](../architecture/pre_g9b_maint_p0_bspline_proposal.md) §12; BS-16 onward, DX-8 onward and SEP-1 onward are new |

## 0. Rules for every case

- Reference values are independent of the implementation under test: exact rational
  arithmetic on the binary64 inputs (Cox–de Boor or blossoming over `Q`), closed forms, or
  a separately written evaluator. A case never validates the evaluator against itself.
- Tolerances are stated per case in units of the model or as a relative bound; "exact"
  means equality of binary64 data or of a structural fact, never of a floating evaluation.
- Every case runs at two zooms and two DPI settings where a view exists; semantic results
  must be identical.
- Every case is deterministic: two runs produce identical results and identical
  diagnostics.
- A typed rejection must publish no geometry and must not retain a previous revision's
  geometry.

## 1. Family `bspline-2d/v1` (`MAINT-A2`)

| Id | Case | Expected property | Spec |
|---|---|---|---|
| BS-1 | Degree 1, uniform and non-uniform knots, open and exactly closed | the curve coincides with the control polygon; vertices at the parameters of the interior knots; continuity `C^0` published | §3, §5 |
| BS-2 | Cubic clamped with 4 control points (`n = 3`, knots `0,0,0,0,1,1,1,1`) | equals the cubic Bézier of the same points with `u = 0 → P_0`; the opposite orientation of Classic `BezierCurve` is declared, not "corrected" | §3, §9 |
| BS-3 | Cubic with non-uniform knots | point and first derivative against an exact rational reference at the knots, at span midpoints and at 64 deterministic interior parameters; relative error `≤ 64·2^-53·max‖P_i‖` | §3, §11 |
| BS-4 | Cubic with an interior knot of multiplicity 2 and of multiplicity 3 | published continuity `C^1` and `C^0` exactly from the multiplicity; with three non-aligned control points around a multiplicity-3 knot the image has a corner and no `G^1` claim is made | §5 |
| BS-5 | Interior multiplicity `p + 1`; decreasing knots; `n < p`; knot count mismatch; nonfinite point or knot; non-integral or out-of-range degree; ends not clamped | typed rejection with the cause of §7, no geometry, no stale geometry | §7 |
| BS-6 | Consecutive coincident control points; all control points coincident | defined; regularity not established on the affected spans; total length correct (0 for the collapsed curve, declared degenerate) | §6 |
| BS-7 | Exact closure versus closure within `10^-15` | closed `C^0` only with binary64 equality of `P_0` and `P_n`; the near case is open | §4.3 |
| BS-8 | Total and partial length, including a partial range across a multiplicity-3 knot | against an independent high-precision quadrature; monotone cumulative length on the branch; identical under zoom and DPI; class-3 estimate declared | §9 |
| BS-9 | Intersections with line, circle and conic; a tangency; a root exactly on an interior knot; a root at the closure seam | residuals within tolerance; one root per knot or seam (no duplicate from the two adjacent spans); tangency kept as rich evidence | §3.3, §9 |
| BS-10 | R5 similarities (translation, rotation, reflections, uniform dilation, `k = 0`) | the transformed image evaluates as the B-spline of the transformed control points within tolerance; new durable identity; `k = 0` collapsed image as approved | §9 |
| BS-11 | Dynamic: move one control point, change the degree, edit the knot list, make it invalid and valid again | change confined to the `p + 1` affected spans (exact comparison of unaffected span data); typed undefined state and recovery without stale geometry | §6, §7 |
| BS-12 | Save, reopen, Undo/Redo, copy/paste, compatible redefinition | durable identity kept where the rules say; identical bytes on save-reopen-save; serialized contract identifier present; dependencies identical live and after reopen | §8 |
| BS-13 | SplineV2 non-regression | the CORE SplineV2 selections and the SplineV2 persistence fixtures unchanged; coefficient arrays and consumer results bit-identical | §1, §9.3 |
| BS-14 (`MAINT-A3`) | `SPLINE` written by GeoCeDG read by two independent readers | each reader reproduces reference points within its declared reading tolerance | §13 |
| BS-15 | Homogeneous evaluator with all weights implicit (`w ≡ 1`) | bit-identical to the polynomial de Boor evaluation on BS-1 to BS-4 and BS-16 | §11.2 |
| BS-16 | Degree sweep `p = 1..7` with clamped-uniform knots and with random non-uniform knots (fixed seed recorded), `n + 1 ∈ {p + 1, 2p + 1, 32}` | point and derivative against the exact rational reference; published continuity per knot; work within the declared limits | §3, §5, §11 |
| BS-17 | Bézier equivalence for every degree `p = 1..7` with `n = p` | equals the degree-`p` Bézier curve of the control points | §3 |
| BS-18 | Knot ownership and the domain end | at an interior knot the evaluation uses the right span; at `u = b` the last non-degenerate span; one-sided limits agree with the published continuity order | §3.3 |
| BS-19 | Default forms: `BSpline(L)` with 3 control points; `BSpline(L, p)` with `n + 1 < p + 1`; `BSpline(L, p)` with growing list length | typed rejection instead of a silent degree reduction; the `clamped-uniform/v1` knots regenerate from the current `n` | §4.2 |
| BS-20 | Conditioning of the polynomial publication: knots far from the origin (`[10^6, 10^6 + 10]`), wide ranges, `p = 7`, more than 64 spans | local span coefficients accurate within the declared bound; no consumer reads ill-conditioned global coefficients; above 64 spans the one-sided polynomial route declines with its typed reason and the existing evaluator route answers | §11.3 |
| BS-21 | Persistence forward compatibility: a document with an unknown future contract version, and a document with an unknown child of the family block | fails closed with a typed cause; never read as `v1` | §8.3 |
| BS-22 | Older reader: the same document opened in GeoCeDG 1.2.2 | fails as the characterization predicts (§8.4), with no partial document presented as complete | §8.4 |
| BS-23 | Evaluation independent of view | identical point, derivative, length and intersection results with the view hidden, zoomed and at another DPI; render tessellation never consulted | §9, §11 |
| BS-24 | Approximation provenance block (`MAINT-A-DXF1` writes it; `MAINT-A2` reads and preserves it) | round trip unchanged through save, reopen, Undo/Redo and copy; applicability recomputed from the definition digest | §10.5 |

## 2. Reference import `dxf-spline-reference/v1` (`MAINT-A-DXF0`)

| Id | Case | Expected property | Contract |
|---|---|---|---|
| DX-1 | Polynomial `SPLINE` and a rational quadratic circular arc (quarter and half circle with weights `1, √2/2, 1` as written to 17 digits) | the record evaluates within the declared numerical tolerance of `(cos θ, sin θ)` at reference parameters derived analytically from the rational parametrization; the reading rounding is reported separately | §3, §4.5, §7 |
| DX-2 | Flag 4 without weights; weights without flag 4; a zero, a negative and a nonfinite weight; weight ratio above the limit; inconsistent counts; decreasing knots | per-entity rejection with the cause and order of §4.4; the rest of the file is imported | §4.4 |
| DX-3 | Fit points only; periodic; not clamped; 3D (`z` not constant); paper space; inside a block or reached through `INSERT` | not imported, each with its typed cause; `INSERT` never expanded | §4.2, §4.4 |
| DX-4 | Closed flag contradicted by the data, and exact closure without the flag | diagnostic; the data decide; no closure by proximity or by the code 42–44 tolerances | §4.4 |
| DX-5 | File untouched; construction untouched | file digest identical before and after; `Construction`, its XML and its Undo history unchanged by import, removal and Undo/Redo | §0, §11 |
| DX-6 | Distinction | a reference is not selectable by construction tools, not a snapping target, rejected by every command and Locus V2 capability, absent from semantic exports | §10 |
| DX-7 | Render | the same tessellation at every zoom and DPI; tessellation within the declared tolerance of the evaluation; incomplete render marked when the work limit is hit | §8 |
| DX-8 | Units and transformation | `$INSUNITS` and the document unit recorded; no implicit scaling; differing units give a non-blocking notice; an explicit affine transform is applied to control points only, in the declared operation order, and recorded; a singular transform is rejected | §6 |
| DX-9 | Provenance | file digest, base name, version, handle, layer, ordinal, units, plane, transform, contract and data digests present and identical across two imports of the same file; no time-dependent field | §9 |
| DX-10 | Decimal reading | decimal commas, `NaN`, `Infinity`, hexadecimal forms rejected as `MALFORMED_REAL`; an overflow rejected as `NONFINITE_VALUE`; correctly rounded values for representative tokens | §4.5 |
| DX-11 | File limits and binary DXF | file above `FILE_BYTES_MAX` or entity count above `SPLINE_ENTITIES_MAX` rejected as a whole; binary DXF rejected | §4.1 |
| DX-12 | Session lifecycle `R-a` | records survive neither New, Open nor Close; Save writes none and shows the session-only notice; Undo/Redo never touches them | §11 |

## 3. Reserved approximation interface (`MAINT-A-DXF1`)

These cases keep the proposal's AP identifiers; they bind `MAINT-A-DXF1` only after its own
approximation contract is approved.

| Id | Case | Expected property |
|---|---|---|
| AP-1 | Polynomial source within the family limits; rational source with all weights equal | exact conversion: identical `p`, knots and control points; declared as conversion, not approximation |
| AP-2 | Rational quarter circle approximated with `q = 3` | residual `≤` estimate `≤` certified bound (when computed), all `≤ ε`; each measure carries its own grade |
| AP-3 | `ε` unreachable within the limits | typed failure; no object; best estimated error reported |
| AP-4 | Edit of the approximation | measures declared not applicable to the current definition; the object stays a valid B-spline |
| AP-5 | Exactly closed source | closed `C^0` approximation by exact endpoint equality |
| AP-6 | Provenance | survives save, reopen and Undo/Redo; a copy keeps it with a new durable identity |

## 4. Separation of layers

| Id | Case | Expected property |
|---|---|---|
| SEP-1 | A reference, its approximation and an independent B-spline drawn at the same place | three distinct presentations and inspector states; no association by coordinates |
| SEP-2 | Re-import of a changed source file while an approximation exists | the approximation is unchanged; its provenance reports a different source digest when both are present |
| SEP-3 | Measures on the approximation | every text and inspector entry says «on the approximation»; nothing is attributed to the reference |
| SEP-4 | Semantic DXF export of a document with references, approximations and B-splines | references absent; approximations and B-splines exported by the approved approximate route until `MAINT-A3` |

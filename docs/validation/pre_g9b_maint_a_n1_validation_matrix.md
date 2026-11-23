# PRE-G9B-MAINT-A-N1 — candidate validation matrix

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR REVIEW.** `selfApproved=false`. A plan of tests, not evidence: no case is executed by `MAINT-A-N1` |
| Contracts covered | [`bspline-2d`](../../geocedg/specs/curves/bspline-2d.md) (family), [`dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md) (reference import), reserved approximation interface; [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md) (proposed) |
| Executing increments | `MAINT-A2` (BS), `MAINT-A-DXF0` (DX), `MAINT-A-DXF1` (AP, reserved), `MAINT-A3` (BS-14); none is authorized |
| Identifiers | BS-1 to BS-15 and DX-1 to DX-7 keep the identifiers and the intent of the [proposal](../architecture/pre_g9b_maint_p0_bspline_proposal.md) §12; BS-16 onward, DX-8 onward and SEP-1 onward are new |
| Revision | 2 (2026-11-23, author's corrective instruction): error criteria §0.1–§0.2 replace the revision-1 tolerance of BS-3; DX-1 fixtures defined in §2.1; DX-2 and the new DX-13 follow the weight reconstruction of the import contract §4.6. Revision 1 is commit `307bc8fda` |

## 0. Rules for every case

- Reference values are independent of the implementation under test: exact rational
  arithmetic on the binary64 inputs (Cox–de Boor or blossoming over `Q`), closed forms, or
  a separately written evaluator. A case never validates the evaluator against itself.
- Numerical comparisons use the error criteria of §0.1, never a single universal threshold;
  "exact" means equality of binary64 data, of bit patterns where a case says
  bit-identical, or of a structural fact (multiplicity, continuity order, clamping,
  closure, knot rule), never of a floating evaluation.
- Every case runs at two zooms and two DPI settings where a view exists; semantic results
  must be identical.
- Every case is deterministic: two runs produce identical results and identical
  diagnostics.
- A typed rejection must publish no geometry and must not retain a previous revision's
  geometry.

### 0.1 Numerical error criteria for positions and derivatives

Revision 2 (author's corrective instruction of 2026-11-23). Revision 1 stated, for BS-3, a
"relative error" bounded by `64·2^-53·max‖P_i‖`; that quantity has the dimension of a length
and is not a dimensionless relative tolerance, and it ignored the parametric scale of the
derivative. It is withdrawn and replaced by the criteria below.

Notation, for a parameter `u` owned by the non-degenerate span `j` (spec §3.3), with
`ε = 2^-53` and `‖·‖` the Euclidean norm:

| Symbol | Definition | Dimension |
|---|---|---|
| `C(u)`, `C'(u)` | reference point and first derivative with respect to `u`, computed in exact rational arithmetic at the same binary64 inputs (`u`, knots, control points) by an implementation independent of the one under test | length; length per unit of `u` |
| `C̃(u)`, `C̃'(u)` | values computed by the implementation under test | same |
| `S_pos(u)` | `max ‖P_i‖` over the active control points `P_{j-p}..P_j`, measured from the model origin | length |
| `L_ref` | diameter of the bounding box of all control points | length |
| `S_der(u)` | `p · max (‖P_{i+1}‖ + ‖P_i‖) / (u_{i+p+1} − u_{i+1})` over `i = j−p..j−1`; every denominator is `≥ u_{j+1} − u_j > 0` for a non-degenerate span, so no division by zero occurs | length per unit of `u` |

`S_pos` depends on absolute coordinates because binary64 rounding does: a curve far from the
origin has a larger absolute error than the same curve near it. `S_der` grows as knot
spacings shrink. Both effects are real conditioning of the binary64 computation; the
criteria expose them instead of hiding them in a relative figure.

| Id | Criterion | Applies |
|---|---|---|
| E1 | absolute position error `‖C̃(u) − C(u)‖ ≤ K_pos(p)·ε·S_pos(u)` | always |
| E2 | relative position error `‖C̃(u) − C(u)‖ / L_ref`, reported with the position conditioning factor `S_pos(u) / L_ref`; its bound is E1 divided by `L_ref` | only when `L_ref > 0`; with all control points coincident (`L_ref = 0`) only E1 |
| E3 | absolute derivative error `‖C̃'(u) − C'(u)‖ ≤ K_der(p)·ε·S_der(u)` | always |
| E4 | relative derivative error `‖C̃'(u) − C'(u)‖ / ‖C'(u)‖`, reported with the derivative conditioning factor `S_der(u) / ‖C'(u)‖`; its bound is E3 divided by `‖C'(u)‖` | only where `‖C'(u)‖ ≥ ρ_der·S_der(u)` |
| E5 | zero or near-zero derivative: only E3; no relative derivative error and no tangent direction is asserted; a test never requires the classification `SINGULAR` (spec §6) from a nonzero reference derivative | where `C'(u) = 0` or `‖C'(u)‖ < ρ_der·S_der(u)` |

For rational records (`MAINT-A-DXF0`) the same five criteria apply with homogeneous scales:
`S_pos` becomes `(max w_i‖P_i‖ + ‖C(u)‖·max w_i) / W(u)` over the active indices, with
`W(u) ≥ min w_i > 0`, and the derivative scale follows from the quotient rule
`C' = (A' − W'C) / W`; their constants are fixed by the procedure of §0.2 in that increment.

Exact structural facts and bit-identity cases (BS-13, BS-15) are compared exactly and never
through these criteria.

### 0.2 Procedure for fixing the constants

`MAINT-A-N1` asserts no value of `K_pos(p)`, `K_der(p)`, `ρ_der` or of their rational
counterparts. Their state is `PENDING_VALIDATION_IN_MAINT_A2` (`MAINT-A-DXF0` for the
rational ones). The implementing increment fixes them reproducibly:

1. write down the exact operation sequence of the implemented evaluator (de Boor on the
   homogeneous points, hodograph for the derivative);
2. derive forward-error bounds under the standard model `fl(a ∘ b) = (a ∘ b)(1 + δ)`,
   `|δ| ≤ ε`, giving `K_pos(p)` and `K_der(p)` as explicit functions of `p`, and record the
   derivation in its report;
3. justify `ρ_der` from the derived bound so that E4 is asserted only where it is
   meaningful;
4. validate on the BS-16 sweep (fixed, recorded seed) and on adversarial families:
   coordinates offset to `10^6`, knot spacings down to `10^-6` of the domain,
   near-coincident consecutive control points, `p = 7`. Record the maximum observed ratios
   `error / (ε·S)`. An observation above the derived constant stops the increment (the
   analysis or the implementation is wrong); observations below it are recorded and never
   used to tighten a constant without a proof.

## 1. Family `bspline-2d/v1` (`MAINT-A2`)

| Id | Case | Expected property | Spec |
|---|---|---|---|
| BS-1 | Degree 1, uniform and non-uniform knots, open and exactly closed | the curve coincides with the control polygon (exact reference: linear interpolation in rational arithmetic) within E1 of §0.1; vertices at the parameters of the interior knots; continuity `C^0` published exactly | §3, §5 |
| BS-2 | Cubic clamped with 4 control points (`n = 3`, knots `0,0,0,0,1,1,1,1`) | equals the cubic Bézier of the same points (exact Bernstein reference) within E1 and E3, with `u = 0 → P_0`; the opposite orientation of Classic `BezierCurve` is declared, not "corrected" | §3, §9 |
| BS-3 | Cubic with non-uniform knots | point and first derivative against an exact rational reference at the knots (with the ownership rule), at span midpoints and at 64 deterministic interior parameters; criteria E1 to E5 of §0.1, with constants fixed by §0.2 | §3, §11 |
| BS-4 | Cubic with an interior knot of multiplicity 2 and of multiplicity 3 | published continuity `C^1` and `C^0` exactly from the multiplicity; with three non-aligned control points around a multiplicity-3 knot the image has a corner and no `G^1` claim is made | §5 |
| BS-5 | Interior multiplicity `p + 1`; decreasing knots; `n < p`; knot count mismatch; nonfinite point or knot; non-integral or out-of-range degree; ends not clamped | typed rejection with the cause of §7, no geometry, no stale geometry | §7 |
| BS-6 | Consecutive coincident control points; all control points coincident | defined; regularity not established on the affected spans; total length correct (0 for the collapsed curve, declared degenerate) | §6 |
| BS-7 | Exact closure versus closure within `10^-15` | closed `C^0` only with binary64 equality of `P_0` and `P_n`; the near case is open | §4.3 |
| BS-8 | Total and partial length, including a partial range across a multiplicity-3 knot | against an independent high-precision quadrature; monotone cumulative length on the branch; identical under zoom and DPI; class-3 estimate declared | §9 |
| BS-9 | Intersections with line, circle and conic; a tangency; a root exactly on an interior knot; a root at the closure seam | residuals within tolerance; one root per knot or seam (no duplicate from the two adjacent spans); tangency kept as rich evidence | §3.3, §9 |
| BS-10 | R5 similarities (translation, rotation, reflections, uniform dilation, `k = 0`) | the transformed image evaluates as the B-spline of the transformed control points within the sum of the E1 bounds of both evaluations and the rounding bound of the transformation applied to the control points; new durable identity; `k = 0` collapsed image as approved | §9 |
| BS-11 | Dynamic: move one control point, change the degree, edit the knot list, make it invalid and valid again | change confined to the `p + 1` affected spans (exact comparison of unaffected span data); typed undefined state and recovery without stale geometry | §6, §7 |
| BS-12 | Save, reopen, Undo/Redo, copy/paste, compatible redefinition | durable identity kept where the rules say; identical bytes on save-reopen-save; serialized contract identifier present; dependencies identical live and after reopen | §8 |
| BS-13 | SplineV2 non-regression | the CORE SplineV2 selections and the SplineV2 persistence fixtures unchanged; coefficient arrays and consumer results bit-identical | §1, §9.3 |
| BS-14 (`MAINT-A3`) | `SPLINE` written by GeoCeDG read by two independent readers | each reader reproduces reference points within its declared reading tolerance | §13 |
| BS-15 | Homogeneous evaluator with all weights implicit (`w ≡ 1`) | bit-identical to the polynomial de Boor evaluation on BS-1 to BS-4 and BS-16 | §11.2 |
| BS-16 | Degree sweep `p = 1..7` with clamped-uniform knots and with random non-uniform knots (fixed seed recorded), `n + 1 ∈ {p + 1, 2p + 1, 32}` | point and derivative against the exact rational reference under E1 to E5 of §0.1; this sweep and the adversarial families of §0.2 fix `K_pos(p)`, `K_der(p)` and `ρ_der`; published continuity per knot exactly; work within the declared limits | §3, §5, §11 |
| BS-17 | Bézier equivalence for every degree `p = 1..7` with `n = p` | equals the degree-`p` Bézier curve of the control points (exact Bernstein reference) within E1 and E3 | §3 |
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
| DX-1 | A polynomial `SPLINE`; the rational quarter circle Q of §2.1; the rational semicircle H of §2.1, made of two rational quadratic quarter arcs | three separate checks per fixture: (a) evaluator against the exact rational evaluation of the binary64 data under the rational form of §0.1; (b) the exact-weight curve against the circle oracle of §2.1; (c) the weight-rounding discrepancy between the binary64 data and the exact-weight curve, reported separately and never charged to the evaluator; reading rounding reported separately | §3, §4.5, §4.6, §7 |
| DX-2 | Non-unit weights without flag 4; a partial weight list (fewer than `n + 1` values, and more than `n + 1`); a zero, a negative and a nonfinite weight; weight ratio above the limit; inconsistent counts; decreasing knots | per-entity rejection with the cause and order of §4.4 and §4.6; the rest of the file is imported; no weight is deduced for a partial list | §4.4, §4.6 |
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
| DX-13 | Admitted weight representations: no flag and no code 41; flag 4 and no code 41; flag 4 with `n + 1` unequal weights; flag 4 with `n + 1` equal weights `≠ 1`; flag 4 with `n + 1` weights equal to 1; no flag with `n + 1` weights equal to 1 | admitted with nature `POLYNOMIAL`, `POLYNOMIAL`, `RATIONAL`, `RATIONAL_EQUAL_WEIGHTS`, `POLYNOMIAL`, `POLYNOMIAL`; the recorded flag and weight provenance (`IMPLICIT_UNIT` or `EXPLICIT`) match the file; the informative diagnostics of §4.6 appear where due; implicit weights are never recorded as code-41 values; the data digest uses the absence marker for implicit weights | §3, §4.6, §5, §9 |

### 2.1 Rational circle fixtures for DX-1

A single clamped rational quadratic segment with strictly positive weights cannot represent
a 180° arc: the middle weight of a symmetric arc of angle `φ` is `cos(φ/2)`, which is 0 for
`φ = 180°` (the middle control point at infinity). The semicircle is therefore defined as two
quarter arcs.

**Quarter circle Q.**

| Item | Value |
|---|---|
| Degree | `p = 2` |
| Control points | `P_0 = (1, 0)`, `P_1 = (1, 1)`, `P_2 = (0, 1)` |
| Weights | `(1, w, 1)` with `w = √2/2`; written in the file as `1.0`, `0.7071067811865476`, `1.0`, which reads as the binary64 value `0x3FE6A09E667F3BCD` |
| Flag 70 | rational bit set (4), planar bit set (8) |
| Knots | `(0, 0, 0, 1, 1, 1)` |
| Domain and orientation | `[0, 1]`, counterclockwise from `(1, 0)` to `(0, 1)` |

**Semicircle H.**

| Item | Value |
|---|---|
| Degree | `p = 2` |
| Control points | `P_0 = (1, 0)`, `P_1 = (1, 1)`, `P_2 = (0, 1)`, `P_3 = (−1, 1)`, `P_4 = (−1, 0)` |
| Weights | `(1, w, 1, w, 1)`, with the same written value of `w` |
| Flag 70 | rational bit set (4), planar bit set (8) |
| Knots | `(0, 0, 0, 1, 1, 2, 2, 2)`: clamped ends; the interior knot `1` has multiplicity `2 = p` |
| Domain and orientation | `[0, 2]`, counterclockwise from `(1, 0)` through `(0, 1)` to `(−1, 0)` |
| Spans | `[0, 1)` is the quarter arc Q; `[1, 2]` is Q rotated by `+90°` (control points `P_2, P_3, P_4`, weights `1, w, 1`), with local parameter `t = u − 1`; `u = 1` is owned by the right span and evaluates to `P_2` |
| Published continuity at `u = 1` | `C^0`, from multiplicity `2 = p` (structural) |
| Observed, not published | with the exact weight both one-sided first derivatives at `u = 1` equal `(−√2, 0)` (`2·(w/1)·(P_2 − P_1)` and `2·(w/1)·(P_3 − P_2)`), so the curve is `C^1` and `G^1` there; a test may compare derivatives at parameters approaching `u = 1` from both sides under E3 but must not expect a published order above `C^0` |

**Circle oracle (independent of the evaluator).** For one quarter arc with control points
`(1,0), (1,1), (0,1)` and weights `(1, w, 1)`, let `a = (1−t)²`, `b = 2t(1−t)w`, `c = t²`,
`D = a + b + c`; then `C(t) = ((a + b)/D, (b + c)/D)` and, for every `w`,

\[
\|C(t)\|^2 - 1 = \frac{2t^2(1-t)^2\,(2w^2 - 1)}{D(t)^2},
\]

so the curve lies on the unit circle exactly when `w² = 1/2`. With `w = √2/2` exactly, the
parameter correspondence is the Möbius map fixed by `t = 0 → θ = 0`, `t = 1/2 → θ = π/4`
(by symmetry) and `t = 1 → θ = π/2`:

\[
\theta_Q(t) = 2\arctan\frac{t}{\sqrt{2} - (\sqrt{2} - 1)\,t},
\qquad C(t) = (\cos\theta_Q(t), \sin\theta_Q(t)).
\]

For H, `θ_H(u) = θ_Q(u)` on `[0, 1)` and `θ_H(u) = π/2 + θ_Q(u − 1)` on `[1, 2]`. The oracle
points are computed from these formulas in high precision (at least 50 significant digits),
not with the evaluator under test.

**Weight-rounding discrepancy.** The binary64 weight differs from `√2/2`, so the curve
defined by the file data is not exactly the circle: by the identity above its radial
deviation is governed by `2w² − 1` evaluated exactly on the binary64 `w`. Check (c) computes
the maximum distance between the exact rational evaluation of the binary64 data and the
oracle over the test parameters, in high precision, and reports it as the representation
error of the fixture; check (a) compares the evaluator only with the exact evaluation of the
same binary64 data.

**Test parameters.** For Q: `t ∈ {0, 1/8, 1/4, 1/2, 3/4, 7/8, 1}` and 64 deterministic
interior values (fixed seed recorded). For H: the images of those values in both spans,
plus `u = 1` and `u = 1 ∓ 2^-k` for `k = 10, 20, 30`.

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

# Explicit 2D B-spline family — `bspline-2d`

| Field | Value |
|---|---|
| Status | **CANDIDATE — PENDING AUTHOR REVIEW.** `selfApproved=false`, `authorApproved=false`. Not normative until the author accepts it explicitly |
| Contract identifiers | `bspline-2d/v1` (polynomial subset, specified here for implementation); `bspline-2d/v2` (rational extension, designed in §14, not specified for implementation and not authorized) |
| Document revision | 1, 2026-11-23 |
| Increment that writes it | `PRE-G9B-MAINT-A-N1` (normative design only) |
| Increment that would implement `v1` | `MAINT-A2`, **not authorized** |
| Architectural layer | Shared Java kernel (semantic curve family publishing `GeoLocusV2`); pure mathematical library outside the kernel packages (§12) |
| Decision | [ADR 0043](../../../docs/adr/0043-explicit-2d-bspline-family-rational-ready.md) (proposed) |
| Author directions | D-A-1 to D-A-5 (architecture), D-A-8 to D-A-10 (approximation provenance), D-V4; [decision sheet](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md) §0 |
| Companion contracts | [DXF reference import](../import/dxf-spline-reference.md) (candidate); [validation matrix](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md) (candidate); [candidate report](../../../docs/validation/pre_g9b_maint_a_n1_candidate_report.md) |

## 0. Status and boundary

This specification defines a new semantic curve family. It authorizes no code. It does
not amend [`semantic-spline-2d.md`](semantic-spline-2d.md), the SplineV2 ADRs (0018, 0021,
0022, 0028), the DXF export contracts or any Locus V2 contract. Where `MAINT-A2` needs an
additive change to a generic Locus V2 surface, this specification names it (§11.3,
§9.4) and `MAINT-A2` must justify it again against the code of its own base.

## 1. Scope and relation to SplineV2

The family is the **control-point B-spline**: the curve is a convex combination of its
control points weighted by B-spline basis functions over a user knot vector. Its general
definition is rational (§3.2). `bspline-2d/v1` is the polynomial subset with implicit unit
weights.

SplineV2 is an **interpolating** family with a normalized chord parameter and simple
knots. The two families share the Locus V2 apparatus and the knot-ownership rule, and
nothing else. SplineV2 keeps its entire contract: command, forms, equations, limits,
continuity, persistence (no family or version attribute) and tests. Nothing in this
specification changes the result of any SplineV2 document or operation; `MAINT-A2` must
prove it (BS-13).

Out of scope for `v1`: weights (`MAINT-A-R`); periodic curves, unclamped ends and exact
affine images on the control net (`MAINT-A7`); a certified interval model, certified pairs
and certified implicit targets (`MAINT-A4`); a graphical tool; DXF `SPLINE` export
(`MAINT-A3`); 3D curves and surfaces.

## 2. Authority and dependencies

`AGENTS.md` §1, §4, §9, §11 and §17; ADR 0006, 0013 and 0018 for the Locus V2 surface;
ADR 0016 for the native document; ADR 0026 for redefinition; ADR 0031 for the canonical
English command header; ADR 0032 for units; the Locus V2 specifications under
`geocedg/specs/locus/`. This specification does not restate their rules.

## 3. Mathematical definition

### 3.1 Data

| Datum | Definition | Precondition in `v1` |
|---|---|---|
| Degree `p` | integer | `1 ≤ p ≤ 7` |
| Control points | ordered list `P_0..P_n` of finite 2D points | `p + 1 ≤ n + 1 ≤ 128` |
| Weights | `w_0..w_n` | implicit, all equal to 1; not serialized |
| Knot vector | `U = (u_0..u_m)`, `m = n + p + 1`, finite binary64 values | nondecreasing; clamped ends (§4.1); interior multiplicity `≤ p` |
| Domain | `[a, b] = [u_p, u_{n+1}]` | `a < b` |
| Orientation | increasing `u` | — |

The multiplicity of a knot value is the number of entries of `U` exactly equal to it
(binary64 equality). Knots are never merged by proximity.

### 3.2 General rational definition and homogeneous form

\[
C(u) = \frac{\sum_{i=0}^{n} N_{i,p}(u)\,w_i\,P_i}{\sum_{i=0}^{n} N_{i,p}(u)\,w_i},
\qquad u \in [a,b],
\]

with the Cox–de Boor basis

\[
N_{i,0}(u) = \begin{cases}1 & u_i \le u < u_{i+1}\\ 0 & \text{otherwise}\end{cases},
\qquad
N_{i,p}(u) = \frac{u-u_i}{u_{i+p}-u_i}N_{i,p-1}(u) + \frac{u_{i+p+1}-u}{u_{i+p+1}-u_{i+1}}N_{i+1,p-1}(u),
\]

and the convention `0/0 := 0`. With `P_i^w = (w_i x_i, w_i y_i, w_i)` the homogeneous curve
`C^w(u) = Σ N_{i,p}(u) P_i^w` is a polynomial B-spline in `R^3` and `C` is its central
projection `C = (A_x / W, A_y / W)` with `A = Σ N_{i,p} w_i P_i` and `W = Σ N_{i,p} w_i`.

`bspline-2d/v1` is the case `w_i ≡ 1`. Then `W ≡ 1` on `[a, b]` by partition of unity and

\[
C(u) = \sum_{i=0}^{n} N_{i,p}(u)\,P_i .
\]

The same homogeneous algorithm evaluates both cases (§11.2).

### 3.3 Domain and knot ownership

The domain is `[a, b]`. The interior knots partition it into non-degenerate spans
`[u_j, u_{j+1})` with `u_j < u_{j+1}`; spans of zero length are not evaluated. Ownership
rule, the same as SplineV2's:

- a parameter equal to an interior knot value belongs to the span on its **right**;
- `u = b` belongs to the last non-degenerate span;
- for an exactly closed curve (§4.3) the parameter `b` is identified with `a` and owned by
  the first span.

A geometric point on a shared knot therefore has exactly one canonical semantic address;
genuinely distinct preimages (loops) remain distinct.

### 3.4 Orientation

Orientation is that of increasing `u`. Reversing a curve is a different definition (a
redefinition with reversed control points and reflected knots), never a silent
reinterpretation. `u = a` maps to `P_0` for a clamped curve; this is the opposite of
Classic `BezierCurve`, whose `t = 0` gives its last point, and it is declared, not
"corrected".

### 3.5 Properties guaranteed by the definition

For `v1`, in exact arithmetic, and therefore usable as structural facts on the captured
data: local support of `N_{i,p}`; partition of unity and non-negativity on `[a, b]`; each
span `[u_j, u_{j+1})` lies in the convex hull of `P_{j-p}..P_j`; affine invariance (the
image by an affinity is the B-spline with the same `p` and `U` and transformed control
points); the derivative is the degree-`p − 1` B-spline with control points
`Q_i = p (P_{i+1} − P_i) / (u_{i+p+1} − u_{i+1})`; variation diminishing; polynomial of
degree `≤ p` on each span. Binary64 evaluation approximates these facts; it does not
establish them (§11).

## 4. Admitted forms in `v1`

### 4.1 Limits

| Item | `v1` |
|---|---|
| Degree | `1 ≤ p ≤ 7`, integral |
| Control points | `p + 1 ≤ n + 1 ≤ 128` |
| Ends | clamped: `u_0 = … = u_p = a` and `u_{n+1} = … = u_{n+p+1} = b`, exact equality |
| Interior multiplicity | `1 ≤ k ≤ p` |
| Weights | implicit unit |
| Closure | open, or exactly closed `C^0` (§4.3) |

Degrees above 7 and sizes above 128 are rejected, never truncated or reduced.

### 4.2 Command forms

| Form | Meaning |
|---|---|
| `BSpline(<List of Points>, <Degree>, <List of Knots>)` | explicit form; the knot list has `n + p + 2` numbers |
| `BSpline(<List of Points>, <Degree>)` | knots generated by the persisted rule `clamped-uniform/v1` |
| `BSpline(<List of Points>)` | degree 3 and rule `clamped-uniform/v1` |

Rule `clamped-uniform/v1`: for `n + 1` control points and degree `p`, the knots are `p + 1`
zeros, then the integers `1, 2, …, n − p`, then `p + 1` copies of `n − p + 1`. All values are
exact binary64 integers. The rule identifier is persisted (§8.2), so the knots regenerate
from the current `n` at every revision and a future change of default rule cannot
reinterpret an existing document.

No form lowers the degree silently: `BSpline(<List of Points>)` with fewer than 4 points and
`BSpline(L, p)` with `n + 1 < p + 1` are undefined with cause `TOO_FEW_CONTROL_POINTS`.

Result: a new `GeoLocusV2` with type string `BSpline`, a new durable identifier and its own
branch key (`bspline-2d/main`). The canonical English header and the localized names follow
ADR 0031; the form with weights is reserved for `MAINT-A-R` (§14) and is not recognized by
`v1`.

### 4.3 Exact closure

The curve is **exactly closed** when `P_0` and `P_n` are the same point object or have equal
coordinates under binary64 `==`. Then the domain is published as closed (periodic in the
Locus V2 domain sense, fundamental interval `[a, b)`), the closure is
provider-proved by that equality, and the seam continuity is `C^0` (a corner unless the
control polygon happens to be aligned there; no smoothness is claimed). Approximate
equality never closes the curve; the curve is then open.

## 5. Parametric continuity

| Location | Published continuity | Basis |
|---|---|---|
| inside a span | `C^∞` | polynomial |
| interior knot of multiplicity `k`, `1 ≤ k ≤ p` | `C^{p-k}` | knot structure, exact |
| seam of an exactly closed curve | `C^0` | exact endpoint equality |

This is a structural fact of the represented function computed exactly from the
multiplicities; it does not depend on the control points and needs no tolerance. It is the
minimum guaranteed order; the actual order may be higher for particular control points,
and no higher order is published.

## 6. Regularity, geometric continuity and degeneracies

Regularity (`C'(u) ≠ 0`) and geometric continuity (`G^1`: continuous unit tangent;
`G^2`: continuous curvature) are distinct from parametric continuity and are **not**
established by it:

- a `C^{p-1}` curve can have a cusp inside a span (for example with coincident consecutive
  control points);
- a knot of multiplicity `p` passes the curve through a control point and admits a corner;
- `G^1` at a knot requires regularity on both sides and `C^1`, or `C^0` with the three
  control points around a multiplicity-`p` knot aligned and ordered.

In `v1` regularity and `G^1` are `FLOATING_POINT_UNCERTIFIED`. A parameter is reported
`SINGULAR` only when the floating velocity is exactly zero. Their certification (hodograph
convex hull excluding the origin) belongs to `MAINT-A4`.

| Degeneracy | Consequence |
|---|---|
| consecutive coincident control points | defined; possible stationary point; regularity not established on the affected spans |
| aligned control points | defined; image in a line, possibly with reversal |
| all control points coincident | defined; the curve is a point, total length 0; declared degenerate |
| knot value of multiplicity `> 1` | zero-length spans not evaluated; ownership unique |
| self-intersection | admitted; distinct preimages |
| interior multiplicity `p + 1` | rejected (§7); a disconnected curve is two objects, not one |

## 7. Invalidity: typed causes

A revision whose data fail a precondition publishes an undefined `GeoLocusV2` with exactly
one primary cause, in this order, and never retains the geometry of an earlier revision:

| # | Cause | Condition |
|---|---|---|
| 1 | `INPUT_UNDEFINED` | an input object is undefined |
| 2 | `NONFINITE_DATA` | a control point coordinate or knot is not finite |
| 3 | `DEGREE_INVALID` | degree not integral or outside `1..7` |
| 4 | `TOO_FEW_CONTROL_POINTS` | `n + 1 < p + 1` |
| 5 | `TOO_MANY_CONTROL_POINTS` | `n + 1 > 128` |
| 6 | `KNOT_COUNT_MISMATCH` | explicit knot list length `≠ n + p + 2` |
| 7 | `KNOTS_DECREASING` | some `u_{i+1} < u_i` |
| 8 | `NOT_CLAMPED` | end multiplicities differ from `p + 1` |
| 9 | `EMPTY_DOMAIN` | `a = b` |
| 10 | `INTERIOR_MULTIPLICITY_EXCEEDED` | an interior knot value with multiplicity `> p` |

Dynamic recovery is ordinary: when the inputs become valid again, the object is
recomputed from them.

## 8. Persistence

### 8.1 Mechanism

The characterization of the base (candidate report §2) established that
`AlgoElement.getCmdXML` offers no hook for command attributes, that an unknown attribute
of `<command>` is ignored silently, that an unknown child of `<command>` only logs an error,
and that only the GeoCeDG metadata codecs whose typed exceptions are rethrown by
`MyXMLio.doParseXML` make a load fail closed with rollback.

Decision: the family's XML is the ordinary command XML plus **one dedicated child element
of `<command name="BSpline">`, written before `<input>`**:

```xml
<command name="BSpline">
  <geocedgBSpline contract="bspline-2d/v1" knots="explicit"/>
  <input a0="L" a1="p" a2="K"/>
  <output a0="b"/>
</command>
```

with `knots="explicit"` for the three-argument form and `knots="clamped-uniform/v1"` for
the forms that generate knots. `MAINT-A2` writes it through an additive protected hook of
the command XML writer (default empty, so every other algorithm's bytes are unchanged) and
reads it with a GeoCeDG-owned codec in the command handler whose typed exception joins the
fail-closed family of `MyXMLio.doParseXML` (rethrow and restoration of the previous
construction), as the unit and hidden-layer codecs do. Because the reader sees the element
before `<output>`, the contract is known before the command executes.

Rejected mechanisms: an attribute of `<command>` (ignored silently by every current
reader, and no writer hook); an attribute of `<input>` or `<output>` (every attribute there
is read as an argument or a label); a record or attribute in `geocedgSpatial` (it would
mix a family contract into the identity layer, and older GeoCeDG readers already fail on
these documents, §8.4); a separate archive entry (not part of Undo snapshots, clipboard or
redefinition XML).

### 8.2 Serialized content

Serialized: the contract identifier; the knot rule; the inputs (control-point list, degree,
explicit knot list when present) as ordinary command arguments; the output's durable
identity through the existing `geocedgId` and `geocedgSpatial` machinery; the optional
approximation provenance block (§10.5). Inputs that are not labeled objects are promoted to
labeled objects as SplineV2's arguments are, so that the DAG is identical live and after
reopening.

Never serialized: evaluated points, span coefficients, tessellations, caches, digests of
derived data, solver results.

### 8.3 Versions and forward compatibility

- A reader implementing `v1` accepts `contract="bspline-2d/v1"` only. Any other value of
  `contract`, a missing `<geocedgBSpline>` element, an unknown attribute, an unknown value of
  `knots`, or an unknown child of `<geocedgBSpline>` fails closed with a typed cause
  (`UNSUPPORTED_CONTRACT_VERSION`, `CONTRACT_MISSING`, `MALFORMED_CONTRACT`) and the load
  is rolled back. A later version is never read as `v1`.
- `bspline-2d/v2` (§14) is a strict superset: a `v2` reader reads a `v1` element as the
  same curve with `w ≡ 1`, without migration and without rewriting the document.
- No document existing at the base contains `BSpline`; there is nothing to migrate.

### 8.4 Older readers

GeoCeDG 1.2.2 and earlier do not know the command. The characterization shows that the
unknown command alone would give a lenient partial load, but that the output's identity
record (family `LOCUS_V2`) then disagrees with the placeholder created for the unknown
element type, so the identity validation of the native open fails and the whole file is
refused with a generic load error. The same holds for Insert File, identity-bearing paste
and redefinition. This is accepted as the compatibility behavior of released readers; it is
a side effect, without a cause naming the family, and release notes of the first version
containing `BSpline` must state that such documents do not open in earlier versions.
BS-22 confirms which exception fires.

### 8.5 Undo/Redo, copy, redefinition, replacement and recovery

| Operation | Behavior |
|---|---|
| Undo/Redo | the snapshot is the construction XML, which contains the `<geocedgBSpline>` element; a restored state is the exact earlier definition |
| Copy/paste | the copied command XML includes the element; the pasted output receives a new durable identity; dependencies follow the existing paste rules |
| Compatible redefinition | a redefinition that keeps the family (for example a new knot list or degree) follows ADR 0026 and the existing V2 compatible-redefinition contract; whether the durable identity survives is decided by those rules, not by this family |
| Replacement by another family | ordinary replacement; the `BSpline` element and the approximation block disappear with the algorithm |
| Recovery from file | the object is rebuilt from the command and its inputs; nothing evaluated is read back |

Identity is never inferred from labels, coordinates, XML positions or construction order.

## 9. Participation in Locus V2

### 9.1 What the family inherits

The evaluator of the family implements `LocusEvaluator2D`, `LocusDifferentialEvaluator2D`
and `PiecewisePolynomialLocus2D` (with the additive extension of §11.3). The
characterization (candidate report §1) gives:

| Capability | Route for the family | Guarantee |
|---|---|---|
| Render, approximate DXF/LaTeX export, existence | generic, evaluator only | as for any `GeoLocusV2`; render is derived |
| Total and partial length | `PolicyV2LocusMetricCapability2D` with the differential evaluator and the knot breakpoints | class 3 (estimate) only; classes 1 and 2 need a certified model |
| One-sided intersections with line, segment, ray, circle, conic, polynomial implicit target | `PolynomialTargetIntersectionCapability2D` up to 64 spans and composed degree 96; above 64 spans the existing adaptive evaluator capability | floating evidence; no certified roots |
| Locus × locus pairs | piecewise-polynomial pair capability (≤ 33 coefficients per coordinate) | rich-only, as for every non-certified source |
| Point on the curve, inversion, continuation | point-interaction resolver and continuation through the polynomial path | floating; no certified continuation chart |
| R5 similarities | `LocusSimilarityEvaluator2D` by composition | new durable identity; `k = 0` collapsed image as approved |

### 9.2 What the family does not inherit

Every certified tier is tied to the final SplineV2 evaluator class: the certified interval
curve model, certified implicit targets, certified pair materialization with D2 tokens,
class-1 and class-2 metrics and certified continuation charts. The family gets none of them
in `v1` and says so in its published guarantees (§15). `MAINT-A4` would add a certified
interval model for the family (convex hull per span, hodograph regularity) under an ADR
0028-type amendment.

### 9.3 SplineV2 non-regression

The additive changes of §11.3 and §9.4 must leave every SplineV2 path bit-identical:
coefficients read by consumers, consumer results, persistence bytes and test outcomes
(BS-13).

### 9.4 Transformations

R5 similarities compose `T(C(u))` and keep domain, span ownership, orientation and semantic
addresses (as for SplineV2). The similarity wrapper must forward the local-coefficient
extension of §11.3 (similarities act on local coefficients linearly, with the translation
on the constant term); otherwise the transformed image declines the polynomial capability
under the residual rule of §11.3. Exact affine images on the control net (a new `BSpline`
with transformed control points) are `MAINT-A7`, not `v1`.

## 10. Identity, dependencies and revision

### 10.1 Identity

Each `BSpline` output is a first-class `GeoLocusV2` with its own durable identifier, one
branch (`bspline-2d/main`) and one component. Its provider contract string is
`bspline-2d/v1`. Identity never comes from labels, coordinates, XML positions, list order
or construction order.

### 10.2 Dependencies

The control-point list, the degree and the explicit knot list are DAG parents. Moving a
control point, editing the list or the knots, or changing the degree recomputes the curve
through the DAG; no hidden recomputation exists outside it. A change of one control point
changes at most the `p + 1` spans in its support.

### 10.3 Revision

Each recomputation produces a new definition revision of the output. Rich results and
tokens keyed by revision follow the existing Locus V2 rules. A typed undefined revision
(§7) publishes no geometry.

### 10.4 Future weights and identity

The family identity is conceptual: a `v1` object is a valid object of the rational family
with `w ≡ 1`, without migration. Adding, removing or changing weights is a redefinition of
the object's inputs and follows the general redefinition rules; it is **not** assumed that
every such redefinition preserves the durable identity of the object. A weight change that
preserves the image (Möbius reparametrization) changes the parametrization, so semantic
addresses by parameter do not survive it unchanged (§14).

### 10.5 Approximation provenance block (reserved for `MAINT-A-DXF1`)

D-A-10 leaves to this contract the location and behavior of the provenance of an
approximating construction. Decision: an optional passive child of the family element,

```xml
<geocedgBSpline contract="bspline-2d/v1" knots="explicit">
  <approximationProvenance version="1" … />
</geocedgBSpline>
```

whose exact attribute grammar the approximation contract of `MAINT-A-DXF1` fixes before
code. This contract fixes its semantics:

- **Content**: source provenance (file digest, entity handle and data digests,
  `dxf-spline-reference/v1`), the method identifier, degree, tolerance, knot strategy, each
  error measure with its own grade (sample residual, numerical estimate, certified bound
  when computed), and the **definition digest** of the approximating curve at fit time.
- **Passive**: it is never a DAG input, never identity, never read by evaluation, metrics,
  intersections or export.
- **Applicability**: at every revision the family computes the digest of its current
  definition (§12.3). The measures are applicable only when it equals the recorded digest;
  otherwise the inspector declares them «not applicable to the current definition». The
  object stays a valid B-spline: the guarantee is degraded, not the object.
- **Copy**: the block is copied with the command; the copy has a new durable identity and
  the same provenance; applicability follows the digest.
- **Undo/Redo and persistence**: the block is part of the command XML and round-trips with
  it.
- **Compatible redefinition**: the block is kept and its applicability recomputed;
  replacement by another family removes it.
- **`MAINT-A2` obligation**: a `v1` reader parses the block, validates its `version`,
  preserves it byte-for-byte on save and fails closed on an unknown `version`
  (`UNSUPPORTED_PROVENANCE_VERSION`). `MAINT-A2` never writes a new block.

The approximating construction is frozen and independent of its reference (D-A-10): its
control points are free objects; it has no dependency on the reference and is never refit
automatically. No second DAG is introduced.

## 11. Numerical policy

### 11.1 Determinism

Evaluation is binary64 with a fixed operation order, no fused multiply-add, no
parallelism and no dependence on view, zoom, DPI, tessellation or time. Java `double`
arithmetic makes the results platform-independent.

### 11.2 Homogeneous evaluation

Points are computed by the de Boor algorithm on homogeneous control points
`P_i^w = (w_i x_i, w_i y_i, w_i)`, followed by the division by `W`. In `v1` the library
represents implicit unit weights without materializing them, and the evaluation of `v1`
data must be bit-identical to the polynomial de Boor algorithm (BS-15). First derivatives
come from the de Boor algorithm on the hodograph control points of §3.5 for polynomial data
and from the quotient rule `C' = (A' − W' C) / W` for rational data (used only by the import
and approximation in `v1`, never by the kernel family). Results are numerical and declared
as such.

### 11.3 Conditioning of the polynomial publication

`PiecewisePolynomialLocus2D` publishes coefficients in powers of the **global** canonical
parameter, and its consumers re-expand them around each span start in floating point.
With the user parameter `u` of D-A-2, a span `[α, α + h]` gives a re-expansion
amplification of the order of `κ = ((|α| + h) / h)^p`. With the integer knots of
`clamped-uniform/v1` this is about `7.6·10^5` on the last span of a degree-5 curve with 20
control points and about `2·10^6` on the last span of a cubic with 128 control points;
offset knots make it unbounded.

Decision (local per-span representation, with a residual fail-closed rule):

1. **Authority.** The polynomial authority of the family is, per non-degenerate span, the
   local polynomial in `s ∈ [0, 1]`, `u = α + h·s`, obtained from the span's Bernstein
   coefficients (computed from the data by knot insertion) by the fixed binomial transform.
   Its amplification is bounded by `max_k C(p,k)·2^k ≤ 3^p` (at most 2 187 for `p = 7`),
   independent of `α` and `h`.
2. **Additive extension.** `MAINT-A2` adds one optional interface extending
   `PiecewisePolynomialLocus2D` that returns these local coefficients. The consumers that
   use polynomial coefficients (one-sided target intersection, pair intersection, the
   polynomial path of point interaction, the similarity wrapper) obtain them through a
   single shared accessor that returns the published local coefficients when the source
   offers them and otherwise performs today's computation unchanged, bit for bit.
3. **Global coefficients.** The family still implements the global accessors required by
   the interface. It computes them from the local coefficients in exact rational arithmetic
   on the binary64 values and rounds each once; they are a correctly rounded expansion,
   never the authority. `MAINT-A2` must show, by an inventory test over production code,
   that no consumer reads global coefficients of a source that offers local ones.
4. **Residual fail-closed rule.** If a consumer cannot be routed additively, or a wrapper
   does not forward the extension, the family declines the polynomial capability for that
   definition (`supportsPiecewisePolynomial` false) whenever `max_j κ_j > κ_max`
   (candidate `κ_max = 2^16`), with the typed diagnostic
   `POLYNOMIAL_PUBLICATION_ILL_CONDITIONED`; the existing evaluator-only routes answer.
5. **Span cap.** A definition with more than 64 non-degenerate spans is outside the
   one-sided polynomial capability by that capability's own limit; the adaptive evaluator
   capability answers, with its own declared evidence. `v1` keeps `n + 1 ≤ 128` (D-A-3) and
   declares this limitation (open decision Q-BS-2).

Rejected: the fail-closed check alone with global coefficients (it would decline ordinary
curves, see the figures above); normalizing the parameter to `[0, 1]` (contrary to D-A-2);
doing nothing (silent loss of accuracy, contrary to AGENTS §1.8).

### 11.4 Structural facts are exact

Multiplicities, continuity orders, clamping, exact closure, the knot rule and (for the
extension) exact equality of all weights are decided on the captured binary64 data by exact
comparison, with no tolerance.

### 11.5 Separation of evidence

Numerical evaluation (floating values), conditioned evidence and certified results stay
separate. The family has no conditioned regions of its own: its existence is
provider-proved on the whole domain. Certified results exist only through `MAINT-A4`.

## 12. Pure mathematical library

### 12.1 Placement

A stateless library in the GeoCeDG-owned package `org.geocedg.common.math.bspline` of the
shared common module (proposed name; the first implementing increment may adjust it within
`org.geocedg.common.math`). It must not import `Construction`, `GeoElement`, any
`org.geogebra.common.kernel` or `org.geocedg.common.kernel` type, any view or
presentation type, or Desktop code. It holds no global state and no cache.

It is shared by the kernel family (`MAINT-A2`), the reference import (`MAINT-A-DXF0`) and
the approximation (`MAINT-A-DXF1`). It is **not** a geometric authority: authority comes
from the family contract in the kernel for constructions, and from nothing for imported
references. It contains rational evaluation because the import must draw rational
references; that is not a rational kernel interface.

### 12.2 API (behavioral contract)

| Operation | Contract |
|---|---|
| validated immutable data | from degree, knots, control points and optional weights; returns the data or one typed cause (§7 for the family; the import maps to its own causes) |
| span lookup | returns the owning non-degenerate span of a parameter by the rule of §3.3 |
| point | homogeneous de Boor (§11.2) |
| first derivative | hodograph de Boor (polynomial) or quotient rule (rational) |
| knot insertion | Boehm insertion, returning new immutable data; exact in exact arithmetic, rounded in binary64 |
| span Bernstein form | homogeneous Bernstein coefficients of one span |
| span local power form | local coefficients in `s ∈ [0, 1]` (§11.3) |
| structural continuity | `C^{p-k}` per interior knot; seam order for exact closure |
| canonical encoding and digest | §12.3 |

Every operation is deterministic and total on validated data; on invalid data it returns a
typed cause and never throws an unchecked exception as control flow.

### 12.3 Canonical encoding and definition digest

The canonical encoding of a definition is the ASCII contract identifier, then `p`, `n + 1`,
the knots, the control points and the weights (or an explicit absence marker for implicit
unit weights) as big-endian binary64 bit patterns in index order. The definition digest is
its SHA-256. It is derived data: never serialized as authority, never identity.

## 13. Reservations

- **Weights**: the `<geocedgBSpline>` element of `v1` has no weight attribute and no weight
  argument exists; weights belong to `bspline-2d/v2` (§14).
- **DXF `SPLINE` export** (`MAINT-A3`, D-A-4): the family's authoritative data (degree,
  knots, binary64 control points) can be written as a non-rational, clamped, non-periodic
  `SPLINE` without transformation, with a data-exact fidelity class and reader-conformance
  evidence (BS-14). Until then the family is exported by the approved approximate route.
  This specification does not amend the DXF export contracts.

## 14. Rational extension `MAINT-A-R` (designed, not authorized)

`bspline-2d/v2` would add explicit weights. Designed responsibilities:

| Layer | Responsibility |
|---|---|
| Pure library | already evaluates homogeneous data; adds nothing conceptual |
| Kernel family | weights as user data and DAG parents (a list of positive numbers); exact positivity check; bounded ratio `max w / min w`; no silent normalization; exact detection of all-equal weights (published as polynomial) |
| Publication | a per-span homogeneous numerator interface (`A_x`, `A_y`, `W` local polynomials) beside the polynomial one; intersections with an algebraic target of degree `d` isolate roots of `W^d · G(A / W)`, polynomial of degree `≤ d·p` per span; rational × rational pairs need their own method |
| Consumers that need only the evaluators | render, export, existence, class-3 metrics (`C' = (A' − W' C) / W`, analytic per span since `W > 0`), point interaction by evaluator search, similarities: usable with a rational evaluator without a kernel interface change |
| Consumers that need a polynomial publication | one-sided target intersections, pairs, the polynomial point-interaction path: must use the homogeneous interface or decline |
| Certification | convex hull still holds for the curve; regularity is not certified by the polynomial hodograph |
| Persistence | `contract="bspline-2d/v2"` with weights as an input; a `v1` reader fails closed |
| Identity | a reparametrization by weight change moves parameter addresses (§10.4) |
| Name | a form of `BSpline` or a command `NURBS` producing the same family, decided when `MAINT-A-R` opens (D-A-5) |

None of this is specified for implementation here.

## 15. Published guarantees of `v1`

| Aspect | Guarantee |
|---|---|
| Construction fidelity | `SEMANTICALLY_CONSTRUCTED` |
| Evaluation | `ANALYTIC_EVALUATION`, numeric guarantee `FLOATING_POINT_UNCERTIFIED` |
| Existence | provider-proved on `[a, b]` (contract `bspline-2d/v1`); closure provider-proved only by exact equality |
| Parametric continuity | exact structural minimum order (§5) |
| Regularity, `G^1`, `G^2` | not established (§6) |
| Length | class-3 estimate with error estimate |
| Intersections | floating evidence; one-sided transverse roots may enter the existing selector lifecycle under its rules; pairs rich-only |
| Certified results | none in `v1` |

## 16. Validation

The candidate cases are BS-1 to BS-24 of the
[A-N1 validation matrix](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md).
`MAINT-A2` must execute them with SplineV2 non-regression; `MAINT-A-N1` does not.

## 17. Stop conditions and open decisions

Stop conditions for `MAINT-A2`:

- any SplineV2 path stops being bit-identical (§9.3);
- the local-coefficient extension cannot be added without a non-additive change to a
  generic Locus V2 interface;
- a generic consumer assumes seam smoothness beyond the published `C^0` of an exactly closed
  curve and cannot be corrected additively;
- the persistence element cannot be made fail-closed without changing the behavior of
  existing documents;
- the work would need weights, a rational kernel interface or a reference in the DAG.

Open decisions for the author:

| Id | Question | Candidate proposal |
|---|---|---|
| Q-BS-1 | Conditioning approach | local per-span representation with additive extension and the residual fail-closed rule (§11.3) |
| Q-BS-2 | Curves with more than 64 spans | keep `n + 1 ≤ 128` and declare the adaptive route above 64 spans; alternative: limit `v1` to 64 spans |
| Q-BS-3 | `κ_max` of the residual rule | `2^16` |
| Q-BS-4 | Persistence mechanism | dedicated `<geocedgBSpline>` child of `<command>` with a fail-closed codec (§8.1) |
| Q-BS-5 | Library package | `org.geocedg.common.math.bspline` |
| Q-BS-6 | Location of the approximation provenance | passive child of the family element (§10.5) |

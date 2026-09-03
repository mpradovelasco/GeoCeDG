# G9S1 semantic spline numerical-method review

- Status: **approved G9S1 implementation characterization; sections 10–11 are unapproved post-closeout R1 research iterations**
- Phase: G9S1
- Scope: deterministic 2D spline interpolation/evaluation, rich intersection
  evidence and rich length
- Citation policy: no theorem, guarantee or complexity claim may be published
  without a checked primary or scholarly source

## 1. Characterized Classic family

The inspected Classic `Spline` route constructs an upstream Cartesian curve.
Its algorithm solves a floating interpolation system, then compiles the local
polynomial pieces into conditional coordinate expressions over a normalized
parameter. That output is sufficient for the established Classic contract but
does not expose reconstructible provider-owned knots, canonical span ownership
or a stable semantic polynomial capability to Locus V2 consumers.

G9S1 therefore implements Option B. `SplineV2` mirrors the supported Classic
interpolation family in a separate normal-DAG parent and retains the polynomial
pieces as immutable semantic provider data. Classic `Spline`, its output type
and its command routing remain unchanged.

## 2. Implemented interpolation model

For 3–32 finite 2D interpolation points and integral degree 3 through
`min(point-count, 12)`, subject to at most 512 dense unknowns, the candidate:

1. computes positive finite cumulative increments from Euclidean chord length
   or the supplied weight function evaluated on each `(dx,dy)`;
2. normalizes those cumulative values to the oriented domain `[0,1]`;
3. solves the Classic-family continuity/interpolation equations in floating
   power basis with scaled partial pivoting;
4. rejects singular, nonfinite or excessive-backward-error systems; and
5. publishes immutable knots and per-span coordinate coefficients.

The implementation's backward-error guard is an engineering validity check,
not a proof of exact interpolation. Coefficients and derived knots are rebuilt
from ordinary command dependencies and are not independent serialization
authority.

Equal first and last input points select the supported closed periodic form.
The provider uses a half-open fundamental interval and canonical shared-knot
ownership. This does not claim support for arbitrary B-splines, control-point
splines, NURBS or general fitting.

## 3. One-sided polynomial intersections

For line, segment, ray, circle, supported nondegenerate conic and regular
polynomial-implicit targets, the target supplies a finite bivariate polynomial.
For each semantic source span the implementation:

1. composes the target polynomial with the two source coordinate polynomials;
2. normalizes the scalar power-basis polynomial to the local span;
3. recursively finds roots of its derivative;
4. partitions the span at those derivative roots into deterministic monotone
   cells;
5. detects endpoint, sign-changing and derivative-stationary root candidates;
6. refines accepted brackets by deterministic bisection/safeguarded evaluation;
   and
7. verifies the result through the common residual, target-membership,
   contact/germ and R4 identity publication seams.

Derivative partitioning is why even-contact candidates are not dependent on a
sign change. A transverse candidate with an established isolating cell may
enter the existing R4 selector/token ledger. Tangent/multiple candidates remain
nonmaterializable unless the stronger existing local contract is established.
A scalar zero polynomial on a span is overlap evidence and never a fabricated
finite root.

The arithmetic guarantee is floating estimated error. Global completeness is
`NOT_ESTABLISHED`; the method does not perform interval-rounded coefficient
enclosure or a certified polynomial root count.

Bounded function graphs that cannot expose the polynomial target capability use
the already approved general rich intersection fallback. They are not evidence
for the polynomial method above.

## 4. Piecewise-polynomial pair intersections

When both semantic providers expose polynomial spans, the candidate traverses
canonical component/span pairs, converts normalized span coordinate
polynomials to Bernstein form for convex-hull rejection, subdivides remaining
parameter rectangles in a deterministic order and applies safeguarded
dual-parameter Newton refinement. Canonical knot/seam ownership and semantic
parameter deduplication prevent duplicate diagnostic roots. Equivalent operand
order is canonicalized.

This is deliberately a **rich-only** capability. Floating Bernstein boxes and
Newton residuals do not prove exhaustive interval-rounded rectangle coverage or
unique pair isolation. Consequently pair candidates carry no public
continuation key, do not create active token-ledger allocations and cannot be
materialized as exact-token points. Tangency and coincident polynomial spans
remain typed ambiguity/overlap evidence; budget exhaustion fails coherently.

A future materializable pair contract requires a symmetric existence/uniqueness
certificate in both parameter domains and a reviewed symmetric selector. A
one-sided rank, caller order, root enumeration, Cartesian proximity or render
sample cannot fill that gap.

## 5. Metric method

The semantic evaluator supplies analytic first derivatives of the current
floating polynomial model. Total and partial rich length split the oriented
integration domain at provider knots and validity boundaries, then use the
existing deterministic adaptive Simpson capability in world coordinates. The
shared result reports its absolute/relative tolerance, Richardson-style error
estimate and work-limit status.

This is controlled numerical integration. It is not a chord sum and is not
described as symbolic or exact arc length. R5 similarity covariance is obtained
from the transformed semantic derivative/source authority, not by multiplying a
displayed scalar.

## 6. Candidate techniques and disposition

| Technique | Candidate disposition | Reason |
|---|---|---|
| floating power-basis interpolation with scaled pivoting | implemented | matches the bounded Classic family while retaining reconstructible spans |
| derivative-root partition plus safeguarded bisection | implemented for one-sided polynomial targets | deterministic, finds even-contact candidates and integrates with existing R4 evidence |
| Bernstein convex-hull rejection/subdivision | implemented for polynomial pair discovery | useful bounded rich evidence, but not a uniqueness certificate |
| adaptive Simpson over analytic derivative | reused for metrics | existing deterministic rich-metric seam with explicit error/work reporting |
| Sturm/Descartes certified counting | not implemented | would require coefficient/rounding hypotheses and a separately reviewed source-backed contract |
| interval Newton/Krawczyk | not implemented | no approved outward-rounding interval authority/dependency exists in this candidate |
| resultants | not implemented | degree growth, extraneous roots and conditioning would broaden the bounded phase |
| generic sampling/render polyline | rejected as authority | cannot own semantic identity, completeness or metric truth |

## 7. Determinism, work and evidence

The source iterates branches, components, spans, derivative cells and pair boxes
in canonical semantic order. Viewport, DPI, render tessellation and movement
history are absent from the algorithm. One-sided exact tokens still come from
the R4 semantic selector/ledger rather than span array or solver output order.

Instrumentation records polynomial spans examined/rejected and raw root
candidates, alongside the existing semantic evaluations, subdivisions,
refinements and pair-box counters. Existing policy ceilings bound polynomial
degree, dense solve size, candidate intervals, subdivisions and pair boxes.
Work-limit exhaustion is a result state, not permission to publish a partial set
as complete. Timing remains characterization only.

## 8. Scholarly work still required

The original G9S1 characterization asserted no new bibliographic title, author,
DOI or theorem. The separately labeled post-closeout R1 review in section 10
records subsequently inspected sources without changing G9S1's guarantees.
Before publication claims method-specific theorems or certification, verify and
record suitable primary/scholarly sources for:

- Classic-family polynomial spline interpolation and continuity equations;
- stable power/Bernstein conversion and convex-hull exclusion;
- recursive derivative-root isolation and multiple-root limitations in finite
  precision;
- interval root counting and interval Newton/Krawczyk guarantees if adopted;
- robust parametric/implicit and parametric/parametric curve intersection;
- controlled-error spline arc length; and
- deterministic root labeling through topology transitions if a general theorem
  is cited beyond the repository's narrower contract.

Search the existing research corpus first. An absent source remains a research
requirement; it must never be replaced by an invented citation.

## 9. Candidate boundary

This characterization records what the author-approved G9S1 implementation
does. ADR 0018 is Accepted and G9S1 is PASS, but neither fact certifies global
root completeness or authorizes pair-root materialization.

## 10. G9S1-R1 preimplementation research update (2026-09-03)

R1 investigation/implementation is separately authorized, subject to its stop
conditions. It has stopped before productive mutation at a symmetric-selector
design question. The [source-backed review and analytic counterexamples](../architecture/g9s1_r1_pair_materialization_design_review.md)
do not change the rich-only published behavior or the accepted G9S1 report.

Checked primary/scholarly authority:

- S. M. Rump, *Verification methods: Rigorous results using floating-point
  arithmetic*, Acta Numerica 19 (2010), 287–449,
  [DOI 10.1017/S096249291000005X](https://doi.org/10.1017/S096249291000005X).
  The [author manuscript](https://www.tuhh.de/ti3/rump/intlab/ActaNumerica2010.pdf),
  sections 2, 4, 5 and 13, was inspected. Theorem 13.3 supplies the smooth
  nonlinear Krawczyk inclusion hypotheses; it does not assign durable roots.
- R. Krawczyk, *Newton-Algorithmen zur Bestimmung von Nullstellen mit
  Fehlerschranken*, Computing 4 (1969), 187–201,
  [DOI 10.1007/BF02234767](https://doi.org/10.1007/BF02234767).
  Publisher metadata/abstract were checked; the inspected theorem text used
  here is Rump's, not an assertion of full-text inspection of this paper.
- T. W. Sederberg and T. Nishita, *Curve intersection using Bézier clipping*,
  Computer-Aided Design 22(9) (1990), 538–549,
  [DOI 10.1016/0010-4485(90)90039-F](https://doi.org/10.1016/0010-4485(90)90039-F).
  Publisher metadata/abstract were checked. This is a discovery/exclusion
  reference, not evidence that the current rounded conversions are certified.
- [Java Language Specification 17, section 15.4](https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.4)
  provides the floating evaluation contract; the
  [Java Math API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Math.html)
  specifies adjacent-representable `nextDown`/`nextUp` operations.

The plausible bounded numerical approach is an internal finite interval type
and outward Horner/Jacobian Krawczyk proof on original semantic polynomial
authority. Smoothness, owning-span containment, coefficient provenance and
transformed composition must all be established; rounded Bernstein conversion,
approximate knot glue and numerical residual alone cannot discharge them.

The outstanding identity question is independent of that numerical method:
two projection ranks can change while distinct pair roots remain regular.
Moreover, rank among discovered roots is not a certified intrinsic rank without
selector-domain coverage. The review distinguishes these facts from any claim
of general mathematical impossibility. Broader algebraic/topological cell
research or an explicitly reviewed bounded chart-validity contract is still
needed before public pair token allocation.

## 11. Symmetric atlas and genuine monodromy (design iteration 2)

The author accepted the projected-rank counterexamples, not the fallback
invalidation at ordinary chart/rank boundaries. **DESIGN NOT APPROVED —
CONTINUE DESIGN** applies to R1. The [new design derivation](../architecture/g9s1_r1_symmetric_atlas_design.md)
preserves successful certified transitions and distinguishes the fiber boxes
from a semantic atlas over construction-state space and its durable sheets.

New inspected primary sources:

- Allen Hatcher, *Algebraic Topology*, Cambridge University Press, 2002,
  [author-hosted chapter 1](https://pi.math.cornell.edu/~hatcher/AT/ATch1.pdf),
  section 1.3, Proposition 1.30: covering path/homotopy lifting. A local
  nonsingular solve does not by itself prove all covering hypotheses.
- Jonathan D. Hauenstein and Margaret H. Regan, *Real monodromy action*,
  Applied Mathematics and Computation 373 (2020), 124983,
  [DOI 10.1016/j.amc.2019.124983](https://doi.org/10.1016/j.amc.2019.124983),
  [NSF-hosted text](https://par.nsf.gov/servlets/purl/10195953), section 3.1,
  Example 3.1 and Theorem 3.3. Real solution sheets can permute on regular loops;
  the simply-connected scope has additional restrictions. Their generic system
  is not claimed to be a SplineV2 pair.
- Timothy Duff and Kisun Lee, *Certified homotopy tracking using the Krawczyk
  method*, [arXiv:2402.07053v2](https://arxiv.org/html/2402.07053v2), 29 May 2024,
  section 3.2, Theorem 3.1. Parametric inclusion can certify a solution throughout
  a time interval, unlike merely certifying two endpoints. It does not provide
  global history-independent root labels. Its exact-real/affine-homotopy
  termination hypotheses are not a Java floating-point termination guarantee.

The new exact dyadic piecewise-cubic double-traversal/radial-segment witness is
derived in the design note itself. Its all-parameter determinant bound, exhaustive
two-root description and exact closed-loop transposition establish genuine
monodromy, not a projected-order artifact. Separate public-host floating spline
checks cannot silently replace exact C2-glue and all-parameter proof. This
research adds no productive interval/atlas/ledger code and claims no R1 PASS.

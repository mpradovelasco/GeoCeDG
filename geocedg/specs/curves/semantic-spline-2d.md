# Semantic Spline V2

- Status: **NORMATIVE — PASS — AUTHOR APPROVED**
- Version: 1
- Phase: **G9S1**
- Architectural layer: shared Java kernel
- Public result: experimental semantic `GeoLocusV2`
- Decision record:
  [ADR 0018 — Accepted](../../../docs/adr/0018-semantic-spline-2d-capability.md)

## 1. Scope and public boundary

G9S1 introduces `SplineV2` as a bounded public semantic spline constructor.
The result is a new first-class `GeoLocusV2` with normal dependencies and a new
durable ID. Classic `Spline`, its result type and every legacy overload remain
unchanged.

The candidate supports the current Classic interpolation family through the
separate forms `SplineV2[point-list]`, `SplineV2[point-list, degree]`,
`SplineV2[point-list, degree, weight-function]` and the documented point-list
wrapping form. The default degree is three. The bounded policy accepts 3–32
finite 2D interpolation points, integral degree 3 through
`min(point-count, 12)`, and a dense system of at most 512 unknowns. A weight
function, when supplied, evaluates each consecutive `(dx,dy)` increment and
must produce a positive finite value.

This is a floating polynomial interpolation family, not a NURBS, control-point
B-spline or arbitrary fitting contract. Equal first/last input points declare
the supported closed periodic form; otherwise the normalized oriented domain is
finite and nonperiodic.

## 2. Mathematical definition

A semantic spline consists of:

- an explicit oriented parameter domain;
- an ordered partition into stable semantic spans;
- a polynomial or otherwise explicitly supported local evaluator on each span;
- source-point/control/interpolation dependencies;
- degree/order and knot or break structure;
- continuity and validity metadata at every shared boundary; and
- open/closed/periodic policy where supported.

For each valid semantic address `(branch, component, span, t)`, evaluation is
independent of rendering. Undefined source data, invalid addresses and
nonfinite evaluations publish typed invalid states rather than stale
coordinates.

Repeated points, repeated knots, zero-length spans, cusps, self-intersections,
coincident spans, collapsed images and temporarily undefined inputs require
explicit typed consequences.

## 3. Domain, orientation and knots

Parameter authority belongs to the semantic spline definition. Render index,
Cartesian arc-length order and construction list order are not parameters.

Adjacent spans use one canonical ownership rule at a shared knot. Equivalent
representations of the same knot root must not publish duplicate solutions.
Distinct semantic preimages remain distinct even when their Cartesian images
coincide.

Reversing semantic orientation is a different parametrization and may reverse
intrinsic rank. It is not nondeterminism. Redefinition follows existing
revision/identity rules and cannot be repaired from coordinates.

## 4. Public operations

The output participates through existing Locus V2 authorities in:

- semantic evaluation and Point-on-Locus;
- total and partial length through the ordinary guarded `Length` scalar surface
  and the diagnostic-rich `LocusLength` surface;
- rich intersections;
- copy, undo/redo and native `.cedg` persistence;
- translation, rotation, point/line reflection and uniform dilation; and
- further supported transformations and downstream DAG recomputation.

No `SplineV2Length` or `IntersectSplineV2` parallel command is introduced.
`Length(S)` and `Length(S,P,Q)` are the ordinary scalar forms. `LocusLength(S)`
and `LocusLength(S,P,Q)` retain computation status, coverage, guarantee, error
and diagnostics as a rich nonnumeric result. Partial endpoints must carry exact
semantic addresses on the same spline; Cartesian coincidence never repairs an
invalid provenance.

## 5. Intersection contract

Candidate discovery is span-aware. Current public target families are:

1. line, segment and ray;
2. circle and supported conic;
3. regular supported polynomial implicit curve;
4. bounded supported function graphs through the existing general rich
   capability; and
5. semantic piecewise-polynomial locus × semantic piecewise-polynomial locus
   through the bounded rich-only pair capability.

The algorithm separates:

1. broad span/target exclusion;
2. candidate isolation;
3. refinement in original semantic parameters;
4. residual and conditioning evidence;
5. multiplicity/contact classification;
6. deterministic semantic selector construction;
7. local materialization admissibility; and
8. global completeness.

Local admissibility is not global completeness. A locally isolated,
deterministically identified solution may be materializable while complete
enumeration remains `NOT_ESTABLISHED`. A globally complete but locally
ambiguous/nonisolated solution remains nonmaterializable.

### 5.1 One-sided polynomial authority

Line, segment, ray, circle, supported nondegenerate conic and regular
polynomial-implicit targets expose polynomial coefficients. On each semantic
spline span the candidate composes that implicit polynomial with `C(t)`,
recursively isolates roots of its derivative, partitions the residual into
deterministic monotone cells and refines accepted cells. This detects transverse
roots and can discover even-contact roots without relying only on sign changes.

Only transverse roots with established local isolation (including the required
two-sided periodic-seam evidence) enter the existing R4 selector/token authority
and may be materialized. Tangencies, zero-polynomial spans/overlap and unresolved
cells remain rich evidence and fail closed. Arithmetic evidence is
`ESTIMATED_ERROR`; global completeness remains `NOT_ESTABLISHED`.

### 5.2 Pair boundary

For two piecewise-polynomial semantic loci, the candidate uses canonical
span-pair traversal, Bernstein convex-hull rejection, bounded subdivision and
safeguarded dual-parameter Newton refinement. It deduplicates canonical knot
and periodic-seam representations and reports finite/overlap/work-limit evidence
symmetrically under operand exchange.

Floating boxes do not prove exhaustive interval-rounded rectangle coverage or
unique pair isolation. Therefore current pair candidates are deliberately
**rich-only**: local isolation and public continuation identity are
`NOT_ESTABLISHED`, no active public-ledger allocation is created, and an
intersection point cannot be materialized from a pair diagnostic token. A
future extension requires a separately reviewed symmetric rectangle+uniqueness
certificate and selector; it may not inject a one-sided rank or infer identity
from coordinates/order.

## 6. Deterministic identity

For a one-sided query, permitted selector evidence includes durable source-pair
context, branch/component/span lineage, canonical oriented parameter cell,
typed germ/contact and current topology certificate. Pair diagnostics use a
canonical symmetric source/pair context but are not durable public selector or
token-ledger authority under G9S1.

Forbidden identity authority includes coordinates, nearest-root matching,
screen state, list/index order, solver enumeration, sampling index and movement
history.

R4 intrinsic phase/rank and active/dormant/reactivated lifecycle remain
authoritative. Spline spans refine semantic address evidence; they do not
replace the opaque exact token ledger. Exact token means exact identity
matching, not exact arithmetic.

## 7. Numerical truth and work limits

The selected solver must be deterministic and report:

- arithmetic/numerical guarantee;
- residual and refinement state;
- local isolation and multiplicity evidence;
- overlap/nonisolated evidence;
- global completeness;
- unresolved candidates; and
- explicit work-budget exhaustion.

Tangencies and even-multiplicity roots cannot rely on sign changes alone.
Unsupported or ill-conditioned cases fail closed with diagnostics. Viewport,
zoom, DPI and render tessellation never alter semantic results.

## 8. Length

Total and partial length operate spanwise over the analytic semantic derivative,
split at provider knots and validity boundaries. The current shared capability
uses deterministic adaptive Simpson integration with the existing
absolute/relative tolerance, Richardson-style estimate and explicit work limit.
It reports controlled numerical evidence, never symbolic/exact arc length.

Partial endpoints are semantic addresses. Coincident Cartesian points with
multiple preimages are not silently assigned. R5 covariance remains:
isometries preserve length and a uniform dilation by `k` scales it by
`|k|`, including the approved collapsed-image `k=0` result.

## 9. Transformations

For every supported R5 map `T`, the transformed locus evaluates
`C'(t)=T(C(t))` and retains domain, span ownership, orientation and valid
semantic addresses. It receives a new durable identity. Downstream intersection
selectors and tokens belong to the transformed source-pair context and are not
copied from the source query.

## 10. Persistence and compatibility

Serialization records reconstructible spline family/version, source
dependencies, degree/order, domain/span/knot semantics and the normal
`GeoLocusV2` durable identity. It never serializes render vertices, caches,
solver enumeration or opaque executable objects.

Feature-off and Classic routes preserve supported documents without enabling
experimental creation. The `.cedg`/`.ggb` policy and transactional native-open
contract remain unchanged.

## 11. Efficiency

The pipeline reads immutable provider polynomial spans once per current
revision, performs target broad-phase exclusion, isolates/refines only remaining
cells, and resolves materialized one-sided token bindings by the existing
selector map.
No child point triggers an independent whole-curve solve and no movement
trajectory history is retained.

Instrumentation records polynomial spans examined/rejected and raw root
candidates in addition to existing evaluation/subdivision/refinement counters.
Explicit work ceilings fail coherently. No asymptotic or completeness claim is
inferred from timing alone; final observed counts belong to validation evidence.

## 12. Required validation

The normative validation matrix is
[G9S1 validation](../../../docs/validation/g9s1_semantic_spline_2d_capability_validation_matrix.md).
It includes analytic curves, knot roots, tangent/multiple roots, repeated and
degenerate spans, dynamic updates, path/enumeration independence, persistence,
length/intersection/transform covariance, the explicit rich-only pair boundary
and historical G5–G9 regressions.

## 13. Exclusions

G9S1 does not implement G9U1, markers, auto-materialization, workspace/profile
UI, 3D splines, surfaces, CAD feature trees, arbitrary NURBS, a generic `Path`
contract or Classic `Spline` migration.

## 14. Terminal state

This specification is normative under `G9S1 = PASS — AUTHOR APPROVED`.
`selfApproved=false`, `authorApproved=true`, and `passClaimed=true`. The phase
does not authorize G9U1 or broaden the explicit rich-only pair boundary.

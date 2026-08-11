# Locus V2 semantic contract

- Status: **APPROVED AS NORMATIVE G6 SEMANTIC CONTRACT**
- Version: `1.1`
- Approval date: 2026-08-11
- Roadmap gate: G6A `PASS`; G6B `PASS`; G6 `PASS`
- Affected layer: shared Java kernel semantics; derived rendering
- Architecture decision: Accepted ADR 0006

## Objective

Define the minimum semantic object that an explicitly authorized G6B may
implement.
A Locus V2 is a dynamic, parameterized two-dimensional geometric entity. Its
identity is independent of viewport, zoom, DPI, render tessellation and elapsed
render budget. Approval of this contract closes G6A but does not itself start
G6B or authorize work outside its restricted boundary.

## Semantic object

For locus identity `l` and semantic revision `r`, a versioned driver-domain
provider publishes a finite set of semantic branches `B_j`. Each branch has:

```text
branchKey
declaredDriverDomain
validDomainComponents[]
semantic orientation
provider/evaluator
provenance
lineage
quality metadata
```

For branch `j`, let `Omega_j` be its declared oriented domain and let
`V_j subseteq Omega_j` be the union of its valid components. The evaluator is

```text
F_(l,r,j) : V_j -> R^2.
```

The locus image is the union of all branch images. A branch is a constructively
identifiable solution or sheet; it is not created automatically for every
connected component of `V_j`. Undefined intervals may split a branch's valid
domain without changing `branchKey`.

The semantic address `(l,r,branchKey,t)` and the geometric point `F(t)` are
different concepts. Several addresses may map to the same Cartesian point.

## Driver-domain providers and parameters

The semantic parameter is supplied by a versioned provider. A provider must
declare its parameter descriptor, domain, orientation, endpoint policy,
periodicity, native mapping and stability conditions. The parameter may equal a
GeoGebra native path parameter only when the provider explicitly proves it
suitable and stable. Public normalized `PathParameter[...]` in `[0,1]` is never
automatic semantic identity.

G6B's approved minimum contains two internal providers:

1. `explicit-numeric-domain/v1`: a construction-owned finite interval, with
   explicit open/closed endpoints and optional periodicity; and
2. `stable-path-domain/v1`: a point-on-path driver restricted to path types for
   which G6A has recorded a construction-independent native mapping. The first
   approved fixtures are segment `[0,1]` and circle/ellipse angular parameter
   `[-pi,pi)`; view-derived function domains are excluded.

This common provider protocol does not claim that slider and path drivers have
identical semantics. A future CeDG provider may publish one shared parameter for
two projections even when their internal 2D `Path` parameterizations differ.

## Branch identity and lineage

`branchKey` is a deterministic provider descriptor derived from constructive
role and provider version. It must not depend on samples, coordinates, visual
order, proximity, labels or current screen orientation. A geometric crossing
does not change identity. Apparent direction on screen does not change semantic
orientation.

Lifecycle is separate from identity. Providers publish typed transitions:
`UNCHANGED`, `APPEARED`, `DISAPPEARED`, `SPLIT` and `MERGED`, including parent
and child keys for real split/merge events. A deterministically recognized
branch recovers its previous key after an inactive interval. Domain-component
split/merge is recorded independently and does not imply branch lineage.

The executable G6A fixture in
`geocedg/validation/locus-v2/topology-fixture.yml` is the reference policy.

## Status and degeneration axes

The following axes are independent:

- definition status: `VALID`, `EMPTY_DOMAIN`, `DRIVER_INVALID`, `UNSUPPORTED`;
- branch/domain properties: `FINITE`, `UNBOUNDED`, `PERIODIC`,
  `COLLAPSED_IMAGE`, plus only provider-justified additions;
- evaluation status: `VALID`, `OUT_OF_DOMAIN`, `DEPENDENCY_UNDEFINED`,
  `NON_FINITE`, `EVALUATION_FAILED`, `UNSUPPORTED_NONDETERMINISM`;
- optional regularity: `REGULAR`, `SINGULAR`, `UNKNOWN`;
- topology/lineage transition: `UNCHANGED`, `APPEARED`, `DISAPPEARED`, `SPLIT`,
  `MERGED`.

A cusp may be a valid evaluation with regularity `UNKNOWN`; G6B need not infer
differential regularity. Empty domains, isolated valid components, collapsed
images, unbounded branches and non-finite results remain explicit and may not
reuse stale coordinates or render data.

## Determinism and evaluator

The minimum read-only contract is conceptually:

```text
definition(revision)
branches(revision)
domain(branchKey, revision)
evaluate(branchKey, semanticParameter, revision, evaluationContext)
```

Every provider/evaluator is classified as:

- `POINTWISE_DETERMINISTIC`: direct evaluation is independent of query history;
- `CANONICAL_CONTINUATION_DETERMINISTIC`: a declared anchor, orientation and
  continuation rule produce the same result independently of external query
  order; or
- `UNSUPPORTED_NONDETERMINISM`: no approved reproducible rule exists.

G6B supports the first category and only an explicitly characterized example
of the second. Unsupported nondeterminism returns a typed status. Derivatives,
tangents, interval bounds, metrics and intersections are optional future
capabilities, not G6B requirements.

## Quality and numeric guarantee

Quality metadata has four independent axes:

1. construction fidelity: whether the evaluator follows the approved
   construction or an explicit approximation;
2. evaluation method: analytic expression, deterministic numeric dependency
   evaluation or canonical numeric continuation;
3. representation role: semantic result, validation sample, render
   tessellation or future operation-specific approximation; and
4. numeric guarantee: `EXACT_ARITHMETIC`, `CERTIFIED_ERROR_BOUND`,
   `ESTIMATED_ERROR`, `FLOATING_POINT_UNCERTIFIED`.

An analytic expression evaluated with `double` is not exact arithmetic. The
G6A analytic fixtures use construction fidelity `SEMANTICALLY_CONSTRUCTED`,
method `ANALYTIC_EVALUATION`, role `SEMANTIC_RESULT`, and guarantee
`FLOATING_POINT_UNCERTIFIED` unless a stronger bound is actually produced.

The approved G6B validation comparison for a documented characteristic
world-coordinate geometric scale `S` is:

```text
eps_eval(S) = max(1e-12 * max(1, S), 64 * ulp(max(1, S))).
```

This is an uncertified validation envelope derived from the pinned kernel's
`MAX_PRECISION` order and measured Level-A residuals; it is not a geometric
error certificate. `S` must be derived from a case-relevant geometric magnitude
such as a defining length, radius or documented local construction scale. It
must not depend on zoom, DPI, viewport, pixel density or absolute distance from
the coordinate origin. A case without a defensible characteristic scale must
declare that limitation rather than infer `S` from display state or coordinate
offset. Provider endpoint predicates use an independently declared
`eps_domain`. Pixel tolerances are render-only. G7 and G8 must define separate
metric and root/residual tolerances.

## Dependency graph, revisions and nested composition

Normal `AlgoElement` inputs and outputs remain the dependency authority. Each
V2 definition publishes one monotonically increasing semantic revision per
normal recompute that changes its semantic snapshot. Point evaluation does not
publish a new revision and must not reconstruct a second hidden graph.

A downstream V2 locus consumes an upstream V2 locus only through branch/domain,
semantic evaluation, revision, state and quality metadata. Render vertices,
sampled polylines, render caches and whole-locus regeneration are prohibited.

G6B shall use recursive semantic evaluator composition with a scoped shared
evaluation session as its minimum. Its memoization key contains locus
identity, semantic revision, branch key and the provider-canonical parameter
bits. Duplicate keys in one batch are evaluated at most once; cache-enabled and
disabled results must agree. An active-key stack rejects hidden callback cycles
with a diagnosable failure. Controlled DAG flattening remains deferred unless a
measured bottleneck justifies the extra coupling.

For controlled pointwise fixtures with `q` outer requests and dependency depth
`d`, the approved functional budget is `q*d` semantic evaluator calls without
duplicate requests, plus fixed preparation/synchronization. Memoization is
bounded and scoped. Slice construction may occur at most once per locus
definition/revision, never per point query. The normal kernel DAG remains the
dependency and invalidation authority. Absolute timing and retained-memory
budgets remain informational until productive G6B measurements are repeatable.

## Forward compatibility of derived semantic services

Derived semantic services consumed by downstream constructions must preserve
semantic composition. In particular, future G7 metric operations must be
revision-scoped and must consume Locus V2 semantic data rather than render
samples or whole-locus regeneration.

A future metric used as a dependency of another Locus V2 must:

- be associated with the upstream locus semantic revision;
- never consume `LocusRenderCache2D` or sum legacy sampled chords;
- avoid recomputing the complete metric for every downstream evaluation while
  the upstream semantic revision is unchanged; and
- use cache invalidation consistent with the normal kernel DAG.

This is a forward architectural requirement only. G6 does not implement G7 or
define its metric tolerances or public API.

## Render and caches

Semantic definition/evaluation and render caches are different authorities:

```text
LocusDefinition2D + LocusEvaluator2D != LocusRenderCache2D
```

Render tessellation may depend on viewport, zoom, DPI and visual tolerance.
Those inputs may not change definition status, domains, keys, revision or
semantic evaluations. The minimum cache policy is immutable definition per
revision, optional scoped evaluation memoization, and a bounded per-view render
cache keyed by semantic revision and view/render policy.

## Approved compatibility boundary for G6B

- parallel experimental entity; legacy `GeoLocus` remains unchanged;
- no public command and no redirection of `Locus[...]`;
- no `.ggb` persistence or migration;
- no public `Path` implementation;
- internal/test factory only;
- diagnostic `LEGACY`, `V2` and `DUAL` modes do not alter Classic defaults;
- distinct appended V2 `GeoClass`/classification, preserving every existing
  ordinal; do not reuse `GeoClass.LOCUS`;
- V2 reports neither `isGeoLocus()` nor `isGeoLocusable()` in G6B and remains
  outside legacy `Path`, metrics, commands, XML and 3D dispatch;
- no G7 length, G8 intersections, G9 spatial semantics or G5 locus export.

## Validation authority and phase gate

The validation matrix, tolerance policy and characterization baseline under
`geocedg/validation/locus-v2/` are the approved G6A evidence and the normative
input to G6B. G6A is `PASS`; ADR 0006 is `Accepted`. The author subsequently
authorized G6B through the hash-pinned canonical prompt. Productive G6B code
must conform to this contract; implementation notes below do not weaken or
reinterpret it.

The evidence gap is resolved by two author-supplied, hash-pinned legacy models.
`InterCilConoObliqueTwoLevels.ggb` is the functional two-level control
(approximately 125–127 ms in the recorded run). `InterCilConoOblique.ggb` is
the pathological reference: before `Flatten` recompute was approximately
31.9 ms; its three third-level creations took approximately 6.03/5.95/5.67 s,
all became undefined after exceeding the legacy 500 ms per-step guard, and the
subsequent recompute was approximately 21.0 s.

Instrumentation shows that each outer `AlgoLocusSliderND` sampling evaluation
updates a dependency slice containing two inner locus algorithms and two
`AlgoPerimeterLocus` algorithms. It therefore regenerates upstream geometric
and sampled-metric work instead of consuming composable semantic evaluators.
This causal statement is limited to the measured model and run; it is not a
universal complexity claim about every legacy nested locus.

The originals, hashes, manifests and inventories remain immutable legacy
scientific/operational evidence with public redistribution blocked pending
rights and asset review. Because G6B has no public command or persistence, its
required three-level V2 demonstrator shall be a small internal typed fixture
with explicit traceability to these originals. The pathological original
remains a legacy comparison and does not require G6B to implement G7
`Perimeter` or reproduce its sampled coordinates as V2 truth.

## G6B implementation conformance profile

The minimal implementation realizes this contract through:

- immutable `LocusDefinition2D`, `LocusBranch2D`, domain, lineage, evaluation
  and quality values under `org.geocedg.common.kernel.locus`;
- `explicit-numeric-domain/v1` plus the characterized
  `stable-path-domain/v1` mappings for segment, circle and ellipse; only the
  segment provider currently has a live kernel-DAG algorithm;
- a parallel `GeoLocusV2` with append-only `GeoClass.LOCUS_V2`;
- an internal `AlgoLocusV2` family that publishes immutable snapshots through
  normal `AlgoElement` input/output dependencies;
- recursive `AlgoNestedLocusV2` evaluation through a disposable bounded
  `LocusEvaluationSession2D` with the full semantic key and active-key cycle
  detection; and
- a dedicated `DrawLocusV2` and bounded per-drawable `LocusRenderCache2D`
  derived only from semantic evaluation.

G6B implements `POINTWISE_DETERMINISTIC`. Canonical continuation remains
unsupported until a provider with an approved anchor, orientation and
continuation rule exists. The numeric comparison envelope remains
`FLOATING_POINT_UNCERTIFIED`; no certified error claim is introduced.

The implementation is deliberately internal and nonpersistent. The typed
`LEGACY`, `V2` and `DUAL` values are a factory/test diagnostic seam, not an
end-user preference and not a public command switch. `Locus[...]` remains
legacy in both Classic and GeoCeDG. Versioned execution evidence is stored in
[`g6b-functional-evidence.yml`](../../validation/locus-v2/g6b-functional-evidence.yml).

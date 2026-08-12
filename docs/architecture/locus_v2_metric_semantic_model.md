# Locus V2 metric semantic model

| Field | Value |
|---|---|
| Status | Author-reviewed G7 planning model; no metric implementation |
| Mathematical authority | Total variation on each valid-domain component |
| Upstream semantic authority | [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md) |
| Proposed metric contract | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Architecture | [`locus_v2_metric_architecture.md`](locus_v2_metric_architecture.md) |
| Date | 2026-08-12 |

This document defines the semantic vocabulary that G7A must characterize and
that an explicitly authorized G7B may later implement. It is planning and
design evidence, not a normative or productive API. G7A, G7B and G8 remain
`NOT STARTED`.

## 1. Mathematical object

For locus identity `l`, semantic revision `r` and constructive branch `j`, the
G6 contract supplies an evaluator

\[
F_{l,r,j}:V_{r,j}\longrightarrow\mathbb R^2,
\]

where the valid domain is decomposed into connected components

\[
V_{r,j}=\bigcup_k C_{r,j,k}.
\]

For a parameter interval or oriented subinterval `I` contained in one valid
component, length is total variation:

\[
\operatorname{Var}(F;I)=
\sup_{P}\sum_i\lVert F(t_i)-F(t_{i-1})\rVert.
\]

When `F` is absolutely continuous on `I`, the equivalent differential formula
is

\[
\operatorname{Var}(F;I)=\int_I\lVert F'(t)\rVert\,dt.
\]

The supremum, not a particular partition, is the definition. An analytic
formula, differential quadrature or adaptive evaluator-only method is a way of
establishing or approximating that value; it does not redefine length.

The following are prohibited metric authorities:

- render vertices or `LocusRenderCache2D`;
- legacy `GeoLocusND.myPointList` or `PathMoverLocus`;
- legacy samples or sampled chord sums;
- viewport bounds, zoom, DPI or pixel tolerance.

## 2. Metric meaning and multiplicity

The minimum G7 meaning is:

```text
CONSTRUCTIVE_TRAVERSAL_LENGTH
```

Length is accumulated over constructive parameter traversals. Therefore:

- retracing contributes once for every preimage traversal;
- distinct constructive branches that have coincident images contribute
  separately;
- branch and valid-component provenance remains visible in the result; and
- a Cartesian union or deduplication of coincident image sets is not performed.

The alternative

```text
GEOMETRIC_IMAGE_UNION_LENGTH
```

is outside minimum G7. It would require overlap classification and geometric
measure on image unions, which is a different operation.

## 3. Two different operations

G7 defines two independent query types.

### 3.1 Between-position length

```text
BetweenPositionsMetricQuery
```

This operation selects one route from semantic position `A` to semantic
position `B`. It includes direction, same-position policy and open-boundary
policy. Its result records whether the target was reached and whether the
selected route is geometrically connected.

### 3.2 Complete locus length

```text
TotalLocusMetricQuery
```

This operation has no `A`, no `B` and no direction as semantic input. It is not
encoded by `A == B`, `FULL_CYCLE` or `WRAP_TO_START`.

Its mathematical value is

\[
\mathcal L_{\mathrm{total}}(L_r)=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Every valid-domain component contributes exactly once. No chord is inserted
between disconnected components, no route is fabricated across a gap, and a
periodic branch contributes exactly one provider-declared fundamental cycle.

## 4. Durable semantic positions and revision bindings

A durable semantic position is proposed as the immutable value:

```text
LocusSemanticPosition2D
    locusIdentity
    branchKey
    providerVersion
    providerCanonicalParameter
```

The position identifies a constructive parameter preimage. Cartesian
coordinates are neither identity nor repair data. The semantic revision does
not necessarily belong to durable identity.

A position used by a metric query must be resolved for one concrete semantic
snapshot:

```text
MetricPositionBinding2D
    semanticPosition
    semanticRevision
    resolvedValidComponentKey
    evaluationStatus
    evaluatedPoint
    diagnostics
```

`resolvedValidComponentKey` is revision-scoped. It is an address into the
current topology, not a durable position identity. A component split does not
silently mutate `LocusSemanticPosition2D`.

Binding rules are:

1. binding uses locus identity, branch key, provider version and canonical
   parameter only;
2. coordinate proximity, labels, visual order and sample order are forbidden;
3. a binding from another semantic revision is `POSITION_STALE`;
4. a stale binding is never repaired automatically; rebinding is a separate,
   explicit operation against a named revision;
5. explicit rebinding may succeed only when the same semantic identity resolves
   unambiguously in that revision; and
6. branch disappearance, provider mismatch, invalid parameter or ambiguous
   component membership produces a typed diagnostic rather than stale data.

## 5. Direction and same-position policy

Metric length is always non-negative. Direction chooses a route; it does not
give the result a sign.

```text
TraversalDirection
    FORWARD
    REVERSE
```

`FORWARD` follows the provider-declared semantic orientation and `REVERSE`
follows the opposite orientation. Both are required in the minimum G7B
candidate. `SHORTEST` is deferred; G7A may characterize it, but it is neither a
default nor a minimum implementation requirement.

When `A` and `B` are the same semantic position, the query must choose:

```text
SamePositionPolicy
    ZERO_LENGTH
    FULL_CYCLE
```

`ZERO_LENGTH` resolves an empty route with value zero. `FULL_CYCLE` is legal
only for an approved periodic provider and resolves exactly one fundamental
cycle in the selected direction. Neither policy is inferred from Cartesian
equality, because different preimages may share one point.

## 6. Open-branch boundary policies

Consider an open branch whose semantic forward order is:

```text
start --- B ------ A ----- end
```

The target is behind `A` for a `FORWARD` query.

### 6.1 `STOP_AT_END`

The route ends at the global branch boundary:

```text
value = length(A -> end)
targetReached = false
wrapped = false
geometricallyConnected = true
TraversalOutcome = STOPPED_AT_BOUNDARY
MetricCoverage = INCOMPLETE
```

The finite value is a partial route contribution, not the complete length from
`A` to `B`, and is not scalar-admissible by default.

### 6.2 `WRAP_TO_START`

The route uses an explicit metric convention:

```text
value = length(A -> end) + length(start -> B)
targetReached = true
wrapped = true
geometricallyConnected = false
TraversalOutcome = WRAPPED_TO_START
MetricCoverage = COMPLETE
```

No chord joins `end` to `start`. The convention does not close the geometry,
create incidence, change topology or make the route geometrically connected.

### 6.3 `STRICT`

The query refuses an unreachable target:

```text
MetricValueKind = ABSENT
targetReached = false
TraversalOutcome = TARGET_NOT_REACHABLE
MetricCoverage = INCOMPLETE
```

The query itself was interpreted successfully; `ABSENT` and the traversal
outcome carry the semantic refusal. `STRICT` is part of the minimum G7B
candidate.

### 6.4 Internal gaps

No boundary policy may cross an internal invalid-domain gap. Encountering one
produces `DISCONTINUITY_ENCOUNTERED`, `targetReached = false` and incomplete
coverage. A result may preserve the finite contribution established before the
gap, but it is not the requested complete `A`/`B` length and is never
scalar-admissible by default. `WRAP_TO_START` applies only to global branch
boundaries, never to an internal gap.

## 7. Route model

Route interpretation and numerical integration are separate responsibilities.

```text
LocusMetricRouteResolver2D
    resolve(BetweenPositionsMetricQuery, bindings, definition)

LocusMetricRoute2D
    locusIdentity
    semanticRevision
    branchKey
    orderedRouteSegments[]
    direction
    boundaryPolicy
    targetReached
    wrapped
    geometricallyConnected
    routeStatus

LocusMetricRouteSegment2D
    resolvedValidComponentKey
    startCanonicalParameter
    endCanonicalParameter
    segmentDirection
    endpointPolicy
    segmentRole
```

The route resolver interprets semantic positions, revision, branch,
valid-domain components, direction, periodic seams, `ZERO_LENGTH`,
`FULL_CYCLE`, `STOP_AT_END`, `WRAP_TO_START`, `STRICT`, global boundaries,
internal gaps and reachability. It never evaluates an integral or owns an
adaptive partition.

Every route segment lies wholly within one valid-domain component. A wrapped
open route contains two ordered segments and an explicit non-geometric seam;
it does not contain a connector segment.

## 8. Total aggregation and zero cases

A total query enumerates components from the immutable revision snapshot and
creates one contribution request for each `(branchKey,
resolvedValidComponentKey)`. The aggregator may sum disconnected
contributions, but it does not turn them into a route.

The author-reviewed working policy is:

| Case | Value | Coverage | Required diagnostic |
|---|---:|---|---|
| Empty domain | 0 | `COMPLETE` | `EMPTY_DOMAIN` |
| Isolated point | 0 | `COMPLETE` | `ISOLATED_COMPONENT` |
| Collapsed image | 0 | `COMPLETE` | `COLLAPSED_IMAGE` |

A valid zero is not absence, failure, unsupported behavior or numerical
underflow.

## 9. Rich result axes

`LocusMetricResult2D` is an immutable semantic value. It does not encode all
meaning in a `double` or one status enum.

### 9.1 Value kind

```text
MetricValueKind
    FINITE
    POSITIVE_INFINITY
    ABSENT
```

`FINITE` carries a non-negative finite value. `POSITIVE_INFINITY` is explicit
and is not a large finite sentinel. `ABSENT` carries no numeric payload. NaN is
not used as a semantic discriminator.

### 9.2 Coverage

```text
MetricCoverage
    COMPLETE
    INCOMPLETE
```

Coverage says whether all contributions required by the selected operation
were established. It is independent of whether a partial finite value is
available.

### 9.3 Computation status

```text
MetricComputationStatus
    SUCCESS
    INVALID_QUERY
    UNSUPPORTED
    NUMERICAL_FAILURE
    LIMIT_NOT_ESTABLISHED
```

`DIVERGENT` is not a generic failure. Positive infinity, non-rectifiability,
failure to establish an improper limit and a failed finite computation remain
distinct.

### 9.4 Rectifiability

```text
MetricRectifiability
    RECTIFIABLE
    NON_RECTIFIABLE
    UNDETERMINED
```

### 9.5 Between-position traversal outcome

```text
TraversalOutcome
    TARGET_REACHED
    STOPPED_AT_BOUNDARY
    WRAPPED_TO_START
    TARGET_NOT_REACHABLE
    DISCONTINUITY_ENCOUNTERED
```

### 9.6 Additional required metadata

The result also separates:

- construction fidelity inherited from the semantic construction;
- evaluator method used to obtain `F` or `F'`;
- metric/integration method;
- representation role;
- numeric guarantee;
- absolute and relative error information;
- units;
- query, algorithm, policy and tolerance provenance; and
- immutable contribution decomposition.

One proposed structural shape is:

```text
LocusMetricResult2D
    operationKind
    valueKind
    finiteValue?
    coverage
    computationStatus
    rectifiability
    traversalOutcome?
    targetReached?
    wrapped?
    geometricallyConnected?
    scalarAdmissibility
    constructionFidelity
    evaluatorMethod
    metricMethod
    representationRole
    numericGuarantee
    absoluteError?
    relativeError?
    units
    provenance
    contributionDecomposition[]
    diagnostics[]
```

## 10. Metric methods and guarantees

G7A must characterize this capability hierarchy:

1. analytic or closed-form metric capability;
2. differential quadrature from an approved derivative capability;
3. evaluator-only adaptive variation approximation; and
4. unsupported.

An analytic construction evaluated with `double` is not automatically exact
arithmetic. Differential quadrature must state its rule, stopping policy and
error guarantee. Evaluator-only refinement agreement cannot by itself claim a
certified bound:

- `CERTIFIED_ERROR_BOUND` requires a mathematically valid certificate;
- `ESTIMATED_ERROR` requires explicit regularity/error assumptions recorded in
  provenance;
- otherwise the result is `FLOATING_POINT_UNCERTIFIED`; and
- when no defensible method applies, return `UNSUPPORTED`.

A refined chord sum may be an operation approximation; it is never described
as exact length.

## 11. Metric aggregation

```text
LocusMetricAggregator2D
```

combines component or route contributions. It does not integrate a component.
Its responsibilities are:

- constructive-multiplicity preservation;
- non-negative value aggregation;
- absolute-error aggregation and derived relative error;
- weakest numeric-guarantee propagation;
- coverage and status propagation;
- rectifiability propagation; and
- immutable contribution decomposition.

The proposed deterministic rules are:

- finite established contributions add normally;
- established positive infinity dominates finite non-negative contributions;
- an unsupported or failed contribution makes coverage incomplete and remains
  visible even when another contribution establishes positive infinity;
- `NON_RECTIFIABLE` dominates `RECTIFIABLE`; otherwise an unresolved
  contribution makes rectifiability `UNDETERMINED`;
- compatible absolute bounds/estimates add; missing error information weakens
  the aggregate guarantee; and
- `ABSENT` is used when no defensible aggregate numeric value is established,
  while a known partial sum may be `FINITE + INCOMPLETE`.

G7A must turn the status/guarantee precedence into an executable truth table
before G7B is authorized.

## 12. Scalar admissibility

Rich-result existence, `GeoElement.isDefined()` and scalar admissibility are
different questions. The candidate scalar rule admits a value only when all
of the following hold:

- value kind is `FINITE`;
- computation status is `SUCCESS`;
- the selected query's semantic condition is satisfied;
- coverage is suitable for scalar consumption;
- no position is stale;
- no contribution is unsupported or failed; and
- any wrap was explicitly requested and valid.

Valid zero is admissible. A complete, explicitly requested `WRAP_TO_START`
result is a candidate admissible value even though it is not geometrically
connected.

The following are not scalar-admissible by default:

- `STOP_AT_END` partial results;
- `TARGET_NOT_REACHABLE`;
- `INCOMPLETE` coverage;
- `POSITION_STALE` or `DIFFERENT_BRANCH`;
- discontinuity, unsupported or numerical failure;
- `LIMIT_NOT_ESTABLISHED`;
- `ABSENT`; and
- positive infinity, pending an explicit later policy.

G7A must compare no generic numeric facade, a conditional read-only numeric
facet and an explicit numeric adapter. The working preference is the
conditional facet only if the real GeoGebra API can prevent inadmissible rich
states from entering generic arithmetic, lists, CAS and numeric algorithms.

## 13. Reparameterization

Total variation is invariant under orientation-preserving reparameterization.
G7A must retain the endpoint-degenerate monotone fixture `t = u^3` and add a
regular fixture such as

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1},\qquad c\ne0,
\]

on `[0,1]`. Its derivative is positive, so it preserves orientation. For an
approved curve `F`, the evidence must show

\[
\operatorname{Var}(F;[0,1])=
\operatorname{Var}(F\circ\phi;[0,1]).
\]

The `u^3` case separately tests a monotone map whose derivative degenerates at
an endpoint.

## 14. Unbounded and improper metrics

Unbounded semantics never use a viewport cutoff. G7A must distinguish:

- finite `A`/`B` length on an unbounded branch;
- finite improper total length;
- positive-infinite total length;
- non-rectifiable behavior;
- unsupported evaluator/provider capability; and
- an improper limit that was attempted but not established.

The last case is `LIMIT_NOT_ESTABLISHED`, not `NUMERICAL_FAILURE` or a finite
viewport approximation. The current G6 finite interval provider does not by
itself authorize a native infinite provider; G7A must map that source boundary
before selecting a G7B executable subset.

## 15. Arc coordinate

For one valid component and an approved component anchor `t_0`, derived arc
coordinate is

\[
s(t)=\operatorname{Var}(F;[t_0,t]).
\]

It may be cached as component-local metric data. It does not replace the
provider-owned semantic parameter, connect gaps, connect branches or create an
automatic public coordinate surface.

## 16. Semantic invariants

Every G7 characterization and implementation must preserve:

1. zoom, viewport, DPI and render-policy invariance;
2. constructive multiplicity and complete provenance;
3. no traversal across an internal invalid-domain gap;
4. non-negative length independent of route direction;
5. stable semantic positions distinct from revision bindings;
6. explicit periodic seam and same-position policy;
7. total query distinct from every between-position convention;
8. valid zero distinct from absence or failure;
9. immutable results and decomposition;
10. normal kernel-DAG invalidation; and
11. cache/index ON/OFF semantic equality.

## 17. Phase boundary

This model approves `GeoLocusMetricResult` as a G7A working architecture only.
It does not accept ADR 0007, make the proposed metric spec normative, authorize
G7A execution or start G7B. Public `LocusLength`, changes to `Length` or
`Perimeter`, public `Path`, point-on-locus, XML, persistence, G8 intersections,
G9 spatial semantics and G5 locus export remain outside G7B minimum scope.

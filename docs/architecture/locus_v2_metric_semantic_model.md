# Locus V2 metric semantic model

| Field | Value |
|---|---|
| Status | Author-approved G7A/G7A-R1 semantic model; G7B implementation author-approved |
| Mathematical authority | Total variation on each valid-domain component |
| Upstream semantic authority | [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md) |
| Normative metric contract | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Architecture | [`locus_v2_metric_architecture.md`](locus_v2_metric_architecture.md) |
| Date | 2026-08-13 |

This document defines the author-approved semantic vocabulary characterized by
G7A/R1 and governed by the normative G7 metric spec. The author-approved
productive G7B implementation realizes it as an internal API. G7A-R1, G7A and
G7B are `PASS — AUTHOR APPROVED`; G7 is `PASS`, and G8 remains `NOT STARTED`.

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

The durable semantic position is the immutable value:

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

### 9.1 Closed metric value

```text
MetricValueKind
    FINITE
    POSITIVE_INFINITY
    ABSENT
```

R1 established that the enum never travels beside a bare value. One closed
immutable `MetricValue2D` is exactly one of:

```text
FiniteMetricValue2D(non-negative finite value)
PositiveInfinityMetricValue2D
AbsentMetricValue2D
```

Only `FiniteMetricValue2D` can expose a present `OptionalDouble`.
`POSITIVE_INFINITY` is not a large finite sentinel and `ABSENT` has no numeric
payload. NaN, null, magic values and exceptions are not normal-state
semantics. Contributions and aggregate results use the same closed contract.

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
- typed absolute/relative error evidence, method, assumptions, certificate and
  applicability scope;
- units;
- query, algorithm, policy and tolerance provenance; and
- immutable contribution decomposition.

The accepted structural shape is:

```text
LocusMetricResult2D
    operationKind
    metricValue: Finite | PositiveInfinity | Absent
    coverage
    computationStatus
    rectifiability
    traversalOutcome: Optional<TraversalOutcome>
    targetReached?
    wrapped?
    geometricallyConnected?
    scalarAdmissibility
    constructionFidelity
    evaluatorMethod
    metricMethod
    representationRole
    errorEvidence
        G6 NumericGuarantee?  // absent only when not applicable
        absolute: ESTABLISHED(value) | NOT_ESTABLISHED | NOT_APPLICABLE
        relative: ESTABLISHED(value) | NOT_ESTABLISHED | NOT_APPLICABLE
        scope: COMPLETE_VALUE | REPORTED_PARTIAL_VALUE | NOT_APPLICABLE
        method / assumptions / certificate metadata
    units
    provenance
    contributionDecomposition[]
    diagnostics[]
```

The optional traversal member is present for between-position outcomes and
empty for total results, which have no traversal semantics. `null`, sentinels
and an artificial `NOT_APPLICABLE` traversal value are forbidden; an equivalent
separation into total and between-position result subtypes is also valid.

Error amount is a closed value:

```text
sealed MetricErrorAmount2D
    EstablishedMetricErrorAmount2D(non-negative finite amount)
    NotEstablishedMetricErrorAmount2D
    NotApplicableMetricErrorAmount2D
```

This makes a contradictory `state + OptionalDouble` pair unrepresentable. No
variant carries NaN, `-1`, magic zero or `null`.

## 10. Metric methods and guarantees

G7 directly reuses
`LocusSemanticMetadata2D.NumericGuarantee`, the normative productive G6 type.
It does not define an equivalent metric enum. A metric-specific immutable
evidence wrapper contains that type and adds only metric error information.

G7A characterized this capability hierarchy:

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

The accepted deterministic rules are:

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

Exact terms retain `EXACT_ARITHMETIC`; certified bounds add; any estimate makes
the established finite aggregate estimated; and an uncertified finite term
makes amounts not established. Infinity/absence have not-applicable evidence.
For a finite incomplete subtotal, evidence is explicitly scoped
`REPORTED_PARTIAL_VALUE` and does not bound the unknown complete total.

G7A turned the status/guarantee precedence into an executable truth table; the
author accepted those rules at final closeout.

## 12. Scalar admissibility

Rich-result existence, `GeoElement.isDefined()` and scalar admissibility are
different questions. The accepted scalar rule admits a value only when all
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

G7A compared no facade, a conditional read-only facet and an explicit adapter.
The real API exposes numeric participation through static interfaces with no
instance-level admissibility hook, so the conditional facet is rejected. The
author-approved choice is an explicit derived adapter while the rich Geo
implements no numeric interface and remains authoritative. In normative G6
vocabulary the admitted scalar guarantees are `EXACT_ARITHMETIC`,
`CERTIFIED_ERROR_BOUND` and `ESTIMATED_ERROR`; uncertified values remain
rich-only unless a later explicit policy approves them.

## 13. Reparameterization

Total variation is invariant under orientation-preserving reparameterization.
G7A retained the endpoint-degenerate monotone fixture `t = u^3` and added the
regular fixture

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

Unbounded semantics never use a viewport cutoff. G7A distinguished:

- finite `A`/`B` length on an unbounded branch;
- finite improper total length;
- positive-infinite total length;
- non-rectifiable behavior;
- unsupported evaluator/provider capability; and
- an improper limit that was attempted but not established.

The last case is `LIMIT_NOT_ESTABLISHED`, not `NUMERICAL_FAILURE` or a finite
viewport approximation. The current G6 finite interval provider does not by
itself authorize a native infinite provider. G7A mapped that source boundary;
G7B shall implement only the subset authorized by the normative spec and its
versioned prompt.

## 15. Arc coordinate

For one valid component and an approved component anchor `t_0`, derived arc
coordinate is

\[
s(t)=\operatorname{Var}(F;[t_0,t]).
\]

It may be cached as component-local metric data. It does not replace the
provider-owned semantic parameter, connect gaps, connect branches or create an
automatic public coordinate surface.

## 16. R1 deterministic work and shared derived state

Requested accuracy is attempted under a versioned `MetricWorkBudget2D` with
independent maximum evaluations, subdivisions and depth. Exhausting any guard
produces `Absent + LIMIT_NOT_ESTABLISHED`; it never changes total variation's
mathematical definition or turns a partial refinement into success. The whole
budget affects result/status and is therefore part of policy identity and the
complete component key.

Compatible metric results may reuse one immutable
`LocusMetricComponentState2D` from a dedicated per-locus owner. It contains
component-wide adaptive partition/cumulative arc-coordinate evidence,
capability/integration metadata and component-level error state. Sharing has no
semantic effect: the cache-off result
must match value, coverage, status, rectifiability, guarantee, error evidence,
contributions and diagnostics. The owner neither selects a route nor connects
queries. It is bounded derived state with no dependency edges, and it is
invalidated by revision/topology/undefined/removal transitions.

The complete key is component-scoped and contains no A/B endpoints. Lookup and
evaluation are deliberately separate:

```text
complete component key -> immutable component metric state
component metric state + route segment -> route-specific contribution
```

A total query evaluates the complete component extent. A between-position
query evaluates the subarc of each route segment. One state can produce many
contributions; the owner shares no route, query result, contribution or
aggregate result.

For N compatible consumers and one complete key, the accepted hard budget is
one component-state build until eviction or invalidation. This is a work bound,
not a change in metric identity.

On a transition from successful revision `r` to a failed computation at
revision `r+1`, the old success becomes non-current before evaluation. P1
publishes no failed index entry but does publish one coherent `Absent` rich
failure snapshot for `r+1`; scalar access is undefined. Old-value/new-status
hybrids, stale current success and partial entries are impossible states.

## 17. Semantic invariants

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

## 18. G7A characterization result

The executable test-private model confirmed the operation split, constructive
multiplicity, revision-separated binding, route semantics and orthogonal result
axes. The difficult infinite-plus-unsupported aggregate retains a known
positive-infinite value with incomplete coverage and both diagnostics.

The accepted scalar rule adds an adequate-guarantee requirement to the
finite/success/complete/satisfied predicate. Rich publication is defined when
an immutable result snapshot exists, including an absent or failed diagnostic;
scalar-defined state is independent. Because GeoGebra numeric participation is
type-static, the rich Geo must not implement `NumberValue`. An explicit derived
adapter is required when generic scalar consumption is requested.

The accepted aggregate precedence and exact initial values are recorded
in the [G7A report](../validation/g7a_locus_v2_metric_characterization_report.md)
and [developer API](../developer/locus_v2_metric_api.md). The author accepted
them and the normative spec now governs G7B.

The focused
[G7A-R1 report](../validation/g7a_r1_locus_v2_metric_refinement_report.md)
adds closed value/error contracts, direct reuse of the G6 guarantee,
deterministic work guards, dedicated shared ownership and P1 failure
publication. These are author-approved.

## 19. Phase boundary

This model records `GeoLocusMetricResult` as the author-approved and implemented
G7B architecture. ADR 0007 is Accepted, the G7 metric spec is normative, G7B is
`PASS — AUTHOR APPROVED`, and G7 is `PASS`. Public `LocusLength`, changes to
`Length` or `Perimeter`, public `Path`, point-on-locus, XML, persistence, G8
intersections, G9 spatial semantics and G5 locus export remain outside G7B
minimum scope.

## 20. Productive semantic realization

The G7B implementation preserves each invariant above with immutable values in
`org.geocedg.common.kernel.locus.metric`. `TotalLocusMetricQuery` exposes no
traversal outcome; between-position results expose an
`Optional<TraversalOutcome>` only where traversal applies. Closed value and
error variants make absence, infinity and unavailable error amounts explicit.

The route resolver consumes revision-bound semantic positions and emits only
single-component route segments. The metric engine obtains immutable component
state, derives segment-specific contributions, and delegates only aggregation
to `LocusMetricAggregator2D`. This realizes total variation and constructive
multiplicity without making the numerical partition the mathematical
definition.

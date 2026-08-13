# Locus V2 metric contract

- Status: **APPROVED AS NORMATIVE G7 METRIC CONTRACT**
- Version: `1.0`
- Approval date: 2026-08-13
- Author-review disposition: **G7A-R1, G7A AND G7B PASS — AUTHOR APPROVED**
- Roadmap gate: G7 `PASS`; G7A `PASS`; G7B `PASS`
- Affected layer: shared Java kernel semantics and internal developer laboratory
- Working architecture: `GeoLocusMetricResult` as a normal kernel-DAG result
- Architecture decision: Accepted ADR 0007
- Date: 2026-08-13

This normative contract records the author-approved G7 planning requirements,
all 42 G7A recommendations, R1-1..R1-22 and the three API normalizations
approved at final closeout. It does not override the normative G6 Locus V2
semantic contract or create a public API. The productive internal G7B
implementation conforms to this contract and is author-approved.

## 1. Scope

The G7 minimum adds native semantic length services for the existing
experimental `GeoLocusV2`. It defines:

- between-position and complete-locus operations;
- durable semantic positions and revision-scoped bindings;
- route resolution independent of integration;
- constructive traversal multiplicity;
- rich immutable results and contribution decomposition;
- component integration and aggregation;
- a revision-scoped bounded metric-index hypothesis;
- publication through a dedicated GeoElement in the normal kernel DAG; and
- internal/developer-only inspection.

It does not define public commands, persistence, public `Path`, point-on-locus,
intersections, spatial behavior or locus export.

## 2. Mathematical definition

For each branch evaluator `F` and each route interval or valid-domain component
`I`, length is total variation:

\[
\operatorname{Var}(F;I)=
\sup_P\sum_i\lVert F(t_i)-F(t_{i-1})\rVert.
\]

For absolutely continuous `F`:

\[
\operatorname{Var}(F;I)=\int_I\lVert F'(t)\rVert\,dt.
\]

The numerical algorithm does not define the quantity. Render vertices,
`LocusRenderCache2D`, legacy samples, `myPointList`, viewport, zoom, DPI and
pixel tolerance shall not be metric inputs or authority.

## 3. Metric semantics

The normative minimum metric is
`CONSTRUCTIVE_TRAVERSAL_LENGTH`. Retracing counts by preimage and distinct
constructive branches count separately even when their images coincide.
Branch/domain provenance shall remain observable.

`GEOMETRIC_IMAGE_UNION_LENGTH` and geometric deduplication are excluded.

## 4. Query types

### 4.1 `BetweenPositionsMetricQuery`

This query shall carry two semantic position bindings, `FORWARD` or `REVERSE`,
a `ZERO_LENGTH` or `FULL_CYCLE` same-position policy, an open-boundary policy
and explicit metric/improper-limit policies.

Length shall remain non-negative. Direction selects the route. `SHORTEST` is
deferred.

### 4.2 `TotalLocusMetricQuery`

This query shall carry no `A`, no `B` and no semantic direction. It shall not be
represented by equal endpoints, `FULL_CYCLE` or `WRAP_TO_START`.

For revision `r`:

\[
\mathcal L_{\mathrm{total}}(L_r)=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Each valid component shall contribute exactly once. Disconnected components
may be aggregated without a connector. One periodic branch shall contribute
one fundamental cycle.

## 5. Semantic positions and bindings

The candidate durable position is:

```text
LocusSemanticPosition2D
    locusIdentity
    branchKey
    providerVersion
    providerCanonicalParameter
```

The candidate revision binding is:

```text
MetricPositionBinding2D
    semanticPosition
    semanticRevision
    resolvedValidComponentKey
    evaluationStatus
    evaluatedPoint
    diagnostics
```

Revision does not automatically become durable identity. Component keys are
revision-scoped. Coordinate-proximity repair is forbidden. A binding from a
different revision shall produce `POSITION_STALE`; rebinding shall be a
separate explicit operation.

## 6. Route resolution

The design shall keep these concepts separate:

```text
LocusMetricRouteResolver2D
LocusMetricRoute2D
LocusMetricRouteSegment2D
```

The resolver shall interpret positions, revision, branch, components,
direction, periodic seam, same-position policy, global boundaries, open
policies, gaps and reachability. It shall not integrate.

`LocusMetricRoute2D` shall preserve:

```text
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
```

Every segment shall belong to one valid component. No policy shall cross an
internal invalid-domain gap.

## 7. Same-position and open-boundary policies

`ZERO_LENGTH` and `FULL_CYCLE` shall be explicit. `FULL_CYCLE` is legal only
for approved periodic semantics and shall never be inferred from Cartesian
equality.

For an open branch where a forward target lies behind the start position:

- `STOP_AT_END` returns the finite length to the global end, incomplete
  coverage, `targetReached = false` and `STOPPED_AT_BOUNDARY`; it is not the
  complete `A`/`B` length;
- `WRAP_TO_START` returns the sum of the two boundary-side contributions,
  `wrapped = true`, `targetReached = true` and
  `geometricallyConnected = false`; it adds no chord or incidence; and
- `STRICT` returns `ABSENT` with `TARGET_NOT_REACHABLE`.

Encountering an internal gap returns `DISCONTINUITY_ENCOUNTERED` and incomplete
coverage; wrap shall not bypass it.

## 8. Zero cases

The working policy is:

| Case | Value | Coverage |
|---|---:|---|
| Empty domain | 0 | `COMPLETE` |
| Isolated point | 0 | `COMPLETE` |
| Collapsed image | 0 | `COMPLETE` |

Each case shall retain a typed diagnostic. Zero shall not be encoded as failure
or absence.

## 9. Rich immutable result

`LocusMetricResult2D` and every `LocusMetricContribution2D` shall contain one
closed immutable metric value:

```text
MetricValue2D
    FiniteMetricValue2D(non-negative finite value)
    PositiveInfinityMetricValue2D
    AbsentMetricValue2D
```

Only the finite variant may expose `OptionalDouble` finite access. A bare
`double getValue()` is forbidden: absence and positive infinity shall not be
encoded with NaN, magic values or exceptions. A derived `MetricValueKind` may
remain as an exhaustive discriminator, but it is not a second payload.

The result shall use defensive collections and coherent constructor invariants
and shall separate at least:

```text
MetricValueKind
    FINITE
    POSITIVE_INFINITY
    ABSENT

MetricCoverage
    COMPLETE
    INCOMPLETE

MetricComputationStatus
    SUCCESS
    INVALID_QUERY
    UNSUPPORTED
    NUMERICAL_FAILURE
    LIMIT_NOT_ESTABLISHED

MetricRectifiability
    RECTIFIABLE
    NON_RECTIFIABLE
    UNDETERMINED

TraversalOutcome
    TARGET_REACHED
    STOPPED_AT_BOUNDARY
    WRAPPED_TO_START
    TARGET_NOT_REACHABLE
    DISCONTINUITY_ENCOUNTERED
```

`TraversalOutcome` is structurally present only for a between-position result
whose contract requires traversal. A total result has no traversal outcome.
The API shall use `Optional<TraversalOutcome>` or an equivalent separation of
result types; it shall not use `null`, a sentinel or an artificial
`NOT_APPLICABLE` outcome.

It shall also carry construction fidelity, evaluator method,
metric/integration method, representation role, units, provenance, scalar
admissibility, diagnostics and contribution decomposition.

Numeric quality shall reuse the productive normative G6 type
`LocusSemanticMetadata2D.NumericGuarantee` directly. G7 shall not declare a
duplicate guarantee enum. Metric-specific evidence shall be held by an
immutable `MetricErrorEvidence2D` or equivalent. Error-amount availability is
a closed sum type, never an independently variable state plus optional number:

```text
sealed MetricErrorAmount2D
    EstablishedMetricErrorAmount2D(non-negative finite amount)
    NotEstablishedMetricErrorAmount2D
    NotApplicableMetricErrorAmount2D

G6 NumericGuarantee?       // absent only when evidence is not applicable
absoluteEvidence: MetricErrorAmount2D
relativeEvidence: MetricErrorAmount2D
scope                      // COMPLETE_VALUE / REPORTED_PARTIAL_VALUE / NOT_APPLICABLE
method
assumptions[]
certificateMetadata?
```

No error field shall use NaN, `-1` or zero to mean unknown. Exact arithmetic,
certified bounds, estimates, uncertified values, positive infinity and absent
values shall remain distinguishable. `DIVERGENT` shall not be used as a
generic numerical-failure state.

## 10. Metric integration and aggregation

G7A characterized, in order:

1. analytic/closed-form capability;
2. differential quadrature;
3. evaluator-only adaptive variation approximation; and
4. unsupported capability.

Evaluator-only refinement agreement shall not claim
`CERTIFIED_ERROR_BOUND`. `ESTIMATED_ERROR` requires explicit recorded
assumptions; otherwise the result is `FLOATING_POINT_UNCERTIFIED` or
`UNSUPPORTED`.

`LocusMetricAggregator2D` shall combine contributions, constructive
multiplicity, values, errors, weakest guarantees, coverage, status and
decomposition. It shall not integrate a component.

Certified absolute bounds add only across exact/certified finite
contributions. Estimated evidence propagates as estimated with its assumptions;
an uncertified finite contribution makes the aggregate uncertified. Positive
infinity and absent/unsupported values have not-applicable numeric error
evidence. If a known finite subtotal has incomplete coverage, its evidence
shall be scoped to `REPORTED_PARTIAL_VALUE`, never to the unknown complete
total.

For second review, differential quadrature uses a small per-call deterministic
GeoCeDG-owned integrator with explicit status, counters and estimated error.
The existing static adaptive-Gauss helper is an accuracy comparator, not the
metric result contract.

## 11. Publication in the kernel DAG

The G7B working architecture is:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

A `GeoNumeric` shall not be the sole output or semantic authority. The rich
result shall exist as its own graph entity.

G7A audited and recommends an append-only classification equivalent to:

```text
GeoClass.LOCUS_METRIC_RESULT
```

It shall not reuse `NUMERIC`, `LOCUS` or `LOCUS_V2`. G7B minimum shall register
no XML type, persistence or 3D behavior and no public command.

## 12. Defined state and scalar participation

Rich-result defined state and scalar-admissible state shall remain separate.
G7A compared:

- no generic numeric facade;
- a conditional read-only numeric facet; and
- an explicit numeric adapter.

The source audit found no safe instance-level gate for the static numeric
interfaces. The approved choice is an explicit derived numeric
adapter; the rich result implements no generic numeric facade and remains the
authority. No automatic companion `GeoNumeric` is created.

Candidate scalar-admissible results are finite, successful, semantically
satisfied, suitably covered, current-revision values with no unsupported
contribution and, by default, at least an estimated-error guarantee. Valid zero
and explicitly requested valid wrap are candidates.

Partial stop, unreachable target, incomplete coverage, stale position,
different branch, discontinuity, unsupported, numerical failure, unestablished
limit, absence, floating-point-uncertified value and positive infinity are not
admissible by default.

The G7A audit covered `GeoElement.isDefined()`, `NumberValue`/numeric
interfaces, Algebra View, expression evaluation, generic numeric algorithms,
lists, sequences and CAS. The resulting strategy C contract is author-approved.

## 13. Lifecycle

G7A covered and G7B shall cover creation, publication, defined/undefined behavior,
scalar admissibility, copy/copyInternal, set, remove, invalidation,
undefined/recovery, topology changes, branch disappearance, undo/redo, labels,
Algebra View, lists/sequences, selection, defaults/styles, numeric interfaces,
GeoClass switches, factory/XML impact, 2D/3D and packaging.

A copy or assignment shall never inherit a stale index, foreign construction
state, obsolete semantic revision, stale binding or partial unpublished result.
Unsupported lifecycle operations shall fail explicitly and be tested; they
shall not fall back to sampled or numeric substitutes.

A rich Geo is defined exactly when it has a current immutable published
snapshot, including a typed absent/failure diagnostic. Copy, assignment and
sequence materialization in the minimum clear current state and require normal
DAG recomputation unless a future safe portable-value contract is approved.

Atomic failure semantics use candidate policy P1. When revision `r+1`
supersedes a successful revision `r`, the old payload immediately becomes
non-current. A successful private build publishes one immutable entry and one
coherent rich snapshot for `r+1`. A handled failure publishes no index entry
and publishes a coherent `Absent` rich failure snapshot for `r+1`; its scalar
adapter is undefined. No old-value/new-status hybrid, stale success or partial
entry may be observed. “Failed build publishes nothing” applies to the index
entry, not to the current-revision rich failure diagnostic.

## 14. Revision-scoped index hypothesis

G7A compared:

```text
REFERENCE_NO_INDEX_REUSE
EAGER_WHOLE_REVISION
LAZY_COMPONENT_REVISION
```

The accepted index strategy is bounded `LAZY_COMPONENT_REVISION` indexing.
Same-component 1/10/100 traces built 1/1/1 lazy components versus
3/3/3 eager and 1/10/100 reference components; 100 repeated totals built 3/3
eager/lazy versus 300 reference. Its
complete key shall include at least:

```text
locusIdentity
semanticRevision
branchKey
resolvedValidComponentKey
provider/evaluator capability version
metricAlgorithmVersion
metricPolicyVersion
metricTolerancePolicy
multiplicityPolicy
improperLimitPolicy
metricWorkBudget
```

The index shall not be global and shall retain no unbounded revision history.
Component keys are revision-scoped, not durable position identity.

The accepted ownership architecture is a GeoCeDG-owned
`DEDICATED_SHARED_OWNER`. It is a non-GeoElement derived service tied to one active source
locus in one Construction and shared by compatible metric algorithms. The
algorithms remain ordinary direct DAG dependents of the locus. The service
owns no dependency edges or result publication, resolves no route, performs no
query aggregation and creates no callbacks between algorithms.

The reusable entry is immutable component-level metric state, conceptually:

```text
LocusMetricComponentState2D
    adaptive partition / cumulative arc-coordinate evidence
    capability and integration metadata
    component-level error state
```

The complete component key has no A/B endpoints and maps only to this state:

```text
complete component key
    -> get/build immutable LocusMetricComponentState2D

LocusMetricComponentState2D + LocusMetricRouteSegment2D
    -> route/component metric evaluation
        -> LocusMetricContribution2D
```

A total query derives a contribution over the complete valid-component extent;
a between-position query derives one contribution for each route segment. One
component state may produce many different contributions. The shared owner
never shares routes, query results, contributions or aggregate results.

For N compatible consumers and one complete key, the hard budget is one
component-state build until eviction or invalidation. The no-reuse path remains
the semantic oracle. Different loci or Constructions never share. Revision,
topology, undefined and removal transitions make old entries unreachable;
consumer and locus removal release ownership without a static/global registry.

The provisional capacity is 64 component entries per active locus owner,
current revision only, with deterministic insertion-order eviction. It is not
64 per metric algorithm and is not a normative constant. Real entry size and
capacity must be measured during G7B before this becomes stable policy.

Hard requirements from the first G7B implementation are:

- bounded state and deterministic eviction;
- no retained obsolete revisions;
- defensive immutable keys and values;
- complete key equality;
- kernel-thread confinement;
- no concurrency or background mutation;
- atomic result/index publication;
- exception-safe active bookkeeping and cleanup through `finally`;
- no partially valid published entry; and
- cache/index ON/OFF semantic equality.

## 15. Tolerance and error ownership

The initial versioned G7B policy is
`eps_metric_abs=1e-10` construction length unit and `eps_metric_rel=1e-9`.
The effective threshold is `max(eps_metric_abs, eps_metric_rel*S)` for a
translation-invariant world-coordinate scale `S`.

Depth shall never be the only work guard. The complete policy shall contain a
deterministic `MetricWorkBudget2D` or equivalent with independent maximum
evaluations, subdivisions and depth, and all three dimensions participate in
the complete index key. The approved initial defaults are `32768` evaluations,
`16384` subdivisions and depth `22`. They are implementation-policy defaults,
not mathematical constants. Exhausting any work dimension yields `Absent`
with `LIMIT_NOT_ESTABLISHED` and a typed cause; evaluator/numeric exceptions
remain `NUMERICAL_FAILURE`. No wall-clock timeout is metric authority.

Absolute contribution errors add deterministically and the weakest guarantee
propagates. These policies do not reuse the G6 evaluation envelope,
`eps_domain`, render pixel tolerance or future G8 root/residual tolerance.

## 16. Reparameterization, improper metrics and arc coordinate

G7A proved candidate length invariance for the regular
orientation-preserving map

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1}
\]

and retained `u^3` as a monotone endpoint-derivative-degenerate fixture.

It distinguished finite bounded subarcs on unbounded branches, finite
improper totals, positive infinity, non-rectifiability, unsupported capability
and unestablished limits. No viewport cutoff is permitted.

Arc coordinate `s(t) = Var(F;[t_0,t])` may exist only as per-valid-component
derived data. It shall not replace the semantic parameter or connect gaps or
branches, and it shall not become an automatic public surface.

## 17. Validation and performance

The validation authority is the
[G7 matrix](../../../docs/validation/g7_locus_v2_metric_validation_matrix.md)
and the [G7 benchmark plan](../../../docs/validation/g7_locus_v2_metric_benchmark_plan.md).
G7A execution evidence is the
[characterization report](../../../docs/validation/g7a_locus_v2_metric_characterization_report.md)
and the focused
[R1 refinement report](../../../docs/validation/g7a_r1_locus_v2_metric_refinement_report.md);
both remain evidence rather than runtime authority; their decisions are
incorporated into this normative contract by author approval.

Repeated traces shall use 1, 10 and 100 queries for same endpoints,
overlapping arcs, reverse, periodic, stop, wrap, strict, repeated total and
tolerance/policy changes. Functional counters precede wall-clock gates.

Nested metric composition shall cover:

```text
L1 -> metric(L1) -> L2 -> metric(L2) -> L3
```

with zero render access, zero legacy-sample access, no whole-locus regeneration
per downstream point, no metric-index build per point, same-revision/policy
reuse, normal-DAG invalidation and cache-off equality.

Independent expected values shall come from analytic references, high
precision or an independent numerical method. Any Python-generated constant
shall record formula, precision, runtime/library, script and output hash.
Python is validation evidence, not kernel authority.

## 18. Compatibility and public boundary

The G7B minimum candidate is:

```text
internal Java API
+ GeoLocusMetricResult
+ developer laboratory
```

It shall not add or change:

- `LocusLength`, `Length` or `Perimeter` commands;
- public `Path` or point-on-locus behavior;
- XML, persistence or migration;
- G8 intersection/incidence behavior;
- G9 spatial semantics;
- G5 locus export;
- 3D behavior;
- concurrency, C++, external numeric libraries or unmeasured DAG flattening.

Classic and public GeoCeDG `Locus[...]` shall remain legacy. Locus V2 remains
experimental/internal and disabled by default.

## 19. Author-approved phase disposition

The final G7A author review accepted all 42 recommendations, R1-1..R1-22 and
the three closeout API normalizations. That approval made the contract
normative and authorized the separately executed G7B implementation, which is
now also author-approved:

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED
G7B = PASS — AUTHOR APPROVED
G7 = PASS
G7B CAPACITY 64 = PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT
G8 = NOT STARTED
G9 = NOT STARTED
```

## 20. G7B conformance clarification

The author-approved G7B implementation realizes this normative contract with internal classes under
`org.geocedg.common.kernel.locus.metric`, one rich
`GeoLocusMetricResult`, one normal-DAG `AlgoLocusMetricV2` and an explicit
scalar adapter. This is implementation evidence, not a semantic amendment.

The component-state key excludes route endpoints. The bounded shared owner
publishes only immutable component-level state after successful private build;
route-specific contributions and aggregate results remain local to the query.
Traversal is structurally absent from total results. Error amount is expressed
only by the three closed variants, reusing the G6 `NumericGuarantee` directly.

No command, XML registration, persistence contract, public `Path`, 3D, G5,
G8 or G9 surface is added. The mathematical and public-boundary clauses above
remain unchanged.

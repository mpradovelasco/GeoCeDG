# Locus V2 metric contract

- Status: **PROPOSED — NOT NORMATIVE**
- Version: `0.1`
- Author-review disposition: **APPROVED FOR G7A CHARACTERIZATION**
- Roadmap gate: G7 `PENDING / NOT STARTED`
- Affected layer: shared Java kernel semantics and internal developer laboratory
- Working architecture: `GeoLocusMetricResult` as a normal kernel-DAG result
- Proposed decision: ADR 0007, not accepted
- Date: 2026-08-12

This proposal records the author-reviewed G7 planning requirements. It does not
override the normative G6 Locus V2 semantic contract, authorize productive
metric code or make a public API. G7A must characterize every unresolved
source, numerical and lifecycle decision before this contract can become
normative.

## 1. Scope

The proposed G7 minimum adds native semantic length services for the existing
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

The proposed minimum metric is
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

`LocusMetricResult2D` shall be an immutable semantic value with defensive
collections and coherent constructor invariants. It shall separate at least:

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

It shall also carry construction fidelity, evaluator method,
metric/integration method, representation role, numeric guarantee, absolute
and relative error information, units, provenance, scalar admissibility,
diagnostics and contribution decomposition. `DIVERGENT` shall not be used as a
generic numerical-failure state.

## 10. Metric integration and aggregation

G7A shall characterize, in order:

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

G7A shall audit an append-only candidate classification equivalent to:

```text
GeoClass.LOCUS_METRIC_RESULT
```

It shall not reuse `NUMERIC`, `LOCUS` or `LOCUS_V2`. G7B minimum shall register
no XML type, persistence or 3D behavior and no public command.

## 12. Defined state and scalar participation

Rich-result defined state and scalar-admissible state shall remain separate.
G7A shall compare:

- no generic numeric facade;
- a conditional read-only numeric facet; and
- an explicit numeric adapter.

The working preference is a conditional read-only facet only if the real
GeoGebra API prevents scalar-inadmissible rich states from being treated as
ordinary numbers. A companion `GeoNumeric` shall not become the authority.

Candidate scalar-admissible results are finite, successful, semantically
satisfied, suitably covered, current-revision values with no unsupported
contribution. Valid zero and explicitly requested valid wrap are candidates.

Partial stop, unreachable target, incomplete coverage, stale position,
different branch, discontinuity, unsupported, numerical failure, unestablished
limit, absence and positive infinity are not admissible by default.

G7A shall audit `GeoElement.isDefined()`, `NumberValue`/numeric interfaces,
Algebra View, expression evaluation, generic numeric algorithms, lists,
sequences and CAS before selecting a facade.

## 13. Lifecycle

G7A and G7B shall cover creation, publication, defined/undefined behavior,
scalar admissibility, copy/copyInternal, set, remove, invalidation,
undefined/recovery, topology changes, branch disappearance, undo/redo, labels,
Algebra View, lists/sequences, selection, defaults/styles, numeric interfaces,
GeoClass switches, factory/XML impact, 2D/3D and packaging.

A copy or assignment shall never inherit a stale index, foreign construction
state, obsolete semantic revision, stale binding or partial unpublished result.
Unsupported lifecycle operations shall fail explicitly and be tested; they
shall not fall back to sampled or numeric substitutes.

## 14. Revision-scoped index hypothesis

G7A shall compare:

```text
REFERENCE_NO_INDEX_REUSE
EAGER_WHOLE_REVISION
LAZY_COMPONENT_REVISION
```

The working hypothesis is a bounded lazy component-scoped revision index. Its
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
```

The index shall not be global and shall retain no unbounded revision history.
Component keys are revision-scoped, not durable position identity.

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

G7A shall measure and propose distinct `eps_metric_abs` and `eps_metric_rel`, a
stopping policy, refinement limits, improper-limit policy and aggregate-error
policy. It shall not reuse the G6 evaluation envelope, `eps_domain`, render
pixel tolerance or future G8 root/residual tolerance.

## 16. Reparameterization, improper metrics and arc coordinate

G7A shall prove length invariance for a regular orientation-preserving map such
as

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1}
\]

and retain `u^3` as a monotone endpoint-derivative-degenerate fixture.

It shall distinguish finite bounded subarcs on unbounded branches, finite
improper totals, positive infinity, non-rectifiability, unsupported capability
and unestablished limits. No viewport cutoff is permitted.

Arc coordinate `s(t) = Var(F;[t_0,t])` may exist only as per-valid-component
derived data. It shall not replace the semantic parameter or connect gaps or
branches, and it shall not become an automatic public surface.

## 17. Validation and performance

The proposed validation authority is the
[G7 matrix](../../../docs/validation/g7_locus_v2_metric_validation_matrix.md)
and the [G7 benchmark plan](../../../docs/validation/g7_locus_v2_metric_benchmark_plan.md).

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

## 19. Normative gate

This proposal may become normative only after G7A supplies reproducible source
audits, numerical experiments, index comparisons, lifecycle evidence,
independent references and explicit author acceptance. Until then:

```text
ADR 0007 = PROPOSED
G7 SPEC = PROPOSED / NOT NORMATIVE
G7A = NOT STARTED
G7B = NOT STARTED
```

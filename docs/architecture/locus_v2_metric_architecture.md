# Locus V2 metric architecture

| Field | Value |
|---|---|
| Status | Author-reviewed G7 working architecture; no implementation |
| Product maturity | Future experimental/internal G7B candidate |
| Semantic model | [`locus_v2_metric_semantic_model.md`](locus_v2_metric_semantic_model.md) |
| Proposed contract | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Index decision | [ADR 0007 Proposed](../adr/0007-revision-scoped-locus-v2-metric-index.md) |
| Date | 2026-08-12 |

This document maps the approved G7 planning semantics to a candidate shared
kernel architecture. Names and responsibilities are working G7A hypotheses,
not productive APIs. G7A, G7B and G8 remain `NOT STARTED`.

## 1. Baseline and placement

The G6/G6R baseline already contains:

- `GeoLocusV2` in `org.geocedg.common.kernel.geos`;
- `AlgoLocusV2` in `org.geocedg.common.kernel.algos`;
- immutable definition/domain/evaluation contracts under
  `org.geocedg.common.kernel.locus`;
- revision-aware bounded `LocusEvaluationSession2D`;
- view-owned `LocusRenderCache2D` under the Euclidian draw package.

It deliberately contains no productive metric entity, index, command or
persistence. The G7 metric changes semantic meaning, must update in the normal
construction DAG and can be consumed by downstream constructions. Therefore
its truth belongs in the shared Java kernel, with only a developer-laboratory
adapter in the Desktop/application layer.

GeoCeDG-owned packages are preferred. Upstream-owned edits are allowed only at
the audited extension points required for a new `GeoElement` classification or
safe read-only scalar participation.

## 2. Dependency direction

The candidate layers are:

```text
G6 locus identity + revision + branches + valid components + evaluator
                              |
                              v
query validation + explicit revision binding
                              |
                              v
route resolution (between-position only)
                              |
                              v
component integration capability / metric index
                              |
                              v
contribution aggregation
                              |
                              v
immutable LocusMetricResult2D
                              |
                              v
AlgoLocusMetricV2 -> GeoLocusMetricResult -> normal kernel DAG consumers
```

Total queries skip route resolution and enumerate each valid-domain component
exactly once. Rendering is a separate downstream consumer and has no arrow
into any metric layer.

## 3. Candidate type groups

### 3.1 Query and position values

`BetweenPositionsMetricQuery` contains:

- locus identity/reference;
- bindings for A and B;
- `FORWARD` or `REVERSE`;
- `ZERO_LENGTH` or `FULL_CYCLE` for the same semantic position;
- `STOP_AT_END`, `WRAP_TO_START` or `STRICT`;
- explicit metric tolerance/policy identity.

`TotalLocusMetricQuery` contains:

- locus identity/reference and target semantic revision;
- metric tolerance, multiplicity and improper-limit policies.

It contains no A, B, direction, same-position or wrap fields.

`LocusSemanticPosition2D` is the durable address:

```text
locusIdentity
branchKey
providerVersion
providerCanonicalParameter
```

`MetricPositionBinding2D` is a revision evaluation:

```text
semanticPosition
semanticRevision
resolvedValidComponentKey
evaluationStatus
evaluatedPoint
diagnostics
```

Both values are defensive and immutable. Bindings cannot silently follow a
component split, branch replacement or nearby coordinate. A new binding is an
explicit operation.

### 3.2 Route values

`LocusMetricRouteResolver2D` performs semantic route selection only. Its input
is one validated between-position query plus a stable G6 definition snapshot.
It interprets:

- locus/branch/revision agreement and binding currency;
- component membership and oriented parameter order;
- `FORWARD`/`REVERSE`;
- periodic seam and one fundamental cycle;
- `ZERO_LENGTH`/`FULL_CYCLE`;
- global branch boundaries;
- internal invalid-domain gaps;
- STOP/WRAP/STRICT and target reachability.

It does not evaluate quadrature, query an index or aggregate error.

`LocusMetricRoute2D` contains:

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

Each immutable `LocusMetricRouteSegment2D` references exactly one resolved
valid component and an oriented parameter subinterval within it. A wrap has
two segments separated by a declared metric seam; it is never represented by a
synthetic connecting segment.

### 3.3 Integration and contribution values

A component metric service selects one of four capabilities:

1. analytic/closed form;
2. quadrature of an explicitly supported derivative/speed;
3. evaluator-only adaptive metric;
4. unsupported.

The working service boundary accepts one route segment or whole valid
component and returns an immutable `LocusMetricContribution2D` containing:

- locus/revision/branch/component provenance;
- parameter interval and orientation metadata;
- non-negative value kind/value;
- rectifiability and coverage;
- construction fidelity and evaluator method;
- integration method and representation role;
- numeric guarantee and absolute/relative error metadata;
- evaluator/derivative/integrator/subdivision counters;
- diagnostics.

Orientation selects a route; it does not make contribution length negative.

Evaluator-only refinement cannot claim `CERTIFIED_ERROR_BOUND` from agreement
of successive chord sums. G7A must establish explicit assumptions before
`ESTIMATED_ERROR` is permitted; otherwise the contribution is
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

### 3.4 Metric index

The working `LocusMetricIndex2D` is a bounded, kernel-thread-confined service
for immutable component-scoped entries. It is not a global cache and is not the
G6 render cache or evaluator session.

The complete candidate key is:

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

G7A must compare no reuse, eager whole-revision and lazy component-revision
strategies. Concrete owner, capacity, entry representation and component-key
derivation remain characterization decisions. Any chosen design has
deterministic eviction, no obsolete-revision retention, atomic publication,
`finally` cleanup and index ON/OFF equality.

### 3.5 Aggregator

`LocusMetricAggregator2D`:

- combines route or total-component contributions;
- preserves constructive multiplicity;
- adds finite values without connecting components;
- aggregates absolute/relative error under the approved policy;
- propagates the weakest numeric guarantee;
- computes coverage, computation status and rectifiability;
- retains ordered contribution decomposition and gap/branch diagnostics.

It does not resolve routes and does not integrate components.

For total length, aggregation iterates stable branch order and stable
revision-component order. Every valid component is submitted exactly once. A
periodic domain is normalized to one fundamental cycle before integration.

### 3.6 Rich semantic result

`LocusMetricResult2D` is the immutable authority. At minimum it records:

```text
query kind and semantic provenance
MetricValueKind
value when FINITE
MetricCoverage
MetricComputationStatus
MetricRectifiability
TraversalOutcome when applicable
construction fidelity
evaluator method
metric/integration method
representation role
numeric guarantee
absolute and relative error
units
contribution decomposition
diagnostics
```

No mutable array, list, diagnostic or index entry may be exposed by alias.
Value zero is valid for an empty domain, isolated point, collapsed image and
`ZERO_LENGTH` query; diagnostics distinguish them from one another.

### 3.7 DAG publication

`GeoLocusMetricResult` is a distinct `GeoElement` whose current payload is one
coherent `LocusMetricResult2D`. It is not a `GeoNumeric` wrapper and never
publishes a scalar independently of the rich payload.

`AlgoLocusMetricV2` registers every semantic input using ordinary
`setInputOutput()` conventions. On update it:

1. captures one coherent locus revision and query/policy snapshot;
2. validates/rebinds only as explicitly requested;
3. resolves a route or total component list;
4. obtains contributions with the selected integration/index path;
5. aggregates a complete immutable candidate payload;
6. atomically replaces the Geo payload;
7. releases scoped work in `finally`.

An exception, topology transition or numerical failure cannot expose a mixture
of old value and new status. Failure produces a coherent rich failure result;
it cannot leave a partially built index entry or keep a previous successful
payload as current.

## 4. Between-position execution

The required decision order is:

1. verify both positions address the queried locus and current branch;
2. reject malformed, foreign, stale or unsupported bindings with explicit
   diagnostics;
3. distinguish semantic-position equality from Cartesian coincidence;
4. apply `ZERO_LENGTH`/`FULL_CYCLE`;
5. resolve parameter order in the selected direction;
6. inspect valid-component boundaries before global branch boundaries;
7. apply STOP/WRAP/STRICT only when target reachability fails at a global open
   boundary;
8. never apply wrap across an internal invalid-domain gap;
9. integrate each resolved segment independently;
10. aggregate without signed length.

The canonical open-branch outcomes are:

| Case | Value kind | Coverage | Computation | Traversal | Scalar default |
|---|---|---|---|---|---|
| reached | finite | complete | success | `TARGET_REACHED` | candidate admissible |
| valid zero | finite zero | complete | success | `TARGET_REACHED` | candidate admissible |
| full periodic cycle | finite/other established kind | complete | success | `TARGET_REACHED` | policy-dependent |
| STOP | finite partial | incomplete | success | `STOPPED_AT_BOUNDARY` | inadmissible |
| explicit WRAP | finite/established | complete | success | `WRAPPED_TO_START` | candidate admissible |
| STRICT unreachable | absent | incomplete | success | `TARGET_NOT_REACHABLE` | inadmissible |
| internal gap | finite partial or absent, as approved | incomplete | success/failure axis as cause requires | `DISCONTINUITY_ENCOUNTERED` | inadmissible |
| stale binding | absent | incomplete | invalid query | no route outcome or explicit diagnostic | inadmissible |

G7A must finalize ambiguous cells without collapsing these axes. In particular,
`DIVERGENT` is not available as a generic status.

## 5. Total execution

A total query:

1. captures one semantic revision;
2. enumerates every constructive branch;
3. enumerates every valid-domain component exactly once;
4. reduces each periodic branch to one approved fundamental cycle;
5. obtains one contribution per component;
6. aggregates them without route fabrication.

For:

```text
component 1
--- invalid gap ---
component 2
```

the numeric total is `L_1 + L_2` when both contributions are established.
There is no gap chord. Decomposition identifies both components and the invalid
gap.

G7A must settle mixed outcomes. The working algebra is:

- finite complete contributions add normally;
- positive infinity is distinct from numerical failure;
- non-rectifiable dominates rectifiability, while unresolved support yields
  `UNDETERMINED`;
- unresolved or unsupported contributions make coverage `INCOMPLETE` even
  when a defensible partial finite sum is retained;
- `ABSENT` is used when no defensible metric value can be published, not as a
  synonym for zero.

## 6. Scalar participation architecture

Rich defined state answers whether `GeoLocusMetricResult` contains a coherent
current semantic result. Scalar admissibility answers whether generic numeric
consumers may read a scalar. They are orthogonal.

G7A compares:

| Option | Shape | Main risk |
|---|---|---|
| A | no generic numeric facade | downstream use requires explicit rich APIs |
| B | conditional read-only numeric facet | Java `instanceof NumberValue` may expose inadmissible states too broadly |
| C | explicit numeric adapter | extra user-visible/algorithmic step and lifecycle |

Working preference B is conditional. Current shared-kernel code commonly uses
`instanceof NumberValue` and `evaluateDouble()` as static capability tests, so
G7A must demonstrate that an instance-level scalar gate is honored by Algebra
View, generic algorithms and CAS. If it cannot, B is rejected in favor of A or
C.

Candidate scalar gate:

```text
FINITE
and SUCCESS
and semantic query satisfied
and suitable coverage
and current bindings
and no unsupported contribution
and (normal reached route or explicit valid wrap or valid zero)
```

Positive infinity is inadmissible unless separately approved.
`isDefined()` must not be overloaded to hide a coherent rich STOP, unsupported
or failure result merely because it is not scalar-admissible.

## 7. GeoLocusMetricResult lifecycle

G7A must determine and G7B must test each transition:

| Event | Required invariant |
|---|---|
| creation | one coherent initial payload; no stale/default scalar |
| publication | atomic rich payload; normal AlgoElement dependency |
| input undefined | current failure/absence payload; old success not current |
| recovery | recompute from current revision; no old binding/index reuse |
| revision update | old revision invalidated before new publication |
| component split/merge | semantic position retained where valid; binding re-resolved explicitly |
| branch disappearance | `POSITION_STALE`/diagnostic; no proximity repair |
| topology change | full route/total re-evaluation and deterministic invalidation |
| remove | release ownership/references; no retained Construction |
| undo/redo | recreate state through normal DAG; no foreign cached payload |
| copy/copyInternal | no stale index, old revision/binding or partial current state |
| set | either safe semantic copy contract or explicitly unsupported behavior |
| label/selection | normal GeoElement conventions without implying public command |
| list/sequence | no silent scalar coercion of inadmissible payload |
| defaults/style | deliberate non-geometric/default behavior |

No lifecycle item is deferred to a hypothetical G7R.

## 8. Index transaction model

The minimum publication sequence is:

```text
lookup immutable complete key
    |
 hit -> verify current revision/policy -> consume immutable entry
    |
 miss -> build private candidate
              |
              +-- exception/failure -> finally cleanup; publish nothing
              |
              +-- complete -> atomically publish -> deterministic eviction
```

Revision invalidation removes or makes unreachable all obsolete entries within
the same kernel update boundary. No background cleanup is allowed. Capacity
and eviction order are deterministic test inputs.

An arc coordinate, if stored, is derived per valid component:

\[
s(t)=\operatorname{Var}(F;[t_0,t]).
\]

It neither replaces `providerCanonicalParameter` nor crosses a gap/branch.

## 9. Numerical capability and tolerances

Metric policy is independent of G6 evaluation/domain/render tolerances. The
candidate policy value includes:

```text
eps_metric_abs
eps_metric_rel
stopping policy
refinement/subdivision limits
improper-limit policy
aggregate error policy
algorithm/policy versions
```

Analytic values still record floating-point evaluation guarantees when
evaluated as `double`. Differential quadrature records derivative provenance
and its own error estimate/certificate. Evaluator-only adaptation records the
assumptions supporting any estimate. Unsupported capability is explicit.

Unbounded domains require parameter-limit reasoning. A finite A/B query can be
supported even when total length is improper or infinite. The result model
distinguishes:

- finite proper or improper value;
- `POSITIVE_INFINITY`;
- `NON_RECTIFIABLE`;
- `UNSUPPORTED`;
- `LIMIT_NOT_ESTABLISHED`.

No viewport cutoff can stand in for an improper limit.

## 10. Repeated and nested composition

Instrumentation is functional first:

- evaluator, derivative and integrator calls;
- subdivisions and component work;
- index builds, hits, misses, evictions and retained entries;
- route resolutions and aggregate operations;
- invalidations and revision changes;
- render and legacy-sample accesses, which must remain zero;
- query latency as informational context.

For repeated compatible queries, the chosen strategy must not rebuild
component state unnecessarily. A tolerance or policy change must miss the old
key rather than reuse incompatible state.

For:

```text
L1 -> metric(L1) -> L2 -> metric(L2) -> L3
```

normal DAG invalidation remains authoritative. One downstream point must not
regenerate a whole upstream locus or build a metric index. Shared
revision/policy work is reusable only through the accepted bounded owner.
Cache-off and cache-on rich outputs must be equal.

## 11. Upstream impact audit

G7A must inspect actual source, not infer from class names:

| Surface | Baseline observation / question | G7B constraint |
|---|---|---|
| `GeoClass` | `LOCUS_V2` is currently appended | audit append-only `LOCUS_METRIC_RESULT` and exhaustive switch/tests |
| `GeoElement` | defined state, copy/set/remove and `evaluateDouble()` conventions are broad | rich and scalar states remain separate |
| `NumberValue` / numeric interfaces | static interface checks occur widely | B only if every relevant consumer honors conditional admissibility |
| Algebra View | formatting/evaluation can assume known value categories | display rich status without false scalar or public surface |
| `AlgoElement` | dependency registration and compute drive the DAG | no hidden graph, background mutation or manual propagation |
| `ConstructionDefaults` | unknown classes can fall through generic defaults | explicit audited behavior; no accidental locus/numeric styling |
| command dispatch | `Length`/`Perimeter` and legacy processors already exist | no command registration or behavior change |
| `AlgoPerimeterLocus` | sums legacy stored sample chords | preserved unchanged and used only as legacy evidence |
| `Path` | legacy locus has path/sample behavior; V2 deliberately does not | metric result and V2 remain non-Path |
| `GeoFactory` / XML | no V2 metric type exists | no G7B XML/factory registration |
| 2D/3D dispatch | new GeoClass can reach exhaustive switches | 2D rich result only; no 3D behavior |
| G5 export | V2 locus is unsupported | metric result is not exported in G7B |
| packaging | common source is shipped in Desktop artifacts | no extra runtime/library; laboratory remains opt-in |
| enum/switch tests | drawable/default/factory tests may enumerate classes | update only required tests after G7A approval |
| CAS/generic algorithms | numeric interfaces may trigger broad coercion | no semantic loss or silent partial/infinite scalar |

The exact editable source set must be listed in the G7A report before G7B.

## 12. Developer laboratory boundary

The future laboratory may create internal metric queries and display every rich
axis, decomposition and counter. It must:

- remain explicitly opt-in and disabled by default;
- use the same kernel API as tests;
- not register a public command, XML type or preference-backed user contract;
- label STOP and WRAP semantics visibly;
- show index strategy/capacity/counters without making them semantic inputs;
- never turn laboratory formatting into result authority.

## 13. Forbidden dependencies

Architecture tests must fail any metric code that imports or reads:

- `org.geocedg.common.euclidian.draw.LocusRenderCache2D`;
- drawable/render vertex collections;
- legacy `GeoLocus`/`GeoLocusND.myPointList` samples;
- Euclidian view, viewport, zoom, DPI or pixel tolerance;
- static/global mutable metric caches;
- background executors or concurrency primitives for index mutation.

Legacy evidence adapters may observe legacy algorithms only in
characterization tests; they cannot feed productive V2 results.

## 14. G7A decisions still open

The following are intentionally unresolved and block G7B:

- whether numeric participation is A, B or C;
- precise rich `isDefined()` and Algebra View presentation;
- copy/set/sequence semantics;
- exact appended `GeoClass` integration impact;
- index owner, capacity, eviction and component-key derivation;
- selected integration methods and capability negotiation;
- `eps_metric_abs`, `eps_metric_rel`, stopping/refinement and aggregate error;
- improper-limit and positive-infinity scalar policies;
- mixed total aggregation rules;
- exact G7B source/test/package edit set and functional budgets.

G7A may characterize `SHORTEST` but it remains deferred.

## 15. G7B minimum acceptance architecture

After all open decisions are author-approved, the minimum includes:

- distinct between-position and total query values;
- position/binding separation and explicit stale/rebind behavior;
- route resolver with FORWARD, REVERSE, ZERO/FULL_CYCLE and
  STOP/WRAP/STRICT;
- component integration capability hierarchy;
- accepted bounded index strategy with cache-off oracle;
- `LocusMetricAggregator2D`;
- immutable `LocusMetricResult2D`;
- `GeoLocusMetricResult` and normal-DAG `AlgoLocusMetricV2`;
- lifecycle, exception, invalidation, repeated and nested gates;
- internal API documentation and developer laboratory.

It still excludes commands, XML, public `Path`, 3D, G5 changes, G8 and G9.
No implementation begins while the spec is proposed or ADR 0007 is not
Accepted/replaced.

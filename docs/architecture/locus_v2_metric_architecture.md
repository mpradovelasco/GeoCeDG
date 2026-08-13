# Locus V2 metric architecture

| Field | Value |
|---|---|
| Status | Author-approved G7A/G7A-R1 architecture; G7B implementation author-approved |
| Product maturity | Experimental/internal; G7B `PASS — AUTHOR APPROVED` |
| Semantic model | [`locus_v2_metric_semantic_model.md`](locus_v2_metric_semantic_model.md) |
| Normative contract | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Index decision | [ADR 0007 Accepted](../adr/0007-revision-scoped-locus-v2-metric-index.md) |
| Date | 2026-08-13 |

This document maps the approved G7 planning semantics and measured G7A findings
to the author-approved G7B shared-kernel architecture. The names below now map
to productive internal classes. G7A-R1, G7A and G7B are
`PASS — AUTHOR APPROVED`; G7 is `PASS`, and G8 remains `NOT STARTED`.

## 1. Baseline and placement

The G6/G6R baseline already contains:

- `GeoLocusV2` in `org.geocedg.common.kernel.geos`;
- `AlgoLocusV2` in `org.geocedg.common.kernel.algos`;
- immutable definition/domain/evaluation contracts under
  `org.geocedg.common.kernel.locus`;
- revision-aware bounded `LocusEvaluationSession2D`;
- view-owned `LocusRenderCache2D` under the Euclidian draw package.

Before G7B it deliberately contained no productive metric entity, index,
command or persistence. The G7 metric changes semantic meaning, must update in the normal
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

The shared integration boundary builds or obtains one immutable
`LocusMetricComponentState2D` for a complete compatible component key. That
state contains adaptive partition/cumulative arc-coordinate evidence,
capability/integration metadata and component-level error state. It contains no
A/B endpoint and is not a contribution.

Route/component evaluation then combines one state with one
`LocusMetricRouteSegment2D` (or the complete component extent for a total
query) and returns an immutable `LocusMetricContribution2D` containing:

- locus/revision/branch/component provenance;
- parameter interval and orientation metadata;
- one closed `MetricValue2D` (`Finite`, `PositiveInfinity` or `Absent`), with
  no sentinel double;
- rectifiability and coverage;
- construction fidelity and evaluator method;
- integration method and representation role;
- a `MetricErrorEvidence2D` wrapper around the normative G6 guarantee, typed
  absolute/relative availability, scope, assumptions and certificate metadata;
- evaluator/derivative/integrator/subdivision counters;
- diagnostics.

Orientation selects a route; it does not make contribution length negative.
One component state may produce many different route-specific contributions.

Evaluator-only refinement cannot claim `CERTIFIED_ERROR_BOUND` from agreement
of successive chord sums. G7A demonstrated aliasing despite exact two-level
agreement: explicit assumptions are required before `ESTIMATED_ERROR` is
permitted; otherwise the contribution is
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

### 3.4 Metric index

The accepted `LocusMetricIndex2D` strategy is a bounded,
kernel-thread-confined service
for immutable component-scoped entries. It is not a global cache and is not the
G6 render cache or evaluator session.

The complete R1 candidate key is:

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
maximumMetricEvaluations
maximumMetricSubdivisions
maximumAdaptiveDepth
```

G7A compared no reuse, eager whole-revision and lazy component-revision
strategies on identical traces. R1 then measured 1/3/10/100 independent metric
results. A per-Algorithm lazy index performed N compatible builds; a dedicated
shared owner performed one build and N-1 cross-result hits. The accepted
architecture therefore uses current-revision lazy entries in one
`LocusMetricSharedOwner2D` per active source locus, separate from semantic
definition and normal DAG ownership.

Capacity 64 remains an initial provisional, non-normative implementation
default, but it is per locus owner rather than per metric result. The
productive `retainedBytes = 336` for the analytic fixture is a deterministic
logical retained-state estimate from metric-state instrumentation, not a JVM
heap or object-layout measurement, not a universal component-state entry size,
and not evidence that stabilizes capacity 64. The last consumer and locus
removal release entries; revision,
topology and undefined transitions synchronously invalidate them. The owner is
not a GeoElement, route resolver, aggregator, result publisher or callback
graph. It shares only immutable component metric state—never routes, query
results, contributions or aggregate results. The accepted design retains
deterministic insertion-order eviction, atomic entry publication, `finally`
cleanup and index ON/OFF equality.

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
closed MetricValue2D
MetricCoverage
MetricComputationStatus
MetricRectifiability
Optional<TraversalOutcome> for between-position results only
construction fidelity
evaluator method
metric/integration method
representation role
MetricErrorEvidence2D containing the normative G6 guarantee when applicable
typed absolute/relative evidence and complete/partial/not-applicable scope
units
contribution decomposition
diagnostics
```

Total results contain no traversal outcome. Absence is structural; no `null`,
sentinel or `NOT_APPLICABLE` outcome is allowed. Error amounts are likewise a
closed `MetricErrorAmount2D` hierarchy with established non-negative finite,
not-established and not-applicable variants; a separate state plus
`OptionalDouble` representation is forbidden.

No mutable array, list, diagnostic or index entry may be exposed by alias.
No absent/infinite value or unavailable error uses NaN, `-1`, zero or another
numeric sentinel. Value zero is valid for an empty domain, isolated point,
collapsed image and `ZERO_LENGTH` query; diagnostics distinguish them.

### 3.7 DAG publication

`GeoLocusMetricResult` is a distinct `GeoElement` whose current payload is one
coherent `LocusMetricResult2D`. It is not a `GeoNumeric` wrapper and never
publishes a scalar independently of the rich payload.

`AlgoLocusMetricV2` registers every semantic input using ordinary
`setInputOutput()` conventions. On update it:

1. captures one coherent locus revision and query/policy snapshot;
2. validates/rebinds only as explicitly requested;
3. resolves a route or total component list;
4. obtains immutable component states and derives route/extent contributions;
5. aggregates a complete immutable candidate payload;
6. atomically replaces the Geo payload;
7. releases scoped work in `finally`.

An exception, topology transition or numerical failure cannot expose a mixture
of old value and new status. Failure produces a coherent rich failure result;
it cannot leave a partially built index entry or keep a previous successful
payload as current.

R1 names this P1 publication. At the start of revision `r+1`, the `r` payload
is made non-current. A handled failure publishes one coherent `Absent` failure
snapshot for `r+1`; the scalar adapter is undefined. “Publish nothing on
failure” applies only to the private candidate index entry, never to retention
of the old rich success as current.

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

G7A finalized these author-approved cells without collapsing the axes. In
particular, `DIVERGENT` is not available as a generic status.

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

G7A characterized mixed outcomes. The accepted algebra is:

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

G7A compared:

| Option | Shape | Main risk |
|---|---|---|
| A | no generic numeric facade | downstream use requires explicit rich APIs |
| B | conditional read-only numeric facet | Java `instanceof NumberValue` may expose inadmissible states too broadly |
| C | explicit numeric adapter | extra user-visible/algorithmic step and lifecycle |

The audit found 219 `instanceof NumberValue` occurrences in 72 shared-source
files and no per-instance admissibility hook. B is therefore rejected. G7A
accepts C: the rich Geo implements no numeric facade and an explicit derived
adapter publishes a scalar only when admissible. The adapter never owns the
metric value or index.

Candidate scalar gate:

```text
FINITE
and SUCCESS
and semantic query satisfied
and suitable coverage
and current bindings
and no unsupported contribution
and at least ESTIMATED_ERROR by default
and (normal reached route or explicit valid wrap or valid zero)
```

Positive infinity is inadmissible unless separately approved.
`isDefined()` reports whether a coherent immutable rich snapshot is current,
including a STOP, unsupported or failure diagnostic. It never implies scalar
admissibility.

## 7. GeoLocusMetricResult lifecycle

G7A characterized and G7B must test each transition:

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
              +-- exception/failure -> finally cleanup; publish no index entry
              |
              +-- complete -> atomically publish -> deterministic eviction
```

The result Geo independently follows P1 and publishes a coherent current-
revision failure snapshot when component construction fails.

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
initial versioned policy value includes:

```text
eps_metric_abs
eps_metric_rel
stopping policy
maximumMetricEvaluations
maximumMetricSubdivisions
maximumAdaptiveDepth
improper-limit policy
aggregate error policy
algorithm/policy versions
```

The three work ceilings are independent, deterministic and included in policy
identity and the complete index key. With the initial tolerances `1e-10` and
`1e-9`, R1 observed at most 2493 evaluations and 1245 subdivisions in the
quadrature fixtures and 4162 evaluator calls in the difficult evaluator-only
trace. The approved initial ceilings are 32768/16384/depth 22. These are
implementation-policy defaults, not mathematical constants. Any exhaustion is
`LIMIT_NOT_ESTABLISHED`; wall-clock is not a metric guard.

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

Across independent metric result Algos, the hard budget is one component-state
build per complete key until eviction or invalidation. R1 measured
100 local owners as 100 builds/99 duplicates, while the dedicated shared owner
produced one build/99 cross-result hits. Both total-first and local-first
orders built exactly the three unique components in the mixed fixture.

For:

```text
L1 -> metric(L1) -> L2 -> metric(L2) -> L3
```

normal DAG invalidation remains authoritative. One downstream point must not
regenerate a whole upstream locus or build a metric index. Shared
revision/policy work is reusable only through the accepted bounded owner.
Cache-off and cache-on rich outputs must be equal.

The dedicated owner is acquired through the source-locus lifecycle and stores
no dependency edges. Every metric Algo still names the source locus as an
ordinary input. Same-Construction different loci and different Constructions
never share entries. The nested multi-consumer fixture produced zero duplicate
compatible builds, render/sample reads, whole-locus regeneration and per-point
index builds.

## 11. Upstream impact audit

G7A inspected actual source rather than inferring from class names:

| Surface | Baseline observation / question | G7B constraint |
|---|---|---|
| `GeoClass` | `LOCUS_V2` is currently appended | audit append-only `LOCUS_METRIC_RESULT` and exhaustive switch/tests |
| `GeoElement` | defined state, copy/set/remove and `evaluateDouble()` conventions are broad | rich and scalar states remain separate |
| `NumberValue` / numeric interfaces | 219 static `instanceof` checks occur across 72 shared-source files | rich Geo implements no numeric interface; optional explicit adapter gates admissibility |
| Algebra View | formatting/evaluation can assume known value categories | display rich status without false scalar or public surface |
| `AlgoElement` | dependency registration and compute drive the DAG | no hidden graph, background mutation or manual propagation |
| `GeoLocusV2` lifecycle | revision publication, undefined and remove are coherent source-locus boundaries | narrow acquire/invalidate/release hook for a separate derived owner; copy never carries it |
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

The exact optimized editable source set is listed in R1 report §9. It adds new
GeoCeDG-owned metric values/services and only narrow `GeoLocusV2`, metric Algo,
GeoClass/test and laboratory edits; it does not require a Construction
repository, base `GeoElement`, public command, XML, Path, 3D, G5 or G8 change.

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

## 14. Author-approved G7A/R1 decisions

The author accepts all 42 G7A recommendations, R1-1..R1-22 and the three final
API normalizations. The resulting decisions are:

- numeric strategy C: the rich Geo has no numeric facade and an explicit
  derived adapter exposes only admissible scalars; static strategy B is unsafe;
- rich `isDefined()` means a current immutable diagnostic snapshot exists;
  scalar admissibility remains independent;
- copy/set/sequence operations clear current revision-bound state and require
  recomputation in the G7B minimum;
- append-only `GeoClass.LOCUS_METRIC_RESULT`, non-drawable and nonpersistent;
- bounded lazy component/revision indexing, current revision only,
  one dedicated shared owner per active locus, deterministic insertion-order
  eviction and provisional capacity 64 per locus rather than per Algo;
- the owner maps complete component keys only to immutable
  `LocusMetricComponentState2D`; route-specific contributions are derived after
  lookup and are never shared;
- analytic capability, then a GeoCeDG-owned per-call differential integrator;
  evaluator-only values are uncertified/unsupported without assumptions;
- initial `eps_metric_abs=1e-10`, `eps_metric_rel=1e-9`, maximum depth 22,
  32768-evaluation and 16384-subdivision ceilings, deterministic
  sentinel-free error aggregation and direct reuse of the G6 guarantee;
- closed `MetricValue2D`, closed `MetricErrorAmount2D` and typed
  `MetricErrorEvidence2D` contracts;
- structurally optional traversal outcome: required for applicable
  between-position results and absent from total results;
- P1 current-revision rich failure publication with no stale success;
- explicit improper tail evidence; positive infinity is rich-only by default;
- the aggregate precedence in the G7A report;
- the exact source/test/package map in the developer API;
- functional repeated/nested gates, not absolute timing gates.

`SHORTEST` remains deferred. The author-approved G7B implementation realizes
this architecture.

## 15. G7B minimum acceptance architecture

The author-approved G7B minimum includes:

- distinct between-position and total query values;
- position/binding separation and explicit stale/rebind behavior;
- route resolver with FORWARD, REVERSE, ZERO/FULL_CYCLE and
  STOP/WRAP/STRICT;
- component integration capability hierarchy;
- accepted bounded index strategy with cache-off oracle;
- accepted dedicated shared-owner lifecycle and one-build-per-key budget;
- `LocusMetricAggregator2D`;
- immutable `LocusMetricResult2D`;
- `GeoLocusMetricResult` and normal-DAG `AlgoLocusMetricV2`;
- an explicit optional scalar adapter that never becomes metric authority;
- lifecycle, exception, invalidation, repeated and nested gates;
- internal API documentation and developer laboratory.

It still excludes commands, XML, public `Path`, 3D, G5 changes, G8 and G9.

## 16. Implemented class and ownership map

The shared common module now contains the query, binding, route, policy,
capability, integration, component-state, index, contribution, aggregation and
rich-result values under `org.geocedg.common.kernel.locus.metric`. Publication
is split between `AlgoLocusMetricV2`, `GeoLocusMetricResult` and the explicit
`AlgoLocusMetricScalarAdapter`.

`GeoLocusV2` owns one lazily created, kernel-thread-confined
`LocusMetricSharedOwner2D`. Every metric algorithm holds a lease. The final
lease, a semantic revision/topology transition, undefined state or source
removal synchronously clears retained state. Capacity is the initial
provisional, non-normative 64-entry implementation default from ADR 0007, with
deterministic insertion-order eviction. No global or Construction-wide
registry exists.

The owner publishes only immutable `LocusMetricComponentState2D` after a
successful private build. It never stores routes, query results, contributions
or aggregates. `REFERENCE_NO_INDEX_REUSE` remains the semantic oracle and the
productive tests require full rich-result equality with the indexed path.

The only upstream-owned productive edit is the append-only
`GeoClass.LOCUS_METRIC_RESULT` enumerator. GeoCeDG-owned existing seams are
limited to `GeoLocusV2` lifecycle and the developer laboratory; base
`GeoElement`, numeric interfaces, commands, XML/factories, Path and 3D remain
unchanged.

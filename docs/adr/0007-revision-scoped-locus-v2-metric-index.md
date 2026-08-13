# ADR 0007: revision-scoped Locus V2 metric index

- Status: **Accepted**
- Author review disposition: **ACCEPTED; PRODUCTIVE G7B ARCHITECTURE AUTHOR APPROVED**
- Prior disposition: **APPROVED AS G7A WORKING ARCHITECTURAL HYPOTHESIS**
- Roadmap state: G7 `PASS`; G7A `PASS`; G7B `PASS — AUTHOR APPROVED`
- Decision phase: final G7A/G7A-R1 author closeout
- Date: 2026-08-13

The author accepted the architecture after G7A and G7A-R1 characterization.
The separately authorized G7B task now implements the candidate; this ADR
remains the decision authority rather than execution evidence.

## Context

The Accepted G6 contract makes the locus evaluator, provider-owned semantic
parameter, branch/domain model and semantic revision authoritative. G6R adds
bounded evaluation sessions and view-local render caches, but no productive
metric exists. A V2 length index must therefore be derived from semantic
evaluation and must not reuse render tessellation or legacy locus samples.

Repeated between-position and total-length queries can reuse component-local
metric work. The reuse boundary is delicate:

- a new semantic revision can change values, valid components or topology;
- the same visible point can have multiple constructive preimages;
- component keys are revision-scoped, while semantic positions should remain
  durable when possible;
- tolerance, multiplicity and improper-limit policy alter the result;
- nested constructions can turn an unbounded or incorrectly owned cache into a
  per-point rebuild or retained-revision leak.

The design must preserve cache/index ON/OFF semantic equality. Performance is
never permission to weaken metric meaning, provenance or diagnostics.

## Decision

The accepted index strategy is **`LAZY_COMPONENT_REVISION`**, a bounded lazy
component-scoped revision index. The accepted ownership is independently
**`DEDICATED_SHARED_OWNER`**. A lazy component/revision organization does not
imply one index per algorithm.

An entry is associated with one locus identity, one semantic revision, one
constructive branch and one resolved valid-domain component. It holds an
immutable `LocusMetricComponentState2D` or equivalent: adaptive partitions,
cumulative variation/arc-coordinate evidence, capability/integration metadata
and component-level error state. It must not hold a route-specific
`LocusMetricContribution2D`, screen-space tessellation or render-cache data.

The complete component key contains no A/B endpoints. A route segment and an
immutable component state produce a route-specific contribution after lookup;
a total query uses the complete component extent. One state can therefore
produce many contributions. The owner shares no query result, route,
contribution or aggregate result.

## Executed comparison

G7A implemented equivalent, test-private characterization paths for:

### `REFERENCE_NO_INDEX_REUSE`

Resolve and compute every query without cross-query metric-index reuse. This is
the semantic reference and cache-off oracle. Ordinary evaluator-session reuse
may be separately controlled and must be reported.

### `EAGER_WHOLE_REVISION`

Build all supported branch/component metric state for a revision on first use.
Measure up-front work, unused components, retained state, invalidation and
repeated-query behavior.

### `LAZY_COMPONENT_REVISION`

Build only requested component entries and reuse them for compatible queries.
Measure build count, partial coverage, overlap reuse, eviction and nested
composition.

The lazy strategy must win on relevant functional counters and bounded-state
behavior without changing semantics. It cannot be selected merely because it
sounds preferable.

## Complete key

Every reusable entry key includes at least:

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

If experiments reveal another result-affecting input, it becomes part of the
key before reuse is allowed. Equality/hashing must use immutable values and
must be covered by mutation/aliasing tests.

`resolvedValidComponentKey` is revision-scoped. It is not a durable semantic
position ID and must not be used to repair a stale binding across revisions.

## Characterized ownership candidate

R1 compared four test-private ownership shapes over the same immutable
component builder:

1. `ALGO_LOCAL_INDEX`;
2. `LOCUS_ATTACHED_SHARED_INDEX`;
3. `CONSTRUCTION_SCOPED_METRIC_REPOSITORY`; and
4. `DEDICATED_SHARED_OWNER`.

The author accepts `DEDICATED_SHARED_OWNER`. The candidate
`LocusMetricSharedOwner2D` is a GeoCeDG-owned,
non-GeoElement derived service associated with one active source locus inside
one Construction. Multiple `AlgoLocusMetricV2` consumers acquire compatible
immutable component metric states through it while remaining ordinary direct
DAG dependents of the source locus.

The owner is deliberately separate from semantic `GeoLocusV2` value state,
render state, Algebra View and a Construction-global repository. It owns no
dependency edge, result, route, query aggregation or algorithm callback. A
narrow source-locus lifecycle hook acquires and invalidates the service; each
metric algorithm owns only a consumer lease. The final consumer and source
locus removal release the owner. Copy/set do not carry it, and undo/redo
reacquire it through normal DAG reconstruction.

`ALGO_LOCAL_INDEX` is lifecycle-simple but R1 measured N component builds for
N compatible results. A direct locus-attached index would couple derived cache
state to semantic Geo copy/set state. The real `Construction` has no narrow
derived metric-service seam, so a repository there broadens retention,
invalidation and capacity competition across unrelated loci. The dedicated
owner obtains the shared reuse without those two ownership couplings.

The selected owner must:

- be reachable and releasable through the normal locus/algorithm lifecycle;
- have an explicit revision/topology/undefined/removal boundary;
- avoid static/global state and a hidden second DAG;
- avoid retaining a removed `GeoLocusV2`, `AlgoElement` or Construction;
- permit deterministic bounded eviction and one capacity per active locus;
- remain confined to the kernel thread; and
- be testable through `REFERENCE_NO_INDEX_REUSE`.

The productive Java package is `org.geocedg.common.kernel.locus.metric`; the
author-approved implementation provides the dedicated per-locus owner there.

## Lifecycle and safety rules

The author-approved G7B implementation provides all of the following from its
first productive version:

1. bounded entry count and/or bounded component work units;
2. a documented deterministic eviction rule;
3. no retained obsolete revision after invalidation/publication;
4. defensive immutable keys, partitions and component-state values;
5. kernel-thread confinement with no concurrency or background mutation;
6. atomic entry publication only after a complete successful private build;
7. exception safety and cleanup through `finally`;
8. no partially valid entry observable after cancellation, exception or
   numerical failure;
9. explicit invalidation on revision, topology, capability, policy or
   tolerance changes;
10. index ON/OFF equality for value, axes, diagnostics and decomposition;
11. counters for builds, hits, misses, evictions and retained entries;
12. copy/remove/undo/redo behavior that cannot carry stale or foreign state;
13. one component-state build per compatible complete key across metric consumers
    until eviction/invalidation; and
14. deterministic evaluation/subdivision/depth work ceilings included in the
    policy identity and key.

Index failure must not corrupt the current rich result. Under accepted P1,
revision `r+1` first makes revision `r` non-current. A failed new private build
leaves no index entry advertised for that key and publishes one coherent
`Absent` rich failure snapshot for `r+1`; the scalar adapter is undefined. A
failure may never leave the successful payload from `r` observable as current
or combine an old value with a new status.

## Semantic boundaries

The index:

- accelerates variation/integration over one valid component;
- never selects a route or open-boundary policy;
- never aggregates disconnected components by inventing connectivity;
- never deduplicates constructive multiplicity;
- never owns durable `LocusSemanticPosition2D` identity;
- never crosses an invalid-domain gap;
- never turns refinement agreement into a certified error bound;
- never reads `LocusRenderCache2D`, render vertices, legacy `myPointList`,
  viewport, zoom, DPI or pixel tolerance.

`LocusMetricRouteResolver2D` resolves route semantics.
`LocusMetricAggregator2D` combines component contributions. Metric evaluation
may request compatible component states, then derives a contribution for the
specific route segment or total extent. Neither route nor aggregation is
absorbed by the index.

## Required measurements

For each strategy, run 1, 10 and 100 queries for:

- identical A/B and overlapping arcs;
- reverse traversal and periodic cycles;
- STOP, WRAP and STRICT;
- repeated total length;
- tolerance/policy change;
- revision/topology invalidation;
- 1, 3, 10 and 100 compatible metric consumers sharing one component key;
- total-first/local-first query-order permutations;
- different loci and different Constructions;
- last-consumer and source-locus removal;
- nested metric composition and repeated upstream total consumption.

Capture evaluator, derivative and integrator calls; subdivisions; component
and index builds; hits/misses; evictions; retained entries; invalidations; and
query latency. Functional counters and semantic equivalence are hard gates;
wall-clock is initially informative.

## Alternatives considered

### No index reuse

This is simplest and remains the semantic oracle. It may be acceptable for a
future deliberately unsupported or low-volume path, but likely repeats
component integration and scales poorly in nested constructions.

### Eager whole-revision index

This can make later queries cheap, but computes unused components, creates
large invalidation bursts and risks retaining unnecessary state. It remains a
required measured comparator.

### Lazy component-scoped revision index

This is the accepted measured G7A strategy because the semantic unit, invalidation
unit and repeated-query reuse unit coincide. The 100-query same-component trace
used one lazy build versus three eager and 100 no-reuse builds; repeated total
used three lazy/eager builds versus 300 no-reuse builds. It adds
eviction/key/lifecycle complexity and therefore must pass stronger tests.

### Global cache

Rejected. Ownership, Construction retention, deterministic invalidation and
bounded lifetime cannot be justified for the minimum.

### Render-cache or legacy-sample reuse

Rejected. Those representations are view/sample derived and cannot define
length.

### Unbounded revision history

Rejected. Historical revisions are not a productive cache contract and would
leak through dynamic edits.

### Ownership alternatives

Per-algorithm ownership remains the simplest reference implementation but is
functionally inferior when compatible results coexist. Direct index state on
`GeoLocusV2` and a Construction-wide repository remain measured comparators,
not selected ownership. The dedicated shared owner is accepted because it
combines one-build cross-result reuse with a per-locus bounded lifecycle and
does not create another dependency graph.

## G7A characterization evidence

The same-component 1/10/100 traces produced:

| Strategy | 1 query | 10 queries | 100 queries | Retained component entries |
|---|---:|---:|---:|---:|
| `REFERENCE_NO_INDEX_REUSE` | 1 build | 10 builds | 100 builds | 0 |
| `EAGER_WHOLE_REVISION` | 3 builds | 3 builds | 3 builds | 3 |
| `LAZY_COMPONENT_REVISION` | 1 build | 1 build | 1 build | 1 |

For 100 repeated total queries over all three components, reference produced
300 builds and both indexed alternatives produced 3. Lazy therefore avoids
unrequested component work without penalizing a warmed total trace.

Same/overlapping/reverse/periodic/STOP/WRAP routes reused the same complete key.
STRICT rejected unreachable input before integration. Changes to algorithm,
route policy, tolerance, multiplicity and improper-limit policy each missed.
Cache OFF, eager and lazy were semantically equal.

The lifecycle probe demonstrated bounded deterministic insertion-order
eviction, obsolete-revision removal, one rebuild after invalidation, branch/
topology invalidation, atomic publication, no retained entry after injected
failure and `finally` cleanup. Nested probes used one build per affected
metric level/revision/policy and none per downstream point.

G7A established `LAZY_COMPONENT_REVISION`, current revision only; the author
accepts a provisional 64-entry capacity per active locus shared owner and an explicit
approximate-byte counter.
The production footprint and capacity remain subject to G7B measurement. The
capacity is explicitly non-normative. No global cache, background thread or concurrent
quadrature is justified.

R1 then measured cross-result ownership for one compatible component key:

| Consumers | Algo-local builds | Algo-local duplicate builds | Dedicated-owner builds | Cross-result hits |
|---:|---:|---:|---:|---:|
| 1 | 1 | 0 | 1 | 0 |
| 3 | 3 | 2 | 1 | 2 |
| 10 | 10 | 9 | 1 | 9 |
| 100 | 100 | 99 | 1 | 99 |

At N=100 the accounting fixture retained 108800 bytes in the local shape and
13760 bytes in the dedicated shared shape; the constants are comparative, not
a JVM heap-size claim. Total-first and local-first orders each built exactly
three unique components. Policy/capability/algorithm/tolerance/multiplicity/
improper/work-budget changes missed independently. Different loci and
Constructions never shared; revision/topology/undefined/removal transitions
retained no obsolete entry. The nested R1 fixture recorded zero compatible
duplicate builds and preserved all zero-render/zero-legacy/per-point gates.

Capacity 64 is therefore a provisional default per active locus shared owner,
not per algorithm and not per Construction. Deterministic insertion-order FIFO
remains the minimum candidate; wall-clock LRU is excluded.

## Consequences

Positive consequences:

- overlapping and repeated component queries can reuse semantic work;
- total and between-position operations share immutable component state without
  sharing route meaning or route-specific contributions;
- invalidation can be tested at explicit revision/component boundaries;
- functional counters can detect nested per-point rebuilds.

Costs and risks:

- complete key/version management becomes part of the semantic contract;
- component identity derivation and topology transitions need explicit tests;
- the dedicated owner and lease must participate correctly in locus/algorithm
  lifecycle without becoming GeoElement semantic state;
- lazy partial state requires careful atomic publication and decomposition.

## Final author disposition

The author reviewed and accepted the three-strategy and ON/OFF evidence,
dedicated shared ownership, current-revision and work-budget keying,
component-key derivation, deterministic eviction, invalidation, exception,
copy/remove/undo/redo and nested gates. Capacity 64 is accepted only as a
provisional initial G7B implementation default and remains non-normative. The
productive logical retained-state estimate does not stabilize that capacity.

```text
INDEX STRATEGY = LAZY_COMPONENT_REVISION
INDEX OWNERSHIP = DEDICATED_SHARED_OWNER
ADR 0007 = ACCEPTED
G7B = PASS — AUTHOR APPROVED
G7 = PASS
```

## Implementation evidence

G7B implements `LAZY_COMPONENT_REVISION` and
`DEDICATED_SHARED_OWNER` as distinct decisions. One non-GeoElement owner is
attached to each active `GeoLocusV2`; metric algorithms acquire leases and the
last lease releases retained state. Revision/topology, undefined and source
removal transitions invalidate synchronously. Owners are isolated by source
locus and Construction and have no dependency edges or callbacks.

The productive map is exactly:

```text
complete component key
    -> private build
    -> immutable LocusMetricComponentState2D publication
    + route segment
    -> query-local LocusMetricContribution2D
```

The provisional capacity remains 64 entries per active source locus with
deterministic insertion-order eviction. Productive probes confirm one build
and 99 cross-result hits for 100 compatible consumers, zero compatible
duplicate builds, no failed entry publication, current-revision retention only
and semantic equality with `REFERENCE_NO_INDEX_REUSE`. The author approves
this productive architecture. Capacity 64 remains a provisional non-normative
implementation default; the deterministic logical retained-state estimate is
not JVM heap/object-layout evidence and does not stabilize it.

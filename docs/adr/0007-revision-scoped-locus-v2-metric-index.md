# ADR 0007: revision-scoped Locus V2 metric index

- Status: **Proposed**
- Author review disposition:
  **APPROVED AS G7A WORKING ARCHITECTURAL HYPOTHESIS**
- Roadmap state: G7 `PENDING / NOT STARTED`
- Decision phase: G7A characterization and second author review
- Date: 2026-08-12

This ADR is deliberately not Accepted. It records the architecture that G7A
must try to falsify or confirm. It does not authorize an index implementation
in productive source and does not start G7A or G7B.

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

## Working decision

G7A shall evaluate a **bounded lazy component-scoped revision index** as the
working architectural hypothesis.

An entry is associated with one locus identity, one semantic revision, one
constructive branch and one resolved valid-domain component. It may hold
immutable adaptive partitions, cumulative variation/arc-coordinate data,
integration contribution summaries and error metadata. It must not store
screen-space tessellation or use a render cache as authority.

This hypothesis remains provisional until measured against the alternatives
and approved by the author.

## Required comparison

G7A shall implement equivalent, test-private characterization paths for:

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
```

If experiments reveal another result-affecting input, it becomes part of the
key before reuse is allowed. Equality/hashing must use immutable values and
must be covered by mutation/aliasing tests.

`resolvedValidComponentKey` is revision-scoped. It is not a durable semantic
position ID and must not be used to repair a stale binding across revisions.

## Ownership to characterize

G7A must compare feasible GeoCeDG-owned ownership locations in the real kernel,
including algorithm/output-local or construction/locus-scoped services. The
selected owner must:

- be reachable through the normal construction lifecycle;
- have an explicit revision/invalidation boundary;
- avoid static/global state;
- avoid retaining a removed `GeoLocusV2`, `AlgoElement` or Construction;
- permit deterministic bounded eviction;
- remain confined to the kernel thread;
- be testable with index disabled.

This ADR does not preselect a concrete Java package or owner before that audit.

## Lifecycle and safety rules

Any accepted implementation must provide all of the following from the first
G7B candidate:

1. bounded entry count and/or bounded component work units;
2. a documented deterministic eviction rule;
3. no retained obsolete revision after invalidation/publication;
4. defensive immutable keys, partitions and contribution values;
5. kernel-thread confinement with no concurrency or background mutation;
6. atomic publication only after a complete successful build;
7. exception safety and cleanup through `finally`;
8. no partially valid entry observable after cancellation, exception or
   numerical failure;
9. explicit invalidation on revision, topology, capability, policy or
   tolerance changes;
10. index ON/OFF equality for value, axes, diagnostics and decomposition;
11. counters for builds, hits, misses, evictions and retained entries;
12. copy/remove/undo/redo behavior that cannot carry stale or foreign state.

Index failure must not corrupt the current rich result. A failed new
publication leaves no entry advertised for that key and produces the
appropriate metric status through the normal algorithm result.

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
`LocusMetricAggregator2D` combines component contributions. Their outputs may
request compatible index entries, but neither responsibility is absorbed by
the index.

## Required measurements

For each strategy, run 1, 10 and 100 queries for:

- identical A/B and overlapping arcs;
- reverse traversal and periodic cycles;
- STOP, WRAP and STRICT;
- repeated total length;
- tolerance/policy change;
- revision/topology invalidation;
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

This is the working preference because the semantic unit, invalidation unit
and repeated-query reuse unit can coincide. It adds eviction/key/lifecycle
complexity and therefore must pass stronger tests.

### Global cache

Rejected. Ownership, Construction retention, deterministic invalidation and
bounded lifetime cannot be justified for the minimum.

### Render-cache or legacy-sample reuse

Rejected. Those representations are view/sample derived and cannot define
length.

### Unbounded revision history

Rejected. Historical revisions are not a productive cache contract and would
leak through dynamic edits.

## Consequences if accepted after G7A

Positive consequences:

- overlapping and repeated component queries can reuse semantic work;
- total and between-position operations share component contributions without
  sharing route meaning;
- invalidation can be tested at explicit revision/component boundaries;
- functional counters can detect nested per-point rebuilds.

Costs and risks:

- complete key/version management becomes part of the semantic contract;
- component identity derivation and topology transitions need explicit tests;
- the owner and capacity must participate correctly in GeoElement lifecycle;
- lazy partial state requires careful atomic publication and decomposition.

## Acceptance conditions

ADR 0007 may become Accepted only after G7A:

- demonstrates all three strategies on the same fixtures;
- proves ON/OFF semantic equality;
- establishes capacity and deterministic eviction;
- shows no obsolete-revision retention;
- resolves owner, component-key derivation and invalidation;
- passes exception, aliasing, copy/remove/undo/redo and nested tests;
- records measured counter evidence and receives explicit author approval.

If any condition is unmet, retain this ADR as Proposed, revise it or replace it.
G7B is blocked until an accepted/replaced index decision exists.

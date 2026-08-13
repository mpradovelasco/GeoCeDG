# G7 Locus V2 native metrics plan

| Field | Value |
|---|---|
| Status | **G7B PASS — AUTHOR APPROVED** |
| Roadmap gate | G7 `PASS` |
| G7A | `PASS — AUTHOR APPROVED` |
| G7B | `PASS — AUTHOR APPROVED` |
| G8 | `NOT STARTED` |
| G6 baseline | G6 `PASS`; G6R `PASS` |
| Metric specification | [`NORMATIVE / AUTHOR APPROVED`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Index ADR | [ADR 0007 `Accepted`](../adr/0007-revision-scoped-locus-v2-metric-index.md) |
| Date | 2026-08-13 |

This package reconstructs G7 planning exclusively from the current
`origin/main` history, versioned G6/G6R authorities and the author's decisions
recorded in the recovery task. No inaccessible G7 commit, result or probe was
recovered or treated as evidence.

Planning recovery itself did not execute G7A. The separately authorized G7A
reexecution now adds only test-private probes and versioned evidence; it creates
no productive metric class or `src/main` change. The focused R1 refinement
adds safe value/error contracts, deterministic work ceilings and measured
multi-consumer ownership evidence, also test-private only. The final author
review approved both stages and authorized G7B. The separately executed G7B
task now supplies the internal productive candidate and review evidence.

## 1. Authority

Apply these sources in order:

1. repository [`AGENTS.md`](../../AGENTS.md);
2. the [living roadmap](geocedg_roadmap.md);
3. the normative
   [Locus V2 semantic contract](../../geocedg/specs/locus/locus-v2-semantics.md);
4. [Accepted ADR 0006](../adr/0006-parallel-locus-v2-semantic-entity.md);
5. the [G6A](../validation/g6a_locus_v2_characterization_report.md),
   [G6B](../validation/g6b_locus_v2_kernel_report.md) and
   [G6R](../validation/g6r_locus_v2_hardening_report.md) reports;
6. the [productive V2 architecture](../architecture/locus_v2_implementation.md)
   and [internal API](../developer/locus_v2_api.md);
7. [G6R traceability](../validation/g6r_locus_v2_traceability_matrix.md) and
   versioned evidence under `geocedg/validation/locus-v2/`;
8. the actual productive Locus V2 source;
9. the versioned CeDG scientific catalog;
10. hash-pinned legacy models.

External conversations are not authority. Render artifacts, screenshots,
generated reports and legacy sampled values remain evidence only.

## 2. Objective and architectural layer

G7 will add native two-dimensional Locus V2 length semantics to the shared Java
kernel. The result must be a normal dynamic construction result with explicit
semantic, numeric and lifecycle state. G7 is split into:

- **G7A — characterization and author decision:** measure and settle the
  numerical, upstream, lifecycle, scalar-participation and index questions;
- **G7B — minimum metric kernel:** implement only the author-approved G7A
  contract, with all basic hardening gates present from its first candidate.

The G7B working product boundary is:

```text
internal Java API
+ GeoLocusMetricResult
+ developer laboratory
```

The candidate kernel shape is:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

`GeoNumeric` is not the result authority. G7A audited and the author accepts an
append-only classification equivalent to `GeoClass.LOCUS_METRIC_RESULT`; it
must not reuse `NUMERIC`, `LOCUS` or `LOCUS_V2`.

## 3. Explicitly excluded

Neither this recovery nor the G7B minimum includes:

- any change to legacy `GeoLocus`, `myPointList`, `AlgoPerimeterLocus`,
  `Length` or `Perimeter`;
- `LocusLength` or any other public command;
- public `Path`/point-on-locus behavior;
- XML, factory registration, persistence or migration;
- a public automatic arc-coordinate surface;
- a companion `GeoNumeric` as metric authority;
- geometric-image-union length or geometric deduplication;
- 3D behavior, G5 export changes, G8 intersections or G9 spatial semantics;
- external numerical libraries, C++, concurrency, background mutation or DAG
  flattening;
- JMH unless G7A demonstrates that functional counters cannot answer a
  required question.

## 4. Mathematical contract

Length is total variation. For `F:[a,b]\to\mathbb R^2`:

\[
\operatorname{Var}(F;[a,b])
=
\sup_P\sum_i\lVert F(t_i)-F(t_{i-1})\rVert .
\]

For absolutely continuous `F`:

\[
\operatorname{Var}(F;[a,b])
=
\int_a^b\lVert F'(t)\rVert\,dt .
\]

The numerical algorithm estimates or establishes this mathematical quantity;
it never defines it. Render vertices, `LocusRenderCache2D`, legacy samples,
`myPointList`, viewport, zoom, DPI and pixel tolerances are forbidden metric
inputs.

The minimum metric is `CONSTRUCTIVE_TRAVERSAL_LENGTH`. Retracing counts once
for every constructive preimage, and distinct constructive branches count
separately even when their images coincide.
`GEOMETRIC_IMAGE_UNION_LENGTH` is outside G7.

## 5. Two operations, not one overloaded convention

### 5.1 Between-position length

`BetweenPositionsMetricQuery` selects a route from semantic position A to
semantic position B. It includes revision bindings, direction, same-position
semantics and an open-boundary policy.

Length is always non-negative. `FORWARD` and `REVERSE` are required in G7B.
`SHORTEST` may be characterized in G7A but is deferred and is never the
default.

Equal semantic positions require an explicit choice:

- `ZERO_LENGTH`: no traversal, value zero;
- `FULL_CYCLE`: exactly one fundamental cycle, only on approved periodic
  semantics.

Cartesian equality cannot select either behavior.

### 5.2 Complete-locus length

`TotalLocusMetricQuery` has no A/B positions and no direction input. It is not
encoded as `A == B`, `FULL_CYCLE` or `WRAP_TO_START`.

For semantic revision `r`, branches `j` and their valid-domain components
`C_{r,j,k}`:

\[
\mathcal L_{\mathrm{total}}(L_r)
=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Every valid component contributes exactly once. A periodic branch contributes
one fundamental cycle. Disconnected components are aggregated without a
joining chord or a fabricated route, and gap diagnostics remain visible.

Empty domains, isolated points and collapsed images have value zero with
`COMPLETE` coverage and a diagnostic. Zero is not failure or absence.

## 6. Semantic positions and revision bindings

Durable semantic position and evaluated binding are separate values:

```text
LocusSemanticPosition2D
    locusIdentity
    branchKey
    providerVersion
    providerCanonicalParameter

MetricPositionBinding2D
    semanticPosition
    semanticRevision
    resolvedValidComponentKey
    evaluationStatus
    evaluatedPoint
    diagnostics
```

Revision is not necessarily part of durable identity. Component keys are
resolved per revision; a component split does not automatically change the
semantic position. There is no coordinate-proximity repair. An invalidated
binding reports `POSITION_STALE` and rebinds only through an explicit
operation.

## 7. Route resolution and open policies

`LocusMetricRouteResolver2D` interprets A/B, revision, branch, component,
direction, periodic seam, `ZERO_LENGTH`/`FULL_CYCLE`, global boundaries,
internal gaps and reachability. It produces an immutable
`LocusMetricRoute2D` with:

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

Each `LocusMetricRouteSegment2D` belongs to exactly one valid component. The
resolver never integrates.

For an open branch ordered `start --- B ------ A ----- end` and a `FORWARD`
query from A to B:

| Policy | Value | Required outcome |
|---|---|---|
| `STOP_AT_END` | length A to end | `targetReached=false`, `STOPPED_AT_BOUNDARY`, incomplete and not scalar-admissible |
| `WRAP_TO_START` | length A to end + start to B | `wrapped=true`, `targetReached=true`, `geometricallyConnected=false` |
| `STRICT` | absent | `TARGET_NOT_REACHABLE` |

Wrap is an explicitly requested metric convention. It creates no incidence,
geometric closure or topology. No policy may cross an internal invalid-domain
gap. A discontinuity produces `DISCONTINUITY_ENCOUNTERED`, not an invented
connection.

## 8. Result and aggregation contract

`LocusMetricResult2D` and each contribution contain a closed immutable
`MetricValue2D`: finite non-negative, positive infinity or absent. Only the
finite variant exposes `OptionalDouble`; a bare value accessor, NaN semantics,
magic doubles and exceptions for normal absence are forbidden.

The result remains a defensive immutable value with orthogonal axes:

- `MetricValueKind`: `FINITE`, `POSITIVE_INFINITY`, `ABSENT`;
- `MetricCoverage`: `COMPLETE`, `INCOMPLETE`;
- `MetricComputationStatus`: `SUCCESS`, `INVALID_QUERY`, `UNSUPPORTED`,
  `NUMERICAL_FAILURE`, `LIMIT_NOT_ESTABLISHED`;
- `MetricRectifiability`: `RECTIFIABLE`, `NON_RECTIFIABLE`,
  `UNDETERMINED`;
- between-position `TraversalOutcome`: `TARGET_REACHED`,
  `STOPPED_AT_BOUNDARY`, `WRAPPED_TO_START`, `TARGET_NOT_REACHABLE` or
  `DISCONTINUITY_ENCOUNTERED`.

It also preserves construction fidelity, evaluator method,
metric/integration method, representation role, units, provenance, diagnostics
and contribution decomposition. Numeric quality directly reuses normative G6
`LocusSemanticMetadata2D.NumericGuarantee`; G7 defines no duplicate enum.
`MetricErrorEvidence2D` or equivalent uses typed established,
not-established and not-applicable amounts, explicit complete/partial scope,
method, assumptions and certificate metadata. Error fields use no sentinels.
`DIVERGENT` is not a generic numerical-failure bucket.

`LocusMetricAggregator2D` combines contributions and constructive
multiplicity, aggregates value and error, propagates the weakest guarantee,
sets coverage/status and preserves decomposition. It does not integrate a
component.

## 9. G7A characterization work

G7A produced reproducible evidence and an author-decision package using only
narrowly scoped test-private probes. It did not implement productive metric
classes.

### 9.1 Upstream impact and rich Geo lifecycle

Audit actual source behavior for:

- `GeoClass` append-only changes and every exhaustive enum/switch test;
- `GeoElement.isDefined()` versus scalar admissibility;
- `NumberValue` and related numeric interfaces, Algebra View, generic numeric
  algorithms and CAS;
- `AlgoElement` input/output registration and normal invalidation;
- creation, publication, undefined/recovery, remove, labels, selection,
  lists/sequences, defaults/styles and topology changes;
- `copy`/`copyInternal`/`set` without foreign construction state, stale
  revision, stale binding, stale index or partial-current leakage;
- `ConstructionDefaults`, commands, legacy metrics, `Path`, XML/factory,
  2D/3D dispatch, G5 export and packaging.

Defined rich-result state and scalar-admissible state are different concepts.

### 9.2 Numeric participation decision

Compare:

- **A:** no generic numeric facade;
- **B:** conditional read-only numeric facet;
- **C:** explicit numeric adapter.

The original working preference was **B only if the real API could express
scalar admissibility without hiding rich status/error semantics**. The source
audit found that it cannot; the author therefore accepts C, an explicit derived
numeric adapter. A companion `GeoNumeric` cannot become authority.

Candidate scalar-admissible results are finite, successful, semantically
satisfied, suitably covered, current, fully supported, valid zero or an
explicit valid wrap. The following are inadmissible by default:
`STOP_AT_END` partial, `TARGET_NOT_REACHABLE`, `INCOMPLETE`,
`POSITION_STALE`, `DIFFERENT_BRANCH`, discontinuity, `UNSUPPORTED`,
`NUMERICAL_FAILURE`, `LIMIT_NOT_ESTABLISHED`, `ABSENT` and positive infinity
unless separately approved.

### 9.3 Integration and error policy

Characterize the capability hierarchy:

1. analytic/closed form;
2. differential quadrature;
3. evaluator-only adaptive metric;
4. unsupported.

Refinement agreement alone never yields `CERTIFIED_ERROR_BOUND`.
`ESTIMATED_ERROR` requires explicit assumptions; otherwise report
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

Measure and propose independent `eps_metric_abs` and `eps_metric_rel`,
stopping policy, independent evaluation/subdivision/depth work limits,
improper-limit policy and aggregate error
policy. Do not reuse the G6 evaluation envelope, `eps_domain`, render pixel
tolerance or a future G8 root tolerance.

Characterize finite A/B length on an unbounded branch, finite improper total,
positive infinity, non-rectifiable, unsupported and limit-not-established.
Viewport cutoff is forbidden.

### 9.4 Reparameterization

Demonstrate length invariance with:

- `t=u^3` as a monotone parameterization with derivative degeneration; and
- a regular orientation-preserving map

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1},\qquad \phi'(u)>0.
\]

The derived arc coordinate
`s(t)=\operatorname{Var}(F;[t_0,t])` may be characterized only per valid
component. It does not replace the provider semantic parameter or connect
gaps/branches, and is not automatically public.

### 9.5 Independent references

Numerical fixtures require an analytic reference, a high-precision reference
or an independent numerical method. Any Python-generated expected constant
must version the formula, precision, runtime/library, script and output hash.
Python is validation evidence, never kernel authority.

## 10. Metric index experiment and working hypothesis

G7A implemented comparable characterization strategies:

1. `REFERENCE_NO_INDEX_REUSE`;
2. `EAGER_WHOLE_REVISION`;
3. `LAZY_COMPONENT_REVISION`.

The measured recommendation is a bounded lazy component-scoped revision index.
It is not accepted architecture until author review. Its complete key contains
at least:

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

There is no global cache and no unbounded revision history. A component key is
revision-scoped and is not durable position identity.

R1 compared algorithm-local, direct locus-attached, Construction-scoped and
dedicated shared ownership. The author accepts a GeoCeDG-owned non-GeoElement
`DEDICATED_SHARED_OWNER` tied to one active source
locus and acquired by ordinary metric algorithms through a narrow lifecycle
hook. The algorithms remain direct normal-DAG dependents; the owner has no
dependency edges, route/aggregate authority, result publication or algorithm
callbacks. It shares only immutable `LocusMetricComponentState2D` values for
complete component keys; route-specific contributions, routes, query results
and aggregates remain algorithm-owned.

For N compatible metric results, the hard budget is one component-state build
per complete key until eviction/invalidation. At N=100 R1 measured 100
local builds and 99 duplicates versus one dedicated-owner build and 99 hits.
Different loci/Constructions never share. The provisional capacity is 64 per
active locus owner, not per algorithm, with deterministic insertion-order
eviction; it is not normative.

## 11. Hardening required in the first G7B candidate

G7B must not defer basic safety to a later G7R. From its first candidate it
must provide:

- defensive immutable values and complete cache/index keys;
- bounded state and deterministic eviction;
- no retained obsolete revisions;
- kernel-thread confinement, no concurrency and no background mutation;
- P1 atomic publication: no failed index entry, but a coherent current-revision
  rich failure snapshot; never stale success or a hybrid payload;
- exception-safe updates;
- cleanup via `finally` and no partially valid published entry;
- complete rich-result and `GeoLocusMetricResult` lifecycle;
- cache/index ON/OFF semantic equality;
- normal DAG invalidation and recovery;
- documentation, API contracts, traceability and packaging boundaries;
- measured repeated-query and nested-composition behavior.

## 12. Repeated and nested gates

For each index strategy, trace 1, 10 and 100 queries for same A/B, overlapping
arcs, reverse, periodic, STOP, WRAP, STRICT, repeated total and
tolerance/policy changes. Record evaluator/derivative/integrator calls,
subdivisions, component/index builds, hits/misses, evictions, retained entries,
latency and invalidation.

For multi-consumer ownership, additionally trace 1, 3, 10 and 100 compatible
results, local-first/total-first order, full-key changes, revision/topology,
multiple loci/Constructions, removal and nested multi-results. Record unique
builds, compatible duplicates, cross-result hits and separate unique payload,
duplicate payload, metadata, owner and consumer retained bytes.

Characterize:

```text
L1
 -> metric(L1)
 -> L2
 -> metric(L2)
 -> L3
```

and repeated consumption of an upstream total metric. Hard expectations are
zero render access, zero legacy sample access, no whole-locus regeneration per
downstream point, no index build per point, reuse for the same revision/policy,
normal DAG invalidation and cache-off equality. Evidence must distinguish
necessary recomputation from waste.

Functional counters, bounded state, invalidation and semantic equality are
initial hard gates. Wall-clock is informative until reproducible budgets are
approved.

## 13. Scientific and legacy evidence

Preserve originals unchanged and use only as characterized evidence:

- `AlgoPerimeterLocus`;
- `Templatev7.ggb` tools `listLength`, `listLength12` and `postLocus`;
- `InterCilConoObliqueTwoLevels.ggb` as the functional two-level control;
- `InterCilConoOblique.ggb` as the pathological third-level reference.

These models are hash-pinned in their manifests. They do not define V2 length
and full migration is not a G7 gate.

Candidate scientific pilots are:

- analytic segment and circle;
- a small deterministic cylinder development;
- a traced nested metric fixture;
- a bounded oblique-cone subset if reproducible.

## 14. Validation package

The detailed cases and benchmark method are in:

- [semantic model](../architecture/locus_v2_metric_semantic_model.md);
- [architecture](../architecture/locus_v2_metric_architecture.md);
- [validation matrix](../validation/g7_locus_v2_metric_validation_matrix.md);
- [benchmark plan](../validation/g7_locus_v2_metric_benchmark_plan.md);
- [G7A execution prompt](../../.github/prompts/tasks/g7a-locus-v2-metric-characterization.prompt.md);
- [G7B execution prompt](../../.github/prompts/tasks/g7b-locus-v2-metric-kernel.prompt.md).

Required families include analytic curves, scale/translation and two
reparameterizations; self-intersection, multibranch/multicomponent, periodic,
cusp, collapsed/isolated/empty, stale binding and topology change; all open
policies, gaps, reverse and limits; total aggregation including infinite/mixed
cases; and repeated/nested composition through three levels.

## 15. Phase gates

### 15.1 G7A entry gate — satisfied

G7A may start only with:

- G6 and G6R still reproducible;
- this package unchanged or explicitly superseded;
- a clean authorized execution scope;
- explicit author authorization.

The reexecution recorded and satisfied all four conditions before probes ran.

### 15.2 Gate to close G7A

G7A passed after evidence:

- compares all three index strategies;
- resolves metric tolerances/error/improper-limit policy;
- resolves rich Geo lifecycle and scalar participation;
- audits every upstream impact listed above;
- validates route, total, repeated and nested cases;
- proposes the exact G7B editable set and budgets;
- received explicit author approval.

At final closeout the author made the metric spec normative and accepted ADR
0007. The closeout adds no productive metric implementation.

### 15.3 Gate to start G7B

The G7B entry decisions are now satisfied:

- `G7A = PASS — AUTHOR APPROVED`;
- `locus-v2-metrics.md` approved as normative;
- ADR 0007 Accepted or explicitly replaced;
- approved scalar, lifecycle, index, tolerance, error and improper-limit
  decisions;
- explicit G7B authorization, granted at this closeout.

The separately authorized prompt was executed on the G7B feature branch. Its
entry conditions remained satisfied and the result is `READY FOR AUTHOR
REVIEW`.

### 15.4 Gate to close G7B

The minimum candidate must pass the full matrix, functional benchmark gates,
cache ON/OFF equality, lifecycle and exception tests, focused Locus V2
verification, shared-kernel/Desktop build gates, traceability and author
review. Public command, XML, `Path`, 3D, G8 and G9 remain absent.

## 16. Author-approved G7A and focused R1 decisions

The fresh G7A reexecution is recorded in the
[characterization report](../validation/g7a_locus_v2_metric_characterization_report.md),
[R1 refinement report](../validation/g7a_r1_locus_v2_metric_refinement_report.md),
[candidate API](../developer/locus_v2_metric_api.md) and
[traceability matrix](../validation/g7a_locus_v2_metric_traceability_matrix.md).
It used no result from the lost workstation.

The author approves the measured recommendations:

- retain total variation, endpoint-free total query, constructive multiplicity,
  revision-separated positions/bindings and the route architecture;
- require FORWARD, REVERSE, ZERO/FULL, STOP, WRAP and STRICT while deferring
  SHORTEST;
- retain the rich result taxonomy and deterministic aggregate precedence;
- use closed immutable finite/infinity/absent values and typed error evidence
  without sentinels;
- use a closed `MetricErrorAmount2D` hierarchy so availability and amount
  cannot contradict;
- make traversal outcome structurally optional in the rich result: present for
  applicable between-position results and absent from total results;
- reuse the exact normative G6 `NumericGuarantee` type;
- publish a dedicated `GeoLocusMetricResult` with append-only
  `GeoClass.LOCUS_METRIC_RESULT`;
- reject conditional `NumberValue` strategy B and use explicit derived adapter
  strategy C when scalar participation is requested;
- use analytic capability first, then a GeoCeDG-owned per-call differential
  integrator; evaluator-only agreement is never a certified bound;
- retain `eps_metric_abs=1e-10` and `eps_metric_rel=1e-9` as the initial
  versioned tolerance policy; review R1's measured work ceilings of 32768
  evaluations, 16384 subdivisions and depth 22;
- advance bounded lazy component/revision indexing with provisional capacity
  64 per active locus shared owner, current-revision-only ownership and
  deterministic insertion-order eviction;
- use `DEDICATED_SHARED_OWNER`, which measured one build and 99 cross-result
  hits for 100 compatible consumers while preserving normal DAG/lifecycle
  isolation;
- map a complete component key to immutable component metric state and derive
  each route-specific contribution only after lookup;
- use P1 coherent current-revision rich failure publication with no failed
  index entry and an undefined scalar adapter;
- hard-gate functional counters, invalidation, exception safety, cache-off
  equality and nested zero-render/zero-sample behavior.

Same-component 1/10/100 traces built 1/10/100 reference components, 3/3/3
eager components and 1/1/1 lazy components. Across 100 repeated totals over
three components, reference built 300 while eager and lazy each built 3.
Wall-clock remains informational.

All 42 G7A recommendations and R1-1..R1-22 are author-approved with the three
final API normalizations. ADR 0007 is Accepted, the G7 metric specification is
normative, and G7B implements their internal candidate without closing G7.

## 17. Current disposition

```text
G7 PLANNING RECOVERY = PASS

G7 PLANNING = RESTORED AND AUTHOR-REVIEWED

GEOLOCUSMETRICRESULT =
APPROVED AS G7A WORKING ARCHITECTURE

G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED

G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED

G7B = PASS — AUTHOR APPROVED
G7 = PASS

G7B CAPACITY 64 =
PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT

G7B PUBLIC COMMAND = ABSENT
G7B XML/PERSISTENCE = ABSENT
G7B PUBLIC PATH = ABSENT
G7B 3D = ABSENT

G8 = NOT STARTED
G9 = NOT STARTED
```

## 18. G7B implementation handoff

G7B adds the internal metric package, rich normal-DAG Geo, metric algorithm,
explicit scalar adapter, bounded per-locus shared owner and opt-in laboratory.
The implementation maps complete component keys to immutable component state;
routes and contributions remain query-local. It registers no command, XML,
public `Path`, 3D, G8 or G9 behavior.

The focused evidence is recorded in the
[G7B kernel report](../validation/g7b_locus_v2_metric_kernel_report.md) and
[G7B traceability matrix](../validation/g7b_locus_v2_metric_traceability_matrix.md).
The gate to close G7B remains explicit author review; this implementation does
not self-declare G7 or G7B `PASS`.

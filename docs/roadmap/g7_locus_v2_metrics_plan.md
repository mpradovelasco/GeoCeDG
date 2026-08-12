# G7 Locus V2 native metrics plan

| Field | Value |
|---|---|
| Status | **RESTORED AND AUTHOR-REVIEWED PLANNING** |
| Roadmap gate | G7 `PENDING / NOT STARTED` |
| G7A | `NOT STARTED` |
| G7B | `NOT STARTED` |
| G8 | `NOT STARTED` |
| G6 baseline | G6 `PASS`; G6R `PASS` |
| Metric specification | [`PROPOSED — NOT NORMATIVE`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Index ADR | [ADR 0007 `Proposed`](../adr/0007-revision-scoped-locus-v2-metric-index.md) |
| Date | 2026-08-12 |

This package reconstructs G7 planning exclusively from the current
`origin/main` history, versioned G6/G6R authorities and the author's decisions
recorded in the recovery task. No inaccessible G7 commit, result or probe was
recovered or treated as evidence.

Planning recovery does not execute G7A. It creates no characterization probe,
no productive metric class and no source change. A future G7A and G7B each
require separate explicit authorization.

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

`GeoNumeric` is not the result authority. G7A must audit an append-only
classification equivalent to `GeoClass.LOCUS_METRIC_RESULT`; it must not reuse
`NUMERIC`, `LOCUS` or `LOCUS_V2`.

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

`LocusMetricResult2D` is a defensive immutable value with orthogonal axes:

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
metric/integration method, representation role, numeric guarantee,
absolute/relative error, units, provenance, diagnostics and contribution
decomposition. `DIVERGENT` is not a generic numerical-failure bucket.

`LocusMetricAggregator2D` combines contributions and constructive
multiplicity, aggregates value and error, propagates the weakest guarantee,
sets coverage/status and preserves decomposition. It does not integrate a
component.

## 9. G7A characterization work

G7A must produce reproducible evidence and an author-decision package. It may
add narrowly scoped read-only/test-private probes only when separately
authorized. It must not implement productive metric classes.

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

The working preference is **B only if the real API can express scalar
admissibility without hiding rich status/error semantics**. Otherwise G7A must
recommend A or C. A companion `GeoNumeric` cannot become authority.

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
stopping policy, refinement limits, improper-limit policy and aggregate error
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

G7A must implement comparable characterization strategies:

1. `REFERENCE_NO_INDEX_REUSE`;
2. `EAGER_WHOLE_REVISION`;
3. `LAZY_COMPONENT_REVISION`.

The working hypothesis is a bounded lazy component-scoped revision index. It
is not accepted architecture until G7A evidence and author review. Its complete
key contains at least:

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

There is no global cache and no unbounded revision history. A component key is
revision-scoped and is not durable position identity.

## 11. Hardening required in the first G7B candidate

G7B must not defer basic safety to a later G7R. From its first candidate it
must provide:

- defensive immutable values and complete cache/index keys;
- bounded state and deterministic eviction;
- no retained obsolete revisions;
- kernel-thread confinement, no concurrency and no background mutation;
- atomic publication and exception-safe updates;
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

### 15.1 Gate to start G7A

G7A may start only with:

- G6 and G6R still reproducible;
- this package unchanged or explicitly superseded;
- a clean authorized execution scope;
- explicit author authorization.

Otherwise:

```text
G7A = BLOCKED PENDING AUTHOR AUTHORIZATION OR G6 BASELINE
```

### 15.2 Gate to close G7A

G7A cannot pass until evidence:

- compares all three index strategies;
- resolves metric tolerances/error/improper-limit policy;
- resolves rich Geo lifecycle and scalar participation;
- audits every upstream impact listed above;
- validates route, total, repeated and nested cases;
- proposes the exact G7B editable set and budgets;
- receives explicit author approval.

At closeout the metric spec may become normative and ADR 0007 may become
Accepted only through explicit author decisions. This planning task does
neither.

### 15.3 Gate to start G7B

G7B requires all of:

- `G7A = PASS — AUTHOR APPROVED`;
- `locus-v2-metrics.md` approved as normative;
- ADR 0007 Accepted or explicitly replaced;
- approved scalar, lifecycle, index, tolerance, error and improper-limit
  decisions;
- separate explicit G7B authorization.

Otherwise:

```text
G7B = BLOCKED PENDING AUTHOR REVIEW
```

### 15.4 Gate to close G7B

The minimum candidate must pass the full matrix, functional benchmark gates,
cache ON/OFF equality, lifecycle and exception tests, focused Locus V2
verification, shared-kernel/Desktop build gates, traceability and author
review. Public command, XML, `Path`, 3D, G8 and G9 remain absent.

## 16. Current disposition

```text
G7 PLANNING RECOVERY = PASS

G7 PLANNING = RESTORED AND AUTHOR-REVIEWED

GEOLOCUSMETRICRESULT =
APPROVED AS G7A WORKING ARCHITECTURE

ADR 0007 = PROPOSED
G7 SPEC = PROPOSED / NOT NORMATIVE

G7A = NOT STARTED
G7B = NOT STARTED
G8 = NOT STARTED
```

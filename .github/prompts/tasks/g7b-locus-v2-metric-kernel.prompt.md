# Objective

Implement the author-approved minimum native Locus V2 metric kernel with a
rich normal-DAG `GeoLocusMetricResult`, route resolution, component metric
capabilities, aggregation and the author-approved form of the G7A/R1 bounded
index strategy and multi-consumer ownership.

This is the author-approved future execution prompt. **Do not execute it during
G7A closeout.** Its presence and G7B authorization do not themselves start
productive work; a task must explicitly execute this prompt.

# Mandatory entry gate

Before any implementation, require all of:

- G6 and G6R reproduce as PASS;
- `G7A = PASS — AUTHOR APPROVED`;
- `geocedg/specs/locus/locus-v2-metrics.md` is explicitly approved as
  normative;
- ADR 0007 is Accepted;
- the author has approved scalar participation, rich lifecycle, GeoClass,
  index ownership/capacity/eviction/key, integration capability, metric
  tolerance/error and improper-limit policies;
- the roadmap records `G7B = AUTHORIZED / NOT STARTED`; and
- the current task explicitly executes this G7B prompt.

If any item is absent:

```text
G7B = BLOCKED — APPROVED ENTRY BASELINE NOT REPRODUCED
```

The versioned closeout is the approval authority. Reconfirm it from disk rather
than inferring approval from conversation or an obsolete proposal.

# Author-approved measured G7A input

G7A has been reproducibly reexecuted from the versioned planning baseline and
the focused R1 refinement has been executed test-private. Their current status
is `PASS — AUTHOR APPROVED`. The raw evidence, reports,
exact candidate API and traceability matrix are:

- `geocedg/validation/locus-v2/g7a/g7a-characterization-evidence.json`;
- `geocedg/validation/locus-v2/g7a-r1/g7a-r1-characterization-evidence.json`;
- `docs/validation/g7a_locus_v2_metric_characterization_report.md`;
- `docs/validation/g7a_r1_locus_v2_metric_refinement_report.md`;
- `docs/developer/locus_v2_metric_api.md`;
- `docs/validation/g7a_locus_v2_metric_traceability_matrix.md`.

The author-approved decisions mandatory for execution are:

- explicit scalar adapter (alternative C), with no `NumberValue` interface on
  the rich Geo;
- append-only `GeoClass.LOCUS_METRIC_RESULT` after `LOCUS_V2`;
- closed immutable `MetricValue2D` finite/positive-infinity/absent variants,
  with no bare/sentinel value access;
- immutable typed `MetricErrorEvidence2D` with a closed
  `MetricErrorAmount2D` hierarchy, no NaN/negative/magic-zero/null unknown
  sentinels, and direct reuse of the productive G6
  `LocusSemanticMetadata2D.NumericGuarantee`;
- a rich Geo is defined when it owns a current immutable diagnostic snapshot;
  scalar admissibility is a separate predicate;
- per-call GeoCeDG-owned deterministic adaptive quadrature, with the inherited
  static Gauss helper used only as a comparison oracle;
- initial versioned `eps_metric_abs = 1e-10`, `eps_metric_rel = 1e-9`, plus
  work ceilings of 32768 evaluations, 16384 subdivisions and depth 22; these
  are implementation-policy defaults, not mathematical constants;
- bounded `LAZY_COMPONENT_REVISION`, current revision only, provisional
  capacity 64 per active locus shared owner and deterministic insertion-order
  eviction;
- `DEDICATED_SHARED_OWNER` with one compatible component-state build per
  complete key across metric results until eviction/invalidation;
- complete component key → immutable `LocusMetricComponentState2D` →
  route/extent-specific `LocusMetricContribution2D`; no routes,
  contributions, query results or aggregates are shared;
- structurally optional traversal outcome in rich results: mandatory for
  applicable between-position results and absent from total results;
- P1 coherent current-revision rich failure publication, no failed index entry,
  no stale success and an undefined scalar adapter;
- component-local cumulative arc coordinate as internal index data;
- the aggregate precedence, improper-limit policy and functional budgets stated
  below.

These are mandatory contracts, not implementation results. G7B is authorized
but remains not started until this prompt is explicitly executed.

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED
R1 OWNERSHIP = DEDICATED_SHARED_OWNER — AUTHOR APPROVED
G7B = AUTHORIZED / NOT STARTED
```

# Preflight

1. Read `AGENTS.md` and every authority below from disk.
2. Require a clean or explicitly accounted worktree.
3. Record branch, HEAD, `origin/main` and ancestry.
4. Confirm the roadmap still shows G6/G6R PASS, G7A author-approved, the G7
   spec normative, ADR 0007 Accepted and G7B authorized/not started.
5. Confirm current productive source still has no metric implementation.
6. Run `tools/agent/verify-operational.ps1` and
   `tools/agent/verify-locus-v2.ps1`.
7. Verify all G7A reference/evidence hashes.

If the baseline does not reproduce:

```text
G7B = BLOCKED — G6/G7A BASELINE NOT REPRODUCED
```

Classify environment, permission, stale-test and production failures before
editing source.

# Authority and evidence hierarchy

Apply in order:

1. `AGENTS.md`;
2. the roadmap and author-approved G7 plan;
3. normative G6 and G7 metric specifications;
4. Accepted ADR 0006 and Accepted/replacement metric-index ADR;
5. author-approved G7A report, traceability and raw evidence;
6. G6A/G6B/G6R reports and productive architecture/API;
7. actual productive Locus V2 source;
8. G7 validation matrix and benchmark plan;
9. CeDG scientific catalog and hash-pinned legacy evidence.

Prompts summarize execution boundaries; they do not override normative specs.
External conversations and inaccessible prior G7 work are not authority.

# Scope

Implement only the author-approved two-dimensional internal metric API, rich
GeoElement/DAG publication, route resolver, approved component metric
capabilities, aggregator, bounded metric index, focused tests/instrumentation
and opt-in developer laboratory. Make the smallest coherent source changes
listed by the G7A impact audit and preserve all unrelated behavior.

# Architectural placement

Metric semantics and the dynamic rich result belong in the shared Java kernel.
Prefer GeoCeDG-owned source packages and make only the smallest audited
upstream-owned edits needed for the new GeoElement classification and approved
numeric participation.

The G7B minimum is:

```text
internal Java API
+ GeoLocusMetricResult
+ developer laboratory
```

The required dependency shape is:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

Do not use `GeoNumeric` as the sole output or semantic authority. Do not create
a companion `GeoNumeric` that can diverge from the rich result.

# Explicitly forbidden scope

Do not:

- register `LocusLength` or any public command;
- change `Length`, `Perimeter` or `AlgoPerimeterLocus`;
- change legacy `GeoLocus`, `myPointList` or public `Locus[...]`;
- add public `Path`/point-on-locus behavior;
- register XML/factory persistence, migrations or serialization;
- add 3D behavior, G5 export changes, G8 intersections or G9 spatial semantics;
- implement `SHORTEST` unless a later explicit scope supersedes this prompt;
- implement `GEOMETRIC_IMAGE_UNION_LENGTH` or geometric deduplication;
- use render vertices, `LocusRenderCache2D`, legacy samples, viewport, zoom,
  DPI or pixel tolerance as metric inputs;
- add global caches, unbounded revision history, concurrency, background
  mutation, external numerical libraries, C++, DAG flattening or JMH without
  separately approved evidence;
- defer the required lifecycle, bounds, invalidation, exception or nested gates
  to a future G7R.

# Required design/specification

Implement exactly the normative metric spec, accepted metric-index decision,
author-approved G7A policy tables and G7 architecture. Do not silently widen
the supported provider/evaluator/query subset. The following sections are the
minimum design contract; any contradiction with actual source stops work for a
new author decision.

# Mathematical and multiplicity contract

Length is total variation:

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

The algorithm estimates or establishes this quantity; it does not define it.

Implement only `CONSTRUCTIVE_TRAVERSAL_LENGTH`. Each preimage retracing and
each distinct constructive branch contributes with multiplicity. Preserve
branch/domain provenance in every contribution.

# Geometric invariants and degeneracies

Preserve viewport/zoom/DPI independence, provider semantic parameter identity,
non-negative length, constructive multiplicity, one coherent revision,
component-bounded traversal, explicit periodic seams and index ON/OFF equality.
Handle self-intersections by preimage; multibranch and multicomponent topology
without fabricated connections; and cusp, collapsed, isolated, empty, gap,
stale binding, branch loss, topology change, unbounded, non-rectifiable and
unestablished-limit states with explicit rich outcomes. Never retain stale
geometry or substitute a sampled scalar.

# Query types

Implement semantically distinct immutable queries:

## `BetweenPositionsMetricQuery`

Contains explicit A/B bindings, `FORWARD`/`REVERSE`, same-position choice,
open-boundary policy and the author-approved metric policy.

Length is non-negative; direction selects a route.

## `TotalLocusMetricQuery`

Contains no A, B, direction, same-position or wrap convention. Do not encode
total as A=B, `FULL_CYCLE` or `WRAP_TO_START`.

For revision `r`:

\[
\mathcal L_{\mathrm{total}}(L_r)
=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Submit every valid-domain component exactly once. Submit exactly one
fundamental cycle for each periodic branch. Aggregate disconnected components
without a chord or fabricated route.

# Position and binding values

Implement the approved immutable split:

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

Revision is not automatically part of durable position identity. Component key
is revision-scoped. A component split does not silently change the semantic
position. Never repair by Cartesian proximity. A stale binding reports
`POSITION_STALE`; rebind is explicit and auditable.

# Route architecture

Implement:

```text
LocusMetricRouteResolver2D
LocusMetricRoute2D
LocusMetricRouteSegment2D
```

The resolver interprets A/B, revision, branch, valid components, direction,
periodic seam, ZERO/FULL, STOP/WRAP/STRICT, global boundaries, internal gaps
and target reachability. It does not integrate or aggregate.

The immutable route preserves:

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

Each segment belongs to exactly one valid component and has an oriented
parameter subinterval.

# Direction, equality and boundary policies

Implement `FORWARD` and `REVERSE`. `SHORTEST` is deferred.

Same semantic position is explicitly:

- `ZERO_LENGTH`; or
- `FULL_CYCLE` only for approved periodic semantics.

Do not infer either from Cartesian equality.

For `start --- B ------ A ----- end` and forward A→B:

- `STOP_AT_END` returns length A→end, `targetReached=false`,
  `STOPPED_AT_BOUNDARY` and incomplete coverage. It is not a complete A/B
  scalar.
- `WRAP_TO_START` returns length A→end + start→B, `wrapped=true`,
  `targetReached=true` and `geometricallyConnected=false`. It is an explicit
  metric convention, not closure/incidence/topology.
- `STRICT` returns absent with `TARGET_NOT_REACHABLE`.

No policy crosses an internal invalid-domain gap. Report
`DISCONTINUITY_ENCOUNTERED` and preserve the component-bounded decomposition.

# Component metric capabilities

Implement only the capabilities and exact policy approved from G7A:

1. analytic/closed form;
2. differential quadrature;
3. evaluator-only adaptive metric;
4. unsupported.

The characterized candidate is analytic segment/circle first; deterministic
per-call adaptive differential quadrature for derivative-capable
ellipse/parabola/transcendental cases; evaluator-only output only with an
explicitly defensible guarantee; otherwise `UNSUPPORTED`. The inherited
`AlgoIntegralDefinite.numericIntegration` helper is not the metric authority:
it is coupled to global formatting bootstrap, owns static mutable integration
state, uses a fixed kernel tolerance and returns only a bare value or `NaN`.

Each component/segment calculation returns an immutable contribution with:

- locus/revision/branch/component and interval provenance;
- one closed `MetricValue2D` finite/positive-infinity/absent value;
- coverage and rectifiability;
- construction fidelity;
- evaluator and metric/integration method;
- representation role;
- the productive G6 `LocusSemanticMetadata2D.NumericGuarantee` when applicable;
- typed absolute/relative error evidence, scope, method, assumptions and
  certificate metadata;
- functional counters and diagnostics.

Do not label a refined chord sum exact. Evaluator-only refinement agreement
cannot create `CERTIFIED_ERROR_BOUND`. Use `ESTIMATED_ERROR` only under the
approved explicit assumptions; otherwise
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

Do not create a G7 duplicate guarantee enum. Do not encode absent value or
unknown/not-applicable error using NaN, infinity in a finite field, `-1`, zero,
magic values or exceptions. Certified bounds, estimates, uncertified evidence,
exact arithmetic and not-applicable evidence remain distinct through
aggregation. An incomplete finite subtotal scopes its error to the reported
partial value, never the unknown complete total.

Use the approved independent:

```text
eps_metric_abs
eps_metric_rel
stopping policy
refinement limits
improper-limit policy
aggregate error policy
```

Use the author-approved initial versioned policy:

```text
eps_metric_abs = 1e-10 construction length unit
eps_metric_rel = 1e-9
threshold = max(eps_metric_abs, eps_metric_rel * S)
S = max(abs(current length estimate), endpoint chord, explicit provider scale)
maximum adaptive depth = 22
maximum metric evaluations = 32768
maximum metric subdivisions = 16384
```

Allocate absolute budgets deterministically by route segment/component and sum
them in the aggregate. Report relative error only for a finite nonzero value.
Evaluation, subdivision and depth guards are independent, deterministic and
checked before new work. Reaching any ceiling is `LIMIT_NOT_ESTABLISHED`, never
a successful estimate. An evaluator/numeric exception remains
`NUMERICAL_FAILURE`; wall-clock timeout is never metric authority.

Do not reuse `eps_domain`, G6 evaluation tolerance, render pixel tolerance or
future G8 root tolerance.

# Unbounded and improper semantics

Implement the G7A-approved support/status mapping for:

- finite A/B on an unbounded branch;
- finite improper total;
- positive infinity;
- non-rectifiable;
- unsupported;
- limit not established.

Keep `POSITIVE_INFINITY`, `NON_RECTIFIABLE`, `UNSUPPORTED`,
`NUMERICAL_FAILURE` and `LIMIT_NOT_ESTABLISHED` distinct. Never use viewport
cutoff. Never use `DIVERGENT` as a generic failure.

# Metric index

Implement only the index strategy accepted after G7A. Preserve the no-reuse
path as a test oracle and cache/index-off mode.

The accepted strategy is bounded `LAZY_COMPONENT_REVISION`. On the G7A
fixture, 100 same-component A/B queries required 1 lazy build versus 3 eager
whole-revision builds and 100 no-reuse builds; 100 repeated totals over three
components required 3 lazy/eager builds versus 300 no-reuse builds. Use current
revision only, provisional capacity 64 entries
per active locus shared owner, deterministic insertion-order eviction and an
approximate retained-byte counter. Capacity is not multiplied by metric
algorithms and 64 is not normative. Measure the real entry footprint before
treating it as a stable contract.

R1 compared `ALGO_LOCAL_INDEX`, `LOCUS_ATTACHED_SHARED_INDEX`,
`CONSTRUCTION_SCOPED_METRIC_REPOSITORY` and
`DEDICATED_SHARED_OWNER`. The accepted ownership is a GeoCeDG-owned
non-GeoElement `LocusMetricSharedOwner2D` tied to
one active source locus in one Construction. Each metric Algo remains an
ordinary direct DAG dependent, holds only a consumer lease and releases it on
removal. Revision/topology/undefined/locus-removal transitions synchronously
invalidate or release entries. The final consumer releases the owner. Copy/set
never carry it; undo/redo reacquires through normal DAG reconstruction.

The owner stores no Algo, Geo result, dependency edge or callback; resolves no
route; aggregates no query; and never becomes metric authority. It stores and
shares only immutable `LocusMetricComponentState2D`, never routes,
contributions, query results or aggregate results. Do not replace
it with a static/global registry, direct semantic-Geo index state, hidden DAG
or Construction-wide repository unless a new explicit author decision replaces
ADR 0007.

The reusable boundary is mandatory:

```text
complete component key
    -> get/build immutable LocusMetricComponentState2D

LocusMetricComponentState2D + LocusMetricRouteSegment2D
    -> route/component metric evaluation
        -> LocusMetricContribution2D
```

The key has no A/B endpoints. A total query derives a contribution over the
complete valid-component extent; a between query derives one contribution per
route segment. One state may produce many contributions.

The key contains every approved field and at least:

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

From the first candidate require:

- bounded state;
- deterministic eviction;
- no retained obsolete revision;
- defensive immutable keys/component-state entries;
- complete keys;
- kernel-thread confinement;
- no concurrency or background mutation;
- atomic publication;
- exception safety;
- `finally` cleanup;
- no partially valid published entry;
- index ON/OFF semantic equality;
- counters for builds, hits, misses, evictions and retained entries.
- one compatible component-state build per complete key across distinct metric
  results until eviction/invalidation;
- cross-result hits and duplicate-compatible-build counters;
- unique/duplicate payload, metadata, owner and consumer retained-byte
  accounting;
- deterministic consumer/source removal and zero cross-locus or
  cross-Construction sharing.

No static/global cache. Component keys are revision-scoped and never durable
position identity.

# Aggregator

Implement `LocusMetricAggregator2D` separately from integration and route
resolution. It:

- combines contributions with constructive multiplicity;
- aggregates finite/positive-infinite/absent values under the approved rules;
- aggregates error;
- propagates weakest guarantee;
- sets coverage/status/rectifiability;
- preserves deterministic branch/component decomposition and gap diagnostics.

Empty domain, isolated point and collapsed image are finite zero with complete
coverage and diagnostics. Zero is not absence or failure.

Unless author review changes it, enforce the characterized aggregate order:

1. stable branch/component ordering before compensated finite summation;
2. an established positive-infinite contribution fixes the known value kind to
   `POSITIVE_INFINITY`;
3. any unresolved contribution makes coverage `INCOMPLETE`, including the
   infinite-plus-unsupported case;
4. status precedence is `INVALID_QUERY` (query-level) > `NUMERICAL_FAILURE` >
   `LIMIT_NOT_ESTABLISHED` > `UNSUPPORTED` > `SUCCESS`;
5. `NON_RECTIFIABLE` dominates rectifiability, then `UNDETERMINED`;
6. propagate the weakest guarantee and sum finite absolute errors;
7. retain every ordered contribution and diagnostic.

Thus infinite plus unsupported remains a known positive-infinite rich value
with incomplete coverage and unsupported status; it is not scalar-admissible.

# Rich result

Implement defensive immutable `LocusMetricResult2D` with orthogonal:

```text
MetricValue2D
    FiniteMetricValue2D(non-negative finite value)
    PositiveInfinityMetricValue2D
    AbsentMetricValue2D

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

Expose traversal outcome as `Optional<TraversalOutcome>` or an equivalent
between/total result-type separation. Applicable between-position results must
contain an outcome. Total results contain none. Do not use `null`, a sentinel
or an artificial `NOT_APPLICABLE` value.

Only the finite variant exposes `OptionalDouble`; neither result nor
contribution exposes a bare value for absent/infinite states. `MetricValueKind`
is at most a discriminator, not a second payload.

Also preserve construction fidelity, evaluator method, metric method,
representation role, direct G6 numeric guarantee, typed absolute/relative
error evidence and scope, units, provenance, diagnostics and contribution
decomposition. Implement error amount as a closed hierarchy equivalent to:

```text
sealed MetricErrorAmount2D
    EstablishedMetricErrorAmount2D(non-negative finite amount)
    NotEstablishedMetricErrorAmount2D
    NotApplicableMetricErrorAmount2D
```

Do not pair an independently variable state with `OptionalDouble`; do not use
NaN, `-1`, magic zero or `null`.

# GeoElement, GeoClass and normal DAG

Implement the exact append-only classification approved after G7A, expected to
be equivalent to `GeoClass.LOCUS_METRIC_RESULT`. Do not reuse `NUMERIC`,
`LOCUS` or `LOCUS_V2`. Preserve every existing ordinal and update all affected
switches/tests deliberately.

`AlgoLocusMetricV2` registers all dependencies through normal
`setInputOutput()` conventions. One compute:

1. captures a coherent input/revision/policy snapshot;
2. validates bindings/query;
3. resolves route or enumerates total components;
4. obtains component states and derives route/extent contributions;
5. aggregates one complete immutable candidate;
6. atomically publishes success or one coherent P1 failure snapshot to
   `GeoLocusMetricResult`;
7. cleans scoped work in `finally`.

At revision `r+1`, first make revision `r` non-current. Build component state
privately. Success publishes an immutable index entry and coherent rich result.
A handled failure publishes no index entry and publishes a coherent `Absent`
rich failure snapshot for `r+1`; its scalar adapter is undefined. Never retain
an old successful value as current after a failed update, publish a status from
one revision with a value from another, or expose a partially built entry.

# Scalar admissibility

Implement exactly the author-approved A/B/C choice.

- A: no generic numeric facade;
- B: conditional read-only numeric facet;
- C: explicit numeric adapter.

G7A measured 219 `instanceof NumberValue` sites in 72 shared-source files and
found no per-state admissibility hook in the static numeric interfaces.
Alternative B would therefore expose inadmissible rich states to generic
algorithms and CAS. The recommendation awaiting approval is C: keep
`GeoLocusMetricResult` non-numeric and provide only an explicit derived
`AlgoLocusMetricScalarAdapter` whose output is undefined unless the current
rich snapshot passes the scalar gate. Do not implement B merely because the
adapter is deferred or inconvenient.

A companion `GeoNumeric` is never authority.

The scalar gate must reject by default:

- STOP partial;
- target not reachable;
- incomplete coverage;
- stale/different branch/discontinuity;
- unsupported or numerical failure;
- limit not established;
- absent;
- positive infinity unless explicitly approved.

Finite successful semantically satisfied current results with suitable
coverage, valid zero and explicitly valid wrap may be admitted under the exact
approved policy. Candidate admissible G6 guarantees are
`EXACT_ARITHMETIC`, `CERTIFIED_ERROR_BOUND` and `ESTIMATED_ERROR`;
`FLOATING_POINT_UNCERTIFIED` remains rich-only by default. Rich-defined and
scalar-admissible remain separate.

# Complete lifecycle

Implement and test:

- creation and coherent initial publication;
- defined/undefined and scalar-admissible states;
- recovery after undefined;
- normal invalidation;
- revision/topology changes and branch disappearance;
- copy/copyInternal/set with approved semantics;
- remove and reference release;
- undo/redo;
- labels, Algebra View, lists/sequences and selection;
- defaults/styles;
- numeric-interface behavior;
- GeoClass switch impact;
- factory/XML deliberate absence;
- 2D-only and explicit no-3D behavior;
- packaging.

A copy cannot inherit stale index, foreign Construction state, old revision,
stale binding or partial result as current. Unsupported copy/set/list behavior
must be deterministic and documented, not accidental.

The characterized lifecycle starts unpublished/undefined, atomically publishes
one complete immutable snapshot, clears current state on invalidation/removal
and recovers only through normal-DAG recomputation. Published diagnostic
snapshots (`ABSENT`, `UNSUPPORTED`, numerical failure or limit not established)
remain rich-defined but scalar-inadmissible. `copy`, `copyInternal` and `set`
must clear revision-bound current state and trigger safe recomputation; reject
list/sequence value copying in the minimum if that cannot be guaranteed.

No lifecycle/safety item is deferred to G7R.

# Reparameterization and arc coordinate

Pass length invariance cases for `t=u^3` and the regular
orientation-preserving:

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1}.
\]

Any derived
`s(t)=\operatorname{Var}(F;[t_0,t])` is per valid component only. It cannot
replace provider semantic parameter, connect a gap/branch or become an
automatic public surface.

# Required tests and commands

Implement focused value, algorithm, lifecycle, integration and architecture
tests for every applicable G7 matrix row:

- segment, circle, ellipse, parabola, transcendental;
- scale/translation and both reparameterizations;
- self-X, multibranch, multicomponent, periodic and cusp;
- collapsed, isolated, empty, stale and topology change;
- STOP, WRAP, STRICT, gap, reverse, endpoints/limits;
- total one/many components/branches, periodic, repeated, invalidated,
  infinite/mixed;
- rich axes and scalar admissibility;
- exception/aliasing/copy/remove/undo/redo;
- index capacity/eviction/key/invalidation/on-off equality;
- repeated and three-level nested composition;
- no public/XML/Path/3D/G5/G8 boundary regressions.

Use analytic/high-precision/independent references approved by G7A. Java tests
must not call Python at runtime.

# Repeated-query hard gates

Run 1, 10 and 100 traces for:

- same A/B;
- overlapping arcs;
- reverse;
- periodic;
- STOP;
- WRAP;
- STRICT;
- repeated total;
- tolerance/policy change.

Record evaluator, derivative and integrator calls; subdivisions; component and
index builds; hits/misses; evictions; retained entries; latency and
invalidation. Enforce the author-approved functional budgets. Wall-clock stays
informational unless G7A explicitly approved hard thresholds.

Candidate budgets measured by G7A, to become gates only after author approval:

- 100 same-component A/B queries: exactly one lazy component build and 99 hits;
- 100 repeated totals over three components: exactly three lazy component
  builds, with subsequent contribution hits;
- same/overlapping/reverse/periodic/STOP/WRAP on one current component: reuse
  one complete-key entry;
- STRICT unreachable: reject before integration;
- any tolerance, algorithm, multiplicity or improper-policy change: cache miss;
- any evaluator capability or deterministic work-budget change: cache miss;
- no retained obsolete revision; retained entries never exceed capacity;
- injected build failure publishes no entry and leaves no build active.

Also run 1, 3, 10 and 100 distinct compatible metric consumers over the same
locus/revision/component/full key. The hard budget is one component-state build
and N-1 cross-result hits until eviction or invalidation. At N=100,
`component-state builds = 1` and
`duplicate compatible component builds = 0`. A local comparator must report
its N builds as `CROSS_RESULT_DUPLICATE_METRIC_WORK`; owner-local hits cannot
hide cross-result duplication.

Run local-first and total-first orders over three components; both must build
exactly three unique components and return equal rich results. Prove zero
sharing for different loci in one Construction and look-alike locus IDs in
different Constructions. Prove first-consumer-one-build/later-consumer-hit after
revision/topology invalidation and deterministic release after consumer/locus
removal.

# Nested metric hard gates

Validate:

```text
L1
 -> metric(L1)
 -> L2
 -> metric(L2)
 -> L3
```

and repeated upstream total consumption.

Extend it with the approved R1 multi-consumer fixture:

```text
L1 -> M1a, M1b, M1total -> L2 -> M2a, M2total -> L3
```

Hard requirements:

- zero render-cache/vertex access;
- zero legacy-sample access;
- no whole-locus regeneration per downstream point;
- no metric-index build per point;
- same revision/policy reuse within bounded ownership;
- normal DAG invalidation/recovery;
- index-off equality;
- zero duplicate compatible component builds across metric outputs while an
  entry is current and retained;
- explicit distinction between necessary and wasteful recompute.

The G7A three-level fixture measured zero render reads, zero legacy-sample
reads, zero whole-locus regeneration, zero index builds per downstream point,
one cold build per metric level/revision/policy and maximum one active metric
computation. Treat those functional values as hard gates; keep wall-clock
informational.

# Compatibility and serialization

Classic and public GeoCeDG `Locus[...]` remain legacy. Preserve existing
`GeoClass` ordinals and behavior, old `.ggb` files, `Length`/`Perimeter`,
`Path`, G5 export, Classic, Desktop packaging and G6/G6R evidence. G7B adds no
XML/factory persistence, migration, public command, public point-on-locus,
3D dispatch, G8 intersection or G9 behavior. Locus V2 and its metric surface
remain experimental/internal and disabled by default outside the explicit
laboratory.

# Developer laboratory

Extend or add only the author-approved opt-in developer laboratory surface. It
may display:

- query kind and route;
- every rich result axis;
- contribution/gap decomposition;
- scalar-admissible versus rich-defined state;
- strategy/capacity and functional counters.

It must remain disabled by default, nonpersistent and separate from normal
GeoCeDG/Classic workflows. It cannot become semantic authority or a public
command.

# Legacy and scientific evidence

Do not modify originals:

- `InterCilConoObliqueTwoLevels.ggb`;
- `InterCilConoOblique.ggb`;
- `Templatev7.ggb`.

Preserve `AlgoPerimeterLocus` and `listLength`/`listLength12`/`postLocus` as
legacy sampled evidence only. No full migration is required.

Validate the approved pilots: segment/circle, small deterministic cylinder
development, traced nested metric fixture and bounded oblique-cone subset if
G7A proved it reproducible.

# Exact characterized source impact

Prefer new files under `org.geocedg.common.kernel.locus.metric`, plus
`org.geocedg.common.kernel.geos.GeoLocusMetricResult`,
`org.geocedg.common.kernel.algos.AlgoLocusMetricV2` and
`AlgoLocusMetricScalarAdapter`. New GeoCeDG-owned metric files include the
closed value/error/work-budget contracts, index key/index,
`LocusMetricSharedOwner2D`, `LocusMetricComponentState2D`, its builder and
route/extent evaluator, contributions/result, route/integrator/aggregator and
instrumentation values.

The narrow existing-file surface characterized by G7A/R1 is limited to:

- appending `LOCUS_METRIC_RESULT` in `GeoClass.java`;
- adding an internal `GeoLocusV2` acquire/invalidate/release seam for the
  dedicated derived owner; clear on revision publication, undefined and
  `doRemove()`; copies never inherit it;
- registering normal input/output and consumer-lease release in
  `AlgoLocusMetricV2`;
- updating `LocusV2KernelIntegrationTest` so it no longer assumes `LOCUS_V2` is
  the final enum;
- adding the new class to `DrawablesTest` as explicitly non-drawable;
- making a deliberate `ConstructionDefaults` style decision if the Geo enters
  default styling;
- auditing the already-null `EuclidianDraw` default without introducing a cast.

No base `GeoElement`, `Construction` repository, `NumberValue`, command
dispatcher, XML/factory, Path, legacy
metric, 3D or G5 export edit was justified by G7A. Any expansion of this surface
requires a new impact explanation and, where semantic, author review.

# Required artifacts

Synchronize:

- roadmap G7 status and plan;
- semantic model and architecture;
- normative metric spec without silently widening it;
- Accepted/replacement ADR;
- validation matrix and benchmark plan;
- internal developer API;
- user guide, accurately labeling internal/experimental availability;
- a G7B implementation report and requirement-to-test/evidence traceability;
- preservation of the G7A-R1 report/raw evidence and explicit disposition of
  every R1-1 through R1-22 recommendation;
- versioned functional-counter and reference manifests.

Document:

- public/internal and 2D/3D boundaries;
- exact classes/packages and dependencies;
- lifecycle and scalar policy;
- tolerance/error/improper policy;
- index key/owner/capacity/eviction;
- numeric guarantees and unsupported cases;
- laboratory use;
- compatibility and packaging effect.

Do not claim public availability, persistence or stable maturity.

## Validation commands

Run the narrow focused tests first, then:

- internal links and planning/spec/API/report consistency;
- `tools/agent/verify-g7a-metrics.ps1` and all approved reference hashes;
- all approved G7 metric test suites and functional-counter gates;
- index ON/OFF and nested evidence comparison;
- `tools/agent/verify-operational.ps1`;
- `tools/agent/verify-locus-v2.ps1`;
- shared-kernel and Desktop gates required by the repository wrapper;
- `git diff --check`;
- source/package boundary and forbidden-import checks;
- proof that legacy files and G5/G6 expected outputs remain unchanged;
- proof that no command/XML/Path/3D/G8/G9 work was introduced.

Save exact commands, exit codes, log paths, environment and evidence hashes.
Do not claim PASS for an uncompleted gate.

# Stop conditions

Stop and report rather than weaken the contract if:

- any entry gate is missing;
- any measured G7A recommendation used by the implementation lacks an explicit
  author disposition;
- G6/G7A baseline does not reproduce;
- implementation needs render/sample/viewport authority;
- result axes must be collapsed to fit `GeoNumeric`;
- scalar admissibility cannot be enforced as approved;
- a complete deterministic index key cannot be formed;
- bounded ownership/eviction/invalidation cannot be implemented;
- compatible results require one duplicate component build per metric output
  despite an approved safe shared owner;
- ownership introduces static/global state, cross-Construction sharing, a
  hidden DAG or retained removed locus/Construction state;
- cache/index ON/OFF results differ;
- exceptions can expose partial entries or stale values;
- nested hard gates require per-point whole-locus/index work;
- reference results disagree outside approved error;
- a requested behavior needs public command, persistence, Path, 3D or G8;
- actual source contradicts an approved policy without a new author decision.

# Completion

G7B can report candidate PASS only when all implementation, hardening,
documentation, traceability, focused/full verification and author-review gates
are complete. Its completion must still state:

```text
G7B PUBLIC COMMAND = ABSENT
G7B XML/PERSISTENCE = ABSENT
G7B PUBLIC PATH = ABSENT
G7B 3D = ABSENT
G8 = NOT STARTED
G9 = NOT STARTED
```

Do not start G8 or a follow-on hardening phase from this prompt.

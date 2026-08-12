# Objective

Implement the author-approved minimum native Locus V2 metric kernel with a
rich normal-DAG `GeoLocusMetricResult`, route resolution, component metric
capabilities, aggregation and the G7A-selected bounded index strategy.

This is a reconstructed future execution prompt. **Do not execute it now.**
Its presence does not start G7B or authorize source edits.

# Mandatory entry gate

Before any implementation, require all of:

- G6 and G6R reproduce as PASS;
- `G7A = PASS — AUTHOR APPROVED`;
- `geocedg/specs/locus/locus-v2-metrics.md` is explicitly approved as
  normative;
- ADR 0007 is Accepted or explicitly superseded by another Accepted decision;
- the author has approved scalar participation, rich lifecycle, GeoClass,
  index ownership/capacity/eviction/key, integration capability, metric
  tolerance/error and improper-limit policies;
- a separate explicit task authorizes G7B.

If any item is absent:

```text
G7B = BLOCKED PENDING AUTHOR REVIEW
```

Do not infer approval from this prompt, planning recovery, completed
characterization or a Proposed document.

# Preflight

1. Read `AGENTS.md` and every authority below from disk.
2. Require a clean or explicitly accounted worktree.
3. Record branch, HEAD, `origin/main` and ancestry.
4. Confirm the roadmap still shows G6/G6R PASS, G7A author-approved and G7B not
   started.
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

Each component/segment calculation returns an immutable contribution with:

- locus/revision/branch/component and interval provenance;
- non-negative value kind/value;
- coverage and rectifiability;
- construction fidelity;
- evaluator and metric/integration method;
- representation role;
- numeric guarantee;
- absolute/relative error;
- functional counters and diagnostics.

Do not label a refined chord sum exact. Evaluator-only refinement agreement
cannot create `CERTIFIED_ERROR_BOUND`. Use `ESTIMATED_ERROR` only under the
approved explicit assumptions; otherwise
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

Use the approved independent:

```text
eps_metric_abs
eps_metric_rel
stopping policy
refinement limits
improper-limit policy
aggregate error policy
```

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
```

From the first candidate require:

- bounded state;
- deterministic eviction;
- no retained obsolete revision;
- defensive immutable keys/entries;
- complete keys;
- kernel-thread confinement;
- no concurrency or background mutation;
- atomic publication;
- exception safety;
- `finally` cleanup;
- no partially valid published entry;
- index ON/OFF semantic equality;
- counters for builds, hits, misses, evictions and retained entries.

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

# Rich result

Implement defensive immutable `LocusMetricResult2D` with orthogonal:

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

Also preserve construction fidelity, evaluator method, metric method,
representation role, numeric guarantee, absolute/relative error, units,
provenance, diagnostics and contribution decomposition.

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
4. obtains component contributions;
5. aggregates one complete immutable candidate;
6. atomically publishes it to `GeoLocusMetricResult`;
7. cleans scoped work in `finally`.

Never retain an old successful value as current after a failed update. Never
publish a status from one revision with a value from another.

# Scalar admissibility

Implement exactly the author-approved A/B/C choice.

- A: no generic numeric facade;
- B: conditional read-only numeric facet;
- C: explicit numeric adapter.

Working preference B has no force unless G7A proved it safe across
`GeoElement.isDefined()`, `NumberValue`/`evaluateDouble()`, Algebra View,
generic algorithms and CAS.

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
approved policy. Rich-defined and scalar-admissible remain separate.

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

Hard requirements:

- zero render-cache/vertex access;
- zero legacy-sample access;
- no whole-locus regeneration per downstream point;
- no metric-index build per point;
- same revision/policy reuse within bounded ownership;
- normal DAG invalidation/recovery;
- index-off equality;
- explicit distinction between necessary and wasteful recompute.

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
- G6/G7A baseline does not reproduce;
- implementation needs render/sample/viewport authority;
- result axes must be collapsed to fit `GeoNumeric`;
- scalar admissibility cannot be enforced as approved;
- a complete deterministic index key cannot be formed;
- bounded ownership/eviction/invalidation cannot be implemented;
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

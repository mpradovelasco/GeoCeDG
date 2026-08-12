# Objective

Characterize native Locus V2 metrics and produce an author-decision package
without implementing the productive G7 metric kernel.

This is the reconstructed future execution prompt for G7A. **Do not execute it
without separate explicit author authorization.** Its presence does not start
G7A, G7B or G8.

# Entry gate and preflight

Before any edit:

1. read repository `AGENTS.md` and all authorities below from disk;
2. require a clean or explicitly accounted worktree;
3. record branch, HEAD SHA, `origin/main` SHA and ancestry;
4. confirm the roadmap states G6 `PASS`, G6R `PASS` and G7
   `PENDING / NOT STARTED`;
5. confirm ADR 0006 is Accepted and `locus-v2-semantics.md` is normative;
6. confirm Locus V2 remains experimental/internal and disabled by default;
7. confirm no productive V2 metric entity exists;
8. run `tools/agent/verify-operational.ps1` and
   `tools/agent/verify-locus-v2.ps1`.

If G6/G6R does not reproduce, stop:

```text
G7A = BLOCKED — G6 BASELINE NOT REPRODUCED
```

Do not patch production code to compensate for environment, permission,
toolchain or external-runtime failures.

# Authority and evidence hierarchy

Apply in order:

1. `AGENTS.md`;
2. `docs/roadmap/geocedg_roadmap.md`;
3. `geocedg/specs/locus/locus-v2-semantics.md`;
4. Accepted ADR 0006;
5. G6A/G6B/G6R reports;
6. `docs/architecture/locus_v2_implementation.md`;
7. `docs/developer/locus_v2_api.md`;
8. G6 traceability and versioned evidence;
9. actual productive Locus V2 source;
10. CeDG scientific catalog;
11. hash-pinned legacy models.

Then use the author-reviewed G7 package:

- `docs/roadmap/g7_locus_v2_metrics_plan.md`;
- `docs/architecture/locus_v2_metric_semantic_model.md`;
- `docs/architecture/locus_v2_metric_architecture.md`;
- `geocedg/specs/locus/locus-v2-metrics.md`;
- Proposed ADR 0007;
- G7 validation matrix and benchmark plan.

External conversations and inaccessible/lost G7 work are not authority. Do not
attempt commit/hash/file recovery outside Git.

# Scope

G7A is planning validation and characterization. It must:

- audit the mathematical/query/route/result model against actual G6 source;
- audit the candidate `GeoLocusMetricResult` lifecycle and append-only
  `GeoClass` impact;
- compare numeric participation A/B/C using real GeoGebra call paths;
- characterize integration/error/tolerance/improper-limit capabilities;
- compare all three metric-index strategies with functional counters;
- exercise repeated and nested composition;
- use independent references and controlled legacy/scientific evidence;
- propose exact G7B APIs, source/test edit set, policies and hard budgets;
- produce a traceable author-review package.

Read-only or test-private characterization probes, fixtures and scripts are
allowed only when required for saved evidence. They must not be callable as a
productive metric API.

# Explicitly forbidden scope

Do not:

- add productive metric classes under `src/main`;
- create `GeoLocusMetricResult`, `AlgoLocusMetricV2` or a productive index;
- implement `LocusLength` or change `Length`/`Perimeter`;
- change legacy `GeoLocus`, `AlgoPerimeterLocus`, `myPointList` or public
  `Locus[...]`;
- read render vertices, `LocusRenderCache2D`, legacy samples, viewport, zoom,
  DPI or pixel tolerance as metric authority;
- add a public `Path`, XML/factory registration, persistence or migration;
- add 3D behavior, G5 export changes, G8 intersections or G9 semantics;
- add generic numeric libraries, concurrency, background mutation, C++, JMH or
  DAG flattening without a separately approved need;
- accept ADR 0007 or make the metric spec normative before explicit author
  review;
- start G7B.

# Architectural placement

Metric truth is a future shared-Java-kernel responsibility because it changes
semantic meaning and must participate in the normal dependency graph. G7A
places only characterization tests/probes in test source, independent
references under `geocedg/validation/` and reports under `docs/validation/`.
No GUI, render cache, script, Python result or generated artifact becomes
geometric authority.

# Required design/specification

Characterize and propose evidence-backed revisions to the author-reviewed G7
plan, metric semantic model, metric architecture, proposed metric spec,
Proposed ADR 0007, validation matrix and benchmark plan. Preserve the normative
G6 contract. The following sections state the required geometric, query,
result, lifecycle, numeric and index design questions.

# Mathematical characterization

Use total variation as authority:

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

The numerical method does not define length. Characterize only
`CONSTRUCTIVE_TRAVERSAL_LENGTH`: retracing and distinct constructive branches
retain multiplicity. `GEOMETRIC_IMAGE_UNION_LENGTH` is out of scope.

# Geometric invariants and degeneracies

Preserve zoom/viewport/DPI independence, provider semantic parameter identity,
constructive multiplicity, non-negative length, revision coherence, explicit
periodic seams, component-bounded traversal and index ON/OFF equality.
Characterize self-intersection preimages, multibranch/multicomponent domains,
periodicity, cusps, collapsed/isolated/empty domains, internal gaps, stale
positions, branch disappearance, topology change, unbounded domains,
non-rectifiability and limits that cannot be established. Every degeneration
must produce explicit rich state rather than stale geometry or a guessed
scalar.

# Query and route audit

Audit two distinct operations:

- `BetweenPositionsMetricQuery`;
- `TotalLocusMetricQuery`.

Total has no A/B or direction and is never represented as A=B,
`FULL_CYCLE` or `WRAP_TO_START`. It sums each valid-domain component exactly
once and one fundamental cycle per periodic branch.

Audit the position/binding split:

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

Show how revision-scoped component identity is derived from the actual G6
domain model. No coordinate-proximity repair is allowed. Stale bindings report
`POSITION_STALE` and only explicit rebind can recover.

Characterize the candidate:

```text
LocusMetricRouteResolver2D
LocusMetricRoute2D
LocusMetricRouteSegment2D
```

The resolver interprets position/revision/branch/component, direction,
periodic seam, ZERO/FULL, STOP/WRAP/STRICT, global boundaries, gaps and
reachability; it never integrates. Every segment belongs to one valid
component.

Required direction/same-position behavior:

- `FORWARD` and `REVERSE`;
- non-negative values;
- explicit `ZERO_LENGTH` versus `FULL_CYCLE`;
- no Cartesian-equality inference;
- `SHORTEST` may be analyzed but remains deferred/non-default.

Required open behavior for `start --- B ------ A ----- end` and forward A→B:

- STOP: length A→end, incomplete, not reached,
  `STOPPED_AT_BOUNDARY` and scalar-inadmissible;
- WRAP: length A→end plus start→B, wrapped/reached,
  `geometricallyConnected=false`;
- STRICT: absent, `TARGET_NOT_REACHABLE`;
- no policy crosses an internal invalid-domain gap.

# Rich result and aggregation audit

Preserve orthogonal:

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

Also preserve construction fidelity, evaluator and integration methods,
representation role, numeric guarantee, absolute/relative error, units,
provenance, diagnostics and contribution decomposition. Do not use
`DIVERGENT` as a numerical-failure bucket.

Characterize `LocusMetricAggregator2D` independently from component
integration. Settle mixed finite/infinite/nonrectifiable/unsupported/
limit-not-established aggregation and weakest-guarantee/error propagation.
Empty, isolated and collapsed cases are finite zero with complete coverage and
diagnostics.

# GeoLocusMetricResult and upstream audit

The working candidate is:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

Do not use `GeoNumeric` as sole output or authority.

Audit a new append-only classification equivalent to
`GeoClass.LOCUS_METRIC_RESULT`. Do not reuse `NUMERIC`, `LOCUS` or `LOCUS_V2`.
Map exact impact in:

- `GeoClass` and every enum/switch/exhaustive test;
- `GeoElement`, `isDefined()` and `evaluateDouble()`;
- `NumberValue` and related numeric interfaces;
- Algebra View, generic numeric algorithms and CAS;
- `AlgoElement` and dependency/update order;
- `ConstructionDefaults`, labels, selection, styles;
- creation/publication/undefined/recovery/invalidation;
- copy/copyInternal/set/remove/undo/redo;
- topology changes and branch disappearance;
- lists/sequences;
- commands and legacy metrics;
- `Path`;
- XML/factory;
- 2D/3D dispatch;
- G5 export and packaging.

A copy cannot inherit a stale index, foreign Construction state, old semantic
revision/binding or partial result as current. If safe copy/set semantics are
not possible, propose explicit unsupported behavior rather than guessing.

# Scalar-admissibility experiment

Compare in real source/tests:

- **A:** no generic numeric facade;
- **B:** conditional read-only numeric facet;
- **C:** explicit numeric adapter.

Working preference B applies only if the real API can prevent scalar
consumption for an inadmissible instance while preserving its rich defined
state. Trace `instanceof NumberValue`, `evaluateDouble()`, Algebra View,
generic algorithms and CAS. If static interface participation leaks an
inadmissible scalar, reject B.

Candidate admissible states are finite, successful, semantically satisfied,
suitably covered, current, fully supported, valid zero or explicitly requested
valid wrap.

Inadmissible by default: STOP partial, unreachable, incomplete, stale,
different branch, discontinuity, unsupported, numerical failure,
limit-not-established, absent and positive infinity unless explicitly decided.

Rich-result defined state and scalar-admissible state must remain distinct.

# Integration, tolerance and reference experiments

Compare:

1. analytic/closed form;
2. differential quadrature;
3. evaluator-only adaptive metric;
4. unsupported.

Evaluator-only refinement agreement never creates a certified bound.
`ESTIMATED_ERROR` requires explicit assumptions; otherwise use
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`.

Measure and propose:

```text
eps_metric_abs
eps_metric_rel
stopping policy
refinement limits
improper-limit policy
aggregate error policy
```

Do not reuse G6 evaluation tolerance, `eps_domain`, render tolerance or future
G8 root tolerance.

Characterize finite A/B on an unbounded branch, finite improper total, positive
infinity, non-rectifiable, unsupported and limit-not-established. Never use a
viewport cutoff.

Demonstrate reparameterization invariance for `t=u^3` and for:

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1},\qquad \phi'(u)>0.
\]

Characterize per-component
`s(t)=\operatorname{Var}(F;[t_0,t])` only as derived index data. It does not
replace semantic parameter identity or connect gaps/branches.

Use analytic, high-precision or independent numerical references. If Python
generates constants, version formula, precision, runtime/library, script and
output hash. Python is not kernel authority.

# Metric-index experiment

Implement only test-private comparable strategies:

```text
REFERENCE_NO_INDEX_REUSE
EAGER_WHOLE_REVISION
LAZY_COMPONENT_REVISION
```

The working hypothesis is bounded lazy component-scoped revision reuse. Do not
assume it wins.

Every candidate key includes:

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

Find any additional result-affecting fields. Compare ownership locations in
the real kernel. Reject global caches and unbounded revision history.

Exercise bounded deterministic eviction, no obsolete revisions, immutable
keys/values, kernel-thread confinement, no background mutation, atomic
publication, exception safety, `finally` cleanup and no partial entries.

# Repeated and nested benchmarks

Follow the versioned benchmark plan. For all three strategies run 1, 10 and 100
queries for:

- same A/B;
- overlapping arcs;
- reverse;
- periodic;
- STOP, WRAP and STRICT;
- repeated total;
- tolerance/policy change.

Record evaluator, derivative and integrator calls; subdivisions; component/
index builds; hits/misses; evictions; retained entries; latency; invalidation;
and all forbidden access counters.

Characterize:

```text
L1
 -> metric(L1)
 -> L2
 -> metric(L2)
 -> L3
```

and repeated upstream total consumption. Hard expectations are zero render and
legacy-sample access, no whole-locus regeneration per downstream point, no
metric-index build per point, same-revision/policy reuse, normal DAG
invalidation and index-off equality. Separate necessary from wasteful
recompute.

Wall-clock is informational. Functional counters and bounded state are hard.

# Required cases and evidence

Execute every applicable row in
`docs/validation/g7_locus_v2_metric_validation_matrix.md`, including:

- segment, circle, ellipse, parabola, transcendental;
- scale/translation and both reparameterizations;
- self-X, multibranch/multicomponent, periodic, cusp;
- collapsed, isolated, empty, stale and topology-change cases;
- STOP/WRAP/STRICT, gaps, reverse, endpoints/limits;
- total one/many components/branches, repeated, invalidate, infinite/mixed;
- repeated/nested/three-level composition.

Use without modifying originals:

- `AlgoPerimeterLocus`;
- `Templatev7.ggb` `listLength`, `listLength12` and `postLocus`;
- `InterCilConoObliqueTwoLevels.ggb`;
- `InterCilConoOblique.ggb`.

Candidate pilots: segment/circle, small deterministic cylinder development,
traced nested metric fixture and a bounded oblique-cone subset if reproducible.
Full legacy migration is not required.

# Compatibility and serialization

Classic and public GeoCeDG `Locus[...]` remain legacy. Preserve all existing
`GeoClass` ordinals and behavior, `Length`/`Perimeter`, `Path`, G5 export,
Classic/Desktop packaging and old `.ggb` files. G7A creates no XML type,
persistence, migration, public command, 3D dispatch or public metric surface.
The developer laboratory remains opt-in and Locus V2 remains
experimental/internal and disabled by default.

# Required artifacts

Produce or update, with exact provenance:

- `docs/validation/g7a_locus_v2_metric_characterization_report.md`;
- `docs/validation/g7a_locus_v2_metric_traceability_matrix.md`;
- versioned G7A evidence/reference manifests under
  `geocedg/validation/locus-v2/`;
- narrowly scoped characterization tests/probes/scripts;
- proposed edits to the G7 plan/model/architecture/spec/ADR/matrix/benchmark
  only where evidence requires them;
- exact G7B source/test/documentation edit set and hard functional budgets.

Record raw large outputs under ignored `artifacts/` with a versioned hash
manifest when needed. Do not overwrite G6/G6R evidence.

The report must label facts, assumptions, inferences, unsupported cases and
author decisions separately. Keep the spec `PROPOSED — NOT NORMATIVE` and ADR
0007 `Proposed` until explicit author approval.

# Required tests and commands

Run at minimum:

- internal link and cross-document consistency checks;
- focused characterization tests/probes;
- reference-data/hash verification;
- `tools/agent/verify-operational.ps1`;
- `tools/agent/verify-locus-v2.ps1`;
- `git diff --check`;
- proof that productive `source/**/src/main` remains unchanged;
- proof that G7B and G8 have not started.

Use repository wrappers as executable authority. Save command, exit code,
environment and log path. Do not claim evidence from an uncompleted command.

# Stop conditions

Stop and report if:

- G6/G6R fails;
- total-variation semantics cannot be preserved;
- references disagree without explanation;
- a method cannot state a truthful guarantee;
- index strategies differ semantically;
- index state cannot be bounded/invalidation cannot be deterministic;
- numeric participation hides rich state or exposes inadmissible values;
- nested metrics require render/sample truth or per-point whole-index builds;
- a legacy original hash changes;
- characterization requires productive/public implementation;
- an ambiguity lacks an author-approved policy.

# Completion and author review

Do not report G7A PASS merely because experiments finish. Before author review:

```text
G7A CHARACTERIZATION = COMPLETE / AWAITING AUTHOR REVIEW
G7B = NOT STARTED
G8 = NOT STARTED
G7 SPEC = PROPOSED / NOT NORMATIVE
ADR 0007 = PROPOSED
```

Only an explicit author decision may promote the spec, accept/replace ADR 0007
and close G7A as `PASS — AUTHOR APPROVED`. That decision still does not start
G7B without separate authorization.

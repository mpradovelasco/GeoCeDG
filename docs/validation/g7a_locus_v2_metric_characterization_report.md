# G7A Locus V2 metric characterization report

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Branch | `feature/g7a-locus-v2-metric-characterization` |
| Initial/planning SHA | `e918846a73829032ab1e1aff37e863fed40c1969` |
| Planning parent (`origin/main`) | `726abd95be928e232f3a3f7c6b637605b46d0cb1` |
| Executed prompt SHA-256 | `4820bf0934b84f3ea84ec5f30930a0be56769c150940ce53483e1150232fab39` |
| Root `AGENTS.md` SHA-256 | `ea76fd1724088398bdee4d14c69079d830a79530e53b3b50187f8ce537b6b2ff` |
| G7A-R1 | `PASS — AUTHOR APPROVED` |
| G7B | `AUTHORIZED / NOT STARTED` |
| G8 | `NOT STARTED` |
| ADR 0007 | `Accepted` |
| G7 specification | `NORMATIVE / AUTHOR APPROVED` |
| Date | 2026-08-13 |

```text
RECOVERY MODE =
REEXECUTED FROM VERSIONED G6/G6R + RESTORED G7 PLANNING BASELINE

PRIOR UNVERSIONED G7A RESULTS =
NOT USED
```

This report records a fresh scientific reexecution. No value, file, commit,
hash or conclusion from the lost workstation was recovered or used. The
author-reviewed planning commit, versioned G6/G6R evidence and current source
were the only starting evidence.

> Follow-up: the focused
> [G7A-R1 refinement](g7a_r1_locus_v2_metric_refinement_report.md) supersedes
> only this report's open value/error API, deterministic-work,
> multi-consumer-index ownership and atomic-failure recommendations. This
> historical report remains the 37-probe first-review evidence. The final
> author closeout accepts all 42 recommendations, R1-1..R1-22 and the three
> API normalizations; current G7A disposition is `PASS — AUTHOR APPROVED`.

## 1. Preflight and scope

The feature branch pointed exactly at the planning commit before edits.
`origin/main` is an ancestor of the planning commit and the planning commit is
an ancestor of this branch. The tracked worktree was clean. One pre-existing
untracked root note, `ChatGPT-Planificación desarrollo GeoCeDG.md`, was an
author-authorized exception; it was not read as authority, modified or used as
G7A evidence.

The following entry assertions were reproduced:

- G6 `PASS` and G6R `PASS`;
- ADR 0006 `Accepted`;
- `locus-v2-semantics.md` approved as the normative G6 contract;
- G7 planning recovery `PASS`;
- ADR 0007 `Proposed`;
- G7 metric specification `PROPOSED — NOT NORMATIVE`;
- G7B and G8 not started;
- Locus V2 remains experimental, internal and disabled by default;
- no productive V2 metric class exists.

Baseline evidence was regenerated before any edit:

| Gate | Result | Evidence |
|---|---|---|
| `tools/agent/verify-operational.ps1` | PASS | 83 controlled upstream files; operational contracts passed |
| `tools/agent/verify-locus-v2.ps1` | PASS | G6/G6R shared gate: 73 tests, zero failures; main/test checkstyle passed |
| G6R Desktop laboratory gate | PASS | 3 tests, zero failures; Desktop main/test checkstyle passed |
| Baseline log root | informational | `%TEMP%/geocedg-g7a-preflight-locus-v2` |

G7A changed no `src/main` file. All executable candidate types live under
`source/shared/common-jre/src/test`. It created no productive metric Geo, no
metric GeoClass, no index, no command, no Path behavior, no XML, no G7B code
and no G8 behavior.

## 2. Executable evidence package

The test-private package contains:

- `G7AMetricSemanticModel`: immutable position, binding, query, route,
  contribution and result candidates plus resolver and aggregator;
- `G7AMetricNumerics`: deterministic adaptive Simpson and evaluator-only
  refinement experiments with explicit counters and guarantees;
- `G7AMetricIndexExperiment`: reference, eager and lazy strategies with full
  keys, bounded state, invalidation and failure injection;
- semantic, numerical, index, nested-composition and Geo lifecycle test suites;
- an independent CPython/mpmath generator and hash-pinned expected manifest;
- `verify-g7a-metrics.ps1`, subordinate to the composed `verify.ps1` authority.

These types are experimental instruments. Their Java names do not themselves
approve the future productive API.

## 3. Mathematical contract

The experiments support total variation as the length authority:

\[
\operatorname{Var}(F;[a,b])=
\sup_P\sum_i\lVert F(t_i)-F(t_{i-1})\rVert.
\]

For absolutely continuous `F`:

\[
\operatorname{Var}(F;[a,b])=
\int_a^b\lVert F'(t)\rVert\,dt.
\]

Partition sums for a parabola increase under refinement and converge from
below to an independently computed reference. The finite chord sum is one
lower-bound approximation, not the definition. No render vertex, render cache,
legacy sample, viewport, zoom, DPI or pixel tolerance participates.

The explicit outcomes characterized are:

| Case | Value kind | Status | Rectifiability |
|---|---|---|---|
| finite segment/ellipse/parabola/transcendental | `FINITE` | `SUCCESS` | `RECTIFIABLE` |
| analytically divergent whole line | `POSITIVE_INFINITY` | `SUCCESS` | `NON_RECTIFIABLE` |
| oscillatory infinite variation | `POSITIVE_INFINITY` | `SUCCESS` | `NON_RECTIFIABLE` |
| missing metric capability | `ABSENT` | `UNSUPPORTED` | `UNDETERMINED` |
| insufficient improper-tail evidence | `ABSENT` | `LIMIT_NOT_ESTABLISHED` | `UNDETERMINED` |
| evaluator/integrator failure | `ABSENT` | `NUMERICAL_FAILURE` | `UNDETERMINED` |

`DIVERGENT` is not used as a generic numerical-failure state.

## 4. Complete-locus query and multiplicity

`TotalLocusMetricQuery` has locus identity and revision only. It has no A/B,
direction, same-position policy, `FULL_CYCLE` or `WRAP_TO_START`. The tested
aggregation is:

\[
\mathcal L_{\mathrm{total}}(L_r)=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Each valid-domain component contributes exactly once and each contribution
retains branch/component provenance. Disconnected components are added without
a gap chord or fabricated route. One periodic branch contributes exactly one
fundamental cycle, independent of orientation or chosen seam.

The measured meaning is `CONSTRUCTIVE_TRAVERSAL_LENGTH`:

- distinct preimages at a self-intersection remain distinct;
- retracing counts for every constructive preimage;
- coincident images from distinct constructive branches count separately;
- geometric-image union deduplication remains out of scope.

Empty domains, isolated points and collapsed images produce finite zero with
complete coverage and retained diagnostics. Zero is not failure or absence.

## 5. Semantic position and revision binding

The test-private split validates this exact candidate shape:

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

A valid-component split changes the revision-scoped component key but not the
semantic position. Equal Cartesian images with parameters `-1` and `1` retain
different semantic positions. Provider-version changes change position
identity. A branch may disappear and later reappear, but only an explicit bind
against the new revision may recover it.

Using a binding at a different revision yields `POSITION_STALE`. No
coordinate-proximity repair is attempted. `copy`, `set`, topology invalidation
and foreign-construction copy tests clear the current binding/result and demand
normal-DAG recomputation.

## 6. Route resolution

Route selection and integration were executed as separate test-private
operations. The resolver owns locus/revision/branch/component validation,
direction, periodic seam, same-position policy, open-boundary policy, global
boundaries, gaps and reachability. It never integrates.

Required findings:

- `FORWARD` and `REVERSE` select routes; values remain non-negative;
- equal semantic positions use explicit `ZERO_LENGTH` or `FULL_CYCLE`;
- `FULL_CYCLE` is rejected for a non-periodic branch;
- a periodic seam produces ordered segments in one component and one
  fundamental cycle;
- an included global endpoint is reachable; an excluded endpoint cannot bind;
- branch/locus mismatch and stale binding are typed failures;
- no policy crosses an internal invalid-domain gap;
- `SHORTEST` was not needed by the characterized CeDG pilots and is recommended
  deferred from G7B minimum.

For `start --- B ------ A ----- end` in `FORWARD` direction:

| Policy | Segments/value | Result |
|---|---|---|
| `STOP_AT_END` | `A -> end` | finite partial; target not reached; `STOPPED_AT_BOUNDARY` |
| `WRAP_TO_START` | `A -> end`, `start -> B` | target reached; wrapped; geometrically disconnected |
| `STRICT` | none | absent; `TARGET_NOT_REACHABLE` |

The WRAP probe asserts exactly two segments and
`geometricallyConnected=false`. It changes neither topology nor incidence.

## 7. Rich result and aggregation precedence

The result candidate keeps independent axes for value kind, coverage,
computation status, rectifiability and traversal outcome. It additionally
records construction fidelity, evaluator method, metric method,
representation role, numerical guarantee, absolute/relative error, units,
provenance and ordered contribution decomposition.

Author-approved aggregate precedence:

1. sort contributions by stable branch/component provenance before compensated
   finite summation;
2. any established positive-infinite contribution makes the known total value
   `POSITIVE_INFINITY`;
3. any unresolved contribution makes coverage `INCOMPLETE`, even when infinity
   is already established;
4. computation precedence is `INVALID_QUERY` (query-level) >
   `NUMERICAL_FAILURE` > `LIMIT_NOT_ESTABLISHED` > `UNSUPPORTED` > `SUCCESS`;
5. any `NON_RECTIFIABLE` contribution dominates rectifiability; otherwise an
   undetermined contribution yields `UNDETERMINED`;
6. propagate the weakest numerical guarantee;
7. sum finite absolute-error bounds/estimates; derive relative error only from
   a finite nonzero total;
8. never discard a contribution diagnostic.

The mandatory infinite-plus-unsupported case yields a known
`POSITIVE_INFINITY`, `INCOMPLETE` coverage, `UNSUPPORTED` status,
`NON_RECTIFIABLE` rectifiability and both contributions. It is rich-defined but
not scalar-admissible.

## 8. Rich Geo lifecycle and scalar participation

### 8.1 Defined state

The recommended `GeoLocusMetricResult.isDefined()` meaning is “an immutable
rich snapshot has been atomically published and not invalidated/removed.” A
published `ABSENT`, `UNSUPPORTED`, `LIMIT_NOT_ESTABLISHED` or
`NUMERICAL_FAILURE` snapshot is still a defined diagnostic object. It is not a
defined scalar. This preserves the required separation between rich defined
state and scalar admissibility.

Publication must be atomic. A thrown computation cannot replace the previous
snapshot or leave a build active. Invalidation clears the current snapshot;
recovery requires normal-DAG recomputation. Removal clears the snapshot and
index ownership.

`copy`, `copyInternal` and `set` must not import a foreign construction,
revision, binding, index or partial current value. The G7B minimum should make
list/sequence value-copy unsupported unless it can trigger an explicit safe
recompute; it must not silently snapshot a revision-bound metric.

### 8.2 A/B/C comparison

The real `NumberValue` and `GeoNumberValue` interfaces are static marker/API
contracts. They expose `getDouble()` and `isDefined()` but no per-state scalar
admissibility hook. The source audit found 219 `instanceof NumberValue`
occurrences in 72 shared-source files, including algebra evaluation, generic
algorithms, lists and CAS paths.

| Alternative | Finding |
|---|---|
| A — no generic numeric facade | safe for the rich authority, but does not offer generic opt-in scalar use |
| B — conditional read-only facet | rejected: implementing the static interface exposes every instance to generic consumers and conflates rich-defined with scalar-defined |
| C — explicit numeric adapter | recommended: opt-in derived `GeoNumeric`/adapter depends on the rich Geo, checks admissibility and becomes undefined otherwise |

Recommendation C does not make the adapter authoritative. The
`GeoLocusMetricResult` remains the only metric result authority. GeoCeDG-owned
nested algorithms may consume the rich type directly after the same explicit
admissibility check, without a generic facade.

Default scalar admissibility is recommended only for finite, successful,
complete, semantically satisfied results with no stale/unsupported
contribution and at least an estimated error guarantee. Valid zero and an
explicit valid WRAP qualify. STOP partial, stale, branch mismatch,
discontinuity, unsupported, incomplete, failure, absent, limit-not-established,
floating-point-uncertified and positive infinity remain rich-only by default.

## 9. Numerical methods and independent references

Analytic segment and circle values, differential ellipse/parabola/exponential
and cusp values, a regular exponential reparameterization and the endpoint-
degenerate `u^3` map were executed. Both reparameterizations preserved length;
the latter is robustness evidence, not a regular-diffeomorphism proof.

The test-private adaptive Simpson integrator owns per-call absolute/relative
tolerance, refinement ceiling, evaluation/subdivision counters, a typed ceiling
outcome and an error estimate. It agrees with independent 80-decimal references.
The existing upstream adaptive Legendre-Gauss implementation is also accurate
on the measured smooth fixtures, but even its static numerical entry point
requires GeoGebra's global formatting bootstrap. It also uses static mutable
integrators and a static counter, fixed `Kernel.STANDARD_PRECISION`, and returns
a bare value or `NaN`. It therefore cannot directly represent G7
error/status/lifecycle semantics and is not recommended as the G7B authority.

The author-approved initial G7B policy default is:

```text
eps_metric_abs = 1e-10 construction length unit
eps_metric_rel = 1e-9
effective threshold = max(eps_metric_abs, eps_metric_rel * S)
S = max(abs(current length estimate), endpoint chord, explicit provider scale)
maximum adaptive depth = 22
```

`S` is world-coordinate and translation invariant. Absolute error budgets are
allocated deterministically per component/segment and aggregate by sum.
Relative error is reported against a finite nonzero result. G7B must expose the
ceiling and stopping reason; reaching the ceiling is
`LIMIT_NOT_ESTABLISHED`, not success. These values are independent of the G6
evaluation envelope, `eps_domain`, render pixels and future G8 root tolerance.

The evaluator-only alias fixture samples a 64-cycle oscillation. Two dyadic
refinement levels both return chord length `1` with zero difference, while the
independent variation is approximately `25.64417077003715`. Refinement
agreement alone therefore cannot certify error. Without explicit regularity or
error assumptions, evaluator-only output is
`FLOATING_POINT_UNCERTIFIED` or `UNSUPPORTED`; `ESTIMATED_ERROR` requires an
explicit assumption and `CERTIFIED_ERROR_BOUND` requires a real proof/bound.

Independent evidence is generated by CPython 3.12.13, mpmath 1.4.1 at 80
decimal digits. The script, equations, values and SHA-256 manifest are under
`geocedg/validation/locus-v2/g7a/`. Python is validation evidence only.

## 10. Improper metrics and arc coordinate

The cases distinguish finite A/B on an unbounded line, infinite whole line,
finite convergent improper length, established infinite oscillatory variation
and insufficient limit evidence. No viewport cutoff is used.

G7B should use explicit variable transforms/tail policies. It may publish a
finite improper value or positive infinity only with analytic capability or a
defensible tail bound. Otherwise it publishes `LIMIT_NOT_ESTABLISHED` or
`UNSUPPORTED`. Finite cutoffs are evidence, never the improper result.

Component-local cumulative arc coordinate is useful: 100 repeated A/B queries
reuse 1 built component and 1,600 indexed intervals in the fixture with no
extra integration. It is recommended as internal immutable index data only.
It is not semantic position, does not cross gaps/branches and creates no public
Path surface.

## 11. Index strategy measurements

All strategies used the same component work model: one build performs 8
integrator calls, 33 evaluator calls, 33 derivative calls and 16 refinements.
Wall-clock values varied markedly on the workstation and remain informational.

| Same A/B trace | Reference builds | Eager builds | Lazy builds | Lazy hits | Retained lazy entries |
|---:|---:|---:|---:|---:|---:|
| 1 query | 1 | 3 | 1 | 0 | 1 |
| 10 queries | 10 | 3 | 1 | 9 | 1 |
| 100 queries | 100 | 3 | 1 | 99 | 1 |

For 1/10/100 repeated total queries over three components, reference performs
3/30/300 builds while eager and lazy each perform exactly 3. Same,
overlapping, reverse, periodic, STOP and WRAP traces were each run at 1/10/100
for all three strategies; the lazy strategy builds the requested component once.
STRICT rejects an unreachable route before integration. Algorithm, route policy,
tolerance, multiplicity or improper-limit changes each miss the key.

One fresh informational timing trace, in nanoseconds, was:

| Strategy | Cold | Warm |
|---|---:|---:|
| no reuse | 12,600 | 3,100 |
| eager whole revision | 230,900 | 19,000 |
| lazy component revision | 25,400 | 10,200 |

The synthetic component work is intentionally tiny, so these values are
dominated by JVM/timer noise and do not establish a timing winner. They satisfy
the requirement to observe cold/warm latency; no wall-clock gate is proposed.

The bounded test uses insertion-order deterministic eviction, proves retained
entries never exceed capacity, removes obsolete revisions, rebuilds once after
invalidation and leaves no entry after injected failure. Each experimental
entry accounts for 512 approximate bytes; capacity 2 retained exactly 1,024
bytes before eviction. Cache OFF/reference, eager and lazy return equal
semantic totals.

The first review advanced bounded `LAZY_COMPONENT_REVISION`; final author
closeout accepts it. R1 supersedes this report's per-metric-owner assumption:
the accepted ownership is one dedicated shared owner and one provisional
capacity of 64 entries per
active source locus. G7B must measure the real entry footprint before fixing a
stable capacity. No global cache, unbounded history, concurrency or background
mutation is justified.

## 12. Nested metric composition

The test-private chain is:

```text
L1 -> metric(L1) -> L2 -> metric(L2) -> L3
```

One cold request builds one index at each metric level. Repeated upstream total
requests and downstream point evaluations reuse the same revision/policy. A
geometry revision invalidates and rebuilds each affected level once; an
unchanged repeat does not. Cache OFF preserves the value while exposing the
extra builds.

Hard measured invariants are:

```text
render reads = 0
legacy sample reads = 0
whole-locus regenerations = 0
index build per downstream point = 0
maximum concurrent builds = 1
kernel thread only = true
```

G7B should gate functional counters, bounded state, invalidation and cache-off
equality. Absolute timing remains informational; no JMH, executor, DAG
flattening or concurrency is justified.

## 13. Upstream and GeoClass impact audit

The candidate dedicated append-only class remains
`GeoClass.LOCUS_METRIC_RESULT`. Reusing `NUMERIC`, `LOCUS` or `LOCUS_V2` would
misstate the rich value and/or geometry. The exact observed switch surface is:

- `GeoClass.java`: append after `LOCUS_V2`, preserving every existing ordinal;
- `LocusV2KernelIntegrationTest`: stop assuming `LOCUS_V2` is the final enum;
- `DrawablesTest`: add the rich metric class to the explicit non-drawable set;
- `ConstructionDefaults`: explicitly select a measurement-style default if
  the object enters default styling; this does not imply numeric participation;
- `EuclidianDraw`: its default already returns no drawable; an explicit
  non-drawable case is optional documentation, not a cast;
- `GeoElement`: no base-class edit was found necessary;
- Algebra View: use rich `toValueString`, non-editable state and label support;
- `NumberValue`/`GeoNumberValue`: no edit and no implementation by the rich Geo;
- CAS and generic numeric commands: unavailable unless the explicit adapter is
  requested and admissible;
- `GeoFactory`/XML: no registration in G7B minimum;
- 2D/3D: metric input is 2D Locus V2; result itself is non-geometric and has no
  3D behavior;
- G5 export: generic unsupported-geometry diagnostic; no metric export;
- packaging: GeoCeDG-owned common classes and developer-lab integration only.

Creation and updates must run through `AlgoLocusMetricV2.setInputOutput()` and
normal dependencies. Undo/redo and topology changes invalidate through the
normal DAG. No index is owned by Algebra View, UI, render code or a global
singleton.

## 14. Exact candidate G7B class/file map

Author-approved GeoCeDG-owned productive file plan for G7B:

```text
org/geocedg/common/kernel/locus/metric/
    LocusSemanticPosition2D
    MetricPositionBinding2D
    LocusMetricQuery2D
    BetweenPositionsMetricQuery
    TotalLocusMetricQuery
    LocusMetricRouteResolver2D
    LocusMetricRoute2D
    LocusMetricRouteSegment2D
    LocusMetricPolicy2D
    LocusDifferentialEvaluator2D
    LocusMetricIntegrator2D
    LocusMetricComponentState2D
    LocusMetricComponentStateBuilder2D
    LocusMetricComponentEvaluator2D
    LocusMetricContribution2D
    LocusMetricIndex2D
    LocusMetricAggregator2D
    LocusMetricResult2D

org/geocedg/common/kernel/geos/GeoLocusMetricResult
org/geocedg/common/kernel/algos/AlgoLocusMetricV2
org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter
```

The scalar adapter is explicit, derived and optional; it is not the authority.
The existing Desktop Locus V2 laboratory should be extended rather than adding
a public command or product surface.

Unavoidable upstream-owned edits are limited to the append-only `GeoClass`
entry and its exhaustive enum/default-style tests/switch handling. No edit is
proposed for command dispatch, legacy metrics, Path, XML/factory, 3D or G5
geometry export.

## 15. Legacy evidence and scientific pilots

`AlgoLengthLocus` delegates to legacy point length. `AlgoPerimeterLocus` sums
consecutive `MyPoint` chords. `listLength` and `listLength12` similarly sum
sampled lists; `postLocus` filters sampled data. These are preserved anti-pattern
evidence, not expected values and not migration targets.

The hash-pinned `InterCilConoObliqueTwoLevels.ggb` remains the functional
two-level control and `InterCilConoOblique.ggb` remains the pathological
three-level reference. Originals were not modified and are not build
dependencies.

Author-approved G7B pilots are segment/circle, a small deterministic cylinder
development, the traced three-level nested fixture, and a bounded oblique-cone
subset only if it is independently reproducible. Full legacy migration is not
a gate.

## 16. Mandatory decision table

Every row is `APPROVED` by the author at final closeout. R1 evidence and the
three API normalizations below refine representation without reversing a row.

| # | Decision | Author-approved G7A decision |
|---:|---|---|
| 1 | total variation | retain as mathematical authority; algorithms only approximate/evaluate it |
| 2 | complete query | separate endpoint-free `TotalLocusMetricQuery` |
| 3 | multiplicity | `CONSTRUCTIVE_TRAVERSAL_LENGTH` |
| 4 | disconnected aggregation | sum component variations; no gap chord/route |
| 5 | empty/isolated/collapsed | finite zero, complete coverage, diagnostic retained |
| 6 | infinity/nonrectifiable/unsupported | separate value/status/rectifiability axes |
| 7 | semantic position | identity + branch + provider version + canonical parameter |
| 8 | revision binding | separate binding with revision-scoped component key |
| 9 | stale/rebind | `POSITION_STALE`; explicit rebind only; no coordinate repair |
| 10 | FORWARD | required G7B route direction |
| 11 | REVERSE | required G7B route direction |
| 12 | SHORTEST | defer; no CeDG pilot justified it |
| 13 | ZERO/FULL_CYCLE | explicit distinct policies; FULL only for periodic semantics |
| 14 | STOP | partial rich result; not scalar-admissible |
| 15 | WRAP | two segments, target reached, geometrically disconnected |
| 16 | STRICT | required; absent unreachable result |
| 17 | route resolver | dedicated non-integrating resolver and immutable route |
| 18 | rich result axes | retain all orthogonal axes and decomposition |
| 19 | aggregate precedence | infinity known + incomplete unresolved; typed weakest status/guarantee |
| 20 | scalar admissibility | finite/success/complete/satisfied and adequate guarantee only |
| 21 | `GeoLocusMetricResult` | dedicated rich GeoElement in normal DAG |
| 22 | metric GeoClass | append-only `LOCUS_METRIC_RESULT` |
| 23 | numeric facet | choose C explicit adapter; reject B; rich Geo remains authority |
| 24 | analytic integration | first capability; only claim bounds actually defended |
| 25 | derivative integration | GeoCeDG-owned per-call adaptive integrator with typed error/status |
| 26 | evaluator-only guarantee | uncertified/unsupported absent explicit assumptions; never certify by agreement |
| 27 | `eps_metric` | abs `1e-10`, rel `1e-9`, 32768 evaluations, 16384 subdivisions and depth 22 as initial versioned policy defaults |
| 28 | error aggregation | deterministic sum of finite absolute errors; weakest guarantee |
| 29 | improper limits | explicit transform/tail evidence; otherwise limit not established |
| 30 | index strategy | accept bounded `LAZY_COMPONENT_REVISION` |
| 31 | index lifecycle | accept `DEDICATED_SHARED_OWNER`, current revision only; capacity 64 provisional/non-normative; insertion-order eviction; atomic/finally |
| 32 | repeated budget | one lazy build for 100 same-component queries; three for repeated total fixture |
| 33 | nested budget | one build per affected level/revision/policy; none per downstream point |
| 34 | arc coordinate | internal per-component index data only |
| 35 | units | construction length unit plus explicit units metadata |
| 36 | thread confinement | kernel thread; no executor/concurrent quadrature/background mutation |
| 37 | public API | internal Java + rich Geo + laboratory only |
| 38 | CeDG pilots | segment/circle, small cylinder, nested trace, feasible bounded cone subset |
| 39 | productive plan | GeoCeDG metric package + rich Geo/algo/optional adapter; minimal enum edits |
| 40 | hardening gates | immutable, bounded, atomic, exception-safe, ON/OFF, repeated/nested from first candidate |
| 41 | ADR 0007 | `Accepted` at final author closeout |
| 42 | G7 spec | `NORMATIVE / AUTHOR APPROVED` |

### 16.1 Final API normalizations

The author additionally approves:

1. a complete component key maps to immutable
   `LocusMetricComponentState2D`; state plus route segment produces a
   route-specific `LocusMetricContribution2D`, which is never shared;
2. `TraversalOutcome` is structurally optional in `LocusMetricResult2D` and is
   absent from total results; and
3. `MetricErrorAmount2D` is a closed established/not-established/not-applicable
   hierarchy, making contradictory state/amount pairs impossible.

## 17. Validation disposition

The canonical focused gate is
[`verify-g7a-metrics.ps1`](../../tools/agent/verify-g7a-metrics.ps1). It is
called by the composed `verify.ps1`; it does not become a parallel authority.

Final closeout evidence:

| Command/gate | Exit | Result/log |
|---|---:|---|
| `tools/agent/verify-g7a-metrics.ps1` | 0 | 37 G7A + 14 R1 tests, 0 failures/skips; test checkstyle 0 errors; links in 13 documents and independent hashes PASS; `artifacts/validation/g7a-author-closeout-focused/` |
| `tools/agent/verify-locus-v2.ps1` | 0 | G6/G6R 73 shared tests plus 3 Desktop laboratory tests, 0 failures; shared/Desktop checkstyle PASS; `artifacts/validation/g7a-author-closeout-locus-v2/` |
| `tools/agent/verify-operational.ps1` | 0 | 94 controlled upstream-tree files registered; schemas/manifests/text/boundary PASS |
| `tools/agent/verify.ps1 -SkipBuild` | 0 | complete composed operational/workstation/legacy/G5/G6/G7A/packaging/baseline/frontend authority PASS in 91.7 s; `artifacts/validation/g7a-author-closeout-composed/` |
| independent CPython/mpmath `--check` | 0 | formula/runtime/precision outputs match the SHA-256 manifest |
| `git diff --check` and cached equivalent | 0 | no whitespace errors |
| productive-source audit from planning SHA | 0 | zero changed `source/**/src/main/**` paths |

Two initial author-closeout runs were environment-only interruptions: the
sandbox denied Conda environment writes and Gradle distribution network access.
The exact commands were rerun with managed access and passed; no project source
change was used to compensate for either restriction.

The original legacy hashes were re-read from disk and matched their manifests:

```text
InterCilConoOblique.ggb =
b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27

InterCilConoObliqueTwoLevels.ggb =
587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6
```

Two validation interruptions were classified rather than converted into source
changes:

- the first final G6/G6R invocation stopped before Gradle because its verifier
  still required the historical literal `G7 (PENDING / NOT STARTED)` in the
  user guide. This was a stale documentation-contract assertion. The verifier
  now accepts either that historical state or the explicit G7A
  characterization-only/G7B-not-started state; static and full reruns passed;
- the first composed `-SkipBuild` invocation reached and passed packaging, then
  exceeded its 600-second external limit while restoring an unnecessary global
  output snapshot. All 10,047 pre-existing files were restored from the
  wrapper-owned snapshot and counted before deletion. Static paths now skip
  generated-state transactions when no build/toolchain/artifact action can
  mutate them; the complete composed rerun passed in 69.5 seconds.

All full wrappers restored the generated state present at invocation. No G7A
generated output is versioned as authority.

The final author review accepts every G7A and G7A-R1 recommendation with the
three API normalizations recorded above. This closeout promotes the spec and
ADR and authorizes, but does not execute, G7B.

```text
RECOVERY MODE =
REEXECUTED FROM VERSIONED G6/G6R + RESTORED G7 PLANNING BASELINE

PRIOR UNVERSIONED G7A RESULTS =
NOT USED

G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED

G8 = NOT STARTED

ADR 0007 = ACCEPTED

G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED

G7B = AUTHORIZED / NOT STARTED

PRODUCTIVE G7 SOURCE CHANGES = 0
```

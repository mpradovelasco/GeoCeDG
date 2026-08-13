# G7B native Locus V2 metric kernel report

| Field | Value |
|---|---|
| Disposition | **PASS — AUTHOR APPROVED** |
| G7B execution baseline | `bb3623dbd5945b558f42ff1a6f2d9ce4262cb983` (`geocedg-g7a-pass`) |
| Productive implementation commit | `92b0684074ef328039946f724d4aa951f70e21ec` |
| Planning ancestor | `e918846a73829032ab1e1aff37e863fed40c1969` |
| Executed prompt SHA-256 | `c215a36a8350e5dd44da9ae3e546899d8ab0cf1abc480aec22666fc363f19aed` |
| Versioned prompt canonical SHA-256 | `11e938be2788902298722d2e0442c9afb5700e1f7512b9b22732248d61af1c11` |
| Closeout commit / worktree | One coherent documentary commit containing this report; its exact SHA is handed off after commit because a commit cannot contain its own hash; clean tracked worktree required |
| Date | 2026-08-13 |

## 1. Entry and scope

G6/G6R, the 51 G7A/R1 probes, the normative metric specification and Accepted
ADR 0007 reproduced before productive edits. G7B was executed only on
`feature/g7b-locus-v2-metric-kernel` from the author-approved G7A closeout.

The implementation is limited to an internal two-dimensional Java API, one
rich metric Geo in the normal kernel DAG, one explicit scalar adapter and the
existing opt-in developer laboratory. It adds no command, XML registration,
persistence, public `Path`, point-on-locus, 3D, G5, G8 or G9 behavior.

The final closeout reconciled the authoritative Gradle/JUnit XML with the
focused verifier without changing tests: value 8, route 11, numerical 17,
improper 6, lifecycle 11, benchmark 7 and nested 2, for 62 productive
common-kernel tests, plus three developer-laboratory tests. All report zero
failures and zero errors.

## 2. Productive implementation map

The package `org.geocedg.common.kernel.locus.metric` contains 78 focused Java
types organized by responsibility:

| Area | Main types |
|---|---|
| identity/binding | `LocusSemanticPosition2D`, `MetricPositionBinding2D`, `LocusMetricPositionBinder2D` |
| queries/routes | `LocusMetricQuery2D`, `BetweenPositionsMetricQuery`, `TotalLocusMetricQuery`, `LocusMetricRouteResolver2D`, route/segment values |
| policy/work | `LocusMetricPolicy2D`, `MetricWorkBudget2D`, improper/evaluator-only policies and work-limit taxonomy |
| closed result values | `MetricValue2D` with finite/infinity/absent variants; `MetricErrorAmount2D` with established/not-established/not-applicable variants |
| capabilities | analytic, differential, evaluator-only and unsupported capabilities plus deterministic integrator |
| shared derived state | `LocusMetricComponentState2D`, complete key/index, shared owner, lease, counters/statistics |
| query-local result | contribution, aggregator, rich result, diagnostics, unit/provenance and engine |

Normal-DAG publication is implemented by:

- `GeoLocusMetricResult`, a rich non-`NumberValue`, non-`Path`, nonpersistent,
  non-drawable Geo with `GeoClass.LOCUS_METRIC_RESULT`;
- `AlgoLocusMetricV2`, which registers the source locus and query-owned Geo
  inputs, owns one shared-owner lease, computes and publishes atomically; and
- `AlgoLocusMetricScalarAdapter`, which publishes a derived `GeoNumeric` only
  for scalar-admissible rich states.

`GeoLocusV2` gains only the derived-owner lifecycle seam and instrumentation.
The Desktop laboratory creates disposable total and between-position metrics,
shows rich axes and functional counters, and removes those algorithms when its
diagnostics window closes.

### 2.1 Exact productive file inventory

Added GeoCeDG-owned productive files:

```text
source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java
source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java
source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusMetricResult.java

source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/
  AbsentMetricValue2D.java
  AnalyticLocusMetricCapability2D.java
  BetweenPositionsMetricQuery.java
  DifferentialLocusMetricCapability2D.java
  EstablishedMetricErrorAmount2D.java
  EvaluatorOnlyLocusMetricCapability2D.java
  EvaluatorOnlyPolicy.java
  FiniteMetricValue2D.java
  ImproperLimitPolicy2D.java
  LocusAnalyticMetricEvaluation2D.java
  LocusAnalyticMetricEvaluator2D.java
  LocusDifferentialEvaluation2D.java
  LocusDifferentialEvaluator2D.java
  LocusMetricAggregator2D.java
  LocusMetricCapability2D.java
  LocusMetricCapabilityHierarchy2D.java
  LocusMetricComponentBuildException.java
  LocusMetricComponentEvaluator2D.java
  LocusMetricComponentKey2D.java
  LocusMetricComponentState2D.java
  LocusMetricComponentStateBuilder2D.java
  LocusMetricContribution2D.java
  LocusMetricEngine2D.java
  LocusMetricIndex2D.java
  LocusMetricIndexKey2D.java
  LocusMetricIndexMode.java
  LocusMetricIndexStatistics2D.java
  LocusMetricInstrumentation2D.java
  LocusMetricInstrumentationSnapshot2D.java
  LocusMetricIntegrator2D.java
  LocusMetricOwnerLease2D.java
  LocusMetricPolicy2D.java
  LocusMetricPositionBinder2D.java
  LocusMetricQuery2D.java
  LocusMetricResult2D.java
  LocusMetricResults2D.java
  LocusMetricRoute2D.java
  LocusMetricRouteResolver2D.java
  LocusMetricRouteSegment2D.java
  LocusMetricSharedOwner2D.java
  LocusSemanticPosition2D.java
  MetricArcCoordinateEvidence2D.java
  MetricCapabilityMetadata2D.java
  MetricComponentPartition2D.java
  MetricComputationStatus.java
  MetricCoverage.java
  MetricDiagnostic2D.java
  MetricDiagnosticCode2D.java
  MetricErrorAmount2D.java
  MetricErrorAmountKind.java
  MetricErrorEvidence2D.java
  MetricErrorEvidenceScope.java
  MetricEvaluatorMethod2D.java
  MetricIntegrationResult2D.java
  MetricMethod2D.java
  MetricMultiplicityPolicy.java
  MetricPositionBinding2D.java
  MetricPositionEvaluationStatus.java
  MetricProvenance2D.java
  MetricRectifiability.java
  MetricRepresentationRole2D.java
  MetricRouteSegmentRole.java
  MetricRouteStatus.java
  MetricUnit2D.java
  MetricValue2D.java
  MetricValueKind.java
  MetricWorkBudget2D.java
  MetricWorkLimit2D.java
  NotApplicableMetricErrorAmount2D.java
  NotEstablishedMetricErrorAmount2D.java
  OpenBoundaryPolicy.java
  package-info.java
  PositiveInfinityMetricValue2D.java
  SamePositionPolicy.java
  TotalLocusMetricQuery.java
  TraversalDirection.java
  TraversalOutcome.java
  UnsupportedLocusMetricCapability2D.java
```

Modified GeoCeDG-owned productive files:

```text
source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java
source/desktop/desktop/src/main/java/org/geocedg/desktop/locus/LocusV2LaboratoryController.java
```

The sole upstream-owned productive modification is:

```text
source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java
```

It appends `LOCUS_METRIC_RESULT`; it changes no existing ordinal. The exact
ownership/provenance inventory is also registered in
`docs/upstream/modified-files.yml`.

The audited productive delta is exactly 84 files: 81 additions (the 78-type
metric package plus the two algorithms and rich Geo) and three modifications
(`GeoLocusV2`, the Desktop laboratory controller and `GeoClass`). Exactly one
of those files is upstream-owned.

## 3. Mathematical and route behavior

Length is total variation of the semantic evaluator. Analytic values and
differential quadrature are implementations of that definition; evaluator-only
adaptive chord evidence is never described as exact. Rendering, legacy samples
and viewport state are absent from the metric dependency graph.

`TotalLocusMetricQuery` has no endpoints, direction or traversal outcome. It
adds each valid component once, one fundamental cycle for a periodic branch,
preserves constructive branch/preimage multiplicity and never adds a gap
chord. Empty, isolated and collapsed cases are finite complete zero with typed
diagnostics.

`BetweenPositionsMetricQuery` requires revision-bound semantic positions and
explicit FORWARD/REVERSE, ZERO/FULL_CYCLE and STOP/WRAP/STRICT policies.
The resolver emits only single-component segments. WRAP emits two segments and
records `geometricallyConnected=false`; STOP is incomplete and scalar-
inadmissible; STRICT rejects before metric work. No internal gap is crossed.

## 4. Value, error and aggregation contracts

All semantic values are immutable and defensively copy list/array state. Value,
coverage, computation status, rectifiability and optional traversal are
orthogonal. Error amount is a closed hierarchy; there is no NaN, `-1`, magic
zero or null state. `MetricErrorEvidence2D` directly reuses the G6
`LocusSemanticMetadata2D.NumericGuarantee`.

The aggregator preserves ordered contribution decomposition and propagates
coverage, diagnostics, error and weakest guarantee deterministically. A known
infinite contribution plus an unsupported contribution remains known positive
infinity with incomplete coverage and both diagnostics. Numerical failure is
not collapsed into divergence.

## 5. Numerical policy and reference coverage

The versioned initial policy is:

```text
eps_metric_abs = 1e-10
eps_metric_rel = 1e-9
maximumMetricEvaluations = 32768
maximumMetricSubdivisions = 16384
maximumAdaptiveDepth = 22
```

The productive integrator is deterministic adaptive Simpson quadrature with
per-call mutable state and independent ceilings. Scientific tests cover
segment, circle, ellipse, parabola, exponential graph, cusp, translation,
scale, a regular exponential reparameterization and `u^3`. Ellipse,
exponential, parabola and cusp compare to the 80-digit references generated and
hashed by G7A, not to the candidate algorithm itself.

Evaluator-only support is uncertified by default. Estimated evidence exists
only under the explicit assumptions policy for complete component evidence.
An evaluator-only between-position subarc derived by arc-coordinate
interpolation remains uncertified because no route-local error has been
established; the component estimate is not proportionally relabelled.
Unsupported, numerical failure and limit-not-established are legitimate typed
outcomes. Improper fixtures cover a finite line subarc, infinite whole line,
convergent improper total, positive infinity/non-rectifiability and
insufficient evidence without viewport cutoff.

Provider/capability coverage is explicit: analytic evaluators publish their
own exact/estimated/infinite semantics; differential evaluators feed the
deterministic quadrature; point-evaluator-only definitions use adaptive
world-coordinate evidence; and absent capabilities yield `UNSUPPORTED`.
No provider reads render caches or legacy samples.

## 6. Component state and shared-owner lifecycle

The accepted boundary is implemented literally:

```text
complete component key
    -> immutable LocusMetricComponentState2D
    + route segment
    -> query-local LocusMetricContribution2D
```

The key includes locus identity, revision, branch, revision-scoped component,
capability version, algorithm/policy/tolerance/work/multiplicity and improper
policy. It contains no A/B endpoints. The owner never stores routes,
contributions, query results or aggregates.

There is one kernel-thread-confined non-GeoElement owner per active source
locus, no global/static or Construction repository, no dependency edges and no
background mutation. Algorithms use leases. Last-consumer removal, source
revision/topology change, undefined transition and source removal release old
state. Owner operations and lease release reject an off-thread caller before
changing lifecycle state. Different loci and Constructions never share.

Private builds publish atomically only after success. Exceptions leave no entry
and active-build cleanup occurs in `finally`. Capacity 64 entries per active
locus is an initial provisional, non-normative implementation default from ADR
0007, with deterministic insertion-order eviction. It is not a stabilized
capacity. `REFERENCE_NO_INDEX_REUSE` remains the semantic oracle.

## 7. Functional measurements

| Gate | Productive result |
|---|---:|
| 100 distinct compatible consumers | 1 component-state build, 99 cross-result hits, 0 duplicate compatible builds |
| same metric consumer, same query N=1/10/100 | 1 build, 0/9/99 normal hits and 0 cross-result hits |
| repeated total, 100 queries × 3 components | 3 builds, 297 hits, 3 retained entries |
| STRICT unreachable, 100 queries | 0 builds, 0 misses |
| 65 complete policy keys | 64 retained entries, 1 deterministic eviction |
| failed private build | 0 published entries, 0 active builds |
| revision invalidation | 0 obsolete revisions retained; one necessary rebuild |
| cache/index ON versus OFF | equal value, coverage, status, rectifiability and decomposition |

The same-A/B analytic traces report the full counter vector. For one metric
consumer at N=1/10/100,
evaluator, derivative, integrator and subdivision counts are all zero because
the selected provider is analytic; builds remain 1, misses remain 1, hits are
0/9/99, retained entries remain 1, evictions and invalidations remain zero,
and `crossResultHits` remains 0. First-query and remaining
warm-query nanoseconds are captured as informational data only, never as a
gate. The 100-distinct-consumer trace separately records one build, 99
cross-result hits and zero compatible duplicate builds. Normal hits and
cross-result hits are intentionally not merged.

For the analytic fixture, productive metric-state instrumentation reports
`retainedBytes = 336`. This is a deterministic logical retained-state estimate,
not a JVM heap measurement, object-layout measurement, proof that all
component-state entries occupy 336 JVM bytes, or a basis for stabilizing
capacity 64. The prior R1 memory weights remain synthetic characterization
evidence. Detailed JVM heap/object-layout accounting was not performed and is
not required for G7 closeout. Productive gates instead establish deterministic
bounded entry count, capacity-plus-one eviction, zero obsolete-revision
retention, and one component-state build with no duplicate compatible builds
for 100 compatible consumers.

Total-first and local-first order each build three states for three components.
The nested `L1 -> metric(L1) -> L2 -> metric(L2) -> L3` fixture uses the normal
DAG, reuses same-key state and records zero render reads, render-vertex reads,
legacy-sample reads, whole-locus regenerations, downstream-point index builds
and compatible duplicate builds. Wall-clock remains informational.

For the nested fixture, L1 records one state build and two cross-result hits;
L2 records one build and one cross-result hit. Every level records zero render,
legacy-sample and whole-locus-regeneration work, zero per-downstream-point
index builds, zero compatible duplicate builds and a maximum of one active
private build. An upstream source change propagates through the ordinary DAG
and publishes current-revision results at every metric level.

## 8. Rich Geo, scalar adapter and P1

`GeoLocusMetricResult.isDefined()` means a current immutable rich snapshot
exists, including a coherent failure snapshot. Scalar admissibility is a
separate predicate. Copy/copyInternal/set clear current revision-bound state;
no stale index, binding or foreign Construction state is copied.

Total algorithms follow the source's current revision because total has no
durable A/B binding to rebind. Between-position algorithms retain their
revision-bound positions and publish a stale rich failure after a source
revision until an explicit new query supplies rebound positions.

At recompute, P1 first begins the current source revision and removes the old
current success. Success or one coherent rich failure is then published for
that revision. Failed component builds never enter shared state. The scalar
adapter becomes undefined for STOP partial, stale, unsupported, incomplete,
failed, absent, uncertified or positive-infinite results.

## 9. Compatibility and upstream impact

The only upstream-owned productive edit is append-only
`GeoClass.LOCUS_METRIC_RESULT`; the inherited base `GeoElement`, numeric
interfaces, command dispatch, XML/factories, Path, 3D and legacy metric classes
are untouched. `DrawablesTest` explicitly records the rich Geo as non-drawable.
GeoCeDG-owned existing edits are limited to `GeoLocusV2`, its integration test
and the developer laboratory.

Classic and public GeoCeDG `Locus[...]`, `Length` and `Perimeter` remain
unchanged. No external numerical dependency is added. Restricted legacy/model
originals are neither changed nor packaged as metric dependencies.

The opt-in Desktop laboratory exposes disposable total and between metrics,
the scalar adapter, result axes, diagnostics and functional counters. Closing
the diagnostic window removes those temporary algorithms. Packaging manifests
gain no public feature, command, XML type, external library or restricted model;
the composed packaging/static gates therefore remain unchanged.

## 10. Verification record

| Command | Result | Log/evidence |
|---|---|---|
| entry `verify-operational.ps1` | PASS (exit 0) | preflight console; controlled manifest reproduced |
| entry `verify-locus-v2.ps1` | PASS (exit 0) | `artifacts/validation/g7b-entry-locus-v2` |
| productive G7B tests and Checkstyle | PASS (exit 0), 62/62 | `artifacts/validation/g7b-final/g7b/g7b-common.log` |
| Desktop laboratory tests and Checkstyle | PASS (exit 0), 3/3 | `artifacts/validation/g7b-final/g7b/g7b-laboratory.log` |
| `verify-g7a-metrics.ps1` full regression | PASS (exit 0), 37+14 | `artifacts/validation/g7b-final/g7a/g7a-metric-characterization-gradle.log` |
| final G7A/R1 hashes, references and links | PASS (exit 0) | `verify-g7a-metrics.ps1 -SkipBuild`; 13 linked documents |
| `verify-g7b-metrics.ps1` | PASS (exit 0), 62+3 and zero Checkstyle findings | `artifacts/validation/g7b-final/g7b` |
| `verify-locus-v2.ps1` full regression | PASS (exit 0), 73+3 | `artifacts/validation/g7b-final/locus-v2/g6-locus-v2-gradle.log`; `g6r-laboratory-gradle.log` |
| final `verify-operational.ps1` | PASS (exit 0), 183 controlled files | console |
| `verify.ps1 -SkipBuild` | PASS (exit 0); builds evidenced separately above | `artifacts/validation/g7b-final/composed` |
| internal links and evidence hashes | PASS (exit 0) | G7A and G7B focused verifiers |
| `git diff --check`, productive/source and residual-process audits | PASS (exit 0) | 84 productive files, one upstream-owned; no Java/Gradle/GeoCeDG process |

### 10.1 Final author-review closeout revalidation

The bounded documentary closeout was revalidated from productive implementation
commit `92b0684074ef328039946f724d4aa951f70e21ec`:

| Command/audit | Closeout result | Log/evidence |
|---|---|---|
| authoritative G7B JUnit inventory | PASS: value 8, route 11, numerical 17, improper 6, lifecycle 11, benchmark 7, nested 2; productive total 62; laboratory 3; zero failures/errors | focused verifier plus generated Gradle/JUnit XML |
| `verify-g7b-metrics.ps1` | PASS (exit 0), 62+3 and zero Checkstyle findings | `artifacts/validation/g7b-closeout/g7b` |
| `verify-g7a-metrics.ps1` regression | PASS (exit 0), 37 original + 14 R1; independent references and Checkstyle pass | `artifacts/validation/g7b-closeout/g7a` |
| `verify-locus-v2.ps1` | PASS (exit 0), G6A/G6B/G6R plus 3/3 laboratory contracts | `artifacts/validation/g7b-closeout/locus-v2` |
| `verify-operational.ps1` | PASS (exit 0), 183 controlled files | console |
| `verify.ps1 -SkipBuild` | PASS (exit 0); delegates include operational, workstation, G6, G7A and G7B | `artifacts/validation/g7b-closeout/composed` |
| links and G7A/G7B evidence hashes | PASS (exit 0) | focused verifiers |
| closeout productive-source delta | PASS: zero `source/**/src/main/**` changes from the implementation commit | Git audit |
| `git diff --check` and residual-process audit | PASS (exit 0); zero Java/Gradle/GeoCeDG processes | Git/PowerShell audit |

Initial sandboxed attempts that required Conda or a Gradle distribution ended
before executing the relevant probes because workstation resources/network were
blocked. The same commands were rerun with normal workstation access and are
the PASS evidence recorded above; no project-code workaround was made.

## 11. Limitations and disposition

- The API is internal and experimental; there is no public construction path.
- Analytic/differential support is capability-injected; the laboratory uses
  evaluator-only semantics and therefore normally reports uncertified values.
- Capacity 64 is an initial provisional, non-normative implementation default;
  the author approval does not stabilize it. Wall-clock data is not a gate.
- Productive retained bytes are a deterministic logical estimate; no JVM heap
  or object-layout measurement was performed.
- Public commands, XML/persistence, Path, 3D, G8 and G9 remain absent.
- No unresolved implementation or validation blocker remains. The author
  approved G7B and closed G7.

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED

G7B = PASS — AUTHOR APPROVED
G7 = PASS

G7 METRIC SPEC = NORMATIVE
ADR 0007 = ACCEPTED

G7B CAPACITY 64 =
PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT

G7B PUBLIC COMMAND = ABSENT
G7B XML/PERSISTENCE = ABSENT
G7B PUBLIC PATH = ABSENT
G7B 3D = ABSENT

G8 = NOT STARTED
G9 = NOT STARTED
```

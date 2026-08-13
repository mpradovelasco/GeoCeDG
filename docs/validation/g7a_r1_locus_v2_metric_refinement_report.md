# G7A-R1 Locus V2 metric focused refinement report

- Status: **PASS — AUTHOR APPROVED**
- Parent phase: **G7A CHARACTERIZATION ONLY**
- Branch: `feature/g7a-locus-v2-metric-characterization`
- Planning/entry HEAD: `e918846a73829032ab1e1aff37e863fed40c1969`
- R1 request SHA-256:
  `af90f2209cbbbead33b0dcca37e9e8f426236f20ec99bcda3e0965482512a055`
- Date: 2026-08-13

This is the bounded refinement requested by the first formal author review.
Final closeout approves it, accepts
[ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md), and promotes
the [G7 metric contract](../../geocedg/specs/locus/locus-v2-metrics.md) to
normative status. It does not implement or start G7B.

## 1. Entry state and scope

The entry state was confirmed before R1 edits:

```text
G6 = PASS
G6R = PASS

G7 PLANNING = APPROVED

G7A =
READY FOR SECOND AUTHOR REVIEW

G7B = NOT STARTED
G8 = NOT STARTED

ADR 0007 = PROPOSED
G7 SPEC = PROPOSED / NOT NORMATIVE
```

The ancestry is
`origin/main@726abd9 -> planning@e918846 -> current feature branch`. The
executed G7A prompt remains hash-pinned at
`4820bf0934b84f3ea84ec5f30930a0be56769c150940ce53483e1150232fab39`.
The existing evidence records 37/37 G7A probes passing and the productive
source audit found zero G7 changes under `source/**/src/main/**`.

The worktree contained the expected uncommitted G7A characterization package
from the immediately preceding execution. The pre-existing root note
`ChatGPT-Planificación desarrollo GeoCeDG.md` remains unused as evidence and
unmodified. No lost workstation result was used.

R1 changed only test-private models/probes, evidence, documentation and the
subordinate verifier. The total-variation, route, multiplicity, rich-result,
GeoClass, explicit-adapter and public-boundary decisions listed as closed by
the author were not redesigned.

## 2. Productive-source audit relevant to R1

The source audit established these concrete extension facts:

- G6 already defines the exact required guarantee vocabulary in
  `LocusSemanticMetadata2D.NumericGuarantee`: `EXACT_ARITHMETIC`,
  `CERTIFIED_ERROR_BOUND`, `ESTIMATED_ERROR` and
  `FLOATING_POINT_UNCERTIFIED`. A second metric enum would be incompatible and
  is rejected.
- `GeoLocusV2.publishSemanticDefinition()` is the coherent revision
  publication seam; `setUndefined()` is the undefined transition.
- `GeoElement.doRemove()` and `AlgoElement.remove()` provide deterministic
  locus/consumer release hooks in the normal kernel lifecycle.
- `LocusEvaluationSession2D` demonstrates a GeoCeDG-owned bounded disposable
  derived service with insertion-order eviction, coherent revisions, explicit
  close and no dependency ownership.
- `LocusRenderCache2D` is explicitly per-Drawable and view-specific. It is not
  a metric-owner pattern and remains forbidden.
- `Construction` has no narrow existing metric-service extension seam. A
  Construction-wide repository would require broader retention and cleanup
  changes than a per-locus owner.

These facts support one small GeoCeDG-owned owner associated with the source
locus lifecycle, rather than a global/static service, a render cache, or a
second dependency graph.

## 3. Safe metric value representation

### 3.1 Compared alternatives

| Option | Safety result |
|---|---|
| V1: kind plus `OptionalDouble` | avoids NaN but permits contradictory states such as `FINITE` plus empty, unless every constructor duplicates validation |
| V2: closed immutable value | makes `Finite`, `PositiveInfinity` and `Absent` exhaustive and mutually exclusive; only `Finite` exposes an `OptionalDouble` value |
| V3: result/contribution subtypes | can be safe, but duplicates all non-value axes across result types and complicates aggregation/Geo publication without source evidence of a benefit |

The author accepts V2:

```text
sealed MetricValue2D
    FiniteMetricValue2D(nonNegativeFiniteValue)
    PositiveInfinityMetricValue2D
    AbsentMetricValue2D
```

`LocusMetricContribution2D` and `LocusMetricResult2D` contain exactly one
`MetricValue2D`. They do not expose a bare `double getValue()`. The scalar
adapter calls `finiteValue()` only after the ordinary admissibility predicate.
No normal state uses null, NaN, infinity in a finite field, magic values or an
exception.

```text
METRIC_VALUE_REPRESENTATION = AUTHOR_APPROVED
```

## 4. Safe error/evidence representation and G6 alignment

The author accepts a metric-specific immutable evidence wrapper that directly
contains, rather than redefines, the normative G6 guarantee:

```text
MetricErrorEvidence2D
    Optional<LocusSemanticMetadata2D.NumericGuarantee>
    absoluteEvidence: MetricErrorAmount2D
    relativeEvidence: MetricErrorAmount2D
    scope
    method
    assumptions[]
    certificateMetadata?

sealed MetricErrorAmount2D
    EstablishedMetricErrorAmount2D(non-negative finite amount)
    NotEstablishedMetricErrorAmount2D
    NotApplicableMetricErrorAmount2D

MetricErrorEvidenceScope
    COMPLETE_VALUE
    REPORTED_PARTIAL_VALUE
    NOT_APPLICABLE
```

The optional guarantee is empty only when error evidence is not applicable,
for example an absent value or established positive infinity. It is not a
fifth guarantee category. This preserves the G6 type exactly and prevents
`NOT_APPLICABLE` from becoming an incompatible duplicate enum value.
The closed hierarchy also makes a contradictory state plus `OptionalDouble`
pair impossible. It admits no NaN, `-1`, magic zero or `null`.

Aggregation rules characterized by the probes are:

- exact finite contributions remain `EXACT_ARITHMETIC`, with established zero
  absolute error and relative zero only when relative error is applicable;
- certified absolute bounds add; the aggregate remains certified only while
  every contributing finite term is exact or certified;
- estimated errors add and retain assumptions; exact/certified plus estimated
  becomes estimated;
- any uncertified finite contribution makes aggregate numeric evidence
  `FLOATING_POINT_UNCERTIFIED`, with amounts `NOT_ESTABLISHED`;
- positive infinity and absent/unsupported values have `NOT_APPLICABLE`
  numeric error evidence;
- error for a known finite subtotal in an incomplete aggregate is explicitly
  scoped `REPORTED_PARTIAL_VALUE`, never presented as an error for the unknown
  complete total.

Scalar-admissible guarantees remain exactly:

```text
EXACT_ARITHMETIC
CERTIFIED_ERROR_BOUND
ESTIMATED_ERROR
```

`FLOATING_POINT_UNCERTIFIED` remains rich-only by default.

## 5. Deterministic integration work budget

Depth 22 is retained as one policy dimension, but it is no longer the only
guard. The tested value is:

```text
MetricWorkBudget2D
    maximumEvaluations
    maximumSubdivisions
    maximumDepth
```

Each counter is deterministic and checked before additional work. Exhaustion
is a normal typed outcome: value `Absent`, status `LIMIT_NOT_ESTABLISHED`, no
applicable error evidence, and a diagnostic naming the exhausted dimension.
Evaluator exceptions remain `NUMERICAL_FAILURE`. Wall-clock time is never a
metric authority.

Using the accepted initial tolerance candidate (`1e-10`, `1e-9`) produced:

| Fixture | Evaluations | Subdivisions | Outcome |
|---|---:|---:|---|
| ellipse | 497 | 247 | `SUCCESS` |
| parabola | 233 | 115 | `SUCCESS` |
| transcendental | 65 | 31 | `SUCCESS` |
| cusp | 233 | 115 | `SUCCESS` |
| regular exponential reparameterization | 129 | 63 | `SUCCESS` |
| endpoint-degenerate `u^3` | 5 | 1 | `SUCCESS` |
| finite improper transform | 5 | 1 | `SUCCESS` |
| difficult improper transform | 2493 | 1245 | `LIMIT_NOT_ESTABLISHED`, depth exhausted |
| difficult evaluator-only trace | 4162 calls | n/a | uncertified evidence trace |

Independent tests exhausted evaluations, subdivisions and depth separately.
The author-approved initial G7B policy is:

```text
eps_metric_abs = 1e-10
eps_metric_rel = 1e-9
maximumMetricEvaluations = 32768
maximumMetricSubdivisions = 16384
maximumAdaptiveDepth = 22
```

The smallest observed margin is about 7.8 times the measured evaluator-only
work; subdivision margin is over 13 times the observed maximum. These are
versioned initial implementation defaults, not mathematical constants. All
three ceilings are part of policy identity and the
complete index key.

The first R1 run expected every transformed improper fixture to succeed. The
difficult fixture correctly reached depth 22 instead. It was reclassified as
typed insufficient evidence; no limit was increased to force success.

## 6. Multi-consumer ownership measurements

All strategies executed the same immutable component builder. The harness uses
512 bytes of component payload, 192 bytes of entry metadata, 256 bytes per
owner and 128 bytes per consumer as explicit comparative accounting constants.
They are not a Java heap-size claim.

### 6.1 Compatible consumers over one complete key

| Consumers | M0 local builds | M0 duplicate builds | M0 retained bytes | Shared builds | Shared cross-result hits | Shared retained bytes |
|---:|---:|---:|---:|---:|---:|---:|
| 1 | 1 | 0 | 1088 | 1 | 0 | 1088 |
| 3 | 3 | 2 | 3264 | 1 | 2 | 1344 |
| 10 | 10 | 9 | 10880 | 1 | 9 | 2240 |
| 100 | 100 | 99 | 108800 | 1 | 99 | 13760 |

At N=100, M0 retains 512 bytes of unique payload plus 50,688 duplicate
payload bytes, 19,200 entry-metadata bytes and 25,600 owner bytes. This is
explicitly classified:

```text
CROSS_RESULT_DUPLICATE_METRIC_WORK
```

All three shared harness alternatives reach one build and N-1 cross-result
hits. Ownership/lifecycle, not arithmetic output, distinguishes them.

### 6.2 Query-order, key and isolation results

For components C1/C2/C3, both
`M1 -> M2 -> M3(total) -> M4` and
`M3(total) -> M4 -> M2 -> M1` produced exactly three unique builds, three
cross-result hits and identical semantic values.

Sharing occurred only for the complete key. Independent changes to evaluator
capability, metric algorithm, tolerance, multiplicity, improper policy or
work budget each produced a miss/build. Different loci in one Construction
and equivalent-looking locus IDs in different Constructions produced zero
sharing.

Revision, split, merge, disappearance/reappearance and undefined/recovery
tests removed old entries. After each transition, the first compatible
consumer built once and the second hit the shared entry. No coordinate repair,
component lineage reuse or obsolete revision retention occurred.

### 6.3 Nested multi-consumer result

The bounded fixture

```text
L1 -> M1a, M1b, M1total -> L2 -> M2a, M2total -> L3
```

produced two unique component builds, three cross-result hits and zero
duplicate compatible builds. It also preserved:

```text
render reads = 0
legacy sample reads = 0
whole-locus regeneration = 0
index build inside downstream point = 0
```

All ownership strategies returned the same values as the local/reference
oracle.

## 7. Ownership alternatives and accepted decision

| Criterion | M0 algo local | M1 locus-attached index | M2 Construction repository | M3 dedicated shared owner |
|---|---|---|---|---|
| semantic correctness | pass | pass if strictly derived | pass with complete construction-scoped key | pass |
| normal DAG/no second DAG | pass | pass | pass | pass; Algos still depend directly on locus |
| cross-result reuse | fail: N builds | pass | pass | pass: one build/key |
| bounded memory | multiplied per Algo | one capacity/locus | one broad capacity/Construction | one capacity/active locus |
| deterministic invalidation | local simple | coupled to Geo fields | broad locus-selective cleanup | revision-local service cleanup |
| lifecycle safety | local simple | derived state contaminates Geo lifecycle/copy surface | Construction retention and multi-locus collision pressure | explicit acquire/release plus locus invalidation/removal |
| no global/static state | pass | pass | pass | pass |
| nested behavior | duplicate outputs | pass | pass, but broad owner | pass |
| upstream modification surface | low | changes semantic Geo internals substantially | changes central `Construction` | narrow GeoCeDG locus/algo hooks |
| reference-off testability | pass | pass | pass | pass |

M1 is rejected as the final shape because the index itself would become a
field-level concern of the semantic Geo, increasing copy/set and semantic
state coupling. M2 is rejected because the real `Construction` lacks a narrow
derived-service seam; it broadens central retention, invalidation and capacity
competition across unrelated loci.

The author accepts M3:

```text
MULTI_METRIC_OWNER =
DEDICATED_SHARED_OWNER
```

`LocusMetricSharedOwner2D` is a GeoCeDG-owned, non-GeoElement,
kernel-thread-confined service acquired through one source-locus lifecycle
hook. It contains only immutable derived `LocusMetricComponentState2D` entries
and counters. It does not resolve routes, share contributions or query results,
aggregate queries, publish results, mutate semantic
revision, own dependency edges or call between metric algorithms.

Each `AlgoLocusMetricV2` remains a normal direct dependent of `GeoLocusV2`.
The Algo acquires a consumer token and releases it in its removal lifecycle.
The last consumer clears entries and releases the owner. Locus revision,
undefined and removal clear the owner synchronously. Copy creates no owner;
undo/redo reacquires through reconstructed normal DAG algorithms.

The capacity is per active locus owner, not per metric result and not global.
Capacity 64 remains provisional and non-normative. Insertion-order eviction
remains the deterministic minimum; wall-clock LRU is forbidden.

### 7.1 Final owner/result API normalizations

The reusable owner boundary is:

```text
complete component key
    -> get/build immutable LocusMetricComponentState2D

LocusMetricComponentState2D + LocusMetricRouteSegment2D
    -> route/component evaluation
        -> LocusMetricContribution2D
```

The key contains no A/B endpoints. A total query evaluates the complete
component extent; a between-position query evaluates each route subarc. One
state can produce many contributions, but the owner shares no route,
contribution, query result or aggregate result. The hard budget is one
component-state build per complete compatible key until eviction/invalidation.

`LocusMetricResult2D` uses `Optional<TraversalOutcome>` or an equivalent result
type separation. Between-position outcomes remain mandatory where applicable;
total results contain no traversal outcome and use no null or sentinel.

## 8. Atomic failure/publication rule

R1 resolves the previous wording conflict; the author accepts P1:

```text
P1 = publish one coherent rich failure snapshot for the current revision
```

The exact sequence is:

1. source revision `r+1` makes the successful payload and index entries from
   `r` non-current before new downstream consumption;
2. metric component/index construction occurs in private local state;
3. success atomically publishes a complete immutable index entry, then one
   coherent rich result for `r+1`;
4. a handled failure publishes no index entry and publishes one coherent
   `Absent` rich failure snapshot for `r+1`;
5. the scalar adapter is undefined.

Thus “failed build publishes nothing” applies only to the index entry. It does
not mean the Geo may retain revision `r` as current. There is no old-value/new-
status hybrid, stale success or partially valid entry. An exceptional failure
that prevents even failure-snapshot construction leaves the result explicitly
non-current/undefined; it still cannot expose `r` as current.

## 9. Exact candidate G7B source impact

New GeoCeDG-owned candidate files:

```text
metric/MetricValue2D.java and its closed variants
metric/MetricErrorAmount2D.java
metric/MetricErrorEvidence2D.java
metric/MetricWorkBudget2D.java
metric/LocusMetricSharedOwner2D.java
metric/LocusMetricIndex2D.java
metric/LocusMetricIndexKey2D.java
metric/LocusMetricComponentState2D.java
metric/LocusMetricComponentStateBuilder2D.java
metric/LocusMetricComponentEvaluator2D.java
metric/LocusMetricContribution2D.java
metric/LocusMetricResult2D.java
the previously characterized query/route/integrator/aggregator values
geos/GeoLocusMetricResult.java
algos/AlgoLocusMetricV2.java
algos/AlgoLocusMetricScalarAdapter.java
```

Narrow unavoidable G7B edits:

- `GeoLocusV2`: acquire/release the dedicated derived owner; clear it on
  revision publication, undefined and `doRemove()`; copies never inherit it;
- `AlgoLocusMetricV2`: normal DAG input/output plus owner consumer-token
  acquire/release and P1 publication;
- `GeoClass`: append `LOCUS_METRIC_RESULT` and update the already audited
  exhaustive switches/tests;
- developer laboratory wiring only.

No `Construction` repository, render change, public command, XML/factory,
Path, 3D or G8 source is required by the accepted ownership.

## 10. Mandatory R1 decision table

Every R1-1..R1-22 row is `APPROVED` by the author, subject to the three final
API normalizations recorded in §7.1 and §4.

| # | Decision | Author-approved R1 decision |
|---:|---|---|
| R1-1 | metric finite/inf/absent value representation | V2 closed immutable `MetricValue2D`; finite access only through `OptionalDouble` |
| R1-2 | metric error evidence representation | immutable evidence wrapper with typed amount states, scope, method, assumptions and certificate metadata |
| R1-3 | NumericGuarantee alignment with G6 | directly reuse `LocusSemanticMetadata2D.NumericGuarantee`; no second enum |
| R1-4 | work-budget shape | evaluations + subdivisions + depth, independently counted |
| R1-5 | initial work-budget defaults | 32768 evaluations, 16384 subdivisions, depth 22 with accepted initial tolerances; implementation policy, not mathematical constants |
| R1-6 | ALGO_LOCAL cross-result cost | N compatible consumers produce N builds and N-1 duplicate payloads |
| R1-7 | LOCUS_ATTACHED candidate | reaches sharing but rejects direct index-in-Geo coupling |
| R1-8 | CONSTRUCTION_SCOPED candidate | reaches sharing but rejected for central retention/surface/capacity coupling |
| R1-9 | DEDICATED_SHARED_OWNER candidate | passes one-build, lifecycle, isolation, nesting and reference-oracle gates |
| R1-10 | selected multi-metric ownership architecture | `DEDICATED_SHARED_OWNER`; shares only immutable component metric state |
| R1-11 | cross-result unique-build hard budget | one component-state build per complete key until eviction/invalidation |
| R1-12 | memory/capacity ownership | one bounded capacity per active locus owner; never capacity per Algo |
| R1-13 | eviction policy | deterministic insertion order; provisional capacity 64; no time-based LRU |
| R1-14 | revision invalidation | clear/make unreachable all old revision entries synchronously; rebuild once then share |
| R1-15 | topology invalidation | split/merge/disappearance use new revision-scoped component keys; no old reuse |
| R1-16 | multi-Construction isolation | owner belongs to one concrete locus in one Construction; zero cross-Construction sharing |
| R1-17 | remove/undo lifecycle | release at last consumer/locus removal; undo/redo rebuilds via normal DAG |
| R1-18 | nested multi-consumer behavior | zero duplicate compatible builds and all previous forbidden counters remain zero |
| R1-19 | atomic failure publication | P1 coherent current-revision rich failure; no stale success; scalar undefined |
| R1-20 | ADR 0007 final disposition | `Accepted`; index strategy remains separate from dedicated ownership |
| R1-21 | G7 spec final disposition | normative/author-approved with R1 and closeout contracts incorporated |
| R1-22 | exact G7B source impact after optimized ownership | new GeoCeDG metric service/values plus narrow `GeoLocusV2`, Algo, GeoClass and laboratory edits; no Construction repository |

## 11. Evidence and disposition

The raw evidence is
[`g7a-r1-characterization-evidence.json`](../../geocedg/validation/locus-v2/g7a-r1/g7a-r1-characterization-evidence.json).
The test-private R1 suite contains 14 tests: six value/error/work-budget tests
and eight ownership tests. The existing 37 G7A probes remain the regression
authority.

Final closeout validation:

| Command/gate | Exit | Result / saved log |
|---|---:|---|
| `tools/agent/verify-g7a-metrics.ps1` | 0 | 37 original + 14 R1 tests, 0 failures/skips; independent references, links, hashes and checkstyle pass; `artifacts/validation/g7a-author-closeout-focused/` |
| `tools/agent/verify-locus-v2.ps1` | 0 | 76 G6A/G6B/G6R/laboratory tests, 0 failures; common/Desktop checkstyle pass; `artifacts/validation/g7a-author-closeout-locus-v2/` |
| `tools/agent/verify-operational.ps1` | 0 | operational contracts pass; 94 controlled upstream files |
| `tools/agent/verify.ps1 -SkipBuild` | 0 | all composed operational/workstation/legacy/DXF/Locus/G7A/packaging/baseline/frontend gates pass; `artifacts/validation/g7a-author-closeout-composed/` |
| `git diff --check` and cached check | 0 | no whitespace error; only configured LF→CRLF notices |
| productive-source audit | 0 | zero diff and zero metric-class match under `source/**/src/main/**` from planning SHA |
| residual process audit | 0 | no Java or Gradle process remains |

The first sandboxed focused and Locus V2 closeout invocations stopped on Conda
environment-write and Gradle distribution-network restrictions, respectively.
Exact managed-access reruns passed. These were environment failures; no source
or expectation was changed in response.

The first full focused attempt ran all 51 tests successfully but the wrapper
correctly returned nonzero for six test-source checkstyle warnings introduced
by the R1 type/import changes. Only test-private import order, declaration and
line wrapping were corrected. A fresh full rerun then passed all tests and
checkstyle. No test expectation or measured value was weakened.

The versioned
[`g7a-r1-evidence.sha256`](../../geocedg/validation/locus-v2/g7a-r1/g7a-r1-evidence.sha256)
pins the raw evidence, report, focused verifier and all 11 G7A/R1 test-private
Java sources.

```text
MULTI-METRIC OPTIMIZATION =
CHARACTERIZED

ACCEPTED OWNERSHIP =
DEDICATED_SHARED_OWNER

CROSS-RESULT COMPATIBLE BUILD BUDGET =
ONE COMPONENT-STATE BUILD PER COMPLETE KEY UNTIL EVICTION OR INVALIDATION

PRODUCTIVE G7 SOURCE CHANGES =
0

G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED

G7B = AUTHORIZED / NOT STARTED
G8 = NOT STARTED

ADR 0007 = ACCEPTED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
```

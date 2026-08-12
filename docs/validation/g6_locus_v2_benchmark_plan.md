# G6 Locus V2 benchmark plan

| Field | Value |
|---|---|
| Status | **G6 PASS / G6R PASS** — distributions measured; functional gates retained |
| Baseline | GeoGebra 5.4.928.0 / GeoCeDG G6A `PASS` |
| Date | 2026-08-12 |
| Principle | Measure semantic evaluation separately from graphical tessellation |

G6A has captured a controlled legacy/semantic-fixture baseline on a recorded
workstation. It is not a product benchmark and does not justify an absolute
timing gate. Every proposed optimization must cite one of the cases and metrics
below; absolute latency budgets remain informational pending G6B measurements.

## 1. Existing operational benchmark boundary

`tools/benchmark/run.ps1` is a G1 operational-smoke runner. Its schema version
1 launches PowerShell verifier scripts in new processes, records wall-clock
durations and requires `budget_mode: informational`. It cannot directly count
kernel evaluations, isolate recompute from render, inspect cache behavior or
measure per-locus allocations.

G6A preserves that runner and its authority. The implemented minimum is:

1. focused Java/JUnit characterization probes in the existing common-jre test
   environment, emitting named metrics and deterministic assertions;
2. a versioned JSON-compatible summary under `geocedg/validation/locus-v2/`;
3. subordinate `tools/agent/verify-locus-v2.ps1`, invoked by the composed
   authority, which validates contracts and reruns the bounded tests.

A new benchmark-suite schema or JMH dependency was not justified. Raw Gradle
logs/XML are regenerable and remain outside version control; the versioned
summary records the review evidence.

G6R executed depth 1/2/3/5 distributions with session on/off, repeated keys,
capacity/eviction, innermost invalidation, branch-component changes, render
cold/warm policies and the segment provider. The durable summary is
[`g6r-hardening-evidence.yml`](../../geocedg/validation/locus-v2/g6r-hardening-evidence.yml).
It preserves `q*d` and zero forbidden-work counts as hard gates. Absolute time
and retained-memory values remain informational because repeatability/noise was
not sufficient to approve hard limits.

Measured adaptive visual tessellation was accepted for normal rendering while
uniform sampling remains the reference. Session capacity/eviction was not
changed, and DAG flattening was not justified.

Do not silently turn G1's informational schema into a hard performance gate.
If enforceable budgets require schema version 2, a future task must specify,
validate and review that contract before use. JMH or another dependency is not justified
unless the focused probe demonstrates measurement noise that the existing JVM
test harness cannot control.

## 2. Measurement protocol

Each saved run must record:

- Git revision and dirty status;
- model/fixture ID and SHA-256;
- Java runtime and Desktop/shared toolchain versions;
- OS, architecture, CPU logical count and available memory;
- headless/render mode and view dimensions/DPI where rendering is measured;
- warm-up count, measured repetitions, sample/evaluation request count;
- feature mode (`LEGACY`, `V2`, `DUAL`), cache mode and semantic revision;
- nesting depth, outer query count, evaluation-session mode and semantic
  revisions/branch keys at every participating locus;
- all tolerance and tessellation-policy identifiers;
- median, minimum, maximum and a dispersion statistic (prefer p95 or median
  absolute deviation) for timings;
- timeout, invalid evaluation and cache-eviction counts.

Run measurements after a fixed warm-up. Use the same process mode for compared
runs, avoid unrelated builds in the timed region, and preserve raw per-run
values. A single elapsed time is not sufficient evidence.

## 3. Metrics

### 3.1 Semantic/kernel metrics

| Metric | Definition |
|---|---|
| `semantic_recompute_ms` | Time for normal dependency change through publication of a new immutable V2 definition revision; excludes drawing |
| `evaluation_throughput_per_s` | Valid `evaluate(branchKey,t)` calls completed per second over a fixed deterministic semantic-parameter set |
| `evaluation_latency_ns` | Distribution for cold and warm evaluator queries |
| `dependency_updates_per_evaluation` | Number of cloned construction updates needed for one semantic query |
| `evaluation_count` | Total evaluator calls issued by the operation under test |
| `outer_query_count` | Requests made to the outermost locus in a nested fixture |
| `evaluator_call_count_by_level` | Semantic evaluator invocations attributed to each nested locus identity/revision |
| `duplicated_upstream_evaluation_count` | Identical upstream semantic keys evaluated more than once inside one eligible session/batch |
| `dependency_slice_build_count` | New cloned/compiled dependency slices or plans constructed in the measured operation |
| `dependency_slice_synchronization_count` | Slice reset/update/synchronization operations, attributed by locus level |
| `invalid_evaluation_count` | Results by explicit invalid status, not exceptions hidden as points |
| `semantic_cache_or_session_hits/misses/evictions` | Bounded cache/session behavior for a fixed query sequence and full semantic key |
| `semantic_retained_bytes` | Approximate retained memory of definition/evaluation cache after a forced stable state; method documented |

### 3.2 Render metrics

| Metric | Definition |
|---|---|
| `render_tessellation_ms` | Time to build a view-local tessellation for a fixed revision/view policy |
| `render_evaluation_count` | Evaluator calls requested only by tessellation |
| `render_vertex_count` | Vertices/moves in the derived cache |
| `render_cache_bytes` | Approximate retained view-cache memory |
| `repaint_reuse_ms` | Repaint using a valid render cache |
| `zoom_rebuild_ms` | Tessellation rebuild after view transform changes |

### 3.3 Zoom sensitivity

For fixed semantic queries before/after a zoom change:

- declared/valid domains, branch keys, semantic revision and coordinates must
  remain equal;
- `semantic_recompute_ms` and semantic evaluation count should not be triggered
  merely by view change in V2;
- render time/evaluation/vertex count may change and are measured separately.

Legacy comparison should record that its sample count and locus recompute can
change with the view. That is characterization, not a performance target to
preserve.

## 4. Benchmark case suite

| ID | Case | Purpose | G6A legacy baseline | G6B comparison |
|---|---|---|---|---|
| `BM-SIMPLE` | Line, circle and ellipse analytic loci | Evaluator overhead and low-cost render | Sample/recompute/render at three zooms | Cold/warm evaluation, render, zoom separation |
| `BM-CHAIN` | Point depending on a controlled chain of 10/50/200 existing algorithms | Dependency-slice cost scaling | Legacy recompute and sample count | Recompute and evaluations/s versus chain length |
| `BM-MULTIBRANCH` | Explicit 2/4/8 branch fixture | Branch enumeration, cache key and render scaling | Legacy `MOVE_TO`/point-list behavior where comparable | Cost versus branches and requested parameters |
| `BM-CONCAT` | 1/2/3 concatenated loci reflecting the historical development workflow | Detect compounded recompute/filter cost | Legacy concatenation and `postLocus` observation | V2 branches without list filtering; no length metric |
| `BM-NESTED-1` | One pointwise deterministic semantic locus | Base cost for the nested family | Comparable simple legacy locus | One outer query equals one level-1 semantic evaluation, excluding explicit render work |
| `BM-NESTED-2` | Locus consumes one semantic evaluation of `BM-NESTED-1` | Two-level composition and duplicate-call accounting | Compare conceptually with the stored working slices in the author-supplied pair | Small internal typed fixture: per-level calls, slice builds/synchronizations, session hits/misses and invalidation |
| `BM-NESTED-3` | Third semantic locus consumes level 2 | Required bounded three-level demonstrator | Preserve `InterCilConoOblique` `Flatten` as the pathological legacy reference | Small internal typed fixture: same counters; no render/sample dependency or upstream whole-locus regeneration; no G7 perimeter required |
| `BM-NESTED-5` | Five-level synthetic affine composition when fixture cost remains useful | Small depth-stress and scaling-shape evidence | Optional legacy comparison only if safely bounded | G6A characterization; G6B only if its approved functional budget includes it |
| `BM-NESTED-CEDG-2` | `InterCilConoObliqueTwoLevels.ggb`, hash-pinned | Real two-level sampled-locus/perimeter comparison | Load, source/driver recompute, slice composition | Informational legacy baseline; no V2 geometry inferred |
| `BM-NESTED-CEDG-3` | `InterCilConoOblique.ggb` plus document `Flatten` commands | Real third-level pathological comparison | Per-command creation, definition status, step timeout, post-create recompute and slice composition | G6B must avoid its full-inner-locus cascade by construction |
| `BM-OBLIQUE-CONE` | Curated oblique-cone development case from local scientific evidence | Required real CeDG stress/regression evidence | Recompute, sample count, timeout, memory, three zooms | Dual diagnostic only if deterministic G6B evaluator is approved |
| `BM-DISCRETE` | Small elbow/development with integer parameter values | Dynamic topology and invalidation cost | Recompute across approved integer values | Revision/branch invalidation and recompute; no optimization search |
| `BM-STRESS` | Generated dependency chain with configurable branches and sample requests | Capacity limit and cache discipline | Establish timeout/partial legacy behavior | Detect nonlinear allocation or hidden graph rebuild |

The stress fixture must have fixed seeds and bounded sizes in CI. The real CeDG
model must be locally curated with provenance; a public URL alone cannot become
a mandatory benchmark input.

`BM-NESTED-*` is an independent family. It is neither `BM-CHAIN` (ordinary
algorithm depth) nor `BM-CONCAT` (joining completed locus/sample groups). The
controlled reference functions are specified in the
[validation matrix](g6_locus_v2_validation_matrix.md). The author-supplied,
hash-pinned cone-cylinder pair now provides the real nested CeDG case; it
remains opt-in for expensive timing and mandatory for structural evidence.

## 5. Baseline experiments required in G6A

1. Build a read-only legacy probe that records sample count, recompute time,
   render time and timeout outcome without changing `GeoLocus`.
2. Run `BM-SIMPLE`, `BM-CHAIN`, `BM-MULTIBRANCH`, `BM-CONCAT`, the independent
   `BM-NESTED-1/2/3` family (and `BM-NESTED-5` when useful),
   `BM-OBLIQUE-CONE`, `BM-DISCRETE` and bounded `BM-STRESS` on the validated
   toolchain.
3. Repeat at fixed view scales representing zoom out, nominal and zoom in.
4. Separate construction recompute from drawable update; document any baseline
   hook that prevents clean separation.
5. Run forward, reverse and shuffled parameter sequences on the selected CeDG
   cases to detect history-dependent evaluation.
6. Capture the baseline's 500 ms step-budget and partial-result behavior.
7. Reproduce the author's legacy two-level nested observation and, when
   technically reproducible, the third level; record slice builds,
   resets/synchronizations, construction updates, sample/`Path` reads, repeated
   calls and render work separately before assigning a cause.
8. Compare recursive semantic evaluator composition with a scoped shared
   evaluation session against controlled DAG flattening/compilation, including
   a cache-disabled semantic reference and explicit cycle diagnostics.
9. Publish raw results under ignored `artifacts/benchmarks/` and a versioned
   summarized baseline/threshold contract under `geocedg/validation/` only
   after schema approval.

## 6. G6B measurement comparisons

For every supported benchmark, measure these configurations:

```text
LEGACY, normal cache
V2, evaluator cache disabled
V2, evaluator cache enabled
V2 nested, shared session disabled
V2 nested, scoped shared session enabled
V2, render cache cold
V2, render cache warm
DUAL, diagnostics enabled
```

Compare semantic throughput only between configurations that answer the same
approved query set. Do not compare legacy sample generation with a V2 point
query and call the ratio an optimization. Dual mode is diagnostic and may have
a separate overhead budget.

## 7. Budget approval method

G6 planning intentionally does not invent absolute millisecond limits. After
G6A baseline measurement, propose budgets as follows:

- **Compatibility gate:** with V2 disabled, legacy median/p95 and memory must
  remain inside the measured noise envelope plus an author-approved margin.
- **Semantic scale gate:** `BM-CHAIN` and `BM-MULTIBRANCH` must exhibit the
  predicted scaling; any superlinear term requires a documented explanation.
- **Nested composition gate:** nested composition must not introduce
  multiplicative/exponential work merely because one semantic V2 locus depends
  on another. For pointwise deterministic fixtures, upstream evaluations should
  grow approximately as `requested semantic evaluations * dependency depth`,
  plus measured fixed preparation/synchronization costs—never as the product of
  nested render-tessellation densities. Clearly superlinear growth with depth
  requires an explanation and blocks mechanism approval pending author review.
- **Interactive render gate:** cold and zoom-rebuild budgets are set from the
  validated Desktop frame target and measured workstation, not from the legacy
  500 ms partial-output timeout.
- **Cache gate:** retained memory is bounded by configuration; repeated novel
  parameters cause eviction rather than unbounded growth.
- **Dual-run gate:** an explicit larger diagnostic budget is acceptable, but
  dual mode remains disabled by default.

Every numeric threshold must include measurement date, environment, raw result
hash and author approval. CI should enforce only deterministic functional
properties initially; timing warnings remain informational until noise and
runner reproducibility are demonstrated.

## 8. Optimization decision table

| Candidate optimization | Required evidence | Initial disposition |
|---|---|---|
| Build dependency slice once per algorithm | `BM-CHAIN` shows per-evaluation rebuild cost | Architecturally preferred; verify correctness first |
| Bounded memoization of evaluations | Repeated-query hit rate and allocation benefit | Optional in G6B, disabled/reference mode required |
| Scoped nested evaluation session | `BM-NESTED-*` shows duplicated upstream requests or repeated synchronization and equal enabled/disabled results | Accepted minimum with full semantic key, bounded memoization and active-key cycle guard; final class name remains local to G6B |
| Controlled evaluation-DAG flattening/compilation | Recursive/session strategy remains a measured bottleneck and flattening preserves identities, revisions and normal invalidation | Defer unless evidence demonstrates necessity and localized impact |
| Interval bounds cache | G8 broad-phase or render benchmark need | Defer; not justified by G6 alone |
| Parallel evaluation | Thread-safe kernel evidence and material speedup | Reject for G6B; baseline is mutable/single-thread confined |
| Object pooling / mutable result reuse | Allocation profile identifies a material hotspot | Defer; immutable results aid correctness |
| Derivative-assisted render | Fewer evaluations with equal render error on analytic cases | Optional experiment only; not a required semantic capability |
| JMH dependency | Existing probe noise prevents reliable decisions | Defer; requires separate dependency/license review |

## 9. PASS evidence

### G6A benchmark PASS

- all cases have reproducible sources or an explicit documented exclusion;
- legacy baseline and call-order evidence are saved with provenance;
- legacy nested behavior is reproduced where technically possible and its
  measured mechanism is separated from the author's observational evidence;
- semantic and render metrics can be measured separately;
- nested per-level calls, duplicate calls, slice lifecycle and session/cache
  behavior can be measured separately;
- raw data and summary hashes agree;
- noise/dispersion is characterized;
- the author approves G6B functional sizes and budgets.

### G6B benchmark PASS

- V2-disabled legacy performance stays within its approved compatibility
  budget;
- V2 meets approved semantic/render/cache budgets on required cases;
- `BM-NESTED-1/2/3` passes with correct geometry, coherent revisions/keys,
  correct innermost invalidation, no upstream render/full-locus regeneration,
  equal session-enabled/disabled results and approved nonmultiplicative scaling;
- view changes cause render-cache work but no semantic recompute/evaluation
  change;
- no unbounded cache growth or residual evaluation process exists;
- any missed target is reported as a blocker, not hidden by a larger timeout.

## 10. G6A measured baseline

Environment: Windows 11 Pro `10.0.26200`, 24 logical processors,
34,186,194,944 bytes memory, PowerShell 7.6.4, Gradle 9.4.1 launched by Oracle
JDK 22.0.2, with common-jre tests on Corretto 17.0.10. These timings are one
run's controlled evidence; deterministic counts are the stronger gate.

| Case | Result |
|---|---|
| `BM-SIMPLE` legacy circle | 277 samples at `[-5,5]`, 160 at `[-100,100]`; sampled chord sums `20.565979779489272` and `20.565121501408928` |
| `BM-CHAIN-10/50/200` | creation 17.601/23.051/63.769 ms; dependency-slice algorithm counts 14/54/204; 160 locus samples in each measured view |
| legacy nested 1/2/3/5 | median source-update/recompute 0.559/0.946/1.268/1.211 ms; outer samples 160/154/168/222 |
| semantic `BM-NESTED-1/2/3/5` | five outer requests produced exactly 5/10/15/25 evaluator calls in recursive and flattened references |
| repeated nested depth 3 | no session: 18 calls; scoped session: 9 calls, 3 cache hits; identical results |
| cycle fixture | re-entry of the same full semantic key was detected before recursion |
| real `BM-NESTED-CEDG-2` | accepted functional control; stored outer slice: one inner locus + one sampled perimeter; measured source/driver recompute about 127/125 ms |
| real `BM-NESTED-CEDG-3` | accepted pathological reference; pre-`Flatten` state about 31.9 ms; each `Flatten` slice: two inner loci + two sampled perimeters; three creations about 6.03/5.95/5.67 s, all undefined after timeout; post-create recompute about 21.0 s |

The controlled affine/point-on-locus legacy chain did not reproduce the failure,
which establishes that nesting depth alone is not its cause. The real CeDG pair
did reproduce it: `AlgoLocusSliderND` cloned both inner locus algorithms and
both sampled perimeter algorithms into each outer macro slice, then invoked the
slice through `copyP.updateCascade()` for outer samples until the 500 ms
per-step guard fired. `BM-OBLIQUE-CONE`, `BM-DISCRETE`, real `BM-CONCAT`, render
timing and retained-memory measurements remain documented exclusions.

## 11. Author-approved G6B functional budgets

The recommended first gates avoid unstable wall-clock thresholds:

- for controlled pointwise nesting, evaluator calls without duplicate requests
  equal `outer_query_count * dependency_depth`;
- an exact semantic key is evaluated at most once per eligible scoped session;
- session-enabled and disabled coordinates/statuses are identical;
- dependency-slice construction occurs at most once per locus definition or
  semantic revision, never per point evaluation;
- no upstream render-cache access, tessellation or whole-locus regeneration is
  permitted for downstream semantic evaluation;
- a source change at the innermost level invalidates the normal declared chain
  once and clears derived semantic/session data;
- clearly superlinear growth with depth blocks closeout unless the author
  reviews a demonstrated cause.

Absolute latency, throughput, retained memory and render budgets remain
informational until G6B has a real implementation and repeatable distributions.
The functional gates above were accepted before implementation and are now
exercised by the completed G6B experimental implementation.

The original `.ggb` files remain manual/scientific legacy benchmarks. G6B uses
a sufficiently small internal typed reproduction, traced to both originals, to
measure at least three semantic levels, innermost invalidation, no
render/sample dependency, no whole-upstream-locus regeneration and bounded
functional scaling. It is not required to implement G7 `Perimeter` semantics.

## 12. G6B measured functional evidence

`LocusV2FunctionalBenchmarkTest` separates deterministic architectural counts
from informational wall-clock observations. Its controlled batch uses 64
unique outer parameters:

| Case | Depth | Required evaluator calls | Observed evaluator calls | Representative median elapsed time |
|---|---:|---:|---:|---:|
| `BM-NESTED-1` | 1 | 64 | 64 | 122,100 ns |
| `BM-NESTED-2` | 2 | 128 | 128 | 133,600 ns |
| `BM-NESTED-3` | 3 | 192 | 192 | 89,599 ns |
| `BM-NESTED-5` | 5 | 320 | 320 | 186,700 ns |

The timings are one local rerun and are deliberately not monotonic or a PASS
budget. They demonstrate why G6B gates call counts and lifecycle behavior, not
unstable nanosecond thresholds.

In the duplicate depth-three batch, 64 unique queries followed by the same 64
queries produce 192 evaluator calls, 192 misses, 64 hits and 192 retained
entries in a 256-entry session. The reference session returns equal semantic
results. A separate capacity-two fixture produces one deterministic eviction,
showing bounded storage.

The instrumentation records zero dependency-slice builds, zero slice
synchronizations, zero whole-locus regenerations and zero upstream render
evaluations for nested semantic work. These zeros describe the implemented V2
architecture: point evaluation has no dependency-slice or render operation to
invoke. They do not erase or reinterpret the measured legacy mechanism.

Render evidence is recorded separately: a fixed semantic definition produces
33 vertices under the coarse policy and 129 under the fine policy; its semantic
revision and evaluated coordinates are unchanged. The per-drawable render
cache retains at most four policy/revision entries.

The machine-readable authority for these counts and the documented geometric
scales `S` is
[`g6b-functional-evidence.yml`](../../geocedg/validation/locus-v2/g6b-functional-evidence.yml).
Retained JVM object-size measurement and absolute latency budgets remain
deferred because no stable allocation probe or repeatability threshold was
approved. This does not weaken the mandatory nonmultiplicative functional gate.

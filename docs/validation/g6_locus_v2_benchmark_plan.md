# G6 Locus V2 benchmark plan

| Field | Value |
|---|---|
| Status | **APPROVED AS G6A WORKING ARCHITECTURAL HYPOTHESIS**; budgets **DEFERRED TO G6A MEASUREMENTS** |
| Baseline | GeoGebra 5.4.928.0 / GeoCeDG G5 `PASS` |
| Date | 2026-08-11 |
| Principle | Measure semantic evaluation separately from graphical tessellation |

No performance result is asserted by this planning document. G6A must capture
the legacy baseline on a recorded workstation/toolchain before the author
approves G6B budgets. Every proposed optimization must cite one of the cases
and metrics below.

## 1. Existing operational benchmark boundary

`tools/benchmark/run.ps1` is a G1 operational-smoke runner. Its schema version
1 launches PowerShell verifier scripts in new processes, records wall-clock
durations and requires `budget_mode: informational`. It cannot directly count
kernel evaluations, isolate recompute from render, inspect cache behavior or
measure per-locus allocations.

G6A should preserve that runner and its authority. The smallest extension is:

1. a focused Java/JUnit benchmark probe in the existing common-jre test
   environment, with deterministic structured JSON output;
2. a subordinate `tools/agent/verify-locus-v2.ps1` that invokes the probe and
   validates its schema/provenance;
3. an optional G6 benchmark suite consumed by either a minimal compatible
   runner extension or a separate subordinate measurement script called by the
   verifier.

Do not silently turn G1's informational schema into a hard performance gate.
If enforceable budgets require schema version 2, G6A must specify, validate and
review that contract before use. JMH or another dependency is not justified
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
| `BM-NESTED-2` | Locus consumes one semantic evaluation of `BM-NESTED-1` | Two-level composition and duplicate-call accounting | Reproduce author's two-level legacy pattern when an equivalent artifact exists | Per-level calls, slice builds/synchronizations, session hits/misses and invalidation |
| `BM-NESTED-3` | Third semantic locus consumes level 2 | Required bounded three-level demonstrator | Attempt reproducible legacy third level and record timeout/practical failure without assuming cause | Same counters; no render/sample dependency or upstream whole-locus regeneration |
| `BM-NESTED-5` | Five-level synthetic affine composition when fixture cost remains useful | Small depth-stress and scaling-shape evidence | Optional legacy comparison only if safely bounded | G6A characterization; G6B only if its approved functional budget includes it |
| `BM-OBLIQUE-CONE` | Curated oblique-cone development case from local scientific evidence | Required real CeDG stress/regression evidence | Recompute, sample count, timeout, memory, three zooms | Dual diagnostic only if deterministic G6B evaluator is approved |
| `BM-DISCRETE` | Small elbow/development with integer parameter values | Dynamic topology and invalidation cost | Recompute across approved integer values | Revision/branch invalidation and recompute; no optimization search |
| `BM-STRESS` | Generated dependency chain with configurable branches and sample requests | Capacity limit and cache discipline | Establish timeout/partial legacy behavior | Detect nonlinear allocation or hidden graph rebuild |

The stress fixture must have fixed seeds and bounded sizes in CI. The real CeDG
model must be locally curated with provenance; a public URL alone cannot become
a mandatory benchmark input.

`BM-NESTED-*` is an independent family. It is neither `BM-CHAIN` (ordinary
algorithm depth) nor `BM-CONCAT` (joining completed locus/sample groups). The
controlled reference functions are specified in the
[validation matrix](g6_locus_v2_validation_matrix.md); a real nested CeDG case
is added only if G6A finds a reproducible, properly sourced artifact.

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
V2 nested, shared session enabled (if G6A approves the mechanism)
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
| Scoped nested evaluation session | `BM-NESTED-*` shows duplicated upstream requests or repeated synchronization and equal enabled/disabled results | Candidate minimum; final abstraction/name deferred to G6A |
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

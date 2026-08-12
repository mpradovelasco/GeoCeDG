# G7 Locus V2 metric benchmark plan

| Field | Value |
|---|---|
| Status | Planning only; no benchmark or probe has been run |
| G7A | `NOT STARTED` |
| G7B | `NOT STARTED` |
| Primary gates | Functional counters, semantic equality, bounded state and invalidation |
| Wall-clock | Initially informational |
| Matrix | [`g7_locus_v2_metric_validation_matrix.md`](g7_locus_v2_metric_validation_matrix.md) |
| Date | 2026-08-12 |

This plan defines reproducible experiments for a separately authorized G7A and
the hard functional budgets that G7A must propose for G7B. It does not create a
benchmark harness, expected constants or productive metric code.

## 1. Questions

The experiments must answer:

1. Which integration capabilities can produce truthful value/error metadata?
2. What independent `eps_metric_abs`/`eps_metric_rel` and stopping policies are
   defensible?
3. How do no-reuse, eager whole-revision and lazy component-revision strategies
   compare on the same semantic queries?
4. Does repeated and overlapping work reuse the same revision/policy without
   retaining obsolete revisions?
5. Does nested metric composition remain pointwise and DAG-driven without
   render/sample access, whole-locus regeneration or per-point index builds?
6. Are index ON/OFF outputs semantically identical?
7. Are capacity, eviction, exception cleanup and invalidation deterministic?
8. Which recomputations are necessary after a semantic change, and which are
   waste?

## 2. Measurement principles

Functional work is the first authority. Collect wall-clock only after counters
and semantic outputs are recorded. Initial hard gates are:

- exact equality of rich semantic axes/decomposition across index strategies,
  allowing only the approved numeric comparison policy for floating values;
- zero metric access to render and legacy-sample authorities;
- no redundant component/index rebuild for compatible repeated work;
- deterministic bounded retained state and eviction;
- complete invalidation with no obsolete revision retention;
- atomic failure behavior and cleanup;
- normal DAG recomputation in nested graphs.

Wall-clock medians, percentiles and dispersion are informative until the author
approves a stable environment-specific budget. Do not add JMH unless G7A
demonstrates a measurement question that the focused deterministic harness
cannot answer.

## 3. Strategies under test

Every query family runs under:

### `REFERENCE_NO_INDEX_REUSE`

No cross-query metric-index reuse. This is the cache-off semantic oracle.
Evaluator-session configuration must be explicit so its reuse is not confused
with metric-index reuse.

### `EAGER_WHOLE_REVISION`

First use builds every supported component in the captured revision.

### `LAZY_COMPONENT_REVISION`

First use builds only the requested component; compatible later queries may
reuse immutable component work.

All three use the same evaluator, route, integration, aggregation, policy and
result code path wherever possible. Strategy must be the only changed
experimental variable.

## 4. Instrumentation

### 4.1 Required counters

Per query and cumulative trace:

```text
semanticEvaluatorCalls
derivativeCalls
integratorCalls
subdivisions
routeResolutions
componentContributionsBuilt
componentIndexBuilds
wholeRevisionIndexBuilds
indexHits
indexMisses
indexEvictions
retainedIndexEntries
retainedSemanticRevisions
indexInvalidations
aggregateOperations
publishedResults
failedPrivateBuilds
renderCacheReads
renderVertexReads
legacySampleReads
wholeLocusRegenerations
metricIndexBuildsInsideDownstreamPointEvaluation
```

The final four forbidden/waste counters have a hard expected value of zero in
all V2 metric runs.

### 4.2 Timings

Record independently:

- route-resolution latency;
- component build/integration latency;
- aggregation latency;
- end-to-end query latency;
- invalidation/recovery latency;
- laboratory presentation latency only as a separate non-kernel measurement.

Use warm-up and measured iterations appropriate to the small harness. Save raw
samples, median, p90/p95 where sample size supports it, minimum/maximum and
dispersion. Never hide functional work behind one end-to-end duration.

### 4.3 State and provenance

Each trace records:

- locus identity and semantic revision;
- branch and resolved component keys;
- provider/evaluator capability version;
- algorithm, metric policy and tolerance-policy versions;
- multiplicity and improper-limit policies;
- strategy and capacity/eviction rule;
- query fields and resolved route summary;
- full rich result hash plus human-readable axes;
- machine, OS, CPU, memory, JDK, Gradle and baseline Git SHA;
- timestamp, repeat seed/order and raw-output hash.

## 5. Query sequence

For every family below, run cumulative sequences of exactly 1, 10 and 100
queries. Report the first query separately from queries 2–10 and 11–100 so a
large build cannot be hidden by averaging.

| ID | Query family | Required variation |
|---|---|---|
| Q-SAME | same A/B route | identical semantic positions/bindings/policy |
| Q-OVERLAP | overlapping arcs | nested and partially overlapping intervals in one component |
| Q-REVERSE | reverse | forward then reverse/complementary routes |
| Q-PERIODIC | periodic | seam-crossing arcs, zero and full cycle |
| Q-STOP | open STOP | reachable and boundary-stopped cases |
| Q-WRAP | explicit WRAP | two route segments, no seam chord |
| Q-STRICT | open STRICT | reachable and absent-unreachable cases |
| Q-TOTAL | repeated total | one, multi-component and multibranch |
| Q-TOLERANCE | tolerance change | alternate approved loose/tight abs/rel policies |
| Q-POLICY | policy change | multiplicity/improper/algorithm-policy version |
| Q-REVISION | revision change | value-only edit and topology/component change |

For `Q-TOLERANCE` and `Q-POLICY`, alternate values within the trace to expose
incomplete keys. A hit on an incompatible key is a hard failure.

## 6. Fixture sizes

Use small deterministic fixtures before scientific models:

| Fixture | Components/branches | Purpose |
|---|---:|---|
| `BM7-SEGMENT` | 1/1 | analytic route and zero overhead |
| `BM7-CIRCLE` | 1 periodic/1 | seam, full cycle, reverse |
| `BM7-ELLIPSE` | 1 periodic/1 | differential/evaluator integration |
| `BM7-GAP` | 2/1 | no gap crossing, total decomposition |
| `BM7-MULTIBRANCH` | 1 each/2,4,8 | constructive multiplicity and eager unused work |
| `BM7-MULTICOMP` | 1,4,16/1 | lazy/eager scaling and capacity |
| `BM7-UNBOUNDED` | 1/1 | finite A/B and improper-limit status |
| `BM7-NESTED-1/2/3` | controlled DAG depth 1/2/3 | metric composition |
| `BM7-EVICTION` | capacity, capacity+1, 2×capacity | deterministic bounded state |

G7A selects concrete capacity values only after recording entry size/work.
Tests use small explicit capacities to force deterministic eviction.

## 7. Index comparison protocol

For each fixture/query sequence:

1. reset the Construction and instrumentation to a declared state;
2. run the no-reuse oracle and save full result/counters;
3. rebuild the same fixture and run eager whole-revision;
4. rebuild again and run lazy component-revision;
5. compare every rich semantic axis and contribution;
6. compare work counters and retained state;
7. repeat in a rotated strategy order to detect order/warm-cache leakage.

The lazy hypothesis is demonstrated only if:

- its rich result equals the no-reuse oracle;
- first-use work is component-scoped;
- compatible overlap/repetition produces measured reuse;
- unused components are not built;
- retained entries never exceed the approved bound;
- capacity overflow evicts the deterministic expected entry;
- obsolete revisions are absent after invalidation;
- its nested behavior meets the per-point zero-build gates.

An eager strategy remains preferable only if measured evidence and lifecycle
simplicity justify it and the author explicitly replaces the working
hypothesis.

## 8. Integration and tolerance experiments

### 8.1 Capability ladder

Run the same curves, where possible, through:

1. analytic/closed form;
2. differential quadrature;
3. evaluator-only adaptive metric;
4. forced unsupported reference.

Record method, assumptions, subdivisions/calls, value, absolute/relative error
metadata and numeric guarantee. Analytic formula evaluated in `double` must not
claim exact arithmetic.

### 8.2 Tolerance grid

G7A proposes a bounded logarithmic grid after measuring coordinate scales.
Each record includes explicit:

```text
eps_metric_abs
eps_metric_rel
maximum refinement/subdivisions
minimum parameter interval rule
stagnation rule
floating-point floor behavior
```

Do not import `eps_domain`, the G6 evaluation envelope, render pixel tolerance
or a future G8 root tolerance. Vary absolute and relative tolerances
independently and include scale/translation cases.

### 8.3 Error truthfulness

Compare returned error metadata with analytic or high-precision error.

- `CERTIFIED_ERROR_BOUND` requires a mathematical certificate provided by the
  selected method.
- `ESTIMATED_ERROR` requires recorded assumptions and must be calibrated.
- refinement agreement alone is `FLOATING_POINT_UNCERTIFIED`.
- inability to produce defensible output is `UNSUPPORTED` or
  `NUMERICAL_FAILURE`, as cause requires.

### 8.4 Aggregate error

For multiple contributions compare candidate policies, including conservative
sum of absolute bounds/estimates. Record how relative error is computed near
zero and how weakest guarantee propagates. No policy is accepted from theory
alone; verify it on multi-component/multibranch cases.

## 9. Reparameterization experiments

For each eligible analytic curve, compare original parameterization with:

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1},\qquad \phi'(u)>0,
\]

using at least one declared nonzero `c`, and with `t=u^3` over a declared
monotone interval.

Record:

- mapped endpoints and orientation;
- result/error axes;
- evaluator/derivative/integrator/subdivision counts;
- whether derivative degeneration changes method or guarantee;
- index-key capability/version differences.

The pass property is length invariance within the independently justified
reference bound, not equality of subdivision patterns.

## 10. Unbounded and improper experiments

Use separate fixtures for:

- finite A/B subarc on an unbounded branch;
- unbounded parameter with finite improper total;
- positive-infinite total variation;
- established non-rectifiability;
- unsupported capability;
- limit not established within policy.

Vary improper cutoff/refinement through parameter-domain policies only.
Viewport, clip rectangle and render extent must remain irrelevant. Report
`POSITIVE_INFINITY`, `NON_RECTIFIABLE`, `UNSUPPORTED` and
`LIMIT_NOT_ESTABLISHED` as distinct outcomes.

## 11. Nested composition protocol

Construct:

```text
L1
 -> M1 = metric(L1)
 -> L2 whose evaluator consumes scalar-admissible M1
 -> M2 = metric(L2)
 -> L3
```

Also construct repeated downstream consumption of `TotalLocusMetricQuery(L1)`.

For each depth:

1. evaluate one downstream point;
2. repeat the same point 10 and 100 times within the same revision/policy;
3. evaluate overlapping downstream parameters;
4. edit a value-only input of L1;
5. edit an input that changes topology/component identity;
6. make L1 undefined, then recover;
7. repeat with metric index disabled.

Hard expectations:

- `renderCacheReads = 0`;
- `renderVertexReads = 0`;
- `legacySampleReads = 0`;
- `wholeLocusRegenerations = 0`;
- `metricIndexBuildsInsideDownstreamPointEvaluation = 0`;
- same revision/policy work is reused within the approved owner;
- invalidation follows the normal DAG;
- index-off outputs equal index-on outputs.

The trace labels **necessary recompute** when a changed upstream semantic value
must propagate, and **wasteful recompute** when unchanged compatible component
work is rebuilt or the whole locus is generated for a point request.

## 12. Lifecycle and failure protocol

With capacities small enough to observe:

- create, update, undefined, recover and remove;
- component split/merge and branch disappearance;
- copy/copyInternal and approved `set` behavior;
- undo/redo;
- tolerance/policy change;
- evaluator, derivative, integrator and aggregator exceptions;
- mutation attempts on input/result containers.

After every event record live algorithms/Geos as feasible, retained entries,
retained revisions, current payload revision, hit/miss/eviction counts and
cleanup counters. A failure must publish no partial index entry and no hybrid
old/new rich result.

## 13. Legacy and scientific comparisons

Preserve original hashes and do not edit:

- `models/legacy/template-v7/original/Templatev7.ggb`;
- `models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb`;
- `models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb`.

Use `AlgoPerimeterLocus` and `listLength`/`listLength12`/`postLocus` only to
explain sampled legacy work and numerical differences. They are not reference
implementations.

Scientific pilot order:

1. segment/circle analytic controls;
2. small deterministic cylinder development;
3. traced nested metric fixture;
4. bounded oblique-cone subset if it can be reproduced deterministically.

The hash-pinned two-level model is the functional legacy control; the
three-level `Flatten` model is pathological evidence. Full model migration is
not required.

## 14. Independent reference provenance

Each expected dataset records:

```text
fixture ID and mathematical formula
input constants and units
reference method
precision/digits and stopping rule
runtime/library and exact version
script path and SHA-256
raw output path and SHA-256
rounded expected value and justified comparison bound
```

Prefer analytic references. A high-precision reference and an independent
numerical method should cross-check non-elementary cases. Version any future
scripts/data under GeoCeDG validation-owned paths; generated files are evidence,
not authority. Python is validation evidence, not kernel authority. Do not add
a runtime dependency from the Java kernel to Python.

## 15. Suggested future evidence layout

Only a separately authorized G7A may create:

```text
geocedg/validation/locus-v2/g7a-metric-characterization.yml
geocedg/validation/locus-v2/g7a-metric-counter-traces.*
geocedg/validation/locus-v2/g7a-metric-references/
docs/validation/g7a_locus_v2_metric_characterization_report.md
docs/validation/g7a_locus_v2_metric_traceability_matrix.md
```

Raw large/generated outputs belong under ignored `artifacts/` with a versioned
manifest/hash when needed. G7A must not overwrite G6/G6R evidence.

## 16. Initial functional acceptance criteria

G7A must propose exact numeric counter budgets. Before those numbers exist, the
non-negotiable shape gates are:

- same rich result for all three strategies and index ON/OFF;
- first lazy request builds only requested components;
- repeated identical/overlapping lazy requests do not rebuild compatible
  component state;
- eager strategy reports every unused build rather than hiding it;
- retained entries/revisions are within explicit bounds after every query;
- eviction and invalidation are deterministic;
- policy/tolerance changes never produce false hits;
- no failed private build is published;
- nested forbidden-access and per-point-build counters remain zero;
- no existing legacy/V2 G6 behavior changes.

Wall-clock regression thresholds may become hard only after multiple runs
show stable dispersion on identified runners and the author approves them.

## 17. Stop conditions

Stop G7A and report rather than alter productive code if:

- G6/G6R baseline cannot be reproduced;
- independent references disagree without explanation;
- a numeric method cannot state a truthful guarantee;
- strategy outputs differ semantically;
- the index cannot be bounded or invalidated deterministically;
- scalar participation exposes inadmissible results;
- nested measurements require render/sample authority;
- a legacy/scientific original hash changes;
- characterization would require a public command, XML or G8 behavior.

G7B remains blocked until G7A evidence, tolerances, functional budgets, spec
promotion and ADR acceptance/replacement receive explicit author approval.

# G8 Locus V2 2D intersection functional-counter plan

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT EXECUTED** |
| Primary authority | Semantic equality, deterministic work, bounded state, and invalidation |
| Wall-clock | Informational until a reproducible environment budget is approved |
| Validation matrix | [`g8_locus_v2_intersection_validation_matrix.md`](g8_locus_v2_intersection_validation_matrix.md) |
| Date | 2026-08-13 |

G8 must not repeat the historical nested-Locus cost pattern. This plan defines
the measurements a separately authorized G8A should collect before selecting
solver, tolerance, work, or cache/index policies. It assigns no numerical
default and reports no G8 result.

## 1. Questions to answer

1. Which target-specific capabilities isolate and refine all supported roots
   without viewport or render state?
2. Which capability can establish absence, tangency, multiplicity, and overlap,
   and at what guarantee level?
3. How do interval/subdivision, derivative-aware, and evaluator-only strategies
   compare on the same semantic components and independent references?
4. What independent parameter-isolation, residual, tangency, deduplication,
   continuation, and work policies are justified by measured cases?
5. Can a query-local implementation meet repeated and nested workloads?
6. If reuse is justified, what is the smallest conservative intersection state
   that can be shared without retaining stale topology or creating a hidden DAG?
7. Does enabled/disabled reusable state preserve the complete rich result?
8. Are root continuation and topology-event costs deterministic and bounded?
9. Does every failure publish a coherent current snapshot and release private
   state?

## 2. Measurement rules

- Compare complete rich semantics, not just coordinate arrays.
- Hold source revisions, query, tolerance/work policy, capability version, and
  evaluator-session configuration constant while changing one strategy.
- Separate candidate isolation from refinement and final verification counts.
- Record per-component and whole-query counters, including unsuccessful work.
- Run deterministic traces from a fresh Construction and from controlled
  repeated/nested consumers.
- Treat wall-clock medians/dispersion as descriptive. Functional work and state
  are the initial hard authority.
- Preserve raw evidence plus a normalized report; hash all generated reference
  inputs/outputs/scripts.

## 3. Strategy comparisons

### Isolation strategies

Compare, where actual capabilities permit:

1. target-specific analytic candidate generation;
2. certified interval/subdivision isolation;
3. adaptive semantic interval isolation with derivative evidence;
4. evaluator-only adaptive search; and
5. conservative spatial-bound broad phase followed by the same semantic
   isolation/refinement/verifier pipeline.

A strategy unable to prove coverage must report partial/unresolved coverage;
it cannot win by returning fewer roots.

### Refinement strategies

For an equivalent isolated interval, compare safeguarded bracketed refinement,
derivative-aware refinement, interval contraction, and target-specific analytic
evaluation. Every path ends at the same independent semantic verifier.

### State strategies

Start with:

`REFERENCE_QUERY_LOCAL`
: No intersection-state reuse across query computations. Existing bounded
  evaluator-session behavior is controlled and counted separately.

Only if repeated-query evidence justifies it, characterize:

`QUERY_LOCAL_BROAD_PHASE`
: One computation owns conservative component bounds/partitions and discards
  them on publication.

`REVISION_SCOPED_INTERSECTION_OWNER`
: A proposed bounded owner shares only immutable, conservative candidate-
  isolation state for one source pair/revision/policy key. It never shares
  result sets, root tokens, continuation lineage, route-specific state, or G7
  metric partitions.

The last strategy is not approved. G8A must compare ownership shapes and
produce a separate author decision before any productive cache/index exists.

## 4. Required counters

Record per query, per valid component, and cumulative trace as applicable:

```text
semanticEvaluatorCalls
semanticDerivativeCalls
targetEquationEvaluations
targetDerivativeEvaluations
candidateIntervalsCreated
candidateBoxesCreated
broadPhaseCandidatesAccepted
broadPhaseCandidatesRejected
rootIsolationSubdivisions
rootIsolationMaximumDepth
rootRefinementCalls
rootRefinementIterations
residualVerificationCalls
targetMembershipChecks
deduplicationComparisons
verifiedFiniteSolutions
rejectedCandidates
unresolvedCandidates
overlapComponentsDetected
identityContinuationPredictions
identityContinuationComparisons
identityContinuationsAccepted
identityContinuationsAmbiguous
topologyMergeEvents
topologySplitEvents
topologyTerminationEvents
queryLocalIndexBuilds
sharedIndexBuilds
indexHits
indexMisses
indexInvalidations
indexEvictions
retainedIndexEntries
retainedSemanticRevisions
retainedRootHistoryEntries
approximateRetainedBytes
publishedSnapshots
partialSnapshotsPublished
failedPrivateComputations
staleRevisionEntriesAfterInvalidation
wholeLocusRegenerations
renderCacheReads
renderVertexReads
legacySampleReads
viewportReads
pixelToleranceReads
metricIndexReads
```

Counters must be incremented at the real operation boundary, not inferred from
expected loop counts. Estimated memory must identify the estimator and remain
separate from measured heap data.

## 5. Hard zero expectations

For every G8 semantic computation:

```text
wholeLocusRegenerations = 0 per candidate refinement
renderCacheReads = 0
renderVertexReads = 0
legacySampleReads = 0
viewportReads = 0
pixelToleranceReads = 0
metricIndexReads = 0
staleRevisionEntriesAfterInvalidation = 0
partialSnapshotsPublished = 0
```

Candidate-isolation sampling is counted under semantic evaluator calls and is
never recorded as a verified solution until independent refinement and
verification complete.

## 6. Policy quantities to measure independently

G8A must sweep and report, without assuming shared numeric values:

- `eps_root_parameter`: isolating/refined semantic-parameter width;
- `eps_residual_abs` and `eps_residual_rel`: normalized target incidence;
- `eps_tangency`: derivative/contact evidence threshold or interval criterion;
- `eps_dedup_parameter`: duplicate candidate decision within one semantic
  component;
- `eps_continuation_parameter`: semantic root continuation prediction; and
- optional `eps_coordinate_verify`: independent coordinate consistency check.

Each policy value has a versioned identity and documented units/normalization.
Experiments must include rescaled target equations, geometric scale/translation,
regular and derivative-degenerate reparameterizations, near tangency, and close
distinct roots. Do not reuse G6 domain, G7 metric, kernel/render, or pixel
tolerances unless measured evidence and author approval explicitly establish a
mapping.

## 7. Deterministic work ceilings

G8A measures candidate limits for each of:

```text
maxSemanticEvaluations
maxSemanticDerivativeEvaluations
maxTargetEvaluations
maxCandidateIntervalsOrBoxes
maxIsolationSubdivisions
maxIsolationDepth
maxRefinementIterationsPerCandidate
maxResidualVerifications
maxCandidateCount
maxContinuationComparisons
maxPublishedFiniteSolutions
maxRetainedIndexEntries
maxRetainedTopologyEpochs
```

Ceilings must compose predictably. Per-candidate limits alone are insufficient
if candidate count is unbounded. Budget exhaustion returns a typed unresolved
or partial-coverage result with exact consumed counters; it never truncates to
a falsely complete finite solution set.

No initial wall-clock stop is proposed for semantic truth. If responsiveness
later requires cancellation, cancellation must publish a typed incomplete
snapshot and cannot make partial roots current as a complete result.

## 8. Experiment families

### Root geometry sweep

Run simple, even, higher-order, clustered, endpoint, seam, false-minimum,
overlap, and no-root polynomials composed with representative semantic loci.
Sweep geometry and equation scale separately.

### Topology traces

Use deterministic parameter sequences across:

- two transverse roots → tangent merge → empty;
- empty → tangent → two-root split;
- root crossing included endpoint;
- root crossing periodic seam;
- valid-component split/loss/recovery; and
- branch replacement with and without unique G6 lineage.

Record root tokens and lineage alongside continuation-operation counts.

### Multi-branch/component sweep

Vary branch count, valid-component count, roots per component, invalid gaps,
and repeated coordinates. Confirm work scales with visited semantic components
and candidates, not render density or creation labels.

### Repeated consumers

For compatible queries use `N = 1, 3, 10, 100` consumers, then repeat after:

- no source change;
- target-only change;
- locus value-only revision;
- locus topology revision;
- removal of half/all consumers; and
- owner capacity pressure if a shared strategy is characterized.

### Nested evaluation

Measure one-, two-, and three-level evaluator nesting and a selected scientific
pilot. Assert no whole-locus regeneration, bounded session state, correct cycle
diagnostics, and normal DAG invalidation.

## 9. Equality oracle

For every reuse experiment, compare against `REFERENCE_QUERY_LOCAL` on:

- query-level status/coverage/geometry/currentness/guarantee axes;
- source/revision/policy/capability provenance;
- ordered branch/component solution keys;
- semantic parameters and verified coordinates under the approved comparison
  policy;
- residual/error evidence and classifications;
- root-lineage semantics for identical topology traces; and
- diagnostics/termination reason.

Cache/index strategies may differ only in documented work/state counters. A
semantic difference rejects the reuse strategy.

## 10. Evidence package and promotion rule

A future G8A report must preserve:

- exact branch, HEAD, baseline and command lines;
- fixture/query/policy manifests;
- raw per-run counter traces;
- normalized tables and plots where useful;
- independent-reference provenance and hashes;
- environment details for informational timing;
- rejected strategies and failure cases; and
- recommended hard budgets with measured margin.

No solver, tolerance, budget, isolation, or owner policy becomes normative
until the author explicitly approves the evidence. Productive G8B cannot begin
on a benchmark-plan recommendation alone.


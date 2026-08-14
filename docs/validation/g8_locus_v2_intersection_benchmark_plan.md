# G8 Locus V2 2D intersection functional-counter plan

| Field | Value |
|---|---|
| Status | **G8B-R1/G8B FUNCTIONAL POLICY — AUTHOR APPROVED** |
| G8A state | **PASS — AUTHOR APPROVED** |
| G8B state | **PASS — AUTHOR APPROVED** |
| G8B-R1 state | **PASS — AUTHOR APPROVED** |
| Primary authority | Semantic equality, deterministic work, bounded state, and invalidation |
| Wall-clock | Informational until a reproducible environment budget is approved |
| Validation matrix | [`g8_locus_v2_intersection_validation_matrix.md`](g8_locus_v2_intersection_validation_matrix.md) |
| Date | 2026-08-14 |

G8 must not repeat the historical nested-Locus cost pattern. This plan defines
the measurements collected by G8A and the author-approved G8B policies derived
from them. Measured evidence and implementation policy remain distinct.

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
7. Does enabled/disabled reusable state preserve the full rich result,
   including its independent completeness axis?
8. Are root continuation and topology-event costs deterministic and bounded?
9. Does every failure publish a coherent current snapshot and release private
   state?

## 2. Measurement rules

- Compare complete rich semantics, not just coordinate arrays.
- Hold source revisions, query, tolerance/work policy, capability version, and
  evaluator-session configuration constant while changing one strategy.
- Separate candidate isolation from refinement and final verification counts.
- For every strategy, separately record verified-root count, completeness
  status, and the method/evidence by which completeness was established or
  failed to be established.
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

A strategy unable to establish completeness must report `INCOMPLETE` or
`NOT_ESTABLISHED`; it cannot win by returning fewer roots or by converging on
only the candidates it found.

### Refinement strategies

For an equivalent isolated interval, compare safeguarded bracketed refinement,
derivative-aware refinement, interval contraction, and target-specific analytic
evaluation. Every path ends at the same independent semantic verifier.

### State strategies

The author-approved starting point is:

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

The last strategy is not approved for G8B. A later task may characterize it
only from new measured repeated-query need and must obtain a separate author
decision before any productive cache/index exists.

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
verifiedRootCount
rejectedCandidates
unresolvedCandidates
completenessCompleteResults
completenessIncompleteResults
completenessNotEstablishedResults
completenessEstablishmentChecks
completenessDomainsExcluded
completenessDomainsUnresolved
overlapComponentsDetected
identityContinuationPredictions
identityContinuationComparisons
identityContinuationsAccepted
identityContinuationsAmbiguous
identityContinuationsNotEstablished
reparameterizationMappingsChecked
reparameterizationContinuationsAccepted
pointConsumerTokenLookups
pointConsumerDefinedPublications
pointConsumerUndefinedPublications
pointConsumerSameTokenRecoveries
pointConsumerRetargets
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
pointConsumerRetargets = 0
staleRevisionEntriesAfterInvalidation = 0
partialSnapshotsPublished = 0
```

Candidate-isolation sampling is counted under semantic evaluator calls and is
never recorded as a verified solution until independent refinement and
verification complete.

## 6. Author-approved normalized policy

G8A swept and reported these independent quantities:

- `eps_root_parameter`: isolating/refined semantic-parameter width;
- `eps_residual_abs` and `eps_residual_rel`: normalized target incidence;
- `eps_tangency`: derivative/contact evidence threshold or interval criterion;
- `eps_dedup_parameter`: duplicate candidate decision within one semantic
  component;
- `eps_continuation_parameter`: revision-scoped semantic root continuation
  prediction evidence, never durable identity; and
- `eps_coordinate_verify`: independent coordinate consistency check.

The accepted `g8b-initial-normalized/v1` values are respectively `1e-12`,
`2e-12`, `2e-12`, `1e-10`, `4e-12`, `1e-8` and `4e-12`. They retain the G8A
provenance but apply only to compatible normalized quantities.

Target adapters expose model-distance-equivalent residuals where correct or a
family-specific typed residual/tolerance contract otherwise. Equation scaling
cannot change the decision. Root/dedup/continuation values remain in declared
provider parameter space and are not Euclidean distance. Tangency uses a
normalized contact indicator, never raw derivatives across equation/parameter
scaling. Coordinate closeness is verification-only and cannot establish
identity. If an adapter normalization changes the interpretation, G8B measures
and validates the normalized equivalent instead of copying the raw number.

G6 domain, G7 metric, kernel/render and pixel tolerances remain separate.

## 7. Provisionally approved deterministic work ceilings

The exact initial G8B values from G8A are:

```text
maxSemanticEvaluations = 32768
maxSemanticDerivativeEvaluations = 16384
maxTargetEvaluations = 32768
maxCandidateIntervalsOrBoxes = 8192
maxIsolationSubdivisions = 8192
maxIsolationDepth = 40
maxRefinementIterationsPerCandidate = 80
maxResidualVerifications = 1024
maxCandidateCount = 512
maxContinuationComparisons = 4096
maxPublishedFiniteSolutions = 256
maxRetainedIndexEntries = 0
maxRetainedTopologyEpochs = 2
```

Ceilings must compose predictably. Per-candidate limits alone are insufficient
if candidate count is unbounded. Budget exhaustion returns a typed unresolved
result with `INCOMPLETE` or `NOT_ESTABLISHED` completeness and exact consumed
counters; it never truncates to a falsely complete finite solution set.

These are implementation defaults rather than universal mathematical
constants. No initial wall-clock stop is authorized for semantic truth. If responsiveness
later requires cancellation, cancellation must publish a typed incomplete
snapshot and cannot make partial roots current as a complete result.

## 8. Experiment families

### Root geometry sweep

Run simple, even, higher-order, clustered, endpoint, seam, false-minimum,
overlap, and no-root polynomials composed with representative semantic loci.
Sweep geometry and equation scale separately.

### Completeness sweep

For every isolation strategy, run matched cases producing:

- `EMPTY + COMPLETE` from exhaustive exclusion;
- `FINITE + COMPLETE` from exhaustive isolation and verification;
- verified finite roots plus known unresolved/unprocessed work, reported as
  `INCOMPLETE`; and
- verified finite roots for which the capability cannot determine
  exhaustiveness, reported as `NOT_ESTABLISHED`.

Repeat for tangency/even roots, unbounded domains, difficult multiple roots,
and a deliberately non-exhaustive broad phase. Solver convergence and a
repeatable root count are not completeness evidence.

### Identity and topology traces

Use deterministic parameter sequences across:

- two roots → tangent/multiple root → two roots and reverse traversal;
- symmetric split cases with intrinsically ambiguous child correspondence;
- root crossing included endpoint;
- root crossing periodic seam;
- merge/split at or near a periodic seam;
- valid-component split/loss/recovery; and
- branch replacement with and without unique G6 lineage, including changes near
  a root merge/split;
- ordinary continuous source motion;
- equivalent monotone reparameterization; and
- parameter reversal/orientation changes where semantically allowed.

Record root tokens/identity status, semantic parameters, isolating intervals,
source/topology revisions, candidate parent/child lineage, ambiguity events,
and continuation-operation counts. Root intervals are revision evidence, not
durable identity. No coordinate-only re-association is allowed.

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

G8B-R1 additionally repeats the consumer sweep with parent completeness
`NOT_ESTABLISHED` and an individually verified, locally isolated root. Creating
or updating a token consumer must not trigger semantic evaluation, target
evaluation, refinement, exhaustive rescanning, or a new retained owner. Adding
a newly discovered root and reordering the finite solution list must preserve
the counters and existing token bindings unless identity evidence becomes
genuinely ambiguous.

### Nested evaluation

Measure one-, two-, and three-level evaluator nesting and a selected scientific
pilot. Assert no whole-locus regeneration, bounded session state, correct cycle
diagnostics, and normal DAG invalidation.

At least one nested case must use the required internal token-selected point
consumer as a normal-DAG input to a later CeDG-style construction. Measure
dynamic propagation, undefined/no-retarget behavior and same-token recovery;
an anonymous coordinate copy does not satisfy this first-class capability
gate.

## 9. Equality oracle

For every reuse experiment, compare against `REFERENCE_QUERY_LOCAL` on:

- query-level status/completeness/geometry/currentness/guarantee axes;
- source/revision/policy/capability provenance;
- ordered branch/component solution keys;
- semantic parameters and verified coordinates under the approved comparison
  policy;
- residual/error evidence and classifications;
- root-lineage semantics for identical topology traces; and
- diagnostics/termination reason.

Cache/index strategies may differ only in documented work/state counters. A
semantic difference rejects the reuse strategy.

## 10. G8A measured outcome

The query-local analytic trace measured exactly:

| Consumers | Semantic evaluations | Residual verifications | Retained entries |
|---:|---:|---:|---:|
| 1 | 1 | 1 | 0 |
| 3 | 3 | 3 | 0 |
| 10 | 10 | 10 | 0 |
| 100 | 100 | 100 | 0 |

For ten consumers at nesting depths 1, 2 and 3, underlying semantic evaluator
calls were 10, 20 and 30. Removal of half/all consumers retained zero
intersection entries/history. All hard-zero authority counters remained zero.
The evaluator-only comparison built one query-local candidate context per
query and retained nothing; it correctly reported completeness
`NOT_ESTABLISHED`.

No measured cross-query benefit was needed to keep work bounded, so G8A did
not characterize or recommend a shared owner. Measured limits and tolerance
values are recorded in the
[characterization report](g8a_locus_v2_intersection_characterization_report.md)
and machine evidence. Work exhaustion produced
`WORK_LIMIT_REACHED + NOT_ESTABLISHED + UNRESOLVED` without partial
publication.

## 11. Evidence package and approved policy boundary

The G8A report preserves:

- exact branch, HEAD, baseline and command lines;
- fixture/query/policy manifests;
- raw per-run counter traces;
- normalized tables and plots where useful;
- independent-reference provenance and hashes;
- environment details for informational timing;
- rejected strategies and failure cases; and
- recommended hard budgets with measured margin.

The author approved the capability hierarchy, normalized tolerance contract,
provisional exact work ceilings and query-local/no-owner state from this
evidence. The measured JSON remains unchanged; the author-approved G8B policy
is a derived contract, not a rewrite of measurements.

G8A is `PASS — AUTHOR APPROVED`. G8B executed the versioned policy with 100
repeated queries and 100 derived consumers, zero retained index entries, at
most two retained topology epochs, deterministic counters, and zero forbidden
authority reads. The author approved this bounded query-local policy; no shared
owner/index was introduced.

G8B-R1 re-executed the 100-consumer path with a `NOT_ESTABLISHED` finite parent
and an admissible local root. All 100 points were defined from the same semantic
token, semantic-evaluation count did not change while consumers were created,
retained index entries remained zero, topology epochs remained bounded by two,
and forbidden-authority counters remained zero. R1 changes no numerical work
budget or query-local ownership rule.

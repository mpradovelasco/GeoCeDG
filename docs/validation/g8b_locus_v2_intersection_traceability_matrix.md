# G8B Locus V2 2D intersection traceability matrix

| Field | Value |
|---|---|
| Status | **G8B-R1 AND G8B PASS — AUTHOR APPROVED** |
| Validation authority | [`g8_locus_v2_intersection_validation_matrix.md`](g8_locus_v2_intersection_validation_matrix.md) |
| Kernel report | [`g8b_locus_v2_intersection_kernel_report.md`](g8b_locus_v2_intersection_kernel_report.md) |
| Machine evidence | [`g8b-intersection-kernel-evidence.json`](../../geocedg/validation/locus-v2/g8b/g8b-intersection-kernel-evidence.json) |
| Date | 2026-08-14 |

`PASS` below means the cited productive path and assertion executed. The author
approved the complete matrix as G8B-R1/G8B closeout evidence on 2026-08-14.

## 1. Semantic result and target families

| Matrix cases | Productive authority | Executed test(s) | Result |
|---|---|---|---|
| K-COMP-EMPTY, K-COMP-FINITE | `IntersectionCompletenessEvidence2D`, solver component-coverage check | `analyticLineCapabilityPublishesCompleteEmpty`, `analyticParabolaLineFindsTwoTransverseRoots` | PASS |
| K-INCOMPLETE, K-NOT-EST, K-PROJECTION | independent completeness axis plus solution-local point-admissibility predicate | `incompleteCoverageCannotPublishFalseEmpty`, `evaluatorOnlyFindsEvenCandidateButNeverClaimsCompleteness`, `incompleteFiniteVerifiedRootRemainsPointAdmissible`, `notEstablishedFiniteVerifiedRootKeepsCompletenessProvenance`, `localizationOnlyAndFailedResidualCannotDefinePoint` | PASS |
| K-TANGENCY | local-minimum fallback plus capability classification evidence | `evenTangencyIsEstablishedWithoutSignChange`, `evaluatorOnlyFindsEvenCandidateButNeverClaimsCompleteness` | PASS |
| A-LINE-00..06 | normalized line adapter, solver, endpoint/domain semantics | line empty/two/tangent/near/endpoint/scaling tests in `G8BIntersectionKernelTest` | PASS |
| A-SEG-01..03 | support-line residual plus captured segment membership | `segmentAndRayUseSeparateCapturedLimitedMembership`, `segmentFiltersSupportRootsAndMarksIncludedTargetBoundary` | PASS |
| A-RAY-01..03 | support-line residual plus captured oriented ray membership | horizontal and diagonal membership assertions, `rayFiltersBehindStartAndRetainsItsStart` | PASS |
| A-AFFINE-01, A-SCALE-01 | model-coordinate residual and semantic parameter evidence | `scaleAndTranslationPreserveReferenceParametersAndResiduals` | PASS |
| A-CIR-00..06 | signed radial-distance circle adapter and full rich semantics | `circleSupportsSecantTangentAndCompleteEmpty`, seam and repeated-preimage tests | PASS |
| full conics | explicit `UNSUPPORTED` family | `circleUsesSignedRadialModelDistanceAndRejectsFullConic`, `unsupportedConicProducesClosedRichState` | DEFERRED AS APPROVED |

## 2. Topology, classification, and set semantics

| Matrix cases | Productive authority | Executed test(s) | Result |
|---|---|---|---|
| B-BRANCH-01, B-COMP-01, B-DISC-01 | branch/component keys and no cross-component candidate verification | `equalCoordinatesOnDistinctBranchesRemainDistinctPreimages`, `disconnectedComponentsAreCoveredSeparately` | PASS |
| B-SELF-01 | semantic preimage identity, never coordinate dedup | `selfIntersectionRetainsTwoPreimagesAtOneCoordinate` | PASS |
| B-CUSP-01 | regularity remains unknown without independent proof | `cuspKeepsSingularRegularityUnclaimedDespiteExactMultiplicity` | PASS |
| B-COLLAPSED-01, B-ISO-01, B-EMPTY-01 | typed infinite/isolated/empty set semantics | `isolatedEmptyAndCollapsedComponentsUseDistinctSetSemantics` | PASS |
| B-DISC-02 | open-domain exclusion cannot fabricate a root | `includedEndpointIsRetainedAndOpenEndpointIsExcluded` | PASS |
| B-PER-01 | canonical plus lifted seam evidence | `periodicSeamIsCanonicalizedAndPublishedOnce`, `periodicSeamRepresentationChangesEvidenceNotIdentity` | PASS |
| B-TOPO-01..02 | topology context, discontinuity, absence, invalidation/recovery | merge/split, seam, and recovery tests in `G8BIntersectionLifecycleTest` | PASS |
| B-REPAR-01 | explicit continuation key; parameter/interval remain evidence | `monotoneScalingAndOrientationReversalPreserveDurableToken` | PASS |
| C-TRANS-01, C-TAN-01, C-HIGH-01, C-NEAR-01 | independent contact/multiplicity fields; exact orders 2/4 only when supplied | transverse, even/order-four, and near-tangency kernel tests | PASS |
| C-OVER-01, C-INF-01 | typed overlap/infinite result and zero point projection | `overlapAndInfiniteSetsCarryTypedEvidenceWithoutPoints` | PASS |
| C-OVER-02 | overlap evidence plus independently verified isolated root | `overlapComponentCanCoexistWithVerifiedIsolatedRootEvidence` | PASS |
| C-UNSUP-01 | truthful unresolved/not-established | evaluator fallback, nonfinite, incomplete, and unsupported tests | PASS |

## 3. Identity and normal-DAG lifecycle

| Matrix cases | Productive authority | Executed test(s) | Result |
|---|---|---|---|
| I-CONT-01, I-MULTI-01 | unique explicit continuation relation, branch lineage, opaque token; independent of result order and global completeness | dynamic point/update, `newlyDiscoveredRootAndOrderingCannotRetargetExistingTokens`, and reparameterization tests | PASS |
| I-MERGE-01, I-SPLIT-01, I-REVERSE-01, I-SYMM-01 | candidate parent evidence; no universal genealogy | `mergeSplitPublishesCandidateGenealogyWithoutUniversalInheritance` | PASS |
| I-SEAM-01 | seam changes localization evidence, not token | `periodicSeamRepresentationChangesEvidenceNotIdentity` | PASS |
| I-BOUND-01, I-GAP-01, I-BRANCH-01 | absent/topology-changed solution is undefined or discontinuous | absence/failure/recovery and merge/split lifecycle tests | PASS |
| I-AMBIG-01 | explicit ambiguous status; no Cartesian tie-break | merge/split and no-coordinate-retarget tests | PASS |
| I-STALE-01, I-FAIL-01, I-RECOVER-01 | begin-revision invalidation and atomic rich publication | `selectedTokenIsUndefinedDuringAbsenceOrFailureAndRecoversByProof` | PASS |
| D-DAG-01, D-NEST-01, D-FIRSTCLASS-01 | `setInputOutput()` and token-selected point feeding a dependent algorithm | `tokenSelectedPointDrivesDownstreamConstructionAndUpdatesNormally` | PASS |
| D-MULTI-01, D-REPEAT-01, D-STATE-01 | 100 consumers/queries, no duplicate solve and bounded retained state | lifecycle many-consumer and topology/scientific repeated-query tests | PASS |
| D-COPY-01, D-REMOVE-01 | no revision-bound payload alias, deterministic removal | `copySetRemovalXmlAndManyConsumersStayBoundedAndInternal` | PASS |
| D-EXC-01, D-REV-01 | exception becomes atomic current failure; source binding object must match publication | recovery test and rich-Geo lifecycle assertions | PASS |

### 3.1 G8B-R1 point-admissibility refinement

| R1 requirement | Productive authority | Executed test(s) | Result |
|---|---|---|---|
| finite + `COMPLETE` remains admissible | token lookup over current rich result | `tokenSelectedPointDrivesDownstreamConstructionAndUpdatesNormally` | PASS |
| finite + `INCOMPLETE` can expose an uncompromised established root | local isolation, verification, provenance and identity predicate | `incompleteFiniteVerifiedRootRemainsPointAdmissible` | PASS |
| finite + `NOT_ESTABLISHED` can expose an uncompromised established root without strengthening completeness | same predicate plus retained completeness evidence | `notEstablishedFiniteVerifiedRootKeepsCompletenessProvenance` | PASS |
| empty/not-established, localization-only, residual failure, stale or ambiguous state cannot expose a point | fail-closed token lookup | `notEstablishedWithoutVerifiedRootIsUnresolvedAndHasNoPoint`, `localizationOnlyAndFailedResidualCannotDefinePoint`, `nonCurrentResultCannotAdmitAnOtherwiseVerifiedToken`, merge/split test | PASS |
| new-root discovery and result ordering never retarget an existing token | explicit continuation key and opaque token, never slot/index | `newlyDiscoveredRootAndOrderingCannotRetargetExistingTokens` | PASS |
| identical coordinates retain constructive preimage identity | source/branch/continuation lineage | `equalCoordinatesFromDistinctPreimagesKeepDistinctAdmissibleTokens` | PASS |
| absence/failure and ambiguity publish undefined atomically and recover only by approved token continuation | rich result publication followed by derived point update | `selectedTokenIsUndefinedDuringAbsenceOrFailureAndRecoversByProof`, `mergeSplitPublishesCandidateGenealogyWithoutUniversalInheritance` | PASS |
| downstream and 100-consumer behavior remain normal-DAG, query-local and bounded | one rich solve plus token consumers | `tokenSelectedPointDrivesDownstreamConstructionAndUpdatesNormally`, `copySetRemovalXmlAndManyConsumersStayBoundedAndInternal` | PASS |

## 4. Numeric, work, and representation independence

| Matrix cases | Productive authority | Executed test(s) | Result |
|---|---|---|---|
| N-ISO-01, N-EVEN-01 | semantic interval/key dedup and non-sign-only even search | near-tangency, even/order-four, dedup tests | PASS |
| N-NORM-01, N-RES-TYPE-01, N-ABSREL-01 | typed model-distance residual contract and scale | line/circle normalization and affine/scale tests | PASS |
| N-PARAM-01, N-TAN-NORM-01 | provider-scoped parameter tolerance and source-speed-normalized contact | reparameterization and normalized contact tests | PASS |
| N-BUDGET-01 | versioned ceilings and fail-closed instrumentation | policy/budget and deterministic work-limit tests | PASS |
| N-NAN-01 | nonfinite values cannot enter result values | `nonfiniteEvaluationDowngradesCompleteCandidateToUnresolved` | PASS |
| R-VIEW-01, R-DPI-01, R-RENDER-01, R-LEGACY-01 | semantic sources only plus zero forbidden-read counters | `viewportAndRenderScaleCannotChangeIntersectionAuthority`, counter tests | PASS |
| R-CACHE-01 | no cache/index exists; retained entries are fixed to zero | functional-counter and repeated-query tests | NOT APPLICABLE — ASSERTED ZERO |
| R-CLASSIC-01, R-LOCUS-01, R-PERSIST-01, R-3D-01, R-EXPORT-01 | no route/path/XML/3D/export edits; append-only class ordering and explicit non-drawable rich type | `LocusV2KernelIntegrationTest`, `LocusMetricProductiveLifecycleTest`, `G8AIntersectionKernelLifecycleCharacterizationTest`, `DrawablesTest`, focused static audit plus operational/Locus V2/G7/composed verifiers | PASS |

## 5. Scientific pilots and deferred scope

| Requirement | Versioned source role | Executed productive regression | Result |
|---|---|---|---|
| multileaf LSIM constructive preimages | scientific topology requirement, not numeric authority | `lsimMultileafPilotKeepsFourConstructivePreimages` | PASS |
| focal illumination qualitative topology | scientific topology requirement, not solver definition | `focalIlluminationCirclePilotTransitionsTwoOneZeroExplicitly` | PASS |
| historical nested-Locus cost | bounded-query requirement | `repeatedQueriesRemainDeterministicQueryLocalAndBounded` and 100-consumer lifecycle test | PASS |
| full conic/function/implicit/locus–locus | Level C characterization only | closed unsupported result for non-circle conic; no productive source | DEFERRED AS APPROVED |

## 6. Scope result

No selected B-core/B-policy row is silently weakened to an empty result or
anonymous point. Cases for which the current capability cannot prove exhaustive
coverage remain finite/unresolved with `INCOMPLETE` or `NOT_ESTABLISHED`. No
cache-enabled comparison is applicable because G8B starts query-local with
zero retained index entries. G8B-R1 and G8B are `PASS — AUTHOR APPROVED`;
extended families remain outside this matrix's productive scope and move only
to the separately authorized, not-started G8C design phase.

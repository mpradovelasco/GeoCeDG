# PRE-G9B-R2 semantic metric endpoint validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Traceability from each focal requirement of the canonical `PRE-G9B-R2` prompt to
the normative clause it exercises in
[`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §23 and to
the executable evidence. Test classes:

```text
SEM  = org.geocedg.common.locus.PreG9bR2SemanticIntersectionEndpointTest
SIDE = org.geocedg.common.kernel.locus.intersection.PreG9bR2IntersectionEndpointSideSelectionTest
NAT  = org.geocedg.desktop.PreG9bR2IntersectionEndpointNativeArchiveTest
```

| # | Focal requirement | Clause | Evidence |
|---|---|---|---|
| 1 | Intersection-materialized point combined with `Length` / `LocusLength` (no prior test existed) | §23, §23.1 | `SEM#pairIntersectionPointsAreEndpointsForRichAndScalarMetrics` — rich and scalar both `1`, and a mixed A1 + intersection query `0.5` |
| 2 | Unambiguous side selection for `S` | §23.2 | `SIDE#exactlyOneMatchingSideIsUniqueAndAnUnrelatedSourceIsNoAddress`; `SEM#sideSelectionFollowsIdentityForEitherSourceAndEitherCallerOrder` (the same point addresses both intersected sources) |
| 3 | Currentness — ledger token validity | §23.3 | `SEM#dormantEndpointFailsClosedAndTheSameTokenRecovers` |
| 4 | Currentness — semantic-revision equality | §23.3 | `SIDE#aStaleSideIsFinalWhileTheUnchangedSideStaysCurrent` — a stale identity match is `UNRESOLVED_INVALID`, never `NO_ADDRESS`, while the unchanged side stays `UNIQUE` |
| 5 | Token retention and reactivation via the dormant path | §23.3, §23.5 | `SEM#dormantEndpointFailsClosedAndTheSameTokenRecovers` (same token, same key after reactivation); `NAT#nativeArchiveReconstructsAndRevalidatesIntersectionEndpoints` (dormancy survives a native save/reopen and recovers) |
| 6 | Copy, including the single authorized closure-copy rebase | §23.7 | `SEM#closureCopyRemapsIdentitiesIntoNewConsistentKeys` — copied source, point and key are all new and the copied scalar is `1` |
| 7 | Reopen and persistence | §23.7 | `SEM#xmlReopenAndUndoRedoRevalidateWithTheSameKeys` (XML reopen, undo/redo, key not serialized); `NAT#nativeArchiveReconstructsAndRevalidatesIntersectionEndpoints` (real native `.cedg` lifecycle, key reconstructed identically) |
| 8 | Self-intersection `S × S` | §23.2 | `SIDE#aSelfPairRootWithBothSidesOnTheAddressedSourceIsAmbiguous` — see note A |
| 9 | Source A/B ordering including the reversed view | §23.2 | `SEM#sideSelectionFollowsIdentityForEitherSourceAndEitherCallerOrder` (`Intersect(S,T)` and `Intersect(T,S)`); every `SIDE` case also runs on `evidence.reversed()` |
| 10 | Duplicate-token ambiguity | §23.2 | `SIDE#aDuplicatedTokenIsNeverPointAdmissibleSoNoEndpointCanCarryIt` — see note B |
| 11 | Redefine | §23.7 | `SEM#compatibleNumericRedefineKeepsTheEndpointAndFollowsTheGeometry` — same point identity and token, address and length follow the new geometry |
| 12 | No-retarget guard | §23.5 | `SEM#changingTheSelectedTokenIsRetargetingAndFailsClosed` — the endpoint stays defined on another root, and the metric reports `INVALID_QUERY` with "retargeting is forbidden" |
| 13 | Existing coincident-free-point negative regressions | invariant | `PostG9U1A1SplineConstructorProvenanceTest#coincidenceAndSelfIntersectionNeverCreateExtraProvenance`; `G9U1MetricReviewTest#foreignOrCoincidentPointsNeverReceiveInferredMetricPreimages` — both unchanged and passing |
| 14 | Explicit exclusions and A1 fall-through | §23.6, §23.8 | `SEM#unadmittedShapesStayInvalidAndA1FallThroughIsPreserved` — coincident free point, coincident similarity image and single-source intersection point all `NO_ADDRESS` / `INVALID_QUERY`; an intersection point used as a constructor of another spline still resolves there through A1 |

## Notes

**A — `S × S` is not representable today.** `Intersect(S,S)` on a genuinely
self-crossing spline publishes no point-admissible root: the pair solver reports
`NUMERICAL_FAILURE` / `UNRESOLVED` with zero finite solutions. No self-pair
endpoint can therefore be materialized, which is `R2-E0`/`R2-E1` territory and is
not widened here. The `MULTIPLE` rule of §23.2 is the fail-closed authority for
when that becomes representable, mirroring §21's
`NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE` precedent. The test exercises it with
two **real** kernel-produced `S`-side evidences at distinct parameters, taken
from two genuine `S × T` roots and paired into one evidence value; no geometry is
fabricated.

**B — duplicate tokens never reach the resolver.** The rich result's
point-admissible index removes any token that appears on more than one solution,
so `findExactPointAdmissibleSolution` cannot return one and such a point is
undefined before this family is consulted. The test rebuilds the current real
result with the same real solution published twice — varying only the solution
list and its consistent root count, the established `G8BIntersectionLifecycleTest`
idiom — and pins that the token is no longer admissible. It pins a structural
invariant and is not acceptance evidence. Ledger-level duplicate allocation is
already covered by `G9S1R1PairTokenLedgerTest#duplicateStagingFailsClosedAndQuarantineCannotContradictStaging`.

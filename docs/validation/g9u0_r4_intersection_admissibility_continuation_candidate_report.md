# G9U0-R4 deterministic intersection identity — closeout report

## Disposition

```text
G9U0-R4 = PASS — AUTHOR APPROVED
historicalAuthorSmoke = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
historicalAuthorSmoke3 = TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE
manualAuthorFinalSmokeFourRoot = PASS
manualAuthorFinalSmokeReactivation = PASS
protectedCheckpoint = 4ef2c9df433aec7c6385a488a02581358da83f60
fourRootRegularMotionCorrected = true
existingPointAutomaticReactivation = implemented
deterministicPolicy = AUTHOR_APPROVED_DIRECTION
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true

G9U0-R5 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = false
implementationAuthorized = false
executed = false

G9U1 = DESIGNED — NOT AUTHORIZED
DETERMINISTIC_CONTINUITY_OFF_REQUIRED
MATERIALIZATION_POLICY_UNCHANGED_STRICT_LOCAL_EVIDENCE
AUTO_MATERIALIZATION_FRONTEND_ONLY
MULTI_MATERIALIZATION_REQUIRED
PERSISTENT_INSPECTOR_SESSION_REQUIRED
AUTO_REACTIVATION_EXISTING_POINTS_KERNEL
blockedUntilR5Pass = true
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

This is the living R4 closeout record. It records the author's decision and does
not present automated validation as self-approval.

## Entry and fixture authority

R4 starts at `ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b`. Annotated R3 tag object
`1c1be8ebb58be9ad4c4e7242bc56105f9f310068` peels to that commit. The branch is
`feature/g9u0-r4-intersection-admissibility-continuation`.

The tracked author fixture remains byte-exact:

- path:
  `source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg`;
- length: 13,301 bytes;
- SHA-256:
  `47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`;
- modified by R4: no.

The second author fixture is preserved byte-exactly for characterization:

- author artifact:
  `artifacts/smoke-test-g9u0-r2/fouSolutions.cedg` (historical filename typo);
- durable path:
  `source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg`;
- length: 14,601 bytes;
- SHA-256:
  `51dcf7a002cb3984bb4cf5843d50e100f4bc8ef91217d4502fa7987c5b1ec21c`;
- copy status: byte-exact; neither filename nor coordinates are identity.

## Smoke chronology

1. The first R4 candidate corrected initial publication: the midpoint/circle
   result exposed two finite, current, transverse, locally isolated and
   initially materializable roots while global completeness remained
   `NOT_ESTABLISHED`.
2. Its original 27-method automated authority passed.
3. Author smoke then found one exact-token point becoming undefined after small
   regular movement with no apparent topology event.
4. That smoke is retained as
   `FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION`.
5. The stateful token-selection design was superseded by current-snapshot
   deterministic selection and the focused authority expanded to 38 methods.
6. Final bounded hardening made unresolved evidence component-scoped and made
   target-contact germ orientation invariant under legitimate nonzero scalar
   representation changes without erasing ray direction.
7. The replacement composed run exposed a missing localization value for
   `DETERMINISTIC_SELECTION_ESTABLISHED`; the bounded correction added the key
   to the base, English and Spanish menu bundles without changing semantics.
8. The second author smoke confirmed substantial ordinary-motion improvement
   but found a construction with four finite solutions that could not all be
   materialized. It is retained as
   `IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE`.
9. Byte-exact reproduction proved this was a base-selector collision, not
   missing local numerical certification.
10. The author accepted ADR 0017, authorizing intrinsic oriented-domain
    phase/rank only for repeated base selectors.
11. The bounded correction now gives the four roots unique complete selectors
    without changing their numerical guarantee or global completeness.
12. The pre-current-correction direct periodic-seam hardening invalidated the
    complete old ranked group when a complete bijective same-selector
    prior/current relation could not be proved; it never rotated opaque tokens.
13. Ledger validation now proves authentic pre-phase v2 migration and rejects a
    phase-bearing v3 state falsely relabeled as v2.
14. Pre-final replacement focused A/B and the full composed authority passed.
15. Final hardening forbids intrinsic-phase binding to legacy singleton token
    material in both runtime migration and manipulated ledger-v3 import, and
    canonicalizes merge/split parent records by token without separating their
    continuation keys.
16. Final focused A/B again pass 50/50 with identical canonical evidence.
17. The author then executed both phase/rank smoke cases. Case A, the canonical
    two-root midpoint case, passed.
18. In Case B the four roots, intrinsic selectors, exact tokens and initial
    materializations were all correct.
19. Case B nevertheless failed during ordinary interactive motion: while four
    finite, transverse and isolated roots remained visible, materialized points
    became undefined. This contradicts the automated regular-motion claim and
    is retained as `TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE`.
20. R4 remains open. No PASS or self-approval is claimed, and the failing
    interactive update path requires a bounded corrective iteration.
21. The reproduced direct center drag showed one root in each two-root germ
    group moving about `0.032409` in semantic phase while the fixed
    `2*pi / 256` transition guard allowed only about `0.024544`. The same final
    geometry reached through smaller updates retained the tokens. The fixed
    work-partition fraction was therefore not topology evidence.
22. The bounded corrective design replaces that threshold in the periodic
    ranked-token guard with an adaptive intrinsic phase-tube/cell certificate
    derived from prior/current canonical phases, disjoint isolating intervals
    and nearest cyclic root gaps. Regular disjoint tubes retain tokens; a true
    cyclic selector shift fails only its affected ranked group.
23. Durable selector binding is now explicitly separate from the current
    topology certificate. A materialized exact allocation may remain dormant
    while its existing `GeoPoint` is undefined, and may reactivate only when the
    same selector again resolves uniquely. This creates no point automatically.
24. The adaptive/dormant correction is implemented in source. Replacement
    focused A/B and the composed authority pass. R5 and G9U1 have not been
    executed.
25. Periodic evidence that is insufficient to distinguish zero cyclic offset
    from monodromy no longer burns the group. Ledger v4 retains it in a durable
    non-current quarantine. A later unique offset zero releases the exact group
    and can reactivate an existing point; a proved unique nonzero offset retires
    it permanently; absent or non-unique offset evidence remains quarantined.
26. The author completed final Case 1 and accepted four-root materialization,
    direct and incremental regular motion, absence of token/root swapping and
    deterministic binding.
27. The author completed final Case 2 and accepted the active/dormant/reactivated
    lifecycle of the same existing points, absence of point creation during
    recompute, exact selector/token ownership and native save/reopen behavior.

## Exact four-root reproduction and blocker matrix

The author document contains `a=LocusV2(E,C)`, `c=Circle(A,B)` and
`b=Intersect(a,c)` with the existing host Continuity value false. The current
result is `SUCCESS`, `FINITE`, `CURRENT`, `VERIFIED_UNCERTIFIED`, global
`NOT_ESTABLISHED`, zero unresolved candidates and zero overlaps.

| semantic parameter | complete deterministic selector unique? | certified arithmetic? | established local evidence? | current eligibility | exact reason |
| --- | --- | --- | --- | --- | --- |
| `-2.093511674974421` | yes | no; `ESTIMATED_ERROR` | yes | yes | positive base germ plus unique intrinsic oriented phase/rank |
| `-1.403606948912654` | yes | no; `ESTIMATED_ERROR` | yes | yes | negative base germ plus unique intrinsic oriented phase/rank |
| `1.4036069489126533` | yes | no; `ESTIMATED_ERROR` | yes | yes | positive base germ plus distinct intrinsic oriented phase/rank |
| `2.0935116749744207` | yes | no; `ESTIMATED_ERROR` | yes | yes | negative base germ plus distinct intrinsic oriented phase/rank |

All four are transverse and locally isolated; normalized residual magnitudes
are approximately `3.39e-13` to `3.55e-13`. They share branch
`generator.main` and component `generator.main/component-0`. The positive and
negative pairs repeat their respective base selectors, but each pair has
pairwise disjoint isolating intervals in one coherent oriented component. The
ADR-0017 extension therefore produces four unique selectors framed by
component, germ, orientation, periodic domain kind, collision cardinality and
intrinsic phase/rank. Each owns an explicit key and exact materializable token.

The gate decomposition is not one Boolean truth claim:

- **existence:** all four finite roots are present;
- **deterministic identity:** established for each by a unique complete
  selector;
- **local evidence:** established for each;
- **global completeness:** independently `NOT_ESTABLISHED`;
- **materialization:** allowed by exact identity/key/lineage, independently of
  global completeness and without upgrading the numerical guarantee.

The ordinary upstream conic algorithm cannot be reused as exact semantic
authority. Continuous mode uses Cartesian-distance/history matching;
deterministic mode freezes an output-slot permutation initialized through that
machinery. Both depend on evidence R4 explicitly forbids for token identity.

## Alternatives A–D and recommendation

**A — strict unique base-selector only.** This was truthful but left all four
roots unusable. ADR 0017 rejects it as the final four-root behavior while
retaining its fail-closed principle when a complete selector is not unique.

**B — bounded adaptive certification escalation.** This is appropriate only
when isolation or numerical evidence is the blocker. It must be solver-owned,
deterministic and budgeted. It cannot separate the duplicate selectors in this
case and was therefore not implemented.

**C — deterministic local with weaker evidence.** This remains a distinct
possible future policy. It is not needed for the four roots because their
blocker was identity, not local evidence, and R4 does not implement it.

**D — manual force.** Reject as a separate semantic bypass. Explicit user
confirmation may select policy C in the future, but it can never override
selector ambiguity, stale evidence, overlap, unresolved identity or
merge/split ambiguity.

**Accepted decision.** ADR 0017 chooses the bounded oriented semantic-domain
phase/rank only for repeated base selectors. “No identity by order” continues
to prohibit extrinsic solver/list/output/UI order. The full selector frames the
rank with component, germ, orientation, domain kind and verified collision
cardinality. Periodic monodromy is explicit: a seam-reaching ranked group is an
identity discontinuity, not a token rotation.

The current corrective refinement does not change that selector. It replaces
only the current periodic topology certificate: adaptive intrinsic phase tubes
prove regular same-rank motion from group separation, while intersecting tubes
or a cyclic selector shift fail closed. Neither the phase tube nor a fixed
parameter displacement becomes durable identity.

No adaptive certification escalation, weaker evidence tier or manual-force
override was added. The ledger stays identity-only: an exact opaque token means
an exact semantic selector binding, not exact arithmetic.

## Root-cause trace

The R3 initial deadlock originated before the final admissibility predicate.
The public candidate capability inherited a G8 key that existed only for one
isolated root per component. Both author roots share one component, so neither
had public identity evidence.

The first R4 candidate allocated both roots but made later token retention
depend on a stateful previous/current relation. Its ledger-preauthorization
path required exact revision address and semantic-parameter equality. A
regularly moving transverse root changes semantic parameter; the old predicate
therefore rejected the same semantic solution and the materialized child became
undefined.

The corrected interpretation is:

```text
current snapshot
-> deterministic semantic classification
-> unique intrinsic selector
-> exact ledger binding
-> current admissible token
```

Previous-state evidence is diagnostic, not token-selection authority.

The current Case B failure was later in that diagnostic/topology guard.
`PublicIntersectionRootTransition2D` bounded every semantic displacement by
`max(continuationTolerance, componentSpan / min(256,
maximumIsolationSubdivisions))`. The author fixture has a `2*pi` component, so
the effective bound was `0.02454369260617026`; the direct ordinary center drag
moved one root in each germ group by `0.0324089744688726`. Each group therefore
lost one edge and the periodic group-wide guard invalidated all four points.
Forty smaller updates to the byte-identical destination stayed below the fixed
bound and passed. Update granularity, the integer `256` and an isolation work
ceiling are not geometric topology evidence.

### First failing protected-checkpoint snapshot

The exact reproduced UI-sized update is `A.x: 3.98 -> 4.18` with `A.y = 10`
and `B = (5.72, 2.22)`. The analytic parameters below characterize the exact
fixture equations; the published solver values retain their unchanged
`ESTIMATED_ERROR` evidence. All four roots stayed on branch `generator.main`,
component `generator.main/component-0`, orientation `INCREASING`, domain
`PERIODIC_FUNDAMENTAL_INTERVAL`, topology context
`g9u0-public-topology/v1`, collision cardinality two in their respective germ
group, `CURRENT`, `TRANSVERSE_ESTABLISHED` and locally isolated. No seam,
overlap, unresolved candidate, branch/component change or cardinality change
was observed.

| intrinsic root slot | semantic parameter before -> first failing snapshot | germ / rank / complete selector | protected-checkpoint root publication before -> after | exact child before -> after |
| --- | --- | --- | --- | --- |
| negative outer | `-2.093511674974421 -> ~-2.12592064944333` (`delta ~-0.032408974469`) | positive germ / rank 0 / same `(component,germ,INCREASING,PERIODIC,2,0)` | claimed ledger token, `DETERMINISTIC_SELECTION_ESTABLISHED`, `UNCHANGED`, explicit key, admissible -> revision-local handle, `IDENTITY_DISCONTINUITY`, `AMBIGUOUS_EVENT`, no explicit key, inadmissible | selected old token resolved -> old token had no current admissible parent solution; point undefined |
| negative inner | `-1.403606948912654 -> ~-1.39419682506843` (`delta ~+0.009410123844`) | negative germ / rank 0 / same `(component,germ,INCREASING,PERIODIC,2,0)` | same active-to-discontinuity transition | same defined-to-undefined parent lookup |
| positive inner | `1.4036069489126533 -> ~1.39419682506843` (`delta ~-0.009410123844`) | positive germ / rank 1 / same `(component,germ,INCREASING,PERIODIC,2,1)` | same active-to-discontinuity transition | same defined-to-undefined parent lookup |
| positive outer | `2.0935116749744207 -> ~2.12592064944333` (`delta ~+0.032408974469`) | negative germ / rank 1 / same `(component,germ,INCREASING,PERIODIC,2,1)` | same active-to-discontinuity transition | same defined-to-undefined parent lookup |

The fixed guard was group-wide. One approximately `0.032409` displacement in
each two-root germ group exceeded `2*pi/256 ~= 0.024543692606`; consequently it
invalidated both ranks of each group, including the roots that moved only about
`0.009410`. The first changed predicate was therefore the prior-token periodic
reuse guard. Root geometry, deterministic selector, local isolation and
currentness did not fail. The corrective A26 regression records every listed
field and compares the same destination reached by one update and forty steps.

## Bounded kernel correction

The numeric solver remains geometry, refinement and local-isolation authority.
The public resolver now derives a base
`IntersectionRootDeterministicSelector2D` from stable component lineage and the
typed oriented transverse contact germ in the current snapshot. If the base is
repeated, and only when its current isolating intervals establish a coherent
intrinsic order, the selector is extended with declared orientation,
periodic/nonperiodic domain kind, verified collision cardinality and oriented
phase/rank. The exact ledger material separately binds result owner, source
pair, constructive lineage, topology context, provider/parameter contract and
target contract.

Only a complete selector unique in the current constructive result may allocate
or resume a token. Unique selectors are processed in canonical selector order.
First publication reports `NEW_TOPOLOGICAL_SOLUTION` / `APPEARED`; subsequent
resolution reports `DETERMINISTIC_SELECTION_ESTABLISHED`. Address evidence is
updated, but the exact token remains byte-identical.

`PublicIntersectionRootTransition2D` retains bounded previous/current
comparison subordinate to the current-snapshot selector. It cannot choose a
token. For a repeated periodic group, the identity resolver no longer treats a
fixed span/work-partition fraction as the reuse certificate. It constructs
adaptive intrinsic phase tubes from prior/current canonical phase points and
isolating intervals, bounds them by the nearest cyclic root gaps in both
snapshots and requires those swept tubes to remain disjoint. Consequently a
regular nonperiodic or seam-free final state is independent of whether it was
reached directly, through many small steps, through forward/reverse motion or
after save/reopen.

For a complete ranked periodic group, current reuse of an old token requires a
complete same-selector phase-tube certificate. The disposition is deliberately
three-way:

1. a unique coherent cyclic offset zero releases any prior quarantine, reuses
   the exact token group and may reactivate its already existing points;
2. a unique proved nonzero cyclic offset is monodromy and permanently retires
   only that ranked group; and
3. missing, intersecting, incomplete, budget-exhausted or multiple-offset
   evidence fails closed into durable periodic quarantine, not permanent
   retirement.

Quarantine keeps the prior selector/phase evidence non-current and blocks a
competing fresh allocation. Its `q`/`r` ledger evidence survives recomputation,
canonical export/import and exact-provenance copy until a later current snapshot
establishes one of the first two outcomes. This is only
a guard against unsafe current reuse. It does not select the current roots,
which remain determined by the current snapshot. A missing bounded relation on
an ordinary nonperiodic group does not become identity authority.

Repeated base selectors are ranked only through the intrinsic semantic-domain
contract. Repeated complete selectors remain explicit ambiguity and receive
only revision-local handles. No coordinate, solver/list order or proximity
fallback was introduced.

For a periodic component, the declared oriented fundamental interval is the
deterministic phase frame. A ranked interval is never wrapped or matched
heuristically. A proved nonzero cyclic shift permanently retires the affected
old allocations with typed `IDENTITY_DISCONTINUITY`; evidence that cannot yet
prove zero or nonzero offset quarantines them instead. Collision cardinality,
component/topology or orientation changes likewise fail closed and cannot
transfer a token to a new occupant of the same integer rank. Locus V2 × Locus
V2 remains under ADR 0009's symmetric pair authority and does not consume this
one-sided rank selector. The direct byte-identical seam control permanently
retires the two prior tokens in the affected germ group only because its phase
evidence proves a nonzero cyclic selector shift; the independently certified
two-root group retains its tokens. No old token is rotated to a new rank.

For merge/split diagnostics, the resolver canonicalizes the complete parent
records once by opaque token and derives both parent-token and continuation-key
evidence from that same ordering. It never sorts those parallel values
independently. The association is deterministic evidence only: the event stays
ambiguous and point-inadmissible.

Equivalent target equations cannot be allowed to flip that selector. The
line/segment adapter canonically orients an unoriented support normal; central
conics use center level, parabolas use quadratic trace, and regular polynomial
implicit curves use the leading nonzero monomial ordered by total degree then
x degree. Legitimate nonzero scalar changes therefore preserve the germ and
exact token. Ray direction remains semantic and intentionally retains its
oriented contract.

## Local admissibility and global state

`LOCAL POINT ADMISSIBILITY != GLOBAL COMPLETENESS` remains exact. The corrected
resolver permits an independently established unique local root under global
`NOT_ESTABLISHED` or `INCOMPLETE`. The solver now publishes exact semantic
component keys for unresolved candidates. Evidence on another component
remains global/work information and does not veto the root; evidence on the
root's own resolved valid component blocks materialization. The resolver also
rejects a root whose own isolation, currentness, selector, contact, revision or
overlap evidence is not established. Global `COMPLETE` cannot make a locally
ambiguous root admissible.

The durable selector/token allocation and this current topology/admissibility
decision are separate. An allocation claimed by an existing materialized point
may be retained as dormant when it is not current. The same `GeoPoint` becomes
undefined and may reactivate only when the same complete selector resolves
uniquely again; it is never retargeted by coordinates, order or proximity.
Neither dormancy nor reactivation creates a new point. R4 still creates only the
rich result plus explicitly requested token-selected children.

## Ledger v4 and v1/v2/v3 compatibility

The token ledger now exports canonical format v4. Each R4 entry stores its
opaque token lineage, separate exact continuation-contract/selector binding and
active/dormant/quarantined materialized-claim state. Internal status codes `q`
(`PERIODIC_QUARANTINE`) and `r` (`CLAIMED_PERIODIC_QUARANTINE`) retain the
complete periodic group as non-current evidence; neither status is an
admissible token. A quarantined group is kept only while an actual materialized
claim needs its evidence, and releasing the final claim prunes the complete
group. Exact-provenance copy cannot itself certify offset zero or release a
quarantine. Persisted selectors and statuses are strictly parsed;
noncanonical incarnation suffixes, forged legacy material, incomplete bindings
and duplicate bindings are rejected.

Canonical v3 phase state, canonical v1 and authentic pre-phase v2 state remain
readable. Intrinsic phase/rank selectors are legal in v3 and v4; claimed active
or dormant status and periodic quarantine status `q`/`r` are legal only in v4.
A v3 phase snapshot falsely relabeled as v2 and a v4 claimed or quarantined
state relabeled as an older version are rejected. An exact
R3 singleton token may migrate
only at its exact initial address and only when it is the sole finite current
root on that component. Migration adds the current selector binding without
changing one character of the token. An older token is preserved only through
one compatible semantic binding; otherwise import fails closed. The real XML test
`preR4V1XmlTokenPointMigratesWithoutTokenChangeAndTracksMotion` loads a v1
ledger and already-materialized point, proves it defined after the first R4
recompute, verifies current canonical state and then proves regular motion
through the migrated selector.

The focused ledger regression also imports a fixed authentic pre-phase v2
snapshot, emits canonical v4 and preserves its exact opaque token. The same test
rejects the phase-bearing relabel case, so compatibility cannot be used to
smuggle v3 selector semantics into v2.

Legacy singleton material is also barred from intrinsic phase under the
correct ledger version. A runtime phase-selector migration receives fresh token
material rather than reusing the singleton. A manipulated current snapshot that
attaches the legacy material to an intrinsic-phase selector is rejected on
import. This closes a separate authenticity boundary from the versioned phase
and claimed-lifecycle cases.

The external `locus-root/v3` token envelope remains unchanged.

## Historical pre-current-correction candidate inventory

The replacement authority freezes 51 changed paths, including 29 under
`source/`. No generated log or artifact is tracked. The bounded localization
additions remain:

- `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties`;
- `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_en.properties`; and
- `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu_es.properties`.

The byte-exact four-solution fixture remains unchanged. Its regression now
validates productive intrinsic phase/rank selection rather than only the old
fail-closed characterization.
`localAdmissibilityRemainsIndependentOfGlobalCompleteness` now exercises both
same-component blocking and unrelated-component non-veto behavior, while
`stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence` exercises
equivalent target-representation changes and exact-token retention.

## Historical executed focused inventory: 50 methods

Both final focused runs executed this exact 25 + 23 + 2 inventory with zero
failures, errors or skips and an exact canonical-summary match. The method count
is unchanged; the final assertions strengthen legacy-singleton and merge-parent
evidence.

### Public kernel — 25

- `authorFourSolutionFixtureUsesFourIntrinsicSemanticSelectors`
- `authorFourSolutionSelectorsIgnoreEverySolverPermutation`
- `authorFourSolutionBindingsArePathIndependentAndMoveRegularly`
- `authorFourSolutionPointsRemainDefinedThroughoutRegularMotion`
- `authorMidpointCircleRootsAreInitiallyPointAdmissible`
- `publishedR3SingletonPositiveControlRemainsAdmissible`
- `preR4V1XmlTokenPointMigratesWithoutTokenChangeAndTracksMotion`
- `authorTokenPointsSurviveSaveReopenAndSameStateRecompute`
- `authorMidpointDirectRegularMotionRetainsExactTokenPoints`
- `authorMidpointBindingIsPathIndependentAcrossRegularHistories`
- `authorMidpointBroadRegularMotionIsContinuousAndDoesNotSwap`
- `materializedAuthorTokenPointUsesNormalUndoRedoLifecycle`
- `localAdmissibilityRemainsIndependentOfGlobalCompleteness`
- `disappearanceNewAppearanceAndTangencyRemainFailClosed`
- `periodicSeamIsCanonicalAndDoesNotDuplicateTheBoundaryRoot`
- `orientedRootCrossesPeriodicSeamWithoutDuplicateOrTokenSwap`
- `stableTransverseMotionUsesDeterministicCurrentSelectors`
- `spatialLeftRightOrderCanReverseWithoutTokenSwap`
- `uniqueDeterministicSelectorSurvivesUnobservedDirectUpdate`
- `repeatedDeterministicSelectorsUseIntrinsicSemanticRank`
- `rankedCollisionGroupAppearanceAndDisappearanceInvalidatesWithoutShift`
- `reversingProviderOrientationNeverTransfersRankedTokensWithoutMap`
- `rankedPeriodicSeamInvalidatesInsteadOfRotatingOpaqueTokens`
- `mergeCandidateParentKeysRemainExplicitAndNonAdmissible`
- `stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence`

Class:
`org.geocedg.common.locus.G9U0R4IntersectionAdmissibilityContinuationTest`.

### Ledger and parser — 23

- `twoDistinctFirstAllocationsReceiveDistinctOpaqueIdentities`
- `duplicateCurrentSelectorReturnsTheSameAllocationAndFailsClosed`
- `changedRevisionAddressKeepsUniqueDeterministicIdentity`
- `differentDeterministicSelectorNeverReusesACommittedAddress`
- `selectorRejectsContextMismatchButNotParameterMotion`
- `unavailablePublicationBurnsPriorAllocation`
- `authorizedCopyRebasesContractAndRetainsIncarnation`
- `multiRootCurrentAllocationsCopyThroughOneToOneProvenance`
- `allocationSnapshotRoundTripRetainsExactOpaqueIdentity`
- `legacyMintRetainsItsExactReuseAndPersistenceContract`
- `r3SingletonTokenMigratesWithoutChangingItsExactOpaqueMaterial`
- `r3SingletonMigrationRequiresTheExactInitialAddress`
- `deterministicSelectorRejectsAGermFromAnotherComponent`
- `intrinsicPhaseSelectorRoundTripPreservesSemanticFrame`
- `phaseSelectorLedgerRoundTripRetainsExactOpaqueTokens`
- `changedVerifiedCollisionCardinalityBurnsAllRankedBindings`
- `orientationReversalCannotReuseRankedAllocationsWithoutDeclaredMap`
- `publishedContinuationKeyExposesItsExactVersionedSelector`
- `persistedAllocationRejectsNoncanonicalIncarnationSuffix`
- `persistedBindingRejectsForgedLegacyTokenMaterial`
- `persistedSnapshotRejectsDuplicateDeterministicBinding`
- `deterministicSelectorResolvesMovedAddressWithoutHistory`
- `multiRootCopyUsesOneSemanticMappingPerSharedLegacyIncarnation`

Class:
`org.geocedg.common.kernel.locus.intersection.G9U0R4TokenLedgerAllocationTest`.

### Desktop native archive — 3

- `newMidpointCircleTokensSurviveNativeCedgSaveAndReopen`
- `nativeCedgReopenPathMatchesDirectDeterministicBinding`
- `nativeCedgPreservesDormantAndReactivatedExistingPoints`

Class: `org.geocedg.desktop.G9U0R4NativeArchivePersistenceTest`.

## Current validated focused inventory: 58 methods

The corrective authority declares and executes 27 public-kernel, 28 ledger and
3 Desktop methods. The additional ledger regression is
`periodicQuarantineSurvivesRecomputeReopenAndCopyUntilProvedRelease`; it covers
durable `q`/`r`, copy non-release, unique offset-zero release, proved nonzero
retirement and final-claim pruning through ledger recompute/export-import/copy.
The third Desktop method,
`nativeCedgPreservesDormantAndReactivatedExistingPoints`, covers native
`2 -> 4 -> 2` dormancy, same-point reactivation and reopen after reactivation;
it is not evidence of a periodic-`q` native round trip. These counts describe
the current 54-path candidate, including 31 paths under `source/`. Focused A and
B each pass 58/58 with zero failures, errors or skips and identical normalized
summary SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`.
Their ignored log roots are respectively
`artifacts/g9u0-r4/four-root-characterization-a` and
`artifacts/g9u0-r4/four-root-characterization-b`. The full composed authority
also exits 0 and terminates with
`All GeoCeDG verification gates passed.` at
`artifacts/g9u0-r4/four-root-characterization-composed`.

## Validation and evidence status

The earlier 27/27 focused runs and canonical SHA-256
`ae88007728027b90151aa9c3cd5394b4d8578cd44ecbaeae6ea137e63abb5ecc`
are preserved as superseded pre-correction evidence. They do not validate the
current ADR-0017 authority.

The earlier replacement authority then executed 38/38 methods and the composed
repository gate at SHA-256
`75babe9f95b21f65bf8f5cb2b9abd2cf4452e761a5b8d47eef714307da4229f1`.
That evidence remains historical because it predates the exact four-root
fixture and nineteenth public-kernel method.

The later 39/39 characterization runs and normalized SHA-256
`2f40527ec29f91b241862bd518af2416490ec029a9beac8631399115efa26cb7`
are also retained as historical evidence. They validate the second-smoke
blocker characterization, not the subsequent phase/rank correction.

Pre-final replacement A/B executed 50/50 methods at canonical SHA-256
`c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`;
that evidence is retained historically. After the final assertions, focused A
and B each executed the same 50/50 methods (25 public-kernel, 23 ledger and 2
Desktop native-archive), with zero failures, errors or skips. Their normalized
summaries match exactly at final SHA-256
`f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`.
La regresión final invierte deliberadamente la enumeración previa de padres
merge y exige las mismas listas canónicas token/clave; la prueba legacy usa el
mismo germ base para aislar que el rechazo procede exclusivamente de intentar
asociar material pre-fase con un selector de fase intrínseca.
Checkstyle passed. The final pre-current-correction inventory was 51 paths, 29
under `source/`. Protective checkpoint
`4ef2c9df433aec7c6385a488a02581358da83f60` records that historical state.

The full composed authority exited 0 and terminated with the literal
`All GeoCeDG verification gates passed.` Historical regressions and the sealed
authorities therefore remain green for this candidate. Logs are ignored under
`artifacts/g9u0-r4/`; generated evidence is not durable source authority. These
automated results did not constitute author approval; the separate final author
re-smokes now do.

Those 50/50 runs, inventory counts and hashes predate the current direct
UI-sized Case B regression and adaptive phase-tube/dormant-allocation
correction. They remain historical evidence and must not be cited as validating
the current correction. The replacement current authority instead executed
focused A and B at 58/58 with an exact normalized-summary match at SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`;
the full composed verifier exited 0 with the literal terminal
`All GeoCeDG verification gates passed.`. This automated PASS was followed by
the two required final author re-smokes, both accepted as `PASS`.

The historical G8B exact-token invariant also remains sealed. Its descendant
verifier now recognizes either the historical exact-copy seam or the current
retained-token seam, but the latter must prove ledger/provenance validation and
exact-token lookup. No coordinate, extrinsic order, proximity, movement-history
or lineage fallback was admitted.

## Retained limitations

The complete selector remains intentionally bounded. Intrinsic phase/rank is
used only for a repeated base selector with coherent component, germ,
orientation, domain kind, cardinality and disjoint isolating intervals. A
duplicate complete selector, changed cardinality/topology/orientation,
non-isolation or periodic seam-reaching ranked interval fails closed. Periodic
monodromy is an explicit identity discontinuity; no token is rotated. A
wide/direct periodic update without a complete adaptive same-selector phase-tube
certificate places the affected prior ranked group in durable non-current
quarantine; it does not permanently retire the group merely because bounded
evidence is insufficient. A later unique offset-zero certificate releases the
same group and may reactivate its existing points, a proved unique nonzero
offset permanently retires that group selectively, and absent or nonunique
offset evidence leaves it quarantined. Claimed exact
allocations may otherwise remain dormant and reactivate only through the same
unique selector. Pair roots retain ADR 0009 authority. R4 makes no
certification-policy relaxation.

The incremental resolver builds the current selector map once and validates
the `P` existing materialized bindings by direct lookup after sorting `R`
current roots, for `O(R log R + P)` additional selector/reactivation work per
rich-result recompute. No child point triggers another global intersection
solve and no movement history is retained. The older whole-transition
diagnostic that compares possible parent/root relations may still have
`O(R^2)` worst-case work; it is diagnostic and cannot select or reactivate a
token.

R4 adds no markers and no automatic point creation. Prospective G9U1 markers,
create-one/create-all and opted-in auto-materialization may consume only current
admissible exact tokens and must remain frontend transactions. They cannot
display a dormant token as selectable, create on recompute/reactivation or
calculate a second identity rank. R5 and G9U1 remain unexecuted.

Retained cross-cutting validation risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` is nonblocking for R4. Native
`.cedg` has no dedicated end-to-end round trip of periodic quarantine state;
current evidence covers ledger recompute/export-import/copy plus nonperiodic
native dormancy/reactivation and reopen after reactivation. The risk must be
resolved or receive explicit author-approved disposition no later than global
G9 closeout, and G9U1 validation must revisit it because marker/materialization
UX consumes persisted rich-result authority. It is not an implicit R5
dependency.

## Final author-smoke disposition

The author executed both cases after the 50/50 replacement authority was green.
Case A passed. Case B passed initial four-root detection, selector/token
allocation and materialization, then failed regular-motion persistence when
materialized points became undefined despite four apparently regular roots.
The real interactive path is now represented by a direct-versus-incremental
regression. Its bounded correction is the adaptive intrinsic periodic
phase-tube/cell certificate, with dormant exact allocations separated from
current topology evidence. The author executed the two procedures below and
accepted both as `PASS`.

### Case 1 — four-root regular motion

1. Open the byte-exact `fourSolutions.cedg` fixture and inspect its four finite,
   transverse and locally isolated roots.
2. Materialize all four exact-token points without copying tokens manually.
3. Apply the direct author-sized update `A.x: 3.98 -> 4.18`; confirm all four
   points remain defined and bound to the same selectors.
4. Return to `A.x = 3.98`, reach `4.18` through many smaller steps and confirm
   the same final bindings, evaluated positions and point IDs.
5. Continue broadly inside the same four-root regular stratum; confirm no swap
   and no dependence on UI update granularity or movement history.
6. Save/reopen `.cedg` and repeat the direct-versus-incremental comparison.

### Case 2 — `2 -> 4 -> 2` topology recurrence

1. Start from a stable two-root state and materialize both exact-token points.
2. Move through a genuine transition to four roots. Confirm the two existing
   points become undefined wherever current unique identity is not established,
   and confirm the two new roots are not silently materialized.
3. Return to the same original two-root geometry through one direct path and
   verify the same two `GeoPoint` IDs automatically reactivate only when their
   durable selectors again resolve uniquely.
4. Repeat `2 -> 4 ->` the same original `2` through a different legal history;
   require identical token bindings, definedness and evaluated positions.
5. Save/reopen `.cedg` with the existing points dormant, return to the original
   two-root state, confirm the same IDs reactivate, then save/reopen once more
   after reactivation. This native control is not a periodic-`q` round-trip
   claim; that tri-state is covered by ledger recompute/export-import/copy.
6. Verify ambiguity never reactivates a point, no coordinate/proximity fallback
   occurs and no new `GeoPoint` is created by kernel recomputation.

Final disposition: Case 1 `PASS`; Case 2 `PASS`; `selfApproved=false`.

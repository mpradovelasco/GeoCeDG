# G9U0-R4 deterministic intersection identity — corrective candidate report

## Disposition

```text
G9U0-R4 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR RE-REVIEW
historicalAuthorSmoke = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
currentAuthorSmoke = TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE
deterministicPolicy = AUTHOR_APPROVED_DIRECTION
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U0-R5 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = false
implementationAuthorized = false

G9U1 = DESIGNED — NOT AUTHORIZED
DETERMINISTIC_CONTINUITY_OFF_REQUIRED
MATERIALIZATION_POLICY_UNCHANGED_STRICT_LOCAL_EVIDENCE
AUTO_MATERIALIZATION_FRONTEND_ONLY
blockedUntilR4Pass = true
blockedUntilR5Pass = true
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

This is a living implementation-candidate record. It contains no phase PASS
claim and does not replace author re-review.

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
12. Direct periodic-seam hardening now invalidates the complete old ranked group
    when a complete bijective same-selector prior/current relation cannot be
    proved; it never rotates opaque tokens.
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
token. Consequently a regular nonperiodic or seam-free final state is
independent of whether it was
reached directly, through many small steps, through forward/reverse motion or
after save/reopen.

For a complete ranked periodic group, reuse of an old token requires a complete
bijection from all prior roots to current roots with the same complete
selectors. Missing, ambiguous, budget-exhausted, incomplete or non-bijective
evidence invalidates the whole prior group. This is only a guard against unsafe
reuse. It does not select the current roots, which remain determined by the
current snapshot. A missing bounded relation on an ordinary nonperiodic group
does not become identity authority.

Repeated base selectors are ranked only through the intrinsic semantic-domain
contract. Repeated complete selectors remain explicit ambiguity and receive
only revision-local handles. No coordinate, solver/list order or proximity
fallback was introduced.

For a periodic component, the declared oriented fundamental interval is the
deterministic phase frame. A ranked interval that reaches the seam is not
wrapped or matched heuristically: affected old tokens are invalidated with
typed `IDENTITY_DISCONTINUITY`. Collision cardinality, component/topology or
orientation changes likewise fail closed and cannot transfer a token to a new
occupant of the same integer rank. Locus V2 × Locus V2 remains under ADR 0009's
symmetric pair authority and does not consume this one-sided rank selector.
A direct byte-identical seam update that lacks the required complete bijection
therefore invalidates all four prior tokens in the exercised group; a later
stable publication may expose fresh admissible tokens without reinterpreting
the old ones.

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

## Ledger v3 and v1/v2 compatibility

The token ledger now exports canonical format v3. Each R4 entry stores its
opaque token lineage plus a separate exact continuation-contract/selector
binding. Persisted selectors are strictly parsed; noncanonical incarnation
suffixes, forged legacy material, incomplete bindings and duplicate bindings
are rejected.

Canonical format v1 and authentic pre-phase v2 state remain readable. Format v3
is the only ledger format that may contain the enriched phase/rank selector. A
v3 phase snapshot falsely relabeled as v2 is rejected. An exact R3 singleton
token may migrate
only at its exact initial address and only when it is the sole finite current
root on that component. Migration adds the current selector binding without
changing one character of the token. An older token is preserved only through
one compatible semantic binding; otherwise import fails closed. The real XML test
`preR4V1XmlTokenPointMigratesWithoutTokenChangeAndTracksMotion` loads a v1
ledger and already-materialized point, proves it defined after the first R4
recompute, verifies v3 state and then proves regular motion through the migrated
selector.

The focused ledger regression also imports a fixed authentic pre-phase v2
snapshot, emits canonical v3 and preserves its exact opaque token. The same test
rejects the phase-bearing relabel case, so compatibility cannot be used to
smuggle v3 selector semantics into v2.

Legacy singleton material is also barred from intrinsic phase under the
correct ledger version. A runtime phase-selector migration receives fresh token
material rather than reusing the singleton. A manipulated ledger-v3 snapshot
that attaches the legacy material to an intrinsic-phase selector is rejected on
import. This closes a separate authenticity boundary from the phase-v2 relabel
case.

The external `locus-root/v3` token envelope remains unchanged.

## Final candidate inventory

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

## Final executed focused inventory: 50 methods

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

### Desktop native archive — 2

- `newMidpointCircleTokensSurviveNativeCedgSaveAndReopen`
- `nativeCedgReopenPathMatchesDirectDeterministicBinding`

Class: `org.geocedg.desktop.G9U0R4NativeArchivePersistenceTest`.

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
Checkstyle passed. The final inventory is 51 paths, 29 under `source/`.

The full composed authority exited 0 and terminated with the literal
`All GeoCeDG verification gates passed.` Historical regressions and the sealed
authorities therefore remain green for this candidate. Logs are ignored under
`artifacts/g9u0-r4/`; generated evidence is not durable source authority. These
automated results do not constitute author approval.

## Retained limitations

The complete selector remains intentionally bounded. Intrinsic phase/rank is
used only for a repeated base selector with coherent component, germ,
orientation, domain kind, cardinality and disjoint isolating intervals. A
duplicate complete selector, changed cardinality/topology/orientation,
non-isolation or periodic seam-reaching ranked interval fails closed. Periodic
monodromy is an explicit identity discontinuity; no token is rotated. A
wide/direct periodic update without a complete bijective same-selector relation
may invalidate the entire prior ranked group conservatively; continuity of the
old tokens across that seam is not claimed. Pair roots retain ADR 0009
authority. R4 makes no certification-policy relaxation.

## Author-smoke disposition and conditional corrective re-smoke

The author executed both cases after the 50/50 replacement authority was green.
Case A passed. Case B passed initial four-root detection, selector/token
allocation and materialization, then failed regular-motion persistence when
materialized points became undefined despite four apparently regular roots.
The procedure below remains the required corrective re-smoke after the real
interactive update path has a failing regression and bounded correction. Only
the author can approve it.

### Case A — canonical midpoint/circle regular motion

1. Open the exact midpoint fixture.
2. Create the author circle intersection.
3. Materialize both exact-token points.
4. Move incrementally through a broad regular interval.
5. Confirm both points remain defined and do not swap.
6. Return to one exact previously visited geometry by a different movement
   path.
7. Confirm the same deterministic token bindings.
8. Traverse the periodic seam.
9. Approach a genuine topology event and confirm conservative invalidation
   rather than a jump.
10. Save and reopen the `.cedg` document.
11. Confirm deterministic recomputation after reopen.

### Case B — four-solution document

1. Open the byte-exact four-solution fixture.
2. Inspect all four finite solutions and their separate identity, local-evidence
   and global-completeness states.
3. Confirm all four roots retain their truthful `ESTIMATED_ERROR` local evidence
   and global `NOT_ESTABLISHED` state while exposing four unique complete
   selectors/tokens.
4. Materialize all four roots under the unchanged strict local-evidence policy;
   copy no token
   manually.
5. Move through an ordinary regular interval and verify defined points neither
   swap nor depend on movement history.
6. Reach the same final geometry through a different regular path and verify
   solver enumeration does not affect the bindings.
7. Approach a collision-cardinality/orientation/topology boundary and verify
   invalidation rather than rank transfer.
8. Exercise the periodic fundamental seam and confirm typed identity
   discontinuity rather than opaque-token rotation where the ranked group
   reaches it.
9. Save/reopen `.cedg` and verify the same token/provenance outcome.

Only the author may mark this re-smoke PASS.

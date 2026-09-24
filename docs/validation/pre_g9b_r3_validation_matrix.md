# PRE-G9B-R3 validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Each row maps an obligation of the canonical `PRE-G9B-R3` prompt or of the
author's `AD-R0-6` resume disposition to the normative clause it exercises and to
its executable evidence. Clause abbreviations: `DP x` =
[durable dependency projection](../../geocedg/specs/spatial/durable-dependency-projection.md)
§x; `ADR n` = [ADR 0029](../adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md)
Decision n; `INV n` = invariant n of the canonical prompt. Test classes:

```text
PROJ  = org.geocedg.common.locus.PreG9bR3DurableDependencyProjectionTest
WIT   = org.geocedg.common.locus.PreG9bR3RedefineWitnessTest
DCC   = org.geocedg.common.locus.PreG9bR3DurableContractRedefineTest
REF   = org.geocedg.common.locus.PreG9bR3CertifiedFrontierRefreshTest
VOC   = org.geocedg.common.kernel.spatial.identity.PreG9bR3RedefineVocabularyTest
UI    = org.geocedg.desktop.PreG9bR3RedefineFrontendTest
```

## 1. Persistence invariants (disposition item 9)

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| P1 | historical version-1 file loads unchanged as version 1 | DP 6.2, DP 11 | `PROJ#historicalDirectRecordsLoadValidateAndSaveUnchanged` (byte-exact author fixture `locusFromMidpoint.cedg`) |
| P2 | ordinary reopen/save does not migrate version 1, nor a free-point move or an ordinary edit upstream | DP 7.1 | `PROJ#historicalDirectRecordsLoadValidateAndSaveUnchanged`; native `.cedg`: `UI#nativeArchivesKeepVersionOneVersionTwoAndMixedRecords` |
| P3 | an identical or compatible redefine of a version-1 record upgrades it atomically | DP 7.2, DP 7.3 | `PROJ#anExplicitRedefineMigratesOneHistoricalRecordAtomicallyEvenWithAnEqualSet`, `PROJ#aDefinitionChangeMigratesTheHistoricalRecordInTheSameEvent` |
| P4 | the upgrade happens even when the direct and transitive sets are equal | DP 6.1, DP 7.2 | `PROJ#anExplicitRedefineMigratesOneHistoricalRecordAtomicallyEvenWithAnEqualSet` |
| P5 | version-2 save/reopen | DP 6 | `WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent`, `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity`, `UI#nativeArchivesKeepVersionOneVersionTwoAndMixedRecords` |
| P6 | version-2 undo/redo | DP 7.3 | `WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent`, `DCC#anAcceptedIdentityPreservingUpdateKeepsIdentityAndPersistsTheNewFrontier`, `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity` |
| P7 | mixed version-1/version-2 document | DP 7.1 | `PROJ#anExplicitRedefineMigratesOneHistoricalRecordAtomicallyEvenWithAnEqualSet`, `PROJ#eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion`, `DCC#anAcceptedContractUpdateMigratesAHistoricalRecord`, `UI#nativeArchivesKeepVersionOneVersionTwoAndMixedRecords` |
| P8 | copy/remap produces new identities with coherent version-2 dependencies | DP 5, DP 7.2 | `PROJ#copiesAndRecreationsPublishVersionTwoWithAFreshCoherentFrontier` |
| P9 | delete/recreate produces a new version-2 identity | DP 7.2 | `PROJ#copiesAndRecreationsPublishVersionTwoWithAFreshCoherentFrontier` |
| P10 | an unsupported future schema rejects cleanly | DP 6.4, ADR 2 | `PROJ#eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion` (`UNSUPPORTED_VERSION`, empty registry, not file-loading), `PROJ#projectionRuleIsNamedOnlyByAKnownSchemaVersion` |
| P11 | no partial persisted migration after failure | DP 7.3 | `PROJ#aRejectedOrCancelledEventLeavesTheHistoricalRecordUntouched` (cancel, family change, cycle) |
| P12 | no version-2 record validated under version-1 semantics | DP 6.2 | `PROJ#eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion` (direct set under a version-2 label: `MALFORMED_RECORD`) |
| P13 | no version-1 record silently validated under version-2 semantics | DP 6.2 | `PROJ#eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion` (frontier under a version-1 label: `MALFORMED_RECORD`; direct set under a version-1 label loads and stays version 1) |
| P14 | a build that predates version 2 rejects it cleanly and never reads it as version 1 | DP 11, ADR consequences | code-derived (base `ef120a54`, `SpatialIdentityRegistry.hasExpectedConstructionIdentityContract` requires `SCHEMA_VERSION == 1`), confirmed by a scratch probe on the extracted base tree (candidate report §6.3); not a committed test, because the base build is not part of the candidate |

## 2. Projection (disposition item 1, canonical invariants)

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| J1 | deterministic, structural, DAG-only; labels, coordinates and order are no evidence | DP 4.3, INV 1 | `PROJ#projectionIsStructuralAndIgnoresLabelsCoordinatesAndRepeatedPaths` |
| J2 | cycle-safe: diamonds and repeated inputs reach each ancestor once | DP 4.3 | `PROJ#projectionIsStructuralAndIgnoresLabelsCoordinatesAndRepeatedPaths` |
| J3 | ordinary helpers stay identity-free | DP 3, INV 2 | `PROJ#projectionIsStructuralAndIgnoresLabelsCoordinatesAndRepeatedPaths`, `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity`, `WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent` |
| J4 | one implementation for publication, assessment, validation and persistence | DP 4.3, DP 5, INV 3 | `PROJ#projectionIsStructuralAndIgnoresLabelsCoordinatesAndRepeatedPaths` (published record equals the projection), `VOC#anIdentityFreeInputNoLongerMakesTheProposalUndescribable` (candidate signature), `PROJ#eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion` (validation) |
| J5 | the rule is named only by a known version | DP 6.1, DP 6.4 | `PROJ#projectionRuleIsNamedOnlyByAKnownSchemaVersion` |

## 3. Certified auto-refresh (disposition items 3 and 10)

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| R1 | upstream numeric/geometry update with an unchanged frontier only recomputes | DP 10 | `REF#aFrontierNeutralEditOnlyRecomputes` |
| R2 | in-place soft redefinition of a frontier helper is frontier-neutral | DP 10 | `REF#anInPlaceSoftRedefineOfAFrontierHelperIsFrontierNeutral` |
| R3 | a structural upstream edit that passes the predicate keeps the identity with a new frontier and revisions | DP 10, ADR 4 | `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity`, `REF#aTransitionOutsideTheCertifiedStepFailsClosedAsAWhole` (second half) |
| R4 | dependents recompute; revision-scoped evidence is revalidated | DP 10 | `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity` (metric equals the analytic length and a from-scratch reopen) |
| R5 | save/reopen reconstructs the refreshed relation | DP 10 | `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity` |
| R6 | undo/redo restores geometry and frontier | DP 10 | `REF#aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity` |
| R7 | a non-compatible transition fails atomically, host reordering included | DP 10, ADR 9 | `REF#aTransitionOutsideTheCertifiedStepFailsClosedAsAWhole` (exact XML after failure) |
| R8 | a role-incomplete participant is never auto-refreshed | DP 10 (4) | `REF#aRoleIncompleteParticipantIsNeverAutoRefreshed` |

## 4. Explicit redefine and durable-contract change (disposition items 4–6 and 10)

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| X1 | identical-definition redefine is a no-op retain | DP 8.3 | `WIT#anIdenticalDefinitionIsANoOpRetain`, `PROJ#anExplicitRedefineMigratesOneHistoricalRecordAtomicallyEvenWithAnEqualSet` |
| X2 | explicit dependency change is typed `DURABLE_CONTRACT_CHANGE` | DP 9.1, ADR 5 | `DCC#explicitDependencySubstitutionIsATypedDurableContractChange`, `DCC#substitutingACoincidentDurableDependencyIsStillAContractChange` |
| X3 | the identity-preserving update is offered only when the kernel predicate authorizes it | DP 9.2, DP 9.3 | `VOC#aDurableContractChangeOffersOnlyTheOperationsItsInspectionAuthorizes`, `VOC#familyChangeIsATypedDurableContractChangeWithoutAnyOperation`, `DCC#anUnauthorizedOrStaleContractUpdateChangesNothing` |
| X4 | an accepted update retains identity and upgrades/persists version 2 | DP 9.3, DP 7.2 | `DCC#anAcceptedIdentityPreservingUpdateKeepsIdentityAndPersistsTheNewFrontier`, `DCC#anAcceptedContractUpdateMigratesAHistoricalRecord`, `UI#keepIdentityExecutesTheCertifiedContractUpdate` |
| X5 | a rejected update changes nothing | DP 7.3 | `DCC#anUnauthorizedOrStaleContractUpdateChangesNothing`, `DCC#defaultIntentNeverExecutesAContractChangeNorInfersReplacement` |
| X6 | explicit replacement creates a new identity | DP 9.2, ADR 6 | `DCC#explicitReplacementCreatesANewIdentityWithTheAnnouncedImpact`, `DCC#p08ReplacementIntentPublishesAFreshIdentityOnlyForAContractChange`, `UI#explicitReplacementCreatesAFreshIdentity` |
| X7 | replacement is never inferred from a retention failure | DP 9.2, INV 7 | `DCC#defaultIntentNeverExecutesAContractChangeNorInfersReplacement`, `VOC#aDurableContractChangeOffersOnlyTheOperationsItsInspectionAuthorizes` |
| X8 | the replacement impact report stays complete | DP 9.2, INV 7 | `DCC#explicitDependencySubstitutionIsATypedDurableContractChange`, `DCC#explicitReplacementCreatesANewIdentityWithTheAnnouncedImpact`, `UI#aCancelledContractChangeShowsTheReasonAndTheCompleteImpact` |
| X9 | Cartesian coincidence of old and new dependencies has no effect | DP 9.1, ADR 5 | `DCC#cartesianCoincidenceOfOldAndNewDependenciesIsNoEvidence`, `DCC#substitutingACoincidentDurableDependencyIsStillAContractChange`, `WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent` (`E` coincides with the identity-free `M`) |

## 5. Canonical R3 invariants and former-`AMBIGUOUS` causes

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| C1 | author witness as a minimal derived fixture: `E` retained, `a`, `e`, `g` and `I`–`L` recomputed, `M` unaffected, undo/redo, reopen | DP 8, INV 6 | `WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent` |
| C2 | genuine durable closure change still classified as a topology change | DP 8.3, DP 9.1 | `DCC#explicitDependencySubstitutionIsATypedDurableContractChange` |
| C3 | genuine provider/family/schema/authority/role/cardinality change fails closed | DP 9.4, INV 4 | `WIT#aGenuineFamilyChangeStillFailsClosedWithItsOwnReason`, `VOC#familyChangeIsATypedDurableContractChangeWithoutAnyOperation`, `VOC#changedCandidateCardinalityIsADurableContractChange`, `VOC#aChangedStableRoleIsADurableContractChange`, `VOC#aNonNeutralOrFutureContextIsUndescribable` |
| C4 | a real cycle stays `INVALID_DAG` | DP 9.4, INV 5 | `WIT#aRealCycleIsStillAnInvalidDag`, `PROJ#aRejectedOrCancelledEventLeavesTheHistoricalRecordUntouched` |
| C5 | an incomplete stable-role group fails closed | INV 5 | `WIT#anIncompleteStableRoleGroupStillFailsClosed` |
| C6 | atomic rollback | INV 6 | `PROJ#aRejectedOrCancelledEventLeavesTheHistoricalRecordUntouched`, `REF#aTransitionOutsideTheCertifiedStepFailsClosedAsAWhole`, `DCC#anUnauthorizedOrStaleContractUpdateChangesNothing` |
| C7 | narrowing only: every correct rejection stays a rejection | INV 8 | C3, C4, C5; the unchanged POST-G9U1-A3, G9A1 and G9A3 lifecycle suites; `G9U0PersistenceCompatibilityTest#p08AssignmentRequiresExplicitReplacementIntentAndPublishesFresh` |
| A1 | cause 1 — changed geo family | DP 9.4 | `VOC#familyChangeIsATypedDurableContractChangeWithoutAnyOperation` |
| A2 | cause 2 — "requires one output": several old outputs / changed candidate cardinality | DP 9.4 | `VOC#severalOldOutputsRemainAGenuineAmbiguityOfTheGroup`, `VOC#changedCandidateCardinalityIsADurableContractChange` |
| A3 | cause 3 — "one unambiguous output" in the effect | DP 9.4 | `VOC#anEffectWithoutOneTargetedOutputIsAmbiguous` |
| A4 | cause 4 — changed durable role contract | DP 9.4 | `VOC#aChangedStableRoleIsADurableContractChange` |
| A5 | cause 5 — ambiguous or missing context | DP 9.4 | `VOC#anAmbiguousOrMissingContextIsAmbiguous` |
| A6 | cause 6 — non-neutral context | DP 9.4 | `VOC#aNonNeutralOrFutureContextIsUndescribable` |
| A7 | cause 7 — unregistered dependency, now resolved by the frontier | DP 8.3 | `VOC#anIdentityFreeInputNoLongerMakesTheProposalUndescribable`, C1 |
| A8 | cause 8 — provider roles do not cover the host group | DP 9.4 | `VOC#providerRolesThatDoNotCoverTheHostGroupAreUndescribable` |
| A9 | cause 9 — targeted candidate not mapped | DP 9.4 | `VOC#anUnmappedTargetedCandidateIsUndescribable` |
| A10 | only a provider-typed ambiguity is `AMBIGUOUS`; an untyped failure is undescribable; other runtime failures stay `UNSUPPORTED` | DP 9.4 | `VOC#onlyAProviderTypedAmbiguityIsReportedAsAmbiguous` |
| A11 | the legacy boolean preparation path still fails closed | DP 9.4 | `VOC#theLegacyPreparationPathStillFailsClosedOnADescriptionFailure` |

## 6. Desktop presentation (disposition item 7)

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| D1 | every unavailable status shows its localized text and the kernel reason, in English and Spanish | ADR 8 | `UI#everyUnavailableStatusShowsItsTextAndTheKernelReason` |
| D2 | only kernel-authorized operations are offered and each maps to its mode; no status is upgraded | ADR 8 | `UI#onlyTheKernelAuthorizedContractOperationsAreOfferedAndMapped` |
| D3 | the witness retains without any dialog | ADR 8 | `UI#witnessRedefineRetainsWithoutAnyDialog` |
| D4 | the durable-contract dialog explains the reason, both operations and the complete impact; cancel changes nothing | ADR 8 | `UI#aCancelledContractChangeShowsTheReasonAndTheCompleteImpact` |
| D5 | keep identity and replace execute the kernel modes | ADR 6 | `UI#keepIdentityExecutesTheCertifiedContractUpdate`, `UI#explicitReplacementCreatesAFreshIdentity` |

## 7. Construction order (disposition items 8 and 11)

| # | Obligation | Disposition |
|---|---|---|
| O1 | minimal reorder: the single safe move of `B` before `A` | not integrated; recorded as `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` (candidate report §4) |
| O2 | undo/redo, failure and complex-dependency cases of the refinement | not applicable while O1 is deferred; the host's GeoGebra ordering is unchanged and never evidence (J1) |

## 8. Superseded pins

| Test | Former pin | Now | Reason |
|---|---|---|---|
| `G9U0PersistenceCompatibilityTest#p08AssignmentRequiresExplicitReplacementIntentAndPublishesFresh` | `s := a+1` rejected with an identity-free `a` (false `AMBIGUOUS`), and rejected with replacement intent over a durable `a` | compatible retain over an identity-free `a`; refusal without an explicit operation over a durable `a`; the explicit operations moved to `DCC` | D3-a resolution; disposition item 5 |
| `PostG9U1A1SplineConstructorProvenanceTest` (`{B,A,C}` replacement intent) | `REDEFINE_INCOMPATIBLE` (undescribable through identity-free list members) | `REDEFINE_REJECTED`: a compatible retain is available, so replacement intent is not authorized | D3-a resolution; still a rejection |
| `PreG9bR2E1CertifiedConstructionModelTest#theInlineLiteralDefectShapeIsRefusedRatherThanCertified` | the defective locus evaluates without a certified program | the locus fails closed at construction (undefined) | version-2 validation rejects the evaluator's duplicated-driver replay; `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` retained |

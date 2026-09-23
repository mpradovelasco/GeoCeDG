# PRE-G9B-R2-E0/E1 validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Frozen in Stage 1, before implementation, from the author's combined
`PRE-G9B-R2-E0/E1` instruction and the `E0` prompt's adversarial list. Each row
maps an obligation to the normative clause it exercises and to its executable
evidence. The evidence column names the implemented tests. Every row whose
evidence or disposition differs from the frozen plan is listed, with its reason,
in the Stage-2 reconciliation at the end. Test classes:

```text
PAIR  = org.geocedg.common.locus.PreG9bR2E1SemanticPairMaterializationTest
MODEL = org.geocedg.common.kernel.locus.intersection.PreG9bR2E1CertifiedConstructionModelTest
END   = org.geocedg.common.locus.PreG9bR2E0SemanticEndpointAdmissibilityTest
EVID  = org.geocedg.common.kernel.locus.intersection.PreG9bR2E0SingleSourceEndpointEvidenceTest
NAT   = org.geocedg.desktop.PreG9bR2E1SemanticPairNativeArchiveTest
```

Clause abbreviations: `M24.x` = `locus-v2-metrics.md` §24.x; `CM x` =
`locus-v2-certified-construction-model.md` §x; `PM10` =
`spline-v2-pair-materialization.md` §10; `PM x` = its §x.

## Objective B — pair materialization

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| B1 | `SeveralDefects` mixed pair `e = Intersect(d,a)`: both germ slots certified and materialized; global completeness stays `NOT_ESTABLISHED` (incomplete global search with locally certified roots) | PM10, PM1, CM 2 | `PAIR#severalDefectsMixedPairMaterializesBothCertifiedGermSlots` |
| B2 | caller/source-order reversal `Intersect(a,d)` gives the same canonical selectors and transposed addresses | PM2, PM10 | `PAIR#callerReversalYieldsTheSameCanonicalSelectorsAndAddresses` |
| B3 | generic × generic (two construction loci) | PM10 | `PAIR#constructionByConstructionPairMaterializes` |
| B4 | similarity image of a construction locus | PM10, CM 7 | `PAIR#similarityImageOfAConstructionLocusMaterializes` |
| B5 | direct scalar affine pair materializes; an uncertifiable scalar pair stays rich-only with an explicit diagnostic | PM10, CM 4 | `PAIR#affineScalarPairMaterializesAndUncertifiableScalarIsDiagnosedRichOnly` |
| B6 | every class-v1 driver and operation certifies in a real pair | CM 4, CM 5 | `PAIR#everyClassV1DriverAndOperationCertifiesInARealPair`; the segment driver's inhomogeneous-read lag is refused by the floating coherence gate: `PAIR#segmentDriverInhomogeneousReadIsRefusedByTheCoherenceGate` |
| B7 | slices outside class v1 (expression point, line–conic intersection, selected Locus V2 root) stay rich-only, diagnosed | CM 3, CM 9 | `PAIR#slicesOutsideClassV1StayRichOnlyWithDiagnostic` |
| B8 | periodic seam: a crossing at the circle-point seam is unresolved for its germ while the opposite germ stays admissible | CM 7, PM10 | `PAIR#seamCrossingIsUnresolvedWhileTheOppositeGermIsAdmissible` |
| B9 | tangency / multiplicity where identity is not established blocks both germs | PM3, PM10 | `PAIR#tangencyBlocksBothGermClassesOfItsComponentPair` |
| B10 | source motion `1 -> many -> 1`: quarantine then reactivation of the same point and token | PM6 | `PAIR#sameGermMultiplicityQuarantinesAndRecoversTheSameSlot` |
| B11 | source motion `1 -> 0 -> 1`: dormancy then reactivation of the same point and token | PM6 | `PAIR#rootLossAndReturnReactivatesTheSameSlot` |
| B12 | temporary invalidity of the construction locus and reactivation | PM6, CM 8 | `PAIR#temporaryConstructionInvalidityIsDormantAndRecovers` |
| B13 | `S × S` self-pair publishes no admissible root | PM2 | `PAIR#selfPairOfAConstructionLocusPublishesNoAdmissibleRoot` |
| B14 | XML reopen and undo/redo preserve the slot; a closure copy re-certifies, and for a generator-driven side its copied point stays dormant (PM10 limitation) | PM7, PM10 | `PAIR#reopenAndUndoRedoPreserveTheCertifiedSlot`, `PAIR#closureCopyOfAConstructionPairReCertifiesAndLeavesTheCopiedPointDormant` |
| B15 | compatible redefine keeps the slot; incompatible replacement is a barrier | PM7 | `PAIR#compatibleAndIncompatibleRedefineFollowTheLifecycle` |
| B16 | corrupted / old pair ledger fails closed | PM7 | `PAIR#corruptedOrUnknownPairLedgerFailsClosed` |
| B17 | globally complete result whose local identity is ambiguous stays unmaterializable | PM1, PM3 | structural: `MODEL#globalCompletenessNeverMakesAnAmbiguousRootAdmissible` |
| B18 | native `.cedg` save/reopen, dormant save/reopen/reactivation | PM7 | `NAT#nativeArchiveReconstructsCertifiedPairSlotsAndEndpointFamilies` |
| B19 | the model encloses the evaluator and its derivative for every driver and operation | CM 2, CM 5, CM 6 | `MODEL#modelEnclosesTheEvaluatorAndItsDerivativeForEveryOperation` |
| B20 | degenerate predicates (parallel meet, coincident join, infinite point) refuse instead of guessing | CM 5.1 | `MODEL#undecidedPredicatesRefuseTheBox` |
| B21 | capture refuses every shape outside class v1 | CM 3 | `MODEL#captureRefusesEveryShapeOutsideClassV1` |
| B22 | period is zero, knots span the declared domain, canonicalization is the provider's | CM 7 | `MODEL#modelObligationsMatchTheContract` |
| B23 | trigonometric enclosure is rigorous across critical points | CM 6 | `MODEL#trigonometricEnclosureIsRigorousAcrossCriticalPoints` |
| B24 | bounded-work exhaustion leaves classes unresolved | PM4 | `MODEL#exhaustedCertificationBudgetIsUnresolved` |
| B25 | spline × spline behaviour is unchanged | PM1–PM9 | every existing `G9S1R1*` test, unchanged except the superseded pin in B26 |
| B26 | superseded pin: the G9S1-R1 generic-pair regression now uses an uncertifiable generic locus | ADR 0028 | `G9S1R1SplinePairMaterializationTest#genericLocusPairStaysRichOnlyDespiteSharedInfrastructure` |
| B27 | superseded pin: the roots of the G9U0 affine × affine lineage pair are admissible, invariantly under source order | ADR 0028, PM2 | `G9U0IntersectionTokenTest#i17CanonicalLocusPairLineageIsSourceOrderInvariant` |
| B28 | superseded pin: the affine Locus V2 target of the G9U0-R4 supported-target regression receives exact evidence | ADR 0028, PM10 | `G9U0R4IntersectionAdmissibilityContinuationTest#stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence` |

## Objective A — endpoint admissibility

| # | Obligation | Clause | Evidence |
|---|---|---|---|
| A1 | existing semantic point endpoints unchanged | M24.2 | existing `G9U0MetricPositionTest`, `PostG9U1A2*`, `G9U1MetricReviewTest` |
| A2 | constructor / interpolation endpoints unchanged, including R5 transport | §21 | existing `PostG9U1A1SplineConstructorProvenanceTest` |
| A3 | `SplineV2 × SplineV2` endpoints unchanged | §23 | existing `PreG9bR2*` tests |
| A4 | SplineV2 × line and SplineV2 × circle/conic endpoints | M24.4 | `END#singleSourceRootsOnLinesAndConicsAreEndpointsOnTheirSource` |
| A5 | LocusV2 × ordinary object endpoints (witness `g = Intersect(a,c)`) | M24.4 | `END#witnessEllipseCircleRootsAreEndpointsOnTheEllipse` |
| A6 | generic semantic-pair materialized endpoints (witness `e`) | §23, PM10 | `END#certifiedGenericPairPointsAreEndpointsOnBothSources` |
| A7 | generator occurrence follows the live driver | M24.5 | `END#generatorPointIsAnEndpointAtTheLiveDriverParameter` |
| A8 | similarity image with identical parameter objects, including images of a semantic point and of the generator; literals rejected | M24.6 | `END#similarityImagePointsRequireTheSameParameterObjects` |
| A9 | dependent copy inherits membership; free copy and transported copy rejected | M24.7 | `END#dependentCopiesInheritMembershipOnly` |
| A10 | no transport of membership points through R5 images; generator transport not admitted | M24.8 | `END#membershipPointsAreNotTransportedThroughImages` |
| A11 | free Cartesian-coincident point; coincident roots of a self-intersecting source stay fail-closed and are never merged; coincident semantic points stay distinct | M24.1, M24.4 | `END#coincidentPointsAndCoincidentRootsAreNeverMerged` |
| A12 | wrong addressed source | M24.4 | `END#wrongSourceAndDormantEndpointsFailClosed` |
| A13 | dormant point; reactivation restores the same key | M24.3 | `END#wrongSourceAndDormantEndpointsFailClosed` |
| A14 | stale semantic revision is final `UNRESOLVED_INVALID` | M24.4 | `EVID#aStaleRootIsFinalAndNeverFallsThrough` |
| A15 | component mismatch / no containing component | M24.4 | `EVID#aDisagreeingComponentOrBranchIsUnresolved`; positive control `EVID#aCurrentRootIsUniqueOnItsOwnComponent` |
| A16 | duplicate-token ambiguity never reaches the family | M24.4, §23.2 | `EVID#aDuplicatedSingleSourceTokenIsNeverPointAdmissible` |
| A17 | multi-preimage / self-intersection ambiguity (`S × S`) | §23.2 | existing `PreG9bR2IntersectionEndpointSideSelectionTest#aSelfPairRootWithBothSidesOnTheAddressedSourceIsAmbiguous`; `PAIR#selfPairOfAConstructionLocusPublishesNoAdmissibleRoot`; `END#coincidentPointsAndCoincidentRootsAreNeverMerged` |
| A18 | family change is retargeting; not reachable in one metric object under the current lifecycle | M24.3 | `END#familyChangeByRedefinitionIsAFreshResolutionNeverARetarget` |
| A19 | XML reopen, undo/redo, copy/remap revalidate every new family | M24.9 | `END#reopenUndoRedoAndCopyRevalidateEveryNewFamily` |
| A20 | compatible redefine follows the geometry | M24.9 | `END#compatibleRedefineFollowsTheGeometry` |
| A21 | ineligible transforms (circle inversion) fail closed; nested images compose through a structurally keyed intermediate | M24.3, M24.6 | `END#ineligibleTransformsFailClosedAndNestedImagesCompose` |
| A22 | native `.cedg` reopen of pair, single-source, generator, copy and image endpoints, including dormancy | M24.9 | `NAT#nativeArchiveReconstructsCertifiedPairSlotsAndEndpointFamilies` |
| A23 | superseded §23.8 bullet 1 in the R2 exclusion test | M24.8 | `PreG9bR2SemanticIntersectionEndpointTest#unadmittedShapesStayInvalidAndA1FallThroughIsPreserved` |
| A24 | a missing durable source, rich-result or endpoint identity is `UNRESOLVED_INVALID` | M24.3, M24.4 | `EVID#missingDurableIdentitiesAreUnresolved` |
| A25 | the user guide's worked endpoint example (§7.5, §9.3) holds as documented in both editions | M24.2, §21 | `END#userGuideWorkedExampleEndpointsHoldAsDocumented`; desktop `PostP1BilingualUserGuideTest` |

## Retained negative regressions

`PostG9U1A1SplineConstructorProvenanceTest#coincidenceAndSelfIntersectionNeverCreateExtraProvenance`
and `G9U1MetricReviewTest#foreignOrCoincidentPointsNeverReceiveInferredMetricPreimages`
use only free and foreign semantic points and must pass unchanged.

## Stage-2 reconciliation

The frozen obligations keep their meaning, except where the design record §14
records a correction. Evidence differs from the frozen plan as follows.

| Row | Frozen plan | Implemented evidence | Reason |
|---|---|---|---|
| B6 | one test | adds `segmentDriverInhomogeneousReadIsRefusedByTheCoherenceGate` | retained debt `RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG` (construction-model spec §2), pinned fail-closed |
| B14 | one test; the copy preserves the slot | two tests; the copied point stays dormant | limitation `PAIR-COPY-GENERATOR-PARAMETERIZATION-CONTRACT` (pair spec §10, design §14) |
| B17 | end-to-end pair test | structural `MODEL` test | D-C4: no capability publishes `COMPLETE` with finite roots |
| B18, A22 | `severalDefectsPairAndEndpointsSurviveNativeReopen` | `nativeArchiveReconstructsCertifiedPairSlotsAndEndpointFamilies` | one native lifecycle test covers both rows |
| A14–A16 | planned names | implemented names | same obligations |
| A18 | `familyChangeIsRetargetingAndFailsClosed` | `familyChangeByRedefinitionIsAFreshResolutionNeverARetarget` | D-C3: `NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`; both lifecycle outcomes are pinned |
| A21 | `ineligibleTransformsFailClosed` | `ineligibleTransformsFailClosedAndNestedImagesCompose` | D-C2: nested derivations compose |
| B27, B28 | — | new rows | D-C5: the first complete `final.shared` run failed exactly these two pins of the pre-ADR-0028 state |
| A24 | — | new row | identity requirements of M24.3 and M24.4 |
| A25 | — | new row | the guide's §9.3 statement had been false since A1; the corrected text is now pinned |

The `SeveralDefects` witness itself is an untracked author input
(`artifacts/author-input/post-p1-defects/SeveralDefects.cedg`, SHA-256
`c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8`). The
tracked tests rebuild its construction by commands. A temporary desktop probe,
not a tracked test, also replayed the file once on the candidate implementation;
the candidate report records that replay.

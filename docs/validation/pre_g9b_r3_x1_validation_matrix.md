# PRE-G9B-R3-X1 validation matrix

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

Each row maps an obligation of the author's `PRE-G9B-R3-X1` instruction to its
executable evidence and to the behaviour on the base `78134aa8…`. The
[design record](../architecture/pre_g9b_r3_x1_certified_expression_point_design.md)
gives the mechanisms. Every new or converted test was also run on an extracted copy
of the base tree, with the few new-API references mapped to equivalent base
expressions (`getVersion()` to the signature prefix, `VERSION_V2` to its literal,
`EXPRESSION_TRANSLATE` to `Operation.valueOf`); only the controls pass there.

```text
MODEL   = org.geocedg.common.kernel.locus.intersection.PreG9bR3X1CertifiedExpressionPointModelTest
CAPTURE = org.geocedg.common.kernel.locus.PreG9bR3X1CaptureBoundaryTest
PAIR    = org.geocedg.common.locus.PreG9bR3X1ExpressionPointPairMaterializationTest
WITNESS = org.geocedg.common.locus.PreG9bR3X1RedefineWitnessTest
EVAL    = org.geocedg.common.locus.PreG9bR3X1EvaluatorCorrectnessTest
E1MODEL = org.geocedg.common.kernel.locus.intersection.PreG9bR2E1CertifiedConstructionModelTest
E1PAIR  = org.geocedg.common.locus.PreG9bR2E1SemanticPairMaterializationTest
```

## 1. Primary X1 capability (instruction §22)

| # | Obligation | Evidence | Base behaviour |
|---|---|---|---|
| A1 | `C+(1,0)` captured | `MODEL#theBoundedGrammarCapturesEveryAuthorizedExpressionForm`, `PAIR#literalAndNamedVectorPairsMaterializeTheirCertifiedRoots` | no program |
| A2 | `C+u` captured | same | no program |
| A3 | `Translate(C,u)` unchanged v1 control | `MODEL#classV1ProgramsKeepTheirVersionIdentifierAndSignature` (signature pinned byte for byte) | passes (unchanged) |
| A4 | arbitrary coordinate expression stays unsupported | `MODEL#expressionShapesOutsideTheGrammarStayWithoutAProgram` (`(x(C)/2,y(C))`, `C+(x(A),0)`, `C+(1/3,0)`) | passes (unchanged) |
| A5 | nonlinear or driver-dependent vector expression stays unsupported | same (`C+2u`, `C+u+u`, `C+(1,0)+(0,1)`, `C+A`, `C+(1;0)`, `(1,0)-C`, `C+w`, `A+w`) | passes (unchanged) |
| A6 | nonfinite or undefined constant vector rejected | `CAPTURE#undefinedOrNonfiniteVectorObjectsAreRefused`, `MODEL#nonfiniteOrUndefinedConstantVectorsAreRefused` | no program in any case |
| A7 | symmetric and inverse forms (`V+P`, `P-V`) literally the same proof problem | `MODEL#theBoundedGrammarCapturesEveryAuthorizedExpressionForm` (`(1,0)+C`, `u+C`, `C-(1,0)`, `C-u`, negated vector) | no program |
| B1 | v1-only construction keeps v1 program and signature | `MODEL#classV1ProgramsKeepTheirVersionIdentifierAndSignature`, `MODEL#theVersionIsDerivedFromTheSteps`, `CAPTURE#theProgramVersionIsDerivedFromItsSteps` | signatures identical |
| B2 | expression construction is v2 | same, and `WITNESS#redefiningIntoTheWitnessExpressionKeepsTheCertifiedPairAndItsPoint` | no program |
| C1 | point and derivative enclosure for every v2 form | `MODEL#everyV2FormEnclosesTheEvaluatorAndItsDerivative` (literal, vector object, `V+P`, `P-V`, the R3 midpoint form, a rotated expression, a meet operand with non-unit `z`, a segment driver) | no model |
| C2 | comparison with evaluator samples as validation only | same (samples inside outward enclosures, finite-difference derivative) | not applicable |
| C3 | unresolved and invalid boxes fail closed | `MODEL#unresolvedAndInvalidBoxesFailClosed` (meet at infinity, outside the domain, interval overflow) | no model |
| C4 | model obligations (period, knots, smoothness, canonicalization) | `MODEL#theModelObligationsOfAV2ProgramMatchTheContract` | no model |
| D1 | transverse roots become certifiable | `PAIR#literalAndNamedVectorPairsMaterializeTheirCertifiedRoots` (`UNIQUE` germs `±1`, `ESTABLISHED` isolation) | 0 admissible, ledger v4 |
| D2 | caller reversal | `PAIR#callerReversalYieldsTheSameCanonicalSelectors` | 0 admissible |
| D3 | no token for an uncertified expression | `PAIR#anUncertifiedExpressionIssuesNoPairToken` (ledger v4, diagnostic) | passes (unchanged) |
| D4 | no false tangency or transversality upgrade | `PAIR#aTangencyIsNotUpgradedToATransverseRoot` | passes (unchanged) |
| D5 | the coherence gate still refuses a certified slot it rejects | `MODEL#theFloatingVerificationStillRefusesACertifiedSlotItRejects` (unattainable residual tolerance; `UNIQUE`, refused, nothing admissible) | no certificate to refuse |
| E1 | exact token emission and materialized roots | `PAIR#literalAndNamedVectorPairsMaterializeTheirCertifiedRoots` (ledger v5) | none |
| E2 | the point follows the current semantic root, no retargeting | `PAIR#aMaterializedRootFollowsItsSlotWithoutRetargeting` (same token, selector and id under vector motion; dormant, then reactivated) | none |
| F | `local admissibility != global completeness` | `PAIR#literalAndNamedVectorPairsMaterializeTheirCertifiedRoots` asserts `NOT_ESTABLISHED` | not applicable |
| G1 | R3 witness, redefine form, undo, redo, reopen | `WITNESS#redefiningIntoTheWitnessExpressionKeepsTheCertifiedPairAndItsPoint` | pair rich-only after the redefine, point dormant |
| G2 | R3 witness, direct form | `WITNESS#theDirectWitnessExpressionCertifiesTheSamePair` | no program |
| G3 | compatible redefine between supported vectors | `WITNESS#aRedefineBetweenSupportedVectorsKeepsTheSlot` (literal to literal, literal to named vector, vector motion) | rich-only |
| G4 | unsupported expression after redefine is truthfully fail-closed | `WITNESS#aRedefineIntoAnUnsupportedExpressionIsTruthfullyRichOnly` (diagnostic, dormant point, same token, undo) | passes (unchanged) |
| G5 | `g = Intersect(a,c)` and its materialized roots unaffected | `WITNESS#redefiningIntoTheWitnessExpressionKeepsTheCertifiedPairAndItsPoint` (`I`…`L` on the redefined intersections) | passes the `g` part |
| H1 | copy/remap, literal case | `PAIR#closureCopyOfALiteralExpressionLocusRecapturesAndStaysCurrent` (fresh ids, literal stays a literal, v2 recaptured, copied selector names copied ids, no cross-link) | copied pair rich-only |
| H2 | copy/remap, named-vector case | `PAIR#closureCopyOfANamedVectorExpressionFollowsTheCopiedVector` (copied expression references the copied vector; each vector moves only its own curve) | copied pair rich-only |
| I | the existing negative expression fixture stays rich-only | `MODEL#expressionShapesOutsideTheGrammarStayWithoutAProgram`, `E1PAIR#slicesOutsideClassV1StayRichOnlyWithDiagnostic`, `E1MODEL#captureRefusesEveryShapeOutsideClassV1` | passes (unchanged) |
| M | metric endpoints arise only through existing families | `PAIR#theExistingPairOccurrenceMakesAMaterializedRootAMetricEndpoint` (`PAIR_INTERSECTION_OCCURRENCE`, generator occurrence of the traced point, `NO_ADDRESS` for another expression point, finite `LocusLength`) | roots not materializable |
| P | persistence: no certificate data in `.cedg` | `PAIR#undoRedoAndReopenPreserveTheCertifiedSlot` (document contains no `certified-construction-program`) | not applicable |

## 2. Evaluator debts (instruction §23)

| # | Obligation | Evidence | Base behaviour |
|---|---|---|---|
| S1 | segment driver current at the requested parameter, canonical expected value, several driver parameters and orders | `EVAL#aSegmentDriverIsCurrentAtEveryRequestedParameter` (`(2t, 1)` exactly, 13 parameters, three positions of `K`) | fails: `t=0.1` gives `x=0.0` |
| S2 | the author's recorded manifestation, expected `1` | same: `LocusLength(ls,Pa,Pb) = 1`, `Length(ls,Pa,Pb) = 1`, `LocusLength(ls) = 2` | scratch probe on the base: `2.25`, `1.5` and `3.5` |
| S3 | evaluator coherent with the live construction for every path driver | `EVAL#theEvaluatorReproducesTheLiveConstructionForEveryPathDriver` (bit-identical to the live DAG for segment, circle and arc) | fails on the segment: `1.74609375` where `-0.25` is expected |
| S4 | the previous fail-closed fixture becomes valid because it is mathematically valid | `E1PAIR#segmentDriverInhomogeneousReadIsCurrentAndCertified` (converted; roots materialize on `y = x - 1`) | fails (refused) |
| L1 | the retained inline-literal fixture varies with its driver, is not a fixed point and matches its source | `EVAL#theInlineLiteralFixtureKeepsItsDriverAndMatchesItsSource` (bit-identical to the live construction, equal to the named-point construction, same length, reopen) | fails: created undefined |
| L2 | certified model only after floating correctness | `E1MODEL#theInlineLiteralShapeIsReplayedWithItsDriverAndCertified` (converted), `CAPTURE#aTracedPointThatDoesNotDependOnTheDriverHasNoProgram` (rule 5 still refuses a disconnected slice) | fails: undefined |
| L3 | the single-literal positive stays valid | `E1MODEL#aSingleInlineLiteralSliceIsReplayedAndCertified` (unchanged) | passes |

## 3. Regressions (instruction §24)

| Suite | Evidence |
|---|---|
| all E0/E1 certified construction model tests | `E1MODEL` 9 and `E1PAIR` 19 pass; the two converted methods are the only changes |
| spline pair materialization, G9U0 tokens, R3/R3-R1 redefine and frontier, C1 and C1-U1 clipboard, copy/remap lineage, metric endpoints, v1/v2 identity lifecycle, native save/reopen | the full `:shared:common-jre:test` run: 6 842 tests, 0 failures or errors, 10 skips; the full `:desktop:desktop:test` run: 1 533 tests, 0 failures or errors, 1 skip |

## 4. Invariants

| Invariant | Evidence |
|---|---|
| capability, not class-name admission | A4, A5, I: `AlgoDependentPoint` outside the grammar stays refused |
| exact structural authority, never values | section 4 of the design record; A7 uses exact IEEE identities; no coordinate comparison enters capture |
| identity chain unchanged | no selector, token, ledger or materialization source changed; D2, E2, G1, H1 |
| certificate material is not identity | B1, P |
| unsupported stays fail-closed with truthful diagnostics | A4, A5, D3, G4 |
| no tolerance change, gate not weakened | D5; no tolerance constant changed |
| no serialization, schema or migration change | P; no schema or `.cedg` format file touched |

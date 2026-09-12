# POST-G9U1-A3 V2 compatible-redefine validation matrix

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `b12d9ad04a766e893b120f3155250d5f1654c19b`
- Required profile: `PHASE -Phase POST-G9U1-A3`
- Product implementation authorized: **true**
- Self approval: **false**
- Guide impact: **NO**

This matrix traces the bounded A3-R1 specialization of the accepted G9A3
lifecycle and the author-approved A4/P3 position contract, with P3-R1 as its
explicit author-approved refinement/amendment. A successful PHASE result is technical evidence only
and does not authorize publication.

| Obligation | Focused evidence | Expected result |
|---|---|---|
| Same V2 contract, changed dependencies | `compatibleLocusRedefineRetainsIdentityAndAdvancesTopology` | IDs retained; definition/topology revisions advance; candidate DAG current |
| A1/A2 downstream revalidation | `splineRedefineRevalidatesA1AndA2BindingsWithoutStaleGeometry` | old occurrence becomes `ABSENT/INVALID_QUERY`; restoring the same occurrence recovers without retargeting |
| Different constructor contract | `explicitReplacementIsFreshAndDifferentConstructorRejectsWithoutIntent` | no-intent edit rejects atomically; explicit legacy selection receives a fresh ID; one Undo restores the exact entry state |
| R5 transform constructor contract | `transformContractRetainsOnlyTheSameSemanticConstructorFamily` | same transform family retains; changed family rejects or is explicitly fresh |
| P3 successful retained placement | `compatibleLocusRedefineRetainsIdentityAndAdvancesTopology` | target remains before the surviving right anchor within DAG bounds |
| Minimal later relocation | `newPredecessorConstraintForcesOnlyTheMinimalLaterRelocation` | compatible topology change moves only after the new lower predecessor; unaffected order and identity remain |
| Symmetric dependent bound | `compatibleLocusRedefineRetainsIdentityAndAdvancesTopology` | existing point and metric dependents remain after the retained source; the host cannot publish a target beyond this upper bound, while target-only A3 introduces no independent “new dependent” operation |
| Rename and delete/recreate | `renameAndDeleteRecreateDoNotImpersonateContinuity` | rename retains; delete/recreate is fresh |
| Ordinary host redefine | `renameAndDeleteRecreateDoNotImpersonateContinuity` | a nonparticipating numeric remains on the inherited host path and acquires no CeDG identity |
| Undo/redo retained transaction | `compatibleLocusRedefineRetainsIdentityAndAdvancesTopology` | undo restores entry revisions/ID; redo restores byte-equal committed XML |
| Ordinary unregistered procedural evidence | `ordinaryUnregisteredNeighborsRemainTransactionLocalP3Evidence` | ordinary neighbours constrain the transaction without gaining durable CeDG identity |
| Adjacent anchors disappear | `capturedOuterIntervalSurvivesAdjacentRemovalButNoAnchorFailsClosed` | only precaptured outer anchor chains may supply bounds |
| One surviving bound | same focused anchor-policy test | surviving precaptured bound plus DAG is accepted |
| No captured anchor survives | same focused anchor-policy test | explicit fail-closed result; no nearest-neighbour adoption |
| Multi-output producer identity and forced relocation | `completeRoleMapAnchorsMultiOutputProducerWithoutUsingOrdinal` | complete role-to-ID map resolves one producer independently of output ordinal; a genuine later dependency forces the group to the P3-R1 clamp destination while all unaffected elements retain relative order |
| Missing/duplicate/changed roles and cardinality | `G9A3SpatialRedefineTransactionTest` | existing complete-group checks reject atomically |
| Typed non-mutating assessment | `preflightDistinguishesAdvancedRelocationLegacyAndInvalidDag` | advanced/no-move, advanced/relocation, legacy and cyclic-invalid are distinct results; cycle offers no fallback |
| Explicit mode and staleness | `retainFailureNeedsExplicitLegacySelectionAndStalePlanRejects` | retention failure cannot execute fresh; legacy is explicit; a real mutation invalidates both an assessment before prepare and a prepared transaction before host mutation/commit |
| Complete semantic impact | `legacyImpactIsCompleteTypedAndNeverUsesCoincidentUnrelatedGeometry` | durable closure is complete and deterministic; source/dependents/broken relation receive typed outcomes; coincident unrelated geometry is absent; no unproved auto-revalidation |
| Incomplete impact is not offerable | `incompleteImpactCannotMasqueradeAsComplete` | the kernel offerability guard accepts only `IMPACT_COMPLETE`; malformed/unresolved live registry state fails earlier and is never fabricated by the test |
| Save/reopen, copy/remap and rollback | A1/A2 and G9A3 selected regressions | existing durable closure/remap/XML rollback contracts remain valid |
| Spline V2 structural lifecycle | `G9S1R1StructuralSplineLifecycleTest` | structural persistence and explicit replacement regressions remain valid |
| Public persistence compatibility | `G9U0PersistenceCompatibilityTest` | legacy/unassociated state gains no inferred continuity |
| No coordinate/label/index/UI authority | focused A3 tests plus static contract review | decisions use provider/schema/contract/roles/IDs/DAG/precaptured anchors only |

## Selection boundary

The A3 PHASE selection contains the four dedicated A3/A3-R1 classes plus directly
affected G9A3 transaction, public V2 persistence, structural Spline V2, A1
constructor-provenance and A2 traversal-policy regressions. It does not broaden
into G9B, A5, A7, `INTEGRATION` or `FINAL`.

Final candidate commit/tree, plan hashes, result identity and receipt paths are
reported by the immutable-candidate PHASE run rather than predicted here.

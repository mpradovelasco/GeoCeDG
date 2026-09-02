# G9U0-R6 semantic Locus point interaction validation matrix

- Status: **PASS — AUTHOR APPROVED**
- Phase: G9U0-R6
- Declared active scenarios: **72**
- Manual GUI smoke: **DEFERRED TO G9U1 BY DESIGN**
- Kernel diagnostic acceptance: **PASS**

Every row is active. The implementation seam, exact focused test methods and
counts are frozen by the R6 closeout. Automated evidence remains distinct from
the author's explicit approval.

## A. Authority, model and API

| ID | Scenario | Required result |
|---|---|---|
| R6-A01 | shared-kernel placement | resolver and editable address authority have no Desktop/Euclidian dependency |
| R6-A02 | non-Path boundary | `GeoLocusV2` remains outside generic `Path` and legacy mutable path parameter semantics |
| R6-A03 | typed result states | none/unique/multiple/unresolved/invalid/degenerate/unsupported remain distinguishable |
| R6-A04 | candidate evidence | source/revision/branch/component/parameter/residual/regularity/guarantee/diagnostics are coherent |
| R6-A05 | forward verification | every candidate re-evaluates through semantic authority within declared evidence |
| R6-A06 | interaction policy separation | world hit distance may filter/rank but never enters identity or persistence |
| R6-A07 | no public parallel command | existing exact Point surface remains; no resolver-specific command/feature flag |
| R6-A08 | current snapshot | stale source revision produces typed invalid/noncurrent result and no candidate reuse |

## B. General Locus V2 inverse resolution

| ID | Scenario | Required result |
|---|---|---|
| R6-G01 | straight scalar locus | unique known projection and exact forward/inverse round trip |
| R6-G02 | no nearby preimage | supported bounded search returns none, not a fabricated endpoint |
| R6-G03 | several equal minima | deterministic canonical multiple-candidate set; no silent choice |
| R6-G04 | disconnected components | candidates retain component identity and never cross an invalid gap |
| R6-G05 | multiple branches | branch-local candidates remain distinct and canonically ordered for presentation only |
| R6-G06 | cusp/zero speed | uniqueness requires sufficient local evidence; otherwise unresolved/ambiguous |
| R6-G07 | high curvature | bounded adaptive semantic refinement reaches forward-verified result without render authority |
| R6-G08 | evaluator work limit | explicit unresolved/work-limit state and no partial unique claim |
| R6-G09 | unbounded provider | explicit supported bounded policy or typed unsupported; no hidden global search |
| R6-G10 | dynamic valid components | source revision changes candidate set coherently and discards revision-scoped hints |
| R6-G11 | certified affine coverage | provider-owned structural `F(u)=a*u+b` certificate searches every requested finite component analytically, with forward verification and no fitting from samples; nonfinite transformed capture atomically removes only the optional certificate, leaves the semantic transform defined with typed `NON_FINITE` evaluation, and recovers on a finite revision |
| R6-G12 | incomplete evaluator coverage | bounded evaluator-only search with zero or one candidate remains unresolved; several established distinct preimages may report multiple but never complete enumeration |

## C. SplineV2 specialization

| ID | Scenario | Required result |
|---|---|---|
| R6-S01 | span interior | polynomial distance-stationary strategy resolves unique canonical address |
| R6-S02 | exact internal knot | right-owned knot yields one candidate, not adjacent-span duplicates |
| R6-S03 | endpoint | nonperiodic endpoint ownership/evaluation is canonical |
| R6-S04 | self-intersection | equal image with distinct parameters returns multiple semantic candidates |
| R6-S05 | repeated/zero span | typed degenerate or unresolved result, no arbitrary span ownership |
| R6-S06 | cusp/zero derivative | local regularity/uniqueness evidence remains truthful |
| R6-S07 | span rejection | provider polynomial bounds reject irrelevant spans without evaluation/refinement |
| R6-S08 | enumeration perturbation | shuffled span/candidate discovery gives the same canonical candidate set |

## D. Interactive semantic point creation and drag

| ID | Scenario | Required result |
|---|---|---|
| R6-P01 | unique candidate creation | explicit choice creates exactly one ordinary point and one normal semantic parent |
| R6-P02 | no candidate creation | zero point/auxiliary residue and atomic failure |
| R6-P03 | ambiguous creation | no point until an exact candidate is explicitly selected |
| R6-P04 | address-owned drag | same point/ID updates explicit semantic address and follows forward evaluation |
| R6-P05 | ordinary regular drag | many updates remain defined without point/algo churn |
| R6-P06 | self-intersection drag | incompatible multiple preimages fail closed; no branch jump |
| R6-P07 | invalid-gap drag | point does not bridge disconnected components by Cartesian nearness |
| R6-P08 | source invalid/recovery | point becomes undefined without stale coordinate/current binding, retains its durable last-accepted selector without retargeting, and recovers the same point only when that address validates again |
| R6-P09 | downstream DAG | metrics/intersections/loci depending on point recompute once through normal graph |
| R6-P10 | original click discarded | later recompute/drag result is independent of creation click and event history |
| R6-P11 | host-snapshot rollback | failure after the first address-input write restores the complete Construction/XML/identity state through the existing host restore seam |
| R6-P12 | auxiliary ownership | only the `LOCUS_INTERACTION_POINT` role plus exact dedicated input structure owns hidden auxiliaries; ordinary encoded inputs retain user presentation |

## E. Periodic and transformed sources

| ID | Scenario | Required result |
|---|---|---|
| R6-T01 | periodic creation at seam | equivalent endpoints deduplicate into one canonical address |
| R6-T02 | periodic seam drag | same interaction-owned point crosses a closed SplineV2 seam in both directions, continues away, preserves ID/source/branch/component, publishes no duplicate, and reaches an identical final semantic address by direct and incremental paths; encoded canonical bits remain authority while the lifted numeric reconstructs exactly; an unresolved seam control leaves point/state unchanged |
| R6-T03 | closed SplineV2 seam | knot/seam ownership composes without duplicate candidates |
| R6-T04 | translated/rotated/reflected source | inverse acceleration and transformed forward verification agree; optional affine-certificate overflow does not invalidate the ordinary transformed locus |
| R6-T05 | transformed SplineV2 | specialization survives R5 composition and point belongs to transformed source |
| R6-T06 | negative dilation | semantic parameter/orientation remains source-defined despite ambient reflection |
| R6-T07 | zero dilation new query | collapsed image reports degenerate/multiple, never arbitrary unique address |
| R6-T08 | zero dilation existing point | address retained through collapse and same point recovers at nonzero factor |
| R6-T09 | transformation chain | nested supported similarities resolve through normal semantic composition |

## F. Persistence and construction lifecycle

| ID | Scenario | Required result |
|---|---|---|
| R6-L01 | native save/reopen | point ID, source, branch/component, address and recomputation reconstruct |
| R6-L02 | save while source invalid | dormant reopen restores the durable selector while current binding/coordinates remain undefined, then exact revalidation recovers the same point without coordinate repair |
| R6-L03 | undo/redo drag | exact semantic address state restores in both directions |
| R6-L04 | copy/remap | new point ID and exactly remapped source/address; no Cartesian reattachment |
| R6-L05 | rename | labels change with zero semantic/address effect |
| R6-L06 | compatible redefine | G9A transaction preserves only compatible durable identity; invalid address fails closed |
| R6-L07 | feature-off/Classic preservation | supported point/document preserves without enabling experimental interactive creation |
| R6-L08 | exact component lineage | persisted `componentLineageKey` selects the exact shared-endpoint component; absent, duplicate or incompatible lineage fails closed rather than taking the first component |

## G. Determinism and performance

| ID | Scenario | Required result |
|---|---|---|
| R6-E01 | solver enumeration | candidate status/set/address identical under discovery-order perturbation |
| R6-E02 | zoom/DPI/viewport | kernel result and counters are independent of view state |
| R6-E03 | render tessellation | render policy/vertices have zero authority and zero effect on candidates |
| R6-E04 | repeated request | deterministic result and bounded revision-cache behavior where implemented |
| R6-E05 | drag benchmark | evaluations/spans/subdivisions/refinements/fallback stay within frozen limits |
| R6-E06 | focused rerun | A/B canonical summaries are byte-identical under Git-blob/canonical-LF hashing |
| R6-E07 | polynomial composition chain | paired x/y coefficient access is coherent, captured depth is O(1), composed work is O(depth), and hard/query limits fail typed before unsafe recursion |

## H. Negative and regression boundaries

| ID | Scenario | Required result |
|---|---|---|
| R6-N01 | nonfinite request/tolerance | typed invalid input and zero construction mutation |
| R6-N02 | undefined source | invalid-source status and no stale candidate |
| R6-N03 | unsupported source/capability | typed unsupported without render/Path fallback |
| R6-N04 | duplicate semantic address | equivalent boundary candidates deduplicate; distinct preimages do not |
| R6-N05 | ambiguous branches | multiple result, never first/list/nearest choice |
| R6-N06 | R4/R5/G9S1 separation | intersection tokens, collapsed semantics and spline authority remain unchanged |
| R6-N07 | Classic Point compatibility | legacy paths and Point behavior are unchanged |
| R6-N08 | retained periodic risk | ordinary R6 round trip does not falsely close R4 quarantine round-trip risk |

## Historical and composed authority

The approved closeout reruns focused R6 twice; relevant semantic Point,
G5/G6/G6R/G7/G8/G9A/G9U0/R1/R2/R3/R4/R5/G9S1/G9X1, persistence and Classic
gates; Checkstyle; both Git diff checks; and full `tools/agent/verify.ps1` with
terminal `All GeoCeDG verification gates passed.`

The accepted R6 validation surface is the kernel/test-host API. Productive GUI
Point-tool acceptance remains explicitly deferred to G9U1 because R6 contains
no such frontend consumer.

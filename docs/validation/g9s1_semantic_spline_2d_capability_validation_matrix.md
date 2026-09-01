# G9S1 semantic Spline V2 validation matrix

- Status: **PASS — AUTHOR APPROVED**
- Date: 2026-08-31
- Authority:
[normative specification](../../geocedg/specs/curves/semantic-spline-2d.md)
- Evidence: not frozen; focused execution and deterministic rerun required

## A. Public construction and semantic model

| ID | Scenario | Required result |
|---|---|---|
| S1-C01 | supported `SplineV2` public forms, including list input and the minimum three-point wrapping form | one new semantic `GeoLocusV2` with new durable ID, normal inputs and equivalent default-degree semantics |
| S1-C02 | Classic `Spline` control | existing command/result/XML behavior unchanged |
| S1-C03 | feature off | existing V2 document preserves; new `SplineV2` creation unavailable under normal policy |
| S1-C04 | domain/span inspection | declared orientation, stable spans, knots and continuity match the mathematical definition |
| S1-C05 | knot ownership | one shared-knot root is owned canonically; no duplicate boundary solution |
| S1-C06 | repeated/coincident source data | typed zero-span/repeated-knot/invalid state; no stale geometry |
| S1-C07 | open/closed/periodic supported forms | truthful domain and seam policy; unsupported form fails closed |
| S1-C08 | self-intersection | distinct semantic preimages remain distinct |

## B. Evaluation, DAG and persistence

| ID | Scenario | Required result |
|---|---|---|
| S1-D01 | change one source/control input | one normal dependency recompute and coherent semantic revision |
| S1-D02 | undefined/nonfinite source then recovery | undefined without stale output; deterministic recovery |
| S1-D03 | copy/remap | new object ID; exact source dependencies remapped under existing rules |
| S1-D04 | undo/redo | semantic provider, ID graph and dependents restore |
| S1-D05 | native `.cedg` save/reopen | provider/domain/spans/ID/downstream results reconstruct deterministically |
| S1-D06 | compatibility `.ggb` and Classic | preserve supported V2 data; no experimental Classic creation |
| S1-D07 | path-independent updates | same final inputs give identical semantic snapshot and downstream bindings |
| S1-D08 | render/view changes | zero semantic/metric/intersection change |

## C. Rich length

| ID | Scenario | Required result |
|---|---|---|
| S1-L01 | analytic line/quadratic/cubic controls | total length agrees with independent analytic/reference evidence where available |
| S1-L02 | partial length across one knot | exact semantic endpoints; interval split once; controlled error |
| S1-L03 | partial length across several spans | oriented accumulation and error composition are deterministic |
| S1-L04 | cusp/zero derivative/repeated knot | truthful finite/undefined/error state; no chord-sum exactness claim |
| S1-L05 | R5 isometry | translated/rotated/reflected length unchanged |
| S1-L06 | R5 dilation | length and partial length scale by `|k|`; `k=0` follows `COLLAPSED_IMAGE` |
| S1-L07 | ordinary scalar surface | `Length(L,P,Q)` returns a guarded `GeoNumeric` child of the same rich between-position authority used by `LocusLength(L,P,Q)`; total `Length(L)` is unchanged |
| S1-L08 | exact endpoints and recovery | endpoint parameters and source geometry recompute the scalar; invalid/mismatched/unaddressed endpoints and invalid source state fail closed without stale values, then recover normally |
| S1-L09 | lifecycle and persistence | hidden rich parent, exact endpoint provenance and scalar child survive copy/remap, undo/redo and native `.cedg` save/reopen |
| S1-L10 | compatibility and separation | Classic curve `Length(curve,P,Q)` remains unchanged; metric-only endpoint updates do not recompute an independent intersection result |

## D. One-sided rich intersections

| ID | Scenario | Required result |
|---|---|---|
| S1-I01 | line transverse roots | correct finite roots, isolation, residual, selectors and exact tokens |
| S1-I02 | segment/ray filters | target-domain admissibility filters without changing root identity |
| S1-I03 | circle/conic | deterministic roots and truthful completeness/evidence |
| S1-I04 | tangent/even multiplicity | derivative-stationary candidate detected without sign-change dependence; nonisolated/tangent evidence remains nonmaterializable |
| S1-I05 | knot root | canonical ownership and one exact token |
| S1-I06 | several roots in one span/component | unique semantic cells independent of solver enumeration |
| S1-I07 | regular polynomial implicit target | spanwise polynomial composition, floating estimated evidence and independent residual check; no global-count claim |
| S1-I08 | bounded function target | existing general rich capability, not mislabeled as polynomial-certified; explicit unsupported/work-limit state |
| S1-I09 | overlap/coincident span | overlap evidence; no fabricated isolated point |
| S1-I10 | local vs global matrix | locally admissible root may materialize with global `NOT_ESTABLISHED`; ambiguous root never does |

## E. Pair intersections and identity

| ID | Scenario | Required result |
|---|---|---|
| S1-P01 | piecewise-polynomial locus × locus transverse | deterministic Bernstein/subdivision/Newton rich candidates; local isolation remains `NOT_ESTABLISHED` without interval-rounded rectangle+uniqueness proof |
| S1-P02 | swapped operands | equivalent canonical semantic parameter pairs, source-pair identity and diagnostic tokens without “first spline wins” identity |
| S1-P03 | spline × non-polynomial Locus V2 | existing common pair capability or explicit unsupported boundary; no one-sided rank injection |
| S1-P04 | pair tangency/overlap | conservative rich-only ambiguity/overlap state; no fabricated finite/materializable point |
| S1-P05 | knot and periodic seam ownership | equivalent span/seam representations deduplicate in both semantic parameters |
| S1-P06 | pair materialization boundary | no public continuation key, no active ledger allocation and `findPointAdmissibleSolution` empty for every floating pair candidate |
| S1-P07 | pair work budget | bounded subdivision/Newton exhaustion yields coherent `WORK_LIMIT_REACHED`, no partial set presented as complete |
| S1-P08 | future pair selector requirement | materializable pair roots remain deferred until a symmetric rectangle+uniqueness certificate and selector are separately approved |
| S1-P09 | uniform-scale covariance | the same transverse polynomial pair at scales `1` and `1e-8` yields the same rich-only classification, root count and semantic parameter-pair contract without screen or absolute-coordinate authority |

## F. Transform, lifecycle and performance

| ID | Scenario | Required result |
|---|---|---|
| S1-T01 | all R5 similarity families | output remains semantic Locus V2 and transformable again |
| S1-T02 | Point-on-Locus covariance | same semantic address maps geometrically under transform |
| S1-T03 | intersection covariance | geometric correspondence; new transformed query IDs/tokens |
| S1-T04 | dynamic transform inputs | normal DAG recompute through metrics/intersections/points |
| S1-E01 | broad-phase exclusion | polynomial spans-examined/rejected counters prove rejected spans do not refine |
| S1-E02 | root/work sweep | deterministic raw-root/evaluation/subdivision/refinement counters and explicit budget status |
| S1-E03 | materialized points | roots solved once; selector lookup is not one full solve per child |
| S1-E04 | deterministic rerun | canonical normalized evidence is byte-identical |

## G. Negative and degeneration cases

| ID | Scenario | Required result |
|---|---|---|
| S1-N01 | invalid degree/order/family inputs | atomic typed failure; no partial publication |
| S1-N02 | empty domain/all-invalid spans | defined empty/undefined contract; no render fallback |
| S1-N03 | near singular coefficients | explicit conditioning/numeric guarantee |
| S1-N04 | unresolved multiple root | nonmaterializable unless required isolation is established |
| S1-N05 | unsupported 3D/surface/NURBS form | fail closed without changing Classic behavior |
| S1-N06 | coordinate/list/render perturbation | zero identity effect |

## H. Historical and composed regression

Require the focused G9S1 suite twice, relevant Classic spline tests, full Locus
semantic/metric/intersection authority (G6–G8), G9U0/R1/R2/R3/R4/R5, G9A,
G9X1, G5, native Desktop I/O, Checkstyle, static G9S1 verifier,
`git diff --check`, `git diff --cached --check` and
`tools/agent/verify.ps1` with terminal:

`All GeoCeDG verification gates passed.`

No generated log is tracked. The first author smoke passed except for partial
length. The replacement correction then passed the focused/deterministic and
composed authorities, and the author accepted the reduced re-smoke:
`Length(S)=4`, `Length(S,P,Q)=2`, and the ordinary Locus V2 control uses the
actual public branch key `generator.main` with `Length(L,LP,LQ)=2`. The earlier
suggestion `scalar-locus/main` was an instruction error, not a product defect.

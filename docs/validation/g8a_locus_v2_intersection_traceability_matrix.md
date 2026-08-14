# G8A Locus V2 intersection traceability matrix

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Report | [`g8a_locus_v2_intersection_characterization_report.md`](g8a_locus_v2_intersection_characterization_report.md) |
| Raw evidence | [`g8a-intersection-characterization-evidence.json`](../../geocedg/validation/locus-v2/g8a/g8a-intersection-characterization-evidence.json) |
| Author closeout | [`g8a-author-closeout-evidence.json`](../../geocedg/validation/locus-v2/g8a/g8a-author-closeout-evidence.json) |
| Reference generator | [`generate_intersection_references.py`](../../geocedg/validation/locus-v2/g8a/generate_intersection_references.py) |
| Date | 2026-08-14 |

This matrix maps each G8A conclusion and final author decision to executable
or static evidence. `PASS` in the case rows means the test-private claim was
reproduced; the separate closeout evidence records `G8A = PASS — AUTHOR
APPROVED`. G8B is authorized but no productive G8B implementation exists.

## 1. Author refinements and principal architecture

| Requirement | Direct evidence | Result | Author decision |
|---|---|---|---|
| First-class Locus V2 intersection drives later CeDG construction | `G8AIntersectionKernelLifecycleCharacterizationTest.identifiedSolutionDrivesLaterCedgConstructionThroughNormalDag` | PASS; token-selected result updates downstream through Construction DAG | D2/D3 |
| Isolating interval is evidence, not identity | `G8AIntersectionIdentityCharacterizationTest.isolatingIntervalIsRevisionEvidenceAndNeverFundamentalIdentity` | PASS; interval changes and token persists via explicit semantic map | D10 |
| Completeness is independent | semantic/numerical tests for complete empty, incomplete finite and not-established finite | PASS; illegal incomplete empty rejected | D4 |
| Merge/split genealogy is a hypothesis | forward, reverse, symmetric, seam and branch-replacement identity tests | universal child inheritance rejected; explicit candidate lineage/ambiguity works | D11 |
| Query-local first; no G7 metric state | functional benchmark plus hard-zero counters | linear work, zero retained entries and metric reads | D12 |
| Rich value + rich Geo + required token-selected point | lifecycle tests for atomic publish, copy/set, downstream use and point refusal | author-approved G8B architecture; point remains derived | D2/D3/D16 |

## 2. Validation-case coverage

| Matrix IDs | Evidence | Characterization result |
|---|---|---|
| `K-COMP-EMPTY`, `K-COMP-FINITE` | numerical factorization tests; independent `complete-empty` and focal references | `EMPTY/FINITE + COMPLETE` only with exhaustive proof |
| `K-INCOMPLETE` | deliberately incomplete broad phase | one verified root retained with `INCOMPLETE` |
| `K-NOT-EST`, `K-TANGENCY` | evaluator-only tangency and unbounded-domain tests | verified candidate permitted; exhaustiveness/contact stays not established |
| `K-PROJECTION` | lifecycle bounded-point refusal | every point becomes undefined for incomplete/not-established result |
| `A-LINE-00..06` | numerical parabola, endpoint and equation-scaling tests | line proposed for core; tangent does not require sign change |
| `A-SEG-01..03` | semantic segment adapter membership probe | support residual and finite membership separated; no projection/clamp |
| `A-RAY-01..03` | semantic ray adapter probe | start/direction membership authoritative |
| `A-AFFINE-01`, `A-SCALE-01`, `N-NORM-01`, `N-ABSREL-01` | normalized target adapters and independent scale sweep | representation/equation scaling separated from tolerance quantities |
| `A-CIR-00..06` | circle matrix probe; focal secant/tangent/empty; seam/repeated-preimage tests | circle proposed for core |
| `A-CONIC-01..05` | ellipse, parabola, hyperbola, rotated and degenerate type probes | equation authority exists; uniform complete solver contract not closed; defer |
| `B-BRANCH-01`, `B-SELF-01` | multibranch topology and repeated-coordinate tests | constructive preimages preserved independently of coordinates |
| `B-COMP-01`, `B-DISC-01/02` | disconnected-component and nonfinite-gap tests | components isolated; gap never interpolated; invalid candidate unresolved |
| `B-CUSP-01`, `B-COLLAPSED-01`, `B-ISO-01`, `B-EMPTY-01` | topology characterization tests | cusp evidence explicit; collapsed overlap; isolated finite; empty complete with zero evaluations |
| `B-PER-01/02`, `I-SEAM-01` | seam canonicalization and lifted-continuation tests | one seam solution; continuation uses declared periodic map |
| `B-TOPO-01/02`, `I-BRANCH-01`, `I-COMBINED-01` | branch replacement, termination and lifecycle revision tests | unique lineage required; otherwise terminate/new epoch |
| `B-REPAR-01..03` | monotone, cubic-degenerate and reversal identity tests; independent references | token invariance only through explicit permitted semantic map |
| `C-TRANS-01`, `C-TAN-01`, `C-HIGH-01`, `N-EVEN-01` | simple, quadratic and fourth-order probes | contact and multiplicity independent; established only with supporting proof |
| `C-NEAR-01`, `N-ISO-01`, `N-DEDUP-01` | near-tangent and clustered-root tests/reference | distinct `2e-8` gap survives `4e-12` semantic dedup candidate |
| `C-OVER-01/02`, `C-INF-01` | overlap and collapsed-component probes | typed set evidence; no point sample |
| `C-UNSUP-01`, `N-FLAT-01`, `N-DERIV-01` | evaluator-only/derivative strategy comparisons | uncertainty remains explicit; no false transverse/empty |
| `I-CONT-01`, `I-MULTI-01` | ordinary continuation and known semantic prediction tests | unique semantic relation retains token |
| `I-MERGE-01`, `I-SPLIT-01`, `I-REVERSE-01`, `I-SYMMETRIC-01` | controlled forward/reverse traces | candidate genealogy recorded; universal child identity rejected |
| `I-SEAM-MERGE-01` | seam merge test | unique parents retained, new event token allocated |
| `I-AMBIG-01` | same constructive key with two admissible parents | explicit ambiguity; no coordinate/slot tie break |
| `I-STALE-01`, `I-FAIL-01`, `I-RECOVER-01` | rich-Geo failure/recovery tests | no stale/partial point; fresh current recovery |
| `N-CAP-01` | five-strategy comparisons | analytic/certified/derivative/evaluator/broad-phase claims closed separately |
| `N-PARAM-01` | reparameterization references and identity traces | parameter tolerance is evidence, not geometry/identity |
| `N-BUDGET-01` | work-exhaustion tests and benchmark | deterministic typed exhaustion; no partial complete set |
| `N-NAN-01` | nonfinite evaluator tests | typed unresolved/incomplete failure |
| `N-REF-01` | 80-digit generator and byte check | independent references reproducible and hash-pinned |
| `D-DAG-01`, `D-FIRSTCLASS-01` | actual test-private `AlgoElement`/`GeoElement` chain | normal dependencies and identified downstream use pass |
| `D-MULTI-01`, `D-REPEAT-01` | 1/3/10/100 query-local and removal tests | exact linear counts; deterministic equality; zero retained state |
| `D-NEST-01/02`, `S-NEST-01` | depth 1–3 nested control plus existing G6 cycle gate | calls equal consumers × depth; no whole-locus regeneration |
| `D-COPY-01`, `D-REMOVE-01`, `D-EXC-01`, `D-REV-01`, `D-STATE-01` | lifecycle suite | no copied/stale solver state; normal removal and atomic failure |
| `R-VIEW-01`, `R-DPI-01`, `R-RENDER-01`, `R-LEGACY-01` | structural dependency scan plus hard-zero counters | no semantic read path to those authorities |
| `R-CACHE-01` | query-local reference has no intersection cache | not applicable in proposed G8B; any future cache reopens this gate |
| `R-CLASSIC-01`, `R-LOCUS-01`, `R-PERSIST-01`, `R-3D-01`, `R-EXPORT-01` | zero productive changes plus existing composed/focused regressions | unchanged by G8A |
| `S-FOCAL-01` | functional pilot and independent discriminant reference | 2 → tangent → empty topology reproduced |
| `S-LSIM-01` | two-branch functional pilot and independent factor reference | 4 leaves → 2 tangent preimages → empty reproduced |

## 3. Level C capability boundary

| Matrix IDs | Source/API evidence | Result |
|---|---|---|
| `L-C-FUNC-01` | `GeoFunction.value`, `hasInterval`, explicit interval probe | legitimate target authority exists; complete semantic-domain/discontinuity contract absent; defer |
| `L-C-IMPL-01/02` | `GeoImplicit` equation, derivative and coefficient probe | legitimate polynomial authority exists; singular/component/general-form completeness absent; defer |
| `L-C-LL-01..04` | paired G6 evaluators/revisions source audit | requires two-parameter isolation, rank/contact, overlap and paired identity; not executed as a G8B candidate |

Level C deferral is a closed characterization result, not a claim that the
families are impossible.

## 4. Scientific traceability

| CeDG requirement | Versioned scientific source | Executable G8A evidence | Boundary |
|---|---|---|---|
| Locus projection is an intermediate geometric entity | `cedg.reference.lsim-preprint-2022`; `cedg.reference.book-2023` | identified root drives later normal-DAG construction | sources define need, not API/algorithm |
| Multileaf topology and qualitative changes | `cedg.reference.intersection-flattening-2023` | reduced two-branch LSIM 4→2→0 preimage trace | no 3D surface semantics |
| Tangent/secant focal transition | `cedg.reference.lsim-preprint-2022`; book focal discussion | reduced focal circle 2→1→0 analytic pilot | independent declared formula, not figure values |
| Nested historical cost | `cedg.reference.tools-and-oblique-cone-2025`; two hash-pinned legacy models | depth 1–3 linear evaluator counters and zero regeneration | dependency shape only; legacy samples not oracle |
| Downstream flattening/construction | `cedg.reference.intersection-flattening-2023` | rich identified solution consumed downstream | no flattening implementation in G8A |

## 5. Decision-to-evidence index

| Decision | Evidence keys |
|---|---|
| D1 phase closeout | 65 green probes, independent references, final verifier |
| D2 rich Geo | `richGeoPublishesAtomically...`, `failedRecompute...`, `copyAndSet...` |
| D3 required identified point | `identifiedSolutionDrives...`, `boundedPointProjectionRefuses...` |
| D4 completeness | all `K-COMP-*` probes and `IntersectionCompleteness` model |
| D5 tangent/multiplicity | `evenTangency...`, `fourthOrderRoot...`, cusp probe |
| D6 overlap | overlap semantic test and collapsed-component probe |
| D7 solver hierarchy | numerical strategy suite and per-strategy machine evidence |
| D8 tolerance policy | independent JSON `tolerance_measurements` and clustered/scale tests |
| D9 work budgets | functional benchmark, typed exhaustion and hard-zero counters |
| D10 durable identity | ordinary/reparameterized/reversed/seam traces |
| D11 merge/split | forward/reverse/symmetric/seam/branch-change tests |
| D12 query-local state | 1/3/10/100 and depth 1–3 metrics, zero retained state |
| D13 minimum families | real target-adapter audit and focal/LSIM pilots |
| D14 Level C deferral | function/implicit API probe and two-parameter source audit |
| D15 public boundaries | zero productive/public/Path/XML/3D/G9 changes |
| D16 `GeoClass` | test-private `DEFAULT` lifecycle plus exhaustive-type source audit |
| D17 spec/ADR gate | normative G8 spec, Accepted ADR 0008 and author closeout evidence |

## 6. Final state

```text
G8A = PASS — AUTHOR APPROVED
G8 SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0008 = ACCEPTED
G8B = AUTHORIZED / NOT STARTED
G8 PRODUCTIVE IMPLEMENTATION = NOT STARTED
G9 = NOT STARTED
```

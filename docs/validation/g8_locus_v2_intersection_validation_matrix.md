# G8 Locus V2 2D intersection validation matrix

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT EXECUTED / NOT NORMATIVE** |
| Characterization phase | Proposed G8A; requires separate author authorization |
| Productive phase | Proposed G8B; blocked on G8A author approval |
| Proposed contract | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) |
| Functional counters | [`g8_locus_v2_intersection_benchmark_plan.md`](g8_locus_v2_intersection_benchmark_plan.md) |
| Date | 2026-08-13 |

This matrix defines the evidence that a future G8A should collect and the
minimum candidate gates that an authorized G8B would have to satisfy. `A`
means characterization. `B-core` is proposed minimum productive coverage.
`B-policy` means the productive kernel must return a truthful rich
unsupported/unresolved/overlap result, even if it does not compute finite
points. Nothing in this file records an executed G8 test.

## 1. Assertions common to every case

Every executed case must inspect the full rich result and assert:

- source identities, captured revisions, branch/component bindings, semantic
  parameters, coordinates, target membership, residual/error evidence,
  method, guarantee, classification, currentness, topology context, and
  diagnostics are coherent;
- complete empty is distinct from incomplete search, unsupported solving,
  overlap, invalid input, and computation failure;
- every finite candidate is independently re-evaluated and verified against
  semantic Locus V2 and target authority;
- distinct constructive preimages are never deduplicated by coordinate;
- viewport, zoom, DPI, render vertices, legacy `myPointList`, and graphical
  proximity make no semantic difference;
- fixed input/revisions/policy produce deterministic ordered rich output and
  functional counters;
- work and retained state remain within the versioned policy; and
- an exception or revision change cannot expose stale or partial success.

Where an independent numeric reference is used, save formula, generator
script, precision, runtime/library versions, output, and hashes. A scientific
`.ggb` or historical sampled locus is a scenario/provenance source, not a
numeric oracle.

## 2. Level A — lines, segments, and rays

| ID | Phase | Semantic fixture / target | Required assertion |
|---|---|---|---|
| A-LINE-00 | A+B-core | bounded curve entirely on one side of line | complete empty; coverage evidence supports absence |
| A-LINE-01 | A+B-core | `F(t)=(t,t²)`, line `y=c`, `c>0` | two transverse roots with distinct semantic parameters and verified residuals |
| A-LINE-02 | A+B-core | same parabola, tangent line `y=0` | one even-multiplicity tangent root; no sign-change-only miss |
| A-LINE-03 | A+B-core | near-tangent offsets above/below | two roots versus empty classified without using a magic tangency threshold |
| A-LINE-04 | A+B-core | root at included component endpoint | one `INCLUDED_ENDPOINT` root, neither dropped nor double-counted |
| A-LINE-05 | A+B-core | root at excluded/open boundary | no ordinary finite root; explicit boundary/limit diagnostic |
| A-LINE-06 | A+B-core | line coefficient rescaling | identical geometry, identities/classifications and normalized residual decision |
| A-SEG-01 | A+B-core | support line has two roots; only one lies in segment | one verified member; other candidate rejected with target-membership evidence |
| A-SEG-02 | A+B-core | locus meets segment endpoint | included target-boundary classification and deterministic dedup |
| A-SEG-03 | A+B-core | support-line root just beyond endpoint | no projected/clamped intersection |
| A-RAY-01 | A+B-core | support line has roots on both ray directions | retain only ray member; explicit start/direction policy |
| A-RAY-02 | A+B-core | root at ray start | one included-boundary root |
| A-RAY-03 | A+B-core | root behind ray start | no silent projection to start point |
| A-AFFINE-01 | A+B-core | translate both source and target | parameters/topology/classification invariant; coordinates translate |
| A-SCALE-01 | A+B-core | scale geometry over characterized magnitudes | root structure invariant; normalized residual/tolerance behavior recorded |

## 3. Level A — circles and conics

| ID | Phase | Semantic fixture / target | Required assertion |
|---|---|---|---|
| A-CIR-00 | A+B-core | disjoint semantic curve and circle | complete empty only with adequate isolation coverage |
| A-CIR-01 | A+B-core | line-parameter locus through circle | two transverse roots and verified circle residual |
| A-CIR-02 | A+B-core | tangent line-parameter locus | one tangent/even root with established or explicitly unknown multiplicity |
| A-CIR-03 | A+B-core | near-tangent family under perturbation | deterministic two→one→zero topology trace |
| A-CIR-04 | A+B-core | locus endpoint on circle | endpoint/contact classifications remain independent |
| A-CIR-05 | A+B-core | periodic locus root on canonical seam | one root, canonical parameter plus seam evidence; no duplicate endpoints |
| A-CIR-06 | A+B-core | same point attained on two source branches | two constructive solutions despite equal coordinates |
| A-CONIC-01 | A | ellipse target with known analytic intersections | audit full target-specific capability, residual normalization and root count |
| A-CONIC-02 | A | parabola/hyperbola target | audit unbounded target semantics independently of locus component bounds |
| A-CONIC-03 | A | degenerate conic types | explicit supported subtype, overlap, finite roots, or unsupported state; no generic-smooth fiction |
| A-CONIC-04 | A | conic tangent and higher-contact examples | determine whether order/multiplicity can be established truthfully |
| A-CONIC-05 | A | rotated/scaled conic | representation-independent classification and normalized residual |

Full supported-conic promotion to B-core is an author decision based on these
characterization results; circle support does not imply every conic subtype.

## 4. Level B — semantic topology and preimages

| ID | Phase | Fixture/change | Required assertion |
|---|---|---|---|
| B-BRANCH-01 | A+B-core | multiple constructive branches, roots on each | branch-bound solutions; stable deterministic ordering independent of creation label |
| B-COMP-01 | A+B-core | one branch with disconnected valid components | each component isolated separately; no candidate/refinement crosses the gap |
| B-SELF-01 | A+B-core | self-intersection whose crossing lies on target | repeated coordinate retains distinct semantic preimages as required by branch/parameter |
| B-RETRACE-01 | A | retraced image segment | characterize finite preimage multiplicity versus overlap; never coordinate-collapse blindly |
| B-CUSP-01 | A+B-policy | cusp lies on target | explicit regularity/contact guarantee; unknown multiplicity stays unknown |
| B-COLLAPSED-01 | A+B-policy | nontrivial domain maps to one target point | classify coincident/infinite-preimage geometry; do not emit arbitrary repeated points |
| B-ISO-01 | A+B-policy | isolated valid-domain point on/off target | one boundary/isolated solution or complete empty with explicit evidence |
| B-EMPTY-01 | A+B-core | no valid components | successful complete empty without evaluator/root work |
| B-DISC-01 | A+B-core | roots on both sides of invalid interval | roots remain component-bound; no interpolation through discontinuity |
| B-DISC-02 | A+B-policy | target approached only at excluded discontinuity | boundary/limit or unresolved status, not a fabricated point |
| B-PER-01 | A+B-core | periodic branch with seam root | canonicalize once and preserve lifted continuation context |
| B-PER-02 | A | root crosses periodic seam dynamically | identity persists only through declared periodic semantics |
| B-TOPO-01 | A+B-core | component creation/loss after source edit | topology revision changes; obsolete solutions stale; fresh result contains no old geometry |
| B-TOPO-02 | A+B-policy | branch replacement with no unique G6 lineage | old token terminates; unsupported continuation is explicit |
| B-REPAR-01 | A+B-core | regular monotone reparameterization | same geometric set/count/classification; parameters map through the known reparameterization |
| B-REPAR-02 | A | monotone reparameterization with vanishing derivative | geometry invariant; derivative-based contact claims remain truthful |

## 5. Tangency, multiplicity, and overlap taxonomy

| ID | Phase | Case | Required assertion |
|---|---|---|---|
| C-TRANS-01 | A+B-core | simple root with established nonzero derivative | `TRANSVERSE` plus supporting evidence |
| C-TAN-01 | A+B-core | quadratic even root | detected without sign change and classified tangent when evidence supports it |
| C-HIGH-01 | A+B-policy | cubic/quartic higher contact | `HIGHER_ORDER` only if order is established; otherwise `CONTACT_UNKNOWN` |
| C-NEAR-01 | A+B-core | arbitrarily close pair around tangency | separate versus merged roots controlled by explicit isolation/evidence policy |
| C-OVER-01 | A+B-policy | locus image coincides with a target line over an interval | query-level `OVERLAP`/infinite set; no arbitrary finite sampling |
| C-OVER-02 | A+B-policy | partial coincident interval plus isolated roots | overlap component and finite solutions represented without claiming a pure finite set |
| C-INF-01 | A+B-policy | collapsed component on target | infinitely many semantic preimages stated explicitly |
| C-UNSUP-01 | A+B-policy | solver cannot establish absence or tangency | unresolved/unsupported; never complete empty |

## 6. Dynamic identity and topology traces

Each trace records every revision, root token, parent/child lineage, semantic
interval, topology epoch, classification and currentness transition.

| ID | Phase | Dynamic trace | Required assertion |
|---|---|---|---|
| I-CONT-01 | A+B-core | one transverse root moves smoothly inside one component | same token through unique semantic continuation; coordinate proximity not required |
| I-MULTI-01 | A+B-core | several roots move without topology change | each follows its semantic interval; no permutation by output order |
| I-MERGE-01 | A+B-core | two roots merge at tangency | explicit merge event; tangent root has both parents or approved equivalent; no arbitrary survivor |
| I-SPLIT-01 | A+B-core | tangent root splits into two | two new children linked to parent; no silent identity cloning |
| I-SEAM-01 | A+B-core | root crosses periodic seam | identity follows canonical/lifted parameter and declared seam |
| I-BOUND-01 | A+B-policy | root exits through component endpoint | termination/boundary event; old point not silently retained |
| I-GAP-01 | A+B-policy | root disappears into invalid interval and later returns | old token terminates unless an approved semantic lineage proves continuation |
| I-BRANCH-01 | A+B-policy | source topology replaces a branch | follow only unique G6 lineage; otherwise new solution/topology epoch |
| I-AMBIG-01 | A+B-policy | two current roots satisfy a continuation prediction | explicit ambiguous/unsupported association, no nearest-coordinate tie break |
| I-STALE-01 | A+B-core | edit during/after a successful computation | previous snapshot becomes stale before new publication |
| I-FAIL-01 | A+B-core | recomputation throws/exhausts work | coherent current failure/unresolved snapshot; no previous success or partial roots current |
| I-RECOVER-01 | A+B-core | invalid source becomes valid again | fresh computation and current tokens per approved continuation boundary |

## 7. Numeric, residual, and tolerance characterization

| ID | Phase | Experiment | Required evidence |
|---|---|---|---|
| N-CAP-01 | A | same case through every available capability | values/classifications/guarantees compared; stronger label only when justified |
| N-ISO-01 | A+B-core | narrow root pair | isolation coverage and semantic-parameter dedup remain distinct |
| N-EVEN-01 | A+B-core | even roots of orders 2 and 4 | no reliance on sign changes; bounded resolution outcome |
| N-FLAT-01 | A | very flat residual near a non-root minimum | avoid false tangent; unresolved if evidence insufficient |
| N-NORM-01 | A+B-core | multiply target equation by nonzero constants | normalized residual decision invariant |
| N-ABSREL-01 | A | several geometry scales/translations | measure independent absolute/relative residual policy; approve no values yet |
| N-PARAM-01 | A | regular reparameterizations with different parameter scales | characterize root-isolation and continuation policies separately from world residual |
| N-DEDUP-01 | A | close roots versus duplicate candidates for one root | determine semantic dedup policy without merging real roots |
| N-DERIV-01 | A | analytic, estimated, absent and singular derivatives | derivative guarantee propagates to classification truthfully |
| N-BUDGET-01 | A+B-core | each evaluation/subdivision/iteration/depth/candidate limit | deterministic termination and exact counter/diagnostic |
| N-NAN-01 | A+B-core | nonfinite evaluator/target/residual data | typed failure; no NaN/magic-number state escapes |
| N-REF-01 | A | independent high-precision reference suite | reproducible script, formula, precision/runtime/library and hashes |

G8A selects values only after these experiments. G6 domain tolerances, G7
metric tolerances, kernel standard precision, and render/pixel tolerances are
comparison inputs, not inherited G8 defaults.

## 8. Level C characterization — not proposed G8B minimum

| ID | Phase | Family | Required characterization |
|---|---|---|---|
| L-C-FUNC-01 | A-future | `GeoFunction` target | semantic target domain, discontinuities, view independence, tangency completeness |
| L-C-IMPL-01 | A-future | polynomial `GeoImplicit` | equation scaling, derivative authority, singular points, component coverage |
| L-C-IMPL-02 | A-future | non-polynomial/unsupported implicit form | explicit capability boundary; no invented conversion |
| L-C-LL-01 | A-future | Locus V2–Locus V2 transverse | two-parameter isolation, paired revisions and two-sided residual |
| L-C-LL-02 | A-future | tangent locus–locus | rank/contact evidence and unresolved policy |
| L-C-LL-03 | A-future | locus–locus overlap | component correspondence and infinite-set result |
| L-C-LL-04 | A-future | two-sided dynamic topology change | paired root identity/lineage and bounded association |

These cases may justify a later subphase, but their existence must not widen
the first productive candidate by default.

## 9. Lifecycle, DAG, and repeated consumption

| ID | Phase | Case | Required assertion |
|---|---|---|---|
| D-DAG-01 | A+B-core | either input changes | one normal dependency recomputation; no listener/GUI hidden edge |
| D-MULTI-01 | A+B-core | 1/3/10/100 compatible consumers | semantic equality, deterministic work accounting, bounded ownership |
| D-REPEAT-01 | A+B-core | repeated identical query | identical rich result; reuse counters truthful |
| D-NEST-01 | A+B-core | downstream evaluator consumes verified intersection result | no whole-locus regeneration per refinement/downstream point |
| D-NEST-02 | A | selected nested scientific construction | bounded session depth/work; cycle diagnostics preserved |
| D-COPY-01 | A+B-core | copy/set rich result | defensive immutable snapshot; no mutable solver-state alias |
| D-REMOVE-01 | A+B-core | remove algorithm/result | outputs and any owner leases/state released deterministically |
| D-EXC-01 | A+B-core | injected evaluator/solver exception | atomic failure, private state cleanup, recovery succeeds |
| D-REV-01 | A+B-core | revision changes during captured query | mixed-revision publication impossible |
| D-STATE-01 | A+B-core | many revisions/queries | no obsolete revision/root/index history beyond approved bound |

## 10. Representation independence and non-regression

| ID | Phase | Perturbation | Required assertion |
|---|---|---|---|
| R-VIEW-01 | A+B-core | change pan/zoom/view bounds | byte/semantic-equivalent rich output and counters where ordering is fixed |
| R-DPI-01 | A+B-core | change DPI/pixel ratio | no result effect |
| R-RENDER-01 | A+B-core | render cache absent/present/different tessellation | identical semantics; forbidden-access counters zero |
| R-LEGACY-01 | A+B-core | legacy sample count/tessellation differs | no G8 result effect |
| R-CACHE-01 | A+B-core if cache exists | cache/index enabled versus disabled | semantic equality; only permitted work counters differ |
| R-CLASSIC-01 | A+B-core | existing Classic line/conic/function intersections | unchanged results, labels, serialization and dispatch |
| R-LOCUS-01 | A+B-core | legacy `GeoLocus` constructions | unchanged; no silent V2 migration or new overload |
| R-PERSIST-01 | A+B-core | save/load existing `.ggb` without G8 type | byte/semantic non-regression; no new XML element |
| R-3D-01 | A+B-core | existing 3D intersection dispatch tests | unchanged; no V2 3D route |
| R-EXPORT-01 | A+B-core | G5 export baselines | unchanged |

## 11. Scientific pilots

| ID | Phase | Pilot | Role and expected assertion |
|---|---|---|---|
| S-FOCAL-01 | A | focal illumination sphere/cone projection: semantic locus versus separatrix circle | repeated projected coordinate/preimage and tangent/secant transition requirement; independent 2D reference required |
| S-LSIM-01 | A | reduced cone–cylinder LSIM section against a basic target | multiple branches/leaves and bite→penetration topology; historical sampled output is not oracle |
| S-NEST-01 | A | selected nested-locus construction | evaluator/session work and failure characterization; no whole-locus regeneration |

Scientific traceability and exact source pages are recorded in
[`g8_locus_v2_intersection_scientific_traceability.md`](g8_locus_v2_intersection_scientific_traceability.md).

## 12. Proposed G8B exit gate

After G8A author approval and a separate implementation authorization, G8B
could pass only if:

1. all selected B-core and B-policy cases execute through productive code;
2. each supported family has a closed capability/degeneration contract;
3. empty, unresolved, overlap and currentness states are distinguishable;
4. tangent/even-root and merge/split traces pass;
5. all hard functional budgets and forbidden-access counters pass;
6. cache-off/reference equality passes if any reusable state exists;
7. Classic/legacy/persistence/3D/export non-regression passes; and
8. the canonical repository verifier passes without weakening existing gates.

This is a proposed gate only. G8A, G8B, and G8 remain `NOT STARTED`.


# PRE-G9B-R2-E0/E1 semantic point admissibility and exact-token materialization design

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R2-E0/E1 (one combined sequential task)
CHANGE_ROUTE              = ORDINARY
STAGE                     = 1 — DESIGN AND NORMATIVE CONTRACT, FROZEN
VERIFICATION_CLASS        = INTEGRATED_PHASE (frozen here, section 12)
selfApproved              = false
```

This is the Stage-1 design record required by the author's combined
`PRE-G9B-R2-E0/E1` authorization. It records facts established from the live
repository at the implementation base and the design frozen from them. It carries
no author-approval field; the separate author-decision record is the sole
authority for approval.

```text
IMPLEMENTATION_BASE_COMMIT = a222b82aad821e3e0c0aa93145a57b7050f024c8
IMPLEMENTATION_BASE_TREE   = 0768848e89961f0f372f514530f9e9e0b81cc872
```

The prompts
[`pre-g9b-r2-e0-…design.prompt.md`](../../.github/prompts/tasks/pre-g9b-r2-e0-locus-v2-pair-materialization-design.prompt.md)
and
[`pre-g9b-r2-e1-…implementation.prompt.md`](../../.github/prompts/tasks/pre-g9b-r2-e1-locus-v2-pair-materialization-implementation.prompt.md)
are planning input. The author's combined instruction supersedes their
design-only / intermediate-approval boundary for this execution only.

Normative outputs of this design:

| Artifact | Role |
|---|---|
| [ADR 0028](../adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md) | the architectural decision; amends ADR 0021 Decision 1 |
| [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) | new normative contract for the certified construction interval model |
| [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §24 | endpoint admissibility amendment, v1.3 → v1.4 |
| [`spline-v2-pair-materialization.md`](../../geocedg/specs/curves/spline-v2-pair-materialization.md) §10 | pair materialization scope amendment |
| [validation matrix](../validation/pre_g9b_r2_e0_e1_validation_matrix.md) | focal and adversarial obligations |

## 1. Three distinct questions

The design keeps three questions apart. None implies another.

| Level | Question | Authority |
|---|---|---|
| **Existence / materialization** | may this root be published as an ordinary point? | rich intersection result, exact token, ledger, ADR 0021 as amended by ADR 0028 |
| **Semantic membership** | does this point lie on curve `S` at one exact semantic address, by construction? | explicit DAG provenance of the point, never its coordinates |
| **Metric endpoint admissibility** | may this point address a position of `S` for `Length(S,P,Q)` / `LocusLength(S,P,Q)`? | membership, or the approved A1 structural constructor transport |

A materialized point need not lie on a given curve. A point on a curve is not
automatically an endpoint for every curve. The single admitted non-membership
case is the approved A1 constructor transport through R5 similarity lineage
(§21), which addresses a structural knot occurrence rather than the point's own
position; it is not extended to any other family.

## 2. Point-producing inventory (live repository)

Every route that can put a `GeoPoint` in front of a between-position metric was
inventoried from the command processors (`CmdPoint`, `CmdIntersect`,
`CmdLength`, `CmdLocusLength`, `CmdTranslate`, `CmdRotate`, `CmdMirror`,
`CmdDilate`, `CmdLocusV2`, `CmdSplineV2`), `LocusV2PublicOperations`, the
desktop creation paths (`GeoCeDGPointInteraction`,
`GeoCeDGIntersectionSession`, `GeoCeDGLocusV2Dialogs`) and the GeoGebra
algorithm families they compose with.

| # | Point family | Producing route | Carrier of provenance |
|---|---|---|---|
| P1 | explicit semantic point | `Point(S,t)`, `Point(S,"branch",t)`, R6 interaction points | `AlgoSemanticLocusPoint2D` (source by reference, branch, parameter) |
| P2 | SplineV2 constructor / interpolation point | any `GeoPoint` in the ordered constructor list of `SplineV2(list,deg)` | direct `AlgoDependentList` slot (A1, §21) |
| P3 | pair-intersection point | `X=Intersect(R,"token")`, `R=Intersect(S,T)`, both Locus V2 | `AlgoLocusIntersectionPointV2` → `AlgoLocusLocusIntersectionV2` → per-side `LocusPairSourceRevisionEvidence2D` |
| P4 | single-source intersection point | `X=Intersect(R,"token")`, `R=Intersect(S,target)` with a line, segment, ray, circle, ellipse, parabola, hyperbola, bounded function graph or regular polynomial implicit curve | `AlgoLocusIntersectionPointV2` → `AlgoLocusIntersectionV2.getSource()` → `IntersectionRootRevisionEvidence2D` (revision, branch, component, canonical parameter) |
| P5 | generator point | the dependent point `E` of `LocusV2(E,C)` / `LocusV2(E,t,dom)` | `AlgoDependentPointLocusV2` descriptor (`dependentPointId`, driver state) and the live driver parameter |
| P6 | similarity image of a point | `Translate(P,v)`, `Rotate(P,α)`, `Rotate(P,α,O)`, `Mirror(P,Q)`, `Mirror(P,g)`, `Dilate(P,k)`, `Dilate(P,k,O)` | `AlgoTranslate`, `AlgoRotate`, `AlgoRotatePoint`, `AlgoMirror`, `AlgoDilate` with explicit parameter objects |
| P7 | dependent copy | `Q=P` | `AlgoDependentPoint` whose definition unwraps to exactly the point `P` |
| P8 | free copy | `CopyFreeObject(P)` | none — independent point |
| P9 | free or Cartesian-coincident point | `A=(x,y)`, dragged points | none |
| P10 | ordinary derived points | `Midpoint`, `Center`, `Vertex`, ordinary `Intersect` of non-semantic objects, `ClosestPoint`, list elements, expressions `C+(1,0)` | GeoGebra algorithm without Locus V2 provenance |
| P11 | macro output | user tools | `AlgoMacro`; its internal construction is a separate copy |
| P12 | classic point on path | `Point(S)` | cannot exist: `GeoLocusV2` is not a `Path` (`CmdPoint` rejects it) |

Internal Locus V2 producers (`AlgoAnalyticLocusV2`, `AlgoDynamicBranchLocusV2`,
`AlgoNestedLocusV2`, `AlgoSegmentPathLocusV2`) are reachable only through
`LocusV2Factory` from internal fixtures; they produce loci, not points, and have
no public construction route.

Witness facts established on the base (rebuilt `SeveralDefects` construction,
kernel probe, not a product test):

```text
a  = LocusV2(E,C)     ReconstructibleLocusEvaluator2D, provider circle-point/v1,
                      periodic, declared [-pi,pi), one COMPLETE (compatibility)
                      component [-pi,pi)
d  = SplineV2(l1,3)   one component [0,1]
e  = Intersect(d,a)   SUCCESS, NOT_ESTABLISHED, 2 floating roots, none admissible:
                      (u,v) = (0.19547, 2.90228) and (0.49720, 0.53063)
g  = Intersect(a,c)   4 admissible single-source roots, u = +-1.48687, +-1.82501
LocusLength(a)        SUCCESS 15.02490586...
LocusLength(a,I,J)    INVALID_QUERY — NO_ADDRESS (I, J from g)
LocusLength(a,E,I)    INVALID_QUERY — NO_ADDRESS
Q=A                   AlgoDependentPoint, definition unwraps to GeoPoint A
Rotate(E,alpha,A)     AlgoRotatePoint, 3 inputs; Rotate(a,alpha,A) shares alpha
```

## 3. Metric-endpoint admissibility matrix

| Family | Classification | Membership | Normative clause |
|---|---|---|---|
| P1 `SEMANTIC_POINT` | already admissible | yes | G9U0/G7, retained; §24.3 adds family retention |
| P2 `CONSTRUCTOR_OCCURRENCE` | already admissible | yes on its root spline; **transport** on R5 images | §21, unchanged |
| P3 `PAIR_INTERSECTION_OCCURRENCE` | already admissible; reach widened because Objective B makes more pair roots materializable | yes | §23, unchanged resolver |
| P4 `SINGLE_SOURCE_INTERSECTION_OCCURRENCE` | admissible from existing provenance (bounded resolver extension) | yes | §24.4 |
| P5 `GENERATOR_OCCURRENCE` | admissible after bounded provenance extension | yes | §24.5 |
| P6 `SIMILARITY_IMAGE_OCCURRENCE` | admissible after bounded provenance extension, only with identical transform parameter objects | yes iff the pre-image is a membership endpoint | §24.6 |
| P7 `DEPENDENT_COPY_OCCURRENCE` | admissible after bounded provenance extension | yes iff the copied point is a membership endpoint | §24.7 |
| P3 with both sides on `S` (`S × S`) | semantically ambiguous — `MULTIPLE` | two preimages | §23.2, unchanged |
| membership points on an R5 image of their curve (P1, P3, P4, P5, P7 transported) | not semantically addressable on the image — `NO_ADDRESS` | no | §23.8 rationale, §24.8 |
| P5 on an R5 image of its locus | **not admitted** — two defensible conventions; see §10 | no | §24.8 |
| P6 with distinct (for example literal) parameter objects | not established — `NO_ADDRESS` | not proved | §24.6 |
| P8, P9, P10, P11 | not semantically addressable — `NO_ADDRESS` | no | §24.8 |
| P12 | cannot exist | — | — |

Families are consulted in the order of §24.2: the direct families first
(semantic point, pair and single-source intersections, generator, constructor),
then the derived families (similarity image, dependent copy). Direct families
are structurally disjoint for a fixed addressed source by DAG acyclicity. Every
non-`NO_ADDRESS` result is final. See §14 for the Stage-2 correction of this
ordering.

## 4. Exact provenance per admitted family

Each admitted family recovers the seven required quantities from explicit DAG
provenance only:

| Required quantity | P3 pair | P4 single-source | P5 generator | P6 image / P7 copy |
|---|---|---|---|---|
| addressed source durable identity | side locus identity = `S` identity | `AlgoLocusIntersectionV2.getSource()` identity = `S` identity | `S` parent `AlgoDependentPointLocusV2` | outer `S'` / `S`, then inner recursion |
| current semantic revision | side revision = `S` revision | evidence `locusSemanticRevision` = `S` revision | current definition built from the live descriptor | inherited |
| branch / component | side branch; one containing valid component | evidence branch; one containing valid component, equal to the evidence component interval | `generator.main`; one containing valid component | inherited, source identity substituted (R5 preserves provider, branch, component, parameter) |
| semantic parameter / occurrence | side semantic parameter | evidence canonical parameter | live driver path parameter or true-coordinate value | inherited |
| point-to-source DAG provenance | point → token consumer → pair rich result | point → token consumer → single-source rich result | point is the descriptor's dependent point (durable identity) | point → transform/copy algorithm → pre-image point |
| lifecycle / currentness | current admissible token (ledger) | current admissible token (ledger) | live descriptor present, driver defined | inherited plus outer lineage identity checks |
| ambiguity status | `MULTIPLE` when both sides are `S` | single side, never ambiguous by coordinates | unique | inherited |

## 5. Intersection provider inventory and semantic-pair capability matrix

Public Locus V2 producers: `SplineV2`; point-driven `LocusV2(E,C)` with the
`CIRCLE_POINT`, `SEGMENT_POINT`, `CIRCULAR_ARC_POINT` and `LOCUS_BRANCH_POINT`
generator families; scalar `LocusV2(E,t,dom)` (`SCALAR_STATE`); and R5
similarity images of any of them. Pair intersections run through
`LocusPairIntersectionSolver2D`: discovery by `PiecewisePolynomialPairIntersectionCapability2D`
(both sides piecewise polynomial) or `EvaluatorPairIntersectionCapability2D`
(fallback), then `PublicSplinePairRootIdentityResolver2D` publication. Before
this phase only sources captured by `SplineIntervalModel2D` could reach
publication; every other pair returned its discovered rich-only result
**without emitting any class evidence**.

The admission unit is a per-side **certified interval curve model** capability
(ADR 0028). A pair is materializable exactly when both sides expose one:

| Side family | Certified model | Source of authority |
|---|---|---|
| SplineV2 and its supported similarity compositions | spline model (existing) | ADR 0022 structural spline |
| point-driven `LocusV2` whose generator slice lies in the certified construction class v1 | construction model (new) | [construction-model spec](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) |
| scalar `LocusV2` with the existing direct scalar affine certificate | affine construction program (new) | existing `CertifiedAffineLocus2D` evidence |
| similarity image of a modeled family | composition (existing similarity maps, shared) | captured `LocusSimilarityTransform2D` |
| `LOCUS_BRANCH_POINT` generator | none — residual | support is itself a Locus V2 reconstructed inside the isolated evaluator construction; no recursive capture exists |
| scalar `LocusV2` with any other expression | none — residual | no interval semantics for GeoGebra expression trees |
| point-driven slice with any operation outside the class | none — residual | the operation has no certified interval semantics (see §9) |
| collapsed similarity image | none — existing | no isolated roots |

| Pair family | Rich intersection | Local verification | Structural identity | Exact token | Point materialization |
|---|---|---|---|---|---|
| `SplineV2 × SplineV2` | current | current | current | current | current (unchanged) |
| `SplineV2 × modeled LocusV2` | current | certified | `pair-singleton-transverse-germ/v1` | admissible when the class is `UNIQUE` | **new** |
| `modeled LocusV2 × SplineV2` | canonical equivalent | certified | same selector after canonical normalization | as above | **new** |
| `modeled LocusV2 × modeled LocusV2` | current | certified | same selector | as above | **new** |
| similarity images of modeled families | current | certified | same selector | as above | **new** |
| any side without a model | current | fail closed | none | none | none — rich-only, now diagnosed |
| `S × S` | discovery overlap failure | none | outside the selector scheme | none | none |

The admissibility levels, per family, are therefore:

```text
rich supported -> discovered -> locally verified -> locally isolated
  -> identity unambiguous (UNIQUE class) -> exact token -> point
```

A side without a model stops at "discovered". A modeled pair stops at the level
its class certificate reaches: `UNRESOLVED` (unknown region, tangency, budget,
seam band) stops before "locally isolated"; `MULTIPLE` stops before "identity
unambiguous"; `UNIQUE` reaches "point".

## 6. Certified construction model — design summary

The normative detail lives in the
[construction-model spec](../../geocedg/specs/locus/locus-v2-certified-construction-model.md).

- The reconstructible evaluator replays an isolated copy of the construction in
  binary64. It has no enclosure semantics, so it cannot certify roots by itself.
  The minimum additional capability is an **outward-rounded interval replay of
  the same captured slice**: an immutable `CertifiedConstructionProgram2D`
  captured by the evaluator from its own isolated objects, interpreted with
  outward intervals and forward-mode first derivatives.
- **Authority** is the exact-real evaluation of the evaluator's own GeoGebra
  formula sequence on its captured constants, including each algorithm's
  branch predicates evaluated on exact values. A parameter box on which any
  predicate is not uniformly decided is unresolved. The floating evaluator
  approximates this function; at every certified root it must additionally be
  valid, agree within the residual tolerance and be transverse before
  publication. This mirrors the approved "captured represented spline
  authority, not exact interpolation".
- **Class v1**: drivers `CIRCLE_POINT`, `CIRCULAR_ARC_POINT`, `SEGMENT_POINT`,
  and `SCALAR_STATE` with the direct affine certificate; operations
  `AlgoJoinPoints`, `AlgoLinePointLine`, `AlgoOrthoLinePointLine`,
  `AlgoIntersectLines` (full-line operands), `AlgoMidpoint`, `AlgoTranslate`,
  `AlgoRotate`, `AlgoRotatePoint`, `AlgoMirror` (point or line mirror) and
  `AlgoDilate`, with every driver-independent input captured as a constant.
- **Periodic seam**: the circle-point canonical domain `[-Math.PI, Math.PI)` is
  not exactly closed under real `cos`/`sin` (the gap is about `2.45e-16 r`), so
  the model reports period `0` and treats the half-open component as a finite
  interval. Roots in the seam band are unresolved, which is the truthful state.
- The pair certifier, selector, contract identifier, ledger format, lifecycle and
  materialization gate are reused unchanged; the certifier consumes a
  model interface implemented by both the existing spline model and the new
  construction model.

## 7. Lifecycle, persistence, copy, redefine and reopen

| Situation | Pair materialization (Objective B) | Endpoint families (Objective A) |
|---|---|---|
| ordinary recompute | same selector re-certified from the current snapshot; zero new points | family and key re-derived; same key |
| temporary invalidity, lost proof, budget exhaustion, seam band | claim retained, point dormant and undefined | endpoint undefined → metric `INVALID_QUERY` before provenance work |
| return of the same unique class | same token, same point reactivated | same key, metric defined again |
| `1 -> 0 -> 1`, `1 -> many -> 1` | dormant / quarantined, then reactivated by the selector | follows the point |
| save / reopen (native `.cedg`), undo / redo | ledger v5 and the command DAG reconstruct; certificates recomputed on load; saved state is never proof | keys never serialized; reconstructed from the DAG and durable identities |
| copy / remap | exact two-source closure-copy provenance and the single authorized token rebase; with a generator-driven side the copied point stays dormant while the copy's own roots remain explicitly materializable (§14, pair spec §10) | new identities, new internally consistent keys |
| compatible redefine | follows G9A / A3; the selector is resolved afresh | family and key re-derived; a change of family or key is retargeting and fails closed; a cross-family change cannot reach a retained metric today (§14, D-C3) |
| incompatible redefine / replacement | structural barrier; no label or coordinate matching | `NO_ADDRESS` or `UNRESOLVED_INVALID`; never repaired |
| old documents | no ledger migration; old rich-only generic results gain admissible tokens only by current certification; no point is created without an explicit action | unchanged |

No ledger or document format changes. The ledger binding key remains
`(contract, selector)`; the address proof is refreshed per revision.

## 8. Ambiguity rules

- Pair: at most one certified singleton per germ sign per component product;
  two certified same-sign roots → `MULTIPLE` → quarantine; any unresolved
  region that may hold the sign → `UNRESOLVED`; a germ-0 region (tangency,
  multiplicity, overlap, `S × S` diagonal) blocks both signs.
- Endpoints: the side is selected by identity; `S × S` → `MULTIPLE`; a token on
  two admissible solutions is excluded by the rich result; single-source roots
  with coincident coordinates keep their distinct parameters and are never
  merged; inherited ambiguity propagates through images and copies.

## 9. Unsupported residual families

| Residual | Reason (missing capability) | Truthful state |
|---|---|---|
| `LOCUS_BRANCH_POINT` generators | recursive certified capture of a Locus V2 support reconstructed inside the isolated evaluator construction | rich-only, diagnosed |
| scalar loci with non-affine expressions | interval semantics for GeoGebra expression trees (open operation set with evaluation-time type dispatch) | rich-only, diagnosed |
| slices through GeoGebra line–conic / conic–conic intersections | GeoGebra orders their two outputs by continuity, i.e. by proximity; no semantic root identity | rich-only, diagnosed |
| slices through selected Locus V2 roots (`AlgoLocusIntersectionPointV2`, the `m` family) | interval enclosure of an implicit root as a function of the driver (parametric interval Newton) | rich-only, diagnosed |
| slices with expressions, lists, macros, functions, segment-bounded meets or any other operation | no certified interval semantics established | rich-only, diagnosed |
| internal factory loci | no public construction route | not reachable |
| `S × S` and overlapping distinct sources | outside the selector scheme; continuum of roots | no admissible root |
| tangency / even multiplicity | germ `0` | no admissible root in that component product |
| roots in the circle-point seam band | domain not exactly closed at the seam | unresolved for that sign |

## 10. Non-blocking open question for the author

Whether the generator point `E` of `S` should address an R5 image `T(S)` has two
defensible answers: A1-style structural transport (the generator, like a
constructor, is part of the constructive definition of `S`), or R2-style
membership semantics (`E` is not on `T(S)`). The repository authority does not
decide it. This design **admits neither**: `E` is `NO_ADDRESS` on `T(S)`, while
`T(E)` built with the same transform parameter objects is admitted on `T(S)` by
membership. Fail-closed non-admission cannot produce a wrong length, so the
question does not block implementation; it is recorded for an explicit author
decision.

## 11. Validation obligations

The complete focal and adversarial list — every mandatory positive and negative
of the author instruction and of the `E0` prompt — is frozen in the
[validation matrix](../validation/pre_g9b_r2_e0_e1_validation_matrix.md) before
implementation.

## 12. Verification class, frozen

The footprint is the shared kernel (intersection certification, construction
model capture, metric endpoint resolution), focal shared and desktop tests, the
user guides, and the JUnit-inventory / registry catalog pin that new tests
require. It does not change the verifier, the registry schema, build, Gradle,
packaging, bootstrap or serialization. This is the same footprint class as
`PRE-G9B-R2`.

```text
VERIFICATION_CLASS = INTEGRATED_PHASE
frozenAtPhaseStart = true
PLANNED_ACCEPTANCE = PHASE -Phase G9S1-R1   (pair materialization contract)
                     INTEGRATION             (shared kernel <-> desktop persistence,
                                              intersection session and metric review)
COHORT_EQUIVALENCE = PHASE -Phase G9U0-R6 resolves to the same required checks as
                     PHASE -Phase G9S1-R1 (OBS-R2-PHASE-SELECTION-COHORT); it is
                     recorded as equivalent, not rerun, if -PlanOnly confirms the
                     identical cohort
FINAL              = not required by this footprint; a VERIFICATION_ESCALATION_REQUEST
                     is issued instead of running it if that assessment changes
RETAINED DEBT      = BASELINE-G9U1-BRANDING-EVIDENCE-PIN (outside the delta)
```

## 13. Stage transition

The design is uniquely determined by current repository authority and the
invariants above: the pair contract, selector, ledger and lifecycle are reused
unchanged; the only new proof capability follows the approved "captured
represented" doctrine; every widened endpoint family is membership-based and
reuses the approved §21/§23 resolver shape; and the one genuinely two-sided
question (§10) is resolved fail-closed without admitting either convention. No
existing approved guarantee is weakened. Stage 2 proceeds on this frozen
contract.

## 14. Stage-2 design corrections and findings (recorded, not silent)

Stage 2 kept the frozen contract. Where implementation showed a frozen statement
to be false or unreachable, the correction below was made openly, applied to
the normative text, and pinned by tests. None weakens an approved guarantee.
The author may overturn any of them.

### D-C1 — dispatch order

While translating §24.2 into the endpoint resolver, and before that resolver was
written, the frozen claim that *all seven* families are structurally disjoint
for a fixed addressed source was found to be false in one edge case: a spline
whose constructor list contains both a point `P` and its image `T(P)` (or a
dependent copy `Q=P`). There a derived family (similarity image, dependent copy)
and the approved §21 constructor occurrence assign two different addresses to
the same endpoint, and the originally frozen order (derived families before the
constructor family) would have silently changed an approved A1 result.

The correction is uniquely determined by the invariants: the derived result
cannot win (it changes an approved §21 address) and `MULTIPLE` cannot be
reported (it turns an approved `UNIQUE` into `INVALID_QUERY`). The only option
that preserves every approved guarantee is to consult all direct families —
including the §21 constructor occurrence — before any derived family. The
frozen §24.2 table, its disjointness paragraph and §24.6 were amended
accordingly before implementation of the resolver; no approved result changes,
and every newly admitted endpoint keeps its meaning.

### D-C2 — identity of intermediate derivation steps

Frozen §24.3 said that a missing durable identity yields `UNRESOLVED_INVALID`,
and frozen §24.6/§24.7 admitted nested derivations (an image of an image, a copy
of an image). Implementation showed that the two statements conflict: a durable
point identity begins only when a point first participates, and an intermediate
step such as `IR` in `IRR=Rotate(IR,α,A)` never participates. Nested derived
endpoints would therefore always have been `UNRESOLVED_INVALID`.

Of the three options, requiring the identity makes the frozen composition
unreachable, and registering an identity during resolution would make a
read-only resolution mutate the identity registry. The chosen option keys an
intermediate step structurally (`point=derived`). It is unique because the
endpoint's durable identity and the explicit input chain of its parent
algorithms determine every intermediate step. The endpoint, every addressed
source and the direct-family origin still require durable identities. Metrics
§24.3 records the rule; `END#ineligibleTransformsFailClosedAndNestedImagesCompose`
pins it.

### D-C3 — family change is not reachable in one metric object

Frozen matrix row A18 planned a direct family-change execution that fails
closed. The current host lifecycle cannot produce one. A compatible redefinition
of a participating endpoint into another family is rejected atomically
(`REDEFINE_REJECTED`) and the rolled-back metric resolves the same family again.
A replacement redefinition has replacement identity semantics for its dependent
closure, so no metric object survives to be retargeted. As for §21, the case is
`NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`; the retained family and key guard
remains the fail-closed authority. Metrics §24.3 records this, and
`END#familyChangeByRedefinitionIsAFreshResolutionNeverARetarget` pins both
lifecycle outcomes.

### D-C4 — globally complete result with an ambiguous root

No current discovery capability publishes `COMPLETE` completeness together with
finite roots, so matrix row B17 cannot be built end to end. It is pinned
structurally on a real `MULTIPLE` result republished as `COMPLETE`
(`MODEL#globalCompletenessNeverMakesAnAmbiguousRootAdmissible`): completeness
never makes an ambiguous class admissible.

### D-C5 — the superseded-pin inventory was incomplete

Stage 1 identified one approved regression that pinned a certifiable generic
pair as rich-only (G9S1-R1, matrix B26). The first complete `final.shared` run
failed exactly two more tests. Both pinned the same pre-ADR-0028 state for affine
scalar pairs, which the frozen positive B5 now certifies:

- `G9U0IntersectionTokenTest#i17CanonicalLocusPairLineageIsSourceOrderInvariant`
  asserted that no root of an affine × affine pair is point-admissible;
- `G9U0R4IntersectionAdmissibilityContinuationTest#stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence`
  asserted `NOT_ESTABLISHED` for its Locus V2 target.

Each test keeps its purpose, and its other assertions are unchanged. The first
now asserts that the roots are admissible and that admissibility is invariant
under source order. The second asserts exact evidence for its Locus V2 target,
as for every other target family. The static contract that the `G9U0-R4`
verifier applies to that method does not cover the changed branch. B26 and B7
still pin the capability boundary itself (matrix B27, B28).

### Retained limitations found in Stage 2 (all fail-closed)

| Identifier | Finding | Truthful state |
|---|---|---|
| `PAIR-COPY-GENERATOR-PARAMETERIZATION-CONTRACT` | a generator side's parameterization contract names its driver's durable identity; the closure-copy map rebases only the two sources | the copied point stays dormant; the copy's own roots remain explicitly materializable (pair spec §10) |
| `CLIPBOARD-COPY-POINT-DRIVEN-LOCUS-SLICE` (pre-existing) | host closure copy of a point-driven locus whose slice has intermediate objects fails with `MALFORMED_RECORD` | copy refused; nothing is created |
| `A3-PAIR-SOURCE-COMPATIBLE-STRUCTURAL-REDEFINE` (pre-existing) | A3 rejects a compatible structural redefinition of a pair source that has materialized points, also for spline × spline | `REDEFINE_REJECTED`; the construction is unchanged |
| `LOCUS-PARTICIPANT-AXIS-NOT-SERIALIZABLE` (pre-existing) | `xAxis`/`yAxis` used directly as an intersection target or a mirror line of a semantic curve are participating geos without a stable ordinary-element attachment; the user guide §10.4 already states it for reflection | creation fails with `GEO_NOT_SERIALIZABLE`; a user-defined line works |
| generator transport under R5 images (§10) | open author question | not admitted |

### Pre-existing wrong-value defects found (outside this task's scope)

Both defects lie in the existing reconstructible evaluator, which this task does
not change. Each was reproduced by the same probe on the implementation base
`a222b82aa`, in a temporary scratch worktree, with identical results. The
certified construction model refuses both shapes, so no certified pair root is
ever built on a wrong curve. Existing floating features that consume the
evaluator do use the wrong values. `Length` and semantic point positions were
observed directly. Render samples and single-source intersections read the same
evaluator, but they were not measured separately. Both defects are reported for
a separately authorized correction.

| Identifier | Defect | Base reproduction |
|---|---|---|
| `RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG` | after `GeoSegment.pathChanged` the evaluator does not refresh a `SEGMENT_POINT` driver's inhomogeneous coordinates, so a slice that reads them (a join or midpoint through the driver) evaluates the previous parameter; the certification's floating verification refuses every such root (construction-model spec §2) | `ls=LocusV2(Midpoint(Cs,K),Cs)` with `Cs` on the segment `(0,0)–(4,0)` and `K=(0,2)`: `t=0.1 -> (1.996,1)` instead of `(0.2,1)`; `Length(ls,Pa,Pb)` between the semantic points at `0.25` and `0.75` is `2.25` instead of `1`; `Length(ls)` is undefined instead of `2` |
| `RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED` | a slice that passes an inline literal to a command, for example `Line(Cj,(0,3))`, is isolated without its dependence on the driver and evaluates one constant point for every parameter; capture refuses it by rule 5 of the construction-model spec §3 | a pedal locus on a circular arc: every parameter gives `(0.986,0.370)`; `Length` is `0`, while the same construction with `T3=(0,3)` has length `2.15` |

The user guides' known limitations name both defects with a workaround. That
text does not fix either defect.

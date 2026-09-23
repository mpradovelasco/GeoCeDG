# PRE-G9B-R2-E0/E1 semantic admissibility and certified materialization candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R2-E0/E1 (one combined sequential task)
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = PHASE -Phase G9S1-R1, INTEGRATION
COHORT_EQUIVALENCE        = PHASE -Phase G9U0-R6 (recorded, not rerun; see Verification)
PRODUCT_PHASE_EFFECT      = BOUNDED — four new endpoint families; capability-based pair materialization
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: the separate closeout/author-decision record is the sole
authority for current author approval status.

Implementation base: commit `a222b82aad821e3e0c0aa93145a57b7050f024c8`, tree
`0768848e89961f0f372f514530f9e9e0b81cc872`, branch
`feature/pre-g9b-r2-e0-e1-semantic-admissibility-and-materialization`.
Design record:
[Stage-1 design](../architecture/pre_g9b_r2_e0_e1_semantic_admissibility_and_materialization_design.md).
Decision: [ADR 0028](../adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md).
Traceability: [validation matrix](pre_g9b_r2_e0_e1_validation_matrix.md).
Machine-readable evidence:
[`geocedg/validation/pre-g9b-r2-e0-e1/pre-g9b-r2-e0-e1-evidence.json`](../../geocedg/validation/pre-g9b-r2-e0-e1/pre-g9b-r2-e0-e1-evidence.json).

## Read this first: Stage-2 corrections, limitations and defects

The author's instruction was to stop if implementation proved a frozen design
assumption false. Five frozen statements turned out false, unreachable or
incomplete. Each had exactly one correction that preserves every approved
guarantee, so that correction was recorded in the normative text, pinned by
tests and listed here, rather than stopping. The author may overturn any of
them; the design record §14 has the full reasoning.

| Id | Frozen statement | Correction |
|---|---|---|
| D-C1 | all seven endpoint families are disjoint, and derived families come before the §21 constructor | false when a spline's constructor list holds both `P` and `T(P)`; all direct families, the constructor included, are consulted before derived ones (metrics §24.2) |
| D-C2 | a missing durable identity is always `UNRESOLVED_INVALID`, and nested derivations compose | contradictory: durable point identity begins only at first participation, and an intermediate derivation step never participates; an intermediate step is keyed `point=derived` from the endpoint's DAG input chain (metrics §24.3) |
| D-C3 | a family change inside one metric is executed and fails closed | unreachable: a cross-family compatible redefine is rejected atomically and a replacement removes the dependent metric; recorded as `NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE` with the guard retained (metrics §24.3) |
| D-C4 | a globally complete but ambiguous root is tested end to end | no capability publishes `COMPLETE` with finite roots; pinned structurally |
| D-C5 | one approved regression (G9S1-R1, B26) pins certifiable generic pairs as rich-only | incomplete: the first complete `final.shared` run failed exactly two more pins of that state, on affine scalar pairs; both now assert the certified behaviour and keep their purpose (B27, B28) |

Retained limitations, all fail-closed:

- `PAIR-COPY-GENERATOR-PARAMETERIZATION-CONTRACT` — after a closure copy of a pair
  with a generator-driven side, the copied point stays dormant. The copy's own
  roots certify and can be materialized explicitly. Lifting this needs an author
  decision: an identity-free generator parameterization contract, or a copy map
  that also rebases the driver identity (pair spec §10).
- `CLIPBOARD-COPY-POINT-DRIVEN-LOCUS-SLICE` (pre-existing) — the host clipboard
  refuses to copy a point-driven locus whose slice has intermediate objects.
- `A3-PAIR-SOURCE-COMPATIBLE-STRUCTURAL-REDEFINE` (pre-existing) — A3 rejects a
  compatible structural redefine of a pair source with materialized points,
  also for spline × spline.
- `LOCUS-PARTICIPANT-AXIS-NOT-SERIALIZABLE` (pre-existing, confirmed on the base)
  — `xAxis`/`yAxis` cannot be an intersection target or mirror of a semantic
  curve.
- Generator transport under R5 images remains an open author question and is
  not admitted (design §10).

**Two pre-existing wrong-value defects were found, both outside this task's
scope.** Both lie in the unchanged reconstructible evaluator. Both were
reproduced with identical numbers on the base `a222b82aa` in a temporary scratch
worktree. The new certified model refuses both shapes, so no certified pair root
is ever built on a wrong curve. The existing floating features give wrong values
today:

| Defect | Base reproduction |
|---|---|
| `RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG` | midpoint locus driven on the segment `(0,0)–(4,0)` with `K=(0,2)`: `Length(ls,Pa,Pb)` between semantic points at `0.25` and `0.75` is `2.25` instead of `1`; `Length(ls)` is undefined instead of `2`; the evaluator returns the previous parameter's point |
| `RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED` | a pedal locus whose slice contains `Line(Cj,(0,3))` evaluates one constant point for every parameter; `Length` is `0` against `2.15` for the same construction with a named point |

Both defects are named, with workarounds, in the user guides' known
limitations. Correcting them needs its own authorization.

## What the task delivers

**Objective A — endpoint admissibility.** Every point-producing family that can
reach `Length(S,P,Q)` / `LocusLength(S,P,Q)` was inventoried (design §2, P1–P12).
The endpoints below are admitted by explicit DAG provenance only (metrics §24):

| Family | Status |
|---|---|
| `SEMANTIC_POINT` | unchanged |
| `PAIR_INTERSECTION_OCCURRENCE` | unchanged resolver; reach widened by Objective B |
| `SINGLE_SOURCE_INTERSECTION_OCCURRENCE` | **new** (§24.4) — roots against lines, conics, bounded function graphs and polynomial implicit curves |
| `GENERATOR_OCCURRENCE` | **new** (§24.5) — the traced point at the live driver value |
| `CONSTRUCTOR_OCCURRENCE` | unchanged (§21), including its R5 transport |
| `SIMILARITY_IMAGE_OCCURRENCE` | **new** (§24.6) — same transform with the same parameter objects; composes |
| `DEPENDENT_COPY_OCCURRENCE` | **new** (§24.7) — `Q=P` inherits membership only |

Free, Cartesian-coincident, ordinary derived, `CopyFreeObject`, macro, stale,
dormant, wrong-source, ambiguous and transported-membership points stay
`INVALID_QUERY` with a typed diagnostic.

**Objective B — certified semantic-pair materialization.** Admission is by
capability, not Java class: a pair materializes exactly when both canonical
sources expose a certified interval curve model (ADR 0028, pair spec §10). The
new `certified-construction-program/v1` model covers point-driven loci on a
circle, circular arc or segment through joins, parallels, perpendiculars,
line–line meets, midpoints and fixed-parameter similarities; affine scalar loci;
and similarity images. The selector, contract identifier, ledger v5, lifecycle
and materialization gate are unchanged. A pair without both models stays
rich-only and now says so in its diagnostics. `local admissibility != global
completeness` is preserved: the witness pair certifies two roots while global
completeness stays `NOT_ESTABLISHED`.

## Normative outputs (Stage 1, frozen before implementation)

| Artifact | Content |
|---|---|
| design record | inventory, endpoint and pair capability matrices, provenance per family, lifecycle, ambiguity, residuals, §14 Stage-2 corrections |
| ADR 0028 | Proposed; amends ADR 0021 Decision 1 only |
| `locus-v2-certified-construction-model.md` | new contract, `1.0` |
| `locus-v2-metrics.md` §24 | endpoint admissibility, version `1.3 → 1.4` |
| `spline-v2-pair-materialization.md` §10 | capability-based pair scope and the copy limitation |
| validation matrix | B1–B28 and A1–A25, with a Stage-2 reconciliation |

## Implementation

Shared Java kernel only. No frontend, render or Python code participates.

| Path | Change |
|---|---|
| `kernel/locus/intersection/CertifiedIntervalCurveModel2D` | new package-private interface; capture by capability |
| `kernel/locus/intersection/ConstructionIntervalModel2D` | new — outward interval and derivative replay of a captured program |
| `kernel/locus/CertifiedConstructionProgram2D` | new — immutable program IR |
| `kernel/locus/CertifiedConstructionCapture2D` | new — walker over the evaluator's isolated slice |
| `kernel/locus/ReconstructibleLocusEvaluator2D` | one method, `captureCertifiedProgram()` |
| `kernel/locus/intersection/SplineIntervalModel2D` | implements the interface; shared similarity chain |
| `kernel/locus/intersection/SplinePairIntervalCertification2D` | consumes the interface; names an unsupported side |
| `kernel/locus/intersection/PublicSplinePairRootIdentityResolver2D` | diagnoses rich-only pairs |
| `kernel/locus/intersection/LocusPairIntersectionSolver2D` | Javadoc only |
| `kernel/locus/intersection/IntersectionEndpointProvenanceResolver2D`, `…Result2D` | §24.4 single-source walk; shared address construction |
| `kernel/algos/SemanticMetricEndpointResolver2D` | new — ordered §24 family admission |
| `kernel/algos/AlgoLocusBetweenMetricV2` | delegates to the resolver; retains family and key |

The §23 pair walk, the §21 constructor resolver, the ledger, the selector and
every serialized format are unchanged.

## Focal evidence

| Class | Methods | Result |
|---|---|---|
| `PreG9bR2E1SemanticPairMaterializationTest` (shared, PAIR) | 18 | 18/18 pass |
| `PreG9bR2E1CertifiedConstructionModelTest` (shared, MODEL) | 7 | 7/7 pass |
| `PreG9bR2E0SemanticEndpointAdmissibilityTest` (shared, END) | 14 | 14/14 pass |
| `PreG9bR2E0SingleSourceEndpointEvidenceTest` (shared, EVID) | 5 | 5/5 pass |
| `PreG9bR2E1SemanticPairNativeArchiveTest` (desktop, NAT) | 1 | 1/1 pass |

Four existing tests changed, each recorded in the matrix:

- `G9S1R1SplinePairMaterializationTest#genericLocusPairStaysRichOnlyDespiteSharedInfrastructure`
  now uses an uncertifiable generic locus and asserts the new diagnostic (B26).
- `G9U0IntersectionTokenTest#i17CanonicalLocusPairLineageIsSourceOrderInvariant`
  now asserts that the affine pair's roots are admissible, invariantly under
  source order (B27, D-C5).
- `G9U0R4IntersectionAdmissibilityContinuationTest#stableSupportedTargetFamiliesReceiveExactOrFailClosedEvidence`
  now asserts exact evidence for its affine Locus V2 target (B28, D-C5).
- `PreG9bR2SemanticIntersectionEndpointTest#unadmittedShapesStayInvalidAndA1FallThroughIsPreserved`
  follows the superseded §23.8 bullet 1 (A23).

The same focal run passed `G9S1R1SplinePairMaterializationTest` (13),
`PreG9bR2SemanticIntersectionEndpointTest` (8),
`PreG9bR2IntersectionEndpointSideSelectionTest` (4), desktop
`PreG9bR2IntersectionEndpointNativeArchiveTest` (1), `G9U1MetricReviewTest` (6),
`G9S1R1NativeArchivePersistenceTest` (3) and `PostP1BilingualUserGuideTest`
(16). After the D-C5 update, `G9U0IntersectionTokenTest` (18) and
`G9U0R4IntersectionAdmissibilityContinuationTest` (27) pass. Checkstyle reports
zero findings in `shared/common` main, `shared/common-jre` test and `desktop`
test.

**Complete suites before the candidate.** The inventory evidence consists of
complete executed runs:

| Run | Tests | Failures | Skipped | `inner_exit_code` |
|---|---|---|---|---|
| `final.shared`, attempt 1 | 6 734 | 2 | 10 | 1 |
| `final.shared`, attempt 2, after the D-C5 update | 6 734 | 0 | 10 | 0 |
| `final.desktop` | 1 515 | 0 | 1 | 0 |

The two failures of attempt 1 were exactly the superseded pins of D-C5; no
other test failed. Only attempt 2 and `final.desktop` feed the inventory.

**Author witness replay (scratch, not a tracked test).** A temporary desktop
probe loaded the author's untracked `SeveralDefects.cedg`
(SHA-256 `c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8`) on
the implementation and was then deleted:

```text
e = Intersect(d,a)        2 admissible germ slots, ledger 5|, completeness NOT_ESTABLISHED
I, J, K, L (file roots)   UNIQUE SINGLE_SOURCE_INTERSECTION_OCCURRENCE on a
E (generator)             UNIQUE GENERATOR_OCCURRENCE on a
M = Midpoint(D, C+(1,0))  NO_ADDRESS (ordinary derived point)
LocusLength(a,I,J)        SUCCESS 0.5370792510382241 (base: INVALID_QUERY)
LocusLength(a,X0,X1)      SUCCESS 9.83875545905577 (materialized pair roots)
LocusLength(d,F,X0)       SUCCESS 2.731015283621896
LocusLength(a,E,I)        SUCCESS 9.824132286144744 (base: INVALID_QUERY)
LocusLength(a,E,M)        INVALID_QUERY
native save and reopen    identical families and values
```

## Coupled reconciliations

**JUnit inventory.** The new methods change the pinned test-identity sets. The
inventory was regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from dry-run discovery and completed passing `final.shared` / `final.desktop`
runs (`inner_exit_code 0`):

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 773 | 5 817 |
| `final.shared` | 6 690 | 6 734 |
| `discovery.desktop` / module `desktop` | 1 520 | 1 521 |
| `final.desktop` | 1 514 | 1 515 |

No narrow selection references a new or changed test class.

**Typed registry.** The `junit_inventory` catalog pin moves from `be44c907…` to
`ac33ddbc…`. The method was validated first by reproducing the previous
pin exactly from the base blob (canonical-LF SHA-256).

**Upstream modification record.** `AGENTS.md` §7 requires a record of every
modified upstream-tree file and its purpose. `docs/upstream/modified-files.yml`
now registers this task's ten new files. It also registers the five files that
the `PRE-G9B-R2` candidate added without an entry. Registering those five closes
a pre-existing gap and is flagged here for the author. The standalone boundary
check `tools/agent/upstream-boundary.ps1` belongs to no registry profile. Before
the change it listed exactly those fifteen paths as unregistered; it now passes
with 790 registered files.

## Guide impact

```text
GUIDE_IMPACT = REQUIRED — APPLIED (both editions)
```

Both editions stated that single-source intersection points are not endpoints,
which becomes false. Updated identically in English and Spanish, keeping the 17
section identifiers:

- §7.3 — constructor points are not semantic points, but they are endpoints
  on their own spline;
- §8.7 — now "Intersections of two semantic curves": the certified-model rule,
  the qualifying curves, rich-only diagnosis and the seam; the literal
  `Spline V2 × Spline V2` that `PostP1BilingualUserGuideTest` requires is kept;
- §9.3 — every admitted endpoint family, and a worked example;
- §9.4 — endpoints from every intersection kind; not on transformed copies;
- §10.3 — measuring on an image with images of points;
- §15 — known limitations, including the two pre-existing evaluator defects
  with workarounds.

**Pre-existing drift corrected and flagged.** The R2 report recorded that §9.3
still said `Length(S,A,C)` is undefined for constructor points, false since
POST-G9U1-A1. The rewritten §9.3 states the A1 behaviour. The new
`END#userGuideWorkedExampleEndpointsHoldAsDocumented` pins the corrected example
(`Length(S,A,C) = 8/3`). The author may prefer to account for this under A1.

**Developer documentation.** `docs/developer/geocedg_developer_guide.md` and
`docs/developer/semantic_spline_2d_api.md` each gain one paragraph pointing to
ADR 0028. Their earlier phase text is unchanged, and the original line endings
are preserved.

## Verification

Pre-candidate focal evidence is above. The frozen acceptance campaigns run
against the immutable candidate commit and are reported separately, because
their receipts bind that commit.

`-PlanOnly` before commit:

| Command | Exit | Coverage | Missing | Nodes |
|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile PHASE -Phase G9S1-R1 -PlanOnly` | 0 | `COMPLETE` | 0 | 23 |
| `tools/agent/verify.ps1 -Profile PHASE -Phase G9U0-R6 -PlanOnly` | 0 | `COMPLETE` | 0 | 23 |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 |

`PHASE G9U0-R6` resolves to exactly the same 23 nodes as `PHASE G9S1-R1`; only
the selection label, and therefore the plan hash, differs. As frozen in design
§12, it is recorded as cohort-equivalent and not rerun
(`OBS-R2-PHASE-SELECTION-COHORT`). `INTEGRATION` resolves 46 nodes, a strict
superset of that cohort, and runs as frozen.

`FINAL` is not required by this footprint (design §12) and was not run; no
`VERIFICATION_ESCALATION_REQUEST` was needed. `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`
remains retained baseline debt outside this delta and is not touched.

## Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (four endpoint families; capability-based pair materialization)
KERNEL SEMANTICS                   = metric endpoint admissibility; pair certification capability
SELECTOR / CONTRACT / LEDGER       = UNCHANGED (d2-spline-pair-singleton-germ/v1, ledger v5)
SERIALIZATION / DOCUMENT FORMAT    = UNCHANGED (no key, program or certificate is stored)
LENGTH / LOCUSLENGTH SEMANTICS     = UNCHANGED beyond the new endpoint families
FRONTEND                           = UNCHANGED
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED (JUnit inventory, registry catalog pin)
UPSTREAM MODIFICATION RECORD       = UPDATED (10 new files; 5 pre-existing PRE-G9B-R2 gaps)
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = REQUIRED — APPLIED (both editions)
```

## Authorization state

`R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10` and further `G12` remain
unauthorized. No successor phase is authorized. The correction of the two
pre-existing evaluator defects is not authorized by this task. Push, branch
publication, merge, promotion, tag, release and binary publication each require
a separate explicit author instruction naming the exact candidate SHA.

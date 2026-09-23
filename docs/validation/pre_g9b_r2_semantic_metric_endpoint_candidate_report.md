# PRE-G9B-R2 semantic metric endpoint provenance candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R2
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = PHASE -Phase G9S1-R1, PHASE -Phase G9U0-R6, INTEGRATION
PRODUCT_PHASE_EFFECT      = BOUNDED — one new admitted metric endpoint family
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: the separate closeout/author-decision record is the sole
authority for current author approval status.

Implementation base: commit `51a6b096335938bb2580eb3b8e1101db0f2f1575`, tree
`1a432a59cde9b6421a3c1089751c26469574d00a`, worktree clean at entry.
Traceability: [validation matrix](pre_g9b_r2_semantic_metric_endpoint_validation_matrix.md).
Machine-readable evidence:
[`geocedg/validation/pre-g9b-r2/pre-g9b-r2-endpoint-evidence.json`](../../geocedg/validation/pre-g9b-r2/pre-g9b-r2-endpoint-evidence.json).

## What the phase closes

`TD-P1-SEMANTIC-LENGTH-INTERSECTION`: `Length(S,P,Q)` and `LocusLength(S,P,Q)`
were undefined when an endpoint was a point materialized from a
`Spline V2 × Spline V2` intersection. The metric admitted only explicit semantic
points on `S` and A1 constructor occurrences; an intersection point is a bare
`GeoPoint` whose provenance lives on its algorithm and rich result, so both shapes
failed.

Both forms are now **defined** for such an endpoint on either intersected curve,
and every ambiguous, stale, dormant or non-matching case fails closed to
`INVALID_QUERY`.

The phase answers exactly one question — whether an *already materialized*
intersection point can address a semantic endpoint — and does not change which
pair roots may be materialized. `R2-E0`/`R2-E1` are not widened and remain
unauthorized; D1 is not widened.

## Normative amendment — written before implementation

[`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §23, version
`1.2 → 1.3`, defines the third endpoint family, as the prompt requires, before any
code:

| Clause | Content |
|---|---|
| §23.1 | the admission walk, entirely through explicit DAG provenance, and the closed result `NO_ADDRESS / UNIQUE / MULTIPLE / UNRESOLVED_INVALID` |
| §23.2 | side selection by exact locus-identity equality; `S × S` is `MULTIPLE` |
| §23.3 | currentness: ledger token validity plus semantic-revision equality; a stale identity match is final |
| §23.4 | address construction, mirroring §21 |
| §23.5 | the versioned no-retarget occurrence key |
| §23.6 | family dispatch and structural disjointness |
| §23.7 | persistence, reopen, copy and redefine |
| §23.8 | explicit exclusions |

§21 and §22 are not reinterpreted; the file changed in exactly two places, the
version line and the appended section. The section carries no approval status of
its own.

**ADR judgement.** The amendment is judged an **extension of the accepted A1
contract**, not a new decision: it reuses §21's resolver shape, closed result,
versioned key, retained-key guard and the existing position binder, and ADR 0007
places no constraint on endpoint families. No ADR was created. This judgement is
the author's to overturn.

## Implementation

Shared Java kernel only; no frontend, render or presentation layer participates.

| Path | Change |
|---|---|
| `kernel/locus/intersection/IntersectionEndpointProvenanceResolver2D` | new — the §23 walk |
| `kernel/locus/intersection/IntersectionEndpointProvenanceResult2D` | new — the closed result |
| `kernel/algos/AlgoLocusBetweenMetricV2` | wiring: one field, one dispatch block, one helper |

The walk follows the established in-kernel pattern the prompt names — the
selected-root certifier's point → intersection-point algorithm → rich input →
exact token → admissible solution path, where every non-matching shape returns
early. Dispatch order is semantic point, then this family, then A1. `NO_ADDRESS`
falls through to A1 unchanged; `UNIQUE`, `MULTIPLE` and `UNRESOLVED_INVALID` are
final.

### Design choices that deserve review

1. **Pair intersections only.** Side selection needs per-side locus evidence,
   which only `Locus V2 × Locus V2` roots carry. A point from a single-source
   intersection — a spline against a line or circle — returns `NO_ADDRESS` and
   behaves exactly as before. Extending the family to single-source roots is a
   separate decision.
2. **R5 similarity lineage is not followed.** An intersection root is a root of
   the intersected source, not of its image. §21 follows that lineage for
   constructor occurrences because the transform preserves the knot parameter;
   for intersections the point does not lie on the image in general.
3. **The solver's lifted periodic parameter is not endpoint authority.** It
   records which cover the pair solver searched. The metric package reads lift
   and seam side only through position equality, so importing that search
   coordinate would change same-position behaviour without any geometric reason.
   The semantic parameter is used instead.
4. **`S × S` is not representable today.** `Intersect(S,S)` on a genuinely
   self-crossing spline publishes no point-admissible root — the pair solver
   reports `NUMERICAL_FAILURE` / `UNRESOLVED`. The `MULTIPLE` rule is the
   fail-closed authority for when `R2-E0`/`R2-E1` makes self-pair roots
   representable, mirroring §21's `NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`.

## Focal evidence

Thirteen new test methods; see the [matrix](pre_g9b_r2_semantic_metric_endpoint_validation_matrix.md).

| Class | Methods | Result |
|---|---|---|
| `PreG9bR2SemanticIntersectionEndpointTest` (shared) | 8 | 8/8 pass |
| `PreG9bR2IntersectionEndpointSideSelectionTest` (shared) | 4 | 4/4 pass |
| `PreG9bR2IntersectionEndpointNativeArchiveTest` (desktop) | 1 | 1/1 pass |

The first three are the only tests in the repository that combine an
intersection-materialized point with `Length` / `LocusLength`. The side-selection
tests use **real** kernel-produced pair evidence; for the `S × S` and
duplicate-token cases, real parts are recombined to state the hypothetical and
this is said explicitly in each test.

Nearest regressions, unchanged and passing: A1 (10), A2 (7), `G9U0MetricPosition`
(11), `G9S1SemanticSpline2D` (25), `G9S1R1NativeStructuralPair` (15),
`G9S1R1StructuralSplineLifecycle` (24), `G9U1MetricReview` (6). The two existing
coincident-free-point negatives —
`PostG9U1A1SplineConstructorProvenanceTest#coincidenceAndSelfIntersectionNeverCreateExtraProvenance`
and `G9U1MetricReviewTest#foreignOrCoincidentPointsNeverReceiveInferredMetricPreimages`
— pass unchanged. Checkstyle reports zero findings in every affected source set.

## Coupled reconciliations

**JUnit inventory.** Thirteen new methods change the pinned test-identity sets.
Regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from dry-run discovery and completed passing `final.shared` / `final.desktop`
runs (`inner_exit_code 0`):

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 761 | 5 773 |
| `final.shared` | 6 678 | 6 690 |
| `discovery.desktop` / module `desktop` | 1 519 | 1 520 |
| `final.desktop` | 1 513 | 1 514 |

**Typed registry.** The `junit_inventory` catalog pin moves from `553cc04e…` to
`be44c907…`. The method was validated first by reproducing the previous pin
exactly from the base blob.

## Guide impact

```text
GUIDE_IMPACT = REQUIRED — APPLIED
```

Both official editions documented this exact capability as a limitation; after
this phase that text would be false. Updated in both editions:

- §9.4 now describes the admitted endpoint, identity-based side selection, the
  dormant behaviour, and the two remaining exclusions;
- the known-limitations entry now states the remaining single-source exclusion.

The literal `Spline V2 × Spline V2` that `PostP1BilingualUserGuideTest` requires in
both editions is preserved; that test's explanatory comment was updated because
its intent — "the debt must appear" — would otherwise be stale. All 16 of its
cases pass against the edited guides.

**Pre-existing drift observed, not fixed.** Guide §9.3 states that
`Length(S,A,C)` is undefined for the constructor points `A` and `C` of the §7.5
example. That has been false since POST-G9U1-A1:
`PostG9U1A1SplineConstructorProvenanceTest#uniqueOccurrencesFeedRichAndScalarMetricsAndAgreeWithAddresses`
asserts that the scalar is defined for a structurally identical spline. It is A1's
guide impact, outside this phase's delta, and is reported rather than absorbed.

## Verification

Pre-candidate focal evidence is above. The three prescribed campaigns run against
the immutable candidate commit and are reported separately, since their receipts
bind that commit. A `-PlanOnly` resolution of `PHASE -Phase G9S1-R1` confirmed
before commit that the registry loads with the new pins: `COMPLETE`, zero missing
checks, 23 nodes.

Observation for the author: `PHASE G9S1-R1` and `PHASE G9U0-R6` currently resolve
to the **same thirteen required checks**, and `INTEGRATION` is a superset of 46
nodes, so the prescribed triple largely re-runs one cohort. They are executed as
prescribed.

`BASELINE-G9U1-BRANDING-EVIDENCE-PIN` remains retained baseline debt outside this
phase and is not touched.

## Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (one admitted endpoint family)
KERNEL SEMANTICS                   = metric endpoint admissibility only
INTERSECTION MATERIALIZATION       = UNCHANGED (R2-E0/R2-E1 not widened)
LENGTH / LOCUSLENGTH SEMANTICS     = UNCHANGED beyond the new endpoint family
LEDGER FORMAT / SERIALIZATION      = UNCHANGED (occurrence key not serialized)
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED (JUnit inventory, registry catalog pin)
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = REQUIRED — APPLIED (both editions)
```

## Authorization state

`R2-E0`, `R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10` and further
`G12` remain unauthorized. No successor phase is authorized. Push, branch
publication, merge, promotion, tag, release and binary publication each require a
separate explicit author instruction naming the exact candidate SHA.

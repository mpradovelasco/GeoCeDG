# PRE-G9B-R2-E0/E1 corrective continuation candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R2-E0/E1, bounded corrective continuation
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE (unchanged)
FROZEN_ACCEPTANCE         = PHASE -Phase G9S1-R1, INTEGRATION
COHORT_EQUIVALENCE        = PHASE -Phase G9U0-R6 (recorded, not rerun)
PREDECESSOR_CANDIDATE     = 3d13ea72f64c241d11759617dbdff70e373e6a50 (tree 22bd3794c91b4432771e34ff143879b211fad006), preserved unchanged
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: the separate closeout/author-decision record is the sole
authority for current author approval status.

The predecessor frozen candidate and its
[candidate report](pre_g9b_r2_e0_e1_candidate_report.md) are unchanged. This
descendant applies only the bounded corrections of the continuation instruction.
The final state of the phase is described by the living normative documents:
the [design record](../architecture/pre_g9b_r2_e0_e1_semantic_admissibility_and_materialization_design.md),
[ADR 0028](../adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md),
[metrics §24](../../geocedg/specs/locus/locus-v2-metrics.md),
[pair materialization §10](../../geocedg/specs/curves/spline-v2-pair-materialization.md),
the [construction model](../../geocedg/specs/locus/locus-v2-certified-construction-model.md)
and the [validation matrix](pre_g9b_r2_e0_e1_validation_matrix.md).
Machine-readable evidence:
[`geocedg/validation/pre-g9b-r2-e0-e1/pre-g9b-r2-e0-e1-corrective-evidence.json`](../../geocedg/validation/pre-g9b-r2-e0-e1/pre-g9b-r2-e0-e1-corrective-evidence.json).

## What the continuation instruction disposes

- `D-C1`, `D-C3`, `D-C4` and `D-C5` accepted as recorded; no change.
- `D-C2` accepted with a mandatory clarification: a structural address inside
  the semantic derivation chain, never an index, XML position, order, label,
  Java reference, coordinate or proximity.
- The transformation covariance rule adopted for the supported similarities.
- The copied-pair limitation to be corrected by remapping the driver identity
  as copy provenance, never by removing it from the parameterization contract.
- The two evaluator defects retained as debt, not repaired.
- The accepted E0/E1 architecture preserved.

## 1. D-C2 structural address

Metrics §24.3 now defines the address of a derivation step as

```text
derivation root  +  operation kind  +  versioned step path  +  parameter identities
```

The derivation root is the innermost direct-family occurrence, with durable
identities. The operation kind is `transform=<versioned R5 similarity contract>`
or `operation=dependent-copy/v1`. Every step below the endpoint carries
`step=derivation-step/v1`. The parameters are the durable identities of the
transform's parameter objects, in input order.

The resolver follows that meaning exactly. The "same parameter objects" test
compares durable identities instead of object references, and a source-side
parameter without one is `UNRESOLVED_INVALID`.

The predecessor wrote `point=derived` only while an intermediate step had no
identity. That made the retained key move as soon as the intermediate gained an
identity by participating elsewhere, so a valid nested metric would then have
failed as a retarget. The structural marker removes that dependence.

## 2. Transformation covariance

```text
P ∈ L, P2 = T(P), L2 = T(L), same authoritative T   =>   P2 ∈ L2
```

The implementation already admitted `T(P)` on `T(L)`, including the generator
point. The continuation reconciles the wording and adds tests, with no redundant
code:

- metrics §24.6 states the rule and what "the same `T`" means;
- §24.8 records that `E` itself is not an endpoint on `T(S)`;
- ADR 0028 Decision 8 and design §10, formerly an open question, are resolved.

The tests pin all seven similarity contracts (`translate-vector`,
`rotate-origin`, `rotate-point`, `reflect-point`, `reflect-line`,
`dilate-origin`, `dilate-point`). For each, `T(E)` is an endpoint on `T(L)` with
the expected length and a key that names its contract and parameter identities.
`E` on `T(L)` and `T(E)` on `L` are `NO_ADDRESS`. Separate objects with equal
values, or the same parameters under another contract, are not the same `T`.

## 3. Copy/remap correction

A pair selector's parameterization contract for a generator-driven source is
`<generator family>/true-coordinate/<durable driver id>`. The driver identity
stays in the contract. On an immediate closure copy, the copy map now remaps
every named participant together with the two sources:

```text
sourceId(original) -> sourceId(copy)
driverId(original) -> driverId(copy)
```

Each participant association is proved at the copy seam. The copied
participant's identity record must name the original as its copy source. It is
never searched by label, order or coordinates. A missing, reused or reshaped
participant fails closed.

The persisted v5 copy evidence is byte-for-byte the same format: the original
selector, its proof and the two-source map. On import, the participant
association is re-derived exactly by source association between the recorded
original selector and the copied binding. A copied binding that still names an
original participant is a malformed ledger.

| Changed code | Change |
|---|---|
| `kernel/locus/intersection/PairSemanticSlotSelector2D` | participant grammar; `remap(sources, participants)`; `participantCorrespondence`; source-only pair identity; shared contract composition |
| `kernel/locus/intersection/LocusIntersectionTokenLedger2D` | participant map on copy authorization; remap in `copiedPairEntry`; import-time correspondence check; `getRetainedPairSelector` |
| `kernel/geos/GeoLocusIntersectionResult` | proves the participant map from identity-record copy provenance; `getRetainedPairSelector` |
| `kernel/locus/intersection/PublicSplinePairRootIdentityResolver2D` | uses the shared contract composition (byte-identical output) |
| `kernel/locus/SemanticGeneratorDomainProvider1D` | names its `true-coordinate/` grammar as a constant |
| `kernel/algos/SemanticMetricEndpointResolver2D` | D-C2 structural address; durable parameter identities |

Every property the instruction lists is tested:

- the original pair and ledger stay unchanged;
- the copied source, driver and point receive new identities;
- the copied selector names only copied identities;
- the copied point is current and admissible;
- metric endpoint resolution and `LocusLength` work on the copied curve;
- XML and native `.cedg` reopen preserve the relation;
- moving and then deleting the original leaves the copy current, so there is no
  cross-link.

## 4. Retained evaluator debts, one description corrected

| Debt | Certified-path consequence | Floating consequence |
|---|---|---|
| `RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG` | the certification's floating verification refuses every affected root (`PAIR#segmentDriverInhomogeneousReadIsRefusedByTheCoherenceGate`) | positions and lengths lag one parameter: `Length(ls,Pa,Pb)` is `2.25` instead of `1` |
| `RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED` | capture refuses the slice (`MODEL#theInlineLiteralDefectShapeIsRefusedRatherThanCertified`) | the locus evaluates to one constant point; `Length` is `0` instead of `2.15` |

**Correction.** The predecessor described the second defect as any inline
literal passed to a command, for example `Line(Cj,(0,3))`. Separate probes show
this was too broad. One inline literal, in the join, the perpendicular or the
arc definition, is replayed correctly and certified
(`MODEL#aSingleInlineLiteralSliceIsReplayedAndCertified`). Inline literals in two
chained commands of the slice, as points or vectors, lose the driver dependence.
The design record, construction-model spec, ADR and both guides now state the
verified shape. Neither defect is repaired here.

## 5. Evidence

### Focal evidence

| Class | Methods | Result |
|---|---|---|
| `PreG9bR2E1SemanticPairMaterializationTest` (shared, PAIR) | 19 | 19/19 pass |
| `PreG9bR2E1PairCopyParticipantTest` (shared, PCP, new) | 4 | 4/4 pass |
| `PreG9bR2E0SemanticEndpointAdmissibilityTest` (shared, END) | 17 | 17/17 pass |
| `PreG9bR2E1CertifiedConstructionModelTest` (shared, MODEL) | 9 | 9/9 pass |
| `PreG9bR2E1SemanticPairNativeArchiveTest` (desktop, NAT) | 2 | 2/2 pass |

One existing test was replaced, as the instruction intends:
`PAIR#closureCopyOfAConstructionPairReCertifiesAndLeavesTheCopiedPointDormant`
pinned the retained limitation and became false. It is now
`PAIR#closureCopyRemapsSourcesAndDriverAndKeepsTheCopiedPointCurrent` (matrix
B14). No other existing test changed an assertion. One explanatory comment in an
existing MODEL test now states the verified defect shape.

The focal run of the 25 affected shared classes passed 351 tests with no
failure. The desktop focal run passed NAT (2),
`PreG9bR2IntersectionEndpointNativeArchiveTest` (1),
`G9S1R1NativeArchivePersistenceTest` (3), `G9U1MetricReviewTest` (6) and
`PostP1BilingualUserGuideTest` (16). Checkstyle reports zero findings in
`shared/common` main, `shared/common-jre` test and `desktop` test. The
standalone upstream boundary check passes with 791 registered files.

**Complete suites before the candidate.** The inventory evidence consists of
complete runs produced by
[`gradle-test-evidence-producer.ps1`](../../tools/agent/checks/gradle-test-evidence-producer.ps1)
on the corrective working tree:

| Run | JUnit files | Tests | Failures | Skipped | `inner_exit_code` |
|---|---|---|---|---|---|
| `discovery.shared` (dry run) | 672 | — | — | — | 0 |
| `discovery.desktop` (dry run) | 100 | — | — | — | 0 |
| `final.shared` | 672 | 6 744 | 0 | 10 | 0 |
| `final.desktop` | 99 | 1 516 | 0 | 1 | 0 |

The skipped identities are exactly the allowlisted `DISABLED` and `CONDITIONAL`
cases of the inventory.

**Author witness replay (scratch, not a tracked test).** Item 5 requires the
`SeveralDefects.cedg` positive witness to keep passing. The predecessor's probe
was replayed unchanged on the corrective tree. It stayed in the session
scratchpad and joined the desktop test source set for that one invocation only,
through a Gradle init script. Nothing was written to the source tree, and the
probe's compiled class and result file were then removed from the build
directory. Witness SHA-256:
`c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8`.

```text
e = Intersect(d,a)        2 admissible germ slots, ledger 5|, completeness NOT_ESTABLISHED
I, J, K, L (file roots)   UNIQUE SINGLE_SOURCE_INTERSECTION_OCCURRENCE on a
E (generator)             UNIQUE GENERATOR_OCCURRENCE on a
M = Midpoint(D, C+(1,0))  NO_ADDRESS (ordinary derived point)
LocusLength(a,I,J)        SUCCESS 0.5370792510382241
LocusLength(a,X0,X1)      SUCCESS 9.83875545905577 (materialized pair roots)
LocusLength(d,F,X0)       SUCCESS 2.731015283621896
LocusLength(a,E,I)        SUCCESS 9.824132286144744
LocusLength(a,E,M)        INVALID_QUERY
native save and reopen    identical families and values
```

Every line is identical to the predecessor's replay.

### Coupled reconciliations

**JUnit inventory.** The new and replaced methods change the pinned
test-identity sets. The inventory was regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from the four runs above:

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 817 | 5 827 |
| `final.shared` | 6 734 | 6 744 |
| `discovery.desktop` / module `desktop` | 1 521 | 1 522 |
| `final.desktop` | 1 515 | 1 516 |

No narrow selection changed, in count or in identity hash.

**Typed registry.** The `junit_inventory` catalog pin moves from `ac33ddbc…` to
`88b5abe8ba3e1c7c33639d7bd4f7aaa3cfd6d40009fd57c83bf16e0518f3c89e`, by
canonical-LF SHA-256. The method was first validated by reproducing
`ac33ddbc…` exactly from the predecessor's committed inventory blob. The change
is one line, and the registry's line endings are unchanged.

**Upstream modification record.** `docs/upstream/modified-files.yml` registers
the one new test file and appends the `PRE-G9B-R2-E0/E1` purpose to each
modified kernel file. The boundary check passes with 791 files.

### Verification

`-PlanOnly` before commit:

| Command | Exit | Coverage | Missing | Nodes | Plan hash |
|---|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile PHASE -Phase G9S1-R1 -PlanOnly` | 0 | `COMPLETE` | 0 | 23 | `4cb0ecf5caa86cc1d26d65a0673c352d83b83ed20de706ee23166e942e0ad430` |
| `tools/agent/verify.ps1 -Profile PHASE -Phase G9U0-R6 -PlanOnly` | 0 | `COMPLETE` | 0 | 23 | `db8be1b098fcbda0ad294de2158e3a298c913f43212849cd4fc8cf11f8aa48c2` |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |

The `PHASE G9S1-R1` and `INTEGRATION` plan hashes equal those of the
predecessor's accepted campaigns, so the continuation leaves the resolved plans
unchanged.

`PHASE G9U0-R6` resolves to the same 23 nodes as `PHASE G9S1-R1`, identical in
full content and order. Only the selection label, and therefore the plan hash,
differs. It is recorded as cohort-equivalent and not rerun
(`OBS-R2-PHASE-SELECTION-COHORT`). `INTEGRATION` resolves 46 nodes, a strict
superset of that cohort, and runs as frozen.

`FINAL` is not required. The frozen class `INTEGRATED_PHASE`
(`verification-levels.md` §12.8) requires PHASE plus the frozen INTEGRATION
coverage. The corrective footprint touches kernel code, tests, documentation,
the JUnit inventory and its catalog pin, the same kinds of change as the
predecessor. It does not touch build, packaging, bootstrap, the verifier, the
registry schema or any serialization format. No
`VERIFICATION_ESCALATION_REQUEST` is needed.

The frozen acceptance campaigns run against the immutable descendant commit and
are reported separately, because their receipts bind that commit.

## Superseded statements of the predecessor report

| Predecessor statement | Now |
|---|---|
| `PAIR-COPY-GENERATOR-PARAMETERIZATION-CONTRACT`, a retained limitation: the copied point stays dormant | resolved; the copied point is current (§3) |
| generator transport under R5 images: an open author question, not admitted | resolved by covariance: `T(E)` is admitted, `E` is not (§2) |
| D-C2 intermediate steps keyed `point=derived` | structural address with `step=derivation-step/v1` (§1) |
| inline-literal defect: "an inline literal passed to a command" | two inline literals in chained slice commands (§4) |
| guide known limitation on copied points | removed from both editions |

## Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (copied point-driven pairs stay current; D-C2 key refinement)
SELECTOR / CONTRACT / LEDGER       = UNCHANGED FORMAT (selector v1, contract, ledger v5, copy evidence encoding)
SERIALIZATION / DOCUMENT FORMAT    = UNCHANGED
FRONTEND                           = UNCHANGED
EVALUATOR                          = UNCHANGED (both defects retained)
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED (JUnit inventory, registry catalog pin)
UPSTREAM MODIFICATION RECORD       = UPDATED (one new test file; E0/E1 purposes appended)
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = REQUIRED — APPLIED (both editions)
```

## Authorization state

The continuation authorizes only the bounded corrections above. It does not
authorize repair of the two evaluator defects, `R3` or later, `G9B`, `G9C`,
`G9U2`, productive `G10`, or release and publication. No successor phase is
authorized. Push, branch publication, merge, promotion, tag, release and binary
publication each require a separate explicit author instruction naming the exact
candidate SHA.

# G9U0-R1 Locus V2 public creation and Desktop lifecycle closeout

```text
G9U0-R1 = PASS — AUTHOR APPROVED
selfApproved = false
authorApproved = true
passClaimed = true
```

## Candidate boundary

- Entry commit: `22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0`.
- Corrective branch: `fix/g9u0-locus-v2-public-creation-lifecycle`.
- Architectural placement: shared kernel / host lifecycle integration.
- G9U0 remains `PASS — AUTHOR APPROVED`; this report neither rewrites nor
  re-approves its historical evidence.
- R1 is corrective public-creation and host-lifecycle hardening; it changes no
  approved G9U0 mathematical semantic, public command or durable-identity rule.
- G9X1 remains frozen in its separate worktree as an implementation candidate
  pending author review.
- No G9U1, G9B, G9C, G9U2 or productive G10 work is included.

The correction preserves the approved mathematical and public API contract.
It changes neither generator/domain semantics nor durable-identity rules.

## Reproduction before the correction

The author-reported interactive construction is:

```text
O = (1,2)
r = 2
c = Circle(O,r)
C = Point(c)
g = PerpendicularLine(C,yAxis)
D = Intersect(g,yAxis)
E = Midpoint(C,D)
L = LocusV2(E,C)
```

The pre-fix focused reproduction executed the same new tests against the clean
entry code. Shared reported 1/4 clean: preview returned `null` after
`GEO_NOT_SERIALIZABLE`; the empty staged handoff threw `instrumentation is
thread-confined`; and the exact circle image expected `4.0` but obtained `4.8`
after the isolated XML loader logged `Undefined variable Axis` for both
`PerpendicularLine(C,yAxis)` and `Intersect(g,yAxis)`. The non-empty foreign
merge rejection remained clean. Desktop reported 0/2 clean: scheduled preview
returned `null` with `GEO_NOT_SERIALIZABLE`, while definitive EDT execution
returned `CAS.GeneralErrorMessage` over `LIFECYCLE_RUNTIME_FAILURE`, caused by
`instrumentation is thread-confined`.

These are three bounded product/host-lifecycle failures, not CAS computation
failures and not invalid geometry. The pre-fix JUnit XML was inspected before
the canonical post-fix reruns replaced module-local test results; the exact
failure classes, assertions and stack seams are retained in this report. No
historical checkout was rewritten to manufacture reproduction evidence.

## Confirmed causes and correction

1. `ScheduledPreviewFromInputBar` evaluates commands with
   `EvalInfo.isScripting() == false`, but `CmdLocusV2` previously entered normal
   durable creation. The command now returns an empty preview output before
   resolving arguments or publishing identity. It does not weaken
   `GEO_NOT_SERIALIZABLE`, persist a preview object or globally disable preview.
2. `SpatialSemanticInstrumentation` captured the Java construction thread and
   checked the live owner before determining that a staged reconciliation was
   empty. The corrected order validates the staged owner first, treats a truly
   empty staged reconciliation as a deterministic no-op across a sequential
   host handoff, and retains the live-owner rejection for every evidence-bearing
   merge. `hasRecordedEvidence()` covers all mutable counters, the publication
   epoch and the per-identity publication counts.
3. The reported construction uses the canonical `yAxis`. The reconstructible
   isolated evaluator now excludes construction-owned canonical constants from
   ordinary-element copying because its `MacroKernel` owns those constants.
   This covers both canonical axes and prevents localization/duplication of axis
   labels without introducing label identity or changing the dependency DAG.

## Exact closeout inventory

The reviewed implementation candidate contained exactly 12 paths: three
productive, four tests, one validation report and four operational/supporting
paths. Author closeout adds only the living roadmap, so the closeout commit
contains exactly 13 paths.

| Path | Reason |
|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocusV2.java` | Isolate provisional input preview from durable creation. |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/SpatialSemanticInstrumentation.java` | Permit only empty sequential host-thread reconciliation while retaining non-empty confinement. |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java` | Preserve `MacroKernel` canonical-constant ownership. |
| `source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2PointDrivenCreationRegressionTest.java` | Reproduce the circle-point DAG and dynamic recomputation. |
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/LocusV2InputPreviewLifecycleTest.java` | Exercise the real input-preview helper and definitive creation. |
| `source/shared/common-jre/src/test/java/org/geocedg/common/spatial/SpatialSemanticInstrumentationSequentialHandoffTest.java` | Prove empty handoff and non-empty cross-thread rejection. |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/locus/LocusV2DesktopLifecycleRegressionTest.java` | Exercise scheduled preview and definitive creation across the Swing EDT boundary. |
| `tools/agent/verify-g9u0-locus-v2-public-surface.ps1` | Anchor the sealed historical authority to the published annotated pass tag and promotion commit while accepting later descendant `HEAD`s. |
| `tools/agent/verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1` | Freeze the corrective boundary and focused authority. |
| `tools/agent/verify.ps1` | Compose R1 after the sealed G9U0 gate. |
| `docs/upstream/modified-files.yml` | Register the four new upstream-boundary test paths and cumulative purposes. |
| `docs/validation/g9u0_r1_locus_v2_public_creation_lifecycle_candidate_report.md` | Record candidate evidence without modifying historical G9U0 evidence. |
| `docs/roadmap/geocedg_roadmap.md` | Record the author-approved R1 closeout in living phase governance. |

Normative G9U0 specifications, ADRs and author-approved evidence are unchanged.

The historical verifier adjustment followed a real static-gate failure on
`22bcc888ebb2ecb102fbeb5b07c87778fddeb3a0`: its previous closeout check assumed
that the current `HEAD` itself was the two-parent G9U0 promotion merge. Current
main legitimately descends from that promotion after BOOK-P0. The correction
anchors the immutable tag object `612845c42925bc519f68443d09fd400ff4365251`
and peeled promotion commit `bdd20da3e9e711dcc35e818d857d604d7b217385`,
checks the original ordered promotion parents there, and requires that promotion
to be an ancestor of current `HEAD`. It does not alter the 93 cases, their
candidate inventory or their evidence.

## Corrective test authority

The R1 authority is exactly 6/6 = 4 shared + 2 Desktop:

| Marker | Test method | Coverage |
|---|---|---|
| `R1-A01` | `reportedCirclePointConstructionUsesNormalV2DagAndRecomputes` | Exact reported geometry, native `GeoLocusV2`, `CIRCLE_POINT`, DAG and recomputation. |
| `R1-B01` | `inputBarPreviewSkipsDurablePublicationAndDefinitiveExecutionStillSucceeds` | Preview leaves XML, construction, identity, reservations, counters and revisions unchanged; definitive creation succeeds. |
| `R1-C01` | `emptyStagedMergeMayCrossASequentialHostThreadHandoff` | Empty staged evidence permits sequential host handoff without publication. |
| `R1-C02` | `nonEmptyStagedMergeStillRejectsForeignThread` | Evidence-bearing cross-thread mutation remains rejected and atomic. |
| `R1-B02` | `launcherCreatedConstructionSupportsScheduledPreviewOnEdt` | Desktop startup-to-EDT scheduled preview remains transient. |
| `R1-C03` | `launcherCreatedConstructionSupportsDefinitiveEdtCreation` | Definitive EDT creation publishes a native durable Locus V2. |

These six cases are intentionally outside the historical `G9U0*` class
wildcards. The author-approved G9U0 suite remains a separate 93/93 authority:
81 shared + 12 Desktop. The combined executed inventory will therefore be 99,
but the historical evidence is never relabelled as 99.

Atomic rollback beyond the new preview snapshots remains covered by the sealed
G9U0 persistence/lifecycle cases and the G9A1/G9A3 authorities. Feature default,
GeoCeDG Classic and legacy compatibility remain covered by G9U0 F01-F04,
C01, M10 and P01/P05/P12-P15, together with the six-case
`LegacyLocusCharacterizationTest`. The corrective tests do not claim these as
new cases.

## Validation state

| Gate | Result | Evidence location |
|---|---|---|
| Pre-fix interactive evidence | OBSERVED | Author-provided `GEO_NOT_SERIALIZABLE` and `instrumentation is thread-confined` traces |
| Historical verifier on descendant `HEAD` | PRE-FIX FAILURE OBSERVED / OPERATIONAL CORRECTION PRESENT | Published G9U0 tag and promotion anchors |
| R1 focused 6/6 | PASSED: 4 shared + 2 Desktop, zero failures/errors/skips | `artifacts/g9u0-r1/candidate/focused` |
| Historical G9U0 93/93 | PASSED: 81 shared + 12 Desktop | `artifacts/g9u0-r1/candidate/historical-g9u0-final` |
| Relevant G9A lifecycle/identity | PASSED: 27/27 | Module JUnit XML and composed authority |
| Legacy locus | PASSED: 6/6 | Module JUnit XML and composed authority |
| Deterministic focused rerun | PASSED: 6/6, same partition and zero outcomes | `artifacts/g9u0-r1/candidate/focused-deterministic` |
| Checkstyle and static R1 verifier | PASSED | Focused logs and `-SkipBuild` probe |
| `git diff --check` and cached check | PASSED | Final candidate audit |
| Complete `tools/agent/verify.ps1` | PASSED: exit 0; `All GeoCeDG verification gates passed.` | `artifacts/g9u0-r1/candidate/composed` |
| Manual author smoke test | PASSED | Circle-constrained `C`, `D` on `yAxis`, `E = Midpoint(C,D)`, `LocusV2(E,C)` and dynamic manipulation; no false CAS error |

All saved validation commands completed successfully. The author then manually
reproduced the originally failing Desktop construction, manipulated it
dynamically and approved the corrective closeout.

## Compatibility and residual risks

- Legacy `Locus[Q,P]`, `Length[GeoLocus]`, mode/XML behavior and feature
  default-off policy are unchanged.
- Preview performs no durable publication; definitive creation continues
  through the normal registry and construction DAG.
- An evidence-bearing spatial reconciliation that genuinely crosses owner
  threads remains rejected. Supporting general concurrent construction
  mutation would require a broader ownership policy and is outside R1.
- Canonical-constant exclusion uses the construction's typed constant-element
  authority; it does not infer identity from labels or coordinates.
- The separate G9X1 candidate also modifies `tools/agent/verify.ps1` and
  `docs/upstream/modified-files.yml`. Its replay/reconciliation is a separate
  transaction and does not form part of R1.
- The separately reported file-save concern is not investigated in this task.

```text
G9U0-R1 = PASS — AUTHOR APPROVED

selfApproved = false
authorApproved = true
passClaimed = true

G9X1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
G9X1 candidate = FROZEN / UNCHANGED

selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

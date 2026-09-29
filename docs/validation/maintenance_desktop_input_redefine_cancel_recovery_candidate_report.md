# Maintenance — Desktop Input recovery after a cancelled spatial redefine: candidate report

```text
TRACK                     = maintenance/desktop-input-redefine-cancel-recovery
DEBT                      = TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozen at track start)
PLANNED_ACCEPTANCE        = FINAL
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This track is independent of every phase. It is based on the published
`PRE-G9B-R3-X1` closeout `f96373aca68e3d8efa5316355f75eb1709ddceda` (tree
`24f8fb1d492c92c1518234da6a995839683c9fc3`) and shares no commit with the sibling
historical-suite retirement track. It reuses no X1 evidence, receipt or
reconciliation.

## 1. Defect

After an explicit Input submission in the GeoCeDG product, a spatial redefine that
the user cancels (durable-contract change or legacy replacement answered with
Cancel), or that is unavailable (the redefine dialog explains why it cannot run),
leaves the Desktop Input permanently ignoring Enter: the text stays in the field
and no later input is processed until the application restarts.

**Reproduction on the exact base.** The new regression class, run against the
unmodified base, fails in both defect cases at the "next Enter creates an object"
assertion, after every earlier assertion (dialog presented, construction XML
unchanged, text kept) passed. An earlier scratch probe reproduced the same state on
the author's retained `test2.cedg` and identically on the published base
`78134aa8…`; ordinary parser errors, unknown commands, wrong arguments and a
cancelled slider prompt recover normally.

**Root cause.**

1. `AlgebraInputD.onEnterPressed` (GeoCeDG product branch, added at `b49219408`)
   sets `productSubmissionPending` before `GeoCeDGAlgebraInputSubmission.submit` and
   ignores every explicit Enter while it is set. It was cleared only by the result
   callback, a non-null error message or an exception.
2. When the redefine frontend returns no execution mode (Cancel or unavailable),
   `AlgebraProcessor` throws `SpatialRedefineCancelledException` and
   `processValidExpression` consumes it as a normal cancellation (added at
   `a53c1c992`): no error, no result; the redefine callback forwards only non-null
   results. This kernel behaviour is correct and is asserted by the existing
   `PreG9bR3RedefineFrontendTest#aCancelledContractChangeShowsTheReasonAndTheCompleteImpact`
   (no output, no error, construction unchanged).

Causality: pre-existing before `PRE-G9B-R3-X1`; X1 changed neither file.

## 2. Design

**Submission invariant.** Every accepted explicit Enter reaches exactly one
terminal state — success, error, cancelled, or unavailable/no result — and every
terminal state releases the Input submission guard.

**Placement.** Desktop frontend. Cancellation is legitimate kernel behaviour; the
defect is the Desktop submission lifecycle remaining pending. Desktop product
submissions complete synchronously: command processing, the slider prompt and the
redefine dialogs are modal and return before `submit` returns. The guard therefore
only has to cover the dynamic extent of one submission (its nested modal dialogs,
where focus loss or a repeated Enter must not submit again). The fix releases the
guard when `submit` returns, in addition to the existing release points. For a
cancelled or unavailable redefine nothing else happens: the dialog has already
explained the outcome, the construction is unchanged and the text stays in the
field for editing. No error is synthesized and no identity or redefine-assessment
semantics change.

**Rejected alternatives.**

| Alternative | Reason |
|---|---|
| forward a `null` result from the kernel redefine callback | changes shared upstream `AlgebraProcessor` callback semantics for every caller |
| report cancellation as an error message | converts a user cancellation into a command error |
| a timer or watchdog that clears the guard | time-based and nondeterministic |
| clear the guard in the redefine frontend | couples the dialog layer to the Input controller |

## 3. Changes

| Path | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/inputbar/AlgebraInputD.java` | release `productSubmissionPending` after the synchronous product submission returns (one statement, with its rationale) |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/DesktopInputRedefineCancelRecoveryTest.java` | new regression class (3 tests) |
| `docs/upstream/modified-files.yml` | purpose of the existing `AlgebraInputD` entry extended; the new test file registered (813 registered files; `Assert-GeoCeDGUpstreamBoundary` passes) |
| `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json` | derived catalog data for the new test identities, produced by the official updater, and the canonical pin |
| this report and its JSON mirror | candidate record |

No kernel, command, identity, serialization, scientific, build or verifier change.

## 4. Tests

`DesktopInputRedefineCancelRecoveryTest` drives the real `AlgebraInputD` with an
injected redefine presentation:

- `cancelledContractChangeLeavesTheConstructionAndTheInputUsable`: durable-contract
  change `s=a+1` answered with Cancel; one contract prompt, construction XML
  unchanged, text kept, next Enter creates an object.
- `unavailableRedefineLeavesTheConstructionAndTheInputUsable`: a number used by a
  semantic point redefined as a line; the unavailable explanation is shown,
  construction XML and value unchanged, next Enter creates an object.
- `acceptedContractChangeAndOrdinaryErrorsKeepTheirBehaviour`: controls — Keep
  identity executes the contract update and clears the field; an ordinary redefine
  `k=1` → `k=2` succeeds; a parse error, an unknown command and wrong arguments keep
  the text, create nothing, and the next Enter still works.

Before the fix the first two fail at the next-Enter assertion. With the fix all
three pass, together with the adjacent suites `G9U1AlgebraSubmissionTest` (7, incl.
the cancelled slider prompt), `PreG9bR3RedefineFrontendTest` (7),
`PostG9U1A3FrontendRedefineTest` (8) and `G9U1DefinitionAffordanceTest` (8); Desktop
Checkstyle main and test report no finding.

**Catalog derivation.** The official updater
(`update-verification-junit-inventory.ps1`) ran with the unchanged pinned shared
discovery evidence (`4536e3bb…`), a new `discovery.desktop` dry-run evidence
(`d252ade17f157ba04a93f4681ba1b2f9b787599dcf786b82421db8a42000316b`) and the
executed `final.desktop` selection evidence (`-SelectionEvidencePath`, 103 JUnit
files, 0 failures). Only the three new identities enter the catalog:

| Value | Base | Candidate |
|---|---|---|
| `discovery.desktop` / module desktop | 1533, `b38fac50…` | 1536, `f76a5fb484a5982224f46d2563e61c9f3a35e782883d0835a79116ea1b810d14` |
| `final.desktop` | 1527, `cc942936…` | 1530, `fcc501dedf627d046ccb18baa95311a1fa48524a358737fddea8dd63a6aeaf5b` |
| inventory canonical hash / registry pin | `b5488fcd…` | `8b9d2719c0d9844351b1b3e546b185a9efa5e9d2b3a68f6b1409dea30049ab04` |
| `FINAL` execution plan | `defe8ab4…` | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` (unchanged) |

The producer evidence is kept locally under the ignored
`artifacts/agent/maintenance-input-inventory/`.

## 5. Verification

The frozen class is `GLOBAL_IMPACT`: the change is in the Input submission path used
by every GeoCeDG product command, the new tests run canonically only in the
`final.desktop` selection, and only `FINAL` issues a receipt. One normal `FINAL` runs
on the committed candidate; its identity and result are reported outside this
artifact because the artifact cannot name its own commit.

## 6. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (maintenance correction of a pre-existing Desktop defect)
SERIALIZATION_FORMAT_IMPACT        = NONE
IDENTITY_SCHEMA_IMPACT             = NONE
MIGRATION_IMPACT                   = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY (new Desktop test identities, registry pin); no verifier change
BOOTSTRAP_IMPACT                   = NO_CHANGE_REQUIRED (no workstation, toolchain, Gradle, Conda, packaging or environment change)
GUIDE_IMPACT                       = NO_CHANGE_REQUIRED (the guides do not describe the stranded state; the documented redefine dialogs are unchanged)
UPSTREAM_BOUNDARY_IMPACT           = existing AlgebraInputD modification extended; one GeoCeDG test file registered; no new upstream file modified (813)
```

## 7. Debt

`TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION` is resolved by this candidate,
pending author approval. No other debt is changed.

## 8. Authorization state

The author authorized implementation and technical verification of this track only.
Author approval, closeout, promotion, tag and release each require a separate
explicit author decision naming the exact candidate.

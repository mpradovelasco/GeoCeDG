# Maintenance — retirement of an orphaned historical verification suite: candidate record

```text
TRACK                     = maintenance/retire-orphaned-historical-verification-suites
OBSERVATION               = OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = OPERATIONAL_VERIFICATION_INFRASTRUCTURE (frozen at track start)
PLANNED_ACCEPTANCE        = focused operational evidence: INFRA_UNIT, STATIC, FINAL -PlanOnly equivalence
FULL_REQUIRED             = false (no global verification infrastructure change)
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This track is independent of every phase. It is based on the published
`PRE-G9B-R3-X1` closeout `f96373aca68e3d8efa5316355f75eb1709ddceda` (tree
`24f8fb1d492c92c1518234da6a995839683c9fc3`) and shares no commit with the sibling
Desktop Input maintenance track.

## 1. Diagnosis (re-verified on the base)

| Fact | Evidence |
|---|---|
| both suites were last changed before the verifier rewrite | `phase-lifecycle.Tests.ps1` at `c976deca6`, `generated-state.tests.ps1` at `9063937a4` |
| they became stale at `4117bdfd5` ("Add typed verification profiles and workstation checks") | `verify.ps1` went from 1686 to 128 lines; the `$BaselineVerifier` and other `*Verifier` declarations, the DEV `-TestFilter $TestFilter` forwarding, the G9U1 gate and declaration and the cleanability-preflight ordering all disappeared; the suite's pinned canonical hash `2cffff70…` no longer matches |
| the same commit unhooked them | `verify-verification-infrastructure.ps1` used to run the `verification-runtime`, `generated-state`, `phase-lifecycle` and `phase-closeout` fixtures; `4117bdfd5` replaced that loop with a delegation to `verify.ps1 -Profile INFRA_UNIT`, whose registered nodes include none of them |
| the sibling was retired explicitly | the sibling phase-closeout test suite was deleted at `596af89a8` ("Remove retired closeout workflow machinery"); `phase-lifecycle.Tests.ps1` was left in place |
| historical behaviour | `phase-lifecycle.Tests.ps1`: 61/61 at `c976deca6` on an LF checkout, 56/61 at `4117bdfd5` (cases 1, 3, 4, 5, 6); `generated-state.tests.ps1`: 19 cases pass at `c976deca6`, fails at `4117bdfd5` ("Cleanability preflight is not ordered before costly gates") |
| on this base `f96373ac…` | unchanged: see §5 |

Case 6 of `phase-lifecycle.Tests.ps1` is additionally line-ending sensitive: its
here-string anchor carries CR on a CRLF checkout, so it fails even at `c976deca6`
on such a checkout (60/61).

## 2. Authority classification

Neither suite is an active authority: no registry node, profile, PHASE selection or
static contract names either file, and no current guide, prompt or README presents
them as live. Every other reference was classified:

| Reference | Class |
|---|---|
| `tools/agent/verify-input-identity-repair.ps1` (runs both suites) | historical phase verifier, not registered; it already fails on the base because it requires both suites to pass |
| `tools/agent/verify-g9s1-r1-spline-pair-materialization.ps1` (lists `phase-lifecycle.Tests.ps1` among its lifecycle authority paths) | historical phase verifier, not registered |
| `tools/agent/phase-lifecycle.ps1` (expects a `phase-lifecycle` entry in a recorded input-identity summary) | validates historical evidence documents, not the file; retained unchanged |
| `geocedg/validation/**` policies and evidence manifests, `docs/validation/**` reports | immutable historical evidence |
| **`tools/agent/tests/verification-runtime.Tests.ps1`** (resolves, parses and hashes `generated-state.tests.ps1` in two cases) | **current passing suite outside this retirement** |

## 3. Disposition

- **`tools/agent/tests/phase-lifecycle.Tests.ps1` — RETIRED AS HISTORICAL (deleted).**
  It asserts structural properties of the superseded pre-registry verifier;
  re-pinning it to the current `verify.ps1` would falsely present a different
  verification architecture as a continuation of the old one, and leaving it
  permanently red and unregistered creates misleading maintenance cost. Its results
  remain reproducible from its exact historical commits
  (`git show c976deca6:tools/agent/tests/phase-lifecycle.Tests.ps1`).
- **`tools/agent/tests/generated-state.tests.ps1` — RETAINED, not eligible in this
  track.** It is equally stale, but the retained and passing
  `verification-runtime.Tests.ps1` depends on the file: deleting it would turn that
  suite red, and adapting it would modify a suite this track must not change.
  Its disposition needs a separate author decision: retire it together with a
  bounded update of those two `verification-runtime` cases, or keep it as a
  documented orphan. `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` stays open for
  this file only.
- **`tools/agent/tests/verification-runtime.Tests.ps1`** — unchanged.

## 4. Historical evidence preservation

No historical record, policy, evidence manifest, hash, report or commit is edited.
The historical references of §2 keep their meaning: they bind the file's content at
their own commits, which Git preserves. No historical run is claimed to have
executed different files.

## 5. Verification

Before deletion, on the base: see the table below. After deletion, on the committed
candidate: the registered `INFRA_UNIT` and `STATIC` profiles and `FINAL -PlanOnly`,
whose identities are reported outside this artifact because it cannot name its own
commit. The first commit of this track, `3c39255c6ac35df5fc2fcc3593536dfe88b4b473`,
named the retired sibling suite by its file name in this record and its JSON
mirror; its `INFRA_UNIT` run `verification-994888e59f0b470cb805f87794ff55f7` is
therefore `REJECTED_VERIFICATION_CORE` (`infra.closeout-compatibility`: retired
closeout names must remain only in the historical allowlist). That result stays
historical evidence for that commit; the corrective descendant only rewords the
two mentions. The frozen class requires no `FULL`: no registry node, execution plan,
verifier module, schema or check changes, and the deleted file is no registered
leaf; a `FULL` above the frozen plan would need a
`VERIFICATION_ESCALATION_REQUEST`.

| Suite on the base | Result |
|---|---|
| `phase-lifecycle.Tests.ps1` | exit 1; 56/61, failures 1, 3, 4, 5, 6 (identical to `4117bdfd5`) |
| `generated-state.tests.ps1` | exit 1; "Cleanability preflight is not ordered before costly gates" (identical to `4117bdfd5`) |
| `verification-runtime.Tests.ps1` | exit 0; 119/119 |

## 6. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = one unregistered historical test script removed; no registry, plan, verifier module, schema or check change
BOOTSTRAP_IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = NO_CHANGE_REQUIRED (no guide presents the suite as live)
UPSTREAM_BOUNDARY_IMPACT           = NONE (tools/agent is GeoCeDG-owned)
```

## 7. Authorization state

The author authorized implementation and technical verification of this track only.
Author approval, closeout, promotion, tag and release each require a separate
explicit author decision naming the exact candidate.

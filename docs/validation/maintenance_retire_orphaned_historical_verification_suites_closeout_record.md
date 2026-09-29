# Maintenance — retirement of an orphaned historical verification suite: closeout record

```text
HISTORICAL-SUITE MAINTENANCE = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
```

This record preserves the explicit author dispositions of 2026-09-29 on the
historical-suite maintenance track and the exact evidence identities behind them. It
changes no product, Desktop, test, build, verifier, inventory, registry, schema,
task, filter, tolerance, reference, toolchain or acceptance-semantics file, and it
does not modify the approved replay candidate or the approved source candidate.
Besides this record and its machine-readable mirror, it only updates the roadmap
status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate record](maintenance_retire_orphaned_historical_verification_suites_record.md)
and its evidence mirror record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole authority
for current author approval. Every statement in those artifacts stands unchanged.

## Author dispositions

```text
HISTORICAL-SUITE MAINTENANCE = PASS — AUTHOR APPROVED

APPROVED_REPLAY_CANDIDATE = 2035131604bcad88207947070afae3815b1339b0   (M_HIST_REPLAY)
APPROVED_TREE             = d37a11ccb697676bac1efa03a0156ff345107699
SOURCE_APPROVED_CANDIDATE = 2f1b091e1427e99c777288015cef43bcfc7face7   (M_HIST)
SOURCE_TREE               = 6f0ae84194b6e8601ddcb7b7972083df9236221e
AUTHOR_APPROVAL           = APPROVED
selfApproved              = false
```

```text
tools/agent/tests/phase-lifecycle.Tests.ps1      = RETIRED AS HISTORICAL
tools/agent/tests/generated-state.tests.ps1      = RETAINED PENDING SEPARATE MAINTENANCE DESIGN
tools/agent/tests/verification-runtime.Tests.ps1 = UNCHANGED
```

The author first approved the exact source candidate `M_HIST`, a sibling of the Input
maintenance closeout. That approval did not extend to a new Git identity. The author
then separately approved the exact replay `M_HIST_REPLAY`, on the basis of the proven
net-delta equivalence below. The approval extends to no other descendant except this
status-only closeout.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| source base `P_X1` | `f96373aca68e3d8efa5316355f75eb1709ddceda` | `24f8fb1d492c92c1518234da6a995839683c9fc3` | `a624e4ad…` |
| superseded intermediate | `3c39255c6ac35df5fc2fcc3593536dfe88b4b473` | — | `P_X1` |
| source candidate `M_HIST` | `2f1b091e1427e99c777288015cef43bcfc7face7` | `6f0ae84194b6e8601ddcb7b7972083df9236221e` | `3c39255c…` |
| replay base `P_INPUT` | `107a98474fcf1ccc98aef282938c6b7fb70460c7` | `3afde754b19a96b15fb32ed456ba6f8279af765f` | `a6f5c5fc…` |
| replay candidate `M_HIST_REPLAY` | `2035131604bcad88207947070afae3815b1339b0` | `d37a11ccb697676bac1efa03a0156ff345107699` | `P_INPUT` |

Branches: `maintenance/retire-orphaned-historical-verification-suites` (source,
retained for provenance, not published) and
`maintenance/retire-orphaned-historical-verification-suites-replay` (replay).

**Historical fact, unchanged.** The intermediate commit `3c39255c…` is a superseded
candidate. Its INFRA_UNIT run `verification-994888e59f0b470cb805f87794ff55f7` is
`REJECTED_VERIFICATION_CORE` (a retired closeout name outside its historical
allowlist) and stays historical evidence for that commit. It was corrected by the
wording-only descendant `M_HIST` and is not part of the replay.

## Accepted content

The net delta of `M_HIST` relative to `P_X1`, replayed as one commit on `P_INPUT`:

| Change | Path |
|---|---|
| deleted | `tools/agent/tests/phase-lifecycle.Tests.ps1` |
| added | `docs/validation/maintenance_retire_orphaned_historical_verification_suites_record.md` |
| added | `geocedg/validation/maintenance-historical-suites/maintenance-historical-suites-candidate-evidence.json` |

The retired suite was no registered verification node; its results stay reproducible
at `c976deca6`. No historical record, policy, manifest, hash or commit is edited.

## Accepted evidence

**Source candidate `M_HIST`:** INFRA_UNIT `verification-515d3d40a03040b6ac39b307cbf048ae`
`ACCEPTED / COMPLETE` 21/21; STATIC `verification-65267b0b4a6741ccb325aa8ee65dae89`
`ACCEPTED / COMPLETE`; `FINAL -PlanOnly` byte-identical to `P_X1` (67 nodes).

**Replay equivalence: PROVEN.**

- the path set changed by `P_INPUT..M_HIST_REPLAY` equals the net path set of
  `P_X1..M_HIST`, and it is disjoint from `P_X1..P_INPUT`;
- both added files have identical blobs and modes (`12b13504…`, `aeae8cf0…`, `100644`);
  the deleted path is absent in both;
- every other path (11 674) is inherited unchanged from `P_INPUT`; outside the Input
  delta the replay tree equals `M_HIST`;
- `git merge-tree --write-tree --merge-base P_X1 P_INPUT M_HIST` reproduces the
  replay tree `d37a11cc…` exactly;
- `generated-state.tests.ps1` (`9929e2b0…`) and `verification-runtime.Tests.ps1`
  (`e67d041d…`) are identical on `P_X1`, `P_INPUT`, `M_HIST` and the replay;
- the replay contains neither `M_HIST` nor `3c39255c…` as ancestor.

**Replay verification** on exact `M_HIST_REPLAY`, under the frozen
`VERIFICATION_CLASS = OPERATIONAL_VERIFICATION_INFRASTRUCTURE` (the Input changes of
the new base add no obligation; no escalation request; no `FULL`):

| Check | Result |
|---|---|
| INFRA_UNIT | `verification-777ed3c141b44c2985d38bda10ca46e1`, `ACCEPTED / COMPLETE`, 21/21 (including `infra.closeout-compatibility` and `infra.registry`); result `45890814dbf126310af009625253e16aa09836756789eca7811035585a66ea24` |
| STATIC | `verification-1b0571a755d24eb283a653d052040cb0`, `ACCEPTED / COMPLETE`; result `63847e9d10b2bfb204422b62c42b48562676cb47c9f40308243c8c95496d2bac`; standing `diagnostic.governance` finding and `diagnostic.historical-consistency` unavailable, as on `M_HIST` |
| `FINAL -PlanOnly` | byte-identical to `P_INPUT`, 67 nodes, plan `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0`: no active node lost |
| `git diff --check` | clean |

This class issues no `FINAL` receipt, so there is no receipt identity to inspect. The
run evidence, the equivalence proof and the replay review record are kept locally in
the ignored evidence area (`artifacts/agent/maintenance-hist-replay/`,
`artifacts/agent/maintenance-hist-replay-infra_unit/`,
`artifacts/agent/maintenance-hist-replay-static/`). No further verification is run
for this status-only closeout beyond its static checks.

## Debt disposition

| Identifier | State |
|---|---|
| `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` | **PARTIALLY RESOLVED — AUTHOR APPROVED**: `phase-lifecycle.Tests.ps1` retired as historical; `generated-state.tests.ps1` retained pending a separate maintenance design, because `verification-runtime.Tests.ps1` (unchanged) resolves, parses and hashes it |

## Retained debt and observations

| Identifier | Owner / state |
|---|---|
| `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` (remainder) | `generated-state.tests.ps1` stale since `4117bdfd5`; `DISPOSITION = RETAINED PENDING SEPARATE MAINTENANCE DESIGN`; no implementation authorized |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observation, retained |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observation, retained |

`TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION` is `RESOLVED — AUTHOR APPROVED`
([Input maintenance closeout](maintenance_desktop_input_redefine_cancel_recovery_closeout_record.md)).

## Promotion

The author authorizes promotion of the linear history
`107a9847… → 20351316… → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only: no merge, rebase, squash, amend, force push, tag or
release. Promotion proceeds only if local `main`, `origin/main` and the live remote
`main` still equal the base below immediately before it.

Remote identity before promotion:

```text
local main       = 107a98474fcf1ccc98aef282938c6b7fb70460c7
origin/main      = 107a98474fcf1ccc98aef282938c6b7fb70460c7
live remote main = 107a98474fcf1ccc98aef282938c6b7fb70460c7
tree             = 3afde754b19a96b15fb32ed456ba6f8279af765f
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branches are retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion. No work on
`generated-state.tests.ps1` or `verification-runtime.Tests.ps1` is authorized.
`PRE-G9B-R4` and every later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and productive
`G10` remain unauthorized. No tag or release is authorized.

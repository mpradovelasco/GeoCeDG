# Maintenance — generated-state historical verification residue: closeout record

```text
GENERATED-STATE MAINTENANCE = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-09-30 on the
generated-state maintenance candidate and the exact evidence identities behind it. It changes no
product, test, build, verifier, inventory, registry, schema, toolchain,
serialization or acceptance-semantics file, and it does not modify the approved
technical candidate. Besides this record and its machine-readable mirror, it only
updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate report](maintenance_final_generated_state_historical_retirement_candidate_report.md)
and its evidence mirror record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole authority
for current author approval. Every statement in those artifacts stands unchanged.

## Author disposition

```text
GENERATED-STATE MAINTENANCE = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = d8a63d49cbe1cf47071639eac2e8abffd44d9537   (M_GSTATE)
APPROVED_TREE                = ed0be0e7fc1d73ddc32fefa635841fa99537eb54
FINAL                        = verification-8fc55499eb4d4d50b06e57c43275077c,
                               ACCEPTED / COMPLETE, 41/41
FINAL_RECEIPT                = 69feae5c4e0d07c3e2b24c4735183b83ec3e03e5441dba36cef7d53ed87893fb
AUTHOR_APPROVAL              = APPROVED
selfApproved                 = false
```

```text
tools/agent/tests/phase-lifecycle.Tests.ps1      = RETIRED AS HISTORICAL (earlier approved maintenance)
tools/agent/tests/generated-state.tests.ps1      = RETIRED AS HISTORICAL (M_GSTATE)
tools/agent/repository-generated-state.ps1       = LIVE; active coverage via infra.repository-generated-state
tools/agent/tests/verification-runtime.Tests.ps1 = unregistered/historical; no dependency on generated-state.tests.ps1
```

The approval applies to the exact candidate above and extends to no other descendant
except this status-only closeout. It does not authorize `PRE-G9B-R4`, a broader audit
or retirement of the other unregistered suites, a redesign of
`verification-runtime.Tests.ps1`, product or scientific changes, or a tag or release.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_HIST` | `387084422106e66540a3e3400b8b65677087ecc2` | `0fed799f4f704387dcfe96f8fcf3eb5d94d33991` | `20351316…` |
| technical candidate `M_GSTATE` | `d8a63d49cbe1cf47071639eac2e8abffd44d9537` | `ed0be0e7fc1d73ddc32fefa635841fa99537eb54` | `P_HIST` |

Branch: `maintenance/final-generated-state-historical-retirement`. The candidate's
artifacts are the
[candidate report](maintenance_final_generated_state_historical_retirement_candidate_report.md)
and
[`maintenance-generated-state-candidate-evidence.json`](../../geocedg/validation/maintenance-generated-state/maintenance-generated-state-candidate-evidence.json).

## Accepted content

The live safety contract of `tools/agent/repository-generated-state.ps1` (unchanged)
moves from the unregistered, stale `generated-state.tests.ps1` to the focused suite
`tools/agent/tests/repository-generated-state.Tests.ps1`, registered as the active
`INFRA_UNIT`/`FINAL` leaf `infra.repository-generated-state` (`WINDOWS`, impact path the
helper). The 19 old cases migrate; the assertion that searched the superseded
`verify.ps1` for its old gate order is dropped, not re-pinned; the old fixture's own
summary publication and its refusal guard retire with it; one real-Git enumeration case
is added. `verification-runtime.Tests.ps1` loses its coupling to the retired file and
stays unregistered.

**Historical facts, unchanged.** `generated-state.tests.ps1` passed 19/19 at
`c976deca6` and became stale from `4117bdfd5`, when `verify.ps1` became the
registry/profile dispatcher and the structural anchors of one assertion disappeared.
It remains reproducible from history.

## Accepted evidence

| Field | Value |
|---|---|
| verification class | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, global infrastructure change (active registry leaf added); one normal `FINAL` |
| STATIC | `verification-93d7185b5ff14fc89da8eb00f39bddb9`, `ACCEPTED / COMPLETE` |
| INFRA_UNIT | `verification-9091b429183d4057a3a83882b89c9d54`, `ACCEPTED / COMPLETE`, 22/22 |
| `FINAL` run | `verification-8fc55499eb4d4d50b06e57c43275077c`, `ACCEPTED / COMPLETE`, 41/41 acceptance contracts, 17/17 producers exit 0 |
| result | hash `7d67ef90076ad052389fbb7858e1881898a8eae18041a3b95041b205987bde8f`, file SHA-256 `6a339f81c6e0e3dc9922e632ff27b560871f0009bf345a7f00e38c8cac07f2b3` |
| plan | 68 nodes, `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517` (base: 67, `defe8ab4…`; exactly `infra.repository-generated-state` added) |
| receipt | `69feae5c4e0d07c3e2b24c4735183b83ec3e03e5441dba36cef7d53ed87893fb`, bound to exact `M_GSTATE` commit and tree; file SHA-256 `7bc504f27f35f8fb7147624dca218a0d93c1662c86979f220aeda740ad46f489` |
| receipt identities | checker `2285db30a0b3802f7b8c0711e05b67e4c09129b9c800d08522d0804afe810ab5`, input `e80f747b0b18f40bf9011ee741c364d2056b3b38aa8d9581def06d5f5e32dcde`, environment `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0` |

JUnit evidence: `final.shared` 6 842 tests and `final.desktop` 1 530 tests, 0 failures
or errors. Diagnostics, separate from acceptance: eight `DIAGNOSTIC_CLEAR`;
`diagnostic.governance` `DIAGNOSTIC_FINDING` and `diagnostic.historical-consistency`
`DIAGNOSTIC_UNAVAILABLE`, the standing pre-existing diagnostics. No evidence of
`PRE-G9B-R3-X1`, of the post-run catalog reconciliation or of the earlier maintenance
candidates is reused.

**Closeout inspection.** `tools/agent/phase-closeout.ps1 -Action INSPECT
-CandidateCommit d8a63d49… -ReceiptPath <receipt> -ApprovedCommit d8a63d49…` returned
exit 0, `operation = IDENTITY_ONLY_CLOSEOUT`, `identity_confirmed = true`,
`mismatches = []`, `verification_executed = false`, `repository_mutated = false`. The run
evidence and receipt are kept locally in the ignored evidence area
(`artifacts/agent/maintenance-gstate-final/`). No further verification is run for this
status-only closeout beyond its static checks.

## Debt disposition

| Identifier | State |
|---|---|
| `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` | **RESOLVED — AUTHOR APPROVED**: `phase-lifecycle.Tests.ps1` and `generated-state.tests.ps1` retired as historical; the live helper is actively covered |

## Retained debt and observations

| Identifier | Owner / state |
|---|---|
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
Other unregistered historical suites (among them `verification-runtime.Tests.ps1`) are
out of this scope; no audit or retirement of them is authorized.

## Promotion

The author authorizes promotion of the linear history
`387084422106… → d8a63d49… → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only: no merge, rebase, squash, amend, force push, tag or
release. Promotion proceeds only if local `main`, `origin/main` and the live remote
`main` still equal the base below immediately before it.

Remote identity before promotion:

```text
local main       = 387084422106e66540a3e3400b8b65677087ecc2
origin/main      = 387084422106e66540a3e3400b8b65677087ecc2
live remote main = 387084422106e66540a3e3400b8b65677087ecc2
tree             = 0fed799f4f704387dcfe96f8fcf3eb5d94d33991
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R4` remains
`DESIGNED — NOT AUTHORIZED`; it requires a separate explicit author authorization from
the exact published base. Every later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and
productive `G10` remain unauthorized. No tag or release is authorized.

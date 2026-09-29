# Maintenance — Desktop Input recovery after a cancelled spatial redefine: closeout record

```text
INPUT MAINTENANCE = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
newHeavyFinal             = NONE
```

This record preserves the explicit author disposition of 2026-09-29 on the Desktop
Input maintenance candidate and the exact evidence identities behind it. It changes
no product, test, build, verifier, inventory, registry pin, schema, toolchain,
serialization or acceptance-semantics file, and it does not modify the approved
technical candidate. Besides this record and its machine-readable mirror, it only
updates the roadmap status statements.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the [candidate report](maintenance_desktop_input_redefine_cancel_recovery_candidate_report.md)
and its evidence mirror record only frozen technical state and
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole authority
for current author approval. Every statement in those artifacts stands unchanged.

## Author disposition

```text
INPUT MAINTENANCE = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = a6f5c5fc21e8975794e483ed311abb241b8f9529   (M_INPUT)
APPROVED_TREE                = d01cd01e4cc3cd5965a2413ec94c7735b08d5115
FINAL                        = verification-9cce80aa09864ae886687ef88c794ee4,
                               ACCEPTED / COMPLETE, 40/40
FINAL_RECEIPT                = e633c952da9814f455ae73ff0e34d544e18bffe2ab7d360d2148fb06e7c5eaec
AUTHOR_APPROVAL              = APPROVED
selfApproved                 = false
```

The fix is accepted as a Desktop-layer lifecycle repair. The approval applies to the
exact candidate above and extends to no other descendant except this status-only
closeout.

## Approved identities

| Role | Commit | Tree | Parent |
|---|---|---|---|
| published base `P_X1` | `f96373aca68e3d8efa5316355f75eb1709ddceda` | `24f8fb1d492c92c1518234da6a995839683c9fc3` | `a624e4ad…` |
| technical candidate `M_INPUT` | `a6f5c5fc21e8975794e483ed311abb241b8f9529` | `d01cd01e4cc3cd5965a2413ec94c7735b08d5115` | `P_X1` |

Branch: `maintenance/desktop-input-redefine-cancel-recovery`. The candidate's
artifacts are the
[candidate report](maintenance_desktop_input_redefine_cancel_recovery_candidate_report.md)
and
[`maintenance-desktop-input-candidate-evidence.json`](../../geocedg/validation/maintenance-desktop-input/maintenance-desktop-input-candidate-evidence.json).

## Accepted content

After a spatial redefine that the user cancels, or that is unavailable, the Desktop
Input no longer ignores every later Enter. `AlgebraInputD` releases its submission
guard when the synchronous product submission returns, in addition to the existing
release points. Every accepted explicit Enter reaches exactly one terminal state —
success, error, cancelled, or unavailable/no result — and every terminal state
releases the guard. No error is synthesized, the text stays editable, and kernel,
identity, redefine-assessment and serialization semantics are unchanged.

## Accepted evidence

| Field | Value |
|---|---|
| verification class | `GLOBAL_IMPACT`, frozen at track start; one normal `FINAL` |
| `FINAL` run | `verification-9cce80aa09864ae886687ef88c794ee4`, `ACCEPTED / COMPLETE`, 40/40 acceptance contracts satisfied, 17/17 producers completed with exit 0 |
| result | hash `0401e4a42327b42fb812f19760bbdbc96ab99e97a0007b39e32575fa114f899b`, file SHA-256 `1178cc4fcf948f9804e2d89e42786143d494d46dd55e66118558a54bf6d7665c` |
| plan | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| receipt | `e633c952da9814f455ae73ff0e34d544e18bffe2ab7d360d2148fb06e7c5eaec`, `GEOCEDG_ACCEPTANCE_RECEIPT`, bound to exact `M_INPUT` commit and tree; file SHA-256 `104c8fe05d7a01ee57f4ff7f987ce7475b89f6c0a3a48302a35c3f6d7110bbcf` |
| receipt identities | checker `71f0a8725924b5388201106e2dde76b46000883f7376ed72d8734875bf94f68a`, input `b10b895a2deb9c27278844bdd786458216e0c4ca197341741052c2467d53d86d`, environment `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0` |

JUnit evidence: `final.desktop` 1 530 tests, 0 failures or errors, 1 skip, including
the 3 new `DesktopInputRedefineCancelRecoveryTest` cases. Diagnostics, separate from
acceptance: eight `DIAGNOSTIC_CLEAR`; `diagnostic.governance` `DIAGNOSTIC_FINDING`
and `diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`, the standing
pre-existing diagnostics. No `PRE-G9B-R3-X1` evidence, receipt or post-run
reconciliation is reused.

**Closeout inspection.** `tools/agent/phase-closeout.ps1 -Action INSPECT
-CandidateCommit a6f5c5fc… -ReceiptPath <receipt> -ApprovedCommit a6f5c5fc…`
returned exit 0, `operation = IDENTITY_ONLY_CLOSEOUT`, `identity_confirmed = true`,
`mismatches = []`, `verification_executed = false`, `repository_mutated = false`.
The run evidence and receipt are kept locally in the ignored evidence area
(`artifacts/agent/maintenance-input-final/`). No further verification is run for
this status-only closeout.

## Debt disposition

| Identifier | State |
|---|---|
| `TD-DESKTOP-INPUT-REDEFINE-CANCEL-STRANDS-SUBMISSION` | **RESOLVED — AUTHOR APPROVED** by the approved candidate |

## Retained debt and observations

| Identifier | Owner / state |
|---|---|
| `OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES` | owner: maintenance track `maintenance/retire-orphaned-historical-verification-suites`; its source candidate `2f1b091e1427e99c777288015cef43bcfc7face7` (tree `6f0ae84194b6e8601ddcb7b7972083df9236221e`) is author-approved (2026-09-29): `tools/agent/tests/phase-lifecycle.Tests.ps1` retired as historical; `tools/agent/tests/generated-state.tests.ps1` retained pending a separate maintenance design, because `tools/agent/tests/verification-runtime.Tests.ps1` (unchanged) depends on it. The source candidate is a sibling of this closeout; its publication requires a linear replay of its net delta and a separate author approval of the exact replay commit |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |
| `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS` | observation, retained |
| `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER` | observation, retained |

## Promotion

The author authorizes promotion of the linear history
`f96373ac… → a6f5c5fc… → this closeout commit` to `main`, by ordinary clean
non-force fast-forward only: no merge, rebase, squash, amend, force push, tag or
release. Promotion proceeds only if local `main`, `origin/main` and the live remote
`main` still equal the base below immediately before it.

Remote identity before promotion:

```text
local main       = f96373aca68e3d8efa5316355f75eb1709ddceda
origin/main      = f96373aca68e3d8efa5316355f75eb1709ddceda
live remote main = f96373aca68e3d8efa5316355f75eb1709ddceda
tree             = 24f8fb1d492c92c1518234da6a995839683c9fc3
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch is retained.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R4` and every
later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and productive `G10` remain
unauthorized. No work on `generated-state.tests.ps1` is authorized. No tag or
release is authorized.

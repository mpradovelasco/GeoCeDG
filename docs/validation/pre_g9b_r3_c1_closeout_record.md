# PRE-G9B-R3-C1 closeout record

```text
PRE-G9B-R3-C1 = PASS — AUTHOR APPROVED
```

```text
selfApproved              = false
authorApproved            = true
passClaimed               = true

technicalCandidateFrozen  = true
candidateMutated          = false
```

This record preserves an explicit author disposition of 2026-09-29 and the exact
evidence identities behind it. It changes no product, test, build, verifier,
inventory, registry pin, schema, serialization or scientific-contract file, and
it does not modify the approved technical candidate. Besides this record and its
machine-readable mirror, it only updates the `PRE-G9B-R3-C1` status statements of
the roadmap and registers the planned successor `PRE-G9B-R3-C1-U1`.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's own artifacts record only `TECHNICAL_CANDIDATE_STATE = FROZEN`
and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole
authority for current author approval. Every statement in the candidate's
artifacts stands unchanged.

## Author disposition

```text
PRE-G9B-R3-C1 = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = aeb40e16a334cdd6eaa52b8182c71e9743f715fe
APPROVED_TREE                = 0776268095f385606f22664420d082d6d945fb98
AUTHOR_SMOKE                 = PASS
FINAL                        = verification-1c5cc16a60db4937a1be29ae69cd4749, ACCEPTED / COMPLETE
FINAL_RECEIPT                = b078e1b878d91d3e8f11263c3e675b9b9ad34149371cd5345ea36a7b2c099da9
```

## Approved technical candidate

```text
commit = aeb40e16a334cdd6eaa52b8182c71e9743f715fe
tree   = 0776268095f385606f22664420d082d6d945fb98
parent = 89d481b1b3eef36c429f920178d5e5de6c0149e3
base   = 89d481b1b3eef36c429f920178d5e5de6c0149e3
branch = feature/pre-g9b-r3-c1-clipboard-correctness
```

The candidate is preserved unchanged. Its artifacts are the
[candidate report](pre_g9b_r3_c1_candidate_report.md), the
[validation matrix](pre_g9b_r3_c1_validation_matrix.md) and the
[design record](../architecture/pre_g9b_r3_c1_clipboard_atomicity_and_closure_design.md).

## Author smoke

```text
AUTHOR_SMOKE = PASS
```

Author-provided, on the real clipboard with the C1 witness: copy only the semantic
locus participant; paste; the reconstructed predecessors remain dependent; axis
references remain ordinary host references; undo and redo behave correctly; native
save and reopen preserve the copied construction.

## Accepted content

The approval covers the bounded C1 repair only.

**C1-A, atomic clipboard rollback:**
- rollback uses the approved spatial runtime rollback protocol and no runtime guard
  is weakened;
- the complete pre-operation authoritative state is restored on failure;
- the previous `blockUpdateScripts` value is restored exactly, whether `true` or
  `false`, and no persistent `<scripting blocked="true">` remains;
- kernel loading mode, view notification and command lookup return to their prior
  states;
- the original failure stays represented in the exception chain if the rollback
  itself fails;
- a failed operation creates no semantic mutation and no undo action.

**C1-B, identity-bearing predecessor closure:**
- identity-bearing payloads include every parent algorithm needed to rebuild the
  copied semantic closure;
- global axes and constants stay ordinary host references and receive no durable
  CeDG identity;
- copied participants get fresh identities and explicit copy lineage and remap;
- dependent geometry stays dependent;
- an identity-bearing payload that cannot be rebuilt completely fails closed as
  `INCOMPLETE_CLOSURE`;
- silent conversion of a dependent CeDG geo into a free geo is forbidden.

## Accepted evidence

The author accepts the single canonical `FINAL` run on the exact approved
candidate. It is not rerun.

| Field | Value |
|---|---|
| command | `tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-c1-final` |
| run | `verification-1c5cc16a60db4937a1be29ae69cd4749` |
| verdict | `ACCEPTED / COMPLETE`; 40/40 acceptance checks satisfied, 0 violated, 0 untrusted, 0 not run; exit 0 |
| plan hash | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| result hash | `98756d98634818ca9df4a3ac3c91b46a396aec13fb5a04f6602895f9d14c457f` |
| result file SHA-256 | `b03761160d08b03860122ed4ce83b643d4a9fd57cac7fd748629459051419491` |
| receipt id | `b078e1b878d91d3e8f11263c3e675b9b9ad34149371cd5345ea36a7b2c099da9` |
| receipt file SHA-256 | `d90b93516eb26c5c23c0a523f32db5a75819d244e4d6c5f6876b1ecd102851db` |
| receipt binding | `candidate_commit = aeb40e16a334cdd6eaa52b8182c71e9743f715fe`, `candidate_tree = 0776268095f385606f22664420d082d6d945fb98`, `base_commit = null` |
| checker / input identity | `941bfa4dce6f3fb5c978209b94df347c77af59d47e1e513fec646a271207a49d` / `ac44e1130f0669691a086b36795927037cf032532826012ce789e4de753b5188` |
| environment | signature `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0`, observation `a329dd0ec835b3c3d7cc0ec5561cb636ebe9552c9e2288b82b75c2fce4d1c4ba` |

JUnit evidence of the run: `final.shared` 6 796 tests, 0 failures or errors, 10
allowlisted skips; `final.desktop` 1 525 tests, 0 failures, 1 skip; `g9u1-isolated`
5 and `revision3-diagnostic` 1, both without failures. The 9 shared and 2 Desktop
C1 focal tests are in the evidence. All 11 classes of the `post-g9u1-a3.narrow`
selection are in the shared evidence (114 tests, 0 failed; pin `9a45556c…`).

Diagnostics, separate from acceptance: eight `DIAGNOSTIC_CLEAR`;
`diagnostic.governance` `DIAGNOSTIC_FINDING` and `diagnostic.historical-consistency`
`DIAGNOSTIC_UNAVAILABLE`, both identical in outcome and cause to the previous
accepted `FINAL`.

The identity-only inspection `tools/agent/phase-closeout.ps1 -Action INSPECT` with
this receipt and `-ApprovedCommit aeb40e16…` returned `identity_confirmed = true`
and `mismatches = []`. The receipt is kept locally under the untracked
`artifacts/agent/pre-g9b-r3-c1-final/`. No further verification is run for this
status-only closeout.

## Debt disposition

| Identifier | State |
|---|---|
| `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` | **RESOLVED** by the approved candidate |
| `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE` | **RESOLVED** by the approved candidate |
| `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION` | retained; owner `PRE-G9B-R3-C1-U1`; `PLANNED / NOT IMPLEMENTED` |

### New defect and planned successor

The C1 characterization exposed a defect outside the identity-bearing path. In a
construction with no durable CeDG identity, the inherited upstream clipboard can
still rebuild a dependent geo as free geometry when its defining algorithm uses a
host constant or axis. The author considers it important and repairs it **before
`PRE-G9B-R3-X1`**. It is not folded into the frozen C1 candidate.

```text
PRE-G9B-R3-C1-U1
Ordinary clipboard constructive-closure correctness
DESIGNED / PLANNED
NOT IMPLEMENTED
NOT AUTHORIZED BY THIS CLOSEOUT FOR PRODUCTIVE EXECUTION
```

Objective: correct ordinary constructive clipboard closure while keeping ordinary
host construction semantics and CeDG durable identity semantics separate, and
without introducing durable identity into ordinary host geometry. Its position in
the order is an execution-order decision, not a statement that CeDG identity
semantics depend on the upstream repair.

## Retained debt

| Identifier | Owner / state |
|---|---|
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | retained unchanged; inspected at `PRE-G9B-R3-X1` entry |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | retained unchanged; inspected at `PRE-G9B-R3-X1` entry |
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | owner `PRE-G9B-R3-X1` |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward for later governance/documentation reconciliation |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward for later governance/documentation reconciliation |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward for later governance/documentation reconciliation |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |

**X1/X2 evaluator decision, unchanged.** At `X1` entry both evaluator debts are
inspected for synergistic repair inside `X1`. If they cannot be repaired under the
approved Ri conditions, `PRE-G9B-R3-X2 — evaluator correctness` is registered
after `X1` and before `R4`. Neither `X1` nor `X2` is implemented by this closeout.

The Ri quality-debt policy of the
[`PRE-G9B-R2-E0/E1` closeout record](pre_g9b_r2_e0_e1_closeout_record.md)
continues to apply.

## Execution order

```text
PRE-G9B-R3-C1          PASS — AUTHOR APPROVED
        ↓
PRE-G9B-R3-C1-U1       DESIGNED / PLANNED, NOT IMPLEMENTED
        ↓
PRE-G9B-R3-X1          DESIGNED / PLANNED, NOT AUTHORIZED
        ↓
PRE-G9B-R3-X2          only if required by the X1 evaluator-debt policy
        ↓
PRE-G9B-R4             DESIGNED, NOT AUTHORIZED
```

The order is an execution order, not a semantic dependency. Each phase needs its
own explicit authorization and exact base commit.

## Promotion

The author authorizes promotion of the complete linear history ending at this
closeout commit to `main`, by ordinary clean non-force fast-forward only: no
rebase, amend, merge, squash, force push, tag or release. Promotion proceeds only
if local `main`, `origin/main` and the live remote `main` still equal the base
below immediately before it.

Remote identity before promotion:

```text
local main       = 89d481b1b3eef36c429f920178d5e5de6c0149e3
origin/main      = 89d481b1b3eef36c429f920178d5e5de6c0149e3
live remote main = 89d481b1b3eef36c429f920178d5e5de6c0149e3
tree             = 56a9823c602710805dfc18bd703f6a0d0b6ed5fb
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch
`feature/pre-g9b-r3-c1-clipboard-correctness` is deleted locally with
`git branch -d` after promotion, unless repository tooling pins it.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R3-C1-U1`,
`PRE-G9B-R3-X1`, `PRE-G9B-R3-X2`, `R4` and every later `PRE-G9B-R` phase, `G9B`,
`G9C`, `G9U2` and productive `G10` remain unauthorized. No tag or release is
authorized.

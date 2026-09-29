# PRE-G9B-R3-C1-U1 closeout record

```text
PRE-G9B-R3-C1-U1 = PASS — AUTHOR APPROVED
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
inventory, registry pin, schema, serialization or scientific-contract file, and it
does not modify the approved technical candidate. Besides this record and its
machine-readable mirror, it only updates the `PRE-G9B-R3-C1-U1` status statements
of the roadmap.

Under the frozen-candidate rule in
[`task-template.prompt.md`](../../.github/prompts/tasks/task-template.prompt.md),
the candidate's own artifacts record only `TECHNICAL_CANDIDATE_STATE = FROZEN`
and `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; this record is the sole
authority for current author approval. Every statement in the candidate's
artifacts stands unchanged.

## Author disposition

```text
PRE-G9B-R3-C1-U1 = PASS — AUTHOR APPROVED

APPROVED_TECHNICAL_CANDIDATE = c595adc5ff5a8cc1b1df0b77850b633fad942926
APPROVED_TREE                = 270d1cfb0ad4076b119040a87e5cb7db14fef2c9
FINAL                        = verification-bff478802c8d464095428ac5f8768988, ACCEPTED / COMPLETE
FINAL_RECEIPT                = e48ca6971b777750df6f32dd36b87137b5394b4205e7fc7ee61c0d4ff9d2326e
```

## Approved technical candidate

```text
commit = c595adc5ff5a8cc1b1df0b77850b633fad942926
tree   = 270d1cfb0ad4076b119040a87e5cb7db14fef2c9
parent = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0
base   = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0
branch = feature/pre-g9b-r3-c1-u1-ordinary-clipboard-closure
```

The candidate is preserved unchanged. Its artifacts are the
[candidate report](pre_g9b_r3_c1_u1_candidate_report.md), the
[validation matrix](pre_g9b_r3_c1_u1_validation_matrix.md) and the
[design record](../architecture/pre_g9b_r3_c1_u1_ordinary_clipboard_closure_design.md).

## Accepted content

The approval covers the bounded correction of ordinary clipboard constructive
closure. The accepted rule is:

```text
ordinary constructive closure != CeDG durable identity closure
```

- The implementation completes the ordinary host construction required to rebuild
  copied dependent geometry: the parent algorithm of every copied labeled geo is
  added when each of its other inputs is already copied, and global host constants
  and axes are referenced by name. It adds parents of copied geos only and never the
  dependents of the copied set.
- It does **not** give ordinary geometry a durable CeDG identity, create spatial
  records, copy the global axes as ordinary construction objects, change the CeDG
  identity schema, change the serialization format, introduce a migration, or change
  the identity-bearing clipboard semantics accepted at `PRE-G9B-R3-C1`.

**Upstream reproduction.** The defect reproduces on the pinned upstream baseline
`9b93256b7df401ff056c37b502d82df4d72b1522`. `CopyPaste.addPredecessorGeos` and
`InternalClipboard.addAlgosDependentFromInside` are byte-identical to that baseline
and are not modified. It is inherited upstream behaviour, not introduced by
GeoCeDG.

**Host versus identity separation.** The failing behaviour occurs in constructions
with no identity participant, in plain and GeoCeDG-configured applications. The
completion runs for every payload and needs no identity; identity-bearing payloads
receive the same completion as at `PRE-G9B-R3-C1`, and the C1 focal tests pass
unchanged in the accepted FINAL evidence.

## Accepted evidence

The author accepts the single canonical `FINAL` run on the exact approved
candidate. No `FINAL` rerun is authorized or required.

| Field | Value |
|---|---|
| command | `tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-c1-u1-final` |
| run | `verification-bff478802c8d464095428ac5f8768988` |
| verdict | `ACCEPTED / COMPLETE`; 40/40 acceptance checks satisfied, 0 violated, 0 untrusted, 0 not run; exit 0 |
| plan hash | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| result hash | `9c1d01467942563c19b285ce449b55d0ba59a5489edd93a5967d4ffd2ebd7c36` |
| result file SHA-256 | `75748cccb282a80a21cea043298df094d5dc5157daec74b2cd9d9b83a121f05e` |
| receipt id | `e48ca6971b777750df6f32dd36b87137b5394b4205e7fc7ee61c0d4ff9d2326e` |
| receipt file SHA-256 | `a10471075affcabebc8288c4c69b216f9ef532adb9b9f8a7f2be14934d498d24` |
| receipt binding | `candidate_commit = c595adc5ff5a8cc1b1df0b77850b633fad942926`, `candidate_tree = 270d1cfb0ad4076b119040a87e5cb7db14fef2c9`, `base_commit = null` |
| checker / input identity | `18492342b4e45957200bccb5eb89c1d0e115b8bef7cec47db718d10f69a992ba` / `4aed0e08c1873b5f4a2c67b846d7ad007f8a7a50565d7b794cf34f754eee272f` |
| environment signature | `de567676a3168eb19a13b0b8764c15e6fdca0d97baec6925aac5b72f6c7dc4c0` |

JUnit evidence of the run: `final.shared` 6 814 tests, 0 failures or errors, 10
allowlisted skips; `final.desktop` 1 527 tests, 0 failures, 1 skip; `g9u1-isolated`
5 and `revision3-diagnostic` 1, both without failures. The 20 C1-U1 focal tests
(9 + 9 shared and 2 Desktop) and the 11 C1 focal tests are in the evidence, and all
11 classes of `post-g9u1-a3.narrow` (114 tests, pin `9a45556c…`) pass.

Diagnostics, separate from acceptance: eight `DIAGNOSTIC_CLEAR`;
`diagnostic.governance` `DIAGNOSTIC_FINDING` and `diagnostic.historical-consistency`
`DIAGNOSTIC_UNAVAILABLE`, identical in outcome and cause to the previous accepted
`FINAL`.

The identity-only inspection `tools/agent/phase-closeout.ps1 -Action INSPECT` with
this receipt and `-ApprovedCommit c595adc5…` returned `identity_confirmed = true` and
`mismatches = []`. The receipt is kept locally under the untracked
`artifacts/agent/pre-g9b-r3-c1-u1-final/`. No further verification is run for this
status-only closeout.

## Debt disposition

| Identifier | State |
|---|---|
| `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION` | **RESOLVED** by the approved candidate |

**Retained observation** (not a product debt):
`OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS`. A generic non-identity XML paste
may still tolerate unrelated command errors according to existing host behaviour.
`PRE-G9B-R3-C1-U1` establishes no new generic XML-error policy. It is not promoted to
a defect unless future evidence shows an actual product-integrity failure that needs
a separate decision.

## Retained debt and observations

| Identifier | Owner / state |
|---|---|
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | owner `PRE-G9B-R3-X1` |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | retained unchanged; inspected at `PRE-G9B-R3-X1` entry |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | retained unchanged; inspected at `PRE-G9B-R3-X1` entry |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE` | observation, retained |
| `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observation, retained |

**X1/X2 evaluator decision, unchanged.** At `X1` entry both evaluator debts are
inspected for synergistic repair under the Ri policy. If either cannot be resolved
cleanly inside `X1` without widening its scientific or architectural scope,
`PRE-G9B-R3-X2 — evaluator correctness` is retained/activated after `X1` and before
`R4`. Neither is implemented by this closeout. None of the items above is repaired
here.

## Execution order

```text
PRE-G9B-R3-C1          PASS — AUTHOR APPROVED (closed)
        ↓
PRE-G9B-R3-C1-U1       PASS — AUTHOR APPROVED (closed)
        ↓
PRE-G9B-R3-X1          NEXT PLANNED PHASE — NOT STARTED — NOT AUTHORIZED
        ↓
PRE-G9B-R3-X2          only if required by the X1 evaluator-debt policy
        ↓
PRE-G9B-R4             DESIGNED, NOT AUTHORIZED
```

The order is an execution order, not a semantic dependency. Each phase needs its own
explicit authorization and exact base commit.

## Promotion

The author authorizes promotion of the complete linear history ending at this
closeout commit to `main`, by ordinary clean non-force fast-forward only: no merge,
rebase, squash, amend, force push, tag or release. Promotion proceeds only if local
`main`, `origin/main` and the live remote `main` still equal the base below
immediately before it.

Remote identity before promotion:

```text
local main       = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0
origin/main      = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0
live remote main = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0
tree             = 46988e065de7d62aa37d1e29a9f1940dcf5c8f6f
```

The identity after promotion is this record's own commit, which the record cannot
name; it is reported with the promotion. The working branch
`feature/pre-g9b-r3-c1-u1-ordinary-clipboard-closure` is deleted locally with
`git branch -d` after promotion, unless repository tooling pins it.

## Authorization state

This closeout authorizes nothing beyond its own promotion. `PRE-G9B-R3-X1`,
`PRE-G9B-R3-X2`, `R4` and every later `PRE-G9B-R` phase, `G9B`, `G9C`, `G9U2` and
productive `G10` remain unauthorized. No tag or release is authorized.

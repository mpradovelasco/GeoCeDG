# ADR 0025: Commit-first acceptance and dual closeout

- Status: **ACCEPTED — AUTHOR APPROVED AND PROMOTED TO `main`**
- Date: 2026-09-07
- Scope: operational/verification infrastructure and governance only
- Product phase effect: `NONE`
- Scientific contract changed: `false`
- Self approval: `false`
- Governing contract: [verification levels, section 12](../../geocedg/specs/operations/verification-levels.md#12-commit-first-acceptance-and-dual-closeout)
- Preserved authorities: [ADR 0020](0020-verification-levels-and-current-run-evidence.md),
  [ADR 0023](0023-phase-verifier-lifecycle-and-author-closeout.md) and
  [ADR 0024](0024-verification-input-identity.md)
- Current record: [dual closeout reform report](../validation/dual_closeout_reform_report.md)
- Published reform tip: `259910d73196145e7b9880a200e3047d8f6c6875`

## Context

GeoCeDG already separates technical verification from author approval and has
an exact-SHA, fail-closed status-only closeout protocol. The G9U1 closeout
exposed an avoidable cost in the order of operations: its successful heavy
technical executions truthfully belonged to the dirty precommit cohort at
`e4ef3d48ea95a0c3243e57dfc703b539d455c33e`, whose exact product bytes were
then committed at `28f7843184cfb202bbfcca1cbcc56a25a7a77bca`. The historical
evidence could prove byte provenance but could not become an execution on that
later commit. A separate lifecycle normalization was therefore needed before
author closeout.

That chronology remains recorded in the
[G9U1 lifecycle design](../architecture/g9u1_verifier_lifecycle.md) and
[report](../validation/g9u1_verifier_lifecycle_report.md). This decision does
not edit, supersede, relabel or reseal any G9U1 receipt, evidence artifact,
author decision, closeout commit or published tag. The incident is historical
motivation for a prospective rule.

ADR 0023 already requires fresh PHASE, COMPOSED and FULL coverage on the clean
committed technical cohort before verified closeout, and ADR 0024 preserves
that exact execution identity. The remaining gap is an early fail-closed
preflight and machine-readable classification that enforce the correct order
before heavy work, plus an explicit distinction between verified and
personally author-operated final Git promotion. This decision generalizes those
additions without changing PHASE, COMPOSED or FULL coverage and without
mistaking coverage levels for mandatory duplicate physical campaigns.

## Decision

### 1. Acceptance is commit-first

For every future closable phase, the normal final-acceptance sequence is:

```text
implementation
  -> DEV/focal verification
  -> clean technical commit T
  -> CLOSEOUT_READINESS(T; both routes validated, no mode selected)
  -> compare real PHASE/COMPOSED/FULL execution plans
  -> one canonical clean FULL(T) with authenticated PHASE,
     COMPOSED and FULL claims
  -> author review/smoke
  -> explicit author approval of exact T
  -> selected closeout path
```

PHASE, COMPOSED or FULL may still run on a dirty/precommit cohort for
development or diagnosis. Such a run must identify that purpose and emit
`closeoutConsumable=false` with reason `DIRTY_PRECOMMIT_COHORT`. It cannot later
be attributed to a commit, even if that commit contains identical bytes. A run
requested as final acceptance rejects a dirty index/worktree before expensive
work begins.

`CLOSEOUT_READINESS` is a cheap, mandatory preflight for the canonical FULL
campaign intended as final acceptance. The generic entry point is
`tools/agent/phase-closeout.ps1 -Action READINESS`, with explicit
`-TechnicalCommit`, `-PolicyPath` and `-ReadinessReceiptPath`; passing
`-CloseoutMode` to READINESS is an error. It runs before the campaign and proves
at least:

1. exact commit `T` exists, is checked out as `HEAD`, and the index/worktree are
   clean under the declared input/materialization contract;
2. the promotion anchor is an ancestor of `T` through a linear single-parent
   path with no merge or substituted technical history;
3. all subsequently emitted acceptance roots and receipts will name exact `T`
   and its tree/source cohort;
4. the phase has valid, constructible declarative routes for exactly both
   `VERIFIED` and `AUTHOR_OPERATED`;
5. no branch name, `latest`, moving ref or prospective tag is used as candidate
   authority;
6. no SHA/evidence self-reference makes the projected closeout impossible;
7. the configured fetch/push remote URLs are identical for this schema-v2
   single-authority route and sealed by exact and sanitized canonical hashes
   without persisting credential material;
8. the future decision-record path is physically absent, nonignored and has
   safe materialization attributes for a regular `100644` blob;
9. the bounded status-only delta can be constructed and audited whichever mode
   is explicitly selected later;
10. the real PHASE, COMPOSED and FULL plans were compared, FULL contains all
    COMPOSED obligations, the applicable phase verifier has a live exact FULL
    integration mapping, and no obligation remains uncovered under schema v2;
    and
11. no known lifecycle/verifier repair would be required after FULL.

A readiness failure blocks the acceptance campaign. Fixing readiness before
the heavy run is ordinary development; repairing lifecycle after FULL is not.

### 2. Evidence purpose and consumability are explicit

Every DEV/PHASE/COMPOSED/FULL root exposes a machine-readable evidence
classification, independent of its technical result:

```json
{
  "evidenceUse": "DEVELOPMENT_DIAGNOSTIC",
  "repositoryCohort": "DIRTY_PRECOMMIT",
  "closeoutConsumable": false,
  "reviewedCandidate": null,
  "closeoutMode": null,
  "reason": "DIRTY_PRECOMMIT_COHORT"
}
```

Only `verify.ps1 -Level FULL -CleanBuild -CloseoutReadinessPath <receipt>`
explicitly requests `FINAL_ACCEPTANCE`. PHASE, COMPOSED and DEV cannot consume
that receipt, nor can `-SkipBuild` or `-IndependentBuilds`. A successful
execution on the clean, readiness-approved exact commit may report:

```json
{
  "evidenceUse": "FINAL_ACCEPTANCE",
  "repositoryCohort": "CLEAN_COMMIT",
  "closeoutConsumable": true,
  "reviewedCandidate": "<exact T>",
  "closeoutMode": null,
  "validatedCloseoutModes": ["VERIFIED", "AUTHOR_OPERATED"],
  "acceptancePlanSha256": "<mode-neutral-plan-sha256>",
  "reason": "READINESS_BOUND_TECHNICAL_GATES_PASSED"
}
```

The result also records the readiness path/hash and whether heavy execution
started. Deterministic non-consumability reasons distinguish dirty/precommit,
missing/failed readiness, pending/failed technical campaign and incomplete
acceptance binding. `FULL=PASS` alone never implies closeout consumability. A
later Git operation, equal tree or lifecycle repair cannot rewrite the
classification or `reviewedCandidate` of historical evidence.

Closeout consumption revalidates the one archived canonical FULL receipt, not
just its top-level counters. The exact clean-`T` input identity, unfiltered
module selections, fresh shared/Desktop test tasks, JUnit XML and case counters,
complete clean Checkstyle authorities, selected JVMs, successful native runs
and their complete hashed audit inventory must remain present and coherent. Its
authenticated envelope must separately prove the physically nested PHASE
assertion, normative COMPOSED subsumption and physical FULL root. Malformed
casing, narrowed coverage, invented lower-level runs, missing files or
synthetic empty evidence fail closed.

Verification levels are coverage/evidence obligations. A higher level avoids a
lower physical rerun only when it executes and authenticates the complete lower
plan in the same cohort and root run. In this policy, PHASE is a physically
nested assertion, COMPOSED is a truthful plan-subsumption claim rather than an
independent run, and FULL is the sole physical root campaign. Schema v2 seals
`complements=[]` only after the executable-plan comparison proves complete
subsumption. A concrete uncovered obligation makes READINESS fail; an approved
generic schema/producer/consumer extension is required before running only that
minimal complement.

### 3. Closeout mode is explicit and post-approval only

Each closable phase policy declares exactly both supported modes. Readiness
validates them and seals a mode-neutral acceptance plan; it selects neither.
After technical evidence and author review, the explicit author approval and
PREPARE/AUDIT invocation select exactly one value:

```text
CLOSEOUT_MODE = VERIFIED | AUTHOR_OPERATED
```

The mode is never inferred from branch, caller identity, available credentials,
tag presence or repository state. It affects only the operations after explicit
author approval of `T`. Both modes require the same clean `T`, readiness PASS,
single clean-output FULL campaign with authenticated PHASE/COMPOSED/FULL
coverage claims, applicable author smoke/review and exact-SHA author decision.
Selecting a mode cannot reduce, replace or reinterpret those gates.

#### `VERIFIED`

This mode preserves the ADR 0023/0024 model:

```text
T
  -> explicit AUTHOR APPROVAL of exact T
  -> verified AUTHOR_CLOSEOUT
  -> bounded status-only commit C
  -> annotated phase tag
  -> fast-forward main
```

Existing ancestry, exact-SHA authority, complete path/mode/blob and
allowed-content checks, input identity, materialization validity, evidence
hash-linkage, no-self-approval and publication guarantees remain mandatory.
For schema-v2 policy, `phase-closeout.ps1 -Action PREPARE -CloseoutMode VERIFIED`
is the generic verified `AUTHOR_CLOSEOUT` preparation gate before `-Action
FINALIZE -CloseoutMode VERIFIED`; PREPARE alone cannot claim the existing gate
that requires exact `C`. FINALIZE revalidates the prepared state, creates and
validates the direct status-only child `C`, annotated tag and protected-branch
  fast-forward, uses a non-force atomic branch-plus-tag push restricted to the
  two literal refspecs, and then
  automatically invokes the post-promotion audit. Exact `C` plus that passing
  audit complete the verified `AUTHOR_CLOSEOUT`. FINALIZE is unavailable in
  `AUTHOR_OPERATED`. Ref-expanding push modes are disabled; an effective remote
  mirror or inherited `push.pushOption` blocks readiness/finalization, and the
  pre-push hook is bypassed for this sealed operation. This decision does not
  weaken or replace either ADR.

#### `AUTHOR_OPERATED`

After explicit approval of `T`,
`tools/agent/phase-closeout.ps1 -Action PREPARE -CloseoutMode AUTHOR_OPERATED`
consumes the exact readiness receipt, the one FULL campaign path with its three
authenticated coverage claims and explicit author-decision JSON. It prepares
and verifies only the exact projected status/documentation delta, stages that
delta/decision record, then stops and prints:

- approved technical SHA `T`;
- the complete expected closeout delta and content policy;
- the exact expected annotated tag and target;
- the target promotion branch/remotes; and
- the commit, tag, fast-forward and push operations that remain under human
  authority.

The tool must not silently create commit `C`, create/move a tag, merge, rebase,
fast-forward, push or force-push in this mode. The author performs those Git
operations personally. No second PHASE/COMPOSED/FULL campaign is required when
the executable inputs remain identical to `T`.

PREPARE is fail-safe through construction and optional atomic persistence of its
receipt. After its first bounded write, any failure preserves the repository
state for inspection; automation must not attempt a destructive rollback across
the index/worktree concurrency boundary. The failure reports
`UNKNOWN_REQUIRES_INSPECTION`, identifies the bounded paths requiring inspection
through the policy-declared status/decision scope, and never claims an unmutated
or restored repository. A fresh PREPARE requires
the author/operator to restore and re-establish exact clean `T` explicitly.

The preparation output represents the human boundary as seven structured Git
operations: create `C`, capture its exact SHA, verify its direct parent, create
the annotated tag, switch to the promotion branch, fast-forward it and push the
branch plus tag. The required read-only post-promotion audit is a separate
structured action and is never counted as or executed as a human Git operation.

After promotion,
`tools/agent/phase-closeout.ps1 -Action AUDIT -CloseoutMode AUTHOR_OPERATED`
names exact `T`, exact `C`, the expected annotated tag and the declared remote.
It is Git-read-only except for writing its ignored/external result. It passes
only when:

1. `T` is an ancestor of `C`;
2. the complete `T..C` delta is exactly the allowed status/documentation delta
   and satisfies its bounded content policy;
3. no product, scientific test, verifier, build input, tolerance, numerical
   reference or other executable input changed;
4. the expected annotated tag peels to the policy-defined target;
5. the promotion is the declared linear fast-forward, with no merge, rebase,
   force-push, unexpected parent or substitution of approved `T`;
6. local `main`, `origin/main` and the queried live remote agree at `C`;
7. index and worktree are clean;
8. technical evidence remains attributed to exact `T`; and
9. `selfApproved=false` remains explicit and consistent.

An audit failure is reported as a closeout/promotion failure. The tool must not
reinterpret technical evidence, mutate refs or repair history to manufacture a
PASS.

The force-push assertion is limited to observable repository evidence: sealed
anchor ancestry, direct `T -> C`, linear history and current local/tracking/live
refs. A transient remote force update that restores the identical object graph
requires an independent hosting audit log and cannot be inferred from final Git
objects alone.

### 4. Phase policy is generic and declarative

The reusable `tools/agent/closeout-workflow.ps1` helper owns the checks above;
`tools/agent/phase-closeout.ps1` is its operator entry point. Each phase
contributes a declarative policy containing its phase identifier, exact
technical-authority rules, bounded status-only paths and content
transformations, decision-record schema, both supported modes, expected
annotated tag, target branch/remote and applicable evidence roles. A phase
verifier may retain its scientific assertions, but must not reimplement generic
readiness, promotion or audit logic.

Unknown policy fields, missing closure data, an unprojectable status delta,
a post-approval selection containing zero or multiple modes, or a route that
depends on a moving reference fail closed. A path allowlist alone remains insufficient: exact Git modes,
content policy and the unchanged executable-input partition must also be proved.
Markdown replacements under `geocedg/specs/` are restricted to the operational
subtree; product/scientific specifications cannot be closeout status targets.

### 5. Review-ready and closeout-ready are different states

- **AUTHOR REVIEW READY** means the product/candidate is prepared for author
  review. It does not assert a clean technical acceptance cohort, complete
  acceptance evidence, selected closeout mode or permission to promote.
- **AUTHOR CLOSEOUT READY** means exact technical SHA `T` was accepted from a
  clean committed checkout, all required final-acceptance evidence is valid and
  closeout-consumable for `T`, one post-approval route is explicitly selected,
  and PREPARE has validated that route against the mode-neutral
  `CLOSEOUT_READINESS` plan. At that point the current index/worktree contains
  exclusively the exact staged closeout delta and is not described as clean.
  Under schema v2 this state is emitted only after PREPARE validates the
  already-existing author decision; automation still cannot originate it.

Only the author can approve exact `T`. Automation may verify an existing
decision and its consequences; it may never infer or create that decision.

### 6. ADR 0024 section 11.2 stays exceptional

The evidence-preserving verifier-repair rule remains a fifteen-condition escape
hatch for the exact input-identity/provenance defect it governs. It is not an
ordinary phase workflow, a substitute for commit-first acceptance or permission
to repair known lifecycle gaps after FULL. A deliberate change to verification
policy, orchestration, acceptance classification, readiness or closeout
methodology uses the normal required levels, including fresh FULL evidence.

## Validation design

Focused fake-first fixtures must exercise both closeout modes and prove:

- final acceptance rejects a dirty/precommit checkout before heavy execution,
  while ordinary dirty PHASE/COMPOSED/FULL remains diagnostic and explicitly
  non-consumable;
- exact clean `T` plus a valid readiness receipt enters final acceptance;
- readiness rejects an absent/unsupported route, moving or branch-name
  authority, impossible self-reference, invalid status projection, absent phase
  integration, an uncovered COMPOSED/FULL plan delta and known lifecycle repair;
- `VERIFIED` retains every ADR 0023/0024 ancestry, exact-SHA, bounded-content,
  provenance, materialization and no-self-approval guarantee, and its FINALIZE
  performs the exact non-force atomic publication plus automatic audit;
- `AUTHOR_OPERATED` preparation changes only the projected status/decision
  paths and index, does not commit, tag, merge, rebase, promote or push, and
  rejects FINALIZE;
- a correct author-operated publication passes post-promotion audit; and
- the audit rejects a substituted SHA, wrong/lightweight/moved tag, merge or
  non-linear history, productive/executable change, extra closeout delta, dirty
  index/worktree and divergent local/tracking/live remote.

Fixtures also prove that the one technical root remains attributed to `T`, that
`selfApproved=false`, that one physical FULL carries truthful authenticated
PHASE/COMPOSED/FULL claims, and that the exact same campaign plan is consumed
for `VERIFIED` and `AUTHOR_OPERATED`. Parser/static validation, Git whitespace
checks and focused repeat/determinism evidence accompany those cases.

Because this change deliberately modifies verification policy and
orchestration, its final executable cohort requires one normal readiness-bound
clean-output FULL campaign with authenticated PHASE/COMPOSED/FULL claims. ADR
0024 section 11.2 is not available to avoid that campaign. A separate PHASE or
COMPOSED campaign is required only if the pre-heavy comparison identifies an
uncovered obligation; none may be run merely to duplicate coverage. Commands,
exits and saved evidence are recorded in the linked implementation report;
pending or failed execution is not a PASS.

The final candidate also freezes verification by impact at phase start.
`BOUNDED_PHASE` requires PHASE; `INTEGRATED_PHASE` adds COMPOSED only for a
declared additional integration obligation; `GLOBAL_IMPACT` and
`RELEASE_OR_MILESTONE` require FULL; global operational verification changes
require focused operational evidence plus FULL; and documentation/status-only
changes require static validation without FULL. Any higher unplanned campaign
must stop with `VERIFICATION_ESCALATION_REQUEST` and await author authority.
The single-integrator defaults are explicitly declared as `BOUNDED_PHASE` and
`AUTHOR_OPERATED`; the latter remains an author choice after review, never an
inferred approval. This reform itself is a global verification-infrastructure
change, so its frozen final plan remains focused evidence plus one FULL.

## Consequences

- Final acceptance evidence has one immutable candidate identity before the
  expensive campaign starts.
- Development runs remain useful but cannot drift into acceptance by prose or
  later commit.
- Authors may choose tool-verified promotion or retain personal authority over
  final Git operations without paying for unchanged scientific execution twice.
- The author-operated audit expands remote/ref/history validation but changes no
  product, scientific, persistence, tolerance or build coverage.
- Existing phase-specific lifecycle policies may be migrated to the generic
  declarative contract prospectively; historical receipts and published
  closeouts remain immutable.

## Rejected alternatives

| Alternative | Disposition |
|---|---|
| Run final FULL before creating `T`, then prove equal bytes | Rejected; equality is provenance, not execution on the later commit. |
| Always run standalone PHASE, then COMPOSED, then FULL | Rejected when one same-cohort FULL executes and authenticates all three coverage obligations; physical repetition adds cost without evidence. |
| Infer closeout mode from credentials or branch | Rejected; authority and side effects would be ambiguous. |
| Let author-operated preparation commit, tag or push when convenient | Rejected; those operations are explicitly reserved to the author in this mode. |
| Re-run FULL after status-only `C` unconditionally | Rejected when executable inputs are proved identical; exact linkage and audit are the appropriate gates. |
| Generalize ADR 0024 section 11.2 into routine evidence reuse | Rejected; the exception remains bounded and conjunctive. |
| Add one lifecycle script per phase | Rejected; generic checks plus declarative phase policy are the sustainable boundary. |

## Current decision boundary

This ADR and its implementation were explicitly approved by the author and
promoted to `main` through published reform tip
`259910d73196145e7b9880a200e3047d8f6c6875`. This status correction records that
completed decision; it changes no technical claim and reinterprets no evidence.

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: the reform changes repository verification policy and Git lifecycle
checks only; it adds no PowerShell, Git, JDK, Gradle, Conda, packaging, download,
environment-variable or workstation prerequisite.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md;
  docs/developer/geocedg_agent_prompt_guide.md;
  .github/prompts/canonical/verification.prompt.md;
  .github/prompts/canonical/governance.prompt.md;
  .github/prompts/reviews/change-review.prompt.md;
  .github/prompts/tasks/task-template.prompt.md
selfApproved = false
authorApproved = true
promotedToMain = true
passClaimed = false
```

The accepted reform authorizes no G9U2, G9B, G9C, G10 or other productive phase.

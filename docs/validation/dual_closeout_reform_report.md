# Commit-first acceptance and dual closeout reform

- Status: **IMPLEMENTATION CANDIDATE — AUTHOR REVIEW READY**
- Date: 2026-09-07
- Scope: operational/verification infrastructure and governance only
- Product phase effect: `NONE`
- Scientific or product runtime semantics changed: `false`
- Author approval: `false`
- Self approval: `false`
- Decision: [ADR 0025](../adr/0025-commit-first-acceptance-and-dual-closeout.md)
- Normative contract: [verification levels, section 12](../../geocedg/specs/operations/verification-levels.md#12-commit-first-acceptance-and-dual-closeout)

## Outcome under review

The candidate introduces one generic, declarative lifecycle for future closable
phases. A clean technical commit is required before an acceptance-intended
campaign; mode-neutral `CLOSEOUT_READINESS` binds the exact commit, phase
policy, both constructible `VERIFIED` and `AUTHOR_OPERATED` routes, expected
status-only projection, pre-promotion refs and the real PHASE/COMPOSED/FULL plan
comparison before heavy work begins. One route is selected explicitly only
after author review and exact-SHA approval, when PREPARE validates that
post-approval choice.

The selected acceptance strategy is one physical clean FULL campaign on `T`.
Its authenticated same-run envelope preserves separable evidence for the
physically nested PHASE verifier, COMPOSED plan subsumption and the physical
FULL root. No standalone lower campaign is claimed. Schema v2 is fail-closed:
an uncovered obligation blocks READINESS until an approved generic extension
can represent and verify the exact minimal complement. The current executable
plan proves complete subsumption and requires none.

Root verification results now distinguish technical success from evidence use,
repository cohort and closeout consumability. Runs without a validated
readiness receipt remain development/diagnostic evidence. A dirty/precommit
success cannot be relabelled for a later commit.

The closeout consumer also revalidates the full archived canonical receipt and
its three coverage claims:
exact clean-commit identity and selection, fresh shared/Desktop test tasks,
JUnit XML/cases/counters, all Checkstyle authorities, native executions and the
complete hashed audit inventory. Top-level PASS fields or non-empty counters
alone are insufficient.

The `AUTHOR_OPERATED` route prepares and stages only the declared status delta,
then stops before commit, tag, fast-forward or push. Its post-promotion audit is
read-only apart from its generated result and checks exact ancestry, complete
delta, unchanged executable inputs, annotated tag, linear promotion, live
remote agreement, cleanliness, exact approved SHA, evidence attribution to the
technical commit and `selfApproved=false`. The existing `VERIFIED` guarantees
of ADR 0023/0024 remain in force. Its explicit verified-only FINALIZE revalidates
PREPARE, creates `C` and the annotated tag, fast-forwards `main`, performs one
non-force atomic push limited to the literal branch and tag refspecs, and invokes
the same audit automatically. Ref expansion and the pre-push hook are disabled;
remote mirror mode or configured push options fail closed. AUTHOR_OPERATED
cannot enter FINALIZE.

Preparation exposes exactly seven structured human Git operations. The
required automatic post-promotion audit is emitted separately with its complete
parameter binding and is not silently included in that human-operation list.
Preparation uses bounded repository writes and atomic receipt persistence. Any
failure after the first write preserves index and worktree evidence rather than
attempting a destructive rollback across a concurrent-update window. It reports
an unknown mutation state, possible partial mutation and the bounded paths that
require inspection through the policy-declared status/decision scope; retry
requires an explicit return to exact clean `T`.

The post-state audit proves the observable non-force-push contract from the
sealed readiness anchor through direct `T -> C` ancestry and exact current
local/tracking/live refs. Detecting a transient remote update that restores the
same object graph would require an independent hosting audit log and is not
claimed from Git objects alone.

## Architectural placement

The change is confined to:

- generic PowerShell lifecycle and verification orchestration under
  `tools/agent/`;
- a schema-v2 declarative phase policy under `geocedg/specs/operations/` and
  `geocedg/validation/operations/`;
- operational ADR/specification, developer guidance and canonical prompts; and
- fake-first positive and negative fixtures for Git/ref/delta behavior.

No code under `source/`, application profile, geometric kernel, command,
serialization, product test, scientific tolerance/reference, packaging or
runtime model is changed. Phase-specific scientific logic remains in each phase
verifier rather than in the closeout helper.

## Historical provenance

G9U1 exposed the cost of running heavy gates on a dirty precommit cohort before
creating its technical checkpoint. Its historical execution receipts remain
attributed to their recorded repository/index/status identity. The later
lifecycle normalization, author closeout, closeout commit and published tag are
not edited or reclassified by this reform. ADR 0024 section 11.2 remains a
narrow exceptional repair and is not used to validate this deliberate
methodology change.

## Validation design and evidence

The pre-change focal baseline passed:

```text
command = .\tools\agent\verify-verification-infrastructure.ps1 -LogDirectory artifacts\dual-closeout-reform\baseline\verification-infrastructure
exitCode = 0
result = artifacts/dual-closeout-reform/baseline/verification-infrastructure/verification-infrastructure.json
evidence = fake-first operational contract
```

The final development focal and negative checks completed before freezing the
technical commit:

```text
command = .\tools\agent\tests\verification-runtime.Tests.ps1 -ModulePath <resolved tools/agent/verification-runtime.psm1> -LogDirectory artifacts\dual-closeout-reform\focal\verification-runtime-final-root-r3
exitCode = 0
result = artifacts/dual-closeout-reform/focal/verification-runtime-final-root-r3/7756d766a1c642e8965b73d1eebbee41/verification-runtime-tests.json
assertions = 119/119 PASS
receiptSha256 = d70a2001c19ac28120b786afc9ccaefca4d04ae402245509e3664fc28ce0f741

command = .\tools\agent\tests\phase-lifecycle.Tests.ps1 -HelperPath tools/agent/phase-lifecycle.ps1 -LogDirectory artifacts\dual-closeout-reform\focal\phase-lifecycle-r2
exitCode = 0
result = artifacts/dual-closeout-reform/focal/phase-lifecycle-r2/4e783906874442198eb173d124c8ae6c/phase-lifecycle-tests.json
cases = 61/61 PASS
fixtureSha256 = ad0d2e202e02f6610768d17a33d697ca7441ad9846f5941d35eb1179fbb8c8dc
receiptSha256 = 8df780c64cf03400030ebe88f90a3a718488edda073d40c8c5e27724a88adeef

command = .\tools\agent\tests\phase-closeout.Tests.ps1 -HelperPath tools/agent/closeout-workflow.ps1 -LogDirectory artifacts\dual-closeout-reform\focal\phase-closeout-r20
exitCode = 0
result = artifacts/dual-closeout-reform/focal/phase-closeout-r20/024480b695b94faa927ae1a6a00d6dc5/phase-closeout-tests.json
cases = 107/107 PASS
fixtureSha256 = 3dd685a3a85ee60004bf6de372d41479574b4746b1e36cc7ac0ac1cf676b8956
receiptSha256 = d0e212746d6b54505494ff6d81a4abe01d5eefde1f5002f7a71252ff2336f7d0

command = .\tools\agent\verify-verification-infrastructure.ps1 -LogDirectory artifacts\dual-closeout-reform\focal\verification-infrastructure-final-r3
exitCode = 0
result = artifacts/dual-closeout-reform/focal/verification-infrastructure-final-r3/verification-infrastructure.json
state = PASS_FAKE_FIRST_OPERATIONAL_ONLY
fixtures = verification-runtime 119/119; generated-state 18 cases/143 assertions;
  phase-lifecycle 61/61; phase-closeout 107/107
receiptSha256 = ddb3dab422e9d553639e7794ba4053c6a4f1e4602fc90a2645b25ccf39bb455e

command = .\tools\agent\verify.ps1 -Level FULL -CleanBuild -CloseoutReadinessPath artifacts\dual-closeout-reform\missing-readiness.json -LogDirectory artifacts\dual-closeout-reform\negative-dirty-final-root-r1
exitCode = 1 (expected rejection before heavy execution)
result = artifacts/dual-closeout-reform/negative-dirty-final-root-r1/verification-result.json
classification = requested FINAL_ACCEPTANCE; DEVELOPMENT_DIAGNOSTIC;
  DIRTY_PRECOMMIT; closeoutConsumable=false;
  reason=DIRTY_PRECOMMIT_COHORT; heavyCampaignStarted=false
```

These are development/fake-first checks, not final acceptance evidence and not
retrospectively attributable to the later clean technical commit.

The candidate fixtures cover at least:

- dirty final-acceptance rejection and clean exact-commit acceptance;
- readiness rejection for absent route, moving identity, self-reference and
  known lifecycle repair, missing phase integration and incomplete plan
  subsumption;
- readiness rejection when a closeout replacement targets a product/scientific
  specification or validation Markdown outside operational status authority;
- unchanged PHASE/COMPOSED/FULL coverage for both modes from one physical FULL;
- truthful nested-PHASE/subsumed-COMPOSED/physical-FULL evidence and rejection
  of invented independent lower executions;
- preservation of the ADR 0023/0024 verified-closeout perimeter;
- absence of commit/tag/ref/push mutation during author-operated preparation;
- correct post-promotion acceptance; and
- rejection of substituted SHA, wrong/lightweight tag, merge or non-direct
  closeout, product/executable or extra delta, dirty state and divergent remote.

Because verifier policy and orchestration change, acceptance requires focused
fixtures followed by one readiness-bound clean-output FULL on the eventual
clean technical commit. That physical root, its authenticated
PHASE/COMPOSED/FULL claims and their hashes are recorded in the status-only
closeout record; their absence here is deliberate, because this source file
must be frozen before the final campaign rather than edited retrospectively.

## Impact declarations

```text
PRODUCT_PHASE_EFFECT = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = UPDATE_REQUIRED
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: the reform changes repository policy, PowerShell orchestration and
Git lifecycle checks only. It adds no supported runtime, PowerShell, Git, JDK,
Gradle, Conda, packaging, download, environment-variable or workstation
prerequisite.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/developer/geocedg_developer_guide.md;
  docs/developer/geocedg_agent_prompt_guide.md;
  .github/prompts/canonical/verification.prompt.md;
  .github/prompts/canonical/governance.prompt.md;
  .github/prompts/reviews/change-review.prompt.md;
  .github/prompts/tasks/task-template.prompt.md
selfApproved = false
authorApproved = false
passClaimed = false
```

The candidate authorizes no G9U2, G9B, G9C, G10 or other product phase.

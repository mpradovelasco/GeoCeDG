# GeoCeDG canonical verification prompt

Status: canonical operational prompt

Use executable repository tools as the authority. Reports summarize their
saved evidence; they do not replace a command result.

## Entry points

- Default COMPOSED gate applicable to the current checkout: `tools/agent/verify.ps1`.
- Exhaustive supported test gate: `tools/agent/verify.ps1 -Level FULL`
  (`-FullTests` remains the legacy alias).
- Explicit inner-loop/capability scope: `-Level DEV -Module <shared|desktop>
  -TestFilter <filters>` or `-Level PHASE -Phase <supported-id>`.
- Operational structure only: `tools/agent/verify-operational.ps1`.
- Pinned upstream compile gate: `tools/agent/verify-baseline.ps1`.
- Informational benchmark suite: `tools/benchmark/run.ps1`.

Use `geocedg/specs/operations/verification-levels.md` for level/perimeter,
bootstrap-impact and infrastructure-review rules. Start with the narrowest
relevant command, then complete the required COMPOSED/FULL gate. DEV is not an
acceptance gate; static `-SkipBuild` evidence is incomplete. A required but
unrun/failed FULL gate cannot be waived by narrower success. Record applicable
interactive, packaging and external-runtime evidence separately.

## Final acceptance and closeout

Ordinary verifier runs are `DEVELOPMENT_DIAGNOSTIC`. They may run dirty for
development, but must report `repositoryCohort=DIRTY_PRECOMMIT` and
`closeoutConsumable=false`; never reattribute them to a later commit.

For final acceptance, first create clean technical commit `T` and run the cheap,
mode-neutral
`tools/agent/phase-closeout.ps1 -Action READINESS` with exact `T`, the
declarative phase policy and an ignored/external receipt path, without
`-CloseoutMode`. It must validate both routes. If readiness fails, do not start
acceptance FULL. Readiness compares the actual PHASE, COMPOSED and FULL plans.
When FULL contains and authenticates the lower obligations, pass the valid
receipt once to `verify.ps1 -Level FULL -CleanBuild -CloseoutReadinessPath
<receipt>`; this is the only `FINAL_ACCEPTANCE` route and
is incompatible with DEV/PHASE/COMPOSED, `-SkipBuild` and
`-IndependentBuilds`. Require one physical FULL root with separable same-run
claims: physically nested PHASE, plan-subsumed COMPOSED and physical FULL. Never
describe the COMPOSED claim as an independent execution. Schema v2 blocks
readiness if FULL cannot execute or evidence an obligation; extend the approved
schema, producer and consumer before running the exact minimal complement.

Require the result to name exact `reviewedCandidate=T`, `CLEAN_COMMIT`,
`closeoutMode=null`, both `validatedCloseoutModes`, the mode-neutral acceptance
plan, readiness path/hash and `closeoutConsumable=true`. `FULL=PASS` alone is
not closeout evidence. Keep `AUTHOR REVIEW READY` (product prepared for review)
separate from `AUTHOR CLOSEOUT READY` (clean `T`, consumable required evidence,
one explicitly selected post-approval route and a PREPARE-valid binding to
readiness). Readiness is not approval and selects no mode.

After explicit author approval of exact `T`, `VERIFIED` keeps the ADR 0023/0024
verified AUTHOR_CLOSEOUT, status-only `C`, annotated tag and fast-forward path;
its generic schema-v2 gate is `phase-closeout.ps1 -Action PREPARE
-CloseoutMode VERIFIED`, followed by verified-only `-Action FINALIZE` for exact commit/tag/
fast-forward/non-force atomic push and automatic audit. For `AUTHOR_OPERATED`,
use the same PREPARE interface with the explicit alternate mode, receipt, the
single FULL campaign path and author-decision JSON. It may stage
only the projected status delta and must stop before commit/tag/promotion/push.
After the author performs those operations, run `-Action AUDIT` with exact `T`
and `C`; report mismatches without moving refs or reinterpreting evidence.

ADR 0024 section 11.2 remains an exceptional fifteen-condition repair, not the
ordinary workflow. It cannot waive required FULL for deliberate verification
policy, readiness, classification or closeout-methodology changes.

COMPOSED/FULL use separate shared and Desktop test invocations. Current-run
receipt consumption is valid only through the explicit executable protocol;
every phase retains its own live assertions. `-IndependentBuilds` retains the
original orchestration for equivalence/diagnosis. Explicit `-KeepBuildOutputs`
retains generated outputs; it does not change test scope or freshness. Never
install tools silently, suppress a failure, edit generated evidence, or
translate an environment/permission failure into product code.

`verify.ps1` reports the current branch or detached HEAD, exact commit, and
latest included phase from the normative roadmap. The branch is diagnostic;
the roadmap and versioned checkout determine the applicable productive gates.
Historical G7 phase preconditions remain available only through the explicit
`-ReproduceCharacterization` and `-ReproduceImplementation` modes of their
focused verifiers.

For every command record its working directory, arguments, exit code, log or
artifact path, whether evidence is static, fake-first, skipped, or from a real
runtime, and its machine-readable evidence-use/consumability classification.
Run `git diff --check` and report final `git status --short`.

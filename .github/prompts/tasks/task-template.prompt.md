# Objective

State one concrete outcome.

Declare and freeze `VERIFICATION_CLASS` and the planned acceptance level before
implementation. For one developer/integrator, default explicitly to
`BOUNDED_PHASE` and propose `AUTHOR_OPERATED`; later closeout selection remains
an explicit author decision. A higher campaign requires a concrete
`VERIFICATION_ESCALATION_REQUEST` and author authorization before execution.

# Authority and evidence hierarchy

Reference `AGENTS.md`, current code/build, accepted specifications/ADRs, and
task-specific evidence in order.

# Scope

List exact files, modules, and observable behavior included.
For an operational-only task, state `PRODUCT_PHASE_EFFECT = NONE` and name the
productive phases that remain unauthorized.

# Explicitly forbidden scope

List adjacent layers and behavior that must remain unchanged.

# Architectural placement

Name the owning layer and explain why it is authoritative.

# Required design/specification

Name the approved specification/ADR or require one before implementation.

# Geometric invariants and degeneracies

Reference governing definitions. State `not applicable` only for a genuinely
non-geometric task.

# Compatibility and serialization

State legacy, file-format, feature-flag, and migration constraints.

# Required tests and commands

Name focused checks and the relevant `tools/agent/verify*.ps1` entry point.
Distinguish DEV, PHASE, COMPOSED and FULL; name explicit DEV filters and the PHASE
regression perimeter. Identify verification-infrastructure impact and required
FULL validation under `geocedg/specs/operations/verification-levels.md`. A smaller
development selection never changes the acceptance gate.

If the commands are intended as final acceptance evidence, require this order:
focused/DEV work, clean technical commit `T`,
`tools/agent/phase-closeout.ps1 -Action READINESS`, comparison of the actual
PHASE/COMPOSED/FULL plans, then one `verify.ps1 -Level FULL -CleanBuild
-CloseoutReadinessPath <receipt>` when it authenticates all three obligations.
READINESS validates both routes and accepts no mode. Require the single final
root to name exact `T`, `CLEAN_COMMIT`, `closeoutMode=null`, both validated
modes and `closeoutConsumable=true`, with truthful nested-PHASE,
subsumed-COMPOSED and physical-FULL claims. Schema v2 must block readiness if
FULL cannot cover an obligation; require an approved generic extension before
running the exact minimal complement. Only after author
review and exact-`T` approval, select exactly one `CLOSEOUT_MODE = VERIFIED |
AUTHOR_OPERATED` for PREPARE; never infer it. Otherwise label the run
`DEVELOPMENT_DIAGNOSTIC`; a dirty/precommit PASS may not be reattributed after
commit.

# Acceptance and closeout

State whether the task stops at `AUTHOR REVIEW READY` or must reach `AUTHOR
CLOSEOUT READY`. The first means prepared for review. The second additionally
requires clean exact `T`, closeout-consumable required evidence, one explicitly
selected post-approval route and a PREPARE-valid binding to mode-neutral
readiness. Readiness itself means neither mode selection nor author approval;
only an explicit author decision naming `T` does.

For `VERIFIED`, reference the unchanged ADR 0023/0024 AUTHOR_CLOSEOUT,
status-only `C`, annotated tag and fast-forward contract, with verified-only
FINALIZE performing a non-force atomic push and automatic audit. For
`AUTHOR_OPERATED`, require `phase-closeout.ps1 -Action PREPARE` to verify/stage
only the expected delta and stop before commit/tag/promotion/push; list those
human-owned operations and require `-Action AUDIT` after promotion. Do not
require another PHASE/COMPOSED/FULL when executable inputs are proved unchanged.

# Required artifacts

List durable sources, generated evidence, and the completion report.
Include the bootstrap-impact outcome and rationale/affected paths required by
the verification-level contract, infrastructure-impact assessment, exact
required-level command/exit/log evidence, and existing `GUIDE_IMPACT`. Distinguish
technical verification from author approval; explicitly report incomplete gates.
For closeout work, also record exact `T`/`C` where applicable, readiness and
technical-result paths/hashes, evidence-use/consumability fields, chosen mode,
expected delta/tag/promotion plan and post-promotion audit result. Historical
receipts retain their original cohort and classification.

# Stop conditions

List decisions or failures that require human review before continuing.
Include dirty final-acceptance state, failed/missing readiness, absent explicit
closeout mode, moving-ref or self-referential authority, known post-FULL
lifecycle repair, missing exact-SHA author decision, unexpected closeout delta,
and failed post-promotion audit. ADR 0024 section 11.2 remains exceptional and
cannot waive FULL for a deliberate verification-methodology change.

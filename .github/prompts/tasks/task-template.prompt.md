# GeoCeDG task template

<!-- geocedg-field: objective -->
## Objective

State one concrete outcome.

Declare `CHANGE_ROUTE = ORDINARY | AUTHOR_DIRECT` before implementation.
`AUTHOR_DIRECT` is valid only when the author explicitly authorizes the exact
scope and paths; it is outside `VERIFICATION_CLASS` and must use
`author-direct-change.prompt.md`. Never infer it or select it to bypass a
failure. For `ORDINARY`, declare and freeze `VERIFICATION_CLASS` and the planned
acceptance level. For one developer/integrator, default explicitly to
`BOUNDED_PHASE` and propose `AUTHOR_OPERATED`; later closeout selection remains
an explicit author decision. A higher campaign requires a concrete
`VERIFICATION_ESCALATION_REQUEST` and author authorization before execution.

<!-- geocedg-field: implementation_base -->
## Implementation base

Name the exact commit and tree to which the task applies. Moving branch names
are context, not implementation identity.

## Authority and evidence hierarchy

Reference `AGENTS.md`, current code/build, accepted specifications/ADRs, and
task-specific evidence in order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

List exact files, modules, and observable behavior included.
For an operational-only task, state `PRODUCT_PHASE_EFFECT = NONE` and name the
productive phases that remain unauthorized.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

List adjacent layers and behavior that must remain unchanged.

## Architectural placement

Name the owning layer and explain why it is authoritative.

## Required design/specification

Name the approved specification/ADR or require one before implementation.

## Geometric invariants and degeneracies

Reference governing definitions. State `not applicable` only for a genuinely
non-geometric task.

## Compatibility and serialization

State legacy, file-format, feature-flag, and migration constraints.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Name focused checks and the relevant canonical `tools/agent/verify.ps1`
profile. Distinguish development profiles from final acceptance, name explicit
PHASE selections and record the required semantic and safety coverage. A
smaller development selection never changes the final acceptance gate.

If the commands are intended as final acceptance evidence, require this order:
focused work, clean immutable candidate commit, complete `FINAL -PlanOnly`
resolution, then exactly one authorized `verify.ps1 -Profile FINAL
-LogDirectory <new-external-root>` campaign. Require a complete trusted report
and receipt bound to the exact commit/tree, plan, checker, portable environment,
inputs and evidence. Diagnostics remain separate from acceptance. Do not rerun
FINAL for an unchanged candidate and do not reattribute dirty or precommit
evidence.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

State which planning, implementation, verification and author-decision actions
are authorized. Silence or general agreement is not authorization.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

State whether commit, push, merge, tag and promotion are authorized. Final
acceptance evidence never grants publication authority by itself.

## Acceptance and closeout

State whether the task stops with a candidate pending author review or includes
separately authorized promotion. Author approval is an explicit decision naming
the exact accepted commit; neither a report nor a receipt creates it.

After approval, `phase-closeout.ps1 -Action INSPECT` may validate only the
receipt and exact candidate/approved commit identities. It must not run checks,
repeat FINAL, modify Git, create commits or tags, push, publish or record author
approval. Promotion, when authorized, is a separate exact non-force
fast-forward operation and does not require another FINAL for the unchanged
accepted commit.

## Required artifacts

List durable sources, generated evidence, and the completion report.
Include the bootstrap-impact outcome and rationale/affected paths required by
the verification-level contract, infrastructure-impact assessment, exact
required-level command/exit/log evidence, and existing `GUIDE_IMPACT`. Distinguish
technical verification from author approval; explicitly report incomplete gates.
For closeout work, also record the exact candidate commit/tree, plan/result and
receipt identities, durable evidence location, explicit author-approved SHA and
remote identity before and after any separately authorized promotion.
Historical receipts retain their original cohort and classification.

## Stop conditions

List decisions or failures that require human review before continuing.
Include dirty final-candidate state, incomplete or untrusted coverage, a
missing/invalid receipt, identity mismatch, missing exact-SHA author decision,
remote divergence and any request to mutate Git through closeout. Historical
exceptions cannot waive the current verification contract for a deliberate
verification-methodology change.

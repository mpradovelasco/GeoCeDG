# PRE-G9B-S2 — Construction Text/property and object-font correctness

<!-- geocedg-field: objective -->
## Objective

Implement the author-authorized `PRE-G9B-S2` slice only: give the Desktop
Properties Text editor conventional Apply/OK/Cancel transaction semantics and
allow the GeoCeDG construction-object font setting to reach at least 10 pt
without changing application menu typography or toolbar icon size.

`CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = BOUNDED_PHASE`; final
acceptance is `PHASE -Phase PRE-G9B-S2`. The result remains author-operated and
pending author review.

<!-- geocedg-field: implementation_base -->
## Implementation base

Commit `f502ac8ead5e1b2bf6e70a8583fedfc0a26d9c4a`, tree
`a5bd55820e24a998e5cb4f9c4e39964202b7e0de`, on the published GeoCeDG
`main` after `PRE-G9B-S1`, R1 and R2 were author approved.

## Authority and evidence hierarchy

Follow `AGENTS.md`, current code/tests/build and serialization contracts, the
author-approved
[`pre_g9b_stabilization_deployability_design.md`](../../../docs/architecture/pre_g9b_stabilization_deployability_design.md),
the current roadmap, and this exact author execution contract, in that order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Desktop Properties Text editing lifecycle, embedded `TextInputDialogD`,
  ordinary redefine/update and Undo integration.
- Deterministic draft disposition on Apply, OK, Cancel, selection change,
  Properties tab/panel change and window close.
- The existing GeoCeDG construction-object font-size menu and the minimum
  internal ownership separation needed for 10 pt construction presentation
  without menu-font or toolbar-icon changes.
- Focused tests, additive `PRE-G9B-S2` verification inventory/registry entries,
  truthful candidate-status documentation and modified-upstream provenance.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not implement `PRE-G9B-S3`, `PRE-G9B-S4`, `PRE-G9B-D1`, `PRE-G9B-P1`,
G9B/G9C/G9U2, further G12 or productive G10. Do not add zoom-dependent Text
glyph sizing, independent application presentation controls, a general font
preference framework, toolbar sizing architecture, a parallel editor or
transaction system, new kernel geometry, durable CeDG identity semantics,
format migration, publication, tags or history rewriting.

## Architectural placement

Ownership is Desktop/application/property infrastructure. Shared non-geometric
font-setting utilities may change only where the inherited validation/ownership
coupling demonstrably requires the bounded 10 pt and GUI-isolation correction.
The geometric kernel and construction DAG remain authoritative and unchanged.

## Required design/specification

Use section 4.1 of the approved pre-G9B design and the detailed lifecycle,
redefine, font ownership, persistence and stop contracts in the author
authorization. Prefer the smallest upstream-compatible correction.

## Geometric invariants and degeneracies

No new geometric semantics apply. Preserve constructive dependencies, ordinary
DAG propagation, object continuity, deterministic failure, and the rule that
presentation state is not geometric truth.

## Compatibility and serialization

Use the inherited Text redefine/update mechanism and existing construction-font
persistence scope. Existing `.cedg`/`.ggb` content must load unchanged. Do not
move a setting between document and application preference scope or introduce a
new serialized identity/version contract.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Cover edit/Cancel, edit/OK, edit/Apply, Apply/edit/Cancel,
Apply/edit/OK, selection change, tab/panel change, reopen, practical Undo/Redo
and persistence. Cover 10 pt acceptance/application/persistence, retained
normal values, menu-font isolation and toolbar-icon isolation. Run focused
Desktop tests first, retained affected Properties regressions, checkstyle and
`git diff --check`; run `INFRA_UNIT` after inventory/registry edits. Create a
clean immutable candidate, then run exactly one final
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-S2` against that exact
candidate. Escalate only if actual scope or current governance requires it.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author explicitly authorizes productive S2 implementation and its bounded
verification only. This is not approval of the result and grants no authority
for S3/S4/D1/P1/G9B+ work. `selfApproved=false`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, merge, tag, release,
promotion and any rewrite of published history are forbidden.

## Acceptance and closeout

Stop at
`PRE-G9B-S2 — IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`, or at
`PRE-G9B-S2 — BLOCKED — AUTHOR DISPOSITION REQUIRED` if the bounded ownership
separation cannot be implemented without absorbing S3/S4 or another forbidden
scope. Do not claim author smoke PASS or author approval.

## Required artifacts

Record entry/candidate commit and tree, root cause, lifecycle/disposition,
Undo/redefine behavior, font ownership, files changed, focused and PHASE
evidence, bootstrap/infrastructure/GUIDE impact, smoke checklist, branch/remote
state, ahead/behind, cleanliness and publication/tag state.

## Stop conditions

Stop for dirty entry/final state, remote divergence, non-deterministic or
format-changing behavior without policy, a required kernel semantic change,
unbounded general UI/toolbar architecture, incomplete/untrusted phase coverage,
or any request to begin or publish an unauthorized successor.

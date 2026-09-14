# PRE-G9B-S4-R2 — Persistent user-tool alignment correction

<!-- geocedg-field: objective -->
## Objective

Correct only the remaining visual alignment defect in the unpublished
`PRE-G9B-S4-R1` candidate: persistent user-tool buttons must share the painted
vertical center of adjacent native toolbar tools.

`CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = BOUNDED_PHASE`; final
acceptance remains `PHASE -Phase PRE-G9B-S4`. The result is a corrective
candidate pending author review, never an agent approval.

<!-- geocedg-field: implementation_base -->
## Implementation base

Commit `805419f7ab897920dafbe9aca582ce9895a4d3f3`, tree
`e9c24f5d5f3708bdf3fad675a07e6e37b56e8978`, the unpublished S4-R1 candidate.

## Authority and evidence hierarchy

Follow `AGENTS.md`, current code/tests/build contracts, the author-approved
S2/S3 closeouts, S4/R1 durable design and this exact bounded authorization, in
that order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Reproduce native-versus-persistent component and painted icon geometry in the
  full GeoCeDG toolbar construction path.
- Fix the first demonstrated divergent presentation layer, preferring native
  toolbar membership and removal of special-case layout.
- Add structural regression coverage at 16, 28, 32 and 64 px and the available
  scaled-icon path.
- Record focused verification evidence and a clean local candidate.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not change any S4/R1 preference, default, migration, font ownership, options
layout or Text-dialog behavior. Do not change S2/S3 semantics, user-tool
identity/persistence/execution, toolbar groups, icon resources, document format,
D1, P1, G9B/G9C/G9U2, further G12 or productive G10. Do not push or tag.

## Architectural placement and invariant

This is Desktop toolbar presentation only. Native buttons are the geometry
authority. Persistent buttons must reuse their button presentation and toolbar
row membership; deterministic painted centers must agree without a fixed pixel
translation or DPI-dependent drift.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Run the focused full-toolbar alignment test first, retained S4/R1 tests,
relevant high-DPI/flyout tests, Desktop checkstyle and `git diff --check`.
Commit a clean exact candidate, then run exactly one final
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-S4`. Escalate only if the
actual scope requires it.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author explicitly authorizes productive S4-R2 alignment correction only.
This is not approval of the result. `selfApproved=false`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local corrective candidate commit is authorized. Push, tag, merge,
release, promotion and published-history rewriting are forbidden.

## Acceptance and stop conditions

Stop at `PRE-G9B-S4-R2 — CORRECTIVE IMPLEMENTATION CANDIDATE` and
`PRE-G9B-S4 — FINAL CORRECTED CANDIDATE — PENDING AUTHOR REVIEW`. Stop if a
toolbar architecture redesign or non-deterministic platform workaround is
required. Do not begin D1 and do not claim author smoke PASS.

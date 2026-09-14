# PRE-G9B-S4 — Independent application presentation sizing

<!-- geocedg-field: objective -->
## Objective

Implement the author-authorized `PRE-G9B-S4` slice only: provide independent,
bounded GeoCeDG application preferences for menu font, toolbar icon, Algebra
font, Construction Protocol font and shared Graphics/Graphics2 UI font sizing.
These are frontend presentation preferences, never construction geometry.

`CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = BOUNDED_PHASE`; final
acceptance is `PHASE -Phase PRE-G9B-S4`. The result remains author-operated and
pending author review.

<!-- geocedg-field: implementation_base -->
## Implementation base

Commit `bd39099f668de81e9e65c415b28995de7b1c846b`, tree
`f41cd5050ff013b1f073e5754fe1ccf3feb96a98`, on the then-current published
GeoCeDG `main` after `PRE-G9B-S3 — PASS — AUTHOR APPROVED`.

## Authority and evidence hierarchy

Follow `AGENTS.md`, current code/tests/build and persistence contracts, the
author-approved
[`pre_g9b_stabilization_deployability_design.md`](../../../docs/architecture/pre_g9b_stabilization_deployability_design.md),
the S2/S3 approved closeouts, the normative
[`application-presentation-sizing.md`](../../../geocedg/specs/ui/application-presentation-sizing.md),
and this exact author execution contract, in that order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Existing Desktop application preference storage, font/view update hooks,
  Advanced options surface and GeoCeDG menu/toolbar/view adapters.
- Five independently stored values: menu font, toolbar icon, Algebra font,
  Construction Protocol font and one Graphics/Graphics2 UI font.
- Conservative fallback from inherited effective presentation values, live
  component refresh, restart persistence and default/reset behavior.
- Minimal behavior-neutral upstream seams needed to preserve Classic behavior
  and separate S2/S3 construction Text ownership.
- Focused tests, additive `PRE-G9B-S4` verification inventory/registry entries,
  truthful candidate documentation and modified-upstream provenance.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not implement `PRE-G9B-D1`, `PRE-G9B-P1`, G9B/G9C/G9U2, further G12 or
productive G10. Do not introduce general theming, new construction semantics,
document-format fields, a second preference framework, per-Graphics-view
persistence, toolbar taxonomy changes, icon/font coupling, S3 scaling-law
changes, platform hacks, publication, tags or published-history changes.

## Architectural placement and ownership

Ownership is Desktop/application presentation. The five values use the existing
user preference backend and a GeoCeDG-specific model. Shared/Desktop upstream
seams retain their old defaults for Classic. Construction-object base font,
GeoText logical size and S3 world-Text zoom scaling remain independent.

## Required design/specification

Use section 4.3 of the approved pre-G9B design and the normative S4 UI
specification. Graphics UI font owns inherited Euclidian axes, coordinates and
object-label presentation, but not `GeoText` logical or rendered construction
font ownership. Graphics and Graphics2 share one stored value.

## Compatibility and persistence

Store S4 values as application/user preferences, never `.cedg`/`.ggb` data.
When a new S4 key is absent or invalid, capture and materialize the corresponding
inherited effective value once using the supported application-size list, so the
first S4 launch preserves appearance and subsequent categories are independent.
Do not overwrite valid explicit values.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Cover all five independent setters, live component/model refresh, unchanged
unrelated categories, startup fallback, persistence/reload, no construction
dirty state, Classic-neutral defaults, retained S2 10 pt ownership and retained
S3 Text scaling/logical-font behavior. Run focused tests first, retained S2 and
S3 selections, relevant Desktop options/preferences tests, touched-scope
checkstyle and `git diff --check`; run `INFRA_UNIT` after inventory/registry
edits. Create a clean immutable candidate, then run exactly one final
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-S4` against that exact
candidate. Escalate only if actual scope or current governance requires it.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author explicitly authorizes productive S4 implementation and bounded
verification only. This is not approval of the result and grants no authority
for D1/P1/G9B+ work. `selfApproved=false`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, merge, tag, release,
promotion and any rewrite of published history are forbidden.

## Acceptance and closeout

Stop at `PRE-G9B-S4 — IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`, or at
`PRE-G9B-S4 — BLOCKED — AUTHOR DISPOSITION REQUIRED` if correct separation
requires general theming, construction/document semantics, S3 changes, renewed
icon/font coupling, broad upstream UI work or non-deterministic platform hacks.
Do not claim author smoke PASS or author approval, and do not begin D1.

## Required artifacts

Record entry/candidate commit and tree, inherited and resulting ownership,
every preference/default/fallback, live and restart behavior, S2/S3 containment,
productive paths, focused and PHASE evidence, bootstrap/infrastructure/GUIDE
impact, author smoke checklist, branch/remote/ahead-behind/cleanliness and
publication/tag state.

# PRE-G9B-S4-R1 — Presentation sizing polish and UI ownership completion

<!-- geocedg-field: objective -->
## Objective

Correct the author-reported first-smoke findings on the unpublished
`PRE-G9B-S4` candidate. Preserve S2 construction-Text ownership and S3 zoom
scaling while completing the bounded Desktop presentation preference model,
Text-dialog controls and persistent user-tool alignment.

`CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = BOUNDED_PHASE`; final
acceptance remains `PHASE -Phase PRE-G9B-S4`. The result is a corrective
candidate pending author review, never an agent approval.

<!-- geocedg-field: implementation_base -->
## Implementation base

Commit `8ec5d3eaf17a18f08e9484a37790d54791ef255b`, tree
`fb955194107594bcb3e632ab909af35d60e6a4e2`, the unpublished S4 implementation
candidate produced from published `main` entry
`bd39099f668de81e9e65c415b28995de7b1c846b`.

## Authority and evidence hierarchy

Follow `AGENTS.md`, current code/tests/build and serialization contracts, the
author-approved pre-G9B design, the approved S2/S3 closeouts, the S4
presentation-sizing specification and this exact author corrective
authorization, in that order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Add the independent GeoCeDG **General UI font size** user preference and bind
  the residual inherited Desktop GUI-font surfaces to it.
- Adopt fresh GeoCeDG defaults 12/14/28/12/12/12 for General UI, Menu, Toolbar,
  Algebra, Construction Protocol and Graphics while conservatively preserving
  explicit and migrated existing values.
- Align the Presentation Sizes label/value columns with a normal Swing layout.
- Relabel the S2 construction control **Text font size**.
- Restore Apply/OK/Cancel/Help and coherent button sizing in the Text dialog,
  retaining the approved S2 lifecycle.
- Remove toolbar-size leakage into non-toolbar icons and correct persistent
  user-tool alignment using native container geometry.
- Focused tests, additive inventory updates, truthful candidate documentation,
  guide/provenance records and bounded verification.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not implement D1, P1, G9B/G9C/G9U2, further G12 or productive G10. Do not
change S2 Text semantics, S3 scaling law, construction/document serialization,
toolbar taxonomy/actions, theme/colors, general Swing theming, packaging,
licensing or feature-default promotion. Do not push, tag, merge or publish.

## Architectural placement

Ownership remains Desktop application/frontend presentation. General UI is the
bounded residual owner implemented through the inherited GUI-font machinery.
Specific S4 owners take precedence over that fallback. Toolbar icon pixels are
toolbar presentation, not application-wide icon size. Construction Text and
its zoom law remain outside this model.

## Required design/specification

Apply [`application-presentation-sizing.md`](../../../geocedg/specs/ui/application-presentation-sizing.md).
Use the existing `GeoGebraPreferencesD`, `FontSettings`, options, toolbar and
Text-dialog infrastructure; do not create parallel frameworks.

## Compatibility and migration

Fresh means no S4 key and no legacy saved user-preferences XML. Fresh missing
values use the approved defaults. Any valid stored category is explicit and is
preserved. A missing/invalid category in an existing profile captures only its
own effective inherited value. Never infer one category from another after
initialization. Classic is behavior-neutral; `.cedg`/`.ggb` formats are
unchanged.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Cover fresh defaults, explicit/missing migration, aligned grid structure,
Text-font label ownership, Text-dialog button presence/lifecycle/height,
General UI on Input Bar/help/dialog surfaces, toolbar help, persistent-tool
alignment at 16/28/32/64 and available scaled rendering, the full independence
matrix, retained S2 and retained S3. Run focused tests, relevant compilation,
checkstyle and `git diff --check`; run `INFRA_UNIT` after inventory changes.
Commit a clean exact candidate, then run exactly one final
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-S4`. Escalate only if the
actual scope requires it.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author explicitly authorizes productive S4-R1 correction only. This is not
approval of the result. `selfApproved=false`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local corrective candidate commit is authorized. Push, tag, merge,
release, promotion and published-history rewriting are forbidden.

## Acceptance and stop conditions

Stop at `PRE-G9B-S4-R1 — CORRECTIVE IMPLEMENTATION CANDIDATE` and
`PRE-G9B-S4 — CORRECTED IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`.
Stop for any required theme rewrite, construction semantic/format change, S3
law change, font/icon recoupling, platform-specific nondeterminism or scope
expansion. Do not begin D1 automatically and do not claim author smoke PASS.

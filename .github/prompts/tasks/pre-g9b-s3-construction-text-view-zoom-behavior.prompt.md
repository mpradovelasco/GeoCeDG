# PRE-G9B-S3 — Construction Text view-zoom behavior

<!-- geocedg-field: objective -->
## Objective

Implement the author-authorized `PRE-G9B-S3` slice only: make GeoCeDG
construction `Text` whose inherited placement mode is Euclidian/world-relative
scale visually with its owning Euclidian view, while preserving screen-fixed
Text, construction identity, content, anchors, persistence and the logical base
font size established by S2.

`CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = BOUNDED_PHASE`; final
acceptance is `PHASE -Phase PRE-G9B-S3`. The result remains author-operated and
pending author review.

<!-- geocedg-field: implementation_base -->
## Implementation base

Commit `e076648b8879924abe7f7e1594a2a6bc64b12c4c`, tree
`8b3d8e5702aad050396b93843a97dcc0f676d107`, on the published GeoCeDG
`main` after `PRE-G9B-S2 — PASS — AUTHOR APPROVED`.

## Authority and evidence hierarchy

Follow `AGENTS.md`, current code/tests/build and serialization contracts, the
author-approved
[`pre_g9b_stabilization_deployability_design.md`](../../../docs/architecture/pre_g9b_stabilization_deployability_design.md),
the current roadmap,
[`construction-text-view-scaling.md`](../../../geocedg/specs/ui/construction-text-view-scaling.md),
and this exact author execution contract, in that order.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- GeoCeDG-only Euclidian presentation of non-absolute-screen `GeoText` through
  the shared `DrawText` seam with a behavior-neutral Classic default.
- Per-view effective font derivation, ordinary/multiline/LaTeX layout,
  background/highlight bounds, hit testing and view-change invalidation.
- Graphics and Graphics2 behavior derived independently from each view.
- Focused tests, a deterministic manual fixture, additive `PRE-G9B-S3`
  verification inventory/registry entries, truthful candidate documentation
  and modified-upstream provenance.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not implement `PRE-G9B-S4`, `PRE-G9B-D1`, `PRE-G9B-P1`, G9B/G9C/G9U2,
further G12 or productive G10. Do not change menu, Algebra, Construction
Protocol or general GUI fonts; toolbar icon sizing; construction/DAG semantics;
`GeoText` identity, content, anchor or logical font ownership; document-format
semantics; absolute-screen Text scale; general zoom behavior; LaTeX architecture;
publication, tags or published history.

## Architectural placement and scale contract

This is a Euclidian view/presentation behavior. `GeoText` keeps the inherited
logical font calculation. A drawable in a GeoCeDG view derives its effective
font as `logicalFont * (view.xscale / EuclidianView.SCALE_STANDARD)`.
`SCALE_STANDARD` is the inherited 50-pixel-per-unit reference, and `xscale` is
the inherited primary Euclidian `scale`; `yscale` remains independent and does
not stretch glyphs. The scale is recalculated from current view state, never
creation history, and is not stored in the object.

## Required design/specification

Use section 4.2 of the approved pre-G9B design and the normative S3 UI
specification. Preserve the inherited `isAbsoluteScreenLocActive()` mode as the
scope discriminator. Stop if correct behavior requires a creation-time scale,
new persistence, viewport authority in the kernel, anisotropic text or a broad
LaTeX redesign.

## Geometric invariants and degeneracies

View scale may affect only rendered glyph/layout dimensions and corresponding
screen hit bounds. It must not alter the anchor point, dependencies, identity,
content, logical font multiplier, XML or undo identity. Independent views may
render different pixel sizes. Backend font rasterization may quantize extreme
sizes, but no additional semantic clamp or stored scale is introduced.

## Compatibility and serialization

The behavior is enabled only by the GeoCeDG application configuration. Other
application configurations retain the inherited font calculation. Existing
`.cedg`/`.ggb` Text loads unchanged; no new XML field is permitted. Zoom alone
must not mutate serialized Text state or create a construction undo entry.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Cover zoom-in/out bounds, fixed world anchor, unchanged logical font/XML,
ordinary and multiline Text, LaTeX, background/hit bounds, nonuniform axes,
independent per-view derivation, absolute-screen containment, Classic
containment and retained S2 10 pt/font ownership. Run focused shared tests first,
retained S2 regressions, touched-scope checkstyle and `git diff --check`; run
`INFRA_UNIT` after prompt/inventory/registry edits. Create a clean immutable
candidate, then run exactly one final
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-S3` against that exact
candidate. Escalate only if actual scope or current governance requires it.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author explicitly authorizes productive S3 implementation and bounded
verification only. This is not approval of the result and grants no authority
for S4/D1/P1/G9B+ work. `selfApproved=false`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, merge, tag, release,
promotion and any rewrite of published history are forbidden.

## Acceptance and closeout

Stop at
`PRE-G9B-S3 — IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`, or at
`PRE-G9B-S3 — BLOCKED — AUTHOR DISPOSITION REQUIRED` if the bounded view-layer
contract cannot satisfy ordinary and LaTeX Text consistently. Do not claim
author smoke PASS or author approval, and do not begin S4.

## Required artifacts

Record entry/candidate commit and tree, inherited Text-mode characterization,
applicability and scalar contracts, ordinary/LaTeX/bounds/multi-view/persistence
behavior, S2 containment, files changed, focused and PHASE evidence,
bootstrap/infrastructure/GUIDE impact, smoke fixture/checklist, branch/remote
state, ahead/behind, cleanliness and publication/tag state.

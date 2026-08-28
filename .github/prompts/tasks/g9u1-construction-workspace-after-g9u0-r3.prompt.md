# Objective

Implement the manifest-defined **CeDG Construction** workspace after the
separately approved closeout of `G9U0-R3 — PUBLIC LOCUS V2 UI EXPOSURE
HARDENING`.

**PROPOSED FUTURE SUCCESSOR PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

This prompt prospectively supersedes
`.github/prompts/tasks/g9u1-construction-workspace.prompt.md` for any future
G9U1 execution. The historical prompt remains immutable G9P evidence with
canonical-LF SHA-256
`502dabbac1f756e01d0f7935a337e389a3c5e26eaabf3452a6ffe953e83b6ddd`.

Mandatory entry conditions are all of:

- `G9U0`, `G9U0-R1`, `G9X1`, `G9U0-R2` and `G9U0-R3` are
  `PASS — AUTHOR APPROVED` on the current clean main;
- the annotated phase tags, ancestry, current remote main and composed
  verification agree with the repository authority;
- ADR 0012 remains Accepted, ADR 0016 remains Accepted, and the normative
  workspace, Locus V2 presentation/public-surface and native-document
  contracts are current and mutually consistent;
- the author has separately authorized this exact successor prompt after
  reviewing its hash; and
- the author-provided top-bar and startup assets are either present with
  approved provenance or each explicitly recorded as a bounded UI/branding
  review item under the fallback policy below.

An R3 PASS does not authorize G9U1 automatically. At entry, record the exact
prompt SHA, branch/base, modified-file boundary and verification baseline.

# Authority and evidence hierarchy

Use current repository code, tests and serialization contracts first; then
Accepted ADRs 0012 and 0016 and the normative contracts under
`geocedg/specs/ui/`, `geocedg/specs/locus/` and
`geocedg/specs/operations/`; then approved G9U0/R1/R2/R3, G9X1 and G9P
evidence. Generated screenshots, package outputs and previous summaries are
evidence only.

Read the current application-profile schema and compiler, `AppGeoCeDG`,
Desktop layout/perspective/menu/controller code, RuntimeFeatureService,
resource and provenance manifests, Windows package profile and builder, and
the approved reference-workflow audit before design or code.

Preserve the historical G9U1 prompt and G9P prompt catalog as historical
evidence. This successor is the only future G9U1 execution authority after R3;
do not maintain two live workspace prompts.

# Scope

Implement application-profile schema version 2, deterministic version-1
migration, one manifest-owned action registry, named workspaces, views/docking,
bottom input and contextual help, toolbar/menu groups, maturity and feature
availability, localization, accessibility, workspace preferences, saved-layout
policy and workspace switching.

The default **CeDG Construction** workspace remains professional and
non-minimal. Its manifest/action registry must represent at least these approved
families, grouping or overflowing actions where needed without losing
discoverability:

1. Inspect and construct;
2. Linear geometry;
3. Parameters and drivers;
4. Relations and intersections;
5. Circles, conics and curves;
6. Locus V2;
7. Metrics and validation;
8. Transformations and manual projections;
9. CeDG procedures and developments placeholders under their independent
   unavailable gates;
10. Presentation and document; and
11. Automation and import/export.

Expose the already approved Locus V2 and DXF services as frontend clients. The
R3-fixed GeoCeDG menu lifecycle and public rich-result inspector must remain
accessible through the same action registry and the sole existing Locus V2
runtime opt-in. Workspace membership affects presentation and discoverability,
not command meaning or feature policy.

Add the approved rich-intersection presentation flow:

```text
Intersect(L,T)
 -> rich result remains semantic authority
 -> transient markers for the active/selected rich result, default ON in
    CeDG Construction
 -> click/rank preselects an already admissible exact token
 -> explicit action materializes one ordinary GeoPoint
 -> optional explicit "create all admissible points"
 -> never automatic persistent GeoPoints
```

Only currently finite and point-admissible solutions receive markers. Inactive
historical results do not pollute Graphics. A workspace/user preference may
turn markers off, but the CeDG Construction default for the active-result
context is on unless later author review changes it.

Define a restrained, professional GeoCeDG frontend identity through normal
application seams: application/window identity, reviewable accent/theme tokens,
workspace-identifying chrome, grouping/spacing, contextual help/status,
active-workspace indication, accessible contrast and normal DPI/scaling. CeDG
Construction must be immediately distinguishable from the GeoCeDG Classic
diagnostic route without using presentation as geometric authority. Permanent
palette choices remain subject to author visual review.

Provide one clean brand-resource seam with two distinct stable logical roles:

- `geocedg.brand.topbar`: GeoCeDG frontend/top-bar and product-chrome identity
  only; it has no geometric or document authority. The author's intended source
  filename is `helixTopBar.png`.
- `geocedg.brand.startup`: startup/application identity. After visual
  suitability and platform requirements are verified, it may be the source for
  deterministic application/window/package/file-association icon derivatives.
  The author's intended source filename is `helixSnapshot.png`; never assume a
  startup image is automatically legible or suitable at 16 x 16 or 32 x 32.

The existing `geocedg/resources/assets-manifest.yml` remains the single
provenance authority. For each role, record the supplied filename, SHA-256,
pixel dimensions, format/alpha characteristics, ownership, provenance and
license/review state. Establish exactly one canonical resource authority per
logical role. Deterministically derive only the inspected size/platform assets
that are actually required, recording the derivation tool/version/options and
every derived hash; do not maintain duplicate hand-copied packaging resources.
Do not invent a physical durable source path before the supplied assets and
current resource convention determine it.

Use the top-bar role only for the supported frontend/top-level chrome seam. Use
the startup role for startup/application identity and only for validated
window/frame, Windows package/app and `.cedg` association derivatives. Neither
role is a geometric toolbar icon or document authority. If either author asset
is absent at implementation start, use the existing bounded text/default
fallback for that role, retain its explicit manifest slot and report the
omission. Do not fabricate or substitute a logo. Final G9U1 PASS should require
both author assets when their public frontend and Windows packaging/icon roles
are claimed in scope; otherwise those claims remain incomplete for author
review. GeoCeDG Classic retains its separate diagnostic visual identity.

# Explicitly forbidden scope

Do not implement or change geometric semantics, commands, algorithms,
intersection solving, exact-token identity, Locus V2 topology, metric
authority, render tessellation, persistence semantics, G9B/G9C spatial
semantics, G9U2 procedures or productive G10 work.

Do not:

- make a candidate marker a `GeoElement`, durable ID, XML record, Construction
  Protocol entry, DAG node, undo object, semantic revision or token identity;
- create persistent points automatically from `Intersect(L,T)`;
- identify a solution by coordinate, list order, screen proximity or marker
  identity;
- show markers for stale solutions, unresolved candidates, overlap-only
  evidence, non-point-admissible roots or ambiguous/inadmissible tokens;
- create a second hard-coded toolbar/menu/action authority or restore scattered
  product-menu definitions;
- promote reference macros, disabled procedure placeholders or experimental
  semantics merely through workspace visibility;
- copy upstream branding, trademarks, installer resources or reference-file
  assets;
- invent, generate, substitute or permanently freeze an application logo or a
  permanent palette without author review; or
- turn GeoCeDG Classic into an in-process workspace or erase its diagnostic
  visual distinction.

# Architectural placement

Workspace schema/compiler, action registry, menus/toolbars, selection
transactions, overlays, theme tokens, the two brand-role resolutions and
preferences belong to the Desktop/application frontend. Package-icon and
`.cedg` association consumption belong to the existing Windows packaging seam.
Resource ownership and hashes belong to the existing GeoCeDG asset manifest.

Rich intersection results and exact tokens remain shared-kernel semantic
authority. Candidate markers are a presentation overlay derived from the
active result's already admissible exact tokens. Zoom, DPI, camera and screen
state may determine placement, clipping, hit target and ranking among those
tokens; they never establish or change token identity.

The manifest/action registry is the single declarative authority for action
IDs, placement, localization/help, icon references, feature requirements and
unavailable policy. Classic remains a separate process/path and preference
namespace.

# Required design/specification

Before productive edits, reconcile the current source against ADRs 0012/0016,
the normative CeDG workspace and document-identity contracts, the post-R3
public Locus surface, the Windows packaging contract and the resource manifest.
Record:

- strict schema-v2 and deterministic v1-migration design;
- stable action IDs and complete mappings for all eleven professional groups;
- typed selection and cancellation contracts for each constructive action;
- marker overlay ownership, active-result lifecycle, admissibility filtering,
  hit/rank behavior, preference/default policy and explicit one/all-point
  materialization transactions;
- presentation-purity, saved-layout and Classic/Laboratory boundaries;
- GeoCeDG visual tokens, contrast/scaling policy and Classic distinction;
- `geocedg.brand.topbar` and `geocedg.brand.startup` resolution, independent
  provenance/fallback, suitability review, deterministic derivation and
  packaging consumption without duplicate resource authorities;
- localization, help, keyboard and accessibility behavior; and
- modified upstream seams, rejected alternatives and deterministic validation
  plan.

If current approved specs contradict current product behavior or this
author-approved direction, stop before code and report the exact normative
conflict. Do not resolve it by silently changing semantics or historical
evidence.

# Geometric invariants and degeneracies

Workspace switches, visual identity, theme/accent changes, marker visibility,
hover, selection and DPI/zoom changes produce zero Construction/DAG/semantic
updates. The rich result remains authority through finite, stale, unresolved,
overlap, ambiguous and inadmissible states.

Marker hit testing may preselect only a token already supplied as currently
admissible by the rich result. Explicit materialization passes that exact token
to the existing exact-token construction path without re-solving or fallback.
One-point and create-all actions are explicit and undoable; cancel and marker
inspection create no persistent object. Genuine topology, continuation and
token lifecycle semantics remain those of the approved G9U0/R1/R2/R3 kernel
surface.

No color, icon, highlight, screen position, label, order or proximity is
semantic identity. Disabled or unavailable spatial/procedure actions expose a
localized reason and no speculative semantic output.

# Compatibility and serialization

`.cedg` remains the native GeoCeDG document identity and `.ggb` compatibility
input. Retain the validated ZIP/XML machinery and `app="classic"`; workspace,
markers, visual identity and either brand role do not change document geometry
or infer semantics from a filename.

Marker overlays have no XML, durable ID, DAG/Protocol record or undo state.
Materialized points and their hidden opaque exact-token dependencies keep the
existing R3-validated save/reopen, copy/remap and undo/redo behavior. Loading a
document with supported rich results reconstructs semantic authority; overlay
presentation is recomputed only for the active result under current workspace
preference.

Legacy saved perspectives remain explicit Document layout state. GeoCeDG
Classic opens and preserves supported `.cedg` without experimental creation
authority or full Construction branding. External upstream open for unknown
GeoCeDG types remains unsupported; no lossy downgrade is introduced.

The author top-bar and startup assets and their reviewed derivatives are product
assets, not document or geometric authority. Packaging may consume a validated
startup-role Windows ICO for the application and `.cedg` association, but
GeoCeDG must not claim `.ggb`; the top-bar role is not a packaging identity.

# Required tests and commands

Add focused G9U1 tests and deterministic evidence for schema validation,
version-1 migration, action references and the one-authority static contract;
all eleven group mappings and stable action IDs; views/docking/help,
preferences, saved-layout precedence, feature-unavailable policy, localization,
keyboard/accessibility and Classic/Laboratory isolation.

Rich-intersection presentation tests must prove:

- the active/selected result shows only finite point-admissible markers by
  default and an inactive result shows none;
- stale, overlap-only, unresolved, ambiguous and inadmissible solutions show
  none;
- zoom/DPI/view changes do not alter exact identity;
- click/ranking selects only an existing exact token;
- cancel creates nothing; explicit one-point materialization is exact and
  normally undoable; explicit create-all creates exactly the currently
  admissible set as an undoable user action;
- `Intersect(L,T)` alone still creates zero persistent points; and
- no marker appears in XML, the DAG, Construction Protocol, copy closure or
  undo history.

Visual-identity tests must prove that CeDG Construction is visibly
distinguishable from Classic, switching style/workspace changes no Construction
state, accent/theme changes produce zero DAG updates, contrast/accessibility
requirements pass and normal DPI/scaling preserves usability.

Asset tests must prove each author source filename, provenance, SHA-256,
dimensions and format/alpha record; independent resolution of
`geocedg.brand.topbar` and `geocedg.brand.startup`; startup-role suitability at
every claimed icon size; deterministic derived hashes; Windows package and
`.cedg` association consumption where claimed; absence of duplicate hand-copied
packaging assets or upstream substitution; per-role fallback behavior; and
retained Classic distinction.

Menu/tool tests must prove every approved professional group is represented,
all action IDs/localization/help/feature policies resolve, there is no duplicate
hard-coded authority, and the R3-fixed menu/inspector survives initialization,
repeated font updates and language refresh.

Rerun G9U0, G9U0-R1, G9U0-R2, G9U0-R3, G9X1, G5, relevant G9A, legacy Locus,
frontend/profile/localization, Desktop/document persistence, packaging when
affected, relevant Checkstyle, `git diff --check`, `git diff --cached --check`
and the full `tools/agent/verify.ps1` without weakening any gate. Require two
matching focused executions and terminal
`All GeoCeDG verification gates passed.` Generated evidence belongs only below
ignored `artifacts/` except the repository's approved compact durable records.

Manual author review must cover professional workflow/density, markers and
materialization, accessibility/scaling, Construction-versus-Classic visual
identity, final palette, the top-bar source in frontend chrome and every claimed
startup-role application/package derivative.

# Required artifacts

Produce the approved schema/profile/action registry and minimal frontend and
packaging changes; focused tests and verifier integrated after R3; scenario
matrix, exact evidence/hashes, implementation architecture/report, modified-file
registry and living roadmap/traceability; owned localization/help/theme
resources; and the two author-asset provenance/derivation records only when the
corresponding real sources were supplied.

Screenshots, installed-package probes and generated icon derivatives outside
durable reviewed resources remain generated evidence. Historical prompts,
reports, tags and reference GGBs remain unchanged. User/developer guides may
claim only behavior actually validated in the implementation candidate.

# Stop conditions

Stop before or during implementation if:

- R3 is not `PASS — AUTHOR APPROVED`, this exact successor is not separately
  authorized, entry authority is dirty/inconsistent, or its hash changed;
- a workspace/menu repair needs another action authority or requires kernel,
  command, solver, token, identity or serialization changes;
- marker UX cannot remain a transient overlay over existing admissible tokens;
- an exact point would require coordinate/order/proximity identity, re-solving
  or automatic creation;
- switching, theme, icon or marker presentation mutates Construction state;
- the resource provenance/license boundary is unclear or an upstream asset
  would need to be copied;
- either required author asset is absent while its final public
  branding/packaging PASS is being claimed;
- Classic preservation/distinction or native `.cedg` behavior would regress;
- any approved historical or composed gate fails unexpectedly; or
- completion would require G9B, G9C, G9U2 or productive G10 implementation.

Do not commit, push, merge, tag, self-approve or continue beyond the authority
granted by the future author instruction. The implementation task must stop at
`G9U1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW` with
`selfApproved=false`, `authorApproved=false` and `passClaimed=false`.

Until such separate authorization, the terminal state remains:

```text
G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

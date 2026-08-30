# Objective

Implement the manifest-defined **CeDG Construction** workspace only after the
separately approved closeouts of `G9U0-R3 — PUBLIC LOCUS V2 UI EXPOSURE
HARDENING`, `G9U0-R4 — PUBLIC LOCUS V2 INTERSECTION INITIAL ADMISSIBILITY AND
CONTINUATION CORRECTION` and `G9U0-R5 — LOCUS V2 2D SIMILARITY
TRANSFORMATIONS`.

**PROPOSED FUTURE SUCCESSOR PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Current planning status remains closed: G9U0-R4 is `PASS — AUTHOR APPROVED`
after the retained failed intermediate smokes and the final four-root regular-
motion and existing-point reactivation smokes both passed. G9U0-R5 and G9U1
have not been executed. The accepted R4 periodic phase-tube and materialized-
token dormancy/reactivation contract below is input authority, not authorization
to start this prompt.

This prompt prospectively supersedes
`.github/prompts/tasks/g9u1-construction-workspace.prompt.md` for any future
G9U1 execution. The historical prompt remains immutable G9P evidence with
canonical-LF SHA-256
`502dabbac1f756e01d0f7935a337e389a3c5e26eaabf3452a6ffe953e83b6ddd`.

Mandatory entry conditions are all of:

- `G9U0`, `G9U0-R1`, `G9X1`, `G9U0-R2`, `G9U0-R3`, `G9U0-R4` and
  `G9U0-R5` are `PASS — AUTHOR APPROVED` on the current clean main;
- the annotated phase tags, ancestry, current remote main and composed
  verification agree with the repository authority;
- ADR 0012, ADR 0016 and ADR 0017 remain Accepted, the author-approved R4
  intrinsic semantic phase/rank decision remains current, and the normative
  workspace, Locus V2 presentation/public-surface, intersection-identity and
  native-document contracts are mutually consistent;
- the author has separately authorized this exact successor prompt after
  reviewing its hash; and
- the author-provided top-bar and startup assets are either present with
  approved provenance or each explicitly recorded as a bounded UI/branding
  review item under the fallback policy below.

No predecessor PASS authorizes G9U1 automatically. At entry, record the exact
prompt SHA, branch/base, modified-file boundary and verification baseline.

# Authority and evidence hierarchy

Use current repository code, tests and serialization contracts first; then
Accepted ADRs 0012, 0016 and 0017 and the
normative contracts under
`geocedg/specs/ui/`, `geocedg/specs/locus/` and
`geocedg/specs/operations/`; then approved G9U0/R1/R2/R3/R4/R5, G9X1 and G9P
evidence. Generated screenshots, package outputs and previous summaries are
evidence only.

Read the current application-profile schema and compiler, `AppGeoCeDG`,
Desktop layout/perspective/menu/controller code, RuntimeFeatureService,
resource and provenance manifests, Windows package profile and builder, the
host `Kernel` continuity setting and XML/preference lifecycle, the Desktop
Advanced-settings control, and the approved reference-workflow audit before
design or code.

The approved host characterization to reverify at entry is exact and bounded:

- `org.geogebra.common.kernel.Kernel` owns the sole `continuous` boolean
  (default `false`) and the existing `isContinuous()` / `setContinuous(boolean)`
  API;
- `Kernel` writes `<kernel><continuous val="..."/></kernel>` and
  `org.geogebra.common.io.MyXMLHandler.handleKernelContinuous(...)` reads that
  same document/preference XML through `Kernel.setContinuous(...)`;
- `org.geogebra.common.main.GeoGebraPreferencesXML` supplies the factory
  default `<continuous val="false"/>`, while Desktop
  `GeoGebraPreferencesD.loadXMLPreferences(AppD)` loads saved preference XML
  through the normal `App.setXML(...)` / `MyXMLHandler` route; and
- Desktop `OptionsAdvancedD` owns the existing on/off radio controls, reads
  `Kernel.isContinuous()` and currently writes `Kernel.setContinuous(...)`
  followed by Construction update/unsaved handling.

This is one host setting shared by document XML and XML preferences, not two
independent values. Reverify these exact seams against the then-current R4/R5
main before implementation; if they have materially changed, stop and update
the prospective design instead of guessing.

Preserve the historical G9U1 prompt and G9P prompt catalog as historical
evidence. This successor is the only future G9U1 execution authority after R5;
do not maintain two live workspace prompts.

# Scope

Implement application-profile schema version 2, deterministic version-1
migration, one manifest-owned action registry, named workspaces, views/docking,
bottom input and contextual help, toolbar/menu groups, maturity and feature
availability, localization, accessibility, workspace preferences, saved-layout
policy and workspace switching.

Enforce the GeoCeDG deterministic product policy through the host's existing
kernel **Continuity** option. In the GeoCeDG product profile and CeDG
Construction application, `Continuity = OFF` is an invariant, not a workspace
preference: fresh launch, restart, workspace switching and restored UI state
must all leave the existing kernel option off. Do not introduce a second
continuity concept or persistence field. The separate GeoCeDG Classic
diagnostic route retains the upstream user-configurable Continuity behavior.

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

Add the prospective author-directed rich-intersection presentation flow below,
consuming the final author-approved R4 identity/evidence contract:

```text
Intersect(L,T)
 -> current deterministic rich result remains semantic authority
 -> every current finite solution exposes separately:
      deterministic-identity status
      current topology-certificate status
      local numerical/topological evidence class
      global completeness
      current exact-token point-admissibility
 -> transient markers for the active/selected rich result, default ON in
    CeDG Construction, distinguish exact-token eligible and
    nonmaterializable candidates
 -> marker hit testing preselects only an already identified exact kernel token
 -> explicit create-one, create-selected and create-all consume the exact
    selector/token of every selected/current eligible solution
 -> optional user-enabled auto mode executes one second visible frontend
    materialization transaction over that exact eligible-token snapshot after
    an explicit Intersect action
 -> later recomputation never creates persistent GeoPoints
 -> an existing token-selected GeoPoint may become undefined while its exact
    allocation is dormant, and the same GeoPoint may reactivate only when the
    same selector again resolves uniquely
```

Only current finite solutions with truthful current presentation evidence may
receive markers. Only a root already carrying a unique deterministic semantic
selector/exact token is selectable; no frontend policy may override identity
ambiguity, the current R4 topology certificate or numerical/topological
evidence. A retained dormant token is not a current selectable token. Markers
never run a second previous-frame or continuous-tracking heuristic. Inactive
historical results do not pollute Graphics. A workspace/user preference may
turn markers off, but the CeDG Construction default for the active-result
context is on unless later author review changes it.

The intrinsic semantic phase/rank approved by R4 is shared-kernel identity
evidence inside the exact selector. It is not a presentation ordinal. The
workspace, marker overlay, chooser and action registry may neither calculate,
renumber nor persist it. UI list order and marker order are never selector
authority; frontend hit testing can only return one exact selector/token already
published by the current rich-result snapshot.

The adaptive intrinsic periodic phase-tube/cell certificate is separate
revision-scoped kernel topology evidence. It excludes false invalidation from a
fixed update-size fraction while failing closed for an affected true
seam/monodromy shift. G9U1 may consume its current outcome only; it may not
reconstruct phase tubes, treat `span / 256` as topology, or persist the
certificate as workspace state.

Expose conceptually equivalent preferences without freezing Java enum names:

```text
Show intersection candidates: ON / OFF
Auto-materialize eligible points: OFF / ON
```

These labels are prospective placeholders, not frozen Java names. Both values
are isolated GeoCeDG frontend/profile defaults, never document geometry.
Enabling auto-materialization must not retroactively materialize an already
existing rich result. Recommended review defaults are markers ON and
auto-materialization OFF. G9U1 may consume only the final R4 kernel
point-admissibility result; a weaker evidence/materialization tier would require
separate future author authorization.

The rich-result inspector must support a persistent materialization session.
Creating one point does not implicitly close the inspector. It must identify
already-materialized choices without exposing opaque tokens as layout labels,
allow one or several admissible solutions to be selected, and provide explicit
create-selected and create-all-currently-eligible actions. Each produced point
owns its own exact token binding. One multi-point action is one coherent visible
and undoable compound transaction. Close and cancel remain explicit; cancel
creates nothing and does not undo earlier separately confirmed actions from the
same session.

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
authority, render tessellation, or persistence semantics beyond consuming the
final R4 exact selector/token and current point-admissibility seam. If that
authority is absent, G9U1 must STOP rather than invent kernel/XML semantics.
Do not implement G9B/G9C spatial semantics, G9U2
procedures or productive G10 work.

Do not:

- make a candidate marker a `GeoElement`, durable ID, XML record, Construction
  Protocol entry, DAG node, undo object, semantic revision or token identity;
- make kernel `Intersect(L,T)`, recompute, load/reopen or background updates
  create persistent points; only the separately visible user-opted frontend
  transaction defined below is permitted;
- identify a solution by coordinate, list order, screen proximity or marker
  identity;
- calculate, replace or continue the kernel's intrinsic semantic phase/rank
  from a UI list position, marker order, presentation ordinal or hit-test rank;
- continue or reidentify a marker/token from a previous marker position,
  previous Cartesian root or movement history;
- make stale/overlap-only/unresolved non-point evidence selectable, or let any
  marker create identity for an ambiguous/inadmissible token;
- add a GeoCeDG-only continuity flag, parallel kernel mode, duplicate
  preference/XML field or workspace-owned continuity state;
- allow a prior preference, restored workspace, `.cedg` or compatibility
  `.ggb` to activate Continuity in the GeoCeDG product;
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
active result's current root/evidence assessments. A root may be selectable only
when R4 supplies a unique, current, point-admissible exact token. A finite
nonmaterializable root may have a
nonselectable diagnostic marker when its current point is truthful; stale,
overlap-only or unresolved non-point evidence has none. Zoom, DPI, camera and
screen state may determine placement, clipping and which already identified
marker is hit or preselected; they never establish, rank, continue or change
semantic identity. The frontend consumes the kernel selector/token atomically
and never reconstructs its phase/rank from presentation state.

The R4 durable selector binding, adaptive revision-scoped topology certificate
and claimed active/dormant/periodic-quarantine token lifecycle all remain
shared-kernel authority. A dormant or quarantined allocation owned by an existing materialized `GeoPoint` has no
selectable marker. If the kernel later reactivates that same token through the
same uniquely resolved selector, G9U1 only redraws current presentation; it does
not construct, replace or retarget the point.

The existing host `Kernel` Continuity value remains the sole kernel/persistence
authority. Reverify the frozen setter/XML/preference/UI characterization above,
then enforce the GeoCeDG product policy at one application-configuration seam
consumed by the existing setter, so every write path is clamped to off for
`AppGeoCeDG`; make the settings UI omit/disable the on choice or present the
enforced policy read-only. Do not special-case individual file-open paths. The
separate Classic configuration continues to permit the same host setting and
ordinary UI control.

The manifest/action registry is the single declarative authority for action
IDs, placement, localization/help, icon references, feature requirements and
unavailable policy. Classic remains a separate process/path and preference
namespace.

# Required design/specification

Before productive edits, reconcile the current source against ADRs
0012/0016/0017,
the normative CeDG workspace and document-identity contracts, the post-R4/R5
public Locus surface, the Windows packaging contract and the resource manifest.
Record:

- strict schema-v2 and deterministic v1-migration design;
- stable action IDs and complete mappings for all eleven professional groups;
- typed selection and cancellation contracts for each constructive action;
- marker overlay ownership, active-result lifecycle, admissibility filtering,
  identity/evidence/global-completeness separation, hit-testing/preselection
  behavior, strict separation from kernel semantic phase/rank and adaptive
  current topology certificate,
  marker/auto defaults and explicit/opted-in automatic
  one/all-point materialization transactions over exact kernel tokens;
- exact selector/token persistence through the existing R4 parent-algorithm
  seam, including dormant claimed allocation and same-`GeoPoint` reactivation,
  without a new quality or policy field on ordinary `GeoPoint`;
- the host Continuity field/setter, every XML and preference read/write path,
  application-profile policy seam, settings-panel behavior and separate
  Classic behavior, with no duplicate option or serialization field;
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

For GeoCeDG, deterministic semantic selection is authoritative over a
continuity heuristic. Movement history does not decide the current solution.
Ordinary continuity should emerge when the same current deterministic semantic
selector remains uniquely valid; genuine topology ambiguity may invalidate
rather than guess. Identical final Construction state and durable IDs reached
through different regular update histories must produce the same current token
binding, point definedness and marker set.

The frontend must not confuse durable selector identity with the current
topology certificate. Adaptive intrinsic periodic phase tubes are kernel-only
revision evidence: ordinary regular motion is independent of UI update
granularity, while a true seam/monodromy shift fails only its affected ranked
group. Insufficient/nonunique cyclic evidence leaves the old group in durable
non-current quarantine, unique offset zero may release/reactivate it, and proved
unique nonzero offset permanently retires it. A dormant or quarantined
materialized token is retained evidence, not a current marker or materialization candidate.

Marker hit testing may preselect only a token already supplied by the current
deterministic rich result. The frontend may neither relax nor reinterpret the
kernel's point-admissibility decision; it changes no root computation,
selector/token meaning, numerical evidence, topology evidence or global
completeness. An exact token means exact semantic identity, not exact arithmetic.
Explicit materialization passes that exact token alone to the approved R4
construction path without re-solving, previous-frame matching or fallback.

The kernel-owned intrinsic phase/rank remains part of that exact selector's
semantic proof. Marker order, list order and a transient presentation ordinal
are allowed only for accessibility and display; they cannot be passed back as
identity, persisted, or used to choose a different root. The enforced
`Continuity = OFF` product invariant does not create a frontend continuation
substitute.

Create-one, create-selected and create-all actions are visible and undoable;
the inspector may remain open across several confirmed materializations and
must identify already-materialized exact-token choices. Cancel and marker
inspection create no persistent object. Kernel `Intersect(L,T)` always creates
only the rich result. With explicit user opt-in, auto mode may issue one second
deterministic create-all transaction immediately after an explicit Intersect
action. It must be a separate atomic undo step, avoid duplicate token children
and never run on recompute, later root appearance, document load/reopen,
workspace switch, preference restoration or background update. Genuine
topology, continuation and token lifecycle semantics remain those of the
approved G9U0/R1/R2/R3/R4/R5 kernel surface.

Dormancy, periodic quarantine and kernel reactivation are recomputation
lifecycle events for an already materialized point. They never count as an
explicit Intersect action and never trigger create-one, create-all or
auto-materialization of a new point.

The existing kernel Continuity option is always off in the GeoCeDG product,
including while no workspace is active and during workspace transitions. This
product policy changes no construction object, semantic revision, DAG edge or
undo state. GeoCeDG Classic retains ordinary upstream configurability.

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
existing R3/R4-validated save/reopen, copy/remap and undo/redo behavior. Marker
and auto preferences are application presentation/workflow state and never
reinterpret an existing point. Loading a document with supported rich results reconstructs only
serialized semantic authority and points; it never auto-materializes. Overlay
presentation is recomputed only for the active result under current workspace
preference.

If a serialized materialized token is retained dormant, native load/reopen
restores that exact point/token relationship under the R4 ledger contract. The
point remains undefined until the same selector resolves uniquely and currently;
its reactivation is not point creation and produces no marker-driven
transaction. Periodic `q`/`r` remains non-current kernel evidence; the current
R4 source authority proves its ledger recompute/export-import/copy lifecycle but
does not yet claim a native periodic-quarantine round trip. Retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` must be revisited by G9U1
validation and resolved or explicitly dispositioned by global G9 closeout.
G9U1 must not expose or market a stronger native persistence claim without new
sealed evidence.

The host's existing Continuity persistence remains the only serialization
seam. GeoCeDG product policy wins over a previously enabled user preference,
restored profile/window state, workspace switching, application restart, a
native `.cedg`, and a compatibility `.ggb` that records Continuity on. Normal
GeoCeDG save records the enforced off value through that existing host seam
where the host serializes it; do not add a second field or rewrite geometry.
Opening compatibility input must leave the source `.ggb` untouched under the
R2 non-destructive transition contract. The separate Classic route keeps its
own preference namespace and normal configurable value.

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

- the active/selected result distinguishes exact-token eligible and
  nonmaterializable candidates
  using accessible shape/glyph/text/status cues rather than color alone; an
  inactive result shows none;
- stale and overlap-only evidence with no truthful finite point shows none;
  a finite current nonmaterializable candidate may have only a nonselectable
  diagnostic marker;
- zoom/DPI/view changes do not alter exact identity;
- marker hit testing/preselection returns only an existing exact kernel
  selector/token and never computes semantic phase/rank or continues a token
  from UI order, previous marker position or movement history;
- byte-identical constructions with identical durable IDs at the same final
  regular geometry expose the same token/marker bindings after direct,
  incremental, reverse and save/reopen update paths;
- one ordinary UI-sized regular update and many smaller updates to the same
  four-root geometry expose identical current tokens, points and markers; no
  frontend threshold repairs or masks an R4 topology-certificate failure;
- a true periodic seam/monodromy shift suppresses only the affected ranked
  group's current selectable markers and never rotates a token, while an
  independently certified group remains current;
- roots lacking current exact-token point admissibility remain nonmaterializable;
  G9U1 introduces no weaker certification or identity override;
- cancel creates nothing; explicit one-point materialization is exact and
  normally undoable; the inspector remains usable for further choices unless
  the user explicitly closes it;
- create-selected materializes exactly the selected eligible token set and
  create-all exactly the current eligible token set; each is one coherent
  compound undo action, avoids duplicate children and marks already-materialized
  choices without using presentation order as identity;
- auto OFF leaves `Intersect(L,T)` with zero persistent points; auto ON performs
  exactly one separate visible create-all transaction
  for the explicit action's eligible exact-token snapshot, with no list/marker
  order becoming materialization identity;
- recompute, newly appearing roots, load/reopen, workspace switch and preference
  restoration never auto-create a point;
- a claimed dormant or periodically quarantined token has no selectable marker and triggers no
  materialization; when the kernel reactivates the same exact token through the
  same unique selector, the same `GeoPoint` becomes defined without any new DAG
  object or automatic frontend transaction;
- changing marker/auto presentation preferences does not reinterpret existing
  point algorithms;
- exact selector/token provenance survives save/reopen, supported copy and
  undo/redo;
- no marker appears in XML, the DAG, Construction Protocol, copy closure or
  undo history.
- dedicated G9U1 persistence validation revisits
  `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`; absence of that evidence may not
  be hidden by the inspector or marker layer.

Deterministic-product-policy tests must prove:

- a fresh GeoCeDG launch has the existing kernel Continuity option off;
- a previously enabled user preference cannot turn it on in GeoCeDG;
- a compatibility `.ggb` or native `.cedg` that records Continuity on cannot
  activate it in the live GeoCeDG product;
- the Advanced/settings UI cannot switch it on and communicates the product
  policy accessibly;
- workspace switching, restored UI/profile state, restart and save/reopen all
  retain off without changing geometry or creating undo state;
- GeoCeDG save uses the existing host serialization field and emits no second
  GeoCeDG continuity field;
- the original compatibility `.ggb` remains untouched; and
- the separate GeoCeDG Classic route retains upstream user configurability and
  its isolated preference behavior.

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

Rerun G9U0, G9U0-R1, G9U0-R2, G9U0-R3, G9U0-R4, G9U0-R5, G9X1, G5, relevant
G9A, legacy Locus, frontend/profile/localization, Desktop/document persistence,
packaging when affected, relevant Checkstyle, `git diff --check`,
`git diff --cached --check` and the full `tools/agent/verify.ps1` without
weakening any gate. Require two matching focused executions and terminal
`All GeoCeDG verification gates passed.` Generated evidence belongs only below
ignored `artifacts/` except the repository's approved compact durable records.

Manual author review must cover professional workflow/density, markers and
materialization, accessibility/scaling, Construction-versus-Classic visual
identity, final palette, the top-bar source in frontend chrome and every claimed
startup-role application/package derivative. It must also confirm that
GeoCeDG exposes the deterministic Continuity-off policy and cannot enable the
host option while the separate Classic route remains normally configurable.

# Required artifacts

Produce the approved schema/profile/action registry and minimal frontend and
packaging changes; focused tests and verifier integrated after R5; scenario
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

- R3, R4 or R5 is not `PASS — AUTHOR APPROVED`, this exact successor is not
  separately authorized, entry authority is dirty/inconsistent, or its hash
  changed;
- a workspace/menu repair needs another action authority or requires kernel
  geometry, command, solver, token, identity or serialization changes beyond
  the approved existing Continuity policy seam;
- the deterministic product policy cannot be enforced by clamping the existing
  host Continuity option without a second mode/field or a Classic regression;
- marker UX cannot remain a transient overlay over current deterministic
  root/evidence authority, or a frontend action would override current
  exact-token point admissibility or deterministic-selector ambiguity;
- marker UX would need to expose a dormant retained allocation as current, or
  dormancy/reactivation would trigger point creation;
- marker/list ordering or frontend hit testing would need to calculate,
  renumber, persist or replace the kernel's intrinsic semantic phase/rank;
- an exact point would require coordinate/order/proximity identity, re-solving
  or DAG creation during recompute/load/background updates;
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
DETERMINISTIC_CONTINUITY_OFF_REQUIRED
INTRINSIC_SEMANTIC_PHASE_RANK = KERNEL_AUTHORITY_UI_CONSUMER_ONLY
INTRINSIC_PHASE_RANK_TOKEN_AUTHORITY_REQUIRED
CURRENT_TOPOLOGY_CERTIFICATE = KERNEL_AUTHORITY_UI_CONSUMER_ONLY
DORMANT_TOKEN_REACTIVATION = SAME_SELECTOR_SAME_GEOPOINT_NO_CREATION
PERIODIC_QUARANTINE = KERNEL_AUTHORITY_NO_MARKER_NO_CREATION
MULTI_MATERIALIZATION_REQUIRED
PERSISTENT_INSPECTOR_SESSION_REQUIRED
AUTO_REACTIVATION_EXISTING_POINTS_KERNEL
AUTO_MATERIALIZATION_FRONTEND_ONLY
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

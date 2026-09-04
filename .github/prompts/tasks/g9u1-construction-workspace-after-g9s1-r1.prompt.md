# G9U1 — CeDG Construction workspace after G9S1-R1

- Status: **DESIGN PASS — AUTHOR APPROVED; POST-R1 RECONCILED**
- Phase: G9U1; productive implementation explicitly conditionally authorized
- Protected approved predecessor: `00982e7e148a634cd57ed928f322774df267d5e3`
  on `feature/g9u1-construction-workspace-planning-after-r6`
- Predecessor prompt: `g9u1-construction-workspace-after-g9u0-r6.prompt.md`
- Predecessor canonical-LF SHA-256: `561546019efc1e1d5e4367ddde73e9a2b0a0d767343eb9348b46d9e9c06f12df`
- Supersedes prospectively only; both earlier protected checkpoints and prompts
  remain immutable historical authorities.
- The historical pre-R6 prompt `g9u1-construction-workspace-after-g9s1.prompt.md`
  remains pinned at `857de6628489bda0b65a5ba5145e62ca0795fc32`.
- Entry/base: clean published operational main
  `f8a21a087234b18fc13741a0ac2baf80608e9022`

```text
POST-R6 RECONCILED = true
POST-R1 RECONCILED = true
materialNovelty = NONE_IDENTIFIED
implementationStarted = false
implementationAuthorized = true
selfApproved = false
authorApprovedDesign = true
authorApprovedImplementation = false
passClaimedImplementation = false
manualAuthorSmoke = PENDING
```

The current autonomous author instruction authorizes the bounded post-R1
reconciliation and productive implementation if no new author-level semantic
decision is required. The source/API/workspace audit found none. The planning
verifier must pass before the first productive edit; record
`implementationStarted=true` at that edit. This is authorization to produce a
candidate, never author approval of its implementation.

# Objective

Implement one complete, professional and usable **CeDG Construction** workspace
from the published post-R1 product surface. This is a frontend/application/profile gate
that consumes approved kernel authorities; it is not permission to add geometric
truth in the UI or to execute G9B, G9C, G9U2 or productive G10.

The approved post-R6 design is reconciled only for the published R1 delta under
the current explicit conditional authorization. Freeze this successor's exact
canonical-LF hash before productive implementation. During implementation:

```text
REASONING_EFFORT = ultra
CONTINUE_INDEPENDENTLY = true
NO_SELF_APPROVAL = true
```

After required technical gates pass, the current author instruction permits
candidate commits and feature-branch publication. Do not merge G9U1 to main or
create a G9U1 PASS tag. Never self-approve the manual product smoke.

# Authority and evidence hierarchy

Follow `AGENTS.md`: current code, tests, build and serialization contracts;
approved specifications and ADRs; canonical references and evidence; pinned
upstream authority; generated reports; then conversation history. This prompt
scopes the authorized work and does not override those source authorities.

## Entry authority and satisfied kernel prerequisite

Before mutation verify a clean equal `HEAD`, `main`, `origin/main` and direct
remote main, an empty index, the exact current prompt hash, and the sealed PASS
tags/reports/evidence for G9U0, G9U0-R1, G9U0-R2, G9U0-R3, G9U0-R4, G9U0-R5,
G9S1, G9S1-R1, G9X1, G9A and G5–G8. In particular:

- `geocedg-g9s1-pass` must be an annotated tag peeling to
  `de33f3a80102adb051aaa7547a72b7e97409c58c`, with annotated tag object
  `ece0ca6f00299d3347e57fac38b7a28cade28644`;
- `geocedg-g9u0-r6-pass` must be an annotated tag object
  `2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e` peeling to
  `3942af594e4507e479f2c75019cef62e3d9fea6f`;
- `geocedg-g9s1-r1-pass` must peel to
  `af459d856f1cdc384805f3035203acce8e6f6104`, ancestral to the entry main;
- ADRs 0012, 0016, 0017, 0018 and 0019 and their normative specifications must be
  mutually consistent;
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` must remain visible until the
  native lifecycle evidence below closes it or the author explicitly disposes
  it; and
- the live schema-v1 profile must still be the current product authority before
  the authorized migration.

Interactive creation and dragging of an interaction-owned semantic point on
Locus V2, Spline V2 or an R5-transformed semantic curve consume the published
R6 shared-kernel contract:

```text
G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT
    = PASS — AUTHOR APPROVED
```

The exact public seam is
`LocusPointInteractionQuery2D` ->
`LocusPointInteractionResolver2D.resolve(...)` ->
`LocusPointInteractionResult2D`, with status authority in
`LocusPointInteractionStatus2D`. Creation consumes the exact resolver-owned
`LocusPointInteractionCandidate2D` through
`LocusV2PublicOperations.createInteractiveSemanticPoint(...)`; dragging uses
`LocusV2PublicOperations.moveInteractiveSemanticPoint(...)`. R6 is already
`PASS — AUTHOR APPROVED`; this prompt neither reimplements nor broadens it. If
the exact tag/commit, ADR 0019 or normative R6 specification is absent, **STOP
before productive G9U1 implementation**.

Create a bounded G9U1 implementation branch according to repository convention.

# Required design/specification

Read and execute the repository authorities rather than duplicating them:

- `docs/adr/0012-manifest-defined-geocedg-workspaces.md`;
- `geocedg/specs/ui/cedg-workspaces.md`;
- `geocedg/specs/ui/g9u1-construction-interaction.md`;
- `geocedg/specs/ui/application-profile-v2.candidate.schema.json`;
- `geocedg/specs/ui/application-profile-v2.candidate.yml`;
- `docs/architecture/cedg_workspace_architecture.md`;
- `docs/validation/g9u1_workspace_completeness_matrix.md`;
- `docs/validation/g9u1_command_tool_consistency_matrix.md`;
- `docs/validation/g9_public_workspace_validation_matrix.md`; and
- `geocedg/validation/g9u1/g9u1-preexecution-scenarios.json`.

The accepted eleven broad workspace families remain the organizational
authority. The candidate manifest refines them into eighteen operational
clusters and currently declares exactly 110 stable action IDs; it does not
replace ADR 0012. Preserve that count unless implementation evidence justifies
and records an exact action addition/removal. Promote exactly one validated
schema-v2 manifest/action catalog into the live profile. Toolbars, menus,
context actions, help, unavailable reasons and inspectors must reference the
same stable action IDs. Do not retain a second hard-coded product-menu/toolbar
authority.

# Architectural placement

Classify every implementation change before code:

- shared-kernel semantics: already approved R2/R4/R5/G9S1/G9S1-R1 and the separately
  approved R6 resolver only;
- Desktop/frontend: menus, hit-testing, overlays, dialogs, explicit UI
  transactions, definition inspection, navigation and accessibility;
- workspace/profile: action membership, placement, defaults and availability;
- command/help/localization: ordinary host registries and English/Spanish
  resources;
- preferences/persistence: presentation preferences and existing host settings,
  never geometry; or
- deferred: capability requiring a distinct semantic gate.

Frontend state must not own geometry, solve roots, derive tokens, invent
preimages, change metric evidence or participate in the construction DAG.

# Scope

## 4. Complete workspace and action authority

Implement every intended action in the approved manifest, including the
eighteen operational clusters:

1. selection, move and inspection;
2. point construction;
3. linear geometry;
4. parameters and drivers;
5. relations and intersections;
6. circles, conics and curves;
7. Locus V2;
8. Spline V2;
9. metrics and validation;
10. similarity transformations;
11. currently authorized manual projection actions;
12. presentation, visibility and style;
13. navigation and zoom;
14. document lifecycle;
15. automation and scripting;
16. authorized import/export;
17. help and command discovery; and
18. construction history and definition inspection.

Use the completeness and command/tool matrices as executable inventory. Every
visible action must resolve to a real host/product seam or be disabled with a
localized truthful reason. No dead button is permitted. Legacy Locus mode 47 is
compatibility-only; Template-v7 macros remain Laboratory evidence; circle
inversion is not ordinary reflection; spatial/3D/procedure work remains gated.
Workspace membership changes discoverability only, not command availability or
feature policy.

# Geometric invariants and degeneracies

## 5. Deterministic product policy

CeDG Construction reuses and locks the existing kernel setting:

```text
Continuity = OFF
```

The product lifecycle must reassert OFF after fresh launch, preferences,
workspace restore/switch, application restart, native `.cedg` load and
compatibility `.ggb` load. The ordinary product settings UI cannot turn it on;
show a read-only policy indication where useful. Do not add another setting or
serialization field. GeoCeDG Classic retains upstream configurability in its
separate process/profile. A compatibility `.ggb` with Continuity ON must not
change the live product policy and must remain unmodified by the R2 workflow.

Deterministic current semantic selection remains authoritative over continuity
heuristics. Screen state, coordinates, nearest-root matching, output/list order
and movement history are never identity.

## 6. Curve selection and semantic Point interaction

### 6.1 Stroke-only hit testing

Correct Locus V2/Spline V2/R5-transformed curve hit testing through the shared
Euclidian presentation seam. A click well inside a closed curve must not hit the
curve. A click within the ordinary stroked curve tolerance must hit it. Stroke
thickness and DPI affect picking tolerance only; they never alter semantic
geometry. Define deterministic overlap precedence among ordinary/materialized
points, current candidate markers, curve strokes and other objects. Open and
closed curves use the same stroke rule.

Do not use filled-path intersection, viewport containment or semantic-area
inference.

### 6.2 Ordinary Point tool

Using the published R6 authority, make the ordinary Point tool create a
one-degree-of-freedom interaction-owned semantic point on Locus V2, Spline V2
and supported transformed sources:

Cover every source family for which R6 establishes a selectable typed result:
general supported Locus V2, scalar-driven and point-driven Locus V2, periodic
Locus V2, disconnected/component-aware Locus V2 and Spline V2. A source that R6
reports unresolved or unsupported stays fail-closed; the frontend must not fill
that gap with a local inverse heuristic.

```text
frontend stroke hit
 -> world/geometric interaction target
 -> LocusPointInteractionQuery2D
 -> LocusPointInteractionResolver2D.resolve(...)
 -> typed LocusPointInteractionResult2D
```

Handle its exact statuses as follows:

- `UNIQUE_ADMISSIBLE_PREIMAGE`: pass `getUniqueCandidate()` unchanged to
  `createInteractiveSemanticPoint(...)` and create exactly one point;
- `MULTIPLE_SEMANTIC_PREIMAGES`: show a deterministic ambiguity chooser; an
  explicit choice passes that exact candidate object to the same creation seam,
  while Cancel creates nothing;
- `NO_ADMISSIBLE_PREIMAGE`, `UNRESOLVED_NUMERICAL_SEARCH`, `INVALID_SOURCE`,
  `DEGENERATE_SOURCE_IMAGE` and `UNSUPPORTED_CAPABILITY`: create nothing and
  present truthful localized feedback.

The chooser may present branch/component, canonical semantic location,
span/local orientation, isolation interval/evidence and a transient graphical
highlight. It must retain the exact resolver-produced candidate object; it may
not reconstruct an address from a presentation ordinal. One locally discovered
candidate under incomplete bounded evaluator coverage remains
`UNRESOLVED_NUMERICAL_SEARCH`, not unique. The frontend-derived world hit radius
may initialize `LocusPointInteractionPolicy2D.initial(hitRadius)` as query
tolerance and presentation ranking only; it is never persisted identity.

### 6.3 Dragging and fail-closed updates

Dragging an existing interaction-owned point sends the new world target to
`moveInteractiveSemanticPoint(...)`. A unique current result updates the exact
semantic address on the same `GeoPoint`, durable ID and source binding. Multiple,
none, unresolved, invalid, degenerate or unsupported results leave the point and
its semantic direction unchanged; no replacement point or silent retarget is
allowed. Ordinary exact command points not owned by the R6 interaction seam are
not retroactively converted into draggable interaction points.

The frontend owns one coherent undo gesture and must reacquire construction
instances if an exceptional host rollback reconstructs them. It must not perform
a second inverse solve or store pointer history. Persist/copy/remap/undo/redo use
semantic source and address, never screen proximity.

### 6.4 Seam, self-intersection and transformed sources

Closed SplineV2 seam drag must expose the proved R6 behavior naturally: the same
point crosses approximately `u=0.98 -> 0.02` with the canonical lift, crosses
back, has no duplicate seam candidate and reaches the same final semantic state
by direct or incremental paths. Exact stored IEEE-754 semantic-direction bits,
parameter, lift and seam side remain kernel authority; ordinary UI need not show
`periodicLift` jargon.

At self-intersection, multiple semantic preimages require the chooser. Point
creation/drag must also work on R5 Translate, Rotate, Reflect/Mirror and positive
or negative Dilate sources supported by R6, with the point bound to the
transformed source. At `k=0`, a new query is `DEGENERATE_SOURCE_IMAGE` and creates
no arbitrary point; an existing interaction-owned point retains its exact
semantic direction, may share the collapsed image, and recovers as the same point
when `k` becomes nonzero.

## 7. Rich intersections and materialization UX

The rich intersection result remains non-Euclidian semantic authority. For the
active/selected result only, show transient markers for current finite
point-admissible exact tokens. Markers default ON in CeDG Construction and are
presentation overlays only: no GeoElement, ID, XML, DAG, Protocol or undo state.
Stale, dormant, quarantined, overlap-only, unresolved, ambiguous and
inadmissible candidates are nonselectable; accessibility must not rely on color.

Provide in one persistent inspector session:

- materialize selected point;
- materialize several explicitly selected admissible points; and
- materialize all currently eligible points.

Each result point receives its own exact current token. Multi-create is one
visible coherent undo transaction. Already materialized choices are indicated;
successful creation need not close the inspector; Close/Cancel remain explicit.
Opaque token length must never size labels or dialogs.

An optional auto-materialization preference is an explicit visible frontend
transaction immediately after query creation. It is undoable. Kernel recompute,
load and later root appearance never create new DAG nodes. Kernel automatic
reactivation remains limited to an already-existing exact-token point whose same
selector again resolves uniquely.

### Published G9S1-R1 pair consumer contract

Spline×Spline is consumed through the certified R1 subset below.

G9S1-R1 is PASS — AUTHOR APPROVED at `geocedg-g9s1-r1-pass`,
peeling to `af459d856f1cdc384805f3035203acce8e6f6104`. The reviewed
technical cohort is `a38d4fcde846fc97c51abc8d958de6998302c436`; the
published operational successor is `f8a21a087234b18fc13741a0ac2baf80608e9022`.
The frontend consumes the current rich pair result once. Candidate discovery,
floating refinement, certified local existence/uniqueness and transversality,
durable semantic identity, and exact-token publication are distinct gates.

The approved selector subset is `pair-singleton-transverse-germ/v1`.
A current locally certified root is eligible only when its canonical
source-pair/component/germ slot is uniquely established by R1. Several distinct
certified roots can therefore be individually eligible, including opposite-germ
roots in the same span pair. Coexisting same-germ roots are not distinguished
by projected parameter rank: in a `+,+,-` group the unique negative germ may
be eligible while the two positive-germ roots remain rich-only.

`Intersect(S,T)` and `Intersect(T,S)` have equivalent canonical pair/slot
evidence. Independently constructed rich-result owners deliberately have
different opaque token namespaces; do not require cross-owner token equality.
Within one owner the exact current token is reused for its materialized point.
No UI identity comes from Cartesian coordinates, parameters, rectangle ends,
solver order, marker ordering, screen proximity, or an R6 inverse query.

The existing inspector and one/selected/all actions consume each independently
eligible exact token. Uncertified, stale, same-germ ambiguous, tangent or
multiple-contact, overlapping, monodromic and budget/numerically unresolved
candidates stay rich-only with truthful localized reasons. Several distinct
roots are not the same as unresolved multiplicity at one root.
`local admissibility != global completeness`: eligible local roots may
coexist with global `INCOMPLETE` or `NOT_ESTABLISHED` evidence.

Regular current identity keeps the same point/ID/token; temporary
non-admissibility is displayed as dormant/undefined, never retargeted. Only
the kernel may reactivate the same exact selector. Recompute creates no new
point. Pair materialization uses R1 tokens, while Point-on-curve creation/drag
continues to use the distinct R6 typed semantic-preimage API. Generic LocusV2
pair materialization is not broadened.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED.
G9U1 must execute the native lifecycle experiment. If it remains unproved
without corruption or a kernel/persistence contract violation, the author
explicitly permits the G9U1 candidate to continue with the risk open. A real
kernel/persistence violation is a STOP condition. Global G9 closeout still
requires explicit resolution/disposition; R1 periodic-pair evidence does not
close this separate R4 risk.

## 8. Algebra, definition and redefine contracts

### 8.1 Preview and explicit commit

Audit the complete Algebra input lifecycle. Parsing, autocomplete and preview
must create zero productive GeoElements, algorithms, IDs, XML changes or undo
entries. Enter/explicit submit creates exactly one atomic construction
transaction; Escape/cancel creates none. Apply this invariant to legacy commands
and every exposed GeoCeDG command, including `LocusV2`, `SplineV2`, `Intersect`,
`Length`, `Translate`, `Rotate`, `Reflect`/`Mirror` and `Dilate`. Fix the normal
command/preview seam, not one input widget workaround.

### 8.2 Definition inspection

Expose the reconstructible definition of Locus V2, Spline V2 and transformed
semantic objects through ordinary Algebra row/context/Properties affordances
without requiring a global description-mode change. Viewing is read-only and
must be distinct from edit/redefine authority. Do not make dependent semantic
objects directly editable merely because their command definition is visible.

### 8.3 Algebra description mode

Use the existing Algebra style setting as the single authority. Value,
Definition and Description choices must use normal mutually exclusive radio/
check state, reflect the actual current value, update immediately, survive the
host lifecycle and apply orientation to the correct menu. Do not derive this
state from Algebra tree sort order or add a new preference.

### 8.4 G9A-compatible free-input redefine

Route an explicit user assignment such as `k=0.25` through the approved atomic
G9A compatible-redefine transaction. A label may locate the explicit current
command-context candidate but is never durable identity. Preserve ID only when
the G9A compatibility predicate succeeds. Incompatible redefine follows the
existing new-identity contract; ambiguous/nonexistent targets fail closed.
Undo/redo/save/reopen must preserve the graph. Characterize Classic separately.

## 9. Navigation

Pull forward the bounded `ZoomWindow` capability needed by G9U1. Reuse the
ordinary selection-rectangle/`MODE_ZOOM_IN` view seam and expose one stable
product action in menu, toolbar and an accessible configurable keyboard
activation anchored at current cursor context. Also expose the already bounded
host navigation companions declared by the manifest: pan, zoom in/out, standard
view and show all.

Leave `ZoomPrevious`, `FitSelection`, `FitLayer`, named views, precise scale and
the broader navigation/scale system in G12 unless separately authorized. This
is an explicit roadmap split, not duplicate scope. Navigation affects view state
only; metrics, intersections, tokens and certificates are invariant.

## 10. Commands, help, scripts and languages

All commands/actions in the matrix must use the ordinary command/action/help
authority and provide:

- autocomplete, command hints and exact syntax/argument forms;
- concise localized name, short help, status and error text;
- Help/F1/context path where the host supports it;
- consistent feature-unavailable behavior; and
- representative GGBScript execution through the same AlgebraProcessor/kernel.

Audit at least `LocusV2`, `LocusLength`, `SplineV2`, semantic `Point`, scalar
`Length` overloads, rich `Intersect` and all seven R5 transform forms. Do not add
a script-only implementation. If an action cannot safely support GGBScript,
record the precise reason as an author decision rather than leaving a matrix
hole.

Pointer/inverse resolution is UI interaction support, not a synthetic GGBScript
mouse API. GGBScript continues to use exact semantic forms such as
`Point(L,"branch",u)` when the address is known. Toolbar Point interaction,
exact command form, script form, chooser, persistence, undo/redo, help and EN/ES
localization must each have an explicit entry in the command/tool matrix.

GeoCeDG product languages are English and Spanish only, with deterministic
English fallback. Do not delete or rewrite the upstream corpus. Product
selectors expose only EN/ES; all product keys/help/errors are complete in both;
semantic XML is language-independent. Classic retains upstream language
availability.

# Compatibility and serialization

## 11. Professional frontend completeness

Ensure normal Algebra/Graphics consistency for user-visible semantic objects:
selection synchronization, label/type/value/definition, visibility, Properties,
rename, delete, auxiliary status, style, context menu and Construction Protocol.
Hidden rich parents remain intentionally hidden.

Preserve normal document behavior: new/open/save/reopen `.cedg`, compatibility
open `.ggb`, recent files, modified state, transactional failed load, Save As,
undo/redo, styles, definitions and workspace restore. Never silently overwrite a
compatibility `.ggb`; retain ZIP/XML and `app="classic"` internals.

Workspace/profile preferences may persist active workspace, visible panels,
toolbar organization, supported language, dock/window layout and permitted view
navigation state. They must not alter construction bytes, identity, tokens,
certificates or metrics. Opening the same `.cedg` under another layout yields the
same construction.

Implement keyboard navigation, deterministic focus order, Escape cancellation,
Enter commit, accessible labels/tooltips, non-color-only state, high-DPI and
resizable bounded dialogs. Regress the R3 long-token layout defect.

Use typed localized EN/ES errors that distinguish invalid input, unsupported
operation, semantic ambiguity, incomplete/not-established evidence and internal
failure. Fail atomically without leaking opaque token internals.

Markers, inspectors and existing point bindings consume one rich-result snapshot
and selector map. Do not repeat a global solve per marker/point or store movement
trajectories. Establish bounded responsiveness for representative rich results,
many markers, materialized points, transformed loci and splines.

## 12. Visual identity and assets

Implement restrained, professional GeoCeDG frontend identity distinct from
Classic using normal reviewable theme/chrome seams. Presentation/theme changes
cause zero construction or undo mutation and color is never semantic authority.

Maintain two provenance-owned logical roles:

- `geocedg.brand.topbar`, intended source `helixTopBar.png`; and
- `geocedg.brand.startup`, intended source `helixSnapshot.png`.

Record filename, SHA-256, dimensions, alpha/format and provenance. Derive
platform sizes deterministically with tool/version/options; do not maintain
hand-copied packaging duplicates, assume a startup bitmap is suitable at
16×16/32×32, fabricate missing assets or copy upstream branding. Classic keeps
its diagnostic identity. If an author asset is absent, use the bounded existing
text/default fallback and report the missing review item; do not substitute a
new logo. Final branding/package claims require author asset review.

## 13. Periodic quarantine disposition

Attempt to close `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` with the actual
native archive lifecycle specified in the command/tool matrix: enter quarantine
through real resolver/recompute, save `.cedg`, reopen while unresolved, and
verify exact selectors/tokens/point IDs, dormant state, zero markers and zero new
points; then test unique-zero release and proved-nonzero retirement from
byte-identical seeds plus feature-off/Classic preservation.

Ledger export/import, copy or forged XML cannot substitute for native evidence.
If the lifecycle cannot be proven without demonstrating corruption or a real
kernel/persistence violation, keep the risk OPEN/TRACKED and continue the
candidate under the author's bounded disposition. A demonstrated contract
violation is a STOP condition. Never silently close the risk; global G9
closeout still requires disposition.

## 14. Implementation boundary and artifacts

Expected productive changes are bounded to schema/profile compiler, Desktop/UI
adapters, GeoCeDG resources/localization, normal help/command metadata, focused
tests, preference/document adapters and modified-upstream registration. Point
interaction is frontend orchestration over the already-published R6 API: do not
modify its kernel semantics in G9U1. A demonstrated R6 contract violation is a
STOP condition requiring separately bounded corrective authority, not license to
embed a kernel correction here. Any new geometry/identity algorithm is likewise
a STOP condition.

# Required artifacts

Produce focused tests, deterministic evidence/hashes, final implementation
report, user/developer guidance after observable validation, exact inventory and
composed-verifier integration. Generated logs remain ignored under `artifacts/`.

# Explicitly forbidden scope

Do not implement G9B, G9C, G9U2, productive G10, generic Path conformance,
frontend pair-root identity, marker persistence, new geometry from recompute,
unapproved PDF/SVG/CAD/3D/procedure features or substitute branding.

# Required tests and commands

Execute all 138 scenarios in
`docs/validation/g9_public_workspace_validation_matrix.md` and every row of the
workspace completeness and command/tool consistency matrices. This includes:

- schema v2, v1 migration and single action authority;
- all eighteen clusters and eleven family mappings with no dead actions;
- product/Classic, feature and preservation boundaries;
- locked Continuity OFF;
- stroke-only Locus/Spline hit testing;
- R6 semantic Point creation/drag/ambiguity/persistence;
- the exact `U1-PNT-01` through `U1-PNT-20` Point interaction group, including
  stroke-only selection, chooser cancel/accept, seam drag, transforms, negative
  dilation, `k=0`, save/reopen, undo/redo, copy/remap and zoom/DPI identity;
- all 20 `U1-PAIR-01` through `U1-PAIR-20` frontend R1-consumption scenarios,
  without duplicating the numerical R1 suite or inferring global completeness;
- markers, create-one/selected/all, persistent inspector, compound undo and
  zero recompute creation;
- Algebra zero-mutation preview, exact single commit and Escape;
- definition inspection, description-state radio/check and G9A redefine;
- ZoomWindow in menu/toolbar/keyboard and semantic zoom invariance;
- command help, autocomplete, GGBScript, English/Spanish completeness;
- native/compatibility document lifecycle and periodic-quarantine disposition;
- branding, accessibility, DPI, bounded token layout and performance; and
- Classic plus every historical G9U0/R1/R2/R3/R4/R5/G9S1, G9A, G9X1, G5–G8,
  legacy/scientific Locus, packaging and full composed authority.

Use ADR 0020/0024 and the current verification-level contract: focused DEV while
implementing; final PHASE and COMPOSED, and FULL only when the impact contract
requires it. Run a required FULL once on the final executable cohort, using
same-run receipts to avoid duplicated Gradle tasks. Archived heavy evidence is
never reported as a new execution. Run the focused G9U1 verifier twice where
deterministic summary evidence requires it; require relevant Checkstyle, `git diff --check`, `git diff --cached --check`
and `./tools/agent/verify.ps1` (PowerShell equivalent on Windows) with exit 0 and:

```text
All GeoCeDG verification gates passed.
```

Do not claim the manual smoke. Prepare the first real GUI acceptance of R6 in
`docs/validation/g9u1_command_tool_consistency_matrix.md`, covering document
lifecycle and the exact sequence: Point tool -> stroke click on LocusV2 ->
create -> drag; Point tool -> stroke click on SplineV2 -> create -> drag;
periodic seam crossing; self-intersection chooser accept/cancel; transformed
source; negative dilation; `k=0` no-new-point plus existing-point recovery; and
save/reopen. Also cover intersections/multi-materialization, metrics/transforms,
ZoomWindow, help/GGBScript, EN/ES, definition/preview/redefine, undo/reopen,
accessibility/branding and Classic.

# Stop conditions

Stop and report before broadening if:

- the exact R6 PASS tag/commit or another mandatory entry authority is absent;
- a requested action needs new unapproved kernel semantics;
- hit testing or semantic Point would need fill/proximity/render authority;
- preview cannot be made nonproductive through the normal host seam;
- markers/materialization would need frontend-invented identity or repeated
  solving;
- Continuity OFF requires a parallel setting;
- workspace state changes Construction;
- spline×spline point creation would need an uncertified pair selector;
- periodic quarantine is silently lost;
- branding provenance is absent for a claimed final asset; or
- any historical gate regresses.

Under the current conditional authorization, stop implementation at:

```text
G9U1 DESIGN = PASS — AUTHOR APPROVED
POST-R1 RECONCILED = true
G9U1 IMPLEMENTATION = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
implementationAuthorized = true
implementationComplete = true
manualAuthorSmoke = PENDING
selfApproved = false
authorApprovedImplementation = false
passClaimedImplementation = false

G9S1 = PASS — AUTHOR APPROVED
G9U0-R6 = PASS — AUTHOR APPROVED
G9S1-R1 = PASS — AUTHOR APPROVED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

Report exact entry authority, R6 gate, architecture/layering, complete action
inventory, productive paths, every corrected UX defect, 138-scenario and matrix
results, deterministic hashes, historical/composed verification, periodic-risk
disposition, retained limitations, manual author smoke and technical readiness.

STOP FOR AUTHOR REVIEW.

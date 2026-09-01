# Specification: G9U1 CeDG Construction interaction

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Version: 0
- Owners: GeoCeDG project owner
- Roadmap gate: G9U1, post-G9S1; implementation **DESIGNED — NOT
  AUTHORIZED / NOT STARTED**
- Mandatory kernel prerequisite: G9U0-R6 — Semantic Locus Point Interaction
  Support (kernel inverse-address resolution), **NOT AUTHORIZED / NOT
  IMPLEMENTED**
- Affected layers: Desktop/application interaction and product policy; shared
  kernel only through the separately gated G9U0-R6 prerequisite
- Companion architecture:
  `docs/architecture/cedg_workspace_architecture.md`
- Existing normative workspace authority:
  `geocedg/specs/ui/cedg-workspaces.md` and Accepted ADR 0012

## Objective

Define the interaction boundary for the future CeDG Construction workspace
after G9U0-R5 and G9S1. The workspace must present the approved semantic
Locus V2, transformed-Locus and Spline V2 capabilities through ordinary,
accessible Desktop interactions without moving geometric truth into the view.

This candidate reconciles six bounded public-interaction contracts:

1. stroke-only curve hit testing;
2. zero-mutation Algebra preview followed by one explicit commit;
3. read-only definition inspection distinct from G9A redefine;
4. correct Algebra description-mode radio/check state;
5. the existing host Continuity option locked `OFF` in GeoCeDG; and
6. an interactive semantic Point workflow that is blocked until a separately
   approved shared-kernel inverse-address resolver exists.

Approval of this design would not authorize G9U0-R6 or G9U1 implementation.

## Authority and dependencies

This candidate consumes, but does not redefine:

- `AGENTS.md` constructive, semantic and frontend placement rules;
- the author-approved G9U0 through G9U0-R5 public Locus V2, deterministic
  intersection, similarity-transform and persistence contracts;
- the author-approved G9S1 semantic Spline V2 contract;
- `geocedg/specs/ui/cedg-workspaces.md` and Accepted ADR 0012;
- `geocedg/specs/ui/native-document-identity.md` and Accepted ADR 0016;
- `geocedg/specs/locus/locus-v2-presentation.md`;
- `geocedg/specs/locus/locus-v2-public-ui-exposure.md`;
- Accepted ADR 0017 and the R4 exact-token/selector lifecycle;
- the accepted R5 `COLLAPSED_IMAGE` decision;
- the accepted G9A compatible-redefine transaction contract; and
- `docs/validation/g9_public_workspace_validation_matrix.md`.

The post-G9S1 `GeoLocusV2` family includes native Locus V2, semantic Spline V2
and the first-class results of the approved R5 similarity transforms. A
frontend distinction between those producers must never become a different
geometric or identity model.

## Scope

This candidate includes:

- Desktop stroke selection for semantic curve presentation;
- Algebra input preview, commit and inspection behavior;
- product ownership of Algebra presentation controls;
- GeoCeDG-only enforcement and presentation of the host Continuity setting;
- the prerequisite contract for mouse-driven semantic Point creation and drag;
- current-token intersection markers and explicit materialization actions;
- bounded view/navigation, locale, accessibility, preferences, document,
  branding, performance and error boundaries.

## Forbidden scope

This candidate does not authorize:

- productive G9U0-R6 or G9U1 code;
- making `GeoLocusV2` implement generic `Path`;
- using a render polyline, stroke hit, viewport, screen coordinate or pointer
  history as semantic incidence or inverse-address authority;
- changing G9U0-R5, G9S1, R4 tokens, selectors, metrics or intersections;
- a second Algebra preview, command, action, style, Continuity or preference
  authority;
- editable Locus/Spline definitions outside the G9A transaction contract;
- automatic kernel creation of new point children;
- implementing the broader G12 view-history, fit-selection/layer, named-view or
  advanced scale/navigation capability;
- G9B, G9C, G9U2 or productive G10; or
- changing GeoCeDG Classic behavior merely to configure the Construction
  product.

## Definitions and layer boundary

- **Semantic curve:** a `GeoLocusV2` with explicit oriented domain,
  branch/component and evaluation authority, regardless of whether its parent
  is `LocusV2`, `SplineV2` or an R5 similarity transform.
- **Presentation stroke:** the view-derived stroked centerline of the current
  render subpaths. It is selection presentation only.
- **Preview:** speculative Algebra-input feedback before an explicit user
  commit. Preview owns no Construction state.
- **Definition inspection:** read-only presentation of a reconstructible parent
  command/definition. It is not redefine.
- **Inverse semantic address:** a current, typed kernel result mapping an
  ephemeral geometric query to zero, one or several semantic addresses.
- **Candidate marker:** a presentation overlay for a current rich-result token;
  it is not a geometric object or token.

The owning dependency direction is:

```text
shared kernel semantic source/rich result/current token
        -> immutable current snapshot/query result
        -> Desktop/application adapter
        -> stroke, inspector, marker, toolbar or status presentation
```

The Desktop may initiate an explicit kernel transaction. It may not compute a
semantic address, solve an intersection or continue a root.

## 1. Stroke-only semantic-curve hit testing

### 1.1 Required behavior

`GeoLocusV2` presentation is curve-like and unfilled. This applies uniformly to
native Locus V2, Spline V2 and R5-transformed semantic curves.

A hit is true only when the hit tolerance intersects the effective selection
stroke of one current render subpath. Closing a visual loop must not make its
interior selectable. Genuine semantic gaps and distinct subpaths remain
unhittable between their endpoints. Stroke thickness and the ordinary view hit
tolerance may enlarge the presentation-selection band; they do not establish
incidence. A dashed style may use the host's ordinary curve-selection stroke,
but it must not bridge a genuine semantic subpath boundary.

A zero-scale R5 `COLLAPSED_IMAGE` may follow the approved degenerate
presentation convention, but a presentation hit does not establish a unique
semantic address in its retained source domain.

### 1.2 Placement

The correction belongs in the shared Euclidian drawable/hit-test seam used by
`DrawLocusV2`, following ordinary curve stroke-shape invalidation. It does not
belong in `GeoLocusV2`, semantic evaluation, incidence, metrics or
intersection solving.

### 1.3 Invariants

- Zoom, DPI, stroke and hit tolerance affect only selection presentation.
- A hit never supplies branch, component, parameter, preimage or durable ID.
- View caches may be rebuilt or discarded without semantic revision.
- Interior-area selection and render-subpath bridging are forbidden.

## 2. Algebra preview and explicit commit

### 2.1 Zero-mutation preview

Algebra input preview of `LocusV2`, `SplineV2`, R5 transforms and related
GeoCeDG commands must publish nothing. In particular, preview creates no
`GeoElement`, algorithm, durable identity reservation/record, token, diagnostic
geo, XML state, undo entry or semantic revision. Repeated preview, replacement
of the input text, Escape/cancel and loss of focus leave the authoritative
Construction exactly unchanged.

Preview may parse syntax, resolve command/help metadata and display bounded
non-authoritative feedback. It must stop before argument helpers or default
dependencies can publish semantic objects. Command processors that can publish
GeoCeDG participation must honor the host non-scripting preview contract before
processing arguments.

### 2.2 Explicit commit

Enter or an equivalent explicit commit evaluates the accepted expression once
through the ordinary Algebra processor and publishes exactly the intended
result and dependencies in one normal undoable transaction. A successful
Spline V2 commit creates one semantic `GeoLocusV2`; it must not first create a
preview spline and then replace it. A failed commit is atomic and follows the
typed command failure contract.

This contract reuses the host scheduled-preview and silent-evaluation seam. It
must not create a GeoCeDG-only input widget or weaken the normal construction
participation batch.

## 3. Read-only definition inspection

Double-clicking or invoking Inspect definition on a non-editable semantic curve
must expose its current reconstructible parent definition in a read-only,
copyable and keyboard-accessible control. Examples include the approved
`SplineV2(...)` form and ordinary R5 transform commands around a semantic
source. The view may normalize formatting; it need not reproduce the user's
original whitespace.

Definition visibility and editability are separate properties:

- `GeoLocusV2` remains non-editable under the existing contract;
- showing a definition creates no redefine context and performs no mutation;
- long definitions wrap or scroll within a bounded normal Desktop window; and
- undefined or unavailable parent definitions produce a localized typed state,
  not an editable text field or raw internal exception.

The existing Algebra/Properties definition provider remains the authority.
Free-input compatible redefine, including `k=0.25`, is a separate G9A-backed
G9U1 interaction. A label may locate an explicitly intended command-context
target there, but never becomes durable identity. This inspection contract must
not be used to bypass the G9A compatibility predicate or atomic transaction.

## 4. Algebra description-mode state

The Algebra description-mode menu is one mutually exclusive presentation
choice backed by the existing Algebra-style setting. On every initial build,
menu rebuild, font update, English/Spanish language refresh and left-to-right or
right-to-left refresh, exactly one available description mode is selected and
it equals the current Algebra-style setting.

Tree sort mode, dependency ordering, menu item order and transient widget state
are not the selected-mode authority. The control uses one radio/button-group or
equivalent host abstraction and writes only the existing presentation setting.
Changing it produces zero Construction, DAG, identity, semantic-revision and
undo changes.

## 5. Deterministic product policy: Continuity OFF

GeoCeDG CeDG Construction uses the existing host Continuity option and locks it
`OFF` as a product invariant. It does not introduce a second deterministic-mode
field.

The product policy wins before geometric publication over:

- clean or previously saved application preferences;
- application restart and restored window/profile state;
- workspace switching;
- native `.cedg` load; and
- compatibility `.ggb` load, including input carrying Continuity `ON`.

The Advanced settings surface must not permit mouse, keyboard, preference or
script interaction to enable Continuity in GeoCeDG. It may omit the control or
show a localized, accessible read-only/disabled policy indication. Native save
uses the existing host serialization seam to retain `OFF`. A compatibility
`.ggb` source remains byte-preserved under the R2 transition policy.

The separate GeoCeDG Classic diagnostic process retains upstream Continuity
configurability and its isolated preferences. Enforcement must be selected by
product/application configuration, not by mutating the shared Classic default.
Applying or refreshing this policy changes no Construction state.

## 6. G9U0-R6 prerequisite: inverse semantic address

### 6.1 Gate

Mouse-driven Point creation and drag on a semantic curve require a kernel-owned
inverse semantic-address resolver. That capability is a separate mandatory
gate:

```text
G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT
(shared-kernel inverse semantic-address resolution)
design authorization = false
implementationStarted = false
implementationAuthorized = false
```

G9U1 must not be authorized for implementation until G9U0-R6 is implemented,
validated and closed `PASS — AUTHOR APPROVED`, followed by a separate G9U1
authorization. This candidate neither designs R6 exhaustively nor authorizes
its implementation.

The existing explicit public forms that already receive a branch/component and
semantic parameter remain valid; they are not evidence that a screen click can
derive that address.

### 6.2 Minimum R6 result contract

Given a semantic source and an ephemeral world-coordinate query with an
explicit deterministic work/tolerance policy, the shared kernel must return a
typed current result containing zero, one or several candidates. Each candidate
must carry, where applicable:

- durable source identity and current revision evidence;
- branch and component lineage;
- canonical oriented semantic parameter/address;
- periodic canonicalization and seam/monodromy evidence;
- residual/error and local uniqueness/admissibility evidence; and
- an explicit ambiguity, undefined, work-limit or unsupported status.

The result is not a persistent point and does not reserve identity. View or
pointer coordinates may seed the query only. They never become the stored
semantic address or choose between several semantic preimages.

### 6.3 Required source closure

R6 must treat every approved `GeoLocusV2` producer through one semantic source
contract, including Spline V2 and R5-transformed curves. It must preserve the
source domain, orientation, branch/component and periodic policy; it must not
invert the render cache.

An invertible similarity may use approved semantic transform evidence, but the
resolved address still belongs to the transformed semantic source and its
normal DAG context. A zero-scale `COLLAPSED_IMAGE`, self-intersection,
multi-preimage point, knot/component boundary, unresolved periodic seam or any
other nonunique inverse fails closed unless the kernel result explicitly exposes
multiple candidates for user disambiguation. No address may be fabricated from
coordinates, nearest distance, enumeration order or gesture history.

### 6.4 Future G9U1 consumer

After R6 PASS, a stroke hit may select a candidate source. The Point tool asks
R6 for semantic candidates:

```text
stroke hit -> source candidate -> R6 inverse query
           -> zero: typed no-address state, no point
           -> one: explicit commit through existing semantic-point algorithm
           -> many: explicit kernel-candidate disambiguation or cancel
```

It must never fall back silently to an ordinary free point after a semantic
curve was the intended target. A committed point stores the exact semantic
address and normal source dependency.

Dragging such a point is a semantic-position update, not Cartesian retargeting.
Each drag query goes through the approved resolver/transaction; branch and
component remain fixed unless a later contract explicitly permits a transition.
Ambiguity, knot/component boundaries, seam/monodromy uncertainty, undefined
source or work-limit failure stops/fails closed. No pointer trajectory becomes
identity.

## 7. Rich-intersection markers and materialization

The active or Algebra-selected rich result remains semantic authority. The
Construction workspace may show current-token candidate markers, default `ON`
for that active-result context:

- only current, finite, point-admissible exact-token solutions are selectable;
- inactive results do not contribute overlays;
- stale, dormant, quarantined, overlap-only, unresolved or ambiguous evidence
  has no selectable marker;
- markers are no `GeoElement`, ID, XML, DAG, Protocol, undo/copy object,
  semantic revision or token; and
- pointer proximity may choose among already established current tokens, but
  never establishes or continues identity.

The G9S1 piecewise-polynomial Locus V2 × Locus V2 rich-only boundary remains:
without a symmetric certified unique pair selector, candidates are inspectable
evidence but have no selectable marker or materialization action.

The inspector must support, without manual opaque-token handling:

- create the selected point;
- create several selected admissible points; and
- create all currently eligible points.

Every output has its own exact current token. A multi-create operation is one
visible coherent undoable transaction. Cancel creates nothing. The inspector
remains usable for repeated materialization until explicitly closed, identifies
already-materialized solutions, remains keyboard accessible and uses compact
labels whose size is independent of opaque-token length.

Optional auto-materialization is an explicit, visible and undoable frontend
transaction immediately after the rich query is created. Kernel recompute,
load or later root appearance never creates new `GeoPoint` nodes. Kernel
automatic reactivation remains limited to an already-existing exact-token point
whose durable selector again resolves uniquely.

## 8. View navigation and ZoomWindow boundary

Zoom, pan, fit and view-history operations belong to the frontend/view layer.
They change view coordinates and preferences only; they never change metric,
intersection, inverse-address, token or Construction authority.

G9U1 must expose a bounded `ZoomWindow` action through the single action
registry, menu, toolbar placement and keyboard-accessible route. It reuses the
ordinary Euclidian rectangle gesture and view-coordinate transform; it creates
no geometric rectangle, undoable Construction object or semantic clipping
state. Cancel and invalid/zero-area rectangles leave the view and Construction
unchanged. Pointer pixels define only the requested viewport rectangle and are
never geometric authority.

Inherited pan, zoom in/out, standard view and show-all/fit-all may be grouped
with it where the host already supports them. `ZoomPrevious`, fit-to-selection,
fit-to-layer, named views, fixed-scale workflows and broader navigation history
remain G12 scope. G9U1 must not silently absorb those G12 capabilities.

## 9. English/Spanish product language boundary

The initial validated GeoCeDG Construction product locales are English (`en`)
and Spanish (`es`). All GeoCeDG-owned action names, help, statuses, typed
errors, inspector text, Continuity policy text and accessibility names must be
complete in both. Command localization/delocalization maps both languages to
the same internal command IDs and argument grammar; labels are not translated
as identity.

The Construction product must not claim another locale as supported until its
GeoCeDG-owned bundle and validation matrix are complete. For an unsupported or
missing product locale, the deterministic fallback is the English base value
plus a validation/runtime diagnostic; raw localization keys are never normal
UI. This allowlist/fallback is product configuration, not a deletion or mutation
of upstream bundles. GeoCeDG Classic retains its upstream language surface.

Repeated English/Spanish switching, command-dictionary rebuild, font update and
menu rebuild must leave actions, help, feature policy and selected UI state
current, without duplicates or stale strings.

## 10. Documents, preferences and compatibility

- `.cedg` remains native GeoCeDG identity; `.ggb` remains non-destructive
  compatibility input.
- Current ZIP/XML and `app="classic"` internals remain unchanged.
- Workspace, layout, marker visibility, inspector position, theme/accent and
  product locale are application/presentation preferences, not geometric
  identity or semantic dependencies.
- Continuity uses the one existing host kernel/XML field and is always saved
  `OFF` by the GeoCeDG product.
- Candidate markers and preview state never enter XML.
- Exact-token materialized points and semantic-point addresses persist through
  their existing kernel algorithms.
- Workspace or language changes must not trigger a `.ggb` to `.cedg` save or
  rewrite an input document.
- GeoCeDG Classic preserves supported native semantic objects without gaining
  experimental creation authority.

The retained risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains explicit. Marker or
inspector validation does not close it; G9U1 must resolve it or obtain the
required author disposition under its current risk authority.

## 11. Visual identity, branding and accessibility

The Construction workspace uses a restrained, professional frontend identity
distinct from the Classic diagnostic route. Theme/accent, grouping, spacing,
status/help and active-workspace presentation have no geometric meaning and
must pass contrast, keyboard, focus, normal/high-DPI and text-scaling checks.

Branding retains two provenance-owned logical roles:

- `geocedg.brand.topbar`: frontend/top-bar/product chrome; and
- `geocedg.brand.startup`: startup/application identity and only suitable,
  verified deterministic frame/package/`.cedg` association derivatives.

Each author source records filename, SHA-256, dimensions, format/alpha and
provenance. Derived assets record tool, version and options and reproduce
identical bytes. Missing sources use an explicit text/default fallback; the
implementation does not fabricate a logo or copy upstream branding. Classic
retains a visibly distinct diagnostic identity.

## 12. Performance contract

- A stroke hit uses the current drawable selection shape and performs no
  semantic solve.
- Algebra preview performs no durable construction work.
- Marker/inspector presentation consumes one current rich-result snapshot and
  its selector/token index; it performs no solve per marker or point.
- Existing exact-token point validation remains direct selector lookup, with
  the accepted R4 bound characterized around `O(R log R + P)` for `R` roots and
  `P` existing bindings, subject to re-verification at implementation time.
- Workspace, language, Algebra-style and theme changes cause zero semantic
  evaluations.
- G9U0-R6 must separately declare, instrument and validate deterministic inverse
  query budgets. G9U1 may not conceal an unbounded resolver behind pointer
  motion or issue an independent global solve per candidate.

## 13. Failure and recovery

All failures are localized, typed and fail closed:

- an ambiguous/missing inverse address creates no free or semantic point;
- failed/cancelled preview, commit, definition inspection or materialization
  leaves Construction and undo state unchanged;
- a stale/inadmissible token cannot be selected or materialized;
- a multi-create transaction is all-or-nothing;
- menu/language/resource failure uses the declared accessible fallback and does
  not create a hard-coded second action catalog;
- Continuity cannot become transiently `ON` while loaded geometry is published;
- workspace/view switch failure rolls back presentation only; and
- unsupported pair-intersection or collapsed-image evidence remains
  inspectable without fabricated isolated points.

Raw Java exceptions, opaque token strings as layout-sizing labels and silent
fallback to screen/coordinate identity are forbidden public recovery paths.

## 14. Validation

The future G9U1 validation matrix must cover at least:

| Family | Required scenarios |
|---|---|
| `U1-H` | stroke/closed-interior/genuine-gap hit testing across native, spline and transformed Locus V2; zoom/DPI/style purity |
| `U1-IN` | repeated preview/cancel/replacement zero mutation; one explicit successful commit; failed commit atomicity; historical R1 preview regression |
| `U1-DEF` | read-only Spline/transform definition, long/undefined definition accessibility, zero G9A/redefine mutation |
| `U1-AV` | exactly one Algebra description mode from the Algebra-style setting across rebuild/font/EN/ES/RTL; zero semantic mutation |
| `U1-D01`–`U1-D08` | Continuity `OFF` precedence, one host field and Classic configurability |
| `R6-P` then `U1-PNT` | zero/one/multiple inverse candidates, transformed source, collapsed/self-intersection/seam/knot ambiguity, exact semantic commit and drag; no free-point fallback |
| `U1-I01`–`U1-I14` | markers, selected/multiple/all materialization, persistent inspector, explicit auto transaction and periodic-risk disposition |
| `U1-C`/`U1-L` | English/Spanish discovery, syntax/help, feature parity, rebuild and fallback |
| `U1-Z` | real menu/toolbar/keyboard `ZoomWindow`, rectangle/cancel/zero-area behavior and zero Construction mutation; broader G12 navigation remains excluded |
| `U1-B`/`U1-A` | visual identity, accessibility, two brand roles and deterministic assets |
| `U1-P01`–`U1-P02` | overlay/selector work bound and bounded long-token inspector layout |

Every family requires deterministic rerun where applicable, relevant G9U0/R1/
R2/R3/R4/R5/G9S1, G9A, G9X1, G5 and Classic controls, Checkstyle, Git diff
checks and full `tools/agent/verify.ps1`. UI automation must exercise actual
menu, Algebra, keyboard and Graphics routes rather than controller-only calls.

## 15. Stop conditions and open decisions

Stop for author review if:

- stroke-only selection would require semantic incidence changes;
- preview cannot be made mutation-free without weakening the normal
  Construction transaction;
- definition visibility requires making `GeoLocusV2` editable;
- Algebra description state requires a second setting;
- Continuity enforcement requires a second persistence field, changes Classic
  or permits transient continuous publication;
- interactive semantic Point or drag lacks G9U0-R6 PASS, needs
  coordinate/proximity/order/history identity or cannot cover transformed
  curves fail-closed;
- marker/materialization code would solve geometry or create nodes during
  recompute;
- G9A, R4/R5/G9S1 semantics, the broader G12 navigation family or another
  roadmap gate must be broadened; or
- a claimed locale/brand resource lacks complete validation or provenance.

The following decisions remain for explicit author review before productive
G9U1 authorization:

1. the exact normal/floating initial state of Properties;
2. the final visual palette/accent and review of supplied brand assets;
3. the final user-facing wording for disabled Continuity and inverse-address
   ambiguity;
4. whether the product language selector hides unsupported upstream locales or
   shows them as unavailable with reason (only English and Spanish may be
   claimed supported by this candidate); and
5. the separately designed R6 inverse-query tolerance/work-budget and semantic
   drag transition policy.

Terminal design state:

```text
G9U0-R6 = NOT AUTHORIZED / NOT IMPLEMENTED
G9U1 = DESIGNED — NOT AUTHORIZED / NOT STARTED
selfApproved = false
authorApproved = false
passClaimed = false
```

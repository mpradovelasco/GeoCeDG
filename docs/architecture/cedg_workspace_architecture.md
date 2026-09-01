# CeDG Construction workspace architecture — post-G9S1 candidate

- Status: **DEFINITIVE POST-G9S1 DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Phase: G9U1 `DESIGNED — NOT AUTHORIZED / NOT STARTED`
- Mandatory new prerequisite: G9U0-R6 — Semantic Locus Point Interaction
  Support (kernel inverse-address resolution), `NOT AUTHORIZED / NOT
  IMPLEMENTED`
- Product baseline consumed: G9U0 through G9U0-R5 and G9S1
  `PASS — AUTHOR APPROVED`
- Governing normative workspace contract:
  `geocedg/specs/ui/cedg-workspaces.md`
- Companion interaction candidate:
  `geocedg/specs/ui/g9u1-construction-interaction.md`
- Accepted workspace decision: ADR 0012

This document supersedes the pre-R2 explanatory architecture as the prospective
post-G9S1 G9U1 design. It changes no product code and does not authorize either
G9U0-R6 or G9U1.

## Outcome

CeDG Construction remains a schema-v2 evolution of the existing GeoCeDG
application profile, not a second toolbar, command processor or geometry
system. One manifest defines stable actions and workspace placement. Product
configuration owns presentation and policy. The shared kernel owns every
semantic curve, address, metric, intersection result, token and constructed
point.

The eleven approved professional action groups remain intact. G9U1 presents the
closed G9U0–R5 and G9S1 capabilities, including semantic Spline V2 and
transformed Locus V2, without reimplementing them. G9B and G9C remain
independent and unauthorized; G9U2 remains separately blocked.

The intended dependency direction is:

```text
apps/geocedg/application-profile.yml (future schema v2)
  -> ProfileManifestLoader + strict schema validation
  -> ActionRegistry -----------------> localization/help/icon resolvers
  -> WorkspaceRegistry --------------> toolbar/menu/view compilers
  -> WorkspaceController ------------> GuiManager / Layout / controller
  -> WorkspacePreferenceAdapter -----> isolated GeoCeDG preferences

AppConfigGeoCeDG / product policy
  -> feature availability
  -> existing host Continuity option forced OFF
  -> EN/ES product locale policy

shared kernel semantic authority
  -> GeoLocusV2 / SplineV2 / transformed GeoLocusV2
  -> rich metric/intersection result + current exact tokens
  -> existing exact semantic-point algorithms
  -> future G9U0-R6 inverse semantic-address result

Desktop presentation clients
  <- immutable semantic snapshots only
  -> explicit normal kernel transactions only
```

No arrow runs from workspace, hit testing, marker, view coordinates, theme or
language state back into semantic truth.

## Gate and authority reconciliation

The architecture consumes these closed product authorities:

- R2: ordinary Locus V2 visual-style authority and native `.cedg` document
  lifecycle with `.ggb` compatibility input;
- R3: stable GeoCeDG menu lifecycle, real inspector route, hidden exact-token
  auxiliary and bounded opaque-token presentation;
- R4: deterministic current-state selectors/tokens, intrinsic phase/rank,
  active/dormant/reactivated existing points and Continuity-independent
  identity;
- R5: all approved similarity transforms produce new first-class semantic
  `GeoLocusV2` objects, including truthful `COLLAPSED_IMAGE`; and
- G9S1: semantic Spline V2 is a `GeoLocusV2` source, with one-sided supported
  rich intersections and a deliberately rich-only spline×spline boundary where
  a symmetric materializable selector is not certified.

The current post-G9S1 prompt is prospective, unexecuted input. This architecture
adds the mandatory G9U0-R6 dependency because ordinary pointer hit testing does
not provide the semantic preimage needed by the interactive Point tool. R6 must
be separately designed, authorized, implemented, validated and closed
`PASS — AUTHOR APPROVED` before any G9U1 execution authorization.

## Architectural placement map

| Existing source/seam | Current responsibility | Prospective bounded owner |
|---|---|---|
| `apps/geocedg/application-profile.yml` | schema-v1 product profile | sole schema-v2 action/workspace instance |
| `geocedg/specs/ui/application-profile.schema.json` | validates schema v1 | versioned strict v2 schema plus deterministic v1 adapter |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java` | loads profile and compiles one toolbar | immutable profile/action/workspace definitions; no raw duplicate catalogs |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java` | product app/profile lifecycle | product-policy selection, workspace controller and presentation-only document layout |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GuiManagerGeoCeDG.java` | product GUI manager | manifest toolbar/menu/view adapters |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMenuBar.java` | R3 product-menu lifecycle | render the single action registry and retain R3 rebuild behavior |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianController.java` | product action gesture adapter | consume typed kernel results; never solve or infer identity |
| `source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/DrawLocusV2.java` | semantic-curve render path and current area-like hit | curve-stroke-only presentation hit consistent with semantic subpaths |
| `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianDraw.java` | drawable routing | keep every `GeoClass.LOCUS_V2` producer on the common drawable seam |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/inputbar/AlgebraInputD.java` | preview scheduling and explicit commit | reuse unchanged lifecycle; no GeoCeDG input fork |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/ScheduledPreviewFromInputBar.java` | host silent preview | one zero-publication preview authority |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocusV2.java` | sealed preview-safe command precedent | contract pattern for every publishing GeoCeDG command |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdSplineV2.java` | G9S1 command creation | stop non-scripting preview before arguments/default helpers publish |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/view/algebra/AlgebraControllerD.java` and `AlgebraViewD.java` | double-click/edit/Properties routing | route noneditable semantic curves to bounded read-only definition inspection |
| `source/shared/common/src/main/java/org/geogebra/common/gui/dialog/options/model/ObjectNameModel.java` and Desktop `NamePanelD.java` | definition visibility/editor model | separate definition visibility from editability; keep Locus V2 noneditable |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/view/algebra/AlgebraHelperBar.java` | Algebra description menu | select exactly one item from the existing Algebra-style setting |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/dialog/options/OptionsAlgebraD.java` | existing Algebra-style setting UI | remain the presentation-state authority |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/dialog/options/OptionsAdvancedD.java` | host Continuity control | product-specific locked/disabled/read-only presentation |
| shared `Kernel` / XML settings seams | existing Continuity value and serialization | single setting authority, clamped OFF by GeoCeDG product policy |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java` and `AlgoSemanticLocusPoint2D.java` | explicit exact semantic-point publication | remain final commit path after an approved address exists |
| future G9U0-R6 shared-kernel seam | absent | typed inverse-address resolver for all semantic `GeoLocusV2` producers |

Changes inside upstream-owned source paths must be the minimum compatible patch
when G9U1/R6 are separately authorized. This architecture does not authorize
any of them now.

## Components

### 1. Profile manifest loader and action registry

The loader reads one packaged profile, validates its schema before constructing
immutable definitions and fails closed on unknown fields or references. It
does not scan the filesystem for fragments. A v1 profile is lifted by a
deterministic in-memory adapter and never rewritten automatically.

The action registry owns stable IDs and immutable metadata: kind/target,
selection grammar, maturity, feature dependencies, localization/help keys,
icon ID, unavailable policy and output category. Toolbars, menus, overflow,
context actions, inspectors and help consume that same entry. Runtime widgets
do not repeat targets or product strings.

Workspace membership changes discoverability only. Command existence, runtime
feature permission, creation permission and compatibility loading remain
independent decisions. `--enableLocusV2=true` remains the only public V2 opt-in;
there is no separate spline, intersection or transform flag.

The action catalog retains the eleven approved professional groups:

1. Inspect and construct;
2. Linear geometry;
3. Parameters and drivers;
4. Relations and intersections;
5. Circles, conics and curves;
6. Locus V2 and semantic Spline V2;
7. Metrics and validation;
8. Transformations and manual projections;
9. gated CeDG procedures/developments placeholders;
10. Presentation and document; and
11. Automation and import/export.

Gated placeholders cannot publish an unavailable semantic capability.

### 2. Workspace registry and controller

The registry exposes immutable workspace definitions. The controller performs
one atomic UI-only transition:

1. validate the target workspace and action availability;
2. cancel an unfinished selection/preview transaction and activate Move;
3. capture customizable dock state under the old workspace ID;
4. compile and apply views, toolbar, menus, status/help and visual identity;
5. restore compatible saved dock state;
6. persist the active workspace ID; and
7. publish a presentation-only notification.

It writes no `Construction`, `Kernel` geometry, command output, semantic
revision or undo entry and initiates no Locus evaluation. A failed transition
restores the previous presentation atomically.

### 3. Semantic-curve stroke selection

All `GeoClass.LOCUS_V2` producers share `DrawLocusV2`. Their selection shape is
the effective stroke around each current render subpath, not the area enclosed
by a closed path. The interior of a closed spline/locus is not a hit; a genuine
semantic gap is not bridged. Normal line thickness and hit tolerance may widen
the band but never establish semantic incidence.

This correction is presentation-only. Hit testing may identify a candidate
source object for a gesture. It cannot identify branch, component, parameter,
preimage, point-on-curve membership or token. A `COLLAPSED_IMAGE` presentation
does not imply a unique retained-domain address.

### 4. Algebra preview, commit and definition inspection

The existing scheduled-preview path remains the single preview authority. A
GeoCeDG command preview may parse and present syntax/help but must stop before
arguments, helpers or default dependencies publish anything. There is no geo,
algorithm, identity reservation, XML, undo, token or semantic-revision change.
Repeated preview, cancellation and replacement of text are exact no-ops on the
authoritative Construction.

Explicit Enter/commit evaluates once through the ordinary Algebra processor and
publishes one normal atomic result graph. Spline V2 follows the sealed
`CmdLocusV2` non-scripting preview precedent rather than creating a parallel
input system or weakening construction participation.

Definition inspection is a separate read-only route. A noneditable
`GeoLocusV2` exposes the reconstructible parent definition (including
`SplineV2` and transform composition) in a bounded, copyable, keyboard-
accessible control. Visibility of a definition is not editability. Inspection
does not open G9A redefine context and does not mutate Construction.

The future free-input `k=0.25` compatible-redefine UX remains a distinct G9A
consumer: label only locates an explicitly intended command-context target;
identity preservation requires the accepted compatibility predicate and atomic
transaction; ambiguous/absent/incompatible targets fail or follow existing
new-identity semantics. Inspection is never its bypass.

### 5. Algebra description-mode presentation

The description menu has one mutually exclusive selected item derived from the
existing Algebra-style setting. It must not derive selection from tree sort
mode, dependency order, menu index or stale widget state. Initial build,
rebuild, repeated font refresh, English/Spanish switch and component-orientation
refresh reproduce exactly one correct radio/check state.

Changing it writes only the existing presentation setting and performs zero
Construction, DAG, identity, semantic revision or undo work.

### 6. GeoCeDG deterministic product policy

GeoCeDG CeDG Construction forces the existing host Continuity setting `OFF`.
This is application/product policy, not a workspace toggle, new kernel concept
or second persisted field. Enforcement occurs before loaded or restored state
can publish geometry and wins over preferences, restart, workspace/profile
state, native `.cedg` and compatibility `.ggb` input.

The Advanced settings surface cannot enable it and supplies a localized,
accessible locked-policy indication if the control remains visible. Native
save records `OFF` through the existing XML setting. A compatibility input is
not rewritten. Product-policy refresh mutates no Construction.

The separate GeoCeDG Classic diagnostic process retains upstream
configurability and isolated preferences. Shared kernel defaults are not
globally changed to implement the product rule.

### 7. G9U0-R6 inverse-address resolver

The current explicit semantic-point operation already works when
branch/component and parameter are known. The ordinary mouse Point tool does
not know that address. `GeoLocusV2` deliberately does not implement generic
`Path`, and a screen hit or nearest render vertex cannot fill the gap.

G9U0-R6 is therefore a mandatory separate shared-kernel phase:

```text
semantic GeoLocusV2 source + ephemeral world query + deterministic policy
  -> typed current inverse-address result
     {zero | unique | ambiguous | undefined | unsupported | work-limit}
  -> candidates carrying source revision, branch/component, canonical u,
     periodic/seam evidence, residual/error and local uniqueness evidence
```

R6 must cover native Locus V2, Spline V2 and R5-transformed semantic curves
through one source contract. It preserves semantic domain, orientation,
branches/components and periodic policy. It never inverts a render cache. An
invertible similarity may contribute approved transform evidence; source and
transformed objects remain different DAG/identity contexts. A zero-scale
`COLLAPSED_IMAGE`, self-intersection, multiple preimages, knot/component
boundary or unresolved seam must expose ambiguity/nonuniqueness and fail closed
rather than fabricate an address.

R6 design must define deterministic tolerance/work budgets, typed results,
periodic and transformed semantics, persistence implications if any, and tests.
It is `NOT AUTHORIZED / NOT IMPLEMENTED`; this architecture is not that design.

Only after R6 is `PASS — AUTHOR APPROVED` may the G9U1 Point gesture consume it:

```text
stroke hit -> candidate semantic source -> R6 query
zero       -> no point + typed reason
unique     -> explicit existing semantic-point commit
ambiguous  -> explicit kernel-candidate chooser or cancel
```

No semantic-curve gesture silently falls back to a free point. Drag updates an
explicit semantic-position dependency through the approved R6/transaction
seam; it does not store Cartesian pointer positions or trajectory history.
Ambiguity, component/knot/seam transition, undefined input or work-limit failure
stops or fails closed.

### 8. Rich-result markers and point materialization

For the active/Algebra-selected rich result, the frontend may present markers
for current finite point-admissible exact tokens. Markers default ON in the
Construction workspace and remain overlay-only: no geo, ID, XML, DAG, Protocol,
undo, copy, revision or token. Inactive, stale, dormant, quarantined,
overlap-only, unresolved or ambiguous evidence has no selectable marker.

Pointer proximity can choose among already established tokens; it cannot
discover or continue identity. R4 intrinsic phase/rank and current certificate
remain kernel-only. Spline×spline rich-only candidates without symmetric
certified uniqueness remain inspectable but not selectable or materializable.

The persistent inspector supports create-one, create-selected and create-all
for current eligible tokens. Each output gets its own exact token. Multi-create
is one visible coherent undo transaction. The inspector remains open for
repeated work, identifies existing outputs, supports keyboard operation and
uses compact token-independent labels. Cancel commits nothing.

Optional auto-materialization is a separate explicit frontend transaction just
after rich-query creation. It is visible and undoable. Kernel recompute, load
and later root appearance never create a new node. R4 automatic reactivation
applies only to an already-existing exact-token point.

### 9. View navigation and ZoomWindow

View coordinate transforms, pan, zoom, fit and view history are frontend state
and never metric, clipping, address or identity authority. G9U1 exposes a
bounded `ZoomWindow` action through the one action registry and real menu,
toolbar and keyboard routes. It reuses the ordinary Euclidian rectangle gesture
and view-transform seam, creates no geometric rectangle or Construction undo
entry, and leaves state unchanged on cancel or an invalid/zero-area rectangle.

Inherited pan, zoom in/out, standard view and show-all/fit-all may share its
navigation group. `ZoomPrevious`, fit-selection/layer, named views, fixed scale
and broader navigation history remain G12. View pixels never supply semantic
clipping, hit, inverse-address or identity evidence.

### 10. Localization, help, branding and accessibility

The initially validated GeoCeDG Construction locale set is English and Spanish.
Every GeoCeDG-owned action, help text, selection role, error, Continuity-policy
message, inspector label and accessible name is complete in both. Both map
localized command discovery/help to the same internal IDs and grammar. Missing
or unsupported product localization follows an English-base fallback and emits
a diagnostic; raw keys are not normal UI.

The allowlist/fallback belongs to GeoCeDG product configuration and does not
delete upstream resources. The exact presentation of unsupported locales
(hidden or unavailable-with-reason) remains an author-review choice. GeoCeDG
Classic retains the upstream language surface.

Repeated EN/ES changes rebuild menus, action help, command dictionaries and
radio/check state without stale entries or duplicates. Status/help presents the
next typed selection role and bounded failure details.

Visual identity remains restrained, professional, accessible and distinct from
Classic. Theme/accent changes are presentation-only. The two logical brand
roles remain:

- `geocedg.brand.topbar` for top-bar/product chrome; and
- `geocedg.brand.startup` for startup/application identity and only verified
  deterministic size/platform derivatives.

Each source and derivative has recorded provenance and deterministic hashes.
Missing author assets use explicit fallback; no substitute logo or upstream
branding is fabricated. Contrast, focus, keyboard paths and normal/high-DPI
scaling are mandatory.

### 11. Preferences and document layout

Application preferences own active workspace, per-workspace dock layout,
marker visibility, inspector presentation, locale and supported theme/accent.
They are not geometric inputs. Loaded document perspectives remain transient
`Document layout` presentation and can be replaced by Reapply workspace without
changing Construction.

`.cedg` remains native and `.ggb` compatibility input. Workspace/UI changes do
not duplicate or override the R2 open/save state machine. Current ZIP/XML and
`app="classic"` remain. Candidate markers and preview never serialize. Exact
semantic addresses/tokens and point children use their existing kernel
persistence. Continuity alone uses its existing host field, always `OFF` in the
product.

The R4 risk `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains tracked. G9U1
must execute or receive the required author-approved disposition; marker tests
cannot silently close it.

## State ownership

| State | Owner | Persistence | Geometric authority |
|---|---|---|---|
| action/workspace definitions | checked-in profile manifest | packaged resource | none |
| active workspace and dock layout | GeoCeDG UI | isolated preferences | none |
| marker/inspector/theme/locale preference | GeoCeDG UI | isolated preferences | none |
| document perspective | existing document presentation contract | current archive presentation XML where already supported | none |
| runtime feature decision | feature service | manifests + approved launch override | creation gate only |
| Continuity | existing host Kernel setting clamped by product config | existing preference/XML seam | deterministic host policy, not geometry identity |
| preview and active selection slots | input/controller | transient | none before commit |
| render path and hit stroke | Euclidian view | derived/transient | none |
| inverse semantic-address evidence | future G9U0-R6 shared kernel | current query result; persistence only if separately designed | semantic query authority |
| rich result/selectors/tokens | shared kernel | existing R4/R5/G9S1 XML/ledger | authoritative in declared scope |
| materialized point | shared kernel/Construction | normal `.cedg` algorithm graph | authoritative semantic child |
| view zoom/pan/history | Euclidian frontend | view/preferences under host policy | none |

## Performance and instrumentation

- Workspace, language, Algebra-style, theme and view changes perform zero
  semantic evaluations and Construction writes.
- Stroke hits use the current drawable selection shape and no kernel solve.
- Preview performs no durable construction work.
- One active rich-result snapshot/token index feeds all markers and inspector
  entries; there is no independent solve per marker or materialized point.
- Existing point binding remains direct selector lookup and the accepted R4
  update is reverified around `O(R log R + P)` for `R` roots and `P` bindings.
- G9U0-R6 separately owns inverse-query budgets and counters. Pointer motion may
  not hide unbounded work, repeat one global solve per candidate or retain an
  unbounded movement history.
- UI controls, help and opaque-token diagnostics remain bounded at normal and
  high DPI.

## Failure and recovery

- Invalid profile/action reference: reject v2 and use the last accepted v1
  profile with an explicit diagnostic; never construct a fallback catalog.
- Workspace switch failure: restore old presentation; Construction untouched.
- Missing feature: action unavailable with localized reason; dispatch not
  called.
- Missing locale/icon/brand source: approved accessible fallback and diagnostic;
  no unregistered asset.
- Preview/cancel/failed commit: zero publication and undo change.
- Definition unavailable: bounded read-only typed state, not editable fallback.
- Continuity hostile input/preference: clamp OFF before publication; input file
  untouched.
- Inverse address zero/ambiguous/unsupported/work-limit: no point and no free-
  point fallback.
- Stale/inadmissible token: no marker/materialization.
- Multi-materialization failure: atomic rollback of the compound action.
- Rich-only pair or `COLLAPSED_IMAGE`: inspect truthful evidence; fabricate no
  isolated token/point.

All user-facing failures use English/Spanish localized typed messages. Raw Java
exceptions, opaque tokens as layout labels, coordinate identity and silent
semantic fallback are forbidden.

## Validation architecture

`docs/validation/g9_public_workspace_validation_matrix.md` remains the composed
scenario authority. The definitive implementation must add and execute focused
families for:

- `U1-H`: semantic-curve stroke hit/interior/gap behavior;
- `U1-IN`: zero-mutation preview and one explicit commit;
- `U1-DEF`: bounded read-only definition inspection;
- `U1-AV`: description-mode state from the Algebra setting;
- `U1-D01`–`U1-D08`: Continuity OFF and Classic control;
- separately gated `R6-P`, then `U1-PNT`: inverse candidates, exact Point commit
  and semantic drag across native, spline and transformed curves;
- `U1-I01`–`U1-I14`: markers, multi-materialization, persistent inspector,
  auto transaction and periodic-risk disposition;
- `U1-C`/`U1-L`: English/Spanish command/action/help parity;
- `U1-Z`: real `ZoomWindow` menu/toolbar/keyboard paths, rectangle/cancel/
  zero-area behavior, view purity and broader G12 exclusion;
- `U1-B`/`U1-A`: visual identity, accessibility and brand provenance; and
- `U1-P01`–`U1-P02`: overlay/selector performance and bounded token UI.

The future focused verifier runs after every required sealed G9U0/R1/R2/R3/R4/
R5/G9S1 authority, relevant G9A/G9X1/G5/Classic controls, Checkstyle, Git diff
checks and full `tools/agent/verify.ps1`. UI tests exercise the real Algebra,
menu, keyboard and Graphics routes. No verifier may substitute direct-controller
invocation for the public path.

## Implementation slices and gates

1. **G9U0-R6 (separate shared-kernel gate):** inverse semantic-address result,
   algorithms, deterministic work/tolerance policy, transformed/periodic/
   ambiguity semantics and focused verification. Not authorized here.
2. **G9U1A:** strict schema-v2 definitions, v1 adapter, one action registry,
   feature/localization/help/icon validation and professional group compiler.
3. **G9U1B:** Construction layout/controller/preferences/document-layout,
   product Continuity/locale policy, Algebra preview/inspection/description
   contracts and stroke-only curve hit presentation.
4. **G9U1C:** R6-consuming interactive semantic Point gesture/drag, current-token
   markers, persistent inspector and explicit one/selected/all materialization.
5. **G9U1D:** visual identity, author brand roles, accessibility, performance,
   packaging consumers where separately in scope and complete regression.

G9U1 implementation begins only after R6 PASS and separate authorization of the
then-canonical prompt. G9B/G9C remain semantically independent. G9U2 remains
blocked by its global G9/procedure authority. Broader G12 navigation is not
folded into these slices.

## Open decisions and STOP boundaries

Author review is still required for:

- initial Properties floating/open state;
- final product palette/accent and supplied brand-asset suitability;
- final wording for the locked Continuity and inverse ambiguity states;
- whether unsupported product locales are hidden or displayed unavailable;
- R6 inverse-query tolerance/work budgets and semantic drag transitions; and
- any G12 navigation beyond the bounded G9U1 `ZoomWindow`/host-navigation set.

Stop before implementation if any slice would:

- put semantic truth in a drawable, marker, workspace or input preview;
- make `GeoLocusV2` a generic `Path`;
- derive an inverse address from coordinates, nearest distance, screen/order or
  movement history;
- begin G9U1 without R6 `PASS — AUTHOR APPROVED`;
- use a second action, Algebra, Continuity, locale or persistence authority;
- make definitions editable outside atomic G9A redefine;
- publish geometry while Continuity is transiently ON;
- create new points during kernel recompute;
- broaden R4/R5/G9S1, G9B/G9C/G9U2/G10 or the deferred G12 family; or
- claim unsupported localization or branding provenance.

Terminal planning state:

```text
G9U0-R6 = NOT AUTHORIZED / NOT IMPLEMENTED
G9U1 = DESIGNED — NOT AUTHORIZED / NOT STARTED
selfApproved = false
authorApproved = false
passClaimed = false
```

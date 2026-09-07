# CeDG Construction workspace architecture — post-R1 implementation candidate

- Status: **DESIGN PASS — AUTHOR APPROVED / POST-R1 RECONCILED**
- Phase: G9U1 design approved; implementation `IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`
- Mandatory entry authorities: G9S1 and G9U0-R6
  `PASS — AUTHOR APPROVED`
- Published R6 authority: `geocedg-g9u0-r6-pass` ->
  `3942af594e4507e479f2c75019cef62e3d9fea6f`
- Protected pre-R6 design checkpoint:
  `857de6628489bda0b65a5ba5145e62ca0795fc32` (historical and immutable)
- Product baseline consumed: G9U0 through G9U0-R6, including G9S1,
  `PASS — AUTHOR APPROVED`
- Governing normative workspace contract:
  `geocedg/specs/ui/cedg-workspaces.md`
- Companion interaction candidate:
  `geocedg/specs/ui/g9u1-construction-interaction.md`
- Accepted workspace decision: ADR 0012

This document carries the protected design forward onto published R6/R1.
The post-R1 planning gate passed before productive edits. The author's explicit
conditional authorization then activated implementation after the no-novelty
audit; this document is design authority, not implementation approval. Actual
candidate/run evidence is linked in the
[implementation report](../validation/g9u1_construction_workspace_implementation_candidate_report.md).

## Outcome

CeDG Construction remains a schema-v2 evolution of the existing GeoCeDG
application profile, not a second toolbar, command processor or geometry
system. One manifest defines stable actions and workspace placement. Product
configuration owns presentation and policy. The shared kernel owns every
semantic curve, address, metric, intersection result, token and constructed
point.

The eleven approved professional action groups, eighteen operational clusters
and 110 stable action definitions remain intact. G9U1 presents the closed
G9U0–R6 and G9S1 capabilities, including semantic Spline V2 and transformed
Locus V2, without reimplementing them. G9B and G9C remain independent and
unauthorized; G9U2 remains separately blocked.

The intended dependency direction is:

```text
apps/geocedg/application-profile.yml (live schema v2)
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
  -> R6 query/resolver/result + interaction-owned point operations

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
  rich intersections; G9S1-R1 now certifies the bounded singleton transverse-germ
  pair subset, with all insufficiently certified/identified roots rich-only; and
- R6: the accepted shared-kernel inverse resolver maps a finite world request to
  typed semantic-preimage candidates and provides exact creation/move seams for
  interaction-owned semantic points without generic `Path` conformance.

The protected checkpoint records how G9U1 review discovered R6 as a mandatory
kernel prerequisite. Published R6 now satisfies that prerequisite. ADR 0019 and
`geocedg/specs/locus/locus-v2-point-interaction.md` are the actual authority;
the protected provisional names and states are historical only. The subsequent
conditional author decision authorized the current implementation candidate;
implementation acceptance remains a separate author decision.

## Architectural placement map

| Existing source/seam | Current responsibility | Prospective bounded owner |
|---|---|---|
| `apps/geocedg/application-profile.yml` | live schema-v2 product profile | sole action/workspace/taxonomy/presentation authority |
| `geocedg/specs/ui/application-profile.schema.json` | validates live schema v2 | strict action, workspace, presentation-group, toolbar and typed menu-entry contract |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java` | loads/validates the profile and compiles its presentation projection | immutable profile/action/workspace definitions; no raw duplicate catalogs |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java` | product app/profile lifecycle | product-policy selection, workspace controller and presentation-only document layout |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GuiManagerGeoCeDG.java` | product GUI manager | manifest toolbar/menu/view adapters |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMenuBar.java` | product-menu lifecycle | render profile-declared groups/actions/separators/host controls from the single registry |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGHostMenuFactory.java` | bounded bridge to existing host view/preference state | construct checked/radio View and Options controls without a second preference store |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserToolLibrary.java` and `GeoCeDGUserTools.java` | isolated installed-tool preference, host Macro activation and dynamic presentation | versioned definition-digest equivalence, grouped pins and optional app-only PNG icons; embedded document macro remains reconstruction authority |
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
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/interaction/LocusPointInteractionQuery2D.java`, `LocusPointInteractionResolver2D.java` and `LocusPointInteractionResult2D.java` | published R6 typed inverse resolution | consume a finite world request and bounded policy; return exact resolver-owned candidates/status, never presentation identity |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2PublicOperations.java` and `AlgoSemanticLocusPoint2D.java` | exact semantic-point publication plus R6 interaction-owned create/move | G9U1 invokes the public R6 operations; the same point, durable ID, source and exact address state remain kernel-owned |

Changes inside upstream-owned source paths must be the minimum compatible patch
under the current explicit conditional author authorization after planning
validation. This architecture does not reopen published R6/R1 kernel semantics.

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

The 11 professional families and 18 operational clusters are classification and
traceability. The same manifest separately declares 28 ordered presentation
groups, 11 primary-toolbar group references and typed menu entries. The
compiler proves exact action reachability and uniqueness before constructing
the seven menus. File/Edit flattening, separators and host-control placeholders
therefore remain profile data rather than Java-owned ordering. Construction and
the toolbar reference the same final semantic presentation groups.

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

### 7. Published R6 inverse-address authority and G9U1 consumer

The explicit command `Point(L,"branch",u)` remains the exact scripting/command
surface when the semantic address is already known. It deliberately does not
create an R6 interaction-owned point and cannot be moved through the R6 edit
operation. The ordinary mouse Point tool instead consumes the published R6
pipeline:

```text
frontend stroke hit on one semantic curve
  -> finite world/geometric target + transient world-radius policy
  -> new LocusPointInteractionQuery2D(source, x, y, policy)
  -> new LocusPointInteractionResolver2D().resolve(query)
  -> LocusPointInteractionResult2D
```

The exact result statuses are:

```text
NO_ADMISSIBLE_PREIMAGE
UNIQUE_ADMISSIBLE_PREIMAGE
MULTIPLE_SEMANTIC_PREIMAGES
UNRESOLVED_NUMERICAL_SEARCH
INVALID_SOURCE
DEGENERATE_SOURCE_IMAGE
UNSUPPORTED_CAPABILITY
```

`NO_ADMISSIBLE_PREIMAGE` and `UNIQUE_ADMISSIBLE_PREIMAGE` require complete
requested-scope evidence. One locally established candidate under
`BOUNDED_EVALUATOR_SEARCH` remains `UNRESOLVED_NUMERICAL_SEARCH` and is not a
creation candidate. `MULTIPLE_SEMANTIC_PREIMAGES` exposes resolver-owned
`LocusPointInteractionCandidate2D` values for an explicit chooser. Their
constructor is package-owned; the frontend retains and passes the selected
candidate object exactly rather than rebuilding an address from a UI ordinal.
Candidate collection order and world distance are transient presentation/work
evidence, never identity.

Each candidate contains the exact `LocusSemanticAddress2D`, source revision,
forward-evaluated point, world residual, semantic interval, regularity, numeric
guarantee, method and established local evidence. Automatic creation is legal
only for `UNIQUE_ADMISSIBLE_PREIMAGE`; an explicit chooser may select exactly
one candidate from `MULTIPLE_SEMANTIC_PREIMAGES`. Both paths call:

```java
LocusV2PublicOperations.createInteractiveSemanticPoint(
    construction, label, source, selectedCandidate)
```

Every other status creates no point and supplies localized truthful feedback.
There is no silent free-point fallback.

An interaction-owned point uses a normal `AlgoSemanticLocusPoint2D` parent,
stable `LOCUS_INTERACTION_POINT` role and dedicated hidden address-state inputs.
Its durable selector is source/provider/branch/component/canonical parameter/
periodic lift/seam side; pointer coordinates disappear as authority after
selection. Dragging calls:

```java
LocusV2PublicOperations.moveInteractiveSemanticPoint(
    point, targetX, targetY, policy)
```

The operation accepts only an R6-owned point with a current exact address,
resolves before mutation, and writes the exact address inputs atomically only
for `UNIQUE_ADMISSIBLE_PREIMAGE`. Ambiguous, unresolved, invalid, degenerate or
unsupported moves leave the point inputs untouched; dormant points are not
globally reattached. The frontend groups successful drag edits in ordinary undo
history. A failed atomic rollback can reconstruct Java instances, so the
gesture aborts and reacquires current objects rather than retaining stale
references.

R6 covers certified affine native Locus V2, piecewise-polynomial SplineV2 and
supported R5 similarity images with complete evidence in their declared scope.
Its bounded generic-evaluator fallback remains deliberately conservative: zero
or one locally found preimage without complete scope is unresolved. G9U1 must
expose that limitation instead of promising arbitrary generic-locus creation.
It is not a missing frontend workaround and does not authorize another kernel
gate.

For a closed periodic SplineV2, R6 proves bidirectional seam movement from
canonical `u=0.98` to `u=0.02` with `periodicLift=1`, reverse movement, no
duplicate candidate and path-independent final state while preserving the same
point, durable ID, source, branch and component. The encoded canonical
IEEE-754 bits, lift and seam side remain exact authority; the frontend neither
reconstructs nor recanonicalizes them. A genuinely unresolved seam remains
unchanged and fails closed.

Invertible R5 transforms resolve in the transformed source context, including
rotation, reflection and negative dilation; transformed and source identities
remain distinct. At `k=0`, a new query returns
`DEGENERATE_SOURCE_IMAGE` rather than selecting an arbitrary retained address.
An existing addressed point keeps its semantic direction, evaluates at the
collapsed image when currently valid and recovers with the same identity when a
nonzero factor returns.

Native `.cedg` persistence, dormant selector hydration/reactivation, copy/remap,
rename and undo/redo are already R6 kernel authority. Requests, candidates,
world targets and UI ordinals are transient and never serialized. This actual
contract removes the last kernel prerequisite identified by the G9U1 gap audit;
stroke hit testing, chooser/feedback/accessibility, drag gesture grouping and
the first end-to-end GUI smoke remain frontend work in G9U1.

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

R4 intersection tokens and R6 semantic-preimage candidates remain distinct
authorities: the former select rich intersection solutions; the latter select
an address on one semantic source. Neither is converted into the other, and no
marker ordinal becomes Point-on-Locus identity.

The persistent inspector supports create-one, create-selected and create-all
for current eligible tokens. Each output gets its own exact token. Multi-create
is one visible coherent undo transaction. The inspector remains open for
repeated work, identifies existing outputs, supports keyboard operation and
uses compact token-independent labels. Cancel commits nothing.

Optional auto-materialization is an explicit frontend operation just after
rich-query creation, within the same user-confirmed compound undo transaction.
One Enter/tool confirmation therefore undoes/redoes the rich result and its
opted-in points together; a later explicit inspector materialization is a
separate atomic action. No asynchronous second undo store is scheduled for the
same confirmation. Kernel recompute, load and later root appearance never
create a new node. R4 automatic reactivation applies only to an already-existing
exact-token point.

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

- `geocedg.brand.topbar` for the application/window frame and deterministic
  Windows-package icon derivatives of `helixTopBar.png`; and
- `geocedg.brand.startup` for the startup/splash derivative of
  `helixSnapshot.png` only.

Each source and derivative has recorded provenance and deterministic hashes.
The byte-exact promoted sources live under the versioned Desktop resource tree;
the 64×64 frame PNG, 16/24/32/48/64/128/256 Windows ICO and current 361×480 splash are
deterministic contain/center derivatives with transparent padding and no crop or
distortion. The published 542×720 splash remains a byte-exact historical
derivative. The ignored author-ingestion directory is not a build dependency.
Missing declared resources fail validation; no substitute logo or upstream
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
| inverse semantic-address evidence | published R6 shared kernel | transient typed query/result/candidates; selected exact address only is persisted by the normal point parent | semantic query authority |
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
- R6 owns inverse-query budgets and counters. The initial policy bounds one
  query to 32,768 semantic evaluations, 512 subdivisions, 80 refinements and
  1,024 candidates, with evaluator composition depth capped at 128. Pointer
  motion may not hide unbounded work, repeat one global solve per candidate or
  retain movement history.
- One successful R6 move uses the accepted O(N) Construction snapshot/rollback
  seam. G9U1 must coalesce/group the user gesture and validate responsiveness;
  it must not bypass that correctness boundary with frontend-owned state.
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
- `NO_ADMISSIBLE_PREIMAGE`, `UNRESOLVED_NUMERICAL_SEARCH`, `INVALID_SOURCE`,
  `DEGENERATE_SOURCE_IMAGE` or `UNSUPPORTED_CAPABILITY`: no point and no free-
  point fallback. Diagnostic candidates under an unresolved status are not
  selectable.
- `MULTIPLE_SEMANTIC_PREIMAGES`: exact resolver candidates only; chooser cancel
  creates nothing, and selection passes the candidate object rather than an
  ordinal.
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
- `U1-PNT-01`–`U1-PNT-20`: the actual R6 query/status/candidate/create/move
  contract through real Point-tool click, exact ambiguity choice, semantic drag,
  periodic seam, transformed/k=0 states, persistence, undo/copy and view purity;
- `U1-I01`–`U1-I14`: markers, multi-materialization, persistent inspector,
  auto transaction and periodic-risk disposition;
- `U1-C`/`U1-L`: English/Spanish command/action/help parity;
- `U1-Z`: real `ZoomWindow` menu/toolbar/keyboard paths, rectangle/cancel/
  zero-area behavior, view purity and broader G12 exclusion;
- `U1-B`/`U1-A`: visual identity, accessibility and brand provenance; and
- `U1-P01`–`U1-P02`: overlay/selector performance and bounded token UI.

The future focused verifier runs after every required sealed G9U0/R1/R2/R3/R4/
R5/G9S1/R6 authority, relevant G9A/G9X1/G5/Classic controls, Checkstyle, Git
diff checks and full `tools/agent/verify.ps1`. UI tests exercise the real
Algebra, menu, keyboard and Graphics routes. The G9U1 author smoke is the first
productive GUI acceptance of R6: Point-tool create/drag on LocusV2 and SplineV2,
periodic seam crossing, self-intersection chooser/cancel, transformed and
negative-dilation sources, `k=0`, and save/reopen. No verifier may substitute
direct-controller invocation for that public path.

## Implementation slices and gates

1. **Published entry authority:** G9S1 and G9U0-R6 are sealed
   `PASS — AUTHOR APPROVED`; their kernel semantics are consumed, not reopened.
2. **G9U1A:** strict schema-v2 definitions, v1 adapter, one action registry,
   feature/localization/help/icon validation and professional group compiler.
3. **G9U1B:** Construction layout/controller/preferences/document-layout,
   product Continuity/locale policy, Algebra preview/inspection/description
   contracts and stroke-only curve hit presentation.
4. **G9U1C:** R6-consuming interactive semantic Point gesture/drag, current-token
   markers, persistent inspector and explicit one/selected/all materialization.
5. **G9U1D:** visual identity, author brand roles, accessibility, performance,
   packaging consumers where separately in scope and complete regression.

R6 and R1 PASS satisfy the kernel prerequisites found by the complete post-R1
gap audit. No material semantic novelty was found; the current author instruction
therefore authorizes implementation after this planning gate passes. G9B/G9C remain
semantically independent. G9U2 remains blocked by its global G9/procedure
authority. Broader G12 navigation is not folded into these slices.

## Open decisions and STOP boundaries

Author review is still required for:

- initial Properties floating/open state;
- final product palette/accent and supplied brand-asset suitability;
- final wording for the locked Continuity and inverse ambiguity states;
- whether unsupported product locales are hidden or displayed unavailable;
- final frontend mapping from stroke tolerance to R6 world-radius policy and
  drag-event/undo grouping within the accepted R6 work contract; and
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
G9U0-R6 = PASS — AUTHOR APPROVED
G9U1 DESIGN = PASS — AUTHOR APPROVED
POST-R6 RECONCILED = true
POST-R1 RECONCILED = true
G9U1 IMPLEMENTATION = PASS — AUTHOR APPROVED
implementationStarted = true
implementationAuthorized = true
implementationComplete = true
selfApproved = false
authorApprovedDesign = true
authorApprovedImplementation = true
passClaimedImplementation = true
manualAuthorSmoke = PASS
```


## Post-R1 reconciliation authority

The approved post-R6 checkpoint `00982e7e148a634cd57ed928f322774df267d5e3`
remains immutable. This successor consumes published G9S1-R1 without changing
R6 Point semantics or adding an action/family. The current conditional author
authorization is satisfied by the no-material-novelty audit. Implementation is
`PASS — AUTHOR APPROVED` by an explicit exact-SHA author decision; the
status-only closeout does not relabel technical execution as running on the
closeout commit and remains never self-approved. See the
[published R1 pair-consumer contract](../../geocedg/specs/ui/g9u1-construction-interaction.md#published-g9s1-r1-pair-consumer-contract)
and the 20 additive `U1-PAIR` rows (138 total). The R4 periodic risk remains
OPEN / TRACKED; an inconclusive native experiment without a real kernel or
persistence violation does not block this candidate under the author's explicit
disposition. No other workspace scope is expanded.

## Author-review stabilization, round 1

This successor remains an implementation candidate pending author re-smoke; it
does not rewrite the protected original candidate's executable evidence. The
round-1 live profile projected the unchanged 110 stable actions into six
ordinary menus (File, Edit, Construction, View, Automation, Help) and a reduced
34-action primary toolbar. Eleven professional families and eighteen clusters
remain catalog taxonomy, not eleven mandatory permanent toolbar buttons. Menus,
overflow and command help continue to consume the same registry.

`GeoCeDGUserToolLibrary` stores explicitly installed, validated user `.ggt`
packages in isolated application preferences. Installed entries and pins are
presentation/library state, not additional stable product actions. Definitions
enter the current document only on explicit activation through the existing
macro engine. Document-local tools never auto-install; the historical Template
archive and assets remain read-only provenance, not bundled product tools.

Public metric definitions display reconstructible `Length(L[,P,Q])` intent when
the scalar's rich parent supplies that exact operation. The real DAG remains
`rich metric -> scalar adapter`, including `Length(o)` XML where o is the rich
parent. Free interpolation points are not semantic endpoints; no proximity or
vertex-index admission is added.

The review also corrects two bounded lifecycle seams without changing geometry
or serialization schema: ordinary registry publication atomically refreshes
derived dependency records when existing inputs first participate, and native
Save preflights its temporary archive using the existing reader before replacing
the target. G9A staging/leases, strict load rejection and exact IDs remain; no
historical-file repair or migration is introduced. See the
[native lifecycle review](../validation/g9u1_native_lifecycle_review.md),
[frontend review matrix](../validation/g9u1_frontend_review_matrix.md),
[user-tool review](../validation/g9u1_user_tools_review.md),
[quick guide](../user/geocedg_construction_quick_guide.md) and
[author re-smoke checklist](../validation/g9u1_author_resmoke_checklist.md).

## Author-review stabilization, round 2

The second bounded candidate applies the author's presentation amendment while
retaining the same 110 stable actions. The sole declarative profile now projects
them into **File, Edit, View, Construction, Options, Automation, Help**. Options
contains only profile-declared, audited preference/property actions; it is not a
restored parallel upstream menu. Global menu projection rejects or removes
duplicate placements, so one stable ID always resolves to the same registry
action in menu, toolbar, context and help surfaces. Construction keeps
Continuity locked OFF and offers only EN/ES; diagnostic Classic retains its
upstream policy.

Installed user-tool commands remain outside the stable catalog. Their app-only
preference record may retain pin order and an optional presentation group;
several tools in one group render as a dropdown. Populating the toolbar never
registers the macro. Explicit selection activates the host Macro definition in
the current document, and only invocation creates construction results.

A native archive with both document macros and G9 spatial identities has two
parsing steps but one restore purpose: after the macro preamble clears the
construction, the following `geogebra.xml` remains a full native/undo restore,
not a generic identity merge. The reader communicates that existing one-shot
load purpose without relaxing `GENERIC_MERGE_FORBIDDEN`. Thus an invoked macro
is document-owned and reconstructible without the installed preference package.

The inspector is only a presenter over typed kernel data. For a semantic curve
it shows published branch/component structure; for a semantic point it shows
its durable address and whether a current address is admissible. It never parses
the displayed definition or infers an address from coordinates. Product version
and window/About identity derive from the packaging authority. In the Round-2
cohort no authorized GeoCeDG frame/splash asset existed, so
`AUTHOR_ASSET_MISSING` was reported rather than substituting an upstream
trademark asset.

## Author-review stabilization, round 3

Round 3 is a successor technical candidate, not approval or PASS. It retains the
taxonomy and 110 stable actions while moving all UI ordering into the profile's
single presentation projection. `GeoCeDGMenuBar` renders File/Edit direct action
runs and separators, semantic Construction groups, host View controls and host
Options controls. `GeoCeDGWorkspaceController` compiles the toolbar from the
same semantic presentation groups. Neither owns a fallback taxonomy. Input Help
remains outside the tool container at its existing right-hand layout position.

The bounded host bridge reads and writes existing presentation state only:
Graphics-view construction navigation, Algebra/Graphics 2/Spreadsheet/CAS/
Properties-view visibility, global Preferences, Algebra display/sort, rounding,
labeling, font size and Save Settings. It adds no document or preference model,
does not expose 3D in this candidate, and creates no Construction/undo state.
Continuity and locale policy remain owned by the existing product controls.

The real `Revision3.cedg` failure was an application-startup ownership mismatch,
not an R5 Dilate, G9A compatibility or spatial-instrumentation defect. GeoCeDG
previously created the Construction and its metric owner on the launcher thread,
then dispatched ordinary Algebra gestures on Swing EDT. The product-specific
three-argument `GeoGebra.doMain(...)` overload now constructs the
`GeoCeDGFrame`/`AppGeoCeDG` and runs `GeoGebraFrame.init(...)` synchronously on
Swing EDT; splash resolution and presentation still happen first, outside that
EDT initialization boundary. The original two-argument Classic launcher and
kernel thread-confinement rules remain unchanged. Both the byte-exact author
fixture and its clean deterministic fallback cover ROW, DOUBLE_CLICK, F2 and
FREE_INPUT. They retain the same `GeoNumeric` reference and durable ID across
`1 -> 0 -> 0.25 -> -1 -> 1`, with the existing atomic redefine/update, undo and
native persistence paths.

Installed user-tool preferences use a version-3 record. Raw `.ggt` SHA-256
remains package integrity/identity; each command additionally records a
version-1 normalized definition digest from host-parsed `Macro.getXML()`. Only
`showInToolBar` is normalized because it is presentation. A complete equal set
adopts the document's existing Macro objects for application presentation; it
never registers, renames or replaces them. Partial or mismatched sets fail
closed. The embedded macro therefore remains the self-contained `.cedg`
reconstruction authority even when the installed package is absent.

Optional pinned-tool icons are bounded app preferences rather than document
assets: PNG signature and `.png` basename, 256-KiB encoded maximum, decoded
dimensions 1..1024 per side and at most 1,048,576 pixels. Exact bytes/name/hash/
dimensions persist; a validated 64×64 centered, bicubic, aspect-fit ARGB image
with transparent padding is derived on load. Replacing the icon, unpinning or
removing the package deterministically removes the only inline reference. No
Macro icon name, external-image registry, `.cedg`, undo or Construction state
is touched.

The author-confirmed brand sources are now tracked byte-exact under
`source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/source/`.
Deterministic resources in `derived/` supply the application frame, startup
splash and Windows package icon. `GeoCeDGBrandingResource` resolves only these
registered roles. `GeoGebra` retains its original two-argument Classic launcher;
the narrow product-specific overload accepts the GeoCeDG splash supplier and
places only frame/application creation and initialization on Swing EDT after the
splash has been prepared. Public redistribution remains independently blocked
by the asset/licensing authority.

`GeoCeDGProductInfo` consumes the same build/package provenance rather than a
second version constant: semantic package version `0.9.0` is presented as
`GeoCeDG 0.9` in the frame title and About surface. About also identifies the
recorded GeoGebra baseline and `Manuel Prado-Velasco, Universidad de Sevilla`,
while retaining mandatory upstream credits and license notice.

## Final presentation-polish successor

The published Round-3 technical candidate is immutable at
`56cf32c922baefeb30c7dff02dbdd5091107ea1a`. Its bounded successor retains the
same registry and 110 IDs while extending the schema-v2 presentation projection
with `presentation_name_key` and `toolbar_rendering`. The former is a
product-only label override; the latter has only `native` and `profile-flyout`
values and is rejected if unknown. A profile flyout contains the same `Action`
instances as menu/help dispatch and never owns a command or mode.

Native toolbar grammar now produces 44 unique modes. Two mixed action groups are
rendered beside it: Semantic Curves (Locus V2, Spline V2, Point on semantic
curve) and Graphics-view navigation (Pan, ZoomWindow, Zoom In, Zoom Out, Copy
Visual Style). The point/intersection, Move, three linear and Parameters groups
are populated solely with their already-declared upstream modes. Exactly 52
stable actions are projected to the toolbar across native and mixed groups; no
action is duplicated and Input Help remains outside the tool container.

Startup branding uses a new deterministic 361×480 derivative without altering
the author source or the historical 542×720 derivative. Only the GeoCeDG startup
overload requests supported always-on-top foreground presentation; Classic
retains its inherited call order and policy. This layer owns presentation only:
kernel geometry, R6/R1 identity, G9A redefine, macros and `.cedg` persistence are
unchanged.

## Final micro-presentation successor

The functionally accepted technical checkpoint
`34ffdd9af5f94ded2765e7d495ee66543d4d751f` remains immutable. Its bounded
successor changes only the projection of the same schema-v2 action catalog. The
primary toolbar now follows the profile's exact eleven-group order: Move;
Point/intersection; Lines/vectors; Polygons; Derived constructions;
Circles/conics; Semantic curves; Metrics; Transformations;
Parameters/drivers; Navigation. Fixed Angle and Tangent move only in the
toolbar projection to Derived constructions; their Construction-menu taxonomy
does not change.

Native mode-only groups continue to use upstream `ModeToggleMenuD`. Semantic
Curves and Navigation mix mode and non-mode registry actions, so a bounded
Desktop adapter provides the same compact icon-and-arrow interaction and keeps
the most recently selected action visible. That last-used state is transient UI
state, not document state or geometric authority. Persistent user tools use
their validated custom PNG when present and otherwise render a deterministic
in-memory monogram in the normal toolbar footprint. Help and File ordering are
also profile projections: Help has the six approved help actions, while the
unchanged isolated Classic diagnostic action follows Open Recent in File.

# CeDG workspace contract

- Status: **NORMATIVE / AUTHOR APPROVED**
- Phase: G9P base design approved; definitive G9U1 reconciliation candidate
  pending author review; productive G9U1/G9U2 implementation is not authorized
- Normative manifest target: application profile schema version 2
- Related Accepted ADR: `docs/adr/0012-manifest-defined-geocedg-workspaces.md`
- Evidence: `docs/references/cedg/models/g9p/g9p-reference-workflow-audit.md`

## G9U0-R2 approved supersession record

The author closed both the planning/design and implementation of
`G9U0-R2 — PRE-G9U1 PRODUCT / DOCUMENT REFINEMENT` as
`PASS — AUTHOR APPROVED`. This record normatively refines the future G9U1
entry/document assumptions below; it does not authorize U1.

Before G9U1 execution, this contract must:

- require `G9U0-R2 IMPLEMENTATION PASS — AUTHOR APPROVED` as an additional U1
  entry gate, followed by a separate U1 authorization;
- read `serialization: <unchanged classic compatibility policy>` as retention
  of the current ZIP/XML machinery and `app_code: classic`, not as retention of
  `.ggb` as GeoCeDG's native extension;
- revise §9 so `.cedg` is the native document and a document-carried `.ggb`
  layout is compatibility input, while workspace preferences remain outside
  geometric/document semantics;
- revise §10 so GeoCeDG Classic directly opens and preserves supported `.cedg`
  without downgrade or creation enablement; accepting `.cedg` does not change
  its default new-document identity;
- revise §12/§13 and the U1 validation entry to consume the R2 evidence without
  changing workspace presentation purity or introducing a G9B dependency.

The owning contracts are the normative
`geocedg/specs/ui/native-document-identity.md`,
`geocedg/specs/locus/locus-v2-presentation.md` and Accepted ADR 0016. The
separately authorized R2 implementation is validated and approved: `.cedg` is
the native GeoCeDG document identity and `.ggb` is compatibility input under
the non-destructive transition policy.

ADR 0012 remains Accepted for its workspace/profile, presentation-purity and
separate-Classic decisions. ADR 0016 supersedes only its former `.ggb`
native-document assumptions and adds G9U0-R2 implementation PASS as a
prerequisite for any later, separately authorized G9U1 execution.

## G9U1 definitive pre-execution reconciliation candidate

The post-G9S1 reconciliation is a **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**.
It is not productive authority and does not change the checked-in version-1
profile, its reader, Desktop code, kernel code or user preferences. Its bounded
planning artifacts are:

- `application-profile-v2.candidate.schema.json` and
  `application-profile-v2.candidate.yml`, which model the proposed version-2
  manifest without replacing `apps/geocedg/application-profile.yml`;
- `g9u1-construction-interaction.md`, which assigns each interaction defect to
  its owning layer and records the proposed pre-U1 semantic-point gate;
- `docs/validation/g9u1_workspace_completeness_matrix.md` and
  `docs/validation/g9u1_command_tool_consistency_matrix.md`, which reconcile
  the complete action surface and the 98-scenario future validation authority;
- `.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md`, the
  definitive prospective execution prompt whose exact canonical-LF hash still
  requires author approval and a later implementation authorization.

The eleven professional families accepted below remain the broad workspace
taxonomy. The candidate manifest refines them into eighteen operational
clusters for discoverability and validation; this is a one-to-many presentation
mapping, not a replacement taxonomy and not a second action authority. Every
menu, toolbar, overflow and help surface must consume the same version-2 action
catalog after implementation is authorized.

One newly exposed kernel prerequisite remains deliberately outside G9U1:
interactive click-to-create `Point` on a semantic Locus V2 requires an approved
inverse semantic-address resolver. The proposed bounded gate is
`G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT`, with design and
implementation both **NOT AUTHORIZED**. It is a mandatory precondition for the
definitive G9U1 prompt unless the author approves an equivalent separately
numbered kernel gate. G9U1 may consume the existing exact typed
`Point(L, branch, u)` authority but may not infer a semantic address from render
samples, pixels or Cartesian proximity.

The candidate also freezes these prospective product rules: the existing host
`Continuity` setting is clamped OFF for GeoCeDG while Classic remains
configurable; `ZoomWindow` uses the existing rectangle-view seam while broader
view-history/fit/layer/scale work remains G12; Locus V2 hit testing uses stroked
curve geometry rather than filled path interiors; Algebra preview and
description-mode controls use their normal command/tree seams; rich markers
consume current deterministic tokens; Spline V2 × Spline V2 remains rich-only;
and `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` stays open until a real native
round trip or explicit author disposition closes it.

## 1. Purpose

This contract evolves the accepted version-1 GeoCeDG application profile from
one initial perspective and one toolbar into a manifest-defined set of named
workspaces. A workspace is a presentation and interaction arrangement. It is
not a geometric mode, a command filter, a construction state, or a source of
semantic truth.

The approved design contains:

1. **CeDG Construction** — the default professional 2D construction workspace;
2. **CeDG Dihedral Procedures** — a reduced, procedure-oriented workspace whose
   implementation remains blocked until `G9 global PASS — AUTHOR APPROVED`;
3. **GeoCeDG Classic (diagnostic)** — a visible route to the existing separate
   fork Classic process/path, not an in-process GeoCeDG workspace.

The author approved “workspace”, **CeDG Construction**, and **CeDG Dihedral
Procedures**. The localized diagnostic-route label may be finalized during
G9U1, but it must identify the GeoCeDG Classic path rather than promise external
upstream interoperability.

## 2. Invariants

A future implementation of this contract must preserve all of the following:

- Switching workspace does not create, delete, redefine, relabel, recompute, or
  reinterpret a `GeoElement`.
- Workspace selection does not alter command meaning or the kernel feature set.
  Commands hidden from a toolbar remain available through algebra input when
  their independent maturity/feature policy permits them.
- A workspace never provides metric, projection, spatial, visibility, or
  intersection authority.
- A loaded document toolbar or perspective is presentation evidence only. It
  cannot mutate the versioned product manifest.
- Runtime feature policy is independent of workspace selection. A workspace
  cannot enable an experimental command merely by showing its action.
- An unavailable action is absent or disabled with a localized reason according
  to its declared policy; a visible button and command dispatch cannot disagree.
- The Construction Protocol records geometric operations, not workspace
  switches. Workspace switching resets transient tool selection to Move and
  cancels an unfinished selection transaction without changing construction
  history.
- GeoCeDG Classic remains a separate process and preference namespace, but its
  supported persisted V2/rich/spatial objects use the same GeoCeDG kernel
  semantics and are never silently downgraded. External upstream GeoGebra is a
  separate, unsupported-open boundary for unknown GeoCeDG types.

## 3. Current source boundary

The accepted version-1 authority is
`apps/geocedg/application-profile.yml`, governed by
`geocedg/specs/ui/application-profile.md` and ADR 0001.
`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java:80-139`
currently reads one perspective, and the same file at `:142-166` compiles one
array of numeric mode IDs.
`source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java:42-46`
lets a loaded/saved perspective take precedence. This contract changes none of
those files; it defines a future, separately gated schema-v2 implementation.

## 4. Schema version 2 contract

Version 2 should retain the version-1 `profile_id`, `application`,
`serialization`, and top-level feature declarations. It should replace the
singular `perspective`/`toolbar` fields with one action catalog and a set of
workspaces:

```text
schema_version: 2
profile_id: geocedg-desktop
application: <unchanged v1 identity>
serialization: <unchanged classic compatibility policy>
default_workspace_id: cedg-construction

actions:
  - id
    kind: upstream-mode | command | product-action | view-toggle |
          result-inspector | diagnostic-route
    target
    maturity
    feature_requirements[]
    selection_contract
    localization
    icon
    unavailable_policy

workspaces:
  - id
    name_key
    description_key
    maturity
    availability
    perspective
    toolbar.groups[].action_ids[]
    menus[].action_ids[]
    help_policy
    document_layout_policy

workspace_persistence:
  scope: profile-preferences
  active_workspace_key
  per_workspace_layout_key_prefix
  unavailable_fallback

diagnostic_routes:
  - id
    action_id
    process_profile
    preference_isolation

features: <versioned feature IDs>
```

### 4.1 Action catalog

An action is declared once. Toolbars, menus, context actions, result inspectors,
and command help reference its stable action ID. An action declaration must
contain:

- stable ID unrelated to display text;
- action kind and symbolic target;
- maturity and required runtime flags;
- typed selection contract;
- localized name, short help, long help, status and error keys;
- GeoCeDG-owned icon reference or an explicit text-only fallback;
- availability policy: `hidden`, `disabled-with-reason`, or
  `diagnostic-route-only`;
- output/procedure kind and whether the action starts a kernel construction;
- compatibility behavior in GeoCeDG, the fork's Classic launcher, and an
  unsupported upstream installation.

Upstream mode actions may record the audited numeric ID, but workspace groups
must reference the stable action ID rather than repeat a raw toolbar string.
Future GeoCeDG actions must not reserve a numeric mode ID in this design phase.

### 4.2 Workspace declaration

Each workspace declares only presentation state:

- ordered visible views and dock relationships;
- default visibility, not forced window dimensions;
- toolbar groups referencing action IDs;
- menu placements referencing the same actions;
- input location and contextual-help policy;
- availability gate and localized blocked reason;
- handling of a document-carried layout;
- user-customizable state fields.

No command processor, algorithm, object type, tolerance, or geometric policy is
defined in a workspace manifest.

### 4.3 Version migration

The future reader should support version 1 and version 2 during one compatibility
period. A deterministic in-memory migration of version 1 should:

1. create a `cedg-construction` workspace;
2. lift the existing perspective unchanged;
3. create one upstream-mode action for every existing mode ID;
4. preserve category and action order;
5. retain planned categories as blocked group metadata;
6. retain `classic` as the serialization app code;
7. leave the checked-in version-1 manifest unchanged until the G9U1 prompt is
   authorized and its schema/adapter tests pass.

The migration must not write user files or `.ggb` files.

## 5. CeDG Construction workspace

### 5.1 Panel arrangement

The default panel set should be:

- primary 2D Graphics: visible and dominant;
- Algebra: visible;
- Construction Protocol: visible, with step navigation;
- Input bar: visible at the bottom;
- contextual command/tool help: visible;
- Properties: directly accessible and allowed to float; initial floating/open
  state is an author choice;
- Graphics 2, Spreadsheet, CAS and 3D: available but closed.

Exact divider positions are user state. The reference files demonstrate both
two-panel and three-panel arrangements, and the screenshot demonstrates a
floating Properties workflow; none supplies a universal pixel layout.

### 5.2 Toolbar/action groups

The audited upstream IDs below are from
`source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianConstants.java:28-169`.
Symbolic action IDs remain the manifest authority.

| Proposed group | Primary actions | Current mapping | Maturity/gate |
|---|---|---|---|
| Inspect and construct | Move; point; point on object; Algebra/Protocol/Properties toggles; undo/redo; object inspection | modes `0`, `1`, `501` plus product view actions | stable organization in G9U1 |
| Linear geometry | line, segment, ray, vector, parallel, perpendicular, midpoint | modes `2`, `15`, `18`, `7`, `3`, `4`, `19` | inherited stable |
| Parameters and drivers | explicit scalar domains/maps and true-driver roles; numeric parameters; point generators on segment/circle/arc; orientation, periodicity and animation controls | Algebra/product actions plus mode `501`; typed V2 generator metadata is kernel semantics, not toolbar state | inherited parameters; semantic generators require G9U0 |
| Relations and intersections | general Intersect, tangent, relation; rich-result inspector; separate exact-token point materialization | modes `5`, `13`, `14`; V2 actions after G9U0 | V2 actions require `cedg.locus.v2` |
| Circles, conics and curves | circle families, ellipse/conic, tangent, approved axis helpers; function/curve entry | modes `10`, `11`, `12`, `55` and inherited commands | axis helpers require separate characterization |
| Locus V2 | create semantic locus from typed scalar/point generator; create supported point on Locus V2; total/partial rich length; inspect semantic branches/components/preimages | future symbolic actions; legacy mode `47` and generic `Path` are not reused | G9U0 PASS; experimental flag until promotion |
| Metrics and validation | distance, angle, area, authoritative rich Locus length plus guarded standard scalar, semantic status/certificates | modes `38`, `36`, `49`; command/result actions | inherited plus G9U0 result inspectors/adapters |
| Transformations and manual projections | reflect in line, translate, rotate; ordinary manual auxiliary construction | modes `30`, `31`, `32` | inherited; no spatial claim |
| CeDG procedures and developments | projection change, fold, true magnitude, section, development procedure | no productive target yet | disabled until approved G9 procedure phase |
| Presentation and document | style/visibility via Properties, layers when available, Protocol controls, sheet setup | view/product actions | presentation only; no geometric visibility inference |
| Automation and import/export | algebra input, user tools, Laboratory route, DXF and future sheet exports | product/menu actions | independently feature-gated |

These eleven groups remain the professional Construction design; R1 adds
actions within them and does not collapse the workspace into a minimal toolbar.
The main toolbar should expose groups, not one permanent button per action.
Actions the author explicitly does not need as direct buttons—such as Delete,
Show/Hide, Copy Visual Style, image/freehand tools, rigid/vector polygon and
several circle-arc variants—remain available through menus, context actions,
overflow groups, shortcuts, or algebra input.

Legacy Locus mode `47` remains available only through a clearly labelled
compatibility placement or command input. It is not the implementation of the
Locus V2 group.

The supported point-on-Locus action is distinct from the rich-intersection token
point action. The former selects/persists a semantic source
branch/component/preimage and may act as the generator of another Locus V2; the
latter consumes one exact solution token from a rich intersection result.
Neither action persists click proximity or a render/sample vertex.

### 5.3 Command availability

Workspace membership does not filter commands. The following are separate:

```text
command exists in the fork
  != command is enabled by runtime feature policy
  != action is visible in this workspace
  != object can be loaded for compatibility
```

This distinction is required for default-off experimental Locus V2 and for
Classic compatibility. Loading an already persistent supported object must not
depend on whether its creation button is visible.

G9U1 is a GUI client of already approved command/application services. It does
not implement generator, metric, intersection, projection or spatial semantics.
It has no hard dependency on G9B, and G9B must not acquire a reverse dependency
on workspace completion merely because G9U1 is recommended earlier
operationally.

## 6. CeDG Dihedral Procedures workspace

This workspace is designed now but must not be implemented or enabled until G9
spatial semantics are author-approved and their global gate passes. A narrower
post-G9A pilot requires a separate explicit decision.

### 6.1 Minimum semantic inputs

The workspace requires production equivalents of:

- durable `SpatialObject3D` identity and revision;
- a durable `ProjectionSystem`-equivalent aggregate containing multiple
  `ProjectionFrame` identities and geometry;
- relative geometric relations between frames, intrinsic coordinates for each
  frame, and the explicit map from intrinsic projection coordinates to the
  common CeDG diagram;
- line-of-ground/hinge semantics where applicable, orientation, defining versus
  auxiliary frame roles, change-of-plane lineage, current revision,
  consistency and degeneration;
- typed `ProjectionBinding` with defining/derived role;
- sufficiency/reconstruction/reprojection result;
- explicit validity, ambiguity and degeneration states;
- copy, deletion, undo/redo and persistence contracts.

It must never infer an object, binding, frame relation or hinge from labels,
layer, selection order, screen proximity, visible 2D placement or coincidence.
A workspace may display/choose these kernel-owned semantics but does not own
them.

### 6.2 Reduced toolbar groups

| Group | Actions | Selection and output contract |
|---|---|---|
| Inspect spatial semantics | select spatial object; inspect frames, bindings, certificate and provenance | one typed spatial object or one binding; no geometry created |
| Projection systems, frames and bindings | choose/inspect approved system and auxiliary frame; inspect intrinsic coordinates, diagram map and defining/derived roles | explicit system/frame/binding IDs; ambiguity dialog when several qualify |
| Change projection plane | select object/plane, persisted frame relation or reference line and destination frame | consume ProjectionSystem map/change-of-plane semantics; explicit construction steps and derived bindings |
| Rotation and folding | rotation; fold/abatimiento; true magnitude | select typed object/plane and a system-admissible line-of-ground/hinge/destination; no hinge inferred from visible placement |
| Auxiliary plane and section | projecting plane; section; generator/support construction | outputs remain normal dependency-graph objects |
| Reconstruction and projection | reconstruct, derive projection, reproject and compare | certificate remains visible; invalid states produce no stale spatial result |
| Procedure results | inspect generated auxiliaries, ownership, validity and handoff to Construction | no opaque feature; all steps available in Protocol |
| Workspace/navigation | return to Construction; Algebra, Protocol and input toggles | no construction mutation |

All ordinary algebra commands remain available. The reduced toolbar changes
discoverability only.

### 6.3 Procedure interaction

A procedure such as folding a plane should follow this explicit flow:

```text
select SpatialPlane
 -> resolve durable ProjectionSystem and typed defining bindings
 -> inspect intrinsic source/destination frames and diagram embedding
 -> resolve or explicitly select a declared line-of-ground/hinge relation
 -> select destination ProjectionFrame in that system
 -> preview validity and ambiguity
 -> construct explicit auxiliary entities
 -> publish true-magnitude/derived projection and certificate
 -> retain dependencies, generated-object ownership and provenance
```

Defaults are allowed only when a unique admissible choice is established by the
ProjectionSystem/binding semantic model. Otherwise the UI presents typed
alternatives. Screen proximity may rank choices for the dialog but cannot
establish a frame relation, diagram map, hinge or identity.

One user confirmation may be one undo transaction, while the Construction
Protocol must still expose the explicit generated steps and their ownership.
Deleting a procedure invalidates or removes its owned auxiliaries according to
the approved lifecycle; it must not leave stale results.

## 7. Selection contract

Every constructive action must declare an ordered selection grammar:

- slot ID and localized role;
- accepted semantic/object families;
- required and maximum cardinality;
- whether selection order is meaningful;
- admissibility and degeneration checks;
- preview and cancellation behavior;
- ambiguity policy;
- output kind and Construction Protocol effect.

The status area displays the next required role, not merely a generic tool
name. Rejected selection produces a localized reason and leaves earlier valid
slots intact unless the action declares atomic reset.

For supported point-on-Locus V2 creation, slots identify source locus,
branch/component and semantic preimage (including periodic seam choice). The
created ordinary point carries the durable preimage address plus a current
revision/continuation binding and can be selected as an outer-Locus generator.
Ambiguous continuation disables downstream creation rather than choosing the
nearest displayed coordinate.

For rich Locus intersections, candidate markers come only from established
result solutions. Graphical proximity may preselect among currently admissible
tokens. The created point stores the exact selected token; proximity and list
order are never persisted.

Changing workspace cancels the active selection transaction and activates Move.
It does not delete already constructed outputs or alter the current construction
step.

## 8. Help, icons and localization

Action declarations must use keys, not user-visible literals:

```text
name_key
short_help_key
long_help_key
status_key
invalid_selection_key
blocked_reason_key
icon_id
```

GeoCeDG-owned resources must supply at least the author-approved initial
languages; a missing translation follows an explicit fallback chain and is a
validation warning. Dynamic status messages use typed placeholders and are
tested for every selection slot and result state.

Icons are resolved from `geocedg/resources/` and registered in the asset
manifest with rights evidence. Embedded Templatev7 or upstream UI images are
workflow evidence only and must not be copied into the product without the
license/asset gate. Text-only fallback must keep every action usable, and icons
require accessible names.

## 9. Persistence and document layout

The active workspace and per-workspace dock customizations are application
preferences under the existing GeoCeDG preference namespace. A proposed key
shape is:

```text
geocedg.workspace.v2.active
geocedg.workspace.v2.layout.<workspace-id>
```

The exact key names are implementation details, but the following policy is
part of this proposal:

- Workspace state is not geometric truth and is not a dependency input.
- G9U1 introduces no new `.ggb` semantic format merely to remember a workspace.
- A document-carried upstream perspective remains readable. On load, the UI
  identifies it as **Document layout** without registering it as a product
  workspace or rewriting the manifest.
- The user may reapply the current product workspace without changing the
  document construction.
- An unknown/unavailable saved workspace falls back to CeDG Construction with
  a localized diagnostic; it is not silently mapped to another semantic mode.
- User duplication of a workspace copies presentation preferences, not action
  declarations or feature policy.

## 10. Classic and Laboratory boundaries

The workspace menu must expose an action named by localization key for the
**GeoCeDG Classic diagnostic** route. It launches the existing fork Classic
task/process with a separate settings file. It does not switch the current
`AppGeoCeDG` instance to `AppConfigDefault`, share preferences, or change the
serialized `classic` app code policy. Supported GeoCeDG V2/rich/spatial objects
remain native, retain semantic IDs/tokens/bindings, and recompute/save/reopen
through the same shared kernel; no presentation path may downgrade them to
legacy locus, coordinates, or lists. Opening such files in an external upstream
distribution that lacks the persisted types is not guaranteed and must be
documented as unsupported rather than hidden by lossy conversion.

The CeDG Laboratory remains an explicit, hash-validated legacy/research loader.
Opening Templatev7 may show its document toolbar. That toolbar remains document
context and never becomes a schema-v2 workspace or stable action catalog.

## 11. Failure behavior

The future workspace controller must fail closed for:

- duplicate workspace/action IDs;
- unknown action references;
- duplicate upstream modes in one emitted toolbar;
- missing localization or icon declarations beyond the permitted fallback;
- cyclic feature dependencies;
- a workspace enabled while its required semantic feature is unavailable;
- a saved workspace whose schema version is unsupported;
- an action whose selection and command contracts disagree.

Manifest failure at startup falls back to the last accepted version-1
Construction profile with an explicit diagnostic; it must not fall back to an
unvalidated hard-coded toolbar.

## 12. Implementation staging and gates

1. **G9U1A — schema/compiler foundation:** author-approved version-2 schema,
   action catalog, runtime feature service, deterministic v1 migration, static
   localization/icon validation, no new geometric action.
2. **G9U1B — CeDG Construction:** workspace controller, panel defaults, curated
   groups, view/menu actions, document-layout state, public G9U0 actions only if
   G9U0 has passed. This slice is a GUI client and has no G9B dependency.
3. **G9U2 — Dihedral Procedures:** remains blocked until G9 global PASS and a
   dedicated prompt/specification authorize procedures consuming the approved
   ProjectionSystem/frame-map/hinge and binding semantics.

Future implementation evidence is defined in
`docs/validation/g9_public_workspace_validation_matrix.md`. Approval of this
contract does not authorize G9U1 or G9U2.

## 13. Approved closeout decisions

| Decision | Recommendation | Alternative | Gate impact |
|---|---|---|---|
| User-facing term | Workspace | Perspective | approved; localization keys implement it |
| Default name | CeDG Construction | Construction | approved; schema-v2/action-help review remains |
| Properties default | directly accessible; author chooses initially floating/open | closed | visual workflow validation |
| Protocol default | visible | closed but one-click | reference workflow acceptance |
| Disabled experimental actions | show group only when flag enabled; otherwise localized disabled entry in workspace menu | hide completely | feature-policy UX tests |
| Legacy Locus placement | compatibility overflow/input only | visible beside V2 | old-file usability test |
| Document-carried layout | transient Document layout with reapply action | force workspace immediately | open/reference-model tests |
| Classic path | separate GeoCeDG diagnostic process/path with native semantic preservation; external upstream open unsupported | in-process profile switch or lossy downgrade | preference/compatibility corpus required |
| Dihedral gate | after G9 global PASS | narrower post-G9A pilot | approved global gate; separate G9U2 author approval required |

# CeDG workspace contract

- Status: **NORMATIVE / AUTHOR APPROVED**
- Phase: G9P base design approved; definitive G9U1 design
  `PASS — AUTHOR APPROVED`, post-R1 reconciled; G9U1 implementation is an
  authorized candidate pending author review. G9U2 remains blocked.
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

## G9U1 definitive post-R6 approved pre-execution design

The post-G9S1/post-G9U0-R6 reconciliation is **G9U1 DESIGN PASS — AUTHOR
APPROVED**.
It is approved planning authority, not productive authority, and does not change the checked-in version-1
profile, its reader, Desktop code, kernel code or user preferences. Its bounded
planning artifacts are:

- `application-profile-v2.candidate.schema.json` and
  `application-profile-v2.candidate.yml`, which model the proposed version-2
  manifest without replacing `apps/geocedg/application-profile.yml`;
- `g9u1-construction-interaction.md`, which assigns each interaction defect to
  its owning layer and consumes the published R6 semantic-point authority;
- `docs/validation/g9u1_workspace_completeness_matrix.md` and
  `docs/validation/g9u1_command_tool_consistency_matrix.md`, which reconcile
  the complete 110-action surface, 11 professional families, 18 operational
  clusters and the 138-scenario future validation authority;
- `.github/prompts/tasks/g9u1-construction-workspace-after-g9s1-r1.prompt.md`, the
  definitive successor execution prompt whose canonical-LF hash is frozen before
  the conditionally authorized implementation begins.

The eleven professional families accepted below remain the broad workspace
taxonomy. The candidate manifest refines them into eighteen operational
clusters for discoverability and validation; this is a one-to-many presentation
mapping, not a replacement taxonomy and not a second action authority. Every
menu, toolbar, overflow and help surface must consume the same version-2 action
catalog after implementation is authorized.

The kernel prerequisite discovered during the protected pre-R6 review is now
closed: `G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT` is
`PASS — AUTHOR APPROVED` at `geocedg-g9u0-r6-pass` ->
`3942af594e4507e479f2c75019cef62e3d9fea6f`. The immutable historical planning
checkpoint is `857de6628489bda0b65a5ba5145e62ca0795fc32`.

G9U1 consumes `LocusPointInteractionQuery2D`,
`LocusPointInteractionResolver2D`, `LocusPointInteractionResult2D` and the R6
public create/move operations. The exact command
`Point(L, branch, u)` remains the scripting route for a known semantic address;
it is not the mouse inverse-resolution path and does not create an
interaction-owned R6 point. No render sample, pixel or Cartesian proximity may
become the semantic address. Published R6 satisfies the last shared-kernel
prerequisite found by the complete Construction-workspace audit; no further
kernel gate blocks the accepted G9U1 workflow.

## G9U1 author-review stabilization round 3

The published Round-3 schema-v2 implementation at
`56cf32c922baefeb30c7dff02dbdd5091107ea1a` remains historical technical
authority, not phase PASS. Round 3 preserves the approved 11 professional families, 18
operational clusters and 110 stable action IDs, and adds an explicit
presentation projection inside the same profile authority. It does not add a
second menu or toolbar catalog. That published profile declared 28 ordered
presentation groups and selected 12 of them for the primary toolbar; every
stable action remains reachable exactly once through the declared menu
projection.

The seven top-level menus are, in order, **File, Edit, View, Construction,
Options, Automation, Help**. File and Edit render final actions directly with
profile-declared separators. Construction uses the final semantic groups,
including **Lines and vectors**, **Polygons**, **Derived constructions**,
**Circles and conics**, **Semantic curves**, and **Annotations and media**.
Text and Image therefore remain construction actions and no longer appear under
View. The same semantic group IDs and ordering compile the primary toolbar.

View and Options consume existing host state through bounded adapters. View
exposes compatible view toggles and the view-specific **Show construction
navigation bar** action. Options groups the existing Algebra display modes as
one radio group and reuses host Sort by, Rounding, Labeling, Font size and Save
Settings authorities. Its preferences route is global and never selects an
arbitrary construction object. These controls create no construction or undo
state; Continuity remains locked OFF and product language remains EN/ES.

Round 3 also promotes the author-supplied frame and splash sources into tracked
GeoCeDG resources with deterministic derivatives and recorded provenance.
Installed user tools retain application-owned order/group/pin state and may
carry an optional bounded PNG icon in version-3 preferences. Their embedded
document macro remains the portable reconstruction authority. A normalized
definition digest may recognize a provably equivalent installed definition for
presentation ownership without deleting or replacing the embedded macro;
non-equivalence fails closed.

The candidate also freezes these prospective product rules: the existing host
`Continuity` setting is clamped OFF for GeoCeDG while Classic remains
configurable; `ZoomWindow` uses the existing rectangle-view seam while broader
view-history/fit/layer/scale work remains G12; Locus V2 hit testing uses stroked
curve geometry rather than filled path interiors; Algebra preview and
description-mode controls use their normal command/tree seams; rich markers
consume current deterministic tokens, including individually R1-certified
Spline V2 × Spline V2 singleton-germ slots; uncertified/ambiguous roots stay rich-only;
and `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` stays open until a real native
round trip or explicit author disposition closes it.

## G9U1 final presentation-polish successor

The bounded successor is **PASS — AUTHOR APPROVED** by an explicit exact-SHA
author decision; its status-only closeout changes no workspace semantics. It preserves the same 11 families, 18 operational clusters, 110 stable
actions, 28 presentation groups and seven menus, while selecting 11 toolbar
groups. The apparent second Locus operation did not exist: the sole inherited
Locus V2 mode had an asymmetric host label. GeoCeDG now presents the two creation
actions symmetrically as **Locus V2** and **Spline V2**, without changing the
Classic label or duplicating an action.

The toolbar projection contains three separate native flyouts for **Lines and
vectors**, **Polygons** and **Derived constructions**; a complete interactive
**Parameters and drivers** flyout; Move plus Move/Rotate around Point; and a
point/intersection flyout containing Point, Point on Object, Attach/Detach,
Intersect and Tangent. Two mixed profile flyouts use the same registry action
objects: **Semantic Curves** contains Locus V2, Spline V2 and Point on semantic
curve, while **View navigation** contains Pan, ZoomWindow, Zoom In, Zoom Out and
Copy Visual Style. The semantic inspector, parameter Animation toggle, Standard
View and Show All Objects remain menu/context operations because they are not
interactive creation/navigation tools. Input Help remains at the host toolbar's
far right.

The startup role now selects a deterministic `361 x 480` derivative and requests
foreground presentation only through the GeoCeDG overload. The author source and
the published Round-3 `542 x 720` derivative remain byte-exact historical
resources. No kernel, construction persistence, macro lifecycle, Continuity or
Classic contract changes.

## G9U1 final micro-presentation successor

The functionally accepted checkpoint
`34ffdd9af5f94ded2765e7d495ee66543d4d751f` remains immutable. Its successor
keeps the same 110 actions and projects the exact requested eleven-group toolbar
order from `application-profile.yml`. Fixed Angle and Tangent move only in the
toolbar to **Derived constructions**; menu Construction continues to present
them under their existing semantic groups. The point/intersection toolbar group
therefore contains Point, Point on Object, Attach/Detach and Intersect.

**Semantic Curves** and **Navigation** are compact normal-size flyouts whose
main icon follows the last selected registered action. Their state is transient
frontend presentation and is neither persisted in the document nor used for
identity. Iconless installed user tools likewise use an in-memory monogram;
validated custom PNGs retain precedence and the application-owned pin/group
preferences remain unchanged. File owns the isolated Classic diagnostic route
immediately after Open Recent. Help contains, in order, Input Help, Current Tool
Help, Command List, GeoCeDG User Guide, Keyboard Shortcuts and About GeoCeDG.

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

The live candidate authority is `apps/geocedg/application-profile.yml`, governed
by `geocedg/specs/ui/application-profile.schema.json`, this contract and ADR
0012. `GeoCeDGProfile` loads and validates schema version 2, compiles its action,
workspace and presentation references, and retains a deterministic in-memory
version-1 compatibility adapter. `GeoCeDGMenuBar` and
`GeoCeDGWorkspaceController` consume the compiled profile; they do not own a
parallel list of product actions or semantic groups. A loaded document layout
remains presentation evidence and does not mutate the profile.

## 4. Schema version 2 contract

Version 2 retains the version-1 `profile_id`, `application`, `serialization`,
and top-level feature declarations. It replaces the singular
`perspective`/`toolbar` fields with one action catalog, a taxonomy, an ordered
presentation projection and a set of workspaces:

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

taxonomy:
  broad_families[]
  operational_clusters[]

presentation_groups:
  - id
    name_key
    action_ids[]

toolbar_group_ids[]

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

menu_sections:
  - id
    entries[]: group | actions | separator | approved host-control

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
defined in a workspace manifest. The taxonomy is traceability/classification;
`presentation_groups`, `toolbar_group_ids` and `menu_sections` are its one
ordered UI projection. Both layers reference the same stable action catalog.

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
| Linear geometry | line, segment, ray, vector, polygon families, parallel, perpendicular, midpoint | modes `2`, `15`, `18`, `7`, `3`, `4`, `19` and inherited polygon modes | inherited stable; presented as Lines and vectors, Polygons, and Derived constructions |
| Parameters and drivers | explicit scalar domains/maps and true-driver roles; numeric parameters; point generators on segment/circle/arc; orientation, periodicity and animation controls | Algebra/product actions plus mode `501`; typed V2 generator metadata is kernel semantics, not toolbar state | inherited parameters; semantic generators require G9U0 |
| Relations and intersections | general Intersect, tangent, relation; rich-result inspector; separate exact-token point materialization | modes `5`, `13`, `14`; V2 actions after G9U0 | V2 actions require `cedg.locus.v2` |
| Circles and conics | circle, arc, sector, ellipse, parabola, hyperbola and conic families | modes `10`, `11`, `12`, `55` and inherited commands | inherited stable; semantic curves are not placed here |
| Semantic curves | create Locus V2 or Spline V2; create an explicit-address semantic point; inspect semantic branches/components/preimages | symbolic actions; legacy mode `47` and generic `Path` are not reused | G9U0/G9S1/R6 authority; experimental flag until promotion |
| Metrics and validation | distance, angle, area, authoritative rich Locus length plus guarded standard scalar, semantic status/certificates | modes `38`, `36`, `49`; command/result actions | inherited plus G9U0 result inspectors/adapters |
| Transformations and manual projections | reflect in line, translate, rotate; ordinary manual auxiliary construction | modes `30`, `31`, `32` | inherited; no spatial claim |
| CeDG procedures and developments | projection change, fold, true magnitude, section, development procedure | no productive target yet | disabled until approved G9 procedure phase |
| Presentation and document | style/visibility via Properties, layers when available, Protocol controls, sheet setup | view/product actions | presentation only; no geometric visibility inference |
| Automation and import/export | algebra input, user tools, Laboratory route, DXF and future sheet exports | product/menu actions | independently feature-gated |

These eleven groups remain the professional Construction design; R1 is consumed
through existing actions and does not change the 110 stable action IDs.
The main toolbar should expose groups, not one permanent button per action.
Actions the author explicitly does not need as direct buttons—such as Delete,
Show/Hide, Copy Visual Style, image/freehand tools, rigid/vector polygon and
several circle-arc variants—remain available through menus, context actions,
overflow groups, shortcuts, or algebra input.

The round-2 author-review amendment projects the live catalog into seven ordinary
menus, in this order: File, Edit, View, Construction, Options, Automation and
Help. The 34-action primary toolbar and the 11-family/18-cluster taxonomy do not
require every catalog action to be a permanent button. Options is a declarative
projection of existing action IDs for safe host settings; it is not a second
settings registry. An action referenced by more than one cluster is rendered at
most once in the menu bar. Every reduced placement still resolves through the
one profile/action registry; menu rebuild, EN/ES localization and overflow must
preserve discoverability and availability. These seven menus are presentation
sections, not new workspace families or separate command authority.

Round 3 makes that projection explicit in schema-v2. File and Edit are flattened
with separators; View owns host view visibility, presentation/navigation and
workspace switching; Construction owns object-producing actions; Options owns
audited host/product preferences; Automation owns input/scripting/Laboratory and
dynamic user tools; Help owns discovery and the single Classic diagnostic route.
The semantic-curve presentation group generalizes the existing explicit-address
point label to **Point on semantic curve** without changing its action ID or R6
semantics. The ordinary graphical Point tool remains the inverse-resolution
workflow.

Persistent user tools reuse `automation.manage-user-tools`. Explicit installation,
removal, pinning, pin order and pin grouping affect isolated GeoCeDG application
preferences only; a group with several tools is rendered as a normal toolbar
dropdown and does not mint stable product-action IDs. An optional PNG icon is
also application preference: source bytes/name, SHA-256 and decoded dimensions
are recorded; inputs are limited to 256 KiB, maximum edge 1024 and at most
1024×1024 decoded pixels; the toolbar uses a deterministic aspect-preserving
64×64 transparent-padded ARGB derivative. Changing, unpinning or removing a
tool removes unreferenced application icon data. Neither icons nor layout enter
`.cedg` or register a macro. An iconless pin uses a compact square initial while
retaining the complete localized tool name in tooltip and accessibility
metadata; long command labels therefore do not resize the toolbar. Activation uses the
existing macro engine and then ordinary document-local definitions. The native
document archive must embed the macro definition required by each `AlgoMacro`;
reopen must never depend on the application library still being installed.
Installing a package does not alter a blank document, and opening a document with
macros does not install them into the application library.

When the embedded definition and an installed package have the same normalized,
versioned command-definition digest, reopen adopts only user-facing presentation
ownership for the installed entry and suppresses a duplicate local-tool choice.
The embedded macro remains the reconstruction/fallback authority. Normalization
may ignore only the non-semantic `showInToolBar` presentation flag; command name,
construction, inputs, outputs, coordinates, order and references remain part of
the definition evidence. Missing installed packages preserve portable document
reconstruction; mismatched or partial packages report a collision and fail
closed.
No Templatev7 tool or asset is automatically imported, bundled or promoted.
See the [user-tool review](../../../docs/validation/g9u1_user_tools_review.md).

Legacy Locus mode `47` remains available only through a clearly labelled
compatibility placement or command input. It is not the implementation of the
Locus V2 group.

The supported point-on-Locus action is distinct from the rich-intersection token
point action. The former consumes an exact R6 semantic-preimage candidate and
persists its source branch/component/address; it may act as the generator of
another Locus V2. The latter consumes one exact R4 solution token from a rich
intersection result. R6 candidates and R4 tokens are not interchangeable.
Neither action persists click proximity or a render/sample vertex.

### 5.3 View and Options projection

The View menu reuses host visibility state for Algebra, Graphics 2,
Spreadsheet, CAS and the Properties view. It does not expose 3D in
this candidate and does not imply spatial semantic authority. The Construction
Protocol and the Graphics-view construction navigation bar remain distinct: the
latter is toggled through its view-specific host seam, reports its real checked
state, and creates no Construction or undo mutation.

The Options menu contains one **Algebra display** radio group for Value,
Description and Definition, plus host-owned **Sort by**, **Rounding**,
**Labeling**, **Font size** and **Save Settings** controls. Menu rebuild and
language refresh read the current host state. **Preferences…** opens the global
settings surface without selecting a construction object; object-specific
Properties remains an explicit-selection/context action. GeoCeDG owns no second
copy of these settings.

### 5.4 Command availability

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

For supported point-on-Locus V2/Spline V2 creation, the frontend first uses the
stroked curve presentation to identify a candidate semantic source; clicking
merely inside a closed curve does not select it. The finite world target then
enters the published R6 pipeline:

```text
LocusPointInteractionQuery2D
  -> LocusPointInteractionResolver2D.resolve(...)
  -> LocusPointInteractionResult2D
```

`UNIQUE_ADMISSIBLE_PREIMAGE` commits the exact resolver-produced candidate by
`LocusV2PublicOperations.createInteractiveSemanticPoint(...)`.
`MULTIPLE_SEMANTIC_PREIMAGES` opens a deterministic chooser; the user-selected
candidate object is passed unchanged, and cancel creates nothing. The other
typed R6 statuses create no point and produce localized truthful feedback. In
particular, diagnostic candidates from `UNRESOLVED_NUMERICAL_SEARCH` are not
selectable. No semantic-curve hit falls back silently to a free point.

Dragging an existing interaction-owned point calls
`LocusV2PublicOperations.moveInteractiveSemanticPoint(...)`. A unique result
updates the same point/durable ID/source/address; any nonunique or unresolved
result leaves it unchanged rather than retargeting by proximity. The frontend
owns coherent undo grouping and must reacquire object references after a kernel
rollback. Closed-Spline seam crossing must preserve the same point with the
canonical wrapped parameter/lift and exact stored semantic-direction bits.

Supported R5 transforms, including negative dilation, use the transformed
semantic source. At `k=0`, a new query reports `DEGENERATE_SOURCE_IMAGE` and
creates no arbitrary point; an existing addressed point remains the same point
at the collapsed image and recovers when the factor becomes nonzero. Native
save/reopen, undo/redo and copy/remap preserve the selected exact semantic
address; transient queries and candidates are not serialized.

For rich Locus intersections, candidate markers come only from established
result solutions. Graphical proximity may preselect among currently admissible
tokens. The created point stores the exact selected token; proximity and list
order are never persisted.

Spline V2 × Spline V2 uses the published R1 certificate and symmetric
singleton-germ selector subset. Insufficiently certified/identified candidates
remain rich-only. The R6 inverse resolver must not bypass that token authority.

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

GeoCeDG branding has two distinct tracked roles: the byte-exact promoted
`helixTopBar.png` source supplies the frame/application role and deterministic
64-pixel and Windows-package derivatives; the byte-exact promoted
`helixSnapshot.png` source supplies the startup role and its current deterministic
361×480 splash derivative; the prior 542×720 derivative remains tracked as the
published Round-3 artifact. The asset manifest records ingestion filenames,
SHA-256 values, promoted paths, transformation parameters and redistribution
status. Runtime and packaging consumers resolve these tracked resources; the
ignored `artifacts/author-input/` ingestion area is never a build dependency.

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

Entry requires G9S1, G9U0-R6 and G9S1-R1 `PASS — AUTHOR APPROVED`;
all are published at their exact tags. The protected pre-R6 and post-R6
checkpoints remain historical and immutable. The current conditional author
instruction permits the following frontend stages after planning validation:

1. **G9U1A — schema/compiler foundation:** author-approved version-2 schema,
   action catalog, runtime feature service, deterministic v1 migration, static
   localization/icon validation, no new geometric action.
2. **G9U1B — CeDG Construction:** workspace controller, panel defaults, curated
   groups, view/menu actions, document-layout state, public G9U0 actions only if
   G9U0 has passed. This slice is a GUI client and has no G9B dependency.
3. **G9U1C — semantic interaction and rich results:** consume the actual R6
   Point create/move contract; add stroke-only selection, ambiguity chooser,
   current-token markers and explicit selected/multiple/all materialization.
4. **G9U1D — product hardening:** EN/ES help, Continuity `OFF`, accessibility,
   DPI/layout, documents/preferences, branding and end-to-end GUI acceptance.
5. **G9U2 — Dihedral Procedures:** remains blocked until G9 global PASS and a
   dedicated prompt/specification authorize procedures consuming the approved
   ProjectionSystem/frame-map/hinge and binding semantics.

Future implementation evidence is defined in
`docs/validation/g9_public_workspace_validation_matrix.md`. Approval of this
contract does not authorize G9U1 or G9U2.

The first productive GUI acceptance of R6 belongs to G9U1: Point-tool create and
drag on Locus V2 and Spline V2; stroke-vs-interior selection; explicit
self-intersection chooser/cancel; closed periodic-seam drag; transformed and
negative-dilation sources; `k=0`; then save/reopen, undo/redo and copy/remap.
No separate productive R6 GUI existed.

Terminal reconciliation state:

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


## Post-R1 reconciliation authority

The approved post-R6 checkpoint `00982e7e148a634cd57ed928f322774df267d5e3`
remains immutable. This successor consumes published G9S1-R1 without changing
R6 Point semantics or adding an action/family. The current conditional author
authorization is satisfied by the no-material-novelty audit. Implementation has
not started at this planning freeze and is never self-approved. See the
[published R1 pair-consumer contract](g9u1-construction-interaction.md#published-g9s1-r1-pair-consumer-contract)
and the 20 additive `U1-PAIR` rows (138 total). The R4 periodic risk remains
OPEN / TRACKED; an inconclusive native experiment without a real kernel or
persistence violation does not block this candidate under the author's explicit
disposition. No other workspace scope is expanded.

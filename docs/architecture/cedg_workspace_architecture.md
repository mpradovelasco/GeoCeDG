# CeDG workspace architecture

- Status: author-approved G9 architecture; not yet implemented
- Governing normative contract: `geocedg/specs/ui/cedg-workspaces.md`
- Decision: Accepted ADR 0012

## Approved pre-G9U1 product/document gate

G9U0-R2 planning/design is **PASS — AUTHOR APPROVED**. Its implementation is
not authorized and not started. The normative native `.cedg` document policy
and bounded Locus V2 presentation contract must be implemented, validated and
closed PASS before G9U1 can be considered for separate execution
authorization. No workspace, manifest or Desktop behavior is implemented by
this planning closeout.

After R2 implementation passes, this architecture consumes its results as
inputs rather than owning them:

```text
ordinary GeoElement/Locus presentation authority --+
                                                    +--> G9U1 action/layout client
GeoCeDG native document I/O policy -----------------+
```

The workspace manifest continues to own views/actions/layout only. It must not
duplicate the `.cedg`/`.ggb` state machine or style semantics. Its unchanged
`serialization` declaration continues to mean the current ZIP/XML machinery
and `app_code: classic`. The exact approved supersession is listed in
`docs/architecture/g9u0_r2_product_refinement_design.md` and in the
non-normative notice of the workspace spec.

## Outcome

The sustainable extension is a schema-v2 evolution of the existing GeoCeDG
application profile, not a second toolbar system. A workspace controller applies
validated presentation state; an independent feature service decides whether
an action can create an experimental object; the kernel remains unaware of the
active workspace.

The eleven-group CeDG Construction design remains intentionally professional
and non-minimal. G9U1 only presents already approved services—including typed
Locus V2 creation, supported point-on-Locus, rich/guarded metric and rich
intersection/token actions. It neither owns those semantics nor depends on G9B.

```text
apps/geocedg/application-profile.yml (future schema v2)
  -> ProfileManifestLoader + schema validation
  -> ActionRegistry -----------------> localization/icon resolvers
  -> WorkspaceRegistry --------------> toolbar/menu/view compilers
  -> WorkspaceController ------------> GuiManager / Layout / controller
  -> WorkspacePreferenceAdapter -----> isolated GeoCeDG preferences

geocedg/features/*.yml
  -> RuntimeFeatureService ----------> action/command creation gate

kernel construction <---------------- no dependency on workspace state
```

## Current extension points

| Current source | Existing responsibility | Proposed minimal extension |
|---|---|---|
| `apps/geocedg/application-profile.yml` | one perspective and six toolbar groups | become the sole schema-v2 workspace/action instance after authorization |
| `geocedg/specs/ui/application-profile.schema.json` | validates schema v1 | versioned v2 schema; retain v1 reader during migration |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java:33-36,80-139` | loads/validates the packaged manifest | split parsed identity, actions and workspaces into immutable definitions |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java:142-166` | compiles one numeric toolbar | compile each workspace from action references; no duplicate raw strings |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java:42-46` | applies default only when no loaded perspective exists | expose workspace controller and a transient Document-layout state |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GuiManagerGeoCeDG.java:19-22` | creates product menu bar | install workspace/menu action adapters |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMenuBar.java:37-54` | inserts hard-coded DXF product menu | render manifest placements through localization keys |
| `source/shared/common/src/main/java/org/geocedg/common/main/settings/config/AppConfigGeoCeDG.java:50-56` | Classic app code, no command filter | consume runtime feature policy without tying it to workspace choice |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDG.java:34-59` | isolated profile preferences | persist active/per-workspace layout keys in the same namespace |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/geogebra3D/App3D.java:127-130` | creates `EuclidianControllerFor3DD` | `AppGeoCeDG` may return a GeoCeDG subclass for approved product actions |
| `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianController.java:2116-2422,2769-2800` | baseline Intersect and legacy Locus selection | product controller delegates baseline behavior and adds only approved typed selections |
| `tools/legacy/open-laboratory.ps1:36-126` | explicit validated resource launch | remain external; optionally receive a manifest diagnostic-route action |

No productive change to these sources is part of G9P.

## Components

### Profile manifest loader

Reads exactly one packaged profile resource, validates schema version before
constructing definitions, and fails closed on unknown keys/references. It must
not scan the filesystem for workspace fragments. Version-1 input is lifted by a
deterministic in-memory adapter; it is never rewritten automatically.

### Action registry

Owns stable action IDs and immutable metadata: action kind/target, selection
grammar, maturity, feature dependencies, help/localization keys, icon ID,
unavailable policy and output category. Toolbar and menu compilers consume the
same entry. Runtime widgets do not embed product strings or repeat target IDs.

An upstream mode target is resolved against the audited `EuclidianConstants`.
A command action resolves through ordinary command dispatch. A product action
uses a narrow application service. A result-inspector action is read-only. A
diagnostic route launches a separate process.

The Locus action set must keep three distinct operations: a point on semantic
Locus support (durable preimage binding), a rich intersection query, and an
exact-token point child. Their shared graphical chooser is an adapter only; it
does not merge their identities or persist proximity.

### Runtime feature service

Loads the stable and experimental feature manifests once, validates dependency
closure, combines defaults with an explicit user/developer override policy, and
publishes immutable capability decisions. It distinguishes:

```text
LOAD_EXISTING_OBJECT
CREATE_FROM_COMMAND
CREATE_FROM_TOOL
SHOW_ACTION
DIAGNOSTIC_DUAL_RUN
```

Workspace changes never modify these decisions. Existing-file preservation may
remain enabled while interactive creation is disabled.

### Workspace registry and controller

The registry exposes immutable workspace definitions. The controller performs
an atomic UI transaction:

1. validate target availability;
2. cancel the unfinished tool selection and activate Move;
3. capture current user-customizable dock state under the old workspace ID;
4. compile/apply target views, toolbar, menu contributions and help policy;
5. restore compatible saved dock customization;
6. persist the active workspace ID;
7. publish a UI-only change notification.

The transaction has no `Construction`, `Kernel`, command, undo-manager, or
semantic evaluator write. Functional counters should prove zero construction
updates and zero Locus evaluations during a switch.

### Selection adapter

The GeoCeDG Euclidian controller subclass handles only approved product action
IDs. It delegates every inherited mode/type to `super`. For the general
Intersect mode, it adds V2 type acceptance while retaining all existing
branches. It uses action-declared selection slots and passes typed inputs to the
public command/application service; it never duplicates geometric solving.

For a supported point-on-Locus action it passes the selected semantic
branch/component/preimage (including periodic seam choice) to the public point
parent. For scalar creation it gathers the declared state, true coordinate and
domain/mapping roles. It never infers a generator from slider visibility, a
render vertex or arbitrary dependency ancestors.

### Localization, help and icons

Resolvers map declared keys/IDs to GeoCeDG-owned resources. Missing resources
follow the spec's explicit text fallback and generate validation diagnostics.
Template/upstream images are not copied. The status presenter renders the next
selection slot and typed failure reason. Command syntax help remains tied to the
command registry, not a toolbar label.

### Preference and document-layout adapter

Preference state contains active workspace and per-workspace presentation
customization. It is scoped to the existing GeoCeDG settings file and schema
version. Loaded upstream perspectives become a transient `document-layout`
presentation. The adapter offers **Reapply workspace** without writing or
reinterpreting the construction.

The separate GeoCeDG Classic diagnostic route receives its own settings file.
No in-process profile mutation is supported. Supported GeoCeDG semantic objects
remain native and use the same kernel persistence/recomputation semantics; an
external upstream distribution that does not know them is a distinct
unsupported-open boundary, never a reason for lossy downgrade.

## State ownership

| State | Owner | Persisted where | Geometric authority |
|---|---|---|---|
| workspace definitions/actions | checked-in application manifest | packaged resource | none |
| active workspace | GeoCeDG UI | preferences | none |
| dock sizes/visibility | user UI | per-workspace preferences | none |
| document perspective | upstream document UI contract | existing `.ggb` presentation XML | none |
| runtime feature decision | feature service | manifest + approved override | gates creation only |
| active tool/selection slots | Euclidian controller | transient | none until command commits |
| constructed outputs | kernel/Construction | normal `.ggb` contracts | authoritative according to object spec |

## Dihedral procedure boundary

The procedure workspace does not own spatial semantics. Its action service asks
the shared kernel for typed object, ProjectionSystem, frame, binding,
sufficiency and certificate data. The ProjectionSystem-equivalent supplies
relative frame relations, intrinsic frame coordinates, the map into the common
CeDG diagram, orientation/roles, line-of-ground or hinge semantics,
change-of-plane lineage, revision, consistency and degeneration. A confirmed
procedure creates ordinary algorithms/objects in the kernel graph and records
provenance. The UI may group outputs and offer a one-action undo transaction,
but Construction Protocol exposes the explicit steps.

```text
typed selection
 -> resolve durable ProjectionSystem/frame/binding IDs
 -> obtain intrinsic coordinates and diagram-map evidence
 -> obtain admissible persisted frame-relation/hinge choices
 -> user confirmation when non-unique
 -> kernel procedure algorithm(s)
 -> auxiliary/result objects + certificate
 -> ordinary update, undo, copy and persistence
```

No visible-placement or screen-coordinate inference, label association, macro
side effect, or duplicate editable 3D representation is permitted. Moving a
view is never a change of projection-system semantics; changing an approved
diagram map is an explicit kernel/document-semantic operation, not a workspace
layout mutation.

## Failure and recovery

- Invalid schema/action reference: reject v2 and use the last accepted v1
  profile with an explicit startup diagnostic.
- Missing feature dependency: action disabled with reason; never call dispatch.
- Unknown saved workspace: fall back to Construction and preserve the unknown
  preference for diagnosis.
- Document toolbar parse failure: retain construction load, apply Construction
  workspace, and report presentation recovery.
- Workspace switch failure: roll back the UI transaction to the old workspace;
  construction remains untouched.
- Localization/icon failure: use approved text fallback; never load an
  unregistered embedded asset.
- Procedure ambiguity/degeneration: do not commit outputs; present typed kernel
  evidence.

## Implementation slices

1. Schema-v2 definitions, validator, v1 adapter and static action compiler.
2. Runtime feature service and consistency checks across command/tool/menu/load.
3. Construction workspace controller, preferences and Document-layout handling.
4. GeoCeDG controller extensions as GUI clients for already approved public
   actions, with point-on-Locus preimage selection and no G9B dependency.
5. Visual/accessibility/localization/icon validation.
6. Dihedral Procedures only after G9 global PASS and its own approved semantic
   procedure contract consuming ProjectionSystem/map/hinge semantics.

G9B must not depend on G9U1, and G9U1 must not depend on G9B. Recommended serial
execution may still place G9U1 earlier for operational safety; that ordering
does not create a semantic edge. G9U2 retains the approved global G9 gate.

The future implementation validation authority is defined in
`docs/validation/g9_public_workspace_validation_matrix.md`.

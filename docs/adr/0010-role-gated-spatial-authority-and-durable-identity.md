# ADR 0010: role-gated spatial authority and durable identity

- Status: **Accepted**
- Decision phase: G9P closeout — author approved
- Date: 2026-08-16
- Scope: shared-kernel spatial identity, projection bindings, and edit authority
- Productive implementation: not authorized

## Context

GeoCeDG must bind one semantic spatial object to its defining and derived
orthographic representations without losing constructive traceability. The
pinned upstream kernel already provides native 3D geos, construction-DAG
algorithms, coordinate systems, projection operations, XML persistence, and
view adapters. It does not provide a durable per-geo identity plus a typed,
serializable spatial/projection relation and a type-specific sufficiency
certificate.

The current identifiers and associations cannot fill that gap:

- `ConstructionElement.ceID` is an in-memory creation/sort value allocated by
  the application, not a persisted semantic identity;
- construction indices change with construction-list operations;
- labels are mutable and are collision-renamed during copy/paste;
- layers and view flags are presentation state;
- the GGB document header ID identifies a document, not each object; and
- an existing plane-view relation persists the plane label, which is adequate
  only for that legacy view-state contract.

The 2022 CeDG proof of concept demonstrates a useful hierarchical association
idea with `ListObjectF`, `ListAssociations3DViews`, `ObjectViewCouple`,
`ggbOnInit`, `registerAddListener`, `onAdd`, and `searchAssociatedList`. Its
list membership and label-string lookup are research evidence, not a safe
production identity or dependency mechanism.

The initial G9P candidate modeled each intrinsic projection
`pi_i: R3 -> R2_i`, but author review exposed a missing semantic level. A
dihedral construction also needs a geometric map
`delta_i: R2_i -> R2_CeDG` that unfolds or places each frame in one common CeDG
construction diagram. That map carries relative plane, line-of-ground,
orientation, fold-side, and auxiliary change-of-plane meaning. It is not a
viewport, screen transform, physical sheet placement, or toolbar arrangement.

Current source confirms the boundary. `CoordSys` provides reusable intrinsic
plane-coordinate mathematics. In contrast,
`EuclidianViewForPlaneCompanion.setTransformRegardingView()` chooses a
mirror/rotation from the 3D camera and screen matrix, and its XML identifies the
plane by label. That legacy view state cannot be promoted to semantic diagram
authority.

A second design question is edit direction. CeDG procedures frequently define
spatial geometry through orthographic projections, while upstream already
offers editable native 3D geos. Letting both sides mutate the same revision
would create cycles, conflict resolution by event order, and hidden authority.

## Decision

GeoCeDG adopts the following architecture.

### Durable identities

1. Introduce opaque, document-scoped, serialized IDs for spatial objects,
   projection frames, projection systems, diagram maps, frame relations, and
   projection bindings.
2. Permit ordinary kernel geos that participate in these relations to receive
   an optional serialized `PersistentGeoId`.
3. Resolve those IDs through a construction-scoped registry with uniqueness
   checks and a two-stage load process.
4. Never derive an ID from label, coordinates, type, creation order, XML
   position, layer, or visual coincidence.
5. Do not repurpose `ConstructionElement.ceID`, construction indices, view
   flags, or the document header ID.

### Projection system and diagram maps

Introduce a construction-owned `ProjectionSystem` with durable identity,
semantic version, common two-dimensional diagram coordinate frame, current
revision, participating map/relation IDs, state, and immutable system
certificate. Each `ProjectionDiagramMap` has its own identity and relates one
frame use to the system:

\[
x\xrightarrow{\pi_i}q_i\xrightarrow{\delta_m}p_m,
\qquad
\delta_m(q)=A_mq+b_m,
\qquad \det A_m\ne0.
\]

The initial admitted family is an oriented Euclidean isometry or declared unit
similarity. Other affine/projective families remain unsupported until separately
specified. Each map records frame-use role (`DEFINING` or `AUXILIARY`),
orientation, fold side, units, provenance, definition inputs and revision.
Frame-use role is independent of an object's projection-binding role.

Typed `ProjectionFrameRelation` records express hinge unfolding and auxiliary
change-of-plane ancestry. For a declared hinge, its intrinsic line in both
planes must map to the same common-diagram line with the declared orientation.
No relation is inferred from labels, coordinate proximity, creation order, or
visible placement.

Object sufficiency remains defined on intrinsic projections

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

Bindings store ordinary kernel geos in common-diagram coordinates and recover
intrinsic observations with `delta_m^{-1}`. The typed composition

\[
\Psi_{T,S}(x)=\left(\delta_m^{(T)}(\pi_i(x))\right)_{m\in S}
\]

has the same injectivity as `Phi` only when every required diagram map is fixed,
valid and bijective. A coherent common diagram gauge `g`, applied to every map
and bound diagram object, leaves the recovered intrinsic observations and
sufficiency unchanged. Viewport transforms are never certificate inputs.

### Identity continuity across lifecycle operations

Durable semantic identity is not Java reference identity and is not label
identity. Current host replacement can rewire a target, while XML reload and
undo may rebuild Java instances. Therefore the decision distinguishes:

- ordinary recomputation preserves identity and updates revision evidence;
- an explicit target-based semantically compatible redefine may preserve
  identity only through one atomic provider/type/schema/role compatibility
  transaction, even when the Java instance changes; definition revision always
  increments and topology revision increments only when explicitly declared;
- true semantic replacement or type-incompatible redefine receives fresh
  identity or fails explicitly, with no silent binding transfer;
- delete then recreate receives fresh identity;
- copy/duplicate receives a fresh, consistently remapped closure; and
- undo/redo and save/reopen restore the serialized identity graph.

Host compatibility or a retained label is evidence neither of continuity nor
of replacement. The explicit transaction target and approved semantic
compatibility predicate decide.

### Role-gated bindings

Each binding has exactly one role:

- `DEFINING`: an authoritative input under projection-defined mode;
- `DERIVED`: output generated from the spatial object;
- `AUXILIARY`: construction support without defining authority;
- `ANALYSIS`: validation or measurement representation; or
- `PRESENTATION`: layout-only representation.

Roles are persistent and explicit. They are not inferred from the view in
which a geo happens to appear.

A binding also names its projection system and diagram map. Its projected geos
are therefore unambiguously interpreted as common-diagram representations, and
the validated map supplies intrinsic frame coordinates. A map that arranges a
geometric CeDG projection is not a `PRESENTATION` binding.

### One authority per object revision

Each spatial object revision has exactly one `EditAuthorityMode`.

- `PROJECTION_DEFINED` is the first approved pilot direction: defining
  projections reconstruct and validate the semantic spatial object; spatial
  and additional projected representations are derived.
- `SPATIAL_DEFINED` is designed for a later gate: defining spatial primitives
  generate derived projections.

The modes share identity, frames, bindings, certificates, lifecycle, and DAG
contracts. They are never active simultaneously for the same revision.

Changing mode is an explicit, undoable construction transaction. It checks
destination sufficiency, rewrites DAG inputs/outputs, performs cycle checks,
reclassifies binding roles, creates a new revision, and republishes only after
validation. “The most recently edited side wins” is prohibited.

### Publication and view boundary

Projection-system evaluation first validates map invertibility, units,
orientation, hinge/change-of-plane relations, and revisions. Reconstruction
then builds an immutable candidate from intrinsic observations and validates
sufficiency, degeneration, correspondence, intrinsic reprojection, and composed
diagram reprojection before one atomic publication.
Failure publishes the current failure state and withdraws or undefines the
derived payload. An old valid payload may remain as historical diagnostic
evidence, but it cannot be exposed as current geometry.

The existing 3D view receives ordinary compatible derived kernel geos through
the established `GeoElement -> Drawable3D` boundary. Camera, renderer, hit
testing, and screen transforms never feed the certificate. Direct editing of
the derived view representation is disabled or routed to an explicit future
authority-transition operation.

### Independent status axes

Capability support, authority mode, source definition, certificate result,
currentness, representation fidelity, numeric guarantee, and
topology/correspondence are separate. Projection-system consistency is a
further independent axis with `NOT_EVALUATED`, `CONSISTENT`, `INCONSISTENT`,
`DEGENERATE`, and `UNDEFINED`. Capability support is closed as
`SUPPORTED` or `UNSUPPORTED`. The mandatory certificate states are
`NOT_EVALUATED`, `VALID`, `UNDERDETERMINED`, `AMBIGUOUS`,
`INCONSISTENT_PROJECTIONS`, `DEGENERATE`, and `UNDEFINED`. An unsupported
capability publishes `UNSUPPORTED` plus `NOT_EVALUATED` and no current payload.
`VALID` does not claim exact arithmetic, and numeric approximation does not
itself imply invalidity.

System inconsistency is not object-level `INCONSISTENT_PROJECTIONS`: the latter
applies only after a valid system has interpreted the bound diagram objects.

## Rationale

The decision preserves the normal kernel graph and makes the semantic choice
visible at every revision. It supports the projection-defined workflow that
CeDG requires without discarding the reusable upstream 3D model. Persistent
identity is needed because rename, save/reopen, undo, copy/paste, redefine,
delete/recreate, and dynamic invalidity cannot be handled reliably by labels
or object position.

Role gates avoid two common conflations:

1. a representation can exist without being defining; and
2. a spatial object can retain identity while its current geometry is
   degenerate or undefined.

## Consequences

### Positive

- Projection associations become typed, inspectable, serializable, and
  independent of labels and view state.
- Multi-frame dihedral arrangements, hinges, fold orientation and auxiliary
  changes of plane become explicit semantic construction inputs instead of
  visible-layout conventions.
- Dynamic changes use the normal construction scheduler and invalidation
  semantics.
- Projection-defined and later spatial-defined objects can share one model
  without sharing authority in one revision.
- The 3D view remains a reusable frontend rather than a second geometric
  database.
- Copy, undo, and reload can be specified and tested by identity rather than by
  coordinate coincidence.

### Costs and risks

- Adding optional persistent IDs to arbitrary participating geos touches
  shared XML, copy/paste, undo/redefine, macros, and construction lifecycle.
- Projection-system, diagram-map and relation records add another versioned
  identity closure whose copy, reload and invalidation rules must be tested.
- Registry collision and malformed-reference policies must be deterministic.
- Existing code often resolves geos by label, so accidental fallback is a
  significant regression risk.
- A mode transition is a graph rewrite, not a UI toggle.
- The initial projection-defined-only pilot deliberately leaves direct 3D
  editing unavailable.

## Rejected alternatives

### Labels as persistent identity

Rejected because rename and collision renaming change labels, label reuse after
delete creates false continuity, and labels have user-facing rather than
semantic ownership.

### `ceID` or construction index as durable identity

Rejected because these values support in-memory order/update behavior and are
not the versioned, serialized, copy-remapped identity required here.

### Coordinates, visual coincidence, layer, or view membership

Rejected because dynamic movement and projection degeneracy destroy the
association, and viewport/presentation state cannot own geometric truth.

### An unordered frame set without diagram maps

Rejected because individual `pi_i` operators do not encode how their intrinsic
coordinates inhabit the common CeDG diagram, share a line of ground, or arise
through an auxiliary change of plane.

### Plane-view mirror/rotation as `delta_i`

Rejected because the current transform is selected from camera/screen state and
persisted as label-keyed UI settings. Low-level matrix operations may be reused
behind the new semantic contract, but the view transform itself is not
geometric authority.

### JavaScript/list association service

Rejected for production because it depends on labels/listeners, creates a
parallel lifecycle, and cannot supply normal-DAG type-specific reconstruction,
serialization, or failure semantics. Retained as historical regression input.

### Always bidirectional editing

Rejected because two active authorities create cycles and nondeterministic
conflicts. A later explicit authority transition can enable spatial-defined
work without weakening the single-authority invariant.

### New independent 3D kernel or opaque CAD feature tree

Rejected because upstream already owns geometric primitives and the normal
dependency graph, while CeDG requires explicit construction provenance and
projection correspondence.

### Sidecar identity map

Rejected as the primary persistence mechanism because save/reopen, copy,
undo, share, and document portability must keep identity inside the
construction serialization authority.

## Acceptance record and implementation gate

The G9P closeout author review accepted:

- the one-authority invariant and projection-defined first pilot;
- optional persistent IDs on participating geos plus the construction registry;
- projection-system/map/relation identities, intrinsic-versus-diagram
  composition, hinge consistency, and common-gauge invariance;
- the source-backed recomputation/compatible-redefine/true-replacement/copy/
  restore identity taxonomy;
- explicit mode transition rather than event-order conflict resolution;
- atomic candidate publication and no-stale-payload rule;
- separate status axes; and
- the one-way derived 3D-view boundary.

This Accepted ADR authorizes design-to-code work only through the separately
authorized G9A phase gates in ADR 0011. G9A1/A2/A3 remain designed, not
authorized, and not started.

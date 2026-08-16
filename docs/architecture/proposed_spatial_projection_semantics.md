# GeoCeDG — Spatial object and canonical projection research input

**Status:** Historical research/architecture input; non-normative
**Authority:** Governing normative contract is `geocedg/specs/spatial/g9-spatial-projection-semantics.md`; ADR 0010/0011 are Accepted
**Target phase:** G9, before the Python DSL

**G9P-R1 refinement:** this research input now distinguishes intrinsic
projection from geometric placement in a common CeDG diagram. The refinement is
author approved in the governing contract; this explanatory research input
remains non-normative.

## 1. Purpose

GeoCeDG shall make the association between a spatial object and its orthographic projections explicit, typed, dynamic, and serializable. The goal is not merely to group drawings. The goal is to determine whether the available projections define the spatial object completely, reconstruct or update that object, generate additional projections, and report degenerations or ambiguity.

The existing CeDG proof of concept based on GeoGebra lists and JavaScript demonstrates that view–object association is hierarchically composable. It remains a research reference; production semantics belong in the shared Java kernel/semantic layer.

## 2. Fundamental distinction

```text
SpatialObject3D       geometric identity and dependencies
ProjectionFrame       geometric projection definition
ProjectionSystem      typed multi-frame geometric arrangement
ProjectionDiagramMap  intrinsic-frame to common-diagram map
ProjectionFrameRelation  hinge/change-of-plane relation
ProjectionBinding     typed association to 2D kernel entities
Viewport              screen transform and interaction state
DrawingViewport       physical sheet placement and scale
```

A `ProjectionFrame` is geometric. A `Viewport` is visual. Changing zoom or DPI must not change the spatial object, canonical certificate, metric result, or intersection.

A `ProjectionDiagramMap` is also geometric: it records how intrinsic
coordinates of a frame are unfolded or placed in the common two-dimensional
CeDG construction diagram. It is not the plane-view camera-derived
mirror/rotation, a canvas transform, or physical sheet placement.

## 3. Core model

```text
SpatialObject3D
├─ stableId
├─ semanticVersion
├─ spatialType
├─ constructionDefinition
├─ parameters and validity domain
├─ exactness status
├─ ProjectionSet
└─ primitiveDefinition | compositeDefinition

ProjectionSystem
├─ stableId / semanticVersion / systemRevision
├─ common DiagramCoordinateFrame2D
├─ ProjectionDiagramMaps[]
├─ ProjectionFrameRelations[]
├─ state and immutable system certificate
└─ construction provenance

ProjectionDiagramMap
├─ stableId / systemId / frameId
├─ frameUseRole: defining | auxiliary
├─ delta(q)=Aq+b / units / orientation / fold side
├─ defining relation IDs / provenance
└─ mapRevision / validity

ProjectionFrameRelation
├─ stableId / sourceMapId / destinationMapId
├─ hingeUnfold | changeOfPlane
├─ oriented hinge/support construction
└─ relationRevision / validity

ProjectionBinding
├─ objectId
├─ systemId
├─ diagramMapId
├─ frameId
├─ projectedElementIds[]
├─ role
├─ correspondence
├─ provenance
├─ exactness/tolerance metadata
└─ validity state
```

Projection roles are `defining`, `derived`, `auxiliary`, `analysis`, and `presentation`.
Frame-use role in the system is independent of binding role. A geometric
diagram map is not presentation state.

## 4. Canonical projection schema

For type `T`, let `X_T` be its valid configuration space and let `Pi={pi_i}` be known projection operators. Define

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

For a point, explicitly distinguish

\[
x\xrightarrow{\pi_i}q_i\xrightarrow{\delta_m}p_m,
\qquad
\delta_m(q)=A_mq+b_m,
\qquad \det A_m\ne0.
\]

Bound ordinary 2D geos store common-diagram `p_m`; canonical reconstruction
uses `q_i=delta_m^{-1}(p_m)`. The initial admitted map family is an oriented
isometry or declared unit similarity. Define the typed diagram composition

\[
\Psi_{T,S}(x)=
\left(\delta_m^{(T)}(\pi_i(x))\right)_{m\in S}.
\]

Sufficiency remains intrinsic. For fixed valid bijective maps, `Psi` is
injective exactly when `Phi` is injective. A coherent common-diagram gauge
`delta'=g o delta`, `p'=g(p)` leaves `delta'^-1(p')=delta^-1(p)` and therefore
cannot change sufficiency.

For a declared hinge between two nonparallel frame planes, its intrinsic line
in both frames must map to the same common-diagram line with explicit
orientation and fold side. Auxiliary changes of plane are typed relations with
construction provenance, never inferences from visible placement.

A schema is canonical on a declared domain when:

1. `Phi` is injective modulo explicitly declared geometric equivalences;
2. an explicit CeDG reconstruction exists;
3. reprojection matches the defining bindings;
4. all correspondence and non-degeneration predicates hold;
5. the reconstruction is represented in the dependency graph.

A schema returns a certificate, not a Boolean only:

```text
VALID
UNDERDETERMINED
AMBIGUOUS
INCONSISTENT_PROJECTIONS
DEGENERATE
UNDEFINED
```

The certificate includes the failed predicates and implicated bindings.
It also references the exact system/map/relation/frame revisions. Projection
system state (`NOT_EVALUATED`, `CONSISTENT`, `INCONSISTENT`, `DEGENERATE`, or
`UNDEFINED`) remains distinct from object-level
`INCONSISTENT_PROJECTIONS`.

## 5. Primitive schemas

### Point

Two orthographic projections in known non-parallel frames, with explicit correspondence, normally determine one point.

### Line

The schema must recover both location and direction. A projection may collapse to a point when the line is perpendicular to the plane. Such a projection remains semantically useful but cannot be the only directional definition. A second non-collapsed projection or an equivalent defining construction is required.

### Plane

Use sufficient defining primitives, such as three non-collinear points or two incident lines. Projected outlines alone are not a general definition.

### Spatial curve

Two projected curves do not determine a spatial curve without correspondence. A shared parameter, a point-pair mapping, or a topological correspondence is required. Locus V2 should provide a common parameter for CeDG-generated spatial curves whenever possible.

### Surfaces

Canonical schemas should use defining primitives and parameters: center, axis, vertex, directrix, generatrix law, radius, section, or equivalent data. Silhouettes and contours are normally derived bindings.

## 6. Composite objects and solids

A CeDG complex object is defined through spatial components and constructive relations. A projective boundary object may contain:

```text
vertices
spatial curves / edges
supporting surfaces
oriented faces
oriented boundary loops
incidence and adjacency
projection bindings
validity domains
closure and orientation diagnostics
```

A closed oriented valid boundary may define a solid interior. The model may reuse B-Rep topological concepts, but its authority is the CeDG construction and the linked projections. CSG operations, if later added, are construction procedures rather than the sole persistent truth.

## 7. Dynamic evaluation

```text
defining projections changed
    -> invalidate certificate
    -> reconstruct/update spatial object
    -> validate topology and degenerations
    -> regenerate derived projections
    -> compare reprojection residuals
    -> publish explicit state
```

No stale spatial object may remain active after failed validation. Multiple admissible branches must be exposed as ambiguity unless the construction explicitly selects one.

## 8. Serialization

Persist:

- stable object, frame, projection-system, diagram-map, frame-relation and
  binding IDs;
- common diagram coordinate frame, typed maps, hinges/change-of-plane inputs,
  units, orientation and fold choice;
- semantic/schema versions;
- binding roles;
- projected element IDs;
- parameter/topology correspondence;
- construction provenance;
- exactness/tolerance policy;
- inputs needed to recompute the certificate.

Do not persist associations only through labels or names. Legacy files remain valid without associations and may be upgraded only through explicit migration or user action.

Loading registers all IDs before resolving relations and building normal-DAG
dependencies. Save/reopen and undo restore serialized IDs. Copy creates and
remaps a fresh selected closure, except for an explicitly permitted shared
same-construction system dependency; cross-document live references are
forbidden.

Ordinary recomputation preserves identity. A target-based semantically
compatible redefine may retain identity only through an atomic
provider/type/schema/role compatibility transaction, with a definition
revision and a topology revision only when declared. True replacement or
type-incompatible redefine gets fresh identity or fails; delete/recreate and
copy get fresh identity; labels and host-instance compatibility are never the
continuity predicate.

## 9. Relationship with the DSL

The DSL shall target the kernel contract:

```text
spatial object declaration
+ projection frame declarations
+ defining bindings or defining primitives
+ construction relations
+ expected canonical schema
```

The DSL compiler asks the kernel to create and validate objects. It does not implement independent reconstruction or validity rules.

## 10. Minimum G9 validation matrix

| Case | Expected result |
|---|---|
| General point, two views | `VALID` |
| Common-diagram `q -> p -> q` round trip | equal intrinsic observation |
| Coherent common-diagram gauge | unchanged sufficiency and payload |
| Inconsistent hinge maps | system `INCONSISTENT`; no stale object payload |
| Noninvertible diagram map | system `DEGENERATE`; no object evaluation |
| Auxiliary change-of-plane map | explicit relation and deterministic revision |
| Point, one view only | `UNDERDETERMINED` |
| General line, two non-collapsed views | `VALID` |
| Line of sight to one plane plus second view | `VALID` |
| Line with only collapsed view | `UNDERDETERMINED` |
| Contradictory point projections | `INCONSISTENT_PROJECTIONS` |
| Spatial curve without correspondence | `UNDERDETERMINED` or `AMBIGUOUS` |
| Spatial curve with common Locus V2 parameter | `VALID` |
| Tetrahedron from sufficient views | `VALID` |
| Open boundary claimed as solid | invalid closure diagnostic |
| Dynamic passage through singular position | explicit `DEGENERATE`, then recovery |
| Save/reopen | stable IDs and equal certificate inputs |

G9A1 owns system/map/relation identity, XML, copy and restore substrate; G9A2
owns map/relation evaluation and the point pilot; G9A3 owns hostile lifecycle,
redefine and migration behavior. G9B consumes the foundation after G9A3 and
has no semantic dependency on G9U1. After the approved global G9 gate, G9U2
may consume these records for explicit procedures but may not define their
meaning in a GUI controller.

## 11. Deferred decisions

- exact mapping to existing GeoGebra 3D classes;
- whether `SpatialObject3D` is a new `GeoElement`, a semantic aggregate, or a hybrid;
- XML element names and migration version;
- public command names;
- editing policy from the 3D view;
- exact surface/solid topology implementation;
- symbolic versus numerical reconstruction per type.

These decisions require the source map produced by the first agent task.

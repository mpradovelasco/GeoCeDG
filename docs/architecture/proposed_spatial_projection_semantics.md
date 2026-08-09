# GeoCeDG — Proposed Spatial Object and Canonical Projection Semantics

**Status:** Proposed research/architecture input  
**Authority:** Non-normative until converted into approved specifications and ADRs during G9  
**Target phase:** G9, before the Python DSL

## 1. Purpose

GeoCeDG shall make the association between a spatial object and its orthographic projections explicit, typed, dynamic, and serializable. The goal is not merely to group drawings. The goal is to determine whether the available projections define the spatial object completely, reconstruct or update that object, generate additional projections, and report degenerations or ambiguity.

The existing CeDG proof of concept based on GeoGebra lists and JavaScript demonstrates that view–object association is hierarchically composable. It remains a research reference; production semantics belong in the shared Java kernel/semantic layer.

## 2. Fundamental distinction

```text
SpatialObject3D       geometric identity and dependencies
ProjectionFrame       geometric projection definition
ProjectionBinding     typed association to 2D kernel entities
Viewport              screen transform and interaction state
DrawingViewport       physical sheet placement and scale
```

A `ProjectionFrame` is geometric. A `Viewport` is visual. Changing zoom or DPI must not change the spatial object, canonical certificate, metric result, or intersection.

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

ProjectionBinding
├─ objectId
├─ frameId
├─ projectedElementIds[]
├─ role
├─ correspondence
├─ provenance
├─ exactness/tolerance metadata
└─ validity state
```

Projection roles are `defining`, `derived`, `auxiliary`, `analysis`, and `presentation`.

## 4. Canonical projection schema

For type `T`, let `X_T` be its valid configuration space and let `Pi={pi_i}` be known projection operators. Define

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

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

- stable object and frame IDs;
- semantic/schema versions;
- binding roles;
- projected element IDs;
- parameter/topology correspondence;
- construction provenance;
- exactness/tolerance policy;
- inputs needed to recompute the certificate.

Do not persist associations only through labels or names. Legacy files remain valid without associations and may be upgraded only through explicit migration or user action.

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

## 11. Deferred decisions

- exact mapping to existing GeoGebra 3D classes;
- whether `SpatialObject3D` is a new `GeoElement`, a semantic aggregate, or a hybrid;
- XML element names and migration version;
- public command names;
- editing policy from the 3D view;
- exact surface/solid topology implementation;
- symbolic versus numerical reconstruction per type.

These decisions require the source map produced by the first agent task.

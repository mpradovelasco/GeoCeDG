# G9 spatial semantic model

**Status:** author-approved G9 architecture; implementation not started

**Affected layer:** shared Java kernel semantic layer

**Implementation state:** no productive G9 spatial implementation started

**G9P-R1 refinement:** adds explicit projection-system and common-diagram map
semantics and the source-backed redefine taxonomy; the governing spatial
specification is normative and author approved.

## 1. Architectural outcome

The least-invasive sustainable architecture is a construction-scoped semantic
graph layered beside existing native 3D geos and algorithms. It adds durable
identity, projection frames, typed bindings, per-type certificates, and
revision publication. It does not replace `Kernel3D`, duplicate coordinates in
a GUI service, or infer object association from saved view state.

```text
ordinary 2D kernel geos ---- PersistentGeoId ----+
                                                  |
ProjectionFrame inputs ---------------------------+--> normal Construction DAG
ProjectionSystem + DiagramMap + FrameRelation ----+           |
                                                  |           |
ProjectionBinding (role + correspondence) --------+           v
                                                   CanonicalProjectionSchema<T>
                                                               |
                                                       immutable candidate
                                                               |
                                                   validate + reproject
                                                               |
                                                  atomic current publication
                                                   /                    \
                                      derived 2D kernel geos     derived 3D kernel geo
                                                                          |
                                                                  existing 3D view adapter
```

Only the construction graph above owns geometric truth. Registries provide
identity lookup; they do not schedule hidden recomputation.

The ordinary 2D geos in a binding are common CeDG diagram objects `p`. The
validated system map supplies intrinsic frame coordinates `q=delta^-1(p)`
before a canonical schema runs. Viewport coordinates never enter this graph.

## 2. Existing reusable substrate and missing contract

The pinned source already provides:

- native `GeoPoint3D`, `GeoVector3D`, `GeoLine3D`, `GeoRay3D`,
  `GeoSegment3D`, `GeoPlane3D`, `GeoConic3D`, `GeoCurveCartesian3D`,
  `GeoLocus3D`, `GeoSurfaceCartesian3D`, quadrics, polygons, and polyhedra;
- `Kernel3D`, `Manager3D`, and `AlgoDispatcher3D` integrated with the normal
  construction graph;
- `CoordSys`, `CoordMatrix4x4`, and `Coords` projection operations;
- ordinary `AlgoElement.setInputOutput()` dependency registration;
- shared construction XML and whole-construction load/undo behavior; and
- `EuclidianView3D` mapping kernel geos to derived `Drawable3D` objects.

It does not provide:

- a stable per-geo document identity;
- a stable semantic spatial-object identity independent of one representation;
- a geometric projection-frame identity independent of a viewport;
- a durable relation among several frames and their common CeDG diagram;
- an explicit intrinsic-to-diagram map, hinge, fold orientation, or auxiliary
  change-of-plane relation;
- defining/derived/auxiliary/analysis/presentation binding roles;
- explicit endpoint/branch/parameter/topology correspondence;
- type-specific canonical sufficiency and reconstruction evidence; or
- revision-scoped inconsistency, ambiguity, and degeneration publication.

The architecture adds only this missing semantic boundary.

Current source supplies low-level but not semantic substitutes. `CoordSys`
maps intrinsic plane coordinates into 3D and maintains drawing matrices. The
current `EuclidianViewForPlaneCompanion`, however, multiplies those matrices by
a mirror/rotation selected from the 3D camera and screen matrix; its XML stores
view settings and a plane label. That transform remains legacy UI state and
must not become a `ProjectionDiagramMap`.

## 3. Construction-scoped aggregates

### 3.1 Identity registry

`SpatialIdentityRegistry` is a conceptual construction-owned service:

```text
PersistentGeoId      -> participating GeoElement
SpatialObjectId      -> SpatialObjectRecord
ProjectionFrameId    -> ProjectionFrameRecord
ProjectionSystemId   -> ProjectionSystemRecord
ProjectionDiagramMapId -> ProjectionDiagramMapRecord
ProjectionFrameRelationId -> ProjectionFrameRelationRecord
ProjectionBindingId  -> ProjectionBindingRecord
```

Registry operations are transactional and collision-checked. Lookup is never
by label fallback. The registry is not global: copying between constructions
requires an explicit closure/remap transaction.

### 3.2 Spatial object record

```text
SpatialObjectRecord<T>
├─ SpatialObjectId
├─ semanticVersion / SpatialType
├─ EditAuthorityMode
├─ CanonicalSchemaId + schemaVersion
├─ construction definition input IDs
├─ ordered or keyed ProjectionBindingIds
├─ numeric policy + validity domain
├─ semanticRevision
├─ state axes + diagnostics
└─ current SpatialPayload<T>? / current certificate
```

The record's identity and definition outlive invalid geometry. `SpatialPayload`
exists only when the current revision is publishable. The payload should use or
adapt established kernel 3D values instead of defining another numeric kernel.

### 3.3 Frame record

```text
ProjectionFrameRecord
├─ ProjectionFrameId / semanticVersion
├─ construction inputs for origin and orientation
├─ origin, oriented basis, normal, projection direction
├─ handedness / units / projection kind
├─ exactness and validity
└─ frameRevision
```

The frame composes `CoordSys`/matrix operations, but adds persistent identity
and semantic guarantees. Euclidian settings and camera transforms are not
inputs.

### 3.4 Projection system, diagram map, and frame relation records

Before binding evaluation, the semantic layer introduces the following
construction-scoped system records.

```text
ProjectionSystemRecord
├─ ProjectionSystemId / semanticVersion
├─ common DiagramCoordinateFrame2D / units
├─ ProjectionDiagramMapIds[]
├─ ProjectionFrameRelationIds[]
├─ construction definition input IDs
├─ systemRevision
├─ ProjectionSystemState + diagnostics
└─ current ProjectionSystemCertificate

ProjectionDiagramMapRecord
├─ ProjectionDiagramMapId / ProjectionSystemId / ProjectionFrameId
├─ frameUseRole: DEFINING | AUXILIARY
├─ optional diagramAnchor
├─ typed map definition delta(q)=Aq+b
├─ units / orientation / fold side
├─ defining relation IDs / provenance
└─ mapRevision / exactness / validity

ProjectionFrameRelationRecord
├─ ProjectionFrameRelationId / ProjectionSystemId
├─ sourceMapId / destinationMapId
├─ HINGE_UNFOLD | CHANGE_OF_PLANE
├─ oriented hinge/support construction input IDs
├─ fold choice / provenance
└─ relationRevision / state / diagnostics
```

`frameUseRole` characterizes the frame within the system and is independent of
the object-specific binding role. An auxiliary frame can be a defining
observation for one object. The system relation graph is semantic data whose
algorithms declare normal DAG dependencies; it is not a second scheduler.

### 3.5 Binding record

```text
ProjectionBindingRecord
├─ ProjectionBindingId
├─ SpatialObjectId / ProjectionSystemId / ProjectionDiagramMapId
├─ ProjectionFrameId
├─ PersistentGeoId[]
├─ role
├─ representation kind
├─ correspondence descriptor
├─ provenance / exactness / tolerance
├─ validity
└─ bindingRevision
```

Correspondence is typed rather than an unstructured string. Initial descriptor
families should cover a single point, oriented endpoints, free/bound vector,
line orientation, conic support/parameter data, and branch/component/common
parameter mappings. Unsupported correspondence is an explicit status.

The projected geos use the common diagram coordinates selected by the map.
Their intrinsic frame representation is never inferred from screen placement:

```text
diagram geo p -> validated DiagramMap inverse -> intrinsic q -> frame lift
```

### 3.6 Schema and certificate

`CanonicalProjectionSchema<T>` is a versioned stateless semantic contract or a
construction-owned algorithm family. It consumes resolved frame and binding
snapshots plus a numeric policy and returns an immutable candidate:

```text
CanonicalCandidate<T>
├─ source revision tuple
├─ required system/map/relation certificate tuple
├─ reconstructed payload?
├─ predicate results
├─ rank/conditioning/residual evidence
├─ certificate state
├─ other state-axis updates
└─ diagnostics / deterministic work counters
```

A publisher compares the candidate revision tuple with current inputs. A
candidate computed for superseded inputs is discarded, not partially applied.
One current publication updates the certificate, payload availability, derived
geos, and downstream invalidation coherently.

The separate immutable `ProjectionSystemCertificate` evaluates required map
invertibility, unit/orientation policy, hinge coincidence and relation
consistency for one system revision. It reports `NOT_EVALUATED`, `CONSISTENT`,
`INCONSISTENT`, `DEGENERATE`, or `UNDEFINED` independently from object-level
`INCONSISTENT_PROJECTIONS`. A broken unused auxiliary record does not invalidate
an object whose certificate does not reference it.

## 4. Authority and DAG topology

### 4.1 Projection-defined topology

The G9A2 point pilot uses:

```text
frame geos + projection-system maps/relations + defining 2D diagram geos
    + binding/correspondence records
    -> AlgoCanonicalSpatialPoint
        -> validate system; map p through delta^-1 to intrinsic q
        -> semantic point record/certificate
        -> derived GeoPoint3D-compatible representation
        -> optional derived 2D projections
```

The algorithm declares every geometric input and output with the normal kernel
dependency mechanism. Registry records resolve identity but are not listener
callbacks. An edit to one defining geo follows ordinary invalidation and
recompute order.

For a frame embedding `iota_i(q)=o_i+B_i q`, the initial map contract is

```text
q_i = pi_i(x)
p_m = delta_m(q_i) = A_m q_i + b_m, det(A_m) != 0
```

The first admitted diagram maps are oriented isometries or declared unit
similarities. For a hinge `H_ij`, its intrinsic lines in both frames must map to
the same oriented common-diagram line. Arbitrary affine/projective map families
remain unsupported until separately specified.

### 4.2 Later spatial-defined topology

A future spatial-defined object reverses the algorithm edges:

```text
defining spatial primitives/parameters
    -> semantic spatial object
        -> derived ProjectionBinding geos
```

It uses the same object and binding model but a different authority mode. This
direction is not implemented in G9A2.

### 4.3 Authority transition

An authority transition is a graph rewrite:

1. freeze and identify the source revision;
2. validate destination defining data;
3. construct the prospective input/output graph;
4. reject cycles or mixed role ownership;
5. atomically replace algorithm ownership and roles;
6. create a new object revision and recompute; and
7. record the transaction for undo/redo and serialization.

The object ID is preserved only if the transaction explicitly asserts semantic
continuity. A click or edit event does not change authority.

## 5. Revision and publication model

Each relevant source owns a monotone construction-revision key. The certificate
provenance contains at least:

```text
object semantic revision
schema ID/version
authority mode
projection system ID/revision
diagram map ID/revision tuples
frame relation ID/revision tuples
frame ID/revision tuples
binding ID/revision tuples
projected geo ID/value/topology revision tuples
numeric policy ID/version
```

The publication protocol is:

1. invalidate currentness when an input revision changes;
2. snapshot resolved inputs at one DAG evaluation boundary;
3. reconstruct an immutable candidate;
4. validate the required projection-system context, convert diagram objects to
   intrinsic representations, and evaluate type predicates, correspondence,
   intrinsic reprojection and composed diagram reprojection;
5. verify that the snapshot is still current;
6. atomically publish valid payload plus all status axes, or publish an explicit
   failure and no current payload; and
7. notify derived geos/downstream algorithms once.

The previous valid payload may be retained only as labeled diagnostic history.
Any renderer or consumer asking for current geometry sees undefined until a
current valid payload exists.

Identity continuity is an explicit semantic decision, not Java reference
continuity. Ordinary recomputation preserves every durable ID and updates value
or definition revision evidence. A target-based compatible redefine may retain
identity only through one atomic provider/type/schema/role compatibility
transaction, even if the host changes the Java instance or rebuilds it from
XML. That transaction increments definition revision and increments topology
revision only when it declares a topology change. A true semantic replacement
or type-incompatible redefine receives fresh identity or fails explicitly;
delete/recreate and copy receive fresh identities, while undo/redo and reopen
restore serialized IDs. Labels and generic host replacement compatibility are
never the transfer predicate.

System/map/relation persistence uses the same two-stage protocol as other
spatial records: register identities first, then resolve references and build
normal-DAG dependencies. Copying a system remaps its complete selected closure.
A same-construction copied object may share an existing system/map only through
an explicit permitted external-reference rule; a cross-document operation must
include/remap the required system closure or fail.

## 6. State model

Do not use one mutable “valid” flag to encode all concerns.

| Record | Proposed domain | Consumer question |
|---|---|---|
| `CapabilitySupport` | supported, unsupported | Is this semantic type/schema/provider/frame/correspondence combination admitted? |
| `ProjectionSystemState` | not evaluated, consistent, inconsistent, degenerate, undefined | Are the required diagram maps and frame relations current and geometrically coherent? |
| `EditAuthorityMode` | projection-defined; later spatial-defined | Which side owns input edits now? |
| `DefinitionState` | defined, undefined, degenerate | Did the input construction yield admissible values? |
| `CertificateState` | not evaluated, valid, underdetermined, ambiguous, inconsistent projections, degenerate, undefined | Are these views canonically sufficient for this type? |
| `CurrentnessState` | current, invalidated, failed current revision | Does the evidence match current inputs? |
| `RepresentationFidelity` | exact, numerical, discrete | What kind of representation is this? |
| `NumericGuarantee` | not applicable, certified bound, estimated error, unresolved | What can the residual claim prove? |
| `CorrespondenceState` | established, ambiguous, broken, not required | Are endpoints/branches/parameters linked? |

Consumers must branch on the axes they need. Rendering needs current payload;
an inspector needs diagnostics even when no payload exists; downstream exact
algorithms may additionally require an accepted fidelity/guarantee.
An unsupported capability publishes `CapabilitySupport.UNSUPPORTED`,
`CertificateState.NOT_EVALUATED`, diagnostics and no current payload; it is not
silently collapsed into an undefined or degenerate construction.

For a valid system, a coherent common-diagram gauge `g` transforms every map
as `delta'=g o delta` and every bound diagram object as `p'=g(p)`. Because
`delta'^-1(p')=delta^-1(p)`, intrinsic sufficiency is gauge-invariant. A
viewport transform changes screen coordinates only and causes no semantic
revision or evaluation.

## 7. Frame and reprojection services

The semantic layer should compose existing matrix operations behind narrow
contracts:

- `ProjectionOperator.projectPoint(x)`;
- `ProjectionOperator.liftPoint(q)` returning a spatial line;
- `ProjectionOperator.projectDirection(v)`;
- `ProjectionOperator.liftLine(l)` returning a plane or explicit collapsed
  result;
- type-specific projectors for conics/curves when later approved; and
- `ReprojectionComparator<T>` returning normalized residual evidence.

The system layer additionally needs narrow contracts:

- `ProjectionDiagramMap.forward(q)` and `inverse(p)`;
- typed induced transforms for lines, endpoint tuples, vectors, conics and
  curves;
- `ProjectionFrameRelationEvaluator` for hinge and change-of-plane relations;
- `ProjectionSystemEvaluator` returning one immutable system certificate; and
- paired intrinsic/diagram reprojection evidence.

Each result identifies its frame revision and degeneracy. The service receives
world-coordinate numeric policy; it has no `EuclidianView`, renderer, screen,
or DPI dependency.

## 8. One-way 3D adapter

The first adapter translates a current semantic payload into an ordinary
compatible 3D kernel geo or updates an algorithm-owned one. It must:

- preserve `SpatialObjectId` association without using the visible geo label;
- become undefined or disappear when current payload is unavailable;
- never accept a 3D-view drag as an authority edit;
- keep style/view membership separate from geometric validity;
- expose reprojection diagnostics without feeding camera transforms back; and
- avoid a second independently serialized geometry value.

Existing `EuclidianView3D.newDrawable()` and view notification paths then render
the normal derived geo. Bidirectional 3D editing needs a later accepted
propagation and authority-transition policy.

## 9. Dynamic failure examples

### Point lifts become parallel

The object and binding IDs persist. The point candidate loses rank, certificate
becomes `UNDERDETERMINED` or `DEGENERATE` according to the type predicate, and
the derived point becomes undefined. If independence returns with consistent
topology, the same object identity can recover at a new revision.

### Defining projections disagree

Residuals fail after a full-rank candidate or lift lines are skew. Certificate
becomes `INCONSISTENT_PROJECTIONS`; implicated bindings and normalized
residuals are published; no old point remains current.

### Defining geo is deleted

Normal dependency removal invalidates the binding and object certificate.
No geo with a reused label is selected. Rebinding requires an explicit
transaction.

### Hinge or diagram map becomes inconsistent

System, map, relation, frame and binding IDs persist. The system certificate
publishes `INCONSISTENT` or `DEGENERATE`, and only object certificates that
reference that context lose their current payload. No visible-line match or
nearest-coordinate repair is attempted.

### Curve branch is created or lost

Object identity may persist, while correspondence/topology status changes.
New branches need provider-owned keys or explicit lineage. Coordinates and
list positions cannot establish continuation.

## 10. Scope map

| Gate | Semantic-model increment | Explicit exclusions |
|---|---|---|
| G9A1 | identity types including system/map/relation, registry, XML relation skeleton, closure remap | no map evaluation, reconstruction or spatial payload |
| G9A2 | frames, system/map/relation evaluation, roles, status axes, `p -> delta^-1 -> q`, projection-defined point, atomic publication, derived 3D adapter | no other primitive or spatial editing |
| G9A3 | binding/system-map/relation mutation and complete lifecycle/compatibility hardening | no inferred migration |
| G9B | individually approved primitive schemas; hard semantic dependency G9A3 only | no GUI dependency, composed boundary or solid |
| G9C | composed objects/projective boundary after primitive gates | no opaque CAD feature-tree authority |

After the approved global G9 gate, G9U2 consumes these system and relation
records for explicit procedures. It cannot infer them in a controller or view.

## 11. Design questions reserved for implementation review

- exact package names and whether semantic records are `GeoElement`s,
  algorithm-owned aggregates, or a narrow hybrid;
- exact opaque ID encoding and XML namespace;
- whether participating-geo IDs are emitted inline on elements or in one
  relation table while retaining two-stage resolution;
- how existing undo and copy serializers expose a complete transaction hook;
- exact initial encoding of the common diagram frame, map definition and hinge
  orientation, without weakening the isometry/unit policy;
- how a target-based compatible redefine exposes one atomic
  provider/type/schema/role predicate even when the host replaces a Java
  instance;
- how a derived 3D geo exposes its semantic association without changing
  unrelated geos; and
- which numeric linear algebra yields portable rank/conditioning evidence.

These choices may refine code placement but may not weaken the identities,
roles, intrinsic/diagram distinction, single-authority rule, normal DAG, or
no-stale-publication invariant. Ordinary recomputation preserves identity;
compatible target-based redefine may transfer it only through that explicit
transaction; true/incompatible replacement and delete/recreate use fresh IDs;
copy uses a fresh closure; undo/reopen restore serialized IDs. Labels or host
replacement compatibility alone never decide continuity.

# G9 spatial objects and canonical projection semantics

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR APPROVED** |
| Version | `1.0` |
| Design phase | G9P |
| Intended implementation gates | G9A1, G9A2, G9A3; later G9B and G9C; post-gate G9U2 consumer |
| Affected layer | Shared Java kernel/semantic model and construction XML |
| Product state | Normative design; productive G9 spatial implementation is not started and is not authorized |
| Accepted decisions | ADR 0010 and ADR 0011, both Accepted |

This document is the author-approved normative G9 spatial contract. It governs
future design-to-code work but does not itself authorize G9A1, G9A2, G9A3,
G9B, G9C, or G9U2 implementation. No class, XML element, command, migration, or
public behavior described here exists merely because the contract is approved.

G9P-R1 refined the original G9P candidate by separating intrinsic projection
coordinates from their geometric placement in a common CeDG diagram. It also
replaces the original blanket redefine rule with a source-backed continuity
taxonomy. The author approved those refinements at G9P closeout; the historical
initial candidate remains distinguishable from the final normative contract.

## 1. Objective and governing invariants

GeoCeDG needs a durable semantic identity for a spatial object and explicit,
typed relations between that identity and its two-dimensional projections.
The relation must participate in the normal construction dependency graph,
survive the supported document lifecycle, and state whether concrete
projections are sufficient, ambiguous, inconsistent, degenerate, or
undefined.

The design preserves these invariants:

1. A spatial object is not a collection of labels, layers, view memberships,
   screen coordinates, or coincident drawings.
2. A projection system is a first-class geometric relation among frames and
   their maps into one common CeDG construction diagram. It is not viewport or
   physical-sheet state.
3. A projection binding is a first-class typed relation with a stable identity,
   system map, frame, role, provenance, correspondence, and validity.
4. Exactly one edit authority applies to one spatial object at one construction
   revision. The first productive pilot is projection-defined.
5. Reconstruction, validation, and derived projections are normal DAG work.
   No listener-owned hidden dependency graph may become geometric authority.
6. A failed current revision never leaves a stale spatial payload presented as
   current geometry.
7. Sufficiency is type-specific and is evaluated in intrinsic frame
   coordinates after validating the diagram maps. A view count alone is never
   a proof.
8. Numerical work is world-coordinate, tolerance-controlled, deterministic,
   and reported separately from exact or symbolic evidence.
9. The 3D view consumes kernel-owned derived geometry. It is never an
   independently editable duplicate or reconstruction authority.
10. Legacy files remain unassociated until an explicit, versioned association
   or migration operation is performed.
11. Composite boundary semantics do not enter G9A; they remain G9C scope.

## 2. Mathematical model

### 2.1 Projection frame

A projection frame `i` is independent of viewport state and contains

\[
R_i=(o_i,u_i,v_i,n_i,d_i),
\]

where `o_i` is a point on the projection plane, `(u_i,v_i)` is an oriented
in-plane basis, `n_i` is its normal, and `d_i` is the projection direction.
For a parallel projection, `n_i dot d_i != 0`. Orthographic projection is the
declared special case `d_i` parallel to `n_i`. Handedness, units, exactness,
semantic version, and validity domain are part of the frame contract.

For a spatial point `x`, let

\[
y_i(x)=x+\frac{n_i\cdot(o_i-x)}{n_i\cdot d_i}d_i,
\qquad
\pi_i(x)=
\begin{bmatrix}
u_i\cdot(y_i(x)-o_i)\\
v_i\cdot(y_i(x)-o_i)
\end{bmatrix}.
\]

The lift of projected coordinate `q=(q_u,q_v)` is the line

\[
\ell_i(q)=o_i+q_u u_i+q_v v_i+\lambda d_i.
\]

Viewport zoom, DPI, camera position, physical drawing-sheet placement, and
pixel scale are absent from these definitions. A semantic map into the common
CeDG construction diagram is defined separately below; it is not any of those
presentation transforms.

### 2.2 Projection system and CeDG diagram map

Let the intrinsic embedding of the frame plane be

\[
\iota_i(q)=o_i+B_iq,\qquad B_i=[u_i\ v_i].
\]

A `ProjectionSystem` is a durable, construction-owned relation among frame
identities and their geometric placements in one common two-dimensional CeDG
diagram coordinate frame. A first-class `ProjectionDiagramMap` `m` associates
one frame use with the system and defines

\[
\delta_m(q)=A_mq+b_m,
\qquad \det A_m\ne 0.
\]

Equivalently, in homogeneous coordinates,

\[
\bar p=D_m\bar q,
\qquad
D_m=\begin{bmatrix}A_m&b_m\\0&1\end{bmatrix}.
\]

When one map `m_i` is selected for frame `i`, write
`delta_i := delta_{m_i}`. The map identity remains explicit because one frame
may participate in more than one system or diagram placement.

The initial admitted orthographic CeDG family is a typed oriented Euclidean
isometry or declared unit similarity:

\[
A_m^TA_m=s_m^2I,
\qquad s_m>0.
\]

The determinant sign carries the declared orientation or reflection. General
affine or projective diagram maps are not admitted merely because the matrix
form can express them; each additional family requires a versioned semantic
contract and capability support.

For a projected point bound to map `m`, the ordinary 2D kernel object stores
common-diagram coordinates

\[
p_m=\delta_m(\pi_i(x)),
\]

and reconstruction first obtains intrinsic coordinates

\[
q_i=\delta_m^{-1}(p_m).
\]

The same distinction applies through typed induced maps for lines, endpoints,
vectors, conics, and parameterized curves. A binding never leaves the
coordinate space implicit.

For frame planes `P_i` and `P_j` with a declared line-of-ground/hinge

\[
H_{ij}=P_i\cap P_j,
\]

let `h_i=iota_i^{-1}(H_ij)` and `h_j=iota_j^{-1}(H_ij)`. A hinge-unfold relation
is consistent only when

\[
\delta_{m_i}(h_i)=\delta_{m_j}(h_j)
\]

as the same diagram line with the declared orientation and fold side. Parallel
frames may still be useful projection frames, but they cannot claim a
nonexistent hinge relation. Change-of-plane relations add an explicitly typed
auxiliary frame, parent map, hinge/support construction, orientation, and
provenance; they are never inferred from visible 2D placement.

One common diagram gauge transformation `g` changes every map and bound diagram
object coherently:

\[
\delta'_m=g\circ\delta_m,
\qquad p'_m=g(p_m),
\qquad {\delta'_m}^{-1}(p'_m)=\delta_m^{-1}(p_m).
\]

It therefore cannot change intrinsic sufficiency. A viewport transform is only
`screen = V(p)` and is not a system input, revision, or certificate dependency.

### 2.3 Canonical schema

For spatial type `T`, let `X_T` be its valid configuration space and let
`Pi={pi_i}` be the declared projection operators. Define

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

A concrete binding set is canonical only on a declared domain when all of the
following hold:

1. equality of the bound projection data implies equality in `X_T`, modulo
   only explicitly declared equivalences;
2. a constructive reconstruction exists and is represented in the normal DAG;
3. reconstruction followed by reprojection reproduces every defining binding
   under the declared exactness and tolerance policy;
4. type-specific correspondence, incidence, rank, orientation, domain, and
   non-degeneration predicates hold; and
5. all inputs and revisions used by the result are explicit.

The schema produces a `CanonicalProjectionCertificate`, never a bare Boolean.

For a valid projection system `S`, also define the typed diagram observation

\[
\Psi_{T,S}(x)=
\left(\delta_m^{(T)}(\pi_i(x))\right)_{m\in S}.
\]

Sufficiency remains a property of the intrinsic map `Phi`. For fixed valid
bijective diagram maps, `Psi` has exactly the same injectivity as `Phi` on the
declared domain. If a required system/map/relation is unsupported,
inconsistent, degenerate, or undefined, the object schema does not reinterpret
the visible diagram and publishes no spatial payload.

### 2.4 Residuals and tolerances

For reconstructed object `x_hat`, a binding residual has a type-specific
world-coordinate definition. A point binding may use

\[
r_i=\|\pi_i(x_{hat})-q_i\|,
\]

while line, conic, and curve bindings require normalized incidence or
parameter-correspondence residuals. The policy records absolute and relative
tolerances, normalization scale, arithmetic method, termination reason, and
guarantee. Screen distance is forbidden.

An estimated residual does not prove uniqueness or exhaustiveness. Rank and
conditioning diagnostics remain separate from residual magnitude.

For a common-diagram binding, evidence records both intrinsic reprojection
`qHat=pi_i(xHat)` against `delta_m^{-1}(p_m)` and composed diagram reprojection
`pHat=delta_m(qHat)` against `p_m`. A small diagram residual cannot compensate
for an invalid projection system.

## 3. Semantic records

Names are conceptual and may change after the implementation source review.

### 3.1 Durable identities

- `PersistentGeoId`: optional, document-scoped, serialized identity assigned
  to any ordinary kernel geo participating in a spatial relation;
- `SpatialObjectId`: stable identity of the spatial semantic object;
- `ProjectionFrameId`: stable identity of a geometric frame;
- `ProjectionSystemId`: stable identity of one multi-frame CeDG arrangement;
- `ProjectionDiagramMapId`: stable identity of one frame use in that system;
- `ProjectionFrameRelationId`: stable identity of one hinge or change-of-plane
  relation;
- `ProjectionBindingId`: stable identity of one typed relation; and
- `SchemaId` plus `schemaVersion`: identity of the type-specific sufficiency
  contract.

IDs are opaque and allocated once. They are not derived from coordinates,
labels, construction order, layers, types, or XML position. The current
`ConstructionElement.ceID` is a transient creation/sort value and is expressly
not reused as a durable identity. A construction-scoped registry enforces
uniqueness and resolves references.

### 3.2 `SpatialObject3D`

The proposed immutable current-revision record contains:

- `SpatialObjectId`, semantic version, and spatial type;
- construction-owned definition inputs and source revision;
- one `EditAuthorityMode` for the revision;
- schema selection and projection-set membership;
- exactness/numeric policy and declared validity domain;
- candidate spatial payload, when current and valid;
- independent state axes and structured diagnostics; and
- references to ordinary compatible 3D kernel geos used by the derived-view
  adapter, when present.

The identity survives a geometric invalid interval. The current valid payload
does not: failed evaluation publishes an explicit invalid current state and
must withdraw or undefine derived geometry.

### 3.3 `ProjectionFrame`

The frame record contains its stable ID, origin, oriented basis, normal,
projection direction/operator, units, handedness, semantic version, validity,
and construction dependencies. It may compose existing `CoordSys`,
`CoordMatrix4x4`, and `Coords` operations, but those classes alone do not
provide frame identity or canonical meaning.

### 3.4 `ProjectionSystem` and diagram-map records

The system record contains its stable ID, semantic version, common diagram
coordinate frame and units, map IDs, relation IDs, construction definition
inputs, current system revision, independent state and an immutable system
certificate. It is a construction-owned semantic aggregate, never an
`EuclidianView` setting.

Each diagram-map record contains its stable map/system/frame IDs, a frame-use
role `DEFINING` or `AUXILIARY`, optional diagram anchor, typed map definition,
orientation/fold-side/unit policy, defining relation IDs, exactness, validity,
provenance, dependencies, and map revision. Frame-use role is independent of
binding role: an auxiliary frame may carry a defining binding for one spatial
object.

Each frame-relation record contains its stable ID, source and destination map
IDs, typed hinge-unfold or change-of-plane definition, oriented hinge/support
inputs, declared fold choice, provenance, revision, and diagnostics. Map and
relation evaluation participates in the normal construction DAG.

### 3.5 `ProjectionBinding`

Each binding contains:

- stable binding, spatial-object, projection-system, diagram-map, frame, and
  projected-geo IDs;
- exactly one role: `DEFINING`, `DERIVED`, `AUXILIARY`, `ANALYSIS`, or
  `PRESENTATION`;
- representation type and expected spatial type/schema;
- constructive provenance and source revision;
- exact, numerical, or discrete representation evidence;
- branch, endpoint, orientation, parameter, or topology correspondence where
  required;
- current validity and diagnostics; and
- ownership rules for create, edit, replace, and delete.

The projected geos are expressed in the common diagram coordinates identified
by the map. Intrinsic coordinates are obtained only through the validated map
inverse. A CeDG diagram binding is not demoted to `PRESENTATION` merely because
its map determines a visible arrangement.

Only `DEFINING` bindings may drive projection-defined reconstruction.
`DERIVED` bindings are outputs. Auxiliary, analysis, and presentation bindings
can support procedures or display but never silently become defining.

### 3.6 `CanonicalProjectionCertificate`

The certificate is an immutable result for one object revision,
system/map/relation/frame/binding revision tuple, schema version, and numeric
policy. It records:

- sufficiency state and every evaluated predicate;
- reconstructed payload identity/revision when valid;
- equivalence or branch assumptions;
- rank, conditioning, residuals, and tolerance evidence where applicable;
- implicated bindings for inconsistency or degeneration;
- dependency and provenance keys; and
- deterministic termination diagnostics.

Persist the certificate inputs. Recompute the certificate after load; a cached
previous result may be diagnostic evidence but is not trusted authority.

## 4. Independent state axes

The implementation must not compress the following concerns into one enum:

| Axis | Minimum proposed values | Meaning |
|---|---|---|
| Capability support | `SUPPORTED`, `UNSUPPORTED` | Whether the semantic type, schema/provider version, projection-system/map/relation family, frame family and correspondence contract are implemented and admitted |
| Projection system | `NOT_EVALUATED`, `CONSISTENT`, `INCONSISTENT`, `DEGENERATE`, `UNDEFINED` | Whether required maps and frame relations form a valid current intrinsic-to-diagram context |
| Edit authority | `PROJECTION_DEFINED`, later `SPATIAL_DEFINED` | Which input side may change this object revision |
| Definition | `DEFINED`, `UNDEFINED`, `DEGENERATE` | Whether the source construction yields admissible data |
| Canonical certificate | `NOT_EVALUATED`, `VALID`, `UNDERDETERMINED`, `AMBIGUOUS`, `INCONSISTENT_PROJECTIONS`, `DEGENERATE`, `UNDEFINED` | Type-specific sufficiency result |
| Currentness | `CURRENT`, `INVALIDATED`, `FAILED_CURRENT_REVISION` | Whether evidence belongs to current inputs |
| Representation fidelity | `EXACT`, `NUMERICAL`, `DISCRETE` | How the representation was produced |
| Numeric guarantee | `NOT_APPLICABLE`, `CERTIFIED_BOUND`, `ESTIMATED_ERROR`, `UNRESOLVED` | Strength of numeric evidence |
| Topology/correspondence | `ESTABLISHED`, `AMBIGUOUS`, `BROKEN`, `NOT_REQUIRED` | Whether components and parameters correspond |

`UNSUPPORTED` is a capability result, not a synonym for undefined input. It
requires certificate `NOT_EVALUATED` and no current spatial payload. For an
admitted capability whose current inputs were evaluated, the certificate cannot
remain `NOT_EVALUATED`. `VALID` never means exact arithmetic. `NUMERICAL` never
means invalid. `UNDERDETERMINED` never means the source geo is undefined. These
axes are reported together.

System inconsistency is not `INCONSISTENT_PROJECTIONS`: the former means the
declared maps/hinges disagree, while the latter means observations disagree
after a valid system has converted them to intrinsic coordinates. A broken
unused auxiliary map need not invalidate an object that does not reference it;
each object certificate names its exact required system subcontext.

## 5. Role-gated edit authority

GeoCeDG adopts a hybrid architecture with one active authority per object and
revision:

### 5.1 Projection-defined mode — first implementation

1. User or construction algorithms edit `DEFINING` projected geos, frames, or
   their semantic projection-system maps/relations.
2. The normal DAG invalidates the current certificate.
3. The type schema reconstructs an immutable candidate.
4. The candidate is checked for sufficiency, degeneracy, correspondence, and
   reprojection consistency.
5. One atomic publication makes the new payload and certificate current, or
   publishes an explicit failure with no stale current payload.
6. Derived projections and the 3D-view adapter update from the published
   semantic object.

Direct editing of the derived 3D representation is rejected or routed to an
explicit mode-transition command; it never mutates the authority implicitly.

### 5.2 Spatial-defined mode — designed, deferred

A later gate may allow defining primitives/parameters on the spatial side and
generate `DERIVED` projections. This is not authorized in G9A2. It must reuse
the same identities, frames, binding roles, certificate axes, and DAG rules.

### 5.3 Authority transition

Changing authority is an explicit construction transaction that:

- verifies that the destination definition is sufficient;
- rewrites inputs and outputs in the DAG with cycle checks;
- reclassifies binding roles explicitly;
- creates a new semantic revision while retaining the object ID only when the
  transaction declares semantic continuity;
- invalidates derived caches and republishes after validation; and
- is undoable and serializable.

No heuristic based on the last-edited view is permitted. One revision cannot
be simultaneously projection-defined and spatial-defined.

## 6. Primitive canonical schemas

These are sufficiency requirements, not implementation algorithms. Detailed
derivations and validation cases are in
`docs/architecture/g9_projection_sufficiency_and_primitives.md`.

Every schema consumes intrinsic representations obtained from the required
validated diagram maps. Type-specific diagram transforms do not replace any
rank, correspondence, orientation, or non-degeneration predicate below.

### 6.1 Point

Two or more projected points in known frames define lift lines `ell_i(q_i)`.
The point is unique only when the combined linear system has rank three and
the lift lines meet under the declared exact/numeric policy. Rank deficiency
is `UNDERDETERMINED`; several admissible discrete solutions are `AMBIGUOUS`;
incompatible lift lines are `INCONSISTENT_PROJECTIONS`. A point on a projection
plane is valid; parallel or repeated frames may still be insufficient.

### 6.2 Line

A non-collapsed projected line lifts to a plane containing the spatial line.
Two independent lifting planes normally determine their intersection line.
A line parallel to a projection direction collapses to a point in that frame;
that binding can constrain position but not supply the missing direction.
Collapsed, coincident, parallel, and inconsistent lifting-plane cases require
explicit results. Orientation needs an oriented correspondence or defining
points; a visual line alone does not provide it.

### 6.3 Segment and ray

The supporting-line certificate is necessary but insufficient. A segment also
requires explicit corresponding endpoints or a common oriented parameter and
finite interval. A ray requires a corresponding origin and oriented direction.
Endpoint swaps, zero length, collapsed views, and contradictory ordering are
diagnosed. A viewport crop is never an endpoint.

### 6.4 Vector

A free vector has direction and magnitude but no spatial anchor; a bound
vector additionally has an origin relation. Projected vector components in
sufficient independent frames reconstruct the spatial vector. Zero vector and
orientation/magnitude ambiguity are explicit. A line binding cannot silently
stand in for a vector.

### 6.5 Plane

Canonical definitions use constructive primitives such as three non-collinear
points, two distinct incident lines, or a point and a nonzero normal. Projection
traces may be defining only under a versioned trace schema with known frames
and incidence. A shaded outline or apparent screen region is never sufficient.
Collinearity, coincident lines, zero normal, and projecting-plane degeneration
are explicit.

### 6.6 Circle

A spatial circle requires a support plane, center, positive radius, and
orientation when downstream semantics need it, or equivalent sufficient
constructive data such as three non-collinear spatial points. Projected
ellipses alone are not assumed sufficient: correspondence, support-plane
evidence, and type-specific injectivity must be proved. A collapsed edge-on
segment can be a valid derived projection but not the sole circle definition.
Zero radius, collinear defining points, and inconsistent conics are explicit.

### 6.7 Conic

A spatial conic is a planar typed conic with a support plane and an intrinsic
quadratic form, domain/branch information, and correspondence. Reconstruction
from projected conics requires known plane homographies or equivalent
constructive primitives and consistent classification. Rank-deficient forms,
type changes, missing hyperbola-branch correspondence, and projection collapse
are explicit. A silhouette does not establish spatial conic identity.

### 6.8 Spatial curve

A curve is a semantic map

\[
C:D\setminus E\rightarrow\mathbb{R}^3
\]

with stable branch/component keys, orientation, validity domain, and explicit
correspondence. Projected curves `c_i(t)=pi_i(C(t))` can define it only when a
common semantic parameter or an equivalent one-to-one correspondence is
declared. Locus V2 parameter evidence is admissible when it is genuinely
shared; sample indices are not. Per-parameter lift consistency, invalid gaps,
branch creation/loss, seams, self-intersections, and continuity claims are
reported without coordinate deduplication.

## 7. Composite boundary direction and scope boundary

A future `ProjectiveBoundaryObject3D` may compose vertices, spatial curves,
supporting surfaces, oriented faces and loops, incidence/adjacency, projection
bindings, and closure/manifold diagnostics. Its authority remains the CeDG
construction and bound projections, not an opaque CAD feature tree.

G9A1–G9A3 do not implement composite objects, solids, surfaces, B-Rep storage,
CSG, or surface–surface solving. G9B may promote approved primitive schemas.
G9C may propose composed boundary semantics only after the primitive and
lifecycle gates pass.

## 8. Persistence and lifecycle

The document stores stable IDs, semantic/schema versions, frame definitions,
projection systems, common diagram coordinate frames, diagram maps,
frame/hinge/change-of-plane relations, authority mode, binding roles,
projected-geo references, correspondence, provenance, numeric policy, and
certificate inputs. References use IDs, never labels. Loader resolution is
two-stage: register all identities, then resolve relations and build the normal
DAG. Unknown versions or broken references produce explicit
unsupported/undefined records; they do not guess by label or visible placement.

### 8.1 Exact lifecycle contract

| Event | Object/system/map/relation/frame/binding IDs | Participating geo IDs | Required result |
|---|---|---|---|
| Rename or relabel | Preserved | Preserved | Display text changes only; every system, map, relation and certificate dependency remains bound by ID. |
| Ordinary recomputation | Preserved | Preserved | Value/definition evidence receives the required revision update; geometric invalidity does not reassign identity. |
| Save then reopen | Restored from XML | Restored from XML | Registry and exact system/map/relation graph are rebuilt, references are resolved, certificates are recomputed, and cached/stale geometry is not trusted. |
| Undo/redo | Restored from the serialized transaction state | Restored from that state | The exact prior identity graph, projection system and authority revision return; no fresh IDs are allocated for restored objects. |
| Copy/paste or duplicate | Fresh IDs for the copied closure | Fresh IDs for copied geos | One deterministic remap table rewrites every copied system/map/relation/binding reference. A same-construction shared system is retained only through an explicit permitted external-reference rule; cross-document live references are forbidden. |
| Explicit target-based semantically compatible redefine | Preserved only when an atomic provider/type/schema/role compatibility transaction declares continuity, even if the Java instance or XML-reload instance changes | Preserved or remapped as declared by that transaction | Definition revision increments; topology revision increments only when the transaction explicitly declares a topology change. Registry, DAG, system maps, bindings, and undo evidence update atomically. Host compatibility or label equality alone is insufficient. |
| True semantic replacement or type-incompatible redefine | Fresh IDs, or the operation fails explicitly | Fresh IDs for admitted replacement geos | No silent binding/system transfer; old dependents invalidate under an explicit policy. |
| Delete | Retired/removed according to dependency policy | Removed | Dependent bindings and certificates invalidate through the DAG; no dangling relation is silently rebound. |
| Delete then recreate with the same label | New IDs | New ID | The recreated geo is a different object and receives no former association automatically. |
| Geometric invalidity or passage through degeneration | Preserved | Preserved | Current certificate changes state; derived geometry is undefined/withdrawn, and no stale spatial payload remains current. Recovery with the same topology may reuse identity. |
| Add/remove/re-role a projection binding | Object ID preserved; affected binding IDs follow explicit create/delete transaction | Existing geo IDs preserved | New object revision, cycle/sufficiency check, certificate invalidation, and deterministic recomputation. |
| Add/remove/re-role/change a system map or frame relation | System ID preserved only for an in-place system transaction; affected child IDs follow explicit create/delete rules | Existing referenced geo IDs preserved | New system revision; validate map invertibility, units, hinge consistency and cycles; invalidate only certificates that reference the changed context. |
| Legacy unassociated file load | No spatial IDs synthesized merely from labels | Geo IDs assigned only on explicit participation | Legacy geometry remains usable and unassociated; association/migration requires a user-visible operation. |
| ID collision on load/import | Never accepted silently | Never accepted silently | Native reopen fails with a structured diagnostic; import/paste may use an explicit whole-closure remap transaction. |

## 9. Compatibility and serialization boundary

- Existing GeoGebra XML remains readable; absence of GeoCeDG spatial records
  means “unassociated,” not malformed.
- An implementation may add optional persistent IDs to participating ordinary geos,
  system/map/relation identities, a construction-scoped registry, and a
  versioned GeoCeDG spatial section.
- The exact XML spelling remains a G9A1 design-to-code decision, but the fields
  and lifecycle above are required.
- Document IDs, labels, `ceID`, construction indices, view flags, and layers are
  not substitutes for per-object persistent identity.
- Copy/paste, macros, undo, target-based compatible redefine, true replacement,
  delete/recreate, and reload must be characterized before a spatial point is
  published.
- The existing plane-view XML relation that resolves a plane by label remains
  legacy view state; it must not be reused as the new semantic binding.
- All additions remain guarded by `cedg.spatial.semantics` or an approved
  equivalent. The GeoCeDG Classic diagnostic path must parse, preserve,
  recompute, save, and reopen supported native spatial records with the same
  GeoCeDG kernel semantics while creation UI remains disabled. It must never
  downgrade them to labels, coordinate lists, or unassociated approximations.
  An external upstream GeoGebra distribution that does not implement these
  persisted types is outside the compatibility guarantee; G9A3 must
  characterize that unsupported-open boundary without adding a silent lossy
  conversion.

## 10. Phased implementation gates

### G9A1 — durable identity and persistence substrate

Scope: optional participating-geo IDs, spatial/frame/binding ID types,
projection-system/map/relation ID types, construction registry, XML read/write
skeleton, two-stage resolution, copy-closure remapping,
undo/reopen/redefine/delete behavior, collision and unknown-version diagnostics.

Gate: identity/lifecycle tests pass with no reconstruction, spatial solver,
public command, UI, migration, or 3D editing.

### G9A2 — semantic core and projection-defined point pilot

Scope: frame and projection-system evaluation, diagram-map inverse/forward
evaluation, hinge/change-of-plane relation validation, binding roles, authority
mode, separate status axes, immutable certificate publication,
projection-defined point reconstruction from common-diagram bindings, intrinsic
and composed-diagram reprojection, normal-DAG invalidation, and one-way derived
3D adapter.

Gate: analytic point cases, dynamic degeneration/recovery, hard-zero forbidden
authority counters, serialization round trip, and deterministic functional
counters pass. No other primitive is promoted.

### G9A3 — lifecycle and migration hardening

Scope: add/remove/re-role frame and binding transactions; copy/paste closure;
add/remove/re-role system maps and frame relations; undo/redo; compatible
target-based redefine, true replacement and incompatible redefine; deletion;
collision handling; legacy unassociated documents; malformed and future-version
records; Classic load; and recovery after topology-preserving invalidity.

Gate: the complete lifecycle matrix passes and the author approves the stable
foundation for G9B. G9A3 does not infer associations or implement automatic
legacy migration.

### Later gates

G9B has the hard semantic dependency `G9A3` and may implement individually
approved canonical primitive schemas without depending on G9U1 or another GUI
client. G9C may address composed spatial objects and projective boundaries.
After the approved G9 global gate, G9U2 may consume projection systems, maps and
relations to construct explicit dihedral procedures; it cannot define those
  semantics in the GUI. None is authorized by approval of this specification.

## 11. Validation obligations

Minimum invariant families include:

- point: general, on projection plane, repeated/parallel frames, inconsistent,
  near-rank-deficient, dynamic singular passage;
- projection system: intrinsic-to-diagram round trip, two-frame hinge,
  defining and auxiliary maps, change of plane, common-gauge invariance,
  inconsistent hinge, noninvertible map, unit/orientation mismatch,
  save/reopen/copy/undo, and no viewport dependency;
- line: general, collapsed in each frame, coincident/parallel lift planes,
  orientation conflict;
- segment/ray/vector: endpoints/origin/orientation, zero-length/zero-vector,
  correspondence reversal;
- plane: three-point and incident-line definitions, general/projecting plane,
  collinear/coincident/zero-normal cases;
- circle/conic: general plane, edge-on collapse, degenerate/type-changing input,
  support-plane and correspondence failures;
- spatial curve: common parameter, missing/many-to-one correspondence, gaps,
  seams, repeated coordinates, branch creation/loss;
- lifecycle: every row of the exact table above;
- redefine: recomputation preserves identity; compatible target-based redefine
  transfers it atomically; true/incompatible replacement and delete/recreate do
  not; copy creates a fresh closure; undo/reopen restore serialized identity;
- compatibility: legacy unassociated construction, Classic load, unknown
  version, malformed/colliding IDs; and
- independence: zoom, DPI, labels, layers, visibility, toolbar, camera, and
  creation order have no effect on certificates.

Functional counters and benchmark requirements are specified in
`docs/validation/g9_spatial_validation_and_benchmark_plan.md`.
The R1 system seam must expose deterministic counters equivalent to
`projectionSystemEvaluations`, `diagramMapForwardEvaluations`,
`diagramMapInverseEvaluations`, `hingeConsistencyEvaluations`,
`projectionSystemCertificatePublications`, and
`projectionSystemCertificateRejections`. Viewport, screen, DPI, camera, and
label-fallback authority counters remain hard zero.

## 12. Forbidden implementation shortcuts

The productive phases must not:

- infer identity or correspondence from labels, proximity, layer, order, or
  matching screen coordinates;
- infer a projection system, line of ground, fold orientation, or auxiliary
  frame from visible 2D placement;
- reuse `ConstructionElement.ceID` as serialized identity;
- keep the old valid spatial payload active after current validation fails;
- edit a derived 3D object as though it were authoritative;
- maintain projection reconstruction in JavaScript listeners, GUI controllers,
  exporters, or a sidecar hidden graph;
- reuse `EuclidianViewForPlaneCompanion` camera-derived mirror/rotation or
  label-keyed plane-view XML as a semantic diagram map;
- regard two views as sufficient without a type-specific certificate;
- use render samples as curve correspondence or geometric authority;
- serialize only a cached certificate result without its inputs;
- silently remap native-file ID collisions; or
- expand G9A into surfaces, solids, generic CAD features, public commands, or
  automatic legacy migration.

## 13. Approved closeout decisions and implementation-owned details

G9P closeout approved:

1. role-gated hybrid authority with projection-defined mode first;
2. the projection-system/map/relation model, initial admitted diagram-map
   families, and common-diagram gauge equivalence;
3. the scope and encoding of optional `PersistentGeoId` on participating geos;
4. native-file collision failure versus any recoverable repair workflow;
5. the compatible-redefine/true-replacement identity transaction;
6. the XML extension namespace and future-version behavior;
7. the G9A1/A2/A3 split and independent stop gates; and
8. GeoCeDG Classic diagnostic preservation/recomputation of supported native
   objects with creation disabled, no semantic downgrade, and no guarantee for
   external upstream distributions that do not know the persisted types.

Exact XML names, Java/API names, admitted numeric thresholds, and migration UI
remain design-to-code choices owned by the relevant future phase and its author
gate. This specification is **NORMATIVE / AUTHOR APPROVED**; productive G9
spatial work remains not started and is not authorized.

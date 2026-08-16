# ADR 0011: G9 spatial persistence lifecycle and implementation gates

- Status: **Accepted**
- Decision phase: G9P closeout — author approved
- Date: 2026-08-16
- Scope: persistence, lifecycle, compatibility, and G9A subdivision
- Productive implementation: not authorized

## Context

Accepted ADR 0010 defines durable object/frame/binding identities, an explicit
`ProjectionSystem` with intrinsic-to-diagram maps and frame relations, and
role-gated edit authority. The highest-risk prerequisite is not point
reconstruction; it is whether that complete identity graph survives real
GeoGebra document operations without silently rebinding by label or visible
diagram placement.

Current construction persistence serializes the construction and reloads it
for document and undo workflows. Copy/paste serializes a selected dependency
closure and may rename labels to avoid collision. Redefinition may mutate the
old geo, soft-retarget its algorithm, install a new instance, or rebuild the
construction from rewritten XML. Those operations are reasonable for current
contracts but neither their labels nor their Java-instance outcomes
automatically preserve a new semantic identity graph.

R1 source characterization found an explicit old target at
`AlgebraProcessor.changeGeoElement(...)`, but same-label replacement also uses
`kernel.lookupLabel`, host type compatibility is mostly `GeoClass` equality,
and `Construction.replace(...)` has branch-dependent instance behavior.
`AlgoElement` soft compatibility is command/input-count compatibility, not a
CeDG semantic predicate. Construction Protocol changes the current step,
order, deletion, or caption; it does not supply another durable identity
mechanism. XML reopen and undo/redo rebuild serialized construction state, and
copy/duplicate routes through XML/label remapping. Therefore a source-backed
contract must classify the user's semantic intent before host replacement and
persist the result independently of object references, labels, indices, and XML
positions.

Implementing a point schema before specifying this substrate would make its
observed identity depend on accidental load/copy/redefine behavior. Implementing
all persistence, spatial semantics, primitive schemas, and migration in one
phase would make failure attribution and author review too broad.

## Decision

G9A is split into three separately invoked and separately
reviewed gates: G9A1, G9A2, and G9A3. Passing one gate does not automatically
authorize the next.

### G9A1 — durable identity and persistence substrate

G9A1 may implement only:

- opaque ID value types for participating geos, spatial objects, frames,
  projection systems, diagram maps, frame relations, and bindings;
- a construction-scoped registry with uniqueness and reference resolution;
- optional persistent identity XML on participating ordinary geos;
- a versioned GeoCeDG spatial-relation XML skeleton containing inert
  `ProjectionSystem`, `ProjectionDiagramMap`, and `ProjectionFrameRelation`
  records and a two-stage loader;
- deterministic whole-closure ID remapping for copy/paste and duplication;
- explicit diagnostics for missing, duplicate, malformed, and unsupported
  versions;
- an explicit old-target redefine context and atomic
  `RETAIN`/`FRESH`/`REJECT` decision across every host replacement branch;
- the initial semantically compatible redefine boundary: one explicitly
  targeted output with the same provider-declared family, schema/version,
  authority mode and stable output role, with no topology change;
- foundational transaction behavior for save/reopen, undo/redo, rename,
  redefine/replace, delete/recreate, copy lineage, and collision cases; and
- tests and instrumentation for that substrate.

G9A1 must not add spatial reconstruction, a spatial point solver, public
commands, GUI tools, automatic legacy association, direct 3D editing, surfaces,
solids, or composed objects.

The gate passes only if the complete identity graph round-trips deterministically
and the admitted foundational lifecycle rules below are executable without
label, coordinate, order, XML-position, output-index, or Java-reference
fallback. Multi-output/cardinality-changing redefine and hostile lifecycle
combinations remain G9A3 work.

### G9A2 — semantic core and projection-defined point pilot

After author approval of G9A1 evidence, G9A2 may add:

- semantic projection frames composed from upstream coordinate operations;
- evaluation of projection systems, typed diagram-map forward/inverse
  transforms, hinge/change-of-plane relations, and immutable system
  certificates;
- role-typed projection bindings;
- per-revision `PROJECTION_DEFINED` authority;
- independent status axes and immutable certificate records;
- one type-specific point reconstruction/reprojection schema;
- normal-DAG invalidation and atomic candidate publication; and
- a one-way adapter to ordinary derived 3D point/view behavior.

G9A2 must not add spatial-defined editing, other primitive schemas, automatic
migration, public product commands, or composite geometry. The point pilot
must prove general, inconsistent, rank-deficient, degenerate, dynamic recovery,
reload, undo, copy, and viewport-independence cases.

### G9A3 — lifecycle and migration hardening

After author approval of G9A2 evidence, G9A3 may harden:

- add/remove/re-role frame and binding transactions;
- dependency-closure copy and external-reference policy;
- repeated undo/redo and save/reopen cycles;
- compatible, incompatible, no-op, true-replacement, missing-context, and
  multi-output/cardinality-changing redefine, including provider-owned stable
  output-role mapping and rollback;
- projection-system/map/relation mutation, partial-copy, deletion, and
  reconfiguration;
- deletion, recreation with reused labels, and partial dependency deletion;
- collision, malformed input, future version, and interrupted resolution;
- legacy files that contain no associations; and
- GeoCeDG Classic diagnostic load/preserve/recompute/save/reopen behavior for
  supported native semantic records, with creation disabled and no lossy
  downgrade; plus explicit characterization of unsupported external-upstream
  open behavior.

“Migration” in this gate means deterministic compatibility handling and an
explicit user-directed association boundary. It does not authorize heuristics
that infer spatial identity from labels, drawings, layers, or proximity.

### Later gates

G9B may promote point plus individually reviewed line, segment, ray, vector,
plane, circle, conic, and spatial-curve schemas after G9A3 is accepted. G9C may
address composed projective boundary objects only after G9B. These later gates
are designed, not authorized.

G9O1 remains a recommended operational-first documentation/bundle gate, not a
hard semantic dependency of G9A1. The spatial-kernel dependency is
`G9A1 -> G9A2 -> G9A3 -> G9B -> G9C`; G9B has no dependency on G9U1. Frontend
and procedural gates consume the shared semantic layer rather than authorizing
it.

## Required persistence shape

The exact XML element names remain a G9A1 design-to-code detail. The persistent
information must nevertheless include:

```text
participating geo persistent ID
spatial object ID, type, semantic version, authority mode, schema version
projection frame ID, origin/basis/direction/handedness/units/version
projection system ID, semantic version, common DiagramCoordinateFrame2D,
  units, map/relation membership, definition inputs and system revision
projection diagram map ID, system/frame IDs, frame-use role, typed invertible
  map, orientation/fold-side/units/provenance/dependencies and map revision
projection frame relation ID, system/source/destination-map IDs, typed hinge or
  change-of-plane definition, support inputs/fold choice/state and revision
binding ID, object/system/diagram-map/frame/projected-geo IDs, role and correspondence
constructive provenance, validity domain and numeric policy
object/system/map/relation/frame/binding/geo certificate revisions and dependency references
```

Loading is two-stage:

1. parse and register all geo/object/frame/system/map/relation/binding IDs by
   kind while rejecting duplicate native-file identities;
2. resolve system membership, map/frame and relation/map endpoints, binding
   references and constructive inputs; validate kinds, versions, closure,
   roles, map families/units and cycles; then build normal-DAG dependencies and
   schedule system and object certificate recomputation.

A cached certificate output is not authoritative after load. Broken references
remain diagnosed; they are never repaired by matching labels.

## Required lifecycle rules

The redefine decision is made before replacement parsing from an explicit old
target and a provider predicate over semantic family, schema/version, authority
mode, binding role, stable output role, and admitted topology transition. Host
`GeoClass`/command compatibility and whether `set`, soft redefine, instance
replacement, or XML rebuild happens do not decide it.

| Class/event | Identity and revision rule | Binding and geometry rule |
|---|---|---|
| **A — ordinary recomputation** | Preserve every durable ID. Increment value/source revision only when current evidence changes; rename, protocol reordering and a proven semantic no-op do not increment definition/topology revision. | Relations stay bound; normal DAG invalidation/recomputation publishes the current certificate or explicit failure. |
| **B — explicit target-based semantically compatible redefine** | Preserve the target ID even if the Java/XML instance changes. Increment definition revision unless a provider proves no-op; increment topology revision only for an explicitly admitted topology change. | Atomically replace the registry current-instance mapping, rebuild normal DAG dependencies, retain ID-based bindings, invalidate/recompute certificates, and store undo evidence. G9A1 admits one stable targeted output role and a topology-preserving change only. |
| **C — true semantic replacement** | Retire the old ID and allocate a fresh one. | Invalidate old bindings; no automatic rebind. |
| **D — type/schema/role-incompatible redefine** | Reject atomically or, only under an explicitly selected replacement operation, use class C. | No silent transfer even if host editing accepts the type change. |
| **E — delete then recreate** | Delete retires IDs; recreation gets fresh IDs despite matching label/type/coordinates/order. | Normal DAG invalidation; no inherited association. Undo restoration is class G, not recreation. |
| **F — copy/paste/duplicate/macro invocation** | Allocate fresh IDs and copy-lineage provenance for the complete copied semantic closure. Macro invocations never reuse template IDs. | Rewrite every copied geo/object/frame/system/map/relation/binding reference through one table. Same-construction shared-system references require an explicit permitted external-reference rule; cross-document copy includes/remaps the system closure or fails. |
| **G — undo/redo or native save/reopen** | Restore exact serialized IDs and revision graph although Java instances and `ceID` values may be new. | Two-stage resolve and normal-DAG rebuild; recompute system/object certificates and never trust stale cached geometry. |
| Invalid/degenerate current revision | Preserve IDs. | Publish invalid current state and withdraw/undefine derived payload; never expose the previous payload as current. |
| Binding-set mutation | Preserve object/system and unaffected IDs; create/retire binding IDs explicitly; increment object revision. | Run cycle/sufficiency checks, then atomically publish or roll back. |
| Diagram-map/frame-relation mutation | Preserve system ID only for an explicit in-place transaction; affected child IDs follow create/retire rules; increment child and system revisions. | Validate map invertibility/units/orientation, hinge or change-of-plane consistency and cycles; invalidate only certificates that reference the changed revision tuple. |
| Legacy unassociated load | Do not synthesize spatial identity from existing labels or diagram layout. | Preserve ordinary construction; association is explicit and versioned. |
| Native-file ID collision | Reject with structured diagnostic. | Do not silently remap; an import/paste transaction may remap the whole closure explicitly. |

## Host boundary established by R1 source characterization

The current host offers no persistent per-geo identity and no semantic
continuity predicate:

- `ConstructionElement.ceID` is constructor-allocated and used for ordering;
  equality is Java reference equality, and ordinary geo XML emits type/label but
  no such durable ID.
- `AlgebraProcessor.processReplace(...)` may preserve an instance through
  `GeoElement.set(...)`, whereas `Construction.replace(...)` may preserve it,
  install a new one, or reconstruct from XML. Those outcomes are incidental.
- `AlgoElement.isCompatible(...)` checks command kind and input count;
  `AlgebraProcessor.compatibleTypes(...)` checks host classes. Neither checks a
  spatial provider/schema/role/topology contract.
- `Construction.prepareReplace(...)` can remove sibling outputs, so output
  ordinal and label cannot map multi-output identity safely.
- `AlgoElement` and its inherited `ceID` remain replaceable scheduler state,
  not a durable semantic result. Participating geos and explicit spatial/system
  records own IDs; additional algorithm outputs require provider-stable roles.
- Construction Protocol exposes navigation/order/deletion/caption editing, not
  an independent semantic-ID edit. The subsequent definition-edit route must
  carry the explicit target context.
- save/reopen and undo/redo reload XML snapshots; copy/duplicate also serializes
  and remaps labels. New Java instances are therefore compatible with restored
  identity, while same Java instances do not prove continuity.

G9A1 must introduce only the target context, narrow compatibility SPI, registry
switch, XML restoration and single-output transaction needed to make class B
explicit. G9A3 owns multi-output/provider-role mapping and hostile combinations.
If the target context is missing, the safe outcome is `FRESH` or `REJECT`, never
label-derived `RETAIN`.

## Compatibility policy

1. Absence of a GeoCeDG spatial section is valid and means unassociated.
2. Feature creation remains behind `cedg.spatial.semantics` or an accepted
   equivalent throughout G9A.
3. Old `.ggb` files do not silently gain relations or change results.
4. GeoCeDG writes semantic versions and preserves unknown records only if a
   reviewed safe round-trip rule exists; otherwise it fails explicitly rather
   than degrading to labels.
5. The GeoCeDG Classic diagnostic path must parse, preserve, recompute, save,
   and reopen supported associated files with native IDs/relations and creation
   UI disabled. An external upstream distribution that does not know the
   persisted types is outside the guarantee; unsupported-open behavior is
   characterized explicitly and never hidden through lossy conversion.
6. Plane-view settings, labels, layers, view membership, and the GGB document
   ID remain distinct legacy contracts. A `ProjectionDiagramMap` is geometric
   construction state and must not serialize any viewport transform.

## Why the phase order is strict

Durable identity and XML are shared foundations for both public Locus V2 and
spatial objects. G9A1 must therefore settle the registry and lifecycle seams
before spatial objects or other new persistent semantic geos proliferate.
G9A2 then proves the smallest analytic spatial schema. G9A3 subjects that pilot
to hostile document lifecycle and compatibility scenarios before G9B adds
more types.

This order reduces parallel changes to `GeoElement`, `Construction`, XML,
copy/paste, and redefinition. It also gives each failure one likely layer:
identity/persistence, point semantics, or lifecycle hardening.

Operationally running G9O1 first is recommended because its deterministic
bundles improve later agent work, but failure or delay there does not make the
G9A1 identity contract semantically undefined. Likewise, productizing the
frontend in G9U1 cannot gate the G9B kernel schemas.

## Rejected alternatives

### One undivided G9A implementation

Rejected because it combines shared persistence, geometric reconstruction,
view adaptation, and migration, preventing narrow validation and review.

### Implement point reconstruction before durable IDs

Rejected because save/reopen and copy evidence would then validate a temporary
identity contract that must be replaced.

### Sidecar-only or label-keyed persistence

Rejected because it breaks document portability and cannot reliably follow
undo, copy, redefine, or label reuse.

### Blanket `redefine -> new identity` or `same instance -> same identity`

Rejected because current redefine routes have different Java-instance outcomes
for reasons unrelated to user semantic intent. Only the explicit target and a
provider compatibility transaction may retain identity; true replacement,
incompatibility, delete/recreate, and copy remain fresh-ID operations.

### Automatic legacy association

Rejected because a visible resemblance or conventional label is not a
type-specific sufficiency proof and can create false spatial identity.

### Silently repair native-file collisions

Rejected because remapping part of an identity graph can redirect relations.
Only an explicit whole-closure import/paste transaction may remap.

### Cache the last valid geometry through invalid input

Rejected because it presents stale spatial truth. Historical evidence may be
retained for diagnostics but not published as current geometry.

## Acceptance record and stop conditions

G9P closeout approved the phase split, A–G lifecycle taxonomy,
projection-system/map/relation persistence shape, two-stage registry load,
collision policy, explicit target-based compatibility transaction,
no-automatic-migration rule, and GeoCeDG Classic diagnostic policy.

Each phase stops and returns to author review when:

- an ID cannot be made deterministic across its promised lifecycle;
- shared XML compatibility would require label inference;
- copy/paste cannot remap the dependency closure atomically;
- target-based compatible redefine cannot distinguish continuity from true
  replacement without label, coordinate, order, XML-position, output-index, or
  Java-instance inference;
- diagram placement needs viewport or screen state;
- a failed certificate would expose stale geometry;
- Classic compatibility requires an unapproved behavior change; or
- implementation pressure expands the phase into later primitives or composed
  objects.

This Accepted ADR does not authorize any code or serialization change. G9A1,
G9A2, and G9A3 remain designed and not authorized.

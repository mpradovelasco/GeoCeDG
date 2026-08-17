# G9 spatial persistence and upstream impact

**Status:** author-approved G9 architecture and source-impact analysis; G9A1 `PASS — AUTHOR APPROVED`

**Baseline:** `9b93256b7df401ff056c37b502d82df4d72b1522`

**G9A1 entry baseline:** `001f7920a1154b09a22b54c190f7bc5f94b48e90`

**Implementation state:** the G9A1 durable-identity/persistence substrate is
implemented and author-approved; no spatial solving, projection
evaluation, certificate, payload, public command, GUI, 3D authority, migration,
G9A2, or later-G9 implementation is present

## 1. Purpose

This document maps the normative identity/binding contract to the pinned source
and defines the smallest coherent upstream impact. Its approved design analysis
remains normative. The G9A1 implementation notes below record the concrete names
and seams selected by the separately authorized implementation; the G9A1
closeout does not authorize G9A2.

The fundamental insertion is a shared-kernel semantic and persistence layer.
Frontend, JavaScript, exporters, 3D renderer, labels, and generated sidecars
remain consumers or evidence, never the identity authority.

## 2. Current identity and lifecycle evidence

### 2.1 Construction identity is not durable object identity

`source/shared/common/src/main/java/org/geogebra/common/kernel/algos/ConstructionElement.java`
owns a private `ceID`, allocated from the application in the constructor and
used by `compareTo()` for creation ordering. It also owns a mutable construction
index. Neither value is emitted as a stable semantic geo ID in ordinary
construction XML. G9 must not reinterpret either value.

`GeoElement` identity in current commands and XML is commonly expressed by its
label. Labels are intentionally user-editable and participate in collision
handling. They cannot survive the G9 lifecycle contract as semantic identity.

`MyXMLio` can write an `id` in the GGB document header. That value belongs to
the document, not to every `GeoElement`, frame, binding, or spatial object.

### 2.2 Source-backed redefine and replacement behavior

On the pinned baseline, `redefine` is not one host operation and Java reference
identity is not a stable proxy for constructive continuity:

| Source seam | Observed host behavior | G9 consequence |
|---|---|---|
| `AlgebraProcessor.changeGeoElement(...)` (lines 465–474 and callback at 619–664) | The edit path starts with an explicit old `GeoElement` target, evaluates a replacement, emits `REDEFINE`, and stores undo. | This old-object target is the narrowest available carrier of user continuity intent. G9 must capture its durable ID before evaluation; the emitted event and returned instance do not decide continuity. |
| `AlgebraProcessor.getReplaceable(...)` / `processReplace(...)` (2121–2253) | Same-label input finds an object through `kernel.lookupLabel`; compatible independent values may use `set(...)`, otherwise control reaches `Construction.replace(...)`. | A same-label assignment without an explicit target transaction is only a namespace collision/replacement request. Label equality cannot transfer identity. |
| `AlgebraProcessor.compatibleTypes(...)` (2293–2310) | Compatibility is mostly `GeoClass` equality, plus numeric/angle and list/vector exceptions. | This is a host editing rule, not a CeDG semantic-family/schema/role compatibility predicate. |
| `Construction.replace(...)` (1464–1614) | Depending on the case, it mutates the old geo, retargets the old parent, removes the old geo and installs a new instance, or rewrites construction XML and reloads it. | All branches need one explicit G9 identity decision. Neither “same Java object” nor “new Java object” settles durable identity. |
| `Construction.softRedefine(...)` and `AlgoElement.setFrom(...)` / `isCompatible(...)` (1623–1629; 211–238 and 1842–1846) | Soft redefine accepts the same command enum and input count and may copy input values into existing inputs. | It is an implementation optimization. It does not prove semantic continuity, output-role continuity, or topology compatibility. |
| `Construction.doReplaceInXML(...)` (1725–1820) | The replacement is positioned and referenced through labels, and a temporary result may receive the old label before rebuild. | Existing XML rewrite preserves current label contracts only. A G9 transaction must carry IDs independently through the rebuild. |

`Construction.prepareReplace(...)` (2431–2467) can remove sibling outputs and
label new siblings. Therefore index, construction position, label, and output
ordinal cannot safely map durable IDs for a multi-output algorithm. The initial
compatible-redefine boundary is one explicitly targeted output with the same
provider-declared semantic family, schema and stable output role. Multi-output
or cardinality-changing transfer remains unsupported until G9A3 introduces a
provider-owned output-role mapping and rollback tests.

The current Construction Protocol is not a separate semantic editing engine.
`ConstructionProtocolView` exposes construction-step selection and only the
caption cell as editable (430–438, 638–643, 719–722);
`ConstructionProtocolViewD` moves the current step, reorders it, or deletes it
(436–442, 536–540, 583–591, 1040–1046). A subsequent definition edit still
travels through an algebra/input redefine route. G9 must test that route
end-to-end and must not treat the displayed protocol row or construction index
as continuity evidence.

The existing `RedefineTest` confirms the branch distinction: its
`simpleRedefinitionsShouldBeSoft` case preserves a `Line` instance only for
selected same-command shapes and observes a different instance for another
valid line definition; other cases cover type changes, sibling outputs,
construction order and listeners. The focused baseline run executed all 55
tests with zero failures. This proves current host behavior only; it does not
prove the proposed durable identity transaction.

The `REDEFINE` event and existing object-listener preservation tests prove event
and callback compatibility, not semantic identity: the event targets whichever
geo is current after replacement. A G9 binding follows its durable ID and
revision tuple, then recomputes through normal DAG dependencies; it never treats
listener survival as an identity-transfer oracle.

### 2.3 Recompute, remove, copy, and Java identity

Normal `AlgoElement.update()` and cascade processing recompute the same
algorithm/output instances. Type-specific `GeoElement.set(...)` methods copy
state into an existing target; `GeoElement.copy()` / `copyInternal()` create a
new target. Those mechanisms explain host instance behavior but do not allocate
or transfer a durable semantic ID.

An `AlgoElement` instance and its inherited `ceID` remain replaceable scheduler/
implementation state, not the identity of the constructive result. Durable IDs
belong to participating geos and explicit spatial/system records. A compatible
definition edit may replace the parent algorithm while the targeted output ID
continues; any additional output continuity requires provider-declared stable
roles, not algorithm instance or output order.

`GeoElement.moveDependencies(...)` (6048–6058) is a no-op by default; its
point/numeric/boolean/text overrides move narrow ancillary relationships such as
locateables, view/min-max listeners, conditional visibility, or dynamic captions.
It is not a general construction-graph or future spatial-binding rewire. G9 must
update its registry mapping atomically and let recreated `AlgoElement`
dependencies drive semantic invalidation; it must not expand those ad hoc
overrides into a second graph.

`GeoElement.doRenameLabel(...)` mutates the label lookup table around the same
instance. Rename therefore preserves durable identity. `GeoElement.remove()`
and `AlgoElement.remove()` remove normal DAG links and outputs; delete followed
by a new same-label construction is a new identity. Undo of that deletion is a
different operation and restores the earlier serialized identity.

`InternalClipboard` and desktop `CopyPasteD` implement duplication through
construction XML plus label remapping; `CmdCopyFreeObject` creates an ordinary
copy. None carries semantic lineage today. G9 copy must allocate a fresh
identity closure and explicit copy provenance.

### 2.4 XML and undo

Construction state is written in `geogebra.xml` by shared XML infrastructure
and is parsed back through `MyXMLio`, `MyXMLHandler`, and 3D companions. Undo
uses serialized construction state/reload behavior. Therefore durable G9 IDs
must be in the construction serialization authority and restored from the
snapshot; allocating new IDs during undo reload would break identity.

`MyXMLio.doParseXML(...)` clears the construction for a normal reload;
`ConsElementXMLHandler.getGeoElement(...)` then creates or looks up geos by
loaded label. `ConstructionElement.ceID` is freshly allocated in its constructor
and equality remains Java reference equality. `DefaultUndoManager` and
`UndoManagerD` reload serialized snapshots; even the shared `UndoRedoTester`
re-looks up results by label after undo/redo. Consequently, reopen and undo/redo
may reconstruct every Java object while still being required to restore the
same future G9 durable IDs.

The G9 loader needs a relation-resolution phase because a binding, projection
system, diagram map, or frame relation may be read before one of its referenced
records in XML order. It must first register IDs and then resolve relations and
build dependencies.

### 2.5 Copy/paste, duplication, and macro boundaries

Copy/paste routes selected construction XML through application copy services
and can rename labels to avoid collisions. The new contract requires a
transaction-local `old ID -> fresh ID` table for the entire copied dependency
closure. Every internal object/frame/system/diagram-map/frame-relation/binding/
geo reference is rewritten through that table before publication.

References from the copied closure to uncopied source objects require an
explicit policy. The safe initial rule is to reject an incomplete semantic
closure or preserve a declared external reference only when source and
destination are the same construction and the binding type permits it. Silent
cross-document references are prohibited.

Macro/tool serialization must be characterized separately. A macro definition
is not a live document object. Each macro invocation must allocate a fresh
identity closure for outputs and internal semantic records; template IDs cannot
be reused across invocations.

### 2.6 Rename and plane-view evidence

Rename handlers update the label contract and label lookup tables. G9
relations must not need a rename callback beyond display/diagnostic text.

`EuclidianViewForPlaneCompanion` currently writes a plane name in a
`<viewId plane="...">` setting, and `MyXMLHandler3D`/`App3DCompanion` resolve
that name by label. This is existing plane-view UI state, not precedent for a
semantic projection binding. It may remain compatible while the new G9 record
uses persistent IDs.

## 3. Serialized information model

The broad G9 model below remains the normative target. G9A1 implements only its
inert identity/reference skeleton: an optional `geocedgId` attribute on a
participating element plus a flat, versioned `geocedgSpatial` sibling section.
It does not persist or evaluate the future geometric definitions and current
certificate data described later in this section.

```xml
<element type="point" label="A" geocedgId="geo:...">
  <!-- existing geo data -->
</element>

<geocedgSpatial version="1">
  <frame id="..." kind="orthographic" version="1" .../>
  <system id="..." version="1" revision="..."
          maps="map:..." relations="relation:..." .../>
  <diagramMap id="..." system="..." frame="..." role="defining"
              family="orientedIsometry" revision="..." .../>
  <frameRelation id="..." system="..." sourceMap="..."
                 destinationMap="..." kind="hingeUnfold"
                 revision="..." .../>
  <object id="..." type="point" authority="projectionDefined"
          schema="cedg.point.orthographic" schemaVersion="1" .../>
  <binding id="..." object="..." system="..." map="..."
           frame="..." role="defining" projectedGeos="..."
           correspondence="point" .../>
</geocedgSpatial>
```

The model must store, independent of exact element layout:

- stable participating-geo, object, frame, projection-system, diagram-map,
  frame-relation, and binding IDs plus semantic/schema versions;
- object type, authority mode, and construction definition inputs;
- full geometric frame definition or stable construction inputs that
  deterministically recompute it;
- the system's common `DiagramCoordinateFrame2D`, units, membership, system
  revision, and definition inputs;
- for each `ProjectionDiagramMap`, its system/frame IDs, independent frame-use
  role, typed invertible map definition, orientation/fold-side/unit policy,
  provenance, dependencies, and map revision;
- for each `ProjectionFrameRelation`, its system/source/destination map IDs,
  typed `HINGE_UNFOLD` or `CHANGE_OF_PLANE` definition, oriented hinge/support
  inputs, fold choice, provenance, state, and relation revision;
- binding role, projected-geo IDs, representation type, and correspondence;
- provenance, validity domain, representation fidelity, and numeric policy;
- object/system/map/relation/frame/binding/geo revision inputs needed to
  recompute certificates; and
- compatibility/feature version.

Do not persist only a last reconstructed coordinate or certificate state. Such
values may be retained as nonauthoritative diagnostic cache only if they are
versioned and discarded on mismatch.

## 4. Two-stage load protocol

G9A1 implements parse/register and typed-reference resolution for inert records
only. The scheduling, projection-system evaluation, certificate and payload
steps below remain G9A2-or-later normative design and are not part of the
approved G9A1 implementation.

### Stage 1 — parse and register

1. Parse IDs without resolving label aliases.
2. Validate syntax, namespace/version, and per-kind uniqueness.
3. Register participating geo, frame, projection-system, diagram-map,
   frame-relation, object, and binding stubs by kind in the current construction.
4. Record unresolved stable-ID references.
5. Reject duplicate native-file IDs with a structured diagnostic.

### Stage 2 — resolve and schedule

1. Resolve every reference through the construction registry.
2. Resolve system membership, diagram-map frame uses, frame-relation endpoints,
   object/binding references, and all constructive input IDs by kind.
3. Validate schema/type compatibility, independent frame-use and binding roles,
   map family/invertibility/units, hinge/change-of-plane consistency, and closure.
4. Build normal `AlgoElement` dependencies with cycle checks.
5. For an unadmitted type, schema/provider version, frame/map/relation family or
   correspondence contract, publish capability `UNSUPPORTED`, certificate
   `NOT_EVALUATED` and no payload. For a missing or broken stable-ID reference,
   publish the applicable broken lifecycle/definition state (`UNDEFINED`) and
   no payload. Neither case may search by label, position, coordinate, layer, or
   visual coincidence.
6. Schedule deterministic projection-system and object recomputation after all
   inputs are available.
7. Atomically publish current system/object certificates and payload, or their
   current failure states.

Loading must not briefly expose a stale serialized payload as current geometry.

## 5. Lifecycle transaction design

The registry transaction must classify intent before replacement parsing. A
conceptual `RedefinitionIdentityContext`, captured from the explicit old target
in `changeGeoElement(...)` and propagated through evaluation and every
`Construction.replace(...)` branch, carries the old durable ID and one
provider decision: `RETAIN`, `FRESH`, or `REJECT`. The exact productive name is
not fixed here. No context means that label equality cannot request `RETAIN`.

Provider compatibility must include semantic family, schema/version, binding
role, stable output role, authority mode and admitted topology transition. Host
`GeoClass` compatibility, command equality, coordinates, labels, construction
indices, XML positions, and current Java instance are insufficient.

### 5.1 Redefine and identity taxonomy

| Class | Identity and revision rule | Binding/DAG/undo rule |
|---|---|---|
| **A — ordinary recomputation**: parameter value, upstream value, dynamic reevaluation | Preserve geo/object/system/map/relation/binding IDs. Increment the affected value/source revision only when current evidence changes; a pure rename, order move, or semantic no-op changes no geometric definition/topology revision. | Keep relations; invalidate and recompute affected certificates through the normal DAG. Invalid geometry preserves identity but withdraws current payload. |
| **B — explicit target-based semantically compatible redefine** | Preserve the target durable ID even when host execution creates a new Java instance or rebuilds XML. Increment definition revision unless the provider proves a semantic no-op. Increment topology revision only for an explicitly admitted topology change. | Atomically replace the registry current-instance pointer, re-establish normal DAG dependencies, retain ID-based bindings, invalidate/recompute certificates, and snapshot the transaction. Initial G9A1 support is one targeted stable output role and a topology-preserving change only. |
| **C — true semantic replacement** | Retire the old identity and allocate a fresh identity. | Old bindings invalidate; no automatic rebind. A separately explicit user association may bind the new object later. |
| **D — type/schema/role-incompatible redefine** | Reject without mutation, or perform class C under an explicitly chosen replacement operation. Never transfer identity because the host accepts a type change. | No silent binding/system transfer; rollback is atomic. |
| **E — delete then recreate** | Delete retires the ID; recreation gets a fresh ID even with the same label, coordinates, type, or construction position. | Dependents invalidate normally. An undo snapshot restoration is class G, not recreation. |
| **F — copy, duplicate, or macro invocation** | Allocate fresh IDs for the whole copied semantic closure and record explicit copy lineage/provenance. A macro invocation never reuses template IDs. | Rewrite all internal references through one transaction-local map. Same-construction reuse of a shared projection system/map is allowed only as a declared external dependency; cross-document copy includes/remaps its complete closure or fails. |
| **G — undo/redo and native save/reopen** | Restore the exact serialized IDs and revision graph, although all Java instances and `ceID` values may be new. | Two-stage resolve, rebuild the normal DAG, and recompute. Never infer continuity from labels after reload. |

### 5.2 Projection-system lifecycle

| Operation | Registry/revision transaction | Failure behavior |
|---|---|---|
| Assign first participating identity | Allocate an opaque kind-correct unique ID and register. Emit on the next save/undo snapshot. | Collision retries only before publication; never coordinate-derived. |
| Add/remove/re-role a binding | Preserve object/system and unaffected IDs; create or retire the affected binding ID; increment object revision. | Cycle/sufficiency failure rolls back or publishes the approved explicit invalid revision; it never guesses another map. |
| Add/remove/re-role/change a diagram map or frame relation | Preserve the system ID only for an explicit in-place system transaction; create/retire child IDs explicitly; increment map/relation and system revisions. | Validate map invertibility, units, fold choice, hinge consistency, and cycles; invalidate only certificates whose revision tuple references the changed context. |
| Replace the meaning of a projection system | Allocate a new system/child identity closure. | Bindings to the former system invalidate; visible diagram placement never transfers them. |
| Native ID collision | Reject the relation/document load according to the approved atomic policy. | Only explicit import/paste may remap a complete closure. |

## 6. Minimal source impact by phase

Exact class/package choices require the G9A source review. The likely impact
zones and allowed purpose are:

### G9A1

| Source zone | Minimal purpose | Forbidden expansion |
|---|---|---|
| `kernel/geos/GeoElement` or a narrow companion | optional participating-geo ID access/XML hook | no ID on every object unless evidence requires it; no geometry change |
| `kernel/Construction` | own kind-aware registry and atomic identity transaction lifecycle | no parallel dependency scheduler |
| `io/MyXMLio`, `io/MyXMLHandler`, `io/ConsElementXMLHandler`, 3D XML companion as required | versioned parse/write and two-stage resolution for geo/object/frame/system/map/relation/binding stubs | no semantic projection evaluation; no label fallback; no inferred migration |
| copy/paste and macro serialization seams | full-closure remap/instantiation, including projection-system records and copy provenance | no global label rewrite workaround |
| `AlgebraProcessor.changeGeoElement`, `EvalInfo` or a narrower context, and every `Construction.replace` branch | capture explicit target intent; decide and commit `RETAIN`/`FRESH`/`REJECT` for the admitted single-output, topology-preserving compatibility boundary | no identity decision from `set`, soft-redefine success, label, `GeoClass`, command equality, construction index, or Java reference |
| GeoCeDG-owned shared package | opaque ID types, registry/stub records for `ProjectionSystem`, `ProjectionDiagramMap`, and `ProjectionFrameRelation`, diagnostics and compatibility SPI | no reconstruction, diagram-map, or hinge solver |

G9A1 validates the persistence substrate with inert or test-private semantic
stubs. It has no hard semantic dependency on G9O1 bundle generation; G9O1 is a
recommended operational prerequisite only.

The approved G9A1 implementation realizes this boundary in the GeoCeDG-owned
`org.geocedg.common.kernel.spatial.identity` package and the minimum host seams
for construction ownership, optional geo attachment/XML, staged host parsing,
shared/desktop clipboard remap, snapshot undo/delete, macro instantiation, and
explicit-target redefine routing. `Kernel` participates only to terminate an
expected rejected semantic macro invocation without publishing its output or
use registration. `ParametricProcessor` carries the same explicit redefine
context as the general algebra route. These are lifecycle integrations, not
projection or reconstruction behavior.

### G9A2

| Source zone | Minimal purpose | Forbidden expansion |
|---|---|---|
| GeoCeDG-owned shared spatial package | evaluate `ProjectionSystem`, diagram-map forward/inverse transforms and frame relations; publish immutable system certificate; frames, roles, schemas, point certificate/algorithm | point only; no procedural workspace automation |
| `kernel/matrix` consumers | compose existing projection algebra | do not change general matrix semantics without need |
| normal algorithm dispatch/factory internal seam | create test/internal pilot | no public command/UI |
| `geogebra3D/kernel3D` adapter seam | derived `GeoPoint3D`-compatible representation | no independent duplicate or direct 3D editing |
| feature configuration | gate creation/use | no default enablement |

### G9A3

Harden only the same lifecycle seams with explicit binding/system/map/relation
transactions; provider-owned multi-output role mapping; repeated reload and
undo; copy subsets and external references; compatible/incompatible/true
replacement; deletion/recreation; rollback; legacy/future/malformed input;
collision behavior; and Classic compatibility. Avoid new primitive families.

After G9A3, the spatial-kernel track may enter G9B without G9U1. G9B owns
per-primitive sufficiency over the already authoritative system context; G9U1
is a frontend/productization consumer, not a semantic dependency. Future G9U2
procedures may consume projection-system records for change of plane, rotation,
folding and true magnitude, but do not own those records.

## 7. Compatibility matrix

| Document / operation | Proposed behavior |
|---|---|
| Legacy `.ggb` without spatial section | Load unchanged and unassociated. Do not scan labels for pairs. |
| GeoCeDG spatial file in supported version | Restore the exact geo/object/frame/system/map/relation/binding graph, rebuild DAG, recompute system and object certificates. |
| Supported file with undefined current geometry | Preserve identity and explicit status; no stale derived object. |
| File with unknown future spatial version | Explicit unsupported diagnostic; preserve only under a reviewed safe round-trip mechanism. |
| File with missing reference | Explicit broken/undefined relation; no label repair. |
| Native reopen with duplicate IDs | Reject spatial relation load or document load according to approved atomic policy; never silently remap. |
| Explicit import/paste collision | Remap entire imported closure through one transaction. |
| Same-label expression without explicit old-target identity context | Treat as namespace replacement/collision; never transfer durable identity from the label. |
| Explicit target-based compatible redefine in the admitted boundary | Preserve the target ID through all host replacement branches and recompute ID-bound dependents. |
| GeoCeDG Classic diagnostic path | Parse/preserve/recompute/save/reopen supported native spatial records with exact IDs/relations and creation disabled; never reinterpret or downgrade. External upstream distributions that do not know the persisted types are outside the guarantee and receive characterized unsupported-open behavior. |
| Feature flag disabled | Existing associated data must not be silently destroyed; creation and editing behavior need an explicit compatibility policy. |

## 8. Upstream-risk register

| Risk | Why material | Required containment |
|---|---|---|
| XML order and forward references | relation may precede its geo | two-stage resolver; deterministic diagnostics |
| System/map/relation partial resolution | a binding can name a valid frame but an unresolved or inconsistent diagram context | kind-aware stage-two closure validation; system certificate precedes object publication |
| Copy closure incomplete | relation can point back to source | explicit closure/external-reference policy and atomic remap |
| Redefinition route varies | `set`, soft redefine, new instance, and XML rebuild expose different Java identity | one pre-parse target context and one atomic `RETAIN`/`FRESH`/`REJECT` decision across every branch |
| Redefinition by label | accidental identity transfer | label-only requests cannot select `RETAIN`; dedicated target-based semantic transaction required |
| Host compatibility overclaims continuity | `GeoClass` or command/input-count equality omits schema, role and topology | provider-declared semantic compatibility and stable output-role mapping |
| Multi-output replacement | sibling removal/cardinality change makes index mapping ambiguous | reject in G9A1; require provider output roles and rollback hardening in G9A3 |
| Undo reload allocates fresh IDs | downstream identity breaks | restore serialized IDs and exact graph |
| Macro template reuses IDs | collisions across invocations | per-invocation fresh closure |
| Registry becomes hidden graph | duplicate invalidation/ordering | registry only resolves; `AlgoElement` remains scheduler |
| View or renderer feeds projection | viewport-dependent semantics | hard-zero view/screen read instrumentation |
| Cached candidate published after newer edit | stale geometry | revision tuple check plus atomic publication |
| GeoCeDG Classic drops or downgrades native data | compatibility loss | native preservation/recompute corpus plus save/reopen equality; external upstream unsupported-open corpus with no lossy repair |
| Broad `GeoElement` churn | upstream sync burden | optional ID and narrow hooks; no unrelated refactor |

## 9. Verification seam requirements

G9A tests need observable counters at the semantic boundary, including:

- ID allocation, restore, remap, collision, and label-fallback counts by geo,
  object, frame, system, diagram-map, frame-relation, and binding kind;
- registry entries and unresolved references by kind;
- compatible-redefine retain/no-op/reject, true-replacement fresh-ID,
  missing-target-context, multi-output mapping rejection, definition/topology
  revision, and transaction rollback counts;
- label-, coordinate-, construction-order-, XML-position-, output-index-, and
  Java-reference-based identity-transfer attempt counts;
- DAG recomputation and candidate publication/rejection counts;
- projection-system, diagram-map forward/inverse, and hinge/change-of-plane
  consistency evaluations plus system-certificate publication/rejection counts;
- stale-candidate and stale-payload publication counts;
- projection/reconstruction/reprojection evaluations;
- renderer, viewport, DPI, screen, and camera authority reads; and
- copy/redefine/delete transaction commit/rollback counts.

Forbidden-authority and stale-publication counters are hard zero. Performance
budgets use deterministic work counts; wall-clock measurements are diagnostic
only.

## 10. Stop conditions

Stop the productive phase and return to author review if:

- an implementation needs a label or screen-state fallback;
- a native reload cannot distinguish collision from intentional import;
- undo cannot restore the exact identity graph;
- copy/paste cannot establish a complete remap closure;
- a compatible target-based redefine can preserve continuity only by label,
  coordinate, construction index, XML position, output ordinal, or Java instance;
- a label-only redefine silently transfers identity;
- a diagram map depends on viewport/screen placement or cannot name its common
  diagram coordinate frame;
- the semantic registry would own an update graph separate from `AlgoElement`;
- current upstream XML cannot be extended compatibly without a migration
  policy; or
- minimal changes expand into public commands, GUI workspaces, other primitive
  schemas, surfaces, or solids.

G9P records these seams; it does not modify them.

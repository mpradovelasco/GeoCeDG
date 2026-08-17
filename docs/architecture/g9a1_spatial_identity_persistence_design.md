# G9A1 spatial identity and persistence design

Status: **IMPLEMENTED — PASS — AUTHOR APPROVED**

Authority: `geocedg/specs/spatial/g9-spatial-projection-semantics.md`, ADRs
0010/0011, and the G9A1 canonical task prompt. This record fixes the
design-to-code choices owned by G9A1. It does not approve G9A2 or any later
phase.

## Scope boundary

G9A1 adds a shared, construction-scoped identity and persistence substrate.
It stores opaque typed IDs, inert semantic records, typed references, roles,
provider/schema metadata, revisions, provenance, copy lineage, and structured
diagnostics. `AlgoElement` remains the only construction update scheduler.

The phase does not evaluate a frame or diagram map, reconstruct or reproject
geometry, publish a certificate or spatial payload, read a view, create a
public command, add UI, infer a legacy association, or alter Locus/DXF/3D
authority.

## Productive model

`SpatialIdentityRegistry` is owned by one `Construction`. It has distinct ID
types for participating geos, spatial objects, frames, projection systems,
diagram maps, frame relations, and bindings. Every ID contains a kind prefix
and a 128-bit opaque token. A construction-wide raw-token index rejects both
same-kind duplicates and cross-kind token reuse. Allocation uses an injectable
shared-Java token source, is independent of kernel random state, and retries a
bounded number of collisions before failing without publication.

Ordinary geos acquire no ID merely by being created or loaded. A
`PersistentGeoId` is assigned only by an explicit participation transaction.
`ConstructionElement.ceID`, labels, coordinates, construction/XML order,
layers, output ordinals, and Java instances are never identity sources.

The version-one inert records are:

| XML record | Required identity/reference skeleton |
|---|---|
| `geo` | geo ID; provider, family, schema/version, authority, binding role, stable output role and cardinality; definition/topology revision; optional copy source |
| `object` | object ID; semantic type/version, authority, schema/version, definition geo IDs and revisions |
| `frame` | frame ID; semantic version, definition geo IDs and revision |
| `system` | system ID; semantic version, map/relation IDs, definition geo IDs and revision |
| `diagramMap` | map/system/frame IDs; frame-use role, family, defining relation/geo IDs and revision |
| `frameRelation` | relation/system/source-map/destination-map IDs; relation kind, definition geo IDs and revision |
| `binding` | binding/object/system/map/frame/projected-geo IDs; binding role, representation/expected type, schema/version and revision |

These records deliberately contain no transform, geometric result, cached
certificate, or hidden dependency graph in A1.

## XML contract

Participating element tags carry optional `geocedgId="geo:<token>"`. The
attribute is emitted only while a full construction or explicit semantic
clipboard fragment is being serialized. It is not emitted by `getStyleXML()`
or generic style/copy operations.

The construction contains one flat, versioned sibling section:

```xml
<geocedgSpatial version="1">
  <geo id="geo:..." ... />
  <frame id="frame:..." ... />
  <system id="system:..." maps="map:..." relations="relation:..." ... />
  <diagramMap id="map:..." system="system:..." frame="frame:..." ... />
  <frameRelation id="relation:..." system="system:..." ... />
  <object id="object:..." ... />
  <binding id="binding:..." object="object:..." system="system:..." ... />
</geocedgSpatial>
```

Lists use ID values, never labels. Writers sort records by kind and ID and sort
set-like references, so a fixed registry state has deterministic XML.

Loading is a two-stage atomic session. Stage one collects element attachments,
records, IDs, versions, and unresolved references in any XML order. Stage two
validates global uniqueness, kind correctness, required fields, and closure,
then publishes the complete graph. Malformed IDs, duplicate/native collisions,
and unsupported structure publish no partial graph. The host XML entry point
restores its complete pre-parse construction snapshot when a structured
spatial-identity rejection occurs, including after an earlier host element was
parsed. A well-formed missing reference remains an explicit `BROKEN` record and
diagnostic; it is never repaired by label. A future section or record version
is rejected atomically with an explicit unsupported diagnostic. Opaque
future-section preservation requires the separately reviewed safe path
deferred to lifecycle hardening; A1 neither activates nor silently downgrades
it.

Every parse has an explicit purpose:

- `NATIVE_OR_UNDO_RESTORE`: restore exact serialized IDs;
- `CLIPBOARD_IMPORT`: require a complete declared closure and remap it once;
- `REDEFINE_REBUILD`: restore unaffected IDs and the approved target decision;
- `ROLLBACK_RESTORE`: restore the prior snapshot without replaying a decision;
- `GENERIC_MERGE`: reject identity-bearing input without an import plan.

Legacy XML without the section or attributes loads unchanged and allocates
nothing. Disabled creation UI does not discard supported native records.

## Lifecycle transactions

Copy expands the selected semantic component before ordinary construction-DAG
predecessors. Shared and desktop clipboard payloads use the same registry
closure exporter. Each paste validates the complete closure, allocates one
fresh typed-ID map, rewrites every internal reference atomically, and records
immediate copy lineage. Desktop fast paste cannot use a reduced payload for a
participating selection. Generic `evalXML` is not an import authority.

Participating add/paste/delete operations use full XML undo checkpoints until
an action format can carry a complete identity closure and explicit restore
purpose. Undo/redo and reopen therefore restore exact IDs and relations.

Deletion retires the geo ID and every inert record transitively dependent on
it; replacement suppression keeps the old mapping alive until its explicit
transaction commits. Clearing live construction state retains a bounded
Construction-instance ledger of every issued token; an exact native, undo,
redefine, or rollback restore may reactivate the same typed ID, while ordinary
allocation and registration cannot reuse it. Recreating the same label never
restores a retired ID. Rename, recomputation, temporary undefinedness,
visibility and ordering changes do not affect identity.

Macro templates never publish their template IDs into an invocation. Once the
host macro-to-instance geo map exists, the registry instantiates a freshly
remapped complete semantic closure for that invocation. An incomplete template
closure fails rather than referring back to the template construction.

## Explicit redefine boundary

`changeGeoElement` captures an immutable context from the explicit old target
before parsing. `EvalInfo` transports it through every host route. Before
`extendMinMax`, `set`, style copy, sibling removal, or `prepareReplace`, the
registered provider inspects the actual result and returns `RETAIN`, `FRESH`,
or `REJECT`.

Initial `RETAIN` requires one targeted output, one provider-owned stable output
role, identical provider/family/schema/version/authority/binding-role contract,
unchanged cardinality, and a topology-preserving proposal. Host `GeoClass`,
command equality, label equality, successful soft redefine, or instance reuse
cannot prove compatibility. Missing context/provider, sibling or multi-output
ambiguity, incompatible signatures, and topology change reject before mutation.
Composite vector/rigid-polygon host operations that would rewrite more than one
target reject before helper creation if any rewritten target participates; A1
does not invent multi-target continuity semantics.

The transaction covers in-place value assignment, independent `set`, same-
definition update, soft redefine, no-child instance replacement, circular
in-place update, collected redefine, and XML rebuild. A serialization overlay
lets a rebuild candidate emit the decided ID without publishing a duplicate.
The rebuilt result is resolved by durable ID, not label. Failure uses a
context-free rollback snapshot. `FRESH` is admitted only when the explicit
old-target operation also declares true-replacement intent. It retires the old
closure and allocates a new ID; it never transfers bindings. A provider cannot
turn an ordinary compatible-edit request into an implicit replacement merely
by returning `FRESH`.

## Evidence and stop conditions

Deterministic counters cover allocation, restore, remap, collision, unresolved
references, copy/delete/redefine commit and rollback, definition/topology
revision, and every kind. Forbidden label/coordinate/order/XML-position/output-
ordinal/viewport/DPI/camera identity-authority counters remain hard zero.

Focused tests cover legacy load, flat forward references, malformed/duplicate/
cross-kind/future data, missing-reference state, deterministic XML, rename,
recompute, reopen, undo/redo, repeated copy/paste, macro invocation, delete and
recreate, and compatible/fresh/rejected redefine. The unchanged upstream
`RedefineTest` remains a required regression gate. Any need for label fallback,
partial copy remap, inferred legacy association, unanchored continuity, or a
second scheduler is a phase stop rather than permission to broaden G9A1.

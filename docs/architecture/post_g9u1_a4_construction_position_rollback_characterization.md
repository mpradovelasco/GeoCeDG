# POST-G9U1-A4 — construction-position and rollback characterization

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `c0a018ab01c0b44a7ed1fb33010bf2481e30ac2a`
- Base tree: `4ab3a420c96a79ca0a88d16c597845293f453a00`
- Task kind: `RESEARCH_THEN_DESIGN`
- Product phase effect: **NONE**
- Product implementation authorized: **false**
- Guide impact: **NO**

This document characterizes the current GeoCeDG/GeoGebra host so that a later
POST-G9U1-A3 design can distinguish compatible V2 redefine from true
replacement. It neither implements nor authorizes A3.

## 1. Sources and evidence boundary

The characterization was made against the exact base above using:

- `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java`;
- `source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java` and
  `MyXMLHandler.java`;
- `source/shared/common/src/main/java/org/geogebra/common/gui/view/consprotocol/ConstructionProtocolView.java`;
- the current undo manager and G9A3 redefine tests/support;
- `ConstructionGeoRedefineProvider`, `SpatialRedefineTransaction` and the
  spatial identity registry;
- `docs/architecture/g9a3_spatial_lifecycle_migration_design.md`;
- the accepted G9 spatial specification and lifecycle ADRs; and
- the pinned upstream baseline
  `9b93256b7df401ff056c37b502d82df4d72b1522`.

`ConstructionProtocolView` and its ordering logic are unchanged from that
pinned upstream baseline. GeoCeDG extends `Construction` and XML loading for
durable spatial identity and atomic redefine rollback; those extensions do not
turn Construction Protocol state into kernel authority.

Evidence labels in this document mean:

- **OBSERVED**: directly established by current source or the focused
  characterization test;
- **NORMATIVE**: required by an accepted specification/ADR;
- **INFERENCE**: a consequence of the observed and normative evidence; and
- **RECOMMENDATION**: an A3 design input pending author review.

## 2. Independent state axes

| Axis | Characterization |
|---|---|
| A. Durable semantic identity | **NORMATIVE:** typed, document-scoped identity held by the spatial registry; never a label, index or Java reference. |
| B. Dependency/DAG topology | **OBSERVED:** algorithm inputs/outputs and update dependencies in the live construction. |
| C. Topological evaluation order | **OBSERVED:** `algoList` drives construction update. It is host-maintained, distinct from `ceList`, and is not an independently serialized semantic identity. |
| D. Construction-list membership/order | **OBSERVED:** `ceList` is the ordered host construction and the source of construction-element serialization order. |
| E. Construction index/position | **OBSERVED:** a mutable zero-based number recomputed from `ceList`; insertion, removal and legal movement renumber elements. |
| F. Serialized XML order | **OBSERVED:** construction XML is emitted by iterating current `ceList` order and reconstructs that order on native load/rollback. |
| G. Protocol displayed position/order | **OBSERVED:** rows are sorted from `GeoElement.getConstructionIndex()`, but filtering, unlabeled/auxiliary objects, breakpoints, CAS twins and multiple rows sharing an index mean protocol row/step is not identical to the raw index. |
| H. Protocol current navigation step | **OBSERVED:** transient view/navigation state acting through `Construction.setStep`; it is loaded only conditionally from `consStep` when protocol navigation is shown. The redefine rollback snapshot does not include this protocol extra state. |
| I. Java object-instance identity | **OBSERVED:** some in-place/soft edits retain an instance, while XML rebuild, rollback and reopen reconstruct instances. **NORMATIVE:** never continuity authority. |
| J. Undo/redo snapshot state | **OBSERVED:** string XML snapshots reconstruct construction and durable identity state. Restoration need not retain Java instances or the previous protocol navigation step. |

`ceList` order and DAG topology are related but not identical. The host permits
movement only inside `getMinConstructionIndex()` / `getMaxConstructionIndex()`
bounds, so every legal list order is topologically valid; more than one valid
order can exist for independent elements.

## 3. Operation characterization

### 3.1 Identity, DAG and construction order

| Operation | Durable identity | DAG dependencies / evaluation | Construction membership and order/index | Serialized state / restoration |
|---|---|---|---|---|
| Ordinary recomputation | Preserved | Same dependency graph; values/revisions recompute | Same membership and relative order; numeric indices can shift only because of a separate construction mutation | Next save serializes current state |
| Rename | Preserved | Unchanged | Same element and order; label changes only | New label is serialized; identity remains typed and separate |
| Compatible redefine candidate | Not yet a public V2 contract; G9A3 provider can decide `RETAIN` | Candidate dependencies are explicit and validated | Candidate construction may already occupy another legal slot before publication | Candidate is not authority until atomic publication |
| Incompatible replacement | Fresh identity when admitted, otherwise reject | New dependency graph or explicit invalidation policy | No right to inherit the old slot; host order follows the admitted replacement/rebuild | Fresh replacement state if committed |
| Failed redefine before publication | Entry identity graph remains authoritative | Entry DAG remains authoritative | No candidate becomes authoritative; if host snapshot restoration runs, exact entry order returns | Entry undo XML is the rollback authority |
| Failed redefine after candidate construction, before commit | No partial retained/fresh publication | Exact entry dependencies reconstructed | Exact entry `ceList`/XML order reconstructed | Operation-local XML snapshot restored atomically |
| Successful atomic redefine | `RETAIN` or `FRESH` exactly as provider transaction declares | Candidate DAG and revisions publish atomically | Exact old numeric slot is not guaranteed. The current new-instance/no-child path can move the retained object after an independent neighbor | Committed construction is serialized in its resulting `ceList` order |
| Undo after redefine | Prior serialized identities restored | Prior serialized DAG restored | Prior serialized membership/order restored | Snapshot reconstruction |
| Redo after redefine | Redo serialized identities restored | Redo serialized DAG restored | Redo serialized membership/order restored | Snapshot reconstruction |
| Save/reopen | Serialized durable identities restored | Serialized DAG reconstructed | Serialized XML/`ceList` order reconstructed | Full document XML is authority |
| Copy/remap | Fresh copied closure IDs with explicit lineage/remap | Copied dependencies remap to the copied closure or declared external references | Destination insertion/order is a new construction fact; source numeric slot is not continuity | Copy plan/remap, not source XML position, is authority |
| Delete + recreate | New identity | New graph membership | New construction element at a currently valid insertion position | Old identity is retired; no coordinate/label recovery |

### 3.2 Protocol, Java instances and rollback

| Operation | Protocol displayed position | Protocol navigation step | Java instance | Rollback/restoration meaning |
|---|---|---|---|---|
| Ordinary recomputation | Re-derived from the unchanged construction order and current filters | May remain as UI state; not geometric authority | Usually same | No rollback boundary |
| Rename | Row content refreshes; order is unchanged | Presentation state only | Same | No identity transaction |
| Compatible redefine candidate | Not authoritative before commit | Must not drive redefine | May be a new candidate instance | Candidate disposal or snapshot restore on failure |
| Incompatible replacement | Derived from committed new construction | Presentation state only | Normally new | Reject leaves entry state; admitted replacement has no continuity claim |
| Failed before publication | Entry presentation can be rebuilt | Not a rollback postcondition | May change if snapshot reconstruction occurred | Durable/XML equality, not reference equality |
| Failed after candidate construction | Rows re-derived from restored order | Current implementation does not restore the earlier step; focused evidence observes final construction step | Reconstructed instances replace all entry instances | Exact durable graph, DAG and construction XML/order; no partial publication |
| Successful atomic redefine | Consequence of resulting kernel order | Not part of semantic commit | Same for in-place/soft branches; new for rebuild/new-instance branches | No rollback after operation completion without normal undo |
| Undo / redo | Re-derived from restored construction | Exact independent protocol-navigation restoration is **NOT ESTABLISHED** and is not semantic authority | Reconstructed | Restore respective serialized construction snapshots |
| Save/reopen | Re-derived from serialized construction; protocol filters/settings may affect visibility | `consStep` restoration is conditional on saved GUI/navigation state | Reconstructed | Reopen is reconstruction, not instance continuity |
| Copy/remap | Derived in destination | Destination UI state | New | Fresh closure, not rollback |
| Delete + recreate | Derived from new element/order | UI state | New | Undo may restore the deleted serialized snapshot; recreation itself is distinct |

The focused evidence deliberately uses existing lifecycle seams. It does not
manufacture state by reflection and changes no production behavior.

## 4. Meaning of construction position

### 4.1 Candidate interpretations

| Candidate | Finding |
|---|---|
| P1 — exact list/index preservation | **REJECTED.** The numeric index is mutable and can legitimately change when unrelated elements are inserted or removed. Requiring the same integer would confuse an ephemeral coordinate of `ceList` with identity. |
| P2 — topological-equivalence preservation | **INSUFFICIENT.** It protects dependency correctness but allows a compatible redefine to cross surviving independent neighbors, changing the explicit procedural narrative even when no semantic need requires it. |
| P3 — stable procedural position stronger than topology | **SELECTED DESIGN CANDIDATE.** Preserve relative placement among surviving unaffected construction elements, subject to all predecessor/dependent constraints. |
| P4 — current transaction already supplies the invariant | **REJECTED.** Focused evidence demonstrates an admitted `RETAIN` new-instance redefine that moves from before to after an independent neighbor. |

```text
A4_POSITION_CONTRACT = P3
```

P3 does not require the same numeric index. It means a reconstructible
procedural position expressed by stable anchors/relative order among surviving
unaffected construction elements. A future A3 design must define the bounded
anchor capture and validation rule, including what happens when anchors are
removed or a new dependency makes the former interval illegal. Dependency
correctness always wins; incompatibility with the preserved procedural interval
must reject or classify the edit as replacement rather than silently move it.

This stronger procedural invariant follows CeDG constructive traceability, not
Construction Protocol UI state. The protocol merely renders the resulting
kernel order.

## 5. Rollback characterization

```text
ROLLBACK_IDENTITY_GRAPH = exact durable snapshot reconstruction
ROLLBACK_CONSTRUCTION_DAG = exact serialized dependency reconstruction
ROLLBACK_CONSTRUCTION_ORDER = exact ceList/XML order reconstruction
ROLLBACK_SERIALIZED_STATE = exact operation-entry construction undo XML
ROLLBACK_JAVA_INSTANCE_STABILITY = NOT PRESERVED
ROLLBACK_PROTOCOL_PRESENTATION = rows re-derived; navigation step excluded
```

**OBSERVED:** `Construction` captures `getCurrentUndoXML(false)` before the
participating mutation. On failure it reloads that snapshot under the spatial
rollback load purpose. Registry epochs and publication leases prevent an older
rollback capability from overwriting newer graph publication.

**INFERENCE:** exact equality is defined over reconstructible state: durable
records, dependencies, construction membership/order and serialized
construction content. It is not `oldJavaObject == restoredJavaObject`.

**NORMATIVE:** failed redefine must leave no partially published identities,
half-rewired dependencies, stale A1/A2/V2 downstream currentness or UI state
masquerading as semantics.

## 6. Construction Protocol boundary

```text
PROTOCOL_AUTHORITY_FINDING = PRESENTATION_ONLY
```

- Protocol rows and their displayed indices are derived from construction
  indices plus presentation filters/grouping rules.
- Auxiliary visibility, breakpoints, hidden/unlabeled objects and several rows
  with one construction index can change the presentation without changing the
  DAG or durable identity.
- Navigation uses the construction step to reveal a partial construction but is
  transient UI control. It must never perform or authorize redefine/rollback.
- Full file persistence may carry `consStep` when the GUI/navigation bar emits
  it. The operation-local redefine snapshot does not establish it as a rollback
  postcondition.
- A3 must not implement “step backward, redefine, step forward”.

## 7. Normative handoff candidate for A3

```text
A3_MUST_PRESERVE = [
  compatible durable semantic identity and exact stable-role mapping,
  declared dependency/DAG relations and valid topological constraints,
  correct revision evolution and downstream currentness/revalidation semantics,
  A1/A2/V2 durable bindings, with current bindings revalidated rather than
    blindly preserved,
  relative procedural position among surviving unaffected construction anchors
]

A3_MAY_CHANGE = [
  Java object and algorithm instances,
  numeric construction indices shifted by legitimate insert/remove operations,
  runtime caches and host evaluation-list implementation details,
  Protocol row numbers/filtering and current navigation step
]

A3_MUST_NOT_USE_AS_IDENTITY = [
  label, coordinates, proximity, construction index, XML position,
  Protocol row/order/navigation step, Java reference, render or layer state
]

A3_ROLLBACK_POSTCONDITIONS = [
  exact entry durable identity graph,
  exact entry dependency graph,
  exact entry construction membership and relative/XML order,
  no partial publication or stale A1/A2/V2 current binding,
  reconstructed instances permitted,
  Protocol navigation excluded
]

A3_CONSTRUCTION_POSITION_PREDICATE =
  compatible redefine preserves the target group's relative interval between
  surviving unaffected construction anchors while satisfying every predecessor
  and dependent bound; if no legal preserved interval exists, continuity is not
  silently claimed

A3_PROTOCOL_PRESENTATION_CONSEQUENCE =
  displayed rows/order are re-derived from the committed or restored kernel
  construction; navigation state neither participates in nor authorizes it

A3_NEW_KERNEL_MECHANISM_REQUIRED = true
```

The required mechanism is bounded: capture/validate/restore stable procedural
anchors for the successful compatible-redefine path. Existing XML snapshot
rollback already restores failed operations and must be reused, not redesigned.
The exact A3 compatibility predicate and anchor API remain future A3 work.

## 8. Unresolved facts and limits

- The precise anchor representation for multi-output V2 construction groups is
  intentionally left to A3; A4 establishes the invariant, not its API.
- Behavior when both adjacent independent anchors are deleted by the same future
  admitted transaction needs an explicit A3 compatibility rule.
- Exact restoration of Construction Protocol navigation under every desktop/web
  undo configuration is **UNKNOWN / NOT ESTABLISHED**. It is presentation-only
  and therefore not required to decide A3 semantic compatibility.
- The precise stability of `algoList` ordering across all rebuild families is
  **UNKNOWN / NOT ESTABLISHED** and is not a separate serialized authority; A3
  must require valid deterministic DAG evaluation, not equality of that list.
- No current host defect is claimed. The observed successful-redefine movement
  is a gap relative to the proposed future P3 contract, not a violation of an
  already approved product requirement.
- No ADR is created: this remains a design candidate pending author review and
  no productive A3 mechanism is authorized by A4.

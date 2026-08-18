# G9A3 spatial lifecycle and migration design

Status: **IMPLEMENTED — PASS — AUTHOR APPROVED**.

This document refines the lifecycle table in
`geocedg/specs/spatial/g9-spatial-projection-semantics.md` and Accepted ADR
0011. It does not authorize G9B primitives, G9C composition, a public migration
workflow, or any later G9/G10 phase.

## 1. Boundary and invariants

G9A3 hardens the existing POINT / `PROJECTION_DEFINED` pilot. It adds no new
spatial type or canonical schema. Every operation obeys these invariants:

- durable identity is addressed only by typed IDs and explicit old targets;
- labels, coordinates, proximity, layer, construction/XML order, output ordinal,
  viewport state and Java reference equality are never continuity authority;
- an immutable prospective graph is validated before the live graph changes;
- one commit changes records, attachments, resolutions and normal-DAG topology;
- a rejected or failed operation leaves the prior registry, DAG, payload and XML
  snapshot exact;
- geometrically invalid current inputs preserve identity but publish no payload;
- copy allocates a fresh internally consistent closure and records immediate copy
  provenance;
- legacy files remain unassociated until an explicit typed association request;
- unknown future syntax fails truthfully and is never silently downgraded.

## 2. Lifecycle transaction

`SpatialLifecycleTransaction` is construction-confined. A prepared transaction
owns a `SpatialLifecycleMutation` containing:

- an operation kind and explicit provenance token;
- expected source records/revisions;
- immutable records to create or replace;
- record IDs to retire;
- explicit geo attachments or detachments;
- an optional declared same-construction external-reference allow-list.

Preparation creates a complete prospective record and attachment graph without
mutating live state. It validates:

1. typed ID kind, supported record version and exact POINT-v2 shape;
2. reserved/fresh IDs for creates and exact same IDs for in-place replacements;
3. current expected records and revisions, preventing stale commits;
4. reciprocal object/binding and system/map/relation membership;
5. frame/map/relation ownership, roles, units and reference closure;
6. absence of semantic authority cycles;
7. operation-specific revision deltas;
8. complete typed resolution evidence for the prospective graph, while
   preserving any pre-existing explicitly `BROKEN` records that the operation
   does not claim to repair;
9. prospective `SpatialSemanticInputs` and replacement normal-DAG topology.

Commit performs one no-fail registry state switch and one prepared runtime switch.
If either switch cannot complete, the transaction restores the exact captured
state and publishes no candidate payload. Rollback before commit releases fresh
reservations but retains them in the construction-lifetime issued-token ledger.

### 2.1 Revision rules

| Operation | Preserved IDs | Required revision change |
|---|---|---|
| semantic no-op | all | none |
| compatible definition change | affected identity | definition/child `+1` |
| admitted topology change | affected identity | definition `+1`, topology `+1` |
| add/remove binding | object/system/geos | fresh/retired binding; object definition and topology `+1` |
| re-role binding | object/system/geos | retire old binding, fresh replacement; object definition and topology `+1` |
| compatible map/relation change | child and system | child `+1`, system `+1` |
| add/remove map/relation | system | fresh/retired child; system `+1` |
| re-role map/relation | system and referenced geos | retire old child, create fresh child, rewrite the complete affected typed closure; system `+1` |
| true semantic replacement | none from old meaning | fresh closure starting at revision zero |
| ordinary value recomputation | all | no topology revision; runtime value tuple changes |

Revision arithmetic is checked with `Math.addExact`; overflow rejects before the
state switch.

## 3. Productive POINT lifecycle service

`SpatialPointLifecycleService` is a UI-neutral shared-kernel facade over the
transaction. Its admitted operations are deliberately typed:

- add, remove or re-role a POINT projection binding;
- add or remove a coupled POINT-pilot frame/map pair;
- compatibly replace a frame, map, or frame relation while advancing its
  owning system revision;
- add, remove or re-role a system map or frame relation with reciprocal typed
  closure updates;
- replace the semantic meaning of a system using a fresh explicit closure;
- recover a typed missing reference by supplying the exact missing identity;
- associate legacy geos through a complete explicit POINT association plan.

The service accepts actual `GeoElement` targets only as caller-selected handles.
It never scans a construction or offers lookup by label, coordinates, proximity,
layer or ordering. A migration plan supplies every participating geo and every
typed frame/system/map/relation/binding/object record. Repeating an already
applied plan returns an explicit already-associated outcome or rejects; it never
allocates a duplicate graph.

Migration provenance is the explicit operation kind plus the durable
`EXPLICIT_ASSOCIATION` marker on each migrated POINT root. Existing G9A2 roots
without that optional marker remain `CONSTRUCTION_OWNED`, so their canonical XML
does not change. Provider/schema/role and ordinary copy/relation provenance stay
separate and retain their existing meaning. G9A3 adds no second inferred
identity or geometry authority.

`ProjectionFrameUseRole` is identity-defining for a diagram map. Re-role is
therefore not admitted as an in-place `MAP_CHANGE`. The typed `MAP_REROLE`
transaction preserves the system, retires the old map, creates a fresh rerolled
map, and fresh-retargets every affected binding/relation while rewriting each
owning object, peer map and system membership. `RELATION_REROLE` applies the
same fresh-child rule and updates both endpoint maps and the preserved system.
Incomplete or semantically expanded requests reject atomically. This keeps role
changes explicit without adding general genealogy or partial transfer.

## 4. Reference resolution and recovery

Resolution is recomputed over the complete prospective graph, not only newly
published records. Consequently:

- a new exact missing identity can move all affected records from `BROKEN` to
  `ACTIVE` deterministically;
- retirement or replacement can move every dependent record to its explicit
  current state in the same transaction;
- runtime reconciliation receives the union of changed records and records whose
  resolution changed;
- same-label or coincident replacement geometry never repairs a reference.

Native load remains two-stage. If an identity-bearing parse commits its spatial
section and a later XML error occurs, the host restores the entry snapshot for
the entire parse. Identity-free upstream parsing retains its existing behavior.

## 5. Copy policies

`COMPLETE_CLOSURE` remains the default for clipboard, duplicate, macro and
cross-document import. One deterministic remap rewrites every copied typed ID.

`DECLARED_SAME_CONSTRUCTION_EXTERNAL` is an explicit internal plan for copying
an object/binding/geo subgraph while sharing caller-declared active system
dependencies in the same construction. Every retained external ID is supplied
in an allow-list, belongs to the destination registry, is active, and is not
remapped. All copied owned records receive fresh IDs and immediate copy-source
provenance. The mode is forbidden across constructions. Any undeclared external
reference rejects the whole operation.

## 6. Redefine effects and stable-role groups

The identity decision remains `RETAIN`, `FRESH` or `REJECT`. A separate frozen
provider effect is one of:

- `NO_OP`;
- `DEFINITION_CHANGE`;
- `ADMITTED_TOPOLOGY_CHANGE`.

Single-output providers use the group machinery with one role. For a
multi-output candidate, the captured context and inspected proposal contain a
complete map from persisted `stableOutputRole` to old/candidate output. `RETAIN`
requires:

1. complete participating sibling coverage;
2. one unique stable role per output;
3. exact old/candidate role sets and cardinality;
4. exact provider/family/schema/authority/binding-role compatibility per role;
5. the provider's explicit effect and compatibility decision.

The host output array is inspected only to enumerate candidates; its ordinal is
never the mapping key. Missing or duplicate roles, partial sibling participation,
ambiguous mapping or cardinality change rejects atomically. A cardinality-changing
true replacement is not admitted in G9A3: explicit replacement still requires the
same complete provider-owned stable-role set. Merge/split genealogy and every
cardinality-changing replacement remain deferred and forbidden.

When a retained multi-output group must replace its host parent and any exact
participating output has dependents, the host uses a dependency-preserving XML
rebuild. Only after the provider has sealed complete exact-role compatibility,
old output labels are temporarily aligned to the corresponding candidate-role
labels for dependency transport. The intact pre-mutation construction is then
serialized and rebuilt with the decided durable-ID overlay. Labels are never
continuity authority, and every temporary host mutation remains covered by the
operation-local rollback snapshot. In-place and soft branches keep their live
parent and dependent DAG unchanged.

Serialization overlays every mapped candidate role with its decided durable ID.
Commit transfers the group and revision effects as one transaction. `NO_OP`
changes neither revision; `DEFINITION_CHANGE` advances definition only;
`ADMITTED_TOPOLOGY_CHANGE` advances definition and topology.

The productive `g9a2.point.pilot` provider derives expected semantic input kind
from durable POINT records and typed references. It may retain only a candidate
that satisfies that persisted role contract. Generic host class, definition text
or label equality is not proof.

## 7. Runtime atomicity

`SpatialSemanticRuntime` prepares all changed topologies and replacement
algorithms while the current publications remain available. It must not retire
the current algorithm before every replacement is constructible. A successful
switch removes old algorithms, installs the prepared maps and evaluates through
the normal `AlgoElement` DAG exactly once. On failure, prepared algorithms are
removed and the old maps/publications remain current.

The transient 3D adapter is queued only after the semantic switch is terminal
and announced to presentation listeners only after the redefine transaction is
validated and every operation/batch rollback context is explicitly completed.
Listener notification is best-effort and non-authoritative; a listener-triggered
model mutation is a subsequent publication, never absorbed into the completed
redefine lease. Listener failure is recorded diagnostically and is never
misrepresented as a rollbackable part of registry/DAG state.

Structural invalidity is a transaction rejection. Geometric invalidity under a
structurally valid graph is a committed current failure certificate with no
payload; it is not a registry rollback.

An operation-local host rollback capability captures the construction operation
epoch, the installed identity-graph publication epoch and the authoritative
semantic-certificate publication epoch. Allocation, reservation and preflight do
not advance either publication clock. Only a registry-owned lexical lease may
advance the captured clocks across that redefine's own synchronous live commit or
XML rebuild. Any unrelated graph install, delete/recovery publication or normal
DAG certificate publication therefore makes an older rollback capability stale.
Prepared runtime algorithms publish only to a staged evidence sink, merged after
all activation and computation succeeds, so failed or abandoned preparation does
not advance the live runtime clock.

## 8. Host and compatibility seams

Existing target-authority routes remain authoritative. The 3D release route
must capture its spatial context before creating a helper point and use the
pre-operation replace overload. Label/XML-discovered routes, CAS twins and
PolygonFactory's unsupported participating rewrites continue to reject.

GeoCeDG Classic is the same fork kernel with creation disabled. It must load,
recompute, save and reopen supported native records exactly. An external upstream
distribution does not understand the extension and is characterized as an
unsupported-open boundary; no converter, label fallback or lossy resave path is
added.

## 9. Verification boundary

Focused G9A3 validation covers mutation, copy, redefine, snapshots, XML,
explicit migration, Classic/native compatibility and forbidden-authority
counters. Every atomic rejection compares a canonical graph snapshot containing
typed IDs, references, revisions, resolutions, attachments, certificate axes,
payload presence and normalized spatial XML. G9A1 and G9A2 suites and upstream
`RedefineTest` are regressions. The final composed authority runs without
`-SkipBuild`.

## 10. Author closeout

The executable checks first produced an implementation candidate. A separate
author closeout on 19 August 2026 reviewed the sealed 81-path inventory, the 72
G9A3 scenarios, 181 inherited regressions, two deterministic focused runs and
the composed authority. Every authoritative run completed with zero failures,
errors or skips; main and test Checkstyle were clean. The historical candidate
report and machine evidence retain their original pending-review claims and
canonical-LF hash.

The accepted residual boundary remains POINT-only. General primitives and
composition are deferred to G9B/G9C; the external-upstream unsupported-open
case remains an explicit compatibility boundary; and transient 3D presentation
notifications remain non-authoritative. None is a lifecycle, identity or
persistence blocker for this phase. The user guide was reviewed and remains
unchanged because G9A3 adds no public command, GUI workflow or default-enabled
capability.

```text
G9A3 = PASS — AUTHOR APPROVED
G9A = PASS — AUTHOR APPROVED
G9U0 / G9X1 / G9U1 / G9B / G9C = DESIGNED — NOT AUTHORIZED
G9U2 = DESIGNED — BLOCKED ON THE APPROVED G9 GATE
G10 PRODUCTIVE IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

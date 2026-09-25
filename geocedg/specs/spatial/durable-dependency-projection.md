# Durable dependency projection of construction-defined identities

| Field | Value |
|---|---|
| Version | `1.1` (construction identity `schemaVersion` 1 and 2) |
| Phase | `PRE-G9B-R3`; version 1.1 from the `PRE-G9B-R3-R1` corrective descendant |
| Decision | [ADR 0029](../../../docs/adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md) |
| Parents | [ADR 0010](../../../docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md), [ADR 0011](../../../docs/adr/0011-g9-spatial-persistence-and-phase-gates.md), [ADR 0026](../../../docs/adr/0026-advanced-redefine-and-explicit-legacy-fallback.md) |
| Design record | [compatible-redefine design §7](../../../docs/architecture/post_g9u1_a3_v2_compatible_redefine.md) |
| Reconciled authorities | [ADR 0011](../../../docs/adr/0011-g9-spatial-persistence-and-phase-gates.md) lifecycle table, [G9 spatial projection semantics](g9-spatial-projection-semantics.md) §8.1–§8.2, [G9U1 construction interaction](../ui/g9u1-construction-interaction.md) §10 |
| Affected layer | Shared Java kernel, spatial identity registry and construction XML |

This contract was introduced by the `PRE-G9B-R3` technical candidate. Its
author-approval status is recorded only in that phase's author-decision record,
never here.

Version 1.1, written by the `PRE-G9B-R3-R1` corrective descendant, names the two
lifecycle events that move a persisted dependency set without an explicit
redefine (§10, §14), places every event of this contract in the approved
lifecycle taxonomy (§15) and states exactly what undo and redo restore (§7.1).
It changes no implemented behaviour.

## 1. Purpose

A construction-defined identity record (provider
`geocedg-construction-provider/v1`, schema `geocedg-construction-geo`)
persists the durable identities its participant depends on. Before this
contract no document stated which identities those are, and two code paths
disagreed: publication dropped every direct input without a durable identity,
while redefine assessment rejected it. The disagreement made every redefine of
an ordinary participant with an identity-free input fail as a false
`AMBIGUOUS` (`D3-a`).

This contract defines the projection once, versions it through the record's
`schemaVersion`, and states where it is used, how historical records keep their
meaning and when a record moves to the current rule.

## 2. Scope

Included:

- the two projection rules and the record versions that name them;
- validation of every record under its own rule;
- the lazy migration events and their atomicity;
- the certified durable-frontier refresh of a version-2 record after an
  ordinary edit (§10);
- the late-participation re-projection of every record (§14);
- the classification of an explicit redefine that changes the projected set;
- the place of each of these events in the approved lifecycle taxonomy (§15).

Excluded: projection bindings and other spatial records, public Locus V2
completeness (unchanged, §6.3), construction-order policy, the metric,
intersection and spline semantics, and any promotion of ordinary construction
elements to durable identities.

## 3. Definitions

- **Participant**: a geo attached to a construction-defined identity record.
- **Durable identity**: a `PersistentGeoId` attached to a geo in the identity
  graph that governs the operation (§5).
- **Identity-free geo**: a construction geo with no durable identity. It stays
  identity-free under this contract.
- **Direct inputs** of a geo: the inputs of its parent algorithm, except that a
  productive Locus V2 parent exposes its declared durable dependency geos
  (`AlgoDependentPointLocusV2.getDurableDependencyGeos`). An independent geo has
  none.

## 4. The two rules

### 4.1 Version 1 — `DIRECT` (historical)

`DIRECT(P)` is the set of durable identities among the direct inputs of `P`.
An identity-free direct input is ignored and is not traversed.

### 4.2 Version 2 — `TRANSITIVE_DURABLE_FRONTIER`

`FRONTIER(P)` is the set of nearest durable ancestors of `P`: a walk starts at
the direct inputs of `P`; a visited geo with a durable identity contributes that
identity and is not traversed further; an identity-free geo is traversed
through its own direct inputs. Example: `A -> H -> C`, where `H` is
identity-free and `A`, `C` are durable, gives `DIRECT(A) = []` and
`FRONTIER(A) = [C]`.

### 4.3 Properties of both rules

- The result is sorted by durable identity and contains each identity once.
- Each geo is visited at most once per projection, and `P` itself is never
  revisited, so repeated inputs and diamonds terminate.
- The only evidence is the construction DAG and the durable identity of each
  visited geo. Coordinates, proximity, labels, construction order or index,
  XML position and render state are never read.
- Both rules are implemented once, by
  `org.geocedg.common.kernel.spatial.identity.DurableDependencyProjection`.
  No other projection of construction dependencies exists.

## 5. Where the projection is used

Every use calls the single implementation with the identity mapping that
governs that operation:

| Use | Rule | Identity mapping |
|---|---|---|
| New publication (participation batch) | version 2 | current identities plus the batch's staged ones |
| Validation of any published batch (load, undo/redo, clipboard, macro, rebuild) | the record's own version | the batch's prospective records |
| Redefine assessment of the old target | version 2 over the live DAG (§8.2) | live identities |
| Redefine candidate description | version 2 | the sealed candidate identity overlay |
| Late-participation re-projection (§14) | the record's own version | prospective identities |
| Certified durable-frontier refresh (§10) | version 2 | the rebuild's attachments |
| Copy and macro instantiation | version 2 | remapped attachments, then live identities |

A participation batch additionally requires every dependency the Locus V2
operation declares to lie in the published frontier; a violation is a kernel
fault, not a user outcome.

## 6. Versioning and validation

### 6.1 The version names the rule

The record's `schemaVersion` identifies the rule that governs its
dependencies, not whether the two rules happen to agree for this record. Every
new durable record is version 2.

### 6.2 Each record under its own rule

A record is valid only if its persisted dependencies equal the projection of its
own version over the prospective DAG. Otherwise the whole batch fails with
`MALFORMED_RECORD` ("dependencies disagree with the prospective algorithm
DAG"). A version-1 record is never validated as version 2, nor the reverse.

### 6.3 Unchanged public completeness

A public Locus V2 output still requires every direct durable dependency geo to
have a prospective identity, independently of the version.

### 6.4 Unknown versions

A construction identity record whose `schemaVersion` is neither 1 nor 2 fails
the batch with `UNSUPPORTED_VERSION` ("Construction identity schema version N
is newer than this GeoCeDG supports"). It is never read under an older rule.

### 6.5 Unchanged XML

The spatial section keeps `<geocedgSpatial version="1">` and the closed
attribute set of `<geo/>`. Only the value `schemaVersion="2"` is new.

## 7. Lazy migration

### 7.1 No eager migration

Opening, saving and reopening a document, moving free objects, ordinary edits
and late participation (§14) never change a record's version. A document may mix
version-1 and version-2 records; each is validated under its own rule.

Undo and redo restore the committed serialized snapshot of a historical
construction state, including the version of every record in it. They never
perform a migration themselves. The following sequence is therefore valid, and no
silent migration occurs in it:

```text
version-1 state -> semantic event (§7.2) -> committed version-2 state
                -> undo -> restored version-1 snapshot
                -> redo -> restored version-2 snapshot
```

### 7.2 Migration events

A version-1 record becomes version 2 only within an explicit semantic event of
its own participant under current semantics:

- a successful compatible redefine (no-op, definition change or admitted
  topology change) that retains its identity;
- an explicitly accepted identity-preserving durable-contract update (§9.3).

The event upgrades the record even when `DIRECT` and `FRONTIER` are equal for
it at that instant. A no-op keeps both revisions; the other effects advance
them as they would without the upgrade. Other records of the document are not
touched.

Events that create identities publish version 2 directly: new participation,
explicit replacement, copy/paste, macro instantiation and delete/recreate.

### 7.3 Atomicity

The upgrade is part of the redefine's lifecycle transaction. A rejected,
cancelled, stale or failed redefine leaves the record, and the persisted
document, exactly as before.

## 8. Redefine assessment

### 8.1 Candidate

The candidate signature of a construction participant is always version 2; its
dependencies are `FRONTIER(candidate)` over the sealed candidate overlay.

### 8.2 Old target

The persisted signature stays the currentness authority. For comparison the
kernel lifts the old target to version 2, with `FRONTIER(old target)` over the
live DAG. A version-2 old target whose persisted dependencies differ from its
live frontier fails closed with `REDEFINE_INCOMPATIBLE`.

### 8.3 Effect

Equal frontiers give a no-op or a definition change; different frontiers give an
admitted topology change. An identity-free input no longer makes a proposal
undescribable.

## 9. Durable-contract change

### 9.1 Classification

An admitted topology change of an ordinary participant (the candidate is not a
public Locus V2 output and the old target is not a V2 source) is an explicit
change of the participant's own durable contract. It is assessed as
`DURABLE_CONTRACT_CHANGE` and is never executed without an explicitly selected
operation. Coinciding values or coordinates of old and new dependencies are no
evidence of equivalence.

### 9.2 Available operations

The assessment states two independent kernel decisions:

- `identityPreservingUpdateAvailable` — the predicate of §9.3 holds and the
  retain signatures are compatible;
- `explicitReplacementAvailable` — the provider admits `FRESH` with replacement
  intent and the impact report is complete.

Neither is implied by the other, and the failure of retention never implies
replacement.

### 9.3 Identity-preserving update predicate

The update keeps the identity only when the candidate keeps the provider,
family, schema, authority, binding role, stable role (supported by the
candidate) and cardinality of the old target, and neither side is a V2 source.
Cycles, stale state and incomplete role groups are excluded before the
predicate. The frontier may change. An accepted update keeps the identity,
publishes version 2 with the new frontier, advances the definition and topology
revisions and recomputes the dependents, as one host operation.

### 9.4 Failures that describe no proposal

| Cause | Status |
|---|---|
| Changed geo family or candidate output cardinality | `DURABLE_CONTRACT_CHANGE`, no operation available |
| Changed stable-role contract | `DURABLE_CONTRACT_CHANGE`, no operation available |
| Several old outputs, missing or ambiguous context or effect group | `AMBIGUOUS` |
| Non-neutral or unknown-version context, uncovered or unmapped candidate roles, any untyped description failure | `UNDESCRIBABLE_PROPOSAL` |

Only a provider-typed ambiguity is `AMBIGUOUS`. A real cycle stays
`INVALID_DAG`; other runtime failures stay `UNSUPPORTED`.

## 10. Certified durable-frontier refresh after an ordinary edit

`CERTIFIED_DURABLE_FRONTIER_REFRESH` is the lifecycle event in which an ordinary
semantic edit changes the authoritative frontier of a version-2 ordinary
participant. The change is an authorized semantic lifecycle event when, and only
when, the predicate below certifies identity preservation. It is distinct from
ordinary value-only recomputation, from an explicit target redefine (§8, §9),
from incompatible replacement and from late participation (§14).

An ordinary edit of an identity-free geo rebuilds the construction. When the
rebuild changes the frontier of a version-2 ordinary participant
(`oldFrontier != newFrontier`), the kernel refreshes the record only when all of
the following hold:

1. the participant is an ordinary construction participant (not a public Locus
   V2 output) whose record was version 2 before the edit;
2. its own construction step is unchanged: its geo class, its parent algorithm
   class and, per direct input, the same durable identity or an identity-free
   input of the same geo class;
3. its contract matches the rebuilt geo (provider, family, schema, authority,
   binding role, supported stable role, cardinality one);
4. its parent algorithm has exactly one output, so its role group is complete;
5. the published batch then passes the full DAG, cycle and currentness
   validation.

```text
oldFrontier != newFrontier
        |
        v
certified predicate (conditions 1-5)
        |
        +-- certified ----> keep the durable identity
        |                   publish the new frontier atomically with the rebuild
        |                   advance definitionRevision and topologyRevision
        |                   keep host, DAG and persistence coherent
        |
        +-- not certified -> REDEFINE_INCOMPATIBLE; the exact operation-entry
                            snapshot restores the whole edit atomically
```

Advancing both revisions is not an accidental consequence of ordinary
recomputation: it records a certified semantic change of the participant's
durable dependency contract. Revision-scoped evidence of its dependents follows
the normal currentness rules. A computable new frontier is never sufficient on
its own.

An edit that leaves every frontier unchanged, including the in-place soft
redefinition of constant helper inputs, only recomputes. A version-1 record
never auto-refreshes: an ordinary edit cannot change its `DIRECT` set.

## 11. Compatibility

- Builds before `PRE-G9B-R3` accept only `schemaVersion="1"` for construction
  records and reject a version-2 record as `MALFORMED_RECORD`: opening such a
  document fails as a whole and nothing is read under the direct rule.
- This build rejects any later version as `UNSUPPORTED_VERSION` (§6.4).
- Historical version-1 documents open, save and reopen byte-identically in
  their spatial records.

## 12. Validation

The focused authority is the `PreG9bR3*` test classes of `PRE-G9B-R3`
(projection and persistence, vocabulary, durable-contract change, certified
refresh, witness, Desktop frontend) together with the unchanged POST-G9U1-A3
and G9A3 lifecycle suites. The phase candidate report records the executed
evidence.

For §14, `PreG9bR3R1LateParticipationTest` (the `PRE-G9B-R3-R1` corrective
descendant) pins the positive single-output case through a traversed helper,
with undo, redo and reopen, a version-1 record, and the whole-publication
rejection. `G9U1ConstructionParticipationReviewTest` pins the direct-input case
and the rollback of a failed publication. Its report records the executed
evidence.

## 13. Stop conditions

Stop and obtain an author decision before:

- adding a third rule or a new record version;
- migrating records outside the events of §7.2;
- relaxing any condition of §9.3 or §10;
- changing the identity, role, revision or version effect of an event of §10 or
  §14, or merging the two events;
- deriving dependencies from any evidence other than the DAG and durable
  identities.

## 14. Late-participation re-projection

Sections 14 and 15 were added by the `PRE-G9B-R3-R1` corrective descendant;
sections 1-13 keep their numbering.

`LATE_PARTICIPATION_REPROJECTION` is the lifecycle event in which a geo on the
projection path of an already-participating record, whose own construction
definition is unchanged, first acquires a durable identity. It is distinct from
the certified durable-frontier refresh of §10: the DAG and the geometry are
unchanged; only the set of durable geos grows.

The projection path is fixed by the record's own version:

| Record version | Rule | Projection path |
|---|---|---|
| 1 | `DIRECT` (§4.1) | the direct inputs |
| 2 | `TRANSITIVE_DURABLE_FRONTIER` (§4.2) | the identity-free geos visited by the frontier walk, identity-free direct inputs included |

Inside the publication that attaches the new identity, every record whose
projection path contains the newly durable geo is re-projected under its own
version over the prospective identities of that publication:

```text
a geo on the projection path first acquires a durable identity
        |
        v
recompute the projection under the record's own version
        |
        v
prospective publication valid (DAG, cycle, currentness, per-version validation)?
    yes -> publish the refreshed dependency set atomically with the new identity;
           keep the durable identity, the roles, definitionRevision,
           topologyRevision and the schema version
    no  -> reject the whole publication atomically; nothing is published
```

The event performs no version-1 to version-2 migration. It invokes neither the
redefine compatibility predicate (§8, §9.3) nor the certified-refresh predicate
of §10. Its only evidence is the construction DAG and the durable identities
(§4.3): labels, construction index, XML position, Java references, coordinates,
proximity and visible ordering are never read.

Example: `f = Line(C, xAxis)`, `D = Intersect(f, yAxis)`, `E = Midpoint(D, C)`,
with `C` durable and `f`, `D` identity-free. `E` is a version-2 record with
`FRONTIER(E) = [C]`. When `f` first acquires a durable identity, for example as
the target of a semantic intersection of a Locus V2 with `f`, the record of `E`
becomes `[C, f]` with the same identity, roles, revisions and version. Were `E`
a version-1 record, `f` would not enter it, because `f` is not a direct input of
`E`; a newly durable direct input such as `D` would.

The positive guarantee of this section is pinned for a participant whose parent
algorithm has exactly one output (§12). A participant that shares its parent
algorithm with further outputs is subject to this section only as far as the
existing participation contracts admit its record; this contract adds no
guarantee for that shape.

For version-1 records this event is the derived-dependency refresh of the
[G9U1 construction interaction](../ui/g9u1-construction-interaction.md) §10,
which remains authoritative, and unchanged, for the records to which it
originally applied.

## 15. Relation to the approved lifecycle taxonomy

The classes A-G of
[ADR 0011](../../../docs/adr/0011-g9-spatial-persistence-and-phase-gates.md) and
the lifecycle table of the
[G9 spatial projection semantics](g9-spatial-projection-semantics.md) §8.1 are
unchanged. For construction identity records this contract refines them as
follows; the extension note of ADR 0011 and §8.2 of that specification point
here.

| Event | ADR 0011 / G9 §8.1 | Durable identity | Roles | Definition and topology revisions | Schema version |
|---|---|---|---|---|---|
| first participation of a geo | creation, outside classes A-G | new | new | initial | 2 |
| ordinary recomputation, every frontier unchanged | class A; "Ordinary recomputation" | kept | kept | unchanged | unchanged |
| `CERTIFIED_DURABLE_FRONTIER_REFRESH` (§10) | new event beside class A: an ordinary edit with no explicit target | kept | kept | both advance | unchanged (version 2 only) |
| `LATE_PARTICIPATION_REPROJECTION` (§14) | new event beside class A; generalizes G9U1 §10 | kept | kept | both kept | unchanged; never a migration |
| compatible redefine (§8) | class B | kept | kept | as class B: definition unless a proven no-op, topology only for an admitted topology change | a version-1 record becomes version 2 in the same transaction (§7.2) |
| accepted identity-preserving contract update (§9.3) | class B under an explicitly selected operation | kept | kept | both advance | 2 |
| explicit replacement (§9.2) | class C, selected explicitly as class D allows | new | new | new record | 2 |
| durable-contract change with no selected operation (§9.4) | class D, rejected atomically | kept | kept | unchanged | unchanged |
| delete then recreate | class E | new | new | new record | 2 |
| copy/paste, macro instantiation | class F | new for the copy | new | new record | 2 |
| undo/redo, native save and reopen | class G | restored exactly | restored exactly | restored exactly | restored exactly; never migrated (§7.1) |

The lazy upgrade of §7.2 happens only inside an explicit semantic event of the
participant. It is therefore the deterministic compatibility handling inside an
explicit user-directed operation that ADR 0011 admits as migration, not an
automatic migration, and it never touches another record. Neither new event
allocates or retires an identity or transfers a binding.

# Durable dependency projection of construction-defined identities

| Field | Value |
|---|---|
| Version | `1.0` (construction identity `schemaVersion` 1 and 2) |
| Phase | `PRE-G9B-R3` |
| Decision | [ADR 0029](../../../docs/adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md) |
| Parents | [ADR 0010](../../../docs/adr/0010-role-gated-spatial-authority-and-durable-identity.md), [ADR 0011](../../../docs/adr/0011-g9-spatial-persistence-and-phase-gates.md), [ADR 0026](../../../docs/adr/0026-advanced-redefine-and-explicit-legacy-fallback.md) |
| Design record | [compatible-redefine design §7](../../../docs/architecture/post_g9u1_a3_v2_compatible_redefine.md) |
| Affected layer | Shared Java kernel, spatial identity registry and construction XML |

This contract was introduced by the `PRE-G9B-R3` technical candidate. Its
author-approval status is recorded only in that phase's author-decision record,
never here.

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
- the certified refresh of a version-2 record after an ordinary edit;
- the classification of an explicit redefine that changes the projected set.

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
| Late participation of a traversed input | the record's own version | prospective identities |
| Certified refresh after an ordinary edit | version 2 | the rebuild's attachments |
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

Opening, saving, reopening, undo/redo, moving free objects and ordinary edits
never change a record's version. A document may mix version-1 and version-2
records; each is validated under its own rule.

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

## 10. Certified refresh after an ordinary edit

An ordinary edit of an identity-free geo rebuilds the construction. When the
rebuild changes the frontier of a version-2 ordinary participant, the kernel
refreshes the record only when all of the following hold:

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

The refreshed record keeps its identity, receives the new frontier and advances
its definition and topology revisions; revision-scoped evidence of its
dependents follows the normal currentness rules. If any condition fails, the
rebuild fails with `REDEFINE_INCOMPATIBLE` and the host restores the exact
operation-entry snapshot, so the whole edit is rolled back. A computable new
frontier is never sufficient on its own.

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

## 13. Stop conditions

Stop and obtain an author decision before:

- adding a third rule or a new record version;
- migrating records outside the events of §7.2;
- relaxing any condition of §9.3 or §10;
- deriving dependencies from any evidence other than the DAG and durable
  identities.

# POST-G9U1-A3-R1 — advanced V2 redefine, legacy fallback and impact

- Status: **DESIGN / IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Base commit: `b12d9ad04a766e893b120f3155250d5f1654c19b`
- Base tree: `930e58379dfe2e87e80d1ac81963636809274c41`
- Governing position contract: **A4/P3 — AUTHOR APPROVED; P3-R1 AUTHOR APPROVED REFINEMENT/AMENDMENT OF P3**
- Change route: `ORDINARY`
- Product phase: `POST-G9U1-A3`
- Self approval: `false`

This replacement design reconciles the reviewed A3 candidate with P3-R1. It
specializes the existing G9A3 lifecycle transaction for public V2 constructions,
adds a non-mutating assessment seam and a typed semantic-impact report, and adds
no second identity, rollback or construction-order framework. The rejected
candidate `4373c81b4961d0ab802eff0d77e28fc577586d75` remains historical review
evidence only.

## 1. Design record

```text
CURRENT_G9A3_REDEFINE_MODEL =
  explicit old-target context + provider-owned RETAIN/FRESH/REJECT decision
  + complete stable-role output groups + atomic registry/DAG publication
  + operation-entry XML rollback

V2_COMPATIBILITY_PREDICATE =
  same registered provider/family/schema/authority/binding-role contract,
  same complete stableOutputRole set and cardinality,
  same versioned V2 semantic-constructor contract; dependency-edge changes
  are admitted topology changes and remain subject to DAG and P3 validation

V2_TRUE_REPLACEMENT_PREDICATE =
  an explicitly selected replacement operation; a V2 constructor-contract
  mismatch without that intent is REJECT, never implicit continuity

MULTI_OUTPUT_ANCHOR_MODEL =
  one producer/group represented by the complete sorted
  stableOutputRole -> PersistentGeoId map

BOTH_ANCHORS_REMOVED_POLICY =
  use only operation-entry structural evidence; an unestablished surviving
  sequence makes advanced RETAIN unavailable, never a post-mutation search

REVISION_EFFECT =
  NO_OP preserves revisions; compatible definition change advances definition;
  admitted dependency/topology change advances definition and topology;
  downstream state is recomputed/revalidated

PERSISTENCE_EFFECT =
  retained IDs and candidate dependency edges are serialized atomically;
  no numeric position, XML ordinal, Java reference or transient cache is stored

ROLLBACK_EFFECT = existing snapshot mechanism reused
```

## 2. V2 constructor contract

Every admitted `GeoLocusV2` source algorithm declares a stable, versioned
semantic constructor-contract identifier. The identifier distinguishes
mathematical constructor families and overloads whose meaning differs, such as
weighted and unweighted `SplineV2` and each supported transform kind/centre
form. Downstream metrics, semantic points and intersections are revalidated;
A3 does not broaden their own public redefine surface.

The token is explicit kernel authority. Java class equality, command text,
labels, coordinates, output ordinal and list position are deliberately absent.
Equality of the token is necessary but not sufficient: the generic G9A3
signature and complete stable-role group checks remain mandatory.

For a same-contract edit, changed durable dependency IDs are an
`ADMITTED_TOPOLOGY_CHANGE`. The retained record publishes the candidate edge
set and advances both definition and topology revisions. Candidate-only
dependencies may become durable in that same sealed lifecycle mutation only
when they are the exact transitive dependency closure declared by the candidate
output signatures. Extraneous or incomplete staged dependencies remain
rejected; the implementation never promotes an uninspected parser side effect.

Different constructor contracts fail closed unless the caller explicitly
selects true replacement. True replacement receives fresh durable identities
and does not inherit P3 continuity.

## 3. A4/P3-R1 procedural position

P3 requires preservation of procedural position relative to surviving
unaffected anchors, subject to DAG legality. P3-R1 is its explicit
author-approved refinement/amendment: preserve the previous procedural interval
whenever legal; if new semantic dependencies make that interval illegal,
compatible identity may survive only through the minimum deterministic
DAG-forced relocation of the participating producer/group. This is not an
arbitrary topological equivalence policy.

The redefine context captures the target producer's entry slot and the complete
ordered sequence of unaffected construction elements. A participating producer
is identified durably by its complete `stableOutputRole -> PersistentGeoId`
relation. An ordinary GeoGebra element is instead a transaction-local procedural
witness: its entry-sequence position and live handle may be used only within the
captured host operation. Neither is serialized or promoted to CeDG identity.

The precise placement contract is:

```text
OLD_POSITION_LEGAL = oldSlot lies within the candidate DAG bounds
REQUIRED_PREDECESSOR_BOUND = candidate producer getMinConstructionIndex()
REQUIRED_DEPENDENT_BOUND = retained target/group getMaxConstructionIndex()
MINIMAL_RELOCATION_RULE = clamp oldSlot to the legal bound interval
UNAFFECTED_ORDER_INVARIANT = move only the target/group producer
MULTI_OUTPUT_GROUP_MOVEMENT = all stable roles share that one producer move
```

Equivalently:

```text
P3-R1 != any valid topological order
P3-R1 = minimum deterministic relocation forced by the new DAG
destination = clamp(oldSlot, requiredPredecessorBound, requiredDependentBound)
```

If the old slot is legal, no relocation occurs. Otherwise `RETAIN` may move the
producer only to the closest legal boundary forced by the new DAG. The host
`moveInConstructionList` operation preserves the relative order of every other
construction element. Numeric position is therefore a transaction-local
coordinate used to realize P3-R1, never semantic identity or persistent
authority. An invalid bound interval, a cycle, a changed/unprovable unaffected
sequence, or a group without one producer makes advanced `RETAIN` inadmissible.
No resulting-construction nearest-neighbour search is permitted.

## 4. Assessment, legacy fallback and semantic impact

A provider-participating redefine first produces a non-mutating assessment
bound to the captured old output group, proposal, host-operation epoch, durable
graph/runtime publication epochs and the complete serializable host state at
assessment. Preparation carries that host-state seal forward; the construction
must pass one final currentness authorization immediately before entering its
mutating replace path, and commit rejects an assessed transaction that never
received that authorization. Its closed result states
distinguish advanced retain with or without relocation, an offerable legacy
replacement, invalid DAG, incompatible host redefine, unsupported/ambiguous
evidence and a stale assessment. Preparing or committing a stale assessment
fails and requires reassessment.

Failure of `RETAIN` is never replacement authority. A legacy offer exists only
when the provider independently admits `FRESH`, the host proposal is structurally
valid, and the registry can enumerate the complete CeDG-relevant downstream
closure. Execution then requires the explicit `LEGACY_REPLACEMENT` mode. The
fresh transaction retires the old semantic closure and publishes fresh candidate
identities through the existing G9A3 transaction; it remains one host redefine
and one normal undo step.

The assessment's impact report is authoritative and typed. Each entry identifies
the durable participant, record/relation kind, every affected source, predicted status,
reason and required recovery class. Its closed vocabulary distinguishes automatic
revalidation, noncurrent/undefined results, explicit rebind/redefine and retirement.
If any referenced record or current attachment needed to prove the closure is
missing, completeness is `NOT_ESTABLISHED` and legacy fallback is not offerable.
No entry selects a replacement source. Under the current source-identity-loss
policy no affected semantic relation is labelled `AUTO_REVALIDATES` without an
independent exact contract; that vocabulary value is reserved for a provider
that can actually prove such revalidation.

## 5. Publication, currentness and rollback

A retained transaction publishes, as one lifecycle operation:

- the same durable IDs and exact stable roles;
- the candidate dependency/DAG edge set;
- the required monotone revisions;
- re-evaluated A1 occurrence provenance, A2 route bindings and other V2
  downstream results; and
- a legal P3 position.

An admitted topology change bypasses host `softRedefine`, because mutating old
inputs in place would not install the provider-approved candidate DAG. The
normal replacement/rebuild route reconstructs dependencies and current result
state instead.

P3 validation occurs within the existing publication/rollback boundary. Any
provider, role, currentness, DAG or P3 failure restores the operation-entry XML
snapshot and exact durable graph/order. Reconstructed Java instances are
permitted. Construction Protocol presentation/navigation is excluded from
identity and rollback authority.

## 6. Kernel/frontend boundary and compatibility

The kernel owns compatibility, P3-R1 planning, host-fallback eligibility,
RETAIN/FRESH/REJECT, impact completeness, identity, revisions/currentness and
atomic commit/rollback. A later frontend may display the typed assessment, ask
for explicit legacy confirmation, list affected identities, reacquire their
current `GeoElement` handles, open the existing edit workflow and offer ordinary
Undo. It may not decide or upgrade semantic validity. No Swing, localization or
dialog state enters the kernel result.

The bounded integration contract is recorded in
[`post_g9u1_a3_r1_frontend_handoff.md`](post_g9u1_a3_r1_frontend_handoff.md).

Compatibility boundaries remain:

- ordinary recomputation and temporary invalidity retain identity and use the
  existing currentness rules;
- rename has no semantic effect;
- save/reopen and undo/redo reconstruct the committed durable relation;
- copy/remap allocates the fresh copied closure defined by G9A3;
- delete/recreate receives fresh identity;
- missing, duplicate, partial or changed stable-role groups fail closed;
- arbitrary cardinality-changing redefine and inferred old-file associations
  remain unsupported.

No public command syntax changes. `GUIDE_IMPACT = NO` because A3 supplies a
kernel lifecycle guarantee and does not add a documented user command or UI
workflow.

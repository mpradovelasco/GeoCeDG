# ADR 0029: Versioned durable dependency projection, lazy migration and typed redefine outcomes

- Status: **Proposed — `PRE-G9B-R3` technical candidate**
- Date: 2026-09-24
- Phase: `PRE-G9B-R3` (redefine correctness), resumed under the author's
  `AD-R0-6` migration disposition
- Extends: [ADR 0026](0026-advanced-redefine-and-explicit-legacy-fallback.md)
  (Decisions 4 and 5 apply unchanged to the replacement path)
- Normative contract: [durable dependency projection](../../geocedg/specs/spatial/durable-dependency-projection.md)
- Design record: [compatible-redefine design §7](../architecture/post_g9u1_a3_v2_compatible_redefine.md)

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This record carries no approval of its own; acceptance is recorded only by a
separate author-decision record.

## Context

`PRE-G9B-R0` classified the author witness redefine `E := Midpoint(D, C+(1,0))`
as `INCORRECTLY_REJECTED`. Publication projected an ordinary participant's
durable dependencies over its direct inputs and dropped every input without a
durable identity; redefine assessment used the same direct projection but threw
on such an input. Every redefine of `E`, whose direct input `D` is identity-free,
therefore failed, and nine distinct description failures were all reported as
`AMBIGUOUS` (`D3-a`, `D3-b`).

Only a projection that reaches the durable ancestor through the identity-free
helper restores a comparable set. Adopting it on both sides changes what a
persisted dependency list means. Under `AD-R0-6` the author reviewed that
migration consequence and decided the contract recorded here. A silent eager
migration remains forbidden.

## Decision

1. **Two versioned rules.** A construction identity record's `schemaVersion`
   names the rule governing its dependencies. Version 1 keeps the historical
   direct projection: durable identities among the direct inputs, identity-free
   inputs neither kept nor traversed. Version 2 is the transitive durable
   frontier: identity-free inputs are traversed to the nearest durable
   ancestors. Both rules are structural, deterministic and cycle-safe, read only
   the DAG and durable identities, and exist once, in
   `DurableDependencyProjection`. Publication, assessment, validation,
   refresh and copy all use it.
2. **Each record under its own rule.** Every published batch validates each
   record under the rule of its own version. A version-1 record is never read as
   version 2, nor the reverse. A record of an unknown version fails with
   `UNSUPPORTED_VERSION`.
3. **Lazy migration.** Opening, saving, undo/redo and ordinary edits never change
   a version. A version-1 record becomes version 2 only inside an explicit
   semantic event of its own participant: a successful compatible redefine or an
   accepted identity-preserving contract update. It upgrades even when both
   rules give the same set at that instant, because the version names the rule,
   not the current difference. Records created by publication, replacement,
   copy, macro instantiation or recreation are version 2. Mixed documents are
   valid.
4. **Certified auto-refresh.** When an ordinary edit of an identity-free geo
   changes the frontier of a version-2 ordinary participant, the kernel keeps the
   identity only under a closed predicate: unchanged contract (provider, family,
   schema, authority, binding and stable role, cardinality), unchanged own
   construction step, a complete one-output role group, a non-public
   participant, and a valid DAG. The record then receives the new frontier and
   advanced definition and topology revisions; dependents recompute and follow
   their normal currentness rules. Otherwise the rebuild fails closed and the
   whole edit is rolled back. A computable new set is never sufficient.
5. **Explicit durable-contract change.** An explicit redefine that changes the
   frontier of an ordinary participant is typed `DURABLE_CONTRACT_CHANGE`.
   Construction order and coinciding coordinates are never evidence that a new
   dependency is equivalent to an old one.
6. **Two independent explicit operations.** The assessment states whether an
   identity-preserving contract update is available (a dedicated predicate that
   keeps the invariant identity class and allows the frontier to change) and
   whether an explicit replacement with a complete impact report is available.
   The update runs only as `IDENTITY_PRESERVING_CONTRACT_UPDATE`, the
   replacement only as `LEGACY_REPLACEMENT`. Failure of retention never implies
   replacement.
7. **Typed vocabulary.** `DURABLE_CONTRACT_CHANGE` is a describable change of a
   durable-contract field; `UNDESCRIBABLE_PROPOSAL` is a proposal that cannot be
   represented well enough to assess; `AMBIGUOUS` is reserved for genuinely
   multiple interpretations; `INVALID_DAG` remains a real cycle. Every rejection
   that was correct before stays a rejection.
8. **Kernel authority.** The frontend renders the closed status, the kernel's
   reason and only the operations the kernel declared available. It never
   derives or upgrades compatibility.
9. **Atomicity.** The upgrade, the contract update and the refresh are part of
   the existing lifecycle transaction. The operation-entry snapshot, now also
   captured for an ordinary edit that crosses a version-2 frontier, restores the
   exact prior document on any failure, including the host's legacy reordering.
10. **Construction order is not part of this decision.** The minimal Construction
    Protocol reorder refinement was not integrated in this phase and is recorded
    as `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`. The host's existing
    GeoGebra ordering is unchanged and, as before, never identity or provenance
    evidence.

## Consequences

- The author witness redefine is a compatible retain: `E` keeps its identity and
  its dependents recompute.
- Historical documents keep their exact meaning and bytes until the user
  explicitly redefines a participant.
- A build that predates this decision accepts only version 1 for construction
  records. It rejects a version-2 record as `MALFORMED_RECORD` and loads nothing;
  it never interprets the frontier under the direct rule.
- An ordinary edit on a version-2 frontier that fails certification is refused
  as a whole instead of leaving a stale record.
- The assessment carries two new closed flags and two new statuses; the Desktop
  frontend offers at most keep identity, replace and cancel.

## Rejected alternatives

- Dropping identity-free inputs on both sides: the candidate set becomes empty,
  unequal to the stored set, and the non-public dependency gate rejects both
  inspections.
- Eager migration of every record on load or save.
- Reinterpreting version-1 lists under the frontier rule, or validating a record
  under both rules.
- Promoting ordinary helpers to durable identities.
- Refreshing a record merely because a new frontier can be computed.
- Inferring equivalence from construction order, labels or coordinates.
- Inferring replacement from a retention failure.
- Treating every durable-contract change as retention-compatible.

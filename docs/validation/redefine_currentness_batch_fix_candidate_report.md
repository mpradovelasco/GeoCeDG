# Collected redefine currentness defect implementation candidate

## Status and scope

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
- Parent candidate: `52baac55562b14a7e75fa8afad9defa479a8cfa3`, tree
  `5b9428f7b00b0114e57818df4e9cc2767b2f8210`.
- Governing authority:
  [`POST-G9U1-A3-R1`](../architecture/post_g9u1_a3_v2_compatible_redefine.md)
  and [ADR 0026](../adr/0026-advanced-redefine-and-explicit-legacy-fallback.md).
- Classification: implementation correction to satisfy existing authority.
- `selfApproved=false`.

This candidate repairs only the final-currentness seam for collected retained
redefine. It does not change provider decisions, durable identity rules,
serialization, public APIs, frontend behavior or the approved A3-R1 contract.
The wider FINAL reconciliation remains paused.

## Reproduced defect

On the untouched parent candidate, these existing tests failed deterministically:

- `G9A1SpatialIdentityRedefineHostTest`
  `.compatibleReplacementWithChildrenUsesXmlRebuildAndRetainsIdentity`;
- `G9A3SpatialRedefineHostTest`
  `.redef09CollectedRetainedBatchCommitsOrRollsBackAsAWhole`.

The two-test Gradle run completed with exit code 1. Both failures reached commit
with an assessed transaction that had not passed
`authorizeRedefineHostMutation`, producing `REDEFINE_CONTEXT_MISSING`.
Evidence is retained under
`artifacts/redefine-currentness-batch-fix/before/two-failing-tests.json`.

## Root cause and correction

The ordinary Algebra redefine path prepares a transaction, opens its publication
lease and executes the final currentness authorization before host mutation. The
collected path stages the same prepared transaction and returns before that seam.
`Construction.processCollectedRedefineCalls()` validated the combined retained
serialization view but proceeded directly into label/XML mutation and rebuild.
No later code supplied the omitted authorization, so the commit guard correctly
rejected the transaction.

The correction keeps the existing mechanism and ordering:

```text
collect prepared transactions
-> validate combined retained serialization view
-> authorize each prepared transaction against current host/graph/runtime state
-> begin host mutation
-> rebuild and commit atomically
```

`Construction.replaceInternal()` no longer authorizes a participating
transaction early when collection is active. The batch processor authorizes the
complete prepared set immediately before setting its host-mutation boundary.
Any authorization failure therefore enters the existing pre-mutation collected
rollback/abandon path; it cannot publish a partial batch.

## Preserved contracts

- RETAIN/FRESH/REJECT remains provider-owned.
- Durable semantic identity remains authoritative; Java instances may be
  reconstructed by XML rebuild.
- The complete stable-role group and candidate DAG remain validated before
  publication.
- Successful retained rebuilds keep the approved durable IDs and revisions.
- Revision overflow, stale assessment, invalid context, incompatible proposals
  and incomplete groups continue to fail closed.
- Collected rollback remains all-or-nothing and restores the operation-entry
  construction/identity graph when host mutation has begun.

## Validation route

Focused validation uses the unchanged failing tests, complete relevant G9A1/G9A3
lifecycle classes and the canonical `post-g9u1-a3.narrow` selection. The required
immutable-candidate gate is `PHASE -Phase POST-G9U1-A3`, whose current registry
selection consists of `compile.shared.semantic` and
`junit.shared.post-g9u1-a3.semantic`. The final candidate identity and PHASE
receipt are reported by the verification run rather than predicted here.

No FINAL, inventory regeneration, workspace reconciliation, algebra-gesture
investigation, publication or product-phase transition is part of this candidate.

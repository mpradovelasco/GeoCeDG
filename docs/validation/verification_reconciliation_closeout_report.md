# Verification reconciliation closeout

- Status: **PASS — AUTHOR APPROVED**
- Date: 14 September 2026
- Scope: independent verification and CI maintenance; not a product phase
- Published entry: `c51c9308003d0563123b8fc13f521115f893aa50`, tree
  `dbac2b6d0b5477e6ff87ad8e6feecce3104698b5`
- Preserved reconciliation: `52baac55562b14a7e75fa8afad9defa479a8cfa3`,
  tree `5b9428f7b00b0114e57818df4e9cc2767b2f8210`
- Approved redefine correction: `3654b7bd2bb82bee0257cbf7105445fd8130ae53`,
  tree `2fd7f0381d1cf0a71c016a5910da050b05283258`
- Reviewed technical candidate: `5531239d25844a74da6545026e8f399e86630062`,
  tree `3302ec16414d62f26183f3a97a4e31560344fb53`
- `selfApproved=false`

## Author decision

The author approves both bounded results:

```text
REDEFINE CURRENTNESS DEFECT — PASS — AUTHOR APPROVED
VERIFICATION RECONCILIATION — PASS — AUTHOR APPROVED
selfApproved=false
```

This is an operational closeout. It changes no CeDG semantics and starts no
product phase. `PRE-G9B-S4 — PASS — AUTHOR APPROVED` remains unchanged. The next
product gate remains `PRE-G9B-D1 — DESIGNED / NOT YET AUTHORIZED`.

## Reconciled maintenance perimeter

The reviewed candidate contains the completed bounded work without reopening
the accepted investigations:

1. GitHub Actions launches Gradle with JDK 22, retains JDK 17 and 25 as
   toolchains, and materializes Gradle 9.4.1 before the read-only
   `workstation.live-safety` check.
2. Historical SplineV2 `Length(S,A,C)` expectations follow the approved
   POST-G9U1-A1 provenance contract while retaining real provenance negatives.
3. G9A1/G9A3 assertions no longer treat internal instrumentation or Java
   reference equality as durable semantic identity.
4. The collected/batch redefine path performs final currentness authorization
   before authoritative mutation. This productive correction is exactly
   `3654b7bd2bb82bee0257cbf7105445fd8130ae53` and is separately author approved.
5. `G9U1AlgebraGestureEditingTest` uses deterministic event/EDT synchronization
   instead of timing-sensitive polling, without changing observable behavior.
6. The workspace verification contract recognizes the two POST-G9U1-A7 actions,
   `navigation.zoom-factor-in` and `navigation.zoom-factor-out`: the live profile
   therefore remains 11 families, 18 clusters and 112 actions.
7. Canonical dry-run discovery and the official inventory updater reconcile the
   current JUnit inventory. No count, identity or hash was hand-edited.
8. Packaging repository safety excludes only its explicit, contained task output
   root from the protected snapshot, so task-owned FINAL evidence is not reported
   as a product mutation.

No product/kernel change was made after the approved redefine correction.

`GUIDE_IMPACT=NO_CHANGE`: this closeout adds no user-facing capability, command,
workflow or semantic rule, so the user and developer guides require no update.

## Preserved historical evidence

Rejected and partial results remain truthful historical evidence. In particular,
this closeout does not overwrite or reinterpret:

- the initial bootstrap-related FINAL rejection;
- the post-bootstrap rejected FINAL;
- the first closing FINAL,
  `verification-6b771063403543e683abece0e6739d1e`, rejected because of the stale
  isolated-test identity and repository-safety bookkeeping;
- provisional INFRA_UNIT `verification-2fd6d3575ec342ec8cafc00ffab965e3`,
  rejected by pending static-hash bookkeeping;
- any partial reconciliation receipt or focused failing evidence.

Accepted INFRA_UNIT runs are successor evidence, including
`verification-632e8b19680e4870b1d0bba69df026cf` on the pre-FINAL candidate and
`verification-6b2ebf03a5bb4d58b2fa3b45d7148c37` on the final technical candidate.
Both are `ACCEPTED / COMPLETE` with zero diagnostics.

## Accepted replacement FINAL

The single author-authorized replacement FINAL is authoritative:

| Field | Value |
|---|---|
| Run ID | `verification-403d5931a36d488aa8df153b2a255da2` |
| Candidate | `5531239d25844a74da6545026e8f399e86630062` |
| Tree | `3302ec16414d62f26183f3a97a4e31560344fb53` |
| Plan hash | `89cf112e7d4f84c4972da2a163f871bbb42119f38d9e2a176837e2addfa7a1f1` |
| Result hash | `a53073b56fec293406b002bb92c0f09c30ad4762ef1956c2322549c269623085` |
| Acceptance / coverage | `ACCEPTED / COMPLETE` |
| Acceptance checks | 40/40 satisfied; 0 violated, 0 untrusted, 0 not run |
| Shared JUnit | 6675 tests; 0 failures, 0 errors, 10 skips |
| Desktop broad JUnit | 1440 tests; 0 failures, 0 errors, 1 skip |
| G9U1 isolated JUnit | 5 tests; 0 failures, 0 errors, 0 skips |
| Revision3 diagnostic JUnit | 1 test; 0 failures, 0 errors, 0 skips |
| Diagnostics | 8 clear, 1 finding, 1 unavailable; non-blocking by contract |

The result schema, deterministic result hash, candidate commit and candidate tree
were rechecked read-only after execution. The worktree and index remained clean.
No FINAL, PHASE, COMPOSED, FULL or INFRA_UNIT was rerun during documentary
closeout.

## FINAL receipt observation

The accepted FINAL run produced valid, reproducible `verification-result.json`
evidence but did not produce a separate artifact conforming to
`verification-receipt.schema.json`. Therefore `phase-closeout.ps1` was not used,
because the current FINAL flow has no canonical producer for the required
receipt/checker/input identities. No receipt or hashes were fabricated.

This is a non-blocking operational observation and bounded future
verification-infrastructure debt. It is not a product defect, does not invalidate
the accepted FINAL, and is not repaired or redesigned by this closeout.

## Publication and boundary

This documentary closeout may be fast-forward published with its exact ancestor
chain. It authorizes no tag and no product work. `PRE-G9B-D1`, P1, G9B, G9C,
G9U2, further G12 and productive G10 remain unauthorized.

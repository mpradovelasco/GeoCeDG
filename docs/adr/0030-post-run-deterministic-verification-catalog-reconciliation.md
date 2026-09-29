# ADR 0030: Post-run deterministic verification-catalog reconciliation

- Status: **Proposed — GOV-R technical bootstrap**
- Date: 2026-09-29
- Scope: operational/verification governance; `PRODUCT_PHASE_EFFECT = NONE`
- Normative contract: [verification levels §11.3](../../geocedg/specs/operations/verification-levels.md#113-post-run-deterministic-catalog-reconciliation)
- Receipt schema: [`verification-catalog-reconciliation-receipt.schema.json`](../../geocedg/specs/operations/verification-catalog-reconciliation-receipt.schema.json)
- Related: [ADR 0024](0024-verification-input-identity.md) (input identity and the §11.2 repair),
  [ADR 0025](0025-commit-first-acceptance-and-dual-closeout.md) (commit-first acceptance)

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This record carries no approval of its own. Acceptance is recorded only by a
separate author-decision record naming the exact adoption commit.

## Context

A `FINAL` campaign executes the scientific and product work once on an immutable
candidate `T`. Its acceptance also depends on derived verification metadata that
describes that work, for example the expected JUnit identity set of a tracked
selection in `verification-junit-inventory.json`. When that metadata is stale,
the verifier correctly reports the executed identities as untrusted, the run is
`REJECTED_VERIFICATION_CORE / UNTRUSTED` and no receipt exists, even though every
producer completed and every executed test passed.

Before this decision the only remedy was a corrective descendant plus a second
heavy `FINAL` that re-executes identical code, tests and tasks. The existing
evidence-preserving repair of §11.2 does not apply: it requires prior *successful*
sealed heavy evidence and cannot be used for a deliberate change of verification
policy or closeout.

The first occurrence is the `PRE-G9B-R3-X1` campaign
`verification-ecdb8e4845064b46af2ec1df49963418`: the candidate added tests, the
`final.shared` selection was refreshed from discovery evidence only, and its
tracked expected set stayed at the base cohort.

## Decision

1. **Two different facts.** The scientific/product execution result of a run is
   distinct from the consistency of the derived acceptance metadata that
   describes it. Only the second may be repaired after the run, and only when it
   is a deterministic function of the run's own authenticated evidence.
2. **Historical truth is immutable.** A rejected source run stays rejected. It is
   never relabelled, re-hashed, given a `FINAL` receipt or made closeout
   consumable. The mechanism emits a separate authority, a
   `GEOCEDG_POST_RUN_CATALOG_RECONCILIATION` receipt, that composes the source
   run with a reconciliation descendant `R` of `T`.
3. **Narrow, fail-closed eligibility.** Version 1 supports exactly one reason,
   `STALE_DERIVED_JUNIT_SELECTION_INVENTORY`, and exactly one reconciled
   selection. Every condition R1–R18 of §11.3 is established mechanically by
   `tools/agent/reconcile-verification-catalog.ps1`; a caller-supplied Boolean,
   count or hash is never accepted. Any other rejection cause, any failing or
   missing execution, any change to executed code, tests, build, tasks, filters,
   tolerances, references, toolchain, verifier execution modules or the heavy
   execution plan, or any mutation of source evidence rejects reconciliation.
4. **The catalog is re-derived, not edited.** The corrected catalog must equal
   the output of the repository's official inventory updater applied to `T`'s
   tracked inventory, `T`'s authenticated discovery evidence and the source run's
   own executed selection evidence (`-SelectionEvidencePath`), and the registry
   pin must equal its canonical-text hash. The verifier's own JUnit projection is
   replayed on the archived evidence: with `T`'s selection it must reproduce the
   historical untrusted outcome and cause, with `R`'s selection it must be
   `CONTRACT_SATISFIED / COMPLETE`.
5. **Closeout consumes one of two routes.** `phase-closeout.ps1 -Action INSPECT`
   accepts either an ordinary accepted `FINAL` receipt (unchanged) or an accepted
   reconciliation receipt together with the exact reviewed technical commit `T`
   (`-CandidateCommit`) and reconciliation commit `R`
   (`-ReconciliationCommit`). The second route revalidates the complete
   reconciliation read-only and requires the receipt identity to be reproduced;
   an approved commit must be `T`, never `R`. The historical source result is not
   a receipt and is rejected as one.
6. **Bootstrap.** This adoption is the one-time
   `BOOTSTRAP_EXCEPTION = POST_RUN_CATALOG_RECONCILIATION_INITIAL_ADOPTION`
   authorized by the author, with `PRODUCT_PHASE_EFFECT = NONE`,
   `SCIENTIFIC_CONTRACT_CHANGED = false`, `PRODUCT_SEMANTICS_CHANGED = false`,
   `PREVIOUS_EVIDENCE_REINTERPRETED = false` and
   `HEAVY_CAMPAIGN_RERUN_REQUIRED_FOR_BOOTSTRAP = false`. It narrowly supersedes,
   for this adoption only, the §11.2 sentence requiring a fresh `FULL` for a
   deliberate change of verification policy or closeout. The adoption commit `G`
   is a direct child of the executed candidate `T`, and the first application
   `R` is a direct child of `G`. A bootstrap receipt records
   `bootstrapAdoption = true` and binds `G`. After publication the mechanism is
   an ordinary part of `T`: a later use permits no change of the mechanism
   between `T` and `R`.
7. **The heavy plan is untouched.** The reconciler is a separate post-run
   consumer. It does not modify `verify.ps1`, the supervisor, the registry
   nodes, any producer or projection, and its focused suite is not registered as
   a `FINAL` leaf. Registering it would change the `FINAL` execution plan, so it
   is deferred to the next separately authorized verification-infrastructure
   change, whose normal `FINAL` will execute it.

## Consequences

- A stale derived selection inventory no longer forces a redundant heavy
  campaign when all heavy execution already succeeded; the accepted technical
  evidence is the explicit pair *source heavy execution* plus *reconciliation
  receipt*, and neither is ever presented alone as the other.
- A new heavy campaign remains mandatory whenever what is executed, selected or
  judged could differ.
- Future author closeouts must name both `T` and `R` for this route.
- The mechanism introduces no product, scientific, serialization or build change.

## Rejected alternatives

| Alternative | Reason |
|---|---|
| rerun `FINAL` on a corrective descendant | redundant heavy execution of unchanged work; remains the fallback when eligibility fails |
| relabel or re-hash the source result as accepted | falsifies historical evidence |
| issue an ordinary `FINAL` receipt for `R` | no `FINAL` executed `R`; a fabricated receipt |
| hand-enter the corrected count and hash | not derived evidence; forbidden |
| extend §11.2 | §11.2 requires prior successful heavy evidence and excludes policy changes |
| general post-run repair of any verifier failure | unbounded; only one deterministic metadata class is admitted |
| use `AUTHOR_DIRECT` | its contract forbids new scripts, validators, receipts and gates |
| add the reconciler inside `verify.ps1` | would change the heavy verifier that executed the source run |

## Approval boundary

This ADR is a technical bootstrap candidate. It authorizes no product phase, no
push, promotion, tag or release, and no use of the mechanism beyond its immediate
application to `PRE-G9B-R3-X1` authorized by the same author instruction.

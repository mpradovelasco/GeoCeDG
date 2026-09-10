# GeoCeDG change review

<!-- geocedg-field: objective -->
## Objective

Review the exact candidate without changing it.

<!-- geocedg-field: implementation_base -->
## Implementation base

Record the candidate commit/tree and its exact parent before interpreting the diff.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Inspect the declared candidate, its governing sources and saved evidence.

<!-- geocedg-field: forbidden_scope -->
## Forbidden scope

Do not edit, publish, reinterpret missing evidence or expand the reviewed cohort.

<!-- geocedg-field: required_checks -->
## Required checks

Apply the review dimensions below and identify every check actually inspected.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Review is read-only unless a separate exact implementation authorization is supplied.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Review never authorizes commit, push, merge, tag or promotion.

Read `AGENTS.md`, the task prompt, governing specifications/ADRs, and the real
diff. Review facts before recommendations.

Check, in order:

1. architectural placement and source authority;
2. geometric invariants, degeneracies, dependency propagation, and exactness;
3. serialization, migration, legacy behavior, and feature maturity;
4. upstream diff size and unrelated changes;
5. deterministic tests, regression cases, budgets, and executable evidence;
6. manifest/schema consistency and generated-versus-source boundaries;
7. licensing, provenance, assets, and modified-upstream records;
8. prompt duplication or commands that bypass `tools/agent/`;
9. required DEV/PHASE/COMPOSED/FULL scope, retained scientific/persistence and
   non-test assertions, and current-run evidence freshness under
   `geocedg/specs/operations/verification-levels.md`;
10. substantive bootstrap-impact rationale, verification-infrastructure impact,
    required FULL evidence and existing `GUIDE_IMPACT`; a declaration alone is
    not compliance and a narrower PASS does not excuse a missing required gate;
11. for final acceptance, exact clean technical commit `T`, mode-neutral
    pre-campaign `CLOSEOUT_READINESS` validating both routes and comparing the
    real PHASE/COMPOSED/FULL plans, then one clean FULL root bound to that
    receipt/SHA with `closeoutMode=null`, `closeoutConsumable=true` and
    authenticated nested-PHASE, subsumed-COMPOSED and physical-FULL claims;
    reject invented independent lower runs, any schema-v2 uncovered obligation,
    unproved FULL subsumption and
    dirty/precommit reattribution;
12. exact distinction between `AUTHOR REVIEW READY` (product prepared for
    review), `AUTHOR CLOSEOUT READY` (clean `T`, valid consumable acceptance
    evidence, one explicitly selected post-approval route and PREPARE-valid
    binding to readiness), the separate
    author review/smoke gate and explicit approval, including
    `selfApproved=false`; and
13. for `AUTHOR_OPERATED`, proof that preparation did not commit, tag, promote
    or push, plus post-promotion audit of exact ancestry/delta, unchanged
    executable inputs, annotated-tag target, linear fast-forward, clean state,
    exact approved SHA, technical evidence still attributed to `T`, and
    local/tracking/live-remote agreement.

Report actionable findings by severity with exact paths and lines. Distinguish
production defects from environment, sandbox, stale-test, unavailable-runtime,
and documentation failures. If no finding remains, state the checks performed
and residual risks; do not claim unexecuted gates passed.

Treat ADR 0024 section 11.2 as an exceptional conjunctive repair only. Reject
its use as normal lifecycle/closeout or as a waiver of FULL for a deliberate
verification-policy, readiness, classification or closeout-methodology change.

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
9. required development/PHASE/INTEGRATION/FINAL scope, retained
   scientific/persistence and non-test assertions, and current-run evidence freshness under
   `geocedg/specs/operations/verification-levels.md`;
10. substantive bootstrap-impact rationale, verification-infrastructure impact,
    required FINAL evidence and existing `GUIDE_IMPACT`; a declaration alone is
    not compliance and a narrower PASS does not excuse a missing required gate;
11. for final acceptance, an exact clean immutable candidate, a complete FINAL
    plan, exactly one authorized FINAL campaign, complete trusted coverage and a
    receipt bound to the exact commit/tree, plan, checker, portable environment,
    inputs and evidence; reject dirty/precommit reattribution and any attempt to
    infer acceptance from diagnostics or a mixed wrapper;
12. exact separation of technical acceptance, author review and explicit
    exact-SHA approval; and
13. for closeout and promotion, proof that identity inspection ran no checker
    and changed no Git state, plus explicit authorization and live-remote
    identity for any separate non-force fast-forward promotion.

Report actionable findings by severity with exact paths and lines. Distinguish
production defects from environment, sandbox, stale-test, unavailable-runtime,
and documentation failures. If no finding remains, state the checks performed
and residual risks; do not claim unexecuted gates passed.

Treat historical ADR closeout designs as history, not executable authority.
Reject their use to bypass the current registry, receipt identity or explicit
publication boundary.

# GeoCeDG change review

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
    not compliance and a narrower PASS does not excuse a missing required gate.

Report actionable findings by severity with exact paths and lines. Distinguish
production defects from environment, sandbox, stale-test, unavailable-runtime,
and documentation failures. If no finding remains, state the checks performed
and residual risks; do not claim unexecuted gates passed.

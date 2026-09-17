# PRE-G9B-R7 — integrated PRE-G9B readiness and closeout

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

**Additionally dependent on closure, or explicit author disposition, of all
authorized prior `PRE-G9B-R` work.** At the time of writing, no `PRE-G9B-R`
phase after `R0` has been authorized, so this phase has no prerequisites
satisfied.

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R7` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
DEPENDS_ON               = closure or explicit author disposition of every
                           authorized prior PRE-G9B-R phase
```

<!-- geocedg-field: objective -->
## Objective

Integrate the track, reconcile the roadmap and debt records, and place an
explicit author decision about what follows.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS` at phase start
from the **actual integrated scope**: `INTEGRATION` at minimum, and `FINAL` if
any authorized phase changed global verification infrastructure.

This phase **must not declare that `G9B` necessarily follows.** It ends with an
explicit author decision about what comes next.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
AUTHORIZED_PRIOR_PHASES = UNRESOLVED — MUST BE ENUMERATED AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace these fields with the exact commit and tree the author explicitly
authorizes, and with the exact enumeration of which prior `PRE-G9B-R` phases were
authorized, closed or explicitly dispositioned. A moving branch name is not
sufficient. **Execution is forbidden while either field remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; the closeout evidence of every authorized prior `PRE-G9B-R` phase;
`docs/roadmap/geocedg_roadmap.md`;
`geocedg/specs/operations/verification-levels.md` and
`geocedg/specs/operations/documentation-maintenance.md`; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
and the
[R0 disposition matrix](../../../docs/validation/pre_g9b_r0_technical_debt_disposition_matrix.md)
as the record of what the track set out to do.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Integrated verification of the accumulated candidate, scoped to the work that
  was **actually authorized and closed**, not to the full designed sequence.
- Reconciliation of every debt record opened, retained or closed by the track:
  `TD-P1-SEMANTIC-LENGTH-INTERSECTION`, `TD-P1-PACKAGING-SUMMARY`,
  `TD-VERIFY-RECEIPT-RECOVERY`, `TD-G9X1-HISTORICAL-PIN`,
  `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`,
  `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`,
  `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`,
  `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` and `TD-R0-G9U1-Q02-BINDING`.
  Each must end with a **terminal disposition or an explicit retained owner**.
- Exact roadmap reconciliation, stating the true status of each phase.
- The readiness statement.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**No new capability.** No publication, tag or release. No moving any risk record
to resolved without an explicit author decision. **No authorization of `G9B`**,
and no statement that `G9B` necessarily follows. Do not mark anything approved by
agent action. Do not touch `G9C`, `G9U2`, productive `G10`, further `G12`,
`PROFILE COMMERCIAL` or exact DXF `SPLINE`. Do not contact GeoGebra or
OpenGeoProver.

## Architectural placement

Verification, documentation and roadmap. This phase produces no product
capability and no geometric semantics.

## Required design/specification

Roadmap and traceability reconciliation only. No new normative contract is
created by this phase.

## Geometric invariants and degeneracies

**Not applicable.** This phase changes no geometry, kernel semantics, dependency
graph, serialization or metric behaviour. It reports on work already closed under
its own contracts.

## Compatibility and serialization

No document format, persisted identity or serialization change. Historical
evidence, receipts and approvals retain their original cohort, meaning and
consumability, and must not be relabelled, inherited or reinterpreted during
reconciliation.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The accumulated focal suites of the authorized phases. Acceptance: `INTEGRATION`
at minimum; `FINAL` if any authorized phase changed global verification
infrastructure. Then `git diff --check` and one clean immutable candidate commit.

Report the exact command, exit code, report path, acceptance verdict, coverage
verdict, diagnostic count and execution identity for every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R7` and its
exact implementation base, and requires every authorized prior phase to be closed
or explicitly dispositioned.

Closing this phase authorizes **nothing** that follows it. `G9B`, `G9C`, `G9U2`,
productive `G10`, further `G12`, `PROFILE COMMERCIAL`, exact DXF `SPLINE`,
publication and commercial distribution all remain separate explicit author
decisions. No tag, release, publication or commercial action is implied by this
prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA. Final acceptance evidence never grants publication
authority by itself.

## Acceptance and closeout

The phase stops with a candidate pending author review and **must end with an
explicit author decision about what follows**. **Author smoke is required.**
Author approval is an explicit decision naming the exact accepted commit; nothing
is marked approved by the agent.

## Required artifacts

A readiness and closeout report under `docs/validation/`; the reconciled debt
record with a terminal disposition or named retained owner for every item;
roadmap updates stating the exact status of each phase; machine-readable evidence
under `geocedg/validation/`; the exact required-level commands with exit codes and
log paths; bootstrap- and infrastructure-impact outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when an open debt item has no owner; when an
author decision from the `PRE-G9B-R0` decision list remains unresolved; when
integrated verification cannot complete or is untrusted; when reconciliation
would require relabelling historical evidence; or while `IMPLEMENTATION_BASE` or
`AUTHORIZED_PRIOR_PHASES` remains `UNRESOLVED`.

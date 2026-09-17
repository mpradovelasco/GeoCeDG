# PRE-G9B-R1-R1 — native periodic-quarantine round-trip closure

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R1-R1` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Supply the missing real native `.cedg` lifecycle evidence for
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`, and reconcile the overclaimed
`U1-Q02` manifest binding recorded as `TD-R0-G9U1-Q02-BINDING`.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze
`VERIFICATION_CLASS = BOUNDED_PHASE` before implementation begins, with
`frozenAtPhaseStart=true`; the frozen acceptance is
`tools/agent/verify.ps1 -Profile PHASE -Phase G9U1`.

`AD-R0-3` is author-decided: plan closure in this refinement using **genuine
native `.cedg` lifecycle evidence**, and avoid widening the kernel API solely to
enable a test where durable lifecycle evidence can establish the property.

This refinement supplies evidence and a disposition proposal only. **Only the
author may move the canonical risk record to resolved.**

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace this field with the exact commit and tree the author explicitly
authorizes for this phase. A moving branch name is not sufficient. **Execution
is forbidden while this field remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; current source and the existing Desktop periodic-quarantine native
test; the G9U1 scenario and lifecycle manifests under `geocedg/validation/`; the
closure fixture specified in
[the G9U1 command/tool consistency matrix](../../../docs/validation/g9u1_command_tool_consistency_matrix.md);
`geocedg/specs/ui/native-document-identity.md` and the accepted native-document
ADR; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
and the
[R0 disposition matrix](../../../docs/validation/pre_g9b_r0_technical_debt_disposition_matrix.md)
as planning input only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Two additional test methods in the existing periodic-quarantine native
  Desktop test: **proved-nonzero-offset retirement** (`U1-Q04`) and the
  **feature-off / Classic preservation boundary** (`U1-Q05`), each forking a
  byte-identical reopened archive.
- A **second unresolved quarantined round trip** so that the declared `U1-Q02`
  procedure — reopen the native quarantined document twice while offset evidence
  remains insufficient or non-unique — is actually performed by the test bound
  to it.
- Manifest reconciliation for `U1-Q02`, `U1-Q04` and `U1-Q05`.

The byte-identical seed-forking pattern the fixture requires already exists in
an earlier native-archive test, where one seed document is written once and then
loaded independently by two separate applications. A layout-independent evidence
style — asserting ledger-state equality across a real save/reopen rather than
hand-parsing the serialized string — already exists in the spline-pair native
archive test and is preferred.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No XML fabrication as a substitute for a real archive lifecycle. No ledger-only
export/import substitute. No change to quarantine semantics, the resolver or the
ledger format. No kernel API widening merely to ease a test unless separately
authorized. Do not move the canonical risk record to resolved. Do not touch
`R2`, `R2-E0`, `R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10` or
further `G12`. Do not publish, tag, release or push a publication ref.

## Architectural placement

Desktop test plus operational manifest. **No kernel semantics change.** The
quarantine mechanism is already complete in the kernel; this refinement observes
it through a real document lifecycle.

## Required design/specification

None new. The closure fixture and its mandatory steps are already specified in
the G9U1 command/tool consistency matrix, which states that the risk is closed
only by an actual native-archive lifecycle and that ledger export/import, copy
and non-periodic dormant/reactivated archive tests remain supporting evidence
that cannot substitute for it.

## Geometric invariants and degeneracies

Periodic-domain identity is the subject under test, not a target of change.
Preserve the existing quarantine semantics exactly: durable, non-current,
persisted ledger state; fail-closed phase-tube certification; a later unique
cyclic offset zero releasing or reusing that exact group; a proved unique
nonzero offset permanently retiring only its affected group; no offset or
multiple offsets leaving the group quarantined.

## Compatibility and serialization

Every assertion must go through a real save and reopen of a real `.cedg` in a
fresh application. Saved coordinates are never sufficient evidence. The ledger
format is not changed, and no migration is introduced.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The two new methods plus the reconciled `U1-Q02` path, dispatched by the
existing G9U1 verifier — note that the `--tests` dispatch sits in the
non-skip-build branch, so do not invoke it with `-SkipBuild` and claim runtime
acceptance. Then `git diff --check`, one clean immutable candidate commit, and
`tools/agent/verify.ps1 -Profile PHASE -Phase G9U1`.

Report the exact command, exit code, report path, acceptance verdict, coverage
verdict, diagnostic count and execution identity for every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R1-R1` and
its exact implementation base.

Passing evidence does **not** resolve
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`; that remains an explicit author
disposition after the evidence exists. Global `G9` closeout is not authorized.
Successor phases remain unauthorized. No tag, release, publication or commercial
action is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a candidate pending author review, presenting the evidence
and a disposition proposal for the risk record. Author approval is an explicit
decision naming the exact accepted commit.

## Required artifacts

A candidate report under `docs/validation/`; updated G9U1 scenario and lifecycle
manifest entries for `U1-Q02`, `U1-Q04` and `U1-Q05`; the exact required-level
command with exit code and log path; the bootstrap- and infrastructure-impact
outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when the quarantine observation surface cannot
be reached without widening a deliberately resolver-scoped kernel API — that
widening is its own author decision, not a refactor; when a real native archive
lifecycle cannot be exercised; when any substitute evidence would have to stand
in for a genuine round trip; or while `IMPLEMENTATION_BASE` remains
`UNRESOLVED`.

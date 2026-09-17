# PRE-G9B-R1 — verification and bounded operational debt

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R1` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Close `TD-VERIFY-RECEIPT-RECOVERY`, `TD-P1-PACKAGING-SUMMARY` and
`TD-G9X1-HISTORICAL-PIN` exactly as disposed by `PRE-G9B-R0`, without changing
any product, kernel or geometric behaviour.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze
`VERIFICATION_CLASS = OPERATIONAL_VERIFICATION_INFRASTRUCTURE` before
implementation begins, with `frozenAtPhaseStart=true`. Because receipt emission
is global verification infrastructure, the frozen acceptance is focused
operational evidence **plus FULL**.

`AD-R0-2` is author-decided: this phase owns resolving the semantics and
production of the three caller-supplied receipt inputs that have no producer
today — the checker identity hash, the input identity hash and the
accepted-profiles set — reusing existing acceptance semantics, without creating
a second acceptance system and without fabricating any hash.

The refinement `PRE-G9B-R1-R1` carries the
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` closure. It is a Desktop product
test under the `G9U1` perimeter, carries its own `BOUNDED_PHASE` class, and is
**not** part of this prompt.

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

`AGENTS.md`; current source, build and verification configuration;
`geocedg/specs/operations/verification-levels.md`,
`geocedg/specs/operations/verification-registry.json`,
`geocedg/specs/operations/verification-receipt.schema.json` and
`geocedg/specs/operations/verification-static-contracts.json`; the accepted
operational ADRs; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
and the
[R0 disposition matrix](../../../docs/validation/pre_g9b_r0_technical_debt_disposition_matrix.md)
as planning input only. R0 planning prose does not become an accepted
specification by being referenced here.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Three declared slices, each independently reportable.

1. **Receipt emission.** Wire the existing, already-gated acceptance-receipt
   writer into the accepted-FINAL path, and add producers for the three
   caller-supplied receipt inputs that have no producer today. The receipt
   factory's acceptance gating is already complete — it refuses non-completed
   runs, non-accepted acceptance, incomplete coverage, an invalid result hash,
   any untrusted or not-run check, a coverage/acceptance mismatch and a profile
   set that does not contain the report profile, and the writer refuses to
   overwrite — so **no new acceptance logic is to be written**.
2. **Packaging summary.** Derive the final package-summary redistribution line
   from the selected distribution profile instead of the historical global
   sentence, using the profile-derived flag the same script already computes for
   the build manifest.
3. **G9X1 historical pin.** Reconcile the one stale pin by recording a named
   historical constant bound to the frozen tag and verified from the tag blob, a
   separate live-successor pin, and an explicit supersession record naming the
   superseding commit and `PRE-G9B-S1-R1`. The historical authority is
   **retained**, never replaced.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Do not fabricate any receipt, hash or identity. Do not substitute the historical
G9X1 hash. Do not reopen `G9X1`. Do not change acceptance semantics, gating,
coverage, severity classification or exit-status meaning. Do not change any
kernel, geometric, serialization or product behaviour. Do not change packaging
composition, licensing disposition or the distribution profile set. Do not touch
`R1-R1`, `R2`, `R2-E0`, `R2-E1`, `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive
`G10` or further `G12`. Do not publish, tag, release or push a publication ref.

## Architectural placement

External tooling and verification only. Receipts, pins and reporting are
evidence, never source authority, and never translate an environment or
permission failure into product code.

## Required design/specification

Amend `geocedg/specs/operations/verification-levels.md` only if the receipt
emission point changes the documented contract. Record the G9X1 supersession
relation in the verifier and its evidence record without rewriting historical
evidence.

## Geometric invariants and degeneracies

**Not applicable.** This phase is operational. Geometry, kernel semantics, the
dependency graph, intersection identity and metric behaviour remain unchanged.

## Compatibility and serialization

No document format, persisted identity or serialization contract changes.
Historical receipts and results retain their original cohort, meaning and
consumability, and must not be relabelled, inherited or reinterpreted.

Known couplings that must move atomically in the same candidate: the packaging
script's SHA-256 is pinned by the static-contract catalog; the registry pins that
catalog and fails closed for *every* profile if either drifts; and two existing
desktop tests assert the current summary wording in living documents.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused first: extend the existing receipt test module with a positive emission
path and a negative path asserting that a non-FINAL or non-accepted run emits
nothing; a packaging-summary derivation test; a real NC package build for the
summary line; the G9X1 verifier.

Then `git diff --check`, one clean immutable candidate commit, complete
`FINAL -PlanOnly` resolution, and exactly one authorized
`tools/agent/verify.ps1 -Profile FINAL -LogDirectory <new external root>`
campaign. Report the exact command, exit code, report path, acceptance verdict,
coverage verdict, diagnostic count and execution identity for every run.

Invoke the PowerShell verifier through PowerShell 7.2 or later; there is no
POSIX variant.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R1` and its
exact implementation base; silence or general agreement is not authorization.

`R1-R1`, `R2`, `R2-E0`, `R2-E1`, `R3`–`R7` and every later gate remain
unauthorized regardless of this phase's outcome. No tag, release, publication or
commercial action is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA. Final acceptance evidence never grants publication
authority by itself.

## Acceptance and closeout

The phase stops with a candidate pending author review. Author approval is an
explicit decision naming the exact accepted commit; neither a report nor a
receipt creates it. Closeout inspects identity only and never reruns acceptance,
mutates Git or records approval.

## Required artifacts

A candidate report under `docs/validation/`; machine-readable evidence under
`geocedg/validation/`; the bootstrap-impact outcome with rationale and affected
paths; the infrastructure-impact assessment; the exact required-level command
with exit code and log path; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when a receipt input has undefined semantics;
when any step would require synthesizing a hash; when the static-contract repin
and the registry catalog pin cannot move atomically with the packaging change;
when the required FULL campaign cannot complete or is untrusted; when closeout
tooling would have to mutate Git; or while `IMPLEMENTATION_BASE` remains
`UNRESOLVED`.

# PRE-G9B-R6 — GGBScript compatibility gate

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-01 names `PRE-G9B-R6`, its exact
implementation base and the published `PRE-G9B-R5-B` closeout it depends on. That
instruction also authorizes this governance-layer amendment as part of `R6`. The
amendment replaces the proposed future stub (blob
`e1da5f3b4f0dcc9139b19788fdb1dd4f03138de6`), created under `AD-R0-9` from the
author-approved `PRE-G9B-R0` closeout, whose base and naming-policy fields were
unresolved. It keeps the stub's technical scope and intent. This file is an
execution contract, not a second policy document.

```text
implementationAuthorized = true
authorApproved           = false
passClaimed              = false
selfApproved             = false
```

`authorApproved = false` means that the technical candidate this phase produces
has not been author-approved. Technical verification never creates author
approval. The phase stops with one exact technically verified candidate pending
author review.

<!-- geocedg-field: objective -->
## Objective

Prove, reproducibly, that GeoCeDG commands work through the **real script path**,
and record the result as machine-readable authority.

Compatibility must never be inferred from the fact that a command works in
Algebra Input:

```text
Algebra Input success != evidence of GGBScript compatibility
```

```text
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = OPERATIONAL_VERIFICATION_INFRASTRUCTURE
frozenAtPhaseStart  = true
```

Before implementation, classify and freeze
`VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL | GLOBAL` under the live
verification contract (`geocedg/specs/operations/verification-levels.md`
section 12.8, the typed registry and `tools/agent/verify.ps1`):

- `PHASE_LOCAL`: `R6` registers and executes its own matrix through the existing
  registry, inventory and checker mechanisms, without changing global verifier
  semantics or profile composition. Planned acceptance is the corresponding
  focused operational evidence plus the registered `PRE-G9B-R6` phase selection.
  `FINAL` is not run.
- `GLOBAL`: the change alters verifier core, registry or profile-composition
  semantics, a globally consumed schema, or otherwise meets the contract's
  definition of a global verification-infrastructure change. Planned acceptance
  is focused operational evidence plus `FINAL` on the exact candidate.

`GLOBAL` is not chosen merely because `R6` is a verification phase. If the
impact widens after implementation begins beyond the frozen plan, stop and
report rather than escalate silently.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
e7401734b4ef76d8f79dd4b0535845304937e4f7          (P_R5B, published R5-B closeout)

IMPLEMENTATION_BASE_TREE =
3ad888b0895d0cb026be60e3d435c0819c4faed8

R5_B_STATE =
PASS — AUTHOR APPROVED — PUBLISHED

R5_B_CLOSEOUT =
e7401734b4ef76d8f79dd4b0535845304937e4f7

R5_B_FINAL_TECHNICAL_CANDIDATE =
a1d96746a42c5ba7fc83d8909647083f11616009
```

A moving branch is not a base. The task is not rebased onto any later commit
without a new author instruction.

Planning authority base, recorded as provenance only and **not** as this phase's
implementation identity: the author-approved `PRE-G9B-R0` closeout, commit
`48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at `P_R5B`: the script runner, the command lookup
   strategies, the command dispatcher and processor factories, the nested
   `Execute` processor, the Desktop Algebra-input submission adapter, the script
   editor model, the command bundles, the existing script-workflow tests and the
   existing schema conventions under `geocedg/specs/operations/`.
3. [ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md),
   `ACCEPTED — AUTHOR APPROVED`, and the
   [`PRE-G9B-R5-B` closeout](../../../docs/validation/pre_g9b_r5_b_closeout_record.md),
   which fix the naming policy the locale dimension consumes. Neither is reopened.
4. Planning input only, re-characterized at `P_R5B` before use: the
   [PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
   §4 (`PRE-G9B-R6` block) and the
   [R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
   §7.1.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- This governance amendment.
- The machine-readable capability matrix and its schema, following existing
  schema conventions.
- The tests that execute the matrix, its completeness gate and the focal
  regressions below.
- Correction of `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`.
- Bounded truthful-failure corrections the gate exposes: minimal, causally
  demonstrated, inside existing semantics and covered by a pre-fix failing
  regression, where the current implementation reports success after a real
  failure, mutates construction state on a refused operation, leaks lookup or
  evaluation state after failure, or violates an already-established
  compatibility contract.
- The phase-local verification registration, inventory selections and input
  pins the matrix needs, through existing mechanisms.

### Inventory and surfaces

Cover every GeoCeDG-**added** and GeoCeDG-**modified** command. The inventory is
re-derived from `P_R5B` source at execution time and never assumed from this
prompt; R0 recorded the added set as `LocusV2`, `LocusLength` and `SplineV2`,
plus modified upstream processors carrying GeoCeDG branches. Adding a GeoCeDG
command without matrix rows must fail validation, and completeness must not
depend on a hand-maintained duplicate command list.

Surfaces: `ALGEBRA_INPUT`, `GGBSCRIPT` and `GGBSCRIPT_EXECUTE` (nested `Execute`,
which forces the XML strategy and is therefore a third regime). The three are
never collapsed because they share dispatch machinery. Locales: at least `en`
and `es`, under the final `R5-B` policy, recording separately the UI locale, the
lookup strategy, the display name, the stored script representation and the
runtime token.

Per applicable row, cover: parsing and name lookup; command-processor dispatch;
ordinary DAG construction; recompute; undo behaviour; native `.cedg` save and
reopen; and truthful failure for unsupported forms.

### Matrix shape

Row identity `(command_id, arity_form, entry_surface, ui_locale)`. `command_id`
is a kernel command enum constant or another already-authoritative identity,
never redeclared. `arity_form` quotes a syntax line from the command bundle.
Each capability carries an explicit typed verdict plus a JUnit identity;
unsupported behaviour is never an empty value or a skipped test. Every
`UNSUPPORTED_TRUTHFUL` row carries an expected error key and a before/after
construction-XML identity assertion. Rows also record the lookup strategy in
force, the input token and the frontend-orchestration expectation.

Reproducibility comes from properties already used in this repository: every
row names a JUnit identity and the matrix is registered so a verification level
executes it; the row set is checked structurally against the derived inventory
crossed with surfaces, locales and applicable arity forms; consumed inputs are
pinned through existing mechanisms; and every unsupported verdict carries an
error key and an XML-identity assertion.

### Lookup regimes under the final `R5-B` policy

`USER`, `SCRIPT` and `XML` stay distinct; they are exposed, not normalized:

- Algebra Input (`USER`): canonical English names are accepted; localized
  aliases stay accepted exactly where the current `USER` lookup accepts them.
- GGBScript (`SCRIPT`): runtime lookup stays canonical English/internal. A raw
  localized token that bypasses editor delocalization is not valid merely
  because the UI is Spanish; where the `SCRIPT` strategy rejects it, the row
  records a truthful typed failure. No localized `SCRIPT` lookup is added.
- Nested `Execute` (`XML`): the real XML strategy, characterized from source
  (exact case, internal or enum-compatible names). XML lookup is not changed to
  make rows uniform.

The script editor boundary set by `R5-B` is tested, not redesigned: displayed
canonical English heads, stored valid script semantics and cross-locale
reopen/run behaviour.

### Planning facts to re-characterize at `P_R5B`

`R0` recorded, as planning input: the script and Algebra-input paths converge on
one command dispatcher and one processor factory; the Desktop Algebra input wraps
the identical processor entry point, adding a redefine branch and, on creation
only, post-creation frontend orchestration and a single undo store; the real
differences are name lookup and case sensitivity, locale exposure of raw
localized tokens, evaluation flags, error handling and undo storage, the nested
`Execute` regime, the swallowed-throwable and strategy-restore defect, and the
frontend-orchestration asymmetry — the opt-in auto-materialize session
behaviour exists only on the explicit human Algebra path, is **off by default**,
and is *not* an explanation of the D1 witness. `R0` also recorded that existing
coverage executes the `R0` GeoCeDG commands through scripts and mechanizes the
feature-off atomic-rejection row with an XML-identity assertion, leaving the
locale dimension, the swallowed-throwable row, the undo row and the
rich-result/auto-materialize row unmechanized. Each statement is confirmed or
corrected from current source and tests before it is relied on.

The matrix records, and never erases, the deliberate asymmetries: Desktop
post-creation orchestration and undo storage on explicit Algebra Input are not
inherited by scripts, and scripts are not made to invoke Desktop orchestration
to obtain equal rows.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**Do not add GGBScript functionality.** Do not change command semantics merely to
make a matrix row pass. Do not declare a second command registry, a second source
of geometric semantics, a replacement dispatcher or a new script language. Do not
assert geometric values that a normative specification or an accepted test oracle
does not already own. Do not add localized `SCRIPT` lookup, change `XML` lookup,
reopen the `R5-B` policy or its accepted `R1`/`R2` corrections, change
serialization, or alter global acceptance, receipt, profile, trust-model,
registry-schema or checker-identity semantics unless the frozen plan requires
it. Do not touch `R7`, `G9B`, `G9C`, `G9U2`, productive `G10` or further `G12`.
Do not publish, tag, release or push a publication ref.

## Architectural placement

Verification and tooling, plus the authorized product corrections. The matrix is
observational verification authority, **not** a second source of command or
geometric truth. The kernel and shared runtime remain authoritative for script
lookup, command dispatch, DAG construction and script execution semantics.

## Required design/specification

A capability-matrix schema and its instance contract, following the existing
schema conventions — a JSON Schema under `geocedg/specs/operations/` with the
instance under `geocedg/validation/`, mirroring the existing static-contract and
inventory conventions.

## Geometric invariants and degeneracies

**Not applicable as a target of change.** The gate observes existing behaviour.
No row may assert a geometric value that a normative specification or accepted
test oracle does not already own, and no command's geometric semantics may be
altered to satisfy the matrix. Semantic identity, DAG structure, Locus V2
branch/component identity, durable tokens, Spline V2 semantics, metric and
intersection provenance, redefine contracts and spatial identity are preserved;
identity is never inferred from labels, coordinates, proximity, ordering or
presentation text.

## Compatibility and serialization

Native save and reopen is a required per-row capability where persistence is
meaningful: the resulting DAG survives `.cedg` save/reopen and recomputes. Every
`UNSUPPORTED_TRUTHFUL` verdict must carry both an error key and an assertion that
the construction XML is identical before and after the attempt, so that
"unsupported" is a verified, atomic, non-mutating refusal rather than an excuse.
The serialization format is not changed.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: matrix schema validation; matrix completeness validation; the matrix
executor; `SCRIPT` lookup locale cases; nested `Execute`/XML strategy cases; the
`TD-R0-GGBSCRIPT-STRATEGY-RESTORE` regression; the swallowed-throwable
truthful-failure regression; undo cases; native save/reopen; unsupported atomic
XML-identity cases; and the rich-result/frontend-orchestration asymmetry. Run the
adjacent script, lookup, XML, persistence and `R5-B` regressions where script
naming or presentation boundaries are involved.

Then Checkstyle, the upstream-boundary validation, the JUnit inventory update
through the official updater where tests change, `git diff --check` and the
closeout-readiness checks current governance requires, and one clean immutable
candidate commit.

Acceptance follows the frozen impact classification: under `PHASE_LOCAL`, the
corresponding focused operational evidence plus
`tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6` on the exact candidate,
with no `FINAL`; under `GLOBAL`, focused operational evidence plus
`tools/agent/verify.ps1 -Profile FINAL` on the exact candidate. Report exact
commands, exit codes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: this amendment; implementation and technical verification of `R6`
on a local branch from `P_R5B`; local commits; and freezing one technical
candidate. Design existence is not execution authorization for anything else,
and technical verification never creates author approval.

`R7` and every later gate remain unauthorized. If a GeoCeDG command that is
expected to be script-reachable cannot be reached through the real script path,
that is a product defect requiring separate author authorization unless it is
exactly covered by `TD-R0-GGBSCRIPT-STRATEGY-RESTORE` or the bounded
truthful-failure scope above.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch, local commits, verification and candidate freeze are authorized.
Push of the working branch, merge, promotion to `main`, rebase, squash, force
push, tag, release, binary publication and any rewrite of published history are
forbidden, and each requires a separate explicit author instruction naming the
exact candidate SHA.

## Acceptance and closeout

The phase stops with one exact candidate pending author review. Author smoke is
not performed on the author's behalf; a concise checklist is prepared, including
a representative script under a Spanish UI that uses canonical English/internal
GGBScript semantics. Author approval is an explicit decision naming the exact
accepted commit.

Frozen candidate artifacts record only facts fixed when written:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

## Required artifacts

The capability-matrix schema and instance; source and focal tests; a candidate
report under `docs/validation/`; machine-readable evidence under
`geocedg/validation/`; the verification registration; the exact required-level
commands with exit codes and log paths; bootstrap- and infrastructure-impact
outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when: an identity of `P_R5B` differs; the
published `R5-B` policy is inconsistent; current source invalidates the assumed
inventory model; a command expected to be script-reachable cannot be reached from
the real script path; a row could only pass by changing geometric or command
semantics; truthful unsupported behaviour needs a broader product redesign;
completeness would need a second command registry; serialization would need to
change; global verification infrastructure would need widening beyond the
frozen plan; governance requires a verification-class change; or a failure
belongs to `R7` or a later phase.

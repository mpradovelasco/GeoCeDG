# PRE-G9B-R6 — GGBScript compatibility gate

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

**Additionally dependent on the `PRE-G9B-R5-B` naming-policy disposition and
implementation state**, because the matrix's locale dimension is defined by it.

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R6` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
DEPENDS_ON               = PRE-G9B-R5-B decision and implementation state
```

<!-- geocedg-field: objective -->
## Objective

Prove, reproducibly, that GeoCeDG commands work through the **real script path**,
and record the result as machine-readable authority.

Compatibility must never be inferred from the fact that a command works in
Algebra Input.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze
`VERIFICATION_CLASS = OPERATIONAL_VERIFICATION_INFRASTRUCTURE` before
implementation begins, with `frozenAtPhaseStart=true`; focused operational
evidence plus the registered phase selection, and `FULL` only if the matrix is
wired into global verification infrastructure.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
R5_B_STATE          = UNRESOLVED — MUST BE RECORDED AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace these fields with the exact commit and tree the author explicitly
authorizes, and with the actual `R5-B` disposition and implementation state the
locale dimension assumes. A moving branch name is not sufficient. **Execution is
forbidden while either field remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; current source for the script runner, the command lookup strategy,
the command dispatcher and processor factory, and the Desktop Algebra-input
submission adapter; the existing script-workflow tests; the existing schema
conventions under `geocedg/specs/operations/`; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§4 (`PRE-G9B-R6` block) and the
[R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
§7.1 as planning input only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- The machine-readable capability matrix and its schema, following existing
  schema conventions.
- The tests that execute the matrix.
- Correction of `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`.
- Any truthful-failure corrections the gate exposes.

### Inventory and surfaces

Cover every GeoCeDG-**added** and GeoCeDG-**modified** command. R0 established
the added set as `LocusV2`, `LocusLength` and `SplineV2`, plus the modified
upstream processors carrying GeoCeDG branches; the inventory must be re-derived
from source at execution time, never assumed from this prompt.

Surfaces: `ALGEBRA_INPUT`, `GGBSCRIPT` and `GGBSCRIPT_EXECUTE` (nested
`Execute`, which forces the XML strategy and is therefore a third regime).
Locales: at least `en` and `es`.

Per row, cover: parsing and name lookup; command-processor dispatch; normal DAG
construction; recompute; undo behaviour where relevant; native save and reopen;
and truthful failure for unsupported forms.

### Proposed matrix shape

Row identity `(command_id, arity_form, entry_surface, ui_locale)`. `command_id`
must be a kernel command enum constant, never redeclared. `arity_form` must quote
a syntax line from the command bundle. Each capability field carries a typed
verdict plus a JUnit identity. Additional fields record the lookup strategy in
force, the expected error key, and whether frontend orchestration is expected on
that surface.

Reproducibility comes from four properties already used in this repository: every
row names a JUnit identity and the matrix is registered so a verification level
executes it; the row set is derived structurally from the command inventory
crossed with surfaces and locales, so adding a command without rows fails
validation; inputs are pinned by hash in a static-contract-style catalog; and
every unsupported verdict carries both an error key and a before/after
XML-identity assertion.

### What R0 already established, to be recorded rather than rediscovered

The script path and the Algebra-input path converge on exactly one command
dispatcher instance and one processor factory, so GeoCeDG commands are
structurally reachable from scripts. The Desktop Algebra input **wraps** the
identical processor entry point — it does not bypass it — adding a redefine
branch and, on the creation branch only, post-creation frontend orchestration and
a single undo store.

Real differences the matrix must record: name lookup and case sensitivity; locale
exposure for raw localized tokens that bypass delocalization; evaluation flags,
error handling and undo storage; the nested-`Execute` third regime; the
swallowed-throwable and strategy-restore defect; and the frontend orchestration
asymmetry — the opt-in auto-materialize session behaviour exists only on the
explicit human Algebra path and is **off by default**, and is *not* an
explanation of the D1 witness.

Existing coverage already executes all three GeoCeDG commands through the script
path and mechanizes the feature-off atomic-rejection row with an XML-identity
assertion. Genuinely unmechanized: the locale dimension, the truthful-failure row
for a swallowed throwable, the undo row and the rich-result/auto-materialize row.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**Do not add GGBScript functionality.** Do not change command semantics merely to
make a matrix row pass. Do not declare a second command registry — `command_id`
values are read from the kernel enum, never redeclared. Do not assert geometric
values that are not already owned by a normative specification. Do not touch
`R7`, `G9B`, `G9C`, `G9U2`, productive `G10` or further `G12`. Do not publish,
tag, release or push a publication ref.

## Architectural placement

Verification and tooling, plus any product corrections the gate exposes. The
matrix is verification authority, **not** a second source of command or geometric
truth.

## Required design/specification

A capability-matrix schema and its instance contract, following the existing
schema conventions — a JSON Schema under `geocedg/specs/operations/` with the
instance under `geocedg/validation/`, mirroring the existing static-contract and
inventory conventions.

## Geometric invariants and degeneracies

**Not applicable as a target of change.** The gate observes existing behaviour.
No row may assert a geometric value that a normative specification does not
already own, and no command's geometric semantics may be altered to satisfy the
matrix.

## Compatibility and serialization

Native save and reopen is a required per-row capability. Every
`UNSUPPORTED_TRUTHFUL` verdict must carry both an error key and an assertion that
the document XML is byte-identical before and after the attempt, so that
"unsupported" is a verified, atomic, non-mutating refusal rather than an excuse.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: the matrix-executing tests; the strategy-restore regression; the
locale dimension; and the truthful-failure row for a swallowed throwable.

Acceptance: focused operational evidence plus the registered phase selection;
`FULL` only if the matrix is wired into global verification infrastructure. Then
`git diff --check` and one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R6` and its
exact implementation base, and requires the `R5-B` naming-policy disposition to
be settled because the locale dimension depends on it.

`R7` and every later gate remain unauthorized. No tag, release, publication or
commercial action is implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a candidate pending author review. Author smoke is
recommended: run a representative script under an ES UI. Author approval is an
explicit decision naming the exact accepted commit.

## Required artifacts

The capability-matrix schema and instance; source and focal tests; a candidate
report under `docs/validation/`; machine-readable evidence under
`geocedg/validation/`; the exact required-level commands with exit codes and log
paths; bootstrap- and infrastructure-impact outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when a command cannot be reached from the
script path — that is a product defect and needs its own authorization; when a
row could only pass by changing command semantics; when the `R5-B` disposition is
unsettled and the locale dimension therefore undefined; or while
`IMPLEMENTATION_BASE` or `R5_B_STATE` remains `UNRESOLVED`.

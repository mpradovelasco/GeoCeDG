# PRE-G9B-R3 — redefine correctness

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R3` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Remove the false `AMBIGUOUS` verdict (`D3-a`) and separate the collapsed status
vocabulary (`D3-b`, tracked as `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`).

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS = INTEGRATED_PHASE`
before implementation begins, with `frozenAtPhaseStart=true`, per `AD-R0-6`.

`AD-R0-6` is author-decided: **no silent eager migration of historical persisted
signatures.** If the correct canonical durable-dependency projection requires a
persisted-signature migration, design it explicitly and **stop for author review
before implementing that migration**.

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

`AGENTS.md`; current kernel source for the spatial identity registry, the
construction-geo redefine provider and the algebra processor redefine path; the
accepted advanced-redefine and legacy-fallback ADR; the
[compatible-redefine design note](../../../docs/architecture/post_g9u1_a3_v2_compatible_redefine.md);
then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§2.3 and the
[R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
§5 as planning input only.

Note the R0 correction that must be carried forward: the accepted ADR's rejected
alternative about unregistered neighbours concerns **procedural position and
surviving order**, not dependency-signature projection, and that same decision
item explicitly sanctions failing closed when surviving order cannot be
established. The classification rests on the code-level asymmetry, not on that
quotation.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- **One durable dependency projection** shared by publication and redefine
  assessment. R0 established that both sides use the same direct projection over
  the parent algorithm's inputs with **opposite null handling** — publication
  drops inputs lacking a persistent identity, assessment throws — and that the
  stored signature produced by the dropping rule is then compared against a
  candidate signature produced by the throwing rule.
- A **typed status set** that distinguishes a genuine durable-contract change, an
  undescribable proposal and a real ambiguity. R0 found that nine distinct
  failure causes collapse onto the single `AMBIGUOUS` status, so a *correct*
  rejection such as a geo-family change is reported to the user as ambiguity.
- **Surfacing the kernel reason in the desktop dialog.** The kernel already
  carries the provider's reason string on the no-handler path; only the dialog
  discards it.

**A naive fix is insufficient and must not be attempted.** Merely making
assessment drop nulls too leaves the candidate dependency set empty and unequal
to the stored set, so the non-public dependency-set gate rejects on both the
retain and the replacement inspection, yielding `UNSUPPORTED` rather than a
legacy offer. Only a projection that reaches the durable dependency
**transitively through** the unregistered helper restores the stored set. That
transitive closure is a genuinely new third rule and must be adopted on **both**
sides together, or the asymmetry merely moves.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No global weakening of ambiguity detection. No promoting ordinary construction
elements to durable CeDG identities. No change to cycle detection, stable-role
completeness, the staged-dependency closure check, atomicity, the host-state
seal, the final pre-mutation currentness authorization or XML rollback. No
frontend authority to upgrade a status. **No silent persisted-signature
migration.** Do not touch `R4`–`R7`, intersection, metric or spline semantics,
`G9B`, `G9C`, `G9U2`, productive `G10` or further `G12`. Do not publish, tag,
release or push a publication ref.

## Architectural placement

Shared Java kernel for the projection defect, plus Desktop presentation strictly
for surfacing the kernel's own reason string. The frontend remains a renderer of
a closed kernel status with no authority to upgrade it.

## Required design/specification

**Write the durable dependency projection rule**, which no document currently
states. Update the compatible-redefine design note. An ADR is required if the
canonical projection changes observable behaviour or persisted signatures.

## Geometric invariants and degeneracies

Eight invariants a fix must not break:

1. no coordinate, proximity, label, ordinal or render-geometry repair of identity
   or incidence, at any point of the projection;
2. ordinary construction elements must not become durable CeDG identities; the
   unlabeled helper must stay identity-free;
3. publication and redefine must use one and the same durable dependency
   projection — the asymmetry is the defect;
4. a genuine durable-contract change (provider, family, schema, authority,
   binding role, stable role, cardinality) must still fail closed;
5. real cycles must still be `INVALID_DAG`, incomplete stable-role groups must
   still fail closed, and the staged-dependency closure check must stay exact;
6. atomicity, the host-state seal, the final pre-mutation currentness
   authorization and XML rollback must be preserved;
7. failure of `RETAIN` must never imply `FRESH`; legacy replacement stays
   explicit and requires a complete impact report;
8. **narrowing only** — every case the detector rejects correctly today must
   still be rejected.

## Compatibility and serialization

Durable identity, the DAG edge set, undo/redo and save/reopen must all survive a
corrected retain. Persisted signatures must not change silently; if they must
change, that is a migration decision requiring a separate author review before
implementation.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: the witness case as a minimal derived fixture; identical-definition
no-op; a candidate whose durable closure genuinely changes, still classified as a
topology change; a genuine durable-contract change, still failing closed; a real
cycle, still `INVALID_DAG`; an incomplete stable-role group, still failing
closed; undo/redo; persistence and reopen; **one test per distinct former-
`AMBIGUOUS` cause**; plus the first kernel assertions of the status itself, which
has no coverage today.

Acceptance: `PHASE -Phase POST-G9U1-A3` plus `INTEGRATION`. If the fix must also
change the publication projection or persisted signatures, escalate to
`GLOBAL_IMPACT` / `FINAL` only by explicit `VERIFICATION_ESCALATION_REQUEST` and
author authorization, since that is shared kernel infrastructure with a migration
consequence. Then `git diff --check` and one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R3` and its
exact implementation base.

A persisted-signature migration requires its own separate author review before
implementation, per `AD-R0-6`. `R4`–`R7` and every later gate remain
unauthorized. No tag, release, publication or commercial action is implied by
this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a candidate pending author review. **Author smoke is
required**: the author's own witness construction, redefining the target point
and confirming the dependent locus, its intersection results and the materialized
points behave as expected after undo/redo and reopen. Author approval is an
explicit decision naming the exact accepted commit.

## Required artifacts

The durable dependency projection rule as normative text; an ADR if required;
source and focal tests; a candidate report under `docs/validation/`; a validation
matrix; machine-readable evidence under `geocedg/validation/`; the exact
required-level commands with exit codes and log paths; bootstrap- and
infrastructure-impact outcomes; and `GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when the transitive projection cannot be made
identical on both sides without changing persisted signatures — a
persisted-signature change is a migration decision, not an implementation
detail; when any proposed narrowing would also relax a case correctly rejected
today; or while `IMPLEMENTATION_BASE` remains `UNRESOLVED`.

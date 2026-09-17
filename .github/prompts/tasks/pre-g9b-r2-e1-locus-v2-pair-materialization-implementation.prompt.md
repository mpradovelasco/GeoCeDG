# PRE-G9B-R2-E1 — Locus V2 pair exact-token materialization implementation

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

**Additionally blocked on `R2-E0 = AUTHOR APPROVED DESIGN` and on an exact
author-approved `E0` ADR / specification candidate.** Neither exists yet.

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout and its `AD-R0-1` amendment. The
existence of this file is not authorization.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
BLOCKED_ON               = R2-E0 AUTHOR APPROVED DESIGN + exact approved ADR/spec
```

<!-- geocedg-field: objective -->
## Objective

Implement the author-approved `R2-E0` contract, and nothing beyond it.

`E1` implements **only** the family and admissibility contract approved in `E0`.
There is no opportunistic widening during implementation. If implementation
demonstrates that an `E0` assumption is mathematically or architecturally
invalid, **stop and return to design and author review** — the contract is not
repaired inside the implementation.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS` at `E1` start
according to the actual implementation footprint, as finalized by `E0`.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
APPROVED_E0_ADR     = UNRESOLVED — MUST BE NAMED EXACTLY AT AUTHORIZATION
APPROVED_E0_SPEC    = UNRESOLVED — MUST BE NAMED EXACTLY AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace these fields with the exact commit and tree the author explicitly
authorizes, and with the exact approved `E0` ADR and specification identities. A
moving branch name is not sufficient. **Execution is forbidden while any of
these fields remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

The approved `E0` ADR and normative specification are the governing authority for
this phase and take precedence over any planning prose. Then `AGENTS.md`; current
kernel source; the existing spline-pair materialization contract and the Locus V2
intersection specification; the accepted intersection and token-selection ADRs.
The
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
is planning input only and is **not** a substitute for the `E0` contract.

**Without the approved `E0` ADR and specification, this phase has no normative
authority and must not begin.**

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Implementation of exactly the approved `E0` contract:

- the approved family set and no other;
- the approved structural selectors, versioned exactly as `E0` specifies;
- the approved local certification mechanism per family;
- the approved ambiguity and fail-closed rules;
- the approved symmetry and canonical source-ordering behaviour;
- the approved lifecycle: current, dormant, reactivation, retirement, disposal;
- the approved persistence, versioning and migration behaviour;
- the approved bounded-work policy and budget failure status;
- explicit ordinary-point materialization from a current admissible exact token.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No broadening of the approved family set during coding. No weakening of any
`G9S1-R1` guarantee. No coordinate, proximity, nearest-root, order, index, label,
render-geometry or Java-object-identity matching at any point. No capability
whose mathematical or lifecycle guarantees `E0` did not establish. No normative
change of its own — `E1` does not amend its own contract. No unrestricted global
topology solver. Do not touch `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10`
or further `G12`. Do not publish, tag, release or push a publication ref.

## Architectural placement

Shared Java kernel. Materialization admissibility, root identity and provenance
are semantic truth and remain in the kernel dependency graph. No frontend,
render, help or presentation layer participates in deciding admissibility.

## Required design/specification

None new. `E1` implements the approved `E0` ADR and normative specification. If
implementation reveals a gap in that contract, that is a stop condition and a
return to `E0`, not an implementation decision.

## Geometric invariants and degeneracies

Preserve exactly, as approved by `E0`:

```text
semantic curve A + semantic curve B
  -> rich semantic intersection
  -> current locally admissible semantic root
  -> exact durable token
  -> explicit ordinary-point materialization
  -> normal DAG propagation
```

`local admissibility != global completeness` must hold in the implementation:
global completeness is not upgraded because one local root is admissible, and a
globally `COMPLETE` result does not make an ambiguous local root materializable.

Degeneracies to handle exactly as approved: tangency; multiplicity; overlap and
coincident segments; singular Jacobian; disconnected components; periodic seams;
repeated traversal; `S × S` self-intersection including the `u = v` trivial
diagonal; source-order reversal; parameterization and orientation reversal.
Coordinate equality alone must never merge semantic preimages.

## Compatibility and serialization

Native `.cedg` behaviour exactly as `E0` approves: token and selector
persistence; both source identities; currentness revalidated on load; saved
coordinates never sufficient evidence; old documents remain loadable; no old
rich-only pair silently materialized after reopening; unknown selector versions
fail closed; malformed or contradictory pair records fail closed; copy,
duplicate and macro remap with appropriate lineage; rename preserves identity;
compatible redefine follows approved identity rules; incompatible replacement
cannot match by label or coordinate. Any ledger-format migration is the one `E0`
defined explicitly, never an overload of an existing version.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Future validation obligations carried from `PRE-G9B-R0` and finalized by `E0`:

- kernel focal tests;
- semantic identity;
- pair symmetry, including caller reversal under canonical normalization;
- lifecycle, including dormant and reactivation;
- persistence;
- copy and redefine;
- native `.cedg` round trips;
- the adversarial degeneracy list from `E0`;
- existing `SplineV2 × SplineV2` pair regressions, unchanged;
- ordinary `LocusV2` intersection regressions, unchanged;
- `R2` provenance integration where applicable;
- the `SeveralDefects.cedg` mixed pair as a mandatory positive target.

Acceptance: `PHASE` / `INTEGRATION` / `FINAL` evidence according to the actual
implementation footprint, frozen at `E1` start and finalized by `E0`. Then
`git diff --check` and one clean immutable candidate commit. Report the exact
command, exit code, report path, acceptance verdict, coverage verdict,
diagnostic count and execution identity for every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.

`E1` requires **all** of: an explicit author instruction naming `PRE-G9B-R2-E1`;
an exact frozen implementation base; `R2-E0 = AUTHOR APPROVED DESIGN`; and the
exact approved `E0` ADR and specification identities. Approval of `E0`'s design
does not by itself authorize `E1`.

`R3`–`R7` and every later gate remain unauthorized regardless of this phase's
outcome. No tag, release, publication or commercial action is implied by this
prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

Acceptance criteria carried from the R0 staged design as planning input, not as
accepted specification:

- **every family behaves at exactly the admissibility level `E0` approved**;
- **every excluded case fails closed and truthfully**.

The phase stops with a candidate pending author review. Author smoke is
required, including the author witness construction. Author approval is an
explicit decision naming the exact accepted commit.

## Required artifacts

Source and focal tests; a candidate report under `docs/validation/`; a validation
matrix bound to the `E0` capability matrix; machine-readable evidence under
`geocedg/validation/`; the exact required-level commands with exit codes and log
paths; bootstrap- and infrastructure-impact outcomes; and `GUIDE_IMPACT`.

## Stop conditions

**Stop and return to design and author review if implementation demonstrates
that an `E0` assumption is mathematically or architecturally invalid.** Also stop
when any family would require coordinate, proximity or order matching; when a
guarantee `E0` asserted cannot actually be established in code; when bounded work
cannot be honoured without approximating; when the approved `E0` ADR or
specification is absent or ambiguous; or while any `IMPLEMENTATION_BASE`,
`APPROVED_E0_ADR` or `APPROVED_E0_SPEC` field remains `UNRESOLVED`.

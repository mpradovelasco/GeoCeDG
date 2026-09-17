# PRE-G9B-R2 — semantic metric endpoint provenance

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout. The existence of this file is not
authorization. Execution requires a new explicit author instruction naming
`PRE-G9B-R2` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
```

<!-- geocedg-field: objective -->
## Objective

Close `TD-P1-SEMANTIC-LENGTH-INTERSECTION` by admitting a third, explicitly
typed metric endpoint provenance family.

This phase answers exactly one question:

> Given an already materialized semantic-intersection point, can it address a
> semantic endpoint on a specified `LocusV2` / `SplineV2`?

It does **not** answer which pair roots may be materialized in the first place.
That is `R2-E0` / `R2-E1`, and the two must not be conflated or merged.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze
`VERIFICATION_CLASS = INTEGRATED_PHASE` before implementation begins, with
`frozenAtPhaseStart=true`.

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

`AGENTS.md`; current kernel source for the metric, locus and intersection
packages; `geocedg/specs/locus/locus-v2-metrics.md` — in particular its §21
normative amendment bounding ordinary-`GeoPoint` endpoints by source family —
`geocedg/specs/locus/locus-v2-intersections.md` and
`geocedg/specs/curves/spline-v2-pair-materialization.md`; the accepted metric and
intersection ADRs; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
and the
[R0 disposition matrix](../../../docs/validation/pre_g9b_r0_technical_debt_disposition_matrix.md)
as planning input only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

A resolver family that walks, entirely through explicit semantic provenance
carried by the DAG:

```text
point -> intersection-point algorithm -> rich result and exact token
      -> admissible solution
      -> per-side evidence whose locus identity and semantic revision match the
         addressed source
      -> semantic position -> the existing position binder
```

The kernel already performs this exact walk elsewhere, with a fail-closed idiom
in which every non-matching shape returns null; follow that established pattern
rather than inventing a new mechanism.

Design anchor for the no-retarget guard: the shape already exists for spline
constructor occurrences — a composite occurrence key of addressed source, root
source, constructor, slot and point durable identities under an explicit version
tag. An intersection-endpoint key should mirror that shape.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No Cartesian, proximity, tolerance, label, order or index resolution at any
point. No change to intersection materialization admissibility — that is
`R2-E0`/`R2-E1` and **must not be widened here**. No change to
`Length`/`LocusLength` semantics beyond admitting the new endpoint family. No
widening of D1. Do not touch `R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10`
or further `G12`. Do not publish, tag, release or push a publication ref.

## Architectural placement

Shared Java kernel. Endpoint provenance is semantic identity and must remain in
the kernel dependency graph; no frontend, render or presentation layer
participates in resolving an endpoint address.

## Required design/specification

**Required before implementation**: a normative amendment to
`geocedg/specs/locus/locus-v2-metrics.md` defining the third endpoint family,
its currentness rule, its ambiguity rule and its persistence/reopen behaviour.
An ADR is required if the endpoint contract is judged a decision rather than an
extension of an accepted one. Implementation may not begin while that normative
work is absent.

## Geometric invariants and degeneracies

A metric endpoint is admitted **only** by explicit semantic provenance carried
through the DAG. Coordinates, proximity, tolerance, labels, branch order,
solution index, render samples, construction order and XML position must never
establish, disambiguate or repair an endpoint address. Ambiguity and staleness
fail closed to `INVALID_QUERY`, never to a nearest match. No current code path
resolves a metric endpoint from Cartesian coincidence, and two existing negative
regressions pin that; both must continue to pass.

Per-side locus identity and semantic revision are copied from the query binding,
so side ordering is fixed and only the genuine `S × S` case is ambiguous. An
undefined endpoint is already rejected one step earlier than the resolver, so a
dormant materialized point already fails closed.

## Compatibility and serialization

Existing metric regressions must be unchanged. Persistence and reopen must
revalidate currentness rather than trusting saved state; saved coordinates are
never sufficient evidence. The single authorized closure-copy rebase remains the
only permitted copy provenance path.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests: unambiguous side selection for `S`; currentness (ledger token
validity plus semantic-revision equality); token retention and reactivation via
the dormant path; copy and reopen, including the single authorized closure-copy
rebase; self-intersection `S × S`, where both sides carry the same identity and
side selection is genuinely ambiguous; source A/B ordering including the
reversed view; duplicate-token ambiguity; redefine; persistence and reopen; plus
the two existing coincident-free-point negative regressions. **No existing test
combines an intersection-materialized point with `Length`/`LocusLength`**, so
that combination must be newly covered.

Acceptance: `PHASE -Phase G9S1-R1` and `PHASE -Phase G9U0-R6`, plus
`INTEGRATION`. Escalate to `GLOBAL_IMPACT` / `FINAL` only by explicit
`VERIFICATION_ESCALATION_REQUEST` and author authorization, if the change
reaches shared metric infrastructure beyond the resolver. Then
`git diff --check` and one clean immutable candidate commit.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R2` and its
exact implementation base, and requires the normative metric amendment to exist
first.

`R2-E0`, `R2-E1`, `R3`–`R7` and every later gate remain unauthorized regardless
of this phase's outcome. No tag, release, publication or commercial action is
implied by this prompt's existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

Acceptance criteria carried from the R0 staged design as planning input, not as
accepted specification:

- `Length(S,P,Q)` and `LocusLength(S,P,Q)` are **defined** when an endpoint is a
  current, materialized point of a supported semantic intersection on `S`;
- every **ambiguous, stale, dormant or non-matching** case fails closed to
  `INVALID_QUERY`;
- existing metric regressions are unchanged.

The phase stops with a candidate pending author review. Author smoke is
recommended: the author's own witness construction plus a partial `Length` on a
spline-pair intersection endpoint. Author approval is an explicit decision naming
the exact accepted commit.

## Required artifacts

The normative metric amendment; an ADR if required; source and focal tests; a
candidate report under `docs/validation/`; a validation matrix; machine-readable
evidence under `geocedg/validation/`; the exact required-level commands with exit
codes and log paths; bootstrap- and infrastructure-impact outcomes; and
`GUIDE_IMPACT`.

## Stop conditions

Stop and report rather than guess when the side corresponding to `S` cannot be
selected unambiguously in the `S × S` case without a new rule — that rule is
normative work, not an implementation choice; when any design would need a
coordinate comparison; when the normative amendment does not yet exist; or while
`IMPLEMENTATION_BASE` remains `UNRESOLVED`.

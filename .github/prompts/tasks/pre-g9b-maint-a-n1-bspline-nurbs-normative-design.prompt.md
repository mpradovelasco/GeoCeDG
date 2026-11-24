# PRE-G9B-MAINT-A-N1 — normative design of the NURBS-ready B-spline family and of the DXF reference-import contract

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR NORMATIVE/DOCUMENTARY EXECUTION
ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N1. Autorización de diseño
normativo — Familia B-spline preparada para NURBS e interoperabilidad DXF»
(2026-11-23, delivered in the conversation) authorizes exclusively the
normative and documentary increment `PRE-G9B-MAINT-A-N1` on the exact base
named below. It also authorizes, as the preparatory governance step of this
increment and in its own commit, registering this task prompt under
`.github/prompts/tasks/`, reconciled with the P0 author resolution (revision 3).

This prompt replaces, for execution, the planning candidate
[`maint-a-n1-bspline-nurbs-normative-design.prompt-candidate.md`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-a-n1-bspline-nurbs-normative-design.prompt-candidate.md),
which stays unchanged as planning evidence. Where the candidate and the author
instruction differ, the instruction prevails; the differences carried here are:

- three separately approvable documents instead of two (the ADR is separate from
  the family specification);
- the ADR number is the next number actually free after an inventory check, never
  assumed;
- read-only characterization of the real XML writer/reader and of the `.cedg`
  container before choosing the persistence mechanism;
- the explicit conditioning decision for the polynomial publication;
- the consumer-by-consumer Locus V2 interface characterization;
- the interfaces and reservations for the future approximation (`MAINT-A-DXF1`);
- a candidate validation matrix (degrees 1 to 7, unit-weight homogeneous
  evaluation, rational circular arc import) that keeps the BS-1..BS-15 and
  DX-1..DX-7 identifiers;
- an exclusive Git worktree.

This file is an execution contract, not a second copy of geometric truth: the
geometric and persistence rules live in the documents this increment writes and,
once the author accepts them, only there. The approved directions are those of the
[P0 decision sheet](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md)
§0 and the
[P0 resolution record](../../../docs/validation/pre_g9b_maint_p0_author_resolution_record.md);
the planning evidence is the
[B-spline proposal](../../../docs/architecture/pre_g9b_maint_p0_bspline_proposal.md).

```text
PRE-G9B-MAINT-A-N1 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (normative/documentary execution only)
passClaimed              = false
PHASE_KIND               = NORMATIVE DESIGN — FAMILY SPECIFICATION, ADR AND
                           DXF REFERENCE-IMPORT CONTRACT; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT-P0 = PLANNING APPROVED BY THE AUTHOR
                           (implementation not authorized)
TERMINAL_STATE           = NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW
```

<!-- geocedg-field: objective -->
## Objective

Write three mutually coherent, separately approvable candidate contracts, each
marked `CANDIDATE — PENDING AUTHOR REVIEW` and never self-approved:

1. **Family specification** `geocedg/specs/curves/bspline-2d.md`: the explicit 2D
   B-spline family, rational in its general definition and polynomial with
   implicit unit weights in its first contract `bspline-2d/v1`.
2. **Architecture decision record** under `docs/adr/`, numbered with the next
   number actually free in the inventory of every local and remote ref, with
   status proposed.
3. **DXF reference-import contract** `dxf-spline-reference/v1`, placed in the
   specification tree according to the repository structure.

Supporting candidate documents are allowed: a validation matrix and a
characterization/candidate report under `docs/validation/`.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-23)
frozenAtPhaseStart   = true
PRODUCT_PHASE_EFFECT = NONE
PLANNED_ACCEPTANCE   = STATIC on the exact documentary commit;
                       no PHASE, no INTEGRATION, no FINAL
```

`DOCUMENTATION_STATUS_ONLY` is defined in
`geocedg/specs/operations/verification-levels.md` as "Static validation only; no
FULL". It is correct only while the increment touches documentation,
specification, ADR, validation and this prompt. A need to change `source/**`,
`apps/**`, `packaging/**`, a product resource, a verifier contract or any
`tools/agent/**` script stops the increment with a
`VERIFICATION_ESCALATION_REQUEST`.

<!-- geocedg-field: implementation_base -->
## Implementation base

- Repository: `mpradovelasco/GeoCeDG`.
- Commit: `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`.
- Tree: `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84`.
- Branch: `phase/pre-g9b-maint-a-n1`, created directly from that commit.
- Worktree: `GeoCeDG-wt-maint-a-n1`, a sibling of the main repository directory,
  never inside another worktree.

Every file, commit and `STATIC` result of the increment belongs to this worktree.
Result directories are not shared with other agents.

## Authority and evidence hierarchy

`AGENTS.md` (§1, §2, §4, §9, §11, §13, §16, §17); the canonical governance and
verification prompts; the roadmap read from disk; the
[P0 decision sheet](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md)
§0 (D-A-1 to D-A-10); current code and serialization contracts;
[`semantic-spline-2d.md`](../../../geocedg/specs/curves/semantic-spline-2d.md);
ADR 0014, 0018, 0021, 0022 and 0028;
[`dxf-curve-fidelity-and-approximation.md`](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
§4 and §9; the D-V4 conditions (roadmap, 2026-11-21). Evidence, not authority:
the [B-spline proposal](../../../docs/architecture/pre_g9b_maint_p0_bspline_proposal.md)
and the [P0 characterization report](../../../docs/validation/pre_g9b_maint_p0_characterization_report.md).
Names, interfaces and responsibilities proposed by the dossier are verified
against the repository before they are adopted.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- This task prompt, in its own commit.
- New candidate documents: the family specification, the ADR, the DXF
  reference-import contract, a validation matrix and a characterization/candidate
  report.
- Read-only characterization, without changing code or tests:
  - which Locus V2 consumers depend only on `LocusEvaluator2D` and
    `LocusDifferentialEvaluator2D` and which also need `PiecewisePolynomialLocus2D`;
  - the real XML writer and reader of `GeoLocusV2` and of commands, and what they
    accept for a serialized contract identifier;
  - how the native `.cedg` container and an older reader treat an unknown command
    and an unknown archive entry.

`PRODUCT_PHASE_EFFECT = NONE`. `MAINT-A2`, `MAINT-A-DXF0`, `MAINT-A-DXF1`,
`MAINT-A3` and `MAINT-A-R` remain unauthorized.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Java, Desktop or productive resources; tests; verification tools.
- `semantic-spline-2d.md`, the SplineV2 ADRs and every approved SplineV2 contract.
- Approved DXF export contracts.
- The canonical roadmap.
- `AGENTS.md`, `CLAUDE.md`, the canonical prompts, the other task prompts,
  `ai-shell/prompts/**` and the governance rules.
- `main`, other worktrees (N1, U2, the main worktree), the book `geocedg_book`.
- Push, merge, promotion, tags and releases.
- Any implementation of commands with weights, of the kernel family, of the DXF
  reader or of the approximation; any general rational interface in the kernel.
- Declaring the ADR accepted or any document approved.

## Architectural placement

- Family: shared Java kernel specification (a semantic curve family publishing
  `GeoLocusV2`).
- Reference import: an adapter outside the kernel; imported references are
  external read-only data, not DAG objects.
- Mathematical library: pure stateless code with no dependency on `Construction`,
  `GeoElement` or the view.

## Required design/specification

The family specification fixes, at least: degree, control points, knots and
multiplicities; parametric domain and orientation; knot ownership; clamped curves
and exact closure; parametric continuity by multiplicity; regularity and geometric
continuity as distinct facts; degeneracies and typed invalidity causes; degree and
size limits; deterministic numerical policy; dependencies, identity and revision;
versioned persistence and forward compatibility; the homogeneous representation
and the responsibilities of the future rational extension; the decision on the
conditioning of the polynomial publication (local per-piece basis or documented
fail-closed check); the placement and API of the mathematical library; the
interfaces reserved for approximation provenance (D-A-10). D-A-1 to D-A-5 are
respected and SplineV2 keeps its whole contract.

The import contract fixes the scope of D-A-6, provenance per entity and file,
domain, orientation, units and explicit transformations, diagnosed rejection, the
separation of render from semantic evaluation, R-a persistence and the
characterization list that R-c requires. R-d is not specified.

## Geometric invariants and degeneracies

Those of the proposal §2 and §8, verified and fixed in the specification and the
import contract.

## Compatibility and serialization

No effect on existing documents. The family contract is versioned and serialized
from its origin; an older reader fails closed. The import contract does not change
the document format under R-a. Identity is never inferred from labels,
coordinates, XML positions or construction order.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- Link, Markdown, UTF-8 and LF checks of the new documents; decision-integrity
  check against the decision sheet §0; `git diff --check`.
- `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\<fresh-name>`
  on the exact documentary commit, run from the A-N1 worktree.
- No `PHASE`, `INTEGRATION` or `FINAL` is a condition of this increment. No test
  of the future implementation is executed.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: this prompt registration in its own commit; the read-only
characterization; the candidate documents in a separate commit; `STATIC` on that
commit. The author's later approval of A-N1 is required before any implementation
of the family or of the DXF importer is authorized. Silence or general agreement is
not authorization.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n1` only. No push, merge, `main`, tags,
releases or promotion. The worktree is not removed after the increment.

## Acceptance and closeout

The increment stops at `NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW` with
`selfApproved = false`, `authorApproved = false` and `passClaimed = false`. A
technical `STATIC` acceptance is not author approval.

## Required artifacts

The three candidate contracts, the validation matrix and the characterization
report; the documentary commit SHA and tree; the `STATIC` command, exit code,
report path, acceptance and coverage verdicts and diagnostic count. Bootstrap
impact: `NO_CHANGE` (documentation only; no workstation prerequisite changes).
`GUIDE_IMPACT`: none (no user-visible behavior changes). The completion report
lists the technical decisions adopted with their justification, the questions that
need author approval, the effective dependencies of `MAINT-A2` and
`MAINT-A-DXF0`, and the open compatibility or persistence risks.

## Stop conditions

- The design requires an immediate non-additive change to SplineV2 or to a generic
  Locus V2 interface.
- NURBS readiness requires a general rational interface in the kernel before
  `MAINT-A-R` is authorized.
- The import contract requires the reference to take part in the DAG (R-d) or
  introduces a new external geometric authority.
- A collision of branch, path, ADR number or shared file with another worktree.
- A conflict with a higher rule, or a need to touch a forbidden path.
- `STATIC` incomplete or untrusted, or a dirty candidate.

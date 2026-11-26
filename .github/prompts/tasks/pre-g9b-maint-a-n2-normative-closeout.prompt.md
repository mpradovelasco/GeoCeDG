# PRE-G9B-MAINT-A-N2 — author approval and normative closeout

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR DOCUMENTARY CLOSEOUT EXECUTION ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N2. Aprobación autoral de los tres
contratos y ejecución del cierre normativo» (2026-11-26, delivered in the conversation)
records the individual author approval of `cedg-reference-store/v1` with ADR 0044,
`cedg-reference-export/v1` with ADR 0045 and `dxf-spline-approximation/v1`, on the normative
candidate `9c7e612294a2a0521a2c21d8f9c4580cb3b34efb` (tree
`7faccc1fc5d41f5d55d9fbb1ca5fd3473aa3e4fe`), with the adoption records of
`cadad2b5630e9928a737a9c4b51c2832b1b1395b` (tree `47edc050828bd94044e9dcb30bc3829be7b567fb`).
It authorizes the documentary closeout and the update of the coordination proposal for five
future increments. It does not authorize the push to `main`, tags, releases, packaging or any
implementation.

This file is an execution contract, not a second copy of the approved rules: those live in
the contracts, the ADR and the amendments.

```text
PRE-G9B-MAINT-A-N2-CLOSEOUT =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = true    (the three contracts, as decided by the author on 2026-11-26)
implementationAuthorized = true    (documentary closeout only)
passClaimed              = false
PHASE_KIND               = NORMATIVE CLOSEOUT — APPROVAL RECORD, STATES, AMENDMENTS,
                           ROADMAP RECORD, COORDINATION PREPARATION; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT-A-N2-R4 (9c7e61229, records cadad2b56); D-R4-1
TERMINAL_STATE           = NORMATIVE CONTRACTS — AUTHOR APPROVED — CLOSEOUT VERIFIED —
                           PENDING PUBLICATION
```

<!-- geocedg-field: objective -->
## Objective

Record the author approval with date and exact identities; set the three contracts to
`AUTHOR APPROVED` and ADR 0044 and 0045 to accepted; apply the amendments of groups R, X and A
to the approved texts as dated amendments that keep their approval history; add the canonical
roadmap record A-RM-1; record the dependencies and gates of RC-S0, RC, EXP, DXF1-S0 and DXF1;
update cross-references and the coordination proposal; prepare one closeout candidate with
accepted verification, pending publication.

```text
CHANGE_ROUTE          = ORDINARY
VERIFICATION_CLASS    = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-26)
REQUIRED_VERIFICATION = STATIC
frozenAtPhaseStart    = true
PRODUCT_PHASE_EFFECT  = NONE
```

<!-- geocedg-field: implementation_base -->
## Implementation base

Published base `a9ee803bf4be92c58ab7e120111ebacc70055487`; approved normative content
`9c7e612294a2a0521a2c21d8f9c4580cb3b34efb`; adoption records
`cadad2b5630e9928a737a9c4b51c2832b1b1395b`. Branch `phase/pre-g9b-maint-a-n2-closeout` from
the records commit, in the reused worktree `GeoCeDG-wt-maint-a-n2`. Every earlier branch of
A-N2 stays unchanged.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

This prompt in its own commit; the approval record and the filled adoption records; status
lines of the three contracts and of ADR 0044 and 0045; the amendments of groups R, X and A in
`dxf-spline-reference.md`, `bspline-2d.md`, `dxf-curve-fidelity-and-approximation.md`,
`geometry-export-foundation.md`, `native-document-identity.md` and the status lines of
ADR 0014 and ADR 0043; one additive roadmap record; the plan, the matrix, the amendment set,
the candidate prompts and the coordination proposal; a consolidated closeout record.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Any change of the approved technical content; product code, tests, resources, packaging,
verification tools; historical reports and evidence beyond status pointers; `AGENTS.md`,
`CLAUDE.md`, canonical and other task prompts; rewriting earlier commits; `main`, push, merge,
tags, releases; any implementation increment; `MAINT-A-DXF1b`, `MAINT-A3`, native NURBS and
independent maintenance of the existing DXF importer or exporter.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Prompt-contract validation; links, UTF-8, line endings (byte-preserving edit of the mixed
roadmap), tables, states and cross-references; `git diff --check`;
`tools/agent/verify.ps1 -Profile STATIC` on the exact closeout commit; classification of any
documentary descendant with `tools/agent/documentary-descendant.ps1`; a check that remote
`main` is an ancestor of the candidate. No `PHASE`, `INTEGRATION` or `FINAL`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Closeout and preparation only. A modification that introduces a material discrepancy with the
approved candidate stops the closeout and is reported. Publication needs a separate author
authorization naming the exact candidate (AGENTS §8.1).

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n2-closeout` only; no push, merge, `main`, tags,
releases or packaging. The worktree is kept.

## Stop conditions

An amendment cannot be applied without changing approved technical content; a forbidden path
must change; `STATIC` incomplete or untrusted; remote `main` not an ancestor of the candidate;
a dirty candidate.

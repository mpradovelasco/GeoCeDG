# MAINT-A-DXF1-S0 — characterization of numeric persistence and of the creation route for A-DXF1

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`; registered under
`.github/prompts/tasks/` only after the author adopts the contract and authorizes the
increment on an exact base.

**Dependency status (normative closeout of 2026-11-26):** `dxf-spline-approximation/v1` is AUTHOR APPROVED ([approval record](../../validation/pre_g9b_maint_a_n2_author_approval_record.md)) — satisfied. The increment itself stays not authorized.

```text
selfApproved = false; authorApproved = false; implementationAuthorized = false
PHASE_KIND   = CHARACTERIZATION; NO PRODUCT CHANGE
DEPENDS_ON   = dxf-spline-approximation/v1 ADOPTED
```

<!-- geocedg-field: objective -->
## Objective

Execute NUM-1 to NUM-10 as characterization tests: bit-exact round trips of the chosen input
representation (`dxf-spline-approximation/v1` §12.2) through save/reopen, Undo/Redo, both
clipboards, Insert File and both redefinition routes; byte stability of save → reopen → save;
the replacement-route loss of the provenance block (pinned as a known defect for
`MAINT-A-DXF1`); the 15-digit path pinned; `-0.0`. Measure first values for the constants of
§8 on the AP fixtures with a scratch implementation of the method outside product paths.

```text
CHANGE_ROUTE = ORDINARY; VERIFICATION_CLASS = BOUNDED_PHASE
PLANNED_ACCEPTANCE = STATIC + PHASE PRE-G9B-MAINT-A-DXF1-S0
```

<!-- geocedg-field: implementation_base -->
## Implementation base

The `main` commit named by the author's authorization; branch `phase/pre-g9b-maint-a-dxf1-s0`;
worktree `GeoCeDG-wt-maint-a-dxf1-s0`. May run in parallel with `MAINT-A-DXF0-RC-S0`.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Characterization tests and fixtures; the `PHASE` registration and inventory regeneration;
a characterization report.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Product code; the kernel number format; approved specifications; `main`; push, merge, tags.

<!-- geocedg-field: required_checks -->
## Required tests and commands

`STATIC`, `PHASE PRE-G9B-MAINT-A-DXF1-S0`; `git diff --check`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Characterization only; a route that is not exact stops for an author decision (contract §19.1).

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch only.

## Stop conditions

No exact and byte-stable creation route with standard objects; any need to change the kernel
number format.

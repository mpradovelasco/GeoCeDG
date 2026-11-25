# MAINT-A-DXF0-RC-S0 — characterization of the `R-c` document store before implementation

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`. It becomes an
execution contract only when the author adopts `cedg-reference-store/v1` and authorizes this
increment on an exact base; it is then registered under `.github/prompts/tasks/`.

```text
selfApproved = false; authorApproved = false; implementationAuthorized = false
PHASE_KIND   = CHARACTERIZATION; NO PRODUCT CHANGE
DEPENDS_ON   = cedg-reference-store/v1 ADOPTED; ADR 0044 ACCEPTED
```

<!-- geocedg-field: objective -->
## Objective

Produce the evidence that `cedg-reference-store/v1` §14 requires before code: C-1 (every
open, New, replacement, URL, Insert File and recovery route against the prepare-and-commit
rule, with failures injected after parsing), C-2 (isolated GeoCeDG 1.2.2 with a store entry),
C-3 (GeoCeDG Classic with the entry, for the preservation or save-barrier rule), C-4 (cost of
reading, validating and tessellating stores at the measured budget points, in open and in the
save preflight, with heap observations), C-5 (inspection and angle-mode scanners, recovery
snapshot). Characterization tests live in a registered `PHASE` selection; no product file
changes.

```text
CHANGE_ROUTE = ORDINARY; VERIFICATION_CLASS = BOUNDED_PHASE
PLANNED_ACCEPTANCE = STATIC + PHASE PRE-G9B-MAINT-A-DXF0-RC-S0
```

<!-- geocedg-field: implementation_base -->
## Implementation base

The `main` commit that contains the adopted contracts, named by the author's authorization;
branch `phase/pre-g9b-maint-a-dxf0-rc-s0`; worktree `GeoCeDG-wt-maint-a-dxf0-rc-s0`.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Characterization tests and fixtures; the `PHASE` registration of this increment and its
inventory regeneration from executed evidence; a characterization report under
`docs/validation/`.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Product code and resources; approved specifications; `AGENTS.md`, `CLAUDE.md`, canonical
prompts; `main`; push, merge, tags, releases, packaging.

<!-- geocedg-field: required_checks -->
## Required tests and commands

`STATIC`, then `PHASE PRE-G9B-MAINT-A-DXF0-RC-S0` on the frozen candidate; `git diff --check`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Characterization only. A finding that contradicts the adopted contract stops the increment
for an author decision; it is never absorbed into code.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch only; no push, merge or promotion.

## Stop conditions

An open route that cannot respect prepare-and-commit without a non-additive change; Classic
unable both to preserve and to block; costs that make the measured budget unsafe on the
reference workstation.

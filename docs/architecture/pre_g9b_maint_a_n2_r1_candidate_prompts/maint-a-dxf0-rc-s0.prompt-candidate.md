# MAINT-A-DXF0-RC-S0 — characterization of the `R-c` document store before implementation

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`. It becomes an
execution contract only when the author adopts `cedg-reference-store/v1` and authorizes this
increment on an exact base; it is then registered under `.github/prompts/tasks/`.

**Dependency status (normative closeout of 2026-11-26):** `cedg-reference-store/v1` is AUTHOR APPROVED and ADR 0044 accepted ([approval record](../../validation/pre_g9b_maint_a_n2_author_approval_record.md)) — satisfied. The increment itself stays not authorized.

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
C-3 (GeoCeDG Classic with the entry, for the preservation or save-barrier rule), the
current-behavior part of C-5 (inspection and angle-mode scanners, expert worker, recovery
snapshot with a document carrying the entry, refusal or acceptance recorded). C-4 (the full
Open → Save → Reopen cycle at the budget) and the budget part of C-5 need the store
implementation and are closeout gates of `MAINT-A-DXF0-RC` (R3), not part of this increment. Characterization tests live in a
registered `PHASE` selection; no product file changes.

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
unable both to preserve and to block; a scanner, worker or recovery route (C-5) whose current
behavior could not be changed to accept the store without a non-additive change.

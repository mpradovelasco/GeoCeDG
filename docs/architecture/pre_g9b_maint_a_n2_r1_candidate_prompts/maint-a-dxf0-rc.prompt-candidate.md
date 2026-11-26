# MAINT-A-DXF0-RC — implementation of the `R-c` document store

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`; registered under
`.github/prompts/tasks/` only after the author adopts the contract and authorizes the
increment on an exact base.

```text
selfApproved = false; authorApproved = false; implementationAuthorized = false
PHASE_KIND   = PRODUCT INCREMENT — DOCUMENT STORE OUTSIDE Construction
DEPENDS_ON   = cedg-reference-store/v1 ADOPTED; ADR 0044 ACCEPTED; MAINT-A-DXF0-RC-S0 REVIEWED
```

<!-- geocedg-field: objective -->
## Objective

Implement `cedg-reference-store/v1`: first the shared model milestone RC-M (pure types:
reference identifier, export preference, resource-policy values, store snapshot interface, with
unit tests), then the
codec and validation in `org.geocedg.common.importer.dxf` (with the `values` counter of the
contract §5.2 item 12 and, additively, the presence of each of the groups 210, 220 and 230 in
the import record, A-IMP-12, authorized by the author decision D-R4-1 with its six conditions:
presence and original binary64 per component, importer geometry, admission, diagnostics and
interpretation unchanged, presence never inferred from a value, recorded and effective normal
never confused, references outside `Construction` and the DAG, RC-25 tests), the reader branch and writer step
in `MyXMLioJre`, the configurable resource policy, prepare-and-commit in every document
route, the saved-state rule, the Desktop store addressed by identifier, the scanners, the
Classic preservation or save barrier, the guide and the cases RC-* of the matrix.

```text
CHANGE_ROUTE = ORDINARY; VERIFICATION_CLASS = INTEGRATED_PHASE
PLANNED_ACCEPTANCE = STATIC + PHASE PRE-G9B-MAINT-A-DXF0-RC + INTEGRATION (CORE + FOCUSED)
GUIDE_IMPACT = UPDATE; FINAL = NOT PROPOSED
```

<!-- geocedg-field: implementation_base -->
## Implementation base

The `main` commit named by the author's authorization; branch `phase/pre-g9b-maint-a-dxf0-rc`;
worktree `GeoCeDG-wt-maint-a-dxf0-rc`. RC-M is the first commit of the branch.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

The files listed in the operational plan §4.1; tests; the `PHASE` registration and inventory
regeneration from executed evidence; guide and localized texts; an implementation report.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

`MyXMLHandler`, `MyXMLio.getFullXML`, `doParseXML`, Undo managers, spatial identity, command
codecs, Locus V2, exporters, the B-spline family; approved specifications (amendments are
applied only by the author's adoption record); `main`; push, merge, tags, releases.

<!-- geocedg-field: required_checks -->
## Required tests and commands

RC-1 to RC-26, SEP-5 and C-1 regression; DX-1 to DX-13 non-regression; **C-4 as a closeout
gate** (R3): the full Open → Save → Reopen cycle on the implementation at the budget, including
the worst admissible case at 100 % of `maxEntryBytes`, with a minimum-heap sweep up to 1 GiB,
compared with the R3 measurements, plus the reader-safety cases of the contract §14; **the
budget part of C-5 as a closeout gate**: inspection and angle-mode scanners, expert worker and
recovery snapshot with stores at the budget, the entry recognized and validated, no 4 MiB
refusal; `STATIC`,
`PHASE`, `INTEGRATION` on the frozen candidate; `git diff --check`; bootstrap impact review.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Implementation of the adopted contract only; any normative change stops the increment.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch; integration and promotion only by a later author instruction naming exact SHAs.

## Stop conditions

Those of `cedg-reference-store/v1` §16; a silent loss on any route; a change of the importer's
geometric behavior, admission, diagnostics or interpretation beyond the additive presence of
D-R4-1; a resource policy that
cannot reject an over-budget document without truncation; a C-5 route that still refuses a store at the budget; a C-4 result that does not confirm
the budget (the increment does not close; the budget is revised on that evidence by the
author).

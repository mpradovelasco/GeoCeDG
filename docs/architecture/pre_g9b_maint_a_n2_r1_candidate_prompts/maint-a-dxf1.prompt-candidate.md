# MAINT-A-DXF1 — exact conversion and approximation of DXF references

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`; registered under
`.github/prompts/tasks/` only after the author adopts the contract and authorizes the
increment on an exact base.

**Dependency status (normative closeout of 2026-11-26):** `dxf-spline-approximation/v1` is AUTHOR APPROVED ([approval record](../../validation/pre_g9b_maint_a_n2_author_approval_record.md)) — satisfied; `MAINT-A-DXF1-S0` reviewed, `MAINT-A-DXF0-RC` integrated and RC-M merged — pending. Closeout gate: the constants `bspline-param-lsq-constants/v1` fixed. The increment itself stays not authorized.

```text
selfApproved = false; authorApproved = false; implementationAuthorized = false
PHASE_KIND   = PRODUCT INCREMENT — CONVERSION AND APPROXIMATION INTO bspline-2d/v1
DEPENDS_ON   = dxf-spline-approximation/v1 ADOPTED; MAINT-A-DXF1-S0 REVIEWED;
               MAINT-A-DXF0-RC INTEGRATED (service and interface); RC-M MERGED
```

<!-- geocedg-field: objective -->
## Objective

Implement `dxf-spline-approximation/v1` without the certified bound. Its first commit is the milestone DXF1-M: the read-only parser of the `approximationProvenance` grammar `v1` and the applicability computation (§10, §11), additive in the family code, with unit tests; EXP merges it. Then: exact conversion; the
parametric least-squares method with Householder QR, the three disjoint measure sets and the
stop rules; the provenance block grammar, its GeoCeDG factory and applicability (closing
BS-24); the replacement-route preservation of the block; the exact input representation with
its self-check; no persisted failure history (immediate diagnostic and session report);
Desktop action, dialog, inspector text and report; cases AP-1 to AP-11, AP-14, NUM-1 to
NUM-10, SEP-7, SEP-8, and the export cases RX-7 and RX-18 that need the operation. The pure library part may be developed while
`MAINT-A-DXF0-RC` progresses; the service and interface integrate after it.

```text
CHANGE_ROUTE = ORDINARY; VERIFICATION_CLASS = INTEGRATED_PHASE
PLANNED_ACCEPTANCE = STATIC + PHASE PRE-G9B-MAINT-A-DXF1 + INTEGRATION (CORE + FOCUSED)
GUIDE_IMPACT = UPDATE; FINAL = NOT PROPOSED
```

<!-- geocedg-field: implementation_base -->
## Implementation base

The `main` commit named by the author's authorization, with RC integrated for the service;
branch `phase/pre-g9b-maint-a-dxf1`; worktree `GeoCeDG-wt-maint-a-dxf1`.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

The files of the operational plan §4.3; tests; the `PHASE` registration and inventory
regeneration; guide and texts; report.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

The exporter; `MyXMLHandler`; the XML string template and kernel number format; the family
element grammar; the store schema; the certified bound (`MAINT-A-DXF1b`); weights in the
produced construction; approved specifications; `main`; push, merge, tags.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The cases listed in the objective; A2 and A-DXF0 non-regression; `STATIC`, `PHASE`,
`INTEGRATION`; `git diff --check`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Implementation of the adopted contract only; constants stay candidates until the author fixes
them on the measured evidence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch; integration and promotion only by a later author instruction.

## Stop conditions

Those of `dxf-spline-approximation/v1` §19.

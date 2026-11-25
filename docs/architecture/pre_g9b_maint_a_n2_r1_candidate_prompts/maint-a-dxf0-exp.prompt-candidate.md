# MAINT-A-DXF0-EXP — DXF export of imported references

**CANDIDATE PROMPT — NOT AUTHORIZED.** Prepared by `PRE-G9B-MAINT-A-N2-R1`; registered under
`.github/prompts/tasks/` only after the author adopts the contract and authorizes the
increment on an exact base.

```text
selfApproved = false; authorApproved = false; implementationAuthorized = false
PHASE_KIND   = PRODUCT INCREMENT — SEPARATE REFERENCE POPULATION IN THE DXF EXPORT
DEPENDS_ON   = cedg-reference-export/v1 ADOPTED; ADR 0045 ACCEPTED; RC-M AND DXF1-M MERGED
```

<!-- geocedg-field: objective -->
## Objective

Implement `cedg-reference-export/v1`: the option in the existing DXF dialog, the store
snapshot, the resolver of §6 (computed from `Construction` and the store), the separate
reference adapter, the `SPLINE` writer with its write round-trip check, the preflight rule,
sidecar schema 4, «Review…», and the conformance evidence of §12.1 with AutoCAD Core Console
and a pinned ezdxf, separating definition reading from evaluation. Integrate after
`MAINT-A-DXF0-RC`.

```text
CHANGE_ROUTE = ORDINARY; VERIFICATION_CLASS = INTEGRATED_PHASE
PLANNED_ACCEPTANCE = STATIC + PHASE PRE-G9B-MAINT-A-DXF0-EXP + INTEGRATION (CORE + FOCUSED)
                     + external conformance evidence (RX-16, RX-17)
GUIDE_IMPACT = UPDATE; FINAL = NOT PROPOSED
```

<!-- geocedg-field: implementation_base -->
## Implementation base

The `main` commit named by the author's authorization, with RC-M and DXF1-M merged; branch
`phase/pre-g9b-maint-a-dxf0-exp`; worktree `GeoCeDG-wt-maint-a-dxf0-exp`. Never in parallel
with `MAINT-A3`.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

The files of the operational plan §4.2; tests; conformance scripts and fixtures outside
product paths; the `PHASE` registration and inventory regeneration; guide and texts; report.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Document persistence, the kernel, the math library, the DXF reader, the construction export
adapters, the legacy G5 route; installing ezdxf into the product; approved specifications;
`main`; push, merge, tags, releases.

<!-- geocedg-field: required_checks -->
## Required tests and commands

RX-1 to RX-6 and RX-8 to RX-17, with provenance blocks written as test fixtures and read through the DXF1-M parser; RX-7 and RX-18, which need the A-DXF1 operation, run in `MAINT-A-DXF1`; byte identity of the ordinary export (for the product's Java runtime); SEP-6 and SEP-9; `STATIC`, `PHASE`,
`INTEGRATION`; `git diff --check`; bootstrap impact (ezdxf and AutoCAD are external
validation tools, not workstation prerequisites of the product).

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Implementation of the adopted contract only; a non-conforming reader or a normative change
stops the increment.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branch; integration and promotion only by a later author instruction.

## Stop conditions

Those of `cedg-reference-export/v1` §14.

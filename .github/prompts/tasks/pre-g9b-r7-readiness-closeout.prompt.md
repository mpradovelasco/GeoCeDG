# PRE-G9B-R7 — integrated PRE-G9B readiness and closeout

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR EXECUTION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction "GeoCeDG — PRE-G9B-R7 Integrated PRE-G9B
Readiness, Technical-Debt Reconciliation, Roadmap Revision and Final
Verification" of 2026-10-15 names `PRE-G9B-R7`, its exact implementation base
and its frozen verification class. That instruction also authorizes this
prompt-specific documentary reconciliation as part of `R7`: it resolves the two
execution placeholders and records the current state of the predecessors. The
amendment replaces the proposed future stub (blob
`be941a71454b85abf21a55f8f141b88b44a12281`), created under `AD-R0-9` from the
author-approved `PRE-G9B-R0` closeout and amended minimally at the `PRE-G9B-R6`
closeout and at the `PRE-G9B-R6-plus` planning closeout. It keeps the stub's
technical scope and intent. This file is an execution contract, not a second
policy document, and the
[phase-entry record](../../../docs/validation/pre_g9b_r7_phase_entry_record.md) holds
the verified identities it summarizes.

```text
implementationAuthorized = true   (execution and technical verification only)
authorApproved           = false
passClaimed              = false
selfApproved             = false
```

`authorApproved = false` means that the technical candidate this phase produces
has not been author-approved. Technical verification never creates author
approval. The phase stops with one exact technically verified candidate pending
author review, the author smoke and the author's roadmap and debt decisions.

| Revision | Origin | State |
|---|---|---|
| stub | `AD-R0-9` governance task (2026-09-18) | `PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED`; history |
| stub, amended | `PRE-G9B-R6` closeout and `PRE-G9B-R6-plus` planning closeout (2026-10-01) | dependency on `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED` added; history |
| execution | author instruction of 2026-10-15 | this file |

The stub's historical sentence "no `PRE-G9B-R` phase after `R0` has been
authorized" described the repository on 2026-09-18. It is superseded by the
later author-approved closeouts enumerated below and is not a current stop
condition. Its dependency `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED` is
satisfied: the author approved the global `PRE-G9B-R6-plus` closeout on
2026-10-15 and it is published as `main` `b9bdb1e1`.

<!-- geocedg-field: objective -->
## Objective

Integrate the track, reconcile the roadmap and debt records, and place an
explicit author decision about what follows.

```text
CHANGE_ROUTE               = ORDINARY
VERIFICATION_CLASS         = GLOBAL_IMPACT          (frozen by the author)
PRIMARY_ACCEPTANCE_PROFILE = FINAL
CLOSEOUT_MODE              = AUTHOR_OPERATED
frozenAtPhaseStart         = true
```

The class follows the stub's own rule: `FINAL` is required because authorized
phases of the track changed global verification infrastructure (`PRE-G9B-R1`
wired receipt emission into the verifier and registry; later maintenance tracks
changed the registered `INFRA_UNIT`/`FINAL` leaves). It is not downgraded
because `R7` itself changes only documentation. `FINAL` runs on `R7`'s own
frozen candidate and provides its own candidate-bound coverage; no historical
acceptance is inherited.

This phase **must not declare that `G9B` necessarily follows.** It ends with an
explicit author decision about what comes next.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE      = b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513
IMPLEMENTATION_TREE      = 3aef929b0b5939854d9ccd736a919b496172a30e
BASE_STATE               = published main; author-approved documentary closeout of
                           PRE-G9B-R6-plus-G and of the PRE-G9B-R6-plus mini-track
APPROVED_TECHNICAL_PARENT = da1a09528a6f97254734400b12256e8fb20a6216 (G-R1 T')
AUTHORIZED_PRIOR_PHASES  = the exact enumeration below, derived from the
                           author-approved closeouts and verified on the base in the
                           phase-entry record §3
```

A moving branch is not a base. The task is not rebased onto any later commit
without a new author instruction.

`AUTHORIZED_PRIOR_PHASES` — every phase of the track that was authorized and
closed, or explicitly dispositioned, by the author:

- `PRE-G9B-R0` — `PASS — AUTHOR APPROVED` (characterization and planning only).
- `PRE-G9B-R1` — `AUTHOR APPROVED FOR PROMOTION — RETAINED PRE-EXISTING BASELINE
  DEFECT`; its single `FINAL` stays `REJECTED_SEMANTIC` with no receipt.
- `PRE-G9B-R1-R1` — `PASS — AUTHOR APPROVED`.
- `PRE-G9B-R2`; `PRE-G9B-R2-E0/E1` — `PASS — AUTHOR APPROVED`.
- `PRE-G9B-R3` (on its corrective descendant `PRE-G9B-R3-R1`), `PRE-G9B-R3-C1`,
  `PRE-G9B-R3-C1-U1` — `PASS — AUTHOR APPROVED`.
- `PRE-G9B-R3-X1` with `GOV-R` — `PASS — AUTHOR APPROVED`; its source `FINAL` stays
  `REJECTED_VERIFICATION_CORE / UNTRUSTED`, reconciled by a separate
  reconciliation receipt. `PRE-G9B-R3-X2` — `NOT NEEDED` (author decision).
- The author-approved maintenance tracks executed inside the track: Input,
  historical-suite retirement, generated-state retirement and `Dot`/XML.
- `PRE-G9B-R4` (on its corrective descendant), `PRE-G9B-R5-A`, ADR 0031 and
  `PRE-G9B-R5-B` (on `R5-B-R2`), `PRE-G9B-R6` — `PASS — AUTHOR APPROVED`.
- `PRE-G9B-R6-plus` and every subphase of its mini-track, including the POST-E2
  and POST-E3 activities, F4 (`COMPLETE — AUTHOR APPROVED`) and `G` on the G-R1
  candidate — `PASS — AUTHOR APPROVED — PUBLISHED`.

Designed but unauthorized work contributes no productive scope and no acceptance
coverage.

Planning authority base, recorded as provenance only and **not** as this phase's
implementation identity: the author-approved `PRE-G9B-R0` closeout, commit
`48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; the canonical governance and verification prompts; the executable
verification authority (`tools/agent/verify.ps1`,
`geocedg/specs/operations/verification-registry.json` and its schemas; the
archived part of `verification-levels.md` is history only);
`geocedg/specs/operations/documentation-maintenance.md`; the closeout evidence of
every authorized prior `PRE-G9B-R` phase, in particular the
[R6-plus G closeout record](../../../docs/validation/pre_g9b_r6_plus_g_closeout_record.md)
and its [structured debt register](../../../docs/validation/pre_g9b_r6_plus_g_structured_debt_register.md);
`docs/roadmap/geocedg_roadmap.md`; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md),
the [R0 disposition matrix](../../../docs/validation/pre_g9b_r0_technical_debt_disposition_matrix.md)
and the [G11/G12/G15 reconciliation](../../../docs/architecture/pre_g9b_r6_plus_g11_g12_g15_roadmap_reconciliation.md)
as the record of what the track set out to do and the starting evidence for the
roadmap review.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- Integrated verification of the accumulated candidate, scoped to the work that
  was **actually authorized and closed**, not to the full designed sequence.
- Reconciliation of every debt record opened, retained or closed by the track,
  including `TD-P1-SEMANTIC-LENGTH-INTERSECTION`, `TD-P1-PACKAGING-SUMMARY`,
  `TD-VERIFY-RECEIPT-RECOVERY`, `TD-G9X1-HISTORICAL-PIN`,
  `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`,
  `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX`,
  `TD-R0-REDEFINE-AMBIGUOUS-VOCABULARY`,
  `TD-R0-GGBSCRIPT-STRATEGY-RESTORE`, `TD-R0-G9U1-Q02-BINDING`,
  `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` and the R6-plus register rows owned
  by or checkpointed at `PRE-G9B-R7`. Each must end with a **terminal disposition
  or an explicit retained owner**. For a previously unowned item the author
  permits `OWNER = AUTHOR — ACCOUNTABLE`, `NEXT_CHECKPOINT = PRE-G9B-R7 AUTHOR
  REVIEW` as a retention and accountability designation; it resolves nothing.
  The reconciliation is a phase-specific view with references back to the
  authoritative records (`AD-R0-7`); it is not a global ledger.
- Exact roadmap reconciliation: factual corrections of outdated current-status
  statements and references, and the true status of each phase.
- An evidence-backed proposed revision of the future `G11`, `G12` and `G15`
  scopes with a decision matrix, presented as a proposal pending author decision.
- Factual reconciliation of obsolete distribution-status and version references in
  `geocedg/specs/packaging/windows-packaging.md`,
  `docs/licensing/geocedg_profile_nc.md` and related living documentation:
  `INTERNAL` stays the explicit repository default, `NC` is the approved
  non-commercial packaging profile, and package construction is distinguished
  from authorization to publish a public release. Packaging behaviour and
  historical licensing evidence are unchanged.
- The readiness statement, the frozen candidate report, the author-review sheet
  with the author smoke checklist, and their machine-readable mirrors.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**No new capability.** No product, kernel, geometry, serialization, identity,
document-format, DAG, Desktop-behaviour, build, packaging or global
verification-infrastructure change, and no change to the semantics of the
executable verifier. No publication, push, merge, tag or release. No moving any
risk record to resolved, and no promotion of `OPEN` to `RESOLVED` or `ACCEPTED
LIMITATION`, without an explicit author decision. No rewriting of historical
closeout records or accepted evidence; a rejected verdict is never relabelled.
No second global debt or verification authority. **No authorization of `G9B`**,
and no statement that `G9B` necessarily follows. Do not mark anything approved by
agent action. Do not implement `G11`, `G12`, `G15` or
`ENH-B-MANUAL-EXPORT-AREA-DRAG`; do not touch `G9C`, `G9U2`, productive `G10`,
`PROFILE COMMERCIAL` or exact DXF `SPLINE`. Do not contact GeoGebra or
OpenGeoProver. Do not reopen the published `PRE-G9B-R6-plus` approval.

## Architectural placement

Verification, documentation and roadmap. This phase produces no product
capability and no geometric semantics.

## Required design/specification

Roadmap and traceability reconciliation only. No new normative contract is
created by this phase. The proposed `G11`/`G12`/`G15` revision is planning
documentation awaiting author decision.

## Geometric invariants and degeneracies

**Not applicable.** This phase changes no geometry, kernel semantics, dependency
graph, serialization or metric behaviour. It reports on work already closed under
its own contracts and re-executes the accumulated tests on its own candidate.

## Compatibility and serialization

No document format, persisted identity or serialization change. Historical
evidence, receipts and approvals retain their original cohort, meaning and
consumability, and must not be relabelled, inherited or reinterpreted during
reconciliation.

<!-- geocedg-field: required_checks -->
## Required tests and commands

On the exact frozen candidate, each executed once:

```text
tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\<fresh-name>
tools/agent/verify.ps1 -Profile FINAL  -LogDirectory artifacts\agent\<fresh-name>
```

`FINAL` covers the live `INTEGRATION` leaves and the `INFRA_UNIT` leaves of the
registry; no standalone `INTEGRATION` or `INFRA_UNIT` campaign duplicates it.
Focused verification adds only the registered accumulated leaves that `FINAL`
does not contain, through their registered phase selections:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R5-A -LogDirectory artifacts\agent\<fresh-name>
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6   -LogDirectory artifacts\agent\<fresh-name>
```

(`infra.guide-structure-diagnostic`; `ggbscript-matrix.product` and
`static.ggbscript-matrix-inputs.semantic`). Documentary checks: repository-relative
Markdown links of the changed files, JSON validity of the mirrors,
`git diff --check`, the roadmap state, the phase lineage and closeout identities,
debt-ownership completeness, and `Test-PromptContractDocument` with the `task`
profile for this prompt. If `FINAL` is accepted and emits a receipt, authenticate
it and inspect it with `tools/agent/phase-closeout.ps1 -Action INSPECT`; if it
emits none, report the actual result without inventing one.

Report the exact command, exit code, report path, candidate commit and tree, run
identifier, result hash, acceptance verdict, coverage verdict, diagnostic findings
and receipt identity for every run. `FINAL` is not rerun on an unchanged candidate
to erase a diagnostic or environmental incident; a failure needs causal
characterization before any new scope is proposed.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author instruction of 2026-10-15 authorizes execution and technical
verification of `PRE-G9B-R7` only. It does not approve the final candidate.
Closing this phase authorizes **nothing** that follows it. `G9B`, `G9C`, `G9U2`,
productive `G10`, `G11`, `G12`, `G15`, `PROFILE COMMERCIAL`, exact DXF `SPLINE`,
publication and commercial distribution all remain separate explicit author
decisions. No tag, release, publication or commercial action is implied by this
prompt.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA. Final acceptance evidence never grants publication
authority by itself.

## Acceptance and closeout

The phase stops with a candidate pending author review and **must end with an
explicit author decision about what follows**. **Author smoke is required**; the
author executes it and reports it separately, and `AUTHOR SMOKE = PASS` is never
recorded without the author's statement. Author approval is an explicit decision
naming the exact accepted commit; nothing is marked approved by the agent.

## Required artifacts

A phase-entry record, the reconciled debt record, the `G11`/`G12`/`G15`
roadmap-review package, a readiness report, a frozen candidate report and an
author-review sheet under `docs/validation/` and `docs/architecture/`; roadmap
updates stating the exact status of each phase; machine-readable evidence under
`geocedg/validation/pre-g9b-r7/`; the exact required-level commands with exit
codes and log paths; bootstrap- and infrastructure-impact outcomes; and
`GUIDE_IMPACT`.

## Stop conditions

Stop the affected operation and preserve evidence when the authorized base does
not match the published repository; when an approved predecessor is missing or
contradicts a higher-authority record; when an open debt item cannot be given a
truthful status and accountable owner; when an author decision from the
`PRE-G9B-R0` decision list remains unresolved; when integrated verification
cannot complete with trusted coverage; when a product or verifier change becomes
necessary; when reconciliation would require rewriting historical evidence; when
a future roadmap scope change cannot be separated from its required author
decision; or when `FINAL` fails for a cause attributable to the candidate. A
failure proven to be retained baseline debt keeps its original verdict under the
approved causality rules.

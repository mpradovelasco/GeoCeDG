# PRE-G9B-MAINT-A-N2-R3 — focal correction of M1–M3, normative audit and closeout preparation

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR DOCUMENTARY AND CHARACTERIZATION EXECUTION
ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N2-R3. Corrección de M1–M3, auditoría
normativa y preparación del cierre» (2026-11-25, delivered in the conversation) authorizes
exclusively the focal documentary and characterization correction `PRE-G9B-MAINT-A-N2-R3` of
the A-N2-R2 candidate: the three material defects M1 (direct measurement of the proposed R-c
budget), M2 (sidecar coherence for excluded and rejected references) and M3 (references-only
export against the approved foundation), the minor findings that are only coherence,
cross-reference or wording, an independent audit, and the preparation of the normative
closeout. It authorizes no product change, no implementation increment and no publication.

This file is an execution contract, not a second copy of persistence or export truth: those
rules live in the candidate contracts.

```text
PRE-G9B-MAINT-A-N2-R3 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (documentary and characterization execution only)
passClaimed              = false
PHASE_KIND               = FOCAL NORMATIVE CORRECTION — M1 BUDGET MEASUREMENT, M2 SIDECAR,
                           M3 REFERENCES-ONLY EXPORT, MINOR COHERENCE, CLOSEOUT PREPARATION;
                           NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT-A-N2-R2 (101f574ab, documentary descendant
                           ea38a1684); pre-adoption audit with M1–M3
TERMINAL_STATE           = NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION
```

<!-- geocedg-field: objective -->
## Objective

Resolve M1–M3 and the minor coherence findings in the A-N2 candidate documents, with M1 backed
by a direct measurement of the proposed store budget on a 1 GiB heap; run an independent audit
that verifies M1–M3 explicitly; prepare the adoption records, the normative updates, the
roadmap record and the documentary publication proposal without recording any approval.

```text
CHANGE_ROUTE          = ORDINARY
VERIFICATION_CLASS    = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-25)
REQUIRED_VERIFICATION = STATIC
frozenAtPhaseStart    = true
PRODUCT_PHASE_EFFECT  = NONE
```

<!-- geocedg-field: implementation_base -->
## Implementation base

Published base `a9ee803bf4be92c58ab7e120111ebacc70055487`; R2 content
`101f574ab2929e09ed8295f427c7c67c4f951f06` (tree `14fa0d1d8fc7c9c7955c87d4c81bc73d350c02fc`);
documentary descendant `ea38a168472b327aaa8796d7098387956e03822a` (tree
`3b5f2f9952f7cfd5bea2e3912a744065b92d45f4`). Branch `phase/pre-g9b-maint-a-n2-r3` from that
descendant, in the reused worktree `GeoCeDG-wt-maint-a-n2`. The A-N2, R1, R2 and empty adoption
branches stay unchanged.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

This prompt in its own commit; revisions of the A-N2 candidate documents (three contracts,
ADR 0044 and 0045, amendments, matrix, plan, candidate prompts, adoption proposals, reports);
an R3 report with the prepared closeout package; isolated scratch probes of the store budget
that change no tracked file.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Product code, executable tests, resources, packaging, verification tools; in-place edits of
approved specifications, accepted ADRs, approval records, the canonical roadmap and historical
evidence (closeout updates are prepared, not applied); `AGENTS.md`, `CLAUDE.md`, canonical and
other task prompts; rewriting earlier commits; `main`, push, merge, tags, releases; executing
any implementation increment; recording `AUTHOR APPROVED` or `PASS`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Prompt-contract validation; links, UTF-8, line endings, tables, identifiers and
cross-references; `git diff --check`; `tools/agent/verify.ps1 -Profile STATIC` on the exact
R3 content commit; classification of a documentary descendant with
`tools/agent/documentary-descendant.ps1`. No `PHASE`, `INTEGRATION` or `FINAL`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Correction and preparation only. A measurement that does not support the proposed budget is
reported and the budget revised on evidence, never hidden; a finding that needs a different
normative decision, or a contradiction of the paired DXF/sidecar contract, stops that part and
is reported. Adoption requires the author's review of the R3 identity.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n2-r3` only; no push, merge, `main`, tags, releases or
packaging. The worktree is kept.

## Stop conditions

A semantic or guarantee contradiction that the author decisions do not settle; a forbidden path
must change; `STATIC` incomplete or untrusted; a dirty candidate.

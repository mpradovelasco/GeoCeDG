# PRE-G9B-MAINT-A-N2-R4 — final R-c grammar correction and closure of audit observations

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR DOCUMENTARY EXECUTION ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N2-R4. Corrección final de gramática R-c y
cierre de observaciones de auditoría» (2026-11-26, delivered in the conversation) authorizes
exclusively the focal documentary correction `PRE-G9B-MAINT-A-N2-R4` of the A-N2-R3 candidate:
the two IMPORTANT findings of the independent R3 audit (I-1, the grammar of `normal`; I-2, the
`values` counter) and its five MINOR findings (m-1 informative read of provenance, m-2 pending
decisions of the export adoption proposal, m-3 the empty-population guard of the DXF controller,
m-4 a single roadmap draft with C-4 and C-5 as RC closeout gates, m-5 the documentary
descendant record), a final independent read-only review and the preparation of the three
approval records and of the joint documentary closeout. It authorizes no product change, no
implementation increment and no publication.

This file is an execution contract, not a second copy of persistence or export truth: those
rules live in the candidate contracts.

```text
PRE-G9B-MAINT-A-N2-R4 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (documentary execution only)
passClaimed              = false
PHASE_KIND               = FOCAL NORMATIVE CORRECTION — R-c GRAMMAR (normal, values),
                           MINOR AUDIT OBSERVATIONS, ADOPTION PREPARATION; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT-A-N2-R3 (da27d4590, documentary descendant
                           476f6976f); independent R3 audit with I-1, I-2 and m-1 to m-5
TERMINAL_STATE           = NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION
```

Author decisions carried by the instruction: the `normal` record preserves the DXF groups read
and their presence, never filling absent groups with `(0, 0, 1)`, and the export keeps writing
the output normal `(0, 0, 1)` without attributing it to the source DXF; `values` is defined
exhaustively, without introducing a new geometric restriction; the initial configurable budget
is 1 500 000 values (2 000 000 is not chosen without new evidence); RX-D19 is not extended to
references excluded for other causes.

<!-- geocedg-field: objective -->
## Objective

Correct I-1 and I-2 in `cedg-reference-store/v1` and its dependent documents after re-inspecting
the current DXF reader; resolve m-1 to m-5; obtain a final independent read-only review of the
resulting content; prepare the three approval records and the joint documentary closeout
without recording any approval.

```text
CHANGE_ROUTE          = ORDINARY
VERIFICATION_CLASS    = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-26)
REQUIRED_VERIFICATION = STATIC
frozenAtPhaseStart    = true
PRODUCT_PHASE_EFFECT  = NONE
```

<!-- geocedg-field: implementation_base -->
## Implementation base

Published base `a9ee803bf4be92c58ab7e120111ebacc70055487`; R3 content
`da27d459091341b4952d4fc14250fc6e29b838aa` (tree `c0aa42375cda55dd0109b3f981f788a62f6e2d75`);
documentary descendant `476f6976fd07679c919ea7782ba9fa393fd9a396` (tree
`862b3975a840da36af2f0d76bf8c77a3d13e73d3`). Branch `phase/pre-g9b-maint-a-n2-r4` from that
descendant, in the reused worktree `GeoCeDG-wt-maint-a-n2`. The A-N2, R1, R2, R3 and empty
adoption branches stay unchanged.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

This prompt in its own commit; revisions of the A-N2 candidate documents (three contracts,
ADR 0044 and 0045, amendments, matrix, plan, candidate prompts, adoption proposals, reports);
an R4 report with the prepared approval records and closeout proposal; read-only inspection of
product code.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Product code (the DXF importer and export controller included), executable tests, resources,
packaging, verification tools; in-place edits of approved specifications, accepted ADRs,
approval records, the canonical roadmap and historical evidence; `AGENTS.md`, `CLAUDE.md`,
canonical and other task prompts; rewriting earlier commits; `main`, push, merge, tags,
releases; executing any implementation increment; cleaning unrelated worktrees; recording
`AUTHOR APPROVED` or `PASS`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Prompt-contract validation; links, UTF-8, line endings, tables, identifiers and
cross-references; `git diff --check`; `tools/agent/verify.ps1 -Profile STATIC` on the exact
R4 content commit; classification of a documentary descendant with
`tools/agent/documentary-descendant.ps1`, recorded as a classification and not as a new
`STATIC`. No `PHASE`, `INTEGRATION` or `FINAL`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Correction and preparation only. A material contradiction found by the final review stops the
adoption preparation and is reported; absent one, no new general redesign cycle is opened.
Adoption requires the author's decision on the exact R4 identity.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n2-r4` only; no push, merge, `main`, tags, releases or
packaging. The worktree is kept.

## Stop conditions

The current reader cannot support a lossless representation of the groups read without an
importer change; a counter rule that would need a new geometric restriction; a forbidden path
must change; `STATIC` incomplete or untrusted; a dirty candidate.

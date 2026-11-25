# PRE-G9B-MAINT-A-N2-R1 — normative refinement, resolution of author decisions and preparation of coordinated implementations

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR NORMATIVE/DOCUMENTARY EXECUTION
ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N2-R1. Refinamiento normativo,
resolución de decisiones y preparación de implementaciones coordinadas» (2026-11-25,
delivered in the conversation) authorizes exclusively the focal documentary refinement
`PRE-G9B-MAINT-A-N2-R1` of the A-N2 normative design, on the exact base named below. It
fixes the author decisions D-R1-1 to D-R1-10, which are transcribed in the consolidated R1
report and bind every document of the increment. They are design criteria; they do not
approve the final contracts in advance.

This file is an execution contract, not a second copy of persistence, export or numerical
truth: those rules live in the candidate contracts and, once the author adopts them, only
there. The predecessor prompt
[`pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md`](pre-g9b-maint-a-n2-reference-store-export-approximation-normative-design.prompt.md)
stays unchanged as the record of A-N2.

```text
PRE-G9B-MAINT-A-N2-R1 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (normative/documentary execution only)
passClaimed              = false
PHASE_KIND               = NORMATIVE REFINEMENT — R-c CAPACITY POLICY, NO PERSISTED
                           FAILURE HISTORY, EXPORT STATE 4, EXTERNAL CONFORMANCE,
                           A-DXF1 / A-DXF1b SEPARATION, ADOPTION PACKAGE,
                           CANDIDATE IMPLEMENTATION PROMPTS; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT-A-N2 = NORMATIVE DESIGN CANDIDATE
                           (874286e95, documentary descendant e7803badb)
TERMINAL_STATE           = NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR ADOPTION
```

<!-- geocedg-field: objective -->
## Objective

Revise the three A-N2 candidate contracts (`cedg-reference-store/v1`,
`cedg-reference-export/v1`, `dxf-spline-approximation/v1`), ADR 0044 and ADR 0045, the
candidate amendment set, the validation matrix, the operational roadmap and the reports so
that they implement D-R1-1 to D-R1-10 coherently; replace the fixed store cap by a measured,
configurable resource policy; remove every persisted record of failed approximations; adopt
alternative A for export state 4; fix AutoCAD Core Console and ezdxf as conformance readers;
separate `MAINT-A-DXF1` and `MAINT-A-DXF1b`; prepare one adoption proposal per contract,
candidate prompts for the five next increments and a coordinated execution proposal; deliver
one consolidated report.

```text
CHANGE_ROUTE          = ORDINARY
VERIFICATION_CLASS    = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-25)
REQUIRED_VERIFICATION = STATIC
frozenAtPhaseStart    = true
PRODUCT_PHASE_EFFECT  = NONE
```

A need to change `source/**`, `apps/**`, `packaging/**`, a product resource, an executable
test, a verifier contract or any `tools/agent/**` script stops the increment with a
`VERIFICATION_ESCALATION_REQUEST`.

<!-- geocedg-field: implementation_base -->
## Implementation base

- Repository: `mpradovelasco/GeoCeDG`; published base `a9ee803bf4be92c58ab7e120111ebacc70055487`,
  tree `92f8ab0674587f3bc249ae7964e0eddb18b6a35c`.
- A-N2 content candidate `874286e95564234d27094c9c983e69f9bff160bf`, tree
  `30c21042c4b18a1416374a1ea0cfd9a4802cf6fa`; documentary descendant
  `e7803badb3344c8d26af36c82a5b05a20eb5f7e6`, tree
  `2c9d8f8d814052a904fc9f11bac8a8d9f4316d37` (branch `phase/pre-g9b-maint-a-n2`, kept
  unchanged).
- Branch `phase/pre-g9b-maint-a-n2-r1`, created from `e7803badb`, in the reused worktree
  `GeoCeDG-wt-maint-a-n2`. The A-N2 history is never rewritten.

## Authority and evidence hierarchy

`AGENTS.md`; the canonical governance and verification prompts; the roadmap read from disk;
current code and serialization contracts; the approved specifications and ADRs named by the
A-N2 prompt; the author decisions D-R1-1 to D-R1-10. Evidence, not authority: the A-N2
candidate documents, the A-N2 characterization evidence, and the new R1 characterizations
and probes.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- This task prompt, in its own commit.
- Revisions of the A-N2 candidate documents and new candidate documents: adoption proposals
  per contract, candidate canonical prompts of `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC`,
  `MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0` and `MAINT-A-DXF1`, a coordinated execution
  proposal and the consolidated R1 report.
- Read-only characterization; isolated scratch probes of capacity, persistence and
  conformance (AutoCAD Core Console, ezdxf when available) that change no tracked file;
  fixtures and numerical references in a scratch area.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Product code, executable tests, resources, packaging, verification tools.
- In-place edits of approved specifications, accepted ADRs, approval records and historical
  evidence; amendments remain separate candidates.
- The canonical roadmap; `AGENTS.md`, `CLAUDE.md`, the canonical prompts, other task prompts,
  `ai-shell/prompts/**`.
- Rewriting the A-N2 commits or branch; `main`; other worktrees; the book repository.
- Creating worktrees of future increments; executing any implementation increment.
- Push, merge, promotion, tags, releases, packaging, installing software.
- Recording `AUTHOR APPROVED` or any approval.

## Architectural placement

Unchanged from A-N2: document store outside the kernel; export adapter outside the kernel;
pure library and external service for A-DXF1, with additive GeoCeDG family code only where
`bspline-2d.md` §10.5 reserves it.

## Required design/specification

As listed in the author instruction §4 to §11 and §14: capacity policy distinguishing
admission, per-operation, reader-safety, configurable storage-budget and platform limits,
backed by measurements; source and transformed data kept; no persisted failure history and a
minimal model for successful derivations; atomic document transition for every route and a
no-silent-loss rule for Classic; export state table without a failure state; conformance
procedure with AutoCAD Core Console and ezdxf separating definition reading from
mathematical evaluation; A-DXF1 mathematical justifications; maturity reconciled with
`AGENTS.md` without novelty flags.

## Compatibility and serialization

No effect on existing documents. A change to the kernel's general number format or to a
shared persistence semantics remains a stop condition.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- Prompt-contract validation of this prompt; links, UTF-8, line endings, tables,
  identifiers and cross-references of the changed documents; `git diff --check`.
- `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\<fresh-name>` on the
  exact R1 content commit; classification of any documentary descendant with
  `tools/agent/documentary-descendant.ps1`.
- No `PHASE`, `INTEGRATION` or `FINAL`.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: this prompt in its own commit; characterization and isolated probes; the R1
candidate content commit; `STATIC`; a documentary descendant recording the verification.
The author's adoption of each contract, and a separate instruction, are required before any
implementation increment. New decisions that change semantics, identity, persistence,
mathematical guarantees or product compatibility stay pending for the author.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n2-r1` only. No push, merge, `main`, tags, releases,
packaging or promotion. The worktree is kept for review.

## Acceptance and closeout

Terminal state `PRE-G9B-MAINT-A-N2-R1 — NORMATIVE DESIGN CANDIDATE — READY FOR AUTHOR
ADOPTION`, with `selfApproved = false`, `authorApproved = false`, `passClaimed = false`. A
technical `STATIC` acceptance is not author adoption.

## Required artifacts

The revised and new candidate documents; commit SHAs and trees; the `STATIC` command, exit
code, report path, verdicts and diagnostic count. Bootstrap impact: `NO_CHANGE`.
`GUIDE_IMPACT`: none in this increment.

## Stop conditions

- A refinement requires reference geometry in `Construction`, the DAG or CeDG geometric
  identity, or a kernel number-format change.
- An independent-review finding changes semantics, identity, persistence, guarantees or
  compatibility in a way the author decisions do not settle: it is left pending, never
  chosen implicitly.
- A forbidden path must change; `STATIC` incomplete or untrusted; a dirty candidate.

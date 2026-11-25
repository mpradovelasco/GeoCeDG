# PRE-G9B-MAINT-A-N2 — coordinated normative design of the R-c reference store, the DXF export of references and the A-DXF1 conversion and approximation

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR NORMATIVE/DOCUMENTARY EXECUTION
ONLY.**

The author's instruction «GeoCeDG — PRE-G9B-MAINT-A-N2. Diseño normativo
coordinado de persistencia R-c, exportación DXF de referencias y aproximación
A-DXF1» (2026-11-25, delivered in the conversation) authorizes exclusively the
characterization and normative documentary increment `PRE-G9B-MAINT-A-N2` on the
exact base named below, and asks for this canonical task prompt to be prepared
and validated as part of the increment. The prompt is registered in its own
commit before the candidate documents.

The instruction does **not** authorize implementing the reference store, the
export of references or the conversion and approximation. Analyses may be
coordinated from one main session, with one consolidated delivery.

This file is an execution contract, not a second copy of geometric,
persistence or export truth: those rules live in the candidate documents this
increment writes and, once the author accepts them, only there. The author
directions D-A-N2-1 to D-A-N2-5 of the instruction (semantic separation,
historical compatibility, export policy, non-convertible references, user
interface) are transcribed in the consolidated report and bind every document of
the increment.

```text
PRE-G9B-MAINT-A-N2 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (normative/documentary execution only)
passClaimed              = false
PHASE_KIND               = CHARACTERIZATION AND NORMATIVE DESIGN — R-c STORE
                           CONTRACT, REFERENCE DXF EXPORT CONTRACT, A-DXF1
                           CONTRACT, CANDIDATE AMENDMENTS; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-MAINT SECOND WAVE = PASS — AUTHOR APPROVED —
                           PUBLISHED ON MAIN (a9ee803bf)
TERMINAL_STATE           = NORMATIVE DESIGN CANDIDATE — PENDING AUTHOR REVIEW
```

<!-- geocedg-field: objective -->
## Objective

Write mutually coherent candidate documents, each marked
`CANDIDATE — PENDING AUTHOR REVIEW` and never self-approved, so that the author
can approve each of the three contracts separately:

1. **R-c contract**: versioned persistence of imported DXF references inside the
   native document and outside `Construction`.
2. **Reference DXF export contract**: an explicit, separate export capability
   that writes admitted external definitions as DXF entities without turning them
   into CeDG constructions, with its own fidelity class, inclusion policy and
   explicit, persistent reference–construction correspondence.
3. **A-DXF1 contract**: exact conversion and explicit polynomial approximation of
   a reference into a new `bspline-2d/v1` construction, with typed failures,
   separated error measures and the `approximationProvenance` grammar.
4. **Candidate amendments** of approved texts that conflict with the three
   contracts, kept apart from the approved texts until the author decides.
5. **Proposed ADRs** where a contract changes an approved architectural decision,
   numbered with the next numbers actually free.
6. A validation matrix, an operational implementation roadmap, a
   characterization and decisions report, and a final consolidated report with a
   per-contract decision table.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-11-25)
frozenAtPhaseStart   = true
PRODUCT_PHASE_EFFECT = NONE
PLANNED_ACCEPTANCE   = STATIC on the exact documentary commit;
                       no PHASE, no INTEGRATION, no FINAL
```

`DOCUMENTATION_STATUS_ONLY` is defined in
`geocedg/specs/operations/verification-levels.md` as "Static validation only; no
FULL". It is correct only while the increment touches documentation, candidate
specifications, proposed ADRs, validation documents and this prompt. A need to
change `source/**`, `apps/**`, `packaging/**`, a product resource, an executable
test, a verifier contract or any `tools/agent/**` script stops the increment with
a `VERIFICATION_ESCALATION_REQUEST`.

<!-- geocedg-field: implementation_base -->
## Implementation base

- Repository: `mpradovelasco/GeoCeDG`.
- Commit: `a9ee803bf4be92c58ab7e120111ebacc70055487`.
- Tree: `92f8ab0674587f3bc249ae7964e0eddb18b6a35c`.
- Base state: `PRE-G9B-MAINT SECOND WAVE — PASS — AUTHOR APPROVED — PUBLISHED ON MAIN`.
- Branch: `phase/pre-g9b-maint-a-n2`, created directly from that commit.
- Worktree: `GeoCeDG-wt-maint-a-n2`, a sibling of the main repository directory,
  never inside another worktree. No older branch is reused and the main checkout
  is not changed.

Every file, commit and `STATIC` result of the increment belongs to this worktree.

## Authority and evidence hierarchy

`AGENTS.md` (§1, §2, §4, §5, §9, §12.6, §13, §16, §17); the canonical governance
and verification prompts; the roadmap read from disk; current code and
serialization contracts; [`bspline-2d.md`](../../../geocedg/specs/curves/bspline-2d.md);
[`dxf-spline-reference.md`](../../../geocedg/specs/import/dxf-spline-reference.md);
[ADR 0043](../../../docs/adr/0043-explicit-2d-bspline-family-rational-ready.md);
ADR 0014 and ADR 0016;
[`geometry-export-foundation.md`](../../../geocedg/specs/export/geometry-export-foundation.md);
[`dxf-curve-fidelity-and-approximation.md`](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md);
[`native-document-identity.md`](../../../geocedg/specs/ui/native-document-identity.md);
the first- and second-wave approval and decision records. Evidence, not
authority: the focal reports of A-DXF0, A-DXF0-R1 and A2, the second-wave
consolidated report, and the stage-4 drafts of the previous session under the
ignored folder `artifacts/agent/pre-g9b-maint-wave2-closeout/stage4-drafts/` of
the main checkout. Every hypothesis of those drafts is verified against the
current code before it is adopted; none is transferred mechanically.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- This task prompt, in its own commit.
- New candidate documents: the three contracts, the candidate amendment set, the
  proposed ADRs, the validation matrix, the implementation roadmap, the
  characterization and decisions report and the consolidated report.
- Read-only characterization of the current code: the `.cedg` ZIP reader and
  writer, `geogebra.xml` document blocks, open preflight and restoration, atomic
  save, Undo/Redo, copy/paste and Insert File, session versus document state, size
  limits; the export chain `GeometryExportService → GeometryExportModel →
  DxfExporter` and its manifest; the numeric serialization of `BSpline` inputs and
  of the provenance block.
- Scratch probes outside the repository tree that change no tracked file, as
  evidence for the characterization.

`PRODUCT_PHASE_EFFECT = NONE`. `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC`,
`MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0`, `MAINT-A-DXF1`, `MAINT-A-DXF1b`,
`MAINT-A3` and `MAINT-A-R` remain unauthorized.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Java, Desktop or productive resources; executable tests; verification tools;
  packaging.
- In-place edits of approved specifications, accepted ADRs, approval records and
  historical evidence; amendments are written as separate candidates.
- The canonical roadmap (a candidate roadmap record may be proposed in the
  amendment set).
- `AGENTS.md`, `CLAUDE.md`, the canonical prompts, the other task prompts,
  `ai-shell/prompts/**` and the governance rules.
- `main`, other worktrees, the book repository.
- Push, merge, promotion, tags, releases and packaging.
- Creating worktrees for future implementation increments.
- Declaring any contract, amendment or ADR approved or accepted.

## Architectural placement

- R-c store: document/application model outside the geometric kernel
  (AGENTS §4); references never become `GeoElement`, `GeoLocusV2` or DAG
  participants.
- Reference export: an export adapter outside the kernel consuming a separate
  read-only reference adapter (AGENTS §13); no new geometric solving in the
  exporter.
- A-DXF1: pure library computation plus a creation service outside the kernel;
  additive GeoCeDG code only where the family contract reserves it.

## Required design/specification

The R-c contract fixes the container mechanism, versioned schema, source and
transformed data, explicit transformation, provenance, a durable document
identity independent of `ceID`, labels, coordinates, handles and XML positions,
visibility, conversion/approximation state, integrity, restoration, duplicates,
save and reopen, copy, duplication, deletion, New, and exclusion from the DAG,
with the compatibility rules of D-A-N2-2 and the incompatibility with older
readers stated explicitly.

The export contract fixes the extension point, the `SPLINE` mapping, a fidelity
class for transfer of an external definition distinct from `EXACT` and
`APPROXIMATE`, the exhaustive inclusion table of the instruction §6.1, the minimal
correspondence record and the resolution of `bspline-2d.md` §10.5, and the
round-trip and external-reader obligations.

The A-DXF1 contract fixes exact conversion, approximation (parameter, domain,
orientation, degree, tolerance, point limit, initial knots, adaptive refinement,
QR least squares, endpoint interpolation, closure, work limits, typed failures,
rank deficiency and conditioning), the separation of sample residual, independent
estimate and certified bound, the A-DXF1 / A-DXF1b split, and the
`approximationProvenance` grammar. Its numeric persistence risk is characterized
before any implementation is proposed.

## Geometric invariants and degeneracies

Those of `bspline-2d.md` and `dxf-spline-reference.md`; a reference never gains
geometric authority; a conversion never transmits authority from the reference
to the new object; no association is inferred from coordinates, labels, handles,
proximity or order.

## Compatibility and serialization

No effect on existing documents in this increment. The candidate store is
versioned and fails closed on unknown versions; documents written with it are
declared unsupported by older readers. A change to the kernel's general number
format or to shared persistence semantics is a stop condition.

<!-- geocedg-field: required_checks -->
## Required tests and commands

- Link, Markdown, UTF-8 and LF checks of the new documents; prompt-contract
  validation of this prompt with `Test-PromptContractDocument`; `git diff --check`.
- `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\<fresh-name>`
  on the exact documentary commit, run from the A-N2 worktree.
- No `PHASE`, `INTEGRATION` or `FINAL` is a condition of this increment. No test
  of the future implementation is executed.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Authorized: this prompt registration in its own commit; the read-only
characterization and scratch probes; the candidate documents in separate commits;
`STATIC` on the documentary candidate; a documentary-only descendant that records
the candidate identity and the `STATIC` result. The author's later approval of
each contract is required before any implementation increment is authorized.
Silence or general agreement is not authorization.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits on `phase/pre-g9b-maint-a-n2` only. No push, merge, `main`, tags,
releases, packaging or promotion. The worktree is kept for review.

## Acceptance and closeout

The increment stops at `PRE-G9B-MAINT-A-N2 — NORMATIVE DESIGN CANDIDATE — PENDING
AUTHOR REVIEW` with `selfApproved = false`, `authorApproved = false` and
`passClaimed = false`. A technical `STATIC` acceptance is not author approval.

## Required artifacts

The candidate documents of the objective; the documentary commit SHA and tree;
the `STATIC` command, exit code, report path, acceptance and coverage verdicts and
diagnostic count. Bootstrap impact: `NO_CHANGE` (documentation only; no
workstation prerequisite changes). `GUIDE_IMPACT`: none in this increment (no
user-visible behavior changes); each future implementation increment records its
own. The consolidated report lists code evidence, confirmed and refuted
hypotheses, architectural compatibility, numbered and motivated author decisions,
risks and stop conditions, dependencies of the future increments and a decision
table that allows each contract to be approved separately.

## Stop conditions

- A contract requires a reference to enter `Construction`, the DAG or a Locus V2
  capability (`R-d`), or a geometric dependency between a reference and a
  construction.
- A contract requires changing the kernel's general XML number format or a shared
  persistence semantics without an architectural decision.
- The export would require geometric solving in the exporter or a byte change of
  the ordinary export.
- A collision of branch, path, ADR number or shared file with another worktree.
- A conflict with a higher rule, or a need to touch a forbidden path.
- `STATIC` incomplete or untrusted, or a dirty candidate.

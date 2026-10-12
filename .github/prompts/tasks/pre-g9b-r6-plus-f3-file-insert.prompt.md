# PRE-G9B-R6-plus-F3 — File / Insert surface and conservative document insertion

**CANONICAL PROMPT — RECONCILED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F3-PREP` (author instruction of
2026-10-12; version kept in commit `480c972972e90514e88421194a792898c916834e`)
and reconciled by `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (author
instruction of 2026-10-12, "Conservative and Verifiable File Insertion
Semantics"). That instruction recorded a conservative author direction —
GeoCeDG provides only insertion and template operations whose correctness,
integrity and reversibility can be demonstrated; fail closed; correctness
before breadth and upstream compatibility — and authorized only scope
reassessment, focused characterization, design reconciliation, decision
preparation and this prompt. It approved no feature set, architecture,
inherited change or class. The planning decisions `F3-D1` to `F3-D6`
([F3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decisions_record.md))
remain in force until the author amends them.

Evidence: the historical
[preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f3_preparation_characterization_report.md)
(`K*`, `CF-F3-*`) and the
[focused feasibility report](../../../docs/validation/pre_g9b_r6_plus_f3_reconciliation_feasibility_report.md)
(`FQ-*`, `RF-*`, `OTI-F3R-*`). Assessment:
[conservative-scope assessment](../../../docs/validation/pre_g9b_r6_plus_f3_conservative_scope_assessment.md).
Design: [conservative insertion design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_conservative_insertion_design_candidate.md)
(`PROPOSED — PENDING AUTHOR REVIEW`), reusing §3, §4, §8 and §9 of the
[F3-PREP design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md).
Questions: [author-decision reconciliation record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decision_reconciliation_record.md)
v2.0 (`DQ-F3-1` to `DQ-F3-20`). The existence of this file is not
authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F3` and an exact base, disposes of `DQ-F3-1` to `DQ-F3-20`,
and approves or rejects the proposed amendments of `F3-D2` items 4 and 5
(`DQ-F3-13`, `DQ-F3-17`) and of the accepted `DQ-D1-7` (`DQ-F3-16`). Where a
section below names alternatives, **the author disposition selects one; the
agent never chooses**. A disposition may authorize, as the first tracked edit,
an amendment of this prompt to the authorized state. This file is an execution
contract, not a second policy document: units in `unit-system.md`; classes in
`verification-levels.md` §12.8; evidence in the reports; behavior in the
design candidate.

```text
PRE-G9B-R6-plus-F3 =
PLANNED — AUTHOR APPROVED (2026-10-03, F3-D1 to F3-D6)
PREPARED — RECONCILED — NOT AUTHORIZED

CONSERVATIVE DIRECTION   = RECORDED (2026-10-12) — not a decision on any DQ
RECONCILIATION           = PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION (DOCUMENTATION_STATUS_ONLY)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP PRODUCT SURFACE AND A GEOCEDG-OWNED, FAIL-CLOSED
                           ORCHESTRATION OF EXISTING KERNEL CONSTRUCTION COPY
                           (insertFrom) AND OF A WHITELISTED STYLE COPY; no kernel,
                           command, algorithm, serialization, parser or unit change
DEBT                     = DEBT-R6PLUS-FILE-INSERT-SURFACE (closure per DQ-F3-20)
DEPENDS_ON               = PRE-G9B-R6-plus-D1 (hard, F3-D5); F2/F2-R1 published
PLANNED CLASS            = INTEGRATED_PHASE (F3-D4) — NOT FROZEN
GATES                    = DQ-F3-1 to DQ-F3-20; amendments DQ-F3-13, -16, -17
NEXT_SUBPHASE            = none implied; F4, G stay unauthorized
```

<!-- geocedg-field: objective -->
## Objective

Deliver, as the author disposes:

1. **File → Insert / Archivo → Insertar** with **Insert File / Insertar
   archivo** and **Apply Template / Aplicar plantilla** as independent profile
   actions, removed from the GeoCeDG Open dialog (`DQ-F3-1`, `DQ-F3-2`).
2. **`.cedg` and `.ggb` containers only** (`DQ-F3-3`; content admissibility
   per `DQ-F3-13`/`DQ-F3-14`).
3. **OFF out of the GeoCeDG UI** (`DQ-F3-4`); internal OFF support unchanged.
4. **Insert File** under the contract chosen by `DQ-F3-13` to `DQ-F3-16` and
   `DQ-F3-18` (recommended: design contract R — admission before mutation,
   whitelist matrix, macros per `DQ-F3-15`, labels per `DQ-F3-5`, no
   Overwrite, no scripts, dedicated insertion buffer, dry run, verification,
   rollback, one undo step), always keeping the `D1` provenance and notice and
   never scaling coordinates.
5. **Apply Template** under the contract chosen by `DQ-F3-17` (recommended:
   T0 whitelist of object styles, one undo step, nothing else changed).
6. Helper isolation (`DQ-F3-10`), Classic and fallback (`DQ-F3-11`), optional
   inherited corrections only if `DQ-F3-19` selects them, the tests of the
   design candidate §9 and the durable specification.

### Decision gates

| Id | Gate | Recommendation (pending) |
|---|---|---|
| `DQ-F3-1`–`-4` | surface, chooser, containers, OFF | N1; A1; X1; OFF-B |
| `DQ-F3-5` | labels | L1 safe rename, no Overwrite (L0 alternative) |
| `DQ-F3-6`, `-18` | atomicity, undo | A18-1; one undo step |
| `DQ-F3-7`, `-16` | `D1`, buffer | dedicated insertion buffer (amends `DQ-D1-7`) |
| `DQ-F3-8`, `-9`, `-17` | template | T0 (amends `F3-D2` item 5); layers untouched |
| `DQ-F3-10`, `-11` | helper; Classic, fallback | H-1, H-3; F-1 |
| `DQ-F3-12` | class | `INTEGRATED_PHASE` |
| `DQ-F3-13`, `-14` | Insert File contract, matrix | A13-1 (amends `F3-D2` item 4); A14-1 (Design R) |
| `DQ-F3-15` | macros | M0 (M1 alternative) |
| `DQ-F3-19` | inherited corrections | U0 only |
| `DQ-F3-20` | debt closure | C20-1 |

<!-- geocedg-field: implementation_base -->
## Implementation base

Reconciled on, and to be authorized on an exact descendant named by the
author:

```text
published main         = e70718fec3612814144cd405d46f6c08864ceadd (tree 26817bfc6a72b378e4babc1cd08efd0d734473c9)
preparation candidate  = 480c972972e90514e88421194a792898c916834e (tree 166caf6c7802c37db03060a2fd07cd71d28daa17)
reconciliation         = the local commit of phase/pre-g9b-r6-plus-f3-reconciliation
                         that contains this version (documentation only)
```

Before any tracked change the agent reads `AGENTS.md` and nested
instructions; verifies the live remote `main`, local `main` and `origin/main`,
the authorized base and that only documentary candidates separate it from
`main`; requires a clean worktree; confirms `D1` and `F2`/`F2-R1` published and
`F3` not implemented; creates a local phase branch; re-establishes the cited
seams; and records the dispositions applied.

**Implementation entry gate** (before product code, recorded in the candidate
report): `OTI-F3R-1` (a forced commit failure rolls back to the identical
target XML through `restoreCurrentUndoInfo()`, and the pre-insertion state
equals the last stored undo state); `OTI-F3R-2` (dry-run determinism and cost
bounds); `OTI-F3-3` (helper release); every matrix row selected by `DQ-F3-14`
has its test design; `OTI-F3R-3` only if `DQ-F3-15` selects M1+. A failed item
stops the work for the author; it is never worked around by reducing checks or
by an unauthorized inherited change.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests; `unit-system.md`
and `verification-levels.md`; the `A-1`/`A-2` and `D1` contracts; `F3-D1` to
`F3-D6` as amended by the author; the dispositions of `DQ-F3-1` to `DQ-F3-20`;
this prompt; then the design candidates, the reports and their JSON mirrors.
Probe outputs are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Under the dispositions; paths re-established at the base (design candidate §8).

- `apps/geocedg/application-profile.yml`: the `file-insert` group, its File
  entry, the two actions, cluster placement, EN/ES texts, rejection and status
  texts.
- GeoCeDG-owned Desktop code: `GeoCeDGActionRegistry`, `GeoCeDGProfile`, a new
  orchestration class (proposed `GeoCeDGDocumentInsertion`) with the
  admissibility matrix, `GeoCeDGDocumentPolicy`, `GuiManagerGeoCeDG`
  (`openFile(File)`, `doOpenFiles(File[], boolean)`, fallback-gated),
  `AppGeoCeDG` (helper flag, helper `setCurrentFile`, helper reference),
  reuse of `GeoCeDGCopyPaste` as a dedicated insertion buffer.
- Only if `DQ-F3-19` selects them: the named inherited Desktop corrections
  (U1-a `CopyPasteD.insertFrom`; U1-b `AppD.insertFile`/`applyTemplate`
  load-result checks; U1-c `GuiManagerD.openFile` Cancel null checks), each
  with a modified-files record and a Classic regression test.
- Desktop tests of the design candidate §9; amendments of the pinned tests
  (catalog size, File flatness); the `D1` guide sentence on later paste if
  `DQ-F3-16` is approved.
- The durable specification `geocedg/specs/ui/document-insertion.md`
  (proposed), `PROPOSED` until approved; `docs/upstream/modified-files.yml`
  records; the validator amendment and re-pins; the phase registration
  `PRE-G9B-R6-PLUS-F3` through the official mechanisms; the EN/ES guides; the
  candidate report, evidence and status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**` (kernel, `CopyPaste`, `InternalClipboard`,
  `ConstructionDefaults`, `TemplateHelper`, macro semantics, XML handlers,
  parsers, unit system).
- Any inherited Desktop change not selected by `DQ-F3-19`.
- Calling `AppD.insertFile` or `AppD.applyTemplate` from GeoCeDG code under
  the recommended contracts; using `TemplateHelper` for T0.
- A second geometric construction engine, a GeoCeDG reimplementation of
  construction copy, partial import, Overwrite of existing objects, reuse of a
  target macro by name, label- or coordinate-based identity inference.
- Admitting any (object class, algorithm) pair, macro case or script case not
  selected by `DQ-F3-14`/`DQ-F3-15`.
- Unit conversion, normalization or scaling; applying template units,
  construction defaults or the labeling style unless a later disposition adds
  them with an undo design.
- Document-format, XML, serialization, parser or preference-format changes;
  removing or rewriting OFF support; changes to Classic behavior beyond
  `DQ-F3-19`; new geometric commands or modes.
- `F4`, `G`, `PRE-G9B-R7`, `G9B`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than an authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop product layer (`AGENTS.md` §4): surface, profile and fail-closed
orchestration. The construction copy itself stays the kernel's
(`CopyPasteD.insertFrom` → construction-XML evaluation); geometry, dependencies,
identity, macro semantics, loading, parsing and units stay in shared services,
unchanged.

## Required design/specification

First deliverable: `geocedg/specs/ui/document-insertion.md` — the contracts of
the design candidate §2 as disposed, the admissibility matrix and exclusions,
the atomicity procedure, the template whitelist, the `D1` and layer
interactions, the user-facing messages; `PROPOSED` until approved. An ADR is
required if `DQ-F3-19` selects an inherited correction.

## Geometric invariants and degeneracies

- Design candidate §1, §2 and §7.
- Every inserted object has the source's class, parent algorithm, inputs inside
  the insertion and value; no target object or dependency changes.
- Coordinates and the target `UnitState` are identical before and after either
  operation.
- Apply Template changes only the whitelisted presentation properties.

## Compatibility and serialization

No format change. Classic and the v1 fallback keep the inherited behavior
(and its recorded defects unless `DQ-F3-19`). Ordinary GeoCeDG Open of `.cedg`,
`.ggb`, `.ggt`, `.html`, `.htm` unchanged. Rollback of the phase: revert its
commits.

## Integration obligations

One commit must show: the File surface across the profile compiler, menu bar
and registry; Insert File across the helper load, the construction copy, the
`D1` provenance and notice, the undo manager and the window buffer; Apply
Template across the `A-1`/`A-2` layer workspace and undo; the document-lifecycle
and Open suites unchanged. These facts select `INTEGRATION` under
`INTEGRATED_PHASE`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations of the design candidate §9 (`T-F3-MENU`, `T-F3-L10N`,
`T-F3-PINS`, `T-F3-CLASSIC`, `T-F3-FALLBACK`, `T-F3-OPEN-UNCHANGED`,
`T-F3-OFF`, `T-F3-GUIDES`, `T-F3R-MATRIX`, `T-F3R-REJECT`, `T-F3R-RENAME`,
`T-F3R-MACROS`, `T-F3R-ATOMIC`, `T-F3R-UNDO`, `T-F3R-D1`, `T-F3R-IDENTITY`,
`T-F3R-TEMPLATE`, `T-F3R-TEMPLATE-UNDO`, `T-F3R-TEMPLATE-FAIL`,
`T-F3R-HELPER`), restricted or extended exactly as the dispositions select,
plus a Classic regression test for each selected U1 correction, and the author
smoke `T-SMOKE` (not performed by the agent). Rejection tests compare
`getXML()`, the macro list, the undo history, the saved state, the window
buffer and the recent list before and after.

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F3 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F3 -LogDirectory artifacts\agent\<fresh>
.\tools\agent\verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase covers at least `compile.desktop.semantic`,
`workspace-profile.product` and `junit.desktop.pre-g9b-r6-plus-f3.semantic`.
`PHASE` and `INTEGRATION` (`COMPOSED` alias) run once each on the frozen clean
commit. No `FINAL`; a run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

**Author smoke (EN and ES).** File → Insert shows both actions; Open shows only
the document filter; insert a macro-free `.cedg` in m into an mm document
(notice; coordinates unchanged; one Undo removes it; Ctrl+V afterwards pastes
what was copied before, with correct labels); insert a document sharing labels
(renamed per the disposition; target objects untouched); insert documents with
a tool, a script and an unsupported object (each rejected, nothing changed);
insert a missing, a non-document and an empty file (rejected); a Locus V2
document; Apply Template onto objects on several layers including a hidden
layer, with captions, a fixed object and a conditional object (only the
whitelisted styles change; one Undo restores); type `x.off` in Open (refused);
the recent-file list unchanged by insertions and templates; Classic Open still
offers its filters.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`F3` on an exact base with the `DQ-F3-1` to `DQ-F3-20` dispositions and the
approved amendments; it then authorizes local implementation, local commits,
focused tests, the phase registration, the validator amendment and their
official catalog updates, development `INFRA_UNIT` and `STATIC` runs, the
frozen acceptance runs and one frozen technical candidate for author review and
author smoke. It never authorizes self-approval, author smoke by the agent, a
run above the frozen class, a class change, an inherited change not selected by
`DQ-F3-19`, or an amendment of `F3-D2` or `DQ-D1-7` that the author did not
approve. A reduced implementation is never reported as complete fulfilment of
the unamended `F3-D2`.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-F3      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class: PHASE + INTEGRATION)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

A later closeout record is the sole authority for approval and, per `DQ-F3-20`,
for closing `DEBT-R6PLUS-FILE-INSERT-SURFACE` and naming deferred items.

## Required artifacts

The specification; profile, Desktop, validator, registry and guide deltas with
tests; a candidate report with the entry gate (including `OTI-F3R-1`,
`OTI-F3R-2`, `OTI-F3-3`), the dispositions applied, the admissibility matrix as
implemented with its test per row, the exclusions as implemented, test
evidence, exact commands, exit codes, report paths and verdicts, and the
author-smoke checklist.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs;
- a disposition is missing, ambiguous or contradicts another;
- a target needs `source/shared/**`, serialization, parsing, unit semantics,
  `TemplateHelper` or `ConstructionDefaults`;
- an inherited Desktop change not selected by `DQ-F3-19` is needed;
- any rejection path mutates the target, the macros, the undo history, the
  saved state, the window buffer or the recent list;
- a commit cannot be verified or rolled back to the identical pre-insertion
  state;
- an admitted type fails its class, algorithm, closure, value or persistence
  test;
- the `D1` invariants cannot be kept;
- Apply Template would change a non-whitelisted property;
- the frozen class cannot cover a required change.

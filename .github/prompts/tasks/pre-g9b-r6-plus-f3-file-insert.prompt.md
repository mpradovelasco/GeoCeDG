# PRE-G9B-R6-plus-F3 — File / Insert surface and restricted native document insertion

**CANONICAL PROMPT — RISK-RECONCILED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

History of this file (each version kept in Git): prepared by
`PRE-G9B-R6-plus-F3-PREP` (commit `480c972972e90514e88421194a792898c916834e`);
reconciled by `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (commit
`0eb50311d55a099de70e3f222b885b31685ac0df`); reconciled again by
`PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction of 2026-10-12,
"Focused Feasibility of Restricted Native Insertion, Macros and Specialized
GeoCeDG Objects"), which recorded the author's preferred directions (R, M1, L1,
an independent insertion buffer, specialized objects, T0, U0) and authorized
characterization and documentary reconciliation only. Nothing in this file is
authorization.

This prompt distinguishes three kinds of content:

- **APPROVED** — the planning decisions `F3-D1` to `F3-D6`
  ([F3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decisions_record.md))
  and the accepted `D1` decisions, unchanged until the author amends them;
- **PENDING DECISION** — `DQ-F3-1` to `DQ-F3-24` and the proposed amendments of
  `F3-D2` and `DQ-D1-7`
  ([focused author-decision preparation record](../../../docs/validation/pre_g9b_r6_plus_f3_focused_author_decision_preparation_record.md)
  v3.0 §2–§4); the agent never chooses among their alternatives;
- **TECHNICAL GATE** — feasibility items the implementation entry gate must
  establish before product code (*Implementation base*).

Evidence: [focused feasibility report](../../../docs/validation/pre_g9b_r6_plus_f3_focused_feasibility_report.md)
(`G0`–`G9`), [risk closeout candidate](../../../docs/validation/pre_g9b_r6_plus_f3_risk_closeout_candidate.md)
and, as history, the earlier F3 reports. Design:
[restricted insertion design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md)
(`PROPOSED — PENDING AUTHOR REVIEW`), with §3, §4, §8, §9 of the
[F3-PREP design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md)
by reference. This file is an execution contract, not a second policy
document: units in `geocedg/specs/units/unit-system.md`; classes in
`geocedg/specs/operations/verification-levels.md` §12.8.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F3` and an exact base, disposes of every pending decision and
approves or rejects each proposed amendment. That instruction may authorize, as
the first tracked edit, an amendment of this prompt to the authorized state.

```text
PRE-G9B-R6-plus-F3 =
PLANNED — AUTHOR APPROVED (2026-10-03, F3-D1 to F3-D6)
PREPARED — RISK-RECONCILED — NOT AUTHORIZED

AUTHOR DIRECTIONS        = R / M1 / L1 / INDEPENDENT BUFFER / SPECIALIZED OBJECTS / T0 / U0
                           (2026-10-12; directions, not approvals)
RISK REGISTER            = 2 CLOSED, 8 CLOSED WITH RESTRICTION, 1 OPEN
                           (F3R-ATOMICITY-COMMIT), 0 BLOCKED
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP PRODUCT SURFACE AND A GEOCEDG-OWNED, FAIL-CLOSED
                           ORCHESTRATION OF THE KERNEL CONSTRUCTION COPY AND OF A
                           WHITELISTED STYLE COPY; no kernel, command, algorithm,
                           serialization, parser or unit change
DEBT                     = DEBT-R6PLUS-FILE-INSERT-SURFACE (closure per DQ-F3-20)
DEPENDS_ON               = PRE-G9B-R6-plus-D1 (hard, F3-D5); F2/F2-R1 published
PLANNED CLASS            = INTEGRATED_PHASE (F3-D4) — NOT FROZEN
NEXT_SUBPHASE            = none implied; F4, G stay unauthorized
```

<!-- geocedg-field: objective -->
## Objective

Deliver, as the author disposes:

1. **APPROVED** — File → Insert / Archivo → Insertar with two independent
   actions removed from the GeoCeDG Open dialog (`F3-D2` 1–2); `.cedg`/`.ggb`
   containers only (`F3-D2` 3, 8); OFF out of the GeoCeDG UI with internal OFF
   support unchanged (`F3-D2` 6–7); `D1` provenance and notice, no coordinate
   scaling (`F3-D2` 4; `F3-D5`); no geometry import and no unit change by the
   style operation (`F3-D2` 5); Classic unchanged; closure before `G`.
2. **PENDING DECISION** — the restricted insertion contract (design §1, §3:
   admission, matrix, M1 with transitive closure and digest reuse, L1 without
   overwrite, strict dry run, strict verification, operation-scoped insertion
   buffer, one undo step) under `DQ-F3-5`, `-6`, `-7`, `-13`–`-16`, `-18`,
   `-21`, `-22`, `-24`.
3. **PENDING DECISION** — the T0 style operation and its name ("Apply Styles…"
   / "Aplicar estilos…" proposed) under `DQ-F3-8`, `-17`, `-23`.
4. The surface, chooser, OFF, helper and fallback details of `DQ-F3-1`–`-4`,
   `-10`, `-11`; the class of `DQ-F3-12`; the debt closure of `DQ-F3-20`; the
   tests of design §5 and of the conservative design §9; the durable
   specification.

### Pending decisions (recommendations only)

| Id | Subject | Recommendation |
|---|---|---|
| `DQ-F3-1`–`-4` | surface, chooser, containers, OFF | N1 (+ `document.apply-styles` if `DQ-F3-23`); A1; X1; OFF-B |
| `DQ-F3-5` | labels | L1, no overwrite |
| `DQ-F3-6`, `-18`, `-21` | atomicity | admission + strict dry run + strict verification; commit residual K1 (disclosed compensating rollback) |
| `DQ-F3-7`, `-16` | buffer and `D1` | operation-scoped insertion buffer; `DQ-D1-7` amendment (record §4.2) |
| `DQ-F3-8`, `-9`, `-17`, `-23` | style operation | T0 with exclusions; "Apply Styles" |
| `DQ-F3-10`, `-11`, `-12`, `-19`, `-20` | helper, Classic, class, upstream, debt | H-1, H-3; F-1; `INTEGRATED_PHASE`; U0; C20-1 |
| `DQ-F3-13`, `-14` | contract, matrix | A13-1; matrix of the risk candidate §2 |
| `DQ-F3-15`, `-22`, `-24` | macros | M1 with restrictions; transferred macros kept after Undo (MU-1); reject identity + macros (R24-1) |

<!-- geocedg-field: implementation_base -->
## Implementation base

Risk-reconciled on, and to be authorized on an exact descendant named by the
author:

```text
published main         = e70718fec3612814144cd405d46f6c08864ceadd (tree 26817bfc6a72b378e4babc1cd08efd0d734473c9)
T_F3_PREP              = 480c972972e90514e88421194a792898c916834e (tree 166caf6c7802c37db03060a2fd07cd71d28daa17)
T_F3_RECONCILIATION    = 0eb50311d55a099de70e3f222b885b31685ac0df (tree 061e08f1a9d821cf642827afaf52d802629b940a)
focused risk closeout  = the local commit of phase/pre-g9b-r6-plus-f3-focused-risk-closeout
                         that contains this version (documentation only)
```

Before any tracked change the agent reads `AGENTS.md` and nested
instructions; verifies the live remote `main`, local `main`, `origin/main`, the
authorized base and the linear documentary chain; requires a clean worktree;
confirms `D1`, `F2`, `F2-R1` published and F3 not implemented; creates a local
phase branch; re-establishes the cited seams; records the dispositions applied.

**TECHNICAL GATES** (before product code; recorded in the candidate report; a
failed gate stops the work for the author and is never worked around):

1. under `DQ-F3-21` K1: after an injected commit-time failure the target's
   undo history has no surviving entry from the identity-bearing paste, the saved
   flag and macro list are restored, and the serializable state is identical;
2. the dry run detects every deterministic content failure class of the
   feasibility report G2 and its cost on a large document is recorded;
3. strict verification (count, pairwise class and parent algorithm, closure,
   identity records) rejects degraded objects;
4. the operation-scoped insertion buffer records provenance through the
   existing `D1` path only, and the window buffer is never touched;
5. digest-based macro reuse drops reused macros from the transfer; malformed
   macro definitions are rejected before mutation;
6. the asynchronous undo store's completion is awaited and its absence reported;
7. every matrix row selected by `DQ-F3-14` has its test design.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests;
`unit-system.md` and `verification-levels.md`; the approved `A-1`/`A-2` and
`D1` contracts (with `DQ-D1-7` as amended only if approved); `F3-D1`–`F3-D6`
as amended only if approved; the dispositions of `DQ-F3-1`–`-24`; this prompt;
then the design candidates, reports and JSON mirrors. Probe outputs are
evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Under the dispositions; paths re-established at the base (restricted design
§2; F3-PREP design §10).

- `apps/geocedg/application-profile.yml`: the `file-insert` group, its File
  entry, the two actions (names per `DQ-F3-23`), cluster placement, EN/ES texts,
  rejection, failure and status texts.
- GeoCeDG-owned Desktop code: `GeoCeDGActionRegistry`, `GeoCeDGProfile`, a new
  orchestration class (proposed `GeoCeDGDocumentInsertion`) with the
  admissibility matrix, a new operation-scoped insertion buffer (proposed
  `GeoCeDGInsertionCopyPaste`, subclass of `CopyPasteD`),
  `GeoCeDGDocumentPolicy`, `GuiManagerGeoCeDG` (`openFile(File)`,
  `doOpenFiles(File[], boolean)`, fallback-gated), `AppGeoCeDG` (helper flag,
  helper `setCurrentFile`, helper reference), `GeoCeDGUserToolLibrary` (package
  access to the existing definition digest).
- Inherited corrections only if `DQ-F3-19` selects them, each with a
  modified-files record and a Classic regression test.
- Tests of the restricted design §5 and the conservative design §9; amendments
  of the pinned tests (catalog size, File flatness).
- `geocedg/specs/ui/document-insertion.md` (proposed) `PROPOSED` until approved;
  `docs/upstream/modified-files.yml` records; the validator amendment and
  re-pins; the phase registration `PRE-G9B-R6-PLUS-F3`; EN/ES guides; candidate
  report, evidence and status lines; and, only if approved, the `DQ-D1-7`
  amendment record.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**`; any shared rollback, macro-semantics or
  construction-transfer change (`DQ-F3-21` K2 would require reclassification).
- Inherited Desktop changes not selected by `DQ-F3-19`; calling
  `AppD.insertFile` or `AppD.applyTemplate`; using `TemplateHelper` for T0.
- A GeoCeDG reimplementation of construction copy; partial import; overwrite;
  reuse of a macro by name without equal product digest; transferring
  digest-equal macros as renamed duplicates; label- or coordinate-based identity
  inference.
- Admitting any type, macro case or script case outside the dispositions;
  3D objects; projection-binding or spatial-object records; payloads with
  identity records and macros (unless a disposition changes `DQ-F3-24`).
- Using or replacing the window copy buffer for insertion; a second provenance
  mechanism; unit conversion or scaling; applying template units, construction
  defaults or the labeling style.
- Document-format, XML, serialization, parser or preference-format changes;
  removing or rewriting OFF support; Classic changes beyond `DQ-F3-19`; new
  geometric commands or modes.
- `F4`, `G`, `PRE-G9B-R7`, `G9B`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than an authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Desktop product layer (`AGENTS.md` §4), U0: surface, profile, fail-closed
orchestration and an operation-scoped buffer that drives the kernel's own
construction copy (`CopyPasteD.insertFrom` → construction-XML evaluation).
Geometry, dependencies, identity, macro semantics, loading, parsing and units
stay in shared services, unchanged.

## Required design/specification

First deliverable: `geocedg/specs/ui/document-insertion.md` — the disposed
contracts of the restricted design §1, the matrix, the transaction procedure
with its disclosed residual, the macro rules, the style whitelist and
exclusions, the `D1`, layer and identity interactions and the user-facing
messages; `PROPOSED` until approved. An ADR is required if `DQ-F3-19` selects
an inherited correction.

## Geometric invariants and degeneracies

- Restricted design §1; risk candidate §2 conditions.
- Every inserted object has the source's class, parent algorithm, inputs inside
  the insertion and value; target objects, dependencies and persistent ids are
  untouched; identity-bearing inserted objects receive new persistent ids.
- Coordinates and the target `UnitState` are identical before and after either
  operation; dimension and sheet texts follow the target units.
- The style operation changes only the six whitelisted properties.

## Compatibility and serialization

No format change. Classic and the v1 fallback keep the inherited behavior.
Ordinary GeoCeDG Open of `.cedg`, `.ggb`, `.ggt`, `.html`, `.htm` unchanged.
Rollback of the phase: revert its commits.

## Integration obligations

One commit must show the integration facts of the restricted design §4: File
surface across profile compiler, menu bar and registry; the insertion
transaction across helper load, construction copy, undo manager, macro manager
and identity registry; `D1` provenance and notice through the independent
buffer; L1 across macro and specialized objects; the style operation across the
layer workspace and undo; the Open and document-lifecycle suites unchanged.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The obligations of the restricted design §5 (`T-F3R-ATOMIC-PRECOMMIT`,
`T-F3R-ATOMIC-DRYRUN`, `T-F3R-ATOMIC-COMMIT`, `T-F3R-STRICT-VERIFY`,
`T-F3R-M1`, `T-F3R-L1`, `T-F3R-SPECIAL`, `T-F3R-BUFFER`, `T-F3R-T0`) and of the
conservative design §9 that remain applicable (`T-F3-MENU`, `T-F3-L10N`,
`T-F3-PINS`, `T-F3-CLASSIC`, `T-F3-FALLBACK`, `T-F3-OPEN-UNCHANGED`,
`T-F3-OFF`, `T-F3-GUIDES`, `T-F3R-MATRIX`, `T-F3R-REJECT`, `T-F3R-HELPER`),
restricted or extended exactly as disposed, plus the author smoke `T-SMOKE`
(not performed by the agent). Non-mutation tests compare serializable state and
runtime identity separately (feasibility report G1).

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
`VERIFICATION_ESCALATION_REQUEST`.

**Author smoke (EN and ES).** File → Insert shows both actions; Open shows only
the document filter; insert a `.cedg` in m into an mm document (notice; numbers
unchanged; one Undo removes it; Ctrl+V pastes what was copied before); insert
documents with label conflicts (renamed, target untouched); a document using a
curated tool, into an empty document and into one with the same tool active
(no duplicate tool); a document whose tool collides with a different tool
(rejected); a Locus V2 / Spline V2 document, a document with Linear and Aligned
dimensions and an IsoABorder (correct, editable, export link not imported); a
document with a script, a 3D object or Locus V2 plus a tool (rejected, nothing
changed); missing, non-document and empty files (rejected); the style
operation on several layers, captions, fixed and conditional objects and a
dimension (only whitelisted styles change; dimension unchanged; one Undo); `x.off`
in Open (refused); the recent-file list unchanged by insertions; Classic Open
still offers its filters.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`F3` on an exact base with the dispositions and the approved amendments; it
then authorizes local implementation, local commits, focused tests, the phase
registration, the validator amendment and their official catalog updates,
development `INFRA_UNIT` and `STATIC` runs, the frozen acceptance runs and one
frozen technical candidate for author review and smoke. It never authorizes
self-approval, author smoke by the agent, a run above the frozen class, a class
change, an inherited or shared change not selected by a disposition, or an
amendment the author did not approve. A reduced implementation is never
reported as complete fulfilment of the unamended `F3-D2`.

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

A later closeout record is the sole authority for approval, for the `F3-D2`
and `DQ-D1-7` amendments if approved, and, per `DQ-F3-20`, for closing
`DEBT-R6PLUS-FILE-INSERT-SURFACE` and naming deferred items.

## Required artifacts

The specification; profile, Desktop, validator, registry and guide deltas with
tests; a candidate report with the technical gates, the dispositions applied,
the matrix as implemented with its test per row, the exclusions, the
non-mutation certificates, test evidence, exact commands, exit codes, report
paths and verdicts, and the author-smoke checklist.

## Stop conditions

Stop and report, without guessing, when:

- the base, the documentary chain or a technical gate differs or fails;
- a disposition is missing, ambiguous or contradicts another;
- a target needs `source/shared/**`, serialization, parsing, unit semantics,
  `TemplateHelper` or `ConstructionDefaults`, or an unselected inherited change;
- any rejection path changes the serializable or runtime certificate;
- a commit-time failure cannot be compensated as `DQ-F3-21` disposes;
- an admitted type fails its class, algorithm, closure, value, identity,
  dynamic or persistence test;
- macro transfer would substitute, duplicate or leak a macro;
- the `D1` invariants cannot be kept with the independent buffer;
- the style operation would change a non-whitelisted property or an excluded
  object;
- the frozen class cannot cover a required change.

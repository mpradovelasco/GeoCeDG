# PRE-G9B-R6-plus-F3 — File / Insert surface and restricted native document insertion

**CANONICAL PROMPT — DECISION-RECONCILED, RECOVERY-CORRECTED, HISTORY-INTEGRITY-GATED AND AUDIT-RECONCILED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

History of this file (each version kept in Git): prepared by
`PRE-G9B-R6-plus-F3-PREP` (commit `480c972972e90514e88421194a792898c916834e`);
reconciled by `PRE-G9B-R6-plus-F3-AUTHOR-RECONCILIATION` (commit
`0eb50311d55a099de70e3f222b885b31685ac0df`); reconciled again by
`PRE-G9B-R6-plus-F3-FOCUSED-RISK-CLOSEOUT` (author instruction of 2026-10-12,
"Focused Feasibility of Restricted Native Insertion, Macros and Specialized
GeoCeDG Objects"), which recorded the author's preferred directions (R, M1, L1,
an independent insertion buffer, specialized objects, T0, U0) and authorized
characterization and documentary reconciliation only (commit
`8b41af11316f90d7a068ce6af4859213240ce6a6`); reconciled again by
`PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author instruction of
2026-10-12), which resolved `DQ-F3-21` into options, corrected the commit-time
compensation procedure (`ER-F3R-1`) and prepared the final decision sheet
(commit `3e24ed3aca422eef4e844a6c1230a3a3fc599c21`); reviewed by
`PRE-G9B-R6-plus-F3-DQ21-FOCUSED-RECOVERY-SAFETY-GATE` (author instruction of
2026-10-12), which found that the earlier K1-R procedure destroys pre-existing
Redo states (`ER-F3R-2`) and replaced the recovery gates (commit
`6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6`); corrected by
`PRE-G9B-R6-plus-F3-DQ21-RECOVERY-CORRECTIVE-FEASIBILITY-GATE` (author
instruction of 2026-10-12), which replaced the prefix isolation by a generation
barrier, the inherited reload by a guarded reload, and added document-UUID
preservation and serious-failure containment gates (commit
`6e41bf4426b9bdcb137f42e783038b02827ff3bd`); gated by
`PRE-G9B-R6-plus-F3-UNDO-HISTORY-SEMANTIC-INTEGRITY-GATE` (author instruction of
2026-10-12), which found that taking the history aside by copy loses Undo
metadata (`ER-F3R-3`), required moving the original commands through a GeoCeDG
undo-manager subclass with a pending-store tracker, and made the containment
contract a modal isolation (commit `e479b76d47c4e62baf9a4cf511ec06268c5b5ab3`);
reconciled by `PRE-G9B-R6-plus-F3-INDEPENDENT-AUDIT-RECONCILIATION` (author
instruction of 2026-10-12) with the independent final design safety audit
(`GO WITH CONDITIONS`): a distinct verified native recovery snapshot,
conservative fail-closed pending-store accounting, attach-side quiescence,
recovery-source capture by reference, save-prompt bypass, a stateless factory,
exception preservation and the stop conditions FR-F3-1 to FR-F3-8. Nothing in
this file is authorization.

This prompt distinguishes three kinds of content:

- **APPROVED** — the planning decisions `F3-D1` to `F3-D6`
  ([F3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f3_author_decisions_record.md))
  and the accepted `D1` decisions, unchanged until the author amends them;
- **PENDING DECISION** — `DQ-F3-1` to `DQ-F3-24` and the proposed amendments
  `P1`–`P7`, including those of `F3-D2` and `DQ-D1-7`
  ([Final F3 Author Decision Sheet v6.0](../../../docs/validation/pre_g9b_r6_plus_f3_final_author_decision_sheet_v6.md),
  superseding v5.0;
  [reconciled amendment proposals](../../../docs/validation/pre_g9b_r6_plus_f3_normative_amendment_proposals_reconciled.md));
  the agent never chooses among their alternatives;
- **TECHNICAL GATE** — feasibility items the implementation entry gate must
  establish before product code (*Implementation base*).

Evidence: [independent final design safety audit](../../../docs/validation/pre_g9b_r6_plus_f3_independent_final_design_safety_audit.md)
(G1–G7, N1–N5, NE1–NE2), [audit reconciliation report](../../../docs/validation/pre_g9b_r6_plus_f3_independent_audit_reconciliation_report.md),
[recovery design addendum successor](../../../docs/architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_successor_candidate.md)
(transaction, native recovery snapshot, accounting C1, isolation; superseding
the recovery procedure of every earlier document),
[audit risk register](../../../docs/validation/pre_g9b_r6_plus_f3_risk_register_audit_reconciliation.md),
[Undo history semantic integrity report](../../../docs/validation/pre_g9b_r6_plus_f3_undo_history_semantic_integrity_report.md)
(`HS-*`, H1–H5, O1–O4, P1–P4), [final DQ-F3-21 assessment](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_final_option_assessment.md)
(history-preserving barrier and gates 1–8, superseding the recovery procedure of
every previous assessment and §3 of the design successor),
[history integrity risk update](../../../docs/validation/pre_g9b_r6_plus_f3_risk_register_history_integrity_update.md),
[recovery design addendum](../../../docs/architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_candidate.md),
[corrective feasibility report](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_corrective_feasibility_report.md)
(`CF-S*`, Focus A–C), [corrected DQ-F3-21 assessment](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_corrected_option_assessment.md)
(history),
[corrective risk update](../../../docs/validation/pre_g9b_r6_plus_f3_risk_register_corrective_update.md),
[focused recovery safety report](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_recovery_safety_report.md)
(`RG-S*`, Focus A–E), [recovery option assessment](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_recovery_option_assessment.md)
(history),
[risk register update](../../../docs/validation/pre_g9b_r6_plus_f3_risk_register_recovery_gate_update.md),
[decision reconciliation report](../../../docs/validation/pre_g9b_r6_plus_f3_author_decision_reconciliation_report.md),
[DQ-F3-21 option assessment](../../../docs/validation/pre_g9b_r6_plus_f3_dq21_option_assessment.md)
(`DQ21-Q1`–`Q5`, history), [risk register reconciliation](../../../docs/validation/pre_g9b_r6_plus_f3_risk_register_reconciliation.md),
[focused feasibility report](../../../docs/validation/pre_g9b_r6_plus_f3_focused_feasibility_report.md)
(`G0`–`G9`), [risk closeout candidate](../../../docs/validation/pre_g9b_r6_plus_f3_risk_closeout_candidate.md)
(as corrected by `ER-F3R-1`) and, as history, the earlier F3 reports. Design:
[restricted insertion design successor candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md)
(`PROPOSED — PENDING AUTHOR REVIEW`), with the
[restricted insertion design candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md)
where the successor does not change it and §3, §4, §8, §9 of the
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
PREPARED — DECISION-RECONCILED — RECOVERY-CORRECTED — HISTORY-INTEGRITY-GATED
— AUDIT-RECONCILED — NOT AUTHORIZED

AUTHOR DIRECTIONS        = R / M1 / L1 / INDEPENDENT BUFFER / SPECIALIZED OBJECTS / T0 / U0
                           (2026-10-12; directions, not approvals); for this
                           reconciliation: accounting C1, recovery copy keeps
                           the UUID, no export of uncertified state (preferred,
                           not ratified)
FINAL DECISION SHEET     = Q1–Q7 (Final F3 Author Decision Sheet v6.0), all PENDING
INDEPENDENT AUDIT        = GO WITH CONDITIONS (0 blockers; G1–G7 gates; N1–N5;
                           NE1–NE2), reconciled
RISK REGISTER            = see the audit risk register: 6 OPEN IMPLEMENTATION
                           GATE, 2 OPEN NOT ESTABLISHED (containment, EDT),
                           1 OPEN PENDING AUTHOR DISPOSITION
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
PLANNED CLASS            = INTEGRATED_PHASE (F3-D4) — NOT FROZEN; the frozen plan
                           names integration coverage of the existing Desktop
                           undo, document-lifecycle and identity suites
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
| `DQ-F3-6`, `-18`, `-21` | atomicity | history-preserving barrier (conservative fail-closed accounting C1, verified native recovery snapshot, original history moved by a GeoCeDG undo-manager subclass, generation barrier, attach-side quiescence) + admission + strict dry run + strict verification; K1-S and K1-R-H guarded reload; destination UUID preserved; isolation with three exits, no export of the uncertified state; K1-R with undo/prune withdrawn |
| `DQ-F3-7`, `-16` | buffer and `D1` | operation-scoped insertion buffer; `DQ-D1-7` amendment (record §4.2) |
| `DQ-F3-8`, `-9`, `-17`, `-23` | style operation | T0 with exclusions (`P5`); "Apply Styles…" / "Aplicar estilos…" |
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
T_F3_RISK              = 8b41af11316f90d7a068ce6af4859213240ce6a6 (tree 8be60e86e09f957f1735990b966190bd44ffab2a)
T_F3_DECISION_RECONCILIATION
                       = 3e24ed3aca422eef4e844a6c1230a3a3fc599c21 (tree 65e282431a6ece824c783e8217bed74b85b79e68)
T_F3_RECOVERY_SAFETY   = 6c07e01a834e0a3ecd7af7290bc7d2ddef50ceb6 (tree 8f2e141bcb1311051b14d8efb3d7289b15e95212)
T_F3_RECOVERY_CORRECTIVE
                       = 6e41bf4426b9bdcb137f42e783038b02827ff3bd (tree 8b614863de45a7fccb93ec751871498c8ee7bbd1)
T_F3_UNDO_HISTORY_INTEGRITY
                       = e479b76d47c4e62baf9a4cf511ec06268c5b5ab3 (tree 749005c525af0d54724fa9acc1608bde2420253a)
audit reconciliation   = the local commit of
                         phase/pre-g9b-r6-plus-f3-independent-audit-reconciliation
                         that contains this version (documentation only)
```

Before any tracked change the agent reads `AGENTS.md` and nested
instructions; verifies the live remote `main`, local `main`, `origin/main`, the
authorized base and the linear documentary chain; requires a clean worktree;
confirms `D1`, `F2`, `F2-R1` published and F3 not implemented; creates a local
phase branch; re-establishes the cited seams; records the dispositions applied.

**TECHNICAL GATES** (before product code; recorded in the candidate report; a
failed gate stops the work for the author and is never worked around):

1. under `DQ-F3-21` as disposed (recovery design addendum successor §1–§4),
   on the EDT: a GeoCeDG `UndoManagerD` subclass installed through a
   **stateless** `AppGeoCeDG.getUndoManager(Construction)` override (G6) serves
   every GeoCeDG document and leaves ordinary Undo behavior unchanged; its
   pending-store accounting is the conservative fail-closed contract C1
   (registration in `storeUndoInfo`, thread-local marker for synchronous
   callers, success flag from the store thread's `pruneStateList`, no decrement
   on exception exits, `degraded` on any anomaly, reset only by
   `commitUndoBaseline`) and is proven exit by exit (G2); in one block
   synchronized on the undo manager the insertion requires `pending == 0` and
   not degraded (bounded wait outside the monitor, else rejection before
   mutation), captures and preflights the **complete native recovery snapshot**
   (`writeGeoGebraFile(OutputStream, false)`, UUID equal to the live one,
   provenance recorded; failure → rejection before mutation, G1), detaches the
   original history (moved, never copied — `undoHistoryTo` is not used,
   `ER-F3R-3`), commits a baseline (`commitUndoBaseline`, cancelling every
   earlier store) and captures that baseline's `AppState` **by reference** as
   the reload source (G4); re-attachment happens only in a synchronized block
   with `pending == 0`, not degraded and an unchanged temporary list, else
   after a closing barrier (G3); attach installs first and refreshes actions
   best-effort; failures in cleanup are added as suppressed to the original
   failure (G7); labels, grouping, cursor, Redo and snapshot files are
   identical after every path (H2–H5, O1–O4); the paste's own store is
   identified by generation and content with a bounded wait; `undo()` +
   `pruneStateList()` and the inherited `restoreCurrentUndoInfo` are never used
   as recovery (`ER-F3R-1`, `ER-F3R-2`);
2. K1-S (if disposed): after an injected post-commit failure on a payload
   without identity records, subtractive removal, macro removal, UUID restore
   and reinstatement of the held history leave the full document XML
   byte-identical, the complete Undo/Redo history (list and cursor) identical
   in U1–U4, live instances, the Parallel/Perpendicular memory and the
   IsoABorder link kept, the saved flag restored; a failed removal goes to the
   guarded reload and is certified again (B-F2);
3. K1-R-H (if disposed): after an injected failure on a Locus V2 or Spline V2
   payload, the guarded reload of the barrier baseline undo archive (captured
   by reference; a reload source only, never the recovery copy) (no history access, no
   inherited fallback, one retry) and reinstatement of the held history leave
   the full document XML, the root UUID and the complete Undo/Redo history
   (U1–U4, with an equal-content pending store) identical, no later Undo/Redo
   makes the insertion reappear, the saved flag is restored without saving,
   and only the declared session reset occurs (A-Q1, B-F3); on success the
   destination UUID is kept, the insertion is one undo step made by the
   orchestration (the paste's own store replaced for identity payloads) and the
   held Redo branch is discarded as for any new action (A-Q6, Focus C);
4. serious failure: a failure inside the inherited rollback (`DQ21-Q5`), two
   failed guarded reloads (B-F4, B-F5), an unexpected or missing store, or a
   failed certificate re-attaches the original history unchanged (or keeps it
   held if attach is unsafe), transfers the native recovery snapshot to the
   isolation controller and isolates the drawing (addendum successor §5, `P7`):
   modal dialog; GeoCeDG isolation flag refusing Save, Save As, Export, Insert
   File, Apply Styles, clipboard and any save of the live state; update scripts
   blocked, global listeners suppressed, animation stopped, Undo/Redo suspended
   (`Kernel.setUndoActive(false)`), file drop and in-process API mutation
   refused; the `P6` dialog's only exits are **save the verified native
   recovery snapshot** to a new file (original UUID; never the current file; no
   silent replacement; atomic publication; published bytes re-validated),
   **reopen** the document (Undo re-enabled first, native open of the existing
   file) and **close** — none routes through an inherited save-changes prompt
   (G5); no export of the uncertified state; the snapshot is released only
   after the exit completes; the certificate includes full XML, root UUID,
   identities, macro definition digests, history list, cursor and per-entry
   labels and grouping, saved flag and file association;
5. the dry run detects every deterministic content failure class of the
   feasibility report G2 and its cost on a large document is recorded;
6. strict verification (count, pairwise class and parent algorithm, closure,
   identity records) rejects degraded objects;
7. the operation-scoped insertion buffer records provenance through the
   existing `D1` path only, and the window buffer is never touched;
8. digest-based macro reuse drops reused macros from the transfer; malformed
   macro definitions are rejected before mutation;
9. every matrix row selected by `DQ-F3-14` has its test design.

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
  helper `setCurrentFile`, helper reference, the `getUndoManager(Construction)`
  override installing the GeoCeDG undo-manager subclass, and — only as
  `DQ-F3-21` disposes — the isolation wiring), a GeoCeDG undo-manager subclass
  (proposed `GeoCeDGUndoManager extends UndoManagerD`: detach/attach of
  original commands, conservative fail-closed pending-store accounting C1;
  bookkeeping-only overrides that delegate unchanged), the native recovery
  snapshot holder, the serious-failure isolation controller, dialog and guards,
  `GeoCeDGUserToolLibrary` (package
  access to the existing definition digest).
- Inherited corrections only if `DQ-F3-19` selects them, each with a
  modified-files record and a Classic regression test.
- Tests of the restricted design §5, the design successor §6 and the
  conservative design §9; amendments
  of the pinned tests (catalog size, File flatness).
- `geocedg/specs/ui/document-insertion.md` (proposed) `PROPOSED` until approved;
  `docs/upstream/modified-files.yml` records; the validator amendment and
  re-pins; the phase registration `PRE-G9B-R6-PLUS-F3`; EN/ES guides; candidate
  report, evidence and status lines; and, only if approved, the `DQ-D1-7`
  amendment record.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change in `source/shared/**`; any shared rollback, macro-semantics or
  construction-transfer change, and any shared undo-storage change
  (`DQ-F3-21` K2 would require a separate reclassified prompt).
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
contracts of the restricted design §1, the matrix, the store-aware transaction
of the design successor §2 and the barrier transaction, containment and gates
of the recovery design addendum successor and `P6`/`P7` as disposed (the final
`DQ-F3-21` assessment and the first addendum as history), the macro
rules, the style whitelist and
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
`T-F3R-M1`, `T-F3R-L1`, `T-F3R-SPECIAL`, `T-F3R-BUFFER`, `T-F3R-T0`), of the
design successor §6 (`T-F3R-UNDO-RACE`, `T-F3R-RECOVERY-S`, `T-F3R-RECOVERY-R`
— now for K1-R-H —, `T-F3R-RECOVERY-SERIOUS`, for the branches `DQ-F3-21`
disposes), of the updated assessment §4 gate 9 (`T-F3R-REDO-BRANCH` U1–U4 per
path and payload class, `T-F3R-QUIESCENCE`, `T-F3R-DOCUMENT-ID`,
`T-F3R-SAVED-STATE` clean and modified, `T-F3R-RELOAD-FAULT`), of the corrected
assessment §4 (`T-F3R-BARRIER` A-Q1/A-Q3/A-Q4 on the EDT, `T-F3R-GUARDED-RELOAD`
B-F2–B-F5, `T-F3R-SUCCESS-UUID` for the four payload classes with Save, Save As
and reopen, `T-F3R-CONTAINMENT` B-F4/B-F6), of the recovery design addendum
§3 (`T-F3R-HISTORY-METADATA`, `T-F3R-HISTORY-OWNERSHIP`, regression
`ER-F3R-3`), of the recovery design addendum successor §6
(`T-F3R-NATIVE-SNAPSHOT`, `T-F3R-ACCOUNTING`, `T-F3R-ATTACH`,
`T-F3R-SOURCE-CAPTURE`, `T-F3R-ISOLATION-ROUTES`, `T-F3R-ISOLATION-EXITS`,
`T-F3R-FACTORY`, `T-F3R-EDT`; these supersede `T-F3R-PENDING-TRACKER` and
`T-F3R-ISOLATION`), the existing `AppGeoCeDG`-based suites named in the audit
reconciliation report §9 as integration coverage, and of the
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
- a commit-time failure cannot be compensated as `DQ-F3-21` disposes, or a
  compensation would need a restore while a store can still land, the
  inherited undo-load fallback, a copy of the history (`undoHistoryTo`), a
  shared change, an unidentified undo store, or would remove pre-existing Redo
  states;
- an admitted type fails its class, algorithm, closure, value, identity,
  dynamic or persistence test;
- macro transfer would substitute, duplicate or leak a macro;
- the `D1` invariants cannot be kept with the independent buffer;
- the style operation would change a non-whitelisted property or an excluded
  object;
- the frozen class cannot cover a required change.

Named F3 recovery stop conditions (blocking; never reinterpreted as
observations; on failure preserve the evidence and request a new author
decision):

- **FR-F3-1 Undo accounting** — the conservative pending-store accounting C1
  cannot be shown free of unsafe false negatives on every exit path (success,
  stale discard, stale and current exception exits, other `Error`,
  thread-creation failure, delayed notification); no automatic switch to C2 or
  C3;
- **FR-F3-2 History semantics** — labels, grouping, Redo, cursor, checkpoints
  or snapshot ownership cannot be preserved;
- **FR-F3-3 Native recovery snapshot** — the complete verified recovery archive
  cannot be captured, validated or retained safely until isolation ends;
- **FR-F3-4 Serious-failure isolation** — an uncertified drawing can still be
  edited, saved or exported through an admitted mutation route, or an exit
  routes through a save-changes prompt;
- **FR-F3-5 EDT safety** — synchronization deadlocks, blocks indefinitely or
  orders operations invalidly;
- **FR-F3-6 Document identity** — a successful insertion changes the
  destination document UUID, or the recovery copy loses the approved identity
  semantics;
- **FR-F3-7 Semantic admission** — an accepted construction cannot be verified
  for DAG, identities, algorithms, macros and specialized-object conditions;
- **FR-F3-8 Compatibility** — the implementation introduces an unauthorized
  shared or Classic behavior change.

# PRE-G9B-R6-plus-F2-R1 — dual-mode Parallel/Perpendicular activation (corrective)

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — DESIGN CANDIDATE PENDING AUTHOR REVIEW — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION`
(Stage 2) at the author's instruction, after the Stage 1 characterization
`PRE-G9B-R6-plus-F2-R1-ACTIVATION-CHARACTERIZATION`. It is the execution
contract of a corrective successor of the frozen F2 technical candidate. The
governing design is the
[reconciled dual-activation design candidate](../../../docs/architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md)
(pending author review); the decisions and proposals are in the
[F2-R1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f2_r1_author_decisions_record.md)
v1.0; tests and smoke in the
[test matrix and author-smoke plan](../../../docs/validation/pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-F2-R1`, confirms or amends the proposals `DQ-F2R1-2` to
`DQ-F2R1-5`, approves the design, and names the exact base. That instruction may
authorize, as the first tracked edit, an amendment of this prompt to the
authorized state (F1/F2 precedent). This file is not a second policy document:
state classes are in `geocedg/specs/units/unit-system.md` §17.2; verification
classes in `geocedg/specs/operations/verification-levels.md` §12.8; F2 memory
semantics in the [F2 specification](../../../geocedg/specs/ui/authoring-memory.md)
and the F2 decision record; F2-R1 semantics in the reconciled design, whose
section numbers are cited below.

```text
PRE-G9B-R6-plus-F2-R1 =
PREPARED — NOT AUTHORIZED

DESIGN                   = CANDIDATE PENDING AUTHOR REVIEW
AUTHOR DECISIONS         = DQ-F2R1-1 direction (Candidate A, conditional) and DQ-F2R1-6
                           scope recorded; DQ-F2R1-2..5 proposals pending confirmation
F2 ORIGINAL CANDIDATE    = 6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873 (FROZEN — NOT AUTHOR
                           APPROVED — NOT PUBLISHED)
IMPLEMENTATION           = NOT AUTHORIZED
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = DESKTOP AUTHORING-MODE CORRECTION: activation behaviour
                           NORMAL / MEMORY_ENABLED for Parallel and Perpendicular,
                           toolbar-modifier capture, AppGeoCeDG.setMode coordination,
                           memory gate, status-bar indication; no kernel, command,
                           algorithm, XML, identity, catalog, GV2 or Classic change
PROPOSED CLASS           = BOUNDED_PHASE — NOT FROZEN; new phase PRE-G9B-R6-PLUS-F2-R1
NEXT_SUBPHASE            = none implied; F3, F4, G stay unauthorized
```

<!-- geocedg-field: objective -->
## Objective

Give the existing Parallel and Perpendicular tools two operating behaviours
(design §1):

1. `NORMAL` — the inherited GeoGebra tool; the F2 memory is never read, written
   or cleared by the tool.
2. `MEMORY_ENABLED` — the F2 remembered-reference behaviour unchanged (reuse of a
   current, usable slot; write only after a certified construction).

The behaviour is selected at activation (design §2–§3): Ctrl (Windows/Linux) on a
native-toolbar or flyout activation requests `MEMORY_ENABLED`, any other
activation `NORMAL`; it is latched until the next activation or until another
tool is chosen; a behaviour change on an active tool cancels the partial gesture
through the inherited protected `initNewMode(mode)`; a status-bar segment shows
`MEMORY_ENABLED` (design §5). Every F2 obligation not amended in the decision
record §2 remains in force.

<!-- geocedg-field: implementation_base -->
## Implementation base

To be named exactly by the authorizing instruction. Prepared on:

```text
F2 ORIGINAL CANDIDATE = 6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873
                        tree 3d412a801024411ebfcbaf8469691ec00f7c4fee
STAGE 1               = b7faad3af2cbce0c2254cbdc7366c96a3df5ad48
                        tree 980207638c8bd045621879b2a28f407104b08bc4
STAGE 2               = the local commit of phase/pre-g9b-r6-plus-f2-r1-design that
                        contains this file (documentation only), child of b7faad3a
PUBLISHED MAIN        = 6d9bafb04235a71c8dfadef9631cd6057964b1f9 (does not contain F2)
```

**Entry gate.** Verify the named base, its ancestry to `6af85ee3`, the clean
worktree and the F2 receipts; confirm that free `localized_text` keys pass
`tools/agent/workspace-profile-validation.ps1`; re-establish the seams of design
§2 (`ProfileToolbar.addNativeGroup`, `App.setMode(int, ModeSetter)` overridable
in `AppGeoCeDG`, protected `initNewMode`, `GeoCeDGStatusBar` segments); freeze
`BOUNDED_PHASE` only if every planned change is GeoCeDG-owned Desktop code,
profile texts, tests, registration and documentation. Otherwise stop.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. Then: current code and tests at the base; the author
instruction that authorizes F2-R1; the F2-R1 decision record; this prompt; the
F2-R1 reconciled design; the F2 decision record v1.1 and the F2 specification
for every unamended F2 rule; Stage 1 and Stage 2 evidence. Probe outputs and
reports are evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

GeoCeDG-owned Desktop code (`source/desktop/desktop/src/main/java/org/geocedg/desktop/`):

- `GeoCeDGToolbarContainer.ProfileToolbar.addNativeGroup`: one `ActionListener`
  per native group button that records `{mode, behaviour, current AWT event}` for
  PAR/PERP actions from the event's own modifier.
- `AppGeoCeDG`: override of `setMode(int, ModeSetter)` resolving the behaviour
  for every activation (token match → request; otherwise `NORMAL`; other modes →
  both tools `NORMAL`), then `super.setMode`.
- `GeoCeDGEuclidianController`: behaviour state of the active tool,
  `applyActivationBehaviour` with `initNewMode(mode)` on a same-mode behaviour
  change, and the `NORMAL` gate in `processLineReferenceTool`.
- `GeoCeDGLineReferenceMemory`: only if the behaviour is stored there; slot,
  currentness, certificate and lifecycle logic unchanged.
- `GeoCeDGStatusBar`: one hidden-by-default segment and separator; localized text
  keys (EN, ES) in `apps/geocedg/application-profile.yml` `localized_text` only.
- Tests: a new `PreG9BR6PlusF2R1DualActivationTest`; `PreG9BR6PlusF2AuthoringMemoryTest`
  adapted to activate `MEMORY_ENABLED` explicitly, obligations unchanged.
- Specification: an F2-R1 amendment specification or an amended
  `geocedg/specs/ui/authoring-memory.md` version, status `PROPOSED`.
- Registration of phase `PRE-G9B-R6-PLUS-F2-R1`, JUnit inventory and pins,
  `docs/upstream/modified-files.yml`, guides EN/ES, candidate report and evidence,
  minimal status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- `source/shared/**`; inherited toolbar classes (`ToolToggleButton`,
  `ModeToggleMenuD`, `ToolbarD`, `ToolbarContainer`), `EuclidianController`,
  `EuclidianView`, `App`, `AppD`, `GuiManager*` other than GeoCeDG subclasses.
- Reading `AppD.getControlDown()` or any global keyboard state as the activation
  modifier; synthetic tool changes to force controller reinitialization.
- New tool ids, commands, aliases, algorithms, `GeoElement` types, actions,
  catalog entries or toolbar icons; any change to construction semantics, the
  DAG, identity, document or macro XML, undo behaviour of constructions.
- Any change to F2 currentness, admissibility, certificate, lifecycle,
  serialization or undo rules; Angle with Given Size; Graphics View 2; Classic;
  `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`.
- Claiming macOS or Linux modifier support without verification.
- Editing the F2 decision record, the F2 reconciled design, the Stage 1/Stage 2
  artifacts other than by an authorized amendment; `AGENTS.md`, `CLAUDE.md`,
  `FIRST_AGENT_TASK.md`, `.github/prompts/**` (other than an authorized amendment
  of this prompt), `ai-shell/prompts/**`.
- `F3`, `F4`, `G`, publication, merge, tag, release.

## Architectural placement

Desktop application/controller layer (`AGENTS.md` §4): activation coordination
and a transient presentation segment. The behaviour is `SESSION` state; nothing
is persisted. Kernel, export adapters, Python: unchanged.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Obligations: the test matrix of the
[test matrix and author-smoke plan](../../../docs/validation/pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md)
§1 (`T-R1-*`) and every `T-F2-*` obligation executed in `MEMORY_ENABLED`; author
smoke §2 (not performed by the agent).

Commands (PowerShell; fresh log directories under `artifacts\agent\`):

```powershell
.\gradlew.bat :desktop:desktop:test --tests <focused F2-R1 and F2 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F2-R1 -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-F2-R1` covers `compile.desktop.semantic`,
`junit.desktop.pre-g9b-r6-plus-f2-r1.semantic` (F2-R1 class, the adapted F2
class, toolbar and status-bar regression classes, the F2 regression selection)
and `workspace-profile.product`. `PHASE` runs once on the frozen clean candidate.
No `INTEGRATION` or `FINAL`. The F2 `PHASE` receipt is not inherited.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
F2-R1 on an exact base; it then authorizes local implementation on a local
branch, local commits, focused tests, the phase registration and catalog
updates, development and acceptance `STATIC`, `INFRA_UNIT` and `PHASE`, and one
frozen technical candidate for author review and smoke. It never authorizes
self-approval, author smoke by the agent, a run above the frozen class or a
change of class. If Candidate A cannot satisfy the contract, the work stops;
Candidate B1 needs a separate author decision. F2 and F2-R1 author approval are
never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, merge, promotion to `main`, rebase,
squash, amend after freeze, force push, tag, release, installer or version change
are forbidden; each needs a separate explicit author instruction naming the exact
candidate SHA.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-F2-R1   = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

## Stop conditions

- The base or entry gate differs from *Implementation base*.
- A same-mode indirect activation cannot be resolved to `NORMAL` within
  GeoCeDG-owned code, or a host path re-asserts the active mode and would reset an
  intentional `MEMORY_ENABLED` without user action.
- `initNewMode` cancellation changes anything other than the partial gesture or
  stores an undo point.
- The status texts require an action, catalog, schema or icon change.
- Any shared, inherited-class, kernel, command, algorithm, XML or identity change.
- The frozen class cannot cover a required change.

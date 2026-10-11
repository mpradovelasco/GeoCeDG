# PRE-G9B-R6-plus-F2-R1 — dual-mode Parallel/Perpendicular activation (corrective)

**CANONICAL PROMPT — AUTHORIZED FOR CONDITIONAL IMPLEMENTATION (author authorization "PRE-G9B-R6-plus-F2-R1 Implementation Authorization") — IMPLEMENTATION PUBLICATION NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION`
(Stage 2) at the author's instruction, after the Stage 1 characterization
`PRE-G9B-R6-plus-F2-R1-ACTIVATION-CHARACTERIZATION`, and reconciled by
`PRE-G9B-R6-plus-F2-R1-DESIGN-CLOSEOUT` with the author dispositions of
`DQ-F2R1-1` to `DQ-F2R1-6`. It is the execution contract of a corrective
successor of the frozen F2 technical candidate. The governing design is the
[reconciled dual-activation design](../../../docs/architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED`); the decisions are in the
[F2-R1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f2_r1_author_decisions_record.md)
v1.1 §5; tests and smoke in the
[test matrix and author-smoke plan](../../../docs/validation/pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md).
The author instruction "PRE-G9B-R6-plus-F2-R1 Implementation Authorization"
(record v1.2 §6) authorizes the conditional bounded corrective implementation on
the exact base below, up to one frozen local technical candidate; this amendment
is its first tracked change. The feasibility gate `FR-1` to `FR-4` must pass
before production behaviour changes; a failed gate stops the work. This file is not a second policy document:
state classes are in `geocedg/specs/units/unit-system.md` §17.2; verification
classes in `geocedg/specs/operations/verification-levels.md` §12.8; F2 memory
semantics in the [F2 specification](../../../geocedg/specs/ui/authoring-memory.md)
and the F2 decision record; F2-R1 semantics in the reconciled design, whose
section numbers are cited below.

```text
PRE-G9B-R6-plus-F2-R1 =
DESIGN — AUTHOR APPROVED
CANONICAL PROMPT — AUTHORIZED FOR CONDITIONAL IMPLEMENTATION

DESIGN                   = DESIGN — AUTHOR APPROVED (record v1.1 §5)
AUTHOR DECISIONS         = DQ-F2R1-1 to DQ-F2R1-6 RECORDED (with refinements)
FEASIBILITY ITEMS        = FR-1 to FR-4 open; resolved at the entry gate (design §8)
F2 ORIGINAL CANDIDATE    = 6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873 (FROZEN — NOT AUTHOR
                           APPROVED — NOT PUBLISHED)
IMPLEMENTATION BASE      = e13bfbd231700019a8f81c115b22a5bb8c44f7bb
                           tree 2e8ee23addebcb05c4505c30e1fa593c8a3d4fa7
IMPLEMENTATION           = AUTHORIZED (conditional on FR-1..FR-4)
PUBLICATION              = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false (implementation pending author review and smoke)
implementationAuthorized = true
passClaimed              = false
PHASE_KIND               = DESKTOP AUTHORING-MODE CORRECTION: activation behaviour
                           NORMAL / MEMORY_ENABLED for Parallel and Perpendicular,
                           toolbar-modifier capture, AppGeoCeDG.setMode coordination,
                           memory gate, status-bar indication; no kernel, command,
                           algorithm, XML, identity, catalog, GV2 or Classic change
VERIFICATION CLASS       = BOUNDED_PHASE — frozen only after FR-1..FR-4 pass; phase
                           PRE-G9B-R6-PLUS-F2-R1; no INTEGRATION or FINAL
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

The behaviour is selected at activation (design §2–§4): Ctrl (Windows; macOS
and Linux not claimed) read from a native-toolbar, reactivation or native-flyout
activation event requests `MEMORY_ENABLED`; every other genuine activation
request (menu, registry, user tools, keyboard Space; scripting under `FR-2`)
requests `NORMAL`, also for the active tool; host synchronization of the active
mode changes nothing; departure resets to `NORMAL`; a language change preserves
the active tool and behaviour (`FR-1`); a behaviour change on the active tool
cancels the partial gesture through the inherited protected `initNewMode(mode)`
(`FR-3`); a localized status-bar segment shows `MEMORY_ENABLED` (design §7,
`FR-4`). Every F2 obligation not refined in the decision record §5 remains in
force.

<!-- geocedg-field: implementation_base -->
## Implementation base

Authorized: the F2-R1 design closeout `e13bfbd231700019a8f81c115b22a5bb8c44f7bb`
(tree `2e8ee23addebcb05c4505c30e1fa593c8a3d4fa7`); chain `6d9bafb0` → `bdb9b6a9` →
`6af85ee3` → `b7faad3a` → `d630b8dd` → `e13bfbd2`; branch
`phase/pre-g9b-r6-plus-f2-r1` (local). Prepared on:

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
worktree and the F2 receipts; resolve the feasibility items of design §8
(`FR-1` language-change restoration, `FR-2` scripting seam, `FR-3` cancellation
conditions including saved state, `FR-4` status texts and layout); re-establish
the seams of design §2 (`ProfileToolbar.addNativeGroup`, `App.setMode(int,
ModeSetter)` and `setLanguage(Locale)` overridable in `AppGeoCeDG`, registry and
user-tool call sites, protected `initNewMode`, `GeoCeDGStatusBar` segments); freeze
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
  (pending genuine request → its behaviour; other mode without request →
  departure, both tools `NORMAL`; same mode without request → host
  synchronization, unchanged), then `super.setMode`; override of
  `setLanguage(Locale)` preserving and restoring the active Parallel/Perpendicular
  tool and behaviour around the inherited switch (`FR-1`).
- `GeoCeDGActionRegistry` and GeoCeDG user-tool call sites: scoped `NORMAL`
  request markers around their own `app.setMode`; a GeoCeDG-owned scripting-API
  seam only if `FR-2` establishes one without inherited-class change.
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

The author instruction of record v1.2 §6 authorizes F2-R1 on the exact base
above, conditional on `FR-1` to `FR-4`: local implementation on a local
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
- A genuine same-mode indirect activation cannot be resolved to `NORMAL`, or host
  synchronization cannot be kept from changing the behaviour, within
  GeoCeDG-owned code.
- `FR-1` fails: the active tool and behaviour cannot be preserved across a
  language change without an inherited-class change or a GUI regression.
- `FR-2` finds no safe GeoCeDG-owned scripting seam (report; scripting same-mode
  activations then stay host synchronization pending author disposition).
- `initNewMode` cancellation changes anything other than the partial gesture or
  stores an undo point.
- The status texts require an action, catalog, schema or icon change.
- Any shared, inherited-class, kernel, command, algorithm, XML or identity change.
- The frozen class cannot cover a required change.

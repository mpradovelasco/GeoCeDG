# PRE-G9B-R6-plus-E3 — native `IsoABorder` and `ExportArea` integration

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED — PREPARED — NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-E3-PREP` at the author's
instruction of 2026-10-09, given after `POST-E2-P4` was closed and published.
That instruction authorized characterization, design reconciliation, author
decision preparation and this prompt only. It stated that **product
implementation is not authorized**, that the `P0` design candidate is evidence
and not authority, and that no design candidate or recommendation may be marked
author-approved. The evidence behind the characterization summarized below is
in the
[E3 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md)
(sections `K0`–`K21`); the proposed design, the alternatives, the validation
matrix and the full decision table are in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-E3` and the exact prepared candidate or base, and that
disposes of the requested decisions `DQ-E3-1` to `DQ-E3-16`. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `A-1`, `B`, `D0`, `D1`, `A-2`,
`C`, `C-X1`, `E1-L`, `E1-X1` and `E2` precedent. This file is an execution
contract, not a second policy document: the unit and scale rules are stated
once in the [unit-system specification](../../../geocedg/specs/units/unit-system.md)
(§5.3, §6, §7.3, §12, §13, §17, §18.4); the `ExportArea` authority once in the
[B author decisions](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md)
(`AQ-X2`, `AQ-X7`); the command-head rules once in
[ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md);
the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the probe evidence once
in the characterization report. Where this prompt names a design value that no
durable specification holds yet, the value is a proposal until the author
disposes of the matching `DQ`, and the first deliverable of `E3` turns it into
the durable specification named under *Required design/specification*.

```text
PRE-G9B-R6-plus-E3 =
PREPARED — NOT AUTHORIZED

PREPARATION ACTIVITY     = PRE-G9B-R6-plus-E3-PREP (DOCUMENTATION_STATUS_ONLY)
CHARACTERIZATION         = COMPLETE (2026-10-09, on P_R6PLUS_POST_E2_P4 13e08ac1)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = SHARED-KERNEL COMMAND AND ALGORITHM + DESKTOP MODE,
                           DIALOG, PROFILE AND HELP + TWO SESSION PRODUCERS OF
                           THE SINGLE ExportArea + ADDITIVE COMMAND
                           SERIALIZATION WITH EXISTING OUTPUT TYPES
                           (representation R2 proposed, DQ-E3-1)
DEPENDS_ON               = PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (hard: unit state, §18.4 capture)
                           PRE-G9B-R6-plus-B  = PASS — AUTHOR APPROVED — PUBLISHED
                                                (the ExportArea linkage only)
                           PRE-G9B-R6-plus-C  = PASS — AUTHOR APPROVED — PUBLISHED
                                                (session drawingScale, physical export)
                           PRE-G9B-R6-plus-E1, E2 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (recommended predecessors)
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PROPOSED CLASS           = INTEGRATED_PHASE — NOT FROZEN (DQ-E3-16)
PRECONDITION             = author dispositions of DQ-E3-1 to DQ-E3-16
NEXT_SUBPHASE            = none implied; F1, F2, F3, F4, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-defined `IsoABorder` scope (mini-track plan §2, §3.2 `E3`
brief, §8.6; `unit-system.md` §18.4; `B` `AQ-X7`;
`ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`) as a shared-kernel command with a
Desktop menu tool and the two ISO producers of the single `ExportArea`:

1. A native command (default head `IsoABorder`, Spanish alias of `DQ-E3-14`)
   implemented by a GeoCeDG shared-kernel algorithm whose outputs are existing
   object types (`DQ-E3-1`, `DQ-E3-2`, `DQ-E3-11`): the trimmed ISO 216 sheet
   edge, the drawing frame and the construction label, from an upper-left
   point, an ISO A index, an orientation, a captured scale pair, a captured
   model-units-per-millimetre factor and (under `DQ-E3-2` (b)) a margin.
2. A pure shared helper holding the ISO 216 table and the canonical conversion
   (`DQ-E3-5`), used by the command and by `ISO_A_SELECTION`.
3. A menu-only Desktop tool under `Construction → Annotations and media`
   (design candidate §9) with the unit gate (`DQ-E3-7`), one-shot capture of
   `u` and `a:b` (`unit-system.md` §6.3, §18.4), and the yes/no linkage
   question.
4. The `ISO_A_BORDER` and `ISO_A_SELECTION` producers of `ExportAreaSession`
   with the precedence of `AQ-X2` unchanged, the link lifecycle of `DQ-E3-9`,
   and the File actions of `DQ-E3-14`.
5. The transient scale-coherence indicator of `DQ-E3-12`, never exported,
   never persisted, never a document dependency.

### Authorities and author requirements this prompt implements

- Plan §2 (author-fixed): native; `Construction → Annotations and Media`; no
  toolbar button; inputs upper-left point, ISO A size, portrait or landscape,
  drawing scale; ISO physical size, drawing scale and the unit contract; an
  optional `ExportArea` configuration after a yes/no question; no second
  export-area mechanism.
- `AQ-G3` / `DQ-E1-8` (reference evidence): the outer dotted rectangle and all
  corner points are hidden in the final sheet presentation.
- `unit-system.md` §18.4: capture `u = 10^-3 / fb(effC)` and `a:b` once; no
  live read; tool unavailable with `UNSPECIFIED_MODEL_UNIT`; `usm` like `mm`.
- `AQ-X2`: `explicitly activated producer > Export_1/Export_2 > visible viewport`;
  `ExportArea` `SESSION`. `AQ-X7`: both ISO producers belong to `E3`.

### Characterization results at the base (summary; evidence in the report)

- ISO 216 table versus the legacy unrounded series (A3 legacy 297.30 × 420.45
  mm) — `K4`; legacy macros: upper-left `D`, y downward, dotted trim, inset
  frame, title block, no `Export_1`/`Export_2` — `K5`.
- Binary64 order matters (247/616 cases); `((L·b)/a)·u` best, ≤ 1 ulp — `K6`.
- Segments are not draggable through `P`; polygons and polylines are; polygon
  adds fill, region and area — `K8`.
- A model-space ISO rectangle already exports to the exact ISO page when the
  session scale equals the captured one; otherwise the page changes silently —
  `K12`.
- `ExportAreaSession` has two producers, no element field and no rebuild
  signal — `K10`; scale presets lack 1:20, 1:50, 1:100 — `K11`; no unit-gated
  action exists — `K14`; catalog 127 — `K14`; ADR 0031 pins 566/515/488 —
  `K15`; candidate names free — `K16`.

### Decisions requested before authorization

The options, recommendations and rationale are in design candidate §14; none is
decided here.

| Id | Topic |
|---|---|
| `DQ-E3-1` | representation (R1 segments, R2 polylines, R3 polygon, B dedicated type) |
| `DQ-E3-2` | sheet content: trim / frame / title block; margin rule (`AQ-E2` against `AQ-G3`) |
| `DQ-E3-3` | ISO size set |
| `DQ-E3-4` | input forms (`AQ-E3a`) |
| `DQ-E3-5` | canonical binary64 expression and bound |
| `DQ-E3-6` | later construction-unit change (`AQ-E3c`) |
| `DQ-E3-7` | unspecified-unit gate |
| `DQ-E3-8` | `ExportArea` participation |
| `DQ-E3-9` | `ISO_A_BORDER` link lifecycle (`AQ-E3b`) |
| `DQ-E3-10` | `ISO_A_SELECTION` design |
| `DQ-E3-11` | persistent construction label |
| `DQ-E3-12` | transient coherence indicator |
| `DQ-E3-13` | user workflow |
| `DQ-E3-14` | names, action ids, mode id, catalog delta |
| `DQ-E3-15` | relation to the legacy sheet tools |
| `DQ-E3-16` | verification class |

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_R6PLUS_POST_E2_P4   = 13e08ac1fe1c6c931a88985b97df93d8445812b9
tree                  = dd2ba17c483a18b8931e1848f38616edfa98b06a
preparation candidate = the local commit of phase/pre-g9b-r6-plus-e3-prompt
                        that contains this file (documentation only)
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base (or that the
authorized preparation candidate is its only delta), and that the worktree is
clean; otherwise it stops and reports.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` (v1.0 + amendment 1.1), ADR 0031, ADR 0032, ADR 0034, the `B`
and `C` author decisions and `verification-levels.md`; the author dispositions
of `DQ-E3-1`–`DQ-E3-16`; this prompt; then the reconciled design candidate,
the characterization report and its JSON mirror; then the `P0` candidate, the
legacy macros and older evidence. Probe outputs, reports and screenshots are
evidence, not authority. The legacy `sheetISOAnLand`/`sheetISOAnVert` are
behavioral references only; their formula, margins and title block are not
promoted into the native semantics.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Subject to the `DQ` dispositions; paths are the expected ones and the
implementation re-establishes each at its base.

**Shared kernel (`source/shared/common`, `source/shared/common-jre`).**

- One `Commands` constant; one `case` in `CommandDispatcher`; processor
  creation in `BasicCommandProcessorFactory`; one `Cmd*` processor (argument
  count and types, typed errors, no screen, unit or scale read).
- GeoCeDG-owned algorithm and pure helper (for example under
  `org.geocedg.common.kernel.sheet`): ISO 216 table, the canonical conversion
  of `DQ-E3-5`, outputs in the fixed order of design candidate §2.4,
  degeneracies of §2.5, the label string of `DQ-E3-11` (`—` escaped).
- `command.properties` / `command_en` / `command_es` name, alias and
  `.Syntax`; `menu.properties` / `menu_en` / `menu_es` tool keys;
  `EuclidianConstants` mode (proposed 144) with its mode text.
- Shared tests: command, `SelfTest` method, syntax localization, geometry,
  conversion bit identity, degeneracies, unit invariance, serialization.

**Desktop (`source/desktop/desktop`).**

- Mode dispatch and a tool orchestrator in `GeoCeDGEuclidianController`
  (precedent `GeoCeDGDimensionTools`); the unit gate through
  `GeoCeDGActionRegistry.unavailableReason` and `openDocumentUnits`; the
  dialog of design candidate §9 (local scale presets; `DrawingScale` parsing);
  one-shot capture; one undo point per creation; cleanup on cancel; the yes/no
  linkage question.
- `ExportAreaSession.Producer`, `ExportArea.Source`,
  `GeometryExportArea.Producer`, the DXF producer switch and manifest value for
  `ISO_A_BORDER` and `ISO_A_SELECTION`; the link and its identity-only
  staleness test (`DQ-E3-9`); the notice and re-link action; the File actions
  of `DQ-E3-14`.
- The coherence indicator in the status-bar scale segment and the export
  dialogs, with the explicit "use sheet scale" action if `DQ-E3-12` keeps it.
- Profile actions, group and cluster placement, localized texts; the
  `GeoCeDGProfile` count pin and every test pin of `K14`; the registry target
  whitelist.
- Desktop tests: tool, gate, dialog, capture, undo, producers, staleness,
  indicator, profile, GGBScript matrix rows, exporters.

**Documentation and verification.**

- The durable specification and ADR of *Required design/specification*; the
  status marker of `unit-system.md` §18.4 only if the author authorizes it.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The `PRE-G9B-R6` matrix rows, probes and fixture; its hash pins.
- The phase registration `PRE-G9B-R6-PLUS-E3` (selections, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, reconciled through the official mechanism.
- The ADR 0031 fingerprint fixture, regenerated by its documented route.
- The EN and ES user guides and their outline pins.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- A new `GeoElement` type, `GeoClass`, XML element type or attribute, or any
  reader change, unless the author froze option B of `DQ-E3-1` and
  `GLOBAL_IMPACT`.
- Any read of `constructionUnit`, `presentationUnit`, the `usm` factor,
  `drawingScale`, viewport, zoom or DPI by the command or algorithm; any unit
  or scale dependency in the DAG; parsing the label into geometry or a number.
- Any physical size derived from mouse distances or the screen scale.
- Identifying a border or its parts from labels, coordinates, proximity,
  construction indices or XML position; a comparator- or label-based staleness
  test.
- A second export-area mechanism; a change of the `AQ-X2` precedence; making a
  border an active producer without the user's explicit yes or File action;
  serializing `ExportArea`, the link or the indicator.
- Changing the session `drawingScale` without an explicit user action; changing
  `DrawingScale.PRESETS` or the `C` export dialogs beyond the indicator.
- Modifying `Templatev7.ggb`, the legacy macros, their manifests or the curated
  library; shipping or curating the legacy sheet tools; migrating legacy
  documents.
- Title blocks, paper space, viewports, sheet sets, page setup or any other
  `G15` capability; a toolbar button.
- `F1`, `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Shared Java kernel: command identity, algorithm, ISO table, conversion,
degeneracies, label string, serialization through existing outputs
(`AGENTS.md` §4: DAG dependency on a point, dynamic evaluation, persistence,
several frontends). Desktop application layer: mode, dialog, capture, unit
gate, linkage, File actions, `ExportArea` producers, staleness, indicator.
Export adapters: unchanged rendering of polylines and text and the DXF
producer value; no sheet semantics in an exporter. Python: none.

## Required design/specification

First deliverables of the phase, before product code:

1. `geocedg/specs/sheets/iso-a-border.md` (proposed path) — the durable
   contract (signature, domain, coordinate convention, outputs, conversion and
   error bound, degeneracies, label, capture, unit gate, producers and link
   lifecycle, indicator, persistence, compatibility, export behavior), from the
   reconciled design candidate and the `DQ` dispositions.
2. ADR 0036 (proposed number) — `IsoABorder` representation and `ExportArea`
   integration (`DQ-E3-1`, `DQ-E3-2`, `DQ-E3-9`), status `PROPOSED` until the
   author approves it.

## Geometric invariants and degeneracies

- Unit and scale independence: for every operation of `unit-system.md` §17.1
  and every session-scale change, every coordinate of every output and the
  construction XML apart from `geocedgUnits` stay byte-identical.
- Zoom, viewport and DPI independence of every output.
- Model-frame convention of design candidate §2.3: `P` upper-left; axis-aligned;
  orientation swaps width and height only.
- Canonical conversion bit-identical to `DQ-E3-5`; error bound as declared.
- Degeneracies exactly as design candidate §2.5: explicit undefined outputs,
  identities kept, no stale geometry, recovery on valid input.
- Presentation (visibility, style, layer, selection) never modifies geometry
  or `ExportArea` authority.
- Determinism: identical outputs and XML for a fixed construction revision.

## Compatibility and serialization

- Additive `<command name="IsoABorder">` with existing element types only; the
  captured values as ordinary inputs; the label string recomputed.
- Documents without the new command re-save byte-identically.
- Save and reopen, undo and redo, copy and paste, redefinition of an input:
  identity of geometry and style.
- Older GeoCeDG, Classic and the pinned upstream baseline: unknown command,
  load error, outputs and dependents missing, lost on re-save (report `K9`);
  stated in the guides.
- `ExportArea`, the link and the indicator are `SESSION` or derived: reset on
  New and Open, untouched by undo, never serialized.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Proposed verification plan (frozen at authorization with `DQ-E3-16`). The
cases of each obligation are listed in design candidate §12.

| ID | Obligation |
|---|---|
| `T-E3-GEOMETRY` | `T-ISO-TABLE`, `T-REFERENCE-CASE`, `T-REFERENCE-POINT`, `T-ORIENTATION` |
| `T-E3-CONVERSION` | `T-UNITS-SCALES` (bit identity and ulp bound for `mm`, `cm`, `m`, `usm`) |
| `T-E3-DYNAMICS` | `T-MOVE`, `T-UNDO-REDO`, `T-DETERMINISM` |
| `T-E3-UNITS` | `T-UNIT-CHANGE`, `T-NO-UNIT` |
| `T-E3-SCALE` | `T-SESSION-SCALE`, `T-EXPORT-OTHER-SCALE` |
| `T-E3-DEGENERACY` | `T-INVALID` |
| `T-E3-SERIALIZATION` | `T-SAVE-REOPEN`; byte identity of unrelated documents |
| `T-E3-EXPORT` | `T-EXPORT-CONSISTENCY` on every consumer of `ExportArea` |
| `T-E3-PRODUCERS` | `T-TRACEABILITY`, `T-ISO-SELECTION`; precedence unchanged; staleness by identity only |
| `T-E3-PRESENTATION` | `T-PRESENTATION`, `T-LABEL`; the indicator is never exported or persisted |
| `T-E3-COMMANDS` | ADR 0031 gate 567/516/489 (if displayed) and regenerated fingerprints; EN/ES alias; Input Help; `SelfTest`; syntax localization |
| `T-E3-GGBSCRIPT` | matrix rows and probes; the completeness gate passes and its negative control still fails closed |
| `T-E3-PROFILE` | catalog pin per `DQ-E3-14` and every `K14` pin; menu-only placement |
| `T-E3-LEGACY` | `T-LEGACY` |
| `T-E3-GUIDE` | EN/ES guide parity and outline pins |
| `T-SMOKE` | author smoke in EN and ES (not performed by the agent) |

Commands (PowerShell; log directories fresh, under `artifacts\agent\`):

```powershell
.\gradlew.bat :shared:common-jre:test --tests <focused E3 shared classes>
.\gradlew.bat :desktop:desktop:test --tests <focused E3 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E3 -LogDirectory artifacts\agent\<fresh>
.\tools\agent\verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-E3` requires at least
`compile.shared.semantic`, `compile.desktop.semantic`,
`workspace-profile.product`, `static.ggbscript-matrix-inputs.semantic`,
`ggbscript-matrix.product`, `junit.shared.pre-g9b-r6-plus-e3.semantic` and
`junit.desktop.pre-g9b-r6-plus-e3.semantic` (precedent `PRE-G9B-R6-PLUS-E2`).
`PHASE` and `INTEGRATION` run once each, on the same frozen clean commit and
tree; `INTEGRATION` covers `final.desktop` and the shared suites. No `FINAL`
unless the class is `GLOBAL_IMPACT`. A run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`E3` on an exact base with the `DQ-E3` dispositions; it then authorizes local
implementation on a local branch, local commits, focused tests, the phase
registration and its official catalog updates, development `INFRA_UNIT` and
`STATIC` runs, the frozen acceptance runs, and one frozen technical candidate
for author review and author smoke. It never authorizes self-approval, author
smoke by the agent, a run above the frozen class, or a change of the class; a
proven inability of the frozen class to cover the change is reported as a
reclassification proposal, never applied silently.

`F1`, `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so
does every open observation and enhancement not named here. Author approval is
never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA. Acceptance evidence
never grants publication authority.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-E3      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification and ADR; the kernel, Desktop, profile, matrix, registry and
  guide deltas with their tests.
- A candidate report with the entry gate, `K0`–`K21` re-established with every
  correction, the `DQ` dispositions applied, test evidence, the exact commands,
  exit codes, report paths and verdicts, and an author-smoke checklist: create
  an A3 1:50 sheet in `mm` in EN and ES; check that the trim and every corner
  are hidden and the frame and label are shown; drag the frame; answer yes to
  the linkage question and export PDF at 1:50 (420 × 297 mm) and at 1:100
  (indicator shown); change the construction unit and see only the indicator
  change; undo and redo; save and reopen; try the tool with an unspecified unit;
  use `ISO_A_SELECTION`; export SVG, EMF, PNG, the three LaTeX dialects and DXF.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- the chosen representation cannot express a required output with existing
  types, or correctness needs a new `GeoElement` type, XML element or
  attribute, or a reader change (`GLOBAL_IMPACT`, author decision);
- the canonical conversion cannot be made bit-reproducible across the command,
  `ISO_A_SELECTION` and the tests;
- the command would need to read the unit state, `drawingScale` or the view;
- a live `ISO_A_BORDER` link cannot be validated by identity, without label,
  index, coordinate or XML-position inference;
- the `AQ-X2` precedence, the `SESSION` lifetime of `ExportArea` or the `C`
  export dialogs would have to change beyond the indicator;
- the ADR 0031 gate or the GGBScript completeness gate cannot pass without
  weakening it;
- an export of the border would need a new DXF entity mapping or a change of
  Classic export semantics;
- a legacy macro, `Templatev7.ggb` or a user document would have to be
  modified, renamed or reinterpreted;
- licensing or provenance of any asset is unclear.

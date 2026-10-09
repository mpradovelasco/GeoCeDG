# PRE-G9B-R6-plus-E3 — native `IsoABorder` and `ExportArea` integration

**CANONICAL PROMPT — AUTHORIZED FOR IMPLEMENTATION (author authorization of 2026-10-10) — IMPLEMENTATION PUBLICATION NOT AUTHORIZED.**

This prompt was prepared by `PRE-G9B-R6-plus-E3-PREP` (author instruction of
2026-10-09), reconciled by `PRE-G9B-R6-plus-E3-PREP-CLOSEOUT` with the author
decisions of 2026-10-10 on `DQ-E3-1` to `DQ-E3-16`, and amended to the
authorized state by `PRE-G9B-R6-plus-E3-IMPLEMENTATION` with the author
instruction of 2026-10-10 that resolves `OTQ-E3-1`, corrects the
numerical-accuracy contract and **authorizes the implementation**
([E3 author-decision record](../../../docs/validation/pre_g9b_r6_plus_e3_author_decisions_record.md)
§5). The governing design is the
[reconciled design](../../../docs/architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md)
(`DESIGN — AUTHOR APPROVED — RECONCILED`). The authorization covers the
complete `E3` implementation specified here, on the exact base below, up to one
frozen local technical candidate for author review and author smoke. It does
not authorize publication, author approval of the implementation, or any later
subphase.

This file is an execution contract, not a second policy document: the unit and
scale rules are stated once in the
[unit-system specification](../../../geocedg/specs/units/unit-system.md)
(§3.3, §5.3, §6, §7.2, §7.3, §12, §13, §17, §18.4); the `ExportArea`
authority once in the
[B author decisions](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md)
(`AQ-X2`, `AQ-X7`); the command-head rules once in
[ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md);
the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the `E3` semantics
once in the reconciled design, whose section numbers are cited below; the probe
evidence once in the
[characterization report](../../../docs/validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md).
The first deliverable of `E3` turns the approved design into the durable
specification named under *Required design/specification*.

```text
PRE-G9B-R6-plus-E3 =
DESIGN — AUTHOR APPROVED — RECONCILED (2026-10-10)
CANONICAL PROMPT — AUTHORIZED FOR IMPLEMENTATION

AUTHOR AUTHORIZATION     = 2026-10-10 (PRE-G9B-R6-plus-E3-IMPLEMENTATION)
IMPLEMENTATION BASE      = ff56099184544e5988c63e0ea3339e3d0fe01fac
                           tree 1cdd1e986df785f5f16e7887300d9034e5e4a677
CHARACTERIZATION         = COMPLETE (2026-10-09, on P_R6PLUS_POST_E2_P4 13e08ac1)
AUTHOR DECISIONS         = DQ-E3-1 to DQ-E3-16 (2026-10-10); OTQ-E3-1 RESOLVED and
                           numerical contract corrected (2026-10-10)
IMPLEMENTATION           = AUTHORIZED
IMPLEMENTATION PUBLICATION = NOT AUTHORIZED
selfApproved             = false
authorApproved (design)  = true    (the reconciled design only)
authorApproved (implementation) = false   (pending author review and smoke)
implementationAuthorized = true
passClaimed              = false
PHASE_KIND               = SHARED-KERNEL COMMAND AND ALGORITHM + DESKTOP MODE,
                           DIALOG, PROFILE AND HELP + TWO SESSION PRODUCERS OF
                           THE SINGLE ExportArea + DERIVED COHERENCE INDICATOR +
                           ADDITIVE COMMAND SERIALIZATION WITH EXISTING OUTPUT
                           TYPES (R2, DQ-E3-1)
DEPENDS_ON               = PRE-G9B-R6-plus-D1, B, C, E1, E2 = PASS — AUTHOR APPROVED — PUBLISHED
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
VERIFICATION CLASS       = INTEGRATED_PHASE — FROZEN (PHASE + INTEGRATION)
NEXT_SUBPHASE            = none implied; F1, F2, F3, F4, G stay unauthorized
```

<!-- geocedg-field: objective -->
## Objective

Implement the approved `IsoABorder` design (authorized 2026-10-10):

1. **Kernel command** `IsoABorder` (Spanish alias `MarcoISOA`) with the single
   seven-argument signature of design §2 —
   `IsoABorder(<Point>, <ISO A index>, <Landscape>, <Scale numerator>,
   <Scale denominator>, <Model units per millimetre>, <Inner frame>)` — and
   three outputs of existing types (design §3): `PAPER` (closed
   `GeoPolyLine`, nominal ISO 216 paper boundary, hidden), `FRAME` (closed
   `GeoPolyLine`, optional inner frame with margins left 20 / right 10 /
   top 10 / bottom 10 mm), `LABEL` (`GeoText` `A<n> — <a>:<b>`).
2. **Shared helper** holding the ISO 216 table, the margins, frame validity and
   the canonical conversion `conv(L) = ((L·b)/a)·u` (design §5–§7), used by the
   command, `ISO_A_SELECTION` and the coherence computation.
3. **Desktop tool** `Construction → Annotations and media → ISO A Border`
   (menu only), with the unit gate, one-shot capture of `u` and `a:b`, the
   dialog and the activation question (design §12).
4. **`ExportArea` producers** `ISO_A_BORDER` (live link to `PAPER`) and
   `ISO_A_SELECTION`, with `AQ-X2` precedence unchanged and the identity-only
   link validity (design §11).
5. **Coherence indicator** — physical-size and scale-label coherence as two
   independent derived states, the explicit "Use sheet scale" action and the
   activation warning (design §9).

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_E3_DESIGN = ff56099184544e5988c63e0ea3339e3d0fe01fac
tree               = 1cdd1e986df785f5f16e7887300d9034e5e4a677
                     (child of e063a8a27450cded0655afb6abc1e78f89f9ac6f, itself a child of
                     13e08ac1fe1c6c931a88985b97df93d8445812b9)
first tracked edit = the documentary reconciliation and authorization commit of
                     PRE-G9B-R6-plus-E3-IMPLEMENTATION (Stage A), the immediate
                     predecessor of the implementation
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base and that the
worktree is clean; otherwise it stops and reports. It then re-establishes at
that base every characterization fact it relies on (report `K0`–`K21`).

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` (v1.0 + amendment 1.1), ADR 0031, ADR 0032, ADR 0034, the `B`
and `C` author decisions and `verification-levels.md`; the `E3` author
decisions of 2026-10-10; the approved reconciled design; this prompt; then the
characterization report and its JSON mirror; then the `P0` candidate, the
legacy macros and older evidence. Rules tagged **[ID]** in the design are
implementation design inside the approved scope; a proven need to deviate from
one is reported, not applied silently. Probe outputs, reports and screenshots
are evidence, not authority. `sheetISOAnLand`/`sheetISOAnVert` are behavioral
references only.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones; the implementation re-establishes each at its
base.

**Shared kernel (`source/shared/common`, `source/shared/common-jre`).**

- One `Commands` constant; one `case` in `CommandDispatcher`; processor
  creation in `BasicCommandProcessorFactory`; one `Cmd*` processor (exactly
  seven arguments, typed errors, no screen, unit or scale read).
- GeoCeDG-owned algorithm and pure helper (for example under
  `org.geocedg.common.kernel.sheet`): ISO 216 integer table, margin constants,
  exact-integer frame validity, the canonical conversion, fixed-arity outputs
  of design §3, geometry of §4–§5, degenerations of §15, label string of §10
  (`—` escaped).
- `command.properties` / `command_en` / `command_es` name, alias `MarcoISOA`
  and `.Syntax`; `menu.properties` / `menu_en` / `menu_es` tool keys;
  `EuclidianConstants.MODE_ISO_A_BORDER = 144` with its mode text.
- The Classic surface of `OTQ-E3-1` (resolved): the shared command is available in Classic through the ordinary command infrastructure, with no feature gate, flag or parallel implementation, and no Classic GUI.
- Shared tests of the matrix below.

**Desktop (`source/desktop/desktop`).**

- Mode dispatch and a tool orchestrator in `GeoCeDGEuclidianController`
  (precedent `GeoCeDGDimensionTools`); the unit gate through
  `GeoCeDGActionRegistry.unavailableReason` and `openDocumentUnits`; the
  dialog of design §12 with its local presets and frame-option rules; one-shot
  capture; one undo point; cleanup on cancel; the activation question.
- `ExportAreaSession.Producer`, `ExportArea.Source`,
  `GeometryExportArea.Producer`, the DXF producer switch and manifest value;
  the link with its identity-only validity (§11.3); notices; the File actions
  `export.area.iso-a` and `export.area.use-iso-a-border`.
- The coherence states, the status-bar and export-dialog presentation, "Use
  sheet scale" and the activation warning (§9).
- Profile actions (127 → 130), group and cluster placement, localized texts,
  the `GeoCeDGProfile` count pin and every test pin of report `K14`, the
  registry target whitelist.
- Desktop tests of the matrix below.

**Documentation and verification.**

- The durable specification and ADR of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The `PRE-G9B-R6` GGBScript matrix rows, probes and fixture, and their hash
  pins.
- The phase registration `PRE-G9B-R6-PLUS-E3` (selections, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins, reconciled through the official mechanism.
- The ADR 0031 fingerprint fixture, regenerated by its documented route.
- The EN and ES user guides and their outline pins.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- A new `GeoElement` type, `GeoClass`, structural XML element or attribute, a
  reader change, or a new durable identity mechanism.
- Any read of `constructionUnit`, `presentationUnit`, the `usm` factor,
  `drawingScale`, viewport, zoom or DPI by the command or algorithm; any unit
  or scale dependency in the DAG; parsing the label into geometry or a number.
- Any physical size from mouse distances or the screen scale; any conversion of
  the frame other than `conv()` of `W_f`, `H_f`, `m_L`, `m_T`; silently reduced
  margins; a zero or negative frame; rounding of paper sizes.
- Identifying a border or its parts from labels, coordinates, proximity,
  construction indices or XML position; a comparator- or label-based link
  validity test; trusting a stored Java reference without the identity check.
- A second export-area mechanism; any change of the `AQ-X2` precedence;
  activating a border without the user's explicit consent; using `FRAME` as
  the export area; serializing `ExportArea`, the link or the indicator.
- Changing the session `drawingScale` other than through the explicit "Use
  sheet scale" action; changing `DrawingScale.PRESETS` or the `C` dialogs
  beyond the indicator; automatic geometry scaling.
- Modifying `Templatev7.ggb`, the legacy macros, their manifests,
  `tools/legacy/ingest.ps1`, `models/legacy/`, derived inventories or the
  curated library; migrating legacy sheets.
- Title blocks, drawing-sheet sets, paper space, viewports, page setup, physical
  text size or any other `G15` capability; a toolbar button.
- `F1`, `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Shared Java kernel: command identity, algorithm, ISO table, margins,
conversion, frame validity, degenerations, label string, serialization through
existing outputs (`AGENTS.md` §4). Desktop application layer: mode, dialog,
capture, unit gate, activation, File actions, `ExportArea` producers and link
validity, coherence indicator. Export adapters: unchanged rendering of
polylines and text and the DXF producer value; no sheet semantics in an
exporter. Python: none.

## Required design/specification

First deliverables of the phase, before product code:

1. `geocedg/specs/sheets/iso-a-border.md` (proposed path) — the durable
   contract transcribed from the approved design §2–§15 without semantic
   change.
2. ADR 0036 (proposed number) — `IsoABorder` representation (R2), inner frame,
   and `ExportArea` producers and link validity; status `PROPOSED` until the
   author approves it.

## Geometric invariants and degeneracies

- Nominal ISO 216 integer sizes; A3 landscape = 420 × 297 mm (design §6).
- `P` upper-left in model coordinates; axis-aligned; orientation swaps width
  and height only (§4).
- `conv(L) = ((L·b)/a)·u` bit-identical everywhere (obligation A); relative
  bounds only under the stated normality conditions (B: `< 2^-51.99` against
  the captured `u` when `fl(q·u)` is normal; C: `< 2^-50` against the physical
  definition when `u` and `fl(q·u)` are normal); not representable results are
  undefined, subnormal ones `UNGUARANTEED`; equivalent scale pairs
  bit-identical (§7).
- Frame from `W_f = W_p − 30`, `H_f = H_p − 20` (mm) through `conv()`; valid
  iff `W_f > 0` and `H_f > 0`; A10 portrait impossible (§5–§6).
- Unit and scale independence: for every operation of `unit-system.md` §17.1
  and every session-scale change, coordinates and the construction XML apart
  from `geocedgUnits` are byte-identical (§8).
- Degenerations exactly as §15; identities kept; no stale geometry; recovery
  on valid inputs.
- Presentation (visibility, style, layer, selection) never changes geometry
  or `ExportArea` authority; the hidden `PAPER` remains the export rectangle.
- Determinism: identical outputs and XML for a fixed construction revision.

## Compatibility and serialization

- Additive `<command name="IsoABorder">` with element types `polyline` and
  `text` only; captured values as literal inputs (bit-exact round trip); label
  string recomputed (§3, §13).
- Documents without the command re-save byte-identically.
- Save and reopen, undo and redo, copy and paste, redefinition of an input:
  geometry and style identical.
- Older GeoCeDG, Classic and the pinned upstream baseline: unknown command,
  load error, outputs and dependents missing, lost on re-save; stated in the
  guides (§14).
- `ExportArea`, the link and the coherence states are `SESSION` or derived:
  reset on New and Open, untouched by undo, never serialized.
- Legacy documents and macros unchanged; no migration (§14).
- Classic (`OTQ-E3-1`, design §12): the command parses, evaluates, persists and
  reopens identically in GeoCeDG and in the Classic profile of this fork; Classic
  gains no menu, tool, dialog, producer, indicator or notice; no other command
  changes; ADR 0031 head and alias rules hold in both profiles.

<!-- geocedg-field: required_checks -->
## Required tests and commands

The cases of every obligation are those of design §16; all are required.

| ID | Obligation (design §16 rows) |
|---|---|
| `T-E3-GEOMETRY` | `T-ISO-TABLE` (all sizes, both orientations), `T-REFERENCE-CASE`, `T-REFERENCE-POINT`, `T-ORIENTATION` |
| `T-E3-FRAME` | `T-FRAME` (optional and invalid frames), `T-MARGINS` (20/10/10/10 mm) |
| `T-E3-CONVERSION` | `T-CONVERSION` (captured units and scales, `usm`, equivalent pairs) |
| `T-E3-NUMERIC` | `T-E3-NUMERIC-NORMAL`, `-SUBNORMAL`, `-EXTREME-USM`, `-OVERFLOW`, `-UNDERFLOW`, `-COORDINATE-ABSORPTION`, `-CAPTURE-ERROR`, `-REPRODUCIBILITY`, `-COHERENCE-UNDETERMINABLE` (design §7, §16): obligations A, B and C against `BigDecimal` references independent of the binary64 expression; validity versus reliability |
| `T-E3-CLASSIC` | `T-CLASSIC`: command parsing in GeoCeDG and Classic, canonical English identity, alias `MarcoISOA`, GGBScript, persistence and reopening, no Classic GUI change, no other command changed |
| `T-E3-SIGNATURE` | `T-SIGNATURE` |
| `T-E3-DYNAMICS` | `T-MOVE-DAG` (dragging the reference point, DAG propagation), `T-UNDO-REDO`, `T-DETERMINISM` |
| `T-E3-UNITS` | `T-UNIT-CHANGE`, `T-NO-UNIT`, `T-USM` (invalid and unspecified units) |
| `T-E3-COHERENCE` | `T-SESSION-SCALE`, `T-COHERENCE` (physical-size versus scale-label coherence), `T-SCALE-ACTION`, `T-ACTIVATION-WARNING` |
| `T-E3-DEGENERACY` | `T-INVALID` (numerical degenerations) |
| `T-E3-SERIALIZATION` | `T-SAVE-REOPEN` (persistence and reconstruction); byte identity of unrelated documents |
| `T-E3-EXPORT` | `T-EXPORT-CONSISTENCY` (physical output in every supported exporter) |
| `T-E3-PRODUCERS` | `T-PRODUCERS` (precedence), `T-LINK-LIFECYCLE` (identity and stale linkage), `T-ISO-SELECTION` |
| `T-E3-PRESENTATION` | `T-PRESENTATION`, `T-LABEL`, `T-TOOL` |
| `T-E3-COMMANDS` | `T-GATES`: ADR 0031 pins and regenerated fingerprints; EN/ES alias; Input Help; `SelfTest`; syntax localization; Classic surface per `OTQ-E3-1` |
| `T-E3-GGBSCRIPT` | matrix rows and probes; the completeness gate passes and its negative control still fails closed |
| `T-E3-PROFILE` | catalog 127 → 130 and every `K14` pin; menu-only placement |
| `T-E3-LEGACY` | `T-LEGACY` (legacy macro coexistence) |
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
Class `INTEGRATED_PHASE` (`DQ-E3-16`): `PHASE` and `INTEGRATION` run once
each, on the same frozen clean commit and tree; `INTEGRATION` covers
`final.desktop` and the shared suites. No `FINAL`. A run above the frozen
class fails with `VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author instruction of 2026-10-10 authorizes `E3` on the exact base above:
local implementation on a local branch, local commits, focused tests, the
phase registration and its official catalog updates, development
`INFRA_UNIT` and `STATIC` runs, the frozen `PHASE` and `INTEGRATION` runs, and
one frozen technical candidate for author review and author smoke. A
necessary correction after acceptance is a new candidate with its own
acceptance runs; an accepted candidate is never amended.
It never authorizes self-approval, author smoke by the agent, a run above the
frozen class, or a change of the class; a proven inability of the frozen
class to cover the change is reported as a reclassification proposal, never
applied silently.

`F1`, `F2`, `F3`, `F4`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so
does every open observation and enhancement not named here. Author approval of
the design is not approval of any implementation; technical verification never
creates author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Implementation work stays on local branches and local commits. Push, branch
publication, merge, promotion to `main`, rebase, squash, amend after freeze,
force push, tag, release, installer or binary publication are forbidden; each
needs a separate explicit author instruction naming the exact candidate SHA.
Acceptance evidence never grants publication authority. (The publication of
this documentary prompt was authorized separately and grants nothing for the
implementation.)

## Acceptance and closeout

```text
PRE-G9B-R6-plus-E3      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (INTEGRATED_PHASE: PHASE + INTEGRATION)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false (candidate), passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification and ADR; the kernel, Desktop, profile, matrix, registry
  and guide deltas with their tests.
- A candidate report with the entry gate, the re-established characterization
  facts with every correction, the design rules implemented and any reported
  deviation, test evidence, exact commands, exit codes, report paths and
  verdicts, and an author-smoke checklist: create an A3 1:50 sheet in `mm` in
  EN and ES and check that the paper boundary and every corner are hidden and
  the frame (20/10/10/10 mm) and the label `A3 — 1:50` are shown; create an A5
  sheet and check the frame is off by default and can be enabled; check that
  A10 portrait refuses the frame with a reason; drag the frame; answer yes to
  the activation question and export PDF at 1:50 (420 × 297 mm); change the
  session scale to 1:100 and see both coherence states and "Use sheet scale";
  change the construction unit and see the physical state change while the
  geometry does not; undo and redo, and see the link replaced by the fallback
  with a notice; save and reopen; try the tool with an unspecified unit and
  with a `usm`; use `ISO_A_SELECTION`; export SVG, EMF, PNG, the three LaTeX
  dialects and DXF.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- the numerical contract of design §7 cannot be implemented reproducibly, or a
  representability failure could only be hidden by clamping, rounding or an
  approximate sheet;
- the shared command in Classic would need a material, unplanned compatibility
  redesign, or GeoCeDG-only GUI would leak into Classic;
- R2 cannot express a required output with existing types, or correctness
  needs a new `GeoElement` type, structural XML element or attribute, a reader
  change or a new identity mechanism (reclassification, author decision);
- the canonical conversion cannot be made bit-reproducible across the command,
  `ISO_A_SELECTION`, the coherence computation and the tests;
- the command would need to read the unit state, `drawingScale` or the view;
- a live `ISO_A_BORDER` link cannot be validated by identity alone, without
  label, index, coordinate or XML-position inference, or needs a substantially
  wider session architecture;
- the `AQ-X2` precedence, the `SESSION` lifetime of `ExportArea` or the `C`
  export dialogs would have to change beyond the indicator, or persistent
  `ExportArea` state would be needed;
- unplanned global rendering or geometry changes, or a material widening of
  compatibility requirements, would be needed;
- the ADR 0031 gate or the GGBScript completeness gate cannot pass without
  weakening it;
- an export of the sheet would need a new DXF entity mapping or a change of
  Classic export semantics;
- a legacy macro, `Templatev7.ggb`, a derived inventory or a user document would
  have to be modified, renamed or reinterpreted;
- licensing or provenance of any asset is unclear.

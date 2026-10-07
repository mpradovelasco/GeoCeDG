# PRE-G9B-R6-plus-E2 — native dimensions (`AlignedDimension`, `LinearDimension`)

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-07 (E2
characterization and preparation, documentary candidate `T_R6PLUS_E2_PREP`
`6ad853c618da8f07a279b266c83002c145c6292c`) and revised at the author's second
instruction of the same day, which froze the decisions `DQ-E2-1` to `DQ-E2-11`
and authorized one focal characterization of the dimension-value orientation
(`DQ-E2-6`). Neither instruction authorizes product implementation. The
decisions are recorded, as given, in the
[E2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_e2_author_decisions_record.md),
which prevails over this file. The evidence is in the
[E2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md)
(`K1`–`K20`) and the
[`DQ-E2-6` focal addendum](../../../docs/validation/pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md)
(`A1`–`A10`); the design is in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-E2` and the exact prepared candidate or base, that accepts the
`DQ-E2-6` focal result and the preparation resolutions of the author-decision
record (or changes them), and that authorizes implementation. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `A-1`, `B`, `D0`, `D1`, `A-2`,
`C`, `C-X1`, `E1-L` and `E1-X1` precedent. This file is an execution contract,
not a second policy document: the unit rules are stated once in the
[unit-system specification](../../../geocedg/specs/units/unit-system.md)
(§5, §6, §14, §17, §18.3); the command-head rules once in
[ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md);
the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the probe evidence once
in the report and the addendum. This prompt cites them and does not restate
them differently.

```text
PRE-G9B-R6-plus-E2 =
PREPARED — AUTHOR DECISIONS FROZEN — IMPLEMENTATION NOT AUTHORIZED

CHARACTERIZATION         = COMPLETE (P_R6PLUS_E1_P_X1, 2026-10-07)
DQ-E2-6 FOCAL RESULT     = ALIGNED NORMATIVE-STYLE TEXT — FEASIBLE WITH β
                           (bounded drawable seam; pending the author's review)
DQ-E2-1                  = β — AUTHOR APPROVED FOR DESIGN
DQ-E2-3                  = AUTHOR APPROVED
VERIFICATION_CLASS       = INTEGRATED_PHASE — AUTHOR APPROVED FOR DESIGN
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false   (no technical candidate exists)
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = SHARED-KERNEL COMMANDS AND ALGORITHMS + ONE GATED
                           PRESENTATION DRAWABLE + DESKTOP TOOLS, DRAG, PROFILE,
                           ICONS AND HELP + BOUNDED GeoCeDG LaTeX EXPORT ADAPTATION +
                           ADDITIVE COMMAND SERIALIZATION WITH EXISTING OUTPUT TYPES
DEPENDS_ON               = PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (hard: unit state and listener)
                           PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (hard for the Desktop tool icons only)
                           PRE-G9B-R6-plus-E1-P-X1 = PASS — AUTHOR APPROVED —
                                                PUBLISHED (clean final.desktop)
                           NOT F1 (E2 executes before F1 and must not depend on it)
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PRECONDITION             = a separate author instruction authorizing implementation
NEXT_SUBPHASE            = none implied; E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-defined native dimension scope (mini-track plan §2, §3.2,
§8.5; `unit-system.md` §14, §18.3) under the frozen decisions:

1. Two native commands, `AlignedDimension` and `LinearDimension` (ES
   `CotaAlineada`, `CotaLineal`), implemented by shared-kernel algorithms whose
   outputs are existing object types (β): the model-unit value, a dimension
   segment with arrow endings, two extension segments and the presentation text,
   in the frozen order of the design candidate §3.4.
2. Value = model-unit measure (`L = |B − A|`, or `L = |⟨B − A, d̂⟩|`), never
   dependent on `constructionUnit`, `presentationUnit`, `drawingScale`,
   viewport, zoom or DPI; presentation string `format(display(L)) + " " +
   suffix(effP)`, or `format(L)` for `UNSPECIFIED_MODEL_UNIT`, refreshed on unit
   changes by a presentation step outside geometric `compute()`.
3. A mandatory signed model-unit `Offset`; `Overshoot` and `Gap` optional, 0 when
   omitted; no hidden unit, viewport, DPI or length default.
4. The side pinned by explicit inputs (B1), the `E2`-local direction authority
   (`getDirection` / vector coordinates), no `F1` dependency.
5. A normative-style aligned value: parallel, centred, never upside down, not
   crossed by the dimension or extension lines, following the drawn figure,
   reproduced from geometry at every draw — through the bounded drawable seam
   characterized in the addendum (`A8`).
6. Desktop tools in `construction-metrics` and the Construction menu (ids
   `measure.aligned-dimension`, `measure.linear-dimension`, modes 142 and 143),
   GeoCeDG-owned SVG icons, bilingual text, one-shot creation-time capture and a
   drag that edits only the offset.
7. Picture export with the complete figure; a bounded GeoCeDG LaTeX adaptation
   for dimension outputs; the DXF segment subset recorded as a durable
   limitation.
8. The ADR 0031 canonical-head and complete-inventory gates, the `PRE-G9B-R6`
   GGBScript matrix rows, and validated coexistence with the legacy
   `directDimension` / `axisDimension` macros, which are never renamed, deleted,
   migrated or reinterpreted.

### Frozen author decisions (2026-10-07)

| ID | Decision (authority: the author-decision record) |
|---|---|
| `DQ-E2-1` | β — composition of existing output types — approved for design; a new `GeoElement` or XML element type escalates |
| `DQ-E2-2` | `AlignedDimension(A, B, Offset [, Overshoot, Gap])`, `LinearDimension(A, B, Direction, Offset [, Overshoot, Gap])`; `Offset` mandatory; `Overshoot = Gap = 0` when omitted; output order frozen in preparation (value, dimension line, extension `A`, extension `B`, text) |
| `DQ-E2-3` | approved: numeric = model-unit measure; displayed = `format(display(L)) + suffix(effP)`; bare when unspecified; no `UnitState` read in geometric `compute()`; presentation-only refresh; no parsing back |
| `DQ-E2-4` | B1: side pinned explicitly; preparation resolution: input order plus signed `Offset`, no extra input; the tool fixes the sign once from the placement click |
| `DQ-E2-5` | `E2`-local direction authority: `getDirection` and vector coordinates; never `getDirectionInD3`; no `F1` |
| `DQ-E2-6` | normative-style aligned value (requirement); focal result: feasible with β through the addendum `A8` seam (`C4`) |
| `DQ-E2-7` | `AlignedDimension`, `LinearDimension`; ES `CotaAlineada`, `CotaLineal`; the four legacy spellings are not registered; user macros untouched |
| `DQ-E2-8` | `INTEGRATED_PHASE` (registered `PHASE` + `INTEGRATION`); escalation as stated below |
| `DQ-E2-9` | picture complete; PSTricks, PGF, Asymptote preserve arrows, positioning and orientation by bounded GeoCeDG adaptation; DXF: no `DIMENSION` entity, segment subset recorded as durable limitation |
| `DQ-E2-10` | surface frozen (names, aliases, ids, modes 142/143, `construction-metrics`, Construction menu), current-base pins only |
| `DQ-E2-11` | offset from the placement click, materialized at once; command defaults 0; tool capture once (paper magnitudes with a physical unit and `drawingScale`; view fallback materialized; no millimetre pretence when unspecified); no live dependency after creation |

Preparation resolutions still open to the author's review: the output order,
the B1 representation, the failure marker `?` plus suffix, the creation-time
magnitudes (paper 2 mm / 1 mm; view 8 px / 4 px), and the `A8` seam.

### Characterization results at the base (summary)

- `Dimension` is upstream cardinality, unrelated, unchanged (`K2`).
- The frozen heads and aliases are free; registration follows `LocusV2` /
  `SplineV2`; ADR 0031 pins 564/513/486 → 566/515/488 with a regenerated R5-B
  fingerprint fixture (`K3`).
- GGBScript matrix: 6 rows per syntax line → +36 rows (2 + 4 forms) and +28
  probes with distinct Spanish aliases; the completeness gate fails closed until
  both commands are covered (`K4`).
- Profile: 125 actions at the base, `construction-metrics` at
  `application-profile.yml:3475-3480`, every pin listed in `K5`.
- Directions: `getDirectionInD3` is opposite for parallel, perpendicular and
  point–vector lines and rays (`K7`).
- Unit seam: the `D1` listener exists; no length formatter yet (`K8`).
- Aligned value: rotated LaTeX alone fails clearance and anisotropy; the drawable
  seam satisfies every measured property (`A5`).
- Exporters: picture complete; LaTeX loses arrows and centring at the base; DXF
  segments only (`K12`, `A6`).
- Drag: `ChangeableParent` is 3D-only; the inherited dependent move would move
  `A` and `B` (`K13`).
- Legacy macros resolve before natives, case-insensitively; with the frozen
  names they no longer collide; the whole-library failure mode and the locale
  dependence of the library check are pre-existing (`K15`).
- Older readers drop the command's outputs and dependents (`K16`).

### Prepared bilingual tool text (wording subject to the author)

`upstream-mode` actions take name and help from `X.Tool` / `X.Help` (`K5`).

| Field | Storage | EN — aligned | ES — aligned | EN — linear | ES — linear |
|---|---|---|---|---|---|
| name | `X.Tool` | Aligned Dimension | Cota alineada | Linear Dimension | Cota lineal |
| short_help | `X.Help` | Select two points, then click where the dimension line goes | Selecciona dos puntos y haz clic donde va la línea de cota | Select two points and a line or vector, then click where the dimension line goes | Selecciona dos puntos y una recta o vector, y haz clic donde va la línea de cota |
| long_help | guides §9 (new subsection) | Measures the distance between two points in model units. The value never changes with the units; only the displayed text uses the presentation unit. Drag the dimension line to move it. | Mide la distancia entre dos puntos en unidades del modelo. El valor no cambia con las unidades; solo el texto mostrado usa la unidad de presentación. Arrastra la línea de cota para moverla. | Measures the projection of two points on the direction of a line or vector, in model units. The value never changes with the units; only the displayed text uses the presentation unit. | Mide la proyección de dos puntos sobre la dirección de una recta o un vector, en unidades del modelo. El valor no cambia con las unidades; solo el texto mostrado usa la unidad de presentación. |
| status | `X.Help` (mode help line) | as short_help | as short_help | as short_help | as short_help |
| error | tool key `X.InvalidPlacement` (precedent `SplineV2.InvalidDefinition`) | The dimension cannot be placed: the points coincide or the direction is undefined. | No se puede colocar la cota: los puntos coinciden o la dirección no está definida. | same | igual |

### Owned icon designs (artwork drawn in `E2`)

GeoCeDG-authored SVG sources, 24 × 24 view box, the `mode_geocedg_splinev2.svg`
header and palette (`#111`, `#555`, accent `#4b6fff`), no inherited pixels or
paths (`K14`):

- `mode_geocedg_aligneddimension.svg`: two measured points on an oblique line;
  short perpendicular extension strokes with a gap at each point; an offset
  dimension line parallel to the measured segment with two filled arrowheads; a
  short accent bar above its middle, parallel to it, for the value.
- `mode_geocedg_lineardimension.svg`: two measured points at different heights;
  two vertical extension strokes with gaps; a horizontal dimension line with two
  filled arrowheads; a short accent arrow at the lower left marking the
  measurement direction.

No stored raster derivative is needed; the canonical-LF hash in the assets
manifest and the icon review test give determinism.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_E1_P_X1      = dc63b0e55f12eb96d3aea359db4476cbda6c89dc
tree                  = 8b2b5027a2119f996de8ca5af91b03ce32731a8c
T_R6PLUS_E2_PREP      = 6ad853c618da8f07a279b266c83002c145c6292c (documentary)
decision candidate    = the local descendant of T_R6PLUS_E2_PREP on
                        phase/pre-g9b-r6-plus-e2-prompt that contains this revision
                        (documentation only)
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base, that the documentary
candidates are the only delta, that the worktree is clean, and that
`final.desktop` is clean at the base; otherwise it stops and reports.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` v1.0, ADR 0031, ADR 0032 and `verification-levels.md`; the
E2 author-decision record and the authorizing instruction; this prompt; then the
reconciled design candidate, the report, the addendum and their JSON mirrors;
then the `P0` candidate and older evidence.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Paths are the expected ones; the implementation re-establishes each at its base.

**Shared kernel (`source/shared/common`, `source/shared/common-jre`).**

- `Commands.AlignedDimension` and `Commands.LinearDimension` (`TABLE_GEOMETRY`);
  their `case` in `CommandDispatcher.commandTableSwitch`; processor creation in
  `BasicCommandProcessorFactory`; two `Cmd*` processors.
- GeoCeDG-owned algorithms and a pure geometry helper (for example under
  `org.geocedg.common.kernel.dimension`): the design candidate §3–§5 and §7,
  the offset mapping helper for the drag, the presentation step and its single
  unit-listener registration per construction, and the `A8` text-geometry
  interface.
- A length presentation formatter over `UnitState.display` and the kernel
  number format, with the failure marker.
- The `A8` routing change in `EuclidianDraw` `case TEXT` and a GeoCeDG
  `DrawDimensionText extends DrawText` (rotation, reading rule, lift, rotated hit
  test, highlight, background, `Corner` bounds), active only for dimension text
  outputs.
- `command.properties` / `command_en` / `command_es` names, aliases and
  `.Syntax` lines; `menu.properties` / `menu_en` / `menu_es` tool keys;
  `EuclidianConstants` modes 142 and 143 with their mode text.
- Shared tests (commands, `SelfTest` methods, syntax localization, `Dimension`
  regression, geometry, degeneracies, presentation, unit invariance,
  serialization, drawable).

**Desktop (`source/desktop/desktop`).**

- Mode dispatch and the tool orchestrator in `GeoCeDGEuclidianController`;
  one-shot capture of offset, overshoot and gap as free hidden auxiliary
  numbers; one undo point per creation.
- The offset drag through the press/drag/release interception, one undo point on
  release; interception of every inherited route that would move `A` or `B`
  through a dimension output.
- `GeoCeDGToolImageResource` entries and the two owned SVG icons with their
  assets-manifest entries; the profile actions, group and cluster placement; the
  `GeoCeDGProfile` count pin and every `K5` test pin; the approved-amendment
  allow-list of `tools/agent/workspace-profile-validation.ps1` and its static
  contract pins, re-pinned through the official mechanism.
- In `GeoCeDGGeoGebraToPgf`, `…ToPstricks`, `…ToAsymptote`: overrides of
  `drawText` and `drawGeoSegment` for dimension outputs only (rotation,
  centred anchoring with lift, arrow endings).
- Coexistence tests with `Templatev7.ggb` documents and with a user store holding
  `directDimension` / `axisDimension`.

**Documentation and verification.**

- `geocedg/specs/dimensions/native-dimensions.md` and ADR 0034 (*Required
  design/specification*).
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The `PRE-G9B-R6` matrix rows, probes and fixtures and their hash pins.
- The phase registration `PRE-G9B-R6-PLUS-E2` (selections, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- The regenerated ADR 0031 fingerprint fixture.
- EN/ES guides (§3.2 tables, a §9 subsection, the reading flip at the vertical,
  the DXF subset, the older-reader note) and their outline pins.
- Candidate report, JSON evidence, minimal roadmap and mini-track status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Registering `DirectDimension`, `AxisDimension`, `directDimension` or
  `axisDimension` as names or aliases; renaming, deleting, migrating, rewriting
  or reinterpreting any user or document macro or stored user package.
- Any change to `Dimension`, its processor, algorithm, aliases or syntax, or
  overloading any command for graphical dimensioning.
- A new `GeoElement` type, `GeoClass`, XML element type or attribute, or a reader
  change.
- Any `UnitState`, `drawingScale`, viewport, zoom or DPI read by geometric
  computation or by a command; any unit factor as a DAG input; parsing a
  presentation string into geometry; LaTeX markup in the value string.
- A hidden unit multiplier; a default that depends on units, scale, viewport or
  `|AB|`.
- Storing a screen angle, a text bounding box or any other view state as
  construction state.
- Changing how any non-dimension text or segment is drawn, hit or exported, or
  changing Classic export semantics.
- Changing macro-versus-native dispatch precedence, `MacroManager` or the
  reverse table.
- `getDirectionInD3` as direction authority; any `F1` cue, option or accessor.
- A DXF `DIMENSION` entity, a DXF text or arrowhead mapping, or a DXF
  specification change.
- `E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Shared kernel: command identity, algorithms, geometry, degeneracies, value,
presentation step, text-geometry interface, serialization through existing
outputs. Shared renderer: one gated drawable for dimension text outputs
(presentation only). Desktop: tools, capture, drag, profile, icons, help.
Export adapters: the gated GeoCeDG LaTeX overrides. No dimension semantics in a
drawable or an exporter: both read the algorithm's geometry.

## Required design/specification

First deliverables of the phase, before product code:

1. `geocedg/specs/dimensions/native-dimensions.md` — the durable contract from
   the design candidate and the author-decision record (signatures, outputs,
   geometry, degeneracies, direction, pinned side, presentation, aligned value
   and reading rule, capture, drag, compatibility, export and the DXF subset).
2. ADR 0034 — native dimension representation (β), presentation seam and
   aligned-value drawable seam, status `PROPOSED` until the author approves it.

## Geometric invariants and degeneracies

- For every operation of `unit-system.md` §17.1 the value, every coordinate of
  every output and the construction XML apart from `geocedgUnits` stay
  byte-identical; only the text string changes.
- Zoom, viewport, DPI and axis-ratio independence of every semantic output; the
  aligned value follows the drawn figure.
- No side flip under continuous motion (B1); the reading-direction change at the
  vertical is presentation only.
- Degeneracies exactly as the design candidate §4.4; no stale geometry.
- Determinism for a fixed construction revision.

## Compatibility and serialization

- Additive `<command name="AlignedDimension">` / `"LinearDimension"` with
  existing output element types; documents without them re-save byte-identically.
- Save and reopen, undo and redo, copy and paste (with the hidden inputs),
  redefine: identity of value, geometry, style and of the derived orientation.
- Older GeoCeDG, Classic and the pinned baseline: unknown command, load error,
  outputs and dependents missing (`K16`); characterized on an archived base build
  and stated in the guides.
- Coexistence: a `Templatev7.ggb`-derived document and a user store with the
  legacy macros keep working; the new commands are reachable in both; no byte of
  the store or document changes.

<!-- geocedg-field: required_checks -->
## Required tests and commands

| ID | Obligation |
|---|---|
| `T-E2-VALUE` | analytic values for aligned and linear (lines, segments, rays, axes, vectors, both orientations); independence from every unit operation, `drawingScale`, zoom, viewport |
| `T-E2-GEOMETRY` | feet, extension rule, arrow endings, anchor; recompute on every input; no side flip under continuous motion (B1) |
| `T-E2-DEGENERACY` | every row of the design candidate §4.4 |
| `T-E2-PRESENTATION` | `cm`/`mm` 12 → `120 mm`, then `m` → `0.12 m`, number 12 throughout; `usm` with and without symbol; unspecified; failure marker; rounding; no geometric recompute and no XML change but `geocedgUnits` on a unit change; undo/redo of a unit operation |
| `T-E2-TEXT-ALIGN` | the addendum sweep (0°, 30°, 44°, 45°, 46°, 89°, 90°, 91°, 135°, 179°, both input orders) through the real drawable: parallel, centred, readable, no crossing; zoom, resize, value widths, suffix change, both sides, anisotropic axes, long value on a short line; hit testing, highlight, `Corner` bounds |
| `T-E2-SERIALIZATION` | save/reopen, undo/redo, copy/paste, redefine; byte identity of unrelated documents; no view state in XML |
| `T-E2-COMPAT` | archived-base reader behavior (evidence) |
| `T-E2-COMMANDS` | ADR 0031 gate 566/515/488 and regenerated fingerprints; EN/ES aliases; autocomplete; Input Help; `SelfTest`; syntax localization; **`Dimension` regression** (values, alias, syntax) |
| `T-E2-GGBSCRIPT` | matrix rows and probes for both commands; the completeness gate passes and its negative control still fails closed |
| `T-E2-COEXISTENCE` | `Templatev7.ggb` document and a user store with the legacy macros: macros unchanged and working, new commands reachable, no byte change |
| `T-E2-TOOL` | tool flows; one-shot capture (later unit, scale or zoom changes leave the inputs unchanged; no millimetre pretence when unspecified); one undo point per creation; drag edits only the offset; no route moves `A` or `B`; mode cancel |
| `T-E2-PROFILE` | 127 actions and every `K5` pin; allow-list amendment; icons in the manifest and the icon review test |
| `T-E2-EXPORT` | picture formats at several DPI and scales with arrows and the aligned value; PSTricks, PGF, Asymptote with arrows, rotation and centring for dimension outputs only and unchanged output for other objects; DXF segment subset and the text diagnostic; `ExportArea` and hidden layers apply |
| `T-E2-GUIDE` | EN/ES guide parity and outline pins |
| `T-SMOKE` | author smoke in EN and ES (not by the agent) |

Commands (PowerShell; fresh log directories under `artifacts\agent\`):

```powershell
.\gradlew.bat :shared:common-jre:test --tests <focused E2 shared classes>
.\gradlew.bat :desktop:desktop:test --tests <focused E2 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E2 -LogDirectory artifacts\agent\<fresh>
.\tools\agent\verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase requires at least `compile.shared.semantic`,
`compile.desktop.semantic`, `workspace-profile.product`,
`static.ggbscript-matrix-inputs.semantic`, `ggbscript-matrix.product`,
`junit.shared.pre-g9b-r6-plus-e2.semantic` and
`junit.desktop.pre-g9b-r6-plus-e2.semantic` (precedent `PRE-G9B-R6-PLUS-B`).
`PHASE` and `INTEGRATION` run once each on the same frozen clean commit and tree.
No `FINAL`. A run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`E2` on an exact base; it then authorizes local implementation on a local
branch, local commits, focused tests, the phase registration and its official
catalog updates, development `INFRA_UNIT` and `STATIC` runs, the frozen
acceptance runs, and one frozen technical candidate for author review and author
smoke. It never authorizes self-approval, author smoke by the agent, a run above
the frozen class, or a change of the class; a proven inability of the frozen
class to cover the change is reported as a reclassification proposal, never
applied silently.

`E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does
every open observation not named here. Author approval is never created by
technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication are forbidden; each needs a separate
explicit author instruction naming the exact candidate SHA. Acceptance evidence
never grants publication authority.

## Acceptance and closeout

```text
PRE-G9B-R6-plus-E2      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (INTEGRATED_PHASE: PHASE + INTEGRATION)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval.

## Required artifacts

- The specification and ADR; the kernel, renderer, Desktop, export, profile,
  icon, matrix, registry and guide deltas with their tests.
- A candidate report with the entry gate, `K1`–`K20` and `A1`–`A10`
  re-established with every correction, test evidence, exact commands, exit
  codes, report paths and verdicts, and an author-smoke checklist: create both
  dimensions in EN and ES; change construction and presentation units and watch
  only the texts change; rotate a dimension through the vertical; drag the
  dimension line; undo and redo; save and reopen; copy and paste; open
  `Templatev7.ggb` and use its legacy macros and the new commands; export PNG,
  PDF, SVG, the three LaTeX dialects and DXF.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- correctness needs a new `GeoElement` type, XML element type, attribute, reader
  change or materially new global serialization semantics (request a
  `GLOBAL_IMPACT` review; do not implement α);
- the aligned value cannot be produced without storing view state, without
  changing non-dimension texts, or without a new semantic identity
  (`DQ-E2-6 REPRESENTATION ESCALATION REQUIRED`);
- the presentation seam cannot refresh the text without a geometric recompute or
  a unit dependency;
- a route moves `A` or `B` through a dimension output and cannot be intercepted
  in the GeoCeDG layer;
- coexistence with the legacy macros would need renaming, deleting or
  reinterpreting a macro, or a dispatch-precedence change;
- `Dimension` behavior, aliases or syntax would change;
- the ADR 0031 or GGBScript completeness gate cannot pass without weakening it;
- an export change would alter Classic export semantics, non-dimension outputs or
  the DXF specification;
- licensing or provenance of an icon is unclear.

# PRE-G9B-R6-plus-E2 — native dimensions (`DirectDimension`, `AxisDimension`)

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-07, given after
`PRE-G9B-R6-plus-E1-P-X1` was closed and published with a clean `final.desktop`
baseline. That instruction authorized the characterization, design
reconciliation and preparation of `E2` only. It stated that it **does not
authorize product implementation**, that the `P0` design candidate is evidence
and not authority, that the kernel placement of the two commands is frozen for
the characterization, and that the representation, the verification class and
every other open choice stay pending author decision. The evidence behind the
characterization below is in the
[E2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md)
(sections `K1`–`K20`); the proposed design is in the
[reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-E2` and the exact prepared candidate or base, and that disposes
of the requested decisions `DQ-E2-1` to `DQ-E2-11`. That instruction may
authorize, as the first tracked edit of the phase, an amendment of this prompt
to the authorized state, following the `A-1`, `B`, `D0`, `D1`, `A-2`, `C`,
`C-X1`, `E1-L` and `E1-X1` precedent. This file is an execution contract, not a
second policy document: the unit rules are stated once in the
[unit-system specification](../../../geocedg/specs/units/unit-system.md)
(§5, §6, §14, §17, §18.3); the command-head rules once in
[ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md);
the verification classes once in
`geocedg/specs/operations/verification-levels.md` §12.8; the probe evidence once
in the characterization report. This prompt cites them and does not restate
them differently. Where it fixes a design value that no durable specification
holds yet, that value is a proposal until the author disposes of the matching
`DQ`, and the first deliverable of `E2` turns it into the durable specification
named under *Required design/specification*.

```text
PRE-G9B-R6-plus-E2 =
PREPARED — NOT AUTHORIZED

CHARACTERIZATION         = COMPLETE (2026-10-07, on P_R6PLUS_E1_P_X1)
IMPLEMENTATION           = NOT AUTHORIZED
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = SHARED-KERNEL COMMANDS AND ALGORITHMS + DESKTOP TOOLS,
                           DRAG, PROFILE, ICONS AND HELP + ADDITIVE COMMAND
                           SERIALIZATION WITH EXISTING OUTPUT TYPES
                           (representation β proposed, DQ-E2-1)
DEPENDS_ON               = PRE-G9B-R6-plus-D1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (hard: unit state and listener)
                           PRE-G9B-R6-plus-E1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                (hard for the Desktop tool icons
                                                only; recommended predecessor)
                           PRE-G9B-R6-plus-E1-P-X1 = PASS — AUTHOR APPROVED —
                                                PUBLISHED (clean final.desktop)
                           NOT F1 (E2 executes before F1 and must not depend on it)
GLOBAL GATE              = closeout in PRE-G9B-R6-plus-G
PROPOSED CLASS           = INTEGRATED_PHASE — NOT FROZEN (DQ-E2-8)
PRECONDITION             = author dispositions of DQ-E2-1 to DQ-E2-11
NEXT_SUBPHASE            = none implied; E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-defined native dimension scope (mini-track plan §2, §3.2
`E2` brief, §8.5; `unit-system.md` §14 and §18.3) as shared-kernel commands
with Desktop tools:

1. Two native commands, default heads `DirectDimension` and `AxisDimension`
   (or the collision-free pair of `DQ-E2-7`), implemented by shared-kernel
   algorithms whose outputs are existing object types (β, `DQ-E2-1`): a stable
   model-unit value, a dimension segment with arrow endings, two extension
   segments, and a horizontal centred presentation text.
2. Numeric value = model-unit measure (`L = |B − A|`, or
   `L = |⟨B − A, d̂⟩|`), never dependent on `constructionUnit`,
   `presentationUnit`, the `usm` factor, `drawingScale`, viewport, zoom or DPI.
3. Presentation string = `format(display(L)) + " " + suffix(effP)` for a
   physical construction unit and the bare `format(L)` for
   `UNSPECIFIED_MODEL_UNIT`, refreshed on unit changes without any geometric
   recompute (`DQ-E2-3`).
4. An explicit signed model-unit offset, and explicit model-unit overshoot and
   gap, as ordinary construction inputs; no hidden unit multiplier; no screen
   read in the command (`AGENTS.md` §10).
5. A deterministic, continuous placement side (`DQ-E2-4`) and an `E2`-local
   direction contract for `AxisDimension` (`DQ-E2-5`).
6. Desktop tools in the `construction-metrics` group (Metrics and validation)
   and the Construction menu, with GeoCeDG-owned SVG icons, bilingual text, a
   one-shot creation-time capture of the initial parameters and a drag of the
   dimension line that edits exactly the offset (`DQ-E2-10`, `DQ-E2-11`).
7. ADR 0031 canonical-head and complete-inventory gates, the `PRE-G9B-R6`
   GGBScript matrix rows, and a disposition of the legacy macro collision
   (`DQ-E2-7`) that never renames, deletes or reinterprets a user's macro
   silently.

### Authorities and author requirements this prompt implements

| Requirement | Source | Where it lands |
|---|---|---|
| kernel commands; DAG; persistence; every frontend | plan §8.5; `AGENTS.md` §4 | §*Architectural placement* |
| value = model-unit measure; text-only unit conversion; bare value when unspecified; failure marker | `unit-system.md` §6.1, §6.2, §14.1–§14.3, §18.3 | design candidate §6; `DQ-E2-3` |
| overshoot and gap as creation-time captured model-unit inputs | `unit-system.md` §6.3, §18.3 (`AQ-E4`) | design candidate §3.2, §8; `DQ-E2-11` |
| explicit signed offset, `DOCUMENT_SEMANTIC`, independent of `|AB|`, zoom, DPI | plan §8.5; author instruction | design candidate §3.2 |
| no far-away line; centred figure; deterministic above/left side; parallel text where sustainably supported | `P0` candidate §1 (author-fixed scope) | `DQ-E2-4`, `DQ-E2-6` |
| English canonical head, Spanish aliases, complete-inventory gate | ADR 0031 decisions 5, 7, 12 | `DQ-E2-10`; *Required tests* |
| GGBScript matrix completeness gate fails closed | `PRE-G9B-R6` | *Required tests* |
| owned icons through the `E1` pipeline | `E1` (hard frontend dependency) | `DQ-E2-10` |
| legacy macro collision is an `E2`-owned cross-phase constraint | `E1` preparation closeout record (2026-10-07) | `DQ-E2-7` |

### Characterization results at the base (summary; evidence in the report)

- `Dimension(<Object>)` is upstream cardinality of lists, matrices, points and
  vectors and is unrelated (`K2`). It is not reused or overloaded; its values,
  alias `Dimensión` and syntax must stay unchanged.
- All candidate names are free (`K3`). The registration path follows `LocusV2`
  and `SplineV2`; autocomplete, Input Help and syntax help follow
  automatically; no GeoCeDG command allow-list exists.
- The ADR 0031 pins move from 564/513/486 to 566/515/488 and the R5-B
  fingerprint fixture must be regenerated by its documented route (`K3`).
- The GGBScript matrix gate is a JUnit derived-inventory test that will fail
  `MISSING_COVERAGE` until both commands are covered; each syntax line needs six
  rows, plus lookup probes (`K4`).
- The profile has **125** actions (not 115); `construction-metrics` is at
  `application-profile.yml:3475-3480`; the next mode ids are 142 and 143; the
  count, toolbar and allow-list pins are listed in `K5`.
- `getDirection` follows the construction convention; `getDirectionInD3` is
  opposite for parallel, perpendicular and point–vector lines and rays (`K7`).
- The `D1` unit listener exists; no production code formats a length with a
  unit yet; no existing text refreshes outside a recompute (`K8`).
- Centred horizontal text anchored in model coordinates is supported; rotated
  text exists only as LaTeX `\rotatebox` without a start point; the point of
  `Rotate(text, angle, point)` is ignored (`K9`).
- Picture export draws the whole β figure; PSTricks, PGF and Asymptote drop the
  arrow endings and the text centring; DXF writes the segments only (`K12`).
- `ChangeableParent` is 3D-only; the inherited 2D dependent move would
  translate the measured points; the GeoCeDG press/drag/release interception
  is the seam for the offset drag (`K13`).
- Macros resolve before native commands, case-insensitively, in every regime;
  a stored legacy package disables the whole user library; an older reader
  drops the unknown command's outputs and their dependents (`K15`, `K16`).

### Design proposed for authorization

The [reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md)
holds the proposed contract: signatures and arguments (§3), output order
(§3.3), geometry (§4.1–§4.3), degeneracies (§4.4), direction authority (§5),
presentation seam (§6), side rule (§7), interaction (§8), persistence,
compatibility and export (§9), and the stale `P0` facts it supersedes (§10).
None of it is author-approved.

### Decisions requested before authorization

Each recommendation is **PROPOSED — PENDING AUTHOR DECISION**. Nothing below is
self-approved.

| ID | Question | Options (evidence) | Recommendation |
|---|---|---|---|
| `DQ-E2-1` | Representation | **β**: one algorithm per command, outputs of existing types; additive `<command>` XML; picture export at once; `INTEGRATED_PHASE`. **α**: composite `GeoDimension` with `GeoClass`, XML element type, drawable, hit testing, style panels, copy/redefine and four export adapters; older readers fail on the element type; `GLOBAL_IMPACT` (`K10`–`K12`, `K17`) | **β** |
| `DQ-E2-2` | Exact signatures and output contract | (a) offset mandatory: `DirectDimension(A, B, Offset[, Overshoot, Gap])`, `AxisDimension(A, B, Direction, Offset[, Overshoot, Gap])`; (b) also the short forms with `Offset = 0`. Both: overshoot = gap = 0 when omitted; outputs `[value, dimension line, extension A, extension B, text]`; `A = B` gives value `0` and an undefined figure; an extension exists only when `|t_P| > Gap`; `Direction` = 2D line (incl. segment, ray, axes) or 2D vector; separate syntax lines for line and vector (design candidate §3, §4.4; `K4` row counts) | **(a)**, with the listed contract |
| `DQ-E2-3` | Presentation-text seam | **S1a**: the algorithm splits `compute()` (geometry and value, never reads the unit state) from a presentation step run after it in the ordinary update, at construction and from the `D1` listener, writing only the text string; no geometric recompute, no DAG input. **S1b**: the same step inside `compute()` (needs the author to read `unit-system.md` §6.1 as limited to geometric computation). S2: a second presentation algorithm. S3: draw-time string in a special drawable (α territory). S4: a unit-factor input (forbidden by §6.1). Failure marker for a non-finite `display(L)` (`unit-system.md` §14.3) (`K8`) | **S1a**; failure marker `?` followed by the suffix; a dimension inside a user macro shows the bare value (macro unit state is `EMPTY`), recorded as a known limitation |
| `DQ-E2-4` | Side rule | **A**: live canonical rule (`+x` if `|tx| ≥ |ty|`, else `+y`), order-independent but flips at `|tx| = |ty|`. **B1**: side pinned by explicit inputs — input order (direct) or kernel direction (axis) plus the signed offset; continuous; the tool applies the canonical above/left rule once at creation. **B2**: an extra explicit reference-axis input; the flip moves to 90° from the pin (design candidate §7) | **B1** |
| `DQ-E2-5` | `AxisDimension` direction authority | **E2-local**: measurement axis = unoriented line of the input's `getDirection` (line, segment, ray, axis) or vector coordinates; placement side = that existing orientation; never `getDirectionInD3`; no user-visible cue, option or accessor (`F1` scope). Alternative: canonicalized axis orientation (flips if the direction object rotates through the tie) (`K7`) | **E2-local** |
| `DQ-E2-6` | Text orientation | **Horizontal centred text**, recorded as `KNOWN LIMITATION — horizontal centred text`; parallel text would need screen metrics in the kernel or a new drawable (α), re-tested at the base (`K9`) | **KNOWN LIMITATION — horizontal centred text** |
| `DQ-E2-7` | Legacy `directDimension` / `axisDimension` collision | **A**: keep names, native wins in dispatch — rejected: USER input normalizes `directDimension(A,B,10)` to the native head and would silently reinterpret `kconversion` as an offset; it changes shared macro precedence for every document (`GLOBAL_IMPACT`). **B**: collision-free native heads `AlignedDimension` / `LinearDimension` (ES `CotaAlineada` / `CotaLineal`), free at the base; macros keep working unchanged in documents and in the user library. **C**: keep the names; the user library isolates each colliding stored package with an explicit conflict notice (store bytes unchanged, nothing renamed or deleted, other packages usable); in a document that defines a colliding macro the native tool and command refuse with a typed explicit conflict naming the macro, and a rename is offered only as an explicit user action (one undo point); native unavailable in that document until then. **D**: keep names and current behavior — rejected (whole library disabled; native elements unloadable in such documents) (`K15`) | **B**; if the author keeps the names, **C** |
| `DQ-E2-8` | Verification class | **`INTEGRATED_PHASE`** (registered `PHASE -Phase PRE-G9B-R6-PLUS-E2` + `INTEGRATION` on the same frozen commit) for β with `DQ-E2-7` B or C and `DQ-E2-9` X0 or X1; **`GLOBAL_IMPACT`** (one `FINAL`) for α, `DQ-E2-7` A, a new `GeoElement` type, a new XML element type or attribute, or any change of global serialization or command-dispatch semantics (`K18`) | **`INTEGRATED_PHASE`**, frozen at authorization |
| `DQ-E2-9` | Export fidelity of the figure | **X0**: picture only; record the LaTeX and DXF gaps as known limitations with the existing DXF diagnostic. **X1**: X0 plus GeoCeDG-profile LaTeX fidelity for segment arrow endings and text alignment in PSTricks, PGF and Asymptote (general, through the GeoCeDG LaTeX exporter of `C`; Classic unchanged); DXF stays as in X0. **X2**: X1 plus DXF `TEXT` and arrowhead geometry, amending the normative DXF specification and the G9X1 pins (`K12`) | **X1** (class unchanged, `C` precedent) |
| `DQ-E2-10` | Names, aliases and product placement | heads per `DQ-E2-7`; ES aliases `CotaDirecta` / `CotaAxial` (or `CotaAlineada` / `CotaLineal`); action ids `measure.direct-dimension` / `measure.axis-dimension` (or `measure.aligned-dimension` / `measure.linear-dimension`); `upstream-mode` actions with new `MODE_*` ids 142 and 143; `construction-metrics` presentation group and toolbar after `measure.distance-length`; cluster `metric-validation-tools` toolbar; Construction menu through the group; `TABLE_GEOMETRY` for the constants (`K5`) | as listed |
| `DQ-E2-11` | Tool flow and creation-time capture | clicks: direct = point, point, placement; axis = point, point, line or vector, placement. Offset = signed distance of the placement click, model units, captured once. Overshoot and gap: (i) from the view (8 px and 4 px converted once with the view scale); (ii) from paper sizes (2 mm and 1 mm on the output medium, converted once with the effective construction unit and the session `drawingScale`) when the unit is physical, else (i); (iii) fixed fractions of the captured offset. All three are one-shot and stored as free hidden auxiliary numbers (`unit-system.md` §6.3) | **(ii) with (i) for `UNSPECIFIED_MODEL_UNIT`** |

### Prepared bilingual tool text (proposal; final wording is the author's)

The profile carries `name`/`short_help`/`long_help`/`status`/`error` only for
non-mode actions; `upstream-mode` actions take name and help from the
properties keys `X.Tool` and `X.Help` (`K5`). The prepared text and its storage:

| Field | Storage | EN — direct | ES — direct | EN — axis | ES — axis |
|---|---|---|---|---|---|
| name | `X.Tool` | Direct Dimension | Cota directa | Axis Dimension | Cota axial |
| short_help | `X.Help` | Select two points, then click where the dimension line goes | Selecciona dos puntos y haz clic donde va la línea de cota | Select two points and a line or vector, then click where the dimension line goes | Selecciona dos puntos y una recta o vector, y haz clic donde va la línea de cota |
| long_help | guides §9 (new subsection) | Measures the distance between two points in model units. The value never changes with the units; only the text shows the presentation unit. Drag the dimension line to move it. | Mide la distancia entre dos puntos en unidades del modelo. El valor no cambia con las unidades; solo el texto muestra la unidad de presentación. Arrastra la línea de cota para moverla. | Measures the projection of two points on the direction of a line or vector, in model units. The value never changes with the units; only the text shows the presentation unit. | Mide la proyección de dos puntos sobre la dirección de una recta o un vector, en unidades del modelo. El valor no cambia con las unidades; solo el texto muestra la unidad de presentación. |
| status | `X.Help` (mode help line) | as short_help | as short_help | as short_help | as short_help |
| error | new tool keys, e.g. `X.Conflict`, `X.InvalidPlacement` (precedent `SplineV2.InvalidDefinition`) | The dimension cannot be placed: the points coincide or the direction is undefined. | No se puede colocar la cota: los puntos coinciden o la dirección no está definida. | same | igual |

With `DQ-E2-7` B the names become Aligned Dimension / Cota alineada and Linear
Dimension / Cota lineal; under C a conflict error names the colliding macro.

### Owned icon designs (proposal; artwork drawn in `E2`)

Both are GeoCeDG-authored SVG sources, 24 × 24 view box, the
`mode_geocedg_splinev2.svg` header and palette (`#111` strokes, `#555` auxiliary,
`#4b6fff` accent), no inherited GeoGebra pixels or paths
(`K14`):

- `mode_geocedg_directdimension.svg`: two measured points on an oblique line
  (lower left, upper right); two short extension strokes perpendicular to it
  with a visible gap at each point; an offset dimension line parallel to the
  measured segment with two filled arrowheads; a small accent tick at its
  middle for the text.
- `mode_geocedg_axisdimension.svg`: two measured points at different heights;
  two vertical extension strokes with gaps; a horizontal dimension line with
  two filled arrowheads above them; a short accent arrow at the lower left
  marking the measurement direction.

Desktop tool icons need no stored raster derivative: they are rendered at run
time from the SVG; determinism is the canonical-LF hash in the assets manifest
and the existing icon review test (`K14`).

<!-- geocedg-field: implementation_base -->
## Implementation base

Prepared on, and to be authorized on an exact descendant named by the author:

```text
P_R6PLUS_E1_P_X1 = dc63b0e55f12eb96d3aea359db4476cbda6c89dc
tree             = 8b2b5027a2119f996de8ca5af91b03ce32731a8c
preparation candidate = the local commit of phase/pre-g9b-r6-plus-e2-prompt
                        that contains this file (documentation only)
```

Before any tracked change the agent verifies that local `main`, `origin/main`
and the live remote `main` resolve to the authorized base (or that the authorized
preparation candidate is its only delta), that the worktree is clean, and that
`final.desktop` is clean at the base; otherwise it stops and reports.

## Authority and evidence hierarchy

`AGENTS.md` §2 governs. For this phase: current code and tests at the base;
`unit-system.md` v1.0, ADR 0031, ADR 0032 and `verification-levels.md`; the
author dispositions of `DQ-E2-1`–`DQ-E2-11`; this prompt; then the reconciled
design candidate, the characterization report and its JSON mirror; then the
`P0` candidate and older evidence. Probe outputs, reports and screenshots are
evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Subject to the `DQ` dispositions; paths are the expected ones and the
implementation re-establishes each at its base.

**Shared kernel (`source/shared/common`, `source/shared/common-jre`).**

- Two `Commands` constants in `TABLE_GEOMETRY`; one `case` each in
  `CommandDispatcher.commandTableSwitch`; processor creation in
  `BasicCommandProcessorFactory`; two `Cmd*` processors (argument checking,
  typed errors, no screen read).
- GeoCeDG-owned algorithms and a pure geometry helper (for example under
  `org.geocedg.common.kernel.dimension`): β outputs in the fixed order,
  geometry of design candidate §4, degeneracies of §4.4, a pure
  `offsetFor(point)` mapping used by the drag, and the presentation step of
  `DQ-E2-3` with its single unit-listener registration per construction.
- A length presentation formatter over `UnitState.display` and the kernel
  number format (`unit-system.md` §14.2), with the failure marker.
- `command.properties` / `command_en` / `command_es` names, aliases and
  `.Syntax` lines; `menu.properties` / `menu_en` / `menu_es` tool keys;
  `EuclidianConstants` modes 142 and 143 with their mode text.
- Shared tests: commands, `SelfTest` methods, syntax localization, `Dimension`
  regression, geometry, degeneracies, presentation, unit invariance,
  serialization.

**Desktop (`source/desktop/desktop`).**

- Mode dispatch and a tool orchestrator in `GeoCeDGEuclidianController`
  (precedent `GeoCeDGSimilarityTools`, `GeoCeDGSplineV2Authoring`); one-shot
  capture of offset, overshoot and gap as free hidden auxiliary numbers; one
  undo point per creation.
- Press/drag/release interception that maps a drag of a dimension output to the
  offset only, one undo point on release; interception of every inherited route
  that would move `A` or `B` through a dimension output.
- The `DQ-E2-7` collision handling (none needed under B beyond tests).
- `GeoCeDGToolImageResource` entries and the two owned SVG icons; the assets
  manifest entries; profile actions, group and cluster placement; the
  `GeoCeDGProfile` count pin and every test pin of `K5`; the approved-amendment
  allow-list in `tools/agent/workspace-profile-validation.ps1` and its static
  contract input pins, re-pinned through the official mechanism.
- Under `DQ-E2-9` X1: the GeoCeDG LaTeX exporters for segment endings and text
  alignment in the GeoCeDG profile only.
- Desktop tests: tools, drag, undo, profile, icons, GGBScript matrix rows,
  collision behavior, exporters, unit refresh.

**Documentation and verification.**

- The durable specification and ADR of *Required design/specification*.
- `docs/upstream/modified-files.yml` records for every changed or added file
  under `source/`.
- The `PRE-G9B-R6` matrix rows, probes and fixtures; its hash pins.
- The phase registration `PRE-G9B-R6-PLUS-E2` (selections, nodes, inventory
  through `tools/agent/update-verification-junit-inventory.ps1`) and the
  registry-shape pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- The ADR 0031 fingerprint fixture, regenerated by its documented route.
- The EN and ES user guides (§3.2 tables, a new §9 subsection, the older-reader
  compatibility note) and their outline pins.
- The candidate report, its JSON evidence, minimal roadmap and mini-track
  status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change to `Dimension`, its processor, algorithm, aliases or syntax.
- Overloading `Dimension`, or any command name, for graphical dimensioning.
- A new `GeoElement` type, `GeoClass`, XML element type or attribute, or any
  change of a reader, unless the author froze α and `GLOBAL_IMPACT`.
- Any read of `constructionUnit`, `presentationUnit`, the `usm` factor,
  `drawingScale`, viewport, zoom or DPI by geometric computation or by a
  command; any unit factor as a DAG input; parsing a presentation string into
  geometry or a number.
- A hidden unit multiplier; a default that depends live on units, scale,
  viewport or `|AB|`.
- Any change of macro-versus-native dispatch precedence, of `MacroManager` or of
  the reverse table, unless the author chose `DQ-E2-7` A and `GLOBAL_IMPACT`.
- Renaming, deleting, rewriting or reinterpreting a user's macro, a stored user
  package or a document macro without an explicit user action.
- Using `getDirectionInD3` for direction; any `F1` orientation cue, option or
  accessor.
- A Desktop-only geometry helper; geometry in an exporter, a script or the
  profile.
- DXF entity or specification changes unless `DQ-E2-9` X2; Classic export
  semantics changes.
- `E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7`, publication, merge, tag, release.
- Editing `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `.github/prompts/**`
  (other than the authorized amendment of this prompt) or `ai-shell/prompts/**`.

## Architectural placement

Shared Java kernel: command identity, algorithms, geometry, degeneracies,
value, presentation step, serialization through existing outputs (`AGENTS.md`
§4: DAG, dynamic evaluation, persistence, several frontends). Desktop
application layer: tools, initial capture, drag gesture, profile placement,
icons, help. Export adapters: only the GeoCeDG-gated fidelity of `DQ-E2-9`; no
dimension semantics in an exporter.

## Required design/specification

First deliverables of the phase, before product code:

1. `geocedg/specs/dimensions/native-dimensions.md` — the durable contract
   (signatures, arguments, outputs, geometry, degeneracies, direction contract,
   side rule, presentation, capture, drag, compatibility, export behavior), from
   the reconciled design candidate and the `DQ` dispositions.
2. ADR 0034 — native dimension representation and presentation seam (β versus
   α, `DQ-E2-3`, `DQ-E2-7`), status `PROPOSED` until the author approves it.

## Geometric invariants and degeneracies

- Value independence: for every operation of `unit-system.md` §17.1 the value,
  every coordinate of every output and the construction XML apart from
  `geocedgUnits` are byte-identical; only the text string changes.
- Zoom, viewport and DPI independence of every output.
- Continuity: no side flip under continuous motion of `A`, `B` or the direction
  for every non-degenerate configuration (`DQ-E2-4` B1).
- Degeneracies exactly as design candidate §4.4: no tolerance, explicit
  undefined outputs, no stale geometry, auxiliary inputs always valid.
- Determinism: identical outputs for a fixed construction revision.

## Compatibility and serialization

- Additive `<command name="…">` with existing output element types; arrow
  endings as element style; offset, overshoot and gap as ordinary numerics.
- Documents without the new commands re-save byte-identically.
- Save and reopen, undo and redo, copy and paste (with the hidden numeric
  inputs), redefine of an output and of an input number: identity of value,
  geometry and style.
- Older GeoCeDG, Classic and the pinned upstream baseline: unknown command,
  load error, outputs and dependents missing, lost on re-save (`K16`); stated in
  the guides and characterized on an archived base build.
- `DQ-E2-7` behavior for `Templatev7.ggb`-derived documents and for a user
  store holding the legacy macros, with store and document bytes unchanged.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Proposed verification plan (frozen at authorization with `DQ-E2-8`):

| ID | Obligation |
|---|---|
| `T-E2-VALUE` | analytic values for direct and axis (lines, segments, rays, axes, vectors, both orientations); independence from every unit operation, `drawingScale`, zoom and viewport |
| `T-E2-GEOMETRY` | feet, extension rule, arrow endings, text anchor and alignment; dynamic recompute on every input; no flip across 45° (B1) |
| `T-E2-DEGENERACY` | every row of design candidate §4.4 |
| `T-E2-PRESENTATION` | strings for `mm`, `cm`, `m`, `usm` with and without symbol, unspecified, overflow marker, rounding; a unit change refreshes the text with no geometric recompute (instrumented) and no XML change but `geocedgUnits`; undo and redo of a unit operation; macro limitation |
| `T-E2-SERIALIZATION` | save/reopen, undo/redo, copy/paste, redefine, byte identity of unrelated documents |
| `T-E2-COMPAT` | archived-base reader drops the outputs and dependents with a load error (evidence, scratch base build) |
| `T-E2-COMMANDS` | ADR 0031 gate 566/515/488 and regenerated fingerprints; EN/ES aliases; autocomplete and Input Help; `SelfTest`; syntax localization; **`Dimension` regression** (values, alias, syntax unchanged) |
| `T-E2-GGBSCRIPT` | matrix rows and probes for both commands (`K4`); the completeness gate passes, and its negative control still fails closed |
| `T-E2-COLLISION` | `DQ-E2-7`: `Templatev7.ggb` document, a user store with the legacy macros, typed legacy calls; no silent rename, delete or reinterpretation; store bytes unchanged |
| `T-E2-TOOL` | tool flows, one-shot capture (later unit, scale or zoom change leaves the inputs unchanged), one undo point per creation, drag edits only the offset with one undo point, no route moves `A` or `B`, mode cancel |
| `T-E2-PROFILE` | 127 actions and every `K5` pin; allow-list amendment; icons in the manifest and in the icon review test |
| `T-E2-EXPORT` | picture formats show arrows and centred text; LaTeX per `DQ-E2-9`; DXF segments and the text diagnostic; `ExportArea` and hidden layers apply |
| `T-E2-GUIDE` | EN/ES guide parity and outline pins |
| `T-SMOKE` | author smoke in EN and ES (not performed by the agent) |

Commands (PowerShell; log directories fresh, under `artifacts\agent\`):

```powershell
.\gradlew.bat :shared:common-jre:test --tests <focused E2 shared classes>
.\gradlew.bat :desktop:desktop:test --tests <focused E2 desktop classes>
.\tools\agent\verify.ps1 -Profile STATIC
.\tools\agent\verify.ps1 -Profile INFRA_UNIT
.\tools\agent\verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E2 -LogDirectory artifacts\agent\<fresh>
.\tools\agent\verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh>
git diff --check
```

The registered phase `PRE-G9B-R6-PLUS-E2` requires at least
`compile.shared.semantic`, `compile.desktop.semantic`,
`workspace-profile.product`, `static.ggbscript-matrix-inputs.semantic`,
`ggbscript-matrix.product`, `junit.shared.pre-g9b-r6-plus-e2.semantic` and
`junit.desktop.pre-g9b-r6-plus-e2.semantic` (precedent `PRE-G9B-R6-PLUS-B`).
`PHASE` and `INTEGRATION` run once each, on the same frozen clean commit and
tree; `INTEGRATION` covers `final.desktop` and the shared suites. No `FINAL`
unless the class is `GLOBAL_IMPACT`. A run above the frozen class fails with
`VERIFICATION_ESCALATION_REQUEST` and waits for the author.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`E2` on an exact base with the `DQ-E2` dispositions; it then authorizes local
implementation on a local branch, local commits, focused tests, the phase
registration and its official catalog updates, development `INFRA_UNIT` and
`STATIC` runs, the frozen acceptance runs, and one frozen technical candidate
for author review and author smoke. It never authorizes self-approval, author
smoke by the agent, a run above the frozen class, or a change of the class; a
proven inability of the frozen class to cover the change is reported as a
reclassification proposal, never applied silently.

`E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does
every open observation and enhancement not named here. Author approval is never
created by technical verification.

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
AUTOMATED VERIFICATION  = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE            = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The specification and ADR; the kernel, Desktop, profile, icon, matrix,
  registry and guide deltas with their tests.
- A candidate report with the entry gate, `K1`–`K20` re-established with every
  correction, the `DQ` dispositions applied, test evidence, the exact commands,
  exit codes, report paths and verdicts, and an author-smoke checklist: create
  both dimensions in EN and ES; change construction and presentation units and
  watch only the texts change; drag the dimension line; undo and redo; save and
  reopen; copy and paste; open `Templatev7.ggb` and exercise the `DQ-E2-7`
  behavior; export PNG, PDF, SVG, the three LaTeX dialects and DXF.

## Stop conditions

Stop and report, without guessing, when:

- the base or the entry gate differs from *Implementation base*;
- β cannot express a required output with existing types, or correctness
  needs a new `GeoElement` type, XML element or attribute, or a reader change
  (`GLOBAL_IMPACT`, author decision);
- the presentation seam cannot refresh the text without a geometric recompute
  or a unit dependency;
- a route moves `A` or `B` through a dimension output and cannot be
  intercepted in the GeoCeDG layer;
- the collision disposition would rename, delete or reinterpret a user's macro
  without an explicit user action, or would change shared dispatch precedence;
- `Dimension` behavior, aliases or syntax would change;
- the ADR 0031 gate or the GGBScript completeness gate cannot pass without
  weakening it;
- an export fidelity change would alter Classic export semantics or need a DXF
  specification change not chosen in `DQ-E2-9`;
- licensing or provenance of an icon is unclear.

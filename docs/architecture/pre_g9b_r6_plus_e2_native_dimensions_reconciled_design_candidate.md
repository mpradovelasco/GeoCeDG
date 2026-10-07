# PRE-G9B-R6-plus-E2 — native dimensions: reconciled design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED AS A WHOLE.** The author
  decisions `DQ-E2-1` to `DQ-E2-11` of 2026-10-07 are recorded in the
  [E2 author-decision record](../validation/pre_g9b_r6_plus_e2_author_decisions_record.md),
  which is their authority and prevails over this file; the design below
  applies them. Details the decisions delegated to the preparation are marked
  *preparation resolution*.
- Produced by: the `PRE-G9B-R6-plus-E2` characterization and preparation
  (2026-10-07), revised by the author-decision freeze and the `DQ-E2-6` focal
  characterization of the same day; no implementation
- Bases: `P_R6PLUS_E1_P_X1` = `dc63b0e55f12eb96d3aea359db4476cbda6c89dc`
  (tree `8b2b5027a2119f996de8ca5af91b03ce32731a8c`); documentary base
  `T_R6PLUS_E2_PREP` = `6ad853c618da8f07a279b266c83002c145c6292c`
- Supersedes, where they differ: the `P0`
  [design candidate](pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md)
  (kept unchanged as `P0` evidence) and the stale facts of the
  [mini-track plan](pre_g9b_r6_plus_minitrack_plan.md) §8.5 listed in §11
- Evidence: [characterization report](../validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md)
  (`K1`–`K20`) and the
  [`DQ-E2-6` focal addendum](../validation/pre_g9b_r6_plus_e2_dq6_aligned_text_addendum.md)
  (`A1`–`A10`), with their JSON mirrors
- Execution contract when authorized: [canonical `E2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md)
- Normative inputs it implements, not restates: [unit-system specification](../../geocedg/specs/units/unit-system.md)
  §5, §6, §14, §17, §18.3; [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md)
  decisions 5–7, 10–12; [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md)
  decision 2; `AGENTS.md` §4 and §10

This file creates no normative contract; the implementing phase turns it into
the durable specification named in the prompt.

## 1. Placement

`AlignedDimension` and `LinearDimension` need DAG dependencies, dynamic
recomputation, persistence, command and script access and coherent consumption
by every frontend, so their **semantics live in the shared Java kernel**
(`AGENTS.md` §4). The Desktop frontend owns tool orchestration, initial
parameter capture, the drag gesture, product placement, icons and help. The
upstream `Dimension(<Object>)` (cardinality of lists, matrices, points and
vectors) is unrelated and is neither reused nor overloaded (`K2`).

## 2. Names (`DQ-E2-7`, `DQ-E2-10`, decided)

```text
canonical heads   AlignedDimension, LinearDimension
Spanish aliases   CotaAlineada, CotaLineal
never registered  DirectDimension, AxisDimension, directDimension, axisDimension
action ids        measure.aligned-dimension, measure.linear-dimension
modes             142, 143
placement         construction-metrics group; Construction menu
```

The legacy macro names stay in historical and user macro space; no macro is
renamed, deleted, migrated or reinterpreted. The new heads collide with no
command, alias, parser function, `Templatev7.ggb` macro or curated tool at the
base (`K3`, `K15`); coexistence is still validated in `E2`.

## 3. Representation and contract

### 3.1 Representation (`DQ-E2-1`, decided: β)

One shared-kernel algorithm per command whose outputs are existing object types.
No `GeoDimension` type; a new `GeoElement` type or XML element type is an
escalation condition (`DQ-E2-8`).

### 3.2 Signatures (`DQ-E2-2`, decided)

```text
AlignedDimension( <Point A>, <Point B>, <Offset> [, <Overshoot>, <Gap>] )
LinearDimension ( <Point A>, <Point B>, <Direction>, <Offset> [, <Overshoot>, <Gap>] )

Offset     mandatory, signed, model units
Overshoot  model units, ≥ 0, 0 when omitted
Gap        model units, ≥ 0, 0 when omitted
Direction  2D line (GeoLine, including segment, ray and axes) | 2D vector
```

Syntax lines (*preparation resolution*): `AlignedDimension` has two
(`[A, B, Offset]`, `[A, B, Offset, Overshoot, Gap]`); `LinearDimension` has four
(line or vector, each with and without `Overshoot, Gap`).

### 3.3 Arguments

| Argument | Meaning | Persistence | Visibility when created by the tool | Command input or tool state |
|---|---|---|---|---|
| `A`, `B` | measured points; their order fixes the normal of `AlignedDimension` (§7) | ordinary inputs | the user's points | command input |
| `Direction` | measurement axis of `LinearDimension` | ordinary input | the user's object | command input |
| `Offset` | signed distance of the dimension line along `n̂` (§4) | ordinary construction input, `DOCUMENT_SEMANTIC` | free hidden auxiliary `GeoNumeric` | command input; the tool captures it once from the placement click |
| `Overshoot` | extension beyond the dimension line | ordinary input | free hidden auxiliary `GeoNumeric` | command input; captured once by the tool (`DQ-E2-11`) |
| `Gap` | gap between a measured point and its extension line | ordinary input | as `Overshoot` | as `Overshoot` |

No argument, default or output reads `constructionUnit`, `presentationUnit`,
the `usm` factor, `drawingScale`, the viewport, the zoom or the DPI; there is no
unit multiplier; the command never reads the screen (`AGENTS.md` §10).

### 3.4 Output order (*preparation resolution*, frozen for XML and labels)

| # | Output | Type | Role |
|---|---|---|---|
| 0 | value | `GeoNumeric` | the stable model-unit measure `L` (semantic) |
| 1 | dimension line | `GeoSegment`, arrow endings at both ends | figure |
| 2 | extension line at `A` | `GeoSegment` | figure |
| 3 | extension line at `B` | `GeoSegment` | figure |
| 4 | presentation text | `GeoText`, plain value string, aligned by the §6 seam | presentation (`DERIVED_TRANSIENT` string) |

Each output keeps its own ordinary element style; deleting any output deletes
the algorithm and all outputs.

## 4. Geometry

### 4.1 Common frame

For a unit axis direction `d̂` let `n̂ = rot90(d̂) = (−d̂y, d̂x)`. The dimension line
is `{X : ⟨X − A, n̂⟩ = s}` for the offset `s`. For `P ∈ {A, B}`:

```text
t_P   = s − ⟨P − A, n̂⟩            signed distance from P to the dimension line
P'    = P + t_P · n̂               foot on the dimension line
σ_P   = sign(t_P)
ext_P = Segment(P + σ_P·g·n̂, P' + σ_P·o·n̂)     defined only when |t_P| > g
```

The dimension line is `Segment(A', B')`; the text anchor is `Midpoint(A', B')`.

### 4.2 `AlignedDimension`

`v = B − A`, `L = |v|`, `d̂ = v / L` (ordered pair `A → B`); `t_A = t_B = s`.

### 4.3 `LinearDimension`

`d̂ = dir / |dir|` with `dir` = `GeoLine.getDirection()` (line, segment, ray,
axis) or the vector coordinates; never `getDirectionInD3` (`DQ-E2-5`).
`L = |⟨B − A, d̂⟩|`; the sign of `d̂` never changes `L`.

### 4.4 Degeneracies (no tolerance, no silent approximation)

| Case | Value | Dimension line | Extension lines | Text |
|---|---|---|---|---|
| `A` or `B` undefined or non-finite | undefined | undefined | undefined | undefined |
| `A = B` (exact), aligned | `0` | undefined | undefined | undefined |
| `Direction` undefined, non-finite or zero | undefined | undefined | undefined | undefined |
| linear measure `L = 0` (exact) | `0` | undefined (zero length) | each by its rule | shows `0` at the foot |
| `s`, `o` or `g` non-finite, `o < 0` or `g < 0` | defined | undefined | undefined | undefined |
| `s = 0` | defined | defined | undefined | defined |
| `0 < |t_P| ≤ g` | defined | defined | `ext_P` undefined | defined |
| `UNSPECIFIED_MODEL_UNIT` | defined | defined | defined | bare value, no suffix |
| non-finite `display(L)` | defined | defined | defined | failure marker (§5) |

A partially defined dimension keeps every defined output valid; the auxiliary
inputs stay ordinary free numbers; an output that becomes undefined is set
undefined in the same computation.

## 5. Value and presentation string (`DQ-E2-3`, decided)

```text
numeric output 0 = L  (model units; never depends on units, drawingScale, viewport, zoom, DPI)
text string      = format(display(L)) + " " + suffix(effP)      physical effC
                 = format(L)                                    UNSPECIFIED_MODEL_UNIT
display(L)       = L · metresPerUnit(effC) / metresPerUnit(effP)
example          = cm / mm, L = 12  →  12 and "120 mm";  presentation m  →  12 and "0.12 m"
```

Seam (S1a, consistent with the decision): `compute()` computes geometry and the
value only and never reads `UnitState`; a separate presentation step writes
only the string of output 4, after `compute()`, at construction and from the
`D1` presentation listener on every unit change, with no geometric recompute
and no DAG input. The string holds the plain value, never LaTeX markup, is not
written to XML and is never parsed back. Failure marker for a non-finite
`display(L)` (*preparation resolution*, proposed): `?` followed by the suffix.
A dimension inside a user macro presents the bare value (macro unit state is
`EMPTY`), recorded as a known limitation.

## 6. Aligned dimension value (`DQ-E2-6`, author requirement; seam characterized)

Required: parallel to the dimension line; centred longitudinally; never upside
down; not crossed by the dimension or extension lines; follows the dimension
figure, not screen axes; zoom, DPI and viewport never semantic authority;
preserved by save and reopen; explicit validated export behavior.

Characterized seam (addendum `A7`, `A8`; C4, feasible with β):

1. The two algorithms implement a GeoCeDG kernel interface that exposes the
   dimension-line end points of their text output (model coordinates) and the
   overshoot side. No new geo type, no XML.
2. `EuclidianDraw` routes a text to a GeoCeDG `DrawDimensionText extends
   DrawText` only when its parent algorithm implements that interface; every
   other text keeps `DrawText`.
3. The drawable takes the screen angle of the projected dimension line, applies
   the reading rule `θ = φ − 180·⌈(φ − 90)/180⌉ ∈ (−90°, 90°]` (order-independent;
   vertical reads bottom to top), rotates about the screen anchor and lifts the
   value by half its height plus a fixed clearance along "text up"; when the
   projected value is longer than the dimension line the lift also clears the
   overshoot tips. Hit testing, highlight, background and `Corner` bounds use
   the rotated rectangle.
4. Nothing of this is stored: the orientation is re-derived from the geometry
   at every draw, so save, reopen, undo and redo reproduce it.

Measured (addendum `A5`): parallel within 2.4°, centred within 1.9 px, never
crossed, readable in every case, including reversed input order, zoom,
resizing, value width and suffix changes, both offset sides, anisotropic axes,
moves of `A` and `B`, an offset change, an XML round trip and a value change.
The reading direction, and the side on which the value sits, change by 180° at
the vertical; that is presentation only.

## 7. Pinned side (`DQ-E2-4`, decided: B1)

*Preparation resolution* of the minimal exact representation: no extra input.

```text
AlignedDimension  n̂ = rot90((B − A)/|B − A|)    left normal of the ordered pair A → B
LinearDimension   n̂ = rot90(d̂)                  d̂ from getDirection() or the vector
side              = sign(Offset) relative to n̂
```

Input order and `Offset` are explicit, serialized command inputs, so the side is
construction state. The normal is continuous in `A`, `B` and the direction, so
dragging geometry never flips the side; the only singular configurations are the
degeneracies of §4.4. The tool chooses the sign of the captured offset once,
from the placement click. No classification is re-evaluated after creation.

## 8. Interaction (Desktop; `DQ-E2-11`, decided)

- `AlignedDimension`: point, point, placement click. `LinearDimension`: point,
  point, line or vector, placement click.
- At the placement click `C`: `Offset = ⟨C − A, n̂⟩` in model units, stored at once
  as a free hidden auxiliary number.
- `Overshoot` and `Gap`: captured once; with a physical unit and the session
  `drawingScale`, from paper magnitudes (proposed 2 mm and 1 mm); with
  `UNSPECIFIED_MODEL_UNIT`, a view-derived fallback (proposed 8 px and 4 px)
  materialized at once in model units; no millimetre relation pretended.
  After creation no live viewport, DPI, `drawingScale` or geometric `UnitState`
  dependency.
- Drag: pressing on a dimension output in Move mode and dragging writes only the
  free offset number, `s := ⟨C − A, n̂⟩`, through a pure kernel helper; one undo
  point on release (GeoCeDG press/drag/release interception, `K13`). Nothing is
  dragged when the offset is not a free independent number. No route moves `A`
  or `B` through a dimension output.

## 9. Persistence, compatibility, export

- XML: additive `<command name="AlignedDimension">` / `"LinearDimension"` with
  existing output element types; arrow endings as element style; offset,
  overshoot and gap as ordinary numerics. No new element type or attribute; no
  reader change.
- Older readers (GeoCeDG before `E2`, Classic, the pinned baseline): the
  unknown command is reported as a load error, the rest loads, the outputs and
  every dependent are missing and lost on re-save (`K16`); stated in the guides.
- Export (`DQ-E2-9`, decided):
  - picture formats (PNG, SVG, PDF, EMF, print): the complete figure through
    the drawables, including the aligned value of §6;
  - PSTricks, PGF/TikZ, Asymptote: a bounded GeoCeDG adaptation, for dimension
    outputs only, emits the arrow endings, the rotation, the centred anchoring
    and the lift; other segments and texts keep their current output;
  - DXF: no `DIMENSION` entity; the existing path writes the dimension line and
    the two extension lines as `LINE` and excludes the value with its existing
    diagnostic; recorded as the durable limitation
    `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET`; no claim of DXF dimension semantics.

## 10. Verification class (`DQ-E2-8`, decided)

`INTEGRATED_PHASE`: registered `PHASE` plus `INTEGRATION`. The §6 renderer
seam is gated to dimension outputs and changes no serialization, reader, undo
snapshot or kernel semantics, so it stays within the class. A new `GeoElement`,
a new XML element type, materially new global serialization semantics, or a
change to how any non-dimension text is drawn, hit or exported → stop and
request a `GLOBAL_IMPACT` review.

## 11. Stale facts superseded

| Source | Stale statement | Current |
|---|---|---|
| `P0` candidate, plan §8.5 | heads `DirectDimension` / `AxisDimension` | `AlignedDimension` / `LinearDimension` (`DQ-E2-7`) |
| `P0` candidate §5, plan §8.5 | 115 actions; group at `application-profile.yml:3276-3280` | 125 actions at the base; `:3475-3480` |
| `P0` candidate §3 | optional offset | offset mandatory (`DQ-E2-2`) |
| `P0` candidate §3 | direction from `F1` | `E2`-local (`DQ-E2-5`) |
| `P0` candidate §3 | `A = B` undefined dimension | value `0`, figure undefined (§4.4) |
| `P0` candidate §3 | canonical side recomputed live | B1 pinned by explicit inputs (§7) |
| `P0` candidate §4 | parallel text not sustainable; horizontal text | aligned value through the §6 seam (`DQ-E2-6`) |
| `P0` candidate §4 | exporters work at once | picture yes; LaTeX by adaptation; DXF subset (§9) |
| `P0` candidate §6 | drag mechanism unspecified | Desktop interception (§8); `ChangeableParent` is 3D-only |
| first version of this file | `KNOWN LIMITATION — horizontal centred text` recommended | rejected by the author; superseded by §6 |

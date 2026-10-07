# PRE-G9B-R6-plus-E2 — native dimensions: reconciled design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**; every choice below
  that a `DQ-E2` row names stays **PROPOSED — PENDING AUTHOR DECISION**
- Produced by: the `PRE-G9B-R6-plus-E2` characterization and preparation
  (author instruction of 2026-10-07; characterization and preparation only, no
  implementation)
- Base: `P_R6PLUS_E1_P_X1` = `dc63b0e55f12eb96d3aea359db4476cbda6c89dc`, tree
  `8b2b5027a2119f996de8ca5af91b03ce32731a8c`
- Supersedes, where they differ: the `P0`
  [design candidate](pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md)
  (kept unchanged as `P0` evidence) and the stale facts of the
  [mini-track plan](pre_g9b_r6_plus_minitrack_plan.md) §8.5 listed in §10
- Evidence: [E2 preparation characterization report](../validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md)
  (sections `K1`–`K20`) and its JSON mirror
- Execution contract when authorized: [canonical `E2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md)
- Normative inputs it implements, not restates: [unit-system specification](../../geocedg/specs/units/unit-system.md)
  §5, §6, §14, §17, §18.3; [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md)
  decisions 5–7, 10–12; [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md)
  decision 2; `AGENTS.md` §4 and §10

This file is a design candidate. It creates no normative contract; the
implementing phase turns the author-approved parts into the durable
specification named in the prompt (*Required design/specification*).

## 1. Placement (frozen premise of the characterization)

`DirectDimension` and `AxisDimension` need DAG dependencies, dynamic
recomputation, persistence, command and script access and coherent consumption
by every frontend, so their **semantics live in the shared Java kernel**
(`AGENTS.md` §4). The Desktop frontend owns only interactive tool orchestration,
initial parameter capture, the drag gesture, product placement, icons and help.
No geometry is computed in a Desktop helper, an exporter or a script.

The existing upstream command `Dimension(<Object>)` is unrelated: it returns the
cardinality of a list, `{rows, cols}` of a matrix, and 2 or 3 for a point or
vector (report `K2`). It is neither reused nor overloaded.

## 2. Command names (`DQ-E2-2`, `DQ-E2-7`, `DQ-E2-10`)

The default candidates stay `DirectDimension` and `AxisDimension`. They are free
in every command table, bundle and parser-function table at the base (`K3`), but
they collide case-insensitively with the legacy macros `directDimension` and
`axisDimension`, which every `Templatev7.ggb`-derived document embeds and which
users may hold in their personal tool store (`K15`). Because macros are resolved
**before** native commands and case-insensitively, keeping these names makes the
native commands unreachable in such documents and makes a stored legacy package
disable the whole user library. The collision-free alternative pair
`AlignedDimension` / `LinearDimension` (ES `CotaAlineada` / `CotaLineal`) is
also free at the base. The choice is `DQ-E2-7`; the rest of this file is written
with the default names, and every statement holds unchanged for the alternative
pair.

## 3. Representation (`DQ-E2-1`)

**Recommended: β — one shared-kernel algorithm per command whose outputs are
existing object types.** α (a composite `GeoDimension` with its own drawable,
XML element type and export adapters) is the documented alternative; it is
`GLOBAL_IMPACT`. The A-versus-B table is in the prompt (`DQ-E2-1`) and the
evidence in `K10`–`K12`.

### 3.1 Signatures (`DQ-E2-2`)

Recommended (offset mandatory; scale-free defaults for the rest):

```text
DirectDimension( <Point A>, <Point B>, <Offset> [, <Overshoot>, <Gap>] )
AxisDimension  ( <Point A>, <Point B>, <Direction>, <Offset> [, <Overshoot>, <Gap>] )

Direction  = 2D line (GeoLine, including segment, ray and the axes) | 2D vector
Overshoot  = 0 and Gap = 0 when omitted (scale-free constants, no hidden factor)
```

The alternative keeps the 2-argument `DirectDimension(A, B)` and the
3-argument `AxisDimension(A, B, Direction)` with `Offset = 0`; it is the only
scale-free default for the offset and it yields a dimension line on `AB` with no
extension lines (§4.4).

### 3.2 Arguments

| Argument | Type | Meaning | Default | Persistence | Dependency | Visibility | Command input or tool state |
|---|---|---|---|---|---|---|---|
| `A`, `B` | 2D point | measured points | — | ordinary inputs | DAG input | as created by the user | command input |
| `Direction` | 2D line or 2D vector | measurement axis of `AxisDimension` | — | ordinary input | DAG input | the user's object | command input |
| `Offset` | number, model units, signed | distance from the measured feature to the dimension line along `n̂` (§4) | none (recommended) or `0` (alternative) | ordinary construction input; `DOCUMENT_SEMANTIC` | DAG input | a free hidden auxiliary `GeoNumeric` when created by the tool | command input; the tool captures its initial value once from the placement click |
| `Overshoot` | number, model units, `≥ 0` | extension-line extension beyond the dimension line | `0` | ordinary input | DAG input | free hidden auxiliary `GeoNumeric` when created by the tool | command input; the tool captures its value once (`DQ-E2-11`) |
| `Gap` | number, model units, `≥ 0` | gap between the measured point and the start of the extension line | `0` | ordinary input | DAG input | as `Overshoot` | as `Overshoot` |

No argument, default or output reads `constructionUnit`, `presentationUnit`,
the `usm` factor, `drawingScale`, the viewport, the zoom or the DPI. No input is
a unit multiplier. The offset never derives from the screen inside the command
(`AGENTS.md` §10).

### 3.3 Output contract (order fixed for XML and labels)

| # | Output | Type | Role | Visible by default |
|---|---|---|---|---|
| 0 | value | `GeoNumeric` | the stable model-unit measure `L` (semantic) | Algebra View; not drawn |
| 1 | dimension line | `GeoSegment` with start and end arrow endings | figure | yes |
| 2 | extension line at `A` | `GeoSegment` | figure | yes |
| 3 | extension line at `B` | `GeoSegment` | figure | yes |
| 4 | presentation text | `GeoText`, horizontal, centred | presentation (`DERIVED_TRANSIENT` string) | yes |

All five are children of the one algorithm; it alone owns their dependencies.
Each output keeps its own ordinary element style (colour, thickness, line type,
arrow endings, font, layer), so style ownership, layers, hiding and hit testing
are the existing ones. Deleting any output deletes the algorithm and all
outputs, as for every multi-output command.

## 4. Geometry

### 4.1 Common frame

For an axis direction `d̂` (unit vector) let `n̂ = rot90(d̂) = (−d̂y, d̂x)`, the left
normal. The dimension line is the line `{X : ⟨X − A, n̂⟩ = s}` where `s` is the
offset. For a measured point `P ∈ {A, B}`:

```text
t_P  = s − ⟨P − A, n̂⟩          signed distance from P to the dimension line
P'   = P + t_P · n̂              foot on the dimension line
σ_P  = sign(t_P)
ext_P = Segment(P + σ_P·g·n̂,  P' + σ_P·o·n̂)     defined only when |t_P| > g
```

The dimension line is `Segment(A', B')` with arrow endings at both ends. The text
anchor is `Midpoint(A', B')`; the text is horizontal, horizontally centred, and
placed above the anchor by the existing screen-space text alignment (a
presentation rule of the drawable, never authoritative state; `K9`).

### 4.2 `DirectDimension`

`v = B − A`, `L = |v|`, `d̂ = v / L` (input order `A → B`). Then `t_A = t_B = s`,
`A' = A + s·n̂`, `B' = B + s·n̂`. A positive offset places the dimension line on
the left of `A → B`.

### 4.3 `AxisDimension`

`d̂ = dir / |dir|` with `dir` the **kernel direction of the input** (§5):
`GeoLine.getDirection()` = `(y, −x)` of the line coefficients for a line, segment,
ray or axis; the coordinates for a vector. `L = |⟨B − A, d̂⟩|`. The sign of `d̂`
never changes `L`; it only fixes which side a positive offset denotes (left of
`d̂`: above for `xAxis`, left for `yAxis`; `K7`).

### 4.4 Degeneracies (no tolerance, no silent approximation)

| Case | Value (output 0) | Dimension line | Extension lines | Text |
|---|---|---|---|---|
| `A` or `B` undefined or non-finite | undefined | undefined | undefined | undefined |
| `A = B` (exact binary64 equality), direct | defined, `0` | undefined (no direction) | undefined | undefined |
| `Direction` undefined, non-finite or zero (`|dir| = 0`) | undefined | undefined | undefined | undefined |
| axis measure `L = 0` (exact), including `A = B` | defined, `0` | undefined (zero length) | each by its own rule | defined, shows the zero value at the foot |
| `s`, `o` or `g` non-finite, or `o < 0`, or `g < 0` | defined (does not depend on them) | undefined | undefined | undefined |
| `s = 0` | defined | defined (on the measured feature) | undefined (`|t_P| = 0 ≤ g`) | defined |
| `0 < |t_P| ≤ g` | defined | defined | `ext_P` undefined, the other by its rule | defined |
| unit state `UNSPECIFIED_MODEL_UNIT` | defined | defined | defined | bare value, no suffix (§6) |
| unit conversion not finite | defined | defined | defined | presentation failure marker (`DQ-E2-3`) |

A partially defined dimension keeps every defined output valid; auxiliary
inputs (offset, overshoot, gap) stay ordinary free numbers in every state. No
case retains stale geometry: an output that becomes undefined is set undefined
in the same computation.

## 5. Direction authority for `AxisDimension` (`DQ-E2-5`)

`E2` executes before `F1` and must not depend on it. The minimal `E2`-local
contract is: **the measurement axis is the unoriented line spanned by the
input's kernel direction; the placement side uses that direction's existing
kernel orientation.** At the base (`K7`, probe P1/P3):

| Construction | `getDirection` | `getDirectionInD3` |
|---|---|---|
| `Line(A,B)`, `Segment(A,B)`, `Ray(A,B)` | `B − A` | `B − A` |
| parallel `Line(P, l)` | as `l` | **opposite** |
| `PerpendicularLine(P, l)` | `l` rotated +90° | **opposite** |
| `Line(P, v)`, `Ray(P, v)` | `v` | **opposite** |
| `xAxis`, `yAxis` | `(1, 0)`, `(0, 1)` | same |

`E2` uses `getDirection` (or the vector coordinates) only and never
`getDirectionInD3`. It adds no user-visible orientation cue, no orientation
option and no accessor; `F1` keeps that scope.

## 6. Presentation text and units (`DQ-E2-3`)

```text
numeric value (output 0)     = L, model units — stable, never unit-dependent
presentation string (output 4) = format(display(L)) + " " + suffix(effP)   physical effC
                               = format(L)                                UNSPECIFIED
display(L) = L · fb(effC) / fb(effP)                (unit-system §5.3, UnitState.display)
```

`format` is the text's ordinary kernel number formatting (global rounding or the
text's own decimals). The unit system adds no rounding rule and the text is
never parsed back.

**Recommended seam (S1a).** The algorithm separates two steps:

1. `compute()` — geometry and the value only; a pure function of the geometric
   inputs; it never reads the unit state;
2. a presentation step that reads `cons.getUnitSystem().getState()` and the
   kernel number format and writes **only** the string of output 4.

The presentation step runs after `compute()` in the algorithm's ordinary update
and at construction, and on every unit-state change through the existing
`DocumentUnitSystem` presentation listener (`K8`), which iterates the
construction's dimension algorithms, runs only their presentation step and
notifies the text outputs. No geometry is recomputed, no DAG input is added, and
the unit state never becomes a dependency of any geometric output. This is the
only unit-state reader `unit-system.md` §6.2 item 1 and §14.2 permit; it keeps
§6.1 literally (no read inside `compute()`, no recompute because of a unit
change). The text string is not written to XML (dependent text), so a unit
change changes no construction XML except `geocedgUnits`.

Consequences (recorded, not hidden): a dimension inside a user macro computes in
a macro construction whose unit state is always `EMPTY` (`unit-system.md` §8.8),
so it presents the bare value; a script or object that explicitly reads the text
sees the new string after a unit change (a presentation dependency, §6.2).

## 7. Deterministic side (`DQ-E2-4`)

The `P0` rule (canonical `t̂` flipped to `+x` when `|tx| ≥ |ty|`, else to `+y`)
is order-independent but **discontinuous at `|tx| = |ty|`**: dragging `A` or `B`
across 45° moves a positive-offset dimension line to the other side, and the
exact tie needs its own rule.

Recommended (**B1 — pinned by explicit inputs**): the geometry uses the input
order (`DirectDimension`) or the kernel direction (`AxisDimension`) and the
signed offset; it is continuous for every non-degenerate configuration and
never flips. The tool applies the canonical above/left rule **once, at
creation**, by choosing the sign of the captured offset (or the placement click
fixes it). Alternatives: A (live canonical rule, documented flip) and B2 (an
extra explicit reference-axis input; its flip moves to 90° from the pin).

## 8. Interaction (Desktop)

- Tool flow (`DQ-E2-11`): `DirectDimension` = point, point, placement click;
  `AxisDimension` = point, point, line or vector, placement click. Existing
  points are selected or new free points created by the host rules.
- At the placement click `C` the tool computes once, in model units, the signed
  offset `s = ⟨C − A, n̂⟩` with the `n̂` of §4.2 or §4.3, and creates three free
  hidden auxiliary numbers (offset, overshoot, gap) whose values are then
  ordinary construction parameters.
- Drag: pressing on output 1 (or 2–4) in Move mode and dragging writes exactly
  one value, the free offset number, `s := ⟨C − A, n̂⟩` for the current pointer
  `C`, using a kernel helper of the algorithm (pure geometry); one undo point
  on release, through the existing GeoCeDG press/drag/release interception
  (`K13`). When the offset is not a free independent number nothing is dragged.
- No route — mouse, arrow keys or multi-selection — may move `A` or `B`
  through a dimension output: the inherited dependent-move rule would translate
  the free measured points (`K13`).

## 9. Persistence, compatibility, export

- XML: an additive `<command name="DirectDimension">` (or `AxisDimension`) with
  existing output element types; arrow endings are ordinary element style
  (`startStyle`/`endStyle`); the offset, overshoot and gap are ordinary free
  numerics. No new element type, no new attribute, no change to any reader.
- Older readers (GeoCeDG before `E2`, Classic, the pinned upstream baseline):
  the unknown command is reported as a load error, the rest of the document
  loads, and the command's outputs **and every object that depends on them** are
  missing; a re-save loses them (`K16`, probe P5). This is the expected boundary
  of any new command and must be stated in the guides.
- Export (`K12`): picture formats (PNG, SVG, PDF, EMF, print) draw the complete
  figure through the shared drawables. PSTricks, PGF/TikZ and Asymptote draw the
  segments but drop the arrow endings and place the text top-left at its anchor
  (no centring). DXF writes the three segments only: arrowheads are absent and
  the text is excluded from the geometric population with a non-blocking
  diagnostic (a selection that includes the text is not writable). `DQ-E2-9`
  decides whether `E2` closes these gaps.

## 10. Stale facts superseded

| Source | Stale statement | Current base |
|---|---|---|
| `P0` candidate §5, plan §8.5 | catalog pin 115 actions; group at `application-profile.yml:3276-3280` | 125 actions (`GeoCeDGProfile.java:340-345` and eight test pins); `construction-metrics` at `:3475-3480` |
| `P0` candidate §3 | direction "from the orientation authority of `F1`" | `E2`-local contract of §5; no `F1` dependency |
| `P0` candidate §3 | `A = B` gives an undefined dimension | value `0` defined, figure undefined (§4.4; `DQ-E2-2`) |
| `P0` candidate §3 | positive offset canonical side recomputed live | B1 pinned by explicit inputs (§7; `DQ-E2-4`) |
| `P0` candidate §4 | exporters "work at once" for segments and text | picture only; LaTeX and DXF lose arrow endings, text centring and DXF text (§9) |
| `P0` candidate §4 | the text "updates when either unit changes (`D1`)" through the kernel graph | presentation step outside `compute()` driven by the `D1` listener (§6) |
| `P0` candidate §6 | a drag edits the offset (mechanism unspecified) | no 2D kernel mechanism exists (`ChangeableParent` is 3D-only); Desktop interception (§8) |
| plan §8.5, `P0` §5 | `X.Tool` / `X.Help` keys | confirmed for `upstream-mode` actions; status and error text need tool-owned keys (prompt) |
| `E1` observation | collision affects the user library only | it also affects every document that embeds the legacy macros, and the library fails as a whole (§2) |

# GeoCeDG native dimensions

| Field | Value |
|---|---|
| Status | **PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW**; not author approved |
| Version | `1.1` (revised candidate of the author smoke follow-up, [record](../../../docs/validation/pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md): §4.2, §6, §8.5, §9.1); `1.0` is the text of the original candidate `878ff66b` |
| Owner phase | `PRE-G9B-R6-plus-E2` (native dimensions) |
| Decision | [ADR 0034](../../../docs/adr/0034-native-dimension-representation-and-presentation-seams.md) (`PROPOSED`) |
| Governing author decisions | [E2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_e2_author_decisions_record.md) (`DQ-E2-1` to `DQ-E2-11`); [E2 authorization record](../../../docs/validation/pre_g9b_r6_plus_e2_authorization_record.md) |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §5, §6, §14, §17, §18.3; [ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md); [DXF curve fidelity](../export/dxf-curve-fidelity-and-approximation.md) |
| Serialization effect | additive `<command>` names with existing output element types; no new element type, attribute or reader change |

## 0. Status and normative language

This specification is the durable contract of the `E2` technical candidate. It
becomes normative only through an author closeout record; until then it binds
the candidate and nothing else. `MUST`, `MUST NOT`, `SHOULD` and `MAY` have
their usual meaning. Where an author decision and this text differ, the
decision prevails and this text is defective.

## 1. Scope and placement

1.1 Two commands measure a straight distance between two points and draw an
engineering dimension: `AlignedDimension` (the true distance) and
`LinearDimension` (the distance projected on a direction).

1.2 The semantics live in the shared kernel (`AGENTS.md` §4): command identity,
algorithms, geometry, degeneracies, value, presentation step and the
text-geometry interface. The shared renderer owns one gated drawable for the
value text. The Desktop frontend owns tools, capture of the initial parameters,
the drag gesture, profile placement, icons and help. The export adapters own the
gated LaTeX output. No dimension semantics live in a drawable or an exporter;
both read the algorithm's geometry.

1.3 The upstream `Dimension(<Object>)` command (cardinality of lists, matrices,
points and vectors) is unrelated, unchanged and never overloaded.

## 2. Names

| Item | Value |
|---|---|
| canonical heads | `AlignedDimension`, `LinearDimension` (`TABLE_GEOMETRY`) |
| Spanish aliases | `CotaAlineada`, `CotaLineal` |
| never registered | `DirectDimension`, `AxisDimension`, `directDimension`, `axisDimension` |
| action ids | `measure.aligned-dimension`, `measure.linear-dimension` |
| modes | `142` (`MODE_ALIGNED_DIMENSION`), `143` (`MODE_LINEAR_DIMENSION`) |
| placement | construction-metrics group, after Distance or Length; Construction menu |

2.1 Legacy macros named `directDimension` or `axisDimension` (in
`Templatev7.ggb` documents or user stores) stay in user macro space; they are
never renamed, deleted, migrated or reinterpreted. Macro-versus-native dispatch
precedence is unchanged.

## 3. Signatures and arguments

```text
AlignedDimension( <Point>, <Point>, <Offset> )
AlignedDimension( <Point>, <Point>, <Offset>, <Overshoot>, <Gap> )
LinearDimension( <Point>, <Point>, <Line>, <Offset> )
LinearDimension( <Point>, <Point>, <Vector>, <Offset> )
LinearDimension( <Point>, <Point>, <Line>, <Offset>, <Overshoot>, <Gap> )
LinearDimension( <Point>, <Point>, <Vector>, <Offset>, <Overshoot>, <Gap> )
```

3.1 `A`, `B` are 2D points; `Line` is any 2D `GeoLine` (lines, segments, rays,
axes); `Vector` is a 2D vector. `Offset` is mandatory and signed; `Overshoot`
and `Gap` are both present or both omitted and are `0` when omitted. Every
length argument is in model units. Any other count or type is an argument
error.

3.2 No argument, default or output reads `constructionUnit`,
`presentationUnit`, the `usm` factor, `drawingScale`, the viewport, the zoom or
the DPI; there is no unit multiplier and the command never reads the screen.

## 4. Outputs

| # | Output | Type | Role |
|---|---|---|---|
| 0 | value | `GeoNumeric` | the model-unit measure `L` (semantic) |
| 1 | dimension line | `GeoSegment`, filled arrow endings at both ends | figure |
| 2 | extension line at `A` | `GeoSegment` | figure |
| 3 | extension line at `B` | `GeoSegment` | figure |
| 4 | value text | `GeoText`, plain value string | presentation |

4.1 The order is part of the command contract (XML outputs and labels). A
single label given to the command names the value and the other outputs take
default labels; several labels (XML) name the outputs in order. The labels of
outputs 1 to 3 are hidden. Each output keeps its ordinary element style;
deleting any output deletes the algorithm and all its outputs.

4.2 Initial line thickness (`1.1`). At creation, outputs 1 to 3 receive the
GeoGebra line thickness `2`, like the arrow endings a presentation default of
the algorithm; the tools then apply the creation preference of §8.5. Afterwards
each segment keeps and serializes its own style, which a load restores over the
default; no argument, geometric rule or dependency is involved.

## 5. Geometry

5.1 Common frame. For a unit direction `d̂` let `n̂ = rot90(d̂) = (−d̂y, d̂x)`.
The dimension line is `{X : ⟨X − A, n̂⟩ = s}` for the offset `s`. For
`P ∈ {A, B}`:

```text
t_P   = s − ⟨P − A, n̂⟩             signed distance from P to the dimension line
P'    = P + t_P · n̂                foot on the dimension line
σ_P   = sign(t_P)
ext_P = Segment(P + σ_P·g·n̂, P' + σ_P·o·n̂)      defined only when |t_P| > g
```

The dimension line is `Segment(A', B')`; the text anchor is the midpoint of
`A'B'`.

5.2 `AlignedDimension`: `v = B − A`, `L = |v|`, `d̂ = v / L` for the ordered
pair `A → B`.

5.3 `LinearDimension`: `d̂ = dir / |dir|` with `dir` = `GeoLine.getDirection()`
or the vector coordinates, never `getDirectionInD3`; `L = |⟨B − A, d̂⟩|`; the
sign of `d̂` never changes `L`.

5.4 Side. `n̂` is the left normal of `A → B` (aligned) or of `d̂` (linear); the
side is `sign(Offset)` relative to `n̂`. Input order and `Offset` are explicit
serialized inputs, so the side is construction state; the normal is continuous
in the inputs, so continuous motion never flips the side. No classification is
re-evaluated after creation.

5.5 Degeneracies (exact tests, no tolerance, no silent approximation):

| Case | Value | Dimension line | Extension lines | Text |
|---|---|---|---|---|
| `A` or `B` undefined or non-finite | undefined | undefined | undefined | undefined |
| `A = B`, aligned | `0` | undefined | undefined | undefined |
| direction undefined, non-finite or zero | undefined | undefined | undefined | undefined |
| linear `L = 0` | `0` | undefined | each by its rule | `0` at the foot |
| `s`, `o` or `g` non-finite, `o < 0` or `g < 0` | defined | undefined | undefined | undefined |
| `s = 0` | defined | defined | undefined | defined |
| `0 < |t_P| ≤ g` | defined | defined | `ext_P` undefined | defined |

An output that becomes undefined is set undefined in the same computation; no
stale geometry remains.

## 6. Value and presentation string

```text
output 0     = L                                        (model units)
text string  = format(display(L)) + " " + suffix(effP)   physical construction unit
             = format(L)                                 UNSPECIFIED_MODEL_UNIT
             = "? " + suffix(effP)                       display(L) not finite
```

6.1 `display`, `suffix` and the effective units are those of the unit-system
specification §5, §6 and §14; `format` is the kernel number format of the text.
Example: construction unit `cm`, presentation unit `mm`, `L = 12` → value `12`,
text `120 mm`; presentation unit `m` → value `12`, text `0.12 m`.

6.4 Unit suffix policy (`1.1`). The strings above are those of the shown
policy. With the document's hidden policy (unit-system §20.2) the
`" " + suffix(effP)` part is omitted: `120`, and `?` when not finite; the value
and the unspecified case are unchanged. A policy change runs only the
presentation step of §6.2.

6.2 Seam. `compute()` computes geometry and the value only and MUST NOT read the
unit state. A separate presentation step writes only the string of output 4:
after `compute()`, at construction, and from one keyed unit-state listener per
construction on every unit change, with no geometric recompute and no DAG input.
The string is the plain value, never LaTeX markup; it is not written as text
content to XML and is never parsed back.

6.3 A dimension inside a user macro presents the bare value (the macro
construction's unit state is empty); known limitation.

## 7. Aligned value (presentation only)

7.1 The value is drawn parallel to the projected dimension line, centred on it,
never upside down and not crossed by the dimension or extension lines. It
follows the drawn figure, not the screen axes.

7.2 The algorithms implement a kernel interface that exposes the text frame
(anchor and line direction in model coordinates), the dimension-line end
points, the extension tips and the overshoot direction. `EuclidianDraw` routes a
text to `DrawDimensionText` only when its parent algorithm implements that
interface and the text is its value text; every other text keeps `DrawText`.

7.3 `DrawDimensionText` takes the screen angle `φ` of the projected line and the
reading angle `θ = φ − 180·⌈(φ − 90)/180⌉ ∈ (−90°, 90°]` (order-independent; a
vertical line reads bottom to top). It paints an ordinary `DrawText` of the same
text inside a rotation about the placement centre, lifted along "text up" by
half the text height plus 2 px. When the text plus 4 px at each end is longer
than the projected dimension line, the value is placed beyond the extension tips
on the overshoot side. Hit testing, highlight and selection use the rotated
rectangle; the text's bounding box (used by `Corner`) is the axis-aligned
bounds of the rotated value.

7.4 Nothing of §7 is stored: angle, lift and pixel box are re-derived from the
geometry and the current view at every update, so save, reopen, undo and redo
reproduce them. The reading direction changes by 180° at the vertical; that is
presentation only.

## 8. Desktop interaction

8.1 Aligned Dimension tool: point, point, placement click. Linear Dimension
tool: point, point, line or vector, placement click. Points can be created by
the clicks.

8.2 At the placement click `C`, `Offset = ⟨C − A, n̂⟩` in model units. The tool
creates `Offset`, `Overshoot` and `Gap` as free, hidden, labelled numbers and
then the command; one undo point per creation. A placement that yields no finite
offset creates nothing and shows the invalid-placement notice.

8.3 `Overshoot` and `Gap` are captured once: with a physical construction unit
and a drawing scale, from 2 mm and 1 mm on paper; otherwise from 8 px and 4 px
of the current view, materialized at once in model units. After creation no
viewport, DPI, drawing-scale or unit dependency remains.

8.4 Drag: in Move mode, pressing on a dimension output (not on a point) and
dragging writes only the free offset number, `s := ⟨C − A, n̂⟩`; one undo point
on release. Nothing is dragged when the offset is not an independent, labelled,
unlocked number. No route moves `A` or `B` through a dimension output.

8.5 Creation preferences (`1.1`). Preferences → Layout & Presentation →
Dimension presentation holds, in the GeoCeDG properties store, the initial line
thickness of the three segments of dimensions created with the tools
(`geocedg.dimensions.initial-line-thickness.v1`, `1` to the GeoGebra maximum,
default `2`; an invalid value means the default) and the unit suffix policy of
new documents (unit-system §20.5). A preference change affects only dimensions
or documents created afterwards; it is not a document operation. The unit suffix
policy of the current document is set in Options → Document units.

## 9. Persistence and compatibility

9.1 XML: `<command name="AlignedDimension">` or `"LinearDimension"` with
existing output element types; arrow endings as element style; offset,
overshoot and gap as ordinary numerics. Documents without dimensions re-save
byte-identically. (`1.1`) The segment thickness is ordinary element style; the
suffix policy belongs to the unit state (unit-system §20.3), not to the
dimension.

9.2 Save and reopen, undo and redo, copy and paste (with the hidden inputs) and
redefine preserve value, geometry, style and the derived orientation.

9.3 Older readers (GeoCeDG before `E2`, Classic, the pinned baseline) report a
load error naming each dimension command and keep loading. The outputs survive
as independent objects: the value as a fixed number, the dimension and extension
lines and the text undefined; objects that depend on them become undefined. A
re-save from such a reader drops both commands.

## 10. Export

10.1 Picture formats (PNG, SVG, PDF, EMF, print) draw the complete figure
through the drawables, including the aligned value.

10.2 PSTricks, PGF/TikZ and Asymptote: for dimension outputs only, the dimension
line carries arrow endings at both ends and the value is written rotated by the
reading angle derived from the export units, centred on the dimension line and
lifted to its reading-up side. Every other segment and text keeps the host
output.

10.3 DXF: no `DIMENSION` entity. The existing path writes the dimension line and
the two extension lines as `LINE`; the value numerics and the text are excluded
with the existing `OUTSIDE_GEOMETRIC_POPULATION` diagnostics. Durable
limitation `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET`; no claim of DXF dimension
semantics.

## 11. Known limitations

| Limitation | Effect |
|---|---|
| DXF segment subset | no DXF dimension semantics, text or arrowheads |
| dimension inside a user macro | bare value without unit suffix |
| very short dimension line | the two arrowheads overlap; the value is lifted beyond the extension-line tips (§7.3) |
| `Corner` of the value text | axis-aligned bounds of the rotated value, not its rotated corners |
| older readers | §9.3 |

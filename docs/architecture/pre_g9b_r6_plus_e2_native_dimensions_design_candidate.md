# PRE-G9B-R6-plus-E2 — native `DirectDimension` and `AxisDimension`: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `E2` needs its own
  explicit author authorization, after `D1` (and `E1` for tool icons).
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  §6, finding 14 and probe P5

Path abbreviations as in the P0 report.

## 1. Author-fixed scope

Native `DirectDimension` and `AxisDimension` in the Distance group and the
Construction menu, with:

- help and tips;
- no hidden unit multiplier;
- construction- and presentation-unit integration;
- a traceable placement and a user-controlled dimension-line position;
- no far-away line;
- a centred figure;
- a deterministic above/left side;
- text parallel to the line where sustainably supported.

## 2. Behavioral reference: the legacy tools (P0 §6)

| Legacy behavior | Cause (macro construction) | Native candidate |
|---|---|---|
| value `kconversion·|DE|` | user input `kconversion` (template value 10) multiplies `Distance(D,E)` | numeric value = the model-unit `Distance`; only the **text** shows `Distance × conversion(constructionUnit → presentationUnit)`; no multiplier input |
| dimension line about `1.02·|AB|` from `AB` (direct); guide `±10·|AB|` (axis) | placement point `D = Point[g]` on the line orthogonal to `AB` (direct) or on segment `r = Segment[A ± 10·|AB|·û]` (axis); the offset scales with `|AB|` | explicit signed offset in model units, independent of `|AB|` |
| visible infinite guide line `g` (direct) / visible `20·|AB|` guide segment `r` (axis) | macro outputs, visible | no guide output |
| extension overshoot `|AD|/10` | `F = D + (|AD|/10)·û` | explicit overshoot and gap in model units, chosen by the tool at creation (`AQ-E4`) |
| arrows from two opposed vectors `u = E→D`, `v = D→E` | vectors drawn over the dimension line | one segment with arrow endings (`SegmentStyle` ARROW / ARROW_OUTLINE; `common/kernel/geos/SegmentStyle.java:24-26`) |
| horizontal text at `Midpoint(D,E)` plus a fixed **pixel** `labelOffset` (−55,1 / −73,−5) | text start point plus a screen offset, so the gap changes with zoom | centred text anchored in model coordinates |

## 3. Command semantics (kernel)

```text
DirectDimension( <Point A>, <Point B> [, <Offset> [, <Overshoot>, <Gap>]] )
AxisDimension  ( <Point A>, <Point B>, <Direction (line | vector)> [, <Offset> [, <Overshoot>, <Gap>]] )
```

- **Measured value:** direct, `|B − A|`; axis, `|⟨B − A, d̂⟩|`, with `d̂` the
  unit direction of the line (or vector) from the orientation authority of `F1`
  (`GeoLine.getDirection`). The sign of `d̂` does not change the value. The
  numeric output **is this model-unit measure**. It never carries a unit
  conversion, so a construction that uses it (for example as a radius) is
  unaffected by `presentationUnit` (`AQ-U1`) and by `constructionUnit` (plan
  §4.1).
- **Presentation (text only):** the text output shows
  value × `conversion(c → p)` with the `p` suffix; with an unspecified unit,
  the bare value and no suffix. Never a hidden multiplier. The text updates
  when either unit changes (`D1`). It is the only unit-dependent output.
- **Unit and scale independence of geometry.** No geometric output reads
  `constructionUnit`, `presentationUnit` or an export `drawingScale` live
  (D0 inputs §4a). The extension overshoot and the gap are two further
  explicit model-unit inputs (free, hidden numbers). The tool chooses them at
  creation, from the view or from a drawing scale and unit captured at that
  moment (`AQ-E4`). Afterwards they are ordinary construction parameters.
- **Placement parameter (`DOCUMENT_SEMANTIC`):** a signed offset, in model
  units, of the dimension line from the measured points, along the canonical
  normal `n̂` (below). It is a construction input: a free, by default hidden,
  `GeoNumeric` that the tool creates, so it is labelled, undoable, traceable
  and draggable. The command never reads screen scale (`AGENTS.md` §10). The
  **tool** picks the initial value from the view at creation; afterwards it is
  an ordinary construction parameter.
- **Deterministic side.** Canonical direction `t̂`: `B − A` normalized, then
  flipped so that it points to +x when `|t_x| ≥ |t_y|`, else to +y. Canonical
  normal `n̂ = rot90(t̂)`. A positive offset places the line **above**
  near-horizontal measures and **left of** near-vertical ones, independent of
  input order.
- **Degeneracies:**
  - `A = B` or a zero direction gives an undefined dimension.
  - Non-finite inputs give undefined.
  - An undefined unit conversion gives the bare value in the text.
  - An axis dimension whose measured projection is near zero stays defined
    (value 0), with the line drawn at the offset.
  - **Side-rule discontinuity:** at `|t_x| = |t_y|` the canonical axis changes,
    so dragging `A` or `B` across 45° flips the side of a positive offset.
    Options (`AQ-E5`): accept the documented flip, or let the tool pin the
    canonical axis at creation as one more explicit input. Recommended: pin it.

## 4. Representation (`AQ-E1`)

| | β — composition of existing outputs (recommended for this generation) | α — composite `GeoDimension` class with its own drawable |
|---|---|---|
| outputs | two extension segments, one dimension segment with arrow endings, a model-unit value `GeoNumeric` and a presentation `GeoText`, all children of one algorithm | one object |
| exporters | work at once (segments, text) in picture, LaTeX and DXF | new adapters for picture, LaTeX and DXF |
| XML | additive `<command>` element with existing output types | a new element type |
| centred text | horizontal centring through existing `Text` alignment options (`common/kernel/commands/CmdText.java:86-131`) | full |
| parallel text | **not sustainably supported**: rotated text exists only as LaTeX `\rotatebox` from `AlgoRotateText`, whose result has **no start point**, and `Rotate(text, angle, point)` ignores the point (probe P5; `common/kernel/advanced/AlgoRotateText.java:97-132`; `common/kernel/advanced/CmdRotateText.java:50-55`); centring rotated text needs screen metrics the kernel does not have | supported by the drawable |
| dragging the line | a GeoCeDG controller drag handler maps a drag of the dimension segment to the offset number (frontend orchestration writing one construction parameter) | the drawable's own handle |
| verification | `INTEGRATED_PHASE` | `GLOBAL_IMPACT` |

Recommendation: **β**, with horizontal centred text and parallel text recorded
as a known limitation. α remains the route if the author requires parallel
text.

## 5. Placement in the product

- Profile actions `measure.direct-dimension` and `measure.axis-dimension` in
  `construction-metrics` (the Distance toolbar group,
  `apps/geocedg/application-profile.yml:3276-3280`) and in the Construction
  menu. Tool names, help and tips in English and Spanish through `X.Tool` /
  `X.Help` keys.
- ADR 0031 canonical-head and complete-inventory gates (English canonical name,
  Spanish aliases).
- The `PRE-G9B-R6` GGBScript capability matrix gains rows for both commands on
  Algebra Input, GGBScript and nested `Execute` in English and Spanish; its
  completeness gate fails closed otherwise.
- Catalog pin 115 → +2 (together with `A`'s +1 and `E3`'s +2); its tests change
  in the same subphase.
- Tool icons are owned SVGs (`E1` pipeline; frontend-scoped dependency
  `E1 → E2`).

## 6. Dependency graph (finding 14)

Today a legacy dimension is one `AlgoMacro` parent of all copied outputs
(`common/kernel/algos/AlgoMacro.java:168-170`). Its point-on-path output `D` is
draggable because `AlgoMacro` is a `FixedPathRegionAlgo` that pushes the
dragged coordinates into the internal point (`AlgoMacro.java:608-663`;
`common/kernel/geos/GeoPoint.java:857-860`). In the native candidate, the
outputs depend on `A`, `B`, (direction) and the explicit model-unit numbers
(offset, overshoot, gap), never on unit or scale state. A drag edits only the
offset number. Exporters see ordinary segments and text.

## 7. Serialization

Additive: a `<command name="DirectDimension">` (or `AxisDimension`) element with
existing output types and the offset as an ordinary free numeric. INFERRED:
older builds do not know the command and cannot rebuild that element. This is
the normal consequence of any new command; `E2` must characterize it on an
older build and record it in the ADR 0031 inventory.

## 8. Verification-class recommendation

`INTEGRATED_PHASE` (registered `PHASE` plus `INTEGRATION`) for β: new shared
commands with additive XML, the ADR 0031 gates, the GGBScript matrix, the
profile pin and unit integration. α, or any new `GeoElement` class or XML
element type, means `GLOBAL_IMPACT`.

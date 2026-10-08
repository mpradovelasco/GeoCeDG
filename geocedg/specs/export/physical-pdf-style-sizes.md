# GeoCeDG physical PDF style sizes

| Field | Value |
|---|---|
| Status | **PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW**; not author approved |
| Version | `0.1` |
| Owner phase | `POST-E2-P1-R2` |
| Decision | [ADR 0035](../../../docs/adr/0035-physical-pdf-style-sizes.md) (`PROPOSED`) |
| Governing author decisions | [P1-R1 closeout record](../../../docs/validation/post_e2_p1_r1_closeout_record.md) (`D-R2-1` to `D-R2-7` in the author instruction of 2026-10-08) |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §13 (physical output of model lengths), §15 |
| Serialization effect | none |

## 0. Status

This specification fills the physical-output contract gap registered by
`POST-E2-P1-R1`: unit-system §13 defines the physical size of model lengths, not
of graphical style sizes. It binds the `POST-E2-P1-R2` technical candidate only;
the mapping of §2 is the author's initial implementation and validation mapping
and becomes normative only through a later author decision after a visual smoke.

## 1. Scope

1.1 The **physical PDF route**: GeoCeDG's picture route
(`PictureExportService.writePDF`) for a document whose effective construction
unit is physical (unit-system §5.2). Every entry point that reaches it (the
picture dialog, the command line, the API routes that delegate to it) is
included.

1.2 Unchanged and out of scope: GeoGebra Classic; GeoCeDG documents with
`UNSPECIFIED_MODEL_UNIT` (legacy device-scale PDF); screen rendering; PNG, SVG,
EMF/EMF+, print; PGF/TikZ, PSTricks, Asymptote; DXF.

## 2. Physical presentation scale

2.1 A **style pixel** is the unit in which GeoGebra drawables size their
graphical styles (stroke width `lineThickness / 2`, dash lengths, end-style and
vector arrowheads, decoration ticks, point markers). On the physical PDF route
one style pixel is rendered as exactly

```text
σ_phys = 0.8 pt = 0.8 · 25.4 / 72 mm ≈ 0.28222 mm
```

on the output, whatever the zoom, the viewport size, the screen resolution, the
construction unit, the presentation unit, the drawing scale and the export-area
mode.

2.2 Consequence for strokes (author mapping `D-R2-2`):

```text
w_PDF(t) = (t / 2) · 0.8 pt = 0.4 · t pt
t = 1 → 0.1411 mm   t = 2 → 0.2822 mm   t = 3 → 0.4233 mm   t = 5 → 0.7056 mm
```

2.3 Every other style size of §3 keeps GeoGebra's own proportion to the stroke:
it is its style-pixel size times `σ_phys`. No separate physical size is defined
for any decoration. Resulting examples: the end-style arrow of a segment of
thickness `t` has a half-width of `t` style pixels, i.e. `0.8 · t` pt; a point of
`pointSize` `s` has a marker diameter of `2 · s` style pixels, i.e. `1.6 · s` pt
(5 → 8 pt ≈ 2.82 mm).

2.4 Drawing scale (`D-R2-3`): style sizes are not multiplied by the drawing
scale. Geometry keeps unit-system §13: `output(L) = L · fb(c) · a / b`.

## 3. Covered style sizes

| # | Quantity | Source |
|---|---|---|
| S1 | stroke width of every drawable stroke (`objStroke`, `decoStroke`), including strokes rebuilt by `updateStrokesJustLineThickness` | `Drawable` |
| S2 | dash pattern lengths of S1 (scaled with the stroke, so dash-to-width proportions are kept) | `EuclidianStatic` |
| S3 | filled line bodies built from S1 (`createStrokedShape`), e.g. the dimension line with end styles | `DrawSegmentWithEndings` |
| S4 | segment end styles: arrows, squares, circles, diamonds, crow's feet, lines, and their 0.5 px outline strokes | `DrawSegmentWithEndings` |
| S5 | segment middle decorations (ticks and arrows) | `DrawSegment` |
| S6 | vector arrowheads | `DrawVector`, `DrawVectorModel` |
| S7 | point markers of every point style, including their outline and fill strokes | `DrawPoint` |

## 4. Not covered (existing rule kept)

Text and labels of every kind, including label positions and offsets, the
dimension value and its placement (`D-R2-7`); angle arc radii and angle
decorations other than their stroke width; fill hatching and patterns; images;
axes, grid, tick marks and axis labels; widgets (sliders, buttons, check boxes,
input boxes); traces; highlighting and selection styles (never exported). Point
labels keep their present offset from the point centre.

## 5. Invariants

5.1 Effective PDF stroke width = `0.4 · t pt` within the PDF number resolution
(five significant figures, ≤ 0.001 pt in practice) for every covered drawable,
at every zoom, unit, drawing scale and export-area mode.

5.2 Geometry: centrelines, page size and clipping are identical to the route
without the scale (unit-system §13 and the `B`/`C` export contracts).

5.3 Proportions: each covered size divided by the stroke width equals the
GeoGebra screen proportion.

5.4 Outside the physical PDF route every output is byte-identical to the route
before `POST-E2-P1-R2` (`σ = 1` exactly).

5.5 No document, style, unit or construction value is changed by an export.

## 6. Known limitations

The PDF device grid stays the view's pixel grid; style sizes are exact, but
positions that the host drawables round to whole pixels (for example square and
circle end styles) keep that rounding. Excluded sizes (§4) keep the zoom-dependent
rule characterized in `POST-E2-P1-R1`.

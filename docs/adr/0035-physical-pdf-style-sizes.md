# ADR 0035 — Physical style sizes in the GeoCeDG PDF route

- Status: **PROPOSED** (technical candidate pending author review; not author approved)
- Date: 2026-10-08
- Phase: `POST-E2-P1-R2`
- Author decisions: [P1-R1 closeout record](../validation/post_e2_p1_r1_closeout_record.md); author instruction of 2026-10-08 (`D-R2-1` to `D-R2-7`)
- Specification: [`geocedg/specs/export/physical-pdf-style-sizes.md`](../../geocedg/specs/export/physical-pdf-style-sizes.md)
- Evidence: [P1-R1 characterization report](../validation/post_e2_p1_r1_characterization_report.md); [R2 design](../architecture/post_e2_p1_r2_physical_pdf_stroke_design.md)

## Context

GeoGebra sizes graphical styles in view pixels and its PDF export scales the
view, so the physical stroke width of a GeoCeDG physical PDF is
`(t / 2) · printingScale · 10 / xscale` mm: it follows the zoom at export time
(`POST-E2-P1-R1`). The geometry is physical (`C`), the style sizes are not, and
no GeoCeDG contract governs them. Text uses another mechanism and is excluded.

## Decision

1. On GeoCeDG's physical PDF route one style pixel is rendered as `0.8 pt`
   (author mapping `w = 0.4 · t pt`), independent of zoom, viewport, screen,
   unit, presentation unit, drawing scale and export area.
2. The mapping is one physical presentation scale applied at the source of every
   `lineThickness`- and `pointSize`-derived style size (stroke, dash, filled line
   bodies, end styles and arrowheads, decoration ticks, vector arrowheads, point
   markers), so GeoGebra's proportions between them are kept; no decoration gets
   an independent physical size.
3. The scale is a property of the drawing view: `EuclidianView` returns `1`, and
   only GeoCeDG's `ExportViewport` returns
   `σ = 0.8 / pdfPointsPerPixel` when `PictureExportService` writes a physical
   PDF. The live views, Classic and every other route keep `σ = 1` exactly.
4. Text, labels and their placement, and the excluded sizes of the specification
   §4, keep their existing rule.

## Rejected alternatives

- **Render the export viewport at a scale where one pixel is 0.8 pt.** One
  transform for everything, but it changes text sizes and label offsets, which
  `D-R2-7` forbids.
- **Wrap the PDF graphics and rescale `setStroke`.** It misses the filled line
  bodies, arrowheads and point markers, which are fills built from pixel sizes.
- **Temporarily change the elements' `lineThickness` during export.** Mutates
  document styles, has integer granularity and does not reach point markers or
  constants.
- **Change `Drawable` globally.** Changes the screen and Classic.
- **Independent physical sizes per decoration (for example the PGF dot rule).**
  Arbitrary relative to GeoGebra's proportions; `D-R2-6` prefers one coherent
  transform.

## Consequences

- Physical PDFs have reproducible line weights equal to the PGF/TikZ weights.
- Point markers become `1.6 · pointSize` pt in diameter (PGF draws
  `pointSize` pt); arrowheads follow the stroke as on screen.
- Shared upstream drawables gain one multiplication by `view.getPhysicalStyleScale()`
  in their style-size paths; with `σ = 1` they compute exactly what they computed
  before.
- Excluded sizes (text, angle arcs, hatching, axes, widgets) remain
  zoom-dependent in physical PDFs; known limitation.

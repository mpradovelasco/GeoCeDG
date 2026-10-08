# POST-E2-P1-R2 — physical PDF stroke width: design

```text
STATE              = DESIGN — PROPOSED (bound to the R2 technical candidate)
BASE               = P1-R1 closeout 0bb34739168e71eb62ca099273a9920e8a61673a
                     tree 6bd46e7a7dea44d5d18ad3beeee5cd7134984b03
VERIFICATION CLASS = INTEGRATED_PHASE (PHASE + INTEGRATION)
DESIGN GATE        = PASSED (§8); author approval PENDING
IMPLEMENTATION     = TECHNICAL CANDIDATE (docs/validation/post_e2_p1_r2_candidate_report.md)
selfApproved       = false
```

Deliverables: proposed specification
[`physical-pdf-style-sizes.md`](../../geocedg/specs/export/physical-pdf-style-sizes.md);
proposed [ADR 0035](../adr/0035-physical-pdf-style-sizes.md); this document
(ownership map, object matrix, invariants, compatibility boundaries, test plan).

## 1. Mechanism to correct (from P1-R1)

`Drawable.updateStrokes` sizes strokes at `lineThickness / 2` view pixels;
`PictureExportService.writePDF` paints the GeoCeDG `ExportViewport` (a separate,
unattached `EuclidianViewD` with its own drawables, at the source view's scale)
under the page transform `pdfPointsPerPixel = printingScale · 72 / (2.54 ·
xscale)`. Physical width = style pixels × `pdfPointsPerPixel`.

## 2. Selected transform

```text
σ = 0.8 pt / pdfPointsPerPixel        (style pixels → view pixels of the export)
physical size = styleSize · σ · pdfPointsPerPixel = styleSize · 0.8 pt
```

`σ` is computed per export from the same `pdfPointsPerPixel` that sizes the
page, so the zoom, the viewport, the unit (through `printingScale`) and the
drawing scale cancel exactly. It is exposed by one accessor,
`EuclidianView.getPhysicalStyleScale()` (`1` by default), overridden only by
`ExportViewport`, whose value `PictureExportService` sets before the viewport's
drawables are created, and only for a physical document on the PDF route.

## 3. Source ownership and implementation map

| Component | Owner | Change |
|---|---|---|
| `PictureExportService.writePDF`, `paint` | GeoCeDG export adapter | computes `σ` for a physical document; passes it to the viewport |
| `ExportViewport` | GeoCeDG export adapter | stores `σ` before binding; overrides `getPhysicalStyleScale()` |
| `EuclidianView.getPhysicalStyleScale()` | upstream file, GeoCeDG seam | returns `1` |
| `EuclidianStatic.scaleStroke(stroke, σ)` | upstream file, GeoCeDG helper | width and dash × `σ`; cap, join, miter kept |
| `Drawable.updateStrokes`, `updateStrokesJustLineThickness` | upstream, GeoCeDG-gated | strokes scaled by `σ` when `σ ≠ 1` (S1, S2, S3) |
| `DrawSegmentWithEndings` | upstream, GeoCeDG-gated | end-style sizes and 0.5 px outlines × `σ` (S4) |
| `DrawSegment` middle decorations | upstream, GeoCeDG-gated | tick spacing, length and arrow size × `σ` (S5) |
| `DrawVectorModel` | upstream, GeoCeDG-gated | arrowhead factor × `σ` (S6) |
| `DrawPoint` | upstream, GeoCeDG-gated | marker geometry and strokes × `σ`; label offset unchanged (S7) |

With `σ = 1` each seam computes exactly the previous values (multiplication by
`1.0` is exact; integer arithmetic is preserved where it existed), so live
views, Classic and every other route are unchanged.

## 4. Object matrix

| Object | Rendering mechanism (PDF) | Covered by |
|---|---|---|
| segment, line, ray | stroked path, `objStroke` | S1 |
| circle and other conics | stroked path, `objStroke`; degenerate conics via `updateStrokesJustLineThickness` | S1 |
| curves, loci (incl. Locus V2), splines | stroked path, `objStroke` | S1 |
| polygon edges / outline | stroked segments or path, `objStroke` | S1 |
| dashed styles | stroke dash array | S2 |
| dimension line (end styles) | filled outline from `objStroke.createStrokedShape` minus end shapes | S3 |
| dimension arrowheads | filled end-style shapes sized by `lineThickness` | S4 |
| dimension extension lines | stroked segments | S1 |
| segment ticks / middle arrows | stroked with `decoStroke` at pixel offsets | S1, S5 |
| vector arrowheads | filled shape sized by `getFactor(lineThickness)` | S6 |
| point markers (all styles) | filled/stroked shapes sized by `pointSize` | S7 |
| texts, labels, dimension value | glyphs, another mechanism | not covered (`D-R2-7`) |
| angle arcs (radius), hatching, images, axes/grid, widgets | pixel sizes outside lineThickness/pointSize | not covered (spec §4) |

## 5. Invariants

Specification §5: width `0.4 · t pt`; geometry, page and clipping unchanged;
GeoGebra proportions kept; byte-identical outside the physical PDF route; no
document change.

## 6. Compatibility and regression boundaries

- Classic PDF (upstream `GraphicExportDialog.exportPDF` body): unchanged —
  never reaches `ExportViewport`.
- GeoCeDG unspecified-unit PDF: `σ = 1`; unchanged.
- Screen rendering and every other picture route (PNG, SVG, EMF, print): their
  views report `σ = 1`; unchanged.
- PGF/TikZ, PSTricks, Asymptote, DXF: do not use drawables' strokes for
  output; unchanged.
- Serialization, kernel, units, native-dimension geometry, `Offset`, `Gap`,
  `Overshoot`, dimension text placement: untouched.

## 7. Test and verification plan

Focused Desktop test `PostE2P1R2PhysicalPdfStyleTest` reusing the P1-R1 PDF
interpreter (graphics-state stack, CTM, `w`, caps/joins, dash, paths, fills,
colour attribution):

- §8.1 objects: segment, line, circle, a function curve, a polygon edge; `t` =
  1, 2, 3, 5: stroke `0.4 · t pt` at three zooms.
- §8.2 dimension line body, extension lines A and B, arrowheads at A and B:
  body thickness and arrowhead size proportional to `t`, zoom-independent.
- §8.3 point markers: dot, cross, circle, diamond styles and sizes 3, 5, 9:
  diameter `1.6 · s pt`, zoom-independent; point centre unchanged.
- §8.4 drawing scales 1:1, 1:2, 1:10, 2:1: constant width; geometry follows the
  scale.
- §8.5 physically equivalent mm, cm, m, `usm`: equal widths; presentation-unit
  change: byte-identical PDF.
- §8.6 zooms 2, 8, 32 px per unit; §8.7 explicit area and viewport fallback.
- §8.8 compatibility: Classic PDF stroke rule and bytes as before; unspecified
  PDF as before; PGF unchanged; live view strokes unchanged; dash proportions.
- §8.9 page size, centrelines and clipping unchanged; independent rasterization
  cross-check (scratch, Ghostscript and poppler).

Then the affected focused suites (P1, P1-R1, C picture/physical tests, E2
Desktop), `final.shared` and `final.desktop` at the 512 MB heap, the official
inventory producers, STATIC, PHASE `POST-E2-P1-R2` and INTEGRATION on the frozen
commit.

## 8. Design gate

| Binding decision | Satisfied by |
|---|---|
| D-R2-1 stable width | `σ` derived from the page transform; all listed factors cancel |
| D-R2-2 `0.4 · t pt` | one style pixel = 0.8 pt |
| D-R2-3 drawing-scale independence | `printingScale` contains `a/b`, `σ` cancels it for styles only |
| D-R2-4 product scope | only `ExportViewport` on the physical PDF route reports `σ ≠ 1` |
| D-R2-5 distinguish strokes, bodies, arrowheads, markers, decorations | object matrix §4; bodies through `objStroke`, ends and markers through their own seams |
| D-R2-6 coherent decorations | one transform; GeoGebra proportions kept; no arbitrary sizes |
| D-R2-7 text excluded | no text path consults `σ`; point label offsets unchanged |

No unresolved semantic, persistence, compatibility or architectural choice
remains. Two consequences are reported for the author's visual smoke, not
decided silently: point markers of `1.6 · pointSize pt` (PGF uses
`pointSize pt`), and point labels that keep their present offset while the
marker size changes.

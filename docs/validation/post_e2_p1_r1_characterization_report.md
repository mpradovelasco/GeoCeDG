# POST-E2-P1-R1 — PDF stroke width: characterization and design report

```text
TECHNICAL_CANDIDATE_STATE = CHARACTERIZATION CANDIDATE, FROZEN with the commit that
                            contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P1-R1
ACTIVITY                  = CHARACTERIZATION AND DESIGN ONLY
VERIFICATION_CLASS        = BOUNDED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P1-R1
BASELINE                  = 64d25ba88703372c7c83e1c2fbdd59cbdc4d9aaa
                            tree 50f0f7ccb84b441d7c7584454459f0f01d7c4b02
                            (POST-E2-P1 closeout, published main)
OBSERVATION               = OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM
CLASSIFICATION            = REAL PDF STROKE-WIDTH VARIATION
                            (inherited upstream output policy; not governed by the
                            GeoCeDG physical-output contract)
PRODUCT CHANGE            = NONE
implementationAuthorized  = false
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written; it carries no author
approval. The candidate commit cannot name itself; its identity and its `PHASE`
run are reported outside this file. Authority:
[P1 closeout record](post_e2_p1_closeout_record.md). Mirror:
[`post-e2-p1-r1-characterization-evidence.json`](../../geocedg/validation/post-e2/post-e2-p1-r1-characterization-evidence.json);
measured rows:
[`post-e2-p1-r1-measurements.json`](../../geocedg/validation/post-e2/post-e2-p1-r1-measurements.json);
raster cross-check:
[`post-e2-p1-r1-raster-crosscheck.json`](../../geocedg/validation/post-e2/post-e2-p1-r1-raster-crosscheck.json).
Statements are labelled **[observed]**, **[code]**, **[hypothesis]** or
**[recommendation]**.

## 1. Origin of the observation

The P1 characterization (`02d84945`, report §9) measured the extension-line
stroke of one `mm` `1:1` dimension at 1.0, 0.25 and 0.10 mm for 1, 4 and
10 px per model unit at export time, with the paper geometry unchanged. The
author registered it as `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM`
and reported historical experience of excessively thick lines in some
GeoGebra PDF exports.

## 2. Source path GeoCeDG → Graphics View → PDF [code]

| Layer | Class / method | Role |
|---|---|---|
| presentation | `Drawable.updateStrokes` (`common/euclidian/Drawable.java:612-645`) | stroke width = `lineThickness / 2.0` **view pixels** (`EuclidianStatic.getStroke`, `:100-150`); dash lengths also in pixels |
| presentation | `GeoElement.getLineThickness` (int; `MAX_LINE_WIDTH = 13`, `GeoElement.java:144`) | style value, not a physical size |
| GeoCeDG export adapter | `PictureExportService.writePDF` (`geocedg-desktop/export/PictureExportService.java:159-186`) | page = export-area pixels at the view scale × `pointsPerPixel`; `setExactPageSize`, no margins, no fit-to-page (`DQ-B8`) |
| GeoCeDG export adapter | `PictureExportService.pdfPointsPerPixel` (`:192-194`) | `pointsPerPixel = printingScale · 72 / (2.54 · xscale)` |
| GeoCeDG export adapter | `ExportViewport.printingScaleOf` (`geocedg-desktop/export/ExportViewport.java:120-133`) | physical document: `printingScale = getPhysicalExportScale()` = cm per model unit = `f(c)·100·a/b` (`C4`); otherwise the view's printing scale or a power-of-ten fallback from the zoom |
| GeoCeDG export adapter | `PictureExportService.paint` (`:375-381`) → `ExportViewport.exportPaint` | paints the view drawables in view pixels under the CTM `pointsPerPixel` |
| upstream (Classic route) | `GraphicExportDialog.exportPDF` static (`desktop/export/GraphicExportDialog.java:1057`) | in GeoCeDG delegates to the picture route; in Classic runs the unchanged upstream body: `exportPaint(g, printingScale / (xscale · 2.54 / 72))` with FreeHEP default margins and fit-to-page |
| PDF | FreeHEP `PDFGraphics2D` | writes `w` in device (view-pixel) units under the page CTM |

Kernel, units, `ExportArea` and `drawingScale` determine only the page and the
geometry. The stroke width is a presentation quantity in view pixels mapped by
the zoom-dependent `pointsPerPixel`.

## 3. Method

Committed test
`source/desktop/desktop/src/test/java/org/geocedg/desktop/PostE2P1R1PdfStrokeWidthCharacterizationTest.java`
(selection `post-e2-p1-r1.desktop`, phase `POST-E2-P1-R1`). Each object has its
own colour; the produced PDF is decoded (ASCII85, Flate) and interpreted: graphics
state stack (`q`/`Q`), concatenated `cm`, `w`, `J`, `j`, `m`/`l`/`c`/`re`,
`S`, fills. Effective width = `w` × uniform CTM scale at stroke time; centreline
lengths come from the transformed path, never from the stroke. Scenes: a
segment, a line, a circle, a point and an `AlignedDimension` (dimension line,
extension lines, arrowheads, value text) plus an ordinary text; explicit export
area unless stated; panel 800×600 px, origin (100, 500).

Independent cross-check: the kept PDFs rasterized with Ghostscript 10.02.1 and
poppler `pdftoppm` at 600 and 1200 dpi without anti-aliasing; ink pixels counted
across the segment.

## 4. Results

### 4.1 Primary: `mm`, `1:1`, explicit area 60 × 30 mm, zoom only changes [observed]

`p` = 0.1 cm per model unit; `pointsPerPixel` = 1.4173 / 0.35433 / 0.088583 pt
at 2 / 8 / 32 px per unit.

| `lineThickness` | PDF `w` (device units) | 2 px/unit | 8 px/unit | 32 px/unit |
|---:|---:|---:|---:|---:|
| 1 | 0.5 | 0.250 mm | 0.0625 mm | 0.0156 mm |
| 2 | 1.0 | 0.500 mm | 0.125 mm | 0.0312 mm |
| 3 | 1.5 | 0.750 mm | 0.1875 mm | 0.0469 mm |
| 5 | 2.5 | 1.250 mm | 0.3125 mm | 0.0781 mm |

Identical for the segment, the line, the circle and both extension lines (cap 1
round, join 1 round). The decorated dimension line is not stroked: it is a
filled outline whose body height equals the same width. In every case the
40-unit segment measures 40.000 mm and the page 60.000 × 30.000 mm. Law, exact
to 1e-4 relative:

```text
width_mm = (lineThickness / 2) · printingScale_cm · 10 / xscale
         = (lineThickness / 2) px · (physical mm per view pixel at export)
```

### 4.2 Decorations and text [observed]

- Arrowheads (filled): box height 5 × the line width (2.5 / 0.625 / 0.156 mm
  at `t = 2`), same 1/zoom rule.
- Point markers (filled; point size, not `lineThickness`): 5.0 / 1.25 /
  0.3125 mm, same 1/zoom rule.
- Text: **another rule.** The dimension value and an ordinary text have
  identical glyph heights, 0.37 / 0.27 / 0.23 mm, neither constant nor 1/zoom.
  Not characterized further (outside the stroke question); proposed as a
  separate observation (§8).

### 4.3 Units and drawing scale [observed]

Physically equivalent 40 mm segments (`B = 40`, `4`, `0.04`), `t = 2`:

| scale | normalized zoom (8 px per physical model mm) | raw zoom 8 px/unit: mm · cm · m |
|---|---|---|
| 1:1 | 0.125 mm in mm, cm and m | 0.125 · 1.25 · **125** mm |
| 1:10 | 0.0125 mm in mm, cm and m | 0.0125 · 0.125 · 12.5 mm |

Units act only through the physical millimetres per view pixel; the drawing
scale divides the width by `b/a` together with the geometry. A metre drawing
viewed at an ordinary zoom exports strokes centimetres wide (here 125 mm), which
is consistent with the reported "excessively thick lines" **[hypothesis for the
historical cases; observed for these conditions]**.

### 4.4 Unspecified unit and viewport fallback [observed]

- Unspecified unit: `printingScale` is the legacy device scale, a power of ten
  derived from the zoom (0.1 cm/unit at 2 and 8 px/unit; 1 cm/unit at 32 and
  37.8): width 0.50 / 0.125 / 0.3125 / 0.265 mm and the page itself changes by
  decades (60 mm → 600 mm). The same rule applies; the width varies within a
  decade because the scale follows the zoom.
- No explicit area (viewport-derived): the page follows the zoom (400×300,
  100×75, 25×18.75 mm) and the width keeps the same rule (0.5, 0.125,
  0.031 mm).

### 4.5 Upstream Classic [observed]

`GraphicExportDialog.exportPDF` (upstream body) with a fixed printing scale of
0.1 cm/unit and `Export_1`/`Export_2` points: the content is shrunk by FreeHEP's
default margins and fit-to-page (factor 0.540 at 2 px/unit, 0.529 at 8 and 32),
so absolute sizes carry that factor (segment 21.6 mm instead of 40); the
stroke-to-geometry proportion is exactly `(t/2) / (40 · xscale)` and the width
0.270 / 0.066 / 0.017 mm follows the same 1/zoom rule.

```text
UPSTREAM BEHAVIOR = SAME rule (screen-relative stroke width), plus fit-to-page
GEOCEDG BEHAVIOR  = SAME rule; exact page; fixed physical printingScale (C4)
                    makes the zoom dependence direct for physical documents
```

**[code]** The upstream body is unchanged in GeoCeDG (the only GeoCeDG addition
in `exportPDF` is the delegation to the picture route), so the rule predates
`E2`, `C` and `B`. `C` made the page and the geometry physical but did not
address style sizes.

### 4.6 PGF control [observed]

PGF writes `line width = 0.4 · t pt` (0.4 / 0.8 / 1.2 / 2.0 pt for `t` = 1, 2,
3, 5; 0.28 mm at `t = 2`), identical at every zoom: the LaTeX exporters use a
fixed physical mapping, the PDF route a screen-relative one. Geometry agrees in
both (P1).

### 4.7 Raster cross-check [observed]

Ink widths agree with the interpreted PDF to within one or two device pixels
(0.02–0.04 mm), e.g. `t = 5`: 1.25 mm (PDF) vs 1.23–1.27 mm (both renderers,
both resolutions) at 2 px/unit; 0.3125 vs 0.30–0.34 at 8; 0.078 vs 0.04–0.13 at
32 (below 3 px). The variation is real, not a graphics-state misreading.

## 5. Diagnosis

```text
CLASSIFICATION = REAL PDF STROKE-WIDTH VARIATION
                 (INTENTIONAL OUTPUT POLICY upstream: export "as displayed",
                 screen-relative style sizes; NOT specified by the GeoCeDG
                 physical-output contract)
```

- **Root cause [code + observed].** Style sizes (stroke width `t/2`, arrowheads,
  point markers, dash lengths) are defined in view pixels by the presentation
  layer (`Drawable.updateStrokes`), and the PDF adapter maps view pixels to
  points with `pointsPerPixel = printingScale · 72 / (2.54 · xscale)`. Geometry
  is expressed in view pixels that themselves scale with `xscale`, so it cancels;
  style sizes do not, so their physical size is `(pixels) × printingScale · 10 /
  xscale` mm.
- **Ownership.** The rule is upstream GeoGebra's (presentation in screen pixels;
  export by scaling the view), inherited unchanged by GeoCeDG's picture route
  (`PictureExportService`, `ExportViewport`), which owns the physical page since
  `B`/`C`.
- **Affected population.** Every drawable whose style is pixel-sized: segments,
  lines, circles, dimension segments (including the filled dimension-line
  body), arrowheads and point markers (measured); by code also other curves,
  polygon edges and dash patterns. Text follows another, separate rule. Not the geometry, not the page.
  All physical units (through physical mm per view pixel), every drawing scale,
  both export-area modes, the unspecified legacy mode; the Classic route with a
  fixed printing scale.
- **Impact.** For technical documentation the PDF line weight is not
  reproducible: the same document exports different line weights depending on
  the zoom at export (factor 16 between 2 and 32 px/unit here), ranging from
  near-hairlines (0.016 mm) to centimetre strokes in metre drawings. The
  exported geometry is exact.
- **Baseline.** Predates `E2`; inherited from GeoGebra. **[code]** By the same
  pixel-to-page mapping SVG (`cmPerPixel = p / xscale`) and print are expected
  to follow the same rule; **not measured here** and out of scope.
- **Contract.** unit-system §13 defines the physical output of model lengths
  only; no GeoCeDG specification defines physical stroke widths for picture
  exports (`geometry-export-foundation.md` line 149 excludes line thickness from the
  geometric transport). The behavior is therefore a contract gap, not a
  violation of an approved clause.

## 6. Corrective design alternatives (design only; not implemented)

| Criterion | A — preserve, document | B — stable physical mapping in the GeoCeDG PDF route | C — GeoCeDG export option ("physical line weights") |
|---|---|---|---|
| source ownership | docs only | `PictureExportService` / `ExportViewport` (GeoCeDG picture route) | same, behind an option of the route |
| existing styles | unchanged | `lineThickness` keeps its meaning on screen; the export maps it to a fixed physical width (to be decided, e.g. the PGF rule `0.4·t pt`) | unchanged by default |
| physical semantics | screen-relative weights | weights independent of zoom, unit and export area; independent of or proportional to the drawing scale (author decision) | as B when enabled |
| Classic | unchanged | unchanged (route is product-only) | unchanged |
| `ExportArea`, `drawingScale` | — | page and geometry unchanged; drawing-scale policy for weights to be decided | same |
| print/PDF consistency | inconsistent with PGF | consistent with PGF if the same mapping is chosen; print and SVG to be aligned later | same |
| arrowheads, markers, text | unchanged | arrowheads follow the stroke; markers and text need their own decision (text has a separate open observation) | same |
| regression tests | none | interpreted-PDF widths at several zooms equal the mapping; geometry and page unchanged; Classic route unchanged | both modes |
| spec/ADR | guide note | new physical-output clause for style sizes (unit-system §13 or the picture-export specification) and an ADR | same, plus the option's UI |
| verification class | `DOCUMENTATION_STATUS_ONLY` | `INTEGRATED_PHASE` (shared picture route used by every physical PDF; normative amendment) | `INTEGRATED_PHASE` |

Implementation idea for B (to be validated, not decided): render the export
viewport with style sizes scaled by `xscale / reference` so that one style
pixel equals a fixed paper length, or set the PDF stroke scale independently of
the geometry CTM; either way only in the GeoCeDG picture route, never in
`Drawable` (which would change the screen) and never in the kernel.

**[recommendation]** Option B, scoped to the GeoCeDG physical PDF route
(physical documents only; the unspecified legacy mode and Classic unchanged),
with the mapping taken from the existing LaTeX rule (`0.4 · lineThickness` pt,
independent of zoom) so that PDF and PGF agree, and the drawing-scale policy and
the treatment of point markers decided explicitly by the author. Reasons: it
fixes the reproducibility problem at the narrowest GeoCeDG-owned point, keeps
geometry, kernel, screen and Classic unchanged, and reuses a mapping the product
already ships. Option A alone leaves physical PDFs unsuitable for technical
documentation; a global change in `Drawable` or the upstream body is not
recommended. Text sizing needs its own characterization before it is included.

**Implementation owner:** GeoCeDG Desktop picture export
(`PictureExportService`, `ExportViewport`); specification owner the physical
output contract. **Implementation verification class:** `INTEGRATED_PHASE`
(PHASE + INTEGRATION), because the change touches the shared physical PDF route
and a normative clause; not `BOUNDED_PHASE`.

## 7. Scope and exclusions

No product, exporter, kernel, geometry, unit, serialization, DAG or command
change. SVG, EMF, print, PGF, PSTricks and Asymptote were not changed and, apart
from the PGF control, not measured. The approved `E2` and P1 evidence is
unchanged.

## 8. Open author decisions

1. Accept the diagnosis (real, inherited, screen-relative policy; contract gap)?
2. Option A, B or C? If B or C: the physical mapping (recommended
   `0.4 · lineThickness` pt as in PGF), whether weights scale with the drawing
   scale, and whether point markers follow a physical size.
3. Scope: PDF only first, or PDF, SVG and print together (one physical
   style rule for all picture routes)?
4. Register `OBS-POST-E2-P1-R1-PDF-TEXT-SIZE` (text glyphs 0.23–0.37 mm, another
   rule) for separate characterization?
5. Register `OBS-POST-E2-P1-R1-PDF-EMBEDDED-FONTS-FAILS` for separate
   characterization: in the headless test harness the embedded-font PDF route
   (`textAsShapes = false`) throws `StackOverflowError` in FreeHEP
   (`AbstractCharTable.toName`) even for a scene without text, and the Classic
   route throws `NullPointerException`; not verified in the windowed product.

## 9. Verification

Committed: the characterization test, its selection and phase registration
(`POST-E2-P1-R1`: compile shared/Desktop and
`junit.desktop.post-e2-p1-r1.semantic`), the inventory refreshed in-session by
the official updater from passing producers (discovery 6046 shared / 1917
Desktop; `post-e2-p1-r1.desktop` 7; `final.desktop` 1904 → 1911), the registry
`junit_inventory` pin `f11e9c72…` → `e5c6a0f0…` and coverage literals (46
selections, 53 PHASE selections, `post-e2-p1-r1.desktop` 7), the upstream register entry, this report, its evidence, the measured
rows and the raster cross-check, and the roadmap and planning status.
`PHASE -Phase POST-E2-P1-R1` runs on the frozen commit; its identity is reported
outside this file.

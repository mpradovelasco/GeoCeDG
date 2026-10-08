# POST-E2-P1-R2 — physical PDF stroke width: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P1-R2
VERIFICATION_CLASS        = INTEGRATED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P1-R2
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
BASELINE                  = P_POST_E2_P1_R1 0bb34739168e71eb62ca099273a9920e8a61673a
                            tree 6bd46e7a7dea44d5d18ad3beeee5cd7134984b03
DESIGN                    = COMPLETE — PROPOSED (design gate passed)
MAPPING                   = 0.4 · lineThickness pt (one style pixel = 0.8 pt), initial
                            implementation and validation mapping (D-R2-2), not normative
PRODUCT CHANGE            = YES (GeoCeDG physical PDF route only)
SERIALIZATION CHANGE      = NONE
AUTHOR VISUAL SMOKE       = PENDING
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written; it carries no author
approval. Its identity and the two acceptance runs are reported outside it.
Authority: [P1-R1 closeout record](post_e2_p1_r1_closeout_record.md) and the author
instruction of 2026-10-08 (`D-R2-1` to `D-R2-7`). Design:
[`post_e2_p1_r2_physical_pdf_stroke_design.md`](../architecture/post_e2_p1_r2_physical_pdf_stroke_design.md);
specification
[`physical-pdf-style-sizes.md`](../../geocedg/specs/export/physical-pdf-style-sizes.md)
and [ADR 0035](../adr/0035-physical-pdf-style-sizes.md), both `PROPOSED`. Mirror:
[`post-e2-p1-r2-candidate-evidence.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-candidate-evidence.json);
measured rows:
[`post-e2-p1-r2-measurements.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-measurements.json);
raster cross-check:
[`post-e2-p1-r2-raster-crosscheck.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-raster-crosscheck.json).

## 1. Design summary

One physical presentation scale `σ = 0.8 pt / pdfPointsPerPixel` (view pixels per
style pixel) is computed by `PictureExportService.writePDF` for a document with a
physical construction unit and given to GeoCeDG's `ExportViewport` before its
drawables are created. `EuclidianView.getPhysicalStyleScale()` returns `1`; only
that viewport returns `σ`. Drawables multiply every size they derive from
`lineThickness` and `pointSize` by it: strokes and dash patterns
(`Drawable`, `EuclidianStatic.scaleStroke`), filled line bodies (from the scaled
`objStroke`), end styles and arrowheads (`DrawSegmentWithEndings`), middle
decorations (`DrawSegment`), vector arrowheads (`DrawVectorModel`), point markers
(`DrawPoint`). Text, labels and their placement are untouched; vector and point
label offsets keep their style-pixel size. With `σ = 1` every seam computes the
previous values exactly.

Resulting physical sizes, independent of zoom, viewport, unit, presentation unit,
drawing scale and export-area mode:

| quantity | rule | t or s = 1 | 2 | 3 | 5 |
|---|---|---:|---:|---:|---:|
| stroke, filled dimension body | `0.4 · t` pt | 0.1411 mm | 0.2822 mm | 0.4233 mm | 0.7056 mm |
| dimension arrowhead height | `2.5 · t` style px = `2 · t` pt (5 × the line, as on screen) | 0.706 mm | 1.411 mm | 2.117 mm | 3.528 mm |
| long dash / gap (t = 2) | `(8 + t/2) · 0.8` / `8 · 0.8` pt | | 7.2 / 6.4 pt | | |
| point marker extent | `2 · s · 0.8` pt = `1.6 · s` pt | | | s = 3: 1.693 mm | s = 5: 2.822 mm; s = 9: 5.080 mm |

## 2. Verification matrix (focused tests)

`PostE2P1R2PhysicalPdfStyleTest` (7 methods, PDF interpreter of P1-R1 extended
with dash arrays) — all pass:

- §8.1 segment, line, circle, curve, polygon edge, dashed segment, `t` = 1, 2, 3,
  5, zooms 2, 8, 32 px/unit: every stroke `0.4 · t pt` (relative 1e-3); dash
  `(8 + t/2) · 0.8` / `8 · 0.8` pt; caps and joins as before (round); segment
  40.000 mm, page 60 × 30 mm at every zoom.
- §8.2 dimension: filled body `0.4 · t pt`, extension lines A and B
  `0.4 · t pt`, arrowheads A and B `2 · t pt` and equal (within the printed
  coordinate resolution, 0.0012–0.0050 mm); dimension geometry and value
  unchanged by the exports.
- §8.3 point markers dot, filled diamond, cross, circle; sizes 3, 5, 9:
  extent `1.6 · s pt` at every zoom; point centre fixed on the page.
- §8.4 drawing scales 1:1, 1:2, 1:10, 2:1: stroke 0.2822 mm; segment
  `40 · a/b` mm; page `60 · a/b` mm.
- §8.5 physically equivalent 40 mm drawings in mm, cm, m and `usm` (inch), three
  normalized zooms: stroke 0.4233 mm (t = 3); changing only the presentation unit
  gives byte-identical PDFs.
- §8.6/§8.7 zoom and export area: explicit area (all of the above) and viewport
  fallback (page `800 / zoom` mm, stroke 0.2822 mm).
- §8.8 document and live view: the export changes no XML; the live view reports
  `σ = 1`.

`PostE2P1R2BaseFingerprintTest` — compatibility (§8.8): the digests of the
Classic PDF and PGF, the GeoCeDG unspecified-unit PDF and the physical PNG, SVG,
EMF and PGF of a scene with strokes, dashes, end-style arrows and squares, middle
decorations, a vector, cross and dot markers and a native dimension are equal to
the fixture produced by the same class on the base tree `0bb34739`
(`git archive`, scratch build) — byte-identical outside the physical PDF route.

Reconciled characterization tests (P1, P1-R1): their physical-route assertions
described the pre-R2 behavior; they now assert the R2 mapping (renamed methods),
and their Classic, unspecified, PGF, viewport-page and text characterizations
are unchanged. The P1 and P1-R1 reports and measurements stay historical
evidence.

## 3. Independent raster cross-check

The kept PDFs (`t` = 2 and 5 at 2, 8, 32 px/unit) rasterized by Ghostscript
10.02.1 and poppler `pdftoppm` at 600 and 1200 dpi without anti-aliasing: the
ink width is the same at the three zooms (t = 2: 7–8 px at 600 dpi, 14–15 px at
1200 dpi; t = 5: 17–18 and 34–35 px), i.e. 0.30–0.34 mm and 0.72–0.76 mm against
the interpreted 0.282 and 0.706 mm, within one or two device pixels of the
renderers' coverage rules. Before R2 the same t = 5 line measured 1.25, 0.31 and
0.08 mm (P1-R1).

## 4. Compatibility and scope

Unchanged by construction and by the fingerprint test: GeoGebra Classic (its
`exportPDF` body never reaches `ExportViewport`); GeoCeDG unspecified-unit PDF
(`σ = 1`); screen rendering (live views report 1); PNG, SVG, EMF/EMF+ and print
(the picture routes pass `σ = 1`); PGF/TikZ, PSTricks, Asymptote and DXF (no
drawable strokes). No kernel, unit, serialization, DAG, command or native
dimension geometry change; `Offset`, `Gap`, `Overshoot` and the dimension text
placement are untouched.

Not covered (specification §4, unchanged): text and labels (TD-PDF-1 stays
open), angle arc radii, hatching, images, axes and grid, widgets.

## 5. Points for the author's visual smoke

1. Line weights: t = 1, 2, 3, 5 give 0.14, 0.28, 0.42, 0.71 mm in every
   physical PDF; compare with the PGF/TikZ output of the same document.
2. Point markers: `1.6 · pointSize` pt (default 5 → 2.82 mm); PGF draws
   `pointSize` pt (1.76 mm). Point labels keep their present offset from the
   point centre while the marker size no longer follows the zoom.
3. Arrowheads: five times the line width, as on screen.
4. Text keeps its existing (very small) size in physical PDFs; not part of R2.
5. Classic, unspecified-unit PDFs and every other format are unchanged.

Suggested smoke: a `mm` document with segments of thickness 1, 2, 3, 5, a
dashed line, a circle, points of sizes 3, 5, 9 in several styles and an
`AlignedDimension`; export PDF at three different zooms and at drawing scales
1:1 and 1:10; check that line weights and markers are identical across the
PDFs and that the geometry scales with the drawing scale.

## 6. Verification

Full suites, inventory, registration and the acceptance runs are recorded in the
evidence mirror; the frozen commit's `PHASE` and `INTEGRATION` identities are
reported outside this file.

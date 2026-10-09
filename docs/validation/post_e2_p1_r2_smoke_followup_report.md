# POST-E2-P1-R2 — author smoke follow-up: revised candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P1-R2 (author smoke follow-up)
ORIGINAL CANDIDATE        = 542a5adc7e4be15fb05985ed0a8e84817d22f795
                            tree 06a98e3a51dd0a539753f855d48518a85378acf4
AUTHOR SMOKE              = PASS WITH THREE REQUESTED REFINEMENTS (author instruction
                            of 2026-10-09; not recorded as an approval here)
VERIFICATION_CLASS        = INTEGRATED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P1-R2
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
PRODUCT CHANGE            = YES — BOUNDED PRESENTATION
GEOMETRIC SEMANTICS CHANGE = NONE
SERIALIZATION SCHEMA CHANGE = NONE
AUTHOR RE-SMOKE           = PENDING
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written; it carries no author
approval. The original R2 candidate, its
[report](post_e2_p1_r2_candidate_report.md), its
[evidence](../../geocedg/validation/post-e2/post-e2-p1-r2-candidate-evidence.json)
and its PHASE / INTEGRATION runs stay unchanged as historical evidence. The R2
design, specification and ADR are amended, still `PROPOSED`:
[design §9](../architecture/post_e2_p1_r2_physical_pdf_stroke_design.md),
[specification 0.2 §2.5](../../geocedg/specs/export/physical-pdf-style-sizes.md),
[ADR 0035 amendment](../adr/0035-physical-pdf-style-sizes.md). Mirror:
[`post-e2-p1-r2-smoke-followup-evidence.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-smoke-followup-evidence.json);
measured marker rows:
[`post-e2-p1-r2-smoke-followup-markers.json`](../../geocedg/validation/post-e2/post-e2-p1-r2-smoke-followup-markers.json).

## 1. Refinement A — physical PDF point markers

**Meaning of `pointSize` (verified in both exporters).** `pointSize` `s` is a marker
size, never a stroke.

| | screen / R2 PDF (`DrawPoint`) | PGF/TikZ (`GeoGebraToPgf.drawGeoPoint`) |
|---|---|---|
| nominal size | half-size `s` style px (circumradius `s` for triangles) | `s` pt geometric extent |
| radius / diameter | dot and circle: radius `s` px, diameter `2s` px | `circle (s/2 pt)`: diameter `s` pt |
| filled geometry | dot, filled diamond, triangles | dot (`fill=`), filled diamond, triangles |
| outer stroke | `s/2` px, square caps, mitre joins (cross, plus, circle, empty diamond); filled diamond and triangles outlined at `s/2` px; dot border 1 px | TikZ default `0.4 pt`, round caps and joins (header `line cap=round,line join=round`) |
| triangles | circumradius `s` px | circumradius `3s/4` pt |

R2 rendered one marker style pixel as `0.8 pt` (the stroke scale), so a marker
spanned `1.6 s` pt, 1.6 times PGF. The follow-up gives markers their own scale,
`μ = 0.5 pt / pdfPointsPerPixel` (`EuclidianView.getPhysicalMarkerScale()`, `1`
everywhere except GeoCeDG's physical PDF `ExportViewport`), applied to the whole
marker in `DrawPoint`. Strokes keep `σ` and `0.4 · t pt`.

**Measured from the exported files** (PDF interpreted by the P1-R1 interpreter,
PGF from its written path; zooms 2, 8 and 32 px per unit, sizes 3, 5 and 9, all ten
styles; identical at every zoom):

| style | PDF extent | PGF extent | PDF outline | PGF outline |
|---|---|---|---|---|
| dot | `s` pt | `s` pt | 0.5 pt (black border) | 0.4 pt |
| cross, plus, circle, empty diamond, filled diamond | `s` pt | `s` pt | `s/4` pt | 0.4 pt |
| triangles N, S, E, W | `0.866 s` × `0.75 s` pt | `1.299 s` × `1.125 s` pt | `s/4` pt | 0.4 pt |

For `s` = 3, 5, 9 the PDF/PGF geometric ratio is 1.000 for the first six styles and
0.667 for triangles. Independent raster cross-check (Ghostscript at 1200 dpi of the
PDF and of the pdfLaTeX-compiled PGF, zoom 8): §5.

Documented shape differences, not copied from PGF (ADR 0035 amendment): PGF's
triangles are 1.5 times the screen proportion, and PGF outlines are a fixed thin
line. Copying either would add a per-style rule that changes GeoGebra's marker
proportions; the author can choose otherwise at the re-smoke.

Preserved: marker centres (asserted constant over the zooms), point identity,
style and stored `pointSize` (asserted after export), screen rendering and every
other route (`getPhysicalMarkerScale() = 1`; base fingerprints of Classic PDF and
PGF, unspecified PDF, physical PNG, SVG, EMF and PGF still equal the base
fixture), the stroke mapping and the dimension measurements of R2.

## 2. Refinement B — numeric line-thickness field

**Located slider.** The line-thickness slider of the object Properties is
`PropertiesPanelD.LineStylePanel.thicknessSlider` (`JSlider(1,
GeoElement.MAX_LINE_WIDTH = 13)`, minimum 0 for polygons through
`LineStyleModel.updateProperties`), shared by the Object Properties and the
Defaults dialog through the same class. It applies through
`LineStyleModel.applyThickness` and stores undo on mouse release (`SliderUtil`).
The Dimension presentation panel's `JSpinner` is a creation preference and was not
touched; the style-bar line popup is unchanged.

**Change.** A GeoCeDG class, `GeoCeDGLineThicknessField`, adds a `JSpinner`
beside the slider only under the GeoCeDG profile (`AppConfigGeoCeDG`; Classic
gets `null` and no component). The upstream file gains a profile-gated
construction, labels and font call (`PropertiesPanelD`, registered).

- The slider stays the only style writer: an accepted entry calls
  `slider.setValue`, the existing listener applies it, then one
  `LineStyleModel.storeUndoInfo()`.
- The field mirrors the slider's model (value, minimum and maximum) under a guard,
  so selection updates and slider moves never write back and never store undo.
- Accepted: an integer within the current slider range (spaces trimmed), by
  `Enter`, focus loss or the spinner arrows. Refused: empty, non-numeric,
  decimal, signed, out of range; the field shows the slider value again and the
  style, document and undo history are untouched.
- No second thickness property, no kernel, XML or persistence change.

## 3. Refinement C — drawing scale in the status bar

`GeoCeDGStatusBar` gains a permanent `drawing-scale` segment after the
presentation unit: `Scale: a:b` / `Escala: a:b` from `AppGeoCeDG.getDrawingScale()`
(the drawing scale, never zoom, pixels per unit or DPI); without a construction
unit `Scale: non-physical` / `Escala: no física` with an explanatory tooltip
(unit-system §13). `AppGeoCeDG.getStatusBar()` registers one
`addDrawingScaleListener` callback when the bar is created; the bar lives as long
as its window and panel rebuilds reattach the same bar, so there is one listener
per window and none per rebuild. The callback refreshes the bar on the thread that
changed the scale, like the layer-workspace listener (in the product that is the
event thread); it never queues a deferred refresh that could run concurrently with
a reset or load on another thread (§6).
New, Open and the other transition events reset the scale through the existing
`resetDrawingScale`; unit changes and language changes refresh the bar as before.
No undo point, no modified flag, no serialization.

The existing segment texts are kept (`Layer: 0 | Construction unit: mm |
Presentation unit: mm | Scale: 1:10`); the author's target wording with the short
labels `Construction:` / `Presentation:` would change the D1 segments and is left
for the re-smoke. The paste notice stays the last, elided segment of the single
row.

## 4. Verification matrix (focused tests)

| Test | Covers |
|---|---|
| `PostE2P1R2PhysicalPdfStyleTest.pointMarkersSpanThePointSizeInPointsAtEveryZoom` | six styles, sizes 3, 5, 9, zooms 2, 8, 32: extent `s` pt, square, outline `s/4` pt (dot border 0.5 pt), centre constant, stored size and style unchanged (amends the R2 method `pointMarkersAreOnePointSixTimesThePointSizeAtEveryZoom`) |
| `PostE2P1R2PhysicalPdfStyleTest.pointMarkersMatchThePgfMarkerGeometry` | all ten styles, three sizes, three zooms: PDF width and height equal the PGF path extent (triangles 2/3); PGF constant over the zoom |
| other `PostE2P1R2PhysicalPdfStyleTest` methods | R2 stroke, dash, dimension body and arrowhead, drawing-scale, unit, viewport and document assertions, unchanged and passing |
| `PostE2P1R2BaseFingerprintTest` | Classic PDF/PGF, unspecified PDF, physical PNG/SVG/EMF/PGF digests equal the base fixture |
| `PostE2P1R2SmokeFollowUpTest` (B) | field beside the slider in the real `PropertiesPanelD`; host range 1–13 kept; follows slider moves and selection without writing; polygon minimum 0; refusals (`""`, `abc`, `2.5`, `-1`, `0`, `14`, `99999`, `7x`, blank) change nothing; one undo point; undo and redo; spinner step; one field; EN/ES tooltip; Classic has none |
| `PostE2P1R2SmokeFollowUpTest` (C) | segment order; `Scale: 1:1`, `1:10`, `2:1`, ES `Escala:`; off-EDT change presented; no undo, no modified flag, XML unchanged; non-physical text and tooltip, physical after a unit; New and Open reset; one listener across three panel rebuilds; window-scoped scale; one-row layout with the paste notice last |
| `PreG9BR6PlusD1DocumentUnitsTest`, `PreG9BR6PlusD1PasteNoticeVisibilityTest` | D1 status segments (order reconciled to include the scale segment) and the paste notice layout |

The selection `post-e2-p1-r2.desktop` gains `PostE2P1R2SmokeFollowUpTest` and the two
D1 status-bar test classes.

## 5. Independent raster cross-check of the marker ink

Scratch procedure, not a product test: the test kept the physical PDFs and the PGF
code of the marker comparison at zoom 8 (`GEOCEDG_POST_E2_P1_R2_PDF_DIR`); each PGF
document was compiled with pdfLaTeX (MiKTeX; the unrelated parametric curve, which
needs gnuplot, removed from the scratch copy); both PDFs were rasterized by
Ghostscript 10.02.1 at 1200 dpi without anti-aliasing (one pixel = 0.06 pt). The
marker is located by its yellow fill or stroke, and its ink is the bounding box of
every non-white pixel within 2.5 mm of it, its outline included.

| style | s | PDF ink w × h (pt) | PGF ink w × h (pt) | expected PDF ink |
|---|---:|---|---|---|
| dot | 5 | 5.52 × 5.58 | 5.46 × 5.40 | `s + 0.5` |
| dot | 9 | 9.60 × 9.54 | 9.42 × 9.48 | |
| cross | 5 | 6.84 × 6.84 | 5.46 × 5.40 | `≈ 1.354 s` (square caps on diagonals) |
| cross | 9 | 12.24 × 12.24 | 9.42 × 9.48 | |
| circle | 5 | 6.30 × 6.30 | 5.46 × 5.40 | `1.25 s` |
| circle | 9 | 11.28 × 11.34 | 9.42 × 9.48 | |
| plus | 5 | 6.30 × 6.30 | 5.46 × 5.40 | `1.25 s` |
| plus | 9 | 11.28 × 11.34 | 9.42 × 9.48 | |
| filled diamond | 5 | 6.84 × 6.84 | 5.46 × 5.40 | `≈ 1.354 s` (mitre vertices) |
| filled diamond | 9 | 12.24 × 12.24 | 9.42 × 9.48 | |
| empty diamond | 5 | 6.84 × 6.84 | 5.46 × 5.40 | `≈ 1.354 s` |
| empty diamond | 9 | 12.24 × 12.24 | 9.42 × 9.48 | |
| triangle north | 5 | 6.54 × 5.64 | 6.90 × 6.06 | mitre vertices |
| triangle north | 9 | 11.76 × 10.14 | 12.06 × 10.56 | |

The PGF ink is `s + 0.4` pt for every non-triangle style, as its code says. The
PDF ink follows the geometry `s` plus GeoGebra's own outline (`s/4` pt, square caps,
mitre joins): equal to PGF within 0.2 pt for the dot, up to 1.35 times PGF for the
stroked shapes at large sizes. The two triangle conventions nearly cancel in ink
(PGF's larger triangle with a thin line against the screen triangle with its
heavier outline). This is the documented outline difference of §1.

## 6. Verification

Before the freeze, on the candidate worktree:

| Check | Result |
|---|---|
| `post-e2-p1-r2.desktop` focused classes | 107 tests, 0 failures |
| full shared (`:shared:common-jre:test`, `--rerun-tasks`) | 6963 tests, 0 failures, 0 errors, 10 skipped |
| full Desktop (`:desktop:desktop:test`, `--rerun-tasks`, 512 MB heap unchanged) | 1933 tests, 0 failures, 0 errors, 1 skipped, no `OutOfMemoryError` |
| Checkstyle (`:desktop:desktop:checkstyleMain`, `checkstyleTest`, `:shared:common:checkstyleMain`) | no new violation; one pre-existing `LineLength` warning in `PreG9BR6PlusA1HiddenLayerTest` |
| upstream boundary (`Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b`) | PASS, 1008 registered files |
| official producers: discovery shared / Desktop (`-TestDryRun`); executed `pre-g9b-r6-plus-c.desktop`, `pre-g9b-r6-plus-e2.desktop`, `post-e2-p1-r2.desktop`, `final.shared`, `final.desktop` | all exit 0, `COMPLETED` |
| inventory (in-session updater) | discovery 6046 / 1933; `post-e2-p1-r2.desktop` 83 → 107; `final.desktop` 1919 → 1927; `final.shared` 6963; 47 selections |
| registry `junit_inventory` pin | `cc3e23a3…` → `fc712b3ef1e73e92593246bbc764b2b132503075ba7095f07f2b4ec555267963` |
| coverage literals | `post-e2-p1-r2.desktop` 107; 47 selections and 54 PHASE selections unchanged |

**Corrective history.** A first freeze of this follow-up,
`27388d60a300d0b44173ed8fbf912643c049f3af` (tree
`707b04f9e89c872b5b2fc47eb85954a1d1dae39b`), was accepted by STATIC
(`verification-1897d1a7bdf84cccbf9cd5bc5cbaf20c`), INFRA_UNIT
(`verification-3f1ee6b711eb487b8a25a17ccc22db53`) and PHASE
(`verification-0495a9aec48d4dab977645bc094f0740`), and rejected by INTEGRATION
(`verification-1247ac65be8f4c35a6572b00fd447a31`, `REJECTED_SEMANTIC`, 18/19):
`junit.desktop.final` failed one case,
`PreG9BR6PlusCDrawingScaleTest.e6AResetToABlankDocumentResetsAndAFileResetRelyOnTheLoad`,
with a `NullPointerException` in `Component.getMaximumSize` while the toolbar was
rebuilt. Cause: that test calls `reset()` off the event thread; the reload resets
the drawing scale, and the first freeze's status-bar listener queued the refresh on
the event thread, where `JLabel.setText` invalidated the shared ancestors while the
test thread was rebuilding the toolbar under them. The same test had passed in the
full Desktop run and in the official `final.desktop` producer run. The corrective
refreshes on the changing thread (layer-workspace precedent), so no Swing work is
queued concurrently; `PostE2P1R2SmokeFollowUpTest` now asserts the refresh before
any event-thread turn. The first freeze and its runs stay as historical evidence;
no evidence is reused across commits.

STATIC, INFRA_UNIT, PHASE `POST-E2-P1-R2` and INTEGRATION run on the frozen
commit; their identities and results are reported outside this artifact.

## 7. Scope kept

Unchanged: native dimension algorithms and values, `Offset`, `Gap`, `Overshoot`,
unit semantics, DAG, geometric identity, serialization schema, physical export
geometry (page, centrelines, clipping), Classic, unspecified-unit PDF and every
other format; the R2 stroke mapping `0.4 · lineThickness pt`. `TD-PDF-1` (text size)
and `TD-PDF-2` (embedded fonts) stay registered and not implemented. No P2, P3,
P4, E3, F1, F2, F3 or G work.

## 8. Author re-smoke checklist

A — markers (physical document, File → Graphics View as Picture… → PDF):

1. Points of sizes 3, 5 and 9 in the dot, cross, circle, plus and both diamond
   styles: in the PDF each spans `s` pt (5 → about 1.76 mm), at every zoom, as in a
   PGF/TikZ export of the same points.
2. Triangles: smaller than PGF's (2/3), same proportion as on screen.
3. Outlines keep the screen weight relative to the marker (`s/4` pt), heavier than
   PGF's 0.4 pt line for large markers.
4. Line weights, dimension arrowheads and texts as in the R2 smoke.

B — thickness (select a segment, Properties → Style):

5. A field beside the thickness slider shows the slider value; moving the slider
   updates it.
6. Typing 7 and `Enter` sets thickness 7 and moves the slider; one `Ctrl+Z`
   restores the previous thickness.
7. `0`, `14`, `2.5`, `abc` are refused; the field shows the slider value again.
8. A polygon accepts 0. The slider range is unchanged.

C — status bar:

9. `Layer: 0 | Construction unit: mm | Presentation unit: mm | Scale: 1:1`.
10. Setting 1:10 in an export dialog shows `Scale: 1:10` immediately; New and Open
    show `Scale: 1:1`; the document stays unmodified.
11. A document without a construction unit shows `Scale: non-physical`.
12. Spanish: `Escala: …`; a narrow window keeps one row and the paste notice still
    shows (elided) at the end.

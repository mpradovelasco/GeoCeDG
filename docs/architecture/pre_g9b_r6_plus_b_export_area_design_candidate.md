# PRE-G9B-R6-plus-B — export surface and `ExportArea`: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `B` needs its own
  explicit author authorization.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  findings 5–9 and probes P1 and P2

Path abbreviations as in the P0 report (`common/`, `desktop/`, `geocedg-desktop/`).

## 1. Author-fixed scope

Expose PNG, PDF, SVG, EMF/EMF+, DXF, PSTricks, PGF/TikZ and Asymptote. Never
expose STL, Collada, HTML Collada or Dynamic Worksheet. File actions to define
and display the export area. One `ExportArea` authority, with candidate
producers `Export_1`/`Export_2`, a manual rectangle, an ISO A selection and
`IsoABorder`:

```text
explicit ExportArea              != viewport
zoom / camera / screen clipping  != export authority
```

An explicit area is exported **completely** even when it lies partly or wholly
outside the viewport; the visible view is the fallback only when no explicit
area exists. For an `Export_1`/`Export_2` area: **exactly** the rectangle, with
no outside border. Author preference: session lifetime.

## 2. What the base does (P0 probes P1 and P2)

**Surface.** The GeoCeDG File menu has New, Open, Open recent, Open Classic
diagnostic session, Save, Save as, Print preview, **Export 2D geometry as DXF
(experimental)** `{Ctrl+Shift+D}` and Close. No picture or LaTeX export appears
there. Hidden routes nevertheless fire (probe P1):

| Route | Result in GeoCeDG |
|---|---|
| `Ctrl+Shift+U` | opens *Export as Picture* (`GraphicExportDialog`) |
| `Ctrl+Shift+W` | opens *Upload to GeoGebra Resources* (`WorksheetExportDialog`), the excluded Dynamic Worksheet upload; probe opened and disposed it, nothing uploaded |
| `Ctrl+Shift+D` | **fires twice**: the upstream dispatcher toggles "selection allowed" on all objects and fixes sliders without consuming the key, then the DXF menu accelerator opens the modal DXF dialog |
| `Ctrl+P` | Print Preview |
| `Ctrl+Shift+C`, `Ctrl+Shift+M`, `Ctrl+Shift+B` | not fired by the probe (they write the system clipboard). Source: graphics to clipboard, full HTML5 export to clipboard, Base64 document to clipboard (`common/main/GlobalKeyDispatcher.java:719-747`); GeoCeDG does not override them (`geocedg-desktop/AppGeoCeDG.java:416-435`) |
| command line `--export` | live: `GeoGebraFrame.checkCommandLineExport` ran, wrote a 0-byte file (zero-size view at export time), then `AppD.exit(0)` (`desktop/gui/app/GeoGebraFrame.java:1033-1127`) |
| command line `--exportAnimation=<file> --slider=…` (with `--delay`, `--loop`, `--dpi`, `--maxSize`) | not probed; PROVEN FROM SOURCE in the same method (`GeoGebraFrame.java:852-1031`); an Animated GIF route although GIF is proposed out of the surface (`AQ-X4`) |
| GGBScript `ExportImage` | writes PNG, SVG and PDF files |
| JS/API | `writePNGtoFile`, `getPNGBase64` and `exportPGF` work; scripting is enabled |
| v1-profile fallback | restores the full upstream File menu, including STL, Collada and Worksheet (`geocedg-desktop/GuiManagerGeoCeDG.java:34-50`) |

**Area rules.** `selectionRectangle` takes precedence over `Export_1`/`Export_2`,
which take precedence over the full view, for every picture route
(`common/euclidian/EuclidianView.java:5153-5238`, `:5795-5824`). A leftover
selection rectangle overrides `Export_1`/`Export_2` (probe P2: 300×200 output
with both points present). It survives `MODE_MOVE` re-selection and the
transform modes and is cleared by other modes (`EuclidianView.java:780-823`).
The LaTeX exporters use the selection rectangle or the visible bounds and never
consult `Export_1`/`Export_2`
(`common/export/pstricks/GeoGebraExport.java:240-258`).

**Padding (`AQ-X1`, reproduced in every format).**

| Case | Exact rectangle | PNG s=1 | PNG s=2 | PDF MediaBox (pt) | SVG | EMF / EMF+ bounds |
|---|---|---|---|---|---|---|
| inside viewport | 200×200 px | 202×202 | 404×404 | 114×114 (exact 113.39) | 202×202 px | 202×202 |
| larger than viewport | 1200×900 px | 1202×902 | 2404×1804 | 681×511 (exact 680.31×510.24) | 1202×902 px | 1202×902 |
| disjoint from viewport | 150×150 px | 152×152 | 304×304 | 86×86 (exact 85.04) | 152×152 px | 152×152 |

Cause: `getExportWidth/Height` and `exportPaintPre` add `+2` px
(`EuclidianView.java:5216`, `:5234`, `:5815-5816`), while `getFrame()` uses the
exact rectangle (`:5172-5173`). The 2-px band is on the right and bottom only.
It contains real content outside the rectangle: a point centred at column 201.5
and both axes were painted into it. Objects that clip themselves to the export
frame (lines, segments) stop at the exact edge. PDF additionally truncates the
page size to integer points (`desktop/export/GraphicExportDialog.java:1008-1010`).

**Clipping (`AQ-X1`, reproduced).** The large-area PNG (shared `exportPaint`
path, also used by PDF, EMF, clipboard, GIF and print) **keeps** lines,
segments, rays, vectors, conics, polygons, Locus V2, legacy loci inside their
sampling window, and text outside the viewport. It **loses**:

- points (`common/euclidian/draw/DrawPoint.java:180-187` culls against the
  view size; no PDF/EMF/SVG drawing operation is emitted for an off-view point);
- function graphs beyond the view x-range
  (`common/euclidian/draw/DrawParametricCurve.java:212-224`);
- axes and grid (`DrawAxis.java:160-162`, `DrawGrid.java:129`, `:194`).

**SVG** goes through a separate path that never re-updates drawables against the
export frame (`GraphicExportDialog.java:888-907`). In the probe, segments and
lines were clipped to the on-screen view (path `M -5 550 L 805 550`), and
off-view circles, polygons, lines, Locus V2 and points were missing entirely.

## 3. `ExportArea`: one authority

```text
ExportArea := NONE | Rectangle(worldBounds, sourceViewId, producer)
producer   := EXPORT_POINTS | MANUAL | ISO_A_SELECTION | ISO_A_BORDER
```

- **Owner:** application/session state, never geometry (`AGENTS.md` §4 places
  print and export state in the shared document/application model). Because
  shared code must also obey it, notably the `ExportImage` command
  (`common/kernel/commands/CmdExportImage.java`) and the API
  (`common/plugin/GgbAPI.java`), the candidate is a **shared application-level
  seam**: an export-area provider on `App`, whose default reproduces upstream.
  A GeoCeDG Desktop implementation holds the session state. It is the only
  object any exporter asks for its frame: picture (PNG, PDF, SVG, EMF/EMF+),
  clipboard, print, scripting and API, LaTeX (`C`), and DXF clipping if `C`
  adopts it.
- **Producers delivered by `B`:**
  - `EXPORT_POINTS`: derived live from the document points `Export_1`/`Export_2`
    whenever it is read (the legacy convention stays a document object);
  - `MANUAL`: a world rectangle set by a File action or by editing LaTeX
    bounds; stored values.
- **Producers added by `E3`** (they need `D1`'s unit contract and `B` runs
  before `D1`; `AQ-X7`):
  - `ISO_A_SELECTION`: a corner point, ISO A size, orientation and drawing
    scale, computed once;
  - `ISO_A_BORDER`: linked to an `IsoABorder` object; derived live while the
    link is valid.
- **Resolution rule (`AQ-X2`, recommended):** the producer the user
  activated explicitly; otherwise `EXPORT_POINTS` when both points exist
  (legacy documents keep working); otherwise the visible viewport. **The
  selection rectangle never counts.** There is no hidden precedence chain and
  no second mechanism. LaTeX bound editing writes a `MANUAL` producer instead
  of the selection rectangle.
- **Lifetime:** `SESSION` (author preference; P0 found no contrary evidence). It resets on New and
  Open, after which `EXPORT_POINTS` applies automatically if the document has
  both points. It is not an undo step. Rebuild events (undo, redo, Open, a
  rebuilding redefinition) drop an `ISO_A_BORDER` link, because the Java object
  is replaced and P0 found no persistent identity for ordinary objects. The
  status bar or the File action then shows the fallback (`AQ-E3b`).
- **Display:** a File action toggles a non-exported overlay rectangle in the
  view, on the existing `GeoCeDGEuclidianView.paint` overlay precedent
  (`geocedg-desktop/GeoCeDGEuclidianView.java:38-75`), which never reaches
  exports. It exists only in Graphics 1, the GeoCeDG view class.
- **Degeneracies:** an empty or inverted rectangle is normalized, and a
  zero-area rectangle is refused with a message. Non-finite `Export_1`/`Export_2`
  coordinates mean "no `EXPORT_POINTS` area". An area larger than the device
  limit triggers the existing maximum-size handling with an explicit message,
  never a silent crop.

## 4. Exact and complete rendering (`AQ-X1` implementation)

Candidate, to be prototyped first in `B`: **an offscreen export viewport.** A
GeoCeDG export service creates a temporary 2D view whose pixel size and
coordinate system equal the `ExportArea` at the export scale. It copies the
source view's settings (axes, grid, background, layer filter) and paints through
the ordinary `drawObjects` path. Every view-bounded drawable is then correct by
construction: points, function sampling, axes and grid, labels, and the legacy
locus sampling window. The live view is not touched. The size is exact in
double precision: no `+2`, and PDF page size not truncated. SVG uses the same
viewport and keeps its `layer<n>` groups.

Rejected or fallback alternatives:

- *Per-drawable export-frame awareness* (DrawPoint, curve plotter, axes, grid,
  label clamping and the SVG path). This means many shared upstream edits and
  still leaves the legacy-locus sampling window bound to the live view. It is
  the fallback if the offscreen viewport proves infeasible.
- *Temporarily re-zooming the live view.* Visible flicker, listener churn and
  recomputation of view-dependent algorithms in the user's view.

`GeoLocusV2` tessellates at 0.75 view pixels
(`LocusRenderPolicy2D.java:20-23`), so at high DPI the error grows. The export
viewport re-tessellates at export resolution because it is a real view.

## 5. Export surface

- Profile actions (candidate ids): `export.picture` (one dialog for PNG, PDF,
  SVG and EMF/EMF+, bound to `ExportArea`), `export.pstricks`, `export.pgf`,
  `export.asymptote`, `export.dxf-2d` (existing), `document.print-preview`
  (existing, bound to `ExportArea`), and four area actions
  `export.area.define-rectangle`, `export.area.use-export-points`,
  `export.area.show` and `export.area.clear`. `E3` adds `export.area.iso-a`.
  New registry targets for each. The profile deferral `document.sheet-export`
  (`apps/geocedg/application-profile.yml:3046-3051`) is updated by `B`.
- `AQ-X4` (recommended): Graphics View to Clipboard only through the picture
  dialog's Clipboard button; Animated GIF stays out of this generation; Print
  Preview stays and obeys `ExportArea`.
- **Hidden routes (`AQ-X3`, recommended):** a GeoCeDG override of the Ctrl-key
  handling in `AppGeoCeDG.newGlobalKeyDispatcher`.
  - `Ctrl+Shift+U` opens the GeoCeDG picture action.
  - `Ctrl+Shift+C` goes through `ExportArea`.
  - `Ctrl+Shift+W` and `Ctrl+Shift+M` are consumed with no action (excluded
    worksheet and HTML5).
  - `Ctrl+Shift+B` is the author's choice (a document, not a graphics export).
  - `Ctrl+Shift+D` keeps only the DXF action; the upstream selection toggle is
    consumed. This resolves `OBS-R6P-CTRL-SHIFT-D`.
  - Command-line `--export` is routed through the export service and
    `ExportArea`, or rejected in GeoCeDG. `--exportAnimation` is rejected
    while Animated GIF stays out (`AQ-X5`).
  - Scripting `ExportImage` and the API keep working but resolve their frame
    through the shared seam, so behavior is identical everywhere (`AQ-X6`,
    recommended). This changes the behavior of an upstream command inside
    GeoCeDG. `ExportImage` therefore becomes a GeoCeDG-modified command that
    must enter the `PRE-G9B-R6` GGBScript capability matrix. The alternative —
    scripting keeps the upstream rule, as a documented exception to the single
    authority — avoids that but weakens the single-authority requirement.
  - The v1 fallback still exposes STL, Collada and Worksheet. Leaving it
    unchanged would contravene the author's "do not expose" rule in that
    fail-safe state. Recommended (`AQ-X3`): the fallback menu filters the
    excluded entries too. The alternative is an explicit author acceptance of
    the diagnostic exception.

## 6. Units and scale

`B` keeps the current units. The engineering scale (`1:n`), the replacement of
"Scale in cm" and the physical size come with `D1` and `C`.

## 7. Serialization and compatibility

None. `ExportArea` is session state. `Export_1`, `Export_2` and `IsoABorder`
remain ordinary document objects. Old documents export the same rectangle
2 px smaller, which is the author-required correction. No document byte
changes.

## 8. Verification-class recommendation

`INTEGRATED_PHASE` (registered `PHASE` plus `INTEGRATION`): profile surface,
Desktop dispatcher, the shared export-area seam and export paint path consumed
by picture, clipboard, print, `ExportImage` and API routes, the export
viewport, and the GGBScript matrix rows for `ExportImage` under `AQ-X6`. It would escalate to
`GLOBAL_IMPACT` if the export area became document-serialized, or if the
per-drawable fallback edits shared rendering broadly enough that the frozen
plan must name every view consumer.

# PRE-G9B-R6-plus-B — export surface and `ExportArea` authority

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-02 names `PRE-G9B-R6-plus-B`, its
exact implementation base (the published closeout of the `B` preparation
package, `P_R6PLUS_B_PROMPT`) and the scope of this canonical prompt read
together with the
[B preparation closeout record](../../../docs/validation/pre_g9b_r6_plus_b_prompt_closeout_record.md).
That instruction also authorizes this amendment, as the first tracked edit of
the phase, so that the prompt becomes the executable contract of the phase.
The amendment replaces the prepared prompt published in `P_R6PLUS_B_PROMPT`
(blob `4a1e0bc537885546aa17d4dae5a68e3a7a8920cf`): it records the authorized
base, freezes the verification class, integrates the author dispositions on
`DQ-B1` to `DQ-B9` and the corrections of the closeout record, and adds the
author's explicit raster contract. Where the prepared prompt and the closeout
record differed, the closeout prevailed; this amendment carries that
precedence into the text. Every other scope, forbidden-scope and stop rule of
the prepared prompt is kept. The author decisions are recorded, versioned, in
the
[B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md).
This file is an execution contract, not a second policy document. The design
input is the `P0`
[export-area design candidate](../../../docs/architecture/pre_g9b_r6_plus_b_export_area_design_candidate.md),
as amended by the author decisions and re-characterized below against the
base.

```text
PRE-G9B-R6-plus-B =
AUTHORIZED FOR IMPLEMENTATION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — EXPORT SURFACE, SESSION EXPORT AREA
                           AND SHARED EXPORT PAINT PATH
DEPENDS_ON               = PRE-G9B-R6-plus-P0  = PASS — AUTHOR APPROVED — PUBLISHED
                           PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (AUTHOR_SMOKE = PASS)
DEPENDS_ON_PACKAGE       = B PREPARATION PACKAGE = PASS — AUTHOR APPROVED — PUBLISHED
NEXT_SUBPHASE            = PRE-G9B-R6-plus-D0   (operational order; D0 is
                                                 technically independent)
```

`implementationAuthorized = true` authorizes only the implementation and
technical verification defined below. It authorizes no later subphase.
`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
The phase stops with one exact technically verified candidate pending author
review and author smoke:
`PRE-G9B-R6-plus-B = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`.

<!-- geocedg-field: objective -->
## Objective

Give GeoCeDG one export surface and one export-area authority:

- a GeoCeDG File/Export surface with `Graphics View as Picture…` (PNG, PDF,
  SVG, EMF/EMF+), `DXF…`, `PSTricks…`, `PGF/TikZ…`, `Asymptote…` and
  `Print Preview`, and no STL, Collada, HTML Collada, Dynamic Worksheet /
  worksheet upload, Animated GIF or WebM animation, in the GeoCeDG profile and
  in the v1 fallback;
- one `SESSION` `ExportArea` authority, with the producers `EXPORT_POINTS` and
  `MANUAL`, consumed by every picture route: the Picture dialog and its
  preview, Print Preview, the graphics clipboard, the command line,
  `ExportImage` and the graphics APIs;
- **exact and complete** picture output through an offscreen export viewport:
  the exact rectangle, no outside padding, complete content even outside the
  visible viewport, in PNG, PDF, SVG and EMF/EMF+;
- the hidden routes closed or rerouted (`Ctrl+Shift+U/C/W/M/D`, command line,
  v1 fallback);
- `save-dialog generated preview effective visibility == final export
  effective visibility` (`OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`, as
  precised by the author in the closeout record).

```text
ExportArea := NONE | Rectangle(worldBounds, sourceViewId, producer)
producer   := EXPORT_POINTS | MANUAL           (B)
              ISO_A_SELECTION | ISO_A_BORDER   (E3, not in B)

effectiveArea(view) =
    the explicitly activated producer, when it yields a valid area for view
    else EXPORT_POINTS, when Export_1 and Export_2 are both valid points
    else the visible viewport of view

selectionRectangle  : never an ExportArea authority
```

`B` adds no serialization, does not change units or scale, and does not
integrate the LaTeX or DXF exporters with `ExportArea`; that is `C`.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = INTEGRATED_PHASE        (author-proposed and frozen
                                                on 2026-10-02)
frozenAtPhaseStart   = true
PLANNED_ACCEPTANCE   = registered PHASE  -Phase PRE-G9B-R6-plus-B
                       + INTEGRATION
                       both on the same exact frozen candidate; no FINAL
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` defines
`INTEGRATED_PHASE` as "PHASE; add COMPOSED only when the frozen plan identifies
concrete additional integration coverage", and maps legacy `COMPOSED` to the
`INTEGRATION` profile. This plan identifies that coverage: the change touches
the shared export paint path and export frame of `EuclidianView`, consumed by
the Picture dialog, Print Preview, the graphics clipboard, the command line,
`ExportImage` and the graphics APIs; a new shared application-level
export-area seam; the Desktop key dispatcher; the profile surface and action
registry; the v1 fallback menu; and the GGBScript capability matrix. The
acceptance gate is therefore both the registered PHASE selection and
`INTEGRATION`; neither substitutes for the other. The class is not downgraded
because single edits are small.

Escalation triggers. Stop and report a `VERIFICATION_ESCALATION_REQUEST`,
rather than escalate silently, when:

- `ExportArea`, or any part of it, would be serialized (document XML, undo XML,
  preferences, macro or clipboard content);
- the offscreen export viewport would need a material widening of the shared
  renderer, or the frozen plan would have to name every view consumer;
- the work would fall back to distributed per-drawable modifications as the
  primary strategy (forbidden by `AQ-X1`).

`FINAL` is not run unless such a stop and an explicit author reclassification
require it.

### Author decisions this prompt implements

The author decisions of 2026-10-02, recorded in the
[B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md),
which is their authority:

| ID | Decision (substance) | Effect in `B` |
|---|---|---|
| `AQ-X1` | Primary strategy: an offscreen export viewport whose geometry and coordinates represent the `ExportArea` at output resolution; exact rectangle, zero outside padding, complete content outside the visible viewport, appropriate recomputation of points, functions, axes, grid, labels, legacy loci and Locus V2, SVG from the area. No silent substitution by per-drawable edits; stop and escalate if the viewport is not viable. | implemented |
| `AQ-X2` | `explicitly activated producer > Export_1/Export_2 > visible viewport`; `selectionRectangle` never an authority; `SESSION`, not serialized, not undoable, reset on New/Open, `Export_1`/`Export_2` automatic again afterwards; producers `EXPORT_POINTS` and `MANUAL`. | implemented |
| `AQ-X3` | Surface: Picture (PNG, PDF, SVG, EMF/EMF+), DXF, PSTricks, PGF/TikZ, Asymptote, Print Preview; not STL, Collada, HTML Collada, Worksheet, Animated GIF. Routes: `Ctrl+Shift+U` Picture; `Ctrl+Shift+C` the same service; `W` and `M` consumed; `D` DXF only, double effect removed; `B` kept. The v1 fallback filters STL, Collada, HTML Collada and Worksheet. | implemented |
| `AQ-X4` | Print Preview stays under `ExportArea`; the graphics clipboard is a consumer of the same service and area; Animated GIF out of this generation. | implemented |
| `AQ-X5` | `--export` stays and uses the same service and area; `--exportAnimation` rejected explicitly. | implemented |
| `AQ-X6` | `ExportImage` and the relevant graphics APIs obey `ExportArea`; `ExportImage` becomes GeoCeDG-modified and enters the GGBScript matrix and its tests. | implemented |
| `AQ-X7` | `ISO_A_SELECTION` and `ISO_A_BORDER` in `E3`, after `D1`. | none; `E3` |
| `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | as precised in the closeout record: the preview generated in the save dialog of the Picture export equals the final export in effective visibility, through the same `ExportArea`, hidden-layer state and rendering semantics. | reproduced first, then implemented |
| `OBS-A1-BACKGROUND-IMAGE-LAYER` | re-characterized for preview and export; the general layer semantics of the normal view are not changed silently. | re-characterized; see *Contract details* |

No other open decision of the
[P0 candidate report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15 is resolved by this prompt or by `B`.

### Re-characterization of the design candidate against the base

The preparation re-read every seam this prompt turns into a contract at the
base below. Paths use the `P0` abbreviations (`common/`, `geocedg-common/`, `desktop/`,
`geocedg-desktop/`). Citation drift caused by `A-1` and the profile growth is
corrected; substantive differences from the design candidate are marked
**contradiction** or **new**. The phase re-establishes each one before relying
on it.

| Seam | At the base | Relation to the `P0` candidate |
|---|---|---|
| frame precedence | `getFrame()` returns `selectionRectangle`, else `Export_1`/`Export_2`, else the view (`common/euclidian/EuclidianView.java:5183-5204`); `getExportCoords()` `:5211-5234` | confirmed; lines moved by `A-1` |
| `+2` padding | `getExportWidth()` `:5246`, `getExportHeight()` `:5264`, `exportPaintPre` `:5845-5846` | confirmed |
| frame exactness | `getFrame()` truncates the corners and the size of the `Export_1`/`Export_2` rectangle to `int` (`:5202-5203`); `exportPaintPre` clips with `int` sizes | **contradiction**: the candidate says `getFrame()` uses the exact rectangle; it is exact only to whole source-view pixels |
| selection rectangle clearing | cleared by `setMode` except for the transform modes (`:782-827`) | confirmed |
| shared export paint | `exportPaint` sets the export frame, re-updates drawables, paints through `drawObjects` (`:5891-5912`); `getExportImage` floors `getExportWidth() * scale` (`:5979-5995`) | confirmed |
| `A-1` hidden-layer hook | `drawGeometricObjects` and `drawObjects` filter by `isOnShownLayer` (`:3604-3631`, `:3649-3651`); masks and measurement tools also filtered | **new**: every route through `exportPaint` (PNG, PDF, EMF/EMF+, print, clipboard, view image, API PNG) already omits hidden layers |
| SVG | own path: `exportPaintPre`, a direct `drawActionObjects`, then its own loop over `getAllDrawableList()` with `layer<n>` groups (`desktop/export/GraphicExportDialog.java:859-917`); no drawable re-update against the export frame | confirmed; the loop and the direct call bypass the `A-1` hook, so SVG still writes hidden layers |
| PDF page size | `new Dimension((int) (getExportWidth() * printingScale / factor), …)` (`GraphicExportDialog.java:1008-1010`) | confirmed; the writer takes an integer `Dimension` |
| Picture save-dialog preview (corrected by the closeout record) | `GraphicExportDialog` has no rendered preview component of its own (format, resolution and size label only, `:237-394`). Every `GuiManagerD.showSaveDialog` call, including the PNG, PDF, SVG and EMF calls of the dialog (`GraphicExportDialog.java:636`, `:670`, `:706`, `:749`), sets `GeoGebraFileChooser.MODE_GEOGEBRA_SAVE` (`desktop/gui/GuiManagerD.java:1702-1706`); in that mode the right-hand panel generates `app.getExportImage(THUMBNAIL_PIXELS_X, THUMBNAIL_PIXELS_Y)` during the save flow (`desktop/gui/util/GeoGebraFileChooser.java:474-479`). The same save-mode preview also serves the native document Save | **new**: read from source, that call reaches `exportPaint`, which `A-1` filters, so the source alone does not explain the observation; the phase reproduces the exact author flow first |
| Print Preview | `EuclidianViewD.print` → `exportPaint` (`desktop/euclidian/EuclidianViewD.java:360-372`) | confirmed; already shares the frame rules and the `A-1` hook |
| graphics clipboard | `AppD.copyGraphicsViewToClipboard` clears the object selection and calls `simpleExportToClipboard` → `getExportImage` (`desktop/main/AppD.java:1933-1987`); the dialog's Clipboard button (`GraphicExportDialog.java:370-394`, not shown on macOS) | confirmed |
| hidden shortcuts | `Ctrl+Shift+W` `showWebpageExport` (`common/main/GlobalKeyDispatcher.java:678-688`), `Ctrl+Shift+C` `:719-731`, `Ctrl+Shift+M` `:733-741`, `Ctrl+Shift+B` `:743-749`, `Ctrl+Shift+U` `showGraphicExport` `:801-810` | confirmed |
| `Ctrl+Shift+D` | the upstream branch toggles `selectionAllowed` on every object and fixes or unfixes sliders, leaves `consumed = false` (`GlobalKeyDispatcher.java:900-952`), and the DXF accelerator then fires (`geocedg-desktop/GeoCeDGMenuBar.java:35-36`) | confirmed, and **new**: `selectionAllowed` is serialized (`common/kernel/geos/GeoElement.java:4703-4704`), so the double effect also writes document state |
| GeoCeDG dispatcher | `AppGeoCeDG.newGlobalKeyDispatcher` overrides only Esc (`geocedg-desktop/AppGeoCeDG.java:554-573`) | confirmed; lines moved |
| command line | `checkCommandLineExport` handles `--exportAnimation` (`desktop/gui/app/GeoGebraFrame.java:852-1031`) and `--export` (`:1033-1128`) through the static dialog methods and `getExportWidth` | confirmed |
| `ExportImage` | arguments `view`, `clipboard`, `width`, `height`, `type`, `slider` (`common/kernel/commands/CmdExportImage.java:81-93`); SVG, PDF and PNG through `GgbAPI` | confirmed |
| `ExportImage` animation | `type` `gif` and `webm` call `GgbAPI.exportGIF` / `exportWebM` (`CmdExportImage.java:186-193`), which are empty in shared `GgbAPI` (`common/plugin/GgbAPI.java:2199-2203`) and not overridden by `GgbAPID` | **new**: a silent no-op in GeoCeDG; see `DQ-B1` |
| graphics APIs | `GgbAPID.writePNGtoFile`, `getPNGBase64`, `exportSVG`, `exportPDF` use the view export image or the static dialog methods (`desktop/plugin/GgbAPID.java:141-290`) | confirmed |
| GGBScript matrix inventory | derived from the processor class chain and its provenance in `docs/upstream/modified-files.yml` (`geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`, `inventory_rule`); the derived set is pinned to ten commands (`desktop` test `PreG9BR6CapabilityMatrixTest.java:45-47`) | **new**: `ExportImage` enters the derived inventory only when its own processor chain is GeoCeDG-modified and registered |
| Locus V2 tessellation | `DEFAULT_VISUAL_TOLERANCE_PIXELS = 0.75`, and the render policy carries a `viewId` (`geocedg-common/euclidian/draw/LocusRenderPolicy2D.java:20-24`) | confirmed, and **new**: the export viewport needs its own view identity that never collides with Graphics 1 or 2 and leaves no render cache behind |
| view-bounded drawables | points (`common/euclidian/draw/DrawPoint.java:178-187`), function sampling (`DrawParametricCurve.java:210-224`), axes (`DrawAxis.java:158-162`), grid (`DrawGrid.java:127-131`, `:192-196`) | confirmed |
| LaTeX exporters | use `selectionRectangle` or the visible bounds (`common/export/pstricks/GeoGebraExport.java:246`) | confirmed; unchanged until `C` |
| v1 fallback | the full upstream menu bar (`geocedg-desktop/GuiManagerGeoCeDG.java:33-50`), with the upstream Export submenu: Worksheet, Picture, GIF, Clipboard, PSTricks, PGF, Asymptote, STL, Collada, Collada HTML (`desktop/gui/menubar/FileMenuD.java:147-180`) | confirmed |
| overlay precedent | `GeoCeDGEuclidianView.paint` draws a transient overlay after `super.paint`; `exportPaint` never calls `paint` (`geocedg-desktop/GeoCeDGEuclidianView.java:38-75`) | confirmed |
| profile deferral | `document.sheet-export` (`apps/geocedg/application-profile.yml:3066-3071`); registry targets allow-list (`geocedg-desktop/GeoCeDGActionRegistry.java:61-63`) | confirmed; lines moved |

A contradiction that would change scope, owner, serialization or class stops
the phase.

### Author dispositions on `DQ-B1` to `DQ-B9`

The preparation found details that the author decisions did not settle. The
author disposed of each on 2026-10-02 in the
[B preparation closeout record](../../../docs/validation/pre_g9b_r6_plus_b_prompt_closeout_record.md),
which is their authority and prevails over the defaults of the prepared
prompt.

| ID | Question | Author disposition |
|---|---|---|
| `DQ-B1` | `ExportImage` with `type` `gif` or `webm` is a silent no-op in GeoCeDG. | **accepted**: rejected explicitly while Animated GIF/WebM stay outside the authorized surface; no silent no-op. The empty API methods `exportGIF`/`exportWebM` stay as upstream and are documented |
| `DQ-B2` | `AQ-X3` lists STL, Collada, HTML Collada and Worksheet for the v1 fallback. | **accepted**: the v1 fallback also filters Animated GIF; `Ctrl+Shift+M` is consumed in both profiles because the dispatcher override belongs to `AppGeoCeDG` |
| `DQ-B3` | Screen-anchored objects (sliders, buttons, check boxes, input boxes, drop-down lists, texts and images with an absolute screen position) have no world position. | **replaced**: `screen-anchored → export-canvas / presentation anchored`, never `screen position → live-view world coordinate → export authority`; see *Screen-anchored objects* below |
| `DQ-B4` | Gesture that defines a `MANUAL` area. | **accepted**: a File dialog with world bounds `xmin`, `xmax`, `ymin`, `ymax`, prefilled from the current effective area, with a "use visible view" button; no drag gesture in `B` |
| `DQ-B5` | Which views the producers apply to. | **accepted with a precision**: the initial UI may expose the `MANUAL` definition from Graphics 1 only; `ExportArea` and the shared service never hard-code Graphics 1 as a semantic identity, keep an explicit `sourceViewId` and stay valid for any supported 2D view; `EXPORT_POINTS` may be resolved for the corresponding 2D source view; the 3D-view export stays entirely outside `B` and does not change |
| `DQ-B6` | `Ctrl+Shift+C`: open the Picture surface or copy directly? | **accepted**: the graphics copy runs directly through the common service and the same `ExportArea`; no Picture dialog; not a second authority |
| `DQ-B7` | PSTricks, PGF/TikZ and Asymptote still read `selectionRectangle` until `C`. | **accepted**: their upstream area behavior, including `selectionRectangle`, is kept until `C` as an explicitly documented temporary inconsistency; `B` does not change it |
| `DQ-B8` | The PDF writer takes an integer `Dimension`. | **accepted as an explicit stop condition**: PDF exactness is never obtained by silent rounding or truncation. If the PDF backend cannot represent the required physical size with the exactness of this contract, the phase stops, characterizes the limitation and requests an author decision before introducing any approximation. **Raised and resolved** on 2026-10-02: see *Author resolution of the `DQ-B8` stop* |
| `DQ-B9` | Command-line `--export` ran on a zero-size view and wrote a 0-byte file (`OBS-R6P0-CLI-EXPORT-ZERO-SIZE`). | **accepted**: when no valid area or output can be produced, the export fails explicitly with a message and a non-zero exit and leaves no empty file as an apparently valid result. The viewport fallback uses the stored size of the document's source view |

### Screen-anchored objects (`DQ-B3`, author disposition)

A screen-anchored object is **not** turned into a world-anchored object by
computing its world position from the live viewport.

```text
contract : screen-anchored -> export-canvas / presentation anchored
never    : screen position -> live-view world coordinate -> export authority
```

In the offscreen export viewport the object keeps, where technically
sustainable, its presentation position relative to the output canvas; if it
falls outside the canvas it is clipped normally. Presentation is never turned
into geometry. The phase characterizes explicitly the compatibility with legacy
documents that use `Export_1`/`Export_2`: at the base such an export shifts
screen-anchored objects with the export translation, so their canvas position
can differ from the base. If the backend cannot preserve this semantics
without turning presentation into geometry or without a material widening of
scope, the phase stops and reports the evidence for an author decision.

### Author resolution of the `DQ-B8` stop (2026-10-02)

The phase stopped at the `DQ-B8` gate before any product change. A scratch
probe of the vendored FreeHEP `PDFGraphics2D` showed that the page and canvas
sizes are integer `java.awt.Dimension` values, that `openPage` clips, centres
and fits with those integers, that the writer prints about five significant
digits (`MediaBox 113.39` for 113.3858 pt), and that
`GraphicExportDialog.exportPDF` keeps the FreeHEP defaults `PAGE_MARGINS =
SMALL` and `FIT_TO_PAGE = true`, which shrink the drawing inside the page at the
base. The author recorded the stop as design evidence, not as a failure of `B`,
and decided:

- **PDF.** Integer truncation, silent rounding, implicit FreeHEP margins,
  `FIT_TO_PAGE = true` and removing PDF exactness from `B` are not accepted.
  A minimal remediation of the vendored PDF backend is authorized so that page
  extents and page transforms are held in `double` and an integer
  `java.awt.Dimension` is no longer the authority of the physical size, under a
  prior gate: before any `org/freehep/**` file is modified, the phase
  identifies the most precise origin and revision of the vendored snapshot,
  links it to the applicable license evidence, registers FreeHEP in the
  GeoCeDG licensing and provenance records, preserves copyright and notices,
  and registers every modified FreeHEP file as modified third-party source. If
  provenance or license cannot be established sufficiently, the phase stops
  again before touching FreeHEP. The GeoCeDG route sets `PAGE_MARGINS = 0` and
  `FIT_TO_PAGE = false` explicitly; the content is never reduced, centred with
  implicit margins or shifted to accommodate FreeHEP defaults. The target
  extent derives from `ExportArea` and the current export scale. Tolerance of
  the finite PDF serialization:
  `abs(emittedExtent - requestedExtent) <= 0.001 pt` for each page-box
  dimension. It belongs only to the numeric representation and changes no
  world bounds, geometry, geometric scale, dependency or identity. The report
  records, for the canonical cases, the requested extent, the emitted
  `/MediaBox`, the absolute error, the applied transform, and the absence of
  margins, of `FIT_TO_PAGE`, of crop and of additional scaling.
- **SVG.** An integer `viewBox` is not by itself a stop condition. The world
  bounds are the exact authority and the `viewBox` is an internal canvas
  representation; the export-viewport transform maps the exact world bounds
  onto the SVG canvas without outside padding, crop or deformation, and the
  physical `width` and `height` keep the precision the backend offers. Only a
  probe proving that the integer `viewBox` forces a change of world bounds, a
  deformation, padding or crop stops the phase.
- **EMF / EMF+.** The intrinsic quantization of the integer fields of the
  format is accepted when explicit and bounded: world bounds unchanged, no
  geometric deformation, no crop, no added padding, extents and device units
  quantized to the nearest native representation. For each relevant physical
  field the evidence records the requested value, the emitted value, the
  smallest representable unit and the absolute error; the maximum error is half
  a native unit of the field.
- **Raster.** The raster contract below is kept.
- **`getFrame()` and `+2`.** Neither the integer truncation of `getFrame()` nor
  the `+2` remains an authority: the architecture is
  `ExportArea.worldBounds → output mapping`, never
  `ExportArea → source-view integer rectangle → padding → output`.
- **Characterization first.** Before product changes resume, the phase
  characterizes in the scratchpad the viability of the offscreen export
  viewport, the reproduction and identification of the Save side preview, and
  the canvas-anchored semantics of screen-anchored objects, plus focal SVG and
  EMF/EMF+ probes. It reports and stops immediately if any of them raises
  another stop condition; otherwise it continues without a new confirmation.
- **Class.** `VERIFICATION_CLASS = INTEGRATED_PHASE` is unchanged. A broad
  modification of the library, several `graphicsio` families, a new
  dependency, a backend replacement, or a change that affects consumers outside
  the GeoCeDG export stops the phase for reclassification. A small, focal
  change of the vendored PDF backend, backed by provenance and licensing and by
  specific tests, stays within `B`. `FINAL` is not run without an explicit
  escalation and authorization.

### Raster discretization (author contract of 2026-10-02)

`ExportArea.worldBounds` is the exact geometric authority. A PNG and every
raster output necessarily have integer pixel dimensions; that discretization
belongs exclusively to sampling and rendering. A deterministic, documented rule
obtains the raster grid and, if needed, a sub-pixel adjustment of the
transform, provided that:

- the world bounds of `ExportArea` do not change;
- the four exact bounds of the area map onto the output canvas;
- there is no outside padding and no `+2`;
- the world area is never widened or narrowed to fit the grid;
- there is no silent crop;
- every difference between the requested and the effective resolution caused
  by the integer grid is documented as a sampling property, never as a
  geometric change.

No particular `floor`, `ceil` or `round` is imposed when the existing
architecture allows a better representation; the phase adopts the minimal
deterministic rule compatible with the backend and tests it. If the backend
forces a change of the world bounds to produce the raster, the phase stops and
reports the evidence. This does not relax `DQ-B8`: for PDF no silent rounding
or truncation of the physical size is authorized.

### Contract details fixed by this prompt

Where the author decisions and dispositions above leave a detail open, this
prompt adopts the `P0` design candidate. Each item is a contract of this
prompt, not an author decision.

- **Owner.** `ExportArea` is application/session state, never geometry
  (`AGENTS.md` §4). A minimal export-area provider seam on shared `App`, whose
  default reproduces upstream exactly (including the `selectionRectangle`
  precedence in Classic and Web), is the only object a picture route asks for
  its frame. `AppGeoCeDG` supplies the GeoCeDG implementation and holds the
  session state.
- **Export service.** One GeoCeDG export service builds the export viewport
  and renders every picture route: the Picture dialog for PNG, PDF, SVG and
  EMF/EMF+, its preview, Print Preview, the graphics clipboard, `--export`,
  `ExportImage` and the graphics APIs. There is no second mechanism and no
  route with its own area authority.
- **Export viewport.** A temporary 2D view whose pixel size and coordinate
  system represent the effective area at the output resolution. It copies the
  source view's presentation settings (axes, grid, background colour, layer
  filter, label and point styles, `A-1` hidden layers) and paints through the
  ordinary `drawObjects` path. It is never a kernel view whose attachment would
  change a construction value: view-dependent commands (`Corner`, dynamic view
  bounds) keep evaluating against the source view. It never changes the live
  view, its zoom, its settings, the selection or the undo history, and it is
  disposed after each export, leaving no drawable, render cache or listener.
  It carries its own view identity, which never collides with Graphics 1 or 2,
  and the Locus V2 render caches it creates are released with it.
- **Exact dimensions.** The area width and height in world units times the
  source view scale and the export scale give the exact output size. PDF uses
  it within `0.001 pt` per page-box dimension, SVG maps the exact bounds onto
  its canvas, EMF/EMF+ quantize their integer fields by at most half a native
  unit, and raster outputs follow the *Raster discretization* contract, all as
  decided in the *Author resolution of the `DQ-B8` stop*. There is no `+2`.
- **Producers.**
  - `EXPORT_POINTS` is derived live from `Export_1` and `Export_2` whenever it
    is read. It is valid only when both labels name finite `GeoPoint` objects
    with a non-zero-area rectangle.
  - `MANUAL` stores a world rectangle. Defining it normalizes an inverted
    rectangle, refuses a zero-area or non-finite one with a message, and makes
    it the explicitly activated producer.
- **File area actions.** `export.area.define-rectangle` (`MANUAL`),
  `export.area.use-export-points` (activates `EXPORT_POINTS` explicitly),
  `export.area.show` (toggles the overlay) and `export.area.clear` (no explicit
  producer: back to the automatic resolution). An explicitly activated producer
  that becomes invalid (for example `Export_2` deleted) resolves to the next
  rule and is reported in the File action state; it is never silently
  replaced by a stale rectangle.
- **Lifecycle.** New and Open clear the explicit producer and the stored
  `MANUAL` rectangle and hide the overlay. Undo, redo and redefinition do not
  change the session state; `EXPORT_POINTS` follows the rebuilt points.
  Defining, activating, showing or clearing creates no undo point and does not
  mark the document modified.
- **Overlay.** A transient rectangle in Graphics 1 on the
  `GeoCeDGEuclidianView.paint` precedent. It is presentation only, not
  geometry and not a construction object. It never reaches any export,
  preview, print, clipboard, command-line or API output.
- **Save-dialog preview (author precision in the closeout record).** The
  observed preview appears when Save is run from the Picture export window, in
  the right-hand panel of the dialog that chooses the file name and path; it
  is generated during the save flow of the pending export, not read from an
  earlier file. The phase reproduces the exact flow
  (`Picture export → Save → file chooser → right-hand preview panel`) before
  changing anything and establishes which chooser mode and route generate the
  preview. Contract:
  `save-dialog generated preview effective visibility == final export effective visibility`.
  With an `A-1` hidden layer, the side preview omits its objects and the PNG,
  PDF, SVG or EMF file applies the same effective visibility. The preview
  reuses, directly or indirectly, the same `ExportArea` authority, hidden-layer
  state and effective rendering semantics as the pending export. It is never
  resolved by hiding the panel, by presenting it as the preview of an earlier
  file, or by a second independent render route with different semantics. If
  the panel uses another route than inferred, the phase corrects that route
  with the minimal change and documents the evidence; if the observation cannot
  be reproduced, or the real route contradicts the characterization, the phase
  stops and reports instead of inventing a correction. The same save-mode
  preview serves the native document Save; that consumer is preserved and
  tested. The general layer semantics outside the export surface are not
  widened.
- **Views (`DQ-B5`).** `ExportArea` carries an explicit `sourceViewId`. The
  service resolves producers per 2D source view and never hard-codes Graphics 1
  as a semantic identity; only the initial `MANUAL` UI is limited to
  Graphics 1. The 3D-view export is outside `B` and unchanged.
- **Hidden layers.** Every picture format, SVG included, applies the `A-1`
  effective-visibility predicate exactly as the normal view does. SVG keeps its
  `layer<n>` groups for the layers it writes.
- **Background images (`OBS-A1-BACKGROUND-IMAGE-LAYER`).** The phase reports how
  background images behave in the normal view, the preview and every picture
  format with their layer hidden. The preview equals the final file in every
  case. The normal-view layer semantics stay as `A-1` left them; if aligning
  them would widen those semantics, the phase documents and defers, or requests
  an author decision.
- **Shortcuts.** One GeoCeDG override of the Ctrl-key handling in
  `AppGeoCeDG.newGlobalKeyDispatcher`: `U` opens the GeoCeDG Picture surface;
  `C` copies through the same service (`DQ-B6`); `W` and `M` are consumed with
  no action; `D` runs only the GeoCeDG DXF action, so no object's
  `selectionAllowed` or slider fixing is written; `B` keeps the upstream
  Base64 copy.
- **Command line.** `--export` renders through the export service and the
  effective area of the opened document (`DQ-B9`). `--exportAnimation` is
  rejected with a message and a non-zero exit and writes no file.
- **`ExportImage` animation (`DQ-B1`).** `type=gif` and `type=webm` are
  rejected with a localized error in GeoCeDG; Classic keeps the upstream
  behavior.
- **v1 fallback.** The fallback menu filters STL, Collada, HTML Collada,
  Worksheet and Animated GIF (`DQ-B2`). Its remaining picture, clipboard and
  print entries reach the same export service and `ExportArea`.
- **Status and messages.** Every refused area, rejected route and fallback is
  reported with a localized message (at least `en` and `es`); nothing is
  cropped or skipped silently. An area above the device limit triggers the
  existing maximum-size handling with an explicit message.

### Temporary export inconsistency between `B` and `C`

`B` exposes PSTricks, PGF/TikZ, Asymptote and DXF in the File/Export surface
but does not anticipate `C`. After `B`:

| Export | Area | Objects on hidden layers | Owner of the final behavior |
|---|---|---|---|
| PNG, PDF, SVG, EMF/EMF+, preview, print, clipboard, command line, `ExportImage`, graphics APIs | the effective `ExportArea`, exact and complete | omitted | `B` |
| PSTricks, PGF/TikZ, Asymptote | `selectionRectangle` or the visible bounds, as at the base | written, as at the base | `C` (exclusion decided by `AQ-L3`) |
| DXF | whole 2D geometry, as at the base | written, as at the base | `C` (policy open; clipping by `ExportArea`) |
| Locus V2 / Spline V2 in LaTeX | sampled, as at the base | — | `C` (semantic export) |
| units and engineering scale | as at the base | — | `D1`, `C` |

The phase documents this inconsistency in its candidate report, in the living
user and developer documentation it touches, and in its author-smoke checklist.
It does not correct it.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
f6194f09358de9c5f8ac5051c31b3a9dc8a9491b          (P_R6PLUS_B_PROMPT, published
                                                    closeout of the B preparation package)

IMPLEMENTATION_BASE_TREE =
9ac09c5371bce7d9b093541f232b43464073118b

B_PREPARATION_PACKAGE_STATE =
PASS — AUTHOR APPROVED — PUBLISHED

B_PREPARATION_APPROVED_CANDIDATE =
7e00e451167b0d915b061655c478c2faac99e51c          (T_R6PLUS_B_PROMPT)

A1_STATE =
PASS — AUTHOR APPROVED — PUBLISHED                 (AUTHOR_SMOKE = PASS;
                                                    T_R6PLUS_A1 2a71133ad622e18e9963b04a466a92665a784722)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal `IMPLEMENTATION_BASE`, its tree equals
`IMPLEMENTATION_BASE_TREE`, and the worktree is clean. The phase works on the
new local branch `phase/pre-g9b-r6-plus-b-export-surface` from that commit and
is not rebased onto any later commit without a new author instruction. Between
`d32ad608ba8821bc18c9d4dc783c1b700a165ff2`, where the seams above were
re-characterized, and this base only documentation changed, so the citations
still apply; the phase re-establishes every one it relies on before using it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at the base.
3. The [B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md)
   and the [B preparation closeout record](../../../docs/validation/pre_g9b_r6_plus_b_prompt_closeout_record.md),
   whose dispositions prevail over any contradicting default of the prepared
   prompt.
4. Design input, re-characterized before use: the `P0` export-area design
   candidate §2–§8, the
   [P0 report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
   findings 5–9, probes P1 and P2 and §16, the
   [A-1 closeout record](../../../docs/validation/pre_g9b_r6_plus_a1_closeout_record.md)
   (observations and the log-root rule) and the
   [A-1 candidate report](../../../docs/validation/pre_g9b_r6_plus_a1_candidate_report.md)
   §6 and §9. Candidate §3's `ISO_A_SELECTION` and `ISO_A_BORDER` belong to
   `E3`; candidate §6 (units and scale) belongs to `D1` and `C`. None of them is
   implemented here.
5. Generated artifacts, earlier reports and previous agent output are
   evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Export-area authority

- The shared export-area provider seam on `App`, with an upstream-identical
  default, and the minimal consumers of it in `EuclidianView`'s export frame
  (`getFrame`, `getExportWidth`, `getExportHeight`, `exportPaintPre`,
  `exportPaint`) that the export viewport needs. The `+2` and the `int`
  truncation are removed only on the GeoCeDG path; the Classic default keeps
  the upstream values.
- An `AppGeoCeDG`-owned `ExportArea` session service with the producers
  `EXPORT_POINTS` and `MANUAL`, the resolution rule, the lifecycle and the File
  area actions above.
- The non-exported overlay in `GeoCeDGEuclidianView`.

### Export viewport and service

- A GeoCeDG export service and offscreen export viewport in
  `geocedg-desktop`, with the narrowest shared seams needed to construct a 2D
  view that is not attached to the kernel as a construction view, to give it
  its own view identity, and to dispose of it.
- The Desktop picture writers in `desktop/export/GraphicExportDialog.java`
  (PNG, PDF, SVG, EMF/EMF+, clipboard), changed only so that they render from
  the export service; SVG writes from the export viewport through the `A-1`
  predicate and keeps its layer groups.
- The Picture dialog wired to the export service and the effective area,
  including any preview it shows.
- After the provenance and licensing gate, the minimal focal remediation of
  the vendored FreeHEP PDF backend (`org/freehep/graphicsio/pdf/**`) that holds
  page extents and page transforms in `double`, opt-in for the GeoCeDG route so
  that every other caller keeps the base output; the FreeHEP licensing and
  provenance records; the modified-file registration of every FreeHEP file
  changed.

### Surface and routes

- Profile actions and registry targets (candidate identifiers
  `export.picture`, `export.pstricks`, `export.pgf`, `export.asymptote`,
  `export.area.define-rectangle`, `export.area.use-export-points`,
  `export.area.show`, `export.area.clear`), with the existing
  `export.dxf-2d` and `document.print-preview` placed in the same File/Export
  surface; the `document.sheet-export` deferral updated to the delivered truth.
  The action-count, File-group and group-order pins and the tests that assert
  them change together and only by the actions `B` adds.
- The `AppGeoCeDG` Ctrl-key override for `U`, `C`, `W`, `M` and `D`.
- Print Preview, the graphics clipboard, command-line `--export` and the
  rejection of `--exportAnimation` routed through the export service
  (`desktop/gui/app/GeoGebraFrame.java`, minimally).
- `ExportImage` (`CmdExportImage`) and the relevant graphics APIs
  (`GgbAPI`/`GgbAPID` `writePNGtoFile`, `getPNGBase64`, `exportSVG`,
  `exportPDF`) resolving their frame through the seam; the `DQ-B1` rejection.
- The v1 fallback menu filter in `GuiManagerGeoCeDG`.
- Localized strings for at least `en` and `es`.

### Supporting changes

- Tests for every obligation below, registered in
  `docs/upstream/modified-files.yml` where they live under `source/`.
- Registration of the upstream files modified by this phase in
  `docs/upstream/modified-files.yml`, with the narrowest correct rationale;
  `CmdExportImage` registered as modified so that `ExportImage` enters the
  derived GGBScript inventory.
- The `ExportImage` rows of the GGBScript capability matrix
  (`GEOCEDG_MODIFIED`, every surface and language of the matrix) and its
  derived-inventory pin, through the existing schema and gate.
- The registered `PRE-G9B-R6-plus-B` PHASE selection in the typed verification
  registry, covering every focal obligation below, with the JUnit inventory and
  registry-shape pins it needs, through the existing mechanisms and the
  official updater.
- Living documentation that describes the new behavior and the temporary
  inconsistency (user guide in both editions, developer guide, the export and
  workspace specifications it touches), plus the candidate report and its
  machine-readable evidence.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any serialization of `ExportArea`, its producer, the `MANUAL` rectangle or
  the overlay state: no document element, XML attribute, undo-XML content,
  preferences key, macro or clipboard content.
- `ExportArea` as part of undo, or any write of object visibility, object
  layer, `selectionAllowed`, slider fixing or other document state by an
  export, an area action or a shortcut.
- Distributed per-drawable modifications (`DrawPoint`, the curve plotter,
  axes, grid, label clamping, the SVG loop) as the primary strategy, or any
  silent substitution of the export viewport (`AQ-X1`).
- Temporarily re-zooming or otherwise mutating the live view to export.
- Any change to the meaning of the `A-1` effective-visibility predicate, or to
  the general layer semantics of the normal views.
- `selectionRectangle` as an `ExportArea` authority on any GeoCeDG picture
  route.
- The `ISO_A_SELECTION` and `ISO_A_BORDER` producers and `IsoABorder` (`E3`).
- The LaTeX exporters' area, their hidden-layer exclusion, semantic Locus V2 /
  Spline V2 export, DXF clipping and DXF hidden-layer policy (`C`).
- Units, engineering scale, "Scale in cm" and physical size (`D1`, `C`).
- Exposing or reopening STL, Collada, HTML Collada, Dynamic Worksheet /
  worksheet upload, the HTML5 clipboard export, Animated GIF or WebM animation,
  in any profile or mode.
- Turning a screen-anchored object's presentation position into geometry, or
  deriving its export position from live-view world coordinates (`DQ-B3`).
- Widening or narrowing the world bounds of `ExportArea` to fit a raster grid,
  or any silent rounding or truncation of the PDF physical size (`DQ-B8`).
- Resolving the save-dialog preview by hiding the panel, by presenting it as an
  earlier file, or by a second render route with different semantics.
- Changes to the 3D view or its picture export, to Classic or Web behavior, or
  to the Classic diagnostic session, beyond upstream-identical seam defaults.
- Every other subphase (`D0`, `D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`,
  `G`), `PRE-G9B-R7` and `G9B`.
- Changes to the verifier, its schemas, `prompt-contracts.json`, the GGBScript
  matrix schema or gate logic, the governance layer or `tools/agent/**`, beyond
  registry, inventory and matrix data updated through existing mechanisms.
- Any commit, move, rename, rewrite or copy of `artifacts/author-input/**`.

## Architectural placement

- `ExportArea`, its producers, the overlay and the area actions: application
  and session state and Desktop presentation (`AGENTS.md` §4: print and export
  state belong to the shared document/application model, never to geometric
  truth). They are not geometry.
- The export-area provider: a minimal overridable seam in shared `App`,
  because shared code (`ExportImage`, `GgbAPI`, `EuclidianView`) must obey it.
  It carries no CeDG policy; `AppGeoCeDG` supplies it.
- The export service and viewport: Desktop GeoCeDG export layer, an export
  adapter outside the kernel (`AGENTS.md` §4, §13). It reads geometry; it never
  computes or changes it.
- Shortcuts, surface and fallback filter: Desktop GeoCeDG application layer
  and profile.

Each shared seam is the narrowest that serves the behavior and is recorded in
`docs/upstream/modified-files.yml`. If an owner is wrong at the base, stop and
report instead of choosing another layer.

## Required design/specification

The `P0` design candidate, amended by the author-decision record and by the
re-characterization above, is the design. Before implementation, the phase
prototypes the export viewport and records in its candidate report: its
feasibility evidence, every shared seam it needs, every place where the base
contradicts the candidate, and the correction it adopts within this scope. A
contradiction that would change scope, owner, serialization or class stops the
phase. The export specification the phase touches records `ExportArea` as
`SESSION` and `DERIVED_TRANSIENT` for the overlay.

## Geometric invariants and degeneracies

`B` changes no geometric definition, algorithm, dependency or numeric value.
For every construction, the kernel state, the construction XML and every
computed value are identical before, during and after any export, area action
or shortcut. Rendering data is never metric authority.

| Case | Required behavior |
|---|---|
| area inside the viewport | exact size; content equals the view's content in the area |
| area larger than the viewport | exact size; complete content, including points, function graphs, axes, grid and labels outside the view |
| area completely disjoint from the viewport | exact size; complete content of the area |
| `Export_1`/`Export_2` inverted or in any order | normalized rectangle |
| `Export_1` and `Export_2` coincident, collinear horizontally or vertically | zero area: no `EXPORT_POINTS` area; next rule; message |
| one of the points missing, undefined, non-finite or not a point | no `EXPORT_POINTS` area; next rule |
| leftover `selectionRectangle` | ignored on every GeoCeDG picture route |
| `MANUAL` zero-area or non-finite input | refused with a message; previous state kept |
| area above the device limit | existing maximum-size handling with an explicit message; never a silent crop |
| object on a hidden layer | omitted in preview and in every picture format |
| screen-anchored object | anchored to the export canvas at its presentation position; clipped normally outside the canvas; never converted through live-view world coordinates (`DQ-B3`) |
| raster output | integer grid by a documented deterministic rule; the four exact world bounds map onto the canvas; world bounds unchanged |
| Locus V2 at high export resolution | tessellated for the export viewport, not reused from the live view |
| legacy locus | sampled for the export viewport window |
| view-dependent command (`Corner`) | value unchanged by the export |
| overlay shown | never present in any output |

## Compatibility and serialization

No serialization change. Documents saved after `B` are byte-identical to the
base for the same construction. Legacy `.cedg` and `.ggb` documents open and
re-save unchanged; a legacy document with `Export_1`/`Export_2` exports the same
rectangle without the 2-px band, which is the author-required correction; the
canvas position of its screen-anchored objects follows `DQ-B3` and is
characterized against the base. Older
GeoCeDG builds and Classic read `B` documents unchanged. No feature flag,
migration or format version is introduced. Classic and Web keep the upstream
frame rules through the seam defaults.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation, and the registered PHASE selection
executes all of them:

| ID | Obligation |
|---|---|
| `T-VIEWPORT` | the export viewport is built, painted and disposed without changing the live view, its settings, the selection, the undo history or any kernel value; no drawable, render cache or listener remains; its view identity never collides with Graphics 1 or 2 and its Locus V2 render caches are released |
| `T-EXACT-SIZE` | exact output size for inside, larger and disjoint areas in PNG (two scales), PDF, SVG, EMF and EMF+; zero outside padding; no `+2` |
| `T-RASTER-BOUNDS` | the documented raster rule: world bounds unchanged, the four exact bounds mapped onto the canvas, no padding, no crop; requested versus effective resolution recorded as a sampling property |
| `T-PDF-EXACT` | for the canonical cases: requested extent, emitted `/MediaBox`, absolute error `<= 0.001 pt` per dimension, applied transform, no margins, no `FIT_TO_PAGE`, no crop and no additional scaling |
| `T-SVG-EXACT` | the exact world bounds map onto the SVG canvas without padding, crop or deformation; the physical `width` and `height` keep the backend precision |
| `T-EMF-QUANTIZATION` | for each relevant EMF/EMF+ field: requested value, emitted value, smallest native unit and absolute error `<= 0.5` unit; no crop, padding or deformation |
| `T-FREEHEP-CLASSIC` | the remediated PDF backend reproduces the base output byte for byte for every caller that does not opt in, including Classic |
| `T-CONTENT` | points; lines, segments, rays and vectors; conics; polygons; functions; axes; grid; labels and text; legacy Locus; Locus V2 at export resolution — present and complete for inside, larger and disjoint areas in every picture format, with nothing outside the rectangle |
| `T-SVG-AREA` | SVG is written from the area, not clipped to the view, with its layer groups |
| `T-HIDDEN-LAYERS` | every picture format, SVG included, and the preview omit objects on hidden layers; showing the layer restores them |
| `T-PREVIEW-FINAL` | first reproduces the author flow `Picture export → Save → file chooser → right-hand preview panel` and identifies its chooser mode and route; then the save-dialog generated preview equals the final export in effective visibility and area, with an `A-1` hidden layer |
| `T-NATIVE-SAVE-PREVIEW` | the native document Save preview, which shares the save-mode route, keeps its behavior |
| `T-SCREEN-ANCHORED` | screen-anchored objects keep their presentation position relative to the output canvas and are clipped normally; never converted through live-view world coordinates; the legacy `Export_1`/`Export_2` difference against the base is characterized |
| `T-BACKGROUND-IMAGE` | the re-characterization of `OBS-A1-BACKGROUND-IMAGE-LAYER`; preview equals final; normal-view semantics unchanged |
| `T-PRECEDENCE` | explicit producer > `EXPORT_POINTS` > viewport on every route; `selectionRectangle`, including a leftover one, ignored |
| `T-EXPORT-POINTS` | live derivation; degeneracies; New and Open reactivate it automatically when both points are valid |
| `T-MANUAL` | define, normalize, refuse, activate, clear; New and Open reset; undo, redo and redefinition leave it unchanged; no undo point; document not marked modified |
| `T-NO-SERIALIZATION` | document, undo, preferences, macro and clipboard XML unchanged by any area state; a document re-saves byte-identically |
| `T-OVERLAY` | shown in Graphics 1; absent from every output and preview |
| `T-SURFACE` | the GeoCeDG File/Export entries and their order; the excluded entries absent; profile and registry pins; the `document.sheet-export` deferral |
| `T-SHORTCUTS` | `U` Picture; `C` copy through the service; `W` and `M` consumed with no action; `D` DXF only, single effect, no `selectionAllowed` or slider write; `B` Base64 unchanged |
| `T-PRINT` | Print Preview renders the effective area and effective visibility |
| `T-CLIPBOARD` | the dialog button and `Ctrl+Shift+C` produce the effective area through the same service |
| `T-CLI` | `--export` writes the effective area of the opened document; the `DQ-B9` failure path; `--exportAnimation` rejected with a non-zero exit and no file |
| `T-EXPORTIMAGE` | PNG, SVG, PDF and the clipboard argument obey the effective area; `type=gif` and `type=webm` rejected explicitly (`DQ-B1`); the matrix rows and the derived-inventory pin |
| `T-API` | `writePNGtoFile`, `getPNGBase64`, `exportSVG` and `exportPDF` obey the effective area; `exportPGF`, `exportPSTricks` and `exportAsymptote` unchanged |
| `T-FALLBACK-V1` | the fallback filters STL, Collada, HTML Collada, Worksheet and Animated GIF; its remaining picture, clipboard and print entries use the service |
| `T-LEGACY` | legacy documents with `Export_1`/`Export_2` export the exact rectangle and re-save byte-identically |
| `T-CLASSIC` | upstream defaults of every new shared seam reproduce base behavior, including the `selectionRectangle` precedence |
| `T-INTERIM-C` | PSTricks, PGF/TikZ, Asymptote and DXF produce the same output as at the base (records the `B`/`C` boundary) |
| `T-DETERMINISM` | two exports of the same state are identical, apart from documented writer-generated identifiers |
| `T-SMOKE` | an explicit author-smoke checklist, including the temporary inconsistency; the agent does not perform author smoke |

Desktop tests mock `JOptionPane`, inject the spatial-redefine presentation
where a redefine dialog could open, wait for the asynchronous undo store, and
never fire `Ctrl+Shift+C`, `Ctrl+Shift+M` or `Ctrl+Shift+B` against the real
system clipboard; clipboard routes are tested through injected sinks.

Then the adjacent export, print, clipboard, command-line, scripting, API,
profile, `A-1` layer and GGBScript-matrix regressions; Checkstyle; the
upstream-boundary validation; the JUnit inventory and registry pins through the
official updater with executed selection evidence; and `git diff --check`.

Acceptance reproduces the `INTEGRATED_PHASE` contract of
`verification-levels.md` §12.8: the registered PHASE selection, plus COMPOSED
(`INTEGRATION`) because this plan names concrete additional integration
coverage. On one frozen clean immutable candidate, run both:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-B -LogDirectory <fresh root; may be outside the repository>
tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts/agent/<fresh>
```

Log roots, correcting the `A-1` lesson from the start:

```text
PHASE and STATIC:      -LogDirectory may be a fresh root outside the repository
INTEGRATION and FINAL: -LogDirectory must be artifacts/agent/<fresh> inside the
                       repository, as packaging.repository-safety requires
console log of every run: outside artifacts/ (for example the session scratchpad)
```

Both must be `ACCEPTED / COMPLETE` on the same exact commit and tree. The
PHASE run authenticates the phase-named obligations; `INTEGRATION` adds the
repository-level integration coverage. Neither substitutes for the other, a
narrow focal or DEV PASS substitutes for neither, and neither is repeated for
an unchanged candidate. `FINAL` is not run; if the implemented scope would need
it, the phase stops first. Report exact commands, exit codes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author instruction of 2026-10-02 authorizes only the scope above: local
implementation, local commits, tests, the ordinary registry and inventory
updates `B` needs, the registered PHASE run plus `INTEGRATION`, and one frozen
technical candidate for author review and smoke. It does not authorize `FINAL`
unless a stop or escalation condition is first raised and a new explicit author
authorization follows.

`B` authorizes nothing that follows it. The operational order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`; it is the order the
author chose, not a dependency, and `D0` may run in parallel with `B`. `D0`,
`D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B`
stay unauthorized. Author approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local branch from the base and local commits only. Push,
branch publication, merge, promotion to `main`, rebase, squash, amend after
freeze, force push, tag, release and binary publication are forbidden; each
needs a separate explicit author instruction naming the exact candidate SHA.
Acceptance evidence never grants publication authority.

## Acceptance and closeout

`B` stops with one technically verified candidate pending author review and
author smoke. Author approval is an explicit decision naming the exact accepted
commit. The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The implementation, tests and registrations above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  export-viewport feasibility evidence and its shared seams with their
  upstream-identical defaults; the re-characterized citations and any
  correction to the design candidate; the reproduction and correction of the
  save-dialog preview reported in `A-1`; the raster discretization rule
  implemented; the PDF exactness evidence; the screen-anchored behavior and its
  legacy characterization; the background-image re-characterization; the
  obligation-to-test map; the size table (inside, larger, disjoint × every
  format) against the `P0` probe P2 table; the temporary `B`/`C` inconsistency
  table; the route table (surface, shortcuts, command line, `ExportImage`,
  APIs, fallback); the byte-identity evidence; residual risks and
  observations; the author-smoke checklist.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale, the
  verification-infrastructure-impact assessment, `GUIDE_IMPACT` with paths,
  and the exact PHASE and `INTEGRATION` commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- the offscreen export viewport is not viable without a material widening of
  the shared renderer or of the verification class (`AQ-X1`);
- the export viewport cannot be built without attaching it to the kernel in a
  way that changes a construction value, or without mutating the live view;
- any part of `ExportArea` would be serialized or enter undo;
- the save-dialog preview reported in `A-1` cannot be reproduced or
  identified, or its real route contradicts the characterization;
- the provenance or license of the vendored FreeHEP snapshot cannot be
  established sufficiently before an `org/freehep/**` file is modified;
- the PDF remediation needs a broad library change, several `graphicsio`
  families, a new dependency, a backend replacement, or a change visible to
  consumers outside the GeoCeDG export (reclassification);
- the PDF page box cannot meet `0.001 pt` per dimension;
- an SVG probe proves that the integer `viewBox` forces a change of world
  bounds, a deformation, padding or crop;
- the raster backend would force a change of the `ExportArea` world bounds;
- the canvas-anchored semantics of screen-anchored objects cannot be preserved
  without turning presentation into geometry or materially widening scope
  (`DQ-B3`);
- the work would fall back to distributed per-drawable modifications as its
  primary strategy;
- `ExportImage` cannot enter the derived GGBScript inventory through the
  existing inventory rule, schema and gate;
- resolving `OBS-A1-BACKGROUND-IMAGE-LAYER` would widen the normal-view layer
  semantics;
- an excluded surface cannot be closed in some profile or mode;
- the registered PHASE selection cannot be created through existing
  mechanisms;
- current governance requires a verification class other than
  `INTEGRATED_PHASE`;
- the work would belong to `C`, `D0`, `D1`, `A-2`, `E3` or later.

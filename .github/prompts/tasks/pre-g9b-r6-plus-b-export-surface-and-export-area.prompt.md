# PRE-G9B-R6-plus-B — export surface and `ExportArea` authority

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-02. That
instruction fixed the decisions for `PRE-G9B-R6-plus-B`, named the exact
published base below, and authorized only the documentary preparation of this
prompt, the record of the decisions and the status updates they require. It
stated that it **does not authorize implementation**. The decisions are
recorded, versioned, in the
[B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-B` and confirms or replaces the base below. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `P0` and `A-1` precedent. This
file is an execution contract, not a second policy document. The design input
is the `P0`
[export-area design candidate](../../../docs/architecture/pre_g9b_r6_plus_b_export_area_design_candidate.md),
as amended by the author decisions and re-characterized below against the
base.

```text
PRE-G9B-R6-plus-B =
PREPARED — NOT AUTHORIZED

selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — EXPORT SURFACE, SESSION EXPORT AREA
                           AND SHARED EXPORT PAINT PATH
DEPENDS_ON               = PRE-G9B-R6-plus-P0  = PASS — AUTHOR APPROVED — PUBLISHED
                           PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (AUTHOR_SMOKE = PASS)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-D0   (operational order; D0 is
                                                 technically independent)
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Give GeoCeDG one export surface and one export-area authority:

- a GeoCeDG File/Export surface with `Graphics View as Picture…` (PNG, PDF,
  SVG, EMF/EMF+), `DXF…`, `PSTricks…`, `PGF/TikZ…`, `Asymptote…` and
  `Print Preview`, and no STL, Collada, HTML Collada, Dynamic Worksheet /
  worksheet upload or Animated GIF, in the GeoCeDG profile and in the v1
  fallback;
- one `SESSION` `ExportArea` authority, with the producers `EXPORT_POINTS` and
  `MANUAL`, consumed by every picture route: the Picture dialog and its
  preview, Print Preview, the graphics clipboard, the command line,
  `ExportImage` and the graphics APIs;
- **exact and complete** picture output through an offscreen export viewport:
  the exact rectangle, no outside padding, complete content even outside the
  visible viewport, in PNG, PDF, SVG and EMF/EMF+;
- the hidden routes closed or rerouted (`Ctrl+Shift+U/C/W/M/D`, command line,
  v1 fallback);
- `preview effective visibility == final export effective visibility`
  (`OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`).

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
VERIFICATION_CLASS   = INTEGRATED_PHASE        (author-proposed on 2026-10-02;
                                                frozen at authorization)
frozenAtPhaseStart   = REQUIRED AT AUTHORIZATION
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
| `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` | preview effective visibility equals final export effective visibility. | implemented |
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
| Picture dialog preview | `GraphicExportDialog` has no rendered preview component (format, resolution and size label only, `:237-394`). The only image preview found on the Picture route is the file chooser's preview panel, which shows an image file **already on disk** (`desktop/gui/util/GeoGebraFileChooser.java:440-500`, `MyImageD.fromFile`); its save-mode thumbnail uses `getActiveEuclidianViewExportImage`, which goes through `exportPaint` | **new**: the source does not explain the preview reported in `A-1`; the phase reproduces and identifies it first |
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

### Decisions requested before authorization

The preparation found details that the author decisions do not settle. For
each, this prompt fixes a **default contract**. The authorizing instruction may
confirm or replace it; without an explicit replacement the default applies.
None is an author decision until the author makes it.

| ID | Question | Default contract of this prompt |
|---|---|---|
| `DQ-B1` | `ExportImage` with `type` `gif` or `webm` is a silent no-op in GeoCeDG. | rejected explicitly with a localized error, consistent with `AQ-X4` and `AQ-X5`; the empty API methods `exportGIF`/`exportWebM` stay as upstream and are documented |
| `DQ-B2` | `AQ-X3` lists STL, Collada, HTML Collada and Worksheet for the v1 fallback. Animated GIF and the HTML5 clipboard export are also expressly excluded. | the v1 fallback also filters Animated GIF; `Ctrl+Shift+M` is consumed in both profiles because the dispatcher override belongs to `AppGeoCeDG` |
| `DQ-B3` | Screen-anchored objects (sliders, buttons, check boxes, input boxes, drop-down lists, texts and images with an absolute screen position) have no world position. | rendered at the world position that corresponds to their current screen position in the source view, so an area inside the visible viewport reproduces the base, and they appear only when that position lies in the area |
| `DQ-B4` | Gesture that defines a `MANUAL` area. | a File dialog with world bounds `xmin`, `xmax`, `ymin`, `ymax`, prefilled from the current effective area, with a "use visible view" button; no drag gesture in `B` |
| `DQ-B5` | Which views the producers apply to. | `MANUAL` is bound to Graphics 1 (`sourceViewId`); `EXPORT_POINTS` applies to any exported 2D view, as at the base; another 2D view without an applicable producer uses its visible viewport; the 3D-view picture export is unchanged and documented |
| `DQ-B6` | `Ctrl+Shift+C`: open the Picture surface or copy directly? | copy the effective area directly as PNG through the same export service, identical to the dialog's Clipboard button at its default settings; no dialog |
| `DQ-B7` | PSTricks, PGF/TikZ and Asymptote still read `selectionRectangle` until `C`. | unchanged in `B` and documented as the temporary inconsistency; the LaTeX exporters' area belongs to `C` |
| `DQ-B8` | The PDF writer takes an integer `Dimension`. | the phase first establishes whether the vendored PDF writer can represent the exact page size; if it cannot, it stops and reports before choosing any rounding policy |
| `DQ-B9` | Command-line `--export` ran on a zero-size view and wrote a 0-byte file (`OBS-R6P0-CLI-EXPORT-ZERO-SIZE`). | the viewport fallback uses the stored size of the document's Graphics 1; when no valid area can be resolved, the export fails with a message and a non-zero exit and writes no file |

### Contract details fixed by this prompt

Where the author decisions and the requested decisions above leave a detail
open, this prompt adopts the `P0` design candidate. Each item is a contract of
the prepared prompt, not an author decision, and the authorizing instruction
may change it.

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
- **Exact dimensions.** The area width and height in world units times the
  source view scale and the export scale give the exact output size. Vector
  formats (PDF, SVG, EMF/EMF+) use that size within the writer's representable
  precision (`DQ-B8`). A raster image has
  `round(exactWidth) × round(exactHeight)` pixels, and its transform maps the
  area exactly onto that grid; the sub-pixel adjustment is deterministic,
  smaller than one output pixel, and documented. There is no `+2`.
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
- **Preview.** Any preview the GeoCeDG Picture surface shows is rendered by the
  export service from the same effective area and effective visibility as the
  final file. A chooser preview of an image file already on disk is not
  presented as a preview of the export.
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
d32ad608ba8821bc18c9d4dc783c1b700a165ff2          (P_R6PLUS_A1, published A-1 closeout)

IMPLEMENTATION_BASE_TREE =
9b123be7ed8c7176b5a7e19f74be5946f0164c8b

A1_STATE =
PASS — AUTHOR APPROVED — PUBLISHED                 (AUTHOR_SMOKE = PASS)

A1_APPROVED_TECHNICAL_CANDIDATE =
2a71133ad622e18e9963b04a466a92665a784722          (T_R6PLUS_A1)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal the base the authorizing instruction names, its tree
matches, and the worktree is clean. If the documentary commit that publishes
this prompt and the author-decision record is published first, the authorizing
instruction names that commit instead; this prompt assumes no later base on its
own. The phase works on a new local branch from the named commit and is not
rebased onto any later commit without a new author instruction. Between the
base and that documentary commit only documentation changes, so the citations
above still apply; the phase re-establishes every one it relies on before using
it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at the base.
3. The [B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md).
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
  worksheet upload, the HTML5 clipboard export or Animated GIF, in any profile
  or mode.
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
| screen-anchored object | `DQ-B3` |
| Locus V2 at high export resolution | tessellated for the export viewport, not reused from the live view |
| legacy locus | sampled for the export viewport window |
| view-dependent command (`Corner`) | value unchanged by the export |
| overlay shown | never present in any output |

## Compatibility and serialization

No serialization change. Documents saved after `B` are byte-identical to the
base for the same construction. Legacy `.cedg` and `.ggb` documents open and
re-save unchanged; a legacy document with `Export_1`/`Export_2` exports the same
rectangle without the 2-px band, which is the author-required correction. Older
GeoCeDG builds and Classic read `B` documents unchanged. No feature flag,
migration or format version is introduced. Classic and Web keep the upstream
frame rules through the seam defaults.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation, and the registered PHASE selection
executes all of them:

| ID | Obligation |
|---|---|
| `T-VIEWPORT` | the export viewport is built, painted and disposed without changing the live view, its settings, the selection, the undo history or any kernel value; no drawable, render cache or listener remains |
| `T-EXACT-SIZE` | exact output size for inside, larger and disjoint areas in PNG (two scales), PDF, SVG, EMF and EMF+; no `+2`; the raster rounding rule; the PDF page size per `DQ-B8` |
| `T-CONTENT` | points; lines, segments, rays and vectors; conics; polygons; functions; axes; grid; labels and text; legacy Locus; Locus V2 at export resolution — present and complete for inside, larger and disjoint areas in every picture format, with nothing outside the rectangle |
| `T-SVG-AREA` | SVG is written from the area, not clipped to the view, with its layer groups |
| `T-HIDDEN-LAYERS` | every picture format, SVG included, and the preview omit objects on hidden layers; showing the layer restores them |
| `T-PREVIEW-FINAL` | first reproduces and identifies the preview reported in `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`; then preview effective visibility equals final export effective visibility |
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
| `T-EXPORTIMAGE` | PNG, SVG, PDF and the clipboard argument obey the effective area; `DQ-B1`; the matrix rows and the derived-inventory pin |
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

Nothing in this file is authorized. The author decisions it records do not
authorize implementation. Execution requires a new explicit author instruction
naming `PRE-G9B-R6-plus-B` and its exact base. That instruction would
authorize only the scope above: local implementation, local commits, technical
verification and one frozen technical candidate for author review and smoke.

`B` authorizes nothing that follows it. The operational order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`; it is the order the
author chose, not a dependency, and `D0` may run in parallel with `B`. `D0`,
`D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B`
stay unauthorized. Author approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Once authorized: a local branch from the base and local commits only. Push,
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
  correction to the design candidate; the identification of the preview
  reported in `A-1`; the background-image re-characterization; the
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
- the preview reported in `A-1` cannot be reproduced or identified;
- the PDF writer cannot represent the exact page size (`DQ-B8`);
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

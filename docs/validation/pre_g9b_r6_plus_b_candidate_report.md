# PRE-G9B-R6-plus-B — export surface and `ExportArea` authority: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-B
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-B
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)
PRODUCT CHANGE            = YES (Desktop export surface, export service and
                            session ExportArea; shared export seams)
SERIALIZATION CHANGE      = NONE
GUIDE_IMPACT              = UPDATED
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval. The candidate commit cannot name
itself, so its identity and the two acceptance runs made on it are reported
outside this file. The machine-readable mirror is
[`pre-g9b-r6-plus-b-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-B` for implementation and technical
verification on 2026-10-02, exclusively for the scope of its canonical prompt
and of the [prompt closeout record](pre_g9b_r6_plus_b_prompt_closeout_record.md),
whose dispositions prevail over the prompt's defaults.

| Identity | Value |
|---|---|
| implementation base `P_R6PLUS_B_PROMPT` | `f6194f09358de9c5f8ac5051c31b3a9dc8a9491b` |
| base tree | `9ac09c5371bce7d9b093541f232b43464073118b` |
| local `main` = `origin/main` = live remote `main` (`git ls-remote`) at entry | `f6194f09358de9c5f8ac5051c31b3a9dc8a9491b` |
| worktree at entry | clean |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-plus-b-export-surface` |

Entry cleanup (`git branch -d` only, merged into `main`, no remote deletion):
`phase/pre-g9b-r6-plus-a1-layer-workspace` (`d32ad608b`),
`phase/pre-g9b-r6-plus-a1-prompt` (`069c7a03d`),
`phase/pre-g9b-r6-plus-b-prompt` (`f6194f093`) and
`phase/pre-g9b-r6-plus-p0-characterization-design` (`562e2bb1e`). Kept:
`main`, `feature/g9u1-construction-workspace-planning`,
`feature/g9u1-construction-workspace-planning-after-r6` and
`maintenance/retire-orphaned-historical-verification-suites`.

## 2. Canonical prompt amendments

Three tracked edits of
`.github/prompts/tasks/pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md`,
each in its own local commit and each before the first product change it
governs:

| Commit | Content | Prompt blob |
|---|---|---|
| base `f6194f09` | prepared prompt | `4a1e0bc537885546aa17d4dae5a68e3a7a8920cf` |
| `fe70c5ac` | `AUTHORIZED`, `implementationAuthorized = true`, the base and tree above, `INTEGRATED_PHASE` frozen, the `DQ-B` dispositions and the raster contract | `d13b3cb182c0fed4b21bab30e074c697ec58f423` |
| `dd6d517c` | author resolution of the `DQ-B8` stop (PDF remediation under a provenance gate, SVG and EMF rules, stop conditions) | `4ac70712bad82e6d5825c8fce2ed71f8b21742bc` |
| `574808a0` | author resolution of the characterization stops (legacy Locus option a, EMF option c, conservative PDF provenance, SVG `viewBox`, Save preview, screen-anchored objects, viewport, class) | `feb0fb0d2e987477045c124f51216d11130f9083` |

`selfApproved`, `authorApproved` and `passClaimed` stay `false` throughout.

## 3. What changed

### 3.1 Shared seams (upstream files, host-identical defaults)

| Seam | Host default | GeoCeDG answer |
|---|---|---|
| `App.getExportFrameWidth(view)` / `getExportFrameHeight(view)` | `view.getExportWidth()` / `getExportHeight()` | the exact pixel extent of the effective `ExportArea` |
| `App.isAnimatedExportAvailable()` | `true` | `false` |
| `EuclidianView.drawActionObjectsOnShownLayers(g)` | the `A-1` filtered action pass, now also used by `drawObjects` | used by the SVG groups of the export viewport |
| `CmdExportImage` | sizes through the frame seam; `gif`/`webm` refused only where `isAnimatedExportAvailable()` is `false`, through `org.geocedg.common.export.PictureExportPolicy` | `IllegalArgument` |
| `LocusRenderPolicy2D.adaptiveFrom` (GeoCeDG-owned) | resolution 1 when not exporting | tolerance and budget follow the export scale (>1 only) |

### 3.2 Modified third-party source: FreeHEP `PDFGraphics2D`

Provenance was established before the edit (prompt gate): FreeHEP
VectorGraphics 2.0 snapshot (FreeHEP revision `36ad488`, 2006-11-12), LGPL as
recorded by the release notes, no Apache-2.0 attribution inferred for this
snapshot; [provenance record](../licensing/freehep-vectorgraphics-provenance.md)
with its [machine-readable mirror](../../geocedg/validation/pre-g9b-r6-plus/freehep-vectorgraphics-provenance.json),
`THIRD_PARTY.md` and the component matrix updated; copyright and notice kept;
the file registered as modified third-party source. Only
`org/freehep/graphicsio/pdf/PDFGraphics2D.java` changed: an opt-in
`setExactPageSize(width, height)` that holds the page extent in `double`; when
set, the page box, the page transform, every transform and every rectangular
clip are written with nine decimals instead of five significant digits. A
writer that does not opt in is byte-identical to the base writer (§8). No other
FreeHEP family changed; the EMF writer is untouched.

### 3.3 Desktop upstream routing (host path unchanged)

`AppD` adds `getPictureExportRoute()` (none), `getSavePreviewImage(extension, …)`
(the host thumbnail) and `isUpstreamExportEntryAvailable(entry)` (every entry).
Consumers: the static `GraphicExportDialog` writers and size label,
`PrintScalePanel`, `EuclidianViewD.print`, `GgbAPID` (`writePNGtoFile`, the
clipboard PNG and `getPNGBase64`), `GeoGebraFrame` (`--export`,
`--exportAnimation`), `GuiManagerD.showSaveDialog` with
`GeoGebraFileChooser` (format context and `refreshPreview`), and the v1
fallback `FileMenuD`. `SVGExtensions` gains an opt-in exact fractional
`viewBox`.

### 3.4 GeoCeDG-owned code

`org.geocedg.desktop.export`: `ExportArea`, `ExportAreaSession`,
`ExportViewport`, `PictureExportRoute`, `PictureExportService`,
`PictureExportCommandLine`, `ExportAreaUnavailableException`;
`GeoCeDGExportAreaPrompt`; `AppGeoCeDG` (session, service, seams, clipboard,
area actions, Ctrl-key override, New/Open reset); `GeoCeDGActionRegistry`
(eight actions); `GeoCeDGEuclidianView` (overlay); `GeoCeDGProfile` (124
actions); the profile manifest (eight actions, the File/Export groups and their
`en`/`es` texts, the `document.sheet-export` deferral).

## 4. Offscreen export viewport

Every picture is painted by a fresh `ExportViewport`, an `EuclidianViewD` with
its own `EuclidianControllerD`, `EVNO_GENERAL`, no settings object (presentation
copied through setters; `settingsChanged(settings)` is never called), view id
`0x40000000` and its own drawables. `attachView` is a no-op: the kernel never
sees it, so there is no kernel view-bound change, no `notifyEuclidianViewCE`
and no recomputation. Its coordinate system represents the area at the source
scale; `getFrame` is the canvas, `getSelectionRectangle` is `null`, and the
clip is the exact area. The Desktop `GGraphics2DD.setClip(double…)` truncates to
`int`, so the clip is a rectangle shape (found by the PDF content check: an
area of 200.15 × 150.355 px was clipped to 200 × 150). Visibility follows the
source view. The viewport is discarded after each export; the live view's
drawables, Locus V2 caches, selection, undo history, saved flag and kernel
bounds are unchanged (`T-VIEWPORT`).

## 5. Save preview: reproduction and fix

Route: every `GuiManagerD.showSaveDialog` sets `MODE_GEOGEBRA_SAVE`, and that
mode's right-hand panel renders `app.getExportImage(…)` on
`SELECTED_FILE_CHANGED` for an existing file. A scratch probe on the real
chooser reproduced the author's observation: after a first preview with the
layer shown (404 red pixels), hiding the layer and reopening Save with the same
file left the preview at 404 red pixels, because the reused chooser fires no
selection event for an unchanged file; selecting another file and back gave 0.
The route itself was correct; the thumbnail was stale.

Fix: `showSaveDialog` gives the chooser its format and, when the application
has a picture route, calls `refreshPreview()` on every opening;
`refreshPreview` regenerates the preview of the pending save (the selected file,
or the name the file-name field keeps after the selection is reset to `null`)
and clears it when there is nothing to preview. The save-mode panel asks
`getSavePreviewImage(format, …)`: picture formats render through the picture
service with the current `ExportArea` and hidden layers; the native document
Save keeps the host thumbnail. Regression `T-SAVE-PREVIEW-STALE` replays the
author flow through the real `showSaveDialog` with a recording chooser.

## 6. Raster rule

`gridWidth = max(1, round(w·s))`, `gridHeight = max(1, round(h·s))`, where `w`
and `h` are the exact pixel extents of the area at the source scale and `s` the
requested scale; the canvas is mapped by `scale(gridWidth / w, gridHeight / h)`,
so the four exact world bounds land on the four image edges. The world bounds
never change; there is no padding, no `+2`, no widening or narrowing and no
crop. The effective resolution differs from the requested one only by the
integer grid, per axis, below `0.5 / (w·s)` relative (§9).

## 7. PDF exactness

Route: page size in points = exact pixel extent × `printingScale · 72 / (2.54 ·
xscale)`; instance properties `PAGE_MARGINS = 0` and `FIT_TO_PAGE = false`;
`setExactPageSize`. Evidence for the fractional case 200.15 × 150.355 px:

| Item | Value |
|---|---|
| requested extent | 113.470866141732 × 85.240629921260 pt |
| emitted `/MediaBox` | `[0 0 113.470866142 85.240629921]` |
| absolute error | 2.7 × 10⁻¹⁰ pt (bound 0.001 pt) |
| page transform | `1 0 0 -1 0 85.240629921 cm` (a pure flip: no margin, no fit, no centring) |
| page clip | `0 0 113.470866142 85.240629921 re` |
| viewport transform | `0.566929134 0 0 0.566929134 0 0 cm` (points per pixel; no additional scaling) |
| area clip | `0 0 200.15 150.355 re` (no crop) |

The page boxes of the canonical areas are in §9.

## 8. Byte identity

- **FreeHEP writer without opt-in.** A host-style PDF (default page, margins and
  fit) written by the base `PDFGraphics2D` compiled apart from the tree and by
  the candidate has the same normalized SHA-256
  `d392d83957e4724d34c81efd18a814ba33761234a5039cc8f8a4a91ea219fbb7`
  (creation date normalized); pinned by `T-FREEHEP-CLASSIC`.
- **LaTeX and DXF.** PGF/TikZ, PSTricks, Asymptote and DXF of a fixed scene give
  the same SHA-256 on a `git archive` of `f6194f09` and on the candidate, with
  and without a MANUAL area (`T-INTERIM-C`).
- **Documents.** The `geogebra.xml` entry re-saves identically with any area
  state (`T-NO-SERIALIZATION`, `T-LEGACY`).
- **Classic.** Every new seam keeps the base behavior, including the host
  `selectionRectangle` precedence (`T-CLASSIC`).

## 9. Size table against the P0 probe P2

Same configuration as P0 probe P2: view 800 × 600 at 50 px/unit, `printingScale
= 1`.

| Case (world area) | Exact (px) | P0 base PNG s=1 / s=2 | B PNG s=1 / s=2 / s=3.7 | P0 base PDF page | B PDF `/MediaBox` (error) | P0 base SVG / EMF | B SVG `viewBox` | B EMF/EMF+ `rclBounds` s=1 / s=2.5 |
|---|---|---|---|---|---|---|---|---|
| inside (−2,−2)–(2,2) | 200×200 | 202×202 / 404×404 | 200×200 / 400×400 / 740×740 | 114×114 (113.39) | 113.385826772 × 113.385826772 (3.5×10⁻¹⁰ pt) | 202×202 / 202×202 | `0 0 200.0 200.0` | 200×200 / 500×500 |
| larger (−12,−9)–(12,9) | 1200×900 | 1202×902 / 2404×1804 | 1200×900 / 2400×1800 / 4440×3330 | 681×511 | 680.31496063 × 510.236220472 (4.4×10⁻¹⁰ pt) | 1202×902 / 1202×902 | `0 0 1200.0 900.0` | 1200×900 / 3000×2250 |
| disjoint (9,6)–(12,9) | 150×150 | 152×152 / 304×304 | 150×150 / 300×300 / 555×555 | 86×86 (85.04) | 85.039370079 × 85.039370079 (2.6×10⁻¹⁰ pt) | 152×152 / 152×152 | `0 0 150.0 150.0` | 150×150 / 375×375 |
| fractional (20,10)–(24.003,13.0071) | 200.15×150.355 | — | 200×150 / 400×301 / 741×556 | — | 113.470866142 × 85.240629921 (2.7×10⁻¹⁰ pt) | — | `0 0 200.15 150.35499999999996` | 200×150 / 500×376 |

No `+2` band, no padding and no crop in any format; the base truncated the PDF
page to whole points and added the band everywhere else. For the fractional
case the raster grid is the nearest integer of the exact extent: requested
200.15 × 150.355, 400.3 × 300.71 and 740.555 × 556.3135 px; effective scale per
axis 0.99925/0.99764, 1.99850/2.00193 and 3.70222/3.69791 for the requested 1,
2 and 3.7 — a sampling property only.

## 10. SVG and EMF

**SVG.** The root `viewBox` is the exact area in source pixels and the
physical size is the same rectangle: `width="7.060847222222222cm"
height="5.304190277777776cm" viewBox="0 0 200.15 150.35499999999996"` for the
fractional case at 1/72 in per pixel (`200.15px` × `150.35499999999996px`
without a physical size). Width/height and `viewBox` have the same aspect ratio,
no `preserveAspectRatio` is written, every drawing matrix is isotropic, and the
drawing is the export viewport at scale 1, so world bounds, padding, crop and
deformation are untouched.

**EMF/EMF+.** `rclBounds` = the exact device extent rounded to whole units,
with a drawing scale equal in both axes:

| Case | requested device extent | `rclBounds` | `rclFrame` (0.01 mm) |
|---|---|---|---|
| fractional, s=1 | 200.15 × 150.355 | 0,0,200,150 | 0,0,6250,4687 |
| fractional, s=2.5 | 500.375 × 375.8875 | 0,0,500,376 | 0,0,15625,11750 |
| inside, s=1 | 200 × 200 | 0,0,200,200 | 0,0,6250,6250 |
| larger, s=2.5 | 3000 × 2250 | 0,0,3000,2250 | 0,0,93750,70312 |
| disjoint, s=1 | 150 × 150 | 0,0,150,150 | 0,0,4687,4687 |

`rclFrame` follows the FreeHEP EMF writer, which derives it from the integer
device bounds; it is outside the `B` contract
(`OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION`, owner `C`).

## 11. Screen-anchored objects

Objects with an absolute screen position keep it relative to the export canvas
(`screen-anchored → export-canvas`), are clipped normally and are never
converted through live-view world coordinates. Legacy characterization (the
same scratch probe on a `git archive` of the base and on the candidate's
unchanged host path): with `Export_1`/`Export_2` at (0,−1)–(4,3) the host
picture is 202 × 202 (the `+2` band) and a text anchored at (10,20) is absent,
because the host translates the canvas by the area corner; the GeoCeDG picture
is 200 × 200 and shows the text at (10,20).

## 12. Background images and legacy Locus

- **`OBS-A1-BACKGROUND-IMAGE-LAYER` re-characterized.** Background images are
  drawn by the background pass, which is not layered. With their layer hidden
  they stay visible in the normal view (as `A-1` left it), in the Save preview
  and in every picture: the preview equals the final file. The normal-view
  semantics are unchanged; aligning them would widen the layer semantics, so it
  is documented, not done.
- **`OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE`.** A legacy `GeoLocus` is drawn only
  from the samples the construction already holds; its points, the construction
  XML and the kernel view bounds are unchanged by an export, and no
  recomputation happens. Its coverage outside the sampling window is not
  claimed. Locus V2 is tessellated for the export resolution.

## 13. Temporary `B`/`C` inconsistency

| Export | Area | Objects on hidden layers | Final owner |
|---|---|---|---|
| PNG, PDF, SVG, EMF/EMF+, preview, print, clipboard, `--export`, `ExportImage`, graphics APIs | effective `ExportArea`, exact and complete | omitted | `B` (this candidate) |
| PSTricks, PGF/TikZ, Asymptote | selection rectangle or visible bounds, as at the base | written, as at the base | `C` |
| DXF | whole 2D geometry, as at the base | written, as at the base | `C` |
| Locus V2 / Spline V2 in LaTeX | sampled, as at the base | — | `C` |
| units and engineering scale | as at the base | — | `D1`, `C` |

Documented in user guide §4.5, §12.5 and §15 (both editions), the developer
guide and the author-smoke checklist.

## 14. Route table

| Route | Reaches |
|---|---|
| File → Import and export → Graphics View as Picture… (PNG, PDF, SVG, EMF/EMF+) | the picture service through the static `GraphicExportDialog` writers; size label through the frame seam |
| Save dialog preview | `getSavePreviewImage` → the service (picture formats); host thumbnail (native document) |
| Print preview… | `EuclidianViewD.print` → the service |
| Clipboard: dialog button, `Ctrl`+`Shift`+`C`, v1 fallback entry | `AppGeoCeDG.copyGraphicsViewToClipboard` → the service (`copyToClipboard`) |
| `Ctrl`+`Shift`+`U` | host branch → `showGraphicExport`, the same as the profile action |
| `Ctrl`+`Shift`+`W`, `Ctrl`+`Shift`+`M` | consumed, no action |
| `Ctrl`+`Shift`+`D` | the GeoCeDG DXF action only; no `selectionAllowed` or slider write |
| `Ctrl`+`Shift`+`B` | host Base64 copy, unchanged |
| `--export` | `PictureExportCommandLine` → the service; temporary file, non-zero exit and no empty file on failure |
| `--exportAnimation` | refused, non-zero exit, no file |
| `ExportImage` PNG/SVG/PDF, clipboard argument | `GgbAPID` → the service; `gif`/`webm` refused (`DQ-B1`) |
| `writePNGtoFile`, `getPNGBase64`, `exportSVG`, `exportPDF` | the service |
| `exportPGF`, `exportPSTricks`, `exportAsymptote` | unchanged (`C`) |
| v1 fallback File menu | STL, Collada, HTML Collada, Dynamic Worksheet/upload and Animated GIF not created; picture, clipboard and print entries reach the service |
| File → Export area | `defineManual`, `useExportPoints`, overlay, `clear` on the session |

## 15. Obligation → test map

| Obligation | Test(s) |
|---|---|
| `T-VIEWPORT` | `PreG9BR6PlusBPictureFidelityTest.theViewportLeavesTheLiveViewAndTheConstructionUntouched`, `theViewportHasItsOwnIdentityAndController` |
| `T-EXACT-SIZE` | `…PictureFidelityTest.exportPointsAreasHaveExactSizesWithoutTheHostBand` |
| `T-RASTER-BOUNDS` | `…PictureFidelityTest.theRasterGridMapsTheExactWorldBoundsWithoutPaddingOrCrop` |
| `T-PDF-EXACT` | `…PictureFidelityTest.thePdfPageBoxIsExactWithoutMarginsOrFitting` |
| `T-SVG-EXACT` | `…PictureFidelityTest.theSvgViewBoxIsTheExactAreaWithTheSameAspectAndNoAnisotropy` |
| `T-EMF-QUANTIZATION` | `…PictureFidelityTest.emfBoundsAreTheNearestDeviceUnitsOfTheExactArea` |
| `T-FREEHEP-CLASSIC` | `…PictureFidelityTest.theFreeHepPdfWriterWithoutOptInKeepsTheHostOutput` |
| `T-CONTENT` | `…PictureFidelityTest.everyDrawableKindIsCompleteInADisjointArea`, `axesAndGridAreCompleteInALargerArea`, `locusV2IsTessellatedAtTheExportResolution`, `legacyLocusIsRenderedOnlyFromItsExistingSamples` |
| `T-SVG-AREA` | `…PictureFidelityTest.everyDrawableKindIsCompleteInADisjointArea`, `everyPictureFormatOmitsHiddenLayers` (layer groups) |
| `T-HIDDEN-LAYERS` | `…PictureFidelityTest.everyPictureFormatOmitsHiddenLayers`; `PreG9BR6PlusBExportSurfaceTest.theSavePreviewIsTheFinalExportInAreaAndVisibility`, `printPreviewRendersTheEffectiveAreaAndVisibility`; `PreG9BR6PlusA1HiddenLayerTest.svgLatexAndDxfAreUnchangedByAHiddenLayerUntilBAndC` |
| `T-PREVIEW-FINAL` | `…ExportSurfaceTest.theSavePreviewIsTheFinalExportInAreaAndVisibility` |
| `T-NATIVE-SAVE-PREVIEW` | `…ExportSurfaceTest.theNativeSavePreviewKeepsTheHostThumbnail` |
| `T-SAVE-PREVIEW-STALE` | `…ExportSurfaceTest.reopeningSaveWithTheSameNameRegeneratesThePreview` |
| `T-SCREEN-ANCHORED` | `…PictureFidelityTest.screenAnchoredObjectsStayAnchoredToTheExportCanvas` (with the base characterization of §11) |
| `T-BACKGROUND-IMAGE` | `…ExportSurfaceTest.backgroundImagesFollowTheBackgroundPassInLiveViewPreviewAndExport` |
| `T-PRECEDENCE` | `…ExportSurfaceTest.anExplicitProducerBeatsExportPointsWhichBeatTheViewport`, `everyRouteRendersTheSameEffectiveArea` |
| `T-EXPORT-POINTS` | `…ExportSurfaceTest.exportPointsAreDerivedLiveAndDegeneraciesFallBack` |
| `T-MANUAL` | `…ExportSurfaceTest.theManualAreaIsSessionStateWithoutUndoOrModification` |
| `T-NO-SERIALIZATION` | `…ExportSurfaceTest.noAreaStateReachesTheDocumentUndoOrClipboard` |
| `T-OVERLAY` | `…ExportSurfaceTest.theOverlayIsShownLiveAndAbsentFromEveryOutput` |
| `T-SURFACE` | `…ExportSurfaceTest.theFileExportSurfaceOffersOnlyTheAuthorizedEntries`; `G9U1WorkspaceSurfaceTest`, `G9U1ActionRegistryTest`, `G9U1ProfileCompilerTest`, `GeoCeDGProfileTest` (124-action pins and File order) |
| `T-SHORTCUTS` | `…ExportSurfaceTest.hiddenExportShortcutsReachOnlyTheirGeoCeDGRoutes` (W, M, D); `C` through `everyRouteRendersTheSameEffectiveArea` |
| `T-PRINT` | `…ExportSurfaceTest.printPreviewRendersTheEffectiveAreaAndVisibility` |
| `T-CLIPBOARD` | `…ExportSurfaceTest.everyRouteRendersTheSameEffectiveArea`, `theV1FallbackFiltersTheExcludedEntriesAndKeepsTheServiceRoutes` |
| `T-CLI` | `…ExportSurfaceTest.theCommandLineWritesTheEffectiveAreaOrFailsWithoutAFile`, `theCommandLineExportsADocumentWhoseViewIsNotLaidOut`; process-level check (§19) |
| `T-EXPORTIMAGE` | `…ExportSurfaceTest.everyRouteRendersTheSameEffectiveArea`, `exportImageRejectsAnimatedTypesExplicitly`; `PreG9BR6CapabilityMatrixTest.exportImageRows`, `completenessCoversTheDerivedInventory`, `lookupProbesEnglish`, `lookupProbesSpanish` |
| `T-API` | `…ExportSurfaceTest.everyRouteRendersTheSameEffectiveArea`, `latexAndDxfKeepTheBaseOutputWhatEverTheAreaUntilC` |
| `T-FALLBACK-V1` | `…ExportSurfaceTest.theV1FallbackFiltersTheExcludedEntriesAndKeepsTheServiceRoutes` |
| `T-LEGACY` | `…ExportSurfaceTest.legacyExportPointDocumentsExportTheExactRectangleAndResaveIdentically` |
| `T-CLASSIC` | `…ExportSurfaceTest.theUpstreamDefaultsOfTheNewSeamsKeepTheBaseBehavior`; `CommandsTest.cmdExportImage` (shared host) |
| `T-INTERIM-C` | `…ExportSurfaceTest.latexAndDxfKeepTheBaseOutputWhatEverTheAreaUntilC`; `PreG9BR6PlusA1HiddenLayerTest.svgLatexAndDxfAreUnchangedByAHiddenLayerUntilBAndC` |
| `T-DETERMINISM` | `…PictureFidelityTest.picturesAreDeterministic` |
| `T-SMOKE` | §17 |

Not exercised at runtime, by design: `Ctrl`+`Shift`+`U` (opens the Picture
dialog; it delegates to the host branch that calls the same `showGraphicExport`
as the profile action), `Ctrl`+`Shift`+`B` and the `ExportImage` clipboard
argument (they write the real system clipboard; the clipboard argument consumes
the `getPNGBase64` output that is tested), the `GeoGebraFrame` wiring of
`--export` and `--exportAnimation` in JUnit (it ends the JVM;
`PictureExportCommandLine` and the refusal condition are tested in JUnit, and
the wiring by the process-level check of §19), and the real print dialog
(`EuclidianViewD.print` is driven with a mocked print preview).

## 16. Deviations and interpretations

1. **`PictureExportPolicy` (shared, GeoCeDG-owned).** The existing inventory rule
   admits a modified processor only when its source references an
   `org.geocedg.` package. The `DQ-B1` rule therefore lives in
   `org.geocedg.common.export.PictureExportPolicy`, which `CmdExportImage`
   imports; no schema or gate changed. The six `ExportImage` rows record the
   refusal (`UNSUPPORTED_TRUTHFUL` / `IllegalArgument`) because the matrix
   vocabulary cannot express a successful command that creates no object; the
   supported types are covered by the focal tests.
2. **PDF exactness beyond the page box.** With five significant digits, the page
   clip and the viewport transform could crop or scale the area by more than
   0.001 pt on larger pages. The opt-in path therefore also writes transforms and
   rectangular clips with nine decimals. Path coordinates keep the writer's
   format (`OBS-B-FREEHEP-PATH-COORDINATE-PRECISION`, §18).
3. **Viewport clip.** A rectangle-shape clip replaces `setClip(double…)`, which
   truncates in Desktop (§4).
4. **Single area.** `useExportPoints` discards a stored MANUAL rectangle; the
   session holds one area.
5. **Save online.** Treated as part of the excluded Dynamic Worksheet/upload in
   the v1 fallback.
6. **Profile-authority allow-list.** `tools/agent/workspace-profile-validation.ps1`
   approves the eight new action ids, following the `A-1` precedent.
7. **No new export specification.** The G5/DXF specifications are not touched
   (DXF is unchanged); the picture contract is documented in the developer guide
   and this report.
8. **Printing scale of a view that is not laid out.** A process-level
   `--export` check found that the opened document's view has no printing scale
   yet (`printingScale = 0`, computed by the host only once the view has a
   size), which made the export scale 0: the first candidate tree wrote a 1 × 1
   PNG, and the base wrote the 0-byte file of `OBS-R6P0-CLI-EXPORT-ZERO-SIZE`
   for the same reason. `ExportViewport.printingScaleOf` returns the view's
   printing scale when it has one, otherwise the value of the host rule for its
   scale, without changing the view; `PictureExportCommandLine` and the PDF page
   use it, and a non-positive export scale fails explicitly. Regression
   `theCommandLineExportsADocumentWhoseViewIsNotLaidOut`.

## 17. Author-smoke checklist (not performed or attributed by the agent)

1. File → Import and export lists Graphics View as Picture…, Export 2D geometry
   as DXF (experimental)…, PSTricks…, PGF/TikZ…, Asymptote…; File → Export area
   lists its four actions; no STL, Collada, worksheet, upload, Animated GIF or
   WebM entry anywhere (also in the v1 fallback, if reachable).
2. With `Export_1`/`Export_2` around content partly outside the window, export
   PNG, PDF, SVG and EMF: each shows exactly the rectangle, complete, with no
   margin; the PDF page and the SVG `viewBox` match it.
3. Define an export area (File → Export area → Define export area…) away from
   the window, show it, export: it wins over `Export_1`/`Export_2`; clear it;
   New and Open forget it; the document is not marked modified.
4. Hide a layer: PNG, PDF, SVG, EMF, print preview and `Ctrl`+`Shift`+`C` omit
   it. Export once with the layer visible, close, hide the layer, reopen Save
   with the same name: the preview is updated.
5. A text fixed at a screen position stays at that position in the picture.
6. `Ctrl`+`Shift`+`W` and `Ctrl`+`Shift`+`M` do nothing; `Ctrl`+`Shift`+`D` opens
   the DXF dialog only; `Ctrl`+`Shift`+`U` opens the Picture dialog.
7. `ExportImage("type","gif")` reports an error; `--exportAnimation` exits with
   an error and writes no file.
8. Temporary inconsistency: PSTricks, PGF/TikZ, Asymptote and DXF ignore the
   export area and still write hidden layers.
9. A legacy `Locus` exported outside the window in which it was sampled may look
   incomplete; a Locus V2 is complete.

## 18. Observations and residual risks

- `OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE` (author-defined; not promoted to `C`).
- `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` (owner `C`).
- `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` (new, pre-existing writer property):
  the FreeHEP PDF and SVG writers print path coordinates with five significant
  digits; B keeps that format. The PDF page box, transforms and rectangular
  clips of the opt-in page and the SVG `viewBox` are exact.
- Screen-anchored objects differ from the host `Export_1`/`Export_2` picture
  (§11); legacy documents relying on the host translation now show such objects
  on the canvas.
- Background images on a hidden layer stay visible everywhere (§12).
- The native document thumbnail keeps the host behavior and does not follow the
  export area.
- A view that is not laid out (command line) resolves its visible viewport from
  its settings (`DQ-B9`).

## 19. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance campaigns. The acceptance campaigns on the frozen candidate are
reported with the candidate, outside this file.

| Run | Result |
|---|---|
| focal `PreG9BR6PlusBPictureFidelityTest` (Desktop) | 15 tests, 0 failures/errors |
| focal `PreG9BR6PlusBExportSurfaceTest` (Desktop) | 20 tests, 0 failures/errors |
| `PreG9BR6CapabilityMatrixTest` with the `ExportImage` rows | 15 tests, 0 failures/errors |
| `PreG9BR6PlusA1HiddenLayerTest` after the SVG update | 12 tests, 0 failures/errors |
| development run `:desktop:desktop:test` (before the guide and CLI edits) | 1 732 tests, 0 failures/errors, 1 skip |
| guide suites after the §4.5 pin update | `PreG9BR5AGuideOutlineTest` 20, `PreG9BR5AGuideNavigationTest` 12, `PostP1GuideRenderingTest` 17, `PostP1BilingualUserGuideTest` 16; 0 failures/errors |
| scratch base probes (`git archive f6194f09`) | host PDF hash, LaTeX/DXF digests and the host `Export_1`/`Export_2` characterization identical to the candidate (§8, §11) |
| scratch Save-preview reproduction (real chooser) | stale preview 404 → 404 red pixels before the fix (§5) |
| scratch evidence probe (P0 P2 configuration) | the tables of §9 and §10 |
| process-level `runGeoCeDG` with an isolated settings file | `--export=out.png --dpi=72 doc.cedg` (`Export_1`/`Export_2` (20,10)–(23,13)): exit 0, 85 × 85 PNG after the fix of §16.8 (1 × 1 before); `--exportAnimation=anim.gif`: exit 1, message, no file |
| producer `discovery.shared` (`--test-dry-run`) | completed, 5 940 identities (unchanged) |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 733 identities |
| producer `pre-g9b-r6-plus-b.shared` (executed) | 21 tests, 0 failures/errors |
| producer `pre-g9b-r6-plus-b.desktop` (executed) | 241 tests, 0 failures/errors |
| producer `pre-g9b-r6.desktop` (executed) | 54 tests, 0 failures/errors |
| producer `final.desktop` (executed, complete Desktop suite outside the isolated partition) | 1 727 tests, 0 failures/errors, 1 skip |
| producer `final.shared` (executed, complete shared suite) | 6 857 tests, 0 failures/errors, 10 skips (shared seams regression; identities unchanged) |
| Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest`) | no finding in the touched sources after the fixes (two non-ASCII Javadoc characters, one declaration distance in `GuiManagerD` caused by the inserted call, test declaration distances and line lengths); the one remaining warning, `PreG9BR6PlusA1HiddenLayerTest.java:188`, is pre-existing |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` | OK, 893 registered files |
| `-PlanOnly` | `PHASE -Phase PRE-G9B-R6-plus-B` `COMPLETE`, 11 nodes; `INTEGRATION` 46, `FINAL` 68, `STATIC` 10, `INFRA_UNIT` 22 nodes, all `COMPLETE` |
| development `STATIC` and `INFRA_UNIT` on the uncommitted candidate tree | `ACCEPTED / COMPLETE` (`verification-60ae3acdaa2d4919a688b7c048b540a8`, `verification-26a4c42a392341a5979f9f52cbdbd809`, 22/22); the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; whitespace diagnostic clear; not acceptance evidence |

The skips are the pre-existing allowlisted ones.

**JUnit inventory and pins.** The current pins were reproduced first from the
`HEAD` blobs (`junit_inventory` `b6e11fe4…`, `static_contracts` `c2b6e388…`).
Discovery evidence came from `--test-dry-run` producer runs, executed evidence
from the passing producer runs above, and
`tools/agent/update-verification-junit-inventory.ps1` wrote every count and
hash: Desktop module discovery 1 697 → 1 733 (`c7499918…`), `final.desktop` 1 691 → 1 727 (`f019d45c…`), `pre-g9b-r6.desktop` 53 → 54 (`93da1059…`, the `exportImageRows` method), and the new `pre-g9b-r6-plus-b.shared` (21, `373b352e…`) and `pre-g9b-r6-plus-b.desktop` (241, `a16e8888…`); shared module discovery (5 940, `798f6f2e…`) and `final.shared` (6 857, `5d5f3546…`) are unchanged. Every other selection is unchanged. The static-contract inputs
`tools/agent/workspace-profile-validation.ps1` (`2e5b328d…`) and the GGBScript
matrix (`24344e93…`) were re-pinned with `Get-VerificationCanonicalTextSha256`,
then the registry catalog pins: `junit_inventory` `4ebae708…` and `static_contracts` `eaba0ba5…`. No identity hash was typed by hand; the
two new selection skeletons carried placeholders only until the updater filled
them. The registry-shape pins in
`tools/agent/tests/verification-final-coverage.Tests.ps1` follow: 36
selections, 44 PHASE selections, `pre-g9b-r6.desktop` 54 and the two new
selection counts.

## 20. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: one registered PHASE selection with two JUnit selections through the
existing registry, inventory and updater; ExportImage matrix rows and pins
through the existing schema and gate; registry-shape pins in tools/agent/tests
and the bounded profile-authority amendment follow the R4/R6/A-1 precedent; no
verifier, schema or profile-composition semantics change.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md;
  geocedg/specs/ui/cedg-workspaces.md
SERIALIZATION CHANGE = NONE
```

## 21. Changed paths

- Governance (own commits, before the product changes): `.github/prompts/tasks/pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md`.
- Licensing and provenance: `THIRD_PARTY.md`, `docs/licensing/component-matrix.md`, `docs/licensing/freehep-vectorgraphics-provenance.md`, `geocedg/validation/pre-g9b-r6-plus/freehep-vectorgraphics-provenance.json`.
- Modified third-party source: `source/desktop/desktop/src/main/java/org/freehep/graphicsio/pdf/PDFGraphics2D.java`.
- Shared upstream seams: `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianView.java`, `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdExportImage.java`, `source/shared/common/src/main/java/org/geogebra/common/main/App.java`.
- Shared GeoCeDG-owned: `source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderPolicy2D.java`, `source/shared/common/src/main/java/org/geocedg/common/export/PictureExportPolicy.java`.
- Desktop upstream: `source/desktop/desktop/src/main/java/org/geogebra/desktop/euclidian/EuclidianViewD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/GraphicExportDialog.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/PrintScalePanel.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/SVGExtensions.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/GuiManagerD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/app/GeoGebraFrame.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/FileMenuD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/util/GeoGebraFileChooser.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/plugin/GgbAPID.java`.
- Desktop GeoCeDG-owned: `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGEuclidianView.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGExportAreaPrompt.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/ExportArea.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/ExportAreaSession.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/ExportAreaUnavailableException.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/ExportViewport.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/PictureExportCommandLine.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/PictureExportRoute.java`, `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/PictureExportService.java`.
- Profile: `apps/geocedg/application-profile.yml`.
- Tests: `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ActionRegistryTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1ProfileCompilerTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/G9U1WorkspaceSurfaceTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGProfileTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR5AGuideOutlineTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6CapabilityMatrixTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusA1HiddenLayerTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusA1LayerWorkspaceTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusBExportSurfaceTest.java`, `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusBPictureFidelityTest.java`.
- GGBScript matrix: `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`.
- Verification registration: `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json`, `geocedg/specs/operations/verification-static-contracts.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1`, `tools/agent/workspace-profile-validation.ps1`.
- Documentation and evidence: `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, `docs/developer/geocedg_developer_guide.md`, `docs/roadmap/geocedg_roadmap.md`, `docs/upstream/modified-files.yml`, `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`, `docs/validation/pre_g9b_r6_plus_b_candidate_report.md`, `geocedg/specs/ui/cedg-workspaces.md`, `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-candidate-evidence.json`.

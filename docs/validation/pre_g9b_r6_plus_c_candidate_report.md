# PRE-G9B-R6-plus-C — 2D export completion: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-C
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)
PRODUCT CHANGE            = YES (physical picture, print and LaTeX sizing from the
                            construction unit and a session drawing scale; LaTeX
                            export area, visibility and semantic curves; DXF
                            units, hidden layers, explicit export area and
                            sidecar schema 2; opt-in exact EMF frame)
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
[`pre-g9b-r6-plus-c-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-C` for implementation and technical
verification on 2026-10-04 (pasted authorization, confirmed in chat), with the
dispositions `DQ-C1`–`DQ-C17` recorded in the
[C preparation closeout and authorization record](pre_g9b_r6_plus_c_prompt_closeout_record.md),
which prevails over the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-c-2d-export-completion.prompt.md)
summary.

| Identity | Value |
|---|---|
| published base `P_R6PLUS_A2` (A-2 closeout) | `e613502831e3b412780d69424d4a1e433a4ae688`, tree `48b00681a727ea4bd8768c3e9b0159bc01573ff0` |
| implementation branch start `D_R6PLUS_C_PROMPT` (reconciled preparation candidate, not published) | `6cb09d55e1ce9b18ef046f445da5494288fd8ecd`, tree `00f80cc75db6de6542bb295cef813e70c9823b9c` |
| authorization record commit `A_R6PLUS_C_PROMPT` | `75a2cf06b62afbd20f6e3a3fc0e079579a3cab2a`, tree `06a670f8812105a48db7ef57b081cc05f8943089`, prompt blob `1f9cc33cb5aaf6c9d5894b86f26cf2583a67b5a1` |
| live remote `main` (`git ls-remote`) at implementation start and before freeze | `e613502831e3b412780d69424d4a1e433a4ae688` (unchanged) |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-plus-c-2d-export-completion` |

Neither preparation candidate (`5ad96304`, `6cb09d55`) was amended; the
implementation is one commit on top of `75a2cf06`. No push, merge, tag or
release was made. `selfApproved`, `authorApproved` and `passClaimed` stay
`false` throughout.

## 2. What changed

### 2.1 Shared GeoCeDG export package (`org.geocedg.common.export`)

- `GeometryExportContext` (new, GeoGebra-free): the document export context —
  `UnitState`, persistent `HiddenLayerSet` (`A-2`) and the consumed
  `GeometryExportArea` — with a `Source` that re-resolves it for the staleness
  guard. `GeoElementGeometryExportAdapter.documentContext(sources)` builds the
  document default.
- `GeometryExportArea` (new): the producers `MANUAL`, `EXPORT_POINTS_EXPLICIT`,
  `EXPORT_POINTS_AUTOMATIC` and the `VISIBLE_VIEWPORT` fallback (no bounds, no
  boundary); rule `geocedg-export-area-participation-b1/v1`.
- `ExportAreaParticipation2D` (new): the `B1` predicates — exact `BigDecimal`
  separating-axis and squared-distance tests for points, segments, rays, lines,
  polylines, polygon edges and circles; closed-form binary64 edge intersections
  for circular and elliptic arcs with ties (relative `1E-12`) resolved toward
  participation.
- `SemanticCurveExportAdapter2D` (new): the Locus V2 / Spline V2 glue extracted
  from `G9X1GeometryExportAdapter` without changing its output (§11), with a
  per-component participation hook and an `OUTSIDE_EXPORT_AREA` component
  outcome.
- `GeometryExportModel`: `Unit` `UNITLESS(0)`, `MM(4)`, `CM(5)`, `M(6)`, `USM(0)`;
  the export context; `AreaExclusion` records (require a boundary);
  `isLayerOff`. `DxfExporter`: `$INSUNITS` from the unit; `LAYER` group `62 = -7`
  for hidden GeoCeDG layers; `999` comments for `usm` and for an explicit area.
  `GeometryExportPreflight`: sidecar mandatory for `usm`; outside-area count.
  `G9X1GeometryExportAdapter`: preflight under an export context, `B1`
  participation, guard `context.equals(source.resolve())`.
- `PhysicalExportScale` / `PhysicalExportLimitException` (new): centimetres per
  model unit `fb(c)·100·a/b`, device extents, the EMF frame in 0.01 mm and PDF
  page limits, each failing explicitly instead of clamping.
- `SemanticExportClassification` (new): source classes `COMPLETE`,
  `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `NO_ADMISSIBLE_OUTPUT`; export classes
  `COMPLETE`, `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`,
  `REJECTED_NO_ADMISSIBLE_OUTPUT`; whole-export rejection reasons
  `TOLERANCE_NOT_ESTABLISHED` and `STALE_SOURCE_REVISION`.

### 2.2 Kernel (one read-only query)

`org.geocedg.common.kernel.locus.intersection.CertifiedComponentAreaDisjointness2D.prove`
(new) asks the existing ADR 0028 certified interval curve model of one Locus V2 /
Spline V2 component whether it is disjoint from a closed rectangle: FIFO
bisection over the knot spans of the component with outward enclosures, budget
`DEFAULT_MAXIMUM_BOXES = 4096`; `PROVEN_DISJOINT`, `NOT_PROVEN` (refused
enclosure, indivisible box or exhausted budget) or `NO_CERTIFIED_MODEL`. No
kernel state, identity, domain, certificate, DAG or persistence changes.

### 2.3 Shared upstream seams (host-identical defaults)

- `common/main/App.java`: `getPhysicalExportScale()` (`NaN`),
  `getExportAreaWorldBounds(view)` (`null`), `exportAreaBoundsEdited(view, …)`
  (`false`); primitive and existing common types only.
- `common/export/pstricks/GeoGebraExport.java`: `initBounds` reads the
  application bounds first; `refreshSelectionRectangle` returns when the
  application absorbed the edit; `drawGeoElement` skips objects whose layer
  `App.isLayerShown` hides; unsupported elements go through
  `drawUnsupportedElement`, whose default only logs as before.

### 2.4 Desktop upstream (host path unchanged without a GeoCeDG presentation)

`AppD` (`getExportScalePresentation()` `null`, `runApiDocumentReplacement`
runs), `GgbAPID.setXML(String)` (the `E5` marker), `PrintScalePanel`,
`GraphicExportDialog`, `EuclidianViewD` (print scale and title),
`pstricks/ExportFrame` (LaTeX panel, read-only derived units).

### 2.5 Modified third-party source: FreeHEP EMF (`DQ-C9`)

`emf/EMFOutputStream.java` (`setExactFrame(Rectangle)`, applied to the header's
frame when the stream closes), `emf/EMFGraphics2D.java` and
`emf/EMFPlusGraphics2D.java` (`setExactFrame(width, height)` in 0.01 mm, passed
with the header). `EMFHeader` is not modified. A writer that does not opt in
produces the original bytes (§11). The family's provenance was recorded before
the change in [`freehep-vectorgraphics-provenance.md`](../licensing/freehep-vectorgraphics-provenance.md)
(EMF family section) and its JSON mirror; `THIRD_PARTY.md` and the component
matrix name the files.

### 2.6 GeoCeDG-owned Desktop code

`AppGeoCeDG` (drawing-scale owner and `E1`–`E6`, presentation, physical scale,
LaTeX area seams, `resolveActiveExportArea`, GeoCeDG LaTeX exporters),
`GeoCeDGExportScalePresentation`, `GeoCeDGDxfExportController` (export context,
units text, legacy-flow `usm` refusal, report lines), and in
`org.geocedg.desktop.export`: `DrawingScale`, `DrawingScaleHolder`,
`DrawingScaleControl`, `ExportScalePresentation`, `LatexExportPanel`,
`LatexSemanticExportSupport`, `LatexSemanticExporter`, `GeoCeDGLatexSettings`,
`GeoCeDGGeoGebraToPstricks`, `GeoCeDGGeoGebraToPgf`,
`GeoCeDGGeoGebraToAsymptote`, and the modified `DxfExportPreflightPresentation`,
`DxfFidelityManifestWriter`, `DxfManifestEncoding`, `ExportViewport`,
`PictureExportCommandLine`, `PictureExportRoute`, `PictureExportService`.

### 2.7 Profile

`apps/geocedg/application-profile.yml`: the DXF `selection_contract_ref`
`geocedg.dxf-exportable-geometry-population` (`DQ-C3`); the notes of the Picture,
three LaTeX and DXF actions; texts `ExportScale.*` (9) and `LatexExport.Tolerance`
/ `LatexExport.Report` in English and Spanish. No action identifier changes, so
the profile-authority allow-list is untouched. The historical
`geocedg/specs/ui/application-profile-v2.candidate.yml` (G9U1 design) is not
edited.

## 3. Author dispositions as implemented

| ID | Implementation |
|---|---|
| `DQ-C1` | Spline V2 of every degree goes through the one certified approximation contract (LaTeX and DXF); no Bézier special case; no exactness claim (`splineV2FollowsTheSameApproximationContractAtEveryDegree`). |
| `DQ-C2` | Objects on persistently hidden layers stay on their source DXF layer `GEOCEDG_L<n>`, whose `LAYER` record has `62 = -7`; the hidden set enters the layer table, preflight, report, sidecar `layers` block and the staleness guard; an object both hidden and on a hidden layer carries both. |
| `DQ-C3` | Individually hidden objects keep group `60 = 1`; visibility is never read as layer state; the profile reference and the specifications say "exported population". |
| `DQ-C4` | LaTeX tolerance field per dialog, model coordinates, default `0.001`, positive finite (otherwise the export is rejected with no code), independent of units and scale; G9X1 approximation machinery and budgets. |
| `DQ-C5` | Source and export classes of §2.1; certified components are emitted beside incomplete or failed ones; DXF keeps the strict G9X1 rule (`dxfKeepsTheStrictWritabilityRule`). |
| `DQ-C6` | `DrawingScale`: positive integers, each ≤ `1 000 000 000` after gcd reduction, ASCII `a:b` of at most 18 characters, explicit refusal (previous value kept), equivalent pairs equal; presets `1:1`, `1:2`, `1:5`, `1:10`, `2:1`, `5:1` with free entry. |
| `DQ-C7` | One value per `AppGeoCeDG` window, default `1:1`, reset only at `E1`–`E6` (§5). |
| `DQ-C8` | Physical document: the Picture and Print Preview scale panels show only the `a:b` control and the construction unit; no fixed-size or pixel mode; `presentationUnit` never matters. Unspecified document: device modes kept and relabelled "Device scale (non-physical)", "Device scale from the screen (non-physical)", "Size in pixels (device)", with the statement that the document has no construction unit; no `a:b` control. |
| `DQ-C9` | Opt-in exact `rclFrame` from the requested output size, nearest 0.01 mm (§8). |
| `DQ-C10` | `NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`: no new preview surface; the guides state the Save-preview conditions (existing target file, chooser at least 600 px wide). |
| `DQ-C11` | Sidecar schema version 2 (`units`, `layers`, `export_area`, `outside_export_area`, `dxf.insunits`); version-1 sidecars are never reinterpreted (the validator reads its own version); `$INSUNITS` 4/5/6; `usm`: `0`, header comment, warning, mandatory sidecar with token `usm` and canonical `meters_per_unit`; unspecified: `0`, `UNITLESS`, no factor. |
| `DQ-C12` | Deterministic `%` / `//` comments (export class, tolerance, guarantee, per source and per component, coverage) and a non-modal report area in each LaTeX dialog; comments are never parsed back. |
| `DQ-C13` | `B1` as specified (§10). |
| `DQ-C14` | The five-significant-digit path formatting is documented in both guides; page, `viewBox` and frame sizing are tested apart from it. |
| `DQ-C15` | Live pins repinned with the supersession recorded (§12); frozen G9X1 evidence untouched; the unitless G5/G9X1 corpus is byte-identical (§11). |
| `DQ-C16` | Routes without an explicit device or render scale are physical; explicit API, CLI and command parameters stay device parameters and never mutate the drawing scale (§7). No API signature changed. |
| `DQ-C17` | [ADR 0005](../adr/0005-neutral-2d-geometry-export.md) "Amendment 1 (2026-10-04)"; original text kept; ADR 0014 unchanged. |

`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` is not fixed: the tool-replacement
reload still reaches the `A-2` commit; `C` only keeps its own drawing scale
unchanged on that route.

## 4. Re-established seams and corrections to C1–C18

Every citation of the prompt was re-read at the branch start before use.
Findings that refine C1–C18 (none contradicts a disposition):

1. `GeoGebraExport.setFrame` is `final`: the GeoCeDG exporters swap the
   protected `frame` for the `GeoCeDGLatexSettings` decorator inside
   `generateAllCode` instead of overriding `setFrame`.
2. `ExportAreaSession.defineManual` normalizes an inverted rectangle; LaTeX
   bound edits therefore always produce a valid `MANUAL` area, and an edit
   equal to the current area is absorbed without redefining it.
3. The C1 row "SVG, API and CLI: device" changes under `DQ-C16`: without an
   explicit physical size those routes are now physical in a physical document;
   C1's "EMF, dialog: device" becomes the labelled size via the opt-in frame.
4. `ExportImage` (C1 "explicit parameter") keeps its own `scalecm` default of
   1 cm per unit for PNG; its SVG has no scale and its PDF page follows the
   physical page (§7).
5. In the device mode the dialog's EMF frame is its labelled device size (C5
   last paragraph) because the dialog passes its labelled centimetres.
6. `EMFHeader` computes `rclFrame` in its constructor at
   `EMFOutputStream.close()`, and `getFrame()` returns the mutable rectangle:
   the opt-in sets that rectangle's bounds there, so `EMFHeader` is unmodified.
7. A Locus V2 source identity in DXF and sidecar text carries a per-session
   construction hash (`geo:<32 hex>`) and `source_revision` varies between runs
   of the same build; byte comparisons with the base normalize exactly those
   fields (§11).
8. Upstream `AutoColor` keeps one curve-colour index per JVM, so default curve
   colours depend on the curves earlier tests created; the identity test resets
   it (test-only reflection) to stay order-independent.
9. The historical G9X1 source-authority scan already fails at the base on
   `AppGeoCeDG.java` (the `B` export-frame seam names `EuclidianView`); `C`
   keeps every other perimeter file clean by moving the active-view resolution
   of the DXF context into `AppGeoCeDG.resolveActiveExportArea` (§12).

## 5. `drawingScale` lifecycle (`DQ-C7`)

| Event | Seam | Test |
|---|---|---|
| `E1` completed New | `AppGeoCeDG.fileNew` after the saved baseline | `e1ACompletedNewResetsAndACancelledNewKeeps` |
| `E2` native Open | `nativeDocumentLoadCommitted` | `e2E4NativeOpenAndLoadXmlReset` (chooser route `loadFile`, API `openFile`) |
| `E3` non-native replacement | `documentReplacementCommitted` | `e3NonNativeReplacementsReset` (`setBase64`, `.html`, non-native `openFile`, `loadXML(File)`) |
| `E4` document load | `runDocumentLoad` when loaded | `e2E4NativeOpenAndLoadXmlReset` (`loadXML(String)`) |
| `E5` API `setXML` | `GgbAPID.setXML(String)` → `runApiDocumentReplacement` marker, reset when `AppGeoCeDG.setXML` observes the completed parse | `e5TheApiSetXmlResetsAndAToolReplacementReloadDoesNot` |
| `E6` reset | `AppGeoCeDG.reset()`: with a current file `super.reset()` (the reload fires `E2`/`E4`); otherwise after a successful `clearConstruction` | `e6AResetToABlankDocumentResetsAndAFileResetRelyOnTheLoad` |

Unchanged routes mechanized: cancelled New, clearing `setXML` of a tool
replacement, tool replacement through the shared model, `evalXML`, non-clearing
`setXML`, undo, redo, redefine, `clearConstruction` alone, a failed load, a unit
change, a clearing preference XML without the marker and the installed-preference
reload (`mergesUndoClearsAndFailedLoadsKeepTheScale`,
`preferenceReloadsKeepTheScale`); session-only value, no undo point, no modified
flag, never in XML or preferences (`theScaleIsSessionStateOnly`); one value per
window (`everyWindowHasItsOwnValueStartingAtOneToOne`). The remaining C2
non-reset routes (macro-edit window, `.ggt` open/save/delete, tool library,
portable preferences and *Restore default settings*, Open URL pre-clear and
cancel, paste, Insert File, Apply Template, rollback) reach none of the six
reset sites: the only writers of the value are `setDrawingScale` and the six
sites above, and `loadExistingFile` passes macro files to the host before
`runDocumentLoad`. No `ExportArea` or paste-notice reset was added, moved or
removed; `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` stays open and
unchanged.

## 6. Physical size (C4)

`p = fb(c) · 100 · a / b` centimetres per model unit, `fb(c)` the metres per
unit of the effective construction unit (`usm`: its canonical factor). For an
area of `W × H` model units: PNG `round(W·p·D/2.54)` px at `D` DPI (DPI is
resolution only); PDF page `W·p·72/2.54` pt exact (opt-in page extent); SVG
`width = W·p` cm with the exact `viewBox`; EMF `rclFrame = round(W·p·1000)`
0.01 mm; print `W·p` cm on the page. Zoom, DPI, the view `printingScale` and
`presentationUnit` never enter.

Values asserted by `everyPictureFormatIsSizedByUnitAndDrawingScale` for
`W × H = 4 × 3`:

| Unit | Drawing scale | `p` (cm/unit) | PDF page (pt) | PNG at 300 DPI (px) | SVG (cm) | EMF `rclFrame` (0.01 mm) |
|---|---|---|---|---|---|---|
| `mm` | `1:1` | 0.1 | 11.339 × 8.504 | 47 × 35 | 0.4 × 0.3 | 400 × 300 |
| `mm` | `1:2` | 0.05 | 5.669 × 4.252 | 24 × 18 | 0.2 × 0.15 | 200 × 150 |
| `cm` | `2:1` | 2 | 226.772 × 170.079 | 945 × 709 | 8 × 6 | 8000 × 6000 |
| `cm` | `2:4` (= `1:2`) | 0.5 | 56.693 × 42.520 | 236 × 177 | 2 × 1.5 | 2000 × 1500 |
| `m` | `1:100` | 1 | 113.386 × 85.039 | 472 × 354 | 4 × 3 | 4000 × 3000 |
| `usm` 0.0254 m (`in`) | `1:1` | 2.54 | 288 × 216 | 1200 × 900 | 10.16 × 7.62 | 10160 × 7620 |

Print: `cm` at `1:2`, a 4-unit segment prints 2 cm (56.7 pt) whatever the zoom
(`printingUsesThePhysicalScaleAndPrintsTheDrawingScale`); the printed title is
"Scale 1:2 (construction unit cm)". `presentationUnit` changes leave PDF and SVG
bytes identical; zoom and the view `printingScale` leave the PDF page
unchanged; 72 and 600 DPI change only the pixel count
(`zoomDpiPrintingScaleAndPresentationUnitNeverChangeAPhysicalSize`).
Unrepresentable sizes fail explicitly: a PDF page beyond the writer, an EMF
frame beyond `2^31 − 1` hundredths of a millimetre, a raster below one pixel, a
command-line export that writes no file
(`unrepresentableSizesFailExplicitlyInsteadOfBeingClamped`).

Unspecified unit (C17): no `a:b` control; the `B` device rule (`W·printingScale`)
applies and is labelled non-physical; the session drawing scale applies again as
soon as a unit is declared (`anUnspecifiedDocumentKeepsTheLabelledDeviceScale`).
For a view whose axes ratio is not 1:1, the height follows the `B` isotropic
convention of the view ratio.

## 7. Route matrix after `C`

| Route | Physical document (`p` of §6) | Unspecified document | Test |
|---|---|---|---|
| Picture PNG (dialog) | `p`·DPI raster, explicit limits | `B` device modes, relabelled | `everyPictureFormat…`, `zoomDpi…` |
| Picture PDF / SVG (dialog) | exact page / `width` from `p` | `B` device rule | same |
| Picture EMF/EMF+ (dialog) | opt-in frame `W·p`; device raster at the dialog DPI | opt-in frame = labelled device size | `theEmfFrameIsTheRequestedSize…` |
| Print, Print Preview | `p`; title "Scale a:b (construction unit c)" | host printing scale | `printingUsesThePhysicalScale…` |
| Graphics clipboard | device route (host scale 2, pixel budget), unchanged | unchanged | `clipboardSavePreviewAndExportImageKeepTheirRouteContract` |
| Save preview | thumbnail, unchanged (`DQ-C10`) | unchanged | same |
| CLI `--export` PNG/PDF/SVG/EMF | physical; `--dpi` resolution; EMF frame `W·p` | `B` rule | `routesWithoutAScaleArePhysical…` |
| CLI `--maxSize` | device parameter | device | same |
| API `writePNGtoFile`, `getPNGBase64` with a scale | device parameter | device | same |
| API `exportSVG` (no scale) | physical `width`/`height` | device | same |
| API `exportPDF(scale, …)` | render scale; page physical | `B` rule | same |
| `ExportImage` PNG | its own `scalecm` (default 1 cm per unit) and DPI: explicit command parameters | same | `clipboardSavePreview…` |
| `ExportImage` SVG / PDF | through `exportSVG` / `exportPDF`: physical | device | same |
| LaTeX (dialogs and API) | `xunit = yunit = p` cm, width and height derived from the area | dialog/API values as non-physical device parameters | `physicalUnitsAreDerived…`, `theApiAndTheDialogPanel…` |
| DXF | model coordinates; `$INSUNITS` from the unit | `0`, `UNITLESS` | §10 |

No route mutates the drawing scale or the unit state.

## 8. EMF evidence (`DQ-C9`)

`cm`, `1:1`, area 4 × 3: `rclFrame = (0, 0, 4000, 3000)` at 72, 300 and 600 DPI
while the device bounds follow the resolution (`round(W·D/2.54)` units); a
requested 4.0003 × 2.99996 cm gives 4000 × 3000 (nearest 0.01 mm, error
≤ 0.005 mm); without the opt-in the frame keeps the FreeHEP reference
`⌊bounds·100·320/1024⌋`. The default `EMFGraphics2D` and `EMFPlusGraphics2D`
outputs of a fixed drawing are byte-identical to the base (§11). The
base defect of C5 (frame `W·p·D/81.28` cm, e.g. 147.50 × 110.62 mm for a
labelled 40 × 30 mm at 300 DPI) is gone on every GeoCeDG EMF route.

## 9. LaTeX (PSTricks, PGF/TikZ, Asymptote)

- Bounds: the effective `ExportArea` of the view; a bound edit in the dialog
  defines the `MANUAL` area; the selection rectangle is never read or written in
  GeoCeDG (`boundsComeFromTheExportAreaAndEditsWriteManual`).
- Visibility: hidden layers and individually hidden objects excluded, the same
  inclusion set as the picture (`hiddenLayersAndHiddenObjectsAreExcludedLikeInPictures`;
  `reopenedHiddenLayersAreOffInDxfAndOmittedInLatex`).
- Units: `xunit = yunit = p` cm, width and height from the area, and a first
  comment line "GeoCeDG physical scale: drawing scale a:b, construction unit c:
  1 model unit = p cm on the output (xunit = yunit)"; without a unit the dialog
  values are device parameters with the statement of `DQ-C8`
  (`physicalUnitsAreDerivedAndDeviceUnitsStayWithoutAConstructionUnit`).
- Semantic curves: every eligible Locus V2 / Spline V2 (construction order,
  shown layer, wholly 2D, visible) is adapted read-only through
  `SemanticCurveExportAdapter2D` at the requested tolerance; each certified
  component is one path (`\draw … -- cycle;`, `\psline`/`\pspolygon`,
  `draw(… --cycle)`), never bridged across gaps or failed components; the legacy
  `Locus` keeps the host output (`aLegacyLocusKeepsTheHostOutput`).
- Result model and disclosure: the header comment states the export class,
  sources, tolerance and guarantee; each source and component has its class,
  achieved error or reason (`MISSING_DOMAIN`, `WORK_LIMIT`,
  `DISCONTINUITY_UNRESOLVED`, `DEGENERATE_SOURCE`) and coverage; an incomplete
  export says "Incomplete: some semantic output is locally certified only or
  failed"; an invalid tolerance, `TOLERANCE_NOT_ESTABLISHED` or a stale source
  revision rejects the export with no code; the dialog report repeats the
  classification (`anIncompleteLocusIsEmittedAndReportedIncomplete`,
  `failedComponentsAreDisclosedAndNeverBridged`,
  `invalidToleranceAndStaleSourcesRejectTheWholeExport`). Without semantic
  curves, units or area, the output is the base output (§11).

## 10. DXF

Header and units (`dxfDeclaresTheEffectiveConstructionUnitAndKeepsCoordinates`,
`unspecifiedDocumentsKeepUnitlessAndAPresentationChangeNothing`,
`usmWritesUnitlessDxfAWarningCommentAndRequiresTheSidecar`):

| Effective unit | `$INSUNITS` | Sidecar |
|---|---|---|
| unspecified | `0` | when fidelity requires it; `unitless`, `meters_per_unit` `null` |
| `mm` / `cm` / `m` | `4` / `5` / `6` | when fidelity requires it; unit metadata |
| `usm` | `0`, `999` "GeoCeDG construction unit usm (custom unit, k m per unit); $INSUNITS 0; physical meaning only in the paired fidelity sidecar", warning `CUSTOM_UNIT_USM` | mandatory; token `usm`, canonical `meters_per_unit` |

The exact-only (G5) flow, used when the extended DXF is disabled, writes no
sidecar and therefore refuses a `usm` document with an explicit message and
writes nothing (`theExactOnlyFlowRefusesUsmBecauseItWritesNoSidecar`).
Coordinates are never converted.

Visibility (`hiddenLayersAreOffDxfLayersAndHiddenObjectsKeepGroup60`): hidden
layer `n` → entities on `GEOCEDG_L<n>`, `LAYER` `62 = -7`, warning
`HIDDEN_LAYER_OFF`; individually hidden → `60 = 1`.

Export area (`B1`; `exactPredicatesDecideParticipationOfEveryFamily`,
`explicitAreaSelectsWholeSourcesAndReportsTheRestOutside`,
`theVisibleViewportFallbackIsNoBoundaryAndKeepsTheBaseBytes`,
`semanticComponentsAreExcludedOnlyByACertifiedDisjointnessProof`,
`theControllerReadsUnitHiddenLayersAndTheExplicitAreaOnly`): the area of the
active 2D Graphics view (else Graphics 1); only `MANUAL` and the two
`EXPORT_POINTS` producers are boundaries; participation by the closed area,
whole emission, `OUTSIDE_EXPORT_AREA` counted, commented
("GeoCeDG export area <rule>: <producer and bounds>; whole sources and
components meeting the closed area; N outside the export area, reported, not
emitted"), recorded in the sidecar and never a fidelity reduction; area
filtering alone never makes a sidecar mandatory.

Sidecar schema 2 (`theVersionTwoSidecarAgreesWithTheHeader`; `G9X1DxfManifestTest`):
root `schema`, `schema_version`, `application`, `dxf` (with `insunits`),
`units`, `layers`, `export_area`, `request`, `preflight` (with
`outside_export_area`, `custom_unit_usm`), `outcomes`, `outside_export_area`,
`warnings`. Staleness (`unitHiddenLayerAndAreaChangesMakeAPendingPreflightStale`):
a unit, hidden-layer or explicit-area change after preflight refuses the write.

## 11. Byte identity and the Classic/Web boundary

`PreG9BR6PlusCBaseIdentityTest` compares 20 SHA-256 digests with values
recorded by the same code on a `git archive` of `P_R6PLUS_A2` `e6135028`
(scratch probe; two base runs identical, also after the `B` classes in the same
JVM):

| Group | Outputs | Result |
|---|---|---|
| G9X1 corpus | Locus V2 with two components, closed Locus V2, Spline V2 degree 3, function on a semantic domain: DXF and sidecar | identical after normalizing the per-session `geo:<hash>` and `source_revision` (DXF) and, for sidecars, the build provenance, the paired DXF hash and the declared version-2 additions |
| G5 / G9X1 exact scene | ten exact families with one hidden object: `exportDxf` and G9X1 encode, sidecar | identical |
| GeoCeDG LaTeX | PGF of an unspecified document without semantic curves or area | identical |
| Classic | PGF, PSTricks, Asymptote, PDF (creation date normalized), SVG (clip ids normalized), EMF of a Classic `AppD` | identical |
| FreeHEP default | `EMFGraphics2D` and `EMFPlusGraphics2D` without the opt-in | identical |

Classic seams (`theUpstreamSeamsKeepTheHostDefaults`): `NaN`, `null`, `false`,
no presentation, the API marker only runs, the host `GeoGebraToPgf`, and a bound
edit still writes the selection rectangle. Classic print keeps the host printing
scale through the same `NaN` seam. Web boundary (`theUpstreamLatexPackageStaysWebCompatible`):
no `org.geocedg`, `java.io`, `java.awt`, `java.security` or reflection in
`common/export/pstricks`; the `App` seams use primitive and common types only.

## 12. Specification, ADR and verifier-pin amendment map

| File | Amendment |
|---|---|
| `geocedg/specs/export/geometry-export-foundation.md` | header amendment bullet; model contract with the export context; new section "Explicit export area (`PRE-G9B-R6-plus-C`, `DQ-C13`, choice `B1`)"; units table (§15.2); exported population (`DQ-C2`, `DQ-C3`); G5 PASS clause |
| `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` | version 1.1: status row, §2 population and visibility, §7 (strict writability unchanged; the literal "A wholly `EXACT` export may omit" kept for `verify-g9p-design`), §8 sidecar schema 2; canonical-LF SHA-256 `863c60add091544fe241cc25f0ce48f6b9fa110ae9b4e0c216c0bdcf56268263` |
| `geocedg/specs/units/unit-system.md` | status markers in §0, §6.4, §9, §15 (heading, status, §15.1 title) and §18.2; no clause changed |
| `docs/adr/0005-neutral-2d-geometry-export.md` | "Amendment 1 (2026-10-04)" (`DQ-C17`) |
| `geocedg/specs/ui/cedg-workspaces.md` | the `C` paragraph |
| `tools/agent/verify-g9x1-extended-dxf.ps1` | `DQ-C15`: the live specification pinned as the next successor (`$SpecificationLiveSha`, authorized by `75a2cf06`, phase `PRE-G9B-R6-plus-C`); the recorded `S1-R1` successor `f2a0cacc…` now proven from its own commit blob `e5260fc7`; the shared guide literals accept the unit row of either edition |
| `docs/user/geocedg_user_guide_en.md`, `_es.md` | §4.5 (EMF frame, physical size and drawing scale, API, LaTeX, limits including the Save preview conditions and five-digit paths), §4.6, §11.7–§11.11, §12 intro and §12.5, limits list; no heading added |
| `docs/developer/geocedg_developer_guide.md` | new "Export completion (PRE-G9B-R6-plus-C)" section; `B` section EMF and LaTeX sentences |

`verify-g9x1-extended-dxf.ps1 -SkipBuild` passes the canonical-hash checks of
the prompt, the live specification, the supersession proof and the ADR, then
stops at the pre-existing source-authority failure of `AppGeoCeDG.java` (present
at `e6135028`; §4 item 9); the shared guide literals were checked separately in
both editions. `verify-dxf.ps1 -SkipBuild`: "G5 DXF verification passed"; it has
no pin that `C` supersedes. Neither script is a registry leaf. Frozen evidence
(`geocedg/validation/export/g9x1/**`, earlier reports and closeouts) is not
edited.

## 13. Obligation → test map

| Obligation | Tests |
|---|---|
| `T-ROUTES` | `PreG9BR6PlusCPhysicalExportTest` (all eight picture/print/route methods and `clipboardSavePreviewAndExportImageKeepTheirRouteContract`), `PreG9BR6PlusCLatexExportTest.theApiAndTheDialogPanelUseTheSameSemanticExport`, `PreG9BR6PlusBExportSurfaceTest` |
| `T-SCALE-VALUE` | `PreG9BR6PlusCDrawingScaleTest.theValueIsAReducedPositiveIntegerPairWithExplicitRefusals` |
| `T-SCALE-LIFECYCLE` | `PreG9BR6PlusCDrawingScaleTest` `e1…`–`e6…`, `mergesUndoClears…`, `preferenceReloads…`, `theScaleIsSessionStateOnly`, `everyWindowHas…` (§5) |
| `T-SCALE-UI` | `theControlAcceptsReducedEntries…`, `aPhysicalDocumentOffersOnlyItsDrawingScale`, `anUnspecifiedDocumentKeepsTheLabelledDeviceScale`, `physicalUnitsAreDerived…` |
| `T-PHYSICAL-PICTURE` | `everyPictureFormatIsSizedByUnitAndDrawingScale`, `theEmfFrameIsTheRequestedSize…`, `printingUsesThePhysicalScale…` |
| `T-PRESENTATION-INVARIANCE`, `T-ZOOM-INVARIANCE` | `zoomDpiPrintingScaleAndPresentationUnitNeverChangeAPhysicalSize`; shared `unspecifiedDocumentsKeepUnitlessAndAPresentationChangeNothing` |
| `T-COORDINATES` | shared `dxfDeclaresTheEffectiveConstructionUnitAndKeepsCoordinates`; `PreG9BR6PlusD1ExportRegressionTest`; `reopenedHiddenLayers…` |
| `T-UNSPECIFIED` | `anUnspecifiedDocumentKeepsTheLabelledDeviceScale`, `physicalUnitsAreDerived…`, shared `unspecifiedDocuments…`, `theControllerReads…` |
| `T-EXTREMES` | `unrepresentableSizesFailExplicitly…`, `theValueIsAReduced…`, shared `thePhysicalScaleFollowsUnitAndDrawingScaleOnly` |
| `T-LATEX-AREA` | `boundsComeFromTheExportAreaAndEditsWriteManual` |
| `T-LATEX-VISIBILITY` | `hiddenLayersAndHiddenObjectsAreExcludedLikeInPictures`, `PreG9BR6PlusA1HiddenLayerTest.svgLatexAndDxfFollowAHiddenLayerAfterBAndC` |
| `T-LATEX-UNITS` | `physicalUnitsAreDerivedAndDeviceUnitsStayWithoutAConstructionUnit` |
| `T-SEMANTIC-ADAPTER` | shared `theSemanticAdapterKeepsComponentsGapsOrientationAndRevision`, `theSemanticAdapterNeverReadsTheRenderPath` |
| `T-LATEX-SEMANTIC` | `locusV2IsWrittenAsOnePathPerCertifiedComponentInEveryDialect` |
| `T-LATEX-FAILURES` | `anIncompleteLocusIsEmittedAndReportedIncomplete`, `failedComponentsAreDisclosedAndNeverBridged`, `invalidToleranceAndStaleSourcesRejectTheWholeExport` |
| `T-EXPORT-RESULT-MODEL` | shared `theResultModelSeparatesLocalAdmissibilityFromGlobalCompleteness`, `dxfKeepsTheStrictWritabilityRule`; the LaTeX failure tests |
| `T-SPLINE` | `splineV2FollowsTheSameApproximationContractAtEveryDegree` |
| `T-LEGACY-LOCUS` | `aLegacyLocusKeepsTheHostOutput` |
| `T-DXF-UNITS`, `T-DXF-USM` | shared `dxfDeclares…`, `usmWrites…`; Desktop `aUsmDocumentAlwaysWritesItsSidecarWithTheCanonicalFactor`, `theExactOnlyFlowRefusesUsm…` |
| `T-DXF-SIDECAR` | `theVersionTwoSidecarAgreesWithTheHeader`, `G9X1DxfManifestTest`, `G9X1PairedOutputTest` |
| `T-DXF-STALENESS` | shared `unitHiddenLayerAndAreaChangesMakeAPendingPreflightStale` |
| `T-DXF-VISIBILITY` | shared `hiddenLayersAreOffDxfLayersAndHiddenObjectsKeepGroup60`; Desktop `reopenedHiddenLayersAreOffInDxfAndOmittedInLatex` |
| `T-DXF-AREA` | shared `exactPredicates…`, `explicitAreaSelects…`, `theVisibleViewportFallback…`, `semanticComponentsAreExcludedOnlyBy…`; Desktop `theControllerReads…` |
| `T-G9X1-IDENTITY`, `T-CLASSIC` | `PreG9BR6PlusCBaseIdentityTest.outputsThatCMustNotChangeAreTheBaseBytes` (§11); `G9X1G5CorpusCompatibilityTest`, `GeometryExportFoundationTest` |
| `T-B-REGRESSION` | `PreG9BR6PlusBPictureFidelityTest`, `PreG9BR6PlusBExportSurfaceTest.latexAndDxfKeepTheBaseOutputWithoutAnAreaAndFollowAnExplicitArea` |
| `T-D1-REGRESSION` | `PreG9BR6PlusD1ExportRegressionTest.exportsKeepModelCoordinatesAndOnlyDxfDeclaresTheUnit` (replaces the superseded `everyExportIsIndependentOfTheUnitState`) |
| `T-A2-REGRESSION` | `reopenedHiddenLayersAreOffInDxfAndOmittedInLatex`, `PreG9BR6PlusA2*` |
| `T-WEB-BOUNDARY` | `theUpstreamLatexPackageStaysWebCompatible` |
| `T-NO-SERIALIZATION` | `theScaleIsSessionStateOnly`; `PreG9BR6PlusD1ExportRegressionTest` (XML and undo unchanged by every export) |
| `T-DETERMINISM` | the deterministic-text assertions of the LaTeX and DXF tests; two identical base runs (§11) |
| `T-PREVIEW` | `DQ-C10`: guide text; `clipboardSavePreview…` (preview unchanged) |
| `T-API-ROUTES` | `routesWithoutAScaleArePhysicalAndExplicitScalesStayDeviceParameters`, `clipboardSavePreview…` |
| `T-HISTORICAL-PINS` | §12 (`verify-g9x1-extended-dxf.ps1` and `verify-dxf.ps1` with `-SkipBuild`) |
| `T-GUIDE` | `PreG9BR5AGuideOutlineTest`, `PostP1BilingualUserGuideTest` and the other guide suites in `final.desktop` |
| `T-SMOKE` | §18 and the package `models/regression/pre-g9b-r6-plus-c-export-completion/` |

## 14. Deviations and interpretations

- `DQ-C5` names a whole-export `REJECTED`; the implemented export class is
  `REJECTED_NO_ADMISSIBLE_OUTPUT`, reached both when nothing is admissible and
  when the tolerance or a source revision rejects the export, with the reason in
  the report.
- The device-mode statement and labels are product texts of the profile, so the
  Classic panel keeps its host labels.
- `ExportImage` PNG keeps its `scalecm` default (one centimetre per model unit)
  as an explicit command parameter (C1); a physical PNG comes from the Picture
  dialog, the command line or a page route.
- The two `EXPORT_POINTS` producers keep the `B` names; the sidecar writes
  them, like every sidecar enumeration, in lower case
  (`export_points_automatic`).

## 15. Observations and residual risks

- `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`: unchanged and open
  (C16); no `ExportArea` or paste-notice reset was touched.
- `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`: unchanged and open; not `C` scope.
- `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT`: `NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`
  (`DQ-C10`).
- `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION`: resolved by `DQ-C9` within 0.01 mm.
- `OBS-B-FREEHEP-PATH-COORDINATE-PRECISION`: documented writer limitation
  (`DQ-C14`).
- New, pre-existing (upstream): `GgbAPID.exportPDF` reads the written PDF as
  UTF-8 text for its callback, which fails silently for binary PDF content; the
  file itself is correct. Not `C` scope.
- New, pre-existing (upstream): `AutoColor` keeps a per-JVM curve-colour index
  (§4 item 8).
- New, pre-existing: the historical G9X1 source-authority scan fails at the base
  on `AppGeoCeDG.java` (§4 item 9); not a registry leaf.
- New, pre-existing: Desktop test-JVM heap pressure. With Gradle's default
  512 MB test heap, the complete Desktop suite retains about 398 MB after forced
  garbage collection (90 `AppGeoCeDG` instances) when it reaches the
  `PreG9BR6Plus*` classes and about 443 MB at its end, the same on the base
  (`git archive e6135028`: 398 / 402 / 443 MB) and on the candidate
  (398 / 403 / 444 MB; scratch heap probes after the `B`, `C` and `D1` classes
  and at the end). The `C` classes retain nothing (398 → 398 MB). One
  `final.desktop` producer run of the candidate ended with "Java heap space"
  just after the `C` classes; the next complete run passed. The largest
  transient raster of the `C` tests was reduced (the `m` case moved from `1:10`,
  a 4724 × 3543 px PNG, to `1:100`). Residual risk: an intermittent
  out-of-memory end of `final.desktop` that does not depend on `C`.
- New, pre-existing race: `PreG9BR6CapabilityMatrixTest.exportImageRows` failed
  once, in one `pre-g9b-r6-plus-c.desktop` producer run, with a
  `NullPointerException` inside `java.awt.Component.getMaximumSize` while an
  `AppGeoCeDG` was being constructed off the event thread
  (`GeoCeDGToolbarContainer.applyNativeToolPresentation` copying the native
  toolbar presentation); no file of that stack changes in `C`. The same class
  sequence passed in the other candidate runs and in four runs on the base; it
  was not reproduced deterministically.
- FreeHEP EMF+ provenance: the exact FreeHEP revision of `EMFPlusGraphics2D.java`
  and the `gdiplus` package is not establishable from public sources (recorded
  for the author in the provenance file).
- `PreG9BR6PlusA1HiddenLayerTest.java:188` line length: pre-existing Checkstyle
  warning, unchanged.

## 16. Smoke package

`models/regression/pre-g9b-r6-plus-c-export-completion/` holds eight fixture
documents built through the product API and written by the native writer of
the candidate (manifest with sizes and SHA-256), a README, and
`inspect-export.ps1`, a read-only helper that prints the DXF header, layer
table, group `60` count and `999` comments, the sidecar summary, the EMF frame,
the PDF page, the SVG size, PNG pixels and DPI, and the GeoCeDG LaTeX lines. A
scratch probe opened `c-layers-and-area.cedg`, exported DXF + sidecar, PDF, SVG,
EMF, PNG and PGF at `1:2`, and the helper reported `$INSUNITS = 5`,
`GEOCEDG_L3` OFF, one `60 = 1` entity, `far` outside the export area, and the
same 35 × 45 mm in the EMF frame, the PDF page and the SVG size.

## 17. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance campaigns. The acceptance campaigns on the frozen candidate are
reported with the candidate, outside this file.

| Run | Result |
|---|---|
| focal shared `PreG9BR6PlusCSharedExportTest` | 14 tests, 0 failures/errors |
| focal Desktop `PreG9BR6PlusC*` (5 classes) | 38 tests (identity 3, drawing scale 11, DXF 5, LaTeX 10, physical 9), 0 failures/errors |
| adjacent shared (`org.geocedg.common.export.*`, `org.geocedg.common.kernel.locus.*`, `PreG9BR6PlusA1LayerSeamTest`, `org.geocedg.common.layers.*`, `org.geocedg.common.units.*`, `PstricksTest`) | all passed |
| adjacent Desktop (`PreG9BR6*`, `export.*`, G9U1 action/profile/workspace, `GeoCeDGProfileTest`, `PostP1*`, `PreG9BR5AGuide*`, `PreG9BS1AuthorDxfReproductionTest`, `G9S1*`) | 367 tests; one failure of the first identity-test version (order-dependent `AutoColor`, §4 item 8), fixed and re-run in a polluted batch: pass |
| scratch base probes (`git archive e6135028`) | the 20 digests of §11, identical in two base runs and to the candidate |
| Checkstyle (four tasks, XML reports) | no finding in the touched sources after the fixes (one non-ASCII Javadoc character, one import order, declaration distances); the remaining `PreG9BR6PlusA1HiddenLayerTest.java:188` is pre-existing |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` | OK, 964 registered files |
| `git diff --check` | clean |
| `verify-g9x1-extended-dxf.ps1 -SkipBuild`, `verify-dxf.ps1 -SkipBuild` | §12 |
| producer `discovery.shared` (`--test-dry-run`) | completed, 6 008 identities |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 835 identities |
| producer `pre-g9b-r6-plus-c.shared` (executed) | 101 tests, 0 failures/errors |
| producer `pre-g9b-r6-plus-c.desktop` (executed) | 266 tests, 0 failures/errors (an earlier run of this producer hit the pre-existing toolbar race of §15 once) |
| producer `pre-g9b-r6-plus-a1.desktop` (executed) | 173 tests, 0 failures/errors |
| producer `pre-g9b-r6-plus-b.desktop` (executed) | 241 tests, 0 failures/errors |
| producer `final.shared` (executed, complete shared suite) | 6 925 tests, 0 failures/errors, 10 skips |
| producer `final.desktop` (executed, complete Desktop suite outside the isolated partition) | 1 829 tests, 0 failures/errors, 1 skip (an earlier run ended with the pre-existing heap exhaustion of §15) |
| complete Desktop suite with scratch heap probes, candidate and base | both passed; retained heap of §15 |
| capability-matrix class sequence on the base (`git archive e6135028`) | four runs, 15 tests each, 0 failures |
| `-PlanOnly` | `PHASE -Phase PRE-G9B-R6-plus-C` `COMPLETE`, 9 nodes (`execution_plan_hash` `f820b8dee078e51fd3ac0e1db2be373b1cc51227690814ea47e581559f08c142`); `INTEGRATION` `COMPLETE`, 46 nodes (`4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`) |
| development `STATIC` and `INFRA_UNIT` on the staged candidate tree | `ACCEPTED / COMPLETE` (`STATIC` `verification-0a6d5cddcd4e444689b3cb2f3fa5fc89`, 3/3; `INFRA_UNIT` `verification-986357c88e8f43a886fee401402d720b`, 22/22); the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; documentation, guide-structure, style and whitespace diagnostics clear (an earlier `STATIC` run, `verification-b7a27a06b1af4d518e39d6922204a340`, reported the five edited CRLF lines of the upstream manifest, then converted to LF); not acceptance evidence |

The skips are the pre-existing allowlisted ones.

**JUnit inventory and pins.** The current `junit_inventory` pin was reproduced first from the `HEAD` blob with `Get-VerificationCanonicalTextSha256` (`73d59962…`). The two new selections `pre-g9b-r6-plus-c.shared` and `pre-g9b-r6-plus-c.desktop` were added with placeholder counts and the input identity of the implementation branch start (`6cb09d55`, tree `00f80cc7`). Discovery evidence came from the `--test-dry-run` producers, executed evidence from the passing producer runs above, and `tools/agent/update-verification-junit-inventory.ps1`, run in session with absolute paths, wrote every count and hash: shared module discovery 5 994 → 6 008 (`af1d80f8…`), Desktop module discovery 1 797 → 1 835 (`3620d85f…`), `final.shared` 6 911 → 6 925 (`e160a603…`), `final.desktop` 1 791 → 1 829 (`2082ae7e…`), `pre-g9b-r6-plus-a1.desktop` 173 (`c0449c80…`, renamed method) and `pre-g9b-r6-plus-b.desktop` 241 (`7c1a1b55…`, renamed methods), and the new `pre-g9b-r6-plus-c.shared` (101, `b940ea44…`) and `pre-g9b-r6-plus-c.desktop` (266, `9f332349…`). Every other selection is unchanged. The registry catalog pin `junit_inventory` is now `8a0cf235…`; `static_contracts` is unchanged (no pinned input changed). No identity hash was typed by hand. The registry-shape pins in `tools/agent/tests/verification-final-coverage.Tests.ps1` follow: 38 selections, 45 PHASE selections, and the two new selection counts.

## 18. Author-smoke checklist (not performed or attributed by the agent)

Fixtures are in `models/regression/pre-g9b-r6-plus-c-export-completion/fixtures/`;
inspect any exported file with
`pwsh -NoProfile -File models\regression\pre-g9b-r6-plus-c-export-completion\inspect-export.ps1 <files>`.

1. **Scales in Picture and Print Preview.** Open `c-physical-cm.cedg`; File →
   Export → Graphics View as Picture…: the scale panel shows only the drawing
   scale with presets and "Construction unit: cm". At `1:1` export PDF, PNG
   (300 DPI), SVG and EMF: each is 18 × 8 cm (PDF page, SVG size, EMF frame;
   PNG 2126 × 945 px). At `1:2`: 9 × 4 cm. Type `2:4`: shown as `1:2`. Type
   `0:1` or `1:2000000001`: refused, the previous scale kept. Print Preview shows
   the same control and prints "Scale 1:2 (construction unit cm)".
2. **Measured PDF at `1:1`.** Print the `1:1` PDF at 100 %: the 10-unit ruler
   measures 10 cm.
3. **`presentationUnit`.** Open `c-physical-cm-presentation-mm.cedg`: the same
   sizes as step 1 at the same scales.
4. **mm, m, usm.** `c-units-mm.cedg` at `1:1`: 18 × 8 mm; `c-units-m.cedg` at
   `1:100`: 18 × 8 cm; `c-units-usm-inch.cedg` at `1:1`: 18 × 8 in (45.72 ×
   20.32 cm). Zooming the window changes none of them.
5. **Unspecified document.** `c-unspecified.cedg`: no drawing-scale control; the
   device modes read "Device scale (non-physical)", "Device scale from the screen
   (non-physical)", "Size in pixels (device)" with the no-construction-unit
   statement.
6. **Resets.** Set `1:5`; File → New: `1:1`. Set `1:5`; open any `.cedg`: `1:1`.
   Set `1:5`; open an `.html` export or a Base64 document: `1:1`. Set `1:5`; Undo
   after an edit, create or replace a tool with the same name, edit a macro, open
   a `.ggt`, Insert File, Apply Template: still `1:5`. Changing the scale never
   marks the document modified.
7. **LaTeX dialogs.** In `c-physical-cm.cedg` at `1:2`, open PSTricks, PGF/TikZ
   and Asymptote: the x and y units read 0.5 cm and are not editable; width and
   height follow the area; the code starts with the "GeoCeDG physical scale"
   comment. In `c-unspecified.cedg` the unit, width and height fields are
   editable device parameters with the statement.
8. **Hidden layer and hidden object.** `c-layers-and-area.cedg`: LaTeX omits
   `onHiddenLayer` and `hiddenObject`; the DXF (File → Export 2D geometry as
   DXF) contains both, `GEOCEDG_L3` OFF and `hiddenObject` with `60 = 1`; the
   report names the hidden layer.
9. **Export area.** In the same document (area from `Export_1`/`Export_2`): DXF
   leaves `far` out and reports one source outside the export area; `crossing`
   and the infinite `line` are written whole. Define an export area away from
   the window (File → Export area → Define): LaTeX exports exactly that area.
   Clear the area (back to the visible window): the DXF contains `far` again at
   every zoom.
10. **Semantic curves.** `c-semantic-curves.cedg` in each LaTeX dialog: `L` gives
    two paths, `C` one closed path, `S3` and `S5` approximate paths, each with
    its comment and "COMPLETE" in the report; the legacy `Locus` looks as
    before.
11. **Incomplete and failed Locus V2.** Open
    `models/regression/pre-g9b-s1-dxf/original/TestExport1.cedg`: LaTeX emits
    the certified components of `m`, marks the export "Incomplete" and the
    report says `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`; the DXF keeps refusing a
    strict complete write of `m`. In `c-semantic-curves.cedg` type tolerance
    `1E-300`: every Locus V2 fails with `WORK_LIMIT`, nothing is bridged, the
    report states it; tolerance `0` or `-1`: refused, no code.
12. **DXF units.** Export DXF from `c-units-mm.cedg`, `c-physical-cm.cedg`,
    `c-units-m.cedg`, `c-unspecified.cedg`: `$INSUNITS` 4, 5, 6, 0;
    coordinates identical in all four. `c-units-usm-inch.cedg`: the default
    (extended) flow writes `$INSUNITS 0`, the custom-unit comment and warning,
    and always a sidecar with `usm` and `0.0254`; started with
    `--enableExtendedDxf=false` (exact flow, no sidecar) the export is refused
    with the sidecar message and nothing is written.
13. **EMF in a consumer.** Insert the `1:1` EMF of step 1 into a word processor
    or vector editor that reports the picture size: 18 × 8 cm.
14. **Save preview.** Picture → Save with a new file name: no preview; choose an
    existing file of the same type with the chooser at least 600 px wide: the
    preview shows the pending export.
15. **Classic.** In the Classic diagnostic application, the Picture, Print
    Preview and LaTeX dialogs look and export as before (no drawing scale, host
    labels).

## 19. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: one registered PHASE selection with two JUnit selections through the
existing registry, inventory and updater; registry-shape pins in tools/agent/tests
follow the R4/R6/A-1/B precedent; the live specification pin of the historical
G9X1 verifier under DQ-C15; no verifier, schema or profile-composition
semantics change.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md;
  geocedg/specs/ui/cedg-workspaces.md
SERIALIZATION CHANGE = NONE
```

## 20. Changed paths

- Licensing and provenance: `THIRD_PARTY.md`, `docs/licensing/component-matrix.md`, `docs/licensing/freehep-vectorgraphics-provenance.md`, `geocedg/validation/pre-g9b-r6-plus/freehep-vectorgraphics-provenance.json`.
- Modified third-party source: `source/desktop/desktop/src/main/java/org/freehep/graphicsio/emf/EMFGraphics2D.java`, `source/desktop/desktop/src/main/java/org/freehep/graphicsio/emf/EMFOutputStream.java`, `source/desktop/desktop/src/main/java/org/freehep/graphicsio/emf/EMFPlusGraphics2D.java`.
- Shared upstream seams: `source/shared/common/src/main/java/org/geogebra/common/export/pstricks/GeoGebraExport.java`, `source/shared/common/src/main/java/org/geogebra/common/main/App.java`.
- Shared GeoCeDG-owned: `source/shared/common/src/main/java/org/geocedg/common/export/DxfExporter.java`, `ExportAreaParticipation2D.java`, `G9X1GeometryExportAdapter.java`, `GeoElementGeometryExportAdapter.java`, `GeometryExportArea.java`, `GeometryExportContext.java`, `GeometryExportModel.java`, `GeometryExportPreflight.java`, `GeometryExportService.java`, `PhysicalExportLimitException.java`, `PhysicalExportScale.java`, `SemanticCurveExportAdapter2D.java`, `SemanticExportClassification.java` (same directory); `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedComponentAreaDisjointness2D.java`.
- Desktop upstream: `source/desktop/desktop/src/main/java/org/geogebra/desktop/euclidian/EuclidianViewD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/GraphicExportDialog.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/PrintScalePanel.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/export/pstricks/ExportFrame.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java`, `source/desktop/desktop/src/main/java/org/geogebra/desktop/plugin/GgbAPID.java`.
- Desktop GeoCeDG-owned: `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`, `GeoCeDGDxfExportController.java`, `GeoCeDGExportScalePresentation.java` (same directory); in `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/`: `DrawingScale.java`, `DrawingScaleControl.java`, `DrawingScaleHolder.java`, `DxfExportPreflightPresentation.java`, `DxfFidelityManifestWriter.java`, `DxfManifestEncoding.java`, `ExportScalePresentation.java`, `ExportViewport.java`, `GeoCeDGGeoGebraToAsymptote.java`, `GeoCeDGGeoGebraToPgf.java`, `GeoCeDGGeoGebraToPstricks.java`, `GeoCeDGLatexSettings.java`, `LatexExportPanel.java`, `LatexSemanticExporter.java`, `LatexSemanticExportSupport.java`, `PictureExportCommandLine.java`, `PictureExportRoute.java`, `PictureExportService.java`.
- Profile: `apps/geocedg/application-profile.yml`.
- Tests: `source/shared/common-jre/src/test/java/org/geocedg/common/export/PreG9BR6PlusCSharedExportTest.java`; in `source/desktop/desktop/src/test/java/org/geocedg/desktop/`: `PreG9BR6PlusCBaseIdentityTest.java`, `PreG9BR6PlusCDrawingScaleTest.java`, `PreG9BR6PlusCDxfDesktopTest.java`, `PreG9BR6PlusCLatexExportTest.java`, `PreG9BR6PlusCPhysicalExportTest.java`, `PreG9BR6PlusA1HiddenLayerTest.java`, `PreG9BR6PlusBExportSurfaceTest.java`, `PreG9BR6PlusBPictureFidelityTest.java`, `PreG9BR6PlusD1ExportRegressionTest.java`, `export/G9X1DxfManifestTest.java`, `export/G9X1PairedOutputTest.java`.
- Specifications and ADR: `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md`, `geocedg/specs/export/geometry-export-foundation.md`, `geocedg/specs/units/unit-system.md`, `geocedg/specs/ui/cedg-workspaces.md`, `docs/adr/0005-neutral-2d-geometry-export.md`.
- Verifier pin: `tools/agent/verify-g9x1-extended-dxf.ps1`.
- Verification registration: `geocedg/specs/operations/verification-junit-inventory.json`, `geocedg/specs/operations/verification-registry.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Smoke package: `models/regression/pre-g9b-r6-plus-c-export-completion/` (`README.md`, `manifest.yml`, `inspect-export.ps1`, eight `fixtures/*.cedg`).
- Documentation and evidence: `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, `docs/developer/geocedg_developer_guide.md`, `docs/roadmap/geocedg_roadmap.md`, `docs/upstream/modified-files.yml`, `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`, `docs/validation/pre_g9b_r6_plus_c_candidate_report.md`, `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-c-candidate-evidence.json`.

# PRE-G9B-R6-plus-C — 2D export completion and semantic curve exporters

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-04, confirmed by the author in
writing in the session, approves the documentary preparation package of
`PRE-G9B-R6-plus-C` (`T_R6PLUS_C_PROMPT` and its reconciliation
`D_R6PLUS_C_PROMPT`) as `PASS — AUTHOR APPROVED`, names the reconciliation
candidate as the exact start of the implementation branch, gives the author
dispositions on `DQ-C1` to `DQ-C17`, disposes of
`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`, freezes `INTEGRATED_PHASE` and
authorizes the implementation of `C`. The instruction is recorded, versioned, in
the [C preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_c_prompt_closeout_record.md).
It requires this amendment, as the first tracked edit of the phase, so that the
prompt becomes the executable contract of the phase. The amendment replaces the
reconciled prompt of `D_R6PLUS_C_PROMPT` (blob
`514dee31a64b55e648ed5a9c374ceb1dc1b28b1c`): it records the authorized branch
start, integrates the `DQ-C` dispositions and replaces the pending and default
language they supersede. Where the reconciled prompt and the record differ, the
record prevails; this amendment carries that precedence into the text. Every
other scope, forbidden-scope and stop rule is kept.

History of this file. This prompt was prepared at the author's instruction of 2026-10-04. That
instruction named the published base below, fixed the semantic inputs that `C`
inherits from `B`, `D0`/`D1` and `A-2`, the planned verification class, the
mandatory characterization (`C1`–`C18`), the invariants, the compatibility
corpus, the verification design, the forbidden scope and the stop conditions,
and authorized only the documentary preparation of this prompt, a
characterization report, the record of those inputs and of the pending
questions, their machine-readable mirrors and the status updates they require.
It stated that it **does not authorize the implementation of `C`**. The inputs
are recorded, versioned, in the
[C author-decision record](../../../docs/validation/pre_g9b_r6_plus_c_author_decisions_record.md);
the evidence behind the characterization below is in the
[C preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_c_preparation_characterization_report.md).

**Documentary reconciliation (author instruction of 2026-10-04).** The author
reviewed the prepared candidate `T_R6PLUS_C_PROMPT`
(`5ad96304ae9718c296db753b4822130cf3d99b61`, tree
`1596b5153681828c9c3206b98ca9ebd31505e765`), found it acceptable in general,
kept it intact and did not authorize implementation. The same instruction
required this linear documentary descendant (`D_R6PLUS_C_PROMPT`), which only:
replaces the prepared default of `DQ-C5` with the author direction on local
admissibility versus global completeness (C14); replaces the prepared
`drawingScale` lifecycle of `DQ-C7` with the author direction that it resets
only on a successful effective document transition (C2); re-characterizes
`DQ-C13` after the author rejected the prepared default "DXF ignores
`ExportArea`", with an alternative matrix and a new recommendation that stays
pending (C9); and amends the implementation constraints of `DQ-C9`, which stays
pending. Every other contract of the prepared prompt is kept. The instruction
is recorded in the
[C author-decision record](../../../docs/validation/pre_g9b_r6_plus_c_author_decisions_record.md)
§1.1 and §3; the evidence is in the characterization report §11.

This file is an execution contract, not a second policy document: the unit
rules are stated once in the
[unit-system specification](../../../geocedg/specs/units/unit-system.md)
(§12, §13, §15, §18.2), the export-area rules once in the
[B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md)
and the [B closeout record](../../../docs/validation/pre_g9b_r6_plus_b_closeout_record.md),
and the hidden-layer rules once in the
[A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md)
(`AQ-L3`) and the [A-2 closeout record](../../../docs/validation/pre_g9b_r6_plus_a2_closeout_record.md);
this prompt cites them and does not restate them differently.

```text
PRE-G9B-R6-plus-C =
AUTHORIZED FOR IMPLEMENTATION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (C implementation and technical verification only)
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — ENGINEERING drawingScale, PHYSICAL
                           PICTURE SIZING, DXF UNIT HEADER, LaTeX ExportArea AND
                           EFFECTIVE VISIBILITY, SEMANTIC LocusV2/SplineV2 LaTeX OUTPUT
DEPENDS_ON               = PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (hard: effective-visibility rule)
                           PRE-G9B-R6-plus-B   = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (hard: single ExportArea authority)
                           PRE-G9B-R6-plus-D1  = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (hard: unit contract; D0 normative)
                           PRE-G9B-R6-plus-A-2 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (recommended predecessor; persistent
                                                 hidden layers)
DEPENDS_ON_PACKAGE       = C PREPARATION PACKAGE = PASS — AUTHOR APPROVED (not published)
DISPOSITIONS             = DQ-C1 to DQ-C17 decided by the author (2026-10-04)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-E1    (operational order; not authorized)
STOP_STATE               = PRE-G9B-R6-plus-C = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
```

`implementationAuthorized = true` authorizes only the implementation and
technical verification defined below. It authorizes no later subphase, neither
observation `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` nor
`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`. `authorApproved =
false` means that no technical candidate of this phase has been
author-approved. Technical verification never creates author approval. The
phase stops with one exact technically verified candidate pending author review
and author smoke, with `selfApproved = false`, `authorApproved = false` and
`passClaimed = false`. The agent does not perform or claim author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only:

1. the `SESSION` engineering `drawingScale = a:b` of the unit-system
   specification §12, with one runtime holder per application/document window,
   its lifecycle (C2, C16) and its engineering-scale user interface (C3);
2. physical sizing of the picture and print exporters from the effective
   construction unit and `drawingScale` (unit-system §13 and §15.2), replacing
   the zoom-derived `printingScale` as physical authority (C4), with the
   opt-in exact EMF frame of `DQ-C9` (C5);
3. the DXF unit header, the neutral-model unit field, the `usm` warning and
   mandatory sidecar, the versioned sidecar unit metadata and the staleness
   fingerprint (unit-system §15.2, §15.3; C6, C7), together with hidden GeoCeDG
   layers emitted as OFF DXF layers and individually hidden objects kept with
   `60 = 1` (`DQ-C2`, `DQ-C3`; C8) and `B1` participation in the explicit
   `ExportArea` (`DQ-C13`; C9);
4. the integration of PSTricks, PGF/TikZ and Asymptote with `B`'s single
   `ExportArea`, `A`'s effective visibility and the unit contract (C9, C17);
5. read-only semantic `LocusV2`/`SplineV2` output in PSTricks, PGF/TikZ and
   Asymptote through one shared semantic curve export adapter reused by the
   G9X1 DXF path (C10–C14);
6. the coherent amendment of the export specifications, verifier pins,
   documentation and tests that the delivered behavior requires, in the same
   candidate (C7, *Required design/specification*);
7. the tests and compatibility evidence of its class.

```text
drawingScale        = a:b, a and b positive integers, normal form (a/g):(b/g),
                      default 1:1, SESSION, never serialized, never undoable,
                      reset to 1:1 on New, Open and every effective full-document
                      replacement                                   (unit-system §12)
output(L)           = L · fb(constructionUnit) · a / b   metres     (unit-system §13)
PDF points          = metres · 72 / 0.0254
raster pixels       = metres · dpi / 0.0254
LaTeX xunit = yunit = fb(c) · 100 · a / b   centimetres per model unit
presentationUnit    = expression only; never a scale factor          (AQ-U1)
UNSPECIFIED_MODEL_UNIT = no engineering scale, no 1:n label, no cm inference,
                      DXF $INSUNITS = 0, neutral model UNITLESS      (AQ-U6, DQ-D0-11)
effectiveVisible(g) = objectVisible(g) AND NOT layerHidden(g.layer)  (AQ-L3)
ExportArea          = the single B authority, consumed by picture, LaTeX and DXF;
                      LaTeX bound edits write MANUAL; DXF consumes explicit areas
                      by B1 participation, never by clipping            (DQ-C13)
semantic curves     = kernel semantics, export adapter orchestrates
                      approximation, format writer formats; render tessellation
                      is never export authority
local admissibility != global completeness: a certified valid component may be
                      emitted when global completeness is not established, never
                      as a claim of completeness                  (DQ-C5)
```

Model coordinates, the DAG, object identity, Locus V2 branch and component
identity, Spline V2 semantics and the document XML never change because of a
unit, a scale, an export area, a hidden layer or an export. The only truths that
differ after `C` are export outputs, export dialogs, the session `drawingScale`,
and the specification, verifier and documentation text that describes them.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = INTEGRATED_PHASE        (frozen by the author's authorization of
                                                2026-10-04)
frozenAtPhaseStart   = true
PLANNED_ACCEPTANCE   = registered PHASE -Phase PRE-G9B-R6-plus-C + INTEGRATION,
                       both ACCEPTED / COMPLETE on the same exact frozen clean commit
                       and tree, with fresh log directories; no FINAL unless a stop
                       condition forces reclassification
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` maps
`INTEGRATED_PHASE` to `PHASE`, with `COMPOSED` (`INTEGRATION`) when the frozen plan
identifies concrete additional integration coverage. The impact facts that
select it, re-characterized at the base: the shared upstream LaTeX package
`common/export/pstricks` (consumed by Classic, by the JavaScript API and compiled
for Web) gains minimal host-identical seams; the Desktop picture, print-preview
and LaTeX dialogs shared with Classic gain GeoCeDG-gated scale presentation; the
shared GeoCeDG export package gains a semantic curve adapter extracted from the
G9X1 DXF adapter, a unit-aware neutral model and a unit-aware DXF header; three
LaTeX exporters, the picture exporters and DXF must agree on one export area,
one unit contract and one effective-visibility rule, which is concrete
integration coverage beyond a phase selection. No document serialization, XML
parser, undo snapshot, kernel algorithm, command semantics or global verifier
architecture changes (C2, C18). The `D1` and `A-2` `FINAL` runs are not a reason
to require `FINAL` here.

Escalation triggers. Stop and request reclassification for author review,
rather than reclassify silently, when correctness would require document
serialization (of `drawingScale`, export area, tolerance or any export state),
a change of the shared XML parser or undo snapshot, a change of global renderer
semantics, a material change of Classic export semantics, a change of the
global verifier architecture, or a second FreeHEP graphics family beyond what
the author disposes in `DQ-C9`.

The reconciliation re-checked the class against its three changes: the `DQ-C5`
result model is export-layer reporting derived from existing outcomes; the
`DQ-C7` lifecycle adds Desktop reset calls at existing hooks plus one
GeoCeDG-gated `GgbAPID` marker; every `DQ-C13` alternative stays in the export
package, the DXF controller and the sidecar. None serializes, touches the
kernel, the parser, undo or the verifier architecture: `INTEGRATED_PHASE`
holds. The author chose `B1` for `DQ-C13` and accepted the limited opt-in
FreeHEP EMF change of `DQ-C9` inside `INTEGRATED_PHASE`; the certified
Locus V2 participation test (C9-DXF) adds one read-only kernel query over the
existing ADR 0028 certified interval model and changes no semantics. The class
is frozen; a stop condition that forces reclassification is reported to the
author before any heavier campaign.

### Authorities and author decisions this prompt implements

| Authority | Effect in `C` |
|---|---|
| [unit-system specification](../../../geocedg/specs/units/unit-system.md) v1.0 (`NORMATIVE / AUTHOR APPROVED`) §12, §13, §15, §18.2 | `drawingScale`, physical output, the future export rules and `C`'s obligations; `C` implements them and does not reopen them |
| [ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md) Decision 8 and consequences | export stays an adapter; `C` changes the foundation, the G9X1 contract, its verifier pin, the exporter and the sidecar together; ADR 0005 is not amended by ADR 0032 (`DQ-C17`) |
| [D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md) `AQ-U1`, `AQ-U5`, `AQ-U6`; [D0 preparation closeout](../../../docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md) `DQ-D0-10`, `DQ-D0-11`, `DQ-D0-12` | size invariance under `presentationUnit`; `drawingScale` `SESSION` 1:1; unspecified unit; `usm` DXF rule; the `a:b` pair |
| [D1 closeout record](../../../docs/validation/pre_g9b_r6_plus_d1_closeout_record.md) and the accepted `D1` implementation | the unit state API (`Construction.getUnitSystem()`, `UnitState`), its notification, and the `D1` export regression that `C` supersedes on purpose |
| [B author-decision record](../../../docs/validation/pre_g9b_r6_plus_b_author_decisions_record.md) `AQ-X2`, scope boundary; [B prompt closeout](../../../docs/validation/pre_g9b_r6_plus_b_prompt_closeout_record.md) `DQ-B4`, `DQ-B5`, `DQ-B7`, `DQ-B8`; [B closeout record](../../../docs/validation/pre_g9b_r6_plus_b_closeout_record.md) | the single session `ExportArea`, producer precedence, `MANUAL` through world bounds, `sourceViewId`, the offscreen export viewport, the PDF opt-in, the observations owned by `C`, `ENH-B-MANUAL-EXPORT-AREA-DRAG` (not in `C`) |
| [A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md) `AQ-L3`; [A-2 closeout record](../../../docs/validation/pre_g9b_r6_plus_a2_closeout_record.md) | hidden layers are `DOCUMENT_PRESENTATION`; objects on hidden layers are excluded from LaTeX; the DXF policy is `C`'s (`DQ-C2`); `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` to characterize; `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` not absorbed |
| [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md) (`Experimental`, ADR 0005); [DXF curve fidelity](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md) v1.0 (`NORMATIVE / AUTHOR APPROVED`, ADR 0014) | in force unchanged until `C` delivers its amendment in the same candidate (C7) |
| [C design candidate](../../../docs/architecture/pre_g9b_r6_plus_c_semantic_export_design_candidate.md) and the [P0 report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md) | evidence only; every seam re-established below; where they differ, this prompt records the correction |

No open decision of any other subphase is resolved here.

### Architecture decision: layers and owners

| Concern | Owner | Why |
|---|---|---|
| `drawingScale` value (`a:b`, normal form, parsing) | an immutable GeoCeDG value type in the Desktop export package (`org.geocedg.desktop.export`) | export and presentation state (`AGENTS.md` §4: engineering export scale is an export formatting concern), never geometry |
| `drawingScale` runtime holder and lifecycle | one field on `AppGeoCeDG` per window, reset only at the successful-transition events of C2 | the only per-window owner that every exporter of that window reaches; never `Construction`, XML, preferences, undo or `ExportAreaSession` |
| physical scale for picture, print and LaTeX | one GeoCeDG computation, `fb(effC) · 100 · a / b` centimetres per model unit, reached through host-identical `AppD`/`App` seams | replaces the zoom-derived `printingScale` as physical authority without a second authority (C4) |
| engineering-scale and device-mode UI | Desktop host dialogs gated by the seams, GeoCeDG presentation in GeoCeDG-owned components where the host layout allows | product UI; Classic keeps its dialogs (C18) |
| LaTeX bounds, bound edits, effective visibility | minimal host-identical seams in shared `common/export/pstricks` and `common/main/App` | the dialect classes are shared upstream code read by Classic, the API and Web |
| semantic curve export adapter and its per-component outcomes | shared GeoCeDG export package `org.geocedg.common.export`, extracted from `G9X1GeometryExportAdapter` | one read-only semantic export authority for DXF and LaTeX; no second copy (C10) |
| export result classification (`COMPLETE`, `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `REJECTED / NO_ADMISSIBLE_OUTPUT`) | the same package, computed from the per-component outcomes; each format applies its own admissibility policy | export reporting, never kernel state (C14) |
| dialect formatting of semantic curves | GeoCeDG-owned writers; the upstream dialect classes only gain a host-identical dispatch hook (C10, C12) | format writers format; the kernel is not an export formatter |
| DXF header, neutral-model unit, sidecar, fingerprint | shared GeoCeDG export package and the Desktop DXF controller and sidecar writer | already GeoCeDG-owned (G5/G9X1) |
| kernel | no semantic change; one read-only public query over the existing ADR 0028 certified interval model (C9-DXF) | no new geometric semantics (`AGENTS.md` §4, §13); the certified proof stays kernel authority |

No geometry algorithm, command or construction element reads `drawingScale`,
the unit state for export, the export area or the hidden-layer set. The
semantic evaluators stay authoritative and are only read.

### Characterization results at the base

The preparation re-read every seam below at the base, with scratch-only probes
where useful; the evidence, the probe identities and their outputs are in the
[characterization report](../../../docs/validation/pre_g9b_r6_plus_c_preparation_characterization_report.md).
Paths use `common/` = `source/shared/common/src/main/java/org/geogebra/common/`,
`geocedg-common/` = `source/shared/common/src/main/java/org/geocedg/common/`,
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`,
`freehep/` = `source/desktop/desktop/src/main/java/org/freehep/graphicsio/`,
`jre-test/` = `source/shared/common-jre/src/test/java/org/geocedg/common/` and
`dtest/` = `source/desktop/desktop/src/test/java/org/geocedg/desktop/`. The phase
re-establishes each citation before relying on it. A result marked
**contract** is binding on the implementation; a result marked **finding** is
evidence that a contract rests on. Where a `DQ-C` disposition changes a
contract, the disposition governs.

#### C1. Export-route map (finding)

Notation: `W`, `H` world extent of the effective area; `sx`, `sy` view pixels
per model unit; `p` the view `printingScale` (cm per model unit); `p*` =
`ExportViewport.printingScaleOf(view)` (`p`, or the host rule for a view not laid
out, `geocedg-desktop/export/ExportViewport.java:120-127`); `D` DPI; `s` export
scale (output units per view pixel).

| Route | Area source | Size computation | DPI | `printingScale` | View scale | Physical or device | Hidden layers | Locus V2 |
|---|---|---|---|---|---|---|---|---|
| PNG, dialog, *Scale in cm* | `ExportAreaSession.resolve` (`geocedg-desktop/export/ExportAreaSession.java:65-79`) | `s = p·D/(2.54·sx)` (`desktop/export/GraphicExportDialog.java:576`); raster `round(W·sx·s)` (`geocedg-desktop/export/PictureExportService.java:113-116`, `:295-300`) = `W·p` cm at `D` | resolution and metadata | `p` | cancels | physical, zoom-derived | `EuclidianView.drawObjects` → `isOnShownLayer` (`common/euclidian/EuclidianView.java:3606-3607`, `:3661`) → `AppGeoCeDG.isLayerShown` (`geocedg-desktop/AppGeoCeDG.java:474-476`) | `DrawLocusV2` adaptive, 0.75 px / export scale (`geocedg-common/euclidian/draw/LocusRenderPolicy2D.java:21-24`, `:102-108`) |
| PNG, dialog, *Fixed size* | same | `100 screen px = c cm` writes `p = c·sx/100` (`desktop/export/PrintScalePanel.java:336-345`; `GraphicExportDialog.java:556-571`) | yes | written from zoom | yes | zoom-coupled device | same | same |
| PNG, dialog, *Size in pixels* | same | `s = N/(W·sx)` (`GraphicExportDialog.java:546-551`) | metadata | — | yes | device | same | same |
| PDF (dialog, API, CLI, `ExportImage`) | same | points per pixel `p*·72/(2.54·sx)`; page `W·p*` cm exact (`PictureExportService.java:156-185`; opt-in `setExactPageSize`) | none | `p*` | cancels | physical, zoom-derived | same | resolution ≈ 1 |
| SVG, dialog | same | exact `viewBox` in view px; `width`/`height` = px · `cmWidth/frameWidth` = `W·p` cm (`PictureExportService.java:191-202`; `GraphicExportDialog.java:871-873`) | fixed 72 (`:462-465`) | `p` | `viewBox` in view px | physical (cm) | explicit layer loop (`PictureExportService.java:217-221`) | export scale |
| SVG, API and CLI | same | `width="W·sx px"` | — | — | yes | device | same | same |
| EMF/EMF+, dialog | same | device bounds `round(W·p·D/2.54)` with `D` the **hidden** DPI combo (default 300; `GraphicExportDialog.java:258-260`, `:322-330`, `:462-468`); `rclFrame = ⌊bounds·31.25⌋` (0.01 mm) from the FreeHEP 1024 px = 320 mm reference (`freehep/emf/EMFGraphics2D.java:229-232`; `freehep/emf/EMFHeader.java:15`, `:63-68`) | hidden, still applied | `p` via `s` | yes | **device; frame = `W·p·D/81.28` cm**, not the labelled `W·p` cm (probe `P-C-EMF`, ratio 3.6875 at 300 DPI) | `drawObjects` | export scale |
| Graphics clipboard (`Ctrl`+`Shift`+`C`, menu) | same | `s = 2`, or the 500 000 px budget (`PictureExportService.java:60-61`, `:273-286`) | none | none | yes | device | `drawObjects` | export scale |
| Print, Print Preview | same | `s = (PRINTER_PIXEL_PER_CM/sx)·p` in 1/72 in page units (`desktop/euclidian/EuclidianViewD.java:371-380`); the preview embeds `PrintScalePanel` (`desktop/export/PrintPreviewD.java:440-443`) | none | `p` | cancels | physical, zoom-derived | `drawObjects` | export scale |
| CLI `--export` | same (settings fallback) | `s = p*·dpi/(2.54·sx)` or `--maxSize` (`geocedg-desktop/export/PictureExportCommandLine.java:50-60`) | metadata | `p*` | yes | PNG/PDF physical, zoom-derived; SVG/EMF device | as format | as format |
| `ExportImage` | through `getExportFrameWidth` | own `scalecm` (cm per unit; `common/kernel/commands/CmdExportImage.java:105-166`), DPI default 300 | yes | PDF: `p*` | yes | explicit parameter | as format | as format |
| Save preview | same | 512 × 512 thumbnail of the pending export (`AppGeoCeDG.java:543-552`; `PictureExportService.java:254-264`) | — | — | — | preview | same | same |

`p` is a per-view field recomputed from the zoom on every coordinate-system
change as a power of ten (`common/euclidian/EuclidianView.java:1932-1936`,
`:940-946`), never serialized, and editable through `PrintScalePanel` without
any range check (`PrintScalePanel.java:280-290`, `:336-345`). Probe
`P-C-PRINTING-SCALE`: the same 4-unit area gives a 4 cm PDF page at 20, 37.8 or
50 px per unit, 40 cm at 100–500 px per unit and 400 cm at 4000 px per unit.
`D1` proved every picture output independent of the unit state
(`dtest/PreG9BR6PlusD1ExportRegressionTest.java:70-96`).

| LaTeX route | Bounds | Scale and unit | Visibility | Locus V2 / Spline V2 | Coordinates | UI | Writes `selectionRectangle` |
|---|---|---|---|---|---|---|---|
| PSTricks, PGF/TikZ, Asymptote (Desktop dialogs) | read once in the constructor: the selection rectangle if any, else the active view bounds (`common/export/pstricks/GeoGebraExport.java:126-133`, `:240-258`); `ExportArea`, `Export_1`/`Export_2` never read | `xunit = yunit = 1` cm per unit by default (`:241-242`); editable unit, width and height fields coupled per axis (`desktop/export/pstricks/ExportFrame.java:130-148`, `:388-418`); `printingScale` never read; output `\psset{xunit=…cm}` (`GeoGebraToPstricks.java:1656-1659`), `x=…cm` (`GeoGebraToPgf.java:2531-2536`), `size(…cm)` (`GeoGebraToAsymptote.java:245`) | `isWhollyIn2DView(EV1) && isEuclidianVisible()` in construction order (`GeoGebraExport.java:351-357`, `:1278-1295`); the layer is never read | **silently omitted**: no `GeoLocus`, so `Log.debug("Export: unsupported GeoElement …")` (`:455-459`); probe `P-C-LATEX-SEMANTIC`: byte-identical output with and without a visible `LocusV2` and `SplineV2` | `kernel.format` with `Double.toString` then `canonicalNumber2` (`:143-146`) | text area, Generate, Copy, Save As; no preview, status or report area (`ExportFrame.java:215-255`, `:312-315`) | yes: bound edits (`GeoGebraExport.java:162-171`, `:177-207`; `ExportFrame.java:437`, `:458`, `:480`, `:501`) |
| JavaScript API `exportPGF`/`exportPSTricks`/`exportAsymptote` | constructor bounds | `ExportFrameMinimal`, unit 1 (`common/plugin/GgbAPI.java:2078-2118`) | same | same | same | none | no |

| DXF component | Base behavior | Evidence |
|---|---|---|
| population | complete 2D construction (`geocedg-dxf-geometric-2d/v1`) or explicit selection; no view, no viewport, no `ExportArea` | `geocedg-desktop/GeoCeDGDxfExportController.java:458-464`; `geocedg-common/export/GeometryExportPopulation2D.java:27` |
| neutral model unit | `Unit { UNITLESS }`, constant source and target | `geocedg-common/export/GeometryExportModel.java:26-31`, `:560-567` |
| header | `$ACADVER = AC1015`, `$INSUNITS` group 70 = 0; no `$MEASUREMENT`, `$LUNITS`, `$EXTMIN` | `geocedg-common/export/DxfExporter.java:101-112` |
| layer table | `0` and `GEOCEDG_L<n>`; every layer on (`62 = 7`, `70 = 0`) | `DxfExporter.java:114-145` |
| hidden objects | included, `60 = 1`; preflight counts them; warning `HIDDEN_SOURCE_INCLUDED` | `DxfExporter.java:300-306`; `GeometryExportPreflight.java:63-65` |
| hidden layers | ignored: written as visible entities on an on layer | no export reference to the layer workspace |
| Locus V2 | one approximate `LWPOLYLINE` per certified continuous valid component, model coordinates, model-coordinate tolerance (dialog default `0.001`), `ESTIMATED_ERROR` | `geocedg-common/export/G9X1GeometryExportAdapter.java:288-388`; `GeoCeDGDxfExportController.java:56` |
| sidecar | `org.geocedg.dxf.fidelity-manifest` v1; `dxf.source_unit`/`target_unit` = `"unitless"`; required when not all exact, coverage incomplete or requested | `geocedg-desktop/export/DxfFidelityManifestWriter.java:110-146`; `GeometryExportPreflight.java:95-96` |
| staleness fingerprint | spatial section plus construction elements; unit state and hidden layers excluded | `G9X1GeometryExportAdapter.java:1049-1060` |
| dialog | fixed text `Cartesian 2D world / UNITLESS` | `GeoCeDGDxfExportController.java:276-277` |

#### C2. `drawingScale` lifecycle (contract; author direction of `DQ-C7`)

No holder exists at the base (no `drawingScale`, `DrawingScale` or engineering
scale in `source/**`; only the unrelated `EuclidianView.getScaleRatio`). The
minimal owner is one immutable value held by `AppGeoCeDG`, one per window:
every window has its own `AppGeoCeDG` (`GeoCeDGFrame`/`GeoGebraFrame.createNewWindow`),
and every exporter reaches that application (`GraphicExportDialog` field `app`,
`PrintScalePanel` through `ev.getApplication()`, `ExportFrame` through
`ggb.getApp()`, `GeoCeDGDxfExportController` field `app`). It is not
`Construction` state (every undo restore clears it), not XML or preferences, not
geometry and not a property of `ExportArea` (`ExportAreaSession.resetForDocument`
carries the open observation of C16).

Author direction (2026-10-04): `drawingScale` is window/session export state,
default `1:1`, and resets **only on a successful effective document
transition** — a completed File → New, or a successful full-document
replacement or Open, independently of transport. It stays unchanged through
undo, redo, redefine, rollback, macro and tool processing, generic merge and
`evalXML`, non-document `setXML` and temporary construction clearing. Lifecycle
events, not method names, decide; in particular neither the `clearConstruction`
override nor `commitLoadedDocument` is a reset point.

Why the prepared points are wrong: `AppGeoCeDG.clearConstruction` also runs for
the Open URL dialog's pre-clear (`desktop/gui/GuiManagerD.java:1839-1843`, a
clear before any document is chosen); `commitLoadedDocument` is also reached by
every clearing `setXML` (`AppGeoCeDG.java:445-456`), and the clearing `setXML`
is also the tool-replacement reload. Probe `P-C7-TOOL-REPLACE` proves it at the
base: replacing a tool through the shared overwrite route
(`common/gui/dialog/ToolCreationDialogModel.java:272-291`; Desktop
`desktop/gui/dialog/ToolCreationDialogD.java:223-233`, `:265-294`) reloads the
document window with `setXML(getXML(), true)`, which reaches the `A-2` commit:
the working layer moved from 2 to 6 while the construction stayed identical.

Successful-transition events (the reset points):

| Event | Seam at the base | Entry routes | Fires only on success |
|---|---|---|---|
| `E1` completed File → New | `AppGeoCeDG.fileNew`, after `super.fileNew()` when `clearedInFileNew` (`AppGeoCeDG.java:695-715`, the `A-2` revision 3 saved-baseline point) | profile New (`GeoCeDGActionRegistry`), v1 `FileMenuD`, `GgbAPID.fileNew`, `GgbAPI.newConstruction` | yes: a cancelled save prompt returns before it |
| `E2` native Open committed | `AppGeoCeDG.nativeDocumentLoadCommitted` (`:396-401`; host call `desktop/main/AppD.java:3311` after `loadNativeArchive` commits) | chooser, recent, file drop, macOS open, API `openFile` (file and URL), Open URL with a native document, `reset()` with a current file | yes |
| `E3` non-native full-document replacement committed | `AppGeoCeDG.documentReplacementCommitted` (`:409-414`; host calls `common/main/App.java:4305`, `desktop/headless/GFileHandler.java:111` for non-macro streams) | API `setBase64`, Base64 Open URL, `.html` with `ggbBase64`, non-native file and URL streams | yes |
| `E4` successful document load through `runDocumentLoad` | `AppGeoCeDG.runDocumentLoad` when `loaded` (`:421-430`), the path of `loadExistingFile` (`:736-749`) and `loadXML(String)` (`:751-762`) | File → Open and recent files (with `E2`/`E3` inside), Open URL with XML text (`GuiManagerD.java:1900`) | yes |
| `E5` API document replacement | **no distinguishing seam at the base**: `GgbAPI.setXML(String)` ("Opens construction given in XML format", `common/plugin/GgbAPI.java:1470-1480`) reaches the same `App.setXML(xml, true)` as the tool reload | JavaScript `setXML` | needs a narrow positive marker: a GeoCeDG-gated `GgbAPID.setXML` override that scopes the call as a document replacement, with the reset taken when `AppGeoCeDG.setXML` observes the completed parse (the `A-2` parse report) |
| `E6` reset to a blank document | `AppD.reset()` without a current file → `clearConstruction` (`desktop/main/AppD.java:562-568`); reached by the graphics-view reset icon (`common/euclidian/EuclidianController.java:8600-8602`, `:10630-10632`) and `GgbAPI.reset` | — | a GeoCeDG `reset()` override that resets after a successful clear; `D1` and `A-2` already treat this blank document as a new document (`AppGeoCeDG.java:726-731`) |

Open URL is not in the GeoCeDG profile menu (the profile targets are
`host.document.*` without it, `GeoCeDGActionRegistry.java:62-64`); it is reachable
from the v1 fallback, URL drops and the API, which the events above cover.
Several events may fire for one transition (for example `E2` inside
`runDocumentLoad`); a reset to `1:1` is idempotent. Startup and New Window
create a new application at `1:1`.

Routes that must leave `drawingScale` unchanged, with their current behavior:

| Route | Base evidence | Why it is not a document transition |
|---|---|---|
| tool replacement (Create New Tool with an existing name → Replace; save from a macro-edit window) | `ToolCreationDialogD.overwriteMacro` → `appToSave.setXML(appToSave.getXML(), true)` (`:290-294`); shared `ToolCreationDialogModel.overwriteMacro` (`:286-291`) | same-document reload; probe `P-C7-TOOL-REPLACE` |
| macro-edit window | `ToolManagerDialogD.openTools` → new window (`:274-284`) → `loadMacroFileFromByteArray` → `App.openEditMacro` → `setXML(newXml, true)` (`common/main/App.java:1772-1786`) | macro processing in a fresh window (`1:1` from creation) |
| `.ggt` open, tool library | `loadExistingFile(…, isMacroFile = true)` → host (`AppGeoCeDG.java:737-742`); `GFileHandler` skips its hook for macro files; `GeoCeDGUserToolLibrary` parses macros non-clearing (`GeoCeDGUserToolLibrary.java:689`) | macro processing |
| `.ggt` save, tool delete | `AppD.saveMacroFile` → `writeMacroFile` (`AppD.java:3547-3559`); `ToolManagerDialogModel.deleteTools` → `app.removeMacro` | no parse |
| portable-preferences reload (`--settingsFile`) | `GeoGebraPortablePreferences.loadXMLPreferences` → `setXML(prefs, true)` (`:266-295`): at startup (inactive), inside File → New (covered by `E1`), and in the v1-fallback *Restore default settings* (`desktop/gui/menubar/OptionsMenuD.java:287`) | preference processing, non-document `setXML` |
| installed-preferences reload | `GeoGebraPreferencesD` `setXML(…, false)` (`:545-582`) | non-clearing |
| Open URL dialog pre-clear and cancel | `GuiManagerD.openURL` clears before the dialog (`:1839-1843`) | temporary clearing; the later successful load fires `E2`–`E4` |
| `evalXML`, non-clearing `setXML`, merge | `GgbAPI.evalXML` → `setXML(…, false)` (`common/plugin/GgbAPI.java:169-179`) | merge |
| undo, redo | `UndoManagerD.loadUndoInfo` → `readZipFromMemory` → `doParseXML` → `Kernel.clearConstruction` | no App-level hook |
| redefine, rebuild, rollback, rejected-parse restore | `Construction.buildConstruction`/`processXML`; `MyXMLio.restoreRejectedSpatialParse`; clipboard and native-load rollbacks | no App-level hook |
| paste, Insert File, Apply Template | `InternalClipboard`; a separate hidden helper application (`desktop/main/AppD.java:4744-4816`) | merge into the current document |
| failed or cancelled loads, cancelled New | no hook fires; `AppD.clearConstruction` returns `false` | no transition |

- `drawingScale` is never reset from `clearConstruction`, `commitLoadedDocument`,
  the `setXML` override in general, `documentHiddenLayersParsed`, a
  `DocumentUnitSystem` listener, `Construction.clearConstruction` or
  `Kernel.clearConstruction`.
- Changing `drawingScale` stores no undo point, never calls `setUnsaved`, is
  never written to XML and never notifies the kernel beyond presentation
  refreshes of export dialogs.
- `E5` is the only new seam; it marks a route positively and changes no `A-2`
  commit, `ExportArea` or paste-notice behavior.
- Finding outside `C` (recorded, not fixed): the tool-replacement reload is
  committed by `A-2` as a document transition, recomputing the working layer
  (`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`).

#### C3. Engineering-scale UI (contract, with `DQ-C6` and `DQ-C8`)

Current scale controls:

| Dialog | Control | Evidence |
|---|---|---|
| Picture (PNG, PDF, SVG, EMF) | mode combo `ScaleInCentimeter` / `FixedSize` / `SizeInPixels`; row `a units = b cm`; row `100 screen px = c cm`; pixel row; hidden DPI combo for EMF | `desktop/export/PrintScalePanel.java:114-155`; `GraphicExportDialog.java:251-348` |
| Print Preview | the same `PrintScalePanel`, pixel mode disabled | `desktop/export/PrintPreviewD.java:440-443` |
| PSTricks, PGF/TikZ, Asymptote | `XUnits`, `YUnits`, `PictureWidth`, `PictureHeight` fields, coupled per axis | `desktop/export/pstricks/ExportFrame.java:130-170`, `:388-418` |

Parsing is `Double.parseDouble` with only NaN and infinity rejected
(`PrintScalePanel.java:285-290`, `:339-341`); LaTeX fields accept digits, one
dot and no sign (`desktop/export/pstricks/TextValue.java:61-77`); no dialog
bounds any value. One shared GeoCeDG scale model can drive the picture, print
and LaTeX dialogs, because each derives its physical quantity from one number,
centimetres per model unit; format-specific controls (DPI, transparency, EMF+,
text as shapes, LaTeX options) stay where they are.

Required semantics for a physical construction unit: the control shows and
accepts `a:b`; `1:1` is full size, `1:2` half size, `2:1` double size; the pair
is a positive normalized integer pair; no model unit is inferred;
`presentationUnit` is never a scale factor; LaTeX unit, width and height become
derived displays of `fb(c)·100·a/b`; the *Fixed size* mode, which writes the
scale from the zoom, is not offered as physical. The unspecified-unit behavior is
C17.

#### C4. Physical picture and print sizing (contract)

Every physical picture and print size at the base is `W·p` cm, with `p` the
zoom-derived `printingScale` (C1). The D0 formulas are reproduced exactly by
substituting one GeoCeDG quantity:

```text
pPhysical = fb(effC) · 100 · a / b          centimetres per model unit
PDF page  = W · pPhysical · 72 / 2.54  pt   = output(W) · 72 / 0.0254
PNG       = W · pPhysical · D / 2.54   px   = output(W) · D / 0.0254
SVG       = W · pPhysical              cm   = output(W)
print     = W · pPhysical              cm on the page
LaTeX     = xunit = yunit = pPhysical  cm per model unit
```

Contract: with a physical effective construction unit, every physical route
reads `pPhysical` through one host-identical seam; the view `printingScale` is
**bypassed** as an authority (it stays the Classic value and the device-mode
value of C17); there are never two physical scale authorities. DPI stays raster
resolution only. Zoom, the viewport and the view scale cancel (they fix only the
view-pixel grid of the export viewport). An equivalent `presentationUnit` change
leaves every size identical. Evaluation order of `pPhysical` is fixed and
tested so that the decimal output is deterministic.

Extreme values: `pPhysical` and every derived size are binary64; a non-finite
result, a raster dimension beyond the integer range or memory, an EMF `RectL`
value beyond `2^31 − 1` (0.01 mm, about 21.47 km), and a PDF page beyond the
limits `C` re-establishes for its writer fail explicitly with a message, never
with a silent clamp; an exact physical extent below one device unit fails
explicitly instead of becoming the B raster minimum of one pixel (`DQ-C6`).

#### C5. EMF physical frame (finding; `DQ-C9`)

`OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` is wider than a 0.01 mm quantization:

| Fact | Evidence |
|---|---|
| FreeHEP fixes the reference device at 1024 × 768 px = 320 × 240 mm (0.3125 mm per device unit, 81.28 DPI) and truncates `rclFrame = (int)(bounds·31.25)` | `freehep/emf/EMFGraphics2D.java:229-232`; `freehep/emf/EMFHeader.java:15`, `:63-68` |
| the dialog sizes EMF device bounds with the hidden DPI combo, so the frame is `W·p·D/81.28` cm instead of the labelled `W·p` cm | probe `P-C-EMF`: 4 × 3 units at `p = 1`: frame 35.31 × 26.56 mm at 72 DPI, 147.50 × 110.62 mm at 300 DPI, 295.31 × 221.56 mm at 600 DPI (label 40 × 30 mm) |
| with device units chosen on the 0.3125 mm reference, the frame matches the physical size only to the device grid plus truncation | probe: 4 mm × 3 mm (mm, 1:1) gives 4.06 × 3.12 mm; 40.03 × 30.071 mm gives 40.00 × 30.00 mm; 101.6 × 76.2 mm gives 101.56 × 76.25 mm |
| the format itself quantizes `rclFrame` to integer 0.01 mm; nearest rounding of the exact size bounds the error by 0.005 mm | `RectL` of 32-bit integers in 0.01 mm; probe `nearestFrameErrorMm` ≤ 0.003 mm on every case |
| FreeHEP writes `right = x + w` (not inclusive − 1) | `freehep/emf/EMFOutputStream.java:168-172` |

The physical contract of §13 is therefore not met by the base EMF route at any
DPI; it is met within the format's own 0.01 mm quantization only if the frame
is written from the exact physical size. Contract (`DQ-C9`, accepted): `C`
corrects the frame through an explicit GeoCeDG opt-in in the FreeHEP EMF
writer; the default FreeHEP and Classic behavior stays byte-identical; the
physical `rclFrame` derives from the requested output size, never from DPI;
each dimension is rounded to the nearest representable 0.01 mm, with the
unavoidable quantization of at most 0.005 mm per dimension documented; no wider
FreeHEP redesign; the physical metadata is tested apart from the device raster
and path coordinates. In the unspecified-unit device mode the same opt-in
writes the dialog's labelled device output size, so the frame never depends on
the hidden DPI either. A global FreeHEP semantic change is a stop.

`OBS-B-FREEHEP-PATH-COORDINATE-PRECISION` (finding): PDF and SVG path
coordinates use five significant digits (`freehep/pdf/PDFUtil.java:80-85`;
`freehep/svg/SVGGraphics2D.java:1346-1351`) in view-pixel units under exact
page, `viewBox` and transforms; a coordinate of magnitude 1000 px resolves to
0.1 px. It does not affect page, frame or `viewBox` sizing and is distinct from
the C4 contract: `DQ-C14`.

#### C6. DXF units (contract)

No code maps unit tokens to DXF codes at the base; `$INSUNITS` is the constant
0 (`DxfExporter.java:109-110`) and `D1` pins it for every unit state
(`dtest/PreG9BR6PlusD1ExportRegressionTest.java:80-82`). The Autodesk DXF header
reference defines `$INSUNITS` (group 70) as `0 = Unitless`, `4 = Millimeters`,
`5 = Centimeters`, `6 = Meters` (re-read for this preparation; ADR 0005 cites the
same reference). The repository's controlled reader is the test `DxfDocument`
with `headerValue` (`jre-test/export/GeometryExportFoundationTest.java:147-225`).

```text
mm  -> $INSUNITS 4          cm  -> $INSUNITS 5          m -> $INSUNITS 6
UNSPECIFIED_MODEL_UNIT -> 0, neutral model UNITLESS
usm -> $INSUNITS 0, explicit custom-unit warning, paired sidecar mandatory,
       sidecar records at least the token "usm" and the canonical metersPerUnit
       (unit-system §8.4), core DXF stays unitless
coordinates -> numerically unchanged in every case
```

Neutral model: `Unit` gains the physical tokens; the model carries the
effective construction unit and, for `usm`, its binary64 factor; `UNITLESS`
stays only for `UNSPECIFIED_MODEL_UNIT`; the coordinate-system string is
unchanged. Sidecar: `dxf.source_unit` and `dxf.target_unit` stop being the
constant `"unitless"`; the schema change, its version and the `usm` presentation
are `DQ-C11`. Staleness: the fingerprint adds the unit state (§15.3) and every
other export input whose change alters the DXF (the hidden-layer set under
`DQ-C2`). `$MEASUREMENT` and `$LUNITS` are not in the D0 contract and are not
written.

#### C7. Specification, verifier and test coherence (contract)

One candidate changes, together: the exporter; `geometry-export-foundation.md`
(units section, the pending-amendment note, the G5 PASS clause and the layer
and visibility lines); `dxf-curve-fidelity-and-approximation.md` (§2 unitless
statement, §7 dialog units and hidden-source rule, §8 sidecar units and
triggers); `unit-system.md` §15 status markers only; ADR 0005 (`DQ-C17`); the
verifier pins and the Java pins below; the guides. Map:

| Pin | Location | At the base | After `C` |
|---|---|---|---|
| live DXF spec hash | `tools/agent/verify-g9x1-extended-dxf.ps1:50`, `:730-731` (`f2a0cacc…`, matches the live file) | satisfied | re-pinned as a recorded successor (`DQ-C15`) |
| guide literal `Cartesian 2D world / UNITLESS` | `tools/agent/verify-g9x1-extended-dxf.ps1:772-778`; `docs/user/geocedg_user_guide_en.md:1304`; `docs/user/geocedg_user_guide_es.md:1353` | satisfied | guide and pin changed together (`DQ-C15`) |
| frozen G9X1 evidence `fidelityContract.units == "UNITLESS"` | `geocedg/validation/export/g9x1/g9x1-evidence.json:78`; verifier `:449` | historical truth of G9X1 | unchanged: frozen evidence is not edited |
| G5 corpus `insunits: 0` | `models/regression/g5-dxf-foundation/expected-entities.yml:7`; `tools/agent/verify-dxf.ps1:162-169` | unspecified document | unchanged and still true (the corpus has no unit) |
| `$INSUNITS = 0` | `jre-test/export/GeometryExportFoundationTest.java:52`; `jre-test/export/G9X1G5CorpusCompatibilityTest.java:65-66` | unspecified documents | kept for unspecified documents; physical cases added |
| unit independence of every export | `dtest/PreG9BR6PlusD1ExportRegressionTest.java:70-96` | pins the pre-`C` state on purpose | replaced by the `C` physical contract with coordinates still unchanged |
| sidecar units | `dtest/export/G9X1DxfManifestTest.java:74-77`; `dtest/export/G9X1DesktopPreflightContractTest.java:183-184` | `"unitless"` | per `DQ-C11` |
| interim LaTeX/DXF digests | `dtest/PreG9BR6PlusBExportSurfaceTest.java:651-672` | base output whatever the area | replaced by agreement tests |
| interim hidden-layer LaTeX/DXF | `dtest/PreG9BR6PlusA1HiddenLayerTest.java:307-336` | unchanged by a hidden layer | replaced by `C`'s rule |
| A-2 reopen facts | `dtest/PreG9BR6PlusA2HiddenLayerPersistenceTest.java:622-654` | LaTeX and DXF included in the 20 facts | expectations follow `C` |

Neither PowerShell DXF verifier is a registry leaf (`verify.ps1` profiles reach
DXF only through the whole-module JUnit selections; the scripts are historical
phase verifiers in `tools/agent/verification-runtime.psm1:35`, `:49`). No
intermediate candidate may exist in which the product writes physical units while
a specification says `UNITLESS`, a specification says physical units while a pin
requires `UNITLESS`, or the sidecar and the header disagree.

#### C8. DXF visibility (finding; `DQ-C2`, `DQ-C3`)

Four facts are distinct and stay distinct:

| Fact | Owner | DXF at the base |
|---|---|---|
| object visibility (`isEuclidianVisible`) | document object state | included, `60 = 1`, reported |
| GeoCeDG hidden layer | `DOCUMENT_PRESENTATION` (`A-2`) | ignored |
| DXF layer state | DXF `LAYER` record (negative color = off) | always on |
| source layer identity | the integer layer | `0` / `GEOCEDG_L<n>` |

The normative rule "hidden sources remain included but visibly reported"
(`dxf-curve-fidelity-and-approximation.md` §7) and the G5 mapping "hidden
object → `60 = 1`" are accepted contracts. The action's selection contract
reference is `geocedg.dxf-exportable-visible-geometry`
(`apps/geocedg/application-profile.yml:2234`), which no profile entry defines
and which the behavior contradicts (`OBS-R6P-DXF-VISIBLE-CONTRACT`). Picture and
LaTeX follow `effectiveVisible` (contract).

#### C9. `ExportArea` in LaTeX and DXF (contract, with `DQ-C13`)

LaTeX: the dialect classes read their bounds once from the selection rectangle
or the active view, and bound edits write the selection rectangle (C1). After
`C`, in GeoCeDG: bounds initialize from `ExportArea.resolve(activeView)` (its
`sourceViewId` and producer precedence unchanged); a bound edit that forms a
valid rectangle writes `ExportArea` through the `B` `MANUAL` route
(`ExportAreaSession.defineManual`), never the selection rectangle; the selection
rectangle is never read or written as authority; clipping uses the dialects'
format-level clip at the area's world bounds (`\clip`, `clip(...)`,
`pspicture*`). Classic keeps the host behavior through seam defaults.

Three notions stay separate in every exporter: the **semantic curve domain**
(the certified finite component intervals of the kernel, never replaced or
narrowed by the area; a missing required domain stays `MISSING_DOMAIN` whatever
the area), the **approximation domain and work** (the component domain, the
tolerance and the budgets; the area never bounds, culls or shortens the
approximation) and the **export-area output rule** (LaTeX: format-level clip;
DXF: `DQ-C13`).

##### C9-DXF. DXF and `ExportArea` (finding; `DQ-C13`)

The author rejected the prepared default "DXF ignores `ExportArea`": the
accepted `B`/`C` contract makes the picture exporters, the LaTeX exporters and
DXF converge on the one `B` `ExportArea`. Facts at the base that every
alternative must respect:

- DXF reads no area (C1). G5 states that view limits are never read, that there
  is no viewport-clipped mode and that infinite objects keep `RAY`/`XLINE`
  (`geometry-export-foundation.md`, *Infinite geometry*; ADR 0005 Decision 5);
  its PASS clause requires semantic equality across zoom changes; exact
  entities admit no tolerance beyond floating-point text round trip.
- `ExportArea.resolve` always returns an area: `MANUAL`,
  `EXPORT_POINTS_EXPLICIT`, `EXPORT_POINTS_AUTOMATIC`, or the fallback
  `VISIBLE_VIEWPORT` (`geocedg-desktop/export/ExportArea.java:19-28`;
  `ExportAreaSession.java:65-79`). The fallback is the zoom-dependent visible
  view. A DXF that consumed it would change with the zoom and break the G5 PASS
  clause. Every alternative below therefore treats `VISIBLE_VIEWPORT` as "no
  explicit area" for DXF (model space as at the base), states it in the report,
  and consumes the three explicit, view-independent producers (world bounds or
  document points). No G5 or G9X1 corpus document defines `Export_1`/`Export_2`
  (search of `models/regression` and the export tests), so both corpora keep
  their bytes when no explicit area exists.
- The area belongs to a source view (`sourceViewId`); DXF is view-independent.
  The consumed area is the one resolved for the active 2D Graphics view, the
  same rule as LaTeX, recorded in the report and the sidecar.
- Locus V2 components are known to DXF only through their certified
  approximation in model coordinates; their parameter at an arbitrary point is
  not certified (C10).

Alternatives, with `R` the closed explicit area:

- **A — geometric clipping**: emit the part of every source inside `R`.
- **B — participation filtering**: `R` decides which sources (and which
  certified components of a semantic source) participate; participating
  entities are emitted whole, unchanged and exact; a non-participating source
  or component is reported as outside the area, never silently dropped.
  Predicate variants: **B1** the geometry (curve, not the enclosed region)
  meets `R`; **B2** its bounding box overlaps `R`; **B3** it lies wholly
  inside `R`.
- **C — typed hybrid**: clipping for some entity classes and participation for
  the others, each class justified by its own exactness and identity
  consequences.

Per entity family (A and the clipped classes of C share the A column):

| Source family (G5/G9X1 entity) | A — geometric clipping | B — participation (B1) | Admissible C class? |
|---|---|---|---|
| point (`POINT`) | kept or removed; equal to B | kept or removed by closed inclusion | no difference |
| segment (`LINE`) | `LINE` with computed endpoints (one division per edge; an endpoint such as y = 1/3 is rounded, beyond text round trip) | whole `LINE` | only with a new "exact carrier, computed endpoint" class |
| ray (`RAY`) | **family change** `RAY` → `LINE` (finite) | whole `RAY` | as `LINE`, plus a family change |
| line (`XLINE`) | **family change** `XLINE` → `LINE` (chord of `R`) | whole `XLINE` | as `RAY` |
| circle (`CIRCLE`) | **family change** to 0–4 `ARC` entities; angles from circle–edge intersections (square roots) | whole `CIRCLE`; a circle that encloses `R` without meeting it does not participate under B1, does under B2 | only with a computed-angle class |
| circular arc (`ARC`) | 0–n `ARC` pieces, computed angles | whole `ARC` | as `CIRCLE` |
| ellipse, elliptic arc (`ELLIPSE`) | 0–n elliptic arcs; parameters from quadratic intersections | whole `ELLIPSE` | as `CIRCLE` |
| polygon (closed `LWPOLYLINE`) | boundary clipping gives **open** polylines (closure lost); region clipping would invent edges on `R` that are not source geometry (rejected) | whole closed `LWPOLYLINE` | boundary pieces only, with a closure-loss rule |
| polyline (`LWPOLYLINE`) | open pieces with computed vertices | whole `LWPOLYLINE` | as segment |
| Locus V2 / Spline V2 certified component (approximate `LWPOLYLINE`) | pieces of the approximation; new vertices on chords (still within the estimate) but **the piece parameters come from sampled coordinates**, so piece identity would be derived from samples (forbidden, C10) unless a certified root isolation in parameter space existed (kernel capability, absent) | whole certified component; participation decided on its certified approximation enlarged by the achieved estimate; others recorded as outside the area | **not admissible**: clipping cannot keep component identity truthfully |

Impact per alternative:

| Concern | A | B | C |
|---|---|---|---|
| exact G5 entity types | changed (`RAY`, `XLINE` → `LINE`; `CIRCLE` → `ARC`; closed → open) | unchanged | changed for clipped classes |
| exactness claims | clipped endpoints and angles are computed values: `EXACT` as defined by G5 and the fidelity spec §3 no longer holds without a new class | unchanged | a new class for each clipped family |
| new geometric computation in the exporter | intersection solving (`AGENTS.md` §13: no new geometric solving logic in the exporter) | closed-form predicates only, no emitted computed geometry | solving for clipped classes |
| deterministic handles | deterministic for a fixed `R`; entity count depends on `R` | deterministic subset in construction order | as A for clipped classes |
| sidecar mappings | one source → several entities; sub-addresses for curve pieces | one entity per participating source or component, plus area and outside-area records | mixed |
| G5 corpus | unchanged without an explicit area | unchanged without an explicit area; a subset with one | as A |
| G9X1 fidelity | new piece identity, uncertified piece parameters | unchanged | semantic curves must stay B |
| source and component identity | split | preserved | split for clipped classes |
| complete-construction export | clipped | participation of each source | mixed |
| current-selection export | clipped | selection ∩ area, outside-area sources reported | mixed |
| meaning of `ExportArea` for DXF | output extent, as for pictures | spatial selection of whole sources | two meanings in one file |
| normative impact | amends ADR 0005 Decisions 5–6, the G5 exactness and infinite-geometry rules and the fidelity vocabulary | amends the G5 population statement and the fidelity spec §7 and §8 (area and outside-area records) | the union of both |

Concrete examples, `R = [0, 10] × [0, 10]`:

| Source | A | B1 |
|---|---|---|
| point (12, 5) / (5, 5) / (10, 5) | removed / kept / kept (closed) | the same |
| segment (−1, 0)–(2, 1) | `LINE` (0, 1/3)–(2, 1), endpoint rounded | whole `LINE` (−1, 0)–(2, 1) |
| ray from (5, 5) along +x | `LINE` (5, 5)–(10, 5) | whole `RAY` |
| line y = 5 / y = 15 | `LINE` (0, 5)–(10, 5) / removed | whole `XLINE` / not participating |
| circle centre (10, 10), r = 3 | `ARC` 180°–270° | whole `CIRCLE` |
| circle centre (5, 5), r = 20 (encloses `R`) | removed (no curve inside) | not participating (B2 would include it) |
| ellipse centre (0, 0), a = 4, b = 2 | elliptic arc, parameter 0–π/2 | whole `ELLIPSE` |
| square (−2, −2)…(2, 2) | open `LWPOLYLINE` (0, 2)–(2, 2)–(2, 0) | whole closed square |
| `LocusV2` y = x² on one component [−2, 2] | the piece x ∈ [0, 2], cut at a vertex interpolated on a chord at x = 0 (piece parameter uncertified) | the whole component x ∈ [−2, 2] |
| `LocusV2` y = x² on {[−2, −0.5], [0.5, 2]} | the second component kept, the first removed | the same; the first reported outside the area |

Every alternative records the consumed area (bounds, producer, view) in the
report and, when written, in the sidecar, and adds the explicit area to the
staleness fingerprint, because the DXF depends on it.

**Contract (`DQ-C13`, author choice `B1`).** DXF consumes `B`'s authoritative
`ExportArea` through source and component participation, never rectangular
clipping and never a family change:

- Only `MANUAL`, `EXPORT_POINTS_EXPLICIT` and `EXPORT_POINTS_AUTOMATIC` define a
  finite participation area `R` (closed). `VISIBLE_VIEWPORT` is explicitly no
  DXF boundary: the export is the base model-space population, zoom-invariant.
- A source, entity or component participates when its exported geometric
  support meets `R`, and is then emitted whole. Complete-construction and
  current-selection requests alike: the selection chooses the candidates, the
  area applies the same rule.
- Exact analytic predicates per neutral entity: point (closed inclusion);
  segment and each polyline or polygon edge (parametric slab test); `RAY` and
  `XLINE` (the same on a half-infinite or infinite parameter); circle (distance
  from the centre to `R` at most the radius, and the radius at most the largest
  distance to a corner); arc and ellipse or elliptic arc (an endpoint inside `R`
  or a curve–edge intersection inside the angular or parameter range, by the
  closed-form quadratic per edge); boundaries, never filled regions.
- Locus V2 and Spline V2 certified components: excluded only when a
  conservative certified test proves the component disjoint from `R`. The test
  uses the kernel's existing ADR 0028 certified interval curve model (structural
  Spline V2 and the certified construction class) through one new read-only
  query: outward enclosures over a deterministic parameter bisection, with a
  bounded budget, prove disjointness only if every sub-box enclosure misses `R`.
  A component without a certified model, or whose proof does not close within
  the budget, participates whole. Raw samples, the approximation polyline and
  render tessellation never decide exclusion.
- `OUTSIDE_EXPORT_AREA` is a population outcome, never `UNSUPPORTED` or
  `INVALID` and never a fidelity reduction; it is counted in preflight and the
  report, written to the sidecar when one exists, stated by a deterministic DXF
  header comment, and the explicit area enters the staleness fingerprint. Area
  filtering alone never makes the sidecar mandatory.

#### C10. Semantic curve export adapter (contract)

No format-independent semantic snapshot exists; the Locus V2 logic (component
enumeration, orientation, closure, coverage, revision guard, evaluator, domain
validation) is private to `G9X1GeometryExportAdapter`
(`:288-435`, `:478-513`, `:628-737`) and writes DXF-shaped entities. The reusable
public seams are `AdaptiveCurveApproximationBuilder2D` (model coordinates, no
construction, renderer or viewport state, `:18-24`; DXF-free imports) and the
kernel snapshot API (`LocusDefinition2D`, `LocusBranch2D`,
`LocusExistenceStructure2D`).

Contract: one read-only adapter in `org.geocedg.common.export` is extracted from
the G9X1 private glue; G9X1 maps it to DXF and its DXF bytes stay identical on
the G9X1 and G5 corpora; LaTeX maps it to paths. For every `GeoLocusV2`
(`SplineV2` included) it preserves: branch key; component address; certified
valid interval and orientation; gaps (one approximation per component, never
bridged); revision and currentness (same definition object and semantic
revision, stale → explicit failure); constructive multiplicity (no coordinate
deduplication); local validity and coverage
(`COMPLETE` / `LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED`); closure only from a
full-period certificate; approximation evidence. It calls
`LocusDefinition2D.evaluate`, never `evaluateForRender`; creates no `GeoElement`,
`AlgoElement`, dependency, undo point or label; reads no view, viewport, zoom,
DPI or render cache. Identity is never derived from sampled coordinates.

The adapter returns one outcome per component (and one per source that yields
no component), reusing `SourceExportOutcome` (`Fidelity`, `Reason`,
`ComponentAddress`, `SemanticCoverage`, `ApproximationEvidence`;
`geocedg-common/export/SourceExportOutcome.java:14-52`). It decides no
writability: each format applies its own admissibility policy to those
outcomes. DXF keeps the accepted G9X1 rule unchanged (any `UNSUPPORTED` or
`INVALID` component makes the export not writable;
`GeometryExportPreflight.java:97-99`; fidelity spec §7, ADR 0014 Decision 6);
LaTeX applies C14.

#### C11. Semantic curve tolerance in LaTeX (finding; `DQ-C4`)

The DXF tolerance is a model-coordinate chord tolerance (dialog default
`0.001`, positive finite; `GeoCeDGDxfExportController.java:56`, `:405-423`;
`GeometryExportRequest.java:142`; `AdaptiveCurveApproximationBuilder2D.java:323-341`,
`:417-424`) with
deterministic budgets of 65 536 evaluations, depth 20 (≤ 52), 16 384 vertices
per component and 131 072 in total (`GeometryExportRequest.java:24-34`). The
builder only claims `ESTIMATED_ERROR` (`:211-216`). Operational reuse is
possible: the LaTeX adapter can build a `GeometryExportRequest` with partial
output disabled. The open choices are its owner, default, input and whether it
stays in model coordinates.

#### C12. Per-format semantic output (finding; `DQ-C12`)

Existing dialect idioms for polylines: PSTricks `\psline[...](x,y)(x,y)…`
(`GeoGebraToPstricks.java:2265-2283`) and `\pscustom{\moveto…\lineto…}` for
legacy loci (`:183-227`); PGF `\draw [...] (x,y)-- (x,y)-- …;`
(`GeoGebraToPgf.java:2881-2899`); Asymptote `draw((x,y)--(x,y)--…, pen);`
(`GeoGebraToAsymptote.java:322-354`, `:1983-1994`). There is no
`plot coordinates` or `\pscurve` anywhere. Comments exist in each dialect (`%`,
`/* … */`); the PGF and PSTricks parametric warning lacks a newline and comments
out the next command (`GeoGebraToPgf.java:1531-1532`;
`GeoGebraToPstricks.java:1048-1049`), a pre-existing upstream defect outside
`C`. Safest first representation: one open or closed coordinate path per
certified component in each dialect's existing polyline idiom, with an
approximation disclosure; no smoothed curve primitive without a fidelity proof.

#### C13. `AQ-C1` exact Bézier for Spline V2 (finding; `DQ-C1`)

`SplineV2` degree is 3 to 12 (`geocedg-common/kernel/spline/SplinePolynomialModel2D.java:69-76`,
`:157-160`), so "degree ≤ 3" is exactly the cubic case. The model is an
interpolant solved numerically; knots are binary64 chord-length values;
"rounded power spans are derived evaluation data, not exact coefficient
authority" (`:21-22`); exact truncated-power numerators exist for structural
snapshots but "interpolation solving and rounded spans remain numerical"
(`SplineStructuralModel2D.java:19-24`; `SplinePolynomialModel2D.java:239-256`).
Evaluation and DXF use the rounded spans (`SplineSemanticEvaluator2D`), and DXF
writes an approximate `LWPOLYLINE` (no `SPLINE` anywhere). An exact cubic Bézier
would be exact only relative to a numerically solved snapshot, would need
rational arithmetic, would cover one degree only and would split one Locus V2
approximation contract into two. The `P0` recommendation stands.

#### C14. Invalid, open and incomplete semantic curves (contract; author direction of `DQ-C5`)

Precedent at the base (DXF, G9X1): a Locus V2 with no exportable component
fails `MISSING_DOMAIN`; a non-closed domain fails `MISSING_DOMAIN`; an invalid
evaluation inside a component fails `DISCONTINUITY_UNRESOLVED`; budgets fail
`WORK_LIMIT`; a changed source fails `STALE_SOURCE_REVISION`; any `UNSUPPORTED`
or `INVALID` outcome makes the export not writable (strict complete request;
`GeometryExportPreflight.java:97-99`); locally certified components without
global completeness are written as approximate with
`LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED` coverage, an incomplete-coverage
warning and a mandatory sidecar (`G9X1GeometryExportAdapter.java:349-386`;
`dtest/PreG9BS1AuthorDxfReproductionTest.java:158-162`, `:223-245`). Local
admissibility is not global completeness; a valid certified component is not
dropped because global completeness is not established, and output is never
called complete when it is partial.

Author direction (2026-10-04), which replaces the prepared default: a locally
certified, valid and unambiguous semantic branch component **may** be emitted
in LaTeX even when the source or the export is incomplete or its global
completeness is `NOT_ESTABLISHED`; this never implies global completeness. G9X1's
strict writability rule (partial output disabled) is **not** applied to LaTeX
mechanically, because it would erase the accepted distinction between local
admissibility and global completeness. Requirements: branch and component
identity preserved; explicit gaps and discontinuities preserved; only certified
valid components emitted; no interpolation across an invalid or unresolved
interval; no failure evidence omitted silently; approximation and completeness
comments in the generated LaTeX; incomplete coverage stated visibly in the
dialog report; stale-source, missing-domain, work-limit and tolerance failures
classified explicitly.

Admissible component: it comes from `getContinuousValidComponents()` of a
`VALID`, deterministic definition (`UNSUPPORTED_NONDETERMINISM` rejected,
`G9X1GeometryExportAdapter.java:300-318`), carries a certificate evidence key,
and its approximation succeeded within the request; it is written as its own
path, never joined to another component (the builder works per component,
`G9X1GeometryExportAdapter.java:332-378`, so no interpolation crosses a gap or
an invalid interval).

Minimum result and report model (export layer only; no kernel state). It reuses
the per-component outcomes of C10 and adds one derived classification:

| Level | Classification | Rule |
|---|---|---|
| source | `COMPLETE` | at least one component emitted, every component emitted, coverage `COMPLETE` |
| source | `INCOMPLETE_WITH_CERTIFIED_COMPONENTS` | at least one certified component emitted, and coverage `LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED` or at least one failed component |
| source | `NO_ADMISSIBLE_OUTPUT` | no component emitted (`MISSING_DOMAIN`, `UNDEFINED_SOURCE`, a non-`VALID` or nondeterministic definition, every component failed, `UNSUPPORTED_FAMILY`) |
| export | `COMPLETE` | every semantic source `COMPLETE`; no failure evidence |
| export | `INCOMPLETE_WITH_CERTIFIED_COMPONENTS` | code is generated; every emitted semantic component is certified; at least one semantic source is not `COMPLETE`; all failure and completeness evidence is in the code comments and the report |
| export | `REJECTED / NO_ADMISSIBLE_OUTPUT` | no code is generated: an export-integrity failure (`STALE_SOURCE_REVISION`, the request's tolerance or guarantee not establishable, invalid budgets), or nothing admissible to emit at all |

| Failure | Classification | Export effect |
|---|---|---|
| `MISSING_DOMAIN`, `INVALID_DOMAIN` (no certified or no closed finite domain) | the source or the component | source `NO_ADMISSIBLE_OUTPUT` or a failed component; export incomplete |
| `DISCONTINUITY_UNRESOLVED`, `NON_FINITE` inside a component | the component | failed component, never bridged; export incomplete |
| `DEGENERATE_SOURCE` (zero-width component) | the component | failed component; export incomplete |
| `WORK_LIMIT` (budgets exhausted, deterministic order) | the component | failed component; export incomplete |
| `TOLERANCE_NOT_ESTABLISHED` (the requested guarantee cannot be met) or an invalid tolerance input | the request | export `REJECTED` |
| `STALE_SOURCE_REVISION` (a source changed during generation) | the export | export `REJECTED` |
| gap between components | not a failure | separate paths |
| locally valid component, global completeness not established | coverage | emitted with disclosure; export incomplete |

A source with `NO_ADMISSIBLE_OUTPUT` beside other admissible content makes the
export `INCOMPLETE_WITH_CERTIFIED_COMPONENTS` (nothing of it is emitted; its
evidence is); an export whose only eligible content yields nothing admissible is
`REJECTED / NO_ADMISSIBLE_OUTPUT`. The dialog report shows the export
classification and per-source counts; the generated code carries one header
comment with the export classification and one comment per semantic source
(`DQ-C12` fixes only their exact form). DXF is unchanged by this direction: its
accepted strict rule and its sidecar stay as they are.

#### C15. `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT` (finding; `DQ-C10`)

- The Picture dialog has no preview at the base or after `B`
  (`GraphicExportDialog.java:226-396`); the LaTeX dialogs have none either.
- `B`'s *Save preview* is the right-hand accessory of the reused Save chooser in
  `MODE_GEOGEBRA_SAVE`, rendering the pending export (area and hidden layers)
  (`desktop/gui/util/GeoGebraFileChooser.java:116-157`, `:512-528`;
  `AppGeoCeDG.java:543-552`).
- It renders only when the selected or named target file **exists**
  (`GeoGebraFileChooser.java:147-149`; upstream `:403-418`), and the accessory
  is removed while the chooser is narrower than 600 px (`:73`, `:258-279`).
  Both conditions are upstream code predating `B` (baseline import). The Picture
  dialog's Save passes no file, so the chooser pre-selects
  `<document name>.<ext>` or nothing (`desktop/gui/GuiManagerD.java:1662-1728`).
- Probe `P-C-SAVE-PREVIEW` on the real chooser: no image for a new target; an
  image for an existing target; no accessory at 500 px; accessory again at
  800 px. A macOS sandbox build uses `NSSavePanel`, which has no preview
  (`GuiManagerD.java:1688-1700`); Windows uses the Swing chooser.
- Most likely route of the author's observation (inferred): File → Export →
  Graphics View as Picture… → Save, with a new file name (or a narrow chooser),
  which shows an empty preview area by design of the accepted `B` contract.
- Disposition (`DQ-C10`, accepted): `NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`;
  no new preview surface in `C`; the guides explain the conditions.

#### C16. Non-native document replacement and `drawingScale` (contract)

`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` concerns the `B`
`ExportArea` and the `D1` paste notice, which reset only in
`clearConstruction`, `loadExistingFile` and `loadXML(String)`
(`AppGeoCeDG.java:717-762`) and therefore not on Base64, non-native URL or file,
`.html`, API `openFile` or clearing `setXML` replacements. `C` resets its own
new `drawingScale` only at the successful-transition events `E1`–`E6` of C2,
which cover those transports through the `D1`/`A-2` hooks plus one narrow API
marker, and never at `clearConstruction` or `commitLoadedDocument`. `C` does not
move, add or remove any `ExportArea` or paste-notice reset, so the observation
stays open, unchanged and separately governed. The evidence suggests a later
shared session-reset hook (one call at the same successful-transition events
for every session export state); that is the author's decision for the
observation, not `C`'s. `C` can implement its own lifecycle cleanly without
solving the wider observation; if the implementation finds otherwise, it stops.

#### C17. Unspecified-unit mode (contract, with `DQ-C8`)

With `UNSPECIFIED_MODEL_UNIT`: no `a:b` control, value or label; no
"1 model unit = 1 cm"; DXF `$INSUNITS = 0`, neutral model `UNITLESS`, the dialog
and the sidecar say unitless; picture and print keep the host device-scale
controls, relabelled as a non-physical device scale and accompanied by a short
statement that the document has no construction unit; LaTeX keeps its unit,
width and height fields as explicit non-physical device parameters. The exact
labels, which controls stay visible and enabled, and whether physical
documents keep a device-pixel mode are `DQ-C8`. `drawingScale` keeps its session
value while the unit is unspecified and applies again as soon as a physical
unit is declared (unit-system §12: it has physical meaning only then).

#### C18. Classic and Web boundary (contract)

- Classic: every shared seam has an upstream-identical default (`App`/`AppD`
  default returns the host value or `null`); Classic picture, print and LaTeX
  output and dialogs are unchanged; the evidence is a Classic-configuration
  corpus compared byte for byte with a `git archive` of the base.
- Web: the GWT module compiles `org/geogebra/common` only
  (`source/web/web-common/src/main/resources/org/geogebra/Common.gwt.xml:10-12`);
  no `.gwt.xml` includes `org/geocedg`, and 37 shared upstream classes already
  import `org.geocedg` (finding: Web build compatibility is not established by
  any registered check at the base). `C` must not worsen it: no `org.geocedg`
  import and no `java.io`, AWT, `java.security` or reflection in the upstream
  `common/export/pstricks` classes; new `App` seams use primitive, `String` or
  existing common types; Web product behavior is out of scope.
- If a correct `C` requires materially altering Classic export semantics,
  stop.

#### Contradictions with earlier documents

| Source | Statement | At the base | Effect |
|---|---|---|---|
| C design candidate §4 | paper size follows `L·k(c→mm)/n` with `drawingScale = 1:n`/`n:1` | superseded by unit-system §12–§13 (`a:b`, `output(L) = L·fb(c)·a/b`) | the specification governs |
| C design candidate §3 | `\draw plot coordinates {…}` for PGF | no such idiom in the exporter | C12: existing polyline idioms |
| C design candidate §2 | LaTeX elements filtered by `isEuclidianVisible()` only | true | C9: `effectiveVisible` |
| mini-track plan §4.4 | `physical model-to-output unit = presentationUnit` | superseded by D0 (`unit-system.md` §13) | the specification governs |
| B report §10, `OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION` | frame quantization of the integer device bounds | the frame also scales with the hidden DPI (C5) | `DQ-C9` covers both |
| `dxf-curve-fidelity-and-approximation.md` §7 | the dialog must show unitless coordinates | true until `C` | amended in the `C` candidate |
| ADR 0005 Decision 3 | source and DXF units unitless, `$INSUNITS = 0` | true until `C`; ADR 0032 does not amend it | `DQ-C17` |
| user guides, developer guide, profile notes | LaTeX and DXF ignore the export area and hidden layers "until `C`" | true | updated by `C` |
| this prompt as prepared in `5ad96304` (C2, `DQ-C7`) | reset `drawingScale` in the `clearConstruction` override and in `commitLoadedDocument`, including clearing `setXML` | those points also fire for the Open URL pre-clear and for tool-replacement reloads (probe `P-C7-TOOL-REPLACE`) | superseded by the author direction: events `E1`–`E6` only |
| this prompt as prepared in `5ad96304` (C14, `DQ-C5`) | the DXF strict precedent blocks Generate on any failed component | erases local admissibility versus global completeness for LaTeX | superseded by the author direction and the C14 result model |
| this prompt as prepared in `5ad96304` (`DQ-C13`) | DXF ignores `ExportArea` | contradicts the accepted convergence of every `C` exporter on one `ExportArea` | rejected by the author; `B1` participation chosen (C9-DXF) |

A contradiction that would change scope, owner, serialization or class stops
the phase.

### Author dispositions (2026-10-04)

The author decided every question on 2026-10-04. The full text of each
disposition is in the
[C preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_c_prompt_closeout_record.md),
which prevails over this summary; the earlier alternatives and evidence stay in
the [C author-decision record](../../../docs/validation/pre_g9b_r6_plus_c_author_decisions_record.md)
§3.

| ID | Outcome | Contract for the implementation |
|---|---|---|
| `DQ-C1` | accepted | no exact Bézier special case, including degree ≤ 3; `SplineV2` follows the one certified approximation contract; no output claims exactness because of its degree; rounded spans never become coefficient authority |
| `DQ-C2` | accepted | objects on persistently hidden GeoCeDG layers stay in DXF on their source layer; that DXF `LAYER` is emitted OFF (negative color `62`); the hidden set enters the layer table, the report and evidence, and the staleness fingerprint; an object both on a hidden layer and individually hidden carries both mechanisms |
| `DQ-C3` | accepted | individually hidden objects keep `60 = 1`; object visibility is never read as hidden-layer state; the stale "visible geometry" wording (profile selection-contract reference and specifications) is reconciled to the exported population; Show/Hide semantics unchanged |
| `DQ-C4` | accepted | LaTeX semantic tolerance in model coordinates, default `0.001`, an export-request/session input of each LaTeX dialog, validated positive finite, independent of `constructionUnit`, `presentationUnit` and `drawingScale`, never converted to a paper tolerance; approximation machinery and deterministic budgets of the shared adapter and G9X1 |
| `DQ-C5` | accepted as reconciled | C14: certified valid deterministic unambiguous components may be emitted without established global completeness; `COMPLETE`, `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, `NO_ADMISSIBLE_OUTPUT`, whole-export `REJECTED`; DXF keeps the strict G9X1 contract |
| `DQ-C6` | accepted | `a:b` positive integers in a bounded `int` implementation, gcd normal form, default `1:1`, explicit rejection of invalid or overflowing input, no silent clamp; equivalent pairs identical; presets `1:1`, `1:2`, `1:5`, `1:10`, `2:1`, `5:1` (free entry stays possible); an unrepresentable output fails that export explicitly |
| `DQ-C7` | accepted as reconciled | C2: `AppGeoCeDG` field per window; reset only at `E1`–`E6`; `E5` through the narrowest `GgbAPID` marker; `E6`: a reset that reloads the current file relies on the load commit (no second semantic reset), a successful reset to a blank document resets at the completed reset boundary; no reset because `clearConstruction` ran; never for undo, redo, redefine, rollback, preference reload, macro or tool processing, `.ggt`, tool replacement, `evalXML`, merge, paste, Insert File, Apply Template or temporary clears; `ExportArea` and paste-notice resets unchanged |
| `DQ-C8` | accepted with precision | physical unit: `drawingScale = a:b` is the sole engineering sizing authority; physical size from `constructionUnit + drawingScale` only; DPI is resolution; pixel dimensions are derived, never a second authority; no fixed-physical-size control; `presentationUnit` never affects size. Unspecified: no `a:b` claim, no inferred cm, device-scale controls kept where needed and labelled non-physical |
| `DQ-C9` | accepted | the EMF frame defect is corrected: explicit GeoCeDG opt-in in the FreeHEP EMF writer; default FreeHEP and Classic unchanged; no wider redesign; `rclFrame` from the requested physical output size, never from DPI; nearest representable 0.01 mm, ≤ 0.005 mm per dimension; physical metadata tested apart from device raster and path coordinates; stays `INTEGRATED_PHASE`; stop if a global FreeHEP semantic change would be needed |
| `DQ-C10` | accepted | `OBS-R6PLUS-EXPORT-PREVIEW-ABSENT = NOT_A_DEFECT / UI_EXPECTATION_CLARIFIED`; no new preview surface; the hidden-layer preview-content observation stays distinct |
| `DQ-C11` | accepted | explicit, deterministic, versioned sidecar schema evolution carrying unit metadata; old sidecars never reinterpreted; built-in units: native `$INSUNITS` and coherent sidecar metadata; `usm`: `$INSUNITS = 0`, visible custom-unit warning, mandatory paired sidecar with the token `usm`, the canonical binary64 `metersPerUnit` and versioned unit metadata; unspecified: `$INSUNITS = 0`, `UNITLESS`, no invented factor; coordinates unchanged |
| `DQ-C12` | accepted | deterministic comments with the export classification, requested tolerance, approximation status, per-source and per-component classification and evidence, and incomplete coverage, addressed semantically; a visible non-modal dialog report (status, approximate components, omitted or failed components with reasons, tolerance and work limits); comments never parsed back |
| `DQ-C13` | **B1 chosen** | C9-DXF: explicit producers only, `VISIBLE_VIEWPORT` is no DXF boundary; participation when the exported support meets the closed area, then emitted whole; complete construction and current selection alike; exact analytic predicates per family, boundaries not regions; Locus V2/Spline V2 excluded only by a conservative certified disjointness proof, otherwise participating; `OUTSIDE_EXPORT_AREA` is population filtering, never a fidelity failure, recorded in preflight, report, metadata or comment, sidecar when present, and fingerprint; no sidecar made mandatory by area filtering alone; B2, B3, A and C rejected |
| `DQ-C14` | accepted | the five-significant-digit path formatting is a documented writer limitation; no FreeHEP change for it; tests prove page and frame sizing are not affected |
| `DQ-C15` | accepted | only the live pins that `C` supersedes are updated; frozen G9X1 evidence untouched; historical verifier pins of living specifications repinned with the supersession recorded; the unitless G5/G9X1 corpus stays a byte and semantic baseline |
| `DQ-C16` | accepted with precision | routes without an explicit device/render scale use the unit contract and `drawingScale`; explicit device/render parameters stay device/render parameters, never mutate `drawingScale`, `constructionUnit` or physical meaning, and never become a second physical scale; stop before changing an API whose backward compatibility and this distinction cannot coexist |
| `DQ-C17` | accepted | a dated, versioned amendment section in ADR 0005 records that Decision 3 governed the G5 experimental contract and is superseded for `C`-era GeoCeDG export by the `D0`/`C` unit contract; the original text stays; ADR 0014 unchanged unless a genuine contradiction appears |

`OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` = `POST-A2 LIFECYCLE DEFECT /
OBSERVATION — NOT C BLOCKING — IMPLEMENTATION NOT AUTHORIZED — MUST RECEIVE
DISPOSITION BEFORE GLOBAL PRE-G9B-R6-plus CLOSEOUT`; preferred characterization
point before or during `E1`. It is not fixed in `C`.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
PUBLISHED_BASE =
e613502831e3b412780d69424d4a1e433a4ae688          (P_R6PLUS_A2, published A-2 closeout;
                                                   tree 48b00681a727ea4bd8768c3e9b0159bc01573ff0)

PREPARATION_BASE =
e613502831e3b412780d69424d4a1e433a4ae688          (the same commit; preparation branch
                                                   phase/pre-g9b-r6-plus-c-prompt, local)

IMPLEMENTATION_BRANCH_START =
6cb09d55e1ce9b18ef046f445da5494288fd8ecd          (D_R6PLUS_C_PROMPT, reconciled preparation
                                                   candidate; tree
                                                   00f80cc75db6de6542bb295cef813e70c9823b9c;
                                                   not published)

IMPLEMENTATION_BRANCH =
phase/pre-g9b-r6-plus-c-2d-export-completion     (local; no rebase)
```

A moving branch is not a base. Entry gate (passed on 2026-10-04 before this
amendment): local `main`, `origin/main` and the live remote `main` equal
`P_R6PLUS_A2` with its tree, the branch start exists with its tree, and the
worktree is clean. The phase works on the implementation branch from the named
commit and is not rebased onto any later commit without a new author
instruction; if the remote `main` changes, the phase stops and reconciles.
Between `P_R6PLUS_A2` and the branch start only documentation changes, so the
citations above apply at both; the phase re-establishes every one it relies on
before using it. Both preparation candidates stay immutable.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source, tests, build and serialization at the base.
3. The unit-system specification and ADR 0032; the `B`, `D0`, `D1`, `A` and
   `A-2` author-decision and closeout records; the accepted export
   specifications and ADR 0005 and 0014 (in force until amended by `C`); the
   [C author-decision record](../../../docs/validation/pre_g9b_r6_plus_c_author_decisions_record.md)
   and the author's dispositions of `DQ-C1` to `DQ-C17` in the
   [C preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_c_prompt_closeout_record.md),
   which prevails over every earlier default.
4. The accepted `A-1`, `B`, `D1` and `A-2` implementations.
5. Evidence, re-established before use: the
   [C characterization report](../../../docs/validation/pre_g9b_r6_plus_c_preparation_characterization_report.md)
   and its mirror; the `P0` report and the `C` design candidate.
6. Generated artifacts, earlier reports and previous agent output are evidence,
   not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Shared GeoCeDG export package (`org.geocedg.common.export`)

- The read-only semantic curve export adapter of C10, extracted from
  `G9X1GeometryExportAdapter`, with G9X1 rewired to it behavior-identically,
  returning per-component outcomes without writability.
- The derived export classification of C14 (source and export levels) and the
  LaTeX admissibility policy; no kernel state.
- The DXF `B1` participation rule of `DQ-C13`: the exact predicates per neutral
  entity, the conservative certified test for semantic components, an
  `OUTSIDE_EXPORT_AREA` outcome distinct from every fidelity failure, and the
  area record.
- `GeometryExportModel`: the unit tokens, the effective construction unit and
  the `usm` factor (C6).
- `DxfExporter`: the `$INSUNITS` code from the model unit; the `LAYER` table
  state of `DQ-C2`.
- `G9X1GeometryExportAdapter`: the fingerprint inputs of C6 and C9-DXF (unit
  state, hidden-layer set under `DQ-C2`, the explicit area under `DQ-C13`); the
  visibility policy of `DQ-C2`/`DQ-C3`; the area policy of `DQ-C13`.
- Format-independent export computations needed by several exporters
  (`pPhysical`, the device-unit and overflow checks of C4).

### Kernel (one read-only query)

- In `org.geocedg.common.kernel.locus.intersection`, one public read-only query
  over the existing package-private ADR 0028 `CertifiedIntervalCurveModel2D`
  that answers whether a component of a captured `LocusDefinition2D` is proven
  disjoint from a closed rectangle, with a deterministic bounded bisection
  budget (C9-DXF). It adds no semantics, no state, no dependency and no
  persistence, and never changes a certificate; any wider kernel change is a
  stop.

### Shared upstream seams (host-identical defaults)

- `common/export/pstricks/GeoGebraExport.java` and the three dialect classes:
  a bounds-initialization seam, a bound-edit seam, the effective-visibility gate
  through the existing `App.isLayerShown` (true in the host), and a dispatch hook
  for elements the host does not export, defaulting to the current debug log;
  nothing else.
- `common/main/App.java`: the narrowest seams those hooks need, with
  primitive, `String` or existing common types.
- `common/plugin/GgbAPI.java` only where `DQ-C16` routes the LaTeX API through
  the seams.

### Desktop

- `org.geocedg.desktop.export`: the `drawingScale` value type, the GeoCeDG
  scale presentation, the GeoCeDG LaTeX writers of semantic curves (subclasses
  or helpers created through `AppGeoCeDG`'s `newGeoGebraTo*` overrides), the
  physical-scale computation.
- `AppGeoCeDG`: the `drawingScale` holder and its reset at the events `E1`–`E6`
  of C2 (the completed-New branch of `fileNew`, the two transition hooks, the
  successful `runDocumentLoad`, the API-replacement marker observed in the
  `setXML` override, and a `reset()` override for `E6`),
  the seam answers, the LaTeX factory overrides. No change of any `A-2`,
  `ExportArea` or paste-notice reset.
- `desktop/plugin/GgbAPID.java`: the GeoCeDG-gated marker of `E5` around
  `setXML(String)`, host-identical for Classic.
- `desktop/export/GraphicExportDialog.java`, `PrintScalePanel.java`,
  `PrintPreviewD.java`, `desktop/export/pstricks/ExportFrame.java`,
  `desktop/euclidian/EuclidianViewD.java`, `desktop/main/AppD.java`,
  `desktop/plugin/GgbAPID.java`, `geocedg-desktop/export/PictureExportService.java`,
  `ExportViewport.java`, `PictureExportCommandLine.java`: the physical-scale and
  UI seams of C3, C4, C17 and `DQ-C16`, GeoCeDG-gated.
- `freehep/emf/**`: only the explicit GeoCeDG opt-in exact frame of `DQ-C9`,
  under the `B` provenance gate and records; default behavior byte-identical.
- The DXF controller, preflight presentation and sidecar writer (C6, C8,
  `DQ-C11`, `DQ-C13`).
- Profile: the export action notes and, under `DQ-C3`, the DXF selection-contract
  reference; profile pins through the existing mechanism.

### Supporting changes

- Tests for every obligation below and their fixtures, registered in
  `docs/upstream/modified-files.yml` where they live under `source/`.
- Every modified upstream or third-party file registered in
  `docs/upstream/modified-files.yml` with the narrowest rationale; `THIRD_PARTY.md`,
  the component matrix and the FreeHEP provenance record only under `DQ-C9`.
- The registered phase selection `PRE-G9B-R6-PLUS-C` with its two JUnit
  selections, the JUnit inventory and the registry pins through the official
  updater, the registry-shape pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`,
  and the profile-authority allow-list only if action identifiers change.
- The specification, ADR, verifier-pin, guide and developer-guide amendments of
  *Required design/specification*, in the same candidate.
- The candidate report and its machine-readable evidence.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Document-unit redesign; any change of `<geocedgUnits>`, the unit state, its
  undo, its preferences or its paste provenance; any new document serialization
  of `drawingScale`, export area, tolerance or any export state; any new undo
  state.
- `A-2` layer-semantics redesign; changing object visibility to emulate hidden
  layers; making hidden layers geometric, DAG, construction or object state.
- `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` (`F2`); `F3` File → Insert and the
  Apply Template re-layering question; `E1` GGT library; `E2` native
  dimensions; `E3` `IsoABorder`; `F1`, `F2`, `F3` implementation; `G`.
- 3D export, STL, Collada and AR unit semantics; 3D hidden-layer semantics.
- The construction-protocol rollback debt (`OBS-D1-ROLLBACK-CONSPROT-GUI`).
- A general solution of `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`:
  no `ExportArea` or paste-notice reset may be added, moved or removed.
- Any change of the `A-2` document commit, including
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` (recorded by this preparation, not
  assigned to `C`).
- Any change of the DXF strict writability and partial-output rule (fidelity
  spec §7, ADR 0014 Decision 6); the `DQ-C5` direction concerns LaTeX.
- `ENH-B-MANUAL-EXPORT-AREA-DRAG` unless separately authorized.
- An exact spline authority not justified by the accepted semantic model
  (`DQ-C1`); an exact DXF `SPLINE`; implicit-curve contouring.
- Kernel changes: Locus V2 or Spline V2 semantic identity, domains,
  certificates, DAG, persistence or evaluation; command semantics beyond
  `DQ-C16`; any kernel addition beyond the one read-only certified-disjointness
  query of C9-DXF.
- The pre-existing upstream LaTeX defects outside `C` (the parametric warning
  without a newline, the warning flags never reset, the empty
  `getLineTemplate` of PSTricks and Asymptote) unless the author adds them.
- Edits of `AGENTS.md`, `CLAUDE.md`, `.github/prompts/**` other than this
  prompt's authorized amendment, `ai-shell/prompts/**`, the verifier
  architecture, its schemas or `prompt-contracts.json`, beyond registry and
  inventory data updated through existing mechanisms and the pins of `DQ-C15`.
- Editing frozen evidence (`geocedg/validation/export/g9x1/g9x1-evidence.json`,
  earlier candidate reports and closeout records).
- Any commit, move, rename, rewrite or copy of `artifacts/author-input/**`.

## Architectural placement

- `drawingScale`, the export dialogs and the physical-scale computation are
  export and presentation (`AGENTS.md` §4: engineering export scale belongs to
  export dialogs and adapters; zoom is never metric authority); the unit state is
  read, never written; the semantic adapter is an external read-only consumer of
  kernel semantics (`AGENTS.md` §4, §13).
- Each upstream seam is the narrowest that serves the behavior,
  upstream-identical for Classic, and recorded in
  `docs/upstream/modified-files.yml`. If an owner is wrong at the base, stop and
  report instead of choosing another layer.

## Required design/specification

The unit-system specification, the author records, the `DQ-C` dispositions and
this prompt are the design. Before product edits, the phase records in its
candidate report the re-established seams, the chosen class, method and seam
names, and every correction to C1–C18. In the same candidate, and only to
describe delivered behavior:

| Document | Amendment |
|---|---|
| `geocedg/specs/units/unit-system.md` | status and implementation markers of §0, §6.4 (export-model row), §9 (fingerprint row), §15 (heading, §15.1 "until `C`" wording) and §18.2; no semantic clause changes; a needed semantic change is a stop |
| `geocedg/specs/export/geometry-export-foundation.md` | units section (`$INSUNITS` mapping, unit-bearing model, `usm`), removal of the pending-amendment note, the G5 PASS clause (`$INSUNITS = 0` for unspecified documents, the mapped code otherwise), the layer and visibility lines (hidden GeoCeDG layers as OFF DXF layers, `60 = 1` for individually hidden objects, the truthful population wording), the `B1` area rule (explicit producers only, `VISIBLE_VIEWPORT` no boundary, participation by exact predicates and the conservative certified test, whole exact entities, `OUTSIDE_EXPORT_AREA` reporting; no clipping); status stays `Experimental` |
| `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` | §2 unitless statement, §7 dialog units, hidden-source and hidden-layer rules and the area rule, §8 sidecar units, `usm` trigger, area record and schema; the strict DXF writability rule unchanged; version 1.1 |
| LaTeX semantic-curve contract | the C14 result model, admissibility policy, comments and report, recorded in the developer guide and the candidate report on the `B` precedent (no new normative export specification unless the author asks) |
| `docs/adr/0005-neutral-2d-geometry-export.md` | a dated, versioned amendment section: Decision 3 governed the G5 experimental contract and is superseded for `C`-era GeoCeDG export by the `D0`/`C` unit contract; the original text stays (`DQ-C17`) |
| sidecar contract | `DxfFidelityManifestWriter` and the fidelity spec §8: an explicit `schema_version` evolution with versioned unit metadata, the area record, hidden layers and outside-area outcomes; old version-1 sidecars never reinterpreted (`DQ-C11`) |
| verifier | `tools/agent/verify-g9x1-extended-dxf.ps1` live-document pins (living fidelity spec hash recorded as a successor naming `C`; guide literal), frozen G9X1 evidence untouched (`DQ-C15`); no other verifier file |
| staleness fingerprint | `G9X1GeometryExportAdapter`: the unit state, the hidden-layer set and the explicit area enter the preflight currentness check |
| living documentation | both user guides (export, units and DXF sections, the "until `C`" statements), the developer guide, `geocedg/specs/ui/cedg-workspaces.md`, the profile notes of the export actions |

No preparation or intermediate commit may claim that the product implements
`C`; the amendments land with the implementation, in one candidate.

## Geometric invariants and degeneracies

`C` changes no geometric definition, algorithm, dependency or numeric value.

| Case | Required behavior |
|---|---|
| any unit, `drawingScale`, area, hidden-layer or tolerance change | coordinates, numeric geometry, DAG, identities, Locus V2 branches and components, Spline V2 semantics, construction XML unchanged |
| `presentationUnit` change with fixed construction unit | every output byte-identical |
| zoom, viewport, view scale or DPI change | physical sizes identical; DPI changes only raster resolution |
| `drawingScale` change | export interpretation only; no undo point; document not modified |
| tool replacement, macro editing, `.ggt` open or save, preference reload, merge, undo, redo, redefine, rollback, temporary clear | `drawingScale` unchanged (C2) |
| completed New, successful Open or full-document replacement (any transport) | `drawingScale` `1:1` (C2) |
| semantic source with certified components and established incompleteness or failed components | certified components emitted, evidence disclosed, export `INCOMPLETE_WITH_CERTIFIED_COMPONENTS` (C14) |
| hidden-layer change | export inclusion (picture, LaTeX) or DXF layer state (`DQ-C2`) only |
| Locus V2 with several components, gaps, discontinuities | one path per certified component; gaps never bridged; multiplicity kept |
| missing domain, invalid component, work limit, stale source | explicit outcome per C14 and `DQ-C5`; never silent |
| export area disjoint from or larger than the viewport | complete output in picture (`B`) and LaTeX; the area never becomes a domain |
| no explicit export area (`VISIBLE_VIEWPORT` fallback) | DXF exports model space as at the base, zoom-invariant (C9-DXF) |
| explicit export area | DXF consumes it by `B1` participation; whole entities; nothing outside is dropped silently |
| unspecified unit | no engineering scale; DXF 0; neutral `UNITLESS` |
| identical document and export-session inputs | identical output bytes |

## Compatibility and serialization

No document serialization changes: `drawingScale`, the export area, the
tolerance and every dialog value are session or dialog state; documents re-save
byte-identically whatever their values. Output files change: picture and print
sizes become physical for physical documents (and stay device-scale, relabelled,
for unspecified ones); DXF gains the unit header for physical documents, the
`usm` sidecar, the `DQ-C2` layer state and the explicit-area rule of `DQ-C13`
(documents with `Export_1`/`Export_2` now export through that rule); the
sidecar schema follows `DQ-C11`; LaTeX gains the export area, effective
visibility, the unit, the semantic curves it omitted and the C14 result
disclosure. Classic output is unchanged. Legacy unspecified
documents keep `$INSUNITS = 0` and the G5 and G9X1 DXF bytes.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation; each runs in the phase selections and
in `final.shared` or `final.desktop`:

| ID | Obligation |
|---|---|
| `T-ROUTES` | the C1 matrix after `C`, route by route, including Print Preview, clipboard, CLI, `ExportImage`, the APIs and the Save preview |
| `T-SCALE-VALUE` | parsing, normal form, gcd, equality of equivalent pairs, the `DQ-C6` range and refusals |
| `T-SCALE-LIFECYCLE` | default `1:1`; reset to `1:1` exactly at `E1`–`E6` of C2: completed New (every entry), native Open by chooser, recent, drop and API `openFile`, Base64, `.html`, non-native file and URL, `loadXML(String)`, API `setXML`, and `E6` as disposed; **unchanged** through tool replacement (Desktop overwrite and the shared model), the macro-edit window, `.ggt` open, save and delete, the tool library, portable and installed preference reloads (including *Restore default settings*), the Open URL pre-clear and its cancel, `evalXML`, non-clearing `setXML`, paste, Insert File, Apply Template, undo, redo, redefine, rollback, rejected parses, failed loads and a cancelled New; never in XML, undo or preferences; no modified flag; one value per window; no `A-2`, `ExportArea` or paste-notice behavior changed |
| `T-SCALE-UI` | the `a:b` control in Picture, Print Preview and the three LaTeX dialogs; presets; derived LaTeX unit and size; `DQ-C8` labels |
| `T-PHYSICAL-PICTURE` | PNG pixels, PDF points, SVG size, print and EMF (`DQ-C9`) for `mm`, `cm`, `m`, `usm` × `1:1`, `1:2`, `2:1` and normalized equivalents, against the formulas of C4 |
| `T-PRESENTATION-INVARIANCE` | equivalent `presentationUnit` changes leave every output byte-identical (`AQ-U1`) |
| `T-ZOOM-INVARIANCE` | zoom, view scale, viewport and DPI leave physical sizes unchanged; DPI only resolution; the view `printingScale` is never read in physical mode |
| `T-COORDINATES` | DXF and LaTeX model coordinates and the construction XML unchanged by unit, scale, area and visibility |
| `T-UNSPECIFIED` | C17 for every exporter; no `a:b`, no `1:n`, no cm inference; DXF 0 and `UNITLESS` |
| `T-EXTREMES` | very large and very small scales and units; explicit failures, never silent clamps (C4, `DQ-C6`) |
| `T-LATEX-AREA` | bounds from `ExportArea`; inside, larger and disjoint areas; bound edits write `MANUAL`; the selection rectangle never read or written in GeoCeDG |
| `T-LATEX-VISIBILITY` | hidden layers and hidden objects excluded; the inclusion set equals the picture's |
| `T-LATEX-UNITS` | `xunit = yunit = fb(c)·100·a/b` per dialect; device parameters when unspecified |
| `T-SEMANTIC-ADAPTER` | C10 for branches, components, orientation, gaps, closure, multiplicity, coverage, revision; zero render evaluations; construction untouched; determinism |
| `T-LATEX-SEMANTIC` | per dialect: one path per component, closed paths, disclosure (`DQ-C12`), deterministic text |
| `T-LATEX-FAILURES` | every C14 case: a certified component emitted beside a failed one of the same source; `NOT_ESTABLISHED` completeness; gaps and discontinuities never bridged; `MISSING_DOMAIN`, `WORK_LIMIT`, `DISCONTINUITY_UNRESOLVED`, `DEGENERATE_SOURCE` per component; `TOLERANCE_NOT_ESTABLISHED`, invalid tolerance and `STALE_SOURCE_REVISION` rejecting the export with no code; failure evidence present in comments and report |
| `T-EXPORT-RESULT-MODEL` | the C14 source and export classifications derived from the outcomes, for every combination of complete, incomplete, failed and rejected sources; the LaTeX header and per-source comments and the dialog report state them; no kernel state; DXF writability unchanged |
| `T-SPLINE` | `SplineV2` degrees 3 and above through the same contract (`DQ-C1`) |
| `T-LEGACY-LOCUS` | legacy `GeoLocus` LaTeX output unchanged |
| `T-DXF-UNITS` | `$INSUNITS` 4, 5, 6, 0 read back by the test reader; neutral model unit; coordinates unchanged |
| `T-DXF-USM` | `$INSUNITS = 0`, warning, mandatory sidecar, token and canonical `metersPerUnit` |
| `T-DXF-SIDECAR` | the `DQ-C11` schema; header and sidecar agree |
| `T-DXF-STALENESS` | a unit change, a hidden-layer change (`DQ-C2`) and a change of the explicit area (`DQ-C13`) make a pending preflight stale |
| `T-DXF-VISIBILITY` | `DQ-C2`, `DQ-C3` |
| `T-DXF-AREA` | the `DQ-C13` disposition: no explicit area (`VISIBLE_VIEWPORT`) gives the base bytes of the G5 and G9X1 corpora at every zoom; `MANUAL` and both `EXPORT_POINTS` producers; inside, crossing, outside and boundary-touching sources of every family, including infinite lines and rays, an area-enclosing circle and closed polygons; Locus V2 and Spline V2 components per component; complete-construction and current-selection modes; outside-area reporting, header comment and sidecar area record; area of the active Graphics view; determinism and handle order; a missing semantic domain never replaced by the area |
| `T-G9X1-IDENTITY` | DXF bytes and sidecars of the G9X1 and G5 corpora identical for unspecified documents after the adapter extraction |
| `T-B-REGRESSION` | the `B` picture and surface contracts, updated only where `C` supersedes the interim state |
| `T-D1-REGRESSION` | the unit state, its XML and undo unchanged by every export; the superseded unit-independence test replaced |
| `T-A2-REGRESSION` | hidden layers after reopen: picture, LaTeX and DXF agree with `effectiveVisible` and `DQ-C2` |
| `T-CLASSIC` | Classic picture, print and LaTeX dialogs and output byte-identical to a `git archive` of the base on a corpus |
| `T-WEB-BOUNDARY` | static scan: no `org.geocedg` import and no non-GWT type in `common/export/pstricks`; new `App` seams use GWT-compatible types |
| `T-NO-SERIALIZATION` | document XML, undo XML and preferences identical whatever `drawingScale`, area or tolerance |
| `T-DETERMINISM` | identical document and session inputs give identical bytes in every format |
| `T-PREVIEW` | `DQ-C10` |
| `T-API-ROUTES` | `DQ-C16` |
| `T-HISTORICAL-PINS` | `DQ-C15`: the live-document pins match the amended files; frozen evidence unchanged |
| `T-GUIDE` | the guide suites and literals |
| `T-SMOKE` | an explicit author-smoke checklist with prepared fixtures and inspection helpers (no hand-editing of DXF or XML); the agent does not perform author smoke |

Author precisions (authorization of 2026-10-04), each mechanized inside the
obligations above and named in the obligation-to-test map: tool and macro
replacement does not reset `drawingScale`; API full-document `setXML` does; API
`reset` (`E6`); equivalent-pair normalization; `presentationUnit` invariance; no
physical authority from `printingScale`, zoom or DPI; physical PNG, PDF, SVG and
EMF sizes; EMF `rclFrame` nearest 0.01 mm; Classic and default FreeHEP
unchanged; hidden DXF layer OFF; individually hidden entity `60 = 1`; explicit
area participation for every G5 entity family; participation in current
selection; `VISIBLE_VIEWPORT` never makes DXF zoom-dependent; conservative
certified Locus V2 participation near the boundary; `OUTSIDE_EXPORT_AREA` is not
a fidelity error; G5 and G9X1 bytes unchanged without an explicit area or unit
change; a locally certified incomplete Locus V2 is emitted and visibly reported
incomplete; stale-revision and tolerance-guarantee failures reject; semantic
gaps stay gaps; no Spline V2 exactness special case; mandatory `usm` sidecar;
the unspecified-unit non-physical mode; an explicit API or CLI render scale
never mutates `drawingScale` or physical meaning.

Mandatory corpus (re-established at the implementation base): construction
units `mm`, `cm`, `m`, a valid `usm` and unspecified; scales `1:1`, `1:2`,
`2:1` and equivalent non-normalized pairs; `presentationUnit` different from
`constructionUnit`; export area inside, larger than and disjoint from the
viewport; persistent hidden layers after reopen; individually hidden objects;
the standard exact DXF entities; a Locus V2 with several components, with gaps
and a discontinuity; finite-domain and missing-domain cases; a work-limit case;
`SplineV2` of degree 3 and above; a legacy `GeoLocus`; `D1` unit-bearing
documents; `A-2` layers above 9; PNG, PDF, SVG and EMF physical output;
PSTricks, PGF/TikZ and Asymptote; DXF with and without sidecar; a `usm` DXF; a
legacy unspecified document; New, Open and non-native replacement resets of
`drawingScale`; invariance across zoom and `presentationUnit`.

Harness rules: Desktop tests mock `JOptionPane`, inject the spatial-redefine
presentation and the dialog prompts, use an injected preference store, wait for
the asynchronous undo store with latches, install `LoggerD` per test, start in
Move mode, never fire `Ctrl+Shift+C`, `Ctrl+Shift+M` or `Ctrl+Shift+B`, never
open a real modal dialog, and set strokes to full opacity before color checks.
Shared tests log diagnostics as text.

Adjacent regressions before freezing (development evidence, not acceptance):
`PreG9BR6PlusBPictureFidelityTest`, `PreG9BR6PlusBExportSurfaceTest`,
`PreG9BR6PlusA1HiddenLayerTest`, `PreG9BR6PlusA1LayerWorkspaceTest`, the
`PreG9BR6PlusA2*` and `PreG9BR6PlusD1*` classes, `GeometryExportFoundationTest`,
`G9X1G5CorpusCompatibilityTest`, `G9X1AdaptiveCurveExportTest`,
`G9X1LocusV2ExportTest`, `G9X1PreflightFidelityTest`, `G9X1DxfManifestTest`,
`G9X1PairedOutputTest`, `G9X1DesktopPreflightContractTest`,
`PreG9BS1AuthorDxfReproductionTest`, `LocusV2RenderSeparationTest`, the Spline V2
classes, `PstricksTest`, `CommandsTest`, `PreG9BR6CapabilityMatrixTest`,
`G9U1ActionRegistryTest`, `G9U1ProfileCompilerTest`, `G9U1WorkspaceSurfaceTest`,
`GeoCeDGProfileTest`, the guide suites; Checkstyle
`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`,
`:desktop:desktop:checkstyleMain` and `:desktop:desktop:checkstyleTest` (read the
XML reports; ASCII escapes in non-test sources); `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522`; the historical
`verify-g9x1-extended-dxf.ps1` and `verify-dxf.ps1` only if `DQ-C15` requires
their live pins to be exercised; `git diff --check`.

Registration and catalog: register the phase selection `PRE-G9B-R6-PLUS-C`
(`compile.shared.semantic`, `compile.desktop.semantic`,
`junit.shared.pre-g9b-r6-plus-c.semantic`, `junit.desktop.pre-g9b-r6-plus-c.semantic`,
`workspace-profile.product` when the profile changes, and the GGBScript matrix
leaves when `DQ-C16` changes `ExportImage`), following the
`PRE-G9B-R6-PLUS-B` precedent. Discovery dry-run evidence for both modules and
**executed** selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand. When registry-shape pins change, run
a development `INFRA_UNIT` on the staged tree before freezing.

Acceptance (the planned `INTEGRATED_PHASE` contract), on one clean immutable
committed candidate:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C -PlanOnly
tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-C -LogDirectory <log root>
tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<fresh-name>
```

```text
INTEGRATION -LogDirectory : artifacts\agent\<fresh-name> inside the repository, not
                            pre-existing (packaging.repository-safety requires it)
console log               : the session scratchpad, never under artifacts\
```

Both runs `ACCEPTED / COMPLETE` on the same exact commit and tree; no further
commit afterwards. `FINAL` is not required by this class and is not run; a
request to run it is a `VERIFICATION_ESCALATION_REQUEST` for the author. If
product or test code changes after acceptance, that candidate is no longer the
accepted candidate: stop and report. The standing diagnostics
(`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency`
`DIAGNOSTIC_UNAVAILABLE`) are pre-existing and do not affect acceptance. A
failure proven to predate the candidate and lie outside its delta is retained
baseline debt per the task template, not phase scope. Report exact commands,
exit codes, run ids, plan and result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's instruction of 2026-10-04 authorizes exactly: local
implementation on the implementation branch from the exact branch start, local
commits, technical verification, the registration of the phase selection, the
registered `PHASE` and `INTEGRATION` acceptance runs on one exact frozen clean
candidate, and that candidate for author review and smoke. No instruction
derived from this file authorizes self-approval, author smoke by the agent, a
`FINAL` (unless a stop condition forces a reclassification the author then
decides), or any change of the class.

`C` authorizes nothing that follows it. The operational order is
`P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`; it is the
order the author chose, not a dependency. `E1`, `E2`, `E3`, `F1`, `F2`, `F3`,
`G`, `PRE-G9B-R7` and `G9B` stay unauthorized, and so does every open
observation and enhancement not named in a `DQ-C` disposition. Author approval
is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local implementation branch from the exact branch start and local commits
only. Push, branch publication, merge, promotion to `main`, rebase, squash,
amend after freeze, force push, tag, release and binary publication are
forbidden; each needs a separate explicit author instruction naming the exact
candidate SHA. Acceptance evidence never grants publication authority.

## Acceptance and closeout

`C` stops with one technically verified candidate pending author review and
author smoke: `PRE-G9B-R6-plus-C = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`,
with `selfApproved = false`. Author approval is an explicit decision naming the
exact accepted commit. The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps. No receipt is expected for this class.

## Required artifacts

- The implementation, tests, fixtures and registrations above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-established seams and every correction to C1–C18; the chosen names, seams
  and defaults; the `DQ-C` dispositions as implemented; the route matrix after
  `C`; the physical-size tables per unit, scale and format with their formulas;
  the EMF evidence of `DQ-C9`; the DXF header, sidecar and staleness evidence;
  the specification, ADR and verifier-pin amendment map as delivered; the
  Classic `git archive` comparison; the obligation-to-test map; residual risks
  and observations, including the state of
  `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (unchanged); the
  author-smoke checklist.
- The author-smoke checklist covers at least: `a:b` scales in Picture, Print
  Preview and each LaTeX dialog for `mm`, `cm`, `m` and a `usm`; a printed or
  measured PDF at `1:1`; the same document after a `presentationUnit` change; an
  unspecified document; New and Open (including an `.html` or Base64 document)
  resetting `1:1`; undo after a scale change; a tool replacement and a macro
  edit leaving the scale unchanged; a hidden layer and a hidden object in LaTeX
  and DXF; a LaTeX export of an area outside the window; a Locus V2 and a
  Spline V2 in each dialect with their disclosure; a Locus V2 with a certified
  component and a failed or incomplete one (export marked incomplete); a Locus
  V2 that fails entirely; DXF with each unit and with `usm`; DXF with and
  without an explicit export area; EMF opened in a consumer that shows the
  physical size; the Save preview conditions; the Classic diagnostic
  application.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale, the
  verification-infrastructure-impact assessment, `GUIDE_IMPACT` with paths,
  `SERIALIZATION CHANGE = NONE`, and the exact `PHASE` and `INTEGRATION`
  commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- correctness requires serializing `drawingScale` or making it undoable;
- correctness requires treating `presentationUnit` as a physical scale;
- correctness requires converting model coordinates for export;
- correctness requires deriving a physical model unit from zoom, DPI,
  `printingScale` or the viewport;
- correctness requires render tessellation as semantic curve export;
- correctness requires inventing branch or component identity from sampled
  coordinates;
- correctness requires modifying Locus V2 or Spline V2 semantic geometry,
  domains, certificates, DAG or persistence;
- correctness requires changing Classic export semantics materially;
- correctness requires making hidden-layer state geometric truth;
- correctness requires a second `ExportArea`, or the selection rectangle as an
  authority;
- a semantic curve would be omitted silently;
- the specification, product, verifier or sidecar unit contracts would disagree
  in any commit;
- `drawingScale` cannot keep its lifecycle without solving
  `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`, or an `ExportArea`
  or paste-notice reset would change;
- `drawingScale` would reset on a non-document reload (tool replacement, macro
  editing, preference reload) or at `clearConstruction`/`commitLoadedDocument`,
  or the API document replacement cannot be marked without changing an `A-2`
  commit;
- LaTeX would emit an uncertified component, bridge a gap or an invalid
  interval, call a partial export complete, or drop failure evidence;
- the DXF area rule would consume the `VISIBLE_VIEWPORT` fallback, make a DXF
  depend on the zoom, replace a missing semantic domain, derive component
  identity from sampled coordinates, clip geometry, change an entity family, or
  exclude a semantic component without a certified disjointness proof;
- the certified participation test needs more than one read-only kernel query
  over the existing certified model;
- the DXF strict writability rule or the `A-2` document commit would change;
- the work would absorb `F3`, `F2`, `E1`, `E2`, `E3` or other residual debt;
- the global verifier architecture would change, or the EMF correction would
  require a global FreeHEP semantic change affecting Classic or default
  behavior, or a FreeHEP family other than the EMF opt-in would change;
- an existing API or CLI route cannot keep backward compatibility together with
  the `DQ-C16` distinction between device/render parameters and the physical
  drawing scale;
- the G9X1 adapter extraction changes a DXF byte of an unspecified document;
- an implementation choice contradicts a `DQ-C` disposition of the
  authorization record;
- current governance requires a verification class other than
  `INTEGRATED_PHASE`;
- the `PHASE` or `INTEGRATION` run is rejected for a cause attributable to the
  candidate, or its coverage is incomplete or untrusted;
- product or test code would change after acceptance.

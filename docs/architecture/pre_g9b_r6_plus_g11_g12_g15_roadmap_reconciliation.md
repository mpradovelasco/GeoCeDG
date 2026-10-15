# G11, G12 and G15 — roadmap scope reconciliation after PRE-G9B-R6-plus

```text
REPORT_KIND        = ROADMAP SCOPE RECONCILIATION — PREPARED FOR PRE-G9B-R7 REVIEW
AUTHORITY          = author instruction "GeoCeDG — PRE-G9B-R6-plus-G Final Author Approval,
                     Global R6-plus Closeout and G11/G12/G15 Roadmap Reconciliation"
                     (2026-10-15), §9-§10
G11 / G12 / G15    = PENDING — ROADMAP RECONCILIATION PREPARED (none marked PASS)
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15
ROADMAP CHANGE     = dated factual pointers only; substantive restructuring waits for the
                     author's approval of the revised future scope
IMPLEMENTATION     = NOT AUTHORIZED (G11, G12, G15, PRE-G9B-R7)
selfApproved       = false
```

Bounded slices of three future gates were delivered and approved ahead of them, under the
mini-track rule that delivers slices "without moving their ownership"
([plan](pre_g9b_r6_plus_minitrack_plan.md) §1.1) and the A7 precedent for G12. This report
classifies every original roadmap work item of G11, G12 and G15 against approved evidence:
`IMPLEMENTED — AUTHOR APPROVED`, `PARTIALLY IMPLEMENTED — BOUNDED SLICE`,
`DESIGNED — NOT IMPLEMENTED`, `OPEN / DEFERRED`, `SUPERSEDED` or `NOT ESTABLISHED`. A bounded
slice is never the whole item. Nothing here closes, implements or deletes a requirement.

Abbreviations: `RM` the [roadmap](../roadmap/geocedg_roadmap.md); `PL` the mini-track plan;
`V/` `docs/validation/`; `AR/` `docs/architecture/`; `SP/` `geocedg/specs/`; `ADR/`
`docs/adr/`; `GD/` and `GDT/` GeoCeDG Desktop main and test sources
(`source/desktop/desktop/src/{main,test}/java/org/geocedg/desktop/`); `GC/` and `GCT/` GeoCeDG
shared main and test sources; `UP/` inherited shared sources. Line numbers of `RM` and `PL`
refer to the approved technical candidate `da1a0952`; the closeout that adds this report also
inserts lines in both files. Evidence was gathered read-only from the repository and
spot-checked.

## 1. G11 — Capas y estados de vista (RM §G11; detail RM:1170-1185)

| # | Original item | Status | Implemented subset | Phase and approved evidence | Remaining contract | Owner | Proposed roadmap wording | Reducible | Design / decision |
|---|---|---|---|---|---|---|---|---|---|
| 1 | jerarquía (groups, sublayers, name, explicit order) | `OPEN / DEFERRED` | none: the flat integer layer `0..99` is not a hierarchy | A-2 excluded hierarchy, roles, locking, filters and named states (V/pre_g9b_r6_plus_a2_author_decisions_record.md:103-106); PL §1.1 keeps them with G11; no source concept | serializable hierarchical model | G11 | «Jerarquía de capas (grupos, subcapas, nombre y orden explícitos) construida sobre la capa numérica plana `0..99` de `PRE-G9B-R6-plus-A-1/A-2`, que no es una jerarquía.» | no | yes (model, serialization, ADR) |
| 2 | roles | `OPEN / DEFERRED` | none | none | role vocabulary, semantics, styles (RM:1180-1182) | G11 | «Roles de capa con estilos por rol, sin precedente implementado.» | no | yes |
| 3 | bloqueo (selection and edit locking) | `OPEN / DEFERRED` | none; hidden layers are not hittable (presentation, not locking: GDT/PreG9BR6PlusA1HiddenLayerTest.java:130) | `OBS-A1-NON-HIT-SELECTION` (P-6, deferred to G11 by the G closeout) | per-layer locking; P-6 | G11 | «Bloqueo de selección y edición por capa, incluida `OBS-A1-NON-HIT-SELECTION`.» | no | yes |
| 4 | filtros | `OPEN / DEFERRED` | none; Sort by Layer and the per-group eye are order and visibility (GD/GeoCeDGAlgebraView.java:100-122) | none | filters by role and layer in views and selection | G11 | «Filtros por rol y por capa; la ordenación `Sort by Layer` y la ocultación plana no son filtros.» | no | yes |
| 5 | visibilidad por vista | `OPEN / DEFERRED` | none per view: one document-wide hidden set, equal in Graphics 1 and 2, not applied in 3D | A-1/A-2 (GDT/PreG9BR6PlusA1HiddenLayerTest.java:78,99; V/pre_g9b_r6_plus_a1_closeout_record.md:132); A-2 forbade per-view persistent state; P-5 (3D) deferred to G11 | visibility per view (G1, G2, 3D) | G11 | «Visibilidad de capa por vista (Gráficos 1, Gráficos 2 y 3D) sobre el conjunto oculto documental único de `A-2`.» | no | yes |
| 6 | estados guardables (and print/export/construction flags) | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | one persisted, document-wide, non-undoable hidden set `<geocedgHiddenLayers version="1" layers="…"/>` | A-2 `06a2ea6b` (2026-10-04, smoke PASS, FINAL `verification-814f89fa…`): V/pre_g9b_r6_plus_a2_closeout_record.md:42-47,124-127,143-154; GC/kernel/layers/HiddenLayersXml.java:20-31; GDT/PreG9BR6PlusA2HiddenLayerPersistenceTest.java:132,252,622 | named or multiple states, per-view states, flags; the working layer stays SESSION | G11 | «Estados de capa con nombre y guardables (con indicadores `print`/`export`/`construction`), que amplían el conjunto oculto persistente entregado por `A-2`.» | yes (current-set persistence done) | yes |
| 7 | integración con DXF, PDF y modelos heredados | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` (flat numeric) | DXF layer `0`→`0`, `n`→`GEOCEDG_L<n>`, hidden layer OFF; hidden layers omitted from Picture/PDF/SVG; excluded from LaTeX; legacy documents load with an empty hidden set | C `6248bf0f`: V/pre_g9b_r6_plus_c_closeout_record.md:39-45,162-165; GC/export/DxfExporter.java:184; GDT/PreG9BR6PlusCDxfDesktopTest.java:219; picture route GD/export/PictureExportService.java:262-276 | hierarchical DXF layer tables, PDF layer structure (none designed), migration, background images (P-8) | G11 (G15 for publication) | «Proyección de la jerarquía a DXF, PDF/SVG y documentos heredados, partiendo de la correspondencia plana aprobada en `C`.» | yes (flat mapping done) | yes, plus P-18 for the C amendment |
| out | compatibilidad con capas numéricas de GeoGebra | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | Classic `0..9`, GeoCeDG `0..99`, clamping, `ShowLayer`/`HideLayer` unchanged; older readers clamp above 9 (accepted, `AQ-L1c`) | A-1/A-2: V/pre_g9b_r6_plus_a_author_decisions_record.md:35-41; GC/main/settings/config/AppConfigGeoCeDG.java:43 (`L_MAX = 99`); GCT/layers/PreG9BR6PlusA2LayerDomainTest.java | compatibility of a future hierarchy | G11 | «Preservar la compatibilidad con la capa numérica de GeoGebra al introducir la jerarquía.» | yes (becomes "preserve") | yes |

Implemented numeric-layer capabilities (none is a hierarchy):

| Capability | Phase | Evidence |
|---|---|---|
| explicit working layer (SESSION; new interactive objects; Move-group mode; chooser; adopt an object's layer) | A-1 `2a71133a`; Open initialization in A-2 | V/pre_g9b_r6_plus_a1_closeout_record.md:40-44; GD/GeoCeDGLayerWorkspace.java; GDT/PreG9BR6PlusA1LayerWorkspaceTest.java |
| layers beyond `0..9` (`0..99`) | A-2 | V/pre_g9b_r6_plus_a2_closeout_record.md:120-121; LayerDomain; `L_MAX` |
| flat hide/show, `effectiveVisible = objectVisible AND NOT layerHidden`, in painting and hit testing | A-1 (session), A-2 (document) | UP/euclidian/EuclidianView.java; GDT/PreG9BR6PlusA1HiddenLayerTest.java:78,99,130,253 |
| status-bar segments (layer, units, drawing scale, sheet) | A-1, D1, POST-E2-P1-R2, E3 | GD/GeoCeDGStatusBar.java |
| Sort by Layer with numeric group order and a per-group eye | A-1 | GD/GeoCeDGAlgebraView.java |

Not delivered: direct numeric working-layer entry (P-2, owner G11 by author decision); the
working-layer recomputation on tool replacement (B-2, deferred to G11 by the G closeout).

## 2. G12 — Navegación, zoom y escalas extendidas (RM §G12; detail RM:1187-1209)

| # | Original item | Status | Implemented subset | Phase and approved evidence | Remaining contract | Owner | Proposed roadmap wording | Reducible | Design / decision |
|---|---|---|---|---|---|---|---|---|---|
| 1 | zoom centred on the cursor, explicit ZoomWindow, configurable factor zoom | `IMPLEMENTED — AUTHOR APPROVED` (residue below) | cursor-anchored wheel/pinch/tools; keyboard fallback to the true centre; ratio-preserving ZoomWindow from menu and toolbar with Escape; two configurable factor actions; POST-E2-P2-R1 refuses text-typing chords | POST-G9U1-A7 final candidate `ed3f19a0` (tree `9b4385b7`), PHASE `verification-041cd859…` (91 tests), author smoke accepted, approval commit `5869f9b3` — **no dedicated closeout record; no approval date stated**: AR/post_g9u1_a7_navigation_design.md:3-4,41-47; V/post_g9u1_a7_navigation_design_matrix.md; RM:2916; P2-R1: V/post_e2_p2_r1_closeout_record.md:3-6; GDT/PostG9U1A7NavigationTest.java | ZoomWindow in secondary 2D views, 3D camera and Web | G12 (residue) | «Cerrado por `POST-G9U1-A7` y `POST-E2-P2-R1`; queda solo `ZoomWindow` en vistas 2D secundarias, cámara 3D y Web.» | yes | residue only |
| 2 | vistas nombradas (centre and scale per view) | `OPEN / DEFERRED` | none | deferred by A7 (design :157-165; matrix :121) and SP/ui/cedg-workspaces.md:186-188 | named per-view centre/scale, persistence class, relation to G15 viewports | G12 | «Vistas nombradas que conserven centro y escala por vista, con su clase de persistencia por decidir.» | no | yes |
| 3 | zoom previo (`ZoomPrevious`, `FitSelection`, `FitLayer`) | `OPEN / DEFERRED` | none | deferred by A7 | history independent of undo; `FitLayer` touches G11 | G12 | «Historial de zoom con `FitSelection` y `FitLayer`, aplazados por A7.» | no | yes |
| 4 | separación modelo/vista/dibujo/impresión | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | `constructionUnit`, `presentationUnit`, `usm` (document); `drawingScale` `a:b` (SESSION, DQ-C7); physical output `L·fb(c)·a/b` for picture, print and LaTeX; DXF `$INSUNITS`; physical PDF strokes and markers; zoom, DPI and viewport never authorities | D0 `8383a153` (SP/units/unit-system.md; ADR/0032); D1 `6e1d8459`; C `6248bf0f` (DQ-C7, DQ-C8); POST-E2-P1-R2 `6dde194e` (SP/export/physical-pdf-style-sizes.md); GC/export/PhysicalExportScale.java; GD/export/DrawingScale.java; GDT/PreG9BR6PlusCPhysicalExportTest.java:116,180,203 | view-side obligations: physical zoom aid (D-10, owner G12), per-view centre/scale; `drawingScale` is SESSION and one per window; legacy device scale for unspecified units; text and non-PDF styles still follow zoom | G12 (view side); G15 (paper scale, `TD-PDF-1`) | «Completar en el lado de la vista la separación, cuyo modelo de unidades y escala física ya es normativo (`unit-system.md`; `D0`/`D1`/`C`), incluida la ayuda de zoom físico.» | yes (units and drawing/print scale are not redefined) | yes (zoom aid; whether `drawingScale` becomes document state) |
| 5 | pruebas con rangos de escala extremos | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | spot tests: finite transform at 1E±14 (PostG9U1A7CursorZoomTest), ISO A numeric extremes, unrepresentable physical sizes fail explicitly, `DrawingScale` bounds, `usm` factors | A7, E3, C, D1 | a systematic invariance suite (metrics, intersections, certificates); "extended scale profiles" (PL §1.1) is undefined | G12 | «Batería sistemática de escalas extremas que demuestre la invariancia de métricas, intersecciones y certificados.» | partly | yes (define the profiles) |
| out | ninguna métrica depende de pantalla, zoom o DPI | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | geometric metrics invariant (unit-system §6; GDT/PreG9BR6PlusCPhysicalExportTest.java:203; GDT/PostG9U1A7NavigationTest.java:261) | as rows 1 and 4 | physical text sizes and non-PDF style sizes still depend on zoom | G12 / G15 | «Ninguna métrica geométrica depende de pantalla, zoom o DPI; queda la independencia de texto y estilos fuera del PDF físico.» | — | yes |

## 3. G15 — Hojas, PDF y formatos gráficos (RM §G15; detail RM:1235-1250)

| # | Original item | Status | Implemented subset | Phase and approved evidence | Remaining contract | Owner | Proposed roadmap wording | Reducible | Design / decision |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `DrawingSheet`, marcos y cajetines | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` (a frame in model space only) | `IsoABorder`: ISO 216 A0–A10 paper boundary, optional inner frame and label, an ordinary dependent construction in **model space** at the captured scale and unit — **not a `DrawingSheet` and not a title block** | E3/E3-R1 `7cd50116` (2026-10-10): V/pre_g9b_r6_plus_e3_closeout_record.md; SP/sheets/iso-a-border.md §1.4 (excludes title blocks, sheet sets, paper space, viewports, the G15 architecture); ADR/0036; PL §1.1; GC/kernel/sheet/AlgoIsoABorder.java; GCT/sheet/PreG9BR6PlusE3IsoABorderTest.java | `DrawingSheet` paper-space entity; title block as a document service; sheet sets; print styles; publication metadata (accepted limitations L-28) | G15 | «`DrawingSheet` en espacio papel con marco y cajetín como servicio de documento; `IsoABorder` (`E3`) es una anotación de espacio modelo y no se amplía a hoja.» | limited | yes (paper-space ADR; coexistence with `IsoABorder`) |
| 2 | escalas físicas y viewports | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` (physical scale yes, viewports no) | one SESSION `ExportArea` authority (explicit producer > `Export_1`/`Export_2` > visible viewport; producers MANUAL, EXPORT_POINTS, ISO_A_SELECTION, ISO_A_BORDER); offscreen export rendering; physical scale on the existing exporters | B `848206c4` (V/pre_g9b_r6_plus_b_author_decisions_record.md:36-67, AQ-X1/AQ-X2); E3 (iso-a-border.md §11); C; GD/export/ExportAreaSession.java; GDT/PreG9BR6PlusBExportSurfaceTest.java | paper-space viewports with their own scale, clip and layers; the area is SESSION only; `ENH-B-MANUAL-EXPORT-AREA-DRAG` not implemented; `GD/export/ExportViewport` is the offscreen renderer, not a G15 viewport | G15 (P-3) | «Viewports de espacio papel con escala física propia sobre la autoridad única `ExportArea` (`B`/`E3`) y la escala física de `C`, incluida la definición interactiva del área manual.» | yes | yes (viewport model; persistence) |
| 3 | PDF vectorial con tamaño exacto | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | exact page box (opt-in `setExactPageSize`, zero margins, no fit); physical page size; physical stroke and marker widths; embedded Type 1 fonts and text as shapes | B (DQ-B8), C, POST-E2-P1-R2 (ADR/0035; SP/export/physical-pdf-style-sizes.md), F4-D `6214d15a` (V/pre_g9b_r6_plus_f4_aggregate_closeout_record.md); FreeHEP `PDFGraphics2D.setExactPageSize`; GDT/PreG9BR6PlusBPictureFidelityTest.java:204; GDT/PreG9BR6PlusF4DEmbeddedFontPdfTest.java | retained debts: `TD-PDF-1` physical text size (D-8, G15 after a contract-design activity), `OBS-F4P-FREEHEP-XREF-SIZE` (O-10), path precision (L-19), `exportPDF` callback read (O-22), FreeHEP notes (L-6); a PDF generated from a `DrawingSheet` | G15 | «PDF de hoja con tamaño exacto desde `DrawingSheet`, reutilizando la página exacta, los anchos físicos y las fuentes incrustadas, y resolviendo `TD-PDF-1` tras su diseño de contrato.» | yes | yes (`TD-PDF-1` contract) |
| 4 | SVG y PNG | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | SVG exact fractional viewBox and physical size; PNG exact raster at the chosen DPI; EMF `rclFrame` | B, C: GD/export/PictureExportService.java; GDT/PreG9BR6PlusBPictureFidelityTest.java; GDT/PreG9BR6PlusCPhysicalExportTest.java | physical style sizes outside PDF; sheet-level SVG/PNG; SVG layer groups beyond the inherited `layer<n>` | G15 | «SVG y PNG de alta resolución de la hoja con tamaño físico, decidiendo los tamaños de estilo físicos fuera del PDF.» | yes | yes |
| 5 | pruebas dimensionales del fichero generado | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | PDF MediaBox and transform; per-format physical sizes (PDF, PNG, SVG, EMF); PDF stroke widths; compiled LaTeX page size; A3 landscape 1:50 reference | B, C, POST-E2-P1-R2, E3-R1 (GDT/PreG9BR6PlusE3R1ExportInteroperabilityTest.java) | sheet-level tests (frame, title block, margins, viewport scale, text) | G15 | «Pruebas dimensionales de hoja, ampliando las existentes de `B`, `C`, `E3-R1` y `POST-E2-P1-R2`.» | yes | no separate decision |
| out | publicación reproducible (marco, tamaño físico, escala, capas) | `PARTIALLY IMPLEMENTED — BOUNDED SLICE` | an `ISO_A_BORDER` area plus `drawingScale` plus hidden layers yields such an output | as above | not reproducible from the saved document (area, ISO link and `drawingScale` are SESSION); JVM-global `AutoColor` affects digests (O-23) | G15 | «Publicación reproducible desde el documento, no solo desde estado de sesión.» | — | yes (persistence of sheet, area and scale) |

**`ENH-B-MANUAL-EXPORT-AREA-DRAG`** — `OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15`.
Recorded at the B author smoke (V/pre_g9b_r6_plus_b_closeout_record.md:116 and its mirror);
planning owner G15 by author decision `DQ-G-6`
(V/pre_g9b_r6_plus_g_author_design_closeout_record.md:64); register row P-3. Design
objective: an interactive rectangle-selection gesture that updates the existing
`ExportArea(MANUAL)` and coexists with the numeric coordinate editor
(GD/AppGeoCeDG.java, coordinate prompt); no second export-area authority. Not implemented
now. This closeout adds it to the plan register and the roadmap G15 pointer, where it was
missing.

## 4. Owners after the G closeout

Author decisions: P-2 → G11 and P-3 → G15 (`DQ-G-6`); D-8 → G15 and D-10 → G12 (`DQ-G-6`);
B-2, P-5, P-6, P-8 → G11 (G closeout, Group C). Still proposals with the author accountable
(checkpoint `PRE-G9B-R7`): O-4, O-10, O-22, O-25 and the L-7 tracking (proposed G15).

## 5. Recommended roadmap revision (for PRE-G9B-R7 review)

1. Add a "Cortes acotados entregados" block to G11, G12 and G15 citing the approved
   closeouts (A-1, A-2, B, D0, D1, C, E3, POST-E2-P1-R2, F4-D, A7) and reword the remaining
   work relative to it (the proposed wordings above).
2. G12 item 1: closed by A7 and P2-R1; keep only the secondary-view, 3D and Web residue.
3. G12 "separación": point to `unit-system.md` as the normative authority; keep only the
   view-side obligations (zoom aid D-10, per-view centre and scale, invariance suite).
4. G15: replace "PDF vectorial con tamaño exacto" and "SVG y PNG" by "publicación de hoja";
   keep `DrawingSheet`, viewports, title blocks, sheet sets, `TD-PDF-1` and the FreeHEP debts.
5. G11 "integración": reduce to the hierarchical mapping; the flat mapping is done.
6. Update the gate rows RM:1300-1304 (pointers added by this closeout) and the consolidated
   row RM:1354 ("G11–G16 `NOT STARTED`").
7. Fold the decided owners (P-2, P-3, D-8, D-10, B-2, P-5, P-6, P-8) into the gate sections.
8. Define or drop "extended scale profiles" and "sheet sets"; decide whether `FitLayer` is
   G12 or G11.

None of the three phases can be marked `PASS`; G12 item 1 is the only item fully
implemented. Every reduction above needs the author's approval of the revised scope.

## 6. Contradictions to reconcile in PRE-G9B-R7

1. RM:1354 "G11–G16 `NOT STARTED`" against RM:1301/RM §G12 (A7 approved) and PL §1.1.
2. The RM G11 and G15 sections and rows did not mention the R6-plus slices (now pointed to
   this report); the G12 section mentions A7 but not the units and scale slice.
3. PL §1.1 restates the roadmap incompletely (G11 without "modelos heredados" and the
   numeric-compatibility output; G12 reduced to scale separation; G15 without "SVG y PNG" and
   dimensional tests, with an added "sheet sets").
4. `SP/ui/cedg-workspaces.md:140-143` still calls A7 an amendment candidate pending smoke,
   and its :186-188 defers "printing scales" to G12, although C implemented physical print
   sizing.
5. Approved behaviour still sits in specification text marked as unapproved (the C and E3
   amendments, retained as P-18).
6. PL:303 (A-2 row) still records the direct-entry enhancement "for F2", against `DQ-G-6`
   (G11).
7. Possible, not verified: RM and the G9U1 specification say ZoomWindow is reachable by
   keyboard; the A7 matrix records the direct chord as missing (A7 added factor actions).

## 7. Evidence gaps

- No normative layer specification and no normative `ExportArea` specification exist under
  `geocedg/specs`; the contracts rest on author-decision and closeout records and code.
- A7 has no dedicated closeout record and no stated approval date.
- No design for PDF layer structure, layer roles, "extended scale profiles", "sheet sets" or
  byte-level PDF/SVG/PNG reproducibility.
- G11's output "organización técnica comparable a herramientas de dibujo profesional" has no
  evidence either way.

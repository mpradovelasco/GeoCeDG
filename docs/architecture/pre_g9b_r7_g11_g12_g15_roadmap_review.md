# PRE-G9B-R7 — G11, G12 and G15 roadmap-review package

```text
PACKAGE_KIND        = PROPOSED ROADMAP REVISION AND DECISION MATRIX (PRE-G9B-R7, deliverable D)
AUTHORITY           = author instruction "GeoCeDG — PRE-G9B-R7 Integrated PRE-G9B Readiness,
                      Technical-Debt Reconciliation, Roadmap Revision and Final Verification"
                      (2026-10-15), §9-§10
STARTING EVIDENCE   = G11/G12/G15 roadmap scope reconciliation of 2026-10-15 (REC)
RECHECK BASE        = b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513 (tree 3aef929b)
G11 / G12 / G15     = PENDING — REVISED FUTURE SCOPE (none PASS; implementation not authorized)
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15
PROPOSALS           = §6 (history); AUTHOR DECISIONS ADOPTED 2026-10-15 (§9)
ROADMAP CHANGE BY R7 = factual status and reference corrections only (§7); the "Trabajo"
                      and "Salida" lists of the three gates were revised at the R7
                      closeout by the adopted decisions (§9)
selfApproved        = false
```

This package turns the evidence of the
[G11/G12/G15 reconciliation](pre_g9b_r6_plus_g11_g12_g15_roadmap_reconciliation.md)
(`REC`) into a reviewable roadmap revision. `R7` rechecked every claim of `REC` against
the code, tests, specifications and closeouts at the base (§1). For each gate it
separates the approved bounded slices from the remaining authorized design scope,
proposes refinements and lists the decisions that only the author can take. A bounded
slice is never the gate. `G11`, `G12` and `G15` stay `PENDING`, their implementation
stays unauthorized, and no requirement is removed until the author approves a revised
scope.

Two distinctions govern the whole package:

```text
flat numeric layer 0..99 with one document-wide hidden set  !=  hierarchical layer model (G11)
IsoABorder (model-space dependent construction)             !=  DrawingSheet (paper-space entity, G15)
```

Abbreviations: `RM` the [roadmap](../roadmap/geocedg_roadmap.md); `PL` the
[mini-track plan](pre_g9b_r6_plus_minitrack_plan.md); `V/` `docs/validation/`; `SP/`
`geocedg/specs/`; `GD/` and `GDT/` GeoCeDG Desktop main and test sources
(`source/desktop/desktop/src/{main,test}/java/org/geocedg/desktop/`); `GC/` GeoCeDG shared
main sources (`source/shared/common/src/main/java/org/geocedg/common/`); `REG` the
[R6-plus structured register](../validation/pre_g9b_r6_plus_g_structured_debt_register.md).
Line numbers refer to the base.

## 1. Recheck of the starting evidence

Every source, test class and approving closeout cited by `REC` exists at the base, and
its factual claims hold. Every "not delivered" claim was confirmed by searching the
GeoCeDG shared and Desktop sources and `geocedg/specs` (no layer hierarchy, role, filter,
lock, named-state, named-view, zoom-history, `DrawingSheet`, title-block, paper-space or
viewport concept exists). The recheck qualifies `REC` in eight places; this package uses
the qualified classification.

| # | `REC` statement | R7 qualification |
|---|---|---|
| Q1 | G11 "estados guardables" `PARTIALLY IMPLEMENTED` (`REC`:42) | **not delivered, prerequisite delivered**: A-2 persists only the one current document-wide hidden set and explicitly excluded named layer states (V/pre_g9b_r6_plus_a2_author_decisions_record.md:103-106) |
| Q2 | G11 table omits explicit name and order, the initial role list (RM:1183) and the existence invariant (RM:1185) | added in §2.1 and §2.2: name not delivered; order only the inherited numeric order; the invariant holds for the flat model |
| Q3 | "no normative layer specification" (`REC`:137) | the working-layer action, the `0..99` domain and the `<geocedgHiddenLayers>` grammar with its fail-closed rules are normative in SP/ui/cedg-workspaces.md:483-500; what is missing is a dedicated layer specification |
| Q4 | G12 item 1 "fully implemented" (`REC`:63, :114) | **delivered, bounded to the Desktop primary Graphics view**: the `ZoomWindow` baseline comes from G9U1 (U1-Z), A7 added ratio preservation, Escape, menu and toolbar; secondary 2D views, the 3D camera and Web remain, and no `ZoomWindow` key chord exists (the only accelerator in GD/GeoCeDGActionRegistry.java is DXF's) |
| Q5 | A7 "no approval date stated" (`REC`:63) | true of the documents; the approval commit `5869f9b3` is dated 2026-09-13 in Git |
| Q6 | G12 scale separation (`REC`:66) | the roadmap lists four concepts (model coordinates, view transform, drawing scale, physical print scale); D0 defines **one** `drawingScale` = physical output / physical model, used by picture, print and LaTeX alike (SP/units/unit-system.md:588-598; GDT/PreG9BR6PlusCPhysicalExportTest:180), so drawing scale and print scale are one value today |
| Q7 | G15 `DrawingSheet` `PARTIALLY IMPLEMENTED` (`REC`:74) | **not delivered**; only a model-space frame exists (`IsoABorder`, which SP/sheets/iso-a-border.md:50-53 and PL:164-167 forbid extending into a sheet); split into "model-space frame: partial" and "title block, paper space: not delivered" |
| Q8 | G15 "not reproducible from the saved document" (`REC`:79); "no normative `ExportArea` specification" (`REC`:137) | partly: the border's captured `a:b` and unit persist (SP/sheets/iso-a-border.md:502); the area, the ISO link and `drawingScale` are session state. The producer model and link validity are normative in SP/sheets/iso-a-border.md §11; the B precedence rules live in the B decision record and in code |

Citation corrections: `REC`:43 cites `DxfExporter.java:184`, which is only the OFF flag
(the layer naming is in `GC/export/GeoElementGeometryExportAdapter.java:353` and
`SemanticCurveExportAdapter2D.java:663`); `REC`:76 should cite the
[F4-D author closeout](../validation/pre_g9b_r6_plus_f4_d_author_closeout_record.md) and the
E3 Desktop A3 MediaBox test; `REC`:78 attributes the A3 1:50 PDF/SVG/PNG checks to E3-R1,
but they are in `GDT/PreG9BR6PlusE3IsoABorderDesktopTest` (E3-R1 covers LaTeX and DXF);
`REC`:130 cites PL:303, now PL:304.

## 2. G11 — Capas y estados de vista

### 2.1 Delivered bounded slices (approved)

| Capability | Phase and approval | Evidence |
|---|---|---|
| numeric layer domain `0..99` (Classic stays `0..9`; clamping; `ShowLayer`/`HideLayer` unchanged) | A-2 `06a2ea6b` (`FINAL` `verification-814f89fa…`, smoke PASS) | `GC/main/settings/config/AppConfigGeoCeDG.java:43` (`L_MAX = 99`); `GC/kernel/layers/LayerDomain.java`; `PreG9BR6PlusA2LayerDomainTest` |
| explicit working layer (session state; Move-group one-shot tool; chooser; adopt an object's layer; status-bar segment) | A-1 `2a71133a` (smoke PASS); Open initialization in A-2 | `GD/GeoCeDGLayerWorkspace.java`; `PreG9BR6PlusA1LayerWorkspaceTest` |
| flat hide/show, `effectiveVisible = objectVisible AND NOT layerHidden`, in painting and hit testing (Graphics 1 and 2 alike) | A-1 (session), A-2 (document) | `PreG9BR6PlusA1HiddenLayerTest:78,99,130,253` |
| persistence of the one document-wide hidden set (`<geocedgHiddenLayers version="1">`, fail-closed reading, not an undo step) | A-2 | `GC/kernel/layers/HiddenLayersXml.java`; `PreG9BR6PlusA2HiddenLayerPersistenceTest`; normative grammar SP/ui/cedg-workspaces.md:483-500 |
| numeric ordering and status presentation (Sort by Layer in numeric group order with a per-group eye) | A-1 | `GD/GeoCeDGAlgebraView.java`; `PreG9BR6PlusA1LayerWorkspaceTest:490` |
| bounded export integration: DXF layer `0`→`0`, `n`→`GEOCEDG_L<n>`, hidden layers written OFF; hidden layers omitted from picture, PDF, SVG, print and LaTeX | C `6248bf0f` (picture route since B/A-1) | V/pre_g9b_r6_plus_c_closeout_record.md:39-46; `PreG9BR6PlusCDxfDesktopTest:219`; `GD/export/PictureExportService.java` |
| layer membership changes neither existence nor validity (RM:1185), for the flat model | A-2 | A-2 closeout; `PreG9BR6PlusA2HiddenLayerPersistenceTest:724` |

### 2.2 Remaining authorized design scope (preserved)

Hierarchy (groups and sublayers) with explicit name and order; layer roles, the initial
role list (RM:1183) and styles by role; selection and edit locking; filters by role and
layer; visibility per view (Graphics 1, Graphics 2, 3D); named and saved layer and view
states with the `print`/`export`/`construction` flags; hierarchical export (DXF layer
tables, PDF layer structure) and legacy mapping; compatibility with the numeric GeoGebra
layer; the existence / geometric visibility / interface visibility separation for the
hierarchical model; and the output "organización técnica comparable a herramientas de
dibujo profesional", for which no evidence exists yet.

### 2.3 Proposed scope refinements (pending author decision)

- **G11-P1** Record the flat numeric slice as the delivered base and reword each remaining
  item relative to it (wording in §5). No item is removed.
- **G11-P2** Reduce "integración con DXF, PDF y modelos heredados" to the hierarchical
  mapping; the flat mapping is done (C).
- **G11-P3** Turn the output "compatibilidad con capas numéricas" into "preservar" it when
  the hierarchy is introduced.
- **G11-P4** Fold the decided owners into the gate: B-2 (does a tool replacement keep the
  session working layer?), P-2 `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`, P-5 (3D, per-view),
  P-6 (locking and filters), P-8 (background images).

### 2.4 Dependencies and open decisions

A dedicated layer specification and ADR (model, serialization, migration) are prerequisites
of implementation; per-view states need a persistence class (A-2 forbade per-view
persistent state); `FitLayer` couples G11 and G12; per-view visibility couples G11 with
G14 (P-5, L-8); the existence / geometric visibility split couples G11 with G13; sheet
layers couple G11 with G15.

## 3. G12 — Navegación, zoom y escalas extendidas

### 3.1 Delivered bounded slices (approved)

| Capability | Phase and approval | Evidence |
|---|---|---|
| cursor-centred zoom (wheel, pinch, tools) with cursor currentness and a true-centre keyboard fallback | POST-G9U1-A7 (final candidate `ed3f19a0`, `PHASE` `verification-041cd859…` 91 tests, author smoke, approval commit `5869f9b3`) | [A7 design](post_g9u1_a7_navigation_design.md); `PostG9U1A7CursorZoomTest`; `PostG9U1A7NavigationTest:72` |
| explicit `ZoomWindow` preserving the X/Y relation, Escape, menu and toolbar — Desktop primary Graphics view | G9U1 baseline (U1-Z) + A7 | SP/ui/g9u1-construction-interaction.md:458-463; A7 matrix |
| configurable zoom-by-factor actions (common factor, two independent shortcuts) | A7 | `GD/GeoCeDGNavigationShortcutPreferences`; `PostG9U1A7NavigationTest:132,165,224` |
| refusal of printable and AltGr chords for the factor actions | POST-E2-P2-R1 `38647c79` | V/post_e2_p2_r1_closeout_record.md; SP/ui/cedg-workspaces.md:190-207; `PostE2P2R1ShortcutValidationTest` |
| document units: construction unit, presentation unit, `usm`; normative semantics and persistence | D0 `8383a153` (ADR 0032, SP/units/unit-system.md) and D1 `6e1d8459` (`FINAL` `verification-9cf1a0af…`) | unit-system.md; D1 closeout |
| drawing scale `a:b` (session) and physical export scale `L·fb(c)·a/b` for picture, print and LaTeX; DXF `$INSUNITS`; physical PDF strokes and markers | C `6248bf0f` (DQ-C7, DQ-C8); POST-E2-P1-R2 `6dde194e` (ADR 0035) | `GC/export/PhysicalExportScale.java`; `GD/export/DrawingScale.java`; `PreG9BR6PlusCPhysicalExportTest:116,180,203`; `PostE2P1R2PhysicalPdfStyleTest` |
| zoom, DPI and viewport are never metric authority (geometric metrics) | unit-system §6; A7 | `PostG9U1A7NavigationTest:261`; `PreG9BR6PlusCPhysicalExportTest:203` |

Distinctions kept explicit:

```text
cursor-centred zoom  !=  ZoomWindow  !=  zoom-by-factor actions        (three navigation gestures)
construction unit    !=  presentation unit                             (document metadata, D0/D1)
drawing scale a:b    ==  physical export scale today (one value, session state)  -> decision G12-D2
```

### 3.2 Remaining authorized design scope (preserved)

Named views keeping centre and scale per view; zoom history (`ZoomPrevious`) with
`FitSelection` and `FitLayer`; `ZoomWindow` and navigation in secondary 2D views, the 3D
camera and Web; precise keyboard navigation; very large and very small scale profiles
(undefined: PL:161); a systematic extreme-scale verification of metrics, intersections and
certificates; the view side of the model / view / drawing / print separation, including
the physical zoom aid (D-10); independence of text and non-PDF style sizes from zoom; and
the output "navegación precisa y reproducible".

### 3.3 Proposed scope refinements (pending author decision)

- **G12-P1** Mark the first work item ("zoom centrado en cursor, ZoomWindow explícito y
  zoom por factor") as delivered for the Desktop primary view by A7 and POST-E2-P2-R1 and
  keep only the secondary-view, 3D and Web residue. Do not reimplement it.
- **G12-P2** Point the model / view / drawing / print separation to unit-system.md as the
  normative authority for units and physical scale and keep only the view-side obligations.
- **G12-P3** Define or drop "perfiles de escala extendidos".
- **G12-P4** Fold D-10 (physical zoom aid) into the gate.

### 3.4 Dependencies and open decisions

`FitLayer` needs G11 semantics; named views and per-view centre/scale meet G15 viewports and
sheet scale; physical text size (`TD-PDF-1`) meets the G12 output "ninguna métrica depende
de pantalla, zoom o DPI"; secondary-view parity meets P-7 / L-29 (Graphics View 2 has no
GeoCeDG modes).

## 4. G15 — Hojas, PDF y formatos gráficos

### 4.1 Delivered bounded slices (approved)

| Capability | Phase and approval | Evidence |
|---|---|---|
| explicit `ExportArea` as the single session authority (explicit producer > `Export_1`/`Export_2` > visible view; producers MANUAL, EXPORT_POINTS, ISO_A_SELECTION, ISO_A_BORDER; offscreen rendering) | B `848206c4` (AQ-X1/AQ-X2, DQ-B1 to DQ-B9); E3 (§11 of iso-a-border.md) | `GD/export/ExportAreaSession.java`; `PreG9BR6PlusBExportSurfaceTest:134,212,265` |
| physical graphic export (picture, print, LaTeX at `drawingScale`) | C `6248bf0f` | `PreG9BR6PlusCPhysicalExportTest` |
| PDF, SVG, PNG, EMF/EMF+ with exact area: PDF page of the exact area (opt-in `setExactPageSize`, zero margins, no fit), SVG exact fractional `viewBox`, PNG exact raster at the chosen DPI, EMF `rclFrame` | B (DQ-B8), C | `PreG9BR6PlusBPictureFidelityTest:204,254,294`; `PreG9BR6PlusCPhysicalExportTest:116,151` |
| physical PDF stroke widths and markers | POST-E2-P1-R2 `6dde194e` (ADR 0035; SP/export/physical-pdf-style-sizes.md) | `PostE2P1R2PhysicalPdfStyleTest:97,140,192,370` |
| Type 1 font embedding in PDF (text as shapes or embedded fonts) | F4-D `6214d15a` | [F4-D closeout](../validation/pre_g9b_r6_plus_f4_d_author_closeout_record.md); `PreG9BR6PlusF4DEmbeddedFontPdfTest`; `PreG9BR6PlusF4DType1OutlineTest` |
| ISO A model-space border: `IsoABorder` A0–A10, landscape, optional inner frame and label, captured scale and unit; export area from the sheet | E3 / E3-R1 `7cd50116` (ADR 0036; SP/sheets/iso-a-border.md) | `GC/kernel/sheet/AlgoIsoABorder.java`; `PreG9BR6PlusE3IsoABorderTest`; `PreG9BR6PlusE3IsoABorderDesktopTest:133,767-800` (A3 1:50 → 420 × 297 mm MediaBox) |
| dimensional export tests (MediaBox, per-format physical sizes, stroke widths, compiled LaTeX page) | B, C, POST-E2-P1-R2, E3, E3-R1 | the tests above; `PreG9BR6PlusE3R1ExportInteroperabilityTest:186,213,460` |

```text
IsoABorder != DrawingSheet
```

`IsoABorder` is an ordinary dependent construction in **model space**. It is not a
paper-space document-sheet entity, has no title block, no sheet set and no viewport, and
its specification forbids extending it into one (SP/sheets/iso-a-border.md:50-53).

### 4.2 Remaining authorized design scope (preserved)

`DrawingSheet` in paper space (physical size and orientation, frame and title block as a
document service, units and scale per sheet, `Viewport[]` with their own scale, clip and
layers, print styles, included layers, publication metadata) and sheet collections;
sheet-level publication reproducible from the document rather than from session state;
the outstanding physical text-size contract (`TD-PDF-1`, after a physical-presentation
contract-design activity); the remaining PDF/FreeHEP observations (`OBS-F4P-FREEHEP-XREF-SIZE`,
L-6, L-19, O-22); physical style sizes outside PDF; sheet-level SVG and PNG; sheet-level
export and dimensional tests (frame, title block, margins, viewport scale, text).

### 4.3 `ENH-B-MANUAL-EXPORT-AREA-DRAG` — preserved explicitly

```text
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15
```

- **Origin:** the B author smoke, recorded in the
  [B closeout](../validation/pre_g9b_r6_plus_b_closeout_record.md):116 ("Let the `MANUAL`
  producer also be defined by an interactive rectangle gesture similar to `ZoomWindow`,
  besides entering coordinates") with the contract `interactive rectangle gesture → world
  bounds → ExportArea(MANUAL)`; owner G15 by `DQ-G-6`; register row P-3.
- **Intended UX:** an interactive rectangular gesture activated from the existing
  export-area coordinate dialog (**File → Export area → Define export area…**). The four
  coordinate fields remain available and editable; the gesture fills them and updates the
  same `ExportArea(MANUAL)`.
- **Invariants:** no second export-area authority; the drawn rectangle is never itself the
  area (a selection rectangle is never an export area); viewport and screen transformations
  only convert the gesture to world bounds and never become authoritative geometry.
- **State:** not implemented — `GD/GeoCeDGExportAreaPrompt.java` is a dialog with four
  fields and "use visible view" ("DQ-B4: a dialog, no drag gesture"). No implementation is
  authorized by R7.

### 4.4 Proposed scope refinements (pending author decision)

- **G15-P1** Replace the separate items "PDF vectorial con tamaño exacto" and "SVG y PNG"
  by "publicación de hoja", which reuses the delivered exact page, physical widths and
  embedded fonts; keep `DrawingSheet`, viewports, title blocks, sheet collections,
  `TD-PDF-1` and the FreeHEP debts.
- **G15-P2** State that `IsoABorder` stays a model-space annotation and is not extended into
  a sheet; the coexistence of both needs an ADR.
- **G15-P3** Add `ENH-B-MANUAL-EXPORT-AREA-DRAG` to the viewport and area work of the gate.
- **G15-P4** Fold the decided and proposed owners: P-3, D-8 (decided); O-4, O-10, O-22,
  O-25 and the L-7 tracking (proposed, see the
  [debt reconciliation](../validation/pre_g9b_r7_debt_reconciliation.md) §5.2).
- **G15-P5** Define or drop "sheet sets" (PL:162).

### 4.5 Dependencies and open decisions

Persistence of sheet, area, ISO link and scale (today session state); the paper-space ADR
and its coexistence with `IsoABorder`; the `TD-PDF-1` contract; G11 for included layers;
G12 for viewport scale and named views. DXF stays in G5 as geometric export (RM:1250).

## 5. Proposed roadmap wording

To replace the "Trabajo" and "Salida" blocks of the three gate sections **only after the
author approves it** (§6). R7 does not apply it.

**G11 — Trabajo (propuesto)**

- jerarquía de capas (grupos, subcapas, nombre y orden explícitos) construida sobre la capa
  numérica plana `0..99` de `PRE-G9B-R6-plus-A-1/A-2`, que no es una jerarquía;
- roles de capa con la lista inicial de §14.1 y estilos por rol;
- bloqueo de selección y edición por capa (incluida `OBS-A1-NON-HIT-SELECTION`) y filtros
  por rol y por capa; la ordenación `Sort by Layer` y la ocultación plana no son filtros;
- visibilidad de capa por vista (Gráficos 1, Gráficos 2 y 3D) sobre el conjunto oculto
  documental único de `A-2`;
- estados de capa y de vista con nombre y guardables, con indicadores `print`/`export`/
  `construction`, que amplían la persistencia del conjunto oculto entregada por `A-2`;
- proyección de la jerarquía a DXF, PDF/SVG y documentos heredados, partiendo de la
  correspondencia plana aprobada en `C`;
- deudas de la puerta: B-2, P-2, P-5, P-6, P-8.

**G11 — Salida (propuesta):** organización técnica comparable a herramientas de dibujo
profesional; preservar la compatibilidad con la capa numérica de GeoGebra al introducir la
jerarquía.

**G12 — Trabajo (propuesto)**

- cerrado para la vista Gráficos principal de Desktop por `POST-G9U1-A7` y
  `POST-E2-P2-R1`: zoom centrado en cursor, `ZoomWindow` explícito y zoom por factor con
  teclas configurables; queda su extensión a vistas 2D secundarias, cámara 3D y Web;
- vistas nombradas que conserven centro y escala por vista, con su clase de persistencia
  por decidir, e historial de zoom (`ZoomPrevious`, `FitSelection`, `FitLayer`);
- navegación precisa por teclado;
- completar en el lado de la vista la separación modelo/vista/dibujo/impresión, cuyo modelo
  de unidades y escala física ya es normativo (`unit-system.md`; `D0`/`D1`/`C`), incluida
  la ayuda de zoom físico (D-10);
- batería sistemática de escalas extremas que demuestre la invariancia de métricas,
  intersecciones y certificados, con perfiles de escala definidos.

**G12 — Salida (propuesta):** navegación precisa y reproducible; ninguna métrica geométrica
depende de pantalla, zoom o DPI, y la independencia de textos y estilos fuera del PDF
físico queda contratada.

**G15 — Trabajo (propuesto)**

- `DrawingSheet` en espacio papel con marco y cajetín como servicio de documento y
  colecciones de hojas; `IsoABorder` (`E3`) es una anotación de espacio modelo y no se
  amplía a hoja;
- viewports de espacio papel con escala física propia sobre la autoridad única `ExportArea`
  (`B`/`E3`) y la escala física de `C`, incluida la definición interactiva del área manual
  (`ENH-B-MANUAL-EXPORT-AREA-DRAG`);
- publicación de hoja (PDF de tamaño exacto, SVG y PNG de alta resolución con tamaño
  físico) reutilizando la página exacta, los anchos físicos y las fuentes incrustadas ya
  entregados, y resolviendo `TD-PDF-1` tras su diseño de contrato;
- pruebas dimensionales de hoja, ampliando las existentes de `B`, `C`, `E3`, `E3-R1` y
  `POST-E2-P1-R2`.

**G15 — Salida (propuesta):** publicación reproducible desde el documento —no solo desde el
estado de sesión— que respeta marco, tamaño físico, escala y capas.

## 6. Author decision matrix

| Decision | Question | Options | R7 recommendation | If not decided |
|---|---|---|---|---|
| DEC-R7-G1 | Adopt the "delivered bounded slices" blocks of §2.1, §3.1 and §4.1 in the roadmap? | adopt / amend | adopt (factual) | R7's roadmap pointers stay as written (§7) |
| DEC-R7-G2 | Adopt the proposed "Trabajo"/"Salida" wording of §5 for G11, G12 and G15? | adopt / amend / keep current lists | adopt; it removes no requirement and drops only delivered work | current lists stay authoritative |
| DEC-R7-G3 | G12 first item: closed for the Desktop primary view, residue kept (G12-P1)? | yes / no | yes | item stays open as written |
| DEC-R7-G4 | Is one `drawingScale` an acceptable reading of "escala de dibujo != escala física de impresión", or must G12/G15 design two scales? | one value / two values | decide with G15 viewport design; keep both concepts open until then | open |
| DEC-R7-G5 | Should `drawingScale`, `ExportArea` and the ISO link become document state for reproducible publication? | session / document | decide in the G15 design, with an ADR | session state stays |
| DEC-R7-G6 | Owner of `FitLayer` | G11 / G12 | G12 (navigation), consuming G11 semantics | open |
| DEC-R7-G7 | "perfiles de escala extendidos" and "sheet sets": define or drop | define / drop | define at the gate's design start; do not drop | undefined |
| DEC-R7-G8 | Fold the decided owners (B-2, P-2, P-5, P-6, P-8 → G11; D-10 → G12; P-3, D-8 → G15) and the proposed G15 owners (O-4, O-10, O-22, O-25, L-7) into the gate sections? | yes / no | yes | owners stay in the register only |
| DEC-R7-G9 | Reconcile the stale A7 and printing-scale wording in SP/ui/cedg-workspaces.md:140-143 and :186-188 (same pattern as P-18)? | separate authorized documentary task / retain | separate task, with P-18 | retained |

No decision above authorizes G11, G12 or G15 implementation, `ENH-B-MANUAL-EXPORT-AREA-DRAG`,
or any phase promotion.

## 7. Factual corrections applied by R7

Applied to the living roadmap, as authorized ("factual corrections of outdated
current-status statements and references"):

1. The three §15 panorama rows of G11, G12 and G15, truncated by the G closeout commit at an
   unterminated link, are completed with their links, the status
   `PENDING — ROADMAP SCOPE REVIEW` and a pointer to this package.
2. The consolidated row "G11–G16 `NOT STARTED`" is split: G11, G12 and G15
   `PENDING — ROADMAP SCOPE REVIEW` with delivered bounded slices; G13, G14 and G16
   `NOT STARTED`.
3. The status lines of the three gate sections read `PENDING — ROADMAP SCOPE REVIEW`, and
   each section gains a dated R7 paragraph listing its delivered bounded slices by phase and
   pointing to this package. The "Trabajo" and "Salida" lists are unchanged.

## 8. Dependencies and non-promotion

The roadmap records no explicit dependency edges among G11–G16; the couplings above are
implied by shared concepts: G11 → G12 (`FitLayer`), G11 ↔ G13 (layer hiding is not geometric
visibility), G11 ↔ G14 (per-view and 3D visibility), G11 → G15 (sheet layers), G12 ↔ G15
(views, viewports, scale, `TD-PDF-1`). None of the three gates is a hard dependency of
`G9B`, and the mini-track rule that slices are delivered "without moving their ownership"
(PL §1.1) still holds. `G11`, `G12` and `G15` are `PENDING — ROADMAP SCOPE REVIEW`; none is
`PASS`; none is authorized. Nothing in this package promotes a phase or authorizes
implementation.

## 9. Author decisions (2026-10-15)

The author instruction "GeoCeDG — PRE-G9B-R7 FINAL AUTHOR APPROVAL, DECISION ADOPTION AND
DOCUMENTARY CLOSEOUT" adopted the final decision review
([R7 closeout record](../validation/pre_g9b_r7_closeout_record.md) §4). These decisions prevail
over the proposals of §5 and §6 where they differ; the roadmap gate sections carry the adopted
wording.

| Decision | Author disposition |
|---|---|
| DEC-R7-G1 | approved |
| DEC-R7-G2 | approved with modifications: G15 states the `ENH-B-MANUAL-EXPORT-AREA-DRAG` UX — an explicit button or control in the existing export-area coordinate dialog starts a rectangular pointer gesture that updates the same `ExportArea(MANUAL)`; the numeric fields stay available and editable; no second export-area authority; `OPEN — ENHANCEMENT — NOT IMPLEMENTED — OWNER G15`. G12 distinguishes model units, view transformations, drawing scale, physical output scale and future paper-space viewport scale; geometric model quantities never depend on zoom, DPI, camera or viewport presentation transforms; no new scale authority is introduced |
| DEC-R7-G3 | approved within its tested scope: A7 complete for the primary Desktop Graphics View only |
| DEC-R7-G4 | retained as a future normative design decision (scale independence contract) |
| DEC-R7-G5 | retained as a future normative design decision (G15 design with an ADR); session semantics unchanged |
| DEC-R7-G6 | approved: `FitLayer` owned by G12, consuming future G11 semantics |
| DEC-R7-G7 | approved: extended scale profiles and sheet sets are kept; contracts in future design |
| DEC-R7-G8 | approved: planning owners integrated in the gate sections |
| DEC-R7-G9 | approved as future separate normative maintenance of `cedg-workspaces.md`; no amendment in the closeout |

`G11`, `G12` and `G15` are `PENDING — REVISED FUTURE SCOPE`; their implementation is not
authorized.

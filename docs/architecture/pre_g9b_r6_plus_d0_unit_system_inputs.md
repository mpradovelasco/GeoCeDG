# PRE-G9B-R6-plus-D0 — unit-system inputs from P0

- Status: **PROPOSED — INPUT TO D0 — NOT NORMATIVE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Scope: the complete unit inventory, the persistence recommendation and the
  compatibility evidence that `D0` needs to write the unit ADR and normative
  specification. `P0` does **not** pre-empt `D0`'s normative decisions.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  findings 9–13

Path abbreviations as in the P0 report: `common/` =
`source/shared/common/src/main/java/org/geogebra/common/`, `geocedg-common/` =
`source/shared/common/src/main/java/org/geocedg/common/`, `desktop/` =
`source/desktop/desktop/src/main/java/org/geogebra/desktop/`, `geocedg-desktop/` =
`source/desktop/desktop/src/main/java/org/geocedg/desktop/`.

## 1. Author-fixed semantics (constraints, restated only for traceability)

The governing text is the [mini-track plan](pre_g9b_r6_plus_minitrack_plan.md)
§4. In short:

- `constructionUnit` is the physical meaning of one model unit
  (`DOCUMENT_SEMANTIC`). Changing it rescales nothing and changes no
  coordinate, dependency, identity or parameter domain.
- A legacy document without unit metadata is `UNSPECIFIED_MODEL_UNIT`;
  centimetres are never inferred.
- `presentationUnit` expresses dimensional and export quantities;
  `drawingScale` is dimensionless; physical output size is invariant under an
  equivalent presentation-unit change (`AQ-U1`, resolved).

## 2. Complete unit inventory at the base

| Item | Evidence | Class |
|---|---|---|
| `printingScale` (cm on paper per model unit) | `common/euclidian/EuclidianView.java:311`, `:1930-1934` (`10^-round(log10(PRINTER_PIXEL_PER_CM / xscale))`), `:3049-3059`; never serialized | physical output unit, session, **zoom-derived** |
| Print/picture "Scale in cm" | `desktop/export/PrintScalePanel.java:118`, `:140-145`, `:285-289`; `desktop/export/GraphicExportDialog.java:540-610` | physical output unit |
| PNG/SVG/PDF/EMF device units | DPI list 72–600, default 300 (`GraphicExportDialog.java:257-259`); PDF points (`:1003-1010`); SVG px (`desktop/export/SVGExtensions.java:84-97`) | device unit |
| LaTeX `xunit`/`yunit` (cm per model unit, default 1) | `common/export/pstricks/GeoGebraExport.java:241-242`; `GeoGebraToPstricks.java:1656-1661`; `GeoGebraToPgf.java:149-153` | physical output unit, independent of `printingScale` |
| DXF `Unit.UNITLESS`, `$INSUNITS = 0` | `geocedg-common/export/GeometryExportModel.java:28-31`, `:564-565`; `DxfExporter.java:108-110` | construction-semantic: explicitly no physical unit |
| STL `EDGE_FOR_PRINT = 40` ("4 cm"), `STLscale` ("1 unit = 10 mm"), `STLthickness` | `common/geogebra3D/euclidian3D/EuclidianView3DForExport.java:49-55`, `:222-262` | physical output unit, document-object override; excluded format |
| Collada `<unit name="centimeter" meter="0.01"/>` | `common/geogebra3D/euclidian3D/printer3D/FormatCollada.java:76` | physical output unit; excluded format |
| Axis `unitLabel` (`""`, °, π, mm, cm, m, km, $) | written `common/main/settings/EuclidianSettings.java:1197`; options `common/gui/dialog/options/model/AxisModel.java:113-121`; tick suffix `common/euclidian/DrawAxis.java:572-573` | presentation label per view; never read by geometry (π changes tick spacing) |
| `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT` | `geocedg-common/kernel/locus/metric/MetricUnit2D.java:9-10` | construction-semantic abstract model length |
| intersection residual unit `"model-coordinate"` | `geocedg-common/kernel/locus/intersection/LocusIntersectionPolicy2D.java:78-79` | construction-semantic model length |
| spatial frame `units` token | `geocedg-common/kernel/spatial/identity/ProjectionFrameRecord.java:81`, `:138-140`; compared by string equality only | construction-semantic, opaque |
| `angleUnit` | `common/kernel/Kernel.java:168`, `:2345-2429`; parsing `common/kernel/commands/AlgebraProcessor.java:2630-2636`; rounding `common/kernel/arithmetic/MyDouble.java:596-666` | presentation **and** input parsing **and** rounding (not a pure presentation precedent) |
| `Distance`, `Length`, `Area` values | `common/kernel/algos/AlgoDistancePoints.java:96-98`; `common/kernel/geos/GeoNumeric.java:662-711` | dimensionless; no suffix anywhere |
| AR unit (`cm`/`inch`) | `common/geogebra3D/euclidian3D/EuclidianView3D.java:341` | device unit; out of scope |

**Conclusion:** no geometric computation reads a physical unit today. Every
`cm` is output-oriented, a free-text label, or excluded 3D output (PROVEN FROM
SOURCE). At standard zoom (50 px/unit) `printingScale = 1`, the only upstream
"1 unit = 1 cm" convention, and it is zoom-derived (probe P2:
`printingScale=1.0` at xscale 50). It must never be read as a model unit.

## 3. Persistence owner for `constructionUnit` (finding 11)

**Recommended:** a flat, versioned, self-closing GeoCeDG element as a child of
`<construction>`, written by `Construction.getConstructionXML`
(`common/kernel/Construction.java:1517-1550`) next to `<geocedgSpatial>`, for
example `<geocedgUnits version="1" construction="mm" presentation="cm"/>`
(token names are a `D0` decision).

Rules the evidence forces:

1. **Lazy.** Written only when the user has set a unit. Absent means
   `UNSPECIFIED_MODEL_UNIT`, so legacy documents round-trip unchanged.
2. **Reset in `clearConstruction`, not on parse.** `handleConstruction` also
   runs for the clipboard wrapper, which wraps every paste in a bare
   `<construction>` (`common/util/InternalClipboard.java:624-634`;
   `common/io/MyXMLHandler.java:695-697`, `:2573-2595`), and for macro
   constructions (`:750-752`). Applying "absent → unspecified" while parsing
   `<construction>` would reset the target document's unit on every paste.
3. **Macro guard.** `Macro.getXML` emits `getConstructionXML` for every macro
   (`common/kernel/Macro.java:175`, `:792-796`). The writer must skip macro
   constructions and the reader must ignore the element inside them, on the
   existing `!(kernel instanceof MacroKernel)` precedent
   (`MyXMLHandler.java:2575`).
4. **Flat.** Unknown elements are not skipped as a subtree. Their nested
   children are processed as siblings in the parent's mode
   (`MyXMLHandler.java:3068-3094`, `:3135`). An element with children would be
   misread by older builds.
5. **Undo.** `<construction>` is part of the undo snapshot
   (`common/main/undo/UndoManager.java:285`; `common/io/MyXMLio.java:138-166`),
   so a unit change is undoable unless `D0` decides otherwise.

Rejected owners:

- `<kernel>`: not reset per load (`Kernel.java:3061-3082`); also written into
  the preferences XML (`MyXMLio.java:346-359`; `common/main/App.java:1816-1842`)
  and re-applied at File → New (`desktop/main/AppD.java:1172-1173`).
- A `<construction>` attribute: unversioned; dropped on re-save
  (`MyXMLHandler.java:2580-2582`; `Construction.java:1522-1524`); also emitted
  for macros.
- `<euclidianView>`: per view and presentation.
- Inside `<geocedgSpatial>`: it would gain fail-closed behavior in older
  GeoCeDG builds (`geocedg-common/kernel/spatial/identity/SpatialRecordXmlCodec.java:255`; `MyXMLio.java:435-449`) but
  mix unit semantics into the spatial identity section, which is written only
  when the registry is non-empty.

**Older-build behavior (`AQ-U3`).** An older GeoCeDG build logs
"unknown tag in `<construction>`" (`MyXMLHandler.java:3092-3094`), loads the
rest, and re-saves without the element: lenient, with silent semantic loss.
Classic GeoGebra behaves the same. Fail-closed is possible only by carrying a
marker that older builds already reject (the `<geocedgSpatial>` route). `D0`
decides; P0 found **no compatibility blocker** for `DOCUMENT_SEMANTIC`.

## 4. Owner and default for `presentationUnit` (finding 12)

- **Recommended class:** `DOCUMENT_PRESENTATION`, stored in the same element.
  Its default for **new** documents comes from a `USER_PREFERENCE` in the
  portable GeoCeDG properties store, on the `geocedg.<area>.<name>.v1` key
  precedent (`geocedg-desktop/GeoCeDGPresentationPreferences.java:23-29`).
  The default never applies to a loaded document.
- **Reachability:** native dimension **texts** are kernel objects updated in
  the dependency graph, so the conversion must be readable when a dimension
  text is formatted, like the output half of `angleUnit`. A unit change must
  trigger an update of every dimension text. It must **not** copy `angleUnit`'s
  parsing and rounding effects (`AlgebraProcessor.java:2630-2636`;
  `MyDouble.java:596-666`).
- **Absent value (`D0`):** equal to `constructionUnit` when that is physical;
  inert, with bare numbers, when `constructionUnit` is unspecified.

## 4a. Unit-independence invariant (consequence of the author-fixed §4.1)

P0 recommends that `D0` state this invariant normatively:

```text
No geometric output, numeric value, dependency or parameter domain may depend
live on constructionUnit, presentationUnit or an export drawingScale.
Only presentation strings (dimension texts, status-bar segments) and
export adapters read them.
```

- A **tool** may read the current unit or a drawing scale **once, at
  creation**, to choose explicit model-unit inputs. Examples: the `E2`
  overshoot and gap, and the `E3` model-units-per-millimetre factor. Those
  inputs are ordinary construction parameters. A later unit change is
  therefore a reinterpretation, never a recompute.
- A dimension's numeric output is the **model-unit** measure; only its text
  converts. Otherwise a construction that uses the number would change with
  `presentationUnit`, which `AQ-U1` forbids.
- Without this invariant, `IsoABorder` and dimension decorations would rescale
  on a `constructionUnit` change, contrary to plan §4.1 (independent fidelity
  review of the P0 candidate). `E3`'s resulting consequence is the author
  decision `AQ-E3c`.

## 5. `UNSPECIFIED_MODEL_UNIT` compatibility (finding 13)

| Case | Evidence | Behavior |
|---|---|---|
| legacy `.cedg` | same ZIP/XML as `.ggb` (`geocedg/specs/ui/native-document-identity.md:14-17`, `:77-85`) | no element, so `UNSPECIFIED_MODEL_UNIT`; nothing written until the user sets a unit |
| Classic `.ggb` (input only) | `geocedg-desktop/AppGeoCeDG.java:438-443`; `GeoCeDGDocumentPolicy.java:19-36` | same; saved only as `.cedg` |
| older GeoCeDG reading a new document | `MyXMLHandler.java:3092-3094` | unit dropped; lenient (`AQ-U3`) |
| copy/paste between documents | clipboard carries element XML only (`InternalClipboard.java:137-175`); buffers are JVM-static in common and one `CopyPasteD` per `AppD` in Desktop (`desktop/main/AppD.java:354`, `:4988-4992`; `desktop/util/CopyPasteD.java:57-70`); the clipboard survives File → New and Open in one window (no clear call; `CopyPasteD.java:608-616`); two windows do not share it | numbers move, not lengths; `D0` decides warn, refuse or accept. P0 recommends: accept, with a non-blocking notice when both units are physical and differ. Never convert on paste, which would be geometric scaling |
| exporters | DXF `UNITLESS` until `C`/`D1`; picture and LaTeX keep their output scales | unchanged until `C` |

No blocker found. Evidence labels:

- **PROVEN FROM SOURCE:** the parser and writer facts in each row (unknown
  tags logged and skipped; no unit read or written today; clipboard content and
  lifetime).
- **Design:** the behavior column (absent means `UNSPECIFIED_MODEL_UNIT`;
  nothing written until set) is the author-fixed rule plus the P0 design. It
  is not current behavior, because no unit exists yet.
- **INFERRED:** that two windows do not share a clipboard; a two-window smoke
  test would settle it.

## 6. Export-foundation amendment `D0` must state

The accepted [geometry export foundation](../../geocedg/specs/export/geometry-export-foundation.md)
fixes `UNITLESS` (`:39`, `:72-75`, `:118`) and requires "an explicit
document/application contract" that "must not reinterpret screen scale as model
scale" (`:77-78`). `D0` is that contract. Candidate amendment: DXF `$INSUNITS`
from `constructionUnit`, with unchanged model coordinates; `0` while
unspecified. The DXF staleness fingerprint
(`geocedg-common/export/G9X1GeometryExportAdapter.java:1049-1060`) must then
include the unit (`C`/`D1`).

## 7. Physical export consequences for `C`

With physical `constructionUnit` `c`, `presentationUnit` `p` and scale `1:n`, a
model length `L` prints as `L·k(c→mm)/n` millimetres, whatever `p` is (`AQ-U1`).
Picture export therefore derives its paper scale from `drawingScale` and
`constructionUnit` instead of the zoom-derived `printingScale`. LaTeX
`xunit = k(c→cm)/n` cm per model unit. With an unspecified unit,
engineering-scale export is unavailable, requires a declared unit, or uses an
explicitly labelled legacy mapping (`AQ-U6`, `D0`).

## 8. Open questions for `D0`

`AQ-U2` (which results present units; P0 recommends native dimensions first),
`AQ-U3` (unit set, tokens, lenient or fail-closed), `AQ-U4` (cross-document
paste), `AQ-U5` (`drawingScale` persistence for picture and LaTeX export; P0
recommends `SESSION`, default `1:1`), `AQ-U6` (behavior with an unspecified
unit). Whether a unit change is undoable is also a `D0` decision.

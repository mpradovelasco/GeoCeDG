# G5 native 2D geometry and DXF export report

- Phase: G5 — native 2D geometry export foundation and DXF export
- Branch: `feature/g5-dxf-export`
- Date: 2026-08-10
- Baseline: GeoGebra 5.4.928.0,
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- Feature: `cedg.export.dxf.2d`
- Maturity: `experimental`
- Final state: **PASS**

## Scope and stop-condition audit

G5 implements a read-only 2D export boundary, deterministic DXF encoder,
GeoCeDG-only Desktop workflow, semantic regression corpus and subordinate
verification gate. It does not change the definition or dynamic evaluation of
any geometric object.

The implementation required none of the stop conditions named for G5:

- no fundamental geometric semantics changed;
- `Locus` was not modified;
- no 3D/projection semantics were introduced;
- `.ggb` construction serialization was not changed;
- Classic retained its launcher, GUI manager default and menu;
- no external runtime or library dependency was added.

G6 was not started.

## Analysis of the pinned upstream baseline

The source and build audit covered Desktop/common export packages, printing,
3D printers, plugin APIs and build dependencies.

| Capability found | Layer and primary code | Finding |
|---|---|---|
| PNG/PDF/SVG/EMF graphics export | Desktop `GraphicExportDialog`, FreeHEP/AWT renderers | Repaints `EuclidianView`; uses view/export/physical scale and is a presentation pipeline |
| PSTricks/PGF/Asymptote | shared `org.geogebra.common.export.pstricks`, Desktop frames | Reads construction types, but combines traversal, view bounds, approximation and encoding |
| STL/Collada | shared 3D renderer/printer packages | Renderer-oriented 3D mesh export, outside G5 |
| construction protocol | Desktop export dialog | Tabular construction history rather than geometry interoperability |
| printing | Desktop print preview/scale panel and view print path | Page/view concern, not model-space geometry |
| SVG/PDF plugin methods | Desktop `GgbAPID` | Delegates to existing view-oriented graphic export |
| DXF | repository-wide source/build search | No writer, dependency, DTO, menu, test or export service found |

No existing DTO, visitor, adapter or intermediate representation provided the
required world-coordinate, exactness-aware, format-neutral boundary. Reusing
the view repaint path would have made zoom/viewport presentation an export
input. Reusing a PSTricks generator would have retained direct format-specific
interpretation of source objects. The minimum sustainable solution was
therefore a small new neutral model and a single source adapter, while leaving
all upstream exporters intact.

Detailed evidence and file roles are retained in
`docs/architecture/native_2d_geometry_export.md`.

## Architectural decision

ADR 0005 records the structural boundary:

```text
resolved 2D GeoElement population
  -> GeoElementGeometryExportAdapter
  -> immutable GeometryExportModel
  -> DxfExporter
  -> ASCII DXF AC1015
```

Only `GeoElementGeometryExportAdapter` reads GeoGebra geometry classes.
`DxfExporter` consumes the neutral model and has no dependency on source
objects, kernel, views, Desktop, dialogs or files. `GeometryExportService` is
the reusable application API; Desktop resolves selection and file destination
outside it.

The design preserves these contracts:

- kernel objects and their construction dependency graph remain geometric
  authority;
- the neutral model is a read-only export snapshot, not persisted truth;
- a format exporter encodes approved parameters but never solves geometry;
- unsupported geometry remains explicit;
- future formats can reuse the neutral boundary;
- a future source adapter can add a declared approximation without redesigning
  the DXF writer.

## Neutral representation

`GeometryExportModel` records:

- input `SelectionMode`;
- coordinate system `GEOGEBRA_CARTESIAN_2D_WORLD`;
- explicit source and target unit (`UNITLESS` in G5);
- immutable typed entity values;
- construction-revision source ID, source type and optional label;
- normalized layer, RGB and visibility;
- `EXACT`/`APPROXIMATE` status and required tolerance for approximation;
- explicit diagnostics for omitted sources.

Source identifiers combine construction position with a sanitized label or
input ordinal. They support deterministic G5 correspondence but are not a new
persistent spatial/object identity contract.

## DXF choice and format profile

G5 writes deterministic ASCII DXF **AC1015 (AutoCAD 2000)**. This profile was
chosen because it natively supports the required `LWPOLYLINE`, `RAY`, `XLINE`
and `ELLIPSE` entities while permitting a compact audited group-code writer.
`$INSUNITS = 0` explicitly declares unitless model space. The writer produces
HEADER, LTYPE/LAYER tables and ENTITIES, deterministic handles and no
timestamps.

No external DXF library was necessary. Consequently G5 adds no dependency,
third-party license issue, packaging payload or SBOM component.

## Supported and unsupported types

| GeoGebra source | Neutral/DXF mapping | Policy |
|---|---|---|
| finite 2D point | point / `POINT` | exact |
| segment | bounded line / `LINE` | exact endpoints |
| ray | origin + unit vector / `RAY` | exact; never clipped |
| line | base + unit vector / `XLINE` | exact; never clipped |
| circle | circle / `CIRCLE` | exact |
| circular arc | arc / `ARC` | exact oriented bounds |
| ellipse/elliptic arc | ellipse / `ELLIPSE` | exact parameterization |
| polygon | closed polyline / `LWPOLYLINE` | exact boundary; no fill |
| polyline | open polyline / `LWPOLYLINE` | exact vertices |
| sector | none | unsupported |
| parabola/hyperbola/degenerate conic | none | unsupported |
| function/parametric/implicit curve | none | unsupported |
| legacy `Locus` | none | unsupported; no display-sample substitution |
| text/image/widget/3D | none | unsupported/out of scope |

The adapter emits no approximate entity in G5. The neutral model rejects an
approximate value without positive finite tolerance, preventing a future
adapter from silently hiding tessellation.

## Infinite entities

Lines and rays use native DXF `XLINE` and `RAY`, with a finite base/origin and
normalized direction. The adapter does not read Euclidian view bounds. G5 has
no viewport-clipped mode, so current zoom or window size cannot truncate an
unbounded geometric object.

## Units, coordinates and scale

G5 exports resolved 2D Cartesian world coordinates with identity transform and
`z = 0`. The baseline does not expose an approved physical model-unit contract;
G5 therefore records `UNITLESS` and does not invent millimetres or another CAD
unit. Zoom, DPI, axes ratio, viewport, print scale and graphic-export scale are
absent from the shared service API.

Physical drawing units and sheet scale remain future contracts. They cannot be
derived from display zoom.

## Layer and style policy

The current integer GeoGebra layer maps to `0` or `GEOCEDG_L<n>`. This is an
adapter over existing flat layers and is explicitly not the planned advanced
GeoCeDG layer architecture. True RGB and current hidden state are transported.
Line thickness, dash, point size, fill, opacity and label text are omitted
because geometry takes priority and current values do not define physical
model style.

DXF group `999` records the construction-revision source identifier for
semantic correspondence. It is validation provenance, not a persistence
contract.

## API and Desktop integration

`GeometryExportService.createModel(Collection<GeoElement>, SelectionMode)`
accepts a population already selected by its caller. `exportDxf(model)` returns
the deterministic DXF text. This API is dialog-independent and usable from
tests and future automation.

The GeoCeDG menu action is:

`GeoCeDG > Export 2D geometry as DXF (experimental)...`

The menu mnemonic is `Alt+G` and the direct action accelerator is
`Ctrl+Shift+D`; neither is added to Classic.

The controller supports complete labeled construction and current selection,
displays every diagnostic, requires confirmation for a supported subset,
chooses/normalizes the destination and reports written/skipped counts. Empty
or wholly unsupported populations write nothing.

Classic uses the default upstream menu factory. Its menu does not contain the
GeoCeDG action.

Visual inspection on the final app-image confirmed the six-group conservative
toolbar for an empty GeoCeDG document and the separate `GeoCeDG` product menu.
Opening the upstream `circles.ggb` fixture restored that document's saved
ten-group toolbar, as the inherited `.ggb` layout contract permits. This is
contextual document behavior rather than loss of GeoCeDG tools: the manifest
still defines 19 upstream modes in six default groups, while planned native
CeDG categories remain intentionally absent. G5 did not change that G2/G3
policy.

## Regression corpus and tests

`models/regression/g5-dxf-foundation/` contains:

- `construction.ggs`: deterministic synthetic source;
- `expected-entities.yml`: semantic expected evidence;
- `manifest.yml`: provenance, baseline, maturity, domain and limitations.

The case covers point, segment, circle, arc, polygon, polyline, line, ray,
ellipse and an unsupported function. The regression and model catalogs point
to the manifest and focused verifier.

`GeometryExportFoundationTest` includes a lightweight group-code parser. Its
invariants cover entity types/order, known coordinates and dimensions,
polygon closure, normalized infinite directions, unit header, layer/RGB/
visibility, source correspondence, explicit unsupported behavior,
approximation tolerance and equality after a view zoom/viewport change.
Byte equality is used only for the deterministic same-source/same-writer zoom
case; the versioned regression authority is semantic entity evidence.

`GeoCeDGProfileTest` also verifies that the GeoCeDG menu action is declared.

## Operational integration

`tools/agent/verify-dxf.ps1` is the focused G5 verifier and remains subordinate
to `tools/agent/verify.ps1`. It checks durable files, manifests, the regression
contract, AC1015 pinning, writer dependency boundaries, absence of a Classic
DXF menu action, focused tests and relevant checkstyle tasks. It cleans only
new regenerable build outputs and checks repository status preservation.

The feature is registered as `experimental` and enabled in the GeoCeDG
application profile. The regression and model manifests validate against the
existing G1 schemas; no parallel operational authority was introduced.

Two compatibility updates were required in older subordinate gates. The G3
verifier now filters the shared model catalog by `maturity: legacy` instead of
assuming that every later catalog entry is Templatev7, and it validates the
G2 feature IDs without rejecting later profile features. The G2 frontend gate
now expects the five-test Desktop suite produced after G5 added its menu
contract test. These changes retain all original zero-failure and G2/G3
assertions; they only allow the operational contracts to be extended by a
later phase.

## Packaging

No G4 packaging architecture changed. G4 `installDist` already includes the
updated shared/Desktop JARs, so app-image, normalized ZIP, MSI and EXE inherit
the GeoCeDG menu and export service. The packaging verifier continues to prove
that scientific PDFs, `Templatev7.ggb`, legacy corpora, non-Windows natives and
unapproved branding/assets are excluded.

The final G5 source state was packaged again with `-Target All`. The resulting
`common.jar` contains `GeometryExportModel` and `DxfExporter`; `desktop.jar`
contains `GeoCeDGDxfExportController` and `GeoCeDGMenuBar`. The app-image,
portable ZIP, MSI and EXE were generated successfully with jpackage 25.0.4,
.NET SDK 8.0.303 and WiX 5.0.2. Artifact hashes for this validation build were:

| Artifact | SHA-256 |
|---|---|
| portable ZIP | `98de7f9ea9716c9c6b4169b91d8e41d5c72738feb6cfda91abe0521abb3f6196` |
| MSI | `816f2786fa779a60ea7e6bea8b47462847ce152614d2406203dfbcbefaefba50` |
| EXE | `a51f6f88b3bb123af0d95cda2f604c1595b96fbc7925b049aefae97f66bbd322` |

These regenerable artifacts were removed after validation; hashes identify
the validation build but do not make it a redistributable release.

The distribution statement remains:

`PACKAGING TECHNICAL STATUS = PASS`

`PUBLIC REDISTRIBUTION STATUS = BLOCKED PENDING LICENSE/ASSET APPROVAL`

G5 does not change the `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION` marker.

## Controlled source impact

Pre-existing files modified for G5 are deliberately limited:

- `GuiManagerD.java`: protected menu-bar factory, defaulting to the exact
  Classic menu behavior;
- `AppGeoCeDG.java`: selects the GeoCeDG GUI manager;
- existing GeoCeDG profile test/manifests/catalogs/verifiers/documentation.

New Java implementation lives under `org.geocedg`. The complete controlled
list and purpose are recorded in `docs/upstream/modified-files.yml`.

There were no changes to kernel algorithms, command dispatch, `Locus`, 3D
semantics or construction serialization.

## Documentation and traceability

Traceability is organized as:

- requirement and phase gate: roadmap and G5 task prompt;
- decision: ADR 0005;
- normative design: `geocedg/specs/export/geometry-export-foundation.md`;
- implementation detail/audit: architecture document;
- feature state: experimental feature and application manifests;
- expected behavior: versioned regression model/evidence;
- executable validation: focused verifier and composed authority;
- operation and conceptual explanation: living user guide;
- completion evidence: this report.

`docs/user/geocedg_user_guide.md` was updated as a central G5 deliverable with
the GUI workflow, example, exactness table, units, layers, architecture,
validation method, historical evolution and future boundaries.

## Validation evidence

| Gate | Result | Evidence |
|---|---|---|
| focused shared G5 tests | PASS | 5 semantic tests, 0 failures/errors/skips |
| focused Desktop profile tests | PASS | 5 profile/menu tests, 0 failures/errors/skips |
| relevant checkstyle | PASS | shared common/common-jre and Desktop main/test |
| shared and Desktop build | PASS | repository Gradle wrapper; clean rebuilds in focused/composed gates |
| GeoCeDG launch | PASS | `runGeoCeDG` and final app-image both opened with window title `GeoCeDG`; both closed without a residual process |
| GeoCeDG toolbar/menu inspection | PASS | empty document showed 6 manifest-defined groups and the `GeoCeDG` menu; `circles.ggb` restored its own saved toolbar without changing the profile source |
| DXF production and inspection | PASS | packaged API produced an 806-byte AC1015 file with `POINT`, `LINE`, `CIRCLE`, unit/source metadata; SHA-256 `9465389bcc11c8439f1cce0952b1569ce394f67471f2b1836a849a7fc617cb8a` |
| Classic launch regression | PASS | `:desktop:desktop:run` opened `GeoGebra Classic 5` and ended `BUILD SUCCESSFUL` after orderly close |
| manifests/schemas | PASS | operational, legacy and focused DXF verifiers |
| packaging composition | PASS | app-image, ZIP, MSI and EXE regenerated; `verify-packaging.ps1 -CheckToolchain -RequireArtifacts` passed and packaged-class presence was inspected |
| composed authority + benchmark | PASS | `tools/agent/verify.ps1 -RunBenchmarks -AllowToolchainDownload`, exit 0 |
| whitespace/status/output/process checks | PASS | `git diff --check`; only regenerable outputs removed; no GeoCeDG process remains |

The Desktop action, mnemonic/accelerator, selection modes, diagnostics and
Classic exclusion are covered by executable tests and both application
launches were exercised. The host's virtual desktop did not provide reliable
automation of the native modal file chooser, so the saved sample was produced
through the same packaged, dialog-independent `GeometryExportService`/
`DxfExporter` API rather than by synthesizing a mouse click. This is a
validation-environment limitation, not a separate export path; a human UI
smoke remains a useful optional check on a normal interactive workstation.

## Limitations and debt

- source IDs are deterministic only within the construction revision; they are
  not persisted stable IDs;
- G5 uses unitless coordinates and has no drawing-sheet scale;
- text/labels and several analytic/general curve families are unsupported;
- there is no approximation policy enabled, DXF import or batch CLI;
- GUI selection is an input surface only; layer/visible/view modes are not yet
  exposed;
- RGB/visibility are transported, but line/fill presentation is intentionally
  incomplete;
- the writer is intentionally narrow and must evolve under semantic tests if
  more DXF sections/entities are required.
- automated native-file-dialog interaction is not reliable in the current
  virtual desktop; service behavior and the GUI contract are nevertheless
  covered independently and an actual packaged-service DXF was inspected.

## Relation to later phases

G6–G8 may provide a semantically valid Locus V2 export adapter, but G5 neither
anticipates nor implements it. G9 3D/projection identity, G10 DSL, G11 layers
and G15 drawing-sheet/PDF/SVG work may reuse/evolve the neutral boundary only
under their own decisions. None is claimed as available in G5.

## Final assessment

**G5 = PASS.** The neutral export boundary, deterministic DXF writer,
GeoCeDG-only Desktop workflow, regression contracts, operational gate,
packaging inheritance and living user documentation are complete and
validated. The feature remains deliberately `experimental`; unsupported
families and the lack of an approved physical-unit contract are explicit, not
silent approximations. G6 has not been initiated.

# GeoCeDG extension points and G2 frontend foundation

Status: G0 characterization retained; G2 frontend extension implemented
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`

“Fact” below means observed in the pinned source. “Recommendation” means a
future change subject to an accepted ADR/specification.

## Desktop launcher and application configuration

### Facts

The root Desktop task selects the `desktop` project in the included `desktop`
build. The application plugin declares
`org.geogebra.desktop.GeoGebra3D` as its main class
(`source/desktop/desktop/build.gradle.kts:75-81`) and requests Java 25 only for
the `run` task (`source/desktop/desktop/build.gradle.kts:20-24`).

The creation chain is:

```text
GeoGebra3D.main
  -> GeoGebra.doMain(..., GeoGebraFrame3D::new)
  -> GeoGebraFrame3D.createApplication(...)
  -> new App3D(...)
  -> AppD initialization
```

Source anchors are `GeoGebra3D.java:26-34`,
`gui/app/GeoGebraFrame3D.java:31-38`, and
`geogebra3D/App3D.java:64,79-88`. The shared `App` starts with
`AppConfigDefault` (`source/shared/common/src/main/java/org/geogebra/common/main/App.java:472`)
and exposes `getConfig()`/`setConfig()` at lines 4532-4545. `AppD` calls
`getConfig()` during construction, before kernel and UI initialization complete
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java:381-430`).

### G2 implementation

ADR 0001 is accepted and implemented through `AppConfigGeoCeDG`,
`GeoCeDG`/`GeoCeDGFrame`/`AppGeoCeDG`, and one protected early-config
constructor seam in `AppD`/`App3D`. The default constructors and
`GeoGebra3D` entry remain the Classic path. Gradle exposes
`:desktop:desktop:runGeoCeDG` alongside the unchanged
`:desktop:desktop:run` diagnostic task.

## Perspectives and dock layout

### Facts

- `source/shared/common/src/main/java/org/geogebra/common/gui/Layout.java:74-123`
  builds the default graphing, geometry, spreadsheet, CAS, 3D, probability,
  whiteboard, scientific, and evaluator perspectives.
- `Layout.getDefaultPerspective()` returns entry zero at lines 432-445.
- `source/shared/common/src/main/java/org/geogebra/common/io/layout/Perspective.java:36`
  is the serialized layout value object; `PerspectiveDecoder.java:43,200-241`
  decodes saved/default perspective codes.
- Desktop `LayoutD` initializes common defaults in its constructor
  (`source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/layout/LayoutD.java:42-53`)
  and owns `DockManagerD` (`LayoutD.java:61-68` and `DockManagerD.java:55`).
- `AppD` applies a saved temporary perspective or the default at
  `source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java:494-497`.

### G2 implementation

`GeoCeDGProfile` compiles a fresh initial `Perspective` from
`apps/geocedg/application-profile.yml`. Algebra and the primary 2D view are
initially visible; other upstream views remain available. A document or saved
profile perspective takes precedence. No upstream default perspective is
deleted or mutated.

## Toolbar and user tools

### Facts

`source/shared/common/src/main/java/org/geogebra/common/gui/toolbar/ToolBar.java`
contains the default 2D/3D definitions (`:39-80`). Its parser documents and
implements the grammar at lines 657-709:

- integers are Euclidian mode IDs;
- `,` inserts an in-menu separator;
- `|` begins a new menu;
- `||` creates a separator before the next menu.

Desktop `ToolbarD` parses a dock-panel, custom, or default definition at
`source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/toolbar/ToolbarD.java:172-196`.

`ToolBar.getAllTools()` appends visible macros after ` || ` and assigns each
`kernel macro index + EuclidianConstants.MACRO_MODE_ID_OFFSET`
(`ToolBar.java:755-787`). When the tool-creation dialog saves a visible macro,
it adds that derived mode to the toolbar and selects it
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/dialog/ToolCreationDialogD.java:224-246`).

### G2 implementation

The versioned GeoCeDG application manifest is now the single durable toolbar
source. `GeoCeDGProfile` validates unique mode IDs and adapts its ordered
categories to the inherited grammar. G2 exposes only existing conservative
modes; user macros and legacy CeDG tools are not imported or promoted.

## Commands and dependency graph

### Facts

- Command names are enumerated in
  `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/Commands.java:28`.
- `CommandDispatcher` groups commands into processor factories; `Intersect`
  and `Locus` are routed at `CommandDispatcher.java:580,601`.
- `BasicCommandProcessorFactory` constructs `CmdIntersect` and `CmdLocus` at
  `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/BasicCommandProcessorFactory.java:112-113,154-155`.
- Every `AlgoElement` declares inputs/outputs. `setDependencies()` registers
  the algorithm on its inputs and adds output/algorithm-list dependencies
  (`source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoElement.java:713-749`).
- `setEfficientDependencies()` preserves standard serialized/dependency inputs
  while using a reduced update set; it is explicitly intended for Locus
  (`AlgoElement.java:751-771`).

### Current decision

Future semantic commands must follow registration, processor, algorithm,
`setInputOutput()`, compute, serialization, localization, and test conventions.
No GUI-only command shortcut is suitable for kernel-owned semantics.

## Current Locus contract

| Responsibility | Current implementation and evidence |
|---|---|
| Command processing | `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocus.java:34-56`; 3D extends it in `geogebra3D/kernel3D/commands/CmdLocus3D.java:29`. |
| Generation | `source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLocusND.java:45`. It constructs a sampled locus, registers for Euclidian-view changes (`:128-154`), and uses standard versus efficient dependencies (`:303-342`). |
| Storage | `source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoLocusND.java:47-56` stores an `ArrayList<T extends MyPoint>`; `getPointLength()` is list size (`:112-120`). `GeoLocus` and `GeoLocus3D` supply concrete points. |
| Path behavior | Parameter bounds are sample indexes `0..size-1` (`GeoLocusND.java:189-197`). `pathChanged()` uses `floor(pp.t)` as the segment index and the fractional part as interpolation (`:325-367,382-397`). `GeoLocus.pointChanged()` writes `closestPointIndex + closestPointParameter` (`GeoLocus.java:71-85`). |
| Rendering | `source/shared/common/src/main/java/org/geogebra/common/euclidian/draw/DrawLocus.java:47-89,193-227` builds a general path directly from `locus.getPoints()`. 3D uses `geogebra3D/euclidian3D/draw/DrawLocus3D.java`. |
| XML persistence | Ordinary algorithm XML serializes the Locus command/expression and labeled output geo through `AlgoElement.getXML()` (`AlgoElement.java:1291-1401`). The `GeoElement` open tag carries type and label (`kernel/geos/GeoElement.java:4495-4510`); there is no separate semantic Locus definition/version contract. Pen strokes have additional point XML but are a distinct `GeoLocusStroke` case. |
| Existing `Length` behavior | `source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLengthLocus.java:67-75` returns `locus.getPointLength()`: sample cardinality, not world-coordinate arc length. |

### Screen- and sample-dependent decisions

`AlgoLocusND.compute()` refreshes screen borders before regenerating its point
list (`AlgoLocusND.java:440-476`). Bounds and adaptive distances use kernel
viewport limits and divide pixel thresholds by `kernel.getXscale()` and
`getYscale()` (`AlgoLocusND.java:818-884`). A view change can trigger a full
recompute (`:887-895`). Storage order then defines path parameter, incidence
against sampled segments, rendering order, and the existing `Length` result.

**Finding:** the current Locus is a view-sensitive sampled path. It is not an
authority suitable for exact/controlled metric length, stable semantic
parameterization, or robust Locus intersections. This is characterization only;
no Locus behavior is changed in this task.

## Intersection dispatch

### Facts

`source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdIntersect.java`
explicitly dispatches these two-object families:

- line-line and line-parametric curve (`:142-160`);
- parametric curve-curve (`:161-167`);
- line/polyline/polygon and polyline/polygon combinations (`:168-217`);
- line/polyline/polygon/function with conics and conic-conic (`:222-258`);
- real-valued functions with lines, polylines, polygons, or functions
  (`:259-307`);
- implicit curve with polyline, line, conic, function, or another implicit
  curve (`:308-347`);
- implicit surface-line (`:348-357`);
- generic `Path`-point incidence (`:359-367`).

Three-argument forms select an indexed/near intersection for line-line,
line-conic, conic-conic, polynomial/function, and implicit combinations
(`CmdIntersect.java:385-520`); four arguments support bounded
function-function and seeded parametric curve-curve forms (`:104-127`). 3D
algorithms live under `geogebra3D/kernel3D/algos/AlgoIntersect*` and are
selected through the 3D dispatcher/manager.

There is no Locus-specific branch or `AlgoIntersect*Locus` implementation in
this dispatcher. Because `GeoLocusND` implements `Path`, only the generic
Locus-point incidence case is reachable; Locus-line, Locus-conic, and
Locus-Locus are not explicitly supported.

## Views, rendering, selection, layers, and visibility

### Facts

- `Kernel` owns a list of `View` instances (`source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java:174`) and attaches/detaches them at
  `Kernel.java:3703-3758`; `notifyAddAll()` repopulates a view at
  `:3766-3771,3863`.
- `EuclidianView` creates and maps 2D drawables at
  `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianView.java:2037-2113,2424-2468`.
- `EuclidianView3D` maintains a `GeoElement -> Drawable3D` map and selects
  concrete drawables by geo type
  (`source/shared/common/src/main/java/org/geogebra/common/geogebra3D/euclidian3D/EuclidianView3D.java:238-252,549-770`).
- Pointer hits are view/renderer results (`EuclidianView3D.java:1988-2049`),
  while application selection is centralized in
  `source/shared/common/src/main/java/org/geogebra/common/main/SelectionManager.java:75,306-332`.
- `GeoElement` owns flat integer `layer`, visual visibility, fixed state,
  selection permission, 3D visibility, and per-view flags
  (`source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java:160-215,278,312`).
- XML writes `<show ... ev=...>` and `<layer>` through
  `source/shared/common/src/main/java/org/geogebra/common/kernel/geos/XMLBuilder.java:52-107,142`; colors, fill, line style, fixed, and
  `selectionAllowed` are written by `GeoElement.java:4586-4732`.

The current layer is not hierarchical and carries no independent CeDG
print/export role. Euclidian visibility and per-view membership are
presentation/application state; neither is proof of geometric visibility in a
projection.

### Recommendation

Keep geometric visibility as a future shared projection service. Place
hierarchical organization, locking, print/export state, and named views in a
shared document/application model. Reuse existing style/view flags only through
explicit adapters; do not reinterpret them as spatial truth.

## Print, vector/raster export, and pages

### Facts

Desktop `GraphicExportDialog` supports PNG, PDF, SVG, and EMF
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/export/GraphicExportDialog.java:78,98,115`).
It paints the active `EuclidianView`; PDF derives physical output from the
view's printing scale (`GraphicExportDialog.java:980-1030`). SVG groups
drawables by the current flat integer layer (`:870-907`).

`EuclidianViewD` implements `Printable`; its print scale is
`(PRINTER_PIXEL_PER_CM / getXscale()) * printingScale`
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/euclidian/EuclidianViewD.java:360-372`).
`PrintPreviewD` owns Java `PageFormat`, `Paper`, orientation, margins, and a
`Pageable` preview (`source/desktop/desktop/src/main/java/org/geogebra/desktop/export/PrintPreviewD.java:66-74,545-601,704-802`).

The web Notes feature has a separate multipage model. `PageContent` stores
construction XML, object names, thumbnail, title, and order
(`source/web/web-common/src/main/java/org/geogebra/web/html5/main/PageContent.java:26-47`),
and `PageListController` loads each slide as a GGB file and can export all
slides to PDF (`source/web/web/src/main/java/org/geogebra/web/full/gui/pagecontrolpanel/PageListController.java:87-118,171-278`).
Web PDF rendering is backed by `Canvas2Pdf`
(`source/web/web-common/src/main/java/org/geogebra/web/html5/euclidian/EuclidianViewW.java:480-522`).

### Recommendation

These paths provide rendering and page-format infrastructure, not a semantic
CeDG sheet model. A future PDF/SVG sheet service should consume a read-only,
world-coordinate document/export model and make scale, paper, projection,
approximation, and layer policy explicit.

## Tests, performance, and packaging

### Facts

Testing locations and Gradle conventions are mapped in
`docs/architecture/upstream_module_map.md`. A representative timing assertion
exists in `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/arithmetic/FractionTest.java:61-80`, but it is a one-second wall-clock threshold,
not a reusable benchmark harness.

Available instrumentation includes:

- `GeoGebraProfiler` counters for repaint, cascade, algebra, event, and drag
  durations (`source/shared/common/src/main/java/org/geogebra/common/util/debug/GeoGebraProfiler.java:20-134`);
- `FpsProfiler` frame counts and min/max/average FPS
  (`source/shared/common/src/main/java/org/geogebra/common/util/profiler/FpsProfiler.java:26-127`);
- web drawing record/replay classes under
  `source/web/web-common/src/main/java/org/geogebra/web/html5/euclidian/profiler`.

The checked tree contains no JMH dependency, checked-in model benchmark suite,
or runnable `test/scripts/benchmark/art-plotter`; only a comment still refers
to that external/removed harness (`source/web/web-common/src/main/java/org/geogebra/web/html5/main/AppW.java:2592-2599`). Therefore the baseline verifier exposes no misleading timing switch.

Gradle's application plugin exposes `installDist`, `distZip`, `distTar`, and
`startScripts`; no `jpackage` task exists. The future clean input is
`:desktop:desktop:installDist`, after substituting a GeoCeDG launcher and only
licensing-approved resources.

### Recommendation

Create deterministic model-based kernel/render benchmarks in the later G1
operational skeleton, reusing profiler hooks where useful. Record warm-up,
machine/JVM, model revision, repetitions, and result distribution; do not use a
single UI wall-clock assertion as the performance authority.

## G2 boundary and deferred work

G2 changes only application identity, early configuration, initial layout,
toolbar selection, window identity, and launch/build wiring. The controlled
upstream diff is recorded in `docs/upstream/modified-files.yml`. No command,
kernel behavior, Locus, spatial object, projection relation, serialization
app code, layer model, exporter, installer, dependency, legacy tool, or final
branding asset is added. Those subjects remain deferred to their roadmap
gates and require their own approved contracts and focused tests.

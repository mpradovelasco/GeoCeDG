# G9P reference-model workflow and current-GUI audit

- Status: G9P read-only evidence report
- Date: 2026-08-16
- Inspected revision: `667e1cfa6f36`
- Machine inventory: `g9p-reference-inputs.json`
- Productive implementation authorized: **no**

## Scope and evidence discipline

This report compares the current GeoCeDG Desktop profile, the inherited Classic
path, the immutable Templatev7 context, four author-supplied GGBs, the author
note, and the supplied screenshot. The GGBs were read as ZIP/XML containers;
they were not opened for update, resaved, normalized, or executed. The PNG and
notes were not modified. Raw hashes, sizes, every archive member, object and
command inventories, layouts, macro sets, script counts, and workflow summaries
are recorded in `g9p-reference-inputs.json`.

The model labels help explain the author's procedure but are not treated as
semantic identity. In particular, three reference models and Templatev7 carry
the same document UUID. That is direct evidence that an archive UUID, label,
construction position, or visual coincidence cannot identify a spatial object
or Locus solution across documents.

## Immutable input digest

| Input | Bytes | SHA-256 | Role |
|---|---:|---|---|
| `geocedg-reference-general-construction-workflow.ggb` | 140136 | `738e3edcf44e10f0c07b846d1f53127c4d8450cf0ad209ea9a0e3e8cc2c36a2e` | general construction workflow |
| `geocedg-reference-locus-cylindrical-graft-development.ggb` | 76870 | `9e220a695a4b1ee2bf60adc77872133e8074740443ae659de1404539de8141f2` | intersection and development |
| `geocedg-reference-locus-focal-sphere-illumination.ggb` | 111502 | `baec7131aa95676864457d1602f73f8fef3ce37674fc0e48b28efd4feac204a0` | projection changes and illumination |
| `geocedg-reference-locus-truncated-cone-cylinder-connections.ggb` | 97204 | `18c6fad4d53fc3bb03a1546021a0677656fded2c853b7ec198312b96ee55e155` | folding and two connection loci |
| `geocedg-reference-construction-workspace.png` | 571678 | `3c30ddc0ae4aa54d02292a66bae3ed37447911803f52b64315cc1c6b71f966ba` | layout/density evidence |
| `geocedg-reference-workflows-notes.md` | 16372 | `d30f08d8ff278e40fbf65e26a030969d129c56e5e5bda8f18b5fdee760a8b6a0` | author interpretation |
| `Prompt.md` | 42340 | `b29a919c5c3a7ba781f720d3a9c646f2892c6be19b87419001493481077a9e08` | task input |
| `models/legacy/template-v7/original/Templatev7.ggb` | 48149 | `f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113` | immutable legacy context |

All listed input hashes were rechecked after inspection. Inputs are unchanged.

## Archive and layout findings

All five GGBs have 22 ZIP members: sixteen identical embedded image resources
plus `geogebra_defaults2d.xml`, `geogebra_defaults3d.xml`,
`geogebra_javascript.js`, `geogebra_macro.xml`, a thumbnail, and
`geogebra.xml`. All embed the same ordered 24 macro names and the same seven
custom toolbar groups inherited from Templatev7. The author models therefore
record a document-carried legacy environment, not evidence that those macros
are stable GeoCeDG product commands.

| Model | GeoGebra | Visible saved views | Elements / commands / expressions | Recognized dependency depth lower bound | Controls | Legacy Locus |
|---|---|---|---:|---:|---|---:|
| general construction | 5.2.892.0 | Graphics, Algebra | 267 / 183 / 14 | 32 | 4 sliders, 3 checks, 3 buttons, 4 fields | 0 |
| cylindrical graft | 5.4.927.1 | Graphics, Algebra | 234 / 156 / 25 | 25 | 3 sliders, 4 checks, 4 buttons, 6 fields | 6 |
| focal sphere | 5.4.927.1 | Graphics, Algebra, Construction Protocol | 208 / 140 / 23 | 29 | 3 sliders, 3 checks, 3 buttons, 4 fields | 4 |
| truncated cone | 5.4.927.1 | Graphics, Algebra, Construction Protocol | 245 / 174 / 21 | 32 | 5 sliders, 3 checks, 3 buttons, 4 fields | 6 |
| Templatev7 context | 5.2.879.0 | Graphics, Algebra | 23 / 5 / 13 | 1 | 1 slider, 3 checks, 3 buttons, 4 fields | 0 |

The dependency depth is deliberately reported as a lower bound: it follows
only ordered command inputs that exactly match an already known label. It is
useful evidence of deep constructions, not a substitute for kernel dependency
inspection.

The focal-sphere and truncated-cone files save the Construction Protocol as a
visible dock. The general model saves protocol navigation at step 205 even
though the protocol dock is closed. The screenshot shows the more expressive
working arrangement described in the note: Algebra on the left, primary
Graphics in the center, Construction Protocol on the right, a floating
Properties window, contextual tool help beside the toolbar, and the input bar
at the bottom. This is workflow-density evidence, not a requirement to copy
window sizes or pixels.

All reference GGBs retain the 19-group Template toolbar, including 24 custom
mode IDs. The raw toolbar string and its UTF-8 SHA-256 are in the manifest.
Every author model has a bottom command input and contextual toolbar help
enabled. Properties is saved as an available floating view but closed in XML.

## Per-model procedure findings

### General construction

The model constructs a hemispherical vessel with three cylindrical legs from
orthographic information. The dominant repeated operations are intersection,
line construction, rotation, circle/conic construction, segment construction,
and styling. It uses 66 `Intersect`, 27 `Line`, 16 `Rotate`, 13 `Segment`, 11
`Circle`, and seven `Mirror` command nodes. Five `EllipseAxis` invocations and
one `conj2mainAxesEllipse` invocation encode an important reusable 2D conic
workflow.

The procedure first completes projected points, rotates an axis into a frontal
position, constructs an intersection circle and ellipse projections, returns
the result to its original position, obtains the other legs by rotations and
reflection, then extends generators and constructs support-plane ellipses.

Native G6-G8 is not a direct replacement because the file contains no Locus.
The missing durable capability is G9 spatial identity, frames, projection
bindings, and procedure provenance. Axis-conversion macros remain candidates
for separately characterized high-level 2D tools; they are not evidence for a
spatial kernel object.

### Cylindrical graft and development

The file uses six legacy Locus objects. An auxiliary frontal plane obtains the
moving entrance points; paired projections generate the spatial intersection;
transported generators produce a flat development referenced to the generator
through `A`. It also uses 39 `Intersect`, 30 `Line`, 25 `Segment`, 12 `Point`,
10 `Translate`, three `pointJump`, and three `DuctSymbol` nodes.

Legacy Locus cannot be intersected with the generators on which its result
lies, so downstream points are obtained indirectly. No sampled-list length
macro is invoked: the file transports geometry to the development and uses
ordinary measurements instead. G6-G8 can replace locus definition, semantic
metric and intersection workarounds after G9U0 exposes them. G9 is still needed
to bind the paired projections to one spatial curve and to preserve the
development procedure's provenance.

### Truncated cone to two cylinders

The file uses six legacy Locus objects and 64 `Intersect` nodes. It folds a
freely positioned right section of the cone, selects a moving point on that
section, transports the corresponding generator into a profile view, uses a
pencil plane to obtain cylinder generators, and projects two moving connection
points into six locus curves.

G6-G8 can replace the six legacy semantic curves and their downstream
intersection limitations. The fold, section plane, cone/cylinder identity,
and correspondence between projections depend on G9. `DuctSymbol` and the
sheet/export scripts remain presentation/model concerns.

### Focal illumination of a sphere

The file uses four legacy Locus objects. Two changes of projection plane place
first the tangent-cone axis and then the illumination-cone axis in convenient
positions. A plane controlled by point `K` cuts the sphere and cone in circles;
their moving intersections generate paired loci. The model then combines the
bite intersection, the light separatrix, and sensor rotation to report an
illuminated fraction.

The command inventory includes 40 `Intersect`, 28 `Line`, 11 `Circle`, nine
`Point`, seven `translationCoor`, and five `OrthogonalLine` nodes. G6-G8 can
replace the four locus objects and support tangent-aware intersections. G9 is
needed for typed sphere/cone identity, the two projection-frame changes, and
coherent reprojection. The illumination fraction is model-specific analysis.

## R1 one-dimensional generator evidence mapping

The reference artifacts do not implement the normative future public providers; they
show why the public contract cannot stop at “slider or segment.” The mapping
below separates observed procedure evidence from the future semantic
replacement.

| Required workflow | Immutable evidence | Semantic generator mapping | Future public destination |
|---|---|---|---|
| free point on folded circle | author note, truncated-cone model: the folded-down circle carries free point `p`, which selects a cone generator | typed point-state provider on a circle; oriented angular domain and seam are explicit, Cartesian position is not identity | G9U0 point-support provider/action; G9U1 exposes it |
| free point on arc/circle | the folded-circle procedure is concrete circle evidence; the archived Template toolbar also carries circumcircular arc operations, but toolbar presence alone is not promotion evidence | closed typed circle/circular-arc providers; arc support/start/extent/orientation are durable inputs | G9U0 provider; upstream arc commands remain ordinary construction surface |
| scalar-driven auxiliary surfaces | all models use sliders/parameters; focal model explicitly parameterizes cone aperture by `halfCono`; free point `K` controls the auxiliary cutting plane | explicit scalar coordinate/domain and deterministic algebraic state map `s -> t(s)`; other inputs remain registered external parameters | G9U0 scalar generator; later G9 procedure may consume the same semantic parameters |
| nested/intermediate loci | graft note transports moving intersection points to a developed domain whose resulting curve is itself a locus; the truncated and focal models construct several paired/intermediate projection loci | acyclic source-Locus V2 -> semantic point -> dependent construction -> outer-Locus V2 when a locus is the actual support; ordinary shared-driver sibling loci remain siblings | G9U0 normal-DAG nesting, never sampled locus reuse |
| intersection-derived downstream construction | graft note states legacy loci cannot be intersected with their own generators; all locus models construct downstream intersection/measurement geometry through workarounds | G8 rich intersection remains the procedure result; a separately materialized exact-token point is the downstream dynamic object | G9U0 general `Intersect` plus exact-token point |
| legacy `postLocus` | embedded in all Template-derived archives but invoked by none of the four saved models; replacement request is explicit in the author note | valid semantic components/invalid intervals plus rich G8 intersection/status, not thresholded sample deletion | replace with native G6-G8 capability after G9U0 publication |
| legacy `listLength` | same provenance: embedded, not invoked; author note requests native replacement | authoritative G7 rich total metric; guarded standard scalar only when admissible | replace with native G6-G8 capability after G9U0 publication |
| legacy `listLength12` | same provenance: embedded, not invoked; author note also names the corresponding `_2` helper | G7 metric between durable semantic positions/preimages | replace with native G6-G8 capability after G9U0 publication |

This mapping does not claim that every moving point in the saved files is
already a Locus-on-Locus construction. It specifies the native replacement when
the semantic support really is a V2. Scalar and point providers remain kernel
semantics; the future workspace is only their GUI client.

## Cross-model command inventory

The four author models contain 653 command nodes. `Intersect` alone accounts
for 209; line, segment, circle, point, ray, tangent, rotation, reflection, and
orthogonality recur across the workflows. This supports a broad professional
construction workspace rather than the current six small toolbar groups.

| Command | General | Graft | Focal | Truncated | Total |
|---|---:|---:|---:|---:|---:|
| Angle | 2 | 0 | 3 | 2 | 7 |
| Circle | 11 | 11 | 11 | 12 | 45 |
| conj2mainAxesEllipse | 1 | 0 | 1 | 0 | 2 |
| DuctSymbol | 0 | 3 | 0 | 2 | 5 |
| Ellipse | 0 | 0 | 0 | 1 | 1 |
| EllipseAxis | 5 | 0 | 2 | 0 | 7 |
| ellipseLength12 | 4 | 2 | 4 | 1 | 11 |
| If | 1 | 1 | 1 | 1 | 4 |
| Intersect | 66 | 39 | 40 | 64 | 209 |
| Line | 27 | 30 | 28 | 27 | 112 |
| Locus | 0 | 6 | 4 | 6 | 16 |
| Midpoint | 4 | 0 | 1 | 0 | 5 |
| Mirror | 7 | 0 | 1 | 5 | 13 |
| OrthogonalLine | 2 | 1 | 5 | 1 | 9 |
| Perimeter | 0 | 1 | 0 | 0 | 1 |
| Point | 2 | 12 | 9 | 12 | 35 |
| pointJump | 0 | 3 | 0 | 0 | 3 |
| PoliLineVisibility | 4 | 0 | 0 | 0 | 4 |
| Ray | 7 | 2 | 6 | 7 | 22 |
| Rotate | 16 | 0 | 2 | 2 | 20 |
| Segment | 13 | 25 | 8 | 20 | 66 |
| Semicircle | 1 | 0 | 0 | 0 | 1 |
| sheetISOAnLand | 1 | 1 | 1 | 1 | 4 |
| sheetISOAnVert | 1 | 0 | 0 | 0 | 1 |
| Tangent | 4 | 2 | 2 | 5 | 13 |
| Textfield | 4 | 6 | 4 | 4 | 18 |
| Translate | 0 | 10 | 0 | 0 | 10 |
| translationCoor | 0 | 0 | 7 | 1 | 8 |
| Vector | 0 | 1 | 0 | 0 | 1 |

All four archives embed all 24 Template macros, but use only selected subsets.
The general model invokes 16 macro commands, graft 10, focal 15, and truncated
cone five. None invokes `postLocus`, `listLength`, or `listLength12`; their
migration case comes from Templatev7 and the author's explicit note, not from a
false claim that these particular saved constructions call them.

## Legacy macro migration matrix

“Native” below means a currently available primitive or an already implemented
G6-G8 internal capability. It does not imply a public G9U0 command exists. Each
row has exactly one normalized disposition from the six R1 classes. The class
governs future promotion; compatibility loading of an embedded macro is a
separate concern.

| Legacy operation | Current implementation | Current/native equivalent | Approved future placement | Kernel/public command need | G9 dependency | Normalized disposition class |
|---|---|---|---|---|---|---|
| `SplineLength` | sampled list → spline and sum | ordinary spline/curve length where supported; not Locus metric | legacy compatibility only | no new exact claim; explicit approximation would need a separate contract | none | **retire** |
| `sheetISOAnLand` | macro-generated ISO sheet | future sheet/document service | Laboratory pending separately governed document export | application/export action, not kernel | none | **Laboratory-only** |
| `sheetISOAnVert` | macro-generated ISO sheet | future sheet/document service | Laboratory pending separately governed document export | application/export action, not kernel | none | **Laboratory-only** |
| `directDimension` | segments, vectors, text and conversion | `Distance` plus presentation | Metrics and validation; Presentation and document | characterize a later high-level dimension action | none | **stable GeoCeDG tool candidate** |
| `SquarebyDiagonal` | composed primitive macro | existing point/line/polygon primitives | menus/user tools or composition | no | none | **upstream-native** |
| `CirclebyD` | diameter convenience macro | `Circle` with converted radius | menus/user tools or composition | no | none | **upstream-native** |
| `EllipseAxis` | centre and axis-end construction | current conic primitives | Circles, conics and curves | characterize a composed public 2D tool | none | **stable GeoCeDG tool candidate** |
| `pointJump` | signed transport along direction | point/vector/translate primitives | CeDG procedures and developments | typed procedure/DSL, not new geometric truth | ProjectionSystem/frame/binding provenance for spatial use | **requires G9 spatial semantics** |
| `PoliLineVisibility` | solid/dashed segment output | style controls only; not geometric visibility | Laboratory/model presentation | no kernel visibility inference | future projection visibility only under separate spec | **Laboratory-only** |
| `Perimeter` | wrapper over `Circumference` | existing command | Metrics and validation overflow | no | none | **upstream-native** |
| `axisDimension` | axis-oriented graphics/text | `Distance` plus presentation | Metrics and validation; Presentation and document | characterize a later high-level dimension action | none | **stable GeoCeDG tool candidate** |
| `relCoor` | line/vector/intersection composition | primitives reproduce coordinates but not spatial provenance | CeDG procedures and developments | typed procedure/DSL | ProjectionSystem/frame/binding semantics | **requires G9 spatial semantics** |
| `DuctSymbol` | presentation symbol macro | none required in kernel | model/user-tool presentation | no | none | **Laboratory-only** |
| `SymmSymbol` | presentation symbol macro | none required in kernel | model/user-tool presentation | no | none | **Laboratory-only** |
| `listLength` | sampled chord sum | G7 rich total Locus V2 metric | Locus V2; Metrics and validation | G9U0 public rich metric plus guarded scalar | identity/persistence foundation | **replace with native G6–G8 capability** |
| `listLength12` | sampled partial list sum | G7 metric between durable semantic positions | Locus V2; Metrics and validation | G9U0 public point positions and partial metric | identity/persistence foundation | **replace with native G6–G8 capability** |
| `postLocus` | threshold filtering of legacy samples | G6 semantic components plus G8 intersections | Locus V2; Relations and intersections | G9U0 public definition/result inspector | identity/persistence foundation | **replace with native G6–G8 capability** |
| `ellipseVisibility` | presentation ellipse arc | conic arc plus style; not visibility truth | Laboratory/model presentation | no | future visibility service only under separate spec | **Laboratory-only** |
| `translationCoor` | coordinate transport macro | primitives reproduce diagram coordinates but not frame provenance | CeDG procedures and developments | typed procedure/DSL | ProjectionSystem intrinsic coordinates and diagram map | **requires G9 spatial semantics** |
| `circArcbyAngle` | rotate then circular arc | existing `Rotate`/arc commands | Circles, conics and curves overflow | no | none | **upstream-native** |
| `dummyRotate` | point rotation convenience | existing `Rotate` | Transformations and manual projections | no new stable command; a future semantic procedure is separate | none for manual composition | **upstream-native** |
| `conj2mainAxesEllipse` | conjugate-to-principal axes | no one-step baseline equivalent | Circles, conics and curves | characterize a public 2D command | none | **stable GeoCeDG tool candidate** |
| `ellipseLength12` | ellipse arc then baseline `Length` | existing arc and `Length` | Metrics and validation overflow | no new Locus command | none | **upstream-native** |
| `IFPositiveSelectPoint` | sign-based conditional point | existing `If` | algebra/user tools | no | none unless part of a separately specified procedure | **upstream-native** |

## Common operation and phase mapping

| Reference operation | Current native surface | Missing capability | Approved UI group | Future phase |
|---|---|---|---|---|
| Select, inspect, relabel, step through construction | Move, Algebra, Properties, Protocol | coherent workspace defaults and localized action catalog | Inspect and construct | G9U1 |
| Point/line/segment/ray/vector construction | upstream modes/commands | none | Linear geometry | G9U1 organization only |
| Scalar parameters/maps and moving points on segment/circle/arc/Locus V2 | upstream numeric/algebra and point-on-object behavior; V2 has no public Path | typed one-dimensional generator, explicit true driver/domain, durable preimage and continuation | Parameters and drivers; Locus V2 | G9U0 semantic providers, then G9U1 client placement |
| Repeated curve intersections and relation checks | general Intersect/Relation supports baseline types | V2 public dispatch, rich result and token point | Relations and intersections | G9U0 then G9U1 |
| Circle/conic/tangent/axis and general curve work | upstream conic/curve commands plus macros | characterized axis tools | Circles, conics and curves | G9U1; optional later 2D tool phase |
| Legacy Locus creation and semantic nesting | legacy mode 47 and `CmdLocus` | reconstructible typed V2 evaluator, point-on-Locus and persistence | Locus V2 | G9U0 |
| Locus total/partial metric and construction checks | sampled legacy macros plus native baseline measurements | public rich G7 metric, semantic positions and status inspection | Metrics and validation | G9U0 then G9U1 |
| Rotate, reflect, translate and build manual auxiliary projections | inherited transforms and explicit primitive sequences | coherent task grouping; no spatial claim | Transformations and manual projections | G9U1 organization only |
| Fold/change plane/true magnitude/section/development | explicit transported geometry and macros | typed frames/bindings, procedure provenance and generated-step ownership | CeDG procedures and developments | G9U2 after global G9 PASS |
| Style, visibility, Properties, Protocol and document layout | inherited Properties/Protocol and saved document perspective | manifest-defined workspace state and later owned layer/sheet policy | Presentation and document | G9U1; later document/layer phases separately gated |
| Input/scripts/macros, user tools and DXF/sheet export | Algebra input, document scripts, legacy macros and G5 DXF | owned automation routes and separately governed extended/document export | Automation and import/export | G9U1 route, G9X1 DXF, other export phases separate |

## Current GeoCeDG GUI and actual gap

### Current implementation

- The dedicated launcher and isolated preferences are real:
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDG.java:34-59`
  selects a GeoCeDG settings file, and
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGFrame.java:18-53`
  preserves product identity/new windows.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java:42-46`
  uses the manifest perspective only when no saved or
  document perspective was loaded. A GGB-carried legacy layout/toolbar can
  therefore appear when a reference model is opened; this is document context,
  not a change to the stable product manifest.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java:33-36,80-139`
  loads and validates the version-1 application profile. The same file at
  `:142-166` compiles one flat list of numeric modes into
  a toolbar. It has no workspace registry or switch operation.
- `apps/geocedg/application-profile.yml` declares one perspective, six toolbar
  categories, 19 total mode entries, and four unimplemented category names.
  It defaults to Graphics and Algebra only; Protocol and floating Properties
  are not part of that first-run workspace.
- `source/shared/common/src/main/java/org/geocedg/common/main/settings/config/AppConfigGeoCeDG.java:50-56`
  retains the `classic` app code and returns no command filter. Consequently
  the inherited Classic command population is available; the feature manifests
  do not currently enforce runtime command availability.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMenuBar.java:37-54`
  inherits the full menu bar and inserts one hard-coded GeoCeDG menu containing
  DXF export. Its product/menu text is not localization-key driven.
- `geocedg/features/experimental.yml:16-30` records Laboratory and Locus V2 as
  default-off, but no runtime feature service consumes those declarations.
- `tools/legacy/open-laboratory.ps1:36-88` validates a registered immutable
  resource before launch; `:91-126` launches it explicitly in GeoCeDG or the
  separate Classic process. Laboratory is not a stable workspace.
- The Classic diagnostic boundary remains process-level with separate
  preferences, as accepted by ADR 0001 and ADR 0003.

### Comparison

| Concern | Current GeoCeDG | Current Classic path | Template/reference evidence | Required design gap |
|---|---|---|---|---|
| Product launcher | dedicated GeoCeDG | inherited separate launcher | all archives serialize `app=classic` | preserve both; do not equate workspace with app code |
| Default layout | Graphics + Algebra | inherited Classic defaults | Protocol frequently visible; Properties floating; input bottom | manifest-defined Construction workspace |
| Toolbar | six groups, 19 baseline entries | inherited broad toolbar | 19 groups, including seven custom groups/24 macros | curated professional groups, not wholesale macro import |
| Loaded document layout | takes precedence | takes precedence | carries Template toolbar | explicit “document layout” state and reapply-workspace action |
| Commands | unrestricted Classic set | unrestricted Classic set | heavy command input use | workspace must not filter algebra commands; feature service gates experimental creation independently |
| Intersect tool | baseline types only | same | 209 calls; legacy Locus cannot participate | extend general mode 5 for V2 after G9U0 |
| Locus | legacy mode 47 only | same | 16 legacy Locus nodes | separate experimental V2 creation; never silently redirect mode 47 |
| Menus | inherited plus hard-coded DXF | inherited | legacy scripts add document actions | manifest action placements and localization keys |
| Flags | declarative files only | none | macros carried in document | runtime feature evaluation and consistent command/tool/menu gate |
| Laboratory | explicit external loader | optional `-Classic` route | Template is opt-in legacy evidence | retain as separate experimental route, not stable workspace |
| Help/localization/icons | inherited help; product strings hard-coded; no owned icon set | inherited | contextual help and embedded legacy icons | owned keys/assets, fallback policy, rights gate |
| Persistence | isolated preferences; upstream saved/document perspective | Classic namespace | document toolbar/layout embedded | preference-only active workspace; presentation state never geometric truth |
| Workspace switching | absent | inherited perspective controls | author switches panels manually | schema-v2 workspace registry/controller |

The current GeoCeDG GUI is therefore a valid G2 foundation, not the mature
workspace shown by the references. It already has the correct single manifest
authority and Classic separation, but it lacks multiple named workspaces,
action metadata, runtime feature enforcement, owned localization/icons,
Protocol/Properties defaults, and every public G6-G8 operation.

## Recommendations and boundaries

1. Evolve the application profile to schema version 2 rather than adding a
   second toolbar authority. Define action metadata once and let named
   workspaces reference it.
2. Make `CeDG Construction` the default professional workspace. Show Graphics,
   Algebra, Construction Protocol, bottom input, and contextual help; keep
   Properties readily floatable. Exact panel sizes remain user state.
3. Preserve all algebra commands independently of workspace. A workspace
   changes presentation and action discoverability, never geometry or command
   meaning.
4. Keep the GeoCeDG Classic diagnostic route process-level and isolated. It is
   a menu-visible route, not an in-process workspace that shares preferences.
   When native V2/rich/spatial persistence exists, it preserves those types,
   IDs/tokens/bindings and kernel semantics without downgrade. External
   upstream distributions lacking those types remain an unsupported-open
   boundary.
5. Do not import the 24 macros into the stable toolbar. Replace the three
   sampled-locus tools through G9U0, retain model/presentation tools in the
   Laboratory or user tools, and defer spatial procedures to G9U2.
6. Keep legacy mode 47 and old GGB semantics unchanged. The public V2 surface
   must have a reconstructible evaluator, lifecycle, durable identity, XML
   contract, and runtime flag before any button appears stable.
7. Treat the screenshot as evidence for density and panel availability. Do not
   reproduce embedded upstream icons or exact pixel layout without the asset
   and licensing gates.
8. Keep the eleven professional Construction groups. Add supported
   point-on-Locus creation, rich/guarded length, V2 general intersection and
   token-point materialization as G9U0-backed actions; G9U1 is only their GUI
   client and has no G9B dependency.

The normative, author-approved contracts are
`geocedg/specs/ui/cedg-workspaces.md` and
`geocedg/specs/locus/locus-v2-public-surface.md`. Their approval does not claim
that G9U0/G9U1 has been implemented or authorized.

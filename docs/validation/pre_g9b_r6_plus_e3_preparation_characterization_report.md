# PRE-G9B-R6-plus-E3 characterization and preparation report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE
ACTIVITY                  = PRE-G9B-R6-plus-E3-PREP
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-E3 (native IsoABorder and ExportArea integration)
PRE-G9B-R6-plus-E3        = PREPARED — NOT AUTHORIZED
CHARACTERIZATION          = COMPLETE
IMPLEMENTATION            = NOT AUTHORIZED
PREPARATION CLASS         = DOCUMENTATION_STATUS_ONLY (acceptance STATIC; §K19)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
TEST / REGISTRY CHANGE    = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the canonical
[`E3` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md)
and the
[reconciled design candidate](../architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md).
Those two hold the resulting contract and the requested decisions `DQ-E3-1`
to `DQ-E3-16`; this report does not restate them and carries no authority of
its own. Its machine-readable mirror is
[`pre-g9b-r6-plus-e3-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-preparation-characterization.json).

Path abbreviations: `common/` =
`source/shared/common/src/main/java/org/geogebra/common/`, `geocedg-common/` =
`source/shared/common/src/main/java/org/geocedg/common/`, `desktop/` =
`source/desktop/desktop/src/main/java/org/geogebra/desktop/`, `geocedg-desktop/` =
`source/desktop/desktop/src/main/java/org/geocedg/desktop/`, `dtest/` =
`source/desktop/desktop/src/test/java/org/geocedg/desktop/`, `PROF` =
`apps/geocedg/application-profile.yml`.

Evidence vocabulary: **PROVEN BY PROBE** (scratch probe or scratch script run
on the base, §K2), **PROVEN FROM SOURCE** (read at the base with `path:line`),
**INFERRED** (a consequence of proven facts, not executed).

## K0. Preparation inputs

The author's instruction of 2026-10-09 is the authority of this preparation.
It decides no product question; every open question is a `DQ-E3` row of the
prompt.

- Preserved: `POST-E2-P1`, `P1-R1`, `P1-R2`, `P2`, `P2-R1`, `P3` and `P4` =
  `PASS — AUTHOR APPROVED — PUBLISHED`, with their contracts, evidence and
  candidate identities; `PRE-G9B-R6-plus-E2` = `PASS — AUTHOR APPROVED —
  PUBLISHED`.
- `PRE-G9B-R6-plus-E3` = `NOT AUTHORIZED` on entry; the `POST-E2-P4` closeout
  recorded "documentary readiness preparation" as its only authorized step
  (`docs/validation/post_e2_p4_closeout_record.md:79-81`).
- Authorized: repository and baseline inspection; characterization of the
  GeoGebra and GeoCeDG mechanisms and of the legacy ISO sheet macros;
  reconciliation with the approved unit and export contracts; author-decision
  questions; the canonical implementation prompt; documentary registration and
  the applicable preparation verification.
- Forbidden: `IsoABorder` itself, new kernel commands, XML serialization
  changes, menu actions, `ExportArea` producer changes, legacy macro
  modifications, new drawing-scale behavior, page-layout features, publication,
  and the start of `F1`, `F2`, `F3`, `F4` or `G`.
- The `P0` design candidate is evidence, not authority. Every stale path,
  count and API was revalidated (§K18).

## K1. Entry gate

```text
local HEAD         = 13e08ac1fe1c6c931a88985b97df93d8445812b9
origin/main        = 13e08ac1fe1c6c931a88985b97df93d8445812b9   (after git fetch origin)
live remote main   = 13e08ac1fe1c6c931a88985b97df93d8445812b9   (git ls-remote origin refs/heads/main)
HEAD tree          = dd2ba17c483a18b8931e1848f38616edfa98b06a
worktree           = clean
preparation branch = phase/pre-g9b-r6-plus-e3-prompt (local, unpublished, from 13e08ac1)
```

The roadmap at the base names `PRE-G9B-R6-plus-E3` `NOT AUTHORIZED`, hard
dependency `D1`, the `ExportArea` linkage additionally on `B`, recommended
predecessors `E1` and `E2`, proposed class `INTEGRATED_PHASE`
(`docs/roadmap/geocedg_roadmap.md`, row `PRE-G9B-R6-plus-E3`). `D1`, `B`, `C`,
`E1` and `E2` are `PASS — AUTHOR APPROVED — PUBLISHED`, so every dependency is
satisfied.

## K2. Method, probes and scripts

Static reading at the base of the files cited below, three read-only
characterization passes (export area, scale and units; legacy sheet macros;
the `E2` command precedent), and these scratch-only executions. Nothing was
written to a tracked path.

| Id | Kind | Exit | Purpose |
|---|---|---|---|
| P1 | JUnit `E3PrepBorderProbeTest.p1DragPredicatesPerOutputType` | 0 | inherited drag predicates per output type; polygon fill, area and region; polyline value and XML |
| P2 | JUnit `E3PrepBorderProbeTest.p2PaperSpaceConsistency` | 0 | a model-space ISO rectangle exported through the existing `MANUAL` area and session `drawingScale`: PDF page size, eleven cases |
| P3 | JUnit `E3PrepBorderProbeTest.p3DxfEntityPerRepresentation` | 0 | DXF entity per representation: polygon, closed polyline, segment |
| P4 | JUnit `E3PrepBorderProbeTest.p4LiteralInputPrecisionRoundTrip` | 0 | full-precision double literals as command inputs and as named numerics through `.cedg` save and reopen |
| S1 | PowerShell `e3-binary64.ps1` | 0 | `u` per unit; agreement of four evaluation orders over 616 cases; legacy √2 series against ISO 216 |
| S2 | PowerShell `e3-binary64-orders.ps1` | 0 | six evaluation orders against the binary64 nearest to the exact value, 624 cases |
| S3 | read-only extraction of `Templatev7.ggb` | — | the legacy macro XML, SHA-256 before and after |

The JUnit probe was compiled into the Desktop test source set through a Gradle
init script from the session scratchpad:

```text
gradlew.bat -p <repo> :desktop:desktop:test --tests org.geocedg.desktop.E3PrepBorderProbeTest
            --init-script <scratchpad>/probe.gradle --console=plain      exit 0
```

P1–P3 ran in one execution and P4 in a second one (`--tests …p4LiteralInputPrecisionRoundTrip`),
both exit 0. After each, `:desktop:desktop:compileTestJava` ran without the
init script (exit 0), the probe's result file was copied to the scratchpad
(SHA-256 `258251ba69bc4814b6f13fa76c8f3859b5aeba723bc4f2472b328619447c42e4`
for P1–P3, `413d3a281e92051c3f5f3d993eeb4a5a7f03fc9c1140289b20edd9033d17277a`
for P4) and the result file and the compile-transaction stash copy were
deleted; the build directory holds no probe class and no tracked file changed.

## K3. Governing inputs reconciled

| Input | Status at the base | Use |
|---|---|---|
| `P0` E3 design candidate | `PROPOSED — CANDIDATE`, `P0` evidence | superseded where stale (§K18); kept unchanged |
| mini-track plan §2, §3.1, §8.6, §11, §12, §14 | accepted planning direction | author-fixed scope; §8.6 conversion wording superseded by `unit-system.md` §18.4 (`P0` finding 19) |
| `unit-system.md` v1.0 + amendment 1.1 | `NORMATIVE / AUTHOR APPROVED` | §2 (physical operation), §3.3, §5.3, §6.1–§6.3, §7.3, §12, §13, §17, **§18.4** |
| ADR 0032 | `ACCEPTED — AUTHOR APPROVED` | no live unit dependency of `IsoABorder` (alternatives table; consequences) |
| `B` author decisions `AQ-X1`–`AQ-X7` | author authority | precedence `explicitly activated producer > Export_1/Export_2 > visible viewport`; `ExportArea` `SESSION`; `ISO_A_SELECTION` and `ISO_A_BORDER` delivered by `E3` (`AQ-X7`) |
| `C` author decisions (`DQ-C7`) | author authority | `drawingScale` owner, `1:1` default, reset only on successful document transitions |
| `E1` decisions `DQ-E1-7`, `DQ-E1-8` | author authority | the legacy sheet tools are neither curated nor shipped; the hide requirement is reference evidence for `E3` |
| ADR 0031 | `ACCEPTED — AUTHOR APPROVED` | canonical English head gate, aliases, GGBScript boundary |
| ADR 0034, `native-dimensions.md` 1.1 + P4 §8.6 | `ACCEPTED` / `NORMATIVE` | β precedent: existing output types, no new element type; DXF segment subset |
| `verification-levels.md` §12.8 | in force | class definitions (§K19) |

## K4. ISO 216 sizes against the legacy series (PROVEN BY PROBE S1, S3)

ISO 216 nominal A-series sizes (short × long, millimetres):

| A0 | A1 | A2 | A3 | A4 | A5 | A6 | A7 | A8 | A9 | A10 |
|---|---|---|---|---|---|---|---|---|---|---|
| 841×1189 | 594×841 | 420×594 | 297×420 | 210×297 | 148×210 | 105×148 | 74×105 | 52×74 | 37×52 | 26×37 |

The legacy macros use the unrounded series `long = 100·2^(1/4)·2^(−n/2)`,
`short = 100·2^(−1/4)·2^(−n/2)` (centimetres at scale 1), built with a
two-term `Iteration` that halves the term two places back, with `n` rounded by
`Math.round` (`geogebra_macro.xml` L107, L122 of `Templatev7.ggb`;
`common/kernel/algos/AlgoIteration.java:283-332`). Deviations from ISO 216:

| | A0 | A3 | A4 | A10 |
|---|---|---|---|---|
| legacy (mm) | 840.90 × 1189.21 | 297.30 × 420.45 | 210.22 × 297.30 | 26.28 × 37.16 |
| ISO 216 (mm) | 841 × 1189 | 297 × 420 | 210 × 297 | 26 × 37 |

The legacy result is therefore not the normative size. The native design uses
the ISO 216 integer table (`DQ-E3-3`).

## K5. Legacy sheet macros (PROVEN FROM SOURCE S3; behavioral reference only)

Location: `models/legacy/template-v7/original/Templatev7.ggb`
(SHA-256 `f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`,
48 149 bytes, 24 macros, written by 5.2.879.0; `immutable: true`, rights
`review_status: blocked`, `models/legacy/template-v7/manifest.yml`). The
author copies `artifacts/author-input/g9b-r6-plus/sheetISOland.ggt` and
`sheetISOvert.ggt` (git-ignored) are construction-equivalent once
`<eqnStyle>` tags are ignored. SHA-256 of every opened file was identical
before and after; nothing tracked changed.

| Property | `sheetISOAnLand` (XML L75–1224) | `sheetISOAnVert` (XML L1225–3502) |
|---|---|---|
| tool help | "Left top corner, n, Scale, Margin" | same |
| inputs (ordered) | `D` point, `ISO216n`, `CeDGScale`, `CeDGMargin` (all ordinary inputs; no global lookup) | same |
| outputs | 21: outer `f g h i`; inner frame `n p q r`; title block (10 segments); corners `E G F` | 22: `f'`; frame `q'' n'' p'' r''`; outer `f'' i'' g'' h''`; title block (10); corners `F_2 G_2 H_2` |
| reference corner | `D` = **upper-left**; `E = D + (L, 0)`, `G = D + (L, −S)`, `F = D + (0, −S)`: x increases to the right, y decreases downward in model coordinates | landscape built internally, then `Rotate(·, −90°, D)` and a translation back so that `D` stays upper-left with width = short, height = long |
| size | `Iteration(…)/CeDGScale` (§K4); `CeDGScale` acts as paper-to-model ratio, paper in cm | same |
| outer rectangle | dotted (`type="20"` = `LINE_TYPE_DOTTED`), thickness 2, black, shown | same; `f'` duplicates `h''` |
| inner frame | uniform margin `CeDGMargin/CeDGScale` on all sides (no filing margin), solid, thickness 3 | same |
| title block | lower-right inside the frame, grid scaled by `CeDGlM/42.04` and `CeDGlm/29.73` (A3 constants), no text | translated, **not rotated** |
| corners | `E G F` shown, point size 3 | `F_2 G_2 H_2` shown |
| guards | `round(n) < 0` or an undefined input → undefined; no upper bound; non-integer `n` silently rounded; no guard on scale or margin sign | same |
| `Export_1`/`Export_2` | **not created** by either macro; they belong to the template document and follow its own `ISO216n`/`CeDGScale` objects, not the macro call's literal inputs | same |
| persistence | `<command name="sheetISOAnLand"><input a0="A_{1}" a1="3" a2="1" a3="1"/>` in the g9p reference documents; reopening needs the embedded macro | same |

Author requirement preserved as reference evidence (plan §2, `AQ-G3`,
`DQ-E1-8`): in the final sheet presentation the **outer dotted rectangle and
every corner point are hidden**; the macros cannot hide their input `D`
(`AQ-G3a`). In the legacy presentation the visible content is then the inner
margin frame and the title block. This is the source of `DQ-E3-2` (§K8 of the
design candidate's conflict analysis).

## K6. Unit and scale conversion (PROVEN FROM SOURCE; PROVEN BY PROBE S1, S2, P2)

Normative rule (`unit-system.md` §18.4, §6.3, §12, §13): the **tool** captures
once, at creation, `u = 10^-3 / fb(effC)` model units per millimetre (for
`usm`, `10^-3 / k`) and the `drawingScale` pair `a:b`, as ordinary construction
inputs; the **command** never reads the unit state or `drawingScale`. With
`drawingScale = a:b` meaning *paper length / physical model length = a/b*, a
paper length `P` mm corresponds to the physical model length `P·(b/a)` mm, i.e.

```text
L_model = (P / 1000) / (f(c) · (a / b))  =  P · b / a · u           model units
A3, mm, 1:50:   420 · 50 / 1 · 1 = 21000      297 · 50 / 1 · 1 = 14850
```

The task's expression and the normative capture form are **mathematically
identical**. In binary64 they are not:

| Fact | Evidence |
|---|---|
| `u`: mm `1`, cm `0.1` (binary64 nearest), m `0.001`, inch-`usm` `0.03937007874015748` | S1 |
| 247 of 616 size/unit/scale cases differ in the last bit between four algebraically equal orders; e.g. A0 mm 1:50: `((P·u)·b)/a = 42050`, `((P/1000)·b)/(f·a) = 42049.99999999999` | S1 |
| over 624 cases (4 units × 12 paper lengths × 13 scales), against the binary64 nearest to the exact value: `((P·b)/a)·u` 536 exact, ≤ 1 ulp; `((P·u)·b)/a` 478, ≤ 1 ulp; `(P·u)·(b/a)` 479, ≤ 2 ulp; `((P/1000)·b)/(f·a)` 410, ≤ 2 ulp; `(P·b)/(a·(1000·f))` 511, ≤ 2 ulp; `((P·b)/a)/(1000·f)` 515, ≤ 2 ulp | S2 |
| `P·b` is exact in binary64 for every ISO length and every `b ≤ DrawingScale.MAXIMUM_TERM = 10^9` (`< 2^53`) | `geocedg-desktop/export/DrawingScale.java:27`; INFERRED |
| A3, cm, 1:50 through `((P·u)·b)/a`: `2100 × 1485.0000000000002`; the exported page is still `420.000000 × 297.000000 mm` | P2 |
| `E2` captures its overshoot and gap with a different expression, `mm · 1e-3 / (fb(effC) · a / b)` | `geocedg-desktop/GeoCeDGDimensionTools.java:70-81` |

Consequence: the specification must fix **one canonical expression** and a
declared error bound, and tests must compare against that expression, never
against a decimal literal such as `1485`. `((P·b)/a)·u` is the best measured
order, uses only the captured `u`, and is the order of the `P0` wording
("paper size × denominator ÷ numerator × u") (`DQ-E3-5`). No exactness beyond
binary64 is claimed (`AGENTS.md` §1.8; `unit-system.md` §3.2, §4.1).

## K7. Captured versus live state (PROVEN FROM SOURCE)

| State | Owner at the base | Read by the future command | Read by the future tool |
|---|---|---|---|
| construction unit / `usm` `k` | `DocumentUnitSystem` per construction (`common/kernel/Construction.java:247`, `:283`, `:333-334`), effective function `UnitState.effectiveConstructionMetresPerUnit()` (`geocedg-common/kernel/units/UnitState.java:185-187`, NaN when unspecified) | never (§6.1) | once, to compute `u` (§6.3) |
| session `drawingScale` | `AppGeoCeDG` (`geocedg-desktop/AppGeoCeDG.java:135`, `:531-556`), `DrawingScaleHolder`; `SESSION`, reset to `1:1` at E1–E6 (`:428-429`, `:443-444`, `:460-462`, `:490-493`, `:525-526`, `:952-953`), unchanged by undo (`dtest/PreG9BR6PlusCDrawingScaleTest.java:258`) | never | once, as the default of the scale entry (§18.4) |
| export physical scale | `AppGeoCeDG.getPhysicalExportScale()` = `fb(effC)·100·a/b` cm per model unit (`:588-596`; `geocedg-common/export/PhysicalExportScale.java:42-53`) | never | never |
| viewport zoom | view | never (`AGENTS.md` §10) | never for physical sizes |

Lifecycle consequences (INFERRED from the above and §7.3, §12):

| Event | Border geometry | Session `drawingScale` | Captured `a:b`, `u` |
|---|---|---|---|
| save / reopen | restored from the command inputs | reset to `1:1` | restored (ordinary inputs) |
| undo / redo | restored with the construction | unchanged | restored |
| construction-unit change | **unchanged** (§6.1) | unchanged | unchanged; the border now *represents* a different paper size (`AQ-E3c` = `DQ-E3-6`) |
| session-scale change | unchanged | changed | unchanged; only the coherence indicator reacts (`DQ-E3-12`) |
| reference point moved | translated | unchanged | unchanged |
| redefinition of an input | recomputed from the new input | unchanged | as redefined |

## K8. Representation facts (PROVEN FROM SOURCE; PROVEN BY PROBE P1, P3)

**Inherited drag rule.** A dependent object is dragged by moving its parent's
free input points only if `hasMoveableInputPoints` holds
(`common/euclidian/EuclidianController.java:6777-6870`). For `SEGMENT`,
`LINE`, `RAY`, `TEXT` and conics that rule also requires
`hasOnlyFreeInputPoints`, i.e. **every** input is a free point
(`common/kernel/geos/GeoElement.java:1968-2001`;
`common/kernel/algos/AlgoElement.java:202-204`). For `POLYGON` and `POLYLINE`
it only requires the free input points to be movable (`GeoElement.java:1993-1999`).
Numbers are not points, so an algorithm with inputs `(P, n, …)` and `P` free
has free input points `[P]` (`AlgoElement.java:1097-1117`).

| Probe object | Type | `hasMoveableInputPoints` | free input points |
|---|---|---|---|
| `Segment(P, w)` | SEGMENT | **false** | `[P]` |
| `Segment(P, P+(w,0))` | SEGMENT | **false** | `[P]` |
| `Polygon(P, P+(w,0), P+(w,−h), P+(0,−h))` | POLYGON | **true** | `[P]` |
| its four sides | SEGMENT | false | — |
| `Polyline(P, …, P)` (closed by repeating `P`) | POLYLINE | **true** | `[P P]` |
| `Text("A3 1:50", P+(w,−h))` | TEXT | false | `[]` |

INFERRED: a `GeoPolyLine` or `GeoPolygon` output of an `IsoABorder` algorithm
with a free `P` would be dragged by translating `P`; four `GeoSegment` outputs
would not be draggable at all.

**Polygon semantics.** A default polygon is filled (`alpha = 0.1`,
`isFilled = true`), is a region, and its value is its area (`3.1185E8` for the
A3 1:50 probe). An unfilled polygon is hit only on its boundary
(`common/euclidian/draw/DrawPolygon.java:341-355`).

**Polyline semantics.** A path, not a region; value = length (`71700` for the
probe); XML `<command name="PolyLine">` with `<element type="polyline">`
(existing element type). `Vertex(<GeoPoly>, n)` accepts polylines and polygons
(`common/kernel/commands/CmdVertex.java:72-102`), so corners can be explicit
downstream constructions without being outputs.

**DXF** (`geocedg-common/export/GeoElementGeometryExportAdapter.java:186-212`;
P3):

| Representation | DXF |
|---|---|
| polygon | one **closed** `LWPOLYLINE`, 4 vertices; sides dropped as duplicates when exported with it (`:326-344`) |
| polyline closed by repetition | one **open** `LWPOLYLINE`, 5 vertices (last = first) |
| segment | one `LINE` each |
| text | excluded (`GeometryExportPopulation2D.java:109-110`) |

LaTeX dispatch handles `GeoPolyLine` and `GeoPolygon`
(`common/export/pstricks/GeoGebraExport.java:385-391`); picture export draws
them through the shared drawables.

**E2 β pattern** (`geocedg-common/kernel/dimension/AlgoNativeDimension.java`):
fixed `setOutputLength`/`setOutput` without an `OutputHandler` (`:153-163`);
segment end points and the text anchor are internal unlabelled `GeoPoint`s
(`:101-107`, `:117-125`); one label names the primary output and the others get
default labels (`:139-145`); the text is `setIsTextCommand(true)`,
`setStartPoint(anchor)`, `setAlwaysFixed(true)` (`:99-112`); undefined states
through `setUndefined()` (`:186-237`). Regular polygons use three
`OutputHandler`s instead (`common/kernel/algos/AlgoPolygonRegularND.java:51-55`,
`:104-216`). No `Rectangle` command exists (`common/kernel/commands/Commands.java:138-140`).

## K9. Persistence and identity (PROVEN FROM SOURCE; P0 probe P6; E2 probe P5)

- A command with existing output types serializes as an additive
  `<command name="…"><input …/><output …/></command>` plus ordinary
  `<element>` records; the `E2` test asserts that only existing element types
  appear, that the presentation string is never serialized and that
  save/reopen is byte-identical
  (`source/shared/common-jre/src/test/java/org/geocedg/common/dimension/PreG9BR6PlusE2NativeDimensionTest.java:562-573`).
- Captured values survive persistence bit-exactly in either form (P4): for
  `0.03937007874015748`, `0.1`, `1/3`, `1485.0000000000002`,
  `16.53543307086614`, a command-input literal is written as the shortest
  round-trip decimal (`<input a0="(0, 0)" a1="0.03937007874015748"/>`), a named
  numeric as `exp` and `<value val>` with the same digits, and after `.cedg`
  save and reopen both reproduce the original binary64 exactly.
- A build that does not know a command drops its outputs and their dependents
  with a load error (`E2` preparation report `K16`, probe P5). This is the
  forward-compatibility limitation `E3` inherits.
- Ordinary objects have no persistent identity: after undo, redo, a rebuilding
  redefinition or reopen the object is a different Java object, and
  `Construction.isInConstructionList` still answers `true` for the stale
  object (`P0` report §7 P6; `common/kernel/Construction.java:1295-1300`).
- Output identity of an algorithm is its **output index**, assigned in
  `setInputOutput`; labels are assigned afterwards and are not identity
  (`AlgoNativeDimension.java:134-151`).

## K10. `ExportArea` at the base (PROVEN FROM SOURCE)

- Authority: `geocedg-desktop/export/ExportAreaSession.java:30`; precedence in
  `resolve` (`:65-79`): an explicit `MANUAL` bound to the view, else
  `Export_1`/`Export_2`, else the visible viewport; the selection rectangle
  never counts (`:18-29`).
- Producers: `ExportAreaSession.Producer` = `EXPORT_POINTS`, `MANUAL` only
  (`:31-37`, comment "Producers implemented by B; ISO A producers belong to
  E3"); `ExportArea.Source` = `MANUAL`, `EXPORT_POINTS_EXPLICIT`,
  `EXPORT_POINTS_AUTOMATIC`, `VISIBLE_VIEWPORT`
  (`geocedg-desktop/export/ExportArea.java:19-28`). No Java source or test
  names `ISO_A_SELECTION`, `ISO_A_BORDER` or `IsoA`.
- A second producer enum exists for DXF:
  `geocedg-common/export/GeometryExportArea.java:27-36`, mapped by
  `geocedg-desktop/GeoCeDGDxfExportController.java:151-179`, whose manifest
  writes `resolved_producer` (`geocedg-desktop/export/DxfFidelityManifestWriter.java:194-195`).
- The session holds **no `GeoElement`** (`ExportAreaSession.java:39-43`);
  `Export_1`/`Export_2` are looked up by label at every resolve (`:188-205`).
  There is **no rebuild hook**: undo and redo leave the session untouched
  (`dtest/PreG9BR6PlusBExportSurfaceTest.java:245-249`), and no listener tells
  it that the construction was rebuilt.
- Lifecycle: reset on New (`AppGeoCeDG.java:958-963`) and after a successful
  Open (`:983-986`, `:996-999`); `isExplicitProducerUnavailable` (`:86-94`) has
  no caller.
- File actions `export.area.define-rectangle`, `.use-export-points`, `.show`,
  `.clear` (`PROF:2351-2417`; `geocedg-desktop/GeoCeDGActionRegistry.java:348-361`);
  `MANUAL` is tied to Graphics 1 (`AppGeoCeDG.java:804-822`); manual entry UI
  `geocedg-desktop/GeoCeDGExportAreaPrompt.java:24-99`; overlay drawn in
  `GeoCeDGEuclidianView.java:48-72`, never exported.
- Consumers: picture PNG/PDF/SVG/EMF(+), save preview, clipboard
  (`geocedg-desktop/export/PictureExportService.java:108-365`), print preview
  (`desktop/euclidian/EuclidianViewD.java:379-385`), `--export`, API and
  `ExportImage` (`desktop/plugin/GgbAPID.java:210-236`), LaTeX bounds
  (`AppGeoCeDG.java:689-722`), DXF (`:677-683`).

## K11. `drawingScale` at the base (PROVEN FROM SOURCE)

- Value type `geocedg-desktop/export/DrawingScale.java`: gcd-normal form,
  positive terms ≤ `10^9` (`:27`, `:51-67`), `parse("a:b")` (`:76-107`).
- Presets 1:1, 1:2, 1:5, 1:10, 2:1, 5:1 (`:31-34`); **1:20, 1:50, 1:100 are not
  presets**. Every export dialog uses them through `DrawingScaleControl`
  (`geocedg-desktop/export/DrawingScaleControl.java:22-125`), the only setter UI.
- The status-bar segment is display-only and shows `a:b` or
  `Units.Status.ScaleNonPhysical` (`geocedg-desktop/GeoCeDGStatusBar.java:38-52`,
  `:173`, `:246-252`).

## K12. Paper-space consistency (PROVEN BY PROBE P2)

A model-space rectangle computed with the captured `u` and `((P·u)·b)/a`,
exported through the existing `MANUAL` producer and session `drawingScale`:

| Case | Model size | Session | PDF page (mm) |
|---|---|---|---|
| A3 landscape, mm, 1:50 | 21000 × 14850 | 1:50 | **420.000000 × 297.000000** |
| same border | | 1:100 | 210.000000 × 148.500000 |
| same border | | 1:1 | 21000.000000 × 14850.000000 |
| A3 landscape, cm, 1:50 | 2100 × 1485.0000000000002 | 1:50 | 420.000000 × 297.000000 |
| A3 landscape, m, 1:50 | 21 × 14.85 | 1:50 | 420.000000 × 297.000000 |
| A4 portrait, cm, 1:1 | 21 × 29.700000000000003 | 1:1 | 210.000000 × 297.000000 |
| A4 portrait, mm, 2:1 | 105 × 148.5 | 2:1 | 210.000000 × 297.000000 |
| A4 portrait, mm, 1:10 | 2100 × 2970 | 1:10 | 210.000000 × 297.000000 |
| A4 portrait, inch `usm`, 1:2 | 16.535433… × 23.385826… | 1:2 | 210.000000 × 297.000000 |
| A0 portrait, mm, 1:1 | 841 × 1189 | 1:1 | 841.000000 × 1189.000000 |
| A10 landscape, m, 1:100 | 3.6999999999999997 × 2.6 | 1:100 | 37.000000 × 26.000000 |

Conclusions: when the session scale equals the captured scale, the existing
`B`/`C` pipeline already yields the exact ISO page (to six decimals) for every
unit, including `usm`; at a different session scale the page is the border
scaled by `session/captured`, so a scale mismatch is **silent** at the base.
That is the defect the transient indicator of `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`
addresses. Exporting a 1:50 border at the reset scale `1:1` gives a 21 × 14.85 m
page; it did not raise `PhysicalExportLimitException` in this case.

## K13. Label and indicator facts (PROVEN FROM SOURCE)

- The only command-owned `GeoText` output today is `E2`'s dimension text
  (§K8); its string depends on the unit state through a presentation listener
  (`geocedg-common/kernel/dimension/DimensionPresentation.java:70-90`). A label
  `A3 — 1:50` depends only on `n`, `a` and `b`, which are construction inputs,
  so it needs no unit listener (INFERRED).
- `GeoText` is excluded from DXF (§K8); text size in PDF is not physical
  (`TD-PDF-1`, owned by the planned `F4`, `post_e2_p4_closeout_record.md`).
- Non-ASCII in non-test Java sources is written with `\u` escapes in this
  repository (the em dash is `—`).
- No existing GeoCeDG indicator compares two scales. Available surfaces: the
  display-only status-bar scale segment (§K11), the export dialogs' scale
  presentation (`geocedg-desktop/GeoCeDGExportScalePresentation.java:50-113`),
  and the non-exported view overlay (§K10).

## K14. Product surface (PROVEN FROM SOURCE)

- Group `construction-annotations-media` at `PROF:3527-3532`
  (`action_ids: ["presentation.image"]`, `toolbar_action_ids: []`), rendered as
  a Construction submenu (`PROF:3666`; `geocedg-desktop/GeoCeDGMenuBar.java:97-103`);
  it is not a toolbar group (`PROF:3570-3582`). `presentation.image` is an
  `upstream-mode` (`PROF:1878-1896`) in cluster `presentation-visibility-style`
  (`PROF:2954-2968`).
- Action catalog: **127** actions (upstream-mode 73, product-action 17,
  document-action 7, preference-action 7, help-route 6, navigation-action 5,
  view-toggle 5, materialization-action 3, diagnostic-route 2,
  result-inspector 1, workspace-switch 1). Pins: `geocedg-desktop/GeoCeDGProfile.java:340-345`;
  `dtest/G9U1ActionRegistryTest.java:65`; `dtest/G9U1ProfileCompilerTest.java:44`,
  `:211`, `:383`; `dtest/G9U1WorkspaceSurfaceTest.java:124`, `:703`;
  `dtest/GeoCeDGProfileTest.java:45`; `dtest/PreG9BR6PlusA1LayerWorkspaceTest.java:450`;
  `dtest/PreG9BR6PlusE1LCuratedLibraryTest.java:380`;
  `dtest/PreG9BR6PlusE2NativeDimensionDesktopTest.java:423`.
- Modes: last id `MODE_LINEAR_DIMENSION = 143`
  (`common/euclidian/EuclidianConstants.java:470-472`); a new mode would be 144
  (INFERRED). A menu-only mode is an `upstream-mode` action outside
  `toolbar_action_ids`, as `presentation.image`.
- Dialog patterns: `GeoCeDGDocumentUnitsPrompt.java:26-141` (injectable,
  GridBag, OK/Cancel), `GeoCeDGWorkingLayerChooser.java:18-47` (fixed list),
  `DrawingScaleControl` (embeddable panel), yes/no in
  `GeoCeDGDxfExportController.java:322-324`; click-then-prompt in
  `GeoCeDGSimilarityTools.java:91-100`; mode-with-dialog `MODE_WORKING_LAYER`
  (`GeoCeDGEuclidianController.java:625-631`).
- Unspecified unit: **no "declare a unit first" pattern exists**; the reusable
  gate is `GeoCeDGActionRegistry.unavailableReason()` (`:183-218`, disables and
  explains, `:160-170`, `:224-230`), and `AppGeoCeDG.openDocumentUnits()`
  (`:234-240`) opens the Units dialog.

## K15. Command-surface gates (PROVEN FROM SOURCE)

- ADR 0031 pins `566 / 515 / 488` (`Commands.values()`, heads, displayed) in
  `dtest/PreG9BR5BCanonicalCommandGateTest.java:67-71`; one new displayed
  command makes them `567 / 516 / 489` (INFERRED); the R5-B base fingerprints
  (`source/desktop/desktop/src/test/resources/org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-base-fingerprints.json`)
  are regenerated, as in `E2`.
- Registration points used by `E2`: `Commands.java:154-158`,
  `CommandDispatcher.java:566-567`, `BasicCommandProcessorFactory.java:160-163`,
  `command.properties:513-516`, `command_en.properties:282-285`,
  `command_es.properties:501-504`, `menu*.properties` `X.Tool`/`X.Help`;
  `SelfTest` requires `GeoCeDGCommandsTest.cmd<Name>`.
- GGBScript matrix `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`:
  six rows per syntax line (3 surfaces × 2 locales;
  `PreG9BR6CapabilityMatrix.java:68-71`, `:426-441`) and 14 probes per
  command; completeness gate `PreG9BR6CapabilityMatrixTest.java:45-48`; role
  derived from `docs/upstream/modified-files.yml` (`PreG9BR6CapabilityMatrix.java:257-327`);
  hash chain `verification-static-contracts.json:32` →
  `verification-registry.json:7`.

## K16. Names (PROVEN FROM SOURCE)

`IsoABorder`, `MarcoISOA`, `BordeISOA` and `HojaISOA` are free: absent from
`Commands.java`, `command.properties` and `command_es.properties`, and none
equals, case-insensitively, any of the 24 `Templatev7` macro names
(`CirclebyD`, `DuctSymbol`, `EllipseAxis`, `IFPositiveSelectPoint`,
`Perimeter`, `PoliLineVisibility`, `SplineLength`, `SquarebyDiagonal`,
`SymmSymbol`, `axisDimension`, `circArcbyAngle`, `conj2mainAxesEllipse`,
`directDimension`, `dummyRotate`, `ellipseLength12`, `ellipseVisibility`,
`listLength`, `listLength12`, `pointJump`, `postLocus`, `relCoor`,
`sheetISOAnLand`, `sheetISOAnVert`, `translationCoor`). The macro-before-native
shadowing of `E2` (`OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`)
therefore does not reach these names.

## K17. Verification registration at the base (PROVEN FROM SOURCE)

- `geocedg/specs/operations/verification-registry.json`: 58 phase selections,
  none for `E3` (last entries `POST-E2-P2`, `P2-R1`, `P3`, `P4`,
  `VERIFICATION-INFRASTRUCTURE`); `geocedg/specs/operations/verification-junit-inventory.json`:
  51 selections. `E2`'s registration (`verification-registry.json:77`,
  `:597-615`; inventory `pre-g9b-r6-plus-e2.shared` / `.desktop`) is the
  template; registration goes through the official updater and changes the
  infrastructure pins, which `E3` reconciles through the official mechanism.
- New source files are listed as `added` in `docs/upstream/modified-files.yml`;
  touched upstream files as `modified` with the phase in `purpose`.

## K18. Stale facts in the earlier candidates

| Source | Stale statement | Fact at the base |
|---|---|---|
| `P0` E3 candidate §5 | `construction-annotations-media` at `application-profile.yml:3288-3292` | `PROF:3527-3532` |
| `P0` E3 candidate §5 | catalog pin +2 (from the `P0` count) | 127 today; +2 → 129, +1 → 128 |
| `P0` E3 candidate §3 | outputs: four segments | four segments are not draggable through `P` (§K8); the outputs are re-opened as `DQ-E3-1`/`DQ-E3-2` |
| `P0` E3 candidate §3 | "model units per millimetre" input with no evaluation order | binary64 order matters (§K6) |
| `P0` E3 candidate §4 | the link is dropped on rebuild | true, but the session has no rebuild signal and no `GeoElement` field (§K10); detection is an implementation obligation |
| plan §8.6 | "model length = paper length × scale denominator ÷ conversion(constructionUnit → mm)" | superseded by `unit-system.md` §18.4: a captured `u`, never a live unit read (`P0` finding 19) |
| B design candidate §3 | `ISO_A_BORDER` "derived live while the link is valid" | still the design direction; no code exists |
| P0 inventory `tool-inventory.yml` | macro types such as `D` = `conic` | wrong; see `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` (§K20) |

## K19. Preparation class

The preparation delta (§K21) is documentation, a JSON evidence mirror under
`geocedg/validation/`, an uncatalogued phase prompt and two status files. No
product, test, build, registry, verifier, profile, schema or serialization path
changes. `geocedg/specs/operations/prompt-contracts.json` catalogues only the
task template, the change review and the author-direct prompts, so a phase
prompt is not a prompt-contract or verifier path. The class is therefore
`DOCUMENTATION_STATUS_ONLY` (`verification-levels.md:1229`): `STATIC` only, no
`FINAL`, as for the `E2` preparation. The phase prompt is additionally checked
with the prompt-contract parser directly (`Test-PromptContractDocument`,
profile `task`), because `STATIC` does not cover uncatalogued prompts.

## K20. Observations (proposed identifiers; none is fixed here)

| Identifier | Status | Owner proposed |
|---|---|---|
| `OBS-R6PLUS-E3-SILENT-SCALE-MISMATCH` | at the base, exporting a model-space sheet at a session scale other than the one it was drawn for changes the page size silently (§K12) | `E3` (`ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE`, `DQ-E3-12`) |
| `OBS-R6PLUS-E3-EXPORT-AREA-NO-REBUILD-SIGNAL` | `ExportAreaSession` has no rebuild notification and no element field; a live `ISO_A_BORDER` link needs a staleness test that uses no label, index or position inference (§K10) | `E3` (`DQ-E3-9`) |
| `OBS-R6PLUS-E3-LEGACY-HIDE-REQUIREMENT-VERSUS-BORDER-ONLY` | the `P0` "border only" recommendation (`AQ-E2`) and the author requirement to hide the outer dotted rectangle cannot both hold if the trim rectangle is the only output (§K5) | author (`DQ-E3-2`) |
| `OBS-R6PLUS-E3-UNIT-FACTOR-ORDER` | `E2` and the normative `E3` capture use different binary64 expressions for the same physical conversion (§K6) | `E3` documents it; `E2` unchanged |
| `OBS-R6PLUS-E3-LEGACY-INVENTORY-CASE-FOLDED-TYPES` | `models/legacy/template-v7/derived/tool-inventory.yml` records wrong types (e.g. `D` as `conic`) because `tools/legacy/ingest.ps1:282-285` keys a case-insensitive `@{}` by label | separate activity (not `E3`); suggested as a separate task |
| `OBS-R6PLUS-E3-NO-UNIT-GATE-PATTERN` | no Desktop action is gated on a physical unit today (§K14) | `E3` (`DQ-E3-7`) |

## K21. Paths changed and product-effect proof

Tracked delta of the preparation candidate (documentation, evidence and prompt
only):

```text
A  .github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md
A  docs/architecture/pre_g9b_r6_plus_e3_iso_a_border_reconciled_design_candidate.md
A  docs/validation/pre_g9b_r6_plus_e3_preparation_characterization_report.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-preparation-characterization.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md      (status line and §14 block)
M  docs/roadmap/geocedg_roadmap.md                          (version, E3 row)
```

No path under `source/`, `apps/`, `tools/`, `geocedg/specs/`, `models/`,
`packaging/` or `docs/upstream/` changes; no registry, inventory, verifier,
profile, icon or serialization file changes; `Templatev7.ggb` and its macro
definitions are byte-identical; the `P0` E3 design candidate is unchanged.

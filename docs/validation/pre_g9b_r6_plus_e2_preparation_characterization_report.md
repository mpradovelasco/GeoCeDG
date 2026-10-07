# PRE-G9B-R6-plus-E2 characterization and preparation report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE
TECHNICAL_CANDIDATE_STATE = FROZEN with the preparation candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-E2 (native dimensions)
PRE-G9B-R6-plus-E2        = PREPARED — NOT AUTHORIZED
CHARACTERIZATION          = COMPLETE
IMPLEMENTATION            = NOT AUTHORIZED
FINAL.DESKTOP BASELINE    = CLEAN (unchanged; no product, test or registry change)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the canonical
[`E2` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md)
and the
[reconciled design candidate](../architecture/pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md).
Those two hold the resulting contract and the decisions `DQ-E2-1` to
`DQ-E2-11`; this report does not restate them and carries no authority of its
own. Its machine-readable mirror is
[`pre-g9b-r6-plus-e2-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-preparation-characterization.json).

Path abbreviations: `common/` =
`source/shared/common/src/main/java/org/geogebra/common/`, `geocedg-common/` =
`source/shared/common/src/main/java/org/geocedg/common/`, `desktop/` =
`source/desktop/desktop/src/main/java/org/geogebra/desktop/`, `geocedg-desktop/` =
`source/desktop/desktop/src/main/java/org/geocedg/desktop/`, `props/` =
`source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/`,
`dtest/` = `source/desktop/desktop/src/test/java/org/geocedg/desktop/`.

Evidence vocabulary: **PROVEN BY PROBE** (scratch probe run on the base, §2),
**PROVEN FROM SOURCE** (read at the base with `path:line`), **INFERRED** (a
consequence of proven facts, not executed).

## 0. Preparation inputs

The author's instruction of 2026-10-07 is the authority of this preparation.
It is recorded here, not in a separate author-decision record, because it
decides no product question; every open question is a `DQ-E2` row of the
prompt.

- Preserved: `PRE-G9B-R6-plus-E1`, `E1-X1` and `E1-P-X1` =
  `PASS — AUTHOR APPROVED — PUBLISHED`; `FINAL.DESKTOP` = `CLEAN`.
- `PRE-G9B-R6-plus-E2` = `NOT AUTHORIZED` on entry; this task authorizes
  characterization, design reconciliation and preparation only.
- Frozen premise: the semantics of `DirectDimension` and `AxisDimension` belong
  to the shared Java kernel; Desktop owns tool orchestration, initial parameter
  capture, drag interaction, product placement, icons and help.
- The `P0` design candidate is evidence, not authority; every stale path, count
  and API was revalidated (`K1`, design candidate §10).
- Forbidden: any product implementation (kernel algorithms, commands,
  registration, Desktop tools, icons, profile actions, GGBScript rows,
  serialization, export behavior), publication, merge, tag, release, and the
  start of `E3`, `F1`, `F2`, `F3` or `G`.

## 1. Entry gate

```text
local main        = dc63b0e55f12eb96d3aea359db4476cbda6c89dc
origin/main       = dc63b0e55f12eb96d3aea359db4476cbda6c89dc
live remote main  = dc63b0e55f12eb96d3aea359db4476cbda6c89dc   (git ls-remote origin refs/heads/main)
HEAD tree         = 8b2b5027a2119f996de8ca5af91b03ce32731a8c
worktree          = clean
preparation branch = phase/pre-g9b-r6-plus-e2-prompt (local, unpublished)
```

The roadmap at the base names `PRE-G9B-R6-plus-E2` as the next phase,
`NOT AUTHORIZED` without a separate author instruction, proposed class
`INTEGRATED_PHASE` (`docs/roadmap/geocedg_roadmap.md`, row `PRE-G9B-R6-plus-E2`).

## 2. Method and scratch probes

Static reading at the base of the files cited below, plus five scratch-only
JUnit probes kept in the session scratchpad and compiled into the Desktop test
source set through a Gradle init script (never in a tracked path):

```text
gradlew.bat -p <repo> :desktop:desktop:test --tests org.geocedg.desktop.<Probe>
            --init-script <scratchpad>/probe.gradle --console=plain
```

| Probe | Class | Exit | Purpose |
|---|---|---|---|
| P1 | `E2PrepKernelProbeTest` | 0 | `Dimension`, name lookups, line directions, text alignment and rotation, document-macro shadowing |
| P2 | `E2PrepBetaFigureProbeTest` | 0 | β figure from existing commands: text anchoring under zoom, XML round trip of arrow endings, SVG, PGF, PSTricks, Asymptote, DXF |
| P3 | `E2PrepAxisProbeTest` | 1 | axis and `Ray(A,v)` directions (recorded); a trailing lookup of the reserved label `z` failed with a `NullPointerException` after the direction lines were printed; that line carries no evidence |
| P4 | `E2PrepUserLibraryProbeTest` | 0 | a stored user tool whose name becomes a native command name |
| P5 | `E2PrepUnknownCommandProbeTest` | 0 | a build that does not know a command loading a document that uses it |

The probes changed no tracked file. Their compiled classes and result files
were removed from the build directories after the run (§K20).

## K1. Governing inputs reconciled

| Input | Status at the base | Use |
|---|---|---|
| `P0` E2 design candidate | `PROPOSED — CANDIDATE`, `P0` evidence | superseded where stale (design candidate §10); kept unchanged |
| mini-track plan §8.5 | accepted planning direction | 115-action pin and group lines stale; kernel placement, offset, value and placement-in-UI directions confirmed |
| `unit-system.md` v1.0 | `NORMATIVE / AUTHOR APPROVED` | §5.3 conversion, §6.1–§6.3 invariant, §14 presentation, §17 matrices, §18.3 obligations |
| ADR 0031 | `ACCEPTED — AUTHOR APPROVED` | canonical English head gate (decision 5), shadowing (6), alias regimes (7), GGBScript boundary (10), GeoCeDG commands (12) |
| ADR 0032 | `ACCEPTED — AUTHOR APPROVED` | decision 2 (no live geometric dependency) |
| `verification-levels.md` §12.8 | in force | class definitions (`INTEGRATED_PHASE` = PHASE, plus INTEGRATION when the frozen plan names concrete integration coverage; `GLOBAL_IMPACT` = FINAL) |

## K2. The upstream `Dimension` command (PROVEN FROM SOURCE, PROVEN BY PROBE)

- `Commands.Dimension(TABLE_VECTOR)` (`common/kernel/commands/Commands.java:757`),
  the only constant containing "Dimension"; no alias in `englishToInternal`
  (`:1287-1373`); dispatched by `AdvancedCommandProcessorFactory.java:271-272` to
  `CmdDimension` (`common/kernel/advanced/CmdDimension.java:35-73`), one
  argument, `AlgoDimension` only.
- Semantics (`common/kernel/advanced/AlgoDimension.java:51-132`): list → size;
  matrix (decided once at construction) → `{rows, cols}`; 2D point or vector →
  `2`; 3D → `3`; undefined input → undefined. CAS: `Ggb2giac.java:264-265`.
- P1: `Dimension({1,2,3})` = 3; `Dimension({{1,2,3},{4,5,6}})` = `{2, 3}`;
  `Dimension((1,2))` = 2; `Dimension((1,2,3))` = 3; `Dimension(Vector((3,4)))` = 2;
  `Dimension({})` = 0; `Dimension("abc")` → "Illegal argument … Syntax:
  Dimension( <Object> )".
- Bundles: `props/command.properties:208-209` (`Dimension`, `[ <Object> ]`),
  `props/command_es.properties:203-204` (`Dimensión`, `[ <Objeto> ]`).
- **It is the dimension (cardinality) of matrices, lists, points and vectors and
  is unrelated to drawing dimensions.** No `E2` registration may alter it, its
  alias or its syntax. One observable side effect is expected: autocomplete
  matches substrings (`geocedg-common/main/command/CanonicalCommandSurface.java:140-148`),
  so typing `Dimension` will also list the new commands.

## K3. Command inventory, names and registration path

- **Names are free.** No `Commands` constant, no command bundle key or value in
  any locale and no parser function equals, case-insensitively,
  `DirectDimension`, `AxisDimension`, `CotaDirecta`, `CotaAxial`, `CotaEjes`,
  `CotaAlineada`, `CotaHorizontal`, `Cota`, `DimensionDirecta`,
  `DimensionAxial`, `AcotacionDirecta`, `AlignedDimension`, `LinearDimension`,
  `CotaLineal`, `CotaEje` or `CotaSegunEje` (searches over `Commands.java`,
  every `props/command*.properties`, all tracked `*.java`, the parser-function
  tables). P1: `lookupInternal`, `stringToCommand` and `getReverseCommand` return
  null for the candidate names; `DirectDimension((0,0),(1,0))` → "Unknown command".
- **Registration path** (precedent `LocusV2`/`LocusLength` in `f5904c613`,
  `SplineV2` in `de33f3a80`): enum constant in `Commands.java` (`LocusV2` `:150`,
  `SplineV2` `:355-356`); `case` in `CommandDispatcher.commandTableSwitch`
  (`:564-565`, `:633`) returning the basic factory (`:718`); processor creation
  in `BasicCommandProcessorFactory.java:156-159`, `:304-305`; a `Cmd*` class and
  algorithm classes; `command.properties` / `command_en` / `command_es` name and
  `.Syntax` entries (`:507-512`, `:276-281`, `:495-500`); upstream-modification
  records in `docs/upstream/modified-files.yml`. Autocomplete, Input Help, the
  editor lexer and syntax help pick a new visible constant up automatically
  (`common/main/App.java:834-891`; GeoCeDG consumers listed in the JSON).
- **No GeoCeDG command allow-list exists.** The profile filter is
  `RuntimeFeatureService::isCommandVisible`
  (`geocedg-common/main/settings/config/AppConfigGeoCeDG.java:133-136`), which
  hides only the three Locus V2 family commands when creation is disabled
  (`geocedg-common/main/feature/RuntimeFeatureService.java:85-88`, `:134-137`).
  `E2` commands are not Locus V2 commands and need no feature gate.
- **ADR 0031 gate pins** (`dtest/PreG9BR5BCanonicalCommandGateTest.java:68-70`):
  564 constants, 513 with an English name, 486 displayed. Two visible
  constants with English names make them 566, 515 and 488. The R5-B fingerprint
  fixture (`dtest/../resources/org/geocedg/desktop/pre-g9b-r5-b/pre-g9b-r5-b-base-fingerprints.json`)
  pins `commandNames.en` for every constant, the token universe, reverse tables
  (EN 532/557, ES 983/1029 entries), syntax listings, dictionaries and
  resolutions; it must be regenerated by its documented route, and the tests
  reading it (`PreG9BR5BCanonicalDisplayTest.java:91`, `:101`, `:177-190`;
  `PreG9BR5BR1RuntimeLanguageSwitchTest.java:147`) move with it. `SelfTest`
  (`source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/SelfTest.java:30-55`)
  requires a `cmdDirectDimension` and `cmdAxisDimension` test method;
  `SyntaxLocalizationTest` requires non-empty syntax; `CommandsValidationTest`
  validates argument counts and types automatically.
- **Pre-existing doc staleness** (not `E2` scope): the `modified-files.yml`
  purposes of `Commands.java`, the factory and the dispatcher (`:2599-2611`) and
  of the command bundles (`:2683-2695`) do not mention the earlier `SplineV2`
  lines.

## K4. GGBScript capability matrix

- Matrix `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`
  (168 rows, 152 lookup probes, 10 fixtures, 14 form exclusions, 3 processor
  exclusions); schema `geocedg/specs/operations/ggbscript-capability-matrix.schema.json`;
  schema-only checker `tools/agent/checks/ggbscript-capability-matrix.ps1`; the
  fail-closed completeness gate is JUnit:
  `dtest/PreG9BR6CapabilityMatrixTest.java:41-75`, logic in
  `dtest/PreG9BR6CapabilityMatrix.java:257-331`, `:365-504`.
- Inventory rule: a processor class counts when `modified-files.yml` records it
  `added`/`modified` under `/src/main/java/` and its source contains
  `"org.geocedg."` (`:81`, `:285-287`). New GeoCeDG `Cmd*` classes are therefore
  `GEOCEDG_ADDED` and the pinned derived inventory
  (`PreG9BR6CapabilityMatrixTest.java:45-47`, 11 commands) fails
  `MISSING_COVERAGE` until both commands are covered — the gate stays fail
  closed.
- Required per command: for every syntax line, six rows
  `Cmd#i|{ALGEBRA_INPUT,GGBSCRIPT,GGBSCRIPT_EXECUTE}|{en,es}` (added commands
  may not use form exclusions, `:417-418`); lookup probes: six `CASE_VARIANT`,
  plus eight alias probes when the Spanish name differs from the English head;
  a `@Test <cmd>Rows` method bound by name (`:973-981`); fixtures as needed.
- Counts (prompt `DQ-E2-2`, `DQ-E2-10`): with the recommended signatures and
  separate line and vector syntax lines, `DirectDimension` has 2 forms and
  `AxisDimension` 4 → **+36 rows (204)**; with distinct Spanish aliases
  **+28 probes (180)**. With the alternative short forms, 3 + 6 forms → +54 rows
  (222). Then re-pin the matrix hash in `verification-static-contracts.json:30-33`,
  the static-contracts catalog pin (`verification-registry.json:7`) and the
  JUnit selections that contain `PreG9BR6CapabilityMatrixTest`
  (`pre-g9b-r6.desktop` 54, `pre-g9b-r6-plus-b.desktop` 241,
  `pre-g9b-r6-plus-c.desktop` 269, `final.desktop` 1874, `discovery.desktop` 1880;
  pins in `tools/agent/tests/verification-final-coverage.Tests.ps1`).

## K5. Application profile and pins

- `apps/geocedg/application-profile.yml` (JSON content): **125 actions**, 18
  clusters, 30 presentation groups, 12 toolbar groups, 7 menu sections, 572
  localized-text keys. The `P0` pin of 115 is stale.
- `construction-metrics` (`:3475-3480`), name `Group.Construction.Metrics`
  (EN "Metrics and validation", ES "Métricas y validación", `:3722`): actions
  angle, distance-length, locus total, locus partial, area, slope; toolbar:
  the first four. Cluster `metric-validation-tools` (`:2852-2873`). Construction
  menu entry 10 of 13 (`:3614-3628`).
- `measure.distance-length` (`:1566-1583`): `upstream-mode`, `MODE_DISTANCE`/38,
  `localization_ref`/`icon_ref` `upstream.mode.Distance`, text from
  `props/menu.properties:541-542` and `props/menu_es.properties:519-520`.
- Placement rules (`geocedg-desktop/GeoCeDGProfile.java:350-470`): one cluster,
  exactly one presentation group, toolbar consistency; `upstream-mode` actions
  must name an existing `EuclidianConstants` mode (`:308-317`); non-mode actions
  need `localized_text` `name/short_help/long_help/status/error` in EN and ES
  (`:319-322`, contract `:401-414`).
- Next free mode ids: 142 and 143 (`common/euclidian/EuclidianConstants.java`,
  `MODE_WORKING_LAYER` = 141 at `:468`).
- Pins that move with two new toolbar actions: `GeoCeDGProfile.java:340-345`
  (125); `dtest/G9U1ProfileCompilerTest.java:44`, `:54-57` (47 native toolbar
  modes, 71 mode actions), `:211`, `:383`; `dtest/G9U1ActionRegistryTest.java:65`;
  `dtest/G9U1WorkspaceSurfaceTest.java:124-125`, `:370-405`
  (`construction-metrics` toolbar contents `:396-398`), `:701` (57), `:702`;
  `dtest/GeoCeDGProfileTest.java:45`; `dtest/PreG9BR6PlusA1LayerWorkspaceTest.java:450`;
  `dtest/PreG9BR6PlusE1LCuratedLibraryTest.java:380`; the approved-amendment
  allow-list `tools/agent/workspace-profile-validation.ps1:36-80` (precedents
  R4, A-1, B, D1) and the static-contract input pin it feeds; the prose count in
  `geocedg/specs/ui/cedg-workspaces.md:459`.
- Guides: the Construction table rows (EN `docs/user/geocedg_user_guide_en.md:197`,
  ES `docs/user/geocedg_user_guide_es.md:204`) and §9 Lengths and measurements
  (EN `:1043`, ES `:1089`); a new level-3 section moves the guide outline pins
  (`dtest/PreG9BR5AGuideOutlineTest.java:55-58`: 102 headings).

## K6. Desktop mode and controller architecture

- `AppGeoCeDG.newEuclidianController` returns the final
  `GeoCeDGEuclidianController` (`geocedg-desktop/AppGeoCeDG.java:1015-1018`;
  `geocedg-desktop/GeoCeDGEuclidianController.java:44-45`), which dispatches
  GeoCeDG modes in `switchModeForProcessMode` (`:483-557`).
- Multi-click precedents: `SplineV2` (`:698-710`), ordered list (`:745-759`),
  `GeoCeDGSimilarityTools` (point, then a typed operand, axes rejected, number
  dialog, `runAtomicConstructionMutation`; `GeoCeDGSimilarityTools.java:43-189`).
  `GeoCeDGSplineV2Authoring.commit` builds `new Command(kernel, …, false)` and
  runs `processCommand` (`:71-104`).
- Undo: a mode returning a non-null result stores one undo point through the
  host callback (`common/euclidian/EuclidianController.java:5466-5476`,
  `:5521-5532`).
- Icons and text for modes: `EuclidianConstants.getModeText` (`:532+`) maps a
  mode to `X.Tool`; help key `X.Help` (`:1172-1174`); icons through
  `GeoCeDGToolImageResource.forMode`/`forIconKey`.

## K7. Line and vector direction (PROVEN BY PROBE P1, P3; PROVEN FROM SOURCE)

| Object (`A=(1,1)`, `B=(4,3)`, `P=(0,5)`, `v=(2,1)`) | coefficients | `getDirection` | `getDirectionInD3` |
|---|---|---|---|
| `f = Line(A,B)` | (−2, 3, −1) | (3, 2) | (3, 2) |
| `fr = Line(B,A)` | (2, −3, 1) | (−3, −2) | (−3, −2) |
| `g = Line(P, f)` (parallel) | (−2, 3, −15) | (3, 2) | **(−3, −2)** |
| `h = PerpendicularLine(P, f)` | (−3, −2, 10) | (−2, 3) | **(2, −3)** |
| `k = Line(P, v)` | (−1, 2, −10) | (2, 1) | **(−2, −1)** |
| `s = Segment(A,B)`, `r = Ray(A,B)` | (−2, 3, −1) | (3, 2) | (3, 2) |
| `Ray(A, v)` | (−1, 2, −1) | (2, 1) | **(−2, −1)** |
| `xAxis` / `yAxis` | (0, 1, 0) / (−1, 0, 0) | (1, 0) / (0, 1) | same |

- `getDirection` = `(y, −x)` (`common/kernel/geos/GeoLine.java:444-457`);
  `getDirectionInD3` = `(−y, x)` **when the line has no end point**, else
  `end − start` (`:1402-1409`). `UnitVector`/`Direction` follow `getDirection`
  (`AlgoUnitVectorLine.java:38-41`; P1 `Direction(h)` = (−2, 3)).
- Conclusion: an `E2`-local contract needs only `getDirection` (and the vector
  coordinates); `getDirectionInD3` is opposite for parallel, perpendicular and
  point–vector lines and rays and must not be used. The probe's path-parameter
  readout returned (0, 0) for every line (the probe set the parameter on a free
  point outside a path algorithm); it is not evidence. The mini-track §9.1
  source finding that path parameters follow `getDirection` stands unexamined
  here and is `F1` scope.

## K8. Unit-state seam (PROVEN FROM SOURCE)

- One `DocumentUnitSystem` per construction (`common/kernel/Construction.java:247`,
  `:283`, `:333`), reset by `clearConstruction`, applied by clearing loads
  (`common/io/MyXMLHandler.java:3011-3025`), macro constructions always `EMPTY`.
- Presentation listener: `addListener(Runnable)`
  (`geocedg-common/kernel/units/DocumentUnitSystem.java:53-62`); listeners run
  only on an actual change; failures are logged and never block the state
  (`:95-109`); Javadoc: listeners "never trigger a construction recompute"
  (`:19-20`). It fires on document operations, loads, undo and redo.
- `UnitState.display(L)` (`geocedg-common/kernel/units/UnitState.java:154-161`)
  returns `UnitQuantity` `FINITE`/`UNSPECIFIED`/`NOT_FINITE`
  (`UnitQuantity.java:13-58`); `symbolOf` (`:144-146`). **No production code
  calls `display()` and no length formatter exists** — `E2` is its first
  consumer.
- Only consumer today: the status bar (`geocedg-desktop/AppGeoCeDG.java:178-184`,
  `:220-229`; `GeoCeDGStatusBar.java:215-238`).
- Precedents for text refresh: every text algorithm computes its string in
  `compute()` (`AlgoText.java:243-248`, `AlgoDependentText.java:103`); a global
  rounding change runs `Kernel.updateConstruction(false)` — a full recompute
  (`Kernel.java:4218-4230`; `Construction.java:1447-1510`); a language change
  re-runs text parent algorithms (`Construction.java:4488-4515`). **No existing
  text refreshes outside a recompute.** A unit change must not recompute
  (`unit-system.md` §6.1), so `E2` needs its own presentation step (design
  candidate §6).
- `AlgoElement.update()` = `compute()` then output updates
  (`common/kernel/algos/AlgoElement.java:579-595`); `compute()` is abstract
  (`:560`). A presentation step outside `compute()` is therefore possible at the
  algorithm level without touching `AlgoElement`.
- Dependent text strings are not written to XML (P1: `Text("12.5", M)` writes
  only style and `startPoint`), so a unit change leaves construction XML
  unchanged apart from `geocedgUnits`.

## K9. Text anchoring, alignment and rotation (PROVEN BY PROBE P1, P2)

- `Text(obj, P, subst, latex, h, v)` sets alignment (`common/kernel/commands/CmdText.java:101-132`);
  `AlgoText` re-applies it in every `compute()` and fixes the text
  (`common/kernel/algos/AlgoText.java:126-164`, `:249-254`, `:157`). Alignment is
  not serialized in element XML; the algorithm restores it (P1:
  `Text("12.5", M, true, true, 0, 0)` → `hAlign=0 vAlign=0`, no alignment tag).
- Centring is done by the drawable in screen space (`AlignDrawText.java:43-93`,
  `MARGIN = 6` px) from a model-coordinate anchor. P2: a text centred at the
  midpoint of the dimension line drew its bounding-box centre at x = 182.5 for
  an anchor at 181.8 px, and at 314.5 for 313.6 px after a zoom change — the
  anchor stays in model coordinates and the centring follows the zoom.
- `GeoText` has no rotation property (`GeoText.java:68-70`). `RotateText` and
  `Rotate(text, angle[, point])` produce a LaTeX `\rotatebox` text **without a
  start point**; the point argument is resolved and discarded
  (`AlgoRotateText.java:52-62`, `:112-131`; `CmdRotateText.java:49-59`;
  `CmdRotate.java:132-135`). P1: `Rotate(T0, 30°)` and `Rotate(T0, 30°, M)` both
  give `RotateText[T0, 30°]`, `start=null`.
- Conclusion (re-tested, unchanged from `P0` P5): parallel centred text is not
  sustainable in β without screen metrics in the kernel or a new drawable.
  Horizontal centred text is sustainable. Recommended record:
  `KNOWN LIMITATION — horizontal centred text` (prompt `DQ-E2-6`).
- Minor pre-existing defect: `AlignDrawText.hasChanged()` compares the old
  horizontal alignment with the vertical one (`AlignDrawText.java:100-101`).

## K10. Segment arrow endings (PROVEN FROM SOURCE, PROVEN BY PROBE P2)

- `SegmentStyle` (`common/kernel/geos/SegmentStyle.java:24-27`): `DEFAULT, LINE,
  ARROW, CROWS_FOOT, ARROW_OUTLINE, ARROW_FILLED, …`; fields on `GeoSegment`
  (`:62-63`, accessors `:858-874`); element XML `<startStyle val=…/>`,
  `<endStyle val=…/>` (`HasSegmentStyle.java:55-58`; reader
  `common/io/ConsElementXMLHandler.java:1111-1139`). Not copied by
  `setVisualStyle` (`GeoSegment.java:204-211`); copy, paste and undo go through
  XML and keep it.
- P2: `dl` with `arrow_filled` at both ends wrote
  `<startStyle val="arrow_filled"/><endStyle val="arrow_filled"/>`; after
  `getXML` → `setXML` both styles were restored and the XML was byte-equal.
- Drawn by `DrawSegment`/`DrawSegmentWithEndings` in screen pixels from the line
  thickness (`DrawSegmentWithEndings.java:114-153`, `:233-291`).

## K11. β figure prototype (PROVEN BY PROBE P2)

Built only from existing commands (no new command): `A=(1,1)`, `B=(5,2)`,
free `off=1.5`, `ov=0.2`, `gp=0.1`; `n` the left normal; two extension
segments, the dimension segment with arrow endings, `L=Distance(A,B)`, and
`Text(L, Midpoint(...), true, false, 0, -1)`. The figure, the value
(`4.12` at the default rounding) and the centred text were produced; the XML
round trip was identical. It shows that existing output types carry every part
of the figure; it does not prototype the algorithm.

## K12. Exporters (PROVEN BY PROBE P2, PROVEN FROM SOURCE)

| Family | Segments | Arrow endings | Text | Evidence |
|---|---|---|---|---|
| Picture (PNG, SVG, PDF, EMF, print) | yes | yes | yes, centred | shared drawables through `ExportViewport` (`geocedg-desktop/export/PictureExportService.java:375-380`); P2 SVG contains `4.12` |
| PSTricks | yes (`\psline`) | **no** | `\rput[tl]` at the anchor — **not centred** | `GeoGebraToPstricks.java:1549-1575`, `:899-961`; P2 |
| PGF/TikZ | yes (`\draw … --`) | **no** | `node[anchor=north west]` — **not centred** | `GeoGebraToPgf.java:2318-2344`, `:998-1074`; P2 |
| Asymptote | yes (`draw(A--B)`) | **no** | `label(…, SE)` — **not centred** | `GeoGebraToAsymptote.java:1971-1999`, `:996-1110`; P2 |
| DXF | yes (3 × `LINE`) | **no** | **absent**: full-construction export excludes texts with a non-blocking `OUTSIDE_GEOMETRIC_POPULATION` diagnostic; a selection containing a text is not writable | `geocedg-common/export/DxfExporter.java:167-219`; `GeometryExportPopulation2D.java:34`, `:71-114`; `GeometryExportPreflight.java:100-102`; P2 (`LINE=3 TEXT=0`) |

No exporter reads `SegmentStyle` or text alignment; the GeoCeDG LaTeX subclasses
override only the Locus V2 paths. Ordinary segments and text are enough for a
coherent figure in picture formats only. Closing the gaps is `DQ-E2-9`; any
closure belongs in GeoCeDG-gated export code (the `C` precedent:
`geocedg-desktop/export/LatexSemanticExporter`), not in kernel semantics.

## K13. Drag semantics (PROVEN FROM SOURCE)

- Inherited 2D rule for a dependent segment: `handleMovedElementDependent`
  (`common/euclidian/EuclidianController.java:6776-6871`) tries a translation
  vector, then a changeable parent, then **moves the free input points**
  (`hasMoveableInputPoints`/`getFreeInputPoints`, `:6848-6876`;
  `GeoElement.java:1968-2033`). Dragging a β dimension line without an `E2`
  handler would move `A` and `B`.
- `ChangeableParent` maps a drag to one free `GeoNumeric` but is effectively
  3D-only: `move` returns false when `viewDirection == null`, the 2D case
  (`common/kernel/geos/ChangeableParent.java:235-240`); 2D `GeoSegment` cannot
  carry one (`GeoSegment.java:842-845`); only `EuclidianController3D` records
  and drives it (`:3463-3500`).
- GeoCeDG precedent for a drag that edits a parameter: the semantic interaction
  point — press interception (`GeoCeDGEuclidianController.java:103-137`), drag
  (`:194-223`), one undo point on release (`:244-256`), cancellation on mode
  change (`:430-446`).
- Conclusion: kernel = geometry and the offset mapping helper; Desktop = the
  gesture writing exactly one free number; inherited moves of `A`/`B` through a
  dimension output must be intercepted.

## K14. Owned-icon pipeline (PROVEN FROM SOURCE)

- Owned Desktop tool icons are SVG sources under
  `source/shared/common/src/main/resources/org/geogebra/common/icons/svg/web/toolIcons/`
  (`mode_geocedg_splinev2.svg`: 24 × 24 view box, GeoCeDG-authored header,
  EUPL SPDX line), registered in `geocedg/resources/assets-manifest.yml`
  `owned_runtime_assets` (`:88-99`: id, path, kind `svg-tool-icon`, version,
  role, canonical LF SHA-256, provenance, licence class, trademark role,
  redistribution status), mapped by `GeoCeDGToolImageResource` (`:24-92`) and
  rasterized at run time by `JSVGIcon` at 64 px (`:121-150`).
- The 32 × 32 PNG derivation of `E1-L` (`dtest/CuratedGgtLibraryGenerator.java:176-266`)
  applies only to `.ggt` archives; **a Desktop tool icon needs no stored
  derivative**. Determinism is the canonical-LF hash in the manifest and the
  rendering checks of `dtest/G9U1IconReviewTest.java:44-75`.
- `assets-manifest.yml` is a pinned static-contract input (re-pin through the
  official mechanism).
- Repository practice (the `E1` and `E1-X1` preparation packages) ships no
  artwork in a preparation package; the two designs are specified in the prompt
  and drawn in `E2`.

## K15. Legacy macro name collision (PROVEN BY PROBE P1, P4; PROVEN FROM SOURCE)

- Macro lookup is case-insensitive (`common/kernel/MacroManager.java:99-101`)
  and `CommandDispatcher.getProcessor` resolves a macro **before** any native
  command (`common/kernel/commands/CommandDispatcher.java:303-313`). Nothing at
  document load compares macro names with native commands
  (`MyXMLHandler.java:2648-2662`; `Kernel.addMacro` `:4601-4608`).
- P1, with `Templatev7.ggb` (24 macros) loaded:
  `getMacro("DirectDimension")` and `getMacro("AXISDIMENSION")` return the
  legacy macros. With a macro renamed `midpoint` in a fresh document, the native
  `Midpoint(Q1,Q2)` failed with "Macro midpoint: Illegal number of arguments",
  `Midpoint(Q1,Q2,1)` built the 8 macro outputs, `Execute({"Midpoint(Q1,Q2)"})`
  produced nothing, and `evalXML("<command name=\"Midpoint\">…")` created
  nothing. The native command is unreachable in every regime (USER, SCRIPT,
  `Execute`/XML).
- Consequences once native `DirectDimension`/`AxisDimension` exist (INFERRED
  from the above): in a document that embeds the legacy macros the native
  command, its tool and its saved `<command name="DirectDimension">` elements
  resolve to the macro; a native element saved in such a document fails on
  reopen and is dropped with its dependents. In USER input the reverse table
  normalizes `directDimension` to `DirectDimension` before dispatch
  (`common/kernel/arithmetic/Command.java:120-136`;
  `common/main/Localization.java:950-962`), so a "native wins" dispatch rule
  would silently turn a typed legacy call `directDimension(A, B, 10)`
  (`kconversion = 10`) into a native dimension with offset 10.
- Pre-existing instance of the same class: `Templatev7.ggb` defines a macro
  `Perimeter` (`models/legacy/template-v7/derived/tool-inventory.yml`), which
  already shadows the native `Perimeter` in every derived document.
- User library (`geocedg-desktop/GeoCeDGUserToolLibrary.java`): `inspect()`
  rejects a command whose `nativeCommand(name)` is non-null (`:979-984`), using
  `lookupInternal` and the UI-locale alias (`:1068-1074`), at install, bundled
  load, activation, availability, digests and stored read (`:350-441`, `:793`,
  `:918`, `:1429`); `readStored` does not skip packages one by one
  (`:1398-1490`). P4: with two installed packages (`OwnedMidpoint`,
  `Distancia`), switching the UI to Spanish made `Distancia` a native alias and
  the whole library failed to read (`IOException: UserTools.CommandConflict:
  Distancia`); the store bytes were unchanged; back in English both packages
  read again. With native `DirectDimension`/`AxisDimension`, a store holding the
  legacy macros would make **every** user tool unavailable
  (`GeoCeDGUserTools.java:79-88`, `:511-514`, `:774-783`), without rewriting
  the store.
- Pre-existing defect revealed by P4 (independent of `E2`): the native-name
  check depends on the current UI language, so a valid library becomes
  unreadable after a language switch (proposed
  `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION`, §K19).
- Curated library: it excludes both legacy dimension tools
  (`dtest/PreG9BR6PlusE1LCuratedLibraryTest.java:53`) and none of its twelve
  names collides with the candidates.

## K16. Serialization and compatibility (PROVEN BY PROBE P5, PROVEN FROM SOURCE)

- β writes an additive `<command name="…">` with existing output element types
  and ordinary free numerics; arrow endings are element style (`K10`).
- P5 (the current build stands for any build that does not know the command):
  a document with `<command name="FutureDimension">` (inputs `A`, `B`, `d`,
  outputs `L`, `dl`) and a dependent `Midpoint(dl)` loaded through `setXML` and
  through the native-archive file route (`loadXML` returned `true`); the error
  list contained "Unknown command : FutureDimension" and "processing of command:
  FutureDimension(A, B, d)"; `A`, `B`, `d` and an unrelated segment loaded;
  **`L`, `dl` and the dependent `M` were absent**. Errors are aggregated into one
  `LoadFileFailed` report at `endDocument` (`common/io/MyXMLHandler.java:352-358`,
  `:3758-3797`). A re-save from such a build loses the dimension and its
  dependents.
- Classic and the pinned upstream baseline use the same shared handler
  (INFERRED: same code path; measured in the GeoCeDG profile only).

## K17. α minimum surface (PROVEN FROM SOURCE, INFERRED cost)

A composite `GeoDimension` would need: a `GeoClass` and XML element type read by
`ConsElementXMLHandler` (older readers fail on an unknown element type, a
stronger break than `K16`); `GeoElement` copy, `set`, redefine and
`getXMLtags`; a `Drawable` with hit testing and selection; style handling and
property panels; adapters in the picture path (free through the drawable) and in
PSTricks, PGF, Asymptote and DXF (new entity mapping, spec amendment, G9X1
pins); algebra and properties presentation; tests across every route. It
changes global serialization semantics: `GLOBAL_IMPACT`, one `FINAL`.

## K18. Verification registry and class

- Phase selections: 50 (`geocedg/specs/operations/verification-registry.json:27-78`);
  JUnit inventory selections: 42; pins in
  `tools/agent/tests/verification-final-coverage.Tests.ps1` (`:45`, `:166-167`,
  per-selection identity counts `:50-155`, catalog hashes `:29-32`). Registering
  `PRE-G9B-R6-PLUS-E2` makes them 51 and 43 or more, through the official
  updater (`tools/agent/update-verification-junit-inventory.ps1`).
- JUnit inventory: `final.desktop` 1874, `discovery.desktop` 1880,
  `final.shared` 6925 (refreshable only from executed evidence).
- `INTEGRATED_PHASE` (`verification-levels.md:1225`): PHASE, plus INTEGRATION
  when the frozen plan names concrete integration coverage. The concrete facts
  for `E2` are listed in the prompt (shared commands, inventories, GGBScript,
  Desktop profile, unit presentation, exporters through ordinary outputs).
- Prompt contracts: the parser
  (`tools/agent/prompt-contract-parser.psm1:49-76`) and the STATIC leaf
  `prompt.execution-safety` validate only the three catalogued prompts
  (`geocedg/specs/operations/prompt-contracts.json:21-25`); phase prompts carry
  the seven `geocedg-field` markers by convention and are checked here with the
  parser directly.

## K19. Observations (proposed identifiers; none is fixed here)

| Identifier | Status | Owner proposed |
|---|---|---|
| `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` | re-characterized: the collision affects document macros in every regime and disables the whole user library (`K15`) | `E2` (`DQ-E2-7`) |
| `OBS-R6PLUS-E2-USER-LIBRARY-LOCALE-DEPENDENT-NATIVE-COLLISION` | pre-existing; a language switch can make a valid stored library unreadable (`K15`, P4) | `E2` if `DQ-E2-7` changes the library policy, otherwise `G` |
| `OBS-R6PLUS-E2-TEMPLATE-V7-PERIMETER-MACRO-SHADOWS-NATIVE` | pre-existing; the `Perimeter` macro of `Templatev7.ggb` shadows the native command (`K15`) | author disposition (record only) |
| `OBS-R6PLUS-E2-LATEX-DXF-DIMENSION-FIDELITY` | arrow endings, text centring and DXF text absent outside picture export (`K12`) | `E2` (`DQ-E2-9`) |
| `OBS-R6PLUS-E2-DEPENDENT-DRAG-MOVES-MEASURED-POINTS` | the inherited dependent move would translate `A` and `B` (`K13`) | `E2` (mandatory interception) |
| `OBS-R6PLUS-E2-ALIGNDRAWTEXT-HASCHANGED` | pre-existing upstream comparison slip (`K9`) | none (upstream; record only) |
| `OBS-R6PLUS-E2-UPSTREAM-MANIFEST-SPLINEV2-PURPOSE` | `modified-files.yml` purposes omit the `SplineV2` lines (`K3`) | `G` |

## K20. Paths changed and product-effect proof

Tracked delta of the preparation candidate (documentation, evidence and prompt
only):

```text
A  .github/prompts/tasks/pre-g9b-r6-plus-e2-native-dimensions.prompt.md
A  docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_reconciled_design_candidate.md
A  docs/validation/pre_g9b_r6_plus_e2_preparation_characterization_report.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-preparation-characterization.json
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md      (status line and §14 block)
M  docs/roadmap/geocedg_roadmap.md                          (version, E2 row)
```

No path under `source/`, `apps/`, `tools/`, `geocedg/specs/`, `models/`,
`packaging/` or `docs/upstream/` changes; no registry, inventory, verifier,
profile, icon or serialization file changes. The five probe classes lived only
in the scratchpad; after the runs `:desktop:desktop:compileTestJava` was re-run
without the init script and the probe result files were deleted, so the build
directory holds no probe class. The `P0` design candidate is unchanged.

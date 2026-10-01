# PRE-G9B-R6-plus-P0 — integrated characterization and design candidate report

- Status: **TECHNICAL DESIGN CANDIDATE PENDING AUTHOR REVIEW**
- Recorded: 2026-10-01
- Task: [`PRE-G9B-R6-plus-P0`](../../.github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md),
  authorized by the author's explicit instruction of 2026-10-01 for
  characterization and design only
- Implementation base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`,
  tree `3f7abded9163324a3142ad7446fb4e6c85407809`
- Change route `ORDINARY`; frozen class `DOCUMENTATION_STATUS_ONLY`
  (`frozenAtPhaseStart = true`); planned acceptance `STATIC` on the exact frozen
  candidate; no `PHASE`, `INTEGRATION` or `FINAL`
- Claim vocabulary: **proposed / not normative**. Every design, matrix and
  recommendation here is a candidate. No specification or ADR is created or
  accepted.
- Self approval: **false**

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PRODUCT_PHASE_EFFECT      = NONE
```

Path abbreviations used throughout:

| Abbreviation | Path |
|---|---|
| `common/` | `source/shared/common/src/main/java/org/geogebra/common/` |
| `geocedg-common/` | `source/shared/common/src/main/java/org/geocedg/common/` |
| `common-jre/` | `source/shared/common-jre/src/main/java/org/geogebra/common/jre/` |
| `desktop/` | `source/desktop/desktop/src/main/java/org/geogebra/desktop/` |
| `geocedg-desktop/` | `source/desktop/desktop/src/main/java/org/geocedg/desktop/` |
| `profile` | `apps/geocedg/application-profile.yml` |

**Evidence labels:** `PROVEN FROM SOURCE`, `PROVEN BY PROBE`, `INFERRED`,
`UNKNOWN`. Planning statements were re-established from source at this base.
Where the planning text is wrong, the correction is listed in §14.

## Design candidates produced

| Subphase | Candidate |
|---|---|
| `A` | [layer workspace and status bar](../architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md) |
| `B` | [export surface and `ExportArea`](../architecture/pre_g9b_r6_plus_b_export_area_design_candidate.md) |
| `D0` | [unit-system inputs](../architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md) (inputs only; `D0` decides) |
| `C` | [2D export completion and semantic curve exporters](../architecture/pre_g9b_r6_plus_c_semantic_export_design_candidate.md) |
| `E1` | [GGT library, provenance, owned icons and packaging](../architecture/pre_g9b_r6_plus_e1_ggt_library_design_candidate.md) |
| `E2` | [native dimensions](../architecture/pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md) |
| `E3` | [`IsoABorder`](../architecture/pre_g9b_r6_plus_e3_iso_a_border_design_candidate.md) |
| `F1` | [orientation display](../architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md) |
| `F2` | [authoring memory](../architecture/pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md) |

Machine-readable evidence:
[`pre-g9b-r6-plus-p0-author-input-inventory.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-p0-author-input-inventory.json)
and
[`pre-g9b-r6-plus-p0-persistence-matrix.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-p0-persistence-matrix.json).

## 1. Entry gate and identities

Verified after a fresh `git fetch origin`, before any edit:

```text
local main       = babf20c7c03d0454e2da6760bf5d1fc3fa546f6b
origin/main      = babf20c7c03d0454e2da6760bf5d1fc3fa546f6b
live remote main = babf20c7c03d0454e2da6760bf5d1fc3fa546f6b   (git ls-remote)
HEAD tree        = 3f7abded9163324a3142ad7446fb4e6c85407809
tracked worktree = clean
P0 prompt blob   = 2c51ca73e6a1ccbd04d41885f8d4f86b69dd1578   (as named by the author)
```

- The local planning branch `feature/pre-g9b-r6-plus-planning` no longer
  exists, and no other `r6-plus` branch exists locally or remotely.
- No `P0` artifact existed: the only tracked `r6-plus` paths were the prompt,
  the plan, the planning report, the planning closeout and their two JSON
  mirrors.
- The roadmap records `PRE-G9B-R7` as `DESIGNED — NOT AUTHORIZED` and blocked by
  `PRE-G9B-R6-plus`, and `G9B` as not authorized.
- Working branch `phase/pre-g9b-r6-plus-p0-characterization-design`, created at
  the base. Local only; never pushed.

## 2. Canonical P0 prompt amendment

The first and only governance-layer edit amends the canonical prompt, as the
authorizing instruction requires.

- **Old blob:** `2c51ca73e6a1ccbd04d41885f8d4f86b69dd1578`
  (`PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED`).
- **New state:** `PRE-G9B-R6-plus-P0 = AUTHORIZED FOR CHARACTERIZATION AND
  DESIGN EXECUTION`, with `selfApproved = false`, `authorApproved = false`,
  `implementationAuthorized = true` and `passClaimed = false`.
- **Base and class:** the base is frozen at the commit and tree above
  (`BASE_IDENTITY = P_R6PLUS_PLAN`), with the planning provenance `T`/`D`/`P`
  recorded. `VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY` with
  `frozenAtPhaseStart = true`.
- **Strengthened, never weakened:**
  - the escalation paths now also name `apps/**`, `packaging/**` and product
    resources;
  - creating the prompts of later subphases is forbidden;
  - `UNKNOWN` is an admitted answer label;
  - the final local-input integrity check and the never-amend-after-freeze rule
    are explicit;
  - the instruction's additional stop conditions are added.

  Author-fixed scope, author-fixed unit semantics and every forbidden-scope
  rule are unchanged.

**Validation.** `Test-PromptContractDocument` (`tools/agent/prompt-contract-parser.psm1`)
with the `task` profile of `geocedg/specs/operations/prompt-contracts.json`
reported:

- all seven fields present and in order;
- no missing, duplicate, unknown or out-of-order field;
- one title;
- `execution_safe = True`.

The flag is a structural parser result, not permission. The amended blob is
recorded with the candidate outside this file, because this file is part of
the same commit. The prompt stays unregistered in the catalog, following the
convention for phase prompts.

Three attempts to inspect and validate the amended prompt were refused by the
session's automatic permission classifier until the author confirmed the
authorization directly in the conversation. The amendment itself was not
changed by that interruption.

## 3. Classification and verification

```text
CHANGE_ROUTE                       = ORDINARY
VERIFICATION_CLASS                 = DOCUMENTATION_STATUS_ONLY   (frozen at phase start)
PLANNED_ACCEPTANCE                 = STATIC on the exact frozen P0 candidate
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
  Rationale: no workstation prerequisite, toolchain, Gradle, Conda, packaging,
  download or environment contract changes.
GUIDE_IMPACT        = NONE
GUIDE_JUSTIFICATION = characterization and design candidates only; no
  observable product behavior, command, tool, menu, persistence, installation
  path or export format changes. Help is subphase G.
```

All runtime evidence comes from **scratch probes** kept in the session
scratchpad: a Gradle `--init-script` adds a scratch test source directory to
`:desktop:desktop:test`. Probe results are characterization evidence, never
acceptance evidence. No probe source, output or build artifact enters the
candidate.

## 4. Local author input

`artifacts/author-input/g9b-r6-plus/` is ignored by `.gitignore` rule
`/artifacts/*` (line 7). Nothing in it was committed, moved, renamed, rewritten
or copied into a product path. Each `.ggt` was copied into the scratchpad and
only the copy was unpacked.

| File | Bytes | Role | SHA-256 |
|---|---:|---|---|
| `ReportExport.md` | 26 074 | author-commissioned analysis of the upstream export surface | `fbf8524cfdf147cbd6999f9ff3d6f5bfc8a48b164b236a4df21aad61bb8f52d3` |
| `axisdimension.ggt` | 2 986 | dimension tool reference | `7989b4a87e1e10275627bc22ec3cb8f6783af7b05052dbbcc76610208b0d0936` |
| `circarcbyangle.ggt` | 2 308 | arc utility | `640f0906749b2396c676e0b3ed1f7324599ef1ec3302704784f225ed22fa5348` |
| `circlebyD.ggt` | 2 143 | circle utility | `977d812d55d8a151f7ed8caa2983d0960daab72f7fc3010f5cccfe4b15bd76f8` |
| `directdimension.ggt` | 2 708 | dimension tool reference | `228d54d7f08316cbed9c0a84bc5d62085ca59931e573908990c84d60cacf0a01` |
| `ductsymbol.ggt` | 3 000 | presentation symbol | `a2c1faa18d1727dd59bf590f998c360bb66d473174c605f4e127c96f5ea42890` |
| `ellipseaxisbyconjaxis.ggt` | 3 747 | conic utility (Rytz) | `7639c82aa2aad8b470f9f6ab9130e419c3e321f33f8d1ccc672326acf6c40539` |
| `ellipsebyaxis.ggt` | 1 490 | conic utility | `01437a90761366b619178cde37a3ebb88bc7ac51b6de0b04e3369d38ac77040b` |
| `ellipsepartiallength.ggt` | 2 495 | conic utility | `35c20a5d35d7bce362d36f2ef10f8db4d1b719e1254566234268f20cd16eb293` |
| `IFpositivethenPoint.ggt` | 1 263 | selection utility | `c490e221a90e2acaeeaad3575ba172acc6cf0e82fcb765560bcafeb9cb7beb27` |
| `pointjump.ggt` | 988 | transport utility | `a350031eb98be790627dd15294550a07638d0b7968c64e783695976ef8ba76f1` |
| `relcoor.ggt` | 2 327 | transport utility | `95ff380d56bb70fd07457b06b977fb27d03de22bfc0dfdd4850ea4bed8b7a0b2` |
| `sheetISOland.ggt` | 4 900 | ISO sheet reference, landscape | `d3b05cafb5d938ec4ddf096c0e1df60c7c0850ca6327cb8d42e0ca06c18e5c23` |
| `sheetISOvert.ggt` | 7 268 | ISO sheet reference, portrait | `4412824a19da659900dc269924bddfc6f5f03981c3f39c9286b08ef5538e2015` |
| `squarebydiagonal.ggt` | 1 400 | planar utility | `29e2a3d88d926f75f9b7fc0e24290340a10f092da7e161dc19c3a83129e9470f` |
| `symmsymbol.ggt` | 2 219 | presentation symbol | `491628c089749cd99b5759b0ed9259e530284529bedb3d2f8dc33d44b01fa74e` |
| `translationcoor.ggt` | 2 553 | transport utility | `ef05762d7e1847185374d737d929f868616e6c8a8d270483bd4b9c91ed47b42b` |

Seventeen files, 69 869 bytes.

- **Planning-time comparison.** A programmatic comparison of name, size and
  SHA-256 with `pre-g9b-r6-plus-planning.json` found **no added, removed or
  changed file**: 17 of 17 identical.
- **Final integrity check.** The entry inventory was taken before any
  characterization. A re-hash immediately before freezing found every file
  **unchanged**. There was neither an author-side change during the session nor
  an agent-side mutation.
- **Missing inputs.** No expected input is missing. The two author-supplied
  requirements (`AQ-X1`, `AQ-G3`) are scope, not missing input.

## 5. `ReportExport.md` claim ledger

The report analyses **upstream** 5.4.928.0 accurately. Its main gaps are the
`+2` px padding, the viewport-bounded drawables, SVG's separate path, and what
GeoCeDG actually exposes.

| # | Claim | Ledger | Governing evidence |
|---|---|---|---|
| 1 | Baseline 5.4.928.0, commit `9b93256b…`, dated 2026-08-05, build 28 July 2026, tag `geogebra-baseline-5.4.928.0` | CONFIRMED | `UPSTREAM.md:10-15` |
| 2 | The menu is defined in `FileMenuD` | CONFIRMED upstream; CORRECTED for GeoCeDG: built only in the v1-profile fallback | `desktop/gui/menubar/FileMenuD.java:146-180`; `geocedg-desktop/GuiManagerGeoCeDG.java:34-52` |
| 3 | Export submenu: Worksheet, Picture, GIF, Clipboard, PSTricks, PGF, Asymptote, STL, Collada, Collada (html) | CONFIRMED (Print Preview follows on the File menu itself, after a separator, despite the misleading "End Export SubMenu" comment) | `FileMenuD.java:146-180` |
| 4 | Collada and Collada-HTML only when `app.is3D()` | CONFIRMED; `AppGeoCeDG extends App3D`, so `is3D()` is true and the fallback shows them | `FileMenuD.java:170-173`; `geocedg-desktop/AppGeoCeDG.java:51`; `desktop/geogebra3D/App3D.java:283-285` |
| 5 | Picture formats PNG, PDF, SVG, EMF; no EPS | CONFIRMED | `desktop/export/GraphicExportDialog.java:225-233`, `:415-459` |
| 6 | Menu label "(png, svg)" | CONFIRMED | `FileMenuD.java:336-338` |
| 7 | DPI 72, 96, 150, 300 (default), 600; transparency | CONFIRMED | `GraphicExportDialog.java:257-259`, `:340` |
| 8 | Pixel size `floor(W_export·s)` | CONFIRMED (probe P2) | `common/euclidian/EuclidianView.java:5952-5953` |
| 9 | 3D view: PNG only | CONFIRMED | `GraphicExportDialog.java:225-228` |
| 10 | SVG through FreeHEP `SVGGraphics2D` | CONFIRMED (`SVGExtensions extends SVGGraphics2D`) | `desktop/export/SVGExtensions.java:46` |
| 11 | PDF through `PDFGraphics2D`, text as shapes or embedded fonts | CONFIRMED | `GraphicExportDialog.java:980-1035` |
| 12 | PDF size from `printingScale`, x-scale and export size | CONFIRMED; plus integer truncation of the page size (probe P2) | `GraphicExportDialog.java:1003-1010` |
| 13 | EMF or EMF+ | CONFIRMED | `GraphicExportDialog.java:933-963` |
| 14 | Priority `selectionRectangle` > `Export_1/2` > full view | CONFIRMED (probe P2: a leftover rectangle overrides both points) | `EuclidianView.java:5153-5238`, `:5795-5824` |
| 15 | Crop and translate, no fit scaling | CONFIRMED | `EuclidianView.java:5800-5823` |
| 16 | `Export_1`/`Export_2` found by label, both `GeoPoint`, min/max then screen transform | CONFIRMED | `EuclidianView.java:163-167`, `:5181-5204` |
| 17 | The Export rectangle is geometric but materialized as a screen clip | CONFIRMED; CORRECTED by omission: a `+2` px band is added on the right and bottom, and view-bounded drawables are lost outside the viewport (§7, P2) | `EuclidianView.java:5216`, `:5234`, `:5815-5816` |
| 18 | Fallback is the full visible view; no object bounding box | CONFIRMED | `EuclidianView.java:5219`, `:5237`, `:5822` |
| 19 | Clipboard uses `getExportImage` on the active view with a clipboard-adapted scale | CONFIRMED | `desktop/main/AppD.java:1932-1989`; `common/main/App.java:593-603` |
| 20 | Animated GIF: sliders with active min/max, interval, step, type, delay, loop; 3D rotation over 2π in 1° steps; frames via `getExportImage(1)` | CONFIRMED | `desktop/export/AnimationExportDialogD.java:136`, `:211-215`, `:275`, `:283-311`; `AppD.java:4583-4601` |
| 21 | LaTeX exporters reinterpret objects; bounds from the selection rectangle or visible bounds; `Export_1/2` not consulted; bound edits update the selection rectangle | CONFIRMED | `common/export/pstricks/GeoGebraExport.java:162-171`, `:240-258` |
| 22 | PSTricks article/beamer; PGF article, Plain TeX, ConTeXt, beamer, force Gnuplot; Asymptote `.asy` | CONFIRMED | `desktop/export/pstricks/PstricksFrame.java:35`; `PgfFrame.java:33-49` |
| 23 | `drawGeoElement` has `GeoImplicit` and `GeoLocus` branches | CONFIRMED; CORRECTED in consequence: `GeoLocusV2` is not a `GeoLocus` and is silently skipped | `GeoGebraExport.java:453-460`; `geocedg-common/kernel/geos/GeoLocusV2.java:28-33` |
| 24 | STL is ASCII `solid geogebra` | CONFIRMED | `common/geogebra3D/euclidian3D/printer3D/FormatSTL.java:60`, `:70` |
| 25 | STL bounds from exportable objects; `EDGE_FOR_PRINT = 40` "4 cm"; `STLscale`, `STLthickness` | CONFIRMED | `common/geogebra3D/euclidian3D/EuclidianView3DForExport.java:49-55`, `:222-262` |
| 26 | STL ignores the selection rectangle and `Export_1/2` | CONFIRMED (no reference in the 3D export path) | same |
| 27 | Collada 1.5, `centimeter`, `Z_UP`, `needsScale` and `useSpecificViewForExport` false | CONFIRMED | `common/geogebra3D/euclidian3D/printer3D/FormatCollada.java:71`, `:76-77`, `:443-458` |
| 28 | Collada HTML loads Three.js from external CDNs | CONFIRMED (three.js r92 through cdnjs and rawgit) | `common/geogebra3D/euclidian3D/printer3D/FormatColladaHTML.java:63-73` |
| 29 | Dynamic Worksheet: Help, Upload, Cancel; Upload calls `GeoGebraTubeExportD.uploadWorksheet()` | CONFIRMED (probe P1: dialog title *Upload to GeoGebra Resources*) | `desktop/export/WorksheetExportDialog.java:99-125`, `:150` |
| 30 | Three independent upstream export mechanisms (view render, object translators, 3D meshes) | CONFIRMED; GeoCeDG adds a fourth, the DXF adapter | as above |
| 31 | The manual still documents EPS | NOT DETERMINABLE FROM SOURCE (external documentation); the code part is CONFIRMED (#5) | — |
| 32 | The manual says implicit curves and loci cannot be LaTeX-exported | NOT DETERMINABLE FROM SOURCE (external documentation); the code part is CONFIRMED (#23) | — |
| 33 | `Export_1/2` is a 2D presentation convention, not an identity precedent; STL and Collada are not precedents for semantic export | NOT DETERMINABLE FROM SOURCE (an architectural opinion; consistent with `AGENTS.md` §12.1 and §13) | — |

## 6. GGT characterization

### 6.1 Provenance (re-established)

- Every archive holds one `geogebra_macro.xml` (UTF-8, LF) with one `<macro>`.
  13 of the 16 archives also hold one 32 × 32 PNG icon. There is no JavaScript
  and no defaults file.
- The root is `format="5.0" app="classic" platform="d"`. Writer versions range
  from 5.2.820.0 to 5.2.836.0.
- All 16 macros carry `copyCaptions="true"` and `showInToolBar="true"`.
- **All 16 exist in `models/legacy/template-v7/original/Templatev7.ggb`**
  (SHA-256 `f62e5b7a…ed4113`; 24 macros). Ordered inputs and outputs are
  identical for all 16. Constructions are identical once `<eqnStyle
  style="implicit"/>` tags are ignored; nine are identical even without that
  (machine-readable list in the inventory JSON).
- `EllipseAxis`, `pointJump` and `SquarebyDiagonal` have no icon reference,
  while the template names one. The 13 bundled icons are byte-identical to the
  template's. 12 of the 13 use GeoGebra stock toolbar names; only
  `circulodiametro.png` is custom-named.
- The two sheet tools carry `<ggbscript onUpdate="&#xd;&#xa;"/>` (CR LF only)
  on their input `ISO216n`.

### 6.2 Behavior, provenance and rights kept distinct

| Aspect | Status |
|---|---|
| behavioral equivalence | PROVEN (construction-equivalent to `template-v7`) |
| provenance | author Manuel Prado-Velasco per `models/legacy/template-v7/manifest.yml:9`; standalone 2024 exports per archive timestamps and writer versions |
| redistribution rights | **not established**: `review_status: blocked` (`models/legacy/template-v7/manifest.yml:36-37`); GeoGebra UI images classed under CC BY-NC-SA terms (`docs/licensing/component-matrix.md:21`, `:70`, `:80`). No legal conclusion is made here |
| GeoCeDG-owned replacement icon | none exists; `E1` designs an owned pipeline |
| redistributable product asset | none until a rights record and an author decision exist |

### 6.3 Per-tool characterization

| Tool (file) | Ordered inputs → outputs | Construction | Fixed style / constants |
|---|---|---|---|
| `directDimension` | `A, B, kconversion` → `m, p, u, v, D, g, distanceDE, TextDE` | `f = Line(A,B)`; `g = OrthogonalLine(A,f)`; `D = Point(g)`; `E = h ∩ i`; `u = Vector(E,D)`, `v = Vector(D,E)`; `F = D + (|AD|/10)·û`; extension segments `m = AF`, `p = BG`; `distanceDE = Distance(D,E)` | value text `"  " + kconversion·distanceDE`, anchored at `Midpoint(D,E)` with `labelOffset (−55, 1)` px; **visible infinite guide line `g`**; thickness 2 |
| `axisDimension` | `A, B, f, kconversion` → `u, v, TextDE, distanceDE, p, m, D, r` | `g = OrthogonalLine(A,f)` (hidden); guide `r = Segment(A ∓ 10·|AB|·ĝ)`; `D = Point(r)`; rest as direct | **visible 20·|AB| guide segment `r`**; `labelOffset (−73, −5)` px |
| `sheetISOAnLand` | `D, ISO216n, CeDGScale, CeDGMargin` → 21 | trim rectangle `D E G F`; margin border; title-block grid | sizes `Iteration(…, ISO216n)/CeDGScale` (unrounded A series); trim dotted (type 20, thickness 2) |
| `sheetISOAnVert` | same → 22 | landscape geometry rotated −90° about `D` and translated; title block translated, not rotated | as above; `h''` duplicates `f'` |
| `DuctSymbol` | `A, B, lt` → 6 | tube with three arcs | thickness 3 |
| `SymmSymbol` | `A, f, ds` → 2 | two short symmetry segments | thickness 3 |
| `circArcbyAngle` | `A, B, phi` → `c, B'` | `B' = Rotate(B, phi, A)`; `c = If(phi > 0, CircleArc(A,B,B'), CircleArc(A,B',B))` | — |
| `CirclebyD` | `A, diameter` → `g` | `Circle(A, Point(Circle(A, diameter/2)))` | — |
| `EllipseAxis` | `O, B, C` → `e` | ellipse from centre and axis ends; assumes `C` on the axis ⟂ `OB` | — |
| `conj2mainAxesEllipse` | `A, B, C` → `L, K, N, M` | Rytz construction of the axis vertices | — |
| `ellipseLength12` | `f, H, C` → `g, a` | `g = Arc(f, H, C)`; `a = Length(g)` | — |
| `IFPositiveSelectPoint` | `M_4, L_4, v_1` → `O_4` | `If(v_1 > 0, L_4, M_4)` | — |
| `pointJump` | `A, f, Avance` → `B` | `B = A + Avance·UnitVector(f)` | — |
| `relCoor` | `D, C, f` → `relx` | signed coordinate of `D` along `f` from `C` | — |
| `translationCoor` | `D, C, f, F, i` → `I` | transfers `relx` from `F` along `i` | — |
| `SquarebyDiagonal` | `A, B` → `k, j, i, h, E, D` | square from a diagonal | — |

**`AQ-G3` effect (finding 16).**

- Landscape: hide `f g h i` (dotted trim) and `E G F` (corner outputs).
- Portrait: hide `f' f'' i'' g'' h''` (dotted trim, with `h''` duplicating
  `f'`) and `F_2 G_2 H_2`.
- With `copyCaptions="true"`, `AlgoMacro` creates outputs hidden whenever they
  are hidden in the definition (`common/kernel/algos/AlgoMacro.java:118-128`),
  so a curated derived definition needs no runtime code.
- The upper-left corner `D` is the user's **input**; a macro cannot hide it
  (`AQ-G3a`).

## 7. Probe results

All probes ran on the base tree from the scratchpad, through
`gradlew.bat :desktop:desktop:test --tests <probe> --init-script <scratch>/probe.gradle`.
Embedded probes use the `G9U1TestApp` host, with `JOptionPane` statically
mocked after one command-error dialog blocked the first run. The windowed probe
uses `GeoCeDG.main` with an isolated settings file.

**P1 — export shortcuts and routes (windowed, plus a separate CLI process).**

| Route | Result |
|---|---|
| File menu | New, Open, Open recent, Open Classic diagnostic session, Save, Save as, Print preview, Export 2D geometry as DXF (experimental) `{shift ctrl D}`, Close |
| `Ctrl+Shift+D` | global dispatcher **not consumed**; segment *selection allowed* true → false; slider fixed false → true; then the menu accelerator **opened the modal DXF dialog** |
| `Ctrl+Shift+U` | consumed; opened *Export as Picture* (non-modal) |
| `Ctrl+Shift+W` | consumed; opened *Upload to GeoGebra Resources* (modal); disposed, nothing uploaded |
| `Ctrl+P` | consumed; opened *Print Preview* |
| `Ctrl+Shift+C`, `M`, `B` | deliberately **not fired**: they write the author's system clipboard. PROVEN FROM SOURCE in the same live switch (`common/main/GlobalKeyDispatcher.java:719-747`; gate `:562-566`, `:1037-1039`; no GeoCeDG override, `geocedg-desktop/AppGeoCeDG.java:416-435`) |
| `--export=<file>.png --dpi=72 <file>.cedg` | live: `checkCommandLineExport` attempted the export, wrote a **0-byte** file because the view had zero size at export time, then exited 0 (`desktop/gui/app/GeoGebraFrame.java:1033-1127`) |
| `--exportAnimation=<file> --slider=…` (with `--delay`, `--loop`, `--dpi`, `--maxSize`) | not probed; PROVEN FROM SOURCE in the same method (`desktop/gui/app/GeoGebraFrame.java:852-1031`): an Animated GIF route, although GIF is proposed out of the surface |
| `ExportImage("type", "png"/"svg"/"pdf", "filename", …)` | files written: 7 938, 43 644 and 9 928 bytes |
| JS/API | `writePNGtoFile` wrote 2 086 bytes; `getPNGBase64` returned 2 784 characters; `exportPGF` returned 731 characters; `isScriptingDisabled() = false` |
| v1-profile fallback | PROVEN FROM SOURCE only (`GuiManagerGeoCeDG.java:34-50`; `GeoCeDGMenuBar.java:54-77`): restores the full upstream menu, including STL, Collada and Worksheet |

**P2 — export area, padding and clipping.** The view was 800 × 600 at
50 px/unit, giving x ∈ [−8, 8] and y ∈ [−6, 6], with `printingScale = 1`.

| Case (area) | Exact | PNG s=1 / s=2 | PDF MediaBox (exact) | SVG | EMF/EMF+ rclBounds |
|---|---|---|---|---|---|
| inside: (−2,2)–(2,−2) | 200×200 | 202×202 / 404×404 | 114×114 (113.39) | 202×202 | 202×202 |
| larger: (−12,9)–(12,−9) | 1200×900 | 1202×902 / 2404×1804 | 681×511 (680.31×510.24) | 1202×902 | 1202×902 |
| disjoint: (9,9)–(12,6) | 150×150 | 152×152 / 304×304 | 86×86 (85.04) | 152×152 | 152×152 |

- **Padding.** The `+2` band lies on the right and bottom only. A point centred
  1.5 px outside the right edge, and both axes, were painted into it. Lines
  clipped by the export frame stopped at the exact edge.
- **Clipping, `exportPaint` family** (PNG pixels; PDF/EMF drawing operations).
  - Kept outside the viewport: lines, segments, vectors, a circle, a polygon,
    Locus V2, a legacy locus within its sampling window, and text.
  - Lost: a point (no drawing operation emitted in PDF, EMF or SVG), the
    function graph beyond x = 8, and axes and grid (0 dark pixels off-view
    against 31 in view).
- **Clipping, SVG.** Segments and lines were clipped to the on-screen view (path
  `M -5 550 L 805 550`). The off-view circle, polygon, line, Locus V2 and point
  were absent. Groups `misc`, `layer0`, `layer5`.
- **Leftover selection rectangle.** A 300 × 200 rectangle overrode
  `Export_1/2` and produced a 300 × 200 PNG. It was kept by a same-mode
  `setMode(MOVE)` and by `TRANSLATE_BY_VECTOR`, and cleared by `POINT` and by a
  real switch back to `MOVE`.

**P3 — layer creation.** Shared kernel, through a headless `AppCommon` subclass
that overrides `getMaxLayerUsed`, and Desktop `AppGeoCeDG`:

- the clamp turns 12 into 9 and −3 into 0;
- with the override at 9, a point, a segment and a dependent number were
  created on layer **8** (defaults-path cap); at 5, on 5;
- macro outputs went to **9** at the first use and **4** at the second,
  ignoring the definition's layers 3 and 1;
- after reload with the override at 6, stored layers 0, 2, 7 and 8 were
  restored, while a dependent numeric without a `<layer>` tag became **6**;
  after undo and redo with the override at 8, it became **8**;
- a fresh embedded `AppGeoCeDG` has `maxLayerUsed = 5`, and a new point goes to
  layer 5;
- with the default at 6, every new object took 6 **except** `LocusV2` and
  `SplineV2`, which took **0**;
- `InternalClipboard.duplicate` of an object on layer 2 kept **2**, not the
  current default 7;
- `clearConstruction` left `maxLayerUsed` at 5, and `resetMaxLayerUsed` set
  it to 0.

**P4 — user-tool library.** `GeoCeDGUserToolLibrary.install` on the 16
scratchpad copies:

- **16 installed, 0 rejected.**
- Both sheet tools activated and created their 21 and 22 outputs: 4 (land) and
  5 (vert) dotted trim segments, and 3 corner points each.
- All outputs landed on the current default layer 5. No output carries a
  script; the CR LF `onUpdate` sits on an input inside the macro construction.
- `directDimension` created the visible infinite guide line.

**P5 — rotated text.**

- `Rotate(T, 30°)` → `AlgoRotateText`. The text is
  `\rotatebox{30.000000000000004}{ \text{ 12.5 } }`, `isLaTeX = true`, **start
  point null**.
- `Rotate(T, 30°, P)` gave the same definition; the point is ignored.
- `RotateText("12.5", 45°)` behaved the same way.
- PGF emits `node[anchor=north west] {$\rotatebox{…}…$}`.
- So rotated text exists only as LaTeX, with no anchor control.

**P6 — remembered-reference lifecycle.**

- An ordinary `Line(A,B)` has **no persistent geo ID**.
- After undo, redo, a redefinition `Line(A,C)` and a reopen, the line is a
  **different Java object**. A deleted line is gone, and `clearConstruction`
  clears everything.
- `Construction.isInConstructionList` returned `true` for the stale pre-undo
  object.

**P7 — SVG layer grouping.** The SVG writer opens a `<g id="layer<n>">` each
time the layer changes while walking the layer-sorted drawable list
(`desktop/export/GraphicExportDialog.java:895-907`). Probe output contained
`misc`, `layer0` and `layer5`, matching LocusV2 on 0 and the other objects on 5.

## 8. Required findings

**F1 — layer domain and extension.** PROVEN FROM SOURCE and PROVEN BY PROBE (P3).

- The domain is 0..9 and clamped in `GeoElement.setLayer`
  (`common/kernel/geos/GeoElement.java:1001-1018`). Every dependent consumer is
  listed in the [A candidate](../architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md)
  §3.1. That list covers clamping, sorting, hit testing, selection sentinels,
  UI controls, XML read and write, scripting commands, SVG and DXF naming, and
  the 3D renderer.
- **Least invasive correct extension:** a bounded, non-negative domain
  `0..L_MAX`, supplied by the application configuration (Classic and Web stay
  at 9). It needs a numeric Algebra View order, a spinner instead of the
  index-coupled combo, and 3D render coding clamped to its range.
- **Compatibility:** legacy documents are unaffected. Classic and older
  GeoCeDG builds clamp values above 9 on read, so the value is lost for older
  readers (`AQ-L1c`).
- **Class:** `A` unsplit is `GLOBAL_IMPACT`. The recommended split is `A-1`
  `INTEGRATED_PHASE` plus `A-2` `GLOBAL_IMPACT` (`AQ-L1b`).
- **Stop-condition handling.** The arbitrary-layer target needs two author
  choices: the domain bound (`AQ-L1a`) and acceptance of the clamping by older
  readers (`AQ-L1c`). No migration of existing documents is required. As the
  prompt prescribes, `P0` reports the design and its classification for author
  decision and chooses neither. The widening (`A-2`) stays blocked until both
  are decided; `A-1` does not depend on them.

**F2 — working layer.**

- PROVEN BY PROBE that overriding `getMaxLayerUsed` alone is insufficient: the
  defaults path caps at 8, rebuilt dependent numerics follow the session value,
  and LocusV2 stays on 0.
- INFERRED design: a new `getLayerForNewObject()` hook in
  `ConstructionDefaults` and `AlgoMacro`; working layer `SESSION` in
  `AppGeoCeDG`; New → 0; Open → the maximum drawable layer of the document.

**F3 — hidden layers.** INFERRED design: document-wide
`DOCUMENT_PRESENTATION`, a lazily written flat GeoCeDG element excluded from
preferences, macros and clipboard, and not an undo step (`AQ-L3`; `SESSION` is
the minimal alternative).

**F4 — visibility path.** PROVEN FROM SOURCE.

- `add` and `update` use the private `drawableNeeded`
  (`common/euclidian/EuclidianView.java:2037-2091`, `:2227-2240`): visible in
  this view (→ `GeoElement.isVisibleInView`, `:5742-5748`), labelled, and
  `isEuclidianVisible()` (`GeoElement.java:1424-1445`: forced, then
  `showInEuclidianView`, then restricted, then raw flag or condition).
- Painting reads cached drawable visibility
  (`common/euclidian/draw/DrawPoint.java:133`, `:370`). Hit testing reads it
  live (`common/euclidian/HitDetector.java:61-63`).
- The rule therefore **cannot** live in `isVisibleInThisView`:
  - `AlgoSlopeField` and `AlgoIntegralODE` compute geometry from that predicate
    (`common/kernel/advanced/AlgoSlopeField.java:212-233`;
    `common/kernel/algos/AlgoIntegralODE.java:151-160`);
  - drawables are neither removed nor re-added when it changes.
- Candidate: one presentation predicate at painting (a hook in the shared view
  so that Graphics 2, which is not a GeoCeDG class, obeys it), at hit testing
  (a post-filter of `setHits`, `EuclidianView.java:2328`), in the export paint
  path and SVG loop (`B`), and in LaTeX and DXF (`C`).
- Object visibility, `<show object>` (`common/kernel/geos/XMLBuilder.java:60`),
  the Algebra View marble
  (`desktop/gui/view/algebra/AlgebraTreeCellRenderer.java:120`,
  `AlgebraTreeController.java:136`) and every script keep reading object
  visibility.

**F5 — export-menu architecture.** PROVEN FROM SOURCE.

- GeoCeDG never builds `FileMenuD` except in the v1 fallback. The menu is
  projected from the profile action catalog and fails closed on mismatch
  (`geocedg-desktop/GeoCeDGMenuBar.java:49-162`, `:153-155`).
- The registry admits only `host.document.print-preview` and
  `geocedg.export.dxf.dialog` as export-like targets
  (`geocedg-desktop/GeoCeDGActionRegistry.java:61-63`, `:304-306`, `:326-328`).
- The profile has `file-print` and `file-import-export` groups
  (`profile:3166-3176`) and defers `document.sheet-export`
  (`profile:3046-3051`). The DXF accelerator is hard-coded in Java
  (`GeoCeDGMenuBar.java:35-36`).
- A v1 or missing profile falls back to the upstream menu
  (`geocedg-desktop/GeoCeDGProfile.java:147-176`;
  `GuiManagerGeoCeDG.java:34-50`).

**F6 — `selectionRectangle`.** PROVEN FROM SOURCE and PROVEN BY PROBE.

- The field is owned by `EuclidianView` (`common/euclidian/EuclidianView.java:263`,
  setter `:2921-2924`). It is created by right-drag, Select mode and
  `allowSelectionRectangle` (`common/euclidian/EuclidianController.java:7516-7523`,
  `:8749-8775`), and also by GeoCeDG's zoom window, which nulls it on press,
  release and cancel (`geocedg-desktop/GeoCeDGEuclidianController.java:94`,
  `:235-236`, `:375`, `:395-397`). LaTeX frames write it when a bound is edited
  (`common/export/pstricks/GeoGebraExport.java:162-171`).
- It is cleared on mouse press (`EuclidianController.java:9287`, `:9482-9486`)
  and on `setMode`, except for the mirror, rotate, translate and dilate modes
  and a same-mode call (`EuclidianView.java:780-823`).
- It is never serialized. A leftover rectangle crops a later export (P2).

**F7 — `Export_1`/`Export_2` per family.** PROVEN FROM SOURCE and PROVEN BY PROBE.

| Family | Relation |
|---|---|
| PNG, PDF, EMF, clipboard, GIF, print | used when no selection rectangle exists, with `+2` px |
| SVG | the same frame, drawn without export re-update |
| LaTeX | ignored |
| DXF | ignored (whole construction or selection; `geocedg-desktop/GeoCeDGDxfExportController.java:53-54`, `:458-464`) |
| 3D (STL, Collada) | ignored |

**F8 — `AQ-X1`.** PROVEN BY PROBE (P2) in PNG, PDF, SVG, EMF and EMF+, for
areas inside, larger than and disjoint from the viewport.

- Padding: the `+2` px of `getExportWidth/Height` and `exportPaintPre`
  (`EuclidianView.java:5216`, `:5234`, `:5815-5816`) against the exact
  `getFrame` (`:5172-5173`); PDF also truncates the page size
  (`desktop/export/GraphicExportDialog.java:1008-1010`).
- Clipping: point culling against the view size (`DrawPoint.java:180-187`),
  curve sampling over the view x-range (`DrawParametricCurve.java:212-224`),
  axes and grid bound to the view (`DrawAxis.java:160-162`;
  `DrawGrid.java:129`, `:194`), and SVG without export re-update
  (`GraphicExportDialog.java:888-907`).

**F9 — scale and unit per exporter.** PROVEN FROM SOURCE.

| Exporter | Scale and unit |
|---|---|
| picture | `printingScale` (cm/unit), a zoom-derived power of ten (`EuclidianView.java:1930-1934`; P2: 1.0 at 50 px/unit); DPI; PDF in points; SVG in px; EMF device-independent |
| LaTeX | `xunit = yunit = 1` cm/unit, independent of `printingScale` (`GeoGebraExport.java:241-242`) |
| DXF | `UNITLESS`, `$INSUNITS = 0` (`geocedg-common/export/DxfExporter.java:108-110`) |
| STL | 4 cm edge, `STLscale` |
| Collada | centimetres |

Full table in the [D0 inputs](../architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md) §2.

**F10 — legacy cm, scale, export and print units.** PROVEN FROM SOURCE.

- Every `cm` is a physical output or device unit, or a free-text axis label.
- The GeoCeDG model-length notions (`MetricUnit2D.CONSTRUCTION_LENGTH_UNIT`,
  `"model-coordinate"`, the spatial `units` token) are construction-semantic
  abstract model units. Scale denominators and `printingScale` exponents are
  dimensionless numbers.
- No geometry reads a physical unit. Classified table in the D0 inputs §2.

**F11 — `constructionUnit` owner.** PROVEN FROM SOURCE (constraints) and
INFERRED (choice).

- A flat, versioned GeoCeDG child of `<construction>`, written lazily and
  skipped for macro constructions.
- Reset in `clearConstruction`, **not** on parse: the clipboard wrapper re-runs
  `handleConstruction` (`common/util/InternalClipboard.java:624-634`;
  `common/io/MyXMLHandler.java:2573-2595`).
- Unknown children are not skipped as subtrees (`MyXMLHandler.java:3068-3094`).
- No compatibility blocker for `DOCUMENT_SEMANTIC`. D0 inputs §3.

**F12 — `presentationUnit` owner and default.** INFERRED.
`DOCUMENT_PRESENTATION` in the same element, read only to format dimension texts (numeric outputs stay model-unit; D0 inputs §4a);
the default for new documents is a `USER_PREFERENCE`; the absent value follows
`constructionUnit` when physical. D0 inputs §4.

**F13 — `UNSPECIFIED_MODEL_UNIT` compatibility.** The parser and clipboard
facts are PROVEN FROM SOURCE. The behavior below is the author-fixed rule plus
the P0 design (no unit exists yet). Cross-window paste is INFERRED: one
`CopyPasteD` per `AppD` (`desktop/main/AppD.java:354`, `:4988-4992`).

- Legacy `.cedg` and `.ggb` load as unspecified, and nothing is written.
- Older GeoCeDG builds drop the unit (lenient).
- The clipboard carries no unit and survives New and Open in one window.
- D0 inputs §5.

**F14 — dimensions in the dependency graph.** PROVEN FROM SOURCE.

- Today a legacy dimension is one `AlgoMacro` parent of all outputs
  (`common/kernel/algos/AlgoMacro.java:168-170`). Its point-on-path output is
  draggable through `FixedPathRegionAlgo` (`AlgoMacro.java:608-663`;
  `common/kernel/geos/GeoPoint.java:857-860`).
- Native candidate (INFERRED): outputs depend on `A`, `B`, the direction and
  explicit free model-unit numbers (offset, overshoot, gap). The numeric output
  is the model-unit measure; only the text converts units (D0 inputs §4a). [E2 candidate](../architecture/pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md) §6.

**F15 — GGT style.**

- PROVEN FROM SOURCE that a macro copies each output's fixed style
  (`AlgoMacro.java:288-315`).
- INFERRED that a remembered style is not sustainable for multi-role tools.
- The **author fallback applies** (black at 3 pt) in curated derived
  definitions. Whether "3 pt" means GeoGebra thickness 3 (a 1.5 px stroke,
  `common/euclidian/Drawable.java:626`) or 3 typographic points is `AQ-G7`.

**F16 — every supplied GGT and `AQ-G3`.** PROVEN FROM SOURCE and PROVEN BY
PROBE. §6 above.

**F17 — orientation authority.** PROVEN FROM SOURCE for the accessors and the
constructive routes; `Tangent(P, conic)` UNKNOWN and equation lines INFERRED
(F1 candidate §2).

- `GeoLine.getDirection` = `(y, −x)` (`common/kernel/geos/GeoLine.java:443-447`)
  is the semantic authority for lines, segments and rays. It
  agrees with the path parameters, `UnitVector`/`Direction` and the oriented
  `Angle(g,h)`.
- `getDirectionInD3` is reversed whenever there is no end point
  (`GeoLine.java:1402-1409`).
- Per-route table in the [F1 candidate](../architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md) §2.
  Orientation is already observable through `UnitVector`, `Direction`,
  `Angle(g,h)`, ray drawing and point-on-path parameters.

**F18 — parallel and perpendicular modes.** PROVEN FROM SOURCE and PROVEN BY
PROBE (P6).

- `parallel` is `protected final`; `orthogonal` is overridable
  (`common/euclidian/EuclidianController.java:2424`, `:2507`).
- Either order works, they fire on the second selection, and the lists are
  cleared as they are read.
- Point-on-path conflict: issue #2610 (`:9171-9177`).
- Least invasive hook: `GeoCeDGEuclidianController.switchModeForProcessMode`
  and the press path, with a session helper.
  [F2 candidate](../architecture/pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md) §3.

**F19 — serialization impact.** PROVEN FROM SOURCE for current behavior;
INFERRED for every future row. §11.

**F20 — profile, catalog, features, packaging.** PROVEN FROM SOURCE for the
current pins and contracts; INFERRED for the deltas. §12.

**F21 — verification class per subphase.** INFERRED: recommendations derived
from proven impact facts. §13.

## 9. Architecture-owner matrix

"KERNEL" means the shared Java kernel and its shared document serialization
(`AGENTS.md` §4). Everything else is EXTERNAL: application, frontend, export
or packaging.

| Capability | Owner | Rationale |
|---|---|---|
| layer domain bound | KERNEL (shared `GeoElement.setLayer` clamp and XML range), configured by the application | persistent object property in shared serialization |
| working-layer state | EXTERNAL — Desktop session | authoring state; never geometric truth |
| new-object layer hook | KERNEL seam (`ConstructionDefaults`, `AlgoMacro`) with an EXTERNAL value (`AppGeoCeDG`) | one creation seam for every route |
| hidden-layer set | EXTERNAL — application presentation state; its persistence (`A-2`) touches shared serialization (`MyXMLio` writer, `MyXMLHandler` reader) | presentation; must not change object visibility |
| effective-visibility predicate | EXTERNAL — shared view painting and hit testing, exporters | presentation consumers only |
| Algebra View layer eye and numeric order | EXTERNAL — Desktop | GUI |
| status bar | EXTERNAL — Desktop | presentation |
| working-layer Move-group mode | EXTERNAL — Desktop mode plus profile; the mode constant lives in the shared `EuclidianConstants` table by precedent | GUI orchestration |
| export menu surface and shortcut routing | EXTERNAL — profile, registry, Desktop dispatcher | menu and profile projection |
| `ExportArea` authority | EXTERNAL — shared application-level seam with a Desktop session implementation | export state; never geometry; the shared `ExportImage` command and API consult it |
| exact and complete export rendering | EXTERNAL — export service and view rendering | render, not semantics |
| `constructionUnit` | KERNEL — shared document model | document semantic, serialized, consumed by every frontend |
| `presentationUnit` | KERNEL-readable document setting | dimension **texts** are kernel objects updated in the graph; numeric outputs stay model-unit |
| engineering export scale, DXF unit header | EXTERNAL — export adapters | export formatting |
| `LocusV2`/`SplineV2` LaTeX export | EXTERNAL — export adapter over the semantic evaluator | the kernel is not an export formatter |
| GGT library, curation, icons, catalog | EXTERNAL — legacy store, resources, Desktop library | packaging and resources |
| GGT packaging | EXTERNAL — packaging and verification | installer concern |
| `DirectDimension`, `AxisDimension` | KERNEL (commands); EXTERNAL tool, drag handler, profile | DAG, dynamic evaluation, persistence, several frontends |
| `IsoABorder` | KERNEL (command, with an explicit model-units-per-millimetre input); EXTERNAL menu action, ISO A area selection and linkage | DAG and persistence; never reads units live |
| orientation cue | EXTERNAL — view overlay reading the kernel direction | presentation derived from kernel semantics |
| parallel/perpendicular memory | EXTERNAL — Desktop controller helper | mode and session orchestration |
| hidden angle of Angle with Given Size | EXTERNAL — tool path | authoring default; command unchanged |
| help | EXTERNAL — documentation | help |

No row duplicates kernel semantics in Desktop or Python. Kernel rows: the layer
domain bound and the creation seam, `constructionUnit`, `presentationUnit` and
the two command families. Hidden-layer persistence and the layer range (`A-2`)
also touch shared serialization without being kernel semantics. No kernel
output reads `constructionUnit`, `presentationUnit` or `drawingScale` live,
except dimension texts (D0 inputs §4a).

## 10. Persistence matrix (summary; JSON mirror is authoritative for the row data)

| State | Class | Author-fixed | Serialized |
|---|---|---|---|
| `constructionUnit` | `DOCUMENT_SEMANTIC` | **yes** | yes, lazily, `<construction>` child |
| legacy missing `constructionUnit` | `UNSPECIFIED_MODEL_UNIT` | **yes** (no blocker found) | — |
| `presentationUnit` | `DOCUMENT_PRESENTATION` (`USER_PREFERENCE` default for new documents) | no | yes, same element |
| `workingLayer` | `SESSION` | no | no |
| `hiddenLayers` | `DOCUMENT_PRESENTATION` (document-wide) | no | yes, lazily, document-level element |
| `ExportArea` | `SESSION` (author preference; no contrary evidence found) | no | no |
| orientation-display option | `USER_PREFERENCE` | no | no |
| parallel `lastReference` | `SESSION` | no | no |
| perpendicular `lastReference` | `SESSION` (independent) | no | no |
| GGT remembered style | `USER_PREFERENCE` if ever adopted; **not adopted** (fallback) | no | no |
| dimension placement parameter | `DOCUMENT_SEMANTIC` | no | yes, ordinary free numeric input |
| `IsoABorder` export linkage | `SESSION` | no | no |
| `IsoABorder` model-units-per-millimetre input | `DOCUMENT_SEMANTIC` | no | yes, ordinary command input |
| export `drawingScale` | `SESSION` (`D0` may choose otherwise) | no | no |
| `effectiveVisible`, status text, export frame and viewport, tessellations | `DERIVED_TRANSIENT` | — | never |

Evidence, rejected alternatives and the undo, save/reopen and verification
consequences per row are in the JSON mirror and in the candidates.

## 11. Serialization impact matrix

| Feature | Serialized | Owner and location | Legacy `.cedg` | Classic `.ggb` | Older GeoCeDG | Copy/paste | Undo | Migration |
|---|---|---|---|---|---|---|---|---|
| layer values above 9 (`A-2`) | yes | existing `<layer val>` | unaffected | input; unaffected | clamped to 9, lost on re-save | carried, clamped by the target build | stored layer restored | none |
| hidden layers (`A-2`) | yes | document-level GeoCeDG element | absent | absent | ignored | not carried | not an undo step | none |
| working layer, status bar, Move-group mode (`A-1`) | no | — | — | — | — | — | — | — |
| export surface, `ExportArea`, exact rendering (`B`) | no | — | export 2 px smaller | same | — | — | — | none |
| `constructionUnit`, `presentationUnit` (`D1`) | yes | `<construction>` child | absent → unspecified | absent → unspecified | dropped (lenient) or fail-closed (`AQ-U3`) | not carried | in the snapshot | none (lazy) |
| LocusV2/SplineV2 LaTeX, DXF `$INSUNITS`, engineering scale (`C`) | no (output files only) | — | — | — | — | — | — | — |
| GGT library (`E1`) | no format change; used tools save `<macro>` with owned `iconFile` | existing macro persistence | — | — | — | — | — | none |
| `DirectDimension`, `AxisDimension` (`E2`, β) | yes, additive `<command>` | existing output types | — | — | INFERRED: unknown command, element not loaded | carried with inputs | ordinary | none |
| `IsoABorder` (`E3`) | yes, additive `<command>` | existing output types; explicit scale pair and unit-factor inputs | — | — | as `E2` | carried | ordinary | none |
| orientation cue (`F1`) | no (preference) | — | — | — | — | — | — | — |
| authoring memory, hidden fixed angle (`F2`) | no (the angle's visibility is the existing object flag) | — | — | — | — | — | — | — |

No candidate silently widens layer XML semantics, array-index assumptions or a
numeric domain. The one widening (`A-2`) is explicit, bounded and gated by
`AQ-L1a` and `AQ-L1c`.

## 12. Application profile, action catalog, features and packaging

- **Catalog pin.** 115 actions (`geocedg-desktop/GeoCeDGProfile.java:340-343`),
  pinned also in `G9U1ActionRegistryTest`, `G9U1ProfileCompilerTest`,
  `G9U1WorkspaceSurfaceTest` and `GeoCeDGProfileTest`. Candidate additions:
  - `A`: +1 working-layer mode;
  - `B`: picture, PSTricks, PGF, Asymptote and four area actions;
  - `E2`: +2;
  - `E3`: +2 (`IsoABorder` and the ISO A area selection);
  - `F1`: +1 option.

  Each subphase updates the pin and its tests. The `edit-selection` toolbar ids
  and the group-order pins change with `A`.
- **Workspace specification** `geocedg/specs/ui/cedg-workspaces.md` still says
  110 actions and 11 toolbar groups (`:61`, `:95`, `:192-193`, `:221`, `:458`);
  the code has 115 and 12 (observation, owner `G`).
- **Feature manifests** (`geocedg/features/`). New user-visible capabilities
  need entries and maturity decisions. Presence in a toolbar is never
  promotion (`geocedg/features/README.md:7-10`).
- **Packaging.** Only `E1-P` touches it: the `.ggt` exclusion of the
  [Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md)
  (`geocedg/specs/packaging/windows-packaging.md:83-85`), enforced by `tools/agent/checks/packaging-product.ps1:562-567`.

## 13. Verification-class recommendation per later subphase

| Subphase | Planning proposal | P0 recommendation | Impact facts |
|---|---|---|---|
| `P0` | `DOCUMENTATION_STATUS_ONLY` | **confirmed** | documents only |
| `A` | `INTEGRATED_PHASE` (provisional) | **corrected:** `GLOBAL_IMPACT` unsplit; recommended split `A-1` `INTEGRATED_PHASE` (`PHASE` + `INTEGRATION`), `A-2` `GLOBAL_IMPACT` (`FINAL`) | the layer-domain widening changes a shared serialized range read on every load; hidden-layer persistence adds a document element; `A-1` covers every creation route and the export paint path |
| `B` | `INTEGRATED_PHASE` | **confirmed** | profile, dispatcher, shared export-area seam and export paint path, export viewport, `ExportImage` matrix rows (`AQ-X6`); no serialization |
| `D0` | `DOCUMENTATION_STATUS_ONLY` | **confirmed** | ADR and specification candidates |
| `D1` | `GLOBAL_IMPACT` | **confirmed** (`FINAL`) | new document-semantic `<construction>` element read on every load and paste wrapper, written on every save and undo; legacy byte identity must be proven |
| `C` | `INTEGRATED_PHASE` | **confirmed** | shared LaTeX package, DXF header under an amended accepted specification, three LaTeX exporters plus picture and DXF agreeing on area, units and visibility |
| `E1` | `BOUNDED_PHASE` + packaging evidence | **corrected:** split `E1-L` `BOUNDED_PHASE`; `E1-P` `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` (impact frozen at start) plus packaging smoke | packaging requires changing `tools/agent/checks/packaging-product.ps1`; a new `models/legacy` package would change `tools/agent/verify-legacy.ps1:210-215`, `:470-483` |
| `E2` | `INTEGRATED_PHASE` | **confirmed** for representation β; `GLOBAL_IMPACT` for α | new shared commands with additive XML, ADR 0031 gates, GGBScript matrix, profile pin |
| `E3` | `INTEGRATED_PHASE` | **confirmed** | new shared command with explicit unit-factor input, the two ISO `ExportArea` producers, units |
| `F1` | `BOUNDED_PHASE` | **confirmed** (screen-only overlay) | presentation overlay and preference |
| `F2` | `BOUNDED_PHASE` | **confirmed** | Desktop mode orchestration only |
| `G` | `INTEGRATED_PHASE` (`INTEGRATION`) | **confirmed**; `FINAL` if `E1-P` is frozen as `GLOBAL` | integrates every accepted subphase; inherits no evidence |

Each subphase registers its own `PHASE` selection, which updates the
registry-shape pins in `tools/agent/tests` through the official mechanism. That
is ordinary phase-local work. None of these campaigns was executed by `P0`.

## 14. Corrections to the planning pre-characterization

1. **Working-layer seam.** Overriding `getMaxLayerUsed` is insufficient: the
   cap at 8, rebuild leakage into dependent numerics and LocusV2 on layer 0
   (P3). The plan's §5.2 inference is corrected.
2. **View predicate.** Overriding `isVisibleInThisView` is **not** the least
   invasive correct placement. It changes slope-field and ODE geometry, and
   drawables are not removed or re-added (plan §5.3).
3. **Painting versus hit testing.** Painting reads cached drawable visibility;
   hit testing reads it live (plan §5.3).
4. **XML clamp.** The clamp sits in `GeoElement.setLayer`, not in the XML
   handler (plan §5.1).
5. **User-tool library.** The library **accepts** the sheet tools'
   whitespace-only `onUpdate` (P4; plan §8.2).
6. **Verifier pins.** `verify-legacy.ps1` pins exactly three legacy manifests
   and resources, so `E1`'s class is corrected (§13).
7. **Hidden-layers XML.** A hidden-layers child of `<euclidianView>` is **not**
   excluded from the preferences XML automatically; it would need an
   `asPreference` gate (plan §11, `AQ-L3` row).
8. **Paste and the unit rule.** Paste re-runs the `<construction>` handler, so
   the legacy-unit rule must live in `clearConstruction` (plan §7).
9. **Unknown XML.** Unknown elements are not skipped as subtrees (plan §7).
10. **Macro guard.** Any child written from `getConstructionXML` also needs a
    macro guard, not only a `<construction>` attribute (plan §7).
11. **Lazy-migration wording.** "Nothing written until needed; legacy bytes
    round-trip" paraphrases ADR 0029 and the durable-dependency specification.
    Their byte guarantee covers spatial records, and re-saving always rewrites
    the `<geogebra version>` header (plan §7).
12. **Axis `unitLabel` options** also include `""`, °, π, km and $ (plan §7).
13. **Command-line export** is handled in `GeoGebraFrame.checkCommandLineExport`,
    not `AppD.handleOptionArgs`. There is no `--scale` option.
14. **Hidden routes now proven:** `Ctrl+Shift+U` and `Ctrl+Shift+W` fire, and
    `Ctrl+Shift+D` fires twice (P1). Planning had them as source findings
    pending a probe.
15. **Padding and clipping now proven per format:** the off-view loss is
    narrower than planned (lines, conics, polygons and loci survive in the
    `exportPaint` family) and wider in SVG (P2).
16. **Toolbar group count.** The workspace specification is also stale on the
    group count (11 against 12).
17. **Hidden-layer recommendation changed.** The plan recommended per-view
    persistence captured by undo. P0 recommends document-wide persistence that
    is not an undo step (A candidate §7).
18. **Placement of the ISO A area selection.** It needs `D1`'s unit contract,
    while `B` runs before `D1`. P0 recommends delivering it in `E3` (`AQ-X7`)
    rather than adding a frontend-scoped `D1 → B` dependency. The author-fixed
    producer list is unchanged.
19. **Geometry must be unit-independent.** `IsoABorder` and the dimension
    decorations must not read units or scale live (D0 inputs §4a). The planning
    brief for `E3` ("model length = paper length × scale denominator ÷
    conversion(constructionUnit → mm)") is corrected to an explicit
    unit-factor input captured by the tool. The consequence is `AQ-E3c`.
    (Found by the independent fidelity review of this candidate.)
20. **Second command-line export route.** `--exportAnimation` also exists
    (`desktop/gui/app/GeoGebraFrame.java:852-1031`).

The plan's decomposition and author-fixed parts are **not** changed by `P0`.
The two recommended splits (`A-1`/`A-2`, `E1-L`/`E1-P`) and the placement in
item 18 are proposals for the author (`AQ-L1b`, `AQ-G5`, `AQ-X7`). P0 does not
pre-empt those decisions, so the plan and the roadmap's sequence are not
edited; the plan is updated after author review if the author accepts them.

## 15. Open author decisions (reconciled with plan §13)

| ID | Question | Blocks | P0 recommendation |
|---|---|---|---|
| `AQ-L1` | **fixed target** | — | design in the A candidate §3 |
| `AQ-L1a` | domain bound `L_MAX`; negative layers? A finite bound narrows plan §2's "arbitrary layer number" | `A-2` | `0..99`; no negatives; needs explicit acceptance of the bounded reading |
| `AQ-L1b` | split `A` into `A-1`/`A-2`? | `A` | split |
| `AQ-L1c` | accept that Classic and older GeoCeDG clamp layers above 9 (forward-compatibility loss for older readers)? | `A-2` | accept, documented |
| `AQ-L2` | working-layer persistence and initial value | `A` | `SESSION`; New → 0; Open → maximum drawable layer |
| `AQ-L3` | hidden layers: document-wide or per view; persisted; undo; LaTeX/DXF policy | `A`, `C` | document-wide, `DOCUMENT_PRESENTATION`, not undoable; LaTeX excludes; DXF in `C` |
| `AQ-L4` | **fixed target**; mode detail: one-shot or persistent | `A` | one-shot back to Move |
| `AQ-L5` | `ShowLayer`/`HideLayer` unchanged? | `A` | unchanged |
| `AQ-L7` | may the working layer be hidden? | `A` | no |
| `AQ-L8` | does paste move objects to the working layer? | `A` | no, keep copied layers |
| `AQ-X1` | **author-supplied**; implementation route | `B` | offscreen export viewport, prototyped first |
| `AQ-X2` | producer precedence; leftover selection rectangle | `B` | explicit producer > `Export_1/2` > viewport; rectangle never |
| `AQ-X3` | close hidden routes (`Ctrl+Shift+U/C/W/M/B/D`, CLI, API, `ExportImage`, v1 fallback) | `B` | route U, C, D and API through GeoCeDG and `ExportArea`; consume W and M; author decides B; filter the v1 fallback too (leaving it would contravene "do not expose") |
| `AQ-X4` | clipboard, GIF and print preview | `B` | print preview kept; clipboard via the picture dialog; GIF out |
| `AQ-X5` | command-line `--export` and `--exportAnimation` in GeoCeDG | `B` | route `--export` through `ExportArea` or reject it; reject `--exportAnimation` while GIF is out |
| `AQ-X6` | do scripting `ExportImage` and the API obey `ExportArea` (making `ExportImage` a GeoCeDG-modified command with matrix rows)? | `B` | yes |
| `AQ-X7` | where the ISO A area selection is delivered | `B`/`E3` | in `E3`, with the `IsoABorder` producer |
| `AQ-U1` | **resolved** | — | — |
| `AQ-U2` | which results present units | `D0` | native dimensions first |
| `AQ-U3` | unit set, tokens, lenient or fail-closed | `D0` | `mm`, `cm`, `m`; decide fail-closed explicitly |
| `AQ-U4` | cross-document paste policy | `D0` | accept with a non-blocking notice; never convert |
| `AQ-U5` | export `drawingScale` persistence | `D0`/`C` | `SESSION`, default `1:1` |
| `AQ-U6` | engineering export and `IsoABorder` with an unspecified unit | `D0` | unavailable until a unit is declared |
| `AQ-C1` | exact Bézier output for `SplineV2` of degree ≤ 3 | `C` | not in `C` |
| `AQ-G1` | **design work** → structure | `E1` | E1 candidate §5: provenance stays in `models/legacy/template-v7`; curated library under `models/curated/ggt-library/` (a small `AGENTS.md` §3.2 amendment); `tools/ggtfiles/` rejected |
| `AQ-G2` | shipped selection; retire references | `E1` | E1 candidate §7 |
| `AQ-G3` | **author-supplied** | `E1` | per-output map §6.3 |
| `AQ-G3a` | does "corner points hidden" include the input `D`? | `E1` | needs author answer |
| `AQ-G4` | rights record for the macro definitions | `E1-P` | explicit record and author decision; no agent conclusion |
| `AQ-G5` | packaging route; split `E1` | `E1` | split `E1-L`/`E1-P`; admit `.ggt` only at the library path with manifest hash |
| `AQ-G6` | ingest the 2024 standalone archives as a legacy package? | `E1` | no; record their hashes as provenance evidence |
| `AQ-G7` | meaning of "black at 3 pt" (GeoGebra thickness 3, a 1.5 px stroke, or 3 typographic points) and whether role-specific line types stay | `E1` | needs author answer |
| `AQ-E1` | dimension representation | `E2` | β (composition) |
| `AQ-E2` | `IsoABorder` border only | `E3` | yes |
| `AQ-E3a` | `IsoABorder` scale input form | `E3` | integer pair |
| `AQ-E3b` | export linkage across rebuilds | `B`/`E3` | session link dropped on rebuild |
| `AQ-E3c` | after a `constructionUnit` change, `IsoABorder` keeps its coordinates and represents a different paper size until its unit factor is edited | `E3` | accept: the only behavior compatible with plan §4.1 |
| `AQ-E4` | extension-line overshoot and gap | `E2` | explicit model-unit inputs chosen by the tool at creation; never read live from units or `drawingScale` |
| `AQ-E5` | dimension side flips at 45° under the canonical-axis rule | `E2` | pin the canonical axis at creation |
| `AQ-F1` | cue global or per object; exported? | `F1` | global, screen-only |
| `AQ-R7` | **closed by the planning closeout** | — | — |

## 16. Out-of-scope observations and proposed owners

Pre-existing observations are kept as observations. None is turned into
product scope or fixed.

| ID | Observation | Evidence | Owner |
|---|---|---|---|
| `OBS-R6P-SAVE-WARNING` | the Locus V2 save warning has no call site | `geocedg-desktop/GeoCeDGExternalCompatibilityWarning.java:27` (definition only, confirmed) | author disposition |
| `OBS-R6P-DXF-DEFAULT-DOC` | the extended-DXF note says default-off while the default is on | planning evidence | `G` |
| `OBS-R6P-DXF-SPEC-HEADER` | the DXF fidelity specification header says "implementation not authorized" | planning evidence | `G` |
| `OBS-R6P-DXF-VISIBLE-CONTRACT` | "visible geometry" contract name versus hidden objects exported with `60 = 1` | `geocedg-common/export/DxfExporter.java:300-306` | `C` |
| `OBS-R6P-DOC-IDENTITY-HEADER` | the native-document-identity specification header is stale; ADR 0016 is stale the same way; ADR 0029's header still says "Proposed" | `geocedg/specs/ui/native-document-identity.md:10`; `docs/adr/0016-native-geocedg-document-identity.md:6-7`; `docs/adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md:3-4` | `G` |
| `OBS-R6P-CTRL-SHIFT-D` | now **PROVEN BY PROBE**: a latent defect that toggles selection and slider fixing on every object while opening DXF | P1 | `B` |
| `OBS-R6P-ACTION-COUNT-DOC` | workspace specification counts (110 actions, 11 groups) are stale | §12 | `G` |
| `OBS-R6P0-HIDDEN-EXPORT-ROUTES` | picture dialog, worksheet upload, HTML5 and Base64 clipboard, CLI (`--export`, `--exportAnimation`), `ExportImage` and the API are reachable outside the menu | P1; `desktop/gui/app/GeoGebraFrame.java:852-1031` | `B` |
| `OBS-R6P0-CLI-EXPORT-ZERO-SIZE` | CLI `--export` wrote a 0-byte PNG (zero-size view) | P1 | `B`, if the route is kept |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | LocusV2 and SplineV2 created on layer 0 (re-proven) | P3 | `A` (the creation hook closes it) |
| `OBS-R6P0-STALE-REFERENCE-FLAG` | `isInConstructionList` answers `true` for objects replaced by undo | P6; `common/kernel/Construction.java:1273-1278` | none (upstream); consumers must not rely on it |
| `OBS-R6P0-ROTATE-TEXT-POINT` | `Rotate(text, angle, point)` ignores the point; the rotated text has no start point | P5 | none (upstream); input to `E2` |
| `OBS-R6P0-LOCUSV2-SCREEN-TESSELLATION` | Locus V2 picture export tessellates at 0.75 view pixels, not export resolution | `LocusRenderPolicy2D.java:20-23` | `B` (the export viewport) |
| `OBS-R6P0-CONSTRUCTION-XML-SWALLOW` | `getConstructionXML` swallows exceptions, which can leave a truncated `<construction>` (INFERRED) | `common/kernel/Construction.java:1545-1546` | `D1` awareness |
| `OBS-R6P0-PLAN-STATUS-STALE` | the mini-track plan's §3 State column and §14 still show `P0` as not authorized; the roadmap is the living status authority and is updated | `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` §3, §14 | `G`, or the `P0` closeout after review |

## 17. Recommended scope and order for the later prompts

No prompt file is created; the author generates them after reviewing this
candidate.

- **Order:** the author's order stays valid:
  `A (A-1) → B → D0 → D1 → C → E1 → E2 → E3 → F1 → F2 → G`. `A-2` may be
  scheduled adjacent to `D1`, since both need `FINAL`. `D0` may run beside `A`
  or `B`. `F1` and `F2` may move earlier. `E1-P` waits for `AQ-G4`. Under
  `AQ-X7`, `E3` also delivers the ISO A area selection.
- **Each prompt should freeze:**
  - the subphase's candidate document as its design input;
  - the decisions in §15 that block it;
  - the class in §13;
  - the catalog-pin delta;
  - the ADR 0031 and GGBScript-matrix obligations for new commands;
  - the explicit degeneracies of its candidate.

## 18. Paths changed and product-effect proof

The candidate changes exactly these paths:

```text
M  .github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md
A  docs/architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_b_export_area_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_c_semantic_export_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md
A  docs/architecture/pre_g9b_r6_plus_e1_ggt_library_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_e3_iso_a_border_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md
A  docs/architecture/pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md
M  docs/roadmap/geocedg_roadmap.md
A  docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-p0-author-input-inventory.json
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-p0-persistence-matrix.json
```

- **No changes under:** `source/`, `apps/`, `packaging/`, `geocedg/specs/`,
  `geocedg/features/`, `geocedg/resources/`, `models/`, `tools/` or
  `artifacts/`.
- **Not touched:** `prompt-contracts.json`, the registry, schemas, `AGENTS.md`,
  `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/**`, and every other
  prompt.
- **Not changed:** product code, tests, build, serialization, profile, feature
  manifest, verifier, guide, specification and ADR.
- Probe sources, outputs and scratch copies stay in the session scratchpad. The
  probe classes compiled into the ignored `build/` directory were removed by a
  clean recompile without the init script.

```text
PRODUCT_PHASE_EFFECT               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT                       = NONE
selfApproved                       = false
```

## 19. Roadmap status

The roadmap moves to version 4.35 and records `PRE-G9B-R6-plus-P0` as
`TECHNICAL DESIGN CANDIDATE PENDING AUTHOR REVIEW`, in its header, its track
paragraph and the `P0` sequence row. Nothing is marked authorized, approved or
`PASS`, and the plan is not edited (§14).

## 20. State when this candidate was frozen

This frozen report is never amended to follow later decisions. The author's
review is the sole authority for the outcome.

```text
TECHNICAL_CANDIDATE_STATE  = FROZEN
AUTHOR_DECISION            = NOT_RECORDED_IN_THIS_ARTIFACT

PRE-G9B-R6-plus-P0         = TECHNICAL DESIGN CANDIDATE PENDING AUTHOR REVIEW
PRODUCT IMPLEMENTATION     = NONE
PRE-G9B-R6-plus-A … G      = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED BY PRE-G9B-R6-plus
G9B                        = NOT AUTHORIZED
publication                = NONE (local commit only; no push, merge, tag or promotion)
```

The candidate's own commit, tree, `STATIC` run and `git diff --check` are
reported with the candidate, outside this file, because this file cannot name
its own commit.

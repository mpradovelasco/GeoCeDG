# PRE-G9B-R6-plus — planning candidate report

- Status: **PLANNING CANDIDATE PENDING AUTHOR REVIEW**
- Recorded: 2026-10-01
- Task: `PRE-G9B-R6-plus` mini-track planning and canonical `P0` prompt design
- Authorization: author instruction of 2026-10-01, planning and governance work
  only
- Planning base: `P_R6` = `9b8bc5b10a7ef61da0095853ea4c572ca06bfa72`, tree
  `f5c8c9f0cc76982170c6282a1242d2a5d07a0897`
- Change route: `ORDINARY`
- Frozen verification class: `DOCUMENTATION_STATUS_ONLY`
  (`frozenAtPhaseStart = true`)
- Claim vocabulary: **proposed / not normative** planning; no normative
  geometric, unit, export or layer contract is created here
- Self approval: **false**

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PRODUCT_PHASE_EFFECT      = NONE
```

This report is the evidence record of the planning task. The architecture and
the decomposition it supports are in the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md). The
first executable task is the proposed future prompt
[`PRE-G9B-R6-plus-P0`](../../.github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md).
Nothing here authorizes `P0` or any later subphase.

## 1. Entry identities

Verified at task entry, before any edit:

```text
local main       = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
origin/main      = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
live remote main = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
HEAD tree        = f5c8c9f0cc76982170c6282a1242d2a5d07a0897
tracked worktree = clean
PRE-G9B-R6       = PASS — AUTHOR APPROVED — PUBLISHED
```

No `PRE-G9B-R6-plus` implementation existed. No canonical `R6-plus-P0` prompt
existed. `PRE-G9B-R7` was unexecuted and unauthorized; its proposed future
prompt carries the dependency `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED`. The
local branch `feature/pre-g9b-r6-plus-planning` was created at `P_R6`.

## 2. Classification and verification

The live contract (`geocedg/specs/operations/verification-levels.md` §12.8)
maps `DOCUMENTATION_STATUS_ONLY` to "Static validation only; no FULL". The
class was frozen before editing because this task changes only planning,
roadmap and prompt documents. The precedents are `PRE-G9B-R0` and the
`AD-R0-9` prompt-stub task.

The task touches no `geocedg/specs/operations/verification-*` file, no
`prompt-contracts.json`, no `tools/agent/**` script and no registry entry. The
new prompt is not registered in `prompt-contracts.json`. That matches the
standing convention for unexecuted phase prompts, which
`prompt.execution-safety` does not enumerate. It is validated directly with the
parser instead (§9).

```text
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, toolchain, Gradle, Conda, packaging,
  download or environment contract changes.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION = planning only; no observable product behavior, command,
  tool, menu, persistence, installation path or exported format changes. Help
  reconciliation is planned as subphase G.
```

## 3. Local author input

`artifacts/author-input/g9b-r6-plus/` exists in the local worktree and is
ignored by `.gitignore` rule `/artifacts/*` (line 7). Its presence is not a
dirty-worktree condition. None of these files was committed, moved, renamed or
modified. Each `.ggt` was copied to the session scratchpad and unpacked there.

| File | Bytes | Role | SHA-256 |
|---|---:|---|---|
| `ReportExport.md` | 26 074 | author-commissioned analysis of the upstream export surface | `fbf8524cfdf147cbd6999f9ff3d6f5bfc8a48b164b236a4df21aad61bb8f52d3` |
| `axisdimension.ggt` | 2 986 | dimension tool reference (`axisDimension`) | `7989b4a87e1e10275627bc22ec3cb8f6783af7b05052dbbcc76610208b0d0936` |
| `circarcbyangle.ggt` | 2 308 | arc utility (`circArcbyAngle`) | `640f0906749b2396c676e0b3ed1f7324599ef1ec3302704784f225ed22fa5348` |
| `circlebyD.ggt` | 2 143 | circle utility (`CirclebyD`) | `977d812d55d8a151f7ed8caa2983d0960daab72f7fc3010f5cccfe4b15bd76f8` |
| `directdimension.ggt` | 2 708 | dimension tool reference (`directDimension`) | `228d54d7f08316cbed9c0a84bc5d62085ca59931e573908990c84d60cacf0a01` |
| `ductsymbol.ggt` | 3 000 | presentation symbol (`DuctSymbol`) | `a2c1faa18d1727dd59bf590f998c360bb66d473174c605f4e127c96f5ea42890` |
| `ellipseaxisbyconjaxis.ggt` | 3 747 | conic utility (`conj2mainAxesEllipse`) | `7639c82aa2aad8b470f9f6ab9130e419c3e321f33f8d1ccc672326acf6c40539` |
| `ellipsebyaxis.ggt` | 1 490 | conic utility (`EllipseAxis`) | `01437a90761366b619178cde37a3ebb88bc7ac51b6de0b04e3369d38ac77040b` |
| `ellipsepartiallength.ggt` | 2 495 | conic utility (`ellipseLength12`) | `35c20a5d35d7bce362d36f2ef10f8db4d1b719e1254566234268f20cd16eb293` |
| `IFpositivethenPoint.ggt` | 1 263 | selection utility (`IFPositiveSelectPoint`) | `c490e221a90e2acaeeaad3575ba172acc6cf0e82fcb765560bcafeb9cb7beb27` |
| `pointjump.ggt` | 988 | transport utility (`pointJump`) | `a350031eb98be790627dd15294550a07638d0b7968c64e783695976ef8ba76f1` |
| `relcoor.ggt` | 2 327 | transport utility (`relCoor`) | `95ff380d56bb70fd07457b06b977fb27d03de22bfc0dfdd4850ea4bed8b7a0b2` |
| `sheetISOland.ggt` | 4 900 | ISO sheet reference, landscape (`sheetISOAnLand`) | `d3b05cafb5d938ec4ddf096c0e1df60c7c0850ca6327cb8d42e0ca06c18e5c23` |
| `sheetISOvert.ggt` | 7 268 | ISO sheet reference, portrait (`sheetISOAnVert`) | `4412824a19da659900dc269924bddfc6f5f03981c3f39c9286b08ef5538e2015` |
| `squarebydiagonal.ggt` | 1 400 | planar utility (`SquarebyDiagonal`) | `29e2a3d88d926f75f9b7fc0e24290340a10f092da7e161dc19c3a83129e9470f` |
| `symmsymbol.ggt` | 2 219 | presentation symbol (`SymmSymbol`) | `491628c089749cd99b5759b0ed9259e530284529bedb3d2f8dc33d44b01fa74e` |
| `translationcoor.ggt` | 2 553 | transport utility (`translationCoor`) | `ef05762d7e1847185374d737d929f868616e6c8a8d270483bd4b9c91ed47b42b` |

Seventeen files, 69 869 bytes. No other asset (icon sources, models, images)
was supplied. No expected input is missing for planning. Two author inputs are
referred to but not supplied: the list of requested `sheetISOland` and
`sheetISOvert` modifications, and the concrete clipping and padding
observations. Both are recorded as author questions (§8).

## 4. GGT pre-characterization

### 4.1 Provenance: the supplied tools are already preserved

Each archive holds one `geogebra_macro.xml` (UTF-8, LF), one `<macro>` and,
for 13 of the 16, one 32 × 32 PNG icon. None has scripts, JavaScript or a
defaults file. The root element is `format="5.0" app="classic" platform="d"`.
The writer versions are 5.2.820.0–5.2.836.0, not the 5.4.928.0 baseline.

The decisive finding is that **all 16 macros already exist in the repository**,
embedded in the G3 legacy package `cedg.legacy.template-v7`
(`models/legacy/template-v7/original/Templatev7.ggb`, SHA-256
`f62e5b7a…ed4113`, author Manuel Prado-Velasco, written by 5.2.879.0). The
comparison was made in memory against the read-only original:

- **command name and ordered inputs/outputs:** equal to the committed
  inventory `models/legacy/template-v7/derived/tool-inventory.yml` for all 16;
- **macro construction:** equal for all 16 after removing the
  `<eqnStyle style="implicit"/>` tags that the later 5.2.879.0 writer emits on
  segments. Nine constructions are byte-equal after whitespace normalization
  without that removal;
- **macro attributes:** equal, except that `ellipsebyaxis`, `pointjump` and
  `squarebydiagonal` carry no icon while the template names one.

So the supplied `.ggt` files are earlier standalone exports of tools that G3
already preserved, characterized and curated. The author selected exactly the
24 template tools minus the eight that are superseded: the six class-1 native
replacements (`SplineLength`, `Perimeter`, `listLength`, `listLength12`,
`postLocus`, `dummyRotate`) and the two presentation-visibility devices
(`PoliLineVisibility`, `ellipseVisibility`). The
[G9U1 user-tool review](g9u1_user_tools_review.md) class of each retained tool
is the starting disposition in the plan §8.

Two rights facts carry over. The template manifest records redistribution
review as **blocked**, because embedded images have no source-licence
clearance. Twelve of the supplied PNGs carry GeoGebra stock toolbar names
(`GeoGebra_button_distance.png`, `GeoGebra_icon_perspective1.png`, …); only
`circulodiametro.png` has a custom name. Their provenance and licence were not
established. This independently supports the author's requirement for
GeoCeDG-owned icons.

### 4.2 Per-tool summary

Styles are fixed inside each macro. Upstream applies them on use
(`AlgoMacro.createOutputObjects`, `source/shared/common/.../kernel/algos/AlgoMacro.java:298`)
and then **forces every output onto `app.getMaxLayerUsed()`**
(`AlgoMacro.java:291` and `:306`), discarding the layer stored in the macro.

| Tool | Inputs → outputs | Behavior | Notes |
|---|---|---|---|
| `directDimension` | `A, B, kconversion` → `m p u v D g distanceDE TextDE` | aligned dimension; extension lines ⟂ AB; arrows from two opposed vectors | value text is `kconversion·Distance(D,E)`; placement point `D` on the visible infinite output line `g` |
| `axisDimension` | `A, B, f, kconversion` → `u v TextDE distanceDE p m D r` | dimension of AB projected on direction `f` | `D` on a visible guide segment `r` of length `20·|AB|` |
| `sheetISOAnLand` | `D, ISO216n, CeDGScale, CeDGMargin` → 21 objects | landscape A_n trim edges, border, unlabelled title-block grid | sizes from `Iteration`, divided by `CeDGScale` |
| `sheetISOAnVert` | same → 22 objects | portrait by `Rotate(…, −90°, D)` plus translation | title block **not** rotated |
| `DuctSymbol` | `A, B, lt` → 6 objects | tube with conventional break end | presentation only |
| `SymmSymbol` | `A, f, ds` → 2 segments | symmetry end marks | presentation only |
| `circArcbyAngle` | `A, B, phi` → `c, B'` | signed-angle arc | `If(phi > 0, …)` |
| `CirclebyD` | `A, diameter` → `g` | circle from centre and diameter | |
| `EllipseAxis` | `O, B, C` → `e` | ellipse from centre and axis ends | silently assumes `C` lies on the axis ⟂ `OB` |
| `conj2mainAxesEllipse` | `A, B, C` → 4 points | Rytz construction of the axis vertices | |
| `ellipseLength12` | `f, H, C` → `g, a` | ellipse arc and its native `Length` | |
| `SquarebyDiagonal` | `A, B` → 4 sides, 2 points | square from a diagonal | |
| `pointJump` | `A, f, Avance` → `B` | signed step along a direction | |
| `relCoor` | `D, C, f` → `relx` | signed coordinate of `D` along `f` from `C` | |
| `translationCoor` | `D, C, f, F, i` → `I` | transfer of `relx` from `F` along `i` | |
| `IFPositiveSelectPoint` | `M_4, L_4, v_1` → `O_4` | `If(v_1 > 0, L_4, M_4)` | |

### 4.3 Dimension tools: the observed pathologies have exact causes

- **Hidden unit multiplier.** The value is `kconversion · |DE|`, with
  `kconversion` a user input whose template value is 10 (INFERRED: cm → mm).
  The text has no unit and no scale awareness. This is the "manual unit
  multiplier inside the geometry" that the native tools must eliminate.
- **Far-away placement.** The only placement control is the draggable
  point-on-path `D`. Source-read: `D = A + t·rot90(B − A)` for
  `directDimension`, stored `t ≈ 1.02`, so the dimension line starts about
  `1.02·|AB|` from AB and the offset grows with the measured length. For
  `axisDimension` the guide spans `±10·|AB|` and the stored offset is about
  `2.05·|AB|`, even when AB is almost perpendicular to `f`. The visible guide
  outputs (`g`, an infinite line; `r`, a `20·|AB|` segment) extend the drawing
  and its bounds.
- **Overshoot.** The extension lines overshoot by `|AD|/10`, so they also scale
  with the offset rather than having a fixed length.
- **Text.** Always horizontal, positioned at the screen projection of
  `Midpoint(D, E)` plus a fixed pixel offset, so its distance from the
  dimension line changes with zoom.

### 4.4 Sheet tools: ISO sizes, scale and defects

- **Sizes are the unrounded theoretical A series**, INFERRED in cm:
  long side `100·2^(1/4)·2^(−n/2)`, short side `100·2^(−1/4)·2^(−n/2)`. At
  `n = 3` the stored values are 42.0448 × 29.7302, that is
  420.45 × 297.30 mm, while ISO 216 specifies A3 as 420 × 297 mm. A native
  border must use the ISO 216 nominal sizes, not this formula.
- **Scale and margin.** Sizes and margin are divided by `CeDGScale`. There is no
  guard for a non-positive scale. The margin is uniform on four sides.
- **Title block.** Designed for A3 in cm (18 × 3.6) and scaled by
  `long/42.04`, `short/29.73`. It has grid lines only, no fields or text. In
  portrait it keeps the landscape size and orientation.
- **Defects.** Both tools carry an unlabelled internal point with NaN
  coordinates from a two-output `Intersect`; the portrait tool duplicates its
  left edge and contains redundant duplicate objects.
- **Export.** Neither tool creates `Export_1`/`Export_2`, so neither defines an
  export area today.

The requested `sheetISOland`/`sheetISOvert` modifications were not supplied
with this instruction. They remain an author input for `P0` (§8).

## 5. Corrections and confirmations to the planning assumptions

Each item cites, in brackets, the section of the
[mini-track plan](../architecture/pre_g9b_r6_plus_minitrack_plan.md) or of this
report ("report §") that evidences it.

**Corrected**

1. *A layer information strip exists.* It does not. GeoCeDG has no status bar or
   layer strip; the only information strip is the toolbar help panel. The status
   bar is new work in `A`, and the unused `SOUTH` slot of the application panel
   can host it [§5.4].
2. *Arbitrary layer numbers are a local change.* The domain is 0..9, clamped in
   `GeoElement.setLayer`, and several consumers assume it. Widening is a global
   change; the plan proposes keeping 0..9 in this track (`AQ-L1`) [§5.1].
3. *The working-layer tool can sit with the Move tools.* Only if it is a mode:
   native toolbar groups reject non-mode actions [§5.5].
4. *Hiding a layer from the Algebra View is available upstream.* Upstream
   `ShowLayer`/`HideLayer` rewrite each object's own visibility, which the
   author excludes; group nodes have no toggle [§5.3, §5.4].
5. *The supplied `.ggt` files are new input.* They are construction-equivalent to
   macros already preserved, curated and classified in the G3 `template-v7`
   package, whose redistribution review is blocked [report §4.1].
6. *`tools/ggtfiles/` is the library location.* It conflicts with `AGENTS.md`
   §3.2 and §5 and the controlled-integration contract (`AQ-G1`) [§8.2].
7. *Installer integration of `.ggt` files.* The accepted Windows packaging
   contract makes the pipeline fail on `.ggt` content; it needs an explicit
   amendment (`AQ-G5`) [§8.2].
8. *Icons can be generated.* No tool-icon generator exists; owned tool icons are
   hand-authored SVGs in the asset manifest [§8.2].
9. *GGT style memory.* Macro outputs carry the macro's fixed style and the style
   bar cannot target them; memory needs a new frontend hook [§8.3].
10. *ISO A sizes in the legacy sheets.* They are the unrounded theoretical series
    (A3 ≈ 420.45 × 297.30 mm), not ISO 216 nominal sizes [report §4.4].
11. *`LocusV2`/`SplineV2` reach the LaTeX exporters.* They are silently skipped
    with a debug log; only DXF exports them semantically today [§6.5].
12. *`selectionRectangle` > `Export_1/2` > viewport holds for every exporter.*
    The LaTeX exporters ignore `Export_1/2`; SVG is not re-rendered against the
    export frame [§6.2].
13. *`R6-plus` is independent of later gates.* It overlaps `G11`, `G12` and
    `G15`; the plan delivers bounded slices without moving their ownership
    [§1.1].
14. *Excluded exports are unreachable.* In source, `Ctrl+Shift+W` still opens the
    GeoGebra.org worksheet upload and `Ctrl+Shift+U` the picture export; whether
    they fire in GeoCeDG is a `P0` probe [§6.1].

**Confirmed**

1. Every legacy `cm` is output-oriented or a free-text axis label; no geometry
   reads a physical unit [§7].
2. `UNSPECIFIED_MODEL_UNIT` for legacy documents met no compatibility blocker in
   pre-characterization; lazy migration keeps legacy bytes unchanged. `P0` must
   still confirm it [§7].
3. The accepted export foundation already anticipates a physical-unit contract
   that must not reinterpret screen scale [§4.5].
4. The explicit-area principle fails today: points, function graphs, axes, grid
   and some labels are lost outside the viewport, and the only padding is `+2`
   px for `Export_1/2` [§6.3].
5. The legacy dimension tools hide a `kconversion` multiplier and place the
   dimension line at an offset proportional to `|AB|` [report §4.3].
6. Kernel line, segment and ray orientation is well defined and already
   observable; `getDirectionInD3` is reversed for lines without an end point
   [§9.1].
7. `Construction → Annotations and media` and the Metrics and validation group
   (holding Distance) exist in the profile [§8.5, §8.6].
8. New commands must pass the ADR 0031 gates and extend the `PRE-G9B-R6` GGBScript
   matrix, which fails closed [§8.5].

## 6. `ReportExport.md` claim ledger

The author report targets the right baseline and is largely accurate. It
describes **upstream** behavior; it does not describe what GeoCeDG exposes.

| # | Claim | Ledger |
|---|---|---|
| 1 | Baseline 5.4.928.0, commit `9b93256b…` | `CONFIRMED` |
| 2 | Export submenu contents; Collada only when `is3D()` | `CONFIRMED` for upstream `FileMenuD`. `CORRECTED` for GeoCeDG, which never builds that menu and exposes only DXF and Print preview |
| 3 | Picture formats PNG, PDF, SVG, EMF; no EPS; label "(png, svg)" | `CONFIRMED` |
| 4 | DPI list 72–600, default 300; 3D view PNG only | `CONFIRMED` |
| 5 | PDF size from `printingScale`, x-scale and export size | `CONFIRMED`; `printingScale` is zoom-derived and transient |
| 6 | `selectionRectangle` > `Export_1/2` > full view | `CONFIRMED` for picture, clipboard, GIF and print; plus `+2` px padding with `Export_1/2`, and losses outside the view |
| 7 | Crop and translate, no fit scaling | `CONFIRMED` |
| 8 | `Export_1/2` found by label, both `GeoPoint` | `CONFIRMED` |
| 9 | Clipboard and Animated GIF share the rule | `CONFIRMED` |
| 10 | LaTeX: selection > visible bounds, `Export_1/2` not consulted, editable bounds update the rectangle | `CONFIRMED`; the rectangle is set only after a bound is edited |
| 11 | `GeoGebraExport` handles `GeoImplicit` and `GeoLocus` | `CONFIRMED`; `CORRECTED` in consequence: `GeoLocusV2` is not a `GeoLocus` and is skipped |
| 12 | STL is ASCII, scaled to `EDGE_FOR_PRINT = 40` ("4 cm"), with `STLscale`/`STLthickness` | `EDGE_FOR_PRINT` `CONFIRMED`; the rest not checked here, because STL is excluded from this track; `P0` classifies it |
| 13 | Collada declares centimetres | `CONFIRMED` |
| 14 | Dynamic Worksheet uploads to GeoGebra.org | `CONFIRMED`; still reachable in source through `Ctrl+Shift+W` |
| 15 | `Export_1/2` is a presentation convention, not an identity precedent | consistent with `AGENTS.md` §12.1 |
| 16 | STL/Collada are not precedents for semantic spatial export | consistent; out of scope |

## 7. Observations outside this task's scope

Pre-existing at `P_R6`, found during pre-characterization, **not** fixed here:

| ID | Observation | Evidence | Suggested owner |
|---|---|---|---|
| `OBS-R6P-SAVE-WARNING` | the Locus V2 save warning required by the public-surface specification has no call site | `GeoCeDGExternalCompatibilityWarning.confirmSave` defined only; `geocedg/specs/locus/locus-v2-public-surface.md:608-611` | author disposition |
| `OBS-R6P-DXF-DEFAULT-DOC` | the extended-DXF architecture note says "default-off" while the default is on | `docs/architecture/g9_extended_dxf_architecture.md:3-4`; `AppConfigGeoCeDG.DEFAULT_EXTENDED_DXF_ENABLED = true` | `G` |
| `OBS-R6P-DXF-SPEC-HEADER` | the DXF fidelity specification header still says "implementation not authorized" | `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:7` | `G` |
| `OBS-R6P-DXF-VISIBLE-CONTRACT` | the DXF action's selection contract is named "visible geometry", but hidden objects are exported with group `60 = 1` | `apps/geocedg/application-profile.yml:2209`; `DxfExporter.java:303-305` | `C` |
| `OBS-R6P-DOC-IDENTITY-HEADER` | the native-document-identity specification header still says "not authorized / not started" | `geocedg/specs/ui/native-document-identity.md:6-10` | `G` |
| `OBS-R6P-CTRL-SHIFT-D` | GeoCeDG's DXF accelerator is also the upstream chord that toggles "selection allowed" on all objects | `GlobalKeyDispatcher.java:898-952` | `P0` probe, then `B` |
| `OBS-R6P-ACTION-COUNT-DOC` | the workspace specification still names 110 stable action IDs; the compiled catalog is pinned at 115 since `PRE-G9B-R4` | `geocedg/specs/ui/cedg-workspaces.md:457-458`; `GeoCeDGProfile.java:340-343` | `G` |

## 8. Author decisions

The decisions requested before each subphase are listed in the plan §13
(`AQ-L1` … `AQ-R7`). Two inputs the instruction refers to were not supplied:
the requested `sheetISOland`/`sheetISOvert` modifications (`AQ-G3`) and the
concrete clipping and padding observations (`AQ-X1`).

The documentation-maintenance contract (§7, step 5) asks that a change of phase
relationships update the roadmap, the phase plan, affected prompts and machine
evidence. This task updates the roadmap and the plan, creates the `P0` prompt,
and adds the machine-readable mirror
`geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-planning.json`. One
affected prompt is **deliberately deferred**. The proposed future `PRE-G9B-R7`
prompt still says the scope of `PRE-G9B-R6-plus` is "pending author
definition", although its dependency on `PRE-G9B-R6-plus = PASS — AUTHOR
APPROVED` remains true. The instruction authorized creating the `P0` prompt
only, and `CLAUDE.md` forbids editing `.github/prompts/**` as a side effect of
another task. `AQ-R7` asks the author to authorize that minimal amendment.

## 9. Canonical `P0` prompt and its validation

Path:
`.github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md`.

The prompt carries the verbatim banner `PROPOSED FUTURE PROMPT — UNEXECUTED AND
NOT AUTHORIZED.`, `selfApproved`, `authorApproved`, `implementationAuthorized`
and `passClaimed` all `false`, and `IMPLEMENTATION_BASE = UNRESOLVED — MUST BE
FROZEN AT AUTHORIZATION`, with `P_R6` recorded as provenance only. It carries
the seven `geocedg-field` markers in the `task` profile order.

It was validated with `Test-PromptContractDocument` from
`tools/agent/prompt-contract-parser.psm1` against the `task` profile of
`geocedg/specs/operations/prompt-contracts.json`, the mechanism the
`PRE-G9B-R6` closeout used for an unregistered prompt. Result: no missing,
duplicate, unknown or out-of-order fields, one title and
`execution_safe = true`. That flag is a structural parser result meaning the
execution-safety fields are complete; it is not permission to execute. The
prompt is not registered in the catalog, following the convention for
unexecuted phase prompts, so `prompt.execution-safety` and its pins are
unchanged.

## 10. Verification of this candidate

```text
VERIFICATION_CLASS = DOCUMENTATION_STATUS_ONLY (frozenAtPhaseStart = true)
PLANNED_ACCEPTANCE = STATIC on the exact frozen candidate; no FINAL
```

Pre-commit checks on the working tree, which are **not** acceptance evidence:

- the prompt-contract validation above;
- repository-relative link resolution of every Markdown document this task adds
  or changes (no automated link checker exists, as `PRE-G9B-R0` recorded);
- JSON validity of the machine-readable mirror;
- byte-level end-of-line inspection and `git diff --check`;
- an independent read-only fidelity review of the four documents against the
  instruction, the governance contracts and 32 spot-checked source claims. It
  found no blocker; its eight major and sixteen minor findings were corrected
  before freezing, including the scoping of three dependencies that would
  otherwise have made frontend or resource work a prerequisite of kernel
  semantics.

The acceptance run is `tools/agent/verify.ps1 -Profile STATIC -LogDirectory
<new external root>` on the clean frozen candidate. This report is part of that
candidate and cannot name the candidate's own commit or its run, so both are
reported with the candidate outside this file. No evidence is reattributed
between commits.

## 11. Roadmap edits

Only `docs/roadmap/geocedg_roadmap.md` among living documents changed, at six
sites that were already in `LF` regions of that mixed-EOL file, written
byte-level with `LF` (the `CRLF` line count is unchanged):

1. the document version, `4.32` → `4.33`;
2. the `Siguiente puerta` header sentence on `PRE-G9B-R6-plus`;
3. the `PRE-G9B-R` track status paragraph;
4. the sequence table: the `PRE-G9B-R6-plus` row is replaced and twelve
   subphase rows are added. Each is `NOT AUTHORIZED` except `P0`, which is
   `DESIGNED / CANONICAL PROMPT CREATED / NOT AUTHORIZED`, and each row names
   its hard dependency, recommended predecessor and global gate;
5. the `PRE-G9B-R7` row, whose status keeps `DESIGNED — NOT AUTHORIZED` and
   now reads `BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED`, the
   wording the instruction requires, with the same meaning as before;
6. the dependency-class paragraph, which now names the three dependency
   classes of the mini-track and links the plan.

`PRE-G9B-R6` stays `PASS — AUTHOR APPROVED`. `G9B` stays not authorized. No
phase is marked authorized, approved or `PASS`.

## 12. Product effect

The candidate changes exactly five paths:

```text
A  .github/prompts/tasks/pre-g9b-r6-plus-p0-integrated-characterization-and-design.prompt.md
A  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md
M  docs/roadmap/geocedg_roadmap.md
A  docs/validation/pre_g9b_r6_plus_planning_candidate_report.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-planning.json
```

No path under `source/`, `apps/`, `packaging/`, `geocedg/specs/`,
`geocedg/features/`, `geocedg/resources/`, `models/`, `tools/` or
`artifacts/` changed. No product code, test, build, serialization, profile,
feature manifest, registry, schema, verifier, guide, ADR or specification
changed.

```text
PRODUCT_PHASE_EFFECT               = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
GUIDE_IMPACT                       = NONE
selfApproved                       = false
```

## 13. State when this candidate was frozen

These are the states at the moment this planning candidate was frozen. The
author's later review and closeout record is the sole authority for current
status; this frozen report is never amended to follow it.

```text
TECHNICAL_CANDIDATE_STATE  = FROZEN
AUTHOR_DECISION            = NOT_RECORDED_IN_THIS_ARTIFACT

PRE-G9B-R6                 = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus            = AUTHOR-DEFINED MINI-TRACK
                             PLANNING / DESIGN AUTHORIZED
                             PRODUCT IMPLEMENTATION NOT AUTHORIZED
                             (this planning candidate pending author review)
PRE-G9B-R6-plus-P0         = DESIGNED / CANONICAL PROMPT CREATED / NOT AUTHORIZED
PRE-G9B-R6-plus-A … G      = NOT AUTHORIZED
PRE-G9B-R7                 = DESIGNED — NOT AUTHORIZED
                             BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                        = NOT AUTHORIZED
PRODUCT IMPLEMENTATION     = NONE
publication                = NONE (local commit only; no push, merge, tag or promotion)
```

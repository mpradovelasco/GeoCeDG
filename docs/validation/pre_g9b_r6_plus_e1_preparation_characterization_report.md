# PRE-G9B-R6-plus-E1 preparation characterization report

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (E1 canonical prompt)
TECHNICAL_CANDIDATE_STATE = PREPARED in the working tree of the preparation branch
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

SUBPHASE                  = PRE-G9B-R6-plus-E1
PROPOSED DECOMPOSITION    = E1-L / E1-P (not author-approved; DQ-E1-1)
PROMPT STATE              = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = false
passClaimed               = false
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
```

This report preserves the evidence behind the characterization encoded in the
canonical
[`E1` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-ggt-library-and-packaging.prompt.md)
(sections `K1`–`K12` and `DQ-E1-1` to `DQ-E1-16`). The prompt holds the
resulting contract; this report does not restate it and carries no authority of
its own. Its machine-readable mirror is
[`pre-g9b-r6-plus-e1-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-preparation-characterization.json).
Evidence labels: `PROVEN FROM SOURCE`, `PROVEN BY PROBE`, `INFERRED`, `UNKNOWN`.
No legal conclusion is made or implied anywhere in this report.

## 0. Preparation inputs

The author's instruction of 2026-10-06 is the authority of this preparation. It
is recorded here, instead of in a separate author-decision record, because it
fixes no new product decision; every open question is a `DQ-E1` row of the
prompt.

- Start condition: `PRE-G9B-R6-plus-C-X1` closed and published (verified, §1).
- Baseline: the then-current published `main`, resolved at task start.
- Authorized: characterization, design reconciliation, author-decision
  preparation, and the canonical `E1` execution prompt in
  `PREPARED — NOT AUTHORIZED`. Not authorized: product implementation, `E2`,
  `E3`, `F1`, `F2`, `F3` implementation, `G`, `PRE-G9B-R7`, `G9B`, native
  dimensions, `IsoABorder`, unrelated retained debt.
- Starting design evidence, not authority: the approved mini-track plan (§2,
  §3.2, §8, §13) and the P0 characterization and E1 design candidate. P0
  recommendations are not promoted to author decisions.
- Preserved author requirement: the future curated sheet definitions hide the
  outer dotted rectangle and all macro-created corner points (`AQ-G3`); the
  immutable originals are never modified.
- Questions the instruction requires: `AQ-G3a`, `AQ-G4`, `AQ-G7`, the shipped
  subset, the packaging route, the `E1-L`/`E1-P` split and its classes. They are
  `DQ-E1-8`, `DQ-E1-13`, `DQ-E1-9`, `DQ-E1-6`/`DQ-E1-7`, `DQ-E1-12` and
  `DQ-E1-1` to `DQ-E1-3`.

## 1. Entry gate and identities

Verified after a fresh `git fetch origin`, before any edit:

```text
local main       = 23b971f5a967958f93e745ca8c39fce1162706e6
origin/main      = 23b971f5a967958f93e745ca8c39fce1162706e6
live remote main = 23b971f5a967958f93e745ca8c39fce1162706e6   (git ls-remote)
tree             = 5f564c8391828dc781dee0b1be93ef223431af58
tracked worktree = clean
name             = P_R6PLUS_C_X1 (the C-X1 closeout commit, published)
```

- The roadmap records `PRE-G9B-R6-plus-C-X1` as `PASS — AUTHOR APPROVED —
  PUBLISHED` and `E1` as `NOT AUTHORIZED`, with no implementation prompt.
- Preparation branch `phase/pre-g9b-r6-plus-e1-prompt`, created at the base.
  Local only; never pushed.
- No `E1` artifact existed: the only `e1` paths were the P0 design candidate
  and the plan/roadmap rows.

## 2. Method

- Static reading of every cited seam at the base, with line numbers re-read.
- Scratch-only probes (session scratchpad, never the product tree), PowerShell
  7.6.6 on .NET 10.0.12: each author `.ggt` and `Templatev7.ggb` copied to the
  scratchpad and only the copy unpacked; macros parsed as XML and compared
  element by element.

  | Artifact | SHA-256 |
  |---|---|
  | `ggt_probe.ps1` (inventory and equivalence) | `ad4d49a3042d86e988c7eb3cd91392092efec4e7305912deba5a4530a4b7b6b3` |
  | `ggt_probe_result.json` | `b8f6ce4006b8d07af66aa74c85e83e3700b6a30da4ae225aea9e21c8c4d0887f` |
  | `ggt_style_probe.ps1` (outputs, visibility, style) | `87ee83297110b4ef8b2cd57f57d29f8a4a887bf70a200dadfc3e386eb859c347` |
  | `ggt_style_probe.json` | `2e44ec3bded3ef5b59f82fd2c75c2ca4dc219779758cf93a365aa36f56fdcf67` |

- Two probe defects were found and corrected before any result was used: a
  helper named `H` was shadowed by the PowerShell alias `h`, and a first output
  classification used case-insensitive label matching (`B` matched output `b`);
  the reported results use case-sensitive matching.
- Not run: `PHASE`, `INTEGRATION`, `FINAL`, `PACKAGING`, any build or product
  test. No product, test, build, registry, verifier or packaging file changed.

## K1. Inventory of the supplied tools (`PROVEN BY PROBE`)

`artifacts/author-input/g9b-r6-plus/` is ignored (`/artifacts/*`). The 17 files
(16 `.ggt` plus `ReportExport.md`) have the names, sizes and SHA-256 values of
the P0 inventory (P0 report §4): **17 of 17 identical**, so neither the author
nor an agent changed them since P0.

| File | Command | Writer | Entry date | Icon in archive | Raw construction equal to template |
|---|---|---|---|---|---|
| `axisdimension.ggt` | `axisDimension` | 5.2.826.0 | 2024-02-23 | `GeoGebra_button_distance.png` | no |
| `circarcbyangle.ggt` | `circArcbyAngle` | 5.2.827.0 | 2024-03-06 | `GeoGebra_button_circle_sector_3.png` | yes |
| `circlebyD.ggt` | `CirclebyD` | 5.2.820.0 | 2024-02-05 | `circulodiametro.png` | yes |
| `directdimension.ggt` | `directDimension` | 5.2.820.0 | 2024-01-29 | `GeoGebra_button_distance.png` | no |
| `ductsymbol.ggt` | `DuctSymbol` | 5.2.827.0 | 2024-02-28 | `GeoGebra_button_copy_visual_style.png` | no |
| `ellipseaxisbyconjaxis.ggt` | `conj2mainAxesEllipse` | 5.2.829.0 | 2024-03-11 | `GeoGebra_button_ellipse_3.png` | yes |
| `ellipsebyaxis.ggt` | `EllipseAxis` | 5.2.820.0 | 2024-02-05 | none (template: `Ellipse.png`) | yes |
| `ellipsepartiallength.ggt` | `ellipseLength12` | 5.2.832.0 | 2024-04-02 | `GeoGebra_button_ellipse_3.png` | yes |
| `IFpositivethenPoint.ggt` | `IFPositiveSelectPoint` | 5.2.836.0 | 2024-04-22 | `GeoGebra_button_relation.png` | yes |
| `pointjump.ggt` | `pointJump` | 5.2.820.0 | 2024-02-04 | none (template: `TranspLength.PNG`) | yes |
| `relcoor.ggt` | `relCoor` | 5.2.826.0 | 2024-02-26 | `GeoGebra_button_translate_by_vector.png` | yes |
| `sheetISOland.ggt` | `sheetISOAnLand` | 5.2.820.0 | 2024-01-24 | `GeoGebra_icon_perspective1.png` | no |
| `sheetISOvert.ggt` | `sheetISOAnVert` | 5.2.820.0 | 2024-01-24 | `GeoGebra_icon_perspective1.png` | no |
| `squarebydiagonal.ggt` | `SquarebyDiagonal` | 5.2.820.0 | 2024-02-05 | none (template: `Square.png`) | no |
| `symmsymbol.ggt` | `SymmSymbol` | 5.2.827.0 | 2024-02-28 | `GeoGebra_button_copy_visual_style.png` | no |
| `translationcoor.ggt` | `translationCoor` | 5.2.829.0 | 2024-03-05 | `GeoGebra_button_translate_by_vector.png` | yes |

Every archive holds exactly one `geogebra_macro.xml` (root `format="5.0"
app="classic" platform="d"`, LF) with one `<macro>`, `copyCaptions="true"`,
`showInToolBar="true"`; no JavaScript or defaults entry. Only the two sheet
tools carry a script element: `<ggbscript onUpdate="&#xD;&#xA;"/>`.

Provenance facts, kept separate from rights (§K7): the template manifest names
the author Manuel Prado-Velasco as source author
(`models/legacy/template-v7/manifest.yml:9`), with no date (`:10`); the 2024
archives add writer versions and entry dates only. Nothing here establishes
redistribution permission.

## K2. Equivalence with `models/legacy/template-v7` (`PROVEN BY PROBE`)

- `Templatev7.ggb` SHA-256 `f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`
  (unchanged since G3), 24 macros. All 16 supplied command names are among
  them; the 8 others (`SplineLength`, `PoliLineVisibility`, `Perimeter`,
  `listLength`, `listLength12`, `postLocus`, `ellipseVisibility`,
  `dummyRotate`) are out of `E1` scope.
- Ordered inputs and outputs equal: **16/16**. Constructions equal once
  `<eqnStyle>` tags are ignored: **16/16**. Raw equal: **9/16** (the "yes" rows
  above). The template, written by a later 5.2 build, carries the
  `<eqnStyle>` tags (e.g. 45 in `sheetISOAnLand`, 54 in `sheetISOAnVert`).
- Icons: the 13 archive icons are byte-identical to the template's entries of
  the same name. The 8 distinct images (all 32 × 32; 8-bit indexed or 32-bit
  ARGB) are **not** byte-identical to any PNG tracked under `source/`, so their
  pixel provenance is `UNKNOWN`; 12 of the 13 references use GeoGebra stock
  names. They are treated as GeoGebra-derived UI images and excluded.
- The template archive also contains `geogebra_javascript.js`,
  `geogebra_defaults2d.xml`, `geogebra_defaults3d.xml`, a thumbnail and 16 image
  entries; none belongs to a curated definition.

Geometric equivalence says what the tools construct. It says nothing about
redistribution.

## K3. Manifests, controlled integration and repository structure (`PROVEN FROM SOURCE`)

- `models/` contains `README.md`, `legacy/`, `manifests/`, `regression/`; no
  `curated/`.
- `models/legacy/template-v7/manifest.yml`: `maturity: legacy` (`:6`), source
  author (`:9`), `loaded_by_default: false` (`:17`), `license.spdx: null`,
  evidence "embedded GeoGebra UI images and code; no complete redistribution
  clearance", `review_status: "blocked"` (`:34-38`). `curation.yml` holds the
  24 tools with `toolbar_interpretation: authoritative-legacy-reference`;
  `derived/` holds only `tool-inventory.yml`.
- Controlled integration (`geocedg/specs/legacy/controlled-integration.md`):
  immutable `original/`, `manifest.yml`, `curation.yml`, `derived/` (`:20-31`);
  `tools/legacy/ingest.ps1` (`:36-46`); generated inventory never hand-edited
  (`:48-49`); maturity path `legacy -> research -> experimental -> stable`
  (`:14-16`); six promotion steps (`:65-73`).
- `tools/agent/verify-legacy.ps1`: Pin A asserts exactly three catalog
  manifests with `maturity == legacy` (`:205-215`); Pin B asserts that every
  `.ggb/.ggt/.js/.ggs` under `models/legacy` is registered, lies inside
  `original/`, and that there are exactly 3 (`:470-484`); it also pins the
  template hash and the 24 macro names. It is not referenced by
  `verify.ps1`, the registry or `PhaseVerifiers` (`INFERRED`: standalone).
- `AGENTS.md:73` lists canonical, research, legacy and regression model
  categories; `:67` assigns icons, styles, translations and branding to
  `geocedg/resources/`; `:142-152` requires legacy import first.

Structure options evaluated for `DQ-E1-4`:

| Option | Assessment |
|---|---|
| `tools/ggtfiles/` (author's original request) | rejected: `tools/` holds tooling; product content and provenance would mix |
| `models/legacy/template-v7/derived/ggt-library/` | rejected: Pin B admits `.ggt` under `models/legacy` only inside `original/` and counts exactly 3; `derived/` is generated inventory |
| new legacy package of the 2024 archives | rejected as the library location; as provenance it changes Pins A and B and duplicates bytes already present in the template (`DQ-E1-5`) |
| `geocedg/resources/ggt-library/` | rejected: puts construction content in the resource category |
| `models/canonical/...` | rejected: would rank convenience tools with canonical CeDG models (`AGENTS.md` §2 item 3) |
| `models/curated/ggt-library/` + one-line §3.2 amendment | recommended; not registered in `models/manifests/catalog.yml`, so Pins A and B are untouched (`INFERRED` from the code; to be re-run at implementation) |

The `AGENTS.md` amendment is a governance-layer change. This preparation does
not make it; the prompt requires that the `E1-L` authorizing instruction name it
explicitly.

## K4. Current user-tool library behavior (`PROVEN FROM SOURCE`)

Paths below are under `source/desktop/desktop/src/main/java/org/geocedg/desktop/`.

- Install only through the manager's file chooser (`GeoCeDGUserTools.java:488-498`).
  Store: `<preferences>.user-tools-v1.json` next to the preferences file
  (`:96-104`), internal `STORE_VERSION = 3`, `DEFINITION_DIGEST_VERSION = 1`
  (`GeoCeDGUserToolLibrary.java:74-75`); the reader accepts versions 1, 2 and 3
  and checks exact root key counts (`:1115-1122`).
- Identity: package id = SHA-256 of the whole `.ggt` bytes (`:109`); per-command
  definition digest over `Macro.getXML` with only `showInToolBar` normalized
  (`:931-946`), so `iconFile` and every style attribute are definition-bearing.
- Validation: blank-only `ggbscript`/`javascript` accepted, non-blank rejected
  (`:828-837`); archive images (png/jpg/jpeg/gif/svg) accepted and never
  loaded, other entries rejected (`:1064-1071`); command names matching a native
  command rejected (`:785-795`) through `Commands.lookupInternal`, which compares
  with `equalsIgnoreCase` (`common/kernel/commands/Commands.java:1268-1276`).
- Conflicts: install rejects a case-insensitive command collision with another
  installed package (`:325-332`); a same-name document macro does not block
  install. Activation adopts an equal-digest document macro and refuses a
  mismatching or partial one (`DefinitionMismatch`, `DocumentConflict`); nothing
  is renamed.
- Activation registers the macro into the live kernel only on explicit user
  invocation; nothing is registered at install or startup. The inherited
  startup macro snapshot is neutralized (`AppGeoCeDG.java:1413-1436`).
- No bundled catalog and no first-run seed exist; pins use a monogram or a
  user-chosen PNG stored in the preferences store, never the package icon.
- Existing tests: `G9U1UserToolLibraryTest`, `G9U1MacroNativeArchivePersistenceTest`;
  no dedicated test of older store versions as a regression family.

All 16 author archives install under these rules (P0 probe P4; not re-run here,
the rules above are unchanged).

## K5. Resource loading route and installer layout

- `PROVEN FROM SOURCE`: Desktop loads resources from the classpath only
  (`Class.getResource`); no GeoCeDG code reads `jpackage.app-path`, the code
  source or the installation directory. `geocedg/resources/` is not copied into
  any JAR; `processResources` copies only the profile, its schemas and the
  guides (`source/desktop/desktop/build.gradle.kts:205-223`).
- `PROVEN FROM SOURCE`: the package input is only the `installDist` `lib/`
  JARs, the profile notice and `legal/`
  (`tools/release/build-windows-package.ps1:538-572`), then
  `jpackage --type app-image --input <input>` (`:576-597`). Layout:
  `GeoCeDG/GeoCeDG.exe`, `app/*.jar`, `app/<notice>`, `app/legal/**`, `app/*.cfg`,
  `runtime/**`.
- `INFERRED`: a directory staged as `input/ggt-library/` lands at
  `app/ggt-library/`; the jpackage launcher sets `jpackage.app-path`, from which
  the `app/` directory is derivable. Both are to be re-established at
  implementation (stop condition in the prompt).
- Consequence for the split: a classpath route would put the definitions into
  `desktop.jar`, hence into every package, before the rights gate. The
  installation-directory route lets `E1-L` build and test the catalog against an
  injected directory while nothing ships until `E1-P` (`INV-E1-7`).

## K6. Icon ownership and stock references (`PROVEN FROM SOURCE` / `PROVEN BY PROBE`)

- Five owned tool icons exist, all SVG, under
  `source/shared/common/src/main/resources/org/geogebra/common/icons/svg/web/toolIcons/`,
  registered in `geocedg/resources/assets-manifest.yml` (`:76-136`) with
  `canonical_lf_sha256`, provenance, `geocedg-documentation-or-ordinary-art` and
  "author-approved CC-BY-4.0 ordinary artwork". No PNG tool-icon entry exists.
- Desktop draws owned SVGs with the in-tree `JSVGIcon` (no external SVG
  library) in `GeoCeDGToolImageResource.renderImage` (`:123-150`).
- `GeoCeDGToolImageResource` reuses three upstream stock images for profile
  actions (`:31-39`); no user tool uses a stock image.
- The only raster generator is `tools/resources/generate-geocedg-branding.ps1`:
  System.Drawing rescaling of pinned PNG sources, `-VerifyOnly` byte compare,
  reproduction claimed on the recorded runtime only
  (`assets-manifest.yml:253`); it cannot rasterize SVG. No `tools/agent` check
  runs it.
- The supplied tools' icons: §K2 (stock names, unknown pixel provenance).

Design consequence: an SVG → PNG rasterizer must reuse a renderer the
repository already has; the in-tree Java renderer is the only one. Running it
inside a JUnit golden test keeps the build scripts and `tools/agent` unchanged
(`DQ-E1-2`, `DQ-E1-11`).

## K7. Packaging prohibition and licensing records (`PROVEN FROM SOURCE`)

- Contract: `geocedg/specs/packaging/windows-packaging.md:83-85` — the pipeline
  fails if the result contains PDFs, `.ggb`/`.ggt` models, `Templatev7.ggb`,
  repository documentation or non-Windows natives; `:15` the only application
  input is `installDist` `lib/`. Status `:3-5`: stable G4 infrastructure,
  internal evaluation, public redistribution blocked.
- Enforcement: `tools/agent/checks/packaging-product.ps1:562-567`
  (`packaging.portable-boundary`): every file of the app-image with extension
  `.pdf`, `.ggb` or `.ggt` (case-insensitive) or named `Templatev7.ggb` is a
  violation; JAR contents are not inspected; the contract runs only with
  `-RequireArtifacts`. Fixtures in
  `tools/agent/tests/verification-contract-boundary.Tests.ps1:374-402`.
- Other affected subcontracts (`INFERRED` impact): `packaging.sbom`
  (`:605-642`, counts JARs, fonts and runtime only), `packaging.build-manifest`
  (`:657-684`, fixed counts), `packaging.artifact-hashes` (`:702-714`, would hash
  new files automatically), `packaging.required-files` (`:281-305`),
  `packaging.asset-authority` (`:380-396`, status string and exclusions count).
- Records: `assets-manifest.yml:489-495` deliberately excludes "Templatev7.ggb
  and all models/legacy resources"; `NOTICE.md:26-28` says no legacy/research
  model is an intentional package input; `docs/licensing/component-matrix.md:80`
  "exclude from release packaging until human review"; `:21` and
  `THIRD_PARTY.md:16` class GeoGebra UI images under CC BY-NC-SA 4.0-or-later
  terms.
- Tracked main resources contain one `.ggb` (`source/web/web/.../ggmexample.ggb`,
  web module) and no `.ggt`; a JAR-entry scan in `E1-P` would need an inventory
  of the Desktop JARs first (`DQ-E1-12`).

Candidate elements of the `AQ-G4` record, for the author (`DQ-E1-13`); none is
a conclusion:

1. per tool: command, source package and macro, source SHA-256, curated SHA-256
   and definition digest;
2. the author's authorship statement for each macro construction and its dates;
3. the exclusion record: no GeoGebra image, no template JavaScript, no template
   document content beyond the extracted `<macro>`;
4. the licence the author chooses for the curated definitions, and separately
   for the owned icons;
5. open questions for human or professional review, e.g. whether parts of the
   macro XML written by GeoGebra software are third-party material, and whether
   tool names or help texts raise any trademark question;
6. the distribution profiles covered (`NC`, `INTERNAL`; `COMMERCIAL` stays
   pending external terms);
7. the record location and its JSON mirror, and the licensing records it
   updates (`component-matrix.md`, `THIRD_PARTY.md`, `NOTICE.md`, `LICENSES/`,
   `assets-manifest.yml`);
8. the exact author decision label, which the packaging check consumes.

## K8. Verification decomposition (`PROVEN FROM SOURCE`, then analysis)

Facts:

- `verification-levels.md` §12.8 (`:1216-1251`): `BOUNDED_PHASE` → PHASE;
  `INTEGRATED_PHASE` → PHASE (+ COMPOSED when the plan names concrete
  coverage); `GLOBAL_IMPACT` → FULL; `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`
  → focused operational evidence, plus FULL only when global verification
  infrastructure changes; `DOCUMENTATION_STATUS_ONLY` → static. §8 (`:425-432`)
  lists the verification-infrastructure change kinds; FULL is required for
  build/toolchain or verifier/bootstrap changes (`:151-153`) unless §12.8 gives
  less.
- Registry: `packaging.product` (`SEMANTIC`, `PRODUCT`, tier `INTEGRATION`,
  required for `PACKAGING`, `INTEGRATION`, `FINAL`; `:183-198`);
  `infra.contract-boundary` (`VERIFICATION_CORE`, `INFRA_UNIT` + `FINAL`;
  `:491-505`); profiles `PACKAGING` `COMPLETE`, `OPERATIONAL` `INCOMPLETE`
  (`operational.remaining-pure-coverage` missing), `PHASE` `COMPLETE` (`:9-19`).
  `PRE-G9B-P1` registered `packaging.product` in its phase selection (`:65`).
- Precedents: R6 added a PROCESS product leaf and its checker script under
  `tools/agent/checks/` and froze `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`
  with `PHASE_LOCAL` (`.github/prompts/tasks/pre-g9b-r6-ggbscript-compatibility.prompt.md:42-58`);
  `C-X1` registered a Desktop-only selection and stayed `BOUNDED_PHASE`,
  updating the registry-shape pins in
  `tools/agent/tests/verification-final-coverage.Tests.ps1` as ordinary
  phase-local work; P1 (packaging builder and check changes, before §12.8)
  ran `PHASE`, `PACKAGING` for two artifact sets and `FINAL`.
- `PACKAGING` is missing from the canonical profile list of
  `verification-levels.md` (`:23-24`) although the registry declares it; the
  packaging-smoke rule is in `AGENTS.md:554`, and "packaging artifact checks
  remain separate applicable gates" (`verification-levels.md:257-261`).

Analysis:

- `E1-L` changes Desktop application code, GeoCeDG-owned content, a profile
  text block and adds JUnit tests. Kept free of `tools/agent/checks/`, build
  scripts, the shared kernel, the document format and the action catalog, it
  matches `BOUNDED_PHASE` (`DQ-E1-2`). Including `workspace-profile.product` in
  its selection follows `A-1`/`B`/`C`; it does not change the class.
- `E1-P` changes a product contract (`windows-packaging.md`), the builder, a
  `PRODUCT` acceptance leaf and a `VERIFICATION_CORE` test. P0's
  `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` remains defensible because a
  verification-core test changes, but "focused operational evidence" cannot be
  the `OPERATIONAL` profile, which is incomplete; it has to be named: `PHASE`
  with `packaging.product`, `INFRA_UNIT`, `PACKAGING` toolchain and artifact runs,
  and the packaging smoke (`DQ-E1-3`). `GLOBAL_IMPACT` would follow the P1
  precedent at FULL cost; `INTEGRATED_PHASE` would add `INTEGRATION`, whose only
  packaging content is the same `packaging.product` leaf without artifacts.
- The split is re-justified by the rights gate and the different acceptance
  surfaces, not only by P0's tooling argument (`DQ-E1-1`).

## K9. Feature-manifest impact (`PROVEN FROM SOURCE`)

- `geocedg/features/stable.yml`: `cedg.frontend.profile`,
  `cedg.frontend.classic-diagnostic`, `cedg.legacy.ingest`,
  `cedg.packaging.windows`. `experimental.yml`: `cedg.export.dxf.2d`,
  `cedg.export.dxf.extended`, `cedg.laboratory.legacy`, `cedg.locus.v2`,
  `cedg.spatial.semantics`; several experimental entries are
  `enabled_by_default: true`.
- An entry needs an id, maturity, specification path, default state and
  dependencies (`geocedg/specs/operations/manifest-contracts.md:21-24`); adding
  one does not authorize implementation, and experimental entries must not
  become default merely by appearing in a toolbar or package
  (`geocedg/features/README.md:7-10`).
- No feature id covers user tools or a library; the user library lives only in
  the profile (`automation.manage-user-tools`, group `automation-user-tools`,
  menu kind `user-tools`, `UserTools.*` texts).
- Consequence: `E1-L` needs a feature entry and a specification; promotion to
  `stable` needs the six controlled-integration steps, the first of which is
  the rights step (`DQ-E1-14`).

## K10. Style mechanics (`PROVEN FROM SOURCE`)

- `AlgoMacro.createOutputObjects` sets `setUseVisualDefaults(false)` and copies
  the definition output's visual style and layer (`AlgoMacro.java:298-305`).
- With `copyCaptions`, outputs hidden in the definition are created hidden
  (`:118-128`).
- `compute` re-applies `setAdvancedVisualStyleCopy` (`:268-270`), which copies
  only a color function and a show-object condition (`GeoElement.java:1376-1392`);
  thickness, color and line type are not re-copied, so a post-creation restyle
  hook would survive recompute (correction of a possible reading of P0 §6:
  sustainability fails on role semantics, not on recompute).
- Strokes are drawn at `lineThickness / 2.0` (`common/euclidian/Drawable.java:626`);
  thickness is device-pixel relative, so no fixed value equals 3 typographic
  points on every surface (`DQ-E1-9`).
- Linear **outputs** of the supplied tools (`PROVEN BY PROBE`): thickness 2
  (both dimension tools, `EllipseAxis`, `SquarebyDiagonal`, `circArcbyAngle`),
  3 (sheet inner border and title block, `DuctSymbol`, `SymmSymbol`,
  `CirclebyD`, `ellipseLength12`), dotted type 20 at thickness 2 (sheet trim);
  output colors include black, grays and light blue (dimension vectors). The
  outputs of `conj2mainAxesEllipse`, `IFPositiveSelectPoint`, `pointJump` and
  `translationCoor` are points only, and `relCoor` returns a number; the
  thickness-5 and dashed (type 15) elements of the Rytz construction are
  non-output helpers.

## K11. Sheet tools in detail (`PROVEN BY PROBE`)

- Inputs `D, ISO216n, CeDGScale, CeDGMargin`; 21 (landscape) and 22 (portrait)
  outputs.
- Landscape: `E = (x(D)+L, y(D))`, `G = (x(D)+L, y(D)-l)`, `F = (x(D), y(D)-l)`;
  trim `f = DE`, `g = EG`, `h = GF`, `i = FD` (dotted, thickness 2, black);
  corners `E`, `G`, `F` visible (dark gray).
- Portrait: the landscape figure rotated −90° about `D` and translated by
  `v = Vector(F', D)` = `(l, 0)`. Hence `F'' = D` (upper-left), `D''` upper-right,
  `E''` lower-right, `G''` lower-left. Trim `f''` (right), `i'' = Segment(D, D'')`
  (top), `g''` (bottom), `h'' = Segment(G'', D)` (left) and the untranslated
  `f' = Segment(D, E')` = the same left edge, so `h''` duplicates `f'`. Corner
  outputs `F_2 = D''` (blue, the input color), `G_2 = E''`, `H_2 = G''`.
- `D` is the upper-left corner and the user's input in both tools; a macro
  cannot hide its own input (`DQ-E1-8`).
- Sizes: `CeDGlM` and `CeDGlm` by `Iteration` from `100·2^(1/4)` and
  `100/2^(1/4)`, i.e. A0 = 118.92 × 84.09 **model units** read as centimetres,
  divided by `CeDGScale`; title-block constants scale by `CeDGlM/42.04` and
  `CeDGlm/29.73` (A3 in cm). Theoretical √2 sizes differ from ISO 216 by at
  most 0.65 mm for A0–A6 (A3: 420.45 × 297.30 mm versus 420 × 297).
- Under the `D1` unit system the macro is unit-unaware: in a document whose
  construction unit is `mm` the sheet comes out ten times too small; unit
  metadata is never written inside a macro construction
  (`geocedg/specs/units/unit-system.md:473`, `:503`).

## K12. Serialization when a curated macro is used (`PROVEN FROM SOURCE`)

- Saving writes every kernel macro and then `geogebra_macro.xml`
  (`common-jre/io/MyXMLioJre.java:350-363`), including an activated tool that
  was never used (`G9U1MacroNativeArchivePersistenceTest.java:124-131`).
- Macro images are written only if present in the image manager
  (`MyXMLioJre.java:586-605`); library images are never loaded, so documents
  carry the `iconFile` string but no icon bytes.
- Loading a `.ggb`/`.cedg` removes all kernel macros first (`:194-197`) and
  binds the document's macros as command authority; equal digests adopt,
  different digests produce `DefinitionMismatch` with the document winning.
- Consequence: no document-format change; a curated tool behaves exactly as a
  user-library tool in documents. Because every curated transformation changes
  the definition digest, documents built from the original tools or the
  template never adopt the curated definition silently.

## Cross-phase observation (recorded, not `E1` scope)

`OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` (proposed id; owner `E2`):
the `E2` design candidate names native commands `DirectDimension` and
`AxisDimension`; `Commands.lookupInternal` compares names case-insensitively,
so once those commands exist the user library rejects the legacy macros
`directDimension`/`axisDimension` at install and when it re-validates stored
packages. How documents that contain those macros resolve the name is not
characterized here. `E2` must characterize and dispose of it; `E1` neither
curates nor ships the two dimension tools (`DQ-E1-6`).

## R. Reconciliation with P0

| P0 statement | Status at `P_R6PLUS_C_X1` |
|---|---|
| 16 tools construction-equivalent to `template-v7`; 9 raw-equal; 3 lack icon references; 13 icons equal to the template's | confirmed (§K1, §K2) |
| 12 of 13 icons use stock names | confirmed; added: none is byte-identical to a tracked `source/` PNG, pixel provenance unknown |
| `AQ-G3` output map | confirmed, with the portrait geometry decoded (§K11) |
| sheet sizes use the unrounded series | confirmed; added: centimetre model unit, unit-unaware under `D1` |
| `verify-legacy.ps1` pins (`:210-215`, `:470-483`) | confirmed (`:205-215`, `:470-484`); added: the script is standalone, not a registry node |
| packaging exclusion `:83-85`, `portable-boundary` `:562-567`, JARs not inspected | confirmed; added: `infra.contract-boundary` fixtures and the other affected subcontracts |
| user library: script rule, icon ignored, digest includes `iconFile`, no catalog/seed | confirmed (§K4); added: native-name rule is case-insensitive (cross-phase observation) |
| remembered style not sustainable | confirmed on role grounds; corrected: a restyle would survive recompute (§K10) |
| `E1-P` = `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` + packaging smoke | not inherited: re-derived with the `OPERATIONAL`-incomplete and `PRODUCT`-leaf facts; options in `DQ-E1-3` |
| bundled read-only catalog over first-run seed | confirmed with current store facts; added: installation-relative location, sidecar pins, precedence (`DQ-E1-10`) |
| `models/curated/ggt-library/` + `AGENTS.md` amendment; `tools/ggtfiles/` rejected | confirmed; more alternatives evaluated (§K3) |
| deterministic rasterizer on the branding precedent | corrected: the branding script cannot render SVG; the in-tree Java renderer is the only available one (§K6) |
| dimension and sheet tools retired after `E2`/`E3` | refined: dimension tools not curated at all; sheet tools curated, shipping pending `DQ-E1-7` |

## 3. Preparation files

| Path | Change |
|---|---|
| `.github/prompts/tasks/pre-g9b-r6-plus-e1-ggt-library-and-packaging.prompt.md` | new canonical prompt, `PREPARED — NOT AUTHORIZED` |
| `docs/validation/pre_g9b_r6_plus_e1_preparation_characterization_report.md` | this report |
| `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-preparation-characterization.json` | machine-readable mirror |
| `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` | preparation bullet, `E1` status line, proposed observation |
| `docs/roadmap/geocedg_roadmap.md` | documental version, `E1` row |

The prompt was validated with `Test-PromptContractDocument` against the `task`
profile and is not added to `prompt-contracts.json`, following the convention
for phase prompts. The `STATIC` run and `git diff --check` are reported outside
this artifact.

```text
PRE-G9B-R6-plus-E1 =
PREPARED — NOT AUTHORIZED
PENDING AUTHOR REVIEW
```

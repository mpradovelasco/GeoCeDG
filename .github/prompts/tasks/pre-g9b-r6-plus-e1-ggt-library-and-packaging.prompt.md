# PRE-G9B-R6-plus-E1 — curated GGT library, owned tool icons and packaging support

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-06, given after
`PRE-G9B-R6-plus-C-X1` was closed and published. That instruction authorized
the characterization, design reconciliation, author-decision preparation and
creation of this canonical execution prompt for `E1` (GGT library, curated
assets, GeoCeDG-owned tool icons, library integration, packaging support, and
the provenance and redistribution gate). It stated that it **does not
authorize product implementation**, that the P0 recommendations are starting
evidence and not author decisions, and that the split of `E1` and its
verification classes are not frozen without author approval. The evidence
behind the characterization below is in the
[E1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_e1_preparation_characterization_report.md),
sections `K1`–`K12`. The existence of this file is not authorization.

Execution requires a new explicit author instruction that names the subphase to
start, the exact prepared candidate or base, and that disposes of the requested
decisions `DQ-E1-1` to `DQ-E1-16`. That instruction may authorize, as the first
tracked edit of the phase, an amendment of this prompt to the authorized state,
following the `A-1`, `B`, `D0`, `D1`, `A-2`, `C` and `C-X1` precedent. This file
is an execution contract, not a second policy document: the verification
classes are defined once in `geocedg/specs/operations/verification-levels.md`
§12.8, the legacy promotion path once in
`geocedg/specs/legacy/controlled-integration.md`, the packaging contract once in
`geocedg/specs/packaging/windows-packaging.md`, and the probe evidence once in
the characterization report; this prompt cites them and does not restate them
differently. Where it fixes a design value that no durable specification holds
yet, that value is a proposal until the author disposes of the matching `DQ`,
and the first deliverable of `E1-L` turns it into the durable specification
named under *Required design/specification*.

```text
PRE-G9B-R6-plus-E1 =
PREPARED — NOT AUTHORIZED

proposed decomposition   = E1-L (library / curation / icons / bundled catalog)
                           E1-P (packaging / verification / distribution inclusion)
                           PROPOSED — NOT AUTHOR APPROVED (DQ-E1-1)
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = GEOCEDG APPLICATION LAYER + CURATED MODEL CONTENT +
                           OWNED ASSETS (E1-L); PACKAGING / RELEASE LAYER (E1-P);
                           NO KERNEL, NO GEOMETRY, NO DOCUMENT-FORMAT CHANGE
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED — PUBLISHED
                           (hard); C = PASS — AUTHOR APPROVED — PUBLISHED
                           (recommended predecessor, satisfied)
GLOBAL GATES             = rights review of the GGT definitions and of the icons
                           before any distribution (AQ-G4, DQ-E1-13); closeout in G
PRECONDITION             = author dispositions of DQ-E1-1 to DQ-E1-16
                           (E1-P additionally: E1-L PASS — AUTHOR APPROVED and an
                           author-approved rights record, DQ-E1-13)
NEXT_SUBPHASE            = none implied; E2, E3, F1, F2, F3, G stay unauthorized
```

`authorApproved = false` means that no technical candidate of `E1` has been
author-approved. Technical verification never creates author approval. Once
authorized, each authorized subphase stops with one exact technically verified
candidate pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Deliver the author-defined `E1` scope (mini-track plan §2 "GGT library", §3.2
`E1` brief, §8) in two separately authorized subphases, if `DQ-E1-1` approves
the split:

**`E1-L` — curated library, owned icons and bundled catalog.**

1. A curated, GeoCeDG-owned **derived** GGT library generated deterministically
   from the immutable original `models/legacy/template-v7/original/Templatev7.ggb`,
   with an authored curation record, a generated library manifest and
   generated `.ggt` files, under the structure fixed by `DQ-E1-4`.
2. The author-required sheet modifications (`AQ-G3`) on the curated sheet
   definitions only, with the `AQ-G3a` disposition (`DQ-E1-8`).
3. The style policy of `DQ-E1-9` applied to the curated definitions only.
4. GeoCeDG-owned SVG icon sources, a deterministic rasterizer to the 32 × 32
   PNG that the curated `.ggt` carries, manifest registration with reproducible
   hashes and a clear source/generated separation (`DQ-E1-11`).
5. A read-only bundled catalog in the existing user-tool manager and
   `user-tools` menu, fed only from an installation-relative library directory
   (empty when absent), with the precedence, pinning and integrity rules of
   `DQ-E1-10`.
6. The durable specification, ADR and feature-manifest entry of *Required
   design/specification*, and, only if the authorizing instruction explicitly
   authorizes it, the one-line `AGENTS.md` §3.2 governance amendment of
   `DQ-E1-4`.

**`E1-P` — packaging, verification and distribution inclusion.**

1. The amended Windows packaging contract that admits `.ggt` content **only**
   from the canonical curated library, **only** for manifest members marked
   shipped, **only** with the expected SHA-256 and **only** with an
   author-approved rights disposition (`DQ-E1-12`), and stays fail-closed for
   every other `.ggt`, every `.ggb`, and every other forbidden class.
2. The staging of the approved library into the app-image, its SBOM and
   build-manifest records, the amended `packaging.portable-boundary` and a
   library-membership subcontract, with positive and negative fixtures.
3. The licensing records that the author-approved rights record requires
   (`DQ-E1-13`), and the promotion of the library feature decided by
   `DQ-E1-14`.
4. The packaging smoke that `AGENTS.md` §14 requires when packaging changes.

Invariants every candidate must establish and test:

```text
INV-E1-1  The originals are immutable. No byte under models/legacy/** changes;
          Templatev7.ggb keeps SHA-256 f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113;
          the curated library never writes into models/legacy/**.
INV-E1-2  Four things stay distinct and separately recorded for every shipped
          tool: behavioral reference; source/provenance; asset ownership;
          redistribution permission. Geometric equivalence is never evidence
          of redistribution permission.
INV-E1-3  Every curated .ggt is generated, byte-reproducible from its recorded
          inputs on the recorded toolchain, never hand-edited, and listed in the
          generated library manifest with its SHA-256 and its user-library
          definition digest.
INV-E1-4  No curated .ggt contains a GeoGebra stock or template image, JavaScript,
          a non-blank script, or an iconFile that does not name the tool's
          GeoCeDG-owned icon entry inside the same archive.
INV-E1-5  The bundled catalog is read-only: it never writes the user-tool store,
          never rewrites a stored user package, and never activates a tool
          without an explicit user action; a document is changed only by the
          same explicit activation the user library already uses.
INV-E1-6  User data and document authority win: a stored user package with a
          colliding command name keeps precedence over a bundled entry; a
          document macro keeps precedence over both (existing adoption /
          DefinitionMismatch policy unchanged).
INV-E1-7  (E1-L) No build output consumed by packaging (installDist lib/,
          any JAR) contains a curated .ggt; the curated library reaches users
          only through E1-P.
INV-E1-8  (E1-P) The package contains a .ggt if and only if it is a shipped,
          hash-matching member of the canonical curated library with an
          approved rights disposition, under the single admitted app-image
          path; every other .ggt or .ggb anywhere in the app-image fails the
          packaging check.
```

### Authorities and author requirements this prompt implements

- Author scope (mini-track plan §2): a curated library using the local author
  tools as references, with provenance, installer integration and
  GeoCeDG-owned, reproducibly maintained icons for every retained tool;
  packaging support is a required design target; style through line type,
  color and thickness with remembered style if sustainable, otherwise black at
  3 pt.
- Author requirement `AQ-G3` (plan §2, preserved verbatim in substance): in the
  future curated sheet definitions, the outer dotted rectangle is hidden and
  all macro-created corner points are hidden. Implemented only on derived
  definitions; the originals are never modified.
- Governance: `AGENTS.md` §3.2 (`models/` and `tools/` placement), §5 (legacy
  import and maturity), §7 (licensing, owned icons, no legal conclusion), §14
  (license/asset inventory, packaging smoke); the
  [controlled-integration contract](../../../geocedg/specs/legacy/controlled-integration.md)
  (immutable `original/`, promotion steps); the
  [Windows packaging contract](../../../geocedg/specs/packaging/windows-packaging.md)
  (`.ggb`/`.ggt` exclusion).
- Starting design evidence, not authority: the
  [P0 E1 design candidate](../../../docs/architecture/pre_g9b_r6_plus_e1_ggt_library_design_candidate.md)
  and P0 report §6, §12, §13, §15. Every P0 recommendation used here was
  re-established or corrected at the base (report §R).

### Characterization results at the base

Re-established at `P_R6PLUS_C_X1` (report sections in brackets):

- **Inventory [K1].** The 17 local author inputs under the ignored
  `artifacts/author-input/g9b-r6-plus/` are byte-identical to the P0 inventory
  (16 `.ggt` plus `ReportExport.md`). Each `.ggt` holds one macro; writers
  5.2.820.0–5.2.836.0; entry dates 2024-01-24 to 2024-04-22; all carry
  `copyCaptions="true"` and `showInToolBar="true"`.
- **Equivalence [K2].** All 16 command names exist in `Templatev7.ggb` (24
  macros). Ordered inputs and outputs are equal for 16/16; constructions are
  equal for 16/16 once `<eqnStyle>` tags are ignored and raw-equal for 9/16.
  Thirteen archives carry a 32 × 32 PNG byte-identical to the template's; three
  (`EllipseAxis`, `pointJump`, `SquarebyDiagonal`) carry no icon reference.
  Twelve of the thirteen icon names are GeoGebra stock names
  (`GeoGebra_button_*`, `GeoGebra_icon_*`); none of the eight distinct icon
  images is byte-identical to any PNG in the current `source/` tree, so their
  pixel provenance is **not established** — they are treated as GeoGebra-derived
  UI images and never shipped. The template also carries
  `geogebra_javascript.js`; no curated archive carries it.
- **Manifests and governance [K3].** `template-v7/manifest.yml`:
  `maturity: legacy`, `loaded_by_default: false`, `license.spdx: null`,
  `review_status: "blocked"`. `models/` holds `legacy/`, `manifests/`,
  `regression/`; no `curated/` exists. `tools/agent/verify-legacy.ps1` pins three
  legacy manifests (`:205-215`) and exactly three `.ggb/.ggt/.js/.ggs` files
  under `models/legacy`, each inside `original/` (`:470-484`); it is a
  standalone script, not a registry node.
- **User-tool library [K4].** Install only from a user-chosen `.ggt`
  (`GeoCeDGUserTools.java:488-498`); store `<preferences>.user-tools-v1.json`
  (`:96-104`), internal `STORE_VERSION = 3` read with exact key counts
  (`GeoCeDGUserToolLibrary.java:74`, `:1115-1122`); package id = SHA-256 of the
  whole `.ggt` (`:109`); definition digest over `Macro.getXML` with only
  `showInToolBar` normalized, so `iconFile` is definition-bearing (`:931-946`);
  script rule rejects only non-blank `ggbscript`/`javascript` (`:828-837`);
  archive images are accepted but never loaded (`:1064-1071`); command names
  equal to a native command, case-insensitively, are rejected (`:785-795`,
  `:879-885`, `Commands.lookupInternal`). No bundled catalog or first-run seed
  exists; the inherited startup macro snapshot is neutralized
  (`AppGeoCeDG.java:1413-1436`).
- **Resource route [K5].** Desktop resources load only from the classpath; no
  GeoCeDG code reads the installation directory. Packaging input is only
  `installDist` `lib/` plus the notice and `legal/`
  (`tools/release/build-windows-package.ps1:556-597`); every file of `input/`
  lands under `app/`.
- **Icons [K6].** Owned tool icons are hand-authored SVGs registered with a
  canonical LF SHA-256 (`geocedg/resources/assets-manifest.yml:76-87`, five
  `svg-tool-icon` entries); Desktop renders them with the in-tree `JSVGIcon`
  (`GeoCeDGToolImageResource.java:123-150`). The only raster generator is the
  branding script, which rescales PNG sources with System.Drawing and claims
  reproduction on the recorded runtime only; no .NET SVG renderer exists in
  the toolchain.
- **Packaging conflict [K7].** `windows-packaging.md:83-85` makes the pipeline
  fail on `.ggb`/`.ggt`; `packaging-product.ps1:562-567`
  (`packaging.portable-boundary`) rejects those extensions anywhere in the
  app-image, does not open JARs and runs only with `-RequireArtifacts`;
  `infra.contract-boundary` pins it with `.pdf`/`.ggb` fixtures
  (`tools/agent/tests/verification-contract-boundary.Tests.ps1:374-402`).
  `assets-manifest.yml:489-495` excludes "Templatev7.ggb and all models/legacy
  resources"; `NOTICE.md:26-28` and `component-matrix.md:80` say the same.
- **Verification facts [K8].** `packaging.product` is a `SEMANTIC`/`PRODUCT`
  acceptance leaf required by `PACKAGING`, `INTEGRATION` and `FINAL`
  (`verification-registry.json:183-198`); `infra.contract-boundary` is
  `VERIFICATION_CORE` in `INFRA_UNIT` and `FINAL` (`:491-505`); the `PACKAGING`
  profile is `COMPLETE` and the `OPERATIONAL` profile is `INCOMPLETE`
  (`:9-19`). The R6 precedent classed a new PROCESS check script under
  `tools/agent/checks/` as `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`
  (`PHASE_LOCAL`); `C-X1` kept `BOUNDED_PHASE` with JUnit-only checks.
- **Feature manifests [K9].** No feature id covers user tools or a library;
  experimental features may be `enabled_by_default: true` (`cedg.locus.v2`,
  `cedg.export.dxf.*`).
- **Style [K10].** `AlgoMacro.createOutputObjects` copies each output's fixed
  style (`AlgoMacro.java:298-305`); hidden definition outputs are created
  hidden with `copyCaptions` (`:118-128`); `compute` re-copies only the dynamic
  color and show-condition (`:268-270`, `GeoElement.java:1376`), so a
  post-creation restyle would survive recompute. Strokes are drawn at
  `lineThickness / 2.0` (`Drawable.java:626`). Linear outputs today: thickness
  2 (dimensions, utilities), 3 (sheet inner border and title block, symbols,
  `CirclebyD`, `ellipseLength12`), dotted type 20 at thickness 2 (sheet trim);
  four tools return only points or a number.
- **Sheet tools [K11].** Both sheet tools take `D, ISO216n, CeDGScale,
  CeDGMargin`. `D` is the upper-left trim corner in both. Landscape trim
  `f g h i` (dotted, thickness 2), corner outputs `E` (upper-right),
  `G` (lower-right), `F` (lower-left). Portrait trim `f' f'' i'' g'' h''`, with
  `h''` = `Segment(G'', D)` duplicating `f'` = `Segment(D, E')`; corner outputs
  `F_2` (upper-right, blue like the input), `G_2` (lower-right), `H_2`
  (lower-left). The sizes are the theoretical √2 series in **centimetre** model
  units (`100·2^(1/4)` = A0 long side 118.92) divided by `CeDGScale`: unit-
  unaware under the `D1` unit system, and up to 0.65 mm away from the rounded
  ISO 216 sizes for A0–A6.
- **Serialization [K12].** Saving writes **all** kernel macros, including an
  activated but unused tool (`MyXMLioJre.java:350-363`); macro images are
  written only if loaded in the image manager (`:586-605`); loading a document
  clears kernel macros (`:194-197`). No document-format change is needed.
- **Cross-phase observation.** `Commands.lookupInternal` is case-insensitive, so
  the `E2` candidate native names `DirectDimension`/`AxisDimension` collide
  with the legacy macro names `directDimension`/`axisDimension`; once such a
  native command exists, the user library rejects those macros (install and
  re-validation of stored packages). Recorded as the proposed observation
  `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION`, owner `E2`; not
  `E1` scope.

### Design proposed for authorization

Each subsection is the proposal the author approves, amends or rejects through
the named `DQ`.

#### Library structure (`DQ-E1-4`, `DQ-E1-5`)

```text
models/legacy/template-v7/original/Templatev7.ggb   immutable provenance source (unchanged)
models/curated/ggt-library/
  README.md               provenance boundary and "generated, never hand-edited"
  curation.yml            AUTHORED: per tool — source package + cmdName,
                          shipped flag, transformation ids and parameters,
                          maturity, rights-disposition reference, provenance
                          evidence (2024 archive name, size, SHA-256, writer,
                          entry date)
  library-manifest.json   GENERATED: library version, generator identity and
                          toolchain, Templatev7 SHA-256, per tool: file, SHA-256,
                          user-library definition digest (v1), source macro XML
                          SHA-256, applied transformations, icon SVG canonical-LF
                          SHA-256, icon PNG SHA-256, shipped, rights status
  tools/<cmdName>.ggt     GENERATED, binary in .gitattributes
geocedg/resources/icons/ggt-library/<cmdName>.svg   AUTHORED owned icon sources
tools/legacy/curate-ggt-library.ps1                  operator entry point (-VerifyOnly)
```

Rejected, with reasons in report §K3: `tools/ggtfiles/` (tooling path, would mix
provenance and product content, `AGENTS.md` §3.2); `models/legacy/template-v7/derived/`
(`verify-legacy.ps1:470-484` admits `.ggt` there only inside `original/`, and
`derived/` is generated inventory); `geocedg/resources/ggt-library/` (puts
construction content in the resource category); `models/canonical/` (would give
convenience tools the authority rank of canonical CeDG models, `AGENTS.md` §2
item 3). The curated library is not registered in `models/manifests/catalog.yml`
(Laboratory resolution, `tools/legacy/open-laboratory.ps1`), so the legacy pins
are unaffected. The 2024 standalone archives are provenance evidence only: their
hashes enter `curation.yml`; their bytes are not imported (`AQ-G6`).

`AGENTS.md:73` lists only canonical, research, legacy and regression model
categories. A `curated` category is a governance amendment: one line in §3.2,
made only if the authorizing instruction of `E1-L` names it explicitly, as a
separate commit before any curated content. Without that authorization `E1-L`
stops before creating `models/curated/`.

#### Curation transformations (`DQ-E1-8`, `DQ-E1-9`, `DQ-E1-11`)

A closed set, each recorded per tool in `curation.yml` and in the generated
manifest; no other edit of a definition is allowed:

| ID | Transformation | Applies to |
|---|---|---|
| `T-EXTRACT` | copy one `<macro>` element by `cmdName` from the template's `geogebra_macro.xml`; everything else in the template is ignored | every curated tool |
| `T-ICON` | set `iconFile` to the tool's owned PNG entry `<md5 of PNG bytes>/geocedg-ggt-<cmdName>.png` | every curated tool |
| `T-STYLE` | the `DQ-E1-9` policy on visible linear outputs; points, texts and numerics unchanged | per `DQ-E1-9` |
| `T-HIDE` | `show object="false"` on the listed outputs | sheet tools (`AQ-G3`) |
| `T-NOSCRIPT` | remove a `ggbscript` or `javascript` element whose attributes and text are all blank | the two sheet tools |

Unchanged: command name, tool name and help (`DQ-E1-15`), ordered inputs and
outputs, construction, `copyCaptions`, `showInToolBar`. `AQ-G3` map for
`T-HIDE`: landscape `f g h i E G F`; portrait `f' f'' i'' g'' h'' F_2 G_2 H_2`;
the duplicate `h''` is hidden with the trim and kept as an output, so the tool
interface stays identical to the reference.

#### Shipped selection (`DQ-E1-6`, `DQ-E1-7`)

| Tool | Proposed disposition |
|---|---|
| `SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`, `IFPositiveSelectPoint`, `conj2mainAxesEllipse` | curate and ship |
| `EllipseAxis` | curate and ship; documented validity domain: `C` on the axis perpendicular to `OB` |
| `pointJump`, `relCoor`, `translationCoor` | curate and ship; documented: no frame, projection or spatial authority |
| `DuctSymbol`, `SymmSymbol` | curate and ship as presentation symbols |
| `sheetISOAnLand`, `sheetISOAnVert` | curate with `AQ-G3`; `shipped = false` until the author accepts their documented constraints (centimetre model unit, theoretical √2 series) or decides otherwise (`DQ-E1-7`); behavioral references for `E3` |
| `directDimension`, `axisDimension` | not curated, not shipped; behavioral references for `E2` (manual `kconversion` multiplier; name collision with the `E2` native commands) |

The eight template macros outside the 16 supplied tools are out of scope.

#### Sheet-tool disposition (`DQ-E1-7`, `DQ-E1-8`)

The curated sheet definitions exist in `E1-L` with `T-HIDE` and `T-NOSCRIPT`
applied, whatever the shipping decision, because `AQ-G3` targets the curated
definitions. Proposed `AQ-G3a` reading: "corner points hidden" applies to the
**macro-created** corner outputs; the input `D` is the user's point and is not
hidden by the tool. The technically possible alternatives are listed in
`DQ-E1-8`.

#### Style policy (`DQ-E1-9`)

A remembered last style is feasible only as a GeoCeDG frontend hook that
restyles new `AlgoMacro` outputs after creation (a restyle survives recompute,
§K10), but one remembered style per tool would erase the role distinctions
several tools need (dotted trim versus solid border, extension versus
dimension line, helper versus result). Proposed: no remembered style; the
author fallback applied to the curated definitions, with the interpretation
chosen in `DQ-E1-9`. Preparation recommendation: color black (0,0,0) and
GeoGebra line thickness **3** on every visible linear output, line type kept
per role, points, texts, numerics and hidden helpers unchanged.

#### Icon pipeline (`DQ-E1-11`)

- **Source.** One owned SVG per curated tool under
  `geocedg/resources/icons/ggt-library/`, LF, `viewBox="0 0 32 32"`, restricted to
  the subset the in-tree SVG renderer draws (paths, lines, circles, ellipses,
  rectangles, solid fill and stroke; no text, raster, external reference,
  filter or script). Drawn from the tool's own construction; never traced from,
  derived from or byte-equal to a GeoGebra stock icon or a template/archive image.
- **Rasterizer.** Java, reusing the product's in-tree `JSVGIcon` renderer as
  `GeoCeDGToolImageResource.renderImage` does, at 32 × 32, `TYPE_INT_ARGB`,
  fixed rendering hints, PNG written by `ImageIO` without metadata. Byte
  reproduction is claimed on the recorded toolchain (JDK identity recorded in
  the manifest), as the branding precedent does.
- **Archive.** The generator writes each `.ggt` with `java.util.zip`: entries in
  fixed order (`geogebra_macro.xml`, then the icon), `STORED` with precomputed
  CRC and sizes, fixed DOS time through `ZipEntry.setTimeLocal`, no extra
  fields or comments; macro XML UTF-8 without BOM, LF, fixed root
  `<geogebra format="5.0">` header.
- **Form.** The generator lives in the Desktop **test** source set (no build
  script change) and runs as a JUnit golden test that regenerates the library
  in memory from `Templatev7.ggb`, `curation.yml` and the SVG sources and
  byte-compares it with the tracked files; on mismatch it writes the
  regenerated set under the module build directory and fails.
  `tools/legacy/curate-ggt-library.ps1` runs that test and, without
  `-VerifyOnly`, copies the regenerated set into the tracked paths.
- **Registration.** `assets-manifest.yml` gains one entry per SVG source
  (`canonical_lf_sha256`, provenance "GeoCeDG-authored E1-L GGT library icon",
  licensing class per `DQ-E1-11`) and one per derived PNG (`raw_sha256` plus a
  `derivation` block naming the generator and toolchain, following the
  branding entries). The status string, schema version and
  `deliberate_exclusions` stay unchanged in `E1-L`.
- **Separation.** Sources: SVGs and `curation.yml`. Generated: `.ggt` files and
  `library-manifest.json`. No standalone PNG is tracked; the PNG exists only
  inside its `.ggt` and is identified by hash.

#### Runtime: read-only bundled catalog (`DQ-E1-10`)

- **Location.** `<app-dir>/ggt-library/` beside the packaged JARs, resolved from
  the jpackage launcher's `jpackage.app-path` property (re-established at
  implementation), overridable only through an injected path in tests; absent
  directory → empty catalog, no error, no message.
- **Integrity.** The catalog loads only the files its staged
  `library-manifest.json` lists as shipped, with matching SHA-256 and definition
  digest; any extra, missing or mismatching file makes the whole bundled
  catalog unavailable with one diagnostic in the manager. Every entry passes the
  same validation as a user install (`inspect`, digest, script rule, safe
  tables, native-name rule).
- **Presentation.** A separate read-only "GeoCeDG library" section in the
  existing manager dialog (`automation.manage-user-tools`) and in the existing
  `user-tools` menu kind; no new action id, toolbar group or profile catalog
  entry. Bundled entries cannot be removed. The owned icon is decoded from the
  bundled archive for display through the existing PNG validation; it is not
  loaded into the document image manager, so saved documents carry the
  `iconFile` string but not the icon bytes, as user-library tools do today.
- **Activation.** The existing explicit activation path (`registerWithHost`);
  the existing adoption, `DefinitionMismatch` and `DocumentConflict` policy is
  unchanged.
- **Precedence.** A stored user package whose command collides
  case-insensitively with a bundled command keeps precedence; the bundled entry
  shows as unavailable with a reason. Installing such a user package is allowed.
- **Pins.** Bundled pins live in a separate sidecar
  `<preferences>.bundled-tools-v1.json` holding only `{command, group, order}`
  and the library version; the version-3 user store is not changed, so older
  GeoCeDG builds keep reading it.
- **Rejected:** a first-run seed of the user store (mutates user data, makes
  bundled tools indistinguishable from user installs, cannot be upgraded with
  the product, collides with the existing install-conflict rule); classpath
  resources inside `desktop.jar` (would ship the definitions in every build
  before the rights gate, `INV-E1-7`, and would evade the packaging check
  instead of reconciling it).

#### Serialization

No document-format change. Activating or using a bundled tool writes its
`<macro>` into `geogebra_macro.xml` exactly as a user-library tool does today
(all activated kernel macros, used or not). A later library version never
rewrites a saved document: on reopen the document macro wins and the bundled
entry shows `DefinitionMismatch` when its digest differs. Documents made with
the original author tools or with `Templatev7.ggb` therefore keep their own
definitions.

#### Packaging contract (`E1-P`, `DQ-E1-12`)

- `windows-packaging.md` "Required exclusions" amended: `.ggt` files are
  admitted only under `app/ggt-library/`, only when listed as shipped in the
  staged `library-manifest.json`, with matching SHA-256, and only while the
  author-approved rights record (`DQ-E1-13`) admits that tool for the built
  distribution profile; `.ggb`, PDFs, `Templatev7.ggb`, repository documentation
  and non-Windows natives stay forbidden everywhere; no `.ggt` or `.ggb` inside a
  JAR is admitted.
- `build-windows-package.ps1` stages the shipped subset plus a staged manifest
  into `input/ggt-library/` after the `legal/` copy and before `jpackage`; the
  SBOM gains one component per staged file; `build-manifest.json` gains a
  library record (version, count, manifest SHA-256).
- `packaging-product.ps1`: `packaging.portable-boundary` excludes exactly the
  admitted set from the forbidden scan; a new subcontract
  `packaging.ggt-library` asserts set equality between staged files, the staged
  manifest, the repository's generated manifest and the rights disposition;
  `packaging.required-files` names the new sources.
- `infra.contract-boundary` fixtures: admitted shipped file; unlisted `.ggt`;
  hash mismatch; `.ggt` outside `app/ggt-library/`; listed but `shipped = false`;
  rights not approved; `.ggb` anywhere; existing `.pdf`/`.ggb` cases unchanged.

#### Provenance and rights gate (`DQ-E1-13`)

No legal conclusion is made here or by any later agent. `E1-L` records facts
only. `E1-P` cannot be authorized before the author approves a rights record
for the curated definitions and the owned icons; its content is `DQ-E1-13`.
The template's `review_status: "blocked"` is not changed by either subphase:
the original still contains GeoGebra UI images and JavaScript, which the
curated library excludes.

#### Feature manifest and maturity (`DQ-E1-14`)

`E1-L` adds `cedg.library.curated-ggt` to `geocedg/features/experimental.yml`,
`enabled_by_default: true`, specification
`geocedg/specs/legacy/curated-ggt-library.md`; in a build without a packaged
library the catalog is simply empty. Per-tool maturity in `curation.yml`:
`experimental`. Promotion to `stable` follows the six controlled-integration
steps; the rights step makes it an `E1-P` or `G` decision.

### Decisions requested before authorization

| ID | Question | Preparation recommendation |
|---|---|---|
| `DQ-E1-1` | Split `E1` into `E1-L` and `E1-P` (P0 `AQ-G5`, first part)? Identifiers? | split; `PRE-G9B-R6-plus-E1-L` / registry `PRE-G9B-R6-PLUS-E1-L` and `PRE-G9B-R6-plus-E1-P` / `PRE-G9B-R6-PLUS-E1-P`; re-evaluated reasons: the rights gate must precede any distribution (`INV-E1-7`), and the acceptance surfaces differ (Desktop/JUnit versus packaging artifacts and a verification-core test). Alternative: one unsplit `E1`, authorized only after `DQ-E1-13`, with the union of both evidence sets |
| `DQ-E1-2` | `E1-L` verification class | `BOUNDED_PHASE`, one registered `PHASE`; all reproducibility, provenance and runtime checks as JUnit; no new `tools/agent/checks/` script, no build-script change, no shared-kernel, document-format or action-catalog change. Any of those → stop with `VERIFICATION_ESCALATION_REQUEST`. Alternative: a PROCESS reproducibility leaf, which by the R6 precedent is `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` `PHASE_LOCAL` |
| `DQ-E1-3` | `E1-P` verification class | not silently inherited from P0. Facts: `packaging.product` is a `PRODUCT` leaf in `INTEGRATION`/`FINAL`; the changed fixtures live in the `VERIFICATION_CORE` leaf `infra.contract-boundary`; `OPERATIONAL` is `INCOMPLETE`. Recommended: `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, impact `PHASE_LOCAL`, focused evidence = registered `PHASE` (`packaging.product`) + `INFRA_UNIT` + `PACKAGING -CheckToolchain` + `PACKAGING -VerifyPackagingArtifacts` per built artifact set + packaging smoke; no `FINAL`. Alternatives: `GLOBAL_IMPACT` (`FINAL` + `PACKAGING`, the P1 precedent); `INTEGRATED_PHASE` (`PHASE` + `INTEGRATION` + `PACKAGING`) |
| `DQ-E1-4` | Repository structure (`AQ-G1`) and governance amendment | the structure above; one-line `AGENTS.md` §3.2 amendment adding a `curated` model category, explicitly authorized in the `E1-L` instruction and committed alone first; `tools/ggtfiles/` rejected |
| `DQ-E1-5` | Provenance source; ingest the 2024 archives (`AQ-G6`)? | curate from the immutable `Templatev7.ggb`; record the 16 archive hashes as provenance evidence; no ingest (an ingest changes the `verify-legacy.ps1` pins) |
| `DQ-E1-6` | Shipped subset of the non-sheet tools (`AQ-G2`) | the twelve tools above shipped; `directDimension`/`axisDimension` neither curated nor shipped |
| `DQ-E1-7` | Sheet tools: ship? | curate with `AQ-G3`; `shipped = false` unless the author accepts the documented centimetre-unit assumption and theoretical √2 sizes (≤ 0.65 mm from ISO 216 for A0–A6); alternatives: ship with those constraints documented; ship until `E3` and retire at `G`; a further curated correction (unit-aware or ISO-rounded geometry) is a new author requirement, not `AQ-G3` |
| `DQ-E1-8` | `AQ-G3a`: does "corner points hidden" include the input `D`? | (a) macro-created corners only — recommended; (b) a Desktop post-creation hook that hides `D` only when the tool created it on the fly (new runtime code; changes a user object); (c) change the interface so the corner is an output (e.g. numeric coordinates instead of a point; breaks equivalence with the reference); (d) defer the sheet tools to `E3` |
| `DQ-E1-9` | `AQ-G7`: meaning of "black at 3 pt"; do role-specific line types remain? | GeoGebra thickness value 3 (1.5 px stroke; already the sheet border and symbol thickness) in black, line types kept per role, points/texts unchanged. Alternatives: literal 3 typographic points (no fixed thickness equals 3 pt on every surface: 4 px at 96 dpi ≈ thickness 8, 3 px ≈ thickness 6 when 1 px = 1 pt); another explicit normalized value; uniform solid line type |
| `DQ-E1-10` | Runtime model and policies | read-only bundled catalog (not first-run seed); installation-relative directory; user package precedence; sidecar pin store; whole-catalog fail-closed integrity |
| `DQ-E1-11` | Icon pipeline form and icon licensing class | Java golden-test generator + `tools/legacy/curate-ggt-library.ps1`; icons classed like the existing owned tool icons (`geocedg-documentation-or-ordinary-art`, redistribution status set by the author at closeout) |
| `DQ-E1-12` | Packaging route (`AQ-G5`, second part) | the manifest-driven contract above; which artifact sets (NC, INTERNAL) carry the library; whether `portable-boundary` also starts inspecting JAR entries (only the web resource `ggmexample.ggb` exists among tracked main resources; Desktop JARs would need an inventory first) |
| `DQ-E1-13` | `AQ-G4`: the rights/provenance record required before a definition becomes a redistributable asset | the author decides; preparation lists candidate elements only (report §K7): per-tool identity and hashes; authorship declaration and dates; exclusion of GeoGebra images and JavaScript; the licence chosen for the curated definitions and for the icons; open questions for human or professional review (e.g. material generated by GeoGebra software inside the macro XML); distribution profiles covered (NC, INTERNAL; COMMERCIAL stays pending external terms); record location and its JSON mirror; the exact author decision label |
| `DQ-E1-14` | Feature id, maturity and specification route | `cedg.library.curated-ggt`, experimental, enabled by default; spec `geocedg/specs/legacy/curated-ggt-library.md` and ADR 0033 as the first `E1-L` deliverables, approved with the `E1-L` closeout; promotion to stable decided at `E1-P` or `G` |
| `DQ-E1-15` | Tool names and help texts | keep the author's texts verbatim (e.g. `CirclebyD: Centre & Diameter`); the bilingual guide (`G`) documents them; any rename is a new author decision |
| `DQ-E1-16` | If `DQ-E1-13` cannot be satisfied in this mini-track | `G` depends on `E1-L` only and `E1-P` is re-planned by the author; otherwise `G` depends on both |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
P_R6PLUS_C_X1 = 23b971f5a967958f93e745ca8c39fce1162706e6
tree          = 5f564c8391828dc781dee0b1be93ef223431af58
```

`P_R6PLUS_C_X1` is the published `main` after the `C-X1` closeout. The `E1-L`
implementation branch starts from the exact commit the authorizing instruction
names: either `P_R6PLUS_C_X1` or the preparation candidate that contains this
prompt (documentary only, no product delta). `E1-P` starts from the published
`E1-L` closeout, named exactly by its own authorizing instruction. Entry gate:
local `main`, `origin/main` and the live remote `main` agree with the named
base, the worktree is clean, the roadmap still records the predecessor as
`PASS — AUTHOR APPROVED — PUBLISHED`, and `Templatev7.ggb` still has the hash of
`INV-E1-1`.

## Authority and evidence hierarchy

1. `AGENTS.md`, then current code, build and tests at the base.
2. `geocedg/specs/operations/verification-levels.md`, the typed registry, the
   controlled-integration and Windows packaging contracts.
3. The author instructions: the mini-track plan scope and `AQ-G3`, the
   authorizing instruction of the subphase and its `DQ-E1` dispositions.
4. The characterization report and its JSON mirror; the P0 design candidate
   (evidence, not authority).
5. Earlier scratch probe outputs and session notes (lowest rank; never a
   substitute for re-establishing a fact).

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### `E1-L`

- Governance (only if explicitly authorized, `DQ-E1-4`): the one-line
  `AGENTS.md` §3.2 amendment, in its own commit.
- Curated content: `models/curated/ggt-library/**` (README, `curation.yml`,
  generated `library-manifest.json`, generated `tools/*.ggt`), one
  `.gitattributes` rule marking `models/curated/**/*.ggt` binary.
- Owned icons: `geocedg/resources/icons/ggt-library/*.svg`; new entries in
  `geocedg/resources/assets-manifest.yml` (no change to its status string,
  schema version or `deliberate_exclusions`).
- Tooling: `tools/legacy/curate-ggt-library.ps1`.
- Desktop application layer: `GeoCeDGUserToolLibrary.java`,
  `GeoCeDGUserTools.java` and new classes under
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/` for the bundled
  catalog; `UserTools.*` texts in `apps/geocedg/application-profile.yml`
  (texts only: no action, group, toolbar or feature-flag catalog change).
- Specification and decision: `geocedg/specs/legacy/curated-ggt-library.md`,
  `docs/adr/0033-*.md`, `geocedg/features/experimental.yml` entry.
- Tests: new Desktop tests (generator golden test, transformation and
  invariant tests, catalog tests) under
  `source/desktop/desktop/src/test/java/org/geocedg/desktop/`, their
  registration and the registry-shape pins that registration moves.
- The user-tools sections of both user guides, if the authorizing instruction
  does not defer all help to `G`; the candidate report, its machine-readable
  evidence, and the roadmap and mini-track status lines.

### `E1-P`

- `geocedg/specs/packaging/windows-packaging.md` (the admission amendment only);
  `tools/release/build-windows-package.ps1`; `packaging/windows/**` only where
  the staging needs a declared input.
- `tools/agent/checks/packaging-product.ps1`;
  `tools/agent/tests/verification-contract-boundary.Tests.ps1`.
- The rights record approved under `DQ-E1-13` and the licensing records it
  requires: `docs/licensing/component-matrix.md`, `THIRD_PARTY.md`,
  `NOTICE.md`, `LICENSES/**` with `LICENSES/manifest.json`, the
  `assets-manifest.yml` exclusion wording.
- The `cedg.library.curated-ggt` promotion decided under `DQ-E1-14`; the
  `shipped` and rights fields of `curation.yml` and the regenerated manifest.
- Phase registration, the candidate report, evidence and status lines.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any byte under `models/legacy/**`, including `Templatev7.ggb`, its manifest,
  `curation.yml`, `derived/` and `review_status`; ingesting the 2024 archives;
  editing `verify-legacy.ps1` or `models/manifests/catalog.yml`.
- Placing authoritative GGT sources under `tools/ggtfiles/` or anywhere outside
  the approved structure; hand-editing a generated `.ggt` or manifest.
- Shipping or embedding any GeoGebra stock icon, template image, the template's
  JavaScript, or any `.ggb`; tracing owned icons from stock icons.
- Curated `.ggt` content inside `desktop.jar` or any other JAR or `installDist`
  output in `E1-L`; any packaging change in `E1-L`.
- Shared kernel, `AlgoMacro`, `Macro`, `MacroManager`, `MyXMLioJre`, Classic,
  document format, undo, preferences format of the version-3 user store,
  document-macro adoption policy, the user-library install rules for user
  packages, the action catalog and toolbar groups.
- A remembered-style frontend hook (unless `DQ-E1-9` decides otherwise).
- Native dimensions (`E2`), `IsoABorder` and ISO export areas (`E3`),
  orientation (`F1`), authoring memory (`F2`), File → Insert (`F3`), help and
  closeout (`G`), `PRE-G9B-R7`, `G9B`.
- `OBS-R6PLUS-E2-LEGACY-DIMENSION-MACRO-NAME-COLLISION` (owner `E2`),
  `OBS-R6PLUS-DESKTOP-ADJACENT-OFF-EDT-SITES`,
  `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`,
  `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT`,
  `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION`, and every other retained debt.
- Any legal conclusion; changing `PROFILE COMMERCIAL`; publication.

## Architectural placement

`E1-L`: GeoCeDG application layer (Desktop user-tool library and manager),
curated model content under `models/`, owned resources under
`geocedg/resources/`, tooling under `tools/legacy/`. `E1-P`: packaging/release
layer (`AGENTS.md` §4 "Own installer"). No kernel placement: the curated tools
are ordinary macros over existing primitives and carry no new geometric
semantics; the library is not a geometric authority.

## Required design/specification

- `geocedg/specs/legacy/curated-ggt-library.md` (first `E1-L` deliverable): the
  structure, transformation set, invariants `INV-E1-1`–`INV-E1-7`, runtime
  catalog, pin sidecar, serialization statement and the four-way distinction,
  as approved through the `DQ-E1` dispositions.
- `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md`: the
  `curated` model category, the rejected alternatives and the split rationale.
- `E1-P`: the amendment of `windows-packaging.md` and the rights record. No
  other specification or ADR is created by the agent.

## Geometric invariants and degeneracies

No geometric object, algorithm, tolerance or dependency changes. The curated
constructions are the template constructions with only the listed style,
visibility, icon and blank-script transformations. Tests prove construction
identity: for every curated tool, the `<construction>` with `T-STYLE` and
`T-HIDE` attributes and `<eqnStyle>` tags normalized is equal to the
template's, and the ordered inputs and outputs are equal. Documented validity
domains (`EllipseAxis`) and limitations (sheet unit assumption, no frame
authority of the transport tools) are statements, not new semantics.

## Compatibility and serialization

```text
SERIALIZATION CHANGE          = NONE (document format)
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE (documents); NEW application sidecar
                                <preferences>.bundled-tools-v1.json (E1-L);
                                NEW library manifest and packaging records (E1-P)
```

Existing user stores (versions 1, 2 and 3) load and save byte-identically when
no bundled pin is set; older GeoCeDG builds ignore the sidecar. Documents
created with the original tools or the template keep their macros; a bundled
entry never replaces a document macro.

<!-- geocedg-field: required_checks -->
## Required tests and commands

`E1-L` focal tests, in its phase selection:

| ID | Obligation | Kind |
|---|---|---|
| `T-E1-ORIGINAL` | `Templatev7.ggb` hash equals `INV-E1-1`; no tracked change under `models/legacy/**` | deterministic |
| `T-E1-REPRO` | golden test: regeneration from the recorded inputs is byte-identical to every tracked `.ggt` and to `library-manifest.json` | deterministic (recorded toolchain) |
| `T-E1-MANIFEST` | manifest ↔ files ↔ `curation.yml` set equality; SHA-256 and definition digests recomputed | deterministic |
| `T-E1-EQUIV` | per tool: ordered inputs/outputs equal to the template; construction equal after the documented normalization | deterministic |
| `T-E1-AQG3` | sheet tools: exactly the `T-HIDE` outputs hidden in the definition and created hidden by `AlgoMacro`; all other outputs unchanged | deterministic |
| `T-E1-STYLE` | `T-STYLE` applied exactly per `DQ-E1-9`; nothing else restyled | deterministic |
| `T-E1-ASSETS` | no curated archive contains JavaScript, non-blank script, a stock-named or template/archive-identical image, or an icon whose bytes match any PNG under `source/`; every `iconFile` names the owned entry in its own archive | deterministic |
| `T-E1-ICONS` | each owned SVG is in the allowed subset; its canonical LF hash and the PNG hash match `assets-manifest.yml` | deterministic |
| `T-E1-CATALOG` | injected directory: listing, read-only behavior, whole-catalog fail-closed on extra/missing/mismatching files, validation identical to user installs, absent directory → empty | deterministic |
| `T-E1-PRECEDENCE` | user package collision, document `DefinitionMismatch`/adoption, no store write by the catalog | deterministic |
| `T-E1-STORE` | version-1/2/3 store fixtures load and save byte-identically; sidecar created only by a bundled pin; old store readers unaffected | deterministic |
| `T-E1-ROUNDTRIP` | activate and use a bundled tool, save `.cedg`, reopen: macro present, `iconFile` string present, no icon bytes, outputs and hidden states preserved | deterministic, child JVM |
| `T-E1-NOJAR` | no `.ggt` under any module's `src/main/resources`, and no build script or resource task references `models/curated` (`INV-E1-7`); the candidate report adds one listing of a built `installDist` `lib/` as development evidence | deterministic |
| `T-SMOKE` | author smoke checklist (*Required artifacts*) | author |

`E1-P` focal checks: the `infra.contract-boundary` fixtures listed under
*Packaging contract*; `packaging.ggt-library` and the amended
`packaging.portable-boundary` on real artifacts; packaging smoke on the built
app-image and installer (install or portable run, launch, bundled section
lists exactly the shipped tools with owned icons, activate and use one, save
and reopen, uninstall leaves the user store and sidecar intact).

Harness rules: isolated settings files, never the author's `%APPDATA%`; app
scenarios in child JVMs (Desktop test heap); `LoggerD` per test and no logged
runtime exceptions in AppCommon tests; follow the existing Desktop harness
rules for modal dialogs and the asynchronous undo store; new files under
`source/` registered where the upstream-boundary contract requires it; ASCII
escapes in non-test sources.

Adjacent regressions before freezing (development evidence):
`G9U1UserToolLibraryTest`, `G9U1MacroNativeArchivePersistenceTest`,
`GeoCeDGProfileTest`, `G9U1ActionRegistryTest`; Checkstyle main and test;
`Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline
9b93256b7df401ff056c37b502d82df4d72b1522`; `tools/agent/verify-legacy.ps1`
(standalone, diagnostic evidence that the legacy pins are untouched);
`git diff --check`.

Registration and catalog: register the phase selection `PRE-G9B-R6-PLUS-E1-L` with
`compile.shared.semantic`, `compile.desktop.semantic`, `workspace-profile.product`
and `junit.desktop.pre-g9b-r6-plus-e1-l.semantic`; discovery dry-run and
**executed** selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with absolute
paths, the canonical pin reproduced before repinning; no hash entered by hand;
a development `INFRA_UNIT` on the staged tree when registry-shape pins change.
`E1-P` registers `PRE-G9B-R6-PLUS-E1-P` with `packaging.product` (plus any
`E1-P` JUnit selection).

Acceptance, on one clean immutable committed candidate per subphase, as frozen
by `DQ-E1-2` and `DQ-E1-3`. Recommended plans:

```text
E1-L (BOUNDED_PHASE):
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L -PlanOnly
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L -LogDirectory <log root>

E1-P (OPERATIONAL_VERIFICATION_INFRASTRUCTURE, PHASE_LOCAL):
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-P -LogDirectory <log root>
tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -LogDirectory <log root>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -VerifyPackagingArtifacts
                       -PackagingArtifactRoot <artifact set root> -LogDirectory <log root>
                       (once per built artifact set named by DQ-E1-12)
```

The `-Phase` spelling follows the `C-X1` acceptance precedent; the registry
phase ids are the upper-case forms of `DQ-E1-1`.

Each run `ACCEPTED / COMPLETE` on the exact commit and tree; no further commit
afterwards. A run not required by the frozen class is a
`VERIFICATION_ESCALATION_REQUEST` for the author. A failure proven to predate
the candidate and lie outside its delta is retained baseline debt, not phase
scope. Report exact commands, exit codes, run ids, plan and result hashes and
log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing is authorized by this prompt. A future author instruction may authorize
`E1-L` (and, separately and later, `E1-P`) on an exact base, with the `DQ-E1`
dispositions; it then authorizes local implementation on a local branch, local
commits, the named governance amendment if any, focused tests, the phase
registration and its official catalog updates, development `INFRA_UNIT` and
`STATIC` runs, the frozen acceptance runs, and one frozen technical candidate
for author review and author smoke. It never authorizes self-approval, author
smoke by the agent, a run above the frozen class, a legal conclusion, or a
change of the class; a proven inability of the frozen class to cover the
change is reported as a reclassification proposal, never applied silently.

`E2`, `E3`, `F1`, `F2`, `F3`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized,
and so does every open observation and enhancement not named here. Author
approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local branches and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release, installer or binary publication, and any distribution of the library
are forbidden; each needs a separate explicit author instruction naming the
exact candidate SHA. Building packaging artifacts for verification is not
publication. Acceptance evidence never grants publication authority.

## Acceptance and closeout

Each authorized subphase stops with one technically verified candidate pending
author review and author smoke:

```text
PRE-G9B-R6-plus-E1-<L|P> = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION   = ACCEPTED / COMPLETE (frozen class)
AUTHOR_SMOKE             = PENDING (required)
selfApproved = false, authorApproved = false, passClaimed = false
```

The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps. `E1` as a whole is closed only when every
subphase the author keeps in scope (`DQ-E1-16`) is `PASS — AUTHOR APPROVED`.

## Required artifacts

- `E1-L`: the governance commit if authorized; the specification and ADR; the
  curated library, icons, manifests and tooling; the Desktop delta and tests;
  the phase registration; a candidate report with entry-gate evidence, `K1`–`K12`
  re-established with every correction, the generated manifest, the hash
  tables, the four-way record per tool, test evidence and an author-smoke
  checklist (open the manager, see the bundled section with owned icons —
  empty in an unpackaged run unless the test path is injected —, activate
  `CirclebyD` and a sheet tool if shipped, verify hidden trim and corners and
  the line style, save and reopen, confirm the user store is untouched).
- `E1-P`: the contract amendment, builder and check deltas, fixtures, the
  licensing records under the approved rights record, the built artifact
  hashes, the packaging smoke record and an author-smoke checklist for the
  installed product.
- Both: `GUIDE_IMPACT`, bootstrap impact and verification-infrastructure impact
  declarations; `git diff --check`; final repository status.

## Stop conditions

Stop and report rather than guess when:

- the `DQ-E1` disposition needed by the current step is missing or ambiguous;
- the `AGENTS.md` amendment is needed but not explicitly authorized;
- any change under `models/legacy/**` would be needed;
- a curated definition cannot be generated byte-reproducibly, or the in-tree
  SVG renderer cannot draw an owned icon deterministically;
- a curated definition would need an edit outside the closed transformation set;
- an archive would need a stock or template image, JavaScript or a non-blank script;
- the bundled catalog would need a new action, toolbar group, shared-kernel,
  document-format or build-script change (class escalation);
- `jpackage.app-path` or an equivalent installation-relative resolution is not
  available without a launcher or build change;
- an existing user store or document would change behavior beyond the stated
  precedence rule;
- `E1-L` would place curated `.ggt` content into any packaging input;
- `E1-P` is requested without an author-approved rights record, or the rights
  record leaves a shipped tool or icon undecided;
- the packaging amendment cannot stay fail-closed for every non-admitted
  `.ggt`/`.ggb`;
- licensing is unclear (`AGENTS.md` §16): record facts and questions only;
- the frozen class cannot cover the change.

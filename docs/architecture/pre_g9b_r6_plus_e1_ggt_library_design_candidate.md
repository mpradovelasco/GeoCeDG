# PRE-G9B-R6-plus-E1 — GGT library, provenance, owned icons and packaging: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. No legal conclusion is made
  or implied. Subphase `E1` needs its own explicit author authorization, and
  any distribution needs a separate rights record and author decision.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  §6 (GGT characterization), findings 15, 16, 20 and probe P4

## 1. Author-fixed scope

A curated GGT library using the local author tools as references, with
provenance, installer integration and GeoCeDG-owned, reproducibly maintained
icons. Packaging support is a required design target. Style through line type,
color, thickness and remembered style if sustainable, otherwise black at 3 pt.
In their future authorized phase, `sheetISOland`/`sheetISOvert` hide the outer
dotted rectangle and all corner points.

## 2. Four things kept distinct

```text
macro behavior / reference      what a tool constructs; usable as a design reference
macro source / provenance       who authored the definition and under which terms
GeoCeDG-owned replacement icons new owned assets, never the bundled stock icons
redistributable packaged asset  only after an explicit rights record and author decision
```

Geometric equivalence with `template-v7` grants no redistribution permission.

## 3. Facts at the base

- **All 16 author tools already exist** in the immutable G3 package
  `models/legacy/template-v7/original/Templatev7.ggb` (SHA-256 `f62e5b7a…ed4113`,
  `models/legacy/template-v7/manifest.yml:42-44`), with identical command name, ordered inputs and
  outputs, and identical construction once the writer's `eqnStyle` tags are
  ignored. Nine are raw-equal. Three lack the template's icon reference (P0
  report §6).
- Rights: `models/legacy/template-v7/manifest.yml:36-37` "embedded GeoGebra UI images and code; no
  complete redistribution clearance", `review_status: blocked`.
  `docs/licensing/component-matrix.md:21`, `:70` and `:80` classify GeoGebra UI
  images as CC BY-NC-SA terms and exclude Templatev7 from release packaging
  until human review. 12 of the 13 icon-carrying author archives use GeoGebra
  stock toolbar names; only `circulodiametro.png` is custom-named. The 13
  bundled icons are byte-identical to the template's.
- Governance: `AGENTS.md:73` (`.ggt` under `models/`), `:142-152` (legacy import
  first under `models/legacy/` with a manifest); the
  [controlled-integration contract](../../geocedg/specs/legacy/controlled-integration.md)
  `:22-31` (`original/` immutable, `manifest.yml`, `curation.yml`, `derived/`),
  `:36-49` (`tools/legacy/ingest.ps1`; generated inventory never hand-edited)
  and `:65-73` (six promotion steps).
- **Verifier pins (correction to planning).** `tools/agent/verify-legacy.ps1:210-215`
  asserts exactly three registered legacy manifests, and `:470-483` asserts
  exactly three legacy resources under `models/legacy`. Any new legacy package
  is a verification-infrastructure change.
- **Packaging.** [Windows packaging](../../geocedg/specs/packaging/windows-packaging.md)
  (`geocedg/specs/packaging/windows-packaging.md:83-85`) requires the pipeline to fail on `.ggb`/`.ggt` content. It is
  enforced only by `tools/agent/checks/packaging-product.ps1:562-567`
  (`packaging.portable-boundary`, file extensions in the app-image; JAR
  contents are not inspected).
- **User-tool library (probe P4, correction to planning).** All 16 author
  archives install. The script rule rejects only non-blank `ggbscript` or
  `javascript` content
  (`source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserToolLibrary.java:828-837`),
  so the sheet tools' `onUpdate="&#xd;&#xa;"` is accepted. The library installs
  only from a user-chosen file (`GeoCeDGUserTools.java:488-498`), ignores the
  package `iconFile` for presentation (`GeoCeDGUserToolLibrary.java:1067-1071`)
  but includes it in the definition digest (`:939-945`), and has no bundled
  catalog or first-run seed.
- **Style (finding 15).** `AlgoMacro.createOutputObjects` copies each output's
  fixed style and disables visual defaults (`common/kernel/algos/AlgoMacro.java:288-315`).
  With `copyCaptions="true"`, which all 16 macros carry, outputs hidden in the
  macro definition are created hidden (`AlgoMacro.java:118-128`).

## 4. `AQ-G3` effect on the sheet macros (finding 16)

| Macro | Outer dotted rectangle (thickness 2, line type 20) | Corner-point outputs | Unaffected outputs |
|---|---|---|---|
| `sheetISOAnLand` (21 outputs) | `f`, `g`, `h`, `i` | `E`, `G`, `F` | inner border `n p q r`; title block `b f_1 j_1 l_1 m_1 a_1 b_1 f_2 j_2 i_2` |
| `sheetISOAnVert` (22 outputs) | `f'`, `f''`, `i''`, `g''`, `h''` (`h''` duplicates `f'`) | `F_2`, `G_2`, `H_2` | inner border `q'' n'' p'' r''`; title block `b'' a_2 b_2 e_2 f_3 g_3 h_3 i_3 j_3 k_3` |

Implementation path: set `show object="false"` on those outputs in the
**derived** curated definition; `AlgoMacro.java:118-128` then hides them at
creation, with no runtime code. The upper-left corner `D` is the user's
**input** point, not a macro output, so a macro cannot hide it (`AQ-G3a`: does
the requirement include `D`?). Both macros also carry an unlabelled internal
NaN point from a two-output `Intersect`; it is not an output. The derived
curation may drop the duplicate `h''`.

## 5. Candidate structure (`AQ-G1`)

1. **Canonical provenance source — unchanged.**
   `models/legacy/template-v7/original/Templatev7.ggb` stays the immutable source
   of all 16 definitions. The author's standalone 2024 archives are recorded as
   **provenance evidence**: SHA-256, archive dates, writer versions and the
   equivalence result, inside the curated-library manifest. Their bytes are not
   re-imported.
   Alternative (`AQ-G6`): ingest the 16 archives byte-identically as a new
   legacy package through `tools/legacy/ingest.ps1`. That changes the
   `verify-legacy.ps1` pins (verification infrastructure).
2. **Curated library — a GeoCeDG-owned derived artifact.** Candidate location
   `models/curated/ggt-library/`:
   - `library-manifest.json`: per tool, the source package and macro, source
     SHA-256, applied transformations, maturity, rights status, shipped flag;
   - generated `.ggt` files, never hand-edited.

   `AGENTS.md` §3.2 places `.ggt` files under `models/`, but lists only the
   *canonical, research, legacy and regression* categories there (`AGENTS.md:73`).
   It assigns `geocedg/resources/` to icons, styles, translations and branding
   (`AGENTS.md:67`). A `curated` category under `models/` is therefore a small,
   explicit **governance amendment** for the author (`AQ-G1`), not a choice made
   here. `geocedg/resources/ggt-library/` was considered and rejected: it would
   put `.ggt` content into the resource category. INFERRED: the legacy verifier
   lists only manifests of maturity `legacy` in `models/manifests/catalog.yml`
   (`tools/agent/verify-legacy.ps1:205-215`), and counts resources only under
   `models/legacy` (`:470-483`). A curated, non-legacy package would therefore
   not trip those pins. `E1` must confirm that no other model-catalog check
   applies.

   A proposed deterministic curation script (`tools/legacy/curate-ggt-library.ps1`,
   not yet existing; on the ingest precedent) extracts each macro from the
   immutable original and applies the recorded transformations:
   - style fallback;
   - `AQ-G3` hiding;
   - owned-icon reference;
   - removal of the whitespace-only `ggbscript`;
   - optional duplicate removal.
3. **Owned icons.** SVG sources under `geocedg/resources/icons/ggt-library/`.
   A deterministic rasterizer to the library's PNG size follows the
   `tools/resources/generate-geocedg-branding.ps1` precedent: pinned inputs,
   fixed interpolation and a `-VerifyOnly` byte compare. Entries are added to
   `geocedg/resources/assets-manifest.yml` with canonical LF SHA-256 and
   provenance. GeoGebra stock icons are never shipped.
4. **Runtime.** A read-only **bundled catalog** in the user-tool manager lists
   shipped tools separately and activates them without writing the user's
   store until the user pins one. Rejected: a first-run seed of the user store,
   which mutates user data and complicates versioning.
5. **Packaging.** A staging step copies the approved curated `.ggt` files into
   the app-image under a dedicated path, for example `app/ggt-library/`. The
   Windows packaging contract and `packaging.portable-boundary` are amended to
   admit `.ggt` **only** at that path, **only** with a matching
   `library-manifest.json` hash and **only** for entries marked shipped
   (`AQ-G5`, explicit author-approved contract amendment). Hiding `.ggt`
   content inside a JAR to evade the check is explicitly rejected.

`tools/ggtfiles/` is **rejected**. `tools/` holds tooling, not distributable
content (`AGENTS.md` §3.2), and a library there would mix provenance and
product assets.

## 6. Style decision (finding 15)

A remembered style is **not sustainable** for this library. Several tools have
roles that a single remembered style would destroy (dotted trim versus solid
border; extension lines versus the dimension line; points versus linework). The
style bar cannot target macro outputs, so remembering would need a new frontend
hook re-styling `AlgoMacro` outputs after creation. Recommended: the **author
fallback**, black at 3 pt, applied to the curated derived definitions.

Two interpretations of that fallback need the author (`AQ-G7`):

- **Unit.** GeoGebra's line thickness is not in points: strokes are drawn at
  `lineThickness / 2.0` px (`common/euclidian/Drawable.java:626`). "3 pt" can
  mean thickness value 3 (a 1.5 px stroke) or 3 typographic points (about
  thickness 6 at 72 dpi screen scale).
- **Roles.** P0 proposes to keep role-specific line types where a role needs
  them, for example the dotted trim before `AQ-G3` hides it. The author's text
  says only "black at 3 pt", so this is an interpretation.

Point sizes are unchanged.

## 7. Proposed shipped selection (`AQ-G2`, subject to author)

| Tool | G9U1 class | Recommendation |
|---|---|---|
| `directDimension`, `axisDimension` | 3 | not shipped once `E2` is accepted; reference only |
| `sheetISOAnLand`, `sheetISOAnVert` | 5 | not shipped once `E3` is accepted (they use the unrounded theoretical A series, A3 ≈ 420.45 × 297.30 mm); otherwise ship with `AQ-G3` applied |
| `SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`, `IFPositiveSelectPoint`, `EllipseAxis`, `conj2mainAxesEllipse` | 2–3 | ship; documented validity domain for `EllipseAxis` (assumes `C` on the axis perpendicular to `OB`) |
| `pointJump`, `relCoor`, `translationCoor` | 4/6 | ship, stating that they carry no frame or projection authority |
| `DuctSymbol`, `SymmSymbol` | 5 | ship as presentation symbols |

## 8. Serialization

No document format change. Using a curated tool saves its `<macro>` into the
document (`source/shared/common-jre/src/main/java/org/geogebra/common/jre/io/MyXMLioJre.java:350-363`),
including the owned `iconFile`. Owned icons may therefore travel inside saved
`.cedg` archives, which is acceptable for owned assets.

## 9. Verification-class recommendation (corrected)

The planning proposal `BOUNDED_PHASE` (plus packaging evidence) is **corrected**.
Packaging support requires amending `packaging-product.ps1` under
`tools/agent/**`, and ingesting new originals would change `verify-legacy.ps1`.
Recommended split (`AQ-G5`):

- `E1-L`: curation script, curated library, owned icons, bundled catalog and
  the `AQ-G3` modifications. `BOUNDED_PHASE` (registered `PHASE`), provided
  no new `models/legacy` package is ingested.
- `E1-P`: packaging contract amendment and the `packaging.portable-boundary`
  change, with the packaging smoke that `AGENTS.md` §14 requires.
  `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, impact frozen at phase start
  (`PHASE_LOCAL` or `GLOBAL`). It needs the rights record (`AQ-G4`) before any
  distribution.

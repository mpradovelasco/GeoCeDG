# Curated GGT library

| Field | Value |
|---|---|
| Status | **CANDIDATE — APPROVAL ONLY THROUGH THE `PRE-G9B-R6-plus-E1-L` AUTHOR CLOSEOUT** |
| Version | `1.0` |
| Owner phase | `PRE-G9B-R6-plus-E1-L` (library, curation, owned icons, read-only bundled catalog) |
| Packaging | `PRE-G9B-R6-plus-E1-P` (`PREPARED — NOT AUTHORIZED`) |
| Decision | [ADR 0033](../../../docs/adr/0033-curated-ggt-library-and-distribution-boundary.md) |
| Governing author decisions | [E1 author decisions and E1-L authorization record](../../../docs/validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md); [rights record version 1](../../../docs/licensing/curated-ggt-library-rights-record.md) |
| Feature | `cedg.library.curated-ggt`, `experimental`, enabled by default |
| Serialization effect | none on documents; a new application sidecar for bundled pins |

## 1. Purpose and boundary

The curated GGT library offers twelve author-approved user tools as
GeoCeDG-owned derived definitions. They are ordinary macros over existing
primitives; they carry no new geometric semantics and no geometric authority
(`AGENTS.md` §4). Four things stay distinct for every tool:

```text
behavioral reference       what the tool constructs (the template macro)
source / provenance        the immutable Templatev7.ggb macro and the author's 2024 exports
asset ownership            curated definition: geocedg-software (EUPL-1.2);
                           owned icon: geocedg-documentation-or-ordinary-art (CC-BY-4.0)
redistribution permission  the author rights record (INTERNAL and NC; COMMERCIAL not authorized)
```

Geometric equivalence is never evidence of redistribution permission.

## 2. Structure

```text
models/legacy/template-v7/original/Templatev7.ggb   immutable provenance source (read only)
models/curated/ggt-library/curation.yml             authored curation record
models/curated/ggt-library/library-manifest.json    generated
models/curated/ggt-library/tools/<command>.ggt      generated, binary
geocedg/resources/icons/ggt-library/<command>.svg   authored owned icon sources
tools/legacy/curate-ggt-library.ps1                  operator entry point (-VerifyOnly)
```

The library is not registered in `models/manifests/catalog.yml` and never writes
under `models/legacy/`.

## 3. Selection

`SquarebyDiagonal`, `CirclebyD`, `circArcbyAngle`, `ellipseLength12`,
`IFPositiveSelectPoint`, `EllipseAxis`, `conj2mainAxesEllipse`, `pointJump`,
`relCoor`, `translationCoor`, `DuctSymbol`, `SymmSymbol`. Command names, tool
names and help texts are the author's, verbatim.

Statements: `EllipseAxis` is valid only when `C` lies on the axis through the
centre `O` perpendicular to `OB`. `pointJump`, `relCoor` and `translationCoor`
carry no spatial-frame, projection or spatial-identity authority.

Excluded: `directDimension`, `axisDimension` (behavioral references for `E2`),
`sheetISOAnLand`, `sheetISOAnVert` (behavioral references for `E3`: they assume
1 model unit = 1 cm and the theoretical √2 sizes), and the other template
macros.

## 4. Transformations (closed set)

| ID | Transformation |
|---|---|
| `T-EXTRACT` | the exact `<macro …>…</macro>` text of the source command, wrapped in the template's own prolog/root and closing text |
| `T-ICON` | `iconFile` set to `<lower-case MD5 of the PNG bytes>/geocedg-ggt-<command>.png` |
| `T-STYLE` | on each macro **output** of type `line`, `segment`, `ray`, `vector`, `conic`, `conicpart`, `polygon` or `polyline`: `objColor` `r`, `g`, `b` = `0` (alpha kept), `lineStyle` `thickness` = `3` (line type kept) |

"Black at 3 pt" means GeoGebra `lineThickness = 3`, not typographic points.
Points, texts, numerics and non-output helpers are unchanged. Every edit is an
exact replacement that must match once.

## 5. Owned icons and determinism

Each icon is an owned SVG (`viewBox="0 0 32 32"`, LF; paths, lines, circles,
ellipses, rectangles, polylines, polygons and groups only; no text, raster,
reference, style attribute, filter or script). It is rasterized through the
product's EchoSVG route (`JSVGImageBuilder`/`SVGImage`) into a 32 × 32
`TYPE_INT_ARGB` image with fixed hints and written as PNG. The PNG exists only
inside its archive. Archives hold exactly `geogebra_macro.xml` and the icon,
`STORED`, DOS time 1980-01-01T00:00, in that order.

Byte reproduction of every archive and of the manifest is claimed on the
recorded Desktop test JVM (`curation.yml`, `recorded_toolchain`); the golden
test regenerates the library and compares bytes.

## 6. Runtime: read-only bundled catalog

- Location: `<launcher dir>/app/ggt-library/`, derived from `jpackage.app-path`;
  an absent property or directory gives an empty catalog and no message.
- Layout: `library-manifest.json` and `tools/<command>.ggt` only.
- Integrity is whole-catalog fail-closed: an extra, missing or mismatching file,
  manifest field, hash or definition digest hides every bundled entry and shows
  `UserTools.BundledUnavailable` once.
- Bundled entries pass the same validation as a user install, appear after the
  user packages in the manager and the `user-tools` menu with their owned icon,
  cannot be removed and do not take custom icons.
- Hover tips (R1, author UX rule of 2026-10-07): each bundled tool's short tip
  (`UserTools.BundledTip.<command>`, English and Spanish profile texts) briefly
  says what the tool constructs or computes and, where useful, its principal
  input; it is never generic (no "GeoCeDG tool", "Utility tool" or "Planar
  convenience"). Validity-domain and CeDG semantic caveats are extended help
  (`UserTools.BundledNote.<command>` and §3), not hover text. Tool names, macro
  identity and macro definitions are unchanged by the tips.
- Precedence: a stored user package with a case-insensitively colliding command
  wins (`UserTools.BundledShadowed`); a document macro wins over both (existing
  adoption, `DefinitionMismatch` and `DocumentConflict` rules).
- No first-run seed; the installation directory is never written.

## 7. Bundled pins

`<preferences>.bundled-tools-v1.json` beside the user store:

```json
{"version": 1, "libraryId": "geocedg.curated-ggt-library",
 "pinned": [{"command": "...", "group": "...", "order": 0}]}
```

It is written atomically under the user-store lock, only by an explicit pin,
unpin, group or move action that changes a bundled pin; never at startup.
Entries for commands the current catalog does not offer are kept verbatim. An
unreadable sidecar disables bundled pinning only. The version-3 user store is
unchanged and is never written or created by a bundled-only action.

## 8. Serialization

No document-format change. Activating or using a bundled tool writes its
`<macro>` into `geogebra_macro.xml` as for a user-library tool, with the owned
`iconFile` string and without icon bytes. A later library version never
rewrites a saved document.

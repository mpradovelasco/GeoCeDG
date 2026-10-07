# Curated GGT library: rights and provenance record

- Record version: **1**
- Recorded: 2026-10-07, from the author's instruction of 2026-10-07 that
  resolved `DQ-E1-1` to `DQ-E1-16` of `PRE-G9B-R6-plus-E1`
  ([decision and authorization record](../validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md))
- Machine-readable mirror:
  [`curated-ggt-library-rights-record.json`](../../geocedg/validation/pre-g9b-r6-plus/curated-ggt-library-rights-record.json)
- Character: a repository rights and provenance record of an author
  declaration. It is **not legal advice**, makes no legal conclusion and grants
  nothing beyond what the author declared. A later change of any declaration
  below needs a new record version.

## Author declaration

The author's instruction states, for the twelve selected macro definitions
listed below:

```text
author / sole rights holder of the selected macro definitions:
  Manuel Prado-Velasco

modification into GeoCeDG curated derivatives:  AUTHORIZED
inclusion in GeoCeDG:                           AUTHORIZED
PROFILE INTERNAL inclusion:                     AUTHORIZED
PROFILE NC redistribution:                      AUTHORIZED
PROFILE COMMERCIAL:                             NOT AUTHORIZED / PENDING
```

The declaration applies to the twelve macro definitions only. It does **not**
grant rights over GeoGebra stock icons, embedded third-party UI artwork,
GeoGebra software, or any other content of `Templatev7.ggb` (its other twelve
macros, its JavaScript, its defaults, its thumbnail and its sixteen images).
None of those materials may be copied into the curated library unless they are
independently authorized.

## Licensing classes (existing repository classes)

| Material | Licensing class (`geocedg/resources/assets-manifest.yml`) | License |
|---|---|---|
| Curated GeoCeDG GGT definitions (the twelve derived macro definitions) | `geocedg-software` | EUPL-1.2 |
| New GeoCeDG-owned GGT-library tool icons (SVG sources and their PNG derivatives) | `geocedg-documentation-or-ordinary-art` | CC-BY-4.0 |

No new licensing class is created.

## Selected macro definitions

Provenance source: `models/legacy/template-v7/original/Templatev7.ggb`, SHA-256
`f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`, immutable;
its `geogebra_macro.xml` has SHA-256
`f3744e131a627ba5e9bef092427930e6e7c1526a6d527b43c3b4f30e04fc94f4`. The
"source macro" hash is the SHA-256 of the UTF-8 bytes of the exact
`<macro …>…</macro>` element in that file. The 2024 archives are the author's
standalone exports, kept outside the repository and recorded as provenance
evidence only; their equivalence with the template was proven by the E1
preparation (inputs and outputs equal; constructions equal once `eqnStyle`
tags are ignored).

| Command | Source macro SHA-256 | 2024 archive (name, bytes, SHA-256) | Writer | Archive date |
|---|---|---|---|---|
| `SquarebyDiagonal` | `1f01fd91b942dc268813499a3b0b0f15364005b3ff62403eba1dc43861513269` | `squarebydiagonal.ggt`, 1 400, `29e2a3d88d926f75f9b7fc0e24290340a10f092da7e161dc19c3a83129e9470f` | 5.2.820.0 | 2024-02-05 |
| `CirclebyD` | `888db63faa586cc89a7f6fc54d1de11690a3cbd57f08f1ee5c8696e20cb05825` | `circlebyD.ggt`, 2 143, `977d812d55d8a151f7ed8caa2983d0960daab72f7fc3010f5cccfe4b15bd76f8` | 5.2.820.0 | 2024-02-05 |
| `circArcbyAngle` | `6c788b66cafe9554f84cad18f976222bfa071efbda01575256e1136768ac84e6` | `circarcbyangle.ggt`, 2 308, `640f0906749b2396c676e0b3ed1f7324599ef1ec3302704784f225ed22fa5348` | 5.2.827.0 | 2024-03-06 |
| `ellipseLength12` | `1195f89860881fc48cd481dcac75e912573ded8403d4c968db17dc0a79ede4fe` | `ellipsepartiallength.ggt`, 2 495, `35c20a5d35d7bce362d36f2ef10f8db4d1b719e1254566234268f20cd16eb293` | 5.2.832.0 | 2024-04-02 |
| `IFPositiveSelectPoint` | `3d6d2b10ba018ee0a488c1d6fa2a5a2fb466165d16b78d5cf3ea8f3d16eaef96` | `IFpositivethenPoint.ggt`, 1 263, `c490e221a90e2acaeeaad3575ba172acc6cf0e82fcb765560bcafeb9cb7beb27` | 5.2.836.0 | 2024-04-22 |
| `EllipseAxis` | `d00f2527877516f7c8858c6750ae284bbe92e39f402e5d1380694a74cccdd481` | `ellipsebyaxis.ggt`, 1 490, `01437a90761366b619178cde37a3ebb88bc7ac51b6de0b04e3369d38ac77040b` | 5.2.820.0 | 2024-02-05 |
| `conj2mainAxesEllipse` | `01e3f585aa78cf0d9780ce8e922f9889e92998b6cdab61833a8de5cacb643c87` | `ellipseaxisbyconjaxis.ggt`, 3 747, `7639c82aa2aad8b470f9f6ab9130e419c3e321f33f8d1ccc672326acf6c40539` | 5.2.829.0 | 2024-03-11 |
| `pointJump` | `ee2ab641ee97d8bb712069048b5e788df253eeeb527c7ab1386b42df6fcb7765` | `pointjump.ggt`, 988, `a350031eb98be790627dd15294550a07638d0b7968c64e783695976ef8ba76f1` | 5.2.820.0 | 2024-02-04 |
| `relCoor` | `744b49c8cd9169c294e40e3dc5175815ebd7ceb2b5c922cdda1d1a0916d579ea` | `relcoor.ggt`, 2 327, `95ff380d56bb70fd07457b06b977fb27d03de22bfc0dfdd4850ea4bed8b7a0b2` | 5.2.826.0 | 2024-02-26 |
| `translationCoor` | `304d0d87992de461456ab11c70252a45fba9b4b9cb10606fbcb570cabdef20ea` | `translationcoor.ggt`, 2 553, `ef05762d7e1847185374d737d929f868616e6c8a8d270483bd4b9c91ed47b42b` | 5.2.829.0 | 2024-03-05 |
| `DuctSymbol` | `acf782297f721c02a0ffe5eee45bfe23e2039a7dfcc29c0d47ac43a8fb853dff` | `ductsymbol.ggt`, 3 000, `a2c1faa18d1727dd59bf590f998c360bb66d473174c605f4e127c96f5ea42890` | 5.2.827.0 | 2024-02-28 |
| `SymmSymbol` | `0289320da131fdd040384d13887ae0d99483548607020265d5acb3e0b9b2a236` | `symmsymbol.ggt`, 2 219, `491628c089749cd99b5759b0ed9259e530284529bedb3d2f8dc33d44b01fa74e` | 5.2.827.0 | 2024-02-28 |

For every row: rights holder Manuel Prado-Velasco; modification into curated
derivatives authorized; inclusion in GeoCeDG authorized; profiles `INTERNAL`
and `NC` authorized, `COMMERCIAL` not authorized / pending; licensing class
`geocedg-software` (EUPL-1.2). The curated derivative's own SHA-256 and
user-library definition digest are generated evidence recorded in
`models/curated/ggt-library/library-manifest.json` by `E1-L`; this record does
not repeat them.

## Exclusions

- `directDimension`, `axisDimension`, `sheetISOAnLand` and `sheetISOAnVert` are
  not selected: no curated derivative, no inclusion, no redistribution under
  this record (they stay behavioral references for `E2` and `E3`).
- The eight other `Templatev7.ggb` macros are not selected.
- Every image carried by `Templatev7.ggb` or by the 2024 archives (including
  the twelve stock-named icons and `circulodiametro.png`) is excluded; curated
  definitions reference only GeoCeDG-owned icons.
- `geogebra_javascript.js`, `geogebra.xml`, the defaults and the thumbnail of
  `Templatev7.ggb` are excluded.
- The template's own manifest stays `review_status: "blocked"`; this record
  does not change it, because the template as a whole still contains material
  the declaration does not cover.

## Use by later phases

`E1-L` consumes this record for the curated definitions and the owned icons.
`E1-P` (not authorized) is planned to consume it as the "approved rights
disposition" of its packaging admission contract. A future GeoCeDG About
surface (`ENH-R6PLUS-G-ABOUT-PRODUCT-METADATA`, owner `G`) may reference it as
part of the versioned GeoCeDG licensing information.

# ADR 0033 — Curated GGT library and its distribution boundary

- Status: **PROPOSED — APPROVAL ONLY THROUGH THE `PRE-G9B-R6-plus-E1-L` AUTHOR CLOSEOUT**
- Date: 2026-10-07
- Phase: `PRE-G9B-R6-plus-E1-L`
- Author decisions: [E1 author decisions and E1-L authorization record](../validation/pre_g9b_r6_plus_e1_prompt_closeout_record.md)
- Specification: [`geocedg/specs/legacy/curated-ggt-library.md`](../../geocedg/specs/legacy/curated-ggt-library.md)

## Context

The author wants a curated library of selected legacy `.ggt` tools, with
provenance, GeoCeDG-owned icons and eventual installer integration. The
definitions already exist in the immutable G3 package
`models/legacy/template-v7/`; the author's 2024 standalone exports are
construction-equivalent. `AGENTS.md` §3.2 placed `.ggt` content under
`models/` but named only canonical, research, legacy and regression categories;
the Windows packaging contract forbids `.ggt` content in the package; the user
library installs only user-chosen files.

## Decision

1. A new `models/curated/` category (author-authorized `AGENTS.md` §3.2
   amendment): curated product artifacts generated deterministically from
   immutable legacy originals plus recorded curation metadata, with a manifest
   and a provenance and rights record, never hand-edited.
2. The library is generated from `Templatev7.ggb`; the 2024 archives are
   provenance evidence only and are not ingested.
3. A closed transformation set (`T-EXTRACT`, `T-ICON`, `T-STYLE`) and owned SVG
   icons rasterized deterministically in the Desktop test JVM.
4. A read-only, installation-relative bundled catalog, whole-catalog
   fail-closed, with user and document precedence and user-side pins in a
   separate sidecar.
5. `E1` is split: `E1-L` delivers the library and catalog without any packaging
   change; `E1-P` alone may stage the library into `app/ggt-library/` under an
   amended, still fail-closed packaging contract and the rights record.

## Rejected alternatives

| Alternative | Reason |
|---|---|
| `tools/ggtfiles/` | `tools/` holds tooling, not authoritative product content |
| `models/legacy/template-v7/derived/` | the legacy pins admit `.ggt` under `models/legacy` only inside `original/`; `derived/` is generated inventory |
| ingesting the 2024 archives as a legacy package | duplicates bytes already in the template and changes the `verify-legacy.ps1` pins |
| `geocedg/resources/ggt-library/` | puts construction content in the resource category |
| `models/canonical/` | gives convenience tools the authority rank of canonical CeDG models |
| first-run seed of the user store | mutates user data, cannot be upgraded with the product, collides with the install-conflict rule |
| classpath resources in `desktop.jar` | ships the definitions in every build before the rights gate and evades the packaging check |
| remembered style hook | one remembered style erases role distinctions; the author chose black, `lineThickness` 3 |
| a PowerShell/.NET rasterizer | no SVG renderer exists in that toolchain; the product already ships EchoSVG |

## Consequences

- Shipping the library needs `E1-P`; until then installed products show an
  empty catalog.
- `PRE-G9B-R6-plus-E2` must dispose of the case-insensitive collision between
  the legacy `directDimension`/`axisDimension` macros and the native
  `DirectDimension`/`AxisDimension` commands.
- Promotion of `cedg.library.curated-ggt` to `stable` belongs to `G` or a later
  author decision.

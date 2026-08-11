# Legacy CeDG resources

Each directory is a controlled, non-default package governed by
`geocedg/specs/legacy/controlled-integration.md`.

- `original/` contains the immutable source artifact.
- `manifest.yml` records provenance, compatibility, maturity, and rights.
- `curation.yml` records reviewable interpretation that cannot be inferred
  mechanically.
- `derived/` contains reproducible inventories and other generated evidence.

Run `tools/legacy/ingest.ps1 -Check` through the registered manifest before
using or updating a package. A resource being present here does not make it a
stable feature or a regression authority.

The two `inter-cil-cono-oblique*` packages are G6A scientific models rather
than tool containers. The author accepted `TwoLevels` as the functional control
and the `Flatten` model as the pathological third-level reference. Their deterministic inventories record container facts;
their semantic/performance interpretation lives in the G6A specification,
validation baseline and report. Both remain non-default and rights-blocked.

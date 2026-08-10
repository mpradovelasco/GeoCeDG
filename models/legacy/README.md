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

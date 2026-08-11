# GeoCeDG model repository

Models are durable validation or research inputs, not generated evidence.
Every future model or tool package requires a versioned manifest with
provenance, validity, degeneration, metric, compatibility, and license data.

G3 introduces the first controlled legacy package under `models/legacy/`.
Original artifacts remain immutable, are identified by SHA-256, and are kept
separate from curated metadata and deterministic derived inventories. Presence
in this repository does not promote a resource beyond its declared maturity.

G6A adds the author-supplied `InterCilConoOblique` pair as controlled legacy
scientific/performance evidence: `TwoLevels` is the accepted functional
two-level control and the original `Flatten` model is the accepted pathological
third-level reference. These models are not loaded by default, are not V2
semantic authority, and remain blocked for public redistribution.

Use `tools/legacy/ingest.ps1` to import or check a package and
`tools/legacy/open-laboratory.ps1` for explicit, non-default loading. Remote
model corpora are metadata sources, not build dependencies.

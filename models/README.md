# GeoCeDG model repository

Models are durable validation or research inputs, not generated evidence.
Every future model or tool package requires a versioned manifest with
provenance, validity, degeneration, metric, compatibility, and license data.

G3 introduces the first controlled legacy package under `models/legacy/`.
Original artifacts remain immutable, are identified by SHA-256, and are kept
separate from curated metadata and deterministic derived inventories. Presence
in this repository does not promote a resource beyond its declared maturity.

Use `tools/legacy/ingest.ps1` to import or check a package and
`tools/legacy/open-laboratory.ps1` for explicit, non-default loading. Remote
model corpora are metadata sources, not build dependencies.

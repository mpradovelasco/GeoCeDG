# Generated GeoCeDG artifacts

This directory is reserved for generated verification, benchmark, regression,
packaging, and release evidence. Its contents are derived artifacts, never
source authority, and are ignored by Git except for this contract.

Every durable report must identify the command, source revision, environment,
and relevant manifest/specification that produced an artifact. Do not place
model sources, specifications, accepted baselines, or license evidence here.

Deterministic source/knowledge bundle instances belong only below
`artifacts/knowledge/`. Their checked manifest and archive are generated,
ignored evidence; the generator, schemas, profiles and tests remain in tracked
source paths. A book-oriented export may add the bridge-owned deterministic
`book-evidence-export.v1.json` sidecar that records its disposition and binds
the referenced G9O1 manifest/archive hashes without changing bundle authority.

Book technical-baseline candidates belong only below `artifacts/book/`. They
are deterministic product-side review evidence and never constitute an
editorially accepted baseline or a BOOK phase decision.

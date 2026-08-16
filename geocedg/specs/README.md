# GeoCeDG specifications

This directory contains durable GeoCeDG contracts. A specification owns
behavioral meaning; prompts and verification scripts reference it and must not
restate its geometric rules.

Current author-approved contracts include the application profile, controlled
legacy integration, Windows packaging, neutral 2D export, and the internal
Locus V2 semantics/metrics/intersections closed through G8. G9P-R1 refined the
spatial, public-surface, workspace, extended-DXF, documentation, and bundle
specifications without implementing them. Those six G9P specifications are
`NORMATIVE / AUTHOR APPROVED`; implementation authorization remains an
independent phase status. G9O1 alone is authorized and not started.

Every specification must state its status, version, authority, scope,
invariants, compatibility policy, validation evidence, and stop conditions.
Use `templates/specification-template.md` as the starting structure.

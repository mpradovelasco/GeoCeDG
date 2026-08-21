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
independent phase status. G9O1, G9A1–G9A3/the G9A track, G9U0, G9U0-R1 and
G9X1 are now `PASS — AUTHOR APPROVED` within their recorded scopes. G9U1, G9B
and G9C remain designed but not authorized; G9U2 remains blocked and productive
G10 remains unauthorized.

The G9U0-R2 contracts
`locus/locus-v2-presentation.md` and
`ui/native-document-identity.md` are **NORMATIVE / AUTHOR APPROVED** planning
authorities. G9U0-R2 implementation remains unauthorized/not started, so these
contracts must not be treated as changing the current `.ggb` product behavior
or current Locus presentation.

Every specification must state its status, version, authority, scope,
invariants, compatibility policy, validation evidence, and stop conditions.
Use `templates/specification-template.md` as the starting structure.

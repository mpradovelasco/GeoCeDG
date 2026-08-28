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
independent phase status. G9O1, G9A1–G9A3/the G9A track, G9U0, G9U0-R1,
G9X1, G9U0-R2 and G9U0-R3 are `PASS — AUTHOR APPROVED` within their recorded scopes. G9U1, G9B
and G9C remain designed but not authorized; G9U2 remains blocked and productive
G10 remains unauthorized.

`locus/locus-v2-public-ui-exposure.md` is the bounded, author-approved G9U0-R3
implementation contract. R3 is `PASS — AUTHOR APPROVED`; its retained smoke
chronology includes the long-token width defect, bounded correction and passing
re-smoke. This additive authority leaves the hash-frozen G9U0 public-surface
spec unchanged and assigns graphical candidate markers prospectively to G9U1.

The G9U0-R2 contracts
`locus/locus-v2-presentation.md` and
`ui/native-document-identity.md` are **NORMATIVE / AUTHOR APPROVED** authorities.
Their implementation is `PASS — AUTHOR APPROVED`. The original R2-L11 smoke
failure remains historical evidence; the bounded derived-render correction,
automated authority and interactive author re-smoke all pass. `.cedg` is now the
native GeoCeDG document identity, while `.ggb` remains compatibility input and
the existing ZIP/XML plus `app="classic"` internals remain unchanged.

Every specification must state its status, version, authority, scope,
invariants, compatibility policy, validation evidence, and stop conditions.
Use `templates/specification-template.md` as the starting structure.

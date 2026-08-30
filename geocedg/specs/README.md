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
G9X1, G9U0-R2 and G9U0-R3 are `PASS — AUTHOR APPROVED` within their
recorded scopes. G9U0-R4 is an `IMPLEMENTATION CANDIDATE — PENDING AUTHOR
RE-REVIEW`. Its first candidate removed the local-admissibility deadlock, but
the author smoke then found that a materialized point became undefined during
regular motion. The corrective candidate records the author-approved priority
`deterministic semantic selection > continuity heuristic`: each recomputation
resolves an exact ledger token from a unique intrinsic current-snapshot
selector, independent of the previous parameter, Cartesian position, output
order or movement history. Continuity is expected while that selector remains
uniquely valid; topology ambiguity remains fail-closed. Unresolved candidates
block only solutions on their own semantic component, equivalent homogeneous or
projective target representations cannot change a deterministic binding, and a
ray's direction remains semantic evidence rather than being normalized away.
The second author smoke then exposed a separate boundary: the exact four-root
fixture publishes four finite, transverse, locally isolated roots, but the
component-plus-oriented-germ selector repeats in two pairs. The author
subsequently authorized the narrow intrinsic semantic phase/rank refinement in
[ADR 0017](../../docs/adr/0017-deterministic-intersection-phase-rank-identity.md).
Only repeated base-selector groups are ordered by pairwise-disjoint canonical
root intervals in the explicit oriented domain; solver/list/UI/coordinate
order remains non-authoritative. Orientation, domain kind, verified group
cardinality and intrinsic rank form one versioned selector context. Topology,
cardinality, orientation and periodic-seam ambiguity invalidate rather than
retarget old tokens. This fixes identity collision without weakening numerical
evidence or global completeness. The current focused authority comprises 25
public-kernel, 23 ledger and 2 Desktop methods (50 total). A replacement
composed run exposed the missing
localized value for `DETERMINISTIC_SELECTION_ESTABLISHED`; the bounded
base/English/Spanish bundle addition changes no semantic contract. The final
candidate inventory is 51 paths, 29 under `source/`; it does not self-approve
or erase either failed smoke. The four-root case now has four exact selectors
and tokens under the existing local-evidence contract; full replacement
validation and author re-review remain mandatory.
`locus/locus-v2-similarity-transformations.md` is the G9U0-R5 **DRAFT /
NORMATIVE CANDIDATE**; R5 implementation is not authorized or started. G9U1,
G9B and G9C remain designed but not authorized; G9U1 is blocked until R4 and R5
each close `PASS — AUTHOR APPROVED` and its future GeoCeDG product contract
requires the host Continuity setting to remain off while Classic stays
configurable. G9U2 remains blocked and productive G10 remains unauthorized.

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

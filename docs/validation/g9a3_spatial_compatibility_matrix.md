# G9A3 spatial lifecycle compatibility matrix

**Status:** IMPLEMENTATION DESIGN EVIDENCE — CANDIDATE IN FLIGHT

**Author approved:** `false`

This matrix applies the approved G9 identity/persistence contract to the G9A3
lifecycle and migration boundary. It is not a public migration workflow and it
does not authorize G9B, G9C, G9U, DXF or productive G10 work.

## Runtime boundary

`AppConfigGeoCeDG` and `AppConfigDefault` are two application configurations of
the GeoCeDG fork. The latter is the GeoCeDG Classic diagnostic process; it is
not an external upstream GeoGebra distribution. Both use the same shared kernel
and therefore must preserve supported native spatial records exactly.

An external upstream distribution that does not implement the GeoCeDG spatial
extension is a separate unsupported-open boundary. G9A3 records that boundary
truthfully and adds no label, list, coordinate or geometry downgrade.

| Input/state | GeoCeDG profile | GeoCeDG Classic diagnostic | External upstream |
|---|---|---|---|
| ordinary legacy file, no spatial section | load normally; remain unassociated | load normally; remain unassociated | ordinary upstream behavior; no GeoCeDG association claim |
| supported native POINT-v2 graph | parse, resolve, recompute, save and reopen exact IDs/relations | same shared-kernel behavior; public creation remains absent | unsupported-open; preservation and resave are not guaranteed |
| supported graph with invalid current geometry | preserve IDs and explicit failure state; no payload | same | unsupported-open |
| shuffled but complete forward-reference graph | two-stage deterministic resolution; XML order has no authority | same | unsupported-open |
| missing typed reference | explicit broken/undefined result or atomic native-load rejection; no repair | same | unsupported-open |
| duplicate native durable ID | reject atomically; never remap | same | unsupported-open |
| explicit import/paste collision | remap the entire admitted imported closure | same internal kernel policy | not a supported interchange route |
| malformed ID/kind/role/family/schema | reject before publication | same | no GeoCeDG claim |
| unknown outer or record version | fail atomically and truthfully; no opaque or lossy fallback | same | no GeoCeDG claim |
| ordinary geometry after spatial section is absent | load as legacy/unassociated; no inference | same | ordinary upstream behavior only |
| feature disabled after native creation | preserve, recompute, save and reopen existing data; creation remains disabled | same | unsupported-open |

## Explicit association and recovery

| Request | Decision | Required evidence |
|---|---|---|
| no request, regardless of labels/proximity/coordinates/order/layer | no association | all inference counters remain zero |
| complete typed POINT plan with caller-selected live `GeoElement` targets | atomic association | fresh durable IDs, complete closure, explicit-association provenance, normal-DAG publication |
| incomplete, ambiguous, mixed-construction, stale or deleted target | reject | exact pre-operation graph and payload snapshot |
| repeated already-applied plan | explicit already-associated no-op or typed rejection | no duplicate ID, no revision drift |
| exact missing durable target supplied to a broken graph | typed recovery transaction | affected resolution states and normal-DAG dependents update deterministically |
| coincident or same-label replacement supplied without the missing ID | no recovery | broken references remain broken |

Caller-supplied `GeoElement` objects are explicit selection handles, not
continuity evidence. The migration service must not scan a construction or
expose lookup by label, coordinate, proximity, construction/XML order, output
ordinal, layer or viewport state.

## Copy and redefine compatibility

| Operation | Identity result |
|---|---|
| complete clipboard/duplicate/macro/cross-document closure | fresh deterministic remap of every owned typed ID |
| same-construction partial copy with exact typed external allow-list | fresh owned IDs; only declared active system/map identities remain shared |
| undeclared external or any cross-construction live reference | atomic reject |
| explicit semantic no-op redefine | `RETAIN`; neither definition nor topology revision changes |
| compatible definition change | `RETAIN`; definition revision advances once |
| provider-admitted topology change | `RETAIN`; definition and topology revisions advance once |
| true replacement | `FRESH`; old meaning is retired or invalidated explicitly |
| incompatible or unsupported topology change | `REJECT`; exact rollback |
| provider-owned multi-output group with unique stable roles and exact cardinality | retain by role, never by host output ordinal |
| missing/duplicate roles, partial siblings, ambiguity or cardinality change | atomic reject; cardinality-changing replacement/merge/split genealogy is deferred and forbidden in G9A3 |

## Evidence classification

The exact corpus and provenance classifications are recorded in
`docs/validation/g9a3_spatial_compatibility_corpus.json`. The synthetic
external-loss-shape fixture is deliberately not described as output from an
external runtime. Until an exact external binary is executed and its products
are preserved with hashes, external behavior remains a static unsupported-open
characterization rather than a runtime result.

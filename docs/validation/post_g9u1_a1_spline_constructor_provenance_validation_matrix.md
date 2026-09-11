# POST-G9U1-A1 SplineV2 constructor-provenance validation matrix

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Product phase: `POST-G9U1-A1`
- Planned acceptance: canonical `PHASE -Phase POST-G9U1-A1`
- Bootstrap impact: **NONE** — no prerequisite, toolchain, environment or
  workstation assumption changes
- Guide impact: **UPDATED**
- Focused test:
  `org.geocedg.common.locus.PostG9U1A1SplineConstructorProvenanceTest`

This matrix traces only the bounded A1 endpoint-provenance contract. It does
not authorize A2-A5, A7, G9B, G9C or G9U2 and does not reinterpret G7, G9S1,
R5, R6, A6 or G9U1 evidence.

| Obligation | Candidate evidence |
|---|---|
| Existing explicit semantic endpoints unchanged | explicit-address and A6 points remain valid metric inputs |
| Unique constructor occurrence | ordinary source point resolves to one typed occurrence/address |
| `Length(S,A,C)` positive path | scalar uses the existing rich parent and publishes the expected guarded value |
| Rich `LocusLength(S,A,C)` authority | rich result succeeds and agrees bit-for-bit with explicit semantic endpoints |
| Same point used multiple times | result is `MULTIPLE`; rich metric is explicit `INVALID_QUERY` |
| Distinct coincident points | distinct durable IDs and slots produce distinct occurrence keys/addresses |
| Arbitrary Cartesian coincidence | `NO_ADDRESS`; no projection or nearest-preimage fallback |
| Self-intersection/coincident image | only declared point occurrences are returned; image crossings add none |
| Interior knot ownership | one constructor slot produces one knot address, independent of adjacent spans |
| Periodic seam | endpoint occurrences retain canonical parameter, lift and lower/upper seam evidence |
| Invalid/undefined source | `UNRESOLVED_INVALID`; downstream rich value is absent |
| Same-provenance recovery | coordinate degeneration fails closed and recovery restores the same occurrence key |
| Moving source inputs | ordinary recomputation refreshes knots through the same ordered occurrence relation |
| Topology change | computed/unordered list families are unresolved; existing G9S1-R1 lifecycle tests govern rebuild/replacement |
| Save/reopen | command XML rebuilds the same key from durable DAG inputs; no occurrence cache is serialized |
| Undo/redo and rename | result, source IDs and occurrence relation remain current without label authority |
| Copy/remap | copied closure receives new source/input IDs and a new internally consistent occurrence key |
| Malformed/incompatible provenance | no serialized A1 token exists to repair; unsupported constructor/list lineage fails closed |
| R5 covariance | supported similarity image maps the address to the transformed source ID without coordinate comparison |
| Unsupported source/transform | `NO_ADDRESS`/explicit invalid metric; no generic transform framework |
| Determinism | repeated focused execution and exact PHASE inventory use the same declared test identities |
| No viewport/render authority | productive resolver reads only DAG algorithms, durable IDs, spline knots and semantic domain state |

The A1 `PHASE` perimeter also retains the directly affected G9S1-R1 spline
lifecycle suite, R5 similarity suite, R6 semantic-point suite and A6 optional-
branch suite. Those accepted suites remain regression evidence; this matrix
does not rewrite their historical status.

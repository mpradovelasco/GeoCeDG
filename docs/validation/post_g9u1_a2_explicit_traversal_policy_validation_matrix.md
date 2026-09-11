# POST-G9U1-A2 explicit traversal-policy validation matrix

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Product phase: `POST-G9U1-A2`
- Planned acceptance: canonical `PHASE -Phase POST-G9U1-A2`
- Bootstrap impact: **NONE** — no prerequisite, toolchain, environment or
  workstation assumption changes
- Guide impact: **UPDATED**
- Focused test:
  `org.geocedg.common.locus.PostG9U1A2ExplicitTraversalPolicyTest`

This matrix traces only the bounded A2 public traversal-policy contract. It
does not authorize signed length, `shortest`, A3-A5, A7, G9B, G9C or G9U2 and
does not reinterpret G7, A1 or earlier Locus V2 evidence.

| Obligation | Candidate evidence |
|---|---|
| Existing rich default | `LocusLength(L,A,B)` remains `forward/strict/zero-length` |
| Existing scalar default | `Length(L,A,B)` is unchanged, guarded and non-negative |
| Additive public syntax | six-argument rich overload consumes three stable text tokens |
| Public/internal separation | parser maps public lowercase tokens to existing route policies without exposing enum names or ordinals |
| Forward/reverse route | explicit directions select different ordered routes while both magnitudes remain non-negative |
| Strict unreachable | absent rich value and explicit `TARGET_NOT_REACHABLE` route evidence |
| Stop at boundary | partial value, incomplete coverage and `targetReached=false` |
| Wrap convention | two boundary-side contributions, no connector and `geometricallyConnected=false` |
| Internal gap | no policy, including wrap, crosses a disconnected valid-domain component |
| Periodic seam | direction traverses the declared oriented fundamental domain using semantic seam authority |
| Same-position zero | zero value with no route segments |
| Same-position cycle | one full cycle only on one approved periodic component; rejected on open input |
| Total length | total rich/scalar behavior remains independent of between-position policy |
| Rich evidence | result retains direction, ordered segments, boundary, wrap, reachability, connectivity, status and outcome |
| A1 endpoint consumption | unique SplineV2 constructor occurrences work under explicit traversal; repeated occurrence remains ambiguous |
| Explicit semantic endpoints | addressed/A6-compatible points keep the existing endpoint authority |
| No new inference | no coordinate, proximity, label, branch-order, render or solution-index authority |
| Malformed token | explicit current `INVALID_QUERY`; no fallback to a default policy |
| Dynamic policy | ordinary GeoText input change recomputes the same metric construction |
| Save/reopen and undo/redo | command DAG reconstructs the policy and durable metric identity |
| Copy/remap | copied closure has remapped durable inputs and reconstructs all six arguments |
| Rename | labels are persistence handles, not route authority |
| Determinism | repeated evaluation retains the same route and magnitude for fixed semantic inputs |

The A2 `PHASE` perimeter also retains the existing productive route-engine
tests, G9U0 metric/command tests and A1 endpoint-provenance suite. Those suites
remain regression evidence; this matrix does not rewrite their historical
status.

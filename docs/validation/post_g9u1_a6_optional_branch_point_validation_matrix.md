# POST-G9U1-A6 optional-branch Point validation matrix

- Status: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Product phase: `POST-G9U1-A6`
- Planned acceptance: canonical `PHASE -Phase POST-G9U1-A6`
- Focused test: `org.geocedg.common.locus.PostG9U1A6OptionalBranchPointTest`

This matrix traces the bounded A6 contract. It does not reinterpret G9U0-R6
evidence or authorize A1–A5, A7, G9B, G9C or G9U2.

| Obligation | Candidate evidence |
|---|---|
| Existing `Point(L,"branch",u)` unchanged | explicit and omitted forms evaluated together |
| Unique eligible branch/component | successful scalar Locus V2 command construction |
| Zero eligible component | out-of-domain and gap commands reject explicitly |
| Multiple eligible components/shared endpoint | shared-endpoint command rejects explicitly |
| Multiple branches | both branch enumeration orders reject during recomputation |
| Enumeration-order independence | reversed two-branch snapshots remain undefined |
| No label/coordinate/proximity authority | predicate consumes only definition, branch/component and provider state |
| Unique-to-ambiguous transition | existing point becomes undefined and retains its address |
| No retarget to another singleton | different sole branch remains undefined |
| Same-selector recovery | original branch/component restoration reactivates the point |
| Multiple components/gaps | disconnected-domain and shared-endpoint cases |
| Periodic seam/lift | periodic circle retains canonical address and lift |
| `SplineV2` | omitted form materializes a semantic spline point |
| Supported transformed source | translated `SplineV2` retains transformed source identity |
| Save/reopen | selector and durable point identity survive native XML reload |
| Undo/redo | point identity and selector survive reconstruction |
| Copy/remap | copy gets a new source identity and its address references that source |
| Rename/recompute | labels change without changing durable point identity |
| Malformed persisted state | versioned selector fails closed |
| Deterministic repetition | focused class is included in the exact candidate `PHASE` perimeter |

The `GeoLocusV2` object remains non-`Path`; no viewport, render sample,
coordinate coincidence or nearest-point operation participates in these cases.

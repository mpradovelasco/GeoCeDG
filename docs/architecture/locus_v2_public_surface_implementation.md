# G9U0 Locus V2 public-surface implementation record

- Status: **G9U0 = PASS — AUTHOR APPROVED**
- Phase: G9U0
- Entry: `d4de0e480b0a6439c940a0f6e0cfde51c5e56bd2`
- Normative contract: `../../geocedg/specs/locus/locus-v2-public-surface.md`
- Accepted decision: `../adr/0013-public-locus-v2-surface-and-token-selection.md`

## Purpose

This record captures the author-approved experimental public API, dependency
placement, persistence shape and verification boundary for G9U0. Productive
source, tools and 93 mapped test methods exist, and the exact 114-path candidate
inventory is frozen. Focused and deterministic execution each passed 93/93,
the complete composed authority passed, and the author approved G9U0 as PASS.

Legacy `Locus`, mode 47, old locus XML and legacy `Length[GeoLocus]` remain
separate compatibility contracts. Locus V2 remains experimental, default-off
and unavailable for interactive creation in the GeoCeDG Classic diagnostic
path.

## Working public API selection

The author-approved G9U0 selection follows existing typed-overload practice
and keeps the experimental semantic boundary visible:

```text
LocusV2[Q, P]
LocusV2[Q, s, D]
LocusV2[Q, t, s, D]
Point[L, branchKey, canonicalParameter]
LocusLength[L]
LocusLength[L, A, B]
Length[L]
Intersect[L, T]
Intersect[L1, L2]
Intersect[R, opaqueToken]
```

`P` is an already constrained point on one registered support. In the scalar
identity form `s` is both state and true coordinate. In the mapped form `t` is
the dependent state, while `s` is the only true coordinate and is varied only
inside the isolated reconstructible evaluation transaction. `D` has the strict
shape `{periodic,{a,b,includeA,includeB},...}`; ordered interval endpoints
encode orientation. No form infers a free ancestor.

This spelling is implemented in the command dispatcher, localization and
reconstructible parent algorithms. G9U0 author approval accepts this selection
without promoting it to a stable or default-on API.

## Layer placement

The shared kernel owns:

- typed one-dimensional generator descriptors and provider versions;
- reconstructible dependent-construction evaluation;
- durable locus, generator, preimage and solution-token identities;
- normal construction-DAG dependencies and cycle rejection;
- rich metric and intersection queries;
- guarded scalar and exact-token point children; and
- XML, copy, undo, delete and reopen behavior.

The GeoCeDG frontend owns only runtime availability, selection transactions,
localized help, accessible result inspection and candidate ranking. Screen
coordinates may rank already established tokens but never become serialized
identity.

## Durable identity seam

G9U0 uses the approved G9A lifecycle registry for ordinary construction geos.
`GeoIdentityRecord` explicitly supports `ProjectionBindingRole.NOT_APPLICABLE`
for construction-defined records, so a Locus/query/token child receives durable
identity without a fictitious spatial object or projection. Publication is an
atomic batch and copied closures are authorized only through immediate
`copySource` provenance. The focused lifecycle tests remain the executable
authority for copy/redefine/undo/reopen behavior.

## Evaluation and dependency rules

Every accepted generator normalizes to one typed mapping from one explicit
oriented domain. The dependent point, declared state, true driver, support and
all external mapping inputs are normal algorithm inputs. Evaluation-session
re-entry detection is defense in depth; it cannot accept a graph that ordinary
Construction dependency checks reject.

An evaluation transaction must restore live state after success, undefined
evaluation, exception and work-limit exit. It cannot assign a dependent `t`,
read slider visibility, discover an ancestor by label or mutate the visible
construction outside the reconstructible slice.

## Metrics and intersections

`LocusLength` owns the reconstructible rich metric query. Standard total
`Length[GeoLocusV2]` reuses that authority and publishes only when
`isScalarAdmissible()` succeeds. It performs no independent integration or
sample sum.

General `Intersect` keeps every non-V2 branch unchanged. A V2 operand selects
the existing G8 rich-result framework. `Intersect[R, opaqueToken]` stores an
exact opaque token and depends on `R`; it never invokes a solver or falls back
to list order, coordinates or proximity. Option B remains intact: a locally
admissible solution can feed a point even when global completeness is not
established.

The opaque token contains owner/query and semantic-lineage digest material plus
an incarnation, never coordinates or a parameter. A separate persistent ledger
stores the exact provider signature, target contract and canonical-parameter
bits used to justify cross-revision reuse. Identical address proof may retain an
incarnation while the evaluated Cartesian point moves; changed parameter,
merge/split, overlap or unsupported evidence burns it. The ledger is versioned
XML state, rejects noncanonical/tampered material and preflights incarnation
overflow before mutating its snapshot.

## Persistence and compatibility

The parent algorithms persist reconstructible inputs and policy, not computed
samples or revision-bound snapshots. A semantic point separates its durable
preimage address from its current revision/component binding. Copy remaps the
owned closure only through immediate `copySource` provenance for the rich
result, token text and point child. Copy-of-copy accepts its direct parent, not
the grandparent; undo restores operation identity, and reopen recomputes before
a number or point becomes current.

Feature-off loading is independent of interactive command filtering. GeoCeDG
Classic preserves native types and recomputes through the same shared kernel
while creation remains unavailable. External upstream products without the
extension remain an explicitly unsupported-open boundary; no legacy-locus,
list or coordinate downgrade is added. G9U0 did not execute an external
upstream runtime.

## Author-approved frozen source boundary

The G9U0 closeout boundary records:

1. the chosen overloads are implemented and localized;
2. exact-token address proofs, immediate-copy provenance and atomic lifecycle
   behavior are covered by the focused source;
3. the native XML grammar and hostile fixture injections are verified;
4. all 93 scenarios map one-to-one to executable tests;
5. the exact 114-path candidate inventory is recorded with no duplicate,
   missing or additional paths; and
6. focused and deterministic verification each passed 93/93, the complete
   composed authority passed, and the author approved G9U0 as PASS.

The surface remains experimental, GeoCeDG-only and default-off. No G9X1,
G9U1, G9B, G9C, G9U2 or G10 implementation was executed by this closeout.

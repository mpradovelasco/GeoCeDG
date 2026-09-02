# G9U0-R6 — Semantic Locus Point Interaction Support

**CURRENT AUTHOR-SUPPLIED EXECUTION AUTHORITY — IMPLEMENTATION CANDIDATE.**

This prompt governs only G9U0-R6. It does not claim PASS, execute G9U1, or
authorize frontend workspace implementation.

# Objective

Add one shared-kernel semantic interaction capability for ordinary points on:

- Locus V2;
- SplineV2;
- supported R5 similarity images of either source.

The existing forward authority is an exact semantic address:

```text
source + branch/component + canonical parameter -> Cartesian evaluation
```

R6 adds the bounded inverse interaction direction:

```text
semantic source + geometric interaction request
  -> zero / one / several evidenced semantic-address candidates
  -> explicit candidate selection
  -> ordinary normal-DAG semantic point
```

An existing interactive semantic point may update its explicit semantic
coordinate through the same resolver. Its click, pixel, render segment and
previous Cartesian location never become identity.

# Authority and evidence hierarchy

## Entry authority

Before mutation verify:

- clean published `main`, `origin/main` and direct remote main at
  `de33f3a80102adb051aaa7547a72b7e97409c58c`;
- annotated `geocedg-g9s1-pass` object
  `ece0ca6f00299d3347e57fac38b7a28cade28644` peeling to that commit;
- G9S1, G9U0-R5 and G9U0-R4 remain author-approved ancestors;
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open/tracked;
- no R6 implementation is already present; and
- implementation branch
  `feature/g9u0-r6-semantic-locus-point-interaction-support` starts exactly at
  the G9S1 commit.

The protected downstream planning authority is intentionally outside this
branch:

```text
branch: feature/g9u1-construction-workspace-planning
checkpoint: 857de6628489bda0b65a5ba5145e62ca0795fc32
prompt canonical-LF SHA-256:
2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322
```

Do not merge or cherry-pick that 17-path G9U1 candidate into R6. Record actual
R6 API deltas for a later reconciliation.

# Scope

R6 is limited to the shared-kernel inverse interaction capability, its ordinary
normal-DAG semantic point consumer, focused tests and the bounded operational
and documentation integration required to validate that capability.

# Required design/specification

## Governing design

Implement the candidates:

- [ADR 0019](../../../docs/adr/0019-semantic-locus-point-interaction-support.md);
- [R6 specification](../../../geocedg/specs/locus/locus-v2-point-interaction.md);
- [R6 architecture](../../../docs/architecture/g9u0_r6_semantic_locus_point_interaction_support.md);
- [validation matrix](../../../docs/validation/g9u0_r6_semantic_locus_point_interaction_validation_matrix.md).

If current source contradicts a required semantic decision, stop before
broadening the contract.

# Explicitly forbidden scope

The forbidden product and authority boundaries are enumerated together with the
placement contract below.

# Architectural placement

## Placement and forbidden scope

The inverse resolver, typed evidence, semantic-address edit state and normal
DAG point behavior belong in the shared Java kernel. Frontends may later supply
a selected source and transient world-space request; they do not solve or own
the resulting address.

Do not:

- make `GeoLocusV2` implement legacy `Path`;
- use render tessellation, pixels, viewport, DPI or screen state as truth;
- infer durable identity from coordinates, proximity, enumeration or history;
- add a public `ResolvePointOnLocus`-style command or a new feature flag;
- add the final Point-tool, workspace, marker or inspector implementation;
- change intersection, metric, R4 token-ledger or R5 transformation semantics;
- execute G9U1, G9B, G9C, G9U2 or productive G10.

## Typed inverse result

Return a typed current-snapshot result distinguishing at least:

- no admissible semantic preimage;
- one uniquely established candidate;
- several semantic preimages / explicit ambiguity;
- unresolved bounded numerical search;
- invalid or undefined source;
- degenerate image; and
- unsupported capability.

Each candidate preserves, where applicable, source durable identity, semantic
revision, branch, component, canonical semantic parameter, periodic
canonicalization, evaluated point, interaction residual, local regularity,
uniqueness/isolation evidence, numeric guarantee and diagnostics.

Residual and proximity are evidence about an interaction request. They are not
semantic identity.

## General evaluator strategy

Inspect provider domain, branch/component validity, canonicalization,
differential capability, semantic partitions, evaluator sessions and existing
instrumentation before selecting APIs.

For evaluator-only finite Locus V2 sources, use a deterministic bounded search
over semantic components, followed by evaluator-owned refinement and forward
verification. Adaptive subdivision, distance minimization and derivative-aware
stationary refinement are permitted. Render samples may at most be discarded
performance hints; every published candidate must be independently established
from semantic evaluation.

No unbounded global search may be hidden behind an interactive request.
Unbounded or insufficiently evidenced cases report `UNRESOLVED` or
`UNSUPPORTED` truthfully.

## SplineV2 specialization

Reuse G9S1 piecewise-polynomial authority. For a span `C(u)` and query point
`q`, examine endpoints and roots of the derivative of
`||C(u)-q||^2`. Use provider-owned span bounds, polynomial data, canonical knot
ownership and deterministic refinement. Equivalent adjacent-span knot
representations publish one candidate; distinct semantic preimages remain
distinct.

Do not reconstruct a spline from render vertices or generic expressions.

## Creation and drag state

One explicitly selected unique candidate creates one ordinary point whose
authoritative parents are the current semantic source and explicit semantic
address state. Choose the smallest normal-DAG representation supported by the
host after inspecting existing point and parameter mechanisms.

Dragging edits that semantic state through an explicit transaction. The same
`GeoPoint` and durable identity remain; source locus, renderer and hidden graph
structure are not recreated per event. Undo/redo restores the semantic address
exactly.

For an existing point, its current source, branch/component and semantic
address may constrain and seed the edit. The previous Cartesian position is
not identity. An ambiguous cross-branch or self-intersection update fails
closed or returns candidates for future frontend disambiguation.

# Geometric invariants and degeneracies

## Domain, seam and degeneration rules

- Branch/component identity is explicit; no jump crosses an invalid gap.
- Periodic parameters use the provider's half-open fundamental interval and
  canonical seam equivalence; no duplicate seam candidate is published.
- A uniquely continued existing semantic point may cross the seam without a
  new durable point or screen-continuity heuristic.
- At self-intersections, equal Cartesian images with different semantic
  addresses remain multiple candidates.
- Cusps, zero speed, repeated images, zero-length spans and insufficient local
  evidence report ambiguity or unresolved state rather than guessed uniqueness.
- For R5 `COLLAPSED_IMAGE` at `k=0`, a new query at the image is generally
  degenerate/nonunique. An existing point retains its exact address, collapses
  geometrically and recovers normally when the transform becomes invertible.
- For invertible similarities, inverse mapping may accelerate search, but the
  resulting address belongs to the transformed semantic source.

# Compatibility and serialization

## Persistence and lifecycle

Transient query candidates are never serialized. A constructed point persists
its durable identity, semantic source, branch/component and editable canonical
address through the normal host XML/DAG authority. Persist no click coordinate,
pixel, render vertex or solver enumeration.

Require native `.cedg` save/reopen, copy/remap, rename, compatible redefine,
source invalidity/recovery and undo/redo. A copied point receives a new durable
point identity and remaps only through existing exact construction-copy rules.
An invalid branch/address becomes undefined instead of reattaching by
coordinate.

R6 does not close the separate R4 periodic-quarantine native-round-trip risk
unless that exact quarantine state receives its missing end-to-end evidence.

## Determinism and efficiency

For identical semantic source revision and geometric request, candidate status,
canonical candidate set and semantic addresses are independent of solver
enumeration, render resolution, zoom, DPI, viewport and UI event history.

Instrument semantic evaluations, components/spans inspected, subdivisions,
refinement iterations, candidate count, local/global fallback and cache
hit/miss if a revision-scoped cache is introduced. Work must be bounded and
render-resolution independent. Reuse current polynomial/provider data; do not
perform an independent whole-curve solve per existing point.

# Required tests and commands

Implement every active row in the R6 validation matrix, including:

- forward/inverse round trips;
- straight, periodic, disconnected and multibranch Locus V2;
- SplineV2 span, knot, self-intersection and singular cases;
- unique/none/multiple/unresolved typed results;
- creation and drag with exact semantic-state undo/redo;
- bidirectional periodic-seam drag of the same interaction-owned point,
  path-independent final address, unresolved-seam fail-closed control and
  deterministic candidate ordering;
- all supported R5 similarity images, negative dilation and `k=0`;
- source update/recovery, copy/remap, rename/redefine and `.cedg` reopen;
- solver-enumeration and viewport/render independence; and
- bounded performance/instrumentation.

The focused authority is
`tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1`.
Follow the R5/G9S1 parameter contract (`-KeepBuildOutputs`, `-LogDirectory`,
`-CanonicalSummaryPath`, `-CompareCanonicalSummaryPath` and the composed-history
switch where applicable). Run it twice and require byte-identical canonical
summaries. Source evidence uses tracked Git blobs when clean or current
candidate bytes when modified/new, strict UTF-8 with BOM removal and canonical
LF SHA-256. Binary fixtures remain byte-exact.

Run relevant semantic Point, G5, G6/G6R, G7, G8, G9A, G9U0/R1/R2/R3/R4/R5,
G9S1, G9X1, persistence, Classic, Checkstyle, both Git diff checks and full:

```powershell
.\tools\agent\verify.ps1
```

Require terminal output:

```text
All GeoCeDG verification gates passed.
```

Generated logs remain under ignored `artifacts/` only.

# Required artifacts

Freeze the canonical prompt, ADR 0019, specification, architecture note,
72-row matrix, exact scenario/evidence/hash records, candidate report, focused
verifier, composed-verifier insertion, modified-files registration and living
documentation traceability. Generated logs and canonical run summaries remain
ignored evidence under `artifacts/g9u0-r6/`; they are never tracked sources.

## R6 acceptance surface and future G9U1 smoke

R6 intentionally has no productive Desktop Point-tool consumer. The author
accepts the focused test-host/API surface as the correct R6 acceptance surface;
a separate manual GUI smoke is therefore deferred to G9U1 by design, not
pending in R6.

The R6 kernel authority must exercise directly:

1. unique point creation and drag on a straight Locus V2;
2. unique point creation and drag on SplineV2;
3. explicit self-intersection ambiguity;
4. bidirectional periodic seam crossing by the same interaction-owned point,
   plus an unresolved seam request that leaves it unchanged;
5. an invertible transformed source;
6. new-query ambiguity plus existing-point retention/recovery at `k=0`; and
7. native `.cedg` reopen followed by another semantic drag.

The future G9U1 manual smoke must use the productive Point-tool route and cover:

```text
Point tool -> click LocusV2/SplineV2 -> create -> drag -> seam crossing
  -> ambiguity chooser -> transformed source -> k=0 -> save/reopen
```

That future frontend smoke is not implemented or executed by R6.

# Stop conditions

Stop and report before broadening if correctness would require generic `Path`,
render/pixel authority, silent self-intersection selection, an unbounded global
interactive solve, unapproved persistence semantics, G9U1 product UI, or a
change to R4/R5/G9S1 meaning.

## Terminal state

Do not commit, push, merge, tag or self-approve R6. Stop at:

```text
G9U0-R6 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR FINAL APPROVAL
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false
manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN
kernelDiagnosticAcceptance = PASS

G9U1 = DESIGN CANDIDATE — PROTECTED / NOT AUTHORIZED
g9u1ProtectedCheckpoint = 857de6628489bda0b65a5ba5145e62ca0795fc32
blockedUntilR6Pass = true

G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

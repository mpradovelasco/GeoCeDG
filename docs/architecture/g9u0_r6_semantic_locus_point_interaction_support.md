# G9U0-R6 semantic Locus point interaction architecture

- Status: **PASS — AUTHOR APPROVED**
- Date: 2026-09-01
- Phase: G9U0-R6
- Layer: shared Java kernel
- Approval: `selfApproved=false`, `authorApproved=true`, `passClaimed=true`

## 1. Entry and downstream boundary

R6 starts from the published G9S1 authority
`de33f3a80102adb051aaa7547a72b7e97409c58c`; annotated
`geocedg-g9s1-pass` object
`ece0ca6f00299d3347e57fac38b7a28cade28644` peels to that commit.

The G9U1 preexecution design is protected independently at:

```text
feature/g9u1-construction-workspace-planning
857de6628489bda0b65a5ba5145e62ca0795fc32
```

Its 17 planning paths are not merged into R6. The protected prompt has
canonical-LF SHA-256
`2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322`.
R6 records actual APIs for a later reconciliation; it does not execute G9U1.

## 2. Existing forward architecture

The existing semantic point flow is exact and one-way:

```text
GeoLocusV2 + explicit branch key + GeoNumeric parameter
    -> normal semantic-point algorithm
    -> evaluate current immutable definition at provider-canonical address
    -> ordinary GeoPoint
```

The locus definition owns domain, orientation, branch/component validity and
canonicalization. The point is reconstructible and participates in normal
DAG/copy/undo/persistence. `GeoLocusV2` deliberately does not implement generic
`Path`.

R6 does not replace that flow. It supplies the inverse interaction evidence and
an editable semantic-address input suitable for the same forward evaluator.

## 3. Layered design

```text
future frontend stroke hit-test
  selected source + transient world request + transient distance policy
                          |
                          v
shared-kernel inverse semantic-address resolver
  provider/domain/branch/component/canonicalization
  general bounded capability OR provider specialization
                          |
                          v
typed current-snapshot result
  none | unique | multiple | unresolved | invalid | degenerate | unsupported
                          |
                 explicit candidate choice
                          |
                          v
normal semantic point parent + explicit editable semantic-address state
                          |
                          v
ordinary GeoPoint / persistence / copy / undo / downstream DAG
```

Frontend tolerance determines only whether a source/request is worth querying.
It never enters the point identity or XML. The kernel does not read Euclidian
view state.

## 4. Typed values and authority

The candidate uses small immutable values at the shared-kernel seam:

- one query context containing current semantic source, finite world request and
  bounded nonpersistent interaction policy;
- one typed result with status, deterministic candidate collection,
  instrumentation and diagnostics;
- one candidate containing exact semantic address plus residual/regularity/
  uniqueness evidence; and
- one explicit semantic-address state used by the normal point algorithm.

The source-driven seam is `LocusPointInteractionQuery2D` ->
`LocusPointInteractionResolver2D` -> `LocusPointInteractionResult2D`, with
immutable candidate, policy, work-budget and instrumentation values. Selected
state is encoded by `LocusSemanticAddressState2D` and consumed by
`AlgoSemanticLocusPoint2D`. The architectural separation remains normative:
candidate evidence is transient; selected address state is constructive.

The accepted implementation signatures are:

```java
new LocusPointInteractionQuery2D(GeoLocusV2 source, double targetX,
    double targetY, LocusPointInteractionPolicy2D policy)
new LocusPointInteractionQuery2D(GeoLocusV2 source, double targetX,
    double targetY, LocusPointInteractionPolicy2D policy,
    LocusSemanticAddress2D currentAddress)
new LocusPointInteractionResolver2D().resolve(
    LocusPointInteractionQuery2D query)
LocusV2PublicOperations.createInteractiveSemanticPoint(
    Construction construction, String label, GeoLocusV2 source,
    LocusPointInteractionCandidate2D candidate)
LocusV2PublicOperations.moveInteractiveSemanticPoint(
    GeoPoint point, double targetX, double targetY,
    LocusPointInteractionPolicy2D policy)
```

`resolve(...)` and `moveInteractiveSemanticPoint(...)` return
`LocusPointInteractionResult2D`. Its exact statuses are
`NO_ADMISSIBLE_PREIMAGE`, `UNIQUE_ADMISSIBLE_PREIMAGE`,
`MULTIPLE_SEMANTIC_PREIMAGES`, `UNRESOLVED_NUMERICAL_SEARCH`,
`INVALID_SOURCE`, `DEGENERATE_SOURCE_IMAGE` and `UNSUPPORTED_CAPABILITY`.
The candidate constructor is package-owned; callers consume immutable
resolver-produced candidates. The caller must gate creation by typed result:
automatic creation only from `UNIQUE_ADMISSIBLE_PREIMAGE`, or exact explicit
selection from `MULTIPLE_SEMANTIC_PREIMAGES`. An unresolved discovered
candidate is diagnostic evidence, not a frontend creation authority.

Candidate order is canonical only for deterministic presentation/testing. It is
not identity. A selected candidate is identified by its full semantic address,
not by its collection index.

## 5. General resolver

The general resolver operates over current finite provider authority:

1. read branches/components in a canonical semantic order;
2. derive a bounded semantic partition from declared components and available
   provider partitions, never the render cache;
3. use evaluator/differential evidence to exclude or subdivide distance cells;
4. refine stationary/boundary candidates under explicit evaluation and work
   limits;
5. provider-canonicalize parameters and deduplicate only equivalent semantic
   boundary representations;
6. forward-evaluate candidates and compute residual evidence; and
7. classify uniqueness, ambiguity, no-result or unresolved state.

A finite request distance can bound acceptance but not identity. If a general
source lacks sufficient global coverage for creation, the truthful result is
unresolved or unsupported. Existing-point drag may use the current exact
address and branch/component as an explicit local edit constraint, but not as a
license to jump elsewhere.

The implementation adds `CertifiedAffineLocus2D` as the narrow complete-
coverage capability for structurally captured `F(u)=a u+b` definitions.
`ReconstructibleLocusEvaluator2D` recognizes only its direct affine semantic
expression; `LocusSimilarityEvaluator2D` propagates the certificate
algebraically. No sample fitting is allowed. Inspecting all requested finite
components produces `ALL_CERTIFIED_AFFINE_COMPONENTS`, which can justify
definitive no-preimage or unique-preimage results.

This certificate is optional acceleration/evidence. Similarity propagation
captures all branches atomically; a nonfinite transformed coefficient or
intercept yields no affine certificate rather than invalidating the semantic
transform. Address evaluation still publishes the truthful `NON_FINITE` state,
and a later finite transform revision can recapture the capability.

The evaluator-only fallback deliberately publishes
`BOUNDED_EVALUATOR_SEARCH`. Even when it finds zero or one forward-verified
candidate, it cannot exclude a narrow unsampled minimum and therefore returns
`UNRESOLVED_NUMERICAL_SEARCH`. Multiple distinct candidates may be exposed as
multiple, but the fallback never promotes absence or one local candidate to
global uniqueness.

## 6. SplineV2 specialization

The G9S1 provider already owns piecewise polynomial spans, bounds, derivative
evaluation and right-owned knots. For query (q), the specialization examines
endpoints and stationary roots of

\[
d_q(u)=\|C(u)-q\|^2.
\]

It can reject spans from semantic polynomial bounds, isolate roots of
(d'_q), refine in the original parameter and canonicalize shared knots. The
implementation reuses the current polynomial model; it does not fit a curve
from render samples.

Equal Cartesian output from different spline parameters remains multiple
semantic candidates. A zero-speed or repeated span requires separate local
evidence and may remain ambiguous/unresolved.

`PiecewisePolynomialLocus2D` exposes one paired x/y coefficient snapshot and an
O(1) captured composition depth. R5 similarity layers transform the pair once
per layer, giving linear coefficient work in nesting depth instead of two
duplicated recursive traversals. `LocusEvaluationSession2D` bounds nested cache
misses and active evaluator depth; the query budget may be stricter than the
shared hard ceiling of 128. Polynomial intersection consumers use the same
paired seam. Depth/work exhaustion is typed unresolved state. Coefficients,
root cells and residuals remain bounded floating evidence, not exact arithmetic.

## 7. Similarity images

For an invertible R5 transformation, the query may be mapped through the exact
current inverse similarity and resolved on the source. Final forward
verification is performed on the transformed evaluator. The resulting address
belongs to the transformed locus and its durable context; no source
intersection token or source object identity is reused.

Negative scaling changes ambient orientation but not the retained semantic
parameter orientation. At `k=0`, the semantic source is a valid
`COLLAPSED_IMAGE`: many valid addresses share one Cartesian image. A new query
is degenerate/nonunique. An existing point keeps its explicit address and
therefore collapses and recovers by ordinary source recomputation.

## 8. Point state and editing

The implemented host seam makes the editable semantic coordinate a dedicated
independent `GeoText`/`GeoNumeric` input pair of the normal point algorithm.
`AlgoSemanticLocusPoint2D.getSemanticAddress()` exposes its retained
last-accepted selector, whereas `getCurrentSemanticAddress()` and
`getMetricPositionBinding()` expose current revalidation/certificate state.
Temporary invalidity clears the latter and all coordinates but keeps the former
for exact recovery; edit operations require the current address and therefore
cannot move a dormant point or reinterpret its retained selector.
Required properties are:

- source and semantic address are inspectable/reconstructible authority;
- the same ordinary point and durable ID survive every drag update;
- updates are atomic and undoable;
- no point/algorithm churn occurs per pointer event;
- source invalidity makes the point undefined without stale coordinates;
- source recovery at the same valid address restores it; and
- copy/remap rewrites exact source dependencies without coordinate attachment.

The controller will later request an edit. It does not retain the authoritative
parameter itself.

`LocusV2PublicOperations.moveInteractiveSemanticPoint` resolves and validates
before mutation, then runs address writes, `updateCascade` and the publication
postcondition inside `Construction.runAtomicConstructionMutation`. The
versioned address state owns the exact provider-canonical parameter bits. Its
hidden raw numeric is checked as the exact reconstruction
`canonical + lift * period`; the point algorithm does not recover identity by
modularly re-canonicalizing that sum because a valid floating addition and
subtraction may change the canonical bits. This is exact state reconstruction,
not a parameter tolerance or proximity rule. That host
seam snapshots the complete current undo XML and reuses the existing
Construction restore path on `RuntimeException` or `MyError`. A restore failure
is catastrophic and retains the original failure as suppressed evidence. It
does not create an undo record: a successful future gesture must still be
grouped by its frontend owner.

The snapshot is O(N) in construction size for every edit. Rollback can replace
Java object instances during reconstruction, so a failed caller must stop the
gesture and reacquire current objects. This is a bounded candidate cost and an
explicit safety caveat, not a claim of constant-time or instance-preserving
rollback.

## 9. Ambiguity and topology

Creation succeeds automatically only for one established candidate. Multiple
branches, disconnected components or self-intersection preimages are returned
explicitly. A future G9U1 chooser may select one exact address.

Dragging can constrain search to the point's current semantic branch/component
and local address neighborhood. It fails closed when unique continuation is not
established. Previous screen position, nearest Cartesian point and event
history are forbidden tie breakers.

Invalid gaps are never bridged. Cusps, repeated images, zero-length spans,
component boundaries and local noninjectivity retain typed ambiguity or
unresolved states.

## 10. Periodic seam

The domain provider owns half-open canonicalization and seam equivalence. The
resolver deduplicates equivalent endpoint representations and retains one
canonical address. Existing-point seam drag uses current semantic address plus
provider seam structure; it does not infer identity from pointer continuity.

The final kernel regression uses one closed periodic SplineV2. It moves one
interaction-owned point from canonical `u=0.98` through the seam to canonical
`u=0.02` with intrinsic `periodicLift=1`, reverses to the original side,
continues away from the seam and reaches one final address by direct and
incremental legal paths.
The point instance, durable ID, source, branch/component and construction size
remain unchanged; canonical parameter and intrinsic lift wrap as selected and
each snapshot contains one candidate. The existing evaluator-only periodic
negative remains `UNRESOLVED_NUMERICAL_SEARCH` and leaves the current point and
address inputs untouched. Thus a unique seam is constructive while an
unresolved seam still fails closed.

This ordinary semantic-point seam evidence is distinct from R4 intersection
token quarantine. The risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open/tracked unless the
exact missing quarantine-state `.cedg` round trip is added.

## 11. Cache and complexity model

Any acceleration is immutable/revision-scoped and non-authoritative. A changed
semantic revision invalidates it. The resolver records evaluations,
components/spans inspected, subdivisions, refinements, candidates,
local/global fallback and cache hit/miss where present.

Spline polynomial models and provider partitions are reused once per current
revision. An existing point edit may begin locally and fall back once to the
bounded provider search. No full solve is repeated independently for every
point, no render-resolution work enters the count and no trajectory history is
stored.

For the accepted implementation, semantic evaluator/polynomial work is bounded by
the query counters and composition-depth policy described above. Polynomial
coefficient propagation is linear in captured transform depth. One interactive
state mutation additionally pays the host O(N) Construction snapshot cost.
Final observed counts and hashes are frozen by the focused evidence; no
arithmetic-exactness or universal latency claim is inferred from these bounds.

## 12. Persistence boundary

Transient requests/results/caches are discarded. Native persistence records
only normal semantic point construction inputs/state. Reopen recomputes from
the current semantic source before the point is current. No pointer coordinate,
pixel, render segment or candidate ordinal appears in XML.

`LocusSemanticAddressState2D` persists branch, exact component lineage,
canonical parameter context, periodic lift and seam side in a versioned state.
It hydrates the durable selector on a dormant reopen independently of the
current binding: the point stays undefined until exact current revalidation,
then the same point may reactivate without coordinate repair.
At a shared endpoint the recorded lineage must match exactly one containing
component. An old/plain branch input with several containing components, a
missing lineage match or duplicate lineage fails closed instead of selecting a
component by list order.

Hidden auxiliary presentation is authorized only by both the stable
`LOCUS_INTERACTION_POINT` output role and structurally dedicated/exclusive
address inputs. `SpatialIdentityRegistry` reapplies auxiliary and restricted-
Euclidian presentation after persistent identity attachment on reopen. An
ordinary user `GeoText` that happens to decode like the R6 codec is not hidden,
claimed or removed without that role and ownership proof.

Feature-off and Classic preservation reuse existing G9U0/R2/G9S1 behavior.
R6 does not enable experimental creation in Classic.

## 13. Operational integration

The focused R6 verifier is
`tools/agent/verify-g9u0-r6-semantic-locus-point-interaction-support.ps1` and
follows the G9S1/R5 parameter pattern (`-KeepBuildOutputs`, `-LogDirectory`,
`-CanonicalSummaryPath`, `-CompareCanonicalSummaryPath` and composed-history
reuse):

- exact worktree candidate inventory before closeout and sealed tagged-
  descendant inventory afterward;
- exact test method names/counts and matrix/scenario equality;
- Git-blob-or-current-candidate UTF-8 canonical-LF SHA-256 for text;
- byte-exact hash for any binary fixture;
- LF/CRLF equivalence plus real-content-mutation regression;
- generated-state snapshot/cleanup;
- focused shared-kernel and Desktop persistence tests plus Checkstyle;
- deterministic A/B canonical summaries; and
- historical G9S1 static authority unless already composed.

The validated matrix/scenario inventory contains 72 rows after adding the
certified-affine, evaluator-only, atomic rollback, auxiliary ownership/reopen,
shared-endpoint lineage and polynomial-depth negatives. The focused suite now
contains 55 methods (52 shared-kernel and 3 Desktop), including the positive
bidirectional/path-independent periodic-seam drag and its retained unresolved
negative. Both focused runs pass 55/55 with identical canonical summary
SHA-256
`8f48ab5ef1d8129fcb9ccce2c203daf524e41e2153f0d0b5c90d2e1e662277a2`.
The canonical-LF R6 prompt, evidence and scenario hashes are respectively
`6abf56ccb45fb635f2e09dc239bdf75e1a85e379ac7a544adb07e06025732d2e`,
`e535c863c16a384653c547a2eac220c2d05e13ee391283772b5b57c99eb24561`
and `e85c5e5cf7fc915d84ad4f084bc4ca8f9c87500a67b83d8e3b7f953ed038caee`.

`tools/agent/verify.ps1` inserts the complete paired R6 authority after G9S1
and before packaging/future G9U1 work. Partial registration fails closed.

## 14. Protected G9U1 delta

The protected planning prompt describes only a conceptual “typed, bounded,
deterministic resolver” and normal semantic point result. A post-R6 successor
must consume the actual kernel types, statuses, edit transaction and
performance contract frozen above. No provisional Java name in the protected
branch is authority.

Current main retains its historical post-G9S1 prompt with canonical-LF hash
`6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4`;
the separately protected candidate hash is `2319df21...fa322`. They are not the
same artifact or status.

## 15. Terminal boundary

R6 is `PASS — AUTHOR APPROVED`; ADR 0019 is Accepted and the specification is
normative. Because R6 has no productive Desktop consumer, the author accepts
the test-host/API evidence as its diagnostic acceptance surface and defers the
manual GUI smoke to G9U1 by design:

```text
manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN
kernelDiagnosticAcceptance = PASS
selfApproved = false
authorApproved = true
passClaimed = true
```

G9U1 remains protected, unexecuted and unauthorized. The periodic point seam
does not close `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`, which remains
`OPEN / TRACKED`.

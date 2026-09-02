# G9U0-R6 semantic Locus point interaction closeout report

- Status: **PASS — AUTHOR APPROVED**
- implementationStarted: true
- selfApproved: false
- authorApproved: true
- passClaimed: true
- manualGuiSmoke: **DEFERRED TO G9U1 BY DESIGN**
- kernelDiagnosticAcceptance: **PASS**

## 1. Entry authority

R6 begins from clean published G9S1 main
`de33f3a80102adb051aaa7547a72b7e97409c58c`. The annotated
`geocedg-g9s1-pass` object
`ece0ca6f00299d3347e57fac38b7a28cade28644` peels to that commit.

The independent G9U1 preexecution design candidate is protected at branch
`feature/g9u1-construction-workspace-planning`, checkpoint
`857de6628489bda0b65a5ba5145e62ca0795fc32`, with 17 planning-only paths and
prompt canonical-LF SHA-256
`2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322`.
The local and published origin branch both point to that checkpoint with 0/0
divergence. It is not merged into R6, remains immutable historical pre-R6
design evidence and does not authorize G9U1.

## 2. Characterization and decision

The entry kernel provided exact forward semantic evaluation and the normal
reconstructible `Point(L,"branch",u)` construction, but no approved bounded
inverse from a geometric interaction request to typed semantic-address
candidates and no interaction-owned editable address seam for the same point.

Accepted ADR 0019 establishes one shared-kernel resolver and explicit
normal-DAG semantic address state. It rejects generic `Path`, render/pixel
authority, coordinate
identity, solver/list order and movement history. A provider-owned structural
affine certificate covers the direct scalar form `F(u)=a*u+b` analytically;
SplineV2 uses G9S1 polynomial spans and stationary points of squared distance.
A general evaluator-only search remains bounded discovery/local evidence: zero
or one candidate cannot prove whole-component cardinality and therefore fails
closed as unresolved. Multiple distinct established preimages remain explicit.

## 3. Productive implementation

`LocusPointInteractionResolver2D` consumes an immutable
`LocusPointInteractionQuery2D` and publishes
`LocusPointInteractionResult2D` with typed none, unique, multiple, unresolved,
invalid, degenerate and unsupported states. Each
`LocusPointInteractionCandidate2D` carries the exact
`LocusSemanticAddress2D`, source revision, forward-evaluated point, world
distance, local interval, regularity, numeric guarantee, method and local
evidence. `SearchCoverage.establishesCompleteRequestedScope()` is the single
guard permitting definitive unique/none results.

The inverse tiers are:

1. `CertifiedAffineLocus2D`: immutable provider-owned coefficients, analytic
   projection over every requested finite component and forward verification,
   with `ALL_CERTIFIED_AFFINE_COMPONENTS` coverage;
2. `PiecewisePolynomialLocus2D`: complete or explicitly partial polynomial-span
   coverage and `PolynomialRootIsolation2D` stationary-cell isolation; and
3. bounded semantic evaluator search, which may discover and locally establish
   candidates but never claims unique/none without global coverage.

Affine certification is optional evidence. A composed historical R5 regression
exposed nonfinite coefficient capture aborting a valid transformed-locus
publication. R6 now makes that capture atomic and fail-soft: the certificate is
unavailable, the semantic transform remains defined, address evaluation reports
`NON_FINITE`, and a finite later revision recaptures the capability.

`LocusV2PublicOperations.createInteractiveSemanticPoint()` creates one ordinary
`GeoPoint` with an `AlgoSemanticLocusPoint2D` parent and two dedicated normal-DAG
inputs: a versioned `LocusSemanticAddressState2D` `GeoText` and a raw-parameter
`GeoNumeric`. The stable output role `LOCUS_INTERACTION_POINT`, plus exact
structural ownership of those independent inputs, is required before they are
hidden as auxiliaries or may be edited. Encoded text alone conveys no ownership.
Persisted provider, branch, `componentLineageKey`, parameter, periodic lift and
seam side must all reconstruct exactly; a shared endpoint without one unique
persisted component lineage fails closed.

The durable last-accepted selector is distinct from the current revision
binding. A composed historical G9U0 split/merge regression exposed premature
loss of the observable selector when current component validation failed.
`getSemanticAddress()` now retains that selector, while
`getCurrentSemanticAddress()` and the metric binding become null and the point
coordinates remain undefined. Public movement and publication postconditions
consume only the current address, so retention cannot authorize a dormant edit
or global fallback. Native reopen hydrates that durable selector from the
versioned input while leaving the current binding null; exact component
revalidation reactivates the same point.

`moveInteractiveSemanticPoint()` resolves before mutation, then performs the
two exact address-input edits and postcondition inside
`Construction.runAtomicConstructionMutation()`. A `RuntimeException` or
`MyError` restores the complete host Construction/XML/spatial-identity snapshot;
failed restoration is catastrophic rather than best-effort continuation. A
successful gesture keeps the existing point/algorithm and uses normal undo
grouping. Rollback may rebuild Java instances, so a caller aborts the failed
gesture and reacquires by normal construction authority.

Periodic addresses use provider canonicalization, explicit lift and seam-side
state. The final acceptance gap exposed one floating reconstruction defect:
after a unique resolver result crossed a closed SplineV2 seam, adding the lift
to the canonical parameter and modularly canonicalizing the hidden numeric
changed the canonical parameter bits. Exact state matching then rejected the
otherwise valid selected address. `AlgoSemanticLocusPoint2D` now treats the
versioned encoded address as exact canonical authority and checks the hidden
numeric as its exact lifted reconstruction. No tolerance, Cartesian proximity
or movement-history identity was added.

The named regression crosses the seam in both directions with one existing
interaction-owned point, continues away from it and reaches the same final
semantic state by direct and incremental paths. Point ID, source, branch,
component and Construction cardinality remain unchanged, every successful
snapshot has one candidate, and the provider-canonical parameter/lift wrap
correctly. The retained evaluator-only periodic control stays
`UNRESOLVED_NUMERICAL_SEARCH` and leaves the point/address unchanged.

Invertible R5 similarity images retain source parameter authority but
receive their own locus identity; `k=0` reports a degenerate collapsed image and
an existing addressed point may become undefined and recover. XML reopen,
copy/remapping, undo/redo, compatible identity attachment and removal of only
exclusive dedicated auxiliaries use existing host seams. No frontend, generic
`Path`, public command or G9U1 implementation was added.

## 4. Determinism and efficiency

The initial query ceiling is 32,768 semantic evaluations, 512 subdivisions, 80
refinement iterations and 1,024 candidates. Nested evaluation shares the same
memoizing session and miss counter. Polynomial composition depth is captured in
O(1), limited by the smaller of the query evaluation budget and the shared
stack-safe ceiling 128, and obtains x/y coefficients as one coherent pair. A
similarity chain therefore costs O(depth), not the former duplicated O(2^depth)
coefficient traversal; a limit breach is typed unresolved before unsafe
recursion. Polynomial coefficient degree is separately bounded before root
isolation.

Candidates are canonicalized by branch, component lineage, periodic lift,
canonical parameter and seam side. That order is presentation/determinism only,
never persistent identity by list position. A move with a current address scopes
the search to its exact branch/component; there is no hidden global fallback.
The host snapshot required for atomic movement is O(N) in current Construction
serialization size for each successful user gesture. This bounded correctness
seam is intentionally retained for R6; it does not add a second graph or retain
trajectory history. Instrumentation records search scope, evaluations, cache
hits/misses, spans, subdivisions, refinements, candidates and zero render,
viewport or pixel reads.

Canonical source evidence must follow the R3/R4/R5/G9S1 convention: unchanged
tracked text is read from its Git blob; dirty/new candidate text is read from
candidate bytes; strict UTF-8 is BOM-independent and normalized to LF before
SHA-256. A controlled regression requires LF = CRLF and a true content mutation
to produce a different hash. Binary fixtures remain byte-exact.

## 5. Validation authority

The author-approved
[72-row matrix](g9u0_r6_semantic_locus_point_interaction_validation_matrix.md)
and [scenario inventory](../../geocedg/validation/g9u0-r6/g9u0-r6-semantic-locus-point-interaction-scenarios.json)
are the frozen R6 validation authority.

```text
focusedJUnit = 55 (52 shared-kernel + 3 Desktop)
focusedA = PASS
focusedB = PASS — EXACT CANONICAL MATCH
preCloseoutCandidateSummarySha256 = 8f48ab5ef1d8129fcb9ccce2c203daf524e41e2153f0d0b5c90d2e1e662277a2
approvedCloseoutSummarySha256 = 7aaed6a558bf6f86ec93a5b45eb74155d45e66b52b47c373a9ad32f43b156cc9
gitDiffCheck = PASS
gitDiffCachedCheck = PASS
composedVerifier = PASS — All GeoCeDG verification gates passed.
```

The recorded hash is the byte-exact accepted implementation-candidate A/B
summary. Closeout verification regenerates a new A/B summary after approval-only
authority edits; its final hash is recorded before commit rather than guessed
here. Summary hashes belong outside recursively hashed evidence JSON. Generated
logs and canonical summaries remain ignored under `artifacts/g9u0-r6/`.

## 6. Exact changed-path inventory

The frozen candidate contains **46 paths**: 26 productive shared-kernel Java
paths, 3 test paths and 17 governance/documentation/operational paths. There is
no productive frontend path, generated artifact or protected G9U1 path. The
exact path-by-path set is the `$ExpectedCandidatePaths` authority enforced by
`verify-g9u0-r6-semantic-locus-point-interaction-support.ps1`; any missing or
additional path fails static and composed verification.

## 7. Compatibility and retained risks

- Existing exact Point commands remain authoritative.
- `GeoLocusV2` remains non-`Path`.
- Classic Point/path behavior is unchanged.
- R4 intersection selectors/tokens, R5 transformations and G9S1 spline
  semantics are not redefined.
- G9U1 product UI remains unexecuted.
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains
  **OPEN / TRACKED / NONBLOCKING**. Ordinary R6 point reopen is not the missing
  quarantine-state round trip.

## 8. Protected G9U1 API delta

The protected prompt assumes only a typed bounded resolver returning zero, one
or several semantic addresses, plus normal-DAG point creation/drag. Published
R6 source now owns the exact API mapping. A separate post-R6 planning task, not
R6 implementation, must reconcile those conceptual assumptions against that
source before any G9U1 authorization.

Main's historical post-G9S1 prompt has canonical-LF SHA-256
`6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4`;
the protected checkpoint prompt has `2319df21...fa322`. Neither is executed by
R6.

## 9. Acceptance surface and future G9U1 manual smoke

The author accepts the test-host/API evidence as the appropriate R6 acceptance
surface because R6 intentionally contains zero productive Desktop Point-tool
integration. A manual R6 GUI smoke would test a consumer that does not exist;
it is therefore **DEFERRED TO G9U1 BY DESIGN**, not pending or waived silently.

The kernel diagnostic acceptance covers unique creation/drag for affine Locus
V2 and SplineV2, self-intersection ambiguity, successful bidirectional periodic
seam drag, unresolved-seam no-mutation, R5 images, `k=0` collapse/recovery and
native reopen. It is PASS only with paired deterministic and composed authority
green.

Future G9U1 must perform the real end-to-end manual smoke through the productive
frontend path:

```text
Point tool -> click LocusV2/SplineV2 -> create -> drag -> seam crossing
  -> ambiguity chooser -> transformed source -> k=0 -> save/reopen
```

This future Point-tool/chooser/accessibility work was not implemented in R6.

## 10. Author decision and smoke chronology

1. The completed R6 implementation candidate passed 55/55 focused methods in
   both deterministic runs, the relevant historical gates and full composed
   verification.
2. Review confirmed that R6 deliberately exposes no productive Desktop Point
   tool, so an ordinary GUI gesture cannot exercise the new kernel seam.
3. The author accepted the test-host/API diagnostic surface as the correct R6
   acceptance surface and explicitly deferred the first productive GUI smoke to
   G9U1 by design.
4. The final periodic-seam regression closed the remaining kernel acceptance
   gap, including bidirectional and path-independent seam traversal plus the
   unresolved no-mutation control.
5. The author then declared `G9U0-R6 = PASS — AUTHOR APPROVED`; this approval is
   external to automated evidence and is not self-approval.

## 11. Terminal declaration

```text
G9U0-R6 = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN
kernelDiagnosticAcceptance = PASS

G9U1 = DESIGN CANDIDATE — PROTECTED / NOT AUTHORIZED
g9u1ProtectedCheckpoint = 857de6628489bda0b65a5ba5145e62ca0795fc32
r6EntrySatisfied = true
postR6ReconciliationRequired = true
implementationAuthorized = false

G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
```

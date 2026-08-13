# Locus V2 2D intersections — proposed architecture

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT APPROVED / NOT IMPLEMENTED** |
| Roadmap phase | G8 planning; G8 remains `NOT STARTED` |
| Semantic model | [`locus_v2_intersection_semantic_model.md`](locus_v2_intersection_semantic_model.md) |
| Proposed contract | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) |
| Upstream audit | [`locus_v2_intersection_upstream_impact.md`](locus_v2_intersection_upstream_impact.md) |
| Date | 2026-08-13 |

This document turns the proposed G8 semantics into an implementation shape for
characterization. It does not authorize productive source, public commands,
ordinary `Path` behavior, persistence, or dispatcher integration.

## 1. Placement and dependency direction

Intersection truth belongs in the shared Java kernel only after author
approval because it changes incidence, participates in the construction DAG,
and must remain valid for non-render consumers. Characterization probes,
independent references, diagnostics, and functional counters remain in test
and validation layers.

```text
GeoLocusV2 definition snapshot + semantic revision
                         |
                         v
LocusEvaluationSession2D + target semantic adapter
                         |
                         v
capability selection -> candidate isolation -> refinement
                         |
                         v
membership/residual verification + semantic-parameter deduplication
                         |
                         v
topology classification + root continuation
                         |
                         v
immutable LocusIntersectionResult2D
                         |
                         v
AlgoLocusIntersectionV2 -> proposed GeoLocusIntersectionResult
                         |
                         v
optional bounded ordinary-point projection, only after separate approval
```

There is deliberately no arrow from `LocusRenderCache2D`, render vertices,
legacy `GeoLocus.myPointList`, viewport, zoom, DPI, or pixel tolerances into
this graph.

## 2. Proposed responsibility split

Names are conceptual until G8A records exact candidate APIs.

| Responsibility | Candidate component | Contract |
|---|---|---|
| Immutable request | `LocusIntersectionQuery2D` | Binds source snapshots, target family, policy/version, tolerances, budgets, and requested support level |
| Target authority | `IntersectionTarget2D` adapters | Exposes only a representation already authoritative for the actual target type |
| Evaluation | existing `LocusEvaluationSession2D` | Evaluates one captured Locus V2 revision; retains existing bounded/cycle-safe behavior |
| Capability choice | `LocusIntersectionCapability2D` | Selects analytic, certified, derivative-aware, or evaluator-only behavior without inflating guarantees |
| Isolation | `RootIsolation1D` | Produces semantic-parameter intervals and explicit coverage evidence |
| Refinement | `RootRefiner1D` | Refines an isolated candidate within its valid component and work budget |
| Verification | `IntersectionVerifier2D` | Re-evaluates semantic geometry, target membership, normalized residual, and domain membership independently of broad phase |
| Classification | `IntersectionClassifier2D` | Reports contact and domain-location evidence without converting unknown into transverse or absent |
| Continuation | `IntersectionContinuation2D` | Associates roots within one source-pair/topology context using semantic intervals and lineage |
| Immutable result | `LocusIntersectionResult2D` | Publishes query-level state plus zero or more rich solution records atomically |
| DAG owner | `AlgoLocusIntersectionV2` | Registers both geometric inputs and replaces the entire current-revision snapshot on recompute |
| Rich Geo | `GeoLocusIntersectionResult` | Makes success, absence, unresolved, overlap, and stale outcomes inspectable in the normal DAG |
| Optional point view | dedicated derived adapter | Projects only finite verified solutions into bounded ordinary point outputs |

The rich result is the authority. A point adapter cannot improve a guarantee,
discard an unresolved case and call the remainder complete, or manufacture a
finite representative for overlap.

## 3. Target adapters and minimum capability surface

G8A should compare adapters rather than introduce one universal implicit
conversion layer.

### Lines, segments, and rays

Use the line's homogeneous incidence equation for the residual. Segment and
ray membership add their existing finite/half-line restriction after the
supporting-line root is refined. Included endpoints are classified explicitly.
The adapter must define residual normalization under coefficient scaling.

### Circles and conics

Use the conic's existing matrix/evaluation authority. The adapter must retain
the actual conic type and degeneration state; it must not silently reinterpret
a degenerate conic as a generic smooth implicit curve. Circle-specialized
analytic or derivative evidence may be used only when it is derived from the
same authoritative conic data.

### Functions and implicit curves

`GeoFunction` is not admitted merely because a view interval can be sampled;
its view-clipped root paths are not a semantic domain for G8. `GeoImplicit`
does expose polynomial evaluation and derivative facilities, but coverage and
degeneration characterization are required before promotion. Both families
therefore remain Level C candidates.

### Locus–locus

Two semantic curves require a two-parameter solver for
`F(t)-Q(u)=0`, paired source revisions, two branch/component bindings, and a
two-sided identity policy. This is outside the proposed G8B minimum.

## 4. Solver pipeline

### 4.1 Capture

At the start of `compute()`, capture both source identities and revisions, the
Locus V2 definition snapshot, target state, policy version, tolerance policy,
and work budget. No stage may mix evidence from different revisions.

### 4.2 Capability selection

Choose the strongest *available and truthful* capability in this order:

1. an authoritative exact/analytic target-specific operation;
2. certified interval or enclosure-based isolation/refinement;
3. derivative-aware safeguarded one-dimensional numerical solving;
4. evaluator-only adaptive search with explicitly limited completeness; or
5. `UNSUPPORTED`/`UNRESOLVED`.

Higher placement in the list is not an automatic implementation priority.
G8A must establish whether each capability exists in the pinned upstream
baseline and what guarantee it actually provides.

### 4.3 Candidate isolation

Isolation operates separately on every valid semantic component. It must
cover included endpoints and the canonical periodic seam exactly once. A
sign-change bracket is one kind of candidate, not a complete isolation method.
Tangency probes require residual-minimum, derivative, interval, or equivalent
evidence capable of detecting even-multiplicity roots.

Adaptive semantic samples or spatial bounds may accelerate isolation. They are
never results: every candidate is refined in the original parameter and then
independently verified. A negative broad phase may imply `EMPTY_COMPLETE` only
when its exclusion guarantee is itself established for the target/component.

### 4.4 Refinement and verification

Refinement stays inside the candidate's component and records method,
iterations, evaluations, final interval, and termination reason. Verification
must check:

- finite semantic parameter and successful evaluator status;
- membership in the valid component and in the segment/ray/conic subtype;
- finite evaluated coordinate;
- normalized equation or incidence residual against its own policy;
- coordinate consistency where an independent coordinate construction exists;
- whether multiplicity/contact classification was established or remains
  unknown.

Failure to verify is `UNRESOLVED_NUMERICAL` or another explicit diagnostic, not
`NO_INTERSECTION`.

### 4.5 Semantic deduplication

Deduplicate roots only within one locus branch/component and target binding in
semantic parameter space, using overlapping isolating intervals and the
approved deduplication policy. Distinct preimages at the same coordinate stay
distinct. The two sides of a periodic seam are canonicalized according to the
provider's periodic contract and can retain a lifted continuation coordinate.

## 5. Comparison required in G8A

| Strategy | Strength | Principal risk | Required evidence |
|---|---|---|---|
| Target-specific analytic/exact | Strongest classification when genuinely authoritative | Hidden numeric fallbacks or type-specific inconsistency | Capability provenance, residual recheck, degeneration cases |
| Certified interval/subdivision | Can establish coverage and even roots | Dependency/complexity and interval-extension quality | Proof boundary, enclosure behavior, bounded-work failures |
| Derivative-aware safeguarded 1D | Natural for `h_j(t)` and tangency refinement | Derivative absence/instability and missed candidates | Independent isolation, derivative guarantee, multiple-root probes |
| Evaluator-only adaptive | Broadest Locus V2 compatibility | Cannot generally prove no roots or complete tangency coverage | Honest partial/unresolved statuses and adversarial tests |
| Spatial bounds/index broad phase | Repeated-query acceleration | False negatives or accidental sample authority | Conservative bounds, independent refinement, cache-off equality |
| Two-parameter solver | Necessary for locus–locus | Much larger topology/identity surface | Separate Level C characterization |

The proposed G8B architecture supports a hierarchy, not one convenience
solver. Unsupported capability is a legitimate rich outcome.

## 6. Dynamic root continuation

Continuation is scoped to one active algorithm, ordered source pair, policy,
and topology epoch. Each finite root owns an opaque token plus:

```text
source identities and current revisions
locus branch and valid-component binding
canonical semantic parameter and isolating interval
optional lifted periodic parameter
contact/domain classification evidence
parent/child lineage for an explicit topology event
```

For a topology-preserving perturbation, a previous root predicts a semantic
interval on the corresponding branch lineage. Re-association is permitted only
when exactly one verified current root satisfies the approved semantic
continuation test. Coordinates may be a diagnostic cross-check but never the
matching key.

Merge, split, seam, and invalidation events are explicit:

- two transverse parents merging at a tangent produce a new merge-event root;
- a tangent root splitting produces two children rather than reusing one slot
  arbitrarily;
- a seam crossing preserves identity through canonical/lifted parameter
  semantics when the provider declares a periodic seam;
- a component split, disappeared branch, invalid gap, or incompatible target
  revision ends the old token unless G6 branch lineage proves a unique
  continuation;
- obsolete results become stale before current computation begins and cannot
  be republished after a failed recomputation.

If G8A cannot make a continuation case deterministic, G8B must expose it as an
unsupported topology transition.

## 7. DAG and publication lifecycle

The proposed lifecycle follows the useful G7 P1 precedent without reusing its
metric result type or index:

1. `setInputOutput()` registers the Locus V2 and target `GeoElement` inputs;
2. `compute()` starts a monotonically ordered computation context;
3. the previous success is no longer current as soon as either source revision
   changes;
4. all work occurs in private immutable builders/state;
5. exactly one complete current-revision success, empty, overlap, unsupported,
   or failure snapshot is published;
6. exceptions publish one coherent diagnostic snapshot and no partial roots;
7. removal releases any owner lease and bounded continuation state.

Ordinary geometric absence is a successful complete set result with zero
solutions. It is neither an exception nor an undefined magic value.

## 8. Variable point outputs

The current upstream `OutputHandler` naturally grows and marks unused outputs
undefined; Classic intersection algorithms also use slot/order and
coordinate-near heuristics. Those rules cannot define G8 identity.

If the author later approves ordinary points, the safest minimum is a separate
adapter with a fixed approved maximum number of active slots. Slots are bound
to root tokens for the current topology epoch. Unused slots are undefined;
capacity exhaustion makes the adapter incomplete/unsupported and leaves the
rich result intact. Labels attach to slots only after the policy is explicitly
approved. Historical roots or slots cannot grow without a deterministic cap.

## 9. State ownership and G7 boundary

G8 may reuse the evaluator/session infrastructure and G6
`NumericGuarantee` vocabulary. It must not use `LocusMetricSharedOwner2D`,
`LocusMetricComponentState2D`, cumulative metric partitions, or the G7 metric
index as intersection truth.

Start G8A and the minimum G8B candidate query-local. Introduce an
intersection-specific revision-scoped owner only if measurements show a
repeated-query benefit and the author approves:

- the complete immutable key;
- conservative payload semantics;
- current-revision-only retention;
- capacity and deterministic eviction;
- one-Construction lifecycle and removal behavior;
- no hidden dependency edges; and
- semantic equality with cache disabled.

## 10. Candidate productive package after approval

Prefer GeoCeDG-owned classes under a package such as
`org.geocedg.common.kernel.locus.intersection`, plus a focused algorithm and
rich Geo under existing GeoCeDG packages. The exact files are a G8A output,
not a commitment.

The minimum productive edit should not touch `CmdIntersect`,
`AlgoDispatcher`, `GeoFactory`, XML handlers, legacy `GeoLocus`, Classic
intersection algorithms, 3D dispatch, rendering, export, or public `Path`
interfaces. An append-only `GeoClass` entry and an exhaustive drawing switch
test may be unavoidable only if the rich-Geo option is approved.

## 11. Approval gate

Before productive G8B work, the author must approve the normative contract,
ADR, rich-result shape, continuation semantics, support matrix, numerical
capability hierarchy, tolerance/work policy, and exact candidate edit set.
Until then every item in this document remains a characterization hypothesis.


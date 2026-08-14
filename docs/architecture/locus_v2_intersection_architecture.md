# Locus V2 2D intersections — author-approved architecture

| Field | Value |
|---|---|
| Status | **G8A AUTHOR-APPROVED G8B ARCHITECTURE — NOT IMPLEMENTED** |
| Roadmap phase | G8 planning `PASS`; G8A `PASS — AUTHOR APPROVED`; G8B `AUTHORIZED / NOT STARTED` |
| Semantic model | [`locus_v2_intersection_semantic_model.md`](locus_v2_intersection_semantic_model.md) |
| Normative contract | [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) |
| Upstream audit | [`locus_v2_intersection_upstream_impact.md`](locus_v2_intersection_upstream_impact.md) |
| Date | 2026-08-14 |

This document turns the normative G8 semantics and Accepted ADR 0008 into the
author-approved architecture for a separately executed G8B. It authorizes no
work outside the internal source boundary and does not add public commands,
ordinary `Path` behavior, persistence, or dispatcher integration.

## Fundamental CeDG capability

First-class Locus V2 intersection is structural CeDG infrastructure. A
locus-defined projection must be able to intersect a supported ordinary 2D
entity, produce semantically identified solution entities, and feed later
construction steps through normal dynamic propagation whenever continuation is
unambiguous:

```text
CeDG construction -> Locus V2 projection -> identified intersection solution
    -> downstream CeDG construction -> normal DAG update
```

The architecture must retain constructive source, branch/component/preimage,
identity, topology, and degeneration information; an anonymous instantaneous
coordinate is not an acceptable result. This requirement does not widen the
initial family scope beyond line, segment, ray, and circle without new evidence
and author approval.

## 1. Placement and dependency direction

Productive intersection truth belongs in the shared Java kernel because it
changes incidence, participates in
the construction DAG, and must remain valid for non-render consumers.
Characterization probes,
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
AlgoLocusIntersectionV2 -> GeoLocusIntersectionResult
                         |
                         v
required internal point consumer selected by semantic root token
```

There is deliberately no arrow from `LocusRenderCache2D`, render vertices,
legacy `GeoLocus.myPointList`, viewport, zoom, DPI, or pixel tolerances into
this graph.

## 2. Responsibility split

Names are conceptual until G8B implements the approved candidate API.

| Responsibility | Candidate component | Contract |
|---|---|---|
| Immutable request | `LocusIntersectionQuery2D` | Binds source snapshots, target family, policy/version, tolerances, budgets, and requested support level |
| Target authority | `IntersectionTarget2D` adapters | Exposes only a representation already authoritative for the actual target type |
| Evaluation | existing `LocusEvaluationSession2D` | Evaluates one captured Locus V2 revision; retains existing bounded/cycle-safe behavior |
| Capability choice | `LocusIntersectionCapability2D` | Selects analytic, certified, derivative-aware, or evaluator-only behavior without inflating guarantees |
| Isolation | `RootIsolation1D` | Produces semantic-parameter intervals plus explicit evidence for whether query completeness was or was not established |
| Refinement | `RootRefiner1D` | Refines an isolated candidate within its valid component and work budget |
| Verification | `IntersectionVerifier2D` | Re-evaluates semantic geometry, target membership, normalized residual, and domain membership independently of broad phase |
| Classification | `IntersectionClassifier2D` | Reports contact and domain-location evidence without converting unknown into transverse or absent |
| Continuation | `IntersectionContinuation2D` | Associates roots within one source-pair/topology context using constructive/branch lineage and proven continuation relations; parameters/intervals are revision evidence |
| Immutable result | `LocusIntersectionResult2D` | Publishes query-level state plus zero or more rich solution records atomically |
| DAG owner | `AlgoLocusIntersectionV2` | Registers both geometric inputs and replaces the entire current-revision snapshot on recompute |
| Rich Geo | `GeoLocusIntersectionResult` | Makes success, absence, unresolved, overlap, and stale outcomes inspectable in the normal DAG |
| Required point consumer | dedicated derived algorithm | Projects one selected token from a current complete finite set to an ordinary point; never solves or retargets |

The rich result is the authority. A point adapter cannot improve a guarantee,
discard an unresolved case and call the remainder complete, or manufacture a
finite representative for overlap.

`IntersectionCompleteness` is a mandatory axis independent of computation,
per-root residual validity, numeric guarantee, geometry kind, identity, and
currentness. Its values are `COMPLETE`, `INCOMPLETE`, and
`NOT_ESTABLISHED`. In particular, three verified roots do not prove that a
fourth was excluded, and no point adapter may hide that distinction.

## 3. Target adapters and minimum capability surface

G8B implements family-specific adapters rather than one universal implicit
conversion layer.

### Lines, segments, and rays

Use the line's homogeneous incidence equation for the residual. Segment and
ray membership add their existing finite/half-line restriction after the
supporting-line root is refined. Included endpoints are classified explicitly.
The adapter uses signed perpendicular model-coordinate distance, invariant
under nonzero coefficient scaling.

### Circles

Use the conic's existing matrix/evaluation authority after establishing the
actual nondegenerate circle type. The adapter exposes a signed
radial-distance-equivalent residual in model coordinates. It must not silently
reinterpret another or degenerate conic as a circle. Circle-specialized
analytic or derivative evidence may be used only when derived from the same
authoritative data. Full conics remain deferred.

### Functions and implicit curves

`GeoFunction` is not admitted merely because a view interval can be sampled;
its view-clipped root paths are not a semantic domain for G8. `GeoImplicit`
does expose polynomial evaluation and derivative facilities, but completeness and
degeneration characterization are required before promotion. Both families
therefore remain Level C candidates.

### Locus–locus

Two semantic curves require a two-parameter solver for
`F(t)-Q(u)=0`, paired source revisions, two branch/component bindings, and a
two-sided identity policy. This is outside the approved G8B minimum.

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
G8A established whether each capability exists in the pinned upstream
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

Residual evidence declares quantity kind, units, raw value, normalization and
adapter provenance. A common tolerance is used only for compatible normalized
quantities; otherwise the adapter supplies a family-specific typed contract.
Root/dedup/continuation tolerances remain in provider-declared semantic
parameter units. The tangency threshold applies only to a normalized contact
indicator, preferably the derivative of model-distance residual with respect
to source arc length for a regular source. Raw equation/parameter derivatives
are never compared across scaling or reparameterization.

### 4.5 Semantic deduplication

Deduplicate roots only within one locus branch/component and target binding in
semantic parameter space, using overlapping isolating intervals and the
approved deduplication policy. Distinct preimages at the same coordinate stay
distinct. The two sides of a periodic seam are canonicalized according to the
provider's periodic contract and can retain a lifted continuation coordinate.

## 5. G8A capability comparison

| Strategy | Strength | Principal risk | Required evidence |
|---|---|---|---|
| Target-specific analytic/exact | Strongest classification when genuinely authoritative | Hidden numeric fallbacks or type-specific inconsistency | Capability provenance, residual recheck, degeneration cases |
| Certified interval/subdivision | Can establish completeness and even roots | Dependency/complexity and interval-extension quality | Proof boundary, enclosure behavior, bounded-work failures |
| Derivative-aware safeguarded 1D | Natural for `h_j(t)` and tangency refinement | Derivative absence/instability and missed candidates | Independent isolation, derivative guarantee, multiple-root probes |
| Evaluator-only adaptive | Broadest Locus V2 compatibility | Cannot generally prove no roots or complete tangency isolation | Honest `INCOMPLETE`/`NOT_ESTABLISHED` statuses and adversarial tests |
| Spatial bounds/index broad phase | Repeated-query acceleration | False negatives or accidental sample authority | Conservative bounds, independent refinement, cache-off equality |
| Two-parameter solver | Necessary for locus–locus | Much larger topology/identity surface | Separate Level C characterization |

The approved G8B architecture supports a hierarchy, not one convenience
solver. Unsupported capability is a legitimate rich outcome.

G8A found that analytic/factorization and certified exclusion can establish
completeness only with explicit coverage evidence; derivative-aware work finds
even contact but needs an independent root-count/isolation argument;
evaluator-only work cannot establish exhaustiveness; and a broad phase is safe
only as independently proved conservative candidate isolation. Two-parameter
solving remains deferred.

## 6. Dynamic root continuation

Continuation is scoped to one active algorithm, ordered source pair, policy,
and topology context. Each finite root owns an opaque token. Its candidate
durable/continuation information is:

```text
source-pair identity
constructive intersection lineage
applicable branch lineage
topology/continuation context
explicit continuation relation when established
```

Its separate revision-scoped localization evidence is:

```text
source identities and current revisions
valid-component binding for those revisions
canonical semantic parameter and isolating interval
optional lifted periodic parameter
residual, contact, method, and solver/certificate evidence
```

An isolating interval is localization/certification evidence, not fundamental
durable identity. For a topology-preserving perturbation, re-association is
permitted only when exactly one verified current root satisfies an approved
semantic continuation relation. G8A tested ordinary motion, known equivalent
monotone reparameterization, allowed orientation reversal, and periodic-seam
representation. A changed parameter or interval alone cannot force a new
geometric identity. Coordinates may be diagnostic but never the matching key.

Universal merge/split genealogy is rejected. The accepted topology policy is:

- candidate parent/child lineage is recorded across `2 roots -> tangent root ->
  2 roots` and reverse traversal when it can be established robustly;
- symmetric child correspondence, seam interaction, and nearby
  branch/component changes must expose ambiguity or identity discontinuity
  when continuation is not unique;
- a seam crossing preserves identity through canonical/lifted parameter
  semantics when the provider declares a periodic seam;
- a component split, disappeared branch, invalid gap, or incompatible target
  revision ends the old token unless G6 branch lineage proves a unique
  continuation;
- obsolete results become stale before current computation begins and cannot
  be republished after a failed recomputation.

The universal genealogy hypothesis failed symmetric/reverse cases, so the
accepted contract is narrower. Cases outside it expose
`IDENTITY_DISCONTINUITY`, or `NOT_ESTABLISHED`-equivalent status, never a
coordinate-based guess.

## 7. DAG and publication lifecycle

The accepted lifecycle follows the useful G7 P1 precedent without reusing its
metric result type or index:

1. `setInputOutput()` registers the Locus V2 and target `GeoElement` inputs;
2. `compute()` starts a monotonically ordered computation context;
3. the previous success is no longer current as soon as either source revision
   changes;
4. all work occurs in private immutable builders/state;
5. exactly one coherent current-revision success, empty, overlap, unsupported,
   or failure snapshot with an explicit completeness axis is published;
6. exceptions publish one coherent diagnostic snapshot and no partial roots;
7. removal releases any owner lease and bounded continuation state.

Ordinary geometric absence is a successful complete set result with zero
solutions. It is neither an exception nor an undefined magic value.

## 8. Required token-selected point consumer

The current upstream `OutputHandler` naturally grows and marks unused outputs
undefined; Classic intersection algorithms also use slot/order and
coordinate-near heuristics. Those rules cannot define G8 identity.

G8B includes a separate internal `AlgoLocusIntersectionPointV2`-style consumer
for one selected semantic root token. It has one ordinary point output and no
solver, identity or cache. It consumes the rich Geo through a normal DAG edge
and is defined only for the selected token in a current successful complete
finite set.

If that token disappears, is stale, or has ambiguous continuation, the point
becomes coherently undefined. It never selects a replacement by coordinates,
slot/order or labels. It may recover only when the same token becomes current
again under the approved lifecycle contract. A variable-size public point
array and public label/slot policy remain outside G8B.

## 9. State ownership and G7 boundary

G8 may reuse the evaluator/session infrastructure and G6
`NumericGuarantee` vocabulary. It must not use `LocusMetricSharedOwner2D`,
`LocusMetricComponentState2D`, cumulative metric partitions, or the G7 metric
index as intersection truth.

The minimum G8B implementation is query-local. It contains no shared
intersection owner or index. A later proposal may introduce one only if new
measurements show a repeated-query benefit and the author separately approves:

- the complete immutable key;
- conservative payload semantics;
- current-revision-only retention;
- capacity and deterministic eviction;
- one-Construction lifecycle and removal behavior;
- no hidden dependency edges; and
- semantic equality with cache disabled.

G8A measured exact linear query-local work for 1/3/10/100 consumers and depth
1–3, with zero retained intersection entries. That evidence supports the
accepted no-owner/no-index minimum.

## 10. Author-approved G8B productive package

Prefer GeoCeDG-owned classes under
`org.geocedg.common.kernel.locus.intersection`, plus a focused algorithm and
rich Geo under existing GeoCeDG packages. The exact candidate signatures and
files are recorded in
[`locus_v2_intersection_api.md`](../developer/locus_v2_intersection_api.md).
Exact Java spelling may adapt to source conventions without changing the
normative semantic roles.

The minimum productive edit should not touch `CmdIntersect`,
`AlgoDispatcher`, `GeoFactory`, XML handlers, legacy `GeoLocus`, Classic
intersection algorithms, 3D dispatch, rendering, export, or public `Path`
interfaces. One append-only `GeoClass.LOCUS_INTERSECTION_RESULT`-equivalent
entry and its exhaustive-type/drawing tests are authorized if required by the
rich Geo; no unrelated type-system edit is authorized.

## 11. Execution gate

The rich-result/normal-DAG architecture, required token-selected point
consumer, query-local state, core-four scope, normalized tolerance/work policy,
narrow identity/topology contract and closed public boundaries are author
approved. The specification is normative and ADR 0008 is Accepted. G8B is
authorized but not started; productive editing begins only through a separate
explicit execution of the canonical G8B prompt after its repository entry
gates reproduce.

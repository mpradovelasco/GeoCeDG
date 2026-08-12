# Locus V2 — mathematical and semantic model

| Field | Value |
|---|---|
| Status | **G6 PASS / G6R PASS** — normative model unchanged; implementation hardened |
| Phase | G6A `PASS`; G6B `PASS`; G6R `PASS`; G7/G8/G9 not started |
| Date | 2026-08-12 |
| Scope | Two-dimensional dynamic loci only |
| Excluded | Public length (G7), intersections (G8), spatial semantics (G9) |

The author approved the characterized model at G6A closeout. This document
explains the normative contract without changing the meaning of the legacy
GeoGebra `Locus` command. The authority is
[the normative semantic specification](../../geocedg/specs/locus/locus-v2-semantics.md).
The execution plan is
[G6 Locus V2 plan](../roadmap/g6_locus_v2_plan.md); the baseline implementation
evidence is recorded in the
[upstream impact map](locus_v2_upstream_impact.md).
The post-G6 implementation and lifecycle details are in the
[G6R implementation architecture](locus_v2_implementation.md); that document
does not alter this mathematical contract.

## 1. Scientific purpose and non-goals

Locus V2 represents the image of a dynamic construction, not the finite point
list chosen to draw that image. Its semantic state must therefore be
independent of zoom, DPI, viewport, render tolerance and graphical time
budget. The display remains free to discretize that state.

The local scientific corpus uses loci as constructive carriers for surface
intersections and developments. It also records the limits of the historical
workflow: sampled chord sums, filtering with `postLocus`, concatenated sampled
loci and view-dependent parameter coverage. These results are evidence for the
requirements, not an implementation design:

- the [CeDG book](../references/cedg/book/Modelado-parametrico-computacional-v2.pdf)
  relates `PathParameter`, loci, developments and the shortcomings of sampled
  perimeter measurement;
- the [LSIM preprint](../references/cedg/locus-and-intersections/CeDGLocusIntersect_INGEGRAF2022_vFinal_PrePrin.pdf)
  formulates locus-driven intersection procedures and exhibits distinct
  leaves and topology changes;
- the [surface-intersection paper](../references/cedg/locus-and-intersections/symmetry-15-00984-with-cover.pdf)
  supplies multileaf and dynamic intersection cases;
- the [oblique-cone/tool study](../references/cedg/developments/978-3-031-72829-7_81.pdf)
  characterizes `postLocus`, sampled length tools and accumulated error in
  concatenated loci;
- the [developable-surface manuscript](../references/cedg/developments/DevelopableRuledSurfaces_Rev.pdf)
  distinguishes continuous constructions from explicitly discrete
  approximations;
- the [discrete-model paper](../references/cedg/discrete-models/1-s2.0-S1524070324000419-main.pdf)
  supplies dynamic models whose discrete design parameter changes the
  construction topology.

G6 does not promise a symbolic closed form for every construction, nor does it
make an arbitrary sampled output exact. It establishes a stable semantic
question: given a branch and a valid driver state, evaluate the dependent point
deterministically and report the quality and validity of that result.

## 2. Mathematical object

Let `B = {b_1, ..., b_m}` be the set of semantic branches recognized by a
versioned driver-domain provider. Each branch `b_j` has a declared oriented
driver domain `Omega_j`. Its valid subset is

\[
V_j = \Omega_j \setminus D_j
    = \bigcup_{k=1}^{n_j} V_{j,k},
\]

where `D_j` contains driver states for which the driver or dependent
construction is invalid. The `V_{j,k}` are valid domain components. They do
**not** automatically create new branch identities. The evaluator of branch
`b_j` is

\[
F_j : V_j \longrightarrow \mathbb{R}^2,
\]

and the locus image is

\[
L = \bigcup_{j=1}^{m} F_j(V_j).
\]

This formula is insufficient unless the following data are part of the
contract:

- a versioned semantic parameter supplied by the driver-domain provider;
- declared branch domain, valid components, endpoint policy, orientation and
  optional periodic equivalence;
- invalid subdomains and their reason;
- stable branch key, constructive provenance and lifecycle/lineage;
- the construction revision against which `F_j` is evaluated;
- evaluator determinism category, validity, exactness and numeric guarantee.

Consequently Locus V2 is not merely the set `L`. It is an oriented,
parameterized dynamic construction whose image is `L`.

### 2.1 Declared branch domain and valid components

`Omega_j` is the declared driver state space for one semantic solution/leaf.
`V_j` is the subset at which its construction currently evaluates. Neither is:

- the range of screen pixels;
- a render sampling interval;
- the current visible interval of an unbounded function;
- the index range of `myPointList`;
- a physical arc-length parameter unless the provider declares that fact.

Each valid domain component records lower and upper endpoints in semantic
parameter space, whether each endpoint is included or finite, and any explicit
periodic-equivalence metadata. An isolated valid driver state is a
zero-dimensional component rather than a fabricated interval. A discontinuity
can therefore divide `V_j` into several components while `branchKey` continues
to identify the same constructive branch.

### 2.2 Parameter value versus geometric point

The semantic address of an evaluated locus point is `(locusIdentity,
branchKey, t, revision)`. Its Cartesian result is `F(branchKey, t)`. These are
different identities:

- multiple parameter values may map to the same geometric point at a
  self-intersection;
- a periodic branch may map equivalent endpoints to the same point;
- a collapsed image may map a domain interval to one point;
- a coincident point not created from the locus has no unique preimage.

Future point-on-locus, length and intersection behavior must retain or return
the semantic preimage. Coordinate equality alone may not select one silently.

### 2.3 Versioned semantic parameter

**The semantic parameter is supplied by a versioned Locus V2 driver-domain
provider. It may coincide with the underlying GeoGebra native parameter only
when that provider explicitly declares the parameter suitable and stable.**

It is generally not intrinsic to the image curve. Reparameterizing a branch
can preserve `L` while changing orientation, cross-projection correspondence
and future metric integration.

The baseline has two representations that are evidence, not automatic V2
authority:

- `PathParameter.t` stores a path-specific native parameter (`[-pi, pi]` for a
  circle/ellipse, `[0,1]` for a segment, extended intervals for unbounded
  paths, and special branch encodings for some conics);
- the public `PathParameter[...]` command applies `PathNormalizer` and returns
  a traversal coordinate in `[0,1]`.

Neither becomes semantic identity by default. A provider may wrap a proven
stable native parameter or define a different parameter with explicit mapping
to the underlying driver. Finite normalized endpoints `0` and `1` for an
infinite native interval remain traversal boundaries, not finite geometric
states.

This provider boundary also allows two future CeDG projections to share one
semantic parameter even when their internal two-dimensional `Path` objects use
different native parameterizations. G9 must validate that correspondence; G6
only preserves the seam.

Periodicity is declared by the provider. It is never inferred from matching
sampled endpoints or from an animation mode that happens to loop.

## 3. Drivers

Two historical driver families share an evaluation protocol but not all
semantics.

### 3.1 Point constrained to a `Path`

The locus is driven by a point whose native path parameter changes. The
conceptual `PathLocusDriver2D` contract must provide:

- a stable reference to the path and moving point;
- a versioned semantic parameter descriptor and mapping, if needed, to the
  underlying path parameter;
- a declared branch domain, valid components and endpoint policy;
- evaluation of the moving point at a semantic parameter;
- declared topology and periodicity capabilities.

The existing `Path` interface alone is not sufficient for V2 branch semantics:
it exposes only one minimum/maximum pair and no branch descriptors. Each G6B
path type therefore requires an explicitly characterized provider.
The current `GeoFunction.getMinParameter()` / `getMaxParameter()` values are
view-dependent even when a function interval exists (the declared interval is
intersected with the view range). That `Path` domain is therefore a mandatory
rejection. A function with an explicit, construction-owned interval can be
supported only through a type-specific provider that reads the declared
interval independently of those view-clipped `Path` bounds.

### 3.2 Numeric parameter or slider

The locus is driven by a number over a declared numeric interval. The
conceptual `NumericLocusDriver2D` contract must provide the interval, endpoint policy,
orientation and numeric update operation. Slider animation style (increasing,
oscillating or wrapping) is presentation/traversal state and does not declare
the domain periodic.

### 3.3 Common contract and non-equivalence

Both can implement a small versioned driver-domain provider protocol: declare
semantic branches and domains, map/set a semantic driver state in a controlled
evaluation context, and report valid components. They remain different driver
kinds in metadata and future serialization. G6 must not force a numeric slider
to pretend to be a `Path`, nor discard the geometric constraints of a path
driver.

## 4. Branch model, key and lifecycle

A semantic branch is an identifiable constructive solution/leaf with a
declared domain and one deterministic evaluator policy. It is not automatically
a connected component of `V_j` or of the image. A self-intersecting curve, or a
branch interrupted by undefined states, can retain one identity.

```text
LocusBranch2D
  branchKey                 deterministic semantic descriptor
  declaredDriverDomain
  validDomainComponents[]
  evaluator
  orientation
  provenance
  branchLifecycle / lineage
```

`branchKey` is the stable semantic descriptor. A runtime `branchId` may be a
compact representation of that key, but may not replace its deterministic
meaning. Lifecycle/lineage is separate state describing `UNCHANGED`,
`APPEARED`, `DISAPPEARED`, `SPLIT` or `MERGED` transitions.

The author-approved policy is:

| Event | Working identity rule |
|---|---|
| Ordinary recompute, same constructive solution | Recover the same `branchKey` |
| Geometric crossing | Preserve both keys; screen order and proximity are irrelevant |
| Apparent reversal in the view | Preserve key and semantic orientation |
| Valid-domain gap | Preserve key; update `validDomainComponents[]` |
| Branch disappears and later reappears | Recover the same key when the provider deterministically recognizes the same constructive solution |
| Branch appears with no prior recognized solution | Allocate a new deterministic key and record `APPEARED` |
| Real split | Apply an approved typed split rule and record parent/child lineage |
| Real merge | Apply an approved typed merge rule and record predecessor/result lineage |
| Declared domain merely changes endpoints | Preserve key when the provider declares the same branch and orientation |

Keys must never derive from sample order, display label, creation order among
samples, screen proximity or a hash of floating-point coordinates. Every G6B
provider must define deterministic branch descriptors and typed split/merge
rules; otherwise the case remains unsupported rather than receiving a guessed
identity.

## 5. Validity, properties and degeneration taxonomy

Definition, branch/domain properties, point evaluation, regularity and lineage
belong to different semantic levels and must not be collapsed into one enum.
The separation is normative; final implementation names remain a G6B detail.

### 5.1 Definition status

| Status | Meaning | Observable policy |
|---|---|---|
| `VALID` | The provider published a coherent definition snapshot | Branches and their declared/valid domains are inspectable |
| `EMPTY_DOMAIN` | No valid driver state exists for the definition | Definition remains inspectable; no semantic point is fabricated |
| `DRIVER_INVALID` | Driver/path/slider or its declared domain is undefined | Evaluations are unavailable for this revision |
| `UNSUPPORTED` | The provider cannot supply the approved semantic contract | Explicit diagnostic; no fallback to legacy samples |

### 5.2 Branch and domain properties

Properties describe a branch or valid-domain component; they are not evaluation
errors:

- `FINITE` or `UNBOUNDED` domain;
- `PERIODIC` when explicitly declared by the provider;
- `COLLAPSED_IMAGE` when a nontrivial valid domain maps to one geometric point;
- zero-dimensional/isolated valid component metadata;
- discontinuity boundaries between valid components.

`FINITE` and `UNBOUNDED` are mutually exclusive for one declared endpoint
model; other properties may coexist. An equal-endpoint component is valid only
when the provider explicitly declares an isolated driver state. A sampled gap
is not evidence of a discontinuity, and disconnected valid components do not
automatically create distinct branch keys.

### 5.3 Evaluation status

`evaluate(branchKey,t)` returns an immutable result rather than leaving stale
coordinates:

| Status | Meaning | Observable policy |
|---|---|---|
| `VALID` | A finite point was evaluated at a valid semantic parameter | Return coordinates and all quality metadata |
| `OUT_OF_DOMAIN` | `t` is outside the branch's valid subset | No clamping and no coordinates |
| `DEPENDENCY_UNDEFINED` | The dependent construction is undefined at `t` | No coordinates; retain a diagnostic reason |
| `NON_FINITE` | Evaluation produced NaN or infinity | Reject the result; never insert a drawable point |
| `EVALUATION_FAILED` | Controlled evaluation failed or exceeded its approved execution contract | No partial semantic value is promoted |
| `UNSUPPORTED_NONDETERMINISM` | No approved reproducible pointwise or canonical-continuation rule exists | Reject semantic evaluation for this provider/case |

### 5.4 Optional regularity metadata

When the provider has sufficient differential evidence it may report
`REGULAR`, `SINGULAR` or `UNKNOWN`. `UNKNOWN` is the required value when G6B
lacks that capability. A cusp may therefore be a `VALID` evaluation with
regularity `SINGULAR` or `UNKNOWN`; it is not forced into an error state or a
new branch.

### 5.5 Topology transition and lineage

Between two definition revisions a provider may report `UNCHANGED`,
`APPEARED`, `DISAPPEARED`, `SPLIT` or `MERGED`, together with typed predecessor
and successor branch keys. Valid-domain components have their own topology and
must not create branch-lineage events merely by splitting around an undefined
parameter. Any definition transition publishes a new immutable snapshot and
invalidates derived semantic caches through the normal dependency graph.

## 6. Exactness model

Exactness and numerical assurance have four orthogonal axes and must be
reported rather than collapsed into one boolean.

### 6.1 Definition fidelity

- `CONSTRUCTED_SEMANTIC`: the evaluator executes the approved dynamic
  construction and the locus is defined by that construction;
- `APPROXIMATE_CONSTRUCTION`: an input procedure is already a declared
  approximation, such as a discrete model with a chosen resolution;
- `UNKNOWN`: evidence is insufficient.

This axis does not claim exact real arithmetic.

### 6.2 Evaluation method

- `ANALYTIC_OR_SYMBOLIC`: a supported closed or symbolic evaluator exists;
- `DETERMINISTIC_NUMERIC`: the approved construction is evaluated with
  deterministic floating-point operations;
- `CONTROLLED_APPROXIMATION`: an algorithm returns a declared tolerance/error
  estimate;
- `UNCONTROLLED_OR_UNKNOWN`: unsuitable for promotion.

### 6.3 Representation role

- `SEMANTIC_EVALUATION`: a point returned by the branch evaluator;
- `METRIC_APPROXIMATION`: future G7 index/quadrature data;
- `INTERSECTION_APPROXIMATION`: future G8 isolation/refinement data;
- `RENDER_DISCRETIZATION`: view-specific tessellation only.

Increasing a point count changes only the last category. Java `double`
evaluation is not described as mathematically exact merely because the
defining construction is exact in Euclidean geometry.

### 6.4 Numeric guarantee

The conceptual value type is `NumericGuarantee` with these working values:

- `EXACT_ARITHMETIC`: the returned coordinates are represented by an approved
  exact-arithmetic mechanism, not merely by an exact-looking formula;
- `CERTIFIED_ERROR_BOUND`: an absolute/relative error bound is certified for
  this result;
- `ESTIMATED_ERROR`: an explicit but non-certified estimate accompanies the
  result;
- `FLOATING_POINT_UNCERTIFIED`: deterministic floating-point evaluation has no
  certified or estimated coordinate-error bound.

An analytic expression evaluated with Java `double` is normally
`ANALYTIC_OR_SYMBOLIC` on the evaluation-method axis and
`FLOATING_POINT_UNCERTIFIED` on this axis. It does not produce mathematically
exact coordinates merely because its construction fidelity is semantic.

## 7. Evaluator contract

The minimal conceptual API is:

```text
LocusDefinition2D
  semanticVersion()
  revision()
  driver()
  branches()

LocusEvaluator2D
  branch(branchKey) -> LocusBranch2D
  declaredDomain(branchKey) -> LocusDriverDomain
  validDomainComponents(branchKey) -> LocusDomainComponent[]
  evaluate(branchKey, semanticParameter) -> LocusEvaluation2D
  validity(branchKey, semanticParameter) -> LocusEvaluationStatus
```

`LocusEvaluation2D` is immutable and contains locus identity, branch key,
semantic parameter, semantic revision, evaluation status, finite world
coordinates only when valid, the four quality axes, optional regularity and
diagnostics. Any mapping to a native GeoGebra parameter remains provider
metadata, not the address returned to clients.

The core law is referential consistency within one revision:

\[
E(r,j,t) = E(r,j,t)
\]

independently of view state and of the external order in which other parameters
were requested.

### 7.1 Determinism categories

Each provider/case must be classified as exactly one of:

- `POINTWISE_DETERMINISTIC`: `evaluate(branch,t)` can be calculated directly
  and is independent of evaluation history;
- `CANONICAL_CONTINUATION_DETERMINISTIC`: evaluation needs continuation or
  solution selection, but a declared canonical anchor, semantic orientation
  and deterministic continuation rule make `F(branch,t)` reproducible
  independently of the caller's query history;
- `UNSUPPORTED_NONDETERMINISM`: no approved reproducible rule exists.

The second category may use internal continuation state as an implementation
detail, but a fresh evaluation session and a warmed session must return the
same semantic result. A CeDG construction must not be rejected solely because it
needs continuation when such a canonical rule can be specified and validated.

### 7.2 Deferred capabilities

Derivatives, tangents and interval bounds are useful to G7/G8 but need not be
mandatory in G6B. The interface should expose capability queries or optional
subinterfaces, not placeholder numeric values:

```text
LocusDifferentialEvaluator2D.derivative(branchKey, t)
LocusDifferentialEvaluator2D.tangent(branchKey, t)
LocusBoundsEvaluator2D.bounds(branchKey, interval, tolerance)
```

G6B may implement a capability for an analytic test provider only when needed
to prove the extension seam. Render subdivision can initially use world-point
deviation transformed solely inside the render layer.

## 8. Dependency-graph integration

The V2 object is an `AlgoElement` output and its inputs must include the
driver, dependent point and every construction input needed by the evaluated
dependency slice. Normal GeoGebra dependency propagation remains authoritative.

Legacy `AlgoLocusND` clones a dependency slice into a `MacroKernel` once and
updates that controlled construction for samples. G6 may reuse that mechanism
only behind an explicit `DependencySliceEvaluationContext2D` with these rules:

1. build the slice from the same construction dependencies as the main graph;
2. synchronize its source values once per main-graph recompute;
3. never rebuild a second informal graph for every evaluation;
4. evaluate `(branch,t)` using its pointwise or canonical-continuation
   determinism contract;
5. never publish mutable macro objects or render samples as semantic data;
6. invalidate the complete V2 snapshot if synchronization/evaluation fails;
7. preserve existing single-thread/kernel-thread confinement in G6B.

The baseline has no suitable global construction-revision token. G6B should
give each `AlgoLocusV2` output a monotonic local semantic revision incremented
only when its normal `compute()` publishes a new immutable definition snapshot.
`Construction.getStep()` is construction-protocol position and must not be
misused as a revision counter.

If G6A cannot define either pointwise determinism or canonical continuation for
a selected construction, that case is excluded from G6B and reported as
`UNSUPPORTED_NONDETERMINISM`. Creating an unrelated parallel graph or
perturbing the live construction for every query is not acceptable.

## 9. Nested Locus V2 semantic composition

Nested composition is a first-class semantic requirement, distinct from an
ordinary long algorithm chain and from concatenating already sampled loci. For

```text
L1(s) = F1(s)
L2(t) = F2(t, L1(phi(t)))
L3(u) = F3(u, L2(psi(u)))
```

the required call structure is conceptually:

```text
evaluate L3(u)
  -> evaluate L2(psi(u))
       -> evaluate L1(phi(psi(u)))
```

A downstream `GeoLocusV2` may consume an upstream V2 locus only through its
branch/domain descriptors, semantic evaluator, revision, validity and quality
metadata. It must not consume render vertices, sampled polylines or
`LocusRenderCache2D`; regenerate/tessellate the complete upstream locus; or
reconstruct the complete upstream dependency slice for each downstream point.
The upstream object is an explicit input in the normal kernel DAG, so this
semantic dependency is neither hidden nor callback-only.

### 9.1 Shared evaluation context and memoization

The accepted minimum requires a scoped abstraction equivalent to a
`LocusEvaluationSession2D`; the final implementation name remains local to
G6B. Its responsibilities are:

- carry one coherent set of semantic revisions through a nested query/batch;
- memoize identical upstream requests during that session using at least
  `(locus identity, semantic revision, branchKey, native semantic parameter)`;
- record outer queries, per-level evaluator calls and duplicate avoidance;
- keep an active evaluation stack so callback cycles fail diagnostically;
- remain bounded and discardable, never becoming a second dependency graph.

The “native semantic parameter” in this key means the provider-owned canonical
representation of its semantic parameter, not a normalized `PathParameter` or
a render sample index. Cache-enabled and cache-disabled execution must produce
semantically identical results.

### 9.2 Dependency-slice strategy

| Strategy | Mechanism | Principal advantage | Principal risk |
|---|---|---|---|
| A. Recursive semantic evaluators with a shared session | Each V2 evaluator calls explicit upstream V2 evaluators; one session shares revisions and memoization | Smallest semantic extension and direct preservation of branch identity | Repeated slice synchronization if session boundaries or ownership are wrong |
| B. Controlled flattening/compilation of the evaluation DAG | Compile compatible nested slices into one evaluation plan while retaining locus boundaries and keys | May remove repeated synchronization at greater depth | Larger compatibility surface, invalidation complexity and accidental duplication of the kernel DAG |

Strategy A is the accepted G6B minimum. Strategy B is deferred and is not
authorized merely as an optimization; a later proposal requires profiling that
shows a reproducible bottleneck and proves that the compiled plan preserves the
normal DAG, revisions and semantic identities.

### 9.3 Invalidation

For `L1 -> L2 -> L3`, a source change in `L1` propagates through normal
`AlgoElement` inputs/outputs. Each locus publishes at most the semantic
revisions caused by its normal recompute, and all affected semantic/session
caches are invalidated. No upstream render tessellation is generated eagerly;
each view rebuilds its render cache only on demand.

G6B must specifically guard against a design in which every level owns a slice
that clones the complete upstream locus and then rebuilds or recursively
synchronizes those slices for each point. Nested evaluation cost may grow with
query count and depth, but never with the product of upstream render densities.

### 9.4 Cycles

The baseline registers `AlgoElement` input/output dependencies in construction
order and exposes ancestor checks such as `GeoElement.isChildOf()` /
`isParentOf()` plus `CircularDefinitionException` in reference-setting paths.
The G6A audit found that those mechanisms do not by themselves prove safety for
callbacks hidden inside evaluators. V2 factories must reject graph cycles, and
any re-entry detected by
a semantic evaluation session must return a typed diagnostic rather than
recurse indefinitely or expose a stale value.

## 10. Semantic data and render data

The required separation is:

```text
LocusDefinition2D + LocusEvaluator2D
                   |
                   | read-only evaluations
                   v
             LocusRenderCache2D(view)
```

`LocusRenderCache2D` may depend on view transform, viewport clipping, DPI,
pixel tolerance, time budget and render policy. It may contain move/line
vertices and may differ between views or zoom levels. It may not define or
modify:

- declared/valid domain components or branch keys;
- `evaluate(branch,t)`;
- future length/integration state;
- future intersection roots;
- quality/numeric-guarantee metadata;
- future export geometry.

An unbounded semantic branch is rendered over an explicit view-dependent
presentation interval without claiming that interval as its domain.

## 11. Minimal cache strategy

G6B should implement only measured caches:

- an immutable definition/domain snapshot keyed by the algorithm's semantic
  revision;
- an optional small bounded evaluation cache keyed by at least
  `(locus identity, semantic revision, branchKey, canonical semantic parameter)` for
  approved deterministic evaluators;
- a scoped, bounded nested-session memo using the full semantic key;
- one render cache per view, keyed by revision, view transform and tessellation
  policy.

All caches are invalidated through normal dependency recompute when an affected
locus publishes a new revision. A downstream invalidation does not eagerly
build an upstream render cache. Bounds and metric indexes are deferred until
G7/G8 demonstrate a need. No global cache, unbounded memoization or new
concurrent execution model is justified for G6B. Memory limits, duplicate
upstream evaluations and hit rates must be measured by the
[benchmark plan](../validation/g6_locus_v2_benchmark_plan.md).

## 12. Fundamental invariants

For each approved analytic/construction case and valid `(j,t)`:

1. **Constructive geometry:** `P = F_j(t)` satisfies the defining relations of
   the source construction within its approved world-coordinate tolerance.
2. **Zoom invariance:** for views `Z1` and `Z2`,
   `F_j(t; Z1) = F_j(t; Z2)` within a screen-independent numeric tolerance.
3. **Determinism:** repeated evaluation of the same revision and parameter is
   semantically equal, including status and metadata.
4. **Render separation:** `R(L,Z1)` may differ from `R(L,Z2)` while the
   definition revision, domains, branches and evaluations remain equal.
5. **Dependency coherence:** changing an input triggers one new semantic
   revision, invalidates derived caches and produces no stale point.
6. **Multiplicity:** distinct preimages at a self-intersection remain
   distinguishable even when their coordinates coincide.
7. **Branch/domain separation:** a valid-domain component split does not change
   `branchKey` unless a provider's typed solution rule declares a real branch
   split.
8. **Nested composition:** downstream evaluation reads upstream V2 semantics,
   never render data or regenerated whole-locus samples; results remain equal
   with and without session memoization.
9. **Nested invalidation:** changing the innermost source invalidates the
   affected chain once through the normal DAG and leaves no stale outer result.

The validation matrix defines case-specific relations and tolerances. Pixel
tolerance is never reused as geometric, metric or intersection tolerance.

## 13. Forward compatibility boundaries

- **G7:** may build a world-coordinate `LocusMetricIndex` by querying domain,
  evaluations and optional differential capabilities. A metric consumed by a
  downstream construction must be semantic-revision-scoped, use normal-DAG
  invalidation and caching, and must not read the render cache, sum sampled
  chords or recompute the whole metric for every query while its upstream
  semantic revision is unchanged.
- **G8:** may isolate/refine roots in provider-owned semantic branch parameters
  and preserve `(branchKey,t)` identity. It must not use screen polylines as
  roots.
- **G9:** may use a shared, declared parameter as correspondence between
  projections; normalized traversal coordinates are insufficient unless the
  schema explicitly approves them.
- **G5 extension:** a future `GeoLocusV2` export adapter may consume branches,
  domain, evaluations and exactness/error metadata. It must never consume
  `LocusRenderCache2D`. No DXF locus support is part of G6.

## 14. Evidence conflicts characterized in G6A

1. The scientific literature sometimes calls the locus procedure/result
   “exact” because it is generated by exact constructions, whereas the current
   Java kernel stores sampled `double` points. The author-approved working
   four-axis quality model resolves the vocabulary without upgrading numerical
   evidence; G6A validated it against the selected cases.
2. The book-era reports of `PathParameter(Locus)` behavior differ from the
   present baseline, which implements sample-index path behavior. Both support
   the same conclusion—samples are not a semantic parameter—and G6A records
   the version context rather than declaring either account universally current.
3. The roadmap and `AGENTS.md` name conceptual components, but current code has
   no semantic branch provider and no runtime shared-kernel feature-flag
   service. G6B must add minimal mechanisms rather than assume they exist.
4. `kernel.isContinuous()` lets legacy sampling depend on prior traversal.
   the normative model distinguishes pointwise deterministic cases, cases with
   a provable canonical continuation, and unsupported nondeterminism.
5. The author's observation that a third legacy locus level became practically
   intractable is reproduced by the supplied cone-cylinder pair. A working
   two-level outer slice contains one inner locus and one sampled perimeter;
   each third-level `Flatten` slice contains two inner loci and two perimeters.
   `AlgoLocusSliderND` invokes that macro slice through `copyP.updateCascade()`
   for outer samples, exceeds its 500 ms per-step guard and returns undefined.
   This demonstrated mechanism is model-specific evidence, not a universal
   complexity claim for every legacy nesting pattern.
6. The completed switch/contract audit showed that reusing `GeoClass.LOCUS`
   would opt V2 into legacy casts, defaults, `Path`, metrics, commands and 3D
   dispatch. G6B therefore uses a distinct appended classification, preserving
   all existing ordinals and claiming neither `isGeoLocus()` nor
   `isGeoLocusable()`.
7. The author approved the scale-aware envelope
   `max(1e-12*max(1,S), 64*ulp(max(1,S)))` for uncertified G6B numeric
   comparison only. `S` is a documented characteristic geometric scale for
   the case and cannot depend on screen state or absolute origin offset.
   Absolute performance timings remain informational pending G6B measurements.

The normative specification resolves these conflicts through versioned
providers, independent state/quality axes, explicit determinism classes and
the accepted compatibility boundary. ADR 0006 is `Accepted`.

## 15. G6A evidence and recommended disposition

G6A added only test-private mathematical fixtures and read-only legacy probes.
It introduced no productive V2 object. The saved baseline and exact command are
in
[`g6a-characterization-baseline.yml`](../../geocedg/validation/locus-v2/g6a-characterization-baseline.yml).

| Question | Evidence | Author-approved G6 closeout decision |
|---|---|---|
| Semantic parameter | Native circle parameter is `[-pi,pi]`; a function's path domain changed from `[-5,5]` to `[-100,100]` with the view; public normalization maps native ranges to `[0,1]` | Provider-owned parameter; only a versioned provider may approve a native mapping. Start G6B with explicit numeric interval plus stable segment/circle/ellipse mappings |
| Branch vs component | The analytic fixture keeps one branch key while its valid subset changes from one interval to two intervals, isolated points and empty | Adopt `LocusBranch2D` with `validDomainComponents[]`; lineage is typed separately |
| Determinism | Five Level-A evaluators produced identical maps for forward, reverse and shuffled queries with residual at most `1e-12` | Support `POINTWISE_DETERMINISTIC`; admit canonical continuation only with anchor/orientation/rule; otherwise return `UNSUPPORTED_NONDETERMINISM` |
| Exactness | All executable references use `double`; no certified interval bound is produced | Record construction fidelity, evaluation method, role and numeric guarantee separately; use `FLOATING_POINT_UNCERTIFIED` for G6B references |
| Nested strategy | Controlled semantic composition made exactly `q*d` calls at depths 1/2/3/5. A scoped session reduced repeated depth-3 requests from 18 to 9 calls without changing results; active-key recursion was detected | Select recursive semantic evaluators plus a scoped session as the minimum; defer controlled DAG flattening until profiling proves need |
| Legacy nesting | The synthetic `Point(Locus)` chain stayed functional and cloned no upstream locus. In the hash-pinned scientific pair, working two-level slices contain one inner locus/perimeter, while third-level `Flatten` slices contain two of each and time out undefined | Treat nested full-locus/perimeter regeneration as a demonstrated legacy anti-pattern; G6B must compose semantic evaluators and never reproduce it |
| Numeric tolerance | Analytic residuals were within `1e-12`; the kernel uses the same order for maximum precision | Use `max(1e-12*max(1,S), 64*ulp(max(1,S)))` only as an uncertified validation envelope with case-documented geometric `S`; provider-domain, render, G7 and G8 tolerances remain separate |
| Performance budget | Controlled legacy median recompute was 0.559/0.946/1.268/1.211 ms at nesting depth 1/2/3/5 on the recorded workstation; timings are noisy and model-specific | Gate deterministic call counts, slice lifecycle and no-render dependencies first. Keep absolute timing informational until author-approved repeatability exists |

The formal topology fixture, accepted tolerance envelope and scientific case provenance
are versioned under `geocedg/validation/locus-v2/`. The local hash-pinned
cone-cylinder pair is accepted as two complementary legacy references. Its
public redistribution remains blocked, and neither model becomes V2 semantic
authority.

## 16. Author-approved closeout disposition

The second author review records:

1. the semantic specification is normative for G6;
2. ADR 0006 is `Accepted`;
3. G6B uses a distinct appended V2 classification and leaves the legacy locus
   predicates and contracts untouched;
4. recursive semantic evaluators plus a scoped shared evaluation session are
   the minimum nested strategy; controlled DAG flattening remains deferred;
5. the scale-aware envelope above is accepted only for uncertified numeric
   comparison; and
6. the hash-pinned two-level model is the functional legacy control, while the
   three-level `Flatten` model is the pathological legacy reference.

The original `.ggb` files remain manual/scientific evidence. G6B will use a
small internal, typed, three-level fixture traced to them because it has no
public command, persistence or G7 metric operation. That fixture must prove
composition, inner-level invalidation, absence of render/sample dependence,
absence of whole-upstream-locus regeneration and approved functional scaling.

**Historical G6A closeout:** G6A = PASS — AUTHOR APPROVED. ADR 0006 =
ACCEPTED. G6B had not started at that closeout.

## 17. Realized minimal G6B semantic architecture

G6B realizes the approved model without changing the legacy locus. The
productive data flow is:

```text
normal GeoElement inputs
  -> AlgoLocusV2 family (normal AlgoElement input/output edges)
  -> immutable LocusDefinition2D @ semanticRevision
       -> LocusBranch2D {branchKey, declared domain,
                         validDomainComponents, orientation,
                         provenance, typed lineage, quality}
       -> LocusEvaluator2D
            -> scoped LocusEvaluationSession2D
                 -> exact full-key memoization / active-key cycle guard

EuclidianView
  -> DrawLocusV2
       -> bounded LocusRenderCache2D
            -> semantic evaluate(...)
            -> view-local vertices (never returned by the semantic API)
```

### 17.1 Implemented semantic surface

`GeoLocusV2` is a parallel final `GeoElement` classified as the append-only
`GeoClass.LOCUS_V2`. It implements neither `Path` nor a legacy locus interface,
does not claim `isGeoLocus()` or `isGeoLocusable()`, returns no XML and is not
registered in `GeoFactory`. `CmdLocus`, `AlgoDispatcher`, legacy metrics, ODE,
G5 export and 3D dispatch remain untouched.

The initial providers are deliberately narrow:

| Provider | Semantic parameter | Productive G6B coverage |
|---|---|---|
| `explicit-numeric-domain/v1` | Explicit finite interval, endpoint policy, orientation and optional periodicity owned by the construction | Analytic, topology, nested and test fixtures |
| `stable-path-domain/v1` | Segment `t in [0,1]`; circle/ellipse angle `t in [-pi,pi)` | Live normal-DAG segment pilot; immutable segment/circle/ellipse provider contracts |

No public normalized `PathParameter` is adopted. Functions and arbitrary paths
are not silently assigned a provider. An unbounded image is demonstrated by a
finite open semantic domain and a divergent analytic map; native infinite
driver intervals remain outside this minimum provider version.

### 17.2 Revision and dependency behavior

Each concrete algorithm captures its normal-DAG inputs during `compute()` and
publishes an immutable definition only when semantic content changes. The
local revision is positive and monotonic. Point queries, query order, render,
zoom and a constrained segment driver's current presentation position do not
publish revisions. Changes to defining inputs propagate through ordinary
`AlgoElement` edges; nested V2 outputs are explicit downstream inputs.

The dynamic branch pilot captures immutable source values and constructs typed
lineage from the preceding semantic definition during recompute. Evaluation
therefore never reaches back into a mutable live construction. A validity gap
changes `validDomainComponents`; it does not manufacture a branch identity.

### 17.3 Nested semantic composition

For `L1 -> L2 -> L3`, `AlgoNestedLocusV2` captures the upstream immutable
definition for its own revision and invokes `upstreamDefinition.evaluate(...)`
with the same scoped session. The session key is locus identity, semantic
revision, branch key and provider-canonical parameter bits. It enforces one
coherent revision per locus, has bounded insertion-order eviction, can run with
memoization disabled, and diagnoses an active-key re-entry cycle.

The controlled gate uses 64 unique outer queries. Depths 1, 2, 3 and 5 require
exactly 64, 128, 192 and 320 semantic evaluator calls. Repeating the 64
depth-three requests in the same session yields 64 hits and retains 192 entries;
session-disabled results are identical. Dependency-slice builds,
synchronizations, upstream render work and whole-locus regeneration remain
zero because none is part of the V2 evaluation path.

### 17.4 Render boundary and current limitations

`DrawLocusV2` obtains semantic points from the evaluator and constructs a
`GeneralPathClipped`. Its per-drawable cache is keyed by semantic revision and
an immutable view/render policy and is bounded to four entries. The validated
coarse/fine policies produce 33 and 129 vertices for the same semantic
definition; semantic coordinates and revision do not change.

This first renderer uses a bounded, view-derived uniform presentation sampling
policy. It is not a metric partition, error-certified curve approximation or
semantic domain authority. Native infinite driver domains, canonical
continuation, persistence, public creation, point-on-V2 behavior, metrics,
intersections, locus export, 3D behavior and concurrency remain deliberately
unimplemented.

The internal `GeoElement.copy()` support exists only to satisfy kernel object
contracts. It rebinds an immutable definition but does not establish a public
copy/migration/persistence guarantee; that surface must be reviewed before any
future public exposure.

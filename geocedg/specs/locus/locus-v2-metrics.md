# Locus V2 metric contract

- Status: **G7 v1.0 NORMATIVE — PASS — AUTHOR APPROVED; POST-G9U1-A1 v1.1 AMENDMENT — PASS — AUTHOR APPROVED; POST-G9U1-A2 v1.2 AMENDMENT — PASS — AUTHOR APPROVED; PRE-G9B-CU1 v1.5 AMENDMENT (§25, INCLUDING §25.12) — ARCHITECTURE AUTHOR APPROVED WITH PRECISIONS N1–N5; PASS — AUTHOR APPROVED WITH THE CU1 CLOSEOUT (2026-10-20); PRE-G9B-CU2 v1.6 AMENDMENT (§26, WITH §26.5 FROM AUTHOR DECISION H10 OF 2026-10-22) — ARCHITECTURE AUTHOR APPROVED (2026-10-21); PASS — AUTHOR APPROVED WITH THE CU2 CLOSEOUT (2026-10-24); CONDITIONED USE AMENDMENT (§28, ADR 0042) — AUTHOR APPROVED AS CONTRACT (2026-11-22), IMPLEMENTATION AUTHORIZED, NOT YET APPROVED AS PRODUCT**
- Version: `1.7`
- P2 amendment §26.6: **SCIENTIFIC DECISION AUTHOR APPROVED (2026-11-17); historical initial implementation BLOCKED; final consolidation FOCAL VERIFIED (2026-11-18), global verification and product acceptance pending** ([bounded consolidation](../../../docs/architecture/geocedg-1.2.0-p2-final-consolidation.md)).
- Approval date: 2026-08-13
- Author-review disposition: **G7A-R1, G7A AND G7B PASS — AUTHOR APPROVED**
- Roadmap gate: G7 `PASS`; G7A `PASS`; G7B `PASS`
- Affected layer: shared Java kernel semantics and internal developer laboratory
- Working architecture: `GeoLocusMetricResult` as a normal kernel-DAG result
- Architecture decision: Accepted ADR 0007
- Date: 2026-08-13

This normative contract records the author-approved G7 planning requirements,
all 42 G7A recommendations, R1-1..R1-22 and the three API normalizations
approved at final closeout. It does not override the normative G6 Locus V2
semantic contract or create a public API. The productive internal G7B
implementation conforms to this contract and is author-approved.

## 1. Scope

The G7 minimum adds native semantic length services for the existing
experimental `GeoLocusV2`. It defines:

- between-position and complete-locus operations;
- durable semantic positions and revision-scoped bindings;
- route resolution independent of integration;
- constructive traversal multiplicity;
- rich immutable results and contribution decomposition;
- component integration and aggregation;
- a revision-scoped bounded metric-index hypothesis;
- publication through a dedicated GeoElement in the normal kernel DAG; and
- internal/developer-only inspection.

It does not define public commands, persistence, public `Path`, point-on-locus,
intersections, spatial behavior or locus export.

## 2. Mathematical definition

For each branch evaluator `F` and each route interval or valid-domain component
`I`, length is total variation:

\[
\operatorname{Var}(F;I)=
\sup_P\sum_i\lVert F(t_i)-F(t_{i-1})\rVert.
\]

For absolutely continuous `F`:

\[
\operatorname{Var}(F;I)=\int_I\lVert F'(t)\rVert\,dt.
\]

The numerical algorithm does not define the quantity. Render vertices,
`LocusRenderCache2D`, legacy samples, `myPointList`, viewport, zoom, DPI and
pixel tolerance shall not be metric inputs or authority.

## 3. Metric semantics

The normative minimum metric is
`CONSTRUCTIVE_TRAVERSAL_LENGTH`. Retracing counts by preimage and distinct
constructive branches count separately even when their images coincide.
Branch/domain provenance shall remain observable.

`GEOMETRIC_IMAGE_UNION_LENGTH` and geometric deduplication are excluded.

## 4. Query types

### 4.1 `BetweenPositionsMetricQuery`

This query shall carry two semantic position bindings, `FORWARD` or `REVERSE`,
a `ZERO_LENGTH` or `FULL_CYCLE` same-position policy, an open-boundary policy
and explicit metric/improper-limit policies.

Length shall remain non-negative. Direction selects the route. `SHORTEST` is
deferred.

### 4.2 `TotalLocusMetricQuery`

This query shall carry no `A`, no `B` and no semantic direction. It shall not be
represented by equal endpoints, `FULL_CYCLE` or `WRAP_TO_START`.

For revision `r`:

\[
\mathcal L_{\mathrm{total}}(L_r)=
\sum_j\sum_k\operatorname{Var}(F_{r,j};C_{r,j,k}).
\]

Each valid component shall contribute exactly once. Disconnected components
may be aggregated without a connector. One periodic branch shall contribute
one fundamental cycle.

## 5. Semantic positions and bindings

The candidate durable position is:

```text
LocusSemanticPosition2D
    locusIdentity
    branchKey
    providerVersion
    providerCanonicalParameter
```

The candidate revision binding is:

```text
MetricPositionBinding2D
    semanticPosition
    semanticRevision
    resolvedValidComponentKey
    evaluationStatus
    evaluatedPoint
    diagnostics
```

Revision does not automatically become durable identity. Component keys are
revision-scoped. Coordinate-proximity repair is forbidden. A binding from a
different revision shall produce `POSITION_STALE`; rebinding shall be a
separate explicit operation.

## 6. Route resolution

The design shall keep these concepts separate:

```text
LocusMetricRouteResolver2D
LocusMetricRoute2D
LocusMetricRouteSegment2D
```

The resolver shall interpret positions, revision, branch, components,
direction, periodic seam, same-position policy, global boundaries, open
policies, gaps and reachability. It shall not integrate.

`LocusMetricRoute2D` shall preserve:

```text
locusIdentity
semanticRevision
branchKey
orderedRouteSegments[]
direction
boundaryPolicy
targetReached
wrapped
geometricallyConnected
routeStatus
```

Every segment shall belong to one valid component. No policy shall cross an
internal invalid-domain gap.

## 7. Same-position and open-boundary policies

`ZERO_LENGTH` and `FULL_CYCLE` shall be explicit. `FULL_CYCLE` is legal only
for approved periodic semantics and shall never be inferred from Cartesian
equality.

For an open branch where a forward target lies behind the start position:

- `STOP_AT_END` returns the finite length to the global end, incomplete
  coverage, `targetReached = false` and `STOPPED_AT_BOUNDARY`; it is not the
  complete `A`/`B` length;
- `WRAP_TO_START` returns the sum of the two boundary-side contributions,
  `wrapped = true`, `targetReached = true` and
  `geometricallyConnected = false`; it adds no chord or incidence; and
- `STRICT` returns `ABSENT` with `TARGET_NOT_REACHABLE`.

Encountering an internal gap returns `DISCONTINUITY_ENCOUNTERED` and incomplete
coverage; wrap shall not bypass it.

## 8. Zero cases

The working policy is:

| Case | Value | Coverage |
|---|---:|---|
| Empty domain | 0 | `COMPLETE` |
| Isolated point | 0 | `COMPLETE` |
| Collapsed image | 0 | `COMPLETE` |

Each case shall retain a typed diagnostic. Zero shall not be encoded as failure
or absence.

## 9. Rich immutable result

`LocusMetricResult2D` and every `LocusMetricContribution2D` shall contain one
closed immutable metric value:

```text
MetricValue2D
    FiniteMetricValue2D(non-negative finite value)
    PositiveInfinityMetricValue2D
    AbsentMetricValue2D
```

Only the finite variant may expose `OptionalDouble` finite access. A bare
`double getValue()` is forbidden: absence and positive infinity shall not be
encoded with NaN, magic values or exceptions. A derived `MetricValueKind` may
remain as an exhaustive discriminator, but it is not a second payload.

The result shall use defensive collections and coherent constructor invariants
and shall separate at least:

```text
MetricValueKind
    FINITE
    POSITIVE_INFINITY
    ABSENT

MetricCoverage
    COMPLETE
    INCOMPLETE

MetricComputationStatus
    SUCCESS
    INVALID_QUERY
    UNSUPPORTED
    NUMERICAL_FAILURE
    LIMIT_NOT_ESTABLISHED

MetricRectifiability
    RECTIFIABLE
    NON_RECTIFIABLE
    UNDETERMINED

TraversalOutcome
    TARGET_REACHED
    STOPPED_AT_BOUNDARY
    WRAPPED_TO_START
    TARGET_NOT_REACHABLE
    DISCONTINUITY_ENCOUNTERED
```

`TraversalOutcome` is structurally present only for a between-position result
whose contract requires traversal. A total result has no traversal outcome.
The API shall use `Optional<TraversalOutcome>` or an equivalent separation of
result types; it shall not use `null`, a sentinel or an artificial
`NOT_APPLICABLE` outcome.

It shall also carry construction fidelity, evaluator method,
metric/integration method, representation role, units, provenance, scalar
admissibility, diagnostics and contribution decomposition.

Numeric quality shall reuse the productive normative G6 type
`LocusSemanticMetadata2D.NumericGuarantee` directly. G7 shall not declare a
duplicate guarantee enum. Metric-specific evidence shall be held by an
immutable `MetricErrorEvidence2D` or equivalent. Error-amount availability is
a closed sum type, never an independently variable state plus optional number:

```text
sealed MetricErrorAmount2D
    EstablishedMetricErrorAmount2D(non-negative finite amount)
    NotEstablishedMetricErrorAmount2D
    NotApplicableMetricErrorAmount2D

G6 NumericGuarantee?       // absent only when evidence is not applicable
absoluteEvidence: MetricErrorAmount2D
relativeEvidence: MetricErrorAmount2D
scope                      // COMPLETE_VALUE / REPORTED_PARTIAL_VALUE / NOT_APPLICABLE
method
assumptions[]
certificateMetadata?
```

No error field shall use NaN, `-1` or zero to mean unknown. Exact arithmetic,
certified bounds, estimates, uncertified values, positive infinity and absent
values shall remain distinguishable. `DIVERGENT` shall not be used as a
generic numerical-failure state.

## 10. Metric integration and aggregation

G7A characterized, in order:

1. analytic/closed-form capability;
2. differential quadrature;
3. evaluator-only adaptive variation approximation; and
4. unsupported capability.

Evaluator-only refinement agreement shall not claim
`CERTIFIED_ERROR_BOUND`. `ESTIMATED_ERROR` requires explicit recorded
assumptions; otherwise the result is `FLOATING_POINT_UNCERTIFIED` or
`UNSUPPORTED`.

`LocusMetricAggregator2D` shall combine contributions, constructive
multiplicity, values, errors, weakest guarantees, coverage, status and
decomposition. It shall not integrate a component.

Certified absolute bounds add only across exact/certified finite
contributions. Estimated evidence propagates as estimated with its assumptions;
an uncertified finite contribution makes the aggregate uncertified. Positive
infinity and absent/unsupported values have not-applicable numeric error
evidence. If a known finite subtotal has incomplete coverage, its evidence
shall be scoped to `REPORTED_PARTIAL_VALUE`, never to the unknown complete
total.

For second review, differential quadrature uses a small per-call deterministic
GeoCeDG-owned integrator with explicit status, counters and estimated error.
The existing static adaptive-Gauss helper is an accuracy comparator, not the
metric result contract.

PRE-G9B-CU1: the guarantee classes and the capability order of §25.1–§25.2
supersede the ordered list above; the aggregation rules of this section remain.

## 11. Publication in the kernel DAG

The G7B working architecture is:

```text
LocusMetricResult2D
    immutable semantic metric value

GeoLocusMetricResult
    GeoElement publishing the rich result in the normal kernel DAG

AlgoLocusMetricV2
    AlgoElement registering dependencies and updating GeoLocusMetricResult
```

A `GeoNumeric` shall not be the sole output or semantic authority. The rich
result shall exist as its own graph entity.

G7A audited and recommends an append-only classification equivalent to:

```text
GeoClass.LOCUS_METRIC_RESULT
```

It shall not reuse `NUMERIC`, `LOCUS` or `LOCUS_V2`. G7B minimum shall register
no XML type, persistence or 3D behavior and no public command.

## 12. Defined state and scalar participation

Rich-result defined state and scalar-admissible state shall remain separate.
G7A compared:

- no generic numeric facade;
- a conditional read-only numeric facet; and
- an explicit numeric adapter.

The source audit found no safe instance-level gate for the static numeric
interfaces. The approved choice is an explicit derived numeric
adapter; the rich result implements no generic numeric facade and remains the
authority. No automatic companion `GeoNumeric` is created.

Candidate scalar-admissible results are finite, successful, semantically
satisfied, suitably covered, current-revision values with no unsupported
contribution and, by default, at least an estimated-error guarantee. Valid zero
and explicitly requested valid wrap are candidates.

Partial stop, unreachable target, incomplete coverage, stale position,
different branch, discontinuity, unsupported, numerical failure, unestablished
limit, absence, floating-point-uncertified value and positive infinity are not
admissible by default.

The G7A audit covered `GeoElement.isDefined()`, `NumberValue`/numeric
interfaces, Algebra View, expression evaluation, generic numeric algorithms,
lists, sequences and CAS. The resulting strategy C contract is author-approved.

PRE-G9B-CU1: the default "at least an estimated-error guarantee" becomes the
admissibility rule of §25.4 — a certified value whose certified error does not
exceed the tolerance, or an estimate whose estimated error does not exceed it,
under the estimate contract of §25.5.

## 13. Lifecycle

G7A covered and G7B shall cover creation, publication, defined/undefined behavior,
scalar admissibility, copy/copyInternal, set, remove, invalidation,
undefined/recovery, topology changes, branch disappearance, undo/redo, labels,
Algebra View, lists/sequences, selection, defaults/styles, numeric interfaces,
GeoClass switches, factory/XML impact, 2D/3D and packaging.

A copy or assignment shall never inherit a stale index, foreign construction
state, obsolete semantic revision, stale binding or partial unpublished result.
Unsupported lifecycle operations shall fail explicitly and be tested; they
shall not fall back to sampled or numeric substitutes.

A rich Geo is defined exactly when it has a current immutable published
snapshot, including a typed absent/failure diagnostic. Copy, assignment and
sequence materialization in the minimum clear current state and require normal
DAG recomputation unless a future safe portable-value contract is approved.

Atomic failure semantics use candidate policy P1. When revision `r+1`
supersedes a successful revision `r`, the old payload immediately becomes
non-current. A successful private build publishes one immutable entry and one
coherent rich snapshot for `r+1`. A handled failure publishes no index entry
and publishes a coherent `Absent` rich failure snapshot for `r+1`; its scalar
adapter is undefined. No old-value/new-status hybrid, stale success or partial
entry may be observed. “Failed build publishes nothing” applies to the index
entry, not to the current-revision rich failure diagnostic.

PRE-G9B-CU1 (§25.7): a deterministic failure of a component-state build
publishes a memoized failure entry under its complete key instead of no entry,
so the same key is never rebuilt until it changes.

## 14. Revision-scoped index hypothesis

G7A compared:

```text
REFERENCE_NO_INDEX_REUSE
EAGER_WHOLE_REVISION
LAZY_COMPONENT_REVISION
```

The accepted index strategy is bounded `LAZY_COMPONENT_REVISION` indexing.
Same-component 1/10/100 traces built 1/1/1 lazy components versus
3/3/3 eager and 1/10/100 reference components; 100 repeated totals built 3/3
eager/lazy versus 300 reference. Its
complete key shall include at least:

```text
locusIdentity
semanticRevision
branchKey
resolvedValidComponentKey
provider/evaluator capability version
metricAlgorithmVersion
metricPolicyVersion
metricTolerancePolicy
multiplicityPolicy
improperLimitPolicy
metricWorkBudget
```

The index shall not be global and shall retain no unbounded revision history.
Component keys are revision-scoped, not durable position identity.

The accepted ownership architecture is a GeoCeDG-owned
`DEDICATED_SHARED_OWNER`. It is a non-GeoElement derived service tied to one active source
locus in one Construction and shared by compatible metric algorithms. The
algorithms remain ordinary direct DAG dependents of the locus. The service
owns no dependency edges or result publication, resolves no route, performs no
query aggregation and creates no callbacks between algorithms.

The reusable entry is immutable component-level metric state, conceptually:

```text
LocusMetricComponentState2D
    adaptive partition / cumulative arc-coordinate evidence
    capability and integration metadata
    component-level error state
```

The complete component key has no A/B endpoints and maps only to this state:

```text
complete component key
    -> get/build immutable LocusMetricComponentState2D

LocusMetricComponentState2D + LocusMetricRouteSegment2D
    -> route/component metric evaluation
        -> LocusMetricContribution2D
```

A total query derives a contribution over the complete valid-component extent;
a between-position query derives one contribution for each route segment. One
component state may produce many different contributions. The shared owner
never shares routes, query results, contributions or aggregate results.

For N compatible consumers and one complete key, the hard budget is one
component-state build until eviction or invalidation. The no-reuse path remains
the semantic oracle. Different loci or Constructions never share. Revision,
topology, undefined and removal transitions make old entries unreachable;
consumer and locus removal release ownership without a static/global registry.

The provisional capacity is 64 component entries per active locus owner,
current revision only, with deterministic insertion-order eviction. It is not
64 per metric algorithm and is not a normative constant. Real entry size and
capacity must be measured during G7B before this becomes stable policy.

Hard requirements from the first G7B implementation are:

- bounded state and deterministic eviction;
- no retained obsolete revisions;
- defensive immutable keys and values;
- complete key equality;
- kernel-thread confinement;
- no concurrency or background mutation;
- atomic result/index publication;
- exception-safe active bookkeeping and cleanup through `finally`;
- no partially valid published entry; and
- cache/index ON/OFF semantic equality.

PRE-G9B-CU1 (§25.9): a between-position query against a cached component state
performs only local work bounded by the leaves that contain its endpoints.

## 15. Tolerance and error ownership

The initial versioned G7B policy is
`eps_metric_abs=1e-10` construction length unit and `eps_metric_rel=1e-9`.
The effective threshold is `max(eps_metric_abs, eps_metric_rel*S)` for a
translation-invariant world-coordinate scale `S`.

Depth shall never be the only work guard. The complete policy shall contain a
deterministic `MetricWorkBudget2D` or equivalent with independent maximum
evaluations, subdivisions and depth, and all three dimensions participate in
the complete index key. The approved initial defaults are `32768` evaluations,
`16384` subdivisions and depth `22`. They are implementation-policy defaults,
not mathematical constants. Exhausting any work dimension yields `Absent`
with `LIMIT_NOT_ESTABLISHED` and a typed cause; evaluator/numeric exceptions
remain `NUMERICAL_FAILURE`. No wall-clock timeout is metric authority.

Absolute contribution errors add deterministically and the weakest guarantee
propagates. These policies do not reuse the G6 evaluation envelope,
`eps_domain`, render pixel tolerance or future G8 root/residual tolerance.

PRE-G9B-CU1 (§25.3): `S` is a translation-invariant extent of the whole
component image, never a local chord, and the resulting tolerance is
distributed over the leaves of the partition. The default requested tolerances
`eps_metric_abs=1e-10` and `eps_metric_rel=1e-9` are unchanged.

## 16. Reparameterization, improper metrics and arc coordinate

G7A proved candidate length invariance for the regular
orientation-preserving map

\[
\phi(u)=\frac{e^{cu}-1}{e^c-1}
\]

and retained `u^3` as a monotone endpoint-derivative-degenerate fixture.

It distinguished finite bounded subarcs on unbounded branches, finite
improper totals, positive infinity, non-rectifiability, unsupported capability
and unestablished limits. No viewport cutoff is permitted.

Arc coordinate `s(t) = Var(F;[t_0,t])` may exist only as per-valid-component
derived data. It shall not replace the semantic parameter or connect gaps or
branches, and it shall not become an automatic public surface.

## 17. Validation and performance

The validation authority is the
[G7 matrix](../../../docs/validation/g7_locus_v2_metric_validation_matrix.md)
and the [G7 benchmark plan](../../../docs/validation/g7_locus_v2_metric_benchmark_plan.md).
G7A execution evidence is the
[characterization report](../../../docs/validation/g7a_locus_v2_metric_characterization_report.md)
and the focused
[R1 refinement report](../../../docs/validation/g7a_r1_locus_v2_metric_refinement_report.md);
both remain evidence rather than runtime authority; their decisions are
incorporated into this normative contract by author approval.

Repeated traces shall use 1, 10 and 100 queries for same endpoints,
overlapping arcs, reverse, periodic, stop, wrap, strict, repeated total and
tolerance/policy changes. Functional counters precede wall-clock gates.

Nested metric composition shall cover:

```text
L1 -> metric(L1) -> L2 -> metric(L2) -> L3
```

with zero render access, zero legacy-sample access, no whole-locus regeneration
per downstream point, no metric-index build per point, same-revision/policy
reuse, normal-DAG invalidation and cache-off equality.

Independent expected values shall come from analytic references, high
precision or an independent numerical method. Any Python-generated constant
shall record formula, precision, runtime/library, script and output hash.
Python is validation evidence, not kernel authority.

## 18. Compatibility and public boundary

The G7B minimum candidate is:

```text
internal Java API
+ GeoLocusMetricResult
+ developer laboratory
```

It shall not add or change:

- `LocusLength`, `Length` or `Perimeter` commands;
- public `Path` or point-on-locus behavior;
- XML, persistence or migration;
- G8 intersection/incidence behavior;
- G9 spatial semantics;
- G5 locus export;
- 3D behavior;
- concurrency, C++, external numeric libraries or unmeasured DAG flattening.

Classic and public GeoCeDG `Locus[...]` shall remain legacy. Locus V2 remains
experimental/internal and disabled by default.

## 19. Author-approved phase disposition

The final G7A author review accepted all 42 recommendations, R1-1..R1-22 and
the three closeout API normalizations. That approval made the contract
normative and authorized the separately executed G7B implementation, which is
now also author-approved:

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED
G7B = PASS — AUTHOR APPROVED
G7 = PASS
G7B CAPACITY 64 = PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT
G8 = NOT STARTED
G9 = NOT STARTED
```

## 20. G7B conformance clarification

The author-approved G7B implementation realizes this normative contract with internal classes under
`org.geocedg.common.kernel.locus.metric`, one rich
`GeoLocusMetricResult`, one normal-DAG `AlgoLocusMetricV2` and an explicit
scalar adapter. This is implementation evidence, not a semantic amendment.

The component-state key excludes route endpoints. The bounded shared owner
publishes only immutable component-level state after successful private build;
route-specific contributions and aggregate results remain local to the query.
Traversal is structurally absent from total results. Error amount is expressed
only by the three closed variants, reusing the G6 `NumericGuarantee` directly.

No command, XML registration, persistence contract, public `Path`, 3D, G5,
G8 or G9 surface is added. The mathematical and public-boundary clauses above
remain unchanged.

## 21. POST-G9U1-A1 normative amendment: SplineV2 constructor endpoints

The public between-position forms may also consume an ordinary `GeoPoint`
when, and only when, the explicitly supplied source is a current `SplineV2` or
a supported R5 similarity image of one and the point has exact constructor
occurrence provenance on that spline. This amendment changes endpoint
admissibility only; the rich result remains authoritative, the scalar remains
guarded, and traversal direction, sign, wrap and same-position policy are
unchanged.

For the bounded native constructor, the ordered input array of its direct
`AlgoDependentList` is the reconstructible occurrence definition. Each slot is
distinct even when several slots refer to the same durable point. A current
slot maps to its single normalized spline knot and then to one canonical
branch/component address. An interior knot is owned once rather than once per
adjacent polynomial span. A periodic endpoint retains explicit lift and seam
side. Computed list families that do not expose this exact ordered definition
are unresolved and are not searched by value.

The resolver publishes one closed result:

```text
NO_ADDRESS
UNIQUE(occurrence key, address)
MULTIPLE(occurrence keys, addresses)
UNRESOLVED_INVALID
```

The occurrence key is versioned and relative to the addressed source, root
spline, constructor list, ordered slot and durable point identity. The slot is
not a global point identity and has authority only inside that exact ordered
constructor definition. Ordinary coordinate recomputation preserves the
relation. Insertion, removal, reorder or replacement is a constructor-topology
change governed by the existing lifecycle/redefine contract, not continuity
inferred from the new array position.

Exactly one current result is rebound through the existing
`LocusMetricPositionBinder2D`. Zero, multiple, changed or unresolved
occurrences yield an explicit absent/invalid rich result and an undefined
scalar adapter. A metric algorithm retains an accepted occurrence key during
its lifetime and never retargets to a different slot. Native save/reopen and
undo/redo reconstruct the relation from the command DAG and durable IDs; the
key and polynomial cache are not serialized. Copy/remap creates source/list/
point identities for the copied closure and therefore a new, internally
consistent occurrence key.

The current host lifecycle cannot produce `UNIQUE(K1) -> UNIQUE(K2)` while
retaining the same metric object: explicit replacement of the participating
ordered constructor list with a reordered list is rejected atomically as
`REDEFINE_INCOMPATIBLE`, while replacement of the spline has replacement
identity semantics for its dependent closure. Accordingly, direct K1/K2
no-retarget execution is
`NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`. The retained-key guard remains the
fail-closed authority if a future approved lifecycle makes such a transition
representable; A1 does not authorize that lifecycle extension.

R5 similarity covariance is admitted only through the explicit
`AlgoLocusSimilarityTransform2D` source lineage, which preserves provider,
branch, component and parameter and substitutes the transformed source ID.
Other transformation/source families are unsupported for A1. Coordinates,
Cartesian coincidence, proximity, labels, rendering, construction order, XML
position and list-value searches never establish or repair provenance. The
[A1 validation matrix](../../../docs/validation/post_g9u1_a1_spline_constructor_provenance_validation_matrix.md)
is the preserved candidate traceability authority. The author subsequently
approved POST-G9U1-A1 at candidate
`1f862dccba6a1f685b718f8e43bf92153fc22f12`; this amendment is now normative
and does not alter the author-approved G7 history.

## 22. POST-G9U1-A2 normative amendment: explicit traversal

The rich between-position authority adds the compatible public form:

```text
LocusLength(L, A, B, "direction", "boundary", "same-position")
```

The stable public tokens are:

```text
direction     = "forward" | "reverse"
boundary      = "strict" | "stop-at-end" | "wrap-to-start"
same-position = "zero-length" | "full-cycle"
```

The existing `LocusLength(L,A,B)` form remains exactly the default
`forward/strict/zero-length`. `Length(L,A,B)` remains a guarded, non-negative
magnitude using that unchanged default. A2 does not add signed length,
`shortest`, another endpoint authority or a policy argument to scalar
`Length`. Total `LocusLength(L)` and `Length(L)` remain unchanged.

Direction selects an ordered constructive route; it is never encoded as an
algebraic sign. `strict` rejects an unreachable target. `stop-at-end` returns
the existing incomplete rich result at the applicable global boundary with
`targetReached=false`. `wrap-to-start` combines the two global-boundary-side
contributions without an artificial connector and records
`geometricallyConnected=false`. No boundary policy crosses an internal invalid
domain gap or disconnected component.

On a periodic branch, forward/reverse operate on the declared oriented
fundamental domain and the existing canonical seam address. For the same
semantic endpoint, `zero-length` has no route segment; `full-cycle` is valid
only for one approved periodic component. Cartesian equality never establishes
same-position or a cycle.

The rich result retains the complete immutable `LocusMetricRoute2D` evidence:
direction, ordered route segments, boundary policy, wrapped state,
`targetReached`, geometric connectivity, route status and traversal outcome.
This evidence is derived on recomputation and is not serialized as a cache.
The three public policy texts are ordinary command/DAG inputs, so save/reopen,
undo/redo, rename and copy/remap reconstruct the choice without enum ordinals,
UI state, labels or render state as authority. Unknown or malformed tokens fail
explicitly with an absent/invalid rich result.

Endpoint resolution remains the existing exact semantic-point and A1
constructor-occurrence authority. Coordinates, proximity, labels, branch
order, render samples and solution indices never select an endpoint or route.
The [A2 validation matrix](../../../docs/validation/post_g9u1_a2_explicit_traversal_policy_validation_matrix.md)
is the candidate traceability authority. This v1.2 amendment is author
approved (status line above) and does not reinterpret G7 or A1 evidence.

## 23. PRE-G9B-R2 normative amendment: semantic-intersection endpoints

This v1.3 amendment was introduced by the `PRE-G9B-R2` technical candidate. Its
author-approval status is recorded only in that phase's author-decision record,
never in this section.

The between-position forms `LocusLength(S, P, Q)` and `Length(S, P, Q)`, with or
without the §22 traversal policy, admit a third, explicitly typed endpoint
provenance family: an ordinary `GeoPoint` materialized from one exact token of a
Locus V2 pair intersection. §21 and §22 are not reinterpreted; they now read
together with this section, so the admitted endpoint families are exactly three:

```text
SEMANTIC_POINT            explicit semantic locus point whose source is S
INTERSECTION_OCCURRENCE   this section
CONSTRUCTOR_OCCURRENCE    §21
```

This amendment changes endpoint admissibility only. The rich result remains
authoritative, the scalar remains guarded, and direction, sign, wrap, boundary and
same-position policy are unchanged. It does **not** change which pair roots may be
materialized as points: that is the separate `R2-E0`/`R2-E1` contract and is not
widened here.

### 23.1 Admission walk

An endpoint belongs to this family only when all of the following hold, each read
from explicit provenance carried by the construction DAG:

```text
endpoint point
  -> parent algorithm is the exact-token intersection-point consumer
  -> its rich input is a Locus V2 x Locus V2 pair intersection result
  -> its current effective root token
  -> the current point-admissible solution for that exact token
  -> the solution's ordered per-side pair evidence
  -> the side whose locus identity is S's current semantic identity
  -> that side's branch and semantic parameter
  -> one canonical branch/component address on S
  -> the existing LocusMetricPositionBinder2D
```

The resolver publishes one closed result, mirroring §21:

```text
NO_ADDRESS
UNIQUE(occurrence key, address)
MULTIPLE(occurrence keys, addresses)
UNRESOLVED_INVALID
```

### 23.2 Side selection and ambiguity

Per-side locus identity and semantic revision are copied from the intersection
query binding, so side order is fixed by the evidence and never by caller order,
labels or solution index. The side corresponding to `S` is selected by **exact
locus-identity equality only**:

| Matching sides | Result |
|---|---|
| none | `NO_ADDRESS` — `S` is not a side of this intersection |
| exactly one | candidate `UNIQUE`, subject to §23.3 |
| two (`S × S` self-intersection) | `MULTIPLE` — the root has two distinct semantic positions on `S`; choosing one would need a rule this contract does not grant |

`MULTIPLE` publishes `INVALID_QUERY`. Caller order `Intersect(S, T)` versus
`Intersect(T, S)` does not change the selected side, because selection is by
identity, not by position in the evidence.

A token that appears on more than one admissible solution is already excluded by
the rich result's own admissible-solution index, so such a point is undefined and
is rejected before this family is consulted. No duplicate token is ever
disambiguated here.

### 23.3 Currentness

A selected side is current only when both hold:

- the effective root token is currently validated by the rich result's token
  ledger, so the solution is present in the current revision; and
- the side's semantic revision equals the semantic revision of `S`'s current
  definition.

A side whose identity matches `S` but whose revision does not is **stale** and
yields `UNRESOLVED_INVALID`, never `NO_ADDRESS`: a stale match must not fall
through to another family. An undefined endpoint is already rejected by the
metric before any provenance work, so a dormant materialized point fails closed
one step earlier.

### 23.4 Address construction

The address is built from the selected side exactly as §21 builds a constructor
address: the side's branch key selects the branch; the raw parameter is the
side's semantic parameter; the provider canonicalizes it; exactly one valid
component of that branch must contain the canonical parameter; periodic lift
and seam side are derived from the raw parameter under the provider's declared
domain. Zero or several containing components yield `UNRESOLVED_INVALID`.

The side's lifted periodic parameter is **not** endpoint authority. It records
which cover of a periodic domain the pair solver searched, which is evidence of
the solver path rather than of the root's position on `S`, so it never selects
a lift or seam side.

### 23.5 No-retarget occurrence key

The occurrence key is versioned and relative to the addressed source, the rich
intersection result, the exact root token and the durable endpoint point:

```text
metric-endpoint/intersection-occurrence/v1
  | addressed-source = <durable S identity>
  | rich-result      = <durable intersection result identity>
  | token            = <exact effective root token>
  | side             = first | second, in evidence order
  | point            = <durable endpoint identity>
```

A metric algorithm retains an accepted key during its lifetime and never
retargets: a later resolution to a different key, including a change of endpoint
family, yields `INVALID_QUERY`. Dormancy and reactivation under the same exact
token retain the same key.

### 23.6 Family dispatch

Dispatch is by the endpoint's explicit parent algorithm, never by value search.
The families are consulted in the order of §23 above and are structurally
disjoint: an intersection-point consumer whose rich input intersects `S` depends
on `S`, so it cannot also be a constructor slot of `S` without a cycle.
`NO_ADDRESS` from this family falls through to §21 unchanged, which preserves an
intersection point used as a constructor slot of an unrelated spline. `UNIQUE`,
`MULTIPLE` and `UNRESOLVED_INVALID` are final.

### 23.7 Persistence, reopen, copy and redefine

Native save/reopen and undo/redo reconstruct the relation from the command DAG,
the durable identities and the rich result's persisted token ledger, then
revalidate currentness; the occurrence key is not serialized and saved
coordinates are never sufficient evidence. Copy/remap creates source, result,
point and metric identities for the copied closure, and the single authorized
closure-copy token rebase remains the only permitted copy provenance path;
the copied metric therefore acquires a new, internally consistent key. Redefine
follows the existing lifecycle and redefine contract; a resulting change of
side, token or identity is resolved afresh and never repaired by proximity.

### 23.8 Explicit exclusions

- A point materialized from a single-source intersection — `LocusV2` against an
  ordinary geometric target — carries no per-side locus evidence and is **not**
  admitted by this family; it returns `NO_ADDRESS` and behaves exactly as before.
- R5 similarity lineage is **not** followed. An intersection root is a root of
  the intersected source, not of its image; an image of `S` is a different source
  identity and returns `NO_ADDRESS`.
- Coordinates, Cartesian coincidence, proximity, tolerance, labels, branch order,
  solution index, render samples, construction order and XML position never
  establish, disambiguate or repair an intersection endpoint.

## 24. PRE-G9B-R2-E0/E1 normative amendment: semantic endpoint admissibility

This v1.4 amendment was introduced by the `PRE-G9B-R2-E0/E1` technical
candidate under [ADR 0028](../../../docs/adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md).
Its author-approval status is recorded only in that phase's author-decision
record, never in this section. §21, §22 and §23 remain in force; this section
widens the admitted endpoint families and states one uniform dispatch, retention
and exclusion rule. It changes endpoint admissibility only: the rich result
remains authoritative, the scalar remains guarded, and direction, sign, wrap,
boundary and same-position policy are unchanged.

### 24.1 Three distinct questions

Point existence or materialization, semantic membership of a point on a curve,
and metric endpoint admissibility are separate questions:

- a point is a **membership endpoint** of `S` when its explicit DAG provenance
  proves that it lies on `S` at one exact semantic address;
- a point is **admissible** for `Length(S,P,Q)` / `LocusLength(S,P,Q)` when it is
  a membership endpoint of `S`, or an approved §21 constructor occurrence
  transported through R5 lineage (the only admitted non-membership case).

A materialized point is not thereby on any given curve, and a point on a curve is
not thereby an endpoint on another one. Coordinates, Cartesian coincidence,
proximity, tolerance, output order, labels, construction index, rendering and XML
position never establish, disambiguate or repair any of the three.

### 24.2 Admitted families and dispatch

| Order | Family | Kind | Membership | Clause |
|---|---|---|---|---|
| 1 | `SEMANTIC_POINT` — explicit semantic point whose source is `S` by reference | direct | yes | G9U0, retained |
| 2 | `PAIR_INTERSECTION_OCCURRENCE` — exact token of a Locus V2 pair root | direct | yes | §23 (`INTERSECTION_OCCURRENCE`) |
| 3 | `SINGLE_SOURCE_INTERSECTION_OCCURRENCE` — exact token of a Locus V2 root against an ordinary target | direct | yes | §24.4 |
| 4 | `GENERATOR_OCCURRENCE` — the dependent generator point of `S` | direct | yes | §24.5 |
| 5 | `CONSTRUCTOR_OCCURRENCE` — SplineV2 constructor slot | direct | on its own root spline; transport on R5 images | §21 |
| 6 | `SIMILARITY_IMAGE_OCCURRENCE` — point image under the same similarity that produced `S` | derived | iff its pre-image is | §24.6 |
| 7 | `DEPENDENT_COPY_OCCURRENCE` — dependent copy `Q=P` | derived | iff `P` is | §24.7 |

Dispatch is by the endpoint's explicit parent algorithm and the addressed
source's explicit parent algorithm, never by value search. Each family returns
the closed result `NO_ADDRESS`, `UNIQUE(key, address)`, `MULTIPLE` or
`UNRESOLVED_INVALID`. `NO_ADDRESS` falls through to the next family; every other
result is final.

For a fixed addressed source the **direct** families are structurally disjoint:
a membership endpoint of `S` depends on `S`, while a generator or constructor of
`S` is depended upon by `S`, so no point can hold two of them without a DAG
cycle, and the remaining pairs differ by parent algorithm. A **derived** family
is consulted only after every direct family returned `NO_ADDRESS`. A derived
point can coincide with a constructor occurrence only when a spline's
constructor list contains the derived point itself, for example both `P` and
`T(P)`, or both `P` and a copy `Q=P`; the endpoint's own §21 occurrence then
prevails. That priority is forced: letting the derived family win, or reporting
`MULTIPLE`, would change an approved §21 result, so every §21 result is
preserved unchanged.

### 24.3 Family retention and no retargeting

A metric algorithm retains the family it accepted and, for every family except
`SEMANTIC_POINT`, the family's versioned occurrence key. A later resolution to a
different family, or within the same family to a different key, is retargeting
and yields `INVALID_QUERY`. This makes §23.5's "including a change of endpoint
family" apply to every family. Ordinary recomputation, dormancy and reactivation
of the same occurrence keep the same family and key; the address may follow
ordinary geometric motion.

The current host lifecycle cannot bring a family change to a retained metric
object. A compatible redefinition of a participating endpoint into another
family is rejected atomically, and a replacement redefinition has replacement
identity semantics for its dependent closure. As in §21, direct family-change
execution is therefore `NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`, and the
retained family and key guard remains the fail-closed authority.

Occurrence keys are in-memory, versioned and never serialized:

```text
metric-endpoint/single-source-intersection-occurrence/v1
  | addressed-source=<S> | rich-result=<R> | token=<exact effective token> | point=<P>
metric-endpoint/generator-occurrence/v1
  | addressed-source=<S> | point=<P>
metric-endpoint/similarity-image-occurrence/v1
  | addressed-source=<S'> | point=<P'> | transform=<R5 similarity contract>
  | parameters=<durable IDs of the transform parameters, in input order>
  | inner=<pre-image key>
metric-endpoint/dependent-copy-occurrence/v1
  | addressed-source=<S> | point=<Q> | operation=dependent-copy/v1
  | inner=<copied key>
```

Identities are durable, and a missing durable identity yields
`UNRESOLVED_INVALID`. The one exception is an intermediate derivation step.

**Structural address of a derivation step.** A similarity image or dependent
copy that lies between the endpoint and its origin is addressed by its place in
the semantic derivation chain, relative to an explicitly identified derivation
root and construction lineage:

```text
derivation root     the innermost key: a direct-family occurrence with durable identities
+ operation kind    transform=<versioned R5 similarity contract>, for example
                    locus-v2/similarity/rotate-point/v1, or operation=dependent-copy/v1
+ step path         the nesting of inner keys; every step below the endpoint carries
                    step=derivation-step/v1 in place of point=<identity>
+ parameters        parameters=<durable IDs of the authoritative parameter objects>
+ lineage           addressed-source=<durable ID> at every step
```

Such a step needs no identity of its own, because the explicit input chain of
the endpoint's parent algorithms determines it. A durable point identity begins
only when a point first participates. If an intermediate step later gains one by
participating elsewhere, or is renamed, its address does not change. A step is
never identified by a global construction index, XML position, list or output
order, label, Java reference, Cartesian coordinate or proximity. The endpoint
itself, every addressed source, every transform parameter and the derivation
root keep durable identities.
The §21 and §23 keys are unchanged. A pre-image or copied `SEMANTIC_POINT` is
represented inside an `inner` key as
`metric-endpoint/semantic-point/v1|addressed-source=<S>|point=<P>`.

### 24.4 Single-source intersection occurrence

An endpoint belongs to this family only when all of the following hold:

```text
endpoint point
  -> parent is the exact-token intersection-point consumer
  -> its rich input is a single-source Locus V2 intersection result
  -> that result's semantic source has S's current locus identity
  -> the current effective root token
  -> the current point-admissible solution for that exact token
  -> its revision evidence: semantic revision, branch, component, canonical parameter
  -> one canonical branch/component address on S
  -> the existing LocusMetricPositionBinder2D
```

A result whose source is not `S` is `NO_ADDRESS`. A source match with a stale
revision, a non-current token, a missing branch, zero or several containing valid
components, or a containing component that differs from the evidence component,
is `UNRESOLVED_INVALID`. There is exactly one side, so this family never yields
`MULTIPLE`; two roots with coincident coordinates are two tokens with two
distinct parameters and are never merged. The address is built as in §23.4.

### 24.5 Generator occurrence

An endpoint belongs to this family only when `S`'s parent is the public
dependent-point Locus V2 algorithm and the endpoint is, by durable identity, the
generator descriptor's dependent point. Its raw parameter is the live value the
evaluator itself would apply: the driver point's path parameter for
`CIRCLE_POINT`, `SEGMENT_POINT` and `CIRCULAR_ARC_POINT`, or the true-coordinate
number for `SCALAR_STATE` and `LOCUS_BRANCH_POINT`. The provider canonicalizes it;
exactly one valid component of `generator.main` must contain it. A missing
descriptor, an undefined or non-finite driver value, or zero or several
containing components is `UNRESOLVED_INVALID`. The address follows driver motion;
the key does not.

### 24.6 Similarity-image occurrence

This family applies transformation covariance of semantic membership:

```text
P is a membership endpoint of L
and P2 = T(P)
and L2 = T(L)
and both derivations prove the same semantic transformation T
-------------------------------------------------------------
P2 is a membership endpoint of L2
```

It is provenance covariance, not coordinate inference. "The same `T`" means the
same authoritative transformation relation: the same versioned R5 similarity
contract, the same parameter objects by durable identity, and the same lineage.
Equal numerical values alone never establish sameness. `T(P)` and `T(L)` keep
their own durable identities; only membership is carried. `P` itself is not an
endpoint on `T(L)`, and `T(P)` is not an endpoint on `L`.

An endpoint `P'` belongs to this family only when all of the following hold:

- `P'`'s parent is `AlgoTranslate`, `AlgoRotate`, `AlgoRotatePoint`,
  `AlgoMirror` or `AlgoDilate` (exact classes) whose first input is a point `P`;
- `S'`'s parent is the R5 similarity algorithm with source `S`, and its
  versioned contract (`translate-vector`, `rotate-origin`, `rotate-point`,
  `reflect-point`, `reflect-line`, `dilate-origin` or `dilate-point`) names the
  transformation kind;
- both algorithms have the same command and the same number of inputs, and each
  transform parameter has the **same durable identity** in both; a source-side
  parameter without a durable identity is `UNRESOLVED_INVALID`;
- `P` is a membership endpoint of `S`: a direct family 1–4 of §24.2, a §21
  constructor of `S` when `S` is itself the root spline, or recursively a
  derived family 6–7. A §21 transport through R5 lineage is not membership and
  never composes.

The address is `P`'s address on `S` with `S'`'s identity substituted; R5 preserves
provider, branch, component and parameter. Distinct parameter objects of equal
value, including literals written separately in each command, are `NO_ADDRESS`.
`MULTIPLE` and `UNRESOLVED_INVALID` of the pre-image are inherited. A composition
depth above `LocusEvaluationSession2D.MAXIMUM_SAFE_ACTIVE_DEPTH` is
`UNRESOLVED_INVALID`.

### 24.7 Dependent-copy occurrence

An endpoint `Q` belongs to this family only when its parent is `AlgoDependentPoint`
(exact class) and its definition unwraps to exactly one point `P` other than
`Q`. The result is `P`'s membership resolution on `S`, with the copy key.
`CopyFreeObject` creates an independent point and is `NO_ADDRESS`. A copy of a
transported, non-membership occurrence is `NO_ADDRESS`.

### 24.8 Membership, transport and exclusions

This section supersedes the first exclusion of §23.8 (single-source points are
now admitted by §24.4) and retains the others:

- a membership endpoint of `S` does not address an R5 image `T(S)`: an
  intersection root, a semantic point, a generator or a copy on `S` is
  `NO_ADDRESS` on `T(S)`; `T(P)` built with the same parameter objects is admitted
  by §24.6 instead;
- the generator `E` of `S` does not address `T(S)` either; by the covariance
  rule of §24.6, `T(E)` built with the same transformation and parameter objects
  is the endpoint on `T(S)`;
- `S × S` pair roots remain `MULTIPLE` (§23.2);
- free, Cartesian-coincident, ordinary derived, list, expression, macro-output and
  free-copy points are `NO_ADDRESS`;
- a `GeoLocusV2` is not a `Path`, so no classic point-on-path endpoint exists.

### 24.9 Persistence, reopen, copy and redefine

Native save/reopen and undo/redo reconstruct every family from the command DAG,
durable identities, the rich results' persisted token ledgers and the live
generator state, then revalidate currentness; saved coordinates are never
sufficient evidence. Copy/remap creates new identities and therefore new,
internally consistent keys; the single authorized closure-copy token rebase
remains the only copy provenance path for intersection points. For a pair root,
that rebase also remaps every structural participant a source's
parameterization contract names, such as a generator's true-coordinate driver
(pair materialization spec §10), so the copied relation refers only to copied
identities. Redefine follows
the existing lifecycle and redefine contract; a resulting change of family, key,
side, token or identity is resolved afresh under §24.3 and never repaired by
proximity.

## 25. PRE-G9B-CU1 normative amendment: metric guarantee classes, estimator v2, admissibility and budgets

Status: architecture approved by the author on 2026-10-20 (instruction "Author
Approval of Metric Architecture and Implementation Authorization") together with
the obligatory precisions N1–N5, which this section incorporates. The section
supersedes the ordered capability list of §10 and refines §12, §13, §14 and §15
as stated there. Decision record: ADR 0037.

### 25.1 Guarantee classes

Every metric contribution belongs to exactly one guarantee class, recorded in its
error evidence together with its method and its work evidence:

1. **`STRUCTURAL`.** The value follows from a structural identity of the provider:
   - a semantic zero or collapsed image;
   - an affine image `|a|·Δu`;
   - a similarity image of a source with an established length (§25.6);
   - a similarity image, with driver-independent parameters, of a circle,
     circular-arc or segment driver whose length has a closed form.

   The guarantee is `CERTIFIED_ERROR_BOUND` with the rounding bound of the
   formula. `EXACT_ARITHMETIC` is reserved for exact zeros.
2. **`CERTIFIED_ENCLOSURE`.** A rigorous enclosure `[lower, upper]` of the semantic
   length, obtained from outward value and first-derivative enclosures of the
   semantic curve over a partition of parameter boxes. The lower bound is the sum
   of chord lower bounds. The upper bound adds, per box:
   - `chord_upper / cos_lower(α)` when the derivative enclosure of the box lies in
     a closed convex cone with apex at the origin and half-angle `α < π/2`;
   - `|box|·sup|F′|` otherwise.

   The cone bound follows from `F′(t)·u ≥ |F′(t)|·cos α` for the unit bisector `u`
   of the cone, which gives `|F(b) − F(a)| ≥ L·cos α`; the chord bound gives
   `L ≥ |F(b) − F(a)|`. A box that the enclosure model refuses is unresolved and
   prevents this class for the whole component.
3. **`ESTIMATED`.** A refinement estimate whose error amount is justified only by
   recorded hypotheses (§25.3). The guarantee is `ESTIMATED_ERROR`. Agreement
   between refinement levels or sample sets, however many, never produces class 1
   or 2.
4. **`NOT_ESTABLISHED`.** No value. The cause is one of: an exhausted work budget,
   a degenerate configuration, a failed consistency check or insufficient
   information. The value is `Absent` with a typed status and cause.

**Rigor of the certified classes (N5).** A contribution carries
`CERTIFIED_ERROR_BOUND` only when its complete computation yields valid bounds.
This covers the evaluation enclosures, norms, square roots, divisions, auxiliary
constants such as `π`, auxiliary functions and every rounding. Each
floating-point operation is rounded outward, one adjacent binary64 value beyond
the correctly rounded IEEE result, or by the documented ulp bound of a library
function. An operation without a rigorous enclosure in the available
implementation makes the certification fail closed: the contribution falls
through to the next capability and never inherits a partial certificate.

### 25.2 Capability order

The engine tries, in order:

1. structural capabilities;
2. certified enclosure;
3. differential estimate;
4. evaluator-only estimate;
5. unsupported.

A capability that cannot produce its class falls through to the next. Rigorous
partial evidence of an earlier capability (for example an enclosure wider than the
tolerance) is attached to the later result.

### 25.3 Estimator v2

Class 3 estimates, evaluator-only and differential, obey the following rules.

a. **Component tolerance.** The tolerance is
   `τ = max(eps_metric_abs, eps_metric_rel·S)`, where `S` is a translation-invariant
   extent of the whole component image and never a local chord. It is distributed
   over the leaves of the partition in proportion to their parameter share.
b. **Minimum partition.** Before any acceptance, the component is partitioned
   uniformly into `2^d_min` cells, split additionally at known semantic
   breakpoints. `d_min` is a versioned policy constant.
c. **Leaf acceptance.** A leaf is accepted only when both conditions hold:
   - the Richardson-corrected estimates of two consecutive levels agree within its
     share of `τ`;
   - an interior probe at a non-dyadic fraction of the leaf (a versioned constant)
     is consistent with the leaf's chord model within the same share.

   The reported value is the corrected sum. The reported error is a versioned
   safety factor times the level difference.
d. **Hypotheses of the correction (N3).** The correction
   `L ≈ S_fine + (S_fine − S_coarse)/3` rests on the chord expansion
   `s − c = κ²s³/24 + O(s⁵)` of a regular arc of length `s`.
   - It is justified on a leaf when the curve is `C⁴` there with nonvanishing speed
     and the leaf lies in the asymptotic regime of that expansion. Then the
     corrected leaf error is `O(s⁵)` and the corrected total `O(h⁴)`.
   - Piecewise `C²` regularity alone guarantees only that the uncorrected chord
     deficiency tends to zero, at the rate `O(h²)` where the curvature is bounded
     and continuous. It does not justify an `O(h⁴)` claim.
   - No condition is verified by sampling. The estimator records the conditions as
     hypotheses, with their numerical scale (`d_min`, the finest probe spacing
     `h_probe`, the safety factor), and claims no convergence order.
   - Where the evidence contradicts a hypothesis on a leaf (a chord that vanishes on
     a leaf of positive parameter width, which indicates zero speed or a cusp), the
     correction is not applied there. The result then records an estimate without
     a demonstrated order.
e. **Failure.** A consistency failure that persists after the permitted refinement
   ends as `NOT_ESTABLISHED` with `CONSISTENCY_NOT_ESTABLISHED`. Reaching the
   parameter resolution without acceptance is reported as
   `PARAMETER_RESOLUTION_REACHED`, never accepted silently.

### 25.4 Evidence, width, tolerance and admissibility (N2)

Four notions are distinct and recorded separately:

1. **Rigorous evidence:** a certified enclosure `[lower, upper]` of the semantic
   quantity, when one exists. Its mathematical validity does not depend on its
   width, and the rich result retains it as rigorous evidence however wide it is.
2. **Width:** `upper − lower`.
3. **Requested tolerance:** `τ` (§25.3a).
4. **Scalar admissibility:** whether the published value may feed the scalar
   adapter.

The published value and its guarantee are chosen as follows:

- **Certified.** When the certified error of the published value (its largest
  distance to the enclosure ends) does not exceed `τ`, the value is published with
  `CERTIFIED_ERROR_BOUND`.
- **Estimated, with rigorous evidence.** Otherwise, when an estimate exists whose
  estimated error does not exceed `τ`, the estimate is published with
  `ESTIMATED_ERROR`, and the enclosure is attached as rigorous evidence. The
  estimate must lie inside the enclosure, up to the outward rounding of the
  comparison. If it does not, it is rejected with `CONSISTENCY_NOT_ESTABLISHED`
  and only the certified evidence is published.
- **An estimate stays an estimate.** An estimate that satisfies `τ` remains
  `ESTIMATED_ERROR`; meeting the tolerance never makes it certified.

The scalar adapter publishes a value only in two cases: a `CERTIFIED_ERROR_BOUND`
value whose certified error does not exceed `τ`, or an `ESTIMATED_ERROR` value whose
estimated error does not exceed `τ`, under the estimate contract of §25.5. Otherwise
the scalar is undefined with its typed cause, while the rich result keeps every
piece of evidence. The rich result and the scalar adapter report the same value.

### 25.5 Use as a DAG input

The admissibility of a metric value as an input of other constructions depends on
its class:

- **Classes 1 and 2.** A certified input: downstream certificates may use its
  enclosure.
- **Class 3.** An ordinary numeric input under the *estimate contract*. It
  propagates through normal dependencies, but every downstream metric result that
  depends on it carries at most `ESTIMATED_ERROR`. No downstream certificate
  (existence, intersection, materialization or metric) may rely on it as a rigorous
  input: a certifier whose captured program contains such a value treats it as
  uncertified.
- **Class 4.** Makes its consumers undefined through the normal kernel DAG.

### 25.6 Propagation through transformations (N1)

A geometric transformation never improves the guarantee class of its source. For a
similarity image with length factor `λ` (the absolute scale factor; 1 for an
isometry), the image length is `λ·L_source` and:

- **Value:** `λ·v`, rounded outward when the source is certified.
- **Errors:** the absolute error becomes `λ·e_abs` plus the rounding bound of the
  product. The relative error is unchanged, plus that rounding bound.
- **Class and hypotheses:** the source's. An estimated source yields an estimated
  image that carries the source hypotheses and the transformed error. A certified
  source yields a certified image only when `λ` itself is enclosed rigorously (a
  captured binary64 parameter, or an outward-rounded product of such parameters)
  and the product is rounded outward. Otherwise the image falls through to its own
  capability.
- **Degenerate cases:**
  - `λ = 0` (a dilation by zero) yields the collapsed image, with exact zero
    length;
  - an undefined or non-finite `λ` yields `NOT_ESTABLISHED`;
  - a negative factor contributes `|λ|`.

### 25.7 Budgets and failures

- **Scope.** Work budgets apply per component-state build and per query, with
  nested work charged to the caller through the slice-scoped child session of the
  semantic contract.
- **Values.** Budget values are versioned policy constants justified by
  reproducible benchmarks; they are not mathematical constants.
- **Failure memoization.** A deterministic failure is memoized under its complete
  key (identity, revision, branch, component, capability and policy). It is
  reported with its typed work dimension in the rich result and is not recomputed
  until the key changes. A failure caused by a caller's session limit depends on
  the caller and is never memoized.
- **No universal budget.** No fixed budget is claimed to suffice for every regular
  component. The cost of classes 2 and 3 grows with the total turning of the
  component and with `1/√τ`.

### 25.8 Determinism and invariance (N4)

For a fixed revision and policy, results are bitwise deterministic and independent
of view, zoom, DPI, render state and presentation size. Translations leave the
results unchanged up to rounding.

Under a similarity of the whole construction with factor `λ`, invariance holds only
in the relative regime, that is, when `eps_metric_rel·S ≥ eps_metric_abs` for every
component involved:

- **Relative regime.** Classes and relative errors are invariant up to rounding.
  The work is invariant except where rounding changes a refinement decision.
- **Absolute floor.** Where the floor dominates (`S < eps_metric_abs /
  eps_metric_rel`), the tolerance is absolute. Neither the relative tolerance, nor
  the class, nor the work is required to be invariant there.

### 25.9 Between-position queries on cached states

A between-position query against a cached component state performs local work
bounded by the leaves that contain its endpoints. It neither rebuilds the component
state nor spends a fresh complete work budget. Its error evidence combines the
evidence of the whole leaves it covers with the local evidence of the partial
leaves, and its class is the weakest of the two.

### 25.10 Versioning

The public policy identifier becomes `cu1-metric-policy/v2`. The algorithm, policy
and tolerance versions take part in the complete index key and in the result
provenance.

### 25.11 Validation (N5)

- **Estimates.** Validation demonstrates honest classification. Adversarial curves
  must never receive class 1 or 2: aliasing frequencies, features narrower than the
  probe spacing, and curves that vanish on the probe sequence. The actual error of
  an estimate is not required to lie within its estimated error for arbitrary
  curves. Its hypotheses are recorded and accessible in the rich result.
- **Certified classes.** Validation demonstrates that every certified bound
  contains an independent reference value, through every auxiliary operation.

### 25.12 Implementation definitions of the CU1 candidate

The CU1 technical candidate fixes the following definitions, which §25.1–§25.9 leave
open. The author adopted them as part of the CU1 contract (decision D-CU1-R2,
[CU1 closeout record](../../../docs/validation/pre_g9b_cu1_closeout_record.md)). The
adoption preserves the distinction between structural length, certified enclosure,
numerical estimate and result not established (§25.1); the hypotheses of an estimator are
not by themselves a mathematical certification.

1. **Extent.** `S` is the largest distance from the start point of the component to
   the points of its minimum partition (§25.3b). It is invariant under translations
   and rotations and scales with every similarity.
2. **Leaf acceptance.** A leaf is accepted when its reported error does not exceed
   its share of `τ`, so every accepted estimate is admissible (§25.4). The reported
   error is the larger of the safety factor times the level difference and the
   inconsistency of the probe:
   - evaluator-only: the length by which the inscribed polyline, refined by the
     probe point, exceeds the corrected value;
   - differential: the difference between the speed at the probe and its quartic
     interpolant through the five samples of the leaf, times the leaf width.
3. **Cells below the parameter resolution.** A cell born narrower than its
   refinement stencil, such as a route end a few units in the last place from a leaf
   boundary or a knot, has no representable interior parameter. Its chord, or its
   trapezoid, is accepted under the recorded hypothesis that no feature is narrower
   than the parameter resolution, without a demonstrated order. A leaf that reaches
   the resolution through refinement fails with `PARAMETER_RESOLUTION_REACHED`
   (§25.3e).
4. **Similarity images (N1).** An image is measured through its source with the
   tolerance `min(τ_source, τ_image / λ_upper)`. A stricter tolerance can only weaken
   the class, and the scaled error meets the tolerance of the image.
5. **Consistency of certificates.** A class 1 or class 2 enclosure whose upper bound,
   plus `τ`, is exceeded by the inscribed polyline of the minimum partition does not
   describe the evaluated curve; the certification fails closed.
6. **Outward rounding.** Sums and products round outward according to their exact
   rounding error (TwoSum, Dekker's product), so an exact operation stays exact;
   outside the range where that error is exact, and for divisions, the result moves
   one adjacent binary64 value outward.
7. **Class 2 work.** Boxes are split until each lies within its share of `τ`. The
   construction stops early, and keeps the enclosure as evidence, when the predicted
   box count exceeds the box budget; for each box over its share, the prediction is
   the square root of its width-to-share ratio.
8. **Direction reversals (N3).** A reversal of direction close to a sample, such as the
   turning point of the foot of a perpendicular, hides the same overshoot from every
   refinement level, so the level difference cannot reveal it. The evaluator-only
   estimator detects consecutive chords, longer than the rounding noise of their points,
   that turn by more than a right angle. It locates the extremum of the projection of the
   curve on the incoming chord by a golden-section search and uses it as a partition
   boundary. The hypotheses record the number of located reversals and that no
   unlocated reversal lies between consecutive samples.

## 26. PRE-G9B-CU2 normative amendment: structural certificate of a metric function and certified topology of metric consumers

Normative text of amendment A2.3-R and of the metric part of A2.5 of the
[`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
approved as architecture by the author on 2026-10-21 with ADR 0038 and decision D-CU2-3,
option A ([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)).
It was approved with `PRE-G9B-CU2` (`PASS — AUTHOR APPROVED`, 2026-10-24; [closeout record](../../../docs/validation/pre_g9b_cu2_closeout_record.md)). No
`LocusLength` value, traversal policy, same-position policy, order or direction changes.

### 26.1 Structural certificate of a metric function

For `LocusLength(L, A, B)` whose target `B` is the generator occurrence of the driver of `L`
(§24.5) and whose origin `A` is a fixed semantic position on a certified component `C`, the
rich result publishes, as revision-scoped derived data that is not persisted, the structure of
the function of the driver it defines: its definition domain (where the route exists), its
continuity away from its events, and its events. These structural facts derive only from the
certified structure of `C`, from semantic addresses and from the route policy; they never
derive from the computed value.

### 26.2 Origin wrap event

When `C` is a certified closed periodic component, the structure contains one event
`ORIGIN_WRAP` at the driver parameter equal to the canonical address of `A`, with the traversal
direction, the same-position policy, its two one-sided ends, a reference to the total length of
`C` with its own guarantee class, and `jumpPositive`. `jumpPositive` is `CERTIFIED` when an
outward-rounded chord between two certified points of `C` is positive; otherwise it is
`NOT_ESTABLISHED`. The two ends of the cut share the canonical address of `A` and are distinct:
each carries its side, its inclusion under the same-position policy and its one-sided limit. The
certificate establishes that both limits exist; their numerical evaluation is a separate fact.
The event changes the value of no query.

### 26.3 Clarification of §25.5

A class-3 value is never a rigorous input of a certificate. The structural certificate of a
metric function is not a value and may be used by an existence certificate.

### 26.4 Certified topology of metric consumers

These rules refine §22, §23.4, §24.4 and §24.5 for the existence structure of
`locus-v2-existence-components.md` §15:

- an endpoint address (§23.4, §24.4, §24.5) requires a certified component, even a local one,
  that contains the canonical parameter; zero or several containing components remain
  `UNRESOLVED_INVALID`;
- a total length and the `full-cycle` and `wrap-to-start` policies of §22 require `COMPLETE`
  or a certified closed periodic component, except the conditioned estimate of a total length
  of §26.5; on an open arc the route follows the arc coordinate across the canonical seam and
  its ends are reached through their one-sided limits;
- a point component contributes zero to every total and admits only a route of length zero
  within itself; it cannot be addressed through a binary64 parameter.

### 26.5 Conditioned estimate outside the certified class

Normative precision of author decision H10, option A (author resolution before `CU2` step T12,
2026-10-22). It was approved with `PRE-G9B-CU2` (`PASS — AUTHOR APPROVED`, 2026-10-24; [closeout record](../../../docs/validation/pre_g9b_cu2_closeout_record.md)). It preserves the class-3 estimation of `CU1` (§25.3) for constructions outside class
v3 without attributing certified existence or completeness to them.

1. **Eligibility.** The total length of a branch that is not `COMPLETE` is a *conditioned
   estimate* only when its existence structure establishes no fact (no component, point
   component, singular parameter or fold) and every coverage item is either `UNRESOLVED` with
   cause `OPERATION_OUTSIDE_CLASS`, a declared piece, or `CERTIFIED_DOES_NOT_EXIST` with cause
   `PROVIDER_COMPLEMENT`, the complement of the declared domain. Any other cause, certified
   nonexistence inside the declared domain and a partial certificate keep the total refused as
   an incomplete aggregate (§26.4). Between-position queries, route policies, endpoint
   addresses and selectors never use it.
2. **Value.** Each declared piece is measured by the estimator v2 of §25.3 alone (§25.2, steps 3
   and 4): no structural capability or certified enclosure is tried, and no rigorous enclosure
   is attached. A declared piece of zero length and a collapsed image contribute an estimated
   zero. An excluded end of a declared piece is never evaluated and keeps
   `LIMIT_NOT_ESTABLISHED` (§16). The whole period of a periodic driver domain is measured up to
   its closing driver position.
3. **Class and evidence.** The result is at most `ESTIMATED` (`ESTIMATED_ERROR`). Its error
   evidence records the estimator's hypotheses and estimated error, preceded by the hypothesis
   that the curve exists and is continuous on each declared piece and, for a whole period, that
   it closes at the seam; neither is certified. Each declared piece carries the diagnostic
   `EXISTENCE_NOT_CERTIFIED`, and the provenance names the conditioned estimate.
4. **No authority.** The conditioned estimate establishes and changes nothing outside its metric
   result: neither existence, continuity, completeness, components, closure, intersections,
   topology, endpoint addresses nor lineage. The existence structure keeps its unresolved
   coverage. The value is of class 3 under §25.5 and §26.3 and is never a rigorous input.
5. **Failures.** An estimator failure, an exhausted work budget, a limit that is not established
   and an evaluation failure keep their typed status; the estimate never replaces them.
6. **Scalar adapter.** §25.4 applies unchanged: the scalar publishes the value when its
   estimated error does not exceed `τ`, as an estimate under the contract of §25.5, never as a
   geometric certificate.

### 26.6 Partial geometric proof and complete conditional numeric traversal (P2)

Version `partial-conditioned-estimate/v1`, author scientific decision of2026-11-17.
Historical H10 (§26.5) stays unchanged. This additive mode concerns total length
on one bounded, closed, nonperiodic declared branch. It requires at least one
certified continuous component and unresolved coverage compatible with the
estimator's hypotheses. The numeric traversal is the entire declared domain,
never a subtotal of proved components. It uses estimator v2, its unchanged
budgets, tolerances, error hypotheses and ordinary revision-scoped metric owner.
Eligible coverage must classify every point of the requested interval, including
both closed ends and every join. An omitted interval or a point omitted by two
open sides is not an eligible unresolved cause and keeps the total refused.
This completeness of classified coverage does not establish geometric completeness.

The initial eligible unresolved cause is `NUMERIC_PROOF_METHOD_LIMIT`. It names
a trigonometric interval-prover range limit, still UNRESOLVED, whose native zero
branch is uniformly excluded and whose captured program has exactly one trig
operation and only total smooth Cartesian/scalar operations (division only by a
finite constant with magnitude at least1). No metric input, root, variable
division, selected intersection or geometric validity predicate may hide behind
the unavailable enclosure. This is no enlargement of the prover's range1000 and
no new geometric certificate. The native-branch guard uses Lipschitz1 with an
outward radius and downward |StrictMath.sin/cos(c)| minus radius minus4*ulp(1.0)
above1e-8, rejecting nonfinite inputs. The margin bounds StrictMath and Math
one-ulp realization errors. Generic NOT_CERTIFIED is not eligible.

Certified nonexistence/boundaries, isolated or singular parameters, folds,
topology/events not covered by this policy, native tolerance bands, uncertain
selection, fidelity or upstream prerequisites, improper limits and exhausted
budgets stay inadmissible. Evaluation/integration failure keeps its cause;
error above requested tolerance never publishes a scalar. For genuine generic
SCALAR_SIN zero thresholds, a successful sampled estimate does not establish
continuity/C4. JUMP-CONTEXT (2026-11-18; certified-construction-model §13.4)
corrects the earlier attribution to N03/N05/N06: their Cartesian inline sine
uses evaluateDouble without that branch. Their admission follows their actual
captured construction and current geometric/metric evidence, never that raw
sampled success. A numeric-geo sine producer retains its generic zero branches. This decision supplies no
piecewise jump-length policy and changes no scalar-trigonometry authority.

The immutable rich result retains the geometric coverage as INCOMPLETE and
unchanged existence structure/completeness. Typed numeric-traversal evidence
records its version, branch, orientation, exact requested domain, source
revision and independent numeric coverage. Only a successful entire traversal
has numeric COMPLETE. Scalar admission may then bypass geometric COMPLETE
exclusively with this typed evidence, matching revision, conditional role,
RECTIFIABLE finite result and ESTIMATED_ERROR with finite error<=recorded tau.
All other scalar guards remain. At most ESTIMATED is published, directly usable
by ordinary formulas through the existing adapter without another integral.
Error and its hypotheses are conditional, never an unconditional bound.

Existence, continuity, intersections, addresses, closure, topology, equivalence
and downstream certificate authority do not follow from this value (§25.5,
§26.3). Source existence and semantic identities never change. Typed API and
existing metric inspector expose condition, geometry, traversal, error,
tolerance, assumptions and failure diagnostics. No result payload is persisted:
FILE/MEMORY reconstruct through the existing rich/scalar command identities and
versioned policies. Inadmissibility and recovery follow the DAG; the scalar's
identity remains stable. Broader causes, branches, cuts and improper routes
require their own demonstrated admission before support is added.

## 27. GeoCeDG 1.2.0 endpoint assistance and numeric public result

The author enables two independent creation routes when a metric operand has
no admitted address. A proven endpoint is a new point in the construction.
The `locus-endpoint/translation/v1` recipe links a locus, a translated
reference, a witness on the same native segment or circle as the driver, and
the same construction vector in the traced and reference translations. The
vector and witness are independent of the instantaneous driver position. The
point binds the witness parameter in the retained branch and component.
Premise loss, unresolved selection and binding failure remain typed undefined
states. An explicit placement uses the existing reference-placement routes
and retains the selected forward address independently of the reference.

New `LocusLength` queries with one, three or six arguments publish the guarded
numeric result of §25.4. The rich query retains its guarantee, coverage,
revision, traversal and structural events. The scalar reads that single query
through its adapter. A newly authored scalar is ordinary by default. Its
internal rich dependency is auxiliary. The result inspector follows the
dependency and Algebra value mode shows the certified or estimated status.

Existing XML `LocusLength(locus, …)` still reconstructs the historical rich
output. Its `Length(richResult)` children and persisted style flags keep their
loaded meanings. New scalar children reconstruct through the typed XML-only
`LocusLength(richResult)` signature. This signature distinguishes the two
outputs independently of labels, document version and application defaults.
The scope and validation design are recorded in the
[1.2.0 design](../../../docs/architecture/geocedg-1.2.0-locus-design.md).

## 28. Conditioned use amendment (ADR 0042): lengths on conditioned regions

Amendment approved by the author on 2026-11-22 with
[ADR 0042](../../../docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md)
and the [conditioned-use contract](candidates/post-r1-conditioned-use.md), §5.3, §8.3, §8.4
and §12. Increment I3. Sections 1 to 27 are unchanged: §26.5 (H10) and §26.6 (P2) keep
their eligibility, their values and their admissibility, and are independent of this mode,
which adds only the cases that had no value.

### 28.1 Length between two positions

Two positions addressed by parameter
([public surface](locus-v2-public-surface.md), conditioned use amendment) are measured when
both lie in one conditioned region and the route in the requested direction stays inside
it.

- The value is the estimate of the stretch that the route travels, computed by the
  estimator of §25 on the evaluator of the source. Its class is at most `ESTIMATED`. It is
  never a rigorous enclosure, and its result records the diagnostic
  `EXISTENCE_NOT_CERTIFIED` with the hypotheses H1 to H4 of the spans and the hypotheses of
  the estimator.
- A position inside a band or an unexplored cell is a valid position with no route:
  `POSITION_IN_BAND`. Positions in two regions: `POSITIONS_IN_DIFFERENT_REGIONS`, with the
  kinds of the bands between them. A route whose requested direction leaves the region:
  `ROUTE_CROSSES_BAND`. None publishes a value.
- A position at an end of a region is in the region, also where a band shares that end:
  its node is an evaluated valid sample, the end of a span. A whole region is therefore
  measured between its two ends, and that node is no position of the next region
  (corrections after block 3B, author authorization of 2026-11-22).
- Equal positions follow the existing same-position policies. A full cycle, and a route
  across the seam, exist only on a region that covers the whole period of a periodic
  provider with no band and with agreeing seam nodes; it is declared closed-conditioned and
  proves no closure.
- A position on a certified component and a position addressed by parameter have no common
  route.
- An evaluation that fails inside the stretch is a failure of the result, never a value:
  the estimator does not interpolate across it.

### 28.2 Total length

A branch that is not `COMPLETE` and has neither the total of §26.5 nor that of §26.6 is
measured over the conditioned evidence that its producer publishes.

- Every cell of the domain must be classified: an `UNEXPLORED` cell gives no value, with
  `UNEXPLORED_DOMAIN`.
- The total accounts for every cell. A conditioned region contributes its estimate; a
  certified component of the same branch contributes as before; an `ABSENT_BAND`
  contributes nothing, under its hypothesis. No other band has a computed contribution or
  a bound in this version: it is never dropped because it is narrow. The value is then a
  **subtotal** over the regions, published with incomplete coverage and the diagnostic
  `CONDITIONED_SUBTOTAL` that lists each omitted band with its kind and its interval.
  What no certificate covers and no cell classifies is listed the same way, as
  `UNCLASSIFIED`: the cell of minimum step at an end of a certificate, and an unresolved
  coverage narrower than that step.
- The class is at most `ESTIMATED`. The rich result carries the value.
- A result is computed again when the producer publishes another content of the evidence:
  its states are keyed by the content identity of the evidence they read.

### 28.3 Scalar admission of a route in one conditioned region

Version `conditioned-length-scalar/v1`. Decision DN-2 of the author, 2026-11-22
([authorization record](../../../docs/validation/post_r1_1_2_2_corrections_author_authorization_record.md)),
which takes the decision that the text of increment I3 left open. It replaces that text,
which admitted no conditioned length as a scalar.

A length measured on conditioned regions has incomplete geometric coverage: existence and
continuity are hypotheses there. The length **between two positions** is nevertheless
admissible as a scalar, and feeds ordinary formulas through the scalar adapter, when all of
the following hold.

1. **Positions.** Both are valid semantic positions of the current revision of the source,
   addressed by parameter.
2. **One region.** Both lie in one conditioned region and the whole route in the requested
   direction lies in it (§28.1). The hypotheses H1 to H4 of the spans of the route are the
   ones under which it is travelled.
3. **Only spans.** Every cell that the route travels is a span. No band of any kind and no
   unexplored cell lies in it; a discontinuity that the traversal found is a band.
4. **No excluded cause.** No coverage of the branch that overlaps the route has one of the
   causes with which the certificate names a possible event on it or an evaluator that does
   not reproduce the document: `INHERITED_TOLERANCE_BEHAVIOUR`,
   `SELECTION_NOT_REPRODUCED`, `RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`,
   `DEGENERATE_CONFIGURATION`, `COMPUTATIONAL_DEPENDENCY_FAILURE` and
   `UNLABELLED_COINCIDENT_OUTPUT`. Under any of them the same estimate is published as a
   rich result, with `CONDITIONED_ROUTE_NOT_ADMISSIBLE` and the cause, and is no number.
5. **Numeric coverage of the route.** The estimator of §25 measured the whole requested
   route: the status is `SUCCESS`, the value is finite, the curve is rectifiable there and
   the traversal reached its target. This numeric coverage of the route is not the
   geometric coverage of the locus, which stays incomplete.
6. **Error.** The guarantee is `ESTIMATED_ERROR` with a recorded tolerance, and the
   estimated error does not exceed it (§25.4). An estimate that does not converge keeps its
   own status and cause and is no number.
7. **Currentness.** The result carries the evidence of the route: branch, region, stretch
   measured, hypotheses, classes of its spans, version of the traversal policy, content
   identity of the conditioned evidence and semantic revision. It is the result of that
   revision and of that content; another revision of the source or another content of the
   evidence (a confirmed contradiction, another policy) computes it again, and the number
   that was published is never kept.

What the number is and is not:

| | States | Does not state |
|---|---|---|
| Value | the estimate of the stretch of the route | anything about the locus between the evaluated nodes |
| Error | class `ESTIMATED`: an estimated error within the tolerance, under the hypotheses of the estimator (§25.5) | a rigorous bound; it is never `CERTIFIED` |
| Conditioned evidence | valid evaluated nodes with their selection testimony, and the criteria of the traversal policy between them | existence, selection, continuity and resolution between the nodes: they are H1 to H4 |
| Certification | none | the existence structure stays as it was; measuring certifies nothing |

The admitted result keeps coverage `INCOMPLETE`, the diagnostic `EXISTENCE_NOT_CERTIFIED`
and adds `CONDITIONED_ROUTE_ADMITTED`. It is never by itself a proof of existence,
continuity, completeness, incidence or intersection. A construction that reads the number
reads an estimate, never a rigorous input (§26.3): a locus that depends on it is outside the
certified class.

Not admitted, and without a scalar as before: positions in different regions, a position
inside a band or an unexplored cell, a route that would cross a band, a position on a
certified component with a position addressed by parameter, and every **total** over
conditioned regions (§28.2), with or without omitted bands. A total stays governed by
§25.4, §26.5 and §26.6, which are unchanged: in particular the cases that §26.6 keeps
inadmissible for the total of a declared domain stay inadmissible for that total. The
bypass of geometric completeness of §26.6 stays exclusive to its typed numeric traversal,
and the bypass of this section to the typed evidence of a route in one region.

#### 28.3.1 PRE-G9B-MAINT-N1 candidate amendment: chord-scaling check (B5a)

Candidate text of 2026-11-23, written under the author authorization of increment
`PRE-G9B-MAINT-N1` and the direction of decision D-B-1a of the same day
([amendment record](../../../docs/validation/pre_g9b_maint_n1_metrics_amendment_record.md)).
**It is not in force.** Until the author adopts it, §28.3 above governs unchanged. Its
author-decision status is recorded only in that record and in later author records, never
here; the word «candidate» in its heading records the stage at which it was written. It
changes no other section, and §28.2 in particular.

On adoption, the admission of §28.3 becomes version `conditioned-length-scalar/v2`.
Conditions 1 to 7 are unchanged and one condition is added:

8. **Chord-scaling check.** The check of this subsection, under the policy
   `conditioned-route-jump-guard/v1`, decided every accepted leaf of the estimate of the
   route without observing the chord-scaling signature and without an inconclusive leaf,
   within its budget.

**What the check is.** A conservative numeric detection. Where it observes a numeric signature
compatible with a discontinuity or with a singular variation of speed, the continuity that H3
requires is not established by the check, and by conservative policy the scalar is not
admitted. The signature does not demonstrate that H3 is false: some continuous curves whose
speed varies extremely or is singular produce it too. The check therefore turns some
discontinuities that leave no trace at the nodes of the traversal into diagnosed refusals,
and may refuse some continuous routes. It is no proof of continuity and no certification: a
route that passes keeps H1 to H4 as hypotheses, exactly as under v1. Three situations stay
distinct:

| Situation | Meaning | Effect of the check |
|---|---|---|
| Continuity not certified, no adverse evidence | the normal state of every conditioned region: H3 is assumed and the check observed nothing against it | none; the route is admitted as under v1 |
| Numeric signature observed | a signature compatible with a discontinuity or with a singular variation of speed was observed on the route. The continuity that H3 requires is not established by the check; no discontinuity is demonstrated | by conservative policy the route is no number, with its own diagnostic |
| Real discontinuity not detected | H3 is false on the route, but the discontinuity is one that finite sampling cannot observe (declared limits below) | none; the number stays an estimate under a hypothesis that is false, as under v1 |

The absence of a signature never demonstrates continuity, and its presence never
demonstrates a discontinuity.

**Rationale.** Let `c(I)` be the chord of a parameter interval `I` and `I'` its half with the
greater chord. Near a parameter where the traced curve is `C^1` with nonvanishing speed,
`c(I')/c(I)` tends to `1/2`. At a stationary point the ratio along the halves that follow the
greater chord falls below `1` after a bounded number of levels. At an end where the speed
diverges as `|u − u*|^{−α}`, `0 < α < 1`, it tends to `2^{α−1}`. Across a jump of size
`J > 0` the chord tends to `J` and the ratio to `1`. A half that keeps the chord of the whole
over consecutive levels is the numeric signature that a jump produces; a continuous curve
whose speed is singular or varies faster than the levels resolve can produce it as well
(conservative refusals below). It shows also where all the chord levels of the estimator
agree and the traversal sees nothing (probe P-1: a jump aligned with the chord).

**Procedure.** For every leaf `λ` that the estimator of §25.3 accepted on the route, in
increasing parameter order:

1. `I_0` is the quarter of `λ` with the greatest chord, among the quarter chords that the
   estimator evaluated; a tie goes to the quarter of lowest parameter. The count `r` of
   consecutive high ratios starts at `0`.
2. For `k = 0, 1, …, L − 1`, in this order:
   - if `c(I_k) ≤ ε_res(λ)`, the leaf ends **below resolution**;
   - if no midpoint of `I_k` is representable strictly inside it, the leaf ends
     **inconclusive** when `r > 0` or `k = 0`, and **without signature** otherwise;
   - the traced point is evaluated at the midpoint of `I_k`. A failed evaluation is a failure
     of the result inside the stretch (§28.1): no number;
   - `q_k = c(I_{k+1}) / c(I_k)`, where `I_{k+1}` is the half of `I_k` with the greater chord
     (the lower half on a tie). A ratio that is not finite ends the leaf **inconclusive**;
   - if `q_k > ρ_J`, `r` increases by one, and the leaf ends with the **signature observed**
     when `r` reaches `m`;
   - otherwise `r` returns to `0`, and the leaf ends **without signature** when fewer than
     `m` levels remain (`L − 1 − k < m`).
3. A leaf still undecided after level `L − 1` ends **inconclusive** if `r > 0`, otherwise
   **without signature**.

The check stops at the first leaf with the signature observed; an inconclusive leaf does not
stop it. The route satisfies condition 8 when every leaf ended without signature or below
resolution before the budget was spent. Otherwise the same estimate is published as a rich
result with `CONDITIONED_ROUTE_NOT_ADMISSIBLE`, as under condition 4, and is no number. The
result carries each diagnostic that applies:

| Diagnostic | When | What it states |
|---|---|---|
| `CHORD_SCALING_JUMP_SIGNATURE` | a leaf ended with the signature observed | a numeric signature compatible with a discontinuity or with a singular variation of speed was observed in the direction of the chord, inside the interval of the decision. The continuity that H3 requires is not established by this check and, by conservative policy, the scalar is not admitted. It demonstrates neither a jump nor that H3 is false; the name of the diagnostic names the signature, not its cause |
| `CHORD_SCALING_INCONCLUSIVE` | no signature, and a leaf ended inconclusive | the check could not decide on that leaf: the parameter resolution was reached during a run of high ratios, or a ratio was not finite |
| `CHORD_SCALING_BUDGET_EXHAUSTED` | no signature, and the budget was spent before every leaf was decided | the check was not completed; nothing is concluded about the leaves left untested |

Each diagnostic records the leaf, the interval of the decision, the ratios observed, the policy
version and the evaluations spent. An admitted result records with its hypotheses the policy
version, the leaves tested, those below resolution, the evaluations spent and the statement
«no chord-scaling signature observed; this is not a proof of continuity».

**Policy.** `conditioned-route-jump-guard/v1`. Its quantities are pure numbers or
world-coordinate lengths; zoom, DPI, viewport and render policy never enter it. Its version is
part of the evidence of the route (condition 7): another version computes the admission again.

| Parameter | Candidate value | Interval admissible for `MAINT-B5a` |
|---|---|---|
| `ρ_J`, ratio threshold | `0.85` | `[0.80, 0.90]` |
| `m`, consecutive high ratios | `3` | `{3, 4}` |
| `L`, levels per leaf | `5` | `m ≤ L ≤ m + 3` |
| `ε_res(λ)`, chord resolution | `max(τ_λ, 2^8 · u · M_λ)` | the same form, with a factor from `2^4` to `2^12` |
| `E_cap`, evaluations per route | `8 192` | `[2 048, 16 384]` |

`τ_λ` is the share of the tolerance of the leaf (§25.3), `u = 2^{−53}` the unit roundoff and
`M_λ` the greatest absolute coordinate of the evaluated nodes of the leaf. `M_λ` depends on
the absolute position of the leaf, not only on its geometry (numeric representation and
translations, below). `MAINT-B5a` fixes
the final values inside these intervals with its focal tests and records them in the policy and
in its report. A value outside an interval needs a new amendment.

The candidate values follow from an analysis in a collinear model, the worst case of an
aligned jump, recorded in the amendment record:

- a jump of size `J`, in a leaf whose quarters have width `h_0`, at speed `s`, is signalled
  when `J` exceeds about `2.36 · s · h_0 / 2^{L−m}`, that is `0.59 · s · h_0` with the
  candidate values;
- an end with divergent speed is signalled when `2^{α−1} > ρ_J`, that is
  `α > 1 + log2 ρ_J ≈ 0.766`; a fold end (`α = 1/2`, ratio `0.707`) is not;
- a stationary point of order up to `26` gives no signature at any alignment examined; the
  parametrizations `u ↦ u³` and `u ↦ u⁸` give none on any leaf of a uniform partition;
- a leaf without signature costs `L − m + 1 = 3` evaluations, any leaf at most `L = 5`.

**Budget.** The check spends at most `L` evaluations per leaf and at most `E_cap` per route.
They are counted apart from the work budget of the estimator (§25.7). They are not leaves,
change neither the value nor the error of the estimate, and are never reused across
revisions. Spending `E_cap` before every leaf is decided gives
`CHORD_SCALING_BUDGET_EXHAUSTED` and no number: the conservative choice. `MAINT-B5a` must show
that it does not happen on the routes admitted today.

**Declared limits.** Finite sampling cannot detect these real discontinuities, which are
violations of H3, and the check does not claim to:

1. a removable discontinuity: one different value at a parameter that is never evaluated;
2. an excursion that leaves and comes back between two evaluated parameters, including a jump
   and its return inside one interval;
3. a jump below the resolution of the check: not above `ε_res(λ)`, or too small for its chord
   to dominate the chord of the continuous part at the levels examined;
4. a jump in a quarter or a half that the check does not follow, because another one carries a
   greater chord;
5. a jump in a cell narrower than the refinement stencil (§25.3) whose chord does not exceed
   `ε_res(λ)`;
6. a change of selection that keeps the traced position continuous: a violation of H2, not a
   jump of the traced function.

**Conservative refusals.** The check may refuse, with `CHORD_SCALING_JUMP_SIGNATURE`,
continuous routes that have no jump: an end whose speed diverges with `α > 1 + log2 ρ_J`; a
stationary point of very high order (from `27` in the analysis); a speed that varies faster
than the leaf resolves, as in a spike or a fast oscillation next to a feature. In each of them
the signature is observed and the continuity that H3 requires is not established by the
check, but no discontinuity is demonstrated. No result reports a proven jump or a proven
failure of H3.

**Scope.** The check runs only on a route that satisfies conditions 1 to 7 and does not change
their causes: an estimate that failed or did not converge keeps its own status and cause, as a
fold end does today. It reads only the estimate of the route and evaluations of the traced
point. It changes nothing in the traversal and its policy (`conditioned-traversal/v1`), the
content identity of the evidence, the existence structure, the component inventory, the
tokens, the semantic identity or the existence certificates; in the value and the error of the
estimate; in §26.5 (H10), §26.6 (P2), §28.1 and §28.2; in `Intersect` and its isolation
policy; or in serialization. It applies to no total. Deferred: the same check inside the
traversal (`MAINT-B5b`, `conditioned-traversal/v2`) and the admission T-A (D-B-1c).

**Determinism and invariance.** For a fixed revision, policy and tolerance, the leaves, the
evaluations and the outcome are the same. Zoom, DPI, viewport and render policy never enter the
check: the outcome does not depend on them.

**Numeric representation and translations.** The mathematical check is invariant under
translations: the chords, their ratios and the tolerance share `τ_λ` (§25.3, built on a
translation-invariant extent) depend only on the relative geometry. Its binary64
implementation is not exactly invariant. The evaluated points carry a rounding error that
grows with the magnitude of their absolute coordinates, a chord computed as a difference of
large coordinates loses relative precision, and the term `2^8 · u · M_λ` of `ε_res(λ)` grows
with `M_λ` by construction. A translation of the whole construction may therefore change a
decision of the check, not only the rounding of a value: a leaf may end below resolution
instead of being tested, a ratio near `ρ_J` may fall on the other side, and the evaluations
spent and the diagnostics may differ. The declared limits 3 and 5 widen as the coordinates
grow: a jump observed near the origin may go unobserved far from it, and a translated route
may be admitted where the untranslated one is refused, or the reverse. The check claims no
exact geometric invariance under translations; the statement of §25.8 that translations leave
results unchanged up to rounding holds for it with this proviso, since here rounding may change
a decision. The form of `ε_res(λ)` stays a candidate and is not changed without
characterization. Before the final policy is approved, `MAINT-B5a`:

- tests the same curves (the probe P-1, the adversarial battery and the continuous references
  of no regression) under translations of increasing magnitude, up to and beyond the
  magnitude at which `2^8 · u · M_λ` exceeds `τ_λ`;
- compares, for each translation and against the untranslated curve, the admissibility, the
  evaluations spent by the check and the diagnostics, and reports every divergence with the
  magnitude at which it appears;
- justifies the resolution factor that it chooses inside the interval of the policy, from the
  rounding of the chord computation and those tests, and declares the range of coordinate
  magnitudes over which the outcome was shown to be unchanged.

Another form of `ε_res(λ)` needs that characterization and a new amendment.

**Compatibility.** No XML changes and no payload is persisted. A document of any version with
`LocusLength(L, A, B)` on a conditioned route whose check observes a signature, is
inconclusive or spends its budget shows an undefined number where 1.2.2 shows one; its rich
result keeps the estimate with the diagnostic, and formulas that read the number become
undefined through the DAG. The release note of `MAINT-B5a` declares it.

**Validation by `MAINT-B5a`.** The acceptance criterion of the author (2026-11-23):

- the probe P-1, line and arc, gives no number; the stretch before the jump stays a number; a
  fold end keeps its current cause;
- the adversarial battery: aligned jumps at angles from `0` to `0.35 rad` to the chord, sizes
  from the resolution upwards by orders of magnitude, dyadic and non-dyadic positions, next to
  an end of the route, several jumps in one route, a jump and its return in one cell, fast
  oscillation next to the jump. What the check cannot refuse is fixed by a test as a declared
  limit;
- no regression of admissibility: `u ↦ u³`, `u ↦ u⁸`, stationary points of orders 1 and 2,
  narrow closed loops and high curvature, divergent speed with `α` below the threshold, and
  every conditioned route admitted today in the author models (`w_2`, `a_3`, `trHole`) and in
  the post-R1 batteries, measured before and after with the evaluations of the check;
- the three diagnostics, each produced by a test, distinct from each other;
- the translation tests and the justification of the resolution factor of «Numeric
  representation and translations»;
- unchanged: the pinned traversal test
  `aJumpAlongTheChordLeavesNoTraceAndIsNotDetected` and
  `aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber`.

## 29. Life cycle of the private rich authority of a number (GeoCeDG 1.2.2)

Closing check of GeoCeDG 1.2.2, author instruction of 2026-11-22. It states an existing
rule of §13 and §27 for one case and corrects its implementation; §1 to §28 are unchanged.
Its author-approval status is recorded only in the closing record of 1.2.2, never here.

A `LocusLength` command, and a `Length` command whose rich result takes the reserved name,
creates one public number and one **private rich authority** for it (§27): an auxiliary rich
result in the reserved namespace of the construction, which no name of the document can
read. The authority is the dependency of that number alone.

- **Deletion.** When the number is deleted, by the ordinary deletion of the application or
  by the kernel, its private authority is removed with it if no other algorithm reads it.
  An authority that another algorithm still reads stays, and goes with its last reader.
  Until 1.2.2 the authority stayed in the document without a reader, was saved and
  reopened, and was computed again at every change of its source. Undo restores the number
  with its authority; Redo removes both.
- **The private name is the criterion.** It is persisted, so a reopened document keeps the
  distinction. A rich result with an ordinary name, as those that `LocusLength` created
  before 1.2.0 and that a document names itself, is never removed with a number that reads
  it: the document owns it.
- **Declared limit: redefinition.** A redefinition of the number is a transaction of the
  spatial identity that replaces the number and removes nothing else. The authority of
  before stays in the document without a reader, as in 1.2.0 and 1.2.1, and the user
  cannot reach it. Removing it belongs to the redefinition transaction and is not part of
  1.2.2. To give up a length, delete it; to change it, delete it and create the new one.
- Nothing else changes: the authority is computed, published, copied and persisted as
  before, and a number with a reader keeps its value and its guarantee.

### 29.1 PRE-G9B-MAINT-N1 candidate amendment: removal at the end of a redefinition (B2-F1)

Candidate text of 2026-11-23, written under the author authorization of increment
`PRE-G9B-MAINT-N1` and decision D-B-2, approved with its scope reduced to B2-F1
([amendment record](../../../docs/validation/pre_g9b_maint_n1_metrics_amendment_record.md)).
**It is not in force.** Until the author adopts it, §29 above governs unchanged, its declared
limit included. Its author-decision status is recorded only in that record and in later author
records, never here. On adoption it replaces the item «Declared limit: redefinition» of §29;
the other items are unchanged.

- **Candidate.** When a redefinition replaces the number, its private authority becomes a
  removal candidate of that redefinition operation instead of staying. Nothing is removed
  then: the identity guards of the redefinition transaction, which refuse to publish a graph
  change while the redefinition is in progress, stay as they are.
- **Removal at completion.** When the replacement of the number has succeeded, and for a batch
  of redefinitions after its final switch, every candidate that the construction still holds
  without a reader is removed by the ordinary removal of the item «Deletion». The removal runs
  outside the publication lease of the redefinition and before the Undo point of the
  operation is stored. It belongs to the same operation: the Undo point records the state
  without the authority, and no Undo point records the intermediate state.
- **Criterion.** An authority is removed only when all of the following hold at completion: it
  has a private name in the reserved namespace of the construction (above); it was the rich
  input of the number that the redefinition replaced; and no algorithm reads it. Never
  removed: a rich result with an ordinary name, which the document owns, such as those that
  `LocusLength` created before 1.2.0; an authority that another algorithm reads, including the
  number that the redefinition creates; a result with its own identity and use; anything that
  the redefinition did not make a candidate.
- **Atomicity.** The redefinition of the number and the removal of its candidates are one
  logical operation:
  1. the operation succeeds only when the replacement and every mandatory removal have
     completed; no success is declared while a mandatory removal is incomplete;
  2. no partially updated state, such as the redefined number with its previous authority
     still present after a failed removal, is published as the final result;
  3. when the removal fails after the number has been replaced, the operation restores the
     previous state coherently: the previous number, its private authority, their
     dependencies and the identities concerned, exactly as before the operation;
  4. a failed operation stores no Undo point;
  5. a successful operation keeps a single Undo/Redo step.
- **Failure.** A redefinition that fails or is reverted, before or after the replacement of the
  number, discards its candidates. The restored state holds the number with its authority,
  exactly as before the operation; nothing is removed in part.
- **Undo and Redo.** Undo of the redefinition restores the previous number with its authority.
  Redo restores the redefined number without the previous authority. Both are one step.
- **Condition on the implementation.** If the existing transactional mechanisms cannot
  guarantee the coherent restoration of item 3 of «Atomicity» when the removal fails after
  the replacement of the number, the implementation `MAINT-B2` stops and asks for an
  architectural decision before it changes the kernel. This text authorizes no new
  transactional infrastructure, no change to the identity guards and no design of a general
  reconstruction.
- **Identity.** The redefinition keeps the identity of the number; it is never treated as a
  deletion followed by a creation.
- **No sweep when loading.** B2-F2 is not implemented (author decision of 2026-11-23). Opening
  a document, reading a file and rebuilding a state for Undo or Redo remove nothing. An
  authority without a reader that a document already holds, left by a redefinition before
  this amendment or by a deletion before 1.2.2, stays: it is saved and reopened as today and
  is computed again at every change of its source. This is a declared limitation, with no
  migration and no silent repair.

No XML changes. Loading an existing document does not change. A document saved after a
redefinition made under this text does not hold the authority that 1.2.2 would have left.

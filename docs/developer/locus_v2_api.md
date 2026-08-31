# Locus V2 developer API

| Field | Value |
|---|---|
| Maturity | `experimental`; public GeoCeDG surface disabled by default plus internal/developer seams |
| Normative semantics | [`geocedg/specs/locus/locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md) |
| Architecture | [`docs/architecture/locus_v2_implementation.md`](../architecture/locus_v2_implementation.md) |
| Additive metric API | [`docs/developer/locus_v2_metric_api.md`](locus_v2_metric_api.md), G7B internal candidate |
| Public command/API | G9U0 surface and G9U0-R5 similarity transformations `PASS — AUTHOR APPROVED` |
| Persistence / `Path` | Native `.cedg` persistence for public V2 objects / no generic `Path` conformance |
| Date | 2026-08-31 |

This reference documents the current Java seams so future kernel work does not
infer contracts from call sites. Java `public` is used where the shared kernel,
public GeoCeDG operations, Desktop laboratory and tests cross packages. It does
**not** promise third-party API compatibility. The G7B metric layer consumes
these seams without changing them; its result, route, integration and
component-state contracts are kept in the separate
[metric API reference](locus_v2_metric_api.md). Public persistence, commands,
metrics and rich intersections were added by later approved G9 phases; the R5
similarity implementation described below is also `PASS — AUTHOR APPROVED`.

## 1. Packages

| Package | Role |
|---|---|
| `org.geocedg.common.kernel.locus` | Immutable values, providers, evaluator/session, factory and diagnostics |
| `org.geocedg.common.kernel.geos` | Parallel `GeoLocusV2` kernel object |
| `org.geocedg.common.kernel.algos` | Normal-DAG algorithms that publish semantic snapshots |
| `org.geocedg.common.euclidian.draw` | Derived 2D drawable, render policy/data/cache |
| `org.geocedg.desktop.locus` | Opt-in developer laboratory; not normal product UI |

## 2. Core creation and evaluation

`LocusV2Factory` is the only approved creation seam in G6/G6R:

```java
ExplicitNumericDomainProvider2D provider =
        new ExplicitNumericDomainProvider2D(
                "example-t/v1",
                new LocusInterval2D(-2, 2, true, true),
                Orientation.INCREASING,
                false,
                1E-14);
LocusBranch2D branch = LocusV2Factory.fullDomainBranch(
        "example.sheet.main", provider, "example/v1",
        EnumSet.noneOf(BranchProperty.class));
GeoLocusV2 locus = LocusV2Factory.createAnalytic(
        LocusV2Mode.V2, construction, "example.locus", source,
        provider, Collections.singletonList(branch),
        (value, semanticBranch, t, session) ->
                new LocusPoint2D(t, value * Math.sin(t)),
        "example-sine/v1");

try (LocusEvaluationSession2D session =
        LocusEvaluationSession2D.memoizing(128)) {
    LocusEvaluation2D result = locus.evaluate(
            "example.sheet.main", 0.5, session);
}
```

Preconditions:

- creation runs on the kernel thread with a live `Construction`;
- locus identity, branch key, provider descriptor and evaluator signature are
  nonempty deterministic identifiers;
- intervals, point coordinates, source snapshots and query parameters are
  finite; provider epsilon is finite and nonnegative;
- the caller supplies provider-canonical semantics, not sample indices or
  viewport-derived ranges;
- `LocusV2Mode.LEGACY` is rejected by the factory.

Postconditions:

- the output is a normal `AlgoElement` output with a semantic revision at least
  one;
- evaluation returns a typed `LocusEvaluation2D`, never a stale coordinate;
- queries do not change the revision or construction graph.

## 3. Principal interfaces

### `LocusDriverDomainProvider2D`

Relevant methods:

- `getProviderId()` and `getParameterDescriptor()` identify the versioned
  semantic contract;
- `getDeclaredDomain()`, `getOrientation()`, `isPeriodic()` and
  `getDomainEpsilon()` declare domain policy;
- `canonicalize(double)` and `contains(double)` own semantic parameter
  normalization/membership;
- `getSemanticSignature()` participates in snapshot equality.

Implementations must be immutable and deterministic. They must not consult
`PathParameter`, screen scale, samples or render cache. Adding a provider
requires a normative mapping, endpoint/periodic tests and dependency behavior;
“GeoGebra has a Path” is not sufficient.

### `LocusEvaluator2D`

The evaluator receives the immutable definition, stable branch, canonical
parameter and scoped session. It returns `LocusEvaluation2D`. It may recursively
evaluate upstream V2 definitions using the same session. It must not mutate the
live construction, create a dependency slice per point, render, publish a
revision or hide query-history dependence.

### `LocusEvaluationSession2D`

- `reference()` disables memoization while retaining cycle/revision checks;
- `memoizing(capacity)` enables bounded exact-key reuse;
- `clear()` releases cache/revision observations when no key is active;
- `close()` disposes the session; reuse returns a typed closed-session failure;
- hits/misses/evictions/cycles and `getLastDiagnostic()` are diagnostic evidence.

The full key is identity + revision + branch key + canonical parameter. Sessions
are kernel-thread-confined and disposable. Do not retain them as global or
cross-revision caches.

### Branch and quality values

`LocusBranch2D` has one `branchKey`, declared domain, zero or more valid domain
components, orientation, provenance, typed lineage, branch properties and four
quality axes. A validity gap does not create a branch. Lineage cardinalities are
validated:

- `UNCHANGED`: no edges;
- `APPEARED`: children only;
- `DISAPPEARED`: parents only;
- `SPLIT`: one parent, at least two children;
- `MERGED`: at least two parents, one child.

Status and quality remain independent: definition status, branch properties,
evaluation status, optional regularity, lineage, construction fidelity,
evaluation method, representation role and numeric guarantee must not be merged
into one convenience enum.

## 4. Normal-DAG algorithms

| Factory method | Productive algorithm | Inputs | Current coverage |
|---|---|---|---|
| `createAnalytic` | `AlgoAnalyticLocusV2` | one `GeoNumeric` | explicit finite numeric provider |
| `createDynamicAnalytic` | `AlgoDynamicBranchLocusV2` | typed numeric list | topology/components/lineage fixtures |
| `createSegmentPathDriven` | `AlgoSegmentPathLocusV2` | `GeoSegment`, constrained `GeoPoint` | stable segment mapping only |
| `createNested` | `AlgoNestedLocusV2` | upstream `GeoLocusV2` | recursive pointwise composition |

Nested example:

```java
GeoLocusV2 l2 = LocusV2Factory.createNested(
        LocusV2Mode.V2, construction, "example.L2", l1,
        "example.sheet.main", provider, branches,
        t -> t / 2,
        (t, upstreamPoint) -> new LocusPoint2D(
                upstreamPoint.getX(), upstreamPoint.getY() + t),
        "example-L2/v1");
```

The upstream object is an ordinary algorithm input. The closure captures the
upstream immutable definition published at recompute. If the upstream revision
changes, normal DAG invalidation republishes downstream snapshots. The session
does not become a second graph.

### R5 similarity transformations

The author-approved R5 design and implementation use one shared kernel seam:

- `LocusV2PublicOperations` receives the seven approved ordinary 2D forms:
  translation; rotation about the origin or a point; reflection in a line or a
  point; and uniform dilation about the origin or a point;
- `AlgoLocusSimilarityTransform2D` is a normal-DAG parent whose inputs are the
  source `GeoLocusV2` and ordinary transformation geos, and whose output is a
  new `GeoLocusV2` with a new durable identity;
- `LocusSimilarityTransform2D` is the immutable finite affine value used by one
  recompute; its line-reflection constructor normalizes homogeneous line
  coefficients with scale-safe arithmetic;
- `LocusSimilarityEvaluator2D` evaluates the source first at the same semantic
  branch/parameter address, propagates invalidity, and transforms only a valid
  finite source point.

The transformed definition retains source domain, branches, components,
orientation, parameter and periodic policy. For finite dilation factor `k=0`,
it retains the source `FINITE` or `UNBOUNDED` domain property and adds
`COLLAPSED_IMAGE`. That property is the semantic proof used by
`EvaluatorOnlyLocusMetricCapability2D` for exact-zero length on valid collapsed
components; it never turns an invalid source gap into a valid point. A
transformed rich-intersection query uses the R4 resolver in its new source-pair
context and receives new selectors/tokens.

The ordinary command processors intercept only the approved 2D `GeoLocusV2`
overloads. Circle inversion, text/vector-at-point behavior and legacy object
routes remain host behavior; axis/plane/spatial-center 3D routes fail closed.
Construction participation and durable-ID publication are one exception-safe
transaction: an R5 transform rejected before its own publication, including a
rejected redefine candidate, removes that candidate algorithm/output and rolls
back its reserved identities. A successful nested subcommand may still remain
under the existing host rule if a later unrelated outer command fails. R5 does not implement
`Path`, sample-based transformation, a mutable-transform interface, G9U1
workspace behavior or candidate markers.

The final author fixture exercises the same live dilation parent through
slider-driven `GeoNumeric.setValue()`, explicit existing-object editing,
positive/negative/zero transitions and native save/reopen. The free-input
expression `k=0.25` is currently rejected before this R5 seam with G9A
`REDEFINE_CONTEXT_MISSING`; rejection is atomic. R5 does not broaden G9A or
infer identity from the label. Compatible free-input redefine remains a future
G9U1 investigation through the accepted G9A transaction/predicate authority.

## 5. Semantic revision

`GeoLocusV2.getSemanticRevision()` identifies the current immutable semantic
snapshot. `publishSemanticDefinition()` is for the algorithm family only and
requires monotonic increase. `AlgoLocusV2` compares semantic content before
publication. Evaluations, labels, selection, zoom and render must not increment
it. Equivalent recompute may restore an explicitly undefined object without a
new revision.

Do not use `Construction.getStep()` as a revision and do not include mutable
instrumentation in semantic equality.

## 6. Render API

`DrawLocusV2` owns one `LocusRenderCache2D`. The cache accepts a
`LocusRenderPolicy2D` and consumes only `evaluateForRender`. Available policies:

- `uniformFrom(view)` / the six-argument constructor: bounded uniform reference;
- `from(view)` / `adaptiveFrom(view)`: normal adaptive visual tessellation;
- `adaptive(...)`: explicit test/developer visual policy.

`LocusRenderData2D.Vertex.getSemanticParameter()` is provenance for testing and
subdivision only. It is not a public point-on-path, metric or export address.
Render cache counters support diagnostics; cache entries are bounded to four per
drawable and become misses when revision/view policy changes.

## 7. Lifecycle and unsupported cases

The old G6 internal-factory objects and the later public persistent objects have
different lifecycle contracts. Public G9U0 objects use durable identity, XML
reconstruction, copy/remap and normal undo/redo; raw internal objects still may
reject those operations rather than pretending to be persistable. Callers must
not catch an unsupported lifecycle operation and fabricate a sample-based
substitute.

These boundaries remain deliberate:

- `GeoLocusV2` does not implement generic `Path`, and point-on-locus incidence
  uses the explicit branch/parameter public operation;
- legacy `isGeoLocus()` / `isGeoLocusable()` classification is not semantic V2
  authority;
- the approved public metric/intersection APIs remain separate rich-result
  authorities rather than legacy `Perimeter`, ODE or list-index behavior;
- G5 export still does not infer V2 geometry from render samples;
- 3D/plane-view transform dispatch is outside R5 and fails closed for a V2
  source; it does not convert the locus into a sampled or generic path object.

## 8. Developer laboratory

From the repository root:

```powershell
.\tools\locus-v2\open-locus-v2-laboratory.ps1 -ValidateOnly
.\tools\locus-v2\open-locus-v2-laboratory.ps1
```

The second command runs `:desktop:desktop:runLocusV2Laboratory`, uses temporary
preferences and creates fixtures through `LocusV2Factory`. The diagnostic panel
shows identity/revision/provider, branches/components/lineage, quality axes,
session counters and derived render counts. It has no stable UI/API contract and
its construction cannot be saved as `.ggb`.

## 9. Extension rules

- G7 derived metrics must key/invalidate by semantic revision, consume semantic
  domain/evaluators and never sum render chords or regenerate a whole locus per
  downstream query.
- G8 intersections must refine in semantic parameters and return explicit
  residual/tolerance evidence; render samples may only seed a non-authoritative
  broad phase if separately approved.
- G9 projection correspondence may share a provider-owned semantic parameter;
  it must not infer identity from labels or samples.
- A future export adapter must consume branches/domain/evaluator/exactness, not
  `LocusRenderCache2D`. G5 continues to reject V2.
- Any new command family, generic `Path` conformance, persistence migration, 3D
  semantic transform or concurrency contract remains a stop condition requiring
  its own phase authority. The bounded R5 ordinary 2D similarity overloads do
  not authorize any of those expansions or G9U1.

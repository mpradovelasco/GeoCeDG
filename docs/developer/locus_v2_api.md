# Locus V2 internal developer API

| Field | Value |
|---|---|
| Maturity | `experimental`, internal/developer-only, disabled by default |
| Normative semantics | [`geocedg/specs/locus/locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md) |
| Architecture | [`docs/architecture/locus_v2_implementation.md`](../architecture/locus_v2_implementation.md) |
| Public command/API | None |
| Persistence / `Path` | None |
| Date | 2026-08-12 |

This reference documents the current Java seams so future kernel work does not
infer contracts from call sites. Java `public` is used where the shared kernel,
Desktop laboratory and tests cross packages. It does **not** promise third-party
API compatibility.

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

The following deliberately throw or remain absent:

- `GeoLocusV2.copy()`, `copyInternal()` and `set()`;
- XML serialization, loading, migration and undo/redo persistence;
- `Path`, public point-on-locus and incidence;
- `isGeoLocus()` / `isGeoLocusable()` classification;
- public command/dispatcher creation;
- legacy `Length`, `Perimeter`, ODE and metrics;
- G5 export and all 3D/plane-view dispatch.

Callers must not catch an unsupported lifecycle operation and fabricate a
sample-based substitute. If a future phase needs one, it requires an approved
contract.

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
- New public commands, `Path`, persistence, metrics, intersections, 3D or
  concurrency remain stop conditions requiring their phase authority.

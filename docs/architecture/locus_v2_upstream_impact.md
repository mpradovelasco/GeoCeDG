# Locus V2 — baseline implementation map and candidate impact

| Field | Value |
|---|---|
| Status | **APPROVED AS G6A WORKING ARCHITECTURAL HYPOTHESIS**; no source change authorized |
| Baseline | GeoGebra 5.4.928.0, `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Inspected tree | GeoCeDG `planning/g6-locus-v2`, G5 `PASS` |
| Date | 2026-08-11 |

This map records the real implementation behind the baseline `Locus` command
and the change surface that G6A must verify before recommending G6B. It
complements the candidate
[semantic model](locus_v2_semantic_model.md) and does not authorize any change
to `source/`.

## 1. End-to-end legacy path

```text
Locus[...] command
  -> CommandDispatcher / BasicCommandProcessorFactory
  -> CmdLocus
  -> AlgoDispatcher.locus(...)
     -> AlgoLocus / AlgoLocusSlider / AlgoLocusList
     -> AlgoLocusND / AlgoLocusSliderND sampling engine
        -> cloned dependency slice in MacroKernel
        -> PathMoverGeneric or SliderMover
        -> adaptive screen-driven traversal
     -> GeoLocus
        -> GeoLocusND.myPointList
        -> Path over sample indices
  -> EuclidianDraw -> DrawLocus
     -> GeneralPath built directly from myPointList
```

The current result is a dynamic sampled drawable with useful dependency
integration. It is not a parameterized semantic curve independent of its
samples.

## 2. Command and algorithm dispatch

| File / class | Relevant entry | Current responsibility | G6 consequence |
|---|---|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CommandDispatcher.java` | command table around `Commands.Locus` | Routes the public command | Must remain unchanged in G6B |
| `.../kernel/commands/BasicCommandProcessorFactory.java` | `new CmdLocus(kernel)` | Constructs the processor | Must remain unchanged in G6B |
| `.../kernel/commands/CmdLocus.java` | `process(Command, EvalInfo)` | Selects ODE, path-point or slider overloads | Existing `Locus` cannot silently produce V2 |
| `.../kernel/algos/AlgoDispatcher.java` | `locus(...)` overloads | Checks path/dependency/slider conditions; selects algorithms | No V2 branch in the public dispatcher until a later approved command policy |
| `.../kernel/algos/AlgoLocus.java` | concrete 2D path locus | Creates `GeoLocus` through `AlgoLocusND` | Legacy comparison target |
| `.../kernel/algos/AlgoLocusSlider.java` | concrete 2D slider locus | Creates `GeoLocus` through `AlgoLocusSliderND` | Legacy comparison target |
| `.../kernel/algos/AlgoLocusList.java` | list-path special case | Builds sub-loci, concatenates their sampled points with `MOVE_TO` | Sample groups are not semantic branches |
| `.../geogebra3D/kernel3D/algos/AlgoLocus3D.java` and `AlgoLocusSlider3D.java` | 3D variants | Reuse the legacy sampling base classes | Out of G6; V2 is explicitly 2D |

`CmdLocus` also redirects some function/existing-locus cases to
`AlgoIntegralODE`. That behavior is a separate command overload and is not a
candidate V2 implementation seam.

## 3. Legacy path-driven sampling engine

### 3.1 `AlgoLocusND`

File:
`source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLocusND.java`.

Important methods and fields in this baseline:

| Member | Responsibility / observation |
|---|---|
| `MAX_TIME_FOR_ONE_STEP = 500` | Wall-clock budget in milliseconds; sampling may stop for graphical responsiveness |
| `init(...)` | Captures driver/dependent points and prepares the cloned evaluation construction |
| `setInputOutput()` | Registers the dependency inputs and sampled locus output |
| `buildLocusMacroConstruction(...)` | Uses `Macro.addDependent...`, serializes a dependency slice with `Macro.buildMacroXML`, and loads it into a `MacroKernel` |
| `resetMacroConstruction()` | Synchronizes the cloned construction with current inputs |
| `compute()` | Traverses driver states, calls `macroCons.updateConstruction(false)`, adapts sample steps and writes the result list |
| `insertPoint(...)` and distance helpers | Work in screen-scaled coordinates and classify `LINE_TO`/`MOVE_TO` samples |
| `euclidianViewUpdate()` | Recomputes when a view changes |

The algorithm reads Euclidian `xscale`/`yscale`, uses pixel-distance criteria,
and bounds traversal by `PathMover.MAX_POINTS = 10,000` multiplied by the
number of participating views. Undefined
intervals are inferred during traversal. When `kernel.isContinuous()` is true,
the same nominal parameter may follow a history-dependent branch, so the small
sample cache is disabled. A timeout is logged and can leave a partial sampled
result suitable for display but unsuitable as a semantic definition.

The dependency-slice construction is valuable prior art: it avoids moving the
live driver for each sample. G6 may reuse it only after G6A proves deterministic
point evaluation from an explicit canonical branch state.

### 3.2 `AlgoLocusSliderND`

File:
`source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLocusSliderND.java`.

This class duplicates much of the path algorithm for a numeric driver: it
builds/resets a `MacroKernel`, updates the copied parameter and construction,
uses screen scales and a 500 ms per-step budget, and recomputes on view update.
The duplication is an argument for one V2 driver/evaluator abstraction, not for
making path and slider semantics identical.

## 4. Driver parameter behavior

| File / type | Native parameter evidence | V2 planning decision |
|---|---|---|
| `kernel/Path.java` | One `getMinParameter()` / `getMaxParameter()` pair; no branch or endpoint descriptors | Insufficient as the complete semantic domain interface |
| `kernel/PathParameter.java` | Mutable scalar `t` plus `pathType` | A provider may map it, but it is not semantic identity by default |
| `kernel/PathNormalizer.java` | Maps finite and infinite native ranges to/from `[0,1]` | Traversal adapter only; endpoints of infinite normalized ranges are not finite semantic states |
| `kernel/advanced/AlgoPathParameter.java` | Public command normalizes native `PathParameter.t` to `[0,1]` | Traversal coordinate only; never V2 identity merely because it is normalized |
| `kernel/geos/GeoLine.java` | Native interval `(-infinity,+infinity)` | Explicit unbounded branch provider required |
| `kernel/geos/GeoRay.java` | Native half-infinite interval | Explicit endpoint/unbounded policy required |
| `kernel/geos/GeoSegment.java` | Native `[0,1]` | Simple initial provider |
| `kernel/kernelND/GeoConicND.java` | Circle/ellipse use a periodic angular interval; parabola is unbounded; hyperbola/degenerate conics encode multiple traversals in one numeric range | Type-specific semantic branch provider required; min/max alone is unsafe |
| `kernel/geos/GeoCurveCartesian.java` | Construction-owned start/end parameter and closed flag | Good finite-domain candidate when deterministic |
| `kernel/geos/GeoFunction.java` | Min/max read the active views' x-range; with an explicit interval they still return its intersection with the view | Reject the current `Path` bounds as V2 domain; a type-specific provider may use a declared interval independently of the view |

`PathMoverGeneric` performs traversal over normalized coordinates and writes the
corresponding native `PathParameter`. `SliderMover` performs a similar adaptive
traversal. Their normalization and animation wrapping are operational choices,
not proof of a periodic geometric domain.

The author-approved contract places authority in a versioned Locus V2
driver-domain provider. Its semantic parameter may coincide with a native
GeoGebra parameter only when the provider explicitly declares that parameter
suitable and stable. This also preserves a future seam for two CeDG projections
to share one semantic parameter while their internal 2D `Path` objects use
different native parameterizations.

## 5. Result object and `Path` behavior

### 5.1 `GeoLocusND`

File:
`source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoLocusND.java`.

`GeoLocusND<T extends MyPoint>` extends `GeoElement` and implements `Path`,
`GeoLocusable` and `Traceable`. Its authority is the mutable
`ArrayList<T> myPointList`:

- `getPoints()` exposes the sample list;
- `getPointLength()` returns its size;
- `getMinParameter()` is `0` and `getMaxParameter()` is `size - 1`;
- `pathChanged(...)` interpolates between adjacent samples using the integer
  and fractional parts of that sample-index parameter;
- `isClosedPath()` compares the first and last sampled coordinates;
- `createPathMover()` returns `PathMoverLocus`.

This behavior is valid legacy compatibility but cannot carry V2 domain,
branches or parameter preimages.

### 5.2 `GeoLocus` and `PathMoverLocus`

`source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoLocus.java`
finds the closest sampled segment in `pointChanged(...)` and assigns the
sample-index path parameter. `source/shared/common/src/main/java/org/geogebra/common/kernel/PathMoverLocus.java`
walks the same points and `MOVE_TO` breaks. Neither preserves a source driver
parameter or a semantic branch identifier.

`GeoLocusStroke` / `AlgoLocusStroke` use related point-list infrastructure for
freehand strokes. They are not V2 migration targets and must not be disturbed.

## 6. Rendering and view coupling

| File / class | Behavior |
|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianDraw.java` | `GeoClass.LOCUS` dispatches to `DrawLocus` and casts to `GeoLocusNDInterface` |
| `.../euclidian/draw/DrawLocus.java` | `update()` builds a `GeneralPath` directly from `locus.getPoints()` |
| `.../geogebra3D/euclidian3D/draw/DrawLocus3D.java` | Builds 3D drawable geometry from the legacy point list |
| `.../geogebra3D/euclidian3D/EuclidianView3D.java` | Dispatches 3D locus types when they advertise 3D drawable support |
| `.../geogebra3D/euclidianForPlane/EuclidianViewForPlaneCompanion.java` | Treats `LOCUS` as potentially visible in a plane view; that switch performs no locus cast |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/ConstructionDefaults.java` | Maps `GeoClass.LOCUS` to the legacy locus presentation defaults |

A V2 drawable needs a separate view-local tessellation cache. G6B should not
alter 3D dispatch and its 2D V2 element should not advertise 3D drawable
support. The baseline 3D switch is guarded by `geo.hasDrawable3D()`, so the
candidate must test that its default `false` prevents the legacy 3D cast.

G6A must not assume that V2 reuses `GeoClass.LOCUS`. It must audit both a
distinct V2 classification and a legacy presentation classification while
keeping `isGeoLocus()` and `isGeoLocusable()` false unless a later public
contract approves otherwise. Baseline command, metric and `Path` dispatch must
not accept V2 in G6B. Label presentation also needs an explicit decision because
`GeoElement.isLabelShowable()` treats objects reporting `isGeoLocus()`
specially.

## 7. Current metric and incidence meaning

| Path | Current legacy meaning | G6 policy |
|---|---|---|
| `kernel/algos/AlgoLengthLocus.java` through `CmdLength` | Number of stored samples | Preserve legacy only; do not expose as V2 length |
| `kernel/algos/AlgoPerimeterLocus.java` through `CmdPerimeter` | Sum of chords between stored samples | Preserve legacy only; G7 owns V2 metric semantics |
| `GeoLocusND` as `Path` | Point membership/projection on sampled segments | Do not implement V2 `Path` behavior in G6B without the approved G7/G8 preimage/incidence contract |

The legacy macros `listLength` / publication role `locusLength`,
`listLength12` / publication role `locusLength12`, and `postLocus` in
[`Templatev7.ggb`](../../models/legacy/template-v7/original/Templatev7.ggb)
operate on samples and filtered lists. Their exact definitions are preserved in
the [derived inventory](../../models/legacy/template-v7/derived/tool-inventory.yml).
They are characterization cases, not APIs to port into the kernel.

## 8. Serialization and type dispatch

- `source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java`
  has one `LOCUS` class with XML name `locus`.
- `source/shared/common/src/main/java/org/geogebra/common/kernel/GeoFactory.java`
  maps XML element type `locus` to `GeoLocus`.
- ordinary dependent loci are reconstructed from serialized command inputs;
  the `myPointList` sample cache is not a versioned semantic definition.
- `GeoPoint` serializes its native path parameter, but `PathParameter` has no
  branch identity.

Adding a distinct V2 classification may expand factory/XML/switch/localization
surface even when persistence stays disabled; reusing `GeoClass.LOCUS` risks
legacy casts and semantic ambiguity. G6A must enumerate every relevant use of:

- `GeoClass.LOCUS` and type-switch dispatch;
- `isGeoLocus()` and `isGeoLocusable()`;
- 2D/3D drawing and plane-view acceptance;
- construction defaults, styles and labels;
- `Length`, `Perimeter`, incidence and public `Path` behavior;
- `GeoFactory`, XML names and serialization dispatch.

The author's preference is a distinct V2 type/classification if the measured
compatibility impact remains reasonably localized. The final choice is
deferred to G6A and must return for the second author review. Independently of
that choice, G6B is approved as non-persistent and internal-only: it does not
change XML, create a migration, expose a public command or implement public
`Path`. Any later user-creatable/savable capability requires a separate
accepted serialization contract and round-trip gate.

## 9. Existing tests and missing evidence

| Test | Existing assertion | Semantic gap |
|---|---|---|
| `source/shared/common-jre/src/test/java/org/geogebra/common/kernel/commands/CommandsTest.java` | Basic path/slider `Locus` commands produce a defined object | No semantic evaluation or branch check |
| `.../kernel/algos/AlgoSequenceTest.java::sequenceOfLociShouldChangeOnZoom` | Sample count changes substantially with zoom | Useful legacy characterization; opposite of V2 semantic invariance |
| `.../euclidian/draw/DrawLocusTest.java` | A locus creates drawable path data | No separation of semantic and render data |
| perimeter command tests | Expected chord result, with view-dependent point generation noted | Confirms metric dependence on samples |
| `.../kernel/geos/GeoLocusStrokeTest.java` | Stroke behavior | Out of scope; protects against accidental regression |

No baseline test establishes branch identity, valid-domain components, an
explicit provider-owned semantic parameter, evaluator determinism,
zoom-independent point evaluation, topology lineage, multi-axis quality
metadata, nested semantic composition or persistence versioning.

## 10. Legacy nested-locus evidence and graph risks

The author's experimental observation is that a legacy locus depending on
another locus was functional but inefficient, while a third
`Locus -> Locus -> Locus` level became practically intractable. This is evidence
that G6A must reproduce; it is not yet a proven causal explanation.

The baseline exposes several mechanisms that could contribute and therefore
must be instrumented separately:

- `GeoLocusND` is itself a sampled `Path`; a moving point on an upstream locus
  traverses sample-index segments through `PathMoverLocus`;
- each path-driven `AlgoLocusND` builds and synchronizes a cloned dependency
  slice in a `MacroKernel` and repeatedly calls `updateConstruction(false)`;
- downstream slices may include the upstream locus algorithm and its sampled
  result, so G6A must determine what is cloned, reset and updated at each level;
- `euclidianViewUpdate()` recomputes legacy locus sampling, so render/view
  interaction must be measured separately from semantic dependency work;
- the `CmdLocus` overload accepting an existing locus as its first argument
  routes to `AlgoIntegralODE`; it is not the same case as driving a dependent
  locus from a point whose `Path` is an upstream locus.

G6A must count dependency-slice builds, resets/synchronizations, construction
updates, `Path`/sample reads, repeated evaluator-equivalent calls and view
updates for reproducible two- and three-level artifacts before assigning a
cause.

For V2, at least these strategies must be compared:

| Strategy | Candidate mechanism | Required proof |
|---|---|---|
| A. Recursive semantic evaluator composition with one scoped shared evaluation session | Each downstream evaluator calls the typed upstream evaluator; coherent revisions and memoization travel with the request | No render/sample read, no per-point slice rebuild, bounded duplicate calls and correct DAG invalidation |
| B. Controlled flattening/compilation of compatible evaluation DAG slices | Prepare a shared evaluation plan while preserving each locus identity, branch key and revision | No second hidden DAG, no lost lineage, bounded synchronization and measurable benefit over A |

Strategy A is the minimum working preference, not an accepted implementation
decision. G6A may recommend B or another code-supported strategy only from
measurements. A possible session abstraction and its final name remain open.
Its cache identity must include locus identity, semantic revision, branch key
and provider-owned semantic parameter.

Normal `AlgoElement.setDependencies()` registers explicit input/output edges in
construction order, and the baseline provides ancestry queries plus
`CircularDefinitionException` in several reference-setting paths. G6A must
audit the exact cycle check used by the internal V2 factory. An evaluator
callback can otherwise create a cycle invisible to declared inputs; a scoped
active-evaluation stack must diagnose re-entry if standard DAG construction
checks do not cover it.

## 11. Actual GeoCeDG seams

- `geocedg/features/experimental.yml` is a validated feature catalog, not a
  shared-kernel runtime flag service.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGProfile.java`
  loads the product profile for Desktop; it cannot govern shared-kernel
  semantics by itself.
- existing GeoCeDG shared code uses the `org.geocedg.common` namespace for
  product-owned services such as G5 export.
- the G5 adapter explicitly rejects legacy `GeoLocus`; that policy remains in
  force throughout G6.

G6B must therefore implement the smallest real runtime selection seam. A test
or internal factory can receive a `LocusV2Mode` (`LEGACY`, `V2`, `DUAL`) from an
explicit GeoCeDG application/test setting. These are diagnostic modes; none
redirects the public `Locus` command in G6B. Manifest presence alone is not an
enabled runtime feature, and Classic must continue to construct legacy loci.

## 12. Alternatives evaluated

| Alternative | Compatibility | Graph integration | G7/G8 suitability | Upstream sync / complexity | Assessment |
|---|---|---|---|---|---|
| A. Extend `GeoLocus` and replace its point-list authority | High regression risk; changes `Path`, length, perimeter, draw and XML meaning | Native | Possible but legacy semantics become entangled | Few class names, very high semantic blast radius | Reject for G6B |
| B. Parallel experimental `GeoLocusV2` | Legacy stays exact; explicit opt-in | Native through a new `AlgoElement` | Strong, because V2 owns domain/evaluator | Localized new classes plus one 2D draw seam | Recommend |
| C. Introduce one reusable semantic abstraction and retrofit both V1/V2 | Attractive long term, but V1 cannot honestly provide branch/domain semantics | Native | Strong only if legacy adapter reports its limitations | Broad refactor and risk of false common denominator | Use a small V2-only abstraction now; do not retrofit V1 |
| D. External/service-only sampled curve | Avoids kernel changes | Fails dynamic object/dependency and future path/incidence requirements | Poor | Appears simple but creates a second authority | Reject |
| E. Recast as `GeoCurveCartesian` | Existing parametric curve/render support | Cannot represent an arbitrary dependency slice, multiple semantic branches or topology lineage | Incomplete | Semantic mismatch hidden behind an existing type | Reject |

The proposed ADR records **B plus a minimal form of C**: a parallel kernel
entity implements new reusable semantic interfaces, while legacy remains an
explicit comparison implementation and is not claimed to satisfy them.

## 13. Candidate G6B class and file plan

Names are exact planning candidates; G6A may refine them before author approval.

### 13.1 New shared semantic classes (`org.geocedg.common.kernel.locus`)

| Candidate | Responsibility |
|---|---|
| `LocusDefinition2D` | Immutable semantic version, local revision, driver descriptor and branches |
| `LocusBranch2D` | Deterministic branch key, declared domain, valid-domain components, orientation, provenance and lineage |
| `LocusDomainComponent` / endpoint value types | Finite/infinite/open/closed/periodic domain contract |
| `LocusDriver2D` | Common driver evaluation protocol |
| `PathLocusDriver2D`, `NumericLocusDriver2D` | Preserve distinct driver provenance and domain providers |
| `LocusEvaluator2D` | Point evaluation, domain and validity queries |
| `LocusEvaluation2D` and separated status/quality types | Immutable result, definition/evaluation states, regularity and four-axis quality metadata |
| `DependencySliceEvaluationContext2D` | Controlled, synchronized use of a cloned dependency slice |
| Scoped semantic evaluation session (name deferred) | Candidate coherent revisions, nested memoization and callback-cycle guard; include only if G6A evidence requires it |
| `LocusV2Mode` | Explicit `LEGACY`, `V2`, `DUAL` selection; no manifest-as-runtime illusion |

### 13.2 New kernel-integrated classes

| Candidate path/class | Responsibility |
|---|---|
| `org.geocedg.common.kernel.geos.GeoLocusV2` | Experimental `GeoElement` containing the immutable definition/evaluator handle; not `Path` or `GeoLocusable` in G6B |
| `org.geocedg.common.kernel.algos.AlgoLocusV2` (conceptual family) | Standard input/output dependencies, local semantic revision and snapshot publication; G6A may select separate path/numeric/nested algorithms |
| `org.geocedg.common.kernel.algos.LocusV2Factory` | Internal/test construction seam for approved drivers; not a public command |
| `org.geocedg.common.euclidian.draw.DrawLocusV2` | Derives graphical paths by evaluator calls |
| `org.geocedg.common.euclidian.draw.LocusRenderCache2D` | Per-view, revision-keyed tessellation only |
| `org.geocedg.common.kernel.locus.LocusDualRunDiagnostic` | Compares legacy sampled observations with V2 evaluations without treating V1 as authority |

### 13.3 Minimum existing-source changes anticipated

| Existing file | Candidate minimal change | Why it cannot be wholly external |
|---|---|---|
| 2D draw dispatch, exact file/route selected after the `GeoClass` audit | Route V2 to `DrawLocusV2` without a legacy `GeoLocusNDInterface` cast | A kernel element needs a native drawable while classification remains undecided |
| GeoCeDG application/test feature configuration seam, exact file selected in G6A | Pass explicit `LocusV2Mode`; Classic default remains legacy | The feature catalog is not a runtime flag service |

No change is planned for `CmdLocus`, its public `AlgoDispatcher` route,
`GeoLocus*`, public `Path`, `PathParameter`, `CmdLength`, `CmdPerimeter`, XML
serialization, toolbar, 3D view or G5 DXF code. `GeoFactory` and classification
switches are audit targets, not authorized edits. G6B must update the
upstream-modification record for any actual baseline file touched.

## 14. G6B restricted demonstrator scope

The minimal implementation should support finite, explicitly owned domains and
deterministic dependency slices first. It must prove:

- analytic Level A cases and one explicit multibranch provider;
- closed endpoint equivalence, self-intersection multiplicity, cusp and
  discontinuity statuses;
- point evaluation on an unbounded branch, with render range kept separate;
- one dependency-chain CeDG case and one discrete-topology invalidation case;
- an internal, typed three-level V2-on-V2 composition fixture, with upstream
  semantic evaluators only, coherent revisions and innermost-source
  invalidation through the normal DAG;
- V2 and legacy coexistence plus dual diagnostic output;
- render-tessellation changes across zoom with unchanged semantic evaluations.

It need not solve unsupported nondeterminism, make V2 a public `Path`, persist
V2, expose a public command, migrate `.ggb`, export it, measure it or intersect
it. `LEGACY`, `V2` and `DUAL` remain internal diagnostic modes and never
redirect `Locus[...]`. Those exclusions keep the first kernel change reviewable
and prevent G7/G8 from entering G6B.

## 15. Stop conditions carried into execution

G6A/G6B must stop for author review if evidence requires any of the following:

- replacing legacy `Locus` command behavior;
- changing `.ggb` XML or choosing a `GeoClass` strategy before the G6A audit
  and second author review;
- assigning geometric authority to render samples;
- evaluating by mutating the live construction outside normal dependency
  propagation;
- guessing branch continuity from point order or coordinate proximity;
- using view bounds as a semantic domain;
- consuming an upstream V2 locus through render samples, rebuilding a complete
  upstream locus/slice per downstream point, or hiding a callback dependency
  outside the kernel DAG;
- accepting nested scaling that is clearly superlinear in depth without an
  explained, reviewed cause;
- adding a concurrent evaluation model or large external dependency without a
  separate decision;
- implementing public length, intersections, DXF locus export or 3D projection
  semantics.

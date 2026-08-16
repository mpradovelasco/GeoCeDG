# Locus V2 public-surface architecture

- Status: author-approved G9 architecture; no productive implementation
- Normative specification: `geocedg/specs/locus/locus-v2-public-surface.md`
- Accepted decision: ADR 0013

## Architectural result

The public surface should remain a thin, typed layer over the G6-G8 semantic
engine. It needs a typed one-dimensional generator registry plus one production
evaluator/persistence foundation; it must not replicate metric or intersection
solving in command processors or tools.

```text
command/tool selection
  -> runtime creation gate
  -> typed command processor
  -> typed generator normalization G:D->S
  -> reconstructible kernel parent algorithm in the normal DAG
  -> existing Locus V2 definition / metric / intersection services
  -> rich Geo result in the normal dependency graph
  -> read-only inspector
  -> exact-token or guarded-scalar child algorithm
```

## Current internal seams and required evolution

| Current source/symbol | Current role | Public evolution required |
|---|---|---|
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2Factory.java:24-110` | internal/test analytic, dynamic, nested and segment creation | retain test helpers; add production evaluator factory from reconstructible construction inputs |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDriverDomainProvider2D.java`, `ExplicitNumericDomainProvider2D.java`, `StablePathDomainProvider2D.java` | versioned finite/periodic domains and stable segment/circle mappings | generalize behind a closed typed generator-provider registry; do not infer generic `Path` semantics |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSegmentPathLocusV2.java:31-51` | live segment pilot with injected `LocusPathPointFunction2D` | replace opaque function injection at public boundary with dependent-point/evaluator inputs |
| `source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoNumeric.java:406-408` | `isSlider()` couples independence to Euclidian visibility | never use slider presentation as generator identity; require explicit scalar driver/domain/mapping |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusV2.java:16-64` | normal DAG output and revision publication; `Algos.Expression` identity | production parent returns real command/algo identity and serializable inputs |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoNestedLocusV2.java` and `LocusEvaluationSession2D.java` | acyclic semantic nesting, revision-aware memoization and active-key reentry defense | retain one scoped recursive session; expose point-on-Locus nesting through ordinary parent inputs, not a second graph |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java:166-178,225-228` | internal noncopyable/nonpersistent geo | approved durable ID, copy/set/delete/undo and XML contract |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java:38-104` | rich total/partial metric algorithm | invoke from public rich metric processor; do not flatten status |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricScalarAdapter.java:18-50` | guarded numeric child | expose total `Length[GeoLocusV2]` only through the approved admissibility guard |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusSemanticPosition2D.java:11-64` | locus/branch/provider/canonical-parameter address | extend or compose with periodic lift/seam and durable branch/component-lineage evidence |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricPositionBinder2D.java` and `MetricPositionBinding2D.java` | revision/provider/branch/component validation, explicit stale state | reuse as revision-binding seam, but add explicit topology continuation; never coordinate repair |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java:44-220` | rich V2-target algorithm | public `Intersect` branch; durable token provider required |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java:41-207` | rich V2×V2 algorithm | public `Intersect` branch with canonical source-pair order |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java:49-74,98-164` | atomic rich snapshot and token admissibility, no XML | persist query/policy/identity; recompute snapshot on load |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java:17-77` | exact-token ordinary point, no solving/retargeting | public child with real command identity and token persistence |
| `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTargets2D.java:41-185` | closed target adapters | remain the only public target-family admission boundary |

## Creation path

```text
conceptual LocusV2[Q,G]
 -> processor validates profile/feature and normalizes a typed G:D->S
 -> scalar form validates state t, true coordinate s, explicit D and mapping,
    or point form validates P plus its one typed semantic support
 -> processor validates Q -> state and mapping -> true-driver dependencies
 -> LocusConstructionEvaluatorFactory captures the approved dependency slice
 -> AlgoDependentPointLocusV2 inputs:
      Q, state, true driver/support/provider inputs, all slice inputs,
      domain/orientation/periodic/topology policy, versions, durable IDs
 -> evaluator varies only the canonical true coordinate in a deterministic
    kernel-managed evaluation transaction
 -> LocusDefinition2D with branches/components/invalid intervals
 -> GeoLocusV2 publishes one coherent semantic revision
```

The processor performs no sampling or geometric solving. The parent algorithm
must declare every input through `setInputOutput()` and normal dependencies.
Evaluation cannot depend on the viewport or mutate persistent unrelated state.
The transaction restores driver/construction state even on undefined evaluation
or work-limit failure and is reconstructed by durable input identity, never
legacy label lookup.

### Typed generator layer

The internal generator descriptor is immutable and reconstructible. It records
durable generator/parent identity, provider kind/version, exactly one true
coordinate, domain/components/orientation/periodicity/seam policy, typed state
mapping, all external inputs, branch/component policy, semantic/topology
revision and typed status. The initial closed families are:

- scalar state: identity map for a bounded/free numeric with explicit domain, or
  a deterministic algebraic map `t=m(s;alpha)` from one explicit true coordinate
  `s`; and
- semantic-support point: point on finite segment, circle, circular arc or
  supported Locus V2 branch/component.

The evaluator never writes a dependent `t`. It changes only the isolated copy
of `s`, then normal dependency propagation computes `t` and `Q`. Other
free ancestors are registered external parameters. The generator address stays
in the `s` domain, so non-injective state maps retain distinct preimages.
Ambiguous true-driver inference, random/history-dependent evaluation and an
unreconstructible slice are typed unsupported states.

Segment uses `[0,1]` under ordered endpoint identities. Circle uses an
oriented angular fundamental domain and explicit seam. A circular-arc provider
records the conic/support, start, extent and positive-orientation inputs and maps
a canonical local coordinate without declaring generic `PathParameter`
universal. A Locus-support provider records source identity/provider/branch and
an explicit component policy and evaluates the immutable source definition in
the same session. New providers register by semantic type/version, not label,
Java class-name string, visual class, coordinate or fitted signature.

A bare support is rejected because it does not identify the constrained point
on which `Q` depends. Legacy `GeoLocus`, unrestricted curves, generic paths
and render/sample polylines remain outside the initial provider registry.

## Identity and revision services

G9U0 needs a shared durable document-object identity service before publication.
It is separate from:

- display label;
- document/archive UUID;
- XML order or construction index;
- coordinates;
- semantic revision;
- branch/component key;
- root token.

The locus and generator parent each have durable identity under the shared
identity policy. Semantic revisions identify snapshots. A public point on V2
must not collapse two different layers:

| Layer | Durable/current contents | Use |
|---|---|---|
| semantic preimage address | support/generator ID, provider version, branch lineage, canonical coordinate, periodic lift/wrap and seam side/direction | identity across compatible revisions, copy and reopen |
| revision binding | source semantic revision, resolved revision-scoped component, continuation state and evaluated point | currentness/admissibility for this snapshot |

Branch/component keys that are derived from one metric index are revision
evidence, not durable identity. Intersection source-pair IDs derive from durable
source identity and constructive lineage. Root tokens carry durable continuation
lineage plus revision evidence.

Copying as a new user object allocates a new durable locus ID and rewrites
internal generator/point/query/token bindings as specified by the shared copy
context; referenced external supports follow the declared copy policy.
Undo/redo of the same operation restores its identity. Deleting a source
invalidates result/point/outer-locus children before releasing caches.

## Point-on-Locus, continuation and cycle boundary

```text
source GeoLocusV2
  -> semantic-position point parent
  -> dependent construction slice
  -> outer GeoLocusV2
```

Every arrow is a normal `AlgoElement`/Construction dependency. The point
parent resolves a durable preimage through the source semantic evaluator; the
outer evaluator recursively enters the same scoped `LocusEvaluationSession2D`.
No render data and no secondary “generator graph” participates.

Continuation is established only through provider/branch/component lineage.
Unambiguous continuation rebinds the same address to the new source revision.
Branch/component loss, split/merge ambiguity, provider incompatibility or an
unresolvable periodic seam makes the point undefined/noncurrent and forces the
outer locus to publish truthful invalidation. Cartesian-nearest repair is never
attempted.

Normal graph construction/redefine/load checks reject direct and indirect
cycles such as `L1 -> P1 -> L2 -> ... -> L1`. The evaluation session's
active-key reentry result remains defense in depth for a corrupt internal
callback, not graph authority. Acyclic nesting of depth greater than one remains
supported, revision-coherent and bounded by the existing work policy.

## Metric path

```text
authoritative LocusLength[L] or LocusLength[L,A,B]
 -> feature/type/semantic-position validation
 -> TotalLocusMetricQuery or BetweenPositionsMetricQuery
 -> AlgoLocusMetricV2
 -> GeoLocusMetricResult
 -> Properties/result inspector
 -> optional standard Length[GeoLocusV2] child/reuse
 -> AlgoLocusMetricScalarAdapter publishes only when admissible
```

`A` and `B` are ordinary points whose parent algorithms retain exact
`LocusSemanticPosition2D`. The query never obtains positions from screen or
nearest-sample search. The inspector reads the immutable rich snapshot; it does
not calculate length.

On persistence, serialize the parent query, policies and position bindings.
Rebuild the metric index and rich result against the current semantic revision
on load. Never serialize render/cache data as authority.

The standard total `Length[GeoLocusV2]` surface must coexist with the rich
command, but it never owns or repeats metric calculation. It
constructs/reuses the rich query and delegates publication exclusively to
`isScalarAdmissible()`. Legacy `Length[GeoLocus]` dispatch remains unchanged.

## Intersection path

```text
Intersect[L,T]
 -> CmdIntersect detects an approved V2 operand before legacy type switch
 -> LocusIntersectionTargets2D captures a supported target, or
    AlgoLocusLocusIntersectionV2 captures canonical V2 pair
 -> rich algorithm publishes GeoLocusIntersectionResult
 -> result inspector exposes status/completeness/solutions/overlap/work

user chooses one admissible marker/token
 -> Intersect[R,"token"]
 -> AlgoLocusIntersectionPointV2-equivalent child
 -> ordinary GeoPoint, exact token dependency
```

For every non-V2 input pair, `CmdIntersect` and the general tool delegate to
existing behavior unchanged. General Euclidian mode `5` expands typed selection
only; it does not run a second solver. The product controller can extend
`EuclidianControllerFor3DD` through `App3D.newEuclidianController()`
(`source/desktop/desktop/src/main/java/org/geogebra/desktop/geogebra3D/App3D.java:127-130`)
and call inherited selection for all baseline families.

Candidate overlays are a render of immutable result data. Their screen position
can rank a click but the chosen action passes an exact token. The point child
queries `findPointAdmissibleSolution(token)` on every update. Missing,
duplicated, stale, ambiguous or unisolated tokens produce an undefined point.

## Rich-result inspector

The inspector is a read-only projection of the current result object. It needs
typed presenters for:

- source IDs and semantic/update revisions;
- computation/currentness/support;
- completeness and method;
- finite solutions, branch/component/preimage and contact evidence;
- isolation/multiplicity/continuation status;
- overlap evidence;
- metric value/coverage/rectifiability/traversal/error;
- work counts/limits and diagnostic codes.

Actions are enabled from the result's API (`isPointAdmissible`,
`isScalarAdmissible`), not reimplemented GUI predicates. A copied text report is
presentation output and never a serializable replacement for the rich geo.

## Command and localization layer

Conceptual new entries are `LocusV2` and `LocusLength`; typed creation may
normalize a typed generator form or use explicit point/scalar overloads,
including state/true-driver/domain arguments. G9P deliberately freezes no
mapped-scalar spelling: G9U0 must inspect actual GeoGebra overload conventions,
compare alternatives, preserve the normative generator semantics, and present
its final public surface for author review. Other conceptual overloads are a dedicated
V2 `Point[L,branch,t]`, `Intersect[L,T]`,
`Intersect[R,"token"]`, and guarded `Length[GeoLocusV2]`. The remaining exact
names and argument spellings are G9U0 API/serialization decisions.

Each accepted command needs:

- one `Commands` identifier and localized syntax/help;
- processor factory/dispatcher integration;
- explicit argument/type/domain errors;
- algorithm `getClassName()` suitable for reconstruction;
- XML reader/writer and round-trip tests;
- command-filter/runtime-feature handling that distinguishes interactive
  creation from existing-file load.

Do not add hard-coded Algebra type/status strings. GeoCeDG resource bundles and
the workspace action catalog reference the same command/help keys.

## Runtime capability policy

```text
FeatureManifestLoader
 -> RuntimeFeatureService
    -> command creation gate
    -> toolbar/menu action availability
    -> diagnostic dual-run availability
    -> existing-file preservation policy (separate decision)
```

The current `cedg.locus.v2` flag remains default-off. The command processor must
not rely solely on absence from a toolbar. The workspace must not make the
command available merely by becoming active.

## Persistence model

Suggested XML concepts, not a final XML grammar:

```text
locus-v2 parent:
  durable object ID
  semantic/evaluator version
  dependent point and typed generator/parent references
  true scalar coordinate + state mapping, or typed point-support inputs
  dependency-slice references
  provider/domain/orientation/periodicity/topology/policy

semantic position point:
  durable address:
    parent locus ID + provider version + branch lineage
    canonical parameter + periodic lift/wrap + seam side/direction
  revision binding:
    source semantic revision + resolved component + continuation state

metric result:
  source/query/positions/policy

intersection result:
  canonical source IDs
  target adapter/domain/policy
  constructive lineage

token point:
  rich result reference
  durable token/lineage
```

Computed revisions, coordinates, candidate order, screen markers, render samples
and cached numeric snapshots are not persistence authority. Load constructs the
parents first, validates the ordinary DAG, recomputes, establishes continuation,
and only then lets guarded children publish. Copy rewrites durable internal
references through the shared copy context; undo/redo restores the same
operation identity rather than minting a new one.

## Classic compatibility

The semantic classes and XML factories live in shared code so the GeoCeDG
Classic diagnostic launcher/path preserves native V2/rich types, semantic IDs,
tokens and bindings through load/recompute/save/reopen with the same kernel.
Runtime policy suppresses creation UI and interactive commands. Unsupported
semantic versions remain explicit unsupported objects. Old legacy loci are
never auto-converted and new V2/rich objects are never downgraded.

An external upstream product that does not contain these classes is outside the
compatibility guarantee. The GeoCeDG save path must warn before interchange,
and G9A3/U0 must characterize unsupported external-open behavior without a
silent coordinate/list or legacy-locus conversion.

## Failure containment

- Evaluator reconstruction fails: locus undefined/unsupported; no sampled
  fallback.
- Scalar true driver/domain/mapping is ambiguous or nonreconstructible: reject
  creation/load with a typed generator diagnostic; never mutate a dependent
  numeric or guess an ancestor.
- Typed support/provider is absent, collapsed or unknown: point/locus undefined;
  no generic-Path fallback.
- Dependency slice incomplete: reject creation or load as unsupported; never
  recompute outside the graph.
- Normal graph cycle: reject creation, redefine or load before evaluation; the
  session reentry guard remains defense in depth only.
- Unknown provider/version: preserve identity/raw metadata where possible and
  expose unsupported status.
- Point-on-Locus continuation absent/ambiguous: point and dependent outer locus
  become undefined/noncurrent; never choose a nearest Cartesian point.
- Metric incomplete/unrectifiable/uncertified: rich result remains inspectable;
  scalar undefined.
- Intersection incomplete: known locally admissible tokens retain explicit
  incomplete provenance.
- Token cannot continue: point undefined; never choose nearest.
- Work limit: publish typed rich status and deterministic work evidence; do not
  return the current render approximation.
- Feature off: reject interactive creation with localized reason; existing-file
  policy remains independently applied.

## Dependency order

1. durable general identity/lifecycle/XML foundation;
2. reconstructible typed generator descriptor/evaluator, scalar mapping and
   segment/circle/arc/Locus-support providers;
3. semantic-position point with durable preimage, continuation and normal-DAG
   nesting/cycle tests;
4. public rich metric and intersection commands;
5. inspector and exact-token/guarded-scalar children;
6. GeoCeDG tool/controller integration and Classic policy;
7. Construction workspace placement;
8. stable promotion only after the full 22-case generator suite and remaining
   validation matrix.

This order prevents a GUI or exporter from becoming the first owner of durable
Locus identity.

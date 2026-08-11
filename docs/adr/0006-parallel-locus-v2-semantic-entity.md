# ADR 0006: parallel experimental Locus V2 semantic entity

- Status: **Accepted**
- Author review disposition: **ACCEPTED AT G6A CLOSEOUT**
- G6A disposition: **PASS — AUTHOR APPROVED**
- Date: 2026-08-11
- Scope: normative architectural boundary for G6B and forward compatibility

The author's second review accepts this ADR and closes G6A. It does not start
or otherwise authorize execution of G6B; G6B remains `NOT STARTED` until a
separate implementation task is authorized.

## Context

The baseline `Locus` command produces a `GeoLocus` whose authoritative data is
`GeoLocusND.myPointList`. Its path parameter is the sample index plus an
interpolation fraction. Sampling density, traversal and timeout are coupled to
the Euclidian view. Existing length/perimeter and point-on-path behavior consume
that list.

CeDG requires a different semantic contract: a dynamic two-dimensional locus
with a versioned driver-domain provider, stable semantic branches, explicit
valid-domain components and deterministic point evaluation independent of zoom
and rendering. G7 must later build metric semantics on that contract; G8 must
preserve branch/parameter identity during intersections; G9 may use a shared
semantic parameter to correspond projections even when their internal 2D paths
are parameterized differently. Retrofitting the new meaning into `GeoLocus` in
one step would silently change legacy files and commands.

The baseline also lacks a shared-kernel runtime feature-flag service and a
versioned locus XML type. G6B is intended to prove the semantic entity, not to
commit prematurely to public command or persistence compatibility.

The author also reports experimental legacy evidence: two nested loci were
functional but slow, and three nested levels became practically intractable.
G6A must reproduce and explain that behavior. It must not assume a cause before
instrumenting sample-path traversal, dependency-slice build/reset/update and
render interaction.

## Decision

1. Keep a parallel experimental `GeoLocusV2`,
   leaving legacy `GeoLocus` intact. `AlgoLocusV2` denotes a conceptual
   algorithm family; G6A may recommend separate algorithms by driver.
2. Put reusable, read-only semantic contracts in a GeoCeDG-owned shared package:
   versioned driver/domain providers, `LocusDefinition2D`, deterministic branch
   keys, declared domains and valid-domain components, `LocusEvaluator2D`,
   immutable evaluation results and separated state/quality metadata.
3. Make the provider-owned semantic parameter authoritative. It may coincide
   with a GeoGebra native parameter only when the provider explicitly declares
   that parameter suitable and stable. Normalized `PathParameter[...]` in
   `[0,1]` is not semantic identity.
4. Do not make legacy `GeoLocus` implement those interfaces. A diagnostic
   adapter may observe V1 samples for comparison but must identify them as
   sampled legacy evidence.
5. Reuse any baseline cloned dependency-slice technique only through a
   controlled evaluation context proven deterministic for the G6B subset. The
   normal `AlgoElement` inputs/outputs remain the dependency authority.
6. Classify evaluator behavior as `POINTWISE_DETERMINISTIC`,
   `CANONICAL_CONTINUATION_DETERMINISTIC` or
   `UNSUPPORTED_NONDETERMINISM`. Canonical continuation requires a declared
   anchor, orientation and reproducible continuation rule independent of the
   caller's query history.
7. Separate definition status, branch/domain properties, evaluation status,
   optional regularity and topology lineage. Separate construction fidelity,
   evaluation method, representation role and numeric guarantee; an analytic
   formula evaluated with `double` is not exact arithmetic.
8. Derive rendering through a separate `DrawLocusV2` and per-view
   `LocusRenderCache2D`. No render sample is exposed as the V2 definition,
   metric or intersection authority.
9. Treat nested V2-on-V2 semantic composition as first-class. A downstream V2
   locus consumes only the upstream branch/domain information, semantic
   evaluator, revision, validity and quality metadata. It must never consume
   render vertices/sample polylines or regenerate a whole upstream locus for a
   downstream point.
10. Use recursive semantic evaluators with a scoped shared evaluation session
    as the minimum nested-composition strategy. Its cache key contains locus
    identity, semantic revision, branch identity and native semantic parameter;
    memoization is bounded and active keys detect callback cycles. Controlled
    DAG flattening/compilation remains deferred until profiling demonstrates a
    need. No hidden dependency graph or callback cycle is permitted.
11. In G6B, keep V2 non-persistent and without a public command. Construct it
   through an internal/test factory and an explicit experimental mode. Any
   public creation or `.ggb` persistence requires a later accepted
   serialization decision and round-trip tests.
12. Provide explicit `LEGACY`, `V2` and `DUAL` diagnostic modes. They do not
    redirect the public `Locus[...]` command. Classic remains `LEGACY`; no
    existing `.ggb` silently changes meaning and no migration is introduced.
13. Do not implement public `Path`/incidence, public length, intersections, DXF locus
   export or 3D projection semantics in G6B.
14. Append a distinct V2 `GeoClass`/classification while preserving every
    existing ordinal. Do not reuse `GeoClass.LOCUS`; during G6B, V2 must not
    claim `isGeoLocus()` or `isGeoLocusable()` and remains outside legacy
    `Path`, metrics, commands, XML and 3D dispatch contracts.
15. Future derived semantic services used by downstream constructions must be
    revision-scoped and compositional. In particular, G7 metrics must consume
    V2 semantic data rather than render samples, must not sum sampled chords or
    regenerate the complete metric for every downstream query when the semantic
    revision is unchanged, and must use normal-DAG invalidation and caching.

This is alternative B plus a deliberately small part of alternative C: the new
abstraction is reusable by V2 and its future consumers, but V1 is not forced
through a false semantic interface.

## Consequences

- Legacy construction loading and Classic behavior remain isolated from the
  experimental object.
- G6B introduces real kernel integration where dynamic dependency semantics
  require it, while most implementation is GeoCeDG-owned and additive.
- G7/G8 can consume branches and evaluator results without reading a drawable
  polyline.
- A V2 locus can be an explicit semantic dependency of another V2 locus. G6B
  must demonstrate at least three levels with bounded work as a PASS condition.
- The initial V2 cannot be created through the standard input bar or saved to a
  `.ggb`; this is an intentional demonstrator boundary, not a hidden omission.
- A minimal shared Euclidian draw-dispatch change and one explicit runtime mode
  seam are anticipated. The appended V2 classification keeps legacy dispatch
  opt-in. Any actual G6B source change must be recorded in the upstream
  modification record.
- Driver types whose domain is view-dependent, or whose evaluation has neither
  pointwise nor canonical-continuation determinism, remain unsupported with
  diagnostics.
- Nested memoization may reduce duplicate requests, but cache-enabled and
  cache-disabled results must be identical. Clearly superlinear scaling with
  nesting depth blocks the mechanism pending author review.
- A later persistence design may select a new XML/type contract; this ADR does
  not reserve one by overloading the existing `locus` XML meaning.

## Alternatives

### A. Extend `GeoLocus` in place

Rejected in the proposal because it changes the authority behind existing
`Path`, length, perimeter, rendering and serialized commands. A feature flag
would not by itself remove the compatibility risk of one object carrying two
incompatible parameterizations.

### B. Parallel experimental `GeoLocusV2`

Selected. It provides native graph identity and isolates compatibility while
the new entity remains experimental.

### C. Introduce one abstraction implemented by both V1 and V2

Partially selected only for V2-facing reusable interfaces. Requiring V1 to
implement explicit domain/branch semantics would either misrepresent its
sample list or trigger a broad legacy rewrite.

### D. External service without a kernel GeoElement

Rejected because the locus could not participate correctly in construction
dependencies, dynamic invalidation or future point/incidence behavior. It
would create a second geometric authority outside the kernel.

### E. Represent every V2 as `GeoCurveCartesian`

Rejected because an arbitrary dependent construction is not necessarily one
Cartesian formula; it may be multibranch, topology-changing and evaluated by a
dependency slice.

### Nested evaluation strategy A: recursive semantic evaluators plus session

Selected minimum. It preserves explicit locus boundaries and normal DAG inputs
while a bounded scoped session shares revision context, memoization and active-
key cycle detection.

### Nested evaluation strategy B: controlled DAG flattening/compilation

Retained as an alternative when profiling proves that recursive slice
synchronization remains a bottleneck. It must not create a second authority,
erase branch/revision identity or broaden upstream impact without demonstrated
benefit.

## Accepted closeout decisions

The second author review accepts:

1. the normative branch-key, valid-component, typed-lineage, determinism and
   multiaxial exactness model in the G6 semantic contract;
2. recursive semantic evaluators plus a scoped shared evaluation session as the
   minimum nested strategy, with a full semantic key, bounded memoization and
   active-key cycle protection;
3. a distinct appended V2 classification, leaving all legacy locus contracts
   untouched during G6B;
4. the G6B numeric comparison envelope
   `max(1e-12 * max(1,S), 64 * ulp(max(1,S)))` solely for uncertified numeric
   comparison. `S` is a documented, case-specific characteristic geometric
   scale and may not depend on zoom, DPI, viewport or absolute distance from
   the origin. Domain, render, G7 metric and G8 intersection tolerances remain
   separate; and
5. the two supplied cone-cylinder models as complementary scientific and
   operational evidence. Their originals, hashes, manifests and inventories
   are preserved, while public redistribution remains blocked pending rights
   and asset review.

Absolute timing budgets remain informational until a G6B execution task adopts
budgets from the G6A measurements. The accepted functional gates prohibit
render dependence, per-query dependency-slice rebuilding and work that
multiplies with nested render densities.

## Acceptance evidence

- approved mathematical/branch/degeneration/exactness contract;
- completed legacy characterization, pointwise/canonical-continuation
  experiments and formal topology fixture;
- reproduced legacy two-/three-level nested behavior with causal instrumentation;
- selected nested evaluation strategy and benchmark evidence that work does not
  multiply with render densities or nesting depth;
- exact impact review against the pinned baseline;
- approved validation matrix and measured tolerances;
- measured legacy benchmark baseline and approved G6B budgets;
- proof that the proposed non-persistent element can render without changing
  legacy `GeoLocus` dispatch;
- G6B acceptance criteria that require a typed three-level nested semantic
  demonstrator, correct invalidation and no upstream render dependency;
- second author sign-off recorded in this ADR.

Those conditions were met by the G6A evidence package and accepted by the
author on 2026-08-11. Acceptance defines the architecture; it does not start
G6B implementation.

## G6A closeout evidence and recommendation

G6A executed the hypothesis without adding productive V2 code. The durable
evidence is the normative semantic contract, formal topology/tolerance fixtures,
characterization baseline, upstream audit and G6A report linked from the G6
plan.

The measured/tested results support the accepted decisions:

1. retain the parallel experimental entity and leave legacy `GeoLocus`
   unchanged;
2. use provider-owned semantic parameters, branch keys distinct from valid
   components, typed lineage and the separated status/quality axes already
   accepted above;
3. start G6B with pointwise deterministic explicit-numeric and narrowly
   approved stable-path providers; canonical continuation requires a separate
   characterized provider rule;
4. use recursive semantic evaluator composition plus a scoped shared session
   as the minimum nested strategy. The controlled fixture scaled exactly as
   outer queries times depth, and memoization removed repeated exact keys
   without changing results. Defer controlled DAG flattening until profiling
   demonstrates need;
5. protect callbacks with an active semantic-key stack in addition to normal
   declared DAG inputs;
6. prefer a distinct V2 `GeoClass`, appended to preserve current ordinals,
   because reusing `LOCUS` triggers legacy casts, defaults, `Path`, metrics,
   command and 3D contracts; and
7. keep absolute timing budgets informational. Make deterministic evaluator
   counts, no per-query slice build, no render dependency and cache equality the
   initial functional performance gates.

The controlled `Point(Locus)` fixture confirms sampled-`Path` traversal but did
not reproduce the slowdown. The subsequently supplied hash-pinned
cone-cylinder pair did. `InterCilConoObliqueTwoLevels.ggb` is the functional
two-level control (approximately 125–127 ms). In
`InterCilConoOblique.ggb`, the state before `Flatten` was approximately
31.9 ms; creating its three third-level loci took approximately 6.03, 5.95 and
5.67 s, each became undefined after exceeding the legacy 500 ms per-step guard,
and the post-attempt recomputation took approximately 21.0 s.

For this measured model, each outer `AlgoLocusSliderND` sampling/evaluation
updates a dependency slice containing two inner loci and two
`AlgoPerimeterLocus` instances. Upstream geometric and metric work is
regenerated rather than consumed through composable semantic evaluators, and
the model reaches the legacy time guard. This is a bounded observation about
the instrumented fixtures, not a universal causal claim about every legacy
locus.

The original models remain manual/scientific legacy references. Because G6B
has no public command or persistence, its required pilot is a small internal,
typed, three-level reproduction traced explicitly to those originals. It must
prove semantic composition, inner-level invalidation, no render/sample
dependency, no whole-upstream-locus regeneration and approved functional
scaling; it does not implement G7 `Perimeter` semantics.

**G6A = PASS — AUTHOR APPROVED. ADR 0006 = ACCEPTED. G6B = NOT STARTED.**

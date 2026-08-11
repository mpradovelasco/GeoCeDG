# ADR 0006: parallel experimental Locus V2 semantic entity

- Status: **Proposed**
- Author review disposition: **APPROVED AS G6A WORKING ARCHITECTURAL HYPOTHESIS**
- Date: 2026-08-11
- Scope: authorizes G6A characterization only; candidate G6B kernel boundary

The current approval does not accept this ADR and does not authorize G6B. The
ADR remains `Proposed` until G6A closeout and a second author review.

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

## Proposed decision

1. Keep a parallel experimental `GeoLocusV2` as the G6A working hypothesis,
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
5. Reuse the baseline cloned dependency-slice technique only through a
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
10. Require G6A to compare recursive semantic evaluators with a scoped shared
    evaluation session against controlled DAG flattening/compilation. Session
    naming and final strategy remain open. No hidden dependency graph or
    callback cycle is permitted.
11. In G6B, keep V2 non-persistent and without a public command. Construct it
   through an internal/test factory and an explicit experimental mode. Any
   public creation or `.ggb` persistence requires a later accepted
   serialization decision and round-trip tests.
12. Provide explicit `LEGACY`, `V2` and `DUAL` diagnostic modes. They do not
    redirect the public `Locus[...]` command. Classic remains `LEGACY`; no
    existing `.ggb` silently changes meaning and no migration is introduced.
13. Do not implement public `Path`/incidence, public length, intersections, DXF locus
   export or 3D projection semantics in G6B.
14. Defer the choice between `GeoClass.LOCUS` and a distinct V2 classification
    to the G6A switch/contract audit. Prefer a distinct V2 type when its measured
    compatibility impact remains reasonably localized.

This is alternative B plus a deliberately small part of alternative C: the new
abstraction is reusable by V2 and its future consumers, but V1 is not forced
through a false semantic interface.

## Consequences if accepted

- Legacy construction loading and Classic behavior remain isolated from the
  experimental object.
- G6B introduces real kernel integration where dynamic dependency semantics
  require it, while most implementation is GeoCeDG-owned and additive.
- G7/G8 can consume branches and evaluator results without reading a drawable
  polyline.
- A V2 locus can be an explicit semantic dependency of another V2 locus. G6B
  must demonstrate at least three levels with bounded work before final ADR
  acceptance.
- The initial V2 cannot be created through the standard input bar or saved to a
  `.ggb`; this is an intentional demonstrator boundary, not a hidden omission.
- A minimal shared Euclidian draw-dispatch change and one explicit runtime mode
  seam are anticipated, but their exact form depends on the G6A `GeoClass`
  audit. Actual changes require the second author review and must be added to
  the upstream modification record.
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

Preferred working hypothesis. It provides native graph identity and isolates
compatibility while the semantic contract is still experimental. Final
selection remains subject to G6A evidence and the second author review.

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

Working minimum for G6A comparison. It preserves explicit locus boundaries and
normal DAG inputs while a bounded session can share revision context,
memoization and cycle detection. It is not accepted until measured.

### Nested evaluation strategy B: controlled DAG flattening/compilation

Retained as an alternative when profiling proves that recursive slice
synchronization remains a bottleneck. It must not create a second authority,
erase branch/revision identity or broaden upstream impact without demonstrated
benefit.

## Decisions deferred to G6A and second author review

The author has approved non-persistence, no public command, no public V2
`Path`, no `.ggb` migration, unchanged public `Locus[...]`, diagnostic-only
`LEGACY`/`V2`/`DUAL`, and the parallel entity as the G6A working hypothesis.
Before this ADR can become `Accepted`, G6A must provide and the author must
review:

1. final typed branch-key, valid-component and split/merge lineage rules;
2. provider-specific pointwise/canonical-continuation classifications;
3. the dependency-slice and nested evaluation-session/DAG strategy, including
   cycle protection and measured scaling;
4. the `GeoClass`/drawing/defaults/labels/metric/Path/2D–3D dispatch decision;
5. the approved driver providers and real CeDG pilots for G6B;
6. the runtime owner of diagnostic modes without changing Classic;
7. numeric tolerances and performance budgets, which are **DEFERRED TO G6A
   MEASUREMENTS**.

## Validation required before acceptance

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
- second author sign-off recorded in this ADR or a superseding decision.

Until those conditions are met this ADR remains **Proposed**. The current
disposition authorizes G6A characterization only; it authorizes no G6B
implementation.

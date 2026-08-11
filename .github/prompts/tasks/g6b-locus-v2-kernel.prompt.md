# Objective

Execute G6B: implement the minimum experimental two-dimensional Locus V2
kernel entity that demonstrates the author-approved G6A semantic contract.

This prompt is the single canonical G6B execution prompt. Do not reconstruct
requirements from prior conversations and do not create a parallel prompt.

# Preconditions and authorization gate

Before editing:

1. verify the current branch and its ancestry against the author-designated G6B
   branch;
2. require a clean or explicitly understood worktree;
3. verify that G6A is committed as `PASS — AUTHOR APPROVED`;
4. verify that `geocedg/specs/locus/locus-v2-semantics.md` is
   `APPROVED AS NORMATIVE G6 SEMANTIC CONTRACT`;
5. verify that ADR 0006 is `Accepted`;
6. verify the approved G6A validation records, model hashes and baseline SHA;
   and
7. require a separate explicit author instruction to execute G6B.

If any precondition is absent, contradictory or uncommitted, do not implement.
Report exactly:

```text
G6B = BLOCKED PENDING AUTHOR REVIEW
```

The repository state captured when this prompt was hardened is G6A
`PASS — AUTHOR APPROVED`, ADR 0006 `Accepted`, the semantic contract normative,
and G6B `NOT STARTED`. The prompt's existence is not implementation
authorization.

# Authority and evidence hierarchy

Read and apply, in this order:

1. `AGENTS.md`;
2. `docs/roadmap/geocedg_roadmap.md`;
3. `docs/roadmap/g6_locus_v2_plan.md`;
4. `geocedg/specs/locus/locus-v2-semantics.md` — normative;
5. `docs/adr/0006-parallel-locus-v2-semantic-entity.md` — Accepted;
6. `docs/validation/g6a_locus_v2_characterization_report.md`;
7. `docs/architecture/locus_v2_semantic_model.md`;
8. `docs/architecture/locus_v2_upstream_impact.md`;
9. `docs/validation/g6_locus_v2_validation_matrix.md`;
10. `docs/validation/g6_locus_v2_benchmark_plan.md`;
11. versioned evidence under `geocedg/validation/locus-v2/`;
12. the manifests, inventories and immutable originals for
    `InterCilConoObliqueTwoLevels.ggb` and `InterCilConoOblique.ggb`; and
13. the actual pinned source and build tree.

Accepted ADR/spec decisions prevail over older planning language. If current
code invalidates an accepted premise, stop for author review rather than
silently widening the implementation.

# Required design/specification

Implement the normative semantic contract and Accepted ADR 0006 exactly within
the G6B boundary. Use the upstream-impact map as the reviewed minimum change
map and the validation matrix, tolerance policy and benchmark plan as the
executable evidence contract. Do not edit an accepted semantic decision to fit
an implementation shortcut; stop for author review if the real source requires
a larger semantic or serialization decision.

# Architectural placement

Definition, provider parameter, evaluator, branch identity, semantic revision
and dynamic dependencies belong in the shared Java kernel. Tessellation belongs
in a separate Euclidian drawable/cache. Runtime selection belongs in the
minimum explicit GeoCeDG application/kernel seam. Validation and benchmarks
belong in the existing external operational layer. No GUI, render cache,
exporter, prompt, manifest or generated artifact may become geometric
authority.

# Accepted architecture

## Parallel V2 entity

Implement a new, parallel experimental `GeoLocusV2`. Keep legacy `GeoLocus`,
`GeoLocusND`, `myPointList` and their meaning intact.

- Do not reinterpret, wrap or promote `myPointList` as V2 semantic data.
- Do not change the public command `Locus[...]`, `CmdLocus` or its dispatcher.
- Do not silently redirect any legacy construction or `.ggb` file to V2.
- A V1 diagnostic adapter may report sampled evidence only when it labels that
  evidence as legacy and non-semantic.

`AlgoLocusV2` names a conceptual algorithm family. Use separate minimal
driver-specific algorithms when the real dependency contracts justify them;
do not force one oversized algorithm merely to preserve that provisional name.

## Distinct append-only classification

Append a distinct V2 classification to `GeoClass`. Never insert it among
existing entries and never reuse `GeoClass.LOCUS`. Capture an ordinal snapshot
before and after the change and prove that every pre-existing ordinal is
unchanged.

During G6B, `GeoLocusV2`:

- is not a `Path`;
- returns `false` from inherited `isGeoLocus()`;
- returns `false` from inherited `isGeoLocusable()`;
- is not accepted by `Length`, `Perimeter`, `First`, ODE, `CmdLocus`, legacy
  incidence or point-on-locus paths;
- has no XML type, factory registration, persistence or migration;
- has no 3D behavior or 3D drawable; and
- remains explicitly unsupported by the G5 geometry/DXF export adapter.

Audit every switch or predicate affected by the appended enum, including 2D
draw dispatch, 3D draw dispatch, plane-view dispatch, defaults, labels,
drawable enumeration tests, metrics, commands, ODE, factories/XML and G5
export. Add explicit behavior or a narrow negative test; do not suppress broad
switch/test coverage. Tests must prove that V2 does not accidentally enter any
legacy contract.

## Provider-owned semantic parameter

The versioned driver-domain provider owns semantic parameter identity. Never
derive it implicitly from:

- normalized public `PathParameter[...]`;
- a sample index or interpolation fraction;
- `myPointList` or `PathMoverLocus`;
- current viewport bounds, zoom, DPI or render range; or
- query order or cache history.

The approved minimum is:

- `explicit-numeric-domain/v1`, with construction-owned interval, endpoint,
  orientation and periodicity policy; and
- `stable-path-domain/v1`, restricted initially to the path mappings approved
  by G6A: segment `[0,1]` and circle/ellipse angular parameter `[-pi,pi)`.

Do not add other path types for convenience. View-derived function domains are
excluded. A native GeoGebra parameter is semantic only when the provider
explicitly declares its mapping and stability. Preserve the future ability for
two CeDG projections to share one semantic parameter even when their internal
2D path parameterizations differ.

# Geometric invariants and degeneracies

Apply every applicable G6B row of
`docs/validation/g6_locus_v2_validation_matrix.md`. Preserve parameter
multiplicity, explicit invalid states, topology/lineage, determinism, zoom
invariance and semantic/render separation. Degenerate and unsupported cases
must return typed observable states; never retain stale geometry or substitute
a sampled approximation silently.

## Branches, valid components and lineage

Represent each semantic branch with at least:

```text
branchKey
declared driver domain
validDomainComponents[]
semantic orientation
provider/evaluator provenance
typed lineage
separated state and quality metadata
```

A branch is a constructively identifiable solution or sheet. A gap in validity
may create multiple valid-domain components without creating a new branch.
Component split/merge and semantic branch split/merge are separate events.

`branchKey` must be a deterministic semantic descriptor. It must not depend on
coordinates, render or validation samples, labels, list/visual order,
proximity, screen orientation or apparent direction. A crossing does not change
identity. A deterministically recognized branch may recover its key after an
inactive interval. Real split/merge publishes typed parent/child lineage.

Implement and test the approved fixture in
`geocedg/validation/locus-v2/topology-fixture.yml`, including one valid
component, two components within the same branch, isolated valid states, empty
domain and the independent branch-lineage sequence.

## Separated semantic axes

Do not create one catch-all “locus status” or collapse categories to reduce
class count. Keep these axes independently typed:

1. definition status;
2. branch/domain properties;
3. evaluation status;
4. optional regularity;
5. topology/lineage transition;
6. construction fidelity;
7. evaluation method;
8. representation role; and
9. numeric guarantee.

Use the normative values from the semantic contract. Do not infer
`SINGULAR` without a supported differential capability; a cusp may remain a
valid evaluation with regularity `UNKNOWN`. An analytic expression evaluated
with `double` is not `EXACT_ARITHMETIC`.

## Deterministic evaluation

Implement `POINTWISE_DETERMINISTIC` for the approved providers. Implement only
the subset of `CANONICAL_CONTINUATION_DETERMINISTIC` for which G6A evidence
defines a canonical anchor, semantic orientation and query-history-independent
continuation rule. If no such executable provider is author-approved at G6B
start, leave that capability unsupported rather than inventing one.

Every other history-dependent or ambiguous evaluator returns
`UNSUPPORTED_NONDETERMINISM`. Never hide history dependence inside mutable
cache/session state. For one semantic snapshot and address, results must be
independent of external query order and whether memoization is enabled.

# Scope

Implement only enough product code and internal/test seams to prove:

1. the semantic value/status/quality contracts;
2. `explicit-numeric-domain/v1`;
3. the approved `stable-path-domain/v1` segment and circle/ellipse providers;
4. Level A analytic fixtures required by the validation matrix;
5. the approved branch/valid-component topology and lineage fixture;
6. self-intersection parameter multiplicity;
7. periodic seam behavior;
8. unbounded semantic image/evaluation with display-only clipping;
9. normal dependency propagation and semantic revision publication;
10. nested semantic composition at depths one, two and three, plus only the
    approved small stress depth when useful;
11. a separate derived 2D drawable/cache;
12. `LEGACY`, `V2` and `DUAL` diagnostic seams; and
13. legacy, Classic and GeoCeDG coexistence.

Do not broaden support to every GeoGebra `Path`, every CeDG scientific model or
every topology family. Unsupported cases must produce typed diagnostics.

# Explicitly forbidden scope

Do not introduce a public surface, persistence, migration, public V2 `Path`,
G7/G8/G9 behavior, DXF locus export, 3D V2 behavior, concurrency, external
dependencies, controlled DAG flattening, broad upstream refactoring or any
legacy `Locus` semantic change. The complete mandatory stop list and disposition
appear under `# Stop conditions` and apply throughout execution.

# Kernel dependency and semantic revision contract

Normal `Construction`/`AlgoElement` input-output edges remain the sole
dependency authority. The implementation must not create a second dependency
graph or mutate the live construction outside normal DAG propagation.

Each V2 definition owns a local monotonically increasing semantic revision:

- publish a new revision only when normal recompute publishes a new semantic
  snapshot;
- point evaluation does not increment the revision;
- zoom, viewport, DPI, tessellation and render-cache rebuild do not increment
  the revision;
- upstream invalidation reaches downstream definitions through the normal DAG;
- derived semantic and render caches invalidate coherently by revision; and
- do not use `Construction.getStep()` as a semantic revision ID.

Dependency-slice construction or synchronization, if needed for an approved
driver evaluator, must be explicit, deterministic and revision-scoped. It must
not be reconstructed for every point query.

# Nested semantic composition — critical PASS gate

For a normal DAG dependency `L1 -> L2 -> L3`, evaluation must be compositional:

```text
semantic evaluate L3(u)
    -> semantic evaluate L2(psi(u))
        -> semantic evaluate L1(phi(psi(u)))
```

It must never mean:

```text
regenerate or tessellate L2
    -> regenerate or tessellate L1
```

A downstream V2 may consume an upstream V2 only through approved branch/domain
information, semantic evaluator, semantic revision, validity/exactness and
quality metadata. A downstream evaluation must never consume or invoke:

- `myPointList`;
- `PathMoverLocus`;
- `LocusRenderCache2D`;
- sampled or render polylines/vertices;
- upstream tessellation;
- complete upstream-locus regeneration;
- a complete upstream dependency-slice rebuild per point query; or
- hidden callback dependencies outside the normal kernel DAG.

## Scoped shared semantic evaluation session

Use the accepted minimum strategy: recursive semantic evaluators plus one
scoped shared evaluation session.

The session must:

- be bounded, disposable and limited to one coherent evaluation/batch;
- carry a coherent set of semantic revisions for all traversed loci;
- remain a memoization/evaluation context, not a second dependency graph;
- use the full semantic key:

  ```text
  locus identity
  semantic revision
  branchKey
  provider-canonical semantic parameter
  ```

- evaluate one eligible exact key at most once per session;
- provide cache-disabled/reference execution;
- return identical semantic results with memoization enabled or disabled; and
- maintain an active-key stack that detects re-entry/hidden cycles and returns
  a diagnosable typed failure.

Controlled DAG flattening/compilation is outside G6B. If recursive evaluators
plus the scoped session cannot meet the functional gates, stop for author
review; do not introduce flattening silently.

## Functional nested budgets and instrumentation

For controlled pointwise deterministic fixtures with `q` outer semantic
queries, dependency depth `d` and no duplicate requests:

```text
semantic evaluator calls = q * d
```

Only fixed, separately reported preparation/synchronization costs may be added.
Do not set an absolute millisecond budget before repeatable productive
measurements exist.

The following are functional PASS requirements:

- dependency-slice build count is at most once per definition/revision;
- no slice build occurs per point query;
- no upstream tessellation or render work occurs;
- no whole-upstream-locus regeneration occurs;
- invalidating the innermost source coherently invalidates `L1 -> L2 -> L3`;
- each locus publishes at most the revision warranted by its normal recompute;
- cache/session on and off produce equal results; and
- clearly superlinear growth with dependency depth is a blocker requiring
  author review.

Instrument and persist evidence for:

- dependency-slice build count;
- slice synchronization/reset count;
- dependency-update count;
- semantic evaluator call count;
- nested calls by level;
- duplicated upstream semantic request count;
- session hits and misses;
- semantic revision publication/consumption; and
- render work, recorded separately from semantic work.

“Appears fast” or wall-clock timing alone is not acceptance evidence.

## Cycle policy

Normal GeoGebra dependency checks remain responsible for DAG cycles. The
session active-key guard must additionally reject evaluator callback re-entry;
it must not make hidden callback cycles valid. Add a deterministic cycle test
and an actionable diagnostic.

# Real nested-locus evidence

Preserve and revalidate the original hashes and manifests:

- `models/legacy/inter-cil-cono-oblique-two-levels/original/InterCilConoObliqueTwoLevels.ggb`
  is the functional two-level legacy control; and
- `models/legacy/inter-cil-cono-oblique/original/InterCilConoOblique.ggb`
  is the pathological third-level `Flatten` legacy reference.

Do not modify, normalize, migrate or package either original. Their public
redistribution remains blocked pending rights/assets review. Do not generalize
the measured `AlgoLocusSliderND`/inner-locus/`AlgoPerimeterLocus` degradation
beyond the recorded model and run.

G6B must build a small internal, typed and deterministic V2 fixture explicitly
traced to the same nested-dependency mechanism. It must prove at least three
V2 levels, innermost invalidation, semantic-only consumption, no whole-locus
regeneration and the approved functional scaling. It need not convert either
`.ggb`, reproduce their coordinates or implement V2 `Perimeter`.

# Rendering contract

Implement a separate V2 drawable. Rendering consumes only the semantic
definition/evaluator and never exposes tessellation as semantic API.

- Render cache is bounded and keyed by view, semantic revision and render
  policy.
- Viewport, zoom, DPI and visual tolerance may change tessellation only.
- They must not change definition status, domain, branch keys, semantic
  revision or point evaluation.
- An unbounded branch is clipped only for presentation; clipping is not a
  semantic domain restriction.
- Demonstrate two zoom/view policies with different render tessellation and the
  same semantic locus/evaluations.

# Numerical validation and tolerance ownership

Use:

```text
max(1e-12 * max(1,S), 64 * ulp(max(1,S)))
```

only as the approved uncertified G6B comparison envelope. For every case,
document `S` as a characteristic geometric scale such as a defining length,
radius or local construction scale. `S` must not depend on absolute distance
from the origin, zoom, DPI, viewport, pixel density or render bounds. If no
defensible `S` exists, report the limitation.

Keep these policies separate:

- `eps_domain` and endpoint/component predicates;
- `eps_render` and pixel tessellation;
- future G7 metric error/tolerance; and
- future G8 isolation/residual tolerance.

Do not reuse the G6B envelope as a certified error bound or as any of those
other policies.

# Compatibility and serialization

Provide only the minimum real runtime/test seam needed for:

- `LEGACY`: Classic and public `Locus[...]` remain legacy;
- `V2`: internal/test experimental creation through a typed factory/API; and
- `DUAL`: explicit comparison of V1 sampled evidence with V2 semantic evidence,
  without treating V1 samples as authority.

The `experimental.yml` catalog records maturity; it is not a runtime feature
flag by itself. Do not create a broad feature-management framework. Existing
`.ggb` files open unchanged and no legacy result silently changes.

# G7 forward boundary

Record, but do not implement, this accepted forward rule: derived semantic
services consumed downstream must preserve semantic composition and be scoped
to the upstream semantic revision.

Do not implement `LocusMetricIndex`, `LocusLength`, V2 perimeter, productive
metric caches or sampled-chord metrics. G7 must later consume semantic data,
avoid render samples and avoid complete metric recomputation per downstream
query while the upstream semantic revision is unchanged.

# Implementation sequence

1. Reconfirm authorities, exact source impact, branch and clean entry state.
2. Record a before-edit ordinal/dispatch inventory and planned upstream files.
3. Implement immutable GeoCeDG-owned semantic value contracts and focused unit
   tests.
4. Implement only the approved provider/evaluator subset and determinism tests.
5. Implement scoped session, revision handling and nested fixtures with counters
   before adding rendering.
6. Add the parallel kernel element/driver algorithms and normal DAG wiring.
7. Append the V2 classification and audit every affected dispatch.
8. Add the separate 2D drawable/cache and zoom-separation evidence.
9. Add the minimum diagnostic runtime seam without changing Classic/public
   `Locus[...]` behavior.
10. Integrate focused verification, benchmarks, manifests and documentation.
11. Run focused gates, then the composed authority, clean generated outputs and
    produce the G6B report.

Do not postpone semantic, nested or negative-compatibility tests until after a
large implementation diff.

# Upstream modification discipline

Before modifying each productive file under `source/`:

1. explain why the change cannot reside wholly in GeoCeDG-owned additive code;
2. confirm it is within the accepted impact boundary;
3. make the smallest localized diff; and
4. immediately register the file and purpose in
   `docs/upstream/modified-files.yml`.

Do not perform general refactors, package moves, unrelated formatting or
cleanup. `GeoFactory`, XML, public command dispatch, legacy `GeoLocus*`, public
`Path`, metrics, G5 export and 3D paths are audit targets, not automatically
authorized edit targets.

# Required tests and commands

Add focused tests for at least:

- immutable value contracts and all separated state/quality axes;
- numeric/path provider identity, domains, endpoints, orientation and
  periodicity;
- pointwise determinism and the approved canonical-continuation subset, if any;
- typed unsupported nondeterminism;
- branch identity, valid components, topology and lineage;
- self-intersection multiplicity, periodic seam and unbounded semantic image;
- semantic revision publication and non-publication rules;
- dependency propagation and innermost invalidation;
- `NESTED-1`, `NESTED-2` and `NESTED-3`, plus only approved small depth stress;
- session enabled/disabled equality, duplicate memoization and full-key
  distinction;
- active-key cycle guard;
- required slice/session/revision/call counters and `q * d` budget;
- zoom invariance and semantic/render separation;
- append-only `GeoClass` ordinals and every affected dispatch;
- legacy `GeoLocus`, public `Locus[...]` and Classic compatibility;
- absence of V2 persistence/XML/factory migration;
- absence of public V2 `Path`, legacy metrics/incidence/ODE and 3D behavior; and
- explicit continued G5 rejection of V1/V2 locus export.

Create or update `tools/agent/verify-locus-v2.ps1` as a focused subordinate
verifier. It must not become a second acceptance authority. Keep
`tools/agent/verify.ps1` as the canonical composed gate and integrate G6B only
where deterministic and appropriate.

Run and record, at minimum:

- focused G6B tests and benchmarks;
- `tools/agent/verify-locus-v2.ps1` with the productive G6B gate enabled;
- relevant shared build/checkstyle and Desktop build;
- GeoCeDG launch smoke and Classic regression launch;
- manifests/schemas and preserved model hashes;
- relevant G2/G3/G5 regressions;
- `tools/agent/verify.ps1 -RunBenchmarks`;
- `git diff --check`;
- generated-output cleanliness; and
- residual-process checks.

Do not claim a launch, benchmark or invariant PASS without saved command
evidence. Any absolute timing remains evidence, not an approved gate, unless a
separate author-approved budget exists.

# Packaging boundary

Do not reopen G4 or create a new packaging policy. Verify only that productive
G6B classes are included by the existing Desktop/app-image/package layout when
the current operational gate requires it. Do not add the rights-blocked legacy
models or other excluded evidence to packages and do not generate installers
unless the existing validation authority requires them for this change.

# Required artifacts

Produce the implemented semantic types, focused tests, deterministic nested
fixtures, benchmark/counter evidence, minimal runtime and render seams, updated
feature/validation metadata, subordinate verifier integration, upstream
modification record, user-guide material and final G6B validation report. Do
not version generated build outputs or raw caches as source authority.

# Documentation and future-monograph evidence

Update `docs/user/geocedg_user_guide.md` exhaustively even though V2 has no
public command. Document only behavior actually demonstrated by G6B.

The guide must cover:

## Use and status

- what V2 product code and internal/test seam actually exist;
- how an authorized developer activates or inspects the experimental mode, if
  an observable seam exists;
- what an end user still cannot create, save, load, measure, intersect or
  export;
- Classic/public `Locus[...]` compatibility and diagnostic modes.

## Scientific foundation

- the sampled-list limitation of legacy `GeoLocus`;
- why samples/render polylines are not locus identity;
- the formal V2 object and provider-owned parameter;
- branch identity versus valid-domain components and lineage;
- semantic revisions and deterministic evaluation;
- separated exactness/numeric-guarantee axes and tolerance limits.

## Implemented architecture

- actual classes/interfaces and their responsibilities;
- normal kernel DAG integration;
- scoped evaluation session, key, memoization and cycle policy;
- nested semantic composition and functional counters;
- semantic/render/cache separation;
- distinct appended `GeoClass` and dispatch boundaries;
- every upstream productive file changed and why.

## Evidence and limitations

- the roles/hashes of both real nested legacy models;
- the measured legacy degradation mechanism without over-generalization;
- how the V2 architecture avoids that structural pattern;
- G6B nested, revision, zoom, compatibility and performance results; and
- explicit G7/G8/G9/DXF/persistence/public-Path exclusions.

The guide must remain usable as primary source material for a future GeoCeDG
monograph without presenting planned capabilities as implemented. Link to the
normative spec, ADR, architecture map, matrix, benchmark evidence and final
report rather than duplicating their full internal details.

Update the living roadmap, feature manifests and operational docs only to
reflect observed G6B state. Do not alter accepted semantic decisions to fit the
implementation.

# Required final report

Create `docs/validation/g6b_locus_v2_kernel_report.md` containing at least:

- entry commit, branch, baseline and authority states;
- exact implemented scope and explicit exclusions;
- semantic/provider/branch/revision/evaluator design realized;
- nested session design and `q * d` functional evidence;
- slice/session/revision/render instrumentation results;
- rendering and zoom-invariance evidence;
- classification/dispatch and legacy/Classic compatibility evidence;
- files added and every upstream productive file changed with justification;
- tests, commands, exit codes and saved log/evidence paths;
- benchmark results and distinction between functional and timing evidence;
- model hash/provenance verification;
- documentation, manifest and packaging checks;
- limitations, debt, unresolved risks and author decisions required;
- confirmation that no public command, persistence, public `Path`, G5 locus
  export, G7, G8 or G9 was implemented; and
- final state `G6B = PASS` or the exact blocked disposition below.

# G6B PASS criteria

G6B may be `PASS` only when all applicable rows of section 6.5 of
`docs/roadmap/g6_locus_v2_plan.md` and the normative validation matrix pass,
including:

- a real parallel experimental V2 entity with distinct append-only
  classification;
- provider-owned domain/parameter and explicit branches/components;
- viewport-independent deterministic evaluation and semantic revisions;
- normal-DAG dependency behavior;
- separate derived rendering with zoom-invariant semantics;
- three-level nested semantic composition satisfying `q * d` and lifecycle
  counters;
- session on/off equality and cycle diagnostics;
- legacy/Public Locus/Classic coexistence;
- verified absence of persistence and forbidden future surfaces;
- subordinate focused verification plus composed authority PASS; and
- complete traceable user/architecture/validation evidence.

# Stop conditions

Stop before implementing or changing any of the following:

- public command creation or any change to `Locus[...]`;
- V2 XML, persistence, serialization version or `.ggb` migration;
- public V2 `Path`, `Point[GeoLocusV2,...]` or
  `PathParameter[GeoLocusV2]`;
- G7 `LocusMetricIndex`, length, perimeter or productive metric cache;
- G8 intersections or incidence;
- G9 spatial/projection semantics;
- G5/DXF locus export;
- 3D Locus V2;
- a concurrency model;
- controlled DAG flattening without a new reviewed decision;
- a new external dependency or substantial toolchain;
- legacy `GeoLocus`/`myPointList` semantic changes;
- branch identity inferred from samples, coordinates, order or proximity;
- semantic domain/revision/evaluation derived from view/render state;
- live-construction mutation outside the normal DAG;
- hidden evaluator dependency/cycle;
- per-query upstream slice/locus regeneration; or
- clearly superlinear nested growth without an explained and author-reviewed
  cause.

If implementation needs any forbidden surface, contradicts the normative spec
or cannot satisfy a critical nested/compatibility gate, stop without widening
scope and report exactly:

```text
G6B = BLOCKED PENDING AUTHOR REVIEW
```

# G6B — Minimal Locus V2 kernel implementation report

| Field | Value |
|---|---|
| Phase | G6B — Minimal Locus V2 kernel implementation |
| Status | **G6B PASS / G6 PASS** |
| Branch | `feature/g6b-locus-v2-kernel` |
| Entry SHA | `b25153f4cfd563a47f00c3f98b5c67277037121d` |
| G6B prompt SHA-256 | `394b1fb1677205d6740a10da512a91b4e01b0f998f4eadcaf1c0e04a90b0fd53` |
| Baseline | GeoGebra 5.4.928.0 / `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Date | 2026-08-11 |
| Maturity | `experimental`, disabled by default, internal/test creation only |

## 1. Scope and preflight

The implementation executes the hardened canonical prompt
`.github/prompts/tasks/g6b-locus-v2-kernel.prompt.md`. Preflight established:

- a clean worktree before implementation;
- branch `feature/g6b-locus-v2-kernel` at entry SHA
  `b25153f4cfd563a47f00c3f98b5c67277037121d`;
- both `main` and annotated tag `geocedg-g6a-pass` resolving to ancestor
  `b6601425acdcd073e6f5d49eda82113cd03cd32f`;
- `G6A = PASS — AUTHOR APPROVED`;
- ADR 0006 `Accepted`;
- `geocedg/specs/locus/locus-v2-semantics.md` normative; and
- G7, G8 and G9 pending/not started.

The initial operational gate `tools/agent/verify-operational.ps1` passed before
productive edits. The task changes shared-kernel semantics only for the new
parallel type and adds derived 2D rendering. It does not reinterpret or migrate
legacy `GeoLocus`.

## 2. Architecture implemented

```text
declared GeoElement inputs
  -> AlgoLocusV2 family / normal kernel DAG
  -> GeoLocusV2
       -> immutable LocusDefinition2D @ semanticRevision
            -> provider + branches + evaluator + quality metadata
            -> LocusEvaluationSession2D
                 -> bounded full-key memoization + active-key cycle guard

EuclidianView
  -> DrawLocusV2
       -> bounded per-drawable LocusRenderCache2D
            -> semantic evaluator
            -> presentation vertices only
```

The semantic object is independent of view state. The renderer is a consumer,
not an authority. Nested algorithms declare upstream V2 outputs as ordinary
`AlgoElement` inputs and consume immutable semantic definitions, preserving the
kernel DAG as dependency authority.

## 3. New productive classes

### 3.1 Semantic value and service layer

Added under `org.geocedg.common.kernel.locus`:

- `LocusSemanticMetadata2D`: independent definition, branch-property,
  evaluation, regularity, lineage, fidelity, method, role, numeric-guarantee,
  determinism and orientation enums;
- `LocusPoint2D`, `LocusInterval2D`, `LocusEvaluation2D`, `LocusQuality2D`,
  `LocusLineage2D` and `LocusSemanticKey2D`;
- `LocusBranch2D`, `LocusBranchSnapshot2D`, `LocusDefinition2D` and immutable
  source-snapshot contracts;
- `LocusDriverDomainProvider2D`, `ExplicitNumericDomainProvider2D` and
  `StablePathDomainProvider2D`;
- evaluator, point/branch/parameter/transform functional interfaces;
- `LocusEvaluationSession2D` and `LocusInstrumentation2D`;
- `LocusValidationTolerance2D`, `LocusV2Mode`, `LocusV2Factory` and
  `LocusDualRunDiagnostic2D`.

All value objects are read-only after construction. The session is bounded and
disposable and never owns dependency edges or definitions.

### 3.2 Kernel object and algorithm layer

- `GeoLocusV2`: final parallel `GeoElement`, distinct type, immutable snapshot
  owner, internal semantic evaluation entry and no XML output;
- `AlgoLocusV2`: normal-DAG base that publishes a new monotonic revision only
  when semantic content changes;
- `AlgoAnalyticLocusV2`: explicit numeric pointwise pilot;
- `AlgoSegmentPathLocusV2`: live characterized segment provider without using
  public `PathParameter` as semantic identity;
- `AlgoDynamicBranchLocusV2`: immutable source capture plus provider-owned
  branch/component/lineage snapshots; and
- `AlgoNestedLocusV2`: recursive upstream semantic evaluation with a shared
  scoped session.

### 3.3 Derived rendering

- `DrawLocusV2`: dedicated 2D drawable;
- `LocusRenderPolicy2D`: immutable view-owned presentation policy;
- `LocusRenderData2D`: derived vertices and subpath markers; and
- `LocusRenderCache2D`: four-entry per-drawable cache keyed by semantic revision
  and view/render policy.

Render vertices are not exposed from `GeoLocusV2`, `LocusDefinition2D` or any
semantic evaluator.

## 4. Provider coverage and parameter semantics

| Provider | Versioned parameter | Coverage |
|---|---|---|
| `explicit-numeric-domain/v1` | Explicit finite domain, endpoint policy, orientation, periodicity, provider-owned `eps_domain` | Productive analytic, topology and nested pilots |
| `stable-path-domain/v1` | Segment `t in [0,1]`; circle/ellipse angle `t in [-pi,pi)` | Live segment algorithm; immutable provider tests for segment/circle/ellipse |

The implementation does not adopt normalized public `PathParameter`, a sample
index or a viewport range. It does not broaden provider coverage to functions,
arbitrary paths or native infinite intervals. The unbounded render test uses a
mathematically unbounded image over a finite open semantic parameter domain.

## 5. Branch, domain, lineage and status implementation

`LocusBranch2D` keeps `branchKey`, declared domain,
`validDomainComponents[]`, orientation, provenance, typed lineage, branch
properties and quality. Its semantic signature includes lineage parent/child
keys. Branch identity never uses labels, coordinates, samples, order or
proximity.

The formal dynamic fixture traverses one valid interval, two valid intervals,
two isolated components and empty domain. It also publishes unchanged, split,
merged, disappeared and appeared lineage events with stable provider keys.
Separate tests cover a discrete 1-to-2 branch topology change, collapsed image,
cusp, dependency undefined/recovery, self-intersection multiplicity and a
periodic seam.

Definition status, branch properties, evaluation status, optional regularity,
lineage, construction fidelity, evaluation method, representation role and
numeric guarantee remain separate types. Invalid evaluations contain no stale
point.

## 6. Determinism and numerical policy

G6B implements `POINTWISE_DETERMINISTIC`. Forward, reverse and shuffled query
orders produce identical semantic results for one revision. The diagnostic
mode seam labels V1 sampled evidence and V2 semantic evidence independently.

No canonical-continuation provider met the approved minimum scope. Such cases
remain `UNSUPPORTED_NONDETERMINISM`; no history-dependent result is hidden in a
cache.

Numeric reference comparisons use only:

```text
max(1e-12 * max(1,S), 64 * ulp(max(1,S)))
```

with a documented characteristic geometric `S` for every Level-A, live-path
and topology case. Results are `FLOATING_POINT_UNCERTIFIED`; analytic formulas
evaluated with `double` are not reported as exact arithmetic. Domain, render,
future G7 metric and future G8 intersection tolerances remain separate.

## 7. Semantic revision and invalidation

The local revision starts positive and increases only when a normal recompute
publishes changed semantic content. Point evaluation, memoization, query order,
render, zoom and DPI do not publish a revision. Defining source changes travel
through ordinary kernel dependencies and cause affected downstream definitions
to publish coherently.

The live segment pilot demonstrates that changing the segment endpoints changes
the semantic snapshot, while moving the currently constrained presentation
point does not redefine the complete locus. Undefined dependencies publish a
typed invalid snapshot and recover on the next valid normal-DAG update.

## 8. Mandatory nested semantic composition

For V2 `L1 -> L2 -> L3`, each downstream algorithm captures the upstream
immutable definition for its own revision and invokes its semantic evaluator.
It does not use `myPointList`, `PathMoverLocus`, render vertices, polylines,
samples, a dependency-slice build or whole-locus regeneration.

With 64 unique outer queries:

| Depth | Expected `q*d` calls | Observed calls |
|---:|---:|---:|
| 1 | 64 | 64 |
| 2 | 128 | 128 |
| 3 | 192 | 192 |
| 5 | 320 | 320 |

A depth-three duplicate batch performs 192 misses, then 64 exact-key hits, with
192 retained entries in a 256-entry session. Cache-enabled and reference
session results are equal. A capacity-two fixture verifies deterministic
bounded eviction. Active-key re-entry returns a cycle diagnostic.

Changing the innermost source publishes exactly one changed semantic snapshot
at each affected level. Instrumented dependency-slice builds,
synchronizations, whole-locus regenerations and upstream render evaluations are
zero. The functional cost therefore follows requested semantic queries times
depth, not a product of render tessellation densities.

## 9. Render separation

Two presentation policies produce 33 and 129 vertices for the same definition.
Semantic revision and point evaluations are unchanged. Disconnected valid
components create two graphical subpaths. A divergent analytic image remains
semantically evaluable and is inset/clipped only by the presentation policy.

The renderer currently uses a bounded uniform parameter budget. It provides no
metric, intersection or error-certified approximation and cannot be consumed
by downstream semantics.

## 10. Distinct `GeoClass` and compatibility

`GeoClass.LOCUS_V2` is appended after the former final constant, preserving all
existing ordinals. The dedicated `EuclidianDraw` route avoids the legacy locus
interface cast. `DrawablesTest` keeps exhaustive enum coverage.

Executable and static checks establish:

```text
Classic + public Locus[...] = unchanged legacy GeoLocus
GeoCeDG + public Locus[...] = unchanged legacy GeoLocus
GeoLocusV2                 = internal experimental entity only
```

`GeoLocusV2` is not `Path`, `GeoLocusNDInterface` or `GeoLocusable`; it does
not claim `isGeoLocus()` or `isGeoLocusable()`. There is no V2 route in
`CmdLocus`, `AlgoDispatcher`, `GeoFactory`, `CmdLength`, `CmdFirst`,
`CmdPerimeter`, ODE, XML, G5 DXF or 3D drawing. Existing `.ggb` files cannot
create or persist V2 and therefore retain legacy behavior.

## 11. Legacy scientific controls

The original author-supplied files remain unchanged and hash-pinned:

| Artifact | SHA-256 | Role |
|---|---|---|
| `InterCilConoObliqueTwoLevels.ggb` | `587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6` | Functional two-level legacy control, approximately 125–127 ms |
| `InterCilConoOblique.ggb` | `b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27` | Pathological third-level `Flatten` reference |

The pathological recorded run measured approximately 31.9 ms before
`Flatten`, 6.03/5.95/5.67 s for the three outer-locus creations, undefined
results after the 500 ms per-step guard and approximately 21.0 s subsequent
recompute. Instrumentation located, for this model, repeated
`AlgoLocusSliderND` slice updates containing two inner loci and two
`AlgoPerimeterLocus` operations.

G6B structurally removes sampled/render/full-upstream-locus consumption from
nested V2 evaluation. It does not implement the legacy perimeter dependency or
convert either `.ggb`; G7 must ensure future derived metrics are semantic,
revision-scoped and not recomputed wholesale per downstream point. Public
redistribution of both originals remains blocked pending rights/assets review.

## 12. Upstream files modified

| File | Change and necessity |
|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/plugin/GeoClass.java` | Append a distinct V2 classification; a kernel element cannot obtain safe dispatch externally |
| `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianDraw.java` | Route the new type to its dedicated drawable; the legacy route requires a sampled interface V2 must not claim |
| `source/shared/common-jre/src/test/java/org/geogebra/common/euclidian/DrawablesTest.java` | Preserve exhaustive drawable coverage for the appended enum |

All GeoCeDG-owned additions and purposes are registered in
`docs/upstream/modified-files.yml`. No unrelated upstream refactor or formatting
change was made.

## 13. Deviations from the approved plan

- `LocusV2Factory` resides in the semantic package rather than the algorithm
  package because it assembles value/provider contracts and algorithms.
- No dependency-slice evaluation context was needed for the minimum pilots.
  Algorithms capture immutable inputs during normal recompute; evaluation
  builds and synchronizes no cloned slice.
- Only the segment mapping has a live path-backed algorithm. Circle and ellipse
  mappings are value-contract tested but not broadened into unapproved live
  driver coverage.
- Native infinite driver intervals are deferred. An unbounded geometric image
  is demonstrated without treating viewport bounds as a domain.
- Canonical continuation is deferred because G6A approved no concrete provider
  rule for the minimum implementation.
- The focused verifier originally hashed checked-out Java evidence bytes, which
  failed on Windows CRLF despite identical Git content. It now hashes logical
  UTF-8 text normalized to LF while retaining byte-exact hashes for `.ggb`.
  Its productive-file inventory also matches any `LocusV2` class name instead
  of only exact `AlgoLocusV2` prefixes. Neither correction weakens evidence.

These deviations reduce scope without reducing the normative semantics.

## 14. Tests and validation evidence

| Gate | Result |
|---|---|
| `LocusV2ValueContractsTest` | 5 tests, 0 failures |
| `LocusV2KernelIntegrationTest` | 16 tests, 0 failures |
| `LocusV2RenderSeparationTest` | 5 tests, 0 failures |
| `LocusV2FunctionalBenchmarkTest` | 2 tests, 0 failures |
| `DrawablesTest` | 4 tests, 0 failures |
| `verify-locus-v2.ps1` | PASS; 15 G6A + 28 G6B/dispatch tests, both checkstyles; log `%TEMP%\geocedg-g6b-focused\g6-locus-v2-gradle.log` |
| Full shared/common-jre + Desktop tests/checkstyle | PASS; 53 Gradle tasks, exit 0 |
| GeoCeDG launch smoke | PASS; Windows window title `GeoCeDG`, Temurin JDK 25; process tree exited |
| Classic launch smoke | PASS; Windows window title `GeoGebra Classic 5`, Temurin JDK 25; process tree exited |
| `verify.ps1 -RunBenchmarks` | PASS, exit 0; logs `%TEMP%\geocedg-g6b-composed` |
| Operational benchmark | PASS, informational median 2180.175 ms; `%TEMP%\geocedg-g6b-composed\operational-benchmark.json` |
| Packaging/content contracts | PASS through composed authority; no release artifacts generated |
| Worktree preservation | PASS in focused and composed verifiers |
| Regenerable outputs and residual process audit | PASS; generated build/cache dirs and three temporary probes removed; zero processes reference this checkout |
| `git diff --check` | PASS |

The versioned evidence manifest is
`geocedg/validation/locus-v2/g6b-functional-evidence.yml`. JUnit XML, Gradle
reports and logs are regenerable and remain outside version control.

## 15. Documentation and feature state

Updated artifacts include the normative specification implementation profile,
semantic architecture, upstream impact map, executable plan, living roadmap,
validation matrix, benchmark evidence, scientific-pilot disposition, feature
manifest and the living user guide. `cedg.locus.v2` is experimental, disabled
by default and has no public workflow.

The user guide now documents actual operational access, legacy compatibility,
the scientific distinction between samples and locus, providers, branches,
quality axes, revisions, nested evaluation, render separation, measured legacy
evidence and the G7/G8/G9 boundary. It is suitable as primary technical
evidence for a future GeoCeDG monograph without claiming future features.

## 16. Limitations and technical debt

- internal/test creation only; no end-user activation;
- no canonical-continuation implementation;
- no native infinite provider domain or general path provider;
- no public command, `Path`, XML, migration or public copy contract;
- no metric/index/length/perimeter (G7);
- no intersections/incidence (G8);
- no spatial/projection semantics (G9);
- no DXF locus export or 3D behavior;
- uniform render sampling is presentation-only and not error-certified;
- no concurrency or controlled DAG flattening;
- absolute latency and retained object-size budgets remain informational;
- internal `GeoElement.copy()` rebinds immutable definitions but is not a
  migration/persistence guarantee and requires review before public exposure.

## 17. Closeout disposition

No G7, G8 or G9 implementation was started. The public command and Classic
compatibility boundary remain unchanged. Focused G6, full shared/Desktop,
launch, operational, packaging-content, benchmark, cleanliness and
residual-process gates all completed successfully.

**G6B = PASS**

**G6 = PASS**

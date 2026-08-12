# G6R — Locus V2 hardening report

| Field | Value |
|---|---|
| Status | **G6R PASS** |
| Branch | `feature/g6r-locus-v2-hardening` |
| Entry SHA | `e78b4e71ebf752de8c3552b466dbee52b400ab94` |
| G6 implementation commit | `0c4cc40a389477226b2a6cb507c4fa072790a586` |
| Baseline upstream | `9b93256b7df401ff056c37b502d82df4d72b1522` / GeoGebra 5.4.928.0 |
| Date | 2026-08-12 |
| Public availability | **NOT YET** — experimental/internal/developer-only |

## 1. Scope and semantic boundary

G6R audited and hardened the productive G6 implementation, added an isolated
developer laboratory, measured the current evaluator/session/render behavior,
and consolidated implementation/API/repository documentation. It did not alter
the normative semantic contract or ADR 0006. It did not start G7, G8 or G9.

The source-level audit covered every productive class under the GeoCeDG locus,
algorithm, geo and drawable packages plus the three localized upstream changes
made in G6. The durable API/responsibility/mutability/thread/invariant matrix is
in [the implementation architecture](../architecture/locus_v2_implementation.md).

## 2. Findings and fixes

| Finding | Risk | G6R correction |
|---|---|---|
| Several immutable-looking values lacked value equality | Equivalent recompute could publish spurious revisions | Added equality/hash contracts for providers, branches, snapshots, lineage and source snapshots |
| Semantic keys accepted non-finite values and signed zero was not canonical everywhere | Unstable/incoherent session identity | Reject non-finite addresses and normalize `+0.0/-0.0` |
| Lineage accepted structurally invalid parent/child sets | Ambiguous topology evidence | Added typed transition cardinality and unique-key validation |
| Session diagnostics were string-only and lifecycle was implicit | Cycle/revision/closed failures hard to distinguish; stale retention risk | Added typed immutable diagnostics, `AutoCloseable`, clear/close and active-stack evidence |
| Instrumentation exposed only ad-hoc mutable reads | Diagnostic panels/tests could observe inconsistent dimensions | Added immutable instrumentation snapshots |
| Explicit undefined state did not recover on semantically equivalent recompute | Stale undefined object without semantic change | Restore defined state without creating a revision |
| `copy`/`set` could inherit incomplete generic lifecycle behavior | Semantically incomplete clones/assignments | Explicitly disabled copy/internal copy/assignment pending an approved lifecycle/persistence contract |
| Render policy was uniform only and cache lacked observable counters | Unmeasured render overhead, weak cold/warm evidence | Added uniform reference + bounded adaptive visual policy and cache counters |
| No visual inspection path existed | Developers could only inspect tests | Added isolated opt-in Desktop laboratory with temporary preferences and diagnostics |
| Initial diagnostics dialog exceeded the available desktop height | Its title bar and close control could be placed off-screen | Reduced and contract-tested the initial dialog dimensions; visual smoke confirms a fully reachable window |
| Initial segment laboratory fixture assembled a raw, undefined `GeoSegment` | The live segment provider correctly reported `DRIVER_INVALID`, defeating the fixture purpose | Constructed the segment and constrained point through normal upstream algorithms and asserted `VALID` in the Desktop contract test |
| G6B evidence used ambiguous `entry.commit` | Entry and implementation provenance conflated | Split `entry_sha`, `implementation_commit`, `baseline_upstream_sha`; G6R adds explicit hardening commit semantics |

No coordinate/sample hash was introduced as branch or locus identity.

## 3. Lifecycle and dispatch audit

New tests cover equivalent recompute, signed zero, explicit undefined/recovery,
undefined/recovery through L1→L2→L3, removal of the innermost input, diagnostic
labels/selection/defaults, unsupported copy/set/deep-copy and absence of XML
undo persistence. Removing the source removes all dependent algorithms through
the normal construction DAG.

The repeated `LOCUS_V2` audit classifies:

- supported: append-only `GeoClass`, dedicated 2D drawable, diagnostic labels,
  selection and generic line defaults;
- explicitly unsupported: 3D/plane view, public/legacy Path and incidence,
  metrics, commands/ODE, XML/factory/migration, G5 export;
- not applicable: stable toolbar/default feature exposure.

Existing GeoClass ordinals remain unchanged. `GeoLocusV2` still returns false
for `isGeoLocus()`/`isGeoLocusable()` and implements none of the legacy locus
interfaces.

## 4. Provider matrix

| Provider/family | Contract | Productive provider | Tested | Live normal-DAG algorithm | Public user access |
|---|---|---|---|---|---|
| `explicit-numeric-domain/v1` | Yes | Yes | Yes | Yes | No |
| stable segment mapping | Yes | Yes | Yes | Yes | No |
| stable circle mapping | Yes | Yes | Yes | No | No |
| stable ellipse mapping | Yes | Yes | Yes | No | No |
| general functions/native infinite path domain | Partial semantic characterization only | No general provider | Selected finite fixtures | No | No |
| canonical continuation | Determinism category only | No | Unsupported-status behavior | No | No |

Circle/ellipse were deliberately not promoted to live algorithms: no G6R
hardening requirement needed that new integration surface.

## 5. Developer laboratory

The explicit launcher is:

```powershell
.\tools\locus-v2\open-locus-v2-laboratory.ps1
```

It delegates to `:desktop:desktop:runLocusV2Laboratory`, creates a temporary
preference file and uses a distinct process/window identity. It is absent from
normal GeoCeDG and Classic and registers no command, menu or toolbar entry. Its
ten visible fixtures cover analytic numeric, live segment, branch/components/
lineage, discontinuity, unbounded presentation clipping and nested depth 1–5.
The diagnostics panel exposes identity, revision, provider, branches/domains,
lineage, status/quality axes, session counters and derived render counts. It
warns that the construction cannot be saved as `.ggb`.

The visual smoke on 2026-08-12 confirmed the distinct window title, reachable
diagnostics controls, the no-save warning, a `VALID` live segment provider,
separate valid components, the unbounded presentation fixture and a `VALID`
nested depth-five fixture. Both defects found during the smoke are covered by
the Desktop contract test rather than being accepted as manual-only behavior.

## 6. Performance baseline and decisions

Functional gates remain authoritative. With 128 distinct outer queries, depths
1/2/3/5 performed exactly 128/256/384/640 evaluator calls, i.e. `q*d`, in both
session modes. Dependency-slice builds/synchronizations per query, whole-locus
regeneration and upstream render dependencies remained zero.

For repeated depth-three keys, memoization produced 128 hits after 384 misses.
A capacity-32 session retained 32 entries and deterministically evicted 352.
Because unique-query timing showed memoization overhead and duplicate reuse
already works, capacity/eviction policy was not changed.

Adaptive render was accepted. On the controlled line fixture, uniform 256-step
render used 257 vertices/evaluations; adaptive visual render used 5 vertices and
9 evaluations at the same semantic revision/result. Tests cover parabola chord
error, discontinuous components, periodic seam, unbounded clipping and cache
cold/warm/eviction behavior. Uniform mode remains the reference.

Absolute latency and retained-memory limits are not hard gates: the current
single-workstation JVM distributions did not characterize noise sufficiently.
No JMH, concurrency, pooling, external dependency, C++ or DAG flattening was
introduced.

## 7. Documentation and traceability

Created/updated:

- [implementation architecture](../architecture/locus_v2_implementation.md);
- [internal API reference](../developer/locus_v2_api.md);
- [repository map](../developer/repository_map.md);
- [traceability matrix](g6r_locus_v2_traceability_matrix.md);
- [living user guide](../user/geocedg_user_guide.md);
- versioned [`g6r-hardening-evidence.yml`](../../geocedg/validation/locus-v2/g6r-hardening-evidence.yml).

The user guide is the conceptual/operational entry point. The API document
contains signatures and extension rules; the implementation document contains
the complete class/lifecycle/dispatch audit. No document replaces the normative
specification.

## 8. Upstream/product files

The G6 upstream changes to `GeoClass.java`, `EuclidianDraw.java` and
`DrawablesTest.java` were audited but not modified in G6R. The inherited
`source/desktop/desktop/build.gradle.kts` gained only the explicit laboratory
JavaExec task. Productive hardening and the laboratory otherwise reside in
GeoCeDG-owned `org.geocedg` packages. The upstream modification registry records
the build-task purpose.

## 9. Compatibility and packaging

- Classic and public `Locus[...]` remain legacy and unchanged.
- Normal GeoCeDG does not expose V2 or the laboratory.
- Visual launch smoke confirmed the distinct normal titles `GeoCeDG` and
  `GeoGebra Classic 5`, with no laboratory dialog or V2 toolbar entry in either.
- G5 continues to reject V2 export.
- A temporary `-Target All` build generated and validated app-image, ZIP, MSI
  and EXE from the current G6R worktree. `GeoLocusV2`,
  `LocusEvaluationSession2D` and `DrawLocusV2` were found in `common.jar`; the
  developer-only `LocusV2Laboratory` was found in `desktop.jar`. No installer
  entry point exposes the laboratory.
- `verify-packaging.ps1 -RequireArtifacts` validated the artifacts, SBOM,
  composition manifest, hashes, exclusions and evaluation-only marker. The
  generated manifest records the entry Git SHA because this was a pre-commit
  worktree validation; it is not durable release provenance. All generated
  packages were removed at closeout.
- No blocked legacy/scientific model or prohibited asset was added.
- Public redistribution status remains the independent G4 legal/asset blocker.

## 10. Remaining limitations and G7 readiness

Public creation, persistence/migration, public `Path`, point-on-locus, metrics,
intersections, spatial bindings, locus export, 3D, concurrency and productive
canonical continuation remain absent. Copy/assignment/undo persistence are
explicitly disabled, not partially emulated.

G7 may build on immutable definitions, revisions, branches and evaluators. It
must introduce a separately approved revision-scoped metric contract and must
not consume render samples or recompute an entire upstream locus per downstream
query. G7 was not started by this phase.

## 11. Validation evidence

| Gate | Result | Durable/log evidence |
|---|---|---|
| Focused G6/G6R shared tests and checkstyle | PASS — 26 new hardening tests plus the existing G6 suites | `%TEMP%\geocedg-g6r-composed-final\locus-v2\g6-locus-v2-gradle.log` |
| Desktop laboratory tests and checkstyle | PASS — 3 tests | `%TEMP%\geocedg-g6r-composed-final\locus-v2\g6r-laboratory-gradle.log` |
| Shared/baseline compile | PASS | `%TEMP%\geocedg-g6r-composed-final\shared-compile.log` |
| Desktop/baseline compile | PASS | `%TEMP%\geocedg-g6r-composed-final\desktop-compile.log` |
| G5 DXF and frontend compatibility | PASS | `%TEMP%\geocedg-g6r-composed-final\dxf` and `frontend` |
| `verify.ps1 -RunBenchmarks` | PASS; benchmark within informational budget | `%TEMP%\geocedg-g6r-composed-final` |
| `verify.ps1 -SkipBuild -RunBenchmarks` final composition recheck | PASS, exit 0 | `%TEMP%\geocedg-g6r-composed-static-final` |
| App-image/ZIP/MSI/EXE generation and `verify-packaging.ps1 -RequireArtifacts` | PASS | transient artifacts plus versioned G6R evidence record |
| App-image class inspection | PASS — V2 semantic/render classes in `common.jar`, laboratory in `desktop.jar` | versioned G6R evidence record |
| Developer laboratory, normal GeoCeDG and Classic visual smoke | PASS | versioned G6R evidence record |
| Internal links, manifests, schemas and controlled upstream registry | PASS | operational authority |
| `git diff --check` | PASS | closeout command |
| Generated-output and residual-process audit | PASS — zero repository build/cache outputs; zero Java/Gradle/GeoCeDG processes | closeout command |

The full build used launcher Java 22, Gradle 9.4.1 and the approved Desktop
Temurin JDK 25 toolchain. The packaging validation used jpackage 25.0.4 and WiX
5.0.2. Absolute timing remains informational; the versioned functional counts
are the performance authority.

## 12. Final disposition

```text
G6R = PASS
G6 REMAINS PASS
LOCUS V2 PUBLIC AVAILABILITY = NOT YET
G7 = NOT STARTED
```

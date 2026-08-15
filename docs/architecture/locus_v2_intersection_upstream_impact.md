# Locus V2 2D intersections — author-approved upstream impact map

| Field | Value |
|---|---|
| Status | **G8B-R1/G8B SOURCE IMPACT AUTHOR APPROVED** |
| Baseline | GeoGebra 5.4.928.0, `9b93256b7df401ff056c37b502d82df4d72b1522` |
| GeoCeDG G8A entry HEAD audited | `315aec011cdc719a41a9bdc352a4a10ea502df6e` |
| Roadmap | G7/G8 planning/G8A/G8B-R1/G8B/G8C design/G8C1/G8C2 and global G8 author-approved; G9 design authorized/not started |
| Date | 2026-08-14 |

This map records the actual pinned-source extension points, accepted G8A
lifecycle results, G8B edits, and compatibility risks. The canonical G8B
execution remained within the exact approved boundary.

## 1. Existing intersection dispatch and lifecycle

| Source file / class | Observed responsibility | G8 consequence |
|---|---|---|
| `.../kernel/commands/CmdIntersect.java` | Parses public `Intersect` overloads and dispatches by runtime type | Keep unchanged in the minimum G8B; public command routing is a separate author decision |
| `.../kernel/algos/AlgoDispatcher.java` | Central 2D construction factory; caches/reuses some existing intersection algorithms | An internal G8 kernel need not enter this dispatcher; any later entry risks Classic behavior and must be separately scoped |
| `.../kernel/kernelND/AlgoIntersectND.java` | Base for multi-intersection algorithms and shared output access | Useful lifecycle precedent, not an identity contract for V2 |
| `.../kernel/algos/AlgoIntersect.java` | Common intersection base, incidence registration, existing-point handling | Existing behavior assumes Classic point semantics; do not inherit coordinate identity silently |
| `.../kernel/algos/AlgoIntersectSingle.java` | Selects one output by index/reference and follows a parent intersection algorithm | Index selection is not a durable semantic root identity |
| `.../kernel/algos/AlgoIntersectLineConic.java` | Line/conic analytic-numeric solving, tangent handling and old/new-point continuity | Valuable target-specific oracle; its current continuity policy is not sufficient for G8 semantic identity |
| `.../kernel/algos/AlgoIntersectConics.java` | Conic/conic roots, permutation and continuity management | Shows topology/output complexity; not a locus–conic drop-in solver |
| `.../kernel/algos/AlgoElement.java` (`OutputHandler`) | Creates/resizes outputs and marks excess outputs undefined | Supports bounded derived slots, but it normally grows and does not solve root identity/history bounds |

The inspected Classic conic intersection paths compare old/new output points
and use coordinate distance/permutations for continuity. Some near-to logic
depends on kernel view scale. Those choices remain valid Classic behavior but
are forbidden as G8's geometric authority.

## 2. Existing root helpers

| Source file / class | Observed responsibility | Planning classification |
|---|---|---|
| `.../kernel/algos/AlgoSimpleRootsPolynomial.java` | Sorts and deduplicates polynomial roots/point coordinates using kernel precision conventions | Candidate comparison oracle only; its tolerance and coordinate dedup are not automatically G8 policy |
| `.../kernel/algos/AlgoRoots.java` | Samples functions over an interval and delegates to numeric root/extremum logic | Not a completeness or tangency authority; the interval/sample count can be view driven and sign changes miss even roots |
| existing conic algorithms | Use specialized algebraic capabilities for supported combinations | Audit capability-by-capability; do not label floating output exact without evidence |

G8A identified that none of these generic helpers can be imported as a G8B
completeness or identity authority. G8B therefore calls no viewport-driven
helper: its evaluator-only fallback has independent budgets and reports
coverage `NOT_ESTABLISHED`, while an injected stronger capability must declare
and then survive verification of its coverage, candidates, residuals and
membership.

## 3. Target geometric authorities

| Source file / class | Authoritative data observed | Scope implication |
|---|---|---|
| `.../kernel/geos/GeoLine.java` | Homogeneous line coefficients/incidence plus path range | Suitable Level A support-line equation; segment/ray restrictions remain type-specific |
| `.../kernel/geos/GeoSegment.java` | Finite line subtype and endpoint/path membership | Approved minimum target with explicit included-boundary classification |
| `.../kernel/geos/GeoRay.java` | Half-line subtype and start/path membership | Approved minimum target with explicit boundary/direction membership |
| `.../kernel/kernelND/GeoConicND.java` | Symmetric conic matrix, evaluation, type and degeneration state | Circle is approved through verified type and normalized radial residual; full conics are deferred |
| `.../kernel/implicit/GeoImplicit.java` | Polynomial equation evaluation, coefficient and derivative facilities | Legitimate Level C characterization candidate; not automatic minimum support |
| `.../kernel/geos/GeoFunction.java` | Function evaluation plus paths that often use view-limited intervals | Do not use a visible interval as complete semantic domain; defer pending a domain contract |

No generic implicit conversion layer is approved. Target adapters must retain
the source object's own type, revision, validity, and incidence semantics.

## 4. Locus V2 and G7 source boundary

Productive GeoCeDG source inspected includes:

- `org.geocedg.common.kernel.geos.GeoLocusV2`;
- `org.geocedg.common.kernel.algos.AlgoLocusV2`;
- immutable definition, domain, branch, component, evaluator, revision, and
  metadata types under `org.geocedg.common.kernel.locus`;
- bounded `LocusEvaluationSession2D`;
- `GeoLocusMetricResult`, `AlgoLocusMetricV2`, and the G7 metric service/index
  types; and
- view-owned `LocusRenderCache2D`.

The reusable authority is the captured G6 definition/evaluator/session and its
branch/component/revision model. The G7 result publication pattern and direct
G6 `NumericGuarantee` reuse are precedents. The G7 metric partition, cumulative
length state, shared metric owner, and render cache are not intersection
services or candidate isolation authorities.

## 5. GeoElement classification, copy, and persistence

| Source file / class | Current behavior | Risk / approved boundary |
|---|---|---|
| `.../common/plugin/GeoClass.java` | Append-only type classification now includes GeoCeDG V2, metric-result, and rich intersection-result types | G8B appended only `LOCUS_INTERSECTION_RESULT` at priority 132; no prior value or Classic dispatch changed |
| `.../kernel/GeoFactory.java` | Factory for supported GeoElement types; no public V2/metric persistence route | Keep unchanged; a no-XML internal Geo must not be made factory-persistent by accident |
| `.../kernel/geos/GeoElement.java` | Base definition/copy/set/remove/update contracts | Rich result must implement defensive copy/set and definedness without becoming numeric or path-like |
| XML and construction factories | Recreate supported serialized types | Explicitly outside G8; no V2 intersection result or point association persistence |

The approved rich Geo follows the normal Construction lifetime but is not a
publicly constructible/persisted type. Its `copy`/`set` behavior must preserve
one immutable snapshot or a coherent diagnostic; it cannot alias mutable
solver state.

## 6. Variable outputs, labels, and stable identity

Classic algorithms commonly maintain arrays of `GeoPoint`, reorder new points
to old points, preserve assigned labels, and set unused outputs undefined.
That lifecycle is insufficient for G8 because:

- coordinate-nearest matching conflates distinct constructive preimages;
- output index changes when roots merge or split;
- a tangent root cannot inherit one of two parents arbitrarily;
- a periodic seam can reorder the same semantic root; and
- a growing output handler can retain unbounded historical slots.

The rich result therefore owns root tokens and topology lineage. G8B must add
one separate internal point consumer selected by a root token; it is a bounded
derived consumer rather than the identity source and never retargets by
coordinate.

G8B-R1 refines only that consumer boundary. It adds solution-local isolation
evidence to the existing candidate/revision records and evaluates admissibility
inside the immutable rich result. The point can consume an established token
from an `INCOMPLETE` or `NOT_ESTABLISHED` finite parent without hiding that
parent state. No additional upstream-owned productive file or dispatch surface
is required.

## 7. Incidence and Path boundaries

Existing `Path`, point-on-path, `isOnPath`, and incidence-registration APIs do
not provide G8's branch/component/preimage identity contract. G8 planning does
not add general `Path` behavior to `GeoLocusV2`, does not create arbitrary
points on it, and does not reinterpret a coincident `GeoPoint` as a semantic
position. A verified intersection solution may expose an internal semantic
binding without widening public incidence behavior.

## 8. Construction update, removal, and exception safety

The G8B algorithm must use normal `setInputOutput()` dependency edges
for both sources and normal recomputation/removal. A private computation
captures revisions and publishes atomically. On target/locus invalidation,
exception, or removal:

- the old result is not current;
- no partial root collection is visible;
- any query-local or owned derived state is released/bounded;
- downstream consumers observe one coherent diagnostic state; and
- recovery after valid input is a fresh current-revision computation.

No listener, global repository, UI callback, or renderer may form a hidden DAG.

## 9. 2D/3D and Classic dispatch boundary

`CmdIntersect3D` and the 3D algorithm dispatcher have their own overload and
fallback rules. G8 is strictly a Locus V2 2D phase. It must not make V2 a 3D
curve, route through 3D dispatch, add spatial projection identity, or alter
Classic `Intersect` selection.

Legacy `GeoLocus` and its sampled `myPointList` remain unchanged and cannot be
silently migrated to `GeoLocusV2`.

## 10. Actual G8B editable set

The actual productive set is additive:

- new GeoCeDG-owned intersection query, policy, target-adapter, solver,
  result, classification, diagnostic, and continuation types under
  `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/`;
- a focused `AlgoLocusIntersectionV2` under the GeoCeDG algorithm package;
- a rich `GeoLocusIntersectionResult` under the GeoCeDG geos package;
- a required internal `AlgoLocusIntersectionPointV2`-equivalent consumer;
- focused shared-kernel tests under matching GeoCeDG test packages; and
- test-private characterization/support fixtures and versioned validation
  evidence.

The one unavoidable upstream-owned productive edit was:

1. append `GeoClass.LOCUS_INTERSECTION_RESULT` at priority 132.

No productive drawing switch required an edit because the rich Geo is
deliberately non-drawable and the existing behavior rejects it consistently.
Four focused GeoCeDG shared-kernel test files exercise the additive productive
API. The existing `LocusV2KernelIntegrationTest`,
`LocusMetricProductiveLifecycleTest`, and
`G8AIntersectionKernelLifecycleCharacterizationTest` were updated only to
record append-only ordering; exhaustive `DrawablesTest` proves the rich type
remains non-drawable.

The following are *not* in the approved minimum edit set:

- `CmdIntersect` or command factories;
- `AlgoDispatcher` public integration;
- legacy `AlgoIntersect*` behavior;
- `GeoFactory` or XML readers/writers;
- public `Path`/point-on-path support;
- Euclidian render code except a type-exhaustiveness test if required;
- desktop/web UI, export, 3D, Python DSL, or G9 sources.

Every approved upstream-owned modification must be recorded under the
repository modified-file governance and keep upstream notices intact.

## 11. Characterization versus productive placement

| Work | Correct layer in G8A | Possible layer in approved G8B |
|---|---|---|
| Analytic/high-precision references | `geocedg/validation/` scripts/data | Test oracle only |
| Solver comparisons and adversarial roots | test-private shared-kernel fixtures | Selected GeoCeDG solver implementation |
| Scientific pilot adapters | test/validation only | Only generic approved semantics, never pilot-specific solving |
| Counters and benchmark traces | test-private instrumentation/evidence | Stable functional counters where needed for gates |
| Rich result and DAG lifecycle | API/lifecycle probe | GeoCeDG shared-kernel classes |
| Public points/commands/XML | prohibited | Still deferred; the required internal token-selected point consumer is additive kernel code, not a public surface |

## 12. Principal compatibility risks

1. importing Classic coordinate-near identity or view-dependent root sampling;
2. treating individually verified roots as a complete set without independent
   completeness evidence;
3. losing distinct semantic preimages during coordinate deduplication;
4. allowing variable output slots or history to grow without bound;
5. reusing G7 metric state across incompatible intersection policies;
6. adding a new Geo type without complete copy/set/remove/drawing-switch tests;
7. changing public dispatcher precedence or legacy `.ggb` results;
8. accidentally serializing an experimental internal result; and
9. crossing the 2D/3D or G8/G9 boundary.

Any of these requires stopping productive work and returning to author review.

## 13. G8A audit disposition

The actual test-private adapters confirmed:

- `GeoLine.getX/getY/getZ` supply a scale-normalizable line equation;
- `GeoSegment` and `GeoRay` limited-path membership can be verified after
  support-line incidence without projecting a candidate;
- `GeoConicND.getFlatMatrix`, type and derivative formula supply circle and
  conic equation authority, but ellipse/parabola/hyperbola/degenerate subtype
  completeness is not uniform;
- `GeoFunction.value` plus a genuinely explicit `hasInterval` domain can define
  incidence, while view-limited min/max cannot; and
- `GeoImplicit.evaluateImplicitCurve`, `derivativeX/Y` and coefficients are
  legitimate Level C authority but do not by themselves prove component
  completeness.

The actual `GeoElement`/`AlgoElement` probe validated a nonnumeric rich output,
normal input/output registration, P1-style atomic publication, defensive
copy/set, failure/recovery, a token-selected point consumer, label/removal and
an empty XML implementation. It used `GeoClass.DEFAULT` only test-private.
G8B then realized the approved productive form with the append-only
`LOCUS_INTERSECTION_RESULT` value and focused exhaustive-type behavior tests.

The author requires that internal point consumer in G8B and permits one
append-only dedicated `GeoClass` if the rich Geo requires it. No evidence
requires edits to `GeoLocusV2`, public dispatch, Classic
intersection code, `Path`, `GeoFactory`, XML, rendering or 3D. The exact
candidate additive API is in
[`locus_v2_intersection_api.md`](../developer/locus_v2_intersection_api.md).

## 14. G8B implementation disposition

G8B realized the audited design with 33 additive classes under
`org/geocedg/common/kernel/locus/intersection`, two additive internal
algorithms, one additive rich Geo, four focused test classes, and four narrow
existing compatibility-test updates. The only pre-existing productive file
changed is `GeoClass.java`, where
`LOCUS_INTERSECTION_RESULT` is appended at priority 132. The exact per-file
governance record is in [`modified-files.yml`](../upstream/modified-files.yml).

The following audited files remained unchanged: `GeoLocusV2`, `GeoLocus`,
`CmdIntersect`, `AlgoDispatcher`, every Classic `AlgoIntersect*`, `Path`,
`GeoFactory`, XML readers/writers, Euclidian drawing code, export, desktop/web,
3D, Python, and every G9 source. The rich Geo emits no XML and has no factory
route. Productive evaluator-only isolation explicitly reports completeness
`NOT_ESTABLISHED`; complete claims enter only through an authoritative
capability whose declared coverage and candidates the productive solver checks
again.

The R1 edit stays inside the same authorized productive set: metadata,
candidate/revision evidence, evaluator fallback, solver plumbing, continuation,
rich-result/Geo lookup, and the existing token point algorithm. The append-only
`GeoClass` edit is unchanged. There is still no edit to any public command,
`Path`, persistence, legacy/Classic, 3D, Level C, or G9 source.

The author approved this exact bounded source-impact state for G8B-R1 and G8B
on 2026-08-14. The 2026-08-15 G8C design closeout authorizes only G8C1 within
its separate upstream impact inventory; it does not retroactively widen this
G8B edit set or authorize G8C2.

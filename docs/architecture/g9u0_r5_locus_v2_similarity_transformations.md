# G9U0-R5 Locus V2 2D similarity transformations

- Phase: **G9U0-R5 — LOCUS V2 2D SIMILARITY TRANSFORMATIONS**
- State: **DESIGN PASS — AUTHOR APPROVED**
- Implementation: **PASS — AUTHOR APPROVED**
- Primary layer: shared Java kernel / semantic Locus V2 DAG
- Secondary future surface: existing transformation commands; G9U1 workspace
  discoverability

```text
G9U0-R5 DESIGN = PASS — AUTHOR APPROVED
G9U0-R5 IMPLEMENTATION = PASS — AUTHOR APPROVED
implementationStarted = true
implementationAuthorized = true
selfApproved = false
designAuthorApproved = true
implementationAuthorApproved = true
passClaimed = true
```

This note is the author-approved R5 design and implemented architecture. The
author separately authorized execution of the checked-in canonical R5 prompt,
selected finite `k=0` Option A and, after the final dynamic-factor
characterization, closed R5 `PASS — AUTHOR APPROVED` on 2026-08-31. The author,
not automated evidence, supplies that approval. R5 does not authorize or
execute G9U1.

## 1. Objective and semantic form

R5 makes the ordinary two-dimensional translation, rotation, reflection and
uniform-dilation command families accept a public `GeoLocusV2`. The output is
a new first-class semantic Locus V2 in the normal construction DAG:

```text
L'(u) = T(L(u))
```

`L` remains semantic authority for its address and domain. `T` is derived from
ordinary current GeoGebra transformation inputs. `L'` has a new durable object
identity and is independently usable by Locus V2 point, metric, intersection,
transformation, persistence and presentation operations.

No rendered polyline, render cache, generic `Path`, copied snapshot or frontend
decoration is a transformed-locus authority.

## 2. Inspected upstream transformation surface

The current-source authority inspected for this design is:

- the basic command registration in
  [`BasicCommandProcessorFactory`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/BasicCommandProcessorFactory.java);
- the four 2D processors
  [`CmdTranslate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdTranslate.java),
  [`CmdRotate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdRotate.java),
  [`CmdMirror`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdMirror.java) and
  [`CmdDilate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdDilate.java);
- their shared 2D dispatcher seam in
  [`AlgoDispatcher`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoDispatcher.java); and
- the existing transform wrappers
  [`TransformTranslate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/TransformTranslate.java),
  [`TransformRotate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/TransformRotate.java),
  [`TransformMirror`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/TransformMirror.java) and
  [`TransformDilate`](../../source/shared/common/src/main/java/org/geogebra/common/kernel/TransformDilate.java).

The exact current public forms and routes are:

| Family | Processor and current route | R5 public forms |
|---|---|---|
| Translation | Ordinary object form: `CmdTranslate` -> `AlgoDispatcher.translate` -> `TransformTranslate` -> `AlgoTranslate`. The separate vector-at-point form uses `AlgoTranslateVector` and is not an R5 locus form. | `Translate[L,v]`, with an ordinary finite 2D vector |
| Rotation | Origin form: `CmdRotate` -> `TransformRotate` -> `AlgoRotate`. Centered form: `CmdRotate` -> `AlgoDispatcher.rotate` -> centered `TransformRotate` -> `AlgoRotatePoint`. The special text route is unchanged. | `Rotate[L,a]`; `Rotate[L,a,C]` |
| Reflection | `BasicCommandProcessorFactory` maps both `Reflect` and `Mirror` to `CmdMirror`. Point/line operands route through `AlgoDispatcher.mirror` -> `TransformMirror` -> `AlgoMirror`. | axial `Reflect[L,g]` / `Mirror[L,g]`; central `Reflect[L,C]` / `Mirror[L,C]` |
| Uniform dilation | Origin form: `CmdDilate` -> `TransformDilate` -> `AlgoDilate`. Centered form: `CmdDilate` -> `AlgoDispatcher.dilate` -> centered `TransformDilate` -> `AlgoDilate`. | `Dilate[L,k]`; `Dilate[L,k,C]` |

`CmdMirror` also routes a conic/circle operand to geometric inversion. That is
not an ordinary reflection and is explicitly outside R5. Shear, non-uniform
stretch, projective transformation, circle inversion and 3D transformations
are likewise excluded.

The basic command factory already maps `Reflect` and `Mirror` to `CmdMirror` and
already owns all four command identifiers. R5 needs no parallel command, new
command enum or second dispatcher.

## 3. Why the generic mutation algorithms are not the R5 seam

`GeoLocusV2` deliberately implements neither `Path` nor the upstream mutable
`Transformable`, `Translateable`, `Rotatable`, `Mirrorable` or `Dilateable`
contracts. The generic algorithms create/copy an output geo, call `set()` and
then mutate its coordinates through those interfaces.

That route cannot preserve Locus V2 semantics:

- `GeoLocusV2.copyInternal()` creates an unpublished shell and copies only
  visual style;
- `GeoLocusV2.set()` clears the semantic definition and makes the object
  undefined;
- a mutable coordinate transform would provide no evaluator, domain, branch,
  revision or durable-lineage authority; and
- implementing the generic interfaces merely to satisfy command or tool
  selection would make their mutation contract falsely applicable.

R5 must therefore add explicit `GeoLocusV2` overload branches to the four
existing processors. Each branch delegates immediately to the existing
`LocusV2PublicOperations` construction boundary. It must not place semantic
logic in a command processor.

## 4. Kernel design recommendation

The implementation candidate realizes the smallest architecture-consistent
design through three roles. Class names below identify the current code seam;
the semantic roles, not the names, remain normative.

1. **Public construction operation.** It validates feature access and supported
   2D inputs, reserves a new output identity, builds one normal-DAG parent,
   applies the host transformation style convention and atomically publishes
   the output plus direct source/parameter dependency IDs.
2. **Reconstructible Locus V2 parent.** It extends the existing `AlgoLocusV2`
   publication lifecycle, uses the ordinary command geos as its serialized and
   update inputs, and returns the corresponding existing `Commands` value.
3. **Immutable similarity snapshot/evaluator.** It captures one finite 2D
   similarity map and one immutable source definition. Evaluation first calls
   the source definition at the same canonical semantic address and maps only a
   valid finite point. Invalid source evaluations are propagated without stale
    coordinates. A transformed overflow becomes `NON_FINITE`.

The corresponding candidate classes are
`AlgoLocusSimilarityTransform2D`, `LocusSimilarityTransform2D` and
`LocusSimilarityEvaluator2D`. `LocusV2PublicOperations` remains the single
feature-gated construction and durable-identity boundary. It publishes a new
first-class output ID, records the source and every ordinary transformation
input as direct dependencies, and uses exception-safe participation/redefine
rollback: an R5 transform rejected before its own publication leaves no promoted
label, attached parent or reserved identity behind. This does not change the
upstream rule that a successful nested subcommand may remain when a later,
unrelated outer command fails.

`AlgoNestedLocusV2` is the inspected precedent for evaluator-to-evaluator
composition through one scoped `LocusEvaluationSession2D`. It is not itself the
public solution: it is internal, single-branch and caller-configured rather than
an ordinary reconstructible command parent.

The transformed snapshot signature must be deterministic and include:

- transformed locus identity only through the normal definition owner;
- source persistent identity and semantic revision;
- transformation family/version;
- normalized finite transformation coefficients; and
- source provider/branch content through the normal definition comparison.

It must exclude labels, XML/construction order, screen state, render samples and
coordinates used as identity. A style-only update must compare semantically
equal and publish no new semantic revision.

## 5. Durable object identity and provenance

Every transformed locus receives a new `PersistentGeoId`, even when the map is
geometrically the identity (`v=0`, `a=0`, `k=1`) or when a composition returns
to the source image. Reusing the source ID is forbidden.

The existing participation batch and `GeoIdentityRecord.dependencies` are the
cross-object provenance seam. The transformed record depends exactly on the
source Locus V2 and the vector/angle/center/axis/factor geos used by its command.
Normal copy/remap may record its existing immediate copy lineage.

`LocusLineage2D` is branch lifecycle metadata inside one locus, not an
alternative cross-object provenance graph. It must not be overloaded. Source
branch keys may be retained inside the new locus namespace; the new locus ID
keeps the two semantic addresses distinct across objects.

## 6. Domain, branch and parameter contract

For every supported transformation:

- the source provider remains parameter authority;
- declared domain and valid-domain components are preserved exactly;
- branch keys and branch lifecycle transitions are preserved where current;
- periodic canonicalization and half-open seam policy are preserved;
- increasing/decreasing semantic traversal is preserved;
- disconnected components and invalid gaps remain disconnected/invalid; and
- the transformation maps evaluated world geometry, never the parameter.

Axial reflection reverses ambient planar orientation but does not silently
reverse semantic parameter traversal. Central reflection is a half-turn and
also preserves the source traversal. Negative uniform scale remains a valid
map with unchanged semantic traversal.

That preserved domain, component lineage, periodic seam and traversal provide
the semantic ordering frame used by the R4 intersection authority. A future
query on `T(L)` derives a new intrinsic phase/rank inside its transformed
source-pair context from this frame and current semantic root evidence. It does
not copy the phase/rank of a query on `L`; ambient reflection, transformed
coordinates, solver enumeration and presentation order do not redefine the
source semantic orientation.

## 7. Degeneration contract

| Input/state | Required transformed state |
|---|---|
| Source temporarily undefined or unpublished | undefined output; no stale semantic publication |
| Source `EMPTY_DOMAIN` | `EMPTY_DOMAIN`; no fabricated branch or point |
| Undefined/nonfinite/3D vector | unsupported/undefined 2D transform state |
| Undefined/nonfinite angle | undefined transform state |
| Undefined, infinite or 3D center | undefined transform state |
| Undefined/nonfinite line or zero line normal | undefined axial-reflection state |
| Undefined/nonfinite scale factor | undefined dilation state |
| Finite `k = 0` | Option A — author approved: valid source-domain-preserving degenerate locus with `COLLAPSED_IMAGE` |
| Finite `k < 0` | valid uniform dilation; length scale `abs(k)`; parameter traversal retained |
| Finite coefficients but mapped coordinate overflows | per-address `NON_FINITE`; no stale coordinate |
| Periodic source | identical provider canonicalization and one semantic seam |
| Disconnected source | identical component partition; no bridging |
| Further transformation after `k = 0` | normal DAG output, still collapsed for finite maps |

The author selects **Option A — valid `COLLAPSED_IMAGE`**. The implementation
retains the source domain, branches, semantic addresses and invalid gaps; maps
every source-valid address to the dilation center; keeps the result as a
semantic `GeoLocusV2`, not a `GeoPoint`; and applies the established
collapsed-image metric consequences. In particular, source `FINITE` or
`UNBOUNDED` remains the domain classification and `COLLAPSED_IMAGE` is added as
an independent image property. Evaluation must consult the source first: an
invalid source address remains invalid because zero scale cannot erase a
semantic gap. Option B is rejected for R5.

## 8. Metric covariance

The transformed locus remains an ordinary rich metric source. It is not a
displayed scalar adapter.

```text
translation:              length(T(L)) = length(L)
rotation:                 length(T(L)) = length(L)
line/point reflection:    length(T(L)) = length(L)
uniform dilation:         length(T(L)) = abs(k) * length(L)
```

The same relation holds for partial lengths between corresponding valid
semantic addresses. Tests compare rich values, coverage, diagnostics and error
evidence within the existing metric contract. At `k=0`, the transformed
branch's provider-justified `COLLAPSED_IMAGE` property is semantic proof that
every address in each retained valid component has identical image and therefore
exact rich length zero. Invalid gaps are not valid components and remain
domain-invalid. This component-state specialization is inside the existing
evaluator-only metric authority; it is not a displayed-scalar shortcut and does
not bypass the transformed locus as metric source.

## 9. Intersection covariance and tokens

`Intersect[T(L),X]` uses the ordinary rich Locus V2 intersection pipeline. No
type-specific transformation exception belongs in a solver.

At the required R5 implementation entry, that pipeline includes the
author-approved R4 deterministic current-snapshot selector and intrinsic
semantic phase/rank proof. At every recomputation, the transformed evaluator,
preserved semantic-domain orientation, current source-pair and constructive
lineage, branch/component/topology context and current isolation evidence
derive a new transformed-query phase/rank and determine whether one exact token
selector is uniquely valid. Prior root positions, transformation-update
history, Cartesian order, solver/list order and marker/UI order may not decide
the current transformed-query identity. Reaching the same final transformed
Construction state and durable IDs through different regular update paths must
therefore produce the same admissibility, phase/rank selector, token binding
and materialized-point definedness.

When a supported invertible transformation is applied to both operands, the
finite/overlap geometry must covary within the existing evidence contract:

```text
T(Intersections(L,X))  geometrically corresponds to
Intersections(T(L),T(X))
```

This is geometric correspondence only. The transformed source IDs, result IDs,
source-pair identities, phase/rank selector certificates and exact tokens are
new. Source phase/rank allocations and tokens may not be reused, transformed or
matched by coordinates/order/proximity. Geometric covariance likewise does not
authorize a previous-frame matching heuristic or reuse of a selector
certificate from the untransformed query.

The non-invertible `k=0` case is excluded from bijective covariance. If the
collapsed point lies on the target, the current
overlap/non-isolated policy remains authoritative; R5 must not fabricate an
isolated admissible root.

## 10. Semantic Point-on-Locus covariance

For every valid source address `(branch,u)`:

```text
Point[T(L), branch, u]
```

must geometrically correspond to applying `T` to
`Point[L, branch, u]`. The points and loci remain distinct constructed objects
with their own durable identities. Cartesian position is never the address.

## 11. Transformation closure and dynamic updates

Every R5 output is accepted by every supported R5 family. Compositions such as
`Translate[Rotate[L,a,C],v]`, `Reflect[Dilate[L,k,C],g]` and repeated nested
maps remain normal algorithm chains, not flattened snapshots.

Source, vector, angle, center, axis and factor are normal inputs. A current
change recomputes the transformed semantic snapshot and every downstream point,
metric, intersection and transformation through the DAG. No listener, polling,
view cache or frontend recomputation is an update authority.

The byte-exact author fixture `fourSolutionsDynamicDilate.cedg` closes the final
dynamic-factor question. Slider-driven `GeoNumeric.setValue()` and explicit
editing of the existing numeric object in Algebra recompute the same live R5
parent across positive, negative and zero factors, including repeated
`nonzero -> 0 -> nonzero` recovery and save/reopen. The free-input expression
`k=0.25` is rejected before R5 as `REDEFINE_CONTEXT_MISSING` by the accepted G9A
redefine contract; rejection is atomic and leaves the live construction
unchanged. The author accepts that characterization as a nonblocking future
product-UX requirement. R5 does not broaden G9A and does not describe the host
`Please check your input` response as a dilation failure.

## 12. Style and presentation

At creation the transformed locus follows the host
`setVisualStyleForTransformations()` convention. It then uses the R2 ordinary
`GeoElement` color, thickness, line type, opacity, visibility and label
authority. Style is presentation only and cannot alter source/output IDs,
semantic revisions, domain, topology, metrics or intersections.

No new Locus V2 style store is permitted.

## 13. Persistence and Classic boundary

The normal command plus its ordinary input geos reconstructs the transformed
parent. Save/reopen preserves:

- command inputs and source dependency;
- transformed durable identity and dependency record;
- styles;
- downstream semantic points, metrics, rich intersections and their own exact
  transformed-query selectors/tokens; and
- normal copy/remap and undo/redo behavior.

Render vertices, sampled polylines, matrices detached from input geos and opaque
Java lambdas are not serialized. The current ZIP/XML machinery and
`app="classic"` marker remain unchanged.

During file loading, the existing Locus V2 preservation context may reconstruct
the command. Feature-off GeoCeDG Classic must not gain interactive experimental
creation authority. No additional runtime flag is introduced.

## 14. Command and tool boundary

The existing four commands are the candidate's mandatory public R5 creation
routes. Their
`GeoLocusV2` branches enforce the same one Locus V2 feature flag and delegate to
the one kernel authority.

The 3D command subclasses reject Locus V2 axis, plane, 3D-center and oriented-3D
forms before entering generic 3D transformation algorithms. These exclusions
are fail-closed and leave construction membership, XML and durable identity
unchanged. Axial reflection normalizes large finite homogeneous line
coefficients with scale-safe arithmetic before constructing the semantic map;
ordinary coefficient rescaling remains invisible to the result.

Current Euclidian transformation tools select objects through mutable
`TestGeo.TRANSFORMABLE`, `TRANSLATEABLE` and `DILATEABLE` predicates and then
invoke the generic mutation algorithms. R5 must not make `GeoLocusV2` satisfy
those predicates. Professional discoverability belongs to the post-R5 G9U1
manifest/action registry. If author review requires legacy click-tool parity
before G9U1, it needs a separately enumerated, feature-gated GeoCeDG controller
route that still calls the same public kernel operation; it cannot justify
generic interface conformance.

## 15. Productive candidate boundary

The shared-kernel candidate consists of:

- narrow overload branches in `CmdTranslate`, `CmdRotate`, `CmdMirror` and
  `CmdDilate`;
- fail-closed Locus V2 exclusions in `CmdRotate3D` and `CmdMirror3D` for 3D
  axis/plane/center routes that otherwise intercept before the 2D processors;
- public construction methods plus exception-safe identity participation in
  `LocusV2PublicOperations`;
- `AlgoLocusSimilarityTransform2D`, `LocusSimilarityEvaluator2D` and
  `LocusSimilarityTransform2D` under GeoCeDG-owned packages; and
- the collapsed-image component specialization in the existing
  `EvaluatorOnlyLocusMetricCapability2D`.

All eleven productive paths are registered in
`docs/upstream/modified-files.yml`. The candidate changes no `Path` or mutable
transformation interface conformance, generic `Transform`/
`AlgoTransformation` semantics, intersection solver, XML/archive format,
Desktop renderer or G9U1 frontend.

## 16. Rejected alternatives

- **Implement mutable transform interfaces on `GeoLocusV2`:** false contract;
  produces an unpublished/copy shell rather than a semantic evaluator.
- **Transform render vertices or metric partitions:** presentation/derived data
  would become geometry authority.
- **Create `TranslateLocusV2`-style commands:** duplicates the existing public
  command family and breaks ordinary interoperability.
- **Serialize a transformed point cloud or Java closure:** not reconstructible
  and not normal DAG semantics.
- **Reuse source IDs, selector phase/rank or intersection tokens:** confuses
  geometric covariance with durable identity; every transformed query derives
  its own selector in its new source-pair context.
- **Use the rejected Option B at `k=0`:** the author-approved R5 contract requires
  a valid source-domain-preserving `COLLAPSED_IMAGE`.

## 17. Entry, validation and exit

R5 entered from G9U0-R4 `PASS — AUTHOR APPROVED`, the author-approved
R5 design/specification with Option A, explicit R5 authorization and a clean
selected main.
The retained R4 risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open and tracked: R5 does
not resolve it, does not make it an implicit transformation dependency and does
not weaken its required G9U1/global-G9 disposition.
The focused matrix in
`docs/validation/g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md`
must run deterministically with the historical G6–G9 authorities and full
composed verifier.

Automated implementation initially stopped at the required candidate state. The
subsequent author decision closes the phase as:

```text
G9U0-R5 = PASS — AUTHOR APPROVED
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS WITH G9A FREE-INPUT LIMITATION CHARACTERIZED
freeInputCompatibleRedefine = DEFERRED TO G9U1 DESIGN / NOT AN R5 BLOCKER
```

The prospective G9U1 authority remains unexecuted and requires its own author
authorization. It retains kernel-selector/exact-token-only candidate-marker hit
testing, explicit create-one/create-all and opt-in visible frontend
auto-materialization, with no UI/list/marker rank authority; professional
menu/tool, visual-identity, existing-host `Continuity = OFF` GeoCeDG product
invariant with Classic configurability, and `geocedg.brand.topbar` /
`geocedg.brand.startup` contracts. It must also investigate free-input
compatible numeric redefinition only through the accepted G9A compatibility
predicate and atomic transaction: a label may locate an explicitly intended
current object in the command context but never becomes durable identity;
ambiguous, absent or incompatible cases remain fail-closed. That prospective UX
work did not broaden G9A during R5.

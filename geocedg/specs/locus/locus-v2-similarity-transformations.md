# Locus V2 2D similarity transformations

- Status: **DRAFT / NORMATIVE CANDIDATE — DESIGN CANDIDATE — PENDING AUTHOR
  REVIEW**
- Phase: **G9U0-R5**
- Implementation: **NOT AUTHORIZED — NOT STARTED**
- Semantic base: accepted
  [`Locus V2 semantics`](locus-v2-semantics.md), metrics, intersections, public
  persistence and R2 presentation contracts
- Required predecessor: G9U0-R4 `PASS — AUTHOR APPROVED`

This document becomes normative only after explicit author approval. It does
not authorize implementation or G9U1.

## 1. Scope

R5 shall make a public semantic `GeoLocusV2` a supported first argument of the
existing ordinary two-dimensional forms:

```text
Translate[L, v]
Rotate[L, angle]
Rotate[L, angle, center]
Reflect[L, point]      / Mirror[L, point]
Reflect[L, line]       / Mirror[L, line]
Dilate[L, factor]
Dilate[L, factor, center]
```

The transformed output is a new first-class semantic Locus V2. Circle/conic
inversion, shear, non-uniform stretch, arbitrary affine/projective maps and 3D
transformations are excluded.

The existing 2D host command authority remains in place: the origin forms use
their current transform wrapper, centered rotation/dilation and ordinary
translation/reflection pass through the current shared dispatcher to those
wrappers, and `Reflect`/`Mirror` remain aliases of the same mirror processor.
R5 adds a typed Locus V2 construction branch; it does not define a second
command registry or freeze a future implementation class name as normative.

## 2. Semantic definition

For source definition `L` and one current supported transformation `T`, the
result is:

```text
L'(u) = T(L(u))
```

Evaluation of `L'` shall:

1. validate the same source semantic address through the source provider;
2. evaluate the current immutable source snapshot;
3. propagate every invalid source status without coordinates; and
4. apply `T` only to a valid finite semantic point.

The transformation acts on geometry, not on the semantic parameter. Render
vertices, metric partitions and screen state are never input to this operation.

## 3. Identity and dependencies

`L'` receives a new durable object ID. The source ID shall never be reused,
including for an identity map or a composition whose image equals the source.

The normal construction parent depends on the source locus and every ordinary
transformation input geo. Those DAG dependencies and the existing durable
identity record are the source/derived provenance authority. Labels,
construction order, XML position, coordinates, samples and screen state are
not identity.

Branch lifecycle metadata shall not be used as a second object-provenance
system. A source branch key may be retained within the new locus identity
namespace.

## 4. Domain, parameter and topology

Where the source definition is current, the transformed locus preserves:

- the provider-owned canonical parameter;
- declared domain;
- valid-domain components and their boundaries;
- branch keys and current lineage transitions;
- periodic canonicalization/seam policy; and
- semantic parameter orientation.

Axial reflection may reverse ambient orientation but does not reverse semantic
parameter traversal. Central reflection and negative uniform dilation likewise
do not silently reverse the source parameter.

This preserved semantic domain, component lineage, periodic seam and
orientation are the ordering frame for the R4 intrinsic semantic phase/rank
contract. A rich intersection query on the transformed locus derives its own
phase/rank in the new transformed source-pair context. It shall not copy a
source query's selector allocation or derive rank from transformed Cartesian,
solver/list, marker or UI order.

Disconnected components remain disconnected. Invalid addresses and genuine
semantic discontinuities remain invalid/discontinuous even when their finite
images would coincide.

## 5. Supported transformations

### 5.1 Translation

The vector is the ordinary finite 2D command input. Source geometry is mapped
by the current vector. A zero vector creates a distinct semantic output with
the same geometric image.

### 5.2 Rotation

The angle and optional center follow ordinary 2D command conventions. The
two-argument form uses the ordinary origin. Undefined, nonfinite or 3D inputs
do not produce a valid transformed snapshot.

### 5.3 Reflection

R5 supports axial reflection in a valid finite 2D line and central reflection
in a valid finite 2D point. `Reflect` and `Mirror` are aliases routed through
the existing command processor.

A circle/conic second operand denotes inversion in the existing host command.
It is not included and must not be treated as an ordinary R5 reflection.

### 5.4 Uniform dilation

The factor and optional center follow ordinary 2D command conventions. The
two-argument form uses the ordinary origin. Negative factors are valid.

The finite `k=0` behavior is **AUTHOR DECISION REQUIRED before implementation**.
The accepted [Locus V2 semantic contract](locus-v2-semantics.md), its
[`COLLAPSED_IMAGE` semantic model](../../../docs/architecture/locus_v2_semantic_model.md),
the accepted [metric contract](locus-v2-metrics.md), and the
[`COLLAPSED_IMAGE` metric model](../../../docs/architecture/locus_v2_metric_semantic_model.md)
define consequences when a provider truthfully publishes a valid collapsed
image; they do not require this future command overload to publish one.

The author must select exactly one policy:

- **Option A (recommended) — valid `COLLAPSED_IMAGE`:** preserve source domains,
  branches, semantic addresses and invalid gaps, map every source-valid address
  to the dilation center, and retain a semantic `GeoLocusV2` rather than
  converting it to a `GeoPoint` or sampled object.
- **Option B — unsupported/undefined transformed state:** publish no valid
  transformed locus while `k=0`, expose a typed unsupported/undefined state,
  retain no stale semantic snapshot, and recover deterministically after a
  supported factor returns.

The selected policy must be recorded in the author-approved R5 authority before
productive implementation begins.

## 6. Definition and evaluation failures

| Input/state | Required transformed state |
|---|---|
| Source temporarily undefined or unpublished | undefined output; no stale semantic publication |
| Source `EMPTY_DOMAIN` | `EMPTY_DOMAIN`; no fabricated branch or point |
| Undefined/nonfinite/3D vector | unsupported/undefined 2D transform state |
| Undefined/nonfinite angle | undefined transform state |
| Undefined, infinite or 3D center | undefined transform state |
| Undefined/nonfinite line or zero line normal | undefined axial-reflection state |
| Undefined/nonfinite scale factor | undefined dilation state |
| Finite `k=0` | **AUTHOR DECISION REQUIRED:** Option A valid source-domain-preserving `COLLAPSED_IMAGE`, with source-invalid addresses still invalid; or Option B explicit unsupported/undefined transformed state |
| Finite `k<0` | valid dilation with retained semantic traversal and `abs(k)` metric scale |
| Finite coefficients but mapped coordinate overflows | existing `NON_FINITE` status at that address; no stale coordinate |
| Periodic source | same provider canonicalization and one semantic seam |
| Disconnected source | same component partition; no bridging even if images coincide |
| Further transform after `k=0` | Option A: normal DAG output retaining collapsed-image semantics; Option B: no valid collapsed source until the factor returns to a supported value |

Recovery after a temporary failure uses normal DAG recomputation and semantic
snapshot publication.

## 7. Metric covariance

For every defined rich total or partial metric:

```text
Length(Translate(L))          = Length(L)
Length(Rotate(L))             = Length(L)
Length(Reflect(L))            = Length(L)
Length(Dilate(L,k))           = abs(k) Length(L)
```

The same equations hold between corresponding source/transformed semantic
addresses. The transformed locus itself is the rich metric source. A UI/scalar
multiplication is not implementation authority.

Under Option A, the established collapsed-image metric contract applies at
`k=0`: valid length zero with typed `COLLAPSED_IMAGE` evidence, not numeric
underflow. Under Option B, no metric value may be fabricated while the
transformed locus is unsupported/undefined.

## 8. Intersection covariance

Every R5 output participates in the existing rich Locus V2 intersection
pipeline and inherits its local-admissibility, global-completeness,
deterministic current-selector, continuity/topology-evidence, overlap and
exact-token rules. Current transformed-query semantic evidence is authoritative;
the movement history of transformation inputs or prior Cartesian root positions
cannot establish or continue identity. Equivalent final Construction states
and durable IDs reached through regular paths produce the same current token
binding and point definedness.

The transformed query uses the preserved domain/orientation as its semantic
phase frame and recomputes the R4 intrinsic phase/rank from its own current
roots, source-pair identity and topology context. Ambient reflection changes
world orientation, not that semantic frame. The transformed phase/rank is not a
copy or transformed value of the source query's selector, and no presentation
ordinal may substitute for it.

For a supported invertible `T` applied to both operands, the transformed and
untransformed intersection sets correspond geometrically within existing error
evidence. This relation never reuses durable identities, result identities or
exact tokens. Transformed queries create their own source-pair authority,
intrinsic phase/rank selector certificate and tokens. Neither geometric
covariance nor continuous motion permits reuse of an untransformed or
previous-frame selector/token certificate.

`k=0` is non-invertible and has no bijective-covariance claim. Under Option A,
coincidence of a target with the collapsed point remains an
overlap/non-isolated case unless the existing solver truthfully establishes a
stronger result. Under Option B, no intersection result may be fabricated from
an unsupported/undefined transformed locus.

## 9. Semantic Point-on-Locus covariance

For valid `(branch,u)`, `Point[T(L),branch,u]` geometrically equals applying `T`
to the source point at `(branch,u)`. Both constructed points and both loci keep
their distinct normal identities. Coordinates never replace the semantic
address.

## 10. Closure and updates

The output of every supported family is valid input to every supported family.
Composition remains a normal DAG chain. Changing source, vector, angle, center,
axis or factor updates the transformed locus and all downstream semantic
points, metrics, intersections and later transforms through ordinary
dependencies.

No hidden listener, polling, render cache or frontend calculation is an update
authority.

## 11. Style

The result receives ordinary host transformation style initialization and then
uses the existing R2 `GeoElement` style authority. Style has no effect on
durable identity, semantic revision, domain, branch topology, metric,
intersection or exact tokens. No parallel style model is permitted.

## 12. Commands, feature policy and tools

The existing `Translate`, `Rotate`, `Reflect`/`Mirror` and `Dilate` processors
remain the only public command authority. R5 shall not add Locus-specific
parallel commands.

Interactive creation requires the existing Locus V2 opt-in; no transformation
flag is added. File-loading preservation may reconstruct supported commands
through the existing preservation context. The Classic diagnostic route gains
no new interactive experimental creation authority.

The generic upstream transformation tool predicates are mutable-interface
contracts and shall not be broadened to include `GeoLocusV2`. Post-R5 G9U1 may
make the existing command-backed actions professionally discoverable. Any
separately approved legacy click-tool route must delegate to the same kernel
operation and own no semantics.

## 13. Persistence and copy

Save/reopen serializes the existing command inputs and normal durable identity
records. It shall preserve source dependency, transformation input geos, output
identity, style and downstream points/metrics/intersections with their own
transformed-query selector/token bindings.

Copy/remap and undo/redo use existing exact provenance rules. No render
vertices, point samples, detached matrix snapshot or Java callback is
serialized. Current ZIP/XML and `app="classic"` remain unchanged.

## 14. Forbidden implementations

R5 shall not:

- implement generic `Path` or mutable upstream transform interfaces on
  `GeoLocusV2`;
- transform render/metric samples as semantic geometry;
- create a static copy or frontend-only transformed drawable;
- add parallel Locus-specific transformation commands;
- infer identity from coordinate, label, order, index or proximity;
- reuse source locus IDs, source-query phase/rank certificates or intersection
  tokens;
- merge source branches/components merely because their transformed images
  coincide;
- serialize an evaluator closure or render tessellation; or
- execute G9U1, G9B, G9C, G9U2 or productive G10.

## 15. Validation and phase boundary

The future implementation shall satisfy the complete R5 validation matrix,
deterministic rerun, historical Locus V2/public/persistence/intersection gates,
legacy Locus, Checkstyle, Git diff checks and composed verification.

Productive implementation must stop before code if the author-approved R5
authority does not select exactly one `k=0` policy above. The validation matrix
then activates only the corresponding conditional zero-scale rows.

Automated validation can establish only an implementation candidate. R5 PASS
and author smoke acceptance require later author decisions. R5 closeout must
prepare or supersede, but not execute, the definitive post-R5 G9U1 prompt while
retaining its planned kernel-selector/exact-token-only candidate-marker hit
testing, explicit create-one/create-all and opt-in visible
frontend auto-materialization without UI/list/marker rank authority;
professional-menu/tool, visual-identity, existing-host `Continuity = OFF`
GeoCeDG product invariant with Classic
configurability, and two logical GeoCeDG branding-role contracts.

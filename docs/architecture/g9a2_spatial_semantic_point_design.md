# G9A2 spatial semantic core and projection-defined point design

**Status:** IMPLEMENTED — PASS — AUTHOR APPROVED

**Entry commit:** `5934d706fd9b30ea11b34d6ff0fe293e971cfc3f`

**Execution branch:** `feature/g9a2-spatial-semantic-point-pilot`

**Authority:** normative G9 spatial specification, Accepted ADR 0010/0011,
the author-approved spatial validation plan and the separately invoked G9A2
author authorization.

This document records the author-approved design-to-code boundary for G9A2.
The separate G9A2 closeout authorizes neither G9A3 nor any later G9 or
productive G10 phase.

## 1. Objective and boundary

G9A2 activates the smallest geometric consumer of the G9A1 durable identity
and XML substrate:

```text
projection-frame inputs
  + projection-system diagram maps and frame relations
  + defining ordinary 2D point geos
  + durable object/map/frame/binding identities
    -> one normal-DAG projection-defined point algorithm
      -> immutable system/object certificates
      -> current spatial point payload, only when valid
      -> one algorithm-owned derived GeoPoint3D-compatible output
```

Only `POINT` is an admitted productive spatial schema. Reusable core value
types may express frames, maps, relations, roles, revision tuples, status axes,
residual evidence and publication results, but they may not register another
primitive provider.

G9A2 excludes:

- spatial-defined authority or authority-mode transitions;
- line, segment, ray, vector, plane, circle, conic or spatial-curve schemas;
- composed objects, projective boundaries, surfaces or solids;
- automatic association or migration from labels, positions or visible
  coincidence;
- commands, localization, GUI tools or procedure workspaces;
- Locus V2, DXF, Python product or G10 study implementation; and
- bidirectional 3D editing or any view/camera reconstruction authority.

## 2. Existing substrate and minimum seams

G9A1 already owns opaque IDs, construction-scoped registration, inert
object/frame/system/map/relation/binding records, optional participating-geo
IDs, two-stage XML resolution, complete copy remapping, exact undo/reopen
restoration and explicit redefine transactions. G9A2 must extend those records
without creating a parallel identity table or load path.

The intended productive placement is additive source below
`org.geocedg.common.kernel.spatial`. Minimal host edits are allowed only where
the existing construction/XML lifecycle must instantiate or reconnect normal
DAG algorithms. Ordinary `GeoPoint` inputs and an algorithm-owned
`GeoPoint3D` output remain upstream kernel objects. No renderer, Euclidian view,
desktop or web class becomes a geometric dependency.

The durable registry resolves identity and typed references; it does not
schedule evaluation. `AlgoElement` and the construction update order remain the
only scheduler.

## 3. Frame and projection operator

A supported frame contains finite world-coordinate values

\[
R_i=(o_i,u_i,v_i,n_i,d_i),
\]

where `(u_i,v_i)` is an oriented independent in-plane basis, `n_i` is the
plane normal and `d_i` is a nonzero projection direction with
`n_i dot d_i != 0`. The G9A2 point provider admits orthographic frames, so
`d_i` is parallel to `n_i`. Other frame families are capability
`UNSUPPORTED`, certificate `NOT_EVALUATED`, with no payload.

The persisted G9A2 frame stores the origin, ordered `u`/`v` vectors,
handedness and orthographic family. For this admitted family only, the runtime
derives the oriented unit normal and projection direction deterministically
from that tuple; it does not infer them from a view or label. Oblique and
perspective direction policies remain unsupported.

For a spatial point `x`:

\[
y_i(x)=x+\frac{n_i\cdot(o_i-x)}{n_i\cdot d_i}d_i,
\qquad
q_i=\pi_i(x)=
\begin{bmatrix}
u_i\cdot(y_i-o_i)\\
v_i\cdot(y_i-o_i)
\end{bmatrix}.
\]

An intrinsic point `q=(q_u,q_v)` lifts to

\[
\ell_i(q)=o_i+q_u u_i+q_v v_i+\lambda d_i.
\]

Frame evaluation receives only construction-owned world coordinates and a
versioned numeric policy. Zoom, DPI, pixels, renderer and camera state are not
inputs and do not increment a semantic revision.

## 4. Projection systems and common-diagram maps

An admitted map is an oriented Euclidean isometry or declared unit similarity:

\[
p_m=\delta_m(q)=A_mq+b_m,
\quad A_m^TA_m=s_m^2I,
\quad s_m>0.
\]

Its inverse is validated before use. General affine/projective maps, singular
maps, zero scale and incompatible units never fall back to direct
interpretation of diagram coordinates.

Every frame used in one evaluated system subcontext must declare the same
world/source unit. A map inherits its source unit from its referenced frame;
its persisted `units` token is the target/common-diagram unit and must equal
the system unit. G9A2 performs no implicit unit conversion. The immutable
system certificate records each evaluated map's family, orientation, source
and diagram units, declared scale, semantic revision and evaluated status.

A bound ordinary 2D point stores common-diagram coordinates `p_m`.
Reconstruction must first compute

\[
q_i=\delta_m^{-1}(p_m).
\]

The map ID, system ID and frame ID must all match the binding context. A valid
frame with the wrong system or map is a broken semantic context, not an
intrinsic point.

### 4.1 Hinge and change-of-plane relations

For nonparallel planes with hinge `H_ij`, its intrinsic lines `h_i` and `h_j`
must map to the same oriented diagram line, with an explicit fold side:

\[
\delta_{m_i}(h_i)=\delta_{m_j}(h_j).
\]

Parallel planes cannot claim a finite hinge. An auxiliary change-of-plane
relation names its parent map, support/hinge inputs, orientation, provenance
and revision explicitly. Frame-use role remains independent of object binding
role.

One coherent diagram gauge `g` acts on all maps and bound points:

\[
\delta'_m=g\circ\delta_m,
\qquad p'_m=g(p_m).
\]

Because `{\delta'_m}^{-1}(p'_m)=\delta_m^{-1}(p_m)`, the system/object
certificate and spatial result must be gauge-invariant.

## 5. Independent system and object states

The implementation must retain separate axes:

| Axis | Required values |
|---|---|
| Capability | `SUPPORTED`, `UNSUPPORTED` |
| Projection system | `NOT_EVALUATED`, `CONSISTENT`, `INCONSISTENT`, `DEGENERATE`, `UNDEFINED` |
| Definition | `DEFINED`, `UNDEFINED`, `DEGENERATE` |
| Certificate | `NOT_EVALUATED`, `VALID`, `UNDERDETERMINED`, `AMBIGUOUS`, `INCONSISTENT_PROJECTIONS`, `DEGENERATE`, `UNDEFINED` |
| Currentness | `CURRENT`, `INVALIDATED`, `FAILED_CURRENT_REVISION` |
| Fidelity | `EXACT`, `NUMERICAL`, `DISCRETE` |
| Numeric guarantee | `NOT_APPLICABLE`, `CERTIFIED_BOUND`, `ESTIMATED_ERROR`, `UNRESOLVED` |
| Correspondence | `ESTABLISHED`, `AMBIGUOUS`, `BROKEN`, `NOT_REQUIRED` |

System inconsistency is evaluated before point reconstruction and is not
reported as object-level `INCONSISTENT_PROJECTIONS`. Unsupported capability
means `UNSUPPORTED` plus certificate `NOT_EVALUATED`, diagnostics and no
current payload.

## 6. Projection-defined point schema

Only bindings with role `DEFINING`, representation `POINT`, the admitted point
schema/version and authority `PROJECTION_DEFINED` may drive reconstruction.
Every binding is resolved through its durable IDs; label, coordinates,
construction order and Java reference identity are never association keys.
`AUXILIARY`, `ANALYSIS`, and `PRESENTATION` bindings may coexist on the object,
but their values and map/frame subgraphs do not enter the exact required
reconstruction subcontext. Their durable record identity and role remain
structural inputs so an explicit re-role is detectable. A mixed `DERIVED`
binding is rejected by the G9A2 projection-authority gate; spatial-defined
authority transitions remain outside this phase.

After validated map inversion, stack the two frame equations into

\[
Ax=b.
\]

The result policy is:

| Condition | Result |
|---|---|
| full rank, consistent, all per-binding residuals accepted | `VALID` with current point payload |
| rank below three with a nonempty solution family | `UNDERDETERMINED`, no payload |
| several discrete candidates under a future declared predicate | `AMBIGUOUS`, no arbitrary selection |
| full-rank/overdetermined observations fail residuals | `INCONSISTENT_PROJECTIONS`, no payload |
| required frame/representation loses a nondegeneration predicate | `DEGENERATE`, no payload |
| missing/undefined input or invalid arithmetic | `UNDEFINED`, no payload |

The linear G9A2 point schema has no discrete predicate that creates several
isolated candidates, so the validation case `A2-POINT-07` is explicitly
`NOT_APPLICABLE`; the generic state remains representable for later schemas.

A point on a projection plane is valid. A single view, repeated frames or
parallel equivalent frames are insufficient. More than two views report every
individual residual; aggregation may not hide a failed binding.

## 7. Reprojection and numeric policy

For each defining binding, publish both:

1. intrinsic evidence comparing `pi_i(xHat)` with
   `delta_m^-1(p_m)`; and
2. composed evidence comparing `delta_m(pi_i(xHat))` with `p_m`.

The initial implementation-candidate policy is versioned at
`geocedg/validation/spatial/g9a2/numeric-policy.json`. Its comparisons use a
world-coordinate characteristic scale. The persisted candidate values are
absolute tolerance `1e-10`, relative tolerance `1e-10`, rank-relative
tolerance `1e-12`, map tolerance `1e-10`, hinge tolerance `1e-10` and condition
limit `1e12`. The rank threshold is

\[
10^{-12}\max(\mathit{rows},\mathit{columns})
\max(1,|\sigma_{\max}|).
\]

Rank evidence is separate from residual evidence. No ULP-floor or other hidden
tolerance supplements this persisted policy. A binary64 calculation is
numerical even when its analytic inputs are exact, and the candidate claims
estimated—not certified—numeric error unless an exact path supplies separate
proof.

The independent analytic reference is generated with Python `Decimal` at 80
digits and imports no candidate implementation. It defines exact horizontal,
vertical and profile frames, a shared hinge, folded maps, general/on-plane/
underdetermined/inconsistent points, coherent gauge data and two sides of the
declared near-rank threshold.

## 8. Normal-DAG publication

One evaluation snapshots the complete current revision tuple:

```text
object and schema revision
authority mode
system revision
map and relation revisions
frame revisions
binding and projected-geo revisions
numeric-policy ID/version
```

The algorithm then validates the required system subcontext, reconstructs an
immutable candidate, evaluates every predicate and reprojection, confirms the
snapshot is still current and performs one atomic publication.

A source change first marks currentness invalid. A superseded candidate cannot
publish. Success exposes payload and certificate together. Failure publishes
the current failure axes and removes/undefines the current derived payload.
Historical payload may exist only as clearly noncurrent diagnostic evidence.
Downstream consumers receive one normal-DAG update per accepted publication and
cannot trigger reconstruction for the same revision.

## 9. Persistence and lifecycle increment

G9A2 persists the semantic inputs required to recompute supported frames,
maps, relations, point objects and bindings through the existing versioned
G9A1 spatial section. It does not make a cached certificate or derived point
coordinate authoritative.

The outer `geocedgSpatial` envelope remains version 1. Existing record-version
1 shapes remain byte-compatible and inert. Only the exact admitted
record-version 2 frame/system/map/relation/binding/POINT shapes activate this
pilot; unknown versions or attributes reject atomically. Hinge and
change-of-plane records persist their explicit support endpoints,
orientation and provenance, with fold sign present only for a hinge.

The focused gate covers:

- real host save/reopen of valid, inconsistent and underdetermined points;
- recomputation after load, ignoring any cached derived value as authority;
- undo/redo through valid/failure/recovery;
- rename without identity or certificate change;
- complete-copy remapping with fresh IDs and equal geometry; and
- legacy files remaining unassociated.

These are the baseline lifecycle cases explicitly required by G9A2. Partial
copy, add/remove/re-role transactions, hostile redefine combinations,
migration and broad compatibility hardening remain G9A3.

## 10. One-way derived 3D adapter

The current semantic payload updates one algorithm-owned ordinary 3D point.
The association uses `SpatialObjectId`, not its label. When the payload is not
current and valid, the derived point is undefined or withdrawn. Camera,
renderer, style and view membership may consume the derived geo but do not
feed the certificate.

Direct editing of that derived representation is rejected or left without
effect on defining projections. No authority-transition command exists in
G9A2. Standard host/UI editability predicates reject it. A low-level internal
`GeoPoint3D.setCoords` call can alter only the transient adapter until the next
normal-DAG publication; it cannot alter the durable object, bindings,
certificate or defining projections.

## 11. Deterministic instrumentation

The implementation exposes functional counts for frame/system/map/relation
evaluation, reconstruction, rank, reprojection, certificate publication,
failure publication and superseded-candidate rejection. Validation asserts:

- one current candidate publication at most per affected object/revision;
- system/map changes invalidate only referencing objects;
- adding downstream consumers adds no reconstruction for an existing
  revision;
- repeated runs produce the same normalized counters and certificates; and
- stale payload, mixed authority, hidden graph, label/proximity/order and all
  presentation-authority counters remain zero.

The executable independence matrix changes the supported headless seams for
2D zoom/pan, view size, pixel-ratio/DPI, 3D camera and renderer mode, labels,
layer, visibility, style and view membership one at a time. The headless
pixel-ratio method is a no-op and toolbar/workspace has no safe common-kernel
control; those surfaces are additionally covered by the absence of productive
view/profile dependencies and the hard-zero presentation counters.

No wall-clock threshold is normative in G9A2. Measured counts characterize
future ceilings without performing G16 software optimization.

## 12. Validation and author gate

The focused executable authority is
`tools/agent/verify-g9a2-spatial-point.ps1`. It validates authority hashes,
the exact baseline diff and upstream inventory, analytic references, canonical
models, machine evidence, focused JUnit results, Checkstyle, hard-zero guards
and whitespace. The final candidate must run it twice deterministically and
then run `tools/agent/verify.ps1` without `-SkipBuild`.

The executable checks first produced an implementation candidate. A separate
author closeout on 18 August 2026 reviewed that candidate and recorded:

```text
G9A2 = PASS — AUTHOR APPROVED
G9A3 AND LATER IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
G10 PRODUCTIVE IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

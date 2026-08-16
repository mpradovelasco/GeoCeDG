# G9 projection sufficiency and primitive schemas

**Status:** author-approved G9 architecture elaborating the normative spatial specification

**Scope:** canonical sufficiency, reconstruction, degeneration, and reprojection

**Implementation boundary:** point pilot in G9A2; other primitives remain G9B design

**G9P-R1 refinement:** bound common-diagram representations are now separated
from intrinsic frame coordinates by an explicit projection-system diagram map.
This refinement is author approved; the governing normative contract is
`geocedg/specs/spatial/g9-spatial-projection-semantics.md`.

## 1. Why sufficiency is type-specific

“Two orthographic views” is a drafting convention, not a general theorem. A
view may collapse a direction, repeat another constraint, omit orientation or
endpoint correspondence, or admit several spatial objects of the declared
type. GeoCeDG therefore asks whether a particular typed projection map is
injective on a declared admissible domain and whether a constructive inverse
can be represented in the dependency graph.

For type `T`, configuration space `X_T`, and frames `Pi`, define

\[
\Phi_{T,\Pi}:X_T\longrightarrow Y_1\times\cdots\times Y_k,
\qquad
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

A canonical schema declares:

- admissible domain and allowed equivalences;
- representation expected in each binding;
- explicit correspondence and orientation data;
- rank/non-degeneration and type-consistency predicates;
- a constructive inverse or a finite candidate policy;
- reprojection residual and tolerance definitions; and
- deterministic failure classification.

The schema is sufficient only when `Phi` is injective modulo its declared
equivalence and the inverse/reprojection tests succeed. The certificate binds
that conclusion to concrete object, frame, binding, schema, and policy
revisions.

The `Y_i` above are intrinsic frame representation spaces, not screen space and
not automatically the coordinates of ordinary 2D geos in a folded CeDG
diagram. A valid `ProjectionSystem` supplies typed bijective diagram maps

\[
\delta_m:Y_i\longrightarrow Y_{CeDG}
\]

and the observable composition

\[
\Psi_{T,S}(x)=
\left(\delta_m^{(T)}(\pi_i(x))\right)_{m\in S}.
\]

The diagram composition does not replace intrinsic sufficiency. When each
required `delta_m` is fixed, valid and bijective, `Psi` is injective exactly
when `Phi` is injective. If a required map or frame relation is invalid, no
arrangement of visible objects may be used as a substitute.

## 2. Projection-frame and diagram-system algebra

For frame `i`, let `o_i` lie on its plane, `u_i` and `v_i` be an oriented
orthonormal in-plane basis, `n_i=u_i cross v_i`, and `d_i` be a parallel
projection direction with `n_i dot d_i != 0`. Define the plane projector

\[
H_i=I-\frac{d_i n_i^T}{n_i^T d_i}.
\]

The projected plane point and its two coordinates are

\[
y_i=o_i+H_i(x-o_i),
\qquad
q_i=B_i^T(y_i-o_i),
\qquad B_i=[u_i\ v_i].
\]

Equivalently, each observed coordinate supplies two linear equations

\[
B_i^T H_i x = q_i + B_i^T H_i o_i.
\]

The lifted point set is

\[
\ell_i(q_i)=o_i+B_iq_i+\lambda d_i.
\]

Orthographic projection declares `d_i` parallel to `n_i`. Oblique parallel
frames may reuse the same algebra if explicitly supported. Perspective camera
projection is outside the initial schema even if the 3D renderer displays it.

Frame validity includes finite components, nonzero direction, nonparallel
direction/plane, independent in-plane basis, declared units, and stable
orientation. Invalid frames produce `UNDEFINED` or `DEGENERATE` before object
reconstruction.

Let the intrinsic plane embedding be

\[
\iota_i(q)=o_i+B_iq.
\]

A diagram-map record `m` in a projection system has

\[
p_m=\delta_m(q_i)=A_mq_i+b_m,
\qquad \det A_m\ne0.
\]

The initial orthographic CeDG map family satisfies

\[
A_m^TA_m=s_m^2I,
\qquad s_m>0,
\]

where `s_m` is only a declared unit conversion and the determinant sign records
orientation/reflection. General affine or projective maps remain unsupported
until a type-specific versioned contract admits them.

For a common-diagram point observation `p_m`, every canonical schema first
computes

\[
q_i=\delta_m^{-1}(p_m).
\]

Lines, conics and parameterized curves use the corresponding typed induced
map; they are not converted by sampling or screen coordinates.

For nonparallel frame planes with declared hinge
`H_ij = P_i intersection P_j`, define intrinsic hinge lines
`h_i=iota_i^{-1}(H_ij)` and
`h_j=iota_j^{-1}(H_ij)`. A hinge-unfold relation requires

\[
\delta_{m_i}(h_i)=\delta_{m_j}(h_j)
\]

as one diagram line with explicit orientation and fold side. A parallel-frame
system can be valid for reconstruction while being ineligible for a procedure
that requires such a hinge.

One coherent common-diagram gauge `g` gives

\[
\delta'_m=g\circ\delta_m,
\qquad p'_m=g(p_m),
\qquad {\delta'_m}^{-1}(p'_m)=\delta_m^{-1}(p_m).
\]

Thus intrinsic certificates are gauge-invariant. GUI pan, zoom, DPI, canvas and
camera transforms are not even diagram gauges: they act only after `p_m` is
constructed and never change a semantic revision.

## 3. Numeric and exactness policy

An exact construction may admit symbolic rank/incidence proofs. A `double`
evaluation of an analytic formula is still numerical. Every numerical schema
normalizes distances using a declared world-coordinate scale `S`, for example

\[
\rho = \frac{r}{\tau_{abs}+\tau_{rel}S}.
\]

Acceptance requires `rho <= 1` plus every independent rank, domain, type, and
correspondence predicate. A small residual alone cannot distinguish a unique
solution from an ill-conditioned family.

Certificates report singular values or equivalent rank evidence, condition
diagnostics, residuals by binding, arithmetic method, and whether the error is
certified or estimated. Tolerance scaling cannot use zoom, DPI, line thickness,
or screen pixels.

For diagram bindings, record both the intrinsic residual against
`delta_m^{-1}(p_m)` and the composed residual of
`delta_m(pi_i(xHat))` against `p_m`. The former is the sufficiency authority;
the latter proves that the diagram composition and binding were honored. A
small diagram residual does not repair an inconsistent or degenerate system.

## 4. Point schema

### 4.1 Definition

For projected common-diagram points `p_m`, obtain
`q_i=delta_m^{-1}(p_m)` from the validated required system context, then stack
the two-equation systems into

\[
A x=b.
\]

A point is uniquely reconstructible when:

1. every binding is a valid point representation in a valid frame;
2. `rank(A)=3` under the approved rank policy;
3. the exact system is consistent or the numerical solution satisfies every
   normalized residual; and
4. no schema-declared discrete equivalence remains unresolved.

With two orthographic frames, the equivalent geometric test is that the lift
lines are nonparallel and intersect consistently. More than two bindings can
overdetermine the point and improve inconsistency detection; they do not relax
the rank test.

### 4.2 Results

| Condition | Certificate result |
|---|---|
| Unknown or unimplemented semantic family/schema/provider/frame/correspondence version | capability `UNSUPPORTED`; certificate `NOT_EVALUATED`; no payload |
| Full rank, consistent, all residuals accepted | `VALID` |
| Rank below three with a nonempty solution family | `UNDERDETERMINED` |
| Several discrete candidates survive declared predicates | `AMBIGUOUS` |
| Full-rank/overdetermined observations fail consistency | `INCONSISTENT_PROJECTIONS` |
| Frame or representation loses a required rank/non-degeneration predicate at a singular position | `DEGENERATE` |
| Missing/undefined source or invalid arithmetic | `UNDEFINED` |

A point lying on a projection plane is not inherently degenerate. Two equal or
parallel frames usually are insufficient. A general point, a plane-contained
point, and a dynamic crossing of a projection plane are mandatory G9A2 cases.

An unsupported map family yields capability `UNSUPPORTED` and no object
payload. A noninvertible map or inconsistent required hinge is reported by the
projection-system state and prevents point evaluation; it is not mislabeled as
an inconsistency among otherwise valid point observations.

### 4.3 Reprojection

Reproject `x_hat` in every defining frame, compare the intrinsic result with
`delta_m^{-1}(p_m)`, then apply `delta_m` and compare with the bound 2D point.
Record both per-map vector residuals, normalized norms, maximum, and aggregate
method. Do not average away one failed binding.

## 5. Line schema

### 5.1 Lifted-plane construction

Let a projected 2D line in frame coordinates be

\[
a_i q_u+b_i q_v+c_i=0.
\]

Its spatial preimage is a plane `Lambda_i` containing all projection rays
through that line. In homogeneous form it is obtained by composing the 2D line
covector with the frame projection. Two independent lifted planes intersect in
a spatial line:

\[
L=\Lambda_1\cap\Lambda_2.
\]

When the bound line is a common-diagram covector `l_p` and homogeneous
`p=D_m q`, its intrinsic covector is `l_q=D_m^T l_p`. The map and its units must
be valid before lifted-plane predicates run.

Uniqueness requires independent plane normals and consistent incidence. Three
or more views are checked against the same line.

### 5.2 Collapsed projection

For spatial line `x(t)=p+t w`, its projected direction is

\[
\bar w_i=B_i^T H_i w.
\]

When `bar w_i=0`, the line projects to a point. That binding is a legitimate
collapsed representation, not a 2D line. It can constrain the spatial line to
a projection ray but supplies no projected direction. Another non-collapsed
view or equivalent defining primitive is mandatory.

### 5.3 Required predicates

- each binding representation matches collapsed/non-collapsed reality;
- lifted planes/rays have sufficient independent rank;
- all definitions share one spatial support line;
- orientation is supplied by corresponding ordered points, an explicit
  direction, or a common parameter if orientation matters;
- coincident/projectively identical constraints do not count twice; and
- reprojection returns the correct line or declared collapsed point in each
  frame.

Parallel/coincident lift planes yield underdetermination; inconsistent planes
yield inconsistency; a zero direction or unstable collapse transition is
degenerate. A line label or two visually aligned screen segments is irrelevant.

## 6. Segment schema

A segment is `(L,A,B)` with `A != B`, ordered endpoint identities, and finite
parameter interval `[0,1]`:

\[
S(t)=A+t(B-A),\qquad 0\leq t\leq1.
\]

Required evidence:

1. a valid supporting-line certificate;
2. point certificates for `A` and `B`, or an equivalent common-parameter
   construction;
3. explicit endpoint correspondence across projections;
4. consistent endpoint order/orientation when declared; and
5. finite, nonzero spatial length unless a point degeneration is explicitly
   allowed.

Swapping endpoints in only one projection is a correspondence conflict, not a
harmless label change. A segment clipped by the viewport does not acquire new
endpoints. Projection collapse to one point is valid only if other defining
data still reconstruct both endpoints.

## 7. Ray schema

A ray is `(O,w)` with origin `O` and nonzero oriented direction `w`:

\[
R(t)=O+t w,\qquad t\geq0.
\]

It requires a valid origin point, supporting line/direction, and consistent
orientation. A projected ray may collapse to a point or may appear as a full
line under a degenerate representation; the binding kind must record what was
actually projected. An unoriented line plus an origin admits two rays and is
`AMBIGUOUS` unless another predicate selects one.

## 8. Vector schema

A free vector is an equivalence class under translation and is reconstructed
from projected direction/magnitude components:

\[
\bar w_i=B_i^T H_i w.
\]

Stacking sufficient independent frames gives a linear system for `w`. A bound
vector additionally references an origin point but keeps vector identity
distinct from the supporting segment. Required predicates include full rank,
consistent magnitude and orientation, and a policy for the zero vector. A 2D
line without direction/magnitude metadata is not a vector binding.

## 9. Plane schema

### 9.1 Constructive definitions

Approved definition families may include:

- three point objects `A,B,C` with
  `(B-A) cross (C-A) != 0`;
- two distinct incident lines with independent directions;
- a point and a nonzero normal vector; or
- versioned projection traces in known frames when a trace-specific schema
  proves their incidence and uniqueness.

The plane may be represented by

\[
n\cdot(x-p)=0,\qquad \|n\|>0,
\]

with an orientation equivalence `n ~ alpha n` declared explicitly. Three
non-collinear reconstructed points can build it constructively in the normal
DAG. Two incident lines must actually intersect and not be coincident.

### 9.2 Degeneracies

- collinear or coincident defining points;
- coincident lines or incompatible skew lines;
- zero/undefined normal;
- trace data corresponding to no one plane;
- a projecting plane whose one trace representation collapses; and
- orientation reversal when oriented-face semantics require continuity.

A filled projected polygon, silhouette, or visible “plane patch” is
presentation data and cannot define the infinite plane.

## 10. Circle schema

A spatial circle is

\[
C(\theta)=c+r(e_1\cos\theta+e_2\sin\theta),
\quad r>0,
\]

where `(e_1,e_2)` is an oriented orthonormal basis of a valid support plane.
Canonical constructive families may use:

- support plane + center + positive radius;
- support plane + center + one incident point; or
- three non-collinear spatial points with an orientation policy.

Projected conics can participate only with explicit correspondence and a
schema proving the support plane and intrinsic circle constraints. Two ellipse
shapes are not presumed sufficient: different spatial circles can share
silhouette-like observations without the required frame/correspondence data.

Required checks include support-plane validity, center incidence, positive
radius, circular intrinsic quadratic form, orientation/seam policy, and
reprojection to each conic or declared edge-on segment/point. Zero radius,
collinear points, incompatible centers/radii, and a type-changing conic are
explicit failures.

## 11. Conic schema

A nondegenerate spatial conic is a planar curve with support-plane coordinates
`z=(s,t,1)` and intrinsic quadratic form

\[
z^T Q z=0,
\]

plus conic type, real-domain/branch information, orientation or parameter
policy, and constructive provenance. `Q` is defined up to a declared nonzero
scale; its rank, signature, and real locus classify degeneration and type.

If a frame projection restricts to an invertible plane homography `H_i`, the
projected form satisfies, up to scale,

\[
Q_i=H_i^{-T} Q H_i^{-1}.
\]

Thus a schema may reconstruct/verify `Q` only after the support plane and
homographies are known and nondegenerate. Alternative schemas may reconstruct
defining foci, axes, directrices, or five corresponding points. Each schema is
versioned; data from different schemas are not casually mixed.

Required failures include rank-deficient conic, imaginary/empty real locus,
ellipse/parabola/hyperbola type inconsistency, lost hyperbola-branch
correspondence, singular plane homography, and projection collapse. A conic
outline without point/parameter correspondence is not a spatial identity.

If the bound conic is expressed in common-diagram homogeneous coordinates with
form `Q_p`, its intrinsic form is

\[
Q_q=D_m^TQ_pD_m.
\]

This typed transform is exact only to the declared map representation; screen
tessellation is never a conic binding.

## 12. Spatial-curve schema

### 12.1 Semantic definition

Let

\[
D=\bigcup_j I_j,
\qquad
C_j:I_j\setminus E_j\rightarrow\mathbb{R}^3.
\]

Each branch/component has a provider-owned stable key, orientation, parameter
domain, invalid gaps, and a deterministic evaluator. A defining projected
curve in frame `i` must expose

\[
c_{i,j}(t)=\pi_i(C_j(t))
\]

using the same semantic `t`, or an explicit invertible correspondence
`t_i=psi_{i,j}(t)` with validity proof. Locus V2 can provide such evidence when
its definitions actually share a driver/domain parameter. A sample index,
nearest point, creation order, or equal coordinate is not correspondence.

For a common-diagram curve `p_m(t)`, the intrinsic curve supplied to this
schema is `c_i(t)=delta_m^{-1}(p_m(t))` with the same branch/component and
parameter identity. The map may not deduplicate repeated coordinates or alter
periodic seam identity.

### 12.2 Per-parameter reconstruction

For each admissible `t`, lift the bound projected points and apply the point
schema. Curve validity additionally requires:

- a coherent branch/component topology over declared intervals;
- deterministic evaluation and continuity/regularity claims only where proved;
- consistent seams for periodic domains;
- explicit behavior at invalid gaps and unbounded endpoints;
- no coordinate deduplication at self-intersections or retracing; and
- stable identity/lineage rules across topology-preserving revisions.

Finite sampling may isolate validation candidates but cannot prove the entire
continuum without interval, analytic, or explicitly limited guarantee evidence.
The certificate must state whether reprojection consistency is certified,
estimated, or unresolved over each component.

### 12.3 Failure distinctions

- no correspondence: `UNDERDETERMINED` or `AMBIGUOUS` according to the
  candidate space;
- inconsistent lifts at some `t`: `INCONSISTENT_PROJECTIONS` with interval and
  binding diagnostics;
- branch key or mapping lost: correspondence `BROKEN`, without coordinate
  retargeting;
- source gap or evaluator failure: definition `UNDEFINED` on that component;
- tangent/rank singularity: `DEGENERATE` at the event, followed by explicit
  recovery if possible.

## 13. Reprojection obligations by type

| Type | Required comparison |
|---|---|
| Point | per-frame intrinsic coordinate vector plus composed common-diagram residual |
| Line | typed diagram-covector conversion, normalized intrinsic incidence/direction, composed diagram comparison; declared collapsed-point comparison |
| Segment | support line plus corresponding endpoints and interval orientation |
| Ray | support line, origin, and positive direction |
| Vector | projected direction/magnitude components; origin if bound |
| Plane | defining point/line/normal/trace incidences, not a screen polygon |
| Circle | conic/edge-on representation, center/support/radius invariants, orientation if declared |
| Conic | typed diagram/intrinsic quadratic-form conversion or constructive-primitive equivalence plus branch correspondence |
| Spatial curve | branch/component/common-parameter comparison in intrinsic and diagram spaces over declared valid domains with guarantee by component |

Every comparison names the frame and binding revisions. Aggregate summaries do
not replace the individual failures.

## 14. Dynamic validation traces

The minimum deterministic traces are:

1. a two-frame dihedral system performs `q -> p -> q`, validates its oriented
   hinge, and remains equivalent under a coherent common-diagram gauge;
2. a diagram map becomes noninvertible, a hinge becomes inconsistent, and both
   recover without stale object payload;
3. an auxiliary change-of-plane frame/map is added, reoriented, removed, and
   restored through the normal DAG;
4. point frames pass from independent to rank-deficient and back;
5. a line becomes perpendicular to each projection plane in turn;
6. a segment endpoint pair coincides, crosses, and recovers;
7. a ray orientation becomes ambiguous then receives a defining direction;
8. a vector passes through zero under its declared policy;
9. three plane points become collinear and recover;
10. a circle becomes edge-on in one view and later has zero radius;
11. a conic crosses a type/degeneration boundary with explicit classification;
12. a spatial curve creates/loses a component, crosses a periodic seam, and has
    repeated coordinates with distinct semantic preimages; and
13. one defining projection becomes inconsistent while labels, layers, zoom,
    and viewport are independently varied.

The object IDs persist through geometric invalidity. Current payloads do not.
Only topology/correspondence evidence—not visual nearness—may continue branch
identity.

## 15. Implementation promotion boundary

G9A2 implements only the point schema and generic records needed to prove it.
The equations for other primitives are design requirements for G9B author
review, not permission to implement a generic solver. Each G9B family needs an
approved schema, analytic fixtures, dynamic degeneracies, XML/lifecycle tests,
and deterministic reprojection evidence before promotion.

G9B has hard semantic dependency on G9A3, not on G9U1 or another GUI client.
Its schemas consume validated projection-system context from the shared kernel.

Surfaces, quadrics, silhouette inversion, surface intersections, solids,
projective boundary topology, and bidirectional 3D editing remain outside this
primitive document.

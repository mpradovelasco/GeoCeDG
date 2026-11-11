# GeoCeDG mathematical reference

- Explanatory reference for the current product and for the approved G9 spatial design.
- Normative authority is in `geocedg/specs/`.
- For supported construction, measurement and intersection workflows, see the
  [GeoCeDG user guides](geocedg_user_guide.md).

## 1. Constructive descriptive geometry

GeoCeDG treats a result as the output of an explicit geometric construction.
Objects retain dependencies, parameters, validity domains and failure states.
The same visible curve can have different constructive identities when it is
produced by different branches or procedures. This is why GeoCeDG is not a
solid-first CAD feature tree.

## 2. Locus V2

Let the oriented driver domain be a union of semantic branches and valid
components,

\[
\Omega=\bigcup_j\bigcup_k I_{jk}.
\]

For each branch, a deterministic construction evaluator gives

\[
F_j:I_{jk}\rightarrow\mathbb{R}^2.
\]

The locus image is the union of those images. Branch key, component, parameter,
orientation, provenance and revision remain part of the semantic address.
Invalid-domain gaps are not joined. A render tessellation is derived display
data and is never the authority for length or intersection.

The governing contracts are the [semantic model](../../geocedg/specs/locus/locus-v2-semantics.md)
and the [public surface](../../geocedg/specs/locus/locus-v2-public-surface.md).
The experimental Locus V2 and Spline V2 construction surface is enabled by
default in GeoCeDG. Supported objects persist in `.cedg` documents. Generic
path operations apply only where the public surface explicitly supports them.

## 3. Length and total variation

For an absolutely continuous branch component, the length on the interval
from \(a\) to \(b\), with \(a\le b\), is total variation,

\[
s(a,b)=\int_a^b\lVert F'(t)\rVert\,dt.
\]

Lengths are non-negative in both traversal directions. When an analytic
integral is unavailable, the [metric contract](../../geocedg/specs/locus/locus-v2-metrics.md)
defines tolerance- and work-bounded world-coordinate methods and their
guarantees. Endpoints must identify positions on the construction. Coincident
coordinates alone do not establish a position.

`LocusLength` exposes the value, guarantee, coverage and diagnostics for the
declared domain or for a route between two positions. Scalar `Length` returns
a number only when the metric is admissible for numeric use. The route and
point inputs persist with the construction and the value is recomputed.

## 4. Intersections

For an implicit target `G(x,y)=0`, one branch solves

\[
G(F_j(t))=0.
\]

For a parametric target `Q(u)`, it solves

\[
F_j(t)=Q(u).
\]

The solver must isolate candidates, refine in original parameters, verify
residuals and retain semantic identities. A sign change alone misses even-order
tangency. Equal coordinates do not merge distinct constructive preimages.

Completeness is a property of the complete result set. Individual admissibility
is a property of a particular isolated solution. G8 Option B permits a
token-selected point when that solution is locally established even if global
completeness is weaker. Overlap, ambiguity, unsupported target and work limit
remain typed outcomes.

The public `Intersect` overloads return a persistent rich result for supported
objects and curve pairs. An individually eligible solution can supply a
dependent point. The [intersection contract](../../geocedg/specs/locus/locus-v2-intersections.md)
and its amendments define target families and when an intersection can supply
a point.

## 5. Exact and approximate representations

Exact means that an approved representation preserves the resolved geometric
object without deliberate discretization; it does not automatically mean
symbolic arithmetic. Approximate results require a method, world-coordinate
tolerance or error evidence, work limit and explicit status. Render samples,
screen pixels, zoom and DPI never become semantic or metric truth.

## 6. Projection frames and spatial reconstruction

This section explains the **normative, author-approved G9 design**. It is not an
implemented capability.

A projection frame is a geometric plane/frame and operator independent of the
viewport. A projection system is the approved durable geometric arrangement of
several such frames in one common CeDG construction diagram. A projection
binding associates a stable spatial object with a typed 2D representation,
system map, frame, and role such as defining, derived or auxiliary.

The intrinsic and diagram coordinates are different semantic spaces:

\[
x\in\mathbb{R}^3
\xrightarrow{\pi_i}
q_i\in\mathbb{R}^2_i
\xrightarrow{\delta_m}
p_m\in\mathbb{R}^2_{\mathrm{CeDG}}.
\]

Here `pi_i` projects into the intrinsic basis of frame `i`. The geometric map

\[
\delta_m(q)=A_mq+b_m,
\qquad \det A_m\ne0,
\]

unfolds or places that frame in the common construction diagram. The initial
approved map family is an oriented Euclidean isometry or a declared unit
similarity. It can record line-of-ground/hinge agreement, orientation, fold
side, and an auxiliary change of projection plane. It is not zoom, pan, DPI,
camera, canvas, or physical-sheet state.

For type `T` and projections `\Pi`, define

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

A projection set is sufficient only on a declared domain when projected
equality implies the permitted spatial equivalence, constructive reconstruction
exists, reprojection agrees under the declared tolerance, and type-specific
correspondence/non-degeneration predicates hold. A count of views alone is not
sufficiency.

Ordinary diagram objects supply `p_m`; reconstruction first computes

\[
q_i=\delta_m^{-1}(p_m).
\]

The intrinsic sufficiency map remains authoritative. Its typed visible
composition is

\[
\Psi_{T,S}(x)=
\left(\delta_m^{(T)}(\pi_i(x))\right)_{m\in S}.
\]

For fixed valid bijective maps, `Psi` and `Phi` have the same injectivity. A
coherent global diagram-coordinate change `g` applied to every map and bound
diagram object satisfies

\[
\delta'_m=g\circ\delta_m,
\quad p'_m=g(p_m),
\quad {\delta'_m}^{-1}(p'_m)=\delta_m^{-1}(p_m),
\]

so it cannot change spatial sufficiency. Moving only the GUI viewport does not
even change `p_m` and causes no semantic revision.

For a declared hinge between two projection planes, the intrinsic hinge line
from both frames must map to the same oriented diagram line. A visible
coincidence is not enough; the system stores typed identities, construction
dependencies, orientation, and revision evidence.

Ambiguity means multiple admissible reconstructions. Degeneracy means a
type-specific singular configuration, such as a line collapsing to a point in
one view. Inconsistency means supplied projections fail correspondence or
reprojection. An undefined or failed certificate must not retain stale spatial
geometry.

Projection-system inconsistency is reported separately from inconsistent
object projections. A broken or noninvertible required map prevents canonical
evaluation; it cannot be repaired from labels or screen proximity.

## 7. Reading normative material

- Locus semantics: `geocedg/specs/locus/locus-v2-semantics.md`
- Locus metrics: `geocedg/specs/locus/locus-v2-metrics.md`
- Locus intersections: `geocedg/specs/locus/locus-v2-intersections.md`
- Extended intersections: `geocedg/specs/locus/locus-v2-extended-intersections.md`
- Current DXF: `geocedg/specs/export/geometry-export-foundation.md`

If this explanation and a normative accepted specification disagree, the
specification governs.

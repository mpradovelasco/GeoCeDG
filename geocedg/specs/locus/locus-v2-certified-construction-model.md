# Locus V2 certified construction interval model

| Field | Value |
|---|---|
| Version | `1.4` (`certified-construction-program/v1`, `/v2` and `/v3`; §12 only under `constructive-authority/v1`) |
| Phase | `PRE-G9B-R2-E0/E1`; amended by `PRE-G9B-R3-X1` (§2, §3, §5.1, §5.2, §8, §9), by `PRE-G9B-CU2` (§10) and by `PRE-G9B-CU3` (§11, and the constructive authority amendment of §12) |
| Decision | [ADR 0028](../../../docs/adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md), including its `PRE-G9B-R3-X1` amendment; [ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) for class v3 (§10), accepted as architecture on 2026-10-21; [ADR 0039](../../../docs/adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) for exact evaluation (§11), accepted as architecture on 2026-10-24; [ADR 0040](../../../docs/adr/0040-constructive-geometric-authority-and-relation-integrity.md) for the constructive authority amendment (§12), accepted as architecture on 2026-10-25 |
| v1.2 status | **PRE-G9B-CU2 AMENDMENT (§10, WITH §10.10 FROM AUTHOR DECISION V-CON-6 OF 2026-10-22 AND THE EDITORIAL PRECISION OF §10.3 FROM AUTHOR DECISION C-8 OF 2026-10-23) — ARCHITECTURE AUTHOR APPROVED (2026-10-21); PASS — AUTHOR APPROVED WITH THE CU2 CLOSEOUT (2026-10-24)** |
| v1.3 status | **PRE-G9B-CU3 AMENDMENT (§11, WITH THE AUTHORITY OF THE TARGET IN §11.3 AND THE CONFORMANCE CONDITIONS OF EXACT EVALUATION IN §11.4) — DESIGN AUTHOR APPROVED (2026-10-24); IMPLEMENTATION AUTHORIZED SUBJECT TO GATE G-M3, NOT YET APPROVED AS PRODUCT** |
| v1.4 status | **CONSTRUCTIVE AUTHORITY AMENDMENT (§12, TEXTS AUT-4 AND AUT-7) — AUTHOR APPROVED AS CONTRACT (2026-10-25); IMPLEMENTATION AUTHORIZED IN INCREMENTS E1 TO E4, NOT YET APPROVED AS PRODUCT** ([approval record](../../../docs/validation/pre_g9b_cu3_constructive_authority_approval_record.md)) |
| Consumer | [pair materialization](../curves/spline-v2-pair-materialization.md) §10 |
| Parents | [Locus V2 semantics](locus-v2-semantics.md), [existence and components](locus-v2-existence-components.md), [public surface](locus-v2-public-surface.md) |

This contract was introduced by the `PRE-G9B-R2-E0/E1` technical candidate and
amended by the `PRE-G9B-R3-X1` technical candidate. Its author-approval status
is recorded only in those phases' author-decision records, never here. The X1
design record is
[`pre_g9b_r3_x1_certified_expression_point_design.md`](../../../docs/architecture/pre_g9b_r3_x1_certified_expression_point_design.md).

## 1. Purpose

A reconstructible dependent-point Locus V2 evaluates its curve by replaying an
isolated copy of its generator slice in binary64. That replay has no enclosure
semantics, so it cannot certify an intersection root. This contract defines the
minimum additional capability: an immutable **construction program** captured
from the evaluator's own isolated slice and interpreted with outward-rounded
intervals and first derivatives, so that the unchanged pair certifier can prove
root existence, uniqueness and germ-class coverage.

It defines no new selector, token, ledger field, command, persistence or
frontend behaviour.

## 2. Authority

The **semantic curve** of a modeled branch is the exact-real evaluation of the
evaluator's own GeoGebra formula sequence (§4, §5) on the constants captured
from its isolated slice, with each algorithm's branch predicate evaluated on
exact values. This is the construction analogue of the approved "captured
represented spline authority, not exact interpolation":

- captured binary64 inputs are exact rational constants;
- every rational operation that involves the driver parameter is exact;
- a constant transcendental subexpression (a rotation cosine, a mirror angle)
  is represented by the binary64 value the evaluator itself computes with the
  same function;
- transcendental functions of the driver parameter (`cos t`, `sin t`) are exact
  and are enclosed rigorously (§6).

The floating evaluator approximates this function. A certified root is published
only after the existing floating verification also accepts it at the certified
parameters: both sides valid, residual within the pair residual tolerance,
regular differentials and a transverse contact. A disagreement refuses that
class; it never alters the certificate.

The floating evaluator this verification relies on meets two obligations:

- it applies a path driver's parameter exactly as the normal `AlgoPointOnPath`
  update does, `pathChanged` followed by `updateCoords`, so the driver's
  homogeneous and inhomogeneous coordinates both belong to the requested
  parameter;
- it reconstructs its isolated slice from an element set ordered by construction
  index, then creation id, a strict total order, so every construction element of
  the slice is written and replayed exactly once and the isolated dependent point
  depends on the isolated driver.

Two pre-existing evaluator defects violated these obligations until
`PRE-G9B-R3-X1` corrected the evaluator: segment drivers evaluated the previous
parameter when the slice read their inhomogeneous coordinates
(`TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE`), and slices with inline literal
arguments in two commands lost their driver
(`TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY`). The certification rules were
not changed to accommodate either defect; while they existed the verification
refused the first and rule 5 of §3 refused the second. The X1 design record gives
their mechanisms.

A parameter box on which any predicate of §4 or §5 is not uniformly decided, on
which a divisor may vanish, or on which an enclosure is not finite, is
**unresolved**. Unresolved boxes are never excluded and never certified.

## 3. Capture

The evaluator captures the program under its own lock, after its ordinary
external-state reset, from its isolated objects only. Live construction objects,
labels, rendering, samples, coordinates of the driver point and construction
order are never inputs.

A geo is **driver-dependent** when it is the isolated generator state or a
descendant of it. The walk starts at the isolated dependent point:

1. the state is driver node `0` (§4);
2. a geo that is not driver-dependent is a **constant** and is captured by its
   stored binary64 fields at the maximal driver-independent boundary: a point by
   its normalized homogeneous `(x,y,z)` and inhomogeneous `(X,Y)`; a line by
   `(a,b,c)`; a vector by `(x,y)`; a number by its value. Constant-only
   algorithms are never re-evaluated;
3. a driver-dependent geo must be the single output of an algorithm of §5 whose
   exact class is listed there, or an expression-point translation of §5.2; its
   inputs are captured recursively;
4. every constant must be defined and finite; a constant point must be finite
   (not infinite); a constant line must be defined (§5.1); a constant meet
   operand must be an unbounded line (`GeoLine` or `GeoAxis`);
5. the dependent point must be driver-dependent;
6. at most 64 steps are captured.

Any other shape yields **no program**. The side then has no certified model and
its pairs stay rich-only with an explicit diagnostic.

## 4. Drivers

`t` is the canonical driver parameter the evaluator applies.

| Family | Captured | Driver point `(x,y,1)` | Domain |
|---|---|---|---|
| `CIRCLE_POINT` | complete circle (`GeoConic`, type circle, not a part): half-axes `h0,h1`, eigenvectors `e0,e1`, midpoint `m` (must equal the translation vector bit-for-bit); state path type circle; path parameters in use | `x = h0 cos t e0x + h1 sin t e1x + mx`, `y = h0 cos t e0y + h1 sin t e1y + my` | `[-Math.PI, Math.PI)` |
| `CIRCULAR_ARC_POINT` | circle arc (`GeoConicPart` arc of a circle): as above plus `paramStart s`, `paramExtent w`, orientation | `θ = s + τ w`, `τ = t` (positive) or `1 - t`; point as for the circle with `θ` | `[0,1]` |
| `SEGMENT_POINT` | segment with length `> 0`: start inhomogeneous `(sx,sy)`, line coefficients `(a,b)` | `x = sx + t b`, `y = sy - t a` | `[0,1]` |
| `SCALAR_STATE` | only with the existing direct scalar affine certificate `(αx,βx,αy,βy)` | output `(αx t + βx, αy t + βy)`; no steps | declared domain |

`LOCUS_BRANCH_POINT` and scalar states without that certificate have no program. A
`LOCUS_BRANCH_POINT` dependent may still compose the certificate of its support under §10.10,
which is not a program.
The circle-point formula is exactly `GeoConicND.pathChangedWithoutCheckEllipse`
followed by `coordsEVtoRW`; the arc and segment formulas are exactly
`GeoConicPart.pathChanged` and `GeoSegment.pathChanged`, whose clamps are
inactive inside the domain.

## 5. Operations of class v1

Notation: `P = (Px,Py,Pz)` is a point node after §5.1 normalization with
inhomogeneous `(PX,PY)`; `g = (ga,gb,gc)` is a line. Every formula is the exact
GeoGebra formula of the named algorithm.

| Algorithm (exact class) | Output | Formula |
|---|---|---|
| `AlgoJoinPoints(P,Q)` | line | `(PY - QY, QX - PX, PX QY - PY QX)` |
| `AlgoLinePointLine(P,l)` | line | `(Pz la, Pz lb, -(Px la + Py lb))` |
| `AlgoOrthoLinePointLine(P,l)` | line | `(-Pz lb, Pz la, Px lb - Py la)`, then the power-of-two coefficient normalization (thresholds `1e-2`, `1e7`), whose iteration count must be uniform on the box |
| `AlgoIntersectLines(g,h)` | point | `(gb hc - gc hb, gc ha - ga hc, ga hb - gb ha)` |
| `AlgoMidpoint(P,Q)` | point | `((PX + QX)/2, (PY + QY)/2, 1)` |
| `AlgoTranslate(P,v)` | point | `(Px + vx Pz, Py + vy Pz, Pz)` |
| `AlgoRotate(P,φ)` | point | `(Px c - Py s, Px s + Py c, Pz)`, `c = MyMath.cos(φ)`, `s = Math.sin(φ)` |
| `AlgoRotatePoint(P,φ,Q)` | point | with `qx = Pz QX`, `qy = Pz QY`: `((Px - qx) c + (qy - Py) s + qx, (Px - qx) s + (Py - qy) c + qy, Pz)` |
| `AlgoMirror(P,Q)`, point mirror | point | `(2 Pz QX - Px, 2 Pz QY - Py, Pz)` |
| `AlgoMirror(P,g)`, line mirror | point | if `|ga| > |gb|`: `qx = -Pz gc / ga`, `qy = 0`; else `qx = 0`, `qy = -Pz gc / gb`; with `u = Px - qx`, `w = Py - qy`, `c2 = Math.cos(2 atan2(-ga,gb))`, `s2 = Math.sin(...)`: `(u c2 + w s2 + qx, u s2 - w c2 + qy, Pz)` |
| `AlgoDilate(P,r)` / `AlgoDilate(P,r,S)` | point | `(r Px + (1 - r) SX Pz, r Py + (1 - r) SY Pz, Pz)`, `S = (0,0)` when absent |

Transform parameters (`v`, `φ`, `Q`, `g`, `r`, `S`) must be constants. The
meet operands must be unbounded lines; segment- or ray-bounded meets, whose
incidence predicate depends on containment, are outside the class.

### 5.1 Uniform predicates

After every point-producing operation the GeoGebra point normalization is
replayed exactly:

- the point is **infinite** iff `|z| <= ε`, `|z| <= ε|x|` and `|z| <= ε|y|`
  (`ε = Kernel.STANDARD_PRECISION = 1e-8`); the box must certify it finite;
- the sign of `z` must be uniform; a negative `z` negates `(x,y,z)`;
- `X = x/z`, `Y = y/z` (the identity when `z` is exactly `1`).

Every line node must certify GeoGebra definedness, `|a| >= ε` or `|b| >= ε`, on
the whole box; this also decides the full-line incidence check of
`AlgoIntersectLines`. Every midpoint input is finite by the rule above. The
dependent point must be finite; the model's value is its `(X,Y)`. A midpoint and
an expression-point translation (§5.2) produce a unit point, `z` exactly `1`,
which is always finite.

### 5.2 Operation of class v2: expression-point translation

Class v2 is class v1 plus exactly one operation. A driver-dependent geo is an
**expression-point translation** when, in the isolated slice:

1. its parent algorithm has the exact class `AlgoDependentPoint`;
2. its definition, after removing `NO_OPERATION` leaf wrappers, is one binary node
   `PLUS(P, V)`, `PLUS(V, P)` or `MINUS(P, V)`;
3. the point operand `P`, after the same unwrapping, is a geo of exact class
   `GeoPoint` that is driver-dependent; it is captured as a node;
4. the vector operand `V`, after the same unwrapping, is either a
   driver-independent, defined geo of exact class `GeoVector`, captured by its
   stored `(x, y)` at the maximal driver-independent boundary, or an inline
   `MyVecNode` literal in Cartesian mode whose two components each unwrap to a
   numeric literal of exact class `MyDouble` or `MySpecialDouble`, captured by
   their values;
5. both components of `V` are finite.

Any other expression shape has no program (§9). The operation is:

| Algorithm | Output | Formula |
|---|---|---|
| `AlgoDependentPoint`, `P + V`, `V + P` or `P - V` | point | `TRANSLATE(VEC(P), u)` with `VEC(P) = (PX, PY, 1)`, `u = V` for a sum and `u = -V` for the difference; that is `(PX + ux, PY + uy, 1)` |

This is the exact GeoGebra formula: `GeoPoint.getVector()` reads `(PX, PY)`,
`GeoVec2D.add` and `GeoVec2D.sub` combine componentwise (real and binary64
addition are commutative, and `x - y` is `x + (-y)` exactly), and
`AlgoDependentPoint` sets the result with `z = 1`. The step reuses the v1
`TRANSLATE` formula on the unit reading `VEC(P)`; it is not a relabeling of
`TRANSLATE(P, u)`, which keeps `P`'s homogeneous triple and differs from the
expression point whenever `Pz ≠ 1`. The step has no divisor and no branch
predicate.

## 6. Interval semantics

- Every arithmetic operation widens round-to-nearest by one adjacent binary64
  value, as `SplineOutwardInterval2D` does; nonfinite bounds, overflow and a
  divisor interval containing zero refuse the box.
- Values carry first derivatives with respect to `t` (forward mode, product
  and quotient rules on intervals).
- `cos` and `sin` over `[a,b]`, `|a|,|b| <= 1000`: endpoint values from
  `StrictMath` widened by one ulp each way; the extreme value `±1` is included
  whenever `[a,b]` meets an outward enclosure of a critical point `kπ/2`
  computed from the bracket `Math.PI < π < nextUp(Math.PI)`; the result is
  clamped to `[-1,1]`. Their derivatives are `-sin` and `cos` times the argument
  derivative.

## 7. Model obligations

The captured program is exposed to the certifier as a certified interval curve
model:

| Obligation | Construction model |
|---|---|
| enclosure of value and derivative on a box | §5–§6; refusal throws and leaves the box unresolved |
| smoothness | the box lies inside the closed driver domain; the formulas are infinitely differentiable wherever defined |
| period | always `0`; the circle-point canonical domain is not exactly closed under real `cos`/`sin` (the seam gap is about `2.45e-16 r`), so no periodic identification is claimed and roots in the seam band stay unresolved |
| canonicalization | the definition provider's own |
| computational partition | circle: eight equal spans of the declared domain; arc and segment: quarters of `[0,1]`; scalar: the declared domain. Partition points are certificate material, never identity |

A similarity image of a modeled source is modeled by composing the same
captured `LocusSimilarityTransform2D` maps that the spline model applies, with
the same shared implementation.

v1.2 note: the period row applies to programs of class v1 and v2. A program of
class v3 may claim the periodic identification of its driver circle under §10.8.

## 8. Lifecycle and versioning

The program is recaptured for every certification from the current evaluator.
It is never persisted, never enters a selector, token, ledger entry or document,
and a failed capture never invalidates an existing claim: the claim stays
dormant until the same selector is certified again. A later version of the class
must use a new program version identifier.

The version of a captured program is derived from its steps:

| Steps | Version |
|---|---|
| every step is an operation of §5 | `certified-construction-program/v1`; the signature is unchanged |
| at least one step is the operation of §5.2 | `certified-construction-program/v2` |
| at least one step or driver form of §10.1 | `certified-construction-program/v3` |

No program is relabeled merely because the implementation evolves, and no
document changes: a newer build may certify more current constructions at run
time, which is a capability of the build, not a migration of the file.

## 9. Outside the class

Locus-support generators, scalar states without the direct affine certificate,
expression-defined points (`AlgoDependentPoint`) other than the expression-point
translation of §5.2, GeoGebra line–conic and
conic–conic intersections (their two outputs are ordered by a distance-table
continuity heuristic, which is proximity), selected Locus V2 roots
(`AlgoLocusIntersectionPointV2`), segment- or ray-bounded meets, lists, macros,
functions and every other algorithm have no program. They are not defects of the
class; each needs its own certified semantics before it can be admitted.

Outside §5.2 in particular: arbitrary coordinate expressions such as
`(x(C)/2, y(C))`, nested or repeated sums, `V - P`, a point used as the vector
(`P + A`), scaled or driver-dependent vectors, literal components that are
expressions or reference a geo, polar, complex-number and 3D literals, and
`GeoVector3D`. The scalar family keeps its v1 rule: its program is only the
direct affine certificate, and §5.2 applies to point-driven slices.

v1.2 note: class v3 (§10) admits the line–circle intersection, ray- and
segment-bounded operands, multi-output steps, vector operations, macro expansion
and a structural metric input under its own rules. The exclusions of this
section remain for classes v1 and v2; continuous-mode intersections,
reference-point selections and conic–conic intersections remain outside every
class.

## 10. PRE-G9B-CU2 amendment (v1.2): class v3 and existence certification

Normative text of amendment A2.2-R of the
[`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
approved as architecture by the author on 2026-10-21 together with ADR 0038 and the
decisions D-CU2-2, D-CU2-5, D-CU2-6, D-CU2-16, D-CU2-17 and D-CU2-18
([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)).
The text was approved with `PRE-G9B-CU2` (`PASS — AUTHOR APPROVED`, 2026-10-24; [closeout record](../../../docs/validation/pre_g9b_cu2_closeout_record.md)).
Programs of class v1 and v2 and their consumers (§1 to §9) are unchanged.

### 10.1 Class v3

A program of class v3 has its own version identifier; v1 and v2 programs are not relabelled.
Class v3 adds, strictly for the constructions that the existence certificate needs:

- rays and segments, as a line with a parameter-membership validity predicate;
- circles by centre and constant radius, and by centre and point;
- line–circle intersection, including indexed single outputs (§10.2);
- algorithms with several outputs, as one step with indexed outputs;
- scalar multiples of vectors, dot products, unit vectors of vectors with a nonzero validity
  predicate, and point-plus-vector;
- expansion of a macro whose internal steps are all in the class;
- a structural metric input (`locus-v2-metrics.md` §26), which contributes its structural
  certificate and never its value;
- second-order derivative enclosures for the operations of the class, used for the isolation
  and non-degeneracy of stationary points.

Class v3 is captured from the evaluator's isolated slice under the reconstruction fidelity
rule of `locus-v2-semantics.md` (section "PRE-G9B-CU2 amendment"): a slice whose fidelity is
not established yields no program.

### 10.2 Selection state

Each output of a line–circle intersection is defined by its algorithm, never by a sample: the
raw roots are ordered by the parameter along the algorithm's line direction, and the output is
selected through the algorithm's selection state (its permutation, its initialization flags and
the label state of its outputs). The capture reads that state through read-only accessors, and
the revision fingerprint includes it. At every revision synchronization the evaluator slice
must reproduce the selection state of the live algorithm; otherwise the selection is
`NOT_ESTABLISHED` with a typed diagnostic. The state is never written into an algorithm and is
never replaced by a geometric heuristic. An algorithm still in its initialization selects by
compaction of the roots that lie on its limited paths; this rule is certified only where the
number of such roots is proven constant, and a change of that number is a selection boundary.

A change of the live selection state, including the end of the initialization, changes the
fingerprint and publishes a revision with a typed diagnostic. A real transition of the
inherited state during a first initialization may publish one such revision; repeated
recomputes in an unchanged state never publish further revisions. Continuous-mode
intersections and reference-point selections stay outside the class.

### 10.3 Effective outputs at coincident roots

The mathematical existence of a root, its multiplicity, the semantic identity of the selected
solution, the effective definition of the output that defines the traced point, its
availability in the evaluator slice, the continuity of the one-sided limits and the continuity
of the selected construction are distinct facts, and none is inferred from another. A
component is never declared continuous, and its locus never `COMPLETE`, only because the
geometric equation has a double root, when the indexed output that defines the traced point is
undefined at that parameter. Where the inherited algorithm sets an unlabelled second output
undefined because it coincides with the first within a tolerance, the effective definition of
that output near a coincident root depends on tolerance and rounding: the certificate states
neither existence nor continuity of that output across the coincident root, and publishes
around it an unresolved band with a typed cause, bounded by conservative predicates of the
inherited computation on first-level inputs; outside the band the output exists where its
validity predicates are certified.
Where both outputs are labelled in the live construction and in the slice, the inherited rule
does not apply and the double root is classified by the exact certificate. A certified limit
value at a coincident root is evidence, never an available output, and no output identity is
ever replaced by another because their points coincide. The inherited suppression rule is not
changed.

### 10.4 Box outcome

Evaluating a program on a parameter box yields `VALID` (every validity predicate certified
true), `UNIFORMLY_INVALID` with a typed reason (`DISCRIMINANT_NEGATIVE`,
`RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`,
`DIVISOR_ZERO`) or `UNRESOLVED`. Only `UNIFORMLY_INVALID` may produce
`CERTIFIED_DOES_NOT_EXIST`. Interval enclosures establish it only for strict-inequality
predicates; an equality predicate (vanishing divisor, direction or radius) is uniformly invalid
only through an exact structural identity, and an enclosure that contains zero yields
`UNRESOLVED`.

### 10.5 Certified transitions

A transition is certified when the validity predicate `P` of one identified operation changes
sign across a bracket and an enclosure of `P'` over the bracket excludes zero; the bracket is
narrowed to width at most `delta`. The end is closed for a discriminant root and for a ray or
segment parameter root, and open for a vanishing divisor, direction or radius. When the
enclosures of `P` and `P'` both contain zero on a box that cannot be subdivided further within
the budget, the box is decided by the exact algebraic certificate, when `P` is first-level, or
by an isolated stationary point of `P` (a simple root of `P'` certified with an enclosure of
`P''` that excludes zero) at which the enclosure of `P` excludes zero; otherwise it is an
unresolved band.

### 10.6 Exact algebraic certificate

A validity predicate is first-level when its inputs are captured constants and points that
depend rationally on the driver, and the driver runs on a circle or a segment. Its captured
constants are exact rationals; with `u = tan(t/2)` it becomes a polynomial with exact rational
coefficients, and `t = +-pi` is evaluated separately. Square-free factorization and Sturm
sequences in exact integer arithmetic give its real roots, their multiplicities and the signs
between them. An odd-multiplicity root with a sign change is a certified transition with a
closed end. An even-multiplicity root with a nonnegative predicate on both sides is a certified
singular parameter, interior to its component. An even-multiplicity root with a nonpositive
predicate on both sides is an isolated point of existence, a point component
(`locus-v2-existence-components.md` §15.4). Simple-root isolation (a sign change with a
derivative enclosure that excludes zero) never certifies a multiple root; in class v3 the
multiple roots of first-level predicates are certified by this certificate, and those of other
predicates remain unresolved. The certificate decides the configuration defined by the captured
binary64 constants, never an ideal geometric relation that the construction computes with
rounding. Exhausting the certificate budget yields `UNRESOLVED`.

The evidence records the method version, the coefficient fingerprint, the degree, the maximal
coefficient bits, the width bound, the bisections used and the result. It depends only on the
captured constants and on the method version.

### 10.7 Certified stationary points

A stationary point of the traced map is certified only by separate proofs of: existence, from
an exact structural symmetry `sigma` of the driver domain with `F o sigma = F`, verified step by
step on the captured program; isolation, from an enclosure of one component of `F''` that
excludes zero on a box around it; non-degeneracy, `F''(t0) != 0`; continuity, as an interior
point of a certified component; and, for a fold, opposite one-sided tangents. An enclosure of
`F'` that contains zero certifies nothing. Without these proofs a high turn is not a certified
fold. Second-order enclosures serve these proofs only; they do not extend `CU2` to the
materialization of tangencies.

### 10.8 Structural totality and periodicity

An operation is structurally total when it is defined and continuous for every valid input; the
total operations form a versioned table. A program whose driver is a point of a circle and
whose operations are functions of their current inputs, with captured selections certified
constant on each component, defines a function on the circle: its boxes may wrap the seam, and
it may claim the periodic identification of its driver circle. The identification belongs to
the real semantic map: outward enclosures of the real driver functions absorb the binary64 seam
gap that §7 records for the evaluator. Otherwise the period is `0` and no periodic
identification is claimed.

### 10.9 Versioned constants and budgets

The step limit applies to driver-dependent steps after macro expansion. It and the following
constants are versioned; the author approved their ranges (D-CU2-5) and the technical candidate
fixes their values with reproducible benchmark evidence:

| Constant | Approved range |
|---|---|
| Transition bracket width `delta` | `2^-48` to `2^-32` times the period |
| Existence boxes per branch and revision | 1 024 to 16 384 |
| Minimum existence depth | 4 to 8 |
| Driver-dependent step limit (class v3) | 128 to 512 |
| Maximal intermediate coefficient bits of the exact certificate | 1 024 to 16 384 |
| Exact bisections per root | 256 to 2 048 |

Exhausting a budget yields a typed `UNRESOLVED`; it never yields certified existence or
nonexistence.

### 10.10 Minimal certified composition on a locus support

Normative precision of author decision V-CON-6, option 2 (author resolution before `CU2` step
T12, 2026-10-22). It was approved with `PRE-G9B-CU2` (`PASS — AUTHOR APPROVED`, 2026-10-24; [closeout record](../../../docs/validation/pre_g9b_cu2_closeout_record.md)). It is not a program and does not extend class v3: a locus-support generator still
has no program (§4, §9).

1. **Scope.** A dependent locus whose generator is a point of a branch of a locus support
   (`LOCUS_BRANCH_POINT`), when that branch is `COMPLETE` and has no point component. Any other
   support keeps the declared limitation of `locus-v2-existence-components.md` §15.8.
2. **Constructive program.** The steps from the generator point to the traced point are
   captured from the evaluator's isolated slice with the walk of §3 and §10.1, the generator
   point being node 0, under the reconstruction fidelity rule of §10.1. Every step must be
   structurally total under the versioned table of §10.8 (`structural-totality/v1`): defined and
   continuous for every input, with constant parameters.
3. **Dependencies of the DAG.** The traced point must read the driver coordinate only through the
   generator point. A geo that reads it on a path that avoids that point refuses the composition
   (`driver-outside-generator-point`), because the walk would take it for a constant. A slice
   that depends on a selected Locus V2 root is not composed.
4. **Composition.** The traced point is then a continuous function of the generator point, so the
   dependent exists exactly where the support branch does. Under the contract of the hybrid
   certifier it publishes the support's decomposition, with the domain and orientation of the
   support branch:
   - its topology, coverage and completeness;
   - each component with its interval or arc, its ends (kind, source, inclusion and enclosure),
     its maximality and its nature.

   The support's nonexistence becomes `DEPENDENCY_CERTIFIED_NONEXISTENT`. Lineage keys derive
   from the same structural sources (`locus-v2-existence-components.md` §15.6), so the
   components keep the identity and lineage of the support's. Certified singular parameters and
   folds, facts of the support's own map, are not composed.
5. **No promotion.** A declared domain never becomes a certified component by itself: a
   dependent whose steps are not all structurally total, or whose traced point reads the driver
   elsewhere, stays `NOT_ESTABLISHED` with no component. The composition provides no program
   and no certified curve model, so the dependent's consumers use only the capabilities that do
   not need one.

## 11. PRE-G9B-CU3 amendment (v1.3): residual jets and exact evaluation

Normative text of amendment A3.5 of the
[`CU3` normative proposal](../../../docs/architecture/pre_g9b_cu3_normative_proposal.md),
approved by the author on 2026-10-24 together with ADR 0039 and decisions D-CU3-4 and D-CU3-11,
with the precisions of that decision
([approval record](../../../docs/validation/pre_g9b_cu3_author_design_approval_record.md)): the
authority of the target (§11.3) and the conformance conditions of exact evaluation (§11.4). The
approval of this architecture does not certify in advance the viability of exact evaluation or its
budgets; gate G-M3 decides them. Programs of classes v1, v2 and v3, their consumers and the
certificates of `CU2` (§1 to §10) are unchanged.

### 11.1 Residual jets

The second-order enclosure of the traced point (§10.7) may be composed with the residual of a
supported target into enclosures of the residual and of its first two derivatives. This extends
§10.7 to the isolation of critical points of contact residuals, and certifies only claims of kind
(a) of `locus-v2-intersections.md` (PRE-G9B-CU3 amendment, CU3.1).

### 11.2 Exact evaluation in the authoritative configuration

The decision configuration `Σ_L⁺` of a structural point contains the captured configuration of `L`
(§3 and §10) without any change: the same captured constants, with the same exact meaning that the
class-v3 program gives them, and the same program. It adds the driver point `D`, the target `r`
and their ancestors. Every object receives exactly one exact meaning:

- a captured constant of `L` keeps its captured value and its representation in the program of
  `L` (a constant circle is its matrix circle); when it is also the target, its meaning as target
  is governed by §11.3;
- the driver support keeps the meaning of the captured driver constants, and the algorithm of `D`
  is evaluated on that meaning;
- an object outside the captured configuration that is an ancestor of a captured constant is taken
  at its binary64 state;
- the independent elements, the path parameters and the objects whose exact computation leaves the
  class are taken at their binary64 states;
- every other ancestor of `D` is recomputed exactly from its parents' meanings;
- the target and its ancestors are governed by §11.3: a recomputation of the target's subgraph is
  a diagnostic, never the target's meaning;
- term-identical objects share one meaning only when their representations are proved equal;
- a constant with two different exact representations in the program of `L` fails closed
  (`CONFIGURATION_REPRESENTATION_CONFLICT`).

The exact point `D*` is reconstructed by the exact form of the inherited selection rule (§10.2)
with the captured selection state. Coincident roots, continuous-mode intersections,
reference-point selections and an uncertified initialization fail closed. The slice is evaluated
with the driver replaced by the exact point `D*`, never by a parameter. Ruler-and-compass
operations generate a finite tower of quadratic extensions. The signs and the vanishing of
residuals, validity predicates, limited-target parameters and, when needed, first derivatives
(exact jets) are decided with exact arithmetic and constructive root-separation bounds. The result
is "equal", "different", or `EXACT_EVALUATION_OUT_OF_CLASS` / `EXACT_EVALUATION_BUDGET_EXHAUSTED`,
never "equal within a tolerance".

Because the captured configuration of `L` is contained unchanged, a decision is a property of the
authoritative point of `L` and needs no transfer. A deeper joint recomputation that also recomputes
the captured constants of `L` is a different configuration. It may be computed only as a diagnostic
(`INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`). Its result applies to the authoritative point only
if both configurations are proved to give the same exact value to every object the proof uses. A
refutation in `Σ_L⁺` is reported as such (`INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`), never as
nonexistence in the intended configuration. No captured constant or certificate of `L` is changed or
reinterpreted. The binary64 coordinates of `D` enter only the check that the reconstructed branch
corresponds to the live algorithm.

### 11.3 Authority of the target

Author precision of 2026-10-24. For every incidence that exact evaluation decides, two objects are
identified: the exact point of `L` in its captured configuration, and the authoritative target
under `locus-v2-intersections.md` §2.1. The exact meaning that the decision gives the target is
that authority:

- **line:** its homogeneous coefficients as the target adapter captures them, the current
  `GeoLine` coefficients taken as exact rationals. Another exact meaning, such as a recomputation
  of the target's subgraph or the representation of a captured constant, is admitted only when its
  coefficients are proved exactly proportional to those, with a proved nonzero factor;
- **segment and ray:** the support line as above and, in addition, the membership conditions of
  the limited part as the adapter captures it (start and end, or start and direction);
- **circle:** the representation of the target adapter (`g8b-circle-radial-distance-adapter/v1`:
  centre from the translation vector and radius from the first half axis). The captured matrix of a
  circle constant (§10), its half axes and numerical evaluations, such as implicit coefficients
  computed in binary64, are distinct representations; a decision uses another representation only
  when its exact equality with the adapter's is proved;
- **target recomputed from a subgraph:** it never replaces the binary64 coefficients of the existing
  target by those of an ideal reconstruction; its exact correspondence with the existing target
  object must be proved.

The incidence is decided against that meaning. When the incidence does not hold with it, the
result fails closed (`INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`). A difference between the
target's authority and a reconstruction or another representation of it is reported as
`TARGET_AUTHORITY_MISMATCH`, with the exact difference; an incidence that holds only with such a
reconstruction is a diagnostic, like a deeper joint recomputation, and never establishes the
incidence. Any need to change the geometric authority of a target, the capture convention of `CU2`
or another approved contract is a stop condition for an author decision. The meaning of a target is
never redefined to make an incidence hold.

### 11.4 Conformance conditions of exact evaluation

Exact evaluation conforms only when an early, blocking gate (G-M3) establishes, before its product
implementation:

1. mathematical viability on the mandatory cases and on the adversarial controls, including the
   arithmetic hypotheses of the captured constants (homogeneous coordinate `z = 1` and exact
   structural relations) and the configuration cases V-CFG-1 to V-CFG-6;
2. a versioned budget proposed from deterministic counters (exact operations, tower degree, maximal
   bit length), approved by the author when the contract requires it, never by the implementation
   itself;
3. correspondence with the live algorithm: each exact branch reproduces the inherited selection or
   fails with a typed cause;
4. coherence of configurations: no decision is transferred from another configuration;
5. the authority of the target (§11.3) for every incidence decided.

If any condition fails, the parts that depend on exact evaluation stop, and the alternatives are
presented to the author; none is authorized automatically. Its evidence is never replaced by a
residual, a tolerance or a proof in another configuration.

## 12. Constructive authority amendment (v1.4)

Normative texts AUT-4 and AUT-7 of the
[constructive authority normative proposal](../../../docs/architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md),
approved by the author on 2026-10-25 together with
[ADR 0040](../../../docs/adr/0040-constructive-geometric-authority-and-relation-integrity.md) and
decisions D-AUT-4 and D-AUT-11
([approval record](../../../docs/validation/pre_g9b_cu3_constructive_authority_approval_record.md)).
This section applies only to documents governed by `constructive-authority/v1`
([`locus-v2-constructive-authority.md`](locus-v2-constructive-authority.md) §4). In every other
document §1 to §11 apply unchanged. The programs of classes v1, v2 and v3, their consumers and the
certificates of `CU2` (§1 to §10) are unchanged in every document.

### 12.1 Decision configuration under the constructive authority (replaces §11.2 and §11.3)

The decision configuration of a structural point is the constructive configuration `Σ_C` of the
locus and of the target, with the declared boundary substitutes; the captured configuration `Σ_L`
of CU2 is a certified representation whose certificates enter by `cu2-certificate-transfer/v1`
(§12.2). The authoritative target of an intersection is the constructive object of
`locus-v2-constructive-authority.md` §1 (`locus-v2-intersections.md`, constructive authority
amendment). An incidence is never decided against a reconstruction chosen for that incidence.
`Σ_L⁺` and the target authority of §11.3 remain the decision configuration and the target
authority of `legacy-representation-authority/v1`.

The conditions of §11.4 apply with the constructive target of this section. Condition 3
(correspondence with the live algorithm) is kept and extends to constructive identity
certificates. Condition 4 ("no decision is transferred from another configuration") is replaced,
under the constructive policy, by the admitted routes of §12.2 and by the recheck of one proof in
`locus-v2-constructive-authority.md` §8.

### 12.2 Transfer of CU2 certificates (`cu2-certificate-transfer/v1`)

A CU2 certificate remains, unchanged in text, fingerprint, structure and lineage keys, a
certificate of its captured configuration `Σ_L`. Transferring it to the constructive authority
establishes the same statement in the constructive configuration `Σ_C` of the same objects. The
policy admits the transfer of a certificate if and only if (N1) the captured program is
structurally the program of the constructive object, with the same slice and branches (class-v3
program, inherited selection states, label states, effective outputs and structural definitions),
(N2) the transfer cites the fingerprint of the consuming revision and the same contract versions,
and (N3) every decision of the certificate, in the order of its dependencies, is established in
`Σ_C` by an admitted route named by the transfer record:

- *margin*, for open decisions: their interval program, re-evaluated with every captured constant
  replaced by an enclosure of both its captured value and a rigorous enclosure of its constructive
  value, keeps every strict sign;
- *structure*, for closed decisions implied by structure (fold symmetry, structural totality and
  periodicity, exact zeros that correspond to provenance relations), when every structural premise
  is `STRICT` under the constructive authority;
- *re-decision*, for closed decisions on captured bits (Sturm counts and multiplicities, point
  components, singular parameters, critical values): the decision is taken again in `Σ_C` by a
  rigorous method of the CU2 and CU3 contracts — exact evaluation with a constructible premise, a
  new interval certification of a fact of multiplicity one, or a constructive identity
  certificate.

The list of routes is closed by this policy: no tolerance, coordinate proximity or sampled
robustness transfers a certificate, and a new rigorous route needs a new policy version. Without a
demonstrated transfer the result is `EVIDENCE_INSUFFICIENT` with a typed cause, and every consumer
that requires `STRICT` fails closed. A re-decision that refutes the statement gives
`CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION`. A transfer record is revision evidence: it cites the
source certificate by kind, fingerprint and structure key, the authority version, the route and the
premises, and it is never persisted. No source certificate, component, fingerprint or lineage key
is modified or reinterpreted.

The enclosure of the constructive value of a captured constant contains the ideal geometric value,
including values that come from points on paths, trigonometric constructions and objects outside
the exact class; an error estimate or a numerically wide box never replaces it. Without an
established transfer, the membership of a structural point in its locus is never published as
`STRICT`.

### 12.3 Constructive identity certificates

A relation whose evidence needs a non-constructible boundary object is decided strictly only by a
constructive identity certificate (`locus-v2-constructive-authority.md` §8). Exact evaluation with
non-constructible substitutes gives at most `SUBSTITUTE_GENERIC` evidence (§7.1 of that contract),
which is `RELAXED`.

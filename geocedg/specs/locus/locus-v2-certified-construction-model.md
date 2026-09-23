# Locus V2 certified construction interval model

| Field | Value |
|---|---|
| Version | `1.0` (`certified-construction-program/v1`) |
| Phase | `PRE-G9B-R2-E0/E1` |
| Decision | [ADR 0028](../../../docs/adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md) |
| Consumer | [pair materialization](../curves/spline-v2-pair-materialization.md) §10 |
| Parents | [Locus V2 semantics](locus-v2-semantics.md), [existence and components](locus-v2-existence-components.md), [public surface](locus-v2-public-surface.md) |

This contract was introduced by the `PRE-G9B-R2-E0/E1` technical candidate. Its
author-approval status is recorded only in that phase's author-decision record,
never here.

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

One disagreement is known and pre-existing. After `GeoSegment.pathChanged` the
reconstructible evaluator does not refresh the inhomogeneous coordinates of a
`SEGMENT_POINT` driver, as `AlgoPointOnPath` does, so a segment-driven slice
that reads them (a join or midpoint through the driver, for example) evaluates
the previous parameter, so its floating positions and lengths are wrong as
well. The verification refuses every such certified root, and those classes stay
unmaterialized. Slices that read only the driver's
homogeneous coordinates are unaffected. The defect belongs to the evaluator,
not to this contract, and is retained debt
`RECONSTRUCTIBLE-SEGMENT-DRIVER-INHOMOGENEOUS-LAG`.

A second pre-existing evaluator defect is outside this contract. When two
commands of the slice each take an inline literal argument, for example
`Line(Cj,(0,3))` followed by `PerpendicularLine((0,0),gj)`, the isolated copy of
the slice no longer depends on the driver, and the evaluator returns one
constant point for every parameter. A single inline literal, observed in a
construction built for it alone, is replayed correctly. The capture refuses the
affected slice by rule 5 of §3, so no certified root is built on it. The wrong
floating value is retained product debt
`RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED`, recorded in the
`PRE-G9B-R2-E0/E1` design record.

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
   exact class is listed there; its inputs are captured recursively;
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

`LOCUS_BRANCH_POINT` and scalar states without that certificate have no program.
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
dependent point must be finite; the model's value is its `(X,Y)`.

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

## 8. Lifecycle and versioning

The program is recaptured for every certification from the current evaluator.
It is never persisted, never enters a selector, token, ledger entry or document,
and a failed capture never invalidates an existing claim: the claim stays
dormant until the same selector is certified again. A later version of the class
must use a new program version identifier.

## 9. Outside class v1

Locus-support generators, scalar states without the direct affine certificate,
expression-defined points (`AlgoDependentPoint`), GeoGebra line–conic and
conic–conic intersections (their two outputs are ordered by a distance-table
continuity heuristic, which is proximity), selected Locus V2 roots
(`AlgoLocusIntersectionPointV2`), segment- or ray-bounded meets, lists, macros,
functions and every other algorithm have no program. They are not defects of the
class; each needs its own certified semantics before it can be admitted.

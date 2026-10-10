# PRE-G9B-R6-plus-F1 — orientation cue: preparation characterization report

- Activity: `PRE-G9B-R6-plus-F1-PREP` (author instruction of 2026-10-11)
- Authorization: **characterization and design preparation only**. `F1`
  product implementation and publication are **not authorized**.
- Preparation base: published `main` `bde2827bb21e4a28e080db6a65b4abc892071cf8`,
  tree `1b74102cea5151b8a6c571cb36b96b33af6fb3c8` (`POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1`
  closeout; C1 candidate `22a215d8bcf5bf9a43ce881e992e37dbf0eb070b`)
- Preparation branch: `phase/pre-g9b-r6-plus-f1-prep` (local)
- Preparation class: `DOCUMENTATION_STATUS_ONLY`, acceptance `STATIC`
- State: `CHARACTERIZATION = COMPLETE`; `F1 = PREPARED — NOT AUTHORIZED`;
  `selfApproved = false`
- Machine-readable mirror: `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-preparation-characterization.json`
- Companion documents: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md),
  [author-decision preparation record](pre_g9b_r6_plus_f1_author_decision_preparation_record.md),
  [canonical F1 prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)
- Historical evidence kept unchanged: the `P0`
  [F1 design candidate](../architecture/pre_g9b_r6_plus_f1_orientation_design_candidate.md)
  (`PROPOSED — CANDIDATE — NOT AUTHOR APPROVED`) and the
  [P0 report](pre_g9b_r6_plus_p0_characterization_design_candidate_report.md) finding `F17`

Path abbreviations: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`;
`desktop/` = `source/desktop/desktop/src/main/java/`; `geocedg/` (in code
citations) = `desktop/org/geocedg/desktop/`.

Evidence labels: **AUTHOR-FIXED** (author scope), **PROVEN FROM SOURCE**
(read at the base), **PROVEN BY PROBE** (scratch runtime probe at the base),
**INHERITED CONTRACT** (approved normative source), **INFERRED** (reasoned,
not executed), **HYPOTHESIS** (design proposal), **UNRESOLVED** (author
decision or missing authority).

## K0. Entry gate and baseline

```text
local main / HEAD    = bde2827bb21e4a28e080db6a65b4abc892071cf8
origin/main          = bde2827bb21e4a28e080db6a65b4abc892071cf8
live remote main     = bde2827bb21e4a28e080db6a65b4abc892071cf8   (git ls-remote origin refs/heads/main)
tree                 = 1b74102cea5151b8a6c571cb36b96b33af6fb3c8
worktree             = clean
C1 prerequisite      = PASS — AUTHOR APPROVED — PUBLISHED (roadmap "Última fase cerrada";
                       mini-track plan status block)
F1 roadmap state     = NOT AUTHORIZED (roadmap mini-track table; plan status block)
F1 implemented?      = no: no orientation-cue code, key or test exists at the base
                       (grep of source/, apps/, tools/ for "orientation-cue",
                       "geocedg.view." and "OrientationCue": no match)
```

The entry conditions of the instruction hold. The preparation branch was
created from that commit; nothing was published.

## K1. Authorities and their status

| Source | Status for F1 |
|---|---|
| Mini-track plan §2 "Orientation display", §9.1, §11 row, §12 row, `AQ-F1` | **AUTHOR-FIXED** scope: optional, discreet cue (preferably a hollow arrow) for the actual direction the kernel uses for a line, segment or ray; derived from kernel semantics, never from the viewport; no change of geometry or command behavior. Everything else in §9.1/§11/§12 is planning pre-characterization or candidate |
| `P0` F1 design candidate | historical design evidence, `PROPOSED — CANDIDATE — NOT AUTHOR APPROVED`; not normative |
| `P0` report `F17` | historical characterization at `P_R6PLUS_PLAN` `babf20c7`; re-established here at `bde2827b` (§K15) |
| `E2` preparation report `K7` | earlier probe of `getDirection` / `getDirectionInD3` (7 routes); consistent with §K4 |
| `unit-system.md` §17.2 | **INHERITED CONTRACT** for the meaning of `USER_PREFERENCE`, `SESSION`, `DOCUMENT_PRESENTATION`, `DERIVED_TRANSIENT` |
| `verification-levels.md` §12.8 | **INHERITED CONTRACT** for verification classes |
| `AGENTS.md` §4, §9, §17 | placement rule; design-before-code; "conflate object existence, geometric visibility and layer/UI visibility" prohibited |

No prior author decision exists on `AQ-F1` or on any F1 design question.
Neither the `E3` nor the `C1` closeout authorizes F1.

## K2. Probe route

Scratch-only JUnit class `F1PrepOrientationProbeTest` compiled into the
Desktop test source set through a Gradle init script that adds a scratchpad
source directory; run with
`.\gradlew.bat --init-script <scratch>\probe.gradle :desktop:desktop:test --tests org.geocedg.desktop.F1PrepOrientationProbeTest`
on the base. Two methods: `constructionMatrix` (74 output lines) and
`dynamicBehaviour` (37 output lines; command error dialogs neutralized with a
static `JOptionPane` mock after a first attempt blocked on a modal error dialog
of `SetCoords` on a free vector, which was then replaced by a direct
`GeoVector.setCoords` + `updateCascade`). Both runs exited 0. Afterwards
`:desktop:desktop:compileTestJava` was rerun without the init script and the
probe's test-result XML and compile-stash copy were removed. No tracked path
was written.

For every object the probe reports the class, parent algorithm, coefficients
`(x, y, z)`, `getDirection`, `getDirectionInD3`, start and end points, the
displacement between two points placed on the object by its own
`pathChanged` (`t = 0.5 → 1.5` for lines and rays, `0.25 → 0.75` for segments),
and `UnitVector(g)`. "Path agrees" means that displacement has a positive dot
product with `getDirection`.

Probe environment (PROVEN BY PROBE): `Kernel.isContinuous() = false`,
`usePathAndRegionParameters = true`.

## K3. Orientation accessors and kernel consumers

**K3.1 Storage.** A `GeoLine` stores `(x, y, z)` for `x·X + y·Y + z = 0`;
`GeoSegment` and `GeoRay` extend it (PROVEN FROM SOURCE).

**K3.2 `getDirection`.** `(y, −x)`, `final`, both overloads
(`common/kernel/geos/GeoLine.java:444-457`). The private `getDirectionInD`
used by `getPointInD`, `getMainDirection` and `getDirectionForEquation`
returns the same vector (`:1352-1369`) (PROVEN FROM SOURCE).

**K3.3 `getDirectionInD3`.** `(−y, x)` when `getEndPoint() == null`,
otherwise `end − start` (`GeoLine.java:1402-1409`). It is opposite to
`getDirection` on **every** route without an end point and equal to it on
routes with both points (PROVEN FROM SOURCE; PROVEN BY PROBE on the 64 defined
objects of the matrix: equal on 13 — `Line(A,B)`, `Segment`, `Ray(A,B)`,
polygon edges, the reflected segment, macro `Line(A,B)` and the axes —
opposite on 51). It is not an orientation authority.

**K3.4 Path parameters.** A line point is `start + t·(y, −x)`
(`GeoLine.java:1140-1162`; without a start point, the foot of the
perpendicular from the origin); a segment point is `start + t·(y, −x)` with
`t ∈ [0, 1]` (`GeoSegment.java:401-452`); a ray point is `start + t·(y, −x)`
with `t ≥ 0` (`GeoRay.java:168-243`). `doPointChanged` inverts this mapping
(`GeoLine.java:1064-1112`). PROVEN BY PROBE: the path displacement agrees with
`getDirection` on all 64 defined objects of the matrix (§K4) and on the 19
defined dynamic states of §K7 that have a finite non-zero direction; the only
exceptions are the zero-length segment (zero displacement) and the
infinite-coefficient line (`NaN`) of §K7.

**K3.5 `UnitVector` / `Direction`.** One processor, `CmdUnitVector`, with and
without normalization (`common/kernel/commands/BasicCommandProcessorFactory.java:64-67`);
for a line `x = g.y`, `y = −g.x` (`common/kernel/algos/AlgoUnitVectorLine.java:38-41`).
PROVEN BY PROBE: `UnitVector(g)` equals the normalized `getDirection` on every
defined route.

**K3.6 Oriented angle.** `Angle(g, h)` = `atan2(det(n_g, n_h), n_g·n_h)` of the
normals (`common/kernel/algos/AlgoAngleLines.java:106-118`), which equals the
oriented angle of the `getDirection` vectors (a common rotation of both).
Reversing one line changes the result by 180° (PROVEN FROM SOURCE).

**K3.7 Conclusion.** The **accessor authority** is `GeoLine.getDirection`
(equivalently the path parameter and `UnitVector`): it is the direction every
kernel consumer uses. Whether that direction carries a **constructive
geometric sense** depends on the route (§K4–§K6).

## K4. Construction matrix (PROVEN BY PROBE unless noted)

`A = (1,1)`, `B = (4,3)`, `C = (5,6)`, `P = (0,5)`, `v = (2,1)`,
`c = Circle((0,0), 2)`, `T = (2,0)`, `fl(x) = 2x + 1`, `fq(x) = x²`. "D3" is
`getDirectionInD3` relative to `getDirection` (`=` same, `−` opposite).
"Class" is the orientation class proposed in §K6.

| # | Route | Type / algorithm | `getDirection` | Semantic sense | D3 | Class |
|---|---|---|---|---|---|---|
| R01 | `Line(A,B)` | `GeoLine` / `AlgoJoinPoints` | (3, 2) | `B − A` | = | constructive |
| R02 | `Line(B,A)` | same | (−3, −2) | `A − B` | = | constructive |
| R03 | `Segment(A,B)` | `GeoSegment` / `AlgoJoinPointsSegment` | (3, 2) | start → end | = | constructive |
| R04 | `Segment(B,A)` | same | (−3, −2) | start → end | = | constructive |
| R05 | `Ray(A,B)`, `Ray(B,A)` | `GeoRay` / `AlgoJoinPointsRay` | (3, 2), (−3, −2) | from the start through the second point | = | constructive |
| R06 | `Ray(A,v)` | `GeoRay` / `AlgoRayPointVector` | (2, 1) | `v` | − | constructive |
| R07 | `Line(P,v)`, `Line(A,v)` | `GeoLine` / `AlgoLinePointVector` | (2, 1) | `v` | − | constructive |
| R08 | parallel `Line(P,lAB)`, `Line(P,lBA)`, `Line(P,sAB)` | `AlgoLinePointLine` | (3,2), (−3,−2), (3,2) | direction of the reference | − | inherited |
| R09 | `PerpendicularLine(P,lAB)`, `(P,lBA)` | `AlgoOrthoLinePointLine` | (−2, 3), (2, −3) | reference rotated +90° | − | inherited |
| R09c | `PerpendicularLine(P,v)` | `AlgoOrthoLinePointVector` | (−1, 2) | `v` rotated +90° | − | inherited |
| R10 | `PerpendicularBisector(A,B)`, `(B,A)`, `(sAB)` | `AlgoLineBisector`, `AlgoLineBisectorSegment` | (−2,3), (2,−3), (−2,3) | `B − A` rotated +90° | − | inherited |
| R11 | `AngleBisector(A,B,C)` and `(C,B,A)` | `AlgoAngularBisectorPoints` | (−0.795, 0.607) both | from the vertex into the convex angle; **symmetric** in the outer points | − | configuration |
| R11c | `AngleBisector(lAB, m)` (`m = Line(B,C)`) | `AlgoAngularBisectorLines` | out 0: (0.607, 0.795); out 1: (−0.795, 0.607) | out 0 = normalized sum of the unit directions; out 1 = out 0 rotated +90° | − | inherited |
| R11d | `AngleBisector(lBA, m)` | same | out 0: (−0.795, 0.607); out 1: (−0.607, −0.795) | reversing one input exchanges the outputs and changes a sense | − | inherited |
| R12 | `Tangent(P,c)`, `P` outside | `AlgoTangentPoint` | (−1.833, −4.2), (1.833, −4.2) | from `P` towards each touching point | − | configuration |
| R12b | `Tangent(T,c)`, `T` on `c` | same (polar route) | (0, −2); second output undefined, unlabeled | polar coefficients `M·T`: gradient rotated −90° (clockwise on this circle) | − | representation |
| R12c | `Tangent(T,cEq)`, `cEq: x²+y²=4` | same | (0, −2) | as R12b | − | representation |
| R12d | `Tangent(T,cNeg)`, `cNeg: −x²−y²=−4` | same | **(0, 2)** | reversed by the sign of the conic equation | − | representation |
| R12e, R12f | `Tangent((0,2),c)`; `Tangent((3,0), x²/9+y²/4=1)` | same | (2, 0); (0, −1.333) | as R12b | − | representation |
| R12g | `Tangent(lAB, c)` | `AlgoTangentLine` | (3, 2) both | direction of the reference line | − | inherited |
| R13 | `Tangent(1, fq)`, `Tangent(1, fl)` | `AlgoTangentFunctionNumber` | (1, 2) | towards increasing `x`: `(1, f'(a))` | − | representation |
| R14 | `Line(P, fl)` | `AlgoLinePointLine` with a `GeoFunction` reference | **(−1, −2)** | towards decreasing `x`: `(−1, −m)` | − | representation |
| R14b | `PerpendicularLine(P, fl)` | `AlgoOrthoLinePointLine` | (2, −1) | `(−1, −m)` rotated +90° | − | representation |
| R15 | `y = 2x + 1`; `−2x + y = 1` | independent `GeoLine` | (1, 2) | normal form "left − right" | − | representation |
| R15b | `2x − y + 1 = 0` | independent | **(−1, −2)** | same line, opposite sense | − | representation |
| R15d, R15e | `x = 3`; `y = −1` | independent | (0, −1); (1, 0) | as written | − | representation |
| R15f | `X = (1,2) + t·(3,4)` | independent | (3, 4) | the typed direction vector, stored only as coefficients | − | representation (DQ-F1-5) |
| R15g, R15h | `3x + 4y = 0`; `0 = 3x + 4y` | independent | (4, −3); **(−4, 3)** | same line, opposite sense | − | representation |
| D09 | `k x + y = 0` (`k` a number) | `AlgoDependentLine` | from the coefficients | as written | − | representation |
| R16 | `xAxis`, `yAxis` | `GeoAxis` | (1, 0), (0, 1) | positive axis | = | not drawn by `DrawLine` (§K11) |
| R17 | edges of `Polygon(A,B,C)` | `GeoSegment` / `AlgoJoinPointsSegment` | (3,2), (1,3), (−4,−5) | vertex order | = | constructive |
| R18 | `Reflect(lAB, yAxis)` | `AlgoMirror` | (−3, 2) | image of the oriented direction | − | inherited |
| R18b | `Reflect(lAB, A)` | `AlgoMirror` | (−3, −2) | image under the half-turn | − | inherited |
| R18c | `Rotate(lAB, 90°, A)` | `AlgoRotatePoint` | (−2, 3) | rotated direction | − | inherited |
| R18d | `Translate(lAB, v)` | `AlgoTranslate` | (3, 2) | unchanged | − | inherited |
| R18e | `Dilate(lAB, −1, A)` | `AlgoDilate` | (−3, −2) | image under factor −1 | − | inherited |
| R18f | `Reflect(sAB, yAxis)` | `GeoSegment` / `AlgoJoinPointsSegment` (image endpoints) | (−3, 2) | image start → image end | = | constructive |
| R18g, R18h | `Reflect(par, yAxis)`; `Reflect(e2, yAxis)` | `AlgoMirror` | (−3, 2); (1, −2) | image of the source sense | − | inherits the source class |
| R19 | `Line(X, lAB)`, `X = Intersect(lAB, x = 3)` | `AlgoLinePointLine` | (9, 6) | positive multiple of the reference | − | inherited |
| R21 | `Asymptote(x² − y² = 1)` | `AlgoAsymptote` | (1, 1), (−1, 1) | from the conic coefficients | − | representation |
| R22 | `Polar(P, c)` | `AlgoPolarLine` | (5, 0) | from the conic coefficients | − | representation |
| R23 | macro `ProbeJoin(M1,M2)` (inner `Line(A,B)`) | `GeoLine` / `AlgoMacro` | (−3, −4) = `M2 − M1` | the inner route | = | inner route's class |
| R23b | macro `ProbePar(M1, lBA)` (inner parallel) | `AlgoMacro` | (−3, −2) | the inner route | − | inner route's class |

Every defined row: path displacement agrees with `getDirection`;
`UnitVector` equals its normalization.

## K5. Source mechanisms behind the matrix (PROVEN FROM SOURCE)

- **Two points.** `GeoVec3D.lineThroughPoints` writes
  `(A.y − B.y, B.x − A.x, …)` so that `getDirection = B − A`, with the comment
  "we want AB to be the direction vector of the line"
  (`common/kernel/geos/GeoVec3D.java:268-300`, comment `:273`). Used by
  `AlgoJoinPoints`, `AlgoJoinPointsSegment`, `AlgoJoinPointsRay` (`compute`
  at `:136`, `:205`, `:116` of their files) and by the external-point tangents
  (`common/kernel/algos/AlgoTangentPoint.java:152-158`).
- **Point and vector.** `lineThroughPointVector` writes `(−v.y, v.x, …)` so
  that `getDirection = v` (`GeoVec3D.java:367-379`).
- **Point and direction (homogeneous cross).** `AlgoLinePointLine` computes
  `cross(P, (l.y, −l.x, 0))` (`AlgoLinePointLine.java:118-121`);
  `AlgoOrthoLinePointLine` `cross(P, (l.x, l.y, 0))` followed by a
  power-of-two rescaling that preserves the sign (`AlgoOrthoLinePointLine.java:129-140`;
  `GeoLine.getNormalizedCoefficients` `:1620-1650`); `AlgoOrthoLinePointVector`
  `cross(P, (−v.y, v.x, 0))` (`:107-109`). With `P = (px, py, pz)` the result
  has `getDirection = pz · D`. The sign is safe because `GeoPoint.updateCoords`
  makes `z ≥ 0` for every finite point, with the comment "this is important for
  the orientation of a line or ray computed using two points P, Q with
  cross(P, Q)" (`common/kernel/geos/GeoPoint.java:938-946`; R19 probe).
- **Perpendicular bisector.** `(ax − bx, ay − by, …)`, i.e. `B − A` rotated +90°
  (`AlgoLineBisector.java:107-119`; `AlgoLineBisectorSegment.java:99-114`).
- **Angle bisector of three points.** Sum of the unit directions from the
  vertex, or a normal of their difference when the angle exceeds 90°, with a
  sign correction by `det` (`AlgoAngularBisectorPoints.java:130-237`). A
  continuity branch (`:161`, `:214`) is active only when
  `Kernel.isContinuous()`; in GeoCeDG `Kernel.setContinuous` forces `false`
  (`common/kernel/Kernel.java:1002-1007`), and the macro kernel always turns
  continuity off.
- **Angle bisectors of two lines.** Output 0 from the unit directions of the
  inputs, output 1 its +90° rotation, same continuity branch
  (`AlgoAngularBisectorLines.java:170-320`).
- **Tangent through a point on the conic.** The tangent copies the polar line
  `M·P` (`AlgoTangentPoint.java:59-75`; `common/kernel/kernelND/GeoConicND.java:3233-3241`),
  so its sense follows the sign of the conic matrix, which follows how the
  equation was written (R12d).
- **Tangent parallel to a line.** `(line.x, line.y, …)`, the reference
  direction (`AlgoTangentLine.java:99-108`).
- **Tangent to a function.** `(−f'(a), 1, …)`, i.e. `(1, f'(a))`
  (`common/kernel/cas/AlgoTangentFunctionNumber.java:124`;
  `AlgoTangentFunctionPoint.java:248`).
- **Function as a line reference.** `GeoFunction.getX() = m`, `getY() = −1`
  for a linear function (`common/kernel/geos/GeoFunction.java:2895-2917`), so
  a parallel through `P` points to `(−1, −m)`, opposite to the tangent of R13.
- **Equations.** The normal form is `left − right = 0`
  (`common/kernel/arithmetic/Equation.java:296-299`), and an independent line
  takes its `x`, `y` and constant coefficients unchanged
  (`common/kernel/commands/AlgebraProcessor.java:3304-3318`).
- **Affine transforms.** `GeoLine.matrixTransform` applies the adjoint of the
  3×3 matrix to the coefficients (`GeoLine.java:1412-1422`). For an affine map
  with linear part `A` (`det A ≠ 0`), `J·adj(A)ᵀ·J⁻¹ = A` for the rotation `J`
  that maps a normal to `getDirection`, so the image direction is exactly
  `A·d`, with a positive factor, for reflections too (INFERRED algebraically;
  PROVEN BY PROBE for R18–R18e). Projective maps are not covered.
- **Macros.** `AlgoMacro.initLine` copies the inner start and end points
  (`common/kernel/algos/AlgoMacro.java:474-480`); the outputs carry the inner
  coefficients (R23, R23b).

## K6. Orientation classes and unresolved cases

The accessor is the same everywhere; the **meaning** of its sense is not.
Proposed classification (HYPOTHESIS; admissibility is `DQ-F1-5`):

| Class | Definition | Routes | Admissibility evidence |
|---|---|---|---|
| `O-CONSTRUCTIVE` | the user's ordered inputs define the sense | R01–R07, R17, R18f, R23 (inner R01) | sense reverses exactly when the user reverses the order; deterministic; no discontinuity except at coincidence (undefined) |
| `O-INHERITED` | the sense is transferred from oriented inputs by a fixed documented rule (identity, +90°, affine image, sum of unit directions) | R08–R10, R11c/d, R12g, R18–R18e, R19, R23b | deterministic; inherits the input's class; the +90° sense is a kernel convention, stable and documented in source |
| `O-CONFIGURATION` | the sense is determined by the geometric configuration, not by the input order | R11 (into the convex angle; flips through the straight angle, D16–D18), R12 (from the external point; flips and exchanges outputs when the point reaches the conic, D12–D15) | deterministic per configuration, discontinuous at degenerate configurations |
| `O-REPRESENTATION` | the sense is an artifact of a coefficient sign, an equation's side, a conic matrix sign or a fixed axis convention | R12b–f, R13, R14, R14b, R15–R15h, D09, R21, R22 | the same point set can carry either sense depending on how it was typed or normalized; no constructive meaning |
| `O-UNKNOWN` | any route not characterized here | every other algorithm, 3D line types, lines inside lists | no evidence; fail closed |

**UNRESOLVED.** (a) The P0 rows "Tangent(P, conic) with P on the conic —
orientation UNKNOWN" and "equation line — INFERRED" are now established: both
are `O-REPRESENTATION` (R12b–R12d; R15–R15h). There is no constructive
authority for their sense. (b) Whether a parametric input `X = P + t·v`
(R15f) should count as constructive: the typed `v` determines the stored
coefficients, but the document keeps only coefficients and a display form that
the user can change without changing the sense. (c) Whether
`O-CONFIGURATION` senses are admissible. (d) Whether `O-INHERITED` routes
whose source is `O-REPRESENTATION` (R18h, `Line(P, e2)`) inherit that class —
proposed yes. These are author decisions (`DQ-F1-5`), not facts.

## K7. Dynamic behavior and degeneracies (PROVEN BY PROBE)

| Case | Result |
|---|---|
| D02 `B := A` | `Line(A,B)`, `Ray(A,B)`, the parallel through `P` and `Line(P,v)` with `v = 0` (D06): coefficients (0, 0, 0), `isDefined() = false`. `Segment(A,B)`: `isDefined() = true`, `length = 0`, `getDirection = (0, 0)`, `isEuclidianVisible() = true` |
| D03 `B = A + (10⁻¹³, 0)`; D03b `B = A + (10⁻⁹, 0)` | the line is undefined (`DoubleUtil.isZero`, tolerance `10⁻⁸`, `GeoLine.java:596-602`) although `getDirection = (10⁻⁹, 0)`; the segment stays defined with a direction near zero |
| D04 `B` moved behind `A` | the sense follows `B − A` continuously; the dependent parallel follows |
| D05 restored | same `GeoElement` instances for line, segment, ray and parallel: a temporary invalidity creates no new identity |
| D07 `v` reversed | `Line(P,v)` reverses |
| D08 redefinition `lAB := Line(B,A)` | new sense `A − B`; the redefinition replaces the object (`same instance = false`), label kept; this is upstream redefinition behavior |
| D09 `k = 1/0` in `k x + y = 0` | coefficients (+∞, 1, 0), **`isDefined() = true`**, `isEuclidianVisible() = true`, `getDirection = (1, −∞)`; path displacement `NaN` |
| D10 `k = 0/0` | `isDefined() = false` |
| D11 `Line((0,0), (10³⁰⁰, 10³⁰⁰))` | defined; direction (10³⁰⁰, 10³⁰⁰) finite |
| D12 `T` on `c` | tangent 0 = polar, (0, −2); tangent 1 undefined |
| D13 `T = (2.001, 0)` | two external tangents, (−0.0020, 0.0632) and (−0.0020, −0.0632): tangent 0 now points **up**, with a direction length ≈ 0.063 that tends to 0 as `T` approaches `c` |
| D14 `T = (1.999, 0)` | both undefined |
| D15 `T` back on `c` | both tangents defined and equal, (0, −2): the definedness of output 1 depends on history (pre-existing upstream behavior of `AlgoTangentPointND.compute`, `:131-176`) |
| D16–D18 `AngleBisector(W,U,Z)`, `Z` from (−5, 1) through (−5, 0) to (−5, −1) | (0.099, 0.995) → (0, 1) → (0.099, −0.995): the sense jumps through the straight angle |
| D19 XML reload | independent `2x − y + 1 = 0` keeps (−1, −2); dependent lines recompute the same sense |

Consequences for the cue (INFERRED): `isDefined()` alone is insufficient
(D02 segment, D09); the cue needs a finite, non-zero `getDirection` and finite
coefficients. A vanishing direction near a tangency (D13) is numerically
defined but meaningless in sense. Identity is never affected because the cue
is derived from the current object at paint time.

## K8. Paint pipeline and export routes (PROVEN FROM SOURCE)

- Live paint: `EuclidianViewJPanelD.paint(Graphics)` → `view.paint(g2)`
  (`desktop/org/geogebra/desktop/euclidian/EuclidianViewJPanelD.java:95-99`),
  the only production caller of `paint(GGraphics2D)`; `EuclidianView.paint`
  (`common/euclidian/EuclidianView.java:3782-3793`) → `EuclidianViewCompanion.paint`
  (`common/euclidian/EuclidianViewCompanion.java:387-427`): background,
  `drawObjects`, selection rectangles, bounding boxes, previews, mouse
  coordinates, animation buttons. `EuclidianViewD.repaint()` →
  `evjpanel.repaint()` (`desktop/org/geogebra/desktop/euclidian/EuclidianViewD.java:543-546`).
- `GeoCeDGEuclidianView.paint` (`geocedg/GeoCeDGEuclidianView.java:69-112`)
  calls `super.paint`, revalidates the `ISO_A_BORDER` link, draws the export-area
  overlay (`:48-67`) and the locus-intersection markers. Its own drawing runs
  after the kernel lock of `EuclidianView.paint` is released (INFERRED). The
  P0 citation `:38-75` is stale (the class grew in `B` and `E3`).
- No export, print or clipboard route calls `paint(GGraphics2D)`:

| Route | Path | Overlay reached? |
|---|---|---|
| host image export | `getExportImage` → `exportPaint` → `exportPaintPre` + `drawObjects` (`EuclidianView.java:5925-5946`, `:6013-6029`) | no |
| GeoCeDG picture service (PNG, PDF, EMF, raster) | `PictureExportService.paint` → `ExportViewport.create(...).exportPaint` (`geocedg/export/PictureExportService.java:416-422`); `ExportViewport` is a `final` `EuclidianViewD`, not a `GeoCeDGEuclidianView` (`geocedg/export/ExportViewport.java:33`) | no |
| GeoCeDG SVG | `exportPaintPre`, action objects, own loop over the viewport drawables (`PictureExportService.java:255-271`) | no |
| host dialog fallbacks | SVG/EMF/PDF/PNG (`desktop/org/geogebra/desktop/export/GraphicExportDialog.java:927-930`, `:1032`, `:1116`, `:1144-1152`) | no |
| print / preview | `EuclidianViewD.print` → `exportPaint` (`EuclidianViewD.java:362-386`); `PrintPreviewD` (`desktop/org/geogebra/desktop/export/PrintPreviewD.java:115-137`) | no |
| copy to clipboard | `AppGeoCeDG.copyGraphicsViewToClipboard` (`geocedg/AppGeoCeDG.java:792-806`) → `PictureExportService.exportImage` (`:352-365`) | no |
| CLI `--export` | `PictureExportCommandLine.export` (`geocedg/export/PictureExportCommandLine.java:98-114`) | no |
| PSTricks, PGF, Asymptote | text writer per object (`common/export/pstricks/GeoGebraExport.java`), drawables only for labels and inequalities | no |
| DXF | `org/geocedg/common/export/DxfExporter.java`: no drawable or paint use | no |

A cue drawn inside `DrawLine`, `DrawRay` or `DrawSegment` **would** reach the
picture exports, print and clipboard: `exportPaint` draws the live view's own
drawables, and `ExportViewport.bind` creates drawables of the same classes
(`ExportViewport.java:110`; `EuclidianView.java:2454-2475`).

## K9. Drawables and the existing outline arrow (PROVEN FROM SOURCE)

- `ARROW_OUTLINE` ending: `DrawSegmentWithEndings.getArrow`
  (`common/euclidian/draw/DrawSegmentWithEndings.java:282-312`) builds a
  triangle with its tip at the clipped endpoint, length and half-width equal to
  `lineThickness = geo.getLineThickness() · styleScale` (`:255`), stroked at
  `0.5 · styleScale`, rotated by the screen angle; outline styles are drawn
  with `draw`, filled with `fill` (`:95-106`; `common/kernel/geos/SegmentStyle.java:52-55`);
  ending shapes are subtracted from the stroked segment (`:128-138`). The
  method is `private` and tied to an `EndDecoratedDrawable`; no public helper
  draws an outline arrowhead at an arbitrary point. `RotatedArrow`
  (`common/euclidian/draw/RotatedArrow.java:26-71`) is package-private and
  draws an open V at a line end.
- `DrawLine.draw` and `DrawRay.draw` draw highlight, line and label only
  (`common/euclidian/draw/DrawLine.java:420-440`; `DrawRay.java:230-250`).
  `DrawSegment` has the per-object decoration `DECORATION_SEGMENT_ONE_ARROW`,
  an open V near the midpoint (`DrawSegment.java:261-275`, drawn `:423-468`);
  it is document style, serialized with the segment.
- Clipping: `DrawLine.setClippedLine` (`DrawLine.java:231-358`) orders its
  screen points left→right or bottom→top by slope, **not** by the kernel
  direction; `DrawRay` maps `(y, −x)` to the screen as
  `(dx·xscale, −dy·yscale)` (`DrawRay.java:116-142`) and clips from the start
  (`:181-227`); `DrawSegment` clips with `ClipLine` (`:149-161`, `:373-383`).
- Hit testing reads the drawable's own `line` or ending-decorated shape
  (`DrawLine.java:693-706`; `DrawRay.java:351-369`; `DrawSegment.java:562-593`;
  `common/euclidian/HitDetector.java:64-66`). Nothing drawn in
  `GeoCeDGEuclidianView.paint` is read by hit testing (INFERRED from the
  absence of any reader).

## K10. Graphics View 1 and Graphics View 2 (PROVEN FROM SOURCE)

- GV1: `AppGeoCeDG.newEuclidianView` creates a `GeoCeDGEuclidianView` with a
  `GeoCeDGEuclidianController` (`geocedg/AppGeoCeDG.java:1280-1288`).
- GV2: `GuiManagerD.getEuclidianView2` → `GuiManager3D.newEuclidianView(…, viewId)`,
  which returns a plain `EuclidianViewFor3DD` with an `EuclidianControllerFor3DD`
  (`desktop/org/geogebra/desktop/geogebra3D/gui/GuiManager3D.java:257-265`);
  `GuiManagerGeoCeDG` (`geocedg/GuiManagerGeoCeDG.java:34`) does not override it.
- GV2 is not in either GeoCeDG perspective (`apps/geocedg/application-profile.yml:3176-3179`,
  `:3228-3231`) but is reachable from the Views menu entry "Graphics2"
  (`geocedg/GeoCeDGHostMenuFactory.java:48-49`), workspace layouts
  (`geocedg/GeoCeDGWorkspaceController.java:256-258`), print preview and the
  picture service.
- A `paint` overlay therefore covers GV1 only. GV2 coverage needs either a
  GeoCeDG GV2 view class (overriding `GuiManager3D.newEuclidianView` in
  `GuiManagerGeoCeDG`, a new Desktop class) or drawable-level work (§K8).

## K11. Visibility, layers and lifecycle of drawn objects (PROVEN FROM SOURCE)

- A drawable exists only if `drawableNeeded`: visible in this view, label set
  (or plot panel), `isEuclidianVisible()` (`EuclidianView.java:2229-2241`);
  `getDrawableFor` is a map lookup (`:2429-2431`).
- `DrawableList.drawAll` draws only drawables on a shown layer whose geo
  `isDefined()` (`common/euclidian/DrawableList.java:83-99`); each drawable
  then checks `isEuclidianVisible()` and on-screen clipping; `DrawSegment` skips
  thickness 0 (`DrawSegment.java:390-392`; polygon edges may have thickness 0,
  `GeoSegment.java:251-255`).
- Hidden layers: `EuclidianView.isOnShownLayer` → `app.isLayerShown(layer)`
  (`EuclidianView.java:3683-3685`) → `AppGeoCeDG.isLayerShown`
  (`geocedg/AppGeoCeDG.java:631-633`) → `GeoCeDGLayerWorkspace.isLayerShown`
  (`geocedg/GeoCeDGLayerWorkspace.java:85-87`), document-wide; hit testing uses
  the same filter.
- Auxiliary objects are drawn normally in the Graphics View (no auxiliary
  check in `drawableNeeded` or `drawAll`).
- Axes are `GeoClass.AXIS` (`common/kernel/geos/GeoAxis.java:130-133`), which
  `EuclidianDraw` does not map to `DrawLine` (`common/euclidian/EuclidianDraw.java:154-170`
  maps `SEGMENT(3D)`, `RAY(3D)`, `LINE(3D)`); lines inside a `GeoList` are drawn
  by `DrawList` and are not top-level drawables (INFERRED).
- Only `DrawLine` exposes `isVisible()` (`DrawLine.java:791-793`).
- Best overlay predicate (INFERRED from the pieces above): iterate the view's
  drawable list (`EuclidianView.getAllDrawableList`, `:1844`), take only
  `GeoLine`, `GeoSegment`, `GeoRay` (2D) geos with a drawable, require
  `isDefined()`, `isEuclidianVisible()`, `app.isLayerShown(layer)`, thickness
  > 0, finite non-zero `getDirection`, a non-empty visible chord, and the
  route admissibility of `DQ-F1-5`.
- Lifecycle: on New, Open, undo, redo and XML load the construction is rebuilt
  and drawables are recreated; a `paint` overlay recomputes from the current
  drawables on the next repaint and holds no object reference (INFERRED; same
  pattern as the export-area overlay).

## K12. Screen transform and placement (PROVEN FROM SOURCE; placement HYPOTHESIS)

- `toScreenCoordXd = xZero + x·xscale`, `toScreenCoordYd = yZero − y·yscale`
  (`EuclidianView.java:1124-1147`); logarithmic axes are an exception
  (`:1125-1128`, `:1141-1145`, `:3373-3380`). Scales are always positive:
  `setCoordSystem` ignores NaN, `< 10⁻¹⁵` or `> 10¹⁵` scales (`:1455-1464`).
  Non-uniform scales exist (`getScaleRatio`, `:1671-1673`; locked only on
  request). There is no inverted axis in the linear case.
- A world direction `(dx, dy)` maps to the screen direction
  `(dx·xscale, −dy·yscale)`, exactly as `DrawRay` already does. This is a
  placement operation; it never chooses the sense.
- Labels: segment label at the midpoint plus 16 px along a normal
  (`DrawSegment.java:178-195`), where `DECORATION_SEGMENT_ONE_ARROW` also sits;
  ray label at `start + v/2 + 16 px` normal (`DrawRay.java:159-176`); line label
  near a clipped end at the view border (`DrawLine.java:361-415`).
- Evaluation of the P0 placement (HYPOTHESIS, details in the reconciled design
  §5): segment midpoint collides with the segment label side and with the
  one-arrow decoration; ray "near the start" is undefined when the start is off
  screen; line "visible point nearest the view centre" must be computed in
  screen space and clamped to the visible chord, because the foot of the
  perpendicular from the centre can lie outside the view when the line only
  cuts a corner. Logarithmic axes make straight lines curves on screen; the cue
  must be suppressed there.

## K13. Preference infrastructure and lifecycle (PROVEN FROM SOURCE)

- Store: `GeoCeDG.main` sets `%APPDATA%\GeoCeDG\5.4\preferences.properties`
  (or `~/GeoCeDG/5.4/`) (`geocedg/GeoCeDG.java:24`, `:45-55`, `:77-83`); with a
  file path `GeoGebraPreferencesD.getPref()` returns
  `GeoGebraPortablePreferences` (`desktop/org/geogebra/desktop/main/GeoGebraPreferencesD.java:146-149`),
  one static `java.util.Properties` (`GeoGebraPortablePreferences.java:73`),
  written by `storePreferences()` on window close or Options → Save Settings
  (`:108-120`, `:228`, `:256`).
- GeoCeDG keys follow `geocedg.<area>.<name>.v1`: presentation sizes
  (`geocedg/GeoCeDGPresentationPreferences.java:23-29`), theme
  (`GeoCeDGThemePreference.java:20`), units (`GeoCeDGUnitPreferences.java:20-24`),
  dimension thickness (`GeoCeDGDimensionPreferences.java:20`), navigation
  (`GeoCeDGNavigationShortcutPreferences.java:26-30`). No `geocedg.view.` key
  exists. The closest boolean precedent stores `"shown"`/`"hidden"`
  (`GeoCeDGUnitPreferences.java:25-26`, `:114-120`); invalid values read as the
  default without writing back.
- Test seams: `useStoreForTesting` on units and dimensions; sizes and theme are
  built from `systemStore()` at app construction (`geocedg/AppGeoCeDG.java:1466`,
  `:1479`).
- Existing view toggles (export-area overlay, intersection markers,
  auto-materialize) are `SESSION`: never serialized, reset on New/Open
  (`geocedg/AppGeoCeDG.java:840-846`; `geocedg/export/ExportAreaSession.java:26-29`,
  `:290-296`; `geocedg/GeoCeDGEuclidianController.java:1024-1042`).
- New: `AppD.fileNew` clears the construction and reloads the preferences
  XML (`desktop/org/geogebra/desktop/main/AppD.java:1151-1179`); the GeoCeDG
  override applies unit defaults and `setSaved()` (`geocedg/AppGeoCeDG.java:1171-1192`).
  Open and XML loads reset `ExportAreaSession` (`:1213-1239`). A flat
  `geocedg.*` key lives outside the preferences XML and the document XML
  (`GeoGebraPortablePreferences.java:237-257`), so it cannot leak into a
  document; a value stored in `EuclidianSettings` or the view XML would
  (INFERRED from the theme precedent, `geocedg/AppGeoCeDG.java:1421-1428`).
- Multiple windows share the static `Properties`; each `AppGeoCeDG` caches its
  own preference objects, so a live change must repaint every window
  (INFERRED).
- Preferences UI: Layout & Presentation is assembled by `OptionsLayoutD.initGUI`
  (`desktop/org/geogebra/desktop/gui/dialog/options/OptionsLayoutD.java:131-153`)
  through `AppD` product hooks (`AppD.java:2839-2874`); a new panel needs a new
  hook in these upstream files, or a row inside an existing GeoCeDG panel
  (INFERRED).

## K14. Action catalog and pinned gates (PROVEN FROM SOURCE)

The profile holds **130** actions (`apps/geocedg/application-profile.yml`;
`geocedg/GeoCeDGProfile.java:340-346` throws otherwise), 5 of kind
`view-toggle`. The precedent toggle `export.area.show` is a `product-action`
with `effect_profile_id: preference-only` in File group `file-export-area`
(`application-profile.yml:2404-2419`, `:3469-3475`); axes and grid toggles are
in Options group `options-product`. A new action requires: the `TARGETS`
entry, `execute()` and `checked()` cases in `geocedg/GeoCeDGActionRegistry.java`
(`:54-80`, `:284-470`, `:489-518`); the count 130 in `GeoCeDGProfile.java:340`
and in eleven test assertions of eight classes (`desktop` test tree):
`G9U1ActionRegistryTest.java:65`, `G9U1ProfileCompilerTest.java:44,212,384`,
`G9U1WorkspaceSurfaceTest.java:124,704`, `GeoCeDGProfileTest.java:45`,
`PreG9BR6PlusA1LayerWorkspaceTest.java:450`,
`PreG9BR6PlusE1LCuratedLibraryTest.java:380`,
`PreG9BR6PlusE2NativeDimensionDesktopTest.java:423`,
`PreG9BR6PlusE3IsoABorderDesktopTest.java:407`; a new approved-id block in
`tools/agent/workspace-profile-validation.ps1:36-93` and the re-pin of its
input hash in `geocedg/specs/operations/verification-static-contracts.json:21`
and the `static_contracts` registry pin; the File-menu order list
(`G9U1WorkspaceSurfaceTest.java:138-148`) only if placed in File. A preference
set only in the Layout & Presentation panel avoids every catalog change
(INFERRED).

## K15. Reconciliation with the P0 F1 candidate

| P0 statement | At `bde2827b` |
|---|---|
| authority `getDirection` (`GeoLine.java:443-447`) | confirmed; lines `:444-457` |
| `getDirectionInD3` reversed without end point (`:1402-1409`) | confirmed on every probed route |
| path parameters agree (`GeoLine.java:1140-1162`, `GeoSegment.java:422-452`, `GeoRay.java:179-243`) | confirmed by probe; current segment range `:401-452`, ray `:168-243` |
| `UnitVector` / `Direction` one algorithm (`AlgoUnitVectorLine.java:39-40`; factory `:64-67`) | confirmed; `:38-41` |
| oriented `Angle(g,h)` (`AlgoAngleLines.java:111-117`) | confirmed; `:106-118` |
| parallel to a linear function `(−1, −m)` | confirmed (R14); opposite to `Tangent(a, f)` (R13), new |
| `Tangent(P, conic)` on the conic UNKNOWN | **established**: polar sense, depends on the conic equation sign (R12b–d) |
| equation line INFERRED `(1, 2)` | **established**: left − right; same line typed differently reverses (R15–R15h) |
| `AngleBisector` "may flip for continuity" | in GeoCeDG continuity is forced off; the flip occurs through the straight angle (D16–D18); three-point form symmetric in its outer points (R11) |
| overlay `GeoCeDGEuclidianView.java:38-75` | stale; `paint` is `:69-112`, now also validating the ISO link and drawing the export-area overlay |
| not part of `exportPaint` | confirmed and extended to every route (§K8) |
| GV2 is `EuclidianViewFor3DD` (`GuiManager3D.java:257-265`) | confirmed |
| reuse `ARROW_OUTLINE` geometry (`DrawSegmentWithEndings.java:261-290`) | geometry at `:282-312`; private, not reusable as-is (§K9) |
| `USER_PREFERENCE` key `geocedg.view.orientation-cue.v1` | consistent with the key convention; not implemented; no `geocedg.view.` area exists |
| not shown for undefined, zero-length or non-finite lines | required, and `isDefined()` does not cover zero-length segments or infinite coefficients (D02, D09) |
| `BOUNDED_PHASE` | still the smallest fitting class for a GV1, screen-only overlay without catalog change (§K16) |

New facts not in P0: the representation class (§K6), the tangent-from-point
discontinuity (D12–D15), the GeoPoint `z ≥ 0` dependency of the cross-product
routes, the adjoint proof for affine images, macro behavior, the 130-action
catalog and its pins, the preference test seams, the label collisions.

## K16. Verification planning facts

- The preparation is `DOCUMENTATION_STATUS_ONLY` with `STATIC` acceptance; the
  phase prompt is not in the prompt-contract catalog, so it is validated
  separately with `Test-PromptContractDocument` (profile `task`).
- An `F1` limited to a GV1 `paint` overlay, a Desktop preference and its UI
  row, with no kernel, serialization, export, catalog or upstream drawable
  change, fits `BOUNDED_PHASE` with one registered `PHASE`
  (`verification-levels.md` §12.8). Each of the following moves it out of that
  envelope and needs an author disposition: a new profile action (catalog pins
  and a static-contract re-pin; still bounded but with infrastructure inputs);
  a new `OptionsLayoutD`/`AppD` hook (upstream files and
  `docs/upstream/modified-files.yml`); GV2 coverage through a new view class;
  drawable-level changes (shared code, export reach); export participation;
  per-object persistence (serialization, `GLOBAL_IMPACT`).

## K17. Files changed by the preparation

```text
A  docs/validation/pre_g9b_r6_plus_f1_preparation_characterization_report.md
A  docs/validation/pre_g9b_r6_plus_f1_author_decision_preparation_record.md
A  docs/architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md
A  geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f1-preparation-characterization.json
A  .github/prompts/tasks/pre-g9b-r6-plus-f1-orientation-cue.prompt.md
M  docs/architecture/pre_g9b_r6_plus_minitrack_plan.md   (status lines only)
M  docs/roadmap/geocedg_roadmap.md                        (documental version, F1 row)
```

No product, test, build, registry, verifier, profile, inventory, schema or
serialization path; the `P0` F1 candidate and every historical record are
unchanged.

```text
AUTHOR_DECISION  = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved     = false
```

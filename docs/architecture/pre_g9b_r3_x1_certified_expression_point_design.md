# PRE-G9B-R3-X1 certified expression-point construction model

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PHASE                     = PRE-G9B-R3-X1
KIND                      = CAPABILITY EXTENSION (certified-construction-program/v2)
                            plus two Ri-conditional evaluator correctness repairs
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozenAtPhaseStart = true)
PLANNED_ACCEPTANCE_LEVEL  = FINAL
IMPLEMENTATION_BASE       = 78134aa8bc6ac30b65ffd916713fd3ca0502e6c6
                            (tree f23218cc0b7c7189aae69489a1ce7a5ac5ee9fba)
selfApproved              = false
```

This is the bounded design record that the author's `PRE-G9B-R3-X1` instruction
of 2026-09-29 requires before productive implementation. It records facts
established from the live repository at the implementation base and the design
derived from them. It carries no author-approval field; the separate
author-decision record is the sole authority for approval.

Normative outputs:

| Artifact | Role |
|---|---|
| [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §2, §3, §5.1, §5.2, §8, §9 | the X1 amendment, contract version 1.0 → 1.1 |
| [ADR 0028](../adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md), "Amendment — PRE-G9B-R3-X1" | the architectural decision extending Decision 3 |
| [`spline-v2-pair-materialization.md`](../../geocedg/specs/curves/spline-v2-pair-materialization.md) §10 | one clause: the admitted construction model is class v1 or v2 |
| [validation matrix](../validation/pre_g9b_r3_x1_validation_matrix.md) | every obligation mapped to executable evidence |

The design stays inside the author's authorized envelope: no arbitrary
coordinate-expression certification, no new semantic root identity, no selector
or token change, no persisted certificate data, no migration, no new global
completeness claim, no new certifier, no new tolerance policy and no identity or
provenance inferred from values.

## 1. Characterization on the exact base

Scratch probes (kept in the session scratchpad, not in the tree) ran on
`78134aa8…` with the witness driver `C=Point(c)` on `c=Circle(A,B)`,
`A=(0.78,2.24)`, `B=(2.56,4.78)`, and the spline `d=SplineV2({F,G,H},3)`.

| Case | Parent of the traced point | Isolated definition | Program | Pair `Intersect(d,·)` |
|---|---|---|---|---|
| X1-P1 `E=C+(1,0)` | `AlgoDependentPoint` | `PLUS[GeoPoint C, MyVecNode(cartesian; 1; 0)]`, both literal components `MySpecialDouble` | none | 2 roots, 0 admissible, `NOT_ESTABLISHED`, ledger version `4` (no pair token), `CAPABILITY_NOT_AVAILABLE: … no certified interval curve model` |
| X1-P2 `u=(1,0)` (a `GeoVector`), `E=C+u` | `AlgoDependentPoint` | `PLUS[GeoPoint C, GeoVector u]` | none | same boundary |
| X1-CONTROL `E=Translate(C,u)` | `AlgoTranslate` | — | `certified-construction-program/v1 … TRANSLATE(n0)[1,0]` | 2 roots, 2 admissible, `UNIQUE` germs `±1`, ledger `5` |
| `E=Midpoint(D,C+(1,0))` (R3 witness form) | `AlgoMidpoint` with an unlabeled `AlgoDependentPoint` input | as P1 for the input | none | rich-only, same diagnostic |
| `E=(1,0)+C`, `E=u+C` | `AlgoDependentPoint` | `PLUS[vector, point]` | none | rich-only |
| `E=C-(1,0)`, `E=C-u` | `AlgoDependentPoint` | `MINUS[point, vector]` | none | rich-only |
| `(x(C)/2,y(C))`, `C+2u`, `C+u+u`, `C+(1,0)+(0,1)`, `C+A`, `C+(x(A),0)`, `C+(1/3,0)`, `C+(1;0)` | `AlgoDependentPoint` | nested, scaled, point + point, non-literal or polar components | none | rich-only |

The floating evaluator values of P1, P2 and the control agree bit for bit at every
probed parameter (for example `t=-2.5 → (-0.7048370393404311, 0.38377132660820035)`),
as do `(1,0)+C` and `u+C`. This agreement is recorded as validation evidence
only; it establishes neither certification nor identity.

The current behaviour is the intentional v1 fail-closed boundary:

```text
unsupported construction shape -> no certified program -> CAPABILITY_NOT_AVAILABLE
  -> rich-only pair result -> no pair token -> materialization inadmissible
```

## 2. GeoGebra semantics of the expression point

The authority is the upstream formula sequence, read from source:

1. `AlgoDependentPoint.compute` evaluates its definition to a `VectorValue`,
   takes `getVector()`, sets the point undefined if either coordinate is
   infinite, and otherwise calls `P.setCoords(x, y, 1.0)`. The output is a
   unit point: `updateCoords` sets `inhomX = x`, `inhomY = y` because `z == 1`.
2. `ExpressionNodeEvaluator.handlePlus` (vector + vector) computes
   `GeoVec2D.add(lt.getVector(), rt.getVector())`, i.e. `(lt.x + rt.x, lt.y + rt.y)`;
   `handleMinus` computes `GeoVec2D.sub`, i.e. `(lt.x - rt.x, lt.y - rt.y)`. The
   Cartesian/complex print mode never changes the values. The 3D evaluator
   (`ExpressionNodeEvaluator3D`) intercepts only `Vector3DValue` operands; 2D
   points, vectors and `MyVecNode` literals fall through to the same 2D code.
3. `GeoPoint.getVector()` returns `(inhomX, inhomY)`; `GeoVector.getVector()`
   returns `(x, y)`; `MyVecNode.getVector()` in Cartesian mode returns
   `(x.evaluateDouble(), y.evaluateDouble())` and in polar mode `r cos φ, r sin φ`.

Hence, for a finite point `P` and a vector `V`:

```text
P + V  = (PX + vx, PY + vy, 1)
V + P  = (vx + PX, vy + PY, 1)   identical: real addition is commutative, and so is
                                 IEEE binary64 addition, bit for bit
P - V  = (PX - vx, PY - vy, 1)   identical to P + (-V): IEEE 754 defines x - y as
                                 x + (-y), and negation of a binary64 value is exact
```

`V - P`, `P + P'`, `k V`, nested sums and non-literal vector components are
different formulas and are outside the grammar.

## 3. Bounded v2 grammar

A driver-dependent geo is an **expression-point translation** when all of the
following hold on the evaluator's isolated slice:

1. its parent algorithm has the exact class `AlgoDependentPoint`, with one output;
2. its definition, after removing `NO_OPERATION`/leaf wrappers (the evaluator
   returns the wrapped value unchanged), is one binary node with operation
   `PLUS` (either operand order) or `MINUS` (point on the left);
3. the point operand, after the same unwrapping, is a `GeoElement` of exact class
   `GeoPoint` that is driver-dependent (the isolated state or a descendant); it is
   captured as a node by the ordinary recursive walk;
4. the vector operand, after the same unwrapping, is either
   - a `GeoElement` of exact class `GeoVector` that is **driver-independent** and
     defined, captured as a constant `(x, y)` at the maximal driver-independent
     boundary, exactly as the v1 `TRANSLATE` vector; or
   - an inline `MyVecNode` literal in Cartesian mode (`COORD_CARTESIAN`, no polar
     coordinates) whose two components each unwrap to a numeric literal node of
     exact class `MyDouble` or `MySpecialDouble` (never `FunctionVariable` or any
     other subclass) with a finite value, captured as `getVector()` computes it;
5. both captured components are finite.

Every other shape yields no program: expression trees with more than one
operation, number operands, point + point, `V - P`, scaled or driver-dependent
vectors, literal components that are expressions or reference any geo, polar or
3D literals, `GeoVector3D`, and any other algorithm. The existing negative
fixture `(x(C)/2,y(C))` is a `MyVecNode` definition without a sum and stays
outside.

### Operand provenance

The point operand is captured only through the isolated reconstruction: it must
be the isolated state or its descendant, reached by the same walk as every v1
operand. A named vector (`u`) is the isolated copy of the source object, read by
its stored fields at the driver-independent boundary. An inline literal is read
only as an explicit literal node of the isolated expression tree. A literal and
a named object are never identified because their values match; the program
records values, not identities, and never needs to decide whether two operands
are "the same".

## 4. Normalization onto the existing `TRANSLATE` semantics

The v1 `TRANSLATE` formula is `(Px + ux Pz, Py + uy Pz, Pz)` followed by the
§5.1 normalization. A direct relabeling of the expression step as `TRANSLATE(P, u)`
is **not** exact: `TRANSLATE` keeps the homogeneous triple of `P`, while the
expression point always produces the unit triple `(PX + ux, PY + uy, 1)`. After a
line meet, for example, `Pz ≠ 1`, and the two triples differ by a scale factor;
GeoGebra's downstream formulas are scale covariant but their predicates are not
(line definedness `|a| ≥ ε`, the perpendicular power-of-two normalization count,
point finiteness thresholds). A relabeled program would certify a formula
sequence the evaluator does not run.

The exact normalization uses the unit reading of the point operand that
`GeoPoint.getVector()` performs:

```text
EXPRESSION_TRANSLATE(P, u) := TRANSLATE(VEC(P), u),   VEC(P) = (PX, PY, 1)

with u = V for P + V and V + P, and u = -V for P - V.
```

`VEC(P)` is a unit point, so `TRANSLATE(VEC(P), u)` is `(PX + ux·1, PY + uy·1, 1)`,
the §5.1 unit short-circuit applies, and the result is exactly
`(PX + ux, PY + uy, 1)`, the expression point. The interval model evaluates this
by passing the unit reading of the already computed point jet to the existing
`TRANSLATE` code; no new interval formula is introduced. When `P` is itself a unit
point (the driver, a midpoint, a similarity image of either), `VEC(P) = P` and
the step coincides with v1 `TRANSLATE`.

The step is still recorded as its own operation, `EXPRESSION_TRANSLATE`, so that
the program records that the expression-point grammar was used (section 5) and so
that the TRANSLATE-of-`VEC` definition cannot be applied to an `AlgoTranslate`.

## 5. Program versioning

| Program | Version | Signature |
|---|---|---|
| every step is a class-v1 operation (all existing programs, `Translate(C,u)`) | `certified-construction-program/v1` | unchanged, byte for byte |
| at least one `EXPRESSION_TRANSLATE` step | `certified-construction-program/v2` | begins with the v2 identifier |

The version is derived from the steps; no existing program is relabeled. The
program stays ephemeral certificate material: it is recaptured on every
certification, never persisted, and never a selector, token, ledger field or
document identity. No document changes, so no migration exists or is needed.

## 6. Interval semantics and degeneracies

| Case | Treatment |
|---|---|
| undefined vector object | capture refuses (constant must be defined) |
| nonfinite vector component (object or literal) | capture refuses (every captured constant must be finite) |
| undefined or infinite point operand on a box | the point operand's own §5.1 normalization refuses the box; unresolved |
| infinite output | impossible: the output is a unit point (`z = 1`), as GeoGebra sets it |
| ambiguous expression structure | only the one binary node of section 3 is admitted; anything else refuses |
| driver-dependent vector | capture refuses (the vector must be a constant) |
| polar, complex-number or 3D literal | capture refuses |
| divisor or predicate uncertainty | none introduced: the step has no divisor and no branch predicate |
| interval overflow or nonfinite bound | the shared outward arithmetic refuses the box; unresolved |
| derivative enclosure | forward-mode jets: `d(PX + ux)/dt = dPX/dt`, unchanged arithmetic |

No tolerance is introduced or changed. The floating verification of every
certified root is unchanged.

## 7. Evaluator coherence and the two retained evaluator debts

The certified model is exact only against a correct floating evaluator. The
author's Ri policy requires both evaluator debts to be inspected at X1 entry.
Both lie in `ReconstructibleLocusEvaluator2D`, the evaluator whose isolated slice
the X1 capture reads, and both were characterized to their mechanism on the base.

### 7.1 `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE`

**Reproduction** (`sa=Segment((0,0),(4,0))`, `Cs=Point(sa)`, `K=(0,2)`,
`Ms=Midpoint(Cs,K)`, `ls=LocusV2(Ms,Cs)`; the expected curve is `(2t, 1)`):

```text
t = 0.1 -> (1.99609375, 1)   expected (0.2, 1)
t = 0.25 -> (0.2, 1)         the value for the previous parameter
t = 0.5 -> (0.5, 1), t = 0.75 -> (1.0, 1), t = 0.9 -> (1.5, 1), ...
Pa = Point(ls, "generator.main", 0.25) -> (2, 1), Pb at 0.75 -> (0.5, 1)
LocusLength(ls, Pa, Pb) = 2.25   expected 1   (the author's recorded manifestation)
LocusLength(ls) = 3.5            expected 2
```

The wrong values depend on the previous evaluation, so they are history
dependent; the numbers above are the exact probe order.

**Mechanism.** The normal `AlgoPointOnPath.compute` runs
`path.pathChanged(P); P.updateCoords();`. `GeoSegment.pathChanged` writes the
new position through `setCoords2D`, which does not refresh the inhomogeneous
coordinates, and relies on that `updateCoords()`. Conic paths refresh them
inside `updateCoordsFrom2D(false, getCoordSys())`, so circle and arc drivers are
unaffected. `ReconstructibleLocusEvaluator2D.applyCoordinate` calls
`pathChanged` and `updateCascade` but never `updateCoords`. The next
`resetExternalState` recomputes `AlgoPointOnPath`, which refreshes the
inhomogeneous coordinates to the *previous* parameter, and the new cascade reads
them. Slices that read only homogeneous coordinates (a line through the driver
parallel to another line, for example) are unaffected.

**Repair.** `applyCoordinate` performs the normal update path of
`AlgoPointOnPath`: `pathChanged(point)`, then `point.updateCoords()`, then the
cascade. For circle and arc drivers `updateCoords` recomputes the identical
values (idempotent on a normalized point), so their evaluations are unchanged
bit for bit. The certified `SEGMENT` driver formula already modeled the correct
point, so no certification rule changes. The coherence gate is not weakened: it
now accepts the segment roots because the floating evaluator is correct.

### 7.2 `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY`

**Reproduction** (the retained fixture): `lower=CircularArc((0,-1),(-1.5,-1),(1.5,-1))`,
`Cj=Point(lower)`, `gj=Line(Cj,(0,3))`, `gp=PerpendicularLine((0,0),gj)`,
`Ej=Intersect(gj,gp)`, `literal=LocusV2(Ej,Cj)`. On the base the locus is created
undefined, because the isolated replay is rejected with
`MALFORMED_RECORD: Construction identity dependencies disagree with the
prospective algorithm DAG` (since `PRE-G9B-R3`; before it, the replay evaluated
one fixed point for every parameter).

**Mechanism.** The evaluator gathers its slice into a `TreeSet<ConstructionElement>`
ordered by the elements' natural order. That order is not a strict total order:
`ConstructionElement.compareTo`, used by geos, compares creation ids, while
`AlgoElement.compareTo` compares construction indices. An unlabeled inline literal
such as `(0,3)` is outside the construction list (index `-1`) but was created
after the driver's `AlgoPointOnPath` (creation id `55` versus `53`), so the two
orders disagree for that pair and the tree becomes inconsistent.
`Macro.buildMacroXML` then re-adds the whole closure into the caller's set
(`macroConsElements.addAll(closureElements)`, a GeoCeDG G9 addition; the upstream
baseline `9b93256b…` never re-inserts). A re-inserted `AlgoPointOnPath` navigates by
construction index past the literal node, misses its own node and is inserted a
second time. The macro XML then contains the `Point(lower)` command twice, the
isolated construction holds two copies of the driver, `gj` binds to the second
copy while the durable identity resolves to the first, and the traced point no
longer depends on the evaluator's driver. With a single literal the inconsistent
pair may or may not be crossed, which is why one literal was observed to work.
Classic `Locus` builds a different element set and was probed unaffected
(`Locus(Ej,Cj)` spans `x ∈ [-0.986, 0.986]`, as its named-point variant).

**Repair.** The evaluator builds its slice set with an explicit strict total
order: construction index, then creation id. This is the construction-list order
that GeoGebra itself uses to write a construction, and it coincides with
`AlgoElement`'s own order. Re-insertion by `Macro.buildMacroXML` then finds every
element, each element is written once, and the isolated replay keeps its driver.
`Macro` is not modified. The repair was verified in the probe: one driver copy, a
clean isolated load and a traced point that depends on it.

### 7.3 Ri disposition

| Ri condition | `SEGMENT-DRIVER-UPDATE` | `INLINE-LITERAL-DEPENDENCY` |
|---|---|---|
| same evaluator/certification subsystem already modified by X1 | yes: `applyCoordinate` of the evaluator whose slice X1 captures; X1's new operation reads exactly the inhomogeneous point coordinates the defect leaves stale | yes: the isolated slice construction of the same evaluator |
| does not broaden X1's scientific purpose | yes: no new capability; existing floating evaluation made correct | yes |
| no independent architectural decision | yes: reproduces the existing `AlgoPointOnPath` update contract | yes: restores a strict total order using GeoGebra's construction-list order; `Macro` untouched |
| shares X1's required verification | yes: same focal classes and the same `FINAL` | yes |
| corrects the evaluator, not the certifier | yes | yes |

```text
TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE     = RESOLVED_IN_X1
TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY = RESOLVED_IN_X1
PRE-G9B-R3-X2                                = NOT NEEDED (conditional planning not activated)
```

**Compatibility decision (AGENTS §11.6).** Both repairs change floating results
of existing constructions from wrong to correct: segment-driven loci that read
the driver's inhomogeneous coordinates, and slices with inline literals that the
defect disconnected. No value, sample, certificate or evaluator state is
persisted, so documents are recomputed on load and no migration exists. The
author authorized both repairs inside X1 under the Ri conditions; the regression
evidence is the focal tests of the validation matrix, which fail on the extracted
base and pass on the candidate.

**Observation, not a debt.** `OBS-X1-MACRO-BUILDXML-REQUIRES-CONSISTENT-SET-ORDER`:
`Macro.buildMacroXML` re-adds its closure into the caller's set and is correct only
when that set's order is a strict total order. The evaluator now supplies one; the
other callers (user tools, classic `Locus`) were not observed to fail, and no
generic `Macro` change is made.

## 8. Lifecycle

| Situation | Behaviour |
|---|---|
| ordinary recompute | the program is recaptured; the unchanged selector is re-certified; zero new points |
| compatible redefine into supported syntax (R3 witness `E := Midpoint(D,C+(1,0))`) | R3 compatible retain, unchanged; `E` keeps its identity; the locus recomputes; the pair stays certified, so a point already materialized from it keeps its token and follows the new root |
| compatible redefine between supported vectors | same selector re-certified on the new curve |
| redefine into an unsupported expression | the program is refused; the pair becomes rich-only with its diagnostic; the materialized point becomes dormant and undefined, never retargeted |
| undo / redo | restore the construction; the program is recaptured |
| native save / reopen | no certificate data in `.cedg`; the program is recaptured on load |
| copy / remap | the closure copy rebuilds the expression point from the copied driver; literal nodes stay literals; a named vector is copied as an ordinary predecessor and referenced by the copy; tokens and selectors follow the existing E0/E1 remap rules |

The R3 durable-frontier semantics, the C1/C1-U1 clipboard rules and the E0/E1
identity chain (selector, canonical ordering, contract identifier, ledger v5
binding, quarantine, dormancy, reactivation, exact-token materialization) are
unchanged.

## 9. Pair-materialization and metric boundary

```text
Locus evaluator -> certified interval model -> existing pair certifier
  -> verified local transverse singleton -> existing selector/token -> materialization
```

The certifier, resolver, selector, token and ledger code is consumed unchanged.
`local admissibility != global completeness`: a newly certified root does not
change completeness, which stays `NOT_ESTABLISHED` where it was. No metric
endpoint family is added; a newly materializable root reaches `Length`/`LocusLength`
only through the existing pair-intersection occurrence family. The traced
expression point of a locus keeps addressing that locus through the existing
generator occurrence, exactly as any traced point does; any other expression
point addresses nothing (`NO_ADDRESS`), as before.

## 10. Rejected alternatives

| Alternative | Reason |
|---|---|
| admit `AlgoDependentPoint` by class | forbidden: capability, not class name |
| relabel the expression as `TRANSLATE(P, u)` | not exact when `Pz ≠ 1` (section 4) and indistinguishable from v1 |
| `MIDPOINT(P,P)` followed by `TRANSLATE` | exact but artificial, wider enclosures and misleading provenance |
| a general interval evaluator for GeoGebra expression trees | outside the envelope; arbitrary expression certification is forbidden |
| accept point + point (`C + A`) | the operand is a point, not a vector; not in the authorized grammar |
| relabel every program as v2 | would change every existing signature without cause |
| hide the evaluator debts in the certifier | forbidden; the evaluator is corrected |
| fix debt B inside `Macro.buildMacroXML` | changes generic `Macro` code shared with user tools and classic `Locus` |
| drop inline literals from the evaluator slice | treats the trigger, not the inconsistent order; still exposed to other discordant pairs |

## 11. Canonical prompt and verification

No durable task prompt is created: governance does not require one for a phase
executed from an explicit author instruction, as for `PRE-G9B-R3-C1` and
`PRE-G9B-R3-C1-U1`, and `CLAUDE.md` forbids editing `.github/prompts/**` as a side
effect. The class `GLOBAL_IMPACT` was frozen before productive edits: shared Locus
V2 kernel semantics, certified interval capability, pair admissibility and
materialization, and two evaluator corrections that change floating results.
Acceptance is one `FINAL` campaign on the exact frozen candidate.

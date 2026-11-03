# CU7 R3 — Local projection continuation details

2026-11-03. Technical implementation details under the previously recorded
[integrated R3 candidate](pre_g9b_cu7_general_point_interaction_r3_candidate.md).
Candidate only; no accepted ADR, R6/H1 guarantee or author approval is changed.

## Mathematical claim and initial correspondence

The optional `LOCAL_STRICT_PROJECTION_CHART` consumes a faithful captured v3
construction program. This includes captured v1/v2 programs. It reuses the
existing outward construction evaluator, validity predicates, inherited tolerance
guards, first/second jets and captured raw intersection selectors. No operation
or evaluator specific to LINE_CIRCLE, a segment, an arc or an example is added.

For a parameter interval J and target q, define

    g(u,q) = (F(u)-q) dot F'(u)
    gu(u,q) = ||F'(u)||^2 + (F(u)-q) dot F''(u).

Every accepted chart proves valid construction predicates, one uniform captured
selection, positive squared speed and `gu >= mu > 0` on all of J and the whole
target segment. At internal chart boundaries, strict opposite g signs establish
existence and uniqueness for every target on that segment. Since g and gu are
affine in q, enclosures at both target endpoints suffice for the entire segment.
Subboxes cover J with shared endpoints; every one must prove the same selector.

Initially the old exact argument a0 is the anchor, not a presumed root at the
rounded native forward coordinate. Its exact F(a0) is enclosed by a construction
run and used as the initial target endpoint. The exact root a0 is therefore known
to belong to the initial root interval. An outward interval for that image is
conservative: the proof is required for every image value in that enclosure.

After an accepted move the opaque gesture witness retains the actual floating
target q, the certified ideal-root interval, canonical representative, lift and
uniform selector. The next chart contains the whole prior root interval. It
uses the same g equation and selector; it cannot silently switch to a coordinate
inverse or the affine-circle rule after the gesture has acquired that witness.
The witness changes only after the normal atomic publication succeeds.

## Represented parameter and no-jump bound

Let B enclose `sup_J ||F'||` and let mu be the positive lower bound for gu over
the complete target segment. Strict monotonicity and the mean value theorem give

    |u1-u0| <= (B/mu) ||q1-q0||.

The corresponding bound for the stored representatives adds both represented
argument errors. The initial error is zero before any lifted-period enclosure;
subsequent errors come from the retained root interval. Periodic interval shifts
and complete canonical reduction are included outward. `Result` exposes the
resulting finite `maximumParameterChange` and the new `parameterError`; they are
NaN on historical providers that do not supply this particular evidence.
These numbers do not assert a uniform condition number for arbitrary curves.

Root bisection and a mean-value interval correction reduce the interval to at
most one quarter of the declared parameter tolerance before canonical reduction.
Grid stagnation refuses proof. The complete canonical representative must be
idempotent, belong to the same component and lie within the declared parameter
tolerance of the canonical root enclosure. The normal forward binder then
evaluates that represented argument once. The resulting point is not asserted
to be an exact orthogonal projection. Its world residual keeps
`FLOATING_POINT_UNCERTIFIED`; it is neither a certified distance nor a global
minimum or uniqueness claim.

A repeated identical target retains the exact already accepted representative
and reports zero parameter change, after fresh source/context, chart, selector,
root-containment and forward checks. The ordinary host skips mutation/snapshot
for that exact unchanged address. A nearby image from another preimage or
component never substitutes for the witnessed local root.

## Ends, seams and barriers

At an included genuine `DOMAIN_END`, the strictly monotone g equation defines
a unique constrained minimum in the closed interval. KKT signs may select the
endpoint exactly; the minimum returns continuously into the interior when the
target returns. A chart edge never becomes an endpoint. In particular,
`CERTIFIED_TRANSITION`, `UNRESOLVED_BAND_EDGE`, structural events and enclosed
safe-inner binary endpoints receive no clamp permission.

A periodic provider flag alone is insufficient. The source must have a circle
driver and an existing certifying typed component that is closed periodic or
crosses the seam. Its lifted interval/arc coordinate supplies the one component;
the model evaluates the entire lifted chart with the same uniform captured
selector. This consumes certified-construction-model section 10.8 correspondence.
The real period is enclosed by `[2*Math.PI, 2*nextUp(Math.PI)]`, so canonical
reduction includes the binary64 seam discrepancy. The resulting lift changes
only by the witnessed crossing. Untyped/unproved periodic correspondence fails.

Positive gu alone is insufficient: positive speed is separately required.
Thus folds/cusps of the map, discriminant tangencies, invalid gaps, undefined or
nonfinite jets, nonuniform selectors and unresolved predicates remain barriers.
An inventory may be globally partial while the current existing-point component
and local proof are sufficient; this does not enlarge creation permission.

## Resource contract and provider compatibility

The implementation version uses `MAXIMUM_PROOF_DEPTH=6` for adaptive whole-chart
subdivision. Radius trials alternate larger and smaller parameter neighborhoods
around the entire prior root interval. The policy refinement ceiling bounds the
number of trials and root refinements; the existing subdivisions ceiling bounds
all actual construction proof runs. Every subbox, point jet and endpoint run is
charged before evaluation. Missing evidence at these algorithmic limits is not
a theorem of geometric impossibility. Exhausting the shared work ceiling reports
the existing typed budget refusal and publishes nothing.

The existing structural affine-circle rule retains priority. Historical R6,
affine and Spline resolution retain their complete-evidence contracts. If the new
projection capability is not established for an initial event, the independent
C1 monotone-coordinate permission remains available. Once an equation is chosen
for a gesture, later events must establish that same equation. C2 is sufficient
for this optional provider only; it is not a universal condition on point motion.

Counters cover proof work and the prepared point evaluation. They do not count
arbitrary normal DAG dependents, the native restoration complement or host XML
snapshots as semantic proof evaluations. Whole-mutation timing remains separate.

## Executable regression scope

`org.geocedg.common.locus.R3PointContinuationTest` covers regular LINE_CIRCLE
selection, coincident images on disconnected components, gaps, segment/arc
motion and real endpoints, h chart changes and seam reversal, UNIT_VECTOR/DOT/
MEET, a direct certified parabola, exact repeated off-curve requests, the
represented no-jump bound near repeated images, budgets and reopen/continued
motion. `GeneralPointContinuationTest` retains the historical R6/Spline,
fold/zero-speed, stale/prepared-binding and rejection/atomicity regressions;
its former regular LINE_CIRCLE refusal is replaced by the newly authorized
positive continuation plus explicit no-component-jump and gap assertions.
The original frozen candidate and independent refusal probes stay immutable.

These are source cases pending actual R3 execution; their presence alone is not
test evidence. The effective R3 PHASE selection must include the new class.

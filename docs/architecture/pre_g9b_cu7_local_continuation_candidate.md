# CU7 — Local semantic point continuation candidate

Date: 2026-11-03 (Europe/Madrid). Status: technical normative amendment candidate,
under the author's present implementation authorization; not an accepted ADR or
author approval of the resulting implementation. `selfApproved=false`.

This amendment adds `LOCAL_SEMANTIC_CHART_CONTINUATION` independently of R6 and H1.
R6 result statuses and H1 creation proofs retain their meanings. A globally
unresolved inverse search is never reclassified as uniquely resolved.

The editable state is the existing interaction-owned point's complete semantic
address. Its current source, provider, branch, structurally resolved continuous
component, document authority and revision are required. Local continuation may
use an existing certified component in an otherwise incomplete inventory: its
proof never leaves that component, and does not claim global coverage or extend
the COMPLETE-only table for new individual forward creation. Locked points and points
with other output roles remain ineligible. A successful edit writes the dedicated
address inputs once and propagates through the ordinary DAG inside the existing
atomic construction transaction. The point and durable ID remain unchanged.
Rejection has no construction effect; the frontend stores one Undo per gesture.

Two independent sufficient local charts are candidates:

* A captured reconstructible program may prove algebraically that its output is
  an invertible affine image of its circle driver, using only typed
  operations and captured constants, never sample fitting. Inverting that affine
  map and the driver provides a single semantic argument. For a circle, a
  nonzero radial request in the open half-circle chart anchored at the current
  direction provides a unique lift; the certified closed-periodic component and
  circle driver justify seam correspondence under certified-construction-model
  §10.8 `structural-totality/v1`. This structural correspondence does not claim
  exact-real periodicity for the older v1/v2 interval model; outward enclosures
  include the binary64 seam discrepancy. The centre, hemisphere boundary and
  unproved affine/driver cases are barriers. This chart needs no general C1
  interval model. Its numerical target proximity remains uncertified.
* Existing construction interval models may certify one coordinate derivative
  has a strict sign throughout a parameter interval containing the old argument
  and the entire proposed move. That coordinate is an injective local chart.
  A bounded monotone coordinate inverse selects the new argument. Its bracket,
  including any mean-value root enclosure at an undecidable midpoint, must reach
  the declared parameter tolerance. A world residual alone never supplies that
  parameter bound. The interval
  cannot cross gaps, component boundaries or unproved periodic seams. The model
  must decide all construction predicates throughout the interval. An isolated
  nonzero derivative at either endpoint is insufficient. This route requires C1
  only for its own proof, not universally for all continuation providers.

The initial implemented provider table is deliberately closed:

| Route | Admitted source and evidence | Deliberate limits |
| --- | --- | --- |
| Historical R6 | Existing certified affine and supported piecewise-polynomial/Spline capabilities; unchanged complete inverse evidence | No weakening of historical ambiguity, singularity, degree, budget or seam guards |
| Structural affine circle chart | Direct reconstructible circle-driver capture with certified closed-periodic component; typed `VECTOR_POINTS`, constant-scalar `SCALE`, `POINT_PLUS_VECTOR`, `MIDPOINT`, `TRANSLATE`, `EXPRESSION_TRANSLATE`, `DILATE`; constants and every used intermediate must stay within this table; outward determinant excludes zero | No sample fitting, variable scalar, normalization, line-circle selection, arbitrary v3 operation, segment/arc driver or new similarity/composition adapter |
| Monotone interval chart | Direct reconstructible source with an existing v1/v2 interval model, same certified continuous component, one fixed coordinate with strict-sign whole-chart derivative and uniformly decided predicates | No unproved seam, gap, fold, zero derivative, unresolved predicate or parameter representative outside its declared error bound |

The initial general-chart implementation does not add providers for arbitrary
finite evaluators or new R5 composition chains. A locally certified component
is sufficient for an existing-point chart even when the inventory is partial;
that permission does not enlarge the separate COMPLETE-only new-creation table.

The host creates one opaque kernel context on mouse press and retains it for
the whole gesture. It binds the actual point instance, source definition and
last accepted address. The first accepted monotone chart fixes its coordinate
for that gesture; later events never change axes to reinterpret an off-curve
request. The chart may slide only after a fresh whole-interval proof with that
same coordinate. At a chart boundary the movement stops; releasing and starting
a new gesture may select a new chart. This prevents identical off-curve mouse
requests from oscillating between coordinate inverses near an axis-choice tie.
No new four-argument general move API is provided: publication requires the
gesture context. Standalone resolution is read-only proof/discovery and cannot
publish its prepared binding without that context. Historical R6 API remains.
Any failed new-chart continuation stops that context until a new press; a later mouse
event cannot silently resume after a gap in demonstrated continuation. A
post-publication source/authority/identity check runs inside the atomic host
transaction, so a synchronous observer cannot leave a rejected edit published.
An exactly unchanged address consumes its checked preparation without taking
a mutation snapshot or emitting a redundant DAG update/Undo change.
Historical R6/Spline refusals retain their previously admitted failed-event to
valid-event recovery; this amendment does not retroactively impose the new
chart barrier policy on their complete inverse evidence. Historical work-limit
failures retain their typed budget diagnostic.

The affine-circle angular representative is computed from the request and
captured coefficients alone, in a principal-angle enclosure. The previous
address authorizes the hemisphere and selects the lift, but does not influence
rounding of that representative. Identical requests therefore retain the same
canonical bits on the same runtime across a legitimate seam and its reversal.
Corner enclosures use the Java [`Math.atan2` accuracy contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html#atan2(double,double)), widened
outward by three adjacent doubles. The nominal representative uses reproducible
fdlibm `StrictMath.atan2`, and is accepted only when contained in that rigorous
enclosure. No accuracy claim for `StrictMath` is inferred without this check.
The chosen address bits are persisted and forward reconstructed under the
existing contract; identical model/request/runtime inputs give the same bits.
The complete nominal-to-canonical reduction is bounded outward, including the
first provider rounding and any exact-period seam correspondence. Its error
and the inverse enclosure together fit the declared parameter tolerance. The
chosen canonical argument itself must still satisfy a strict hemisphere proof;
proving that property only for the ideal request direction is insufficient.

Existence and same-component membership do not prove inverse uniqueness.
Injectivity is local to the selected chart; global uniqueness is unnecessary.
The mouse supplies a transient request, not identity. Residual distance is
explicit `FLOATING_POINT_UNCERTIFIED`; it is not a certified ideal-distance
bound, minimum, STRICT incidence or proof of inverse uniqueness.

The mathematical no-jump statement is local and error-controlled. In a closed
monotone chart J, the interval proof establishes `m = inf_J |F'_k| > 0`.
The mean value theorem then gives `|u(q)-u(q')| <= |q_k-q'_k|/m` for the two
coordinate inverses in that same chart. If each numerical argument has error at
most epsilon, the represented arguments differ by at most that bound plus
`2*epsilon`. A near-zero derivative therefore does not become a well-conditioned
inverse by proximity; unresolved error or chart predicates reject the move.
With a fixed coordinate, overlapping freshly proved charts agree on their common
monotone sheet; axis switching cannot choose a different inverse for the same q.

For the structural circle chart write `z=A^-1(q-b)`. On a common open hemisphere
the unwrapped argument of z is continuous and single-valued. On any compact
request neighborhood separated from z=0 it has a finite local derivative bound;
in particular `||D arg(z(q))|| <= ||A^-1|| / inf ||z||`. The interval inverse,
strict hemisphere test and explicit argument enclosure provide the corresponding
local admission and rounding limits. The provider's proved circle correspondence
chooses a unique lift at a legitimate seam. These are derivations of the chart
contracts, not a claim of global projection, uniform screen-speed or injectivity
of an arbitrary locus. Failed new-chart events stop the context instead of
silently resuming at a distant inverse after an unproved interval.

The chart is anchored to the old semantic argument. A request outside that
chart stops the gesture. A nearby Cartesian image on another sheet cannot
replace it. The affine-circle chart bounds angular change strictly below a
quarter-turn, while the monotone chart bounds parameter change by its proven
interval. As a request tends to the old image, the chart inverse tends to the
old argument, up to the explicit parameter enclosure tolerance. Switching a
coordinate chart at a new gesture requires its own whole-interval proof and the same bound; an
uncertain midpoint or poorly conditioned chart cannot authorize an unbounded
parameter error. Cusps, folds and self-intersections lacking the required chart,
unresolved predicates, stale sources and exhausted budgets fail closed.

Kernel: proof, selection, freshness, exact address reconstruction and atomic
publication. Frontend: world request capture, fixed/free choice, diagnostics and
gesture Undo. No Path conversion, new point during drag, persistent mouse
coordinates or independent Desktop geometric solver is introduced.

Validation must cover the v3 vector-circle example, bidirectional/seam motion,
the author's h/ReferenceR1 in regular charts, historical Spline/R6, distant and
antipodal requests, folds/gaps, lock, exact rejection state, identity/dependents,
copy/reopen and budgets. New tests must belong to the effective PHASE selection.
Counters distinguish semantic evaluations and interval/structural proof passes;
the semantic ceiling and counters cover the proof and the point's forward
binding, consumed once by ordinary DAG publication. They do not bound arbitrary
dependent algorithms supplied by a document. Whole-mutation latency includes
those dependents and the existing O(N) atomic construction snapshot. No
whole-DAG work-budget or universal interactive-latency guarantee is claimed.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED: no workstation/runtime prerequisite changes.
GUIDE_IMPACT: creation and drag controls require focused user guidance.
VERIFICATION_INFRASTRUCTURE_IMPACT: effective PHASE selection adds the new tests.
No CU7 closure, main promotion, M08-D, M09, I2G or I3 is authorized by this record.

# CU7 R5-A — Pointer requests for existing semantic points

2026-11-04. Corrective Desktop implementation under the R5 author mandate.
Candidate design, not an accepted ADR, author acceptance, D1 adoption or F8.

## Cause and authority

Inspection follows the actual `EuclidianControllerD.mousePressed` /
`mouseDragged` listeners into `GeoCeDGEuclidianController`. The semantic press
already recognizes the interaction role and captures the current address;
native `moveMode` is deliberately bypassed because P is a dependent point.
The kernel remains the only owner of its editable semantic address.

The R4 physical reproduction on the reopened parabola distinguishes two
requests: a near-curve diagonal moves P and its dependent, whereas a horizontal
request away from the trace leaves both unchanged and displays the continuation
barrier. Therefore selection or a missing mouse listener is not the demonstrated
interruption. The Desktop currently passes the creation hit-discovery radius
(about 4.24 pixels in this observation) to the movement operation as its final
world proximity ceiling. This rejects ordinary off-trace pointer requests even
when the existing certified local continuation can produce a nearby address.
The original observation and screenshots are preserved separately by the root
agent; they are not replaced by this design or automated assertions.

R4's synthetic large MEET event explicitly selected radius .5; its other happy
paths supplied targets on or very close to the curve. Those cases establish
their actual contracts, but do not establish ordinary off-trace GUI dragging.

## Minimal change

Creation keeps exactly its current captured hit-discovery radius and policy.
For an existing editable semantic point only, capture a fresh immutable world
request q and its finite currently evaluated anchor p. Set the declared movement
proximity radius to `nextUp(hypot(p.x-q.x,p.y-q.y) + r_hit)`. This is a bounded,
explicit floating-point numerical query policy; it is neither a certified
geometric distance bound nor an existence/uniqueness/continuation permission.
Nonfinite values reject before the kernel operation. Do not increase the work
budget, weaken tolerances, choose a parameter or consult the render.

This policy admits an ordinary mouse request away from the stroke while keeping
a finite current-anchor-based search neighborhood. The kernel must still prove
its normal same-component / selector / preimage / lift correspondence, discover
and isolate its proposed address, forward-evaluate it, revalidate revision and
publication guards, and publish at most one final semantic mutation. The current
anchor alone never authorizes editing. A genuine barrier continues to reject.

The change belongs in the existing Desktop request-capture helper. It adds no
kernel solver, Path adaptation, persistent state, or identity. Reference Q and
click creation retain their original policies. Tool/policy/view generation,
source currentness, atomic rollback, DAG propagation, one Undo per gesture and
the fixed-point behavior remain unchanged. Explicit feature-off behavior stays
unchanged; it was not the observed cause.

## Validation

Add `R5SemanticPointPointerDesktopTest`, using the public AWT listener path with
OS-conformant drag events (`NOBUTTON` plus `BUTTON1_DOWN_MASK`). Compare the
previous hit-radius request (rejected without a mutation) with the declared new
policy and a proven strict local chart; test off-trace motion in both directions,
new and reopened points, dependent/identity preservation, one Undo/Redo,
save/reopen and dragging again, and fixed points. Preserve genuine singular or
uncertifiable requests as negative cases. These tests are not physical input.

Root must separately repeat physical GUI-1 through GUI-4 over the integrated
candidate before freezing its technical SHA. STATIC, INFRA_UNIT, PHASE R5,
INTEGRATION and FINAL must then refer to that exact SHA.

BOOTSTRAP IMPACT — NO CHANGE REQUIRED: no prerequisites, dependencies or runtime
changes. VERIFICATION_INFRASTRUCTURE_IMPACT=TEST_SELECTION. GUIDE_IMPACT=UPDATE:
explain movement request proximity separately from click/Q discovery, and retain
explicit barrier diagnostics. Required verification level: FINAL.

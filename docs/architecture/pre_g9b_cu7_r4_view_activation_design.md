# CU7 R4 — view activation currentness (P2-C)

Status: candidate implementation design; no author approval or normative adoption.
Baseline: `00d65aa0c1cba9c4c0f673abebb1c446c65f98a7`.

## Scope and evidence

This is a focal Desktop implementation correction. The source of truth is the
live `GeoCeDGPointRequest`, `AppD`, `GuiManagerD` and `DockManagerD`, together
with the R4 author mandate, section 5. There is no new geometric permission,
parameter, reconstruction model, persistent identity, XML field or kernel rule.

The preserved independent probe is
`artifacts/cu7-r3-independent-audit/probe-desktop/org/geocedg/desktop/R3IndependentDesktopContextAuditTest.java`.
Its original Desktop receipt reports tool ABA PASS, policy ABA PASS and view
ABA FAIL (one semantic point instead of zero). R3 compares final view identity,
so EV1 -> EV2 -> EV1 revives the old request incorrectly.

## Activation authority and minimal upstream impact

`AppD.setActiveView` delegates to `DockManagerD.setFocusedPanel(int)`;
`GuiManagerD.setFocusedPanel` delegates to the same manager. Docking/drop,
show/perspective, AWT focus and keyboard focus cycling also enter
`DockManagerD.setFocusedPanel(DockPanelD, boolean)`. The only assignments to
`focusedEuclidianDockPanel` in the live source occur in that method. Hide/close
clear focus through its null path. No independent setter restores that field.

Add a passive, transient activation generation and a read accessor to this
existing convergence point. Increment before **any** focus callback when the
next euclidian panel differs, or null clears a remembered euclidian panel.
Reasserting the same focused panel does not increment. Moving focus to an
Algebra/non-euclidian panel retains the remembered active euclidian view, as
upstream already does, and does not increment. Clearing a remembered panel is
conservatively a context boundary even if EV1 is then the default fallback.
Hide/show and docking round trips cannot recover an earlier generation.

The counter saturates at `Long.MAX_VALUE`, reserved as an exhausted sentinel.
All requests reject that sentinel. It never wraps to an earlier ticket; focus
navigation itself need not fail or introduce an exception into Classic. The
generation belongs to the manager instance; replacing that manager is also a
context boundary. No view creation, geometry evaluation or callback is needed
to read the ticket.

This one inherited file is necessary: overriding only AppGeoCeDG or
GuiManagerGeoCeDG would miss direct docking calls. Introducing alternative
Layout/DockManager factories would require more inherited changes and duplicate
an existing activation authority. Preserve upstream licensing and mark the
small modification. Classic only maintains a passive counter; it does not
consume GeoCeDG request policy.

## Request contract and boundaries

`GeoCeDGPointRequest` captures manager identity and generation alongside the
existing immutable world coordinates, world radius, numeric policy, source
state/revision, view identity, document registry and mode. Every derived
request (selection, reference, bound tool/gesture context) preserves the ticket.
`isCurrent()` additionally requires the same activation manager and exact,
non-exhausted generation. The existing document/source/reference, tool-context
and policy checks remain conjunctive and unchanged.

The existing checks after source/preimage callbacks, on confirmation and inside
the single atomic publication/drag mutation consume this strengthened request.
No second snapshot, intermediate mutation, resolver or new geometry is added.
A view ABA during DAG callbacks therefore rejects and restores the same last
valid semantic state through the existing atomic mechanism.

Pan/zoom does not activate a different view and does not alter the generation.
The already captured world request and radius remain authoritative. Captured
view-transform data stays diagnostic: it is not reinterpreted against the new
screen transform and is not an additional invalidation condition.

## Permanent validation design

New class: `org.geocedg.desktop.R4ViewActivationDesktopTest`, with the real
embedded Desktop host, EDT confinement and existing lifecycle cleanup. No
physical mouse evidence is claimed for these AWT tests.

Cover the preserved source-choice adversary through programmatic activation
and direct docking; tool and policy ABA remain rejected. Verify no-point and
exact construction/ID/reservation/history invariants. Verify preimage callback
ABA and output-add ABA at publication, including a forward creation route and
native placement where available. Exercise null/hide-show transitions and a
same-panel no-op. Check drag ABA during DAG update restores address, identity
and dependents, and that a gesture ticket cannot revive after ABA between
events. Positive controls retain pan/zoom world coordinates/radius, valid
creation and ordinary drag. Explicitly test generation exhaustion without wrap.

The root runner executes focused R4/R3 Desktop selections, then the canonical
frozen R4 profiles. This agent does not run Gradle or GUI. Original failed audit
receipts and R3 fixtures stay untouched.

BOOTSTRAP IMPACT: NO CHANGE REQUIRED; no prerequisite, runtime or dependency
change. GUIDE_IMPACT: UPDATE the request-currentness explanation in EN/ES by
the integrated R4 owner. VERIFICATION_INFRASTRUCTURE_IMPACT: TEST_SELECTION;
the root owner registers the new class. The integrated R4 required level and
final receipts remain controlled by the canonical verification authority.

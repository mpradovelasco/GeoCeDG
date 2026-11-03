# CU7 R3 F8 — Proposed local-component forward creation permission

Status: PROPOSAL FOR AUTHOR DECISION. Not implemented or enabled by R3.
The existing COMPLETE-only INDIVIDUAL_FORWARD_ADDRESS_CREATION permission stays
unchanged. This proposal is separate from the authorized F1–F7 corrections and
must not delay them. D1 adoption is also a separate author decision.

## Existing authority and exact gap

The approved existence contract distinguishes canonical domain, certified local
existence, continuous-valid components and global completeness. Its sections 4,
8 and consumer table permit useful LOCAL_CERTIFIED_SUBINTERVAL evidence while
other coverage is UNRESOLVED. LocusComponentSelectorResolver2D requires an
established structural correspondence and then unique containment; containment
alone never establishes correspondence. ADR0019/0041 keep forward construction
separate from global inverse uniqueness and from proximity.

The current candidate requires COMPLETE on every branch before examining one
individual address (IndividualForwardPointCreation2D.requireCapability).
That is a conservative permission boundary, not a proof that the chosen local
argument does not exist. A forward value F_b(u) needs no proof about unrelated
parts of the inventory, provided its own source, component and address are
authoritative. Discovery still cannot turn a global UNRESOLVED inverse result
into a none/unique or closest-point result.

## Exact candidate clause

Authorize a NEW permission LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION, independent
of INDIVIDUAL_FORWARD_ADDRESS_CREATION, R1/R6 and H1. It may offer and publish one
explicitly selected address only if all the following hold at query and commit:

1. Source, provider/version, definition, document authority and semantic revision
   are current; reconstructible capture and its fidelity report are positive.
2. The selected branch contains a currently certified positive-dimensional
   continuous-valid component, maximal or LOCAL_CERTIFIED_SUBINTERVAL, with
   interval-wide existence and same-construction/root-selection evidence.
3. A full retained structural selector resolves uniquely by the approved
   component resolver. No ordinal enumeration, label, parameter containment,
   coordinate, proximity or render sample creates this correspondence.
4. The complete canonical address belongs to that certified interval, with
   explicit endpoint/lift/seam policy. An unresolved boundary is not included.
5. Normal forward binding is valid and finite under the current source evidence.
   The binding's numerical grade and any error/enclosure are disclosed honestly.
6. Discovery uses an explicit world policy and bounded work. Numerical proximity
   remains FLOATING_POINT_UNCERTIFIED unless a separate distance proof exists.
   The offer asserts neither closest point nor local/global geometric uniqueness.
7. The user explicitly selects this address after seeing the global coverage
   state and the local certificate scope. Cancellation, stale evidence and budget
   exhaustion publish nothing. The normal atomic transaction and its demonstrated
   native restoration capability cover publication and callbacks.

Global completeness may be PARTIAL or another typed non-COMPLETE state only when
the selected component's positive certificate remains valid independently of
the unresolved coverage. No permission follows from pointwise F(u).isValid alone,
an assumed predicate, a sampled interval, or a Boolean check after a global
UNRESOLVED result. An inventory with no certified component remains ineligible.

The offer must distinguish GLOBAL_COVERAGE_INCOMPLETE from LOCAL_COMPONENT_VALID
and retain both values. Do not rewrite source completeness, source existence,
R6 statuses, or H1 guarantees. Existing COMPLETE-only operations retain their
own permission and diagnostics; this clause does not silently widen them.

## Identity, persistence and dynamic updates

Publish the normal semantic point and full address; Q stays transient. The
permission is not an alternative identity or a persistent nearest-point recipe.
Reuse source/branch/component/provider lineage and ordinary DAG construction.
Cross-revision retention requires the approved structural lineage relation.
If that relation disappears, containment in some newly discovered component
cannot silently replace it: report the normal unresolved/undefined state or
request a new explicit selection. Discovery of additional components does not
retroactively turn the old offer into a global unique or complete claim.

This permission does not itself authorize dragging. R3 continuation still needs
its independent local proof within the retained component and current context.

## Proposed implementation after authorization

Add a distinct permission/evidence branch to the existing creation facade and
reuse its discovery, canonical address, binder, budget and atomic publication.
Keep the existing COMPLETE guard intact for the existing permission. The new
branch replaces only the global completeness prerequisite with the positive
per-address component checks above. No new existence solver, identity system,
frontend geometry algorithm or generic Path adapter is needed.

Required regressions before enabling: one certified local component with an
unresolved remote band; unresolved selected band rejection; explicit multiple
preimages; stable selector with additional coverage; lost/split/merged lineage;
local subinterval endpoints and uncertified seam rejection; dynamic source/Q
changes; cancellation, depleted budget and callback rollback; `.cedg`, copy,
Undo/Redo and ordinary DAG propagation. All require the effective PHASE selector
and final-SHA canonical verification. No current R3 test result is claimed as
verification of this unimplemented permission.

## Decision requested after delivery

Recommended author decision: authorize this separate permission with the exact
conditions above and require its focused implementation/audit. Alternative:
retain the existing COMPLETE-only permission, explicitly accepting that this is
a product scope restriction rather than a mathematical nonexistence condition.
Neither decision adopts D1, closes CU7, or authorizes M08-D/M09/I2/I2G/I3.

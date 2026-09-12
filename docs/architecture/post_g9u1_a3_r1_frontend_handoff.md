# POST-G9U1-A3-R1 frontend handoff

- Status: **CLASSIC 5 IMPLEMENTATION — PASS — AUTHOR APPROVED**
- Product phase: `POST-G9U1-A3-FRONTEND`
- Kernel phase: `POST-G9U1-A3 = PASS — AUTHOR APPROVED`
- Kernel design: [`post_g9u1_a3_v2_compatible_redefine.md`](post_g9u1_a3_v2_compatible_redefine.md)
- Decision candidate: [ADR 0026](../adr/0026-advanced-redefine-and-explicit-legacy-fallback.md)

This handoff records the author-approved bounded Classic 5 implementation. Web
remains unchanged. The shared callback is dormant unless a product frontend
installs it; it exposes an already classified kernel fact and adds no frontend
compatibility or impact logic.

## Request flow

1. Parse the proposed definition through the existing staged candidate path and
   ask the construction-owned registry for a `SpatialRedefineAssessment`.
2. For `ADVANCED_RETAIN_AVAILABLE` or
   `ADVANCED_RETAIN_AVAILABLE_WITH_RELOCATION`, execute only
   `ADVANCED_RETAIN`. Relocation requires no extra confirmation because it is
   the deterministic DAG consequence of the requested compatible redefine.
3. For `LEGACY_REPLACEMENT_AVAILABLE`, show an explicit confirmation before any
   mutation. State that CeDG continuity is not established, source identity will
   be fresh and list every entry from the complete typed impact report.
4. Offer only Cancel and explicit legacy execution. Acceptance selects
   `LEGACY_REPLACEMENT`; dismiss/cancel abandons the staged candidate.
5. For `INVALID_DAG`, `INCOMPATIBLE_HOST_REDEFINE`, `UNSUPPORTED`, `AMBIGUOUS`
   or `STALE_ASSESSMENT`, do not offer legacy execution. A stale result is
   abandoned; the user submits through the normal staged-candidate path again,
   so the previous confirmation is never reused.

Classic 5 exposes this flow through its existing Algebra/definition redefine
entry points. Compatible V2 definitions use advanced retain directly. A
host-valid incompatible definition shows one localized modal confirmation with
every typed entry in the complete kernel impact report. Cancel publishes
nothing; acceptance selects exactly `LEGACY_REPLACEMENT` and remains one normal
Undo operation.

## Affected-object navigation

Affected-object navigation and a persistent recovery panel are deliberately
deferred. A future non-authoritative panel may:

- group entries by predicted status and recovery class;
- display localized explanations derived from typed reason codes;
- reacquire a transient current `GeoElement` through the durable impact entry;
- select/navigate to that current object and invoke the existing edit/redefine
  workflow; and
- expose the ordinary Undo affordance for the single host operation.

The panel must reacquire handles after rebuild, undo or reopen. A missing current
handle is presentation state, not permission to infer a new association. The
panel never rebinds by label, coordinate, proximity or construction order.

## Authority boundary

The kernel owns compatibility, P3-R1 placement, assessment currentness,
fallback eligibility, complete impact, identity, revisions/currentness and
atomic commit/rollback. The frontend owns confirmation, localization, list
presentation, navigation and the Undo affordance. No frontend visibility or
choice upgrades semantic evidence, and no Swing/Web type enters the kernel
contract.

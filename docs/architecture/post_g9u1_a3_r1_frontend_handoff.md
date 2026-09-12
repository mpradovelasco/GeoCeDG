# POST-G9U1-A3-R1 frontend handoff

- Status: **DESIGN HANDOFF CANDIDATE — FRONTEND NOT IMPLEMENTED**
- Product phase: `POST-G9U1-A3`
- Kernel design: [`post_g9u1_a3_v2_compatible_redefine.md`](post_g9u1_a3_v2_compatible_redefine.md)
- Decision candidate: [ADR 0026](../adr/0026-advanced-redefine-and-explicit-legacy-fallback.md)

This handoff describes the smallest future frontend integration. It authorizes
no Desktop/Web implementation, dialog text, layout or localization.

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
   or `STALE_ASSESSMENT`, do not offer legacy execution. A stale result must be
   reassessed.

## Affected-object navigation

After an accepted legacy replacement, a future non-authoritative panel may:

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

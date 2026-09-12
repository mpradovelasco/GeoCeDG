# ADR 0026: Advanced semantic redefine and explicit legacy fallback

- Status: **Accepted — AUTHOR-APPROVED POST-G9U1-A3 authority**
- Date: 2026-09-12
- Phase: `POST-G9U1-A3`
- Parent authority: [G9A3 lifecycle design](../architecture/g9a3_spatial_lifecycle_migration_design.md)
- Position authority: [POST-G9U1-A4 characterization](../architecture/post_g9u1_a4_construction_position_rollback_characterization.md)
- Detailed design: [POST-G9U1-A3-R1](../architecture/post_g9u1_a3_v2_compatible_redefine.md)

## Context

The first A3 candidate correctly separated retained identity from explicit true
replacement, but its procedural-position implementation silently omitted
ordinary construction elements lacking a durable GeoCeDG identity. It also had
no typed, non-mutating contract by which a frontend could distinguish an
advanced semantic redefine from an explicitly accepted legacy replacement and
show the complete CeDG impact before mutation.

Ordinary GeoGebra elements are legitimate procedural neighbours without thereby
becoming semantic identities. Conversely, failure to retain semantic continuity
does not itself authorize fresh identity. These constraints apply to future
semantic providers as well as the immediate V2 specialization, so they require a
durable architectural decision rather than implementation commentary alone.

## Decision

1. A participating semantic redefine begins with a non-mutating, typed
   assessment bound to the exact old context, candidate contract and current
   host/graph/runtime epochs.
2. Advanced `RETAIN` requires provider continuity, complete stable-role groups,
   a valid candidate DAG and P3-R1 placement. P3-R1 is an explicit
   author-approved refinement/amendment of P3: the old procedural interval is
   kept when legal; otherwise only the target producer/group moves, to the
   closest legal bound forced by predecessor/dependent constraints. Formally,
   `destination = clamp(oldSlot, requiredPredecessorBound,
   requiredDependentBound)` within the established P3-R1 constraints.
3. Ordinary construction elements may be transaction-local procedural witnesses.
   Their handle and entry-sequence coordinate are never serialized, remapped or
   treated as semantic identity. If their surviving order cannot be established,
   advanced retention fails closed.
4. Failure of advanced retention does not imply `FRESH`. Legacy replacement is
   offerable only through an independent host-valid/provider replacement
   classification and only with a complete typed impact report.
5. Legacy execution requires an explicit caller-selected mode. It retires the
   old semantic closure and assigns fresh candidate identities; labels,
   coordinates, proximity and host ordering cannot transport continuity.
6. The impact report enumerates the complete known durable downstream closure,
   predicts status/recovery with closed reason codes and never performs a rebind.
   Incomplete impact is non-offerable.
7. Advanced and legacy execution reuse the G9A3 lifecycle transaction, XML
   rollback and normal host undo operation. A stale assessment, cycle or
   structurally invalid host proposal rejects before publication.
8. The kernel owns classification, identity, impact and execution authority.
   A frontend owns confirmation, presentation, navigation and Undo affordances.

## Consequences

- P3-R1 refines P3 and remains stronger than arbitrary topological equivalence
  without requiring a fixed numeric construction index: it is minimum
  deterministic relocation forced by the new DAG, not any valid topological
  order.
- A compatible topology change may retain identity while relocating only as far
  as the DAG requires.
- A frontend can truthfully warn about a legacy replacement without guessing
  from exceptions or reconstructing semantic dependencies.
- Ordinary upstream redefine remains unchanged unless an explicit semantic
  provider participates.
- The additional result types are kernel contracts; no frontend implementation
  is part of POST-G9U1-A3-R1.

## Rejected alternatives

- Treating every construction element as a durable CeDG identity.
- Ignoring unregistered neighbours or replacing their omission with a universal
  rejection.
- Preserving an exact numeric index even when a valid new DAG requires movement.
- Choosing any topological order or the nearest post-mutation neighbour.
- Automatically executing `FRESH` when `RETAIN` fails.
- Letting a frontend infer impact from labels, coordinates or exception text.

# ADR 0017: Deterministic intrinsic phase/rank for public Locus V2 roots

- Status: **Accepted**
- Date: 2026-08-29
- Accepted: 2026-08-29, author-directed G9U0-R4 corrective iteration
- Phase: G9U0-R4 `PASS — AUTHOR APPROVED`
- Normative contract:
  `geocedg/specs/locus/locus-v2-intersections.md`

## Context

The public intersection solver can establish several current, finite,
transverse and locally isolated roots on one oriented Locus V2 component while
global completeness remains `NOT_ESTABLISHED`. G9U0-R4 first distinguished
roots by stable component lineage and typed oriented contact germ. The exact
four-root author fixture proves that two distinct roots can still share that
base selector. Failing the whole collision group closed is truthful but blocks
four geometrically and locally established construction points.

Existing decisions prohibit identity from solver output order, result-list
position, coordinates, screen position, proximity, sampling or movement
history. They did not previously authorize an intrinsic ordinal derived from
the Locus V2 semantic domain itself. The author has now made that narrow
normative decision.

The current corrective smoke preserves the historical chronology rather than
rewriting it. Case A, the canonical two-root construction, passed. Case B first
published four unique selectors and exact tokens and materialized all four
points, then failed during ordinary regular motion when those points became
undefined. The failure exposed a fixed `component span / 256` transition bound:
an isolation-work partition had become a proxy for topology evidence. That
fixed bound is not part of the accepted identity decision.

## Decision

1. Interpret “no identity by order” as **no identity by extrinsic enumeration
   order**. Solver discovery order, list/output slot, construction/XML index,
   Cartesian left/right order and UI `Solution N` remain non-authoritative.
2. Permit an intrinsic semantic phase/rank only for roots that share one exact
   public base selector and one coherent current collision context. The rank is
   induced by the explicit oriented one-dimensional Locus V2 component.
3. Establish rank only from canonical semantic parameters with pairwise
   disjoint local isolating intervals. The interval proves order; neither the
   raw parameter value nor interval bits become durable identity material.
4. Bind the enriched selector to:

   ```text
   stable branch/component lineage
   + typed transverse germ/collision class
   + declared semantic orientation
   + periodic/nonperiodic domain kind
   + verified collision-group cardinality
   + intrinsic oriented phase/rank
   ```

   The ordinal alone is never identity. Source pair, constructive lineage,
   topology context and provider/target contracts remain bound by the enclosing
   token ledger.
5. Preserve the unextended base selector when it is already unique. Only a
   repeated base selector receives the phase/rank dimension.
6. On a nonperiodic component, order disjoint root intervals from the declared
   start in the declared orientation. Two isolated roots cannot exchange this
   rank within one regular topology stratum without collision, loss of
   isolation, component change or another detectable selector event.
7. On a periodic component, use the explicit oriented fundamental interval as
   the deterministic phase frame. Equivalent half-open endpoints are one
   semantic parameter. A ranked root interval that reaches the seam is not
   assigned by a heuristic. Until intrinsic cyclic evidence resolves its
   offset uniquely, the affected old token group remains non-current in durable
   periodic quarantine rather than being guessed or prematurely destroyed.
8. Treat collision-group membership/cardinality change, orientation or
   component redefinition, merge/split/tangency/overlap, ambiguous isolation
   and a periodic rank rotation that is proved as identity barriers. Inability
   to exclude a rotation is instead an explicit fail-closed quarantine state,
   not proof that one occurred. No prior token moves to the root that later occupies the
   same integer rank. Merge/split candidate-parent evidence is canonicalized by
   sorting the parent records by their opaque tokens once; each optional
   continuation key remains associated with its parent record. Token order is a
   deterministic evidence encoding, not root identity.
9. Keep previous/current comparison subordinate to current-snapshot identity.
   Current deterministic semantic selection allocates the current root;
   Cartesian nearest-root continuity never does. For a complete ranked periodic
   group, however, current activation of an old token additionally requires an
   adaptive intrinsic periodic phase-tube/cell certificate: prior and current
   canonical phases and isolating intervals must define disjoint same-rank tubes
   within the nearest cyclic root gaps, and every prior root must map to one
   current root with the same complete selector. A fixed fraction of the
   component span, including `span / 256`, and an isolation-subdivision budget
   are not topology evidence. A missing, ambiguous, incomplete or non-bijective
   certificate fails closed into durable periodic quarantine. A later unique
   cyclic offset zero releases/reuses the group, a unique proved nonzero offset
   permanently retires only the affected ranked group, and absent or multiple
   offsets leave it quarantined. This certificate guards current token reuse
   only; it never selects the current root or becomes durable identity. Absence
   of such a relation on an ordinary nonperiodic group is not identity evidence
   and does not override its unique current selector.
10. Keep local admissibility independent of global completeness. This decision
    changes identity resolution only and does not upgrade numerical guarantees,
    global enumeration or unresolved evidence.
11. Continue using opaque public tokens. The original phase/rank correction
    advanced the ledger to format v3. The current materialized-allocation
    lifecycle refinement advances canonical export to format v4, imports
    canonical v3 phase state and authentic v1/v2 pre-phase state, and preserves
    an older exact token only under one compatible semantic binding. Claimed
    active/dormant and periodic-quarantine status is legal only in v4. Canonical
    v4 uses `PERIODIC_QUARANTINE` for unclaimed complete group evidence and
    `CLAIMED_PERIODIC_QUARANTINE` when an existing materialized point retains a
    claim. Neither is current or point-admissible. A phase-bearing v3 state
    falsely relabeled as v2 and a claimed/quarantined-state v4 entry relabeled
    as an older version fail closed. Legacy singleton token material may never bind an
    intrinsic-phase selector: runtime migration must allocate fresh material
    instead of reusing that singleton, and import must reject a manipulated
    current snapshot that forges such a binding. The external `locus-root/v3`
    token envelope is unchanged.
12. Do not apply this one-sided selector to Locus V2 × Locus V2. Pair roots keep
    ADR 0009's symmetric pair-domain and pair-isolation authority until a
    separately justified symmetric cell design exists.
13. Keep the durable selector binding separate from the revision-scoped current
    topology certificate. An exact token already claimed by a materialized
    point may remain retained but dormant while its selector is not currently
    admissible. The existing `GeoPoint` becomes undefined and may reactivate
    with the same token only when that same complete selector again resolves
    uniquely with current admissibility. It is never retargeted, and no new
    point is created automatically. Periodic quarantine retains the complete
    durable selector evidence needed for later resolution, but never counts as
    current identity. Exact-provenance copy preserves quarantine and cannot
    certify release; releasing the final materialized claim prunes the complete
    quarantined group. A proved seam/monodromy transition remains a typed
    permanent barrier and cannot rotate the allocation to another rank.

## Periodic monodromy

A circle-valued semantic domain need not admit a labeling that is simultaneously
global, continuous and independent of history. R4 therefore makes the
fundamental interval and its orientation the deterministic current-state frame.
Within a seam-free chart the phase/rank is stable. If a root crosses the seam,
the current rank frame can cyclically permute. GeoCeDG never hides that
possibility with Cartesian proximity or a mutable winding counter. Insufficient
intrinsic cyclic evidence quarantines the old allocation durably; unique
offset-zero evidence releases it, while unique proved nonzero offset is typed
monodromy and permanently retires it.

A wide or direct periodic update may leave no complete adaptive phase-tube
certificate with which to exclude that permutation. In that case current reuse
fails closed in periodic quarantine for the affected ranked group, even if the
current snapshot again contains unique selectors. The durable selector and the
revision-scoped topology certificate remain separate: the former preserves the
recoverable exact allocation; only the latter can establish current activation,
release by unique offset zero, or permanent retirement by proved nonzero
offset. This conservative reuse guard does not apply to an ordinary nonperiodic
group merely because its bounded diagnostic relation is absent.

Reversing the declared orientation is a different semantic parametrization and
reverses the induced order. It is not nondeterminism and does not authorize
reuse of allocations from the prior orientation without an explicit semantic
map.

## Relationship to existing decisions

This ADR refines ADR 0008 decisions 4–6 and ADR 0013 decisions 10–11 only for
the public single-Locus V2 root selector. Their prohibitions on extrinsic
order, coordinates, proximity and automatic point creation remain intact.
It preserves ADR 0009's source-order-independent pair semantics and ADR 0013's
rule that `Solution N` is transient presentation rather than identity.

Historical reports are not rewritten. Accepting this ADR authorizes the bounded
R4 correction; it does not make R4 PASS, authorize R5, or authorize G9U1.
The author accepted the final R4 four-root regular-motion and existing-point
reactivation smokes. R4 is `PASS — AUTHOR APPROVED`; R5 and G9U1 remain
unexecuted and unauthorized.

## Consequences

- The exact four-root author fixture may publish four unique selectors and four
  exact materializable tokens without claiming global completeness.
- Solver enumeration permutations cannot change selector/token association.
- Regular semantic-parameter motion inside one certified adaptive phase tube
  preserves the binding without exact parameter equality or dependence on UI
  update granularity.
- Cardinality/topology/seam changes may conservatively invalidate points rather
  than retarget them.
- Missing periodic relation evidence durably quarantines rather than destroys
  the complete old group; only unique offset zero releases it and only proved
  nonzero offset permanently retires it.
- A claimed exact token may remain dormant while the same materialized
  `GeoPoint` is undefined, and may reactivate only through the same uniquely
  resolved complete selector; no recompute creates a replacement point.
- Old v1 and authentic pre-phase v2 ledgers remain readable; insufficient or
  ambiguous old evidence is never upgraded from coordinates, and an intrinsic
  phase binding mislabeled as ledger v2, a claimed lifecycle state mislabeled as
  pre-v4, or a phase binding forged onto legacy singleton token material is
  rejected.
- Merge/split diagnostics expose deterministic parent evidence ordered by opaque
  parent token while preserving each token/key association; that encoding does
  not authorize continuation through the ambiguous event.
- Candidate markers and future create-one/create-all/auto-materialize actions
  must consume current kernel tokens, remain frontend-only, and must not
  calculate a second UI rank. They do not expose dormant tokens and never create
  points merely because a later recomputation reactivates a root.
- The incremental selector/reactivation pass costs `O(R log R + P)` for `R`
  current roots and `P` existing materialized token bindings. Existing
  whole-transition diagnostics may still be `O(R^2)` in the worst case, but
  they are non-authoritative and trigger no additional solve per child point.

## Alternatives considered

### Keep repeated base selectors permanently fail-closed

Rejected by the author because it prevents deterministic construction from all
four locally established roots in the canonical reproduction.

### Use solver/list order or Cartesian proximity

Rejected. Both are extrinsic, may vary with implementation or movement history,
and contradict exact token identity.

### Use a global mutable topology counter or winding history

Rejected. It would make history the semantic authority and break deterministic
path independence.

### Relax numerical certification instead

Rejected as the solution to this defect. All four roots already have sufficient
local evidence under the current public materialization contract; their blocker
was selector collision, not a missing numerical tier.

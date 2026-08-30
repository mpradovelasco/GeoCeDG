# ADR 0017: Deterministic intrinsic phase/rank for public Locus V2 roots

- Status: **Accepted**
- Date: 2026-08-29
- Accepted: 2026-08-29, author-directed G9U0-R4 corrective iteration
- Phase: G9U0-R4 implementation candidate; phase PASS remains pending author
  re-review
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
   assigned by a heuristic: the affected group publishes a typed identity
   discontinuity and old tokens are invalidated rather than rotated.
8. Treat collision-group membership/cardinality change, orientation or
   component redefinition, merge/split/tangency/overlap, ambiguous isolation
   and a periodic rank rotation that is observed or cannot be excluded as
   identity barriers. No prior token moves to the root that later occupies the
   same integer rank. Merge/split candidate-parent evidence is canonicalized by
   sorting the parent records by their opaque tokens once; each optional
   continuation key remains associated with its parent record. Token order is a
   deterministic evidence encoding, not root identity.
9. Keep previous/current comparison subordinate to current-snapshot identity.
   Current deterministic semantic selection allocates the current root;
   Cartesian nearest-root continuity never does. For a complete ranked periodic
   group, however, reuse of an old token additionally requires a complete
   bijection from every prior root to one current root with the same complete
   selector. A missing, ambiguous, budget-exhausted, incomplete or non-bijective
   relation invalidates the whole prior collision group rather than guessing.
   This relation guards prior-token reuse only; it never selects the current
   root. Absence of such a relation on an ordinary nonperiodic group is not
   identity evidence and does not override its unique current selector.
10. Keep local admissibility independent of global completeness. This decision
    changes identity resolution only and does not upgrade numerical guarantees,
    global enumeration or unresolved evidence.
11. Continue using opaque public tokens. The token ledger advances to canonical
    format v3 so phase/rank bindings are not silently interpreted as the
    earlier schema. It imports v1 and v2 only for authentic pre-phase state, preserves an
    older exact token only under one compatible semantic binding, and otherwise
    fails closed. An intrinsic-phase binding is legal only in ledger v3; a v3
    phase state falsely relabeled as v2 is rejected. Legacy singleton
    token material may never bind an intrinsic-phase selector: runtime migration
    must allocate fresh material instead of reusing that singleton, and import
    must reject a manipulated v3 snapshot that forges such a binding. The external
    `locus-root/v3` token envelope is unchanged.
12. Do not apply this one-sided selector to Locus V2 × Locus V2. Pair roots keep
    ADR 0009's symmetric pair-domain and pair-isolation authority until a
    separately justified symmetric cell design exists.

## Periodic monodromy

A circle-valued semantic domain need not admit a labeling that is simultaneously
global, continuous and independent of history. R4 therefore makes the
fundamental interval and its orientation the deterministic current-state frame.
Within a seam-free chart the phase/rank is stable. If a root crosses the seam,
the current rank frame can cyclically permute. Without an approved intrinsic
lift that removes that ambiguity, GeoCeDG reports an identity discontinuity and
invalidates affected old tokens. It never hides monodromy with Cartesian
proximity or a mutable winding counter.

A wide or direct periodic update may leave no complete semantic relation with
which to exclude that permutation. In that case all old tokens in the affected
ranked group fail closed, even if the current snapshot again contains unique
selectors. A later stable publication may allocate fresh tokens; it cannot
reinterpret the old ones. This conservative reuse guard does not apply to an
ordinary nonperiodic group merely because its bounded diagnostic relation is
absent.

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

## Consequences

- The exact four-root author fixture may publish four unique selectors and four
  exact materializable tokens without claiming global completeness.
- Solver enumeration permutations cannot change selector/token association.
- Small semantic-parameter drift inside one regular phase cell preserves the
  binding without exact parameter equality.
- Cardinality/topology/seam changes may conservatively invalidate points rather
  than retarget them.
- Old v1 and authentic pre-phase v2 ledgers remain readable; insufficient or
  ambiguous old evidence is never upgraded from coordinates, and an intrinsic
  phase binding mislabeled as ledger v2 or forged onto legacy singleton token
  material in ledger v3 is rejected.
- Merge/split diagnostics expose deterministic parent evidence ordered by opaque
  parent token while preserving each token/key association; that encoding does
  not authorize continuation through the ambiguous event.
- Candidate markers and future create-one/create-all/auto-materialize actions
  must consume the same kernel token authority and must not calculate a second
  UI rank.

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

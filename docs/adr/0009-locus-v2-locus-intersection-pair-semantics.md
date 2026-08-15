# ADR 0009: Dual-parameter Locus V2 intersection pair semantics

- Status: **Accepted**
- G8C design disposition: **PASS — AUTHOR APPROVED**
- G8C2 contract disposition: **NORMATIVE — AUTHOR APPROVED; implementation authorized / not started**
- Proposed: 2026-08-14
- Accepted: 2026-08-15

## Context

Accepted ADR 0008 establishes the immutable rich intersection result, normal-DAG
rich Geo, strict token-selected point consumer, completeness axis, Option B,
durable identity, explicit ambiguity and query-local state for G8. G8B applies
that architecture to one semantic locus and a basic analytic target.

Intersecting two Locus V2 entities is different in kind. For valid branches,

```text
F_j(t) = Q_k(u)
```

is a two-variable system. Both sources have identity, revision, topology,
branches, components, periodicity and dynamic lifecycle. Candidate isolation,
local uniqueness and overlap live in parameter-pair space. Geometry is
commutative, while numerical evidence is ordered. Forcing this into the G8B
one-parameter target adapter would hide dependencies and weaken identity,
coverage, or overlap semantics.

## Decision

1. Introduce a dedicated query-local dual-parameter solver and explicit
   two-source normal-DAG algorithm for G8C2. Reuse the ADR 0008 rich result,
   publication, completeness, Option B and point-consumer authority.
2. Define the geometric source identity as a canonical unordered pair. Allow a
   deterministic ordered computation view, with a total reversal mapping for
   source revisions, branch/components, parameters, rectangle and determinant
   orientation. Operand reversal does not create a new durable token.
3. Represent each finite solution with both semantic preimages and an isolating
   parameter rectangle. Treat `(t,u)`, the rectangle, revisions, residual and
   solver/Jacobian certificate as revision-scoped evidence, not identity.
4. Establish local isolation only through exhaustive local coverage plus a
   justified uniqueness basis. A small residual or converged iterative solve is
   insufficient. Regular-Jacobian evidence may support transverse uniqueness;
   tangent/singular roots require stronger analytic/certified/higher-order
   evidence or remain not established.
5. Use the dimensionless normalized tangent determinant for regular tangent
   classification. Zero is a tangency candidate, not a multiplicity proof.
6. Deduplicate only in semantic branch/component/parameter-pair context. Equal
   coordinates may retain multiple constructive tokens.
7. Represent overlap with typed established, suspected-not-established, or
   unsupported semantics. Establishment requires semantic component-wide
   evidence such as an exact/certified parameter relation. Samples never prove
   or represent overlap.
8. Claim global `COMPLETE` only after every declared branch/component product is
   exhaustively isolated or excluded. Option B permits independently verified,
   locally isolated pair solutions when completeness is weaker.
9. Initially support finite component products and periodic fundamental domains.
   Never use a viewport or arbitrary finite window for unbounded completeness.
10. Start query-local. Any shared pair index/owner requires measured evidence,
    bounded revision-scoped design and separate approval.

## Final review against G8C1

The author reviewed this decision after the G8C1 implementation was closed at
commit `3c72e889a436e4bbccde177e1f24423196575f04` and tagged
`geocedg-g8c1-pass`. The completed one-parameter kernel confirms rather than
contradicts the pair architecture:

- `AlgoLocusIntersectionV2` publishes one immutable result atomically through
  the normal DAG and keeps its source and target dependencies explicit;
- `LocusIntersectionResult2D`, `GeoLocusIntersectionResult`, and
  `AlgoLocusIntersectionPointV2` remain reusable result/publication/consumer
  authority without making the one-dimensional target adapter a pair solver;
- `LocusIntersectionTarget2D` and `ExtendedTargetIntersectionCapability2D`
  are intentionally one-parameter seams and therefore must not encode the
  second locus as an opaque target;
- completeness, Option B, local isolation, semantic tokens, explicit
  ambiguity, atomic failure, and query-local work remain compatible with two
  semantic preimages; and
- G8C1 introduced no shared owner, global cache, coordinate identity, public
  command, `Path`, persistence, legacy, 3D, or G9 dependency.

No substantive contradiction was found. The separate dual-parameter solver and
two-source algorithm remain necessary, while the accepted ADR 0008 rich-result
and lifecycle infrastructure remains the single shared semantic framework.

## Consequences

### Positive

- preserves constructive provenance from both loci;
- makes `A∩B=B∩A` compatible with stable semantic tokens;
- represents tangency, constructive multiplicity, overlap and incomplete
  coverage truthfully;
- preserves G8B point consumers and downstream CeDG DAG behavior;
- isolates two-parameter cost and failure from simpler G8C1 adapters.

### Costs

- requires a separate solver, work ledger and validation suite;
- branch/component products can grow combinatorially;
- general tangency, singularity, overlap and completeness remain unsupported
  unless stronger evidence is available;
- current G6 finite interval authority leaves unbounded pair domains outside the
  first implementation.

## Alternatives considered

### Treat the second locus as a one-parameter target adapter

Rejected. It hides the second semantic parameter/revision and cannot express
pair-space isolation or source symmetry honestly.

### Convert either locus to a sampled path or implicit approximation

Rejected. Render/legacy samples and invented implicit conversions are not
semantic authority and cannot prove identity, tangency, overlap or completeness.

### Make input order part of root identity

Rejected. It makes geometrically commutative queries generate different
solutions and destabilizes downstream points under argument reversal.

### Use coordinates/result order to associate roots

Rejected. It fails constructive multiplicity, reparameterization, crossings,
merge/split and newly discovered roots.

### Introduce a global/shared pair index immediately

Rejected. Characterization shows bounded deterministic query-local behavior and
no measured need. Shared state risks hidden DAG dependencies and stale topology.

## Acceptance and implementation gate

The author accepted this ADR on 2026-08-15 after G8C1 reached `PASS — AUTHOR
APPROVED` and the final source-level comparison above found no contradiction.
The G8C2 contract is normative and G8C2 implementation is authorized, but not
started. Acceptance does not constitute G8C2 implementation approval or G8
global closeout, and it does not authorize commands, `Path`, persistence,
legacy/Classic changes, 3D, G9, or unbounded-domain support.

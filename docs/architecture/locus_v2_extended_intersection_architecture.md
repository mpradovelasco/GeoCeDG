# Locus V2 extended-intersection architecture

**Status: G8C DESIGN AND G8C1 PASS — AUTHOR APPROVED; G8C2 ARCHITECTURE
NORMATIVE — AUTHOR APPROVED / IMPLEMENTATION AUTHORIZED — NOT STARTED**

## 1. Architectural split

```text
                         G8B shared semantic publication
                      immutable result -> rich Geo -> point
                                     ^
                                     |
              +----------------------+----------------------+
              |                                             |
G8C1 one-parameter pipeline                    G8C2 pair-space pipeline
F(t) + typed target adapter                    F(t) and Q(u)
candidate level h(t)                           parameter rectangles
1-D isolation/refinement                       2-D exclusion/refinement
target membership verification                 two-sided verification
```

The shared top and bottom preserve G8B lifecycle semantics. The middle solver
layers stay separate because candidate coverage, local isolation, derivatives,
source symmetry, overlap, and work combinatorics differ.

## 2. Reused G8B components

- `GeoLocusV2`, evaluator/session, branch/component/revision metadata;
- query-local policy, work ledger, diagnostics and atomic publication;
- immutable `LocusIntersectionResult2D` and finite solution vocabulary;
- completeness, result kind, numeric guarantee and currentness axes;
- durable token/continuation framework and strict token point consumer;
- target-independent residual evidence containers where their units remain
  typed rather than presumed uniform.

No G7 metric index, cumulative metric state, render cache, or legacy samples
are reused.

## 3. G8C1 responsibilities

### Adapter

An adapter translates authoritative target semantics into typed capabilities,
not another geometry:

- support/type and target revision;
- finite domain/components and invalid boundaries;
- candidate level evaluation;
- normalized independent residual/membership verification;
- analytic derivative/normal and bounds capability;
- degeneration/overlap diagnostics.

### One-dimensional solver

The G8B solver pipeline remains responsible for component enumeration,
candidate isolation, safeguarded refinement, tangency/minimum handling,
deduplication in semantic parameter space, completeness accounting, work
budgets, and solution publication. Family-specific analytic algorithms may
provide stronger candidates/certificates but cannot bypass independent adapter
verification.

## 4. G8C2 responsibilities

### Query and component pairing

A dual-source algorithm declares both inputs in `setInputOutput()` and captures
one coherent evaluator session. It enumerates branch/component products under a
deterministic canonical source order while preserving caller-order diagnostics.

### Broad phase

Semantic component bounds may reject pairs. Remaining products are subdivided
as parameter rectangles. Bounding/index data are derived from semantic
evaluation, query-local, non-authoritative and verified after refinement.

### Pair solver

The pair solver owns:

- rectangle coverage and bounded subdivision;
- interval/box exclusion when capability exists;
- safeguarded two-variable refinement;
- two-sided residual checks and parameter-domain membership;
- Jacobian/tangent classification;
- local uniqueness/isolation evidence;
- semantic pair deduplication and seam handling;
- overlap evidence and completeness accounting;
- continuation comparison against the immediately previous coherent result.

It does not call existing viewport-oriented curve intersection algorithms as
semantic authority. Existing algorithms may inform design only where their
mathematical capability can be isolated from view bounds, output permutations,
and coordinate deduplication.

## 5. Normalized residual architecture

Candidate functions and verification residuals are separated. A residual value
travels with a `ResidualContract` identifying family, quantity, units,
normalization, validity preconditions, and guarantee.

- conic/regular polynomial implicit: first-order normal residual based on
  canonical `G/||grad G||`, invariant to scalar equation multiplication;
- function graph: vertical model-length residual; optional first-order normal
  diagnostic with finite derivative;
- locus-locus: model-coordinate norm `||F(t)-Q(u)||`, with both semantic
  evaluations retained.

Raw derivative or determinant thresholds are forbidden across differently
scaled representations. Pair tangency uses the normalized tangent determinant.

## 6. Lifecycle and failure

The algorithm captures current source identities/revisions/topology, computes
privately, and publishes exactly one immutable result. Any exception, budget
exhaustion, unsupported capability, or revision mismatch produces a coherent
current status; it never combines an old point with a new result. The point
consumer searches only by semantic token in the current result.

## 7. State boundary

Initial implementation is query-local. Test-private characterization retained
zero entries and showed deterministic linear repeated-query work. A future
owner/index requires:

- measured repeated duplicate work that exceeds approved budgets;
- construction/source/revision/topology scoping;
- deterministic bounded capacity and eviction;
- cache-disabled semantic equality;
- no hidden DAG dependency or cross-consumer lifecycle;
- separate ADR/author approval.

## 8. Compatibility boundary

Likely productive edits are confined to GeoCeDG-owned shared-kernel intersection
packages plus the smallest append-only `GeoClass` or DAG plumbing already
approved by G8 if actually required. No dispatcher/command, public `Path`, XML,
legacy `GeoLocus`, Classic, frontend, 3D, or G9 integration is part of either
implementation contract.

## 9. Phase gates

G8C1 has been executed, verified, and closed as `PASS — AUTHOR APPROVED`.
The final comparison with that implementation found no contradiction in the
dedicated two-parameter design. ADR 0009 is Accepted, the pair contract is
normative, and G8C2 is authorized but not started. G9 remains blocked until
G8C2 passes, receives author approval, and G8 receives global author closeout.

## 10. G8C1 implementation mapping

- `LocusIntersectionTargets2D` performs closed typed assessment and capture.
- `NondegenerateConicIntersectionTarget2D`,
  `BoundedFunctionGraphIntersectionTarget2D`, and
  `RegularPolynomialImplicitIntersectionTarget2D` snapshot target authority.
- `ExtendedTargetIntersectionCapability2D` supplies bounded query-local
  candidate isolation, safeguarded refinement and local-minimum even-contact
  discovery.
- `LocusIntersectionSolver2D` still owns independent residual/membership
  verification, rich solution construction and atomic result publication.
- `AlgoLocusIntersectionV2` captures target state through the same normal DAG;
  the existing rich Geo and strict token point consumer are unchanged.

No retained intersection state is added. The representative query uses 256
subdivisions, 414 source evaluations, 411 target evaluations and 95 refinement
iterations, with zero retained entries and forbidden-authority reads.

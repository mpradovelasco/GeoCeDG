# G8C — Extended Locus V2 2D intersections design

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Baseline | G8B/G8B-R1 `PASS — AUTHOR APPROVED` |
| G8C1 implementation | **PASS — AUTHOR APPROVED** |
| G8C2 implementation | **NOT AUTHORIZED / NOT STARTED** |
| Contract | G8C1 **NORMATIVE — AUTHOR APPROVED**; G8C2 **PROPOSED — NOT NORMATIVE** |
| Proposed ADR | ADR 0009 **Proposed** |
| Author-review date | 2026-08-15 |

## 1. Objective

G8C completes the design of native, representation-independent 2D incidence
needed before spatial G9. It extends the approved G8B semantic chain without
changing it:

```text
semantic evaluator/session
    -> candidate isolation
    -> semantic refinement
    -> independent verification
    -> immutable rich result
    -> normal-DAG nonnumeric rich Geo
    -> strict token-selected internal point consumer
```

The structural CeDG use is a locus-defined projection intersected with an
ordinary curve or another semantic locus, followed by an identified dynamic
point that can drive later construction. Coordinates, render vertices,
`myPointList`, legacy Locus samples, viewport, zoom, DPI, and pixel tolerance
are never geometric or identity authority.

## 2. Scope and exclusions

The design covers four deferred families:

- nondegenerate ellipse, parabola, and hyperbola targets;
- explicitly bounded real-valued `GeoFunction` graphs;
- a typed regular finite-coefficient polynomial `GeoImplicitCurve` subset;
- Locus V2 × Locus V2 over declared finite components and periodic
  fundamental domains.

The author closeout authorizes only the separately invoked G8C1 productive
phase. This design package itself adds no productive Java. It does not authorize
G8C2, commands, generic `Path`, public
point-on-locus behavior, XML/persistence/migration, legacy `GeoLocus`, Classic
semantic changes, 3D, Python DSL, shared/global caches, G9, or unrestricted
claims about functions, implicit curves, singularities, degeneracies, or
unbounded domains.

## 3. Recommended subdivision

One implementation phase is not sustainable. The mathematical dimension,
identity evidence, completeness proof, and overlap semantics differ materially.

### G8C1 — extended one-parameter target adapters

Implement the three typed target families above by extending the G8B
one-dimensional query pipeline. Each adapter owns domain, incidence, normalized
verification residual, derivative/normal evidence, bounds capability, and
support diagnostics. Candidate level functions and verification residuals are
distinct contracts.

Entry gate: satisfied by the 2026-08-15 author closeout for the design,
G8C1-specific normative specification, supported subsets, and normalized
residual contract. The canonical G8C1 prompt has now been executed from the
  published design-closeout baseline; the resulting internal kernel passed its
  author closeout on 2026-08-15.

Exit gate: all three mandatory subsets implemented internally; G8B regression
green; analytic and independent references pass; Option B point admissibility,
identity, completeness, invalid-domain barriers, tangency, deterministic budgets,
and compatibility pass; then explicit author approval.

### G8C2 — dual-parameter Locus V2 × Locus V2

Implement a separate query/solver over branch/component parameter rectangles.
Reuse the rich result, lifecycle, publication, token-selected point, tolerance
vocabulary, and evaluator sessions, but do not disguise a two-dimensional root
problem as a one-dimensional target adapter.

Entry gate: G8C1 `PASS — AUTHOR APPROVED`, ADR 0009 Accepted, and explicit
author approval of source symmetry, local pair isolation, overlap taxonomy,
and completeness limits.

Exit gate: bounded/periodic finite-domain pair support, source-order symmetry,
local isolation, tangency/ambiguity, overlap, constructive multiplicity,
dynamic two-source lifecycle, nesting depths 1/2/3, deterministic budgets, and
all compatibility gates pass; then explicit author approval.

No G8C3 is predeclared. Unsupported singular, degenerate, nonpolynomial, or
unbounded cases remain explicit future decisions.

### G8C1 execution disposition

The candidate implements the three target subsets through the existing G8B
result/lifecycle pipeline. Its default query-local adaptive capability verifies
roots conservatively and reports global completeness as `NOT_ESTABLISHED`;
stronger injected analytic/certified capabilities may retain justified
completeness evidence. The 38 focused tests and measured counters are recorded
in the
[G8C1 kernel report](../validation/g8c1_locus_v2_extended_target_intersection_kernel_report.md).
This is an implementation record, not author approval.

## 4. Preserved G8B contract

- `COMPLETE`, `INCOMPLETE`, and `NOT_ESTABLISHED` remain set-level completeness.
- Option B remains normative: a verified, locally established, current,
  unambiguous solution may be point-admissible without global completeness.
- Durable identity is not coordinate, order, parameter, isolating interval,
  revision, residual, or completeness.
- Merge/split genealogy is not universal; ambiguity is explicit.
- Publication is atomic and query-local; no G7 metric state is reused.
- Overlap is typed and never reduced to sampled points.

## 5. Characterized contracts

### 5.1 Single-target residuals

- Nondegenerate conics use their canonical conic geometry and a regular-point
  first-order normal residual `G/||grad G||`. This is invariant under nonzero
  equation scaling but is not claimed to be exact Euclidean distance.
- Functions use vertical incidence `y-f(x)` as a model-length residual. A
  derivative-normalized first-order normal estimate may refine diagnostics at
  regular points, but cannot be advertised as exact distance.
- The first implicit subset is finite-coefficient polynomial curves at regular
  roots, with scale-invariant `G/||grad G||`. Singular roots need stronger typed
  evidence or remain unclassified/inadmissible.

### 5.2 Locus-locus

A finite solution binds an unordered geometric source pair to ordered
computation evidence `(t,u)`, both revisions, both branch/component preimages,
an isolating parameter rectangle, pair residual, normalized tangent determinant,
continuation status, and an opaque token. Operand reversal must preserve
geometric identity while swapping revision-scoped evidence.

`LocalIsolationStatus.ESTABLISHED` requires an exhaustive isolating region plus
either certified uniqueness or a justified regular-Jacobian uniqueness result.
Newton convergence or small residual alone is insufficient. Tangent/singular
pairs require higher-order or certified evidence; otherwise classification and
point admissibility remain not established.

## 6. State and performance policy

Both phases start query-local. A shared index/owner requires measured duplicate
work, a bounded revision-scoped proposal, semantic equivalence with state
disabled, exception safety, and separate architectural approval. Whole-locus
regeneration, render or legacy sample reads, G7 metric-index reads, viewport
truncation, unbounded history, and stale cross-revision reuse remain forbidden.

Deterministic counters and budgets are defined in the benchmark plan. Wall
clock is informational only.

## 7. G9 gate recommendation

G9 must not start until G8C1 and G8C2 both pass and receive author approval.
Nondegenerate conics, bounded functional graphs, the regular polynomial
implicit subset, and bounded/periodic Locus V2 × Locus V2 are structural 2D
incidence needed by CeDG projections. Degenerate conics, unrestricted functions,
nonpolynomial/general implicit curves, singular-point promotion, and unbounded
pair completeness may remain deferred because the first typed contract reports
them honestly rather than leaving a missing structural family.

## 8. Deliverables

- [G8C1 normative/G8C2 proposed specification extension](../../geocedg/specs/locus/locus-v2-extended-intersections.md)
- [Semantic model](../architecture/locus_v2_extended_intersection_semantic_model.md)
- [Architecture](../architecture/locus_v2_extended_intersection_architecture.md)
- [Upstream impact](../architecture/locus_v2_extended_intersection_upstream_impact.md)
- [Capability matrix](../architecture/locus_v2_extended_intersection_capability_matrix.md)
- [Candidate API](../developer/locus_v2_extended_intersection_api.md)
- [Characterization report](../validation/g8c_locus_v2_extended_intersection_characterization_report.md)
- [Validation matrix](../validation/g8c_locus_v2_extended_intersection_validation_matrix.md)
- [Benchmark plan](../validation/g8c_locus_v2_extended_intersection_benchmark_plan.md)
- [Scientific traceability](../validation/g8c_locus_v2_extended_intersection_scientific_traceability.md)
- [Proposed ADR 0009](../adr/0009-locus-v2-locus-intersection-pair-semantics.md)

## 9. Author-decision table

| Question | Evidence | Alternatives | Recommendation | Impact if rejected | Mandatory gate |
|---|---|---|---|---|---|
| Unified G8C? | One-parameter `h(t)` versus two-parameter `H(t,u)`; different isolation and overlap | One phase; two phases | G8C1 then G8C2 | A unified phase couples unrelated solvers and review risk | Before G8C1 |
| G8C1 conics | `GeoConicND` supplies canonical type/matrix/axes; probes pass scale/contact cases | All conics; nondegenerate; none | Ellipse/parabola/hyperbola only | Degenerate subtype semantics remain deferred | Before G8C1 |
| Degenerate conics | Upstream represents point/line-pair/double/parallel/empty subtypes | Generic adapter; typed subadapters; defer | Defer | Later explicit gate; no false smooth residual | Before promotion if added |
| Conic residual | Raw quadratic scales; normal quotient cancels scale at regular roots | Raw equation; exact distance; first-order normal | Canonical regular first-order normal, typed guarantee | Rejecting blocks trustworthy conic acceptance | Before G8C1 |
| Function subset | Domain API is explicit only after interval binding; path bounds may read view | All functions; bounded real graphs; defer | Explicitly bounded real `GeoFunction` graphs | Unrestricted graphs stay unsupported | Before G8C1 |
| Function gaps | Poles/nonfinite evaluations and conditional boundaries are observable | Bridge; split; fail query | Split valid components; never bridge | May reduce nominal coverage but preserves truth | Before G8C1 |
| Function derivative | Derivatives exist but may be nonfinite/unsupported | Require; hierarchy; ignore | Analytic derivative when valid, safeguarded derivative-free fallback with weaker guarantee | Tangency coverage becomes narrower if rejected | Before G8C1 |
| Implicit subset | `GeoImplicitCurve` includes polynomial and general expressions; only polynomial coefficients give closed scale contract | “General”; polynomial regular; defer | Finite-coefficient regular polynomial subset | Nonpolynomial and singular promotion deferred | Before G8C1 |
| Implicit residual | Raw `G` scales; `G/||grad G||` cancels at regular points | Coefficient norm; gradient norm; family-specific | Gradient-normalized typed first-order residual | Rejecting blocks honest implicit support | Before G8C1 |
| One-parameter reuse | G8B solver already owns lifecycle, roots, Option B, budgets | New solver; adapter extension | Reuse through typed adapter capabilities | Duplication and semantic drift otherwise | Before G8C1 |
| Pair solver | Pair roots need rectangles/Jacobian/source symmetry | Force adapter; separate solver | Separate dual-parameter query/solver | G8C2 becomes unsound if forced into `h(t)` | Before G8C2 |
| Source order | Geometry is commutative; parameter evidence is ordered | Ordered identity; unordered identity | Canonical unordered source-pair identity, swapped evidence | Tokens differ under argument reversal | Before G8C2 |
| Pair token | Coordinate/order/parameters fail reparameterization and multiplicity | Coordinate; pair indices; opaque lineage token | Opaque token scoped to unordered pair and constructive/topology lineage | No stable downstream CeDG point | Before G8C2 |
| Pair isolation | 32-probe suite shows residual and tangency are orthogonal | Residual only; regular Jacobian region; certification | Exhaustive region plus uniqueness/Jacobian evidence; higher-order path for tangency | Fewer admissible tangencies, but no false roots | Before G8C2 |
| Pair tangency | Normalized determinant is scale-free but zero does not prove multiplicity | Raw determinant; normalized hierarchy | Normalized determinant plus analytic/certified/higher-order evidence | Classification stays undetermined when evidence lacks | Before G8C2 |
| Overlap | Reparameterized identical curves cannot be finite samples | Sample; established/suspected/unsupported | Typed established/suspected/unsupported; mixed finite only when decomposed | General overlap remains explicit unsupported | Before G8C2 |
| Pair completeness | Candidate exhaustion is not coverage; G6 domains finite/periodic only | Always unknown; bounded proof hierarchy | `COMPLETE` only with exhaustive component-pair isolation | More `NOT_ESTABLISHED`, Option B still useful | Before G8C2 |
| Periodic/unbounded | Fundamental domains exist; `LocusInterval2D` rejects infinity | View window; extend G6 now; bounded/periodic only | Fundamental-domain semantics; unbounded unsupported | Unbounded work is a later G6/G8 extension | Before G8C2 |
| Shared state | Query-local probes retained zero entries; no evidence of need | Global/shared/query-local | Query-local | Potential speed deferred, correctness isolated | Before either implementation |
| G8 closeout/G9 | Scientific chains require all four typed families, especially locus-locus | Start G9 after G8C1; after G8C2 | Require G8C1 and G8C2 author-approved | G9 remains blocked longer but 2D foundation is coherent | G9 entry |

## 10. Author closeout

On 2026-08-15 the author approved the G8C design, the G8C1/G8C2 subdivision,
every G8C1 decision in the table above, query-local state, and the gate that
both implementation phases must pass before G8 global closeout or G9. The
  G8C1-specific contract and internal implementation are `PASS — AUTHOR
  APPROVED`. Pair-specific G8C2 decisions remain proposed: G8C2 is not
authorized and ADR 0009 remains Proposed.

```text
G8C DESIGN = PASS — AUTHOR APPROVED
G8C1 = PASS — AUTHOR APPROVED
G8C2 = NOT AUTHORIZED — NOT STARTED
G8 = IN PROGRESS
G9 = NOT STARTED
```

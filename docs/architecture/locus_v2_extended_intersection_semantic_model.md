# Locus V2 extended-intersection semantic model

**Status: G8C1 AND G8C2 SEMANTICS NORMATIVE — AUTHOR APPROVED**
**Scope: G8C1 and G8C2 internal kernels `PASS — AUTHOR APPROVED`; global G8 closed**

Sections applying to one semantic source and a typed extended target are the
author-approved G8C1 semantic contract. Dual-source/pair-specific sections are
the author-approved normative G8C2 model governed by Accepted ADR 0009.

## 1. Common result model

G8C reuses the G8B immutable rich set and normal-DAG rich Geo. It adds typed
query and per-solution evidence; it does not add another result authority.

```text
ExtendedIntersectionQuery2D
  source locus identity/revision/topology
  target binding OR second semantic-locus binding
  versioned policy/budgets

LocusIntersectionResult2D
  computation status
  result kind
  completeness                 (set level)
  numeric guarantee
  currentness/revisions
  finite solutions / overlap evidence / diagnostics

LocusIntersectionSolution2D
  opaque durable token
  constructive + branch/component lineage
  revision-scoped localization and verification evidence
  local isolation + classification + identity status
```

`PointAdmissible(solution,result)` remains local to the selected finite
solution. Parent completeness is retained provenance, not a universal veto.

## 2. Single-target binding

`ExtendedTargetAdapter2D<T>` is a closed family-specific capability object:

```text
identity + revision + support
domain/invalid-boundary authority
candidate level function
normalized verification residual (typed guarantee and units)
membership verification
normal/derivative capability
semantic bounds capability
degeneration/overlap classification
```

Adapters do not convert targets to sampled loci. The conic adapter binds
nondegenerate canonical conic data; the function adapter binds an explicit
finite graph domain; the implicit adapter binds a finite polynomial coefficient
representation and regular-point evidence.

## 3. Dual-source query

`LocusPairIntersectionQuery2D` binds two current semantic sources. Its geometry
is commutative even though a deterministic solver orders the operands.

```text
geometricSourceKey = canonicalUnorderedPair(sourceA, sourceB)
orderedEvidence = (revisionA, branchA, componentA, t,
                   revisionB, branchB, componentB, u)
```

A pair solution adds:

- source A/B identity, revision, branch/component and lineage;
- evaluated points on both sides and coordinate residual;
- an isolating rectangle `I_t × I_u` and its coverage/uniqueness basis;
- tangent/Jacobian evidence and classification;
- pair continuation/topology context;
- overlap relation if the solution is non-isolated.

Operand reversal maps `t <-> u`, A/B evidence, branch-pair orientation, and
determinant sign. It preserves the canonical source pair and token.

## 4. Identity model

Durable identity conceptually includes:

- canonical source-pair identity;
- constructive intersection lineage;
- established branch or branch-pair lineage;
- topology epoch/continuation context;
- explicit continuation relation.

Revision evidence includes parameter(s), isolating interval/rectangle, source
revisions, component bindings, residuals, tangent/Jacobian data, and solver
certificate. Neither revision evidence nor coordinate/order is identity.

Additional roots do not renumber existing tokens. Equal coordinates from
different semantic preimages remain distinct. Reparameterization and operand
reversal preserve identity only with an explicit semantic equivalence mapping.

## 5. Completeness and local isolation

Completeness asks whether every solution over all supported declared components
was found. Local isolation asks whether one returned finite solution is uniquely
established in a local semantic neighborhood. These are orthogonal.

For one parameter, G8B isolation semantics continue with target-specific level
and verification evidence. For two parameters, `ESTABLISHED` needs both:

1. exhaustive coverage of a parameter rectangle separating the candidate from
   other solutions; and
2. certified uniqueness or a justified local regularity/uniqueness argument.

A nonsingular Jacobian supports a transverse regular-root contract but does not
replace exhaustive isolation. A singular Jacobian does not imply absence;
tangent roots require stronger evidence or remain unisolated/undetermined.

## 6. Result-kind and overlap model

Finite and overlap information must not be collapsed. Normative pair-level
outcomes include:

- finite isolated solutions;
- `OVERLAP_ESTABLISHED` with semantic extent/parameter relation;
- `OVERLAP_SUSPECTED_NOT_ESTABLISHED`;
- `UNSUPPORTED_OVERLAP`;
- mixed established finite and overlap contributions only when the rich set can
  represent both without pretending completeness.

Matching evaluations or boxes are candidate evidence only. Repeated traversal
and opposite orientation preserve constructive multiplicity.

## 7. Lifecycle states

Every recomputation publishes one coherent current result. A derived point is
undefined for absent/stale/unverified/unisolated/ambiguous/overlap-only/current-
failure states and cannot retarget. Recovery requires the same token through an
approved continuation. If correspondence is not unique after merge, split,
seam, topology change, or overlap transition, the identity event is explicit.

## 8. Domain model

Valid semantic components are mandatory. Function gaps and nonfinite values are
barriers. Periodic branches use a canonical fundamental domain plus seam
equivalence. A finite window cannot stand in for an unbounded domain and cannot
support `COMPLETE`.

## 9. Guarantee vocabulary

Reuse G6/G8 guarantee levels: exact/analytic, certified, estimated or
floating-point uncertified, and unsupported. Each residual declares its units
and physical meaning. First-order normal residuals are not exact distance;
derivative-free or evaluator-only strategies publish weaker guarantees.

## 10. Author-approved G8C1 implementation

The internal candidate realizes the single-target binding as the existing
`LocusIntersectionTarget2D` plus typed support, domain, candidate-level and
residual-evaluation values. The closed families are `ELLIPSE`, `PARABOLA`,
`HYPERBOLA`, `BOUNDED_FUNCTION_GRAPH`, and
`REGULAR_POLYNOMIAL_IMPLICIT`.

The default extended capability keeps completeness `NOT_ESTABLISHED`; local
verification is not extrapolated into global coverage. A regular transverse
root with semantic localization may be `LocalIsolationStatus.ESTABLISHED`.
Even-contact roots discovered through a local minimum are verified but remain
locally unisolated unless stronger uniqueness evidence exists. This preserves
Option B without making residual-only candidates consumable.

The author approved this implementation contract and its measured query-local
baseline on 2026-08-15. G8C1 is `PASS — AUTHOR APPROVED`. The later final
source-level review approved the pair-specific G8C2 model as normative and
authorized its separate implementation. The resulting internal candidate now
realizes the pair model without changing the normative semantics; author review
is still required.

## 11. G8C2 internal-candidate realization

`LocusPairIntersectionQuery2D` canonicalizes the unordered geometric source
pair while retaining reversible ordered evidence. Every finite solution carries
two `LocusPairSourceRevisionEvidence2D` values, a
`LocalPairIsolationEvidence2D`, independently evaluated
`LocusPairResidualEvidence2D`, normalized tangent-determinant evidence when
available, and the existing opaque root token.

The evaluator-only fallback visits query-local semantic parameter boxes and
uses safeguarded dual-variable refinement. Its roots are verified on both
semantic sources, but it deliberately reports global completeness and local
isolation as `NOT_ESTABLISHED`; Newton convergence and a small residual are not
promoted into uniqueness. Analytic or certified capabilities may provide an
exhaustive rectangle and uniqueness basis, which enables Option B point
consumption independently of global completeness.

Overlap evidence is typed as established, suspected-not-established, or
unsupported. Agreement of evaluator samples can produce suspicion only. Mixed
finite-plus-overlap results preserve both contributions. Unbounded components
are rejected rather than truncated, and periodic providers retain fundamental-
domain/seam authority.

# ADR 0027: Locus V2 existence and continuous-valid component certification

- Status: **Accepted — PRE-G9B-S1-R2 DESIGN — AUTHOR APPROVED**
- Date: 2026-09-13
- Phase: `PRE-G9B-S1-R2`
- Parent authority: [Locus V2 semantic contract](../../geocedg/specs/locus/locus-v2-semantics.md)
- Detailed design: [PRE-G9B-S1-R2 architecture](../architecture/pre_g9b_s1_r2_locus_existence_components_design.md)
- Normative candidate: [existence/component certification](../../geocedg/specs/locus/locus-v2-existence-components.md)

## Context

The G6 model distinguishes a declared driver domain from a valid subset, but
the implementation currently stores both meanings in
`LocusBranch2D.getValidDomainComponents()`. Several producers populate that
list from the driver's nominal domain. A reconstructible dependent evaluator
can still return `DEPENDENCY_UNDEFINED` inside such a component.

The author fixture `m = LocusV2(U,R)` is the concrete witness. Its periodic
circle provider declares `[-pi,pi)`, and that is a valid canonical driver
domain. Evaluation is valid at `-pi`, canonicalized `pi`, `0`, and `pi/2`, but
is dependency-undefined at `-pi/2` and through a wider internal interval. The
selected intersection-bound point `S`, then midpoint `U`, cease to exist there.
The present component list therefore cannot truthfully mean that the complete
dependent construction exists and remains continuous throughout `[-pi,pi)`.

Rendering, metrics, intersections, point/path operations, nested loci and DXF
all consume the same component list. An export-local partition would create a
second semantic authority. Repeated point samples cannot prove that the
intervals between them are valid or that no smaller invalid island was missed.

The current intersection token ledger, exact address proof and revision
evidence establish a selected root at a current construction revision. Existing
continuation compares roots across construction revisions. None proves that one
selected semantic root exists uniquely and continuously for every driver value
in a parameter interval.

## Decision

1. Locus V2 separates four axes per branch:
   - canonical/declared driver domain;
   - certified existence coverage;
   - certified continuous-valid components; and
   - global decomposition completeness (`COMPLETE` or `NOT_ESTABLISHED`).
2. A hybrid architecture is used. The generator publishes the canonical domain,
   structural event information and any provider-owned interval evidence. A
   shared kernel certifier combines that evidence with dependency and selected-
   root interval evidence and publishes one immutable, revision-bound existence
   structure.
3. A continuous-valid component requires an oriented interval, explicit endpoint
   policy, interval-wide semantic existence, continuation of the same semantic
   construction/root selector, absence of a certified internal gap, deterministic
   proof evidence and an exact source semantic-revision binding.
4. The certificate may publish locally certified components while global
   completeness remains `NOT_ESTABLISHED`. Uncovered parameter regions remain
   explicit; local proof never becomes a complete-locus claim.
5. Boundary authority is sought in this order: structural/provider events,
   interval root-existence/continuation proof, analytic boundaries, certified
   interval predicates, then bounded numerical isolation driven by such a
   predicate. Pointwise success/failure and render sampling may locate work but
   cannot certify an interval.
6. Existing root tokens, source-pair identity, semantic selectors and branch
   lineage remain root identity authority. A new interval-level root-continuation
   certificate is required where a dependent point is selected from a rich
   intersection result. It proves interval validity for that selector; it does
   not allocate, replace or retarget a token.
7. Continuous components are revision-local semantic addresses. They receive
   durable lineage only when a provider or certifier establishes an explicit,
   unambiguous lineage relation across revisions. Interval order, component
   ordinal and numerical boundary coincidence are not durable identity.
8. `getValidDomainComponents()` is not silently redefined. A successor existence
   structure is introduced first. The legacy accessor remains a compatibility
   view only for branches whose complete continuous-valid decomposition is
   established; every consumer migrates to an explicit local/global rule before
   incomplete local components can be published.
9. Old files do not gain guessed components or identities. Certificates are
   recomputed from reconstructible semantic inputs and versioned policies on
   load, undo, redo and redefine. Failure to re-establish proof publishes
   `NOT_ESTABLISHED`, never stale intervals.

## Consumer rules

- Rendering may display locally certified components, without a completeness
  claim and without joining uncovered regions.
- Component-local metrics may consume one certified component. A total/global
  metric requires `COMPLETE`; a sum of known components is labelled partial
  evidence and is not returned as the total locus measure.
- Intersections may publish independently verified local solutions, preserving
  the existing independent result-completeness axis. An empty/global-complete
  result requires complete domain coverage.
- Point/path operations may consume an exact current address inside a certified
  component. Nearest/global search and traversal across an unresolved region
  remain inadmissible.
- DXF may approximate each finite certified component independently. With global
  completeness `NOT_ESTABLISHED`, publication is admissible only through an
  explicitly incomplete semantic-fidelity result and mandatory sidecar; it must
  not be described as the complete locus. Strict component approximation and
  paired publication remain unchanged.
- Nested producers propagate the source completeness axis and may not promote
  a locally certified support subset to a complete downstream domain.

## Consequences

- The canonical domain remains a provider property even when the dependent
  construction exists on several disjoint intervals or cannot be decomposed
  completely.
- Temporary invalidity does not replace the locus or root selector. It may split
  current continuous-valid coverage, and recurrence only reuses lineage when
  the approved continuation contract proves it.
- `SemanticGeneratorDescriptor1D` can no longer require a periodic generator's
  existence components to equal its whole canonical half-open domain. Periodicity
  applies to the canonical parameterization; existence coverage is separate.
- Existing static/analytic producers can be lifted to a `COMPLETE` certificate
  when their present contract really proves interval-wide validity. Dependent
  producers fail closed as `NOT_ESTABLISHED` until suitable evidence exists.
- The bounded selected SplineV2/parallel-line root capability used by `m`
  publishes conservative interval-wide proofs and leaves adjacent transition
  cells unresolved. Therefore `m` has useful revision-local components while
  its global decomposition remains `NOT_ESTABLISHED`.

## Rejected alternatives

- Treat the declared driver interval as the existence domain.
- Infer continuous components from adaptive or dense point sampling.
- Let rendering or G9X1 maintain a separate component partition.
- Retarget the selected root by coordinate proximity, solver order or current
  component ordinal.
- Make numerically close interval boundaries durable component identity.
- Suppress global incompleteness when one or more local components are certified.
- Return no useful local result until the complete global decomposition is known.

## Approval boundary

The author approved this design and its bounded I1--I3 implementation route.
Implementation evidence remains a candidate pending separate author review;
this ADR does not claim implementation approval or parent-phase PASS.

# ADR 0028: Semantic endpoint admissibility and certified semantic-pair materialization

- Status: **Proposed — `PRE-G9B-R2-E0/E1` technical candidate**
- Date: 2026-09-23
- Phase: `PRE-G9B-R2-E0/E1` (one combined sequential task authorized by the author)
- Amends: [ADR 0021](0021-spline-pair-singleton-germ-materialization.md) Decision 1 only
- Extends: [locus-v2-metrics.md](../../geocedg/specs/locus/locus-v2-metrics.md) §21 and §23
- Normative contracts: [certified construction model](../../geocedg/specs/locus/locus-v2-certified-construction-model.md),
  [metrics §24](../../geocedg/specs/locus/locus-v2-metrics.md),
  [pair materialization §10](../../geocedg/specs/curves/spline-v2-pair-materialization.md)
- Design record: [Stage-1 design](../architecture/pre_g9b_r2_e0_e1_semantic_admissibility_and_materialization_design.md)

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
selfApproved              = false
```

This record carries no approval of its own; acceptance is recorded only by a
separate author-decision record.

## Context

ADR 0021 Decision 1 limits exact-token pair materialization to authenticated
SplineV2 × SplineV2 component pairs and keeps generic Locus V2 pairs rich-only.
The limit reflected a real proof gap: the certifier needed an outward
enclosure of each curve and its derivative, and only the structural spline
model (ADR 0022) supplied one. The reconstructible dependent-point evaluator
replays an isolated construction in binary64 and has no enclosure semantics.
The author witness `SeveralDefects.cedg` (`e = Intersect(d,a)`, spline × point-
driven ellipse) exposes that gap: two clean transverse roots stay rich-only, and
the rich result says nothing about why.

Independently, the metric admits only three endpoint families (§21, §23). Points
that lie on the addressed curve by explicit construction — roots of a Locus V2
against a line or circle, the traced generator point of a locus, similarity
images and copies of admitted points — are rejected, although their provenance
is exact. The author requires the maximum scientifically defensible admission
and materialization, eligibility by capability rather than Java class name, and
`local admissibility != global completeness`.

## Decision

1. **Three levels stay separate.** Point existence/materialization, semantic
   membership of a point on a curve, and metric endpoint admissibility are
   distinct questions. None implies another. The only admitted non-membership
   endpoint remains the approved A1 constructor transport through R5 lineage.
2. **Pair materialization is admitted per side by capability.** A pair is
   eligible for the unchanged `pair-singleton-transverse-germ/v1` publication
   exactly when both sides expose a *certified interval curve model*: an object
   that encloses the side's semantic curve and first derivative outward over
   any parameter box, reports smoothness, period and canonicalization, and is
   coherent with the side's own evaluator. The structural spline model is one
   such model. This replaces the family restriction of ADR 0021 Decision 1;
   Decisions 2–12 are retained unchanged.
3. **The minimum additional proof capability is a certified construction
   model.** A reconstructible evaluator may capture, from its own isolated
   slice, an immutable construction program over a closed class of drivers and
   GeoGebra algorithms. Its authority is the exact-real evaluation of the
   evaluator's own formula sequence on the captured constants, including each
   algorithm's branch predicates evaluated on exact values; any box on which a
   predicate is not uniformly decided is unresolved. The floating evaluator
   approximates this function, and every certified root must also pass the
   existing floating verification. The class is fixed by the construction-model
   specification and is versioned (`certified-construction-program/v1`).
4. **Nothing in the identity chain changes.** The selector, its canonical
   ordering, the contract identifier `d2-spline-pair-singleton-germ/v1`, the
   ledger v5 binding `(contract, selector)`, quarantine, dormancy, reactivation
   and the explicit-materialization gate are reused. They are independent of
   the certification method: certificate material was never a selector field,
   so no selector value changes meaning and no ledger format changes.
5. **Rich-only must be diagnosed.** When a side has no certified model, the
   published result states that explicitly; a pair is never silently rich-only.
6. **Endpoint admission is membership-based and extends to every exactly
   addressable family.** Admitted families are the explicit semantic point,
   pair- and single-source intersection occurrences, the generator occurrence,
   similarity images with identical transform parameter objects, dependent
   copies, and the unchanged constructor occurrence. Images and copies compose
   only membership results, never a transport.
7. **No retargeting across families.** A metric retains both the accepted
   family and, where the family defines one, its versioned occurrence key; a
   later change of either is retargeting and fails closed.
8. **Transformation covariance carries membership; nothing is transported.** An
   intersection root, a semantic point, a copy or a generator of `S` does not
   address `T(S)`. If `P` is a membership endpoint of `L`, `P2 = T(P)` and
   `L2 = T(L)`, and both derivations prove the same semantic transformation `T`
   (the same versioned similarity contract, the same parameter objects by durable
   identity and the same lineage), then `P2` is a membership endpoint of `L2`.
   Equal numerical values never establish sameness, and `T(P)` and `T(L)` keep
   their own durable identities. In particular the generator `E` is not an
   endpoint on `T(S)`, and `T(E)` is.

## Consequences

- `SplineV2 × modeled LocusV2`, its caller reversal, `modeled × modeled`, and
  similarity images of modeled families can materialize certified singleton
  roots while global completeness stays `NOT_ESTABLISHED`. The `SeveralDefects`
  pair is the mandatory witness.
- The circle-point canonical domain is not exactly closed under real `cos` and
  `sin`; the construction model reports no period, and roots in the seam band
  remain unresolved rather than being identified across a gap.
- The approved G9S1-R1 regression that pinned a generic affine pair as rich-only
  now expresses the capability boundary with an uncertifiable generic locus; the
  affine pair itself is a new positive. Two approved G9U0 regressions pinned the
  same rich-only state for affine scalar pairs; they now assert the certified
  behaviour (design record §14, D-C5).
- Families without a model stay rich-only with an explicit reason: locus-support
  generators, non-affine scalar expressions, slices through proximity-ordered
  conic intersections or selected Locus V2 roots, and any operation outside the
  class.
- `Length(S,P,Q)` becomes defined for the new endpoint families; every
  ambiguous, stale, dormant, unproved or non-matching case yields
  `INVALID_QUERY` with a typed diagnostic.
- Direct families are consulted before derived ones, including the §21
  constructor occurrence, so no approved A1 address can change. An intermediate
  step of a nested derivation has a structural address: its derivation root,
  operation contract, versioned step path and parameter identities. It is never
  an identity, index, label, order, reference or value of its own. A family
  change cannot reach a retained metric
  object under the current lifecycle (`NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE`).
  These Stage-2 corrections are recorded in the design record §14.
- A closure copy remaps every structural participant a parameterization
  contract names, such as a generator's driver, together with the sources. The
  copied relation refers only to copied identities, the copied point stays
  current, and the persisted ledger format is unchanged.
- Implementation surfaced retained, fail-closed limitations of the existing host
  and evaluator. It also surfaced two pre-existing wrong-value defects of the
  reconstructible evaluator, retained as product debt: the segment-driver lag,
  and slices with inline literals in two commands. All are listed in the design
  record §14. The certified model refuses both defect shapes, so this decision
  produces no certified root, materialized point or length from them.

## Rejected alternatives

| Alternative | Reason |
|---|---|
| admit pairs by the Java class of the locus | contradicts the capability rule; a spline image and a construction locus share `GeoLocusV2` |
| certify from floating evaluator samples or a residual threshold | not a proof; forbidden by ADR 0021 Decision 5 |
| a new selector or contract identifier for generic pairs | ties durable identity to certificate material; the existing selector is family-agnostic |
| treat the circle-point domain as exactly periodic | false by about `2.45e-16 r`; would certify roots of a curve the locus does not define |
| model expressions, conic intersections or selected roots now | no bounded certified semantics exists; each is a separate capability |
| admit a point because its coordinates lie on the curve | coordinates are never endpoint authority |
| transport intersection or semantic points through R5 images | the root belongs to the intersected source, not to its image (§23.8) |
| accept equal-valued literal transform parameters as lineage | value comparison, not DAG provenance |

## Approval boundary

This ADR is a technical candidate. It authorizes nothing beyond the combined
`PRE-G9B-R2-E0/E1` task that produced it, and it does not authorize `R3` or any
later phase, `G9B`, `G9C`, `G9U2`, productive `G10`, release, tag or
publication.

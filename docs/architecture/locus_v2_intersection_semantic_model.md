# Locus V2 2D intersections — proposed semantic model

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT NORMATIVE / NOT IMPLEMENTED** |
| Phase | G8 planning |
| Authority if approved | `geocedg/specs/locus/locus-v2-intersections.md` |

This document explains the candidate value and identity model that G8A must
test. It does not supersede the normative G6/G7 specifications.

## 1. Separate the curve, query, computation, and point projection

```text
semantic sources
  GeoLocusV2 @ locus revision
  supported target GeoElement @ coherent input revision

intersection query
  target adapter + policies + work limits

rich set result
  coverage + geometry kind + solutions + diagnostics + work evidence

optional presentation/output
  ordinary GeoPoints derived from current verified finite solutions
```

The rich set result is the intersection authority. An ordinary point cannot
carry complete-set coverage, overlap, unresolved tangency, residual evidence,
or parent/child identity lineage and therefore cannot be the sole result.

## 2. Conceptual immutable values

The names below are proposals for G8A, not pre-approved Java APIs.

```text
LocusIntersectionQuery2D
  LocusSourceBinding2D
  IntersectionTargetBinding2D
  IntersectionPolicy2D
  IntersectionWorkBudget2D

LocusIntersectionResult2D
  IntersectionComputationStatus
  IntersectionCoverage
  IntersectionGeometryKind
  IntersectionCurrentness
  IntersectionSupportLevel
  source/provenance/work evidence
  LocusIntersectionSolution2D[]
  IntersectionDiagnostic2D[]

LocusIntersectionSolution2D
  IntersectionRootToken2D
  IntersectionRootLineage2D
  LocusIntersectionPosition2D
  optional TargetParameterBinding2D
  LocusPoint2D
  IntersectionResidualEvidence2D
  IntersectionMethodEvidence2D
  IntersectionClassification2D
  NumericGuarantee
```

All containers are defensively copied and immutable. Optional data is
structural. A missing derivative, target parameter, isolation certificate, or
multiplicity proof is represented by a closed variant, not null, NaN, or a
magic scalar.

## 3. Source bindings

### Locus binding

The locus binding contains:

- locus identity;
- semantic revision;
- branch key;
- resolved valid-component identity for that revision;
- provider ID/signature, orientation, periodicity, and canonical parameter
  policy; and
- evaluator/determinism/quality metadata.

A solution position may remain semantically durable while its current binding
changes revision. Automatic rebind is permitted only through an approved root
continuation event.

### Target binding

The target binding contains:

- active-Construction runtime identity;
- target family and adapter version;
- coherent intersection-input revision;
- exact source GeoElement reference through the normal algorithm input;
- equation/incidence provenance and normalization policy; and
- defined/degenerate/limited-domain status.

GeoElement labels and construction-list positions are diagnostics only. G8 has
no persistent target identity contract.

## 4. Result axes and legal states

The result answers four different questions:

1. Did computation execute under the requested contract?
2. How much of the valid source domain was covered?
3. What geometric result-set kind was established?
4. How strong is the numerical/geometric evidence?

These answers are independent. Example legal states include:

| Situation | Computation | Coverage | Geometry | Support |
|---|---|---|---|---|
| certified no root on all components | success | complete | empty | certified |
| three verified roots but completeness unavailable | success | partial | finite | verified uncertified |
| tangent candidate unresolved at work limit | work limit | not established | unresolved | unsupported for existence |
| source branch coincides with target over an interval | success | complete or localized | overlap | exact/certified/verified as evidenced |
| undefined target | invalid input | not established | unresolved | unsupported |

`EMPTY + PARTIAL` and `SUCCESS + stale payload` are illegal. Result factories
should make contradictory combinations unrepresentable.

## 5. Finite-solution position and repeated coordinates

The semantic address of a root is not its world coordinate:

```text
(source pair, topology epoch, branch key, component, canonical t,
 optional target u, root token)
```

Two different `t` values at a self-intersection remain two constructive
solutions even when `F(t1) = F(t2)`. Two branches that retrace the same geometric
curve retain constructive multiplicity. Deduplication occurs in semantic
parameter/isolation space within one preimage, not in Cartesian space.

Periodic endpoint-equivalent parameters are deduplicated only through the
provider's declared canonicalization. The continuation layer may retain a
lifted parameter/winding value privately so a root crossing the seam remains
continuous without creating duplicate public preimages.

## 6. Classification as independent evidence

`IntersectionClassification2D` is a product of independent fields:

```text
contactOrder:
  TRANSVERSE | TANGENT | HIGHER_MULTIPLE | UNKNOWN_MULTIPLICITY

domainLocation:
  INTERIOR | INCLUDED_ENDPOINT | PERIODIC_SEAM | ISOLATED_COMPONENT

sourceRegularity:
  REGULAR | SINGULAR | UNKNOWN

targetMembership:
  FULL_OBJECT | LIMITED_INTERIOR | LIMITED_ENDPOINT
```

The result-set kind separately represents `EMPTY`, `FINITE`, `OVERLAP`,
`INFINITE`, or `UNRESOLVED`. A tangent endpoint is therefore expressible
without inventing a combined enum value.

Multiplicity greater than one is a claim, not a formatting hint. G8A must
define the exact analytic/differential/interval evidence needed for each
supported claim. A verified root with insufficient order evidence uses
`UNKNOWN_MULTIPLICITY`.

## 7. Residual and tolerance evidence

`IntersectionResidualEvidence2D` should record:

- raw target equation value;
- normalized residual and normalization scale/provenance;
- absolute and relative thresholds used;
- optional coordinate-distance or incidence cross-check;
- target limited-domain check;
- evaluation guarantee and target arithmetic guarantee;
- accepted/rejected outcome; and
- whether the evidence is exact, certified, estimated, or floating-point
  uncertified.

Raw line/conic polynomial values are scale-dependent and cannot be compared to
one universal epsilon. Normalization must make multiplication of the target
equation by a nonzero scalar semantically irrelevant.

The G6 `NumericGuarantee` enum remains the shared numeric-guarantee vocabulary.
Intersection coverage/support is a different axis and must not be added to that
enum.

## 8. Root token and lineage

`IntersectionRootToken2D` is an opaque algorithm-owned runtime identity. It is
not computed from a coordinate, label, or parameter-bit hash. A current binding
associates it with semantic evidence.

`IntersectionRootLineage2D` has closed transitions analogous to, but distinct
from, G6 branch lineage:

- `UNCHANGED`;
- `APPEARED`;
- `DISAPPEARED`;
- `SPLIT` with one parent and at least two children; and
- `MERGED` with at least two parents and one child.

Root lineage cannot be inferred solely from branch lineage: one stable branch
may gain or lose roots against a moving target. Conversely, a branch split may
force root-lineage events even when coordinates remain coincident.

### Proposed continuation rules

| Transition | Proposed semantic interpretation |
|---|---|
| one isolated root moves inside the same branch/component | preserve token when isolation intervals and predicted semantic parameter establish continuation |
| two roots merge at a tangent event | terminate both parents; allocate one child with `MERGED` lineage |
| tangent root splits | terminate parent; allocate two children with `SPLIT` lineage |
| periodic seam crossing | preserve token through provider equivalence and lifted continuation |
| included endpoint touch/reversal | retain or event-transition according to characterized local root topology; always record boundary location |
| invalid-domain gap | terminate at the gap; do not bridge it by coordinate proximity |
| branch/component topology change | require compatible provider/root lineage; otherwise begin a new topology epoch |

This new-token-on-merge/split rule deliberately distinguishes stable
continuation from a topological event. G8A must compare it with alternatives
and request author approval.

## 9. Atomic currentness

The rich Geo publisher follows a P1-style lifecycle:

```text
beginInputRevision(current locus revision, current target/input revision)
  -> old payload is no longer current
  -> compute privately
  -> publish one immutable success/failure payload atomically
```

An evaluator/adapter/solver exception produces a coherent current failure, not
an exception-visible mix of old solutions and new diagnostics. Derived points
are updated only after rich publication; they become undefined when their
tokens have no current verified solution.

## 10. Output-slot semantics

If ordinary point outputs are approved, an output handler may manage storage
and labels but not root identity. The algorithm maintains a bounded token-to-slot
association. It reuses a slot for the same continued token, assigns available
slots deterministically for new tokens, and marks unused slots undefined.

The maximum simultaneously representable point outputs is an explicit work/
output budget. Exceeding it makes the rich result incomplete or work-limited;
the algorithm must not silently truncate a claimed complete set. Historical
root events cannot grow the output list or continuation history without bound.

## 11. Ordinary absence, unsupported cases, and overlap

Ordinary no-intersection is a successful complete empty result. Invalid input,
unsupported target, non-deterministic evaluation, unresolved tangency,
uncertified absence, and work exhaustion are different states.

Overlap can have:

- an established semantic parameter interval on one branch/component;
- a collapsed-image continuum of constructive preimages;
- exact/certified/verified evidence; and
- finite boundary solutions in addition to the overlapping subset.

G8A may conclude that the first G8B kernel only detects and reports overlap as
`UNSUPPORTED_OVERLAP` rather than fully parameterizing it. It may not convert
overlap into a finite sample or `EMPTY`.

## 12. Approval questions

Before G8B, the author must approve or replace:

- one rich set Geo plus optional derived points;
- result-axis names and legal combinations;
- root tokens and new-token-on-split/merge lineage;
- parameter/isolation-based continuation;
- classification and multiplicity evidence;
- overlap representation level; and
- the bounded output/currentness lifecycle.

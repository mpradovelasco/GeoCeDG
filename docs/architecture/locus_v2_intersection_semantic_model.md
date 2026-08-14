# Locus V2 2D intersections — proposed semantic model

| Field | Value |
|---|---|
| Status | **AUTHOR-APPROVED PLANNING MODEL — PROPOSED SEMANTICS / NOT IMPLEMENTED** |
| Phase | G8 planning `PASS`; G8A authorized/not started; G8B not authorized |
| Authority if approved | `geocedg/specs/locus/locus-v2-intersections.md` |

This document explains the author-approved planning architecture and the
candidate value/identity details that G8A must test. It does not make the G8
specification normative or supersede the normative G6/G7 specifications.

## Fundamental CeDG capability

Locus-defined projection curves are genuine CeDG geometric results. A supported
Locus V2 intersection must therefore publish semantically identified solutions
that can feed later normal-DAG construction steps whenever continuation is
unambiguous:

```text
CeDG construction -> Locus V2 projection -> identified 2D intersection
    -> downstream CeDG construction -> normal dynamic propagation
```

An anonymous coordinate at one instant is insufficient. Source,
branch/component, constructive preimage, dynamic identity, topology, and
degeneration evidence must remain available. This requirement does not promote
an uncharacterized target family.

## 1. Separate the curve, query, computation, and point projection

```text
semantic sources
  GeoLocusV2 @ locus revision
  supported target GeoElement @ coherent input revision

intersection query
  target adapter + policies + work limits

rich set result
  completeness + geometry kind + solutions + diagnostics + work evidence

optional presentation/output
  ordinary GeoPoints derived from current verified finite solutions
```

The rich set result is the intersection authority. An ordinary point cannot
carry complete-set completeness, overlap, unresolved tangency, residual evidence,
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
  IntersectionCompleteness
  IntersectionGeometryKind
  IntersectionCurrentness
  IntersectionSupportLevel
  source/provenance/work evidence
  LocusIntersectionSolution2D[]
  IntersectionDiagnostic2D[]

LocusIntersectionSolution2D
  IntersectionRootToken2D
  IntersectionIdentityStatus
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

The result answers at least six different questions:

1. Did computation execute under the requested contract?
2. Has the algorithm established that every solution in the supported semantic
   domain is represented?
3. What geometric result-set kind was established?
4. How strong is the numerical/geometric evidence?
5. Is the payload current for both sources?
6. Is each solution's continuation identity established, new, ambiguous,
   discontinuous, or not established?

These answers are independent. Example legal states include:

| Situation | Computation | Completeness | Geometry | Support |
|---|---|---|---|---|
| certified no root on all components | success | complete | empty | certified |
| three verified roots while a fourth cannot be excluded | success | incomplete | finite | each returned root verified uncertified |
| three verified roots but exhaustiveness capability is unknown | success | not established | finite | each returned root verified uncertified |
| tangent candidate unresolved at work limit | work limit | not established | unresolved | unsupported for existence |
| source branch coincides with target over a resolved interval | success | complete or incomplete as evidenced | overlap | exact/certified/verified as evidenced |
| undefined target | invalid input | not established | unresolved | unsupported |

`EMPTY + INCOMPLETE`, `EMPTY + NOT_ESTABLISHED`, and `SUCCESS + stale payload`
are illegal. Solver convergence does not imply completeness. Result factories
should make contradictory combinations unrepresentable.

## 5. Finite-solution position and repeated coordinates

The semantic preimage/localization address of a root is not its durable
identity and is not its world coordinate:

```text
durable continuation context:
  (source pair, constructive intersection lineage, applicable branch lineage,
   topology/continuation context, root token)

revision-scoped localization evidence:
  (source revisions, component binding, canonical t, isolating interval,
   optional target u, residual/solver evidence)
```

Two different `t` values at a self-intersection remain two constructive
solutions even when `F(t1) = F(t2)`. Two branches that retrace the same geometric
curve retain constructive multiplicity. Deduplication occurs in semantic
parameter/isolation space within one preimage, not in Cartesian space.

Periodic endpoint-equivalent parameters are deduplicated only through the
provider's declared canonicalization. The continuation layer may retain a
lifted parameter/winding value privately so a root crossing the seam remains
continuous without creating duplicate public preimages.

An isolating interval is revision-scoped localization/certification evidence,
not fundamental durable identity. A known equivalent monotone
reparameterization must map the localization evidence without automatically
creating a new geometric intersection. G8A determines the supported invariance
subset for monotone maps, allowed orientation reversal, and seam
representations; outside it, identity is explicitly ambiguous or not
established rather than repaired by coordinates.

## 6. Classification as independent evidence

`IntersectionClassification2D` is a product of independent fields:

```text
contactClass:
  TRANSVERSE_ESTABLISHED | TANGENT_ESTABLISHED | CONTACT_UNDETERMINED

multiplicityEvidence:
  ESTABLISHED(order, evidence) | NOT_ESTABLISHED

domainLocation:
  INTERIOR | INCLUDED_ENDPOINT | PERIODIC_SEAM | ISOLATED_COMPONENT

sourceRegularity:
  REGULAR | SINGULAR | UNKNOWN

targetMembership:
  FULL_OBJECT | LIMITED_INTERIOR | LIMITED_ENDPOINT
```

The result-set kind separately represents `EMPTY`, `FINITE`, `OVERLAP`,
`INFINITELY_MANY`, `UNSUPPORTED_OVERLAP`, or `UNRESOLVED`. A tangent endpoint is therefore expressible
without inventing a combined enum value.

Multiplicity greater than one is a claim, not a formatting hint. G8A must
define the exact analytic/differential/interval evidence needed for each
supported claim. Tangency may be established while exact multiplicity remains
`NOT_ESTABLISHED`; uncertainty cannot be reported as transverse.

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
Intersection completeness/support are different axes and must not be added to
that enum.

## 8. Root token and lineage

`IntersectionRootToken2D` is an opaque algorithm-owned runtime identity. It is
not computed from a coordinate, label, parameter-bit hash, or isolating
interval. A current binding associates it with revision-scoped semantic and
numerical evidence.

Candidate closed identity statuses are:

- `CONTINUATION_ESTABLISHED`;
- `NEW_TOPOLOGICAL_SOLUTION`;
- `AMBIGUOUS_CONTINUATION`;
- `IDENTITY_DISCONTINUITY`; and
- `NOT_ESTABLISHED`.

An `IntersectionRootLineage2D` hypothesis has transitions analogous to, but
distinct from, G6 branch lineage:

- `UNCHANGED`;
- `APPEARED`;
- `DISAPPEARED`;
- `SPLIT` with one parent and at least two children; and
- `MERGED` with at least two parents and one child.

Root lineage cannot be inferred solely from branch lineage: one stable branch
may gain or lose roots against a moving target. Conversely, a branch split may
force root-lineage events even when coordinates remain coincident.

### G8A continuation and genealogy hypotheses

| Transition | Proposed semantic interpretation |
|---|---|
| one isolated root moves inside the same branch/component | preserve token only when a proven semantic continuation relation is unique; intervals/parameters are supporting evidence |
| equivalent monotone reparameterization | preserve geometric identity when an approved map carries the semantic continuation evidence; a changed interval alone is not a new root |
| two roots merge at a tangent event | candidate: terminate both parents and allocate one child with `MERGED` lineage when robustly established |
| tangent root splits | candidate: terminate parent and allocate two children with `SPLIT` lineage when correspondence is robustly established |
| periodic seam crossing | preserve token through provider equivalence and lifted continuation |
| included endpoint touch/reversal | retain or event-transition according to characterized local root topology; always record boundary location |
| invalid-domain gap | terminate at the gap; do not bridge it by coordinate proximity |
| branch/component topology change | require compatible provider/root lineage; otherwise begin a new topology epoch |

The new-token-on-merge/split model deliberately distinguishes stable
continuation from a topological event, but it is not yet universal semantics.
G8A must trace `2 -> 1 -> 2` and reverse traversal, symmetric ambiguous splits,
seam interactions, and nearby branch/component changes. When several
continuations are equally admissible, the result exposes ambiguity or identity
discontinuity. If the hypothesis fails, G8A recommends a narrower rigorous
contract.

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
output budget. Exceeding it makes completeness `INCOMPLETE` or the computation
work-limited;
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

- exact result-axis names and legal combinations within the approved rich-set
  plus normal-DAG rich-Geo architecture;
- root-token invariance and the supported reparameterization subset;
- merge/split genealogy or a narrower identity contract;
- classification and multiplicity evidence;
- overlap representation level; and
- the bounded optional point-output/currentness lifecycle.

Query-local-first computation and the rich-result authority are already
approved planning premises. Ordinary points remain optional derived consumers,
and no point projection may hide `INCOMPLETE` or `NOT_ESTABLISHED`
completeness.

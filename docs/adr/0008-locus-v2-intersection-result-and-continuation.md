# ADR 0008: Locus V2 intersection result and semantic root continuation

- Status: **Accepted — R1 clarification applied**
- Author review disposition: **ACCEPTED AT G8A CLOSEOUT; AUTHOR-APPROVED G8B-R1 CLARIFICATION APPLIED**
- Roadmap state: G8 planning/G8A/G8B-R1/G8B `PASS — AUTHOR APPROVED`; G8C design authorized/not started; G8 in progress
- Decision phase: final G8A author closeout
- Date: 2026-08-14

The author accepts this architecture after reviewing the complete G8A
characterization package. Acceptance makes the intersection specification
normative and authorizes a separately invoked G8B implementation task within
the internal scope below. It does not itself execute G8B or add observable
intersection behavior.

## Context

G6/G6R provide an experimental internal Locus V2 whose evaluator, semantic
parameter, branches, valid components, and revision are geometric authority.
G7 provides an internal rich metric-result lifecycle and bounded metric state,
but no incidence or intersection semantics. G8 must add native 2D intersection
meaning without treating a sampled polyline, output point array, coordinate
proximity, or viewport state as authority.

This is fundamental CeDG infrastructure: locus-defined projection curves are
genuine intermediate geometric results whose identified intersections must be
usable by later Construction-DAG steps whenever continuation is unambiguous.
An anonymous coordinate computed at one revision does not preserve
constructive traceability, branch/component provenance, semantic preimage,
dynamic identity, or topology change.

An intersection computation has more outcomes than a finite array of points:

- complete finite set, including complete empty;
- incomplete or not-established set completeness despite individually verified
  roots;
- unsupported target/capability;
- overlap or infinitely many solutions;
- invalid/degenerate sources;
- current versus stale computation; and
- topology events in which roots merge, split, cross a periodic seam, or lose
  their branch/component.

Each finite solution also has constructive identity and revision evidence. The
former includes source-pair/constructive/branch/topology continuation context;
the latter includes current revisions, component binding, semantic parameter,
isolating interval, residual, and solver evidence. Existing Classic
intersection algorithms use output
indices, permutations, and coordinate-near heuristics for their established
behavior. Those mechanisms cannot safely define V2 identity.

Repeated queries may benefit from candidate-isolation state, but the G7 metric
index contains cumulative length partitions and metric-policy state. Reusing it
would couple unrelated semantics and risk stale topology.

## Decision

1. Make an immutable `LocusIntersectionResult2D`-style value the semantic
   result. It carries orthogonal computation, completeness, geometry-set,
   currentness, support, and numeric-guarantee axes plus immutable finite
   solution/overlap/diagnostic evidence.
2. Publish that value atomically through a dedicated internal nonnumeric rich
   `GeoLocusIntersectionResult`-style `GeoElement` owned by a normal-DAG
   `AlgoLocusIntersectionV2`-style algorithm.
3. Require one internal derived `GeoPoint` consumer in the minimum G8B
   candidate. It selects one current finite solution by semantic root token,
   follows normal DAG dependencies, never becomes authority or silently
   retargets, becomes undefined for absence/staleness/ambiguity, and recovers
   only when the same semantic solution is current again under the continuation
   contract.
4. Identify a finite root with an opaque source-pair/constructive/
   branch/topology continuation token. Keep current semantic parameter,
   isolating interval, component binding, residual, and solver certificate as
   revision-scoped evidence rather than fundamental durable identity.
5. Continue a token only through a unique verified semantic continuation on
   approved G6 branch lineage, including an approved mapping for equivalent
   reparameterization. Coordinates are diagnostics, not matching keys. Cases
   without an invariant contract expose ambiguous/not-established identity.
6. Reject universal merge/split parent-child genealogy. Publish explicit
   topology events and candidate parent/child relations where established;
   preserve identity only where continuation is unique, and otherwise expose
   ambiguity or identity discontinuity.
7. Start query-local. Do not use the G7 metric owner/index. Consider a bounded
   intersection-specific revision-scoped owner only after G8A demonstrates a
   repeat-use benefit and the author separately approves key, payload,
   capacity, eviction, lifecycle, and cache-off equality.
8. Use existing G6 `NumericGuarantee` vocabulary where applicable, while
   defining independent G8 root/residual/tangency/dedup/continuation policies.
   Target residuals are model-distance-equivalent where correct, otherwise
   target-family-specific typed quantities with matching tolerances. Equation
   scaling cannot change geometry. Parameter tolerances remain in declared
   provider space, and tangency thresholds apply only to normalized contact
   evidence, never raw equation derivatives.
9. Start G8B with the author-approved measured tolerance values and
   deterministic work ceilings recorded in the normative specification. They
   are versioned implementation defaults with explicit normalization and
   provenance, not universal mathematical constants; wall clock is
   informational.
10. Limit the minimum productive target family to line, segment, ray and
    circle. Defer full conics, functions, general implicit curves and
    locus–locus.
11. Allow one append-only `GeoClass.LOCUS_INTERSECTION_RESULT`-equivalent value
    only if the audited rich Geo requires it; do not reuse an unrelated class
    or broaden the type system.
12. Keep public command/dispatcher, `Path`, XML/factory/persistence, legacy
   `GeoLocus`, Classic intersections, 3D/G9, and export out of this decision.

### G8B-R1 clarification — point admissibility versus completeness

The author clarified Decision 3 during focused G8B review. Set completeness and
selected-solution admissibility answer different questions:

- `COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED` says whether all roots over the
  supported query domain have been accounted for;
- a selected finite solution is point-admissible only when it is independently
  verified, current, locally isolated as a semantic preimage, bound coherently
  to its branch/component and source revisions, selected by a unique opaque
  token with explicit semantic continuation evidence, and free of identity
  ambiguity; and
- global completeness is retained as parent-result provenance but is not, by
  itself, a point-admissibility veto.

Thus a verified admissible root from an `INCOMPLETE` or `NOT_ESTABLISHED`
finite result may drive the internal derived point. That point neither implies
nor advertises exhaustive root coverage. Residual-only/localization-only
candidates, stale or failed results, overlap-kind results and ambiguous or
discontinuous identities remain inadmissible. Discovery of another root or a
change in result ordering cannot retarget an existing token-selected point.

This is a clarification of the already accepted rich-result and token-selected
consumer architecture, not a replacement of its historical rationale. It adds
no public command, `Path`, persistence, target family, shared state, 3D or G9
surface.

### Final G8B author closeout

On 2026-08-14 the author approved the implemented R1 predicate and the complete
minimum G8B kernel. The approval makes Option B and all strict negative cases
above final for G8B. It preserves this ADR's architecture without changing its
historical rationale. The implementation remains experimental/internal and
limited to line, segment, ray and circle.

The author separately authorizes only G8C design for full conics, functional
curves, general implicit curves and Locus V2 × Locus V2. G8C implementation is
not authorized, G8 remains in progress, and G9 has not started.

## Result invariants

- `EMPTY + COMPLETE` is publishable only with established exhaustive evidence.
- Found roots plus unresolved candidates are not a complete finite set.
- Verified-root count, `COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED`, and the
  completeness method/evidence are independently observable.
- Global completeness and point admissibility are orthogonal; a point keeps a
  dependency on the rich parent result and cannot strengthen its set claim.
- Local semantic-root isolation is explicit revision evidence and is required
  for point consumption; a small residual alone is insufficient.
- Tangency is not inferred from sign changes alone.
- Overlap/infinite sets are never converted into an arbitrary point sample.
- A finite solution is not deduplicated solely because another solution has the
  same Cartesian coordinate.
- Every finite point projection can be traced back to one current rich solution
  token and its source revisions.
- A source revision change makes the old snapshot non-current before new work
  is published.
- Exceptions and work exhaustion publish no partial current success.
- Retained state and token history are deterministically bounded.

## Alternatives

### A. Return only an ordinary variable-size point array

Advantages:

- resembles existing `AlgoIntersect*` outputs;
- simple for conventional downstream point consumers.

Disadvantages:

- cannot faithfully represent unresolved completeness, overlap, guarantee, or
  ordinary absence separately;
- encourages output-slot or coordinate identity;
- variable topology and history are hard to bound;
- discards constructive branch/preimage evidence.

Disposition: **rejected as semantic authority**. The accepted minimum includes
only the token-selected internal derived consumer described by the decision.

### B. Internal immutable result only, no rich GeoElement

Advantages:

- smallest production surface;
- straightforward test-private use.

Disadvantages:

- downstream kernel constructions cannot depend on the full result through the
  normal DAG;
- lifecycle/currentness risks becoming an algorithm-private side channel;
- weakens the stated G8 goal of first-class supported incidence.

Disposition: useful as the value layer, but **rejected alone** because G8B must
create a consumable kernel-semantic result. G8A verified the rich-Geo
lifecycle.

### C. Dedicated rich result plus derived points

Advantages:

- represents every set/numeric/lifecycle outcome explicitly;
- normal DAG integration and atomic currentness;
- keeps point ergonomics separate from authority;
- can preserve constructive root lineage.

Disadvantages:

- likely requires an append-only `GeoClass` and copy/set/remove/exhaustive-type
  tests;
- later point-slot/label policy remains a separate design problem;
- wider internal type surface than alternative B.

Disposition: **selected**, with one required internal token-selected point
consumer and no public/variable point array.

### D. Reuse Classic point-near/output-index continuity

Advantages:

- existing code and familiar UI behavior.

Disadvantages:

- coordinate-near matching is not constructive identity;
- fails repeated-coordinate, merge/split, and periodic-seam requirements;
- some existing heuristics are view-scale dependent.

Disposition: **rejected for G8 identity**; Classic behavior remains unchanged.

### E. Root identity as `(branch, component, parameter)` only

Advantages:

- semantic and simple within one fixed topology.

Disadvantages:

- component keys are revision-scoped;
- parameter values and isolating intervals move under edits and equivalent
  reparameterization;
- merge/split events have no single inherited tuple;
- periodic canonicalization needs a lifted context.

Disposition: **insufficient alone**. These fields are revision-scoped evidence
within an opaque constructive/topology continuation model.

### F. Global or Construction-wide intersection cache from the outset

Advantages:

- potentially high reuse.

Disadvantages:

- premature key/payload assumptions;
- hidden dependency and stale-revision risk;
- difficult bounded removal and multi-consumer ownership;
- no measured need.

Disposition: **rejected for the minimum**. Query-local is the author-approved
starting point.

### G. Reuse the G7 metric index

Advantages:

- already revision-scoped and bounded;
- contains adaptive component partitions.

Disadvantages:

- partitions are built for total variation/cumulative length, not root
  exclusion or target residual;
- metric tolerances/policies do not define intersection completeness;
- creates hidden semantic coupling and risks false candidate exclusion.

Disposition: **rejected**. The evaluator/session may be shared; metric state is
not intersection authority.

## Consequences

- G8A prototyped the rich Geo lifecycle and characterized identity/genealogy
  in test-private code before productive implementation.
- The result taxonomy, token events, normalized tolerance contract and bounds
  are part of the normative contract and validation matrix.
- Minimum productive support can remain line/segment/ray/circle while overlap
  and unsupported cases are still expressed truthfully.
- Public point/command/Path/XML behavior remains available for later explicit
  decisions without forcing it into the semantic kernel; only the internal
  token-selected point consumer is required now.
- Any future reusable state needs its own evidence and ADR amendment/new ADR.

## Consequences of replacing this decision

- Choosing point-only output would require an alternative explicit
  representation for incomplete/not-established completeness, overlap,
  guarantee, stale state, and root lineage
  before G8B can start.
- Choosing algorithm-private values only narrows G8B to a non-consumable
  service and changes the roadmap meaning of first-class incidence.
- Choosing a different identity policy requires re-running merge/split,
  repeated-preimage, seam, and topology tests; coordinate-near or slot-only
  identity remains prohibited by repository principles.
- Choosing shared state from the outset requires a measured ownership/key/
  eviction decision before implementation.

## Required characterization evidence

G8A provided:

1. a real-source lifecycle audit/probe for the immutable internal value,
   required rich Geo, and bounded token-selected derived point under the
   rich-result architecture;
2. deterministic traces for ordinary continuation, equivalent monotone
   reparameterization, allowed reversal, seam representation, branch loss,
   ambiguity, failure, and recovery;
3. controlled `2 -> 1 -> 2` and reverse traces recording tokens, identity
   status, semantic parameters, isolating intervals, topology revisions,
   candidate parent/child lineage, and ambiguity;
4. per-strategy verified-root counts plus independent completeness status,
   establishment method/failure evidence, and work counters;
5. proof that coordinate/label/output order is unnecessary for identity;
6. bounded retained-token/output behavior under many topology revisions;
7. query-local repeated/nested functional counters;
8. a measured reuse comparison if any cache/index is recommended; and
9. the exact proposed API and minimal upstream-owned edit set.

## G8A evidence and accepted resolution

The [G8A report](../validation/g8a_locus_v2_intersection_characterization_report.md)
and [traceability matrix](../validation/g8a_locus_v2_intersection_traceability_matrix.md)
provide 65 passing test-private probes and independent 80-digit references.
They support the rich value/rich Geo architecture, the independent
completeness axis, query-local state and a minimum line/segment/ray/circle
family.

They refine the identity proposal as follows:

- semantic parameter, component binding, isolating interval, residual,
  revisions and solver certificate are revision-scoped evidence;
- a token persists across a known monotone/reversed/periodic map only through
  one explicit semantic continuation relation; and
- universal merge/split child inheritance is rejected. A topology-event token
  records candidate parent/child sets, while symmetric or otherwise
  non-unique continuation is explicit ambiguity/discontinuity.

Repeated 1/3/10/100 and nested depth 1–3 traces retained zero intersection
entries, so the accepted minimum has no shared intersection owner. Full conics
and all Level C families remain deferred. The measured tolerances are accepted
through the explicit normalization contract, and the deterministic ceilings
are provisional initial implementation defaults.

## Approval record

On 2026-08-14 the author approved D1–D17, including:

- the immutable rich set and normal-DAG nonnumeric rich Geo as authority;
- the required internal token-selected derived point consumer;
- independent completeness and typed overlap/infinite semantics;
- analytic/certified/derivative-aware/evaluator-only capability ordering;
- the normalized tolerance contract and G8A-derived initial values;
- the provisional deterministic work ceilings;
- durable identity separated from all revision-scoped numerical evidence;
- explicit topology events without universal merge/split genealogy;
- query-local state with no G7 metric or shared intersection owner;
- line, segment, ray and circle as the minimum family, with Level C deferred;
- the internal/public/persistence/legacy/3D/G9 boundaries; and
- an append-only dedicated `GeoClass` only if the rich Geo requires it.

The normative specification records the exact semantic contract. The G8B
kernel and focused R1 refinement have been executed, remain internal, and are
`PASS — AUTHOR APPROVED`.

```text
ADR 0008 = ACCEPTED — R1 CLARIFICATION APPLIED
G8 SPEC = NORMATIVE / AUTHOR-APPROVED R1 REFINEMENT APPLIED
G8A = PASS — AUTHOR APPROVED
G8B-R1 = PASS — AUTHOR APPROVED
G8B = PASS — AUTHOR APPROVED
G8C DESIGN = AUTHORIZED — NOT STARTED
G8C IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
G8 = IN PROGRESS
G9 = NOT STARTED
```

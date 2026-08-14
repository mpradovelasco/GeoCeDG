# ADR 0008: Locus V2 intersection result and semantic root continuation

- Status: **Proposed**
- Author review disposition: **PLANNING ARCHITECTURE APPROVED; ADR ACCEPTANCE PENDING G8A**
- Roadmap state: G8 planning `PASS — AUTHOR APPROVED`; G8A authorized; G8B not authorized
- Decision phase: proposed G8A characterization and author closeout
- Date: 2026-08-14

This ADR remains a decision proposal, not an Accepted contract or
implementation authorization. The author approves the overall planning
architecture and G8A characterization premises below, but productive G8 work
is blocked until G8A evidence receives a second explicit author review, this
ADR is accepted or superseded, and the intersection specification is made
normative explicitly.

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

## Proposed decision

The author approves items 1–3 and the query-local starting point as planning
architecture. The detailed identity, genealogy, completeness-establishment,
numeric, and lifecycle contracts remain subject to G8A measurement and a
second author approval:

1. Make an immutable `LocusIntersectionResult2D`-style value the semantic
   result. It carries orthogonal computation, completeness, geometry-set,
   currentness, support, and numeric-guarantee axes plus immutable finite
   solution/overlap/diagnostic evidence.
2. Publish that value atomically through a dedicated internal nonnumeric rich
   `GeoLocusIntersectionResult`-style `GeoElement` owned by a normal-DAG
   `AlgoLocusIntersectionV2`-style algorithm.
3. Keep ordinary `GeoPoint` outputs optional and derived. They require a
   separate author decision, are bounded, and use rich-result root tokens; they
   never become the identity authority.
4. Identify a finite root with an opaque source-pair/constructive/
   branch/topology continuation token. Keep current semantic parameter,
   isolating interval, component binding, residual, and solver certificate as
   revision-scoped evidence rather than fundamental durable identity.
5. Continue a token only through a unique verified semantic continuation on
   approved G6 branch lineage, including an approved mapping for equivalent
   reparameterization. Coordinates are diagnostics, not matching keys. Cases
   without an invariant contract expose ambiguous/not-established identity.
6. Characterize merge/split parent-child genealogy as a G8A hypothesis, not a
   universal semantic. Test `2 -> 1 -> 2`, reverse traversal, symmetric
   ambiguity, seam interaction, and nearby branch/component changes. Preserve
   identity only where continuation is unambiguous; otherwise expose ambiguity
   or identity discontinuity.
7. Start query-local. Do not use the G7 metric owner/index. Consider a bounded
   intersection-specific revision-scoped owner only after G8A demonstrates a
   repeat-use benefit and the author separately approves key, payload,
   capacity, eviction, lifecycle, and cache-off equality.
8. Use existing G6 `NumericGuarantee` vocabulary where applicable, while
   defining independent G8 root/residual/tangency/dedup/continuation policies.
9. Keep public command/dispatcher, `Path`, XML/factory/persistence, legacy
   `GeoLocus`, Classic intersections, 3D/G9, and export out of this decision.

## Result invariants under the proposal

- `EMPTY + COMPLETE` is publishable only with established exhaustive evidence.
- Found roots plus unresolved candidates are not a complete finite set.
- Verified-root count, `COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED`, and the
  completeness method/evidence are independently observable.
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

Disposition: **not recommended** as semantic authority. It may be a later
derived adapter.

### B. Internal immutable result only, no rich GeoElement

Advantages:

- smallest production surface;
- straightforward test-private use.

Disadvantages:

- downstream kernel constructions cannot depend on the full result through the
  normal DAG;
- lifecycle/currentness risks becoming an algorithm-private side channel;
- weakens the stated G8 goal of first-class supported incidence.

Disposition: useful as the value layer, but **not recommended alone** if G8B is
to create a consumable kernel-semantic result. G8A must verify the rich-Geo
lifecycle before approval.

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

Disposition: **recommended working hypothesis for G8A**.

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

## Consequences if accepted

- G8A must prototype the rich Geo lifecycle and characterize identity/
  genealogy in test-private code before productive implementation.
- The result taxonomy, token events, and bounds become part of the proposed
  normative contract and validation matrix.
- Minimum productive support can remain line/segment/ray/circle while overlap
  and unsupported cases are still expressed truthfully.
- Public point/command/Path/XML behavior remains available for later explicit
  decisions without forcing it into the semantic kernel.
- Any future reusable state needs its own evidence and ADR amendment/new ADR.

## Consequences if rejected

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

G8A must provide at least:

1. a real-source lifecycle audit/probe for the immutable internal value,
   required rich Geo, and optional bounded derived points under the approved
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

## Approval record required

The author has approved the rich-set/rich-Geo planning architecture,
query-local-first computation, and optional-derived-point boundary. After G8A,
the author must explicitly choose:

- exact rich-Geo lifecycle/API and whether any ordinary points are included;
- completeness establishment policy and legal projections;
- durable token/reparameterization invariance and topology-lineage policy;
- merge/split hypothesis outcome plus seam/termination behavior;
- query-local versus separately characterized shared state.

Only then may this ADR change from `Proposed` to `Accepted` (or be superseded).

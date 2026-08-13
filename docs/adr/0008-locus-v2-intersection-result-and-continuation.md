# ADR 0008: Locus V2 intersection result and semantic root continuation

- Status: **Proposed**
- Author review disposition: **PENDING**
- Roadmap state: G8 `NOT STARTED`
- Decision phase: proposed G8A characterization and author closeout
- Date: 2026-08-13

This ADR is a decision proposal, not an accepted contract or implementation
authorization. Productive G8 work is blocked until G8A evidence is reviewed,
this ADR is accepted or superseded, and the intersection specification is made
normative explicitly.

## Context

G6/G6R provide an experimental internal Locus V2 whose evaluator, semantic
parameter, branches, valid components, and revision are geometric authority.
G7 provides an internal rich metric-result lifecycle and bounded metric state,
but no incidence or intersection semantics. G8 must add native 2D intersection
meaning without treating a sampled polyline, output point array, coordinate
proximity, or viewport state as authority.

An intersection computation has more outcomes than a finite array of points:

- complete finite set, including complete empty;
- incomplete or unresolved numerical search;
- unsupported target/capability;
- overlap or infinitely many solutions;
- invalid/degenerate sources;
- current versus stale computation; and
- topology events in which roots merge, split, cross a periodic seam, or lose
  their branch/component.

Each finite solution also has constructive identity: source revisions,
branch/component, semantic parameter, residual evidence, classification, and
continuation lineage. Existing Classic intersection algorithms use output
indices, permutations, and coordinate-near heuristics for their established
behavior. Those mechanisms cannot safely define V2 identity.

Repeated queries may benefit from candidate-isolation state, but the G7 metric
index contains cumulative length partitions and metric-policy state. Reusing it
would couple unrelated semantics and risk stale topology.

## Proposed decision

Subject to G8A measurement and author approval:

1. Make an immutable `LocusIntersectionResult2D`-style value the semantic
   result. It carries orthogonal computation, coverage, geometry-set,
   currentness, support, and numeric-guarantee axes plus immutable finite
   solution/overlap/diagnostic evidence.
2. Publish that value atomically through a dedicated internal nonnumeric rich
   `GeoLocusIntersectionResult`-style `GeoElement` owned by a normal-DAG
   `AlgoLocusIntersectionV2`-style algorithm.
3. Keep ordinary `GeoPoint` outputs optional and derived. They require a
   separate author decision, are bounded, and use rich-result root tokens; they
   never become the identity authority.
4. Identify a finite root with an opaque source-pair/topology-scoped token
   whose evidence includes the Locus V2 branch/component, canonical semantic
   parameter, isolating interval, and optional lifted periodic parameter.
5. Continue a token only through a unique verified semantic continuation on
   approved G6 branch lineage. Coordinates are diagnostics, not matching keys.
6. Represent merge, split, seam, boundary, and termination as explicit lineage
   events. A merge produces a new child of both parents; a split produces two
   children. Ambiguous continuation is unsupported rather than guessed.
7. Start query-local. Do not use the G7 metric owner/index. Consider a bounded
   intersection-specific revision-scoped owner only after G8A demonstrates a
   repeat-use benefit and the author separately approves key, payload,
   capacity, eviction, lifecycle, and cache-off equality.
8. Use existing G6 `NumericGuarantee` vocabulary where applicable, while
   defining independent G8 root/residual/tangency/dedup/continuation policies.
9. Keep public command/dispatcher, `Path`, XML/factory/persistence, legacy
   `GeoLocus`, Classic intersections, 3D/G9, and export out of this decision.

## Result invariants under the proposal

- `EMPTY_COMPLETE` is publishable only with established coverage.
- Found roots plus unresolved candidates are not a complete finite set.
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

- cannot faithfully represent unresolved coverage, overlap, guarantee, or
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
- parameter values move under dynamic edits;
- merge/split events have no single inherited tuple;
- periodic canonicalization needs a lifted context.

Disposition: **insufficient alone**. These fields are evidence within an opaque
token/topology-lineage model.

### F. Global or Construction-wide intersection cache from the outset

Advantages:

- potentially high reuse.

Disadvantages:

- premature key/payload assumptions;
- hidden dependency and stale-revision risk;
- difficult bounded removal and multi-consumer ownership;
- no measured need.

Disposition: **rejected for the minimum**. Query-local is the default working
hypothesis.

### G. Reuse the G7 metric index

Advantages:

- already revision-scoped and bounded;
- contains adaptive component partitions.

Disadvantages:

- partitions are built for total variation/cumulative length, not root
  exclusion or target residual;
- metric tolerances/policies do not define intersection coverage;
- creates hidden semantic coupling and risks false candidate exclusion.

Disposition: **rejected**. The evaluator/session may be shared; metric state is
not intersection authority.

## Consequences if accepted

- G8A must prototype the rich Geo lifecycle and root lineage in test-private
  code before productive implementation.
- The result taxonomy, token events, and bounds become part of the proposed
  normative contract and validation matrix.
- Minimum productive support can remain line/segment/ray/circle while overlap
  and unsupported cases are still expressed truthfully.
- Public point/command/Path/XML behavior remains available for later explicit
  decisions without forcing it into the semantic kernel.
- Any future reusable state needs its own evidence and ADR amendment/new ADR.

## Consequences if rejected

- Choosing point-only output requires an alternative explicit representation
  for incomplete coverage, overlap, guarantee, stale state, and root lineage
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

1. a real-source lifecycle audit/probe for rich Geo versus internal-only value
   versus bounded points;
2. deterministic traces for root continuation, merge, split, seam, branch loss,
   ambiguity, failure, and recovery;
3. proof that coordinate/label/output order is unnecessary for identity;
4. bounded retained-token/output behavior under many topology revisions;
5. query-local repeated/nested functional counters;
6. a measured reuse comparison if any cache/index is recommended; and
7. the exact proposed API and minimal upstream-owned edit set.

## Approval record required

The author must explicitly choose:

- result architecture (A/B/C or a documented replacement);
- root token and topology-lineage policy;
- merge/split/seam/termination behavior;
- point-output scope and bound, if any; and
- query-local versus separately characterized shared state.

Only then may this ADR change from `Proposed` to `Accepted` (or be superseded).


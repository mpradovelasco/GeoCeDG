# G9S1 semantic Spline V2 architecture

- Status: **PASS — AUTHOR APPROVED**
- Date: 2026-08-31
- Decision: Option B, new semantic `SplineV2` parent; Classic `Spline` unchanged
- Product gate: G9S1
- Approval: `selfApproved=false`, `authorApproved=true`, `passClaimed=true`

## Outcome

The smallest coherent seam is a new shared-kernel algorithm that owns an
explicit spline definition and publishes a normal `GeoLocusV2`. This reuses the
already approved semantic evaluator, rich metric, rich intersection, durable
identity, R4 token lifecycle, R5 transform and native persistence authorities.

```text
ordinary source GeoElements
        |
        v
SplineV2 semantic parent
  family / degree / oriented domain / spans / knots / evaluator
        |
        v
new GeoLocusV2 (durable ID, normal DAG)
        |
        +--> Point / rich length / guarded scalar Length
        +--> rich intersections / exact tokens / existing-point reactivation
        +--> R5 transformations
        +--> persistence / copy / undo
```

Classic `Spline` keeps its current dispatcher, algorithm and Cartesian-curve
output. There is no implicit conversion or migration.

## Semantic model

The provider snapshot is immutable for one construction revision. It contains
the declared domain and orientation, stable branch/component/span identities,
canonical knot ownership, local evaluators and validity/continuity metadata.
Evaluation happens at an explicit semantic address; rendering consumes the
snapshot but never defines it.

The implemented parent uses one branch, normalized oriented parameter `t` on
`[0,1]` (half-open when closed/periodic), normalized cumulative positive input
increments as knots, and one polynomial span per consecutive input pair. An
interior knot is owned by the span on its right; the terminal endpoint is owned
by the last span for nonperiodic evaluation. Span identity is constructive and
parameter-domain based, never render or solver enumeration order.

## Intersection integration

Spline-aware evidence belongs below the existing public rich-result contract:

- provider supplies deterministic span bounds and local polynomial data;
- a target-family adapter performs broad exclusion and span/cell isolation;
- refinement stays in original parameter authority;
- canonical shared-knot ownership deduplicates equivalent boundary roots;
- current root evidence feeds the R4 selector/token ledger;
- `GeoLocusIntersectionResult` remains the public semantic authority.

For line/segment/ray, circle, supported conic and regular polynomial implicit
targets, the common target captures polynomial coefficients. G9S1 composes them
with each semantic spline span, partitions the scalar residual at recursively
isolated derivative roots and refines deterministic cells. A transverse root
with established local isolation feeds the existing R4 selector/token ledger;
tangencies and zero-polynomial overlap fail closed. Bounded functions continue
through the existing general rich capability.

For two piecewise-polynomial loci, a separate common pair seam performs
canonical span-pair Bernstein-hull rejection, bounded subdivision and
safeguarded dual Newton refinement. It is symmetric under caller operand swap,
but its floating boxes do not establish interval-rounded rectangle coverage or
unique pair isolation. Pair results are therefore rich-only: no continuation
key, no active public ledger allocation and no materializable point. This is an
intentional candidate boundary, not a frontend omission. A future pair
materialization design must prove symmetric uniqueness and may not select an
arbitrary “first spline”.

## Numerical method

The implemented method is the bounded floating hybrid documented in
[the research note](../research/g9s1_semantic_spline_numerical_methods.md): a
scaled-pivot dense interpolation solve, power-basis semantic spans, one-sided
polynomial composition plus recursive derivative-root partition/bisection, and
pair Bernstein-hull subdivision plus dual Newton. It reports
`FLOATING_POINT_UNCERTIFIED` / estimated-error evidence. No interval arithmetic,
Sturm/Descartes certificate or global polynomial root count is implemented.

Every work limit is explicit and produces a typed unresolved state rather than
partial success presented as complete.

## Length and transformations

Length splits at semantic knots and validity boundaries and integrates the
analytic derivative in world coordinates with the shared deterministic adaptive
Simpson capability, Richardson-style error estimate and explicit work limit.
Partial endpoints remain semantic addresses. No sampled chord sum or symbolic
arc-length exactness is claimed.

The author-smoke correction retains `LocusLength` as the rich nonnumeric
surface and routes `Length(L,P,Q)` through a hidden reconstructible rich parent
plus the existing scalar-admissibility adapter. For evaluator-only Locus V2,
route evidence is computed directly on the resolved semantic interval by the
existing bounded adaptive evaluator; complete-component arc-coordinate
interpolation is not promoted into route-local evidence. Temporary invalid
source/endpoint state invalidates both rich and scalar publications and normal
DAG recovery recomputes them. This seam has no dependency on intersection or
render authority.

Direct route refinement is an explicit public-command capability choice. The
historical G7B evaluator-only constructor retains its original behavior:
interpolated subarcs remain `FLOATING_POINT_UNCERTIFIED`. This preserves the
distinction between independently established route evidence and evidence that
belongs only to the complete component.

R5 needs no spline-specific frontend route: it maps the published
`GeoLocusV2` provider as `T(C(t))`, retaining parameter orientation and spans
inside a new locus identity. Downstream query tokens are new.

## Persistence and transaction boundary

The ordinary command algorithm and its exact input list/degree/optional weight
reconstruct the provider. Knots, coefficients, bounds and solver cells are
derived and are not independent XML authority. No render cache or executable
closure is persisted. Creation, redefine, copy,
undo/redo and open remain atomic through existing construction/persistence
seams.

Feature-off and diagnostic Classic load/preserve without gaining creation
authority. Native `.cedg` and compatibility `.ggb` policy is unchanged.

## Expected implementation boundary

Expected shared-kernel changes are limited to:

- a `SplineV2` command/processor route under the existing Locus V2 opt-in;
- a semantic spline definition/evaluator/provider and normal parent algorithm;
- span-aware intersection and length adapters where the generic authority lacks
  sufficient polynomial evidence;
- localization and serialization registration required by the public command;
- focused JRE/Desktop persistence tests.

The exact Java class names and changed-file list are implementation evidence,
not normative design. No Desktop geometry logic is permitted.

## Compatibility and retained risks

The open
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` risk remains tracked. G9S1 must
not mark it resolved unless it adds the missing dedicated native round trip;
otherwise G9U1 must revisit it and global G9 closeout must resolve or explicitly
dispose it.

The R5 manual-smoke limitation also remains prospective product work: typing
`k=0.25` as free input is atomically rejected by the current G9A
`REDEFINE_CONTEXT_MISSING` contract, while slider and explicit existing-object
edits recompute correctly. G9S1 neither changes that transaction seam nor turns
a label into durable identity. The post-G9S1 G9U1 prompt retains the bounded
compatible-redefine UX requirement.

## Status boundary

This architecture records the author-approved G9S1 implementation. The author,
not automation, supplied the PASS decision. It does not execute G9U1 or change
Classic `Spline`.

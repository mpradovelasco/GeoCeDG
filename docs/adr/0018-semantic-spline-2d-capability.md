# ADR 0018: Semantic Spline V2 as a Locus V2 parent

- Status: **Accepted — PASS — AUTHOR APPROVED**
- Date: 2026-08-31
- Phase: G9S1 — Semantic Spline 2D Capability
- Decision owner: GeoCeDG author
- Normative specification:
  [Semantic Spline V2](../../geocedg/specs/curves/semantic-spline-2d.md)

## Context

The inherited `Spline` command produces an upstream Cartesian curve whose
parameterization is sufficient for its established Classic behavior, but it
does not expose the stable span, knot, branch and oriented-domain authority
required by GeoCeDG intersections, deterministic solution identity, rich
length, persistence and transformation covariance.

Replacing or retrofitting the Classic object would change an upstream public
contract and make legacy files depend on new experimental semantics. A sampled
polyline or render wrapper would also violate the Locus V2 separation between
semantic geometry and presentation.

## Decision

1. Preserve Classic `Spline` and every existing non-V2 overload unchanged.
2. Introduce the bounded experimental public constructor `SplineV2`. Its
   semantic parent owns the supported spline family, degree/order, source
   dependencies, explicit oriented parameter domain, stable span/knot
   partition, continuity and degeneration metadata.
3. Publish the result as a new first-class `GeoLocusV2` with a new durable
   identity. The semantic spline definition is its provider authority; render
   tessellation is derived presentation only.
4. Reuse the existing Locus V2 feature opt-in and public consumers. Do not add
   a second spline-intersection flag, a generic `Path` conformance or parallel
   metric/intersection/token system.
5. Define canonical half-open ownership of a knot root. A geometric point on a
   shared knot belongs to one deterministic semantic span/cell, while genuine
   distinct preimages remain distinct.
6. Extend the common rich-intersection path with span-aware polynomial evidence.
   For a polynomial target, recursively isolate derivative roots, partition each
   semantic source span into monotone cells and refine accepted cells in the
   original parameter. The resulting evidence remains floating and does not
   claim interval-certified global root counting.
7. Add deterministic Bernstein-hull subdivision for two piecewise-polynomial
   loci only as rich-result candidate evidence. Until a symmetric
   interval-rounded rectangle and uniqueness certificate exists, pair roots
   carry no public continuation key, create no active ledger allocation and are
   not point-materializable.
8. Use durable curve/source identity, oriented branch/component/span lineage,
   canonical parameter cell and typed germ as semantic identity inputs.
   Coordinates, solver enumeration, output index, render samples, proximity
   and motion history are forbidden identity authorities.
9. Preserve R4: deterministic current-state selection outranks a continuity
   heuristic; existing exact-token points may become dormant and reactivate,
   but recompute never creates new points.
10. Route total/partial length, Point-on-Locus, rich intersections, persistence,
   copy/undo and R5 similarity transforms through the existing `GeoLocusV2`
   authorities. A transformed spline image receives new object and query
   identities.

## Numerical-method constraint

The implementation candidate uses a bounded floating-coefficient polynomial
model. One-sided polynomial targets are substituted spanwise; recursively
isolated derivative roots partition the scalar residual, and deterministic
bisection/safeguarded refinement handles accepted cells, including even-contact
discovery. Pair queries use deterministic Bernstein convex-hull rejection and
dual-parameter subdivision/Newton refinement. These methods publish estimated
floating evidence only. They do not establish exact arithmetic, interval
enclosures, exhaustive pair coverage or pair uniqueness.

The source-backed comparison and the scholarly work still required before any
stronger theorem or certification claim are recorded in
[the numerical-method research note](../research/g9s1_semantic_spline_numerical_methods.md).

Every result must report what was established: discovery, residual/refinement,
local isolation, multiplicity/contact, completeness and work-limit status may
not be collapsed into one Boolean.

## Compatibility and persistence

- Existing `Spline` constructions and Classic command dispatch are unchanged.
- Existing `GeoLocusV2` XML/container authority is reused; no render vertices,
  Java closures or mutable caches are serialized.
- Spline family/version, source dependencies, oriented domain and stable
  span/knot semantics must reconstruct the semantic provider.
- Files load with the feature unavailable under the existing preservation
  policy; Classic gains no experimental creation authority.

## Consequences

- The public V2 spline is immediately consumable by approved Locus V2
  operations while retaining a spline-specific exact semantic definition.
- The shared kernel, not Desktop presentation, owns spline truth.
- Scientific validation must cover knot roots, tangencies, multiplicities,
  repeated knots, degenerations, rich-only pair intersections and deterministic
  reruns. Pair materialization remains explicitly outside the candidate claim.
- G9U1 may later expose current tokens as markers and materialization actions;
  it may not create a second marker-order identity system.

## Alternatives

### Extend Classic `Spline` in place

Rejected for G9S1. It would alter upstream behavior and still require an
explicit migration policy for legacy Cartesian-curve semantics.

### Represent the result as sampled/render geometry

Rejected. Samples cannot own incidence, length, identity or persistence.

### Add a standalone spline geometry/token stack

Rejected. It would duplicate the already approved Locus V2 semantic and rich
result architecture.

## Status boundary

This ADR is **Accepted**. The author approved the completed G9S1 implementation
after both the original smoke and the reduced partial-length re-smoke. The
approval does not broaden the explicitly rich-only spline × spline boundary or
authorize G9U1.

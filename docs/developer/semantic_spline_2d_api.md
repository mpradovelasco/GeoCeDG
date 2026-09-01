# Semantic Spline V2 developer API

- Status: **G9S1 PASS — AUTHOR APPROVED**
- Public maturity: experimental, default off with Locus V2
- Kernel result type: `GeoLocusV2`

## Public construction

`SplineV2` is the bounded semantic constructor. Its localized public forms are:

- `SplineV2[<List of Points>]` (degree 3);
- `SplineV2[<List of Points>, <Degree>]`;
- `SplineV2[<List of Points>, <Degree>, <Weight Function>]`; and
- `SplineV2[<Point>, <Point>, ...]` through the ordinary point-list wrapping
  seam.

The candidate accepts 3–32 finite 2D points, integral degree 3 through
`min(point-count,12)`, at most 512 dense unknowns and only positive finite
weight increments. Classic `Spline` is unchanged.

The command returns one new `GeoLocusV2`. The owning algorithm keeps normal
dependencies on every source/control/interpolation input and publishes an
immutable semantic spline provider for the current revision.

## Provider contract

Consumers may request:

- family and semantic version;
- explicit oriented domain;
- branch/component/span descriptors;
- canonical parameter and knot ownership;
- value and supported derivative evaluation;
- span bounds/local polynomial data where truthfully available;
- continuity, degeneration and validity diagnostics.

The concrete candidate provider capability is immutable
piecewise-polynomial data in the normalized oriented parameter. Interior knots
are right-owned. Equal first/last input points use a periodic half-open domain.

Consumers must not infer meaning from provider array order, render vertices,
labels or Cartesian coordinates.

## Existing consumer APIs

Use existing Locus V2 entry points for:

- point evaluation/materialization by semantic address;
- `Length(S)` and `Length(S,P,Q)` ordinary guarded scalar results;
- `LocusLength(S)` and `LocusLength(S,P,Q)` rich status/evidence results;
- `Intersect` rich results and exact-token point children;
- R5 transforms;
- copy, undo/redo and XML persistence.

Spline-specific adapters may provide stronger span evidence below these public
authorities. They must not introduce parallel public token or metric types.

The ordinary partial scalar owns no integration. Its normal DAG is the exact
semantic source and addressed endpoints, a hidden reconstructible
`GeoLocusMetricResult`, and the existing guarded scalar adapter. A rich result
is intentionally nonnumeric; inspect its typed payload rather than interpreting
an empty Algebra value cell as an absent metric. Endpoint provenance from a
different locus or an arbitrary Cartesian-coincident point fails closed.

For one-sided polynomial targets, the adapter publishes estimated floating
evidence and permits only established transverse isolating cells to reach the
R4 selector/token ledger. Bounded functions may use the existing general rich
fallback.

For two piecewise-polynomial loci, G9S1 publishes deterministic rich-only
candidate/overlap/work evidence. There is no certified unique pair selector,
active ledger allocation or point materialization. Do not treat a diagnostic
pair token as a durable exact-token point handle.

## Identity and lifecycle

The spline locus receives a new durable ID. Span/knot lineage is semantic
provider metadata inside that namespace. Rich intersection points bind existing
opaque exact tokens to deterministic selectors; the token is not a coordinate,
span index or proof of exact arithmetic.

R4 active/dormant/reactivated behavior applies. A child resolves through the
current selector map; it does not trigger another full solve or use nearest-root
tracking. Recompute never materializes a new point.

## Error and work reporting

Unsupported family/target, nonfinite source, invalid domain, degenerate or
nonisolated root, ambiguity, overlap and work-budget exhaustion must remain
typed. Discovery, isolation, residual accuracy and completeness are separate.

## Persistence

The parent algorithm serializes/reconstructs its ordinary list, degree and
optional weight dependencies through the existing command/XML seam plus normal
`GeoLocusV2` identity/style state. Coefficients, knots, bounds and solver cells
are derived. Do not serialize Java implementation objects, render caches or
solver order.

## Verification boundary

See the
[G9S1 matrix](../validation/g9s1_semantic_spline_2d_capability_validation_matrix.md).
This guide describes the approved G9S1 surface. Executed counts and hashes must
still be taken from the focused/composed verifier evidence rather than inferred
from prose.

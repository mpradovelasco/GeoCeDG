# Semantic Spline V2 developer API

- Status: **G9S1 PASS — AUTHOR APPROVED**
- Current successor: **G9S1-R1 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW; NOT phase PASS**
- `implementationComplete=true`: bounded construction-precision and univariate
  structural-certification corrections pass fresh PHASE A/B, COMPOSED and FULL
  clean on the archived tested source cohort. Earlier failures remain historical
  evidence; this later status-only reconciliation is not a new runtime cohort.
- `manualAuthorSmoke=PENDING`; `selfApproved=false`;
  `authorApprovedPhase=false`; `passClaimed=false`.
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

The provider capability is immutable semantic spline data in the normalized
oriented parameter. Interior knots are right-owned. Exactly equal finite
first/last input points can use a periodic half-open domain. Under the authorized
R1 correction, approximate endpoint coincidence alone cannot establish exact
periodic closure; an inconsistent closed-source request fails explicitly.

### R1 structural representation prerequisite

The [structural design](../architecture/g9s1_r1_structural_spline_continuity.md)
and [ADR 0022](../adr/0022-structural-spline-continuity.md) replace independent
rounded-span authority with an equivalent reduced structural basis. Shared
truncated powers give C^(d-1) at simple interior knots; exact elimination of
periodic endpoint jets is a separate obligation. Preserve the original
interpolation/boundary equations, including selected-span polynomial extension
rows. Numerical free coefficients and residuals remain numerical: structural
continuity does not claim exact interpolation arithmetic.

Rounded expanded span arrays may serve ordinary approximate evaluation and
candidate discovery. The R1 interval certificate must instead outward-enclose
the exact structural numerator/denominator expansion and actual R5 composition.
It must not certify a different rounded-cache function. Private independent-span
diagnostic fixtures carry no inferred structural guarantee. Derived content
signatures change with representation; source durable identity, oriented
parameter contract and normal command reconstruction do not.

The original native-knot failure is retained in the
[blocker report](../validation/g9s1_r1_implementation_blocker_report.md).
These changes are authorized work in progress, not already validated product
claims; the complete structural and historical perimeter remains required.

The corrective candidate exposes `SplinePolynomialModel2D.getConstructionEvidence()`
with policy `spline-structural-precision/v1`: arithmetic route, working/retained
precision, solve/expansion/admission work and failure categories. This evidence
is query/revision-local derived data, not a durable selector or XML payload.
The binary64 route remains preferred when original admission passes; bounded
higher-precision recovery cannot waive original equations or structural jets.

For `REGULAR_POLYNOMIAL_IMPLICIT` stored coefficient authority,
`SplineImplicitIntervalCertification2D` verifies
the actual structural composition and derivative before public classification.
Floating composed polynomial roots are proposals only. Exclusion publishes no
verified root; simple/transverse admission requires current existence/uniqueness
and derivative-sign proof. Genuine but uncertified contacts remain rich-only.
This is an internal prerequisite correction, not a new command, pair selector,
materialization policy or GUI feature. Current validation is still incomplete.
Line/conic/circle historical paths are unchanged: a circle's rounded expanded
matrix is not exact authority for its center/radius-defined target.

Native replacement serialization reuses the existing G9A lexical identity
overlay: `GeoLocusV2.getXML` may serialize a staged element with an exact
`getPersistentGeoIdForSerialization` association before live attachment. This
does not alter the G9A compatible predicate or explicit incompatible replacement
transaction and does not assign a durable identity by label. The earlier missing
staged element, not an unsupported Spline redefine policy, was the bounded defect.

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

For two piecewise-polynomial loci, the published G9S1 baseline provides rich-only
candidate/overlap/work evidence. There is no certified unique pair selector,
active ledger allocation or point materialization. Do not treat a diagnostic
pair token as a durable exact-token point handle.

The bounded R1 successor adds a separate authenticated SplineV2 pair path under
[ADR 0021](../adr/0021-spline-pair-singleton-germ-materialization.md). A current
interval existence/uniqueness proof plus complete selected-sign coverage over
the component product may admit a singleton normalized transverse germ. Both
opposite signs may qualify; repeated same-germ sheets, unresolved coverage,
tangency/overlap, generic polynomial providers and uncertifiable composition
remain rich-only. This is not an uncertified materialization option.

`PairSemanticSlotSelector2D` associates both durable source descriptors with
their branch/component/orientation/domain and normalized germ. Current u/v,
proof rectangles, spans, knots, coefficients and revisions stay outside that
durable selector. Pair-bearing ledger v5 keeps a strict pair/one-source
discriminator; existing one-source import and opaque `locus-root/v3` token
semantics remain. `PairRootAddressProof2D` holds current two-source address
evidence. The same claimed point may become dormant or pair-non-current and
reactivate only through current unique selector lookup, never a previous
trajectory. R4 periodic quarantine remains a different lifecycle contract.

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
The approved G9S1 baseline and authorized-but-unapproved R1 work are explicitly
separate. The [R1 matrix](../validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
adds structural, periodic, pair-proof and lifecycle requirements. Executed
counts/hashes must come from current PHASE/COMPOSED/FULL evidence, not this guide.
G9U1 is not implemented and the independent R4 quarantine round-trip risk remains open.

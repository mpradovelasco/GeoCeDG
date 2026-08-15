# Locus V2 extended-intersection candidate API

**Status: G8C1 INTERNAL API IMPLEMENTED — AUTHOR APPROVED;
G8C2 API PROPOSED — NOT AUTHORIZED/NOT IMPLEMENTED**

G8C1 semantic responsibilities are normative and the concrete additive names
below describe the author-approved internal kernel. G8C2 types remain illustrative
and proposed. Existing G8B rich-result and point-consumer types remain
authority.

## 1. G8C1 target adapter

```java
interface LocusIntersectionTarget2D {
    TargetFamily getFamily();
    String getTargetIdentity();
    long getTargetUpdateStamp();
    IntersectionResidualContract2D getResidualContract();
    IntersectionTargetDomain2D getDomainContract();
    TargetCandidateEvaluation2D evaluateCandidateLevel(LocusPoint2D point);
    TargetResidualEvaluation2D evaluateResidualEvidence(LocusPoint2D point);
    TargetMembership2D evaluateMembership(LocusPoint2D point,
            double coordinateTolerance);
    TargetContactEvidence2D evaluateContact(LocusPoint2D point,
            LocusDifferentialEvaluation2D differential);
}
```

`IntersectionResidualContract2D` and `TargetResidualEvaluation2D` carry
quantity/units, normalization, characteristic scale and typed availability.
They cannot be a bare interchangeable `double`. Candidate level evaluation
does not itself verify membership.

Author-approved G8C1 closed adapters:

- `NondegenerateConicIntersectionTarget2D` for ellipse/parabola/hyperbola;
- `BoundedFunctionGraphIntersectionTarget2D`;
- `RegularPolynomialImplicitIntersectionTarget2D`.

`LocusIntersectionTargets2D.assess()` returns typed rejection
(`UNSUPPORTED_TARGET_SUBTYPE`,
`DOMAIN_NOT_EXPLICIT`, `RESIDUAL_NORMALIZATION_UNAVAILABLE`,
`NONPOLYNOMIAL_IMPLICIT`, `TARGET_UNDEFINED`, or equivalent), never a `null`,
NaN, or magic value. Singular local implicit geometry is represented by
`TargetEvaluationStatus.RESIDUAL_NORMALIZATION_UNAVAILABLE` during candidate
and verification evaluation.

## 2. Pair query

```java
record LocusPairIntersectionQuery2D(
        LocusSourceBinding2D first,
        LocusSourceBinding2D second,
        LocusPairIntersectionPolicy2D policy) {
}

record LocusPairLocalizationEvidence2D(
        ParameterInterval2D firstInterval,
        ParameterInterval2D secondInterval,
        PairCoverageStatus coverage,
        PairUniquenessStatus uniqueness,
        NumericGuarantee2D guarantee) {
}

record LocusPairResidualEvidence2D(
        LocusPoint2D firstPoint,
        LocusPoint2D secondPoint,
        double modelCoordinateResidual,
        double absoluteError,
        NumericGuarantee2D guarantee) {
}
```

The geometric key is a canonical unordered pair of semantic locus identities.
`first`/`second` remain an ordered computation view. A `reversed()` mapping swaps
all ordered evidence and determinant orientation without changing the durable
solution token.

## 3. Additive solution evidence

The preferred design extends `LocusIntersectionSolution2D` with a closed
localization variant rather than creating a parallel solution hierarchy:

```text
SingleParameterLocalization
    branch/component/parameter/interval

DualParameterLocalization
    branch/component A + parameter/interval A
    branch/component B + parameter/interval B
    isolating rectangle + uniqueness basis
```

Common fields remain token, coordinate, residual/error, classification,
guarantee, currentness, identity status, continuation/topology and diagnostics.
The parameter pair/rectangle is revision evidence, never the token.

## 4. Local isolation

Existing `LocalIsolationStatus` should be reused if it can carry a typed basis.
Otherwise add one evidence object, not a competing status enum:

```java
record LocalIsolationEvidence2D(
        LocalIsolationStatus status,
        IsolationMethod2D method,
        CoverageEvidence2D coverage,
        UniquenessEvidence2D uniqueness,
        NumericGuarantee2D guarantee) {
}
```

For pair roots, `ESTABLISHED` requires an isolating parameter rectangle and
justified uniqueness. Residual or converged Newton state alone maps to
`NOT_ESTABLISHED`.

## 5. Tangency and overlap

Pair classification stores normalized
`det(F'(t),Q'(u))/(||F'|| ||Q'||)` only when both tangents are regular. It uses
the existing established/undetermined classification pattern. Multiplicity is
optional evidence, never inferred from a raw threshold.

Overlap is a rich set contribution:

```text
ESTABLISHED     semantic component relation / parameter map / guarantee
SUSPECTED       candidate evidence, no finite-point projection
UNSUPPORTED     capability absent with diagnostics
```

Mixed finite-plus-overlap results require explicit decomposition in the parent
result. They cannot silently discard either contribution.

## 6. Algorithm and point consumer

G8C1 extends the current `AlgoLocusIntersectionV2` through its typed target
seam and publishes the existing rich Geo. G8C2 should use a two-input
`AlgoLocusLocusIntersectionV2`-style algorithm if that makes DAG input and
revision capture explicit. Both publish the existing
`GeoLocusIntersectionResult` authority.

The current strict token-selected point consumer is reused. It performs no
search, sees parent completeness as provenance, and remains undefined when its
pair solution is absent, stale, unsupported, unverified, unisolated, ambiguous,
or overlap-only.

## 7. Policies and counters

G8C1 adds target candidate, derivative, domain and invalid-evaluation counters
to `LocusIntersectionInstrumentationSnapshot2D`. G8C2
adds branch/component pair counts, boxes visited/rejected, pair refinements,
Jacobian evaluations, overlap checks and two-source continuation comparisons.
All limits are versioned, deterministic and query-local. No public setters or
serialization surface is proposed.

## 8. API boundaries

No command processor, `AlgoDispatcher` method, generic `Path`, XML/factory,
legacy `GeoLocus`, frontend, 3D, or G9 API is part of either phase contract. Any need
for such a surface stops the corresponding implementation for author review.

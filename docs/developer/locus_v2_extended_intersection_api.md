# Locus V2 extended-intersection candidate API

**Status: G8C1 CONTRACT NORMATIVE — AUTHOR APPROVED / NOT IMPLEMENTED;
G8C2 API PROPOSED — NOT AUTHORIZED**

G8C1 semantic responsibilities are normative; concrete additive names remain
illustrative until reconciled with the audited G8B API during the authorized
implementation. G8C2 types remain proposed. Existing G8B types remain
authority.

## 1. G8C1 target adapter

```java
interface ExtendedIntersectionTarget2D<T extends GeoElement> {
    TargetBinding2D bind(T target, long revision);
    TargetSupport2D support();
    DomainContract2D domain();
    CandidateLevel2D candidateLevel(LocusPoint2D point);
    ResidualEvidence2D verify(LocusPoint2D point, ResidualPolicy2D policy);
    DerivativeCapability2D derivatives();
    BoundsCapability2D semanticBounds();
}
```

`ResidualEvidence2D` must carry quantity/units, normalization, error/tolerance,
guarantee and validity preconditions. It cannot be a bare interchangeable
`double`. Candidate level evaluation does not itself verify membership.

Author-approved G8C1 closed adapters:

- `NondegenerateConicIntersectionTarget2D` for ellipse/parabola/hyperbola;
- `BoundedFunctionGraphIntersectionTarget2D`;
- `RegularPolynomialImplicitIntersectionTarget2D`.

Factory rejection is typed (`UNSUPPORTED_TARGET_SUBTYPE`,
`DOMAIN_NOT_EXPLICIT`, `RESIDUAL_NORMALIZATION_UNAVAILABLE`,
`SINGULAR_LOCAL_GEOMETRY`, or equivalent), never a `null`, NaN, or magic value.

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

G8C1 should extend the current `AlgoLocusIntersectionV2` only if its typed
target seam remains clear. G8C2 should use a two-input
`AlgoLocusLocusIntersectionV2`-style algorithm if that makes DAG input and
revision capture explicit. Both publish the existing
`GeoLocusIntersectionResult` authority.

The current strict token-selected point consumer is reused. It performs no
search, sees parent completeness as provenance, and remains undefined when its
pair solution is absent, stale, unsupported, unverified, unisolated, ambiguous,
or overlap-only.

## 7. Policies and counters

G8C1 adds target/derivative evaluations and invalid-boundary counters. G8C2
adds branch/component pair counts, boxes visited/rejected, pair refinements,
Jacobian evaluations, overlap checks and two-source continuation comparisons.
All limits are versioned, deterministic and query-local. No public setters or
serialization surface is proposed.

## 8. API boundaries

No command processor, `AlgoDispatcher` method, generic `Path`, XML/factory,
legacy `GeoLocus`, frontend, 3D, or G9 API is part of either phase contract. Any need
for such a surface stops the corresponding implementation for author review.
